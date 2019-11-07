package com.elementtimes.elementcore.api.template.capability.fluid.vanilla;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import com.elementtimes.elementcore.api.template.capability.fluid.FluidTankInfo;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * Minecraft Fluid 版本的 FluidHandler
 * @author luqin2007
 */
public class FluidHandler implements IFluidHandler {

    public static final BiPredicate<Integer, FluidStack> TRUE = (i, f) -> true;
    public static final BiPredicate<Integer, FluidStack> FALSE = (i, f) -> false;
    public static final FluidHandler EMPTY = new FluidHandler(0, 0);

    private FluidStack[] stacks;
    private int[] capacities;
    private int size;

    private BiPredicate<Integer, FluidStack> isFillValid, isDrainValid;

    public FluidHandler(int slot, int capacity) {
        size = slot;
        stacks = new FluidStack[size];
        Arrays.fill(stacks, FluidStack.EMPTY);
        capacities = new int[size];
        Arrays.fill(capacities, capacity);
    }

    public FluidHandler(int[] capacities) {
        size = capacities.length;
        stacks = new FluidStack[size];
        Arrays.fill(stacks, FluidStack.EMPTY);
        this.capacities = capacities;
    }

    public FluidHandler(FluidStack... stacks) {
        size = stacks.length;
        this.stacks = stacks;
        capacities = new int[size];
        for (int i = 0; i < size; i++) {
            capacities[i] = stacks[i].getAmount();
        }
    }

    public FluidHandler setFillValid(BiPredicate<Integer, FluidStack> f) {
        isFillValid = f;
        return this;
    }

    public FluidHandler setDrainValid(BiPredicate<Integer, FluidStack> d) {
        isDrainValid = d;
        return this;
    }

    @Override
    public FluidTankInfo[] getTankInfos() {
        FluidTankInfo[] infos = new FluidTankInfo[getSize()];
        for (int i = 0; i < getSize(); i++) {
            infos[i] = new FluidTankInfo(stacks[i], capacities[i]);
        }
        return infos;
    }

    @Override
    public Fluid getFluid(int slot) {
        return stacks[slot].getFluid();
    }

    @Override
    public FluidStack getFluidStack(int slot) {
        return stacks[slot];
    }

    @Override
    public void setFluid(int slot, Fluid fluid, int amount) {
        if (isSetValid(slot, fluid, amount)) {
            setNotFilter(slot, fluid, amount);
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean isFull(int slot) {
        return stacks[slot].getAmount() >= capacities[slot];
    }

    @Override
    public boolean isFull() {
        for (int i = 0; i < getSize(); i++) {
            if (!isFull(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty(int slot) {
        return stacks[slot].isEmpty();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getSize(); i++) {
            if (!isEmpty(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCapacity(int slot) {
        return capacities[slot];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource.isEmpty()) {
            return 0;
        }
        Fluid fluidFill = resource.getFluid();
        int amountAll = resource.getAmount();
        int amount = amountAll;
        for (int i = 0; i < getSize(); i++) {
            if (isFillValid(i, resource.getFluid(), amount)) {
                if (isEmpty(i) || ECUtils.fluid.isEquivalent(getFluid(i), fluidFill)) {
                    if (!isFull(i)) {
                        amount -= doFill(i, amount, doFill);
                        if (amount <= 0) {
                            return amountAll;
                        }
                    }
                }
            }
        }
        return amountAll - amount;
    }

    @Override
    public int fill(int slot, FluidStack resource, boolean doFill) {
        if (resource.isEmpty() || !isFillValid(slot, resource.getFluid(), resource.getAmount())) {
            return 0;
        }
        int amountAll = resource.getAmount();
        if (isEmpty(slot) || ECUtils.fluid.isEquivalent(getFluid(slot), resource.getFluid())) {
            if (!isFull(slot)) {
                return doFill(slot, amountAll, doFill);
            }
        }
        return 0;
    }

    private int doFill(int slot, int amount, boolean doFill) {
        FluidStack stack = stacks[slot];
        int capacity = capacities[slot];
        int space = capacity - stack.getAmount();
        if (amount <= space) {
            if (doFill) {
                stack.increase(amount);
            }
            return amount;
        } else {
            if (doFill) {
                stack.setAmount(capacity);
            }
            return space;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int drainTotal = resource.getAmount();
        int drain = drainTotal;
        Fluid fluidDrain = resource.getFluid();
        for (int i = 0; i < getSize(); i++) {
            if (isDrainValid(i, fluidDrain, drain)) {
                FluidStack stack = stacks[i];
                Fluid fluid = stack.getFluid();
                if (!isEmpty(i) && ECUtils.fluid.isEquivalent(fluidDrain, fluid)) {
                    drain -= doDrain(i, drain, doDrain);
                    if (drain <= 0) {
                        return resource.copy();
                    }
                }
            }
        }
        return new FluidStack(fluidDrain, drainTotal - drain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        int drain = maxDrain;
        Fluid fluidDrain = Fluids.EMPTY;
        for (int i = 0; i < getSize(); i++) {
            if (!isEmpty(i)) {
                if (fluidDrain == Fluids.EMPTY) {
                    fluidDrain = getFluid(i);
                }
                if (ECUtils.fluid.isEquivalent(fluidDrain, getFluid(i)) && isDrainValid(i, fluidDrain, drain)) {
                    drain -= doDrain(i, drain, doDrain);
                    if (drain <= 0) {
                        return new FluidStack(fluidDrain, maxDrain);
                    }
                }
            }
        }
        return new FluidStack(fluidDrain, maxDrain - drain);
    }

    @Override
    public FluidStack drain(int slot, FluidStack resource, boolean doDrain) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = stacks[slot];
        Fluid fluid = stack.getFluid();
        if (!isEmpty(slot) && ECUtils.fluid.isEquivalent(resource.getFluid(), fluid) && isDrainValid(slot, resource.getFluid(), resource.getAmount())) {
            return new FluidStack(getFluid(slot), doDrain(slot, resource.getAmount(), doDrain));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = stacks[slot];
        if (!isEmpty(slot) && isDrainValid(slot, stack.getFluid(), maxDrain)) {
            return new FluidStack(getFluid(slot), doDrain(slot, maxDrain, doDrain));
        }
        return FluidStack.EMPTY;
    }

    private int doDrain(int slot, int amount, boolean doDrain) {
        FluidStack stack = stacks[slot];
        int contains = stack.getAmount();
        if (contains <= amount) {
            if (doDrain) {
                stack.setEmpty();
            }
            return contains;
        } else {
            if (doDrain) {
                stack.reduce(amount);
            }
            return amount;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (int i = 0; i < getSize(); i++) {
            CompoundNBT fluid = new CompoundNBT();
            fluid.putInt("Capacity", capacities[i]);
            fluid.put("Fluid", stacks[i].serializeNBT());
            list.add(i, fluid);
        }
        compound.put("Fluids", list);
        return compound;
    }

    @Override
    public FluidStack drainNotFilter(int slot, Fluid fluid, int amount, boolean doDrain) {
        FluidStack stack = stacks[slot];
        if (ECUtils.fluid.isEquivalent(fluid, stack.getFluid())) {
            return new FluidStack(stack.getFluid(), doDrain(slot, stack.getAmount(), doDrain));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setNotFilter(int slot, Fluid fluid, int amount) {
        stacks[slot].setFluid(fluid);
        stacks[slot].setAmount(amount);
    }

    @Override
    public int fillNotFilter(int slot, Fluid fluid, int amount, boolean doDrain) {
        if (ECUtils.fluid.isEquivalent(getFluid(slot), fluid)) {
            return doFill(slot, amount, doDrain);
        }
        return 0;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return write(new CompoundNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        ListNBT list = compound.getList("Fluids", Constants.NBT.TAG_COMPOUND);
        size = list.size();
        stacks = new FluidStack[size];
        capacities = new int[size];
        for (int i = 0; i < size; i++) {
            CompoundNBT fluid = list.getCompound(i);
            capacities[i] = fluid.getInt("Capacity");
            stacks[i] = FluidStack.fromNbt(fluid.getCompound("Fluid"));
        }
    }

    @Override
    public boolean isDrainValid(int slot, Fluid fluid, int amount) {
        return isDrainValid.test(slot, new FluidStack(fluid, amount));
    }

    @Override
    public boolean isFillValid(int slot, Fluid fluid, int amount) {
        return isFillValid.test(slot, new FluidStack(fluid, amount));
    }
}
