package com.crosschain.dispatch;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CrossChainClient {

    public static byte[] innerCall(String[] socketInfo, String[] req) throws Exception {
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