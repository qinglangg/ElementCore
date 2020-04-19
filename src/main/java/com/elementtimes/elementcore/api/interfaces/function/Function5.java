package com.elementtimes.elementcore.api.interfaces.function;

import com.elementtimes.elementcore.api.recipe.MachineRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * 有五个输入的方法
 * 一个函数接口罢了
 *
 * @author luqin2007
 */
@FunctionalInterface
public interface Function5<T1, T2, T3, T4, T5, R> {

    /**
     * 一个函数接口罢了
     * @param v1 param1
     * @param v2 param2
     * @param v3 param3
     * @param v4 param4
     * @param v5 param5
     * @return return
     */
    R apply(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5);

    @FunctionalInterface
    interface Match<T> extends Function5<MachineRecipe, Integer, List<ItemStack>, List<FluidStack>, T, Boolean> {
        /**
         * 用于判断输入物品是否符合合成条件
         * @param recipe 合成表
         * @param slot 输入槽位
         * @param inputItems 所有输入
         * @param inputFluids 所有输出
         * @param input 输入物品
         * @return 是否可以参与合成
         */
        @Override
        Boolean apply(MachineRecipe recipe, Integer slot, List<ItemStack> inputItems, List<FluidStack> inputFluids, T input);
    }

    @FunctionalInterface
    interface StackGetter<T> extends Function5<MachineRecipe, List<ItemStack>, List<FluidStack>, Integer, Float, T> {

        /**
         * 根据输入和合成表，获取匹配的物品
         * @param recipe 合成表
         * @param inputItems 输入物品
         * @param inputFluids 输入流体
         * @param slot 槽位
         * @return 合成所需的物品，包括数量
         */
        @Override
        T apply(MachineRecipe recipe, List<ItemStack> inputItems, List<FluidStack> inputFluids, Integer slot, Float probability);
    }
}