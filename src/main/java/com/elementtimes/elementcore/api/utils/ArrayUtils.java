package com.elementtimes.elementcore.api.utils;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 数组有关
 * @author luqin2007
 */
public class ArrayUtils {

    private static ArrayUtils utils = null;
    public static ArrayUtils getInstance() {
        if (utils == null) {
            utils = new ArrayUtils();
        }
        return utils;
    }

    public <T> T[] newArray(Class<T> type, int size, @Nullable T fill) {
        T[] array = (T[]) Array.newInstance(type, size);
        if (fill != null) {
            Arrays.fill(array, fill);
        }
        return array;
    }

    public int[] newArray(int size, int fill) {
        int[] array = new int[size];
        Arrays.fill(array, fill);
        return array;
    }

    public long[] newArray(int size, long fill) {
        long[] array = new long[size];
        Arrays.fill(array, fill);
        return array;
    }

    public float[] newArray(int size, float fill) {
        float[] array = new float[size];
        Arrays.fill(array, fill);
        return array;
    }

    public double[] newArray(int size, double fill) {
        double[] array = new double[size];
        Arrays.fill(array, fill);
        return array;
    }

    public char[] newArray(int size, char fill) {
        char[] array = new char[size];
        Arrays.fill(array, fill);
        return array;
    }

    public byte[] newArray(int size, byte fill) {
        byte[] array = new byte[size];
        Arrays.fill(array, fill);
        return array;
    }

    public boolean[] newArray(int size, boolean fill) {
        boolean[] array = new boolean[size];
        Arrays.fill(array, fill);
        return array;
    }
}
