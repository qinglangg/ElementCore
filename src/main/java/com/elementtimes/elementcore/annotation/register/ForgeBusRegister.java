package com.elementtimes.elementcore.annotation.register;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 注解注册
 * @author luqin2007
 */
@SuppressWarnings("unused")
public class ForgeBusRegister {

    private AnnotationInitializer mInitializer;

    public ForgeBusRegister(AnnotationInitializer initializer) {
        mInitializer = initializer;
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "ForgeBusRegister");
    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "registerBlock");
        mInitializer.loadBlocks();
        mInitializer.loadFluids();
        IForgeRegistry<Block> registry = event.getRegistry();
        mInitializer.blocks.forEach(registry::register);
        BlockTags.getCollection().registerAll(mInitializer.blockTags);
    }

    @SubscribeEvent
    public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "registerTileEntity");
        mInitializer.loadBlocks();
        IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
        mInitializer.blockTileEntities.forEach(registry::register);
    }

    @SubscribeEvent
    public void registerFeature(RegistryEvent.Register<Feature<?>> event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "registerFeature");
        mInitializer.loadFeatures();
        IForgeRegistry<Feature<?>> registry = event.getRegistry();
        mInitializer.features.forEach(registry::register);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "registerItem");
        mInitializer.loadItems();
        mInitializer.loadBlocks();
        IForgeRegistry<Item> registry = event.getRegistry();
        mInitializer.items.forEach(registry::register);
        ItemTags.getCollection().registerAll(mInitializer.itemTags);
        mInitializer.blocks.forEach(block -> {
            Item itemBlock = new BlockItem(block, new Item.Properties()) {
                @Override
                public int getBurnTime(ItemStack itemStack) {
                    return mInitializer.blockBurningTimes.getOrDefault(block, super.getBurnTime(itemStack));
                }
            };
            //noinspection ConstantConditions
            itemBlock.setRegistryName(block.getRegistryName());
            registry.register(itemBlock);
        });
    }

    @SubscribeEvent
    public void registerFluid(RegistryEvent.Register<Fluid> event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "registerFluid");
        mInitializer.loadFluids();
        IForgeRegistry<Fluid> registry = event.getRegistry();
        mInitializer.fluids.forEach(registry::register);
        FluidTags.getCollection().registerAll(mInitializer.fluidTags);
    }

    @SubscribeEvent
    public void onBurningTime(FurnaceFuelBurnTimeEvent event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "onBurningTime");
        mInitializer.loadItems();
        mInitializer.loadBlocks();
        mInitializer.loadFluids();
        ItemStack itemStack = event.getItemStack();
        AtomicReference<String> name = new AtomicReference<>();
        if (FluidRegistry.isUniversalBucketEnabled() && itemStack.getItem() == ForgeMod.getInstance().universalBucket) {
            LazyOptional<IFluidHandlerItem> fluidHandlerOpt = net.minecraftforge.fluids.FluidUtil.getFluidHandler(itemStack);
            fluidHandlerOpt.ifPresent(handler -> {
                Optional<IFluidTankProperties> fluidBucket = Arrays.stream(handler.getTankProperties()).findFirst();
                if (fluidBucket.isPresent()) {
                    net.minecraftforge.fluids.Fluid fluid = Objects.requireNonNull(fluidBucket.get().getContents()).getFluid();
                    if (fluid != null) {
                        name.set(fluid.getName());
                    }
                }
            });
        }
        int time = mInitializer.fluidBurningTimes.getOrDefault(name, -1);
        if (time > 0) {
            event.setBurnTime(time);
        }
    }

    @SubscribeEvent
    public void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        mInitializer.warn("[{}] {}", mInitializer.modInfo.modid, "registerEnchantment");
        mInitializer.loadEnchantments();
        IForgeRegistry<Enchantment> registry = event.getRegistry();
        for (Enchantment enchantment : mInitializer.enchantments) {
            registry.register(enchantment);
        }
    }
}
