package com.example.examplemod.key;

import com.elementtimes.elementcore.api.annotation.ModKey;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import com.example.examplemod.network.ChannelMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class Key {

    @ModKey(@Method2(value = "com.example.examplemod.key.Key", name = "onKeyTest"))
    public static KeyBinding testKey = new KeyBinding("TestHello", GLFW.GLFW_KEY_T, "ElementCore");

    public static void onKeyTest(InputEvent.KeyInputEvent event, KeyBinding key) {
        Minecraft.getInstance().player.sendChatMessage("TestKey");
        ChannelMessage.test();
    }
}
