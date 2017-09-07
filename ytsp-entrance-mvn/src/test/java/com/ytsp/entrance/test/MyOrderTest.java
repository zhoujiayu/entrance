
//"classpath:META-INF/spring-db.xml",
//@ContextConfiguration(locations = {"classpath:META-INF/spring-db.xml","classpath:spring-datasource.xml","classpath:spring-service.xml","classpath:spring-session.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)

//package com.ytsp.entrance.test;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Test;
//
//import com.ytsp.db.domain.EbOrder;
//import com.ytsp.db.domain.EbOrderDetail;
//import com.ytsp.db.domain.EbSku;
//import com.ytsp.db.domain.EbUserAddress;
//import com.ytsp.db.enums.EbProductValidStatusEnum;
//import com.ytsp.db.exception.SqlException;
//import com.ytsp.entrance.service.EbOrderService;
//import com.ytsp.entrance.service.EbSkuService;
//import com.ytsp.entrance.service.v4_0.EbUserAddressService;
//import com.ytsp.entrance.system.SystemInitialization;
//import com.ytsp.entrance.util.OrderIdGenerationUtil;
//
//public class MyOrderTest extends TestBase{
//	
//	@Test
//	public void test() throws JSONException{
//		try{
//		
//			JSONObject jsonObj = new JSONObject();
//			jsonObj.put("activityId", 1);
//			jsonObj.put("amount", 1);
//			jsonObj.put("color", "蓝色");
//			jsonObj.put("productCode", 1000987);
//			jsonObj.put("size", "170");
//			jsonObj.put("skuCode", 10009878);
//			jsonObj.put("addressId", 2);
//			jsonObj.put("userid", 1231);
//			EbOrder ebOrder = new EbOrder();
//			ebOrder.setOrderid(OrderIdGenerationUtil.getInstance().genOrderId());
//			bindBasicOrderInfo(jsonObj,ebOrder);
//			bindUserAddress(jsonObj,ebOrder);
//			try {
//				bindDetails(jsonObj,ebOrder);
//			} catch (OutOfStorageException e) {
//				JSONObject result = e.getOutOfStorageSkus();
//				return;
//			}
//			JSONObject result = new JSONObject();
//			result.put("orderId", ebOrder.getOrderid());
//			EbOrderService eos =  SystemInitialization.getApplicationContext().getBean(EbOrderService.class);
//			eos.saveOrder(ebOrder);
//			
//		}catch(Exception e){
//			
//		}
//		
//	}
//	
//	
//	protected void bindUserAddress(JSONObject jsonObject,EbOrder ebOrder) throws JSONException, SqlException{
//		int addressId = jsonObject.getInt("addressId");
//		//通过addressId来获取用户地址
//		EbUserAddressService euas =  SystemInitialization.getApplicationContext().getBean(EbUserAddressService.class);
//		EbUserAddress ebUserAddress = euas.retrieveEbUserAddressById(addressId);
//		ebOrder.setAddressId(addressId);
//		ebOrder.setAddress(ebUserAddress.getAddress());
//		ebOrder.setAreaId(ebUserAddress.getAreaId());
//		ebOrder.setAreaName(ebUserAddress.getAreaName());
//		ebOrder.setCellphone(ebUserAddress.getCellphone());
//		ebOrder.setCityId(ebUserAddress.getCityId());
//		ebOrder.setCityName(ebUserAddress.getCityName());
//		ebOrder.setEmail(ebUserAddress.getEmail());
//		ebOrder.setUserName(ebUserAddress.getUserName());
//	}
//	
//	protected void bindBasicOrderInfo(JSONObject jsonObject,EbOrder ebOrder) throws JSONException{
//		int userId = jsonObject.getInt("userid");
//		String deviceCode = jsonObject.getString("deviceCode");
//		int payType = jsonObject.getInt("payType");
//		int isNeedInvoice = jsonObject.getInt("invoice");
//		ebOrder.setPayType(payType);
//		ebOrder.setUserId(userId);
//		ebOrder.setDeviceCode(deviceCode);
//		ebOrder.setOrderTime(new Date());
//		ebOrder.setIsNeedInvoice(isNeedInvoice);
//	}
//	
//	private void bindDetails(JSONObject jsonObject,EbOrder ebOrder) throws JSONException, SqlException, OutOfStorageException{
//		JSONArray array = jsonObject.getJSONArray("details");
//		
//		if (array == null || array.length() == 0) {
//			throw new JSONException("没有订单详情");
//		}
//		int length = array.length();
//		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
//		JSONObject j = null;
//		EbSkuService ebss =  SystemInitialization.getApplicationContext().getBean(EbSkuService.class);
//		List<EbSku> outOfStorageskus = new ArrayList<EbSku>();//没有库存的SKU
//		EbOrderDetail detail = null;
//		for (int i = 0; i < length; i++) {
//			j = array.getJSONObject(i);
//			int activityId = j.getInt("activityId");
//			int amount = j.getInt("amount");
//			String color = j.getString("color");
//			
//			int productCode = j.getInt("productCode");
//			String size = j.getString("size");
//			int skuCode = j.getInt("skuCode");
//			//商品有效性判断
//			EbSku ebSku = ebss.retrieveEbSkuBySkuCode(skuCode);
//			if (ebSku == null || ebSku.getStatus() != EbProductValidStatusEnum.VALID) {
//				throw new JSONException("提交订单中的SKU信息不正确");
//			}
//			
//			if (amount <= 0 || amount >= 10) {
//				throw new JSONException("提交订单中商品的数量不正确");
//			}
//			
//			if (ebSku.getStorage().getNum() <= 0){
//				outOfStorageskus.add(ebSku);
//			}
//			//TODO:需要根据不同的客户类型来获取不同的价格.
//			double price = ebSku.getPrice();
//			detail = new EbOrderDetail();
//			detail.setActivityId(activityId);
//			detail.setAmount(amount);
//			detail.setColor(color);
//			detail.setOrderId(ebOrder.getOrderid());
//			detail.setPrice(price);
//			detail.setProductCode(productCode);
//			detail.setSize(size);
//			detail.setSkuCode(skuCode);
//			detail.setTotalPrice(amount * price);
//			details.add(detail);
//			//VO属性,以便扣减库存的时候使用
//			detail.setEbsku(ebSku);
//		}
//		ebOrder.setOrderDetails(details);
//		//如果有发现缺库存的商品则抛出异常
//		if (outOfStorageskus.size() > 0) {
//			OutOfStorageException e = new OutOfStorageException("超出库存");
//			e.addOutOfStorageSkus(outOfStorageskus);
//			throw e;
//		}
//	}
//	
//	public class OutOfStorageException extends Exception{
//		
//		private static final long serialVersionUID = 1L;
//		private String message;
//		private List<EbSku> ebSkus = new ArrayList<EbSku>();
//		
//		public void addOutOfStorageSku(EbSku ebSku){
//			this.ebSkus.add(ebSku);
//		}
//		
//		public void addOutOfStorageSkus(List<EbSku> ebSkus){
//			this.ebSkus.addAll(ebSkus);
//		}
//		
//		public JSONObject getOutOfStorageSkus() throws JSONException{
//			JSONObject ret = new JSONObject();
//			JSONArray array = new JSONArray();
//			JSONObject o = null;
//			for (EbSku ebSku : ebSkus) {
//				o = new JSONObject();
//				o.put("skuCode", ebSku.getSkuCode());
//				array.put(o);
//			}
//			ret.put("skuCodes", array);
//			return ret;
//		}
//		
//		public String getMessage() {
//			return message;
//		}
//
//		public void setMessage(String message) {
//			this.message = message;
//		}
//
//		public OutOfStorageException(){
//			super();
//		}
//		
//		public OutOfStorageException(String message) {
//	        super(message);
//	        this.message = message;
//	    }
//		
//		
//		
//	}
//	
//}
