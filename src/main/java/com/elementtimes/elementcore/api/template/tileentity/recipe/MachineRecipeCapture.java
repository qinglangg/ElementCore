package com.elementtimes.elementcore.api.template.tileentity.recipe;

import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代表一个实际的合成表。
 * 此合成表是确定的，输入 输出都已明确。
 * @author luqin2007
 */
public class MachineRecipeCapture implements INBTSerializable<NBTTagCompound> {
    public MachineRecipe recipe;
    public NonNullList<ItemStack> inputs;
    public NonNullList<ItemStack> outputs;
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

        this.inputs = NonNullList.withSize(recipe.inputs.size(), ItemStack.EMPTY);
        for (int i = 0; i < recipe.inputs.size(); i++) {
            IngredientPart<ItemStack> part = recipe.inputs.get(i);
            inputs.set(i, part.getter.apply(recipe, input, fluids, i, part.probability));
        }

        this.outputs = NonNullList.withSize(recipe.outputs.size(), ItemStack.EMPTY);
        for (int i = 0; i < recipe.outputs.size(); i++) {
            IngredientPart<ItemStack> part = recipe.outputs.get(i);
            outputs.set(i, part.getter.apply(recipe, input, fluids, i, part.probability));
        }

        this.fluidInputs = new ArrayList<>();
        this.fluidInputAmounts = new int[recipe.fluidInputs.size()];
        for (int i = 0; i < recipe.fluidInputs.size(); i++) {
            IngredientPart<FluidStack> part = recipe.fluidInputs.get(i);
            FluidStack fluid = part.getter.apply(recipe, input, fluids, i, part.probability);
            fluidInputs.add(i, fluid);
            fluidInputAmounts[i] = fluid.amount;
        }

        this.fluidOutputs = new ArrayList<>();
        this.fluidOutputAmounts = new int[recipe.fluidOutputs.size()];
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
        nbtRecipe.setTag(NBT_RECIPE_ITEM_INPUT, ECUtils.item.saveAllItems(inputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_INPUT, ECUtils.fluid.saveToNbt(fluidInputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_INPUT_AMOUNT, new NBTTagIntArray(fluidInputAmounts));
        nbtRecipe.setTag(NBT_RECIPE_ITEM_OUTPUT, ECUtils.item.saveAllItems(outputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_OUTPUT, ECUtils.fluid.saveToNbt(fluidOutputs));
        nbtRecipe.setTag(NBT_RECIPE_FLUID_OUTPUT_AMOUNT, new NBTTagIntArray(fluidOutputAmounts));
        nbtRecipe.setInteger(NBT_RECIPE_ENERGY, energy);
        return nbtRecipe;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.inputs = !nbt.hasKey(NBT_RECIPE_ITEM_INPUT) ? NonNullList.create()
                : ECUtils.item.loadAllItems((NBTTagList) nbt.getTag(NBT_RECIPE_ITEM_INPUT));
        this.outputs = !nbt.hasKey(NBT_RECIPE_ITEM_OUTPUT) ? NonNullList.create()
                : ECUtils.item.loadAllItems((NBTTagList) nbt.getTag(NBT_RECIPE_ITEM_OUTPUT));
        this.fluidInputs = !nbt.hasKey(NBT_RECIPE_FLUID_INPUT) ? Collections.emptyList()
                : ECUtils.fluid.readFromNbt((NBTTagList) nbt.getTag(NBT_RECIPE_FLUID_INPUT));
        this.fluidOutputs = !nbt.hasKey(NBT_RECIPE_FLUID_OUTPUT) ? Collections.emptyList()
                : ECUtils.fluid.readFromNbt((NBTTagList) nbt.getTag(NBT_RECIPE_FLUID_OUTPUT));
        this.fluidInputAmounts = !nbt.hasKey(NBT_RECIPE_FLUID_INPUT_AMOUNT) ? new int[fluidInputs.size()]
                : nbt.getIntArray(NBT_RECIPE_FLUID_INPUT_AMOUNT);
        this.fluidOutputAmounts = !nbt.hasKey(NBT_RECIPE_FLUID_OUTPUT_AMOUNT) ? new int[fluidOutputs.size()]
                : nbt.getIntArray(NBT_RECIPE_FLUID_OUTPUT_AMOUNT);
        this.energy = nbt.getInteger(NBT_RECIPE_ENERGY);
    }
}
