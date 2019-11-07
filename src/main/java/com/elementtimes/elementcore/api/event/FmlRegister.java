package com.elementtimes.elementcore.api.event;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.other.CapabilityObject;
import com.elementtimes.elementcore.other.NetworkObject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 对于 FML 生命周期事件的注册
 *
 * @author luqin2007
 */
public class FmlRegister {

    private ECModElements elements;

    public FmlRegister(ECModElements elements) {
        this.elements = elements;
    }

    @SubscribeEvent
    public void onEvent(Event event) {
        Class eventClass = event.getClass();
        Map<Class<? extends Event>, List<Method>> methods = elements.methods.methods();
        for (Class<? extends Event> eventClassKey : methods.keySet()) {
            if (eventClassKey.isAssignableFrom(eventClass)) {
                for (Method method : methods.get(eventClassKey)) {
                    try {
                        if (method.getParameterCount() == 0) {
                            method.invoke(null);
                        } else {
                            method.invoke(null, event);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        loadCapabilities();
        registerNetwork();
    }

    private void loadCapabilities() {
        for (CapabilityObject capability : elements.capabilities.capabilities()) {
            CapabilityManager.INSTANCE.register(capability.typeInterfaceClass, capability.storage, capability.defSupplier);
        }
    }

    private void registerNetwork() {
        for (NetworkObject network : elements.networks.networks()) {
            int id = elements.nextNetworkId++;
            elements.channel.registerMessage(id, network.type, network.encoder, network.decoder, network.consumer);
        }
    }
}
