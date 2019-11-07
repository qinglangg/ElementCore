package com.elementtimes.elementcore.api.template.ingredient.list;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DamageItemList implements Ingredient.IItemList {

    private Collection<ItemStack> items = null;
    private Item item;
    private int damage;

    public DamageItemList(Item item, int damage) {
        this.item = item;
        this.damage = damage;
    }

    @Override
    @Nonnull
    public Collection<ItemStack> getStacks() {
        if (items == null) {
            ItemStack itemStack = new ItemStack(item);
            if (itemStack.isDamageable()) {
                items = Collections.singleton(itemStack);
            } else {
                int maxDamage = item.getMaxDamage(itemStack);
                items = new ArrayList<>(maxDamage - damage);
                for (int i = 1; i < maxDamage - damage + 1; i++) {
                    ItemStack stack = itemStack.copy();
                    stack.setDamage(i);
                    items.add(stack);
                }
            }
        }
        return items;
    }

    @Override
    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("item", new JsonPrimitive(item.getRegistryName().toString()));
        jsonObject.add("damage", new JsonPrimitive(damage));
        return jsonObject;
    }
}
