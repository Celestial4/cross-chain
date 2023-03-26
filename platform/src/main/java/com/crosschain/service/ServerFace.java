package com.crosschain.service;

import com.crosschain.auth.AuthManager;
import com.crosschain.group.GroupManager;
import com.crosschain.common.CommonCrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.dispatch.DispatcherManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String resolve(@RequestParam("channel") String channel,
                          @RequestParam("des_chain") String desChain,
                          @RequestParam("des_contract") String desContract,
                          @RequestParam("des_function") String desFunc,
                          @RequestParam(value = "args",defaultValue = "") String args,
                          @RequestParam("src_contract") String srcContract,
                          @RequestParam("src_function") String srcFunc,
                          @RequestParam(value = "mode",defaultValue = "default") String mode,
                          @RequestParam("user_name") String username,
                          @RequestParam("user_token") String token) {
        if (!authManager.authForUser(username, token)) {
            return "authentication failed!";
        }

        CommonCrossChainRequest src = new CommonCrossChainRequest();
        src.setChainName("local");
        src.setContract(srcContract);
        src.setFunction(srcFunc);

        CommonCrossChainRequest des = new CommonCrossChainRequest();
        des.setChainName(desChain);
        des.setContract(desContract);
        des.setFunction(desFunc);
        des.setArgs(args);

        Dispatcher dispatcher;
        CrossChainRequest req = new CrossChainRequest(src,des,channel);
        try {
            dispatcher = dispatcherManager.getDispatcher(mode);
        } catch (Exception e) {
            return e.getMessage();
        }

        return dispatcher.process(req);
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
        return res_code == 1 ? "operation failed!" : "operation success!";
    }

    @PostMapping("update")
    public String update(@RequestParam(value = "type", defaultValue = "1") int type,
                         @RequestParam("target") String target,
                         @RequestParam("status") int status) {
        int res_code = groupManager.updateStatus(type, target, status);
        return res_code == 1 ? "operation failed!" : "operation success!";
    }

}