package com.elementtimes.elementcore.api.client.loader;

import com.elementtimes.elementcore.api.annotation.ModBlock;
import com.elementtimes.elementcore.api.client.ECModElementsClient;
import com.elementtimes.elementcore.api.client.LoaderHelperClient;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.tileentity.BaseTESR;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTESR;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class BlockClientLoader {

    public static void load(ECModElements elements) {
        loadBlockStateMap(elements);
        loadBlockTesr(elements);
        loadBlockColor(elements);
        loadBlockTooltips(elements);
    }

    private static void loadBlockStateMap(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModBlock.StateMap.class).forEach(data -> {
            ObjHelper.find(elements, Block.class, data).ifPresent(block -> {
                Map<String, Object> info = data.getAnnotationInfo();
                String suffix = (String) info.get("suffix");
                Object propertyName = info.get("name");
                List<?> propertyIgnore = (List<?>) info.getOrDefault("ignores", Collections.emptyList());
                List<IProperty<?>> ignores = new ArrayList<>();
                propertyIgnore.forEach(object -> RefHelper.get(elements, object, IProperty.class).ifPresent(ignores::add));
                net.minecraft.client.renderer.block.statemap.StateMap.Builder builder = new net.minecraft.client.renderer.block.statemap.StateMap.Builder()
                        .withSuffix(suffix)
                        .ignore(ignores.toArray(new IProperty<?>[0]));
                RefHelper.get(elements, propertyName, IProperty.class).ifPresent(builder::withName);
                client.blockStateMaps.put(block, builder.build());
            });
        });
        ObjHelper.stream(elements, ModBlock.StateMapper.class).forEach(data -> {
            ObjHelper.find(elements, Block.class, data).ifPresent(block -> {
                RefHelper.get(elements, ObjHelper.getDefault(data), IStateMapper.class).ifPresent(mapper -> {
                    client.blockStateMaps.put(block, mapper);
                });
            });
        });
    }

    private static void loadBlockTesr(ECModElements elements) {
        List<ImmutablePair<String, Class<? extends TileEntity>>> collect = elements.blockTileEntities.values().stream()
                .filter(te -> ITileTESR.class.isAssignableFrom(te.right))
                .collect(Collectors.toList());
        for (ImmutablePair<String, Class<? extends TileEntity>> te : collect) {
            elements.getClientNotInit().blockTesr.put(te.right, new BaseTESR());
        }
        ObjHelper.stream(elements, ModBlock.AnimTESR.class).forEach(data -> {
            ObjHelper.find(elements, Block.class, data).ifPresent(block -> {
                ImmutablePair<String, Class<? extends TileEntity>> pair = elements.blockTileEntities.get(block);
                if (pair != null) {
                    String animTesrClassName = (String) data.getAnnotationInfo().get("animationTESR");
                    if (animTesrClassName != null && !animTesrClassName.isEmpty()) {
                        //noinspection
                        Optional<TileEntitySpecialRenderer> optional = ECUtils.reflect.create(animTesrClassName, TileEntitySpecialRenderer.class, elements);
                        if (optional.isPresent()) {
                            elements.getClientNotInit().blockTesr.put(pair.right, optional.get());
                        } else {
                            elements.warn("Can't create AnimationTESR object");
                        }
                    } else {
                        elements.getClientNotInit().blockTesr.put(pair.right, new AnimationTESR<>());
                    }
                } else {
                    elements.warn("Block {}: Animation must have a tileEntity.", block.getRegistryName());
                }
            });
        });
        ObjHelper.stream(elements, ModBlock.TESR.class).forEach(data -> {
            ObjHelper.find(elements, Block.class, data).ifPresent(block -> {
                ImmutablePair<String, Class<? extends TileEntity>> pair = elements.blockTileEntities.get(block);
                String className = (String) data.getAnnotationInfo().get("value");
                if (pair != null) {
                    //noinspection
                    Optional<TileEntitySpecialRenderer> optional = ECUtils.reflect.create(className, TileEntitySpecialRenderer.class, elements);
                    if (optional.isPresent()) {
                        elements.getClientNotInit().blockTesr.put(pair.right, optional.get());
                    } else {
                        elements.warn("Can't create TileEntitySpecialRenderer object");
                    }
                } else {
                    elements.warn("Block {}: TESR must have a tileEntity.", block.getRegistryName());
                }
            });
        });
    }

    private static void loadBlockColor(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModBlock.BlockColor.class).forEach(data -> {
            ObjHelper.find(elements, Block.class, data).ifPresent(block -> {
                IBlockColor blockColor = LoaderHelperClient.getValueBlockColor(client, ObjHelper.getDefault(data));
                if (blockColor != null) {
                    if (!client.blockColors.containsKey(blockColor)) {
                        client.blockColors.put(blockColor, new ArrayList<>());
                    }
                    client.blockColors.get(blockColor).add(block);
                }
                IItemColor itemColor = LoaderHelperClient.getValueItemColor(client, (int) data.getAnnotationInfo().getOrDefault("item", -1));
                if (itemColor != null) {
                    if (!client.blockItemColors.containsKey(itemColor)) {
                        client.blockItemColors.put(itemColor, new ArrayList<>());
                    }
                    client.blockItemColors.get(itemColor).add(block);
                }
            });
        });
    }

    private static void loadBlockTooltips(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModBlock.Tooltip.class).forEach(data -> {
            List<String> tooltips = ObjHelper.getDefault(data);
            if (!tooltips.isEmpty()) {
                ObjHelper.find(elements, Block.class, data).ifPresent(block -> {
                    client.tooltips.add((stack, strings) -> {
                        if (stack.getItem() == Item.getItemFromBlock(block)) {
                            strings.addAll(tooltips);
                        }
                    });
                });
            }
        });
    }
}
