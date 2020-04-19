package com.elementtimes.elementcore.api.book;

import com.elementtimes.elementcore.api.book.screen.IContent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 任务书的一种实现
 * @author luqin2007
 */
public class Book implements IBook {

    protected ResourceLocation mId;
    protected Function<ItemBook, IRecipe> mRecipe = null;
    protected ITextComponent mName = DummyBook.INSTANCE.getBookName(ItemStack.EMPTY);
    protected List<ITextComponent> mTooltips = new ArrayList<>();
    protected ArrayList<Page> mPages = new ArrayList<>();
    protected Function<ItemBook, ItemBook> mItemDecorator = this::decorateItem;
    protected Function<Book, ItemBook> mItemCreator = null;

    public Book(ResourceLocation id) {
        this.mId = id;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return mId;
    }

    @Nullable
    @Override
    public ITextComponent getBookName(ItemStack bookStack) {
        return mName;
    }

    public Book setName(String name) {
        return setName(new TextComponentString(name));
    }

    public Book setNameTranslation(String key, Object... args) {
        return setName(new TextComponentTranslation(key, args));
    }

    public Book setName(ITextComponent name) {
        mName = name;
        return this;
    }

    @Nullable
    @Override
    public List<ITextComponent> getTooltips(ItemStack bookStack) {
        return mTooltips;
    }

    public Book addTooltips(String... tooltips) {
        return addStringTooltips(Arrays.asList(tooltips));
    }

    public Book addStringTooltips(Collection<String> tooltips) {
        return addTooltips(tooltips.stream().map(TextComponentString::new).collect(Collectors.toList()));
    }

    public Book addTooltipTranslation(String tooltipKey, Object... args) {
        return addTooltips(new TextComponentTranslation(tooltipKey, args));
    }

    public Book addTooltipTranslations(String... tooltipKeys) {
        return addTooltipTranslations(Arrays.asList(tooltipKeys));
    }

    public Book addTooltipTranslations(Collection<String> tooltipKeys) {
        return addTooltips(tooltipKeys.stream().map(TextComponentTranslation::new).collect(Collectors.toList()));
    }

    public Book addTooltips(ITextComponent... tooltips) {
        return addTooltips(Arrays.asList(tooltips));
    }

    public Book addTooltips(Collection<ITextComponent> tooltips) {
        mTooltips.addAll(tooltips);
        return this;
    }

    @Nonnull
    @Override
    public ItemBook createItem() {
        ItemBook book;
        if (mItemCreator == null) {
            book = new ItemBook(this);
        } else {
            book = mItemCreator.apply(this);
        }
        return mItemDecorator.apply(book);
    }

    public Book setItemCreator(Function<Book, ItemBook> creator) {
        mItemCreator = creator;
        return this;
    }

    public Book addItemDecorator(Consumer<ItemBook> decorator) {
        if (decorator != null) {
            mItemDecorator = book -> {
                ItemBook b = mItemDecorator.apply(book);
                decorator.accept(b);
                return b;
            };
        }
        return this;
    }

    public Book addItemDecorator(Function<ItemBook, ItemBook> decorator) {
        if (decorator != null) {
            mItemDecorator = book -> {
                ItemBook b = mItemDecorator.apply(book);
                b = decorator.apply(b);
                return b;
            };
        }
        return this;
    }

    public Book setItemDecorator(Function<ItemBook, ItemBook> decorator) {
        if (decorator == null) {
            decorator = this::decorateItem;
        }
        mItemDecorator = decorator;
        return this;
    }

    protected ItemBook decorateItem(ItemBook book) {
        book.setRegistryName(getId());
        return book;
    }

    @Nullable
    @Override
    public IRecipe createRecipe(ItemBook book) {
        return mRecipe == null ? null : mRecipe.apply(book);
    }

    public Book addShapedRecipe(ResourceLocation group, Object... items) {
        return addRecipe(book -> new ShapedOreRecipe(group, book, items));
    }

    public Book addShapedRecipe(ResourceLocation group, CraftingHelper.ShapedPrimer primer) {
        return addRecipe(book -> new ShapedOreRecipe(group, new ItemStack(book), primer));
    }

    public Book addShapelessRecipe(ResourceLocation group, Object... items) {
        return addRecipe(book -> new ShapelessOreRecipe(group, book, items));
    }

    public Book addShapelessRecipe(String group, Object... items) {
        return addRecipe(book -> new ShapelessOreRecipe(new ResourceLocation(group), book, items));
    }

    public Book addRecipe(Function<ItemBook, IRecipe> recipe) {
        mRecipe = recipe;
        return this;
    }

    @Override
    public ArrayList<Page> getPages() {
        return mPages;
    }

    public Book addPage(IContent... contents) {
        Page page = new Page(this);
        if (contents.length > 0) {
            page.addAll(contents);
        }
        return addPage(page);
    }

    public Book addPage(Collection<IContent> contents) {
        Page page = new Page(this);
        page.addAll(contents);
        return addPage(page);
    }

    public Book addPage(Consumer<Page> builder) {
        Page page = new Page(this);
        builder.accept(page);
        return addPage(page);
    }

    public Book addPage(Supplier<Page> builder) {
        return addPage(builder.get());
    }

    public Book addPage(Page pages) {
        mPages.add(pages);
        return this;
    }

    public Book addPages(Page... pages) {
        Collections.addAll(mPages, pages);
        return this;
    }

    public Book addPages(Collection<Page> pages) {
        mPages.addAll(pages);
        return this;
    }

    public Book addContent(IContent content) {
        ArrayList<Page> pages = getPages();
        pages.get(pages.size() - 1).add(content);
        return this;
    }
}
