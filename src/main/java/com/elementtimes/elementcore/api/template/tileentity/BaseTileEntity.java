package com.elementtimes.elementcore.api.template.tileentity;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.ITankHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.TankHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.TankHandlerVisitor;
import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandlerVisitor;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.*;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.RecipeMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeCapture;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeHandler;
import com.elementtimes.elementcore.common.network.GuiEnergyNetwork;
import com.elementtimes.elementcore.common.network.GuiFluidNetwork;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
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
        ITickable, IMachineTickable, IMachineRecipe, ITileHandler.All, IGuiProvider {
    public static final ItemStackHandler EMPTY = new ItemStackHandler(0);
    public static final String BIND_TILE_ENTITY_EXTRA = "_nbt_te_extra_";
    protected EnergyHandler mEnergyHandler;
    protected IItemHandler mInputItems;
    protected IItemHandler mOutputItems;
    protected ItemHandlerVisitor mAllItems;
    protected ITankHandler mInputFluids;
    protected ITankHandler mOutputFluids;
    protected TankHandlerVisitor mAllFluids;
    protected MachineRecipeHandler mRecipe;
    protected Set<IMachineLifecycle> mMachineLifeCycles = new LinkedHashSet<>();
    protected MachineRecipeCapture mWorkingRecipe = null;
    protected boolean isWorking = false;
    protected boolean isPause = false;
    protected int mEnergyProcessed = 0;
    protected int mEnergyUnprocessed = 0;
    protected IntSet mIgnoreInputSlot = new IntOpenHashSet();
    protected List<EntityPlayerMP> mPlayers = new ArrayList<>(5);
    public NBTTagCompound bindNbt = new NBTTagCompound();

    public BaseTileEntity(int energyCapacity, int inputCount, int outputCount) {
        this(energyCapacity, inputCount, outputCount, 0, 0, 0, 0);
    }

    public BaseTileEntity(int energyCapacity, int inputCount, int outputCount, int fluidInput, int inputCapacity, int fluidOutput, int outputCapacity) {
        onCreate();
        mEnergyHandler = new EnergyHandler(energyCapacity, Integer.MAX_VALUE, Integer.MAX_VALUE);
        mInputItems = new ItemHandler(inputCount, this::isInputValid);
        mOutputItems = new ItemHandler(outputCount, (integer, itemStack) -> false);
        mAllItems = new ItemHandlerVisitor(mInputItems, mOutputItems);
        mInputFluids = new TankHandler(this::isFillValid, TankHandler.FALSE, fluidInput, inputCapacity);
        mOutputFluids = new TankHandler(TankHandler.FALSE, TankHandler.TRUE, fluidOutput, outputCapacity);
        mAllFluids = new TankHandlerVisitor(mInputFluids, mOutputFluids);
        mRecipe = new MachineRecipeHandler(inputCount, outputCount, fluidInput, fluidOutput);
        addLifeCycle(new RecipeMachineLifecycle(this));
        addLifeCycle(new HandlerInfoMachineLifecycle.Builder(this)
                .withPlayers(this::getOpenedPlayers)
                .withEnergyInfo(this::postEnergy)
                .withFluidInfo(this::postFluid).build());
        applyConfig();
    }

    protected void onCreate() {}

    // network
    private void postEnergy(EntityPlayer player, HandlerInfoMachineLifecycle.EnergyInfo info) {
        if (player instanceof EntityPlayerMP) {
            GuiEnergyNetwork energyNetwork = new GuiEnergyNetwork(getGuiId(), info.capacity, info.stored);
            ElementCore.instance().container.elements.channel.sendTo(energyNetwork, (EntityPlayerMP) player);
        }
    }
    private void postFluid(EntityPlayer player, HandlerInfoMachineLifecycle.FluidInfo info) {
        if (player instanceof EntityPlayerMP) {
            GuiFluidNetwork fluidNetwork = new GuiFluidNetwork();
            fluidNetwork.put(getGuiId(), info);
            ElementCore.instance().container.elements.channel.sendTo(fluidNetwork, (EntityPlayerMP) player);
        }
    }

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
    public void update() {
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
    @Nonnull
    @Override
    public IntSet getRecipeSlotIgnore() {
        return mIgnoreInputSlot;
    }

    @Override
    public EnergyHandler getEnergyHandler() { return mEnergyHandler; }
    @Override
    public SideHandlerType getEnergyType(EnumFacing facing) {
        return SideHandlerType.INPUT;
    }

    // item
    @Nonnull
    @Override
    public IItemHandler getItemHandler(@Nonnull SideHandlerType type) {
        switch (type) {
            case INPUT:
                return mInputItems;
            case OUTPUT:
                return mOutputItems;
            case READONLY:
                return mAllItems.readonly();
            case IN_OUT:
            case ALL:
                return mAllItems;
            default:
                return ItemHandler.EMPTY;
        }
    }
    @Override
    public boolean isInputValid(int slot, ItemStack stack) {
        IItemHandler itemHandler = getItemHandler(SideHandlerType.INPUT);
        if (getRecipeSlotIgnore().contains(slot)) {
            IFluidHandlerItem capability = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            return capability != null || stack.getItem() == Items.GLASS_BOTTLE;
        }
        List<ItemStack> list = ECUtils.item.toList(itemHandler, getRecipeSlotIgnore());
        ItemStack backup = list.get(slot);
        list.set(slot, stack);
        boolean valid = getRecipes().acceptInput(list, ECUtils.fluid.toListNotNull(getTanks(SideHandlerType.INPUT)));
        list.set(slot, backup);
        return valid;
    }
    @Override
    public void setItemHandler(@Nonnull SideHandlerType type, IItemHandler handler) {
        if (type == SideHandlerType.INPUT) {
            mInputItems = handler;
        } else if (type == SideHandlerType.OUTPUT) {
            mOutputItems = handler;
        }
        mAllItems = new ItemHandlerVisitor(mInputItems, mOutputItems);
    }

    // tanks
    @Override
    public ITankHandler getTanks(SideHandlerType type) {
        switch (type) {
            case IN_OUT:
            case ALL:
                return mAllFluids;
            case READONLY:
                return mAllFluids.readonly();
            case INPUT:
                return mInputFluids;
            case OUTPUT:
                return mOutputFluids;
            default:
                return TankHandler.EMPTY;
        }
    }
    @Override
    public void setTanks(SideHandlerType type, ITankHandler handler) {
        if (type == SideHandlerType.INPUT) {
            mInputFluids = handler;
        } else if (type == SideHandlerType.OUTPUT) {
            mOutputFluids = handler;
        }
        mAllFluids = new TankHandlerVisitor(mInputFluids, mOutputFluids);
    }
    @Override
    public boolean isFillValid(int slot, FluidStack fluidStack) {
        List<FluidStack> fluids = ECUtils.fluid.toListNotNull(getTanks(SideHandlerType.INPUT));
        fluids.set(slot, fluidStack);
        return getRecipes().acceptInput(ECUtils.item.toList(getItemHandler(SideHandlerType.INPUT), getRecipeSlotIgnore()), fluids);
    }

    // gui
    @Override
    public List<EntityPlayerMP> getOpenedPlayers() {
        return mPlayers;
    }
    @Override
    public float getProcess() {
        int total = getEnergyUnprocessed() + getEnergyProcessed();
        return total == 0 ? 0f : ((float) getEnergyProcessed()) / total;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        ITileHandler.All.super.deserializeNBT(nbt);
        IMachineTickable.super.deserializeNBT(nbt);
        IMachineRecipe.super.deserializeNBT(nbt);
        if (nbt.hasKey(BIND_TILE_ENTITY_EXTRA)) {
            bindNbt = nbt.getCompoundTag(BIND_TILE_ENTITY_EXTRA);
        }
        super.readFromNBT(nbt);
    }
    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }
    @Override
    @SuppressWarnings("NullableProblems")
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        ITileHandler.All.super.writeToNBT(nbt);
        IMachineTickable.super.writeToNBT(nbt);
        IMachineRecipe.super.writeToNBT(nbt);
        if (!bindNbt.hasNoTags()) {
            nbt.setTag(BIND_TILE_ENTITY_EXTRA, bindNbt);
        }
        return nbt;
    }
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return ITileHandler.All.super.hasCapability(capability, facing)
                || super.hasCapability(capability, facing);
    }
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T t = ITileHandler.All.super.getCapability(capability, facing);
        return t != null ? t : super.getCapability(capability, facing);
    }

    public void markBucketInput(int... slots) {
        IItemHandler iHandler = getItemHandler(SideHandlerType.INPUT);
        IntSet slotIgnore = getRecipeSlotIgnore();
        if (iHandler instanceof ItemHandler) {
            ItemHandler handler = (ItemHandler) iHandler;
            for (Integer i : slotIgnore) {
                handler.setSize(i, 64);
            }
            slotIgnore.clear();
            for (int slot : slots) {
                handler.setSize(slot, 1);
                slotIgnore.add(slot);
            }
        }
    }

    protected void applyConfig() {}
}
