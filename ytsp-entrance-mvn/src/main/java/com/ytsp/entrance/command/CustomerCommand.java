package com.ytsp.entrance.command;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.Parent;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.MD5;

/**
 * @author GENE
 * @description 用户信息命令
 * 
 */
public class CustomerCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_CUSTOMER_READ_PARENT == code 
		|| CommandList.CMD_CUSTOMER_READ_BABY == code
		|| CommandList.CMD_CUSTOMER_READ_PARENT_AND_BABY == code
		|| CommandList.CMD_CUSTOMER_SAVE_PARENT == code 
		|| CommandList.CMD_CUSTOMER_SAVE_BABY == code
		|| CommandList.CMD_CUSTOMER_REGIST_PARENT_PWD == code
		|| CommandList.CMD_CUSTOMER_MODIFY_PARENT_PWD == code
		|| CommandList.CMD_CUSTOMER_ACCOUNT_STATUS == code
		|| CommandList.CMD_CUSTOMER_PARENT_VERIFY == code
		|| CommandList.CMD_CUSTOMER_PARENT_PWD_STATUS == code
		|| CommandList.CMD_CUSTOMER_REGIST_DEVICE_TOKEN == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_CUSTOMER_READ_PARENT == code) {
				return getParentInfo();

			} else if (CommandList.CMD_CUSTOMER_READ_BABY == code) {
				return getBabyInfo();

			} else if (CommandList.CMD_CUSTOMER_READ_PARENT_AND_BABY == code) {
				return getParentAndBabyInfo();

			} else if (CommandList.CMD_CUSTOMER_SAVE_PARENT == code) {
				return saveParentInfo();

			} else if (CommandList.CMD_CUSTOMER_SAVE_BABY == code) {
				return saveBabyInfo();

			} else if (CommandList.CMD_CUSTOMER_REGIST_PARENT_PWD == code) {
				return registPwd();

			} else if (CommandList.CMD_CUSTOMER_MODIFY_PARENT_PWD == code) {
				return modifyPwd();

			} else if (CommandList.CMD_CUSTOMER_PARENT_VERIFY == code) {
				return verify();

			} else if(CommandList.CMD_CUSTOMER_ACCOUNT_STATUS == code){
				return readAccountStatus();
				
			} else if(CommandList.CMD_CUSTOMER_PARENT_PWD_STATUS == code){
				return readParentPwdStatus();
				
			} else if (CommandList.CMD_CUSTOMER_REGIST_DEVICE_TOKEN == code) {
				return registerCustomerDeviceToken();
				
			}
			
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}

	private ExecuteResult getParentInfo() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		
//		int as = checkAccountStatus();
//		if(as != 2 && as != 3){
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "非VIP用户无此操作权限！", null, this);
//		}

		Customer customer = sc.getCustomer();
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		Parent parent = cs.getParentByCustomerId(customer.getId());
		if (parent != null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取家长信息成功！", getParentJson(parent), this);
		} else {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "未找到家长信息设置！", null, this);
		}
	}
	
	private JSONObject getParentJson(Parent parent) throws Exception {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", parent.getId());
		jsonObj.put("parentName", parent.getNick() == null ? "" : parent.getNick());
		jsonObj.put("parentPhone", parent.getPhone() == null ? "" : parent.getPhone());
		jsonObj.put("parentEmail", parent.getEmail() == null ? "" : parent.getEmail());
		jsonObj.put("parentZip", parent.getZip() == null ? "" : parent.getZip());
		jsonObj.put("address", parent.getAddress() == null ? "" : parent.getAddress());
		jsonObj.put("province", parent.getProvince() == null ? "" : parent.getProvince());
		jsonObj.put("city", parent.getCity() == null ? "" : parent.getCity());
		
		return jsonObj;
	}

	private ExecuteResult saveParentInfo() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		Parent parent = cs.getParentByCustomerId(customer.getId());
		if (parent == null) {
			parent = new Parent();
			parent.setCustomer(customer);
		}
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		parent.setNick(jsonObj.getString("parentName"));
		parent.setPhone(jsonObj.getString("parentPhone"));
		parent.setEmail(jsonObj.getString("parentEmail"));
		parent.setZip(jsonObj.getString("parentZip"));
		parent.setAddress(jsonObj.getString("address"));
		parent.setProvince(jsonObj.getString("province"));
		parent.setCity(jsonObj.getString("city"));
		cs.saveOrUpdateParent(parent);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存家长信息成功！", null, this);
	}

	private ExecuteResult getBabyInfo() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		
//		int as = checkAccountStatus();
//		if(as != 2 && as != 3){
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "非VIP用户无此操作权限！", null, this);
//		}

		Customer customer = sc.getCustomer();
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		Baby baby = cs.getBabyByCustomerId(customer.getId());
		if (baby != null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取宝贝信息成功！", getBabyJson(baby), this);
		} else {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "未找到家长信息设置！", null, this);
		}
	}

	private JSONObject getBabyJson(Baby baby) throws Exception {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", baby.getId());
		jsonObj.put("babyName", baby.getName() == null ? "" : baby.getName());
		jsonObj.put("babyBirthday", DateFormatter.date2String(baby.getBirthday()));
		jsonObj.put("babyInterest", baby.getHobby() == null ? "" : baby.getHobby());
		jsonObj.put("babySex", baby.getSex() == null ? 0 : baby.getSex());
		jsonObj.put("head", baby.getHead() == null ? "" : baby.getHead());
		
		return jsonObj;
	}
	
	private ExecuteResult getParentAndBabyInfo() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		
//		int as = checkAccountStatus();
//		if(as != 2 && as != 3){
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "非VIP用户无此操作权限！", null, this);
//		}

		JSONObject jsonObj = new JSONObject();
		Customer customer = sc.getCustomer();
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		Parent parent = cs.getParentByCustomerId(customer.getId());
		if (parent != null) {
			jsonObj.put("parent", getParentJson(parent));
		}else{
			jsonObj.put("parent", "");
		}
		
		Baby baby = cs.getBabyByCustomerId(customer.getId());
		if (baby != null) {
			jsonObj.put("baby", getBabyJson(baby));
		} else{
			jsonObj.put("baby", "");
		}
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取家长及宝贝信息成功！", jsonObj, this);
	}
	
	private ExecuteResult saveBabyInfo()   {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		try {
			Customer customer = sc.getCustomer();
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			Baby baby = cs.getBabyByCustomerId(customer.getId());
			if (baby == null) {
				baby = new Baby();
				baby.setCustomer(customer);
			}
			if(!jsonObj.isNull("babyName"))
				baby.setName(jsonObj.getString("babyName"));
			if(!jsonObj.isNull("babyBirthday"))
				baby.setBirthday(DateFormatter.string2Date(jsonObj.getString("babyBirthday")));
			if(!jsonObj.isNull("babyInterest"))
				baby.setHobby(jsonObj.getString("babyInterest"));
			if(!jsonObj.isNull("babySex"))
				baby.setSex(jsonObj.getInt("babySex"));
			if(!jsonObj.isNull("head"))
				baby.setHead(jsonObj.getString("head"));
			cs.saveOrUpdateBaby(baby);
		} catch (Exception e) {
			logger.error("Command saveBabyInfo error", e);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存宝贝信息成功！", null, this);
	}

	/**
	 * @deprecated
	 * 统一有modifyPwd处理注册和修改事件
	 * @return
	 */
	public ExecuteResult registPwd() {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String password = StringUtils.trim(jsonObj.getString("password"));
			String password2 = StringUtils.trim(jsonObj.getString("password2"));

			if (StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "密码不能为空！", null, this);
			}

			if (!password.equals(password2)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "重复密码与密码不一致！", null, this);
			}

			Customer customer = sc.getCustomer();
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Parent parent = cs.getParentByCustomerId(customer.getId());
			if (parent == null) {
				parent = new Parent();
				parent.setCustomer(customer);
			}

			String md5Pwd = MD5.code(password.trim());
			parent.setPassword(md5Pwd);
			cs.saveOrUpdateParent(parent);

			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "设置家长控制密码成功！", null, this);
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
			String oldpassword = StringUtils.trim(jsonObj.getString("oldpassword"));
			String password = StringUtils.trim(jsonObj.getString("password"));
			String password2 = StringUtils.trim(jsonObj.getString("password2"));

			oldpassword = StringUtil.isNullOrEmpty(oldpassword) ? "" : oldpassword;

			if (StringUtil.isNullOrEmpty(password)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "新密码不能为空！", null, this);
			}

			if (!password.equals(password2)) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "重复密码与密码不一致！", null, this);
			}

			Customer customer = sc.getCustomer();
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Parent parent = cs.getParentByCustomerId(customer.getId());
			if (parent == null) {
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "您还未创建家长控制密码！", null, this);
				parent = new Parent();
				parent.setCustomer(customer);
			}

			String oldMd5Pwd = MD5.code(oldpassword.trim());
			if (StringUtil.isNotNullNotEmpty(parent.getPassword()) && !oldMd5Pwd.equals(parent.getPassword())) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "旧密码不正确！", null, this);
			}

			String md5Pwd = MD5.code(password.trim());
			parent.setPassword(md5Pwd);
			cs.saveOrUpdateParent(parent);

			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "设置家长控制密码成功！", null, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	public ExecuteResult verify() {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		try {

			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String password = StringUtils.trim(jsonObj.getString("password"));
			password = password == null ? "" : password;
//			if (StringUtil.isNullOrEmpty(password)) {
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "密码不能为空！", null, this);
//			}

			Customer customer = sc.getCustomer();
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Parent parent = cs.getParentByCustomerId(customer.getId());
			JSONObject obj = new JSONObject();
			if (parent == null) {
				obj.put("status", 0);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "您还未创建家长控制密码！", obj, this);
			}
			
			if (StringUtil.isNullOrEmpty(parent.getPassword()) && StringUtil.isNullOrEmpty(password)){
				obj.put("status", 1);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "家长控制密码验证通过！", obj, this);
			}

			String md5Pwd = MD5.code(password.trim());
			if (!md5Pwd.equals(parent.getPassword())) {
				obj.put("status", 2);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "密码不正确！", obj, this);
			}

			obj.put("status", 1);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "家长控制密码验证通过！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	public int checkAccountStatus() throws Exception {
		/*delete by daiyu 2012-4-26 start */
//		SessionCustomer sc = getSessionCustomer();
//		if (sc == null || sc.getCustomer() == null) {
//			return 0;//未知状态
//			
//		}else{
//			String hardwareNumber = getContext().getHead().getUniqueId();
//			MonthlyDao md = SystemInitialization.getApplicationContext().getBean(MonthlyDao.class);
//			HardwareRegisterDao hrd = SystemInitialization.getApplicationContext().getBean(HardwareRegisterDao.class);
//			
//			//1.检查包月情况
//			Monthly monthly = md.findOneByHql(" WHERE customer.id=?", new Object[]{sc.getCustomer().getId()});
//			if(monthly != null){
//				if(monthly.getExpireTime() != null && monthly.getExpireTime().after(new Date())){
//					return 3;//包月状态
//				}
//			}
//			
//			//2.检查是否超过试用期
//			HardwareRegister hw = hrd.findOneByHql(" WHERE number=?", new Object[]{hardwareNumber});
//			if(hw != null){
//				Date probation = hw.getProbation();
//				if(probation != null && probation.getTime() > new Date().getTime()){//仍在试用期内
//					return 2;//试用状态
//				}
//			}
//			
//			return 1;//普通状态
//		}
		/*delete by daiyu 2012-4-26 end */
		return 0;//未知状态
	}
	
	public ExecuteResult readAccountStatus() {
		try {
			JSONObject obj = new JSONObject();
			int as = checkAccountStatus();
			obj.put("accountStatus", as);
			
			if(as != 2 && as != 3){
				SystemParamInDB params = SystemManager.getInstance().getSystemParamInDB();
				String tips = params.getValue(IConstants.VIP_TIPS);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, tips, obj, this);
			}else{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取用户账户状态成功！", obj, this);
			}
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		} 
	}
	
	public ExecuteResult readParentPwdStatus() {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		try {

			Customer customer = sc.getCustomer();
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Parent parent = cs.getParentByCustomerId(customer.getId());
			JSONObject obj = new JSONObject();
			if (parent == null) {
				obj.put("status", 1);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "您还未创建家长控制密码！", obj, this);
			}else if(StringUtil.isNullOrEmpty(parent.getPassword())){
				obj.put("status", 1);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "家长控制密码为空！", obj, this);
			}else{
				obj.put("status", 2);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "已经设置家长控制密码！", obj, this);
			}
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}

	public ExecuteResult registerCustomerDeviceToken() {
		try {
			int uid = getContext().getHead().getUid();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String token = jsonObj.optString("deviceToken");
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			cs.saveRegisterCustomerDeviceToken(uid, token);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "注册用户DeviceToken成功！", null, this);
		} catch (Exception e) {
			logger.error("execute command error!", e);
			return getExceptionExecuteResult(e);
		}
	}
}
