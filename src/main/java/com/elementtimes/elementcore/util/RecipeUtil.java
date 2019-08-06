package com.elementtimes.elementcore.util;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

/**
 * 用于合成的工具类
 * annotation 包以后怕是会独立出去，所以尽量不会依赖包外的东西
 * @author luqin2007
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RecipeUtil {

    private static InventoryCrafting tempCrafting = new InventoryCrafting(new Container() {
        @Override
        public boolean canInteractWith(@Nonnull EntityPlayer playerIn) { return false; }
        @Override
        public void onCraftMatrixChanged(IInventory inventoryIn) { }
    }, 3, 3);

    private static final String INGREDIENT_START_ORE = "[ore]";
    private static final String INGREDIENT_START_NAME = "[id]";

    /**
     * 根据传入的名字获取物品
     * 匹配 domain:registry:metadata
     * 可只写 registry，相当于 minecraft:registry:0
     * @param name 物品名
     * @return 物品栈
     */
    public static ItemStack getFromItemName(String name) {
        String[] split = name.split(":");
        Item item;
        int damage = 0;
        if (split.length == 1) {
            item = Item.getByNameOrId("minecraft:" + name);
        } else if (split.length == 2) {
            item = Item.getByNameOrId(name);
        } else {
            try {
                item = Item.getByNameOrId(split[0] + ":" + split[1]);
                damage = Integer.parseInt(split[2]);
            } catch (Exception e) {
                item = Item.getByNameOrId(name);
            }
        }
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
        stack.setItemDamage(damage);
        return stack;
    }

    /**
     * 根据传入的 Object 对象获取 Ingredient
     *  String
     *      以 [ore] 开头，强制使用矿辞
     *      以 [id] 开头，强制使用 registryName
     *      否则，先测试矿辞，当矿辞中不包含任何内容时，使用 registryName
     *  非 String，调用 CraftingHelper.getIngredient(object) 获取
     * @param obj 对象
     * @return Ingredient
     */
    public static Ingredient getIngredient(Object obj) {
        try {
            if (obj instanceof String) {
                String oreOrName = (String) obj;
                // [ore] 开头 强制矿辞
                if (oreOrName.startsWith(INGREDIENT_START_ORE)) {
                    oreOrName = oreOrName.substring(5);
                    return CraftingHelper.getIngredient(oreOrName);
                }
                // [id] 开头 强制 RegistryName
                if (oreOrName.startsWith(INGREDIENT_START_NAME)) {
                    oreOrName = oreOrName.substring(4);
                    return Ingredient.fromStacks(getFromItemName(oreOrName));
                }
                // 否则 先测试矿辞 失败则使用 RegistryName
                Ingredient ingredient = CraftingHelper.getIngredient(oreOrName);
                ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                if (matchingStacks.length == 0) {
                    return Ingredient.fromStacks(getFromItemName(oreOrName));
                }
                return ingredient;
            }
			return CraftingHelper.getIngredient(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Ingredient.EMPTY;
        }
    }

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
        ItemStack resultEntry = CraftingManager.findMatchingResult(tempCrafting, world).copy();
        tempCrafting.clear();
        return resultEntry;
    }

    /**
     * 获取单物品输入的输出
     * @param oreName 输入矿辞名
     * @param receiver 输出
     */
    public static void collectOneBlockCraftingResult(World world, String oreName, Map<ItemStack, ItemStack> receiver) {
        Arrays.stream(CraftingHelper.getIngredient(oreName).getMatchingStacks())
                .filter(stack -> !stack.isEmpty() && Block.getBlockFromItem(stack.getItem()) != Blocks.AIR)
                .map(stack -> ItemHandlerHelper.copyStackWithSize(stack, 1))
                .forEach(stack -> receiver.put(stack, RecipeUtil.getCraftingResult(world, NonNullList.withSize(1, stack))));
    }
}
