package com.elementtimes.elementcore.api.interfaces.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * 可被扳手拆卸
 * @author KSGFK create in 2019/5/1
 */
@SuppressWarnings("unused")
public interface IDismantleBlock {
    /**
     * 拆下
     * @param world 方块所在世界
     * @param pos 方块所在位置
     * @return 是否成功拆除
     */
    default boolean dismantleBlock(World world, BlockPos pos) {
        if (!world.isRemote) {
            ItemStack item = getDismantleItem(world, pos);
            if (item != ItemStack.EMPTY && !item.isEmpty()) {
                Block.spawnAsEntity(world, pos, item);
                world.removeTileEntity(pos);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return true;
            }
        }
        return false;
    }

    /**
     * 拆下的物品
     * @param world 方块所在世界
     * @param pos 方块所在位置
     * @return 掉落的方块
     */
    @Nonnull
    default ItemStack getDismantleItem(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                BlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock());
                stack.getOrCreateTag().put("BlockEntityTag", tile.serializeNBT());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
