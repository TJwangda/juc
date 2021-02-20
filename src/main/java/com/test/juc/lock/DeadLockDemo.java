package com.test.juc.lock;

import java.util.concurrent.TimeUnit;

/**
 * 死锁测试
 */
public class DeadLockDemo {
    public static void main(String[] args) {
        String lockA = "lockA";
        String lockB = "lockB";

        new Thread(new MyThread(lockA,lockB),"Ta").start();
        new Thread(new MyThread(lockB,lockA),"Tb").start();
    }
}
class MyThread implements Runnable{

    private String lockA;
    private String lockB;

    public MyThread(String lockA,String lockB){
        this.lockA = lockA;
        this.lockB = lockB;
    }

    @Override
    public void run() {
        synchronized (lockA){
            System.out.println(Thread.currentThread().getName()+" lock:"+lockA+"=>get "+lockB);

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lockB){
                System.out.println(Thread.currentThread().getName()+" lock:"+lockB+"=>get "+lockA);
            }

        }
    }
}