package com.elementtimes.elementcore.api.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 用于合成的工具类
 * annotation 包以后怕是会独立出去，所以尽量不会依赖包外的东西
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RecipeUtils {

    private static RecipeUtils u = null;
    public static RecipeUtils getInstance() {
        if (u == null) {
            u = new RecipeUtils();
        }
        return u;
    }

    private InventoryCrafting tempCrafting = new InventoryCrafting(new Container() {
        @Override
        public boolean canInteractWith(@Nonnull EntityPlayer playerIn) { return false; }
        @Override
        public void onCraftMatrixChanged(IInventory inventoryIn) { }
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
        ItemStack resultEntry = CraftingManager.findMatchingResult(tempCrafting, world).copy();
        tempCrafting.clear();
        return resultEntry;
    }

    /**
     * 获取单物品输入的输出
     * @param oreName 输入矿辞名
     * @return 输出
     */
    public List<RecipeInfo> getOneItemCrafting(World world, String oreName) {
        List<RecipeInfo> results = new ArrayList<>();
        ItemStack[] inputItems = CraftingHelper.getIngredient(oreName).getMatchingStacks();
        for (ItemStack inputItem : inputItems) {
            results.add(getOneItemCrafting(world, inputItem));
        }
        return results;
    }

    /**
     * 获取单物品输入的输出
     * @param input 输入物品
     * @return 输出
     */
    public RecipeInfo getOneItemCrafting(World world, ItemStack input) {
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
    public List<RecipeInfo> getCompressCrafting(World world, ItemStack inputType) {
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
