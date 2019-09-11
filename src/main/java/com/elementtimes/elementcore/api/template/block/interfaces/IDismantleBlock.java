package com.elementtimes.elementcore.api.template.block.interfaces;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 可被扳手拆卸
 * @author KSGFK create in 2019/5/1
 */
public interface IDismantleBlock {
    /**
     * 拆下
     * @param world 方块所在世界
     * @param pos 方块所在位置
     * @return 掉落的方块
     */
    default ItemStack dismantleBlock(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                NBTTagCompound tag = tile.serializeNBT();
                tag.removeTag("x");
                tag.removeTag("y");
                tag.removeTag("z");
                IBlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock());
                stack.setTagCompound(tag);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    default void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            TileEntity e = worldIn.getTileEntity(pos);
            if (e != null && stack.getTagCompound() != null) {
                NBTTagCompound tagCompound = stack.getTagCompound().copy();
                // fix: x, y, z
                tagCompound.setInteger("x", pos.getX());
                tagCompound.setInteger("y", pos.getY());
                tagCompound.setInteger("z", pos.getZ());
                e.readFromNBT(tagCompound);
            }
        }
    }
}
