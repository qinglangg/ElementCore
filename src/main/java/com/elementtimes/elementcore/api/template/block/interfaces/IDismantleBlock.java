package com.elementtimes.elementcore.api.template.block.interfaces;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
                world.setBlockToAir(pos);
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
                IBlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock());
                NBTTagCompound compound = stack.getTagCompound();
                if (compound == null) {
                    compound = new NBTTagCompound();
                    stack.setTagCompound(compound);
                }
                compound.setTag("BlockEntityTag", tile.serializeNBT());
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
     * @see Block#onBlockPlacedBy(World, BlockPos, IBlockState, EntityLivingBase, ItemStack)
     * @deprecated 不需要了
     */
    @Deprecated
    default void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
                                 EntityLivingBase placer, ItemStack stack) {
//        if (!worldIn.isRemote) {
//            TileEntity e = worldIn.getTileEntity(pos);
//            if (e != null && stack.getTagCompound() != null) {
//                NBTTagCompound tagCompound = stack.getTagCompound().copy();
//                // fix: x, y, z
//                tagCompound.setInteger("x", pos.getX());
//                tagCompound.setInteger("y", pos.getY());
//                tagCompound.setInteger("z", pos.getZ());
//                e.readFromNBT(tagCompound);
//            }
//        }
    }
}
