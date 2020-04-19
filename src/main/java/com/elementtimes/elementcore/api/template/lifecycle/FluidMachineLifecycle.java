package com.elementtimes.elementcore.api.template.lifecycle;

import com.elementtimes.elementcore.api.template.capability.ProxyItemHandler;
import com.elementtimes.elementcore.api.interfaces.function.ILConsumer;
import com.elementtimes.elementcore.api.template.block.BaseTileEntity;
import com.elementtimes.elementcore.api.interfaces.block.IMachineLifecycle;
import com.elementtimes.elementcore.api.interfaces.block.ITileFluidHandler;
import com.elementtimes.elementcore.api.interfaces.block.ITileItemHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.function.IntFunction;

/**
 * 拥有流体输入/输出槽位的机器，在 tick 前后检查槽位物品
 *  每 tick 机器生命周期开始时检查是否有物品类型的流体容器，将流体从流体容器转移到机器流体槽
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FluidMachineLifecycle implements IMachineLifecycle {

    private final TileEntity mMachine;
    private final Int2ObjectArrayMap<Slot> mSlots = new Int2ObjectArrayMap<>();
    private IItemHandler mItemHandler = null;
    private IFluidHandler mFluidHandler = null;
    private ILConsumer.ItemConsumer saveHandler;
    private IntFunction<ItemStack> loadHandler;

    /**
     * 用于带有流体的机器，流体转移和同步部分生命周期创建
     * @param machine 流体机器 TestTileEntity
     * @param save 处理时，可能会产生一个空容器无法输出，该方法用于保存这个空容器
     * @param load 处理时，可能会产生一个空容器无法输出，该方法用于读取这个空容器
     */
    public FluidMachineLifecycle(TileEntity machine, ILConsumer.ItemConsumer save, IntFunction<ItemStack> load) {
        mMachine = machine;
        saveHandler = save;
        loadHandler = load;
    }

    public FluidMachineLifecycle(BaseTileEntity machine) {
        this(machine, (slot, stack) -> {
            CompoundNBT nbt;
            if (machine.bindNbt.contains("ECFMLI", Constants.NBT.TAG_COMPOUND)) {
                nbt = machine.bindNbt.getCompound("ECFMLI");
            } else if (!stack.isEmpty()) {
                nbt = new CompoundNBT();
                machine.bindNbt.put("ECFMLI", nbt);
            } else {
                return;
            }
            if (stack.isEmpty()) {
                nbt.remove(slot + "");
            } else {
                nbt.put(slot + "", stack.write(new CompoundNBT()));
            }
            machine.markDirty();
        }, slot -> {
            if (machine.bindNbt.contains("ECFMLI", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT nbt = machine.bindNbt.getCompound("ECFMLI");
                String key = slot + "";
                if (nbt.contains(key, Constants.NBT.TAG_COMPOUND)) {
                    ItemStack stack = ItemStack.read(nbt.getCompound(key));
                    nbt.remove(key);
                    machine.markDirty();
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        });
    }

    public FluidMachineLifecycle bindSlot(int fluidSlot, int inputSlot, int outputSlot) {
        mSlots.put(fluidSlot, new Slot(fluidSlot, inputSlot, outputSlot));
        return this;
    }

    public FluidMachineLifecycle bindSlot(int fluidStart, int fluidCount, int inputItemStart, int outputItemStart) {
        for (int i = 0; i < fluidCount; i++) {
            bindSlot(fluidStart + i, inputItemStart + i, outputItemStart + i);
        }
        return this;
    }

    public FluidMachineLifecycle removeSlots(int... slots) {
        for (int slot : slots) {
            mSlots.remove(slot);
        }
        return this;
    }

    @Override
    public void onTickStart() {
        for (Slot value : mSlots.values()) {
            value.fill();
        }
    }

    private IItemHandler getItems() {
        if (mItemHandler == null) {
            if (mMachine instanceof ITileItemHandler) {
                mItemHandler = ((ITileItemHandler) mMachine).getItemHandler();
            } else {
                mItemHandler = mMachine.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            }
            if (mItemHandler instanceof ProxyItemHandler) {
                mItemHandler = ((ProxyItemHandler) mItemHandler).new ProxyIgnore();
            }
        }
        return mItemHandler;
    }

    private IFluidHandler getFluids() {
        if (mFluidHandler == null) {
            if (mMachine instanceof ITileFluidHandler) {
                mFluidHandler = ((ITileFluidHandler) mMachine).getTanks();
            } else {
                mFluidHandler = mMachine.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
            }
        }
        return mFluidHandler;
    }

    private class Slot {
        int fluidSlot, inputSlot, outputSlot;

        Slot(int fluidSlot, int inputSlot, int outputSlot) {
            this.fluidSlot = fluidSlot;
            this.inputSlot = inputSlot;
            this.outputSlot = outputSlot;
        }

        void fill() {
            IItemHandler items = getItems();
            IFluidHandler fluids = getFluids();
            if (items != null && fluids != null) {
                ItemStack emptyContainer = loadHandler.apply(fluidSlot);
                if (!emptyContainer.isEmpty()) {
                    emptyContainer = items.insertItem(outputSlot, emptyContainer, false);
                }
                if (!emptyContainer.isEmpty()) {
                    saveHandler.accept(fluidSlot, emptyContainer);
                    return;
                }
                ItemStack slotInput = items.extractItem(inputSlot, 1, false);
                if (!slotInput.isEmpty()) {
                    slotInput.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(fluidHandler -> {
                        for (int i = 0; i < fluidHandler.getTanks(); i++) {
                            FluidStack drainSimulate = fluidHandler.drain(fluidHandler.getFluidInTank(i), IFluidHandler.FluidAction.SIMULATE);
                            if (!drainSimulate.isEmpty()) {
                                int fillSimulate = fluids.fill(drainSimulate, IFluidHandler.FluidAction.SIMULATE);
                                if (fillSimulate > 0) {
                                    drainSimulate.setAmount(fillSimulate);
                                    drainSimulate = fluidHandler.drain(drainSimulate, IFluidHandler.FluidAction.SIMULATE);
                                    if (!drainSimulate.isEmpty()) {
                                        FluidStack drainExecute = fluidHandler.drain(drainSimulate, IFluidHandler.FluidAction.EXECUTE);
                                        fluids.fill(drainExecute, IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }
                            }
                        }
                        ItemStack stack = fluidHandler.getContainer();
                        if (stack != slotInput && !ItemStack.areItemsEqual(stack, slotInput)) {
                            ItemStack i = items.insertItem(outputSlot, stack, false);
                            if (!i.isEmpty()) {
                                saveHandler.accept(fluidSlot, i);
                            }
                        } else {
                            items.insertItem(inputSlot, slotInput, false);
                        }
                    });
                }
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            Slot slot = (Slot) object;

            if (fluidSlot != slot.fluidSlot) {
                return false;
            }
            if (inputSlot != slot.inputSlot) {
                return false;
            }
            return outputSlot == slot.outputSlot;
        }

        @Override
        public int hashCode() {
            int result = fluidSlot;
            result = 31 * result + inputSlot;
            result = 31 * result + outputSlot;
            return result;
        }
    }
}
