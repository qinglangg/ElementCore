package com.elementtimes.elementcore.annotation.common;

import com.elementtimes.elementcore.ElementContainer;
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

    public static Optional<Class> getOrLoadClass(@Nonnull ElementContainer initializer, @Nonnull String className) {
        Class clazz = initializer.classes.get(className);
        if (clazz == null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                initializer.modInfo.warn("Can't find class: {}", className);
            }
        }
        if (clazz != null) {
            initializer.classes.put(className, clazz);
        }
        return Optional.ofNullable(clazz);
    }

    public static Optional<Block> getBlock(ElementContainer initializer, String className, String objectName) {
        return Optional.ofNullable(initializer.blocks.get(className, objectName == null ? "" : objectName));
    }

    public static Optional<Item> getItem(ElementContainer initializer, String className, String objectName) {
        return Optional.ofNullable(initializer.items.get(className, objectName == null ? "" : objectName));
    }

    public static Optional<Fluid> getFluid(ElementContainer initializer, String className, String objectName) {
        return Optional.ofNullable(initializer.fluids.get(className, objectName == null ? "" : objectName));
    }

    public static CreativeTabs getTab(String key, ElementContainer initializer) {
        CreativeTabs creativeTabs = null;
        if (key != null && !key.isEmpty()) {
            creativeTabs = initializer.tabs.get(key);
            if (creativeTabs == null) {
                for (ElementContainer annotationInitializer : ElementContainer.INITIALIZERS.values()) {
                    creativeTabs = annotationInitializer.tabs.get(key);
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

    public static void decorateFluidBlock(Block fluidBlock, String registerName, String creativeTabKey, String unlocalizedName, String name, int density, ElementContainer initializer) {
        if (fluidBlock != null) {
            registerName = (registerName == null || registerName.isEmpty()) ? name : registerName;
            if (fluidBlock.getRegistryName() == null) {
                fluidBlock.setRegistryName(initializer.modInfo.id(), registerName);
            }
            registerName = fluidBlock.getRegistryName().getResourcePath();
            fluidBlock.setCreativeTab(getTab(creativeTabKey, initializer));

            unlocalizedName = (unlocalizedName == null || unlocalizedName.isEmpty()) ? registerName.toLowerCase() : unlocalizedName;
            if ("tile.null".equals(fluidBlock.getUnlocalizedName())) {
                fluidBlock.setUnlocalizedName(initializer.modInfo.id() + "." + unlocalizedName);
            }

            if (fluidBlock instanceof BlockFluidBase) {
                ((BlockFluidBase) fluidBlock).setDensity(density);
            }
        }
    }
}
