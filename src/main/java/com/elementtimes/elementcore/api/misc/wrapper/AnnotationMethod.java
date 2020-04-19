package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.misc.*;
import com.elementtimes.elementcore.api.misc.optional.*;
import com.elementtimes.elementcore.api.utils.ReflectUtils;
import net.minecraft.util.StringUtils;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

/**
 * @author luqin2007
 */
public class AnnotationMethod implements IAnnotationRef {

    public static AnnotationMethod EMPTY = new AnnotationMethod(null, null);

    private Class<?> mClass;
    private String mName;
    private Class<?>[] mParameters;

    private boolean mIsMethod = false;
    private boolean mHasContent = false;
    private IAnnotationRef mRef;

    private Consumer<Throwable> mErrorHandler = Throwable::printStackTrace;

    public AnnotationMethod(Class<?> aClass, String name, Class<?>... parameters) {
        mClass = aClass;
        mName = name;
        mParameters = parameters;
        init();
    }

    public AnnotationMethod(Class<?> aClass, String name, AnnotationGetter object, Class<?>... parameters) {
        this(aClass, name, parameters);
        setAnnotatedObject(object);
    }

    private void init() {
        if (mClass != null && !StringUtils.isNullOrEmpty(mName)) {
            if ("<init>".equals(mName)) {
                mRef = ReflectUtils.findConstructor(mClass, null, mParameters);
                mIsMethod = false;
            } else {
                mRef = ReflectUtils.findMethod(mClass, null, mName, mParameters);
                mIsMethod = true;
            }
            mHasContent = true;
            mRef.setErrorHandler(mErrorHandler);
        }
    }

    @Override
    public boolean hasContent() {
        return mHasContent && mRef.hasContent();
    }

    @Override
    public boolean isPublic() {
        return hasContent() && mRef.isPublic();
    }

    @Override
    public boolean isStatic() {
        return hasContent() && mRef.isStatic();
    }

    @Override
    public boolean hasContent(Class<?> returnType) {
        return mHasContent && mRef.hasContent(returnType);
    }

    @Override
    public boolean isMethod() {
        return mIsMethod;
    }

    @Override
    public boolean isConstructor() {
        return !mIsMethod;
    }

    @Override
    public void invoke(Object... parameters) {
        if (isMethod()) {
            mRef.invoke(parameters);
        }
    }

    @Override
    public <T> Optional<T> get(Object... parameters) {
        if (hasContent()) {
            return mRef.get(parameters);
        }
        return Optional.empty();
    }

    @Override
    public OptionalByte getByte(Object... parameters) {
        if (hasContent()) {
            return mRef.getByte(parameters);
        }
        return OptionalByte.empty();
    }

    @Override
    public OptionalBoolean getBoolean(Object... parameters) {
        if (hasContent()) {
            return mRef.getBoolean(parameters);
        }
        return OptionalBoolean.empty();
    }

    @Override
    public OptionalChar getChar(Object... parameters) {
        if (hasContent()) {
            return mRef.getChar(parameters);
        }
        return OptionalChar.empty();
    }

    @Override
    public OptionalShort getShort(Object... parameters) {
        if (hasContent()) {
            return mRef.getShort(parameters);
        }
        return OptionalShort.empty();
    }

    @Override
    public OptionalInt getInt(Object... parameters) {
        if (hasContent()) {
            return mRef.getInt(parameters);
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalLong getLong(Object... parameters) {
        if (hasContent()) {
            return mRef.getLong(parameters);
        }
        return OptionalLong.empty();
    }

    @Override
    public OptionalFloat getFloat(Object... parameters) {
        if (hasContent()) {
            return mRef.getFloat(parameters);
        }
        return OptionalFloat.empty();
    }

    @Override
    public OptionalDouble getDouble(Object... parameters) {
        if (hasContent()) {
            return mRef.getDouble(parameters);
        }
        return OptionalDouble.empty();
    }

    public void setAnnotatedObject(AnnotationGetter object) {
        if (hasContent()) {
            mRef.setObject(object.get().orElse(null));
        }
    }

    @Override
    public void setObject(Object object) {
        if (hasContent()) {
            mRef.setObject(object);
        }
    }

    @Override
    public Class<?> getReturnType() {
        return hasContent() ? mRef.getReturnType() : Void.class;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return hasContent() ? mRef.getParameterTypes() : new Class[0];
    }

    @Override
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        mErrorHandler = errorHandler;
        if (hasContent()) {
            mRef.setErrorHandler(mErrorHandler);
        }
    }

    @Override
    public String getRefName() {
        if (mClass == null || mName == null) {
            return "EMPTY";
        }
        StringBuilder desc = new StringBuilder().append(mClass.getName()).append("#").append(mName).append("(");
        for (Class<?> parameterType : getParameterTypes()) {
            desc.append(parameterType.getName()).append(";");
        }
        desc.append(")");
        return desc.toString();
    }

    @Override
    public String toString() {
        return "[Method]" + getRefName();
    }
}
