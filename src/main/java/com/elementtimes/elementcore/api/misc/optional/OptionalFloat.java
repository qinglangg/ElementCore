package com.elementtimes.elementcore.api.misc.optional;

import com.elementtimes.elementcore.api.misc.function.FloatSupplier;
import com.elementtimes.elementcore.api.utils.MathUtils;
import it.unimi.dsi.fastutil.floats.FloatConsumer;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class OptionalFloat {

    private static final OptionalFloat EMPTY = new OptionalFloat();

    private final boolean isPresent;
    private final float value;

    private OptionalFloat() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalFloat empty() {
        return EMPTY;
    }

    private OptionalFloat(float value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalFloat of(float value) {
        return new OptionalFloat(value);
    }

    public float getAsFloat() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void ifPresent(FloatConsumer consumer) {
        if (isPresent) {
            consumer.accept(value);
        }
    }

    public float orElse(float other) {
        return isPresent ? value : other;
    }

    public float orElseGet(FloatSupplier other) {
        return isPresent ? value : other.getAsFloat();
    }

    public<X extends Throwable> float orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalFloat)) {
            return false;
        }

        OptionalFloat other = (OptionalFloat) obj;
        return (isPresent && other.isPresent)
                ? MathUtils.equal(value, other.value)
                : isPresent == other.isPresent;
    }

    @Override
    public int hashCode() {
        return isPresent ? Float.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalFloat[%f]", value)
                : "OptionalFloat.empty";
    }
}
