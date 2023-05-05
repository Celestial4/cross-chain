package com.crosschain.dispatch.basic;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.CommonChainResponse;
import com.crosschain.common.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.CrossChainServiceResponse;
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
        log.debug("[current group info]: {}", group.toString());
        if (group.getStatus() == 0) {
            CrossChainServiceResponse response = new CrossChainServiceResponse();
            setLocalChain(request);

            CommonChainResponse DesRes = processDes(request.getDesChainRequest(), group);
            response.setDesResult(DesRes.getResult());

            CommonChainRequest srcChainRequest = request.getSrcChainRequest();
            if ("".equals(srcChainRequest.getArgs())) {
                srcChainRequest.setArgs(processResult(DesRes));
            }
            CommonChainResponse srcRes = processSrc(srcChainRequest, group);

            //源链执行后上报数据
            processAudit(request, srcRes.getResult());
            response.setSrcResult(srcRes.getResult());

            return response;
        } else {
            throw new Exception("跨链请求失败，跨链群组当前不可用");
        }
    }
}