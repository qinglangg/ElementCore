package com.elementtimes.elementcore.api.utils;

import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

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

    public <T extends Comparable<T>> BlockState checkAndSetState(BlockState state, Property<T> property, T value) {
        if (value != state.get(property)) {
            return state.with(property, value);
        }
        return state;
    }

    public void setBlockState(World world, BlockPos pos, BlockState newState, @Nullable TileEntity tileEntity) {
        BlockState oldState = world.getBlockState(pos);
        if (oldState != newState) {
            world.setBlockState(pos, newState, 3);
            if (tileEntity != null) {
                tileEntity.validate();
                world.setTileEntity(pos, tileEntity);
            }
            world.markForRerender(pos);
        }
    }

    public Direction getPosFacing(BlockPos before, BlockPos pos) {
        // face
        Direction facing = null;
        int dx = pos.getX() - before.getX();
        if (dx > 0) {
            facing = Direction.EAST;
        } else if (dx < 0) {
            facing = Direction.WEST;
        }
        int dy = pos.getY() - before.getY();
        if (dy > 0 && facing == null) {
            facing = Direction.UP;
        } else if (dy < 0 && facing == null) {
            facing = Direction.DOWN;
        } else if (dy != 0) {
            return null;
        }
        int dz = pos.getZ() - before.getZ();
        if (dz > 0 && facing == null) {
            facing = Direction.SOUTH;
        } else if (dz < 0 && facing == null) {
            facing = Direction.NORTH;
        } else if (dz != 0) {
            return null;
        }
        return facing;
    }
}
