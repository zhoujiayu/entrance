package com.ytsp.entrance.command.v4_0;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbSecKill;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbSkillOrderService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.OrderIdGenerationUtil;

public class EbSkillOrderCommand extends EbOrderCommand{
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return ( code == CommandList.CMD_EB_ORDER_SKILL);
	}
	
	@Override
	public ExecuteResult execute() {
		//验证权限.
		int code = getContext().getHead().getCommandCode();
		int uid = getContext().getHead().getUid();//UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		//判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (uid == 0 || customer.getId().intValue() != uid) {
			return getNoPermissionExecuteResult();
		}
		try{
			return skillOrder(customer, jsonObj, uid);
		}catch(Exception e){
			e.printStackTrace();
			logger.info("秒杀订单操作:" + code + " 失败 " + e);
		}
		return null;
	}
	
	/**
	 * 秒杀下单
	 * 秒杀场次的概念:
	 * 一个专辑可能会有很多的秒杀,每次秒杀的商品可能一样或者不一样,这个场次的概念是指一个专辑下一共会有多少场次秒杀,场次不断递增.
	 * 每个场次关联一个商品,也就是说,一个专辑的一场秒杀,只能卖一种商品,如果一个专辑需要同时开卖多个秒杀商品,那么只要增加同一个开始时间的场次即可.
	 * @param customer
	 * @param jsonObj
	 * @param uid
	 * @return
	 * @throws JSONException 
	 * @throws SqlException 
	 * @throws OutOfStorageException 
	 */
	private ExecuteResult skillOrder(Customer customer, JSONObject jsonObj, int uid) throws JSONException, SqlException, OutOfStorageException {
		int activityId = jsonObj.getInt("activityId");//获取专辑ID
		int secKillId = jsonObj.getInt("seckillId");//获取秒杀场次ID
		EbSkillOrderService esos =  SystemInitialization.getApplicationContext().getBean(EbSkillOrderService.class);
		EbSecKill secKill = esos.getSecKill(secKillId, activityId);
		if (secKill == null ) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"秒杀活动不存在",null,this);
		}
		
		if (secKill.getStartTime().compareTo(new Date()) > 0) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"秒杀活动未开始",null,this);
		}
		
		if (secKill.getEndTime().compareTo(new Date()) < 0) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"秒杀活动已结束",null,this);
		}
		
		if (secKill.getProductNum() <= 0) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"商品售罄",null,this);
		}
		//先组织好订单数据
		EbOrder ebOrder = new EbOrder();
		ebOrder.setOrderid(OrderIdGenerationUtil.getInstance().genOrderId());
		bindBasicOrderInfo(jsonObj,ebOrder,uid);
		ebOrder.setOrderPlat(MobileTypeEnum.valueOf(getContext().getHead().getPlatform()));
		bindUserAddress(jsonObj,ebOrder);
		bindSecDetails(jsonObj,ebOrder,secKill);
		//把订单的类型修改为秒杀
		ebOrder.setOrderType(EbOrderTypeEnum.SECKILL);
		EbSkillOrderService ebSkillOrderService = SystemInitialization.getApplicationContext().getBean(EbSkillOrderService.class);
		long orderid = ebSkillOrderService.createSecKillOrder(secKill, ebOrder);
		if (orderid == 0L) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,"商品售罄",null,this); 
		}
		JSONObject result = new JSONObject();
		result.put("orderId", orderid);
		return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,"秒杀成功",result,this);
	}
	
	/**
	 * 绑定秒杀订单的详情.金额不是由商品决定,而是由秒杀活动来决定的.数量限定是1.
	 * @param jsonObject
	 * @param ebOrder
	 * @param ebSecKill
	 * @throws JSONException
	 * @throws SqlException
	 * @throws OutOfStorageException
	 */
	private void bindSecDetails(JSONObject jsonObject,EbOrder ebOrder,EbSecKill ebSecKill) throws JSONException, SqlException, OutOfStorageException{
		JSONArray array = jsonObject.getJSONArray("details");
		
		if (array == null || array.length() == 0) {
			throw new JSONException("没有订单详情");
		}
		int length = array.length();
		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
		JSONObject j = null;
//		EbSkuService ebss =  SystemInitialization.getApplicationContext().getBean(EbSkuService.class);
//		List<EbSku> outOfStorageskus = new ArrayList<EbSku>();//没有库存的SKU
		EbOrderDetail detail = null;
		int productCode=0;
		for (int i = 0; i < length; i++) {
			j = array.getJSONObject(i);
			int activityId = j.getInt("activityId");
			int amount = 1;
			String color = "";
			if(!j.isNull("color"))
				color = j.getString("color");
			double price = ebSecKill.getPrice();
			productCode = j.getInt("productCode");
			String size = "";
			if(!j.isNull("size"))
				color = j.getString("size");
			int skuCode = 0;
			if(!j.isNull("skuCode"))
				skuCode = j.getInt("skuCode");
			String productName = j.getString("productName");
			if (amount <= 0 || amount >= 10) {
				throw new JSONException("提交订单中商品的数量不正确");
			}
			
			detail = new EbOrderDetail();
			detail.setActivityId(activityId);
			detail.setAmount(amount);
			detail.setColor(color);
			detail.setProductName(productName);
			detail.setOrderId(ebOrder.getOrderid());
			detail.setPrice(price);
			detail.setVendorProductCode(ebSecKill.getProduct().getVendorProductCode());
			detail.setProductCode(productCode);
			detail.setSize(size);
			detail.setSkuCode(skuCode);
			detail.setTotalPrice(amount * price);
			details.add(detail);
			//VO属性,以便扣减库存的时候使用
//			detail.setEbsku(ebSku);
		}
		ebOrder.setImgSrc(SystemManager.getInstance().getSystemConfig().
				getImgServerUrl() +ebSecKill.getProduct().getImgUrl());
		ebOrder.setOrderDetails(details);
	}
	
}
