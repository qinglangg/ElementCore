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
@SuppressWarnings({"unchecked", "unused", "WeakerAccess", "SimplifyOptionalCallChains"})
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
            opt = create((Class) holder, type, logger);
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
    public <T> Optional<T> create(@Nonnull Class aClass, Class<? extends T> type, Logger logger) {
        try {
            if (type.isAssignableFrom(aClass)) {
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
                Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(className);
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
    public <T> Optional<T> create(@Nonnull Class aClass, Object[] params, Class<? extends T> type, Logger logger) {
        Class[] paramClass = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);
        Optional<Constructor> constructorOpt = Arrays.stream(aClass.getConstructors())
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
                .findFirst();
        if (constructorOpt.isPresent()) {
            return create(constructorOpt.get(), type, logger);
        }
        return Optional.empty();
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
                Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(className);
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
    public <T> Optional<T> create(@Nonnull Constructor constructor, Class<? extends T> type, Logger logger) {
        if (logger == null) {
            logger = LogManager.getLogger();
        }
        setAccessible(constructor);
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
     * @param <T> 变量类型
     */
    public <T> Optional<T> get(@Nonnull Field field, @Nullable Object object, @Nullable T defaultValue, boolean setIfNull, Class<? extends T> type, Logger logger) {
        T obj = null;
        setAccessible(field);
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
     * 调用一个方法，获取其返回值
     * 方法会先尝试使用无参调用该方法
     * 否则会从该成员的类型中调用其无参构造尝试创建对象
     *
     * @param method 方法签名
     * @param <T> 方法类型
     */
    public <T> Optional<T> invoke(@Nonnull Method method, @Nullable Object object, Class<? extends T> type, Logger logger) {
        Object obj = null;
        setAccessible(method);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        // 成员本身值
        try {
            if (isStatic) {
                obj = method.invoke(null);
            } else if (object != null) {
                obj = method.invoke(object);
            } else {
                logger.warn("Method {} is not static, but the object is null", method.getName());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            logger.warn("Cannot invoke method {}", method.getName());
        }

        if (obj == null || !type.isAssignableFrom(obj.getClass())) {
            return create(method.getReturnType(), type, logger);
        } else {
            return Optional.of((T) obj);
        }
    }

    /**
     * 调用一个方法，获取其返回值
     * 方法会先尝试使用无参调用该方法
     * 否则会从该成员的类型中调用其无参构造尝试创建对象
     *
     * @param method 方法签名
     */
    public void invoke(@Nonnull Method method, @Nullable Object object, Logger logger) {
        setAccessible(method);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        // 成员本身值
        try {
            if (isStatic) {
                method.invoke(null);
            } else if (object != null) {
                method.invoke(object);
            } else {
                logger.warn("Method {} is not static, but the object is null", method.getName());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            logger.warn("Cannot invoke method {}", method.getName());
        }
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
                setAccessible(fieldModifiers);
                fieldModifiers.setInt(field, modifiers & ~Modifier.FINAL);
            }
            // private/public
            setAccessible(field);
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
    public <T> Optional<T> getField(@Nonnull Class clazz, @Nonnull String fieldName, @Nullable Object object, Class<? extends T> type, Logger logger) {
        try {
            // 静态成员
            Field holder = clazz.getDeclaredField(fieldName);
            if (holder != null) {
                return get(holder, object, null, false, type, logger);
            } else {
                if (object == null) {
                    // 尝试初始化实例
                    Constructor constructor = clazz.getConstructor();
                    setAccessible(constructor);
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
     * 获取类成员
     * 优先获取静态成员
     *
     * @param clazz 成员所在类
     * @param object 类实例
     * @param <T> 成员类型
     * @return 尝试获取成员的结果
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getField(@Nonnull Class clazz, Class<? extends T> type, @Nullable Object object, Logger logger) {
        try {
            for (Field field : clazz.getFields()) {
                if (type.isAssignableFrom(field.getType())) {
                    setAccessible(field);
                    T obj = (T) field.get(object);
                    return Optional.ofNullable(obj);
                }
            }
            for (Field field : clazz.getDeclaredFields()) {
                if (type.isAssignableFrom(field.getType())) {
                    setAccessible(field);
                    T obj = (T) field.get(object);
                    return Optional.ofNullable(obj);
                }
            }
            return Optional.empty();
        } catch (IllegalAccessException e) {
            logger.warn("Cannot get type {} from {}", type.getSimpleName(), clazz.getCanonicalName());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Object> getFromFieldOrMethod(Class clazz, String name) {
        Object obj = null;
        try {
            obj = clazz.getField(name).get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) { }
        if (obj == null) {
            try {
                Field f = clazz.getDeclaredField(name);
                setAccessible(f);
                obj = f.get(null);
            } catch (IllegalAccessException | NoSuchFieldException ignored) { }
        }
        if (obj == null) {
            try {
                obj = clazz.getMethod(name).invoke(null);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) { }
        }
        if (obj == null) {
            try {
                Method method = clazz.getDeclaredMethod(name);
                setAccessible(method);
                obj = method.invoke(null);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) { }
        }
        if (obj == null || name.contains("()")) {
            String funcName = name.substring(0, name.indexOf("()"));
            if (obj == null) {
                try {
                    obj = clazz.getMethod(funcName).invoke(null);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) { }
            }
            if (obj == null) {
                try {
                    Method method = clazz.getDeclaredMethod(funcName);
                    setAccessible(method);
                    obj = method.invoke(null);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) { }
            }
        }
        return Optional.ofNullable(obj);
    }

    public void setFinalField(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        setFinalField(null, field, newValue);
    }

    public void setFinalField(Object object, Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        final Field modifiersField = field.getClass().getDeclaredField("modifiers");
        setAccessible(modifiersField);
        final int modifiers = field.getModifiers();
        modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
        field.set(object, newValue);
        modifiersField.setInt(field, modifiers);
    }

    public boolean checkMethodTypeAndParameters(Method method, Class returnType, Class... parameterTypes) {
        if (returnType.isAssignableFrom(method.getReturnType())) {
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == parameterTypes.length) {
                for (int i = 0; i < parameters.length; i++) {
                    if (!parameterTypes[i].isAssignableFrom(parameters[i])) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void setAccessible(AccessibleObject obj) {
        if (!obj.isAccessible()) {
            obj.setAccessible(true);
        }
    }
}
