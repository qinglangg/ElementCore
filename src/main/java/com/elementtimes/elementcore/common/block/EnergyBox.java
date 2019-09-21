package com.elementtimes.elementcore.common.block;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.ECUtils;
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

    private static final String ENERGY_L = "limit";
    private static final String ENERGY_T = "type";

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
        // 能量传输
        private int[] mLimit;
        private EnergyType[] mType;

        public TileEntity() {
            mEnergyHandler = new EnergyHandler(-1, Integer.MAX_VALUE, Integer.MAX_VALUE);
            mLifecycle = new EnergyGeneratorLifecycle<>(this);
            mLimit = ECUtils.array.newArray(6, -1);
            mType = ECUtils.array.newArray(EnergyType.class, 6, EnergyType.BOTH);
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

        public void setLimit(EnumFacing facing, int limit) {
            mLimit[facing.getIndex()] = limit;
            markDirty();
        }

        public int getLimit(EnumFacing facing) {
            return mLimit[facing.getIndex()];
        }

        public void setType(EnumFacing facing, EnergyType type) {
            mType[facing.getIndex()] = type;
            markDirty();
        }

        public EnergyType getType(EnumFacing facing) {
            return mType[facing.getIndex()];
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            if (compound.hasKey(ENERGY_L)) {
                mLimit = compound.getIntArray(ENERGY_L);
            }
            if (compound.hasKey(ENERGY_T)) {
                int[] intArray = compound.getIntArray(ENERGY_T);
                for (int i = 0; i < intArray.length; i++) {
                    mType[i] = EnergyType.get(intArray[i]);
                }
            }
            ITileEnergyHandler.super.deserializeNBT(compound);
        }

        @Override
        @Nonnull
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound = super.writeToNBT(compound);
            compound = ITileEnergyHandler.super.writeToNBT(compound);
            compound.setIntArray(ENERGY_L, mLimit);
            int[] arr = new int[mType.length];
            for (int i = 0; i < mType.length; i++) {
                arr[i] = mType[i].id;
            }
            compound.setIntArray(ENERGY_T, arr);
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

    public enum EnergyType {
        /**
         * 传输类型
         */
        IN_ONLY(0), OUT_ONLY(1), BOTH(2), CLOSE(3);

        int id;

        EnergyType(int id) {
            this.id = id;
        }

        public static EnergyType get(int id) {
            switch (id) {
                case 0: return IN_ONLY;
                case 1: return OUT_ONLY;
                case 3: return CLOSE;
                default: return BOTH;
            }
        }
    }
}
