package com.elementtimes.elementcore.api.utils;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 方块工具
 * @author luqin2007
 */
public class BlockUtils {

    public static void setBlockState(World world, BlockPos pos, BlockState oldState, BlockState newState, int flag) {
        if (oldState != newState) {
            TileEntity te = world.getTileEntity(pos);
            world.setBlockState(pos, newState);
            if (te != null && te != world.getTileEntity(pos)) {
                te.validate();
                world.setTileEntity(pos, te);
            }
        }
    }

    public static void setBlockState(World world, BlockPos pos, BlockState newState, int flag) {
        setBlockState(world, pos, world.getBlockState(pos), newState, flag);
    }

    public static void setBlockState(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        setBlockState(world, pos, oldState, newState, 3);
    }

    public static void setBlockState(World world, BlockPos pos, BlockState newState) {
        setBlockState(world, pos, world.getBlockState(pos), newState, 3);
    }

    public static Direction getPosFacing(BlockPos before, BlockPos pos) {
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
