package com.elementtimes.elementcore.api.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectUtils {

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Supplier<V> newValue) {
        V v = map.get(key);
        if (v == null) {
            v = newValue.get();
            map.put(key, v);
        }
        return v;
    }

    public static IntList rangeList(int fromInclusive, int toExclusive, int step) {
        IntList list = new IntArrayList();
        if ((fromInclusive > toExclusive && step > 0) || (fromInclusive < toExclusive && step < 0)) {
            return list;
        }
        int i = fromInclusive;
        do {
            list.add(i);
            i += step;
        } while (step > 0 ? i < toExclusive : i > toExclusive);
        return list;
    }

    public static IntList rangeList(int fromInclusive, int toExclusive) {
        return rangeList(fromInclusive, toExclusive, fromInclusive < toExclusive ? 1 : -1);
    }

    public static int[] rangeArr(int fromInclusive, int toExclusive, int step) {
        return rangeList(fromInclusive, toExclusive, step).toIntArray();
    }

    public static int[] rangeArr(int fromInclusive, int toExclusive) {
        return rangeList(fromInclusive, toExclusive).toIntArray();
    }

    public static <T extends INBT> Collector<T, ?, ListNBT> toNbtList() {
        return new Collector<T, Object, ListNBT>() {
            @Override
            public Supplier<Object> supplier() {
                return ListNBT::new;
            }

            @Override
            public BiConsumer<Object, T> accumulator() {
                return (list, nbt) -> ((ListNBT) list).add(nbt);
            }

            @Override
            public BinaryOperator<Object> combiner() {
                return (left, right) -> {
                    ((ListNBT) left).addAll(((ListNBT) right));
                    return left;
                };
            }

            @Override
            public Function<Object, ListNBT> finisher() {
                return o -> (ListNBT) o;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
            }
        };
    }
}
