package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.interfaces.INbtReadable;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 与能量有关的接口
 * @author luqin2007
 */
public interface ITileEnergyHandler extends ICapabilityProvider, INbtReadable {

    String NBT_ENERGY = "_energy_";

    /**
     * 获取能量存储对象
     * @return RfEnergy 对象
     */
    EnergyHandler getEnergyHandler();

    /**
     * 获取某一方向的能量存储类型
     * @param facing 方向
     * @return 类型
     */
    SideHandlerType getEnergyType(Direction facing);

    /**
     * 获取某种能量代理
     * @param receive 最大输入值
     * @param extract 最大输出值
     * @return 代理
     */
    default EnergyHandler.EnergyProxy getEnergyProxy(int receive, int extract) {
        return getEnergyHandler().new EnergyProxy(receive, extract);
    }

    /**
     * 获取某种类型的能量代理
     * @param type 类型
     * @return 代理
     */
    default EnergyHandler.EnergyProxy getEnergyProxy(SideHandlerType type) {
        switch (type) {
            case INPUT:
                return getEnergyHandler().new EnergyProxy(true, false);
            case OUTPUT:
                return getEnergyHandler().new EnergyProxy(false, true);
            case ALL:
            case IN_OUT:
                return getEnergyHandler().new EnergyProxy(true, true);
            case READONLY:
                return getEnergyHandler().new EnergyProxy(false, false);
            default:
                return null;
        }
    }

    /**
     * 获取某一方向的能量代理
     * @param facing 方向
     * @return 代理
     */
    default EnergyHandler.EnergyProxy getEnergyProxy(Direction facing) {
        return getEnergyProxy(getEnergyType(facing));
    }

    /**
     * 创建一个只能释放，不能接收的能量代理
     * @param facing 面
     * @return 能量代理
     */
    default EnergyHandler.EnergyProxy getEnergySender(Direction facing) {
        SideHandlerType type = getEnergyType(facing);
        if (type == SideHandlerType.IN_OUT || type == SideHandlerType.OUTPUT) {
            return getEnergyProxy(0, getEnergyTick());
        }
        return getEnergyProxy(SideHandlerType.READONLY);
    }

    /**
     * 设置每 tick 能量变化量
     * 用电器时则为耗电量，发电时则为发电量
     */
    default int getEnergyTick() {
        return 40;
    }

    /**
     * 设置最大转移电量，如管道输入输出电量
     */
    default void setEnergyTransfer(int transfer) {
        getEnergyHandler().setTransfer(transfer);
    }

    /**
     * 向外传电
     * @param count 发送的电量
     * @param facing 发送面
     * @param te 发送面对应 TileEntity
     * @param proxy 使用的代理，将从其中提取能量
     */
    default void sendEnergy(int count, @Nullable Direction facing, @Nullable TileEntity te, @Nonnull EnergyHandler.EnergyProxy proxy) {
        if (te != null && proxy.canExtract() && proxy.getEnergyStored() > 0) {
            te.getCapability(CapabilityEnergy.ENERGY, facing).ifPresent(storage -> {
                if (storage.canReceive()) {
                    int extract = proxy.extractEnergy(count, true);
                    int receive = storage.receiveEnergy(extract, true);
                    if (receive > 0) {
                        int r = storage.receiveEnergy(receive, false);
                        proxy.extractEnergy(r, false);
                    }
                }
            });
        }
    }

    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return LazyOptional.of(() -> (T) getEnergyProxy(getEnergyType(facing)));
    }

    @Override
    default void read(@Nonnull CompoundNBT compound) {
        if (compound.contains(NBT_ENERGY)) {
            getEnergyHandler().deserializeNBT(compound.getCompound(NBT_ENERGY));
        }
    }

    @Nonnull
    @Override
    default CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound.put(NBT_ENERGY, getEnergyHandler().serializeNBT());
        return compound;
    }
}
