package com.dts.framework.jdk;

import com.dts.framework.support.TxMessage;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author jsun
 * @create 2019-05-05 13:50
 **/
public class UnsafeUntil {

    public static Unsafe getUnsafeInstance() throws Exception {
        // 通过反射获取rt.jar下的Unsafe类
        Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeInstance.setAccessible(true);
        // return (Unsafe) theUnsafeInstance.get(null);是等价的
        return (Unsafe) theUnsafeInstance.get(Unsafe.class);
    }

    public static void main(String[] args) throws Exception {

        Unsafe unsafe = getUnsafeInstance();
        TxMessage txMessage = new TxMessage();
        txMessage.setTxFlow("12312");

        long aoffset = unsafe.objectFieldOffset(TxMessage.class.getDeclaredField("txFlow"));
        String  va = unsafe.getObject(txMessage, aoffset).toString();
        System.out.println(va);

    }
}
