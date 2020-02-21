package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeCapture;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeHandler;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 提供合成表及一系列方法
 * @author luqin2007
 */
public interface IMachineRecipe extends INBTSerializable<NBTTagCompound> {

    String NBT_RECIPE = "_recipe_";

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
     * 合成表检查时忽略的机器槽位
     * @return 忽略槽位
     */
    @Nonnull
    IntSet getRecipeSlotIgnore();

    /**
     * 设置正在执行的合成表
     * @param capture 设置合成表
     */
    void setWorkingRecipe(MachineRecipeCapture capture);

    @Override
    default NBTTagCompound serializeNBT() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(NBT_RECIPE) && getRecipes() != null) {
            NBTTagCompound recipe = nbt.getCompoundTag(NBT_RECIPE);
            MachineRecipeCapture capture = MachineRecipeCapture.fromNbt(recipe);
            setWorkingRecipe(capture);
        }
    }

    default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        MachineRecipeCapture recipe = getWorkingRecipe();
        if (recipe != null) {
            NBTTagCompound nbt = recipe.serializeNBT();
            nbtTagCompound.setTag(NBT_RECIPE, nbt);
        }
        return nbtTagCompound;
    }
}
