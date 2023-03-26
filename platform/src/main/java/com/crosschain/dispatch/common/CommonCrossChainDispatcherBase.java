package com.crosschain.dispatch.common;

import com.crosschain.audit.IAuditEntity;
import com.crosschain.common.*;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.group.GroupManager;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommonCrossChainDispatcherBase implements Dispatcher {

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    private GroupManager groupManager;

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    protected SystemInfo systemInfo;

    abstract CommonCrossChainResponse processDes(CommonCrossChainRequest req, Group group) throws Exception;

    abstract CommonCrossChainResponse processSrc(CommonCrossChainRequest req, Group group) throws Exception;

    abstract String processResult(CommonCrossChainResponse rep);

    abstract void processAudit(IAuditEntity entity);

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
    public ResponseEntity process(CrossChainRequest request) {
        try {
            Group group = groupManager.getChannel(request.getGroup());

            if (group.getStatus() == 0) {
                ResponseEntity response = new ResponseEntity();
                setLocalChain(request);
                CommonCrossChainResponse DesRes = processDes(request.getDesChainRequest(), group);

                response.setDesResult(DesRes.getResult());
                CommonCrossChainRequest srcChainRequest = request.getSrcChainRequest();
                srcChainRequest.setArgs(processResult(DesRes));

                CommonCrossChainResponse srcRes = processSrc(srcChainRequest, group);
                response.setSrcResult(srcRes.getResult());
                //todo 处理存证，由DesRes得到auditEntity,并在子类中实现相应的存证逻辑
                IAuditEntity auditEntity = null;
                processAudit(auditEntity);

                return response;
            } else {
                //todo 失败请求的后续处理
                log.error(Loggers.LOGFORMAT, "跨链请求失败，通道或者目标链处于冻结状态");
            }
        } catch (Exception e) {
            return new ResponseEntity("crosschain failed");
        }
        return new ResponseEntity("crosschain failed");
    }
}