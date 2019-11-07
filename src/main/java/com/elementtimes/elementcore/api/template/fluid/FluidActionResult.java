package com.elementtimes.elementcore.api.template.fluid;

import net.minecraft.item.ItemStack;

public class FluidActionResult {
    public final FluidStack transfer;
    public final ItemStack result;

    public FluidActionResult(ItemStack result, FluidStack transfer) {
        this.result = result;
        this.transfer = transfer;
    }
}
