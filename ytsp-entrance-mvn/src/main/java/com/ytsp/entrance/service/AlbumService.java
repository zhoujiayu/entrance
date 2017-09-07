package com.ytsp.entrance.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.Channel;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.DateFormatter;

/**
 * @author GENE
 * @description 专辑服务
 */
public class AlbumService {
	static final Logger logger = Logger.getLogger(AlbumService.class);
	private AlbumDao albumDao;
	
	public JSONObject getAlbumJson(int aid) throws Exception {
//		MemberService ms = SystemInitialization.getApplicationContext().getBean(MemberService.class);
		Album album = this.findAlbumById(aid);
		JSONObject obj = new JSONObject();
		if(album != null){
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover());
			obj.put("startLevel", album.getScore() == null ? 0 : album.getScore());
			obj.put("aid", album.getId());
			obj.put("name", album.getName() == null ? "" : album.getName());
			obj.put("channelNames", getChannelNamesByAlbum(album));
			obj.put("years", DateFormatter.date2YearString(album.getYears()));
			obj.put("director", album.getDirector() == null ? "" : album.getDirector());
			obj.put("nowCount", album.getNowCount()==null?0:album.getNowCount());
			obj.put("starring", album.getStarring() == null ? "" : album.getStarring());
			obj.put("playtimes", album.getPlayCount() == null ? 0 : album.getPlayCount());
			obj.put("albumPoint", 0);//固定为0，已经弃用的字段
			obj.put("freeCount", album.getFreeCount() == null ? 0 : album.getFreeCount());
//			List<Video> videoList = ms.getVideoList(album.getId(), -1, -1);
			obj.put("totalCount", album.getTotalCount());
			obj.put("description", album.getDescription() == null ? "" : album.getDescription());
			obj.put("type", album.getType().getValue());
			obj.put("isCollected", false);
			obj.put("vip", Boolean.TRUE.equals(album.getVip()) ? true : false);
			obj.put("videoPoint", album.getVideoPoint());
		}
		return obj;
	}

	/**
	 * 第三方使用
	 * @return
	 * @throws Exception
	 */
	public JSONArray getAlbumArray() throws Exception {
		List<Album> albums = albumDao.findAllByHql("where ios_uplow=1 AND review=1 and id in('361','378','106','329','283','337','338','365','290','369','370','381','281','358','351','359','362','363','307','308','389','261','285','382','320','313','314','315','321','330','322','323','316','318','317','319','350','340','341','372','276','325','291','345','346','355')");
		JSONArray array = new JSONArray();
		for (Album album : albums) {
			JSONObject obj = new JSONObject();
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover());
			obj.put("startLevel", album.getScore() == null ? 0 : album.getScore());
			obj.put("aid", album.getId());
			obj.put("nowCount", album.getNowCount()==null?0:album.getNowCount());
			obj.put("name", album.getName() == null ? "" : album.getName());
			obj.put("channelNames", getChannelNamesByAlbum(album));
			obj.put("years", DateFormatter.date2YearString(album.getYears()));
			obj.put("director", album.getDirector() == null ? "" : album.getDirector());
			obj.put("starring", album.getStarring() == null ? "" : album.getStarring());
			obj.put("playtimes", album.getPlayCount() == null ? 0 : album.getPlayCount());
//			List<Video> videoList = ms.getVideoList(album.getId(), -1, -1);
			obj.put("totalCount", album.getTotalCount()==null?0:album.getTotalCount());
			obj.put("description", album.getDescription() == null ? "" : album.getDescription());
			obj.put("type", album.getType().getValue());
			obj.put("isCollected", false);
			obj.put("vip", album.getVip());
			array.put(obj);
		}
		return array;
	}
	
	/**
	 * 4.4.2版本
	 * @param platform
	 * @param cid
	 * @param searchName
	 * @param start
	 * @param limit
	 * @param mode
	 * @param version 
	 * @return
	 * @throws Exception
	 * @since v1.0
	 */
	public JSONArray getAlbumArray(String platform, int cid, String searchName, 
			int start, int limit, String mode, String version) throws Exception {
		String sql = "";
		if (cid > 0) {
			sql = "  ac.cid = " + cid;
		}
		else if (!StringUtil.isNullOrEmpty(searchName)) {
			if (!StringUtil.isNullOrEmpty(sql)) {
				sql += " and ";
			}
			sql += "a.name like '%" + searchName + "%'";
		}
		if (!StringUtil.isNullOrEmpty(sql)) {
			sql += " and ";
		}
		//1是审核通过的
		sql += " a.review = 1" ;
		//审核中需要隐藏的album
		if(isInreview(platform,version)) {
			sql += " and a.review_hide = 0 ";
		}
		if (StringUtil.isNullOrEmpty(sql)) {
			sql = "select distinct a.* from ytsp_album a left join ytsp_album_toplist_relation vstlr " +
					"on a.id = vstlr.album_id left join ytsp_album_channel ac on a.id = ac.aid ";
		} else {
			sql = "select distinct a.* from ytsp_album a left join ytsp_album_toplist_relation vstlr " +
					"on a.id = vstlr.album_id left join ytsp_album_channel ac on a.id = ac.aid where " + sql;
		}
		sql += " order by -vstlr.sort_index desc,a.id";
		if(limit==-1)
			limit = Integer.MAX_VALUE;
		List<Album> albums = albumDao.sqlFetch(sql,Album.class,start, limit);
		JSONArray array = new JSONArray();
		for (Album album : albums) {
			JSONObject obj = new JSONObject();
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover());
			obj.put("startLevel", album.getScore() == null ? 0 : album.getScore());
			obj.put("aid", album.getId());
			obj.put("cid", cid);
			obj.put("nowCount", album.getNowCount()==null?0:album.getNowCount());
			obj.put("name", album.getName() == null ? "" : album.getName());
			obj.put("channelNames", getChannelNamesByAlbum(album));
			obj.put("years", DateFormatter.date2YearString(album.getYears()));
			obj.put("director", album.getDirector() == null ? "" : album.getDirector());
			obj.put("starring", album.getStarring() == null ? "" : album.getStarring());
			obj.put("playtimes", album.getPlayCount() == null ? 0 : album.getPlayCount());
//			List<Video> videoList = ms.getVideoList(album.getId(), -1, -1);
			obj.put("totalCount", album.getTotalCount()==null?0:album.getTotalCount());
			obj.put("description", album.getDescription() == null ? "" : album.getDescription());
			obj.put("type", album.getType().getValue());
			obj.put("isCollected", false);
			obj.put("vip", album.getVip());
			array.put(obj);
		}
		return array;
	}
	
	static boolean isInreview(String platform,String version ){
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		if(platform.equals("iphone"))
		{
			 isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			 inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
			 inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
		}
		boolean _isInReview = false;
		if(StringUtil.isNotNullNotEmpty(isInReview)){
			_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true : false;
		}
		if(_isInReview)
		{
			//如果在审核中
			if(inReviewPlatform.equals(platform) && inReviewVersion.equals(version))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}
	
	public int getAlbumCount(String platform, int cid, String searchName,String version) throws Exception {
		String sql = "";
		//a.review = 1是审核通过
		if (cid > 0) {
			sql = "SELECT COUNT(distinct a.id) FROM ytsp_album a,ytsp_album_channel ac " +
					"WHERE ac.cid = "+cid+" and ac.aid=a.id and  a.review = 1";
		}
		else	if (!StringUtil.isNullOrEmpty(searchName)) {
			sql = "SELECT COUNT(distinct a.id) FROM ytsp_album a WHERE a.name like" +
					" '%" + searchName + "%' and  a.review = 1";
		}
		else{
			sql = "SELECT COUNT(distinct a.id) FROM ytsp_album a where 1=1 ";
		}
		
		//审核中需要隐藏的album
		if(isInreview(platform,version))
			sql += " AND a.review_hide = 0 ";
		Object obj = null ;
		try {
			obj = albumDao.executeSql(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(obj!=null)
			return ((BigInteger)obj).intValue();
		return 0 ;
	}
	
	public int getAlbumTopListCount(String platform, String version) throws Exception {
		//1是审核通过
		String sql = "select count(1) from ytsp_album WHERE review = 1 ";
		//审核中需要隐藏的album
		if(isInreview(platform,version))
			sql += " AND review_hide = 0 ";
		Object obj = albumDao.executeSql(sql);
		if(obj!=null)
			return ((BigInteger)obj).intValue();
		return 0 ;
	}
	
	public JSONArray getAlbumTopListArray(String platform, int start, int limit, String version) throws Exception {
//		MemberService ms = SystemInitialization.getApplicationContext().getBean(MemberService.class);
		String sql = "select distinct a.* from ytsp_album a left join ytsp_album_toplist_relation vstlr " +
				"on a.id = vstlr.album_id where a.review=1 ";
		
		//审核中需要隐藏的album
		if(isInreview(platform,version))
			sql += " and a.review_hide = 0 ";
		
		sql += " ORDER BY -vstlr.sort_index DESC,a.id";
		List<Album> albums = albumDao.sqlFetch(sql, Album.class, start, limit);
		JSONArray array = new JSONArray();
		for (int i =0;i<(albums.size()<15?albums.size():15);i++) {
			Album album = albums.get(i) ;
			JSONObject obj = new JSONObject();
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover());
			obj.put("startLevel", album.getScore() == null ? 0 : album.getScore());
			obj.put("aid", album.getId());
			obj.put("name", album.getName() == null ? "" : album.getName());
			obj.put("channelNames", getChannelNamesByAlbum(album));
			obj.put("years", DateFormatter.date2YearString(album.getYears()));
			obj.put("director", album.getDirector() == null ? "" : album.getDirector());
			obj.put("starring", album.getStarring() == null ? "" : album.getStarring());
			obj.put("playtimes", album.getPlayCount() == null ? 0 : album.getPlayCount());
//			List<Video> videoList = ms.getVideoList(album.getId(), -1, -1);
			obj.put("totalCount",album.getTotalCount()==null?0:album.getTotalCount());
			obj.put("description", album.getDescription() == null ? "" : album.getDescription());
			obj.put("type", album.getType().getValue());
			obj.put("nowCount", album.getNowCount()==null?0:album.getNowCount());
			obj.put("isCollected", false);
			obj.put("vip", Boolean.TRUE.equals(album.getVip()) ? true : false);
			array.put(obj);
		}
		return array;
	}
	
	private String getChannelNamesByAlbum(Album album){
		Set<Channel> channels = album.getChannels();
		String names = "";
		int count = 0;
		//只放两个名称
		for (Channel channel : channels) {
			if(count>1)
				break;
			names += "," + channel.getName(); 
			count++;
		}
		if(names.startsWith(",")){
			names = names.substring(1);
		}
		return names;
	}

	public void saveAlbum(Album album) throws Exception {
		albumDao.save(album);
	}

	public void saveOrUpdate(Album album) throws Exception {
		albumDao.saveOrUpdate(album);
	}

	public void updateAlbum(Album album) throws Exception {
		albumDao.update(album);
	}

	public void deleteAlbum(Album album) throws Exception {
		albumDao.delete(album);
	}

	public Album findAlbumById(int albumid) throws Exception {
		return albumDao.findById(albumid);
	}

	public List<Album> getAllAlbums() throws Exception {
		return albumDao.getAll();
	}

	public void deleteAlbumById(int albumid) throws Exception {
		albumDao.deleteById(albumid);
	}

	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}
	
}
