package com.crosschain.fabric;

import com.crosschain.fabric.execution.Fabrics;
import com.crosschain.fabric.net.CommonRule;
import com.crosschain.fabric.execution.FabricStub;
import com.crosschain.fabric.net.MmServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        try {
            Fabrics helper = new Fabrics();
            helper.init();
            MmServer mmServer = new MmServer();
            mmServer.init();
            if (CommonRule.init(Fabrics.getDeployPort(), FabricStub.class.getMethod("handleRequest", String.class))) {
                logger.info(Fabrics.logPlacehdr(),"server started. start serving!");
            } else {
                logger.error(Fabrics.logPlacehdr(),"server initialization failed");
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}