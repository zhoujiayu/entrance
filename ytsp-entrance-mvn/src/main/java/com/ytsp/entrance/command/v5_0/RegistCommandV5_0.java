package com.ytsp.entrance.command.v5_0;

import java.util.Date;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Customer;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.LoginService;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.MD5;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.ValidateUtil;
import com.ytsp.entrance.util.VerifyClientCustomer;

/**
 * 用户注册
 * 
 */
public class RegistCommandV5_0 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_CUSTOMER_REGIST == code;
	}
	
	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (CommandList.CMD_CUSTOMER_REGIST == code) {
				return regist();
			}
		}  catch (Exception e) {
			logger.info("RegistCommandV5_0:" + code + " 失败 " + e.getMessage());
			return getExceptionExecuteResult(e);
		}

		return null;
	}
	
	/**
	 * 入参：nick,account,password,password2,email,phone
	* <p>功能描述:用户注册</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	public ExecuteResult regist() {
		try {
			//注册硬件信息
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
			String email = StringUtils.trim(jsonObj.getString("email"));
			String mobilePhone = StringUtils.trim(jsonObj.getString("phone"));
			
			if(!VerifyClientCustomer.accountValidateV5_0(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户名只能为邮箱地址或者长度6-20（中文算2个字符）的中文、数字、字母、下划线的组合！", null, this);
			}
			
			if (StringUtil.isNullOrEmpty(account) || StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户或密码不能为空！", null, this);
			}
			
			if(!password.equals(password2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "确认密码与密码不一致！", null, this);
			}
			
			//校验11位手机号是否正确
			if(!ValidateUtil.isMoblie(mobilePhone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
			}
			
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
			}
			
			CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer cust = custServ.getCustomerByAccount(account);
			if (cust != null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_REGIST_ACCOUNT_EXIST, "用户名已被占用", null, this);
			}
			//手机 号已被验证校验
			if(custServ.isMobilePhoneExist(mobilePhone)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "该手机号已注册", null, this);
			}
			//校验邮箱是否被注册过
			if(custServ.getCustomerByEmail(email) != null){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "该邮箱已被注册", null, this);
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
			customer.setMobilephone(mobilePhone);
			customer.setEmail(email);
			//注册用户
			custServ.saveCustomerRegist(customer, otherInfo, appDiv);
			//将用户信息放入到session中
			SessionCustomer sc = new SessionCustomer(customer);
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			LoginService.singleRegister(getSession(), customer);//单例登录

			JSONObject json = new JSONObject();
			json.put("uid", customer.getId());
			//添加统计信息
			Util.addStatistics(getContext(), customer);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "账户注册成功！", json, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
}
