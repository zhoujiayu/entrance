package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.BabyVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.v5_0.BabyService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.Util;

public class BabyCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_BABY_QUERY 
				|| code == CommandList.CMD_BABY_SAVE
				||code == CommandList.CMD_BABY_DELETE
				||code == CommandList.CMD_BABY_UPDATE);
	}

	@Override
	public ExecuteResult execute() {
		// 验证权限.
		int userId = getContext().getHead().getUid();
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_BABY_QUERY) {
				return getBabys();
			} else if (code == CommandList.CMD_BABY_SAVE) {
				return saveBaby();
			}else if(code == CommandList.CMD_BABY_DELETE){
				return deleteBaby();
			}else if(code == CommandList.CMD_BABY_UPDATE){
				return updateBaby();
			}
			
		} catch (Exception e) {
			logger.error("BabyCommand," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	/**
	 * <p>
	 * 功能描述:更新宝宝信息
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * @throws Exception 
	 * @throws SqlException 
	 */
	private ExecuteResult updateBaby() throws SqlException, Exception {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		String name = "";
		int sex = 0;
		String birthday = "";
		if (reqBody.isNull("babyId")) {
			new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "babyId不能为空",
					result, this);
		}
		if (reqBody.isNull("name")) {
			new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "name不能为空",
					result, this);
		}
		if (reqBody.isNull("sex")) {
			new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "sex不能为空",
					result, this);
		}
		if (reqBody.isNull("birthday")) {
			new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "birthday不能为空",
					result, this);
		}
		//获取前台参数
		name = reqBody.getString("name");
		sex = reqBody.getInt("sex");
		birthday = reqBody.getString("birthday");
		int babyId = reqBody.getInt("babyId");
		
		//获取宝宝信息
		BabyService babyServ = SystemInitialization.getApplicationContext().getBean(BabyService.class);
		Baby baby = babyServ.getBabyById(babyId);
		baby.setSex(sex);
		baby.setBirthday(DateFormatter.string2Date(birthday));
		baby.setName(name);
		//更新宝宝
		babyServ.updateBaby(baby);
		Util.addStatistics(getContext(), baby);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "更新宝宝成功",
				result, this);
	}
	
	
	
	/**
	* <p>功能描述:删除宝宝</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult deleteBaby() throws SqlException, JSONException {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		if(reqBody.isNull("babyId")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "babyId不能为空", null,
					this);
		}
		int babyId = reqBody.getInt("babyId");
		BabyService babyServ = SystemInitialization.getApplicationContext().getBean(BabyService.class);
		Baby baby = babyServ.getBabyById(babyId);
		if(baby == null){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "所选的宝宝不存在！", null,
					this);
		}
		baby.setStatus(ValidStatusEnum.INVALID);
		babyServ.updateBaby(baby);
		Util.addStatistics(getContext(), baby);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "删除宝宝成功", result,
				this);
	}
	
	/**
	 * <p>
	 * 功能描述:获取宝宝信息
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * @throws SqlException 
	 * @throws JSONException 
	 */
	private ExecuteResult getBabys() throws SqlException, JSONException {
		JSONObject result = new JSONObject();
		int userId = getContext().getHead().getUid();
		List<Baby> babys = getCustomerBabys(userId);
		if(babys == null || babys.size() <= 0){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取宝宝成功", result,
					this);
		}
		BabyInfoVO babyInfo = new BabyInfoVO();
		babyInfo.setBabys(buildBabyVOs(babys));
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(babyInfo));
		result.put("result", true);
		//添加统计数据
		Util.addStatistics(getContext(), babyInfo);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取宝宝成功", result,
				this);
	}
	
	/**
	 * <p>
	 * 功能描述:保存宝宝信息
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * @throws Exception 
	 * @throws SqlException 
	 */
	private ExecuteResult saveBaby() throws SqlException, Exception {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int userId = getContext().getHead().getUid();

		List<Baby> babys = getCustomerBabys(userId);
		if(babys!=null&&babys.size()>=3){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "宝宝数量不能超过3个",
					result, this);
		}
		String name = "";
		int sex = 0;
		String birthday = "";
		if (reqBody.isNull("name")) {
			result.put("result", false);
			result.put("msg", "name字段不能为空");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "name不能为空",
					result, this);
		}
		if (reqBody.isNull("sex")) {
			result.put("result", false);
			result.put("msg", "sex字段不能为空");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "sex不能为空",
					result, this);
		}
		if (reqBody.isNull("birthday")) {
			result.put("result", false);
			result.put("msg", "birthday字段不能为空");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "birthday不能为空",
					result, this);
		}
		name = reqBody.getString("name");
		sex = reqBody.getInt("sex");
		birthday = reqBody.getString("birthday");
		BabyService babyServ = SystemInitialization.getApplicationContext().getBean(BabyService.class);
		Baby baby = createBaby(name,sex,birthday,userId);
		babyServ.saveBaby(baby);
		result.put("result", true);
		Util.addStatistics(getContext(), baby);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "宝宝设置成功",
				result, this);
	}
	
	/**
	* <p>功能描述:构建宝宝VO</p>
	* <p>参数：@param babys
	* <p>参数：@return</p>
	* <p>返回类型：List<BabyVO></p>
	 */
	private List<BabyVO> buildBabyVOs(List<Baby> babys){
		List<BabyVO> vos = new ArrayList<BabyVO>(); 
		if(babys == null || babys.size() <= 0){
			return null;
		}
		for (Baby baby : babys) {
			BabyVO babyvo = new BabyVO();
			babyvo.setAge(countAge(baby.getBirthday()));
			babyvo.setName(baby.getName());
			babyvo.setSex(baby.getSex());
			babyvo.setId(baby.getId());
			babyvo.setBirthday(DateFormatter.date2String(baby.getBirthday()));
			vos.add(babyvo);
		}
		return vos;
	}
	
	/**
	* <p>功能描述:计算年龄：格式为：几岁几个月</p>
	* <p>参数：@param birthDay
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String countAge(Date birthDay){
		Calendar cal = Calendar.getInstance();

        if (cal.getTime().before(birthDay)) {
            return "0岁0个月";
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH)+1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
       
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH)+1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;
        int month = 0;
        
        //未过生日
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
            	//未过生日，将年龄减1
                if (dayOfMonthNow < dayOfMonthBirth) {
                	month = 11;
                    age--;
                }
            } else {
                age--;
                month = 12 - (monthBirth - monthNow);
                if (dayOfMonthNow < dayOfMonthBirth) {
                	month--;
                }
            }
        }else{
        	month = monthNow - monthBirth;
            if (dayOfMonthNow < dayOfMonthBirth) {
              month--;
            }
        }
        return age +"岁"+month+"个月";
	}
	
	/**
	* <p>功能描述:创建宝宝</p>
	* <p>参数：@param name 宝宝姓名
	* <p>参数：@param sex 性别
	* <p>参数：@param birthday 生日
	* <p>参数：@param userId 用户id
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：Baby</p>
	 */
	private Baby createBaby(String name,int sex,String birthday,int userId) throws Exception{
		Baby baby = new Baby();
		baby.setCustomer(getCustomer(userId));
		baby.setSex(sex);
		baby.setBirthday(DateFormatter.string2Date(birthday));
		baby.setName(name);
		return baby;
		
	}
	
	/**
	* <p>功能描述:获取用户</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：Customer</p>
	 * @throws Exception 
	 */
	private Customer getCustomer(int userId) throws Exception{
		CustomerService custServ = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
		return custServ.findCustomerById(userId);
	}
	
	/**
	* <p>功能描述:获取客户宝宝信息</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<Baby></p>
	 */
	private List<Baby> getCustomerBabys(int userId) throws SqlException{
		BabyService babyServ = SystemInitialization.getApplicationContext().getBean(BabyService.class);
		return babyServ.getCustomerBaby(userId);
	}
	
	/**
	 *宝宝信息VO
	 */
	class BabyInfoVO{
		List<BabyVO> babys;

		public List<BabyVO> getBabys() {
			return babys;
		}

		public void setBabys(List<BabyVO> babys) {
			this.babys = babys;
		}
	}
	
}
