package com.ytsp.entrance.errorcode;

public class ErrorCode {

	
	//接口掉用返回状态
	public static final int RESPONSE_STATUS_OK = 200;
	public static final int RESPONSE_STATUS_FAIL = 500;
	

	public static final String RESP_CODE_OK = "00000";
	public static final String RESP_INFO_OK = "执行成功";
	public static final String RESP_CODE_NETWORK_ERR = "00001";
	public static final String RESP_INFO_NETWORK_ERR = "服务器内部异常";
	
	//手机登录注册相关
	public static final String RESP_CODE_REGIST_PHONE_EMPTY = "00011";
	public static final String RESP_INFO_REGIST_PHONE_EMPTY = "请填写手机号";
	
	public static final String RESP_CODE_REGIST_PHONE_ERR = "00012";
	public static final String RESP_INFO_REGIST_PHONE_ERR = "手机号格式错误";	
	
	public static final String RESP_CODE_REGIST_PHONE_VALIDATE = "00013";
	public static final String RESP_INFO_REGIST_PHONE_VALIDATE = "该手机号已被验证，请直接登录或者重新填写手机号";
	
	public static final String RESP_CODE_INPUT_PHONE = "00014";
	public static final String RESP_INFO_INPUT_PHONE = "请填写手机号和验证码";
	
	public static final String RESP_CODE_INPUT_CODE = "00015";
	public static final String RESP_INFO_INPUT_CODE = "请填写验证码";
	
	public static final String RESP_CODE_INPUT_IMAGE_CODE = "00016";
	public static final String RESP_INFO_INPUT_IMAGE_CODE = "请填写图形验证码";
	
	public static final String RESP_CODE_PHONE_LOGIN = "00017";
	public static final String RESP_INFO_PHONE_LOGIN  = "登录成功";
	
	public static final String RESP_CODE_LINK_CUSTOMER_INFO = "00018";
	public static final String RESP_INFO_LINK_CUSTOMER_INFO  = "关联用户信息";
	
	public static final String RESP_CODE_UPDATE_CUSTOMER = "00019";
	public static final String RESP_INFO_UPDATE_CUSTOMER  = "修改用户名成功";
	
	public static final String RESP_CODE_CUSTOMER_EXIST = "00020";
	public static final String RESP_INFO_CUSTOMER_EXIST  = "用户名已存在";
	
	public static final String RESP_CODE_SEND_SMS_TIME_LIMIT = "00021";
	public static final String RESP_INFO_SEND_SMS_TIME_LIMIT  = "离上次获取短信验证码未超过90秒";
	
	public static final String RESP_CODE_PHONE_VERIFY_CODE_ERROR = "00022";
	public static final String RESP_INFO_PHONE_VERIFY_CODE_ERROR  = "验证码错误";
	
	public static final String RESP_CODE_VALIDATE_COUNT_ERROR = "00023";
	public static final String RESP_INFO_VALIDATE_COUNT_ERROR  = "验证次数超3次";
	
	
	public static final String RESP_CODE_PWD_RECOVER_PHONE_NOT_VALIDATE = "00024";
	public static final String RESP_INFO_PWD_RECOVER_PHONE_NOT_VALIDATE  = "密码找回手机未验证";
	
	public static final String RESP_CODE_EXIST_PWD = "00025";
	public static final String RESP_INFO_EXIST_PWD = "有密码";
	
	public static final String RESP_CODE_NOT_EXIST_PWD = "00026";
	public static final String RESP_INFO_NOT_EXIST_PWD = "没有密码";
	
	public static final String RESP_CODE_IMAGE_CODE_ERROR = "00027";
	public static final String RESP_INFO_IMAGE_CODE_ERROR  = "图片验证码错误";
	
	public static final String RESP_CODE_PHONE_NOT_VALIDATED_ERROR = "00028";
	public static final String RESP_INFO_PHONE_NOT_VALIDATED_ERROR  = "该手机号未验证,请重新输入！";
	
	
	public static final String RESP_CODE_PWD_RECOVER_EMAIL_NOT_VALIDATE = "00029";
	public static final String RESP_INFO_PWD_RECOVER_EMAIL_NOT_VALIDATE  = "该邮箱未验证,请重新输入！";
	
	public static final String RESP_CODE_SEND_EMAIL = "00030";
	public static final String RESP_INFO_SEND_EMAIL = "邮件已发送成功,请查收您的邮箱";
	
	public static final String RESP_CODE_PWD_ACCOUNT_ERROR = "00031";
	public static final String RESP_INFO_PWD_ACCOUNT_ERROR = "用户不存在或密码错误";
	
	public static final String RESP_CODE_EMAIL_SEND_SMS_TIME_LIMIT = "00032";
	public static final String RESP_INFO_EMAIL_SEND_SMS_TIME_LIMIT  = "离上次发送验证码未超过90秒";
	
	
	public static final String RESP_CODE_GET_IMG_CODE = "00033";
	public static final String RESP_INFO_GET_IMG_CODE  = "获取图形验证码";
	
	public static final String RESP_CODE_EMAIL_CODE_ERROR = "00034";
	public static final String RESP_INFO_EMAIL_CODE_ERROR  = "验证码错误";
	
	public static final String RESP_CODE_BINDING= "00035";
	public static final String RESP_INFO_BINDING  = "进入绑定手机页面";
	
	public static final String RESP_CODE_PHONE_IS_VALIDATE_ERROR = "00036";
	public static final String RESP_INFO_PHONE_IS_VALIDATE_ERROR  = "手机号被占用";
	
	public static final String RESP_CODE_PASSWORD_ERROR = "00037";
	public static final String RESP_INFO_PASSWORD_ERROR  = "密码错误";
	
	public static final String RESP_CODE_PHONE_CODE_INVALID = "00038";
	public static final String RESP_INFO_PHONE_CODE_INVALID  = "验证码已失效，请重新获取！";
	
	public static final String RESP_CODE_OBTAIN_WX_USERINFO_ERROR = "00039";
	public static final String RESP_INFO_OBTAIN_WX_USERINFO_ERROR  = "根据code获取微信用户信息错误";
	
	//手机发送短信
	public static final String RESP_CODE_SEND_PHONE_CODE_ERR = "00101";
	public static final String RESP_INFO_SEND_PHONE_CODE_ERR = "验证操作过于频繁，请稍后再操作";
	
	
	
	
	
	
}
