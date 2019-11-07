package com.elementtimes.elementcore.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class GlassColor implements IBlockColor {
    @Override
    public int getColor(BlockState state, @Nullable IEnviromentBlockReader reader, @Nullable BlockPos pos, int i) {
        return TextFormatting.GOLD.getColor();
    }
}
