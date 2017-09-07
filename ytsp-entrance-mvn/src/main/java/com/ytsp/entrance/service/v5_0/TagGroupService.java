package com.ytsp.entrance.service.v5_0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.TagGroupDao;
import com.ytsp.db.domain.TagGroup;
import com.ytsp.db.enums.TagStatusEnum;
import com.ytsp.db.enums.TagUseTypeEnum;
import com.ytsp.db.exception.SqlException;

@Service("tagGroupService")
@Transactional
public class TagGroupService {
	
	@Resource(name = "tagGroupDao")
	private TagGroupDao tagGroupDao;
	
	/**
	* <p>功能描述:获取所有TagGroup Map</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Map<Integer,TagGroup></p>
	 */
	public Map<Integer,TagGroup> getTagGroupMap(TagStatusEnum status,TagUseTypeEnum type) throws SqlException{
		List<TagGroup> groups = tagGroupDao.findAllByHql(" WHERE status = ? and useType = ? order by sortNum ", new Object[]{status,type});
		
		return buildTagGroupMap(groups);
	}
	
	/**
	* <p>功能描述:获取所有TagGroup</p>
	* <p>参数：@param status
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<TagGroup></p>
	 */
	public List<TagGroup> getTagGroupList(TagStatusEnum status,TagUseTypeEnum type) throws SqlException{
		List<TagGroup> groups = tagGroupDao.findAllByHql(" WHERE status = ? and useType = ? order by sortNum ", new Object[]{status,type});
		return groups;
	}
	
	/**
	* <p>功能描述:构建标签组Map,key为groupId,value为TagGroup对象</p>
	* <p>参数：@param groups 
	* <p>参数：@return</p>
	* <p>返回类型：Map<Integer,TagGroup></p>
	 */
	private Map<Integer,TagGroup> buildTagGroupMap(List<TagGroup> groups){
		Map<Integer,TagGroup> ret = new HashMap<Integer, TagGroup>();
		if(groups == null || groups.size() == 0){
			return ret;
		}
		for (TagGroup tagGroup : groups) {
			ret.put(tagGroup.getId(), tagGroup);
		}
		return ret;
	}
}
