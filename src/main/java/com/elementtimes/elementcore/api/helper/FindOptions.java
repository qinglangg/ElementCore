package com.elementtimes.elementcore.api.helper;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.utils.CollectUtils;
import com.elementtimes.elementcore.api.utils.ReflectUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class FindOptions<T> {

    private Class<T> returnType;
    private Int2ObjectMap<List<Object[]>> parameters = new Int2ObjectArrayMap<>();
    private List<ImmutablePair<Class<?>[], Supplier<Object[]>>> parameterAndTypes = new ArrayList<>();
    private Object o;
    private ElementType[] types;
    private BiConsumer<Class<?>, T> afterCreate = (a, b) -> {}, afterGet = (a, b) -> {}, afterInvoke = (a, b) -> {};

    public boolean isFound = false;
    public boolean hasClass = false;
    public Class<?> aClass = null;
    public T result = null;
    public ElementType createType = null;

    public FindOptions(@Nullable Object getIn, @Nonnull Class<T> returnType, ElementType allowType, ElementType... otherAllowTypes) {
        this.returnType = returnType;
        this.o = getIn;
        this.types = ArrayUtils.add(otherAllowTypes, allowType);
    }

    public FindOptions(@Nonnull Class<T> returnType, ElementType allowType, ElementType... otherAllowTypes) {
        this(null, returnType, allowType, otherAllowTypes);
    }

    public FindOptions<T> addParametersAndTypes(Supplier<Object[]> parameters, Class<?>... types) {
        parameterAndTypes.add(ImmutablePair.of(types, parameters));
        return this;
    }

    public FindOptions<T> addParameters(Object... parameters) {
        CollectUtils.computeIfAbsent(this.parameters, parameters.length, ArrayList::new).add(parameters);
        return this;
    }

    public Optional<T> get(ECModElements elements, ModFileScanData.AnnotationData data) {
        ElementType targetType = data.getTargetType();
        if (!ArrayUtils.contains(types, targetType)) {
            return Optional.empty();
        }
        switch (targetType) {
            case TYPE: newInstance(elements, data); break;
            case CONSTRUCTOR: newInstanceConstructor(elements, data); break;
            case METHOD: invokeMethod(elements, data); break;
            case FIELD: getField(elements, data); break;
            default:
        }
        isFound = result != null && aClass != null;
        hasClass = aClass != null;
        createType = data.getTargetType();
        return Optional.ofNullable(result);
    }

    private void newInstance(ECModElements elements, ModFileScanData.AnnotationData data) {
        String className = data.getClassType().getClassName();
        aClass = ObjHelper.findClass(elements, className).orElse(null);
        if (checkType(aClass)) {
            Constructor<?>[] constructors = aClass.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                Object[] objects = findAcceptedParameters(constructor.getParameterTypes());
                if (objects != null) {
                    try {
                        if (!Modifier.isPublic(constructor.getModifiers())) {
                            constructor.setAccessible(true);
                        }
                        result = (T) constructor.newInstance(objects);
                        return;
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void newInstanceConstructor(ECModElements elements, ModFileScanData.AnnotationData data) {
        String className = data.getClassType().getClassName();
        Optional<Class<?>> classOpt = ObjHelper.findClass(elements, className);
        if (classOpt.isPresent()) {
            aClass = classOpt.get();
            if (checkType(aClass)) {
                Class<?>[] parameterTypes = Arrays.stream(data.getClassType().getArgumentTypes())
                        .map(type -> ObjHelper.findClass(elements, type).orElse(null))
                        .toArray(Class[]::new);
                try {
                    Constructor<?> constructor = aClass.getDeclaredConstructor(parameterTypes);
                    Object[] values = findAcceptedParameters(parameterTypes);
                    if (values != null) {
                        if (!Modifier.isPublic(constructor.getModifiers())) {
                            constructor.setAccessible(true);
                        }
                        result = (T) constructor.newInstance(values);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void invokeMethod(ECModElements elements, ModFileScanData.AnnotationData data) {
        String memberName = data.getMemberName();
        memberName = memberName.substring(memberName.indexOf(")") + 1);
        String className = data.getClassType().getClassName();
        aClass = ObjHelper.findClass(elements, className).orElse(null);
        if (aClass != null) {
            List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, aClass.getDeclaredMethods());
            Collections.addAll(methods, aClass.getMethods());
            for (Method method : methods) {
                if (memberName.equals(method.getName()) && checkType(method.getReturnType())) {
                    Object[] objects = findAcceptedParameters(method.getParameterTypes());
                    if (objects != null) {
                        try {
                            if (!Modifier.isPublic(method.getModifiers())) {
                                method.setAccessible(true);
                            }
                            result = (T) method.invoke(o, objects);
                        } catch (Exception ignored)  {}
                    }
                }
            }
        }
    }

    private void getField(ECModElements elements, ModFileScanData.AnnotationData data) {
        String memberName = data.getMemberName();
        String className = data.getClassType().getClassName();
        Optional<Class<?>> classOpt = ObjHelper.findClass(elements, className);
        if (classOpt.isPresent()) {
            aClass = classOpt.get();
            result = (T) ReflectUtils.findField(aClass, o, memberName).get().orElse(null);
        }
    }

    private boolean checkType(Class<?> aClass) {
        return ReflectUtils.isAssignableFrom(returnType, aClass);
    }

    private Object[] findAcceptedParameters(Class<?>[] types) {
        if (parameters.isEmpty() && parameterAndTypes.isEmpty()) {
            CollectUtils.computeIfAbsent(parameters, 0, ArrayList::new).add(new Object[0]);
        }
        for (ImmutablePair<Class<?>[], Supplier<Object[]>> pair : parameterAndTypes) {
            Class<?>[] parameterTypes = pair.left;
            if (Arrays.equals(types, parameterTypes)) {
                return pair.right.get();
            }
        }
        int length = types.length;
        List<Object[]> list = parameters.get(length);
        if (list != null && !list.isEmpty()) {
            for (Object[] objects : list) {
                if (typeAccept(length, objects, types)) {
                    return objects;
                }
            }
        }
        return null;
    }

    private boolean typeAccept(int length, Object[] parameters, Class<?>[] types) {
        for (int i = 0; i < length; i++) {
            if (!ReflectUtils.canAccept(types[i], parameters[i])) {
                return false;
            }
        }
        return true;
    }
}
