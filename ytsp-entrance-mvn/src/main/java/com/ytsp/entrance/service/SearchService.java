package com.ytsp.entrance.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.enums.ReviewStatusEnum;
import com.ytsp.db.enums.SearchOrderByEnum;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.EbProductVO;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.util.EsClient;
import com.ytsp.entrance.util.MixSearchResultVO;
import com.ytsp.entrance.util.ProductSuggestVO;
import com.ytsp.entrance.util.RecommentProductVO;
import com.ytsp.entrance.util.SearchVO;
import com.ytsp.entrance.util.Util;

@Service("searchService")
@Transactional
public class SearchService {
	
	@Resource(name = "esClient")
	private EsClient client;
	
	private static String TAGNAME_PREFIX = "tag_name_";
	
	private static String AGE_VALUE = "age_value";
	
	public void searchSuggestion(String searchKey) {
		SearchRequestBuilder srb = client.createSearchRequestBuilder(EsClient.TYPE_GOODS);
		srb.addSuggestion(new CompletionSuggestionBuilder("testSuggestions")
				.field("product_name").text(searchKey).size(20));
	}
	
	public SearchRequestBuilder buildProductSearchRequest(SearchVO searchVO
			,Map<String, String> criteriaList){
		SearchRequestBuilder srb = client.createSearchRequestBuilder(EsClient.TYPE_GOODS);
		QueryBuilder queryBuilder = null;
		if (StringUtils.isBlank(searchVO.getSk())) {
			 if(StringUtils.isNotEmpty(searchVO.getCatg())){
				  //TODO 由于正式环境分类还在用，所以暂用新增的root_category_bak_id，上线后还原回来
//				 queryBuilder = QueryBuilders.matchQuery("product_category_id", searchVO.getCatg());
				 queryBuilder = QueryBuilders.matchQuery("product_category_bak_id", searchVO.getCatg());
			  }else if(StringUtils.isNotEmpty(searchVO.getBrandraw())){
				  queryBuilder = QueryBuilders.matchQuery("brand_raw", searchVO.getBrandraw());
			  }else{
				  queryBuilder = QueryBuilders.matchAllQuery();
			  }
		}else {
			//优化搜索，将输入的关键字，按照分词器进行分词后，再拼接
			queryBuilder = getProductQueryBuilder(searchVO);
		}
		AndFilterBuilder andfliterBuilder = setProductFilters(searchVO,criteriaList);
		srb.setQuery(QueryBuilders.filteredQuery(queryBuilder, andfliterBuilder));
		//设置排序
		setProductOrderBy(searchVO, srb);
		// 设置分页.
		setProductPage(searchVO, srb);
		// 设置需要获取的属性.
		setProductFields(srb);
		return srb;
	}
	
	/**
	* <p>功能描述:构建视频搜索请求</p>
	* <p>参数：@param searchVO
	* <p>参数：@param criteriaList
	* <p>参数：@return</p>
	* <p>返回类型：SearchRequestBuilder</p>
	 */
	public SearchRequestBuilder buildAlbumSearchRequest(SearchVO searchVO
			,Map<String, String> criteriaList){
		SearchRequestBuilder srb = client.createSearchRequestBuilder(EsClient.TYPE_ALBUMS);
		QueryBuilder queryBuilder = null;
		if (StringUtils.isBlank(searchVO.getSk())) {
			queryBuilder = QueryBuilders.matchAllQuery();
		}else {
			//优化搜索，将输入的关键字，按照分词器进行分词后，再拼接
			queryBuilder = getAlbumQueryBuilder(searchVO);
		}
		AndFilterBuilder andfliterBuilder = setAlbumsFilters(searchVO,criteriaList);
		srb.setQuery(QueryBuilders.filteredQuery(queryBuilder, andfliterBuilder));
		//设置排序
		setAlbumOrderBy(searchVO, srb);
		// 设置分页.
		setAlbumPage(searchVO, srb);
		// 设置需要获取的属性.
		setAlbumFields(srb);
		return srb;
	}
	
	public String[] getAllSk(String serarchSk){
        //创建分词对象  
        StringReader sr = new StringReader(serarchSk);  
        IKSegmenter ik = new IKSegmenter(sr, true); 
        Lexeme lex = null;
        try{
        	List<String> tmpKey = new ArrayList<String>();
        	while((lex=ik.next())!=null){ 
        		tmpKey.add(lex.getLexemeText());
            }
        	return tmpKey.toArray(new String[0]);

        }catch(Exception e){
        }finally{
        	sr.close();
        }
          
		return null;
	}

	
	/**
	* <p>功能描述:构建混合搜索请求参数</p>
	* <p>参数：@param keywords
	* <p>参数：@return</p>
	* <p>返回类型：SearchRequestBuilder</p>
	 */
	private SearchRequestBuilder buildMixSearchRequestBuilder(SearchVO searchVO){
		SearchRequestBuilder srb = client.createMixSearchRequestBuilder();
		QueryBuilder queryBuilder = null;
		if(searchVO.getSk() == null || "".equals(searchVO.getSk().trim())){
			queryBuilder = QueryBuilders.matchAllQuery();
		}else{
			//优化搜索，将输入的关键字，按照分词器进行分词后，再拼接
			queryBuilder = getMixQueryBuilder(searchVO);
		}
		AndFilterBuilder andFliterBuilder = FilterBuilders.andFilter();
		OrFilterBuilder fliterBuilder = FilterBuilders.orFilter();
		OrFilterBuilder productStatus = FilterBuilders.orFilter();
		OrFilterBuilder albumStatus = FilterBuilders.orFilter();
		productStatus.add(FilterBuilders.termFilter("product_status", 1));
//		albumStatus.add(FilterBuilders.termFilter("review_status", 1));
		andFliterBuilder.add(FilterBuilders.termFilter("review_status", 1));
		//苹果审核中，根据review_hide隐藏要审核通过的视频
		if(Util.isIOSInReview(searchVO.getPlatform(), searchVO.getVersion())){
			andFliterBuilder.add(FilterBuilders.termFilter("review_hide", 0));
		}
		albumStatus.add(andFliterBuilder);
		fliterBuilder.add(productStatus);
		fliterBuilder.add(albumStatus);
		srb.setQuery(QueryBuilders.filteredQuery(queryBuilder,fliterBuilder));
		//增加先按匹配度排序，然后id排序，否则搜索结果可能有相同的结果
		srb.addSort("_score",SortOrder.DESC);
		// 设置分页.
		setPage(searchVO, srb);
		//设置返回字段
		setMixFields(srb);
		
		return srb;
	}
	
	/**
	* <p>功能描述:混合搜索所有视频和商品</p>
	* <p>参数：@param searchVO
	* <p>参数：@param keywords</p>
	* <p>返回类型：void</p>
	 */
	public List<MixSearchResultVO> mixSearch(SearchVO searchVO,String version,String platform){
		SearchRequestBuilder srb = buildMixSearchRequestBuilder(searchVO);
		SearchResponse response = srb.execute().actionGet();
		List<MixSearchResultVO> minxSearchResult = new ArrayList<MixSearchResultVO>();
		if (response != null) {
			SearchHits hits = response.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					MixSearchResultVO mixVO = new MixSearchResultVO();
					if(sh.getType() != null && EsClient.TYPE_GOODS.equals(sh.getType())){
						mixVO.setProductCode((Integer) sh.field("product_code").getValue());
						mixVO.setName((String) sh.field("product_name").getValue());
						mixVO.setImageSrc(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
//						mixVO.setImageSrc(Util.getFullImageURL(mixVO.getImageSrc()));
						mixVO.setImageSrc(Util.getFullImageURLByVersion(mixVO.getImageSrc(), version, platform));
						mixVO.setIkanPrice((Double) sh.field("product_sale_price").getValue());
						mixVO.setVprice((Double) sh.field("vip_price").getValue());
						mixVO.setStorageStatus((Integer)sh.field("store_status").getValue());
						mixVO.setType(1);
						
						minxSearchResult.add(mixVO);
					}else if(sh.getType() != null && EsClient.TYPE_ALBUMS.equals(sh.getType())){
						mixVO.setAlbumId((Integer) sh.field("album_id").getValue());
						mixVO.setAge(parseString(sh.field(TAGNAME_PREFIX+IConstants.ANIMEAGEGROUPID) == null ?"":sh.field(TAGNAME_PREFIX+IConstants.ANIMEAGEGROUPID).getValue()));
						mixVO.setAlbumCount(parseInteger(sh.field("total_count") == null?null:sh.field("total_count").getValue()));
						mixVO.setAlbumType((String) sh.field("album_category").getValue());
						mixVO.setName((String) sh.field("album_name").getValue());
						mixVO.setNowCount(parseInteger(sh.field("now_count").getValue()));
						mixVO.setVip(sh.field("vip").getValue() == null?false:(Boolean)sh.field("vip").getValue());
						mixVO.setImageSrc(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
//						mixVO.setImageSrc(Util.getFullImageURL(mixVO.getImageSrc()));
						mixVO.setImageSrc(Util.getFullImageURLByVersion(mixVO.getImageSrc(), version, platform));
						Integer specialType = parseInteger(sh.field("special_type").getValue());
						if(specialType == 0){
							mixVO.setType(2);
						}else{
							mixVO.setType(3);
						}
						mixVO.setAge(parseString(sh.field(AGE_VALUE) == null ?"":sh.field(AGE_VALUE).getValue()));
						if(StringUtil.isNotNullNotEmpty(mixVO.getAge()) && isAgeFormatRight(mixVO.getAge())){
							mixVO.setAge(mixVO.getAge().replaceAll("岁", "").replaceAll("-", "~")+"岁");
						}else if(mixVO.getAge().indexOf("-") != -1){
							mixVO.setAge(mixVO.getAge().replaceAll("-", "~"));
						}
						minxSearchResult.add(mixVO);
					}
				}
			}
		}
		
		return minxSearchResult;
	}

	public List<EbProductVO> searchProduct(SearchVO searchVO,Map<String, String> criteriaList,String version,String platform) {
		SearchRequestBuilder srb = buildProductSearchRequest(searchVO,criteriaList);
		SearchResponse response = srb.execute().actionGet();
		//搜索返回数据
		List<EbProductVO> products = new ArrayList<EbProductVO>();
		SearchHits hits = response.getHits();
		for (SearchHit sh : hits.getHits()) {
			EbProductVO vo = new EbProductVO();
			vo.setImgUrl(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
			//设置图片全路径
//			vo.setImgUrl(Util.getFullImageURL(vo.getImgUrl()));
			vo.setImgUrl(Util.getFullImageURLByVersion(vo.getImgUrl(), version, platform));
			vo.setVprice((Double) sh.field("product_sale_price").getValue());
			vo.setSvprice((Double) sh.field("vip_price").getValue());
			vo.setPrice((Double) sh.field("product_market_price").getValue());
			vo.setStatus((Integer) sh.field("product_status").getValue());
			vo.setProductCode((Integer) sh.field("product_code").getValue());
			vo.setProductName((String) sh.field("product_name").getValue());
			vo.setStorageStatus((Integer)sh.field("store_status").getValue());
			//获取搜索结果的总数量
			vo.setTotalNumber(hits.getTotalHits());
			products.add(vo);
		}
		return products;
	}
	
	/**
	* <p>功能描述:设置商品过滤</p>
	* <p>参数：@param searchVO
	* <p>参数：@param criteriaList
	* <p>参数：@return</p>
	* <p>返回类型：AndFilterBuilder</p>
	 */
	private AndFilterBuilder dealProductFilters(SearchVO searchVO,Map<String, List<String>> criteriaList){
		AndFilterBuilder fliterBuilder = FilterBuilders.andFilter();
		if (StringUtils.isNotEmpty(searchVO.getPriceSpan())) {
			String[] prices = searchVO.getPriceSpan().split("-");
			if (prices.length == 2) {
				try {
					Double start = Double.parseDouble(prices[0]);
					Double end = Double.parseDouble(prices[1]);
					if (start < end) {
						fliterBuilder.add(FilterBuilders
								.rangeFilter("product_sale_price")
								.from(start.doubleValue())
								.to(end.doubleValue()));
					}else if(start.doubleValue() == end.doubleValue()){
						fliterBuilder.add(FilterBuilders.termFilter("product_sale_price", start.doubleValue()));
					}
				} catch (Exception e) {

				}
			}
		}
		fliterBuilder.add(FilterBuilders.termFilter("product_status", 1));
		if(searchVO.getCatg() != null){
			fliterBuilder.add(FilterBuilders.termFilter("product_category_id", searchVO.getCatg()));
		}
		for(String tagName:criteriaList.keySet()){
			List<String> tagValue = criteriaList.get(tagName);
			for (String value : tagValue) {
				if(StringUtils.isNoneEmpty(value)){
					fliterBuilder.add(FilterBuilders.termFilter(tagName,
							value));
				}
			}
		}
		return fliterBuilder;
	}
	
	/**
	* <p>功能描述:构建查询请求</p>
	* <p>参数：@param searchVO
	* <p>参数：@param criteriaList
	* <p>参数：@return</p>
	* <p>返回类型：SearchRequestBuilder</p>
	 */
	public SearchRequestBuilder buildProductSearchRequestBuilder(SearchVO searchVO
			,Map<String, List<String>> criteriaList){
		SearchRequestBuilder srb = client.createSearchRequestBuilder(EsClient.TYPE_GOODS);
		QueryBuilder queryBuilder = null;
		if (StringUtils.isBlank(searchVO.getSk())) {
			 if(StringUtils.isNotEmpty(searchVO.getCatg())){
				 //TODO 由于正式环境分类还在用，所以暂用新增的root_category_bak_id，上线后还原回来
//				 queryBuilder = QueryBuilders.matchQuery("product_category_id", searchVO.getCatg());
				 queryBuilder = QueryBuilders.matchQuery("product_category_bak_id", searchVO.getCatg());
			  }else if(StringUtils.isNotEmpty(searchVO.getBrandraw())){
				  queryBuilder = QueryBuilders.matchQuery("brand_raw", searchVO.getBrandraw());
			  }else{
				  queryBuilder = QueryBuilders.matchAllQuery();
			  }
		}else {
			queryBuilder = getProductQueryBuilder(searchVO);
		}
		AndFilterBuilder andfliterBuilder = dealProductFilters(searchVO,criteriaList);
		srb.setQuery(QueryBuilders.filteredQuery(queryBuilder, andfliterBuilder));
		//设置排序
		setProductOrderBy(searchVO, srb);
		// 设置分页.
		setProductPage(searchVO, srb);
		// 设置需要获取的属性.
		setProductFields(srb);
		// 设置Facet.
		//setAggregation(srb, searchVO);
		//System.out.println(srb.toString());
		return srb;
	}
	
	/**
	* <p>功能描述:根据综合条件搜索商品</p>
	* <p>参数：@param searchVO
	* <p>参数：@param criteriaList
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProductVO></p>
	 */
	public List<EbProductVO> searchProductByCondition(SearchVO searchVO,Map<String, List<String>> criteriaList) {
		SearchRequestBuilder srb = buildProductSearchRequestBuilder(searchVO,criteriaList);
		SearchResponse response = srb.execute().actionGet();
		//搜索返回数据
		List<EbProductVO> products = new ArrayList<EbProductVO>();
		SearchHits hits = response.getHits();
		for (SearchHit sh : hits.getHits()) {
			EbProductVO vo = new EbProductVO();
			vo.setImgUrl(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
			vo.setVprice((Double) sh.field("product_sale_price").getValue());
			vo.setSvprice((Double) sh.field("vip_price").getValue());
			vo.setPrice((Double) sh.field("product_market_price").getValue());
			vo.setStatus((Integer) sh.field("product_status").getValue());
			vo.setProductCode((Integer) sh.field("product_code").getValue());
			vo.setProductName((String) sh.field("product_name").getValue());
			
			products.add(vo);
		}
		
		return products;
	}
	
	/**
	* <p>功能描述:视频搜索</p>
	* <p>参数：@param searchVO
	* <p>参数：@param criteriaList
	* <p>参数：@return</p>
	* <p>返回类型：AlbumSearchResultVO</p>
	 */
	public List<AlbumVO> searchAlbum(SearchVO searchVO,Map<String, String> criteriaList,String version,String platform) {
		SearchRequestBuilder srb = buildAlbumSearchRequest(searchVO,criteriaList);
		SearchResponse response = srb.execute().actionGet();
		List<AlbumVO> albums = new ArrayList<AlbumVO>();
		if (response != null) {
			SearchHits hits = response.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					AlbumVO vo = new AlbumVO();
					vo.setId((Integer) sh.field("album_id").getValue());
					vo.setName((String) sh.field("album_name").getValue());
					vo.setNowCount(parseInteger(sh.field("now_count").getValue()));
					vo.setSnapshot(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
//					vo.setSnapshot(Util.getFullImageURL(vo.getSnapshot()));
					vo.setSnapshot(Util.getFullImageURLByVersion(vo.getSnapshot(), version, platform));
					vo.setTotalCount(parseInteger(sh.field("total_count").getValue()));
					vo.setVip(sh.field("vip").getValue() == null?false:(Boolean)sh.field("vip").getValue());
					vo.setAge(parseString(sh.field(AGE_VALUE) == null ?"":sh.field(AGE_VALUE).getValue()));
					vo.setTypeName(sh.field("album_category") != null ? (String)sh.field("album_category").getValue() : "");
					if(StringUtil.isNotNullNotEmpty(vo.getAge()) && isAgeFormatRight(vo.getAge())){
						vo.setAge(vo.getAge().replaceAll("岁", "").replaceAll("-", "~")+"岁");
					}else if(vo.getAge().indexOf("-") != -1){
						vo.setAge(vo.getAge().replaceAll("-", "~"));
					}
					vo.setTotalNumber(hits.getTotalHits());
					albums.add(vo);
				}
			}
		}
		return albums;
	}
	
	/**
	* <p>功能描述:年龄格式是否正确</p>
	* <p>参数：@param age
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isAgeFormatRight(String age){
		Pattern var = Pattern.compile("^\\w+[岁][-]\\w+[岁]$");
		return var.matcher(age).matches();
	}
	
	@SuppressWarnings("unused")
	private boolean isHaveFont(String words,int time,char key){
		if(StringUtil.isNullOrEmpty(words)){
			return false;
		}
		int length = words.length();
		int count = 0;
		for (int i = 0; i < length; i++) {
			char c = words.charAt(i);
			if(key == c){
				count++;
			}
		}
		if(time == count){
			return true;
		}
		return false;
	}

	/**
	 * 设置需要获取的属性.
	 * 
	 * @param searchBuilder
	 */
	private void setProductFields(SearchRequestBuilder searchBuilder) {
		searchBuilder.addFields("brand","brand_raw", "id",
				"product_code", "product_name", "product_status",
				"product_market_price", "product_sale_price",
				"pic_default", "size", "vendor","store_status","vip_price","rebate");
	}
	
	/**
	* <p>功能描述:获取混合搜索返回的字段</p>
	* <p>参数：@param searchBuilder</p>
	* <p>返回类型：void</p>
	 */
	private void setMixFields(SearchRequestBuilder searchBuilder) {
		searchBuilder.addFields("product_code", "product_name","product_market_price","size","vip_price",
				"product_sale_price","album_id","album_name", "album_category","total_count",
				"now_count", "pic_default","special_type","vip",TAGNAME_PREFIX+IConstants.AGEGROUPID,TAGNAME_PREFIX+IConstants.ANIMEAGEGROUPID,AGE_VALUE,"store_status");
	}
	
	/**
	* <p>功能描述:获取视频返回的字段</p>
	* <p>参数：@param searchBuilder</p>
	* <p>返回类型：void</p>
	 */
	private void setAlbumFields(SearchRequestBuilder searchBuilder) {
		searchBuilder.addFields("album_id","album_name", "total_count",
				"now_count", "pic_default","vip","album_category",TAGNAME_PREFIX+IConstants.ANIMEAGEGROUPID,AGE_VALUE,"review_hide");
	}
	

	private void setProductOrderBy(SearchVO searchVO,
			SearchRequestBuilder searchBuilder) {
		if (StringUtils.isNotBlank(searchVO.getOby())) {
			if (SearchOrderByEnum.PRICE.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("product_sale_price",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			} else if (SearchOrderByEnum.SCORE.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("score",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			} else if (SearchOrderByEnum.SALENUM.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("_score", SortOrder.DESC).addSort("sale_num",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			}else if (SearchOrderByEnum.NEWONSHELF.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("product_onshelf_time",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			}else if (SearchOrderByEnum.DISCOUNT.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort(
						"rebate",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			}else{
				searchBuilder.addSort("_score", SortOrder.DESC)
				.addSort("sale_num", SortOrder.DESC)
				.addSort("score", SortOrder.DESC);
			}
		} else {
			searchBuilder.addSort("_score", SortOrder.DESC)
			.addSort("sale_num", SortOrder.DESC)
			.addSort("score", SortOrder.DESC);
		}
	}
	
	/**
	* <p>功能描述:设置视频排序</p>
	* <p>参数：@param searchVO
	* <p>参数：@param searchBuilder</p>
	* <p>返回类型：void</p>
	 */
	private void setAlbumOrderBy(SearchVO searchVO,SearchRequestBuilder searchBuilder) {
		if (StringUtils.isNotBlank(searchVO.getOby())) {
			if (SearchOrderByEnum.COMPOSITE.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("play_times",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("review_time",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			} else if (SearchOrderByEnum.HOTPLAY.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("play_times",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			} else if (SearchOrderByEnum.NEWONSHELF.getValue().equals(parseInteger(searchVO.getOby()))) {
				searchBuilder.addSort("_score", SortOrder.DESC).addSort("review_time",
						searchVO.isDesc() ? SortOrder.DESC : SortOrder.ASC);
			}else{
				searchBuilder.addSort("_score", SortOrder.DESC)
				.addSort("play_times", SortOrder.DESC)
				.addSort("review_time", SortOrder.DESC)
				.addSort("album_id", SortOrder.ASC);
			}
		} else {
			searchBuilder.addSort("_score", SortOrder.DESC)
			.addSort("play_times", SortOrder.DESC)
			.addSort("review_time", SortOrder.DESC)
			.addSort("album_id", SortOrder.ASC);
		}
	
	}

	/**
	 * 设置term aggregation( facet )
	 * 
	 * @param params
	 * @param searchBuilder
	 */
	/*
	private void setAggregation(SearchRequestBuilder searchBuilder,
			SearchVO searchVO) {
		// 品牌
		searchBuilder.addAggregation(this.aggreation("brand"));
		if (StringUtils.isBlank(searchVO.getNld()))
			searchBuilder.addAggregation(this.aggreation("prop_val_0"));
		if (StringUtils.isBlank(searchVO.getCz()))
			searchBuilder.addAggregation(this.aggreation("prop_val_1"));
		if (StringUtils.isBlank(searchVO.getPs())) {
			searchBuilder.addAggregation(this.aggreation("price_span"));
		}
	}*/
	
	
	private AndFilterBuilder setAlbumsFilters(SearchVO searchVO,Map<String, String> criteriaList){
		AndFilterBuilder fliterBuilder = FilterBuilders.andFilter();
		if(Util.isIOSInReview(searchVO.getPlatform(), searchVO.getVersion())){
			fliterBuilder.add(FilterBuilders.termFilter("review_hide",0));
		}
		fliterBuilder.add(FilterBuilders.termFilter("review_status",ReviewStatusEnum.PASS.getValue()));
//		if (searchVO.getIosUplow()!=null) {
//			fliterBuilder.add(FilterBuilders.termFilter("ios_uplow",searchVO.getIosUplow()));
//		}
//		if (searchVO.getAndroidUplow()!=null) {
//			fliterBuilder.add(FilterBuilders.termFilter("android_uplow",searchVO.getAndroidUplow()));
//		}
		if (searchVO.getSpecialType()!=null) {
			fliterBuilder.add(FilterBuilders.termFilter("special_type",searchVO.getSpecialType()));
		}
		//设置分类条件
		if (searchVO.getCatg()!=null) {
			fliterBuilder.add(FilterBuilders.termFilter("album_category_id",searchVO.getCatg()));
		}
		for(String tagName:criteriaList.keySet()){
			String tagValue = criteriaList.get(tagName);
			if(StringUtils.isNoneEmpty(tagValue)){
				fliterBuilder.add(FilterBuilders.termFilter(TAGNAME_PREFIX+tagName,
						tagValue));
			}
		}
		return fliterBuilder;
	}
	
	/**
	* <p>功能描述:设置商品搜索过滤条件</p>
	* <p>参数：@param searchVO  搜索条件VO
	* <p>参数：@param criteriaList  存放groupId和tagid,Map的key值为groupId,value值为tagId
	* <p>参数：@return</p>
	* <p>返回类型：AndFilterBuilder</p>
	 */
	private AndFilterBuilder setProductFilters(SearchVO searchVO,Map<String, String> criteriaList){
		AndFilterBuilder fliterBuilder = FilterBuilders.andFilter();
		if (StringUtils.isNotEmpty(searchVO.getPriceSpan())) {
			String[] prices = searchVO.getPriceSpan().split("-");
			if (prices.length == 2) {
				try {
					Double start = Double.parseDouble(prices[0]);
					Double end = Double.parseDouble(prices[1]);
					if (start < end) {
						fliterBuilder.add(FilterBuilders
								.rangeFilter("product_sale_price")
								.from(start.doubleValue())
								.to(end.doubleValue()));
					}else if(start.doubleValue() == end.doubleValue()){
						fliterBuilder.add(FilterBuilders.termFilter("product_sale_price", start.doubleValue()));
					}
				} catch (Exception e) {

				}
			}
		}
		fliterBuilder.add(FilterBuilders.termFilter("product_status", 1));
		//设置商品分类id
		if(searchVO.getCatg() != null){
			//TODO 由于正式环境分类还在用，所以暂用新增的root_category_bak_id，上线后还原回来
//			fliterBuilder.add(FilterBuilders.termFilter("product_category_id", searchVO.getCatg()));
			fliterBuilder.add(FilterBuilders.termFilter("product_category_bak_id", searchVO.getCatg()));
		}
		//设置动漫周边
		if(searchVO.getProductAlbumId() != null){
			fliterBuilder.add(FilterBuilders.termFilter("product_album_id", searchVO.getProductAlbumId()));
		}
		//设置商品品牌id
		if(searchVO.getBrand() != null){
			fliterBuilder.add(FilterBuilders.termFilter("brand_id", searchVO.getBrand()));
		}
		//设置一级分类
		if(searchVO.getRootCategoryId() != null){
			//TODO 由于正式环境分类还在用，所以暂用新增的root_category_bak_id，上线后还原回来
//			fliterBuilder.add(FilterBuilders.termFilter("root_category_id", searchVO.getRootCategoryId()));
			fliterBuilder.add(FilterBuilders.termFilter("root_category_bak_id", searchVO.getRootCategoryId()));
			
		}
		//设置商品有货条件
		if (searchVO.getStorageStatus() != null) {
			fliterBuilder.add(FilterBuilders.termFilter("store_status",
					searchVO.getStorageStatus()));
		}
		for(String tagName:criteriaList.keySet()){
			String tagValue = criteriaList.get(tagName);
			if(StringUtils.isNoneEmpty(tagValue)){
				fliterBuilder.add(FilterBuilders.termFilter(TAGNAME_PREFIX+tagName,
						tagValue));
			}
		}
		return fliterBuilder;
	}
	
	/**
	* <p>功能描述:获取商品搜索查询</p>
	* <p>参数：@param searchVO
	* <p>参数：@return</p>
	* <p>返回类型：QueryBuilder</p>
	 */
	public QueryBuilder getMixQueryBuilder(SearchVO searchVO){
		String serarchSk = searchVO.getSk();
		//获取所有可能的分词
		String[] searchSks = getAllSk(serarchSk);
		//如果词里含有“的”这个字，全匹配可能匹配不到。所以匹配度降低
		Integer deKey = 0;
		//所有分词拼接
		if(searchSks != null && searchSks.length >0){
			
			BoolQueryBuilder tmpBoolQuery =  QueryBuilders.boolQuery();
			for(String value : searchSks){
				if(value.equals("的") && value.length() == 1){
					deKey += 1;
				}
				tmpBoolQuery.should(QueryBuilders.queryString(value)
						.analyzer("ik"))
				.should(QueryBuilders.regexpQuery("album_name",
						".*" + value + ".*").boost(5.0f))
				.should(QueryBuilders.regexpQuery("product_name",
						".*" +value+ ".*").boost(5.0f)) 
				.should(QueryBuilders.regexpQuery("vendor",
						value).boost(5.0f)) 
				.should(QueryBuilders.regexpQuery("brand",
						".*" + value + ".*").boost(2.0f))
				.should(QueryBuilders.regexpQuery("brand_raw",
						".*" + value + ".*").boost(2.0f))
				.should(QueryBuilders.regexpQuery("album_name_pinyin",
						".*" + value + ".*").boost(3.0f))
				.should(QueryBuilders.regexpQuery("album_name_pinyin_index",
						".*" + value + ".*").boost(4.0f))
				.should(QueryBuilders.regexpQuery("product_name_pinyin",
						".*" + value + ".*").boost(3.0f))
				.should(QueryBuilders.regexpQuery("product_name_pinyin_index",
						".*" + value + ".*").boost(4.0f));
			}
			tmpBoolQuery.minimumNumberShouldMatch(searchSks.length - deKey);
			return tmpBoolQuery;
		}
		
		return QueryBuilders.boolQuery();
	}
	
	/**
	* <p>功能描述:获取商品搜索查询</p>
	* <p>参数：@param searchVO
	* <p>参数：@return</p>
	* <p>返回类型：QueryBuilder</p>
	 */
	public QueryBuilder getAlbumQueryBuilder(SearchVO searchVO){
		String serarchSk = searchVO.getSk();
		//获取所有可能的分词
		String[] searchSks = getAllSk(serarchSk);
		Integer deKey = 0;
		//所有分词拼接
		if(searchSks != null && searchSks.length >0){
			BoolQueryBuilder tmpBoolQuery =  QueryBuilders.boolQuery();
			for(String value : searchSks){
				if(value.equals("的") && value.length() == 1){
					deKey += 1;
				}
				tmpBoolQuery.should(QueryBuilders.queryString(value)
						.analyzer("ik"))
				.should(QueryBuilders.regexpQuery("album_name",
								".*" + value + ".*").boost(5.0f))
						.should(QueryBuilders.regexpQuery("album_category",
								".*" + value + ".*").boost(3.0f)) 
						.should(QueryBuilders.regexpQuery("album_name_pinyin",
								".*" + value + ".*").boost(3.0f))
						.should(QueryBuilders.regexpQuery("album_name_pinyin_index",
								".*" + value + ".*").boost(4.0f));
			}
			tmpBoolQuery.minimumNumberShouldMatch(searchSks.length - deKey);
			return tmpBoolQuery;
		}
		
		return QueryBuilders.boolQuery();
	}
	
	
	/**
	* <p>功能描述:获取商品搜索查询</p>
	* <p>参数：@param searchVO
	* <p>参数：@return</p>
	* <p>返回类型：QueryBuilder</p>
	 */
	public QueryBuilder getProductQueryBuilder(SearchVO searchVO){
		String serarchSk = searchVO.getSk();
		//获取所有可能的分词
		String[] searchSks = getAllSk(serarchSk);
		//如果词里含有“的”这个字，全匹配可能匹配不到。所以匹配度降低
		Integer deKey = 0;
		//所有分词拼接
		if(searchSks != null && searchSks.length >0){
			BoolQueryBuilder tmpBoolQuery =  QueryBuilders.boolQuery();
			for(String value : searchSks){
				tmpBoolQuery.should(QueryBuilders.queryString(value)
						.analyzer("ik"));
				if(value.equals("的") && value.length() == 1){
					deKey += 1;
				}
				tmpBoolQuery.should(QueryBuilders.regexpQuery("product_name",
						".*" + value + ".*").boost(5.0f))
				.should(QueryBuilders.regexpQuery("vendor",
						".*" + value + ".*").boost(5.0f))
				.should(QueryBuilders.regexpQuery("brand",
						".*" + value + ".*").boost(2.0f))
				.should(QueryBuilders.regexpQuery("brand_raw",
						".*" + value + ".*").boost(2.0f))
				.should(QueryBuilders.regexpQuery("product_name_pinyin",
						".*" + value + ".*").boost(3.0f))
				.should(QueryBuilders.regexpQuery("product_name_pinyin_index",
						".*" + value + ".*").boost(4.0f));
			}
			tmpBoolQuery.minimumNumberShouldMatch(searchSks.length - deKey);
//			tmpBoolQuery.adjustPureNegative(true);
			return tmpBoolQuery;
		}
		
		return QueryBuilders.boolQuery();
	}
	
	/**
	 * 组装aggregation
	 * 
	 * @param term
	 * @return
	 */
	/*private AbstractAggregationBuilder aggreation(String term) {
		return AggregationBuilders.terms(term).field(term)
				.size(Integer.MAX_VALUE).order(Terms.Order.count(false));
	}*/

	/**
	 * 设置分页
	 * 
	 * @param searchVO
	 * @param searchBuilder
	 */
	private void setProductPage(SearchVO searchVO, SearchRequestBuilder searchBuilder) {
		int currentPage = this.positiveInt(searchVO.getSp() + "", 0);
		int pageSize = searchVO.getPageSize();
		if (currentPage < 0) {
			currentPage = 0;
		}
		searchBuilder.setFrom(currentPage* pageSize).setSize(pageSize);
	}
	
	/**
	* <p>功能描述:设置分页</p>
	* <p>参数：@param searchVO
	* <p>参数：@param searchBuilder</p>
	* <p>返回类型：void</p>
	 */
	private void setPage(SearchVO searchVO, SearchRequestBuilder searchBuilder) {
		int currentPage = searchVO.getSp();
		int pageSize = searchVO.getPageSize();
		if (currentPage < 0) {
			currentPage = 0;
		}
		searchBuilder.setFrom(currentPage * pageSize).setSize(pageSize);
	}
	
	private void setAlbumPage(SearchVO searchVO, SearchRequestBuilder searchBuilder) {
		int currentPage = searchVO.getSp();
		int pageSize = searchVO.getPageSize();
		if (currentPage < 0) {
			currentPage = 0;
		}
		searchBuilder.setFrom(currentPage * pageSize).setSize(pageSize);
	}

	/**
	 * 
	 * @param numberStr
	 * @param defaultInt
	 * @return
	 */
	private Integer positiveInt(String numberStr, Integer defaultInt) {
		if (StringUtils.isBlank(numberStr) || !StringUtils.isNumeric(numberStr))
			return defaultInt;
		int number = Integer.parseInt(numberStr);
		return number >= 0 ? number : defaultInt;
	}

	public List<ProductSuggestVO> suggestion(String key) {
		List<ProductSuggestVO> suggestVOs = new ArrayList<ProductSuggestVO>();
		SearchResponse sr = client.suggestion(key);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for (SearchHit sh : hits.getHits()) {
					ProductSuggestVO vo = new ProductSuggestVO();
					vo.setBrand((String)sh.field("brand").getValue());
					vo.setProductCode((Integer)sh.field("product_code").getValue());
					vo.setProductName((String)sh.field("product_name").getValue());
					suggestVOs.add(vo);
				}
			}
		}
		return suggestVOs;
	}

	public List<RecommentProductVO> searchRecomendProductsAfterPaySuccess(
			EbOrder ebOrder) {
		List<RecommentProductVO> recommentVOs = new ArrayList<RecommentProductVO>();
		int total = 0;
		for (EbOrderDetail ebOrderDetail : ebOrder.getOrderDetails()) {
			if (ebOrderDetail.getPromotionId() == 1) {
				// 赠品不计
				continue;
			}
			total++;
		}
		int length = 20 / total;
		int i = 0;
		for (EbOrderDetail ebOrderDetail : ebOrder.getOrderDetails()) {
			if (ebOrderDetail.getPromotionId() == 1) {
				// 赠品不计
				continue;
			}
			i++;
			SearchResponse sr = client.moreLikeThis(ebOrderDetail
					.getProductCode().toString());
			if (sr != null) {
				SearchHits hits = sr.getHits();
				if (hits != null && hits.getHits() != null) {
					int j = 0;
					for (SearchHit sh : hits.getHits()) {
						RecommentProductVO vo = new RecommentProductVO();
						vo.imgSrc = (String) sh.getSource().get("pic_default")
								.toString();
						vo.productCode = Integer.valueOf(sh.getSource()
								.get("product_code").toString());
						vo.productName = (String) sh.getSource()
								.get("product_name").toString();
						recommentVOs.add(vo);
						j++;
						if (i < total) {
							if (j >= length) {
								break;
							}
						} else if (i >= total) {
							if (j >= 20 - length * (total - 1)) {
								break;
							}
						}
					}
				}
			}
		}
		return recommentVOs;
	}
	
	/**
	 * 分类热销商品
	 * @param categoryId
	 * @param size
	 * @return
	 */
	public List<RecommentProductVO> searchHotProducts(int categoryId,int size){
		List<RecommentProductVO> hotProducts = new ArrayList<RecommentProductVO>();
		SearchResponse sr = client.searchHotProducts(categoryId,size);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					RecommentProductVO product = new RecommentProductVO();
					product.imgSrc = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					product.productCode = sh.field("product_code").getValue();
					product.productName = sh.field("product_name").getValue();
					product.salePrice = sh.field("product_sale_price").getValue();
					product.marketPrice = sh.field("product_market_price").getValue();
					product.saleNum =  sh.field("sale_num").getValue();
					product.quarterSaleNum = sh.field("quarter_sale_num").getValue();
					hotProducts.add(product);
				}
			}
		}
		return hotProducts;
	}
	
	/**
	 * 详情页推荐热销商品
	 * @param ebProduct
	 * @param size
	 * @return
	 */
	public List<RecommentProductVO> searchHotProducts(EbProduct ebProduct,int size){
		List<RecommentProductVO> hotProducts = new ArrayList<RecommentProductVO>();
		SearchResponse sr = client.searchHotProducts(ebProduct.getEbCatagory().getParent().getValue(),size+1);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				int i =0;
				for(SearchHit sh: hits.getHits()){
					int productCode = sh.field("product_code").getValue();
					if(productCode==ebProduct.getProductCode())
						continue;
					RecommentProductVO product = new RecommentProductVO();
					product.imgSrc = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					product.productCode = productCode;
					product.productName = sh.field("product_name").getValue();
					product.salePrice = sh.field("product_sale_price").getValue();
					product.marketPrice = sh.field("product_market_price").getValue();
					product.saleNum =  sh.field("sale_num").getValue();
					product.quarterSaleNum = sh.field("quarter_sale_num").getValue();
					hotProducts.add(product);
					if(++i>=size){
						break;
					}
				}
			}
		}
		return hotProducts;
	}
	

	/**
	 * 分布搜索热销商品
	 * @param size
	 * @return
	 */
	public List<EbProductVO> searchHotProductsByPage(int page,int pageSize){
		List<EbProductVO> hotProudctVOs = new ArrayList<EbProductVO>();
		SearchResponse sr = client.searchHotProductsByPage(page,pageSize);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					EbProductVO vo = new EbProductVO();
					vo.setImgUrl(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					vo.setVprice((Double) sh.field("product_sale_price").getValue());
					vo.setSvprice((Double) sh.field("product_sale_price").getValue());
					vo.setPrice((Double) sh.field("product_market_price").getValue());
					vo.setStatus((Integer) sh.field("product_status").getValue());
					vo.setProductCode((Integer) sh.field("product_code").getValue());
					vo.setProductName((String) sh.field("product_name").getValue());
					
					hotProudctVOs.add(vo);
				}
			}
		}
		return hotProudctVOs;
	}
	
	/**
	 * 热销商品
	 * @param size
	 * @return
	 */
	public List<RecommentProductVO> searchHotProducts(int size){
		List<RecommentProductVO> hotProducts = new ArrayList<RecommentProductVO>();
		SearchResponse sr = client.searchHotProducts(size);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					RecommentProductVO product = new RecommentProductVO();
					product.imgSrc = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					product.productCode = sh.field("product_code").getValue();
					product.productName = sh.field("product_name").getValue();
					product.salePrice = sh.field("product_sale_price").getValue();
					product.marketPrice = sh.field("product_market_price").getValue();
					product.saleNum =  sh.field("sale_num").getValue();
					product.quarterSaleNum = sh.field("quarter_sale_num").getValue();
					hotProducts.add(product);
				}
			}
		}
		return hotProducts;
	}
	/**
	 * 当季热销商品
	 * @param size
	 * @return
	 */
	public List<RecommentProductVO> searchQuarterHotProducts(int size){
		List<RecommentProductVO> hotProducts = new ArrayList<RecommentProductVO>();
		SearchResponse sr = client.searchQuarterHotProducts(size);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					RecommentProductVO product = new RecommentProductVO();
					product.imgSrc = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					product.productCode = sh.field("product_code").getValue();
					product.productName = sh.field("product_name").getValue();
					product.salePrice = sh.field("product_sale_price").getValue();
					product.marketPrice = sh.field("product_market_price").getValue();
					product.saleNum =  sh.field("sale_num").getValue();
					product.quarterSaleNum = sh.field("quarter_sale_num").getValue();
					hotProducts.add(product);
				}
			}
		}
		return hotProducts;
	}
	
	
	/**
	* <p>功能描述:根据分类id搜索知识和动漫视频</p>
	* <p>参数：@param categoryId 视频分类id
	* <p>参数：@param page  页数
	* <p>参数：@param pageSize 每页显示多少个
	* <p>参数：@param platfrom 平台
	* <p>参数：@return</p>
	* <p>返回类型：List<AlbumVO></p>
	 */
	public List<AlbumVO> searchAlbumByCategoryPage(int categoryId,SearchVO searchVO){
		SearchResponse sr = client.searchAlbumByCategoryPage(categoryId,searchVO);
		List<AlbumVO> albums = new ArrayList<AlbumVO>();
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					AlbumVO vo = new AlbumVO();
					vo.setId((Integer) sh.field("album_id").getValue());
					vo.setName((String) sh.field("album_name").getValue());
					vo.setNowCount(parseInteger(sh.field("now_count").getValue()));
					vo.setSnapshot(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					vo.setTotalCount(parseInteger(sh.field("total_count").getValue()));
					vo.setVip((Boolean)sh.field("vip").getValue());
//					vo.setAge(parseString(sh.field(TAGNAME_PREFIX+"13").getValue()));
					albums.add(vo);
				}
			}
		}
		
		return albums;
	}
	
	/**
	 * 分类随机商品
	 * @param categoryId
	 * @param size
	 * @return
	 */
	public List<EbProductVO> searchIndexProductsByPage(int categoryId,int page,int pageSize,String version,String platform){
		SearchResponse sr = client.searchIndexProductsByPage(categoryId,page,pageSize);
		List<EbProductVO> hotProudctVOs = new ArrayList<EbProductVO>();
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					EbProductVO vo = new EbProductVO();
					vo.setImgUrl(Util.getFullImageURLByVersion(sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "",version,platform));
					vo.setVprice((Double) sh.field("product_sale_price").getValue());
					vo.setSvprice((Double) sh.field("vip_price").getValue());
					vo.setPrice((Double) sh.field("product_market_price").getValue());
					vo.setStatus((Integer) sh.field("product_status").getValue());
					vo.setProductCode((Integer) sh.field("product_code").getValue());
					vo.setProductName((String) sh.field("product_name").getValue());
					
					hotProudctVOs.add(vo);
				}
			}
		}
		
		return hotProudctVOs;
	}
	
	/**
	 * 分类随机商品
	 * @param categoryId
	 * @param size
	 * @return
	 */
	public List<RecommentProductVO> searchIndexProducts(int categoryId,int size){
		List<RecommentProductVO> products = new ArrayList<RecommentProductVO>();
		SearchResponse sr = client.searchIndexProducts(categoryId,size);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				for(SearchHit sh: hits.getHits()){
					RecommentProductVO product = new RecommentProductVO();
					product.imgSrc = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					product.productCode = sh.field("product_code").getValue();
					product.productName = sh.field("product_name").getValue();
					product.salePrice = sh.field("product_sale_price").getValue();
					product.marketPrice = sh.field("product_market_price").getValue();
					products.add(product);
				}
			}
		}
		return products;
	}
	
	/**
	 * 详情页看了又看（热门浏览商品）
	 * @param ebProduct
	 * @param size
	 * @return
	 */
	public List<RecommentProductVO> searchItemViewProducts(EbProduct ebProduct, int size){
		List<RecommentProductVO> products = new ArrayList<RecommentProductVO>();
		SearchResponse sr = client.searchItemViewProducts(ebProduct.getEbCatagory().getParent().getValue(),size+1);
		if (sr != null) {
			SearchHits hits = sr.getHits();
			if (hits != null && hits.getHits() != null) {
				int i = 0;
				for(SearchHit sh: hits.getHits()){
					int productCode = sh.field("product_code").getValue();
					if(ebProduct.getProductCode()== productCode)
						continue;
					RecommentProductVO product = new RecommentProductVO();
					product.imgSrc = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
					product.productCode = productCode;
					product.productName = sh.field("product_name").getValue();
					product.salePrice = sh.field("product_sale_price").getValue();
					product.marketPrice = sh.field("product_market_price").getValue();
					products.add(product);
					if(++i>=size){
						break;
					}
				}
			}
		}
		return products;
	}
	
	/**
	* <p>功能描述:将obj转换成Integer类型</p>
	* <p>参数：@param obj
	* <p>参数：@return</p>
	* <p>返回类型：Integer</p>
	 */
	private Integer parseInteger(Object obj){
		if(obj == null){
			return 0;
		}else if(obj instanceof String){
			if(((String) obj).equals("")){
				return 0;
			}
			return Integer.parseInt((String) obj);
		}else if(obj instanceof Float){
			return ((Float) obj).intValue();
		}else if(obj instanceof Long){
			return ((Long) obj).intValue();
		}else if(obj instanceof Double){
			return ((Double) obj).intValue();
		}else if(obj instanceof Integer){
			return (Integer) obj;
		}else{
			return null;
		}
	}
	
	/**
	* <p>功能描述:将obj转换成String类型</p>
	* <p>参数：@param obj
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String parseString(Object obj){
		if(obj == null || "".equals(obj)){
			return "";
		}else if(obj instanceof String){
			return (String) obj;
		}else if(obj instanceof Double){
			return String.valueOf(obj);
		}else if(obj instanceof Long){
			return String.valueOf(obj);
		}else if(obj instanceof Float){
			return String.valueOf(obj);
		}else if(obj instanceof Integer){
			return String.valueOf(obj);
		}else {
			return "";
		}
	}
}
