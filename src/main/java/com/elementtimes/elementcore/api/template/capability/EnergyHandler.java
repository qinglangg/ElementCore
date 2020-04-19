package com.elementtimes.elementcore.api.template.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

/**
 * 自定义实现的 EnergyStorage
 * 主要自定义实现了 NBT 序列化/反序列化，添加一个代理类
 *
 * 若 capacity = -1 则表示该容器包含无限能量
 *
 * @author KSGFK create in 2019/3/9
 */
@SuppressWarnings("unused")
public class EnergyHandler extends EnergyStorage implements INBTSerializable<NBTTagCompound> {

    private int transfer = Integer.MAX_VALUE;
    private IntSupplier capacitySupplier = null;
    private IntSupplier transferSupplier = null;

    public EnergyHandler(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setTransfer(int transfer) {
        this.transfer = transfer;
    }

    public int getTransfer() {
        if (transferSupplier != null) {
            setTransfer(transferSupplier.getAsInt());
        }
        return this.transfer;
    }

    private int getMaxReceive() {
        if (capacity < 0) {
            return 0;
        }
        return maxReceive;
    }

    private int getMaxExtract() {
        if (capacity < 0) {
            return Integer.MAX_VALUE;
        }
        return maxExtract;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (capacity < this.energy) {
            this.energy = capacity;
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (capacity < 0) {
            return maxReceive;
        }
        return super.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (capacity < 0) {
            return maxExtract;
        }
        return super.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        if (capacity < 0) {
            return Integer.MAX_VALUE;
        }
        return super.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        if (capacitySupplier != null) {
            setCapacity(capacitySupplier.getAsInt());
        }
        if (capacity < 0) {
            return Integer.MAX_VALUE;
        }
        return super.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return capacity < 0 || super.canExtract();
    }

    @Override
    public boolean canReceive() {
        return capacity > 0 && super.canReceive();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("capacity", capacity);
        nbt.setInteger("receive", maxReceive);
        nbt.setInteger("extract", maxExtract);
        nbt.setInteger("energy", energy);
        nbt.setInteger("transfer", transfer);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("capacity")) {
            capacity = nbt.getInteger("capacity");
        }

        if (nbt.hasKey("receive")) {
            maxReceive = nbt.getInteger("receive");
        }

        if (nbt.hasKey("extract")) {
            maxExtract = nbt.getInteger("extract");
        }

        if (nbt.hasKey("energy")) {
            energy = nbt.getInteger("energy");
        }
        energy = Math.min(capacity, energy);

        if (nbt.hasKey("transfer")) {
            transfer = nbt.getInteger("transfer");
        }
    }

    public void setCapacitySupplier(IntSupplier capacity) {
        capacitySupplier = capacity;
    }

    public void setTransferSupplier(IntSupplier capacity) {
        capacitySupplier = capacity;
    }

    public class EnergyProxy implements IEnergyStorage {

        private int maxExtract;
        private int maxReceive;

        public EnergyProxy(int maxReceive, int maxExtract) {
            this.maxReceive = Math.min(Math.min(maxReceive, transfer), EnergyHandler.this.getMaxReceive());
            this.maxExtract = Math.min(Math.min(maxExtract, transfer), EnergyHandler.this.getMaxExtract());
        }

        public EnergyProxy(boolean canReceive, boolean canExtract) {
            this(canReceive ? EnergyHandler.this.getMaxReceive() : 0, canExtract ? EnergyHandler.this.getMaxExtract() : 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || maxReceive == 0) {
                return 0;
            }
            return EnergyHandler.this.receiveEnergy(Math.min(maxReceive, this.maxReceive), simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract() || maxExtract == 0) {
                return 0;
            }
            return EnergyHandler.this.extractEnergy(Math.min(maxExtract, this.maxExtract), simulate);
        }

        @Override
        public int getEnergyStored() {
            return EnergyHandler.this.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return EnergyHandler.this.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return maxExtract > 0;
        }

        @Override
        public boolean canReceive() {
            return maxReceive > 0;
        }

        public void setTransfer(int transfer) {
            maxReceive = transfer;
            maxExtract = transfer;
        }
    }
}
