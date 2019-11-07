package com.elementtimes.elementcore.api;

import net.minecraft.block.Block;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 加载的辅助类
 * @author luqin2007
 */
public class LoaderHelper {

    public static Optional<Class> loadClass(@Nonnull ECModElements elements, @Nonnull String className) {
        return Optional.ofNullable(elements.classes.computeIfAbsent(className, name -> {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                elements.warn("Can't find class: {}", className);
                return null;
            }
        }));
    }

    public static Optional<Block> getBlock(ECModElements elements, String className, String objectName) {
        String key = className + "." + objectName;
        Block block = elements.blocks.blocks().get(key);
        return Optional.ofNullable(block);
    }

    public static Optional<Item> getItem(ECModElements elements, String className, String objectName) {
        String key = className + "." + objectName;
        Item item = elements.items.items().get(key);
        return Optional.ofNullable(item);
    }

    public static Optional<FlowingFluid> getFluid(ECModElements elements, String className, String objectName) {
        String key = className + "." + objectName;
        FlowingFluid fluid = elements.fluids.fluids().get(key);
        return Optional.ofNullable(fluid);
    }

    public static ItemGroup getGroup(ECModElements elements, String key) {
        if (key != null && !key.isEmpty()) {
            ItemGroup group = elements.itemGroups.groups().get(key);
            if (group == null) {
                switch (key) {
                    case "buildingBlocks": return ItemGroup.BUILDING_BLOCKS;
                    case "decorations": return ItemGroup.DECORATIONS;
                    case "redstone": return ItemGroup.REDSTONE;
                    case "transportation": return ItemGroup.TRANSPORTATION;
                    case "misc": return ItemGroup.MISC;
                    case "search": return ItemGroup.SEARCH;
                    case "food": return ItemGroup.FOOD;
                    case "tools": return ItemGroup.TOOLS;
                    case "combat": return ItemGroup.COMBAT;
                    case "brewing": return ItemGroup.BREWING;
                    case "hotbar": return ItemGroup.HOTBAR;
                    case "inventory": return ItemGroup.INVENTORY;
                    default:
                        for (ECModElements mod : ECModElements.MODS.values()) {
                            ItemGroup groupOther = mod.itemGroups.groups().get(key);
                            if (groupOther != null) {
                                return groupOther;
                            }
                        }
                        return null;
                }
            } else {
                return group;
            }
        } else {
            return elements.itemGroups.groups().values().stream().findFirst().orElse(null);
        }
    }

    public static <T extends IForgeRegistryEntry<T>> void regName(ECModElements elements, T entry, @Nonnull String name) {
        if (entry.getRegistryName() == null) {
            if (name.contains(":")) {
                entry.setRegistryName(new ResourceLocation(name.toLowerCase()));
            } else {
                entry.setRegistryName(new ResourceLocation(elements.id(), name.toLowerCase()));
            }
        }
    }

    public static <T extends IForgeRegistryEntry<T>> void regName(ECModElements elements, T entry, IForgeRegistryEntry from, String name) {
        if (entry.getRegistryName() == null) {
            ResourceLocation registryName = from.getRegistryName();
            if (registryName == null) {
                regName(elements, entry, name);
            } else {
                entry.setRegistryName(registryName);
            }
        }
    }

    public static Stream<ModFileScanData.AnnotationData> stream(ECModElements elements, Class<? extends Annotation> containedAnnotation, Class<? extends Annotation> notContainedAnnotation) {
        return elements.data.getAnnotations().stream()
                .filter(data -> containedAnnotation.getName().equals(data.getAnnotationType().getClassName()))
                .filter(data -> !notContainedAnnotation.getName().equals(data.getAnnotationType().getClassName()));
    }

    public static Stream<ModFileScanData.AnnotationData> stream(ECModElements elements, Class<? extends Annotation> containedAnnotation) {
        return elements.data.getAnnotations().stream()
                .filter(data -> containedAnnotation.getName().equals(data.getAnnotationType().getClassName()));
    }

    public static <T> T getDefault(ModFileScanData.AnnotationData data) {
        return (T) data.getAnnotationData().get("value");
    }

    public static <T> T getDefault(ModFileScanData.AnnotationData data, T defVal) {
        return (T) data.getAnnotationData().getOrDefault("value", defVal);
    }
}
