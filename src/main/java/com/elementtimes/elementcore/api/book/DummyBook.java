package com.elementtimes.elementcore.api.book;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DummyBook implements IBook {

    public static IBook INSTANCE = new DummyBook();

    private final ResourceLocation id = new ResourceLocation("", "");
    private final ITextComponent name = new TextComponentString("");
    private final List<ITextComponent> tooltips = Collections.emptyList();
    private final ItemBook book = new ItemBook(this);
    private final IRecipe recipe = null;
    private final ArrayList<Page> page = new ArrayList<>();

    @Nonnull
    @Override
    public ResourceLocation getId() { return id; }

    @Nullable
    @Override
    public ITextComponent getBookName(ItemStack bookStack) { return name; }

    @Nullable
    @Override
    public List<ITextComponent> getTooltips(ItemStack bookStack) { return tooltips; }

    @Nonnull
    @Override
    public ItemBook createItem() { return book; }

    @Nullable
    @Override
    public IRecipe createRecipe(ItemBook book) { return recipe; }

    @Override
    public ArrayList<Page> getPages() { return page; }
}
