package com.elementtimes.elementcore.api.client.event;

import com.elementtimes.elementcore.api.common.events.CoreEvent;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CoreClientEvent {

    @SubscribeEvent
    public static void model(ModelRegistryEvent event) {
        CoreEvent.BOOK_MAP.values().forEach(book -> {
            ModelLoader.setCustomModelResourceLocation(book, 0, new ModelResourceLocation(book.getRegistryName(), "inventory"));
        });
    }
}
