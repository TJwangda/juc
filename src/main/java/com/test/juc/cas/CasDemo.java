package com.test.juc.cas;

import java.util.concurrent.atomic.AtomicInteger;

public class CasDemo {

    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(2020);

        //（期望、修改）
        //public final boolean compareAndSet(int expect, int update
        //如果期望的值对了，就更新，否则不更新  CAS是cpu的并发原语
        System.out.println(atomicInteger.compareAndSet(2020, 2021));
        System.out.println(atomicInteger.get());
        atomicInteger.getAndIncrement();
        System.out.println(atomicInteger.compareAndSet(2020, 2022));
        System.out.println(atomicInteger);
    }
}
