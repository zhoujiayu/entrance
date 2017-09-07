package com.statistics.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.statistics.entity.Statistics;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbOrderPromotionRecord;
import com.ytsp.db.domain.EbShoppingCart;
import com.ytsp.db.domain.FeedbackRecord;
import com.ytsp.db.domain.LogVideo;
import com.ytsp.db.vo.PromotionVO;
import com.ytsp.db.vo.ShoppingCartVO;
import com.ytsp.entrance.util.ImagePropertyUtil;

@Repository("statisticsDao")
public class StatisticsDao {
//	@Resource
//	private MongoTemplate mongoTemplate;

	private static Logger log = Logger.getLogger(StatisticsDao.class);

	@SuppressWarnings("rawtypes")
	public void insert(Statistics statistics) {
//		try {
//			ExclusionStrategy myExclusionStrategy = new ExclusionStrategy() {
//				@Override
//				public boolean shouldSkipField(FieldAttributes fa) {
//					return fa.getName().equals("parent")||fa.getName().equals("entity")||fa.getName().equals("owner");
//				}
//
//				@Override
//				public boolean shouldSkipClass(Class<?> clazz) {
//					return false;
//				}
//			};
//			Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy).create();
//			Object eneity = statistics.getEntity();
//			
//			statistics.setEntity(null);
//			HashMap textOJ = gson.fromJson(gson.toJson(statistics), HashMap.class);
//			//处理循环调用问题
//			dealCirculation(eneity);
////			if(eneity != null){
////				System.out.println("className:"+eneity.getClass());
////			}
//			//处理时间字段为Date类型
//			convertTimestamp2Date(eneity);
//			if(eneity != null){
//				textOJ.put("entity", eneity);
//			}
//			String collectionName = ImagePropertyUtil.getPropertiesValue("mongoLogCollection").trim();
//			mongoTemplate.insert(textOJ, collectionName);
//		} catch (Exception e) {
//			log.error("StatisticsDao Exception is error", e);
//		}
	}
	
	/**
	* <p>功能描述:通过反射将TimeStamp类型的数据转换为Date类型</p>
	* <p>参数：@param eneity</p>
	* <p>返回类型：void</p>
	 */
	private void dealTimestamp(Object eneity){
		if (eneity == null) {
			return;
		}
		try {
			Class clazz = eneity.getClass();
			Class superClazz = clazz.getSuperclass();
			Field[] fileds = superClazz.getDeclaredFields();
			if(fileds == null || fileds.length <=0){
				fileds =  clazz.getDeclaredFields();
			}else{
				clazz = superClazz;
			}
			if(fileds == null || fileds.length <=0){
				return;
			}
			for (int i = 0; i < fileds.length; i++) {
				Field f = fileds[i];
				String name = fileds[i].getName();
				name = name.substring(0, 1).toUpperCase() + name.substring(1); 
				// 设置属性是可以访问的
				f.setAccessible(true);
				Object value = f.get(eneity);
				if(value == null){
					continue;
				}
				if(value instanceof Timestamp){
					Date time = new Date(((Timestamp) value).getTime());
					Method m = clazz.getMethod("set"+name,Date.class);
                    m.invoke(eneity, time);
				}
			}
		} catch (Exception e) {
			log.error("deal timestamp error ", e);
		}
	
	}
	
	/**
	* <p>功能描述:将TimeStamp时间类型转换为Date类型</p>
	* <p>参数：@param entity</p>
	* <p>返回类型：void</p>
	 */
	private void convertTimestamp2Date(Object entity){
		if(entity == null){
			return;
		}
		if(entity instanceof EbShoppingCart){
			dealTimestamp(entity);
		}else if(entity instanceof Baby){
			dealTimestamp(entity);
			Customer cust = ((Baby) entity).getCustomer();
			if (cust != null) {
				dealTimestamp(cust);
				((Baby) entity).setCustomer(cust);
			}
		}else if(entity instanceof ShoppingCartVO){
			dealTimestamp(entity);
			if(((ShoppingCartVO) entity).getPromotions() != null
					&& ((ShoppingCartVO) entity).getPromotions().size() > 0){
				List<PromotionVO> promotions = ((ShoppingCartVO) entity).getPromotions();
				for (PromotionVO p : promotions) {
					dealTimestamp(p);
				}
				 ((ShoppingCartVO) entity).setPromotions(promotions);
			}
		}else if(entity instanceof LogVideo){
			dealTimestamp(entity);
		}else if(entity instanceof CustomerLoginRecord){
			dealTimestamp(entity);
			Customer cust = ((CustomerLoginRecord) entity).getCustomer();
			if(cust != null){
				dealTimestamp(cust);
				((CustomerLoginRecord) entity).setCustomer(cust);
			}
		}else if(entity instanceof EbCoupon){
			dealTimestamp(entity);
		}else if(entity instanceof FeedbackRecord){
			dealTimestamp(entity);
			Customer cust = ((FeedbackRecord) entity).getCustomer();
			if(cust != null){
				dealTimestamp(cust);
				((FeedbackRecord) entity).setCustomer(cust);
			}
		}
		
	}
	
	/**
	* <p>功能描述:过滤掉循环调用实体</p>
	* <p>参数：@param entity</p>
	* <p>返回类型：void</p>
	 */
	private void dealCirculation(Object entity){
		if(entity instanceof EbOrder){
			dealTimestamp(entity);
			Set<EbOrderDetail> details = ((EbOrder)entity).getOrderDetails();
			for (EbOrderDetail ebOrderDetail : details) {
				ebOrderDetail.setParent(null);
				dealTimestamp(ebOrderDetail);
				for (EbOrderPromotionRecord rec: ebOrderDetail.getPromotionRecords()) {
					rec.setParent(null);
					dealTimestamp(rec);
				}
			}
			((EbOrder)entity).setOrderDetails(details);
		}
	}
}
