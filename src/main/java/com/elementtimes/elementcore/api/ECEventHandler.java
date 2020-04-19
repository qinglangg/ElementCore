package com.elementtimes.elementcore.api;

import com.elementtimes.elementcore.api.misc.wrapper.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;

/**
 * @author luqin2007
 */
public class ECEventHandler {

    private ECModContainer mContainer;

    public ECEventHandler(ECModContainer container) {
        mContainer = container;
        // fml
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::doClientStuff);
        bus.register(this);
        MinecraftForge.EVENT_BUS.register(new ForgeEvent());
    }

    private ECModElements elements() {
        return mContainer.elements();
    }

    private void setup(final FMLCommonSetupEvent event) {
        ECModElements elements = elements();
        elements.warn("[{}]Setup: ", mContainer.id());
        BlockFeatureWrapper.registerAll(elements);
        CapabilityWrapper.registerAll(elements);
        EntitySpawnWrapper.registerAll(elements);
        NetSimpleWrapper.registerAll(elements);
        NetEventWrapper.registerAll(elements);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ECModElements elements = elements();
        elements.warn("[{}]Client: ", mContainer.id());
        elements.config.applyClient(mContainer);
        BlockTerWrapper.registerAll(elements);
        EntityRendererWrapper.registerAll(elements);
        ScreenWrapper.registerAll(elements);
        KeyWrapper.registerAll(elements);
    }

    @SubscribeEvent
    public void onBlock(RegistryEvent.Register<Block> event) {
        registerAndLog(elements().blocks, event.getRegistry(), "Blocks");
    }

    @SubscribeEvent
    public void onTileEntityType(RegistryEvent.Register<TileEntityType<?>> event) {
        registerAndLog(elements().teTypes, event.getRegistry(), "TileEntityType");
    }

    @SubscribeEvent
    public void onItem(RegistryEvent.Register<Item> event) {
        ECModElements elements = elements();
        registerAndLog(elements.items, event.getRegistry(), "Item");
        registerAndLog(elements.blockItems, event.getRegistry(), "Item[BlockItem]");
        registerAndLog(elements.buckets, event.getRegistry(), "Item[Bucket]");
        registerAndLog(elements.entityEggs, event.getRegistry(), "Item[EntityEgg]");
    }

    @SubscribeEvent
    public void onEnchantment(RegistryEvent.Register<Enchantment> event) {
        registerAndLog(elements().enchantments, event.getRegistry(), "Enchantment");
    }

    @SubscribeEvent
    public void onEntity(RegistryEvent.Register<EntityType<?>> event) {
        registerAndLog(elements().entities, event.getRegistry(), "EntityType");
    }

    @SubscribeEvent
    public void onFluid(RegistryEvent.Register<Fluid> event) {
        registerAndLog(elements().fluids, event.getRegistry(), "Fluid");
    }

    @SubscribeEvent
    public void onPotion(RegistryEvent.Register<Potion> event) {
        registerAndLog(elements().potions, event.getRegistry(), "Potion");
    }

    @SubscribeEvent
    public void onEffect(RegistryEvent.Register<Effect> event) {
        registerAndLog(elements().effects, event.getRegistry(), "Effect");
    }

    @SubscribeEvent
    public void onContainerType(RegistryEvent.Register<ContainerType<?>> event) {
        registerAndLog(elements().containerTypes, event.getRegistry(), "ContainerType");
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onColor(net.minecraftforge.client.event.ColorHandlerEvent.Item event) {
        ItemColorWrapper.registerAll(elements(), event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onColor(net.minecraftforge.client.event.ColorHandlerEvent.Block event) {
        BlockColorWrapper.registerAll(elements(), event);
    }

    private <U extends T, T extends IForgeRegistryEntry<T>> void registerAndLog(Collection<U> elements, IForgeRegistry<T> registry, String title) {
        ECModElements logger = elements();
        logger.warn("[{}]Register: {}({})", mContainer.id(), title, elements.size());
        for (T element : elements) {
            logger.warn("  {}: {}", element.getClass().getSimpleName(), element.getRegistryName());
            registry.register(element);
        }
    }

    public class ForgeEvent {

        @SubscribeEvent
        public void onBurnTime(FurnaceFuelBurnTimeEvent event) {
            elements().burnTimes.forEach(w -> w.apply(elements(), event));
        }

        @SubscribeEvent
        public void onTooltips(ItemTooltipEvent event) {
            elements().tooltips.forEach(w -> w.apply(elements(), event));
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public void onKeyPress(net.minecraftforge.client.event.InputEvent.KeyInputEvent event) {
            elements().keys.forEach(k -> k.testPress(elements(), event));
        }

        @SubscribeEvent
        public void onServerStarting(FMLServerStartingEvent event) {
            CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
            ECModElements elements = elements();
            elements.warn("[{}]Commands({})", mContainer.id(), elements.commands.size());
            elements.commands.forEach(c -> addCommands(elements, dispatcher, c));
            elements.warn("[{}]Command Builders({})", mContainer.id(), elements.commandBuilders.size());
            elements.commandBuilders.forEach(c -> addCommandBuilders(elements, dispatcher, c));
        }

        private void addCommands(ECModElements elements, CommandDispatcher<CommandSource> dispatcher, CommandNode<CommandSource> node) {
            elements.warn("  {}", node.getName());
            dispatcher.getRoot().addChild(node);
        }

        private void addCommandBuilders(ECModElements elements, CommandDispatcher<CommandSource> dispatcher, LiteralArgumentBuilder<CommandSource> builder) {
            elements.warn("  {}", builder.getLiteral());
            dispatcher.register(builder);
        }
    }
}
