package com.elementtimes.elementcore.api.client.loader;

import com.elementtimes.elementcore.api.annotation.tools.ModColor;
import com.elementtimes.elementcore.api.annotation.tools.ModColorObj;
import com.elementtimes.elementcore.api.annotation.tools.ModTooltip;
import com.elementtimes.elementcore.api.client.ECModElementsClient;
import com.elementtimes.elementcore.api.client.LoaderHelperClient;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.VoidInvoker;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class CommonClientLoader {

    public static void load(ECModElements elements) {
        elements.warn("[CLIENT]load: {}", elements.container.id());
        BlockClientLoader.load(elements);
        ItemClientLoader.load(elements);
        EntityClientLoader.load(elements);
        CommandClientLoader.load(elements);
        KeyClientLoader.load(elements);
        loadColor(elements);
        loadTooltips(elements);
        elements.warn("[CLIENT]load finished");
    }

    private static void loadColor(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModColor.class).forEach(data -> {
            ObjHelper.find(elements, Object.class, data).ifPresent(obj -> {
                if (obj instanceof Item) {
                    Object itemColorRef = data.getAnnotationInfo().get("item");
                    IItemColor itemColor = LoaderHelperClient.getMethodItemColor(elements, itemColorRef);
                    if (itemColor != null) {
                        ECUtils.collection.computeIfAbsent(client.itemColors, itemColor, ArrayList::new).add((Item) obj);
                    }
                    elements.warn("[ModColor]{}, item={}", ((Item) obj).getRegistryName(), RefHelper.toString(itemColorRef));
                } else if (obj instanceof Block) {
                    Object blockColorRef = data.getAnnotationInfo().get("block");
                    Object itemColorRef = data.getAnnotationInfo().get("item");
                    IBlockColor blockColor = LoaderHelperClient.getMethodBlockColor(elements, blockColorRef);
                    if (blockColor != null) {
                        ECUtils.collection.computeIfAbsent(client.blockColors, blockColor, ArrayList::new).add((Block) obj);
                    }
                    IItemColor itemColor = LoaderHelperClient.getMethodItemColor(elements, itemColorRef);
                    if (itemColor != null) {
                        ECUtils.collection.computeIfAbsent(client.blockItemColors, itemColor, ArrayList::new).add((Block) obj);
                    }
                    elements.warn("[ModColor]{}, block={}, item={}", ((Block) obj).getRegistryName(), RefHelper.toString(blockColorRef), RefHelper.toString(itemColorRef));
                }
            });
        });
        ObjHelper.stream(elements, ModColorObj.class).forEach(data -> {
            ObjHelper.find(elements, Object.class, data).ifPresent(obj -> {
                if (obj instanceof Item) {
                    Object itemColorRef = data.getAnnotationInfo().get("item");
                    IItemColor itemColor = LoaderHelperClient.getObjectItemColor(elements, itemColorRef);
                    if (itemColor != null) {
                        ECUtils.collection.computeIfAbsent(client.itemColors, itemColor, ArrayList::new).add((Item) obj);
                    }
                    elements.warn("[ModColorObj]{}, item={}", ((Item) obj).getRegistryName(), itemColor);
                } else if (obj instanceof Block) {
                    Object blockColorRef = data.getAnnotationInfo().get("block");
                    Object itemColorRef = data.getAnnotationInfo().get("item");
                    IBlockColor blockColor = LoaderHelperClient.getObjectBlockColor(elements, blockColorRef);
                    if (blockColor != null) {
                        ECUtils.collection.computeIfAbsent(client.blockColors, blockColor, ArrayList::new).add((Block) obj);
                    }
                    IItemColor itemColor = LoaderHelperClient.getObjectItemColor(elements, itemColorRef);
                    if (itemColor != null) {
                        ECUtils.collection.computeIfAbsent(client.blockItemColors, itemColor, ArrayList::new).add((Block) obj);
                    }
                    elements.warn("[ModColorObj]{}, block={}, item={}", ((Block) obj).getRegistryName(), RefHelper.toString(blockColorRef), RefHelper.toString(itemColorRef));
                }
            });
        });
    }

    private static void loadTooltips(ECModElements elements) {
        ECModElementsClient client = elements.getClientNotInit();
        ObjHelper.stream(elements, ModTooltip.class).forEach(data -> {
            ObjHelper.find(elements, Object.class, data).ifPresent(o -> {
                Predicate<ItemStack> p;
                if (o instanceof Item) {
                    p = s -> s.getItem() == o;
                } else if (o instanceof Block) {
                    p = s -> s.getItem() == Item.getItemFromBlock((Block) o);
                } else if (o instanceof Fluid) {
                    p = s -> ECUtils.fluid.hasFluid(FluidUtil.getFluidHandler(s), (Fluid) o);
                } else if (o instanceof Enchantment) {
                    p = s -> EnchantmentHelper.getEnchantments(s).containsKey(o);
                } else if (o instanceof Class) {
                    Class<?> aClass = (Class<?>) o;
                    if (Item.class.isAssignableFrom(aClass)) {
                        p = s -> aClass.isInstance(s.getItem());
                    } else if (Block.class.isAssignableFrom(aClass)) {
                        p = s -> aClass.isInstance(Block.getBlockFromItem(s.getItem()));
                    } else if (Fluid.class.isAssignableFrom(aClass)) {
                        p = s -> {
                            for (FluidStack stack : ECUtils.fluid.toList(FluidUtil.getFluidHandler(s))) {
                                if (aClass.isInstance(stack.getFluid())) {
                                    return true;
                                }
                            }
                            return false;
                        };
                    } else if (Enchantment.class.isAssignableFrom(aClass)) {
                        p = s -> {
                            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(s);
                            for (Enchantment enchantment : enchantments.keySet()) {
                                if (aClass.isInstance(enchantment)) {
                                    return true;
                                }
                            }
                            return false;
                        };
                    } else if (Entity.class.isAssignableFrom(aClass)) {
                        p = s -> {
                            ResourceLocation entityId = ItemMonsterPlacer.getNamedIdFrom(s);
                            if (entityId != null) {
                                Class<? extends Entity> entityClass = EntityList.getClass(entityId);
                                return entityClass != null && entityClass.isAssignableFrom(aClass);
                            }
                            return false;
                        };
                    } else {
                        p = s -> true;
                    }
                } else {
                    p = s -> true;
                }
                Object aDefault = ObjHelper.getDefault(data);
                VoidInvoker invoker = RefHelper.invoker(elements, aDefault, ItemStack.class, List.class);
                elements.warn("[ModTooltip]Tooltip: {} {}", o, RefHelper.toString(aDefault));
                client.tooltips.add((stack, strings) -> {
                    if (stack != null && p.test(stack)) {
                        invoker.invoke(stack, strings);
                    }
                });
            });
        });
    }
}
