package com.elementtimes.elementcore.api.interfaces.block;

import com.elementtimes.elementcore.api.recipe.MachineRecipeCapture;
import com.elementtimes.elementcore.api.recipe.MachineRecipeHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

/**
 * 提供合成表及一系列方法
 * @author luqin2007
 */
public interface IMachineRecipe {

    String NBT_RECIPE = "ECC";

    /**
     * 获取合成表
     * @return 合成表
     */
    MachineRecipeHandler getRecipes();


    /**
     * 根据所在上下文获取正在执行的合成表
     * @return 正在执行的配方
     */
    @Nullable
    MachineRecipeCapture getWorkingRecipe();

    /**
     * 设置正在执行的合成表
     * @param capture 设置合成表
     */
    void setWorkingRecipe(MachineRecipeCapture capture);

    default void read(CompoundNBT nbt) {
        if (nbt.contains(NBT_RECIPE) && getRecipes() != null) {
            CompoundNBT recipe = nbt.getCompound(NBT_RECIPE);
            setWorkingRecipe(MachineRecipeCapture.fromNbt(recipe));
        }
    }

    default CompoundNBT write(CompoundNBT CompoundNBT) {
        MachineRecipeCapture recipe = getWorkingRecipe();
        if (recipe != null) {
            CompoundNBT.put(NBT_RECIPE, recipe.serializeNBT());
        }
        return CompoundNBT;
    }
}
