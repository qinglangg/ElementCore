package com.elementtimes.elementcore.api.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品/物品栈有关方法
 * @author lq2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemUtils {

    /**
     * 获取适用于某一物品的所有附魔
     * @param itemStack 要查找的物品
     * @return 所有可被附魔到该物品的附魔
     */
    public static List<Enchantment> getSuitableEnchantments(ItemStack itemStack) {
        return GameRegistry.findRegistry(Enchantment.class).getValues().stream()
                .filter(enchantment -> enchantment.canApply(itemStack))
                .collect(Collectors.toList());
    }

    /**
     * 为某一物品附魔所有非诅咒的最大等级
     * @param itemStack 要附魔的物品
     */
    public static void addAllEnchantments(ItemStack itemStack) {
        EnchantmentHelper.setEnchantments(
                getSuitableEnchantments(itemStack).stream()
                        .filter(enchantment -> !enchantment.isCurse())
                        .map(enchantment -> new ImmutablePair <>(enchantment, enchantment.getMaxLevel()))
                        .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue)), itemStack);
    }

    /**
     * 为某一物品附魔所有的最大等级
     * @param itemStack 要附魔的物品
     */
    public static void addAllCurse(ItemStack itemStack) {
        EnchantmentHelper.setEnchantments(
                getSuitableEnchantments(itemStack).stream()
                        .filter(Enchantment::isCurse)
                        .map(enchantment -> new ImmutablePair <>(enchantment, enchantment.getMaxLevel()))
                        .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue)), itemStack);
    }

    /**
     * 将一个物品容器中的物品转化为 ItemStack 列表
     * @param itemHandler 物品容器
     * @return 物品列表
     */
    public static List<ItemStack> toList(IItemHandler itemHandler) {
        List<ItemStack> input = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            input.add(i, itemHandler.getStackInSlot(i));
        }
        return input;
    }

    /**
     * 将物品列表转化为 NBT 列表
     * 主要是为了在一个 NBT 中保存多个 ItmStack 列表，如果只有一个请使用
     * @param items 物品列表
     * @return NBT 列表
     */
    public static ListNBT saveAllItems(NonNullList<ItemStack> items) {
        return (ListNBT) ItemStackHelper.saveAllItems(new CompoundNBT(), items).get("Items");
    }

    /**
     * 将 NBT 列表转化为物品列表
     * 主要是为了在一个 NBT 中保存多个 ItmStack 列表，如果只有一个请使用
     * @param list NBT 列表
     * @return 物品列表
     */
    public static NonNullList<ItemStack> loadAllItems(ListNBT list) {
        int count = list.size();
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(count, ItemStack.EMPTY);
        CompoundNBT compound = new CompoundNBT();
        compound.put("Items", list);
        ItemStackHelper.loadAllItems(compound, itemStacks);
        return itemStacks;
    }

    /**
     * 比较两个 ItemStack 内含的物品是否相同
     * 即使 ItemStack 的 count==0 也可以比较
     * @param is1 ItemStack 1
     * @param is2 ItemStack 2
     * @return 物品是否相同
     */
    public static boolean isItemRawEqual(ItemStack is1, ItemStack is2) {
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

    public static Collection<Item> getItems(ResourceLocation tagName) {
        return getItems(ItemTags.getCollection().get(tagName));
    }

    public static Collection<Item> getItems(Tag<Item> tag) {
        return tag == null ? Collections.emptySet() : tag.getAllElements();
    }
}
