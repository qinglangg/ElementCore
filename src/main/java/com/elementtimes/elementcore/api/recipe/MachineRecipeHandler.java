package com.elementtimes.elementcore.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * 保存合成配方的类
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class MachineRecipeHandler {
    private List<MachineRecipe> mMachineRecipes = new ArrayList<>();

    public int inputItemCount, outputItemCount, inputFluidCount, outputFluidCount;

    public MachineRecipeHandler(int inputItemCount, int outputItemCount, int inputFluidCount, int outputFluidCount) {
        this.inputItemCount = inputItemCount;
        this.outputItemCount = outputItemCount;
        this.inputFluidCount = inputFluidCount;
        this.outputFluidCount = outputFluidCount;
    }

    /**
     * 获取所有合成配方
     * @return 配方列表
     */
    public List<MachineRecipe> getMachineRecipes() {
        return mMachineRecipes;
    }

    /**
     * 创建一个 MachineRecipeBuilder 辅助创建新的合成配方。
     * 所有 newRecipe 方法重载最终都会通过该方法添加
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder newRecipe() {
        return new MachineRecipeBuilder(this);
    }

    /**
     * 匹配输入符合的合成表
     * @return 筛选出的符合要求的合成表
     */
    @Nonnull
    public MachineRecipeCapture[] matchInput(List<ItemStack> input, List<FluidStack> fluids) {
        List<MachineRecipeCapture> captures = new ArrayList<>(mMachineRecipes.size());
        for (MachineRecipe recipe : mMachineRecipes) {
            MachineRecipeCapture capture = recipe.matchInput(input, fluids);
            if (capture != null) {
                captures.add(capture);
            }
        }
        return captures.toArray(new MachineRecipeCapture[0]);
    }

    /**
     * 可能输入有对应合成表
     * @return 是否可能有合成
     */
    public boolean acceptInput(List<ItemStack> input, List<FluidStack> fluids) {
        for (MachineRecipe recipe : mMachineRecipes) {
           if (recipe.checkInput(input, fluids)) {
               return true;
           }
        }
        return false;
    }

    /**
     * 调整配方 IO 数量
     */
    public void resetSize(int inputItem, int outputInput, int inputFluid, int outputFluid) {
        inputItemCount = inputItem;
        outputItemCount = outputInput;
        inputFluidCount = inputFluid;
        outputFluidCount = outputFluid;

        List<MachineRecipe> recipes = new ArrayList<>();
        for (MachineRecipe machineRecipe : mMachineRecipes) {
            recipes.add(machineRecipe.resize(inputItem, outputInput, inputFluid, outputFluid));
        }
        mMachineRecipes = recipes;
    }
}
