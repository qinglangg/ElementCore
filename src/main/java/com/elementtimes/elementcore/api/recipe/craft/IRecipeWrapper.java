package com.elementtimes.elementcore.api.recipe.craft;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * @author luqin
 */
public interface IRecipeWrapper<T extends IRecipe> {

    T getRecipe();

    String getSerializerType();

    String[] getParameters();

    class ShapedRecipe extends net.minecraft.item.crafting.ShapedRecipe implements IRecipeWrapper<net.minecraft.item.crafting.ShapedRecipe> {

        private final net.minecraft.item.crafting.ShapedRecipe mRecipe;
        private final String mType;
        private final String[] mParameters;

        public ShapedRecipe(String type, net.minecraft.item.crafting.ShapedRecipe recipe, String[] parameters) {
            super(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getRecipeOutput());
            mType = type;
            mRecipe = recipe;
            mParameters = parameters;
        }

        @Override
        public net.minecraft.item.crafting.ShapedRecipe getRecipe() {
            return mRecipe;
        }

        @Override
        public String getSerializerType() {
            return mType;
        }

        @Override
        public String[] getParameters() {
            return mParameters;
        }
    }

    class ShapelessRecipe extends net.minecraft.item.crafting.ShapelessRecipe implements IRecipeWrapper<net.minecraft.item.crafting.ShapelessRecipe> {

        private final net.minecraft.item.crafting.ShapelessRecipe mRecipe;
        private final String mType;
        private final String[] mParameters;

        public ShapelessRecipe(String type, net.minecraft.item.crafting.ShapelessRecipe recipe, String[] parameters) {
            super(recipe.getId(), recipe.getGroup(), recipe.getRecipeOutput(), recipe.getIngredients());
            mType = type;
            mRecipe = recipe;
            mParameters = parameters;
        }

        @Override
        public net.minecraft.item.crafting.ShapelessRecipe getRecipe() {
            return mRecipe;
        }

        @Override
        public String getSerializerType() {
            return mType;
        }

        @Override
        public String[] getParameters() {
            return mParameters;
        }
    }

    class OtherRecipe implements IRecipeWrapper<IRecipe>, IRecipe {

        private final IRecipe mRecipe;
        private final String mType;
        private final String[] mParameters;

        public OtherRecipe(String type, IRecipe recipe, String[] parameters) {
            mType = type;
            mRecipe = recipe;
            mParameters = parameters;
        }

        @Override
        public IRecipe getRecipe() {
            return null;
        }

        @Override
        public String getSerializerType() {
            return null;
        }

        @Override
        public String[] getParameters() {
            return new String[0];
        }

        @Override
        public boolean matches(IInventory inv, World worldIn) {
            return mRecipe.matches(inv, worldIn);
        }

        @Override
        public ItemStack getCraftingResult(IInventory inv) {
            return mRecipe.getCraftingResult(inv);
        }

        @Override
        public ItemStack getRecipeOutput() {
            return mRecipe.getRecipeOutput();
        }

        @Override
        public ResourceLocation getId() {
            return mRecipe.getId();
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return mRecipe.getSerializer();
        }

        @Override
        public IRecipeType<?> getType() {
            return mRecipe.getType();
        }
    }
}
