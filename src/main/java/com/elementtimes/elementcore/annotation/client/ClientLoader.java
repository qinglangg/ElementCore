package com.elementtimes.elementcore.annotation.client;

import com.elementtimes.elementcore.ElementContainer;
import com.elementtimes.elementcore.annotation.common.LoaderHelper;
import com.elementtimes.elementcore.annotation.annotations.ModBlock;
import com.elementtimes.elementcore.annotation.annotations.ModItem;
import com.elementtimes.elementcore.util.ReflectUtil;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

/**
 * 客户端资源加载
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ClientLoader {

    public static void load(ElementContainer initializer) {
        initializer.modInfo.warn("client load start");
        // block
        loadBlockStateMapper(initializer);
        loadBlockState(initializer);
        loadBlockTESR(initializer);
        loadItemSub(initializer);
        initializer.modInfo.warn("client load finished");
    }

    private static void loadBlockStateMapper(ElementContainer initializer) {
        // ModBlock.StateMapper
        Set<ASMDataTable.ASMData> asmStateMapperData = initializer.asm.getAll(ModBlock.StateMapper.class.getName());
        if (asmStateMapperData != null) {
            for (ASMDataTable.ASMData asmData : asmStateMapperData) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    String suffix = (String) info.get("suffix");
                    String propertyName = (String) info.get("propertyName");
                    String[] propertyIgnore = (String[]) info.get("propertyIgnore");
                    String propertyIn = (String) info.get("propertyIn");

                    LoaderHelper.getOrLoadClass(initializer, propertyIn).ifPresent(clazz -> {
                        StateMap.Builder builder = new StateMap.Builder().withSuffix(suffix);
                        Optional<Object> property = ReflectUtil.getField(clazz, propertyName, null, initializer).filter(o -> o instanceof IProperty);
                        property.ifPresent(o -> builder.withName((IProperty<?>) o));
                        IProperty[] ignoreProperties = Arrays.stream(propertyIgnore)
                                .map(ignoreProperty -> ReflectUtil.getField(clazz, ignoreProperty, block, initializer))
                                .map(ignoreProperty -> ignoreProperty.filter(o -> o instanceof IProperty))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(o -> (IProperty) o)
                                .toArray(IProperty[]::new);
                        builder.ignore(ignoreProperties);
                    });
                });
            }
        }
        // ModBlock.StateMapperCustom
        Set<ASMDataTable.ASMData> customDataSet = initializer.asm.getAll(ModBlock.StateMapperCustom.class.getName());
        if (customDataSet != null) {
            for (ASMDataTable.ASMData asmData : customDataSet) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    String value = (String) asmData.getAnnotationInfo().get("value");
                    //noinspection unchecked
                    Object mapper = value.isEmpty()
                            ? new DefaultStateMapper()
                            : ReflectUtil.create(value, initializer).filter(o -> o instanceof IStateMapper).orElse(new DefaultStateMapper());
                    initializer.blockStateMaps.put(block, (IStateMapper) mapper);
                });
            }
        }

        initializer.modInfo.warn("loadBlockStateMapper: {}", initializer.blockStateMaps.size());
    }

    private static void loadBlockState(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModBlock.StateMap.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                Map<String, Object> info = asmData.getAnnotationInfo();
                int[] metadatas = (int[]) info.get("metadatas");
                String[] models = (String[]) info.get("models");
                String[] properties = (String[]) info.get("properties");
                final boolean hasSubModel = metadatas != null && models != null && properties != null;
                if (hasSubModel) {
                    int index = Math.min(Math.min(metadatas.length, models.length), properties.length);
                    if (index > 0) {
                        ArrayList<ModelLocation> blockStateTriples = new ArrayList<>(index);
                        for (int i = 0; i < index; i++) {
                            blockStateTriples.add(new ModelLocation(metadatas[i], initializer.modInfo.id(), models[i], properties[i]));
                        }
                        initializer.blockStates.put(block, blockStateTriples);
                    }
                }
            });
        }
        initializer.modInfo.warn("loadBlockState: {}", initializer.blockStates.size());
    }

    private static void loadBlockTESR(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> animDataSet = initializer.asm.getAll(ModBlock.AnimTESR.class.getName());
        if (animDataSet != null) {
            for (ASMDataTable.ASMData asmData : animDataSet) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    ImmutablePair<String, Class<? extends TileEntity>> pair = initializer.blockTileEntities.get(block);
                    if (pair != null) {
                        String animTESRClassName = (String) asmData.getAnnotationInfo().get("animationTESR");
                        if (animTESRClassName != null && !animTESRClassName.isEmpty()) {
                            //noinspection unchecked
                            Optional optional = ReflectUtil.create(animTESRClassName, initializer).filter(o -> o instanceof TileEntitySpecialRenderer);
                            if (optional.isPresent()) {
                                initializer.blockTesr.put(pair.right, (TileEntitySpecialRenderer) optional.get());
                            } else {
                                initializer.modInfo.warn("Can't create AnimationTESR object");
                            }
                        } else {
                            initializer.blockTesr.put(pair.right, new AnimationTESR());
                        }
                    } else {
                        initializer.modInfo.warn("Block {}: Animation must have a tileEntity.", block.getRegistryName());
                    }
                });
            }
        }

        Set<ASMDataTable.ASMData> tesrDataSet = initializer.asm.getAll(ModBlock.TESR.class.getName());
        if (tesrDataSet != null) {
            for (ASMDataTable.ASMData asmData : tesrDataSet) {
                LoaderHelper.getBlock(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    ImmutablePair<String, Class<? extends TileEntity>> pair = initializer.blockTileEntities.get(block);
                    String className = (String) asmData.getAnnotationInfo().get("value");
                    if (pair != null) {
                        //noinspection unchecked
                        Optional optional = ReflectUtil.create(className, initializer).filter(o -> o instanceof TileEntitySpecialRenderer);
                        if (optional.isPresent()) {
                            initializer.blockTesr.put(pair.right, (TileEntitySpecialRenderer) optional.get());
                        } else {
                            initializer.modInfo.warn("Can't create TileEntitySpecialRenderer object");
                        }
                    } else {
                        initializer.modInfo.warn("Block {}: TESR must have a tileEntity.", block.getRegistryName());
                    }
                });
            }
        }
        initializer.modInfo.warn("loadBlockTESR: {}", initializer.blockTesr.size());
    }

    private static void loadItemSub(ElementContainer initializer) {
        Set<ASMDataTable.ASMData> asmDataSet = initializer.asm.getAll(ModItem.HasSubItem.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(initializer, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                Map<String, Object> info = asmData.getAnnotationInfo();
                int[] metadatas = (int[]) info.getOrDefault("metadatas", new int[0]);
                //noinspection unchecked
                List<String> models = (List<String>) info.getOrDefault("models", Collections.EMPTY_LIST);
                if (metadatas != null || models != null) {
                    assert metadatas != null;
                    int index = Math.min(metadatas.length, models.size());
                    if (index > 0) {
                        ArrayList<ModelLocation> modelTriples = new ArrayList<>(index);
                        for (int i = 0; i < index; i++) {
                            modelTriples.add(new ModelLocation(metadatas[i], initializer.modInfo.id(), models.get(i)));
                        }
                        initializer.itemSubModel.put(item, modelTriples);
                    }
                }
            });
        }

        initializer.modInfo.warn("loadItemSub: {}", initializer.itemSubModel.size());
    }
}
