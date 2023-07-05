package com.crosschain.thread;

import com.crosschain.audit.AuditManager;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.exception.UniException;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.entity.ErrorServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class Task implements Runnable{
    private Dispatcher dispatcher;
    private CrossChainRequest crossChainRequest;
    private AuditManager auditManager;

    @Override
    public void run() {
        try {
            Response response = dispatcher.process(crossChainRequest);
            log.info("complete task{},result: {}",crossChainRequest.getRequestId(),response.get());
        } catch (Exception e) {
            log.error(new ErrorServiceResponse((UniException) e).get());
        }finally {
            log.info("all task:{},now cleaning current completed task...",auditManager.show());
            dispatcher.completeTask(crossChainRequest.getRequestId());
            log.info("after clearing:{}", auditManager.show());
        }
    }
}