package com.crosschain.dispatch;

import com.crosschain.channel.ChannelManager;
import com.crosschain.common.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public abstract class DispatcherBase {

    public void setChannelManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    private ChannelManager channelManager;

    protected final Map<String, String> maps = new HashMap<>();

    abstract CommonCrossChainResponse processDes(CommonCrossChainRequest req, Channel channel) throws Exception;

    abstract void processSrc(CommonCrossChainRequest req, Channel channel) throws Exception;

    public void init() {
        try {
            Properties pros = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");
            pros.load(is);
            Set<Map.Entry<Object, Object>> entries = pros.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String chainName = (String) entry.getKey();
                String socketAddress = (String) entry.getValue();
                maps.put(chainName, socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String process(CrossChainRequest request) {
        try {
            Channel channel = channelManager.getChannel(request.getChannel());

            if (channel.getStatus() == 0) {
                CommonCrossChainResponse DesRes = processDes(request.getDesChainRequest(), channel);
                CommonCrossChainRequest srcChainRequest = request.getSrcChainRequest();
                srcChainRequest.setArgs(DesRes.getResult());
                processSrc(srcChainRequest, channel);
                return "crosschain success!";
            } else {
                //todo 失败请求的后续处理
                log.error(Loggers.LOGFORMAT, "跨链请求失败，通道或者目标链处于冻结状态");
            }
        } catch (Exception e) {
            return "crosschain failed!";
        }
        return "crosschain failed!";
    }


    protected byte[] innerCall(String[] socketInfo,String[] req) throws Exception {
        Socket socket = new Socket(socketInfo[0], Integer.parseInt(socketInfo[1]));
        //发送跨链请求
        OutputStream os = socket.getOutputStream();
        String result = String.format("%s\r\n%s\r\n%s", req[0], req[1], req[2]);

        os.write(result.getBytes(StandardCharsets.UTF_8));
        InputStream is = socket.getInputStream();

        //读取跨链回执
        byte[] buff = new byte[8192];
        int cnt = is.read(buff);

        byte[] data = new byte[cnt];
        System.arraycopy(buff, 0, data, 0, cnt);
        return data;
    }
}