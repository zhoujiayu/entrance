package com.ytsp.entrance.command;

import java.util.Date;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.Parent;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.HardwareRegisterService;
import com.ytsp.entrance.service.LoginService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.MD5;
import com.ytsp.entrance.util.VerifyClientCustomer;

/**
 * @author GENE
 * @description 用户注册
 * 
 */
public class RegistCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_REGIST == code
				|| CommandList.CMD_MODIFY_PWD == code 
				|| CommandList.CMD_REGIST_DEVICE == code 
				|| CommandList.CMD_REGIST_DEVICE_TOKEN == code;
	}
	
	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_REGIST == code) {
				return regist();
			} else if (CommandList.CMD_MODIFY_PWD == code) {
				return modifyPwd();
			} else if (CommandList.CMD_REGIST_DEVICE == code) {
				return registerHardWare();
			} else if (CommandList.CMD_REGIST_DEVICE_TOKEN == code) {
				return registerDeviceToken();
			}
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}

		return null;
	}

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
			String email = null;
			try{
				email = StringUtils.trim(jsonObj.getString("email"));
			}catch(Exception ex){
			}

			if (StringUtil.isNullOrEmpty(account) || StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户或密码不能为空！", null, this);
			}
			
//			if(!StringUtil.checkAccount(account)){
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户名称不合法！", null, this);
//			}
//			
			if(!password.equals(password2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "重复密码与密码不一致！", null, this);
			}
			
			if(!VerifyClientCustomer.accountValidate(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户名只能为邮箱地址或者长度4-30（中文算2个字符）的中文、数字、字母、下划线的组合！", null, this);
			}
			
			if(!VerifyClientCustomer.passwordValidate(password)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户密码必须是长度4-20的英文或数字！", null, this);
			}
			
			if(!VerifyClientCustomer.emailValidate(email)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
			}

			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			if (cs.existByAccount(account)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "该账户已注册过！", null, this);
			}
			
			String md5Pwd = MD5.code(password.trim());

			Customer customer = new Customer();
			customer.setAccount(account);
			customer.setPassword(md5Pwd);
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
			//创建一条家长信息
			Parent parent = new Parent();
			parent.setCustomer(customer);
			parent.setEmail(email);
			cs.saveOrUpdateParent(parent);

			HardwareRegisterService hrs = SystemInitialization.getApplicationContext().getBean(HardwareRegisterService.class);
			hrs.saveByNumber(hardwareId, otherInfo,platform,version,appDiv,ip);
			
			SessionCustomer sc = new SessionCustomer(customer);
			getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
			LoginService.singleRegister(getSession(), customer);//单例登录

			JSONObject json = new JSONObject();
			json.put("uid", customer.getId());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "账户注册成功！", json, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	public ExecuteResult modifyPwd() {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String oldpassword = jsonObj.getString("oldpassword");
			String password = jsonObj.getString("password");
			String password2 = jsonObj.getString("password2");

			if (StringUtil.isNullOrEmpty(oldpassword)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "旧密码不能为空！", null, this);
			}
			
			if (StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "新密码不能为空！", null, this);
			}
			
			if(!password.equals(password2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "重复密码与密码不一致！", null, this);
			}

			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			
			Customer customer = sc.getCustomer();
			String oldMd5Pwd = MD5.code(oldpassword.trim());
			if(!oldMd5Pwd.equals(customer.getPassword())){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "旧密码不正确！", null, this);
			}
			
			//校验
			if(!VerifyClientCustomer.passwordValidate(password)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户密码必须是长度4-20的英文或数字！", null, this);
			}
			
			String md5Pwd = MD5.code(password.trim());
			customer.setPassword(md5Pwd);
			cs.updateCustomer(customer);

			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "账户密码修改成功！", null, this);
		} catch (Exception e) {
			logger.error("execute command error!", e);
			return getExceptionExecuteResult(e);
		}
	}

	/**@deprecated*/
	public ExecuteResult registerHardWare() {
		try {
			//注册硬件信息
			String hardwareId = getContext().getHead().getUniqueId();
			String otherInfo = getContext().getHead().getOtherInfo();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();
			HardwareRegisterService hrs = SystemInitialization.getApplicationContext().getBean(HardwareRegisterService.class);
			hrs.saveByNumber(hardwareId, otherInfo,platform,version,appDiv,ip);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "注册硬件成功！", null, this);
		} catch (Exception e) {
			logger.error("execute command error!", e);
			return getExceptionExecuteResult(e);
		}
	}
	
	public ExecuteResult registerDeviceToken() {
		try {
			String number = getContext().getHead().getUniqueId();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String token = null;
			if(jsonObj!=null&&!jsonObj.isNull("deviceToken"))
				token = jsonObj.getString("deviceToken");
			HardwareRegisterService hrs = SystemInitialization.getApplicationContext().getBean(HardwareRegisterService.class);
			hrs.saveRegisterDeviceToken(number, token);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "注册DeviceToken成功！", null, this);
		} catch (Exception e) {
			logger.error("execute command error!", e);
			return getExceptionExecuteResult(e);
		}
	}
}
