package com.elementtimes.elementcore.util;

import com.elementtimes.elementcore.ElementContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 处理反射有关的方法
 *
 * @author luqin2007
 */
@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
public class ReflectUtil {

    /**
     * 获得可被注解条目的值
     * 只能获取静态值
     */
    public static <T> Optional<T> getFromAnnotated(@Nonnull AnnotatedElement holder, @Nullable T defaultValue, ElementContainer initializer) {
        T obj;
        if (holder instanceof AccessibleObject) {
            ((AccessibleObject) holder).setAccessible(true);
        }

        if (holder instanceof Class) {
            obj = (T) create((Class) holder, initializer).orElse(defaultValue);
        } else if (holder instanceof Field) {
            obj = get((Field) holder, null, defaultValue, true, initializer).orElse(defaultValue);
        } else if (holder instanceof Constructor) {
            obj = (T) create((Constructor<T>) holder, initializer).orElse(defaultValue);
        } else if (holder instanceof Method) {
            obj = (T) invoke((Method) holder, null, initializer).orElse(defaultValue);
        } else {
            obj = defaultValue;
        }
        return Optional.ofNullable(obj);
    }

    /**
     * 使用无参构造创建对象
     *
     * @param aClass 要创建的对象类
     */
    public static Optional<Object> create(@Nonnull Class aClass, ElementContainer initializer) {
        Object object = null;
        try {
            object = aClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            initializer.modInfo.warn("Cannot create an instance of {}. Please make sure the class has a public constructor with zero parameter.", aClass.getSimpleName());
            e.printStackTrace();
        }
        return Optional.ofNullable(object);
    }

    /**
     * 使用有参构造创建对象
     * @param aClass 要创建的对象类
     */
    public static Optional create(@Nonnull Class aClass, Object[] params, ElementContainer initializer) {
        Object object = null;
        try {
            Class[] paramClass = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);
            try {
                Constructor<?> constructor = Arrays.stream(aClass.getConstructors())
                        .filter(c -> c.getParameterCount() == paramClass.length)
                        .filter(c -> {
                            Class<?>[] parameterTypes = c.getParameterTypes();
                            for (int i = 0; i < paramClass.length; i++) {
                                if (!parameterTypes[i].isAssignableFrom(paramClass[i])) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .findFirst()
                        .orElseThrow(NullPointerException::new);
                object = constructor.newInstance(params);
            } catch (InvocationTargetException | NullPointerException e) {
                String[] classNames = Arrays.stream(paramClass).map(Class::getSimpleName).toArray(String[]::new);
                initializer.modInfo.warn("Cannot find constructor with param types: {}", Arrays.toString(classNames));
                object = aClass.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            initializer.modInfo.warn("Cannot create an instance of {}. Please make sure the class has a public constructor with zero parameter.", aClass.getSimpleName());
            e.printStackTrace();
        }
        return Optional.ofNullable(object);
    }

    /**
     * 使用有参构造创建对象
     *
     * @param className 要创建的对象类的全类名
     */
    public static Optional create(@Nonnull String className, Object[] params, ElementContainer initializer) {
        if (className.isEmpty()) {
            initializer.modInfo.warn("You want to find an EMPTY class.");
        } else {
            try {
                Class<?> aClass = Class.forName(className);
                return create(aClass, params, initializer);
            } catch (ClassNotFoundException e) {
                initializer.modInfo.warn("Class {} is not exist. Please make sure the class is exist and the ClassLoader can reload the class", className);
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    /**
     * 使用无参构造创建对象
     *
     * @param className 要创建的对象类的全类名
     */
    public static Optional create(@Nonnull String className, ElementContainer initializer) {
        Object object = null;
        if (className.isEmpty()) {
            initializer.modInfo.warn("You want to find an EMPTY class.");
        } else {
            try {
                Class<?> aClass = Class.forName(className);
                object = create(aClass, initializer).orElse(null);
            } catch (ClassNotFoundException e) {
                initializer.modInfo.warn("Class {} is not exist. Please make sure the class is exist and the ClassLoader can reload the class", className);
                e.printStackTrace();
            }
        }
        return Optional.ofNullable(object);
    }

    public static Optional create(@Nonnull Constructor constructor, ElementContainer initializer) {
        constructor.setAccessible(true);
        Object t = null;
        try {
            t = constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            initializer.modInfo.warn("Constructor {} can not invoke", constructor.getName());
            e.printStackTrace();
        }
        return Optional.ofNullable(t);
    }

    /**
     * 获取一个成员变量
     * 方法会先尝试获取该成员本身的值
     * 否则会从该成员的类型中调用其无参构造尝试创建对象
     *
     * @param field 变量签名
     * @param defaultValue 若无法获取，使用的默认对象
     * @param object 变量所在对象，静态则为 null
     * @param setIfNull 当变量原值为 null 时，是否自动赋值
     * @param <T> 变量类型
     */
    public static <T> Optional<T> get(@Nonnull Field field, @Nullable Object object, @Nullable T defaultValue, boolean setIfNull, ElementContainer initializer) {
        T obj = null;
        field.setAccessible(true);
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        // 成员本身值
        try {
            if (isStatic) {
                obj = (T) field.get(null);
            } else if (object != null) {
                obj = (T) field.get(object);
            } else {
                initializer.modInfo.warn("Field {} is not static, but the object is null", field.getName());
            }
        } catch (IllegalAccessException e) {
            initializer.modInfo.warn("Cannot get field {}", field.getName());
        }

        if (obj == null) {
            // 尝试根绝类型获取值
            obj = (T) create(field.getType(), initializer).orElse(null);
            if (obj == null) {
                obj = defaultValue;
            }
            // 尝试赋值
            if (setIfNull && obj != null) {
                set(field, obj, null, initializer);
            }
        }

        return Optional.ofNullable(obj);
    }

    /**
     * 调用一个方法，获取其返回值
     * 方法会先尝试使用无参调用该方法
     * 否则会从该成员的类型中调用其无参构造尝试创建对象
     *
     * @param method 方法签名
     * @param <T> 方法类型
     */
    public static <T> Optional<T> invoke(@Nonnull Method method, @Nullable Object object, ElementContainer initializer) {
        T obj = null;
        method.setAccessible(true);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        // 成员本身值
        try {
            if (isStatic) {
                obj = (T) method.invoke(null);
            } else if (object != null) {
                obj = (T) method.invoke(object);
            } else {
                initializer.modInfo.warn("Method {} is not static, but the object is null", method.getName());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            initializer.modInfo.warn("Cannot invoke method {}", method.getName());
        }

        if (obj == null) {
            // 尝试根绝类型获取值
            obj = (T) create(method.getReturnType(), initializer).orElse(null);
        }
        return Optional.ofNullable(obj);
    }

    /**
     * 尝试为一个成员变量赋值
     *
     * @param field 要赋值的变量
     * @param value 要赋的值
     * @param object 所在对象。静态值可为 null
     */
    public static void set(@Nonnull Field field, @Nullable Object value, @Nullable Object object, ElementContainer initializer) {
        field.setAccessible(true);
        try {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                field.set(null, value);
            } else {
                if (object == null) {
                    initializer.modInfo.warn("Field {} is not state, but the object is null", field.getName());
                }
                field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            initializer.modInfo.warn("Field {} cannot set the value: \n\t{}\n Maybe it was a final field.", field.getName(), value);
        }
    }

    /**
     * 将注解数组转化为注解 Map，用于筛选指定注解
     *
     * @param handler 包含注解的 Class, AccessibleObject 或类实例 object
     * @param find 需要筛选出的注解
     * @return Map，以注解 Class 对象为键，注解实例为值
     */
    public static Map<Class<? extends Annotation>, Annotation> getAnnotations(@Nonnull AnnotatedElement handler, @Nullable Class<? extends Annotation>[] find, ElementContainer initializer) {
        Map<Class<? extends Annotation>, Annotation> annotationMap = new LinkedHashMap<>();
        if (handler instanceof AccessibleObject) {
            ((AccessibleObject) handler).setAccessible(true);
        }
        if (find != null && find.length > 0) {
            for (Class<? extends Annotation> aClass : find) {
                Annotation annotation = handler.getAnnotation(aClass);
                if (annotation != null) {
                    annotationMap.put(aClass, annotation);
                }
            }
        }
        return annotationMap;
    }

    /**
     * 获取类成员
     * 优先获取静态成员
     *
     * @param clazz 成员所在类
     * @param fieldName 成员名
     * @param object 类实例
     * @param <T> 成员类型
     * @return 尝试获取成员的结果
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getField(@Nonnull Class clazz, @Nonnull String fieldName, @Nullable Object object, ElementContainer initializer) {
        T field = null;
        try {
            // 静态成员
            Field holder = clazz.getDeclaredField(fieldName);
            holder.setAccessible(true);
            field = (T) get(holder, null, null, false, initializer).orElse(null);
            if (field == null) {
                // 实例成员
                holder = clazz.getField(fieldName);
                holder.setAccessible(true);
                if (object == null) {
                    // 尝试初始化实例
                    Constructor constructor = clazz.getConstructor();
                    constructor.setAccessible(true);
                    object = constructor.newInstance();
                }
                field = (T) get(holder, object, null, false, initializer);
            }
        } catch (InstantiationException
                | NoSuchFieldException
                | InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException e) {
            initializer.modInfo.warn("Cannot get field {} from {}", fieldName, clazz.getCanonicalName());
            e.printStackTrace();
        }
        return Optional.ofNullable(field);
    }
}
