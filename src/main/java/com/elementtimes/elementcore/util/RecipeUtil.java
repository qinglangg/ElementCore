package com.elementtimes.elementcore.util;

import com.elementtimes.elementcore.ElementCore;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * 用于合成的工具类
 * annotation 包以后怕是会独立出去，所以尽量不会依赖包外的东西
 * @author luqin2007
 */
public class RecipeUtil {

    public static final CraftingInventory tempCrafting = new CraftingInventory(new Container(ContainerType.CRAFTING, -1) {
        @Override
        public boolean canInteractWith(PlayerEntity playerIn) { return false; }
        @Override
        public void onCraftMatrixChanged(IInventory inventoryIn) { }
    }, 3, 3);

    public static Ingredient getIngredient(Object obj) {
        try {
            if (obj instanceof String) {
                String oreOrName = (String) obj;
                // [tag] 开头 强制矿辞
                if (oreOrName.startsWith("[tag]")) {
                    oreOrName = oreOrName.substring(5).trim();
                    return Ingredient.fromTag(new ItemTags.Wrapper(new ResourceLocation(oreOrName)));
                }
                // [id] 开头 强制 RegistryName
                if (oreOrName.startsWith("[id]")) {
                    oreOrName = oreOrName.substring(4).trim();
                    return Ingredient.fromStacks(ItemUtil.getItemStack(oreOrName));
                }
                // 否则 先测试矿辞 失败则使用 RegistryName
                Ingredient ingredient = Ingredient.fromTag(new ItemTags.Wrapper(new ResourceLocation(oreOrName)));
                ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                if (matchingStacks.length == 0) {
                    return Ingredient.fromStacks(ItemUtil.getItemStack(oreOrName));
                }
                return ingredient;
            } else if (obj instanceof Ingredient) {
                return (Ingredient) obj;
            } else if (obj instanceof ItemStack) {
                return Ingredient.fromStacks(((ItemStack)obj).copy());
            } else if (obj instanceof Item) {
                return Ingredient.fromItems((IItemProvider) obj);
            } else if (obj instanceof Block) {
                return Ingredient.fromStacks(new ItemStack(((Block) obj).asItem()));
            } else if (obj instanceof ResourceLocation) {
                return Ingredient.fromTag(new ItemTags.Wrapper((ResourceLocation) obj));
            } else {
                return Ingredient.EMPTY;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Ingredient.EMPTY;
        }
    }

    public static void collectOneBlockCraftingResult(World world, String oreName, Map<ItemStack, ItemStack> receiver) {
        Arrays.stream(getIngredient(oreName).getMatchingStacks())
                .filter(stack -> !stack.isEmpty() && Block.getBlockFromItem(stack.getItem()) != Blocks.AIR)
                .map(stack -> ItemHandlerHelper.copyStackWithSize(stack, 1))
                .forEach(stack -> receiver.put(stack, RecipeUtil.getCraftingResult(world, NonNullList.withSize(1, stack))));
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

        for (IRecipe<?> recipe : world.getRecipeManager().getRecipes()) {
            if (recipe.getType().equals(IRecipeType.CRAFTING)) {
                Method[] methods = recipe.getClass().getMethods();
                boolean matches = false;
                for (Method method : methods) {
                    if (method.getName().equals("matches") && method.getParameterCount() == 2) {
                        Optional<Object> matchesOpt = ReflectUtil.invoke(method, recipe, ElementCore.INITIALIZER);
                        matchesOpt.orElse(false);
                        break;
                    }
                }
                if (matches) {
                    for (Method method : methods) {
                        if (method.getName().equals("getCraftingResult") && method.getParameterCount() == 1) {
                            Optional<Object> invoke = ReflectUtil.invoke(method, recipe, new Object[]{tempCrafting}, ElementCore.INITIALIZER);
                            return (ItemStack) invoke.filter(i -> i instanceof ItemStack).orElse(ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
