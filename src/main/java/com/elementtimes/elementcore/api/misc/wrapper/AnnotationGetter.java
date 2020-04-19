package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import com.elementtimes.elementcore.api.misc.optional.*;
import com.elementtimes.elementcore.api.utils.ReflectUtils;
import net.minecraft.util.StringUtils;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

/**
 * 对 Getter 及 Getter2 注解的封装
 * @see com.elementtimes.elementcore.api.annotation.part.Getter
 * @see com.elementtimes.elementcore.api.annotation.part.Getter2
 * @author luqin2007
 */
public class AnnotationGetter implements IAnnotationRef {

    public static AnnotationGetter EMPTY = new AnnotationGetter(null, "");

    private Class<?> mClass;
    private String mName;

    private boolean mIsField = false;
    private boolean mIsMethod = false;
    private boolean mIsConstructor = false;
    private boolean mHasContent = false;
    private IAnnotationRef mRef;

    private Consumer<Throwable> mErrorHandler = Throwable::printStackTrace;

    public AnnotationGetter(Class<?> aClass, String name) {
        mClass = aClass;
        mName = name;
        init();
    }

    private void init() {
        if (mClass != null && !StringUtils.isNullOrEmpty(mName)) {
            if ("<init>".equals(mName)) {
                mIsConstructor = true;
                mRef = ReflectUtils.findConstructor(mClass, null);
            } else if (mName.startsWith("()")){
                mIsMethod = true;
                mRef = ReflectUtils.findMethod(mClass, null, mName.substring(2));
            } else {
                mIsField = true;
                mRef = ReflectUtils.findField(mClass, null, mName);
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
    public void setObject(Object object) {
        if (hasContent()) {
            mRef.setObject(object);
        }
    }

    @Override
    public Class<?> getReturnType() {
        return mRef.getReturnType();
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return mRef.getParameterTypes();
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
    public boolean isField() {
        return mIsField;
    }

    @Override
    public boolean isConstructor() {
        return mIsConstructor;
    }

    @Override
    public void invoke(Object... parameters) {
        if (isMethod()) {
            mRef.invoke();
        }
    }

    @Override
    public <T> Optional<T> get(Object... parameters) {
        if (hasContent()) {
            return mRef.get();
        }
        return Optional.empty();
    }

    @Override
    public OptionalByte getByte(Object... parameters) {
        if (hasContent()) {
            return mRef.getByte();
        }
        return OptionalByte.empty();
    }

    @Override
    public OptionalDouble getDouble(Object... parameters) {
        if (hasContent()) {
            return mRef.getDouble();
        }
        return OptionalDouble.empty();
    }

    @Override
    public OptionalBoolean getBoolean(Object... parameters) {
        if (hasContent()) {
            return mRef.getBoolean();
        }
        return OptionalBoolean.empty();
    }

    @Override
    public OptionalChar getChar(Object... parameters) {
        if (hasContent()) {
            return mRef.getChar();
        }
        return OptionalChar.empty();
    }

    @Override
    public OptionalLong getLong(Object... parameters) {
        if (hasContent()) {
            return mRef.getLong();
        }
        return OptionalLong.empty();
    }

    @Override
    public OptionalFloat getFloat(Object... parameters) {
        if (hasContent()) {
            return mRef.getFloat();
        }
        return OptionalFloat.empty();
    }

    @Override
    public OptionalShort getShort(Object... parameters) {
        if (hasContent()) {
            return mRef.getShort();
        }
        return OptionalShort.empty();
    }

    @Override
    public OptionalInt getInt(Object... parameters) {
        if (hasContent()) {
            return mRef.getInt();
        }
        return OptionalInt.empty();
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
        return mClass.getName() + "#" + mName;
    }

    @Override
    public String toString() {
        return "[Getter]" + getRefName();
    }
}
