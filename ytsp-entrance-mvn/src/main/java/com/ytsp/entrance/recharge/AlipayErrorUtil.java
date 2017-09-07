/* 
 * $Id: AlipayErrorUtil.java 1491 2011-10-10 09:58:29Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

import com.ytsp.db.enums.ChargingStatusEnum;
import com.ytsp.entrance.recharge.alipay.AlipayError;

public class AlipayErrorUtil {

    private AlipayErrorUtil() {
    }
/*
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
    
    
        SUCCESS(0,"成功"), 
    FAIL(1,"失败"), 
    USED_ERROR(2,"点卡已使用"), 
    EXPIRE_ERROR(3,"点卡已过期"), 
    CARD_NAME_ERROR(4,"点卡卡号错误"), 
    CARD_PWD_ERROR(5,"点卡密码错误"), 
    ALIPAY_ACCOUNT_BALANCE_ERROR(6,"支付宝余额不足"), 
    ALIPAY_ACCOUNT_ERROR(7,"支付宝账户密码错误"), 
    TIMEOUT_ERROR(8,"请求超时"), 
    WAIT_BUYER_PAY(9,"等待买家付款");
*/    
    public ChargingStatusEnum transform(AlipayError ae) {
        
        return null;
    }

}
