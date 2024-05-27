package com.crosschain.fabric.net;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Properties;

public class MmServer {

    private static final Logger logger = LoggerFactory.getLogger(MmServer.class);
    public static String EncryptionType="";

    static String mmserver="";

    static String sm2_c = "/relay/cryptsm2";
    static String sm2_d = "/relay/decryptsm2";

    static String sm4_c = "/relay/cryptsm4";
    static String sm4_d = "/relay/decryptsm4";

    static String rsa_c = "/relay/cryptrsa";
    static String rsa_d = "/relay/decryptrsa";

    private static final Properties pros = new Properties();

    public void init(){
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            pros.load(resolver.getResource("file:conf/fabric.config").getInputStream());
            mmserver = pros.getProperty("mmserver");
            if (mmserver == null || mmserver.equals("")) {
                throw new Exception("没有找到mmserver服务器地址");
            }
            EncryptionType = pros.getProperty("encryptionType");
        } catch (Exception e) {
            logger.error("mmserver 配置出错，请检查配置文件相关信息", e);
        }
    }

    public static void mmInit(String s){
        mmserver=s;
    }

    //解密
    public static String decrypt(String data, String tp, String rid) {
        String ret = "";
        if (mmserver.equals("")) {
            return "error: No configuration MM ";
        }
        String url = "";
        String postdata = "";
        switch (tp) {
            case "sm2":
                url = mmserver + sm2_d;
                break;
            case "sm4":
                url = mmserver + sm4_d;
                break;
            case "rsa":
                url = mmserver + rsa_d;
                break;
            default:
                return "error: There is no such encryption method";
        }
        postdata = "{\"ciphertext\": \"" + data + "\",\"request_id\": \"" + rid + "\"}";
        String json = sendPost(url, postdata);
        boolean stf = false;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            stf = (boolean) jsonObject.get("success");
            ret = (String) jsonObject.get("data");
        } catch (Exception e) {
        }
        if (!stf) {
            return "error: Encryption failed";
        }
        ret = decodeHexString(ret);
        return ret;
    }

    //加密
    public static String crypt(String data, String tp, String rid) {
        String ret = "";
        if (mmserver.equals("")) {
            return "error: No configuration MM ";
        }
        String url = "";
        String postdata = "";
        switch (tp) {
            case "sm2":
                url = mmserver + sm2_c;
                break;
            case "sm4":
                url = mmserver + sm4_c;
                break;
            case "rsa":
                url = mmserver + rsa_c;
                break;
            default:
                return "error: There is no such encryption method";
        }
        String hex = encodeHexString(data);
        postdata = "{\"plaintext\": \"" + hex + "\",\"request_id\": \"" + rid + "\"}";
        String json = sendPost(url, postdata);

        boolean stf = false;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            stf = (boolean) jsonObject.get("success");
            ret = (String) jsonObject.get("data");
        } catch (Exception e) {
        }
        if (!stf) {
            return "error: Encryption failed";
        }
        return ret;
    }

    static String encodeHexString(String str) {
        byte[] byteArray=str.getBytes();
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    static String decodeHexString(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hexString.substring(index, index + 2), 16);
            byteArray[i] = (byte) j;
        }
        String str=new String(byteArray);
        return str;
    }


    static String sendPost(String url, String param) {
        String strRet = "";
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder("");
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            // conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            // 发送请求参数
            out.write(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            strRet = result.toString();
        } catch (Exception e) {
            strRet = "error: Request encryption error";
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
            }
        }
        return strRet;
    }


}