package com.dts.framework.spring;

import com.alibaba.fastjson.JSON;
import com.dts.dlxmq.dlx.DlxConst;
import com.dts.dlxmq.dlx.DlxMessageProducer;
import com.dts.framework.annotation.TxClient;
import com.dts.framework.annotation.TxServer;
import com.dts.framework.support.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jook
 * @create 2019-03-25 19:35
 **/
public class TxInterceptor implements MethodInterceptor, Serializable, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private AmqpTemplate amqpTemplate;
    protected final Log logger = LogFactory.getLog(getClass());

    private ThreadLocal<TxContext> LOCAL_TX_CONTEXT = TxContextSupport.getTxContextThreadLocal();
    //调用方对应的mq路径
    private Map<String, String> SERVER_MQ_MAP = new ConcurrentHashMap<>();
    // 本地方法记录
    private List<String> injvmMethodName = new ArrayList<>();
    private List<String> serverthodName = new ArrayList<>();
    // 记录server第几个参数为RecheckBean
    private Map<String, Integer> recheckBeanIndex = new HashMap<>();
    //server方法是否支持本地单接口反查
    private Map<String, Boolean> LOCAL_SERVER_METHOD_SUPPORT_RECHECK = new HashMap<>();
    private DlxMessageProducer dlxMessageProducer;


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        String name = method.toString();

        if (injvmMethodName.contains(name)) {
            return invokeWithinTransaction(invocation);
        } else if (serverthodName.contains(name)) {
            return invokeServerTranscation(invocation);
        }
        //初始化缓存
        TxClient txClient = invocation.getMethod().getAnnotation(TxClient.class);
        if (txClient != null) {
            injvmMethodName.add(name);
            return invokeWithinTransaction(invocation);
        } else {
            serverthodName.add(name);
            return invokeServerTranscation(invocation);
        }
    }

    /**
     * 调用带@TxServer注解的方法。
     * 区分本地调用和rpc调用
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    private Object invokeServerTranscation(MethodInvocation invocation) throws Throwable {
        Object revalue = null;
        try {
            //本地调用,只有被jdk代理时才会进入. JDK代理 proxy的  targetclass为interface. cglib为xximpl
            if (serverInJvm(invocation.getClass())) {
                //本地调用结束
                revalue = invocation.proceed();
                //rpc过来的请求,触发的本地调用 是否满足recheck
                RecheckBean recheckBean = null;
                if ((recheckBean = localInvokeByRpc(invocation)) != null) {
                    if (dlxMessageProducer == null) {
                        String beanId = DlxConst.DLX_MESSAGE_CONSUMER_BEAN_ID;
                        if (!StringUtils.isEmpty(beanId)) {
                            dlxMessageProducer = applicationContext.getBean("dlxMessageProducer", DlxMessageProducer.class);
                        }
                    }
                    dlxMessageProducer.sendMessageToDlx(JSON.toJSONString(recheckBean), recheckBean.getFirstRecheckSecond());
                }
            } else {
                //开启事务 - txflow在第一次调用注解TxServer的服务时候生成
                checkAndopenTx(invocation.getMethod(), invocation.getMethod().toString());
                //远程调用
                Method method = invocation.getMethod();
                TxServer ts = null;
                if (StringUtils.isEmpty((SERVER_MQ_MAP.get(method.toString())))) {
                    ts = (TxServer) ProxyMethodTXCache.get(invocation.getMethod().toString());
                    if (StringUtils.isEmpty(ts.mqInfo())) {
                        throw new Exception("error: " + method.toString() + " annotation can not find mqInfo");
                    }
                    SERVER_MQ_MAP.put(method.toString(), ts.mqInfo());
                }

                // 填入回调参数
                Integer index = recheckBeanIndex.get(method.toString());
                if (index == null) {
                    index = getRecheckBeanIndex(invocation);
                }
                //-1 表示server接口不需要recheck, >-1 填入参数
                if (index > -1) {
                    Object obj = invocation.getArguments()[index];
                    //必须有默认构造
                    if (obj == null) {
                        obj = method.getParameterTypes()[index].newInstance();
                        invocation.getArguments()[index] = obj;
                    }
                    BeanUtils.copyProperties(LOCAL_TX_CONTEXT.get().getRecheckMap().get(method.toString()), obj);
                }
                revalue = invocation.proceed();
                LOCAL_TX_CONTEXT.get().getInvokeMethodNameList().add(method.toString());
            }
        } catch (Throwable ex) {
            if (logger.isTraceEnabled()) {
                logger.trace("invokeServerTranscation error ", ex);
            }
            throw ex;
        }
        return revalue;
    }

    /**
     * 本地带@TxClient 注解的方法调用
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    private Object invokeWithinTransaction(MethodInvocation invocation) throws Throwable {
        Object revalue = null;
        //事务开启和集成在 调用server时候创建
        if (LOCAL_TX_CONTEXT.get().getTxClient() == null) {
            TxClient txClient = invocation.getMethod().getAnnotation(TxClient.class);
            LOCAL_TX_CONTEXT.get().setTxClient(txClient);
        }
        try {
            revalue = invocation.proceed();
            closeTx();
        } catch (Throwable ex) {
            completeTransactionAfterThrowing(ex);
            throw ex;
        } finally {
            sendMessage();
            LOCAL_TX_CONTEXT.remove();
        }
        return revalue;
    }

    /**
     * 目前只支持单线程事务传递 PROPAGATION_REQUIRED 类型
     */
    private void checkAndopenTx(Method method, String methodName) throws Throwable {
        TxContext context = LOCAL_TX_CONTEXT.get();
        String flowId = context.getTxFlowId();
        if (StringUtils.isEmpty(flowId)) {
            throw new Exception("not find txflowId");
        }
        //首次开启 初始化参数
        if (!context.isStart()) {
            context.setMethod(method.toString());
            TxMessage message = new TxMessage();
            message.setTxFlow(flowId);
            context.setMessage(message);
            RecheckBean recheckBean;
            if ((recheckBean = LOCAL_TX_CONTEXT.get().getRecheckMap().get(methodName)) == null) {
                TxClient txClient = LOCAL_TX_CONTEXT.get().getTxClient();
                if (txClient != null) {
                    recheckBean = new RecheckBean();
                    recheckBean.setRecheckFunction(txClient.recheckFunction());
                    recheckBean.setTxFlowId(LOCAL_TX_CONTEXT.get().getTxFlowId());
                    recheckBean.setDelayTime(txClient.delayTime());
                    recheckBean.setFirstRecheckSecond(txClient.firstRecheckSecond());
                }
                LOCAL_TX_CONTEXT.get().getRecheckMap().put(methodName, recheckBean);
            }
            context.setStart(true);
        }
    }

    private void closeTx() {
        LOCAL_TX_CONTEXT.get().setSuccess(true);
    }

    private void completeTransactionAfterThrowing(Throwable ex) {
        TxContext txContext = LOCAL_TX_CONTEXT.get();
        if (logger.isTraceEnabled()) {
            logger.trace("Completing transaction for [" + txContext.getMethod().toString() + "] after exception: " + ex);
        }
        //发送回滚消息
        txContext.setSuccess(false);
    }

    /**
     * 发送mq消息
     */
    private void sendMessage() {
        TxContext txContext = LOCAL_TX_CONTEXT.get();
        txContext.setEndTime(System.currentTimeMillis());
        txContext.getMessage().setSuccess(txContext.isSuccess());
        //发送mq
        List<String> invokeMethods = txContext.getInvokeMethodNameList();
        //调用的方法中没有tx系列的server. 结束
        if (CollectionUtils.isEmpty(invokeMethods)) return;
        if (amqpTemplate == null) {
            this.amqpTemplate = applicationContext.getBean(TxConst.TX_RABBIT_TEMPLATE_ID, AmqpTemplate.class);
        }
        String message = JSON.toJSONString(txContext.getMessage());
        for (String str : invokeMethods) {
            String mqinfo = SERVER_MQ_MAP.get(str);
            if (StringUtils.isEmpty(mqinfo)) continue;
            String[] args = mqinfo.split("@");
            amqpTemplate.convertAndSend(args[0], args[1], message);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Completing TxClient end for [" + txContext.getMethod().toString() + "] time:" + (txContext.getEndTime() - txContext.getStartTime()));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 找到server接口对应的Recheckbean
     *
     * @param invocation
     * @return
     */
    private int getRecheckBeanIndex(MethodInvocation invocation) {
        Object[] args = invocation.getArguments();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof RecheckBean) return i;
        }
        //单独的参数 并且为null的时候 根据classs
        Class[] classList = invocation.getMethod().getParameterTypes();
        for (int i = 0; i < classList.length; i++) {
            if (findBean(classList[i])) return i;
        }
        return -1;
    }

    private boolean findBean(Class cla_) {
        if (cla_ == RecheckBean.class) return true;
        if (cla_ == Object.class) return false;
        return findBean(cla_.getSuperclass());
    }

    private boolean serverInJvm(Class clas_) {
        return clas_.getName().equalsIgnoreCase(org.springframework.aop.framework.ReflectiveMethodInvocation.class.getName());
    }

    /**
     * 本地方法调用是否需要reckec
     * 根据参数中的flowid判断 是否由rpc调用触发本地调用
     *
     * @param invocation
     * @return
     */
    private RecheckBean localInvokeByRpc(MethodInvocation invocation) {
        Object[] args = invocation.getArguments();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && args[i] instanceof RecheckBean) {
                RecheckBean bean = (RecheckBean) args[i];
                if (!StringUtils.isEmpty(bean.getRecheckFunction()) && !StringUtils.isEmpty(bean.getTxFlowId())){
                    return bean;
                }
            }
        }
        return null;
    }


    private RecheckBean findRecheckBean(MethodInvocation invocation) {
        Object[] args = invocation.getArguments();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof RecheckBean) {
                RecheckBean bean = new RecheckBean();
                BeanUtils.copyProperties(args[i], bean);
                return bean;
            }
        }
        return null;
    }

    /**
     * 支持接口反查 需要TXserver isSupporRecheck = true
     * TxClient recheckFunction 不为空
     *
     * @param invocation
     * @return
     */
    private boolean methodSupportRecheck(MethodInvocation invocation) {
        Boolean b = LOCAL_SERVER_METHOD_SUPPORT_RECHECK.get(invocation.getMethod().toString());
        if (b == null) {
            TxServer serverAnnotation = (TxServer) ProxyMethodTXCache.get(invocation.getMethod().toString());
            b = serverAnnotation != null
                    && serverAnnotation.isSupporRecheck()
                    && !StringUtils.isEmpty(LOCAL_TX_CONTEXT.get().getTxClient().recheckFunction())
                    ? true : false;
            LOCAL_SERVER_METHOD_SUPPORT_RECHECK.put(invocation.getMethod().toString(), b);
        }
        return b;
    }

}