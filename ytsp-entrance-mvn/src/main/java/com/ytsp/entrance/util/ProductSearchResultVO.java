package com.ytsp.entrance.util;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

public class ProductSearchResultVO {
	
	public long totalNum;
	
	public List<ProductResultVO> productResultVOs;
	
	public void buildProductResultVO(SearchResponse sr) {
		productResultVOs = new ArrayList<ProductResultVO>();
		SearchHits hits = sr.getHits();
		totalNum = hits.getTotalHits();
		ProductResultVO pvo = null;
		for (SearchHit sh : hits.getHits()) {
			pvo = new ProductResultVO();
			pvo.productCode = sh.field("product_code").getValue();
			pvo.imageUrl = (sh.field("pic_default") != null ? (String)sh.field("pic_default").getValue() : "");
			pvo.marketPrice = sh.field("product_market_price").getValue();
			pvo.productName = sh.field("product_name").getValue();
			pvo.salePrice = sh.field("product_sale_price").getValue();
			pvo.storageStatus = sh.field("store_status").getValue();
			productResultVOs.add(pvo);
		}
	}
	
	public long getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(long totalNum) {
		this.totalNum = totalNum;
	}

	public List<ProductResultVO> getProductResultVOs() {
		return productResultVOs;
	}

	public void setProductResultVOs(List<ProductResultVO> productResultVOs) {
		this.productResultVOs = productResultVOs;
	}





	public class ProductResultVO {
		public String productName;
		public String imageUrl;
		public Double marketPrice;
		public Double salePrice;
		public Integer productCode;
		private boolean promotion;
		private boolean pricedown;
		private int storageStatus;
		
		public boolean isPricedown() {
			return pricedown;
		}
		
		public void setPricedown(boolean pricedown) {
			this.pricedown = pricedown;
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
		public String getImageUrl() {
			return imageUrl;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public Double getMarketPrice() {
			return marketPrice;
		}
		public void setMarketPrice(Double marketPrice) {
			this.marketPrice = marketPrice;
		}
		public Double getSalePrice() {
			return salePrice;
		}
		public void setSalePrice(Double salePrice) {
			this.salePrice = salePrice;
		}
		public boolean isPromotion() {
			return promotion;
		}
		public void setPromotion(boolean promotion) {
			this.promotion = promotion;
		}

		public int getStorageStatus() {
			return storageStatus;
		}

		public void setStorageStatus(int storageStatus) {
			this.storageStatus = storageStatus;
		}
		
	}
	
	public class FacetResultVO {
		public String facetGroupName;
		public List<FacetResultValueCoupleVO> facetResultValueCoupleVOs;
		
		public String getFacetGroupName() {
			return facetGroupName;
		}
		public void setFacetGroupName(String facetGroupName) {
			this.facetGroupName = facetGroupName;
		}
		public List<FacetResultValueCoupleVO> getFacetResultValueCoupleVOs() {
			return facetResultValueCoupleVOs;
		}
		public void setFacetResultValueCoupleVOs(
				List<FacetResultValueCoupleVO> facetResultValueCoupleVOs) {
			this.facetResultValueCoupleVOs = facetResultValueCoupleVOs;
		}
		
	}
	
	public class FacetResultValueCoupleVO {
		public String facetValueName;
		public long count;
		
		public String getFacetValueName() {
			return facetValueName;
		}
		public void setFacetValueName(String facetValueName) {
			this.facetValueName = facetValueName;
		}
		public long getCount() {
			return count;
		}
		public void setCount(long count) {
			this.count = count;
		}
		
		
	}
}
