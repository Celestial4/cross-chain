package com.crosschain.dispatch;

import com.crosschain.channel.ChannelManager;
import com.crosschain.common.Chain;
import com.crosschain.common.Channel;
import com.crosschain.common.IRequest;
import com.crosschain.common.Loggers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class CrossChainDispatcher {

    @Resource
    private ChannelManager channelManager;

    private final Map<String,String> maps=new HashMap<>();

    public void init() {
        try {
            Properties pros = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");
            pros.load(is);
            Set<Map.Entry<Object, Object>> entries = pros.entrySet();
            for (Map.Entry<Object,Object> entry : entries) {
                String chainName = (String) entry.getKey();
                String socketAddress = (String) entry.getValue();
                maps.put(chainName,socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String process(IRequest request) {

        String desName = request.getDestChain();
        String channelName = request.getDestChannel();
        Channel channel = channelManager.getChannel(channelName);
        Chain desChain = channel.getChain(desName);
        if (Objects.nonNull(desChain) && channel.getStatus()==0 && desChain.getStatus()==0) {
            //todo 跨链请求转发到具体的执行器
            String socAddress = maps.get(desName);
            String[] socketInfo = socAddress.split(":");
            log.info(Loggers.LOGFORMAT,"stub found! Address:"+ Arrays.toString(socketInfo));
            try (Socket socket = new Socket(socketInfo[0], Integer.parseInt(socketInfo[1]))) {
                //发送跨链请求
                OutputStream os = socket.getOutputStream();
                String req = String.format("%s\r\n%s\r\n%s",request.getContract(),request.getContFunc(),request.getArgs());
                log.info(Loggers.LOGFORMAT,"sending crosschain request to "+desName+" stub:"+req);
                os.write(req.getBytes(StandardCharsets.UTF_8));
                InputStream is = socket.getInputStream();

                //读取跨链回执
                byte[] buff = new byte[8192];
                int cnt = is.read(buff);

                byte[] data = new byte[cnt];
                System.arraycopy(buff, 0, data, 0, cnt);
                String res = new String(data, StandardCharsets.UTF_8);
                log.info(Loggers.LOGFORMAT,"received from blockchain:"+res);

                is.close();
                os.close();

                return res;
            } catch (Exception e) {
                log.error(Loggers.LOGFORMAT, e.getMessage());
            }

        } else {
            //todo 失败请求的后续处理
            log.error(Loggers.LOGFORMAT,"跨链请求失败，通道或者目标链处于冻结状态");
        }
        return "crosschain failed!";
    }
}