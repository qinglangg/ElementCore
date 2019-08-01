package com.elementtimes.elementcore.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemUtil {

    public static Item getByNameOrId(String id) {
        if (id.contains(":")) {
            return getByNameOrId(new ResourceLocation(id));
        } else {
            return getByNameOrId(new ResourceLocation("minecraft", id));
        }
    }

    public static Item getByNameOrId(String domain, String id) {
        return getByNameOrId(new ResourceLocation(domain, id));
    }

    public static Item getByNameOrId(ResourceLocation id) {
        return ForgeRegistries.ITEMS.getValue(id);
    }

    public static ItemStack getItemStack(String name) {
        String[] split = name.split(":");
        Item item;
        int damage = 0;
        if (split.length == 1) {
            item = ItemUtil.getByNameOrId("minecraft", name);
        } else if (split.length == 2) {
            item = ItemUtil.getByNameOrId(name);
        } else {
            try {
                item = ItemUtil.getByNameOrId(split[0], split[1]);
                damage = Integer.parseInt(split[2]);
            } catch (Exception e) {
                item = ItemUtil.getByNameOrId(name);
            }
        }
        ItemStack stack = item == null ? new ItemStack(Items.AIR) : new ItemStack(item);
        stack.setDamage(damage);
        return stack;
    }
}
