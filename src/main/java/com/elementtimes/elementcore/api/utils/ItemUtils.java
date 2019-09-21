package com.elementtimes.elementcore.api.utils;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品/物品栈有关方法
 * @author lq2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemUtils {

    private static ItemUtils u = null;
    public static ItemUtils getInstance() {
        if (u == null) {
            u = new ItemUtils();
        }
        return u;
    }

    /**
     * 获取适用于某一物品的所有附魔
     * @param itemStack 要查找的物品
     * @return 所有可被附魔到该物品的附魔
     */
    public List<Enchantment> getSuitableEnchantments(ItemStack itemStack) {
        return GameRegistry.findRegistry(Enchantment.class).getValuesCollection().stream()
                .filter(enchantment -> enchantment.canApply(itemStack))
                .collect(Collectors.toList());
    }

    /**
     * 为某一物品附魔所有非诅咒的最大等级
     * @param itemStack 要附魔的物品
     */
    public void addMaxEnchantments(ItemStack itemStack) {
        EnchantmentHelper.setEnchantments(
                getSuitableEnchantments(itemStack).stream()
                        .filter(enchantment -> !enchantment.isCurse())
                        .map(enchantment -> new ImmutablePair <>(enchantment, enchantment.getMaxLevel()))
                        .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue)),
                itemStack
        );
    }

    /**
     * 将一个物品容器中的物品转化为 ItemStack 列表
     * @param itemHandler 物品容器
     * @return 物品列表
     */
    public List<ItemStack> toList(IItemHandler itemHandler, IntSet ignore) {
        List<ItemStack> input = new ArrayList<>(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!ignore.contains(i)) {
                input.add(i, itemHandler.getStackInSlot(i));
            }
        }
        return input;
    }

    /**
     * 将物品列表转化为 NBT 列表
     * @param items 物品列表
     * @return NBT 列表
     */
    public NBTTagList toNBTList(List<ItemStack> items) {
        NBTTagList list = new NBTTagList();
        items.forEach(item -> list.appendTag(item.writeToNBT(new NBTTagCompound())));
        return list;
    }

    /**
     * 将 NBT 列表转化为物品列表
     * @param list NBT 列表
     * @return 物品列表
     */
    public List<ItemStack> fromNBTList(NBTTagList list) {
        int count = list.tagCount();
        List<ItemStack> itemStacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            itemStacks.add(new ItemStack(list.getCompoundTagAt(i)));
        }
        return itemStacks;
    }

    /**
     * 将 IItemHandler 写入 NBTTagCompound
     * @param handler IItemHandler
     * @param key key
     * @param nbtTagCompound 待存储 NBTTagCompound
     * @return nbtTagCompound
     */
    public NBTTagCompound writeToNBT(IItemHandler handler, String key, NBTTagCompound nbtTagCompound) {
        if (handler.getSlots() != 0) {
            if (handler instanceof INBTSerializable) {
                nbtTagCompound.setTag(key, ((INBTSerializable) handler).serializeNBT());
            } else {
                NBTTagList list = new NBTTagList();
                for (int i = 0; i < handler.getSlots(); i++) {
                    list.appendTag(handler.getStackInSlot(i).serializeNBT());
                }
                nbtTagCompound.setTag(key, list);
            }
        }
        return nbtTagCompound;
    }

    /**
     * 从 NBTTagCompound 恢复 IItemHandler
     * @param handler IItemHandler
     * @param key key
     * @param nbtTagCompound 待存储 NBTTagCompound
     */
    public void readFromNBT(IItemHandler handler, String key, NBTTagCompound nbtTagCompound) {
        if (handler instanceof INBTSerializable) {
            //noinspection unchecked
            ((INBTSerializable) handler).deserializeNBT(nbtTagCompound.getTag(key));
        } else {
            NBTTagList list = (NBTTagList) nbtTagCompound.getTag(key);
            int count = Math.min(list.tagCount(), handler.getSlots());
            for (int i = 0; i < count; i++) {
                ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
                if (handler instanceof IItemHandlerModifiable) {
                    ((IItemHandlerModifiable) handler).setStackInSlot(i, stack);
                } else {
                    handler.extractItem(i, handler.getSlotLimit(i), false);
                    handler.insertItem(i, stack, false);
                }
            }
        }
    }

    public boolean isItemRawEqual(ItemStack is1, ItemStack is2) {
        if (is1 == is2) {
            return true;
        }
        if (is1.getCount() == 0 || is2.getCount() == 0) {
            int i1 = is1.getCount();
            int i2 = is2.getCount();
            is1.setCount(1);
            is2.setCount(1);
            boolean equal = is1.isItemEqual(is2);
            is1.setCount(i1);
            is2.setCount(i2);
            return equal;
        }
        return is1.isItemEqual(is2);
    }
}
