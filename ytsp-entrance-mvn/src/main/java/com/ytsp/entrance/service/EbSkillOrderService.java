package com.ytsp.entrance.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.LockMode;

import com.ytsp.db.dao.EbOrderDao;
import com.ytsp.db.dao.EbSecKillDao;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbSecKill;
import com.ytsp.db.exception.SqlException;

/**
 * 秒杀订单
 *
 */
public class EbSkillOrderService {
	public EbSecKillDao getSecKillDao() {
		return secKillDao;
	}

	public void setSecKillDao(EbSecKillDao secKillDao) {
		this.secKillDao = secKillDao;
	}

	public EbOrderDao getEbOrderDao() {
		return ebOrderDao;
	}

	public void setEbOrderDao(EbOrderDao ebOrderDao) {
		this.ebOrderDao = ebOrderDao;
	}

	private EbSecKillDao secKillDao;
	private EbOrderDao ebOrderDao;
	
	public long createSecKillOrder(EbSecKill ebSecKill,EbOrder ebOrder) throws SqlException{
		//再次获取一次秒杀商品
		EbSecKill secKill = (EbSecKill)secKillDao.getSessionFactory()
								.getCurrentSession().load(EbSecKill.class,ebSecKill.getId(),LockMode.UPGRADE);
		if (secKill.getProductNum() >= 1) {
			ebOrderDao.save(ebOrder);
			secKill.setProductNum(secKill.getProductNum() - 1);
			secKillDao.update(secKill);
			return ebOrder.getOrderid().longValue();
		}
		return 0L;
	}
	
	/**
	 * 获取一个秒杀场次
	 * @param secKillId
	 * @return
	 * @throws SqlException 
	 */
	public EbSecKill getSecKill(int secKillId,int activityId) throws SqlException {
		EbSecKill ebSecKill = secKillDao.findOneByHql(" where id=? and activityId=?", new Object[]{secKillId,activityId});
		return ebSecKill;
	}
	/**
	 * 一个新加的月饼活动，因为版本问题放在这里了；此方法是检查用户是否已经领取过
	 * @return
	 */
	public boolean checkYuebing(int userid){
		Object obj = secKillDao.executeSql("select userid from ytsp_yuebing where userid="+userid);
		if(obj==null)
			return true;
		int ct =  ((Integer)obj).intValue() ;
		if(ct<=0)
			return true;
		return false;
	}
	/**
	 * 一个新加的月饼活动，因为版本问题放在这里了；此方法是生成月饼订单
	 * @return
	 */
	public boolean saveYuebing(int userid,String username,int skucode,String addr,String cellphone){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int row = secKillDao.executeSqlUpdate(
				"insert into  ytsp_yuebing (userid,username,skucode,addr,cellphone,time) values("+userid+",'"+username+"',"+
				+skucode+",'"+addr+"','"+cellphone+"','"+sdf.format(new Date())+"')");
		secKillDao.executeSqlUpdate("update ytsp_ebiz_storage set available=available-1 where skuCode="+skucode);
		return row>0;
	}
}
