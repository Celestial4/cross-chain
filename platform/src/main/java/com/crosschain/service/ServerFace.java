package com.crosschain.service;

import com.alibaba.fastjson2.JSON;
import com.crosschain.audit.AuditManager;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.CommonChainRequest;
import com.crosschain.common.SystemInfo;
import com.crosschain.common.entity.Group;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.dispatch.Dispatcher;
import com.crosschain.dispatch.DispatcherManager;
import com.crosschain.exception.UniException;
import com.crosschain.filter.RequestFilter;
import com.crosschain.group.GroupManager;
import com.crosschain.service.request.CrossChainVo;
import com.crosschain.service.request.SelfVo;
import com.crosschain.service.response.entity.ErrorServiceResponse;
import com.crosschain.service.response.Response;
import com.crosschain.service.response.UniResponse;
import com.crosschain.statistics.STATCSManager;
import com.crosschain.thread.Task;
import com.crosschain.thread.ThreadManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

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

    private final ThreadManager pool = new ThreadManager();

    Response response;

    @PostMapping("/crosschain")
    @ResponseBody
    public String crossChain(@RequestBody CrossChainVo crossChainVo) {

        CommonChainRequest src = new CommonChainRequest();

        CommonChainRequest des = new CommonChainRequest();

        try {
            //拆解源、目标链的跨链请求
            constructRequest(crossChainVo, src, des);

            Dispatcher dispatcher;
            CrossChainRequest req = new CrossChainRequest(src, des, crossChainVo.getGroup(), crossChainVo.getRequest_id());
            log.debug("[destination request]: {},{},{},{}\n[source request]: {},{},{},{}", des.getChainName(), des.getContract(), des.getFunction(), des.getArgs(), src.getChainName(), src.getContract(), src.getFunction(), src.getArgs());

            //鉴权，验证username usertoken是否匹配以及是否指向自己
            filter.doFilter(crossChainVo);
            //通过mode字段获取相应跨链类型的处理器dispatcher
            dispatcher = dispatcherManager.getDispatcher(crossChainVo.getMode());

            //检查跨链群组合法性以及相应的源、目标链的合法性
            dispatcher.checkAvailable(groupManager.getGroup(req.getGroup()), Arrays.asList(src, des));

            //设置请求id，追踪每一次的跨链请求相关信息（数据上报的信息）
            auditManager.joinRequest(crossChainVo);

            //添加异步任务，具体跨链请求在这个任务里面去执行
            pool.addTask(new Task(dispatcher, req, auditManager));

            //
            response = new UniResponse(200, "success", "跨链请求已提交");
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse((UniException) e);
        }

        return response.get();
    }

    @PostMapping("/selfcall")
    @ResponseBody
    public String selfCall(@RequestBody SelfVo vo) {
        Dispatcher dispatcher;
        Response response;
        CommonChainRequest crossChainRequest = new CommonChainRequest();
        try {
            crossChainRequest.setChainName(SystemInfo.getSelfChainName());
            crossChainRequest.setContract(vo.getContract());
            crossChainRequest.setFunction(vo.getFunction());
            crossChainRequest.setArgs(vo.getArgs());

            dispatcher = dispatcherManager.getDispatcher(vo.getMode());

            response = dispatcher.process(crossChainRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse((UniException) e);
        }

        return response.get();
    }

    @PostMapping("/transaction_lock")
    @ResponseBody
    public String transaction_lock(@RequestBody CrossChainVo crossChainVo) {
        crossChainVo.setMode("lock");
        CommonChainRequest src = new CommonChainRequest();
        CommonChainRequest des = new CommonChainRequest();
        try {
            constructRequest(crossChainVo, src, des);
            Dispatcher dispatcher;
            CrossChainRequest req = new CrossChainRequest(src, des, crossChainVo.getGroup(), crossChainVo.getRequest_id());


            filter.doFilter(crossChainVo);
            dispatcher = dispatcherManager.getDispatcher(crossChainVo.getMode());
            dispatcher.checkAvailable(groupManager.getGroup(req.getGroup()), Arrays.asList(src, des));
            pool.addTask(new Task(dispatcher, req, auditManager));
            response = new UniResponse(200, "success", "跨链请求已提交");
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse((UniException) e);
        }

        return response.get();
    }

    @PostMapping("/transaction_unlock")
    @ResponseBody
    public String transaction_unlock(@RequestBody CrossChainVo crossChainVo) {
        crossChainVo.setMode("unlock");
        CommonChainRequest src = new CommonChainRequest();
        CommonChainRequest des = new CommonChainRequest();
        try {
            constructRequest(crossChainVo, src, des);

            Dispatcher dispatcher;
            CrossChainRequest req = new CrossChainRequest(src, des, crossChainVo.getGroup(), crossChainVo.getRequest_id());


            filter.doFilter(crossChainVo);
            dispatcher = dispatcherManager.getDispatcher(crossChainVo.getMode());
            dispatcher.checkAvailable(groupManager.getGroup(req.getGroup()), Arrays.asList(src, des));
            pool.addTask(new Task(dispatcher, req, auditManager));
            response = new UniResponse(200, "success", "跨链请求已提交");
        } catch (Exception e) {
            log.error(e.getMessage());
            response = new ErrorServiceResponse((UniException) e);
        }

        return response.get();
    }

    @PostMapping("/add_chain")
    @ResponseBody
    public String addChain(@RequestParam("chain_name") String chainName,
                           @RequestParam(value = "chain_status", defaultValue = "0") int status,
                           @RequestParam("chain_type") String chainType) {
        try {
            groupManager.putChain(chainName, status, chainType);

        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success", String.format("链%s添加成功", chainName)).get();
    }

    @PostMapping("/add_group")
    @ResponseBody
    public String addGroup(@RequestParam("group_name") String groupName,
                           @RequestParam(value = "group_status", defaultValue = "0") int status) {
        try {
            groupManager.putGroup0(groupName, status);
        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success", String.format("群组%s添加成功", groupName)).get();
    }

    @PostMapping("/ping")
    @ResponseBody
    public String ping() {
        UniResponse uniResponse = new UniResponse(200, "success", "pong");
        return uniResponse.get();
    }

    @PostMapping("/move")
    @ResponseBody
    public String move(@RequestParam("src_group_n") String sr_cnl_n,
                       @RequestParam("des_group_n") String des_grp_n,
                       @RequestParam("chain_n") String chain_n) {
        try {
            groupManager.removeTo(sr_cnl_n, des_grp_n, chain_n);
        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success", "操作成功").get();
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestParam(value = "type", defaultValue = "1") int type,
                         @RequestParam("target") String target,
                         @RequestParam("status") int status) {
        try {
            groupManager.updateStatus(type, target, status);
        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success", "操作成功").get();
    }

    @PostMapping("/cpu")
    @ResponseBody
    public String getCpuInfo() {
        String cpuInfo = STATCSManager.getCpuInfo();
        return new UniResponse(200, "success", cpuInfo).get();
    }

    @PostMapping("/mem")
    @ResponseBody
    public String getMemInfo() {
        String memoryInfo = STATCSManager.getMemoryInfo();
        return new UniResponse(200, "success", memoryInfo).get();
    }

    @PostMapping("list_allgroups")
    @ResponseBody
    public String listGroups() {
        String ret = "";
        try {
            List<Group> allGroups = groupManager.getAllGroups();
            ret = JSON.toJSONString(allGroups);

        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success",ret).get();
    }

    @PostMapping("list_group")
    @ResponseBody
    public String listGroup(@RequestParam("group_name") String grp_name) {
        String ret = "";
        try {
            Group group = groupManager.getGroup(grp_name);
            ret = JSON.toJSONString(group);

        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success",ret).get();
    }

    @PostMapping("list_allchains")
    @ResponseBody
    public String listChains() {
        String ret = "";
        try {
            List<Chain> group = groupManager.getAllChains();
            ret = JSON.toJSONString(group);

        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success",ret).get();
    }

    @PostMapping("list_chain")
    @ResponseBody
    public String listChain(@RequestParam("chain_name") String chain_name) {
        String ret = "";
        try {
            Chain chain = groupManager.getChain(chain_name);
            ret = JSON.toJSONString(chain);

        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success",ret).get();
    }

    @PostMapping("/delete_group")
    @ResponseBody
    public String deleteGroup(@RequestParam("group_name") String group) {
        try {
            groupManager.deleteGroup(group);
        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success","操作成功").get();
    }

    @PostMapping("/delete_chain")
    @ResponseBody
    public String deleteChain(@RequestParam("chain_name") String chain) {
        try {
            groupManager.deleteChain(chain);
        } catch (UniException e) {
            return new ErrorServiceResponse(e).get();
        }
        return new UniResponse(200, "success","操作成功").get();
    }

    private void constructRequest(CrossChainVo crossChainVo, CommonChainRequest src, CommonChainRequest des) throws Exception {
        src.setRequestId(crossChainVo.getRequest_id());
        src.setChainName(SystemInfo.getSelfChainName());
        src.setContract(crossChainVo.getSrc_contract());
        src.setFunction(crossChainVo.getSrc_function());
        src.setArgs(crossChainVo.getSrc_args());

        des.setRequestId(crossChainVo.getRequest_id());
        des.setChainName(crossChainVo.getDes_chain());
        des.setContract(crossChainVo.getDes_contract());
        des.setFunction(crossChainVo.getDes_function());
        des.setArgs(crossChainVo.getDes_args());
    }
}