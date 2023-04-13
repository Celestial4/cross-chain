package com.crosschain.service;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.dispatch.DispatcherManager;
import com.crosschain.filter.RequestFilter;
import com.crosschain.group.GroupManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
public class ServerFace {

    @Resource
    private DispatcherManager dispatcherManager;

    @Resource
    private GroupManager groupManager;

    @Resource
    private AuditManager auditManager;

    @Resource
    private RequestFilter filter;


    @PostMapping("/crosschain")
    @ResponseBody
    public String resolve(@RequestBody RequestEntity requestEntity) {
        CommonCrossChainRequest src = new CommonCrossChainRequest();

        CommonCrossChainRequest des = new CommonCrossChainRequest();

        setRequest(requestEntity, src, des);
        auditManager.setRequest(requestEntity);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src, des, requestEntity.getGroup());
        log.debug("[destination request]: {},{},{},{}\n[source request]: {},{},{}", des.getChainName(), des.getContract(), des.getFunction(), requestEntity.getDesArgs(), src.getChainName(), src.getContract(), src.getFunction());
        ResponseEntity response;
        try {
            filter.doFilter(requestEntity);
            dispatcher = dispatcherManager.getDispatcher(requestEntity.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ResponseEntity(e.getMessage());
        }
        if (!response.getErrorMsg().equals("")) {
            return response.getErrorMsg();
        }
        return String.format("[desChainResult]:---\n%s\n[srcChainResult]:---\n%s\n", response.getDesResult(), response.getSrcResult());
    }

    @PostMapping("/transaction_lock")
    @ResponseBody
    public String transaction_lock(@RequestBody RequestEntity requestEntity) {
        requestEntity.setMode("lock");
        CommonCrossChainRequest src = new CommonCrossChainRequest();
        CommonCrossChainRequest des = new CommonCrossChainRequest();
        setRequest(requestEntity, src, des);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src, des, requestEntity.getGroup());
        ResponseEntity response;
        try {
            filter.doFilter(requestEntity);
            dispatcher = dispatcherManager.getDispatcher(requestEntity.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ResponseEntity(e.getMessage());
        }
        if (!response.getErrorMsg().equals("")) {
            return response.getErrorMsg();
        }
        return String.format("[desChainResult]:---\n%s\n[srcChainResult]:---\n%s\n", response.getDesResult(), response.getSrcResult());
    }

    @PostMapping("/transaction_unlock")
    @ResponseBody
    public String transaction_unlock(@RequestBody RequestEntity requestEntity) {
        requestEntity.setMode("unlock");
        CommonCrossChainRequest src = new CommonCrossChainRequest();
        CommonCrossChainRequest des = new CommonCrossChainRequest();
        setRequest(requestEntity, src, des);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src, des, requestEntity.getGroup());
        ResponseEntity response;
        try {
            filter.doFilter(requestEntity);
            dispatcher = dispatcherManager.getDispatcher(requestEntity.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ResponseEntity(e.getMessage());
        }
        if (!response.getErrorMsg().equals("")) {
            return response.getErrorMsg();
        }
        return String.format("[desChainResult]:---\n%s\n[srcChainResult]:---\n%s\n", response.getDesResult(), response.getSrcResult());
    }

    @PostMapping("/add_chain")
    public String addChain(@RequestParam("chain_name") String chainName,
                           @RequestParam(value = "chain_status", defaultValue = "0") int status) {
        int cnt = groupManager.putChain(chainName, status);
        return cnt > 0 ? "successful" : "failed";
    }

    @PostMapping("/add_group")
    public String addChannel(@RequestParam("group_name") String groupName,
                             @RequestParam(value = "group_status", defaultValue = "0") int status,
                             @RequestParam(value = "chains", defaultValue = "") String chains) {
        String[] split = null;
        if (Strings.isNotEmpty(chains)) {
            split = chains.split(",");
        }
        int cnt = groupManager.putGroup(groupName, status, split);
        return cnt > 0 ? "successful" : "failed";
    }

    @PostMapping("/move")
    public String move(@RequestParam("src_group_n") String sr_cnl_n,
                       @RequestParam(value = "des_group_n", defaultValue = "") String des_cnl_n,
                       @RequestParam("chain_n") String chain_n) {
        int res_code = 1;
        if (Strings.isEmpty(des_cnl_n)) {
            res_code = groupManager.removeTo(sr_cnl_n, null, chain_n);
        } else {
            res_code = groupManager.removeTo(sr_cnl_n, des_cnl_n, chain_n);
        }
        return res_code == 1 ? "operation failed" : "operation success";
    }

    @PostMapping("update")
    public String update(@RequestParam(value = "type", defaultValue = "1") int type,
                         @RequestParam("target") String target,
                         @RequestParam("status") int status) {
        int res_code = groupManager.updateStatus(type, target, status);
        return res_code == 1 ? "operation failed" : "operation success";
    }


    private void setRequest(RequestEntity requestEntity, CommonCrossChainRequest src, CommonCrossChainRequest des) {
        src.setChainName("local");
        src.setContract(requestEntity.getSrcContract());
        src.setFunction(requestEntity.getSrcFunction());
        src.setArgs(requestEntity.getSrcArgs().replaceAll(",", "\r\n"));

        des.setChainName(requestEntity.getDesChain());
        des.setContract(requestEntity.getDesContract());
        des.setFunction(requestEntity.getDesFunction());
        des.setArgs(requestEntity.getDesArgs().replaceAll(",", "\r\n"));
    }
}