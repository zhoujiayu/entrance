package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.EbPropsDao;
import com.ytsp.db.domain.EbProps;
import com.ytsp.db.enums.EbPropsEnum;
import com.ytsp.db.exception.SqlException;

@Service("ebProductPropsService")
@Transactional
public class EbProductPropsService {
	
	@Resource(name="ebPropsDao")
	private EbPropsDao ebPropsDao;

	public EbPropsDao getEbPropsDao() {
		return ebPropsDao;
	}

	public void setEbPropsDao(EbPropsDao ebPropsDao) {
		this.ebPropsDao = ebPropsDao;
	}
	
	public List<EbProps> getPropsByType(EbPropsEnum propsType) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append(" WHERE props = ?");
		return ebPropsDao.findAllByHql(sb.toString(), new Object[]{propsType});
	}
	
}
