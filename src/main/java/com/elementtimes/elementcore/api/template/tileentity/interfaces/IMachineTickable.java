package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.template.block.Properties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * 与机器每 tick 执行的工作有关的接口
 * @author luqin
 */
public interface IMachineTickable extends INBTSerializable<NBTTagCompound>, IMachineLifecycle.IMachineLifecycleManager {

    String TICKABLE = "_tickable_";
    String TICKABLE_IS_PAUSE = "_tickable_pause_";
    String TICKABLE_PROCESSED = "_tickable_processed_";
    String TICKABLE_UNPROCESSED = "_tickable_unprocessed_";

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
    default IBlockState updateState(IBlockState old) {
        if (old.getPropertyKeys().contains(Properties.IS_RUNNING)) {
            if (old.getValue(Properties.IS_RUNNING) != isWorking()) {
                return old.withProperty(Properties.IS_RUNNING, isWorking());
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

            IBlockState newState = updateState(world.getBlockState(pos));
            ECUtils.block.setBlockState(world, pos, newState, tileEntity);
            tileEntity.markDirty();
        } else {
            updateClient();
        }
    }

    @Override
    default NBTTagCompound serializeNBT() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(TICKABLE)) {
            NBTTagCompound tickable = nbt.getCompoundTag(TICKABLE);

            if (tickable.hasKey(TICKABLE_IS_PAUSE)) {
                setPause(tickable.getBoolean(TICKABLE_IS_PAUSE));
            }

            setWorking(false);

            if (tickable.hasKey(TICKABLE_PROCESSED)) {
                setEnergyProcessed(tickable.getInteger(TICKABLE_PROCESSED));
            }

            if (tickable.hasKey(TICKABLE_UNPROCESSED)) {
                setEnergyUnprocessed(tickable.getInteger(TICKABLE_UNPROCESSED));
            }
        }
    }

    default NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound tickable = new NBTTagCompound();
        tickable.setBoolean(TICKABLE_IS_PAUSE, isWorking() || isPause());
        tickable.setInteger(TICKABLE_PROCESSED, getEnergyProcessed());
        tickable.setInteger(TICKABLE_UNPROCESSED, getEnergyUnprocessed());
        nbt.setTag(TICKABLE, tickable);
        return nbt;
    }
}