package com.crosschain.dispatch.transaction.single;

import com.crosschain.common.CommonChainRequest;
import com.crosschain.common.CommonChainResponse;
import com.crosschain.common.Group;
import com.crosschain.dispatch.BaseDispatcher;
import com.crosschain.dispatch.CrossChainRequest;
import com.crosschain.service.response.CrossChainServiceResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SingleTransactionCrossChainDispatcher extends BaseDispatcher {

    CommonChainResponse processDes(CommonChainRequest req, Group group) throws Exception {
        checkAvailable(group, req);

        log.info("[des chain do]---\n");
        String res = sendTransaction(req);

        return new CommonChainResponse(res);
    }

    CommonChainResponse processSrc(CommonChainRequest req, Group group) throws Exception {
        checkAvailable(group, req);

        log.info("[src chain do]---\n");
        String res = sendTransaction(req);

        return new CommonChainResponse(res);
    }

    @Override
    public CrossChainServiceResponse process(CrossChainRequest request) throws Exception {
        setLocalChain(request);

        Group group = groupManager.getGroup(request.getGroup());
        log.info("[current group info]: {}", group.toString());
        CrossChainServiceResponse response = new CrossChainServiceResponse();
        if (group.getStatus() == 0) {

            CommonChainRequest srcChainRequest = request.getSrcChainRequest();
            CommonChainRequest desChainRequest = request.getDesChainRequest();
            //add current timestamp
            long current_time = System.currentTimeMillis() / 1000;
            String ori = srcChainRequest.getArgs();
            ori = ori + "\r\n" + current_time;
            srcChainRequest.setArgs(ori);

            //源链锁资产
            CommonChainResponse srcRes = null;
            boolean res_flag;
            try {
                srcRes = processSrc(srcChainRequest, group);
                response.setSrcResult("lock:\n" + srcRes.getResult() + "\n");
                res_flag = extractInfo("status", srcRes.getResult()).equals("1");
                if (!res_flag) {
                    throw new Exception("源链资产锁定失败");
                }
            } catch (Exception e) {
                throw new Exception("跨链失败：源链事务合约执行异常，" + e.getMessage());
            }

            //通过正则读取sender和原像
            Pattern p = Pattern.compile("(\\w+)(\\s+)(\\w+)\\2(\\w+)\\2(\\w+)\\2(\\w+)");
            Matcher m = p.matcher(srcChainRequest.getArgs());
            String sender;
            String h;
            if (m.find()) {
                sender = m.group(1);
                h = m.group(4);
            } else {
                throw new Exception("源链事务合约参数设置错误");
            }

            //do deschain or rollback
            CommonChainResponse desRes = null;
            try {
                desRes = processDes(desChainRequest, group);
                response.setDesResult(desRes.getResult());

                //目标链执行成功
                res_flag = extractInfo("status", desRes.getResult()).equals("1");
                if (res_flag) {
                    String lock_addr = extractInfo("addr", srcRes.getResult());
                    current_time = System.currentTimeMillis() / 1000;
                    String unlock_args = sender + "\r\n" + h + "\r\n" + lock_addr + "\r\n" + current_time;
                    srcChainRequest.setFunction("unlock");
                    srcChainRequest.setArgs(unlock_args);

                    //actually do unlock
                    srcRes = processSrc(srcChainRequest, group);
                    String final_src_resp = response.getSrcResult() + "unlock:\n" + srcRes.getResult();
                    response.setSrcResult(final_src_resp);
                } else {
                    //rollback
                    rollback(group, response, srcChainRequest, srcRes, sender, h);
                }
                //源链解锁后上报事务数据
                if (!"rollback".equals(srcRes.getResult())) {
                    try {
                        processAudit(request, srcRes.getResult());
                    } catch (Exception e) {
                        log.info("跨链成功，但事务上报出现错误：\n{}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                rollback(group, response, srcChainRequest, srcRes, sender, h);
                throw new Exception("跨链失败，合约执行异常："+e.getMessage());
            }

            return response;
        } else {
            throw new Exception("跨链请求失败，跨链群组当前不可用");
        }
    }

    private void rollback(Group group, CrossChainServiceResponse response, CommonChainRequest
            srcChainRequest, CommonChainResponse srcRes, String sender, String h) throws Exception {
        log.info("----rollback-----");
        String rollback_args = sender + "\r\n" + h;
        srcChainRequest.setFunction("rollback");
        srcChainRequest.setArgs(rollback_args);
        processSrc(srcChainRequest, group);
        srcRes.setResult("rollback");
        String final_src_resp = response.getSrcResult() + "-------------\nrollbacked";
        response.setSrcResult(final_src_resp);
    }
}