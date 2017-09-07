package com.ytsp.entrance.service;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.ParentControlDao;
import com.ytsp.db.dao.ParentTimeControlDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.ParentControl;
import com.ytsp.db.domain.ParentTimeControl;

/**
 * @author GENE
 * @description 家长控制服务
 */
public class ParentControlService {
	private static final Logger logger = Logger.getLogger(ParentControlService.class);

	private ParentControlDao parentControlDao;
	private ParentTimeControlDao parentTimeControlDao;

	
	public ParentControl getParentControlByCustomerId(int customerid) throws Exception {
		return parentControlDao.findOneByHql(" WHERE customer.id = ?", new Object[]{customerid});
	}
	
	public void saveOrUpdateParentControl(ParentControl pc) throws Exception{
		parentControlDao.saveOrUpdate(pc);
	}

	public void saveParentControl(ParentControl pc) throws Exception{
		parentControlDao.save(pc);
	}

	public List<ParentTimeControl> getParentTimeControlByCustomerId(int customerid) throws Exception {
		return parentTimeControlDao.findAllByHql(" WHERE customer.id = ?", new Object[]{customerid});
	}
	
	public void saveParentTimeControl(Customer customer, double startTime, double endTime) throws Exception {
		int count = parentTimeControlDao.getRecordCount(" WHERE customer.id = ? AND startTime = ? AND endTime = ?", new Object[]{customer.getId(), startTime, endTime});
		if(count > 0){
			return;
		}
		
		ParentTimeControl ctrl = new ParentTimeControl();
		ctrl.setCustomer(customer);
		ctrl.setStartTime(startTime);
		ctrl.setEndTime(endTime);
		parentTimeControlDao.save(ctrl);
	}
	
	public void deleteParentTimeControl(int customerid, double startTime, double endTime) throws Exception {
		parentTimeControlDao.deleteByHql(" WHERE customer.id = ? AND startTime = ? AND endTime = ?", new Object[]{customerid, startTime, endTime});
	}
	
	public static double stringTime2Double(String time){
		time = time.replaceAll(":", "\\.");
		try{
			return Double.valueOf(time);
		}catch(Exception ex){
			ex.printStackTrace();
			return 0.0d;
		}
	}
	
	public static String doubleTime2String(Double time){
		if(time == null){
			return "00:00";
		}
		
		DecimalFormat df = new DecimalFormat("00.00"); 
		String _time = df.format(time);
		_time = _time.replaceAll("\\.", ":");
		return _time;
	}
	
	public ParentControlDao getParentControlDao() {
		return parentControlDao;
	}

	public void setParentControlDao(ParentControlDao parentControlDao) {
		this.parentControlDao = parentControlDao;
	}


	public ParentTimeControlDao getParentTimeControlDao() {
		return parentTimeControlDao;
	}


	public void setParentTimeControlDao(ParentTimeControlDao parentTimeControlDao) {
		this.parentTimeControlDao = parentTimeControlDao;
	}
	
	
}
