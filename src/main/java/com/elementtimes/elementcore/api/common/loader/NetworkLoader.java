package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModEventNetwork;
import com.elementtimes.elementcore.api.annotation.ModSimpleNetwork;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.util.List;

public class NetworkLoader {

    public static void load(ECModElements elements) {
        if (elements.simpleChannel != null) {
            ObjHelper.stream(elements, ModSimpleNetwork.class).forEach(data -> {
                ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                    RefHelper.get(elements, ObjHelper.getDefault(data), IMessageHandler.class).ifPresent(handler -> {
                        List<ModAnnotation.EnumHolder> sideHolders = (List<ModAnnotation.EnumHolder>) data.getAnnotationInfo().get("side");
                        boolean server = false, client = false;
                        if (sideHolders != null) {
                            for (ModAnnotation.EnumHolder holder : sideHolders) {
                                if ("CLIENT".equals(holder.getValue())) {
                                    client = true;
                                } else {
                                    server = true;
                                }
                            }
                        } else {
                            client = true;
                            server = true;
                        }
                        elements.netSimple.add(new SimpleNetwork(handler, aClass, server, client));
                    });
                });
            });
        }
        if (elements.eventChannel != null) {
            ObjHelper.stream(elements, ModEventNetwork.class).forEach(data -> {
                ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                    if (ObjHelper.getDefault(data, false)) {
                        elements.netEvent.add(ECUtils.reflect.create(aClass, aClass, elements));
                    } else {
                        elements.netEvent.add(aClass);
                    }
                });
            });
        }
    }

    public static class SimpleNetwork {

        public final IMessageHandler handler;
        public final Class message;
        public final boolean server, client;

        public SimpleNetwork(IMessageHandler handler, Class message, boolean server, boolean client) {
            this.handler = handler;
            this.message = message;
            this.server = server;
            this.client = client;
        }
    }
}
