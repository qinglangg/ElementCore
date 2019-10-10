package com.elementtimes.elementcore.api.common;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

/**
 * 加载的辅助类
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class LoaderHelper {

    public static Optional<Class> getOrLoadClass(@Nonnull ECModElements elements, @Nonnull String className) {
        boolean skip = true;
        for (String packageName : elements.packages) {
            if (className.startsWith(packageName)) {
                skip = false;
                break;
            }
        }
        if (skip) {
            return Optional.empty();
        }
        Class clazz = elements.classes == null ? null : elements.classes.get(className);
        if (clazz == null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                elements.container.warn("Can't find class: {}", className);
            }
            if (clazz != null) {
                if (elements.classes == null) {
                    elements.classes = new HashMap<>(16);
                }
                elements.classes.put(className, clazz);
            }
        }
        return Optional.ofNullable(clazz);
    }

    public static Optional<Block> getBlock(ECModElements elements, String className, String objectName) {
        return Optional.ofNullable(elements.blocks == null ? null : elements.blocks.get(className + (objectName == null ? "" : objectName)));
    }

    public static Optional<Item> getItem(ECModElements elements, String className, String objectName) {
        return Optional.ofNullable(elements.items == null ? null : elements.items.get(className + (objectName == null ? "" : objectName)));
    }

    public static Optional<Fluid> getFluid(ECModElements elements, String className, String objectName) {
        return Optional.ofNullable(elements.fluids == null ? null : elements.fluids.get(className + (objectName == null ? "" : objectName)));
    }

    public static CreativeTabs getTab(String key, ECModElements elements) {
        CreativeTabs creativeTabs = null;
        if (key != null && !key.isEmpty()) {
            creativeTabs = elements == null ? null : elements.tabs.get(key);
            if (creativeTabs == null) {
                for (ECModContainer mod : ECModContainer.MODS.values()) {
                    creativeTabs = mod.elements.tabs == null ? null : mod.elements.tabs.get(key);
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
                if (registerName.contains(":")) {
                    fluidBlock.setRegistryName(registerName);
                } else {
                    fluidBlock.setRegistryName(initializer.container.id(), registerName);
                }
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

    public static <K, V> HashMap<K, V> createMap(Set... sets) {
        int size = 0;
        for (Set set : sets) {
            size += set == null ? 0 : set.size();
        }
        return new HashMap<>(size);
    }

    public static <K, V> HashMap<K, V> createMap(int start, Set... sets) {
        int size = start;
        for (Set set : sets) {
            size += set == null ? 0 : set.size();
        }
        return new HashMap<>(size);
    }
}
