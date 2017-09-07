package com.ytsp.entrance.command.v4_0;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbUserAddress;
import com.ytsp.db.enums.EbUserAddressStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.EbUserAddressService;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

public class EbUserAddressCommand extends AbstractCommand {
	public EbUserAddressCommand() {
	}

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_EB_USERADDRESS_ADD
				|| code == CommandList.CMD_EB_USERADDRESS_DELETE
				|| code == CommandList.CMD_EB_USERADDRESS_UPDATE || code == CommandList.CMD_EB_USERADDRESS_LIST);
	}

	@Override
	public ExecuteResult execute() {

		try {
			// 验证权限.
			int uid = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
			SessionCustomer sc = getSessionCustomer();
			if (sc == null || sc.getCustomer() == null) {
				return getNoPermissionExecuteResult();
			}
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			// 判断操作的用户与当前的session中用户是否一致.
			Customer customer = sc.getCustomer();
			if (uid == 0 || customer.getId().intValue() != uid) {
				return getNoPermissionExecuteResult();
			}

			int code = getContext().getHead().getCommandCode();
			if (code == CommandList.CMD_EB_USERADDRESS_ADD) {
				return add(jsonObj, uid);
			} else if (code == CommandList.CMD_EB_USERADDRESS_DELETE) {
				return delete(jsonObj, uid);
			} else if (code == CommandList.CMD_EB_USERADDRESS_UPDATE) {
				return update(jsonObj, uid);
			} else if (code == CommandList.CMD_EB_USERADDRESS_LIST) {
				return list(jsonObj, uid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ExecuteResult list(JSONObject jsonObj, int uid)
			throws SqlException, JSONException {
		// session中保存的数据可能会变化，所以重新取数据库
		CustomerServiceV5_0 customerServiceV5_0 = SystemInitialization
				.getApplicationContext().getBean(CustomerServiceV5_0.class);
		Customer customer = customerServiceV5_0.getCustomerById(uid);
		int defaultAddress = customer.getAddressId() == null ? 0 : customer
				.getAddressId().intValue();
		EbUserAddressService ebUserAddressService = SystemInitialization
				.getApplicationContext().getBean(EbUserAddressService.class);
		List<EbUserAddress> ls = ebUserAddressService.getAddrListByUser(uid);
		JSONArray array = new JSONArray();
		JSONObject defaultAddr = null;
		for (EbUserAddress addr : ls) {
			JSONObject jo = new JSONObject();
			jo.put("address", addr.getAddress());
			jo.put("addressId", addr.getAddressId());
			jo.put("areaName", addr.getAreaName());
			jo.put("cellphone", addr.getCellphone());
			jo.put("cityName", addr.getCityName());
			jo.put("email", addr.getEmail() == null ? "" : addr.getEmail());
			jo.put("postalCode",
					addr.getPostalCode() == null ? "" : addr.getPostalCode());
			jo.put("provinceName", addr.getProvinceName());
			jo.put("userid", addr.getUserid());
			jo.put("userName", addr.getUserName());
			jo.put("isDefault",
					addr.getAddressId().intValue() == defaultAddress);
			if(addr.getAddressId().intValue() == defaultAddress){
				defaultAddr = jo;
			}
			array.put(jo);
		}
		JSONArray arr = new JSONArray();
		if(defaultAddr != null){
			arr.put(defaultAddr);
			//将默认地址放到第一个位置
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = (JSONObject) array.get(i);
				if(json.optBoolean("isDefault")){
					continue;
				}
				arr.put(json);
			}
			array = arr;
		}
		Util.addStatistics(getContext(), array.toString());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取地址列表成功",
				array, this);
	}

	public ExecuteResult add(JSONObject object, int userId)
			throws JSONException, SqlException {
		// int cityId = object.getInt("cityId");
		// int provinceId = object.getInt("provinceId");
		// int areaId = object.getInt("areaId");
		String cityName = "";
		String areaName = "";
		if (!object.isNull("cityName"))
			cityName = object.getString("cityName");
		String provinceName = object.getString("provinceName");
		if (!object.isNull("areaName"))
			areaName = object.getString("areaName");
		String address = object.getString("address");
		String userName = object.getString("userName");
		String cellphone = object.getString("cellphone");
		String postalCode = null;
		if (!object.isNull("postalCode"))
			postalCode = object.getString("postalCode");
		// String email = object.getString("email");
		EbUserAddress addressObj = new EbUserAddress();
		addressObj.setAddress(address);
		// addressObj.setAreaId(areaId);
		addressObj.setAreaName(areaName);
		addressObj.setCellphone(cellphone);
		// addressObj.setCityId(cityId);
		addressObj.setCityName(cityName);
		// addressObj.setEmail(email);
		addressObj.setPostalCode(postalCode);
		// addressObj.setProvinceId(provinceId);
		addressObj.setProvinceName(provinceName);
		addressObj.setUserid(userId);
		addressObj.setUserName(userName);
		EbUserAddressService ebUserAddressService = SystemInitialization
				.getApplicationContext().getBean(EbUserAddressService.class);
		addressObj.setStatus(EbUserAddressStatusEnum.VALID);

		ebUserAddressService.saveEbUserAddress(addressObj);
		boolean isDefault = object.optBoolean("isDefault", false);
		if (isDefault) {
			CustomerServiceV5_0 customerServiceV5_0 = SystemInitialization
					.getApplicationContext().getBean(CustomerServiceV5_0.class);
			Customer customer = customerServiceV5_0.getCustomerById(userId);
			customer.setAddressId(addressObj.getAddressId());
			customerServiceV5_0.updateCustomer(customer);
		}
		// int status, String statusMsg, Object result, Command command
		JSONObject j = new JSONObject();
		j.put("addressId", addressObj.getAddressId());
		Util.addStatistics(getContext(), addressObj);
		ExecuteResult result = new ExecuteResult(
				CommandList.RESPONSE_STATUS_OK, "地址保存成功", j, this);
		return result;
	}

	public ExecuteResult update(JSONObject object, int userId)
			throws JSONException, SqlException {
		int addressId = object.optInt("addressId");
		String cityName = object.optString("cityName");
		String provinceName = object.optString("provinceName");
		String areaName = object.optString("areaName");
		String address = object.optString("address");
		String userName = object.optString("userName");
		String cellphone = object.optString("cellphone");
		String postalCode = object.optString("postalCode");
		EbUserAddressService ebUserAddressService = SystemInitialization
				.getApplicationContext().getBean(EbUserAddressService.class);
		EbUserAddress addressObj = ebUserAddressService
				.retrieveEbUserAddressById(addressId);
		if (addressObj == null || addressObj.getUserid().intValue() != userId) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"地址更新错误,不存在地址信息", null, this);
		}
		addressObj.setAddress(address);
		addressObj.setAreaName(areaName);
		addressObj.setCellphone(cellphone);
		addressObj.setCityName(cityName);
		addressObj.setPostalCode(postalCode);
		addressObj.setProvinceName(provinceName);
		addressObj.setUserid(userId);
		addressObj.setUserName(userName);
		addressObj.setStatus(EbUserAddressStatusEnum.VALID);
		ebUserAddressService.updateEbUserAddress(addressObj);
		boolean isDefault = object.optBoolean("isDefault", false);
		CustomerServiceV5_0 customerServiceV5_0 = SystemInitialization
				.getApplicationContext().getBean(CustomerServiceV5_0.class);
		Customer customer = customerServiceV5_0.getCustomerById(userId);
		if (isDefault) {
			customer.setAddressId(addressObj.getAddressId());
			customerServiceV5_0.updateCustomer(customer);
		} else if (customer.getAddressId()!=null&&addressId == customer.getAddressId().intValue()) {
			customer.setAddressId(0);
			customerServiceV5_0.updateCustomer(customer);
		}
		// int status, String statusMsg, Object result, Command command
		JSONObject j = new JSONObject();
		j.put("addressId", addressObj.getAddressId());
		Util.addStatistics(getContext(), addressObj);
		ExecuteResult result = new ExecuteResult(
				CommandList.RESPONSE_STATUS_OK, "地址更新成功", j, this);
		return result;
	}

	public ExecuteResult delete(JSONObject object, int userId)
			throws JSONException, SqlException {
		int addressId = object.getInt("addressId");
		EbUserAddressService ebUserAddressService = SystemInitialization
				.getApplicationContext().getBean(EbUserAddressService.class);
		EbUserAddress addressObj = ebUserAddressService
				.retrieveEbUserAddressById(addressId);
		if (addressObj == null || addressObj.getUserid().intValue() != userId) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"地址更新错误,不存在地址信息", null, this);
		}
		addressObj.setStatus(EbUserAddressStatusEnum.DISCARD);
		ebUserAddressService.updateEbUserAddress(addressObj);
		JSONObject j = new JSONObject();
		j.put("addressId", addressObj.getAddressId());
		Util.addStatistics(getContext(), addressObj);
		ExecuteResult result = new ExecuteResult(
				CommandList.RESPONSE_STATUS_OK, "地址更新成功", j, this);
		return result;
	}
}
