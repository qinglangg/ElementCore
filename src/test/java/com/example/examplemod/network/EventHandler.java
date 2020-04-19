package com.example.examplemod.network;

import com.elementtimes.elementcore.api.annotation.ModEventNetwork;
import com.example.examplemod.ExampleMod;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent;

@ModEventNetwork
public class EventHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClient(NetworkEvent.ClientCustomPayloadEvent event) {
        String string = event.getPayload().readString() + " -> [e]client";
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        ExampleMod.CONTAINER.elements.postToServer(buffer.writeString(string));
    }

    @SubscribeEvent
    public static void onServer(NetworkEvent.ServerCustomPayloadEvent event) {
        String string = event.getPayload().readString();
        event.getSource().get().getSender().sendMessage(new StringTextComponent(string));
    }
}
