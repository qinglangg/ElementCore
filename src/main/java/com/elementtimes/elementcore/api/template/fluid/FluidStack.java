package com.elementtimes.elementcore.api.template.fluid;

import com.elementtimes.elementcore.api.ECUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class FluidStack implements INBTSerializable<CompoundNBT> {

    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private Fluid fluid;
    private int amount;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public void setEmpty() {
        amount = 0;
    }

    public boolean isEmpty() {
        return amount <= 0 || fluid == Fluids.EMPTY;
    }

    public void increase(int amount) {
        this.amount += amount;
    }

    public void reduce(int amount) {
        this.amount -= amount;
        if (this.amount < 0) {
            this.amount = 0;
        }
    }

    public FluidStack copy() {
        return new FluidStack(fluid, amount);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("Fluid", fluid.getRegistryName().toString());
        nbt.putInt("Amount", amount);
        return null;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        fluid = ECUtils.fluid.getFluid(nbt.getString("Fluid"));
        amount = nbt.getInt("Amount");
    }

    public static FluidStack fromNbt(CompoundNBT nbt) {
        FluidStack stack = EMPTY.copy();
        stack.deserializeNBT(nbt);
        return stack;
    }

    public boolean contains(FluidStack stack) {
        return stack.isEmpty() || (isFluidEqual(stack) && stack.amount <= amount);
    }

    public boolean isFluidEqual(FluidStack stack) {
        return fluid == stack.getFluid();
    }
}
