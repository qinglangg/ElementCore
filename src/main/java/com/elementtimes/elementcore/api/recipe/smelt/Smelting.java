package com.elementtimes.elementcore.api.recipe.smelt;

import com.elementtimes.elementcore.api.annotation.ModRecipeSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * 等同于 minecraft:smelting，多了一个 count 属性，int 类型，为输出物品的数量
 * @author luqin2007
 */
@ModRecipeSerializer
public class Smelting extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FurnaceRecipe> {

    @Override
    public FurnaceRecipe read(ResourceLocation recipeId, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");
        JsonElement jsonIngredient = JSONUtils.isJsonArray(json, "ingredient")
                ? JSONUtils.getJsonArray(json, "ingredient")
                : JSONUtils.getJsonObject(json, "ingredient");
        Ingredient ingredient = Ingredient.deserialize(jsonIngredient);
        String result = JSONUtils.getString(json, "result");
        ResourceLocation resourcelocation = new ResourceLocation(result);
        int count = JSONUtils.getInt(json, "count", 1);
        ItemStack itemstack = new ItemStack(Registry.ITEM.getValue(resourcelocation).orElseThrow(() ->
                new IllegalStateException("Item: " + result + " does not exist")), count);
        float exp = JSONUtils.getFloat(json, "experience", 0.0F);
        int time = JSONUtils.getInt(json, "cookingtime", 200);
        return new FurnaceRecipe(recipeId, group, ingredient, itemstack, exp, time);
    }

    @Override
    public FurnaceRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        String s = buffer.readString(32767);
        Ingredient input = Ingredient.read(buffer);
        ItemStack result = buffer.readItemStack();
        float experience = buffer.readFloat();
        int cookTime = buffer.readVarInt();
        return new FurnaceRecipe(recipeId, s, input, result, experience, cookTime);
    }

    @Override
    public void write(PacketBuffer buffer, FurnaceRecipe recipe) {
        buffer.writeString(recipe.getGroup());
        recipe.getIngredients().get(0).write(buffer);
        buffer.writeItemStack(recipe.getRecipeOutput());
        buffer.writeFloat(recipe.getExperience());
        buffer.writeVarInt(recipe.getCookTime());
    }
}
