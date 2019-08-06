package com.elementtimes.elementcore.annotation.register;

import com.elementtimes.elementcore.ElementContainer;
import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.util.FluidUtil;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 注解注册
 *
 * @author luqin2007
 */
public class ForgeBusRegister {

    private ElementContainer mInitializer;

    public ForgeBusRegister(ElementContainer initializer) {
        mInitializer = initializer;
    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        mInitializer.blocks.values().forEach(registry::register);
        mInitializer.fluidBlocks.keySet().forEach(fluid -> registry.register(fluid.getBlock()));
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        mInitializer.items.values().forEach(item -> {
            registry.register(item);
            if (mInitializer.itemOreDictionaries.containsKey(item)) {
                OreDictionary.registerOre(mInitializer.itemOreDictionaries.get(item), item);
            }
        });
        mInitializer.blocks.values().forEach(block -> {
            ItemBlock itemBlock = new ItemBlock(block) {
                @Override
                public int getItemBurnTime(ItemStack itemStack) {
                    return mInitializer.blockBurningTimes.getOrDefault(block, 0);
                }
            };
            //noinspection ConstantConditions
            itemBlock.setRegistryName(block.getRegistryName());
            registry.register(itemBlock);
            if (mInitializer.blockOreDictionaries.containsKey(block)) {
                OreDictionary.registerOre(mInitializer.blockOreDictionaries.get(block), block);
            }
            if (mInitializer.blockTileEntities.containsKey(block)) {
                GameRegistry.registerTileEntity(mInitializer.blockTileEntities.get(block).right, new ResourceLocation(mInitializer.modInfo.id(), mInitializer.blockTileEntities.get(block).left));
            }
        });
    }

    @SubscribeEvent
    public void registerRecipe(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();
        mInitializer.recipes.forEach(getter ->
                Arrays.stream(getter.get()).filter(Objects::nonNull).forEach(registry::register));
    }

    @SubscribeEvent
    public void onBurningTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack itemStack = event.getItemStack();
        String name = null;
        if (itemStack.getItem() == ElementCore.Items.bottle) {
            name = FluidUtil.getFluid(itemStack).getFluid().getName();
        } else if (FluidRegistry.isUniversalBucketEnabled() && itemStack.getItem() == ForgeModContainer.getInstance().universalBucket) {
            Optional<IFluidTankProperties> fluidBucket = Arrays.stream(Objects.requireNonNull(net.minecraftforge.fluids.FluidUtil.getFluidHandler(itemStack)).getTankProperties()).findFirst();
            if (fluidBucket.isPresent()) {
                Fluid fluid = Objects.requireNonNull(fluidBucket.get().getContents()).getFluid();
                if (fluid != null) {
                    name = fluid.getName();
                }
            }
        }
        int time = mInitializer.fluidBurningTimes.getOrDefault(name, -1);
        if (time > 0) {
            event.setBurnTime(time);
        }
    }

    @SubscribeEvent
    public void onRegisterEnchantment(RegistryEvent.Register<Enchantment> event) {
        IForgeRegistry<Enchantment> registry = event.getRegistry();
        for (Enchantment enchantment : mInitializer.enchantments) {
            registry.register(enchantment);
        }
    }
}
