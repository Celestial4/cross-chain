package com.crosschain.dispatch;

import com.crosschain.exception.CrossChainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

@Slf4j
public class DispatcherManager {
    private Map<String, Dispatcher> dispatcherPool;


    public void init(ApplicationContext app) {
        dispatcherPool = app.getBeansOfType(Dispatcher.class);
    }

    public Dispatcher getDispatcher(String mode) throws Exception{
        for (Map.Entry<String, Dispatcher> entry : dispatcherPool.entrySet()) {
            if (entry.getKey().startsWith(mode)) {
                log.info("take the dispatcher: class: [{}]",entry.getValue());
                return entry.getValue();
            }
        }
        throw new CrossChainException(100,String.format("跨链通信协议%s设置错误，请查阅相关手册", mode));
    }
}