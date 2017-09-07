package com.ytsp.entrance.service;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.ytsp.db.dao.EbActivityDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.EbProductImageDao;
import com.ytsp.db.dao.EbSecKillDao;
import com.ytsp.db.dao.EbStorageDao;
import com.ytsp.db.domain.EbActivity;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductImage;
import com.ytsp.db.domain.EbSecKill;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.enums.EbProductImageTypeEnum;
import com.ytsp.db.enums.EbProductTypeEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.system.SystemManager;

public class EbProductService {
	private  DecimalFormat df = new DecimalFormat("##0.##");
	static final Logger logger = Logger.getLogger(EbProductService.class);
	
	private EbProductDao ebProductDao;

	private EbActivityDao ebActivityDao;

	private EbProductImageDao ebProductImageDao;

	private EbStorageDao ebStorageDao;

	private EbSecKillDao ebSecKillDao;
	
	
	public EbSecKillDao getEbSecKillDao() {
		return ebSecKillDao;
	}

	public void setEbSecKillDao(EbSecKillDao ebSecKillDao) {
		this.ebSecKillDao = ebSecKillDao;
	}

	public EbStorageDao getEbStorageDao() {
		return ebStorageDao;
	}

	public void setEbStorageDao(EbStorageDao ebStorageDao) {
		this.ebStorageDao = ebStorageDao;
	}

	public EbProductImageDao getEbProductImageDao() {
		return ebProductImageDao;
	}

	public void setEbProductImageDao(EbProductImageDao ebProductImageDao) {
		this.ebProductImageDao = ebProductImageDao;
	}

	public EbActivityDao getEbActivityDao() {
		return ebActivityDao;
	}

	public void setEbActivityDao(EbActivityDao ebActivityDao) {
		this.ebActivityDao = ebActivityDao;
	}

	public EbProductDao getEbProductDao() {
		return ebProductDao;
	}

	public void setEbProductDao(EbProductDao ebProductDao) {
		this.ebProductDao = ebProductDao;
	}
	

	public void saveEbSku(EbProduct ebProduct) throws Exception {
		ebProductDao.save(ebProduct);
	}
	
	public void saveOrUpdate(EbProduct ebProduct) throws Exception {
		ebProductDao.saveOrUpdate(ebProduct);
	}

	public void updateEbShoppingCartDao(EbProduct ebProduct) throws Exception {
		ebProductDao.update(ebProduct);
	}
	
	public EbProduct retrieveEbProductById(int productCode) throws SqlException{
		return ebProductDao.findById(productCode);
	}
	
	/**
	* <p>功能描述:根据skucode获取商品</p>
	* <p>参数：@param skuCode
	* <p>参数：@return</p>
	* <p>返回类型：EbProduct</p>
	 */
	public EbProduct retrieveEbproductBySkuCode(int skuCode){
		StringBuffer sb = new StringBuffer();
		sb.append(" select p.* from ytsp_ebiz_product p,ytsp_ebiz_sku s where p.productcode = s.productcode ");
		sb.append(" and s.skucode = ").append(skuCode);
		List<EbProduct> products = ebProductDao.sqlFetch(sb.toString(), EbProduct.class, 0, 1);
		if(products == null || products.size() <= 0){
			return null;
		}
		return products.get(0);
	}
	
    public List<EbProduct> retrieveValidProductsByActivityId(int activityId) throws SqlException{
    	EbActivity activity = ebActivityDao.findById(activityId);
    	List<EbProduct> ret = ebProductDao.findAllByHql(
    			"where ebActivity=? and status=? and productType=?  order by sortNum", 
    			new Object[]{activity,EbProductValidStatusEnum.VALID,EbProductTypeEnum.NORMAL});
    	return ret;
    }
    
    public EbProduct retrieveProductAndSkusByProductCode(int productCode) throws SqlException{
    	EbProduct ebProduct = ebProductDao.findById(productCode);
    	//显示的拿到所有的SKU
    	ebProduct.getSkus();
    	return ebProduct;
    }
    
    public EbProduct retrieveProductByProductCode(int productCode) throws SqlException{
    	EbProduct ebProduct = ebProductDao.findById(productCode);
    	if(ebProduct != null){
    		ebProduct.getSkus();
    	}
    	return ebProduct;
    } 
    
	public List<EbProduct> getAllValid() throws SqlException{
		return ebProductDao.findAllByHql(" where status=? and productType=?", 
				new Object[]{EbProductValidStatusEnum.VALID,EbProductTypeEnum.NORMAL});
	}
	
    public JSONArray getTopEbProducts(String platform, int start, int limit, String version) throws Exception{
    	JSONArray array = new JSONArray();
    	List<EbProduct> ls = ebProductDao.sqlFetch("select p.* from ytsp_top_ebiz_product t,ytsp_ebiz_product p " +
    			"where t.productCode=p.productCode and p.status=1 and t.operateType=1 order by t.sort", EbProduct.class, start, limit);
    	MobileTypeEnum plat =   MobileTypeEnum.iphone;//推荐位的海报图只能用手机的比例
    	for (int i = 0; i < ls.size(); i++) {
    		EbProduct foo = ls.get(i);
    		JSONObject jo = new JSONObject();
    		jo.put("price", df.format(foo.getPrice()));
    		jo.put("productCode", foo.getProductCode());
    		jo.put("productName", foo.getProductName());
    		jo.put("productType", EbProductTypeEnum.NORMAL.getValue());
    		jo.put("vprice", df.format(foo.getVprice()));
    		jo.put("svprice", df.format(foo.getSvprice()));
//    		List<EbProductImage> lsimg = ebProductImageDao.findAllByHql(
//    				" where productCode=? and status=? and type=? and platform=? " +
//    				" order by sortNum", new java.lang.Object[]{
//    						foo.getProductCode(),1,EbProductImageTypeEnum.DEMO,plat});//1==有效;1==描述图片
    		jo.put("imageSrc",SystemManager.getInstance().getSystemConfig().getImgServerUrl() +
    				foo.getImageCut());//取第一张图片作为海报
    		int productStorage = 0;
			for (EbSku ebSku : foo.getSkus()) {
				productStorage += ebSku.getStorage().getAvailable();
			}
			jo.put("productStorage", productStorage);
    		array.put(jo);
		}
    	return array;
    }

    /**
     * 获取非订单图片
     * @param productCode
     * @return
     * @throws SqlException
     */
	public List<EbProductImage> getEbProductImages(MobileTypeEnum plat,Integer productCode) throws SqlException {
		plat = MobileTypeEnum.iphone;//不是ipad就用iphone的图
		return ebProductImageDao.findAllByHql(" where productCode=? and status=? and type=? and platform=? order by sortNum", 
				new java.lang.Object[]{productCode,1,EbProductImageTypeEnum.DEMO,plat});//1==有效;1==描述图片
	}
	
	/**
	* @功能描述: 获取商品所有图片 
	* @param plat
	* @param productCode
	* @return
	* @throws SqlException     
	* List<EbProductImage>   
	* @author yusf
	 */
	public List<EbProductImage> getEbProductImagesByProductCode(MobileTypeEnum plat,Integer productCode) throws SqlException {
		plat = MobileTypeEnum.iphone;//不是ipad就用iphone的图
		return ebProductImageDao.findAllByHql(" where productCode=? and status=? and platform=? order by sortNum", 
				new java.lang.Object[]{productCode,1,plat});//1==有效;1==描述图片
	}

	public EbSecKill getEbSecKillByActivity(int ebActivityId) throws SqlException {
		return ebSecKillDao.findOneByHql(" where activityId=? and status=?", new Object[]{
				ebActivityId,ValidStatusEnum.VALID});
	}

	public EbSecKill getEbSecKillByProduct(EbProduct product ) throws SqlException {
		return ebSecKillDao.findOneByHql(" where product=? and status=?", new Object[]{
				product,ValidStatusEnum.VALID});
	}
	
	public int getEbProductTopListCount(String plat, String version) {
		return ebProductDao.sqlCount("select count(*) from ytsp_top_ebiz_product"); 
	}

	public List<EbProduct> getVipProductList() throws SqlException {
		try {
			return ebProductDao.findAllByHql("where productType=?", 
					new Object[]{EbProductTypeEnum.VIPMEMBER});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<EbProduct> retrieveEbProductByCodes(final Set<Integer> productCodes) throws SqlException{
		if(productCodes == null || productCodes.size() <= 0){
			return null;
		}
		return ebProductDao.getHibernateTemplate().execute(new HibernateCallback<List<EbProduct>>(){

			@Override
			public List<EbProduct> doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("from EbProduct where productCode in (:productCodes)");
				query.setParameterList("productCodes", productCodes);
				return query.list();
			}
			
		});
	}
	
	public List<EbProduct> retrieveEbProductByBrands(final Set<Integer> brands) throws SqlException{
		if(brands == null || brands.size() <= 0){
			return null;
		}
		return ebProductDao.getHibernateTemplate().execute(new HibernateCallback<List<EbProduct>>(){
			@Override
			public List<EbProduct> doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("from EbProduct where ebbrand in (:brands)");
				query.setParameterList("ebbrand", brands);
				return query.list();
			}
			
		});
	}
}
