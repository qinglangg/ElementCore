package com.elementtimes.elementcore.api.template.tileentity.recipe;

import com.elementtimes.elementcore.api.template.interfaces.Function5;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * 用于辅助构建合成配方
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MachineRecipeBuilder {
    private ToIntFunction<MachineRecipeCapture> cost = (a) -> 0;
    private List<IngredientPart<ItemStack>> inputItems;
    private List<IngredientPart<FluidStack>> inputFluids;
    private List<IngredientPart<ItemStack>> outputItems;
    private List<IngredientPart<FluidStack>> outputFluids;
    private int ptrII = 0, ptrIO = 0, ptrFI = 0, ptrFO = 0;
    private MachineRecipeHandler handler;

    public MachineRecipeBuilder(MachineRecipeHandler handler) {
        this.handler = handler;
        inputItems = NonNullList.withSize(handler.inputItemCount, IngredientPart.EMPTY_ITEM);
        inputFluids = NonNullList.withSize(handler.inputFluidCount, IngredientPart.EMPTY_FLUID);
        outputItems = NonNullList.withSize(handler.outputItemCount, IngredientPart.EMPTY_ITEM);
        outputFluids = NonNullList.withSize(handler.outputFluidCount, IngredientPart.EMPTY_FLUID);
    }

    /**
     * 增加耗能
     * @param energyCost 能量消耗
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder addCost(int energyCost) {
        ToIntFunction<MachineRecipeCapture> c = cost;
        cost = (a) -> c.applyAsInt(a) + energyCost;
        return this;
    }

    /**
     * 增加耗能
     * @param energyCost 能量消耗
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder addCost(ToIntFunction<MachineRecipeCapture> energyCost) {
        ToIntFunction<MachineRecipeCapture> c = cost;
        cost = (a) -> c.applyAsInt(a) + energyCost.applyAsInt(a);
        return this;
    }

    /**
     * 按顺序添加输入
     * @param item 输入物品
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder addItemInput(IngredientPart<ItemStack> item) {
        if (ptrII < handler.inputItemCount) {
            inputItems.set(ptrII, item);
            ptrII++;
        } else {
            throw new RuntimeException("合成配方物品输入已满");
        }
        return this;
    }

    /**
     * 按顺序添加多个输入
     * @param items 输入物品
     * @return MachineRecipeBuilder
     */
    @SafeVarargs
    public final MachineRecipeBuilder addItemInputs(IngredientPart<ItemStack>... items) {
        int ptr = 0;
        while (ptrII < handler.inputItemCount && ptr < items.length) {
            inputItems.set(ptrII, items[ptr]);
            ptrII++;
            ptr++;
        }
        return this;
    }

    /**
     * 按顺序添加输出
     * @param item 输出物品
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder addItemOutput(IngredientPart<ItemStack> item) {
        if (ptrIO < handler.outputItemCount) {
            outputItems.set(ptrIO, item);
            ptrIO++;
        } else {
            throw new RuntimeException("合成配方物品输出已满");
        }
        return this;
    }

    /**
     * 按顺序添加多个输出
     * @param items 输出物品
     * @return MachineRecipeBuilder
     */
    @SafeVarargs
    public final MachineRecipeBuilder addItemOutputs(IngredientPart<ItemStack>... items) {
        int ptr = 0;
        while (ptrIO < handler.outputItemCount && ptr < items.length) {
            outputItems.set(ptrIO, items[ptr]);
            ptrIO++;
            ptr++;
        }
        return this;
    }

    /**
     * 动态匹配物品输入
     * @param inputCheck 检查输入物品是否符合要求
     * @param inputConvert 根据输入物品获取实际输入物品种类及数量
     * @param allInputValues 所有可能的输入，用于jei
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder addItemInput(Predicate<ItemStack> inputCheck, Function<ItemStack, ItemStack> inputConvert, List<ItemStack> allInputValues) {
        return addItemInput(new IngredientPart<>(
                (recipe, slot, inputItems, inputFluids, input) -> inputCheck.test(input),
                (recipe, slot, inputItems, inputFluids, input) -> inputCheck.test(input),
                (recipe, input, fluids, i, probability) ->
                        IngredientPart.RAND.nextFloat() > probability ? ItemStack.EMPTY : inputConvert.apply(input.get(i)),
                () -> allInputValues));
    }

    /**
     * 动态匹配物品输出
     * @param outputGetter 根据合成表获取输出
     * @param allInputValues 所有可能的输出，用于jei
     * @return MachineRecipeBuilder
     */
    public MachineRecipeBuilder addItemOutput(Function5.StackGetter<ItemStack> outputGetter, List<ItemStack> allInputValues) {
        return addItemOutput(new IngredientPart<>(
                (recipe, slot, inputItems, inputFluids, input) -> true,
                (recipe, slot, inputItems, inputFluids, input) -> false,
                outputGetter,
                () -> allInputValues));
    }

    public MachineRecipeBuilder addFluidOutput(IngredientPart<FluidStack> fluid) {
        if (ptrFO < handler.outputFluidCount) {
            outputFluids.set(ptrFO, fluid);
            ptrFO++;
        } else {
            throw new RuntimeException("合成配方流体输入已满");
        }
        return this;
    }

    public MachineRecipeBuilder addFluidInput(IngredientPart<FluidStack> fluid) {
        if (ptrFI < handler.inputFluidCount) {
            inputFluids.set(ptrFI, fluid);
            ptrFI++;
        } else {
            throw new RuntimeException("合成配方流体输入已满");
        }
        return this;
    }

    /**
     * 创建并添加配方
     * @return MachineRecipeHandler
     */
    public MachineRecipeHandler endAdd() {
        MachineRecipe recipe = new MachineRecipe();
        recipe.energy = cost;
        recipe.inputs = inputItems;
        recipe.fluidInputs = inputFluids;
        recipe.outputs = outputItems;
        recipe.fluidOutputs = outputFluids;
        handler.getMachineRecipes().add(recipe);
        return handler;
    }
}