package com.test.juc.cas;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ABA问题
 */
public class CasDemo2 {

    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(2020);

        //（期望、修改）
        //public final boolean compareAndSet(int expect, int update
        //如果期望的值对了，就更新，否则不更新  CAS是cpu的并发原语
//        捣乱的线程
        System.out.println(atomicInteger.compareAndSet(2020, 2021));
        System.out.println(atomicInteger.get());

        System.out.println(atomicInteger.compareAndSet(2021, 2020));
        System.out.println(atomicInteger.get());
//        期望的线程
        System.out.println(atomicInteger.compareAndSet(2020, 6699));
        System.out.println(atomicInteger);
    }
}
