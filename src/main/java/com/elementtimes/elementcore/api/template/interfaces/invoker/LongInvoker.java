package com.elementtimes.elementcore.api.template.interfaces.invoker;

@FunctionalInterface
public interface LongInvoker {

    long invoke(Object... parameters);
}
