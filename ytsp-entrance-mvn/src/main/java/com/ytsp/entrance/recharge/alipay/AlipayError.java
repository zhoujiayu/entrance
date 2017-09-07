/* 
 * $Id: AlipayError.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay;

public enum AlipayError {

    ERROR_0000("0000", "系统异常"),
    ERROR_0001("0001", "缺少必要的参数，检查非空参数是否已经传递"),
    ERROR_0002("0002", "签名错误，检查签名的参数是否符合支付宝签名规范"),
    ERROR_0003("0003", "服务接口错误，检查service是否传递正确"),
    ERROR_0004("0004", "req_data格式不正确"),
    ERROR_0005("0005", "合作伙伴没有开通接口访问权限，合同是否有效"),
    ERROR_0006("0006", "sec_id不正确，支持0001，MD5"),
    ERROR_0007("0007", "缺少了非空的业务参数"),
    ILLEGAL_SIGN("ILLEGAL_SIGN", "签名错诨，检查签名的数据是否符合支付宝签名规范"),
    ILLEGAL_SERVICE("ILLEGAL_SERVICE", "接口丌存在，检查service是否传递正确"),
    ILLEGAL_PARTNER("ILLEGAL_PARTNER", "无效商户，检查传入的PARTNER值是否正确"),
    ILLEGAL_PARTNER_EXTERFACE("ILLEGAL_PARTNER_EXTERFACE", "商户接口不存在，该商户没有开通该接口"),
    HAS_NO_PRIVILEGE("HAS_NO_PRIVILEGE", "无权访问该接口"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常"),
    PAY_CHANNEL_SIGN_ERROR("PAY_CHANNEL_SIGN_ERROR", "支付前置验签名失败"), 
    UNKNOW_ERROR("UNKNOW", "未知错误");

    private static final AlipayError[] VALUES = new AlipayError[]{
        ERROR_0000, ERROR_0001, ERROR_0002, ERROR_0003, ERROR_0004, ERROR_0005, ERROR_0006, ERROR_0007, 
        ILLEGAL_SIGN, ILLEGAL_SERVICE, ILLEGAL_PARTNER, ILLEGAL_PARTNER_EXTERFACE, HAS_NO_PRIVILEGE, SYSTEM_ERROR
    };

    /**
     * @return null if no matches code
     */
    public static AlipayError getAlipayError(String code) {
        for (AlipayError er : VALUES) {
            if (er.getCode().equals(code)) {
                return er;
            }
        }
//        throw new IllegalArgumentException("UNKNOW ERROR CODE: " + code);
        return null;
    }

    private AlipayError(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;
    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
