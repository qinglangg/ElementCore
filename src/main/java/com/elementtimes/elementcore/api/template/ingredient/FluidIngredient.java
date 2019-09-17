package com.elementtimes.elementcore.api.template.ingredient;

import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 流体类型的 Ingredient
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FluidIngredient extends Ingredient {
    private ItemStack mItemStack;

    private FluidIngredient(ItemStack itemStack) {
        mItemStack = itemStack;
    }

    public static FluidIngredient bucket(FluidStack fluidStack) {
        return new FluidIngredient(net.minecraftforge.fluids.FluidUtil.getFilledBucket(fluidStack));
    }

    public static FluidIngredient bucket(Fluid fluid) {
        return bucket(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
    }

    @Override
    @Nonnull
    public ItemStack[] getMatchingStacks() {
        return new ItemStack[] {mItemStack};
    }

    @Override
    public boolean apply(@Nullable ItemStack itemStack) {
        if (itemStack != null && mItemStack.isItemEqual(itemStack)) {
            FluidStack fluidStackIn = ECUtils.fluid.getFluid(itemStack);
            FluidStack fluidStack = ECUtils.fluid.getFluid(mItemStack);
            if (fluidStack != null && fluidStackIn != null) {
                return fluidStack.containsFluid(fluidStackIn);
            }
        }
        return false;
    }
}
