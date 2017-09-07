package com.ytsp.entrance.command;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.ParentControl;
import com.ytsp.db.domain.ParentTimeControl;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.ParentControlService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 家长控制
 * 
 */
public class ParentControlCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		
		return 
		CommandList.CMD_PARENTCONTROL_LOCALTIME_SAVE == code || 
		CommandList.CMD_PARENTCONTROL_ONLINETIME_SAVE == code || 
		CommandList.CMD_PARENTCONTROL_LOCALTIME_READ == code || 
		CommandList.CMD_PARENTCONTROL_ONLINETIME_READ == code || 
		CommandList.CMD_PARENTTIMECONTROL_LIST == code || 
		CommandList.CMD_PARENTTIMECONTROL_ADD == code || 
		CommandList.CMD_PARENTTIMECONTROL_DELETE == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_PARENTCONTROL_LOCALTIME_SAVE == code) {
				return saveLocalTime();

			} else if (CommandList.CMD_PARENTCONTROL_ONLINETIME_SAVE == code) {
				return saveOnlineTime();

			} else if (CommandList.CMD_PARENTCONTROL_LOCALTIME_READ == code) {
				return readLocalTime();

			} else if (CommandList.CMD_PARENTCONTROL_ONLINETIME_READ == code) {
				return readOnlineTime();

			} else if (CommandList.CMD_PARENTTIMECONTROL_LIST == code) {
				return listParentTimeCtrl();

			} else if (CommandList.CMD_PARENTTIMECONTROL_ADD == code) {
				return addParentTimeCtrl();

			} else if (CommandList.CMD_PARENTTIMECONTROL_DELETE == code) {
				return deleteParentTimeCtrl();

			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}

	private ExecuteResult saveLocalTime() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		ParentControl parentControl = pcs.getParentControlByCustomerId(customer.getId());
		if (parentControl == null) {
			parentControl = new ParentControl();
			parentControl.setCustomer(customer);
		}
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		boolean open = jsonObj.getBoolean("open");
		if(!Boolean.valueOf(open).equals(parentControl.getLocalTimeCtrl())){
			parentControl.setLocalTimeCtrl(open);
			pcs.saveOrUpdateParentControl(parentControl);
		}
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "修改本地视频时间控制成功！", null, this);
		
	}
	
	private ExecuteResult saveOnlineTime() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		ParentControl parentControl = pcs.getParentControlByCustomerId(customer.getId());
		if (parentControl == null) {
			parentControl = new ParentControl();
			parentControl.setCustomer(customer);
		}
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		boolean open = jsonObj.getBoolean("open");
		if(!Boolean.valueOf(open).equals(parentControl.getOnlineTimeCtrl())){
			parentControl.setOnlineTimeCtrl(open);
			pcs.saveOrUpdateParentControl(parentControl);
		}
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "修改在线视频时间控制成功！", null, this);
		
	}

	private ExecuteResult readLocalTime() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		ParentControl parentControl = pcs.getParentControlByCustomerId(customer.getId());
		if (parentControl == null) {
			parentControl = new ParentControl();
			parentControl.setCustomer(customer);
			pcs.saveParentControl(parentControl);
		}
		
		JSONObject obj = new JSONObject();
		obj.put("open", parentControl.getLocalTimeCtrl() == null ? false : parentControl.getLocalTimeCtrl());
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取本地视频时间控制成功！", obj, this);
		
	}
	
	private ExecuteResult readOnlineTime() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		ParentControl parentControl = pcs.getParentControlByCustomerId(customer.getId());
		if (parentControl == null) {
			parentControl = new ParentControl();
			parentControl.setCustomer(customer);
			pcs.saveParentControl(parentControl);
		}
		
		JSONObject obj = new JSONObject();
		obj.put("open", parentControl.getOnlineTimeCtrl() == null ? false : parentControl.getOnlineTimeCtrl());
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取在线视频时间控制成功！", obj, this);
		
	}
	
	private ExecuteResult listParentTimeCtrl() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		List<ParentTimeControl> ptcs = pcs.getParentTimeControlByCustomerId(customer.getId());
		JSONArray array = new JSONArray();
		for(ParentTimeControl ptc : ptcs){
			JSONObject obj = new JSONObject();
			obj.put("startTime", ParentControlService.doubleTime2String(ptc.getStartTime()));
			obj.put("endTime", ParentControlService.doubleTime2String(ptc.getEndTime()));
			array.put(obj);
		}
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取视频时间控制信息成功！", array, this);
		
	}

	private ExecuteResult addParentTimeCtrl() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		double startTime = ParentControlService.stringTime2Double(jsonObj.getString("startTime"));
		double endTime = ParentControlService.stringTime2Double(jsonObj.getString("endTime"));
		pcs.saveParentTimeControl(customer, startTime, endTime);
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "添加视频时间控制信息成功！", null, this);
		
	}

	private ExecuteResult deleteParentTimeCtrl() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		Customer customer = sc.getCustomer();
		ParentControlService pcs = SystemInitialization.getApplicationContext().getBean(ParentControlService.class);
		
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		double startTime = ParentControlService.stringTime2Double(jsonObj.getString("startTime"));
		double endTime = ParentControlService.stringTime2Double(jsonObj.getString("endTime"));
		pcs.deleteParentTimeControl(customer.getId(), startTime, endTime);
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "删除视频时间控制信息成功！", null, this);
		
	}
}
