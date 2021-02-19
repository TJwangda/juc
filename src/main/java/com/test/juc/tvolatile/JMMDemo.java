package com.test.juc.tvolatile;

import java.util.concurrent.TimeUnit;

/**
 * 保证可见性
 */
public class JMMDemo {
//    public static int num = 0;
    public volatile static int num = 0;
    public static void main(String[] args) throws InterruptedException {//main线程

        new Thread(()->{//线程1,不加volatile无法感知主内存的变化，会死循环；加了volatile保证可见性
            while (num == 0){
            }
        }).start();

        TimeUnit.SECONDS.sleep(1);//主线程睡眠一秒，线程1可以启动完成

        num = 1;
        System.out.println("num:"+num);
    }
}
