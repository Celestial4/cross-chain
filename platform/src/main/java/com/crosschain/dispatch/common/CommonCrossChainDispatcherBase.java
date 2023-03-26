package com.crosschain.dispatch.common;

import com.crosschain.audit.IAuditEntity;
import com.crosschain.group.GroupManager;
import com.crosschain.common.Channel;
import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.common.CommonCrossChainResponse;
import com.crosschain.common.Loggers;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.service.CrossChainRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
public abstract class CommonCrossChainDispatcherBase implements Dispatcher {

    public void setChannelManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    private GroupManager groupManager;

    protected final Map<String, String> maps = new HashMap<>();

    abstract CommonCrossChainResponse processDes(CommonCrossChainRequest req, Channel channel) throws Exception;

    abstract void processSrc(CommonCrossChainRequest req, Channel channel) throws Exception;

    abstract String processResult(CommonCrossChainResponse rep);

    abstract void processAudit(IAuditEntity entity);

    public void init() {
        try {
            Properties pros = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");
            pros.load(is);
            Set<Map.Entry<Object, Object>> entries = pros.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String chainName = (String) entry.getKey();
                String socketAddress = (String) entry.getValue();
                maps.put(chainName, socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String process(CrossChainRequest request) {
        try {
            Channel channel = groupManager.getChannel(request.getChannel());

            if (channel.getStatus() == 0) {
                CommonCrossChainResponse DesRes = processDes(request.getDesChainRequest(), channel);

                CommonCrossChainRequest srcChainRequest = request.getSrcChainRequest();
                srcChainRequest.setArgs(processResult(DesRes));

                processSrc(srcChainRequest, channel);

                //todo 处理存证，由DesRes得到auditEntity,并在子类中实现相应的存证逻辑
                IAuditEntity auditEntity = null;
                processAudit(auditEntity);

                return "crosschain success!";
            } else {
                //todo 失败请求的后续处理
                log.error(Loggers.LOGFORMAT, "跨链请求失败，通道或者目标链处于冻结状态");
            }
        } catch (Exception e) {
            return "crosschain failed!";
        }
        return "crosschain failed!";
    }
}