package com.elementtimes.elementcore.api.template.interfaces.invoker;

@FunctionalInterface
public interface Invoker<T> {
    Invoker<Object> NULL = p -> null;

    T invoke(Object... parameters);

    static <T> Invoker<T> empty() {
        return (Invoker<T>) NULL;
    }
}
