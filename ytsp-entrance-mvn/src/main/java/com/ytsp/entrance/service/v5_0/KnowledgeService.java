package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.AlbumCategoryDao;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.HotKnowledgeDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.AlbumCategory;
import com.ytsp.db.domain.HotKnowledge;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("knowledgeService")
@Transactional
public class KnowledgeService {
	
	@Resource(name = "hotKnowledgeDao")
	private HotKnowledgeDao hotKnowledgeDao;
	@Resource(name = "albumCategoryDao")
	private AlbumCategoryDao albumCategoryDao;
	@Resource(name = "albumDao")
	private AlbumDao albumDao;
	
	/**
	* <p>功能描述:获取热门知识</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<HotKnowledge></p>
	 */
	public List<HotKnowledge> getHotKnowledge() throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append(" select k.* from ytsp_album a,ytsp_hot_knowledge k ");
		sb.append(" where a.id = k.albumId and a.albumcategory = k.categoryid and valid = 1 order by sort asc ");
		List<HotKnowledge> hotKnowledgeList = hotKnowledgeDao.sqlFetch(sb.toString(), HotKnowledge.class, -1, -1);
		for (HotKnowledge hotKnowledge : hotKnowledgeList) {
			hotKnowledge.getAlbum();
		}
		return hotKnowledgeList;
	}
	
	public List<AlbumCategory> getAlbumCategorys() throws SqlException{
		return albumCategoryDao.findAllByHql(" WHERE 1=1 ORDER BY sortNum");
	}
	
	/**
	* <p>功能描述:获取知识的分类</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AlbumCategory></p>
	 */
	public List<AlbumCategory> getKnowledgeCategorys() throws SqlException{
		return albumCategoryDao.findAllByHql(" WHERE status = ? and level = ? AND type = ? ORDER BY sortNum",new Object[]{ValidStatusEnum.VALID,2,AlbumCategoryTypeEnum.KNOWLEDGE});
	}
	
	/**
	* <p>功能描述:获取某一级分类下的知识</p>
	* <p>参数：@param categoryId
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@param level
	* <p>参数：@param platform
	* <p>参数：@return</p>
	* <p>返回类型：List<Album></p>
	 */
	public List<Album> getKnowledgeByCategoryId(int categoryId,int page,int pageSize,int level,int specialType,String platform){
		StringBuffer sql = new StringBuffer();
		sql.append(" select a.* from ytsp_album a,ytsp_album_category c  ");
		sql.append(" where a.albumcategory = c.id and a.specialType = ").append(specialType);
		sql.append(" and a.review = 1 and c.level = ").append(level);
		sql.append(" and a.albumcategory = ").append(categoryId);
//		if(MobileTypeEnum.iphone == MobileTypeEnum.valueOf(platform)){
//			sql.append(" and a.ios_uplow = ").append(UpLowStatusEnum.UPPER.getValue());
//		}else if(MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platform)){
//			sql.append(" and a.android_uplow = ").append(UpLowStatusEnum.UPPER.getValue());
//		}
		sql.append(" order by id asc ");
		return albumDao.sqlFetch(sql.toString(), Album.class, page, pageSize);
	}
	
}
