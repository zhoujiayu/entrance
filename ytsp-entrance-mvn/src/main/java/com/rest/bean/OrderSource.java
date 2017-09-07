package com.rest.bean;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="resource") // 标注类名为XML根节点
@XmlAccessorType(XmlAccessType.FIELD) // 表示将所有域作为XML节点
public class OrderSource {
	private Long orderid;
	
	private Integer userId;
	
	private String userName;
	
	private String cellphone;
	
	private String phone;
	
	private String email;
	
	private String invoiceTitle;
	
	private String deviceCode;
	
	private Integer payType;
	
	private int  orderSource;
	
	private com.ytsp.db.enums.EbOrderTypeEnum orderType;
	
	private Integer isNeedInvoice = 0;
	
	private com.ytsp.db.enums.EbOrderStatusEnum status;
	
	private Date orderTime;
	
	private Date payTime;
	
	private Date sendTime;
	
	private Date completeTime;
	
	private Integer addressId;
	
	private Integer cityId;
	
	private Integer provinceId;
	
	private Integer areaId;
	
	private String cityName;
	
	private String provinceName;
	
	private String imgSrc;
	
	private Double shipping = 0d;
	
	private Double totalPrice;
	
	private String areaName;
	
	private String address;
	
	private com.ytsp.db.enums.PayStatusEnum payStatus;
	
	private String deliveryTime;

	public Long getOrderid() {
		return orderid;
	}

	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInvoiceTitle() {
		return invoiceTitle;
	}

	public void setInvoiceTitle(String invoiceTitle) {
		this.invoiceTitle = invoiceTitle;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public Integer getPayType() {
		return payType;
	}

	public void setPayType(Integer payType) {
		this.payType = payType;
	}

	public int getOrderSource() {
		return orderSource;
	}

	public void setOrderSource(int orderSource) {
		this.orderSource = orderSource;
	}

	public com.ytsp.db.enums.EbOrderTypeEnum getOrderType() {
		return orderType;
	}

	public void setOrderType(com.ytsp.db.enums.EbOrderTypeEnum orderType) {
		this.orderType = orderType;
	}

	public Integer getIsNeedInvoice() {
		return isNeedInvoice;
	}

	public void setIsNeedInvoice(Integer isNeedInvoice) {
		this.isNeedInvoice = isNeedInvoice;
	}

	public com.ytsp.db.enums.EbOrderStatusEnum getStatus() {
		return status;
	}

	public void setStatus(com.ytsp.db.enums.EbOrderStatusEnum status) {
		this.status = status;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}

	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}

	public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public Integer getAreaId() {
		return areaId;
	}

	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getImgSrc() {
		return imgSrc;
	}

	public void setImgSrc(String imgSrc) {
		this.imgSrc = imgSrc;
	}

	public Double getShipping() {
		return shipping;
	}

	public void setShipping(Double shipping) {
		this.shipping = shipping;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public com.ytsp.db.enums.PayStatusEnum getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(com.ytsp.db.enums.PayStatusEnum payStatus) {
		this.payStatus = payStatus;
	}

	public String getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(String deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	
	
}
