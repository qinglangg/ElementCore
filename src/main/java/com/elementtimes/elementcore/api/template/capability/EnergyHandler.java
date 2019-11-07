package com.elementtimes.elementcore.api.template.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * 自定义实现的 EnergyStorage
 * 主要自定义实现了 NBT 序列化/反序列化，添加一个代理类
 *
 * 若 capacity = -1 则表示该容器包含无限能量
 *
 * @author KSGFK create in 2019/3/9
 */
@SuppressWarnings("unused")
public class EnergyHandler extends EnergyStorage implements INBTSerializable<CompoundNBT> {

    private int transfer = Integer.MAX_VALUE;

    public EnergyHandler(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setTransfer(int transfer) {
        this.transfer = transfer;
    }

    private int getEnergy() {
        if (capacity < 0) {
            return Integer.MAX_VALUE;
        }
        return energy;
    }

    private int getCapacity() {
        if (capacity < 0) {
            return Integer.MAX_VALUE;
        }
        return capacity;
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
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("capacity", capacity);
        nbt.putInt("receive", maxReceive);
        nbt.putInt("extract", maxExtract);
        nbt.putInt("energy", energy);
        nbt.putInt("transfer", transfer);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("capacity")) {
            capacity = nbt.getInt("capacity");
        }

        if (nbt.contains("receive")) {
            maxReceive = nbt.getInt("receive");
        }

        if (nbt.contains("extract")) {
            maxExtract = nbt.getInt("extract");
        }

        if (nbt.contains("energy")) {
            energy = nbt.getInt("energy");
        }
        energy = Math.min(capacity, energy);

        if (nbt.contains("transfer")) {
            transfer = nbt.getInt("transfer");
        }
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
