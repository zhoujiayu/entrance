package com.ytsp.entrance.service;

import java.net.URLEncoder;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.dao.RecommendDao;
import com.ytsp.db.domain.Recommend;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.AesEncrypt;

/**
 * @author GENE
 * @description 推荐服务
 */
public class RecommendService {
	static final Logger logger = Logger.getLogger(RecommendService.class);

	private RecommendDao recommendDao;
	
	public JSONArray getRecommendArray(MobileTypeEnum plat,HeadInfo head) throws Exception{
		List<Recommend> recommends = recommendDao.findAllByHql(
				" WHERE valid = ? AND recommendType = ? ORDER BY sort ASC", 
				new Object[]{ValidStatusEnum.VALID,RecommendTypeEnum.IKANCARTOON});
		JSONArray array = new JSONArray();
		for (Recommend recommend : recommends) {
			String name = "";
			int aid = 0;
			int albumType = 0;
			if(recommend.getAlbum() != null){
				name = recommend.getAlbum().getName() == null ? "" : recommend.getAlbum().getName();
				aid = recommend.getAlbum().getId();
				albumType = recommend.getAlbum().getType().getValue();
			}
			JSONObject obj = new JSONObject();
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + recommend.getImg());
			obj.put("summary", recommend.getSummary() == null ? "" : recommend.getSummary());
			String redirect = recommend.getRedirect();
			//把用户ID加进url，用以跳转网页访问时标示用户
			JSONObject jo = new  JSONObject();
			jo.put("userid", head.getUid());
			jo.put("terminal", head.getUniqueId());
			jo.put("via", "REC");
			if(redirect!=null &&!redirect.trim().equals("")){
				if(redirect.contains("?"))
					redirect = redirect+"&JSON="+URLEncoder.encode(
							AesEncrypt.encrypt(null, jo.toString()),"UTF-8");
				else
					redirect = redirect+"?JSON="+URLEncoder.encode(
							AesEncrypt.encrypt(null, jo.toString()),"UTF-8");
			}
			obj.put("redirect", redirect == null ? "" : redirect);
			obj.put("name", name);
			obj.put("aid", aid);
			obj.put("albumType", albumType);
			obj.put("type", recommend.getType() == null ? "" : recommend.getType().getValue());
			obj.put("rid", recommend.getId());
			array.put(obj);
		}
		return array;
	}
	
	public JSONArray getRecommendArrayByRecommendType(MobileTypeEnum plat,HeadInfo head,RecommendTypeEnum recommendType) throws Exception{
		
		String bigver = head.getVersion().split("\\.")[0];
		//不合法的版本标示一律按4.0来算
		if(bigver==null||bigver.equals("")||bigver.length()>1){
			bigver = "4";
		}else if(bigver.charAt(0)<48||bigver.charAt(0)>57){//表示不是数字
			bigver = "4";
		}
		List<Recommend> recommends = recommendDao.findAllByHql(
				" WHERE valid = ? AND recommendType = ? and version="+bigver+" ORDER BY sort ASC", 
				new Object[]{ValidStatusEnum.VALID,recommendType});
		JSONArray array = new JSONArray();
		for (Recommend recommend : recommends) {
			JSONObject obj = new JSONObject();
			String name = "";
			int albumId=0,activityId=0,productId=0,albumType=0,ebActivityId=0;
			if(recommend.getAlbum() != null){
				albumId=recommend.getAlbum().getId();
				albumType=recommend.getAlbum().getType().getValue();
				name=recommend.getAlbum().getName();
			}
			if(recommend.getActivity()!=null){
				activityId=recommend.getActivity().getActivity_id();
			}
			if(recommend.getEbProduct()!=null){
				productId=recommend.getEbProduct().getProductCode();
			}
			if(recommend.getEbActivity()!=null){
				ebActivityId=recommend.getEbActivity().getActivityId();
			}
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + recommend.getImg());
			obj.put("summary", recommend.getSummary() == null ? "" : recommend.getSummary());
			obj.put("productId",productId);
			obj.put("ebActivityId",ebActivityId);
			obj.put("activityId", activityId);
			obj.put("recommendId", recommend.getId());
			obj.put("albumId", albumId);
			obj.put("albumName", name);
			obj.put("albumType", albumType);
			String redirect = recommend.getRedirect();
			//把用户ID加进url，用以跳转网页访问时标示用户
			JSONObject jo = new  JSONObject();
			jo.put("userid", head.getUid());
			jo.put("terminal", head.getUniqueId());
			jo.put("via", "REC");
			if(redirect!=null &&!redirect.trim().equals("")){
//				if(redirect.contains("?"))
//					redirect = redirect+"&JSON="+URLEncoder.encode(
//							AesEncrypt.encrypt(null, jo.toString()),"UTF-8");
//				else
//					redirect = redirect+"?JSON="+URLEncoder.encode(
//							AesEncrypt.encrypt(null, jo.toString()),"UTF-8");
				if(redirect.contains("?")){
					redirect = redirect+head.getUniqueId();
				}
			}
			obj.put("url", redirect == null ? "" : redirect);
			obj.put("redirect", redirect == null ? "" : redirect);
			obj.put("type", recommend.getType() == null ? "" : recommend.getType().getValue());
			obj.put("rid", recommend.getId());
			array.put(obj);
		}
		return array;
	}
	public void saveRecommend(Recommend recommend) throws Exception {
		recommendDao.save(recommend);
	}
	
	public void saveOrUpdate(Recommend recommend) throws Exception {
		recommendDao.saveOrUpdate(recommend);
	}

	public void updateRecommend(Recommend recommend) throws Exception {
		recommendDao.update(recommend);
	}

	public void deleteRecommend(Recommend recommend) throws Exception {
		recommendDao.delete(recommend);
	}

	public Recommend findRecommendById(int recommendid) throws Exception {
		return recommendDao.findById(recommendid);
	}
	
	public List<Recommend> getAllRecommends() throws Exception {
		return recommendDao.getAll();
	}

	public void deleteRecommendById(int recommendid) throws Exception {
		recommendDao.deleteById(recommendid);
	}
	
	public RecommendDao getRecommendDao() {
		return recommendDao;
	}

	public void setRecommendDao(RecommendDao recommendDao) {
		this.recommendDao = recommendDao;
	}


}
