package com.ytsp.entrance.recharge.alipay.security;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

import org.apache.commons.codec.digest.DigestUtils;

import com.ytsp.common.util.StringUtil;

public class MD5Signature {

    public static String sign(String content, String key) throws Exception {
        String tosign = (content == null ? "" : content) + key;
        try {
            return DigestUtils.md5Hex(getContentBytes(tosign, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SignatureException("MD5签名[content = " + content + "; charset = utf-8" + "]发生异常!", e);
        }
    }

    public static boolean verify(String content, String sign, String key) throws Exception {
        return verify(content, sign, key, "UTF-8");
    }

    public static boolean verify(String content, String sign, String key, String charset) throws Exception {
        String tosign = (content == null ? "" : content) + key;
        try {
            String mySign = DigestUtils.md5Hex(getContentBytes(tosign, charset));
            return StringUtil.equalsNullSafe(mySign, sign) ? true : false;
        } catch (UnsupportedEncodingException e) {
            throw new SignatureException("MD5验证签名[content = " + content + "; charset = " + charset + "; signature = " + sign + "]发生异常!", e);
        }
    }

    public static byte[] getContentBytes(String content, String charset) throws UnsupportedEncodingException {
        if (StringUtil.isNullOrEmpty(content)) {
            throw new IllegalArgumentException("null content");
        }
        if (StringUtil.isNullOrEmpty(charset)) {
            return content.getBytes();
        }

        return content.getBytes(charset);
    }

}
