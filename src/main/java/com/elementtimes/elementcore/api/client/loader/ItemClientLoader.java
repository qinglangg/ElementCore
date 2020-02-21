package com.elementtimes.elementcore.api.client.loader;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.client.ECModElementsClient;
import com.elementtimes.elementcore.api.client.LoaderHelperClient;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.Invoker;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 加载物品（客户端）
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ItemClientLoader {

    public static void load(ECModElements elements) {
        loadItemSub(elements);
        loadItemMeshDefinition(elements);
        loadItemColor(elements);
    }

    private static void loadItemSub(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModItem.HasSubItem.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                Map<String, Object> info = data.getAnnotationInfo();
                int[] metadatas = (int[]) info.get("metadatas");
                List<String> models = (List<String>) info.get("models");
                if (metadatas != null && models != null) {
                    int index = Math.min(metadatas.length, models.size());
                    if (index > 0) {
                        ArrayList<SubModel> modelTriples = new ArrayList<>(index);
                        for (int i = 0; i < index; i++) {
                            modelTriples.add(new SubModel(metadatas[i], elements.container.id(), models.get(i)));
                        }
                        client.itemSubModel.put(item, modelTriples);
                    }
                }
            });
        });
    }

    private static void loadItemColor(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModItem.ItemColor.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                IItemColor itemColor = LoaderHelperClient.getValueItemColor(client, ObjHelper.getDefault(data));
                if (itemColor != null) {
                    if (!client.itemColors.containsKey(itemColor)) {
                        client.itemColors.put(itemColor, new ArrayList<>());
                    }
                    client.itemColors.get(itemColor).add(item);
                }
            });
        });
    }

    private static void loadItemMeshDefinition(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModItem.MeshDefinitionObj.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                RefHelper.get(elements, ObjHelper.getDefault(data), ItemMeshDefinition.class).ifPresent(definition -> {
                    client.itemMeshDefinition.put(item, definition);
                });
            });
        });
        ObjHelper.stream(elements, ModItem.MeshDefinitionFunc.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                Invoker<ModelResourceLocation> invoker = RefHelper.invoker(elements, ObjHelper.getDefault(data), Invoker.empty(), ItemStack.class);
                client.itemMeshDefinition.put(item, stack -> invoker.invoke(stack));
            });
        });
    }

    public static class SubModel {
        public final int metadata;
        public final String path;
        public final String property;
        public final String domain;
        public final ResourceLocation resourceLocation;
        public final ModelResourceLocation modelResourceLocation;

        public SubModel(int metadata, String modid, String key) {
            this.metadata = metadata;

            int i = key.indexOf(":");
            if (i > 0) {
                domain = key.substring(0, i);
                int j = key.indexOf("#", i);
                if (j > 0) {
                    path = key.substring(i+1, j);
                    property = key.substring(j+1);
                } else {
                    path = key.substring(i+1);
                    property = "";
                }
            } else {
                domain = modid;
                int j = key.indexOf("#");
                if (j > 0) {
                    path = key.substring(0, j);
                    property = key.substring(j+1);
                } else {
                    path = key;
                    property = "inventory";
                }
            }

            resourceLocation = new ResourceLocation(domain, path);
            modelResourceLocation = new ModelResourceLocation(resourceLocation, property);
        }

        public SubModel(int metadata, String key) {
            this(metadata, "minecraft", key);
        }

        public SubModel(int metadata, String domain, String path, String property) {
            this.metadata = metadata;
            this.domain = domain;
            this.path = path;
            this.property = property;

            resourceLocation = new ResourceLocation(domain, path);
            modelResourceLocation = new ModelResourceLocation(resourceLocation, property);
        }

        @Override
        public String toString() {
            return "model=" + domain + ":" + path + "#" + property;
        }
    }
}
