package com.elementtimes.elementcore.api.interfaces.block;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 对 物品/流体/能量 接口的一个综合
 * @author luqin2007
 */
public class ITileHandler {

    public interface All extends ITileEnergyHandler, ITileItemHandler, ITileFluidHandler {

        @Nonnull
        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            LazyOptional<T> capability = ITileEnergyHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            capability = ITileFluidHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            capability = ITileItemHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(CompoundNBT nbt) {
            ITileItemHandler.super.read(nbt);
            ITileEnergyHandler.super.read(nbt);
            ITileFluidHandler.super.read(nbt);
        }

        @Override
        default CompoundNBT write(CompoundNBT nbt) {
            nbt = ITileItemHandler.super.write(nbt);
            nbt = ITileEnergyHandler.super.write(nbt);
            nbt = ITileFluidHandler.super.write(nbt);
            return nbt;
        }
    }

    public interface ItemAndEnergy extends ITileEnergyHandler, ITileFluidHandler {

        @Nonnull
        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            LazyOptional<T> capability = ITileEnergyHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            capability = ITileFluidHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(CompoundNBT nbt) {
            ITileEnergyHandler.super.read(nbt);
            ITileFluidHandler.super.read(nbt);
        }

        @Override
        default CompoundNBT write(CompoundNBT nbt) {
            ITileEnergyHandler.super.write(nbt);
            ITileFluidHandler.super.write(nbt);
            return nbt;
        }
    }

    public interface FluidAndEnergy extends ITileEnergyHandler, ITileItemHandler {

        @Nonnull
        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            LazyOptional<T> capability = ITileEnergyHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            capability = ITileItemHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(CompoundNBT nbt) {
            ITileItemHandler.super.read(nbt);
            ITileEnergyHandler.super.read(nbt);
        }

        @Override
        default CompoundNBT write(CompoundNBT nbt) {
            ITileItemHandler.super.write(nbt);
            ITileEnergyHandler.super.write(nbt);
            return nbt;
        }
    }

    public interface ItemAndFluid extends ITileItemHandler, ITileFluidHandler {

        @Nonnull
        @Override
        default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            LazyOptional<T> capability = ITileFluidHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            capability = ITileItemHandler.super.getCapability(cap, side);
            if (capability.isPresent()) {
                return capability;
            }
            return LazyOptional.empty();
        }

        @Override
        default void read(CompoundNBT nbt) {
            ITileItemHandler.super.read(nbt);
            ITileFluidHandler.super.read(nbt);
        }

        @Override
        default CompoundNBT write(CompoundNBT nbt) {
            ITileItemHandler.super.write(nbt);
            ITileFluidHandler.super.write(nbt);
            return nbt;
        }
    }
}
