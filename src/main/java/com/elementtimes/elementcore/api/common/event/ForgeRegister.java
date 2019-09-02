package com.elementtimes.elementcore.api.common.event;

import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.Objects;

/**
 * 注解注册
 *
 * @author luqin2007
 */
public class ForgeRegister {

    private ECModElements mElements;

    public ForgeRegister(ECModElements elements) {
        mElements = elements;
    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            mElements.blocks.values().forEach(registry::register);
            mElements.fluidBlocks.keySet().forEach(fluid -> registry.register(fluid.getBlock()));
        }, event);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            mElements.items.values().forEach(registry::register);
            mElements.blocks.values().forEach(block -> {
                ItemBlock itemBlock = new ItemBlock(block) {
                    @Override
                    public int getItemBurnTime(ItemStack itemStack) {
                        return mElements.blockBurningTimes.getOrDefault(block, 0);
                    }
                };
                //noinspection ConstantConditions
                itemBlock.setRegistryName(block.getRegistryName());
                registry.register(itemBlock);
                if (mElements.blockTileEntities.containsKey(block)) {
                    GameRegistry.registerTileEntity(mElements.blockTileEntities.get(block).right, new ResourceLocation(mElements.container.id(), mElements.blockTileEntities.get(block).left));
                }
            });
        }, event);
    }

    @SubscribeEvent
    public void registerRecipe(RegistryEvent.Register<IRecipe> event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            IForgeRegistry<IRecipe> registry = event.getRegistry();
            mElements.recipes.forEach(getter ->
                    Arrays.stream(getter.get()).filter(Objects::nonNull).forEach(registry::register));
        }, event);
    }

    @SubscribeEvent
    public void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        ECUtils.common.runWithModActive(mElements.container.mod, () -> {
            IForgeRegistry<Enchantment> registry = event.getRegistry();
            for (Enchantment enchantment : mElements.enchantments) {
                registry.register(enchantment);
            }
        }, event);
    }
}
