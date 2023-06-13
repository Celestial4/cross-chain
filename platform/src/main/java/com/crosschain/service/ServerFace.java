package com.crosschain.service;

import com.crosschain.audit.AuditManager;
import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.SystemInfo;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.dispatch.DispatcherManager;
import com.crosschain.filter.RequestFilter;
import com.crosschain.group.GroupManager;
import com.crosschain.service.request.CrossChainVo;
import com.crosschain.service.request.SelfVo;
import com.crosschain.service.response.ErrorServiceResponse;
import com.crosschain.service.response.Response;
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

    Response response;

    @PostMapping("/crosschain")
    @ResponseBody
    public String crossChain(@RequestBody CrossChainVo crossChainVo) {
        CommonChainRequest src = new CommonChainRequest();

        CommonChainRequest des = new CommonChainRequest();

        setRequest(crossChainVo, src, des);
        auditManager.setRequest(crossChainVo);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src, des, crossChainVo.getGroup());
        log.debug("[destination request]: {},{},{},{}\n[source request]: {},{},{}", des.getChainName(), des.getContract(), des.getFunction(), crossChainVo.getDesArgs(), src.getChainName(), src.getContract(), src.getFunction());

        try {
            //鉴权
            filter.doFilter(crossChainVo);

            dispatcher = dispatcherManager.getDispatcher(crossChainVo.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse(e.getMessage());
        }

        return response.get();
    }

    @PostMapping("/selfcall")
    @ResponseBody
    public String selfCall(@RequestBody SelfVo vo) {
        Dispatcher dispatcher;
        Response response;
        CommonChainRequest crossChainRequest = new CommonChainRequest();
        crossChainRequest.setChainName(SystemInfo.getSelfChainName());
        crossChainRequest.setContract(vo.getContract());
        crossChainRequest.setFunction(vo.getFunction());
        crossChainRequest.setArgs(vo.getArgs().replaceAll(",","\r\n"));

        try {
            dispatcher = dispatcherManager.getDispatcher(vo.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(crossChainRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse(e.getMessage());
        }

        return response.get();
    }

    @PostMapping("/transaction_lock")
    @ResponseBody
    public String transaction_lock(@RequestBody CrossChainVo crossChainVo) {
        crossChainVo.setMode("lock");
        CommonChainRequest src = new CommonChainRequest();
        CommonChainRequest des = new CommonChainRequest();
        setRequest(crossChainVo, src, des);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src, des, crossChainVo.getGroup());

        try {
            filter.doFilter(crossChainVo);
            dispatcher = dispatcherManager.getDispatcher(crossChainVo.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse(e.getMessage());
        }

        return response.get();
    }

    @PostMapping("/transaction_unlock")
    @ResponseBody
    public String transaction_unlock(@RequestBody CrossChainVo crossChainVo) {
        crossChainVo.setMode("unlock");
        CommonChainRequest src = new CommonChainRequest();
        CommonChainRequest des = new CommonChainRequest();
        setRequest(crossChainVo, src, des);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src, des, crossChainVo.getGroup());

        try {
            filter.doFilter(crossChainVo);
            dispatcher = dispatcherManager.getDispatcher(crossChainVo.getMode());
            log.debug("[acquired crosschain dispatcher]: {}", dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse(e.getMessage());
        }

        return response.get();
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
        int res_code;
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


    private void setRequest(CrossChainVo crossChainVo, CommonChainRequest src, CommonChainRequest des) {
        src.setChainName("local");
        src.setContract(crossChainVo.getSrcContract());
        src.setFunction(crossChainVo.getSrcFunction());
        src.setArgs(crossChainVo.getSrcArgs().replaceAll(",", "\r\n"));

        des.setChainName(crossChainVo.getDesChain());
        des.setContract(crossChainVo.getDesContract());
        des.setFunction(crossChainVo.getDesFunction());
        des.setArgs(crossChainVo.getDesArgs().replaceAll(",", "\r\n"));
    }
}