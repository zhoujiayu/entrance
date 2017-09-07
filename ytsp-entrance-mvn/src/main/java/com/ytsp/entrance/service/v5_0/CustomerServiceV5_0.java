package com.ytsp.entrance.service.v5_0;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.statistics.enums.LoginStatusEnum;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AgeTagRelationDao;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.AppDownloadDao;
import com.ytsp.db.dao.CustomerCollectionDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.CustomerThirdPlatformDao;
import com.ytsp.db.dao.CustomerValidateCountDao;
import com.ytsp.db.dao.CustomerValidateDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.ForgetPasswordCodeDao;
import com.ytsp.db.dao.HardwareRegisterDao;
import com.ytsp.db.dao.ParentDao;
import com.ytsp.db.domain.AgeTagRelation;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.AppDownload;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerCollection;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.CustomerThirdPlatform;
import com.ytsp.db.domain.CustomerValidate;
import com.ytsp.db.domain.CustomerValidateCount;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.ForgetPasswordCode;
import com.ytsp.db.domain.HardwareRegister;
import com.ytsp.db.domain.Parent;
import com.ytsp.db.enums.AgeSelectEnum;
import com.ytsp.db.enums.CustomerValidateCountTypeEnum;
import com.ytsp.db.enums.TagUseTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.CustomerCollectionVO;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.ValidateUtil;


@Service("registServiceV5_0")
@Transactional
public class CustomerServiceV5_0 {
	
	@Resource(name="hardwareRegisterDao")
	private HardwareRegisterDao hardwareRegisterDao;
	
	@Resource(name="appDownloadDao")
	private AppDownloadDao appDownloadDao;
	
	@Resource(name="parentDao")
	private ParentDao parentDao;
	
	@Resource(name="customerDao")
	private CustomerDao customerDao;

	@Resource(name="customerMemberDao")
	private CustomerMemberDao customerMemberDao;
	
	@Resource(name="customerValidateDao")
	private CustomerValidateDao customerValidateDao;
	
	@Resource(name="forgetPasswordCodeDao")
	private ForgetPasswordCodeDao forgetPasswordCodeDao;
	
	@Resource(name="customerCollectionDao")
	private CustomerCollectionDao customerCollectionDao;
	
	@Resource(name="ebProductDao")
	private EbProductDao ebProductDao;
	
	@Resource(name = "albumDao")
	private AlbumDao albumDao;
	
	@Resource(name = "ageTagRelationDao")
	private AgeTagRelationDao ageTagRelationDao;
	
	@Resource(name = "customerValidateCountDao")
	private CustomerValidateCountDao customerValidateCountDao;
	
	@Resource(name = "customerThirdPlatformDao")
	private CustomerThirdPlatformDao customerThirdPlatformDao;
	
	/**
	* <p>功能描述:检查用户名是否合法，不合法将用户名修改，返回登录状态</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：JSONObject</p>
	 */
	public JSONObject checkAccountReturnJson(int userId){
		JSONObject obj = null;
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		try {
			Customer customer = cs.findCustomerById(userId);
			if(null!=customer){
				obj = new JSONObject();
				String oldAccount = "";
				String newAccount = "";
				oldAccount = customer.getAccount();
//				newAccount = "ikan"+customer.getMobilephone();
				newAccount = Util.getCustomerAccount(customer.getMobilephone());
				if(ValidateUtil.isMoblie(customer.getAccount())){//如果用户名是手机号格式
					
					if(customer.getAccount().equals(customer.getMobilephone())){//绑定手机号码与原账号名称一致
						obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					}else{
						obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());
					}
					obj.put("account", newAccount);
					obj.put("oldAccount", customer.getAccount());
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else if(ValidateUtil.isEmail(customer.getAccount())){//如果账号是邮箱格式
					if(1==customer.getEmailValidate()){
						obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					}else{
						obj.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());
					}
					obj.put("account", newAccount);
					obj.put("oldAccount", customer.getAccount());
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else if(StringUtil.isNullOrEmpty(oldAccount)){
					obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					obj.put("oldAccount", customer.getAccount());
					obj.put("account", newAccount);
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else{
					obj.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					obj.put("oldAccount", customer.getAccount());
					obj.put("account", customer.getAccount());
				}
				obj.put("bindingUid", customer.getId());
			}
		} catch (Exception e) {
			
		}
		return obj;
	}
	
	/**
	* <p>功能描述:检查用户名是否合法，不合法将用户名修改，返回登录状态</p>
	* <p>参数：@param userId
	* <p>参数：@param map</p>
	* <p>返回类型：void</p>
	 */
	public void checkAccount(int userId,Map<String, Object> map){
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		try {
			Customer customer = cs.findCustomerById(userId);
			if(null!=customer){
				String oldAccount = "";
				String newAccount = "";
				oldAccount = customer.getAccount();
//				newAccount = "ikan"+customer.getMobilephone();
				newAccount = Util.getCustomerAccount(customer.getMobilephone());
				if(ValidateUtil.isMoblie(customer.getAccount())){//如果用户名是手机号格式
					
					if(customer.getAccount().equals(customer.getMobilephone())){//绑定手机号码与原账号名称一致
						map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					}else{
						map.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());
					}
					map.put("account", newAccount);
					map.put("oldAccount", customer.getAccount());
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else if(ValidateUtil.isEmail(customer.getAccount())){//如果账号是邮箱格式
					if(1==customer.getEmailValidate()){
						map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					}else{
						map.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());
					}
					map.put("account", newAccount);
					map.put("oldAccount", customer.getAccount());
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else if(StringUtil.isNullOrEmpty(oldAccount)){
					map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					map.put("oldAccount", customer.getAccount());
					map.put("account", newAccount);
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else{
					map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					map.put("oldAccount", customer.getAccount());
					map.put("account", customer.getAccount());
				}
				map.put("bindingUid", customer.getId());
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	* <p>功能描述:检查用户名是否合法，不合法将用户名修改，返回登录状态</p>
	* <p>参数：@param userId
	* <p>参数：@param map</p>
	* <p>返回类型：void</p>
	 */
	public Map<String,Object> checkAccount(int userId){
		Map<String,Object> map = null;
		CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		try {
			Customer customer = cs.findCustomerById(userId);
			if(null!=customer){
				map = new HashMap<String, Object>();
				String oldAccount = "";
				String newAccount = "";
				oldAccount = customer.getAccount();
				newAccount = Util.getCustomerAccount(customer.getMobilephone());
				if(ValidateUtil.isMoblie(customer.getAccount())){//如果用户名是手机号格式
					
					if(customer.getAccount().equals(customer.getMobilephone())){//绑定手机号码与原账号名称一致
						map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					}else{
						map.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());
					}
					map.put("account", newAccount);
					map.put("oldAccount", customer.getAccount());
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else if(ValidateUtil.isEmail(customer.getAccount())){//如果账号是邮箱格式
					if(1==customer.getEmailValidate()){
						map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					}else{
						map.put("loginStatus", LoginStatusEnum.PROMPT_IFNO.getValue());
					}
					map.put("account", newAccount);
					map.put("oldAccount", customer.getAccount());
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else if(StringUtil.isNullOrEmpty(oldAccount)){
					map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					map.put("oldAccount", customer.getAccount());
					map.put("account", newAccount);
					cs.updateAccountByUserId(customer.getId(), newAccount);//是手机号 邮箱  或者 账号为空都会 自动修改账号
				}else{
					map.put("loginStatus", LoginStatusEnum.ISDirect_Login.getValue());//是否提示
					map.put("oldAccount", customer.getAccount());
					map.put("account", customer.getAccount());
				}
				map.put("bindingUid", customer.getId());
			}
		} catch (Exception e) {
			
		}
		return map;
	}
	
	/**
	* <p>功能描述:保存验证次数</p>
	* <p>参数：@param count
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveOrUpdateCustomerValidateCount(CustomerValidateCount count) throws SqlException{
		customerValidateCountDao.saveOrUpdate(count);
	}
	
	/**
	* <p>功能描述:保存验证次数</p>
	* <p>参数：@param count
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveCustomerValidateCount(CustomerValidateCount count) throws SqlException{
		customerValidateCountDao.save(count);
	}
	
	/**
	* <p>功能描述:更新验证次数</p>
	* <p>参数：@param count
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	public void updateCustomerValidateCount(CustomerValidateCount count) throws SqlException{
		customerValidateCountDao.update(count);
	}
	
	/**
	* <p>功能描述:获取2小时内</p>
	* <p>参数：@param phone
	* <p>参数：@return</p>
	* <p>返回类型：int</p>
	 * @throws SqlException 
	 */
	public int getCustomerValidateCountByPhone(String phone) throws SqlException{
		CustomerValidateCount count = customerValidateCountDao.findOneByHql(" WHERE startDate < ? and endDate > ? and account = ?", new Object[]{phone,new Date(),new Date()});
		if(count == null){
			return 0;
		}
		return count.getValidateCount();
	}
	
	/**
	* <p>功能描述:获取2小时内</p>
	* <p>参数：@param phone
	* <p>参数：@return</p>
	* <p>返回类型：int</p>
	 * @throws SqlException 
	 */
	public CustomerValidateCount findCustomerValidateCountByPhone(String phone,int type) throws SqlException{
		CustomerValidateCount count = customerValidateCountDao
				.findOneByHql(
						" WHERE createDate < ? and endDate > ? and type = ? and account = ? ",
						new Object[] { new Date(), new Date(),
								CustomerValidateCountTypeEnum.valueOf(type),
								phone });
		if(count == null){
			return null;
		}
		return count;
	}
	
	
	/**
	 * 根据条件更新次数
	 */
	public CustomerValidateCount getCustomerValidateCount(String condition ,int type ) throws Exception{
		CustomerValidateCount customerCount = customerValidateCountDao
				.findOneByHql(
						" WHERE  type = "+type+" and account = ? ",
						new Object[] {condition});
		
		if(customerCount == null){
			return null;
		}else{
			return customerCount;
		}
	}
	
	
	/**
	 * 根据手机号码/ip/设备号 查询相关错误次数记录
	 */
	public int getCountNum(String condition ,int type) throws Exception{
		int count  = 0;
		CustomerValidateCount customerCount = customerValidateCountDao
				.findOneByHql(
						" WHERE  type = "+type+" and account = ? ",
						new Object[] {condition});
		
		if(customerCount == null){
			return count;
		}else{
			return customerCount.getValidateCount()==null ? count : customerCount.getValidateCount();
		}
	}
	
	
	/**
	* <p>功能描述:更新用户信息</p>
	* <p>参数：@param customer
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateCustomer(Customer customer) throws SqlException{
		customerDao.update(customer);
	}
	
	/**
	* <p>功能描述:根据id获取用户</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Customer</p>
	 */
	public Customer getCustomerById(int userId) throws SqlException{
		return customerDao.findById(userId);
	}
	
	/**
	* <p>功能描述:邮箱是否验证</p>
	* <p>参数：@param userId
	* <p>参数：@param email
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean isPhoneOrEmailValidate(int userId,String validateInfo,int isSuccess,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE userName = ? and isSuccess = ? and Customer.id = ? order by StartTime desc ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{validateInfo,isSuccess,userId});
		
		if(custValid == null || custValid.size() <= 0){
			return false;
		}
		return true;
	}
	
	/**
	* <p>功能描述:邮箱是否验证</p>
	* <p>参数：@param userId
	* <p>参数：@param email
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	public int isEmailValidate(int userId,String email) throws SqlException{
		int flag = 0;
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE userName = ? and isSuccess = 1 and type in(2,4) ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{email});
		
		//未验证状态
		if(custValid == null || custValid.size() == 0){
			return flag;
		}
		//校验手机号被占用
		for (ForgetPasswordCode forgetPasswordCode : custValid) {
			int id = forgetPasswordCode.getCustomer().getId();
			if(id == userId ){
				flag = 1;
			}
			if(id != userId ){
				flag = 2;
				break;
			}
		}
		return flag;
	}
	
	/**
	* <p>功能描述:邮箱是否验证</p>
	* <p>参数：@param userId
	* <p>参数：@param email
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	public int isPhoneOrEmailValidate(int userId,String email,int type) throws SqlException{
		int flag = 0;
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE userName = ? and isSuccess = 1 and type = ? ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{email,type});
		
		//未验证状态
		if(custValid == null || custValid.size() == 0){
			return flag;
		}
		//校验手机号被占用
		for (ForgetPasswordCode forgetPasswordCode : custValid) {
			int id = forgetPasswordCode.getCustomer().getId();
			if(id == userId ){
				flag = 1;
			}
			if(id != userId ){
				flag = 2;
				break;
			}
		}
		return flag;
	}
	
	/**
	* <p>功能描述:手机号是否验证</p>
	* <p>参数：@param userId
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean isPhoneValidate(int userId,String phone) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE userName = ? and isSuccess = 1 ");
		List<ForgetPasswordCode>  custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{phone});
		if(custValid == null || custValid.size() == 0){
			return false;
		}
		return true;
	}
	
	/**
	* <p>功能描述:邮箱是否验证</p>
	* <p>参数：@param userId
	* <p>参数：@param email
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean isEmailValidate(int userId,String email,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE userName = ? and isSuccess = 1 and Customer.id = ? and type =? ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{email,userId,type});
		if(custValid == null || custValid.size() <= 0){
			return false;
		}
		
		return true;
	}
	
	/**
	* <p>功能描述:更新用户验证信息</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateCustomerValidate(ForgetPasswordCode custValid,Customer cust,int userId) throws SqlException{
		forgetPasswordCodeDao.update(custValid);
		//更新客户手机号
		cust.setMobilephone(custValid.getUserName());
		cust.setPhoneValidate(1);
		customerDao.update(cust);
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateValidateInfo(ForgetPasswordCode custValid) throws SqlException{
		forgetPasswordCodeDao.update(custValid);
	}
	
	/**
	* <p>功能描述:更新用户邮箱验证信息并绑定邮箱</p>
	* <p>参数：@param custValid 验证信息
	* <p>参数：@param cust 用户
	* <p>参数：@param userId 用户id
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateCustomerEmailValidate(ForgetPasswordCode custValid,Customer cust) throws SqlException{
		custValid.setSuccessTime(new Date());
		custValid.setIsSuccess(1);
		custValid.setCustomer(cust);
		forgetPasswordCodeDao.update(custValid);
		//将邮箱绑定到客户
		cust.setEmail(custValid.getUserName());
		cust.setEmailValidate(1);
		customerDao.update(cust);
	}
	
	
	/**
	 * 跟新快速找回密码邮箱验证状态
	 * @param custValid
	 * @throws SqlException
	 */
	public void updateEmailCodeStatus(ForgetPasswordCode custValid) throws SqlException{
		Customer cust = new Customer();
		cust.setId(0);
		custValid.setSuccessTime(new Date());
		custValid.setIsSuccess(1);
		custValid.setCustomer(cust);
		forgetPasswordCodeDao.update(custValid);
	}
	
	/**
	 * 更新验证码状态和验证时间
	 */
	public void updateCodeStatus(ForgetPasswordCode custValid)throws Exception{
		forgetPasswordCodeDao.updateByHql(" set isSuccess = 1 ,successTime = ? where id = ? ", new Object[]{custValid.getSuccessTime(),custValid.getId()});
	}
	
	
	/**
	* <p>功能描述:更新通过邮箱修改手机号的验证信息及绑定相应手机号</p>
	* <p>参数：@param custValid 用户未验证的信息
	* <p>参数：@param cust 用户
	* <p>参数：@param phone 更换的手机号
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateChangePhoneByEmail(ForgetPasswordCode custValid,Customer cust,String phone) throws SqlException{
		//废弃旧的验证信息
		forgetPasswordCodeDao.updateByHql(" set isSuccess = 0 where Customer.id = ? and username = ? and isSuccess = 1", new Object[]{cust.getId(),cust.getMobilephone()});
		//绑定新手机号
		custValid.setIsSuccess(1);
		custValid.setSuccessTime(new Date());
		custValid.setCustomer(cust);
		custValid.setUserName(phone);
		forgetPasswordCodeDao.update(custValid);
		//更新用户验证状态
		cust.setMobilephone(phone);
		cust.setPhoneValidate(1);
		customerDao.update(cust);
	}
	
	
	/**
	* <p>功能描述:将第三方用户信息绑定到已有的用户上</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateBindingCustomer(ForgetPasswordCode custValid,Customer bindingCust,int thirdPlatId) throws SqlException{
		//更新用户验证信息
		custValid.setIsSuccess(1);
		custValid.setSuccessTime(new Date());
		custValid.setCustomer(bindingCust);
		forgetPasswordCodeDao.update(custValid);
		//更新用户验证状态
		CustomerThirdPlatform thirdPlat = customerThirdPlatformDao.findOneByHql(" WHERE id = ? ", new Object[]{thirdPlatId});
//		customerThirdPlatformDao.updateByHql(" set customer = ? where id = ? ", new Object[]{});
		thirdPlat.setCustomer(bindingCust);
		customerThirdPlatformDao.update(thirdPlat);
	}
	
	/**
	* <p>功能描述:更新解绑信息</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateUnbindingPhone(ForgetPasswordCode custValid,Customer cust) throws SqlException{
		//解除该手机号的绑定验证
//		forgetPasswordCodeDao.updateByHql(" set isSuccess = 0 where userName = ? and isSuccess = 1 ",new Object[]{custValid.getUserName()});
		//更新用户手机验证状态为解绑状态
		customerDao.updateByHql(" set phoneValidate = 2 where mobilephone = ? and phoneValidate = 1 ", new Object[]{custValid.getUserName()});
		//绑定手机号
		custValid.setIsSuccess(1);
		custValid.setSuccessTime(new Date());
		custValid.setCustomer(cust);
		forgetPasswordCodeDao.update(custValid);
		//更新用户验证状态
		cust.setMobilephone(custValid.getUserName());
		cust.setPhoneValidate(1);
		customerDao.update(cust);
	}
	
	/**
	* <p>功能描述:解绑已占用手机信息</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateUnbindingExistsUser(String phone) throws SqlException{
		//更新用户手机验证状态为解绑状态
		customerDao.updateByHql(" set phoneValidate = 2 where mobilephone = ? and phoneValidate = 1 ", new Object[]{phone});
	}
	
	/**
	* <p>功能描述:更新解绑邮箱验证，并绑定新的邮箱</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateUnbindingEmail(ForgetPasswordCode custValid,Customer cust,String newEmail) throws SqlException{
		//解除该邮箱的绑定验证
		forgetPasswordCodeDao.updateByHql(" set isSuccess = 0 where userName = ? and isSuccess = 1 ",new Object[]{newEmail});
		//更新用户占用的邮箱验证状态为解绑状态
		customerDao.updateByHql(" set emailValidate = 2 where email = ? and emailValidate = 1 ", new Object[]{newEmail});
		//更新邮箱验证状态及成功时间
		custValid.setIsSuccess(1);
		custValid.setSuccessTime(new Date());
		custValid.setCustomer(cust);
		forgetPasswordCodeDao.update(custValid);
		//更新用户邮箱验证状态
		cust.setEmail(custValid.getUserName());
		cust.setEmailValidate(1);
		customerDao.update(cust);
	}
	
	/**
	* <p>功能描述:更换用户手机验证信息</p>
	* <p>参数：@param custValid 旧的验证信息
	* <p>参数：@param newCustValid 新的验证信息
	* <p>参数：@param cust 用户
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void changeCustomerValidate(ForgetPasswordCode newCustValid,Customer cust) throws SqlException{
		//废弃旧的验证信息
		forgetPasswordCodeDao.updateByHql(" set isSuccess = 0 where Customer.id = ? and username = ? ", new Object[]{cust.getId(),cust.getMobilephone()});
		//更新新的手机验证信息
		newCustValid.setIsSuccess(1);
		newCustValid.setSuccessTime(new Date());
		newCustValid.setCustomer(cust);
		forgetPasswordCodeDao.update(newCustValid);
		//更新客户手机号
		cust.setMobilephone(newCustValid.getUserName());
		cust.setPhoneValidate(1);
		customerDao.update(cust);
	}
	
	/**
	* <p>功能描述:更换用户邮箱验证信息</p>
	* <p>参数：@param custValid 旧的验证信息
	* <p>参数：@param newCustValid 新的验证信息
	* <p>参数：@param cust 用户
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void changeCustomerEmailValidate(ForgetPasswordCode newCustValid,Customer cust) throws SqlException{
		//废弃旧的邮箱验证信息
		forgetPasswordCodeDao.updateByHql(" set isSuccess = 0 where Customer.id = ? and username = ? ", new Object[]{cust.getId(),cust.getEmail()});
		//更新新的邮箱验证信息
		newCustValid.setIsSuccess(1);
		newCustValid.setSuccessTime(new Date());
		newCustValid.setCustomer(cust);
		forgetPasswordCodeDao.update(newCustValid);
		//更新客户手机号
		cust.setEmail(newCustValid.getUserName());
		cust.setEmailValidate(1);
		customerDao.update(cust);
	}
	
	/**
	* <p>功能描述:保存用户验证信息</p>
	* <p>参数：@param custValid
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveCustomerValidate(ForgetPasswordCode custValid) throws SqlException{
		forgetPasswordCodeDao.save(custValid);
	}
	
	
	/**
	* <p>功能描述:根据手机号获取已验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getCustomerValidateByPhone(String phone) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE StartTime < ? and endTime > ? and userName = ? and isSuccess = 0 and method = 1 order by StartTime desc ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{new Date(),new Date(),phone});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:根据手机号获取已验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getCustomerValidateByPhoneAndType(String phone,int type,int success) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE type = ? and userName = ? and isSuccess = ? and method = 1 order by StartTime desc ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{type,phone,success});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	 * 手机验证码是否存在
	 */
	public ForgetPasswordCode getCustomerPhoneCodeExist(String phone,String code ,int type,int success) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE type = ? and userName = ? and isSuccess = ? and code = ? and method = 1 order by StartTime desc ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{type,phone,success,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:根据手机号获取已验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getCustomerValidateByPhone(String phone,int userId) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE userName = ? and isSuccess = 1 and Customer.id = ? and method = 1 order by StartTime desc ");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{phone,userId});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	
	/**
	* <p>功能描述:获取邮箱或者手机验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public List<ForgetPasswordCode> getPhoneOrEmailValidate(String validateInfo,int userId,int method,int isSuccess) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and method = ? and userName = ? order by StartTime");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,method,validateInfo});

		return custValid;
	}
	
	/**
	* <p>功能描述:获取用邮箱换手机号的未验证信息</p>
	* <p>参数：@param validateInfo 验证手机号或邮箱
	* <p>参数：@param userId  用户id
	* <p>参数：@param method  验证方式：0邮箱验证1手机验证2邮箱更换手机
	* <p>参数：@param isSuccess 是否成功 0未验证1验证成功
	* <p>参数：@param code 验证码
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ForgetPasswordCode</p>
	 */
	public ForgetPasswordCode getChangePhoneByEmailNumber(String validateInfo,int userId,int type,int isSuccess,String code) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and type = ? and userName = ? and Customer.id = ? and code = ? order by StartTime");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,type,validateInfo,userId,code});
		
		if(custValid == null || custValid.size() == 0){
			return null;
		}
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:获取用邮箱换手机号的验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getChangePhoneByEmailValidate(String validateInfo,int userId,int method,int isSuccess) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and method = ? and userName = ? and Customer.id = ?  order by StartTime desc");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,method,validateInfo,userId});
		
		if(custValid == null || custValid.size() == 0){
			return null;
		}
		return custValid.get(0);
	}
	
	/**
	 * 邮箱快速找回
	 * @throws SqlException
	 */
	public ForgetPasswordCode getQuickEmailCode(String validateInfo,int userId,int method,int isSuccess,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and method = ? and userName = ? and Customer.id = ? and type = ?  order by StartTime desc");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,method,validateInfo,userId,type});
		
		if(custValid == null || custValid.size() == 0){
			return null;
		}
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:用户邮箱是否验证</p>
	* <p>参数：@param email
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	public int getEmailValidateStatus(String email,int userId) throws SqlException{
		int flag = 0;
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE email = ? ");
		List<Customer> custs = customerDao.findAllByHql(sql.toString(), new Object[]{email});
		
		//校验手机号被占用
		for (Customer cust : custs) {
			if(cust.getEmailValidate() != null && cust.getEmailValidate() == 1 && cust.getId() == userId){
				flag = 1;
			}
			if(cust.getEmailValidate() != null && cust.getEmailValidate() == 1 && cust.getId() != userId ){
				flag = 2;
				break;
			}
		}
		return flag;
	}
	
	
	/**
	* <p>功能描述:根据手机号获取验证码</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public int getPhoneValidateStatus(String phone,int userId) throws SqlException{
		int flag = 0;
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE mobilephone = ? ");
		List<Customer> custs = customerDao.findAllByHql(sql.toString(), new Object[]{phone});
		
		//校验手机号被占用
		for (Customer cust : custs) {
			if (cust.getPhoneValidate() != null && cust.getPhoneValidate() == 1
					&& cust.getId() == userId) {
				flag = 1;
			}
			if (cust.getPhoneValidate() != null && cust.getPhoneValidate() == 1
					&& cust.getId() != userId) {
				flag = 2;
				break;
			}
		}
		return flag;
	}
	
	/**
	* <p>功能描述:根据邮箱获取验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public CustomerValidate getCustomerValidateByEmail(String email) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE email = ? ");
		List<CustomerValidate> custValid = customerValidateDao.findAllByHql(sql.toString(), new Object[]{email});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:根据手机获取验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getPhoneValidate(String phone,int userId,int isSuccess,String code,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and type = ? and Customer.id = ? and userName = ? and code = ? order by StartTime desc");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,type,userId,phone,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		return custValid.get(0);
	}

	/**
	* <p>功能描述:根据手机获取验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getPhoneValidate(String phone,int isSuccess,String code,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and type = ? and userName = ? and code = ? order by StartTime desc");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,type,phone,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		return custValid.get(0);
	}
	
	public ForgetPasswordCode getPhoneValidate(String phone,int userId,int isSuccess,String code) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ?  and Customer.id = ? and userName = ? and code = ? order by StartTime desc");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,userId,phone,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		return custValid.get(0);
	}
	/**
	* <p>功能描述:根据邮箱获取验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getEmailValidate(String email,int userId,int isSuccess,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and Customer.id = ? and type = ? and userName = ? order by StartTime");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,userId,type,email});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:根据验证码和邮箱 获取用户邮箱获取验证信息</p>
	* <p>参数：@param email  邮箱
	* <p>参数：@param userId 用户id
	* <p>参数：@param isSuccess  是否成功标识
	* <p>参数：@param code  验证码
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ForgetPasswordCode</p>
	 */
	public ForgetPasswordCode getEmailValidateByCode(String email,int userId,int isSuccess,String code,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE isSuccess = ? and Customer.id = ? and type = ? and userName = ? and code = ? order by StartTime");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{isSuccess,userId,type,email,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:根据验证码和邮箱 获取用户邮箱获取验证信息</p>
	* <p>参数：@param email  邮箱
	* <p>参数：@param userId 用户id
	* <p>参数：@param code  验证码
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ForgetPasswordCode</p>
	 */
	public ForgetPasswordCode getEmailValidateByCode(String email,int userId,String code) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE Customer.id = ? and userName = ? and code = ? order by StartTime");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{userId,email,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		
		return custValid.get(0);
	}
	
	/**
	* <p>功能描述:根据手机获取验证信息</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CustomerValidate</p>
	 */
	public ForgetPasswordCode getPhoneValidate(String phone,int userId,String code) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE Customer.id = ? and userName = ? and code = ? order by StartTime desc");
		List<ForgetPasswordCode> custValid = forgetPasswordCodeDao.findAllByHql(sql.toString(), new Object[]{userId,phone,code});
		if(custValid == null || custValid.size() <= 0){
			return null;
		}
		return custValid.get(0);
	}
	
	
	/**
	* <p>功能描述:帐号是否存在</p>
	* <p>参数：@param account
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean existByAccount(String account) throws Exception {
		int count = customerDao.getRecordCount(" WHERE account = ?",
				new Object[] { account });
		return count > 0 ? true : false;
	}
	
	/**
	* <p>功能描述:根据帐号获取用户</p>
	* <p>参数：@param account
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public Customer getCustomerByAccount(String account) throws Exception {
		List<Customer> cust = customerDao.findAllByHql(" WHERE account = ?",
				new Object[] { account });
		if(cust == null || cust.size() <= 0){
			return null;
		}
		return cust.get(0);
	}
	
	/**
	* <p>功能描述:根据手机号或者邮箱或者帐号获取用户信息<p>
	* <p>（1）如果为手机格式，若手机已验证，返回已验证手机的用户<p>
	* <p>（2）如果为邮箱格式，若邮箱已验证，返回已验证邮箱的用户<p>
	* <p>（3）以上2种都不符合，直接获取该用户名的帐号<p>
	* <p>参数：@param account
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：Customer</p>
	 */
	public Customer getCustomerByPhoneOrEmailOrAccount(String account) throws Exception{
		boolean isMobile = ValidateUtil.isMoblie(account);
		Customer cust = null;
		if(isMobile){
			cust = getCustomerByPhone(account, 1);
		}
		boolean isEmail = ValidateUtil.isEmail(account);
		if(isEmail){
			cust = getCustomerInfoByEmail(account, 1);
		}
		if(cust == null){
			cust = getCustomerByAccount(account);
		}
		return cust;
	}
	
	/**
	* <p>功能描述:根据手机号获取用户</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public Customer getCustomerByPhone(String phone) throws Exception {
		List<Customer> cust = customerDao.findAllByHql(" WHERE mobilephone = ?",
				new Object[] { phone });
		if(cust == null || cust.size() <= 0){
			return null;
		}
		return cust.get(0);
	}
	
	/**
	* <p>功能描述:根据手机号是否存在</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean isMobilePhoneExist(String phone) throws Exception {
		int count = customerDao.sqlCount("select count(1) from ytsp_customer where phoneValidate = 1 and mobilephone = '"+phone+"'");
		return count > 0 ? true : false;
	}
	
	/**
	* <p>功能描述:根据手机号和验证状态获取用户</p>
	* <p>参数：@param phone
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public Customer getCustomerByPhone(String phone,int phoneValidate) throws Exception {
		List<Customer> cust = customerDao.findAllByHql(" WHERE phoneValidate = ? and mobilephone = ?",
				new Object[] { phoneValidate,phone });
		if(cust == null || cust.size() <= 0){
			return null;
		}
		return cust.get(0);
	}
	
	/**
	* <p>功能描述:根据邮箱获取用户</p>
	* <p>参数：@param account
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public Customer getCustomerByEmail(String email) throws Exception {
		List<Customer> cust = customerDao.findAllByHql(" WHERE email = ?",
				new Object[] { email });
		if(cust == null || cust.size() <= 0){
			return null;
		}
		return cust.get(0);
	}
	
	
	/**
	 * 查询以验证的邮箱
	 */
	public Customer getCustomerInfoByEmail(String email,int emailValidate) throws Exception {
		List<Customer> cust = customerDao.findAllByHql(" WHERE email = ? and emailValidate = ?",
				new Object[] { email,emailValidate});
		if(cust == null || cust.size() <= 0){
			return null;
		}
		return cust.get(0);
	}
	
	/**
	* <p>功能描述:保存用户注册信息</p>
	* <p>参数：@param customer
	* <p>参数：@param otherInfo
	* <p>参数：@param appDiv
	* <p>返回类型：void</p>
	 * @throws Exception 
	 */
	public void saveCustomerRegist(Customer customer,String otherInfo,String appDiv) throws Exception{
		//保存客户信息
		customerDao.save(customer);
		
		saveOrUpdateParent(getParent(customer));
		// 注册硬件信息
		saveByNumber(customer.getTerminalNumber(), otherInfo,
				customer.getTerminalType(),customer.getTerminalVersion(),
				appDiv,customer.getRegisterIp());
		//TODO 妈妈网活动，以mmw开头的赠一月vip
		if(customer.getAccount()==null)
			return ;
		if(customer.getAccount().length()>3&&
				customer.getAccount().substring(0, 3).equalsIgnoreCase("mmw")){
			CustomerMember member = new CustomerMember();
			member.setCustomer(customer);
			Date dateNow = new Date();
			member.setCreateTime(dateNow);
			member.setStartTime(dateNow);
			Calendar endTime = Calendar.getInstance();
			endTime.add(Calendar.MONTH, 1);
			member.setEndTime(endTime.getTime());
			member.setValid(true);
			customerMemberDao.save(member);
		}
	}
	
	/**
	* <p>功能描述:获取家长信息</p>
	* <p>参数：@param customer
	* <p>参数：@return</p>
	* <p>返回类型：Parent</p>
	 */
	private Parent getParent(Customer customer){
		//创建一条家长信息
		Parent parent = new Parent();
		parent.setCustomer(customer);
		parent.setEmail(customer.getEmail());
		String[] a = IPSeeker.getAreaNameByIp(customer.getRegisterIp());
		parent.setCity(a[1]);
		parent.setNick(customer.getNick());
		parent.setProvince(a[0]);
		parent.setPhone(customer.getMobilephone());
		return parent;
	}
	
	public void saveOrUpdateParent(Parent parent) throws Exception {
		if (parent == null || parent.getCustomer() == null) {
			return;
		}

		Parent _parent = parentDao.findOneByHql(" WHERE customer.id = ?",
				new Object[] { parent.getCustomer().getId() });
		if (_parent != null) {
			_parent._syncValue(parent);
			parentDao.update(_parent);
		} else {
			parentDao.save(parent);
		}
	}
	
	/**
	 * 注册硬件信息
	 * @param number
	 * @throws Exception
	 */
	public void saveByNumber(String number, String otherInfo, String platform, String version,String appDiv,String ip) throws Exception {
		//首先判断有没有注册硬件
		HardwareRegister hw = hardwareRegisterDao.findOneByHql(" WHERE number=?", new Object[]{number});
		
		//如果没有注册则注册
		if(hw == null){
			Date cur = new Date();
			hw = new HardwareRegister();
			hw.setNumber(number);
			hw.setOtherInfo(otherInfo);
			hw.setCreateTime(cur);
			hw.setTerminalType(platform);
			hw.setTerminalVersion(version);
			hw.setAppDiv(appDiv);
		    long free = (cur.getTime()/1000) + 60*60*24*3;//免费看片天数，默认为3  
		    hw.setProbation(new Date(free*1000));
		    hw.setIp(ip);
			String[] a = IPSeeker.getAreaNameByIp(ip);
			hw.setProvince(a[0]);
			hw.setCity(a[1]);
			hardwareRegisterDao.save(hw);
			List<AppDownload> ls = appDownloadDao.findAllByHql(
					"where  ip=? and activate=? and createTime>?",
					new Object[]{ip,false,new Date(System.currentTimeMillis()-8*3600*1000)});
			if(ls!=null&&ls.size()>0){
				ls.get(0).setActivate(true);
				appDownloadDao.update(ls.get(0));
			}
		}
	}
	
	/**
	 * 保存用户收藏信息
	 */
	public void saveCustoerCollection(CustomerCollection customerCollection){
		
		//先查询是都已经添加收藏，添加过就不进行再次保存
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("select count(1) from ytsp_customer_collection where ").
			append("userId="+customerCollection.getUserId()+
					" and  type="+customerCollection.getType());
			if(customerCollection.getType()==1){
				sb.append(" and productId="+customerCollection.getProductId());
			}else{
				sb.append(" and albumId="+customerCollection.getAlbumId());
			}
			int result = customerCollectionDao.sqlCount(sb.toString());
			if(result==0){
				customerCollectionDao.save(customerCollection);
			}
		} catch (SqlException e) {
			e.printStackTrace();
		}
	}
	
	public int getAlbumCollectionCount(int userId,int type){
		StringBuffer sb = new StringBuffer();
		sb.append("select a.* from ytsp_album a  ,");
		sb.append(" ytsp_customer_collection c ");
		sb.append(" where a.id = c.albumId and c.userId = "+userId+" and c.type="+type+" order by createTime desc");
		List<Album> list = albumDao.sqlFetch(sb.toString(), Album.class, -1,
				-1);
		if(null==list){
			return 0;
		}else{
			return list.size();
		}
	}
	
	public List<CustomerCollectionVO> getAlbumCollection(int userId,int type,int lastId,int page, int pagesize,String version,String platform) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append("select a.* from ytsp_album a  ,");
		sb.append(" ytsp_customer_collection c ");
		if(lastId==0){
			sb.append(" where a.id = c.albumId and c.userId = "+userId+" and c.type="+type+" order by createTime desc");
		}else{
			sb.append(" where a.id = c.albumId and c.userId = "+userId+" and c.type="+type+" and c.id < "+lastId+" order by createTime desc");
		}
		
		List<Album> list = albumDao.sqlFetch(sb.toString(), Album.class, 0,
				pagesize);
		List<CustomerCollectionVO> l = new ArrayList<CustomerCollectionVO>();
		if(list!=null&&list.size()>0){
			String age = "";
			for(Album a :list){
				CustomerCollectionVO v = new CustomerCollectionVO();
				List<AgeTagRelation> ageRelations = ageTagRelationDao.findAllByHql(
						" WHERE useType = ? AND relationcode = ? ", new Object[] {
								TagUseTypeEnum.ALBUM, a.getId() });
				if (ageRelations == null || ageRelations.size() <= 0) {
					v.setAlbumAge(age);
				}else{
					AgeTagRelation ageRel = ageRelations.get(0);
					int startAgeValue = ageRel.getStartValue().getValue();
					int endAgeValue = ageRel.getEndValue().getValue();
					StringBuffer ageSb = new StringBuffer();
					if (startAgeValue == 0 && endAgeValue == 0) {
						ageSb.append("0岁以上");
					} else if (startAgeValue == AgeSelectEnum.age_99y.getValue()
							&& endAgeValue == AgeSelectEnum.age_99y.getValue()) {
						ageSb.append("12岁以上");
					} else if (startAgeValue == endAgeValue) {
						ageSb.append(computeAge(startAgeValue, true));
					} else if (startAgeValue > endAgeValue) {
						if (startAgeValue != AgeSelectEnum.age_99y.getValue()) {
							ageSb.append(computeAge(endAgeValue, false)).append("~")
									.append(computeAge(startAgeValue, true));
						} else {
							ageSb.append(computeAge(startAgeValue, false));
						}
					} else if (startAgeValue == 0
							&& endAgeValue == AgeSelectEnum.age_99y.getValue()) {
						ageSb.append("适用所有年龄段");
					} else {
						if(endAgeValue == AgeSelectEnum.age_99y.getValue()){
							ageSb.append(computeAge(startAgeValue, true)).append("以上");
						}else{
							ageSb.append(computeAge(startAgeValue, false)).append("~")
							.append(computeAge(endAgeValue, true));
						}
					}
					v.setAlbumAge(ageSb.toString());
				}
//				v.setAlbumCover(Util.getFullImageURL(a.getCover()));
				v.setAlbumCover(Util.getFullImageURLByVersion(a.getCover(),version,platform));
				v.setAlbumDescription(a.getDescription());
//				v.setAlbumWidthCover(Util.getFullImageURL(a.getWidthcover()));
				v.setAlbumWidthCover(Util.getFullImageURLByVersion(a.getWidthcover(),version,platform));
				v.setAlbumName(a.getName());
				v.setAlbumId(a.getId());
				v.setTotalCount(a.getTotalCount());
				v.setAlbumType(a.getAlbumCategory()==null?"":a.getAlbumCategory().getCname());
				v.setAlbumVip(a.getVip());
				v.setAlbumNowCount(null == a.getNowCount()? 0 : a.getNowCount());
				
				v.setId(customerCollectionDao.findOneByHql("where albumId = ? and type = ? and userId = ? ", new Object[]{a.getId(),type,userId}).getId());
				l.add(v);
			}
		}
		return l;
	}
	
	private String computeAge(int ageValue, boolean isEnd) {
		if (ageValue == 0) {
			return "0";
		}
		if (ageValue == AgeSelectEnum.age_99y.getValue()) {
			return "12岁以上";
		}
		if ((ageValue % 12) == 0) {
			if (isEnd) {
				return "" + ageValue / 12 + "岁";
			} else {
				return "" + ageValue / 12;
			}
		} else {
			StringBuffer age = new StringBuffer();
			int start = ageValue / 12;
			int rest = ageValue % 12;

			if (start == 0) {
				age.append(rest).append("个月");
			} else {
				age.append(start);
				if (rest != 0) {
					age.append("岁").append(rest).append("个月");
				}
				if (isEnd) {
					age.append("岁");
				}
			}
			return age.toString();
		}

	}

	public int getProductCollectionCount(int userId,int type){
		StringBuffer sb = new StringBuffer();
		sb.append(" select p.* from ytsp_ebiz_product p ,");
		sb.append(" ytsp_customer_collection c  ");
		sb.append(" where p.productcode = c.productId and c.userId = "+userId+" and c.type="+type);
		List<EbProduct> list =  ebProductDao.sqlFetch(sb.toString(), EbProduct.class, -1,
				-1);
		if(null==list){
			return 0;
		}else{
			return list.size();
		}
	}
	
	public List<CustomerCollectionVO> getProductCollection(int userId,int type,int lastId,int page, int pagesize,String version,String platform){
		StringBuffer sb = new StringBuffer();
		
		sb.append(" select p.* from ytsp_ebiz_product p ,");
		sb.append(" ytsp_customer_collection c  ");
		if(lastId!=0){
			sb.append(" where p.productcode = c.productId and c.userId = "+userId+" and c.type="+type+" and c.id < "+lastId+" order by createTime desc");
		}else{
			sb.append(" where p.productcode = c.productId and c.userId = "+userId+" and c.type="+type+" order by createTime desc");
		}
		
		List<EbProduct> list =  ebProductDao.sqlFetch(sb.toString(), EbProduct.class, 0,
				pagesize);
		List<CustomerCollectionVO> l = new ArrayList<CustomerCollectionVO>();
		if(list!=null&&list.size()>0){
			for(EbProduct e :list){
				CustomerCollectionVO v = new CustomerCollectionVO();
//				v.setImgUrl(Util.getFullImageURL(e.getImgUrl()));
				v.setImgUrl(Util.getFullImageURLByVersion(e.getImgUrl(),version,platform));
				v.setPrice(e.getPrice());
				v.setProductCode(e.getProductCode());
				v.setProductDescription(e.getProductDescription());
				v.setProductName(e.getProductName());
				v.setVprice(e.getVprice());
				v.setSvprice(e.getSvprice());
				try {
				v.setId(customerCollectionDao.findOneByHql("where productId = ? and type =?  and userId = ? ", new Object[]{e.getProductCode(),type,userId}).getId());
				} catch (SqlException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				l.add(v);
			}
		}
		return l;
	}
	
	public void delCustomerCollection(CustomerCollection customerCollection) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append("where  userId= ?  and ");
		if(customerCollection.getAlbumId()!=null){
			sb.append(" albumId = ?");
			customerCollectionDao.deleteByHql(sb.toString(), new Object[]{customerCollection.getUserId(),customerCollection.getAlbumId()});
		}
		if(customerCollection.getProductId()!=null){
			sb.append(" productId = ?");
			customerCollectionDao.deleteByHql(sb.toString(), new Object[]{customerCollection.getUserId(),customerCollection.getProductId()});
		}
	}
	
	public void delAllCustomerCollection(int userId,int type) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append("where  userId= ?  and ");
		sb.append(" type = ?");
		customerCollectionDao.deleteByHql(sb.toString(), new Object[]{userId,type});
	}
	
	public void delCustomerCollections(String ids,int type,int userId) throws Exception{
		String sql ="";
		if(1==type){
			sql = "delete from ytsp_customer_collection where userId="+userId+" and productId in ("+ids+")";
		}else{
			sql = "delete from ytsp_customer_collection where userId="+userId+" and albumId in ("+ids+")";
		}
		customerCollectionDao.executeSqlUpdate(sql);
	}
	
	/**
	 * 获取phone／ip／设备号记录次数 是否满足出图形验证码条件
	 * @return
	 * @throws SqlException
	 */
	public boolean  isBeyondCount(String phone,CommandContext commandContext) throws SqlException{
		boolean  flag = false;
		String device  = commandContext.getHead().getUniqueId();
		String ip  = ValidateUtil.getIpAddress(commandContext.getRequest());
		try {
			if((getCountNum(phone,CustomerValidateCountTypeEnum.PHONE.getValue())>=3) || //手机号码超过3次
					(getCountNum(device, CustomerValidateCountTypeEnum.DEVICE.getValue())>=3) || //设备号超过3次
					(getCountNum(ip, CustomerValidateCountTypeEnum.IPADDRESS.getValue())>=10)){ //ip地址超过
				flag = true;
			}
		} catch (Exception e) {
			
		}
		return flag;
	}
	
	/**
	 * 更新phone／ip／设备号／次数
	 */
	public void saveOrUpdateCount(String phone,CommandContext commandContext){
		try {
			if(null!=phone && !"".equals(phone)){
				updateBeyondCount(phone,CustomerValidateCountTypeEnum.PHONE.getValue());
			}
			String device = commandContext.getHead().getUniqueId();
			String ip = ValidateUtil.getIpAddress(commandContext.getRequest());
			updateBeyondCount(device,CustomerValidateCountTypeEnum.DEVICE.getValue());
			updateBeyondCount(ip,CustomerValidateCountTypeEnum.IPADDRESS.getValue());
		} catch (Exception e) {
			
		}
	}
	/**
	 * 更新次数
	 * @param condition
	 * @param type
	 */
	public void updateBeyondCount(String condition,int type){
		try {
			CustomerValidateCount  customerCount = getCustomerValidateCount(condition,type);
			if(customerCount!=null){
				customerCount.setValidateCount(customerCount.getValidateCount()+1);
				saveOrUpdateCustomerValidateCount(customerCount);
			}else{
				customerCount = new CustomerValidateCount();
				customerCount.setAccount(condition);
				customerCount.setCreateDate(new Date());
				customerCount.setValidateCount(1);
				customerCount.setEndDate(new Date());
				customerCount.setType(CustomerValidateCountTypeEnum.valueOf(type));
				saveCustomerValidateCount(customerCount);
			}
		} catch (Exception e) {
			
		}
	}	
	
	
	/**
	 * 清空各种情况下记录的次数
	 */
	public void delBeyondCount(String phone,CommandContext commandContext){
		try {
			if(!"".equals(phone)){
				String sqlPhone = "delete from ytsp_validate_count where account = '"+phone+"' and type = "+CustomerValidateCountTypeEnum.PHONE.getValue();
				customerValidateCountDao.executeSqlUpdate(sqlPhone);
			}
			String sqlIp = "delete from ytsp_validate_count where account = '"+ValidateUtil.getIpAddress(commandContext.getRequest())+"' and type = "+CustomerValidateCountTypeEnum.IPADDRESS.getValue();
			String sqlDevice = "delete from ytsp_validate_count where account = '"+commandContext.getHead().getUniqueId()+"' and type = "+CustomerValidateCountTypeEnum.DEVICE.getValue();
			customerValidateCountDao.executeSqlUpdate(sqlIp);
			customerValidateCountDao.executeSqlUpdate(sqlDevice);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
