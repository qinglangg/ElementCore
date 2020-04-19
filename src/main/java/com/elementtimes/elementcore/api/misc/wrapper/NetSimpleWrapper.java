package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class NetSimpleWrapper {

    public static int nextIndex = 0;

    public final Class message;
    public final BiConsumer<Object, PacketBuffer> encoder;
    public final Function<PacketBuffer, Object> decoder;
    public final BiConsumer<Object, Supplier<NetworkEvent.Context>> handler;

    public NetSimpleWrapper(Class<?> message, BiConsumer<Object, PacketBuffer> encoder, Function<PacketBuffer, Object> decoder, BiConsumer<Object, Supplier<NetworkEvent.Context>> handler) {
        this.message = message;
        this.encoder = encoder;
        this.decoder = decoder;
        this.handler = handler;
    }

    public void apply(ECModElements elements) {
        elements.warn("    {}: {}", nextIndex, message.getName());
        elements.simpleChannel.registerMessage(nextIndex++, message, encoder, decoder, handler);
    }

    public static void registerAll(ECModElements elements) {
        List<NetSimpleWrapper> nets = elements.netSimples;
        elements.warn("  Network for Simple Channel {}({})", elements.simpleChannelName, nets.size());
        nets.forEach(e -> e.apply(elements));
    }
}
