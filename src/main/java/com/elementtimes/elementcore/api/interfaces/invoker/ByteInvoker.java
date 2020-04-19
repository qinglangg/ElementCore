package com.elementtimes.elementcore.api.interfaces.invoker;

@FunctionalInterface
public interface ByteInvoker {

    byte invoke(Object... parameters);
}
