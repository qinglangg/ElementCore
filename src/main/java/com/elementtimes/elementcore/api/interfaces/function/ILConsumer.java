package com.elementtimes.elementcore.api.interfaces.function;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ILConsumer<T> {

    void accept(int slot, T obj);

    @FunctionalInterface
    interface ItemConsumer extends ILConsumer<ItemStack> {
        @Override
        void accept(int slot, ItemStack stack);
    }
}
