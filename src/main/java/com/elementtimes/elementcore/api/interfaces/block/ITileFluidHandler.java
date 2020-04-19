package com.elementtimes.elementcore.api.interfaces.block;

import com.elementtimes.elementcore.api.template.capability.ProxyTankHandler;
import com.elementtimes.elementcore.api.template.capability.SideHandlerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 流体存储有关的接口
 * @author luqin2007
 */
public interface ITileFluidHandler extends ICapabilityProvider {

    String NBT_FLUID = "ECFluid";

    ProxyTankHandler getTanks();

    default ProxyTankHandler.Proxy getTanks(SideHandlerType type) {
        switch (type) {
            case INPUT: return getTanks().new Proxy(true, false);
            case OUTPUT: return getTanks().new Proxy(false, true);
            case IN_OUT: return getTanks().new Proxy(true, true);
            default: return getTanks().empty;
        }
    }

    default SideHandlerType getTankType(@Nullable Direction facing) {
        if (facing == null) {
            return SideHandlerType.IN_OUT;
        }
        switch (facing) {
            case UP: return SideHandlerType.INPUT;
            case DOWN: return SideHandlerType.OUTPUT;
            default: return SideHandlerType.IN_OUT;
        }
    }

    @Nonnull
    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            SideHandlerType type = getTankType(side);
            if (type != SideHandlerType.NONE) {
                return LazyOptional.of(() -> (T) getTanks(type));
            }
        }
        return LazyOptional.empty();
    }

    default void read(CompoundNBT nbt) {
        if (nbt.contains(NBT_FLUID)) {
            getTanks().deserializeNBT(nbt.getCompound(NBT_FLUID));
        }
    }

    default CompoundNBT write(CompoundNBT nbt)  {
        nbt.put(NBT_FLUID, getTanks().serializeNBT());
        return nbt;
    }

    default boolean isFillValid(int slot, int amount, FluidStack fluidStack) { return false; }
}
