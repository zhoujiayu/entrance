package com.ytsp.entrance.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.ytsp.db.dao.EbSkuDao;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.VipProductVO;

import edu.emory.mathcs.backport.java.util.Collections;

public class EbSkuService {
	static final Logger logger = Logger.getLogger(EbSkuService.class);

	private EbSkuDao ebSkuDao;

	public void setEbSkuDao(EbSkuDao ebSkuDao) {
		this.ebSkuDao = ebSkuDao;
	}

	public EbSkuDao getEbSkuDao() {
		return ebSkuDao;
	}

	public void setEbProductDao(EbSkuDao ebSkuDao) {
		this.ebSkuDao = ebSkuDao;
	}

	public void saveEbSku(EbSku ebSku) throws Exception {
		ebSkuDao.save(ebSku);
	}

	public void saveOrUpdate(EbSku ebSku) throws Exception {
		ebSkuDao.saveOrUpdate(ebSku);
	}

	public void updateEbSku(EbSku ebSku) throws Exception {
		ebSkuDao.update(ebSku);
	}

	public EbSku retrieveEbSkuBySkuCode(int skuCode) throws SqlException {
		return ebSkuDao.findById(skuCode);
	}

	public List<EbSku> retrieveEbSkuBySkuCodes(final Set<Integer> skuCodes)
			throws SqlException {
		return ebSkuDao.getHibernateTemplate().execute(
				new HibernateCallback<List<EbSku>>() {

					@Override
					public List<EbSku> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("from EbSku where skuCode in (:skuCodes)");
						query.setParameterList("skuCodes", skuCodes);
						return query.list();
					}

				});
	}

	public List<EbSku> getAll() throws SqlException {
		return ebSkuDao.getAll();
	}

	public List<VipProductVO> getVIPProducts() {
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM ytsp_ebiz_sku a,(SELECT productCode FROM ytsp_ebiz_product WHERE productType=2 ) b");
		sb.append(" WHERE a.productCode=b.productCode ");
		List<EbSku> ebSkus = ebSkuDao.sqlFetch(sb.toString(), EbSku.class, -1,
				-1);
		List<VipProductVO> vipProductVOs = new ArrayList<VipProductVO>();
		if (ebSkus != null && ebSkus.size() > 0) {
			for (EbSku ebSku : ebSkus) {
				VipProductVO vo = new VipProductVO();
				EbProduct ebProduct = ebSku.getParent();
				vo.setPrice(ebProduct.getPrice());
				vo.setProductCode(ebProduct.getProductCode());
				vo.setProductName(ebProduct.getProductName());
				vo.setSkuCode(ebSku.getSkuCode());
				vipProductVOs.add(vo);
			}
			Collections.sort(vipProductVOs);
		}
		return vipProductVOs;
	}
	// public EbShoppingCart createCartBySku( int skuCode) throws SqlException {
	// EbSku ebSku = ebSkuDao.findById(skuCode);
	// if(ebSku==null)
	// return null;
	// EbShoppingCart cart = new EbShoppingCart();
	// cart.setProductCode(ebSku.getSkuCode());
	// cart.setProductName(ebSku.getProductName());
	// cart.setSkuCode(skuCode);
	// cart.setProductColor(ebSku.getColor());
	// cart.setProductSize(ebSku.getSize());
	// cart.setAmount(1);
	// EbProduct ebProduct = ebSku.getParent();
	// cart.setProductImage(ebProduct.getImgUrl());
	// return cart;
	// }

	// public ShoppingCartItem createShoppingCartItemBySku(EbShoppingCart
	// ebShoppingCart) throws SqlException {
	// EbSku ebSku = ebSkuDao.findById(ebShoppingCart.getSkuCode());
	// if (ebSku.getStatus().getValue().intValue() == 1) {
	// EbProduct ebProduct = ebSku.getParent();
	// ShoppingCartItem item = new ShoppingCartItem();
	// item.setAmount(ebShoppingCart.getAmount());
	// item.setChecked(true);
	// item.setColor(ebShoppingCart.getProductColor());
	// item.setId(ebShoppingCart.getId());
	// item.setProductCode(ebShoppingCart.getProductCode());
	// item.setProductImage(ebShoppingCart.getProductImage());
	// item.setProductName(ebShoppingCart.getProductName());
	// item.setSize(ebShoppingCart.getProductSize());
	// item.setSkuCode(ebShoppingCart.getSkuCode());
	// item.setUserId(ebShoppingCart.getUserId());
	// item.setMarketPrice(ebProduct.getPrice());
	// item.setPrice(ebProduct.getVprice());
	// item.setVipPrice(ebProduct.getSvprice());
	// item.setOosNum(item.getAmount()
	// - ebSku.getStorage().getAvailable());
	// return item;
	// }
	// return null;
	// }
}
