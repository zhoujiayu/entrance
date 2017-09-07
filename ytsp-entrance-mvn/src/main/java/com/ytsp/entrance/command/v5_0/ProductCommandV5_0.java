package com.ytsp.entrance.command.v5_0;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.ActivityZone;
import com.ytsp.db.domain.BaseConfigData;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbBrandImg;
import com.ytsp.db.domain.EbComment;
import com.ytsp.db.domain.EbCommentImg;
import com.ytsp.db.domain.EbPoster;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductCollection;
import com.ytsp.db.domain.EbProductImage;
import com.ytsp.db.domain.EbProductParam;
import com.ytsp.db.domain.EbPromotion;
import com.ytsp.db.domain.EbPromotionItem;
import com.ytsp.db.domain.EbSecKill;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbTrack;
import com.ytsp.db.domain.Recommend;
import com.ytsp.db.domain.Topic;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.EbPosterLinkUrlEnum;
import com.ytsp.db.enums.EbProductTypeEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.EbPromotionTypeEnum;
import com.ytsp.db.enums.EbTrackTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.enums.RecommendVersionEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.CommentImgVO;
import com.ytsp.db.vo.CommentInfoVO;
import com.ytsp.db.vo.CommentVO;
import com.ytsp.db.vo.EbProductVO;
import com.ytsp.db.vo.PosterVO;
import com.ytsp.db.vo.ProductImageVO;
import com.ytsp.db.vo.ProductPromotionVO;
import com.ytsp.db.vo.RecommendVO;
import com.ytsp.db.vo.SecKillVO;
import com.ytsp.db.vo.SkuVO;
import com.ytsp.db.vo.TopicVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.BaseConfigService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.SearchService;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.service.v5_0.EbBrandService;
import com.ytsp.entrance.service.v5_0.EbProductCommentService;
import com.ytsp.entrance.service.v5_0.EbPromotionService;
import com.ytsp.entrance.service.v5_0.ProductServiceV5_0;
import com.ytsp.entrance.service.v5_0.RecommendServiceV5_0;
import com.ytsp.entrance.service.v5_0.TrackService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.DateTimeFormatter;
import com.ytsp.entrance.util.ImagePropertyUtil;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.Util;

public class ProductCommandV5_0 extends AbstractCommand {

	private DecimalFormat df = new DecimalFormat("##0.##");
	// 玩具首页热门玩具显示个数
	private static final Integer HOTPRODUCTSIZE = 12;
	// 默认显示的个数
	private static final Integer PAGESIZE = 12;
	// 商品详情属性：毛重
	private static final String WEIGHT = "毛重";
	// 商品详情属性：包装尺寸
	private static final String SIZE = "尺寸";
	// 商品详情属性：年龄
	private static final String AGE = "年龄";
	// 商品详情属性：名称
	private static final String PRODUCTNAME = "产品";
	// 商品详情属性：品牌
	private static final String BRANDNAME = "品 牌";
	// 商品详情属性：上架时间
	private static final String ONSHELFTIME = "上架";
	/**
	 * 一次获取评价的最多个数
	 */
	private Integer COMMENTNUM = 5;

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_PRODUCT_GETBYPRODUCODE
				|| code == CommandList.CMD_PRODUCT_HOMEPAGE
				|| code == CommandList.CMD_PRODUCT_PAGE
				|| code == CommandList.CMD_PRODUCT_BYCATEGORY
				|| code == CommandList.CMD_PRODUCT_MAXPRICE
				|| code == CommandList.CMD_PRODUCT_LOWPRICE
				|| code == CommandList.CMD_PRODUCT_NEWONSHELF
				|| code == CommandList.CMD_PRODUCT_GOODCOMMENT
				|| code == CommandList.CMD_PRODUCT_SELLBEST
				|| code == CommandList.CMD_PRODUCT_GET_BY_EANCODE;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (code == CommandList.CMD_PRODUCT_GETBYPRODUCODE) {
				int productCode = jsonObj.optInt("productCode");
				return getProductInfoByProductCode(productCode);
			} else if (code == CommandList.CMD_PRODUCT_HOMEPAGE) {
				return productHomePage();
			} else if (code == CommandList.CMD_PRODUCT_PAGE) {
				return hotProductByPage();
			} else if (code == CommandList.CMD_PRODUCT_BYCATEGORY) {
				return getProductByCategory();
			} else if (code == CommandList.CMD_PRODUCT_MAXPRICE) {
				return getProductByMaxPrice();
			} else if (code == CommandList.CMD_PRODUCT_LOWPRICE) {
				return getProductByLowPrice();
			} else if (code == CommandList.CMD_PRODUCT_NEWONSHELF) {
				return getProductByNewonShelf();
			} else if (code == CommandList.CMD_PRODUCT_GOODCOMMENT) {
				return getProductGoodComment();
			} else if (code == CommandList.CMD_PRODUCT_SELLBEST) {
				return getSoldBestProduct();
			}else if(code == CommandList.CMD_PRODUCT_GET_BY_EANCODE){
				return getProductByEANCode();
			}
		} catch (Exception e) {
			logger.error("ProductCommandV5_0 error:" + code + " head: "+getContext().getHead().toString()
					+" bodyParam :"+getContext().getBody().getBodyObject().toString()
					+ e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	* <p>功能描述:通过形码获取商品编码</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getProductByEANCode() throws SqlException,
			JSONException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		String EANCode = null;
		if (reqBody.isNull("EANCode")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "条形码不能为空",
					result, this);
		}
		EANCode = reqBody.optString("EANCode");
		//校验商品条形码为13位数字
		String reg = "^\\d{13}$";
		if(!EANCode.matches(reg)){
			result.put("productCode", 0);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "通过条形码获取商品成功",
					result, this);
		}
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		int productCode = prodServ.getProductByEANCode(EANCode);
		result.put("productCode", productCode);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "通过条形码获取商品成功",
				result, this);
	}
 
	
	/**
	 * <p>
	 * 功能描述:分页获取销售最多的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws SqlException
	 * @throws JSONException
	 */
	private ExecuteResult getSoldBestProduct() throws SqlException,
			JSONException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = PAGESIZE;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbProduct> prods = prodServ.getProductBySoldBest(page, pageSize);
		ProductsVO prodsVo = new ProductsVO();
		prodsVo.setProducts(buildProductVO(prods));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodsVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取销售最多的商品成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:分页获取最佳评论的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws SqlException
	 * @throws JSONException
	 */
	private ExecuteResult getProductGoodComment() throws SqlException,
			JSONException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = PAGESIZE;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbProduct> prods = prodServ.getProductByGoodComments(page,
				pageSize);
		ProductsVO prodsVo = new ProductsVO();
		prodsVo.setProducts(buildProductVO(prods));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodsVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取最佳评论商品成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:分页获取最新上价的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws SqlException
	 * @throws JSONException
	 */
	private ExecuteResult getProductByNewonShelf() throws SqlException,
			JSONException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		String column = "onShelfTIme";
		String sort = "desc";
		int page = 0;
		int pageSize = PAGESIZE;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbProduct> prods = prodServ.getProductOrderByColumn(column, sort,
				page, pageSize);
		ProductsVO prodsVo = new ProductsVO();
		prodsVo.setProducts(buildProductVO(prods));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodsVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取最新上价商品成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:分页获取价格最低的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws SqlException
	 * @throws JSONException
	 */
	private ExecuteResult getProductByLowPrice() throws SqlException,
			JSONException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		String column = "vprice";
		String sort = "asc";
		int page = 0;
		int pageSize = PAGESIZE;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbProduct> prods = prodServ.getProductOrderByColumn(column, sort,
				page, pageSize);
		ProductsVO prodsVo = new ProductsVO();
		prodsVo.setProducts(buildProductVO(prods));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodsVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取价格最低商品成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:分页获取价格最高的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws SqlException
	 * @throws JSONException
	 */
	private ExecuteResult getProductByMaxPrice() throws SqlException,
			JSONException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		String column = "vprice";
		String sort = "desc";
		int page = 0;
		int pageSize = PAGESIZE;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbProduct> prods = prodServ.getProductOrderByColumn(column, sort,
				page, pageSize);
		ProductsVO prodsVo = new ProductsVO();
		prodsVo.setProducts(buildProductVO(prods));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodsVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取价格最高商品成功",
				result, this);
	}

	/**
	 * 入参：categoryId 分类id
	 * <p>
	 * 功能描述:
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws JSONException
	 * @throws SqlException
	 */
	private ExecuteResult getProductByCategory() throws JSONException,
			SqlException {
		JSONObject result = new JSONObject();
		JSONObject body = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = 10;
		if (body.isNull("categoryId")) {
			result.put("result", false);
			result.put("msg", "categoryId不能为空");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"根据分类获取商品失败", result, this);
		}
		if (!body.isNull("page")) {
			page = body.getInt("page");
		}
		if (!body.isNull("pageSize")) {
			pageSize = body.getInt("pageSize");
		}

		Integer categoryId = body.getInt("categoryId");
		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		// 从搜索服务中获取某一分类的商品
		List<EbProductVO> productVOs = searchServ.searchIndexProductsByPage(
				categoryId, page, pageSize,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());

		Gson gson = new Gson();
		ProductsVO products = new ProductsVO();
		products.setProducts(productVOs);
		result = new JSONObject(gson.toJson(products));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "根据分类获取商品成功",
				result, this);
	}
	
	
	/**
	* <p>功能描述:获取推荐热搜词</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<String></p>
	 */
	private List<String> getHotSearchKeys(String code) throws SqlException{
		BaseConfigService baseConfServ = SystemInitialization.getApplicationContext().getBean(BaseConfigService.class);
		return baseConfServ.getHotSearchKeys(code);
	}
	
	/**
	 * <p>
	 * 功能描述:玩具首页
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws SqlException
	 * @throws JSONException
	 */
	private ExecuteResult productHomePage() throws SqlException, JSONException {
		int version = 5;
		int defaultSize = HOTPRODUCTSIZE;
		String headVersion = getContext().getHead().getVersion();
		if (StringUtil.isNotNullNotEmpty(headVersion)
				&& Util.validateVersion(headVersion)) {
			version = Integer.parseInt(headVersion.split("\\.")[0]);
		}
		JSONObject reqParam = getContext().getBody().getBodyObject();
		ProductHomePageVO prodHomeVO = new ProductHomePageVO();
		RecommendServiceV5_0 rs = SystemInitialization.getApplicationContext()
				.getBean(RecommendServiceV5_0.class);
		if((MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform()) == MobileTypeEnum.wapmobile)){
			version = 5;
		}
		//移动端网站根据专区类型
		if(reqParam.has("actype")){
			int actype = reqParam.optInt("actype");
			dealWapMobileZoneInfo(actype, prodHomeVO);
		}
		List<Recommend> rds = rs.getRecommend(RecommendTypeEnum.ITOY,
				RecommendVersionEnum.valueOf(version));
		// 处理玩具推荐页
		fillRecommend(rds, prodHomeVO);
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbPoster> posters = prodServ.getProductHomePoster();
		// 处理玩具首页海报
		fillPosters(posters, prodHomeVO);
		//判断是否为pad,pad首页默认显示24个
		if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.ipad)) {
			defaultSize = 24;
		}
		List<EbProduct> ps = new ArrayList<EbProduct>();
		//iphone版本大于5.0.4，android大于5.0.2，要求玩具首页热卖玩具显示与推荐页数据不一致 
		if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.iphone
				&& ("4.4.3".equals(headVersion) || "5.0.0".equals(headVersion) || "5.0.1".equals(headVersion)
						|| "5.0.2".equals(headVersion)
						|| "5.0.3".equals(headVersion) || "5.0.4"
							.equals(headVersion))) {
			ps = rs.getRecommendProduct(0, defaultSize);
		} else if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.gphone
				&& ("5.0.0".equals(headVersion) || "5.0.1".equals(headVersion) || "5.0.2"
						.equals(headVersion))) {
			ps = rs.getRecommendProduct(0, defaultSize);
		} else if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.ipad
				&& ("5.0.0".equals(headVersion))) {
			ps = rs.getRecommendProduct(0, defaultSize);
		} else {
			ps = rs.getRecommendHotProduct(0, defaultSize);
		}
		
		// 处理热门玩具
		prodHomeVO.setProducts(buildProductVO(ps));
		// 设置所有热搜词
		Map<String,List<String>> allHotKeys = getAllHotSearchKeys();
		List<String> productHotKeys = allHotKeys.get(IConstants.CONFIG_PRODUCT_SK);
		//设置动漫热搜词
		//由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
		prodHomeVO.setHotKeys(productHotKeys);
		prodHomeVO.setKnowledgeHotKeys(allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK));
		prodHomeVO.setProductHotKeys(productHotKeys);
		prodHomeVO.setAlbumHotKeys(allHotKeys.get(IConstants.CONFIG_ALBUM_SK));
		prodHomeVO.setRecommendHotKeys(allHotKeys.get(IConstants.CONFIG_RECOMMEND_SK));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodHomeVO));
		//添加统计数据
		Util.addStatistics(getContext(), prodHomeVO);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取玩具首页成功",
				result, this);
	}
	
	/**
	* <p>功能描述:移动端网站获取专区信息</p>
	* <p>参数：@param actype 专区类型
	* <p>参数：@param prodHomeVO 
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	private void dealWapMobileZoneInfo(int actype,ProductHomePageVO prodHomeVO) throws SqlException{
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		ActivityZone zone = prodServ.getActivityZoneById(actype);
		if(zone == null){
			return;
		}
		prodHomeVO.setZoneName(zone.getActivityName());
		prodHomeVO.setZoneRedirectUrl(zone.getActivityUrl());
	}
	
	/**
	* <p>功能描述:移动端网站获取下载提示信息</p>
	* <p>参数：@param code
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 * @throws SqlException 
	 */
	private String getBaseConfigValueByCode(String code) throws SqlException{
		String value = "";
		if(StringUtil.isNullOrEmpty(code)){
			return value;
		}
		BaseConfigService baseConfServ = SystemInitialization.getApplicationContext().getBean(BaseConfigService.class);
		BaseConfigData config = baseConfServ.getBaseConfigDataByCode(code);
		if(config == null){
			return value;
		}
		return config.getAttrvalue().trim();
	}
	
	/**
	* <p>功能描述:获取所有热搜词</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<String></p>
	 */
	private Map<String,List<String>> getAllHotSearchKeys() throws SqlException{
		BaseConfigService baseConfServ = SystemInitialization.getApplicationContext().getBean(BaseConfigService.class);
		return baseConfServ.getAllHotSearchKeys();
	}
	
	/**
	 * <p>
	 * 功能描述:分页获取热门玩具
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult hotProductByPage() throws SqlException, JSONException {
		JSONObject body = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = HOTPRODUCTSIZE;
		if (!body.isNull("page")) {
			page = body.getInt("page");
		}
		if (!body.isNull("pageSize")) {
			pageSize = body.getInt("pageSize");
		}
		RecommendServiceV5_0 rs = SystemInitialization.getApplicationContext()
				.getBean(RecommendServiceV5_0.class);
		List<EbProduct> ps = rs.getRecommendProduct(page, pageSize);
		
		//iphone版本大于5.0.4，android大于5.0.2，要求玩具首页热卖玩具显示与推荐页数据不一致
		String headVersion = getContext().getHead().getVersion();
		if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.iphone
				&& ("4.4.3".equals(headVersion) || "5.0.0".equals(headVersion) || "5.0.1".equals(headVersion)
						|| "5.0.2".equals(headVersion)
						|| "5.0.3".equals(headVersion) || "5.0.4"
							.equals(headVersion))) {
			ps = rs.getRecommendProduct(page, pageSize);
		} else if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.gphone
				&& ("5.0.0".equals(headVersion) || "5.0.1".equals(headVersion) || "5.0.2"
						.equals(headVersion))) {
			ps = rs.getRecommendProduct(page, pageSize);
		} else if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.ipad
				&& ("5.0.0".equals(headVersion))) {
			ps = rs.getRecommendProduct(page, pageSize);
		} else {
			ps = rs.getRecommendHotProduct(page, pageSize);
		}
		ProductInfoVO infoVO = new ProductInfoVO();
		infoVO.setProducts(buildProductVO(ps));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(infoVO));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "分页获取热门玩具成功",
				result, this);
	}

	private List<EbProductVO> buildProductVO(List<EbProduct> ps) {
		List<EbProductVO> ls = new ArrayList<EbProductVO>();
		for (EbProduct p : ps) {
			EbProductVO vo = new EbProductVO();
//			vo.setImgUrl(Util.getFullImageURL(p.getImgUrl()));
			vo.setImgUrl(Util.getFullImageURLByVersion(p.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			vo.setPrice(p.getPrice());
			vo.setProductCode(p.getProductCode());
			vo.setProductName(p.getProductName());
			vo.setStatus(p.getStatus().getValue());
			vo.setSvprice(p.getSvprice());
			vo.setVprice(p.getVprice());
			//计算商品下所有sku库存的总数量
			Integer storageNum = Util.countProductStorage(p);
			vo.setStorageStatus(storageNum > 0? 1 : 0);
			ls.add(vo);
		}
		return ls;
	}

	/**
	 * <p>
	 * 功能描述:动漫海报
	 * </p>
	 * <p>
	 * 参数：@param posters
	 * <p>
	 * 参数：@param albumHome
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void fillPosters(List<EbPoster> posters,
			ProductHomePageVO prodHomeVO) {
		Map<Integer,List<PosterVO>> overDatePoster = new HashMap<Integer, List<PosterVO>>();
		for (EbPoster p : posters) {
			PosterVO vo = new PosterVO();
			vo.setId(p.getId());
			vo.setDescription(p.getDescription());
			//ipad与手机端的图片大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
//				vo.setImg(Util.getFullImageURL(p.getIpadimg()));
				vo.setImg(Util.getFullImageURLByVersion(p.getIpadimg(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			}else{
//				vo.setImg(Util.getFullImageURL(p.getImg()));
				vo.setImg(Util.getFullImageURLByVersion(p.getImg(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			}
			vo.setSortNum(NumericUtil.parseInt(p.getSortNum(), 10000));
			vo.setUrl(p.getUrl());
			vo.setPostertitle(p.getPostertitle());
			vo.setPostersubtitle(p.getPostersubtitle());
			//将无效的海报放到map里
			if(Util.checkDateValidate(p.getStartTime(), p.getEndTime()) != 1){
				if(overDatePoster.containsKey(p.getLocation())){
					overDatePoster.get(p.getLocation()).add(vo);
				}else{
					List<PosterVO> voList = new ArrayList<PosterVO>();
					voList.add(vo);
					overDatePoster.put(p.getLocation(), voList);
				}
				continue;
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPBANNER.getValue() && prodHomeVO.getBanner() == null) {
				prodHomeVO.setBanner(vo);
				//增加ipad横幅，由于横幅有多个，以前的banner之前版本还在用，所以加了个多个横幅。
				List<PosterVO> banners = new ArrayList<PosterVO>();
				banners.add(vo);
				prodHomeVO.setBanners(banners);
			} else if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPBANNER
					.getValue()
					&& prodHomeVO.getBanners() != null
					&& MobileTypeEnum.valueOf(getContext().getHead()
							.getPlatform()) == MobileTypeEnum.ipad) {
				prodHomeVO.getBanners().add(vo);
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND2ED
					.getValue()) {
				if (prodHomeVO.getSeconedPosters() == null) {
					List<PosterVO> seconedPosters = new ArrayList<PosterVO>();
					seconedPosters.add(vo);
					prodHomeVO.setSeconedPosters(seconedPosters);
				} else if (prodHomeVO.getSeconedPosters().size() <= 2) {// 2级海报只有2个
					prodHomeVO.getSeconedPosters().add(vo);
				}
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND3RD
					.getValue()) {// 4个3级海报
				if (prodHomeVO.getThirdPosters() == null) {
					List<PosterVO> thirdPosters = new ArrayList<PosterVO>();
					thirdPosters.add(vo);
					prodHomeVO.setThirdPosters(thirdPosters);
				} else if (prodHomeVO.getThirdPosters().size() <= 4) {
					prodHomeVO.getThirdPosters().add(vo);
				}
			}
			// 处理推荐页的导航
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.NAVIGATIONBAR
					.getValue()) {
				//如果ios在审核期间，将活动页的vip转换成
				if (Util.isIOSInReview(
						getContext().getHead().getPlatform(), getContext()
								.getHead().getVersion())) {
					if (vo.getUrl() != null
							&& vo.getUrl()
									.trim()
									.equals("http://entrance.ikan.cn/act/vipRechange/vipRecharge.html")) {
						// 设置为成为vip购买页面
						vo.setUrl(IConstants.VIPURL);
					}
				}
				if (prodHomeVO.getNavigations() == null) {
					List<PosterVO> navigationPoster = new ArrayList<PosterVO>();
					navigationPoster.add(vo);
					prodHomeVO.setNavigations(navigationPoster);
				} else if (prodHomeVO.getNavigations().size() <= 3) {
					prodHomeVO.getNavigations().add(vo);
				}
			}
		}
		dealPoster(prodHomeVO, overDatePoster);
	}

	/**
	* <p>功能描述:处理过期的海报</p>
	* <p>参数：@param ret
	* <p>参数：@param overDatePoster</p>
	* <p>返回类型：void</p>
	 */
	private void dealPoster(ProductHomePageVO ret,Map<Integer,List<PosterVO>> overDatePoster){
		//2个二级海报位
		int secondCount = ret.getSeconedPosters() != null ?ret.getSeconedPosters().size() : 0;
		if(secondCount < 2){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue());
			List<PosterVO> seconedPosters = ret.getSeconedPosters() != null ? ret.getSeconedPosters():new ArrayList<PosterVO>();
			for (int i = 0; i < 2 - secondCount; i++) {
				if(volist != null && i < volist.size()){
					seconedPosters.add(volist.get(i));
				}
			}
			ret.setSeconedPosters(seconedPosters);
		}
		//处理三级海报
		int thirdCount = ret.getThirdPosters() != null ?ret.getThirdPosters().size() : 0;
		if(thirdCount < 4){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue());
			List<PosterVO> thirdPosters =  ret.getThirdPosters() != null ? ret.getThirdPosters():new ArrayList<PosterVO>();
			for (int i = 0; i < 4 - thirdCount; i++) {
				if(volist != null && i < volist.size()){
					thirdPosters.add(volist.get(i));
				}
			}
			ret.setThirdPosters(thirdPosters);
		}
		//ipad2个横幅
		if(MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform()) == MobileTypeEnum.ipad){
			int bannerCount = ret.getBanners() != null ?ret.getBanners().size() : 0;
			if(bannerCount < 2){
				List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPBANNER.getValue());
				List<PosterVO> bannerPosters =  ret.getBanners() != null ? ret.getBanners():new ArrayList<PosterVO>();
				for (int i = 0; i < 2 - bannerCount; i++) {
					if(volist != null && i < volist.size()){
						bannerPosters.add(volist.get(i));
					}
				}
				ret.setBanners(bannerPosters);
			}
			
		}else{
			//处理横幅
			if(ret.getBanner() == null){
				List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPBANNER.getValue());
				if(volist != null && volist.size() > 0){
					ret.setBanner(volist.get(0));
				}
			}
		}
		//导航处理
		int navigationCount = ret.getNavigations() != null ?ret.getNavigations().size() : 0;
		if(navigationCount < 3){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.NAVIGATIONBAR.getValue());
			List<PosterVO> navigationsPosters =  ret.getNavigations() != null ? ret.getNavigations():new ArrayList<PosterVO>();
			for (int i = 0; i < 3 - navigationCount; i++) {
				if(volist != null && i < volist.size()){
					navigationsPosters.add(volist.get(i));
				}
			}
			ret.setNavigations(navigationsPosters);
		}
		
	}
	
	/**
	 * <p>
	 * 功能描述:推荐玩具
	 * </p>
	 * <p>
	 * 参数：@param rds
	 * <p>
	 * 参数：@param animeHome
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void fillRecommend(List<Recommend> rds, ProductHomePageVO prodHomeVO) {
		List<RecommendVO> recommends = new ArrayList<RecommendVO>();
		for (Recommend r : rds) {
			RecommendVO vo = new RecommendVO();
			vo.setId(r.getId());
			//ipad与手机的轮播图大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
//				vo.setImg(Util.getFullImageURL(r.getPadimg()));
				vo.setImg(Util.getFullImageURLByVersion(r.getPadimg(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			}else{
//				vo.setImg(Util.getFullImageURL(r.getImg()));
				vo.setImg(Util.getFullImageURLByVersion(r.getImg(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			}
			vo.setRedirect(r.getRedirect());
			vo.setSort(NumericUtil.parseInt(r.getSort(), 10000));
			vo.setSummary(r.getSummary());
			recommends.add(vo);
		}
		prodHomeVO.setRecommends(recommends);
	}

	/**
	 * @功能描述:通过商品code获取商品信息
	 * @param productCode
	 * @return
	 * @throws Exception
	 * @throws JSONException
	 *             ExecuteResult
	 * @author yusf
	 */
	private ExecuteResult getProductInfoByProductCode(int productCode)
			throws Exception, JSONException {
		JSONObject result = new JSONObject();
		if (productCode == 0) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"商品编码错误！", result, this);
		}
		EbProductService eps = SystemInitialization.getApplicationContext()
				.getBean(EbProductService.class);
		EbProduct ebProduct = eps.retrieveProductByProductCode(productCode);
		if (ebProduct == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"未找到相应的商品", result, this);
		}

		ProductReturnVO resultVo = getProductAndSkusVO(ebProduct);

		EbPromotionService promotionService = SystemInitialization
				.getApplicationContext().getBean(EbPromotionService.class);
		//是否收藏标识
		if(promotionService.hasCollection(productCode,getContext().getHead().getUid())>0){
			resultVo.setCollectionStatus(true);
		}
		
		List<EbPromotion> promotions = promotionService
				.retrieveEbPromotionList();
		
		// 促销活动
		List<ProductPromotionVO> reducePromotion = new ArrayList<ProductPromotionVO>();
		List<ProductPromotionVO> giftPromotion = new ArrayList<ProductPromotionVO>();
		List<ProductPromotionVO> couponPromotion = new ArrayList<ProductPromotionVO>();
		for (EbPromotion p : promotions) {
			if(p.getPromotionType() == null){
				continue;
			}
			int type = p.getPromotionType().getValue().intValue();
			EbProductCollection prodCollection = p.getEbProductCollection();
			// 如果促销为全场或者所选的商品在促销商品集合内，构建促销VO
			if ((p.getIsForAll() == null ? false : p.getIsForAll())
					|| (isInCollection(prodCollection, ebProduct))) {
				if (type == EbPromotionTypeEnum.REDUCE.getValue().intValue()) {
					reducePromotion.addAll(getProductPromotionVO(p));
				} else if (type == EbPromotionTypeEnum.GIFT.getValue()
						.intValue()) {
					giftPromotion.addAll(getProductPromotionVO(p));
				} else if (type == EbPromotionTypeEnum.COUPON.getValue()
						.intValue()) {
					couponPromotion.addAll(getProductPromotionVO(p));
				}
			}
		}
		resultVo.setReducePromotion(reducePromotion);
		resultVo.setGiftPromotion(giftPromotion);
		resultVo.setCouponPromotion(couponPromotion);
		// 处理商品评价
		dealWithComment(resultVo);
		// 处理评测
		dealEvaluating(resultVo, ebProduct);
		// 设置爱看点评
		resultVo.setiKanComment(ebProduct.getComment() == null ? "" : ebProduct
				.getComment());
		resultVo.setActivityId(ebProduct.getEbActivity().getActivityId());
		resultVo.setActivityName(ebProduct.getEbActivity().getActivityName());
		resultVo.setCurrentTime(System.currentTimeMillis());
		// 写分享的url
		String shareUrl = IConstants.SHAREURL;
		//是否使用新的分享地址
		boolean isUseNewShareURL = Util.isUseNewShareURL(getContext().getHead()
				.getPlatform(), getContext().getHead().getVersion());
		if(isUseNewShareURL){
			shareUrl = Util.getShareURL(EbPosterLinkUrlEnum.PRODUCT, 0, ""
					+ ebProduct.getProductCode());
			resultVo.setShareUrl("####");
		}else{
			resultVo.setShareUrl(shareUrl);
		}
		
		// 正品保证图片
		resultVo.setQualityGuaranteeImageVOs(buildQualityGuaranteeImageVOs(ebProduct
				.getEbBrand() != null ? ebProduct.getEbBrand().getBrandId() : 0));
		// 正品保证文字
		resultVo.setQualityGuaranteeTextVOs(buildQualityGuaranteeTextVOs());
		// 设置商品属性
		setProductParams(resultVo, ebProduct);
		// 保存足迹
		if (isLogin()) {
			saveProductTack(getContext().getHead().getUid(),
					EbTrackTypeEnum.PRODUCT.getValue(), resultVo, ebProduct);
		}
		//移动端网站获取下载提示内容
		if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.wapmobile)) {
			resultVo.setDownloadInfo(getBaseConfigValueByCode(IConstants.DOWNLOADINFO));
		}
		Gson gson = new Gson();
		//使用新的分享地址，由于实体转换为json字符串，会把等号和&符号转义，所以这里特殊处理一下。
		if(isUseNewShareURL){
			result.put("productInfoVO", gson.toJson(resultVo).toString().replaceAll("####", shareUrl));
		}else{
			result.put("productInfoVO", gson.toJson(resultVo));
		}
		Util.addStatistics(getContext(), resultVo);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"获取单个商品和SKU成功", result, this);
	}

	/**
	 * <p>
	 * 功能描述:设置商品属性
	 * </p>
	 * <p>
	 * 参数：@param resultVo
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void setProductParams(ProductReturnVO resultVo, EbProduct ebProduct)
			throws SqlException {
		ProductServiceV5_0 prodServ = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<EbProductParam> params = prodServ
				.getProductParamByProductCode(resultVo.getProductCode());
		// 商品固有属性list
		List<ParamVO> productParams = new ArrayList<ParamVO>();
		// 商品扩展属性list
		List<ParamVO> extendParams = new ArrayList<ParamVO>();
		// 商品名称
		productParams
				.add(createParamVO(PRODUCTNAME, resultVo.getProductName()));
		// 品牌
		productParams.add(createParamVO(BRANDNAME, ebProduct.getEbBrand()==null?"":ebProduct.getEbBrand()
				.getBrandName()));
		// 上架时间
		productParams
				.add(createParamVO(ONSHELFTIME, resultVo.getOnShelfTIme()));
		if (params != null && params.size() > 0) {
			for (EbProductParam ebProductParam : params) {
				String keyName = ebProductParam.getKeyName();
				if (StringUtil.isNullOrEmpty(keyName)
						|| StringUtil.isNullOrEmpty(ebProductParam
								.getValueString())) {
					continue;
				}
				if (keyName.equals(PRODUCTNAME)) {
					// 不处理，取商品名称
				} else if (keyName.equals(AGE) || "适用年龄".equals(keyName)) {
					productParams.add(createParamVO(keyName,
							ebProductParam.getValueString()));
				} else if (keyName.equals(ONSHELFTIME)) {
					// 不处理，取商品上架时间
				} else if (keyName.equals(BRANDNAME)||keyName.equals("品牌")) {
					// 不处理，取商品品牌
				} else if (keyName.equals(WEIGHT)) {
					productParams.add(createParamVO(keyName,
							ebProductParam.getValueString()));
				} else if (keyName.equals(SIZE) || "包装尺寸".equals(keyName)) {
					productParams.add(createParamVO(keyName,
							ebProductParam.getValueString()));
				} else {
					extendParams.add(createParamVO(keyName,
							ebProductParam.getValueString()));
				}
			}
		}
		//将商品属性和扩展属性合并，由于放在第一张图上
		productParams.addAll(extendParams);
		resultVo.setProductParams(productParams);
		resultVo.setExtendParams(new ArrayList<ParamVO>());
	}

	/**
	 * <p>
	 * 功能描述:创建属性VO
	 * </p>
	 * <p>
	 * 参数：@param name
	 * <p>
	 * 参数：@param value
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ParamVO
	 * </p>
	 */
	private ParamVO createParamVO(String name, String value) {
		ParamVO param = new ParamVO();
		param.setParamName(name);
		param.setParaValue(value);
		return param;
	}

	/**
	 * <p>
	 * 功能描述:保存足迹
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@param trackType 足迹类型
	 * <p>
	 * 参数：@param resultVo 商品详情VO
	 * <p>
	 * 参数：@throws Exception
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void saveProductTack(int userId, int trackType,
			ProductReturnVO resultVo, EbProduct ebProduct) throws Exception {
		TrackService trackService = SystemInitialization
				.getApplicationContext().getBean(TrackService.class);
		EbTrack track = trackService.getTrackIdAndType(userId, trackType,
				DateFormatter.date2String(new Date()),
				resultVo.getProductCode());
		// 若已有浏览足迹将相应的足迹浏览次数+1，否则保存足迹
		if (track != null) {
			EbTrack trackBak = track._getCopy();
			// 删除原有，由于分页是按id排序的，所以这里要删除，不然重复浏览的足迹不会显示到最前面
			track.setStatus(0);
			trackService.updateTrack(track);
			trackBak.setViewCount(trackBak.getViewCount() + 1);
			trackBak.setUpdateTime(new Date());
			// 保存新足迹
			trackService.saveTrack(trackBak);
		} else {
			trackService.saveTrack(getTrack(trackType, resultVo, ebProduct));
		}
	}

	/**
	 * <p>
	 * 功能描述:是否登录
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isLogin() {
		if (getSessionCustomer() == null) {
			return false;
		} else if (getSessionCustomer().getCustomer() == null) {
			return false;
		} else if (getContext().getHead().getUid() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * 功能描述:
	 * </p>
	 * <p>
	 * 参数：@param trackType 足迹类型
	 * <p>
	 * 参数：@param resultVo 商品详情VO
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws Exception
	 * </p>
	 * <p>
	 * 返回类型：EbTrack
	 * </p>
	 */
	private EbTrack getTrack(int trackType, ProductReturnVO resultVo,
			EbProduct ebProduct) throws Exception {
		EbTrack track = new EbTrack();
		track.setUserId(getContext().getHead().getUid());
		track.setTrackType(EbTrackTypeEnum.valueOf(trackType));
		track.setViewCount(1);
		track.setCreateTime(DateFormatter.date2String(new Date()));
		track.setProductCode(resultVo.getProductCode());
		track.setIkanPrice(resultVo.getVprice());
		track.setVprice(resultVo.getSvprice());
		track.setTrackName(resultVo.getProductName());
		track.setStatus(1);
		track.setUpdateTime(new Date());
		// 设置图片
		track.setImageSrc(getProductImage(resultVo, ebProduct));

		return track;
	}

	/**
	 * <p>
	 * 功能描述:获取商品的第一张描述图片
	 * </p>
	 * <p>
	 * 参数：@param resultVo
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：String
	 * </p>
	 */
	private String getProductImage(ProductReturnVO resultVo, EbProduct ebProduct) {
		String image = "";
		if (resultVo.getImgs() != null && resultVo.getImgs().size() > 0) {
			image = resultVo.getImgs().get(0).getImageSrc();
			String imageServerHost = ImagePropertyUtil.getPropertiesValue("custImageHost").trim();
			String httpsCustImageHost = ImagePropertyUtil.getPropertiesValue("httpsCustImageHost").trim();
			
			image = image.replaceAll(imageServerHost, "").replaceAll(httpsCustImageHost, "");
		}
		// 若描述图片没有，取商品的图片
		if (StringUtil.isNullOrEmpty(image)) {
			String prodImage = ebProduct.getImgUrl();
//			if (StringUtil.isNullOrEmpty(prodImage)) {
//				return image;
//			}
//			String imgHost = SystemManager.getInstance().getSystemConfig()
//					.getImgServerUrl();
//			if (prodImage.startsWith("/")) {
//				image = imgHost + prodImage.substring(1);
//			} else {
//				image = imgHost + prodImage;
//			}
//			image = Util.getFullImageURL(prodImage);
//			image = Util.getFullImageURLByVersion(prodImage,getContext().getHead().getVersion(),getContext().getHead().getPlatform());
			String imageServerHost = ImagePropertyUtil.getPropertiesValue("custImageHost").trim();
			String httpsCustImageHost = ImagePropertyUtil.getPropertiesValue("httpsCustImageHost").trim();
			
			image = prodImage.replaceAll(imageServerHost, "").replaceAll(httpsCustImageHost, "");
		}
		return image;
	}

	/**
	 * <p>
	 * 功能描述:获取商品评测
	 * </p>
	 * <p>
	 * 参数：@param resultVo
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void dealEvaluating(ProductReturnVO resultVo, EbProduct product)
			throws SqlException {
		// TopicService topicServ = SystemInitialization.getApplicationContext()
		// .getBean(TopicService.class);
		// Topic topic = topicServ.getTopicByProductCode(
		// resultVo.getProductCode(), TopicTypeEnum.COMMENT);

		resultVo.setTopic(getTopicVO(product.getCommentTopic()));
	}

	/**
	 * <p>
	 * 功能描述:获取专题VO
	 * </p>
	 * <p>
	 * 参数：@param topicList
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<TopicVO>
	 * </p>
	 */
	private TopicVO getTopicVO(Topic topic) {
		if (topic == null) {
			return null;
		}

		TopicVO vo = new TopicVO();
		vo.setContent(topic.getContent());
		vo.setTitle(topic.getTitle());
		vo.setTopicId(topic.getId());
		vo.setTopicImage(getImage(topic.getTopicImage()));
		vo.setUrl(topic.getUrl());
		vo.setTopicName(topic.getTopicName());
		return vo;
	}

	private List<String> getImage(String image) {
		if (image == null || image.length() <= 0) {
			return null;
		}
		List<String> ret = new ArrayList<String>();
		String[] imgArr = image.split(",");
		String host = ImagePropertyUtil.getPropertiesValue("custImageHost").trim();
		for (String img : imgArr) {
			if(StringUtil.isNullOrEmpty(img.trim())){
				continue;
			}
			//过滤掉gif图片，因为在安卓或者ios上显示图片会有问题
			if(img.endsWith(".gif") || img.endsWith(".GIF")){
				continue;
			}
			if(img.startsWith("http://images.kandongman.com.cn/")){
				img = img.replaceAll("http://images.kandongman.com.cn/", host).toString();
			}
			ret.add(img);
		}
		return ret;
	}

	/**
	 * @功能描述: 处理商品评价VO,分页获取每种评论
	 * @param resultVo
	 * @author yusf
	 * @throws Exception
	 */
	private void dealWithComment(ProductReturnVO resultVo) throws Exception {
		EbProductCommentService commentService = SystemInitialization
				.getApplicationContext().getBean(EbProductCommentService.class);
		CommentInfoVO commentInfo = new CommentInfoVO();
		
		List<EbComment> poorComments = null;
		List<EbComment> normalComments = null;
		List<EbComment> goodComments = null;
		
		String platform = getContext().getHead().getPlatform();
		//苹果5.0.0，5.0.1，5.0.2是根据评论id排序，由于历史数据有些id是无序的。
		if (MobileTypeEnum.iphone == MobileTypeEnum.valueOf(platform)
				&& ("4.4.3".equals(getContext().getHead().getVersion())
						|| "5.0.0".equals(getContext().getHead().getVersion())
						|| "5.0.1".equals(getContext().getHead().getVersion())
						|| "5.0.2".equals(getContext().getHead().getVersion())
						|| "5.0.3".equals(getContext().getHead().getVersion()) || "5.0.4"
							.equals(getContext().getHead().getVersion()))) {
			poorComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 1, 0, COMMENTNUM);
			normalComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 2, 0, COMMENTNUM);
			goodComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 3, 0, COMMENTNUM);
		}
		if (MobileTypeEnum.ipad == MobileTypeEnum.valueOf(platform)
				&& "5.0.0".equals(getContext().getHead().getVersion())) {
			poorComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 1, 0, COMMENTNUM);
			normalComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 2, 0, COMMENTNUM);
			goodComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 3, 0, COMMENTNUM);
		} else if (MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platform)
				&& "5.0.0".equals(getContext().getHead().getVersion())) {
			poorComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 1, 0, COMMENTNUM);
			normalComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 2, 0, COMMENTNUM);
			goodComments = commentService.getProductCommentByPage(
					resultVo.getProductCode(), 3, 0, COMMENTNUM);
		} else {
			poorComments = commentService.queryPageProductCommentByTime(
					resultVo.getProductCode(), 1, 0, COMMENTNUM);
			normalComments = commentService.queryPageProductCommentByTime(
					resultVo.getProductCode(), 2, 0, COMMENTNUM);
			goodComments = commentService.queryPageProductCommentByTime(
					resultVo.getProductCode(), 3, 0, COMMENTNUM);
		}
		
		commentInfo.setPoorComments(buildCommentVO(poorComments));
		commentInfo.setNormalComments(buildCommentVO(normalComments));
		commentInfo.setGoodComments(buildCommentVO(goodComments));
		// 计算评价数量和分数
		computeCommentNumber(resultVo.getProductCode(), commentInfo);
		resultVo.setCommentInfo(commentInfo);
	}

	/**
	 * @功能描述:计算价数量及分数
	 * @param poorComments
	 * @param normalComments
	 * @param goodComments
	 *            void
	 * @author yusf
	 * @throws Exception
	 */
	private void computeCommentNumber(int productCode, CommentInfoVO commentInfo)
			throws Exception {
		EbProductCommentService commentService = SystemInitialization
				.getApplicationContext().getBean(EbProductCommentService.class);
		Map<Integer, Integer> numberMap = commentService
				.getProductCommentsCount(productCode);
		int goodCommentNum = numberMap.containsKey(3) ? numberMap.get(3) : 0;
		int poorCommentNum = numberMap.containsKey(1) ? numberMap.get(1) : 0;
		int normalCommentnUm = numberMap.containsKey(2) ? numberMap.get(2) : 0;
		int totalCommentNUm = goodCommentNum + poorCommentNum
				+ normalCommentnUm;
		double goodCommentPercent = Math.round(goodCommentNum * 100.0
				/ totalCommentNUm);
		commentInfo.setCommentCount(totalCommentNUm);
		commentInfo.setGoodCommentCount(goodCommentNum);
		commentInfo.setGoodCommentPercent(goodCommentPercent);
		commentInfo.setNormalCommentCount(normalCommentnUm);
		commentInfo.setPoorCommentCount(poorCommentNum);
	}

	/**
	 * @功能描述:计算价数量及分数
	 * @param poorComments
	 * @param normalComments
	 * @param goodComments
	 *            void
	 * @author yusf
	 */
	private void computeCommentScoreAndNum(List<EbComment> poorComments,
			List<EbComment> normalComments, List<EbComment> goodComments,
			CommentInfoVO commentInfo) {
		int goodCommentNum = goodComments.size();
		int poorCommentNum = poorComments.size();
		int normalCommentnUm = normalComments.size();
		int totalCommentNUm = goodCommentNum + poorCommentNum
				+ normalCommentnUm;
		double goodCommentPercent = Math.round(goodCommentNum * 100.0
				/ totalCommentNUm);
		commentInfo.setCommentCount(totalCommentNUm);
		commentInfo.setGoodCommentCount(goodCommentNum);
		commentInfo.setGoodCommentPercent(goodCommentPercent);
		commentInfo.setNormalCommentCount(normalCommentnUm);
		commentInfo.setPoorCommentCount(poorCommentNum);
	}

	/**
	 * @功能描述:构建评价VO
	 * @param Comments
	 * @return CommentVO
	 * @author yusf
	 * @throws SqlException 
	 */
	private List<CommentVO> buildCommentVO(List<EbComment> Comments) throws SqlException {
		List<CommentVO> commentVos = new ArrayList<CommentVO>();
		if (Comments == null || Comments.size() == 0) {
			return commentVos;
		}
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		for (EbComment comment : Comments) {
			if (comment.getValid().getValue().intValue() == 0) {
				continue;
			}
			CommentVO commentVo = new CommentVO();
			commentVo.setColor(comment.getColor());
			commentVo.setComment(comment.getComment());
			commentVo.setCommentId(comment.getId());
			commentVo
					.setCommentImgs(buildCommentImgVo(comment.getCommentImgs()));
			commentVo.setHaveImg(comment.getHaveImg());
			commentVo.setProductCode(comment.getProductId());
			// 如果没有评分，默认5分好评
			commentVo.setScore(comment.getScore() == null ? 5 : comment.getScore());
			commentVo.setSize(comment.getSize());
			commentVo.setSkuCode(comment.getSkuCode());
			commentVo.setUserId(comment.getUserId());
			//由于有的数据没有userName或者第三方登录没有用户名，所以这里特殊处理名称，
			if(StringUtil.isNullOrEmpty(comment.getUserName())){
				int userId = comment.getUserId() == null? 0 : comment.getUserId();
				Customer cust = custServ.getCustomerById(userId);
				commentVo.setUserName(Util.obtainUserName(cust));
			}else{
				commentVo.setUserName(comment.getUserName());
			}
			commentVo.setCommentTime(DateFormatter.date2String(comment
					.getCommentTime(),"yyyy-MM-dd kk:mm:ss"));
			commentVos.add(commentVo);
		}
		return commentVos;
	}
	
	
	/**
	 * @功能描述:构建评价图片VO
	 * @return List<CommentImgVO>
	 * @author yusf
	 */
	private List<CommentImgVO> buildCommentImgVo(Set<EbCommentImg> commentImgs) {
		List<CommentImgVO> imgVos = new ArrayList<CommentImgVO>();
		if (commentImgs == null || commentImgs.size() <= 0) {
			return imgVos;
		}
		for (EbCommentImg commentImg : commentImgs) {
			if (commentImg.getStatus().getValue().intValue() == 0) {
				continue;
			}
			CommentImgVO imgVo = new CommentImgVO();
			imgVo.setCommentId(commentImg.getCommentId());
			imgVo.setId(commentImg.getId());
			imgVo.setImgHeight(commentImg.getImgHeight());
			// 由于网站评论走的切图服务，而手机端存到主机目录下，所以这里对网站数据做兼容
			if(StringUtil.isNotNullNotEmpty(commentImg.getImgSrc())
					&& commentImg.getImgSrc().startsWith("ximages/eb/comments")){
//				imgVo.setImgSrc(Util.getFullImageURL(commentImg.getImgSrc()));
				imgVo.setImgSrc(Util.getCommentImageURLByVersion(commentImg.getImgSrc(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			}else{
//				imgVo.setImgSrc(Util.getCommentImageURL(commentImg.getImgSrc()));
				imgVo.setImgSrc(Util.getCommentImageURLByVersion(commentImg
						.getImgSrc(), getContext().getHead().getVersion(),
						getContext().getHead().getPlatform()));
			}
			imgVo.setImgWidth(commentImg.getImgWidth());
			imgVo.setSortNum(commentImg.getSortNum());
			imgVos.add(imgVo);
		}
		return imgVos;
	}

	/**
	 * 构建正品保证图片VO列表
	 * 
	 * @param brandId
	 * @return
	 */
	private List<QualityGuaranteeImageVO> buildQualityGuaranteeImageVOs(
			int brandId) {
		EbBrandService ebBrandService = SystemInitialization
				.getApplicationContext().getBean(EbBrandService.class);
		List<QualityGuaranteeImageVO> guaranteeImageVOs = new ArrayList<QualityGuaranteeImageVO>();
		try {
			List<EbBrandImg> ebBrandImgs = ebBrandService
					.getQualityGuaranteeImages(brandId);
			if (ebBrandImgs != null && ebBrandImgs.size() > 0) {
				String imageServerHost = SystemManager.getInstance()
						.getSystemConfig().getImgServerUrl();
				for (EbBrandImg foo : ebBrandImgs) {
					QualityGuaranteeImageVO vo = new QualityGuaranteeImageVO(
							foo, imageServerHost);
					guaranteeImageVOs.add(vo);
				}
			}
		} catch (SqlException e) {
			e.printStackTrace();
		}
		return guaranteeImageVOs;
	}

	/**
	 * 构建正品保证文字VO列表
	 * 
	 * @param brandId
	 * @return
	 */
	private List<QualityGuaranteeTextVO> buildQualityGuaranteeTextVOs() {
		ProductServiceV5_0 productServiceV5_0 = SystemInitialization
				.getApplicationContext().getBean(ProductServiceV5_0.class);
		List<QualityGuaranteeTextVO> guaranteeTextVOs = new ArrayList<QualityGuaranteeTextVO>();
		List<BaseConfigData> baseConfigDatas;
		try {
			baseConfigDatas = productServiceV5_0.getQualityGuaranteeText();
			if (baseConfigDatas != null && baseConfigDatas.size() > 0) {
				for (BaseConfigData foo : baseConfigDatas) {
					QualityGuaranteeTextVO vo = new QualityGuaranteeTextVO(foo);
					guaranteeTextVOs.add(vo);
				}
			}
		} catch (SqlException e) {
			e.printStackTrace();
		}
		return guaranteeTextVOs;
	}

	/**
	 * @功能描述:构建商品促销VO
	 * @param prom
	 * @return List<ProductPromotionVO>
	 * @author yusf
	 */
	private List<ProductPromotionVO> getProductPromotionVO(EbPromotion prom) {
		List<ProductPromotionVO> promotList = new ArrayList<ProductPromotionVO>();
		Set<EbPromotionItem> itemSet = prom.getEbPromotionItems();
		for (EbPromotionItem ebPromotionItem : itemSet) {
			ProductPromotionVO prodPromVo = new ProductPromotionVO();
			prodPromVo.setPromotionId(prom.getPromotionId());
			prodPromVo.setPromotionItemId(ebPromotionItem.getPromotionItemId());
			prodPromVo.setPromotionItemName(ebPromotionItem.getItemName());
			prodPromVo.setUrl(prom.getUrl());
			prodPromVo.setSpecialId(prom.getEbSpecial() != null?prom.getEbSpecial().getId():0);
			promotList.add(prodPromVo);
		}
		return promotList;
	}

	private ProductReturnVO getProductAndSkusVO(EbProduct ebProduct)
			throws Exception {
		EbProductService eps = SystemInitialization.getApplicationContext()
				.getBean(EbProductService.class);
		// TODO 图片获取有问题
		List<EbProductImage> imgs = eps.getEbProductImagesByProductCode(
				MobileTypeEnum.iphone, ebProduct.getProductCode());//

		ProductReturnVO result = getProductReturnVo(ebProduct);
		// 设置描述图片和详情图片
		setProductImageVos(imgs, result);

		if (ebProduct != null && ebProduct.getSkus() != null
				&& ebProduct.getSkus().size() > 0) {
			List<SkuVO> skuVos = getSkuVO(ebProduct,result);
			result.setSkus(skuVos);
		}
		return result;
	}

	/**
	 * @功能描述:校验商品是否包含在商品集合中
	 * @param ebProductCollection
	 * @param ebProduct
	 * @return boolean
	 * @author yusf
	 */
	private boolean isInCollection(EbProductCollection ebProductCollection,
			EbProduct ebProduct) {
		if (ebProduct == null || ebProductCollection == null) {
			return false;
		}
		if (ebProduct == null || ebProductCollection == null) {
			return false;
		}
		if (isContaints(ebProductCollection.getBrandIds(),
				ebProduct.getEbBrand() == null ? "" : ebProduct.getEbBrand()
						.getBrandId().toString())) {// 品牌
			return true;
		}
		if (isContaints(ebProductCollection.getCategoryIds(),
				ebProduct.getEbCatagory() == null ? "" : ebProduct
						.getEbCatagory().getId().toString())) {// 分类
			return true;
		}
		if (isContaints(ebProductCollection.getProductCodes(), ebProduct
				.getProductCode().toString())) {// 商品
			return true;
		}
		return false;
	}

	private boolean isContaints(String str, String c) {
		if (StringUtils.isNotEmpty(str)) {
			String[] strArr = str.split(",");
			for (String s : strArr) {
				if (s.compareTo(c) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @功能描述:设置商品描述图片和详情图片 ,服务器端不处理尺寸大小，
	 * @param imgs
	 * @param result
	 *            void
	 * @author yusf
	 */
	private void setProductImageVos(List<EbProductImage> imgs,
			ProductReturnVO result) {
		// 描述图
		List<ProductImageVO> productImgVos = new ArrayList<ProductImageVO>();
		// 详情图
		List<ProductImageVO> productDetailImgVos = new ArrayList<ProductImageVO>();

		// if(MobileTypeEnum.valueOf(getContext().getHead().getPlatform())==
		// MobileTypeEnum.ipad){
		// for (EbProductImage ebProductImage : imgs) {
		// ebProductImage.setPlatform(MobileTypeEnum.ipad);
		// }
		// }
//		String imgHost = SystemManager.getInstance().getSystemConfig()
//				.getImgServerUrl();
		for (EbProductImage img : imgs) {
			if(img == null){
				continue;
			}
			ProductImageVO imgVo = new ProductImageVO();
//			imgVo.setImageSrc(Util.getFullImageURL(img.getImageSrc()));
			imgVo.setImageSrc(Util.getFullImageURLByVersion(img.getImageSrc(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			imgVo.setSortNum(img.getSortNum());
			imgVo.setImgHeight(img.getImgHeight());
			imgVo.setImgWidth(img.getImgWidth());
			imgVo.setType(img.getType().getValue());
			imgVo.setSkuCode(img.getSkuCode());
			// 商品描述图片
			if (img.getType().getValue().intValue() == 1) {
				imgVo.setSkuCode(img.getSkuCode());
				productImgVos.add(imgVo);
			} else if (img.getType().getValue().intValue() == 2) {// 商品详细图片
				productDetailImgVos.add(imgVo);
			}
		}
		result.setImgs(productImgVos);
		result.setDetailImgs(productDetailImgVos);
	}

	/**
	 * @功能描述: 构建商品信息返回VO
	 * @param ebProduct
	 * @param ebSecKill
	 * @return
	 * @throws Exception
	 *             ProductReturnVO
	 * @author yusf
	 */
	private ProductReturnVO getProductReturnVo(EbProduct ebProduct)
			throws Exception {
		ProductReturnVO productVO = new ProductReturnVO();
		if (ebProduct != null) {
			productVO = buildProductReturnVO(ebProduct);
			productVO.setProductType(EbProductTypeEnum.NORMAL.getValue());
			int productStorage = 0;
			for (EbSku ebSku : ebProduct.getSkus()) {
				productStorage += ebSku.getStorage().getAvailable();
			}
			productVO.setProductStorage(productStorage);
		}
		return productVO;
	}

	/**
	 * @功能描述:获取秒杀状态
	 * @param ebSecKill
	 * @param secKillVO
	 * @return int
	 * @author yusf
	 */
	private int getSecKillStatus(EbSecKill ebSecKill) {
		int status = 0;// 倒计时
		if (ebSecKill.getStartTime().before(new Date()))
			status = 1;// 在售中
		if (ebSecKill.getProductNum() <= 0)
			status = 2;// 售罄
		if (ebSecKill.getEndTime().before(new Date())) {
			status = 2;// 售罄
		}
		return status;
	}

	/**
	 * @功能描述:获取SkuVO列表
	 * @return List<SkuVO>
	 * @author yusf
	 */
	private List<SkuVO> getSkuVO(EbProduct ebProduct,ProductReturnVO result) {
		List<SkuVO> skuVos = new ArrayList<SkuVO>();
		SkuVO skuVo = null;
		for (EbSku ebSku : ebProduct.getSkus()) {
			if(ebSku.getStatus() == EbProductValidStatusEnum.INVALID){
				continue;
			}
			skuVo = new SkuVO();
			skuVo.setSkuCode(ebSku.getSkuCode());
			skuVo.setSIZE(ebSku.getSize() == null ? "" : ebSku.getSize());
			skuVo.setCOLOR(ebSku.getColor() == null ? "" : ebSku.getColor());
			skuVo.setStatus(ebSku.getStatus().getValue());
			skuVo.setStorageNum(ebSku.getStorage() == null ? 0 : ebSku
					.getStorage().getAvailable());
			//设置每个sku对应的描述图片
			String skuImage = getSkuImage(result.getImgs(), ebSku.getSkuCode());
//			skuVo.setSkuImage(Util.getFullImageURL(skuImage));
			skuVo.setSkuImage(Util.getFullImageURLByVersion(skuImage,getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			skuVos.add(skuVo);
		}
		return skuVos;
	}
	
	/**
	* <p>功能描述:从描述图里获取每个sku对应的图片</p>
	* <p>参数：@param imgs
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String getSkuImage(List<ProductImageVO> imgs,int skuCode){
		if(imgs == null || imgs.size() <= 0){
			return null;
		}
		for (ProductImageVO productImageVO : imgs) {
			if(productImageVO.getSkuCode() != null
					&&productImageVO.getSkuCode() == skuCode){
				return productImageVO.getImageSrc();
			}
		}
		return null;
	}
	
	/**
	 * @功能描述: 获取秒杀VO
	 * @param ebSecKill
	 * @param productVO
	 * @return List<SecKillVO>
	 * @author yusf
	 */
	private List<SecKillVO> getSecKillVos(EbSecKill ebSecKill) {
		List<SecKillVO> secKillVos = new ArrayList<SecKillVO>();
		SecKillVO secKill = new SecKillVO();
		if (getContext().getHead().getPlatform().equals("ipad")) {
			secKill.setImageSrc(SystemManager.getInstance().getSystemConfig()
					.getImgServerUrl()
					+ ebSecKill.getImageSrcPad());
		} else {
			secKill.setImageSrc(SystemManager.getInstance().getSystemConfig()
					.getImgServerUrl()
					+ ebSecKill.getImageSrcPhone());
		}
		secKill.setSecKillPrice(df.format(ebSecKill.getPrice()));
		secKill.setPrice(df.format(ebSecKill.getProduct().getPrice()));
		secKill.setSeckillId(ebSecKill.getId());
		secKill.setProductCode(ebSecKill.getProduct().getProductCode());
		secKill.setStartTime(DateTimeFormatter.dateTime2String(ebSecKill
				.getStartTime()));
		secKill.setEndTime(DateTimeFormatter.dateTime2String(ebSecKill
				.getEndTime()));
		secKill.setProductNum(ebSecKill.getProductNum());
		secKill.setProductName(ebSecKill.getProduct().getProductName());
		int status = getSecKillStatus(ebSecKill);
		secKill.setStatus(status);
		if (status == 0) {
			// TODO 临时使用，只要status==0，则显示剩余100件
			secKill.setProductNum(100);
		} else if (status == 2) {
			secKill.setProductNum(0);
		}

		secKillVos.add(secKill);
		return secKillVos;
	}

	/**
	 * @功能描述:获取商品返回VO
	 * @param ebProduct
	 * @return ProductReturnVO
	 * @author yusf
	 * @throws SqlException
	 */
	private ProductReturnVO buildProductReturnVO(EbProduct ebProduct)
			throws SqlException {
		ProductReturnVO prodVo = new ProductReturnVO();
		prodVo.setProductCode(ebProduct.getProductCode());
		prodVo.setProductName(ebProduct.getProductName());
		prodVo.setDescription(ebProduct.getProductDescription());
		prodVo.setPrice(ebProduct.getPrice());
		prodVo.setShipping(ebProduct.getShipping());
		prodVo.setVprice(ebProduct.getVprice());
		prodVo.setSvprice(ebProduct.getSvprice());
		prodVo.setProductStorage(0);
		prodVo.setStatus(ebProduct.getStatus().getValue());
		prodVo.setCreditPercentage(ebProduct.getCreditPercentage());
		prodVo.setOnShelfTIme(DateFormatter.date2String(ebProduct
				.getOnShelfTIme()));
		prodVo.setCategoryName(ebProduct.getEbCatagory() == null ? ""
				: ebProduct.getEbCatagory().getCname());
		prodVo.setProducer(ebProduct.getEbVendor() == null ? "" : ebProduct
				.getEbVendor().getVendorName());
		prodVo.setVendorProductCode(ebProduct.getVendorProductCode());
		// setProductParam(ebProduct, prodVo);
		return prodVo;
	}

	/**
	 * 玩具首页
	 */
	class ProductHomePageVO {
		// 动漫推荐
		private List<RecommendVO> recommends;
		// 二级海报：2个
		private List<PosterVO> seconedPosters;
		// 三级海报：4个
		private List<PosterVO> thirdPosters;
		// 海报横幅：1个
		private PosterVO banner;
		// 热卖玩具
		private List<EbProductVO> products;
		// 导航栏
		private List<PosterVO> navigations;
		//商品热搜词
		private List<String> hotKeys;
		//ipad多个横幅
		private List<PosterVO> banners;
		//商品热搜词
		private List<String> productHotKeys;
		//知识热搜词
		private List<String> knowledgeHotKeys;
		//推荐热搜词
		private List<String> recommendHotKeys;
		//视频热搜词
		private List<String> albumHotKeys;
		//专区名称
		private String zoneName;
		//专区跳转URL
		private String zoneRedirectUrl;
		
		public String getZoneName() {
			return zoneName;
		}

		public void setZoneName(String zoneName) {
			this.zoneName = zoneName;
		}

		public String getZoneRedirectUrl() {
			return zoneRedirectUrl;
		}

		public void setZoneRedirectUrl(String zoneRedirectUrl) {
			this.zoneRedirectUrl = zoneRedirectUrl;
		}

		public List<String> getProductHotKeys() {
			return productHotKeys;
		}

		public void setProductHotKeys(List<String> productHotKeys) {
			this.productHotKeys = productHotKeys;
		}

		public List<String> getKnowledgeHotKeys() {
			return knowledgeHotKeys;
		}

		public void setKnowledgeHotKeys(List<String> knowledgeHotKeys) {
			this.knowledgeHotKeys = knowledgeHotKeys;
		}

		public List<String> getRecommendHotKeys() {
			return recommendHotKeys;
		}

		public void setRecommendHotKeys(List<String> recommendHotKeys) {
			this.recommendHotKeys = recommendHotKeys;
		}

		public List<String> getAlbumHotKeys() {
			return albumHotKeys;
		}

		public void setAlbumHotKeys(List<String> albumHotKeys) {
			this.albumHotKeys = albumHotKeys;
		}

		public List<PosterVO> getBanners() {
			return banners;
		}

		public void setBanners(List<PosterVO> banners) {
			this.banners = banners;
		}

		public List<String> getHotKeys() {
			return hotKeys;
		}

		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}

		public List<PosterVO> getNavigations() {
			return navigations;
		}

		public void setNavigations(List<PosterVO> navigations) {
			this.navigations = navigations;
		}

		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}

		public List<RecommendVO> getRecommends() {
			return recommends;
		}

		public void setRecommends(List<RecommendVO> recommends) {
			this.recommends = recommends;
		}

		public List<PosterVO> getSeconedPosters() {
			return seconedPosters;
		}

		public void setSeconedPosters(List<PosterVO> seconedPosters) {
			this.seconedPosters = seconedPosters;
		}

		public List<PosterVO> getThirdPosters() {
			return thirdPosters;
		}

		public void setThirdPosters(List<PosterVO> thirdPosters) {
			this.thirdPosters = thirdPosters;
		}

		public PosterVO getBanner() {
			return banner;
		}

		public void setBanner(PosterVO banner) {
			this.banner = banner;
		}

	}

	class ProductInfoVO {

		List<EbProductVO> hotProduct;
		
		private List<EbProductVO> products;
		
		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}

		public List<EbProductVO> getHotProduct() {
			return hotProduct;
		}

		public void setHotProduct(List<EbProductVO> hotProduct) {
			this.hotProduct = hotProduct;
		}

	}

	class ProductsVO {
		List<EbProductVO> products;

		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}

	}

	/**
	 * 商品VO
	 */
	class ProductReturnVO {
		// 商品描述图片
		private List<ProductImageVO> imgs;
		// 商品详情图片
		private List<ProductImageVO> detailImgs;
		// 商品下的所有单品
		private List<SkuVO> skus;
		// 满减促销活动列表
		private List<ProductPromotionVO> reducePromotion;
		// 满返促销列表
		private List<ProductPromotionVO> couponPromotion;
		// 满赠促销列表
		private List<ProductPromotionVO> giftPromotion;
		// 评论内容
		private CommentInfoVO commentInfo;
		// 活动ID
		private Integer activityId;
		// 活动名称
		private String activityName;
		// 当前时间毫秒数
		private long currentTime;
		// 分享URL
		private String shareUrl;
		// 商品编码
		private Integer productCode;
		// 商品名称
		private String productName;
		// 商品描述
		private String description;
		// 市场价格
		private double price;
		// 邮费
		private double shipping;
		// 会员价格
		private double svprice;
		// 爱看价格
		private double vprice;
		// 商品总上架数量
		private Integer productStorage;
		// 商品类型
		private Integer productType;
		// 状态
		private int status;
		// 积分百分比
		private Integer creditPercentage;
		// 爱看点评
		private String iKanComment;
		// 商品上架时间
		private String onShelfTIme;
		// 适合年龄
		private String forAge;
		// 厂商编号
		private String vendorProductCode;
		// 生产厂商
		private String producer;
		// 分类名称
		private String categoryName;
		// 毛重
		private String netWeight;
		// 包装尺寸
		private String packageSize;
		// 产地
		private String producingArea = "爱看";
		// 评测
		private TopicVO topic;
		// 正品保证图片
		private List<QualityGuaranteeImageVO> qualityGuaranteeImageVOs;
		// 正品保证图片
		private List<QualityGuaranteeTextVO> qualityGuaranteeTextVOs;
		// 商品属性
		private List<ParamVO> productParams;
		// 商品拓展属性
		private List<ParamVO> extendParams;
		//移动端网站下载提示
		private String downloadInfo;
		
		//视频收藏标识
		private boolean  collectionStatus;
		
		public boolean isCollectionStatus() {
			return collectionStatus;
		}

		public void setCollectionStatus(boolean collectionStatus) {
			this.collectionStatus = collectionStatus;
		}

		public String getDownloadInfo() {
			return downloadInfo;
		}

		public void setDownloadInfo(String downloadInfo) {
			this.downloadInfo = downloadInfo;
		}

		public List<ParamVO> getProductParams() {
			return productParams;
		}

		public void setProductParams(List<ParamVO> productParams) {
			this.productParams = productParams;
		}

		public List<ParamVO> getExtendParams() {
			return extendParams;
		}

		public void setExtendParams(List<ParamVO> extendParams) {
			this.extendParams = extendParams;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public double getShipping() {
			return shipping;
		}

		public void setShipping(double shipping) {
			this.shipping = shipping;
		}

		public double getSvprice() {
			return svprice;
		}

		public void setSvprice(double svprice) {
			this.svprice = svprice;
		}

		public double getVprice() {
			return vprice;
		}

		public void setVprice(double vprice) {
			this.vprice = vprice;
		}

		public TopicVO getTopic() {
			return topic;
		}

		public void setTopic(TopicVO topic) {
			this.topic = topic;
		}

		public String getProducingArea() {
			return producingArea;
		}

		public void setProducingArea(String producingArea) {
			this.producingArea = producingArea;
		}

		public List<ProductImageVO> getDetailImgs() {
			return detailImgs;
		}

		public void setDetailImgs(List<ProductImageVO> detailImgs) {
			this.detailImgs = detailImgs;
		}

		public String getOnShelfTIme() {
			return onShelfTIme;
		}

		public void setOnShelfTIme(String onShelfTIme) {
			this.onShelfTIme = onShelfTIme;
		}

		public String getForAge() {
			return forAge;
		}

		public void setForAge(String forAge) {
			this.forAge = forAge;
		}

		public String getVendorProductCode() {
			return vendorProductCode;
		}

		public void setVendorProductCode(String vendorProductCode) {
			this.vendorProductCode = vendorProductCode;
		}

		public String getProducer() {
			return producer;
		}

		public void setProducer(String producer) {
			this.producer = producer;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public String getNetWeight() {
			return netWeight;
		}

		public void setNetWeight(String netWeight) {
			this.netWeight = netWeight;
		}

		public String getPackageSize() {
			return packageSize;
		}

		public void setPackageSize(String packageSize) {
			this.packageSize = packageSize;
		}

		public List<ProductPromotionVO> getReducePromotion() {
			return reducePromotion;
		}

		public void setReducePromotion(List<ProductPromotionVO> reducePromotion) {
			this.reducePromotion = reducePromotion;
		}

		public List<ProductPromotionVO> getCouponPromotion() {
			return couponPromotion;
		}

		public void setCouponPromotion(List<ProductPromotionVO> couponPromotion) {
			this.couponPromotion = couponPromotion;
		}

		public List<ProductPromotionVO> getGiftPromotion() {
			return giftPromotion;
		}

		public void setGiftPromotion(List<ProductPromotionVO> giftPromotion) {
			this.giftPromotion = giftPromotion;
		}

		public String getiKanComment() {
			return iKanComment;
		}

		public void setiKanComment(String iKanComment) {
			this.iKanComment = iKanComment;
		}

		public List<ProductImageVO> getImgs() {
			return imgs;
		}

		public void setImgs(List<ProductImageVO> imgs) {
			this.imgs = imgs;
		}

		public List<SkuVO> getSkus() {
			return skus;
		}

		public void setSkus(List<SkuVO> skus) {
			this.skus = skus;
		}

		public Integer getActivityId() {
			return activityId;
		}

		public void setActivityId(Integer activityId) {
			this.activityId = activityId;
		}

		public String getActivityName() {
			return activityName;
		}

		public void setActivityName(String activityName) {
			this.activityName = activityName;
		}

		public long getCurrentTime() {
			return currentTime;
		}

		public void setCurrentTime(long currentTime) {
			this.currentTime = currentTime;
		}

		public String getShareUrl() {
			return shareUrl;
		}

		public void setShareUrl(String shareUrl) {
			this.shareUrl = shareUrl;
		}

		public Integer getProductCode() {
			return productCode;
		}

		public void setProductCode(Integer productCode) {
			this.productCode = productCode;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Integer getProductStorage() {
			return productStorage;
		}

		public void setProductStorage(Integer productStorage) {
			this.productStorage = productStorage;
		}

		public Integer getProductType() {
			return productType;
		}

		public void setProductType(Integer productType) {
			this.productType = productType;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public Integer getCreditPercentage() {
			return creditPercentage;
		}

		public void setCreditPercentage(Integer creditPercentage) {
			this.creditPercentage = creditPercentage;
		}

		public CommentInfoVO getCommentInfo() {
			return commentInfo;
		}

		public void setCommentInfo(CommentInfoVO commentInfo) {
			this.commentInfo = commentInfo;
		}

		public List<QualityGuaranteeImageVO> getQualityGuaranteeImageVOs() {
			return qualityGuaranteeImageVOs;
		}

		public void setQualityGuaranteeImageVOs(
				List<QualityGuaranteeImageVO> qualityGuaranteeImageVOs) {
			this.qualityGuaranteeImageVOs = qualityGuaranteeImageVOs;
		}

		public List<QualityGuaranteeTextVO> getQualityGuaranteeTextVOs() {
			return qualityGuaranteeTextVOs;
		}

		public void setQualityGuaranteeTextVOs(
				List<QualityGuaranteeTextVO> qualityGuaranteeTextVOs) {
			this.qualityGuaranteeTextVOs = qualityGuaranteeTextVOs;
		}

	}

	class QualityGuaranteeImageVO {
		private String imgSrc;
		private int status;
		private int sortNum;
		private int imgWidth;
		private int imgHeight;

		public String getImgSrc() {
			return imgSrc;
		}

		public void setImgSrc(String imgSrc) {
			this.imgSrc = imgSrc;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public int getSortNum() {
			return sortNum;
		}

		public void setSortNum(int sortNum) {
			this.sortNum = sortNum;
		}

		public int getImgWidth() {
			return imgWidth;
		}

		public void setImgWidth(int imgWidth) {
			this.imgWidth = imgWidth;
		}

		public int getImgHeight() {
			return imgHeight;
		}

		public void setImgHeight(int imgHeight) {
			this.imgHeight = imgHeight;
		}

		public QualityGuaranteeImageVO() {
		}

		public QualityGuaranteeImageVO(EbBrandImg ebBrandImg,
				String imageServerHost) {
			super();
//			this.imgSrc = Util.getFullImageURL(ebBrandImg.getImgSrc());
			this.imgSrc = Util.getFullImageURLByVersion(ebBrandImg.getImgSrc(),getContext().getHead().getVersion(),getContext().getHead().getPlatform());
			this.status = ebBrandImg.getStatus().getValue().intValue();
			this.sortNum = ebBrandImg.getSortNum().intValue();
			this.imgWidth = ebBrandImg.getImgWidth() == null ? 0 : ebBrandImg
					.getImgWidth().intValue();
			this.imgHeight = ebBrandImg.getImgHeight() == null ? 0 : ebBrandImg
					.getImgHeight().intValue();
		}

	}

	class QualityGuaranteeTextVO {
		private String title;
		private String content;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public QualityGuaranteeTextVO(BaseConfigData data) {
			this.title = data.getAttrname();
			this.content = data.getAttrvalue();
		}

		public QualityGuaranteeTextVO() {
			super();
		}

	}

	/**
	 * 商品属性VO
	 */
	class ParamVO {
		// 属性名称
		private String paramName;
		// 属性值
		private String paraValue;

		public ParamVO() {

		}

		public ParamVO(EbProductParam param) {
			this.paramName = param.getKeyName();
			this.paraValue = param.getValueString();
		}

		public String getParamName() {
			return paramName;
		}

		public void setParamName(String paramName) {
			this.paramName = paramName;
		}

		public String getParaValue() {
			return paraValue;
		}

		public void setParaValue(String paraValue) {
			this.paraValue = paraValue;
		}

	}
}
