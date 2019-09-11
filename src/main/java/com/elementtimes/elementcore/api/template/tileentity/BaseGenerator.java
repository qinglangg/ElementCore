package com.elementtimes.elementcore.api.template.tileentity;

import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IMachineLifecycle;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对所有发电设备的抽象
 * @author luqin2007
 */
public abstract class BaseGenerator extends BaseTileEntity {

    public BaseGenerator(int energyCapacity) {
        super(energyCapacity, 1, 0);
        addLifeCycle(new IMachineLifecycle() {
            @Override
            public void onTickFinish() {
                for (EnumFacing value : EnumFacing.values()) {
                    EnergyHandler.EnergyProxy proxy = getEnergyProxy(value);
                    sendEnergy(proxy.getEnergyStored(), value.getOpposite(), world.getTileEntity(pos.offset(value)), proxy);
                }
            }
        });
    }

    @Override
    public SideHandlerType getEnergyType(EnumFacing facing) {
        return SideHandlerType.OUTPUT;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(getEnergyProxy(facing));
        }
        return super.getCapability(capability, facing);
    }

    @Nonnull
    @Override
    public Slot[] getSlots() {
        return new Slot[] {
                new SlotItemHandler(getItemHandler(SideHandlerType.INPUT), 0, 80, 30)
        };
    }
}

