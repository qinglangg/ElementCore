package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModNetwork;
import com.elementtimes.elementcore.annotation.other.IMessageHandler;
import com.elementtimes.elementcore.util.ReflectUtil;

import java.util.Optional;

/**
 * 处理网络通信
 * @author luqin2007
 */
public class ModNetworkLoader {

    public static void getElements(AnnotationInitializer initializer) {
        initializer.elements.get(ModNetwork.class).forEach(element -> {
            ModNetwork network = element.getAnnotation(ModNetwork.class);
            Class messageClass = (Class) element;
            Optional<Object> handler = ReflectUtil.create(network.handlerClass(), initializer);
            if (handler.isPresent()) {
                Object o = handler.get();
                if (o instanceof IMessageHandler) {
                    initializer.networks.add(new Object[] {network, o, messageClass});
                } else {
                    initializer.warn("Handler object {} can't cast to IMessageHandler", o);
                }
            }
        });
    }
}
