package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.EbBrandDao;
import com.ytsp.db.dao.EbBrandImgDao;
import com.ytsp.db.domain.EbBrand;
import com.ytsp.db.domain.EbBrandImg;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("ebBrandService")
@Transactional
public class EbBrandService {

	@Resource(name = "ebBrandDao")
	private EbBrandDao ebBrandDao;

	public EbBrandDao getEbBrandDao() {
		return ebBrandDao;
	}

	public void setEbBrandDao(EbBrandDao ebBrandDao) {
		this.ebBrandDao = ebBrandDao;
	}

	public List<EbBrand> getAllBrands() throws SqlException {
		String sql = " WHERE valid =? order by sortNum";
		return ebBrandDao.findAllByHql(sql,
				new Object[] { EbProductValidStatusEnum.VALID });
	}

	/**
	 * <p>
	 * 功能描述:分页获取所有品牌
	 * </p>
	 * <p>
	 * 参数：@param page
	 * <p>
	 * 参数：@param pageSize
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbBrand>
	 * </p>
	 */
	public List<EbBrand> getAllBrandsByPage(int page, int pageSize)
			throws SqlException {
		if(page < 0 && pageSize < 0){
			page = 0;
		}
		String sql = " WHERE valid =? order by sortNum";
		return ebBrandDao.findAllByHql(sql, page*pageSize, pageSize,
				new Object[] { EbProductValidStatusEnum.VALID });
	}

	@Resource(name = "ebBrandImgDao")
	EbBrandImgDao ebBrandImgDao;

	/**
	 * 正品保证图片
	 * 
	 * @param brandId
	 * @return
	 * @throws SqlException
	 */
	public List<EbBrandImg> getQualityGuaranteeImages(int brandId)
			throws SqlException {
		String hql = "WHERE status=? AND brandId=? ORDER BY sortNum";
		return ebBrandImgDao.findAllByHql(hql, new Object[] {
				ValidStatusEnum.VALID, brandId });
	}
}
