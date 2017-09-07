package com.ytsp.entrance.command.v4_0;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.CustomerLoginRecordDao;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.db.domain.CustomerThirdPlatform;
import com.ytsp.db.domain.Parent;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.errorcode.ErrorCode;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.EbShoppingCartService;
import com.ytsp.entrance.service.HardwareRegisterService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.AesEncrypt;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.Util;

/**
 * @description 第三方用户登录
 * 
 */
public class LoginCommandv4_0 extends AbstractCommand {
	
	                               
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_THIRD_PLATFORM_LOGIN == code
				|| CommandList.CMD_LOGIN_4_0 == code
				|| CommandList.CMD_LOGOUT_4_0 == code
				|| CommandList.CMD_THIRD_PLATFORM_LOGIN_KEY == code
				|| CommandList.CMD_MODIFY_PASSWD_4_0 == code
				|| CommandList.CMD_REGIST_4_0 == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_LOGIN_4_0 == code) {
				return login4_0();
			}
			if (CommandList.CMD_THIRD_PLATFORM_LOGIN == code) {
				return login();
			}
			if (CommandList.CMD_LOGOUT_4_0 == code) {
				return logout4_0();
			}
			if (CommandList.CMD_THIRD_PLATFORM_LOGIN_KEY == code) {
				return loginKey();
			}
			if (CommandList.CMD_MODIFY_PASSWD_4_0 == code) {
				return modifyPasswd();
			}
			if (CommandList.CMD_REGIST_4_0 == code) {
				return regist();
			}
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult regist() {
		try {
			// 注册硬件信息
			String hardwareId = getContext().getHead().getUniqueId();
			String otherInfo = getContext().getHead().getOtherInfo();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String nick = StringUtils.trim(jsonObj.getString("nick"));
			String account = StringUtils.trim(jsonObj.getString("account"));
			String password = StringUtils.trim(jsonObj.getString("password"));
			String password2 = StringUtils.trim(jsonObj.getString("password2"));
			String email = null;
			try {
				email = StringUtils.trim(jsonObj.getString("email"));
			} catch (Exception ex) {
			}

			if (StringUtil.isNullOrEmpty(account)
					|| StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"账户或密码不能为空！", null, this);
			}
			if (!password.equals(password2)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"重复密码与密码不一致！", null, this);
			}
			CustomerService cs = SystemInitialization.getApplicationContext()
					.getBean(CustomerService.class);
			if (cs.existByAccount(account)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"该账户已注册过！", null, this);
			}
			Customer customer = new Customer();
			customer.setAccount(account);
			customer.setPassword(password);
			customer.setNick(nick);
			customer.setCreateTime(new Date());
			customer.setRegisterIp(ip);
			customer.setTerminalType(platform);
			customer.setTerminalVersion(version);
			customer.setTerminalNumber(hardwareId);
			String[] a = IPSeeker.getAreaNameByIp(ip);
			customer.setRegisterProvince(a[0]);
			customer.setRegisterCity(a[1]);
			cs.saveCustomer(customer);
			// 创建一条家长信息
			Parent parent = new Parent();
			parent.setCustomer(customer);
			parent.setEmail(email);
			cs.saveOrUpdateParent(parent);
			HardwareRegisterService hrs = SystemInitialization
					.getApplicationContext().getBean(
							HardwareRegisterService.class);
			hrs.saveByNumber(hardwareId, otherInfo, platform, version, appDiv,
					ip);
			JSONObject json = new JSONObject();
			json.put("uid", customer.getId());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "账户注册成功！",
					json, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult modifyPasswd() {
		int uid = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (uid == 0 || customer.getId().intValue() != uid) {
			return getNoPermissionExecuteResult();
		}
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String oldpassword = jsonObj.getString("oldpassword");
			String password = jsonObj.getString("password");
			String password2 = jsonObj.getString("password2");
			if (StringUtil.isNullOrEmpty(oldpassword)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"旧密码不能为空！", null, this);
			}
			if (StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"新密码不能为空！", null, this);
			}
			if (!password.equals(password2)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"重复密码与密码不一致！", null, this);
			}
			CustomerService cs = SystemInitialization.getApplicationContext()
					.getBean(CustomerService.class);
			if (!oldpassword.equals(customer.getPassword())) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"旧密码不正确！", null, this);
			}
			customer.setPassword(password);
			cs.updateCustomer(customer);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"账户密码修改成功！", null, this);
		} catch (Exception e) {
			logger.error("execute command error!", e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult loginKey() throws JSONException {
		String uuid = UUID.randomUUID().toString();
		getContext().getRequest().getSession(true)
				.setAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY", uuid);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取登录key成功！",
				new JSONObject().put("logKey", uuid), this);
	}

	private ExecuteResult logout4_0() throws BeansException, Exception {
		int uid = getContext().getHead().getUid();
		String sessionId = getContext().getHead().getSessionId();
		if (sessionService.isSignIn(sessionId, uid))
			sessionService.signOut(sessionId, uid);
		getSession().removeAttribute(IConstants.SESSION_CUSTOMER);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "注销成功！", null,
				this);
	}

	/**
	 * @description 合并购物车
	 * @return
	 * @throws Exception
	 */
	private ExecuteResult login4_0() throws Exception {
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
					"用户名或密码错误", null, this);
		}
		final Customer customer = cs.findCustomerByAccountAndPassword(account,
				password.trim());

		if (customer == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"用户名或密码错误", null, this);
		}
		
		
		// customer.setCreateTime(new Date());
		SessionCustomer sc = new SessionCustomer(customer);
		String sessionId = sessionService.signIn(customer.getId());
		getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
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
					//统计登录
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
		json.put("nick", customer.getNick());
		//用户名
		json.put("userAccount", customer.getAccount());
		//用户注册时间
		json.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
		//获取用户是否会员
//		MemberServiceV31 memberService = SystemInitialization
//				.getApplicationContext().getBean(MemberServiceV31.class);
//		boolean isMember = memberService.isMember(customer.getId());
		MemberService memberServ = SystemInitialization
				.getApplicationContext().getBean(MemberService.class);
		JSONObject obj = memberServ.memberCheck_v2_5(customer.getId());
		int memberType = obj.optInt("memberType");
		json.put("isMember", memberType == 1?true : false);
		json.put("memberType", memberType);
		if(memberType == 1){
			json.put("vipEndTime", obj.optString("endTime"));
		}
		
		Baby baby = cs.getBabyByCustomerId(customer.getId());
		if (baby != null && baby.getBirthday() != null) {
			json.put("babyBirthday",
					DateFormatter.date2String(baby.getBirthday()));
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功！", json,
				this);
	}
	

	/**
	 * @description 合并购物车
	 * @return
	 * @throws Exception
	 */
	private ExecuteResult login() throws Exception {
		//设置统计多个对象
		if(getContext().getStatistics() != null){
			getContext().getStatistics().setMult(true);
		}
		String key = (String) getContext().getRequest().getSession(true)
				.getAttribute("CMD_THIRD_PLATFORM_LOGIN_KEY");
		if (key == null)
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "登录失败！",
					null, this);
		JSONObject jsonObj = new JSONObject(AesEncrypt.decrypt(key,
				getContext().getBody().getBodyObject().get("thirdPartJson")
						.toString()));
		if (jsonObj.isNull("token") || jsonObj.isNull("platformName")
				|| jsonObj.isNull("uid")) {
			logger.error("平台用户授权信息不全 : " + jsonObj);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"请先用第三方平台登录授权！", null, this);
		}
		String token = StringUtils.trim(jsonObj.getString("token"));
		String platformName = StringUtils.trim(jsonObj
				.getString("platformName"));
		String userId = StringUtils.trim(jsonObj.getString("uid"));// 在第三方平台的ID，加上平台就可以标示一个唯一用户
		if (platformName.toLowerCase().equals("sinaweibo")) {
			platformName = "1";
		} else if (platformName.toLowerCase().equals("qzone")
				|| platformName.toLowerCase().equals("qq")) {
			platformName = "6";
		} else if (platformName.toLowerCase().equals("wechat")) {
			platformName = "997";
		}
		CustomerService cs = SystemInitialization.getApplicationContext()
				.getBean(CustomerService.class);

		/*
		 * 1老版本与新版本区分  unionId 是否有这个key 
		 */
		CustomerThirdPlatform customerTP = null;
		boolean flag = false;
		if("997".equals(platformName)&&jsonObj.has("unionId")){//微信登陆 新版 并且传递unionId
			CustomerThirdPlatform tp = cs.findCustomerThirdPlatformByUninoId(StringUtils.trim(jsonObj.getString("unionId")));
			if(tp==null){
					customerTP = cs.findCustomerByThirdPlatform(
						platformName, userId);
					flag = true;
			}else{
				     customerTP  = tp; 
			}
		}else{
			customerTP = cs.findCustomerByThirdPlatform(
					platformName, userId);
		}
		
		if (customerTP == null) {
			Customer customer = new Customer();
			customer.setCreateTime(new Date());
			customer.setTerminalNumber(getContext().getHead().getUniqueId());
			customer.setTerminalType(getContext().getHead().getPlatform());
			customer.setTerminalVersion(getContext().getHead().getVersion());
			customer.setRegisterProvince(IPSeeker
					.getAreaNameCHByIp(getContext().getHead().ip));
			customer.setRegisterIp(getContext().getHead().ip);
			// 第三方的昵称变更比较频繁，对于我们来说意义不大，不再存储
			// customer.setNick(getContext().getBody().getBodyObject()
			// .optString("account", ""));
			cs.saveCustomer(customer);
			Util.addStatistics(getContext(), customer);
			customerTP = new CustomerThirdPlatform();
			customerTP.setCustomer(customer);
			customerTP.setPlatform_name(platformName);
			customerTP.setUser_id(userId);
			customerTP.setToken(token);
			if("997".equals(platformName)&&jsonObj.has("unionId")){
				customerTP.setUnion_id(StringUtils.trim(jsonObj.getString("unionId")));
			}
			cs.createCustomerThirdPlatform(customerTP);
			Util.addStatistics(getContext(), customerTP);
		}else{
			if(flag){
				cs.updateUnionId(customerTP.getUser_id(),StringUtils.trim(jsonObj.getString("unionId")));
			}
		}
		// Long now = new Date().getTime();
		// if (customerTP.getToken().equals(token)
		// || customerTP.getExpires_time() <= now) {
		// return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
		// "授权token错误或授权token已过期", null, this);
		// }
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
//		MemberServiceV31 memberService = SystemInitialization
//				.getApplicationContext().getBean(MemberServiceV31.class);
//		boolean isMember = memberService.isMember(customer.getId());
//		json.put("isMember", isMember);
//		json.put("memberType", isMember? 1 : 0);
		MemberService memberServ = SystemInitialization
				.getApplicationContext().getBean(MemberService.class);
		JSONObject obj = memberServ.memberCheck_v2_5(customer.getId());
		int memberType = obj.optInt("memberType");
		json.put("isMember", memberType == 1?true : false);
		json.put("memberType", memberType);
		if(memberType == 1){
			json.put("vipEndTime", obj.optString("endTime"));
		}
		
		Baby baby = cs.getBabyByCustomerId(customer.getId());
		if (baby != null && baby.getBirthday() != null) {
			json.put("babyBirthday",
					DateFormatter.date2String(baby.getBirthday()));
		}
		json.put("userCreateTime", DateFormatter.date2String(customer.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功！", json,
				this);
	}
}
