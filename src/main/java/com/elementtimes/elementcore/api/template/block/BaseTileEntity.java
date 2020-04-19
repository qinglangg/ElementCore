package com.elementtimes.elementcore.api.template.block;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;
import com.elementtimes.elementcore.api.interfaces.block.IMachineRecipe;
import com.elementtimes.elementcore.api.interfaces.block.IMachineTickable;
import com.elementtimes.elementcore.api.interfaces.block.ITileHandler;
import com.elementtimes.elementcore.api.interfaces.gui.IGuiProvider;
import com.elementtimes.elementcore.api.misc.BaseTeNetworkLifecycle;
import com.elementtimes.elementcore.api.recipe.MachineRecipeCapture;
import com.elementtimes.elementcore.api.recipe.MachineRecipeHandler;
import com.elementtimes.elementcore.api.template.capability.ProxyEnergyHandler;
import com.elementtimes.elementcore.api.template.capability.ProxyItemHandler;
import com.elementtimes.elementcore.api.template.capability.ProxyTankHandler;
import com.elementtimes.elementcore.api.template.capability.SideHandlerType;
import com.elementtimes.elementcore.api.template.lifecycle.RecipeMachineLifecycle;
import com.elementtimes.elementcore.api.utils.FluidUtils;
import com.elementtimes.elementcore.api.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 对所有机器的抽象
 * 大量细节被我隐藏在接口中
 * @author KSGFK create in 2019/3/9
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseTileEntity extends TileEntity implements
        ITickableTileEntity, IMachineTickable, IMachineRecipe, ITileHandler.All, IGuiProvider {
    public static final ItemStackHandler EMPTY = new ItemStackHandler(0);
    public static final String BIND_TILE_ENTITY_EXTRA = "_nbt_te_extra_";
    protected ProxyEnergyHandler mEnergyHandler;
    protected ProxyTankHandler mTankHandler;
    protected ProxyItemHandler mItemHandler;
    protected MachineRecipeHandler mRecipe;
    protected Set<IMachineLifecycle> mMachineLifeCycles = new LinkedHashSet<>();
    protected MachineRecipeCapture mWorkingRecipe = null;
    protected boolean isWorking = false;
    protected boolean isPause = false;
    protected int mEnergyProcessed = 0;
    protected int mEnergyUnprocessed = 0;
    protected IntSet mFluidSlot = new IntOpenHashSet();
    protected List<ServerPlayerEntity> mPlayers = new ArrayList<>(5);
    public CompoundNBT bindNbt = new CompoundNBT();

    public BaseTileEntity(TileEntityType type, int energyCapacity,
                          int inputCount, int outputCount,
                          int fluidInput, int fluidOutput, int fluidCapacity,
                          int recipeItemInput, int recipeItemOutput, int recipeFluidInput, int recipeFluidOutput) {
        super(type);
        onCreate();
        mEnergyHandler = new ProxyEnergyHandler(energyCapacity, Integer.MAX_VALUE, Integer.MAX_VALUE);
        mItemHandler = new ProxyItemHandler(inputCount + outputCount);
        mItemHandler.setInsertValid(this::isInputValid);
        mItemHandler.setExtractValid((a, b, c) -> true);
        mItemHandler.setCanInsert(inputCount, inputCount + outputCount, false);
        mItemHandler.setCanExtract(0, inputCount, false);
        mTankHandler = new ProxyTankHandler(fluidInput + fluidOutput, fluidCapacity, this::isFillValid, (a, b, c) -> true);
        mTankHandler.setCanDrain(0, fluidInput, false);
        mTankHandler.setCanFill(fluidInput, fluidInput + fluidOutput, false);
        mRecipe = new MachineRecipeHandler(recipeItemInput, recipeItemOutput, recipeFluidInput, recipeFluidOutput);
        addLifeCycle(new RecipeMachineLifecycle(this));
        addLifeCycle(new BaseTeNetworkLifecycle(this));
        applyConfig();
    }

    protected void onCreate() {}

    // lifecycles
    @Override
    public Set<IMachineLifecycle> getAllLifecycles() {
        return mMachineLifeCycles;
    }
    @Override
    public void setWorking(boolean working) {
        isWorking = working;
    }
    @Override
    public boolean isWorking() {
        return isWorking;
    }
    @Override
    public void setPause(boolean pause) {
        isPause = pause;
    }
    @Override
    public boolean isPause() {
        return isPause;
    }
    @Override
    public int getEnergyProcessed() {
        return mEnergyProcessed;
    }
    @Override
    public void setEnergyProcessed(int energyProcessed) {
        mEnergyProcessed = energyProcessed;
    }
    @Override
    public int getEnergyUnprocessed() {
        return mEnergyUnprocessed;
    }
    @Override
    public void setEnergyUnprocessed(int energyUnprocessed) {
        mEnergyUnprocessed = energyUnprocessed;
    }
    @Override
    public void interrupt() {
        setWorkingRecipe(null);
        setEnergyUnprocessed(0);
        setEnergyProcessed(0);
        setWorking(false);
        setPause(false);
    }
    @Override
    public void tick() {
        update(this);
    }

    // recipes
    @Nullable
    @Override
    public MachineRecipeCapture getWorkingRecipe() {
        return mWorkingRecipe;
    }
    @Override
    public void setWorkingRecipe(MachineRecipeCapture workingRecipe) {
        mWorkingRecipe = workingRecipe;
    }
    @Override
    public MachineRecipeHandler getRecipes() {
        return mRecipe;
    }

    // item
    @Override
    public ProxyItemHandler getItemHandler() {
        return mItemHandler;
    }
    @Override
    public boolean isInputValid(int slot, int count, ItemStack stack) {
        IItemHandler itemHandler = getItemHandler(SideHandlerType.INPUT);
        if (mFluidSlot.contains(slot)) {
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).isPresent();
        }
        List<ItemStack> list = ItemUtils.toList(itemHandler);
        ItemStack backup = list.get(slot);
        list.set(slot, stack);
        boolean valid = getRecipes().acceptInput(list, FluidUtils.toList(getTanks(SideHandlerType.INPUT)));
        list.set(slot, backup);
        return valid;
    }

    // tanks
    @Override
    public ProxyTankHandler getTanks() {
        return mTankHandler;
    }
    @Override
    public boolean isFillValid(int slot, int amount, FluidStack fluidStack) {
        List<FluidStack> fluids = FluidUtils.toList(getTanks(SideHandlerType.INPUT));
        fluids.set(slot, fluidStack);
        return getRecipes().acceptInput(ItemUtils.toList(getItemHandler(SideHandlerType.INPUT)), fluids);
    }

    // energy
    @Override
    public ProxyEnergyHandler getEnergyHandler() {
        return mEnergyHandler;
    }

    // gui
    @Override
    public List<ServerPlayerEntity> getOpenedPlayers() {
        return mPlayers;
    }
    @Override
    public float getProcess() {
        int total = getEnergyUnprocessed() + getEnergyProcessed();
        return total == 0 ? 0f : ((float) getEnergyProcessed()) / total;
    }
    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public void read(CompoundNBT nbt) {
        ITileHandler.All.super.read(nbt);
        IMachineTickable.super.read(nbt);
        IMachineRecipe.super.read(nbt);
        if (nbt.contains(BIND_TILE_ENTITY_EXTRA)) {
            bindNbt = nbt.getCompound(BIND_TILE_ENTITY_EXTRA);
        }
        super.read(nbt);
    }
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        ITileHandler.All.super.write(nbt);
        IMachineTickable.super.write(nbt);
        IMachineRecipe.super.write(nbt);
        if (bindNbt.size() > 0) {
            nbt.put(BIND_TILE_ENTITY_EXTRA, bindNbt);
        }
        return nbt;
    }
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ITileHandler.All.super.getCapability(cap, side);
    }

    protected void applyConfig() {}
}
