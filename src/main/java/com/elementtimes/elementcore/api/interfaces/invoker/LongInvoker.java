package com.elementtimes.elementcore.api.interfaces.invoker;

@FunctionalInterface
public interface LongInvoker {

    long invoke(Object... parameters);
}
