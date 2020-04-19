package com.elementtimes.elementcore.api.template.tileentity.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * 代表一个配方
 * 考虑到某些原料可被替代，该配方是不准确的
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class MachineRecipe {
    public List<IngredientPart<ItemStack>> inputs;
    public List<IngredientPart<ItemStack>> outputs;
    public List<IngredientPart<FluidStack>> fluidInputs;
    public List<IngredientPart<FluidStack>> fluidOutputs;
    public ToIntFunction<MachineRecipeCapture> energy = (a) -> 0;

    /**
     * 根据输入，从一个合成配方中获取匹配的准确合成表
     * @param input 输入物品
     * @param fluids 输入流体
     * @return 精准的合成表
     */
    public MachineRecipeCapture matchInput(List<ItemStack> input, List<FluidStack> fluids) {
        boolean match = input.size() >= inputs.size() && fluids.size() >= fluidInputs.size();
        // item
        for (int i = 0; match && i < inputs.size(); i++) {
            match = inputs.get(i).matcher.apply(this, i, input, fluids, input.get(i));
        }
        // fluid
        for (int i = 0; match && i < fluidInputs.size(); i++) {
            match = fluidInputs.get(i).matcher.apply(this, i, input, fluids, fluids.get(i));
        }
        // endAdd
        return match ? new MachineRecipeCapture(this, input, fluids) : null;
    }

    /**
     * 根据输入 判断输入是否可能作为合成表的一部分
     * @param input 输入物品
     * @param fluids 输入流体
     * @return 是否可能为该合成表
     */
    public boolean checkInput(List<ItemStack> input, List<FluidStack> fluids) {
        // check
        boolean result = true;
        int size = Math.max(inputs.size(), fluidInputs.size());
        int inputItemSize = input.size();
        int inputFluidSize = fluids.size();
        size = Math.max(size, inputItemSize);
        size = Math.max(size, inputFluidSize);
        for (int i = 0; i < size; i++) {
            if (i < inputItemSize) {
                if (!input.get(i).isEmpty() && !inputs.get(i).accept.apply(this, i, input, fluids, input.get(i))) {
                    result = false;
                    break;
                }
            }
            if (i < inputFluidSize) {
                FluidStack fs = fluids.get(i);
                if (fs != null && fs.amount > 0 && !fluidInputs.get(i).accept.apply(this, i, input, fluids, fs)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public MachineRecipe resize(int inputItem, int outputInput, int inputFluid, int outputFluid) {
        MachineRecipe recipe = new MachineRecipe();
        recipe.energy = energy;
        recipe.inputs = NonNullList.withSize(inputItem, IngredientPart.EMPTY_ITEM);
        for (int i = 0; i < Math.min(inputs.size(), recipe.inputs.size()); i++) {
            recipe.inputs.set(i, inputs.get(i));
        }
        recipe.outputs = NonNullList.withSize(outputInput, IngredientPart.EMPTY_ITEM);
        for (int i = 0; i < Math.min(outputs.size(), recipe.outputs.size()); i++) {
            recipe.outputs.set(i, outputs.get(i));
        }
        recipe.fluidInputs = NonNullList.withSize(inputFluid, IngredientPart.EMPTY_FLUID);
        for (int i = 0; i < Math.min(fluidInputs.size(), recipe.fluidInputs.size()); i++) {
            recipe.fluidInputs.set(i, fluidInputs.get(i));
        }
        recipe.fluidOutputs = NonNullList.withSize(outputFluid, IngredientPart.EMPTY_FLUID);
        for (int i = 0; i < Math.min(fluidOutputs.size(), recipe.fluidOutputs.size()); i++) {
            recipe.fluidOutputs.set(i, fluidOutputs.get(i));
        }
        return recipe;
    }
}