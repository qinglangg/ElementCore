package com.elementtimes.elementcore.client;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 调试棒着色器
 * @author luqin2007
 */
@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class DebugStickColor implements IItemColor {
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (stack.getMetadata() == 0b0000) {
            // RED
            return 0xFFFF0000;
        } else if (stack.getMetadata() == 0b0001) {
            // BLUE
            return 0xFF0000FF;
        } else {
            // YELLOW
            return 0xFFFFFF00;
        }
    }
}
