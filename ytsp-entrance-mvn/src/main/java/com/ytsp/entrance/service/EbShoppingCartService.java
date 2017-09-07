package com.ytsp.entrance.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import cn.dongman.util.UtilDate;

import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.EbOrderDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.EbPromotionDao;
import com.ytsp.db.dao.EbShoppingCartDao;
import com.ytsp.db.dao.EbSkuDao;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductCollection;
import com.ytsp.db.domain.EbPromotion;
import com.ytsp.db.domain.EbPromotionItem;
import com.ytsp.db.domain.EbShoppingCart;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.GiftItem;
import com.ytsp.db.vo.PromotionItem;
import com.ytsp.db.vo.PromotionVO;
import com.ytsp.db.vo.ShoppingCartItem;
import com.ytsp.db.vo.ShoppingCartVO;
import com.ytsp.entrance.util.Util;

public class EbShoppingCartService {
	static final Logger logger = Logger.getLogger(EbShoppingCartService.class);

	private EbShoppingCartDao ebShoppingCartDao;
	private EbSkuDao ebSkuDao;
	private EbProductDao ebProductDao;
	private EbPromotionDao ebPromotionDao;
	private CustomerMemberDao customerMemberDao;
	private EbOrderDao ebOrderDao;

	public EbSkuDao getEbSkuDao() {
		return ebSkuDao;
	}

	public void setEbSkuDao(EbSkuDao ebSkuDao) {
		this.ebSkuDao = ebSkuDao;
	}

	public EbProductDao getEbProductDao() {
		return ebProductDao;
	}

	public void setEbProductDao(EbProductDao ebProductDao) {
		this.ebProductDao = ebProductDao;
	}

	public EbPromotionDao getEbPromotionDao() {
		return ebPromotionDao;
	}

	public void setEbPromotionDao(EbPromotionDao ebPromotionDao) {
		this.ebPromotionDao = ebPromotionDao;
	}

	public CustomerMemberDao getCustomerMemberDao() {
		return customerMemberDao;
	}

	public void setCustomerMemberDao(CustomerMemberDao customerMemberDao) {
		this.customerMemberDao = customerMemberDao;
	}

	public EbOrderDao getEbOrderDao() {
		return ebOrderDao;
	}

	public void setEbOrderDao(EbOrderDao ebOrderDao) {
		this.ebOrderDao = ebOrderDao;
	}

	public EbShoppingCartDao getEbShoppingCartDao() {
		return ebShoppingCartDao;
	}

	public void setEbShoppingCartDao(EbShoppingCartDao ebShoppingCartDao) {
		this.ebShoppingCartDao = ebShoppingCartDao;
	}

	public void updateEbShoppingCart(EbShoppingCart ebShoppingCart)
			throws Exception {
		ebShoppingCartDao.update(ebShoppingCart);
	}

	// // 购物车状态1默认，3删除

	public void deleteAll(int userId, String cartId) throws Exception {
		StringBuffer sb = new StringBuffer("SET status=3 WHERE ");
		if (userId > 0) {
			sb.append(" userId= ").append(userId);
		}
		if (StringUtils.isNotEmpty(cartId)) {
			if (userId > 0)
				sb.append(" OR ");
			sb.append(" shoppingCartId= '").append(cartId).append("'");
		}
		ebShoppingCartDao.updateByHql(sb.toString());
	}

	public void delete(int userId, String cartId, int... ids) throws Exception {
		if (ids == null || ids.length == 0) {
			return;
		}
		StringBuffer sb = new StringBuffer("SET status=3 WHERE ");
		if (userId > 0) {
			sb.append(" userId= ").append(userId);
		} else {
			sb.append(" shoppingCartId= '").append(cartId).append("'");
		}
		sb.append(" AND id IN (");
		for (int id : ids) {
			sb.append(id).append(",");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");
		ebShoppingCartDao.updateByHql(sb.toString());
	}

	public EbShoppingCart addShopiingCartItem(int skuCode, String cartId,
			int userId, int amount) throws SqlException {
		EbShoppingCart cart = null;
		if (userId > 0) {
			cart = ebShoppingCartDao.findOneByHql(
					" WHERE skuCode=? and userId=? and status=1", new Object[] {
							skuCode, userId });
		} else {
			cart = ebShoppingCartDao.findOneByHql(
					" WHERE skuCode=? and shoppingCartId=? and status=1",
					new Object[] { skuCode, cartId });
		}
		if (cart != null) {
			amount += cart.getAmount();
			cart.setAmount(amount > 10 ? 10 : amount);
			cart.setAddTime(new Date());
			cart.setChecked(true);
			ebShoppingCartDao.update(cart);
			return cart;
		} else {
			EbSku ebSku = ebSkuDao.findById(skuCode);
			if (ebSku == null)
				return null;
			cart = new EbShoppingCart();
			cart.setProductCode(ebSku.getProductCode());
			cart.setProductName(ebSku.getProductName());
			cart.setSkuCode(skuCode);
			cart.setProductColor(ebSku.getColor());
			cart.setProductSize(ebSku.getSize());
			cart.setAmount(amount > 10 ? 10 : amount);
			cart.setAddTime(new Date());
			cart.setStatus(1);
			cart.setChecked(true);
			if (userId != 0) {
				cart.setUserId(userId);
			} else {
				cart.setShoppingCartId(cartId);
			}
			EbProduct ebProduct = ebSku.getParent();
			if(ebProduct == null){
				ebProduct = ebProductDao.findById(ebSku.getProductCode());
			}
			cart.setProductImage(ebProduct.getImgUrl());
			ebShoppingCartDao.save(cart);
		}
		return cart;
	}

	public void updateShoppingCartCheckedAll(int userId, String cartId, boolean checked)
			throws Exception {
		if (userId > 0) {
			ebShoppingCartDao.updateByHql("SET checked=? WHERE userId=?",
					new Object[] { checked, userId });
		} else {
			ebShoppingCartDao.updateByHql(
					"SET checked=? WHERE shoppingCartId=?", new Object[] {
							checked, cartId });
		}
	}

	public void updateShoppingCartChecked(int userId, String cartId, boolean checked, int... ids)
			throws Exception {
		if (ids == null || ids.length == 0) {
			return;
		}
		StringBuffer sb = new StringBuffer("SET checked=? WHERE ");
		if (userId > 0) {
			sb.append(" userId= ").append(userId);
		} else {
			sb.append(" shoppingCartId= '").append(cartId).append("'");
		}
		sb.append(" AND id IN ( ");
		for (int id : ids) {
			sb.append(id).append(",");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");
		ebShoppingCartDao.updateByHql(sb.toString(), new Object[] { checked });
	}

	public void updateAll(List<EbShoppingCart> ebShoppingCarts)
			throws SqlException {
		ebShoppingCartDao.updateAll(ebShoppingCarts);
	}

	public Map<Integer, EbShoppingCart> retrieveShoppingCarts(int userId,
			String cartId) throws SqlException {
		// TODO 去重
		List<EbShoppingCart> ebShoppingCarts = null;
		if (userId > 0) {
			ebShoppingCarts = ebShoppingCartDao.findAllByHql(
					" WHERE userId=? and status=1", new Object[] { userId });
		} else {
			ebShoppingCarts = ebShoppingCartDao.findAllByHql(
					" WHERE shoppingCartId=? and status=1",
					new Object[] { cartId });
		}
		Map<Integer, EbShoppingCart> carts = new HashMap<Integer, EbShoppingCart>();
		// Set<EbShoppingCart> carts = new HashSet<EbShoppingCart>();
		if (ebShoppingCarts != null) {
			List<EbShoppingCart> records = new ArrayList<EbShoppingCart>();
			for (EbShoppingCart foo : ebShoppingCarts) {
				if (!carts.containsKey(foo.getSkuCode())) {
					carts.put(foo.getSkuCode(), foo);
				} else {
					EbShoppingCart c = carts.get(foo.getSkuCode());
					int amount = c.getAmount() + foo.getAmount();
					c.setAmount(amount > 10 ? 10 : amount);
					foo.setStatus(3);
					records.add(c);
					records.add(foo);
				}
			}
			// TODO 多系统操作可能出现脏读
			if (records.size() > 0) { // TODO 这里的事务是read-only的，不支持更新操作
				ebShoppingCartDao.updateAll(records);
			}
		}
		return carts;
	}

	public List<EbShoppingCart> updateAndCombineShoppingCart(int userId,
			String cartId) throws SqlException {
		List<EbShoppingCart> ebShoppingCarts = null;
		StringBuffer sql = new StringBuffer();
		if (userId > 0) {
			sql.append("select * from ytsp_ebiz_shoppingcart c where ")
			   .append(" exists (select 1 from ytsp_ebiz_product p where p.productcode = c.productcode and p.status = 1) ")
			   .append(" AND status=1 and userId = "+userId+" ORDER BY addTime desc ");
			ebShoppingCarts = ebShoppingCartDao.sqlFetch(sql.toString(), EbShoppingCart.class, 0, -1);
//			ebShoppingCarts = ebShoppingCartDao.findAllByHql(
//					" WHERE userId=? AND status=1 ORDER BY addTime desc ",
//					new Object[] { userId });
		} else if (StringUtils.isNoneEmpty(cartId)) {
			sql.append("select * from ytsp_ebiz_shoppingcart c where ")
			   .append(" exists (select 1 from ytsp_ebiz_product p where p.productcode = c.productcode and p.status = 1) ")
			   .append(" AND status=1 and shoppingCartId = '"+cartId+"' ORDER BY addTime desc ");
			ebShoppingCarts = ebShoppingCartDao.sqlFetch(sql.toString(), EbShoppingCart.class, 0, -1);
//			ebShoppingCarts = ebShoppingCartDao.findAllByHql(
//					" WHERE shoppingCartId=? AND status=1  ORDER BY addTime desc ",
//					new Object[] { cartId });
		}
		List<EbShoppingCart> list = new ArrayList<EbShoppingCart>();
		if (ebShoppingCarts != null) {
			List<EbShoppingCart> records = new ArrayList<EbShoppingCart>();
			for (EbShoppingCart foo : ebShoppingCarts) {
				EbShoppingCart r = null;
				for (EbShoppingCart f : list) {
					if (f.getSkuCode().intValue() == foo.getSkuCode().intValue()) {
						r = f;
						foo.setStatus(0);
						records.add(foo);
						break;
					}
				}
				if (r != null) {
					records.remove(r);
					int amount = foo.getAmount() + r.getAmount();
					r.setAmount(amount > 10 ? 10 : amount);
					records.add(r);
				} else {
					list.add(foo);
				}
			}
			if (records.size() > 0) {
				updateAll(records);
			}
		}
		//
		// Map<Integer, EbShoppingCart> carts = new HashMap<Integer,
		// EbShoppingCart>();
		// if (ebShoppingCarts != null) {
		// List<EbShoppingCart> records = new ArrayList<EbShoppingCart>();
		// for (EbShoppingCart foo : ebShoppingCarts) {
		// if (!carts.containsKey(foo.getSkuCode())) {
		// carts.put(foo.getSkuCode(), foo);
		// list.add(foo);
		// } else {
		// EbShoppingCart c = carts.get(foo.getSkuCode());
		// int amount = c.getAmount() + foo.getAmount();
		// c.setAmount(amount > 10 ? 10 : amount);
		// foo.setStatus(0);
		// records.add(foo);
		// records.add(c);
		// }
		// }
		// // TODO 多系统操作可能出现脏读
		// if (records.size() > 0) {
		// updateAll(records);
		// }
		// }

		// return carts;
		return list;
	}
	
	
	/**
	 * 登录的时候合并购物车
	 * 
	 * @param userId
	 * @param cartId
	 * @throws SqlException
	 */
	public void updateShoppingCartByLogin(int userId, String cartId)
			throws SqlException {
		List<EbShoppingCart> userCarts = null;
		if (userId > 0) {
			userCarts = ebShoppingCartDao.findAllByHql(
					" WHERE userId=? and status=1", new Object[] { userId });
		}
		List<EbShoppingCart> clientCarts = null;
		if (StringUtils.isNoneEmpty(cartId)) {
			clientCarts = ebShoppingCartDao.findAllByHql(
					" WHERE shoppingCartId=? and status=1",
					new Object[] { cartId });
		}
		Map<Integer, EbShoppingCart> carts = new HashMap<Integer, EbShoppingCart>();
		List<EbShoppingCart> records = new ArrayList<EbShoppingCart>();

		if (userCarts != null) {
			for (EbShoppingCart foo : userCarts) {
				if (!carts.containsKey(foo.getSkuCode())) {
					carts.put(foo.getSkuCode(), foo);
				} else {
					EbShoppingCart c = carts.get(foo.getSkuCode());
					int amount = c.getAmount() + foo.getAmount();
					c.setAmount(amount > 10 ? 10 : amount);
					foo.setStatus(3);
					records.add(foo);
					records.add(c);
				}
			}
		}
		if (clientCarts != null) {
			for (EbShoppingCart foo : clientCarts) {
				if (!carts.containsKey(foo.getSkuCode())) {
					foo.setUserId(userId);
//					foo.setShoppingCartId("");
					records.add(foo);
					carts.put(foo.getSkuCode(), foo);
				} else {
					EbShoppingCart c = carts.get(foo.getSkuCode());
					int amount = c.getAmount() + foo.getAmount();
					c.setAmount(amount > 10 ? 10 : amount);
					if(c.getChecked() || foo.getChecked()){
						c.setChecked(true);
					}else{
						c.setChecked(false);
					}
					foo.setStatus(3);
					records.add(foo);
					records.add(c);
				}
			}
		}

		// TODO 多系统操作可能出现脏读
		if (records.size() > 0) {
			updateAll(records);
		}
	}
	
	public EbShoppingCart retrieveShoppingCart(int userId, String cartId, int id)
			throws SqlException {
		EbShoppingCart ebShoppingCart = null;
		if (userId > 0) {
			ebShoppingCart = ebShoppingCartDao.findOneByHql(
					" WHERE userId=? and id=? and status=1", new Object[] {
							userId, id });
		} else {
			ebShoppingCart = ebShoppingCartDao.findOneByHql(
					" WHERE shoppingCartId=? and id=? and status=1",
					new Object[] { cartId, id });
		}
		return ebShoppingCart;
	}

	public EbShoppingCart retrieveShoppingCart(int id) throws SqlException {
		EbShoppingCart ebShoppingCart = ebShoppingCartDao.findOneByHql(
				" WHERE id=? and status=1", new Object[] { id });
		return ebShoppingCart;
	}

	/**
	 * @param userId
	 * @param cartId
	 * @return
	 * @throws SqlException
	 */
	public ShoppingCartVO getShoppingCartVO(int userId, String cartId,
			String imageUrl, List<EbShoppingCart> ebShoppingCarts,String version,String platform)
			throws SqlException {
		return processPromotion(ebShoppingCarts, userId, imageUrl,version,platform);
	}

	// /**
	// * @param userId
	// * @param cartId
	// * @return
	// * @throws SqlException
	// */
	// public ShoppingCartVO getShoppingCartVO(int userId, String cartId,
	// String imageUrl, Map<Integer, EbShoppingCart> ebShoppingCarts)
	// throws SqlException {
	// return processPromotion(ebShoppingCarts, userId, imageUrl);
	// }

	private List<ShoppingCartItem> getShoppingCartItems(String imageUrl,
			Collection<EbShoppingCart> carts,String version,String platform) {
		if (carts != null && carts.size() > 0) {
			List<ShoppingCartItem> shoppingCartItems = new ArrayList<ShoppingCartItem>();
			for (EbShoppingCart foo : carts) {
				ShoppingCartItem cartItem = getShoppingCartItem(imageUrl, foo,version,platform);
				if (cartItem != null)
					shoppingCartItems.add(getShoppingCartItem(imageUrl, foo,version,platform));
			}
			return shoppingCartItems;
		}
		return null;
	}

	private ShoppingCartItem getShoppingCartItem(String imageUrl,
			EbShoppingCart ebShoppingCart,String version,String platform) {
		try {
			EbSku ebSku = ebSkuDao.findById(ebShoppingCart.getSkuCode());
			if (ebSku != null && ebSku.getStatus().getValue().intValue() == 1) {
				EbProduct ebProduct = ebSku.getParent();
				ShoppingCartItem item = new ShoppingCartItem();
				item.setAmount(ebShoppingCart.getAmount());
				item.setChecked(true);
				item.setColor(ebShoppingCart.getProductColor());
				item.setId(ebShoppingCart.getId());
				item.setProductCode(ebShoppingCart.getProductCode());
//				item.setProductImage(Util.getFullImageURL(ebShoppingCart.getProductImage()));
				item.setProductImage(Util.getFullImageURLByVersion(ebShoppingCart.getProductImage(),version,platform));
				item.setProductName(ebShoppingCart.getProductName());
				item.setSize(ebShoppingCart.getProductSize());
				item.setSkuCode(ebShoppingCart.getSkuCode());
				item.setUserId(ebShoppingCart.getUserId() == null ? 0
						: ebShoppingCart.getUserId());
				if(ebProduct == null){
					ebProduct = ebProductDao.findById(ebSku.getProductCode());
				}
				item.setMarketPrice(ebProduct.getPrice());
				item.setPrice(ebProduct.getVprice());
				item.setVipPrice(ebProduct.getSvprice());
				int available = ebSku.getStorage().getAvailable();
				item.setStorageNum(available < 0 ? 0 : available);
				item.setOosNum(item.getAmount() - available);
				//设置是否被选中
				item.setChecked(ebShoppingCart.getChecked() == null?true : ebShoppingCart.getChecked());
				return item;
			}
		} catch (SqlException e) {
			e.printStackTrace();
		}
		return null;

	}

	private boolean isMember(int userId) throws SqlException {
		boolean isMember = false;
		CustomerMember customerMember = customerMemberDao.findOneByHql(
				" WHERE customer.id = ? order by endTime desc",
				new Object[] { userId });
		if (customerMember != null) {
			if (customerMember.getValid()) {
				Date endTime = customerMember.getEndTime();
				if (UtilDate.isOverLiveTime(endTime) <= 0) {
					isMember = true;
				}
			}
		}
		return isMember;
	}

	private ShoppingCartVO processPromotion(
			List<EbShoppingCart> ebShoppingCarts, int userId, String imageUrl,String version,String platform) {
		ShoppingCartVO shoppingCartVO = new ShoppingCartVO();
		try {
			if (ebShoppingCarts != null && ebShoppingCarts.size() > 0) {
				int totalCount = 0;
				List<EbShoppingCart> carts = new ArrayList<EbShoppingCart>();
				for (EbShoppingCart c : ebShoppingCarts) {
					totalCount += c.getAmount();
					carts.add(c);
				}
				shoppingCartVO.setTotalCount(totalCount);
				boolean isVIP = false;
				if (userId > 0) {
					isVIP = isMember(userId);
				}
				// 促销活动列表
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
				List<EbPromotion> ebPromotions = ebPromotionDao
						.findAllByHql(
								" WHERE status=1 AND startDate<? AND endDate>? ORDER BY promotionType",
								new Object[] { calendar.getTime(),
										calendar.getTime() });
				if (ebPromotions != null && ebPromotions.size() > 0) {
					List<PromotionVO> promotions = new ArrayList<PromotionVO>();
					for (EbPromotion foo : ebPromotions) {
						PromotionVO promotionVO = getPromotionVO(foo, imageUrl,version,platform);
						if (foo.getIsForAll()) {
							if (!carts.isEmpty()) {
								List<ShoppingCartItem> cartItems = getShoppingCartItems(
										imageUrl, carts,version,platform);
								if (cartItems != null && cartItems.size() > 0) {
									promotionVO.setCartItems(cartItems);
								}
								promotions.add(promotionVO);
								carts.clear();
							}
							break;
						} else {
							EbProductCollection collection = foo
									.getEbProductCollection();
							List<ShoppingCartItem> cartItems = new ArrayList<ShoppingCartItem>();
							List<EbShoppingCart> carts2 = new ArrayList<EbShoppingCart>();

							if (!carts.isEmpty()) {
								Iterator<EbShoppingCart> it = carts.iterator();
								while (it.hasNext()) {
									EbShoppingCart c = it.next();
									if (isInCollection(collection,
											c.getProductCode())) {
										ShoppingCartItem cartItem = getShoppingCartItem(
												imageUrl, c,version,platform);
										if (cartItem != null)
											cartItems.add(cartItem);
									} else {
										carts2.add(c);
									}
								}
								carts = null;
								carts = carts2;
							}
							if (cartItems != null && cartItems.size() > 0) {
								promotionVO.setCartItems(cartItems);
								promotions.add(promotionVO);
							}
						}
					}
					shoppingCartVO.setPromotions(promotions);
				}

				if (carts != null && carts.size() > 0) {
					shoppingCartVO.setCartItems(getShoppingCartItems(imageUrl,
							carts,version,platform));
				}

				// TODO 计算促销优惠
				// 市场总价
				double totalMarketPrice = 0;
				// IKAN价总价（商品总额）
				double totalIkanPrice = 0;
				// 活动优惠
				double totalPromotionReduceFee = 0;
				// VIP会员优惠
				double totalVIPReduceFee = 0;
				// 总计:非VIP(totalIkanPrice-totalPromotionReduceFee)、VIP(totalIkanPrice-totalPromotionReduceFee-totalVIPReduceFee)
				double totalPrice = 0;
				// 已优惠:非VIP(totalPromotionReduceFee)、VIP(totalPromotionReduceFee+totalVIPReduceFee)
				double totalReduceFee = 0;
				if (shoppingCartVO.getPromotions() != null)
					for (PromotionVO foo : shoppingCartVO.getPromotions()) {
						// 当前活动商品总价格
						double pTotalPrioce = 0;
						for (ShoppingCartItem cartItem : foo.getCartItems()) {
							double price = isVIP ? cartItem.getVipPrice()
									: cartItem.getPrice();
							pTotalPrioce += price * cartItem.getAmount();
							totalPrice += price * cartItem.getAmount();

							totalIkanPrice += cartItem.getPrice()
									* cartItem.getAmount();
							totalMarketPrice += cartItem.getMarketPrice()
									* cartItem.getAmount();
							totalVIPReduceFee += (cartItem.getPrice() - cartItem
									.getVipPrice()) * cartItem.getAmount();
						}
						if (foo.getPromotionType() == 0 && pTotalPrioce > 0) {
							PromotionItem item = null;
							for (PromotionItem promotionItem : foo
									.getPromotionItems()) {
								if (pTotalPrioce > promotionItem
										.getStandardPrice()) {
									if (item == null) {
										item = promotionItem;
									} else {
										item = item.getStandardPrice() > promotionItem
												.getStandardPrice() ? item
												: promotionItem;
									}
								}
							}
							if (item != null) {
								foo.setReduceFee(item.getReducePrice());
								totalPromotionReduceFee += item
										.getReducePrice();
							}
						}
					}
				if (shoppingCartVO.getCartItems() != null) {
					for (ShoppingCartItem cartItem : shoppingCartVO
							.getCartItems()) {
						if (cartItem == null) {
							continue;
						}
						double price = isVIP ? cartItem.getVipPrice()
								: cartItem.getPrice();
						totalPrice += price * cartItem.getAmount();

						totalIkanPrice += cartItem.getPrice()
								* cartItem.getAmount();
						totalMarketPrice += cartItem.getMarketPrice()
								* cartItem.getAmount();
						totalVIPReduceFee += (cartItem.getPrice() - cartItem
								.getVipPrice()) * cartItem.getAmount();
					}
				}
				shoppingCartVO.setTotalIkanPrice(totalIkanPrice);
				shoppingCartVO
						.setTotalPromotionReduceFee(totalPromotionReduceFee);
				shoppingCartVO.setTotalVIPReduceFee(totalVIPReduceFee);
				shoppingCartVO.setTotalPrice(totalPrice
						- totalPromotionReduceFee);
				shoppingCartVO.setTotalMarketPrice(totalMarketPrice);
				// 已优惠:非VIP(totalPromotionReduceFee)、VIP(totalPromotionReduceFee+totalVIPReduceFee)
				totalReduceFee = isVIP ? totalPromotionReduceFee
						+ totalPromotionReduceFee : totalPromotionReduceFee;
				shoppingCartVO.setTotalReduceFee(totalReduceFee);
			}
		} catch (SqlException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return shoppingCartVO;
	}

	// private ShoppingCartVO processPromotion(
	// Map<Integer, EbShoppingCart> ebShoppingCarts, int userId,
	// String imageUrl) {
	// ShoppingCartVO shoppingCartVO = new ShoppingCartVO();
	// try {
	// if (ebShoppingCarts != null && ebShoppingCarts.size() > 0) {
	// int totalCount = 0;
	// Set<EbShoppingCart> carts = new HashSet<EbShoppingCart>();
	// Collection<EbShoppingCart> cc = ebShoppingCarts.values();
	// for (EbShoppingCart c : cc) {
	// totalCount += c.getAmount();
	// carts.add(c);
	// }
	// shoppingCartVO.setTotalCount(totalCount);
	// boolean isVIP = false;
	// if (userId > 0) {
	// isVIP = isMember(userId);
	// }
	// // 促销活动列表
	// Calendar calendar = Calendar.getInstance();
	// calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
	// List<EbPromotion> ebPromotions = ebPromotionDao
	// .findAllByHql(
	// " WHERE status=1 AND startDate<? AND endDate>? ORDER BY promotionType",
	// new Object[] { calendar.getTime(),
	// calendar.getTime() });
	// if (ebPromotions != null && ebPromotions.size() > 0) {
	// List<PromotionVO> promotions = new ArrayList<PromotionVO>();
	// for (EbPromotion foo : ebPromotions) {
	// PromotionVO promotionVO = getPromotionVO(foo, imageUrl);
	// if (foo.getIsForAll()) {
	// if (!carts.isEmpty()) {
	// List<ShoppingCartItem> cartItems = getShoppingCartItems(
	// imageUrl, carts);
	// if (cartItems != null && cartItems.size() > 0) {
	// promotionVO.setCartItems(cartItems);
	// }
	// promotions.add(promotionVO);
	// carts.clear();
	// }
	// break;
	// } else {
	// EbProductCollection collection = foo
	// .getEbProductCollection();
	// List<ShoppingCartItem> cartItems = new ArrayList<ShoppingCartItem>();
	//
	// Set<EbShoppingCart> carts2 = new HashSet<EbShoppingCart>();
	//
	// if (!carts.isEmpty()) {
	// Iterator<EbShoppingCart> it = carts.iterator();
	// while (it.hasNext()) {
	// EbShoppingCart c = it.next();
	// if (isInCollection(collection,
	// c.getProductCode())) {
	// ShoppingCartItem cartItem = getShoppingCartItem(
	// imageUrl, c);
	// cartItems.add(cartItem);
	// } else {
	// carts2.add(c);
	// }
	// }
	// carts = null;
	// carts = carts2;
	// }
	// if (cartItems != null && cartItems.size() > 0) {
	// promotionVO.setCartItems(cartItems);
	// promotions.add(promotionVO);
	// }
	// }
	// }
	// shoppingCartVO.setPromotions(promotions);
	// }
	//
	// if (carts != null && carts.size() > 0) {
	// shoppingCartVO.setCartItems(getShoppingCartItems(imageUrl,
	// carts));
	// }
	//
	// // TODO 计算促销优惠
	// // 市场总价
	// double totalMarketPrice = 0;
	// // IKAN价总价（商品总额）
	// double totalIkanPrice = 0;
	// // 活动优惠
	// double totalPromotionReduceFee = 0;
	// // VIP会员优惠
	// double totalVIPReduceFee = 0;
	// //
	// 总计:非VIP(totalIkanPrice-totalPromotionReduceFee)、VIP(totalIkanPrice-totalPromotionReduceFee-totalVIPReduceFee)
	// double totalPrice = 0;
	// //
	// 已优惠:非VIP(totalPromotionReduceFee)、VIP(totalPromotionReduceFee+totalVIPReduceFee)
	// double totalReduceFee = 0;
	// if (shoppingCartVO.getPromotions() != null)
	// for (PromotionVO foo : shoppingCartVO.getPromotions()) {
	// // 当前活动商品总价格
	// double pTotalPrioce = 0;
	// for (ShoppingCartItem cartItem : foo.getCartItems()) {
	// double price = isVIP ? cartItem.getVipPrice()
	// : cartItem.getPrice();
	// pTotalPrioce += price * cartItem.getAmount();
	// totalPrice += price * cartItem.getAmount();
	//
	// totalIkanPrice += cartItem.getPrice()
	// * cartItem.getAmount();
	// totalMarketPrice += cartItem.getMarketPrice()
	// * cartItem.getAmount();
	// totalVIPReduceFee += (cartItem.getPrice() - cartItem
	// .getVipPrice()) * cartItem.getAmount();
	// }
	// if (foo.getPromotionType() == 0 && pTotalPrioce > 0) {
	// PromotionItem item = null;
	// for (PromotionItem promotionItem : foo
	// .getPromotionItems()) {
	// if (pTotalPrioce > promotionItem
	// .getStandardPrice()) {
	// if (item == null) {
	// item = promotionItem;
	// } else {
	// item = item.getStandardPrice() > promotionItem
	// .getStandardPrice() ? item
	// : promotionItem;
	// }
	// }
	// }
	// if (item != null) {
	// foo.setReduceFee(item.getReducePrice());
	// totalPromotionReduceFee += item
	// .getReducePrice();
	// }
	// }
	// }
	// if (shoppingCartVO.getCartItems() != null) {
	// for (ShoppingCartItem cartItem : shoppingCartVO
	// .getCartItems()) {
	// if (cartItem == null) {
	// continue;
	// }
	// double price = isVIP ? cartItem.getVipPrice()
	// : cartItem.getPrice();
	// totalPrice += price * cartItem.getAmount();
	//
	// totalIkanPrice += cartItem.getPrice()
	// * cartItem.getAmount();
	// totalMarketPrice += cartItem.getMarketPrice()
	// * cartItem.getAmount();
	// totalVIPReduceFee += (cartItem.getPrice() - cartItem
	// .getVipPrice()) * cartItem.getAmount();
	// }
	// }
	// shoppingCartVO.setTotalIkanPrice(totalIkanPrice);
	// shoppingCartVO
	// .setTotalPromotionReduceFee(totalPromotionReduceFee);
	// shoppingCartVO.setTotalVIPReduceFee(totalVIPReduceFee);
	// shoppingCartVO.setTotalPrice(totalPrice
	// - totalPromotionReduceFee);
	// shoppingCartVO.setTotalMarketPrice(totalMarketPrice);
	// //
	// 已优惠:非VIP(totalPromotionReduceFee)、VIP(totalPromotionReduceFee+totalVIPReduceFee)
	// totalReduceFee = isVIP ? totalPromotionReduceFee
	// + totalPromotionReduceFee : totalPromotionReduceFee;
	// shoppingCartVO.setTotalReduceFee(totalReduceFee);
	// }
	// } catch (SqlException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return shoppingCartVO;
	// }

	private boolean isInCollection(EbProductCollection ebProductCollection,
			int productCode) throws SqlException {

		EbProduct ebProduct = ebProductDao.findById(productCode);
		if (ebProduct == null || ebProductCollection == null) {
			return false;
		}
		if (StringUtils.isNotEmpty(ebProductCollection.getBrandIds())) {// 品牌
			if (ebProduct.getEbBrand() != null
					&& ebProductCollection.getBrandIds().contains(
							ebProduct.getEbBrand().getBrandId().toString())) {
				return true;
			}
		}
		if (StringUtils.isNotEmpty(ebProductCollection.getCategoryIds())) {// 分类
			if (ebProduct.getEbCatagory() != null
					&& ebProductCollection.getCategoryIds().contains(
							ebProduct.getEbCatagory().getId().toString())) {
				return true;
			}
		}
		if (StringUtils.isNotEmpty(ebProductCollection.getProductCodes())) {// 商品
			if (ebProductCollection.getProductCodes().contains(
					ebProduct.getProductCode().toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将EbPromotion转换成PromotionVO
	 * 
	 * @param ebPromotion
	 * @return
	 * @throws SqlException
	 */
	private PromotionVO getPromotionVO(EbPromotion ebPromotion, String imageUrl,String version,String platform)
			throws SqlException {
		PromotionVO promotionVO = new PromotionVO();
		promotionVO.setForAll(ebPromotion.getIsForAll());
		promotionVO.setStartDate(ebPromotion.getStartDate());
		promotionVO.setEndDate(ebPromotion.getEndDate());
		promotionVO.setPromotionId(ebPromotion.getPromotionId());
		promotionVO.setPromotionName(ebPromotion.getPromotionName());
		promotionVO.setPromotionType(ebPromotion.getPromotionType().getValue()
				.intValue());
		promotionVO.setSpecialId(ebPromotion.getEbSpecial() == null ? 0
				: ebPromotion.getEbSpecial().getId());
		// TODO promotionVO.setSpecialType(specialType);

		promotionVO.setPromotionItems(getPromotionItems(
				ebPromotion.getPromotionId(),
				ebPromotion.getEbPromotionItems(), imageUrl,version,platform));
		return promotionVO;
	}

	/**
	 * 将EbPromotionItem转换成PromotionItem
	 * 
	 * @param promotionItems
	 * @return
	 * @throws SqlException
	 */
	private List<PromotionItem> getPromotionItems(int promotionId,
			Set<EbPromotionItem> promotionItems, String imageUrl,String version,String platform)
			throws SqlException {
		List<PromotionItem> items = new ArrayList<PromotionItem>();
		Iterator<EbPromotionItem> iterator = promotionItems.iterator();
		while (iterator.hasNext()) {
			EbPromotionItem ebPromotionItem = iterator.next();
			PromotionItem promotionItem = new PromotionItem();
			if (ebPromotionItem.getIsOptional() != null)
				promotionItem.setOptional(ebPromotionItem.getIsOptional());
			if (!StringUtils.isEmpty(ebPromotionItem.getGifts()))
				promotionItem.setOptionalGifts(getOptionGifts(promotionId,
						ebPromotionItem.getPromotionItemId(),
						ebPromotionItem.getGiftSkuCodes(), imageUrl,version,platform));
			if (!promotionItem.isOptional()
					&& promotionItem.getOptionalGifts() != null
					&& promotionItem.getOptionalGifts().size() > 0) {
				promotionItem.setGitf(promotionItem.getOptionalGifts().get(0));
			}
			promotionItem.setPromotionItemId(ebPromotionItem
					.getPromotionItemId());
			promotionItem.setPromotionItemName(ebPromotionItem.getItemName());
			if (ebPromotionItem.getReducePrice() != null)
				promotionItem.setReducePrice(ebPromotionItem.getReducePrice());
			if (ebPromotionItem.getStandardPrice() != null)
				promotionItem.setStandardPrice(ebPromotionItem
						.getStandardPrice());
			items.add(promotionItem);
		}
		Collections.sort(items, new Comparator<PromotionItem>() {
			@Override
			public int compare(PromotionItem p1, PromotionItem p2) {
				int ret = 0;
				if (p1.getStandardPrice() > p2.getStandardPrice()) {
					ret = 1;
				} else if (p1.getStandardPrice() < p2.getStandardPrice()) {
					ret = -1;
				}
				return ret;
			}

		});
		return items;
	}

	/**
	 * 获取促销策略的赠品
	 * 
	 * @param skuCodes
	 * @return
	 * @throws SqlException
	 */
	private List<GiftItem> getOptionGifts(int promotionId, int promotionItemId,
			List<Integer> skuCodes, String imageUrl,String version,String platform) throws SqlException {
		List<GiftItem> giftItems = new ArrayList<GiftItem>();
		StringBuffer sb = new StringBuffer("WHERE skuCode IN ( ");
		for (int id : skuCodes) {
			sb.append(id).append(",");
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");
		List<EbSku> ebSkus = ebSkuDao.findAllByHql(sb.toString());
		for (EbSku ebSku : ebSkus) {
			GiftItem giftItem = new GiftItem(promotionId, promotionItemId,
					ebSku.getProductCode(), ebSku.getProductName(),
					ebSku.getSkuCode(), ebSku.getColor(), ebSku.getSize(), 1,
//					Util.getFullImageURL(ebSku.getParent().getImgUrl()),
					Util.getFullImageURLByVersion(ebSku.getParent().getImgUrl(),version,platform),
					ebSku.getStorage().getAvailable().intValue());
			giftItems.add(giftItem);
		}
		return giftItems;
	}

	/**
	 * 购物车商品件数
	 * 
	 * @param userId
	 * @param cartId
	 * @return
	 * @throws SqlException
	 */
	public int getCount(int userId, String cartId) throws SqlException {
		return ebShoppingCartDao.getShoppingCartCount(userId, cartId);
	}

	private boolean isFirstOrder(int userId) throws SqlException {
		return ebOrderDao.findOneByHql("WHERE userId=? AND status IN (2,1,-8)",
				new Object[] { userId }) == null;
	}

}
