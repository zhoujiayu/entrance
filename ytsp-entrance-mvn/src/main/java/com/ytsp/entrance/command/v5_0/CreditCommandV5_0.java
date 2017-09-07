package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.CreditsRecord;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.vo.CreditsRecordVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.errorcode.ErrorCode;
import com.ytsp.entrance.handleResponse.RestResponse;
import com.ytsp.entrance.service.v5_0.CreditServiceV5_0;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;

public class CreditCommandV5_0 extends AbstractCommand{

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_CREDIT_STRATEGY_QUERY
				|| code == CommandList.CMD_CREDIT_RECORD_LIST;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_CREDIT_STRATEGY_QUERY) {
				return queryCreditStategyList();
			}
			
			// 验证权限.
			int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
			SessionCustomer sc = getSessionCustomer();
			if (sc == null || sc.getCustomer() == null) {
				return getNoPermissionExecuteResult();
			}
			// 判断操作的用户与当前的session中用户是否一致.
			Customer customer = sc.getCustomer();
			if (userId == 0 || customer.getId().intValue() != userId) {
				return getNoPermissionExecuteResult();
			}
			if(code == CommandList.CMD_CREDIT_RECORD_LIST){
				return queryCreditRecordList(userId);
			}
		} catch (Exception e) {
			logger.info("CreditCommandV5_0:" + code + " 失败 " + ",headInfo:"
					+ getContext().getHead().toString() + "bodyParam:"
					+ getContext().getBody().getBodyObject().toString()
					+ e.getMessage());
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	/**
	* <p>功能描述:获取用户积分来源记录列表</p>
	* <p>参数：@param userId 用户Id
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult queryCreditRecordList(int userId) throws Exception{
		JSONObject reqParam = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = -1;
		if(reqParam.has("page")){
			page = reqParam.optInt("page");
		}
		if(reqParam.has("pageSize")){
			pageSize = reqParam.optInt("pageSize");
		}
		CreditServiceV5_0 cs = SystemInitialization.getApplicationContext()
				.getBean(CreditServiceV5_0.class);
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		List<CreditsRecord> creditRec= cs.findUserCreditRecord(userId,page,pageSize);
		RestResponse response = new RestResponse(ErrorCode.RESP_CODE_OK,ErrorCode.RESP_INFO_OK);
		response.setVo(buildCreditsRecordVO(creditRec));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(response));
		Customer cust = custServ.getCustomerById(userId);
		result.put("totalCredits", (cust == null || cust.getCredits() == null) ? 0 :cust.getCredits());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取积分来源列表成功",
				result, this);
	}
	
	/**
	* <p>功能描述:构建积分来源VO</p>
	* <p>参数：@param creditRec
	* <p>参数：@return</p>
	* <p>返回类型：List<CreditsRecordVO></p>
	 */
	private List<CreditsRecordVO> buildCreditsRecordVO(List<CreditsRecord> creditRec){
		if(creditRec == null || creditRec.size() == 0){
			return null;
		}
		List<CreditsRecordVO> vos = new ArrayList<CreditsRecordVO>();
		for (CreditsRecord creditsRecord : creditRec) {
			CreditsRecordVO vo = new CreditsRecordVO();
			vo.setId(creditsRecord.getId());
			vo.setCreateDate(DateFormatter.date2String(creditsRecord.getCreateDate(), "yyyy-MM-dd kk:mm:ss"));
			vo.setCreditsNumber(creditsRecord.getCreditNumber());
			vo.setCreditSourceDesc(creditsRecord.getCreditSourceDesc());
			
			vos.add(vo);
		}
		return vos;
		
	}
	
	/**
	* <p>功能描述:获取积分策略列表</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult queryCreditStategyList() throws Exception{
		CreditServiceV5_0 cs = SystemInitialization.getApplicationContext()
				.getBean(CreditServiceV5_0.class);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取积分策略列表成功",
				cs.getCreditPolicy(), this);
	}

}
