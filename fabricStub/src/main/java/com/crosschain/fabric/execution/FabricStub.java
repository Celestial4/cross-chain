package com.crosschain.fabric.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricStub {
    private static final Logger logger = LoggerFactory.getLogger(FabricStub.class);

    public static String handleRequest(String req) {
        logger.debug(req);

        FabricExecutor executor = new FabricExecutor();

        req = req.replace("\r", "");
        String[] callInfo = req.split("\n");
        String chaincode = callInfo[0];
        String method = callInfo[1];
        String[] args = null;

        if (callInfo.length > 2) {
            args = new String[callInfo.length - 2];
            for (int i = 2; i < callInfo.length; i++) {
                args[i - 2] = callInfo[i];
            }
        }

        logger.debug("sending args：[{},{},{}]", chaincode, method, args);
        String res = executor.sendTransaction(chaincode, method, args);
        logger.info(Fabrics.logPlacehdr(), "result gained from fabric:[" + res + "]");
        return res;
    }
}