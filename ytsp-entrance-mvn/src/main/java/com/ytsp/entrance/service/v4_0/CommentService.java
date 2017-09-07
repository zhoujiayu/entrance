package com.ytsp.entrance.service.v4_0;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbCommentDao;
import com.ytsp.db.domain.EbComment;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

public class CommentService {
	static final Logger logger = Logger.getLogger(CommentService.class);
	
	private EbCommentDao ebCommentDao ;
	public List<EbComment> getCommentsByProduct(int productId) throws Exception{
		List<EbComment> ls = ebCommentDao.findAllByHql(" where productId=? and valid=? order by commentTime desc",
				new Object[]{productId,ValidStatusEnum.VALID.getValue()});
		return ls;
	}

	public void submitComment(Integer uid, String account, String comment,
			int score,int productId) throws SqlException {
		EbComment c = new EbComment();
		c.setComment(comment);
		c.setUserId(uid);
		c.setProductId(productId);
		c.setScore(score);
		c.setUserName(account);
		ebCommentDao.save(c);
	}
	
	public EbCommentDao getEbCommentDao() {
		return ebCommentDao;
	}
	public void setEbCommentDao(EbCommentDao ebCommentDao) {
		this.ebCommentDao = ebCommentDao;
	}


}
