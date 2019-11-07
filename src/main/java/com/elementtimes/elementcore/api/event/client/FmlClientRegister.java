package com.elementtimes.elementcore.api.event.client;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class FmlClientRegister {

    private ECModElements mElements;

    public FmlClientRegister(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void registerGui(FMLClientSetupEvent event) {
        mElements.client().containers.screenFactories().forEach(this::registerGui);
    }

    private void registerGui(Object key, ScreenManager.IScreenFactory factory) {
        if (key instanceof ContainerType) {
            ScreenManager.registerFactory((ContainerType) key, factory);
        } else {
            String nameKey = key.toString();
            ResourceLocation name;
            if (nameKey.contains(":")) {
                name = new ResourceLocation(nameKey.toLowerCase());
            } else {
                name = new ResourceLocation(mElements.id(), nameKey.toLowerCase());
            }
            Registry.MENU.getValue(name).ifPresent(type -> ScreenManager.registerFactory(type, factory));
        }
    }
}
