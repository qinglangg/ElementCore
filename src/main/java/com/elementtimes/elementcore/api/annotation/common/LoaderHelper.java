package com.elementtimes.elementcore.api.annotation.common;

import com.elementtimes.elementcore.api.ECModContainer;
import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * 加载的辅助类
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class LoaderHelper {

    public static Optional<Class> getOrLoadClass(@Nonnull ECModElements initializer, @Nonnull String className) {
        boolean skip = true;
        for (String packageName : initializer.packages) {
            if (className.startsWith(packageName)) {
                skip = false;
                break;
            }
        }
        if (skip) {
            return Optional.empty();
        }
        Class clazz = initializer.classes.get(className);
        if (clazz == null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                initializer.container.warn("Can't find class: {}", className);
            }
            if (clazz != null) {
                initializer.classes.put(className, clazz);
            }
        }
        return Optional.ofNullable(clazz);
    }

    public static Optional<Block> getBlock(ECModElements initializer, String className, String objectName) {
        return Optional.ofNullable(initializer.blocks.get(className, objectName == null ? "" : objectName));
    }

    public static Optional<Item> getItem(ECModElements initializer, String className, String objectName) {
        return Optional.ofNullable(initializer.items.get(className, objectName == null ? "" : objectName));
    }

    public static Optional<Fluid> getFluid(ECModElements initializer, String className, String objectName) {
        return Optional.ofNullable(initializer.fluids.get(className, objectName == null ? "" : objectName));
    }

    public static CreativeTabs getTab(String key, ECModElements initializer) {
        CreativeTabs creativeTabs = null;
        if (key != null && !key.isEmpty()) {
            creativeTabs = initializer.tabs.get(key);
            if (creativeTabs == null) {
                for (ECModContainer mod : ECModContainer.MODS.values()) {
                    creativeTabs = mod.elements.tabs.get(key);
                    if (creativeTabs != null) {
                        break;
                    }
                }
            }
            if (creativeTabs == null) {
                switch (key) {
                    case "buildingBlocks":
                        creativeTabs = CreativeTabs.BUILDING_BLOCKS;
                        break;
                    case "decorations":
                        creativeTabs = CreativeTabs.DECORATIONS;
                        break;
                    case "redstone":
                        creativeTabs = CreativeTabs.REDSTONE;
                        break;
                    case "transportation":
                        creativeTabs = CreativeTabs.TRANSPORTATION;
                        break;
                    case "misc":
                        creativeTabs = CreativeTabs.MISC;
                        break;
                    case "search":
                        creativeTabs = CreativeTabs.SEARCH;
                        break;
                    case "food":
                        creativeTabs = CreativeTabs.FOOD;
                        break;
                    case "tools":
                        creativeTabs = CreativeTabs.TOOLS;
                        break;
                    case "combat":
                        creativeTabs = CreativeTabs.COMBAT;
                        break;
                    case "brewing":
                        creativeTabs = CreativeTabs.BREWING;
                        break;
                    case "hotbar":
                        creativeTabs = CreativeTabs.HOTBAR;
                        break;
                    case "inventory":
                        creativeTabs = CreativeTabs.INVENTORY;
                        break;
                    default:
                        creativeTabs = null;
                }
            }
        }
        return creativeTabs;
    }

    public static void decorateFluidBlock(Block fluidBlock, String registerName, String creativeTabKey, String unlocalizedName, String name, int density, ECModElements initializer) {
        if (fluidBlock != null) {
            registerName = (registerName == null || registerName.isEmpty()) ? name : registerName;
            if (fluidBlock.getRegistryName() == null) {
                fluidBlock.setRegistryName(initializer.container.id(), registerName);
            }
            registerName = fluidBlock.getRegistryName().getResourcePath();
            fluidBlock.setCreativeTab(getTab(creativeTabKey, initializer));

            unlocalizedName = (unlocalizedName == null || unlocalizedName.isEmpty()) ? registerName.toLowerCase() : unlocalizedName;
            if ("tile.null".equals(fluidBlock.getUnlocalizedName())) {
                fluidBlock.setUnlocalizedName(initializer.container.id() + "." + unlocalizedName);
            }

            if (fluidBlock instanceof BlockFluidBase) {
                ((BlockFluidBase) fluidBlock).setDensity(density);
            }
        }
    }
}
