package com.ytsp.entrance.command.v5_0;

import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.ScanningServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;

public class ScanningCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_SCANNING_REDIRECT;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if(code == CommandList.CMD_SCANNING_REDIRECT){
				return getScanningRedirectURL();
			}
		} catch (Exception e) {
			logger.info("ScanningCommand:" + code + " 失败 " + ",headInfo:"
					+ getContext().getHead().toString() + "bodyParam:"
					+ getContext().getBody().getBodyObject().toString()
					+ e.getMessage());
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	
	/**
	* <p>功能描述:混合搜索全部，按分类显示</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getScanningRedirectURL() throws Exception{
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		String scaningCode = "";
		if (reqBody.isNull("code")) {
			result.put("redirectUrl", "");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取跳转URL成功",
					result, this);
		}
		scaningCode = reqBody.optString("code");
		ScanningServiceV5_0 scanServ = SystemInitialization.getApplicationContext().getBean(ScanningServiceV5_0.class);
		String redirectUrl = scanServ.getScanningRedirectURL(scaningCode);
		result.put("redirectUrl", redirectUrl);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取跳转URL成功",
				result, this);
	}
}
