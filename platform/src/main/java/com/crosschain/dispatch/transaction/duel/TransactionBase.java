package com.crosschain.dispatch.transaction.duel;

import com.crosschain.common.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TransactionBase extends BaseDispatcher {
    abstract protected String doDes(CrossChainRequest req, Group grp) throws Exception;

    abstract protected String doSrc(CrossChainRequest req, Group grp) throws Exception;

    @Override
    public ResponseEntity process(CrossChainRequest req) throws Exception {

        Group group = groupManager.getGroup(req.getGroup());
        log.debug("[current group info]: {}", group.toString());
        if (group.getStatus() == 0) {
            log.info("[group info]: {},{}", group.getGroupName(), group.getStatus() == 0 ? "active" : "unavailable");
            ResponseEntity response = new ResponseEntity();
            setLocalChain(req);

            try {
                String src_res = doSrc(req, group);
                String des_res = doDes(req, group);
                response.setDesResult(des_res);
                response.setSrcResult(src_res);
            } catch (Exception e) {
                throw e;
            }
            return response;
        } else {
            //todo 失败请求的后续处理
            return new ResponseEntity("跨链请求失败，跨链群组当前不可用");
        }
    }
}