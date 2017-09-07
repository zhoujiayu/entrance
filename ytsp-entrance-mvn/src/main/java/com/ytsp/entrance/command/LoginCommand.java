package com.ytsp.entrance.command;

import java.util.Date;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.CustomerLoginRecordDao;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.LoginService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.MD5;

/**
 * @author GENE
 * @description 用户注册
 * 
 */
public class LoginCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_LOGIN == code || CommandList.CMD_LOGOUT == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_LOGIN == code) {
				return login();

			} else if (CommandList.CMD_LOGOUT == code) {
				return logout();

			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}

	private ExecuteResult login() throws Exception {
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		if(jsonObj.isNull("account")||jsonObj.isNull("password")){
			logger.error("account or password not exist : "+jsonObj);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "请填写用户名和密码！", null, this);
		}
		String account = StringUtils.trim(jsonObj.getString("account"));
		String password = StringUtils.trim(jsonObj.getString("password"));

		if (StringUtil.isNullOrEmpty(account)) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "请填写用户名！", null, this);
		}
		
		if (StringUtil.isNullOrEmpty(password)) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "请填写密码！", null, this);
		}

		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		
		
		boolean exist = cs.existByAccount(account);
		if(!exist) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "用户名不存在！", null, this);
		}
		
		String md5Pwd = MD5.code(password.trim());
		final Customer customer = cs.findCustomerByAccountAndPassword(account, md5Pwd);
		
		if (customer == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "密码错误！", null, this);
		}

		customer.setRegisterIp(getContext().getHead().ip);
		customer.setTerminalType(getContext().getHead().getPlatform());
		customer.setTerminalVersion(getContext().getHead().getVersion());
		customer.setTerminalNumber(getContext().getHead().getUniqueId());
		customer.setRegisterProvince(IPSeeker.getAreaNameCHByIp(getContext().getHead().ip));
		customer.setCreateTime(new Date());
		
		//先判断是否有重复登录的session,如果有则把前一个失效
//		LoginService.inValidateSameSession(customer, getContext().getHead().getUniqueId(),getContext().getRequest().getSession().getId());
		
		SessionCustomer sc = new SessionCustomer(customer);
		getSession().setAttribute(IConstants.SESSION_CUSTOMER, sc);
		getSession().setMaxInactiveInterval(-1);
		LoginService.singleRegister(getSession(), customer);//登录
		LowPriorityExecutor.execLog(new Runnable() {
			@Override
			public void run() {
				//记录用户登录日志
				try {
					CustomerLoginRecordDao recordDao = SystemInitialization.getApplicationContext().getBean(CustomerLoginRecordDao.class);
					CustomerLoginRecord record = new CustomerLoginRecord();
					record.setCustomer(customer);
					record.setIp(getContext().getHead().ip);
					record.setTerminalType(getContext().getHead().getPlatform());
					record.setTerminalVersion(getContext().getHead().getVersion());
					record.setTime(new Date());
					record.setNumber(getContext().getHead().getUniqueId());
					String[] a = IPSeeker.getAreaNameByIp(record.getIp());
					record.setLoginProvince(a[0]);
					record.setLoginCity(a[1]);
					recordDao.save(record);
				} catch (Exception e) {
					logger.error("Log customer error : ",e);
				}
				
			}
		});
		JSONObject json = new JSONObject();
		json.put("uid", customer.getId());
		json.put("creadits", customer.getCredits());
		Baby baby = cs.getBabyByCustomerId(customer.getId());
		if(baby != null && baby.getBirthday() != null) {
			json.put("babyBirthday", DateFormatter.date2String(baby.getBirthday()));
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "登录成功！", json, this);
	}

	private ExecuteResult logout() {
		SessionCustomer sc = getSessionCustomer();
		if(sc != null){//注销单例登录
			LoginService.singleUnregister(getSession(), String.valueOf(sc.getCustomer().getId()));
		}
		getSession().removeAttribute(IConstants.SESSION_CUSTOMER);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "注销成功！", null, this);
	}

}
