package com.test.juc.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executors工具类，三大方法
 */
public class Demo01 {
    public static void main(String[] args) {
         ExecutorService threadPool = Executors.newSingleThreadExecutor();//创建单个线程
        //ExecutorService threadPool = Executors.newFixedThreadPool(5);//创建固定大小的线程池
        //ExecutorService threadPool = Executors.newCachedThreadPool();//创建一个可伸缩的线程池，遇强则强，遇弱则弱

        try {
            for(int i = 1;i <= 10;i++){
    //            new Thread().start();弃用此种方式创建线程
                //用线程池创建线程
                threadPool.execute(()->{
                    System.out.println(Thread.currentThread().getName()+"==ok");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

    }
}
