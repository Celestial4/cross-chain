package com.crosschain.dispatch;

import org.springframework.context.ApplicationContext;

import java.util.Map;

public class DispatcherManager {
    private Map<String, DispatcherBase> dispatcherPool;


    public void init(ApplicationContext app) {
        dispatcherPool = app.getBeansOfType(DispatcherBase.class);
    }

    public DispatcherBase getDispatcher(String mode) throws Exception{
        for (Map.Entry<String, DispatcherBase> entry : dispatcherPool.entrySet()) {
            if (entry.getKey().startsWith(mode)) {
                return entry.getValue();
            }
        }
        throw new Exception("跨链通信协议设置错误");
    }
}