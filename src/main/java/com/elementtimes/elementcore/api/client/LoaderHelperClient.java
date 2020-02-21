package com.elementtimes.elementcore.api.client;

import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;
import com.elementtimes.elementcore.api.common.helper.RefHelper;
import com.elementtimes.elementcore.api.template.interfaces.invoker.IntInvoker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * 客户端的注册辅助类
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class LoaderHelperClient {

    public static IBlockColor getValueBlockColor(ECModElementsClient client, int value) {
        if (value == -1) {
            return null;
        }
        IBlockColor color = client.blockValueColors.get(value);
        if (color == null) {
            client.blockValueColors.put(value, (state, worldIn, pos, tintIndex) -> value);
        }
        return color;
    }

    public static IBlockColor getMethodBlockColor(ECModElements elements, @Nullable Object method) {
        String key = key(method);
        if (key != null) {
            ECModElementsClient client = elements.getClientNotInit();
            IntInvoker invoker = RefHelper.invoker(elements, method, 0, IBlockState.class, IBlockAccess.class, BlockPos.class, int.class);
            return client.blockMethodColors.computeIfAbsent(key, k ->
                    (state, worldIn, pos, tintIndex) -> invoker.invoke(state, worldIn, pos, tintIndex));
        }
        return null;
    }

    public static IBlockColor getObjectBlockColor(ECModElements elements, @Nullable Object object) {
        String key = key(object);
        if (key != null) {
            ECModElementsClient client = elements.getClientNotInit();
            if (client.blockObjectColors.containsKey(key)) {
                return client.blockObjectColors.get(key);
            } else {
                Optional<? extends IBlockColor> colorOpt = RefHelper.get(elements, object, IBlockColor.class);
                if (colorOpt.isPresent()) {
                    IBlockColor color = colorOpt.get();
                    client.blockObjectColors.put(key, color);
                    return color;
                }
            }
        }
        return null;
    }

    public static IItemColor getValueItemColor(ECModElementsClient client, int value) {
        if (value == -1) {
            return null;
        }
        IItemColor color = client.itemValueColors.get(value);
        if (color == null) {
            client.itemValueColors.put(value, (stack, tintIndex) -> value);
        }
        return color;
    }

    public static IItemColor getMethodItemColor(ECModElements elements, @Nullable Object method) {
        String key = key(method);
        if (key != null) {
            ECModElementsClient client = elements.getClientNotInit();
            IntInvoker invoker = RefHelper.invoker(elements, method, 0, ItemStack.class, int.class);
            return client.itemMethodColors.computeIfAbsent(key, k -> (stack, tintIndex) -> invoker.invoke(stack, tintIndex));
        }
        return null;
    }

    public static IItemColor getObjectItemColor(ECModElements elements, @Nullable Object object) {
        String key = key(object);
        if (key != null) {
            ECModElementsClient client = elements.getClientNotInit();
            if (client.itemObjectColors.containsKey(key)) {
                return client.itemObjectColors.get(key);
            } else {
                Optional<? extends IItemColor> colorOpt = RefHelper.get(elements, object, IItemColor.class);
                if (colorOpt.isPresent()) {
                    IItemColor color = colorOpt.get();
                    client.itemObjectColors.put(key, color);
                    return color;
                }
            }
        }
        return null;
    }

    private static String key(Object obj) {
        Map<String, Object> map = ObjHelper.getAnnotationMap(obj);
        if (map != null) {
            String value = (String) map.get("value");
            String name = (String) map.get("name");
            if (!StringUtils.isNullOrEmpty(value) && !StringUtils.isNullOrEmpty(name)) {
                return value + "." + name;
            }
        }
        return null;
    }
}
