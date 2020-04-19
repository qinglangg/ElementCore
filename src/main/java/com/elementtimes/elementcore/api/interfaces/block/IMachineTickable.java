package com.elementtimes.elementcore.api.interfaces.block;

import com.elementtimes.elementcore.api.utils.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 与机器每 tick 执行的工作有关的接口
 * @author luqin
 */
public interface IMachineTickable extends IMachineLifecycle.IMachineLifecycleManager {

    String TICKABLE = "ECT";
    String TICKABLE_IS_PAUSE = "ECTIP";
    String TICKABLE_PROCESSED = "ECTP";
    String TICKABLE_UNPROCESSED = "ECTU";

    @Override
    default void onPause() {
        IMachineLifecycleManager.super.onPause();
        setPause(true);
        setWorking(false);
    }

    @Override
    default void onStart() {
        IMachineLifecycleManager.super.onStart();
        setPause(false);
        setWorking(true);
    }

    @Override
    default void onResume() {
        IMachineLifecycleManager.super.onResume();
        setPause(false);
        setWorking(true);
    }

    @Override
    default boolean onFinish() {
        boolean ret = IMachineLifecycleManager.super.onFinish();
        setWorking(false);
        setPause(false);
        setEnergyUnprocessed(0);
        setEnergyProcessed(0);
        return ret;
    }

    /**
     * 终止当前任务
     */
    void interrupt();

    /**
     * @return 是否正在工作
     */
    boolean isWorking();

    /**
     * 设置工作状态
     */
    void setWorking(boolean isWorking);

    /**
     * @return 是否暂停
     */
    boolean isPause();

    /**
     * 设置暂停标志
     */
    void setPause(boolean isPause);

    /**
     * 获取已消耗能量
     */
    int getEnergyProcessed();

    /**
     * 设置已消耗能量
     */
    void setEnergyProcessed(int energy);

    /**
     * 获取合成仍需能量/发电机仍存能量
     */
    int getEnergyUnprocessed();

    /**
     * 设置。/。。
     */
    void setEnergyUnprocessed(int energy);

    default void processEnergy(int delta) {
        int unprocessed = getEnergyUnprocessed();
        if (unprocessed > 0) {
            int newUnprocessed = Math.max(0, unprocessed - delta);
            int rDelta = unprocessed - newUnprocessed;
            setEnergyUnprocessed(newUnprocessed);
            setEnergyProcessed(getEnergyProcessed() + rDelta);
        } else if (unprocessed < 0) {
            int newUnprocessed = Math.min(0, unprocessed + delta);
            int rDelta = unprocessed - newUnprocessed;
            setEnergyUnprocessed(newUnprocessed);
            setEnergyProcessed(getEnergyProcessed() + rDelta);
        }
    }

    /**
     * 用于运行完成更新 IBlockState
     *
     * @param old 旧的 IBlockState
     * @return 新的 IBlockState
     */
    default BlockState updateState(BlockState old) {
        if (old.has(BlockStateProperties.ENABLED)) {
            boolean working = isWorking();
            if (old.get(BlockStateProperties.ENABLED) != working) {
                return old.with(BlockStateProperties.ENABLED, working);
            }
        }
        return old;
    }

    /**
     * onUpdate 在客户端执行的方法
     */
    default void updateClient() {}

    default void update(TileEntity tileEntity) {
        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();
        if (!world.isRemote) {
            // 生命周期
            onTickStart();
            if (!isWorking()) {
                if (!isPause()) {
                    if (onCheckStart()) {
                        onStart();
                        if (!onLoop()) {
                            onPause();
                        }
                    }
                } else {
                    if (onCheckResume()) {
                        onResume();
                    }
                }
            } else {
                if (onCheckFinish()) {
                    if (!onFinish()) {
                        onPause();
                    } else if (onCheckStart()) {
                        onStart();
                    }

                } else if (!onLoop()) {
                    onPause();
                }
            }
            onTickFinish();

            BlockState oldState = world.getBlockState(pos);
            BlockState newState = updateState(oldState);
            BlockUtils.setBlockState(world, pos, oldState, newState);
            tileEntity.markDirty();
        } else {
            updateClient();
        }
    }

    default void read(CompoundNBT nbt) {
        if (nbt.contains(TICKABLE)) {
            CompoundNBT tickable = nbt.getCompound(TICKABLE);

            if (tickable.contains(TICKABLE_IS_PAUSE)) {
                setPause(tickable.getBoolean(TICKABLE_IS_PAUSE));
            }

            setWorking(false);

            if (tickable.contains(TICKABLE_PROCESSED)) {
                setEnergyProcessed(tickable.getInt(TICKABLE_PROCESSED));
            }

            if (tickable.contains(TICKABLE_UNPROCESSED)) {
                setEnergyUnprocessed(tickable.getInt(TICKABLE_UNPROCESSED));
            }
        }
    }

    default CompoundNBT write(CompoundNBT nbt) {
        CompoundNBT tickable = new CompoundNBT();
        tickable.putBoolean(TICKABLE_IS_PAUSE, isWorking() || isPause());
        tickable.putInt(TICKABLE_PROCESSED, getEnergyProcessed());
        tickable.putInt(TICKABLE_UNPROCESSED, getEnergyUnprocessed());
        nbt.put(TICKABLE, tickable);
        return nbt;
    }
}