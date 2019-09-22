package com.elementtimes.elementcore.api.client;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.LoaderHelper;
import com.elementtimes.elementcore.api.template.tileentity.BaseTESR;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTESR;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.util.TextUtils;

import java.util.*;
import java.util.stream.Collectors;

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
        // ModBlock.StateMapperCustom
        Set<ASMDataTable.ASMData> customDataSet = elements.asm.getAll(ModBlock.StateMapperCustom.class.getName());
        ECModElementsClient client = elements.getClientElements();
        client.blockStateMaps = LoaderHelper.createMap(asmStateMapperData, customDataSet);
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
                        client.blockStateMaps.put(block, builder.build());
                    });
                });
            }
        }
        if (customDataSet != null) {
            for (ASMDataTable.ASMData asmData : customDataSet) {
                LoaderHelper.getBlock(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    String value = (String) asmData.getAnnotationInfo().get("value");
                    //noinspection
                    IStateMapper mapper = (value == null || value.isEmpty())
                            ? new DefaultStateMapper()
                            : ECUtils.reflect.create(value, IStateMapper.class, elements.container.logger).orElse(new DefaultStateMapper());
                    client.blockStateMaps.put(block, mapper);
                });
            }
        }

        elements.container.warn("loadBlockStateMapper: {}", client.blockStateMaps.size());
    }

    private static void loadBlockState(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModBlock.StateMap.class.getName());
        ECModElementsClient client = elements.getClientElements();
        client.blockStates = LoaderHelper.createMap(asmDataSet);
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
                        client.blockStates.put(block, blockStateTriples);
                    }
                }
            });
        }
        elements.container.warn("loadBlockState: {}", client.blockStates.size());
    }

    private static void loadBlockTesr(ECModElements elements) {
        Set<ASMDataTable.ASMData> animDataSet = elements.asm.getAll(ModBlock.AnimTESR.class.getName());
        Set<ASMDataTable.ASMData> tesrDataSet = elements.asm.getAll(ModBlock.TESR.class.getName());
        List<ImmutablePair<String, Class<? extends TileEntity>>> collect = elements.blockTileEntities.values()
                .stream()
                .filter(te -> ITileTESR.class.isAssignableFrom(te.right))
                .collect(Collectors.toList());
        elements.getClientElements().blockTesr = LoaderHelper.createMap(collect.size(), animDataSet, tesrDataSet);

        for (ImmutablePair<String, Class<? extends TileEntity>> te : collect) {
            elements.getClientElements().blockTesr.put(te.right, new BaseTESR());
        }

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
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModBlock.BlockColor.class.getName());
        ECModElementsClient client = elements.getClientElements();
        client.blockColors = LoaderHelper.createMap(asmDataSet);
        client.blockColorMap = LoaderHelper.createMap(asmDataSet);
        client.blockItemColors = LoaderHelper.createMap(asmDataSet);
        client.blockItemColorMap = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getBlock(elements,asmData.getClassName(), asmData.getObjectName()).ifPresent(block -> {
                    String colorClass = (String) asmData.getAnnotationInfo().get("value");
                    if (!TextUtils.isBlank(colorClass)) {
                        IBlockColor color = client.blockColorMap.get(colorClass);
                        if (color == null) {
                            final Optional<IBlockColor> iBlockColorOpt = ECUtils.reflect.create(colorClass, IBlockColor.class, elements.container.logger);
                            if (iBlockColorOpt.isPresent()) {
                                final IBlockColor iBlockColor = iBlockColorOpt.get();
                                client.blockColorMap.put(colorClass, iBlockColor);
                                client.blockColors.put(iBlockColor, new ArrayList<>());
                                color = iBlockColor;
                            }
                        }
                        if (color != null) {
                            client.blockColors.get(color).add(block);
                        }
                    }

                    final String itemColorClass = (String) asmData.getAnnotationInfo().get("itemColor");
                    if (!TextUtils.isBlank(itemColorClass)) {
                        IItemColor color = client.blockItemColorMap.get(itemColorClass);
                        if (color == null) {
                            final Optional<IItemColor> iItemColorOpt = ECUtils.reflect.create(itemColorClass, IItemColor.class, elements.container.logger);
                            if (iItemColorOpt.isPresent()) {
                                final IItemColor iItemColor = iItemColorOpt.get();
                                client.blockItemColorMap.put(colorClass, iItemColor);
                                client.blockItemColors.put(iItemColor, new ArrayList<>());
                                color = iItemColor;
                            }
                        }
                        if (color != null) {
                            client.blockItemColors.get(color).add(block);
                        }
                    }
                });
            }
        }
    }

    private static void loadItemSub(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.HasSubItem.class.getName());
        ECModElementsClient client = elements.getClientElements();
        client.itemSubModel = LoaderHelper.createMap(asmDataSet);
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
                        client.itemSubModel.put(item, modelTriples);
                    }
                }
            });
        }

        elements.container.warn("loadItemSub: {}", client.itemSubModel.size());
    }

    private static void loadItemColor(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.ItemColor.class.getName());
        ECModElementsClient client = elements.getClientElements();
        client.itemColors = LoaderHelper.createMap(asmDataSet);
        client.itemColorMap = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet != null) {
            for (ASMDataTable.ASMData asmData : asmDataSet) {
                LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                    String colorClass = (String) asmData.getAnnotationInfo().get("value");
                    if (!TextUtils.isBlank(colorClass)) {
                        IItemColor color = client.itemColorMap.get(colorClass);
                        if (color == null) {
                            final Optional<IItemColor> iItemColorOpt = ECUtils.reflect.create(colorClass, IItemColor.class, elements.container.logger);
                            if (iItemColorOpt.isPresent()) {
                                final IItemColor iItemColor = iItemColorOpt.get();
                                client.itemColorMap.put(colorClass, iItemColor);
                                client.itemColors.put(iItemColor, new ArrayList<>());
                                color = iItemColor;
                            }
                        }
                        if (color != null) {
                            client.itemColors.get(color).add(item);
                        }
                    }
                });
            }
        }

        elements.container.warn("loadItemColor: {}", client.itemColors.size());
    }

    private static void loadItemMeshDefinition(ECModElements elements) {
        Set<ASMDataTable.ASMData> asmDataSet = elements.asm.getAll(ModItem.HasMeshDefinition.class.getName());
        ECModElementsClient client = elements.getClientElements();
        client.itemMeshDefinition = LoaderHelper.createMap(asmDataSet);
        if (asmDataSet == null) {
            return;
        }
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            LoaderHelper.getItem(elements, asmData.getClassName(), asmData.getObjectName()).ifPresent(item -> {
                String clazz = (String) asmData.getAnnotationInfo().get("value");
                ECUtils.reflect.create(clazz, ItemMeshDefinition.class, elements.container.logger)
                        .ifPresent(meshDefinition -> client.itemMeshDefinition.put(item, meshDefinition));
            });
        }
    }
}