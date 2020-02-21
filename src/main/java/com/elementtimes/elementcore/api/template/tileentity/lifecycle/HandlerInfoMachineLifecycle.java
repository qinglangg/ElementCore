package com.elementtimes.elementcore.api.template.tileentity.lifecycle;

import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileEnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileFluidHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对 Fluid，Item，Energy 三种信息的封装，在 onTickFinish 时对其进行处理
 * 用于向客户端传递信息
 * @author luqin2007
 */
public class HandlerInfoMachineLifecycle implements IMachineLifecycle {

    private ICapabilityProvider o;
    private Function<Object, List<EntityPlayerMP>> player;
    private BiConsumer<EntityPlayer, EnergyInfo> energy;
    private BiConsumer<EntityPlayer, FluidInfo> fluid;

    private HandlerInfoMachineLifecycle(ICapabilityProvider o, Function<Object, List<EntityPlayerMP>> player,
                                        BiConsumer<EntityPlayer, EnergyInfo> energy,
                                        BiConsumer<EntityPlayer, FluidInfo> fluid) {
        this.o = o;
        this.player = player;
        this.energy = energy;
        this.fluid = fluid;
    }

    @Override
    public void onTickFinish() {
        FluidInfo fluidInfo = buildFluidInfo();
        EnergyInfo energyInfo = buildEnergyInfo();
        for (EntityPlayerMP player : player.apply(o)) {
            if (fluidInfo != null && fluid != null) {
                fluid.accept(player, fluidInfo);
            }
            if (energyInfo != null && energy != null) {
                energy.accept(player, energyInfo);
            }
        }
    }

    private EnergyInfo buildEnergyInfo() {
        if (energy != null) {
            IEnergyStorage e;
            e = o instanceof ITileEnergyHandler
                    ? ((ITileEnergyHandler) o).getEnergyHandler()
                    : o.getCapability(CapabilityEnergy.ENERGY, null);
            if (e != null) {
                return new EnergyInfo(e.getMaxEnergyStored(), e.getEnergyStored());
            }
        }
        return null;
    }

    private FluidInfo buildFluidInfo() {
        if (fluid != null) {
            FluidInfo info = new FluidInfo();
            if (o instanceof ITileFluidHandler) {
                ITileFluidHandler fh = (ITileFluidHandler) o;
                for (SideHandlerType type : SideHandlerType.values()) {
                    info.put(type, fh.getTanks(type));
                }
            } else {
                IFluidHandler tanks = o.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                info.put(SideHandlerType.NONE, tanks);
            }
            return info;
        }
        return null;
    }

    public static class Builder {
        private ICapabilityProvider o;
        private BiConsumer<EntityPlayer, EnergyInfo> energy = null;
        private BiConsumer<EntityPlayer, FluidInfo> fluid = null;
        private Function<Object, List<EntityPlayerMP>> player;

        public Builder(ICapabilityProvider o) {
            this.o = o;
        }

        public Builder withPlayers(Function<Object, List<EntityPlayerMP>> playerGetter) {
            player = playerGetter;
            return this;
        }

        public Builder withPlayers(Supplier<List<EntityPlayerMP>> playerGetter) {
            player = (o) -> playerGetter.get();
            return this;
        }

        public Builder withEnergyInfo(BiConsumer<EntityPlayer, EnergyInfo> energy) {
            this.energy = energy;
            return this;
        }

        public Builder withFluidInfo(BiConsumer<EntityPlayer, FluidInfo> fluid) {
            this.fluid = fluid;
            return this;
        }

        public HandlerInfoMachineLifecycle build() {
            return new HandlerInfoMachineLifecycle(o, player, energy, fluid);
        }
    }

    public static class EnergyInfo {
        public final int capacity;
        public final int stored;

        public EnergyInfo(int capacity, int stored) {
            this.capacity = capacity;
            this.stored = stored;
        }

        @Override
        public int hashCode() {
            return Objects.hash(capacity, stored);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    public static class FluidInfo {
        public final Map<SideHandlerType, IFluidHandler> fluids;

        public FluidInfo() {
            this.fluids = new HashMap<>(6);
        }

        public void put(SideHandlerType type, IFluidHandler handler) {
            this.fluids.put(type, handler);
        }
    }
}
