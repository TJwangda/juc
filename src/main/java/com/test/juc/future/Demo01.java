package com.test.juc.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 异步调用CompletableFuture，相当于页面的Ajax
 */
public class Demo01 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //无返回值示例，发起一个请求
//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(()->{
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println(Thread.currentThread().getName()+"runAsync=>void无返回结果");
//        });
//
//        System.out.println("1111");
//        completableFuture.get();//阻塞获取执行结果,先打印1111

        //有返回值示例  supplyAsync  异步回调
        //成功
        //失败
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(()->{
            System.out.println(Thread.currentThread().getName()+"supplyAsync=>Integer");
            int i = 10/0;
            return 1024;
        });
        //成功
        System.out.println(
            completableFuture.whenComplete((t, u) -> {
            System.out.println("t-->" + t);//第一个参数返回正常执行时的结果
            System.out.println("u-->" + u);//执行出现异常时打印错误信息
            //失败
            }).exceptionally((e) -> {
                System.out.println(e.getMessage());
                return 233;
            }).get()
        );

    }
}
