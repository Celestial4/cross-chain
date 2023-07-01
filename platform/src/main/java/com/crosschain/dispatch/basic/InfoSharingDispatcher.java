package com.crosschain.dispatch.basic;

import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.entity.CommonChainResponse;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.entity.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InfoSharingDispatcher extends BaseDispatcher {
    abstract protected CommonChainResponse processDes(CommonChainRequest req, Group group) throws Exception;

    abstract protected CommonChainResponse processSrc(CommonChainRequest req, Group group) throws Exception;

    abstract protected String processResult(CommonChainResponse rep);

    abstract protected void processAudit(CrossChainRequest req, String msgRtd) throws Exception;

    @Override
    public CrossChainServiceResponse process(CrossChainRequest request) throws Exception {
        Group group = groupManager.getGroup(request.getGroup());

        CrossChainServiceResponse response = new CrossChainServiceResponse();

        CommonChainRequest desChainRequest = request.getDesChainRequest();
        CommonChainResponse DesRes = processDes(desChainRequest, group);
        response.setDesResult(DesRes.getResult());

        CommonChainRequest srcChainRequest = request.getSrcChainRequest();
        if ("".equals(srcChainRequest.getArgs())) {
            //如果需要在源链上回写目标链合约结果，修改processResult（）函数
            srcChainRequest.setArgs(processResult(DesRes));
        }
        CommonChainResponse srcRes = processSrc(srcChainRequest, group);

        //源链执行后上报数据
        processAudit(request, srcRes.getResult());
        response.setSrcResult(srcRes.getResult());

        return response;
    }
}