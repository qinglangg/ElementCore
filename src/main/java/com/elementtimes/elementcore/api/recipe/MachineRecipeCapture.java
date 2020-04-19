package com.elementtimes.elementcore.api.recipe;

import com.elementtimes.elementcore.api.utils.FluidUtils;
import com.elementtimes.elementcore.api.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
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
public class MachineRecipeCapture implements INBTSerializable<CompoundNBT> {
    public MachineRecipe recipe;
    public NonNullList<ItemStack> inputs;
    public NonNullList<ItemStack> outputs;
    public List<FluidStack> fluidInputs;
    public int[] fluidInputAmounts;
    public List<FluidStack> fluidOutputs;
    public int[] fluidOutputAmounts;
    public int energy;

    private static final String ITEM_INPUT = "ECRII";
    private static final String ITEM_OUTPUT = "ECRIO";
    private static final String FLUID_INPUT = "ECRFI";
    private static final String FLUID_OUTPUT = "ECRFO";
    private static final String FLUID_INPUT_AMOUNT = "ECRFIA";
    private static final String FLUID_OUTPUT_AMOUNT = "ECRFOA";
    private static final String ENERGY = "ECRE";

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
            fluidInputAmounts[i] = fluid == null ? 0 : fluid.getAmount();
        }

        this.fluidOutputs = new ArrayList<>();
        this.fluidOutputAmounts = new int[recipe.fluidOutputs.size()];
        for (int i = 0; i < recipe.fluidOutputs.size(); i++) {
            IngredientPart<FluidStack> part = recipe.fluidOutputs.get(i);
            FluidStack fluid = part.getter.apply(recipe, input, fluids, i, part.probability);
            fluidOutputs.add(i, fluid);
            fluidOutputAmounts[i] = fluid == null ? 0 : fluid.getAmount();
        }

        this.energy = recipe.energy.applyAsInt(this);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbtRecipe = new CompoundNBT();
        nbtRecipe.put(ITEM_INPUT, ItemUtils.saveAllItems(inputs));
        nbtRecipe.put(FLUID_INPUT, FluidUtils.saveToNbt(fluidInputs));
        nbtRecipe.put(FLUID_INPUT_AMOUNT, new IntArrayNBT(fluidInputAmounts));
        nbtRecipe.put(ITEM_OUTPUT, ItemUtils.saveAllItems(outputs));
        nbtRecipe.put(FLUID_OUTPUT, FluidUtils.saveToNbt(fluidOutputs));
        nbtRecipe.put(FLUID_OUTPUT_AMOUNT, new IntArrayNBT(fluidOutputAmounts));
        nbtRecipe.putInt(ENERGY, energy);
        return nbtRecipe;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        inputs = !nbt.contains(ITEM_INPUT) ? NonNullList.create() : ItemUtils.loadAllItems(nbt.getList(ITEM_INPUT, Constants.NBT.TAG_COMPOUND));
        outputs = !nbt.contains(ITEM_OUTPUT) ? NonNullList.create() : ItemUtils.loadAllItems(nbt.getList(ITEM_OUTPUT, Constants.NBT.TAG_COMPOUND));
        fluidInputs = !nbt.contains(FLUID_INPUT) ? Collections.emptyList() : FluidUtils.readFromNbt(nbt.getList(FLUID_INPUT, Constants.NBT.TAG_COMPOUND));
        fluidOutputs = !nbt.contains(FLUID_OUTPUT) ? Collections.emptyList() : FluidUtils.readFromNbt(nbt.getList(FLUID_OUTPUT, Constants.NBT.TAG_COMPOUND));
        fluidInputAmounts = !nbt.contains(FLUID_INPUT_AMOUNT) ? new int[fluidInputs.size()] : nbt.getIntArray(FLUID_INPUT_AMOUNT);
        fluidOutputAmounts = !nbt.contains(FLUID_OUTPUT_AMOUNT) ? new int[fluidOutputs.size()] : nbt.getIntArray(FLUID_OUTPUT_AMOUNT);
        energy = nbt.getInt(ENERGY);
    }
}
