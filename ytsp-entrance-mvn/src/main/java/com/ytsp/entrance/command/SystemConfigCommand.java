package com.ytsp.entrance.command;

import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.SystemParamService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;

/**
 * @author GENE
 * @description 广告命令
 * 
 */
public class SystemConfigCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_VIP_ENABLE == code ||
			CommandList.CMD_SYSTEM_CONFIG == code ||
			CommandList.CMD_ADVERTISEMENT_DISABLE == code || 
			CommandList.CMD_SYSTEM_CONFIG_RELOAD == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_VIP_ENABLE == code) {
				return enableVip();

			} else if (CommandList.CMD_ADVERTISEMENT_DISABLE == code) {
				return disableAdvertisement();

			} else if (CommandList.CMD_SYSTEM_CONFIG == code) {
				return readSystemConfig();

			} else if (CommandList.CMD_SYSTEM_CONFIG_RELOAD == code) {
				return reloadSystemConfig();

			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
		return null;
	}

	private ExecuteResult enableVip() throws Exception {
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String enableVip = spi.getValue(IConstants.ENABLE_VIP);
		boolean _enableVip = false;
		if(StringUtil.isNotNullNotEmpty(enableVip)){
			_enableVip = "true".equalsIgnoreCase(enableVip.trim()) ? true : false;
		}

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("enable", _enableVip);
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "判断是否启用VIP模块成功！", jsonObj, this);
	}
	
	private ExecuteResult disableAdvertisement() throws Exception {
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String disableAd = spi.getValue(IConstants.DISABLE_AD_KEY);
		boolean _disableAd = false;
		if(StringUtil.isNotNullNotEmpty(disableAd)){
			_disableAd = "true".equalsIgnoreCase(disableAd.trim()) ? true : false;
		}

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("disable", _disableAd);
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "判断是否禁用广告成功！", jsonObj, this);
	}
	
	private ExecuteResult readSystemConfig() throws Exception {
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		
		String platform = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		
		
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		
		//如果是iphone
		if(platform.equals("iphone"))
		{
			 isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			 inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
			 inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
		}
		
		String weiboDesc = spi.getValue(IConstants.WEIBO_DESC);
		String weiboLoginDesc = spi.getValue(IConstants.WEIBO_LOGIN_DESC);
		
		
		String weiboDescPhone = spi.getValue("weiboDescPhone");
		String weiboLoginDescPhone = spi.getValue("weiboLoginDescPhone");
		
		boolean _isInReview = false;
		if(StringUtil.isNotNullNotEmpty(isInReview)){
			_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true : false;
		}
		
		
		
		JSONObject jsonObjResult = new JSONObject();
		if(_isInReview && 
				inReviewPlatform.equals(platform) && inReviewVersion.equals(version))
		{
			//如果在审核中
			jsonObjResult.put("isInReview", true);
		}
		else
		{
			jsonObjResult.put("isInReview", false);
		}
		if("iphone".equals(platform))
		{
			weiboDesc = weiboDescPhone;
			weiboLoginDesc = weiboLoginDescPhone;
		}
		jsonObjResult.put("weiboDesc", weiboDesc);
		jsonObjResult.put("weiboLoginDesc", weiboLoginDesc);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "读取系统设置成功！", jsonObjResult, this);
	}
	

	private ExecuteResult reloadSystemConfig() throws Exception {
		
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String old_isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String old_inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String old_inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("old_isInReview", old_isInReview);
		jsonObj.put("old_inReviewPlatform", old_inReviewPlatform);
		jsonObj.put("old_inReviewVersion", old_inReviewVersion);
		
		SystemParamService cs = SystemInitialization.getApplicationContext().getBean(SystemParamService.class);
		cs.syncVar();
		
		spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		
		jsonObj.put("isInReview", isInReview);
		jsonObj.put("inReviewPlatform", inReviewPlatform);
		jsonObj.put("inReviewVersion", inReviewVersion);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, null, jsonObj, this);
	}

	
}
