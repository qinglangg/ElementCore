package com.elementtimes.elementcore.api.common.helper;

import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import com.elementtimes.elementcore.api.template.interfaces.invoker.*;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Method/Field 注解系列辅助方法
 * @author luqin2007
 */
public class RefHelper {

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.Getter
     * @see com.elementtimes.elementcore.api.annotation.part.Getter2
     */
    public static <T> Optional<? extends T> get(@Nonnull ECModElements elements, @Nullable Object getter, @Nonnull Class<? extends T> type) {
        return Optional.ofNullable(getter(elements, getter, type).get());
    }
    public static <T> Supplier<T> getter(@Nonnull ECModElements elements, @Nullable Object getter, @Nonnull Class<? extends T> objType) {
        Map<String, Object> getterMap = ObjHelper.getAnnotationMap(getter);
        if (getterMap == null || !getterMap.containsKey("value") || "".equals(getterMap.get("name"))) {
            return () -> null;
        }
        Object type = getterMap.get("value");
        String className;
        if (type instanceof String) {
            className = (String) type;
            if (className.isEmpty()) {
                return () -> null;
            }
        } else if (type instanceof Type) {
            className = ((Type) type).getClassName();
            if (Getter.class.getName().equals(className)) {
                return () -> null;
            }
        } else {
            return () -> null;
        }
        Optional<Class<?>> optional = ObjHelper.findClass(elements, className);
        if (optional.isPresent()) {
            Class<?> aClass = optional.get();
            String name = (String) getterMap.get("name");
            if (name == null || "<init>".equals(name)) {
                return () -> ECUtils.reflect.create(aClass, objType, elements).orElse(null);
            } else if ("()".startsWith(name)) {
                return () -> ECUtils.reflect.invoke(name.substring(2), aClass, null, objType, elements).orElse(null);
            } else {
                return () -> ECUtils.reflect.get(aClass, name, null, objType, elements).orElse(null);
            }
        } else {
            return () -> null;
        }
    }

    /**
     * @see com.elementtimes.elementcore.api.annotation.part.Method
     * @see com.elementtimes.elementcore.api.annotation.part.Method2
     */
    public static <T> Invoker<T>  invoker(@Nonnull ECModElements elements, @Nullable Object method, Invoker<T> defValue, Class<?>... argTypes) {
        return invoker(elements, method, defValue,
                (m) -> (params) -> {
                    try {
                        return (T) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue.invoke(params);
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (T) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue.invoke(params);
                    }
                },
                (c) -> (params) -> {
                    try {
                        return (T) c.newInstance(params);
                    } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        e.printStackTrace();
                        return defValue.invoke(params);
                    }
                },
                argTypes);
    }
    public static BooleanInvoker  invoker(@Nonnull ECModElements elements, @Nullable Object method, boolean defValue,    Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (boolean) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (boolean) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static ByteInvoker     invoker(@Nonnull ECModElements elements, @Nullable Object method, byte defValue,       Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (byte) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (byte) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static CharInvoker     invoker(@Nonnull ECModElements elements, @Nullable Object method, char defValue,       Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (char) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (char) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static DoubleInvoker   invoker(@Nonnull ECModElements elements, @Nullable Object method, double defValue,     Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (int) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (int) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static FloatInvoker    invoker(@Nonnull ECModElements elements, @Nullable Object method, float defValue,      Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (int) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (int) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static IntInvoker      invoker(@Nonnull ECModElements elements, @Nullable Object method, int defValue,        Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (int) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (int) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static LongInvoker     invoker(@Nonnull ECModElements elements, @Nullable Object method, long defValue,       Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> defValue,
                (m) -> (params) -> {
                    try {
                        return (long) m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        return (long) m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return defValue;
                    }
                },
                (c) -> (params) -> defValue, argTypes);
    }
    public static VoidInvoker     invoker(@Nonnull ECModElements elements, @Nullable Object method,                      Class<?>... argTypes) {
        return invoker(elements, method,
                (params) -> {},
                (m) -> (params) -> {
                    try {
                        m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                (c) -> (params) -> {}, argTypes);
    }
    public static <T> Optional<T> invoke(@Nonnull ECModElements elements, @Nullable Object method, @Nonnull Class<? extends T> type, Object[] args, Class<?>... argTypes) {
        return Optional.ofNullable(invoker(elements, method, null, argTypes).invoke(args)).filter(type::isInstance).map(o -> (T) o);
    }
    public static boolean         invoke(@Nonnull ECModElements elements, @Nullable Object method, boolean defValue,                 Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static byte            invoke(@Nonnull ECModElements elements, @Nullable Object method, byte defValue,                    Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static char            invoke(@Nonnull ECModElements elements, @Nullable Object method, char defValue,                    Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static double          invoke(@Nonnull ECModElements elements, @Nullable Object method, double defValue,                  Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static float           invoke(@Nonnull ECModElements elements, @Nullable Object method, float defValue,                   Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static int             invoke(@Nonnull ECModElements elements, @Nullable Object method, int defValue,                     Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static long            invoke(@Nonnull ECModElements elements, @Nullable Object method, long defValue,                    Object[] args, Class<?>... argTypes) {
        return invoker(elements, method, defValue, argTypes).invoke(args);
    }
    public static void            invoke(@Nonnull ECModElements elements, @Nullable Object method,                                   Object[] args, Class<?>... argTypes) {
        invoker(elements, method, argTypes).invoke(args);
    }

    public static VoidInvoker invokerNullable(@Nonnull ECModElements elements, @Nullable Object method, Class<?>... argTypes) {
        return invoker(elements, method, null,
                (m) -> (params) -> {
                    try {
                        m.invoke(null, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                (holder, m) -> (params) -> {
                    try {
                        m.invoke(holder.get(), params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                (c) -> (params) -> {}, argTypes);
    }

    private static <T> T invoker(@Nonnull ECModElements elements, @Nullable Object method,
                                 @Nullable T defValue, Function<java.lang.reflect.Method, T> invokeStatic,
                                 BiFunction<Supplier<Object>, java.lang.reflect.Method, T> invoke, Function<Constructor<?>, T> newInstance,
                                 Class<?>... argTypes) {
        Map<String, Object> methodMap = ObjHelper.getAnnotationMap(method);
        if (methodMap == null) {
            return defValue;
        }
        String className;
        Object container = methodMap.get("value");
        if (container instanceof String) {
            className = (String) container;
            if (className.isEmpty()) {
                return defValue;
            }
        } else if (container instanceof Type) {
            className = ((Type) container).getClassName();
            if (Method.class.getName().equals(className)) {
                return defValue;
            }
        } else {
            return defValue;
        }
        String methodName = (String) methodMap.getOrDefault("name", "<init>");
        if (StringUtils.isNullOrEmpty(methodName)) {
            return defValue;
        }
        Optional<Class<?>> optional = ObjHelper.findClass(elements, className);
        if (optional.isPresent()) {
            try {
                Class<?> aClass = optional.get();
                if ("<init>".equals(methodName)) {
                    Constructor<?> constructor = aClass.getDeclaredConstructor(argTypes);
                    constructor.setAccessible(true);
                    return newInstance.apply(constructor);
                } else {
                    java.lang.reflect.Method m = ReflectionHelper.findMethod(aClass, methodName, methodName, argTypes);
                    if ((boolean) methodMap.getOrDefault("isStatic", true)) {
                        return invokeStatic.apply(m);
                    } else {
                        Supplier<Object> holder = getter(elements, methodMap.get("holder"), Object.class);
                        return invoke.apply(holder, m);
                    }
                }
            } catch (Exception e) {
                return defValue;
            }
        }
        return defValue;
    }

    public static String toString(@Nullable Object method) {
        Map<String, Object> methodMap = ObjHelper.getAnnotationMap(method);
        if (methodMap != null && methodMap.containsKey("value")) {
            Object value = methodMap.get("value");
            Object name = methodMap.get("name");
            String className = value instanceof Type ? ((Type) value).getClassName() : (String) value;
            String methodName = name == null ? "<init>" : name.toString();
            return className + "#" + methodName;
        }
        return "NO METHOD OR GETTER";
    }
}
