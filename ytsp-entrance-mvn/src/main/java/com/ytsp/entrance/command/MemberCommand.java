package com.ytsp.entrance.command;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.enums.CardValidateEnum;
import com.ytsp.db.enums.MemberTypeEnum;
import com.ytsp.db.enums.RechargeStatusEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.HttpUtil;
import com.ytsp.entrance.util.Util;

public class MemberCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_MEMBER_RECHARGE == code
				|| CommandList.CMD_MEMBER_VALIDATE_APP_RECHGARGE == code
				|| CommandList.CMD_MEMBER_VIDEO_LIST == code
				|| CommandList.CMD_MEMBER_VIDEO_PLAY == code
				|| CommandList.CMD_MEMBER_VIDEO_DOWNLOAD == code
				|| CommandList.CMD_MEMBER_COST_DEFINE == code
				|| CommandList.CMD_MEMBER_CHECK == code
				|| CommandList.CMD_MEMBER_CHECK_V2_5 == code
				|| CommandList.CMD_MEMBER_VALIDATE_APP_RECHGARGE_V5 == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_MEMBER_RECHARGE == code) {
				return memberRecharge();
			} else if (CommandList.CMD_MEMBER_VALIDATE_APP_RECHGARGE == code) {
				return memberValidateAppRecharge();
			} else if (CommandList.CMD_MEMBER_VIDEO_LIST == code) {
				return memberVideos();
			} else if (CommandList.CMD_MEMBER_VIDEO_PLAY == code) {
				return memberPlayVideo();
			} else if (CommandList.CMD_MEMBER_VIDEO_DOWNLOAD == code) {
				return memberDownloadVideo();
			} else if (CommandList.CMD_MEMBER_COST_DEFINE == code) {
				return memberCostDefine();
			} else if (CommandList.CMD_MEMBER_CHECK == code) {
				return isMember();
			} else if (CommandList.CMD_MEMBER_CHECK_V2_5 == code) {
				return isMember_v2_5();
			} else if (CommandList.CMD_MEMBER_VALIDATE_APP_RECHGARGE_V5 == code) {
				return newMemberValidateAppRecharge();
			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	/**
	 * 会员充值
	 * @since 4.4
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult memberRecharge() throws Exception {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			JSONObject result = new JSONObject();
			result.put("memberType", MemberTypeEnum.NOMEMBER.getValue());
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			if ( jsonObj.has("cardCode") ) {
				int userId = getContext().getHead().getUid();
				String cardCode = jsonObj.getString("cardCode");
//				String cardPassword = jsonObj.getString("cardPassword");
				String ip = getContext().getHead().getIp();
				String version = getContext().getHead().getVersion();
				String platform = getContext().getHead().getPlatform();
				result = memberService.saveCardRecharge(cardCode,userId, platform, version, ip);
			} else {
				result.put("status", CardValidateEnum.PARAM_ERROR.getValue());
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "充值成功！",result, this);
		} catch (Exception e) {
			logger.error("memberRecharge() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}

	
	private final static String sharePassword = "31e7093f0c854facbca7b7115155a353";
	final static String appVerify_sandbox = "https://sandbox.itunes.apple.com/verifyReceipt";
	final static String appVerify = "https://buy.itunes.apple.com/verifyReceipt";
	/**
	 * 客户端支付后，到后台验证
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult memberValidateAppRecharge() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String receipt = jsonObj.getString("transactionReceipt");
			JSONObject obj = new JSONObject();
			obj.put("receipt-data", receipt);
			obj.put("password", sharePassword);
			String plat = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			StringBuffer sb = new StringBuffer();
			boolean isSandBox = false;
			//针对审核中和正式的版本使用不同的验证url，审核中的版本号应该配置化
//			SystemParamInDB params = SystemManager.getInstance().getSystemParamInDB();
//			String isInReview = params.getValue(IConstants.IS_IN_REVIEW_KEY);
//			if((MobileTypeEnum.iphone.getText().equals(plat)&&"5.0.4".equals(version))||
//					(MobileTypeEnum.ipad.getText().equals(plat) &&
//					"5.0.0".equals(version))){
			if(Util.isIOSInReview(plat, version)){
				sb.append(HttpUtil.submitPost(appVerify_sandbox, obj.toString()));
				isSandBox = true;
			}
			else
				sb.append(HttpUtil.submitPost(appVerify, obj.toString()));
			JSONObject objReturn = new JSONObject(sb.toString());
			int status = objReturn.getInt("status");
			JSONObject result = new JSONObject();
			logger.info(objReturn);
			logger.info(status);
			if(jsonObj.has("uid"))
			{
				MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
				int userId = jsonObj.getInt("uid");
				String ip = getContext().getHead().getIp();
				if(status==0){
					JSONObject receiptObj =objReturn.getJSONObject("receipt");
					memberService.saveAppleRechargeReturn(userId,receiptObj,receipt,
							getContext().getHead().getPlatform(),getContext().getHead().getVersion(),ip,isSandBox);
					result.put("status", RechargeStatusEnum.RECHARGE_SUCCESS.getValue());
				}
				else{
					result.put("status", RechargeStatusEnum.RECHARGE_LOSE.getValue());
				}
			}
			else
			{	
				if(Util.isIOSInReview(plat, version)){
					result.put("status", RechargeStatusEnum.RECHARGE_SUCCESS.getValue());
				}else{
					result.put("status", RechargeStatusEnum.RECHARGE_ERROR.getValue());
				}
			}
			JSONObject receiptObj =objReturn.getJSONObject("receipt");
			String product_id = receiptObj.optString("product_id");
			MemberService memberServ = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			String vipEndTime = memberServ.getVipEndTime(jsonObj.optInt("uid"), product_id);
			result.put("vipEndTime", vipEndTime);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证苹果支付成功！", result, this);
		} catch (Exception e) {
			logger.error("memberValidateAppRecharge() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	 * 客户端支付后，到后台验证
	 * @return
	 * @throws Exception
	 */
	public ExecuteResult newMemberValidateAppRecharge() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String receipt = jsonObj.getString("transactionReceipt");
			String transactionId = jsonObj.optString("transactionId");
			JSONObject obj = new JSONObject();
			obj.put("receipt-data", receipt);
			obj.put("password", sharePassword);
			System.out.println("请求数据："+obj.toString());
			String plat = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			StringBuffer sb = new StringBuffer();
			boolean isSandBox = false;
			//针对审核中和正式的版本使用不同的验证url，审核中的版本号应该配置化
			if (Util.isIOSInReview(plat, version)) {
				sb.append(HttpUtil.submitPost(appVerify_sandbox, obj.toString()));
				isSandBox = true;
			} else {
				sb.append(HttpUtil.submitPost(appVerify, obj.toString()));
			}
			JSONObject objReturn = new JSONObject(sb.toString());
			System.out.println(objReturn);
			int status = objReturn.getInt("status");
			JSONObject result = new JSONObject();
			logger.info(objReturn);
			logger.info(status);
			if(jsonObj.has("uid"))
			{
				MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
				int userId = jsonObj.getInt("uid");
				String ip = getContext().getHead().getIp();
				if(status==0){
					JSONObject receiptObj =objReturn.getJSONObject("receipt");
					memberService.saveNewAppleRechargeReturn(userId,receiptObj,receipt,
							getContext().getHead().getPlatform(),getContext().getHead().getVersion(),ip,isSandBox,transactionId);
					result.put("status", RechargeStatusEnum.RECHARGE_SUCCESS.getValue());
				}
				else{
					result.put("status", RechargeStatusEnum.RECHARGE_LOSE.getValue());
				}
			}
			else
			{	
				if(Util.isIOSInReview(plat, version)){
					result.put("status", RechargeStatusEnum.RECHARGE_SUCCESS.getValue());
				}else{
					result.put("status", RechargeStatusEnum.RECHARGE_ERROR.getValue());
				}
			}
			JSONObject receiptObj =objReturn.getJSONObject("receipt");
//			String product_id = receiptObj.optString("product_id");
			String product_id = getTransactionValue(receiptObj, transactionId, "product_id");
			MemberService memberServ = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			String vipEndTime = memberServ.getVipEndTime(jsonObj.optInt("uid"), product_id);
			result.put("vipEndTime", vipEndTime);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证苹果支付成功！", result, this);
		} catch (Exception e) {
			logger.error("memberValidateAppRecharge() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* 功能描述:获取当前交易
	* 参数：@param receiptObj
	* 参数：@return
	* 返回类型:JSONObject
	 * @throws JSONException 
	 */
	private String getTransactionValue(JSONObject receiptObj,String transactionId,String key) throws JSONException{
		if(!receiptObj.has("in_app")){
			return null;
		}
		JSONArray appArray = receiptObj.getJSONArray("in_app");
		if(appArray == null){
			return null;
		}
		if(StringUtil.isNullOrEmpty(transactionId)){
			JSONObject transaction = appArray.getJSONObject(appArray.length() - 1);
			return transaction.optString(key);
		}
		for (int i = 0; i <appArray.length(); i++) {
			JSONObject transaction = appArray.getJSONObject(i);
			String transId = transaction.optString("transaction_id");
			if(transId.equals(transactionId)){
				return transaction.optString(key);
			}
		}
		JSONObject transaction = appArray.getJSONObject(appArray.length() - 1);
		return transaction.optString(key);
	}
	
	
	/**
	 * 获取点数价格定义
	 * @return
	 */
	public ExecuteResult memberCostDefine() {
		try {
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			String platform = getContext().getHead().getPlatform();
			JSONArray array = memberService.getMemberCostDefines(platform);
			JSONObject obj = new JSONObject();
			obj.put("memberCostDefine", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取会员卡价格定义成功！", obj, this);
		} catch (Exception e) {
			logger.error("memberCostDefine() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 会员剧集列表
	 * 
	 * @return
	 * @throws Exception
	 */
	private ExecuteResult memberVideos() throws Exception {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
//			if(getSessionCustomer()!=null&&getSessionCustomer().getCustomer()!=null
//					&&getSessionCustomer().getCustomer().getId()==24972&&!"3.6".equals(version))
//				return null;
			if (jsonObj.isNull("aid")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null,this);
			}
			int userId = getContext().getHead().getUid();
			int aid = jsonObj.getInt("aid");
			int start = 0;
			int limit = -1;
			if (!jsonObj.isNull("start")) {
				start = jsonObj.getInt("start");
			}
			if (!jsonObj.isNull("limit")) {
				limit = jsonObj.getInt("limit");
			}
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			JSONArray array = memberService.getMemberVideos(userId, aid, platform, version, start, limit);
			JSONObject obj = new JSONObject();
			obj.put("videoList", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取视频列表成功！",obj, this);
		} catch (Exception e) {
			logger.error("memberVideos() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 会员播放视频
	 * 
	 * @return
	 */
	public ExecuteResult memberPlayVideo() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (jsonObj.isNull("vid") || jsonObj.isNull("aid")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误",null, this);
			}
			int userId = getContext().getHead().getUid();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String terminalNumber = getContext().getHead().getUniqueId();
			String ip = getContext().getHead().getIp();
			int videoId = jsonObj.getInt("vid");
			int albumId = jsonObj.getInt("aid");
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberService.savePlayVideo(userId, videoId, albumId, platform, version, terminalNumber, ip);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "播放视频成功！", obj, this);
		} catch (Exception e) {
			logger.error("memberPlayVideo() error , " +
					"HeadInfo:"+ getContext().getHead().toString(),e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 会员下载视频
	 * @return
	 */
	public ExecuteResult memberDownloadVideo() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int userId = getContext().getHead().getUid();
			if (jsonObj.isNull("vid") || jsonObj.isNull("aid")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误",null, this);
			}
			int videoId = jsonObj.getInt("vid");
			int albumId = jsonObj.getInt("aid");
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberService.saveDownloadVideo(
					getContext().getHead().getVersion(),
					userId,videoId,albumId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "验证下载消费成功！", obj, this);
		} catch (Exception e) {
			logger.error("memberDownloadVideo() error , " +
					"HeadInfo:"+ getContext().getHead().toString(),e);
			return getExceptionExecuteResult(e);
		}
	}
	
	public ExecuteResult isMember() {
		try {
			int userId = getContext().getHead().getUid();
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberService.memberCheck(userId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "检查是否会员成功！", obj, this);
		} catch (Exception e) {
			logger.error("isMember() error , " +
					"HeadInfo:"+ getContext().getHead().toString(),e);
			return getExceptionExecuteResult(e);
		}
	}
	
	public ExecuteResult isMember_v2_5() {
		try {
			int userId = getContext().getHead().getUid();
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = memberService.memberCheck_v2_5(userId);
			//添加统计数据
			Util.addStatistics(getContext(), obj.toString());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "检查是否会员成功！", obj, this);
		} catch (Exception e) {
			logger.error("isMember_v2_5() error , " +
					"HeadInfo:"+ getContext().getHead().toString(),e);
			return getExceptionExecuteResult(e);
		}
	}
}
