package com.elementtimes.elementcore.annotation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端与服务端通信 用于 IMessage 接口实现类
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModNetwork {

    /**
     * 频道 ID
     * @return 频道 ID
     */
    int id();

    /**
     * 数据包的 Handler 类, 实现 IMessageHandler 接口
     * @return 数据包的 Handler 类
     */
    String handlerClass();
}
