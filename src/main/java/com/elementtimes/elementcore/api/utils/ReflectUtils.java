package com.elementtimes.elementcore.api.utils;

import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * 处理反射有关的方法
 *
 * @author luqin2007
 */
@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
public class ReflectUtils {

    private static ReflectUtils u = null;
    public static ReflectUtils getInstance() {
        if (u == null) {
            u = new ReflectUtils();
        }
        return u;
    }

    /**
     * 获得可被注解条目的值
     * 只能获取静态值
     */
    public <T> Optional<T> getFromAnnotated(@Nonnull AnnotatedElement holder, @Nullable T defaultValue, Class<? extends T> type, Logger logger) {
        if (holder instanceof AccessibleObject) {
            ((AccessibleObject) holder).setAccessible(true);
        }

        Optional<T> opt = Optional.empty();
        if (holder instanceof Class) {
            opt = create((Class<?>) holder, type, logger);
        } else if (holder instanceof Field) {
            opt = get((Field) holder, null, defaultValue, true, type, logger);
        } else if (holder instanceof Constructor) {
            opt = create((Constructor<T>) holder, type, logger);
        } else if (holder instanceof Method) {
            opt = invoke((Method) holder, null, type, logger);
        }
        return Optional.ofNullable(opt.orElse(defaultValue));
    }

    /**
     * 使用无参构造创建对象
     *
     * @param aClass 要创建的对象类
     */
    public <T> Optional<T> create(@Nonnull Class<?> aClass, @Nullable Class<? extends T> type, Logger logger) {
        try {
            if (type == aClass || type == null || type.isAssignableFrom(aClass)) {
                Object object = aClass.newInstance();
                return Optional.ofNullable((T) object);
            }
        } catch (IllegalAccessException | InstantiationException e) {
            logger.warn("Cannot create an instance of {}. Please make sure the class has a public constructor with zero parameter.", aClass.getSimpleName());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * 使用有参构造创建对象
     *
     * @param className 要创建的对象类的全类名
     */
    public <T> Optional<T> create(@Nonnull String className, Object[] params, Class<? extends T> type, Logger logger) {
        if (className.isEmpty()) {
            logger.warn("You want to find an EMPTY class.");
        } else {
            try {
                Class<?> aClass = Class.forName(className);
                return create(aClass, params, type, logger);
            } catch (ClassNotFoundException e) {
                logger.warn("Class {} is not exist. Please make sure the class is exist and the ClassLoader can reload the class", className);
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    /**
     * 使用有参构造创建对象
     * @param aClass 要创建的对象类
     */
    public <T> Optional<T> create(@Nonnull Class<?> aClass, Object[] params, Class<? extends T> type, Logger logger) {
        Optional<Constructor<?>> constructorOpt = Arrays.stream(aClass.getConstructors())
                .filter(c -> c.getParameterCount() == params.length)
                .filter(c -> {
                    Class<?>[] parameterTypes = c.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        Object o = params[i];
                        if (o != null && !parameterTypes[i].isInstance(o)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst();
        if (constructorOpt.isPresent()) {
            Constructor<?> constructor = constructorOpt.get();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            try {
                T instance = (T) constructor.newInstance(params);
                return Optional.of(instance);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            return create(aClass, type, logger);
        }
    }

    /**
     * 使用无参构造创建对象
     *
     * @param className 要创建的对象类的全类名
     */
    public <T> Optional<T> create(@Nonnull String className, Class<? extends T> type, Logger logger) {
        if (logger == null) {
            logger = LogManager.getLogger();
        }
        if (TextUtils.isBlank(className)) {
            logger.warn("You want to find an EMPTY class.");
        } else {
            try {
                Class<?> aClass = Class.forName(className);
                return create(aClass, type, logger);
            } catch (ClassNotFoundException e) {
                logger.warn("Class {} is not exist. Please make sure the class is exist and the ClassLoader can reload the class", className);
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    /**
     * 使用无参构造创建对象
     *
     * @param constructor 要创建的对象的构造函数
     */
    public <T> Optional<T> create(@Nonnull Constructor<?> constructor, Class<? extends T> type, Logger logger) {
        if (logger == null) {
            logger = LogManager.getLogger();
        }
        constructor.setAccessible(true);
        try {
            Object o = constructor.newInstance();
            if (type.isAssignableFrom(o.getClass())) {
                return Optional.of((T) o);
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            logger.warn("Constructor {} can not invoke", constructor.getName());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * 获取一个成员变量
     *
     * @param field 变量签名
     * @param defaultValue 若无法获取，使用的默认对象
     * @param object 变量所在对象，静态则为 null
     * @param setIfNull 当变量原值为 null 时，是否自动赋值
     * @param type 检查类型
     * @param <T> 变量类型
     */
    public <T> Optional<T> get(@Nonnull Field field, @Nullable Object object, @Nullable T defaultValue, boolean setIfNull, @Nonnull Class<? extends T> type, Logger logger) {
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
                logger.warn("Field {} is not static, but the object is null", field.getName());
            }
        } catch (IllegalAccessException e) {
            logger.warn("Cannot get field {}", field.getName());
        }

        if (obj == null || !type.isAssignableFrom(obj.getClass())) {
            // 尝试根绝类型获取值
            obj = create(field.getType(), type, logger).orElse(null);
            if (obj == null) {
                obj = defaultValue;
            }
            // 尝试赋值
            if (setIfNull && obj != null) {
                set(field, obj, null, logger);
            }
        }

        return Optional.ofNullable(obj);
    }

    /**
     * 获取类成员
     * 优先获取静态成员
     * @param clazz 成员所在类
     * @param fieldName 成员名
     * @param object 类实例
     * @param <T> 成员类型
     * @return 尝试获取成员的结果
     */
    public <T> Optional<T> get(@Nonnull Class<?> clazz, @Nonnull String fieldName, @Nullable Object object, @Nonnull Class<? extends T> type, Logger logger) {
        try {
            // 静态成员
            Field holder = clazz.getDeclaredField(fieldName);
            if (holder != null) {
                return get(holder, object, null, false, type, logger);
            } else {
                if (object == null) {
                    // 尝试初始化实例
                    Constructor<?> constructor = clazz.getConstructor();
                    constructor.setAccessible(true);
                    object = constructor.newInstance();
                }
                return get(clazz.getField(fieldName), object, null, false, type, logger);
            }
        } catch (InstantiationException
                | NoSuchFieldException
                | InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException e) {
            logger.warn("Cannot get field {} from {}", fieldName, clazz.getCanonicalName());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * 调用一个方法，获取其返回值
     * @param method 方法签名
     * @param object 所在对象
     * @param type 返回值类型
     * @param <T> 方法类型
     */
    public <T> Optional<T> invoke(@Nonnull Method method, @Nullable Object object, @Nullable Class<? extends T> type, Logger logger, Object... args) {
        method.setAccessible(true);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        try {
            Object obj;
            if (isStatic) {
                obj = method.invoke(null, args);
            } else if (object != null) {
                obj = method.invoke(object, args);
            } else {
                obj = null;
                logger.warn("Method {} is not static, but the object is null", method.getName());
            }
            if (obj != null && type != null && !type.isInstance(obj)) {
                obj = null;
            }
            return (Optional<T>) Optional.ofNullable(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            logger.warn("Cannot invoke method {}", method.getName());
            return Optional.empty();
        }
    }

    /**
     * 调用一个无参方法，获取其返回值
     * @param methodName 方法名
     * @param holder 方法所在类
     * @param object 所在对象
     * @param type 返回值类型
     * @param <T> 方法类型
     */
    public <T> Optional<T> invoke(@Nonnull String methodName, @Nonnull Class<?> holder, @Nullable Object object, @Nullable Class<? extends T> type, Logger logger) {
        for (Method declaredMethod : holder.getDeclaredMethods()) {
            if (declaredMethod.getParameterCount() == 0 && methodName.equals(declaredMethod.getName())) {
                return invoke(declaredMethod, object, type, logger);
            }
        }
        for (Method method : holder.getMethods()) {
            if (method.getParameterCount() == 0 && methodName.equals(method.getName())) {
                return invoke(method, object, type, logger);
            }
        }
        return Optional.empty();
    }

    /**
     * 调用一个方法，获取其返回值
     * @param methodName 方法名
     * @param holder 方法所在类
     * @param object 所在对象
     * @param type 返回值类型
     * @param args 参数
     * @param argTypes 参数类型
     * @param <T> 方法类型
     */
    public <T> Optional<T> invoke(@Nonnull String methodName, @Nonnull Class<?> holder, @Nullable Object object, @Nullable Class<? extends T> type, Logger logger, Object[] args, Class<?>... argTypes) {
        for (Method declaredMethod : holder.getDeclaredMethods()) {
            if (methodName.equals(declaredMethod.getName())) {
                if (Arrays.equals(argTypes, declaredMethod.getParameterTypes())) {
                    return invoke(declaredMethod, object, type, logger, args);
                }
            }
        }
        for (Method method : holder.getMethods()) {
            if (methodName.equals(method.getName())) {
                if (Arrays.equals(argTypes, method.getParameterTypes())) {
                    return invoke(method, object, type, logger, args);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 尝试为一个成员变量赋值
     *
     * @param field 要赋值的变量
     * @param value 要赋的值
     * @param object 所在对象。静态值可为 null
     */
    public void set(@Nonnull Field field, @Nullable Object value, @Nullable Object object, Logger logger) {
        try {
            int modifiers = field.getModifiers();
            Field fieldModifiers = null;
            // final
            if (Modifier.isFinal(modifiers)) {
                fieldModifiers = field.getClass().getDeclaredField("modifiers");
                fieldModifiers.setAccessible(true);
                fieldModifiers.setInt(field, modifiers & ~Modifier.FINAL);
            }
            // private/public
            if (!Modifier.isPublic(modifiers)) {
                field.setAccessible(true);
            }
            if (Modifier.isStatic(modifiers)) {
                field.set(null, value);
            } else {
                if (object == null) {
                    logger.warn("Field {} is not state, but the object is null", field.getName());
                }
                field.set(object, value);
            }
            // final
            if (fieldModifiers != null) {
                fieldModifiers.setInt(field, modifiers);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            logger.warn("Field {} cannot set the value: \n\t{}.", field.getName(), value);
        }
    }

    public void setFinalField(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        setFinalField(null, field, newValue);
    }

    public void setFinalField(Object object, Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        final Field modifiersField = field.getClass().getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        final int modifiers = field.getModifiers();
        modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
        field.set(object, newValue);
        modifiersField.setInt(field, modifiers);
    }
}
