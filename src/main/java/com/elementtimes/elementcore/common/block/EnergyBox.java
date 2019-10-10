package com.elementtimes.elementcore.common.block;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.template.block.BlockTileBase;
import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileEnergyHandler;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.EnergyGeneratorLifecycle;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 创造能量盒
 * @author luqin2007
 */
public class EnergyBox extends BlockTileBase<EnergyBox.TileEntity> implements ITileEntityProvider {

    public EnergyBox() {
        super(TileEntity.class, ElementCore.instance());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntity();
    }

    public static class TileEntity extends net.minecraft.tileentity.TileEntity implements ITileEnergyHandler, ITickable {

        private EnergyHandler mEnergyHandler;
        private EnergyGeneratorLifecycle mLifecycle;

        public TileEntity() {
            mEnergyHandler = new EnergyHandler(-1, Integer.MAX_VALUE, Integer.MAX_VALUE);
            mLifecycle = new EnergyGeneratorLifecycle<>(this);
        }

        @Override
        public EnergyHandler getEnergyHandler() {
            return mEnergyHandler;
        }

        @Override
        public SideHandlerType getEnergyType(EnumFacing facing) {
            return SideHandlerType.ALL;
        }

        @Override
        public void update() {
            mLifecycle.onTickFinish();
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            ITileEnergyHandler.super.deserializeNBT(compound);
        }

        @Override
        @Nonnull
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound = super.writeToNBT(compound);
            compound = ITileEnergyHandler.super.writeToNBT(compound);
            return compound;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            T energy = ITileEnergyHandler.super.getCapability(capability, facing);
            return energy != null ? energy : super.getCapability(capability, facing);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return ITileEnergyHandler.super.hasCapability(capability, facing)
                    || super.hasCapability(capability, facing);
        }
    }
}
