package com.ytsp.entrance.command.v5_0;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.icu.util.Calendar;
import com.statistics.enums.LoginStatusEnum;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.CustomerLoginRecordDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.db.domain.CustomerThirdPlatform;
import com.ytsp.db.domain.CustomerValidateCount;
import com.ytsp.db.domain.ForgetPasswordCode;
import com.ytsp.db.domain.ImageVerify;
import com.ytsp.db.enums.CustomerValidateCountTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.ValidateTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.errorcode.ErrorCode;
import com.ytsp.entrance.handleResponse.RestResponse;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.EbShoppingCartService;
import com.ytsp.entrance.service.ImageVerifyService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.AesEncrypt;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.ValidateUtil;


public class LoginCommandv5_0 extends AbstractCommand {
	
	//短信校验时限
	private static final int VALIDATETIME = 10;                              
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_NEW_LOGIN_5_0 == code||
			   CommandList.CMD_VALIDATE_PHONE == code||
			   CommandList.CMD_PHONE_QUICK_LOGIN == code||
			   CommandList.CMD_PHONE_VALIDATE_REGIST ==code ||
			   CommandList.CMD_PHONE_QUICK_REGIST == code ||
			   CommandList.CMD_CUSTOMER_SUPPLY_PWD == code ||
			   CommandList.CMD_RELATE_CUSTOMER_ACCOUNT == code ||
			   CommandList.CMD_CUSTOMER_ACCOUNT_CHECK ==code ||
			   CommandList.CMD_CUSTOMER_ACCOUNT_UPDATE ==code ||
			   CommandList.CMD_QUICK_ADD_CUSTOMER == code	||
			   CommandList.CMD_LOGIN_CHECK_THIRD_PLATFORM_USER ==code ||
			   CommandList.CMD_COMMON_LOGIN ==code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			// 验证权限.
			int code = getContext().getHead().getCommandCode();
			if(CommandList.CMD_PHONE_VALIDATE_REGIST == code){
				return validateMoblieRegist ();//手机注册前验证
			}else if(CommandList.CMD_PHONE_QUICK_REGIST == code){
				return moblieRegist();
			}else if(CommandList.CMD_PHONE_QUICK_LOGIN == code){
				return moblidLogin();
			}else if (CommandList.CMD_NEW_LOGIN_5_0 == code) {//新版登录
				return login5_0();
			}else if(CommandList.CMD_RELATE_CUSTOMER_ACCOUNT == code){//关联账号
				return  relateAccount();
			}else if(CommandList.CMD_QUICK_ADD_CUSTOMER == code){//新增用户
				return addCustomer();
			}else if(CommandList.CMD_LOGIN_CHECK_THIRD_PLATFORM_USER ==code){
				return checkThirdPlatUser();
			}else if(CommandList.CMD_COMMON_LOGIN ==code){
				return  commonLogin();
			}
			
			int userId = getContext().getHead().getUid();
			SessionCustomer sc = getSessionCustomer();
			if (sc == null || sc.getCustomer() == null) {
				return getNoPermissionExecuteResult();
			}
			// 判断操作的用户与当前的session中用户是否一致.
			Customer customer = sc.getCustomer();
			if (userId == 0 || customer.getId().intValue() != userId) {
				return getNoPermissionExecuteResult();
			}
			
			if(CommandList.CMD_VALIDATE_PHONE == code){//验证手机
				return validateMobile(userId);
			}else if(CommandList.CMD_CUSTOMER_SUPPLY_PWD == code){//用户补充密码
				return supplyPwd(userId);
			}else if(CommandList.CMD_CUSTOMER_ACCOUNT_UPDATE ==code){//用户修改账号
				return updateAccount(userId);
			}
			
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	
	/**
	 * 普通新版登录
	 */
	private ExecuteResult commonLogin(){
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (jsonObj.isNull("account") || jsonObj.isNull("password")) {
				logger.error("account or password not exist : " + jsonObj);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请填写用户名和密码！", null, this);
			}
			String account = StringUtils.trim(jsonObj.getString("account"));
			String password = StringUtils.trim(jsonObj.getString("password"));
			
			
			if (StringUtil.isNullOrEmpty(account)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请填写用户名！", null, this);
			}

			if (StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请填写密码！", null, this);
			}
			CustomerService cs = SystemInitialization.getApplicationContext()
					.getBean(CustomerService.class);
			boolean exist = cs.existByAccount(account);
			if (!exist) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"用户不存在或密码错误", null, this);
			}
			final Customer customer = cs.findCustomerByAccountAndPassword(account,
					password.trim());

			if (customer == null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"用户不存在或密码错误", null, this);
			}
			JSONObject obj  = new JSONObject();
			String newAccount = "";//新账号
			if(null!=customer.getPhoneValidate()){
				if(1==customer.getPhoneValidate()){
					if(ValidateUtil.isEmail(customer.getAccount()) || ValidateUtil.isMoblie(customer.getAccount())){
						if(customer.getAccount().equals(customer.getMobilephone())){
							obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
						}else{
							obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());//进入重要信息提示页面
						}
						newAccount = Util.getCustomerAccount(customer.getMobilephone());
						obj.put("oldAccount", customer.getAccount());
						customer.setAccount(newAccount);
						obj.put("account", newAccount);
					}else{
						obj.put("account",customer.getAccount());
						obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
					}
				}else{
					obj.put("loginStatus", LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
				}
			}else{
				obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
			}
			
			if(!"".equals(newAccount)){
				cs.updateAccountByUserId(customer.getId(), newAccount);
			}
			
			if(obj.getInt("loginStatus")==2){
				obj.put("retCode", ErrorCode.RESP_CODE_BINDING);
				obj.put("retInfo", ErrorCode.RESP_INFO_BINDING);
				obj.put("uid", customer.getId());
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_BINDING, obj, this);
			}
			
			obj.put("phone", customer.getMobilephone());
			obj = getCustomerInfo(customer,cs,obj);
			if(obj.has("oldAccount")){
				obj.put("oldAccount", obj.get("oldAccount"));
			}
			obj.put("retCode", ErrorCode.RESP_CODE_PHONE_LOGIN);
			obj.put("retInfo", ErrorCode.RESP_INFO_PHONE_LOGIN);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"成功返回用户信息!", obj, this);
			
		} catch (Exception e) {
			logger.error("commonLogin() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:APP端和移动端网站，校验第三方登录用户是否存在</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkThirdPlatUser(){
		try {
			String key = (String) getContext().getRequest().getSession(true)
					.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
			if (key == null)
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
						null, this);
			JSONObject jsonObj = null;
			
			if(MobileTypeEnum.wapmobile == MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				jsonObj = getContext().getBody().getBodyObject();
			}else{
				jsonObj = new JSONObject(AesEncrypt.decrypt(key,
						getContext().getBody().getBodyObject().get("thirdPartJson")
						.toString()));
			}
			
			if (jsonObj.isNull("token") || jsonObj.isNull("platformName")
					|| jsonObj.isNull("uid")) {
				logger.error("平台用户授权信息不全 : " + jsonObj);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请先用第三方平台登录授权！", null, this);
			}
			Map<String,Object> result = new HashMap<String, Object>();
			String platformName = StringUtils.trim(jsonObj
					.optString("platformName"));
			String userId = StringUtils.trim(jsonObj.optString("uid"));
			//移动端网站传的平台类型为：1：微博，6：QQ 997：微信
			if(MobileTypeEnum.wapmobile != MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				platformName = getThirdPlatFormType(platformName);
			}
			String token = StringUtils.trim(jsonObj.optString("token"));
			
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			CustomerService cs = SystemInitialization.getApplicationContext()
					.getBean(CustomerService.class);

			/*
			 * 1老版本与新版本区分  unionId 是否有这个key 
			 */
			CustomerThirdPlatform customerTP = null;
			if("997".equals(platformName)&&jsonObj.has("unionId")){//微信登陆 新版 并且传递unionId
				CustomerThirdPlatform tp = cs.findCustomerThirdPlatformByUninoId(StringUtils.trim(jsonObj.optString("unionId")));
				if (tp == null) {
					customerTP = cs.findCustomerByThirdPlatform(platformName,
							userId);
				} else {
					customerTP = tp;
				}
			}else{
				customerTP = cs.findCustomerByThirdPlatform(platformName,
						userId);
			}
			//第三方用户信息是否存在
			boolean userExist = (customerTP == null || customerTP.getCustomer() == null) ? false
					: true;
			result.put("userExist", userExist);
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			if(userExist){
				Customer cust = customerTP.getCustomer();
				int isValidate = 0;
				//获取手机号验证状态
				if(StringUtil.isNotNullNotEmpty(cust.getMobilephone())){
					isValidate = custServ.getPhoneValidateStatus(cust.getMobilephone(), cust.getId());
				}
				result.put("validateStatus", isValidate);
				result.put("uid", cust.getId());
				result.put("thirdPlatId", customerTP.getId());
				//若手机号已验证，直接返回登录信息
				if(isValidate == 1){
					custServ.checkAccount(cust.getId(), result);
					//获取用户信息
					getCustomerInfo(cust, cs, result);
					result.put("phone", cust.getMobilephone());
					respone.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
					respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
				}
			}
			//若为移动端网站，QQ登录获取用户信息，昵称和头像
			if (MobileTypeEnum.wapmobile == MobileTypeEnum.valueOf(getContext()
					.getHead().getPlatform()) && platformName.equals("6")) {
				JSONObject qqUserInfo = Util.getQQUserInfo(IConstants.QQ_APP_ID, userId, token);
				if(qqUserInfo != null && qqUserInfo.getInt("ret") == 0){
					result.put("figureurl", qqUserInfo.optString("figureurl_qq_2"));
				}
			}
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取第三方登录用户成功", respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("checkPhoneValidateCode() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}
	
	/**
	* <p>功能描述:获取第三方登录类型</p>
	* <p>参数：@param platformName
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String getThirdPlatFormType(String platformName){
		String platform = "";
		if (platformName.toLowerCase().equals("sinaweibo")) {
			platform = "1";
		} else if (platformName.toLowerCase().equals("qzone")
				|| platformName.toLowerCase().equals("qq")) {
			platform = "6";
		} else if (platformName.toLowerCase().equals("wechat")) {
			platform = "997";
		}
		return platform;
	}
	
	/**
	 * 新增用户  
	 */
	private ExecuteResult addCustomer(){
		JSONObject reqBody = getContext().getBody().getBodyObject();
		HeadInfo  headInfo= getContext().getHead();
		try {
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.optString("phone")) ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号不能为空", null, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.optString("validateNum",""))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",null, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",null, this);
			}
			
			int type = reqBody.getInt("type");
			String phone = reqBody.optString("phone");
			String validateNum = reqBody.optString("validateNum");
			String password = "";
			if(ValidateTypeEnum.PHONEREGIST.getValue()==type){
				if(reqBody.isNull("password") || StringUtil.isNullOrEmpty(reqBody.optString("password"))){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "密码不能为空！",null, this);
				}
				password = reqBody.optString("password");
			}
			if(!ValidateUtil.isMoblie(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机格式不正确", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			//校验手机号是否已被验证过：2个手机先后操作关联会有问题，所以这里做一下校验
			int validateStatus = custServ.getPhoneValidateStatus(phone, 0);
			if(validateStatus == 1 || validateStatus == 2){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号已注册，请直接登录", null, this);
			}
			
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, 0, 1, validateNum, type);
			if(null==custValid){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"该手机未通过手机验证码验证", null, this);

			}
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> map =  new HashMap<String, Object>();
			String hardwareId = getContext().getHead().getUniqueId();
			String otherInfo = getContext().getHead().getOtherInfo();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();
			Customer customer = new Customer();
			customer.setCreateTime(new Date());
			customer.setRegisterIp(ip);
			customer.setTerminalType(platform);
			customer.setTerminalVersion(version);
			customer.setTerminalNumber(hardwareId);
			String[] a = IPSeeker.getAreaNameByIp(ip);
			customer.setRegisterProvince(a[0]);
			customer.setRegisterCity(a[1]);
			customer.setMobilephone(phone);
			customer.setPhoneValidate(1);
			customer.setAccount(Util.getCustomerAccount(phone));
			if(!"".equals(password)){
				customer.setPassword(password);
			}
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			cs.addCustomer(customer, otherInfo, appDiv);//新增用户 
			//合并购物车
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(EbShoppingCartService.class);
			String cartId = getContext().getHead().getCartId();
			shoppingCartService.updateShoppingCartByLogin(customer.getId(), cartId);
			SessionCustomer sc = new SessionCustomer(customer);
			String sessionId = sessionService.signIn(customer.getId());
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			map.put("sessionId", sessionId);
			map.put("uid", customer.getId());
			map.put("account", customer.getAccount());
			map.put("isMember", false);
			map.put("memberType", 0);
			map.put("userExist", false);
			map.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
			respone.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
			respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
			respone.setVo(map);
			Util.addStatistics(getContext(), customer);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功", respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("addCustomer() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
			
		}
	}
	
	
	
	
	/**
	 * 用户名修改
	 */
	private ExecuteResult updateAccount(int userId){
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			if(jsonObj.isNull("account") || StringUtil.isNullOrEmpty(jsonObj.optString("account"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "修改账号不能为空！", null, this);
			}
			String account =  jsonObj.optString("account");
			if(ValidateUtil.isEmail(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "修改账号不能为手机号！", null, this);
			}
			if(ValidateUtil.isEmail(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "修改账号不能为邮箱！", null, this);
			}
			if(!ValidateUtil.validateAccount(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账号格式不合法！", null, this);
			}
			if(cs.existAccount(account, userId)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_REGIST_ACCOUNT_EXIST, "该用户名已存在，请重新输入", null, this);
			}
			cs.updateAccountByUserId(userId, account);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "修改用户名成功！", null, this);

		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 用户名检查
	 */
//	private ExecuteResult checkAccount(int userId){
//		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
//		try {
//			Customer customer = cs.findCustomerById(userId);
//			if(null==customer){
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在", null, this);
//			}
//			RestResponse<Map<String,Object>> res = new RestResponse<Map<String,Object>>();
//			Map<String,Object> map = new HashMap<String, Object>();
//			JSONObject obj = null;
//			String oldAccount = "";
//			String newAccount = "";
//			obj = new JSONObject();
//			oldAccount = customer.getAccount();
//			newAccount = "ikan"+customer.getMobilephone();
//			if(ValidateUtil.isMoblie(customer.getAccount())){//如果用户名是手机号格式
//				
//				if(!customer.getAccount().equals(customer.getMobilephone())){//绑定手机号码与原账号名称一致
//					map.put("isRemand", true);//是否提示
//					map.put("account", newAccount);
//					map.put("oldAccount", customer.getAccount());
//					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
//				}
//			}else if(ValidateUtil.isEmail(customer.getAccount())){//如果账号是邮箱格式
//				if(1==customer.getEmailValidate()){
//					map.put("isRemand", true);//是否提示
//					map.put("account", newAccount);
//					map.put("oldAccount", customer.getAccount());
//					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
//				}
//			}else if(StringUtil.isNullOrEmpty(oldAccount)){
//				map.put("isRemand", true);//是否提示
//				map.put("oldAccount", customer.getAccount());
//			}else{
//				map.put("isRemand", false);//是否提示
//				map.put("oldAccount", customer.getAccount());
//				cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
//			}
//			res.setVo(map);
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "用户名校验通过", res.convertJSONObject(), this);
//			
//		} catch (Exception e) {
//			return getExceptionExecuteResult(e);
//		}
//	}
	
	/**
	 * 关联账号
	 */
	private ExecuteResult relateAccount(){
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject  result  = new JSONObject();
		try {
			
			if(null!=validateAccountAndPwd(jsonObj)){
				
				return validateAccountAndPwd(jsonObj);
			}
			
			if (StringUtil.isNullOrEmpty(jsonObj.optString("phone")) || jsonObj.isNull("phone") ) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号码不能为空！", null, this);
			}
			String phone = jsonObj.optString("phone").trim();//要关联的手机号
			String account = jsonObj.optString("account").trim();//关联的账号
			String password = jsonObj.optString("password").trim();//关联账号的密码
			String imageCode = jsonObj.optString("imageCode","");//获取图形验证码
			String ispPhone = "";
			if(ValidateUtil.isMoblie(account)){
				ispPhone = account;
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			if(StringUtil.isNotNullNotEmpty(imageCode)){
				if (!checkImageVerifyCode(imageCode, getContext().getHead()
						.getUniqueId())) {
					result.put("retCode", ErrorCode.RESP_CODE_IMAGE_CODE_ERROR);
					result.put("retInfo",  ErrorCode.RESP_INFO_IMAGE_CODE_ERROR);
					return new ExecuteResult(
							CommandList.RESPONSE_STATUS_OK, "图形验证码不正确",
							result, this);
				}else{//图形验证码验证成功 清空次数
					custServ.delBeyondCount(ispPhone, getContext());
				}
			}
			
			boolean flag = custServ.isBeyondCount(ispPhone, getContext());
			if(flag){
				result.put("retCode", ErrorCode.RESP_CODE_GET_IMG_CODE);
				result.put("retInfo",  ErrorCode.RESP_INFO_GET_IMG_CODE);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",result, this);
			}
	
			
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Customer customer = null;
			JSONObject obj  = new JSONObject();
			customer = cs.findCustomerByAccountAndPassword(account,password);
			if (customer == null) {
				custServ.saveOrUpdateCount(ispPhone, getContext());//更新次数
				result.put("retCode", ErrorCode.RESP_CODE_PASSWORD_ERROR);
				result.put("retInfo",  ErrorCode.RESP_INFO_PASSWORD_ERROR);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_PASSWORD_ERROR, result, this);
			}
			
			//校验手机号是否已被验证过：2个手机先后操作关联会有问题，所以这里做一下校验
			int validateStatus = custServ.getPhoneValidateStatus(phone, 0);
			if(validateStatus == 1 || validateStatus == 2){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号已注册，请直接登录", null, this);
			}
			
			cs.updateRelateAccount(customer.getId(),phone);
			obj = custServ.checkAccountReturnJson(customer.getId());
			obj = getCustomerInfo(customer,cs,obj);
			result.put("retCode", ErrorCode.RESP_CODE_OK);
			result.put("retInfo",  ErrorCode.RESP_INFO_OK);
			result.put("vo",obj);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"成功返回用户信息!", result, this);	
		} catch (Exception e) {
			
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 用户补充密码
	 */
	private ExecuteResult supplyPwd(int userId){
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		RestResponse<Boolean> res = new RestResponse<Boolean>();
		try {
			if(jsonObj.isNull("password") || StringUtil.isNullOrEmpty(jsonObj.optString("password"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"密码不能为空", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			Customer customer = custServ.getCustomerById(userId);
			if(customer == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"用户不存在！", null, this);
			}
			String pwd = jsonObj.optString("password").trim();
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			cs.updatePwd(userId, pwd);
			res.setRetCode(ErrorCode.RESP_CODE_OK);
			res.setRetInfo(ErrorCode.RESP_INFO_OK);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "补充密码成功", res.convertJSONObject(), this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);

		}
	}
	
	
	/**
	* <p>功能描述:构建验证次数</p>
	* <p>参数：@param account
	* <p>参数：@param count
	* <p>参数：@return</p>
	* <p>返回类型：CustomerValidateCount</p>
	 */
	private CustomerValidateCount buildCustomerValidateCount(String account,int count,int type){
		CustomerValidateCount valCount = new CustomerValidateCount();
		valCount.setAccount(account);
		valCount.setCreateDate(new Date());
		valCount.setValidateCount(count);
		valCount.setType(CustomerValidateCountTypeEnum.valueOf(type));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 2);
		valCount.setEndDate(cal.getTime());
		return valCount;
	}
	
	
	/**
	* <p>功能描述:获取手机验证次数，若有验证次数更新原有数量，若没有保存新的手机验证次数</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	private int getAndUpdateCustomerValidateCount(String phone,int type) throws SqlException{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		//获取手机2小时内获取验证码次数
		CustomerValidateCount validateCount = custServ.findCustomerValidateCountByPhone(phone,type);
		if(validateCount == null){
			CustomerValidateCount valCount = buildCustomerValidateCount(phone, 1,type);
			custServ.saveCustomerValidateCount(valCount);
			return valCount.getValidateCount();
		}
		validateCount.setValidateCount(validateCount.getValidateCount()+1);
		custServ.updateCustomerValidateCount(validateCount);
		return validateCount.getValidateCount(); 
	}
	
	
	/**
	 * 手机快速登录
	 * 1、先判断是否需要图形验证码
	 */
	private ExecuteResult moblidLogin(){
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		HeadInfo  headInfo= getContext().getHead();
		JSONObject obj  = new JSONObject();
		try {
			if(jsonObj.isNull("phone") || StringUtil.isNullOrEmpty(jsonObj.optString("phone")) ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号不能为空", null, this);
			}
			if(jsonObj.isNull("validateNum") || StringUtil.isNullOrEmpty(jsonObj.optString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",null, this);
			}
			String phone = jsonObj.optString("phone").trim();
			String validateNum = jsonObj.optString("validateNum").trim();
			String imageCode = jsonObj.optString("imageCode");//获取图形验证码
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> result = new HashMap<String, Object>();
			
			//满足 出图形验证码要求 需要验证图形验证码
			if(StringUtil.isNotNullNotEmpty(imageCode)){
				if (!checkImageVerifyCode(imageCode, getContext().getHead()
						.getUniqueId())) {
					respone.setRetCode(ErrorCode.RESP_CODE_IMAGE_CODE_ERROR);
					respone.setRetInfo(ErrorCode.RESP_INFO_IMAGE_CODE_ERROR);
					return new ExecuteResult(
							CommandList.RESPONSE_STATUS_OK, "图形验证码不正确",
							respone.convertJSONObject(), this);
				}else{//图形验证码验证成功 清空次数
					custServ.delBeyondCount(phone, getContext());
				}
			}
			
			boolean flag = custServ.isBeyondCount(phone, getContext());
			if(flag){
				respone.setRetCode(ErrorCode.RESP_CODE_GET_IMG_CODE);
				respone.setRetInfo(ErrorCode.RESP_INFO_GET_IMG_CODE);
				result.put("isShowImgCode", flag);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",respone.convertJSONObject(), this);
			}
			
			//验证码不正确 添加次数记录
			ForgetPasswordCode custValid = custServ.getCustomerPhoneCodeExist(phone,validateNum,ValidateTypeEnum.PHONELOGIN.getValue(), 0);
			if(custValid == null){
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_VERIFY_CODE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR);
				//记录验证码错误次数 修改
//				int errorCount = getAndUpdateCustomerValidateCount(phone,CustomerValidateCountTypeEnum.VALIDATECODE.getValue());
//				result.put("errorCount", errorCount);
				custServ.saveOrUpdateCount(phone, getContext());// 2016516 新加
//				result.put("isShowImgCode", custServ.isBeyondCount(phone,getContext()));
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR,respone.convertJSONObject(), this);
			}
			if(!ValidateUtil.isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,null, this);
			}
			custValid.setIsSuccess(1);
			custValid.setSuccessTime(new Date());
			custServ.updateCodeStatus(custValid);
			
			//验证通过
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Customer customerInfo = cs.findCustomerByPhone(phone);
			JSONObject resultInfo = new JSONObject();
			
			if(null!=customerInfo){//数据库里已有验证过的手机
				
					if("".equals(customerInfo.getPassword()) || customerInfo.getPassword()==null){//添加是否有密码标识
						obj.put("hasPwd", false);
						resultInfo.put("retCode", ErrorCode.RESP_CODE_NOT_EXIST_PWD);
						resultInfo.put("retInfo",  ErrorCode.RESP_INFO_NOT_EXIST_PWD);
					}else{
						obj.put("hasPwd", true);
						resultInfo.put("retCode", ErrorCode.RESP_CODE_PHONE_LOGIN);
						resultInfo.put("retInfo",  ErrorCode.RESP_INFO_PHONE_LOGIN);
					}
					//手机快速登录 如果用户名为空 就自动修改用户名称
					if(null==customerInfo.getAccount()){
						customerInfo.setAccount(Util.getCustomerAccount(customerInfo.getMobilephone()));
						custServ.updateCustomer(customerInfo);
					}
					
					obj.put("account", customerInfo.getAccount());
					obj.put("userExist", true);
					obj = getCustomerInfo(customerInfo,cs,obj);
					resultInfo.put("vo", obj);
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"成功返回用户信息!", resultInfo, this);
			}else{
				return existAccount(cs,phone,"",headInfo);
			}
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 手机快速登录与注册(该手机号与已有用户用户名称一致逻辑)
	 */
	private ExecuteResult existAccount(CustomerService cs,String phone,String password,HeadInfo  headInfo){
		JSONObject resultInfo = new JSONObject();
		JSONObject obj = new JSONObject();
		try {
			String hardwareId = getContext().getHead().getUniqueId();
			String otherInfo = getContext().getHead().getOtherInfo();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();
			Customer customer = new Customer();
			customer.setCreateTime(new Date());
			customer.setRegisterIp(ip);
			customer.setTerminalType(platform);
			customer.setTerminalVersion(version);
			customer.setTerminalNumber(hardwareId);
			String[] a = IPSeeker.getAreaNameByIp(ip);
			customer.setRegisterProvince(a[0]);
			customer.setRegisterCity(a[1]);
			customer.setMobilephone(phone);
			customer.setPhoneValidate(1);
			if(!"".equals(password)){
				customer.setPassword(password);
			}
			Customer c  = cs.findCustomerByAccount(phone);
			if(null!=c){//该手机号与已有用户名一致
				if(1==c.getPhoneValidate()){//已有用户已验证过其他手机
					customer.setAccount(Util.getCustomerAccount(phone));
					cs.addCustomer(customer, otherInfo, appDiv);//新增用户 
					obj.put("userExist", false);
				}else{//已有用户未已验证过其他手机  返回提示关联账号
					obj.put("account", c.getAccount());
					obj.put("uid", c.getId());
					obj.put("phone", phone);
					obj.put("emailValidate", c.getEmailValidate()==null ? 0 : c.getEmailValidate());
					obj.put("email", c.getEmail()==null ? "" : c.getEmail());
					obj.put("accountPhone", c.getMobilephone());
//					res.setRetCode(ErrorCode.RESP_CODE_LINK_CUSTOMER_INFO);
//					res.setRetInfo(ErrorCode.RESP_INFO_LINK_CUSTOMER_INFO);
//					res.setVo(map);
					resultInfo.put("retCode", ErrorCode.RESP_CODE_LINK_CUSTOMER_INFO);
					resultInfo.put("retInfo",  ErrorCode.RESP_INFO_LINK_CUSTOMER_INFO);
					resultInfo.put("vo", obj);
					obj.put("userExist", true);
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "关联用户信息", resultInfo, this);
				}
			}else{//与已有用户名不一致
				customer.setAccount(Util.getCustomerAccount(phone));
				cs.addCustomer(customer, otherInfo, appDiv);//新增用户
				obj.put("userExist", false);
			} 
			// TODO　合并购物车
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(EbShoppingCartService.class);
			String cartId = getContext().getHead().getCartId();
			shoppingCartService.updateShoppingCartByLogin(customer.getId(), cartId);
			SessionCustomer sc = new SessionCustomer(customer);
			String sessionId = sessionService.signIn(customer.getId());
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			
			obj.put("sessionId", sessionId);
			obj.put("uid", customer.getId());
			obj.put("account", customer.getAccount());
			obj.put("isMember", false);
			obj.put("memberType", 0);
			obj.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
//			res.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
//			res.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
//			res.setVo(map);
			//添加统计信息
			resultInfo.put("retCode", ErrorCode.RESP_CODE_PHONE_LOGIN);
			resultInfo.put("retInfo",  ErrorCode.RESP_INFO_PHONE_LOGIN);
			resultInfo.put("vo", obj);
			Util.addStatistics(getContext(), customer);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功", resultInfo, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
		
	}
	
	
	
	
	/**
	 * 用户手机注册 params{phone,code,imgcode}
	 * 1、
	 */
	private ExecuteResult moblieRegist(){
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		HeadInfo  headInfo= getContext().getHead();
		JSONObject obj  = new JSONObject();
		//手机短信验证通过
		try {
			if(jsonObj.isNull("phone") || StringUtil.isNullOrEmpty(jsonObj.optString("phone","")) ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号不能为空", null, this);
			}
			
			if(jsonObj.isNull("password") || StringUtil.isNullOrEmpty(jsonObj.optString("password")) ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"密码不能为空", null, this);
			}
			if(jsonObj.isNull("validateNum") || StringUtil.isNullOrEmpty(jsonObj.optString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",null, this);
			}
			String phone = jsonObj.optString("phone").trim();
			String password = jsonObj.optString("password").trim();
			String validateNum = jsonObj.optString("validateNum").trim();
			String imageCode = jsonObj.optString("imageCode").trim();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> result = new HashMap<String, Object>();
			
			//满足 出图形验证码要求 需要验证图形验证码
			if(StringUtil.isNotNullNotEmpty(imageCode)){
				if (!checkImageVerifyCode(imageCode, getContext().getHead()
						.getUniqueId())) {
					respone.setRetCode(ErrorCode.RESP_CODE_IMAGE_CODE_ERROR);
					respone.setRetInfo(ErrorCode.RESP_INFO_IMAGE_CODE_ERROR);
					return new ExecuteResult(
							CommandList.RESPONSE_STATUS_OK, "图形验证码不正确",
							respone.convertJSONObject(), this);
				}else{//图形验证码验证成功 清空次数
					custServ.delBeyondCount(phone, getContext());
				}
			}
			
			boolean flag = custServ.isBeyondCount(phone, getContext());
			if(flag){
				respone.setRetCode(ErrorCode.RESP_CODE_GET_IMG_CODE);
				respone.setRetInfo(ErrorCode.RESP_INFO_GET_IMG_CODE);
				result.put("isShowImgCode", flag);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",respone.convertJSONObject(), this);
			}
			
			ForgetPasswordCode custValid = custServ.getCustomerPhoneCodeExist(phone, validateNum,ValidateTypeEnum.PHONEREGIST.getValue(), 0);
			if(custValid == null){
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_VERIFY_CODE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR);
				//记录验证码错误次数 修改
				custServ.saveOrUpdateCount(phone, getContext());// 2016516 新加
//				result.put("isShowImgCode", custServ.isBeyondCount(phone,getContext()));
//				int errorCount = getAndUpdateCustomerValidateCount(phone,CustomerValidateCountTypeEnum.VALIDATECODE.getValue());
//				result.put("errorCount", errorCount);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR,respone.convertJSONObject(), this);
			}
			if(!ValidateUtil.isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,null, this);
			}
			custValid.setIsSuccess(1);
			custValid.setSuccessTime(new Date());
			custServ.updateCodeStatus(custValid);
			
			
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Customer customer = cs.findCustomerByPhone(phone);
			
			if(null!=customer){
				respone.setRetCode(ErrorCode.RESP_CODE_REGIST_PHONE_VALIDATE);
				respone.setRetInfo(ErrorCode.RESP_INFO_REGIST_PHONE_VALIDATE);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_REGIST_PHONE_VALIDATE,respone.convertJSONObject(), this);
			}
			
			return existAccount(cs, phone, password,headInfo);
			
		} catch (Exception e) {
			
			return getExceptionExecuteResult(e);
		}
		
		
	}
	
	/**
	 * 手机注册验证 params{phone}
	 * 1、验证传递参数
	 * @throws JSONException 
	 * 
	 */
	private ExecuteResult validateMoblieRegist() {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		RestResponse res = new RestResponse();
		try {
			if(jsonObj.isNull("phone") || StringUtil.isNullOrEmpty(jsonObj.optString("phone")) ){//注册手机号码传递参数为空
	
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！", 
						null, this);
			}
			String phone  = jsonObj.optString("phone").trim();
			
			if(!ValidateUtil.isMoblie(phone)){//手机格式校验不通过
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"手机号格式不对", 
						null, this);
			}
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			if(cs.isverificateMoblie(phone)){//该手机号已经被验证过
				res.setRetCode(ErrorCode.RESP_CODE_REGIST_PHONE_VALIDATE);
				res.setRetInfo(ErrorCode.RESP_INFO_REGIST_PHONE_VALIDATE);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_REGIST_PHONE_VALIDATE, 
						res.convertJSONObject(), this);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验通过", 
					res.convertJSONObject(), this);
		} catch (Exception e) {
//			res.setRetCode(ErrorCode.RESP_CODE_NETWORK_ERR);
//			res.setRetInfo(ErrorCode.RESP_INFO_NETWORK_ERR);
//			res.setVo(false);
//			return new ExecuteResult(ErrorCode.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_NETWORK_ERR, 
//					new JSONObject(new Gson().toJson(res)), this);
			return getExceptionExecuteResult(e);
		}
	} 
	
	/**
	 * @description 5.0 新版登录
	 * @return ExecuteResult
	 * @throws Exception
	 */
	private ExecuteResult login5_0() throws Exception {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		
		if(validateAccountAndPwd(jsonObj)!=null){//验证传递参数账号密码
			return validateAccountAndPwd(jsonObj);
		}
		String account = jsonObj.optString("account").trim();
		String password = jsonObj.optString("password").trim();
		String imageCode = jsonObj.optString("imageCode","").trim();
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		Customer customer = null;
		JSONObject obj  = new JSONObject();
		String newAccount = "";//新账号
		String phone = "";
		try {
			if(cs.isverificateMoblie(account)){
				phone = account;
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			//满足 出图形验证码要求 需要验证图形验证码
			if(StringUtil.isNotNullNotEmpty(imageCode)){
				if (!checkImageVerifyCode(imageCode, getContext().getHead()
						.getUniqueId())) {
					obj.put("retCode", ErrorCode.RESP_CODE_IMAGE_CODE_ERROR);
					obj.put("retInfo", ErrorCode.RESP_INFO_IMAGE_CODE_ERROR);
					return new ExecuteResult(
							CommandList.RESPONSE_STATUS_OK, "图形验证码不正确",
							obj, this);
				}else{//图形验证码验证成功 清空次数
					custServ.delBeyondCount(phone, getContext());
				}
			}
			boolean flag = custServ.isBeyondCount(phone, getContext());
			if(flag){
				obj.put("retCode", ErrorCode.RESP_CODE_GET_IMG_CODE);
				obj.put("retInfo", ErrorCode.RESP_INFO_GET_IMG_CODE);
				obj.put("isShowImgCode", flag);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",obj, this);
			}
			if(ValidateUtil.isMoblie(account)){//如果传入的账号是手机格式
				if(cs.isverificateMoblie(account)){//是已经验证过的手机
					customer = cs.findCustomrByMoblieAndPassword(account,password);// 输入的是手机号格式且该号码是已绑定过的状态且密码匹配成功且用户名合法
					if(null!=customer){
						
						if(null==customer.getAccount()){
							obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
							//生成新的用户名,修改原有用户名
							newAccount = Util.getCustomerAccount(customer.getMobilephone());
							obj.put("oldAccount", customer.getAccount());
							customer.setAccount(newAccount);
							obj.put("account", newAccount);
						}else{
							if(!ValidateUtil.isEmail(customer.getAccount())&&!ValidateUtil.isMoblie(customer.getAccount())&&null!=customer.getAccount()){//用户名合法
								obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
								obj.put("account", customer.getAccount());
							}else{//用户名不合法
								if(customer.getMobilephone().equals(customer.getAccount())){//用户名与手机号一样，自动修改用户名成
									obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
								}else{
									obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());//重要信息提示页面
								}
								//生成新的用户名,修改原有用户名
								newAccount = Util.getCustomerAccount(customer.getMobilephone());
								obj.put("oldAccount", customer.getAccount());
								customer.setAccount(newAccount);
								obj.put("account", newAccount);
							}
						}
					}
				}else{
					customer = cs.findCustomerByAccountAndPassword(account,password);//输入的是未验证过的手机号码,就按照用户名加密码进行匹配
					if(null!=customer){
						if(null!=customer.getPhoneValidate()){
							 if(1==customer.getPhoneValidate()){
								 obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());//重要信息提示页面
								//生成新的用户名,修改原有用户名
								newAccount = Util.getCustomerAccount(customer.getMobilephone());
								obj.put("oldAccount", customer.getAccount());
								customer.setAccount(newAccount);
								obj.put("account", newAccount);
							 }else{
								 obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
							 }
						}else{
							obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
						}
					}
				}
			}else if(ValidateUtil.isEmail(account)){//如果传入的账号是邮箱格式
				if(cs.isverificateEmail(account)){
					customer = cs.findCustomrByEmailAndPassword(account,password);
					if(null!=customer){
						if(null!=customer.getPhoneValidate()){
							if(1==customer.getPhoneValidate()){
									
									if(null==customer.getAccount()){
										obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
										newAccount = Util.getCustomerAccount(customer.getMobilephone());
										customer.setAccount(newAccount);
										obj.put("account", newAccount);
									}else{
										if(!ValidateUtil.isEmail(customer.getAccount()) && !ValidateUtil.isMoblie(customer.getAccount()) && null!=customer.getAccount()){
											obj.put("account", customer.getAccount());
											obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
										}
										if(ValidateUtil.isEmail(customer.getAccount()) || ValidateUtil.isMoblie(customer.getAccount())){
											if(customer.getMobilephone().equals(customer.getAccount())){
												obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
											}else{
												obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());//进入重要信息提示页面
											}
											newAccount = Util.getCustomerAccount(customer.getMobilephone());
											obj.put("oldAccount", customer.getAccount());
											customer.setAccount(newAccount);
											obj.put("account", newAccount);
										}
									}
								}else{
									obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
								}
							}else{
								obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
							}	
						}
				}else{
					customer = cs.findCustomerByAccountAndPassword(account,password);//未验证的邮箱按账户和密码匹配查询
					if(null!=customer){
						if(null!=customer.getPhoneValidate()){
							if(1==customer.getPhoneValidate()){
								newAccount = Util.getCustomerAccount(customer.getMobilephone());
								obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());//进入重要信息提示页面
								obj.put("oldAccount", customer.getAccount());
								customer.setAccount(newAccount);
								obj.put("account", newAccount);
							}else{
								obj.put("loginStatus", LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
							}
						}else{
							obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
						}
					}
				}
			}else{
				customer = cs.findCustomerByAccountAndPassword(account,password);
				if(customer!=null){
					if(null!=customer.getPhoneValidate()){
						if(1==customer.getPhoneValidate()){
							obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//直接登录
							obj.put("account",customer.getAccount());
						}else{
							obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
						}
					}else{
						obj.put("loginStatus",LoginStatusEnum.BINDING_MOBILE.getValue());//进入绑定手机页面
					}
				}
			}
		//最后根据查询的用户信息返回相应提示
			if (customer == null) {
				obj.put("retCode", ErrorCode.RESP_CODE_PWD_ACCOUNT_ERROR);
				obj.put("retInfo", ErrorCode.RESP_INFO_PWD_ACCOUNT_ERROR);
				custServ.saveOrUpdateCount(phone, getContext());
//				result.put("isShowImgCode", custServ.isBeyondCount(phone, getContext()));
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"用户不存在或密码错误", obj, this);
			}
			//如果新账号不为空 更新原有账号字段
			if(!"".equals(newAccount)){
				cs.updateAccountByUserId(customer.getId(), newAccount);
			}
			if(obj.getInt("loginStatus")==2){
				obj.put("retCode", ErrorCode.RESP_CODE_BINDING);
				obj.put("retInfo", ErrorCode.RESP_INFO_BINDING);
				obj.put("uid", customer.getId());
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_BINDING, obj, this);
			}
			
			obj.put("phone", customer.getMobilephone());
			obj = getCustomerInfo(customer,cs,obj);
			if(obj.has("oldAccount")){
				obj.put("oldAccount", obj.get("oldAccount"));
			}
			obj.put("retCode", ErrorCode.RESP_CODE_PHONE_LOGIN);
			obj.put("retInfo", ErrorCode.RESP_INFO_PHONE_LOGIN);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"成功返回用户信息!", obj, this);
		} catch (Exception e) {
			
			return getExceptionExecuteResult(e);
		}
}
	
	/**
	 * 验证手机号码
	 * params 是否是第三放登录 isPlatformLogin、smsCode、imageCode、codType、userId
	 * 根据 isPlatformLogin 输入手机号已经被绑定进行不同操作来返回不同提示
	 * return ExecuteResult
	 */
	public ExecuteResult validateMobile(int userId)throws Exception{
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		
		ExecuteResult result = null;
		if(null!=validatePhoneAndCode(jsonObj)){//校验手机号码时需要传递 （电话号码：phone，短信验证码：code）
			return result;
		}
		
		/**
		 * 1、根据用户 手机号 查询输入验证获取次数，验证错误次数，2小时之内获取次数为3次或者验证次数为3次，需要验证图形验证码，未传递提示错误。验证成功清除错误信息
		 * 2、没有满足返回图形验证码条件，验证电话和短信验证码相关逻辑
		 * 3、短信验证码验证成功 根据isPlatformLogin 在进行业务逻辑区分
		 */
		
		
		try {
			
			return null;
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 根据登录封装返回用户相关信息
	 * @param customer
	 */
	private Map<String,Object> getCustomerInfo(Customer customer,CustomerService cs,Map<String,Object> json){
	    try {
	    	SessionCustomer sc = new SessionCustomer(customer);
			String sessionId = sessionService.signIn(customer.getId());
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			// TODO　合并购物车
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(EbShoppingCartService.class);
			String cartId = getContext().getHead().getCartId();
			shoppingCartService.updateShoppingCartByLogin(customer.getId(), cartId);
			//TODO　添加登录日志
			addLoginLog(customer);
			//TODO 根据不同的登录类型封装返回用户信息
			json.put("uid", customer.getId());
			json.put("credits", customer.getCredits());
			json.put("sessionId", sessionId);
			MemberService memberServ = SystemInitialization
					.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberServ.memberCheck_v2_5(customer.getId());
			int memberType = obj.optInt("memberType");
			json.put("isMember", memberType == 1?true : false);
			json.put("memberType", memberType);
			if(memberType == 1){
				json.put("vipEndTime", obj.optString("vipEndTime"));
			}
			json.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
		} catch (Exception e) {
			// TODO: handle exception
		}
	    return json;
	}
	
	/**
	 * 根据登录封装返回用户相关信息
	 * @param customer
	 */
	private JSONObject getCustomerInfo(Customer customer,CustomerService cs,JSONObject json){
	    try {
	    	SessionCustomer sc = new SessionCustomer(customer);
			String sessionId = sessionService.signIn(customer.getId());
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			// TODO　合并购物车
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(EbShoppingCartService.class);
			String cartId = getContext().getHead().getCartId();
			shoppingCartService.updateShoppingCartByLogin(customer.getId(), cartId);
			//TODO　添加登录日志
			addLoginLog(customer);
			//TODO 根据不同的登录类型封装返回用户信息
			json.put("uid", customer.getId());
			json.put("credits", customer.getCredits());
			json.put("sessionId", sessionId);
			MemberService memberServ = SystemInitialization
					.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberServ.memberCheck_v2_5(customer.getId());
			int memberType = obj.optInt("memberType");
			json.put("isMember", memberType == 1?true : false);
			json.put("memberType", memberType);
			if(memberType == 1){
				json.put("vipEndTime", obj.optString("vipEndTime"));
			}
			json.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
		} catch (Exception e) {
			// TODO: handle exception
		}
	    return json;
	}
	/**
	 * 添加登录日志
	 * @param customer
	 */
	private void addLoginLog(final Customer customer){
		LowPriorityExecutor.execLog(new Runnable() {
			@Override
			public void run() {
				// 记录用户登录日志
				try {
					CustomerLoginRecordDao recordDao = SystemInitialization
							.getApplicationContext().getBean(
									CustomerLoginRecordDao.class);
					CustomerLoginRecord record = new CustomerLoginRecord();
					record.setCustomer(customer);
					record.setIp(getContext().getHead().ip);
					record.setTerminalType(getContext().getHead().getPlatform());
					record.setTerminalVersion(getContext().getHead()
							.getVersion());
					record.setTime(new Date());
					record.setNumber(getContext().getHead().getUniqueId());
					String[] a = IPSeeker.getAreaNameByIp(record.getIp());
					record.setLoginProvince(a[0]);
					record.setLoginCity(a[1]);
					recordDao.save(record);
					//统计登录
					Util.addStatistics(getContext(), record);
				} catch (Exception e) {
					logger.error("Log customer error : ", e);
				}
			}
		});
	}
	
	/**
	 * 验证登录账号与密码是否为空
	 * @throws JSONException 
	 */
	private ExecuteResult validateAccountAndPwd(JSONObject jsonObj) throws JSONException{
	   if (jsonObj.isNull("account") || jsonObj.isNull("password")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"请填写用户名和密码！", null, this);
		}
		String account = jsonObj.optString("account","");
		String password = jsonObj.optString("password","");
		if (StringUtil.isNullOrEmpty(account)) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"用户名不能为空", null, this);
		}
		if (StringUtil.isNullOrEmpty(password)) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"密码不能为空", null, this);
		}
		return null;
	}
	
	/**
	 * 验证登录传递手机号和验证码
	 * @throws JSONException 
	 */
	public ExecuteResult validatePhoneAndCode(JSONObject jsonObj) throws JSONException{
		
		if(jsonObj.isNull("phone") || jsonObj.isNull("code")){
			return new ExecuteResult(ErrorCode.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_INPUT_PHONE, null, this);
		}
		String phone = jsonObj.optString("phone","");
		String code  = jsonObj.optString("code","");
		if (StringUtil.isNullOrEmpty(phone)) {
			return new ExecuteResult(ErrorCode.RESPONSE_STATUS_FAIL,
					ErrorCode.RESP_INFO_REGIST_PHONE_EMPTY, null, this);
		}
		if (StringUtil.isNullOrEmpty(code)) {
			return new ExecuteResult(ErrorCode.RESPONSE_STATUS_FAIL,
					ErrorCode.RESP_INFO_INPUT_CODE, null, this);
		}
		return null;
	}
	
	/**
	* <p>功能描述:校验图形验证码是否正确</p>
	* <p>参数：@param code
	* <p>参数：@param device
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 * @throws SqlException 
	 */
	private boolean checkImageVerifyCode(String code,String device) throws SqlException{
		ImageVerify imageVerify = getImageVerifyByCodeAndDevice(code, device);
		if(imageVerify != null){
			return true;
		}
		return false;
	}
	/**
	* <p>功能描述:获取图型验证码</p>
	* <p>参数：@param code
	* <p>参数：@param deivce
	* <p>参数：@return</p>
	* <p>返回类型：ImageVerify</p>
	 * @throws SqlException 
	 */
	private ImageVerify getImageVerifyByCodeAndDevice(String code,String device) throws SqlException{
		ImageVerifyService imageServ = SystemInitialization.getApplicationContext().getBean(ImageVerifyService.class);
		ImageVerify imageVerify = imageServ.getImageVerifyByCodeAndDevice(code, device);
		return imageVerify;
	}
	
}
