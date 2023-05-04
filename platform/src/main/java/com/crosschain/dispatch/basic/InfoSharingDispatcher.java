package com.crosschain.dispatch.basic;

import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.CommonCrossChainResponse;
import com.crosschain.common.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InfoSharingDispatcher extends BaseDispatcher {
    abstract protected CommonCrossChainResponse processDes(CommonCrossChainRequest req, Group group) throws Exception;

    abstract protected CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception;

    abstract protected String processResult(CommonCrossChainResponse rep);

    abstract protected void processAudit(CrossChainRequest req,String msgRtd) throws Exception;

    @Override
    public ResponseEntity process(CrossChainRequest request) throws Exception {
        Group group = groupManager.getGroup(request.getGroup());
        log.debug("[current group info]: {}", group.toString());
        if (group.getStatus() == 0) {
            ResponseEntity response = new ResponseEntity();
            setLocalChain(request);

            CommonCrossChainResponse DesRes = processDes(request.getDesChainRequest(), group);
            response.setDesResult(DesRes.getResult());

            CommonCrossChainRequest srcChainRequest = request.getSrcChainRequest();
            if ("".equals(srcChainRequest.getArgs())) {
                srcChainRequest.setArgs(processResult(DesRes));
            }
            CommonCrossChainResponse srcRes = processSrc(srcChainRequest, group);

            //源链执行后上报数据
            processAudit(request, srcRes.getResult());
            response.setSrcResult(srcRes.getResult());

            return response;
        } else {
            return new ResponseEntity("跨链请求失败，跨链群组当前不可用");
        }
    }
}