[TOC]

# JUC

java操作并发的工具包![image-20210125142206545](E:\dev\picture\image-20210125142206545.png)

## 线程、进程

> java进程默认有两个线程：main线程和gc线程

+ java开启线程方式：Thread、runable、callable

+ java程序不能真正开启线程。

  ~~~java
  public synchronized void start() {
          /**
           * This method is not invoked for the main method thread or "system"
           * group threads created/set up by the VM. Any new functionality added
           * to this method in the future may have to also be added to the VM.
           *
           * A zero status value corresponds to state "NEW".
           */
          if (threadStatus != 0)
              throw new IllegalThreadStateException();
  
          /* Notify the group that this thread is about to be started
           * so that it can be added to the group's list of threads
           * and the group's unstarted count can be decremented. */
          group.add(this);
  
          boolean started = false;
          try {
              start0();
              started = true;
          } finally {
              try {
                  if (!started) {
                      group.threadStartFailed(this);
                  }
              } catch (Throwable ignore) {
                  /* do nothing. If start0 threw a Throwable then
                    it will be passed up the call stack */
              }
          }
      }
  //java调用本地方法，调用c++。java无法直接操作硬件
      private native void start0();
  ~~~

> 并发、并行

并发：多线程操作同一资源。单核cpu快速切换，形成同事执行的效果

并行：多核cpu，同事执行多条任务。

> 线程状态 6种

~~~java
public enum State {
        /**新生*/
        NEW,
        /**运行*/
        RUNNABLE,
        /**阻塞*/
        BLOCKED,
        /**等待*/
        WAITING,
        /**超时等待*/
        TIMED_WAITING,
        /**终止*/
        TERMINATED;
    }
~~~

> wait/sleep

1.wait-->Object类； sleep-->Thread类

2.wait释放锁，sleep不释放锁。

3.wait必须在同步代码块使用；sleep在哪都行。

## Lock锁

> Synchronized 本质就是队列



> Lock接口

~~~java
 Lock l = ...;
 l.lock();//加锁
 try {
   // access the resource protected by this lock
 } finally {
   l.unlock();//解锁
 }
~~~

所有已知实现类： ReentrantLock(可重入锁)、  ReentrantReadWriteLock.ReadLock（读锁） 、ReentrantReadWriteLock.WriteLock（写锁）

~~~java
	Lock lock = new ReentrantLock();
	//ReentrantLock构造函数，默认无参为非公平锁，true为公平锁
	public ReentrantLock() {
        sync = new NonfairSync();
    }
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
~~~

公平锁：先来后到。

非公平锁：可插队，后边的可以先执行。（默认）

> Synchronized和Lock的区别

1. synchronized是java内置关键字，lock是一个类
2. synchronized无法判断锁状态，lock可判断是否获取到了锁。
3. synchronized自动释放锁，lock需手动释放.
4. synchronized中一个线程获取了锁，其他线程只能等待；lock有trylock方法，等不到可以直接结束。
5. synchronized可重入，不可中断，非公平锁；lock可重入，可判断锁，可以设置是否公平锁。
6. synchronized适合锁少量代码同步问题；lock适合锁大量代码的同步问题。

> 锁是什么，怎么判断锁的是谁



## 生产者和消费者问题

> 生产者和消费者问题 Synchronized版

~~~java
/**
 * 线程之间的同步问题：生产者和消费者问题  等待唤醒、通知唤醒
 * 线程交替执行  A  B 两个操作同一个变量 num = 0
 * A num+1
 * B num-1
 */
public class A {
    public static void main(String[] args) {
        Data data = new Data();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.increment();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
    }

}

class Data{//数组 资源类
    private int number = 0;

    //+1
    public synchronized void increment() throws InterruptedException {
        if(number != 0){
            // 等待
            this.wait();
        }
        number++;
        System.out.println(Thread.currentThread().getName()+"=======>"+number);
        //通知其他线程加完了
        this.notifyAll();
    }

    //-1
    public synchronized void decrement() throws InterruptedException {
        if(number == 0){
            // 等待
            this.wait();
        }
        number--;
        System.out.println(Thread.currentThread().getName()+"=======>"+number);
        //通知其他线程减完了
        this.notifyAll();
    }
}
~~~

> 问题存在，A、B两个线程没问题，四个线程时还安全吗？ 否，存在虚假唤醒问题

虚假唤醒问题，把if改成while解决![image-20210125171627805](E:\dev\picture\image-20210125171627805.png)



> 生产者消费者问题  juc版

![image-20210125172803361](E:\dev\picture\image-20210125172803361.png)

~~~java
 class BoundedBuffer {
   final Lock lock = new ReentrantLock();
   final Condition notFull  = lock.newCondition(); //新建
   final Condition notEmpty = lock.newCondition(); 

   final Object[] items = new Object[100];
   int putptr, takeptr, count;

   public void put(Object x) throws InterruptedException {
     lock.lock(); try {
       while (count == items.length)
         notFull.await();
       items[putptr] = x;
       if (++putptr == items.length) putptr = 0;
       ++count;
       notEmpty.signal();//代替notifyall
     } finally { lock.unlock(); }
   }

   public Object take() throws InterruptedException {
     lock.lock(); try {
       while (count == 0)
         notEmpty.await();//代替wait
       Object x = items[takeptr];
       if (++takeptr == items.length) takeptr = 0;
       --count;
       notFull.signal();
       return x;
     } finally { lock.unlock(); }
   }
 } 
~~~

代码实现

~~~java
/**
 * 线程之间的同步问题：生产者和消费者问题  等待唤醒、通知唤醒
 * 线程交替执行  A  B 两个操作同一个变量 num = 0
 * A num+1
 * B num-1
 */
public class B {
    public static void main(String[] args) {
        Data2 data = new Data2();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.increment();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
    }

}

class Data2{//数组 资源类
    private int number = 0;

    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    //+1
    public void increment() throws InterruptedException {
        lock.lock();
        try {
            while (number != 0){
                // 等待
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName()+"=======>"+number);
            //通知其他线程加完了
            condition.signalAll();
        } finally {
            lock.unlock();
        }

    }

    //-1
    public void decrement() throws InterruptedException {
        lock.lock();
        try {
            while (number == 0){
                // 等待
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName()+"=======>"+number);
            //通知其他线程减完了
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }
}
~~~

> Conditon精准的通知和唤醒线程

~~~java
public class C {
    public static void main(String[] args) {
        Data3 data = new Data3();
        new Thread(()->{
            for(int i = 0 ;i<10;i++ ){
                data.printA();
            }
        },"A").start();

        new Thread(()->{
            for(int i = 0 ;i<10;i++ ){
                data.printB();
            }
        },"C").start();

        new Thread(()->{
            for(int i = 0 ;i<10;i++ ){
                data.printC();
            }
        },"C").start();
    }

}

class Data3{
    private Lock lock = new ReentrantLock();

    Condition condition1 = lock.newCondition();
    Condition condition2 = lock.newCondition();
    Condition condition3 = lock.newCondition();

    private int num =1 ;//1-A执行，2-B执行，3-C执行

    public void printA(){
        lock.lock();
        try {//业务：判断-》执行-》通知
            while (1 != num){
                //等待
                condition1.await();
            }
            System.out.println(Thread.currentThread().getName()+"===>aaaaaaaaaa");
            //唤醒指定线程B
            num = 2;
            condition2.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void printB(){
        lock.lock();
        try {//业务：判断-》执行-》通知
            while (2 !=num){
                condition2.await();
            }
            System.out.println(Thread.currentThread().getName()+"===>bbbbbbbbbb");
            //唤醒指定线程C
            num = 3;
            condition3.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void printC(){
        lock.lock();
        try {//业务：判断-》执行-》通知
            while (3 !=num){
                condition3.await();
            }
            System.out.println(Thread.currentThread().getName()+"===>cccccc");
            //唤醒指定线程A
            num = 1;
            condition1.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
~~~

## 8锁现象

> 什么是锁？如何判断锁的是谁？ 对象、class

~~~java
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 2、
 */
public class Test1 {
    public static void main(String[] args) {
        Phone phone = new Phone();
        new Thread(()->{
            phone.sendSms();
        },"A").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            phone.call();
        },"B").start();
    }

}

class Phone{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //两个方法调用的事同一个phone对象，谁先拿到谁执行。
    public synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    public synchronized void call(){
        System.out.println("打电话");
    }
}
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 3、增加一个普通方法，先打印哪个线程结果   hello先执行
 * 4、两个对象，两个同步方法，先打印哪个线程结果   打电话先执行，两个对象不互相影响
 */
public class Test2 {
    public static void main(String[] args) {
        Phone2 phone1 = new Phone2();
        Phone2 phone2 = new Phone2();

        new Thread(()->{
            phone1.sendSms();
        },"A").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            phone2.call();
        },"B").start();
    }

}

class Phone2{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //两个方法调用的事同一个phone对象，谁先拿到谁执行。
    public synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    public synchronized void call(){
        System.out.println("打电话");
    }

    //没有锁，不受同步影响
    public void hello(){
        System.out.println("hello==");
    }
}
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 3、增加一个普通方法，先打印哪个线程结果   hello先执行
 * 4、两个对象，两个同步方法，先打印哪个线程结果   打电话先执行，两个对象不互相影响
 * 5、增加两个静态同步方法,只有一个对象，先执行哪个   发短信先执行
 * 6、增加两个静态同步方法,两个对象，先执行哪个   发短信先执行
 */
public class Test3 {
    public static void main(String[] args) {
        Phone3 phone1  = new Phone3();
        Phone3 phone2  = new Phone3();

        new Thread(()->{
            phone1.sendSms();
        },"A").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            phone2.call();
        },"B").start();
    }

}

class Phone3{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //static 静态方法  类一加载就有了，锁class模板，phone3只有一个class对象
    public static synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    public static synchronized void call(){
        System.out.println("打电话");
    }


}
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 3、增加一个普通方法，先打印哪个线程结果   hello先执行
 * 4、两个对象，两个同步方法，先打印哪个线程结果   打电话先执行，两个对象不互相影响
 * 5、增加两个静态同步方法,只有一个对象，先执行哪个   发短信先执行
 * 6、增加两个静态同步方法,两个对象，先执行哪个   发短信先执行
 * 7、一个静态同步方法、一个普通同步方法、一个对象，先执行哪个   先执行打电话，静态同步方法锁class对象，普通同步方法锁的调用对象，不是一个锁，不影响。
 * 8、一个静态同步方法、一个普通同步方法、两个对象，先执行哪个    先执行打电话，不同锁对象，不影响
 */
public class Test4 {
    public static void main(String[] args) {
        Phone4 phone1  = new Phone4();
        Phone4 phone2  = new Phone4();

        new Thread(()->{
            phone1.sendSms();
        },"A").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            phone2.call();
        },"B").start();
    }

}

class Phone4{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //static 静态方法  类一加载就有了，锁class模板，phone4只有一个class对象
    public static synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    // 普通同步方法
    public synchronized void call(){
        System.out.println("打电话");
    }


}

~~~

## 集合类不安全

> list

~~~java
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListTest {
    public static void main(String[] args) {

//        List<String> list = new ArrayList<>();
//        List<String> list = new Vector<>();//方案1、Vector是安全的,不报错
//        List<String> list = Collections.synchronizedList(new ArrayList<>());//方案2、Collections工具类，把集合改成安全
        List<String> list = new CopyOnWriteArrayList<>();//方案3、juc

        //一个线程跑，线程安全
//        for(int i = 1;i<=10;i++){
//            list.add(UUID.randomUUID().toString().substring(0,5));
//            System.out.println(list);
//        }

//        10个线程跑  报错java.util.ConcurrentModificationException，并发修改异常
        //解决方案：方案1、ArrayList换成Vector，Vector是安全的
        //方案2、Collections工具类，把集合改成安全
        //方案3、juc
        //CopyOnWrite 写入时复制， cow 计算机程序设计领域的一种优化策略
        //多个线程调用的时候，list，读取是固定的，写入会覆盖

        for(int i = 1;i<=10;i++){
            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,5));
                System.out.println(list);
            },String.valueOf(i)).start();

        }
    }
}
~~~

> set

~~~java

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * .ConcurrentModificationException
 */
public class SetTest {
    public static void main(String[] args) {
//        Set<String> set = new HashSet<>();
//        Set<String> set = Collections.synchronizedSet(new HashSet<>());//方案一
        Set<String> set = new CopyOnWriteArraySet<>();//方案二

        for(int i = 1;i<=30;i++){
            new Thread(()->{
                set.add(UUID.randomUUID().toString().substring(0,5));
                System.out.println(set);
            },String.valueOf(i)).start();
        }
    }
}
~~~

HashSet的本质：

~~~java
 public HashSet() {
        map = new HashMap<>();
    }
//set的add方法本质：借用了map的key
public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
    private static final Object PRESENT = new Object(); //固定值
~~~

> Map

![image-20210201162206820](E:\dev\picture\image-20210201162206820.png)

~~~java
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapTest {
    public static void main(String[] args) {
        //map平常怎么用，
        // 默认等价于什么 new HashMap<>() ==》new HashMap<>(16,0.75)
//        Map<String, String> map = new HashMap<>();
        Map<String, String> map = new ConcurrentHashMap<>();//方案一
        // 加载因子，初始化容量  稍后查一下

        for(int i = 1 ;i <= 30 ;i++){
            new Thread(()->{
                map.put(Thread.currentThread().getName(), UUID.randomUUID().toString().substring(0,5));
                System.out.println(map);
            },String.valueOf(i)).start();
        }
    }
}
~~~



## Callable(简单)

~~~java
@FunctionalInterface
public interface Callable<V>
    
返回结果并可能引发异常的任务。 实现者定义一个没有参数的单一方法，称为call 。 
Callable接口类似于Runnable ，因为它们都是为其实例可能由另一个线程执行的类设计的。 然而，A Runnable不返回结果，也不能抛出被检查的异常。 
该Executors类包含的实用方法，从其他普通形式转换为Callable类。
1、可以有返回值 2、可以抛出异常 3、方法不同 run()/call()
~~~

~~~java
代码
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
~~~

![image-20210204163812200](E:\dev\picture\image-20210204163812200.png)

![image-20210204163726618](E:\dev\picture\image-20210204163726618.png)

![image-20210204164025421](E:\dev\picture\image-20210204164025421.png)

细节：结果有缓存、结果可能需要等待会阻塞

## 常用的辅助类

### CountDownLatch

![image-20210204165812959](E:\dev\picture\image-20210204165812959.png)

~~~java
import java.util.concurrent.CountDownLatch;

/**
 * 减法计数器
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        //总数是6
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for(int i = 1;i<=6;i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"走了");
                countDownLatch.countDown();//减1
            },String.valueOf(i)).start();
        }
        countDownLatch.await();//等待计数器归零再向下执行。没有这个等待，上边的不执行完就会执行下边的代码。
        System.out.println("没有了");
    }
}
~~~

原理：==countDownLatch.countDown()==减1

​           ==countDownLatch.await()==等待计数器归零再向下执行

​			每次有线程调用countDown，数量减1，假设数量变成0，countDownLatch.await会被唤醒，继续执行。

### CyclicBarrier

![image-20210204171657025](E:\dev\picture\image-20210204171657025.png)

~~~java
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
~~~

###  Semaphore

![image-20210204175201310](E:\dev\picture\image-20210204175201310.png)

~~~java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreDemo {
    public static void main(String[] args) {
//       线程数量： 停车位、限流
        Semaphore semaphore = new Semaphore(3);
        for(int i = 1;i<=6;i++){
            new Thread(()->{
                //acquire 得到
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"抢到车位");
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println(Thread.currentThread().getName()+"离开车位");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    //release 释放
                    semaphore.release();
                }

            },String.valueOf(i)).start();
        }
    }
}
~~~

原理：==semaphore.acquire：获取==,假如已经满了，就等到释放为止

​			==semaphore.release：释放==，会将当前信号量释放，唤醒等待线程。

作用：多个共享资源互斥的使用！并发限流，控制最大线程数。

## 读写锁

![image-20210207101609175](E:\dev\picture\image-20210207101609175.png)

~~~java
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 独占锁（写锁） 一次只能被一个线程持有
 * 共享锁（读锁） 多个线程可以同事占有
 * readwriteLock
 * 读-读 可以共存
 * 读-写  不可以共存
 * 写-写  不可以共存
 */
public class ReadWriteLockDemo {
    public static void main(String[] args) {
//        MyCache myCache = new MyCache();
        MyCacheLock myCache = new MyCacheLock();
        //写入
        for(int i = 1;i<= 5;i++){
            final int temp = i;
            new Thread(()->{
                myCache.put(temp+"",temp+"");
            },String.valueOf(i)).start();
        }
//        获取
        for(int i = 1;i<= 5;i++){
            final int temp = i;
            new Thread(()->{
                myCache.get(temp+"");
            },String.valueOf(i)).start();
        }

    }
}

/**
 * 无锁，多天线程插入影响逻辑。
 * 自定义缓存，set/get
 *
 */
class MyCache{
    private volatile Map<String,Object> map= new HashMap();
    //存
    public void put(String key,Object value){
        System.out.println(Thread.currentThread().getName()+"写入"+key);
        map.put(key,value);
        System.out.println(Thread.currentThread().getName()+"写完ok"+key);
    }
    //取
    public void get(String key){
        System.out.println(Thread.currentThread().getName()+"读取"+key);
        Object o = map.get(key);
        System.out.println(Thread.currentThread().getName()+"读取ok"+key);
//        return o;
    }
}

/**
 * 自定义缓存-有锁，set/get
 *
 */
class MyCacheLock{
    private volatile Map<String,Object> map= new HashMap();
    //读写锁，更加细粒度控制
    private ReadWriteLock readWriteLock =  new ReentrantReadWriteLock();
//    private Lock lock = new ReentrantLock();
    //存，写入时只希望一个时间只有一个线程写入
    public void put(String key,Object value){
//        lock.lock();
        readWriteLock.writeLock().lock();//加写锁
        try {
            System.out.println(Thread.currentThread().getName()+"写入"+key);
            map.put(key,value);
            System.out.println(Thread.currentThread().getName()+"写完ok"+key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();//解写锁
        }
    }
    //取，读可以所有人读取
    public void get(String key){
        readWriteLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+"读取"+key);
            Object o = map.get(key);
            System.out.println(Thread.currentThread().getName()+"读取ok"+key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
//        return o;
    }
}
~~~

## 阻塞队列

![image-20210207125735082](E:\dev\picture\image-20210207125735082.png)

![image-20210207125957271](E:\dev\picture\image-20210207125957271.png)

![image-20210207141424819](E:\dev\picture\image-20210207141424819.png)

什么情况使用阻塞队列？多线程并发处理、线程池

**使用队列**   添加、移除

**四组api**

| 使用方式       | 会抛出异常 | 不抛异常，有返回值 | 阻塞等待,死等 | 超时等待                    |
| -------------- | ---------- | ------------------ | ------------- | --------------------------- |
| 添加           | add        | offer()            | put           | offer("obj",time, TimeUnit) |
| 移除           | remove     | poll()             | take          | poll(time, TimeUnit)        |
| 检测队列首元素 | element()  | peek               |               |                             |

~~~java
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
~~~

> ## SynchronousQueue<E> 同步队列

没有容量，进入一个元素，只有取出来以后才能放入下一个元素。

put、take

~~~java
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
~~~

## 线程池（重点）

> 池化技术 ：事先准备好资源，有人要用就来取，用完归还

线程池：三大方法、七大参数、四种拒绝策略

线程池的好处：==**线程服用、最大并发数可控、管理线程**==

> 三大方法

![image-20210209110555498](E:\dev\picture\image-20210209110555498.png)

~~~java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executors工具类，三大方法
 */
public class Demo01 {
    public static void main(String[] args) {
        //ExecutorService threadPool = Executors.newSingleThreadExecutor();//创建单个线程
        //ExecutorService threadPool = Executors.newFixedThreadPool(5);//创建固定大小的线程池
       ExecutorService threadPool = Executors.newCachedThreadPool();//创建一个可伸缩的线程池，遇强则强，遇弱则弱

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

~~~

> 七大参数

源码分析

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}

public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}

public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}

//ThreadPoolExecutor本质：
public ThreadPoolExecutor(int corePoolSize,//核心线程池大小
                              int maximumPoolSize,//最大核心线程池大小
                              long keepAliveTime,//核心线程池之外的最大线程池中线程，超时没人调用会释放
                              TimeUnit unit,//超时单位
                              BlockingQueue<Runnable> workQueue,//阻塞队列
                              ThreadFactory threadFactory,//线程工厂，创建线程用，一般不用动
                              RejectedExecutionHandler handler//拒绝策略
                         ) {
    if (corePoolSize < 0 ||
        maximumPoolSize <= 0 ||
        maximumPoolSize < corePoolSize ||
        keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.acc = System.getSecurityManager() == null ?
        null :
    AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}
```

![image-20210209164538192](E:\dev\picture\image-20210209164538192.png)

![image-20210209164956822](E:\dev\picture\image-20210209164956822.png)

> 手动创建线程池

~~~java
/**
 * Executors工具类，三大方法
 */
public class Demo02 {
    public static void main(String[] args) {
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
~~~



> 四种拒绝策略

![image-20210216105902967](E:\dev\picture\image-20210216105902967.png)

```java
//new ThreadPoolExecutor.AbortPolicy());//默认拒绝策略。  最大容量满了以后再进来的任务不处理并报异常
//new ThreadPoolExecutor.CallerRunsPolicy());//哪来的去哪里。  最大容量满了以后再进来的任务退回去原线程处理
//new ThreadPoolExecutor.DiscardPolicy());//最大容量满了以后再进来的任务会丢掉不处理，不抛出异常
//new ThreadPoolExecutor.DiscardOldestPolicy());//最大容量满了尝试和最早的竞争，如果最早线程任务处理结束了，可以执行，否则依然丢掉不报异常
```

> 小结

```java
最大线程如何定义？用于调优
1、cpu密集型  几核cpu就定义为几。保持cpu效率最高
2、io密集型  判断程序中消耗io资源高的线程数，一般设置为其两倍
    
    ExecutorService threadPool = new ThreadPoolExecutor(2,
                 Runtime.getRuntime().availableProcessors(),//cpu密集型获取运行时cpu的核数
                 3,
                 TimeUnit.SECONDS,
                 new LinkedBlockingQueue<>(3),//阻塞队列超过三个时，触发最大容量
                 Executors.defaultThreadFactory(),
                 new ThreadPoolExecutor.AbortPolicy());//默认拒绝策略。  最大容量满了以后再进来的任务不处理并报异常             
```



## 四大函数式接口（必须掌握）

lambda表达式、链式编程、函数式接口、Stream流式计算

> 函数式接口：只有一个方法的接口

```java
@FunctionalInterface
public interface Runnable { 
    public abstract void run();
}
//超级多@FunctionalInterface标签
//简化编程模型，在新版本框架底层中大量应用
//forEach(Consumer<? super T> action) 参数为消费类型的函数式接口
```

<img src="E:\dev\picture\image-20210216114542128.png" alt="image-20210216114542128" style="zoom: 80%;" />.

代码测试：

> Function函数式接口

![image-20210216120006674](E:\dev\picture\image-20210216120006674.png).

~~~java
import java.util.function.Function;

/**
 * Function 函数型接口 有一个参数，一个输出
 * 只要是函数式接口，就可以用lambda表达式简化
 */
public class Demo01 {
    public static void main(String[] args) {
//        Function<String,String> fun = new Function<String,String>(){
//
//            @Override
//            public String apply(String o) {
//                return o;
//            }
//        };
        Function<String,String> fun = (str)->{ return str; };
        System.out.println(fun.apply("asdf"));
    }
}
~~~

>  Predicate 断定型接口

![image-20210216120317799](E:\dev\picture\image-20210216120317799.png)

~~~java
import java.util.function.Predicate;

/**
 * Predicate 有一个入参，返回boolean值
 */
public class Demo02 {
    public static void main(String[] args) {
        //判断方法
//        Predicate<String> predicate = new Predicate<String>(){
//
//            @Override
//            public boolean test(String str) {
//                return str.isEmpty();
//            }
//        };
        Predicate<String> predicate = (str)->{ return str.isEmpty(); };
        System.out.println(predicate.test("we"));
    }
}
~~~



>   Consumer 消费型接口

![image-20210216121454703](E:\dev\picture\image-20210216121454703.png)

~~~java
/**
 * Consumer 有入参，无返回值
 */
public class Demo03 {
    public static void main(String[] args) {
//        Consumer<String> consumer = new Consumer<String>(){
//            @Override
//            public void accept(String str) {
//                System.out.println(str);
//            }
//        };
        Consumer<String> consumer = (str)->{ System.out.println(str); };
        consumer.accept("adfsdf");
    }
}
~~~



>   Supplier 供给型接口  没有参数，只有返回值

![image-20210216121941119](E:\dev\picture\image-20210216121941119.png).

~~~java
import java.util.function.Supplier;

/**
 * Supplier 供给型接口 没有参数，只有返回值
 */
public class Demo04 {
    public static void main(String[] args) {
//        Supplier<Integer> supplier = new Supplier<Integer>(){
//
//            @Override
//            public Integer get() {
//                System.out.println("get'''");
//                return 1024;
//            }
//        };
        Supplier<Integer> supplier = ()->{ System.out.println("get");return 1024;};
        System.out.println(supplier.get());
    }
}
~~~



## Stream流式计算

> 什么是Stream流式计算？

![image-20210216151558883](E:\dev\picture\image-20210216151558883.png)

~~~java
import java.util.Arrays;
import java.util.List;

/**
 * 题目要求：一分钟内完成，一行代码实现
 * 筛选要求：1、id必须是偶数
 * 2、年龄必须大于23
 * 3、用户名转为大写
 * 4、按用户名倒序排列
 * 5、只要第一个用户
 */
public class Test {
    public static void main(String[] args) {
        User u1 = new User(1,"a",21);
        User u2 = new User(2,"b",22);
        User u3= new User(3,"c",23);
        User u4 = new User(4,"d",24);
        User u5 = new User(6,"e",25);
        //集合就是存储
        List<User> list = Arrays.asList(u1, u2, u3, u4, u5);
        //计算交给Stream流
        list.stream()
                .filter((u)->{return u.getId()%2 == 0;})
                .filter(u->{return u.getAge()>23;})
                .map(u->{return u.getName().toUpperCase();})
                .sorted((uu1,uu2)-> {return uu2.compareTo(uu1);})
                .limit(1)
                .forEach(System.out::println);
    }
}
~~~



## ForkJoin

> 

jdk1.7以后出现。并行执行任务提高效率，大数据量前提下

![image-20210216153824971](E:\dev\picture\image-20210216153824971.png)

> ForkJoin特点：工作窃取

这个里边维护的都是双端队列，当B线程执行完自己的任务时，可以窃取A线程未完成的任务，从而提高效率。

![image-20210216154040158](E:\dev\picture\image-20210216154040158.png).

> ForkJoin操作

![image-20210216170413347](E:\dev\picture\image-20210216170413347.png)

![image-20210216170803111](E:\dev\picture\image-20210216170803111.png).

~~~java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.LongStream;

public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        test1();//6387
//        test2();//6001
        test3();//176
    }

    public static void test1(){
        long start = System.currentTimeMillis();
        Long sum = 0L;
        for(Long i = 1L; i <= 10_0000_0000; i++){
            sum += i;
        }
        System.out.println(sum);
        long end = System.currentTimeMillis();
        System.out.println("sum:"+sum+"时间："+(end-start));
    }

    //forkjoin
    public static void test2() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinDemo task = new ForkJoinDemo(0l, 10_0000_0000l);
        ForkJoinTask<Long> submit = forkJoinPool.submit(task);
        Long sum = submit.get();

        long end = System.currentTimeMillis();
        System.out.println("sum:"+sum+"时间："+(end-start));
    }

    //stream并行流
    public static void test3(){
        long start = System.currentTimeMillis();
        long sum = LongStream.rangeClosed(0l,10_0000_0000L).parallel().reduce(0,Long::sum);
        long end = System.currentTimeMillis();
        System.out.println("sum:"+sum+"时间："+(end-start));
    }
}

import java.util.concurrent.RecursiveTask;

/**
 * 求和计算
 * 如何使用forkjoin
 * 1、forkjoinPool 通过它执行
 * 2、计算任务 forkjoinpool.execute(ForkJoinTask task)
 * 3、计算类继承ForkJoinTask
 */
public class ForkJoinDemo extends RecursiveTask<Long> {
    private Long start;
    private Long end;


    private long temp=10000l;//临界值
    public ForkJoinDemo(Long start,Long end){
        this.start = start;
        this.end = end;
    }



    //计算方法
    @Override
    protected Long compute() {
        if((end-start)<temp){//差值大于临界值用forkjoin合并计算
            Long sum = 0L;
            for(Long i = start; i <= end; i++){
                sum += i;
            }
//            System.out.println(sum);
            return sum;
        }else {//forkjoin
            long middle = (start+end)/2;
            ForkJoinDemo task1 = new ForkJoinDemo(start, middle);
            task1.fork();//拆分任务，把任务压入线程队列
            ForkJoinDemo task2 = new ForkJoinDemo(middle+1, end);
            task2.fork();//拆分任务，把任务压入线程队列
            //合并结果
            return task1.join() + task2.join();
        }
    }
}

~~~



## 异步回调

> Future 设计的初衷，对将来的某个事件的结果进行建模

![image-20210219100403025](E:\dev\picture\image-20210219100403025.png)。

~~~java
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
~~~

