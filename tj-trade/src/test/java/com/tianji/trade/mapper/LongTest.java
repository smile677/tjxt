package com.tianji.trade.mapper;

public class LongTest {
    public static void main(String[] args) {
        // -128~127 之间是同一个对象
        Long a = 129L;
        Long b = 129L;
        System.out.println(a == b);
        Long c = 128L;
        Long d = 128L;
        System.out.println(c == d);
        Long e = 127L;
        Long f = 127L;
        System.out.println(e == f);
        Long g = -129L;
        Long h = -129L;
        System.out.println(g == h);
        Long i = -128L;
        Long j = -128L;
        System.out.println(i == j);
    }
}
