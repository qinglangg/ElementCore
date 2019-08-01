package com.elementtimes.elementcore.annotation.other;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public interface IMessageHandler<T> {

    void toBuffer(T msg, PacketBuffer buffer);

    T fromBuffer(PacketBuffer buffer);

    default void handler(T msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
    }
}
