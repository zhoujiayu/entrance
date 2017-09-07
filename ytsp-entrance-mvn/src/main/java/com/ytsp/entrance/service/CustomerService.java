package com.ytsp.entrance.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.AppDownloadDao;
import com.ytsp.db.dao.BabyDao;
import com.ytsp.db.dao.CommonRegionDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.CustomerThirdPlatformDao;
import com.ytsp.db.dao.ForgetPasswordCodeDao;
import com.ytsp.db.dao.HardwareRegisterDao;
import com.ytsp.db.dao.ParentDao;
import com.ytsp.db.domain.AppDownload;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.CommonRegion;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.CustomerThirdPlatform;
import com.ytsp.db.domain.ForgetPasswordCode;
import com.ytsp.db.domain.HardwareRegister;
import com.ytsp.db.domain.Parent;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.ValidateUtil;

/**
 * @author GENE
 * @description 用户服务
 */
public class CustomerService {
	private static final Logger logger = Logger
			.getLogger(CustomerService.class);

	private CustomerDao customerDao;
	private ParentDao parentDao;
	private BabyDao babyDao;
	private CommonRegionDao commonRegionDao;
	private CustomerThirdPlatformDao customerThirdPlatformDao;
	private ForgetPasswordCodeDao forgetPasswordCodeDao;
	private CustomerMemberDao customerMemberDao;
	
	
	@Resource(name="hardwareRegisterDao")
	private HardwareRegisterDao hardwareRegisterDao;
	
	@Resource(name="appDownloadDao")
	private AppDownloadDao appDownloadDao;
	
	
	
	//图形验证码
	private static final int IMGCODE_VALIDATETIME = 10;

	public CustomerMemberDao getCustomerMemberDao() {
		return customerMemberDao;
	}

	public void setCustomerMemberDao(CustomerMemberDao customerMemberDao) {
		this.customerMemberDao = customerMemberDao;
	}

	public void saveRegisterCustomerDeviceToken(int uid, String token)
			throws Exception {
		Customer customer = customerDao.findById(uid);
		if (customer != null) {
			customer.setDeviceToken(token);
			customerDao.update(customer);
		}
	}

	public boolean existByAccount(String account) throws Exception {
		int count = customerDao.getRecordCount(" WHERE account = ?",
				new Object[] { account });
		return count > 0 ? true : false;
	}

	public Customer findCustomerByAccountAndPassword(String account,
			String password) throws Exception {
		return customerDao.findOneByHql(" WHERE account = ? AND password = ?",
				new Object[] { account, password });
	}
	public Customer findCustomerByAccount(String account) throws Exception {
		return customerDao.findOneByHql(" WHERE account = ? ",
				new Object[] { account});
	}
	

	public void saveCustomer(Customer customer) throws Exception {
		customerDao.save(customer);
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

	public void saveOrUpdate(Customer customer) throws Exception {
		customerDao.saveOrUpdate(customer);
	}

	public void updateCustomer(Customer customer) throws Exception {
		customerDao.update(customer);
	}

	public void deleteCustomer(Customer customer) throws Exception {
		customerDao.delete(customer);
	}

	public Customer findCustomerById(int customerid) throws Exception {
		return customerDao.findById(customerid);
	}

	public List<Customer> getAllCustomers() throws Exception {
		return customerDao.getAll();
	}

	public void deleteCustomerById(int customerid) throws Exception {
		customerDao.deleteById(customerid);
	}

	public Parent getParentByCustomerId(int customerid) throws Exception {
		return parentDao.findOneByHql(" WHERE customer.id = ?",
				new Object[] { customerid });
	}

	public Baby getBabyByCustomerId(int customerid) throws Exception {
		return babyDao.findOneByHql(" WHERE customer.id = ?",
				new Object[] { customerid });
	}
	
	public List<Baby> getBabysByCustomerId(int customerid) throws Exception {
		return babyDao.findAllByHql(" WHERE customer.id = ?",
				new Object[] { customerid });
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

	public void saveOrUpdateBaby(Baby baby) throws Exception {
		if (baby == null || baby.getCustomer() == null) {
			return;
		}

		Baby _baby = babyDao.findOneByHql(" WHERE customer.id = ?",
				new Object[] { baby.getCustomer().getId() });
		if (_baby != null) {
			_baby._syncValue(baby);
			babyDao.update(_baby);
		} else {
			babyDao.save(baby);
		}
	}

	public CommonRegion findCodeByName(String name) throws SqlException {
		return commonRegionDao.findCodeByName(name);
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public ParentDao getParentDao() {
		return parentDao;
	}

	public void setParentDao(ParentDao parentDao) {
		this.parentDao = parentDao;
	}

	public BabyDao getBabyDao() {
		return babyDao;
	}

	public void setBabyDao(BabyDao babyDao) {
		this.babyDao = babyDao;
	}

	public CommonRegionDao getCommonRegionDao() {
		return commonRegionDao;
	}

	public void setCommonRegionDao(CommonRegionDao commonRegionDao) {
		this.commonRegionDao = commonRegionDao;
	}

	public CustomerThirdPlatformDao getCustomerThirdPlatformDao() {
		return customerThirdPlatformDao;
	}

	public void setCustomerThirdPlatformDao(
			CustomerThirdPlatformDao customerThirdPlatformDao) {
		this.customerThirdPlatformDao = customerThirdPlatformDao;
	}

	public ForgetPasswordCodeDao getForgetPasswordCodeDao() {
		return forgetPasswordCodeDao;
	}

	public void setForgetPasswordCodeDao(ForgetPasswordCodeDao forgetPasswodCodeDao) {
		this.forgetPasswordCodeDao = forgetPasswodCodeDao;
	}

	// 第三方登录
	/**
	 * 判断第三方账户在服务上是有记录
	 * 
	 * @throws SqlException
	 * */
	public boolean existByThirdPlatform(String platformName, String userId)
			throws SqlException {
		int count = customerThirdPlatformDao.getRecordCount(
				" WHERE user_id = ? and platform_name=?", new Object[] { userId,
						platformName });
		return count > 0 ? true : false;
	}

	/**
	 * 获取第三方账户在服务端的信息
	 * */
	public CustomerThirdPlatform findCustomerByThirdPlatform(
			String platformName, String userId) throws SqlException {
		return customerThirdPlatformDao.findOneByHql(
				" WHERE user_id = ? and platform_name=?", new Object[] { userId,
						platformName });
	}
	
	/**
	 *  合并微信三方登陆 将unionId作为同一用户在不同平台下唯一标示 
	 */
	public CustomerThirdPlatform findCustomerThirdPlatformByUninoId(String unionId) throws SqlException{
		
		return customerThirdPlatformDao.findOneByHql(
				"WHERE  union_id = ?", new Object[]{unionId});
	}	
	
	public CustomerThirdPlatform findCustomerThird(String userId,String unionId) throws SqlException{
		
		return customerThirdPlatformDao.findOneByHql(
				"WHERE  user_id=? and union_id = ?", new Object[]{userId,unionId});
	}
	
	
	 public CustomerThirdPlatform createCustomerThirdPlatform(CustomerThirdPlatform obj) throws SqlException{
		 customerThirdPlatformDao.save(obj);
		 return obj;
	 }
	
	//忘记密码
	public void saveForgetPasswordCode(ForgetPasswordCode forgetPasswordCode) throws SqlException{
		forgetPasswordCodeDao.save(forgetPasswordCode);
	}
	
	//记录微信三方登陆unionId
	public void updateUnionId(String userId,String unionId){
		customerThirdPlatformDao.executeSqlUpdate("update ytsp_customer_third_platform set union_id='"+unionId+"' where user_id='"+userId+"'");
	}

	
	/**
	 * 根据userId 补充密码
	 */
	public void updatePwd(int userId,String pwd){
		customerDao.executeSqlUpdate("update ytsp_customer set password = '"+pwd+"'  where id = "+ userId );
	}
	
	/**
	 * 根据userid 修改用账号
	 * @param userId,account
	 */
	public void updateAccountByUserId(int userId,String account){
		customerDao.executeSqlUpdate("update ytsp_customer set account = '"+account+"'  where id = "+ userId );
	}
	
	
	/**
	 * 将手机号关联到用户
	 */
	public void updateRelateAccount(int userId,String phone){
		customerDao.executeSqlUpdate("update ytsp_customer set mobilephone= '"+phone+"', "
				+" phoneValidate=1  where id = "+ userId );
	}
	
	
	
	/**
	 * 该手机号码是否已经被绑定过
	 * @param moblie
	 * @return
	 */
	public boolean isverificateMoblie(String moblie)throws Exception{
		int count  = customerDao.getRecordCount("WHERE mobilephone = ? and phoneValidate = 1 ",new Object[] {moblie});
		return count>0 ? true : false;
	}
	
	/**
	 * 	该邮箱是否已经验证过
	 * @param email
	 * @throws Exception
	 */
	public boolean isverificateEmail(String email)throws Exception{
		int count = customerDao.getRecordCount(
				" WHERE email = ? and emailValidate = 1", new Object[] {email});
		return count>0 ? true : false;
	}
	
	/**
	 * 根据手机号 加 密码进行校验查询用户信息
	 * @param moblie password
	 * @throws Exception 
	 */
	public Customer findCustomrByMoblieAndPassword(String moblie,String password) throws Exception{
		return customerDao.findOneByHql(" WHERE mobilephone = ? AND password = ? and phoneValidate = 1",
				new Object[] { moblie, password });
	}
	
	/**
	 * 根据手机号码查询用户信息
	 */
	public  Customer findCustomerByPhone(String moblie) throws Exception{
		return customerDao.findOneByHql(" WHERE mobilephone = ? and phoneValidate = 1",
				new Object[] { moblie});
	}
	
	/**
	 * 根据手机号返回已经验证手机的账号信息
	 */
	public Customer findCustomerInfoByMoblie(String phone) throws Exception{
		return customerDao.findOneByHql(" WHERE mobilephone = ? AND phoneValidate =1 ",
				new Object[] { phone});
	}
 	
	/**
	 * 根据邮箱 加 密码进行校验查询用户信息
	 * @param customer
	 * @throws Exception
	 */
	public Customer findCustomrByEmailAndPassword(String email,String password) throws Exception{
		return customerDao.findOneByHql(" WHERE email = ? AND password = ? AND emailValidate = 1",
				new Object[] { email, password });
	}
	
	/**
	 * 手机快速登录／注册（手机号与已有用户名不一致添加用户）
	 * @throws SqlException 
	 */
	public Customer addCustomer(Customer customer,String otherInfo,String appDiv) throws Exception{
		customerDao.save(customer);
		// 注册硬件信息
		saveByNumber(customer.getTerminalNumber(), otherInfo,
				customer.getTerminalType(),customer.getTerminalVersion(),
				appDiv,customer.getRegisterIp());
		return customer;
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
	 * 保存发送验证码信息
	 */
	public ForgetPasswordCode saveForgetInfo(String phone,String code,int type) throws Exception{
		ForgetPasswordCode custVaild = new ForgetPasswordCode();
		Customer c = new Customer();
		c.setId(0);
		custVaild.setCustomer(c);
		custVaild.setIsSuccess(0);
		custVaild.setStartTime(new Date());
		custVaild.setEndTime(ValidateUtil.getEndTime());
		custVaild.setUserName(phone);
		custVaild.setType(type);
		custVaild.setCode(code);
		forgetPasswordCodeDao.save(custVaild);
		return custVaild;
	}
	
	/**
	 * 判断除自己以外用户名称是否已经存在
	 * @param validatTime
	 * @param time
	 * @return
	 */
	public boolean existAccount(String account,int userId) throws Exception {
		int count = customerDao.getRecordCount(" WHERE account = ? and id<>"+userId,
				new Object[] { account });
		return count > 0 ? true : false;
	}
	
	private boolean isValidateNumValid(Date validatTime,int time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(validatTime);
		cal.add(Calendar.MINUTE, time);
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		return now.before(cal);
	}
	
}
