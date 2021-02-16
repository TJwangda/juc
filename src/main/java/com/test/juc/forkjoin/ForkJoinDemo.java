package com.test.juc.forkjoin;

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
