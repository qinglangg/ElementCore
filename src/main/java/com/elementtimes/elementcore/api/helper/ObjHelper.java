package com.elementtimes.elementcore.api.helper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.utils.CommonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 有关注解对象与注册对象的辅助类
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
public class ObjHelper {

    public static <T> Optional<T> find(@Nonnull ECModElements elements, ModFileScanData.AnnotationData data, @Nonnull FindOptions<T> option) {
        return option.get(elements, data);
    }

    public static <T> Optional<Class<? extends T>> findClass(@Nonnull ECModElements elements, @Nonnull String className) {
        Class<? extends T> clazz = (Class<? extends T>) elements.classes.get(className);
        if (clazz == null) {
            try {
                clazz = (Class<? extends T>) Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                elements.warn("Can't find class: {}", className);
            }
            if (clazz != null) {
                elements.classes.put(className, clazz);
            }
        }
        return Optional.ofNullable(clazz);
    }

    public static <T> Optional<Class<? extends T>> findClass(@Nonnull ECModElements elements, @Nonnull Type className) {
        return findClass(elements, className.getClassName());
    }

    public static Stream<ModFileScanData.AnnotationData> stream(@Nonnull ECModElements elements, Class<? extends Annotation> annotation) {
        return CommonUtils.findScanData(elements.container.id())
                .map(sd -> sd.getAnnotations().stream().filter(data -> data.getAnnotationType().getClassName().equals(annotation.getName())))
                .orElseGet(Stream::empty);
    }

    public static <T> T getDefault(ModFileScanData.AnnotationData data) {
        return (T) data.getAnnotationData().get("value");
    }

    public static <T> T getDefault(ModFileScanData.AnnotationData data, T defVal) {
        return (T) data.getAnnotationData().getOrDefault("value", defVal);
    }

    public static int getDefault(ModFileScanData.AnnotationData data, int defVal) {
        return (int) data.getAnnotationData().getOrDefault("value", defVal);
    }

    public static boolean getDefault(ModFileScanData.AnnotationData data, boolean defVal) {
        return (boolean) data.getAnnotationData().getOrDefault("value", defVal);
    }

    public static double getDefault(ModFileScanData.AnnotationData data, double defVal) {
        return (double) data.getAnnotationData().getOrDefault("value", defVal);
    }

    public static String getMemberName(ModFileScanData.AnnotationData data) {
        if (data.getTargetType() == ElementType.FIELD) {
            return data.getMemberName();
        } else if (data.getTargetType() == ElementType.METHOD) {
            String memberName = data.getMemberName();
            return memberName.substring(memberName.indexOf(")") + 1);
        } else {
            String className = data.getClassType().getClassName();
            return className.substring(className.indexOf(".") + 1);
        }
    }

    public static <E extends Enum<E>> E getEnum(Class<E> type, Object value, E defValue) {
        if (value instanceof ModAnnotation.EnumHolder) {
            return Enum.valueOf(type, ((ModAnnotation.EnumHolder) value).getValue());
        }
        return defValue;
    }

    @Nullable
    public static Map<String, Object> getAnnotationMap(Object object) {
        Map<String, Object> map;
        if (object instanceof Map) {
            map = (Map<String, Object>) object;
        } else if (object instanceof ModFileScanData.AnnotationData) {
            map = ((ModFileScanData.AnnotationData) object).getAnnotationData();
        } else {
            return null;
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    public static void setRegisterName(ForgeRegistryEntry<?> entry, String name, String defName, ECModElements elements) {
        String register = StringUtils.isNullOrEmpty(name) ? defName : name;
        register = register.replace("$", ".");
        register = register.substring(register.lastIndexOf(".") + 1);
        if (entry.getRegistryName() == null) {
            if (register.contains(":")) {
                entry.setRegistryName(new ResourceLocation(register.toLowerCase()));
            } else {
                entry.setRegistryName(new ResourceLocation(elements.container.id(), register.toLowerCase()));
            }
        }
    }

    public static void setRegisterName(ForgeRegistryEntry<?> entry, String name, ModFileScanData.AnnotationData data, ECModElements elements) {
        setRegisterName(entry, name, getMemberName(data), elements);
    }

    public static void setRegisterName(ForgeRegistryEntry<?> from, ForgeRegistryEntry<?> to) {
        ResourceLocation registryName = from.getRegistryName();
        if (to.getRegistryName() == null && registryName != null) {
            to.setRegistryName(registryName);
        }
    }

    public static <T, C> void saveResult(FindOptions<T> result, Map<Class<? extends C>, T> map) {
        if (result.isFound && result.createType != ElementType.FIELD) {
            map.put((Class<? extends C>) result.aClass, result.result);
        }
    }

    public static <T, C> void saveResult(FindOptions<?> key, T value, Map<Class<? extends C>, T> map) {
        if (key.hasClass && value != null) {
            map.put((Class<? extends C>) key.aClass, value);
        }
    }
}
