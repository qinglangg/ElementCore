package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对 物品/流体/能量 接口的一个综合
 * @author luqin2007
 */
public class ITileHandler {

    public interface All extends ITileEnergyHandler, ITileItemHandler, ITileFluidHandler {
        @Override
        default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return ITileEnergyHandler.super.hasCapability(capability, facing)
                    || ITileItemHandler.super.hasCapability(capability, facing)
                    || ITileFluidHandler.super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return ITileEnergyHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return ITileItemHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return ITileFluidHandler.super.getCapability(capability, facing);
            }
            return null;
        }

        @Override
        default NBTTagCompound serializeNBT() {
            return writeToNBT(new NBTTagCompound());
        }

        @Override
        default void deserializeNBT(NBTTagCompound nbtTagCompound) {
            ITileItemHandler.super.deserializeNBT(nbtTagCompound);
            ITileEnergyHandler.super.deserializeNBT(nbtTagCompound);
            ITileFluidHandler.super.deserializeNBT(nbtTagCompound);
        }

        @Override
        default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
            nbtTagCompound = ITileItemHandler.super.writeToNBT(nbtTagCompound);
            nbtTagCompound = ITileEnergyHandler.super.writeToNBT(nbtTagCompound);
            nbtTagCompound = ITileFluidHandler.super.writeToNBT(nbtTagCompound);
            return nbtTagCompound;
        }
    }

    public interface ItemAndEnergy extends ITileEnergyHandler, ITileFluidHandler {
        @Override
        default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return ITileEnergyHandler.super.hasCapability(capability, facing)
                    || ITileFluidHandler.super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return ITileEnergyHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return ITileFluidHandler.super.getCapability(capability, facing);
            }
            return null;
        }

        @Override
        default NBTTagCompound serializeNBT() {
            return writeToNBT(new NBTTagCompound());
        }

        @Override
        default void deserializeNBT(NBTTagCompound nbtTagCompound) {
            ITileEnergyHandler.super.deserializeNBT(nbtTagCompound);
            ITileFluidHandler.super.deserializeNBT(nbtTagCompound);
        }

        @Override
        default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
            ITileEnergyHandler.super.writeToNBT(nbtTagCompound);
            ITileFluidHandler.super.writeToNBT(nbtTagCompound);
            return nbtTagCompound;
        }
    }

    public interface FluidAndEnergy extends ITileEnergyHandler, ITileItemHandler {
        @Override
        default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return ITileEnergyHandler.super.hasCapability(capability, facing)
                    || ITileItemHandler.super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return ITileEnergyHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return ITileItemHandler.super.getCapability(capability, facing);
            }
            return null;
        }

        @Override
        default NBTTagCompound serializeNBT() {
            return writeToNBT(new NBTTagCompound());
        }

        @Override
        default void deserializeNBT(NBTTagCompound nbtTagCompound) {
            ITileItemHandler.super.deserializeNBT(nbtTagCompound);
            ITileEnergyHandler.super.deserializeNBT(nbtTagCompound);
        }

        @Override
        default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
            ITileItemHandler.super.writeToNBT(nbtTagCompound);
            ITileEnergyHandler.super.writeToNBT(nbtTagCompound);
            return nbtTagCompound;
        }
    }

    public interface ItemAndFluid extends ITileItemHandler, ITileFluidHandler {
        @Override
        default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return ITileItemHandler.super.hasCapability(capability, facing)
                    || ITileFluidHandler.super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return ITileItemHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return ITileFluidHandler.super.getCapability(capability, facing);
            }
            return null;
        }

        @Override
        default NBTTagCompound serializeNBT() {
            return writeToNBT(new NBTTagCompound());
        }

        @Override
        default void deserializeNBT(NBTTagCompound nbtTagCompound) {
            ITileItemHandler.super.deserializeNBT(nbtTagCompound);
            ITileFluidHandler.super.deserializeNBT(nbtTagCompound);
        }

        @Override
        default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
            ITileItemHandler.super.writeToNBT(nbtTagCompound);
            ITileFluidHandler.super.writeToNBT(nbtTagCompound);
            return nbtTagCompound;
        }
    }
}
