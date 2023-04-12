package com.crosschain.dispatch.transaction;

import com.crosschain.common.Chain;
import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.Group;
import com.crosschain.common.SystemInfo;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.group.GroupManager;
import com.crosschain.service.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public abstract class TransactionBase implements Dispatcher {

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    private GroupManager groupManager;

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    protected SystemInfo systemInfo;


    abstract String doDes(CrossChainRequest req, Group grp) throws Exception;

    abstract String doSrc(CrossChainRequest req, Group grp) throws Exception;

    private void setLocalChain(CrossChainRequest req) {
        CommonCrossChainRequest src = req.getSrcChainRequest();
        if (src.getChainName().equals("local")) {
            src.setChainName(SystemInfo.getSelfChainName());
        }
        CommonCrossChainRequest des = req.getDesChainRequest();
        if (des.getChainName().equals("local")) {
            des.setChainName(SystemInfo.getSelfChainName());
        }

    }

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

    protected void check(Group grp, CommonCrossChainRequest reqInfo) throws Exception {
        Chain chain = grp.getChain(reqInfo.getChainName());

        if (!Objects.nonNull(chain)) {
            log.info("目标链不在跨链群组中");
            throw new Exception("目标链不在跨链群组");
        } else if (chain.getStatus() != 0) {
            log.info("目标链当前不可用");
            throw new Exception("目标链当前不可用");
        }
    }
}