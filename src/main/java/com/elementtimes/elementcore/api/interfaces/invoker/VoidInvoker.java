package com.elementtimes.elementcore.api.interfaces.invoker;

@FunctionalInterface
public interface VoidInvoker {

    void invoke(Object... parameters);
}
