package com.test.juc.tvolatile;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 不保证原子性
 */
public class JMMDemo2 {
//    public volatile static int num = 0;
    //原子类
    public volatile static AtomicInteger num = new AtomicInteger();
    public static void add(){
//        num++;//不是原子性操作
        num.getAndIncrement();//AtomicInteger+1方法，底层CAS实现
    }
    public static void main(String[] args) throws InterruptedException {//main线程
        //正常结果为20000
        for (int i = 1; i <= 20; i++) {
            new Thread(()->{
                for (int j = 0; j < 1000; j++) {
                    add();
                }
            }).start();
        }

        while (Thread.activeCount()>2){//main、gc
            Thread.yield();//线程礼让
        }

        System.out.println(Thread.currentThread().getName()+"："+num);
    }
}
