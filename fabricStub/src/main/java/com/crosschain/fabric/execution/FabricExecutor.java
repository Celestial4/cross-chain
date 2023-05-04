package com.crosschain.fabric.execution;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.hyperledger.fabric.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class FabricExecutor {

    private final Logger logger = LoggerFactory.getLogger(FabricExecutor.class);

    private Gateway fabricGateway;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String sendTransaction(String chainCodeName, String call, String[] args) {
        fabricGateway = Fabrics.fabricGateway();
        Network network = fabricGateway.getNetwork(Fabrics.getChannel());
        Contract contract = network.getContract(chainCodeName);
        byte[] res = null;
        try {
            if (Objects.isNull(args)) {
                logger.info(Fabrics.logPlacehdr(),"executing contract:["+call+"]");
                res = contract.submitTransaction(call);
            } else {
                logger.info(Fabrics.logPlacehdr(),"executing contract:["+call+ Arrays.toString(args)+"]");
                res = contract.submitTransaction(call,args);
            }
        } catch (EndorseException | SubmitException | CommitStatusException | CommitException e) {
            e.printStackTrace();
        }

        Fabrics.closeChannel();
        logger.info("*** Transaction committed successfully");
        String resJson = prettyJson(res);
        logger.debug(prettyJson(resJson));
        return resJson;
    }

    public Object queryLedger(Object request) {
        fabricGateway = Fabrics.fabricGateway();
        Network network = fabricGateway.getNetwork(Fabrics.getChannel());


        Fabrics.closeChannel();
        return null;
    }

    private String prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private String prettyJson(final String json) {
        JsonElement parsedJson = JsonParser.parseString(json);
        return gson.toJson(parsedJson);
    }
}