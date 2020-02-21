package com.elementtimes.elementcore.api.utils;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 方块工具
 * @author luqin2007
 */
public class BlockUtils {

    private static BlockUtils u = null;
    public static BlockUtils getInstance() {
        if (u == null) {
            u = new BlockUtils();
        }
        return u;
    }

    public <T extends Comparable<T>> IBlockState checkAndSetState(IBlockState state, IProperty<T> property, T value) {
        if (!Objects.equals(value, state.getValue(property))) {
            return state.withProperty(property, value);
        }
        return state;
    }

    public void setBlockState(World world, BlockPos pos, IBlockState newState, @Nullable TileEntity tileEntity) {
        IBlockState oldState = world.getBlockState(pos);
        if (oldState != newState) {
            world.setBlockState(pos, newState, 3);
            if (tileEntity != null) {
                tileEntity.validate();
                world.setTileEntity(pos, tileEntity);
            }
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    public void setBlockState(World world, BlockPos pos, IBlockState newState) {
        setBlockState(world, pos, newState, world.getTileEntity(pos));
    }

    public EnumFacing getPosFacing(BlockPos before, BlockPos pos) {
        // face
        EnumFacing facing = null;
        int dx = pos.getX() - before.getX();
        if (dx > 0) {
            facing = EnumFacing.EAST;
        } else if (dx < 0) {
            facing = EnumFacing.WEST;
        }
        int dy = pos.getY() - before.getY();
        if (dy > 0 && facing == null) {
            facing = EnumFacing.UP;
        } else if (dy < 0 && facing == null) {
            facing = EnumFacing.DOWN;
        } else if (dy != 0) {
            return null;
        }
        int dz = pos.getZ() - before.getZ();
        if (dz > 0 && facing == null) {
            facing = EnumFacing.SOUTH;
        } else if (dz < 0 && facing == null) {
            facing = EnumFacing.NORTH;
        } else if (dz != 0) {
            return null;
        }
        return facing;
    }
}
