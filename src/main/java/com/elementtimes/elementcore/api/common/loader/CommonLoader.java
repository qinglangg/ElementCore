package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.tools.*;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonLoader {

    public static void load(ECModElements elements) {
        elements.warn("[COMMON]load " + elements.container.id());
        loadConfig(elements);
        TabLoader.load(elements);
        CapabilityLoader.load(elements);
        BlockLoader.load(elements);
        ItemLoader.loader(elements);
        FluidLoader.load(elements);
        PotionLoader.load(elements);
        EnchantmentLoader.load(elements);
        EntityLoader.load(elements);
        RecipeLoader.load(elements);
        NetworkLoader.load(elements);
        CommandLoader.load(elements);
        GuiLoader.load(elements);
        BookLoader.load(elements);
        loadStaticFunction(elements);
        loadBurnTime(elements);
        loadOreName(elements);
        elements.warn("[COMMON]load finished");
    }

    private static void loadConfig(ECModElements elements) {
        ObjHelper.stream(elements, ModConfig.class).forEach(data -> {
            Map<String, Object> info = data.getAnnotationInfo();
            if (!elements.blockObj && (boolean) info.getOrDefault("useOBJ", false)) {
                elements.blockObj = true;
            }
            if (!elements.blockB3d && (boolean) info.getOrDefault("useB3D", false)) {
                elements.blockB3d = true;
            }
            elements.warn("[ModConfig]obj={}, b3d={}", elements.blockObj, elements.blockB3d);
        });
    }

    private static void loadStaticFunction(ECModElements elements) {
        ObjHelper.stream(elements, ModInvokeStatic.class).forEach(data -> {
            ObjHelper.findClass(elements, data.getClassName()).ifPresent(aClass -> {
                String value = ObjHelper.getDefault(data);
                Method method = null;
                try {
                    method = aClass.getMethod(value);
                    if (method == null) {
                        method = aClass.getDeclaredMethod(value);
                        if (method != null) {
                            method.setAccessible(true);
                        }
                    }
                } catch (NoSuchMethodException ignored) { }
                if (method != null) {
                    elements.warn("[ModInvokeStatic]{}#{}", data.getClassName(), value);
                    elements.staticFunction.add(method);
                } else {
                    elements.warn("Skip Function: {} from {}", value, data.getClassName());
                }
            });
        });
    }

    private static void loadBurnTime(ECModElements elements) {
        ObjHelper.stream(elements, ModBurnTime.class).forEach(data -> {
            ObjHelper.find(elements, Object.class, data)
                    .filter(o -> o instanceof Item || o instanceof Block || o instanceof Fluid)
                    .ifPresent(o -> {
                Map<String, Object> info = data.getAnnotationInfo();
                int defValue = (int) info.get("value");
                List<HashMap<String, Object>> subTimes = (List<HashMap<String, Object>>) info.get("sub");
                boolean hasSubTime = subTimes != null && !subTimes.isEmpty();
                Object name = o instanceof IForgeRegistryEntry ? ((IForgeRegistryEntry) o).getRegistryName() : ((Fluid) o).getName();
                elements.warn("[ModBurnTime]{} default={}, subCount={}", name, defValue, subTimes == null ? 0 : subTimes.size());
                elements.burnTimes.put(o, stack -> {
                    if (hasSubTime) {
                        for (HashMap<String, Object> map : subTimes) {
                            int[] acceptMeta = (int[]) map.getOrDefault("metadata", new int[0]);
                            if (acceptMeta.length == 0 || ArrayUtils.contains(acceptMeta, stack.getMetadata())) {
                                int defBurnTime = (int) map.getOrDefault("value", -1);
                                if (defBurnTime < 0) {
                                    HashMap<String, Object> methodMap = (HashMap<String, Object>) map.get("method");
                                    return RefHelper.invoke(elements, methodMap, -1, new Object[]{stack}, ItemStack.class);
                                }
                                return defBurnTime;
                            }
                        }
                    }
                    return defValue;
                });
            });
        });
    }

    private static void loadOreName(ECModElements elements) {
        ObjHelper.stream(elements, ModOreDict.class).forEach(data -> {
            ObjHelper.find(elements, IForgeRegistryEntry.class, data).ifPresent(entry -> {
                List<String> names = ObjHelper.getDefault(data);
                elements.warn("[ModOreDict]{}", entry.getRegistryName());
                names.forEach(name -> elements.warn("[ModOreDict] -> {}", name));
                if (entry instanceof Item) {
                    names.forEach(name -> ECUtils.collection.computeIfAbsent(elements.itemOreNames, name, ArrayList::new).add((Item) entry));
                } else if (entry instanceof Block) {
                    names.forEach(name -> ECUtils.collection.computeIfAbsent(elements.blockOreNames, name, ArrayList::new).add((Block) entry));
                }
            });
        });
    }
}
