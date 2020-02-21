package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Getter;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端与服务端通信 用于 IMessage 接口实现类
 * 注册到 SimpleChannel
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModSimpleNetwork {

    /**
     * @return 数据包的 Handler 实例, 实现 IMessageHandler 接口
     */
    Getter value();

    /**
     * @return 数据包接收端
     */
    Side[] side() default { Side.SERVER, Side.CLIENT };
}
