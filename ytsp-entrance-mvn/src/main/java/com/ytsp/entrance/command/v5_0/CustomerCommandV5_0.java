package com.ytsp.entrance.command.v5_0;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.icu.util.Calendar;
import com.statistics.enums.LoginStatusEnum;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.CustomerLoginRecordDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerCollection;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.db.domain.CustomerThirdPlatform;
import com.ytsp.db.domain.CustomerValidateCount;
import com.ytsp.db.domain.ForgetPasswordCode;
import com.ytsp.db.domain.ImageVerify;
import com.ytsp.db.enums.CustomerValidateCountTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.ValidateTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.CustomerCollectionVO;
import com.ytsp.db.vo.CustomerVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.errorcode.ErrorCode;
import com.ytsp.entrance.handleResponse.RestResponse;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.EbShoppingCartService;
import com.ytsp.entrance.service.ImageVerifyService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.sms.SmsHandler;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.AesEncrypt;
import com.ytsp.entrance.util.DESUtils;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.ValidateUtil;
import com.ytsp.entrance.util.VerifyClientCustomer;
import com.ytsp.entrance.util.mail.MailFacade;
import com.ytsp.entrance.util.mail.MailServiceFactory;

public class CustomerCommandV5_0 extends AbstractCommand{
	//短信校验时限
	private static final int VALIDATETIME = 10;
	//生成的校验码的长度
	private static final int VALIDATENUMLENGTH = 6;
	
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_CUSTOMER_CENTER
				||code == CommandList.CMD_CUSTOMER_PHONE_VALIDATE_NUM
				||code == CommandList.CMD_CUSTOMER_EMAIL_VALIDATE_NUM
				||code == CommandList.CMD_CUSTOMER_PHONE_VALIDATE
				||code == CommandList.CMD_CUSTOMER_EMAIL_VALIDATE
				||code == CommandList.CMD_CUSTOMER_PHONE_IS_VAILDATE
				||code == CommandList.CMD_CUSTOMER_EMAIL_IS_VAILDATE
				||code == CommandList.CMD_CUSTOMER_PHONE_UNBINDING
				||code == CommandList.CMD_CUSTOMER_CHANGE_PHONE_BOUND
				||code == CommandList.CMD_CUSTOMER_CHANGE_PHONE_BYEMAIL
				||code == CommandList.CMD_CUSTOMER_CHANGE_PHONE_SEND_EMAIL
				||code == CommandList.CMD_CUSTOMER_GET_RECOVER_USER
				||code == CommandList.CMD_CUSTOMER_RECOVER_VALIDATE_CODE
				||code == CommandList.CMD_CUSTOMER_SEND_RECOVER_EMAIL
				||code == CommandList.CMD_CUSTOMER_VALIDATE_RECOVER_CODE
				||code == CommandList.CMD_CUSTOMER_PASSWORD_RECOVER_CONFIRM
				||code == CommandList.CMD_CUSTOMER_CHANGE_EMAIL
				||code == CommandList.CMD_CUSTOMER_EMAIL_UNBINDING
				|| code == CommandList.CMD_CUSTOMER_MODIFY_PASSOWRD
				|| code == CommandList.CMD_CUSTOMER_CHECK_PHONE_CODE
				|| code == CommandList.CMD_CUSTOMER_CHECK_EMAIL_CODE
				|| code == CommandList.CMD_CUSTOMER_COLLECTION_LIST//用户收藏产品或动漫列表
				|| code == CommandList.CMD_CUSTOMER_SAVE_COLLECTION//用户添加产品或动漫
				|| code == CommandList.CMD_CUSTOMER_DEL_COLLECTION//用户取消产品活着动漫收藏
				|| code ==CommandList.CMD_CUSTOMER_DEL_ALL_COLLECTION//用户清空收藏列表内容
				|| code ==CommandList.CMD_IMAGE_VERIFY_CHECK
				|| code ==CommandList.CMD_DEL_COLLECTIONS
				|| code ==CommandList.CMD_PHONE_SEND_SMS_VALIDATE_CODE
				||code ==CommandList.CMD_CUSTOMER_UNBINDING_VALIDATE
				|| code ==CommandList.CMD_PHONE_CHECK_VALIDATE_CODE//发送手机验证码
				|| code ==CommandList.CMD_QUICK_FIND_PWD_SEND_CODE//发送快速找回密码验证码
				|| code ==CommandList.CMD_QUICK_VALIDATE_EMAIL_CODE//验证快速找回邮箱验证码
				|| code ==CommandList.CMD_THIRD_PLAT_LOGIN_BINDING_ACCOUNT
				|| code ==CommandList.CMD_THIRD_PLAT_LOGIN_RELATE_EXISTS_USER
				|| code ==CommandList.CMD_THIRD_PLAT_LOGIN_OCCUPY_RELATE
				|| code ==CommandList.CMD_QUICK_FIND_PWD;//快速找回密码
	}

	@Override
	public ExecuteResult execute() {
		
		int code = getContext().getHead().getCommandCode();
		if(code == CommandList.CMD_CUSTOMER_GET_RECOVER_USER){
			return getPasswordRecoverAccount();
		}else if(code == CommandList.CMD_CUSTOMER_RECOVER_VALIDATE_CODE){
			return getRecoverPhoneValidateCode();
		}else if(code == CommandList.CMD_CUSTOMER_SEND_RECOVER_EMAIL){
			return sendPwdRecoverEmail();
		}else if(code ==CommandList.CMD_CUSTOMER_PASSWORD_RECOVER_CONFIRM){
			return passwordRecoverConfirm();
		}else if(code ==CommandList.CMD_CUSTOMER_VALIDATE_RECOVER_CODE){
			return validateRecoverCode();
		}else if(code ==CommandList.CMD_CUSTOMER_CHECK_PHONE_CODE){
			return checkPhoneValidateNum();
		}else if(code ==CommandList.CMD_CUSTOMER_CHECK_EMAIL_CODE){
			return checkEmailValidateNum();
		}else if(code ==CommandList.CMD_IMAGE_VERIFY_CHECK){
			return checkImageCode();
		}else if(code ==CommandList.CMD_PHONE_SEND_SMS_VALIDATE_CODE){
			return sendSMSValidateCode();
		}else if(code ==CommandList.CMD_PHONE_CHECK_VALIDATE_CODE){
			return checkPhoneValidateCode();
		}else if(code ==CommandList.CMD_CUSTOMER_UNBINDING_VALIDATE){
			return bindingAccount();
		}else if(code ==CommandList.CMD_QUICK_FIND_PWD_SEND_CODE){
			return sendQuickCode();
		}else if(code ==CommandList.CMD_QUICK_VALIDATE_EMAIL_CODE){
			return validateQuickEmailCode();
		}else if(code ==CommandList.CMD_QUICK_FIND_PWD){
			return updatePwd();
		}else if(code ==CommandList.CMD_THIRD_PLAT_LOGIN_BINDING_ACCOUNT){
			return thirdPlatformLoginBindingAccount();
		}else if(code ==CommandList.CMD_THIRD_PLAT_LOGIN_RELATE_EXISTS_USER){
			return thirdPlatformLoginRelateExistUser();
		}else if(code ==CommandList.CMD_THIRD_PLAT_LOGIN_OCCUPY_RELATE){
			return thirdPlatLoginPhoneOccupyRelate();
		}
		
		// 验证权限.
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
//		 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}

		if (code == CommandList.CMD_CUSTOMER_CENTER) {
			return customerCenter();
		} else if(code == CommandList.CMD_CUSTOMER_PHONE_VALIDATE_NUM){
			return getPhoneValidateNumber(userId);
		}else if(code == CommandList.CMD_CUSTOMER_EMAIL_VALIDATE_NUM){
			return sendEmailValidateNum(userId);
		} if(code == CommandList.CMD_CUSTOMER_PHONE_VALIDATE){
			return validatePhone(userId);
		}else if(code == CommandList.CMD_CUSTOMER_EMAIL_VALIDATE){
			return validateEmail(userId);
		}else if(code == CommandList.CMD_CUSTOMER_PHONE_IS_VAILDATE){
			return customerPhoneIsValidate(userId);
		}else if(code == CommandList.CMD_CUSTOMER_EMAIL_IS_VAILDATE){
			return customerEmailIsValidate(userId);
		}else if(code == CommandList.CMD_CUSTOMER_PHONE_UNBINDING){
			return unbindingPhone(userId);
		}else if(code == CommandList.CMD_CUSTOMER_CHANGE_PHONE_BOUND){
			return changePhoneValidateNum(userId);
		}else if(code == CommandList.CMD_CUSTOMER_CHANGE_PHONE_SEND_EMAIL){
			return sendChangePhoneByEmail(userId);
		}else if(code == CommandList.CMD_CUSTOMER_CHANGE_PHONE_BYEMAIL){
			return changePhoneByEmail(userId);
		}else if(code == CommandList.CMD_CUSTOMER_CHANGE_EMAIL){
			return changeEmail(userId);
		}else if(code == CommandList.CMD_CUSTOMER_EMAIL_UNBINDING){
			return unbindingEmail(userId);
		}else if(code == CommandList.CMD_CUSTOMER_MODIFY_PASSOWRD){
			return modifyPasswd(userId);
		}else if(code ==CommandList.CMD_CUSTOMER_COLLECTION_LIST){
			return getCustomerCollectionList(userId);//获取用户收藏数据列表
		}else if(code ==CommandList.CMD_CUSTOMER_SAVE_COLLECTION){
			return saveCutomerCollection(userId);//保存用户添加收藏数据
		}else if(code == CommandList.CMD_CUSTOMER_DEL_COLLECTION){
			return delCutomerCollection(userId);//用户取消产品活着动漫收藏
		}else if(code ==CommandList.CMD_CUSTOMER_DEL_ALL_COLLECTION){
			return delAllCutomerCollection(userId);//用户清空收藏列表内容
		}else if(code == CommandList.CMD_DEL_COLLECTIONS){//用户批量删除收藏列表内容
			return delCutomerCollections(userId);
		}
		return null;
	}
	
	/**
	* <p>功能描述:第三方登录手机已被占用关联</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult thirdPlatLoginPhoneOccupyRelate(){
		try {
			
			String key = (String) getContext().getRequest().getSession(true)
					.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
			if (key == null)
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
						null, this);
			JSONObject reqBody = null;
			if(MobileTypeEnum.wapmobile == MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				reqBody = getContext().getBody().getBodyObject();
			}else{
				reqBody = new JSONObject(AesEncrypt.decrypt(key,
							getContext().getBody().getBodyObject().get("thirdPartJson")
									.toString()));
			}
			
			if (reqBody.isNull("token") || reqBody.isNull("platformName")
					|| reqBody.isNull("uid")) {
				logger.error("平台用户授权信息不全 : " + reqBody);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请先用第三方平台登录授权！", null, this);
			}
			if(reqBody.isNull("phone")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号不能为空", null, this);
			}
			
			if(reqBody.isNull("validateNum")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"验证码不能为空", null, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"类型不能为空", null, this);
			}
			if(reqBody.isNull("operateType")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"操作类型不能为空", null, this);
			}
			
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> result = new HashMap<String, Object>();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			String validateNum = reqBody.optString("validateNum");
			String phone = reqBody.optString("phone");
			int type = reqBody.optInt("type");
			String platformName = StringUtils.trim(reqBody
					.optString("platformName"));
			String openId = StringUtils.trim(reqBody.optString("uid"));
			if(MobileTypeEnum.wapmobile != MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				platformName = getThirdPlatFormType(platformName);
			}
			String token = reqBody.optString("token");
			String unionId = reqBody.optString("unionId");
			//operateType:操作类型：1表示：不关联 2：关联
			int operateType = reqBody.optInt("operateType");
			int bindingUid  = reqBody.optInt("bindingUserId");
			
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			//获取验证信息
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, 0, 1, validateNum,type);
			
			if(custValid == null){
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_VERIFY_CODE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR);
				//记录验证码错误次数 修改
				custServ.saveOrUpdateCount(phone, getContext());// 2016516 新加
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR,respone.convertJSONObject(), this);
			}
			
			CustomerService custService = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			//将第三方用户信息绑定到已有的用户上
			if(operateType == 2){
				
				//校验第三方信息是否存在
				CustomerThirdPlatform thirdPlatInfo = getCustomerThirdPlatform(platformName, unionId, openId);
				if(thirdPlatInfo != null){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "关联失败，您的第三方用户已存在，请重新登录",new JSONObject(), this);
				}
				Customer bindingCust = getCustomerById(bindingUid);
				//创建第三方用户，并绑定手机号已占用的用户
				createCustomerThirdPlatform(bindingCust, platformName, openId, token,
						unionId);
				// 获取返回用户信息
				getCustomerInfo(bindingCust, custService, result);
				custServ.checkAccount(bindingCust.getId(), result);
			}else{//将手机绑定到当前用户下 
				//校验第三方信息是否存在
				CustomerThirdPlatform thirdPlatInfo = getCustomerThirdPlatform(platformName, unionId, openId);
				if(thirdPlatInfo != null){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "关联失败，您的第三方用户已存在，请重新登录",new JSONObject(), this);
				}
				String account = Util.getCustomerAccount(phone);
				custServ.updateUnbindingExistsUser(phone);
				//创建新的用户
				Customer newCust = creatNewCustomer(phone, account, 1);
				//创建第三方用户，并绑定新的用户
				createCustomerThirdPlatform(newCust, platformName, openId, token,
						unionId);
				// 获取返回用户信息
				getCustomerInfo(newCust, custService, result);
				result.put("account", newCust.getAccount());
				result.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());
			}
			custServ.updateCodeStatus(custValid);
			respone.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
			respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "关联成功",respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("bindingAccount() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:第三方登录关联已有帐号</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult thirdPlatformLoginRelateExistUser(){
		try {
			String key = (String) getContext().getRequest().getSession(true)
					.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
			if (key == null)
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
						null, this);
			JSONObject reqBody = null;
			if(MobileTypeEnum.wapmobile == MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				reqBody = getContext().getBody().getBodyObject();
			}else{
				reqBody = new JSONObject(AesEncrypt.decrypt(key,
							getContext().getBody().getBodyObject().get("thirdPartJson")
									.toString()));
			}
			if (reqBody.isNull("token") || reqBody.isNull("platformName")
					|| reqBody.isNull("uid")) {
				logger.error("平台用户授权信息不全 : " + reqBody);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请先用第三方平台登录授权！", null, this);
			}
			if(reqBody.isNull("account")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"帐号不能为空", null, this);
			}
			
			if(reqBody.isNull("password")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"密码不能为空", null, this);
			}
			
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> result = new HashMap<String, Object>();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			String account = reqBody.optString("account");
			String password = reqBody.optString("password");
			String platformName = StringUtils.trim(reqBody
					.optString("platformName"));
			String openId = StringUtils.trim(reqBody.optString("uid"));
			if(MobileTypeEnum.wapmobile != MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				platformName = getThirdPlatFormType(platformName);
			}
			String token = reqBody.optString("token");
			String unionId = reqBody.optString("unionId");
			String imageCode = reqBody.optString("imageCode","");//获取图形验证码
			//校验帐号为空
			if(StringUtil.isNullOrEmpty(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户名不能为空", null, this);
			}
			
			
			String ispPhone = "";
			if(ValidateUtil.isMoblie(account)){
				ispPhone = account;
			}
			
			if(StringUtil.isNotNullNotEmpty(imageCode)){
				if (!checkImageVerifyCode(imageCode, getContext().getHead()
						.getUniqueId())) {
					respone.setRetCode(ErrorCode.RESP_CODE_IMAGE_CODE_ERROR);
					respone.setRetInfo(ErrorCode.RESP_INFO_IMAGE_CODE_ERROR);
					return new ExecuteResult(
							CommandList.RESPONSE_STATUS_OK, "图形验证码不正确",
							respone.convertJSONObject(), this);
				}else{//图形验证码验证成功 清空次数
					custServ.delBeyondCount(ispPhone, getContext());
				}
			}
			
			boolean flag = custServ.isBeyondCount(ispPhone, getContext());
			if(flag){
				respone.setRetCode(ErrorCode.RESP_CODE_GET_IMG_CODE);
				respone.setRetInfo(ErrorCode.RESP_INFO_GET_IMG_CODE);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",respone.convertJSONObject(), this);
			}
			
			
			Customer cust = custServ.getCustomerByPhoneOrEmailOrAccount(account);
			if(cust == null || !password.equals(cust.getPassword())){
				custServ.saveOrUpdateCount(ispPhone, getContext());
				respone.setRetCode(ErrorCode.RESP_CODE_PWD_ACCOUNT_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PWD_ACCOUNT_ERROR);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_PWD_ACCOUNT_ERROR,respone.convertJSONObject(), this);
			}
			
			//校验第三方信息是否存在，该接口只有第三方不存在才会调用
			CustomerThirdPlatform thirdPlat = getCustomerThirdPlatform(platformName, unionId, openId);
			if(thirdPlat != null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "数据错误！",result, this);
			}
			//新建第三方登录信息，将用户绑定到第三方信息上
			createCustomerThirdPlatform(cust, platformName, openId, token,
					unionId);
			CustomerService custService = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			// 获取返回用户信息
			getCustomerInfo(cust, custService, result);
			result.put("account", cust.getAccount());
			//校验用户名是否合法
//			checkAccount(cust.getId(), result);
			//第三方登录关联，不用检查用户名是否合法，手机是否绑定。直接登录
			result.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());
			result.put("phone", cust.getMobilephone());
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "关联第三方用户信息成功",respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("thirdPlatformLoginRelateExistUser() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:第三方登录新建用户绑定手机</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult thirdPlatformLoginBindingAccount(){
		try {
			String key = (String) getContext().getRequest().getSession(true)
					.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
			if (key == null)
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
						null, this);
			JSONObject reqBody = null;
			//移动端网站
			if(MobileTypeEnum.wapmobile == MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				reqBody = getContext().getBody().getBodyObject();
			}else{
				reqBody = new JSONObject(AesEncrypt.decrypt(key,
						getContext().getBody().getBodyObject().get("thirdPartJson")
								.toString()));
			}
			
			if (reqBody.isNull("token") || reqBody.isNull("platformName")
					|| reqBody.isNull("uid")) {
				logger.error("平台用户授权信息不全 : " + reqBody);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请先用第三方平台登录授权！", null, this);
			}
			if(reqBody.isNull("phone")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号不能为空", null, this);
			}
			
			if(reqBody.isNull("validateNum")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"验证码不能为空", null, this);
			}
			
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> result = new HashMap<String, Object>();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			String validateNum = reqBody.optString("validateNum");
			String phone = reqBody.optString("phone");
			int type = reqBody.optInt("type");
			//0:为新建，1：为关联
//			int operateType =  reqBody.optInt("operateType");
			String imageCode = reqBody.optString("imageCode","");
			String platformName = StringUtils.trim(reqBody
					.optString("platformName"));
			String openId = StringUtils.trim(reqBody.optString("uid"));
			if(MobileTypeEnum.wapmobile != MobileTypeEnum.valueOf(getContext().getHead().getPlatform())){
				platformName = getThirdPlatFormType(platformName);
			}
			String token = reqBody.optString("token");
			String unionId = reqBody.optString("unionId");
			
			
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
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
			
			//获取验证码，校验验证码是否正确
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, 0, validateNum, type);
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
			CustomerThirdPlatform thirdPlat = getCustomerThirdPlatform(platformName, unionId, openId);
			if(thirdPlat != null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "数据错误！",result, this);
			}
			
			//获取手机验证状态
			int isValidate = custServ.getPhoneValidateStatus(phone, 0);
			result.put("validateStatus", isValidate);
			//已占用
			if(isValidate == 2){
				Customer bingdingcust = custServ.getCustomerByPhone(phone, 1);
				result.put("bindingAccount", bingdingcust.getAccount());
				result.put("bindingUserId", bingdingcust.getId());
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_IS_VALIDATE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_IS_VALIDATE_ERROR);
				respone.setVo(result);
				custServ.updateCodeStatus(custValid);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_PHONE_IS_VALIDATE_ERROR,
						respone.convertJSONObject(), this);
			}
			//手机号未验证，直接创建用户和第三方信息
			if(isValidate == 0){
				CustomerService custService = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
				String account = Util.getCustomerAccount(phone);
				//创建新的用户
				Customer newCust = creatNewCustomer(phone, account, 1);
				//创建第三方用户，并绑定新的用户
				createCustomerThirdPlatform(newCust, platformName, openId, token,
						unionId);
				// 获取返回用户信息
				getCustomerInfo(newCust, custService, result);
				result.put("account", newCust.getAccount());
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
			}
			custServ.updateCodeStatus(custValid);
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机验证码成功",respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("thirdPlatformLoginBindingAccount() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:校验手机验证码是否正确</p>
	* <p>参数：@param phone
	* <p>参数：@param validateNum
	* <p>参数：@param type
	* <p>参数：@param respone
	* <p>参数：@param result
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ExecuteResult</p>
	 */
//	private ExecuteResult checkPhoneCodeRight(String phone, String validateNum,
//			int type, RestResponse<Map<String, Object>> respone,
//			Map<String, Object> result,ForgetPasswordCode custValid) throws Exception {
//		if(custValid == null){
//			respone.setRetCode(ErrorCode.RESP_CODE_PHONE_VERIFY_CODE_ERROR);
//			respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR);
//			//记录验证码错误次数
//			int errorCount = getAndUpdateCustomerValidateCount(phone,CustomerValidateCountTypeEnum.VALIDATECODE.getValue());
//			result.put("errorCount", errorCount);
//			respone.setVo(result);
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证码不正确，请重新获取验证！",respone.convertJSONObject(), this);
//		}
//		if(custValid.getIsSuccess() == 1){
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "输入的验证码已验证！",result, this);
//		}
//		if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
//		}
//		return null;
//	}
	
	
	/**
	 * 根据登录封装返回用户相关信息
	 * @param customer
	 */
	private void getCustomerInfo(Customer customer,CustomerService cs,Map<String,Object> json){
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
			//用户名
//			json.put("account", customer.getAccount());
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
	* <p>功能描述:获取第三方登录信息</p>
	* <p>参数：@param platformNameO
	* <p>参数：@param unionId
	* <p>参数：@param uid
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerThirdPlatform</p>
	 */
	private CustomerThirdPlatform getCustomerThirdPlatform(String platformName,String unionId,String uid) throws SqlException{
		CustomerService cs = SystemInitialization.getApplicationContext()
				.getBean(CustomerService.class);
		CustomerThirdPlatform customerTP = null;
		if("997".equals(platformName)&&StringUtil.isNotNullNotEmpty(unionId)){//微信登陆 新版 并且传递unionId
			CustomerThirdPlatform tp = cs.findCustomerThirdPlatformByUninoId(unionId);
			if(tp==null){
					customerTP = cs.findCustomerByThirdPlatform(
						platformName, uid);
			}else{
				     customerTP  = tp; 
			}
		}else{
			customerTP = cs.findCustomerByThirdPlatform(
					platformName, uid);
		}
		return customerTP;
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@return</p>
	* <p>返回类型：CustomerThirdPlatform</p>
	 * @throws SqlException 
	 */
	private CustomerThirdPlatform createCustomerThirdPlatform(
			Customer customer, String platform, String openId, String token,
			String unionId) throws SqlException {
		CustomerService cs = SystemInitialization.getApplicationContext()
				.getBean(CustomerService.class);
		CustomerThirdPlatform customerTP = new CustomerThirdPlatform();
		customerTP.setCustomer(customer);
		customerTP.setPlatform_name(platform);
		customerTP.setUser_id(openId);
		customerTP.setToken(token);
		if("997".equals(platform)&& StringUtil.isNotNullNotEmpty(unionId)){
			customerTP.setUnion_id(unionId);
		}
		cs.createCustomerThirdPlatform(customerTP);
		return customerTP;
	}
	
	/**
	* <p>功能描述:创建新用户</p>
	* <p>参数：@param phone
	* <p>参数：@param account
	* <p>参数：@return</p>
	* <p>返回类型：Customer</p>
	 * @throws Exception 
	 */
	private Customer creatNewCustomer(String phone,String account,int phoneValidate) throws Exception{
		String hardwareId = getContext().getHead().getUniqueId();
		String otherInfo = getContext().getHead().getOtherInfo();
		String platform = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		String appDiv = getContext().getHead().getAppDiv();
		String ip = getContext().getHead().getIp();
		Customer customer = new Customer();
		customer.setAccount(account);
		customer.setPassword("");
		customer.setNick("");
		customer.setCreateTime(new Date());
		customer.setRegisterIp(ip);
		customer.setTerminalType(platform);
		customer.setTerminalVersion(version);
		customer.setTerminalNumber(hardwareId);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		customer.setRegisterProvince(a[0]);
		customer.setRegisterCity(a[1]);
		customer.setMobilephone(phone);
		customer.setEmail("");
		customer.setPhoneValidate(phoneValidate);
		customer.setEmailValidate(0);
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		//注册用户
		custServ.saveCustomerRegist(customer, otherInfo, appDiv);
		return customer;
	}
	
	/**
	 * 用户快速找回密码
	 */
	private ExecuteResult updatePwd(){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int userId = reqBody.getInt("uid");
			if(reqBody.isNull("password") || StringUtil.isNullOrEmpty(reqBody.getString("password"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "密码不能为空！",null, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",null, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",null, this);
			}
			String pwd = reqBody.getString("password").trim();
			String validateNum = reqBody.getString("validateNum").trim();
			int type = reqBody.getInt("type");
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",null, this);
			}
			ForgetPasswordCode custValid = null;
			
			if(ValidateTypeEnum.QUICKPWDEMAIL.getValue()==type){
				custValid = custServ.getEmailValidateByCode(cust.getEmail(), 0,1,validateNum,type);
			}else{
				custValid = custServ.getPhoneValidate(cust.getMobilephone(), userId, 1, validateNum, type);
			}
			
			if(null!=custValid){
					cust.setPassword(pwd);
					custServ.updateCustomer(cust);
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "找回密码成功！",null, this);
			}else{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不存在！",null, this);
			}
			
		} catch (Exception e) {
			logger.error("updatePwd() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 快速找回邮箱验证码验证
	 * 
	 */
	private ExecuteResult validateQuickEmailCode(){
		try {
//			JSONObject result = new JSONObject();
			Map<String,Object> result = new HashMap<String, Object>();
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",null, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",null, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",null, this);
			}
			String email = reqBody.optString("email","");
			String validateNum = reqBody.optString("validateNum","");
			String imageCode = reqBody.optString("imageCode","");
			int type =  reqBody.optInt("type");
			if(!ValidateUtil.isEmail(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱格式不正确！", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			//加邮箱为验证的提示

			if(custServ.getCustomerInfoByEmail(email,1)==null){
				respone.setRetCode(ErrorCode.RESP_CODE_PWD_RECOVER_EMAIL_NOT_VALIDATE);
				respone.setRetInfo(ErrorCode.RESP_INFO_PWD_RECOVER_EMAIL_NOT_VALIDATE);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "该邮箱未验证", respone.convertJSONObject(), this);
			}
			
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
					custServ.delBeyondCount("", getContext());
				}
			}
			
			boolean flag = custServ.isBeyondCount("", getContext());
			if(flag){
				respone.setRetCode(ErrorCode.RESP_CODE_GET_IMG_CODE);
				respone.setRetInfo(ErrorCode.RESP_INFO_GET_IMG_CODE);
				result.put("isShowImgCode", flag);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",respone.convertJSONObject(), this);
			}
			
			//获取用户邮箱验证信息
			ForgetPasswordCode custValid = custServ.getEmailValidateByCode(email, 0,0,validateNum,type);
			if(custValid == null){
				custServ.saveOrUpdateCount("", getContext());
				respone.setRetCode(ErrorCode.RESP_CODE_EMAIL_CODE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_EMAIL_CODE_ERROR);
//				result.put("isShowImgCode", flag);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "邮箱验证码不正确",respone.convertJSONObject(), this);
			}
			
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			//绑定用户邮箱
			custServ.updateEmailCodeStatus(custValid);
			Customer customer = custServ.getCustomerInfoByEmail(email,1);
			if(null!=customer){
				result.put("account", customer.getAccount());
				result.put("phone", customer.getMobilephone());
				result.put("uid", customer.getId());
				respone.setVo(result);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证成功",respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("validateQuickEmailCode() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	
	/**
	* <p>功能描述:校验解绑参数 是否正确</p>
	* <p>参数：@param reqBody
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkBindingAccountParam(JSONObject reqBody) throws JSONException{
		if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",null, this);
		}
		if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",null, this);
		}
		if(reqBody.isNull("type")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",null, this);
		}
		if(reqBody.isNull("userId")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户id不能为空！",null, this);
		}
		//operateType:操作类型：1表示：将手机绑定到当前用户下 2：将第三方用户信息绑定到已有的用户上
		if(reqBody.isNull("operateType")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "操作类型不能为空！",null, this);
		}
		
		if(reqBody.optInt("operateType") == 2){
			if(reqBody.isNull("bindingUserId")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "已绑定的用户id不能为空！",null, this);
			}
			if(reqBody.isNull("thirdPlatId")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "第三方信息不能为空！",null, this);
			}
		}
		return null;
	}
	
	/**
	* <p>功能描述:用户解绑并绑定帐号</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult bindingAccount(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			ExecuteResult checkParamResult = checkBindingAccountParam(reqBody);
			//校验参数
			if(checkParamResult != null){
				return checkParamResult;
			}
			int userId = reqBody.optInt("userId");
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			
			String validateNum = reqBody.optString("validateNum");
			String phone = reqBody.optString("phone");
			//operateType:操作类型：1表示：将手机绑定到当前用户下 2：将第三方用户信息绑定到已有的用户上
			int operateType = reqBody.optInt("operateType");
			int bindingUid  = reqBody.optInt("bindingUserId");
			int type = reqBody.optInt("type");
			String imageCode = reqBody.optString("imageCode","");
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			Map<String,Object> map = new HashMap<String, Object>();
			
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			//获取验证信息
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, userId, 1, validateNum,type);
			
			if(custValid == null){
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_VERIFY_CODE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR);
				//记录验证码错误次数 修改
				custServ.saveOrUpdateCount(phone, getContext());// 2016516 新加
				respone.setVo(map);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR,respone.convertJSONObject(), this);
			}
			//校验验证码是否超过10分钟
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			//需要检查用户名userId
			int checkUserId = 0;
			//将第三方用户信息绑定到已有的用户上
			if(operateType == 2){
				Customer bindingCust = getCustomerById(bindingUid);
				checkUserId = bindingUid;
				if(bindingCust == null || bindingCust.getPhoneValidate() != 1){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "绑定的用户不存在，请检查用户id是否正确！",result, this);
				}
				if(!phone.equals(bindingCust.getMobilephone())){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "已有用户绑定的手机号与当前验证的手机号不一致！",result, this);
				}
				int thirdPlatId = reqBody.optInt("thirdPlatId");
				custServ.updateBindingCustomer(custValid, bindingCust,thirdPlatId);
				cust = bindingCust;
			}else{//将手机绑定到当前用户下 
				//解绑手机号，解绑相关验证信息
				custServ.updateUnbindingPhone(custValid, cust);
				checkUserId = cust.getId();
			}
			respone.setVo(custServ.checkAccount(checkUserId));
			//校验登录状态，若是直接登录，将用户信息返回
			if(respone.getVo().containsKey("loginStatus")){
				if((Integer)respone.getVo().get("loginStatus") == 1 || (Integer)respone.getVo().get("loginStatus") == 3){
					CustomerService custService = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
					getCustomerInfo(cust, custService, respone.getVo());
					respone.getVo().put("phone", cust.getMobilephone());
					respone.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
					respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
				}
			}
			custServ.updateCodeStatus(custValid);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "解绑用户成功",respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("bindingAccount() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:校验手机验证码是否正确</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkPhoneValidateCode(){
		try {
			Map<String,Object> result = new HashMap<String, Object>();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.optString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.optString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "类型不能为空！",result, this);
			}
			if(reqBody.isNull("userId")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户id不能为空",result, this);
			}
			String validateNum = reqBody.optString("validateNum");
			String phone = reqBody.optString("phone");
			int type = reqBody.optInt("type");
			int userId = reqBody.optInt("userId");
			String imageCode = reqBody.optString("imageCode","");
			
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			//校验11位手机号是否正确
			if(!ValidateUtil.isMoblie(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
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
				
			//手机快速找回，校验用户是否绑定手机
			if(type == ValidateTypeEnum.QUICKPWDPHONE.getValue()){
				if (!isPhoneValidated(phone, result)) {
					respone.setRetCode(ErrorCode.RESP_CODE_PHONE_NOT_VALIDATED_ERROR);
					respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_NOT_VALIDATED_ERROR);
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "手机号未验证，无法进行该操作",respone.convertJSONObject(), this);
				}
			}
			
			
			int isValidate = custServ.getPhoneValidateStatus(phone, userId);
			
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, userId, 0, validateNum, type);
			
			if(custValid == null){
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_VERIFY_CODE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR);
				//记录验证码错误次数
				custServ.saveOrUpdateCount(phone, getContext());// 2016516 新加
//				result.put("isShowImgCode", custServ.isBeyondCount(phone,getContext()));
//				int errorCount = getAndUpdateCustomerValidateCount(phone,CustomerValidateCountTypeEnum.VALIDATECODE.getValue());
//				result.put("errorCount", errorCount);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_PHONE_VERIFY_CODE_ERROR,respone.convertJSONObject(), this);
			}
			
			
			if(custValid.getIsSuccess() == 1){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "输入的验证码已验证！",result, this);
			}
			
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			//手机快速找回更新验证码状态
			if(type == ValidateTypeEnum.QUICKPWDPHONE.getValue()){
				Customer c = custServ.getCustomerByPhone(phone, 1);
				custValid.setIsSuccess(1);
				custValid.setSuccessTime(new Date());
				custValid.setCustomer(c);
				//更新手机验证信息
				custServ.updateValidateInfo(custValid);
				result.put("uid", c.getId());
				respone.setVo(result); 
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机验证码成功",respone.convertJSONObject(), this);
			}
			
			
			result.put("validateStatus", isValidate);
			//如果该手机已被其它用户占用，获取该用户信息
			if(isValidate == 2){
				Customer bingdingcust = custServ.getCustomerByPhone(phone, 1);
				result.put("bindingAccount", bingdingcust.getAccount());
				result.put("bindingUserId", bingdingcust.getId());
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_IS_VALIDATE_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_IS_VALIDATE_ERROR);
				respone.setVo(result);
				//将验证码置为失败
				custServ.updateCodeStatus(custValid);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_PHONE_IS_VALIDATE_ERROR,
						respone.convertJSONObject(), this);
			}else if(isValidate == 0){
				if(userId == 0){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证的用户不存在",respone.convertJSONObject(), this);
				}
				Customer c = getCustomer(userId);
				//解绑手机号，解绑相关验证信息
				custServ.updateUnbindingPhone(custValid, c);
				custServ.checkAccount(userId,result);
				//校验登录状态，若是直接登录，将用户信息返回
				if(result.containsKey("loginStatus")){
					if((Integer)result.get("loginStatus") == 1 || (Integer)result.get("loginStatus") == 3){
						CustomerService custService = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
						getCustomerInfo(c, custService, result);
					}
				}
				respone.setRetCode(ErrorCode.RESP_CODE_PHONE_LOGIN);
				respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_LOGIN);
			}
			
			respone.setVo(result); 
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机验证码成功",respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("checkPhoneValidateCode() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:校验验证码是否正确</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult sendSMSValidateCode(){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			Map<String,Object> result = new HashMap<String,Object>();
			if(reqBody.isNull("phone")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机号不能为空", null, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",null, this);
			}
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			int type = reqBody.getInt("type");
			String phone = reqBody.optString("phone");
			Integer userId = reqBody.optInt("userId",0);
			String imageCode = reqBody.optString("imageCode","");
			//校验11位手机号是否正确
			if(!ValidateUtil.isMoblie(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手机格式错误", null, this);
			}
			
			//非法的手机号直接提示发送成功
			if(!VerifyClientCustomer.validateCellphone(phone)){
				result.put("timeLimit", IConstants.SENDSMSLIMITTIME);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"发送短信验证码成功", respone.convertJSONObject(), this);
			}
			
			//快速手机密码找回，若手机未验证过不可获取验证码
			if(type == ValidateTypeEnum.QUICKPWDPHONE.getValue().intValue()){
				if(!isPhoneValidated(phone, result)){
					respone.setRetCode(ErrorCode.RESP_CODE_PHONE_NOT_VALIDATED_ERROR);
					respone.setRetInfo(ErrorCode.RESP_INFO_PHONE_NOT_VALIDATED_ERROR);
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "手机号未验证，无法进行该操作",respone.convertJSONObject(), this);
				}
			}
			
			//手机快速注册 如果是已经验证的手机提示
			if(type == ValidateTypeEnum.PHONEREGIST.getValue().intValue()){
				CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
				if(cs.findCustomerByPhone(phone)!=null){
					respone.setRetCode(ErrorCode.RESP_CODE_REGIST_PHONE_VALIDATE);
					respone.setRetInfo(ErrorCode.RESP_INFO_REGIST_PHONE_VALIDATE);
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, ErrorCode.RESP_INFO_REGIST_PHONE_VALIDATE,respone.convertJSONObject(), this);
				}
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			boolean flag = custServ.isBeyondCount(phone,getContext());
			ForgetPasswordCode custVal = null;
			//获取短信验证码
			ForgetPasswordCode fpc = custServ.getCustomerValidateByPhoneAndType(phone, type,0);
			long limitTime = getSendSMSTimeLimit(fpc);
			//查看与上次发送短信是否超90秒，未超90秒，返回时间
			if(!isTimeCanSendSMS(limitTime)){
				respone.setRetCode(ErrorCode.RESP_CODE_SEND_SMS_TIME_LIMIT);
				respone.setRetInfo(ErrorCode.RESP_INFO_SEND_SMS_TIME_LIMIT);
				respone.setVo(result);
				result.put("timeLimit", limitTime);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"离上次发送短信间隔未超过90秒", respone.convertJSONObject(), this);
			}
			
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
					flag = custServ.isBeyondCount("",getContext());
				}
			}
			
			if(flag){
				respone.setRetCode(ErrorCode.RESP_CODE_GET_IMG_CODE);
				respone.setRetInfo(ErrorCode.RESP_INFO_GET_IMG_CODE);
				result.put("isShowImgCode", flag);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",respone.convertJSONObject(), this);
			}
			
			//是否发送新的短信验证码
			boolean isSendNewCode = isBuildCustomerValidate(fpc);
			if(isSendNewCode){
				Customer c = getCustomer(userId);
				//构建验证信息
				custVal = buildCustomerValidate(c,1,phone,type);
			}
			
			
			String sendCode = isSendNewCode ? custVal.getCode() : fpc.getCode();
			//发送短信验证码
			String isSuccess = Util.sendSms(phone, sendCode, type);
			if(!"0".equals(isSuccess)){
				//发送短信
				boolean isOk = SmsHandler.sendSms(phone, sendCode);
				if(!isOk){ 
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "您的操作过于频繁，请稍后重试",result, this);
				}
			}
			if(isSendNewCode){
				//保存验证信息
				custServ.saveCustomerValidate(custVal);
			}else{//不重新发送 更新开始时间
				if(fpc!=null){
					Customer c = new Customer();
					c.setId(userId);
					fpc.setCustomer(c);
					fpc.setStartTime(new Date());
					custServ.updateValidateInfo(fpc);
				}
			}
			
			//更新发送次数
			custServ.saveOrUpdateCount(phone, getContext());
//			result.put("isShowImgCode", custServ.isBeyondCount(phone, getContext()));
			result.put("timeLimit", IConstants.SENDSMSLIMITTIME);
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"发送短信验证码成功", respone.convertJSONObject(), this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:获取用户，若userId为0，返回用户id为0的用户</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Customer</p>
	 */
	private Customer getCustomer(int userId) throws SqlException{
		Customer c = new Customer();
		if(userId == 0){
			c = new Customer();
			c.setId(0);
		}else{
			c = getCustomerById(userId);
			if(c == null){
				c = new Customer();
				c.setId(0);
			}
		}
		return c;
	}
	
	/**
	* <p>功能描述:手机是否验证</p>
	* <p>参数：@param phone
	* <p>参数：@param result
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean  true:为已验证为，false为未验证过</p>
	 */
	private boolean isPhoneValidated(String phone,Map<String,Object> result) throws Exception{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		Customer cust = custServ.getCustomerByPhone(phone, 1);
		if(cust == null){
			return false;
		}
		result.put("account", cust.getAccount());
		result.put("phone", phone);
		result.put("userId", cust.getId());
		return true;
	}
	
	/**
	* <p>功能描述:保存或更新次数</p>
	* <p>参数：@param count
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	@SuppressWarnings("unused")
	private void saveOrUpdateCustomerValidateCount(CustomerValidateCount count) throws SqlException{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		custServ.saveOrUpdateCustomerValidateCount(count);
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
	
	/**
	* <p>功能描述:校验离上次发短信时间是否超过90秒</p>
	* <p>参数：@param time
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isTimeCanSendSMS(long time){
		if(time == 0){
			return true;
		}
		return false;
	}
	
	/**
	* <p>功能描述:获取离上次发送短信的时间，返回秒</p>
	* <p>参数：@param fpc
	* <p>参数：@return</p>
	* <p>返回类型：long</p>
	 */
	private long getSendSMSTimeLimit(ForgetPasswordCode fpc){
		if(fpc == null){
			return 0;
		}
		long startTime = fpc.getStartTime().getTime();
		long now = System.currentTimeMillis();
		long endTime = startTime + 90*1000;
		long timeLimit  = (endTime-now)/1000;
		return timeLimit<0 ? 0 : timeLimit;
	}
	
	/**
	* <p>功能描述:是否发送新的短信验证码</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isBuildCustomerValidate(ForgetPasswordCode fpc) throws SqlException{
		if(fpc == null){
			return true;
		}
		//如果之前的短信验证码还剩余不到1分钟，则需要重新发送新的验证码
		if(fpc.getEndTime().getTime() < System.currentTimeMillis()){
			return true;
		}
		if (fpc.getEndTime().getTime() - System.currentTimeMillis() > 0
				&& fpc.getEndTime().getTime() - System.currentTimeMillis() <= 60 * 1000) {
			return true;
		}
		return false;
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
	* <p>功能描述:获取手机验证次数，若有验证次数更新原有数量，若没有保存新的手机验证次数</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	private CustomerValidateCount getCustomerValidateCount(String phone,int type) throws SqlException{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		//获取手机2小时内获取验证码次数
		CustomerValidateCount validateCount = custServ.findCustomerValidateCountByPhone(phone,type);
		if(validateCount == null){
			CustomerValidateCount valCount = buildCustomerValidateCount(phone, 0,type);
			return valCount;
		}
		return validateCount; 
	}
	
	
	
	
	/**
	* <p>功能描述:获取手机验证错误次数</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	private int getValidateErrorCount(String phone,int type) throws SqlException{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		//获取手机2小时内获取验证码次数
		CustomerValidateCount validateCount = custServ.findCustomerValidateCountByPhone(phone,type);
		if(validateCount == null){
			return 0;
		}
		return validateCount.getValidateCount(); 
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
	* <p>功能描述:校验验证码是否正确</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkImageCode(){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("code")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"图片验证码不能为空", null, this);
			}
			String code = reqBody.optString("code");
			ImageVerifyService imageVerifyServ = SystemInitialization.getApplicationContext()
					.getBean(ImageVerifyService.class);
			ImageVerify imageVer = imageVerifyServ.getImageVerifyByCodeAndDevice(code,getContext().getHead().getUniqueId());
			if(imageVer == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"图片验证码不正确", null, this);
			}
			if(imageVer.getStatus() == 1){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"图片验证码已验证，不可再次使用", null, this);
			}
			if(imageVer.getStatus() == 2){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"图片验证码已失效，不可使用", null, this);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"图片验证码验证成功", null, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 功能描述：用户清空收藏操作
	 * 返回类型：ExecuteResult
	 */
	private ExecuteResult delAllCutomerCollection(int userId){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int type = reqBody.getInt("type");
			if(type==0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"收藏类型不能为空", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			custServ.delAllCustomerCollection(userId,type);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"用户清空收藏成功", null, this);
		} catch (Exception e) {
			// TODO: handle exception
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 用户批量删出收藏 
	 * params {type,ids}
	 */
	private ExecuteResult delCutomerCollections(int userId){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int type = reqBody.getInt("type");
			if(type==0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"收藏类型不能为空", null, this);
			}
			String ids = reqBody.getString("ids").trim();
			if("".equals(ids)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"请选择要删除的内容", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			custServ.delCustomerCollections(ids,type,userId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"删除收藏成功", null, this);
		} catch (Exception e) {
			// TODO: handle exception
			return getExceptionExecuteResult(e);
		}
	}
	
	
	
	/**
	 * 功能描述：用户取消/删除收藏操作
	 * 返回类型：ExecuteResult
	 */
	private ExecuteResult delCutomerCollection(int userId){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int type = reqBody.getInt("type");
			int id = 0;
			if(type==0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"收藏类型不能为空", null, this);
			}
			CustomerCollection customerCollection = new CustomerCollection();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			if(reqBody.has("id")){
				id  = reqBody.getInt("id");
			}
			if(type==1){
				customerCollection.setProductId(id);
			}else if(type==2){
				customerCollection.setAlbumId(id);
			}
			customerCollection.setUserId(userId);
			customerCollection.setType(type);
			custServ.delCustomerCollection(customerCollection);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"用户取消收藏成功", null, this);
		} catch (Exception e) {
			// TODO: handle exception
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 功能描述：用户添加收藏操作
	 * 返回类型：ExecuteResult
	 */
	private ExecuteResult saveCutomerCollection(int userId){
		RestResponse<Boolean> res = new RestResponse<Boolean>(); 
		JSONObject result = new JSONObject();
		try {
//			saveCustoerCollection
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int type = reqBody.getInt("type");
			int id  = 0;
			if(type==0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"类型传递错误", result, this);
			}
			if(reqBody.has("id")){
				id  = reqBody.getInt("id");
			}
			CustomerCollection customerCollection = new CustomerCollection();
			if(type==1){
				EbProductService eb  = SystemInitialization.getApplicationContext().getBean(EbProductService.class);
				if(null==eb.retrieveEbProductById(id)){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
							"商品不存在", result, this);
				}
				customerCollection.setProductId(id);
			}else if(type==2){
				AlbumServiceV5_0 al = SystemInitialization.getApplicationContext().getBean(AlbumServiceV5_0.class);
				if(null==al.getAlbum(id)){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
							"动漫不存在", result, this);
				}
				customerCollection.setAlbumId(id);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			customerCollection.setUserId(userId);
			customerCollection.setType(type);
			customerCollection.setCreateTime(new Date());
			custServ.saveCustoerCollection(customerCollection);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"用户添加收藏成功", result, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	/**
	 * 分页获取用户收藏信息列表
	 */
	private ExecuteResult getCustomerCollectionList(int userId){
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int type = reqBody.getInt("type");
			if(type==0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"收藏类型不能为空", null, this);
			}
			int pageSize = reqBody.getInt("pageSize");
			int page = reqBody.getInt("page");
			int lastId  = reqBody.getInt("lastId");
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			//根据type类型查询产品信还是动漫信息
			List<CustomerCollectionVO> l = null;
			JSONObject jo = new JSONObject();
			Gson gson = new Gson();
			int productTotalCount = 0;
			int albumTotalCount = 0;
			if(type==1){
				l = custServ.getProductCollection(userId,type,lastId,page,pageSize,getContext().getHead().getVersion(),getContext().getHead().getPlatform());
				productTotalCount  = custServ.getProductCollectionCount(userId, type);
				albumTotalCount = custServ.getAlbumCollectionCount(userId, 2);
			}else if(type==2){
				l =custServ.getAlbumCollection(userId,type,lastId,page,pageSize,getContext().getHead().getVersion(),getContext().getHead().getPlatform());
				productTotalCount  = custServ.getProductCollectionCount(userId, 1);
				albumTotalCount = custServ.getAlbumCollectionCount(userId, type);
			}
			jo.put("collection", l);
			jo.put("productTotalCount", productTotalCount);
			jo.put("albumTotalCount", albumTotalCount);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取用户收藏分页列表成功！",jo, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:修改密码</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult modifyPasswd(int userId) {
		try {
			JSONObject reqBody = getContext().getBody().getBodyObject();
			String oldpassword = reqBody.optString("oldpassword","");
			String password = reqBody.optString("password");
			String password2 = reqBody.optString("password2","");
//			if (StringUtil.isNullOrEmpty(oldpassword)) {
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
//						"旧密码不能为空！", null, this);
//			}
			if (StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"新密码不能为空！", null, this);
			}
			if (StringUtil.isNotNullNotEmpty(password2) && !password.equals(password2)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"重复密码与密码不一致！", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext()
					.getBean(CustomerServiceV5_0.class);
			Customer customer = custServ.getCustomerById(userId);
			if(customer == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"用户不存在！", null, this);
			}
			
			//若用户密码不为空，前端传的旧密码不能为空
			if(StringUtil.isNotNullNotEmpty(customer.getPassword()) && StringUtil.isNullOrEmpty(oldpassword)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_PASSWORD_ERROR,
						"旧密码不能为空！", null, this);
			}
			
			if (StringUtil.isNotNullNotEmpty(customer.getPassword()) && !oldpassword.equals(customer.getPassword())) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_PASSWORD_ERROR,
						"旧密码不正确！", null, this);
			}
			customer.setPassword(password);
			custServ.updateCustomer(customer);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"账户密码修改成功！", null, this);
		} catch (Exception e) {
			logger.error("modifyPasswd() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:修改已有邮箱</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult changeEmail(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("email")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			String email = reqBody.getString("email");
			String validateNum = reqBody.getString("validateNum");
			int type = reqBody.getInt("type");
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			//获取修改邮箱的验证信息
			ForgetPasswordCode emailValidates = custServ.getEmailValidateByCode(email, userId, 0, validateNum,type);
			if(emailValidates == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确！",result, this);
			}
			if(!isValidateNumValid(emailValidates.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			//更新邮箱验证信息并验证用户手机号
			custServ.changeCustomerEmailValidate(emailValidates, cust);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "邮箱验证成功",result, this);
		} catch (Exception e) {
			logger.error("changeEmail() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:校验密码找回验证码是否正确</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult validateRecoverCode(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			String phone = null;
			String email = null;
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("account") || StringUtil.isNullOrEmpty(reqBody.getString("account"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "帐号不能为空！",result, this);
			}
			if(!reqBody.isNull("phone")){
				phone = reqBody.getString("phone");
			}
			if(!reqBody.isNull("email")){
				email = reqBody.getString("email");
			}
			String account = reqBody.getString("account");
			String validateNum = reqBody.getString("validateNum");
			//校验用户是否存在
			Customer cust = getCustomerByAccount(account);
			if(cust == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			ForgetPasswordCode fpc = null;
			if(phone != null && !"".equals(phone)){
				fpc = custServ.getPhoneValidate(phone, cust.getId(), 0, validateNum,1);
			}
			if(email != null && !"".equals(email)){
				fpc = custServ.getEmailValidateByCode(email, cust.getId(), 0,validateNum,0);
			}
			if(fpc == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确！",result, this);
			}
			if(!isValidateNumValid(fpc.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "密码找回验证码校验成功",result, this);
		} catch (Exception e) {
			logger.error("passwordRecoverConfirm() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:密码找回确认</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult passwordRecoverConfirm(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("account") || StringUtil.isNullOrEmpty(reqBody.getString("account"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户名不能为空！",result, this);
			}
			if(reqBody.isNull("password") || StringUtil.isNullOrEmpty(reqBody.getString("password"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "密码不能为空！",result, this);
			}
			if(reqBody.isNull("password2") || StringUtil.isNullOrEmpty(reqBody.getString("password2"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "确认密码不能为空！",result, this);
			}
			String account = reqBody.getString("account");
			String password = reqBody.getString("password");
			String password2 = reqBody.getString("password2");
			if(!password.equals(password2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "两次密码输入不一致！",result, this);
			}
			//校验用户是否存在
			Customer cust = getCustomerByAccount(account);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			cust.setPassword(password);
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			custServ.updateCustomer(cust);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "密码找回成功",result, this);
		} catch (Exception e) {
			logger.error("passwordRecoverConfirm() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 通过手机号或者邮箱快速找回密码
	 * 
	 */
	private ExecuteResult sendQuickCode(){
		try {
			JSONObject reqBody  = getContext().getBody().getBodyObject();
			Map<String,Object> result = new HashMap<String, Object>();
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！", null, this);
			}
			String email = reqBody.getString("email");
			if((!ValidateUtil.isEmail(email))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱格式错误", null, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",null, this);
			}
			int type = reqBody.getInt("type");
			
			if(custServ.getCustomerInfoByEmail(email,1)==null){
				respone.setRetCode(ErrorCode.RESP_CODE_PWD_RECOVER_EMAIL_NOT_VALIDATE);
				respone.setRetInfo(ErrorCode.RESP_INFO_PWD_RECOVER_EMAIL_NOT_VALIDATE);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "该邮箱未验证", respone.convertJSONObject(), this);
			}
			String imageCode = reqBody.optString("imageCode","");
			boolean flag = custServ.isBeyondCount("",getContext());
				
			ForgetPasswordCode emailValidates = custServ.getQuickEmailCode(email, 0, 0, 0,type);
			//校验90秒内是否重复发邮件验证
			long limitTime = getSendSMSTimeLimit(emailValidates);
			//查看与上次发送短信是否超90秒，未超90秒，返回时间
			if(!isTimeCanSendSMS(limitTime)){
				respone.setRetCode(ErrorCode.RESP_CODE_EMAIL_SEND_SMS_TIME_LIMIT);
				respone.setRetInfo(ErrorCode.RESP_INFO_EMAIL_SEND_SMS_TIME_LIMIT);
				respone.setVo(result);
				result.put("timeLimit", limitTime);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"离上次发送验证码未超过90秒", respone.convertJSONObject(), this);
			}
			
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
					custServ.delBeyondCount("", getContext());
					flag = custServ.isBeyondCount("",getContext());
				}
			}
			
			if(flag){
				respone.setRetCode(ErrorCode.RESP_CODE_GET_IMG_CODE);
				respone.setRetInfo(ErrorCode.RESP_INFO_GET_IMG_CODE);
				result.put("isShowImgCode", flag);
				respone.setVo(result);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取图形验证码！",respone.convertJSONObject(), this);
			}
			
			
			boolean isSendNewCode = isBuildCustomerValidate(emailValidates);
			ForgetPasswordCode custVal = null;
			String validateNum = "";
			if(isSendNewCode){
				//构建密码找回验证信息
				Customer cust  = new Customer();
				cust.setId(0);
				custVal = buildCustomerValidate(cust, 0, email,ValidateTypeEnum.QUICKPWDEMAIL.getValue());
				validateNum = createRandom(VALIDATENUMLENGTH);
				custVal.setCode(validateNum);
				custVal.setMethod(0);
				custVal.setEndTime(ValidateUtil.getEndTime());
				custVal.setType(type);
				
			}
			
			validateNum = isSendNewCode ? validateNum : emailValidates.getCode();
			//发送邮件
			boolean isSuccess = sendEmailValidateMail(email, "",validateNum);
			long timeLeft =  90;
			//邮箱发送失败
			if(!isSuccess){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮件发送失败,请检查邮箱是否正确,然后重新验证邮箱",null, this);
			}
			if(isSendNewCode){//重新发送保存
				custServ.saveCustomerValidate(custVal);
			}else{//不重新发送 更新开始时间
				if(emailValidates!=null){
					Customer c = new Customer();
					c.setId(0);
					emailValidates.setStartTime(new Date());
					emailValidates.setCustomer(c);
					custServ.updateValidateInfo(emailValidates);
				}
			}
			
			custServ.saveOrUpdateCount("", getContext());
			result.put("timeLimit", timeLeft<0 ? 0:timeLeft);
			flag = custServ.isBeyondCount("", getContext());
//			result.put("isShowImgCode", custServ.isBeyondCount("", getContext()));
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证码发送成功",respone.convertJSONObject(), this);
			
		} catch (Exception e) {
			logger.error("sendQuickCode() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:发送密码找回验证码邮件</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult sendPwdRecoverEmail(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("account") || StringUtil.isNullOrEmpty(reqBody.getString("account"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户名不能为空！",result, this);
			}
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			String email = reqBody.getString("email");
			String account = reqBody.getString("account");
			//校验邮箱格式是否正确
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
			}
			//校验用户是否存在
			Customer cust = getCustomerByAccount(account);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			ForgetPasswordCode emailValidates = custServ.getChangePhoneByEmailValidate(email, cust.getId(), 0, 0);
			
			//校验2分钟内是否重复发邮件验证
			if(emailValidates != null && !isValidateNumValid(emailValidates.getStartTime(),2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "邮件已发送成功,请查收您的邮箱",result, this);
			}
			//构建密码找回验证信息
			ForgetPasswordCode custVal = buildCustomerValidate(cust, 0, email,0);
			String validateNum = createRandom(VALIDATENUMLENGTH);
			custVal.setCode(validateNum);
			//发送邮件
			boolean isSuccess = sendEmailValidateMail(email, cust.getAccount(),validateNum);
			//邮箱发送失败
			if(!isSuccess){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮件发送失败,请检查邮箱是否正确,然后重新验证邮箱",result, this);
			}
			custServ.saveCustomerValidate(custVal);
			//移动端网站不返回验证码
			if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) != MobileTypeEnum.wapmobile)) {
				result.put("validateNum", validateNum);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证成功",result, this);
		} catch (Exception e) {
			logger.error("sendPwdRecoverEmail() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:获取密码找回手机验证码</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getRecoverPhoneValidateCode(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("account") || StringUtil.isNullOrEmpty(reqBody.getString("account"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户名不能为空！",result, this);
			}
			String phone = reqBody.getString("phone");
			String account = reqBody.getString("account");
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = custServ.getCustomerByAccount(account);
			if(cust == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "根据用户名未找到相应用户！", null, this);
			}
			//构建验证信息
			ForgetPasswordCode custVal = buildCustomerValidate(cust,1,phone,0);
			//type=0:（密码找回）邮箱,type=1:（密码找回）手机 ,type=2:（验证邮箱）邮箱,type=3:（验证手机）手机
			//type=4:（更改验证邮箱）邮箱,type=5:（更改验证手机）手机,type=6:（重置交易密码）手机
			custVal.setCode(createRandom(VALIDATENUMLENGTH));
			custVal.setType(1);
			//发送短信
			boolean isSuccess = SmsHandler.sendSms(phone, custVal.getCode());
			if(!isSuccess){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "短信发送失败，请重新获取验证",result, this);
			}
			//保存验证信息
			custServ.saveCustomerValidate(custVal);
			//移动端网站不返回验证码
			if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) != MobileTypeEnum.wapmobile)) {
				result.put("validateNum", custVal.getCode());
			}
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取密码找回手机验证码成功",result, this);
		} catch (Exception e) {
			logger.error("getRecoverPhoneValidateCode() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:获取找回密码的用户</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getPasswordRecoverAccount(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("account") || StringUtil.isNullOrEmpty(reqBody.getString("account"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户名不能为空！",result, this);
			}
			String account = reqBody.getString("account");
			Customer cust = getCustomerByAccount(account);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			result.put("phone", cust.getMobilephone());
			result.put("email", cust.getEmail());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取用户成功",result, this);
		} catch (Exception e) {
			logger.error("getPasswordRecoverAccount() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:解绑验证邮箱</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult unbindingEmail(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			String validateNum = reqBody.getString("validateNum");
			String email = reqBody.getString("email");
			int type = reqBody.getInt("type");
			//校验邮箱格式
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			ForgetPasswordCode custValid = custServ.getEmailValidateByCode(email, userId, 0,validateNum,type);
			
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确，请重新获取验证！",result, this);
			}
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			//解绑已占用的邮箱，解绑相关验证信息，并绑定新的邮箱
			custServ.updateUnbindingEmail(custValid, cust,email);
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "解绑邮箱成功",result, this);
		} catch (Exception e) {
			logger.error("unbindingPhone() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:解除绑定的手机号</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult unbindingPhone(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			String validateNum = reqBody.getString("validateNum");
			String phone = reqBody.getString("phone");
			int type = reqBody.getInt("type");
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, userId, 0,validateNum,type);
			
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确，请重新获取验证！",result, this);
			}
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			//解绑手机号，解绑相关验证信息
			custServ.updateUnbindingPhone(custValid, cust);
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "解绑手机成功",result, this);
		} catch (Exception e) {
			logger.error("unbindingPhone() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:通过邮箱修改手机号</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult changePhoneByEmail(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			String phone = reqBody.optString("phone");
			String validateNum = reqBody.optString("validateNum");
			
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			//邮箱未验证的用户不能通过邮箱修改手机号
			if(cust.getEmailValidate() == null || cust.getEmailValidate() == 0 
					||StringUtil.isNullOrEmpty(cust.getEmail())){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱未验证不能修改手机号！",result, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			//获取邮箱改手机的验证信息
			ForgetPasswordCode emailValidates = custServ.getChangePhoneByEmailNumber(phone, userId, 9, 0,validateNum);
			if(emailValidates == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确！",result, this);
			}
			if(!isValidateNumValid(emailValidates.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			//更新验证信息并验证用户手机号
			custServ.updateChangePhoneByEmail(emailValidates, cust,phone);
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证成功",result, this);
		} catch (Exception e) {
			logger.error("changePhoneByEmail() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:邮箱改手机号，发送邮箱验证信息到邮箱 </p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult sendChangePhoneByEmail(int userId){
		try {
			JSONObject result = new JSONObject();
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			
			if(StringUtil.isNullOrEmpty(cust.getEmail())){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "帐号没有邮箱，请验证邮箱后再修改手机号！",result, this);
			}
			//邮箱未验证的用户不能通过邮箱修改手机号
			if(cust.getEmailValidate() == null || cust.getEmailValidate() == 0 ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱未验证不能修改手机号！",result, this);
			}
			String email = cust.getEmail();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			ForgetPasswordCode emailValidates = custServ.getChangePhoneByEmailValidate(email, userId, 2, 0);
			
			//校验2分钟内是否重复发邮件验证
			if(emailValidates != null && isValidateNumValid(emailValidates.getStartTime(),2)){
				result.put("email", email);
				result.put("validateNum", emailValidates.getCode());
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "邮件已发送成功,请查收您的邮箱",result, this);
			}
			
			ForgetPasswordCode custVal = buildCustomerValidate(cust, 2, email,0);
			String validateNum = createRandom(VALIDATENUMLENGTH);
			custVal.setCode(validateNum);
			//发送邮件
			boolean isSuccess = sendEmailValidateMail(email, cust.getAccount(),validateNum);
			//邮箱发送失败
			if(!isSuccess){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮件发送失败,请检查邮箱是否正确,然后重新验证邮箱",result, this);
			}
			custServ.saveCustomerValidate(custVal);
			result.put("email", email);
			//移动端网站不返回验证码
			if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) != MobileTypeEnum.wapmobile)) {
				result.put("validateNum", validateNum);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证成功",result, this);
		} catch (Exception e) {
			logger.error("sendChangePhoneByEmail() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:发送邮箱验证信息到邮箱 </p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult sendEmailValidateNum(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			String email = reqBody.getString("email");
			int type = reqBody.getInt("type");
			//校验邮箱格式是否正确
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
			}
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			ForgetPasswordCode custValid = custServ.getEmailValidate(email, userId, 0,type);
			//校验2分钟内是否重复发邮件验证
			if(custValid != null && !isValidateNumValid(custValid.getStartTime(),2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "邮件已发送成功,请查收您的邮箱",result, this);
			}
			
			ForgetPasswordCode custVal = buildCustomerValidate(cust, 0, email,type);
			String validateNum = createRandom(VALIDATENUMLENGTH);
			custVal.setCode(validateNum);
			//发送邮件
			boolean isSuccess = sendEmailValidateMail(email, cust.getAccount(),validateNum);
			//邮箱发送失败
			if(!isSuccess){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮件发送失败,请检查邮箱是否正确,然后重新验证邮箱",result, this);
			}
			custServ.saveCustomerValidate(custVal);
			//移动端网站不返回验证码
			if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) != MobileTypeEnum.wapmobile)) {
				result.put("validateNum", validateNum);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证成功",result, this);
		} catch (Exception e) {
			logger.error("sendEmailValidateNum() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:校验手机验证码</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkPhoneValidateNum(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int userId = getContext().getHead().getUid();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			String validateNum = reqBody.getString("validateNum");
			String phone = reqBody.getString("phone");
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			int isValidate = custServ.getPhoneValidateStatus(phone, userId);
			
			if(isValidate == 2){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号已被其它帐号占用，无法进行验证 ！",result, this);
			}
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, userId,validateNum);
			
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确，请重新获取验证！",result, this);
			}
			
			if(custValid.getIsSuccess() == 1){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "输入的验证码已验证！",result, this);
			}
			
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			custValid.setIsSuccess(1);
			custValid.setSuccessTime(new Date());
			custValid.setCustomer(cust);
			//更新手机验证信息
			custServ.updateValidateInfo(custValid);
			result.put("result", true);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机验证码成功",result, this);
		} catch (Exception e) {
			logger.error("checkPhoneValidateNum() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:验证邮箱验证码</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkEmailValidateNum(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int userId = getContext().getHead().getUid();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			String email = reqBody.getString("email");
			String validateNum = reqBody.getString("validateNum");
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
			}
			//获取当前登录用户
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			//获取用户邮箱验证信息
			ForgetPasswordCode custValid = custServ.getEmailValidateByCode(email, userId, validateNum);
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确！",result, this);
			}
			if(custValid.getIsSuccess() == 1){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "该验证码已验证过！",result, this);
			}
			
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			custValid.setIsSuccess(1);
			custValid.setSuccessTime(new Date());
			custValid.setCustomer(cust);
			//更新手机验证信息
			custServ.updateValidateInfo(custValid);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证码成功",result, this);
		} catch (Exception e) {
			logger.error("validateEmail() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:绑定邮箱验证</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult validateEmail(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			String email = reqBody.getString("email");
			String validateNum = reqBody.getString("validateNum");
			int type =  reqBody.getInt("type");
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
			}
			//获取当前登录用户
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			//获取用户邮箱验证信息
			ForgetPasswordCode custValid = custServ.getEmailValidateByCode(email, userId,0,validateNum,type);
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确！",result, this);
			}
			//绑定用户邮箱
			custServ.updateCustomerEmailValidate(custValid, cust);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱验证成功",result, this);
		} catch (Exception e) {
			logger.error("validateEmail() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	
	/**
	* <p>功能描述:发送邮件验证邮件</p>
	* <p>参数：@param email 邮箱
	* <p>参数：@param account 用户帐号
	* <p>参数：@param validateNum 验证码
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean sendEmailValidateMail(String email,String account,String validateNum) throws Exception{
		// 邮件内容
		String mailContent = "";
		mailContent = MailServiceFactory.getMailContentService()
					.readBindEmailHtmlContent("bind_email",
							account == null? "" : account, validateNum);
		boolean sendOk = MailFacade.sendMail(mailContent, "“爱看”帐户邮箱验证",
					email);
		return sendOk;
	}
	
	/**
	* <p>功能描述:发送通过邮箱改手机邮件</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param email
	* <p>参数：@param account
	* <p>参数：@param validateNum
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean sendChangePhoneMail(String email,String account,String validateNum) throws Exception{
		// 邮件内容
		String mailContent = "";
		mailContent = MailServiceFactory.getMailContentService()
					.readBindEmailHtmlContent("validate_number_email",
							account, validateNum);
		boolean sendOk = MailFacade.sendMail(mailContent, "“爱看”帐户邮箱验证",
					email);
		return sendOk;
	}
	
	
	/**
	* <p>功能描述:校验手机验证码</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult validatePhone(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			String validateNum = reqBody.getString("validateNum");
			String phone = reqBody.getString("phone");
			int type = reqBody.getInt("type");
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			int isValidate = custServ.getPhoneValidateStatus(phone, userId);
			if(isValidate == 1){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号已验证，无需验证！",result, this);
			}
			if(isValidate == 2){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号已被其它帐号占用，无法进行验证 ！",result, this);
			}
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, userId, 0,validateNum,type);
			
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确，请重新获取验证！",result, this);
			}
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			custValid.setIsSuccess(1);
			custValid.setSuccessTime(new Date());
			custValid.setCustomer(cust);
			custServ.updateCustomerValidate(custValid, cust, userId);
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机验证成功",result, this);
		} catch (Exception e) {
			logger.error("validatePhone() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:校验码是否超时，true为有效，false为无效</p>
	* <p>参数：@param validatTime
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isValidateNumValid(Date validatTime,int time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(validatTime);
		cal.add(Calendar.MINUTE, time);
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		if(now.after(cal)){
			return true;
		}
		return now.before(cal);
	}
	
	
	/**
	* <p>功能描述:用户手机是否验证过</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult customerEmailIsValidate(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("email") || StringUtil.isNullOrEmpty(reqBody.getString("email"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "邮箱不能为空！",result, this);
			}
			String email = reqBody.getString("email");
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = custServ.getCustomerById(userId);
			if(cust == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			int isValidate = custServ.getEmailValidateStatus(email,userId);
			result.put("validate", isValidate);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验邮箱是否被验证成功",result, this);
		} catch (Exception e) {
			logger.error("customerCenter() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:用户手机是否验证过</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult customerPhoneIsValidate(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			
			String phone = reqBody.getString("phone");
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = custServ.getCustomerById(userId);
			if(cust == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			
			int isvalidate = custServ.getPhoneValidateStatus(phone,userId);
			result.put("validate", isvalidate);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机号是否被验证成功",result, this);
		} catch (Exception e) {
			logger.error("customerPhoneIsValidate() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:用户获取手机验证码</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getPhoneValidateNumber(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			String phone = reqBody.getString("phone");
			int type = reqBody.getInt("type");
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = custServ.getCustomerById(userId);
			if(cust == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在，请登录后再验证！", null, this);
			}
			//获取用户验证信息
			int validStatus = custServ.getPhoneValidateStatus(phone, userId);
			result.put("validate", validStatus);
			//构建验证信息
			ForgetPasswordCode custVal = buildCustomerValidate(cust,1,phone,type);
			
			//发送短信
//			boolean isSuccess = SmsHandler.sendSms(phone, custVal.getCode());
//			if(!isSuccess){
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证操作过于频繁，请稍后再操作",result, this);
//			}
			
			//发送短信验证码
			String isSuccess = Util.sendSms(phone, custVal.getCode(), type);
			if(!"0".equals(isSuccess)){
				//发送短信
				boolean isOk = SmsHandler.sendSms(phone, custVal.getCode());
				if(!isOk){ 
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "您的操作过于频繁，请稍后重试",result, this);
				}
			}
			
			//保存验证信息
			custServ.saveCustomerValidate(custVal);
			//移动端网站不返回验证码
			if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) != MobileTypeEnum.wapmobile)) {
				result.put("validateNum", custVal.getCode());
			}
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取手机验证码成功",result, this);
		} catch (Exception e) {
			logger.error("getPhoneValidateNumber() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:更换手机验证</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult changePhoneValidateNum(int userId){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			if(reqBody.isNull("phone") || StringUtil.isNullOrEmpty(reqBody.getString("phone"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号不能为空！",result, this);
			}
			if(reqBody.isNull("validateNum") || StringUtil.isNullOrEmpty(reqBody.getString("validateNum"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不能为空！",result, this);
			}
			if(reqBody.isNull("type")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证类型不能为空！",result, this);
			}
			Customer cust = getCustomerById(userId);
			if(cust == null ){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			String validateNum = reqBody.getString("validateNum");
			String phone = reqBody.getString("phone");
//			int type  = reqBody.getInt("type");
			//校验11位手机号是否正确
			if(!VerifyClientCustomer.validateCellphone(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			
			//更换的新手机与原有验证的手机号一致
			if(cust.getPhoneValidate() != null && cust.getPhoneValidate() == 1
					&& cust.getMobilephone() != null && cust.getMobilephone().equals(phone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "更换的手机号不能与已验证的手机号相同！", null, this);
			}
			
			ForgetPasswordCode custValid = custServ.getPhoneValidate(phone, userId, 0,validateNum);
			
			if(custValid == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "验证码不正确，请重新获取验证！",result, this);
			}
			if(!isValidateNumValid(custValid.getStartTime(),VALIDATETIME)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, ErrorCode.RESP_INFO_PHONE_CODE_INVALID,result, this);
			}
			
			custServ.changeCustomerValidate(custValid, cust);;
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "校验手机验证成功",result, this);
		} catch (Exception e) {
			logger.error("changePhoneValidateNum() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	
	/**
	* <p>功能描述:构建验证信息</p>
	* <p>参数：@param cust
	* <p>参数：@param method
	* <p>参数：@param validateObj
	* <p>参数：@return</p>
	* <p>返回类型：ForgetPasswordCode</p>
	 */
	private ForgetPasswordCode buildCustomerValidate(Customer cust,int method,String validateObj,int type){
		ForgetPasswordCode custVaild = new ForgetPasswordCode();
		if(method == 1){
			custVaild.setCode(createRandom(VALIDATENUMLENGTH));
			custVaild.setEndTime(getEndTime(method));
		}else{
			custVaild.setCode(getSNCode());
			custVaild.setEndTime(getEndTime(method));
		}
		custVaild.setCustomer(cust);
		custVaild.setIsSuccess(0);
		custVaild.setMethod(method);
		custVaild.setStartTime(new Date());
		custVaild.setUserName(validateObj);
		custVaild.setType(type);
		return custVaild;
	}
	
	/**
	* <p>功能描述:获取sn码</p>
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String getSNCode(){
		Date d = new Date();
		String dateStr = DateFormatter.date2String(d, "yyyy-MM-dd HH:mm:ss");
		String sn = DESUtils.getEncryptString(dateStr);
		return sn;
	}
	
	/**
	* <p>功能描述:获取验证结束时间</p>
	* <p>参数：@param method
	* <p>参数：@return</p>
	* <p>返回类型：Date</p>
	 */
	private Date getEndTime(int method){
		Calendar cal = Calendar.getInstance();
		if(method == 1){
			cal.add(Calendar.MINUTE, 10);
		}else{
			cal.add(Calendar.HOUR, 24);
		}
		return cal.getTime();
	}
	
	/**
	* <p>功能描述:用户中心</p>
	* <p>参数：@param sc
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult customerCenter(){
		try {
			JSONObject result = new JSONObject();
			int userId = getContext().getHead().getUid();
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = custServ.getCustomerById(userId);
			if(cust == null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户不存在！",result, this);
			}
			CustomerVO vo = new CustomerVO(cust);
			//获取手机和邮箱是否被验证
			boolean emailValidate = (!StringUtil.isNullOrEmpty(cust.getEmail())&&cust.getEmailValidate()!= null&&cust.getEmailValidate() == 1)? true :false;
			boolean phoneValidate = (!StringUtil.isNullOrEmpty(cust.getMobilephone())&&cust.getPhoneValidate()!= null&&cust.getPhoneValidate() == 1)? true :false;
			vo.setEmailValidate(emailValidate);
			vo.setPhoneValidate(phoneValidate);
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			//获取用户会员信息
			JSONObject member = memberService.memberCheck_v2_5(userId);
			if(member != null){
				vo.setMemberType(member.optInt("memberType",0));
				vo.setEndTime(member.optString("endTime",""));
				vo.setVipEndTime(member.optString("vipEndTime",""));
			}
			//是否有密码标识 
			if(StringUtil.isNullOrEmpty(cust.getPassword())){
				vo.setHasPassword(false);
			}
			Gson gson = new Gson();
			result.put("userInfo", gson.toJson(vo));
			//添加统计数据
			Util.addStatistics(getContext(), vo);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取用户中心成功",result, this);
		} catch (Exception e) {
			logger.error("customerCenter() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:根据用户id获取相应用户</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Customer</p>
	 */
	private Customer getCustomerById(int userId) throws SqlException{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		return custServ.getCustomerById(userId);
	}
	
	/**
	* <p>功能描述:根据用户帐号获取相应用户</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Customer</p>
	 * @throws Exception 
	 */
	private Customer getCustomerByAccount(String account) throws Exception{
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		return custServ.getCustomerByAccount(account);
	}
	
	/**
	* <p>功能描述:生成指定长度的随机数</p>
	* <p>参数：@param length
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public String createRandom(int length) {
		String retNum = "";
		String validateNum = "1234567890";
		int len = validateNum.length();
		for (int i = 0; i < length; i++) {
			double randomNum = Math.random() * len;
			int num = (int) Math.floor(randomNum);
			retNum += validateNum.charAt(num);
		}
		
		return retNum;
	}
	
}
