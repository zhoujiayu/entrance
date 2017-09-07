package com.ytsp.entrance.recharge.alipay.util;

import java.io.InputStream;
import java.util.List;

import org.nuxeo.common.xmap.XMap;

public class XMapUtil {

    private static final XMap xmap;

    static {
        xmap = new XMap();
    }

    public static void register(Class<?> clazz) {
        if (clazz != null) {
            xmap.register(clazz);
        }
    }

    public static Object load(InputStream is) throws Exception {
        Object obj = null;
        try {
            obj = xmap.load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return obj;
    }

    public static String asXml(Object obj, String encoding, List<String> outputsFields) throws Exception {

        return xmap.asXmlString(obj, encoding, outputsFields);
    }
}
