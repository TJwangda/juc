package com.test.juc.bq;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws InterruptedException {
//        test1();
//        test2();
//        test3();
        test4();
    }

    /**
     * 抛出异常
     */
    public static void test1(){
        //对列的大小
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<>(3);

        System.out.println(blockingQueue.add("a") );
        System.out.println(blockingQueue.add("b") );
        System.out.println(blockingQueue.add("c") );
        //继续添加报错  IllegalStateException: Queue full
        //System.out.println(blockingQueue.add("d") );
        System.out.println(blockingQueue.element());//查看队首元素
        System.out.println("==============");
        System.out.println(blockingQueue.remove());//remove无参，先进先出，弹出第一个进入的元素
        System.out.println(blockingQueue.remove());
        System.out.println(blockingQueue.remove());
        //空了以后继续取值，报错java.util.NoSuchElementException
        // System.out.println(blockingQueue.remove());
    }

    /**
     * 不抛异常，有返回值,不成功返回false
     */
    public static void test2(){
        //对列的大小
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<>(3);

        System.out.println(blockingQueue.offer("a") );
        System.out.println(blockingQueue.offer("b") );
        System.out.println(blockingQueue.offer("c") );
        //继续添加返回false
        System.out.println(blockingQueue.offer("d") );
        System.out.println(blockingQueue.peek());
        System.out.println(blockingQueue.element());
        System.out.println("==============");
        System.out.println(blockingQueue.poll());//poll无参，先进先出，弹出第一个进入的元素;有参数：延时等待
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        //空了以后继续取值返回null
         System.out.println(blockingQueue.poll());
    }

    /**
     * 等待，阻塞（一直阻塞）
     */
    public static void test3() throws InterruptedException {
        //对列的大小
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<>(3);
        blockingQueue.put("a");
        blockingQueue.put("b");
        blockingQueue.put("c");
//        blockingQueue.put("d");//队列没位置了，一直等待
        System.out.println("===========");
        System.out.println(blockingQueue.take());
//        blockingQueue.put("d");//去除一个后，正常执行
        System.out.println(blockingQueue.take());
        System.out.println(blockingQueue.take());
        System.out.println(blockingQueue.take());//没有元素了，一直等待，等到有数据能取
    }

    /**
     * 等待，阻塞（等待超时）
     */
    public static void test4() throws InterruptedException {
        //对列的大小
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<>(3);
        System.out.println(blockingQueue.offer("a") );
        System.out.println(blockingQueue.offer("b") );
        System.out.println(blockingQueue.offer("c") );
        //继续添加返回false
        System.out.println(blockingQueue.offer("d",2, TimeUnit.SECONDS) );//;有参数：延时等待
        System.out.println(blockingQueue.peek());
        System.out.println("==============");
        System.out.println(blockingQueue.poll());//poll无参，先进先出，弹出第一个进入的元素;有参数：延时等待
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        //空了以后继续取值返回null
        System.out.println(blockingQueue.poll(2,TimeUnit.SECONDS));
    }

}
