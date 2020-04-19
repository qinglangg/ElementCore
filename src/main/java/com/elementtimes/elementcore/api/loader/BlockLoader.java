package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.Map;

/**
 * @author luqin2007
 */
public class BlockLoader {

    public static void load(ECModElements elements) {
        loadBlock(elements);
        loadFeature(elements);
        if (CommonUtils.isClient()) {
            loadColor(elements);
        }
    }

    private static void loadBlock(ECModElements elements) {
        ObjHelper.stream(elements, ModBlock.class).forEach(data -> {
            // block
            FindOptions<Block> option = new FindOptions<>(Block.class, ElementType.FIELD, ElementType.TYPE);
            ObjHelper.find(elements, data, option).ifPresent(block -> {
                String name = ObjHelper.getDefault(data);
                ObjHelper.setRegisterName(block, name, data, elements);
                elements.blocks.add(block);
                ObjHelper.saveResult(option, elements.generatedBlocks);
                Map<String, Object> map = data.getAnnotationData();
                if (!(boolean) map.getOrDefault("noItem", false)) {
                    Item.Properties properties = Parts.propertiesItem(map.get("item"), elements).orElseGet(Item.Properties::new);
                    BlockItem item = new BlockItem(block, properties);
                    item.setRegistryName(block.getRegistryName());
                    elements.blockItems.add(item);
                    ObjHelper.saveResult(option, item, elements.generatedBlockItems);
                }
            });
        });
    }

    private static void loadFeature(ECModElements elements) {
        ObjHelper.stream(elements, ModBlock.Features.class).forEach(data -> {
            Object o;
            if (data.getTargetType() == ElementType.FIELD) {
                o = ObjHelper.find(elements, data, new FindOptions<>(Block.class, ElementType.FIELD)).orElse(null);
            } else {
                Class<?> aClass = ObjHelper.findClass(elements, data.getClassType()).orElse(null);
                Block block = elements.generatedBlocks.get(aClass);
                if (block == null) {
                    o = aClass;
                } else {
                    o = block;
                }
            }
            if (o instanceof Class) {
                ObjHelper.getDefault(data, Collections.emptyList()).forEach(featureData -> {
                    Parts.feature(featureData, elements, (Class<?>) o).ifPresent(elements.features::add);
                });
            } else {
                ObjHelper.getDefault(data, Collections.emptyList()).forEach(featureData -> {
                    Parts.feature(featureData, elements, (Block) o).ifPresent(elements.features::add);
                });
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadColor(ECModElements elements) {
        ObjHelper.stream(elements, ModBlock.Colors.class).forEach(data -> {
            if (data.getTargetType() == ElementType.TYPE) {
                ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                    Block block = elements.generatedBlocks.get(aClass);
                    Map<String, Object> map = data.getAnnotationData();
                    if (block == null) {
                        Parts.colorBlock(map.get("block"), elements)
                                .ifPresent(wrapper -> elements.blockColors.add(wrapper.bind(aClass)));
                        Parts.colorItem(map.get("item"), elements)
                                .ifPresent(wrapper -> elements.itemColors.add(wrapper.bind(aClass)));
                    } else {
                        Parts.colorBlock(map.get("block"), elements)
                                .ifPresent(wrapper -> elements.blockColors.add(wrapper.bind(block)));
                        Parts.colorItem(map.get("item"), elements)
                                .ifPresent(wrapper -> elements.itemColors.add(wrapper.bind(block)));
                    }
                });
            } else {
                ObjHelper.find(elements, data, new FindOptions<>(Block.class, ElementType.FIELD)).ifPresent(block -> {
                    Map<String, Object> map = data.getAnnotationData();
                    Parts.colorBlock(map.get("block"), elements)
                            .ifPresent(wrapper -> elements.blockColors.add(wrapper.bind(block)));
                    Parts.colorItem(map.get("item"), elements)
                            .ifPresent(wrapper -> elements.itemColors.add(wrapper.bind(block)));
                });
            }
        });
    }
}
