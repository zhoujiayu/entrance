package com.ytsp.entrance.service.v5_0;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.ActivityZoneDao;
import com.ytsp.db.dao.BaseConfigDataDao;
import com.ytsp.db.dao.EbPosterDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.EbProductParamDao;
import com.ytsp.db.dao.EbSkuDao;
import com.ytsp.db.domain.ActivityZone;
import com.ytsp.db.domain.BaseConfigData;
import com.ytsp.db.domain.EbCatagory;
import com.ytsp.db.domain.EbPoster;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductParam;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.SelectAppTypeConditionEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("productServiceV5_0")
@Transactional
public class ProductServiceV5_0 {

	@Resource(name = "ebPosterDao")
	private EbPosterDao ebPosterDao;

	@Resource(name = "ebProductDao")
	private EbProductDao ebProductDao;

	@Resource(name = "baseConfigDataDao")
	private BaseConfigDataDao baseConfigDataDao;
	
	@Resource(name = "ebProductParamDao")
	private EbProductParamDao ebProductParamDao;
	
	@Resource(name = "ebSkuDao")
	private EbSkuDao ebSkuDao;
	
	@Resource(name = "activityZoneDao")
	private ActivityZoneDao activityZoneDao;
	
	/**
	* <p>功能描述:根据id获取专区信息</p>
	* <p>参数：@param id
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ActivityZone</p>
	 */
	public ActivityZone getActivityZoneById(int id) throws SqlException{
		return activityZoneDao.findById(id);
	}
	
	/**
	* <p>功能描述:根据13位条形码获取某商品的code</p>
	* <p>参数：@param productCode
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProductParam></p>
	 * @throws SqlException 
	 */
	public int getProductByEANCode(String EANCode) throws SqlException{
		List<EbSku> skus = ebSkuDao.findAllByHql(" where ean13Code = ? ",new Object[]{Long.parseLong(EANCode)});
		if(skus == null || skus.size() == 0){
			return 0;
		}
		EbProduct product = skus.get(0).getParent();
		if(product.getStatus() == EbProductValidStatusEnum.VALID){
			return product.getProductCode();
		}
		return 0;
	}
	
	/**
	* <p>功能描述:获取某商品的参数</p>
	* <p>参数：@param productCode
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProductParam></p>
	 * @throws SqlException 
	 */
	public List<EbProductParam> getProductParamByProductCode(int productCode) throws SqlException{
		return ebProductParamDao.findAllByHql(" where productCode = ? order by sortNum ",new Object[]{productCode});
	}
	
	public EbProduct getProductByCode(int productCode) throws SqlException {
		return ebProductDao.findById(productCode);
	}

	/**
	 * <p>
	 * 功能描述:获取玩具首页海报
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbPoster>
	 * </p>
	 */
	public List<EbPoster> getProductHomePoster() throws SqlException {
		Date now = new Date();
		return ebPosterDao
				.findAllByHql(
						" where  location in(?,?,?,?) and startTime<? and appType =? order by sortNum asc",
						new Object[] { EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue(),
								EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue(),
								EbPosterAppLocationEnum.APPBANNER.getValue(),
								EbPosterAppLocationEnum.NAVIGATIONBAR.getValue(), now,
								SelectAppTypeConditionEnum.SHOP });
	}
	
	/**
	* <p>功能描述:分页获取某品牌下的所有商品</p>
	* <p>参数：@return</p>
	* <p>返回类型：List<EbProduct></p>
	 */
	public List<EbProduct> getProductByBrand(int brandId,int page,int pageSize){
		StringBuffer sql = new StringBuffer();
		sql.append("select * from ytsp_ebiz_product WHERE status = 1 and ebbrand = ").append(brandId);
		sql.append(" ORDER BY sortNum ");
		
		return ebProductDao.sqlFetch(sql.toString(), EbProduct.class, page, pageSize);
	}
	
	/**
	 * <p>
	 * 功能描述:根据分类id获取商品
	 * </p>
	 * <p>
	 * 参数：@param categoryId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbProduct>
	 * </p>
	 */
	public List<EbProduct> getProductByCategory(int categoryId, int page,
			int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
		sb.append(" WHERE ebCatagory = ").append(categoryId)
		.append(" and status = ").append(EbProductValidStatusEnum.VALID.getValue())
		.append(" order by sortNum asc");
		EbCatagory catagory = new EbCatagory();
		catagory.setId(categoryId);
		
		return ebProductDao.sqlFetch(sb.toString(), EbProduct.class, page, pageSize);
	}

	/**
	 * <p>
	 * 功能描述:获取一级分类下的所有商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbProduct>
	 * </p>
	 */
	public List<EbProduct> getProductByFirstCategory(int firstCategoryId,
			int page, int pageSize) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from ytsp_ebiz_product p ");
		sql.append(
				" where p.ebCatagory in(select id from ytsp_ebiz_catagory c where c.parent = ")
				.append(firstCategoryId).append(") ");
		sql.append(" and p.status = 1 order by sortNum asc ");
		return ebProductDao.sqlFetch(sql.toString(), EbProduct.class, page
				* pageSize, pageSize);
	}

	/**
	 * <p>
	 * 功能描述:按某字段排序分页获取商品
	 * </p>
	 * <p>
	 * 参数：@param column 字段名称
	 * <p>
	 * 参数：@param sort 升序或降序 desc或者asc
	 * <p>
	 * 参数：@param page 页数，从0开始
	 * <p>
	 * 参数：@param pageSize 每页显示个数
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbProduct>
	 * </p>
	 * 
	 * @throws SqlException
	 */
	public List<EbProduct> getProductOrderByColumn(String column,
			String sortKey, int page, int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
		sb.append("	WHERE status =? ORDER BY ");
		sb.append(column).append(" ").append(sortKey);
		return ebProductDao.findAllByHql(sb.toString(), page, pageSize,
				new Object[] { EbProductValidStatusEnum.VALID });
	}

	/**
	 * <p>
	 * 功能描述:获取评价最佳的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbProduct>
	 * </p>
	 */
	public List<EbProduct> getProductByGoodComments(int page, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select p.productcode,p.productName,p.product_description,p.price,p.vprice,p.svprice, ");
		sb.append(" p.status,p.productType,p.creditPercentage,p.ebactivity,p.ebvendor,p.vendorproductCode, ");
		sb.append(" p.sortNum,p.shipping,p.onShelfTime,p.imgUrl,p.ebCatagory,p.ebBrand,p.comment,p.diamond ");
		sb.append(" from ytsp_ebiz_product p LEFT JOIN ");
		sb.append(" (select productId,sum(score) score from ytsp_ebiz_comments group by productId ) c ");
		sb.append(" on p.productcode = c.productid where p.status = 1 order by score desc ");
		return ebProductDao.sqlFetch(sb.toString(), EbProduct.class, page,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:获取销量最多的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbProduct>
	 * </p>
	 */
	public List<EbProduct> getProductBySoldBest(int page, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select p.productcode,p.productName,p.product_description,p.price,p.vprice,p.svprice, ");
		sb.append(" p.status,p.productType,p.creditPercentage,p.ebactivity,p.ebvendor,p.vendorproductCode, ");
		sb.append(" p.sortNum,p.shipping,p.onShelfTime,p.imgUrl,p.ebCatagory,p.ebBrand,p.comment,p.diamond ");
		sb.append(" from ytsp_ebiz_product p LEFT JOIN ");
		sb.append(" (select od.productcode,sum(od.amount) total from ytsp_ebiz_order o,ytsp_ebiz_orderdetail od ");
		sb.append(" where o.orderid = od.orderid and o.status in(0,1,2,-8) group by od.productcode ) a ");
		sb.append(" ON p.productcode = a.productcode where  p.status = 1 ORDER BY a.total desc ");
		return ebProductDao.sqlFetch(sb.toString(), EbProduct.class, page,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:获取折扣最多的商品
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbProduct>
	 * </p>
	 */
	public List<EbProduct> getProductByDiscount(int page, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from ytsp_ebiz_product where STATUS = 1 order by (price - vprice)/price desc ");
		return ebProductDao.sqlFetch(sb.toString(), EbProduct.class, page,
				pageSize);
	}

	/**
	 * 正品保证文字内容
	 * 
	 * @return
	 * @throws SqlException
	 */
	public List<BaseConfigData> getQualityGuaranteeText() throws SqlException {
		String hql = "WHERE status=? AND attrcode=? ORDER BY sortNum";
		return baseConfigDataDao.findAllByHql(hql, new Object[] {
				ValidStatusEnum.VALID, "CONFIG_REALPRODUCT_PROMISE" });
	}
}
