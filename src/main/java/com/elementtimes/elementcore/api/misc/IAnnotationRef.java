package com.elementtimes.elementcore.api.misc;

import com.elementtimes.elementcore.api.misc.optional.*;
import com.elementtimes.elementcore.api.utils.ReflectUtils;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

public interface IAnnotationRef {

    <T> Optional<T> get(Object... parameters);

    default void invoke(Object... parameters) {};

    default OptionalByte getByte(Object... parameters) {
        return OptionalByte.empty();
    }

    default OptionalBoolean getBoolean(Object... parameters) {
        return OptionalBoolean.empty();
    }

    default OptionalChar getChar(Object... parameters) {
        return OptionalChar.empty();
    }

    default OptionalShort getShort(Object... parameters) {
        return OptionalShort.empty();
    }

    default OptionalInt getInt(Object... parameters) {
        return OptionalInt.empty();
    }

    default OptionalLong getLong(Object... parameters) {
        return OptionalLong.empty();
    }

    default OptionalFloat getFloat(Object... parameters) {
        return OptionalFloat.empty();
    }

    default OptionalDouble getDouble(Object... parameters) {
        return OptionalDouble.empty();
    }

    default boolean hasContent(Class<?> returnType) {
        if (hasContent()) {
            return returnType == null || ReflectUtils.isAssignableFrom(returnType, getReturnType());
        }
        return false;
    }

    default boolean isMethod() {
        return false;
    }

    default boolean isField() {
        return false;
    }

    default boolean isConstructor() {
        return false;
    }

    void setErrorHandler(Consumer<Throwable> errorHandler);

    boolean hasContent();

    boolean isPublic();

    boolean isStatic();

    void setObject(Object object);

    Class<?> getReturnType();

    Class<?>[] getParameterTypes();

    String getRefName();
}
