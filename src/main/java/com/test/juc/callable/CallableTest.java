package com.test.juc.callable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        new Thread(new Runnable()).start();//传统方法
//                       ||
//        new Thread(new FutureTask<V>()).start();//传统方法

//        new Thread(new FutureTask<V>(Callable)).start();//调用callable
//        new Thread().start();//callable怎么启动

        MyThread myThread = new MyThread();
        FutureTask futureTask = new FutureTask(myThread);
        new Thread(futureTask,"A").start();
        new Thread(futureTask,"B").start();//只会打印一次call，结果缓存，效率高

        String o = (String) futureTask.get();//获取callable返回结果，get()可能会阻塞，放到最后或者异步通信处理
        System.out.println("==="+o);
    }
}

//class MyThread implements Runnable{
class MyThread implements Callable<String> {

//    @Override
//    public void run() {
//    }
    @Override
    public String call() throws Exception {
        System.out.println("call方法===");
        return "123";
    }

}