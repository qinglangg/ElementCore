package com.elementtimes.elementcore.api.template.ingredient;

import com.elementtimes.elementcore.api.template.ingredient.list.DamageItemList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * 用于多物品耐久匹配的配方。
 * items 为可接受物品，damageCount 为一次合成要消耗的耐久，用于判断
 *
 * @see net.minecraft.item.crafting.Ingredient
 * @see net.minecraftforge.oredict.OreIngredient
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class DamageIngredient extends Ingredient {

    public DamageIngredient(Item[] items, int damageCount) {
        super(Arrays.stream(items).map(item -> new DamageItemList(item, damageCount)));
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
