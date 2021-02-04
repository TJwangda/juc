package com.test.juc.add;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 加法计数器
 */
public class CyclicBarrierDemo {
    public static void main(String[] args) {
        //集齐七颗龙珠召唤神龙
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7,()->{
            System.out.println("召唤神龙");
        });

        for(int i = 1;i<=7;i++){
            final int temp = i;
            //lambda操作不到i
            new Thread(()->{
//                System.out.println("==="+i);无法直接获取i，因为lambda是另一个类
                System.out.println(Thread.currentThread().getName()+"收集==="+temp+"个龙珠");//可以通过final获取到
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }
}
