package com.crosschain.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CrossChainClient {

    public static byte[] innerCall(String[] socketInfo, String[] req) throws Exception {
        Socket socket = new Socket(socketInfo[0], Integer.parseInt(socketInfo[1]));
        //发送跨链请求
        OutputStream os = socket.getOutputStream();
        StringBuilder result = new StringBuilder();

        for (String i : req) {
            result.append(i).append("\r\n");
        }
        os.write(result.toString().getBytes(StandardCharsets.UTF_8));
        InputStream is = socket.getInputStream();

        //读取跨链回执
        byte[] buff = new byte[8192];
        int cnt = is.read(buff);

        byte[] data = new byte[cnt];
        System.arraycopy(buff, 0, data, 0, cnt);
        return data;
    }
}