package com.elementtimes.elementcore.api.template.tileentity;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.capability.EnergyHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.FluidTankInfo;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.FluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.TankHandlerVisitor;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandler;
import com.elementtimes.elementcore.api.template.capability.item.ItemHandlerVisitor;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.*;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.RecipeMachineLifecycle;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeCapture;
import com.elementtimes.elementcore.api.template.tileentity.recipe.MachineRecipeHandler;
import com.elementtimes.elementcore.common.network.GuiDataNetwork;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
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
    protected IFluidHandler mInputFluids;
    protected IFluidHandler mOutputFluids;
    protected TankHandlerVisitor mAllFluids;
    protected MachineRecipeHandler mRecipe;
    protected Set<IMachineLifecycle> mMachineLifeCycles = new LinkedHashSet<>();
    protected MachineRecipeCapture mWorkingRecipe = null;
    protected boolean isWorking = false;
    protected boolean isPause = false;
    protected int mEnergyProcessed = 0;
    protected int mEnergyUnprocessed = 0;
    protected IntSet mIgnoreInputSlot = new IntOpenHashSet();
    protected List<PlayerEntity> mPlayers = new ArrayList<>(5);
    public CompoundNBT bindNbt = new CompoundNBT();

    public BaseTileEntity(TileEntityType type, int energyCapacity, int inputCount, int outputCount) {
        this(type, energyCapacity, inputCount, outputCount, 0, 0, 0, 0);
    }

    public BaseTileEntity(TileEntityType type, int energyCapacity, int inputCount, int outputCount, int fluidInput, int inputCapacity, int fluidOutput, int outputCapacity) {
        super(type);
        onCreate();
        mEnergyHandler = new EnergyHandler(energyCapacity, Integer.MAX_VALUE, Integer.MAX_VALUE);
        mInputItems = new ItemHandler(inputCount, this::isInputValid);
        mOutputItems = new ItemHandler(outputCount, (integer, itemStack) -> false);
        mAllItems = new ItemHandlerVisitor(mInputItems, mOutputItems);
        mInputFluids = new FluidHandler(fluidInput, inputCapacity).setFillValid(this::isFillValid).setDrainValid(FluidHandler.FALSE);
        mOutputFluids = new FluidHandler(fluidOutput, outputCapacity).setFillValid(FluidHandler.FALSE).setDrainValid(FluidHandler.TRUE);
        mAllFluids = new TankHandlerVisitor(mInputFluids, mOutputFluids);
        addLifeCycle(new RecipeMachineLifecycle(this));
        addLifeCycle(new HandlerInfoMachineLifecycle(this::getOpenedPlayers, this::postMessage));
        applyConfig();
    }

    protected void onCreate() {}

    // network
    private void postMessage(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity sp = (ServerPlayerEntity) player;
            GuiDataNetwork network = new GuiDataNetwork();
            network.guiType = getGuiId();
            // energy
            EnergyHandler.EnergyProxy energyProxy = getEnergyProxy(SideHandlerType.READONLY);
            network.capacity = energyProxy.getMaxEnergyStored();
            network.stored = energyProxy.getEnergyStored();
            // process
            network.process = getEnergyProcessed();
            network.total = getEnergyProcessed() + getEnergyUnprocessed();
            // fluid
            FluidTankInfo[] inputTanks = getTanks(SideHandlerType.INPUT).getTankInfos();
            FluidTankInfo[] outputTanks = getTanks(SideHandlerType.OUTPUT).getTankInfos();
            FluidTankInfo[] otherTanks = getTanks(SideHandlerType.NONE).getTankInfos();
            network.fluidInputs = new Int2ObjectArrayMap<>(inputTanks.length);
            for (int i = 0; i < inputTanks.length; i++) {
                network.fluidInputs.put(i, inputTanks[i]);
            }
            network.fluidOutputs = new Int2ObjectArrayMap<>(outputTanks.length);
            for (int i = 0; i < outputTanks.length; i++) {
                network.fluidOutputs.put(i, outputTanks[i]);
            }
            network.fluidOthers = new Int2ObjectArrayMap<>(otherTanks.length);
            for (int i = 0; i < otherTanks.length; i++) {
                network.fluidOthers.put(i, otherTanks[i]);
            }
            ElementCore.INSTANCE.container.channel.sendTo(network, sp.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
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
    public void tick() {
        IMachineTickable.super.update(this);
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

    // energy
    @Override
    public EnergyHandler getEnergyHandler() { return mEnergyHandler; }
    @Override
    public SideHandlerType getEnergyType(Direction facing) {
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
            return stack.getItem() == Items.BUCKET;
        } else {
            List<ItemStack> list = ECUtils.item.collect(itemHandler, getRecipeSlotIgnore());
            ItemStack backup = list.get(slot);
            list.set(slot, stack);
            boolean valid = getRecipes().acceptInput(list, ECUtils.fluid.collect(getTanks(SideHandlerType.INPUT)));
            list.set(slot, backup);
            return valid;
        }
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
    public IFluidHandler getTanks(SideHandlerType type) {
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
                return FluidHandler.EMPTY;
        }
    }
    @Override
    public void setTanks(SideHandlerType type, IFluidHandler handler) {
        if (type == SideHandlerType.INPUT) {
            mInputFluids = handler;
        } else if (type == SideHandlerType.OUTPUT) {
            mOutputFluids = handler;
        }
        mAllFluids = new TankHandlerVisitor(mInputFluids, mOutputFluids);
    }
    @Override
    public boolean isFillValid(int slot, FluidStack fluidStack) {
        List<FluidStack> fluids = ECUtils.fluid.collect(getTanks(SideHandlerType.INPUT));
        fluids.set(slot, fluidStack);
        return getRecipes().acceptInput(ECUtils.item.collect(getItemHandler(SideHandlerType.INPUT), getRecipeSlotIgnore()), fluids);
    }

    // gui
    @Override
    public List<PlayerEntity> getOpenedPlayers() {
        return mPlayers;
    }
    @Override
    public float getProcess() {
        int total = getEnergyUnprocessed() + getEnergyProcessed();
        return total == 0 ? 0f : ((float) getEnergyProcessed()) / total;
    }
    @Override
    public <T extends Container> ContainerType<T> getContainerType() {
        ResourceLocation loc = new ResourceLocation(ElementCore.INSTANCE.container.id(), "tilebasegui");
        ContainerType containerType = Registry.MENU.getOrDefault(loc);
        return containerType;
    }
    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    // NBT && Capability
    @Override
    public void read(@Nonnull CompoundNBT compound) {
        super.read(compound);
        ITileHandler.All.super.read(compound);
        IMachineTickable.super.read(compound);
        IMachineRecipe.super.read(compound);
        if (compound.contains(BIND_TILE_ENTITY_EXTRA)) {
            bindNbt = compound.getCompound(BIND_TILE_ENTITY_EXTRA);
        }
        super.read(compound);
    }
    @Nonnull
    @Override
    @SuppressWarnings("NullableProblems")
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        super.write(nbt);
        ITileHandler.All.super.write(nbt);
        IMachineTickable.super.write(nbt);
        IMachineRecipe.super.write(nbt);
        if (bindNbt.isEmpty()) {
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        LazyOptional<T> t = ITileHandler.All.super.getCapability(capability, facing);
        return t.isPresent() ? t : super.getCapability(capability, facing);
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
