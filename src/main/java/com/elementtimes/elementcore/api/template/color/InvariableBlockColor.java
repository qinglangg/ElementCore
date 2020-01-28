package com.elementtimes.elementcore.api.template.color;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * 总是返回一个颜色的 {@link IBlockColor}
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class InvariableBlockColor implements IBlockColor {

    private int color;

    public InvariableBlockColor(int color) {
        this.color = color;
    }

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        return color;
    }
}
