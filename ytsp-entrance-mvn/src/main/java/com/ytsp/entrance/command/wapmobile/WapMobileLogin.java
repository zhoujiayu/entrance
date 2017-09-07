package com.ytsp.entrance.command.wapmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.wxpay.common.WXPayConfig;
import com.tencent.wxpay.common.WXUtil;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.CustomerLoginRecordDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.db.domain.CustomerThirdPlatform;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.errorcode.ErrorCode;
import com.ytsp.entrance.handleResponse.RestResponse;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.EbShoppingCartService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.service.v3_1.MemberServiceV31;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.service.v5_0.OrderServiceV5_0;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Base32;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.Md5Encrypt;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.weibo4j.Oauth;
import com.ytsp.entrance.weibo4j.http.AccessToken;
import com.ytsp.entrance.weixin.util.Article;
import com.ytsp.entrance.weixin.util.ArticleList;
import com.ytsp.entrance.weixin.util.Wxutil;

public class WapMobileLogin extends AbstractCommand{
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_WEBMOBILE_THIRD_PLATFORM_LOGIN == code
				||CommandList.CMD_WEBMOBILE_MINE_PAGE_ORDER_NUMBER == code
				||CommandList.CMD_WEBMOBILE_WX_THIRD_PLATFORM_LOGIN == code
				||CommandList.CMD_GET_WEIBO_ACCESSTOKEN==code
				||CommandList.CMD_WAPMOBILE_WX_CHECK_THIRD_PLAT_USER==code
				||CommandList.CMD__WX_FILE_LIST==code
				||CommandList.GET_WX_HD_ACCESSTOKEN==code;
				
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			
			if (CommandList.CMD_WEBMOBILE_THIRD_PLATFORM_LOGIN == code) {
				return webMobileThirdPlatformLogin();
			}
			if(CommandList.CMD_WEBMOBILE_MINE_PAGE_ORDER_NUMBER == code){
				return getMyOrderNumber();
			}
			if(CommandList.CMD_WEBMOBILE_WX_THIRD_PLATFORM_LOGIN == code){
				return webMobileTWxhirdPlatformLogin();
			}
			
			if(CommandList.CMD_GET_WEIBO_ACCESSTOKEN == code){
				return getWeiBoAccessToken();
			}
			if(CommandList.CMD_WAPMOBILE_WX_CHECK_THIRD_PLAT_USER == code){
				return checkWXThirdPlatUser();
			}
			if(CommandList.CMD__WX_FILE_LIST == code){
				return getWxFileList();
			}
			if(CommandList.GET_WX_HD_ACCESSTOKEN ==code){
				return getWxHdData();
			}
			
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	
	
	
	/**
	* <p>功能描述:获取微信图文文章列表</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getWxFileList(){
		RestResponse<Map<String,List<Article>>> respone = new RestResponse<Map<String,List<Article>>>();
		try {
//			ArticleList al  = new ArticleList();
			Map<String,List<Article>> resMap = new HashMap<String, List<Article>>();
			//初始化
			Wxutil.init();
			//获取access_token
			String access_token = Wxutil.getAccessToken();
			if(!"".equals(access_token)){
				Wxutil.getArticleList(access_token);
				resMap = Wxutil.resMap;
			}
			respone.setVo(resMap);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取微信图文文章列表", respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("getWxFileList() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 微信活动 母亲节 调用微信接口获取数据
	 */
	
	private ExecuteResult getWxHdData(){
		JSONObject reqBody = getContext().getBody().getBodyObject();
		String url = "";
		String ticket = "";
		try {
			if(!reqBody.isNull("url")){
				url = reqBody.getString("url");
			}else{
				logger.error("url参数没有传递 : " + reqBody);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"第三方平台登录授权信息不全！", null, this);
			}
			JSONObject result = new JSONObject();
			String access_token = Wxutil.getHDAccessToken();
			
			if(!"".equals(access_token)){//获取access_token
				ticket = Wxutil.getHDTicket(access_token);
				if(!"".equals(ticket)){
					result = Wxutil.sign(ticket, url);
				}else{
					result = null;
				}
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"调用微信接口成功", result, this);
		} catch (Exception e) {
			logger.error("getWxHdData() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:微信校验第三方登录用户是否存在</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkWXThirdPlatUser(){
		try {
			String key = (String) getContext().getRequest().getSession(true)
					.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
			if (key == null)
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
						null, this);
			JSONObject reqBody = getContext().getBody().getBodyObject();
			
			if (reqBody.isNull("code")) {
				logger.error("平台用户授权信息不全 : " + reqBody);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"第三方平台登录授权信息不全！", null, this);
			}
			Map<String,Object> result = new HashMap<String, Object>();
			RestResponse<Map<String,Object>> respone = new RestResponse<Map<String,Object>>();
			CustomerService cs = SystemInitialization.getApplicationContext()
					.getBean(CustomerService.class);
			
			//微信的code,该code只能使用1次,若再次使用会获取不到用信息
			String code = reqBody.optString("code");
			String unionId = "";
			String openId = "";
			String accessToken = "";
			
			//获取code,accessToken和openId获取微信用户信息
			JSONObject wxResult =  WXUtil.getWxAccessToken(code);
			JSONObject wxUserInfo = null;
			if (wxResult != null) {
				if (!wxResult.has("errcode")) {
					//1、获取普通access_token 2、获取成功 根据openid 和 accesstoken 获取用户基本信息
					JSONObject accessInfo = WXUtil.getWxNormalAccessToken();
					if (accessInfo != null && !accessInfo.has("errcode")) {
						if(accessInfo.has("access_token")){
							accessToken = accessInfo.optString("access_token");
						}
						if(wxResult.has("openid")){
							openId = wxResult.optString("openid");
						}
						wxUserInfo = WXUtil.getWxUserInfo(accessToken, openId);
					}else{
						logger.error("获取普通access_token错误 :code: " + code);
					}
				} else {
					logger.error("获取access_token错误 :code: " + code);
				}
			}
			
			//未获取到微信用户信息
			if (wxUserInfo == null || wxUserInfo.has("errcode")) {
				respone.setRetCode(ErrorCode.RESP_CODE_OBTAIN_WX_USERINFO_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_OBTAIN_WX_USERINFO_ERROR);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_OBTAIN_WX_USERINFO_ERROR,
						respone.convertJSONObject(), this);
			}
			unionId = wxUserInfo.optString("unionid");
			
			if (StringUtil.isNullOrEmpty(unionId) || StringUtil.isNullOrEmpty(openId)) {
				respone.setRetCode(ErrorCode.RESP_CODE_OBTAIN_WX_USERINFO_ERROR);
				respone.setRetInfo(ErrorCode.RESP_INFO_OBTAIN_WX_USERINFO_ERROR);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						ErrorCode.RESP_INFO_OBTAIN_WX_USERINFO_ERROR,
						respone.convertJSONObject(), this);
			}
			
			//获取第三方平台信息
			CustomerThirdPlatform customerTP = getCustomerThirdPlatform("997", unionId, openId);
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
			if(wxUserInfo.has("nickname")){
				result.put("nick", wxUserInfo.optString("nickname"));
				result.put("figureurl", wxUserInfo.optString("headimgurl"));
			}else{
				result.put(
						"nick",
						customerTP == null || customerTP.getCustomer() == null ? ""
								: customerTP.getCustomer().getAccount());
				result.put("figureurl", "");
			}
			result.put("openId", openId);
			result.put("unionId", unionId);
			result.put("token", accessToken);
			respone.setVo(result);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取第三方登录用户成功", respone.convertJSONObject(), this);
		} catch (Exception e) {
			logger.error("checkPhoneValidateCode() error," + " HeadInfo :"
					+ getContext().getHead().toString()+" \n reqBody:"+getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}
	
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
	* <p>功能描述:获取第三方登录信息</p>
	* <p>参数：@param platformName
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
	
	private JSONObject  getHttpClientResult(String url,String params) throws JSONException{
		JSONObject result = null;
		StringBuffer response = new StringBuffer();
		HttpClient client = new HttpClient();
		try {
			URL website = new URL(url+params);
			InputStream in = website.openStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			if(!"".equals(response.toString())){
				result = new JSONObject(response.toString());
			}
			
		} catch (IOException e) {
			
		} finally {
			
		}
		return result;
	}
	
	private ExecuteResult getWeiBoAccessToken()throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		if (reqBody.isNull("code") ) {
			logger.error("code获取失败！: " + reqBody);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"请先获取code！", null, this);
		}
		JSONObject obj = new JSONObject();
		String code = reqBody.optString("code");
		String info = "";
		try {
			Oauth oauth = new Oauth();
			AccessToken  accesstion  = oauth.getAccessTokenByCode(code);
			if(accesstion!=null){
				info = accesstion.toString();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		if("".equals(info)||info==null ){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "获取accesstoken失败！",null,
					this);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取accesstoken成功！",new JSONObject(info),
				this);
	}
	
	private ExecuteResult webMobileTWxhirdPlatformLogin() throws Exception {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		// String key = (String) getContext().getRequest().getSession(true)
		// .getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
		// if (key == null)
		// return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
		// null, this);

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		if (jsonObj.isNull("code")) {
			logger.error("code获取失败！: " + jsonObj);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"请先获取code！", null, this);
		}

		String params = "appid=" + WXPayConfig.WAP_APP_ID + "&secret="
				+ WXPayConfig.APP_SECRET + "&code="
				+ StringUtils.trim(reqBody.optString("code"))
				+ "&grant_type=authorization_code";
		JSONObject result = getHttpClientResult(
				WXPayConfig.OAUTH_ACCESS_TOKEN_API, params);
		JSONObject userInfo = null;
		if (result != null) {
			/**
			 * 1、获取普通access_token 2、获取成功 根据openid 和 accesstoken 获取用户基本信息
			 */
			if (!result.has("errcode")) {
				String paramsAccess = "grant_type=client_credential&appid="
						+ WXPayConfig.WAP_APP_ID + "&secret="
						+ WXPayConfig.APP_SECRET;
				JSONObject accessInfo = getHttpClientResult(
						WXPayConfig.ACCESS_TOKEN_API, paramsAccess);
				if (accessInfo != null && !accessInfo.has("errcode")) {
					String paramsUserInfo = "access_token="
							+ accessInfo.get("access_token") + "&openid="
							+ result.get("openid") + "&lang=zh_CN";
					userInfo = getHttpClientResult(WXPayConfig.USER_INFO_API,
							paramsUserInfo);
				}else{
					logger.error("获取access_token错误 : " + reqBody);
				}
			} else {
				logger.error("获取access_token错误 : " + reqBody);
				String errorMsg = "获取access_token错误！";
				if ("40029".equals(result.get("errcode"))) {
					errorMsg = "code失效！";
				}
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						errorMsg, null, this);
			}

		}
		if (userInfo != null && !userInfo.has("errcode")) {
			String platformName = "997";

			CustomerService cs = SystemInitialization.getApplicationContext()
					.getBean(CustomerService.class);

			CustomerThirdPlatform customerTP = null;
			boolean flag = false;
			CustomerThirdPlatform tp = cs
					.findCustomerThirdPlatformByUninoId(StringUtils
							.trim(userInfo.optString("unionid")));
			if (tp == null) {
				customerTP = cs.findCustomerByThirdPlatform(platformName,
						userInfo.get("openid").toString());
				flag = true;
			} else {
				customerTP = tp;
			}

			String account = "";
			if (customerTP == null) {
				Customer customer = new Customer();
				customer.setAccount("ikan_"+generateAccount(userInfo
						.getString("unionid")));
				customer.setCreateTime(new Date());
				customer.setTerminalNumber(getContext().getHead().getUniqueId());
				customer.setTerminalType(getContext().getHead().getPlatform());
				customer.setTerminalVersion(getContext().getHead().getVersion());
				customer.setRegisterProvince(IPSeeker
						.getAreaNameCHByIp(getContext().getHead().ip));
				customer.setRegisterIp(getContext().getHead().ip);
				cs.saveCustomer(customer);
				customerTP = new CustomerThirdPlatform();
				customerTP.setCustomer(customer);
				customerTP.setPlatform_name(platformName);
				customerTP.setUser_id(userInfo.get("openid").toString());
				customerTP.setUnion_id(StringUtils.trim(userInfo
						.getString("unionid")));
				cs.createCustomerThirdPlatform(customerTP);
			} else {
				if (flag) {
					cs.updateUnionId(customerTP.getUser_id(),
							StringUtils.trim(userInfo.getString("unionid")));
				}
			}
			final Customer customer = customerTP.getCustomer();
			if (customer == null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"服务错误：授权账户在ikan无对应信息！", null, this);
			}
			account = customer.getAccount();//微信三方登陆获取生成账号
			SessionCustomer sc = new SessionCustomer(customer);
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			String sessionId = sessionService.signIn(customer.getId());
			// TODO　合并购物车
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			String cartId = getContext().getHead().getCartId();
			shoppingCartService.updateShoppingCartByLogin(customer.getId(),
					cartId);
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
						record.setTerminalType(getContext().getHead()
								.getPlatform());
						record.setTerminalVersion(getContext().getHead()
								.getVersion());
						record.setTime(new Date());
						record.setNumber(getContext().getHead().getUniqueId());
						String[] a = IPSeeker.getAreaNameByIp(record.getIp());
						record.setLoginProvince(a[0]);
						record.setLoginCity(a[1]);
						recordDao.save(record);
						Util.addStatistics(getContext(), record);
					} catch (Exception e) {
						logger.error("Log customer error : ", e);
					}

				}
			});
			JSONObject json = new JSONObject();
			json.put("uid", customer.getId());
			json.put("credits", customer.getCredits());
			json.put("sessionId", sessionId);
			MemberService memberServ = SystemInitialization
					.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberServ.memberCheck_v2_5(customer.getId());
			int memberType = obj.optInt("memberType");
			json.put("isMember", memberType == 1 ? true : false);
			json.put("memberType", memberType);
			json.put("userAccount", account);
			if (memberType == 1) {
				json.put("vipEndTime", obj.optString("endTime"));
			}
			if(userInfo!=null && !userInfo.has("errcode") && userInfo.has("nickname")){
				json.put("nick", userInfo.optString("nickname"));
				json.put("figureurl", userInfo.optString("headimgurl"));
			}else{
				json.put("nick", account);
				json.put("figureurl", "");
			}
			json.put("userCreateTime", DateFormatter.date2String(
					customer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功！",
					json, this);
		} else {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取用户信息失败！", null, this);
		}
	}
	
	private String generateAccount(String uid){
		uid = uid.substring(6, uid.length());
	    byte[] uidChar = uid.getBytes();
	    char timeChar = (char) ((new Date().getTime()) % 0xffff);
	    byte[] last2 = Md5Encrypt.char2byte((char) ((timeChar) % 0xffff));
	    byte[] input = new byte[10];
	    for (int i = 0; i < 8; i++) {
			if (i % 2 == 0)
				input[i] = uidChar[i / 2];
			else
				input[i] = uidChar[(i + 1) / 2 - 1];
		}
	    
	    for (int j = 0; j < 2; j++) {
			input[8 + j] = last2[j];
		}
	    return Base32.encode(input);
	}
	
	/**
	* <p>功能描述:我的界面获取待付款、待收货、待评论订单数量</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getMyOrderNumber() throws Exception {
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}
		JSONObject result = new JSONObject();
		result.put("waitPayNum", getMyOrderQuantityByType(userId, 1));
		result.put("waitGoodsNum", getMyOrderQuantityByType(userId, 2));
		result.put("waitCommentNum", getMyOrderQuantityByType(userId, 3));
		MemberService memberServ = SystemInitialization
				.getApplicationContext().getBean(MemberService.class);
		JSONObject obj = memberServ.memberCheck_v2_5(customer.getId());
		int memberType = obj.optInt("memberType");
		if(memberType == 1){
			result.put("endTime", obj.optString("endTime"));
			result.put("vipEndTime", obj.optString("vipEndTime"));
		}
		result.put("credits", obj.optInt("credits"));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取订单数量成功！", result,
				this);
	}
	
	/**
	* <p>功能描述:获取某种状态订单的数量</p>
	* <p>参数：@param userId
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<OrderVO></p>
	 */
	private int getMyOrderQuantityByType(int userId,int type) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		return orderService.getMyOrderCountByType(userId, type);
	}
	
	/**
	* <p>功能描述:移动端网站第三方登录</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult webMobileThirdPlatformLogin() throws Exception {
//		String key = (String) getContext().getRequest().getSession(true)
//				.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
//		if (key == null)
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
//					null, this);
		JSONObject reqBody = getContext().getBody().getBodyObject();
		if (reqBody.isNull("token") || reqBody.isNull("uid")
				|| reqBody.isNull("type")) {
			logger.error("平台用户授权信息不全 : " + reqBody);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"请先用第三方平台登录授权！", null, this);
		}
		String token = StringUtils.trim(reqBody.optString("token"));
		String platformName = StringUtils.trim(reqBody
				.optString("type"));
		String userId = StringUtils.trim(reqBody.optString("uid"));
		
		CustomerService cs = SystemInitialization.getApplicationContext()
				.getBean(CustomerService.class);

		CustomerThirdPlatform customerTP = cs.findCustomerByThirdPlatform(
				platformName, userId);
		if (customerTP == null) {
			Customer customer = new Customer();
			customer.setCreateTime(new Date());
			customer.setTerminalNumber(getContext().getHead().getUniqueId());
			customer.setTerminalType(getContext().getHead().getPlatform());
			customer.setTerminalVersion(getContext().getHead().getVersion());
			customer.setRegisterProvince(IPSeeker
					.getAreaNameCHByIp(getContext().getHead().ip));
			customer.setRegisterIp(getContext().getHead().ip);
			cs.saveCustomer(customer);
			customerTP = new CustomerThirdPlatform();
			customerTP.setCustomer(customer);
			customerTP.setPlatform_name(platformName);
			customerTP.setUser_id(userId);
			customerTP.setToken(token);
			cs.createCustomerThirdPlatform(customerTP);
		}
		final Customer customer = customerTP.getCustomer();
		if (customer == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"服务错误：授权账户在ikan无对应信息！", null, this);
		}
		SessionCustomer sc = new SessionCustomer(customer);
		getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
		String sessionId = sessionService.signIn(customer.getId());
		// TODO　合并购物车
		EbShoppingCartService shoppingCartService = SystemInitialization
				.getApplicationContext().getBean(EbShoppingCartService.class);
		String cartId = getContext().getHead().getCartId();
		shoppingCartService.updateShoppingCartByLogin(customer.getId(), cartId);
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
					Util.addStatistics(getContext(), record);
				} catch (Exception e) {
					logger.error("Log customer error : ", e);
				}

			}
		});
		JSONObject json = new JSONObject();
		json.put("uid", customer.getId());
		json.put("credits", customer.getCredits());
		json.put("sessionId", sessionId);
		MemberServiceV31 memberService = SystemInitialization
				.getApplicationContext().getBean(MemberServiceV31.class);
		boolean isMember = memberService.isMember(customer.getId());
		json.put("isMember", isMember);
		json.put("memberType", isMember? 1 : 0);
		//若为QQ登录获取用户信息
		if(platformName.equals("6")){
			JSONObject qqUserInfo = getQQUserInfo(IConstants.QQ_APP_ID, userId, token);
			if(qqUserInfo != null && qqUserInfo.getInt("ret") == 0){
				json.put("nick", qqUserInfo.optString("nickname"));
				json.put("figureurl", qqUserInfo.optString("figureurl_qq_2"));
			}
		}
		json.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功！", json,
				this);
	}
	
	/**
	* <p>功能描述:获取QQ用户信息</p>
	* <p>参数：@param appId
	* <p>参数：@param openid
	* <p>参数：@param token
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private JSONObject getQQUserInfo(String appId,String openid,String token){
		String url = IConstants.QQ_GET_USER_INFO;
		if (StringUtil.isNullOrEmpty(appId) || StringUtil.isNullOrEmpty(openid)
				|| StringUtil.isNullOrEmpty(token)) {
			return null;
		}
		JSONObject userInfo = new JSONObject();
		url = url.replaceAll("ACCESS_TOKEN", token);
		url = url.replaceAll("OAUTH_CONSUMER_KEY", appId);
		url = url.replaceAll("OPENID", openid);
		try {
			userInfo = getHttpClientResult(url, "");
		} catch (JSONException e) {
			logger.error("获取QQ用户信息错误 : " + userInfo);
		}
		return userInfo;
	}
	
}
