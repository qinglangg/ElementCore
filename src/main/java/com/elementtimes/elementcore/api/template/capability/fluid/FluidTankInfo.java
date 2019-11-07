package com.elementtimes.elementcore.api.template.capability.fluid;

import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import net.minecraft.fluid.Fluid;

import java.util.Objects;

public class FluidTankInfo {

    public final Fluid fluid;
    public final int amount;
    public final int capacity;

    public FluidTankInfo(Fluid fluid, int amount, int capacity) {
        this.fluid = fluid;
        this.amount = amount;
        this.capacity = capacity;
    }

    public FluidTankInfo(FluidStack stack, int capacity) {
        this.fluid = stack.getFluid();
        this.amount = stack.getAmount();
        this.capacity = capacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, amount, capacity);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
    }
}
