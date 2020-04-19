package com.elementtimes.elementcore.api.interfaces.block;

import com.elementtimes.elementcore.api.template.capability.ProxyEnergyHandler;
import com.elementtimes.elementcore.api.template.capability.SideHandlerType;
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
public interface ITileEnergyHandler extends ICapabilityProvider {

    String NBT_ENERGY = "ECE";

    /**
     * 获取能量存储对象
     * @return RfEnergy 对象
     */
    ProxyEnergyHandler getEnergyHandler();

    /**
     * 获取某一方向的能量存储类型
     * @param facing 方向
     * @return 类型
     */
    default SideHandlerType getEnergyType(Direction facing) {
        return SideHandlerType.INPUT;
    }

    /**
     * 获取某种能量代理
     * @param receive 最大输入值
     * @param extract 最大输出值
     * @return 代理
     */
    default ProxyEnergyHandler.Proxy getEnergyProxy(int receive, int extract) {
        return getEnergyHandler().new Proxy(receive, extract);
    }

    /**
     * 获取某种类型的能量代理
     * @param type 类型
     * @return 代理
     */
    default ProxyEnergyHandler.Proxy getEnergyProxy(SideHandlerType type) {
        switch (type) {
            case INPUT: return getEnergyHandler().new Proxy(true, false);
            case OUTPUT: return getEnergyHandler().new Proxy(false, true);
            case IN_OUT: return getEnergyHandler().new Proxy(true, true);
            default: return getEnergyHandler().new Proxy(false, false);
        }
    }

    /**
     * 获取某一方向的能量代理
     * @param facing 方向
     * @return 代理
     */
    default ProxyEnergyHandler.Proxy getEnergyProxy(Direction facing) {
        return getEnergyProxy(getEnergyType(facing));
    }

    /**
     * 创建一个只能释放，不能接收的能量代理
     * @param facing 面
     * @return 能量代理
     */
    default ProxyEnergyHandler.Proxy getEnergySender(Direction facing) {
        return getEnergyProxy(0, getEnergyTick());
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
     * @param te 发送面对应 TestTileEntity
     * @param proxy 使用的代理，将从其中提取能量
     */
    default void sendEnergy(int count, @Nullable Direction facing, @Nullable TileEntity te, @Nonnull ProxyEnergyHandler.Proxy proxy) {
        if (te != null) {
            te.getCapability(CapabilityEnergy.ENERGY, facing).ifPresent(storage -> {
                int extract = proxy.extractEnergy(count, true);
                int receive = storage.receiveEnergy(extract, true);
                if (receive > 0) {
                    int r = storage.receiveEnergy(receive, false);
                    proxy.extractEnergy(r, false);
                }
            });
        }
    }

    @Nonnull
    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            SideHandlerType type = getEnergyType(side);
            if (type != SideHandlerType.NONE) {
                return LazyOptional.of(() -> (T) getEnergyProxy(type));
            }
        }
        return LazyOptional.empty();
    }

    default void read(CompoundNBT nbt) {
        if (nbt.contains(NBT_ENERGY)) {
            getEnergyHandler().deserializeNBT(nbt.getCompound(NBT_ENERGY));
        }
    }

    default CompoundNBT write(CompoundNBT nbt) {
        nbt.put(NBT_ENERGY, getEnergyHandler().serializeNBT());
        return nbt;
    }
}
