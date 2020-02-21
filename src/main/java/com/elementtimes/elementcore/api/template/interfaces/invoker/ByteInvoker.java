package com.elementtimes.elementcore.api.template.interfaces.invoker;

@FunctionalInterface
public interface ByteInvoker {

    byte invoke(Object... parameters);
}
