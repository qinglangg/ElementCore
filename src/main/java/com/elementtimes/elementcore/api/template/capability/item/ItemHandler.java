package com.elementtimes.elementcore.api.template.capability.item;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * 自定义实现的 IItemHandler
 * 对其 insertItem 做了重写，防止管道输入错误物品
 *
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ItemHandler extends ItemStackHandler implements IItemHandler {
    public static final ItemHandler EMPTY = new ItemHandler(0);
    public static final BiPredicate<Integer, ItemStack> FALSE = (i, is) -> false;
    public static final BiPredicate<Integer, ItemStack> TRUE = (i, is) -> true;

    private int mSize;
    private Int2IntMap mSlotSize = new Int2IntOpenHashMap();
    private boolean replaced = false;

    public ItemHandler(int size) {
        this(size, (integer, itemStack) -> true);
    }

    public ItemHandler(int size, BiPredicate<Integer, ItemStack> check) {
        super(size);
        mSize = size;
        mInputValid = check;
    }

    @Override
    public void setSlotIgnoreChangeListener(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
    }

    @Override
    public CompoundNBT serializeNBT() {
        unbindAll();
        CompoundNBT nbt = super.serializeNBT();
        ListNBT slotCount = new ListNBT();
        mSlotSize.int2IntEntrySet().forEach(entry ->
                slotCount.add(new IntArrayNBT(new int[]{entry.getIntKey(), entry.getIntValue()})));
        nbt.put("_bind_slot_count_", slotCount);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        unbindAll();
        super.deserializeNBT(nbt);
        ListNBT slotCounts = nbt.getList("_bind_slot_count_", Constants.NBT.TAG_INT_ARRAY);
        slotCounts.forEach(nbtArray -> {
            int[] slotCount = ((IntArrayNBT) nbtArray).getIntArray();
            mSlotSize.put(slotCount[0], slotCount[1]);
        });
    }

    /**
     * 用于判断一个物品是否可以插入到对应槽位中去
     */
    private BiPredicate<Integer, ItemStack> mInputValid;

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return mInputValid.test(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    /**
     * 直接调用 insertItem 而忽视 isItemValid，用于机器输出产物
     */
    @Override
    @Nonnull
    public ItemStack insertItemIgnoreValid(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public int bind(ItemStack itemStack) {
        // 防止 stacks 不允许 newRecipe
        if (!replaced) {
            NonNullList<ItemStack> newStacks = NonNullList.create();
            for (int i = 0; i < stacks.size(); i++) {
                newStacks.add(i, stacks.get(i));
            }
            stacks = newStacks;
        }
        int index = stacks.size();
        stacks.add(itemStack);
        return index;
    }

    @Override
    public void unbindAll() {
        // 防止 stacks 不允许 newRecipe
        if (!replaced) {
            NonNullList<ItemStack> newStacks = NonNullList.create();
            for (int i = 0; i < stacks.size(); i++) {
                newStacks.add(i, stacks.get(i));
            }
            stacks = newStacks;
        }
        int expend = stacks.size() - mSize;
        if (expend >= 0) {
            ItemStack[] itemStacks = new ItemStack[expend];
            int pointer = expend - 1;
            while (stacks.size() > mSize) {
                itemStacks[pointer] = stacks.remove(stacks.size() - 1);
                pointer--;
            }
            onUnbindAllListener.forEach(consumer -> consumer.accept(itemStacks));
        }
    }

    public final List<IntConsumer> onItemChangeListener = new ArrayList<>();
    public final List<Consumer<ItemStack[]>> onUnbindAllListener = new ArrayList<>();

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        onItemChangeListener.forEach(i -> i.accept(slot));
    }

    @Override
    public void setSize(int slot, int count) {
        mSlotSize.put(slot, count);
        if (getStackInSlot(slot).getCount() > count) {
            getStackInSlot(slot).setCount(count);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return mSlotSize.getOrDefault(slot, super.getSlotLimit(slot));
    }
}
