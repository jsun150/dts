package com.dts.framework.spring;

import com.alibaba.fastjson.JSON;
import com.dts.dlxmq.dlx.DlxMessageProducer;
import com.dts.framework.annotation.TxClient;
import com.dts.framework.annotation.TxServer;
import com.dts.framework.support.RecheckBean;
import com.dts.framework.support.TxConst;
import com.dts.framework.support.TxContext;
import com.dts.framework.support.TxMessage;
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
    //方法对应的回调路径
    private Map<String, RecheckBean> CHECK_F_MAP = new ConcurrentHashMap<>();
    //调用方对应的mq路径
    private Map<String, String> SERVER_MQ_MAP = new ConcurrentHashMap<>();
    // 本地方法记录
    private List<String> injvmMethodName = new ArrayList<>();
    private List<String> serverthodName = new ArrayList<>();
    // 记录server第几个参数为RecheckBean
    private Map<String, Integer> recheckBeanIndex = new HashMap<>();
    //server方法是否是本地实现
    private Map<Class, Boolean> inJvm = new HashMap<>();
    //server方法是否支持本地单接口反查
    private Map<String, Boolean> LOCAL_SERVER_METHOD_SUPPORT_RECHECK = new HashMap<>();
    private DlxMessageProducer dlxMessageProducer;


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        String name = method.getName();

        if (injvmMethodName.contains(name)) {
            return invokeWithinTransaction(invocation);
        } else if (serverthodName.contains(name)) {
            return invokeServerTranscation(invocation);
        }
        TxClient txClient = invocation.getMethod().getAnnotation(TxClient.class);
        if (txClient != null) {
            injvmMethodName.add(name);
            return invokeWithinTransaction(invocation);
        } else {
            serverthodName.add(name);
            return invokeServerTranscation(invocation);
        }
    }

    private Object invokeServerTranscation(MethodInvocation invocation) throws Throwable {
        Object revalue = null;
        try {
            //本地调用
            if (serverInJvm(invocation.getClass())) {
                revalue = invocation.proceed();
            } else {
                //远程调用
                Method method = invocation.getMethod();
                String mqInfo;
                if (StringUtils.isEmpty((mqInfo = SERVER_MQ_MAP.get(method.getName())))) {
                    TxServer ts = invocation.getMethod().getAnnotation(TxServer.class);
                    if (StringUtils.isEmpty(ts.mqInfo())) {
                        throw new Exception("error: " + method.getName() + " annotation can not find mqInfo");
                    }
                    SERVER_MQ_MAP.put(method.getName(), mqInfo);
                }


                // 填入回调参数
                Integer index = recheckBeanIndex.get(method.getName());
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
                    BeanUtils.copyProperties(CHECK_F_MAP.get(method.getName()), obj);
                }
                revalue = invocation.proceed();
                LOCAL_TX_CONTEXT.get().getCheckMqList().add(mqInfo);
            }
        } catch (Throwable ex) {
            throw ex;
        }
        return revalue;
    }

    /**
     * 本地方法调用 开启事务 关闭事务
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    private Object invokeWithinTransaction(MethodInvocation invocation) throws Throwable {
        //获取对应事务回调方法
        String methodName = invocation.getMethod().getName();
        RecheckBean recheckBean;
        if ((recheckBean = CHECK_F_MAP.get(methodName)) == null) {
            TxClient tx = invocation.getMethod().getAnnotation(TxClient.class);
            if (tx != null) {
                recheckBean = new RecheckBean();
                recheckBean.setRecheckFunction(tx.recheckFunction());
                recheckBean.setTxFlowId(LOCAL_TX_CONTEXT.get().getTxFlowId());
                recheckBean.setDelayTime(tx.delayTime());
                recheckBean.setFirstRecheckSecond(tx.firstRecheckSecond());
            }
            CHECK_F_MAP.put(methodName, recheckBean);
        }
        //开启事务
        checkAndopenTx(invocation.getMethod(), methodName);
        Object revalue = null;
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
            context.setMethod(method.getName());
            TxMessage message = new TxMessage();
            message.setTxFlow(flowId);
            context.setMessage(message);
            RecheckBean recheckBean;
            if ((recheckBean = CHECK_F_MAP.get(methodName)) == null) {
                TxClient tx = method.getAnnotation(TxClient.class);
                if (tx != null) {
                    recheckBean = new RecheckBean();
                    recheckBean.setRecheckFunction(tx.recheckFunction());
                    recheckBean.setTxFlowId(LOCAL_TX_CONTEXT.get().getTxFlowId());
                    recheckBean.setDelayTime(tx.delayTime());
                    recheckBean.setFirstRecheckSecond(tx.firstRecheckSecond());
                }
                CHECK_F_MAP.put(methodName, recheckBean);
            }
            context.setCheck(recheckBean.getRecheckFunction());
            context.setStart(true);
        }
    }

    private void closeTx() {
        LOCAL_TX_CONTEXT.get().setSuccess(true);
    }

    private void completeTransactionAfterThrowing(Throwable ex) {
        TxContext txContext = LOCAL_TX_CONTEXT.get();
        if (logger.isTraceEnabled()) {
            logger.trace("Completing transaction for [" + txContext.getMethod() + "] after exception: " + ex);
        }
        //发送回滚消息
        txContext.setEnd(true);
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
        List<String> list = txContext.getCheckMqList();
        if (amqpTemplate == null) {
            this.amqpTemplate = applicationContext.getBean(TxConst.TX_RABBIT_TEMPLATE_ID, AmqpTemplate.class);
        }
        if (!CollectionUtils.isEmpty(list)) {
            String message = JSON.toJSONString(txContext.getMessage());
            for (String str : list) {
                String[] args = str.split("@");
//                amqpTemplate.convertAndSend(args[0], args[1], message);
                logger.info("send message:" + str + " | " + message);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Completing TxClient end for [" + txContext.getMethod() + "] time:" + (txContext.getEndTime() - txContext.getStartTime()));
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
        try {
            Boolean exist = inJvm.get(clas_);
            if (exist == null) {
                exist = Class.forName(clas_.getName() + "Impl") != null ? true : false;
                inJvm.put(clas_, exist);
            }
            return exist;
        } catch (Exception e) {
            inJvm.put(clas_, false);
        }
        return false;
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

    private boolean methodSupportRecheck(MethodInvocation invocation) {
        Boolean b = LOCAL_SERVER_METHOD_SUPPORT_RECHECK.get(invocation.getMethod().getName());
        if (b == null) {
            TxServer serverAnnotation = invocation.getMethod().getAnnotation(TxServer.class);
            b = serverAnnotation != null && serverAnnotation.isSupporRecheck() ? true : false;
            LOCAL_SERVER_METHOD_SUPPORT_RECHECK.put(invocation.getMethod().getName(), b);
        }
        return b;
    }

    public DlxMessageProducer getDlxMessageProducer() {
        return dlxMessageProducer;
    }

    public void setDlxMessageProducer(DlxMessageProducer dlxMessageProducer) {
        this.dlxMessageProducer = dlxMessageProducer;
    }
}