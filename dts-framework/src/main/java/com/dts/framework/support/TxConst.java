package com.dts.framework.support;

/**
 * @author Jook
 * @create 2019-03-28 17:23
 **/
public class TxConst {
    //配置走properties数据
    /**       单次mq事务确认配置          **/
    //配置 factory
    public static  String CONNECTION_FACTORY_ID = null;//"connectionFactory";
    //配置 事务的amqpId
    public static  String TX_RABBIT_TEMPLATE_ID = null;//"amqpTemplate";
    //根据自己接口的exchange 和 routeKey配置对应消费bean 继承MessageConfirm


}
