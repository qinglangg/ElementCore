package com.elementtimes.elementcore.annotation.other;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用于多物品耐久匹配的配方。
 * items 为可接受物品，damageCount 为一次合成要消耗的耐久，用于判断
 *
 * @see Ingredient
 * @author luqin2007
 */
public class DamageIngredient extends Ingredient {

    private DamageIngredient(IItemList[] itemLists) {
        super(Arrays.stream(itemLists));
        acceptedItems = itemLists;
    }

    public static DamageIngredient create(Item[] items, int damageCount) {
        IItemList[] itemLists = new IItemList[]{new ItemList(damageCount, items)};
        return new DamageIngredient(itemLists);
    }

    private ItemStack[] matchingStacks;
    private IItemList[] acceptedItems;
    private IntList matchingStacksPacked;

    @Override
    @Nonnull
    public ItemStack[] getMatchingStacks() {
        this.determineMatchingStacks();
        return matchingStacks;
    }

    private void determineMatchingStacks() {
        if (matchingStacks == null) {
            matchingStacks = Arrays.stream(acceptedItems)
                    .flatMap(itemList -> itemList.getStacks().stream())
                    .distinct()
                    .toArray(ItemStack[]::new);
        }
    }

    @Override
    @Nonnull
    public IntList getValidItemStacksPacked() {
        if (matchingStacksPacked == null) {
            determineMatchingStacks();
            matchingStacksPacked = new IntArrayList(matchingStacks.length);

            for(ItemStack itemstack : matchingStacks) {
                //noinspection deprecation
                int id = Registry.ITEM.getId(itemstack.getItem()) << 16 | itemstack.getDamage() & 65535;
                matchingStacksPacked.add(id);
                matchingStacksPacked.add(RecipeItemHelper.pack(itemstack));
            }

            matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return matchingStacksPacked;
    }

    @Override
    public boolean hasNoMatchingItems() {
        return acceptedItems.length == 0 && matchingStacks.length == 0 && (matchingStacksPacked == null || matchingStacksPacked.isEmpty());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean test(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        } else if (acceptedItems.length == 0) {
            return itemStack.isEmpty();
        } else {
            this.determineMatchingStacks();

            for(ItemStack itemstack : matchingStacks) {
                if (itemstack.getItem() == itemStack.getItem() && itemstack.getDamage() == itemStack.getDamage()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class ItemList implements Ingredient.IItemList {
        private Collection<ItemStack> mItemStacks;

        ItemList(int damageCount, Item... items) {
            mItemStacks = Arrays.stream(items)
                    .map(ItemStack::new)
                    .map(itemStack -> {
                        if (itemStack.isDamageable()) {
                            int itemCount = itemStack.getMaxDamage() - damageCount;
                            ItemStack[] itemsArray = new ItemStack[itemCount];
                            for (int i = 0; i < itemCount; i++) {
                                ItemStack stack = itemStack.copy();
                                stack.setDamage(i + 1);
                                itemsArray[i] = stack;
                            }
                            return itemsArray;
                        } else {
                            return new ItemStack[] {itemStack.copy()};
                        }
                    })
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        @Nonnull
        public Collection<ItemStack> getStacks() {
            return mItemStacks;
        }

        @Override
        @Nonnull
        public JsonObject serialize() {
            JsonObject jsonobject = new JsonObject();
            JsonArray array = new JsonArray();
            for (ItemStack itemStack : mItemStacks) {
                JsonObject object = new JsonObject();
                object.addProperty("item", Objects.requireNonNull(itemStack.getItem().getRegistryName()).toString());
                if (itemStack.isDamageable()) {
                    object.addProperty("damage", itemStack.getDamage());
                }
                array.add(object);
            }
            jsonobject.add("items", array);
            return jsonobject;
        }
    }
}
