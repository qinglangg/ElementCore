package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModRecipe;
import com.elementtimes.elementcore.annotation.other.DamageIngredient;
import com.elementtimes.elementcore.item.CoreItems;
import com.elementtimes.elementcore.util.ItemUtil;
import com.elementtimes.elementcore.util.RecipeUtil;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 加载合成表
 * 处理所有 ModRecipe 注解的成员
 * 目前只有一个可用 ModRecipe.Ore
 *
 * @author luqin2007
 */
public class ModRecipeLoader {

    /**
     * 获取所有合成表
     */
    public static void load(World world, AnnotationInitializer initializer) {
        initializer.elements.get(ModRecipe.class).forEach(element -> {
            Object obj = ReflectUtil.getFromAnnotated(element, null, initializer).orElse(null);
            if (obj == null) {
                return;
            }

            addOreRecipe(world, obj, element, initializer);
            String name = ReflectUtil.getName(element).orElse(null);
            addCraftingRecipe(element, name, initializer);
        });
    }

    private static void addCraftingRecipe(AnnotatedElement element, String name, AnnotationInitializer initializer) {
        ModRecipe.Crafting info = element.getAnnotation(ModRecipe.Crafting.class);
        if (info != null) {
            initializer.recipes.add(() -> {
                String rName = info.value().isEmpty() ? name : info.value();
                Object obj = ReflectUtil.getFromAnnotated(element, null, initializer).orElse(null);
                if (obj instanceof Supplier) {
                    Object o2 = ((Supplier) obj).get();
                    if (o2 instanceof IRecipe) {
                        IRecipe iRecipe = (IRecipe) o2;
                        return new IRecipe[] {iRecipe};
                    } else if (o2 instanceof IRecipe[]) {
                        return (IRecipe[]) o2;
                    } else if (o2 instanceof Collection) {
                        return (IRecipe[]) ((Collection) o2).toArray(new IRecipe[0]);
                    } else {
                        initializer.warn("You annotated a Supplier but it can't provide a Recipe.");
                    }
                } else if (obj instanceof IRecipe) {
                    IRecipe iRecipe = (IRecipe) obj;
                    return new IRecipe[] {iRecipe};
                } else if (obj instanceof Object[] || obj instanceof Collection) {
                    Object[] objects;
                    if (obj instanceof Object[]) {
                        objects = (Object[]) obj;
                    } else {
                        objects = ((Collection) obj).toArray();
                    }
                    if (objects.length > 0) {
                        if (objects[0] instanceof IRecipe | objects[0] instanceof Supplier) {
                            // 合成表数组
                            IRecipe[] recipes = new IRecipe[objects.length];
                            for (int i = 0; i < objects.length; i++) {
                                Object object = objects[i];
                                if (object instanceof IRecipe) {
                                    recipes[i] = (IRecipe) object;
                                } else if (object instanceof Supplier) {
                                    Object o = ((Supplier) object).get();
                                    if (o instanceof IRecipe) {
                                        recipes[i] = (IRecipe) o;
                                    } else {
                                        initializer.warn("You put a Supplier into an array or collection, but it can't provider a Recipe.");
                                    }
                                } else {
                                    initializer.warn("You want put other element in a Recipe/Supplier array or collection.");
                                }
                            }
                            return recipes;
                        }
						// 单一合成表
						Object result = objects[0];
						ItemStack r;
						if (result instanceof Item) {
						    r = new ItemStack((Item) result);
						} else if (result instanceof Block) {
						    r = new ItemStack((Block) result);
						} else if (result instanceof ItemStack) {
						    r = (ItemStack) result;
						} else if (result instanceof Ingredient) {
						    r = ((Ingredient) result).getMatchingStacks()[0];
						} else if (result instanceof String && ((String) result).contains(":")){
						    Item resultItem = ItemUtil.getByNameOrId((String) result);
						    if (resultItem == null || resultItem == Items.AIR || resultItem == Blocks.AIR.asItem()) {
                                initializer.warn("The recipe {} return an NULL item.", rName);
						        return null;
						    }
						    r = new ItemStack(resultItem);
						} else {
						    r = RecipeUtil.getIngredient(result).getMatchingStacks()[0];
						}
						IRecipe recipe;
						int size = info.width() * info.height();
						if (size < objects.length - 1) {
                            initializer.warn("You want to register a recipe({}) with {} items, but you put {} items in it. Some items will ignore.",
						            rName, size, objects.length - 1);
						}
                        ResourceLocation id = new ResourceLocation(initializer.modInfo.modid, rName);
						NonNullList<Ingredient> list = NonNullList.withSize(size, Ingredient.EMPTY);
                        for (int i = 1; i < objects.length; i++) {
						    Object o = objects[i];
						    if (i - 1 >= size) {
                                initializer.warn("Ignore item[()]: {}.", i - 1, o);
						        break;
						    }
						    if (o instanceof String && ((String) o).contains(":")) {
						        Item item = ItemUtil.getByNameOrId((String) o);
						        if (item == null || item == Items.AIR) {
                                    initializer.warn("The recipe {} have an NULL item.", rName);
						            return null;
						        }
						        o = new ItemStack(item);
						    }
						    list.set(i - 1, RecipeUtil.getIngredient(o == null ? ItemStack.EMPTY : o));
						}
						if (info.shaped()) {
                            recipe = new ShapedRecipe(id, initializer.modInfo.modid, info.width(), info.height(), list, r);
						} else {
						    recipe = new ShapelessRecipe(id, initializer.modInfo.modid, r, list);
						}
						return new IRecipe[] {recipe};
                    }
                    initializer.warn("You want to convert an EMPTY array/collection to IRecipe!!!");
                }
                return null;
            });
        }
    }

    private static void addOreRecipe(World world, Object ore, AnnotatedElement element, AnnotationInitializer initializer) {
        // 矿物锤合成
        ModRecipe.Ore info = element.getAnnotation(ModRecipe.Ore.class);
        final String logOreName = "logWood";
        if (info != null) {
            if (logOreName.equals(ore)) {
                // 对原木进行特殊处理
                Map<ItemStack, ItemStack> itemStacks = new LinkedHashMap<>();
                RecipeUtil.collectOneBlockCraftingResult(world, "logWood", itemStacks);
                for (Map.Entry<ItemStack, ItemStack> entry: itemStacks.entrySet()) {
                    initializer.recipes.add(() -> {
                        NonNullList<Ingredient> input = NonNullList.create();
                        ItemStack output = entry.getValue().copy();
                        input.add(Ingredient.fromStacks(entry.getKey()));
                        ShapelessRecipe recipe = getRecipeHammer(input, output, info.damage(), info.value()
                                + "_" + Objects.requireNonNull(output.getItem().getRegistryName()).getNamespace()
                                + "_" + output.getItem().getRegistryName().getPath()
                                + "_" + output.getDamage(), initializer);
                        return new IRecipe[] {recipe};
                    });
                }
            } else {
                initializer.recipes.add(() -> {
                    NonNullList<Ingredient> input = NonNullList.create();
                    ItemStack[] matchingStacks = RecipeUtil.getIngredient(info.output()).getMatchingStacks();
                    if (matchingStacks.length == 0) {
                        return null;
                    }
                    ItemStack output = matchingStacks[0].copy();
                    input.add(RecipeUtil.getIngredient(ore));
                    output.setCount(info.dustCount());
                    ShapelessRecipe recipe = getRecipeHammer(input, output, info.damage(), info.value(), initializer);
                    return new IRecipe[] {recipe};
                });
            }
        }
    }

    private static ShapelessRecipe getRecipeHammer(NonNullList<Ingredient> input, ItemStack output, int damage, String name, AnnotationInitializer initializer) {
        input.add(DamageIngredient.create(new Item[] {CoreItems.smallHammer, CoreItems.bigHammer}, damage));
        return new ShapelessRecipe(new ResourceLocation(initializer.modInfo.modid, name), initializer.modInfo.modid, output, input);
    }
}
