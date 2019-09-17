package com.elementtimes.elementcore.api.template.capability.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * 类似 Java 的 SubList，不过这个做的是加法，对多个 ItemHandler 的访问器
 * @author luqin2007
 */
public class ItemHandlerVisitor implements IItemHandler {

    private final IItemHandler[] mHandlers;
    private final Readonly mReadonly;
    private int mSum = -1;

    // 使用 list 兼容多人访问
    private IntArrayList mSelect;
    private IntArrayList mSelectSlot;
    private ArrayList<IItemHandler> mSelectHandler;

    public ItemHandlerVisitor(IItemHandler... handlers) {
        mHandlers = handlers;
        mSelect = new IntArrayList(getSlots());
        mSelectSlot = new IntArrayList(getSlots());
        mSelectHandler = new ArrayList<>(getSlots());
        mReadonly = new Readonly();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) { }

    private int select(int slot) {
        // check
        for (int i = 0; i < mSelect.size(); i++) {
            if (mSelect.getInt(i) == slot) {
                return i;
            }
        }
        int index = -1;
        synchronized (this) {
            for (int i = 0; i < mSelect.size(); i++) {
                if (mSelect.getInt(i) >= 0) {
                    mSelect.set(i, slot);
                    index = i;
                }
            }
            if (index < 0) {
                index = mSelect.size();
                mSelect.add(slot);
                mSelectSlot.add(0);
                mSelectHandler.add(null);
            }
        }
        // select
        int ptr = 0;
        for (IItemHandler handler : mHandlers) {
            final int slots = handler.getSlots();
            if (slot < ptr + slots) {
                mSelectHandler.set(index, handler);
                mSelectSlot.set(index, slot - ptr);
                return index;
            } else {
                ptr += slots;
            }
        }
        return -1;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).setStackInSlot(mSelectSlot.getInt(i), stack);
        }
    }

    @Override
    public int getSlots() {
        if (mSum >= 0) {
            return mSum;
        }
        int sum = 0;
        for (IItemHandler handler : mHandlers) {
            sum += handler.getSlots();
        }
        mSum = sum;
        return sum;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).getStackInSlot(mSelectSlot.getInt(i));
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).insertItem(mSelectSlot.getInt(i), stack, simulate);
        }
        return stack;
    }

    @Override
    public ItemStack insertItemIgnoreValid(int slot, @Nonnull ItemStack stack, boolean simulate) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).insertItemIgnoreValid(mSelectSlot.getInt(i), stack, simulate);
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).extractItem(mSelectSlot.getInt(i), amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).getSlotLimit(mSelectSlot.getInt(i));
        }
        return 0;
    }

    public IItemHandler readonly() {
        return mReadonly;
    }

    @Override
    public void setSize(int slot, int count) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).setSize(mSelectSlot.getInt(i), count);
        }
    }

    @Override
    public int bind(ItemStack itemStack) {
        return -1;
    }

    @Override
    public void unbindAll() {
        for (IItemHandler handler : mHandlers) {
            handler.unbindAll();
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).isItemValid(mSelectSlot.getInt(i), stack);
        }
        return false;
    }

    @Override
    public void setSlotIgnoreChangeListener(int slot, ItemStack stack) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).setSlotIgnoreChangeListener(mSelectSlot.getInt(i), stack);
        }
    }

    private class Readonly implements IItemHandler {

        @Override
        public int getSlots() {
            return ItemHandlerVisitor.this.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemHandlerVisitor.this.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return ItemHandlerVisitor.this.getSlotLimit(slot);
        }

        @Override
        public void setSize(int slot, int count) { }

        @Override
        public int bind(ItemStack itemStack) {
            return -1;
        }

        @Override
        public void unbindAll() { }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return ItemHandlerVisitor.this.isItemValid(slot, stack);
        }

        @Override
        public void setSlotIgnoreChangeListener(int slot, ItemStack stack) { }

        @Override
        public ItemStack insertItemIgnoreValid(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) { }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) { }
    }
}
