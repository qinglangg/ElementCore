package com.elementtimes.elementcore.other;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkObject<MSG> {
    public Class<MSG> type;
    public BiConsumer<MSG, PacketBuffer> encoder;
    public Function<PacketBuffer, MSG> decoder;
    public BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer;

    public NetworkObject(Class type, Method encoderMethod, Method decoderMethod, Method consumerMethod) {
        this.type = type;
        if (!Modifier.isPublic(encoderMethod.getModifiers())) {
            encoderMethod.setAccessible(true);
        }
        encoder = (msg, buffer) -> {
            try {
                encoderMethod.invoke(null, msg, buffer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };
        if (!Modifier.isPublic(decoderMethod.getModifiers())) {
            encoderMethod.setAccessible(true);
        }
        decoder = buffer -> {
            try {
                return (MSG) decoderMethod.invoke(null, buffer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        };
        if (!Modifier.isPublic(consumerMethod.getModifiers())) {
            encoderMethod.setAccessible(true);
        }
        consumer = (msg, contextSupplier) -> {
            try {
                consumerMethod.invoke(null, msg, contextSupplier);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };
    }
}
