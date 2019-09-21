package com.elementtimes.elementcore.api.template.tileentity.lifecycle;

import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.ITankHandler;
import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandler;
import com.elementtimes.elementcore.api.template.tileentity.BaseTileEntity;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeCapture;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 机器生命周期方法，用于配合 MachineRecipe 进行机器合成
 * @author luqin2007
 */
public class RecipeMachineLifecycle implements IMachineLifecycle {

    private final BaseTileEntity machine;
    private boolean needBind = true;

    private MachineRecipeCapture recipe;
    private final IItemHandler inputItems;
    private final ITankHandler inputTanks;
    private final IItemHandler outputItems;
    private final ITankHandler outputTanks;
    private final Int2IntMap mBindInputToOutputMap = new Int2IntOpenHashMap();

    public RecipeMachineLifecycle(final BaseTileEntity machine) {
        this.machine = machine;
        inputItems = machine.getItemHandler(SideHandlerType.INPUT);
        inputTanks = machine.getTanks(SideHandlerType.INPUT);
        outputItems = machine.getItemHandler(SideHandlerType.OUTPUT);
        outputTanks = machine.getTanks(SideHandlerType.OUTPUT);
        if (inputItems instanceof ItemHandler) {
            ((ItemHandler) inputItems).onItemChangeListener.add(slot -> {
                if (mBindInputToOutputMap.containsKey(slot)) {
                    ItemStack input = inputItems.getStackInSlot(slot);
                    outputItems.setStackInSlot(mBindInputToOutputMap.get(slot), input.copy());
                }
            });
        }
        if (outputItems instanceof ItemHandler) {
            ((ItemHandler) outputItems).onItemChangeListener.add(slot -> {
                if (mBindInputToOutputMap.containsValue(slot)) {
                    ItemStack output = outputItems.getStackInSlot(slot);
                    mBindInputToOutputMap.entrySet().stream()
                            .filter(kv -> kv.getValue().equals(slot))
                            .findFirst()
                            .map(Map.Entry::getKey)
                            .ifPresent(slotInput -> {
                                if (output.isEmpty() || inputItems.isItemValid(slotInput, output)) {
                                    mBindInputToOutputMap.remove(slotInput);
                                    inputItems.setSlotIgnoreChangeListener(slotInput, output);
                                }
                            });
                }
            });
            ((ItemHandler) outputItems).onUnbindAllListener.add(itemStacks -> {
                mBindInputToOutputMap.clear();
                needBind = true;
            });
        }
    }

    @Override
    public void onTickStart() {
        if (needBind) {
            for (int i = 0; i < inputItems.getSlots(); i++) {
                ItemStack is = inputItems.getStackInSlot(i);
                if (!is.isEmpty() && !inputItems.isItemValid(i, is)) {
                    bind(i);
                }
            }
            needBind = false;
        }
    }

    @Override
    public boolean onCheckStart() {
        // 合成表
        recipe = getNextRecipe(inputItems, inputTanks);
        if (isRecipeCanWork(recipe, inputItems, inputTanks)) {
            // 能量
            assert recipe != null;
            int change = recipe.energy;
            if (change > 0) {
                change = Math.min(change, machine.getEnergyTick());
                return machine.getEnergyHandler().extractEnergy(change, true) >= change;
            } else if (change < 0) {
                change = Math.min(-change, machine.getEnergyTick());
                return machine.getEnergyHandler().receiveEnergy(change, true) > 0;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        outputItems.unbindAll();
        needBind = false;
        assert recipe != null;
        machine.setWorkingRecipe(recipe);
        machine.setEnergyUnprocessed(recipe.energy);
        // items
        for (int i = recipe.inputs.size() - 1; i >= 0; i--) {
            ItemStack input = recipe.inputs.get(i);
            inputItems.extractItem(i, input.getCount(), false);
            if (input.getItem().hasContainerItem(input)) {
                inputItems.insertItemIgnoreValid(i, input.getItem().getContainerItem(input), false);
                bind(i);
            }
        }
    }

    @Override
    public boolean onLoop() {
        assert recipe != null;
        int unprocessed = machine.getEnergyUnprocessed();

        int change = 0;
        EnergyHandler handler = machine.getEnergyHandler();
        if (unprocessed > 0) {
            // 耗能过程
            change = Math.min(machine.getEnergyTick(), unprocessed);
            if (handler.extractEnergy(change, true) < change) {
                return false;
            }
            handler.extractEnergy(change, false);
            machine.processEnergy(change);
        } else if (unprocessed < 0) {
            // 产能过程
            change = Math.min(machine.getEnergyTick(), -unprocessed);
            change = handler.receiveEnergy(change, false);
            machine.processEnergy(change);
        }

        // 流体消耗/产出
        float a = (float) change / (float) Math.abs(machine.getEnergyProcessed() + machine.getEnergyUnprocessed());
        if (fluid(true, a)) {
            fluid(false, a);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCheckFinish() {
        return machine.getEnergyUnprocessed() == 0;
    }

    @Override
    public boolean onCheckResume() {
        recipe = machine.getWorkingRecipe();
        assert recipe != null;
        int change = Math.min(machine.getEnergyTick(), Math.abs(machine.getEnergyUnprocessed()));

        float a = (float) (Math.abs(machine.getEnergyProcessed()) + change) / (float) Math.abs(machine.getEnergyProcessed() + machine.getEnergyUnprocessed());
        if (machine.getWorkingRecipe().energy > 0) {
            return machine.getEnergyHandler().extractEnergy(change, true) >= change && fluid(true, a);
        }
		return machine.getEnergyHandler().receiveEnergy(change, true) > 0 && fluid(true, a);
    }

    @Override
    public boolean onFinish() {
        assert recipe != null;
        boolean output = output(true);
        if (output) {
            output(false);
        }
        return output;
    }

    private boolean output(boolean simulate) {
        int itemCount = recipe.outputs.size();
        boolean pushAll = true;
        // item
        for (int i = 0; pushAll && i < itemCount; i++) {
            ItemStack left = recipe.outputs.get(i);
            // 已有物品
            for (int j = 0; j < outputItems.getSlots(); j++) {
                ItemStack slot = outputItems.getStackInSlot(j);
                if (!slot.isEmpty() && slot.isItemEqual(left)) {
                    left = outputItems.insertItemIgnoreValid(j, left, simulate);
                }
            }
            // 空槽位
            for (int j = 0; j < outputItems.getSlots(); j++) {
                ItemStack slot = outputItems.getStackInSlot(j);
                if (slot.isEmpty()) {
                    left = outputItems.insertItemIgnoreValid(j, left, simulate);
                }
            }
            if (simulate) {
                pushAll = left.isEmpty();
            }
        }
        return pushAll;
    }

    private boolean fluid(boolean simulate, float a) {
        int max = Math.max(recipe.fluidInputs.size(), recipe.fluidOutputs.size());
        for (int i = 0; i < max; i++) {
            if (recipe.fluidInputs.size() > i) {
                FluidStack fluid = recipe.fluidInputs.get(i);
                if (fluid != null && fluid.amount > 0) {
                    int amountInput = recipe.fluidInputAmounts[i];
                    if (amountInput > 0) {
                        int amount;
                        if (machine.getEnergyUnprocessed() == 0) {
                            amount = amountInput;
                        } else {
                            float amountFloat = fluid.amount * a;
                            amount = (int) amountFloat;
                            if (amount == 0) {
                                amount++;
                            }
                        }
                        amount = Math.min(amount, amountInput);
                        if (amount > 0) {
                            if (simulate) {
                                FluidStack drain = inputTanks.drainIgnoreCheck(i, new FluidStack(fluid, amount), false);
                                if (drain == null || drain.amount < amount) {
                                    return false;
                                }
                            } else {
                                FluidStack stack = inputTanks.drainIgnoreCheck(i, new FluidStack(fluid, amount), true);
                                if (stack != null) {
                                    recipe.fluidInputAmounts[i] -= stack.amount;
                                }
                            }
                        }
                    }
                }
            }

            if (recipe.fluidOutputs.size() > i) {
                final FluidStack fluid = recipe.fluidOutputs.get(i);
                if (fluid != null && fluid.amount > 0) {
                    int amountOutput = recipe.fluidOutputAmounts[i];
                    if (amountOutput > 0) {
                        float amountFloat = fluid.amount * a;
                        int amount = (int) amountFloat;
                        if (amount == 0 || amountFloat - amount > 0) {
                            amount++;
                        }
                        amount = Math.min(amount, amountOutput);
                        if (amount > 0) {
                            if (simulate) {
                                int fill = outputTanks.fillIgnoreCheck(i, new FluidStack(fluid, amount), false);
                                if (fill < amount) {
                                    return false;
                                }
                            } else {
                                int amountFill = outputTanks.fillIgnoreCheck(i, new FluidStack(fluid.getFluid(), amount), true);
                                if (amountFill > 0) {
                                    recipe.fluidOutputAmounts[i] -= amountFill;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void bind(int slotInput) {
        ItemStack input = inputItems.getStackInSlot(slotInput).copy();
        int bind = outputItems.bind(input);
        mBindInputToOutputMap.put(slotInput, bind);
    }

    /**
     * 检查当前机器条件是否可以进行对应合成表的合成
     * 通常包括是否具有此合成表，输入物体/流体/能量是否足够
     *
     * @return 通常意味着可以进行下一次合成。
     */
    protected boolean isRecipeCanWork(@Nullable MachineRecipeCapture recipeCapture, net.minecraftforge.items.IItemHandler itemHandler, ITankHandler tankHandler) {
        if (recipeCapture == null) {
            return false;
        }
        if (itemHandler.getSlots() < recipeCapture.inputs.size()
                || tankHandler.size() < recipeCapture.fluidInputs.size()) {
            return false;
        }

        // items
        for (int i = 0; i < recipeCapture.inputs.size(); i++) {
            ItemStack item = recipeCapture.inputs.get(i);
            if (!item.isEmpty()) {
                ItemStack extract = itemHandler.extractItem(i, item.getCount(), true);
                if (!item.isItemEqual(extract) || item.getCount() > extract.getCount()) {
                    return false;
                }
            } else {
                return ECUtils.item.isItemRawEqual(item, itemHandler.getStackInSlot(i));
            }
        }

        // fluids
        for (int i = 0; i < recipeCapture.fluidInputs.size(); i++) {
            FluidStack fluid = recipeCapture.fluidInputs.get(i);
            FluidStack drain = tankHandler.drainIgnoreCheck(i, fluid, false);
            if (!fluid.isFluidEqual(drain) || fluid.amount > drain.amount) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据所在上下文获取可行的下一个合成表
     * 通常这里只检查输入量是否足够
     * @param input 输入物品
     * @param tankHandler 输入流体
     * @return 匹配的合成表
     */
    @Nullable
    protected MachineRecipeCapture getNextRecipe(IItemHandler input, ITankHandler tankHandler) {
        List<ItemStack> items = ECUtils.item.toList(input, machine.getRecipeSlotIgnore());
        List<FluidStack> fluids = ECUtils.fluid.toListNotNull(tankHandler);
        MachineRecipeCapture[] captures = machine.getRecipes().matchInput(items, fluids);
        if (captures.length == 0) {
            return null;
        }
        return captures[0];
    }
}
