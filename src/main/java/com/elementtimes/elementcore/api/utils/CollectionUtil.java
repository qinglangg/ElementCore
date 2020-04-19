package com.elementtimes.elementcore.api.utils;

import java.util.Map;
import java.util.function.Supplier;

public class CollectionUtil {

    private static CollectionUtil u = null;
    public static CollectionUtil getInstance() {
        if (u == null) {
            u = new CollectionUtil();
        }
        return u;
    }

    public <K, V> V computeIfAbsent(Map<K, V> map, K key, Supplier<V> newValue) {
        V v = map.get(key);
        if (v == null) {
            v = newValue.get();
            map.put(key, v);
        }
        return v;
    }
}
