package com.ytsp.entrance.service.v3_0;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.ReviewStatusEnum;
import com.ytsp.db.enums.UpLowStatusEnum;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.DateFormatter;
//import com.ytsp.entrance.system.SystemInitialization;

/**
 * 在之前版本的基础上增加了对专辑的tag功能
 * @author GENE
 * @description 专辑服务
 */
public class AlbumServiceV3 {
	static final Logger logger = Logger.getLogger(AlbumServiceV3.class);
	private AlbumDao albumDao;
	
	
	public int getTagedAlbumCount(String platform, int cid, List<String> tagIds) throws Exception {
		String HQL = "select count(1) from ytsp_album a,ytsp_album_channel c #FROM_CLAWS# "+
				" where a.id = c.aid #WHERE_CLAWS#";
		String where = "";
		String from = "";
		if (cid > 0) {
			where += " AND c.cid="+cid;
		}
		if(tagIds.size()>0){
			for (int i=0;i<tagIds.size();i++) {
				if(i>0){
					where += " AND t"+(i-1)+".album_id=t"+i+".album_id";
				}else{
					where += " AND a.id = t0.album_id ";
				}
				where += " AND t"+i+".tag_id='"+tagIds.get(i)+"'";
				from+=",ytsp_album_tag t"+i; 
			}
		}
		where += " AND a.review = "+ReviewStatusEnum.PASS.getValue();
		if( !StringUtil.isNullOrEmpty(platform)){
			platform = platform.trim();
			MobileTypeEnum mte = null;
			try{
				mte = MobileTypeEnum.valueOf(platform);
				switch(mte){
					case ipad:;
					case iphone:{
						where += " AND ";
						where += " a.ios_uplow ="+UpLowStatusEnum.UPPER.getValue();
					}break;
					case gpad:;
					case gpadtv:;
					case gphone:{
						where += " AND ";
						where += " a.android_uplow= "+UpLowStatusEnum.UPPER.getValue();
					}break;
				}
			}catch(Exception ex){
				logger.warn("getAlbumCount error : ",ex);
			}
		}
		HQL = HQL.replace("#FROM_CLAWS#", from);
		HQL = HQL.replace("#WHERE_CLAWS#", where);
		return albumDao.sqlCount(HQL);
	}
	

	/**
	 * 
	 * @param platform
	 * @param cid
	 * @param searchName
	 * @param start
	 * @param limit
	 * @param mode
	 * @return
	 * @throws Exception
	 */
	public JSONArray getTagedAlbumArray(
			String platform, 
			int cid, 
			List<String> tagIds, 
			int start, 
			int limit, 
			String mode) throws Exception {

		String HQL = "select a.* from ytsp_album a,ytsp_album_channel c #FROM_CLAWS# "+
				" where a.id = c.aid #WHERE_CLAWS# order by a.play_count desc";
		String where = "";
		String from = "";
		if (cid > 0) {
			where += " AND c.cid="+cid;
		}
		if(tagIds.size()>0){
			for (int i=0;i<tagIds.size();i++) {
				if(i>0){
					where += " AND t"+(i-1)+".album_id=t"+i+".album_id";
				}else{
					where += " AND a.id = t0.album_id ";
				}
				where += " AND t"+i+".tag_id='"+tagIds.get(i)+"' ";
				from+=",ytsp_album_tag t"+i; 
			}
		}
		where += " AND a.review = "+ReviewStatusEnum.PASS.getValue();
		if( !StringUtil.isNullOrEmpty(platform)){
			platform = platform.trim();
			MobileTypeEnum mte = null;
			try{
				mte = MobileTypeEnum.valueOf(platform);
				switch(mte){
					case ipad:;
					case iphone:{
						where += " AND ";
						where += " a.ios_uplow ="+UpLowStatusEnum.UPPER.getValue();
					}break;
					case gpad:;
					case gpadtv:;
					case gphone:{
						where += " AND ";
						where += " a.android_uplow = "+UpLowStatusEnum.UPPER.getValue();
					}break;
				}
			}catch(Exception ex){
				logger.warn("getAlbumCount error : ",ex);
			}
		}
		HQL = HQL.replace("#FROM_CLAWS#", from);
		HQL = HQL.replace("#WHERE_CLAWS#", where);
		List<Album> albums =  albumDao.sqlFetch(HQL, Album.class,start,limit);
		JSONArray array = new JSONArray();
		for (Album album : albums) {
			JSONObject obj = new JSONObject();
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover());
			obj.put("startLevel", album.getScore() == null ? 0 : album.getScore());
			obj.put("aid", album.getId());
			obj.put("cid", cid);
			obj.put("name", album.getName() == null ? "" : album.getName());
//			obj.put("channelNames", getChannelNamesByAlbum(album));
			obj.put("years", DateFormatter.date2YearString(album.getYears()));
			obj.put("director", album.getDirector() == null ? "" : album.getDirector());
			obj.put("starring", album.getStarring() == null ? "" : album.getStarring());
			obj.put("playtimes", album.getPlayCount() == null ? 0 : album.getPlayCount());
//			List<Video> videoList = ms.getVideoList(album.getId(), -1, -1);
			obj.put("totalCount", album.getTotalCount());
			obj.put("description", album.getDescription() == null ? "" : album.getDescription());
			obj.put("type", album.getType().getValue());
			obj.put("isCollected", false);
			obj.put("vip", album.getVip());
			array.put(obj);
		}
		return array;
	}
	
	public int getAlbumTopListCount(String platform) throws Exception {
		String HQL = " WHERE review = ? ";
		List<Object> params = new ArrayList<Object>(2);
		params.add(ReviewStatusEnum.PASS);
		return albumDao.getRecordCount(HQL, params.toArray());
	}
	
	public JSONArray getAlbumTopListArray(String platform, int start, int limit) throws Exception {
//		MemberService ms = SystemInitialization.getApplicationContext().getBean(MemberService.class);
		String HQL = " WHERE album.review = ? ";
		JSONArray array = new JSONArray();
		List<Album> albums = albumDao.findAllByHql(HQL, start, limit, new Object[]{ReviewStatusEnum.PASS});
		for (Album  album : albums) {
			JSONObject obj = new JSONObject();
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover());
			obj.put("startLevel", album.getScore() == null ? 0 : album.getScore());
			obj.put("aid", album.getId());
			obj.put("name", album.getName() == null ? "" : album.getName());
//			obj.put("channelNames", getChannelNamesByAlbum(album));
			obj.put("years", DateFormatter.date2YearString(album.getYears()));
			obj.put("director", album.getDirector() == null ? "" : album.getDirector());
			obj.put("starring", album.getStarring() == null ? "" : album.getStarring());
			obj.put("playtimes", album.getPlayCount() == null ? 0 : album.getPlayCount());
//			List<Video> videoList = ms.getVideoList(album.getId(), -1, -1);
			obj.put("totalCount",album.getTotalCount());
			obj.put("description", album.getDescription() == null ? "" : album.getDescription());
			obj.put("type", album.getType().getValue());
			obj.put("isCollected", false);
			obj.put("vip", Boolean.TRUE.equals(album.getVip()) ? true : false);
			array.put(obj);
		}
		return array;
	}
	

	public Album findAlbumById(int albumid) throws Exception {
		return albumDao.findById(albumid);
	}

	public List<Album> getAllAlbums() throws Exception {
		return albumDao.getAll();
	}

	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}
	
}
