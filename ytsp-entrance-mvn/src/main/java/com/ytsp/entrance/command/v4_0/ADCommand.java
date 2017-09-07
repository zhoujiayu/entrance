package com.ytsp.entrance.command.v4_0;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.LaunchAd;
import com.ytsp.db.enums.LaunchRedirectEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.LaunchAdService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;

public class ADCommand extends AbstractCommand{

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return ( code == CommandList.CMD_AD_LAUNCH);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try{
			LaunchAdService ls = SystemInitialization.getApplicationContext().getBean(LaunchAdService.class);
			List<LaunchAd> ret = ls.findLaunchAd();
			JSONArray array = new JSONArray();
			//正常状况下最多只能有两个返回值，一个是启动图，一个是启动后
			int flag = 0;
			int plat = MobileTypeEnum.valueOf(getContext().getHead().getPlatform()).getValue();
			String pad = "";
			if(plat==1)//ipad
				pad = "pad";
			else
				pad="phone";
			if(ret!=null&&ret.size()>0){
				for (LaunchAd ad:ret) {
					if((flag&1)>0&&ad.getAfterLaunch()==0)
						continue;
					if((flag&2)>0&&ad.getAfterLaunch()==1)
						continue;
					if(!ad.getPlatType().equals(pad))
						continue;
					JSONObject jo = new JSONObject();
					if(ad.getType()==LaunchRedirectEnum.ACTIVITY)
						jo.put("activityId", ad.getActivity().getActivity_id());
					if(ad.getType()==LaunchRedirectEnum.ALBUM){
						jo.put("albumId", ad.getAlbum().getId());
						jo.put("albumType", ad.getAlbum().getType().getValue());
					}
					if(ad.getType()==LaunchRedirectEnum.PRODUCT)
						jo.put("productCode", ad.getEbProduct().getProductCode());
					jo.put("afterLaunch", ad.getAfterLaunch());
					jo.put("id", ad.getId());
					jo.put("img", SystemManager.getInstance().getSystemConfig().getImgServerUrl()+ad.getImg());
					jo.put("type", ad.getType().getValue());
					jo.put("millis", ad.getMillis());
					array.put(jo);
					if(ad.getAfterLaunch()==0)
						flag++;
					else
						flag+=2;
				}
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取启动广告成功", array, this);
		}catch(Exception e){
			logger.info("获取启动广告" + code + " 失败 " + e);
			return getExceptionExecuteResult(e);
		}
	}
}
