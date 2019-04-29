# dts
基于tcc的分布式事务框架

## 包功能
*dts-dlxmq  基于rabbitMq的延迟队列做的消息延迟功能包*
*dts-framework 用于做分布式事务控制包*

## 流程图
### 简单类型
![image](https://github.com/jsun150/dts/edit/master/simple.png)
### 复杂类型
![image](https://github.com/jsun150/dts/edit/master/complex.png)

-- 目前支持dubbo , feign 的注解配置

-- 20190426  支持手动事务提交
## 使用方式

接口提供方:
    
public interface Rpcserver { 
  
    @TxServer(mqInfo = "#{exchange}@#{routeKey}")
    void String getName(Request request)

    @TxServer(mqInfo = "#{exchange}@#{routeKey}")
    void String getName(Integer userId, RecheckBean var2)
  
}



@PS: 接口方参数必须要有 RecheckBean 可以单独作为一个参数也可以 Request extend RecheckBean. 这个参数不自动填入不需要调用方去填入值

服务调用方

public interface TestService {

    void test();
    void boot1()
}

public class TestServiceImpl implements TestService {

    @Autowired
    private Rpcserver rpcserver 
    
    // 自动提交事务
    @TxClient()
    @Override
    public void boot() {
        //流水号必须获取 可以使用jar包自定义流水也可以自己传入流水
        TxFlowUtil.getTxFlowId(null);
        rpcserver.getName(new Request());
    }
  
    //手动提交事务. 对应的工具类ManualCommitSupport
    @TxClient(commitType = CommintType.MANUAL)
    @Override
    public void boot1() {
        //流水号必须获取 可以使用jar包自定义流水也可以自己传入流水
        TxFlowUtil.getTxFlowId(null);
        rpcserver.getName(123, null);
        bean = ManualCommitSupport.getTxCommitMessage(); //这个bean可以在其他方法中做当前流水事务提交
    }
}


