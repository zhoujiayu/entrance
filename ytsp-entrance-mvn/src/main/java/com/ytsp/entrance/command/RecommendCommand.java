package com.ytsp.entrance.command;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.HardwareRegisterService;
import com.ytsp.entrance.service.RecommendService;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 获取推荐列表
 * 
 */
public class RecommendCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		return CommandList.CMD_RECOMMEND_LIST == getContext().getHead().getCommandCode();
	}

	@Override
	public ExecuteResult execute() {
		try {
			RecommendService rs = SystemInitialization.getApplicationContext().getBean(RecommendService.class);
			String plat = getContext().getHead().getPlatform();
			JSONArray array = null;
			Customer customer = getSessionCustomer()==null? null:getSessionCustomer().getCustomer();
			 getContext().getHead().setUid(customer==null?0:customer.getId());
			array = rs.getRecommendArray(MobileTypeEnum.valueOf(plat),getContext().getHead());
			JSONObject obj = new JSONObject();
			obj.put("recommendList", array);
			//对付苹果审核
//			SessionCustomer sc = getSessionCustomer();
//			if(sc!=null&&sc.getCustomer()!=null&&sc.getCustomer().getAccount()!=null){
//				if(sc.getCustomer().getAccount().equals("appletest")&&
//						!getContext().getHead().getVersion().equals("4.2")){
//					return null;
//				}
//			}
			//获取推荐列表的时候，记录硬件信息，暂时记录，等新版本更新了就不用记录了
//			String hardwareId = getContext().getHead().getUniqueId();
//			String otherInfo = getContext().getHead().getOtherInfo();
//			String platform = getContext().getHead().getPlatform();
//			String version = getContext().getHead().getVersion();
//			String appDiv = getContext().getHead().getAppDiv();
//			String ip = getContext().getHead().getIp();
//			HardwareRegisterService hrs = SystemInitialization.getApplicationContext().getBean(HardwareRegisterService.class);
//			hrs.saveByNumber(hardwareId, otherInfo,platform,version,appDiv,ip);
			
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐列表成功！", obj, this);
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

}
