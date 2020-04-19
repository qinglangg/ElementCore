package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemColorWrapper {

    private Supplier<Collection<IItemProvider>> mItems;
    private Collection<IItemProvider> mItemCollection = null;
    private Object mColor;
    public String msg;

    public ItemColorWrapper(int color) {
        mColor = (net.minecraft.client.renderer.color.IItemColor) (a, b) -> color;
        msg = "#" + Integer.toHexString(color);
    }

    public ItemColorWrapper(ECModElements elements, IAnnotationRef getter) {
        if (!getter.hasContent()) {
            elements.warn("[{}]Color{} not found: {}", elements.container.id(),
                    getter.isField() ? "Field" : getter.isMethod() ? "Method" : "Class",
                    getter.getRefName());
        }
        if (getter instanceof AnnotationMethod) {
            mColor = (net.minecraft.client.renderer.color.IItemColor) (a, b) -> {
                OptionalInt color = getter.getInt(a, b);
                if (!color.isPresent()) {
                    elements.warn("[{}]ItemColor: Can't get an Int from {}, use default value 0.", elements.container.id(), msg);
                }
                return getter.getInt(a, b).orElse(0);
            };
        } else {
            Optional<net.minecraft.client.renderer.color.IItemColor> color = getter.get();
            if (!color.isPresent()) {
                elements.warn("[{}]ItemColor: Can't get an IItemColor from {}, use default value 0.", elements.container.id(), msg);
            }
            mColor = color.orElse((a, b) -> 0);
        }
        msg = getter.getRefName();
    }

    public ItemColorWrapper bind(Supplier<Collection<IItemProvider>> items) {
        mItems = items;
        return this;
    }

    public ItemColorWrapper bind(Class<?> items) {
        if (Block.class.isAssignableFrom(items)) {
            return bind(() -> ForgeRegistries.BLOCKS.getValues().stream().filter(items::isInstance).collect(Collectors.toList()));
        } else if (Item.class.isAssignableFrom(items)) {
            return bind(() -> ForgeRegistries.ITEMS.getValues().stream().filter(items::isInstance).collect(Collectors.toList()));
        } else {
            return bind((Supplier<Collection<IItemProvider>>) Collections::emptyList);
        }
    }

    public ItemColorWrapper bind(IItemProvider items) {
        return bind(() -> Collections.singleton(items));
    }

    public Collection<IItemProvider> getItems() {
        if (mItemCollection == null) {
            mItemCollection = mItems.get();
        }
        return mItemCollection;
    }

    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.renderer.color.IItemColor getItemColor() {
        return (net.minecraft.client.renderer.color.IItemColor) mColor;
    }

    @OnlyIn(Dist.CLIENT)
    public void apply(Logger logger, net.minecraftforge.client.event.ColorHandlerEvent.Item event) {
        net.minecraft.client.renderer.color.IItemColor color = getItemColor();
        if (color != null) {
            for (IItemProvider item : getItems()) {
                logger.warn("  {} <-- {}", item.asItem().getRegistryName(), color);
                event.getItemColors().register(color, item);
            }
        }
        logger.warn("  ==========");
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerAll(ECModElements elements, net.minecraftforge.client.event.ColorHandlerEvent.Item event) {
        List<ItemColorWrapper> colors = elements.itemColors;
        elements.warn("[{}]ItemColor({} groups)", elements.container.id(), colors.size());
        colors.forEach(c -> c.apply(elements, event));
    }
}
