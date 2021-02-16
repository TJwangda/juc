package com.test.juc.function;

import java.util.function.Predicate;

/**
 * Predicate
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
