package com.elementtimes.elementcore.api.common.events;

import com.elementtimes.elementcore.api.book.ItemBook;
import com.elementtimes.elementcore.api.common.loader.BookLoader;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于 EC 的事件
 * @author luqin2007
 */
@Mod.EventBusSubscriber
public class CoreEvent {

    public static final Map<ResourceLocation, ItemBook> BOOK_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onItem(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        BookLoader.BOOKS.forEach(book -> {
            ItemBook bookItem = book.createItem();
            registry.register(bookItem);
            BOOK_MAP.put(book.getId(), bookItem);
        });
    }

    @SubscribeEvent
    public static void onRecipe(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();
        BookLoader.BOOKS.forEach(book -> {
            IRecipe recipe = book.createRecipe(BOOK_MAP.get(book.getId()));
            if (recipe != null) {
                if (recipe.getRegistryName() == null) {
                    recipe.setRegistryName(book.getId());
                }
                registry.register(recipe);
            }
        });
    }
}
