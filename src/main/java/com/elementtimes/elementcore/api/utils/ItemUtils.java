package com.elementtimes.elementcore.api.utils;

import com.elementtimes.elementcore.api.ECUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物品/物品栈有关方法
 * @author lq2007
 */
@SuppressWarnings({"unused"})
public class ItemUtils {

    private static ItemUtils u = null;
    public static ItemUtils getInstance() {
        if (u == null) {
            u = new ItemUtils();
        }
        return u;
    }

    /**
     * 将一个物品容器中的物品转化为 ItemStack 列表
     * @param itemHandler 物品容器
     * @param ignore 忽略的物品位置
     * @return 物品列表
     */
    public List<ItemStack> collect(IItemHandler itemHandler, IntSet ignore) {
        List<ItemStack> input = new ArrayList<>(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!ignore.contains(i)) {
                input.add(i, itemHandler.getStackInSlot(i));
            }
        }
        return input;
    }

    /**
     * 将一个物品容器中的物品转化为 ItemStack 列表
     * @param itemHandler 物品容器
     * @return 物品列表
     */
    public List<ItemStack> collect(IItemHandler itemHandler) {
        List<ItemStack> input = new ArrayList<>(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            input.add(i, itemHandler.getStackInSlot(i));
        }
        return input;
    }

    /**
     * 将物品列表转化为 NBT 列表
     * @param items 物品列表
     * @return NBT 列表
     */
    public ListNBT save(List<ItemStack> items) {
        ListNBT list = new ListNBT();
        items.forEach(item -> list.add(item.write(new CompoundNBT())));
        return list;
    }

    /**
     * 将 NBT 列表转化为物品列表
     * @param list NBT 列表
     * @return 物品列表
     */
    public List<ItemStack> read(ListNBT list) {
        int count = list.size();
        List<ItemStack> itemStacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            itemStacks.add(ItemStack.read(list.getCompound(i)));
        }
        return itemStacks;
    }

    /**
     * 将 IItemHandler 写入 CompoundNBT
     * @param handler IItemHandler
     * @param key key
     * @param compound 待存储 CompoundNBT
     * @return compound
     */
    public CompoundNBT write(IItemHandler handler, String key, CompoundNBT compound) {
        if (handler.getSlots() != 0) {
            if (handler instanceof INBTSerializable) {
                compound.put(key, ((INBTSerializable) handler).serializeNBT());
            } else {
                ListNBT list = new ListNBT();
                for (int i = 0; i < handler.getSlots(); i++) {
                    list.add(handler.getStackInSlot(i).serializeNBT());
                }
                compound.put(key, list);
            }
        }
        return compound;
    }

    /**
     * 从 CompoundNBT 恢复 IItemHandler
     * @param handler IItemHandler
     * @param key key
     * @param compound 待存储 CompoundNBT
     */
    public void read(IItemHandler handler, String key, CompoundNBT compound) {
        if (handler instanceof INBTSerializable) {
            //noinspection unchecked
            ((INBTSerializable) handler).deserializeNBT(compound.get(key));
        } else {
            ListNBT list = compound.getList(key, Constants.NBT.TAG_COMPOUND);
            int count = Math.min(list.size(), handler.getSlots());
            for (int i = 0; i < count; i++) {
                ItemStack stack = ItemStack.read(list.getCompound(i));
                if (handler instanceof IItemHandlerModifiable) {
                    ((IItemHandlerModifiable) handler).setStackInSlot(i, stack);
                } else {
                    handler.extractItem(i, handler.getSlotLimit(i), false);
                    handler.insertItem(i, stack, false);
                }
            }
        }
    }

    /**
     * 比较两个物品栈的物品是否相同
     * 不比较 NBT 和 damage
     * 主要用于当 ItemStack 物品数量为 0 时返回 AIR 无法比较物品是否相同的问题
     * @param is1 物品栈1
     * @param is2 物品栈2
     * @return 是否相同
     */
    public boolean isEqual(ItemStack is1, ItemStack is2) {
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

    /**
     * 比较两个物品的 ItemTags 是否有相同项
     * @param i1 物品1
     * @param i2 物品2
     * @return 是否等效
     */
    public boolean isEquivalent(Item i1, Item i2) {
        if (i1 == i2) {
            return true;
        }
        for (ResourceLocation tagName : i1.getTags()) {
            Tag<Item> tag = ItemTags.getCollection().getOrCreate(tagName);
            if (i2.isIn(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据物品 RegisterName 获取已注册或 Tag 物品
     * @param name RegisterName
     * @return RegisterName
     */
    public Item getItem(String name) {
        return getItem(new ResourceLocation(name.toLowerCase()));
    }

    /**
     * 根据物品 RegisterName 获取已注册或 Tag 物品
     * @return RegisterName
     */
    public Item getItem(ResourceLocation name) {
        if (name.equals(ForgeRegistries.BLOCKS.getKey(Blocks.AIR))) {
            return Items.AIR;
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(name);
            if (item == Items.AIR) {
                return ECUtils.tag.findItems(name).getAllElements().stream().findFirst().orElse(item);
            }
            return item;
        }
    }

    public ItemStack addAllEnchantment(ItemStack itemStack) {
        ItemStack stack = itemStack.copy();
        bindAllEnchantment(stack);
        return stack;
    }

    public void bindAllEnchantment(ItemStack itemStack) {
        Map<Enchantment, Integer> collect = EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
                .collect(Collectors.toMap(enchantment -> enchantment, Enchantment::getMaxLevel));
        EnchantmentHelper.setEnchantments(collect, itemStack);
    }

    public ItemStack addBestEnchantment(ItemStack itemStack) {
        ItemStack stack = itemStack.copy();
        bindBestEnchantment(stack);
        return stack;
    }

    public void bindBestEnchantment(ItemStack itemStack) {
        Map<Enchantment, Integer> collect = EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
                .filter(entry -> !entry.isCurse())
                .collect(Collectors.toMap(enchantment -> enchantment, Enchantment::getMaxLevel));
        EnchantmentHelper.setEnchantments(collect, itemStack);
    }
}
