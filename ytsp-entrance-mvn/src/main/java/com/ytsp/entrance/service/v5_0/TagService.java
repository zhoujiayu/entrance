package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.TagDao;
import com.ytsp.db.domain.Tag;

@Service("tagService")
@Transactional
public class TagService {
	
	@Resource(name = "tagDao")
	private TagDao tagDao;
	
	/**
	* <p>功能描述:获取ytsp_tag_group下的所有标签</p>
	* <p>参数：@return</p>
	* <p>返回类型：List<Tag></p>
	 */
	public List<Tag> getTagsByType(int type){
		StringBuffer sql = new StringBuffer();
		sql.append(" select t.* from ytsp_tag t,ytsp_tag_group g ");
		sql.append(" where g.id = t.groupId and groupId is NOT NULL and t.status = 1 ");
		sql.append(" and g.status = 1 and g.useType = ").append(type);
		sql.append(" order by g.sortNum,t.sortNum ");
		
		return tagDao.sqlFetch(sql.toString(), Tag.class, -1, -1);
	}
	
	/**
	* <p>功能描述:根据groupId获取所有tag标签</p>
	* <p>参数：@param groupId
	* <p>参数：@param type
	* <p>参数：@return</p>
	* <p>返回类型：List<Tag></p>
	 */
	public List<Tag> getTagsByGroupId(Integer groupId,int type){
		StringBuffer sql = new StringBuffer();
		sql.append(" select t.* from ytsp_tag t,ytsp_tag_group g ");
		sql.append(" where g.id = t.groupId and groupId =").append(groupId).append(" and t.status = 1 ");
		sql.append(" and g.status = 1 and g.useType = ").append(type);
		sql.append(" order by g.sortNum,t.sortNum ");
		return tagDao.sqlFetch(sql.toString(), Tag.class, -1, -1);
	}
	
}
