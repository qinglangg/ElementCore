package com.elementtimes.elementcore.api.misc.tool;

import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import com.elementtimes.elementcore.api.misc.optional.*;
import com.elementtimes.elementcore.api.utils.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

/**
 * @author luqin2007
 */
public class MethodObj implements IAnnotationRef {

    public static final MethodObj EMPTY = new MethodObj(null, null);

    private Method mMethod;
    private Object mObject;
    private Class<?>[] mParameters;
    private Class<?> mReturnType;
    private Consumer<Throwable> mErrorHandler = e -> {};
    private boolean mStatic, mPublic;

    public MethodObj(Method method, Object object) {
        mMethod = method;
        if (mMethod == null) {
            mStatic = false;
            mPublic = false;
            mObject = null;
            mParameters = new Class[0];
            mReturnType = Void.class;
        } else {
            int modifiers = method.getModifiers();
            mStatic = Modifier.isStatic(modifiers);
            mPublic = Modifier.isPublic(modifiers);
            mObject = isStatic() ? null : object;
            mParameters = method.getParameterTypes();
            mReturnType = mMethod.getReturnType();

            if (!mPublic) {
                mMethod.setAccessible(true);
            }
        }
    }

    @Override
    public boolean hasContent() {
        return mMethod != null;
    }

    @Override
    public <T> Optional<T> get(Object... parameters) {
        try {
            return Optional.ofNullable((T) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return Optional.empty();
    }

    @Override
    public void invoke(Object... parameters) {
        try {
            getValue(parameters);
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
    }

    @Override
    public OptionalInt getInt(Object... parameters) {
        try {
            return OptionalInt.of((int) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalFloat getFloat(Object... parameters) {
        try {
            return OptionalFloat.of((float) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalFloat.empty();
    }

    @Override
    public OptionalDouble getDouble(Object... parameters) {
        try {
            return OptionalDouble.of((double) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalDouble.empty();
    }

    @Override
    public OptionalBoolean getBoolean(Object... parameters) {
        try {
            return OptionalBoolean.of((boolean) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalBoolean.empty();
    }

    @Override
    public OptionalLong getLong(Object... parameters) {
        try {
            return OptionalLong.of((long) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalLong.empty();
    }

    @Override
    public OptionalByte getByte(Object... parameters) {
        try {
            return OptionalByte.of((byte) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalByte.empty();
    }

    @Override
    public OptionalChar getChar(Object... parameters) {
        try {
            return OptionalChar.of((char) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalChar.empty();
    }

    @Override
    public OptionalShort getShort(Object... parameters) {
        try {
            return OptionalShort.of((short) getValue(parameters));
        } catch (Throwable e) {
            mErrorHandler.accept(e);
        }
        return OptionalShort.empty();
    }

    private Object getValue(Object... parameters) throws InvocationTargetException, IllegalAccessException {
        if (mMethod != null) {
            if (parameters.length != mParameters.length) {
                throw new RuntimeException("You provide " + parameters.length + " parameters, but the method need " + mParameters.length + " parameters.");
            }
            for (int i = 0; i < parameters.length; i++) {
                if (!ReflectUtils.canAccept(mParameters[i], parameters[i])) {
                    throw new RuntimeException("The parameter at " + i + "need a " + mParameters[i].getName() + ", you provide a " + parameters[i]);
                }
            }
            return mMethod.invoke(mObject, parameters);
        } else {
            throw new RuntimeException("Can't find a method");
        }
    }

    @Override
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        mErrorHandler = errorHandler;
    }

    public Consumer<Throwable> getErrorHandler() {
        return mErrorHandler;
    }

    @Override
    public boolean isStatic() {
        return mStatic;
    }

    @Override
    public boolean isPublic() {
        return mPublic;
    }

    @Override
    public Class<?> getReturnType() {
        return mReturnType;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return mParameters;
    }

    @Override
    public String getRefName() {
        return hasContent() ? mMethod.getName() : "EMPTY";
    }

    @Override
    public void setObject(Object object) {
        if (!isStatic() && object != null) {
            mObject = object;
        } else if (isStatic()) {
            mObject = null;
        }
    }

    @Override
    public boolean isMethod() {
        return true;
    }
}
