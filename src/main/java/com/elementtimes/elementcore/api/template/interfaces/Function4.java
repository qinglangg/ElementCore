package com.elementtimes.elementcore.api.template.interfaces;

/**
 * 有五个输入的方法
 * 一个函数接口罢了
 *
 * @author luqin2007
 */
@FunctionalInterface
public interface Function4<T1, T2, T3, T4, R> {

    /**
     * 一个函数接口罢了
     * @param v1 param1
     * @param v2 param2
     * @param v3 param3
     * @param v4 param4
     * @return return
     */
    R apply(T1 v1, T2 v2, T3 v3, T4 v4);
}
