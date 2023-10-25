package com.crosschain.dispatch;

import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.service.response.Response;

import java.util.List;

public interface Dispatcher {
    //处理两条链
    Response process(CrossChainRequest req) throws Exception;

    //处理一条链
    Response process(CommonChainRequest req) throws Exception;

    void checkAvailable(Group grp, List<CommonChainRequest> reqs) throws Exception;

    void completeTask(String id);
}