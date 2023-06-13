package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TransactionBase extends BaseDispatcher {
    abstract protected String doDes(CrossChainRequest req, Group grp) throws Exception;

    abstract protected String doSrc(CrossChainRequest req, Group grp) throws Exception;

    @Override
    public CrossChainServiceResponse process(CrossChainRequest req) throws Exception {

        Group group = groupManager.getGroup(req.getGroup());
        log.debug("[current group info]: {}", group.toString());
        if (group.getStatus() == 0) {
            log.info("[group info]: {},{}", group.getGroupName(), group.getStatus() == 0 ? "active" : "unavailable");
            CrossChainServiceResponse response = new CrossChainServiceResponse();
            setLocalChain(req);

            try {
                String src_res = doSrc(req, group);
                String des_res = doDes(req, group);
                response.setDesResult(des_res);
                response.setSrcResult(src_res);
            } catch (Exception e) {
                rollBack(req);
            }
            return response;
        } else {
            throw new Exception("跨链请求失败，跨链群组当前不可用");
        }
    }

    private void rollBack(CrossChainRequest reqs) {
        CommonChainRequest srcChainRequest = reqs.getSrcChainRequest();
        CommonChainRequest desChainRequest = reqs.getDesChainRequest();

        srcChainRequest.setFunction("rollback");
        srcChainRequest.setArgs("");

        desChainRequest.setFunction("rollback");
        desChainRequest.setArgs("");

        //todo 做具体的回滚流程
    }
}