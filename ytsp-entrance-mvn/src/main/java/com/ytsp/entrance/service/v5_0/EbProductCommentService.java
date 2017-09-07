package com.ytsp.entrance.service.v5_0;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.audit.AuditAction;
import com.ytsp.db.dao.CreditPolicyDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.EbCommentDao;
import com.ytsp.db.dao.EbCommentImgDao;
import com.ytsp.db.dao.EbOrderDao;
import com.ytsp.db.dao.EbOrderDetailDao;
import com.ytsp.db.domain.CreditPolicy;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbComment;
import com.ytsp.db.domain.EbCommentImg;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.enums.CreditSourceTypeEnum;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.util.Util;

@Service("ebProductCommentService")
@Transactional
public class EbProductCommentService {
	private static final Logger logger = Logger
			.getLogger(EbProductCommentService.class);
	@Resource(name = "ebCommentDao")
	private EbCommentDao ebCommentDao;
	@Resource(name = "ebCommentImgDao")
	private EbCommentImgDao ebCommentImgDao;
	@Resource(name = "ebOrderDetailDao")
	private EbOrderDetailDao ebOrderDetailDao;
	@Resource(name = "ebOrderDao")
	private EbOrderDao ebOrderDao;
	@Resource(name = "creditPolicyDao")
	private CreditPolicyDao creditPolicyDao;
	@Resource(name = "customerDao")
	private CustomerDao customerDao;

	/**
	 * <p>
	 * 功能描述:获取某种评价：差评，中评，好评
	 * </p>
	 * <p>
	 * 参数：@param productCode 商品编码
	 * <p>
	 * 参数：@param commentType 评论类型：1为差评2为中评3为好评
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbComment>
	 * </p>
	 */
	public List<EbComment> getProductCommentByPage(int productCode,
			int commentType, int page, int pageSize) throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append("WHERE valid = 1 ");
		if (commentType == 1) {// 差评
			sql.append(" and score = 1 ");
		} else if (commentType == 2) {// 中评
			sql.append(" and score in(2,3) ");
		} else if (commentType == 3) {// 好评,没评分默认5星好评处理
			sql.append(" and (score in(4,5) or score is null ) ");
		}
		sql.append(" and productId =? order by id desc");
		List<EbComment> comments = ebCommentDao.findAllByHql(sql.toString(),
				page * pageSize, pageSize, new Object[] { productCode });
		return comments;
	}
	
	
	/**
	* <p>功能描述:分页查询，按时间排序</p>
	* <p>参数：@param productCode
	* <p>参数：@param commentType
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<EbComment></p>
	 */
	public List<EbComment> queryPageProductCommentByTime(int productCode,
			int commentType, int page, int pageSize) throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append("WHERE valid = 1 ");
		if (commentType == 1) {// 差评
			sql.append(" and score in (0,1)");
		} else if (commentType == 2) {// 中评
			sql.append(" and score in(2,3) ");
		} else if (commentType == 3) {// 好评,没评分默认5星好评处理
			sql.append(" and (score in(4,5) or score is null ) ");
		}
		sql.append(" and productId =? order by commentTime desc");
		List<EbComment> comments = ebCommentDao.findAllByHql(sql.toString(),
				page*pageSize, pageSize, new Object[] { productCode });
		return comments;
	}
	
	/**
	 * <p>
	 * 功能描述:获取评价数量，Key值为1，2，3（差评，中评，好评）
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：Map<Integer,Integer>
	 * </p>
	 * 
	 * @throws Exception
	 */
	public Map<Integer, Integer> getProductCommentsCount(int productCode)
			throws Exception {
		Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select IFNULL(score,5),count(1) totalComment from ytsp_ebiz_comments where valid = 1 and productid = "
				+ productCode + " group by score ");
		Session session = ebCommentDao.getSessionFactory().openSession();
		Transaction trans = session.beginTransaction();
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			List<Object[]> result = query.list();
			for (Object[] object : result) {
				String score = String.valueOf(object[0]);
				String totalComment = String.valueOf(object[1]);
				if ("1".equals(score)) {
					countCommentTotal(countMap, 1,
							Integer.parseInt(totalComment));
				} else if ("2".equals(score) || "3".equals(score)) {
					countCommentTotal(countMap, 2,
							Integer.parseInt(totalComment));
				} else if ("4".equals(score) || "5".equals(score)) {
					countCommentTotal(countMap, 3,
							Integer.parseInt(totalComment));
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			trans.commit();
			session.close();
		}
		return countMap;
	}

	/**
	 * <p>
	 * 功能描述:计算评价数量
	 * </p>
	 * <p>
	 * 参数：@param countMap
	 * <p>
	 * 参数：@param score
	 * <p>
	 * 参数：@param total
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void countCommentTotal(Map<Integer, Integer> countMap, int type,
			int total) {
		if (countMap.containsKey(type)) {
			countMap.put(type, countMap.get(type) + total);
		} else {
			countMap.put(type, total);
		}
	}

	/**
	 * @功能描述:获取某种评价：差评，中评，好评
	 * @param productCode
	 *            商品编码
	 * @param commentType
	 *            评论类型：1为差评2为中评3为好评
	 * @return
	 * @throws SqlException
	 *             List<EbComment>
	 * @author yusf
	 */
	public List<EbComment> retrieveCommentByProductCode(int productCode,
			int commentType) throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append("WHERE valid = 1 ");
		if (commentType == 1) {// 差评
			sql.append(" and score = 1 ");
		} else if (commentType == 2) {// 中评
			sql.append(" and score in(2,3) ");
		} else if (commentType == 3) {// 好评,没评分默认5星好评处理
			sql.append(" and (score in(4,5) or score is null ) ");
		}
		sql.append(" and productId =? order by id desc");
		List<EbComment> comments = ebCommentDao.findAllByHql(sql.toString(),
				new Object[] { productCode });
		return comments;
	}

	/**
	 * @功能描述:分页获取某种评价：差评，中评，好评
	 * @param productCode
	 *            商品编码
	 * @param commentType
	 *            评论类型：1为差评2为中评3为好评
	 * @throws SqlException
	 *             List<EbComment>
	 */
	public List<EbComment> retrieveCommentByProductCode(int productCode,
			int commentType, int page, int pageSize) throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append("WHERE productId =? and valid = 1 ");
		if (commentType == 1) {// 差评
			sql.append(" and score = 1 ");
		} else if (commentType == 2) {// 中评
			sql.append(" and score in(2,3) ");
		} else if (commentType == 3) {// 好评,没评分默认5星好评处理
			sql.append(" and (score in(4,5) or score is null) ");
		}
		sql.append("order by id desc");
		List<EbComment> comments = ebCommentDao.findAllByHql(sql.toString(),
				page, pageSize, new Object[] { productCode });
		return comments;
	}

	/**
	 * @功能描述:获取某种评价的数量
	 * @param productCode
	 *            商品编码
	 * @param commentType
	 *            评论类型：1为差评2为中评3为好评
	 * @throws SqlException
	 *             List<EbComment>
	 */
	public int getCommentNumByProductCode(int productCode, int commentType)
			throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(1) from ytsp_ebiz_comments WHERE productId = ")
				.append(productCode).append(" and valid = 1 ");
		if (commentType == 1) {// 差评
			sql.append(" and score = 1 ");
		} else if (commentType == 2) {// 中评
			sql.append(" and score in(2,3) ");
		} else if (commentType == 3) {// 好评,没评分默认5星好评处理
			sql.append(" and (score in(4,5) or score is null ) ");
		}
		return ebCommentDao.sqlCount(sql.toString());
	}

	/**
	 * @功能描述:分页取某种数量的评价
	 * @param productCode
	 * @param start
	 *            从第几个开始取
	 * @param limit
	 *            取几条数据
	 * @param commentType
	 * @return
	 * @throws SqlException
	 *             List<EbComment>
	 * @author yusf
	 */
	public List<EbComment> getCommentByPage(int productCode, int start,
			int limit, int commentType, int commentId) throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append("WHERE productId =? and valid = 1 ");
		if (commentType == 1) {// 差评
			sql.append(" and score = 1 ");
		} else if (commentType == 2) {// 中评
			sql.append(" and score in(2,3) ");
		} else if (commentType == 3) {// 好评,没评分默认5星好评处理
			sql.append(" and (score in(4,5) or score is null ) ");
		}
		sql.append(" and id <? order by id desc");
		List<EbComment> comments = ebCommentDao.findAllByHql4Cms(
				sql.toString(), start, limit, new Object[] { productCode,
						commentId });
		return comments;
	}

	/**
	 * @功能描述:根据评价id获取评价图片
	 * @param commentIds
	 * @return
	 * @throws SqlException
	 *             List<EbCommentImg>
	 * @author yusf
	 */
	public List<EbCommentImg> retrieveCommentImgByCodes(
			final Set<Integer> commentIds) throws SqlException {
		return ebCommentImgDao.getHibernateTemplate().execute(
				new HibernateCallback<List<EbCommentImg>>() {

					@Override
					public List<EbCommentImg> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("from EbCommentImg where commentId in (:commentIds)");
						query.setParameterList("commentIds", commentIds);
						return query.list();
					}

				});
	}

	/**
	 * @功能描述:根据评价id获取评价图片
	 * @param commentIds
	 * @return
	 * @throws SqlException
	 *             List<EbCommentImg>
	 * @author yusf
	 */
	public List<EbComment> retrieveCommentByIds(final Set<Integer> commentIds)
			throws SqlException {
		return ebCommentDao.getHibernateTemplate().execute(
				new HibernateCallback<List<EbComment>>() {

					@Override
					public List<EbComment> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("from EbComment where id in (:commentIds)");
						query.setParameterList("commentIds", commentIds);
						return query.list();
					}

				});
	}

	/**
	 * @功能描述:获取评论列表
	 * @param commentIds
	 * @return
	 * @throws SqlException
	 *             List<EbCommentImg>
	 * @author yusf
	 */
	public List<EbComment> retrieveHaveComments(int userId, int page,
			String commentTime, int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
//		sb.append("SELECT c.* FROM ytsp_ebiz_orderdetail od,ytsp_ebiz_comments c WHERE ")
//		  .append(" exists (select 1 from ytsp_ebiz_order o where o.orderid = od.orderid and o.orderSource <> 5) ")
//		  .append(" and c.id = od.commentsid and c.valid = 1 and c.userid = ")
//		  .append(userId);
		sb.append("SELECT a.* FROM ytsp_ebiz_comments a WHERE")
		  .append(" exists (select 1 from ytsp_ebiz_orderdetail od where a.id = od.commentsid)")
		  .append(" and a.userId=").append(userId);
		// if (StringUtil.isNotNullNotEmpty(commentTime)) {
		// sb.append(" AND c.commentTime < '").append(commentTime).append("'");
		// }
		sb.append(" ORDER BY a.commentTime DESC");
		return ebCommentDao.sqlFetch(sb.toString(), EbComment.class, page
				* pageSize, pageSize);
	}

	/**
	 * @功能描述:获取评论列表
	 * @param commentIds
	 * @return
	 * @throws SqlException
	 *             List<EbCommentImg>
	 * @author yusf
	 */
	public int retrieveHaveCommentsCount(int userId) throws SqlException {
		StringBuffer sb = new StringBuffer();
		//过滤掉网站订单的评论
//		sb.append("SELECT count(1) FROM ytsp_ebiz_comments a WHERE valid = 1 ")
//				.append(" and exists (select 1 from ytsp_ebiz_orderdetail od,ytsp_ebiz_order o "
//						+ "where a.id = od.commentsid and o.orderid = od.orderid and o.orderSource <> 5)")
//				.append(" and userId=").append(userId);
		sb.append(
				"SELECT count(1) FROM ytsp_ebiz_orderdetail od,ytsp_ebiz_comments c  WHERE c.id = od.commentsid and c.userid =")
				.append(userId);
		return ebCommentDao.sqlCount(sb.toString());
	}

	/**
	 * @功能描述:保存评价
	 */
	public void saveComment(EbComment comment) throws SqlException {
		ebCommentDao.save(comment);
	}

	/**
	 * @功能描述:保存评价,评论加积分
	 */
	public void saveAllComment(List<EbComment> comments, EbOrder order)
			throws SqlException {
		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
		ebCommentDao.saveAll(comments);
		CreditPolicy creditPolicy = creditPolicyDao.findOneByHql("WHERE id=6");
		int oldUserCredit = 0;
		int obtainCredits = 0;
		for (int i = 0; i < comments.size(); i++) {
			EbComment ebComment = comments.get(i);
			int skuCode = ebComment.getSkuCode();
			for (EbOrderDetail detail : order.getOrderDetails()) {
				if (detail.getSkuCode() != skuCode) {
					continue;
				}
				detail.setCommentsId(ebComment.getId());
				details.add(detail);
			}
			// TODO 每件商品前5名评论者双倍积分赠送
			int num = 0;
			int count = ebCommentDao.getRecordCount("WHERE productId=?",
					new Object[] { ebComment.getProductId() });
			if (count > 5) {
				// 非前5名
				num = creditPolicy.getNum();
			} else {
				// 前5名内
				num = creditPolicy.getNum() * 2;
			}
			Customer c = customerDao.findById(ebComment.getUserId());
			if(i == 0){
				oldUserCredit = c.getCredits();
			}
			obtainCredits = num;
			c.setCredits(c.getCredits() + num);
			customerDao.update(c);
			try {
				String serialNumber = UUID.randomUUID().toString();
				customerDao.manualAudit(Util.getAudit(AuditAction.UPDATE, c, "用户评论获得积分："+num,serialNumber));
				Util.saveCreditRecord(CreditSourceTypeEnum.COMMENT.getValue(), c.getId(), "发表评论送积分（订单号："+order.getOrderid()+"）", obtainCredits, oldUserCredit);
			} catch (Exception e) {
				logger.error("提交评论时，发送审记信息失败");
			}
		}
		order.setStatus(EbOrderStatusEnum.COMMENT);
		ebOrderDao.update(order);
		
	}

	public EbCommentDao getEbCommentDao() {
		return ebCommentDao;
	}

	public void setEbCommentDao(EbCommentDao ebCommentDao) {
		this.ebCommentDao = ebCommentDao;
	}

	public EbCommentImgDao getEbCommentImgDao() {
		return ebCommentImgDao;
	}

	public void setEbCommentImgDao(EbCommentImgDao ebCommentImgDao) {
		this.ebCommentImgDao = ebCommentImgDao;
	}

}
