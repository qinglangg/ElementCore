package com.elementtimes.elementcore.client;

import com.elementtimes.elementcore.common.item.DebugStick;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * 调试棒着色器
 * @author luqin2007
 */
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class DebugStickColor implements IItemColor {

    @Override
    @SuppressWarnings("ConstantConditions")
    public int getColor(@Nonnull ItemStack stack, int tintIndex) {
        String[] typeAndServer = DebugStick.getTypeAndServer(stack);
        switch (typeAndServer[0]) {
            case DebugStick.TYPE_DEBUG:
                switch (typeAndServer[1]) {
                    case DebugStick.SIDE_SERVER:
                        return TextFormatting.RED.getColor();
                    case DebugStick.SIDE_CLIENT:
                        return TextFormatting.BLUE.getColor();
                    default:
                        return TextFormatting.GRAY.getColor();
                }
            case DebugStick.TYPE_TOOL:
                switch (typeAndServer[1]) {
                    case DebugStick.SIDE_SERVER:
                        return TextFormatting.YELLOW.getColor();
                    case DebugStick.SIDE_CLIENT:
                    default:
                        return TextFormatting.GRAY.getColor();
                }
            default:
                return TextFormatting.GRAY.getColor();
        }
    }
}
