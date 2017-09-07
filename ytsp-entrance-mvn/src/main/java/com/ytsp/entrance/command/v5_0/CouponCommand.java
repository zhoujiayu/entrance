package com.ytsp.entrance.command.v5_0;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductCollection;
import com.ytsp.db.domain.EbPromotion;
import com.ytsp.db.domain.EbPromotionItem;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.enums.EbCouponTypeEnum;
import com.ytsp.db.enums.EbPromotionTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.CouponItem;
import com.ytsp.db.vo.CouponVO;
import com.ytsp.db.vo.ShoppingCartItem;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.v3_1.MemberServiceV31;
import com.ytsp.entrance.service.v5_0.EbCouponService;
import com.ytsp.entrance.service.v5_0.EbProductCellectionService;
import com.ytsp.entrance.service.v5_0.EbPromotionService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DoubleUtil;
import com.ytsp.entrance.util.Util;

public class CouponCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_COUPON_LIST
				|| code == CommandList.CMD_COUPON_EXCHANGE_AND_USE
				|| code == CommandList.CMD_COUPON_USE
				|| code == CommandList.CMD_COUPON_EXCHANGE
				|| code == CommandList.CMD_COUPON_OBTAIN
				|| code == CommandList.CMD_COUPON_MINE
				|| code == CommandList.CMD_COUPON_CASH_BYPAGE
				|| code == CommandList.CMD_COUPON_REDUCE_BYPAGE
				|| code == CommandList.CMD_COUPON_UNABLE_CASH_BYPAGE
				|| code == CommandList.CMD_COUPON_UNABLE_REDUCE_BYPAGE
				|| code == CommandList.CMD_COUPON_MINE_AVAILABLE
				|| code == CommandList.CMD_COUPON_MINE_UNAVAILABLE
				|| code == CommandList.CMD_COUPON_LIST_AVAILABLE
				|| code == CommandList.CMD_COUPON_LIST_UNAVAILABLE
				|| code == CommandList.CMD_COUPON_LIST_BY_TYPE;
	}

	@Override
	public ExecuteResult execute() {
		// 验证权限.
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}

		int code = getContext().getHead().getCommandCode();
		if (code == CommandList.CMD_COUPON_LIST) {
			return couponList(userId);
		} else if (code == CommandList.CMD_COUPON_EXCHANGE) {
			return couponExchange(userId);
		} else if (code == CommandList.CMD_COUPON_EXCHANGE_AND_USE) {
			return couponExchangeAndUse(userId);
		} else if (code == CommandList.CMD_COUPON_USE) {
			return couponUse(userId);
		} else if (code == CommandList.CMD_COUPON_OBTAIN) {
			return couponObtain(userId);
		} else if (code == CommandList.CMD_COUPON_MINE) {
			return queryMyCoupon(userId);
		} else if (code == CommandList.CMD_COUPON_CASH_BYPAGE) {
			return queryMyCashCouponByPage(userId);
		} else if (code == CommandList.CMD_COUPON_REDUCE_BYPAGE) {
			return queryMyReduceCouponByPage(userId);
		} else if (code == CommandList.CMD_COUPON_UNABLE_CASH_BYPAGE) {
			return queryMyUnableCashCouponByPage(userId);
		} else if (code == CommandList.CMD_COUPON_UNABLE_REDUCE_BYPAGE) {
			return queryMyUnableReduceCouponByPage(userId);
		} else if (code == CommandList.CMD_COUPON_MINE_AVAILABLE) {
			return queryMyAvailableCoupon(userId);
		} else if (code == CommandList.CMD_COUPON_MINE_UNAVAILABLE) {
			return queryMyUnavailableCoupon(userId);
		} else if (code == CommandList.CMD_COUPON_LIST_AVAILABLE) {
			return couponAvailableList(userId);
		} else if (code == CommandList.CMD_COUPON_LIST_UNAVAILABLE) {
			return couponUnavailableList(userId);
		} else if(code == CommandList.CMD_COUPON_LIST_BY_TYPE) {
			return queryMyCouponByType(userId);
		}
		return null;
	}

	/**
	 * <p>
	 * 功能描述:根据传入的Type类型，获取未使用，已使用，已过期的优惠券
	 * </p>
	 * type为0：代表未使用的优惠券，1：代表已使用过的优惠券 2代表已过期
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyCouponByType(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		try {
			if (reqBody.isNull("type")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"查询我的优惠券类型不能为空", null, this);
			}
			int type = reqBody.optInt("type", 0);
			int isGetNum = reqBody.optInt("isGetNum", 0);

			EbCouponService couponServ = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 用户所有优惠券列表
			List<EbCoupon> coupons = couponServ.getCouponByUserIdAndType(
					userId, type);
			// 满减券列表
			List<CouponItem> reduceCoupons = new ArrayList<CouponItem>();
			// 现金券列表
			List<CouponItem> cashCoupons = new ArrayList<CouponItem>();
			
			CouponVO couponVo = new CouponVO();
			Gson gson = new Gson();
			JSONObject res = new JSONObject();
			couponCount(res,couponServ,isGetNum,userId);
			if(coupons == null || coupons.size() <= 0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"查询我的可使用的优惠券成功", res, this);
			}
			for (EbCoupon ebCoupon : coupons) {
				// 现金券
				if (ebCoupon.getMinAmount() == null
						|| ebCoupon.getMinAmount() == 0d) {
					// 用现金券
					cashCoupons.add(new CouponItem(ebCoupon));
				} else {
					// 可使用优惠券
					reduceCoupons.add(new CouponItem(ebCoupon));
				}
			}
			couponVo.setCashCoupons(cashCoupons);
			couponVo.setReduceCoupons(reduceCoupons);
			JSONObject result = new JSONObject(gson.toJson(couponVo));
			couponCount(result,couponServ,isGetNum,userId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"查询我的可使用的优惠券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyCouponByType() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	
	private JSONObject  couponCount(JSONObject res,EbCouponService couponServ,int isGetNum,int userId){
		// 获取优惠券的数量，isGetNum调用时是否要获取数量参数
		try {
			if (isGetNum == 1) {
				// 获取未使用的数量
				res.put("notUsedNum",
						couponServ.getMyCouponQuantityByType(userId, 0));
				// 获取已使用的数量
				res.put("usedNum",
						couponServ.getMyCouponQuantityByType(userId, 1));
				// 获取过期的数量
				res.put("overdueNum",
						couponServ.getMyCouponQuantityByType(userId, 2));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return res;
	}

	/**
	 * 入参：page,pageSize,couponId最后一个现金券id
	 * <p>
	 * 功能描述:分页获取我的不可用满减券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyUnableCashCouponByPage(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			int page = -1;
			int pageSize = -1;
			int couponId = 0;
			if (reqBody.isNull("page")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"page不能为空", result, this);
			}
			if (reqBody.isNull("pageSize")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"pageSize不能为空", result, this);
			}
			page = reqBody.getInt("page");
			pageSize = reqBody.getInt("pageSize");
			couponId = reqBody.getInt("couponId");

			EbCouponService couponServ = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 获取用户不可用的现金券
			List<EbCoupon> couponList = couponServ.getUnableCashCouponByPage(
					userId, couponId, page * pageSize, pageSize);
			// 构建返回优惠券VO
			List<CouponItem> couponItems = buildCouponItemList(couponList);
			Gson gson = new Gson();
			result.put("unableCashCoupons", gson.toJson(couponItems));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"查询我的不可用现金券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyUnableCashCouponByPage() error,"
					+ " HeadInfo :" + getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 入参：page,pageSize,couponId最后一个现金券id
	 * <p>
	 * 功能描述:分页获取我的可用满减券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyUnableReduceCouponByPage(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			int page = -1;
			int pageSize = -1;
			int couponId = 0;
			if (reqBody.isNull("page")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"page不能为空", result, this);
			}
			if (reqBody.isNull("pageSize")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"pageSize不能为空", result, this);
			}
			page = reqBody.getInt("page");
			pageSize = reqBody.getInt("pageSize");
			couponId = reqBody.getInt("couponId");

			EbCouponService couponServ = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 获取用户不可用的满减券
			List<EbCoupon> couponList = couponServ.getUnableReduceCouponByPage(
					userId, couponId, page * pageSize, pageSize);
			// 构建返回优惠券VO
			List<CouponItem> couponItems = buildCouponItemList(couponList);
			Gson gson = new Gson();
			result.put("unableReduceCoupons", gson.toJson(couponItems));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"查询我的不可用满减券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyCoupon() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 入参：page,pageSize,couponId最后一个现金券id
	 * <p>
	 * 功能描述:分页获取我的可用满减券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyReduceCouponByPage(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			int page = -1;
			int pageSize = -1;
			int couponId = 0;
			if (reqBody.isNull("page")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"page不能为空", result, this);
			}
			if (reqBody.isNull("pageSize")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"pageSize不能为空", result, this);
			}
			page = reqBody.getInt("page");
			pageSize = reqBody.getInt("pageSize");
			couponId = reqBody.getInt("couponId");

			EbCouponService couponServ = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 获取用户可用的满减券
			List<EbCoupon> couponList = couponServ.getReduceCouponByPage(
					userId, couponId, page * pageSize, pageSize);
			// 构建返回优惠券VO
			List<CouponItem> couponItems = buildCouponItemList(couponList);
			Gson gson = new Gson();
			result.put("reduceCoupons", gson.toJson(couponItems));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"查询我的可用满减券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyReduceCouponByPage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:构建优惠券VO
	 * </p>
	 * <p>
	 * 参数：@param couponList
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<CouponItem>
	 * </p>
	 */
	private List<CouponItem> buildCouponItemList(List<EbCoupon> couponList) {
		List<CouponItem> couponItems = new ArrayList<CouponItem>();
		if (couponList == null || couponList.size() <= 0) {
			return couponItems;
		}
		for (EbCoupon coupon : couponList) {
			couponItems.add(new CouponItem(coupon));
		}
		return couponItems;
	}

	/**
	 * <p>
	 * 功能描述:分页获取我的可用现金券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyCashCouponByPage(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			int page = -1;
			int pageSize = -1;
			int couponId = 0;
			if (reqBody.isNull("page")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"page不能为空", result, this);
			}
			if (reqBody.isNull("pageSize")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"pageSize不能为空", result, this);
			}
			page = reqBody.getInt("page");
			pageSize = reqBody.getInt("pageSize");
			couponId = reqBody.getInt("couponId");

			EbCouponService couponServ = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 获取用户可用的现金券
			List<EbCoupon> couponList = couponServ.getCashCouponByPage(userId,
					couponId, page * pageSize, pageSize);
			// 构建返回优惠券VO
			List<CouponItem> couponItems = buildCouponItemList(couponList);
			Gson gson = new Gson();
			result.put("cashCoupons", gson.toJson(couponItems));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"查询我的可用现金券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyCashCouponByPage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:获取我的可使用的优惠券:不可使用满减券和现金券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyUnavailableCoupon(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();

		try {
			int page = -1;
			int pageSize = -1;
			if (!reqBody.isNull("page")) {
				page = reqBody.getInt("page");
			}
			if (!reqBody.isNull("pageSize")) {
				pageSize = reqBody.getInt("pageSize");
			}

			// 用户所有优惠券列表
			List<EbCoupon> coupons = getCustomerCoupons(userId);
			// 不可使用满减券列表
			List<CouponItem> unableReduceCoupons = new ArrayList<CouponItem>();
			// 不可使用现金券列表
			List<CouponItem> unableCashCoupons = new ArrayList<CouponItem>();
			for (EbCoupon ebCoupon : coupons) {
				// 现金券
				if (ebCoupon.getMinAmount() == null
						|| ebCoupon.getMinAmount() == 0d) {
					// 不可使用现金券
					if (!isCouponCanUse(ebCoupon)) {
						unableCashCoupons.add(new CouponItem(ebCoupon));
					}
				} else {
					// 不可使用优惠券
					if (!isCouponCanUse(ebCoupon)) {
						unableReduceCoupons.add(new CouponItem(ebCoupon));
					}
				}
			}
			int unableReduceNum = unableReduceCoupons.size();
			int unableCashNum = unableCashCoupons.size();
			CouponVO couponVo = new CouponVO();
			couponVo.setUnableCashCoupons(pageCoupons(unableCashCoupons, page,
					pageSize));
			couponVo.setUnableReduceCoupons(pageCoupons(unableReduceCoupons,
					page, pageSize));
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(couponVo));
			result.put("unableReduceNum", unableReduceNum);
			result.put("unableCashNum", unableCashNum);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"查询我的不可使用的优惠券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyCoupon() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:获取我的可使用的优惠券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyAvailableCoupon(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		try {
			int page = -1;
			int pageSize = -1;
			if (!reqBody.isNull("page")) {
				page = reqBody.getInt("page");
			}
			if (!reqBody.isNull("pageSize")) {
				pageSize = reqBody.getInt("pageSize");
			}

			// 用户所有优惠券列表
			List<EbCoupon> coupons = getCustomerCoupons(userId);
			// 可使用满减券列表
			List<CouponItem> reduceCoupons = new ArrayList<CouponItem>();
			// 可使用现金券列表
			List<CouponItem> cashCoupons = new ArrayList<CouponItem>();
			for (EbCoupon ebCoupon : coupons) {
				// 现金券
				if (ebCoupon.getMinAmount() == null
						|| ebCoupon.getMinAmount() == 0d) {
					// 可使用现金券
					if (isCouponCanUse(ebCoupon)) {
						cashCoupons.add(new CouponItem(ebCoupon));
					}
				} else {
					// 可使用优惠券
					if (isCouponCanUse(ebCoupon)) {
						reduceCoupons.add(new CouponItem(ebCoupon));
					}
				}
			}
			int cashNum = cashCoupons.size();
			int reducNum = reduceCoupons.size();
			CouponVO couponVo = new CouponVO();
			couponVo.setCashCoupons(pageCoupons(cashCoupons, page, pageSize));
			couponVo.setReduceCoupons(pageCoupons(reduceCoupons, page, pageSize));
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(couponVo));
			result.put("cashNum", cashNum);
			result.put("reducNum", reducNum);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"查询我的可使用的优惠券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyCoupon() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:我的优惠券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyCoupon(int userId) {
		JSONObject reqBody = getContext().getBody().getBodyObject();

		try {
			int page = -1;
			int pageSize = -1;
			if (!reqBody.isNull("page")) {
				page = reqBody.getInt("page");
			}
			if (!reqBody.isNull("pageSize")) {
				pageSize = reqBody.getInt("pageSize");
			}

			// 用户所有优惠券列表
			List<EbCoupon> coupons = getCustomerCoupons(userId);
			// 可使用满减券列表
			List<CouponItem> reduceCoupons = new ArrayList<CouponItem>();
			// 不可使用满减券列表
			List<CouponItem> unableReduceCoupons = new ArrayList<CouponItem>();
			// 可使用现金券列表
			List<CouponItem> cashCoupons = new ArrayList<CouponItem>();
			// 不可使用现金券列表
			List<CouponItem> unableCashCoupons = new ArrayList<CouponItem>();
			for (EbCoupon ebCoupon : coupons) {
				// 现金券
				if (ebCoupon.getMinAmount() == null
						|| ebCoupon.getMinAmount() == 0d) {
					// 可使用现金券
					if (isCouponCanUse(ebCoupon)) {
						cashCoupons.add(new CouponItem(ebCoupon));
					} else {
						unableCashCoupons.add(new CouponItem(ebCoupon));
					}
				} else {
					// 可使用优惠券
					if (isCouponCanUse(ebCoupon)) {
						reduceCoupons.add(new CouponItem(ebCoupon));
					} else {
						unableReduceCoupons.add(new CouponItem(ebCoupon));
					}
				}
			}
			int cashNum = cashCoupons.size();
			int reducNum = reduceCoupons.size();
			int unableReduceNum = unableReduceCoupons.size();
			int unableCashNum = unableCashCoupons.size();
			CouponVO couponVo = new CouponVO();
			couponVo.setCashCoupons(pageCoupons(cashCoupons, page, pageSize));
			couponVo.setReduceCoupons(pageCoupons(reduceCoupons, page, pageSize));
			couponVo.setUnableCashCoupons(pageCoupons(unableCashCoupons, page,
					pageSize));
			couponVo.setUnableReduceCoupons(pageCoupons(unableReduceCoupons,
					page, pageSize));
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(couponVo));
			result.put("cashNum", cashNum);
			result.put("reducNum", reducNum);
			result.put("unableReduceNum", unableReduceNum);
			result.put("unableCashNum", unableCashNum);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"查询我的优惠券成功", result, this);
		} catch (Exception e) {
			logger.error("queryMyCoupon() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:将数组分页
	 * </p>
	 * <p>
	 * 参数：@param coupons
	 * <p>
	 * 参数：@param page
	 * <p>
	 * 参数：@param pageSize
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<CouponItem>
	 * </p>
	 */
	private List<CouponItem> pageCoupons(List<CouponItem> coupons, int page,
			int pageSize) {
		if (coupons == null || coupons.size() <= 0) {
			return coupons;
		}
		if (coupons.size() <= pageSize) {
			return coupons;
		}
		if (page == -1 && pageSize == -1) {
			return coupons;
		}

		return coupons.subList(page, pageSize);
	}

	/**
	 * <p>
	 * 功能描述:优惠券是否可使用
	 * </p>
	 * <p>
	 * 参数：@param ebCoupon
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isCouponCanUse(EbCoupon ebCoupon) {
		if (isvalid(ebCoupon.getStartTime(), ebCoupon.getEndTime())
				&& !ebCoupon.getUsed()) {
			return true;
		}
		return false;
	}

	/**
	 * <p>
	 * 功能描述:当前时间是否有效
	 * </p>
	 * <p>
	 * 参数：@param startTime
	 * <p>
	 * 参数：@param endTime
	 * <p>
	 * 参数：@return 返回 true为有效，否则为过期
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isvalid(Date startTime, Date endTime) {
		Date now = new Date();
		// 若没有开始时间，只比较结束时间有没有过期
		if (startTime == null) {
			return endTime.after(now);
		}
		// 若没有结束时间，按无期限算
		if (endTime == null) {
			return startTime.before(now);
		}
		return (startTime.before(now) && endTime.after(now));
	}

	/**
	 * <p>
	 * 功能描述:获取用户下的所有优惠券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private List<EbCoupon> getCustomerCoupons(int userId) throws SqlException {
		EbCouponService couponServ = SystemInitialization
				.getApplicationContext().getBean(EbCouponService.class);
		return couponServ.getCouponByUserId(userId);
	}

	private ExecuteResult couponObtain(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject ret = new JSONObject();
		try {
			if (jsonObj.isNull("couponId")) {
				ret.put("msg", "优惠券不存在");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"优惠券不存在", ret, this);
			}
			int couponId = jsonObj.optInt("couponId", 0);
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			EbCoupon coupon = ebCouponService.getCoupon(couponId);
			if (coupon.getCouponType() != EbCouponTypeEnum.TEMPLET) {
				ret.put("msg", "不是模板类优惠券");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"不是模板类优惠券", ret, this);
			}
			Date now = new Date();
			// 增加优惠券未开始不能领取校验
			if (coupon.getStartTime() == null
					|| coupon.getStartTime().after(now)) {
				ret.put("msg", "未到领取时间!");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"未到领取时间!", ret, this);
			}
			if (isCouponObtain(userId, couponId)) {
				ret.put("msg", "已领取");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "已领取",
						ret, this);
			}
			if (!(coupon.getStartTime().before(now) && coupon.getEndTime()
					.after(now))) {
				ret.put("msg", "手慢了，优惠券已失效");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"手慢了，优惠券已失效", ret, this);
			}
			if (ebCouponService
					.saveObtainEbCoupon(userId, coupon, getContext())) {
				ret.put("msg", "领取成功");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"领取成功", ret, this);
			} else {
				// String msg = "已经领取了" + coupon.getAvailableCount() +
				// "张，不能再领取了";
				String msg = "手慢了，已领完";
				ret.put("msg", msg);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, msg,
						ret, this);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (SqlException e) {
			e.printStackTrace();
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "领取失败",
				null, this);
	}

	/**
	 * <p>
	 * 功能描述:校验专场同一批次同种金额的优惠券只能领一张
	 * </p>
	 * <p>
	 * 参数：@param userId 用户id
	 * <p>
	 * 参数：@param couponId 专场优惠券模板id
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isCouponObtain(int userId, int couponId) {
		EbCouponService couponServ = SystemInitialization
				.getApplicationContext().getBean(EbCouponService.class);
		return couponServ.isCouponObtain(userId, couponId);
	}

	/**
	 * 使用优惠券<br/>
	 * 入参:int couponId,double totalPrice,double shipping,List<ShoppingCartItem>
	 * cartItems <br/>
	 * 
	 * @param userId
	 * @return
	 */
	private ExecuteResult couponUse(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			JSONObject obj = new JSONObject();
			if (!jsonObj.isNull("couponId")) {
				int couponId = jsonObj.getInt("couponId");
				EbCoupon ebCoupon = ebCouponService.retrieveEbCoupon(userId,
						couponId);
				if (ebCoupon == null) {
					obj.put("result", false);
					obj.put("msg", "请选择优惠券！");
				} else if (ebCoupon.getUsed()) {
					obj.put("result", false);
					obj.put("msg", "当前优惠券已使用过！");
				} else {
					JSONArray jsonArray = jsonObj.getJSONArray("cartItems");
					Set<Integer> productSet = new HashSet<Integer>();
					Map<Integer, EbSku> skuMap = getEbSkuHash(jsonArray,
							productSet);
					Map<Integer, EbProduct> productMap = getEbProductHash(productSet);
					List<ShoppingCartItem> productList = getShoppingCartItems(jsonArray);
					List<ShoppingCartItem> products = useCouponProductCellectionProcess(
							productList, skuMap, productMap, userId);

					if (products == null || products.size() == 0) {
						obj.put("result", false);
						obj.put("msg", "当前所购商品不能使用优惠券！");
					} else {
						// VIP、促销优惠之后的总价
						double ableProductTotalPrice = 0;
						List<ShoppingCartItem> record = new ArrayList<ShoppingCartItem>();
						for (ShoppingCartItem foo : products) {//
							ableProductTotalPrice += foo.getPrice()
									* foo.getAmount();
						}
						double productTotalPrice = 0d;
						boolean isInSet = false;
						if (ebCoupon.getEbProductCollection() == null) {// 全场券
							productTotalPrice = ableProductTotalPrice;
							isInSet = true;
							record.addAll(products);
						} else {// 限制部分商品可使用
							for (ShoppingCartItem foo : products) {
								if (skuMap.containsKey(foo.getSkuCode())) {
									EbSku ebSku = skuMap.get(foo.getSkuCode());
									EbProduct prod = productMap.get(ebSku
											.getProductCode());
									if (isInCollection(
											ebCoupon.getEbProductCollection(),
											prod)) {
										productTotalPrice += foo.getPrice()
												* foo.getAmount();
										isInSet = true;
										record.add(foo);
									}
								}
							}
						}
						Date now = new Date();
						if (ebCoupon.getEndTime().after(now)
								&& ebCoupon.getStartTime().before(now)) {
							if (isInSet) {
								if (ebCoupon.getMinAmount() == null
										|| productTotalPrice >= ebCoupon
												.getMinAmount()) {
									obj.put("result", true);
									double totalPrice = jsonObj
											.optDouble("totalPrice");
									obj.put("totalPrice", totalPrice);
									// 运费(促销活动之后的价格)
									// double shipping =
									// countShipping(totalPrice);
									double shipping = Util.computeShipping(
											totalPrice, userId);
									
									// 优惠券额度
									double money = productTotalPrice >= ebCoupon
											.getMoney().doubleValue() ? ebCoupon
											.getMoney().doubleValue()
											: productTotalPrice;
									money = money > totalPrice ? totalPrice
											: money;
									// //////////////////////////////////////////////////
									int useCredits = getCanUseCredits(userId,
											record, productList, skuMap,
											productMap, money,
											totalPrice);
									// TODO 计算下可使用的积分
									obj.put("useCredits", useCredits);
									// //////////////////////////////////////////////////
									// 优惠券面值余额
									double couponBalance = ebCoupon.getMoney()
											.doubleValue() - money;
									if (couponBalance > 0
											&& ebCoupon.getForShipping() != null
											&& ebCoupon.getForShipping()
													.booleanValue()) {// 余额抵用运费
										money += shipping > couponBalance ? couponBalance
												: shipping;
									}
									obj.put("couponReduceFee", money);
									obj.put("shipping", shipping);
									double payPrice = 0;
									// 实付金额
									payPrice = totalPrice + shipping - money;
									if (payPrice < 0) {
										payPrice = 0;
									}
									if(payPrice == 0 || useCredits < 0){
										obj.put("useCredits", 0);
									}
									obj.put("payPrice", payPrice);
								} else {
									obj.put("result", false);
									obj.put("msg",
											"当前券要求订单金额：￥"
													+ ebCoupon.getMinAmount()
													+ "您当前订单金额：￥"
													+ productTotalPrice);
								}
							} else {
								obj.put("result", false);
								obj.put("msg", "当前券不可用于购买当前所选商品");
							}
						} else {
							obj.put("result", false);
							obj.put("msg", "当前券不在使用时间范围内！");
						}
					}
				}
			} else {
				obj.put("result", false);
				obj.put("msg", "请选择优惠券！");
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "", obj,
					this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 积分的计算只用到了ikanPrice和vipPrice，而不是优惠活动之后的价格，故在这里将活动优惠的部分减去
	 * 
	 * @功能描述:计算可使用的积分
	 * @param skuMap
	 * @param productCodeSet
	 * @param cartItems
	 * @param userId
	 * @param promotionReduceCredits
	 *            积分的使用是受促销活动的影响的，
	 *            但在这里积分计算过程中使用的是ikanPrice或vipPrice，而不是优惠活动之后的部分
	 *            ，也就是说可使用的积分多计算了，而promotionReduceCredits保存多计算的部分
	 * @param isMember
	 * @return
	 * @throws SqlException
	 */
	private int getCanUseCredits(int userId, List<ShoppingCartItem> record,
			List<ShoppingCartItem> productList, Map<Integer, EbSku> skuMap,
			Map<Integer, EbProduct> productMap, double money,
			double productTotalPrice) throws SqlException {
		double useCredits = 0;
		MemberServiceV31 memberServiceV31 = SystemInitialization
				.getApplicationContext().getBean(MemberServiceV31.class);
		boolean isMember = memberServiceV31.isMember(userId);
		Set<Integer> recordSet = new HashSet<Integer>();
		for (ShoppingCartItem c : record) {
			recordSet.add(c.getSkuCode());
			EbSku ebSku = skuMap.get(c.getSkuCode());
			if (ebSku != null) {
				double cFee = c.getPrice() * c.getAmount();
				cFee = cFee - money * cFee / productTotalPrice;
				EbProduct prod = productMap.get(ebSku.getProductCode());
//				useCredits += (isMember ? cFee * 2
//						* prod.getCreditPercentage() : cFee
//						* prod.getCreditPercentage());
				
				useCredits = DoubleUtil.add(
						useCredits,
						isMember ? DoubleUtil.mul(cFee * 2,
								prod.getCreditPercentage()) : DoubleUtil.mul(
								cFee, prod.getCreditPercentage()));
				
			}
		}
		for (ShoppingCartItem c : productList) {
			EbSku ebSku = skuMap.get(c.getSkuCode());
			if (ebSku != null && !recordSet.contains(ebSku.getSkuCode())) {
				EbProduct prod = productMap.get(ebSku.getProductCode());
				double cFee = c.getPrice() * c.getAmount();
				useCredits += (isMember ? cFee * 2
						* prod.getCreditPercentage() : cFee
						* prod.getCreditPercentage());
			}
		}
		return (int)DoubleUtil.round(useCredits, 0);
	}

	/**
	 * 
	 * @功能描述: 计算邮费，第1单满39包邮，非第一单满68包邮
	 * @param totalPrice
	 * @return
	 * @throws SqlException
	 *             double
	 * @author yusf
	 */
	private double countShipping(double totalPrice) throws SqlException {
		double shipping = 8.0d;
		SessionCustomer sc = getSessionCustomer();
		int userId = sc.getCustomer() == null ? 0 : sc.getCustomer().getId();
		EbOrderService orderService = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		boolean isFirst = orderService.isFirstOrder(userId);
		if (isFirst && totalPrice >= 39.0d) {
			shipping = 0d;
		} else if (!isFirst && totalPrice >= 68.0d) {
			shipping = 0d;
		}
		return shipping;
	}

	/**
	 * 兑换优惠券<br/>
	 * 入参：String serialNumber
	 * 
	 * @param userId
	 * @return
	 */
	private ExecuteResult couponExchange(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			JSONObject obj = new JSONObject();
			if (!jsonObj.isNull("serialNumber")) {
				String serialNumber = jsonObj.getString("serialNumber");
				EbCoupon ebCoupon = ebCouponService
						.retrieveEbCoupon(serialNumber);
				if (ebCoupon == null) {
					obj.put("result", false);
					obj.put("msg", "不存在该兑换码,请仔细核对兑换码！");
				} else {
					if (ebCoupon.getUserId() != null
							&& ebCoupon.getUserId().intValue() != 0) {
						obj.put("result", false);
						obj.put("msg", "当前券已兑换！请仔细核对兑换码！");
					} else if (ebCoupon.getStartTime().after(new Date())) {
						obj.put("result", false);
						obj.put("msg", "当前券尚不能使用！请仔细核对优惠券使用日期！");
					} else if (ebCoupon.getEndTime().before(new Date())) {
						obj.put("result", false);
						obj.put("msg", "当前券已过期！请仔细核对兑换码！");
					} else {
						ebCoupon.setUserId(userId);
						ebCouponService.update(ebCoupon);
						obj.put("msg", "兑换成功");
						obj.put("result", true);
						obj.put("couponId", ebCoupon.getId());
						Util.addStatistics(getContext(), ebCoupon);
					}
				}
			} else {
				obj.put("result", false);
				obj.put("msg", "兑换码不能为空！");
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "", obj,
					this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * 不可使用优惠券列表<br/>
	 * 入参：double totalPrice,List<ShoppingCartItem> cartItems
	 * 
	 * 规则：
	 * 
	 * 1.价格影响：VIP优惠会影响促销活动，促销活动会影响优惠券使用<br/>
	 * 2.积分的使用不影响优惠券的使用<br/>
	 * 3.有些产品是不可以使用优惠券的，在相应的商品集合里（全局有效）,处理之前优先刨除这些商品<br/>
	 * 4.全场券（现金、满减）、限产品的券（现金、满减）<br/>
	 * 5.每单限用一张优惠券
	 * 
	 * 注意：价格判断一定要注意临界值，totalPrice>=coupon.getMinMoney(),而不是
	 * 
	 * @param userId
	 * @return
	 */
	private ExecuteResult couponUnavailableList(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 用户的优惠券列表（未过期、未使用）
			List<EbCoupon> ebCoupons = ebCouponService
					.retrieveValidEbCouponList(userId);
			CouponVO couponVO = new CouponVO();
			JSONObject obj = new JSONObject();
			Gson gson = new Gson();
			// 用户没有优惠券，直接返回
			if (ebCoupons == null || ebCoupons.size() <= 0) {
				obj.put("couponVO", gson.toJson(couponVO));
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"获取可使用优惠券列表成功", obj, this);
			}

			if (jsonObj.isNull("cartItems")) {
				obj.put("couponVO", gson.toJson(couponVO));
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"获取可使用优惠券列表成功", obj, this);
			}
			JSONArray jsonArray = jsonObj.getJSONArray("cartItems");
			// 所购商品的ProductCode集合
			Set<Integer> productCodeSet = new HashSet<Integer>();
			// TODO 要买的商品(SKU)
			Map<Integer, EbSku> skuMap = getEbSkuHash(jsonArray, productCodeSet);
			// TODO 要买的商品(Product)
			Map<Integer, EbProduct> productMap = getEbProductHash(productCodeSet);
			// 所购商品中可以使用优惠券的那部分商品
			List<ShoppingCartItem> cartItems = useCouponProductCellectionProcess(
					getShoppingCartItems(jsonArray), skuMap, productMap, userId);

			// VIP、促销优惠之后的总价
			double totalPrice = 0;
			for (ShoppingCartItem foo : cartItems) {//
				totalPrice += foo.getPrice() * foo.getAmount();
			}
			// 不可用满减券
			List<CouponItem> unableReduceCoupons = new ArrayList<CouponItem>();
			// 不可用现金券
			List<CouponItem> unableCashCoupons = new ArrayList<CouponItem>();
			for (EbCoupon ebCoupon : ebCoupons) {
				CouponItem couponItem = new CouponItem(ebCoupon);
				Date now = new Date();
				if (ebCoupon.getEndTime().after(now)
						&& ebCoupon.getStartTime().before(now)) {// 有效期范围内，再次过滤
					if (ebCoupon.getEbProductCollection() == null) {// 全场
						if (ebCoupon.getMinAmount() != null
								&& ebCoupon.getMinAmount() > 0d) {// 满减
							// 不可用满减券
							if (totalPrice < ebCoupon.getMinAmount()) {
								String balance = ""+DoubleUtil.sub(ebCoupon.getMinAmount(),
										totalPrice);
								couponItem
										.setReason(IConstants.COUPONMONEYREASON
												.replaceAll("MONEY",
														balance));
								unableReduceCoupons.add(couponItem);
							}
						}
					} else { // 限制使用范围
						// 限制范围
						EbProductCollection ebProductCollection = ebCoupon
								.getEbProductCollection();
						// 所买商品在限制范围内的总价
						double productTotalPrice = 0d;
						// TODO
						for (ShoppingCartItem foo : cartItems) {
							int skuCode = foo.getSkuCode();
							// 款型
							EbSku ebSku = skuMap.get(skuCode);
							if (ebSku != null) {
								EbProduct ebProduct = productMap.get(ebSku
										.getProductCode());
								if (isInCollection(ebProductCollection,
										ebProduct)) {// 在使用范围内，累加价格
									productTotalPrice += foo.getPrice()
											* foo.getAmount();
								}
							}
						}

						if (productTotalPrice > 0) {// 商品清单中有在限制范围内的商品
							if (ebCoupon.getMinAmount() != null
									&& ebCoupon.getMinAmount() > 0) {// 满减券
								if (productTotalPrice < ebCoupon.getMinAmount()) {
									String balance = ""+DoubleUtil.sub(ebCoupon.getMinAmount(),
											totalPrice);
									couponItem
											.setReason(IConstants.COUPONMONEYREASON
													.replaceAll("MONEY",
															balance));
									unableReduceCoupons.add(couponItem);
								}
							}
						} else {// 所购商品不在限制范围内
							couponItem.setReason(IConstants.COUPONRANGEREASON);
							if (ebCoupon.getMinAmount() == null
									|| ebCoupon.getMinAmount() == 0) {// 现金券
								unableCashCoupons.add(couponItem);
							} else {// 满减券
								unableReduceCoupons.add(couponItem);
							}
						}
					}
				} else {
					couponItem.setReason(IConstants.COUPONUSEDATEREASON);
					if (ebCoupon.getMinAmount() == null
							|| ebCoupon.getMinAmount() == 0) {// 现金券
						unableCashCoupons.add(couponItem);
					} else {// 满减券
						unableReduceCoupons.add(couponItem);
					}
				}
			}
			couponVO.setUnableCashCoupons(unableCashCoupons);
			couponVO.setUnableReduceCoupons(unableReduceCoupons);

			obj.put("couponVO", gson.toJson(couponVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取不可使用优惠券列表成功", obj, this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * 可使用优惠券列表<br/>
	 * 入参：double totalPrice,List<ShoppingCartItem> cartItems
	 * 
	 * 规则：
	 * 
	 * 1.价格影响：VIP优惠会影响促销活动，促销活动会影响优惠券使用<br/>
	 * 2.积分的使用不影响优惠券的使用<br/>
	 * 3.有些产品是不可以使用优惠券的，在相应的商品集合里（全局有效）,处理之前优先刨除这些商品<br/>
	 * 4.全场券（现金、满减）、限产品的券（现金、满减）<br/>
	 * 5.每单限用一张优惠券
	 * 
	 * 注意：价格判断一定要注意临界值，totalPrice>=coupon.getMinMoney(),而不是
	 * 
	 * @param userId
	 * @return
	 */
	private ExecuteResult couponAvailableList(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 用户的优惠券列表（未过期、未使用）
			List<EbCoupon> ebCoupons = ebCouponService
					.retrieveValidEbCouponList(userId);
			CouponVO couponVO = new CouponVO();
			JSONObject obj = new JSONObject();
			Gson gson = new Gson();
			// 用户没有优惠券，直接返回
			if (ebCoupons == null || ebCoupons.size() <= 0) {
				obj.put("couponVO", gson.toJson(couponVO));
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"获取可使用优惠券列表成功", obj, this);
			}

			if (jsonObj.isNull("cartItems")) {
				obj.put("couponVO", gson.toJson(couponVO));
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"获取可使用优惠券列表成功", obj, this);
			}

			JSONArray jsonArray = jsonObj.getJSONArray("cartItems");
			// 所购商品的ProductCode集合
			Set<Integer> productCodeSet = new HashSet<Integer>();
			// TODO 要买的商品(SKU)
			Map<Integer, EbSku> skuMap = getEbSkuHash(jsonArray, productCodeSet);
			// TODO 要买的商品(Product)
			Map<Integer, EbProduct> productMap = getEbProductHash(productCodeSet);
			// 所购商品中可以使用优惠券的那部分商品
			List<ShoppingCartItem> cartItems = useCouponProductCellectionProcess(
					getShoppingCartItems(jsonArray), skuMap, productMap, userId);

			// VIP、促销优惠之后的总价
			double totalPrice = 0;
			for (ShoppingCartItem foo : cartItems) {//
				totalPrice += foo.getPrice() * foo.getAmount();
			}
			// 满减券
			List<CouponItem> reduceCoupons = new ArrayList<CouponItem>();
			// 现金券
			List<CouponItem> cashCoupons = new ArrayList<CouponItem>();
			for (EbCoupon ebCoupon : ebCoupons) {
				CouponItem couponItem = new CouponItem(ebCoupon);
				Date now = new Date();
				if (ebCoupon.getEndTime().after(now)
						&& ebCoupon.getStartTime().before(now)) {// 有效期范围内，再次过滤
					if (ebCoupon.getEbProductCollection() == null) {// 全场
						if (ebCoupon.getMinAmount() == null
								|| ebCoupon.getMinAmount() == 0d) {// 现金券
							cashCoupons.add(couponItem);
						} else {// 满减券
							if (totalPrice >= ebCoupon.getMinAmount()) {
								reduceCoupons.add(couponItem);
							}
						}
					} else {
						// 限制范围
						EbProductCollection ebProductCollection = ebCoupon
								.getEbProductCollection();
						// 所买商品在限制范围内的总价
						double productTotalPrice = 0d;
						// TODO
						for (ShoppingCartItem foo : cartItems) {
							int skuCode = foo.getSkuCode();
							// 款型
							EbSku ebSku = skuMap.get(skuCode);
							if (ebSku != null) {
								EbProduct ebProduct = productMap.get(ebSku
										.getProductCode());
								if (isInCollection(ebProductCollection,
										ebProduct)) {// 在使用范围内，累加价格
									productTotalPrice += foo.getPrice()
											* foo.getAmount();
								}
							}
						}

						if (productTotalPrice > 0) {// 商品清单中有在限制范围内的商品
							if (ebCoupon.getMinAmount() == null
									|| ebCoupon.getMinAmount() == 0) {// 现金券
								cashCoupons.add(couponItem);
							} else {// 满减券
								if (productTotalPrice >= ebCoupon
										.getMinAmount()) {
									reduceCoupons.add(couponItem);
								}
							}
						} else {// 所购商品不在限制范围内
							couponItem.setReason("所购商品不在当前优惠券使用范围内");
						}
					}
				} else {
					couponItem.setReason("当前优惠券不再使用时间范围内");
				}
			}
			couponVO.setCashCoupons(cashCoupons);
			couponVO.setReduceCoupons(reduceCoupons);

			obj.put("couponVO", gson.toJson(couponVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取优惠券列表成功", obj, this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * 优惠券列表<br/>
	 * 入参：double totalPrice,List<ShoppingCartItem> cartItems
	 * 
	 * 规则：
	 * 
	 * 1.价格影响：VIP优惠会影响促销活动，促销活动会影响优惠券使用<br/>
	 * 2.积分的使用不影响优惠券的使用<br/>
	 * 3.有些产品是不可以使用优惠券的，在相应的商品集合里（全局有效）,处理之前优先刨除这些商品<br/>
	 * 4.全场券（现金、满减）、限产品的券（现金、满减）<br/>
	 * 5.每单限用一张优惠券
	 * 
	 * 注意：价格判断一定要注意临界值，totalPrice>=coupon.getMinMoney(),而不是
	 * 
	 * @param userId
	 * @return
	 */
	private ExecuteResult couponList(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			// 用户的优惠券列表（未过期、未使用）
			List<EbCoupon> ebCoupons = ebCouponService
					.retrieveValidEbCouponList(userId);
			CouponVO couponVO = new CouponVO();
			if (ebCoupons != null && ebCoupons.size() > 0) {
				if (!jsonObj.isNull("cartItems")) {
					JSONArray jsonArray = jsonObj.getJSONArray("cartItems");
					// 所购商品的ProductCode集合
					Set<Integer> productCodeSet = new HashSet<Integer>();
					// TODO 要买的商品(SKU)
					Map<Integer, EbSku> skuMap = getEbSkuHash(jsonArray,
							productCodeSet);
					// TODO 要买的商品(Product)
					Map<Integer, EbProduct> productMap = getEbProductHash(productCodeSet);
					// 所购商品中可以使用优惠券的那部分商品，这里加了促销活动的处理
					List<ShoppingCartItem> cartItems = useCouponProductCellectionProcess(
							getShoppingCartItems(jsonArray), skuMap,
							productMap, userId);

					// VIP、促销优惠之后的总价
					double totalPrice = 0;
					for (ShoppingCartItem foo : cartItems) {//
						totalPrice += foo.getPrice() * foo.getAmount();
					}
					// 满减券
					List<CouponItem> reduceCoupons = new ArrayList<CouponItem>();
					// 现金券
					List<CouponItem> cashCoupons = new ArrayList<CouponItem>();
					// 不可用满减券
					List<CouponItem> unableReduceCoupons = new ArrayList<CouponItem>();
					// 不可用现金券
					List<CouponItem> unableCashCoupons = new ArrayList<CouponItem>();
					for (EbCoupon ebCoupon : ebCoupons) {
						CouponItem couponItem = new CouponItem(ebCoupon);
						Date now = new Date();
						if (ebCoupon.getEndTime().after(now)
								&& ebCoupon.getStartTime().before(now)) {// 有效期范围内，再次过滤
							if (ebCoupon.getEbProductCollection() == null) {// 全场
								if (ebCoupon.getMinAmount() == null
										|| ebCoupon.getMinAmount() == 0) {// 现金券
									cashCoupons.add(couponItem);
								} else {// 满减券
									if (totalPrice >= ebCoupon.getMinAmount()) {
										reduceCoupons.add(couponItem);
									} else {
										String balance = ""+DoubleUtil.sub(ebCoupon.getMinAmount(),
												totalPrice);
										couponItem
												.setReason(IConstants.COUPONMONEYREASON
														.replaceAll("MONEY",
																balance));
										unableReduceCoupons.add(couponItem);
									}
								}
							} else { // 限制使用范围
								// 限制范围
								EbProductCollection ebProductCollection = ebCoupon
										.getEbProductCollection();
								// 所买商品在限制范围内的总价
								double productTotalPrice = 0d;
								// TODO
								for (ShoppingCartItem foo : cartItems) {
									int skuCode = foo.getSkuCode();
									// 款型
									EbSku ebSku = skuMap.get(skuCode);
									if (ebSku != null) {
										EbProduct ebProduct = productMap
												.get(ebSku.getProductCode());
										if (isInCollection(ebProductCollection,
												ebProduct)) {// 在使用范围内，累加价格
											productTotalPrice += foo.getPrice()
													* foo.getAmount();
										}
									}
								}

								if (productTotalPrice > 0) {// 商品清单中有在限制范围内的商品
									if (ebCoupon.getMinAmount() == null
											|| ebCoupon.getMinAmount() == 0) {// 现金券
										cashCoupons.add(couponItem);
									} else {// 满减券
										if (productTotalPrice >= ebCoupon
												.getMinAmount()) {
											reduceCoupons.add(couponItem);
										} else {
											String balance = ""+DoubleUtil.sub(ebCoupon.getMinAmount(),
													totalPrice);
											couponItem
													.setReason(IConstants.COUPONMONEYREASON
															.replaceAll("MONEY",
																	balance));
											unableReduceCoupons.add(couponItem);
										}
									}
								} else {// 所购商品不在限制范围内
									couponItem.setReason(IConstants.COUPONRANGEREASON);
									if (ebCoupon.getMinAmount() == null
											|| ebCoupon.getMinAmount() == 0) {// 现金券
										unableCashCoupons.add(couponItem);
									} else {// 满减券
										unableReduceCoupons.add(couponItem);
									}
								}
							}
						} else {
							couponItem.setReason(IConstants.COUPONUSEDATEREASON);
							if (ebCoupon.getMinAmount() == null
									|| ebCoupon.getMinAmount() == 0) {// 现金券
								unableCashCoupons.add(couponItem);
							} else {// 满减券
								unableReduceCoupons.add(couponItem);
							}
						}
					}
					couponVO.setCashCoupons(cashCoupons);
					couponVO.setReduceCoupons(reduceCoupons);
					couponVO.setUnableCashCoupons(unableCashCoupons);
					couponVO.setUnableReduceCoupons(unableReduceCoupons);
				}

			}
			JSONObject obj = new JSONObject();
			Gson gson = new Gson();
			obj.put("couponVO", gson.toJson(couponVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取优惠券列表成功", obj, this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * @功能描述:校验商品是否包含在商品集合中
	 * @param ebProductCollection
	 * @param ebProduct
	 * @return boolean
	 * @author yusf
	 */
	private boolean isInCollection(EbProductCollection ebProductCollection,
			EbProduct ebProduct) {
		if (ebProduct == null || ebProductCollection == null) {
			return false;
		}
		if (isContaints(ebProductCollection.getBrandIds(),
				ebProduct.getEbBrand() == null ? "" : ebProduct.getEbBrand()
						.getBrandId().toString())) {// 品牌
			return true;
		}
		if (isContaints(ebProductCollection.getCategoryIds(),
				ebProduct.getEbCatagory() == null ? "" : ebProduct
						.getEbCatagory().getId().toString())) {// 分类
			return true;
		}
		if (isContaints(ebProductCollection.getProductCodes(), ebProduct
				.getProductCode().toString())) {// 商品
			return true;
		}
		return false;
	}

	private boolean isContaints(String str, String c) {
		if (StringUtils.isNotEmpty(str)) {
			String[] strArr = str.split(",");
			for (String s : strArr) {
				if (s.compareTo(c) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 兑换优惠券并使用<br/>
	 * 入参:String serialNumber,double totalPrice,double
	 * shipping,List<ShoppingCartItem> cartItems <br/>
	 * 
	 * @return
	 */
	private ExecuteResult couponExchangeAndUse(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			EbCouponService ebCouponService = SystemInitialization
					.getApplicationContext().getBean(EbCouponService.class);
			JSONObject obj = new JSONObject();
			if (!jsonObj.isNull("serialNumber")) {
				String serialNumber = jsonObj.getString("serialNumber");
				EbCoupon ebCoupon = ebCouponService
						.retrieveEbCoupon(serialNumber);
				if (ebCoupon == null) {
					obj.put("result", false);
					obj.put("msg", "不存在该兑换码,请仔细核对兑换码！");
				} else {
					if (ebCoupon.getUserId() != null
							&& ebCoupon.getUserId().intValue() != 0) {
						obj.put("result", false);
						obj.put("msg", "当前券已兑换！请仔细核对兑换码！");
					} else if (ebCoupon.getStartTime().after(new Date())) {
						obj.put("result", false);
						obj.put("msg", "当前券尚不能使用！请仔细核对优惠券使用日期！");
					} else if (ebCoupon.getEndTime().before(new Date())) {
						obj.put("result", false);
						obj.put("msg", "当前券已过期！请仔细核对兑换码！");
					} else {
						JSONArray jsonArray = jsonObj.getJSONArray("cartItems");
						Set<Integer> productSet = new HashSet<Integer>();
						Map<Integer, EbSku> skuMap = getEbSkuHash(jsonArray,
								productSet);
						Map<Integer, EbProduct> productMap = getEbProductHash(productSet);
						// 传过来的数据
						List<ShoppingCartItem> productList = getShoppingCartItems(jsonArray);
						// 去除不可使用优惠券的商品
						List<ShoppingCartItem> products = useCouponProductCellectionProcess(
								productList, skuMap, productMap, userId);

						if (products == null || products.size() == 0) {
							obj.put("result", false);
							obj.put("msg", "当前所购商品不能使用优惠券！");
						} else {
							// VIP、促销优惠之后的总价
							double ableProductTotalPrice = 0;
							for (ShoppingCartItem foo : products) {//
								ableProductTotalPrice += foo.getPrice()
										* foo.getAmount();
							}
							boolean isInSet = false;
							double productTotalPrice = 0d;
							List<ShoppingCartItem> record = new ArrayList<ShoppingCartItem>();
							if (ebCoupon.getEbProductCollection() == null) {// 全场券
								productTotalPrice = ableProductTotalPrice;
								isInSet = true;
								record.addAll(products);
							} else {// 限制部分商品可使用
								for (ShoppingCartItem foo : products) {
									if (skuMap.containsKey(foo.getSkuCode())) {
										EbSku ebSku = skuMap.get(foo
												.getSkuCode());
										EbProduct prod = productMap.get(ebSku
												.getProductCode());
										if (isInCollection(
												ebCoupon.getEbProductCollection(),
												prod)) {
											productTotalPrice += foo.getPrice()
													* foo.getAmount();
											isInSet = true;
											record.add(foo);
										}
									}
								}
							}
							Date now = new Date();
							if (ebCoupon.getEndTime().after(now)
									&& ebCoupon.getStartTime().before(now)) {// 再次对使用日期过滤
								if (isInSet) {
									if (ebCoupon.getMinAmount() == null
											|| productTotalPrice >= ebCoupon
													.getMinAmount()) {
										ebCoupon.setUserId(userId);
										ebCouponService.update(ebCoupon);
										obj.put("msg", "兑换成功");
										obj.put("result", true);
										double totalPrice = jsonObj
												.optDouble("totalPrice");
										obj.put("totalPrice", totalPrice);
										// 运费(促销活动之后的价格)
										// double shipping =
										// countShipping(totalPrice);
										double shipping = Util.computeShipping(
												totalPrice, userId);
										// 优惠券额度
										double money = productTotalPrice >= ebCoupon
												.getMoney().doubleValue() ? ebCoupon
												.getMoney().doubleValue()
												: productTotalPrice;
										money = money > totalPrice ? totalPrice
												: money;
										// //////////////////////////////////////////////////
										int useCredits = getCanUseCredits(
												userId, record, productList,
												skuMap, productMap, money,
												totalPrice);
										// TODO 计算下可使用的积分
										obj.put("useCredits", useCredits);
										// //////////////////////////////////////////////////
										// 优惠券面值余额
										double couponBalance = ebCoupon
												.getMoney().doubleValue()
												- money;
										if (couponBalance > 0
												&& ebCoupon.getForShipping() != null
												&& ebCoupon.getForShipping()
														.booleanValue()) {// 余额抵用运费
											money += shipping > couponBalance ? couponBalance
													: shipping;
										}
										obj.put("shipping", shipping);
										obj.put("couponReduceFee", money);
										double payPrice = 0;
										// 应付金额
										payPrice = totalPrice + shipping
												- money;
										if (payPrice < 0) {
											payPrice = 0;
										}
										if(payPrice == 0 || useCredits < 0){
											obj.put("useCredits", 0);
										}
										obj.put("payPrice", payPrice);
										obj.put("couponId", ebCoupon.getId());
										obj.put("couponName",
												ebCoupon.getCouponName());
									} else {
										obj.put("result", false);
										obj.put("msg",
												"当前券要求订单金额：￥"
														+ ebCoupon
																.getMinAmount()
														+ "您当前订单金额：￥"
														+ productTotalPrice);
									}
								} else {
									obj.put("result", false);
									obj.put("msg", "当前券不可用于购买当前所选商品");
								}
							} else {
								obj.put("result", false);
								obj.put("msg", "当前券不在使用时间范围内！");
							}
						}
					}
				}
			} else {
				obj.put("result", false);
				obj.put("msg", "兑换码不能为空！");
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "", obj,
					this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * @功能描述: 将Sku列表转换成Map,其key值为skuCode,value值为Sku对象
	 * @return Map<Integer,EbSku>
	 * @author yusf
	 */
	private Map<Integer, EbSku> getEbSkuHash(JSONArray cartItems,
			Set<Integer> productSet) throws Exception {
		EbSkuService ebSkuService = SystemInitialization
				.getApplicationContext().getBean(EbSkuService.class);
		List<EbSku> allSkus = ebSkuService
				.retrieveEbSkuBySkuCodes(getSkuCodeSet(cartItems));
		Map<Integer, EbSku> retMap = new HashMap<Integer, EbSku>();
		for (int i = 0; i < allSkus.size(); i++) {
			EbSku sku = allSkus.get(i);
			if (sku == null) {
				continue;
			}
			retMap.put(sku.getSkuCode(), sku);
			productSet.add(sku.getProductCode());
		}
		return retMap;
	}

	/**
	 * @功能描述:
	 * @param cartItems
	 * @return
	 * @throws JSONException
	 *             Set<Integer>
	 * @author yusf
	 */
	private Set<Integer> getSkuCodeSet(JSONArray cartItems)
			throws JSONException {
		Set<Integer> skuCodeSet = new HashSet<Integer>();
		int size = cartItems.length();
		for (int i = 0; i < size; i++) {
			JSONObject obj = cartItems.getJSONObject(i);
			Integer skuCode = obj.optInt("skuCode", 0);
			if (skuCode == null || skuCode.intValue() == 0) {
				continue;
			}
			skuCodeSet.add(skuCode);
		}
		return skuCodeSet;
	}

	/**
	 * @功能描述: 将EbProduct列表转换成Map,其key值为productCode,value值为EbProduct对象
	 * @return Map<Integer,EbSku>
	 * @author yusf
	 */
	private Map<Integer, EbProduct> getEbProductHash(Set<Integer> productSet)
			throws Exception {
		EbProductService ebProductService = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> productList = ebProductService
				.retrieveEbProductByCodes(productSet);
		Map<Integer, EbProduct> retMap = new HashMap<Integer, EbProduct>();
		for (int i = 0; i < productList.size(); i++) {
			EbProduct product = productList.get(i);
			if (product == null || product.getProductCode() == null) {
				continue;
			}
			retMap.put(product.getProductCode(), product);
		}
		return retMap;
	}

	private List<ShoppingCartItem> getShoppingCartItems(JSONArray array)
			throws JSONException {
		List<ShoppingCartItem> cartItems = new ArrayList<ShoppingCartItem>();
		if (array != null) {
			int size = array.length();
			for (int i = 0; i < size; i++) {
				JSONObject obj = array.getJSONObject(i);
				if (!obj.isNull("skuCode")) {
					ShoppingCartItem cartItem = new ShoppingCartItem(
							obj.optInt("skuCode"), obj.optInt("amount", 1),
							obj.optDouble("price"));
					cartItems.add(cartItem);
				}
			}
		}
		return cartItems;
	}

	/**
	 * <p>
	 * 功能描述:四舍五入double类型
	 * </p>
	 * <p>
	 * 参数：@param d
	 * <p>
	 * 参数：@param length
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：double
	 * </p>
	 */
	private double roundDouble(double d, int length) {
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(length, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * @param cartItems
	 * @param skuMap
	 * @param productMap
	 * @param userId
	 * @throws SqlException
	 */
	private void promotionProcess(List<ShoppingCartItem> cartItems,
			Map<Integer, EbSku> skuMap, Map<Integer, EbProduct> productMap,
			int userId) throws SqlException {
		MemberServiceV31 memberServiceV31 = SystemInitialization
				.getApplicationContext().getBean(MemberServiceV31.class);
		boolean isMember = memberServiceV31.isMember(userId);
		for (ShoppingCartItem foo : cartItems) {
			EbSku sku = skuMap.get(foo.getSkuCode());
			EbProduct product = productMap.get(sku.getProductCode());
			foo.setPrice(isMember ? product.getSvprice() : product.getVprice());
			foo.setProductCode(product.getProductCode());
		}

		EbPromotionService ebPromotionService = SystemInitialization
				.getApplicationContext().getBean(EbPromotionService.class);
		List<EbPromotion> ebPromotions = ebPromotionService
				.retrieveEbPromotionList();
		if (ebPromotions != null && ebPromotions.size() > 0) {
			for (EbPromotion foo : ebPromotions) {
				if (foo.getPromotionType() == EbPromotionTypeEnum.REDUCE) {
					// 判断促销政策的有效性
					Date current = new Date();
					if (foo != null
							&& foo.getEbPromotionItems() != null
							&& foo.getEbPromotionItems().size() > 0
							&& foo.getStatus() == ValidStatusEnum.VALID
							&& foo.getPromotionType() == EbPromotionTypeEnum.REDUCE
							&& current.before(foo.getEndDate())
							&& current.after(foo.getStartDate())) {
						double totalPrice = 0d;
						List<ShoppingCartItem> records = new ArrayList<ShoppingCartItem>();
						for (ShoppingCartItem cartItem : cartItems) {
							if (foo.getIsForAll()) {// 促销范围：全部商品
								totalPrice += cartItem.getPrice()
										* cartItem.getAmount();
								records.add(cartItem);
							} else if (foo.getEbProductCollection() != null) {
								if (isInCollection(
										foo.getEbProductCollection(),
										productMap.get(cartItem
												.getProductCode()))) {
									totalPrice += cartItem.getPrice()
											* cartItem.getAmount();
									records.add(cartItem);
								}
							}

						}

						EbPromotionItem item = null;
						if (records.size() > 0) {
							for (EbPromotionItem pItem : foo
									.getEbPromotionItems()) {
								if (totalPrice >= pItem.getStandardPrice()) {
									if (item == null) {
										item = pItem;
									} else {
										item = item.getStandardPrice() > pItem
												.getStandardPrice() ? item
												: pItem;
									}
								}
							}
						}

						if (item != null) {
							double subTotalfee = 0d;
							int count = 0;
							double fee = item.getReducePrice();
							// 开始均摊.
							for (ShoppingCartItem cartItem : records) {
								double subFee = 0;
								// 处理尾差
								if (records.size() - 1 == count) {
									subFee = roundDouble((fee - subTotalfee)
											/ cartItem.getAmount(), 2);
								} else {
									subFee = roundDouble(cartItem.getPrice()
											* cartItem.getAmount() / totalPrice
											* fee / cartItem.getAmount(), 2);
								}
								cartItem.setPrice(roundDouble(
										cartItem.getPrice() - subFee, 2));
								subTotalfee += subFee * cartItem.getAmount();
								count++;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 做两件事情：<br/>
	 * 1.过一遍满减促销，将价格均摊到每件商品上<br/>
	 * 2. 有些产品是不可使用优惠券的，这里将这些商品从所购商品清单中去除
	 * 
	 * 
	 * @param cartItems
	 * @param skuMap
	 * @param productMap
	 * @return
	 * @throws SqlException
	 * @throws JSONException
	 */
	private List<ShoppingCartItem> useCouponProductCellectionProcess(
			List<ShoppingCartItem> cartItems, Map<Integer, EbSku> skuMap,
			Map<Integer, EbProduct> productMap, int userId) throws SqlException {
		// 可使用优惠券的商品清单
		List<ShoppingCartItem> cartItemList = new ArrayList<ShoppingCartItem>();

		promotionProcess(cartItems, skuMap, productMap, userId);

		EbProductCellectionService ebProductCollectionService = SystemInitialization
				.getApplicationContext().getBean(
						EbProductCellectionService.class);
		// 不可使用优惠券的商品集合
		List<EbProductCollection> collections = ebProductCollectionService
				.retrieveNonCouponCollection();
		if (collections == null || collections.size() <= 0) {
			cartItemList.addAll(cartItems);
		} else {
			for (EbProductCollection c : collections) {
				for (ShoppingCartItem foo : cartItems) {
					// 款型
					EbSku ebSku = skuMap.get(foo.getSkuCode());
					if (ebSku == null) {
						continue;
					}
					// 产品
					EbProduct prod = productMap == null ? null : productMap
							.get(ebSku.getProductCode());
					if (!isInCollection(c, prod)) {
						cartItemList.add(foo);
					}
				}
			}
		}
		return cartItemList;
	}
}
