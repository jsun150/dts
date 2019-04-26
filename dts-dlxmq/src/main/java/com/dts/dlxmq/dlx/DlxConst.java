package com.dts.dlxmq.dlx;

/**
 * @author jsun
 * @create 2019-04-02 16:44
 **/
public class DlxConst {


    /**       单接口recheck服务配置   && 延迟消息配置   **/
    //配置 exchange
    public static  String X_DEAD_LETTER_EXCHANGE;
    //配置 dlx namespace
    public static  String NAME_SPACE;
    //配置 消费用的beanId
    public static  String DLX_MESSAGE_CONSUMER_BEAN_ID;
    //配置 templateid
    public static  String DLX_RABBIT_TEMPLATE_ID;
    public static  String DLX_CONNECTION_FACTORY_ID;

    public static final String X_DEAD_Q_NAME = NAME_SPACE+"x.dead.q.name.#";
    public static final String X_DEAD_Q_ID = "xDeadQId";

    public static final String CONSUMER_Q_NAME = NAME_SPACE+"consumer.q.name";
    public static final String CONSUMER_Q_ID = "consumerQId";

}
