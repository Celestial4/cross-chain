package com.crosschain.thread;

import lombok.NoArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public class ThreadManager {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void addTask(Runnable task) {
        executor.submit(task);
    }
}