package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.TopicDao;
import com.ytsp.db.domain.Topic;
import com.ytsp.db.enums.TopicTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("topicService")
@Transactional
public class TopicService {

	@Resource(name = "topicDao")
	private TopicDao topicDao;

	/**
	 * <p>
	 * 功能描述:获取专题列表
	 * </p>
	 * <p>
	 * 参数：@param page 页数从0开始
	 * <p>
	 * 参数：@param pageSize
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<Topic>
	 * </p>
	 */
	public List<Topic> getTopicList(int start, int pageSize,
			TopicTypeEnum topicType) throws SqlException {
		StringBuffer sql = new StringBuffer();
		if(topicType != null){
			sql.append(" WHERE status = ? and topicType = ? order by sortNum ASC");
			return topicDao.findAllByHql(sql.toString(), start, pageSize,
					new Object[] { ValidStatusEnum.VALID, topicType });
		}else{
			sql.append(" WHERE status = ?  order by sortNum ASC ");
			return topicDao.findAllByHql(sql.toString(), start, pageSize,
					new Object[] { ValidStatusEnum.VALID});
		}
	}

	/**
	 * <p>
	 * 功能描述:根据productCode获取文章
	 * </p>
	 * <p>
	 * 参数：@param page 页数从0开始
	 * <p>
	 * 参数：@param pageSize
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<Topic>
	 * </p>
	 */
	public Topic getTopicByProductCode(int productCode, TopicTypeEnum topicType)
			throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE status = ? and topicType = ? and productCode = ? order by sortNum desc");
		List<Topic> topic = topicDao.findAllByHql(sql.toString(), new Object[] {
				ValidStatusEnum.VALID, topicType, productCode });
		if (topic == null || topic.size() <= 0) {
			return null;
		}
		return topic.get(0);
	}

	/**
	 * <p>
	 * 功能描述:根据id获取文章
	 * </p>
	 * <p>
	 * 参数：@param topicId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：Topic
	 * </p>
	 * 
	 * @throws SqlException
	 */
	public Topic getTopicById(int topicId) throws SqlException {
		return topicDao.findById(topicId);
	}

}
