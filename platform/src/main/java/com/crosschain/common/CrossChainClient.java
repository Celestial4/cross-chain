package com.crosschain.common;

import com.crosschain.service.MmServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CrossChainClient {

    public static byte[] innerCall(String[] socketInfo, String[] req, String req_id) throws Exception {
        Socket socket = new Socket(socketInfo[0], Integer.parseInt(socketInfo[1]));

        String tp = MmServer.EncryptionType;

        //发送跨链请求
        OutputStream os = socket.getOutputStream();
        StringBuilder result = new StringBuilder();

        for (String i : req) {
            result.append(i).append("\r\n");
        }
        String sret = result.toString();
        /////////////////////////////////////////////////////
        if (!"".equals(tp)) {
            sret = MmServer.crypt(sret, tp, req_id);
            if (sret.indexOf("error") < 0) {
                sret = tp + "[Encrypt]" + sret;
            }
        }
        ///////////////////////////////////////////////////
        os.write(sret.getBytes(StandardCharsets.UTF_8));
        InputStream is = socket.getInputStream();

        //读取跨链回执
        byte[] buff = new byte[8192];
        int cnt = is.read(buff);

        byte[] data = new byte[cnt];
        System.arraycopy(buff, 0, data, 0, cnt);

        //////////////////////////////////////////////////////
        String msg = new String(data);
        tp = "";
        int nindexc = msg.indexOf("[Encrypt]");
        if (nindexc > 0) {
            String xxdata = msg.substring(nindexc + 9);
            tp = msg.substring(0, nindexc);
            msg = MmServer.decrypt(xxdata, tp, req_id);
        }
        //////////////////////////////////////////////////////
        return msg.getBytes(StandardCharsets.UTF_8);
    }
}