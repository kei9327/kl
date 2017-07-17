package com.knowlounge.util;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class AESUtil {

    //private String algorithm = "AES";
    //private String transformation = "AES/CBC/PKCS5Padding";
    private SecretKeySpec key;
    private IvParameterSpec initialVector;
    private Cipher cipher;
    private String charset = "utf8";
    private boolean isHex = false;

    public static final String KEY = "fboardeoqkr!@#!!";
    public static final String VECTOR = "fboardEoqkRtkn%!";
    public static final String CHARSET = "UTF-8";

    public AESUtil(String key, String vector, String charset) throws Exception {
        initCipher(key, vector, charset, true);
    }

    public AESUtil(String key, String vector, String charset, boolean isHex) throws Exception {
        initCipher(key, vector, charset, isHex);
    }

    private void initCipher(String key, String vector, String charset, boolean isHex) throws Exception {
        if(key == null || "".equals(key) || vector == null || "".equals(vector)) {
            throw new Exception("initialize fail");
        }
        if(charset != null && !charset.trim().equals("")) {
            this.charset = charset;
        }
        this.isHex = isHex;

        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        if(isHex) {
            this.key = new SecretKeySpec(key.getBytes(this.charset), "AES");
            this.initialVector = new IvParameterSpec(vector.getBytes(this.charset));
        } else {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            this.key = new SecretKeySpec(md5.digest(key.getBytes(this.charset)), "AES");
            this.initialVector = new IvParameterSpec(md5.digest(vector.getBytes(this.charset)));
        }
    }

    public String encrypt(String txt) throws Exception {
        String strResult = "";

        cipher.init(Cipher.ENCRYPT_MODE, this.key, this.initialVector);

        byte[] encrypted = cipher.doFinal(txt.getBytes(this.charset));

        if(this.isHex) {
            strResult = asHex(encrypted);
        } else {
            strResult = Base64.encode(encrypted);
        }

        return strResult;
    }

    public String decrypt(String txt) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, this.key, this.initialVector);

        byte[] encrypted;
        if(this.isHex) {
            encrypted = fromHex(txt);
        } else {
            encrypted = Base64.decode(txt);
        }
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, this.charset);
    }

    public String asHex (byte buf[]) {
        int bufLen = (buf==null)?0:buf.length;

        if(bufLen == 0) {
            return null;
        }
        StringBuffer strbuf = new StringBuffer(bufLen * 2);
        String hexNum;
        for(int i=0; i<bufLen; i++) {
            hexNum = "0" + Integer.toHexString(0xff & buf[i]);

            strbuf.append(hexNum.substring(hexNum.length() - 2));
        }
        return strbuf.toString();
    }

    public byte[] fromHex(String strHex) {
        int hexLen = (strHex==null)?0:strHex.length();

        if(hexLen == 0) {
            return null;
        }
        byte[] bytes = new byte[hexLen / 2];
        for(int i=0; i<bytes.length; i++) {
            bytes[i] = (byte)Integer.parseInt(strHex.substring(2*i, 2*i+2), 16);
        }
        return bytes;
    }

}
