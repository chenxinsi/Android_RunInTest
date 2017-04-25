package com.android.runintest;

import java.util.Random;
import java.util.regex.Pattern;

import android.util.Log;

class CPUTestThread implements Runnable {
    int busyTime = 5;
    private static final int IMPROVE_CPU_TIME = 6 * 60 * 1000;
    private boolean mRunCPU = false;

    public void enableRunCPU(boolean enable) {
        mRunCPU = enable;
    }

    @Override
    public void run() {
        int idleTime = busyTime;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) <= IMPROVE_CPU_TIME && mRunCPU) {
            double b;
            try {
                int n=0;
                double a=0;
                for (n=0;n<=1000000000;n++ )
                {
                    if((System.currentTimeMillis() - startTime) >= IMPROVE_CPU_TIME || !mRunCPU)
                    {
                        return;
                    }
                    a = a + 4*Math.pow(-1,n)/(2*n+1);
                }          
                Thread.sleep(idleTime);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

    }

    public long Result(int n) {
        long m = 1;
        int i = 1;

        do {
            m *= i;
            i++;
        } while (i <= n);

        return m;
    }

    /*
     * class RegexThread extends Thread { RegexThread() { // Create a new,
     * second thread super("Regex Thread"); start(); // Start the thread }
     * 
     * // This is the entry point for the second thread. public void run() {
     * while(true) { Pattern p =
     * Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:
     * \.[a-z0-9!#$%&'*+/=?^_`{|}
     * ~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+(?:
     * [A-Z]{2}|com|org|net|edu|gov
     * |mil|biz|info|mobi|name|aero|asia|jobs|museum)\b"); } } }
     * 
     * class CPUStresser { public static void main(String args[]) { static int
     * NUM_THREADS = 10, RUNNING_TIME = 120; // run 10 threads for 120s for(int
     * i = 0; i < NUM_THREADS; ++i) { new RegexThread(); // create a new thread
     * } Thread.sleep(1000 * RUNNING_TIME); } }
     */

}
