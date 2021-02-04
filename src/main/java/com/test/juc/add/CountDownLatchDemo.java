package com.test.juc.add;

import java.util.concurrent.CountDownLatch;

/**
 * 减法计数器
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        //总数是6
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for(int i = 1;i<=6;i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"走了");
                countDownLatch.countDown();//减1
            },String.valueOf(i)).start();
        }
        countDownLatch.await();//等待计数器归零再向下执行。没有这个等待，上边的不执行完就会执行下边的代码。
        System.out.println("没有了");
    }
}
