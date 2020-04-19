package com.elementtimes.elementcore.api.template.lifecycle;

import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;
import com.elementtimes.elementcore.api.recipe.MachineRecipeCapture;
import com.elementtimes.elementcore.api.template.block.BaseTileEntity;
import com.elementtimes.elementcore.api.template.capability.ProxyEnergyHandler;
import com.elementtimes.elementcore.api.template.capability.ProxyItemHandler;
import com.elementtimes.elementcore.api.template.capability.ProxyTankHandler;
import com.elementtimes.elementcore.api.template.capability.SideHandlerType;
import com.elementtimes.elementcore.api.utils.FluidUtils;
import com.elementtimes.elementcore.api.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 机器生命周期方法，用于配合 MachineRecipe 进行机器合成
 * @author luqin2007
 */
public class RecipeMachineLifecycle implements IMachineLifecycle {

    private final BaseTileEntity mMachine;
    private MachineRecipeCapture recipe;
    private IEnergyStorage mEnergyStorage = null;
    private ProxyItemHandler.Proxy inputItems, outputItems;
    private ProxyTankHandler.Proxy inputTanks, outputTanks;

    public RecipeMachineLifecycle(BaseTileEntity machine) {
        this.mMachine = machine;
        inputItems = machine.getItemHandler(SideHandlerType.INPUT);
        outputItems = machine.getItemHandler(SideHandlerType.OUTPUT);
        inputTanks = machine.getTanks(SideHandlerType.INPUT);
        outputTanks = machine.getTanks(SideHandlerType.OUTPUT);
    }

    @Override
    public boolean onCheckStart() {
        // 合成表
        recipe = getNextRecipe();
        if (isRecipeCanWork(recipe)) {
            // 能量
            assert recipe != null;
            int change = recipe.energy;
            IEnergyStorage handler = getEnergyHandler();
            if (handler == null && change != 0) {
                return false;
            }
            if (change > 0) {
                change = Math.min(change, mMachine.getEnergyTick());
                return handler.extractEnergy(change, true) >= change;
            } else if (change < 0) {
                change = Math.min(-change, mMachine.getEnergyTick());
                return handler.receiveEnergy(change, true) > 0;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        assert recipe != null;
        mMachine.setWorkingRecipe(recipe);
        mMachine.setEnergyUnprocessed(recipe.energy);
        // items
        for (int i = recipe.inputs.size() - 1; i >= 0; i--) {
            ItemStack input = recipe.inputs.get(i);
            inputItems.extractItem(i, input.getCount(), false);
            if (input.getItem().hasContainerItem(input)) {
                inputItems.insertItemIgnoreValid2(i, input.getItem().getContainerItem(input), false);
            }
        }
    }

    @Override
    public boolean onLoop() {
        assert recipe != null;
        int unprocessed = mMachine.getEnergyUnprocessed();

        int change = 0;
        ProxyEnergyHandler handler = mMachine.getEnergyHandler();
        if (unprocessed > 0) {
            // 耗能过程
            change = Math.min(mMachine.getEnergyTick(), unprocessed);
            if (handler.extractEnergy(change, true) < change) {
                return false;
            }
            handler.extractEnergy(change, false);
            mMachine.processEnergy(change);
        } else if (unprocessed < 0) {
            // 产能过程
            change = Math.min(mMachine.getEnergyTick(), -unprocessed);
            change = handler.receiveEnergy(change, false);
            mMachine.processEnergy(change);
        }

        // 流体消耗/产出
        float a = (float) change / (float) Math.abs(mMachine.getEnergyProcessed() + mMachine.getEnergyUnprocessed());
        if (fluid(true, a)) {
            fluid(false, a);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCheckFinish() {
        return mMachine.getEnergyUnprocessed() <= 0;
    }

    @Override
    public boolean onCheckResume() {
        recipe = mMachine.getWorkingRecipe();
        assert recipe != null;
        int change = Math.min(mMachine.getEnergyTick(), Math.abs(mMachine.getEnergyUnprocessed()));

        float a = (float) (Math.abs(mMachine.getEnergyProcessed()) + change) / (float) Math.abs(mMachine.getEnergyProcessed() + mMachine.getEnergyUnprocessed());
        if (mMachine.getWorkingRecipe().energy > 0) {
            return mMachine.getEnergyHandler().extractEnergy(change, true) >= change && fluid(true, a);
        }
		return mMachine.getEnergyHandler().receiveEnergy(change, true) > 0 && fluid(true, a);
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
                    left = outputItems.insertItemIgnoreValid2(j, left, simulate);
                }
            }
            // 空槽位
            for (int j = 0; j < outputItems.getSlots(); j++) {
                ItemStack slot = outputItems.getStackInSlot(j);
                if (slot.isEmpty()) {
                    left = outputItems.insertItemIgnoreValid2(j, left, simulate);
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
                if (fluid != null && fluid.getAmount() > 0) {
                    int amountInput = recipe.fluidInputAmounts[i];
                    if (amountInput > 0) {
                        int amount;
                        if (mMachine.getEnergyUnprocessed() == 0) {
                            amount = amountInput;
                        } else {
                            float amountFloat = fluid.getAmount() * a;
                            amount = (int) amountFloat;
                            if (amount == 0) {
                                amount++;
                            }
                        }
                        amount = Math.min(amount, amountInput);
                        if (amount > 0) {
                            if (simulate) {
                                FluidStack drain = inputTanks.drainIgnoreCheck2(i, new FluidStack(fluid, amount), IFluidHandler.FluidAction.SIMULATE);
                                if (drain.getAmount() < amount) {
                                    return false;
                                }
                            } else {
                                FluidStack stack = inputTanks.drainIgnoreCheck2(i, new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
                                recipe.fluidInputAmounts[i] -= stack.getAmount();
                            }
                        }
                    }
                }
            }

            if (recipe.fluidOutputs.size() > i) {
                final FluidStack fluid = recipe.fluidOutputs.get(i);
                if (fluid != null && fluid.getAmount() > 0) {
                    int amountOutput = recipe.fluidOutputAmounts[i];
                    if (amountOutput > 0) {
                        float amountFloat = fluid.getAmount() * a;
                        int amount = (int) amountFloat;
                        if (amount == 0 || amountFloat - amount > 0) {
                            amount++;
                        }
                        amount = Math.min(amount, amountOutput);
                        if (amount > 0) {
                            if (simulate) {
                                int fill = outputTanks.fillIgnoreCheck(i, new FluidStack(fluid, amount), IFluidHandler.FluidAction.SIMULATE);
                                if (fill < amount) {
                                    return false;
                                }
                            } else {
                                int amountFill = outputTanks.fillIgnoreCheck(i, new FluidStack(fluid.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
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

    protected void saveItem(int slot, ItemStack stack) {
        CompoundNBT nbt;
        if (mMachine.bindNbt.contains("ECRMLI", Constants.NBT.TAG_COMPOUND)) {
            nbt = mMachine.bindNbt.getCompound("ECRMLI");
        } else if (!stack.isEmpty()) {
            nbt = new CompoundNBT();
            mMachine.bindNbt.put("ECRMLI", nbt);
        } else {
            return;
        }
        if (stack.isEmpty()) {
            nbt.remove(slot + "");
        } else {
            nbt.put(slot + "", stack.write(new CompoundNBT()));
        }
        mMachine.markDirty();
    }
    
    protected ItemStack loadItem(int slot) {
        if (mMachine.bindNbt.contains("ECRMLI", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT nbt = mMachine.bindNbt.getCompound("ECRMLI");
            String key = slot + "";
            if (nbt.contains(key, Constants.NBT.TAG_COMPOUND)) {
                ItemStack stack = ItemStack.read(nbt.getCompound(key));
                nbt.remove(key);
                mMachine.markDirty();
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    
    protected IEnergyStorage getEnergyHandler() {
        if (mEnergyStorage == null) {
            mEnergyStorage = mMachine.getEnergyHandler();
        }
        return mEnergyStorage;
    }

    /**
     * 检查当前机器条件是否可以进行对应合成表的合成
     * 通常包括是否具有此合成表，输入物体/流体/能量是否足够
     *
     * @return 通常意味着可以进行下一次合成。
     */
    protected boolean isRecipeCanWork(@Nullable MachineRecipeCapture recipeCapture) {
        if (recipeCapture == null) {
            return false;
        }
        if (inputItems.getSlots() < recipeCapture.inputs.size()
                || inputTanks.getTanks() < recipeCapture.fluidInputs.size()) {
            return false;
        }

        // items
        for (int i = 0; i < recipeCapture.inputs.size(); i++) {
            ItemStack item = recipeCapture.inputs.get(i);
            if (!item.isEmpty()) {
                ItemStack extract = inputItems.extractItem(i, item.getCount(), true);
                if (!item.isItemEqual(extract) || item.getCount() > extract.getCount()) {
                    return false;
                }
            } else {
                return ItemUtils.isItemRawEqual(item, inputItems.getStackInSlot(i));
            }
        }

        // fluids
        for (int i = 0; i < recipeCapture.fluidInputs.size(); i++) {
            FluidStack fluid = recipeCapture.fluidInputs.get(i);
            FluidStack drain = inputTanks.drainIgnoreCheck2(i, fluid, IFluidHandler.FluidAction.SIMULATE);
            if (!fluid.isFluidEqual(drain) || fluid.getAmount() > drain.getAmount()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据所在上下文获取可行的下一个合成表
     * 通常这里只检查输入量是否足够
     * @return 匹配的合成表
     */
    @Nullable
    protected MachineRecipeCapture getNextRecipe() {
        List<ItemStack> items = ItemUtils.toList(inputItems);
        List<FluidStack> fluids = FluidUtils.toList(inputTanks);
        MachineRecipeCapture[] captures = mMachine.getRecipes().matchInput(items, fluids);
        if (captures.length == 0) {
            return null;
        }
        return captures[0];
    }
}
