package com.elementtimes.elementcore.api.recipe.craft;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.annotation.ModRecipeSerializer;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * 返回值从方法中获取
 * 参数：
 *  override: 相当于原本的 type。
 *      Factory 会以该 type 创建 IRecipe 对象
 *      因此，该 json 应当包含 parent 对应 type 的所有元素
 *      parent 不可为 elementcore:method，会导致递归调用
 *  result {
 *      class: 方法所在类
 *      method: 物品创建方法。应当返回一个 ItemStack 对象
 *          必须是静态方法。优先选择无参方法
 *          也可以接收一个 ItemStack 类型的对象，为原本返回的值（getRecipeOutput）
 *  }
 * @author luqin2007
 */
@ModRecipeSerializer
public class Method extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<IRecipe<?>> {

    @Override
    public IRecipe<?> read(ResourceLocation recipeId, JsonObject json) {
        String override = JSONUtils.getString(json, "override");
        String type = JSONUtils.getString(json, "type");
        JsonObject object = json.getAsJsonObject("result");
        String className = object.get("class").getAsString();
        String methodName = object.get("method").getAsString();
        json.addProperty("type", override);
        IRecipe<?> recipe = findSerializer(override).read(recipeId, json);
        json.addProperty("type", type);
        return getRecipe(recipe, type, className, methodName);
    }

    @Override
    public IRecipe<?> read(ResourceLocation recipeId, PacketBuffer buffer) {
        String type = buffer.readString();
        String className = buffer.readString();
        String methodName = buffer.readString();
        IRecipeSerializer serializer = findSerializer(type);
        IRecipe recipe = serializer.read(recipeId, buffer);
        return getRecipe(recipe, type, className, methodName);
    }

    @Override
    public void write(PacketBuffer buffer, IRecipe<?> recipe) {
        if (recipe instanceof IRecipeWrapper) {
            String[] parameters = ((IRecipeWrapper) recipe).getParameters();
            String type = ((IRecipeWrapper) recipe).getSerializerType();
            IRecipeSerializer serializer = findSerializer(type);
            buffer.writeString(type);
            buffer.writeString(parameters[0]);
            buffer.writeString(parameters[1]);
            serializer.write(buffer, recipe);
        }
    }

    private ItemStack getRecipeOutput(String className, String methodName, ItemStack output) {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (method.getName().equals(methodName) && Modifier.isStatic(modifiers)) {
                    if (ItemStack.class.isAssignableFrom(method.getReturnType())) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 0) {
                            ECUtils.reflect.setAccessible(method);
                            return (ItemStack) method.invoke(null);
                        } else if (parameterTypes.length == 1 && ItemStack.class.isAssignableFrom(parameterTypes[0])) {
                            ECUtils.reflect.setAccessible(method);
                            return (ItemStack) method.invoke(null, output);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return ItemStack.EMPTY;
    }

    private IRecipeSerializer findSerializer(String type) {
        return ForgeRegistries.RECIPE_SERIALIZERS.getValue(new ResourceLocation(type));
    }

    private IRecipe getRecipe(IRecipe recipe, String type, String className, String methodName) {
        if (recipe instanceof ShapedRecipe) {
            return new IRecipeWrapper.ShapedRecipe(type, (ShapedRecipe) recipe, new String[] {className, methodName}) {
                @Override
                public ItemStack getRecipeOutput() {
                    return Method.this.getRecipeOutput(className, methodName, super.getRecipeOutput());
                }

                @Override
                public ItemStack getCraftingResult(CraftingInventory inv) {
                    return Method.this.getRecipeOutput(className, methodName, super.getCraftingResult(inv));
                }
            };
        } else if (recipe instanceof ShapelessRecipe) {
            return new IRecipeWrapper.ShapelessRecipe(type, (ShapelessRecipe) recipe, new String[] {className, methodName}) {
                @Override
                public ItemStack getRecipeOutput() {
                    return Method.this.getRecipeOutput(className, methodName, super.getRecipeOutput());
                }

                @Override
                public ItemStack getCraftingResult(CraftingInventory inv) {
                    return Method.this.getRecipeOutput(className, methodName, super.getCraftingResult(inv));
                }
            };
        } else {
            return new IRecipeWrapper.OtherRecipe(type, recipe, new String[] {className, methodName}) {
                @Override
                public ItemStack getRecipeOutput() {
                    return Method.this.getRecipeOutput(className, methodName, super.getRecipeOutput());
                }

                @Override
                public ItemStack getCraftingResult(IInventory inv) {
                    return Method.this.getRecipeOutput(className, methodName, super.getCraftingResult(inv));
                }
            };
        }
    }
}
