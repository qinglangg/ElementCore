package com.elementtimes.elementcore.api.common.events;

import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 注解注册
 *
 * @author luqin2007
 */
public class ForgeRegister {

    private ECModContainer mContainer;
    private Set<Class<?>> teClassSet = new HashSet<>();

    public ForgeRegister(ECModContainer container) {
        mContainer = container;
    }

    private ECModElements elements() {
        return mContainer.elements();
    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            ECModElements elements = elements();
            elements.blocks.forEach(block -> {
                registry.register(block);
                // te
                if (elements.blockTileEntities.containsKey(block)) {
                    Class<? extends TileEntity> teClass = elements.blockTileEntities.get(block).right;
                    if (!teClassSet.contains(teClass)) {
                        GameRegistry.registerTileEntity(teClass, new ResourceLocation(mContainer.id(), elements.blockTileEntities.get(block).left));
                        teClassSet.add(teClass);
                    }
                }
            });
            elements.blockTileEntitiesNull.forEach((s, teClass) -> {
                if (!teClassSet.contains(teClass)) {
                    GameRegistry.registerTileEntity(teClass, new ResourceLocation(mContainer.id(), s));
                    teClassSet.add(teClass);
                }
            });
            elements.fluidBlocks.keySet().forEach(fluid -> registry.register(fluid.getBlock()));
        }, event);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            ECModElements elements = elements();
            elements.items.forEach(registry::register);
            elements.blocks.forEach(block -> {
                ItemBlock itemBlock = new ItemBlock(block);
                itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
                registry.register(itemBlock);
            });

            elements.blockOreNames.forEach((oreName, blocks) -> {
                for (Block block : blocks) {
                    OreDictionary.registerOre(oreName, block);
                }
            });

            elements.itemOreNames.forEach((oreName, items) -> {
                for (Item item : items) {
                    OreDictionary.registerOre(oreName, item);
                }
            });
        }, event);
    }

    @SubscribeEvent
    public void registerRecipe(RegistryEvent.Register<IRecipe> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<IRecipe> registry = event.getRegistry();
            elements().recipes.forEach(getter ->
                    Arrays.stream(getter.get()).filter(Objects::nonNull).forEach(elements().allRecipes::add));
            elements().allRecipes.forEach(registry::register);
        }, event);
    }

    @SubscribeEvent
    public void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<Enchantment> registry = event.getRegistry();
            for (Enchantment enchantment : elements().enchantments) {
                registry.register(enchantment);
            }
        }, event);
    }

    @SubscribeEvent
    public void registerPotion(RegistryEvent.Register<Potion> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<Potion> registry = event.getRegistry();
            for (Potion potion : elements().potions) {
                registry.register(potion);
            }
        }, event);
    }

    @SubscribeEvent
    public void registerPotionType(RegistryEvent.Register<PotionType> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<PotionType> registry = event.getRegistry();
            for (PotionType potionType : elements().potionTypes) {
                registry.register(potionType);
            }
        }, event);
    }

    @SubscribeEvent
    public void registerEntity(RegistryEvent.Register<EntityEntry> event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            IForgeRegistry<EntityEntry> registry = event.getRegistry();
            String id = mContainer.id();
            elements().entities.forEach(data -> registry.register(data.toEntry(id)));
        }, event);
    }
}
