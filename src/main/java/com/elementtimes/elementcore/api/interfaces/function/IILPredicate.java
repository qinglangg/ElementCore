package com.elementtimes.elementcore.api.interfaces.function;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@FunctionalInterface
public interface IILPredicate<T> {

    boolean test(int var0, int var1, T obj);

    @FunctionalInterface
    interface FluidPredicate extends IILPredicate<FluidStack> {
        @Override
        boolean test(int slot, int amount, FluidStack stack);
    }

    @FunctionalInterface
    interface ItemPredicate extends IILPredicate<ItemStack> {
        @Override
        boolean test(int slot, int amount, ItemStack stack);
    }
}
