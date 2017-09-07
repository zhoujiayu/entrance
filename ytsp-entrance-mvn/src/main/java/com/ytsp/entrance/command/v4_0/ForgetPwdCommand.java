package com.ytsp.entrance.command.v4_0;

import java.util.Date;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.ForgetPasswordCode;
import com.ytsp.db.domain.Parent;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.MD5;
import com.ytsp.entrance.util.RandomUtil;
import com.ytsp.entrance.util.mail.MailFacade;
import com.ytsp.entrance.util.mail.MailServiceFactory;

/**
 * @description 第三方用户登录
 * 
 */
public class ForgetPwdCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_FORGET_PWD == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_FORGET_PWD == code) {
				return forgetPwd();
			}
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult forgetPwd() throws Exception {

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		if (jsonObj.isNull("account") || jsonObj.isNull("email")) {
			logger.error("邮箱或密码为空   " + jsonObj);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"邮箱或账号为空！", null, this);
		}
		String account = StringUtils.trim(jsonObj.getString("account"));
		String emial = StringUtils.trim(jsonObj.getString("email"));

		CustomerService cs = SystemInitialization.getApplicationContext()
				.getBean(CustomerService.class);

		final Customer customer = cs.findCustomerByAccount(account);

		if (customer == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"服务错误：帐号错误！", null, this);
		}

		Parent p = cs.getParentByCustomerId(customer.getId());
		if (p == null || p.getEmail() == null || "".equals(p.getEmail())) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"对不起，您没有注册邮箱，不能执行该操作！", null, this);
		}

		if (!p.getEmail().equals(emial)) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"对不起，注册邮箱不正确！", null, this);
		}

		Date current = new Date();
		String forgetPasswordCode = customer.getAccount() + p.getEmail()
				+ current.getTime() + RandomUtil.generateString(10);
		forgetPasswordCode = MD5.code(forgetPasswordCode);

		String mailContent = MailServiceFactory.getMailContentService()
				.readCustomerHtmlContent("forget_password", customer,
						forgetPasswordCode);
		if (MailFacade.sendMail(mailContent, "“爱看”用户密码找回", p.getEmail())) {
			ForgetPasswordCode forgetPassword = new ForgetPasswordCode();
			forgetPassword.setCustomer(customer);
			forgetPassword.setStartTime(current);
			forgetPassword.setCode(forgetPasswordCode);
			try {
				cs.saveForgetPasswordCode(forgetPassword);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"密码已发送到你的邮箱,请注意查收 !", "", this);
			} catch (Exception e) {
				e.printStackTrace();
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"对不起，邮件发送失败，请重试！", null, this);
			}
		} else {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"对不起，邮件发送失败，请重试！", null, this);
		}

	}

	public String getEmailWebsite(String email) {
		return "http://mail." + email.substring(email.indexOf('@') + 1);
	}

}
