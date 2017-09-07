package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.AlbumCategory;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.EbBrand;
import com.ytsp.db.domain.EbCatagory;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProps;
import com.ytsp.db.domain.Tag;
import com.ytsp.db.domain.TagGroup;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.EbPropsEnum;
import com.ytsp.db.enums.EbRootCategoryEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.SearchOrderByEnum;
import com.ytsp.db.enums.TagGroupConstraintEnum;
import com.ytsp.db.enums.TagStatusEnum;
import com.ytsp.db.enums.TagUseTypeEnum;
import com.ytsp.db.enums.UpLowStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.AlbumCategoryVO;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.BabyVO;
import com.ytsp.db.vo.BrandVO;
import com.ytsp.db.vo.EbProductVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.BaseConfigService;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.SearchService;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.service.v5_0.EbBrandService;
import com.ytsp.entrance.service.v5_0.EbCatagoryService;
import com.ytsp.entrance.service.v5_0.EbProductPropsService;
import com.ytsp.entrance.service.v5_0.ProductServiceV5_0;
import com.ytsp.entrance.service.v5_0.TagGroupService;
import com.ytsp.entrance.service.v5_0.TagService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.MixSearchResultVO;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.SearchVO;
import com.ytsp.entrance.util.Util;

public class SearchCommand extends AbstractCommand {
	private static final String[] priceRange = new String[]{"0~50","50~100","150~500","500~1000","1000以上"};
	//商品综合排序
	private static final String[] compositiveSort = new String[]{"价格最高","价格最低","最新上架","评价最佳","销量最多","折扣最高"};
	//动漫综合排序
	private static final String[] animeCompositiveSort = new String[]{"综合","热播","最新"};
	
	private static final String PRICE = "价格";
	
	private static final String BRAND = "品牌";
	
	private static final String ANIMEPRODUCT = "动漫周边"; 
	
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_SEARCH_PRODUCTCATEGORY
				|| code == CommandList.CMD_CATEGORY_FIRST
				|| code == CommandList.CMD_SEARCH_ANIMECATEGORY
				|| code == CommandList.CMD_SEARCH_KNOWLEDGEHOME
				|| code == CommandList.CMD_SEARCH_ALBUMBYCATEGORY
				|| code == CommandList.CMD_SEARCH_BYBRANDID
				|| code == CommandList.CMD_SEARCH_PRODUCT
				|| code == CommandList.CMD_SEARCH_ANIME
				|| code == CommandList.CMD_SEARCH_KNOWLEDGE
				|| code == CommandList.CMD_SEARCH_PRODUCT_WITHCONDITION
				|| code == CommandList.CMD_SEARCH_ANIME_WITHCONDITION
				|| code == CommandList.CMD_SEARCH_KNOWLEDGE_WITHCONDITION
				|| code == CommandList.CMD_SEARCH_MIX
				|| code == CommandList.CMD_SEARCH_MIX_BY_CATEGORY);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_SEARCH_PRODUCTCATEGORY) {
				return getProductCatagorys();
			}else if(code == CommandList.CMD_CATEGORY_FIRST){
				return getProductFirstCategory();
			}else if(code == CommandList.CMD_SEARCH_ANIMECATEGORY){
				return getAnimeCategorys();
			}else if(code == CommandList.CMD_SEARCH_KNOWLEDGEHOME){
				return getKnowledgeCategorysSearch();
			}else if(code == CommandList.CMD_SEARCH_ALBUMBYCATEGORY){
				return searchAlbumByCategory();
			}else if(code == CommandList.CMD_SEARCH_BYBRANDID){
				return getProductByBrandId();
			}else if(code == CommandList.CMD_SEARCH_PRODUCT){
				return searchProduct();
			}else if(code == CommandList.CMD_SEARCH_ANIME){
				return searchAnime();
			}else if(code == CommandList.CMD_SEARCH_KNOWLEDGE){
				return searchKnowledge();
			}else if(code == CommandList.CMD_SEARCH_PRODUCT_WITHCONDITION){
				return searchProductWithCondition();
			}else if(code == CommandList.CMD_SEARCH_ANIME_WITHCONDITION){
				return searchAnimeWithCondtion();
			}else if(code == CommandList.CMD_SEARCH_KNOWLEDGE_WITHCONDITION){
				return searchKnowledgeWithCondtion();
			}else if(code == CommandList.CMD_SEARCH_MIX){
				return searchAllMix();
			}else if(code == CommandList.CMD_SEARCH_MIX_BY_CATEGORY){
				return searchAllMixByCategory();
			}
			
		} catch (Exception e) {
			logger.info("CatagoryCommand:" + code + " 失败 " + ",headInfo:"
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
	private ExecuteResult searchAllMixByCategory() throws Exception{
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		// 搜索词
		String searchKey = "";

		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		// 设置版本和平台
		searchVO.setPlatform(getContext().getHead().getPlatform());
		searchVO.setVersion(getContext().getHead().getVersion());
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		//搜索商品
		List<EbProductVO> products = searchServ.searchProduct(searchVO,
				new HashMap<String, String>(), getContext().getHead()
						.getVersion(), getContext().getHead().getPlatform());
		//搜过动漫
		searchVO.setSpecialType(0);
		List<AlbumVO> albums = searchServ.searchAlbum(searchVO,
				new HashMap<String, String>(), getContext().getHead()
						.getVersion(), getContext().getHead().getPlatform());
		//搜过知识
		searchVO.setSpecialType(1);
		List<AlbumVO> knowledgeVideo = searchServ.searchAlbum(searchVO,
				new HashMap<String, String>(), getContext().getHead()
						.getVersion(), getContext().getHead().getPlatform());
		
		MixSearchResultInfo info = new MixSearchResultInfo();
		info.setProducts(products);
		info.setAlbums(albums);
		info.setKnowledgeVideos(knowledgeVideo);
		//视频的总数量
		info.setAlbumCount((albums != null && albums.size() > 0) ? albums.get(0).getTotalNumber() : 0);
		//玩具的总数量
		info.setProductCount((products != null && products.size() > 0) ? products.get(0).getTotalNumber() : 0);
		//知识的视频的总数量
		info.setKnowledgeCount((knowledgeVideo != null && knowledgeVideo.size() > 0) ? knowledgeVideo.get(0).getTotalNumber() : 0);
		//设置热门搜索词
		info.setHotKeys(getHotSearchKeys(IConstants.CONFIG_RECOMMEND_SK));
		// 获取所有搜索热词
		Map<String, List<String>> allHotKeys = getAllHotSearchKeys();
		List<String> recommendHotKeys = allHotKeys
				.get(IConstants.CONFIG_RECOMMEND_SK);
		// 设置动漫热搜词
		// 由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
		info.setHotKeys(recommendHotKeys);
		info.setRecommendHotKeys(recommendHotKeys);
		info.setAlbumHotKeys(allHotKeys.get(IConstants.CONFIG_ALBUM_SK));
		info.setProductHotKeys(allHotKeys.get(IConstants.CONFIG_PRODUCT_SK));
		info.setKnowledgeHotKeys(allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK));
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(info));
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), info);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索全部成功",
				result, this);
	}
	
	/**
	* <p>功能描述:混合搜索全部</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult searchAllMix() throws Exception{
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		// 搜索词
		String searchKey = "";

		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		// 设置版本和平台
		searchVO.setPlatform(getContext().getHead().getPlatform());
		searchVO.setVersion(getContext().getHead().getVersion());
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		List<MixSearchResultVO> searchResult = searchServ.mixSearch(searchVO,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());

		MixSearchResultInfo info = new MixSearchResultInfo();
		info.setSearchResults(searchResult);
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(info));
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), info);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索全部成功",
				result, this);
	}
	
	
	/**
	 * 商品搜索入参：界面所有搜索的信息：searchKey,page,pageSize,sortVos综合排序,ageRange年龄，catagorys分类，filterInfo筛选条件
	 * 返回带有综合排序、年龄等条件的数据
	* <p>功能描述:玩具的搜索功能,返回结果带有搜索条件数据</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchProductWithCondition() throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		int categoryId = 0;
		// 搜索词
		String searchKey = "";
		// 综合排序id
		Integer sortId = null;
		// 品牌id
		Integer brandId = null;

		// 获取前台传过来的数据
		if (!reqBody.isNull("categoryId")) {
			categoryId = reqBody.optInt("categoryId");
			searchVO.setCatg(String.valueOf(categoryId));
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		if (!reqBody.isNull("sortId")) {
			sortId = reqBody.optInt("sortId");
		}
		// 品牌搜索条件获取
		if (!reqBody.isNull("filterInfo")
				&& !((JSONObject) reqBody.get("filterInfo")).isNull("brandId")) {
			brandId = ((JSONObject) reqBody.get("filterInfo"))
					.optInt("brandId");
		}

		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);
		// 设置搜索综合排序
		setSearchOrder(searchVO, sortId);
		// 设置品牌id搜索条件
		searchVO.setBrand(brandId == null ? null : String.valueOf(brandId));
		// 获取界面的筛选条件
		Map<String, String> criteriaList = bulidSearchFilterData(reqBody,searchVO);
		// 设置年龄搜索条件
		setAgeRangeSearchCondition(criteriaList, reqBody);

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		// 调用搜索服务搜索商品
		List<EbProductVO> products = searchServ.searchProduct(searchVO,
				criteriaList, getContext().getHead().getVersion(), getContext()
						.getHead().getPlatform());

		// 获取商品搜索条件内容
		CatagoryInfoVO catagoryInfoVO = getSearchProductCondition();
		catagoryInfoVO.setProducts(products);
		if(brandId != null && brandId > 0){
			catagoryInfoVO.getFilterInfo().getBrandInfo().setSelectBrandId(brandId);
		}
//		//设置热门搜索词
//		catagoryInfoVO.setHotKeys(getHotSearchKeys(IConstants.CONFIG_PRODUCT_SK));
		// 设置所有热搜词
		Map<String, List<String>> allHotKeys = getAllHotSearchKeys();
		List<String> productHotKeys = allHotKeys
				.get(IConstants.CONFIG_PRODUCT_SK);
		// 设置动漫热搜词
		// 由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
		catagoryInfoVO.setHotKeys(productHotKeys);
		catagoryInfoVO.setKnowledgeHotKeys(allHotKeys
				.get(IConstants.CONFIG_KNOWLEDGE_SK));
		catagoryInfoVO.setProductHotKeys(productHotKeys);
		catagoryInfoVO.setAlbumHotKeys(allHotKeys
				.get(IConstants.CONFIG_ALBUM_SK));
		catagoryInfoVO.setRecommendHotKeys(allHotKeys
				.get(IConstants.CONFIG_RECOMMEND_SK));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(catagoryInfoVO));
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), catagoryInfoVO);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索商品成功",
				result, this);
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
	* <p>功能描述:获取商品搜索页的搜索条件内容</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CatagoryInfoVO</p>
	 */
	private CatagoryInfoVO getSearchProductCondition() throws SqlException{
		// 所有分类信息返回VO
		CatagoryInfoVO catagoryVo = new CatagoryInfoVO();
		//综合排序
		catagoryVo.setSortVos(getCompostiveSort(compositiveSort));
		// 设置一级二级分类
		setFirstAndSecondCatagorys(catagoryVo,0);
		//筛选VO
		FilterInfoVO filterVO = buildFilterVo();
		catagoryVo.setFilterInfo(filterVO);
		//设置年龄
		catagoryVo.setAgeRange(getAgeRange());
		
		return catagoryVo;
	}
	
	
	/**
	 * 入参：界面所有搜索的信息：searchKey,page,pageSize,sortId综合排序,ageRange年龄，catagoryId分类
	 * 返回带有综合排序、年龄等条件的数据
	* <p>功能描述:知识搜索功能，返回结果带有搜索条件数据</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchKnowledgeWithCondtion() throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		int categoryId = 0;
		// 搜索词
		String searchKey = "";
		// 综合排序id
		Integer sortId = null;

		// 获取前台传过来的数据
		if (!reqBody.isNull("categoryId")) {
			categoryId = reqBody.optInt("categoryId");
			// 设置分类
			searchVO.setCatg(String.valueOf(categoryId));
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		if (!reqBody.isNull("sortId")) {
			sortId = reqBody.optInt("sortId");
		}

		// 设置版本和平台
		searchVO.setPlatform(getContext().getHead().getPlatform());
		searchVO.setVersion(getContext().getHead().getVersion());
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);
		// 设置搜索综合排序
		setAnimeSearchOrder(searchVO, sortId);
		// 筛选条件Map
		Map<String, String> criteriaList = new HashMap<String, String>();
		// 设置年龄搜索条件
		setAgeRangeSearchCondition(criteriaList, reqBody);
		// 设置是否上架属性
		// setAlbumUplow(searchVO);
		// 设置视频类型为知识视频
		searchVO.setSpecialType(AlbumCategoryTypeEnum.KNOWLEDGE.getValue());

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		// 调用搜索服务搜索知识
		List<AlbumVO> albumVOs = searchServ.searchAlbum(searchVO, criteriaList,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());
		// 构建知识搜索条件信息
		KnowledgeSearchVO condition = getSearchKnowledgeCondition();
		// 设置搜索的知识视频
		condition.setKnowledgeVideos(albumVOs);
		//设置热门搜索词
		condition.setHotKeys(getHotSearchKeys(IConstants.CONFIG_KNOWLEDGE_SK));
		//7.设置所有搜索热词
		Map<String,List<String>> allHotKeys = getAllHotSearchKeys();
		List<String> knowledgeHotKeys = allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK);
		//设置动漫热搜词
		//由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
		condition.setHotKeys(knowledgeHotKeys);
		condition.setKnowledgeHotKeys(knowledgeHotKeys);
		condition.setProductHotKeys(allHotKeys.get(IConstants.CONFIG_PRODUCT_SK));
		condition.setAlbumHotKeys(allHotKeys.get(IConstants.CONFIG_ALBUM_SK));
		condition.setRecommendHotKeys(allHotKeys.get(IConstants.CONFIG_RECOMMEND_SK));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(condition));
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), condition);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索知识成功",
				result, this);
	}
	
	/**
	* <p>功能描述:获取知识搜索界面的搜索条件内容</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：KnowledgeSearchVO</p>
	 */
	private KnowledgeSearchVO getSearchKnowledgeCondition() throws SqlException{
		KnowledgeSearchVO knowledgeSearch = new KnowledgeSearchVO();
		//1  获取知识分类
		knowledgeSearch.setCategorys(getAlbumCategoryVOs(2, AlbumCategoryTypeEnum.KNOWLEDGE.getValue(),0));
		//2 综合排序
		knowledgeSearch.setCompositiveSort(getCompostiveSort(animeCompositiveSort));
		//3.设置年龄段
		TagService tagServ = SystemInitialization.getApplicationContext().getBean(TagService.class);
		List<Tag> tags = tagServ.getTagsByGroupId(IConstants.ANIMEAGEGROUPID,TagUseTypeEnum.ALBUM.getValue());
		Map<Integer,TagInfoVO> tagInfoMap = buildTagInfo(tags,0);
		knowledgeSearch.setAgeRange(tagInfoMap.get(IConstants.ANIMEAGEGROUPID));
		
		return knowledgeSearch;
	}
	
	/**
	 * 入参：界面所有搜索的信息：searchKey,page,pageSize,sortId综合排序,ageRange年龄，catagoryId分类，filterInfo筛选条件
	 * 返回带有综合排序、年龄等条件的数据
	* <p>功能描述:动漫搜索功能,返回结果带有搜索条件数据</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchAnimeWithCondtion() throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		int categoryId = 0;
		// 搜索词
		String searchKey = "";
		// 综合排序id
		Integer sortId = null;
		// 若有标题将标题名传过来
		String title = null;
		// 获取前台传过来的数据
		if (!reqBody.isNull("categoryId")) {
			categoryId = reqBody.optInt("categoryId");
			// 设置动漫分类
			searchVO.setCatg(String.valueOf(categoryId));
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		if (!reqBody.isNull("sortId")) {
			sortId = reqBody.optInt("sortId");
		}
		if (!reqBody.isNull("title")) {
			title = reqBody.optString("title");
		}

		// 设置版本和平台
		searchVO.setPlatform(getContext().getHead().getPlatform());
		searchVO.setVersion(getContext().getHead().getVersion());
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);
		// 设置搜索综合排序
		setAnimeSearchOrder(searchVO, sortId);
		// 获取界面的筛选条件
		Map<String, String> criteriaList = bulidSearchFilterData(reqBody,searchVO);
		// 设置年龄搜索条件
		setAgeRangeSearchCondition(criteriaList, reqBody);
		// 设置是否上架属性
		// setAlbumUplow(searchVO);
		// 设置视频类型为动漫视频
		searchVO.setSpecialType(AlbumCategoryTypeEnum.VIDEO.getValue());

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		// 调用搜索服务搜索动漫
		List<AlbumVO> albumVOs = searchServ.searchAlbum(searchVO, criteriaList,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());
		// 获取动漫搜索界面条件：综合排序，年龄，分类，筛选
		SearchConditionInfoVO animeSearchInfo = getAnimeSearchCondition();
		// 设置搜索的动漫
		animeSearchInfo.setAlbums(albumVOs);
		//获取所有搜索热词
		Map<String,List<String>> allHotKeys = getAllHotSearchKeys();
		List<String> albumHotKeys = allHotKeys.get(IConstants.CONFIG_ALBUM_SK);
		//设置动漫热搜词
		//由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
		animeSearchInfo.setHotKeys(albumHotKeys);
		animeSearchInfo.setAlbumHotKeys(albumHotKeys);
		animeSearchInfo.setProductHotKeys(allHotKeys.get(IConstants.CONFIG_PRODUCT_SK));
		animeSearchInfo.setKnowledgeHotKeys(allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK));
		animeSearchInfo.setRecommendHotKeys(allHotKeys.get(IConstants.CONFIG_RECOMMEND_SK));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(animeSearchInfo));
		if (sortId != null) {
			result.put("sortId", sortId);
		}
		if (title != null) {
			result.put("title", title);
		}
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), animeSearchInfo);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索动漫成功",
				result, this);
	}
	
	/**
	* <p>功能描述:获取动漫的搜索条件内容</p>
	* <p>参数：@return</p>
	* <p>返回类型：SearchConditionInfoVO</p>
	 * @throws SqlException 
	 */
	private SearchConditionInfoVO getAnimeSearchCondition() throws SqlException{
		//动漫搜索界面返回数据VO
		SearchConditionInfoVO searchCondition = new SearchConditionInfoVO();
		//1.设置综合排序
		searchCondition.setSortVos(getCompostiveSort(animeCompositiveSort));
		//2.处理动漫分类
		searchCondition.setCategorys(getAlbumCategoryVOs(1,AlbumCategoryTypeEnum.VIDEO.getValue(),0));
		//3.获取视频的所有taggroup里的标签
		TagService tagServ = SystemInitialization.getApplicationContext().getBean(TagService.class);
		List<Tag> tags = tagServ.getTagsByType(TagUseTypeEnum.ALBUM.getValue());
		Map<Integer,TagInfoVO> tagInfoMap = buildTagInfo(tags,0);
		//4.设置年龄段
		TagInfoVO ageRange = tagInfoMap.get(IConstants.ANIMEAGEGROUPID);
		searchCondition.setAgeRange(ageRange);
		tagInfoMap.remove(IConstants.ANIMEAGEGROUPID);
		//5.设置筛选标签
		searchCondition.setFilterTags(tagInfoMap.values().toArray(new TagInfoVO[0]));
		
		return searchCondition;
	}
	
	/**
	 * 入参：界面所有搜索的信息：page,pageSize,sortId综合排序,ageRange年龄，categoryId分类
	* <p>功能描述:知识搜索功能</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchKnowledge() throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		int categoryId = 0;
		// 搜索词
		String searchKey = "";
		// 综合排序id
		Integer sortId = null;

		// 获取前台传过来的数据
		if (!reqBody.isNull("categoryId")) {
			categoryId = reqBody.optInt("categoryId");
			// 设置分类
			searchVO.setCatg(String.valueOf(categoryId));
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		if (!reqBody.isNull("sortId")) {
			sortId = reqBody.optInt("sortId");
		}

		// 设置版本和平台
		searchVO.setPlatform(getContext().getHead().getPlatform());
		searchVO.setVersion(getContext().getHead().getVersion());
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);
		// 设置搜索综合排序
		setAnimeSearchOrder(searchVO, sortId);
		// 筛选条件Map
		Map<String, String> criteriaList = new HashMap<String, String>();
		// 设置年龄搜索条件
		setAgeRangeSearchCondition(criteriaList, reqBody);
		// 设置是否上架属性
		// setAlbumUplow(searchVO);
		// 设置视频类型为知识视频
		searchVO.setSpecialType(AlbumCategoryTypeEnum.KNOWLEDGE.getValue());

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		// 调用搜索服务搜索知识
		List<AlbumVO> albumVOs = searchServ.searchAlbum(searchVO, criteriaList,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());
		AlbumVOInfo info = new AlbumVOInfo();
		//设置热门搜索词
		info.setHotKeys(getHotSearchKeys(IConstants.CONFIG_KNOWLEDGE_SK));
		info.setKnowledgeVideos(albumVOs);
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(info));
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), info);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索知识成功",
				result, this);
	}
	
	
	/**
	 * 入参：界面所有搜索的信息：page,pageSize,sortId综合排序,ageRange年龄，categoryId分类，filterInfo筛选条件
	* <p>功能描述:动漫搜索功能</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchAnime() throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
			int page = 0;
			int pageSize = 10;
			int categoryId = 0;
			//搜索词
			String searchKey = "";
			//综合排序id
			Integer sortId = null;
			
			//获取前台传过来的数据
			if(!reqBody.isNull("categoryId")){
				categoryId = reqBody.optInt("categoryId");
				//设置动漫分类
				searchVO.setCatg(String.valueOf(categoryId));
			}
			if(!reqBody.isNull("page")){
				page = reqBody.optInt("page");
			}
			if(!reqBody.isNull("pageSize")){
				pageSize = reqBody.optInt("pageSize");
			}
			if(!reqBody.isNull("searchKey")){
				searchKey = reqBody.optString("searchKey");
			}
			if(!reqBody.isNull("sortId")){
				sortId = reqBody.optInt("sortId");
			}
			
			//设置版本和平台
			searchVO.setPlatform(getContext().getHead().getPlatform());
			searchVO.setVersion(getContext().getHead().getVersion());
			//设置搜索分页信息
			searchVO.setSp(page);
			searchVO.setPageSize(pageSize);
			//设置搜索词
			searchVO.setSk(searchKey);
			//设置搜索综合排序
			setAnimeSearchOrder(searchVO,sortId);
			//获取界面的筛选条件
			Map<String,String> criteriaList = bulidSearchFilterData(reqBody,searchVO);
			//设置年龄搜索条件
			setAgeRangeSearchCondition(criteriaList,reqBody);
			//设置是否上架属性
//			setAlbumUplow(searchVO);
			//设置视频类型为动漫视频
			searchVO.setSpecialType(AlbumCategoryTypeEnum.VIDEO.getValue());
			
			SearchService searchServ = SystemInitialization.getApplicationContext().getBean(SearchService.class);
			//调用搜索服务搜索动漫
		List<AlbumVO> albumVOs = searchServ.searchAlbum(searchVO, criteriaList,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());
			
			//根据选中条件搜索为空标识，1为空，0为非空
			Integer searchNullFlag = 0;
			//如果搜索结果为空，去掉弱关联的筛选搜索条件，获取相关动漫
			if((albumVOs == null || albumVOs.size() == 0) && !criteriaList.isEmpty()){
				//移除搜索条件中的弱关联项条件
				criteriaList = removeWeakSearchCondition(criteriaList,TagStatusEnum.VALID,TagUseTypeEnum.ALBUM);
				//去掉弱关联项的筛选条件再次搜索动漫
			albumVOs = searchServ.searchAlbum(searchVO, criteriaList,
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform());
				searchNullFlag = 1;
			}
			AlbumVOInfo info = new AlbumVOInfo();
			//设置热门搜索词
			info.setHotKeys(getHotSearchKeys(IConstants.CONFIG_ALBUM_SK));
			info.setAlbums(albumVOs);
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(info));
			if(page == 0){
				result.put("searchNullFlag", searchNullFlag);
			}
			result.put("searchKey", searchKey);
			Util.addStatistics(getContext(), info);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索动漫成功", result, this);
	}
	
	/**
	* <p>功能描述:设置平台是否上架属性</p>
	* <p>参数：@param searchVO</p>
	* <p>返回类型：void</p>
	 */
	@SuppressWarnings("unused")
	private void setAlbumUplow(SearchVO searchVO){
		String platform = getContext().getHead().getPlatform();
		if(MobileTypeEnum.iphone == MobileTypeEnum.valueOf(platform)){
			searchVO.setIosUplow(UpLowStatusEnum.UPPER.getValue());
		}else if(MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platform)){
			searchVO.setAndroidUplow(UpLowStatusEnum.UPPER.getValue());
		}
	}
	
	/**
	 * 商品搜索入参：界面所有搜索的信息：page,pageSize,sortVos综合排序,ageRange年龄，catagorys分类，filterInfo筛选条件
	* <p>功能描述:玩具的搜索功能</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchProduct() throws Exception{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		SearchVO searchVO = new SearchVO();
		int page = 0;
		int pageSize = 10;
		int categoryId = 0;
		int rootCategoryId = 0;
		// 搜索词
		String searchKey = "";
		// 综合排序id
		Integer sortId = null;
		// 品牌id
		Integer brandId = null;
		// 动漫周边id
		Integer albumId = null;
		
		// 获取前台传过来的数据
		if (!reqBody.isNull("categoryId")) {
			categoryId = reqBody.optInt("categoryId");
			searchVO.setCatg(String.valueOf(categoryId));
		}
		if (!reqBody.isNull("rootCategoryId")) {
			rootCategoryId = reqBody.optInt("rootCategoryId");
			searchVO.setRootCategoryId(rootCategoryId);
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("searchKey")) {
			searchKey = reqBody.optString("searchKey");
		}
		if (!reqBody.isNull("sortId")) {
			sortId = reqBody.optInt("sortId");
		}
		// 品牌搜索条件获取
		if (!reqBody.isNull("filterInfo")
				&& !((JSONObject) reqBody.get("filterInfo")).isNull("brandId")) {
			brandId = ((JSONObject) reqBody.get("filterInfo"))
					.optInt("brandId");
		}
		// TODO 动漫周边搜索条件获取
		if (!reqBody.isNull("filterInfo")
				&& !((JSONObject) reqBody.get("filterInfo")).isNull("albumId")) {
			albumId = ((JSONObject) reqBody.get("filterInfo"))
					.optInt("albumId");
		}
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		// 设置搜索词
		searchVO.setSk(searchKey);
		// 设置搜索综合排序
		setSearchOrder(searchVO, sortId);
		// 设置品牌id搜索条件
		searchVO.setBrand(brandId == null ? null : String.valueOf(brandId));
		// 获取界面的筛选条件
		Map<String, String> criteriaList = bulidSearchFilterData(reqBody,searchVO);
		// 设置年龄搜索条件
		setAgeRangeSearchCondition(criteriaList, reqBody);
		// 设置动漫周边
		searchVO.setProductAlbumId(albumId);

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		// 调用搜索服务搜索商品
		List<EbProductVO> products = searchServ.searchProduct(searchVO,
				criteriaList, getContext().getHead().getVersion(), getContext()
						.getHead().getPlatform());

		// 根据选中条件搜索为空标识，1为空，0为非空
		Integer searchNullFlag = 0;
		// 如果搜索结果为空，去掉弱关联的筛选搜索条件，获取相关商品
		if ((products == null || products.size() == 0)
				&& !criteriaList.isEmpty()) {
			// 移动搜索条件中的弱关联项条件
			criteriaList = removeWeakSearchCondition(criteriaList,
					TagStatusEnum.VALID, TagUseTypeEnum.PRODUCT);
			// 去掉弱关联项的筛选条件再次搜索商品
			products = searchServ.searchProduct(searchVO, criteriaList,
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform());
			searchNullFlag = 1;
		}
		ProductInfo prodcutInfo = new ProductInfo();
		//设置热门搜索词
		prodcutInfo.setHotKeys(getHotSearchKeys(IConstants.CONFIG_PRODUCT_SK));
		prodcutInfo.setProducts(products);
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(prodcutInfo));
		if(page == 0){
			result.put("searchNullFlag", searchNullFlag);
		}
		result.put("searchKey", searchKey);
		Util.addStatistics(getContext(), prodcutInfo);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "搜索商品成功",
				result, this);
	}
	
	/**
	* <p>功能描述:移除弱关联筛选条件</p>
	* <p>参数：@param criteriaList
	* <p>参数：@param status
	* <p>参数：@param type</p>
	* <p>返回类型：void</p>
	 * @throws SqlException 
	 */
	private Map<String,String> removeWeakSearchCondition(Map<String,String> criteriaList,TagStatusEnum status,TagUseTypeEnum type) throws SqlException{
		TagGroupService groupSver = SystemInitialization.getApplicationContext().getBean(TagGroupService.class);
		Map<Integer,TagGroup> groupsMap = groupSver.getTagGroupMap(status, type);
		Map<String,String> filterMap = new HashMap<String, String>();
		if(groupsMap.isEmpty() || criteriaList.keySet() == null || criteriaList.keySet().size() == 0){
			return filterMap;
		}
		//移除弱关联的筛选条件
		for (String groupId : criteriaList.keySet()) {
			if(!groupsMap.containsKey(Integer.parseInt(groupId))){
				continue;
			}
			TagGroup group = groupsMap.get(Integer.parseInt(groupId));
			if(group.getConstraints().getValue() != TagGroupConstraintEnum.WEEK.getValue().intValue()){
				filterMap.put(groupId, criteriaList.get(groupId));
			}
		}
		return filterMap;
	}
	
	/**
	* <p>功能描述:设置年龄搜索条件值</p>
	* <p>参数：@param criteriaList</p>
	* <p>返回类型：void</p>
	 * @throws JSONException 
	 */
	private void setAgeRangeSearchCondition(Map<String,String> criteriaList,JSONObject reqBody) throws JSONException{
		if(reqBody.isNull("ageRange")){
			return;
		}
		//将年龄放入标签组中，因为年龄用的标签组不在筛选界面，所以这里处理一下
		JSONObject ageRange = reqBody.getJSONObject("ageRange");
		String ageGroupId = null;
		if(!ageRange.isNull("groupId")){
			ageGroupId = ageRange.optString("groupId");
		}
		if(StringUtil.isNotNullNotEmpty(ageGroupId)){
			if(StringUtil.isNotNullNotEmpty(ageRange.optString("tagId"))){
				criteriaList.put(ageGroupId, ageRange.optString("tagId"));
			}
		}
			
	}
	
	/**
	* <p>功能描述:构建搜索筛选条件</p>
	* <p>参数：@param reqBody
	* <p>参数：@return</p>
	* <p>返回类型：Map<String,String></p>
	 * @throws JSONException 
	 */
	private Map<String,String> bulidSearchFilterData(JSONObject reqBody,SearchVO searchVO) throws JSONException{
		Map<String,String> searchMap = new HashMap<String, String>();
		if(reqBody.isNull("filterInfo")){
			return searchMap;
		}
		//获取筛选界面的所有标签组
		JSONObject filterInfo = (JSONObject) reqBody.get("filterInfo");
		JSONArray groupArray = filterInfo.optJSONArray("groupIds");
		if (groupArray == null) {
			return searchMap;
		}
		// 将所有标签组中的数据构建为Map<String,String>的格式
		for (int i = 0; i < groupArray.length(); i++) {
			JSONObject tagObj = (JSONObject) groupArray.get(i);
			if (tagObj.isNull("tagId") || tagObj.isNull("groupId")) {
				continue;
			}
			String groupId = String.valueOf(tagObj.optInt("groupId"));
			int tagId = tagObj.optInt("tagId");
			// 若groupId和tagId为一个为空，将其去掉不放入到搜索条件中
			if (StringUtil.isNullOrEmpty(groupId) || tagId == 0) {
				continue;
			}
			// 有货groupId
			if (String.valueOf(IConstants.HAVEGOODSGROUPID).equals(groupId)) {
				searchVO.setStorageStatus(1);
			} else {
				searchMap.put(groupId, String.valueOf(tagId));
			}
		}
		return searchMap;
	}
	
	
	/**
	* <p>功能描述:构建筛选条件Map</p>
	* <p>参数：@param searchMap
	* <p>参数：@param groupId
	* <p>参数：@param tagArray
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：void</p>
	 */
	@SuppressWarnings("unused")
	private void putSearchTagIds(Map<String,List<String>> searchMap,String groupId,JSONArray tagArray) throws JSONException{
		if(tagArray == null || tagArray.length() <= 0){
			return;
		}
		if(searchMap.containsKey(groupId)){
			List<String> list = searchMap.get(groupId);
			for (int i = 0; i < tagArray.length(); i++) {
				String tagId = String.valueOf(tagArray.get(i));
				list.add(tagId);
			}
		}else{
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < tagArray.length(); i++) {
				String tagId = String.valueOf(tagArray.get(i));
				list.add(tagId);
			}
			searchMap.put(groupId,list);
		}
	}
	
	/**
	* <p>功能描述:设置搜索排序</p>
	* <p>参数：@param searchVO</p>
	* <p>返回类型：void</p>
	 */
	private void setAnimeSearchOrder(SearchVO searchVO,Integer sortId){
		if(sortId == null){
			searchVO.setOby(null);
			return;
		}
		if(sortId == 0){
			searchVO.setOby(SearchOrderByEnum.COMPOSITE.getValue().toString());
			searchVO.setDesc(true);
		}else if(sortId == 1){
			searchVO.setOby(SearchOrderByEnum.HOTPLAY.getValue().toString());
			searchVO.setDesc(true);
		}else if(sortId == 2){
			searchVO.setOby(SearchOrderByEnum.NEWONSHELF.getValue().toString());
			searchVO.setDesc(true);
		}
	}
	
	/**
	* <p>功能描述:设置搜索排序</p>
	* <p>参数：@param searchVO</p>
	* <p>返回类型：void</p>
	 */
	private void setSearchOrder(SearchVO searchVO,Integer sortId){
		if(sortId == null){
			searchVO.setOby(SearchOrderByEnum.DEFAULT.getValue().toString());
			searchVO.setDesc(true);
			return;
		}
		if(sortId == 0){
			searchVO.setOby(SearchOrderByEnum.PRICE.getValue().toString());
			searchVO.setDesc(true);
		}else if(sortId == 1){
			searchVO.setOby(SearchOrderByEnum.PRICE.getValue().toString());
			searchVO.setDesc(false);
		}else if(sortId == 2){
			searchVO.setOby(SearchOrderByEnum.NEWONSHELF.getValue().toString());
			searchVO.setDesc(true);
		}else if(sortId == 3){
			searchVO.setOby(SearchOrderByEnum.SCORE.getValue().toString());
			searchVO.setDesc(true);
		}else if(sortId == 4){
			searchVO.setOby(SearchOrderByEnum.SALENUM.getValue().toString());
			searchVO.setDesc(true);
		}else if(sortId == 5){
			searchVO.setOby(SearchOrderByEnum.DISCOUNT.getValue().toString());
			searchVO.setDesc(false);
		}
	}
	
	/**
	 * 入参：categoryId,page,pageSize
	* <p>功能描述:根据视频分类分页搜索动漫视频或者知识视频</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult searchAlbumByCategory() throws Exception{
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = 10;
		int categoryId = 0;
		String title = null;
		SearchVO searchVO = new SearchVO();

		// 获取前台传过来数据
		if (reqBody.isNull("categoryId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"categoryId不能为空", result, this);
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("title")) {
			title = reqBody.optString("title");
		}

		// 设置版本和平台
		searchVO.setPlatform(getContext().getHead().getPlatform());
		searchVO.setVersion(getContext().getHead().getVersion());
		// 设置搜索分页信息
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		categoryId = reqBody.optInt("categoryId");

		SearchService searchServ = SystemInitialization.getApplicationContext()
				.getBean(SearchService.class);
		List<AlbumVO> animes = searchServ.searchAlbumByCategoryPage(categoryId,
				searchVO);
		AlbumVOInfo info = new AlbumVOInfo();
		info.setAnimes(animes);
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(info));
		result.put("title", title);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取分类视频成功", result, this);
	}
	
	/**
	* <p>功能描述:知识首页分类搜索</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getKnowledgeCategorysSearch(){
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		try {
			int page = -1;
			int pageSize = -1;
			int categoryId = 0;
			String title = null;
			if(reqBody.isNull("categoryId")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "categoryId不能为空", result, this);
			}
			if(!reqBody.isNull("page")){
				page = reqBody.optInt("page");
			}
			if(!reqBody.isNull("pageSize")){
				pageSize = reqBody.optInt("pageSize");
			}
			if(!reqBody.isNull("title")){
				title = reqBody.optString("title");
			}
			categoryId = reqBody.optInt("categoryId");
			KnowledgeSearchVO knowledgeSearch = new KnowledgeSearchVO();
			//1  首页知识分类
			knowledgeSearch.setCategorys(getAlbumCategoryVOs(2, AlbumCategoryTypeEnum.KNOWLEDGE.getValue(),categoryId));
			//2 综合排序
			knowledgeSearch.setCompositiveSort(getCompostiveSort(animeCompositiveSort));
			//3 设置选中分类下的知识视频
			knowledgeSearch.setKnowledgeVideos(getAlbumVO(categoryId,page,pageSize,AlbumCategoryTypeEnum.KNOWLEDGE.getValue()));
			//4.设置年龄段
			TagService tagServ = SystemInitialization.getApplicationContext().getBean(TagService.class);
			List<Tag> tags = tagServ.getTagsByGroupId(IConstants.ANIMEAGEGROUPID,TagUseTypeEnum.ALBUM.getValue());
			Map<Integer,TagInfoVO> tagInfoMap = buildTagInfo(tags,0);
			knowledgeSearch.setAgeRange(tagInfoMap.get(IConstants.ANIMEAGEGROUPID));
			Gson gson = new Gson();
			result = new JSONObject(gson.toJson(knowledgeSearch));
			result.put("title", title);
			result.put("categoryId", categoryId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取知识首页分类搜索成功", result, this);
		} catch (Exception e) {
			logger.error("getAnimeCategorys() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	
	/**
	* <p>功能描述:动漫首页分类过入搜索界面</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult getAnimeCategorys() throws Exception{
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		int categoryId = 0;
		String title = null;
		if (reqBody.isNull("categoryId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"categoryId不能为空", result, this);
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("title")) {
			title = reqBody.optString("title");
		}

		categoryId = reqBody.optInt("categoryId");
		// 动漫搜索界面返回数据VO
		SearchConditionInfoVO searchCondition = new SearchConditionInfoVO();
		// 1.设置综合排序
		searchCondition.setSortVos(getCompostiveSort(animeCompositiveSort));
		// 2.处理动漫分类
		searchCondition.setCategorys(getAlbumCategoryVOs(1,
				AlbumCategoryTypeEnum.VIDEO.getValue(), categoryId));
		// 3.处理分类下的动漫
		searchCondition.setAlbums(getAlbumVO(categoryId, page, pageSize,
				AlbumCategoryTypeEnum.VIDEO.getValue()));
		// 4.获取视频的所有taggroup里的标签
		TagService tagServ = SystemInitialization.getApplicationContext()
				.getBean(TagService.class);
		List<Tag> tags = tagServ.getTagsByType(TagUseTypeEnum.ALBUM.getValue());
		Map<Integer, TagInfoVO> tagInfoMap = buildTagInfo(tags, 0);
		// 5.设置年龄段
		TagInfoVO ageRange = tagInfoMap.get(IConstants.ANIMEAGEGROUPID);
		searchCondition.setAgeRange(ageRange);
		tagInfoMap.remove(IConstants.ANIMEAGEGROUPID);
		// 6.设置筛选标签
		searchCondition.setFilterTags(tagInfoMap.values().toArray(
				new TagInfoVO[0]));
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(searchCondition));
		result.put("title", title);
		result.put("categoryId", categoryId);
		Util.addStatistics(getContext(), searchCondition);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取动漫搜索条件成功", result, this);
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
	* <p>功能描述:获取某一级某种类型的视频分类</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AlbumCategoryVO></p>
	 */
	private List<AlbumCategoryVO> getAlbumCategoryVOs(int level,int type,int selectId) throws SqlException{
		EbCatagoryService categoryServ = SystemInitialization.getApplicationContext().getBean(EbCatagoryService.class);
		List<AlbumCategory> categoryVOs = categoryServ.getAlbumCategoryByLevel(level, type);
		return buildAlbumCategoryVO(categoryVOs,selectId);
	}
	
	/**
	* <p>功能描述:构建视频分类VO</p>
	* <p>参数：@param categoryVOs
	* <p>参数：@return</p>
	* <p>返回类型：List<AlbumCategoryVO></p>
	 */
	private List<AlbumCategoryVO> buildAlbumCategoryVO(List<AlbumCategory> categoryVOs,Integer selectId){
		if(categoryVOs == null || categoryVOs.size() <= 0){
			return null;
		}
		List<AlbumCategoryVO> voList = new ArrayList<AlbumCategoryVO>();
//		String imageHost  = SystemManager.getInstance().getSystemConfig().getImgServerUrl();
		for (AlbumCategory albCate : categoryVOs) {
			AlbumCategoryVO vo = new AlbumCategoryVO();
			vo.setAlbumCategoryId(albCate.getId());
			vo.setCategoryName(albCate.getCname());
//			vo.setImageSrc(Util.getFullImageURL(albCate.getImageSrc()));
			vo.setImageSrc(Util.getFullImageURLByVersion(albCate.getImageSrc(),
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform()));
			if(selectId == albCate.getId().intValue()){
				vo.setIsSelected("1");
			}else{
				vo.setIsSelected("0");
			}
			voList.add(vo);
		}
		return voList;
	}
	
	/**
	* <p>功能描述:获取动漫VO</p>
	* <p>参数：@param categoryId
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@return</p>
	* <p>返回类型：List<AlbumVO></p>
	 */
	private List<AlbumVO> getAlbumVO(int categoryId,int page,int pageSize,int specialType){
		SearchVO searchVo = new SearchVO();
		searchVo.setSp(page);
		searchVo.setPageSize(pageSize);
		searchVo.setCatg(String.valueOf(categoryId));
		searchVo.setSpecialType(specialType);
		searchVo.setPlatform(getContext().getHead().getPlatform());
		searchVo.setVersion(getContext().getHead().getVersion());
		SearchService searchServ = SystemInitialization.getApplicationContext().getBean(SearchService.class);
		List<AlbumVO> albumVOs = searchServ.searchAlbum(searchVo,
				new HashMap<String, String>(), getContext().getHead()
						.getVersion(), getContext().getHead().getPlatform());
		return albumVOs;
	}
	/**
	 * <p>
	 * 功能描述:构建专辑VO
	 * </p>
	 * <p>
	 * 参数：@param ablums
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<AlbumVO>
	 * </p>
	 */
	@SuppressWarnings("unused")
	private List<AlbumVO> buildAlbumVO(List<Album> ablums) {
		if (ablums == null || ablums.size() <= 0) {
			return null;
		}
		List<AlbumVO> vos = new ArrayList<AlbumVO>();
		for (Album a : ablums) {
			AlbumVO vo = new AlbumVO();
			vo.setSnapshot(a.getCover());
			vo.setId(a.getId());
			vo.setName(a.getName());
			vo.setType(a.getType().getValue());
			vo.setVip(a.getVip());
			vo.setTotalCount(NumericUtil.parseInt(a.getTotalCount()));
			vo.setNowCount(NumericUtil.parseInt(a.getNowCount()));
			vos.add(vo);
		}
		return vos;
	}
	
	/**
	* <p>功能描述:获取商品一级分类</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws JSONException 
	 */
	private ExecuteResult getProductFirstCategory() throws JSONException{
		List<FirstCategoryVO> list = getFirstCategoryVOs();
		FirstCategoryInfo info = new FirstCategoryInfo();
		info.setAllCategorys(list);
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(info));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取一级分类成功", result, this);
	}
	
	/**
	* <p>功能描述:获取一级分类VO</p>
	* <p>参数：@return</p>
	* <p>返回类型：List<FirstCategoryVO></p>
	 */
	private List<FirstCategoryVO> getFirstCategoryVOs(){
		List<FirstCategoryVO> ret = new ArrayList<FirstCategoryVO>();
		EbRootCategoryEnum[] firstCates = EbRootCategoryEnum.values();
		for (int i = 0; i < firstCates.length; i++) {
			EbRootCategoryEnum root = firstCates[i];
			FirstCategoryVO vo = new FirstCategoryVO();
			vo.setCategoryId(root.getValue());
			vo.setCategoryName(root.getText());
			
			ret.add(vo);
		}
		return ret;
	}
	
	/**
	 * @功能描述:根据品牌id进入到搜索页面
	 * @return ExecuteResult
	 * @author yusf
	 * @throws JSONException 
	 */
	private ExecuteResult getProductByBrandId() throws Exception {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		String searchKey = "";
		if (reqBody.isNull("brandId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"brandId不能为空！", result, this);
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		int brandId = reqBody.optInt("brandId");
		// 所有分类信息返回VO
		CatagoryInfoVO catagoryVo = new CatagoryInfoVO();
		// 根据界面选中的品牌获取相应的商品
		List<EbProductVO> products = getProudctsByBrandId(searchKey, brandId,
				page, pageSize);
		catagoryVo.setProducts(products);
		// 综合排序
		catagoryVo.setSortVos(getCompostiveSort(compositiveSort));
		// 设置一级二级分类
		setFirstAndSecondCatagorys(catagoryVo, 0);
		// 筛选VO
		FilterInfoVO filterVO = buildFilterVo();
		// 设置选中的品牌id
		filterVO.getBrandInfo().setSelectBrandId(brandId);
		catagoryVo.setFilterInfo(filterVO);
		// 设置年龄
		catagoryVo.setAgeRange(getAgeRange());
		//设置热门搜索词
		catagoryVo.setHotKeys(getHotSearchKeys(IConstants.CONFIG_PRODUCT_SK));
		catagoryVo.setProducts(products);
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(catagoryVo));
		result.put("searchKey", searchKey);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取品牌搜索商品成功",
				result, this);
	}
	
	/**
	* <p>功能描述:获取品牌下的商品</p>
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProduct></p>
	 */
	private List<EbProductVO> getProudctsByBrandId(String searchKey,int brandId,int page,int pageSize){
		SearchService searchServ = SystemInitialization.getApplicationContext().getBean(SearchService.class);
		SearchVO searchVO = new SearchVO();
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		searchVO.setBrand(String.valueOf(brandId));
		searchVO.setSk(searchKey);
		Map<String,String> criteriaList = new HashMap<String, String>();
//		ProductServiceV5_0 prodServ = SystemInitialization.getApplicationContext().getBean(ProductServiceV5_0.class);
//		return prodServ.getProductByBrand(brandId, page*pageSize, pageSize);
		return searchServ.searchProduct(searchVO, criteriaList, getContext()
				.getHead().getVersion(), getContext().getHead().getPlatform());
	}
	
	/**
	* <p>功能描述:玩具首页点分类进入分类搜索界面接口</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult getProductCatagorys() throws Exception {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		String title = null;

		if (reqBody.isNull("categoryId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"categoryId不能为空！", result, this);
		}
		if (!reqBody.isNull("page")) {
			page = reqBody.optInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.optInt("pageSize");
		}
		if (!reqBody.isNull("title")) {
			title = reqBody.optString("title");
		}

		int categoryId = reqBody.optInt("categoryId");
		// 所有分类信息返回VO
		CatagoryInfoVO catagoryVo = new CatagoryInfoVO();
		// 根据界面选中的分类获取相应的商品
		List<EbProductVO> products = searchProductByRootCategoryId(categoryId,
				page, pageSize);
		catagoryVo.setProducts(products);
		// 综合排序
		catagoryVo.setSortVos(getCompostiveSort(compositiveSort));
		// 设置一级二级分类
		setFirstAndSecondCatagorys(catagoryVo, categoryId);
		// 筛选VO
		FilterInfoVO filterVO = buildFilterVo();
		catagoryVo.setFilterInfo(filterVO);
		// 设置年龄
		catagoryVo.setAgeRange(getAgeRange());
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(catagoryVo));
		result.put("title", title);
		result.put("rootCategoryId", categoryId);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取分类成功",
				result, this);
	}
	
	
	/**
	* <p>功能描述:获取年龄段数据</p>
	* <p>参数：@return</p>
	* <p>返回类型：List<TagInfoVO></p>
	 */
	private TagInfoVO[] getAgeRange(){
		TagService tagServ = SystemInitialization.getApplicationContext().getBean(TagService.class);
		List<Tag> tags = tagServ.getTagsByGroupId(IConstants.AGEGROUPID, TagUseTypeEnum.PRODUCT.getValue());
		
		return buildTagInfo(tags,1).values().toArray(new TagInfoVO[0]);
	}
	
	/**
	* <p>功能描述:获取一级分类下所有2级分类的商品</p>
	* <p>参数：@param categoryId
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProduct></p>
	 * @throws SqlException 
	 */
	@SuppressWarnings("unused")
	private List<EbProduct> getProductByCategoryId(int categoryId,int page,int pageSize) throws SqlException{
		ProductServiceV5_0 prodServ = SystemInitialization.getApplicationContext().getBean(ProductServiceV5_0.class);
		return prodServ.getProductByFirstCategory(categoryId, page, pageSize);
	}
	
	/**
	* <p>功能描述:获取一级分类下所有2级分类的商品</p>
	* <p>参数：@param categoryId
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProduct></p>
	 * @throws SqlException 
	 */
	private List<EbProductVO> searchProductByRootCategoryId(int categoryId,int page,int pageSize) throws SqlException{
		SearchService searchServ = SystemInitialization.getApplicationContext().getBean(SearchService.class);
		SearchVO searchVO = new SearchVO();
		searchVO.setRootCategoryId(categoryId);
		searchVO.setSp(page);
		searchVO.setPageSize(pageSize);
		return searchServ.searchProduct(searchVO,
				new HashMap<String, String>(), getContext().getHead()
						.getVersion(), getContext().getHead().getPlatform());
	}
	
	/**
	* <p>功能描述:构建商品VO</p>
	* <p>参数：@param ps
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProductVO></p>
	 */
	@SuppressWarnings("unused")
	private List<EbProductVO> buildProductVO(List<EbProduct> ps) {
		List<EbProductVO> ls = new ArrayList<EbProductVO>();
		for (EbProduct p : ps) {
			EbProductVO vo = new EbProductVO();
//			vo.setImgUrl(Util.getFullImageURL(p.getImgUrl()));
			vo.setImgUrl(Util.getFullImageURLByVersion(p.getImgUrl(),
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform()));
			vo.setPrice(p.getPrice());
			vo.setProductCode(p.getProductCode());
			vo.setProductName(p.getProductName());
			vo.setStatus(p.getStatus().getValue());
			vo.setSvprice(p.getSvprice());
			vo.setVprice(p.getVprice());
			ls.add(vo);
		}
		return ls;
	}
	
	/**
	* <p>功能描述:获取动漫周边VO列表</p>
	* <p>参数：@param catagoryVo 分类返回VO</p>
	* <p>返回类型：void</p>
	 */
	@SuppressWarnings("unused")
	private AnimeProductInfoVO getAnimeProducts(){
		AnimeProductInfoVO info = new AnimeProductInfoVO();
		String platform = getContext().getHead().getPlatform();
		AlbumServiceV5_0 albumServ = SystemInitialization.getApplicationContext().getBean(AlbumServiceV5_0.class);
		List<AnimeProductVO> animeProdList = new ArrayList<AnimeProductVO>();
		//获取所有动漫周边
		List<Album> albums = albumServ.getAllAnimeProduct(platform, -1, -1);
		
		for (Album album : albums) {
			animeProdList.add(buildAnimeProductVO(album));
		}
		info.setCname(ANIMEPRODUCT);
		info.setAnimeProductList(animeProdList);
		return info;
	}
	
	/**
	* <p>功能描述:构建动漫周边信息VO</p>
	* <p>参数：@param album
	* <p>参数：@return</p>
	* <p>返回类型：AnimeProductVO</p>
	 */
	private AnimeProductVO buildAnimeProductVO(Album album){
		AnimeProductVO aimeProd = new AnimeProductVO();
		aimeProd.setAlbumId(album.getId());
		aimeProd.setName(album.getName());
		return aimeProd;
	}
	
	/**
	* @功能描述:获取综合排序VO  
	* @return     
	* CompositiveSortInfoVO   
	* @author yusf
	 */
	private List<CompositiveSortVO> getCompostiveSort(String[] srot){
		List<CompositiveSortVO> sortVoList = new ArrayList<CompositiveSortVO>();
		for (int i = 0; i < srot.length; i++) {
			CompositiveSortVO sortVo = new CompositiveSortVO();
			sortVo.setSortId(i);
			sortVo.setSortName(srot[i]);
			sortVoList.add(sortVo);
		}
		return sortVoList;
	}
	
	/**
	* @功能描述:构建筛选VO  
	* @return FilterInfoVO   
	* @author yusf
	 */
	private FilterInfoVO buildFilterVo() throws SqlException{
		FilterInfoVO filterVo = new FilterInfoVO();
		//设置所有品牌VO
		filterVo.setBrandInfo(getBrandVO());
		//设置颜色、价格范围、材质标签
		filterVo.setTags(getFilterTagVOs());
		//设置动漫周边信息
//		filterVo.setAnimeProducts(getAnimeProducts());
		return filterVo;
	}
	
	/**
	* <p>功能描述:获取筛选界面颜色、价格范围、材质标签</p>
	* <p>参数：@return</p>
	* <p>返回类型：List<TagInfoVO></p>
	 */
	private TagInfoVO[] getFilterTagVOs(){
		TagService tagServ = SystemInitialization.getApplicationContext().getBean(TagService.class);
		List<Tag> tags = tagServ.getTagsByType(TagUseTypeEnum.PRODUCT.getValue());
		Map<Integer,TagInfoVO> tagInfoTag = buildTagInfo(tags,1);
		//去掉年龄
		tagInfoTag.remove(IConstants.AGEGROUPID);
		return tagInfoTag.values().toArray(new TagInfoVO[0]);
	}
	
	/**
	* <p>功能描述:构建标签信息VO</p>
	* <p>参数：@param tags
	* <p>参数：@return</p>
	* <p>返回类型：List<TagInfoVO></p>
	 */
	private Map<Integer,TagInfoVO> buildTagInfo(List<Tag> tags,int sort){
		//存放TagInVO的Map,key值为groupId,value为TagInfoVO对象
		Map<Integer,TagInfoVO> tagInfoMap = new HashMap<Integer,TagInfoVO>();
		int sortNum = sort;
		for (Tag tag : tags) {
			Integer groupId = tag.getTagGroup().getId();
			if(tagInfoMap.containsKey(groupId)){
				tagInfoMap.get(groupId).getTagList().add(buildTagVO(tag));
			}else{
				List<TagVO> tagVoList = new ArrayList<TagVO>();
				TagVO tagVo = buildTagVO(tag);
				tagVoList.add(tagVo);
				TagInfoVO info = new TagInfoVO();
				info.setCname(tagVo.getTagName());
				info.setGroupId(groupId);
				info.setTagList(tagVoList);
				//设置排序字段，若为年龄不用排序
				if(groupId == IConstants.AGEGROUPID || groupId == IConstants.ANIMEAGEGROUPID){
				
				}else{
					info.setSortNum(sortNum);
					sortNum++;
				}
				tagInfoMap.put(groupId, info);
			}
		}
		
		return tagInfoMap;
	}
	
	/**
	* <p>功能描述:构建标签VO</p>
	* <p>参数：@param tag
	* <p>参数：@return</p>
	* <p>返回类型：TagVO</p>
	 */
	private TagVO buildTagVO(Tag tag){
		TagVO vo = new TagVO();
		if(tag == null){
			return vo;
		}
		vo.setGroupId(tag.getTagGroup().getId());
		vo.setTagId(tag.getId());
		vo.setTagName(tag.getTagName());
		vo.setTagValue(tag.getTagValue());
		
		return vo;
	}
	
	/**
	* @功能描述:获取价格范围  
	* @return     
	* List<PriceRangeVO>   
	* @author yusf
	 */
	@SuppressWarnings("unused")
	private PriceRangeVOInfo getPriceRange(){
		PriceRangeVOInfo priceInfo = new PriceRangeVOInfo();
		List<PriceRangeVO> ranges = new ArrayList<PriceRangeVO>();
		for (int i = 0; i < priceRange.length; i++) {
			PriceRangeVO priceVO = new PriceRangeVO();
			priceVO.setPriceId(i);
			priceVO.setPriceRange(priceRange[i]);
			ranges.add(priceVO);
		}
		priceInfo.setCname(PRICE);
		priceInfo.setPriceRanges(ranges);
		return priceInfo;
	}
	
	/**
	* @功能描述:获取产品的属性，主要有年龄和材质 
	* @return List<String>   
	* @author yusf
	 */
	@SuppressWarnings("unused")
	private List<ProductPropsVO> getPropsVO(EbPropsEnum type) throws SqlException{
		List<ProductPropsVO> propsVOList = new ArrayList<ProductPropsVO>();
		EbProductPropsService propService =SystemInitialization.getApplicationContext().getBean(EbProductPropsService.class);
		List<EbProps> props = propService.getPropsByType(type);
		for (EbProps ebProps : props) {
			ProductPropsVO propsVO = new ProductPropsVO();
			propsVO.setPropsId(ebProps.getId());
			propsVO.setPropsValue(ebProps.getPropsValue());
			propsVOList.add(propsVO);
		}
		return propsVOList;
	}
	
	/**
	* @功能描述:获取产品的属性，主要有年龄和材质 
	* @return List<String>   
	* @author yusf
	 */
	@SuppressWarnings("unused")
	private ProductPropsInfoVO getPropsInfoVO(EbPropsEnum type,String typeName) throws SqlException{
		ProductPropsInfoVO propsInfo = new ProductPropsInfoVO();
		List<ProductPropsVO> propsVOList = new ArrayList<ProductPropsVO>();
		EbProductPropsService propService =SystemInitialization.getApplicationContext().getBean(EbProductPropsService.class);
		List<EbProps> props = propService.getPropsByType(type);
		for (EbProps ebProps : props) {
			ProductPropsVO propsVO = new ProductPropsVO();
			propsVO.setPropsId(ebProps.getId());
			propsVO.setPropsValue(ebProps.getPropsValue());
			propsVOList.add(propsVO);
		}
		propsInfo.setCname(typeName);
		propsInfo.setProps(propsVOList);
		return propsInfo;
	}
	
	/**
	* @功能描述:获取品牌VO  
	* @return     
	* List<BrandVO>   
	* @author yusf
	 * @throws SqlException 
	 */
	private BrandsInfoVO getBrandVO() throws SqlException{
		BrandsInfoVO brandInfo = new BrandsInfoVO();
		List<BrandVO> brandVos = new ArrayList<BrandVO>();
		EbBrandService brandService = SystemInitialization.getApplicationContext().getBean(EbBrandService.class);
		List<EbBrand> brands = brandService.getAllBrands();
		for (EbBrand ebBrand : brands) {
			BrandVO bvo = new BrandVO();
			bvo.setBrandId(ebBrand.getBrandId());
			bvo.setBrandName(ebBrand.getBrandName());
			brandVos.add(bvo);
		}
		brandInfo.setCname(BRAND);
		brandInfo.setSortNum(0);
		brandInfo.setBrands(brandVos);
		return brandInfo;
	}
	
	/**
	 * @功能描述:设置一级分类和二级分类
	 * @param catagoryVo
	 * @author yusf
	 * @throws SqlException
	 */
	private void setFirstAndSecondCatagorys(CatagoryInfoVO catagoryVo,int selectId)
			throws SqlException {
		EbCatagoryService catagoryService = SystemInitialization
				.getApplicationContext().getBean(EbCatagoryService.class);
		List<EbCatagory> catagorys = catagoryService.getAllCatagorys();
		//设置一二级分类
		catagoryVo.setCatagorys(buildCatagoryVo(catagorys,selectId));
	}

	/**
	 * @功能描述:构建一级和二级分类VO
	 * @param catagorys
	 * @return Map<Integer,CatagoryVO>
	 * @author yusf
	 */
	private CatagoryVO[] buildCatagoryVo(List<EbCatagory> catagorys,int selectId) {
		
		Map<Integer,CatagoryVO> allRet = new HashMap<Integer, CatagoryVO>();
		for (EbCatagory ebCatagory : catagorys) {
			CatagoryVO catagoryVo = null;
			//若不存在一级分类继续
			if(ebCatagory.getParent() == null){
				continue;
			}
			//一级分类id
			Integer parent = ebCatagory.getParent().getValue().intValue();
			if(allRet.containsKey(parent)){
				catagoryVo = allRet.get(parent);
			}else{
				catagoryVo = new CatagoryVO();
				catagoryVo.setCatagoryId(parent);
				catagoryVo.setCname(EbRootCategoryEnum.valueOf(parent).getText());
				//设置是否选中的分类
				if(selectId == parent){
					catagoryVo.setIsSelected(1);
				}
			}
			
			//若有2级分类将相同的2级分类添加到Map里
			if(catagoryVo.getSecondCatagoryVO() != null){
				catagoryVo.getSecondCatagoryVO().add(buildSecondCatagoryVO(ebCatagory,parent));
			}else{
				List<SecondCatagoryVO> list = new ArrayList<SecondCatagoryVO>();
				list.add(buildSecondCatagoryVO(ebCatagory,parent));
				// 设置2级分类
				catagoryVo.setSecondCatagoryVO(list);
			}
			allRet.put(parent, catagoryVo);
		}
		return allRet.values().toArray(new CatagoryVO[0]);

	}

	/**
	 * @功能描述: 构建同一父类的2级分类
	 * @param catagorys
	 * @param parentId
	 * @return List<SecondCatagoryVO>
	 * @author yusf
	 */
	private SecondCatagoryVO buildSecondCatagoryVO(EbCatagory catagory,Integer parentId) {
		SecondCatagoryVO returnVo = new SecondCatagoryVO();
		if (catagory == null) {
			return null;
		}
		int pid = catagory.getParent().getValue();
		if (pid == parentId) {
			returnVo.setCatagoryId(catagory.getId());
			returnVo.setCname(catagory.getCname());
		}
		return returnVo;
	}
	
	/**
	 * @功能描述: 构建同一父类的2级分类
	 * @param catagorys
	 * @param parentId
	 * @return List<SecondCatagoryVO>
	 * @author yusf
	 */
	@SuppressWarnings("unused")
	private List<SecondCatagoryVO> buildSecondCatagoryVO(
			List<EbCatagory> catagorys, int parentId) {
		List<SecondCatagoryVO> returnVo = new ArrayList<SecondCatagoryVO>();
		if (catagorys == null || catagorys.size() <= 0) {
			return returnVo;
		}
		for (EbCatagory ebCatagory : catagorys) {
			Integer pid = ebCatagory.getParent().getValue();
			if (pid == parentId) {
				SecondCatagoryVO secondCata = new SecondCatagoryVO();
				secondCata.setCatagoryId(ebCatagory.getId());
				secondCata.setCname(ebCatagory.getCname());
				returnVo.add(secondCata);
			}
		}
		return returnVo;
	}

	/**
	 * @功能描述:获取宝宝分类里的信息
	 * @return
	 * @throws Exception
	 *             List<BabyVO>
	 * @author yusf
	 */
	@SuppressWarnings("unused")
	private List<BabyVO> getBabyCatagory() throws Exception {
		Integer customerId = getSessionCustomer().getCustomer().getId();
		if (customerId == null || customerId == 0) {
			return null;
		}
		List<BabyVO> babyVos = new ArrayList<BabyVO>();
		CustomerService custService = SystemInitialization
				.getApplicationContext().getBean(CustomerService.class);
		List<Baby> babys = custService.getBabysByCustomerId(customerId);
		if (babys == null || babys.size() <= 0) {
			return babyVos;
		}
		for (Baby baby : babys) {
			BabyVO bobyVo = new BabyVO();
			bobyVo.setHead(baby.getHead());
			bobyVo.setId(baby.getId());
			bobyVo.setName(baby.getName());
			bobyVo.setSex(baby.getSex());
			bobyVo.setAge(getAge(baby.getBirthday()));
			babyVos.add(bobyVo);
		}
		return babyVos;
	}
	
	/**
	* @功能描述: 根据生日计算年龄 
	* @param birthDay
	* @return
	* @throws Exception     
	* @author yusf
	 */
	public  String getAge(Date birthDay){
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            return "0";
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH)+1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
       
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;
        
        //未过生日
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
            	//未过生日，将年龄减1
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        }
        return age +"";
    } 
	
	/**
	 *动漫搜索条件VO
	 */
	class SearchConditionInfoVO{
		//分类下的视频
		private List<AlbumVO> albums = null;
		//综合排序
		public List<CompositiveSortVO> sortVos = null;
		//年龄范围
		public TagInfoVO ageRange = null;
		//视频分类
		public List<AlbumCategoryVO> categorys;
		//筛选VO
		private TagInfoVO[] filterTags = null;
		//热词
		private List<String> hotKeys = null;
		// 商品热搜词
		private List<String> productHotKeys;
		// 知识热搜词
		private List<String> knowledgeHotKeys;
		// 推荐热搜词
		private List<String> recommendHotKeys;
		// 视频热搜词
		private List<String> albumHotKeys;
		
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
		public List<String> getHotKeys() {
			return hotKeys;
		}
		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}
		public List<AlbumCategoryVO> getCategorys() {
			return categorys;
		}
		public void setCategorys(List<AlbumCategoryVO> categorys) {
			this.categorys = categorys;
		}
		public List<AlbumVO> getAlbums() {
			return albums;
		}
		public void setAlbums(List<AlbumVO> albums) {
			this.albums = albums;
		}
		public List<CompositiveSortVO> getSortVos() {
			return sortVos;
		}
		public void setSortVos(List<CompositiveSortVO> sortVos) {
			this.sortVos = sortVos;
		}
		
		public TagInfoVO getAgeRange() {
			return ageRange;
		}
		public void setAgeRange(TagInfoVO ageRange) {
			this.ageRange = ageRange;
		}
		public TagInfoVO[] getFilterTags() {
			return filterTags;
		}
		public void setFilterTags(TagInfoVO[] filterTags) {
			this.filterTags = filterTags;
		}
		
	}
	
	/**
	 *玩具分类搜索信息VO 
	 */
	class CatagoryInfoVO {
		//商品VO
		private List<EbProductVO> products;
		//综合排序
		public List<CompositiveSortVO> sortVos = null;
		//宝宝分类中的年龄范围
		public TagInfoVO[] ageRange = null;
		//分类
		public CatagoryVO[] catagorys = null;
		//筛选
		public FilterInfoVO filterInfo = null;
		
		public List<String> hotKeys = null;
		// 商品热搜词
		private List<String> productHotKeys;
		// 知识热搜词
		private List<String> knowledgeHotKeys;
		// 推荐热搜词
		private List<String> recommendHotKeys;
		// 视频热搜词
		private List<String> albumHotKeys;
		
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

		public List<String> getHotKeys() {
			return hotKeys;
		}

		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}

		public TagInfoVO[] getAgeRange() {
			return ageRange;
		}

		public void setAgeRange(TagInfoVO[] ageRange) {
			this.ageRange = ageRange;
		}

		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}

		public List<CompositiveSortVO> getSortVos() {
			return sortVos;
		}

		public void setSortVos(List<CompositiveSortVO> sortVos) {
			this.sortVos = sortVos;
		}

		public FilterInfoVO getFilterInfo() {
			return filterInfo;
		}

		public void setFilterInfo(FilterInfoVO filterInfo) {
			this.filterInfo = filterInfo;
		}

		public CatagoryVO[] getCatagorys() {
			return catagorys;
		}

		public void setCatagorys(CatagoryVO[] catagorys) {
			this.catagorys = catagorys;
		}

	}
	
	/**
	 *一级分类VO
	 */
	class CatagoryVO {
		//一级分类id
		private Integer catagoryId;
		//一级分类名称
		private String cname = "";
		//是否选中 1为选中，0为非选中
		private Integer isSelected = 0;
		//二级分类VO
		List<SecondCatagoryVO> secondCatagoryVO = null;
		
		public Integer getIsSelected() {
			return isSelected;
		}
		public void setIsSelected(Integer isSelected) {
			this.isSelected = isSelected;
		}
		public Integer getCatagoryId() {
			return catagoryId;
		}
		public void setCatagoryId(Integer catagoryId) {
			this.catagoryId = catagoryId;
		}
		public String getCname() {
			return cname;
		}
		public void setCname(String cname) {
			this.cname = cname;
		}
		public List<SecondCatagoryVO> getSecondCatagoryVO() {
			return secondCatagoryVO;
		}
		public void setSecondCatagoryVO(List<SecondCatagoryVO> secondCatagoryVO) {
			this.secondCatagoryVO = secondCatagoryVO;
		}
		
	}
	
	/**
	 *分类中的筛选VO
	 */
	class FilterInfoVO {
		//所有品牌信息
		private BrandsInfoVO brandInfo = null;
		//动漫周边
		public AnimeProductInfoVO animeProducts = null;
		//筛选标签：目前只包含颜色，价格范围，材质
		private TagInfoVO[] tags = null;
		
		public TagInfoVO[] getTags() {
			return tags;
		}

		public void setTags(TagInfoVO[] tags) {
			this.tags = tags;
		}

		public AnimeProductInfoVO getAnimeProducts() {
			return animeProducts;
		}

		public void setAnimeProducts(AnimeProductInfoVO animeProducts) {
			this.animeProducts = animeProducts;
		}

		public BrandsInfoVO getBrandInfo() {
			return brandInfo;
		}

		public void setBrandInfo(BrandsInfoVO brandInfo) {
			this.brandInfo = brandInfo;
		}

	}
	
	class ProductPropsVO {
		//属性id
		private Integer propsId;
		//属性值
		private String propsValue;
		
		public Integer getPropsId() {
			return propsId;
		}

		public void setPropsId(Integer propsId) {
			this.propsId = propsId;
		}

		public String getPropsValue() {
			return propsValue;
		}

		public void setPropsValue(String propsValue) {
			this.propsValue = propsValue;
		}
		
		
	}
	
	/**
	 *二级分类VO
	 */
	class SecondCatagoryVO {
		// 分类id
		private Integer catagoryId;
		// 分类名称
		private String cname;
		
		public Integer getCatagoryId() {
			return catagoryId;
		}
		public void setCatagoryId(Integer catagoryId) {
			this.catagoryId = catagoryId;
		}
		public String getCname() {
			return cname;
		}
		public void setCname(String cname) {
			this.cname = cname;
		}
	}
	
	/**
	 *品牌信息VO
	 */
	class BrandsInfoVO{
		private String cname = "";
		//排序字段
		private Integer sortNum;
		//所有品牌VO
		private List<BrandVO> brands;
		//选中的品牌id
		private Integer selectBrandId = 0;
		
		public Integer getSortNum() {
			return sortNum;
		}

		public void setSortNum(Integer sortNum) {
			this.sortNum = sortNum;
		}

		public Integer getSelectBrandId() {
			return selectBrandId;
		}

		public void setSelectBrandId(Integer selectBrandId) {
			this.selectBrandId = selectBrandId;
		}

		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		public List<BrandVO> getBrands() {
			return brands;
		}

		public void setBrands(List<BrandVO> brands) {
			this.brands = brands;
		}

	}
	
	/**
	 *筛选中的价格VO
	 */
	class PriceRangeVO{
		private Integer priceId;
		
		private String priceRange = "";

		public String getPriceRange() {
			return priceRange;
		}

		public void setPriceRange(String priceRange) {
			this.priceRange = priceRange;
		}

		public Integer getPriceId() {
			return priceId;
		}

		public void setPriceId(Integer priceId) {
			this.priceId = priceId;
		}
		
	}
	
	/**
	 *筛选中的价格范围信息VO
	 */
	class PriceRangeVOInfo{
		private String cname = "";
		//所有价格范围
		private List<PriceRangeVO> priceRanges;

		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		public List<PriceRangeVO> getPriceRanges() {
			return priceRanges;
		}

		public void setPriceRanges(List<PriceRangeVO> priceRanges) {
			this.priceRanges = priceRanges;
		}
		
	}
	
	/**
	 *产品属性信息VO
	 */
	class ProductPropsInfoVO{
		private String cname = "";
		//产品属性
		private List<ProductPropsVO> props;

		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		public List<ProductPropsVO> getProps() {
			return props;
		}

		public void setProps(List<ProductPropsVO> props) {
			this.props = props;
		}

	}
	
	/**
	 *综合排序VO
	 */
	class CompositiveSortVO{
		private Integer sortId;
		
		private String sortName;

		public Integer getSortId() {
			return sortId;
		}

		public void setSortId(Integer sortId) {
			this.sortId = sortId;
		}

		public String getSortName() {
			return sortName;
		}

		public void setSortName(String sortName) {
			this.sortName = sortName;
		}
		
	}
	
	class FirstCategoryInfo{
		List<FirstCategoryVO> allCategorys;

		public List<FirstCategoryVO> getAllCategorys() {
			return allCategorys;
		}

		public void setAllCategorys(List<FirstCategoryVO> allCategorys) {
			this.allCategorys = allCategorys;
		}
	}
	
	class FirstCategoryVO{
		private Integer categoryId;
		
		private String categoryName;

		public Integer getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Integer categoryId) {
			this.categoryId = categoryId;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}
		
	}
	
	/**
	 *动漫周边信息VO
	 */
	class AnimeProductInfoVO{
		private String cname = null;
		
		private List<AnimeProductVO> AnimeProductList;
		
		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		public List<AnimeProductVO> getAnimeProductList() {
			return AnimeProductList;
		}

		public void setAnimeProductList(List<AnimeProductVO> animeProductList) {
			AnimeProductList = animeProductList;
		}
		
	}
	
	/**
	 *动漫周边名称
	 */
	class AnimeProductVO{
		//视频id
		private Integer albumId;
		//动漫周边名称
		private String name;

		public Integer getAlbumId() {
			return albumId;
		}

		public void setAlbumId(Integer albumId) {
			this.albumId = albumId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
	
	
	/**
	 *标签组信息VO
	 */
	class TagInfoVO{
		//组名称
		private String cname = null;

		private Integer groupId = null;
		//排序字段
		private Integer sortNum;
		//组下标签列表
		private List<TagVO> tagList = null;
		
		public Integer getSortNum() {
			return sortNum;
		}
		public void setSortNum(Integer sortNum) {
			this.sortNum = sortNum;
		}
		public List<TagVO> getTagList() {
			return tagList;
		}
		public void setTagList(List<TagVO> tagList) {
			this.tagList = tagList;
		}
		public String getCname() {
			return cname;
		}
		public void setCname(String cname) {
			this.cname = cname;
		}
		public Integer getGroupId() {
			return groupId;
		}
		public void setGroupId(Integer groupId) {
			this.groupId = groupId;
		}
		
	}
	
	/**
	 *知识首页分类搜索VO
	 */
	class KnowledgeSearchVO{
		//分类标题
		private String title = null;
		//分类的知识视频
		private List<AlbumVO> knowledgeVideos = null;
		//年龄范围
		private TagInfoVO ageRange = null;
		//知识分类
		private List<AlbumCategoryVO> categorys = null;
		//综合排序
		private List<CompositiveSortVO> compositiveSort = null;
		//热词
		private List<String> hotKeys = null;
		// 商品热搜词
		private List<String> productHotKeys;
		// 知识热搜词
		private List<String> knowledgeHotKeys;
		// 推荐热搜词
		private List<String> recommendHotKeys;
		// 视频热搜词
		private List<String> albumHotKeys;
		
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
		public List<String> getHotKeys() {
			return hotKeys;
		}
		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public List<CompositiveSortVO> getCompositiveSort() {
			return compositiveSort;
		}
		public void setCompositiveSort(List<CompositiveSortVO> compositiveSort) {
			this.compositiveSort = compositiveSort;
		}
		public List<AlbumVO> getKnowledgeVideos() {
			return knowledgeVideos;
		}
		public void setKnowledgeVideos(List<AlbumVO> knowledgeVideos) {
			this.knowledgeVideos = knowledgeVideos;
		}
		public TagInfoVO getAgeRange() {
			return ageRange;
		}
		public void setAgeRange(TagInfoVO ageRange) {
			this.ageRange = ageRange;
		}
		public List<AlbumCategoryVO> getCategorys() {
			return categorys;
		}
		public void setCategorys(List<AlbumCategoryVO> categorys) {
			this.categorys = categorys;
		}
	}
	
	/**
	 *标签VO
	 */
	class TagVO{
		//标签Groupid
		private Integer groupId;
		//标签id
		private Integer tagId;
		//标签值
		private String tagValue = null;
		//标签名称
		private String tagName = null;
		
		public Integer getGroupId() {
			return groupId;
		}
		public void setGroupId(Integer groupId) {
			this.groupId = groupId;
		}
		public Integer getTagId() {
			return tagId;
		}
		public void setTagId(Integer tagId) {
			this.tagId = tagId;
		}
		public String getTagValue() {
			return tagValue;
		}
		public void setTagValue(String tagValue) {
			this.tagValue = tagValue;
		}
		public String getTagName() {
			return tagName;
		}
		public void setTagName(String tagName) {
			this.tagName = tagName;
		}
	}
	
	/**
	 *动漫信息VO
	 */
	class AlbumVOInfo{
		private List<AlbumVO> albums = null;
		
		private List<AlbumVO> knowledgeVideos = null;
		
		private List<AlbumVO> animes = null;
		//热词
		private List<String> hotKeys = null;
		
		public List<String> getHotKeys() {
			return hotKeys;
		}

		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}

		public List<AlbumVO> getAnimes() {
			return animes;
		}

		public void setAnimes(List<AlbumVO> animes) {
			this.animes = animes;
		}

		public List<AlbumVO> getKnowledgeVideos() {
			return knowledgeVideos;
		}

		public void setKnowledgeVideos(List<AlbumVO> knowledgeVideos) {
			this.knowledgeVideos = knowledgeVideos;
		}

		public List<AlbumVO> getAlbums() {
			return albums;
		}

		public void setAlbums(List<AlbumVO> albums) {
			this.albums = albums;
		}

		
	}
	
	/**
	 *视频分类信息VO
	 */
	class AlbumCategoryInfoVO{
		List<AlbumCategoryVO> categorys;

		public List<AlbumCategoryVO> getCategorys() {
			return categorys;
		}

		public void setCategorys(List<AlbumCategoryVO> categorys) {
			this.categorys = categorys;
		}
	}
	
	/**
	 *搜索产品信息VO
	 */
	class ProductInfo{
		private List<EbProductVO> products;
		//热词
		private List<String> hotKeys;
		
		public List<String> getHotKeys() {
			return hotKeys;
		}

		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}

		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}
		
	}
	
	/**
	 *混合搜索返回信息数据
	 */
	class MixSearchResultInfo{
		private List<MixSearchResultVO> searchResults;
		//商品
		private List<EbProductVO> products = null;
		//知识视频
		private List<AlbumVO> knowledgeVideos = null;
		//动漫视频
		private List<AlbumVO> albums = null;
		//全部搜索的视频的总数量
		private long albumCount;
		//全部搜索知识的总数量
		private long knowledgeCount;
		//全部搜索玩具的总数量
		private long productCount;
		//热词
		private List<String> hotKeys = null;
		// 商品热搜词
		private List<String> productHotKeys;
		// 知识热搜词
		private List<String> knowledgeHotKeys;
		// 推荐热搜词
		private List<String> recommendHotKeys;
		// 视频热搜词
		private List<String> albumHotKeys;
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

		public List<String> getHotKeys() {
			return hotKeys;
		}

		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}

		public long getAlbumCount() {
			return albumCount;
		}

		public void setAlbumCount(long albumCount) {
			this.albumCount = albumCount;
		}

		public long getKnowledgeCount() {
			return knowledgeCount;
		}

		public void setKnowledgeCount(long knowledgeCount) {
			this.knowledgeCount = knowledgeCount;
		}

		public long getProductCount() {
			return productCount;
		}

		public void setProductCount(long productCount) {
			this.productCount = productCount;
		}

		public void setProductCount(Integer productCount) {
			this.productCount = productCount;
		}

		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}

		public List<AlbumVO> getKnowledgeVideos() {
			return knowledgeVideos;
		}

		public void setKnowledgeVideos(List<AlbumVO> knowledgeVideos) {
			this.knowledgeVideos = knowledgeVideos;
		}

		public List<AlbumVO> getAlbums() {
			return albums;
		}

		public void setAlbums(List<AlbumVO> albums) {
			this.albums = albums;
		}

		public List<MixSearchResultVO> getSearchResults() {
			return searchResults;
		}

		public void setSearchResults(List<MixSearchResultVO> searchResults) {
			this.searchResults = searchResults;
		}
		
	}
	
}
