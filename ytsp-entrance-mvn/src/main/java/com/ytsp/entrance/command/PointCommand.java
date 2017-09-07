package com.ytsp.entrance.command;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.enums.CardValidateEnum;
import com.ytsp.db.enums.RechargeStatusEnum;
import com.ytsp.db.enums.RechargeTypeEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.PointService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.HttpUtil;
import com.ytsp.entrance.util.VerifyClientCustomer;
import com.ytsp.entrance.util.VerifyClientParams;

@Deprecated 
public class PointCommand  extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_POINT_FEEDBACKS == code
				|| CommandList.CMD_POINT_SAVA_CONSUME_ACTION == code
				|| CommandList.CMD_POINT_SAVA_CONSUME_VIDEO == code
				|| CommandList.CMD_POINT_VALIDATA_APP_RECGARGE == code
				|| CommandList.CMD_POINT_SAVA_CONSUME_VIDEO_DOWNLOAD == code
				|| CommandList.CMD_POINT_SAVA_VIDEO_DOWNLOAD == code
				|| CommandList.CMD_POINT_REGIST == code
				|| CommandList.CMD_POINT_LOGIN == code
				|| CommandList.CMD_WEICO_CANVASSING == code
				|| CommandList.CMD_POINT_VALIDATA_CARD_RECGARGE == code
				|| CommandList.CMD_POINT_CONSUME_RECORD == code
				|| CommandList.CMD_POINT_RECHARGE_RECORD == code
				|| CommandList.CMD_POINT_RECHARGE_CONSUME_RECORD == code
				|| CommandList.CMD_POINT_BALANCE == code
				|| CommandList.CMD_POINT_COST_DEFINE == code 
				|| CommandList.CMD_POINT_QUERY_CONSUME_CUSTOMER_VIDEO == code
				|| CommandList.CMD_POINT_LOGIN_LOG == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_POINT_FEEDBACKS == code) {
				return queryAllFeedbacks();
			} else if (CommandList.CMD_POINT_SAVA_CONSUME_VIDEO == code) {
				return saveVideoConsume();
			} else if (CommandList.CMD_POINT_VALIDATA_APP_RECGARGE == code) {
				return validateAppRecharge();
			} else if (CommandList.CMD_POINT_SAVA_CONSUME_VIDEO_DOWNLOAD == code) {
				return saveVideoDownloadConsum();
			} else if (CommandList.CMD_POINT_SAVA_VIDEO_DOWNLOAD == code) {
				return saveVideoDownload();
			} else if (CommandList.CMD_POINT_REGIST == code) {
				return saveRegist();
			}  else if (CommandList.CMD_WEICO_CANVASSING == code) {
				return saveWeicoCanvassing();
			} else if (CommandList.CMD_POINT_VALIDATA_CARD_RECGARGE == code) {
				return cardRecharge();
			} else if (CommandList.CMD_POINT_CONSUME_RECORD == code) {
				return getPointConsumeRecord();
			} else if (CommandList.CMD_POINT_RECHARGE_RECORD == code) {
				return getPointRechargeRecord();
			} else if (CommandList.CMD_POINT_RECHARGE_CONSUME_RECORD == code) {
				return getPointRechargeConsumeRecord();
			} else if (CommandList.CMD_POINT_BALANCE == code) {
				return getPointBalance();
			} else if (CommandList.CMD_POINT_COST_DEFINE == code){
				return getPointCostDefine();
			} else if (CommandList.CMD_POINT_QUERY_CONSUME_CUSTOMER_VIDEO == code){
				return queryCustomerVideosByAid();
			} else if (CommandList.CMD_POINT_LOGIN_LOG == code) {
				return getLoginLog();
			}
			
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}
	
	/**
	 * 微博拉票赠送点数
	 * @return
	 */
	public ExecuteResult saveWeicoCanvassing() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String terminalNumber = getContext().getHead().getUniqueId();
			int userId = getContext().getHead().getUid();
			JSONObject json = pointService.saveWeicoCanvassing(userId, RechargeTypeEnum.SHARE_WEIBO, platform, version,terminalNumber);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "微博拉票赠送点数成功！", json, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 注册并赠送点数
	 * @return
	 */
	public ExecuteResult saveRegist() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			String terminalNumber = getContext().getHead().getUniqueId();
			String otherInfo = getContext().getHead().getOtherInfo();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();
			HttpSession httpSession = super.getSession();
			
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
			
			if(!password.equals(password2)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "重复密码与密码不一致！", null, this);
			}
			
			if(!VerifyClientCustomer.accountValidate(account)){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "账户名称必须是长度4-30（中文算2个字符）的中文、数字、字母、下划线组合！", null, this);
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
			
			JSONObject json = pointService.saveRegist(password, account, nick, ip, platform, version, terminalNumber, email, otherInfo, appDiv, httpSession);
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "账户注册成功！", json, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}

	private final static String appVerify = "https://sandbox.itunes.apple.com/verifyReceipt";
//	private final static String appVerify = "https://buy.itunes.apple.com/verifyReceipt";
	
	
	/**
	 * 充值卡充值
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult cardRecharge() throws Exception
	{
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		if(!VerifyClientParams.verifyClient(jsonObj))
		{
			return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION, "身份验证失败！", null, this);
		}
		CardValidateEnum trade_status = CardValidateEnum.SUCCESS;
		PointService fs = SystemInitialization.getApplicationContext().getBean(PointService.class);
		if(jsonObj.has("uid") && jsonObj.has("cardCode") && jsonObj.has("cardPassword") && jsonObj.has("rechargeCode"))
		{
			int userId = jsonObj.getInt("uid");
			String cardCode = jsonObj.getString("cardCode");
			String cardPassword = jsonObj.getString("cardPassword");
			String rechargeCode = jsonObj.getString("rechargeCode");
			//先判断是否重复请求了
			if(fs.isRepeatRechargeByOrderCode(rechargeCode))
			{
				trade_status =  CardValidateEnum.REQUEST_OFTEN;
			}
			else
			{
				String ip = getContext().getHead().getIp();
				trade_status = fs.saveCardRecharge(rechargeCode,cardCode, cardPassword, userId,getContext().getHead().getPlatform(),getContext().getHead().getVersion(),ip);
			}
		}
		else
		{
			trade_status = CardValidateEnum.PARAM_ERROR;
		}
		JSONObject result = new JSONObject();
		result.put("status", trade_status.getValue());
		result.put("desc", trade_status.getDescription());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证充值卡成功！", result, this);
	}
	
	/**
	 * 客户端支付后，到后台验证
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult validateAppRecharge() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if(!VerifyClientParams.verifyClient(jsonObj))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION, "身份验证失败！", null, this);
			}
			
			
			String receipt = jsonObj.getString("transactionReceipt");
			JSONObject obj = new JSONObject();
			obj.put("receipt-data", receipt);
			
			StringBuffer sb =HttpUtil.submitPost(appVerify, obj.toString());
			JSONObject objReturn = new JSONObject(sb.toString());
			int status = objReturn.getInt("status");
			
			JSONObject result = new JSONObject();
			
			if(status == 0)
			{
				PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
				JSONObject receiptObj =objReturn.getJSONObject("receipt");
				//验证成功，
				//处理支付成功的操作
				if(jsonObj.has("uid"))
				{
					int userId = jsonObj.getInt("uid");
					String ip = getContext().getHead().getIp();
					pointService.saveAppleRechargeReturn(userId,receiptObj,receipt,getContext().getHead().getPlatform(),getContext().getHead().getVersion(),ip);
					result.put("status", RechargeStatusEnum.RECHARGE_SUCCESS.getValue());
				}
				else
				{
					result.put("status", RechargeStatusEnum.RECHARGE_ERROR.getValue());
				}
			}
			else
			{
				result.put("status", RechargeStatusEnum.RECHARGE_LOSE.getValue());
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证苹果支付成功！", result, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}
	

	/**
	 * 所有反馈
	 * @return
	 * @throws Exception
	 */
	private ExecuteResult queryAllFeedbacks() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			JSONObject obj = new JSONObject();
			JSONArray array = pointService.queryFeedbacks();
			obj.put("list", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取所有反馈成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}

	
	/**
	 * 保存剧集消费
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult saveVideoConsume() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if(!VerifyClientParams.verifyClient(jsonObj))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION, "身份验证失败！", null, this);
			}
//			Node node = BalanceManager.getInstance().getSelector().findFree(null);
//			if (node != null) {
//				String serviceUrl = node.getDatas().getProperty("service");
				//某些错误的调用，导致vid没有值
				if(jsonObj.isNull("vid") || jsonObj.isNull("aid") )
				{
					return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null, this);
				}
				int userId = getContext().getHead().getUid();
				String platform = getContext().getHead().getPlatform();
				String version = getContext().getHead().getVersion();
				String terminalNumber = getContext().getHead().getUniqueId();
				String ip = getContext().getHead().getIp();
				
				int videoId = jsonObj.getInt("vid");
				int albumId = jsonObj.getInt("aid");
				String consumeCode = jsonObj.getString("consumeCode");
				PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
				JSONObject obj = pointService.saveVideoConsume(userId,videoId,albumId,consumeCode,platform,version,terminalNumber,ip);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存剧集消费成功！", obj, this);
//			} else {
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "镜像服务器忙！", null, this);
//			}
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 验证下载消费
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult saveVideoDownloadConsum() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int userId = getContext().getHead().getUid();
			int videoId = jsonObj.getInt("vid");
			int albumId = jsonObj.getInt("aid");
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			JSONObject obj = pointService.saveVideoDownloadConsume(userId,videoId,albumId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证下载消费成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 保存剧集下载消费
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult saveVideoDownload() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if(!VerifyClientParams.verifyClient(jsonObj))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION, "身份验证失败！", null, this);
			}
//			Node node = BalanceManager.getInstance().getSelector().findFree(null);
//			if(node == null) {
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "镜像服务器忙！", null, this);
//			}
//			String serviceUrl = node.getDatas().getProperty("service");
			int userId = getContext().getHead().getUid();
			String terminalType = getContext().getHead().getPlatform();
			String terminalVersion = getContext().getHead().getVersion();
			String terminalNumber = getContext().getHead().getUniqueId();
			String ip = getContext().getHead().getIp();
			int videoId = jsonObj.getInt("vid");
			int albumId = jsonObj.getInt("aid");
			int free = jsonObj.getInt("free");
			boolean bFree = false;
			if(free == 0) {
				bFree = true;
			}
			String consumeCode = jsonObj.getString("consumeCode");
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			return pointService.saveVideoDownload(this, albumId, videoId, userId, consumeCode, bFree, ip, terminalType, terminalVersion, terminalNumber);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 获取消费纪录
	 * @return
	 */
	public ExecuteResult getPointConsumeRecord() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			int userId = getContext().getHead().getUid();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int start = 0;
			int limit = -1;
			if(!jsonObj.isNull("start"))
			{
				start = jsonObj.getInt("start");
			}
			if(!jsonObj.isNull("limit"))
			{
				limit = jsonObj.getInt("limit");
			}
			JSONArray array = pointService.getPointConsumeRecord(userId, start, limit);
			int count = pointService.getPointConsumeRecord(userId,-1,-1).length();
			JSONObject obj = new JSONObject();
			obj.put("count", count);
			obj.put("pointConsumeRecord", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取消费纪录成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 获取充值纪录
	 * @return
	 */
	public ExecuteResult getPointRechargeRecord() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			int userId = getContext().getHead().getUid();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int start = 0;
			int limit = -1;
			if(!jsonObj.isNull("start"))
			{
				start = jsonObj.getInt("start");
			}
			if(!jsonObj.isNull("limit"))
			{
				limit = jsonObj.getInt("limit");
			}
			JSONArray array = pointService.getPointRechargeRecord(userId, start, limit);
			int count = pointService.getPointRechargeRecord(userId,-1,-1).length();
			JSONObject obj = new JSONObject();
			obj.put("count", count);
			obj.put("pointRechargeRecord", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取充值纪录成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 获取充值消费纪录
	 * @return
	 */
	public ExecuteResult getPointRechargeConsumeRecord() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			int userId = getContext().getHead().getUid();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int start = 0;
			int limit = -1;
			if(!jsonObj.isNull("start"))
			{
				start = jsonObj.getInt("start");
			}
			if(!jsonObj.isNull("limit"))
			{
				limit = jsonObj.getInt("limit");
			}
			JSONArray array = pointService.getPointRechargeConsumeRecord(userId, start, limit);
			int count = pointService.getPointRechargeConsumeRecord(userId,-1,-1).length();
			JSONObject obj = new JSONObject();
			obj.put("count", count);
			obj.put("pointRechargeConsumeRecord", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取充值消费纪录成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 获取用户剩余点数
	 * @return
	 */
	public ExecuteResult getPointBalance() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			int userId = getContext().getHead().getUid();
			int balance = pointService.getPointBalance(userId);
			JSONObject obj = new JSONObject();
			obj.put("balance", balance);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取剩余点数成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 获取点数价格定义
	 * @return
	 */
	public ExecuteResult getPointCostDefine() {
		try {
			PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
			String platform = getContext().getHead().getPlatform();
			JSONArray array = pointService.getPointCostDefines(platform);
			JSONObject obj = new JSONObject();
			obj.put("pointCostDefine", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取点数价格定义成功！", obj, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	private ExecuteResult queryCustomerVideosByAid() throws Exception {

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		
		
		if(jsonObj.isNull("aid") || jsonObj.isNull("userId"))
		{
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null, this);
		}
		int aid = jsonObj.getInt("aid");
		int userId = jsonObj.getInt("userId");
		int start = 0;
		int limit = -1;
		if(!jsonObj.isNull("start"))
		{
			start = jsonObj.getInt("start");
		}
		if(!jsonObj.isNull("limit"))
		{
			limit = jsonObj.getInt("limit");
		}
		PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
		JSONArray array = pointService.getAlbumVideoArray(userId,aid, start, limit);

		JSONObject obj = new JSONObject();
		obj.put("videoList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取视频列表成功！", obj, this);
	}
	
	private ExecuteResult getLoginLog() throws Exception {
		int userId = getContext().getHead().getUid();
		PointService pointService = SystemInitialization.getApplicationContext().getBean(PointService.class);
		JSONObject obj = pointService.getLoginLog(userId);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取视频列表成功！", obj, this);
	}
}
