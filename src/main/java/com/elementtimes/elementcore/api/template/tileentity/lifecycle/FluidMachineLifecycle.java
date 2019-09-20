package com.elementtimes.elementcore.api.template.tileentity.lifecycle;

import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.template.capability.fluid.ITankHandler;
import com.elementtimes.elementcore.api.template.capability.item.IItemHandler;
import com.elementtimes.elementcore.api.template.tileentity.BaseTileEntity;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.IMachineLifecycle;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.List;
import java.util.stream.Stream;

/**
 * 拥有流体输入/输出槽位的机器，在 tick 前后检查槽位物品
 *  每 tick 机器生命周期开始时检查是否有物品类型的流体容器，将流体从流体容器转移到机器流体槽
 *  每 tick 机器生命周期结束时检查是否有物品类型的流体容器，将流体从机器流体槽转移到流体容器
 *  每 tick 机器生命周期结束时检查机器 gui 是否被玩家打开，如果打开则同步流体信息到客户端
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FluidMachineLifecycle implements IMachineLifecycle {

    private final BaseTileEntity mMachine;
    public final Int2ObjectMap<int[]> mInputs;
    public final Int2ObjectMap<int[]> mOutputs;

    private final IItemHandler inputItems, outputItems;
    private final ITankHandler inputFluids, outputFluids;

    private final String BIND_FLUID_EMPTY_CONTAINER = "_nbt_fluid_empty_container_";
    private final String BIND_FLUID_EMPTY_CONTAINER_SLOT = "_nbt_fluid_empty_container_slot_";
    private final String BIND_FLUID_EMPTY_CONTAINER_ITEM = "_nbt_fluid_empty_container_item_";
    private final String BIND_FLUID_FULL_CONTAINER = "_nbt_fluid_full_container_";
    private final String BIND_FLUID_FULL_CONTAINER_SLOT = "_nbt_fluid_full_container_slot_";
    private final String BIND_FLUID_FULL_CONTAINER_ITEM = "_nbt_fluid_full_container_item_";

    /**
     * 用于带有流体的机器，流体转移和同步部分生命周期创建
     * @param machine 流体机器 TileEntity
     * @param bucketInputSlots  流体输入槽与物品类型流体容器输入/输出槽的绑定关系，
     *                          key 为流体输入槽位，value 为 int 数组，第一个元素为绑定的物品输入槽位，第二个元素为绑定的物品输出槽位
     * @param bucketOutputSlots 流体输出槽与物品类型流体容器输入/输出槽的绑定关系，
     *                          key 为流体输出槽位，value 为 int 数组，第一个元素为绑定的物品输入槽位，第二个元素为绑定的物品输出槽位
     */
    public FluidMachineLifecycle(BaseTileEntity machine, Int2ObjectMap<int[]> bucketInputSlots, Int2ObjectMap<int[]> bucketOutputSlots) {
        mMachine = machine;
        mInputs = bucketInputSlots;
        mOutputs = bucketOutputSlots;
        inputItems = machine.getItemHandler(SideHandlerType.INPUT);
        outputItems = machine.getItemHandler(SideHandlerType.OUTPUT);
        inputFluids = machine.getTanks(SideHandlerType.INPUT);
        outputFluids = machine.getTanks(SideHandlerType.OUTPUT);
        markBucketInput();
    }

    /**
     * 用于一般流体配置的生命周期
     *  每个流体槽都有桶的输入和输出槽位
     *  所有的桶输入槽位从 inputStart 开始按顺序向后排列
     *  所有的桶输出槽位从 outputStart 开始按顺序向后排列
     * @param machine 机器 TileEntity
     * @param inputCount 输入槽位个数
     * @param outputCount 输出槽位个数
     * @param inputStart 桶输入槽位起始序列
     * @param outputStart 桶输出槽位起始序列
     */
    public FluidMachineLifecycle(BaseTileEntity machine, int inputCount, int outputCount, int inputStart, int outputStart) {
        mMachine = machine;
        mInputs = new Int2ObjectArrayMap<>(inputCount);
        mOutputs = new Int2ObjectArrayMap<>(outputCount);
        inputItems = machine.getItemHandler(SideHandlerType.INPUT);
        outputItems = machine.getItemHandler(SideHandlerType.OUTPUT);
        inputFluids = machine.getTanks(SideHandlerType.INPUT);
        outputFluids = machine.getTanks(SideHandlerType.OUTPUT);

        // fill
        for (int i = 0; i < inputCount; i++) {
            mInputs.put(i, new int[] { inputStart + i, outputStart + i });
        }

        for (int i = 0; i < outputCount; i++) {
            mOutputs.put(i, new int[]{ inputStart + inputCount + i, outputStart + inputCount + i });
        }

        markBucketInput();
    }

    /**
     * 对构造的再次简化，假定流体物品槽位全部位于输入/输出槽位末尾，且流体输入槽绑定的物品槽位于流体输出槽绑定的物品槽之前
     * @param machine 机器 TileEntity
     * @param inputCount 输入槽位个数
     * @param outputCount 输出槽位个数
     */
    public FluidMachineLifecycle(BaseTileEntity machine, int inputCount, int outputCount) {
        this(machine, inputCount, outputCount,
                machine.getItemHandler(SideHandlerType.INPUT).getSlots() - inputCount - outputCount,
                machine.getItemHandler(SideHandlerType.OUTPUT).getSlots() - inputCount - outputCount);
    }

    /**
     * 对构造的再次简化，假定
     *  1.流体物品槽位全部位于输入/输出槽位末尾
     *  2.流体输入槽绑定的物品槽位于流体输出槽绑定的物品槽之前
     *  3.所有流体输入槽都是 GUI 输入，流体输出槽都是 GUI 输出
     * @param machine 机器 TileEntity
     */
    public FluidMachineLifecycle(BaseTileEntity machine) {
        mMachine = machine;
        inputItems = machine.getItemHandler(SideHandlerType.INPUT);
        outputItems = machine.getItemHandler(SideHandlerType.OUTPUT);
        inputFluids = machine.getTanks(SideHandlerType.INPUT);
        outputFluids = machine.getTanks(SideHandlerType.OUTPUT);

        int inputCount = inputFluids.size();
        int outputCount = outputFluids.size();
        int inputStart = inputItems.getSlots() - inputCount - outputCount;
        int outputStart = outputItems.getSlots() - inputCount - outputCount;

        mInputs = new Int2ObjectArrayMap<>(inputCount);
        mOutputs = new Int2ObjectArrayMap<>(outputCount);

        // fill
        for (int i = 0; i < inputCount; i++) {
            mInputs.put(i, new int[] { inputStart + i, outputStart + i });
        }

        for (int i = 0; i < outputCount; i++) {
            mOutputs.put(i, new int[]{ inputStart + inputCount + i, outputStart + inputCount + i });
        }

        markBucketInput();
    }

    private void markBucketInput() {
        int[] slots = Stream
                .concat(mInputs.values().stream(), mOutputs.values().stream())
                .mapToInt(ints -> ints[0])
                .toArray();
        mMachine.markBucketInput(slots);
    }

    @Override
    public void onTickStart() {
        insertEmptyContainers();
        List<FluidStack> inputFluidList = ECUtils.fluid.toListNotNull(inputFluids);
        mInputs.int2ObjectEntrySet().forEach(entry -> {
            int slot = entry.getIntKey();
            int bucketInput = entry.getValue()[0];
            int bucketOutput = entry.getValue()[1];
            if (!ECUtils.fluid.isFull(inputFluids, slot)) {
                ItemStack containerIn = inputItems.getStackInSlot(bucketInput);
                IFluidHandlerItem fluidHandler = containerIn
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    FluidStack fluidInput = inputFluidList.get(slot);
                    tryEmptyContainer(fluidInput, fluidHandler, slot);
                    insertEmptyContainer(fluidHandler, slot, bucketInput, bucketOutput);
                }
            }
        });
    }

    private void insertEmptyContainers() {
        if (mMachine.bindNbt.hasKey(BIND_FLUID_EMPTY_CONTAINER)) {
            NBTTagList emptyList = (NBTTagList) mMachine.bindNbt.getTag(BIND_FLUID_EMPTY_CONTAINER);
            mMachine.bindNbt.removeTag(BIND_FLUID_EMPTY_CONTAINER);
            NBTTagList list2 = null;
            for (NBTBase nbtBase : emptyList) {
                NBTTagCompound nbtContainer = (NBTTagCompound) nbtBase;
                int slot = nbtContainer.getInteger(BIND_FLUID_EMPTY_CONTAINER_SLOT);
                ItemStack item = new ItemStack(nbtContainer.getCompoundTag(BIND_FLUID_EMPTY_CONTAINER_ITEM));
                int[] ints = mInputs.get(slot);
                if (ints != null) {
                    ItemStack stack = outputItems.insertItemIgnoreValid(ints[1], item, false);
                    if (!stack.isEmpty()) {
                        if (list2 == null) {
                            list2 = new NBTTagList();
                        }
                        NBTTagCompound nbtContainer2 = new NBTTagCompound();
                        nbtContainer2.setInteger(BIND_FLUID_EMPTY_CONTAINER_SLOT, slot);
                        nbtContainer2.setTag(BIND_FLUID_EMPTY_CONTAINER_ITEM, stack.serializeNBT());
                        list2.appendTag(nbtContainer2);
                    }
                }
            }
            if (list2 != null) {
                mMachine.bindNbt.setTag(BIND_FLUID_EMPTY_CONTAINER, list2);
            }
        }
    }

    private void tryEmptyContainer(FluidStack fluidInput, IFluidHandlerItem fluidHandler, int slot) {
        FluidStack transfer;
        if (fluidInput != null && fluidInput.amount > 0) {
            int space = inputFluids.getCapacity(slot) - fluidInput.amount;
            FluidStack take = fluidHandler.drain(new FluidStack(fluidInput, space), false);
            if (take != null) {
                transfer = new FluidStack(fluidInput, take.amount);
            } else {
                transfer = null;
            }
        } else if (mMachine.getWorkingRecipe() != null) {
            transfer = mMachine.getWorkingRecipe().fluidInputs.get(slot);
        } else {
            transfer = fluidHandler.drain(inputFluids.getCapacity(slot), false);
        }

        if (transfer != null && transfer.amount != 0) {
            int fill = inputFluids.fill(slot, transfer, false);
            if (fill > 0) {
                FluidStack drain = fluidHandler.drain(transfer, false);
                if (drain != null && drain.amount > 0) {
                    drain.amount = Math.min(drain.amount, fill);
                    inputFluids.fill(slot, drain, true);
                    fluidHandler.drain(drain, true);
                }
            }
        }
    }

    private void insertEmptyContainer(IFluidHandlerItem fluidHandler, int slot, int bucketInput, int bucketOutput) {
        if (ECUtils.fluid.isEmpty(fluidHandler.getTankProperties())) {
            ItemStack extractItem = inputItems.extractItem(bucketInput, 1, false);
            if (!extractItem.isEmpty()) {
                ItemStack fluidContainer = fluidHandler.getContainer();
                ItemStack insertItem = outputItems.insertItemIgnoreValid(bucketOutput, fluidContainer, false);
                if (!insertItem.isEmpty()) {
                    NBTTagList containers;
                    if (!mMachine.bindNbt.hasKey(BIND_FLUID_EMPTY_CONTAINER)) {
                        containers = new NBTTagList();
                        mMachine.bindNbt.setTag(BIND_FLUID_EMPTY_CONTAINER, containers);
                    } else {
                        containers = (NBTTagList) mMachine.bindNbt.getTag(BIND_FLUID_EMPTY_CONTAINER);
                    }
                    NBTTagCompound nbtContainer = new NBTTagCompound();
                    nbtContainer.setInteger(BIND_FLUID_EMPTY_CONTAINER_SLOT, slot);
                    nbtContainer.setTag(BIND_FLUID_EMPTY_CONTAINER_ITEM, insertItem.serializeNBT());
                    containers.appendTag(nbtContainer);
                }
            }
        }
    }

    @Override
    public void onTickFinish() {
        insertFullContainers();
        List<FluidStack> outputFluidList = ECUtils.fluid.toListNotNull(outputFluids);
        mOutputs.int2ObjectEntrySet().forEach(entry -> {
            int slot = entry.getIntKey();
            int bucketInput = entry.getValue()[0];
            int bucketOutput = entry.getValue()[1];
            ItemStack containerIn = inputItems.getStackInSlot(bucketInput);
            IFluidHandlerItem fluidHandler = containerIn.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null) {
                tryFillContainer(slot, outputFluidList, fluidHandler);
                insertFullContainer(slot, fluidHandler, bucketInput, bucketOutput);
            }
        });
    }

    private void insertFullContainers() {
        if (mMachine.bindNbt.hasKey(BIND_FLUID_FULL_CONTAINER)) {
            NBTTagList emptyList = (NBTTagList) mMachine.bindNbt.getTag(BIND_FLUID_FULL_CONTAINER);
            mMachine.bindNbt.removeTag(BIND_FLUID_FULL_CONTAINER);
            NBTTagList list2 = null;
            for (NBTBase nbtBase : emptyList) {
                NBTTagCompound nbtContainer = (NBTTagCompound) nbtBase;
                int slot = nbtContainer.getInteger(BIND_FLUID_FULL_CONTAINER_SLOT);
                ItemStack item = new ItemStack(nbtContainer.getCompoundTag(BIND_FLUID_FULL_CONTAINER_ITEM));
                int[] ints = mOutputs.get(slot);
                if (ints != null) {
                    ItemStack stack = outputItems.insertItemIgnoreValid(ints[1], item, false);
                    if (!stack.isEmpty()) {
                        if (list2 == null) {
                            list2 = new NBTTagList();
                        }
                        NBTTagCompound nbtContainer2 = new NBTTagCompound();
                        nbtContainer2.setInteger(BIND_FLUID_FULL_CONTAINER_SLOT, slot);
                        nbtContainer2.setTag(BIND_FLUID_FULL_CONTAINER_ITEM, stack.serializeNBT());
                        list2.appendTag(nbtContainer2);
                    }
                }
            }
            if (list2 != null) {
                mMachine.bindNbt.setTag(BIND_FLUID_FULL_CONTAINER, list2);
            }
        }
    }

    private void tryFillContainer(int slot, List<FluidStack> outputFluidList, IFluidHandlerItem fluidHandler) {
        if (!ECUtils.fluid.isEmpty(outputFluids, slot)) {
            FluidStack output = outputFluidList.get(slot);
            if (output != null && output.amount > 0) {
                FluidStack drain = outputFluids.drain(slot, output, false);
                int fill = fluidHandler.fill(output, false);
                if (fill > 0) {
                    drain.amount = Math.min(drain.amount, fill);
                    outputFluids.drain(slot, drain, true);
                    fluidHandler.fill(drain, true);
                }
            }
        }
    }

    private void insertFullContainer(int slot, IFluidHandlerItem fluidHandler, int bucketInput, int bucketOutput) {
        if (ECUtils.fluid.isFull(fluidHandler.getTankProperties())) {
            ItemStack extractItem = inputItems.extractItem(bucketInput, 1, false);
            if (!extractItem.isEmpty()) {
                ItemStack itemStack = fluidHandler.getContainer();
                ItemStack insertItem = outputItems.insertItemIgnoreValid(bucketOutput, itemStack, false);
                if (!insertItem.isEmpty()) {
                    NBTTagList containers;
                    if (!mMachine.bindNbt.hasKey(BIND_FLUID_FULL_CONTAINER)) {
                        containers = new NBTTagList();
                        mMachine.bindNbt.setTag(BIND_FLUID_FULL_CONTAINER, containers);
                    } else {
                        containers = (NBTTagList) mMachine.bindNbt.getTag(BIND_FLUID_FULL_CONTAINER);
                    }

                    NBTTagCompound nbtContainer = new NBTTagCompound();
                    nbtContainer.setInteger(BIND_FLUID_FULL_CONTAINER_SLOT, slot);
                    nbtContainer.setTag(BIND_FLUID_FULL_CONTAINER_ITEM, insertItem.serializeNBT());
                    containers.appendTag(nbtContainer);
                }
            }
        }
    }
}
