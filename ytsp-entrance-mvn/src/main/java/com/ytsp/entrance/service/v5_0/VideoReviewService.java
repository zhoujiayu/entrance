package com.ytsp.entrance.service.v5_0;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.VideoDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.ReviewStatusEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("aideoReviewService")
@Transactional
public class VideoReviewService {
	@Resource(name = "albumDao")
	private AlbumDao albumDao;
	
	@Resource(name = "videoDao")
	private VideoDao videoDao;
	
	/**
	* <p>功能描述:获取未审核的剧集</p>
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@return</p>
	* <p>返回类型：List<Album></p>
	 */
	public List<Album> getUnReviewAlbumList(int page,int pageSize) {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from ytsp_album a ")
		   .append(" where exists (select 1 from ytsp_video v where a.id = v.album and v.review = 0) order by a.id desc ");
		return albumDao.sqlFetch(sql.toString(), Album.class, page*pageSize, pageSize);
	}
	
	/**
	* <p>功能描述:获取未审核的剧集</p>
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@return</p>
	* <p>返回类型：List<Album></p>
	 */
	public List<Video> getUnReviewVideoList(int albumId,int page,int pageSize) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from ytsp_video v where review = 0 and v.album = "+albumId+"  order by episode ");
		return videoDao.sqlFetch(sql.toString(), Video.class, page*pageSize, pageSize);
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@param videoId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Video</p>
	 */
	public Video getVideo(int videoId) throws SqlException {
		return videoDao.findById(videoId);
	}
	
	/**
	* <p>功能描述:审核视频</p>
	* <p>参数：@param videoId
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateVideo(int videoId,int type) throws SqlException{
		Video video = (Video) videoDao.getSessionFactory().getCurrentSession()
				.load(Video.class, videoId, LockMode.UPGRADE);
		if (video == null) {
			return;
		}
		//打回
		if(type == 2){
			video.setReview(ReviewStatusEnum.BACK);
		}else if(type == 1){//审核通过
			video.setReview(ReviewStatusEnum.PASS);
			//更新剧集数量
			video.setStatus(ValidStatusEnum.VALID);
			int count = this.videoDao.getRecordCount(
					"  WHERE review=? AND album.id=?", new Object[] {
							ReviewStatusEnum.PASS, video.getAlbum().getId() });
			albumDao.updateByHql(" SET totalCount=? WHERE id=?", new Object[] {
					count, video.getAlbum().getId() });
		}
		video.setReviewTime(new Date());
		video.setModifyTime(new Date());
		videoDao.update(video);
	}
}
