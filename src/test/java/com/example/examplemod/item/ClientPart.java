package com.example.examplemod.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientPart {

    private static Random sRandom = new Random(System.currentTimeMillis());

    private static int last = sRandom.nextInt(0x00FFFFFF) + 0xFF000000;

    public static int debuggerColor(ItemStack item, int p2) {
        if (Minecraft.getInstance().world.getGameTime() % 20 == 0) {
            last = sRandom.nextInt(0x00FFFFFF) + 0xFF000000;
        }
        return last;
    }

    @OnlyIn(Dist.CLIENT)
    public static class DebuggerColor implements IItemColor {

        private int last = sRandom.nextInt(0x00FFFFFF) + 0xFF000000;

        @Override
        public int getColor(ItemStack p_getColor_1_, int p_getColor_2_) {
            if (Minecraft.getInstance().world.getGameTime() % 20 == 0) {
                last = sRandom.nextInt(0x00FFFFFF) + 0xFF000000;
            }
            return last;
        }
    }
}
