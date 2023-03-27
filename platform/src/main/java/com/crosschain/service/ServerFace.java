package com.crosschain.service;

import com.crosschain.auth.AuthManager;
import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.dispatch.DispatcherManager;
import com.crosschain.group.GroupManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.validation.annotation.Validated;
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
    private AuthManager authManager;


    @PostMapping("/crosschain")
    @ResponseBody
    public String resolve(@RequestBody RequestEntity requestEntity) {
        if (!authManager.authForUser(requestEntity.getUserName(), requestEntity.getUserToken())) {
            return new ResponseEntity("authentication failed!").getErrorMsg();
        }

        CommonCrossChainRequest src = new CommonCrossChainRequest();
        src.setChainName("local");
        src.setContract(requestEntity.getSrcContract());
        src.setFunction(requestEntity.getSrcFunction());

        CommonCrossChainRequest des = new CommonCrossChainRequest();
        des.setChainName(requestEntity.getDesChain());
        des.setContract(requestEntity.getDesContract());
        des.setFunction(requestEntity.getDesFunction());
        des.setArgs(requestEntity.getArgs().replaceAll(",", "\r\n"));

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src,des, requestEntity.getGroup());
        log.debug("[destination request]: {},{},{},{}\n[source request]: {},{},{}",des.getChainName(),des.getContract(),des.getFunction(),requestEntity.getArgs(),src.getChainName(),src.getContract(),src.getFunction());
        ResponseEntity response;
        try {
            dispatcher = dispatcherManager.getDispatcher(requestEntity.getMode());
            log.debug("[acquired crosschain dispatcher]: {}",dispatcher.getClass());
            response = dispatcher.process(req);
        } catch (Exception e) {
            log.error(e.getStackTrace().toString());
            response = new ResponseEntity(e.getMessage());
        }
        if (!response.getErrorMsg().equals("")) {
            return response.getErrorMsg();
        }
        return String.format("[desChainResult]:---\n%s\n[srcChainResult]:---\n%s\n",response.getDesResult(),response.getSrcResult());
    }

    @PostMapping("/add_chain")
    public String addChain(@Validated @RequestParam("chain_name") String chainName,
                           @RequestParam(value = "chain_status", defaultValue = "0") int status) {
        int cnt = groupManager.putChain(chainName, status);
        return cnt > 0 ? "successful" : "failed";
    }

    @PostMapping("/add_channel")
    public String addChannel(@Validated @RequestParam("channel_name") String channelName,
                             @RequestParam(value = "channel_status", defaultValue = "0") int status,
                             @RequestParam(value = "chains", defaultValue = "") String chains) {
        String[] split = null;
        if (Strings.isNotEmpty(chains)) {
            split = chains.split(",");
        }
        int cnt = groupManager.putChannel(channelName, status, split);
        return cnt > 0 ? "successful" : "failed";
    }

    @PostMapping("/move")
    public String move(@RequestParam("src_channel_n") String sr_cnl_n,
                       @RequestParam(value = "des_channel_n", defaultValue = "") String des_cnl_n,
                       @RequestParam("chain_n") String chain_n) {
        int res_code = 1;
        if (Strings.isEmpty(des_cnl_n)) {
            res_code = groupManager.removeTo(sr_cnl_n, null, chain_n);
        } else {
            res_code= groupManager.removeTo(sr_cnl_n, des_cnl_n, chain_n);
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

}