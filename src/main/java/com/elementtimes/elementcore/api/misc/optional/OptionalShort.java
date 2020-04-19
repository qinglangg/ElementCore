package com.elementtimes.elementcore.api.misc.optional;

import com.elementtimes.elementcore.api.misc.function.ShortSupplier;
import com.elementtimes.elementcore.api.utils.MathUtils;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class OptionalShort {

    private static final OptionalShort EMPTY = new OptionalShort();

    private final boolean isPresent;
    private final short value;

    private OptionalShort() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalShort empty() {
        return EMPTY;
    }

    private OptionalShort(short value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalShort of(short value) {
        return new OptionalShort(value);
    }

    public short getAsShort() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void ifPresent(ShortConsumer consumer) {
        if (isPresent) {
            consumer.accept(value);
        }
    }

    public short orElse(short other) {
        return isPresent ? value : other;
    }

    public short orElseGet(ShortSupplier other) {
        return isPresent ? value : other.getAsShort();
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

        if (!(obj instanceof OptionalShort)) {
            return false;
        }

        OptionalShort other = (OptionalShort) obj;
        return (isPresent && other.isPresent)
                ? MathUtils.equal(value, other.value)
                : isPresent == other.isPresent;
    }

    @Override
    public int hashCode() {
        return isPresent ? Short.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalShort[%c]", value)
                : "OptionalShort.empty";
    }
}
