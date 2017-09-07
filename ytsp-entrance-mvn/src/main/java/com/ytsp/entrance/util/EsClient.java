package com.ytsp.entrance.util;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component("esClient")
public class EsClient {

	private static Logger LOGGER = LoggerFactory.getLogger(EsClient.class);
	//TODO 测试环境
//	private static final String INDEX = "ikan-test";
	//TODO 正式环境
	private static String INDEX = "ikan-new-v1";
	public static final String TYPE_GOODS = "ikan-products";
	public static final String TYPE_ALBUMS = "ikan-albums";
	
	static Settings settings = ImmutableSettings.settingsBuilder()
			.put( "cluster.name" , "ikan-es" )
			.put( "client.transport.sniff" , true)
			.put( "client.transport.ignore_cluster_name", true).build();
	
	private static TransportClient CLIENT;
	@Value("${es.retry_on_confict}")
	private int  RETRY_ON_CONFLICT;
	@Value("${es.bulk.batch.size}")
	private int BATCH_SIZE;
	@Value("${es.bulk.actions}")
	private int BULK_ACTION;
	@Value("${es.hosts}")
	private String esHost;
	
	@PostConstruct
	protected void init() {
		try {
			Class<?> clazz = Class.forName(TransportClient.class.getName());
			Constructor<?> constructor = clazz.getDeclaredConstructor(new Class[] { Settings.class });
			constructor.setAccessible(true);
			String[] esHosts = esHost.split(";");
			CLIENT = (TransportClient) constructor.newInstance(new Object[] { settings });
			for(String h : esHosts){
				String host = h.split(":")[0];
				int port = Integer.parseInt(h.split(":")[1]);
				CLIENT.addTransportAddress(new InetSocketTransportAddress(host,port));
			}
			INDEX = ImagePropertyUtil.getPropertiesValue("searchIndex").trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @return
	 */
	public BulkProcessor getBulkProcessor(){
		return BulkProcessor.builder(CLIENT, new BulkProcessor.Listener() {
			
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				LOGGER.info( String.format( "exeid: [%s], beforeBulk. action size : %s ", executionId, request.numberOfActions() ) );
			}
			
			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				LOGGER.error( "[afterBulk] exeid : " + executionId + " error.", failure );
			}
			
			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if( response.hasFailures() ) LOGGER.info( String.format( "exeid: [%s], afterBulk. response : %s ", executionId, response.buildFailureMessage() ) );
			}
		}).setConcurrentRequests(Runtime.getRuntime().availableProcessors() )
		.setBulkActions(BULK_ACTION)
		.setFlushInterval( TimeValue.timeValueSeconds( 30 ) )
		.build();
	}
	
	/**
	 * es client
	 * @return
	 */
	public Client getIndexClient(){
		return CLIENT;
	}
	
	public SearchRequestBuilder createSearchRequestBuilder(String type) {
		Client client = this.getIndexClient();
		return client.prepareSearch( INDEX ).setExplain(true).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setTypes( type );
	}
	public SearchRequestBuilder createMixSearchRequestBuilder() {
		Client client = this.getIndexClient();
		return client.prepareSearch( INDEX ).setExplain(true).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
	}
	
	public SuggestRequestBuilder createSearchSuggestionRequestBuilder(String type) {
		Client client = this.getIndexClient();
		return client.prepareSuggest( INDEX );
	}
	
	public SearchResponse moreLikeThis(String id) {
		Client client = this.getIndexClient();
		MoreLikeThisRequestBuilder mlt = new MoreLikeThisRequestBuilder(client, INDEX, TYPE_GOODS, id);
		mlt.setField("product_name","product_category","brand");
		mlt.setMinTermFreq(1);
		mlt.setSearchSize(20);
		SearchResponse response = client.moreLikeThis(mlt.request()).actionGet();  
		return response;
	}
	
	public SearchResponse searchHotProducts(int size){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		SearchResponse response = (SearchResponse) client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","product_name","pic_default","product_sale_price","product_market_price","sale_num","quarter_sale_num")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2))
				.addSort("sale_num", SortOrder.DESC).setSize(size).execute().actionGet();
		return response;
	}
	
	/**
	* <p>功能描述:分页获取热门商品</p>
	* <p>参数：@param size
	* <p>参数：@return</p>
	* <p>返回类型：SearchResponse</p>
	 */
	public SearchResponse searchAlbumByCategoryPage(int categroyId,SearchVO searchVO){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("album_category_id", categroyId);
		QueryBuilder qb2 = QueryBuilders.matchQuery("review_status", 1);
		QueryBuilder qb3 = null;
		if(Util.isIOSInReview(searchVO.getPlatform(), searchVO.getVersion())){
			qb3 = QueryBuilders.matchQuery("review_hide", 0);
		}else{
			QueryBuilders.matchAllQuery();
		}
//		if(MobileTypeEnum.iphone == MobileTypeEnum.valueOf(platfrom)){
//			qb3 = QueryBuilders.matchQuery("ios_uplow", 1);
//		}else if(MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platfrom)){
//			qb3 = QueryBuilders.matchQuery("android_uplow", 1);
//		}else{
//			qb3 = QueryBuilders.matchQuery("android_uplow", 1);
//		}
		SearchResponse response = (SearchResponse) client.prepareSearch( INDEX ).setTypes( TYPE_ALBUMS )
				.addFields("album_id","total_count","now_count","pic_default","album_name","play_times","album_category_id","vip")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2).must(qb3))
				.addSort("play_times", SortOrder.DESC).setSize(searchVO.getPageSize()).setFrom(searchVO.getSp()).execute().actionGet();
		return response;
	}
	
	/**
	* <p>功能描述:分页获取热门商品</p>
	* <p>参数：@param size
	* <p>参数：@return</p>
	* <p>返回类型：SearchResponse</p>
	 */
	public SearchResponse searchHotProductsByPage(int page,int pageSize){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		SearchResponse response = (SearchResponse) client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","product_name","pic_default","product_sale_price","product_market_price","product_status")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2))
				.addSort("sale_num", SortOrder.DESC).setSize(pageSize).setFrom(page).execute().actionGet();
		return response;
	}
	
	public SearchResponse searchHotProducts(int rootCategoryId,int size){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		QueryBuilder qb3 = QueryBuilders.matchQuery("root_category_id", rootCategoryId);
		SearchResponse response = (SearchResponse) client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","product_name","pic_default","product_sale_price","product_market_price","sale_num","quarter_sale_num")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2).must(qb3))
				.addSort("sale_num", SortOrder.DESC).setSize(size).execute().actionGet();
		return response;
	}
	
	public SearchResponse searchItemViewProducts(Integer rootCategoryId,int size){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		QueryBuilder qb3 = QueryBuilders.matchQuery("root_category_id", rootCategoryId);
		SearchResponse response = (SearchResponse) client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","view_num","product_name","pic_default","product_sale_price","product_market_price","sale_num","quarter_sale_num")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2).must(qb3))
				.addSort("view_num", SortOrder.DESC).setSize(size).execute().actionGet();
		return response;
	}
	
	public SearchResponse searchQuarterHotProducts(int size){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		SearchResponse response = (SearchResponse) client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","product_name","pic_default","product_sale_price","product_market_price","sale_num","quarter_sale_num")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2))
				.addSort("quarter_sale_num", SortOrder.DESC).setSize(size).execute().actionGet();
		return response;
	}
	
	public SearchResponse searchIndexProducts(int categoryId,int size){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		QueryBuilder qb3 = QueryBuilders.matchQuery("product_category_id", categoryId);
		SearchRequestBuilder srb = client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","product_name","pic_default","product_sale_price","product_market_price")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2).must(qb3) )
				.setSize(size);
		SearchResponse response = (SearchResponse) srb.execute().actionGet();
		return response;
	}
	
	/**
	* <p>功能描述:分页获取分类的商品</p>
	* <p>参数：@param categoryId
	* <p>参数：@param size
	* <p>参数：@return</p>
	* <p>返回类型：SearchResponse</p>
	 */
	public SearchResponse searchIndexProductsByPage(int categoryId,int page,int size){
		Client client = this.getIndexClient();
		QueryBuilder qb1 = QueryBuilders.matchQuery("product_status", 1);
		QueryBuilder qb2 = QueryBuilders.matchQuery("store_status", 1);
		//TODO 正式环境请修改
//		QueryBuilder qb3 = QueryBuilders.matchQuery("product_category_id", categoryId);
		QueryBuilder qb3 = QueryBuilders.matchQuery("product_category_bak", categoryId);
		
		SearchRequestBuilder srb = client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_code","product_name","pic_default","product_sale_price","product_market_price","product_status","vip_price")
				.setQuery(QueryBuilders.boolQuery().must(qb1).must(qb2).must(qb3) )
				.setSize(size).setFrom(page);
		SearchResponse response = (SearchResponse) srb.execute().actionGet();
		return response;
	}
	
	public SearchResponse suggestion(String key) {
		Client client = this.getIndexClient();
		AndFilterBuilder fliterBuilder =  FilterBuilders.andFilter();
		fliterBuilder.add(FilterBuilders.termFilter( "product_status",1));
		QueryBuilder qb = QueryBuilders.multiMatchQuery(key,new String[] { "product_name", "product_name_pinyin", "product_name_pinyin_index" }).type(MatchQueryBuilder.Type.PHRASE_PREFIX);
		SearchResponse response = (SearchResponse)client.prepareSearch( INDEX ).setTypes( TYPE_GOODS )
				.addFields("product_name","brand","product_code").setSize(10).setQuery(QueryBuilders.filteredQuery( qb, fliterBuilder )).execute().actionGet();
		return response;
	}
	
//	public SearchResponse search(FilterBuilder builder,String sort,int pageSize,int total) {
//		Client client = this.getIndexClient();
//		
//		
//        
//		SearchResponse response = client.prepareSearch( INDEX )
//				.setTypes( TYPE_GOODS )
//				.setPostFilter(builder)
//				//.setPostFilter( fliterBuilder.buildAsBytes() )
//				.addAggregation(AggregationBuilders.terms("brand_aggs").field("brand").size(Integer.MAX_VALUE).subAggregation(
//			            AggregationBuilders.terms("prop_val_0_aggs").field("prop_val_0").size(Integer.MAX_VALUE).subAggregation(
//			            		AggregationBuilders.terms("prop_val_1_aggs").field("prop_val_1").size(Integer.MAX_VALUE).subAggregation(
//			            				AggregationBuilders.terms("prop_val_2_aggs").field("prop_val_2").size(Integer.MAX_VALUE)
//			            				)
//			            )
//			        ))
//				.setFrom( pageSize ).setSize(total)
//				.addSort(sort, SortOrder.DESC )
//				.execute().actionGet();
//		return response;
//	}
	
	/**
	 * 
	 */
	public void search(){
		Client client = this.getIndexClient();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder.must( QueryBuilders.termsQuery( "brand_name.raw", "百思图", "百丽" ) );
		//queryBuilder.must( QueryBuilders.queryString( "拖鞋" ) );
//		queryBuilder.should(  QueryBuilders.termQuery( "brands._id", "hVpM" ) );
//		builder.setQuery( queryBuilder );
//		SearchResponse response = builder.execute().actionGet();
		//QueryBuilders.filteredQuery(queryBuilder, )
		AndFilterBuilder fliterBuilder =  FilterBuilders.andFilter( FilterBuilders.termsFilter( "brand_name.raw", "百思图", "百丽" ));
		fliterBuilder.add( FilterBuilders.termFilter( "commodity_status", 2 ) );
		//fliterBuilder.add( FilterBuilders.termFilter( "", "" ) );
		fliterBuilder.add( FilterBuilders.existsFilter( "default_pic" ) );
		fliterBuilder.add( FilterBuilders.existsFilter( "pic_small" ) );
		fliterBuilder.add( FilterBuilders.rangeFilter( "sale_price" ).from( 10 ).to( 300 ) );
		fliterBuilder.add( FilterBuilders.queryFilter( QueryBuilders.queryString( "牛皮" ) ));
		
		//AggregationBuilders.count( "aaa" ).field( "seo_en_brand_name" ).
		
		SearchResponse response = client.prepareSearch( INDEX )
		.setTypes( TYPE_GOODS )
		.setQuery( QueryBuilders.filteredQuery( queryBuilder, fliterBuilder ) )
		//.setPostFilter( fliterBuilder.buildAsBytes() )
		.addAggregation( AggregationBuilders.terms( "mysqll1l" ).field( "seo_en_brand_name" ).size( Integer.MAX_VALUE ) .order( Terms.Order.count( false ) ))
		.setFrom( 0 ).setSize( 10 )
		.addSort( "score_a", SortOrder.DESC )
		.execute().actionGet();
		SearchHits hits = response.getHits();
		SearchHit[] searchHits = hits.getHits();
		System.out.println( hits.getTotalHits() + " ------------");
		for( SearchHit hit  : searchHits ){
			System.out.println( hit.getSourceAsString() );
		}
		Aggregations aggreations = response.getAggregations();
		Map<String, Aggregation> map = aggreations.asMap();
		for( Map.Entry<String, Aggregation> e : map.entrySet() ){
			InternalTerms t =  (InternalTerms) e.getValue();
			for( Bucket b : t.getBuckets()){
				 System.out.println( b.getKey() );
				 System.out.println( b.getDocCount());
			}
		}
		System.out.println( 1 );
		
	}
	

	public void suggestion() {
		CompletionSuggestionBuilder suggestionsBuilder = new CompletionSuggestionBuilder( 
				"complete"); 
		suggestionsBuilder.text("乐高"); 
		suggestionsBuilder.field("product_name"); 
		suggestionsBuilder.size(10); 
		Client client = this.getIndexClient();
		SuggestResponse resp = client.prepareSuggest(INDEX) 
				.addSuggestion(suggestionsBuilder).execute().actionGet();
		System.out.println(resp);
	}
	
}
