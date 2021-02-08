package com.test.juc.bq;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * 同步队列demo
 * 和其他的blockingqueue不同，SynchronousQueue不存储元素
 * put一个元素后，必须取出来，否则无法继续存。
 */
public class SynchronousQueueDemo {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue = new SynchronousQueue<String>();//同步队列

        new Thread(()->{
            try {
                System.out.println(Thread.currentThread().getName()+"put 1");
                blockingQueue.put("1");
                System.out.println(Thread.currentThread().getName()+"put 2");
                blockingQueue.put("2");
                System.out.println(Thread.currentThread().getName()+"put 3");
                blockingQueue.put("3");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"T1").start();

        new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
                System.out.println(Thread.currentThread().getName()+"取出"+blockingQueue.take());
                TimeUnit.SECONDS.sleep(3);
                System.out.println(Thread.currentThread().getName()+"取出"+blockingQueue.take());
                TimeUnit.SECONDS.sleep(3);
                System.out.println(Thread.currentThread().getName()+"取出"+blockingQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"T2").start();

    }
}
