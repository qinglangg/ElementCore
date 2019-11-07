package com.elementtimes.elementcore.api.template.tileentity.recipe;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代表一个实际的合成表。
 * 此合成表是确定的，输入 输出都已明确，唯有能量消耗不确定。
 * 主要是考虑到能量消耗可能因配置文件或其他原因随时更改，且能量准确值在确定消耗前用处不大
 * @author luqin2007
 */
public class MachineRecipeCapture implements INBTSerializable<CompoundNBT> {
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
    public static MachineRecipeCapture fromNbt(CompoundNBT nbt) {
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
            fluidInputAmounts[i] = fluid.getAmount();
        }

        this.fluidOutputs = new ArrayList<>(recipe.fluidOutputs.size());
        this.fluidOutputAmounts = ECUtils.array.newArray(recipe.fluidOutputs.size(), 0);
        for (int i = 0; i < recipe.fluidOutputs.size(); i++) {
            IngredientPart<FluidStack> part = recipe.fluidOutputs.get(i);
            FluidStack fluid = part.getter.apply(recipe, input, fluids, i, part.probability);
            fluidOutputs.add(i, fluid);
            fluidOutputAmounts[i] = fluid.getAmount();
        }

        this.energy = recipe.energy.applyAsInt(this);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbtRecipe = new CompoundNBT();
        nbtRecipe.put(NBT_RECIPE_ITEM_INPUT, ECUtils.item.save(inputs));
        nbtRecipe.put(NBT_RECIPE_FLUID_INPUT, ECUtils.fluid.save(fluidInputs));
        nbtRecipe.put(NBT_RECIPE_FLUID_INPUT_AMOUNT, new IntArrayNBT(fluidInputAmounts));
        nbtRecipe.put(NBT_RECIPE_ITEM_OUTPUT, ECUtils.item.save(outputs));
        nbtRecipe.put(NBT_RECIPE_FLUID_OUTPUT, ECUtils.fluid.save(fluidOutputs));
        nbtRecipe.put(NBT_RECIPE_FLUID_OUTPUT_AMOUNT, new IntArrayNBT(fluidOutputAmounts));
        nbtRecipe.putInt(NBT_RECIPE_ENERGY, energy);
        return nbtRecipe;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.inputs = !nbt.contains(NBT_RECIPE_ITEM_INPUT) ? Collections.emptyList()
                : ECUtils.item.read(nbt.getList(NBT_RECIPE_ITEM_INPUT, Constants.NBT.TAG_COMPOUND));
        this.outputs = !nbt.contains(NBT_RECIPE_ITEM_OUTPUT) ? Collections.emptyList()
                : ECUtils.item.read(nbt.getList(NBT_RECIPE_ITEM_OUTPUT, Constants.NBT.TAG_COMPOUND));
        this.fluidInputs = !nbt.contains(NBT_RECIPE_FLUID_INPUT) ? Collections.emptyList()
                : ECUtils.fluid.read(nbt.getList(NBT_RECIPE_FLUID_INPUT, Constants.NBT.TAG_COMPOUND));
        this.fluidOutputs = !nbt.contains(NBT_RECIPE_FLUID_OUTPUT) ? Collections.emptyList()
                : ECUtils.fluid.read(nbt.getList(NBT_RECIPE_FLUID_OUTPUT, Constants.NBT.TAG_COMPOUND));
        this.fluidInputAmounts = !nbt.contains(NBT_RECIPE_FLUID_INPUT_AMOUNT) ? ECUtils.array.newArray(fluidInputs.size(), 0)
                : nbt.getIntArray(NBT_RECIPE_FLUID_INPUT_AMOUNT);
        this.fluidOutputAmounts = !nbt.contains(NBT_RECIPE_FLUID_OUTPUT_AMOUNT) ? ECUtils.array.newArray(fluidOutputs.size(), 0)
                : nbt.getIntArray(NBT_RECIPE_FLUID_OUTPUT_AMOUNT);
        this.energy = nbt.getInt(NBT_RECIPE_ENERGY);
    }
}
