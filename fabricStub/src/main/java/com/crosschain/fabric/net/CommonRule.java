package com.crosschain.fabric.net;

import com.crosschain.fabric.execution.Fabrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CommonRule extends Thread{
    static int port = 0;
    static ServerSocket server = null;
    static Method callbackfun;
    private static final Logger logger = LoggerFactory.getLogger(CommonRule.class);

    public static boolean init(int _port, Method call) {
        port = _port;
        callbackfun = call;
        try {
            server = new ServerSocket(port);
            CommonRule cr=new CommonRule();
            cr.start();
            logger.info(Fabrics.logPlacehdr(),"initializing fabric stub!");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                try {
                    socketDetail(socket);
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void socketDetail(Socket s) {
        try {
            InputStream inputStream = s.getInputStream();
            byte[] receive = new byte[8192];

            int len = s.getInputStream().read(receive);
            if (len <= 0) {
                return;
            }
            byte[] data = new byte[len];
            System.arraycopy(receive, 0, data, 0, len);
            String msg = new String(data);
            logger.info(Fabrics.logPlacehdr(),"stub has received cross-chain request: ["+msg+"]");

            //////////////////////////////////////////////////////
            String strid = "0";
            String tp = "";
            int nindexc = msg.indexOf("[Encrypt]");
            if (nindexc > 0) {
                String xxdata = msg.substring(nindexc + 9);
                tp = msg.substring(0, nindexc);
                msg = MmServer.decrypt(xxdata, tp, strid);
            }
            //////////////////////////////////////////////////////

            String sret = null;
            try {
                sret = (String) callbackfun.invoke(null,msg);
                logger.debug(sret);
            } catch (Exception ex) {
                ex.printStackTrace();
                String sxxx=ex.getMessage();
            }
            if (sret == null) {
                sret = "received message from fabric: error";
            }

            /////////////////////////////////////////////////////
            if (!tp.equals("")) {
                sret = MmServer.crypt(sret, tp, strid);
                if (sret.indexOf("error") < 0) {
                    sret = tp + "[Encrypt]" + sret;
                }
            }
            ///////////////////////////////////////////////////

            OutputStream outputStream = s.getOutputStream();
            outputStream.write(sret.getBytes(StandardCharsets.UTF_8));
            logger.info(Fabrics.logPlacehdr(),String.format("sending back result: [%s]", sret));
            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}