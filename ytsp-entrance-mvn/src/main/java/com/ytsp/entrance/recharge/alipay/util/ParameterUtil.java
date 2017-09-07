package com.ytsp.entrance.recharge.alipay.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.protocol.HTTP;

import com.ytsp.common.util.StringUtil;

public class ParameterUtil {

    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    private static String encode(final String content, final String encoding) {
        try {
            return URLEncoder.encode(content, encoding != null ? encoding : HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String formatQuerys(final Map<String, String> params, final String encoding) {
        final StringBuilder result = new StringBuilder();
        for (final Entry<String, String> parameter : params.entrySet()) {
            final String encodedName = encode(parameter.getKey(), encoding);
            final String value = parameter.getValue();
            final String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }
            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        return result.toString();
    }

    /**
     * 将Map组装成待签名数据。 待签名的数据必须按照一定的顺序排列 这个是支付宝提供的服务的规范，否则调用支付宝的服务会通不过签名验证
     */
    public static String getSignData(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("null params");
        }

        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if ("sign".equals(key) || "sign_type".equals(key)) {
                continue;
            }
            String value = (String) params.get(key);
            if (value != null) {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            } else {
                content.append((i == 0 ? "" : "&") + key + "=");
            }

        }

        return content.toString();
    }

    /**
     * 将Map中的数据组装成url
     * @throws UnsupportedEncodingException
     */
    public static String mapToUrl(final Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null) {
            throw new IllegalArgumentException("null params");
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (isFirst) {
                sb.append(key + "=" + URLEncoder.encode(value, "utf-8"));
                isFirst = false;
            } else {
                if (value != null) {
                    sb.append("&" + key + "=" + URLEncoder.encode(value, "utf-8"));
                } else {
                    sb.append("&" + key + "=");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 取得URL中的参数值。
     * <p>
     * 如不存在，返回空值。
     * </p>
     */
    public static String getParameter(String url, String name) {
        if (StringUtil.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("null url");
        }
        if (name == null || name.equals("")) {
            return null;
        }
        name = name + "=";
        int start = url.indexOf(name);
        if (start < 0) {
            return null;
        }
        start += name.length();
        int end = url.indexOf("&", start);
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start, end);
    }
}
