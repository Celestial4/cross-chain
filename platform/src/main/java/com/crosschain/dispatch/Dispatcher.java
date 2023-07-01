package com.crosschain.dispatch;

import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.Group;
import com.crosschain.service.response.Response;

import java.util.List;

public interface Dispatcher {
    Response process(CrossChainRequest req) throws Exception;

    Response process(CommonChainRequest req) throws Exception;

    void checkAvailable(Group grp, List<CommonChainRequest> reqs) throws Exception;

    void saveRequestId(String id);

    void completeTask();
}