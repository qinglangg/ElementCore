package com.elementtimes.elementcore.api.utils;

import com.elementtimes.elementcore.api.misc.tool.ConstructorObj;
import com.elementtimes.elementcore.api.misc.tool.FieldObj;
import com.elementtimes.elementcore.api.misc.tool.MethodObj;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 处理反射有关的方法
 *
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ReflectUtils {

    public static MethodObj findMethod(@Nonnull Class<?> aClass, Object obj, String name, Class<?>... types) {
        try {
            return new MethodObj(aClass.getDeclaredMethod(name, types), obj);
        } catch (Exception e0) {
            try {
                return new MethodObj(aClass.getMethod(name, types), obj);
            } catch (Exception e2) {
                return MethodObj.EMPTY;
            }
        }
    }

    public static MethodObj findMethod2(@Nonnull Class<?> aClass, Object obj, String name, String name2, Class<?>... types) {
        MethodObj method = findMethod(aClass, obj, name, types);
        if (!method.hasContent()) {
            return findMethod(aClass, obj, name2, types);
        }
        return method;
    }

    public static FieldObj findField(@Nonnull Class<?> aClass, Object object, String... names) {
        for (String name : names) {
            try {
                return new FieldObj(aClass.getDeclaredField(name), object);
            } catch (Exception e) {
                try {
                    return new FieldObj(aClass.getField(name), object);
                } catch (Exception ignored) { }
            }
        }
        return FieldObj.EMPTY;
    }

    public static ConstructorObj findConstructor(@Nonnull Class<?> aClass, @Nullable Class<?> type, @Nonnull Class<?>... parameterTypes) {
        try {
            return new ConstructorObj(aClass.getDeclaredConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            return ConstructorObj.EMPTY;
        }
    }

    public static boolean isAssignableFrom(Class<?> parent, Class<?> child) {
        return child != null
                && (parent == null || parent == Object.class || parent == child || parent.isAssignableFrom(child));
//        return isAssignableFrom(parent, child, int.class    , Integer.class)
//            || isAssignableFrom(parent, child, char.class   , Character.class)
//            || isAssignableFrom(parent, child, short.class  , Short.class)
//            || isAssignableFrom(parent, child, byte.class   , Byte.class)
//            || isAssignableFrom(parent, child, boolean.class, Boolean.class)
//            || isAssignableFrom(parent, child, float.class  , Float.class)
//            || isAssignableFrom(parent, child, double.class , Double.class)
//            || isAssignableFrom(parent, child, long.class   , Long.class);
    }

    private static boolean isAssignableFrom(Class<?> parent, Class<?> child, Class<?> c0, Class<?> c1) {
        if (parent == c0) {
            return child == c1;
        }
        return false;
    }

    public static boolean canAccept(Class<?> type, Object o) {
        if (type == null) {
            return true;
        }
        return (type == int.class     && o instanceof Integer)
            || (type == char.class    && o instanceof Character)
            || (type == short.class   && o instanceof Short)
            || (type == byte.class    && o instanceof Byte)
            || (type == boolean.class && o instanceof Boolean)
            || (type == float.class   && o instanceof Float)
            || (type == double.class  && o instanceof Double)
            || (type == long.class    && o instanceof Long)
            || o == null || type.isInstance(o);
    }
}