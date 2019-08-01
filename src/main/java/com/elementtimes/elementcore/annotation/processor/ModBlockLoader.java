package com.elementtimes.elementcore.annotation.processor;

import com.elementtimes.elementcore.annotation.AnnotationInitializer;
import com.elementtimes.elementcore.annotation.annotations.ModBlock;
import com.elementtimes.elementcore.annotation.annotations.ModTags;
import com.elementtimes.elementcore.util.BlockUtil;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 用于处理 Block 注解
 * 所有被 ModBlock 注解的成员都会在此处理
 *
 * @see ModBlock
 * @author luqin2007
 */
public class ModBlockLoader {

    /**
     * 获取所有方块
     */
    public static void load(AnnotationInitializer initializer) {
        initializer.elements.get(ModBlock.class).forEach(element -> buildBlock(initializer, element));
    }
    private static void buildBlock(AnnotationInitializer initializer, AnnotatedElement blockHolder) {
        ModBlock info = blockHolder.getAnnotation(ModBlock.class);
        Block.Properties properties = Block.Properties.create(Material.ROCK);
        Block block = ReflectUtil.getFromAnnotated(blockHolder, new Block(properties), initializer).orElse(new Block(properties));

        String defaultName;
        if (blockHolder instanceof Class) {
            defaultName = ((Class) blockHolder).getSimpleName().toLowerCase();
        } else if (blockHolder instanceof Field) {
            defaultName = ((Field) blockHolder).getName().toLowerCase();
        } else {
            defaultName = null;
        }

        initBlock(initializer, block, info, defaultName);
        // 矿辞
        initBlockTags(initializer, block, blockHolder);
        // TileEntity
        initTileEntity(initializer, block, blockHolder);
        initializer.blocks.add(block);
    }

    private static void initBlock(AnnotationInitializer initializer, Block block, ModBlock info, String defaultName) {
        String registryName = info.registerName();
        if (registryName.isEmpty()) {
            registryName = defaultName;
        }
        if (block.getRegistryName() == null) {
            if (registryName == null || registryName.isEmpty()) {
                initializer.warn("Block {} don't have a RegisterName. It's a Bug!!!", block);
            } else {
                block.setRegistryName(initializer.modInfo.modid, registryName);
            }
        }
        int burningTime = info.burningTime();
        if (burningTime > 0) {
            initializer.blockBurningTimes.put(block, burningTime);
        }
    }

    private static void initBlockTags(AnnotationInitializer initializer, Block block, AnnotatedElement blockHolder) {
        ModTags tag = blockHolder.getAnnotation(ModTags.class);
        if (tag != null) {
            ResourceLocation key = new ResourceLocation(tag.value());
            Tag.Builder<Block> builder = initializer.blockTags.getOrDefault(key, new Tag.Builder<>());
            builder.add(block);
            initializer.blockTags.put(key, builder);
        }

    }

    @SuppressWarnings("unchecked")
    private static void initTileEntity(AnnotationInitializer initializer, Block block, AnnotatedElement blockHolder) {
        ModBlock.TileEntity tileEntity = blockHolder.getAnnotation(ModBlock.TileEntity.class);
        if (tileEntity != null) {
            try {
                Class clazz = Class.forName(tileEntity.clazz());
                TileEntityType type = null;
                if (!tileEntity.teType().isEmpty()) {
                    Method method = clazz.getDeclaredMethod(tileEntity.teType());
                    if (method != null) {
                        method.setAccessible(true);
                        if (TileEntityType.class.isAssignableFrom(method.getReturnType())) {
                            Optional<Object> optional = ReflectUtil.invoke(method, null, initializer)
                                    .filter(o -> o instanceof TileEntityType);
                            if (optional.isPresent()) {
                                type = (TileEntityType) optional.get();
                            }
                        }
                    }
                    if (type != null) {
                        Field field = clazz.getDeclaredField(tileEntity.teType());
                        if (field != null) {
                            field.setAccessible(true);
                            if (TileEntityType.class.isAssignableFrom(field.getType())) {
                                Optional<Object> optional = ReflectUtil.get(field, null, null, true, initializer)
                                        .filter(o -> o instanceof TileEntityType);
                                if (optional.isPresent()) {
                                    type = (TileEntityType) optional.get();
                                }
                            }
                        }
                    }
                }
                if (type == null) {
                    Supplier<TileEntity> builder = null;
                    String methodName = tileEntity.teCreator();
                    if (!methodName.isEmpty()) {
                        Method method = clazz.getDeclaredMethod(methodName);
                        if (method != null && TileEntity.class.isAssignableFrom(method.getReturnType())) {
                            builder = () -> {
                                try {
                                    return (TileEntity) method.invoke(null);
                                } catch (IllegalAccessException | InvocationTargetException ignored) {
                                    return null;
                                }
                            };
                        }
                    }
                    if (builder == null) {
                        builder = () -> (TileEntity) ReflectUtil.create(tileEntity.name(), initializer).orElse(null);
                    }
                    type = BlockUtil.createTileEntityType(builder, tileEntity.name(), block);
                }

                initializer.blockTileEntities.add(type);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }
}
