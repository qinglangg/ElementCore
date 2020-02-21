package com.elementtimes.elementcore.api.template.interfaces.invoker;

@FunctionalInterface
public interface BooleanInvoker {

    boolean invoke(Object... parameters);
}
