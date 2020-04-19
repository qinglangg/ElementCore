package com.elementtimes.elementcore.api.misc.optional;

import com.elementtimes.elementcore.api.misc.function.CharSupplier;
import it.unimi.dsi.fastutil.chars.CharConsumer;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @author luqin2007
 */
public class OptionalChar {

    private static final OptionalChar EMPTY = new OptionalChar();

    private final boolean isPresent;
    private final char value;

    private OptionalChar() {
        this.isPresent = false;
        this.value = '\u0000';
    }

    public static OptionalChar empty() {
        return EMPTY;
    }

    private OptionalChar(char value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalChar of(char value) {
        return new OptionalChar(value);
    }

    public char getAsChar() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void ifPresent(CharConsumer consumer) {
        if (isPresent) {
            consumer.accept(value);
        }
    }

    public char orElse(char other) {
        return isPresent ? value : other;
    }

    public char orElseGet(CharSupplier other) {
        return isPresent ? value : other.getAsChar();
    }

    public<X extends Throwable> char orElseThrow(Supplier<X> exceptionSupplier) throws X {
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

        if (!(obj instanceof OptionalChar)) {
            return false;
        }

        OptionalChar other = (OptionalChar) obj;
        return (isPresent && other.isPresent)
                ? value == other.value
                : isPresent == other.isPresent;
    }

    @Override
    public int hashCode() {
        return isPresent ? Character.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalChar[%c]", value)
                : "OptionalChar.empty";
    }
}
