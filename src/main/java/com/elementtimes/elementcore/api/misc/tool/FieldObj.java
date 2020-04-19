package com.elementtimes.elementcore.api.misc.tool;

import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import com.elementtimes.elementcore.api.misc.optional.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

/**
 * @author luqin2007
 */
public class FieldObj implements IAnnotationRef {

    public static final FieldObj EMPTY = new FieldObj(null, null);

    private Field mField;
    private Object mObject;
    private Class<?> mReturnType;
    private boolean mStatic, mPublic;
    private Consumer<Throwable> mErrorHandler = e -> {};

    public FieldObj(Field field, Object object) {
        mField = field;
        if (mField == null) {
            mStatic = false;
            mPublic = false;
            mObject = null;
            mReturnType = null;
        } else {
            int modifiers = mField.getModifiers();
            mStatic = Modifier.isStatic(modifiers);
            mPublic = Modifier.isPublic(modifiers);
            mObject = mStatic ? null : object;
            mReturnType = mField.getType();

            if (!mPublic) {
                mField.setAccessible(true);
            }
        }
    }

    @Override
    public <T> Optional<T> get(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return Optional.of((T) mField.get(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public OptionalInt getInt(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalInt.of(mField.getInt(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalShort getShort(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalShort.of(mField.getShort(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalShort.empty();
    }

    @Override
    public OptionalFloat getFloat(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalFloat.of(mField.getFloat(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalFloat.empty();
    }

    @Override
    public OptionalLong getLong(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalLong.of(mField.getLong(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalLong.empty();
    }

    @Override
    public OptionalChar getChar(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalChar.of(mField.getChar(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalChar.empty();
    }

    @Override
    public OptionalBoolean getBoolean(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalBoolean.of(mField.getBoolean(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalBoolean.empty();
    }

    @Override
    public OptionalByte getByte(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalByte.of(mField.getByte(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalByte.empty();
    }

    @Override
    public OptionalDouble getDouble(Object... parameters) {
        if (mField != null) {
            try {
                checkStatic();
                return OptionalDouble.of(mField.getDouble(mObject));
            } catch (Throwable e) {
                mErrorHandler.accept(e);
            }
        }
        return OptionalDouble.empty();
    }

    public <T> Optional<T> setIfNull(@Nullable T defValue) {
        Optional<T> t = get();
        if (t.isPresent()) {
            return t;
        } else if (defValue != null) {
            set(defValue);
            return Optional.of(defValue);
        }
        return Optional.empty();
    }
    
    public void set(Object value) {
        if (mField == null) {
            return;
        }
        try {
            int modifiers = mField.getModifiers();
            boolean isFinal = Modifier.isFinal(modifiers);
            if (isFinal) {
                Field modifiersField = mField.getClass().getDeclaredField("modifiers");
                modifiersField.setInt(mField, modifiers & ~Modifier.FINAL);
                mField.set(mObject, value);
                modifiersField.setInt(mField, modifiers);
            } else {
                mField.set(mObject, value);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            mErrorHandler.accept(e);
        }
    }

    private void checkStatic() {
        if (!mStatic && mObject == null) {
            throw new RuntimeException("The field is not static but not have a object");
        }
    }
    
    @Override
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        mErrorHandler = errorHandler;
    }

    @Override
    public boolean hasContent() {
        return mField != null;
    }

    public Consumer<Throwable> getErrorHandler() {
        return mErrorHandler;
    }

    public Object getObject() {
        return mObject;
    }

    @Override
    public void setObject(Object object) {
        if (!mStatic && object != null) {
            mObject = object;
        } else if (mStatic) {
            mObject = null;
        }
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
        return new Class[0];
    }

    @Override
    public String getRefName() {
        return hasContent() ? mField.getName() : "EMPTY";
    }

    @Override
    public boolean isStatic() {
        return mStatic;
    }

    @Override
    public boolean isField() {
        return true;
    }
}
