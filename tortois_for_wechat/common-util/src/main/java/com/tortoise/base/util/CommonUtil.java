package com.tortoise.base.util;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * Created by tortoise on 16/11/9.
 */
public class CommonUtil {
    public static ApplicationContext APPLICATION_CONTEXT = null;

    public static ResourceBundle rsb = ResourceBundle.getBundle("tortoise");

    public static ThreadPoolExecutor threadPool;

    // spring application context str
    public static final String 							APPLICATION_CONTEXT_STR = "applicationContext";


    // logger
    private static Logger log = Logger.getLogger(CommonUtil.class);

    static {

    }

    /**
     * get UUID string
     *
     * @return UUID string
     */
    public static String getUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * get other configure values
     *
     * @param configKey config key
     * @return value of configure
     */
    public static String getConfigValue(String configKey){
        return rsb.getString(configKey);
    }

    /**
     * getSingleQuotesStr
     *
     * @param origin origin
     *
     * @return single with quote string
     */
    public static String getSingleQuotesStr(String origin){
        String results = "";

        if(origin == null || origin.trim().length() == 0){
            return "";
        }

        if(origin.indexOf(",") < 0){
            return "'" + origin.trim() + "'";
        }

        String[] temp = origin.split(",");

        for(int i = 0; temp != null && i < temp.length; i++){
            if(temp[i] == null || temp[i].trim().length() == 0){
                continue;
            }

            if(results.trim().length() > 0){
                results += ",";
            }

            results += "'" + temp[i].trim() + "'";;
        }

        return results;
    }

    /**
     * getWebRootPath
     *
     * @return root path
     *
     */
    public static String getWebRootPath(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String rootPath = request.getSession().getServletContext().getRealPath("/");

        return rootPath;
    }

    /**
     * getWebDirPath
     *
     * @param path file path
     * @return WebDirPath
     *
     */
    public static String getWebDirPath(String path){
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String dirPath = request.getSession().getServletContext().getRealPath(path);

        return dirPath;
    }

    /**
     * create int code
     * @param length
     * @return
     */
    public static String getIntCode(int length){
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for(int i = 0; i < length; i++){
            sb.append((random.nextInt()%10+10)%10);
            //sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
        return sb.toString();
    }

    /**
     * create code by size
     *
     * @param size
     *
     * @return random string
     */
    public static String getCode(int size){
        String str="abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sf=new StringBuffer();
        for(int i=0;i<size;i++){
            sf.append(str.charAt(random.nextInt(62)));
        }
        return sf.toString();
    }

    /**
     * the file stream is encrypted and written to the destination file
     *
     * @param in InputStream
     * @param file file
     * @param key key, default 123456
     */
    public static void fileEncrypt(InputStream in, File file, String key){
        log.debug("[fileEncrypt] --- start --- the file stream is encrypted and written to the destination file");
        if(in == null){
            log.debug("[fileEncrypt] InputStream in is null");
            return;
        }
        if(file == null){
            log.debug("[fileEncrypt] file file  is null");
            return;
        }
        if(key == null || key.trim().length() == 0){
            log.debug("[fileEncrypt] password is null , set  default : 123456");
            key = "123456";
        }

        Cipher cipher = null;
        OutputStream out = null;
        CipherInputStream cis = null;

        try{
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, CommonUtil.getKey(key));
            out = new FileOutputStream(file);
            cis = new CipherInputStream(in, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
        }catch(Exception e) {
            e.printStackTrace();
            log.error("[fileEncrypt] exception to writte the destination,exception msg: " + e.toString());
        }finally{
            try {
                if(cis != null) cis.close();
                if(in != null) in.close();
                if(out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("[fileEncrypt] exception to colse, exception msg: " + e.toString());
            }
        }

        log.debug("[fileEncrypt] --- finish --- ");
    }

    /**
     * create key
     */
    private static SecretKey getKey(String strKey) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("DES");
            generator.init(new SecureRandom(strKey.getBytes()));
            return generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * judge null number
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        if(str == null){
            return true;
        }
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    /**
     * @throws ScriptException
     * @Title: formulaComputer
     * @author youzm  
     * @Description:
     * @param formula
     * @return Object
     * @throws
     */
    public static Object formulaComputer(String formula) throws ScriptException{
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = engine.eval(formula);

        System.out.println("result type：" + result.getClass().getName() + ",compute type:" + result);
        return result;
    }

    public static BigDecimal getBigDecimal( Object value ) {
        BigDecimal ret = null;
        if( value != null ) {
            if( value instanceof BigDecimal ) {
                ret = (BigDecimal) value;
            } else if( value instanceof String ) {
                ret = new BigDecimal( (String) value );
            } else if( value instanceof BigInteger) {
                ret = new BigDecimal( (BigInteger) value );
            } else if( value instanceof Number ) {
                ret = new BigDecimal( ((Number)value).doubleValue() );
            } else {
                throw new ClassCastException("Not possible to coerce ["+value+"] from class "+value.getClass()+" into a BigDecimal.");
            }
        }
        return ret;
    }

    public static boolean isRequestFromWexin(String userAgent) {
        if (userAgent == null){
            return false;
        }
        String ua = userAgent.toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            return true;
        } else {
            return false;
        }
    }

}
