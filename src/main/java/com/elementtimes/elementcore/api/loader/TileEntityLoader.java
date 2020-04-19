package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.ModTileEntity;
import com.elementtimes.elementcore.api.annotation.part.Parts;
import com.elementtimes.elementcore.api.helper.FindOptions;
import com.elementtimes.elementcore.api.helper.ObjHelper;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationGetter;
import com.elementtimes.elementcore.api.misc.wrapper.AnnotationMethod;
import com.elementtimes.elementcore.api.misc.wrapper.BlockTerWrapper;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class TileEntityLoader {

    public static void load(ECModElements elements) {
        loadTileEntityType(elements);
        if (CommonUtils.isClient()) {
            loadTer(elements);
        }
    }

    private static void loadTileEntityType(ECModElements elements) {
        ObjHelper.stream(elements, ModTileEntity.class).forEach(data -> {
            Map<String, Object> map = data.getAnnotationData();
            if (data.getTargetType() == ElementType.FIELD) {
                FindOptions<TileEntityType> option = new FindOptions<>(TileEntityType.class, ElementType.FIELD);
                ObjHelper.find(elements, data, option).ifPresent(type -> {
                    ObjHelper.setRegisterName(type, (String) map.get("name"), data, elements);
                    ObjHelper.saveResult(option, type, elements.generatedTileEntityTypes);
                    elements.teTypes.add(type);
                });
            } else {
                ObjHelper.findClass(elements, data.getClassType()).ifPresent(aClass -> {
                    TileEntityType<?> type = elements.generatedTileEntityTypes.get(aClass);
                    if (type == null) {
                        Block[] blocks = ((List<Map<String, Object>>) map.getOrDefault("blocks", Collections.emptyList())).stream()
                                .map(getter -> Parts.getter(elements, getter))
                                .filter(ag -> ag.hasContent(Block.class))
                                .map(ag -> ag.<Block>get())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toArray(Block[]::new);
                        if (blocks.length > 0) {
                            AnnotationMethod creator = Parts.method(elements, map.get("newTe"), TileEntityType.class);
                            Supplier<TileEntity> factory;
                            if (creator.hasContent(TileEntity.class)) {
                                factory = () -> {
                                    TileEntityType<?> tet = elements.generatedTileEntityTypes.get(aClass);
                                    return (TileEntity) creator.get(tet).orElseGet(() -> {
                                        elements.warn("[{}]Can't create TileEntity by {}, use constructor", elements.container.id(), creator.getRefName());
                                        return newTileEntity(aClass, tet);
                                    });
                                };
                            } else {
                                factory = () -> newTileEntity(aClass, elements.generatedTileEntityTypes.get(aClass));
                            }
                            type = TileEntityType.Builder.create(factory, blocks).build(null);
                            elements.generatedTileEntityTypes.put((Class<? extends TileEntity>) aClass, type);
                            ObjHelper.setRegisterName(type, (String) map.get("name"), data, elements);
                            elements.teTypes.add(type);
                        }
                    }
                });
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadTer(ECModElements elements) {
        ObjHelper.stream(elements, ModTileEntity.Ter.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassType()).filter(TileEntity.class::isAssignableFrom).ifPresent(teClass -> {
                Class<?> terClass = net.minecraft.client.renderer.tileentity.TileEntityRenderer.class;
                AnnotationGetter getter = Parts.getter(elements, ObjHelper.getDefault(data));
                if (getter.hasContent(terClass)) {
                    elements.ters.add(new BlockTerWrapper((Class<? extends TileEntity>) teClass, getter.get().orElseGet(() -> {
                        elements.warn("[{}]Can't create TileEntityRenderer from {}. Use TileEntityRendererAnimation.", elements.container.id(), getter.getRefName());
                        return new net.minecraftforge.client.model.animation.TileEntityRendererAnimation<>();
                    })));
                } else {
                    elements.warn("[{}]Can't find TileEntityRenderer creator. Use TileEntityRendererAnimation.", elements.container.id());
                    elements.ters.add(new BlockTerWrapper((Class<? extends TileEntity>) teClass, new net.minecraftforge.client.model.animation.TileEntityRendererAnimation<>()));
                }
            });
        });
    }

    private static TileEntity newTileEntity(Class<?> aClass, TileEntityType<?> tet) {
        try {
            Constructor<?> constructor = aClass.getDeclaredConstructor(TileEntityType.class);
            if (!Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }
            return (TileEntity) constructor.newInstance(tet);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
