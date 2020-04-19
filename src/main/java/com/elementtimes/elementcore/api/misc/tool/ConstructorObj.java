package com.elementtimes.elementcore.api.misc.tool;

import com.elementtimes.elementcore.api.misc.IAnnotationRef;
import com.elementtimes.elementcore.api.utils.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author luqin2007
 */
public class ConstructorObj implements IAnnotationRef {

    public static final ConstructorObj EMPTY = new ConstructorObj(null);

    private Constructor<?> mConstructor;
    private Class<?>[] mParameters;
    private Class<?> mReturnType;
    private Consumer<Throwable> mErrorHandler = e -> {};
    private boolean mPublic;

    public ConstructorObj(Constructor<?> constructor) {
        mConstructor = constructor;
        if (mConstructor == null) {
            mPublic = false;
            mParameters = new Class[0];
            mReturnType = null;
        } else {
            mPublic = Modifier.isPublic(mConstructor.getModifiers());
            if (!mPublic) {
                mConstructor.setAccessible(true);
            }
            mParameters = constructor.getParameterTypes();
            mReturnType = mConstructor.getDeclaringClass();
        }
    }

    @Override
    public boolean hasContent() {
        return mConstructor != null;
    }

    @Override
    public <T> Optional<T> get(Object... parameters) {
        if (hasContent()) {
            try {
                if (parameters.length != mParameters.length) {
                    throw new RuntimeException("You provide " + parameters.length + " parameters, but the constructor need " + mParameters.length + " parameters.");
                }
                for (int i = 0; i < parameters.length; i++) {
                    if (!ReflectUtils.canAccept(mParameters[i], parameters[i])) {
                        throw new RuntimeException("The parameter at " + i + "need a " + mParameters[i].getName() + ", you provide a " + parameters[i]);
                    }
                }
                return Optional.of((T) mConstructor.newInstance(parameters));
            } catch (Exception e) {
                mErrorHandler.accept(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        mErrorHandler = errorHandler;
    }

    public Consumer<Throwable> getErrorHandler() {
        return mErrorHandler;
    }

    @Override
    public boolean isPublic() {
        return mPublic;
    }

    @Override
    public boolean isStatic() {
        return true;
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
        return hasContent() ? mConstructor.getDeclaringClass().getSimpleName() : "EMPTY";
    }

    @Override
    public boolean isConstructor() {
        return true;
    }

    @Override
    public void setObject(Object object) { }
}
