package com.elementtimes.elementcore.api.interfaces.invoker;

@FunctionalInterface
public interface BooleanInvoker {

    boolean invoke(Object... parameters);
}
