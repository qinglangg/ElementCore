package com.elementtimes.elementcore.api.template.color;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * 使用 {@link IBlockColor} 对物品进行染色
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ItemBlockColor implements IItemColor {

    private IBlockColor colorProvider;
    private Predicate<ItemStack> canApply;

    public ItemBlockColor(IBlockColor colorProvider, Predicate<ItemStack> canApply) {
        this.colorProvider = colorProvider;
        this.canApply = canApply;
    }

    @Override
    public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex) {
        if (canApply.test(stack)) {
            Block block = Block.getBlockFromItem(stack.getItem());
            return colorProvider.colorMultiplier(block.getDefaultState(), null, BlockPos.ORIGIN, tintIndex);
        }
        return 0;
    }
}
