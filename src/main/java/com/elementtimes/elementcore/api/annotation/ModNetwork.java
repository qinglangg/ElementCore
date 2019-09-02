package com.elementtimes.elementcore.api.annotation;

import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端与服务端通信 用于 IMessage 接口实现类
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModNetwork {

    /**
     * @return 数据包的 Handler 类, 实现 IMessageHandler 接口
     */
    String handlerClass();

    /**
     * @return 数据包接收端
     */
    Side[] side();
}
