package com.elementtimes.elementcore.api.client;

import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.common.LoaderHelper;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.util.TextUtils;

import java.util.*;

/**
 * 客户端资源加载
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ClientLoader {

    public static void load(ECModElements modElements) {
        modElements.container.warn("client load start");
        // block
        loadBlockStateMapper(modElements);
        loadBlockState(modElements);
        loadBlockTesr(modElements);
        loadBlockColor(modElements);
        loadItemSub(modElements);
        loadItemMeshDefinition(modElements);
        loadItemColor(modElements);
        modElements.container.warn("client load finished");
    }

    private static void loadBlockStateMapper(ECModElements elements) {
        // ModBlock.StateMapper
        Set<ASMDataTable.ASMData> asmStateMapperData = elements.asm.getAll(ModBlock.StateMapper.class.getName());
        if (asmStateMapperData != null) {
            for (ASMDataTable.ASMData asmData : asmStateMapperData) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    Map<String, Object> info = asmData.getAnnotationInfo();
                    String suffix = (String) info.get("suffix");
                    String propertyName = (String) info.get("propertyName");
                    //noinspection unchecked
                    List<String> propertyIgnore = (List<String>) info.getOrDefault("propertyIgnore", Collections.emptyList());
                    String propertyIn = (String) info.get("propertyIn");

                    LoaderHelper.getOrLoadClass(elements, propertyIn).ifPresent(clazz -> {
                        StateMap.Builder builder = new StateMap.Builder().withSuffix(suffix);
                        ECUtils.reflect.getField(clazz, propertyName, null, IProperty.class, elements.container.logger)
                                .ifPresent(o -> builder.withName((IProperty<?>) o));
                        IProperty[] ignoreProperties = propertyIgnore.stream()
                                .map(ignoreProperty -> ECUtils.reflect.getField(clazz, ignoreProperty, block, IProperty.class, elements.container.logger))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toArray(IProperty[]::new);
                        builder.ignore(ignoreProperties);
                        elements.getClientElements().blockStateMaps.put(block, builder.build());
                    });
                });
            }
        }
        // ModBlock.StateMapperCustom
        Set<ASMDataTable.ASMData> customDataSet = elements.asm.getAll(ModBlock.StateMapperCustom.class.getName());
        if (customDataSet != null) {
            for (ASMDataTable.ASMData asmData : customDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    String value = (String) asmData.getAnnotationInfo().get("value");
                    //noinspection
                    IStateMapper mapper = (value == null || value.isEmpty())
                            ? new DefaultStateMapper()
                            : ECUtils.reflect.create(value, IStateMapper.class, elements.container.logger).orElse(new DefaultStateMapper());
                    elements.getClientElements().blockStateMaps.put(block, mapper);
                });
            }
        }

        elements.container.warn("loadBlockStateMapper: {}", elements.getClientElements().blockStateMaps.size());
    }

    private static void loadBlockState(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModBlock.StateMap.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                Map<String, Object> info = asmData.getAnnotationInfo();
                int[] metadatas = (int[]) info.get("metadatas");
                //noinspection unchecked
                List<String> models = (List<String>) info.getOrDefault("models", Collections.emptyList());
                //noinspection unchecked
                List<String> properties = (List<String>) info.getOrDefault("properties", Collections.emptyList());
                final boolean hasSubModel = metadatas != null && models != null && properties != null;
                if (hasSubModel) {
                    int index = Math.min(Math.min(metadatas.length, models.size()), properties.size());
                    if (index > 0) {
                        ArrayList<ModelLocation> blockStateTriples = new ArrayList<>(index);
                        for (int i = 0; i < index; i++) {
                            blockStateTriples.add(new ModelLocation(metadatas[i], elements.container.id(), models.get(i), properties.get(i)));
                        }
                        elements.getClientElements().blockStates.put(block, blockStateTriples);
                    }
                }
            });
        }
        elements.container.warn("loadBlockState: {}", elements.getClientElements().blockStates.size());
    }

    private static void loadBlockTesr(ECModElements elements) {
        Set<ASMDataTable.ASMData> animDataSet = elements.asm.getAll(ModBlock.AnimTESR.class.getName());
        if (animDataSet != null) {
            for (ASMDataTable.ASMData asmData : animDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    ImmutablePair<String, Class<? extends TileEntity>> pair = elements.blockTileEntities.get(block);
                    if (pair != null) {
                        String animTesrClassName = (String) asmData.getAnnotationInfo().get("animationTESR");
                        if (animTesrClassName != null && !animTesrClassName.isEmpty()) {
                            //noinspection
                            Optional<TileEntitySpecialRenderer> optional = ECUtils.reflect.create(animTesrClassName, TileEntitySpecialRenderer.class, elements.container.logger);
                            if (optional.isPresent()) {
                                elements.getClientElements().blockTesr.put(pair.right, optional.get());
                            } else {
                                elements.container.warn("Can't create AnimationTESR object");
                            }
                        } else {
                            elements.getClientElements().blockTesr.put(pair.right, new AnimationTESR());
                        }
                    } else {
                        elements.container.warn("Block {}: Animation must have a tileEntity.", block.getRegistryName());
                    }
                });
            }
        }

        Set<ASMDataTable.ASMData> tesrDataSet = elements.asm.getAll(ModBlock.TESR.class.getName());
        if (tesrDataSet != null) {
            for (ASMDataTable.ASMData asmData : tesrDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    ImmutablePair<String, Class<? extends TileEntity>> pair = elements.blockTileEntities.get(block);
                    String className = (String) asmData.getAnnotationInfo().get("value");
                    if (pair != null) {
                        //noinspection
                        Optional<TileEntitySpecialRenderer> optional = ECUtils.reflect.create(className, TileEntitySpecialRenderer.class, elements.container.logger);
                        if (optional.isPresent()) {
                            elements.getClientElements().blockTesr.put(pair.right, optional.get());
                        } else {
                            elements.container.warn("Can't create TileEntitySpecialRenderer object");
                        }
                    } else {
                        elements.container.warn("Block {}: TESR must have a tileEntity.", block.getRegistryName());
                    }
                });
            }
        }
        elements.container.warn("loadBlockTESR: {}", elements.getClientElements().blockTesr.size());
    }

    private static void loadBlockColor(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModBlock.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getBlock(elements,asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                final String colorClass = (String) asmData.getAnnotationInfo().get("blockColorClass");
                if (!TextUtils.isBlank(colorClass)) {
                    IBlockColor color = elements.getClientElements().blockColorMap.get(colorClass);
                    if (color == null) {
                        final Optional<IBlockColor> iBlockColorOpt = ECUtils.reflect.create(colorClass, IBlockColor.class, elements.container.logger);
                        if (iBlockColorOpt.isPresent()) {
                            final IBlockColor iBlockColor = iBlockColorOpt.get();
                            elements.getClientElements().blockColorMap.put(colorClass, iBlockColor);
                            elements.getClientElements().blockColors.put(iBlockColor, new LinkedList<>());
                            color = iBlockColor;
                        }
                    }
                    if (color != null) {
                        elements.getClientElements().blockColors.get(color).add(block);
                    }
                }

                final String itemColorClass = (String) asmData.getAnnotationInfo().get("blockItemColorClass");
                if (!TextUtils.isBlank(itemColorClass)) {
                    IItemColor color = elements.getClientElements().blockItemColorMap.get(colorClass);
                    if (color == null) {
                        final Optional<IItemColor> iItemColorOpt = ECUtils.reflect.create(colorClass, IItemColor.class, elements.container.logger);
                        if (iItemColorOpt.isPresent()) {
                            final IItemColor iItemColor = iItemColorOpt.get();
                            elements.getClientElements().blockItemColorMap.put(colorClass, iItemColor);
                            elements.getClientElements().blockItemColors.put(iItemColor, new LinkedList<>());
                            color = iItemColor;
                        }
                    }
                    if (color != null) {
                        elements.getClientElements().blockItemColors.get(color).add(block);
                    }
                }
            });
        }
    }

    private static void loadItemSub(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.HasSubItem.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
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
                            modelTriples.add(new ModelLocation(metadatas[i], elements.container.id(), models.get(i)));
                        }
                        elements.getClientElements().itemSubModel.put(item, modelTriples);
                    }
                }
            });
        }

        elements.container.warn("loadItemSub: {}", elements.getClientElements().itemSubModel.size());
    }

    private static void loadItemColor(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                final String colorClass = (String) asmData.getAnnotationInfo().get("itemColorClass");
                if (!TextUtils.isBlank(colorClass)) {
                    IItemColor color = elements.getClientElements().itemColorMap.get(colorClass);
                    if (color == null) {
                        final Optional<IItemColor> iItemColorOpt = ECUtils.reflect.create(colorClass, IItemColor.class, elements.container.logger);
                        if (iItemColorOpt.isPresent()) {
                            final IItemColor iItemColor = iItemColorOpt.get();
                            elements.getClientElements().itemColorMap.put(colorClass, iItemColor);
                            elements.getClientElements().itemColors.put(iItemColor, new LinkedList<>());
                            color = iItemColor;
                        }
                    }
                    if (color != null) {
                        elements.getClientElements().itemColors.get(color).add(item);
                    }
                }
            });
        }

        elements.container.warn("loadItemSub: {}", elements.getClientElements().itemSubModel.size());
    }

    private static void loadItemMeshDefinition(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.HasMeshDefinition.class.getName());
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                String clazz = (String) asmData.getAnnotationInfo().get("value");
                ECUtils.reflect.create(clazz, ItemMeshDefinition.class, elements.container.logger)
                        .ifPresent(meshDefinition -> elements.getClientElements().itemMeshDefinition.put(item, meshDefinition));
            });
        }
    }
}