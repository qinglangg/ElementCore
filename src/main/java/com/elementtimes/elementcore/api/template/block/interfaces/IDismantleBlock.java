package com.elementtimes.elementcore.api.template.block.interfaces;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
                world.removeBlock(pos, false);
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
                CompoundNBT tag = tile.serializeNBT();
                tag.remove("x");
                tag.remove("y");
                tag.remove("z");
                BlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock());
                stack.setTag(tag);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 放置方块时调用。通常在 Block 同名方法中调用
     * @param worldIn 所在世界
     * @param pos 所处位置
     * @param state state，没啥用
     * @param placer 放置玩家或实体
     * @param stack 物品栈
     */
    default void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            TileEntity e = worldIn.getTileEntity(pos);
            if (e != null && stack.hasTag()) {
                CompoundNBT tagCompound = stack.getTag().copy();
                // fix: x, y, z
                tagCompound.putInt("x", pos.getX());
                tagCompound.putInt("y", pos.getY());
                tagCompound.putInt("z", pos.getZ());
                e.read(tagCompound);
            }
        }
    }
}
