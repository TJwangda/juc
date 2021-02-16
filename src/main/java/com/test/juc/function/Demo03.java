package com.test.juc.function;

import java.util.function.Consumer;

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
