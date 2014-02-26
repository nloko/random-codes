//
//  DynamicThreadPool.java
//
//  Authors:
//      Neil Loknath <neil.loknath@gmail.com>
//
//  Copyright 2010 Neil Loknath
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
 
package com.nloko.util.concurrent;
 
import java.util.Queue;
 
/**
 * A thread pool that automatically adjusts its size depending
 * on specified settings / conditions
 * @author neil
 *
 */
public class DynamicThreadPool {
    private volatile boolean running = true;
    /**
     * Queued work
     */
    private final Queue queue;
 
    /**
     * Minimum number of threads
     */
    private final int minThreads;
    /**
     * Maximum number of threads
     */
    private final int maxThreads;
    /**
     * Threshold of queued work to exceed before starting more threads
     */
    private final int threshold;
 
    /**
     * Track threads
     */
    private final Thread[] threads;
 
    /**
     * Number of threads
     */
    private int threadCount;
 
    /**
     * Create thread pool
     * @param q
     */
    public DynamicThreadPool(Queue q) {
        this(q, 0, 2);
    }
 
    /**
     * Create thread pool with specified min and max threads
     * @param q
     * @param min
     * @param max
     */
    public DynamicThreadPool(Queue q, int min, int max) {
        this(q, min, max, 50);
    }
 
    /**
     * Create thread pool with specified min and max threads
     * @param q
     * @param min
     * @param max
     * @param threshold - queued work to exceed before starting threads up to max
     */
    public DynamicThreadPool(Queue q, int min, int max, int threshold) {
        queue = q;
        minThreads = min;
        maxThreads = max;
        this.threshold = threshold;
 
        threads = new Thread[maxThreads];
 
        // start initial number of threads
        for (int i = 0; i < minThreads; i++) {
            threads[i] = new Worker(i);
            threads[i].start();
 
            System.out.println("Initializing thread number " + i);
        }
 
        threadCount = minThreads;
    }
 
    /**
     * Get number of active threads
     * @return
     */
    public int activeThreads() {
        int active = 0;
 
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] != null && threads[i].isAlive()) active++;
        }
 
        return active;
    }
 
    /**
     * Shutdown threads and flag pool as not running
     */
    public void shutdown() {
        System.out.println("Shutting pool down.");
 
        running = false;
 
        // empty queue and notify all waiting threads
        synchronized(queue) {
            queue.clear();
            queue.notifyAll();
        }
    }
 
    /**
     * Execute an asynchronous task
     * @param r
     */
    public void execute(Runnable r) {
        synchronized(queue) {
            // start a thread if none running or threshold exceeded
            if (threadCount == 0 || queue.size() >= threshold) {
                if (threadCount < maxThreads) {
                    threads[threadCount] = new Worker(threadCount);
                    threads[threadCount].start();
 
                    System.out.println("Thread needed. Creating number " + threadCount);
                    threadCount++;
                }
            }
 
            queue.add(r);
            queue.notify();
        }
    }
 
    /**
     * Inner class as worker thread to run queued tasks
     * @author neil
     *
     */
    private class Worker extends Thread {
        /*
         * Track position of thread in array
         */
        private final int index;
 
        public Worker(int index) {
            this.index = index;
        }
 
        public void run() {
            while (running) {
                Runnable r = null;
 
                synchronized(queue) {
                    if (!queue.isEmpty()) {
                        r = (Runnable) queue.poll();
                    }
                    // queue is empty, so kill active threads over minimum
                    else if (threadCount > minThreads) {
                        System.out.println("Too many threads. Killing thread number " + index);
 
                        // eliminate reference to dying thread so it can be GC'd
                        threads[index] = threads[threadCount - 1];
                        threads[threadCount - 1] = null;
                        threadCount--;
 
                        // notify other threads waiting so they can terminate / process work
                        queue.notify();
 
                        break;
                    }
                    // minimum threads active, so just wait for work
                    else {
                        try {
                            System.out.println("Thread number " + index + " waiting for work");
                            queue.wait();
 
                            System.out.println("Thread number " + index + " awakened");
                        } catch (InterruptedException e) {}
                    }
                }
 
                if (r != null) {
                    try {
                        // run schedule work
                        r.run();
                    } catch (Throwable t) {
                        // prevent thread leakage
                    }
                }
            }
 
            System.out.println("Thread number " + index + " finished");
        }
    }
}