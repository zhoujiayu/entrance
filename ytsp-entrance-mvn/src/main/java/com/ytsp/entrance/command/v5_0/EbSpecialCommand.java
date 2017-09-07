package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbSpecial;
import com.ytsp.db.enums.EbPosterLinkUrlEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.EbProductVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.EbCouponService;
import com.ytsp.entrance.service.v5_0.EbProductCellectionService;
import com.ytsp.entrance.service.v5_0.EbSpecialService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.Util;

public class EbSpecialCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_EBSPECIAL_GETBYID == code||
				CommandList.CMD_EBSPECIALPRODUCTS_GETBYID==code
				||code == CommandList.CMD_SPECIAL_LIST;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if(code == CommandList.CMD_EBSPECIAL_GETBYID)
				return getEbSpecial();
			if(code == CommandList.CMD_EBSPECIALPRODUCTS_GETBYID)
				return getEbSpecialProducts();
			if(code == CommandList.CMD_SPECIAL_LIST)
				return getEbSpecialList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	* <p>功能描述:获取专场列表</p>
	* <p>参数：@return
	* <p>参数：@throws JSONException
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getEbSpecialList() throws JSONException, SqlException{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		if(!reqBody.isNull("page")){
			page = reqBody.getInt("page");
		}
		if(!reqBody.isNull("pageSize")){
			pageSize = reqBody.getInt("pageSize");
		}
		EbSpecialService specServ = SystemInitialization.getApplicationContext().getBean(EbSpecialService.class);
		List<EbSpecial> specials = specServ.getEbSpecialList(page,pageSize);
		EbSpecialInfoVO info = new EbSpecialInfoVO();
		info.setSpecialList(buildSpecialVO(specials));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(info));
		Util.addStatistics(getContext(), info);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专场列表成功！",result, this);
	}
	
	/**
	* <p>功能描述:构建专场VO</p>
	* <p>参数：@param specials
	* <p>参数：@return</p>
	* <p>返回类型：List<EbSpecialVO></p>
	 */
	private List<EbSpecialVO> buildSpecialVO(List<EbSpecial> specials){
		if(specials == null || specials.size() <= 0){
			return null;
		}
		List<EbSpecialVO> specVos = new ArrayList<EbSpecialVO>();
		for (EbSpecial ebSpecial : specials) {
			EbSpecialVO vo = new EbSpecialVO();
			vo.setDescription(ebSpecial.getDescription());
			vo.setEndDate(DateFormatter.date2String(ebSpecial.getEndDate(),"yyyy.MM.dd HH:mm"));
			vo.setId(ebSpecial.getId());
//			vo.setSpecialImage(Util.getFullImageURL(ebSpecial.getSpecialImage()));
			vo.setSpecialImage(Util.getFullImageURLByVersion(ebSpecial.getSpecialImage(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			vo.setSpecialName(ebSpecial.getSpecialName());
			vo.setStartDate(DateFormatter.date2String(ebSpecial.getStartDate(),"yyyy.MM.dd HH:mm"));
			vo.setProductCollect(ebSpecial.getProductCollect() == null ? "" : ebSpecial.getProductCollect().getName());
			vo.setOver(isOver(ebSpecial.getStartDate(),ebSpecial.getEndDate()));
			//专场是否开始,为了兼容以前版本isOver字段不能改，所以增加了isBegin字段
			vo.setBegin(isBegin(ebSpecial.getStartDate()));
			specVos.add(vo);
		}
		return specVos;
	}
	
	/**
	* <p>功能描述:专场是否开始</p>
	* <p>参数：@param startTime
	* <p>参数：@param endTime
	* <p>参数：@return 返回 true为已开始，否则为未开始</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isBegin(Date startTime) {
		Date now = new Date();
		if(startTime == null){
			return false;
		}
		if(startTime.after(now)){
			return false;
		}
		return true;
	}
	
	/**
	* <p>功能描述:专场是否结束</p>
	* <p>参数：@param startTime
	* <p>参数：@param endTime
	* <p>参数：@return 返回 true为结束，否则为false</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isOver(Date startTime, Date endTime) {
		Date now = new Date();
		if(startTime == null){
			return false;
		}
		if(endTime == null){
			return false;
		}
		return endTime.before(now);
	}
	
	private ExecuteResult getEbSpecialProducts() throws JSONException, SqlException {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject jo = new JSONObject();
		int ebSpecialId = jsonObj.optInt("ebSpecialId");
		int pageSize = jsonObj.optInt("pageSize");
		int page = jsonObj.optInt("page");
		EbSpecialService ebSpecialService = SystemInitialization
				.getApplicationContext().getBean(EbSpecialService.class);
		EbSpecial ebSpecial = ebSpecialService.getEbSpecialById(ebSpecialId);
		if(ebSpecial == null){
			jo.put("ebProductList", new JSONArray());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取分页的专场商品列表成功！",jo, this);
		}
		EbProductCellectionService ebProductCellectionService = SystemInitialization
				.getApplicationContext().getBean(EbProductCellectionService.class);
		List<EbProduct> products = ebProductCellectionService.getProductList(
				ebSpecial.getProductCollect(),page,pageSize);
		List<EbProductVO> ebProductList = new ArrayList<EbProductVO>();
		for (EbProduct foo : products) {
			EbProductVO vo = new EbProductVO();
//			vo.setImgUrl(Util.getFullImageURL(foo.getImgUrl()));
			vo.setImgUrl(Util.getFullImageURLByVersion(foo.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			vo.setProductCode(foo.getProductCode());
			vo.setProductName(foo.getProductName());
			vo.setStatus(foo.getStatus().getValue());
			vo.setPrice(foo.getPrice());
			vo.setVprice(foo.getVprice());
			vo.setSvprice(foo.getSvprice());
			//计算商品下所有sku库存的总数量
			Integer storageNum = Util.countProductStorage(foo);
			vo.setStorageStatus(storageNum > 0? 1 : 0);
			vo.setSkuCodes(getSkuCodes(foo));
			ebProductList.add(vo);
		}
		Gson gson = new Gson();
		Util.addStatistics(getContext(), ebProductList);
		jo.put("ebProductList", new JSONArray(gson.toJson(ebProductList)));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取分页的专场商品列表成功！",jo, this);
	}
	
	/**
	* <p>功能描述:获取skuCode列表</p>
	* <p>参数：@param product
	* <p>参数：@return</p>
	* <p>返回类型：List<Integer></p>
	 */
	private List<Integer> getSkuCodes(EbProduct product){
		List<Integer> skuCodes = new ArrayList<Integer>();
		if(product == null || product.getSkus() == null || product.getSkus().size() == 0){
			return skuCodes;
		}
		for (EbSku sku : product.getSkus()) {
			if(sku == null){
				continue;
			}
			skuCodes.add(sku.getSkuCode());
		}
		return skuCodes;
	}
	
	private ExecuteResult getEbSpecial() throws JSONException, SqlException {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int ebSpecialId = jsonObj.optInt("ebSpecialId");
		EbSpecialService ebSpecialService = SystemInitialization
				.getApplicationContext().getBean(EbSpecialService.class);
		EbProductCellectionService ebProductCellectionService = SystemInitialization
				.getApplicationContext().getBean(EbProductCellectionService.class);
		EbSpecial ebSpecial = ebSpecialService.getEbSpecialById(ebSpecialId);
		//根据专场id未找到相应的专场，直接返回
		if(ebSpecial == null){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专场成功！",null, this);
		}
		int pageSize = 12;
		//ipad与手机专场显示商品的个数不同，ipad显示30个，手机显示12个。
		if(MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform()) == MobileTypeEnum.ipad){
			pageSize = 30;
		}
		
		//这个页面的条数是12条
		List<EbProduct> products = ebProductCellectionService.getProductList(
				ebSpecial == null ? null:ebSpecial.getProductCollect(),0,pageSize);
		EbSpecialVO ebSpecialVO = new EbSpecialVO();
		List<EbProductVO> ebProductList = new ArrayList<EbProductVO>();
		for (EbProduct foo : products) {
			EbProductVO vo = new EbProductVO();
//			vo.setImgUrl(Util.getFullImageURL(foo.getImgUrl()));
			vo.setImgUrl(Util.getFullImageURLByVersion(foo.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			vo.setProductCode(foo.getProductCode());
			vo.setProductName(foo.getProductName());
			vo.setStatus(foo.getStatus().getValue());
			vo.setPrice(foo.getPrice());
			vo.setVprice(foo.getVprice());
			vo.setSvprice(foo.getSvprice());
			//计算商品下所有sku库存的总数量
			Integer storageNum = Util.countProductStorage(foo);
			vo.setStorageStatus(storageNum > 0? 1 : 0);
			vo.setSkuCodes(getSkuCodes(foo));
			ebProductList.add(vo);
		}
		ebSpecialVO.setEbProductList(ebProductList);
		List<CouponTemplateVO> coupons = new ArrayList<CouponTemplateVO>();
		if(ebSpecial.getEbCoupons() != null && ebSpecial.getEbCoupons().size() > 0){
			for (EbCoupon coupon : ebSpecial.getEbCoupons()) {
				CouponTemplateVO item = new CouponTemplateVO();
				item.setCouponName(coupon.getCouponName());
				item.setId(coupon.getId());
				item.setMinAmount(coupon.getMinAmount());
				item.setMoney(coupon.getMoney());
				if(getContext().getHead().getUid() != 0 && getSessionCustomer() != null && getSessionCustomer().getCustomer() != null){
					boolean isObtain = isCouponObtain(getContext().getHead().getUid(), item.getId());
					item.setIsObtainCoupon(isObtain? "1" : "0");
				}else{
					item.setIsObtainCoupon("0");
				}
				coupons.add(item);
			}
		}
		ebSpecialVO.setCoupons(coupons);
		ebSpecialVO.setDescription(ebSpecial.getDescription());
		ebSpecialVO.setEndDate(DateFormatter.date2String(ebSpecial.getEndDate(),"yyyy.MM.dd HH:mm"));
		ebSpecialVO.setId(ebSpecial.getId());
		//ipad详情图片与手机的详情图片不一样
		if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.ipad) {
//			ebSpecialVO.setSpecialImage(Util.getFullImageURL(ebSpecial
//					.getIpadImage()));

			ebSpecialVO.setSpecialImage(Util.getFullImageURLByVersion(ebSpecial
					.getIpadImage(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
		} else {
//			ebSpecialVO.setSpecialImage(Util.getFullImageURL(ebSpecial
//					.getSpecialImage()));
			ebSpecialVO.setSpecialImage(Util.getFullImageURLByVersion(ebSpecial
					.getSpecialImage(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
		}
		ebSpecialVO.setSpecialName(ebSpecial.getSpecialName());
		ebSpecialVO.setStartDate(DateFormatter.date2String(ebSpecial.getStartDate(),"yyyy.MM.dd HH:mm"));
		ebSpecialVO.setProductCollect(ebSpecial.getProductCollect()!=null?ebSpecial.getProductCollect().getName():"");
		// 是否使用新的分享地址
		if (Util.isUseNewShareURL(getContext().getHead().getPlatform(),
				getContext().getHead().getVersion())) {
			ebSpecialVO.setShareUrl(Util.getShareURL(
					EbPosterLinkUrlEnum.SPECIALDETAIL, 0,""+ebSpecialVO.getId()));
			ebSpecialVO.setShareUrl(ebSpecialVO.getShareUrl().replaceAll("\\{", "@lt"));
			ebSpecialVO.setShareUrl(ebSpecialVO.getShareUrl().replaceAll("\\}", "@gt"));
		} else {
			ebSpecialVO.setShareUrl(IConstants.SHAREURL);
		}
		Gson gson = new Gson();
		JSONObject jo = new JSONObject(gson.toJson(ebSpecialVO));
		Util.addStatistics(getContext(), ebSpecialVO);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专场成功！",jo, this);
	}
	
	/**
	* <p>功能描述:校验专场同一批次的优惠券只能领一张</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param couponId 专场优惠券模板id
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isCouponObtain(int userId,int couponId){
		EbCouponService couponServ = SystemInitialization.getApplicationContext().getBean(EbCouponService.class);
		return couponServ.isCouponObtain(userId, couponId); 
	}
	
	class CouponTemplateVO{
		private int id;
		private String couponName;
		private String description;
		private double minAmount;
		private double money;
		private boolean available;
		//用户是否获取优惠券，若没登录该字段为空
		private String isObtainCoupon = null;
		
		public String getIsObtainCoupon() {
			return isObtainCoupon;
		}
		public void setIsObtainCoupon(String isObtainCoupon) {
			this.isObtainCoupon = isObtainCoupon;
		}
		public boolean isAvailable() {
			return available;
		}
		public void setAvailable(boolean available) {
			this.available = available;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getCouponName() {
			return couponName;
		}
		public void setCouponName(String couponName) {
			this.couponName = couponName;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public double getMinAmount() {
			return minAmount;
		}
		public void setMinAmount(double minAmount) {
			this.minAmount = minAmount;
		}
		public double getMoney() {
			return money;
		}
		public void setMoney(double money) {
			this.money = money;
		}
	}
	
	
	class EbSpecialVO{
		private List<EbProductVO> ebProductList;
		private List<CouponTemplateVO> coupons;
		private Integer id = null;
		private String specialName = null;
		private String specialImage = null;
		private String description = null;
		private String startDate = null;
		private String endDate = null;
		private String productCollect = null;
		private boolean isOver;
		//专场是否开始标识：false表示未开始，true表示已开始
		private boolean isBegin;
		
		public boolean isBegin() {
			return isBegin;
		}
		public void setBegin(boolean isBegin) {
			this.isBegin = isBegin;
		}
		// 分享URL
		private String shareUrl;
		
		public String getShareUrl() {
			return shareUrl;
		}
		public void setShareUrl(String shareUrl) {
			this.shareUrl = shareUrl;
		}
		public boolean isOver() {
			return isOver;
		}
		public void setOver(boolean isOver) {
			this.isOver = isOver;
		}
		public List<EbProductVO> getEbProductList() {
			return ebProductList;
		}
		public void setEbProductList(List<EbProductVO> ebProductList) {
			this.ebProductList = ebProductList;
		}
		public List<CouponTemplateVO> getCoupons() {
			return coupons;
		}
		public void setCoupons(List<CouponTemplateVO> coupons) {
			this.coupons = coupons;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getSpecialName() {
			return specialName;
		}
		public void setSpecialName(String specialName) {
			this.specialName = specialName;
		}
		public String getSpecialImage() {
			return specialImage;
		}
		public void setSpecialImage(String specialImage) {
			this.specialImage = specialImage;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getStartDate() {
			return startDate;
		}
		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}
		public String getEndDate() {
			return endDate;
		}
		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}
		public String getProductCollect() {
			return productCollect;
		}
		public void setProductCollect(String productCollect) {
			this.productCollect = productCollect;
		}
		
	}
	
	class EbSpecialInfoVO{
		private List<EbSpecialVO> specialList;

		public List<EbSpecialVO> getSpecialList() {
			return specialList;
		}

		public void setSpecialList(List<EbSpecialVO> specialList) {
			this.specialList = specialList;
		}
		
	}
}
