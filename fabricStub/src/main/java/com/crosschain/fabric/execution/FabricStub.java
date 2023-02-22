package com.crosschain.fabric.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
            args = callInfo[2].split(",");
        }

        logger.debug("sending args：["+chaincode+method+ Arrays.toString(args) +"]");
        String res = executor.sendTransaction(chaincode, method, args);
        logger.info(Fabrics.logPlacehdr(),"result gained from fabric:["+res+"]");
        return res;
    }
}