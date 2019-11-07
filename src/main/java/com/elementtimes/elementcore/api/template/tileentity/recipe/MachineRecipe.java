package com.elementtimes.elementcore.api.template.tileentity.recipe;

import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

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
        if (input.size() >= inputs.size() || fluids.size() >= fluidInputs.size()) {
            int size = Math.max(inputs.size(), fluidInputs.size());
            for (int i = 0; i < size; i++) {
                if (i < inputs.size()) {
                    if (!input.get(i).isEmpty() && !inputs.get(i).accept.apply(this, i, input, fluids, input.get(i))) {
                        result = false;
                        break;
                    }
                }

                if (i < fluidInputs.size()) {
                    if (fluids.get(i) != null && fluids.get(i).getAmount() > 0 && !fluidInputs.get(i).accept.apply(this, i, input, fluids, fluids.get(i))) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }
}