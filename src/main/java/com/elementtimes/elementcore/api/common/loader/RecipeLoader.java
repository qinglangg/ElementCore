package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.ModRecipe;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class RecipeLoader {

    public static void load(ECModElements elements) {
        loadRecipeField(elements);
        loadRecipeMethod(elements);
    }

    private static void loadRecipeField(ECModElements elements) {
        ObjHelper.stream(elements, ModRecipe.class).forEach(data -> {
            ObjHelper.find(elements, Object.class, data).ifPresent(obj -> {
                String name = ObjHelper.getDefault(data, data.getObjectName());
                if (obj instanceof IRecipe) {
                    IRecipe recipe = (IRecipe) obj;
                    if (recipe.getRegistryName() == null) {
                        recipe.setRegistryName(new ResourceLocation(elements.container.id(), name));
                    }
                    elements.recipes.add(() -> new IRecipe[] {recipe});
                } else if (obj instanceof Supplier) {
                    Supplier<?> supplier = (Supplier<?>) obj;
                    elements.recipes.add(() -> {
                        Object o = supplier.get();
                        if (o instanceof IRecipe) {
                            IRecipe recipe = (IRecipe) o;
                            if (recipe.getRegistryName() == null) {
                                recipe.setRegistryName(new ResourceLocation(elements.container.id(), name));
                            }
                            return new IRecipe[] {recipe};
                        }
                        return new IRecipe[0];
                    });
                } else if (obj instanceof Object[] && ((Object[]) obj).length > 0) {
                    Object[] objects = (Object[]) obj;
                    if (objects[0] instanceof IRecipe || objects[0] instanceof Supplier) {
                        elements.recipes.add(() -> {
                            int id = 0;
                            List<IRecipe> recipes = new ArrayList<>();
                            for (Object o : objects) {
                                IRecipe recipe;
                                if (o instanceof IRecipe) {
                                    recipe = (IRecipe) o;
                                } else if (o instanceof Supplier) {
                                    recipe = (IRecipe) ((Supplier<?>) o).get();
                                } else {
                                    recipe = null;
                                }
                                if (recipe != null) {
                                    if (recipe.getRegistryName() == null) {
                                        recipe.setRegistryName(new ResourceLocation(elements.container.id(), name + (id++)));
                                    }
                                    recipes.add(recipe);
                                }
                            }
                            return recipes.toArray(new IRecipe[0]);
                        });
                    } else {
                        Map<String, Object> info = data.getAnnotationInfo();
                        boolean shaped = (boolean) info.getOrDefault("shaped", true);
                        boolean ore = (boolean) info.getOrDefault("ore", true);
                        int width = (int) info.getOrDefault("width", 3);
                        int height = (int) info.getOrDefault("height", 3);
                        // 单一合成表
                        elements.recipes.add(() -> {
                            ItemStack r;
                            Object obj0 = objects[0];
                            if (obj0 instanceof Item) {
                                r = new ItemStack((Item) obj0);
                            } else if (obj0 instanceof Block) {
                                r = new ItemStack((Block) obj0);
                            } else if (obj0 instanceof ItemStack) {
                                r = (ItemStack) obj0;
                            } else if (obj0 instanceof Ingredient) {
                                r = ((Ingredient) obj0).getMatchingStacks()[0];
                            } else if (obj0 instanceof String && ((String) obj0).contains(":")) {
                                Item resultItem = Item.getByNameOrId((String) obj0);
                                if (resultItem == null || resultItem == Items.AIR) {
                                    elements.warn("The recipe {} return an NULL item.", name);
                                    r = ItemStack.EMPTY;
                                } else {
                                    r = new ItemStack(resultItem);
                                }
                            } else {
                                try {
                                    Ingredient ingredient = CraftingHelper.getIngredient(obj0);
                                    if (ingredient == null) {
                                        r = ItemStack.EMPTY;
                                    } else {
                                        ItemStack[] stacks = ingredient.getMatchingStacks();
                                        if (stacks.length == 0) {
                                            r = ItemStack.EMPTY;
                                        } else {
                                            r = stacks[0];
                                        }
                                    }
                                } catch (Exception e) {
                                    r = ItemStack.EMPTY;
                                }
                            }
                            IRecipe recipe;
                            CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
                            int size = width * height;
                            primer.input = NonNullList.withSize(size, Ingredient.EMPTY);
                            primer.width = width;
                            primer.height = height;
                            if (size < objects.length - 1) {
                                elements.warn("You want to register a recipe({}) with {} items, but you put {} items in it. Some items will ignore.", name, size, objects.length - 1);
                            }
                            for (int i = 1; i < objects.length; i++) {
                                Object o = objects[i];
                                if (i - 1 >= size) {
                                    elements.warn("Ignore item[{}]: {}.", i - 1, o);
                                    break;
                                }
                                if (o instanceof String && ((String) o).contains(":")) {
                                    Item item = Item.getByNameOrId((String) o);
                                    if (item == null || item == Items.AIR) {
                                        elements.warn("The recipe {} have an NULL item.", name);
                                        o = ItemStack.EMPTY;
                                    } else {
                                        o = new ItemStack(item);
                                    }
                                }
                                primer.input.set(i - 1, CraftingHelper.getIngredient(o == null ? ItemStack.EMPTY : o));
                            }
                            if (shaped) {
                                if (ore) {
                                    recipe = new ShapedOreRecipe(new ResourceLocation(elements.container.id(), "recipe"), r, primer);
                                } else {
                                    recipe = new ShapedRecipes("recipe", primer.width, primer.height, primer.input, r);
                                }
                            } else {
                                if (ore) {
                                    recipe = new ShapelessOreRecipe(new ResourceLocation(elements.container.id(), "recipe"), primer.input, r);
                                } else {
                                    recipe = new ShapelessRecipes("recipe", r, primer.input);
                                }
                            }
                            recipe.setRegistryName(new ResourceLocation(elements.container.id(), name));
                            return new IRecipe[]{recipe};
                        });
                    }
                }
            });
        });
    }

    private static void loadRecipeMethod(ECModElements elements) {
        ObjHelper.stream(elements, ModRecipe.RecipeMethod.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                String objectName = data.getObjectName();
                String methodName = objectName.substring(0, objectName.indexOf("()"));
                Method method = ReflectionHelper.findMethod(aClass, methodName, methodName);
                method.setAccessible(true);
                elements.recipes.add(() -> {
                    try {
                        Object o = method.invoke(null);
                        if (o instanceof Supplier) {
                            o = ((Supplier<?>) o).get();
                        }
                        if (o instanceof IRecipe[]) {
                            return (IRecipe[]) o;
                        } else if (o instanceof IRecipe) {
                            return new IRecipe[] { (IRecipe) o };
                        } else if (o instanceof Collection) {
                            ArrayList<IRecipe> list = new ArrayList<>();
                            for (Object obj : ((Collection<?>) o)) {
                                if (obj instanceof IRecipe) {
                                    list.add((IRecipe) obj);
                                }
                            }
                            return list.toArray(new IRecipe[0]);
                        } else {
                            return new IRecipe[0];
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return new IRecipe[0];
                    }
                });
            });
        });
    }
}
