package com.test.juc.callable;

import java.util.concurrent.Callable;

public class CallableTest {
    public static void main(String[] args) {
//        new Thread(new MyThread()).start();//传统方法

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