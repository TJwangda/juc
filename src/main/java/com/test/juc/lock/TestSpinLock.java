package com.test.juc.lock;

import java.util.concurrent.TimeUnit;

public class TestSpinLock {
    public static void main(String[] args) throws InterruptedException {

        //底层 自旋锁CAS
        SpinLockDeom spinLockDeom = new SpinLockDeom();

        new Thread(()->{
            spinLockDeom.myLock();
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                spinLockDeom.myUnLock();
            }
        },"t1").start();

        TimeUnit.SECONDS.sleep(1);

        new Thread(()->{
            spinLockDeom.myLock();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                spinLockDeom.myUnLock();
            }
        },"t2").start();


    }
}
