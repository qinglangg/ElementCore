package com.elementtimes.elementcore.api.utils;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 用于合成的工具类
 * annotation 包以后怕是会独立出去，所以尽量不会依赖包外的东西
 * @author luqin2007
 */
public class RecipeUtils {

    private static RecipeUtils u = null;
    public static RecipeUtils getInstance() {
        if (u == null) {
            u = new RecipeUtils();
        }
        return u;
    }

    private CraftingInventory tempCrafting = new CraftingInventory(new Container(ContainerType.CRAFTING, 0) {
        @Override
        public void onCraftMatrixChanged(IInventory inventoryIn) { }
        @Override
        public boolean canInteractWith(@Nonnull PlayerEntity playerIn) { return false; }
    }, 3, 3);

    /**
     * 测试合成表
     * @param input 输入
     * @return 输出
     */
    public ItemStack getCraftingResult(World world, @Nonnull NonNullList<ItemStack> input) {
        tempCrafting.clear();
        for (int i = 0; i < input.size(); i++) {
            tempCrafting.setInventorySlotContents(i, input.get(i));
        }
        for (IRecipe recipe : world.getRecipeManager().getRecipes()) {
            if (recipe.matches(tempCrafting, world)) {
                tempCrafting.clear();
                return recipe.getRecipeOutput();
            }
        }
        tempCrafting.clear();
        return ItemStack.EMPTY;
    }

    /**
     * 获取单物品输入的输出
     * @param tag 矿辞
     * @return 输出
     */
    public Map<ItemStack, ItemStack> getCraftingResult(World world, Tag tag) {
        Map<ItemStack, ItemStack> results = Maps.newHashMap();
        for (Object element : tag.getAllElements()) {
            ItemStack stack;
            if (element instanceof IItemProvider) {
                stack = new ItemStack((IItemProvider) element);
            } else if (element instanceof Fluid) {
                stack = new ItemStack(((Fluid) element).getFilledBucket());
            } else {
                stack = ItemStack.EMPTY;
            }
            ItemStack result = getCraftingResult(world, NonNullList.withSize(1, stack));
            results.put(stack, result);
        }
        return results;
    }
}
