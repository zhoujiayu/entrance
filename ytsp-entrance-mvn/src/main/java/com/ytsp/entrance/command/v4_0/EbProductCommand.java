package com.ytsp.entrance.command.v4_0;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductImage;
import com.ytsp.db.domain.EbSecKill;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.enums.EbProductImageTypeEnum;
import com.ytsp.db.enums.EbProductTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.VersionCommand;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.DateTimeFormatter;

public class EbProductCommand extends AbstractCommand{

	private  DecimalFormat df = new DecimalFormat("##0.##");
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_EB_PRODUCT_GETBYPRODUCTCODE || 
					code == CommandList.CMD_EB_PRODUCT_GETBYACTIVITYID|| 
					code == CommandList.CMD_EB_EBSECKILL_GETBYACTIVITYID|| 
							code == CommandList.CMD_EB_PRODUCT_SKU_GETBYPRODUCODE|| 
									code == CommandList.CMD_VIP_PRODUCT_LIST;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (code == CommandList.CMD_EB_PRODUCT_GETBYPRODUCTCODE) {
				int productCode = jsonObj.getInt("productCode");
				 return retrieveProductByProductCode(productCode);
			} else if (code == CommandList.CMD_EB_PRODUCT_GETBYACTIVITYID) {
				int ebActivityId = jsonObj.getInt("ebActivityId");
				return getProductsByActivityId(ebActivityId);
			} else if (code == CommandList.CMD_EB_EBSECKILL_GETBYACTIVITYID) {
				return getOneEbSeckill();
			} else if (code == CommandList.CMD_EB_PRODUCT_SKU_GETBYPRODUCODE) {
				int productCode = jsonObj.getInt("productCode");
				return getProductAndSkuByProductCode(productCode);
			} else if (code == CommandList.CMD_VIP_PRODUCT_LIST) {
				return vipProductList();
			} 
		} catch (Exception e) {
			logger.error("EbProductCommand error:" + code + " : " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	private ExecuteResult vipProductList() throws SqlException, JSONException {
		EbProductService eps =  SystemInitialization.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> ls = eps.getVipProductList();
		JSONArray array = new JSONArray();
		for (EbProduct ebProduct : ls) {
			if(ebProduct.getProductCode().intValue()==9900005){
				continue;
			}
			JSONObject result = new JSONObject();
			result.put("productCode", ebProduct.getProductCode());
			result.put("productName", ebProduct.getProductName());
			result.put("price", df.format(ebProduct.getPrice()));
			result.put("productType", EbProductTypeEnum.VIPMEMBER.getValue());
			array.put(result);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取单个商品成功", array, this);
	}

	/**
	 * 
	 * @param productCode
	 * @return
	 * @throws Exception
	 * @throws JSONException
	 * @deprecated
	 */
	private ExecuteResult retrieveProductByProductCode(int productCode) throws Exception, JSONException{
		EbProductService eps =  SystemInitialization.getApplicationContext().getBean(EbProductService.class);
		EbProduct ebProduct = eps.retrieveProductByProductCode(productCode);
		JSONObject result = getSingleProductJson(ebProduct,null);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取单个商品成功", result, this);
		
	}
	
	private ExecuteResult getProductAndSkuByProductCode(int productCode) throws Exception, JSONException{
		EbProductService eps =  SystemInitialization.getApplicationContext().getBean(EbProductService.class);
		EbProduct ebProduct = eps.retrieveProductByProductCode(productCode);
		if(ebProduct == null){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "浏览的商品已经失效", null, this);
		}
		JSONObject result = getProductAndSkusJson(ebProduct);
		result.put("activityId", ebProduct.getEbActivity().getActivityId());
		result.put("activityName", ebProduct.getEbActivity().getActivityName());
		result.put("currentTime", System.currentTimeMillis());
		result.put("shareUrl", "http://m.ikan.cn/mobileAppAddress.action?from=14");//TODO	写分享的url
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取单个商品和SKU成功", result, this);
	}
	private ExecuteResult getProductsByActivityId(int activityId) throws Exception, JSONException{
		EbProductService eps =  SystemInitialization.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> ebProducts = eps.retrieveValidProductsByActivityId(activityId);
		String plat = getContext().getHead().getPlatform();
		int version = VersionCommand.convert2Num(getContext().getHead().getVersion());
		if(plat.equals("gphone")||plat.equals("gpad")){
			if(version<402000&&ebProducts.size()>14){//4.2版本以前的安卓客户端只能最多显示14个
				ebProducts = ebProducts.subList(0, 14);
			}
		}
//		List<EbSales> ret = null;
		//19悠悠球23铠甲活动25口罩
//		if(activityId==19||activityId==25){
//			int uid = getContext().getHead().getUid();
//			SessionCustomer sc = getSessionCustomer();
//			if (sc != null && sc.getCustomer() != null) {
//				Customer customer = sc.getCustomer();
//				if (uid != 0 &&customer.getId().intValue() == uid) {
//					EbSalesService es = SystemInitialization.getApplicationContext().getBean(EbSalesService.class);
//					ret = es.find(uid);
//				}
//			}
//		}
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		for (EbProduct ebProduct : ebProducts) {
			EbSecKill ebSecKill = eps.getEbSecKillByProduct(ebProduct);
			if(ebSecKill!=null&&ebSecKill.getStatus()==ValidStatusEnum.VALID)
				continue;
			//检查是否特殊专题活动
//			boolean isContain = true;
//			if(ebProduct.getProductCode()==1005003||
//					ebProduct.getProductCode()==1005002||
//							ebProduct.getProductCode()==1004022||
//									ebProduct.getProductCode()==1005001){
//				//只有悠悠球和口罩要隐藏处理
//				if(ret==null)
//					continue;
//				for (EbSales ebSales : ret) {
//					int productCode = ebSales.getProduct().getProductCode();
//					if(productCode==ebProduct.getProductCode()&&
//							ebSales.getCount()>0){
//						isContain = false;
//						break;
//					}
//				}
//				if(isContain)
//					continue;
//			}
			JSONObject jo = getSingleProductJson(ebProduct,ebSecKill);
			JSONArray imgsjos = new JSONArray();
			JSONObject _jo = new JSONObject();
			_jo.put("imageSrc", SystemManager.getInstance().getSystemConfig().
					getImgServerUrl() +ebProduct.getImageCut());
			_jo.put("sortNum", 0);
			_jo.put("type", EbProductImageTypeEnum.DEMO);
			imgsjos.put(_jo);
			jo.put("imgs", imgsjos);
			array.put(jo);
		}
		result.put("products", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑的商品成功", result, this);
	}
	
	/**
	 * @param ebProduct
	 * @param ebSecKill
	 * @return
	 * @throws Exception
	 */
	private JSONObject getSingleProductJson(EbProduct ebProduct,EbSecKill ebSecKill)
			throws Exception {
		JSONObject result = new JSONObject();
		if (ebProduct != null) {
			result.put("productCode", ebProduct.getProductCode()); 
			result.put("productName", ebProduct.getProductName());
			result.put("description", ebProduct.getProductDescription());
			result.put("price", df.format(ebProduct.getPrice()));
			//TODO　运费计算
			result.put("shipping", df.format(ebProduct.getShipping()));
			result.put("svprice", df.format(ebProduct.getSvprice()));
			result.put("vprice", df.format(ebProduct.getVprice()));
			result.put("productStorage", 0);
			if(ebSecKill!=null){
				//TODO 需要兼容早期客户端的设计，所以写个神仙数在这，以后productType并没有秒杀类型
				result.put("productType", 1);//1代表秒杀
				JSONObject obj = new JSONObject();
				if(getContext().getHead().getPlatform().equals("ipad"))
					obj.put("imageSrc", SystemManager.getInstance().getSystemConfig().
							getImgServerUrl() +ebSecKill.getImageSrcPad());
				else
					obj.put("imageSrc", SystemManager.getInstance().getSystemConfig().
							getImgServerUrl() +ebSecKill.getImageSrcPhone());
				obj.put("secKillPrice", df.format(ebSecKill.getPrice()));
				obj.put("price", df.format(ebSecKill.getProduct().getPrice()));
				obj.put("seckillId", ebSecKill.getId());
				obj.put("productCode", ebSecKill.getProduct().getProductCode());
				obj.put("startTime", DateTimeFormatter.dateTime2String(ebSecKill.getStartTime()));
				obj.put("endTime", DateTimeFormatter.dateTime2String(ebSecKill.getEndTime()));
				obj.put("productNum", ebSecKill.getProductNum());
				obj.put("productName", ebSecKill.getProduct().getProductName());
				int status =0;//倒计时
				if(ebSecKill.getStartTime().before(new Date()))
					status = 1;//在售中
				if(ebSecKill.getProductNum()<=0)
					status = 2;//售罄
				if(ebSecKill.getEndTime().before(new Date())){
					status = 2;//售罄
					obj.put("productNum", 0);
				}
				if(status==0){
					//TODO 临时使用，只要status==0，则显示剩余100件
					obj.put("productNum", 100);
				}
				obj.put("status", status);
				result.put("ebSecKill", obj);
			}else{
				result.put("productType", EbProductTypeEnum.NORMAL.getValue());
				int productStorage = 0;
				for (EbSku ebSku : ebProduct.getSkus()) {
					productStorage += ebSku.getStorage().getAvailable();
				}
				result.put("productStorage", productStorage);
			}
			result.put("status", ebProduct.getStatus().getText());
			result.put("creditPercentage", ebProduct.getCreditPercentage());
			
		}
		return result;
	}
	
	/**
	 * @param ebProduct
	 * @return
	 * @throws Exception
	 */
	private JSONObject getProductAndSkusJson(EbProduct ebProduct)
			throws Exception {
		EbProductService eps =  SystemInitialization.getApplicationContext().getBean(EbProductService.class);
		EbSecKill ebSecKill = eps.getEbSecKillByProduct(ebProduct);
		JSONObject result = getSingleProductJson(ebProduct,ebSecKill);
		List<EbProductImage> imgs = eps.getEbProductImages(MobileTypeEnum.iphone,
				ebProduct.getProductCode());//
		if(MobileTypeEnum.valueOf(getContext().getHead().getPlatform())==
				MobileTypeEnum.ipad){
			for (EbProductImage ebProductImage : imgs) {
				ebProductImage.setPlatform(MobileTypeEnum.ipad);
			}
		}
		JSONArray imgsjos = new JSONArray();
		for (int i = 0; i < imgs.size(); i++) {
			EbProductImage img = imgs.get(i);
			JSONObject _jo = new JSONObject();
			_jo.put("imageSrc", SystemManager.getInstance().getSystemConfig().
					getImgServerUrl() +img.getImageCut());
			_jo.put("sortNum", img.getSortNum());
			_jo.put("type", img.getType());
			imgsjos.put(_jo);
		}
		result.put("imgs", imgsjos);
		if (ebProduct != null && ebProduct.getSkus() != null && ebProduct.getSkus().size() > 0) {
			JSONArray skuArrays = new JSONArray();
			JSONObject sku = null;
			int index = 0;
			for (EbSku ebSku : ebProduct.getSkus()) {
				if(ebSku.getStatus()==null||ebSku.getStatus().getValue().intValue() == 0){
					continue;
				}
				if(index>0){
					if( ebSku.getStorage() == null ||ebSku.getStorage().getAvailable()==null)
						continue;
					if(ebSku.getStorage().getAvailable()==0)
						continue;
				}
				index++;
				sku = new JSONObject();
				sku.put("skuCode", ebSku.getSkuCode());
				sku.put("SIZE", ebSku.getSize()==null?"":ebSku.getSize());
				sku.put("COLOR", ebSku.getColor()==null?"":ebSku.getColor());
				sku.put("status", ebSku.getStatus().getValue());
				sku.put("storageNum", ebSku.getStorage() == null ? 0 : ebSku.getStorage().getAvailable());
				skuArrays.put(sku);
			}
			result.put("skus", skuArrays);
		}
		return result;
	}
	
	
	private ExecuteResult getOneEbSeckill(){
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int ebActivityId = jsonObj.getInt("ebActivityId");
			EbProductService eps =  SystemInitialization.getApplicationContext().getBean(EbProductService.class);
			EbSecKill ebSecKill = eps.getEbSecKillByActivity(ebActivityId);
			JSONObject obj = new JSONObject();
			if(ebSecKill!=null){
				if(getContext().getHead().getPlatform().equals("ipad"))
					obj.put("imageSrc", SystemManager.getInstance().getSystemConfig().
							getImgServerUrl() +ebSecKill.getImageSrcPad());
				else
					obj.put("imageSrc", SystemManager.getInstance().getSystemConfig().
							getImgServerUrl() +ebSecKill.getImageSrcPhone());
				obj.put("secKillPrice", df.format(ebSecKill.getPrice()));
				obj.put("seckillId", ebSecKill.getId());
				obj.put("currentTime", System.currentTimeMillis());
				obj.put("productCode", ebSecKill.getProduct().getProductCode());
				obj.put("startTime", DateTimeFormatter.dateTime2String(ebSecKill.getStartTime()));
				obj.put("endTime", DateTimeFormatter.dateTime2String(ebSecKill.getEndTime()));
				obj.put("productNum", ebSecKill.getProductNum());
				obj.put("productName", ebSecKill.getProduct().getProductName());
				obj.put("price", df.format(ebSecKill.getProduct().getPrice()));
				int status =0;//倒计时
				if(ebSecKill.getStartTime().before(new Date()))
					status = 1;//在售中
				if(ebSecKill.getProductNum()<=0)
					status = 2;//售罄
				if(ebSecKill.getEndTime().before(new Date())){
					status = 2;//售罄
					obj.put("productNum", 0);
					
				}
				obj.put("status", status);
				if(status==0){
					//TODO 临时使用，只要status==0，则显示剩余100件，假如客户端改了时间
					obj.put("productNum", 100);
				}
//				int productStorage = 0;
//				for (EbSku ebSku : ebSecKill.getProduct().getSkus()) {
//					productStorage += ebSku.getStorage().getNum();
//				}
				obj.put("shareUrl", "http://m.ikan.cn/mobileAppAddress.action?from=14");//TODO	写分享的url
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取秒杀商品成功", obj, this); 
		} catch (Exception e) {
			logger.error("retrieveEbSeckill() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
}
