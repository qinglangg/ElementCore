package com.elementtimes.elementcore.api.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用于合成的工具类
 * annotation 包以后怕是会独立出去，所以尽量不会依赖包外的东西
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RecipeUtils {

    private static CraftingInventory tempCrafting = new CraftingInventory(new Container(ContainerType.CRAFTING, 0) {
        @Override
        public boolean canInteractWith(@Nonnull PlayerEntity playerIn) { return false; }
    }, 3, 3);

    /**
     * 测试合成表
     * @param input 输入
     * @return 输出
     */
    public static ItemStack getCraftingResult(World world, @Nonnull NonNullList<ItemStack> input) {
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
     * @param itemTag 输入矿辞名
     * @return 输出
     */
    public static List<RecipeInfo> getOneItemCrafting(World world, ResourceLocation itemTag) {
        List<RecipeInfo> results = new ArrayList<>();
        for (Item inputItem : ItemUtils.getItems(itemTag)) {
            results.add(getOneItemCrafting(world, new ItemStack(inputItem)));
        }
        return results;
    }

    /**
     * 获取单物品输入的输出
     * @param itemTag 输入矿辞名
     * @return 输出
     */
    public static List<RecipeInfo> getOneItemCrafting(World world, Tag<Item> itemTag) {
        List<RecipeInfo> results = new ArrayList<>();
        for (Item inputItem : ItemUtils.getItems(itemTag)) {
            results.add(getOneItemCrafting(world, new ItemStack(inputItem)));
        }
        return results;
    }

    /**
     * 获取单物品输入的输出
     * @param input 输入物品
     * @return 输出
     */
    public static RecipeInfo getOneItemCrafting(World world, ItemStack input) {
        ItemStack i = ItemHandlerHelper.copyStackWithSize(input, 1);
        ItemStack result;
        if (!i.isEmpty()) {
            result = getCraftingResult(world, NonNullList.withSize(1, i));
        } else {
            result = ItemStack.EMPTY;
        }
        return new RecipeInfo(1, 1, false, result, i);
    }

    /**
     * 压缩类型合成表获取
     * @param inputType 输入的物品
     * @return 合成表
     */
    public static List<RecipeInfo> getCompressCrafting(World world, ItemStack inputType) {
        List<RecipeInfo> infos = new ArrayList<>();
        // 2 x 2
        ItemStack input = ItemHandlerHelper.copyStackWithSize(inputType, 1);
        NonNullList<ItemStack> inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        inputs.set(0, input);
        inputs.set(1, input);
        inputs.set(3, input);
        inputs.set(4, input);
        ItemStack result = getCraftingResult(world, inputs);
        infos.add(new RecipeInfo(2, 2, true, result, input, 4));
        // 3 x 3
        inputs = NonNullList.withSize(9, input);
        result = getCraftingResult(world, inputs);
        infos.add(new RecipeInfo(3, 3, true, result, input, 9));
        return infos;
    }

    public static class RecipeInfo {
        public final Int2ObjectMap<ItemStack> inputs;
        public final ItemStack output;
        public final boolean shaped;
        public final int width, height;

        public RecipeInfo(int width, int height, boolean shaped, ItemStack output, ItemStack... inputs) {
            this.output = output;
            this.shaped = shaped;
            this.width = width;
            this.height = height;
            this.inputs = new Int2ObjectArrayMap<>();
            for (int i = 0; i < inputs.length; i++) {
                ItemStack stack = inputs[i];
                if (stack != null && !stack.isEmpty()) {
                    this.inputs.put(i, stack);
                }
            }
        }

        public RecipeInfo(int width, int height, boolean shaped, ItemStack output, ItemStack input, int times) {
            this(width, height, shaped, output, Collections.nCopies(times, input).toArray(new ItemStack[0]));
        }
    }
}
