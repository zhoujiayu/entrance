package com.ytsp.entrance.util;

public class SearchVO implements Cloneable {
	//品牌id
	private String brand;
	//分类
	private String catg;
	//排序
	private String oby;
	//正序或者倒序
	private boolean desc;
	//起始页面
	private Integer sp;
	//搜索词
	private String sk;
	//品牌的简写
	private String brandraw;
	//价格范围
	private String priceSpan;
	//ios是否上线
	private Integer iosUplow;
	//安卓是否上线视频
	private Integer androidUplow;
	//album视频表里，用来区分知识视频和其他视频的标识，0为视频1为知识类视频
	private Integer specialType;
	//每页显示个数
	private Integer pageSize;
	//动漫周边id
	private Integer productAlbumId;
	//商品一级分类
	private Integer rootCategoryId;
	//平台
	private String platform;
	//版本
	private String version;
	//库存状态：0表示缺货，1表示有货
	private Integer storageStatus;
	
	public Integer getStorageStatus() {
		return storageStatus;
	}
	public void setStorageStatus(Integer storageStatus) {
		this.storageStatus = storageStatus;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Integer getRootCategoryId() {
		return rootCategoryId;
	}
	public void setRootCategoryId(Integer rootCategoryId) {
		this.rootCategoryId = rootCategoryId;
	}
	public Integer getProductAlbumId() {
		return productAlbumId;
	}
	public void setProductAlbumId(Integer productAlbumId) {
		this.productAlbumId = productAlbumId;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getPriceSpan() {
		return priceSpan;
	}
	public void setPriceSpan(String priceSpan) {
		this.priceSpan = priceSpan;
	}
	public String getBrandraw() {
		return brandraw;
	}
	public void setBrandraw(String brandraw) {
		this.brandraw = brandraw;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getCatg() {
		return catg;
	}
	public void setCatg(String catg) {
		this.catg = catg;
	}
	public String getOby() {
		return oby;
	}
	public void setOby(String oby) {
		this.oby = oby;
	}
	/*public String getPs() {
		return ps;
	}
	public void setPs(String ps) {
		this.ps = ps;
	}
	public String getCz() {
		return cz;
	}
	public void setCz(String cz) {
		this.cz = cz;
	}
	public String getNld() {
		return nld;
	}
	public void setNld(String nld) {
		this.nld = nld;
	}*/
	public Integer getSp() {
		return sp;
	}
	public void setSp(Integer sp) {
		this.sp = sp;
	}
	public String getSk() {
		return sk;
	}
	public void setSk(String sk) {
		this.sk = sk;
	}
	
	
	public boolean isDesc() {
		return desc;
	}
	public void setDesc(boolean desc) {
		this.desc = desc;
	}
	
	public Integer getIosUplow() {
		return iosUplow;
	}
	public void setIosUplow(Integer iosUplow) {
		this.iosUplow = iosUplow;
	}
	public Integer getAndroidUplow() {
		return androidUplow;
	}
	public void setAndroidUplow(Integer androidUplow) {
		this.androidUplow = androidUplow;
	}
	
	public Integer getSpecialType() {
		return specialType;
	}
	public void setSpecialType(Integer specialType) {
		this.specialType = specialType;
	}
	public SearchVO clone() throws CloneNotSupportedException {
		return (SearchVO)super.clone();
	}
	
	
}
