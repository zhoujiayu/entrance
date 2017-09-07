/* 
 * $Id: Notification.java 1210 2011-09-25 08:26:38Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.coolmind.util.XmlUtil;
import com.ytsp.entrance.recharge.alipay.model.adapter.IntegerStringAdapter;
import com.ytsp.entrance.recharge.alipay.model.adapter.StringBidDecimalAdapter;
import com.ytsp.entrance.recharge.alipay.model.adapter.StringDateAdapter;
import com.ytsp.entrance.recharge.alipay.model.adapter.TBooleanNativeAdapter;

@XmlRootElement(name = "notify")
@XmlAccessorType(XmlAccessType.FIELD)
public class Notification {

    private static final JAXBContext ctx;
    static {
        try {
            ctx = JAXBContext.newInstance(Notification.class);
        } catch (JAXBException e) {
            throw new RuntimeException("jaxb2 initialize FAILED", e);
        }
    }

    public static Notification unmarshal(String xml) throws JAXBException {
        Unmarshaller un = ctx.createUnmarshaller();
        return (Notification) un.unmarshal(XmlUtil.getDocumentFromString(xml));
    }

    @XmlElement(name = "payment_type")
    private String paymentType;

    @XmlElement(name = "subject")
    private String subject;

    @XmlElement(name = "trade_no")
    private String alipayTradeId;

    @XmlElement(name = "buyer_email")
    private String buyerEmailOrPhone;

    @XmlElement(name = "gmt_create")
    @XmlJavaTypeAdapter(StringDateAdapter.class)
    private Date createDate;

    @XmlElement(name = "notify_type")
    private String notifyType;

    @XmlElement(name = "quantity")
    @XmlJavaTypeAdapter(IntegerStringAdapter.class)
    private Integer quantity;

    @XmlElement(name = "out_trade_no")
    @XmlJavaTypeAdapter(IntegerStringAdapter.class)
    private Integer tradeId;

    @XmlElement(name = "notify_time")
    @XmlJavaTypeAdapter(StringDateAdapter.class)
    private Date notifyTime;

    @XmlElement(name = "seller_id")
    private String sellerId;

    @XmlElement(name = "trade_status")
    private String tradeStatus; // TODO enum?

    @XmlElement(name = "is_total_fee_adjust")
    @XmlJavaTypeAdapter(TBooleanNativeAdapter.class)
    private Boolean totalFeeAdjust;

    @XmlElement(name = "total_fee")
    @XmlJavaTypeAdapter(StringBidDecimalAdapter.class)
    private BigDecimal totalFee;

    @XmlElement(name = "gmt_payment")
    @XmlJavaTypeAdapter(StringDateAdapter.class)
    private Date payDate;

    @XmlElement(name = "seller_email")
    private String sellerEmailOrPhone;

    @XmlElement(name = "gmt_close")
    @XmlJavaTypeAdapter(StringDateAdapter.class)
    private Date tradeEnd;

    @XmlElement(name = "price")
    @XmlJavaTypeAdapter(StringBidDecimalAdapter.class)
    private BigDecimal unitPrice;

    @XmlElement(name = "buyer_id")
    private String buyerId;

    @XmlElement(name = "notify_id")
    private String notifyId;

    @XmlElement(name = "use_coupon")
    @XmlJavaTypeAdapter(TBooleanNativeAdapter.class)
    private Boolean useCoupon;

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAlipayTradeId() {
        return alipayTradeId;
    }

    public void setAlipayTradeId(String alipayTradeId) {
        this.alipayTradeId = alipayTradeId;
    }

    public String getBuyerEmailOrPhone() {
        return buyerEmailOrPhone;
    }

    public void setBuyerEmailOrPhone(String buyerEmailOrPhone) {
        this.buyerEmailOrPhone = buyerEmailOrPhone;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getTradeId() {
        return tradeId;
    }

    public void setTradeId(Integer tradeId) {
        this.tradeId = tradeId;
    }

    public Date getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(Date notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public Boolean isTotalFeeAdjust() {
        return totalFeeAdjust;
    }

    public void setTotalFeeAdjust(Boolean totalFeeAdjust) {
        this.totalFeeAdjust = totalFeeAdjust;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public String getSellerEmailOrPhone() {
        return sellerEmailOrPhone;
    }

    public void setSellerEmailOrPhone(String sellerEmailOrPhone) {
        this.sellerEmailOrPhone = sellerEmailOrPhone;
    }

    public Date getTradeEnd() {
        return tradeEnd;
    }

    public void setTradeEnd(Date tradeEnd) {
        this.tradeEnd = tradeEnd;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }

    public Boolean isUseCoupon() {
        return useCoupon;
    }

    public void setUseCoupon(Boolean useCoupon) {
        this.useCoupon = useCoupon;
    }

    public static void main(String[] args) throws Exception {
        String xml = "<notify>"+
                        "<payment_type>1</payment_type>"+
                        "<subject>笔记本</subject>"+
                        "<trade_no>2011092518662244</trade_no>"+
                        "<buyer_email>15989536356</buyer_email>"+
                        "<gmt_create>2011-09-25 12:04:45</gmt_create>"+
                        "<notify_type>trade_status_sync</notify_type>"+
                        "<quantity>1</quantity>"+
                        "<out_trade_no>123456</out_trade_no>"+
                        "<notify_time>2011-09-25 12:04:53</notify_time>"+
                        "<seller_id>2088701162312122</seller_id>"+
                        "<trade_status>TRADE_FINISHED</trade_status>"+
                        "<is_total_fee_adjust>N</is_total_fee_adjust>"+
                        "<total_fee>0.01</total_fee>"+
                        "<gmt_payment>2011-09-25 12:04:53</gmt_payment>"+
                        "<seller_email>kandongman@yeah.net</seller_email>"+
                        "<gmt_close>2011-09-25 12:04:53</gmt_close>"+
                        "<price>0.01</price>"+
                        "<buyer_id>2088702141656446</buyer_id>"+
                        "<notify_id>d814f76e9221e3a4f2b46b402244177e04</notify_id>"+
                        "<use_coupon>N</use_coupon>"+
                    "</notify>";
        Notification notification = Notification.unmarshal(xml);
        System.out.println(notification);
    }
}
