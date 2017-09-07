package com.ytsp.entrance.command.v4_0;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.EbActivity;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbActivityService;
import com.ytsp.entrance.service.v4_0.EbSalesService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.DateTimeFormatter;

public class EbActivityCommand extends AbstractCommand{
	
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_EB_ACTIVITY_GET);
	}

	@Override
	public ExecuteResult execute()  {
		try{
			return getValidActivities();
		}catch(Exception e){
			logger.error("获取专题列表出错" + e.getMessage());
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"获取专题列表失败",null,this);
	}
	
	

	public ExecuteResult getValidActivities() throws Exception {
		EbActivityService eas = SystemInitialization.getApplicationContext().getBean(EbActivityService.class);
		List<EbActivity> ebActivities = eas.retrieveValidActivities();
		JSONArray array = new JSONArray();
		JSONObject o = null;
		int uid = getContext().getHead().getUid();//UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		EbSalesService es = SystemInitialization.getApplicationContext().getBean(EbSalesService.class);
		boolean youyou = false;
//		if (sc != null && sc.getCustomer() != null) {
//			//判断操作的用户与当前的session中用户是否一致.
//			Customer customer = sc.getCustomer();
//			if (uid != 0 &&customer.getId().intValue() == uid) {
//				List<EbSales> ret = es.find(uid);
//				for (EbSales ebSales:ret) {
//					if(ebSales.getAward().equals("YOUYOU")&&ebSales.getCount()>0)
//						youyou = true;
//					if(ebSales.getAward().equals("KOUZ"))
//						kouzhao=true;
//				}
//			}
//		}
		
		for(EbActivity ebActivity : ebActivities) {
			//两个活动专题是没秒杀的
//			if((!youyou)&&ebActivity.getActivityId()==21)//奥飞每日一砸活动
//				continue;
//			if((!kouzhao)&&ebActivity.getActivityId()==25) //口罩活动改成都能看到了
//				continue;
			o = new JSONObject();
			o.put("ebActivityId", ebActivity.getActivityId());
			o.put("ebActivityName", ebActivity.getActivityName());
			o.put("ebActivityImage", SystemManager.getInstance().
					getSystemConfig().getImgServerUrl() +ebActivity.getActivityImage());
			o.put("ebActivityDescription", ebActivity.getActivityDescription());
			o.put("startTime", DateTimeFormatter.dateTime2String(ebActivity.getStartTime()));
			o.put("endTime", DateTimeFormatter.dateTime2String(ebActivity.getEndTime()));
			o.put("ebActivityOff", ebActivity.getActivityOff());
			o.put("ebActivityLogo",  SystemManager.getInstance().
					getSystemConfig().getImgServerUrl() +ebActivity.getActivityLogo());
			array.put(o);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,"获取列表成功",array,this);
	}
}
