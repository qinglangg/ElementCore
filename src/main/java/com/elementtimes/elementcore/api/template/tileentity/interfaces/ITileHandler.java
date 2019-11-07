package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对 物品/流体/能量 接口的一个综合
 * @author luqin2007
 */
public interface ITileHandler {

    interface All extends ITileEnergyHandler, ITileItemHandler, ITileFluidHandler {

        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return ITileEnergyHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return ITileItemHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == com.elementtimes.elementcore.api.template.capability.fluid.vanilla.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return ITileFluidHandler.super.getCapability(capability, facing);
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(@Nonnull CompoundNBT compound) {
            ITileItemHandler.super.read(compound);
            ITileEnergyHandler.super.read(compound);
            ITileFluidHandler.super.read(compound);
        }

        @Nonnull
        @Override
        default CompoundNBT write(@Nonnull CompoundNBT compound) {
            compound = ITileItemHandler.super.write(compound);
            compound = ITileEnergyHandler.super.write(compound);
            compound = ITileFluidHandler.super.write(compound);
            return compound;
        }
    }

    interface ItemAndEnergy extends ITileEnergyHandler, ITileFluidHandler {

        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return ITileEnergyHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == com.elementtimes.elementcore.api.template.capability.fluid.vanilla.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return ITileFluidHandler.super.getCapability(capability, facing);
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(@Nonnull CompoundNBT compound) {
            ITileFluidHandler.super.read(compound);
            ITileEnergyHandler.super.read(compound);
        }

        @Nonnull
        @Override
        default CompoundNBT write(@Nonnull CompoundNBT compound) {
            compound = ITileEnergyHandler.super.write(compound);
            compound = ITileFluidHandler.super.write(compound);
            return compound;
        }
    }

    interface FluidAndEnergy extends ITileEnergyHandler, ITileItemHandler {

        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
            if (capability == CapabilityEnergy.ENERGY) {
                return ITileEnergyHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return ITileItemHandler.super.getCapability(capability, facing);
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(@Nonnull CompoundNBT compound) {
            ITileItemHandler.super.read(compound);
            ITileEnergyHandler.super.read(compound);
        }

        @Nonnull
        @Override
        default CompoundNBT write(@Nonnull CompoundNBT compound) {
            compound = ITileItemHandler.super.write(compound);
            compound = ITileEnergyHandler.super.write(compound);
            return compound;
        }
    }

    interface ItemAndFluid extends ITileItemHandler, ITileFluidHandler {

        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return ITileItemHandler.super.getCapability(capability, facing);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return ITileFluidHandler.super.getCapability(capability, facing);
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(@Nonnull CompoundNBT compound) {
            ITileItemHandler.super.read(compound);
            ITileFluidHandler.super.read(compound);
        }

        @Nonnull
        @Override
        default CompoundNBT write(@Nonnull CompoundNBT compound) {
            compound = ITileItemHandler.super.write(compound);
            compound = ITileFluidHandler.super.write(compound);
            return compound;
        }
    }
}
