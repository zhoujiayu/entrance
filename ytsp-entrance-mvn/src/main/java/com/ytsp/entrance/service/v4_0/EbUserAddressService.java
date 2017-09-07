package com.ytsp.entrance.service.v4_0;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbUserAddressDao;
import com.ytsp.db.domain.EbUserAddress;
import com.ytsp.db.enums.EbUserAddressStatusEnum;
import com.ytsp.db.exception.SqlException;

public class EbUserAddressService {
	static final Logger logger = Logger.getLogger(EbUserAddressService.class);

	private EbUserAddressDao ebUserAddressDao;

	public EbUserAddressDao getEbUserAddressDao() {
		return ebUserAddressDao;
	}

	public void setEbUserAddressDao(EbUserAddressDao ebUserAddressDao) {
		this.ebUserAddressDao = ebUserAddressDao;
	}

	public int saveEbUserAddress(EbUserAddress ebUserAddress)
			throws SqlException {
		this.ebUserAddressDao.save(ebUserAddress);
		return ebUserAddress.getAddressId();
	}

	public boolean updateEbUserAddress(EbUserAddress ebUserAddress)
			throws SqlException {
		this.ebUserAddressDao.update(ebUserAddress);
		return true;
	}

	public EbUserAddress retrieveEbUserAddressById(int addressId)
			throws SqlException {
		return this.ebUserAddressDao.findById(addressId);
	}

	public List<EbUserAddress> getAddrListByUser(int uid) throws SqlException {
		return ebUserAddressDao.findAllByHql(" where userid=? and status=? ",
				new Object[] { uid, EbUserAddressStatusEnum.VALID });
	}

}
