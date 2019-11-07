package com.elementtimes.elementcore.api.event.client;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/**
 * 注解注册
 *
 * @author luqin2007
 */
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ForgeBusRegisterClient {

    private ECModElements element;

    public ForgeBusRegisterClient(ECModElements elements) {
        this.element = elements;
    }

    @SubscribeEvent
    public void registerModel(ModelRegistryEvent event) {
        // 三方渲染
        if (element.client().blocks.useOBJ()) {
            OBJLoader.INSTANCE.addDomain(element.id());
        }
        if (element.client().blocks.useB3D()) {
            B3DLoader.INSTANCE.addDomain(element.id());
        }
        // 注册TER
        element.client().blocks.ter().forEach(ClientRegistry::bindTileEntitySpecialRenderer);
    }

    @SubscribeEvent
    public void registerItemColor(ColorHandlerEvent.Item event) {
        element.client().items.colors().forEach((color, items) -> event.getItemColors().register(color, items.toArray(new Item[0])));
    }

    @SubscribeEvent
    public void registerBlockColor(ColorHandlerEvent.Block event) {
        element.client().blocks.colors().forEach((color, blocks) -> event.getBlockColors().register(color, blocks.toArray(new Block[0])));
    }
}
