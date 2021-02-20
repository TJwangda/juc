package com.test.juc.cas;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 原子引用解决ABA问题
 */
public class CasDemo3 {

    public static void main(String[] args) {
//        AtomicInteger atomicInteger = new AtomicInteger(2020);
        //注意AtomicStampedReference泛型如果是包装类，要注意引用问题，是否是同一个对象。
        AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(1,1);

       new Thread(()->{
           int stamp = atomicStampedReference.getStamp();
           System.out.println("a1++++=="+stamp);

           try {
               TimeUnit.SECONDS.sleep(1);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

           System.out.println(atomicStampedReference.compareAndSet(1, 2, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1));

           System.out.println("a2=>"+atomicStampedReference.getStamp());
//           System.out.println("a2=>"+stamp  );

           System.out.println(atomicStampedReference.compareAndSet(2, 1, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1));

           System.out.println("a3=>"+atomicStampedReference.getStamp());
       },"a").start();

        new Thread(()->{
            int stamp = atomicStampedReference.getStamp();
            System.out.println("b1++++=="+stamp);

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(atomicStampedReference.compareAndSet(1, 6, stamp, stamp + 1));
            System.out.println("b2=>"+atomicStampedReference.getStamp());
        },"b").start();
    }
}
