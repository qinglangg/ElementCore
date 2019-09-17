package com.elementtimes.elementcore.api.template.tileentity.recipe;

import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代表一个实际的合成表。
 * 此合成表是确定的，输入 输出都已明确，唯有能量消耗不确定。
 * 主要是考虑到能量消耗可能因配置文件或其他原因随时更改，且能量准确值在确定消耗前用处不大
 * @author luqin2007
 */
public class MachineRecipeCapture implements INBTSerializable<NBTTagCompound> {
    public MachineRecipe recipe;
    public List<ItemStack> inputs;
    public List<ItemStack> outputs;
    public List<FluidStack> fluidInputs;
    public int[] fluidInputAmounts;
    public List<FluidStack> fluidOutputs;
    public int[] fluidOutputAmounts;
    public int energy;

    private static final String NBT_RECIPE_ITEM_INPUT = "_recipe_item_input_";
    private static final String NBT_RECIPE_ITEM_OUTPUT = "_recipe_item_output_";
    private static final String NBT_RECIPE_FLUID_INPUT = "_recipe_fluid_input_";
    private static final String NBT_RECIPE_FLUID_OUTPUT = "_recipe_fluid_output_";
    private static final String NBT_RECIPE_FLUID_INPUT_AMOUNT = "_recipe_fluid_input_amount_";
    private static final String NBT_RECIPE_FLUID_OUTPUT_AMOUNT = "_recipe_fluid_output_amount_";
    private static final String NBT_RECIPE_ENERGY = "_recipe_energy_";

    /**
     * 从 NBT 数据恢复合成表
     * @param nbt NBT 数据
     * @return 恢复的合成表
     */
    public static MachineRecipeCapture fromNbt(NBTTagCompound nbt) {
        MachineRecipeCapture capture = new MachineRecipeCapture();
        capture.deserializeNBT(nbt);
        return capture;
    }

    /**
     * 仅用于 fromNbt 方法
     */
    private MachineRecipeCapture() {}

    /**
     * 根据输入输出，确定每个参与合成的物品/流体的实际种类、数量
     * @param recipe 选定的合成表
     * @param input 物品输入
     * @param fluids 流体输入
     */
    MachineRecipeCapture(MachineRecipe recipe, List<ItemStack> input, List<FluidStack> fluids) {
        this.recipe = recipe;

        this.inputs = new ArrayList<>(recipe.inputs.size());
        for (int i = 0; i < recipe.inputs.size(); i++) {
            IngredientPart<ItemStack> part = recipe.inputs.get(i);
            inputs.add(i, part.getter.apply(recipe, input, fluids, i, part.probability));
        }

        this.outputs = new ArrayList<>(recipe.outputs.size());
        for (int i = 0; i < recipe.outputs.size(); i++) {
            IngredientPart<ItemStack> part = recipe.outputs.get(i);
            outputs.add(i, part.getter.apply(recipe, input, fluids, i, part.probability));
        }

        this.fluidInputs = new ArrayList<>(recipe.fluidInputs.size());
        this.fluidInputAmounts = new int[recipe.fluidInputs.size()];
        for (int i = 0; i < recipe.fluidInputs.size(); i++) {
            IngredientPart<FluidStack> part = recipe.fluidInputs.get(i);
            FluidStack fluid = part.getter.apply(recipe, input, fluids, i, part.probability);
            fluidInputs.add(i, fluid);
            fluidInputAmounts[i] = fluid.amount;
        }

        this.fluidOutputs = new ArrayList<>(recipe.fluidOutputs.size());
        this.fluidOutputAmounts = ECUtils.array.newArray(recipe.fluidOutputs.size(), 0);
        for (int i = 0; i < recipe.fluidOutputs.size(); i++) {
            IngredientPart<FluidStack> part = recipe.fluidOutputs.get(i);
            FluidStack fluid = part.getter.apply(recipe, input, fluids, i, part.probability);
            fluidOutputs.add(i, fluid);
            fluidOutputAmounts[i] = fluid.amount;
        }

        this.energy = recipe.energy.applyAsInt(this);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        nbtRecipe.setTag(NBT_RECIPE_ITEM_INPUT, ECUtils.item.toNBTList(inputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_INPUT, ECUtils.fluid.saveToNbt(fluidInputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_INPUT_AMOUNT, new NBTTagIntArray(fluidInputAmounts));
        nbtRecipe.setTag(NBT_RECIPE_ITEM_OUTPUT, ECUtils.item.toNBTList(outputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_OUTPUT, ECUtils.fluid.saveToNbt(fluidOutputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_OUTPUT_AMOUNT, new NBTTagIntArray(fluidOutputAmounts));
        nbtRecipe.setInteger(NBT_RECIPE_ENERGY, energy);
        return nbtRecipe;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.inputs = !nbt.hasKey(NBT_RECIPE_ITEM_INPUT) ? Collections.emptyList()
                : ECUtils.item.fromNBTList((NBTTagList) nbt.getTag(NBT_RECIPE_ITEM_INPUT));
        this.outputs = !nbt.hasKey(NBT_RECIPE_ITEM_OUTPUT) ? Collections.emptyList()
                : ECUtils.item.fromNBTList((NBTTagList) nbt.getTag(NBT_RECIPE_ITEM_OUTPUT));
        this.fluidInputs = !nbt.hasKey(NBT_RECIPE_FLUID_INPUT) ? Collections.emptyList()
                : ECUtils.fluid.readFromNbt((NBTTagList) nbt.getTag(NBT_RECIPE_FLUID_INPUT));
        this.fluidOutputs = !nbt.hasKey(NBT_RECIPE_FLUID_OUTPUT) ? Collections.emptyList()
                : ECUtils.fluid.readFromNbt((NBTTagList) nbt.getTag(NBT_RECIPE_FLUID_OUTPUT));
        this.fluidInputAmounts = !nbt.hasKey(NBT_RECIPE_FLUID_INPUT_AMOUNT) ? ECUtils.array.newArray(fluidInputs.size(), 0)
                : nbt.getIntArray(NBT_RECIPE_FLUID_INPUT_AMOUNT);
        this.fluidOutputAmounts = !nbt.hasKey(NBT_RECIPE_FLUID_OUTPUT_AMOUNT) ? ECUtils.array.newArray(fluidOutputs.size(), 0)
                : nbt.getIntArray(NBT_RECIPE_FLUID_OUTPUT_AMOUNT);
        this.energy = nbt.getInteger(NBT_RECIPE_ENERGY);
    }
}
