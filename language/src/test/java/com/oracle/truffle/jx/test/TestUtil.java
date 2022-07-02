package com.oracle.truffle.jx.test;

public class TestUtil {
    public static void runWithStackTrace(Runnable r) {
        try{
            r.run();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
