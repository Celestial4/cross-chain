package com.crosschain.thread;

import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class Task implements Runnable{
    private Dispatcher dispatcher;
    private CrossChainRequest crossChainRequest;

    @Override
    public void run() {
        try {
            dispatcher.process(crossChainRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}