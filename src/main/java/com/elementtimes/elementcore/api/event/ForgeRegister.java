package com.elementtimes.elementcore.api.event;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 注解注册
 * @author luqin2007
 */
public class ForgeRegister {

    private ECModElements elements;

    public ForgeRegister(ECModElements elements) {
        this.elements = elements;
    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        elements.blocks.blocks().values().forEach(registry::register);
        BlockTags.getCollection().registerAll(ECUtils.tag.toRegisterMap(elements.blocks.tags()));
        elements.elements.load();
    }

    @SubscribeEvent
    public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
        elements.tileEntityTypes.types().forEach(registry::register);
    }

    @SubscribeEvent
    public void registerGui(RegistryEvent.Register<ContainerType<?>> event) {
        IForgeRegistry<ContainerType<?>> registry = event.getRegistry();
        elements.containerTypes.types().forEach(registry::register);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        elements.items.items().values().forEach(registry::register);
        for (Block block : elements.blocks.blocks().values()) {
            ItemGroup group = LoaderHelper.getGroup(elements, elements.blocks.groups().get(block.getRegistryName().toString()));
            Item.Properties builder = new Item.Properties();
            if (group != null) {
                builder.group(group);
            }
            registry.register(new BlockItem(block, builder).setRegistryName(Objects.requireNonNull(block.getRegistryName())));
        }
        elements.fluids.fluids().values().stream().map(Fluid::getFilledBucket).forEach(registry::register);
        Map<String, List<Item>> itemTag = new HashMap<>(elements.items.tags());
        elements.blocks.tagItems().forEach((name, blocks) -> {
            List<Item> list = blocks.stream().map(Block::asItem).collect(Collectors.toList());
            itemTag.computeIfAbsent(name, (s) -> new ArrayList<>()).addAll(list);
        });
        elements.fluids.tagItems().forEach((name, blocks) -> {
            List<Item> list = blocks.stream().map(Fluid::getFilledBucket).collect(Collectors.toList());
            itemTag.computeIfAbsent(name, (s) -> new ArrayList<>()).addAll(list);
        });
        ItemTags.getCollection().registerAll(ECUtils.tag.toRegisterMap(itemTag));
    }

    @SubscribeEvent
    public void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        IForgeRegistry<Enchantment> registry = event.getRegistry();
        elements.enchantments.enchantments().forEach(registry::register);
    }

    @SubscribeEvent
    public void registerFluid(RegistryEvent.Register<Fluid> event) {
        IForgeRegistry<Fluid> registry = event.getRegistry();
        elements.fluids.fluids().values().forEach(registry::register);
        FluidTags.getCollection().registerAll(ECUtils.tag.toRegisterMap(elements.fluids.tags()));
    }

    @SubscribeEvent
    public void registerRecipeSerializer(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        IForgeRegistry<IRecipeSerializer<?>> registry = event.getRegistry();
        elements.recipes.serializers().forEach(registry::register);
    }

    @SubscribeEvent
    public void registerPotion(RegistryEvent.Register<Potion> event) {
        IForgeRegistry<Potion> registry = event.getRegistry();
        elements.potions.potions().forEach(registry::register);
    }

    @SubscribeEvent
    public void registerEffect(RegistryEvent.Register<Effect> event) {
        IForgeRegistry<Effect> registry = event.getRegistry();
        elements.potions.effects().forEach(registry::register);
    }
}
