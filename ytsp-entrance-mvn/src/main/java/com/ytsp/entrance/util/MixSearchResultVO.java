package com.ytsp.entrance.util;


public class MixSearchResultVO {
	// 商品id
	private Integer productCode = null;
	// 专辑id
	private Integer albumId = null;
	//视频分类
	private String albumType = null;
	// 类型：1：商品 2：动漫类 3：知识类
	private Integer type = null;
	// 动漫知识商品名称
	private String name = null;
	// 图片
	private String imageSrc = null;
	// 爱看价格
	private double ikanPrice;
	// vip价格
	private double vprice;
	// 产品款式
	private String size = null;
	// 适用年龄
	private String age = null;
	// 专辑总数量
	private Integer albumCount = null;
	// 更新至
	private int nowCount;
	//vip
	private boolean vip;
	//库存状态：0为缺货，1为有货
	private Integer storageStatus;
	
	public Integer getStorageStatus() {
		return storageStatus;
	}
	public void setStorageStatus(Integer storageStatus) {
		this.storageStatus = storageStatus;
	}
	public Integer getProductCode() {
		return productCode;
	}
	public void setProductCode(Integer productCode) {
		this.productCode = productCode;
	}
	public Integer getAlbumId() {
		return albumId;
	}
	public void setAlbumId(Integer albumId) {
		this.albumId = albumId;
	}
	
	public String getAlbumType() {
		return albumType;
	}
	public void setAlbumType(String albumType) {
		this.albumType = albumType;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageSrc() {
		return imageSrc;
	}
	public void setImageSrc(String imageSrc) {
		this.imageSrc = imageSrc;
	}
	public double getIkanPrice() {
		return ikanPrice;
	}
	public void setIkanPrice(double ikanPrice) {
		this.ikanPrice = ikanPrice;
	}
	public double getVprice() {
		return vprice;
	}
	public void setVprice(double vprice) {
		this.vprice = vprice;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public Integer getAlbumCount() {
		return albumCount;
	}
	public void setAlbumCount(Integer albumCount) {
		this.albumCount = albumCount;
	}
	public int getNowCount() {
		return nowCount;
	}
	public void setNowCount(int nowCount) {
		this.nowCount = nowCount;
	}
	public boolean isVip() {
		return vip;
	}
	public void setVip(boolean vip) {
		this.vip = vip;
	}
	
}
