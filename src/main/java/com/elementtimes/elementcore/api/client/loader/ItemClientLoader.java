package com.elementtimes.elementcore.api.client.loader;

import com.elementtimes.elementcore.api.annotation.ModItem;
import com.elementtimes.elementcore.api.client.ECModElementsClient;
import com.elementtimes.elementcore.api.client.LoaderHelperClient;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
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
import java.util.Collection;
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
        loadTooltips(elements);
        loadItemMeshDefinition(elements);
        loadItemMeshDefinition2(elements);
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
                int color = ObjHelper.getDefault(data);
                IItemColor itemColor = LoaderHelperClient.getValueItemColor(client, color);
                if (itemColor != null) {
                    elements.warn("[ModItem.ItemColor]{} <- #{}", item.getRegistryName(), Integer.toHexString(color));
                    ECUtils.collection.computeIfAbsent(client.itemColors, itemColor, ArrayList::new).add(item);
                }
            });
        });
    }

    private static void loadTooltips(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModItem.Tooltip.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                List<String> tooltips = ObjHelper.getDefault(data);
                elements.warn("[ModItem.Tooltip]{}", item.getRegistryName());
                for (String tooltip : tooltips) {
                    elements.warn("[ModItem.Tooltip] -> {}", tooltip);
                }
                client.tooltips.add((stack, strings) -> {
                    if (stack.getItem() == item) {
                        strings.addAll(tooltips);
                    }
                });
            });
        });
    }

    @Deprecated
    private static void loadItemMeshDefinition(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModItem.MeshDefinitionObj.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                Object aDefault = ObjHelper.getDefault(data);
                RefHelper.get(elements, aDefault, ItemMeshDefinition.class).ifPresent(definition -> {
                    elements.warn("[ModItem.MeshDefinitionObj]{} {}", item.getRegistryName(), RefHelper.toString(aDefault));
                    client.itemMeshDefinition.put(item, definition);
                });
            });
        });
        ObjHelper.stream(elements, ModItem.MeshDefinitionFunc.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                Object aDefault = ObjHelper.getDefault(data);
                Invoker<ModelResourceLocation> invoker = RefHelper.invoker(elements, aDefault, Invoker.empty(), ItemStack.class);
                elements.warn("[ModItem.MeshDefinitionFunc]{} {}", item.getRegistryName(), RefHelper.toString(aDefault));
                client.itemMeshDefinition.put(item, invoker::invoke);
            });
        });
        ObjHelper.stream(elements, ModItem.MeshDefinitionAll.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                RefHelper.get(elements, ObjHelper.getDefault(data), Object.class).ifPresent(object -> {
                    ResourceLocation[] locations = findResourceLocations(object);
                    elements.warn("[ModItem.MeshDefinitionAll]{}", item.getRegistryName());
                    for (ResourceLocation location : locations) {
                        elements.warn("[ModItem.MeshDefinitionAll] -> {}", location);
                    }
                    client.itemMeshDefinitionAll.put(item, locations);
                });
            });
        });
    }

    private static void loadItemMeshDefinition2(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModItem.ItemMeshDefinitionObj.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                Object aDefault = ObjHelper.getDefault(data);
                RefHelper.get(elements, aDefault, ItemMeshDefinition.class).ifPresent(definition -> {
                    elements.warn("[ModItem.ItemMeshDefinitionObj]{} {}", item.getRegistryName(), RefHelper.toString(aDefault));
                    client.itemMeshDefinition.put(item, definition);
                });
                RefHelper.get(elements, data.getAnnotationInfo().get("all"), Object.class).ifPresent(object -> {
                    ResourceLocation[] locations = findResourceLocations(object);
                    for (ResourceLocation location : locations) {
                        elements.warn("[ModItem.ItemMeshDefinitionObj] -> {}", location);
                    }
                    client.itemMeshDefinitionAll.put(item, locations);
                });
            });
        });
        ObjHelper.stream(elements, ModItem.ItemMeshDefinitionFunc.class).forEach(data -> {
            ObjHelper.find(elements, Item.class, data).ifPresent(item -> {
                Object aDefault = ObjHelper.getDefault(data);
                Invoker<ModelResourceLocation> invoker = RefHelper.invoker(elements, aDefault, Invoker.empty(), ItemStack.class);
                elements.warn("[ModItem.ItemMeshDefinitionFunc]{} {}", item.getRegistryName(), RefHelper.toString(aDefault));
                client.itemMeshDefinition.put(item, stack -> invoker.invoke(stack));
                RefHelper.get(elements, data.getAnnotationInfo().get("all"), Object.class).ifPresent(object -> {
                    ResourceLocation[] locations = findResourceLocations(object);
                    for (ResourceLocation location : locations) {
                        elements.warn("[ModItem.ItemMeshDefinitionFunc] -> {}", location);
                    }
                    client.itemMeshDefinitionAll.put(item, locations);
                });
            });
        });
    }

    private static ResourceLocation[] findResourceLocations(Object object) {
        ResourceLocation[] locations;
        if (object instanceof Collection) {
            locations = (ResourceLocation[]) ((Collection) object).toArray(new ResourceLocation[0]);
        } else if (object instanceof ResourceLocation[]) {
            locations = (ResourceLocation[]) object;
        } else {
            locations = new ResourceLocation[0];
        }
        return locations;
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
