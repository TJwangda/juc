package com.test.juc.pool;

import java.util.concurrent.*;

/**
 * Executors工具类，七大参数、四种拒绝策略
 */
public class Demo02 {
    public static void main(String[] args) {
        //最大线程如何定义？
        //1、cpu密集型  几核cpu就定义为几。保持cpu效率最高
        //2、io密集型  判断程序中消耗io资源高的线程数，一般设置为其两倍
        System.out.println("cpu核数"+Runtime.getRuntime().availableProcessors());//获取运行是cpu的核数

        ExecutorService threadPool = new ThreadPoolExecutor(2,
                 5,
                 3,
                 TimeUnit.SECONDS,
                 new LinkedBlockingQueue<>(3),//阻塞队列超过三个时，触发最大容量
                 Executors.defaultThreadFactory(),
//                 new ThreadPoolExecutor.AbortPolicy());//默认拒绝策略。  最大容量满了以后再进来的任务不处理并报异常
//                 new ThreadPoolExecutor.CallerRunsPolicy());//哪来的去哪里。  最大容量满了以后再进来的任务退回去原线程处理
//                 new ThreadPoolExecutor.DiscardPolicy());//最大容量满了以后再进来的任务会丢掉不处理，不抛出异常
                 new ThreadPoolExecutor.DiscardOldestPolicy());//最大容量满了尝试和最早的竞争，如果最早线程任务处理结束了，可以执行，否则依然丢掉不报异常

        try {
            //最大承载：阻塞队列容量+max线程值
//            for(int i = 1;i <= 8;i++){
            for(int i = 1;i <= 9;i++){//抛出异常
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
