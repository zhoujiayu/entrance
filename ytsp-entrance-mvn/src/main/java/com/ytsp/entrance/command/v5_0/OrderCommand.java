package com.ytsp.entrance.command.v5_0;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.google.gson.JsonSyntaxException;
import com.tencent.wxpay.WXPay;
import com.tencent.wxpay.protocol.pay_protocol.PayReqData;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbComment;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderCouponRecord;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbOrderPromotionRecord;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductCollection;
import com.ytsp.db.domain.EbPromotion;
import com.ytsp.db.domain.EbPromotionItem;
import com.ytsp.db.domain.EbShoppingCart;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbUserAddress;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.EbPromotionTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.PayStatusEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.GiftItem;
import com.ytsp.db.vo.OrderConfirm;
import com.ytsp.db.vo.ShoppingCartItem;
import com.ytsp.db.vo.ShoppingCartItemVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.EbShoppingCartService;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.v3_1.MemberServiceV31;
import com.ytsp.entrance.service.v4_0.CreditService;
import com.ytsp.entrance.service.v4_0.EbUserAddressService;
import com.ytsp.entrance.service.v4_0.MemberServiceV4_0;
import com.ytsp.entrance.service.v5_0.EbCouponService;
import com.ytsp.entrance.service.v5_0.EbProductCellectionService;
import com.ytsp.entrance.service.v5_0.EbProductCommentService;
import com.ytsp.entrance.service.v5_0.EbPromotionService;
import com.ytsp.entrance.service.v5_0.OrderServiceV5_0;
import com.ytsp.entrance.service.v5_0.ProductServiceV5_0;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.DoubleUtil;
import com.ytsp.entrance.util.OrderIdGenerationUtil;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.alipay.AlipaySubmit;

public class OrderCommand extends AbstractCommand {

	private static final String DATEFORMATER = "yyyy-MM-dd H:mm:ss";

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_ORDER_CONFIRM
				|| code == CommandList.CMD_ORDER_CREATE
				|| code == CommandList.CMD_ORDER_MINE
				|| code == CommandList.CMD_ORDER_DETAIL
				|| code == CommandList.CMD_ORDER_CANCEL
				|| code == CommandList.CMD_ORDER_COMPLETE
				|| code == CommandList.CMD_ORDER_DELETE
				|| code == CommandList.CMD_ORDER_WAITCOMMENT
				|| code == CommandList.CMD_ORDER_HAVECOMMENT
				|| code == CommandList.CMD_ORDER_BUY_NOW
				|| code == CommandList.CMD_ORDER_BUY_AGAIN
				|| code == CommandList.CMD_ORDER_PAY_PREPARE
				|| code == CommandList.CMD_EB_ORDER_CLIENT_PAY_SUCCESS_V5
				|| code == CommandList.CMD_ORDER_MINE_QUERY_BY_CATEGORY;
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
		try {
			if (code == CommandList.CMD_ORDER_CONFIRM) {
				return confirm(userId);
			} else if (code == CommandList.CMD_ORDER_CREATE) {
				return createOrder(userId);
			} else if (code == CommandList.CMD_ORDER_MINE) {
				return queryMyOrder(userId);
			} else if (code == CommandList.CMD_ORDER_DETAIL) {
				return queryOrderDetail();
			} else if (code == CommandList.CMD_ORDER_CANCEL) {
				return cancelOrder();
			} else if (code == CommandList.CMD_ORDER_COMPLETE) {
				return completeOrder();
			} else if (code == CommandList.CMD_ORDER_DELETE) {
				return deleteOrder();
			} else if (code == CommandList.CMD_ORDER_WAITCOMMENT) {
				return waitCommentOrderList();
			} else if (code == CommandList.CMD_ORDER_HAVECOMMENT) {
				return haveCommentOrderList();
			} else if (code == CommandList.CMD_ORDER_BUY_NOW) {
				return buyNow();
			} else if (code == CommandList.CMD_ORDER_BUY_AGAIN) {
				return orderBuyAgain();
			} else if (code == CommandList.CMD_ORDER_PAY_PREPARE) {
				return orderPayPrepare();
			} else if (code == CommandList.CMD_EB_ORDER_CLIENT_PAY_SUCCESS_V5) {
				return alipayQuery();
			}else if(code == CommandList.CMD_ORDER_MINE_QUERY_BY_CATEGORY){
				return queryMyOrderByCategory(userId);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	/**
	* <p>功能描述:按订单状态分类查询订单</p>
	* 入参：type(0:全部|1：待付款|2：待收货|3：待评价|4：已取消),
	*     page从0开始,pageSize,orderId上一页最后的订单id。第1页orderId和orderTime传0和空
	*     isGetNum:是否要取所有分类的数量，1代表获取数量，不传或者为0代表不获取数量
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult queryMyOrderByCategory(int userId) {
		JSONObject result = new JSONObject();
		JSONObject body = getContext().getBody().getBodyObject();
		try {
			int page = 0;
			int pageSize = -1;
			String orderTime = null;
			long orderId = 0;
			Integer type = 0;
			if (!body.isNull("pageSize")) {
				pageSize = body.optInt("pageSize");
			}
			if (!body.isNull("page")) {
				page = body.optInt("page");
			}
			if (!body.isNull("orderTime")
					&& StringUtil
							.isNotNullNotEmpty(body.getString("orderTime"))) {
				orderTime = body.optString("orderTime");
			}
			if (!body.isNull("orderId")) {
				orderId = body.optLong("orderId");
			}
			if (!body.isNull("type")) {
				type = body.optInt("type");
			}
			int isGetNum = body.optInt("isGetNum",0);
			List<OrderVO> orderVO = getMyOrderByType(userId, page, pageSize, orderTime, orderId, type);
			Gson gson = new Gson();
			//获取数量
			if(isGetNum == 1){
				result.put("allNum", getMyOrderQuantityByType(userId, 0));
				result.put("waitPayNum", getMyOrderQuantityByType(userId, 1));
				result.put("waitGoodsNum", getMyOrderQuantityByType(userId, 2));
				result.put("waitCommentNum", getMyOrderQuantityByType(userId, 3));
				result.put("cancelNum", getMyOrderQuantityByType(userId, 4));
			}
			result.put("myOrders", gson.toJson(orderVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"分类获取我的订单成功", result, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
	
	
	/**
	* <p>功能描述:支付宝订单查询，若查询支付宝支付成功，更新订单状态</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult alipayQuery() {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		long orderId = jsonObj.optLong("orderId", 0);
		EbOrderService ebOrderService = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		JSONObject result = new JSONObject();
		try {
			EbOrder order = ebOrderService.retrieveOrderByOrderId(orderId);
			if(order == null){
				result.put("result", false);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "订单不存在",
						result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.PAYSUCCESS
					|| order.getStatus() == EbOrderStatusEnum.SUCCESS
					|| order.getStatus() == EbOrderStatusEnum.COMPLETE
					|| order.getStatus() == EbOrderStatusEnum.COMMENT) {
				result.put("result", true);
			} else {
				try {
					int payStatus = AlipaySubmit.queryPaySuccess("", String
							.valueOf(order.getOrderid()), order.getTotalPrice()
							.doubleValue());
					if (payStatus == 1) {
						// TODO 支付成功,支付宝的支付方式是1
						ebOrderService.createOrderPaySuccess(orderId, 1);
						if (order.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
							SystemInitialization.getApplicationContext()
									.getBean(MemberServiceV4_0.class)
									.saveVipPaySuccess(orderId);
						}
						result.put("result", true);
					}else if(payStatus == -2){//支付失败
						result.put("result", false);
					}else {
						// 尚未支付,网络不好？或者？但客户说支付成功了，先确保该commondCode在客户端支付成功之后调用的
						ebOrderService.updateOrderPaySuccess(orderId);
						result.put("result", false);
					}
				} catch (Exception e) {
					result.put("result", false);
					e.printStackTrace();
					
				}
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					result, this);
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:订单支付前准备
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult orderPayPrepare() {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		Gson gson = new Gson();
		try {
			if (reqBody.isNull("orderId")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单ID不能为空!", result, this);
			}
			if (reqBody.isNull("payType")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"支付类型不能为空!", result, this);
			}
			long orderId = reqBody.optLong("orderId");
			Integer payType = reqBody.optInt("payType");
			OrderServiceV5_0 orderServ = SystemInitialization
					.getApplicationContext().getBean(OrderServiceV5_0.class);
			EbOrder order = orderServ.getOrderByOrderId(orderId);

			if (order == null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单不存在!", result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.CANCEL) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单支付超时已被取消，无法进行支付!", result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.PAYSUCCESS
					|| order.getStatus() == EbOrderStatusEnum.COMPLETE
					|| order.getStatus() == EbOrderStatusEnum.COMMENT) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单已支付成功!", result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.RETURN) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单退款处理中!", result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.RETURNSUCCESS) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单已退款成功，无法进行支付!", result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.WAIT) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单状态异常，请刷新页面再试!", result, this);
			}
			// 订单待支付状态，将订单的支付状态置为1支付中
			if (order.getStatus() == EbOrderStatusEnum.ORDERSUCCESS) {
				if (order.getPayStatus() == null
						|| order.getPayStatus() == PayStatusEnum.WAITPAY) {
					order.setPayType(payType);
					order.setPayStatus(PayStatusEnum.PAYING);
					order.setTerminalVersion(getContext().getHead().getVersion());
					orderServ.updateOrder(order);
				}
				if (payType == 3) {
					int isUseNewWXPay = Util.isUseNewWXpay(getContext()
							.getHead().getVersion(), getContext().getHead()
							.getPlatform());
					

					String prepayId = "";
					PayReqData payreqData = null;
					//移动端网站微信支付获取请求参数
					if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.wapmobile)) {
						String code = reqBody.optString("code");
						String currentPageURL = reqBody.optString("currentPageURL");
						prepayId = WXPay.getJSAPIPrepayid(
								String.valueOf(order.getOrderid()), "",
								String.valueOf(order.getOrderid()),
								(int) (Math.round(order.getTotalPrice() * 100)),
								"", "", order.getOrderSource(),code);
						payreqData = WXPay.getWapMobilePayReqData(prepayId,currentPageURL);
					}else {//手机APP微信支付获取请求数据
						prepayId = WXPay.getPrepayid(
								String.valueOf(order.getOrderid()), "",
								String.valueOf(order.getOrderid()),
								(int) (Math.round(order.getTotalPrice() * 100)),
								"", "", order.getOrderSource(),isUseNewWXPay);
						payreqData = WXPay.getPayReqData(prepayId,
								order.getOrderSource(),isUseNewWXPay);
						result.put("payreqData", gson.toJson(payreqData));
					}
					
					result.put("payreqData", gson.toJson(payreqData));
					EbOrderService ebOrderService = SystemInitialization
							.getApplicationContext().getBean(
									EbOrderService.class);
					// 记录日志
					ebOrderService.addOrderPay(order.getOrderid(), getContext()
							.getHead().getUid());
				}
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"订单支付前准备成功", result, this);
		} catch (Exception e) {
			logger.error("orderPayPrepare() error," + " HeadInfo :"
					+ getContext().getHead().toString() + "requestData:"
					+ getContext().getBody().getBodyObject().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:再次购买
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult orderBuyAgain() {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		long orderId = 0;
		try {
			if (reqBody.isNull("orderId")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"orderId不能为空!", result, this);
			}
			orderId = reqBody.getLong("orderId");
			OrderServiceV5_0 orderServ = SystemInitialization
					.getApplicationContext().getBean(OrderServiceV5_0.class);
			EbOrder order = orderServ.getOrderByOrderId(orderId);

			if (order == null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单不存在!", result, this);
			}
			if (order.getOrderType() != EbOrderTypeEnum.NORMAL) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"非普通订单，不能再次购买!", result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.COMPLETE
					|| order.getStatus() == EbOrderStatusEnum.CANCEL
					|| order.getStatus() == EbOrderStatusEnum.COMMENT) {
				orderServ.buyAgain(order,getContext());
			} else {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单状态异常，不能再次购买!", result, this);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "再次购买成功",
					result, this);
		} catch (Exception e) {
			logger.error("orderBuyAgain() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:已评论订单列表
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult haveCommentOrderList() throws SqlException,
			JSONException {
		JSONObject result = new JSONObject();
		JSONObject body = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		String commentTime = null;
		int userId = getContext().getHead().getUid();
		if (!body.isNull("pageSize")) {
			pageSize = body.getInt("pageSize");
		}
		if (!body.isNull("commentTime")
				&& StringUtil.isNotNullNotEmpty(body.getString("commentTime"))) {
			commentTime = body.getString("commentTime");
		}
		if (!body.isNull("page")) {
			page = body.getInt("page");
		}

		EbProductCommentService ebProductCommentService = SystemInitialization
				.getApplicationContext().getBean(EbProductCommentService.class);
		List<EbComment> ebComments = ebProductCommentService
				.retrieveHaveComments(userId, page, commentTime, pageSize);
		List<HaveCommentOrderVO> commentOrderList = getHaveComments(ebComments);
		Gson gson = new Gson();
		result.put("haveCommentOrders", gson.toJson(commentOrderList));
		// TODO 待评论订单数
		if (page <= 0) {
			OrderServiceV5_0 orderServ = SystemInitialization
					.getApplicationContext().getBean(OrderServiceV5_0.class);
			result.put("waitCommentCount",
					orderServ.getWaitCommentOrderCount(userId));
			result.put("haveCommentCount",
					ebProductCommentService.retrieveHaveCommentsCount(userId));
		}
		Util.addStatistics(getContext(), commentOrderList);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取未评论订单成功",
				result, this);
	}

	private List<HaveCommentOrderVO> getHaveComments(List<EbComment> ebComments)
			throws SqlException {
		List<HaveCommentOrderVO> commentOrderList = new ArrayList<HaveCommentOrderVO>();
		if (ebComments == null || ebComments.size() <= 0) {
			return commentOrderList;
		}
		Set<Integer> commtenIds = new HashSet<Integer>();
		for (EbComment foo : ebComments) {
			commtenIds.add(foo.getId());
		}
		OrderServiceV5_0 ebOrderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);

		List<EbOrderDetail> orderDetails = ebOrderService
				.getDetailsByCommentIds(commtenIds);
		//
		String imageHost = SystemManager.getInstance().getSystemConfig()
				.getImgServerUrl();
		// 订单的评论
		Map<Integer, EbComment> commentMap = getCommentHash(ebComments);
		for (EbOrderDetail detail : orderDetails) {
			EbComment comment = commentMap.get(detail.getCommentsId());
			HaveCommentOrderVO vo = createCommentOrderVO(detail, comment,
					imageHost);
			commentOrderList.add(vo);
		}
		return commentOrderList;
	}

	/**
	 * <p>
	 * 功能描述:获取已评论订单VO
	 * </p>
	 * <p>
	 * 参数：@param orderDetails
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<HaveCommentOrderVO>
	 * </p>
	 */
	// private List<HaveCommentOrderVO> getCommentOrderVOs(
	// Set<EbOrderDetail> orderDetails) throws SqlException {
	// if (orderDetails == null || orderDetails.size() <= 0) {
	// return null;
	// }
	// Set<Integer> commtenIds = new HashSet<Integer>();
	// for (EbOrderDetail detail : orderDetails) {
	// if (detail.getCommentsId() == null) {
	// continue;
	// }
	// commtenIds.add(detail.getCommentsId());
	// }
	// EbProductCommentService commentServ = SystemInitialization
	// .getApplicationContext().getBean(EbProductCommentService.class);
	// List<EbComment> comments = commentServ.retrieveCommentByIds(commtenIds);
	// // 订单的评论
	// Map<Integer, EbComment> commentMap = getCommentHash(comments);
	// String imageHost = SystemManager.getInstance().getSystemConfig()
	// .getImgServerUrl();
	// // 已评论订单VO
	// List<HaveCommentOrderVO> ret = new ArrayList<HaveCommentOrderVO>();
	// for (EbOrderDetail detail : orderDetails) {
	// EbComment comment = commentMap.get(detail.getCommentsId());
	// HaveCommentOrderVO vo = createCommentOrderVO(detail, comment,
	// imageHost);
	// ret.add(vo);
	// }
	// return ret;
	// }

	/**
	 * <p>
	 * 功能描述:构建评论hash,key为评论id,value相应的实体类
	 * </p>
	 * <p>
	 * 参数：@param comments
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：Map<Integer,EbComment>
	 * </p>
	 */
	private Map<Integer, EbComment> getCommentHash(List<EbComment> comments) {
		if (comments == null || comments.size() <= 0) {
			return null;
		}
		Map<Integer, EbComment> commentMap = new HashMap<Integer, EbComment>();
		for (EbComment ebComment : comments) {
			commentMap.put(ebComment.getId(), ebComment);
		}
		return commentMap;
	}

	/**
	 * <p>
	 * 功能描述:构建已评论订单的VO
	 * </p>
	 * <p>
	 * 参数：@param detail
	 * <p>
	 * 参数：@param comment
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：HaveCommentOrderVO
	 * </p>
	 */
	private HaveCommentOrderVO createCommentOrderVO(EbOrderDetail detail,
			EbComment comment, String imageHost) {
		HaveCommentOrderVO vo = new HaveCommentOrderVO();
		vo.setColor(detail.getColor());
		vo.setCommentTime(DateFormatter.date2String(comment.getCommentTime(),
				DATEFORMATER));
//		vo.setImageSrc(Util.getFullImageURL(detail.getImageSrc()));
		vo.setImageSrc(Util.getFullImageURLByVersion(detail.getImageSrc(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
		vo.setIsComment((detail.getCommentsId() == null || detail.getCommentsId() == 0) ? 0 : 1);
		vo.setOrderDetailId(detail.getOrderDetailId());
		vo.setOrderId(detail.getOrderId());
		vo.setProductName(detail.getProductName());
		vo.setSize(detail.getSize());
		vo.setCommentId(comment.getId());
		vo.setProductCode(detail.getProductCode());
		return vo;
	}

	/**
	 * <p>
	 * 功能描述:待评论订单列表
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult waitCommentOrderList() throws SqlException,
			JSONException {
		JSONObject result = new JSONObject();
		JSONObject body = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		String orderTime = null;
		int userId = getContext().getHead().getUid();
		if (!body.isNull("pageSize")) {
			pageSize = body.getInt("pageSize");
		}
		if (!body.isNull("page")) {
			page = body.getInt("page");
		}
		if (!body.isNull("orderTime")
				&& StringUtil.isNotNullNotEmpty(body.getString("orderTime"))) {
			orderTime = body.getString("orderTime");
		}
		OrderServiceV5_0 orderServ = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);

		List<EbOrder> orderList = orderServ.getWaitCommentOrder(userId,
				orderTime, page, pageSize);
		List<OrderVO> orderVOList = buildOrderVOList(orderList,false);
		Gson gson = new Gson();
		result.put("waitCommentOrders", gson.toJson(orderVOList));
		// TODO 待评论订单数
		if (page <= 0) {
			result.put("waitCommentCount",
					orderServ.getWaitCommentOrderCount(userId));
			EbProductCommentService ebProductCommentService = SystemInitialization
					.getApplicationContext().getBean(
							EbProductCommentService.class);
			result.put("haveCommentCount",
					ebProductCommentService.retrieveHaveCommentsCount(userId));
		}
		Util.addStatistics(getContext(), orderVOList);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取未评论订单成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:订单完成
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult deleteOrder() throws SqlException, JSONException {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		if (reqBody.isNull("orderId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"orderId不能为空", result, this);
		}
		long orderId = reqBody.getLong("orderId");
		EbOrder order = getOrderByOrderId(orderId);
		if (order == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "订单不存在",
					result, this);
		}
		order.setIsDelete(1);
		OrderServiceV5_0 orderServ = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		orderServ.updateOrder(order);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "订单删除成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:订单完成
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws Exception
	 */
	private ExecuteResult completeOrder() throws Exception {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		if (reqBody.isNull("orderId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"orderId不能为空", result, this);
		}
		long orderId = reqBody.getLong("orderId");
		EbOrder order = getOrderByOrderId(orderId);
		if (order == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "订单不存在",
					result, this);
		}
		if(order.getStatus() != EbOrderStatusEnum.SUCCESS){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "您购买的商品还未发货，不能确认收货",
					result, this);
		}
		EbOrderService orderServ = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		orderServ.processCompleteOrder(order,getContext());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "订单完成成功",
				result, this);
	}

	/**
	 * 入参：orderId
	 * <p>
	 * 功能描述:取消订单：修改订单状态为-2,返回用户使用的积分，返回用户使用的优惠券
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * @throws Exception 
	 */
	private ExecuteResult cancelOrder() throws Exception {
		JSONObject result = new JSONObject();
		int userId = getContext().getHead().getUid();
		JSONObject body = getContext().getBody().getBodyObject();
		if (body.isNull("orderId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"订单id不能为空", result, this);
		}
		Long orderId = body.getLong("orderId");
		OrderServiceV5_0 orderServ = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		EbOrder order = orderServ.getOrderByOrderId(orderId);
		if (order == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"未查到相应的订单", result, this);
		}
		if (order.getStatus() != EbOrderStatusEnum.ORDERSUCCESS) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"订单状态异常,无法取消订单,请刷新后再试", result, this);
		}
		if (order.getStatus() == EbOrderStatusEnum.CANCEL) {
			result.put("result", CommandList.RESPONSE_STATUS_OK);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "取消订单成功",
					result, this);
		}
		
		EbOrderService ebOrderServ = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		Map<String,Object> retMap = ebOrderServ.updateCancelOrder(order, userId,getContext());
		if(retMap.containsKey("success") && !(Boolean)retMap.get("success")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, retMap.get("msg").toString(),
					result, this);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "取消订单成功",
				result, this);
	}

	/**
	 * <p>
	 * 入参：orderId 订单id
	 * </p>
	 * <p>
	 * 功能描述:查看订单明细
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryOrderDetail() {
		JSONObject body = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			if (body.isNull("orderId")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"参数orderId不能为空", result, this);
			}

			Long orderId = body.getLong("orderId");
			// 根据orderId获取订单
			EbOrder order = getOrderByOrderId(orderId);
			if (order == null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"未能找到相应的订单", result, this);
			}
			// 构建订单VO
			OrderVO orderVO = buildOrderVO(order,true);
			// 设置订单收货地址
			orderVO.setAddress(buildUserAddress(order));
			// 设置订单明细VO
			orderVO.setOrderDetails(buildOrderDetailVO(order, orderVO));
			// 设置订单积分抵用金额
			orderVO.setCreditsPrice(getCreditPrice(order.getOrderDetails()));
			// 计算会员优惠金额，优惠券金额，活动优惠金额
			computeReducePrice(orderVO, order);

			Gson gson = new Gson();
			result.put("orderDetail", gson.toJson(orderVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取订单详情成功", result, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 
	 * <p>
	 * 功能描述:根据订单id获取订单
	 * </p>
	 * <p>
	 * 参数：@param orderId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：EbOrder
	 * </p>
	 */
	private EbOrder getOrderByOrderId(long orderId) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		EbOrder order = orderService.getOrderByOrderId(orderId);
		return order;
	}

	/**
	 * <p>
	 * 功能描述:计算会员优惠金额，优惠券金额，活动优惠金额
	 * </p>
	 * <p>
	 * 参数：@param orderVO
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private void computeReducePrice(OrderVO orderVO, EbOrder order)
			throws SqlException {
		Set<Integer> prodCodeSet = new HashSet<Integer>();
		Set<Integer> detailIdSet = new HashSet<Integer>();
		// 将productCode和orderDetailId放入prodCodeSet和detailIdSet中
		setProdCodeSetAndDetailIdSet(prodCodeSet, detailIdSet,
				order.getOrderDetails());
		// 计算vip减免和商品总额
		computeVipReduceAndTotalPrice(prodCodeSet, orderVO, order);
		// 计算优惠活动的减免金额
		computePromotionReduce(detailIdSet, orderVO);
		// 计算 优惠券优惠金额
		computeCouponReduce(orderVO, order);
		// 计算应付金额：商品总额 + 运费 － vip减免 －活动优惠
		orderVO.setDuePrice(orderVO.getTotalPrice() + orderVO.getShipping()
				- orderVO.getPromotionReduceFee() - orderVO.getVipReduceFee());
		// 由于有些历史数据没有totalPrice,所以这里判断一下。
		if (order.getTotalPrice() != null) {
			orderVO.setPayPrice(order.getTotalPrice());
		}
	}

	/**
	 * <p>
	 * 功能描述:计算优惠券减免金额
	 * </p>
	 * <p>
	 * 参数：@param orderVO
	 * <p>
	 * 参数：@param order
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void computeCouponReduce(OrderVO orderVO, EbOrder order)
			throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		EbOrderCouponRecord couponRec = orderService
				.getCouponRecordByOrderId(order.getOrderid());
		if (couponRec == null) {
			orderVO.setCouponReduceFee(0.0d);
			return;
		}
		double couponRed = couponRec.getCouponPrice();
		orderVO.setCouponReduceFee(couponRed);
		//把优惠券抵邮费的金额加到订单的邮费里
		orderVO.setShipping(orderVO.getShipping() + couponRec.getUseToShipping());
	}

	/**
	 * <p>
	 * 功能描述:计算优惠活动减免金额
	 * </p>
	 * <p>
	 * 参数：@param detailIdSet
	 * <p>
	 * 参数：@param orderVO
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private void computePromotionReduce(Set<Integer> detailIdSet,
			OrderVO orderVO) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		List<EbOrderPromotionRecord> promRecords = orderService
				.retrievePromotionRecordByIds(detailIdSet);
		double promReductPrice = 0d;
		if (promRecords == null || promRecords.size() <= 0) {
			orderVO.setPromotionReduceFee(promReductPrice);
			return;
		}
		for (EbOrderPromotionRecord ebOrderPromotionRecord : promRecords) {
			promReductPrice = promReductPrice
					+ ebOrderPromotionRecord.getPromotionPrice().doubleValue();
		}
		orderVO.setPromotionReduceFee(promReductPrice);
	}

	/**
	 * <p>
	 * 功能描述:计算vip优惠金额和商品总额(未减免的金额)
	 * </p>
	 * <p>
	 * 参数：@param prodCodeSet
	 * <p>
	 * 参数：@param orderVO
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：Map<Integer,EbProduct>
	 * </p>
	 */
	private void computeVipReduceAndTotalPrice(Set<Integer> prodCodeSet,
			OrderVO orderVO, EbOrder order) throws SqlException {
		if (prodCodeSet == null || prodCodeSet.size() <= 0) {
			return;
		}
		double totalPrice = 0d;
		double vipPrice = 0d;
		int userId = getContext().getHead().getUid();
		// 是否vip会员
		boolean isVip = isVipMember(userId);
		Map<Integer, Integer> amountMap = getOrderDetailAmountMap(order
				.getOrderDetails());
		EbProductService productSerivce = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> prodList = productSerivce
				.retrieveEbProductByCodes(prodCodeSet);
		for (EbProduct ebProduct : prodList) {
			if (!amountMap.containsKey(ebProduct.getProductCode())) {
				continue;
			}

			int amount = amountMap.get(ebProduct.getProductCode());
			totalPrice += ebProduct.getVprice() * amount;
			vipPrice += (isVip ? ebProduct.getSvprice() * amount : ebProduct
					.getVprice() * amount);
			amountMap.remove(ebProduct.getProductCode());
		}
		
		// 设置商品总额
		if (order.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
			if (order.getTotalPrice() != null) {
				orderVO.setTotalPrice(order.getTotalPrice());
			}
		} else {
			orderVO.setTotalPrice(totalPrice);
		}
		// 设置vip优惠金额
		orderVO.setVipReduceFee(totalPrice - vipPrice);
	}

	/**
	 * <p>
	 * 功能描述:获取订单明细商品购买数量
	 * </p>
	 * <p>
	 * 参数：@param orderDetails
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：int
	 * </p>
	 */
	private Map<Integer, Integer> getOrderDetailAmountMap(
			Set<EbOrderDetail> orderDetails) {
		Map<Integer, Integer> amountMap = new HashMap<Integer, Integer>();
		if (orderDetails == null || orderDetails.size() <= 0) {
			return amountMap;
		}
		for (EbOrderDetail ebOrderDetail : orderDetails) {
			if(ebOrderDetail.getIsGift() != null && ebOrderDetail.getIsGift()){
				continue;
			}
			int prodCode = ebOrderDetail.getProductCode();
			if (amountMap.containsKey(prodCode)) {
				amountMap.put(prodCode,
						amountMap.get(prodCode) + ebOrderDetail.getAmount());
			} else {
				amountMap.put(prodCode, ebOrderDetail.getAmount() == null ? 1
						: ebOrderDetail.getAmount());
			}
		}

		return amountMap;
	}

	/**
	 * <p>
	 * 功能描述:当前登录用户是否为vip
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isVipMember(int userId) throws SqlException {
		MemberServiceV31 memberServiceV31 = SystemInitialization
				.getApplicationContext().getBean(MemberServiceV31.class);
		boolean isMember = memberServiceV31.isMember(userId);
		return isMember;
	}

	/**
	 * <p>
	 * 功能描述:获取积分抵用
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：double
	 * </p>
	 */
	private double getCreditPrice(Set<EbOrderDetail> orderDetails) {
		double creditPrice = 0d;
		for (EbOrderDetail ebOrderDetail : orderDetails) {
			creditPrice += ebOrderDetail.getUseCredits();
		}
		return creditPrice / 100;
	}

	/**
	 * <p>
	 * 功能描述:构建订单收货地址
	 * </p>
	 * <p>
	 * 参数：@param order
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：UserAddressVO
	 * </p>
	 */
	private UserAddressVO buildUserAddress(EbOrder order) {
		UserAddressVO userAddressVO = new UserAddressVO();
		userAddressVO.setAddress(order.getAddress());
		userAddressVO.setAreaName(order.getAreaName());
		userAddressVO.setCellphone(order.getCellphone());
		userAddressVO.setCityName(order.getCityName());
		userAddressVO.setProvinceName(order.getProvinceName());
		userAddressVO.setUserName(order.getUserName());
		return userAddressVO;
	}

	/**
	 * <p>
	 * 接口入数:userId
	 * </p>
	 * <p>
	 * 功能描述:获取用户所有订单
	 * </p>
	 * <p>
	 * 参数：@param userId 用户ID
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryMyOrder(int userId) {
		JSONObject result = new JSONObject();
		JSONObject body = getContext().getBody().getBodyObject();
		try {
			int page = 0;
			int pageSize = -1;
			String orderTime = null;
			if (!body.isNull("pageSize")) {
				pageSize = body.getInt("pageSize");
			}
			if (!body.isNull("page")) {
				page = body.getInt("page");
			}
			if (!body.isNull("orderTime")
					&& StringUtil
							.isNotNullNotEmpty(body.getString("orderTime"))) {
				orderTime = body.getString("orderTime");
			}
			List<OrderVO> orderVO = getAllMyOrder(userId, page, pageSize,
					orderTime);
			Gson gson = new Gson();
			result.put("myOrders", gson.toJson(orderVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取我的订单成功", result, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:获取所有订单VO
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<OrderVO>
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private List<OrderVO> getAllMyOrder(int userId, int page, int pageSize,
			String orderTime) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		List<EbOrder> orders = orderService.getMyOrderByPage(userId, page,
				pageSize, orderTime);
		return buildOrderVOList(orders,false);
	}
	
	
	/**
	* <p>功能描述:获取某种状态订单的数量</p>
	* <p>参数：@param userId
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<OrderVO></p>
	 */
	private int getMyOrderQuantityByType(int userId,int type) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		return orderService.getMyOrderCountByType(userId, type);
	}
	
	/**
	* <p>功能描述:根据前端传入的类型获取不同状态的订单</p>
	* <p>参数：@param userId
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@param orderTime
	* <p>参数：@param orderId
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<OrderVO></p>
	 */
	private List<OrderVO> getMyOrderByType(int userId, int page, int pageSize,
			String orderTime,long orderId,int type) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		List<EbOrder> orders = orderService.getMyOrderByType(userId, page,
				pageSize, orderTime,orderId,type);
		return buildOrderVOList(orders,false);
	}
	
	/**
	 * <p>
	 * 功能描述:将订单名细下的产品Code和明细id分别加入到相应的Set集合中
	 * </p>
	 * <p>
	 * 参数：@param ProdCodeSet
	 * <p>
	 * 参数：@param detailIdSet
	 * <p>
	 * 参数：@param detail
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void setProdCodeSetAndDetailIdSet(Set<Integer> ProdCodeSet,
			Set<Integer> detailIdSet, Set<EbOrderDetail> detail) {
		if (detail == null || detail.size() <= 0) {
			return;
		}
		for (EbOrderDetail ebOrderDetail : detail) {
			ProdCodeSet.add(ebOrderDetail.getProductCode());
			detailIdSet.add(ebOrderDetail.getOrderDetailId());
		}
	}

	/**
	 * <p>
	 * 功能描述:构建订单OrderVO列表
	 * </p>
	 * <p>
	 * 参数：@param orders
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<OrderVO>
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private List<OrderVO> buildOrderVOList(List<EbOrder> orders,boolean isOrderDetail)
			throws SqlException {
		if (orders == null || orders.size() <= 0) {
			return null;
		}
		List<OrderVO> orderVOList = new ArrayList<OrderVO>();
		for (EbOrder order : orders) {
			// 构建OrderVO
			orderVOList.add(buildOrderVO(order,isOrderDetail));
		}
		return orderVOList;
	}

	/**
	 * <p>
	 * 功能描述:构建OrderVO
	 * </p>
	 * <p>
	 * 参数：@param order
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：OrderVO
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private OrderVO buildOrderVO(EbOrder order,boolean isOrderDetail) throws SqlException {
		OrderVO orderVO = new OrderVO();
		orderVO.setOrderDate(formatDate(order.getOrderTime(), DATEFORMATER));
		orderVO.setOrderId(order.getOrderid());
		orderVO.setOrderStatus(order.getStatus().getValue());
		//详情页和订单列表页标识：true为订单详情页，false为订单列表页
		if(isOrderDetail){
			orderVO.setStatusName(getOrderDetailStatus(order.getStatus().getValue(),order.getCancelType()== null? 0:order.getCancelType()));
		}else{
			orderVO.setStatusName(getOrderListStatus(order.getStatus().getValue()));
		}
		// 设置订单明细
		orderVO.setOrderDetails(buildOrderDetailVO(order, orderVO));
		if (order.getTotalPrice() != null) {
			orderVO.setPayPrice(order.getTotalPrice());
		}
		orderVO.setShipping(order.getShipping() == null ? 0d : order
				.getShipping());
		orderVO.setPayType((order.getPayType() == null || order.getPayType() == 0) ? 1
				: order.getPayType());
		orderVO.setOrderType(order.getOrderType() == null ? 1 : order
				.getOrderType().getValue());
		orderVO.setIsComment(getCommentFlag(order));
		// 快递公司
		orderVO.setExpressCompany(order.getExpressCompany() == null ? ""
				: order.getExpressCompany().getText());
		// 快递单号
		orderVO.setExpLogistics(order.getExpLogistics() == null ? "" : order
				.getExpLogistics());
		return orderVO;
	}
	
	/**
	* <p>功能描述:获取订单列表状态，由于订单状态的显示名称变化</p>
	* <p>参数：@param status
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String getOrderListStatus(int status){
		String statusName = "待付款";
		switch (status) {
		case 0:
			statusName = "待付款";
			break;
		case 1:
			statusName = "出库中";
			break;
		case 2:
			statusName = "待收货";
			break;
		case 3:
			statusName = "退款成功";
			break;
		case 4:
			statusName = "已完成";
			break;
		case 5:
			statusName = "已完成";
			break;
		case -2:
			statusName = "已取消";
			break;
		case -3:
			statusName = "缺货";
			break;
		case -7:
			statusName = "申请退款";
			break;
		case -8:
			statusName = "处理中";
			break;
		default:
			statusName = "待支付";
		}
		return statusName;
	}
	
	/**
	* <p>功能描述:用来获取详情页订单状态，由于订单详情页的订单状态显示与列表的状态显示名称不一样</p>
	* <p>参数：@param status
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private String getOrderDetailStatus(int status,int cancelType){
		String statusName = "待付款";
		switch (status) {
		case 0:
			statusName = "待付款";
			break;
		case 1:
			statusName = "支付成功（出库中）";
			break;
		case 2:
			statusName = "待收货";
			break;
		case 4:
			statusName = "已完成（待评论）";
			break;
		case 5:
			statusName = "已完成（已评论）";
			break;
		case -2:
			if (cancelType == 1) {
				statusName = "已取消（逾期未支付）";
			} else if (cancelType == 2) {
				statusName = "已取消（用户取消）";
			} else {
				statusName = "已取消";
			}
			break;
		case -3:
			statusName = "缺货";
			break;
		case -7:
			statusName = "申请退款";
			break;
		case -8:
			statusName = "处理中（确认付款中）";
			break;
		default:
			statusName = "待付款";
		}
		return statusName;
	}
	
	/**
	 * <p>
	 * 功能描述:获取订单是否已评价标识，0代表未评论，1代表已评论
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：Integer
	 * </p>
	 */
	private Integer getCommentFlag(EbOrder order) {
		Integer flag = 1;
		for (EbOrderDetail orderDetail : order.getOrderDetails()) {
			if (orderDetail.getCommentsId() == null
					|| orderDetail.getCommentsId() == 0) {
				flag = 0;
			}
		}
		return flag;
	}

	/**
	 * <p>
	 * 功能描述:将日期格式化为指定格式
	 * </p>
	 * <p>
	 * 参数：@param date 日期
	 * <p>
	 * 参数：@param formater
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：String
	 * </p>
	 */
	private String formatDate(Date date, String formater) {
		String retDate = "";
		if (date == null) {
			return retDate;
		}
		DateFormat sdf = new SimpleDateFormat(formater);
		return sdf.format(date);
	}

	/**
	 * <p>
	 * 功能描述:构建订单明细VO
	 * </p>
	 * <p>
	 * 参数：@param order
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<OrderDetailVO>
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private List<OrderDetailVO> buildOrderDetailVO(EbOrder order,
			OrderVO orderVO) throws SqlException {
		List<OrderDetailVO> orderDetailVOs = new ArrayList<OrderDetailVO>();
		Set<EbOrderDetail> orderDetails = order.getOrderDetails();
		if (orderDetails == null || orderDetails.size() <= 0) {
			return orderDetailVOs;
		}
		// String imageUrl = SystemManager.getInstance().getSystemConfig()
		// .getImgServerUrl();
		int productAmount = 0;
		// 订单表里有些历史数据没有totalPrice,所以这里重新计算一下价格
		double totalPrice = 0d;
		int comments = 0;
		for (EbOrderDetail ebOrderDetail : orderDetails) {
			OrderDetailVO detailVO = new OrderDetailVO();
			detailVO.setAmount(ebOrderDetail.getAmount());
			detailVO.setOrderDetailId(ebOrderDetail.getOrderDetailId());
			detailVO.setProductName(ebOrderDetail.getProductName());
			detailVO.setTotalPrice(ebOrderDetail.getPrice()
					* (ebOrderDetail.getAmount() == null ? 1 : ebOrderDetail
							.getAmount()));
			detailVO.setPrice(ebOrderDetail.getPrice());
			// 有些历史数据没有存图片路径，所以重新从产品里获取图片地址
			if (StringUtil.isNullOrEmpty(ebOrderDetail.getImageSrc())) {
				ProductServiceV5_0 prodServ = SystemInitialization
						.getApplicationContext().getBean(
								ProductServiceV5_0.class);
				EbProduct prod = prodServ.getProductByCode(ebOrderDetail
						.getProductCode());
//				detailVO.setImageSrc(Util.getFullImageURL(prod.getImgUrl()));
				detailVO.setImageSrc(Util.getFullImageURLByVersion(prod.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			} else {
//				detailVO.setImageSrc(Util.getFullImageURL(ebOrderDetail
//						.getImageSrc()));
				detailVO.setImageSrc(Util.getFullImageURLByVersion(ebOrderDetail
						.getImageSrc(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			}
			detailVO.setSize(ebOrderDetail.getSize());
			detailVO.setIsComment((ebOrderDetail.getCommentsId() == null || ebOrderDetail
					.getCommentsId() == 0) ? 0 : 1);
			comments += detailVO.getIsComment();
			detailVO.setProductCode(ebOrderDetail.getProductCode());
			detailVO.setGift(ebOrderDetail.getIsGift() == null ? false
					: ebOrderDetail.getIsGift().booleanValue());
			// 订单表里有些历史数据没有totalPrice,所以这里重新计算一下价格
			if(!detailVO.isGift()){
				totalPrice += detailVO.getTotalPrice();
			}
			//有些数据数量为空
			productAmount += ebOrderDetail.getAmount() == null?1:ebOrderDetail.getAmount();
			orderDetailVOs.add(detailVO);
		}
		// 由于历史数据有些totalPrice为空，这里重新计算一下。
		if (order.getTotalPrice() == null) {
			orderVO.setTotalPrice(totalPrice);
			orderVO.setPayPrice(totalPrice);
		}
		orderVO.setProductAmount(productAmount);
		if (order.getStatus() == EbOrderStatusEnum.COMPLETE
				&& comments == order.getOrderDetails().size()) {
			orderVO.setStatusName(EbOrderStatusEnum.COMMENT.getDescription());
			orderVO.setOrderStatus(EbOrderStatusEnum.COMMENT.getValue());
		}
		//将赠品放到最后面
		Collections.sort(orderDetailVOs, new Comparator<OrderDetailVO>() {
			public int compare(OrderDetailVO od1, OrderDetailVO od2) {
				int gift1 = od1.isGift() ? 1 : 0;
				int gift2 = od2.isGift() ? 1 : 0;
				return gift1 - gift2;
			}
		});
		return orderDetailVOs;
	}

	/**
	 * 进入结算页,查库存、返回可用积分、返回运费<br/>
	 * 若有库存不足的情况则将库存不足的商品返回给客户端<br/>
	 * 入参：List<ShoppingCartItem> cartItems,List<GiftItem> gifts
	 */
	private ExecuteResult confirm(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			// Gson gson = new Gson();
			List<ShoppingCartItem> cartItems = getShoppingCartItems(jsonObj
					.getJSONArray("cartItems"));
			List<ShoppingCartItem> oosCartItems = new ArrayList<ShoppingCartItem>();

			List<GiftItem> gifts = getGiftItems(jsonObj.getJSONArray("gifts"));
			List<GiftItem> oosGifts = new ArrayList<GiftItem>();

			Map<Integer, EbSku> skuMap = new HashMap<Integer, EbSku>();
			Map<Integer, EbProduct> productMap = new HashMap<Integer, EbProduct>();
			// Set<Integer> productCodeSet = new HashSet<Integer>();

			MemberServiceV31 memberServiceV31 = SystemInitialization
					.getApplicationContext().getBean(MemberServiceV31.class);
			boolean isMember = memberServiceV31.isMember(userId);

			EbSkuService ebSkuService = SystemInitialization
					.getApplicationContext().getBean(EbSkuService.class);
			EbProductService ebProductService = SystemInitialization
					.getApplicationContext().getBean(EbProductService.class);
			JSONObject obj = new JSONObject();
			// 所有商品的IKan总价
			double totalIkanPrice = 0;
			// 满减优惠的金额
			double totalPromotionReduce = 0;
			// VIP优惠的价格
			double totalVIPReduce = 0;
			
			List<ShoppingCartItemVO> l = new ArrayList<ShoppingCartItemVO>();
			
//			int size = jsonObj.getJSONArray("cartItems").length();
//			JSONArray sArray = jsonObj.getJSONArray("cartItems");
//			if(size>0){
//				for (int i = 0; i < size; i++) {
//					JSONObject jsonObject = sArray.getJSONObject(i);
//					ShoppingCartItemVO s = new ShoppingCartItemVO();
//					s.setAmount(jsonObject.optInt("amount",0));
//					s.setShoppingCartId(jsonObject.optInt("shoppingCartId",0));
//					s.setSkuCode(jsonObject.optInt("skuCode",0));
//					EbProduct ep = ebProductService
//							.retrieveEbproductBySkuCode(jsonObject.optInt("skuCode"));
//					if(isMember){
//						s.setPrice(ep.getSvprice());
//					}else{
//						s.setPrice(ep.getVprice());
//					}
//					jsonArray.put(new JSONObject(s));
//				}
//			}
			
			// 产品库存
			for (ShoppingCartItem foo : cartItems) {
				EbSku ebSku = ebSkuService.retrieveEbSkuBySkuCode(foo
						.getSkuCode());
				EbProduct ebProduct = ebProductService
						.retrieveEbproductBySkuCode(foo.getSkuCode());
				ShoppingCartItemVO s = new ShoppingCartItemVO();
				s.setId(foo.getId());
				s.setAmount(foo.getAmount());
				s.setPrice(ebProduct.getVprice());
				s.setProductCode(ebProduct.getProductCode());
//				s.setProductImage(Util.getFullImageURL(ebProduct.getImgUrl()));
				s.setProductImage(Util.getFullImageURLByVersion(ebProduct.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
				s.setProductName(ebProduct.getProductName());
				s.setVipPrice(ebProduct.getSvprice());
				s.setSkuCode(foo.getSkuCode());
				s.setSize(StringUtil.isNullOrEmpty(ebSku.getSize())? "默认" : ebSku.getSize());
				l.add(s);
				
				foo.setPrice(ebProduct.getVprice());
				foo.setMarketPrice(ebProduct.getPrice());
				foo.setVipPrice(ebProduct.getSvprice());
				foo.setProductCode(ebSku.getProductCode());
				foo.setProductName(ebSku.getProductName());
				
				// 累积商品IKAN价格
				totalIkanPrice += foo.getAmount() * ebProduct.getVprice();
				totalVIPReduce += (ebProduct.getVprice() - ebProduct
						.getSvprice()) * foo.getAmount();

				// productCodeSet.add(ebSku.getProductCode());
				if (!skuMap.containsKey(ebSku.getSkuCode())) {
					skuMap.put(ebSku.getSkuCode(), ebSku);
				}
				if (!productMap.containsKey(ebSku.getProductCode())) {
					productMap.put(ebSku.getProductCode(), ebProduct);
				}

				if (foo.getAmount() > ebSku.getStorage().getAvailable()
						.intValue()) {// 缺货
					oosCartItems.add(foo);
				}

			}
			// 赠品库存
			for (GiftItem foo : gifts) {
				EbSku ebSku = ebSkuService.retrieveEbSkuBySkuCode(foo
						.getSkuCode());
				foo.setProductCode(ebSku.getProductCode());
				foo.setProductName(ebSku.getProductName());
				if (foo.getAmount() > ebSku.getStorage().getAvailable()
						.intValue()) {// 缺货
					oosGifts.add(foo);
				}
			}
			// 积分的使用是受促销活动的影响的，
			// 但在getCanUseCredits的积分计算过程中使用的是ikanPrice或vipPrice，而不是优惠活动之后的价格
			// ，也就是说可使用的积分多计算了，而promotionReduceCredits保存多计算的部分
			int promotionReduceCredits = 0;
			if (oosCartItems.size() > 0 || oosGifts.size() > 0) {
				obj.put("result", false);
			} else {
				obj.put("result", true);
				EbPromotionService ebPromotionService = SystemInitialization
						.getApplicationContext().getBean(
								EbPromotionService.class);
				List<EbPromotion> ebPromotions = ebPromotionService
						.retrieveEbPromotionList();
				// 促销
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
								//
								double pTotalPrice = 0d;
								List<ShoppingCartItem> records = new ArrayList<ShoppingCartItem>();
								for (ShoppingCartItem cartItem : cartItems) {
									if (foo.getIsForAll()) {// 促销范围：全部商品
										pTotalPrice += (isMember ? cartItem
												.getVipPrice() : cartItem
												.getPrice())
												* cartItem.getAmount();
										records.add(cartItem);
									} else if (foo.getEbProductCollection() != null) {
										if (isInCollection(
												foo.getEbProductCollection(),
												cartItem.getProductCode())) {
											pTotalPrice += (isMember ? cartItem
													.getVipPrice() : cartItem
													.getPrice())
													* cartItem.getAmount();
											records.add(cartItem);
										}
									}
								}
								EbPromotionItem item = null;
								if (records.size() > 0) {
									for (EbPromotionItem pi : foo
											.getEbPromotionItems()) {
										if (pTotalPrice >= pi
												.getStandardPrice()) {
											if (item == null) {
												item = pi;
											} else {
												item = item.getStandardPrice() > pi
														.getStandardPrice() ? item
														: pi;
											}
										}
									}
								}
								if (item != null) {
									double fee = item.getReducePrice();
									totalPromotionReduce += fee;
									for (ShoppingCartItem cartItem : records) {
										double price = (isMember ? cartItem
												.getVipPrice() : cartItem
												.getPrice())
												* cartItem.getAmount();
										double subFee = price / pTotalPrice
												* fee;
										EbProduct ebProduct = productMap
												.get(cartItem.getProductCode());
										promotionReduceCredits += (int) Math
												.round(ebProduct
														.getCreditPercentage()
														* subFee);
									}
								}
							}
						}
					}
				}
				totalVIPReduce = isMember ? totalVIPReduce : 0;
				double payPrice = totalIkanPrice - totalVIPReduce
						- totalPromotionReduce;
				// 计算运费
//				EbOrderService ebOrderService = SystemInitialization
//						.getApplicationContext().getBean(EbOrderService.class);
//				double shippingThreshold = ebOrderService.isFirstOrder(userId) ? 39
//						: 68;
//				double shipping = payPrice >= shippingThreshold ? 0 : 8;
				//邮费将从配置里读取计算
				double shipping = Util.computeShipping(payPrice, userId);

				obj.put("totalIkanPrice", totalIkanPrice);
				obj.put("totalPromotionReduce", totalPromotionReduce);
				obj.put("totalVIPReduce", totalVIPReduce);
				obj.put("payPrice", payPrice + shipping);
				obj.put("shipping", shipping);
			}
			int canUseCredits = getCanUseCredits(skuMap, productMap, cartItems,
					userId, promotionReduceCredits, isMember);
			obj.put("useCredits", canUseCredits);
			obj.put("ebProducts",l);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "", obj,
					this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (SqlException e) {
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
	private int getCanUseCredits(Map<Integer, EbSku> skuMap,
			Map<Integer, EbProduct> prodMap, List<ShoppingCartItem> cartItems,
			int userId, int promotionReduceCredits, boolean isMember)
			throws SqlException {
		int useCredits = 0;
		for (ShoppingCartItem shoppingCartItem : cartItems) {
			EbSku sku = skuMap.get(shoppingCartItem.getSkuCode());
			int productCode = sku.getProductCode();
			EbProduct product = prodMap.get(productCode);
			int credit = (int) (product.getCreditPercentage() * (isMember ? product
					.getSvprice() * 2 : product.getVprice()));
			useCredits += credit * shoppingCartItem.getAmount();
		}
		if (isMember) {
			useCredits -= promotionReduceCredits * 2;
		} else {
			useCredits -= promotionReduceCredits;
		}
		return useCredits > 0 ? useCredits : 0;
	}

	/**
	 * @功能描述: 将EbProduct列表转换成Map,其key值为productCode,value值为EbProduct对象
	 * @return Map<Integer,EbSku>
	 * @author yusf
	 */
	// private Map<Integer, EbProduct> getEbProductHash(Set<Integer> productSet)
	// throws SqlException {
	// EbProductService ebProductService = SystemInitialization
	// .getApplicationContext().getBean(EbProductService.class);
	// List<EbProduct> productList = ebProductService
	// .retrieveEbProductByCodes(productSet);
	// Map<Integer, EbProduct> retMap = new HashMap<Integer, EbProduct>();
	// if (productList == null || productList.size() <= 0) {
	// return null;
	// }
	// for (int i = 0; i < productList.size(); i++) {
	// EbProduct product = productList.get(i);
	// if (product == null || product.getProductCode() == null) {
	// continue;
	// }
	// retMap.put(product.getProductCode(), product);
	// }
	// return retMap;
	// }

	private List<GiftItem> getGiftItems(JSONArray jsonArray)
			throws JSONException {
		List<GiftItem> giftItems = new ArrayList<GiftItem>();
		int size = jsonArray.length();
		for (int i = 0; i < size; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			GiftItem giftItem = new GiftItem();
			giftItem.setPromotionId(jsonObject.optInt("promotionId"));
			giftItem.setPromotionItemId(jsonObject.optInt("promotionItemId"));
			giftItem.setSkuCode(jsonObject.optInt("skuCode"));
			giftItems.add(giftItem);
		}
		return giftItems;
	}

	private List<ShoppingCartItem> getShoppingCartItems(JSONArray array)
			throws JSONException {
		List<ShoppingCartItem> cartItems = new ArrayList<ShoppingCartItem>();
		int size = array.length();
		for (int i = 0; i < size; i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			ShoppingCartItem cartItem = new ShoppingCartItem();
			cartItem.setAmount(jsonObject.optInt("amount", 1));
			cartItem.setId(jsonObject.optInt("id"));
			cartItem.setSkuCode(jsonObject.optInt("skuCode"));
			cartItems.add(cartItem);
		}
		return cartItems;
	}
	
	
	

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 下单<br/>
	 * 入参：OrderConfirm orderConfirm
	 * 
	 * @throws JSONException
	 * @throws JsonSyntaxException
	 * @throws SqlException
	 */
	private ExecuteResult createOrder(int userId) throws JsonSyntaxException,
			JSONException, SqlException {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		if(!jsonObj.has("orderConfirm")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "提交订单参数不能为空",
					null, this);
		}
		if(jsonObj.getJSONObject("orderConfirm").get("payType") instanceof JSONObject){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "支付类型不正确",
					null, this);
		}
//		if (MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.wapmobile
//				&& jsonObj.getJSONObject("orderConfirm").has("payType")) {
//			jsonObj.getJSONObject("orderConfirm").put("payType", "3");
//		}
		Gson gson = new Gson();
		OrderConfirm orderConfirm = gson.fromJson(jsonObj.get("orderConfirm")
				.toString(), OrderConfirm.class);
		EbOrder ebOrder = new EbOrder();
		ebOrder.setOrderid(OrderIdGenerationUtil.getInstance().genOrderId());
		bindBasicOrderInfo(orderConfirm, ebOrder, userId);
		bindAddress(ebOrder);
		// 下单成功后从购物车删除
		List<EbShoppingCart> shoppingcarts = new ArrayList<EbShoppingCart>();
		EbOrderService ebOrderService = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		EbCouponService ebCouponService = SystemInitialization
				.getApplicationContext().getBean(EbCouponService.class);
		try {
			boolean isMember = isVipMember(ebOrder.getUserId());
			bindDetails(orderConfirm, ebOrder, shoppingcarts, isMember);
			// 活动促销处理
			ebOrderService.promotionProcess(ebOrder, orderConfirm.getGifts());
			// TODO 注意： 促销活动会影响每件商品的成交总价格（促销活动会把优惠均摊到每件商品上），
			// 优惠券的使用也会影响每件商品的成交总价格（都会均摊到每件商品上）
			// 运费(促销活动之后的价格)，不受优惠券影响
//			double shippingThreshold = ebOrderService.isFirstOrder(userId) ? 39
//					: 68;
//			double shipping = ebOrder.getTotalPrice() >= shippingThreshold ? 0
//					: 8;
			//邮费将从配置里读取计算
			double shipping = Util.computeShipping(ebOrder.getTotalPrice(), userId);
			
			ebOrder.setShipping(shipping);

			// 优惠券使用
			EbCoupon ebCoupon = couponProcess(ebOrder,
					orderConfirm.getCouponId(), ebCouponService);
			// 积分使用
			creditsProcess(ebOrder, orderConfirm.getCredits(), isMember);

			// 优惠券使用记录
			EbOrderCouponRecord record = null;
			if (ebCoupon != null) {
				// 如果优惠券还有余额，并且余额还可用于运费的抵用
				double couponBalance = ebCoupon.getMoney().doubleValue()
						- ebCoupon.getCouponMoney();
				double useToShipping = 0d;
				if (ebOrder.getShipping() > 0 && couponBalance > 0
						&& ebCoupon.getForShipping() != null
						&& ebCoupon.getForShipping().booleanValue()) {
					useToShipping = ebOrder.getShipping().doubleValue() > couponBalance ? couponBalance
							: ebOrder.getShipping().doubleValue();
					ebCoupon.setCouponMoney(ebCoupon.getCouponMoney()
							+ useToShipping);
					ebOrder.setShipping(ebOrder.getShipping().doubleValue()
							- useToShipping);
				}
				record = new EbOrderCouponRecord();
				record.setCouponId(ebCoupon.getId());
				record.setCouponName(ebCoupon.getCouponName());
				record.setCouponPrice(ebCoupon.getCouponMoney());
				record.setDescription(ebCoupon.getDescription());
				record.setMinAmount(ebCoupon.getMinAmount());
				record.setMoney(ebCoupon.getMoney());
				record.setOrderId(ebOrder.getOrderid());
				record.setEbOrderDetails(ebCoupon.getEbOrderDetails());
				record.setUseToShipping(useToShipping);
			}
			// 订单总金额：产品总计+运费
			ebOrder.setTotalPrice(ebOrder.getTotalPrice()
					+ ebOrder.getShipping());
			// TODO 测试版金额为0.01
			ebOrder.setTotalPrice(0.01d);
			// 订单金额为0则默认支付成功
			if (ebOrder.getTotalPrice() == 0d) {
				ebOrder.setPayType(0);
				ebOrder.setStatus(EbOrderStatusEnum.PAYSUCCESS);
			}
			ebOrderService
					.createOrder(ebOrder, ebCoupon, shoppingcarts, record);
			//添加统计信息
			Util.addStatistics(getContext(), ebOrder);
		} catch (OOSExceprion e) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "商品无库存",
					e.getOOSResult(), this);
		}
		if (ebOrder.getTotalPrice() > 0d) {
			// TODO 1)根据不同的支付方式放回不同的参数，因为微信支付的参数是由服务器向微信后台获取生成的
			if (ebOrder.getPayType().intValue() == 3) {// 微信支付
				// TODO 待优化，当出现异常时，要再次请求，最多3次
				try {
					int isUseNewWXPay = Util.isUseNewWXpay(getContext()
							.getHead().getVersion(), getContext().getHead()
							.getPlatform());
					String prepayId = "";
					PayReqData payreqData = null;
					//移动端网站微信支付获取请求数据
					if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.wapmobile)) {
						String code = jsonObj.optString("code");
						String currentPageURL = jsonObj.optString("currentPageURL");
						prepayId = WXPay.getJSAPIPrepayid(
								String.valueOf(ebOrder.getOrderid()), "",
								String.valueOf(ebOrder.getOrderid()),
								(int) (Math.round(ebOrder.getTotalPrice() * 100)),
								"", "", ebOrder.getOrderSource(),code);
						payreqData = WXPay.getWapMobilePayReqData(prepayId,currentPageURL);
					}else {//手机APP微信支付获取请求数据
						prepayId = WXPay.getPrepayid(
								String.valueOf(ebOrder.getOrderid()), "",
								String.valueOf(ebOrder.getOrderid()),
								(int) (Math.round(ebOrder.getTotalPrice() * 100)),
								"", "", ebOrder.getOrderSource(),isUseNewWXPay);
						payreqData = WXPay.getPayReqData(prepayId,
								ebOrder.getOrderSource(),isUseNewWXPay);
					}
					
					result.put("payreqData", gson.toJson(payreqData));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// TODO 2)加一个支付日志,可优化，该方法里面又从数据库取了一次订单信息
			ebOrderService.addOrderPay(ebOrder.getOrderid(), userId);
		}
		result.put("payPrice", ebOrder.getTotalPrice());
		result.put("orderId", ebOrder.getOrderid());
		result.put("result", true);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "下单成功",
				result, this);
	}

	/**
	 * 初始化订单详情
	 * 
	 * @param orderConfirm
	 * @param ebOrder
	 * @param shoppingcarts
	 * @throws JSONException
	 * @throws OOSExceprion
	 */
	private void bindDetails(OrderConfirm orderConfirm, EbOrder ebOrder,
			List<EbShoppingCart> shoppingcarts, boolean isMember)
			throws JSONException, SqlException, OOSExceprion {
		if (orderConfirm.getCartItems() == null
				|| orderConfirm.getCartItems().size() == 0) {
			throw new JSONException("没有订单详情");
		}
		EbSkuService ebSkuService = SystemInitialization
				.getApplicationContext().getBean(EbSkuService.class);
		EbProductService prodcutService = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		EbShoppingCartService ebShoppingCartService = SystemInitialization
				.getApplicationContext().getBean(EbShoppingCartService.class);

		List<ShoppingCartItem> oosCartItems = new ArrayList<ShoppingCartItem>();

		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
		double totalPrice = 0;
		for (ShoppingCartItem foo : orderConfirm.getCartItems()) {
			EbOrderDetail detail = new EbOrderDetail();
			detail.setAmount(foo.getAmount());
			detail.setSkuCode(foo.getSkuCode());
			EbSku ebSku = ebSkuService.retrieveEbSkuBySkuCode(detail
					.getSkuCode());
			if (ebSku == null
					|| ebSku.getStatus() != EbProductValidStatusEnum.VALID) {
				throw new JSONException("提交订单中的SKU信息不正确");
			}
			if (detail.getAmount() <= 0 || detail.getAmount() > 10) {
				throw new JSONException("提交订单中商品的数量不正确，每件商品限购10件，最少1件");
			}
			int available = ebSku.getStorage().getAvailable();
			foo.setStorageNum(available);
			if (available < detail.getAmount()) {
				foo.setOosNum(detail.getAmount()
						- ebSku.getStorage().getAvailable());
				oosCartItems.add(foo);
			}
			EbProduct ebProduct = prodcutService.retrieveEbProductById(ebSku
					.getProductCode());
			detail.setOrderId(ebOrder.getOrderid());
			detail.setProductCode(ebSku.getProductCode());
			detail.setVendorProductCode(ebProduct.getVendorProductCode());
			detail.setProductName(ebProduct.getProductName());
			detail.setSkuCode(ebSku.getSkuCode());
			detail.setSize(ebSku.getSize());
			detail.setColor(ebSku.getColor());
			detail.setImageSrc(ebProduct.getImgUrl());
			detail.setParent(ebOrder);
			double price = isMember ? ebProduct.getSvprice() : ebProduct
					.getVprice();
			// 注意： 这里不计算积分，只是保留每件商品折扣的百分比，在促销活动处理完后再处理积分
			detail.setUseCredits(ebProduct.getCreditPercentage());

			detail.setPrice(price);
			detail.setTotalPrice(detail.getAmount() * price);
			totalPrice += detail.getTotalPrice();

			details.add(detail);

			if (foo.getId() != 0 && shoppingcarts != null) {
				EbShoppingCart ebShoppingCart = ebShoppingCartService
						.retrieveShoppingCart(foo.getId());
				if (ebShoppingCart != null) {
					shoppingcarts.add(ebShoppingCart);
				}
			}
		}
		ebOrder.setOrderDetails(details);
		ebOrder.setTotalPrice(totalPrice);

		if (oosCartItems.size() > 0) {
			OOSExceprion exceprion = new OOSExceprion();
			exceprion.setMessage("所选商品超出库存");
			exceprion.setCartItems(oosCartItems);
			throw exceprion;
		}

	}

	/**
	 * @description 立即购买，类似购物车列表处理，返回相同的数据
	 * @return
	 */
	private ExecuteResult buyNow() {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int userId = getContext().getHead().getUid();
		try {
			ShoppingCartItem cartItem = new ShoppingCartItem();
			int amount = jsonObj.optInt("amount", 1);
			cartItem.setAmount(amount == 0 ? 1 : amount);
			cartItem.setSkuCode(jsonObj.optInt("skuCode"));
			
			EbSkuService ebSkuService = SystemInitialization
					.getApplicationContext().getBean(EbSkuService.class);
			EbSku ebSku = ebSkuService.retrieveEbSkuBySkuCode(cartItem
					.getSkuCode());
			if (ebSku == null) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"商品信息错误！", null, this);
			}
			if (cartItem.getAmount() > 10) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"每款商品最多选10件！", null, this);
			}
			int availableCount = ebSku.getStorage().getAvailable().intValue();
			if (cartItem.getAmount() > availableCount) {// 缺货
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"缺货，所选商品库存只有" + availableCount + "件！", null, this);
			}
			cartItem.setUserId(userId);
			cartItem.setChecked(true);
			cartItem.setSize(ebSku.getSize());
			cartItem.setColor(ebSku.getColor());
			cartItem.setProductCode(ebSku.getProductCode());
			EbProductService prodcutService = SystemInitialization
					.getApplicationContext().getBean(EbProductService.class);
			// String imageUrl = SystemManager.getInstance().getSystemConfig()
			// .getImgServerUrl();
			EbProduct ebProduct = prodcutService.retrieveEbProductById(ebSku
					.getProductCode());
//			cartItem.setProductImage(Util.getFullImageURL(ebProduct.getImgUrl()));
			cartItem.setProductImage(Util.getFullImageURLByVersion(ebProduct.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			cartItem.setProductName(ebProduct.getProductName());
			cartItem.setMarketPrice(ebProduct.getPrice());
			cartItem.setPrice(ebProduct.getVprice());
			cartItem.setVipPrice(ebProduct.getSvprice());
			cartItem.setStorageNum(availableCount);
			cartItem.setOosNum(cartItem.getAmount() - availableCount);
			MemberServiceV31 memberServiceV31 = SystemInitialization
					.getApplicationContext().getBean(MemberServiceV31.class);
			boolean isMember = memberServiceV31.isMember(userId);
			
			
			ShoppingCartItemVO s = new ShoppingCartItemVO();
			s.setAmount(amount);
			s.setPrice(ebProduct.getVprice());
			s.setProductCode(ebProduct.getProductCode());
//			s.setProductImage(Util.getFullImageURL(ebProduct.getImgUrl()));
			s.setProductImage(Util.getFullImageURLByVersion(ebProduct.getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			s.setProductName(ebProduct.getProductName());
			s.setVipPrice(ebProduct.getSvprice());
			s.setSkuCode(cartItem.getSkuCode());
			s.setSize(StringUtil.isNullOrEmpty(ebSku.getSize()) ? "默认" : ebSku.getSize());
//			ShoppingCartItemVO s = new ShoppingCartItemVO();
//			s.setAmount(amount);
//			s.setSkuCode(cartItem.getSkuCode());
//			if(isMember){
//				s.setPrice(ebProduct.getSvprice());
//			}else{
//				s.setPrice(ebProduct.getVprice());
//			}
//			sArray.put(new JSONObject(s));
			
			
			// 所有商品（ikan单价*数量）之和
			double totalIkanPrice = cartItem.getPrice() * cartItem.getAmount();
			// 所有符合条件的满减促销减掉的金额之和
			double totalPromotionReduceFee = 0;
			// (（ikan单价-VIP单价）*数量)之和
			double totalVIPReduceFee = isMember ? (DoubleUtil.mul(DoubleUtil.sub(
					cartItem.getPrice(), cartItem.getVipPrice()),  cartItem.getAmount())): 0;
			// 总计
			double totalPrice = totalIkanPrice - totalVIPReduceFee;
			GiftItem giftItem = null;
			int promotionReduceCredits = 0;
			try {

				EbPromotionService ebPromotionService = SystemInitialization
						.getApplicationContext().getBean(
								EbPromotionService.class);
				List<EbPromotion> ebPromotions = ebPromotionService
						.retrieveEbPromotionList();
				// 促销处理
				EbPromotion ebPromotion = null;
				if (ebPromotions != null && ebPromotions.size() > 0) {
					for (EbPromotion foo : ebPromotions) {
						if (foo.getIsForAll()) {
							// 全场促销
							ebPromotion = foo;
							break;
						} else {
							EbProductCollection collection = foo
									.getEbProductCollection();
							if (isInCollection(collection,
									cartItem.getProductCode())) {
								ebPromotion = foo;
								break;
							}
						}
					}
				}
				if (ebPromotion != null) {
					if (ebPromotion.getPromotionType() == EbPromotionTypeEnum.REDUCE
							&& totalPrice > 0) {
						// 立即购买的商品属于满减促销活动
						EbPromotionItem record = null;
						for (EbPromotionItem foo : ebPromotion
								.getEbPromotionItems()) {
							if (totalPrice >= foo.getStandardPrice()) {
								if (record == null) {
									record = foo;
								} else {
									record = record.getStandardPrice() > foo
											.getStandardPrice() ? record : foo;
								}
							}
						}
						if (record != null) {
							totalPromotionReduceFee = record.getReducePrice();
						}
					} else if (ebPromotion.getPromotionType() == EbPromotionTypeEnum.GIFT
							&& totalPrice > 0) {
						// 立即购买的商品属于满增促销活动
						EbPromotionItem record = null;
						for (EbPromotionItem foo : ebPromotion
								.getEbPromotionItems()) {
							if (totalPrice >= foo.getStandardPrice()) {
								if (record == null) {
									record = foo;
								} else {
									record = record.getStandardPrice() > foo
											.getStandardPrice() ? record : foo;
								}
							}
						}
						if (record != null) {
							if (!StringUtils.isEmpty(record.getGifts())) {
								Set<Integer> skuCodeSet = new HashSet<Integer>();
								String[] skuCodeArr = record.getGifts().split(
										",");
								for (int i = 0; i < skuCodeArr.length; i++) {
									int code = Integer.parseInt(skuCodeArr[i]);
									skuCodeSet.add(code);
								}
								List<EbSku> ebSkus = ebSkuService
										.retrieveEbSkuBySkuCodes(skuCodeSet);
								Map<Integer, EbProduct> prodMap = new HashMap<Integer, EbProduct>();
								try {
									prodMap = getEbProductHash(ebSkus);
								} catch (Exception e) {
									e.printStackTrace();
								}
								for (EbSku foo : ebSkus) {
									if (foo != null
											&& foo.getStorage() != null
											&& foo.getStorage().getAvailable() > 0) {
										giftItem = new GiftItem(
												ebPromotion.getPromotionId(),
												record.getPromotionItemId(),
												foo.getProductCode(),
												foo.getProductName(),
												foo.getSkuCode(),
												foo.getColor(),
												foo.getSize(),
												1,
//												Util.getFullImageURL(prodMap.get(foo.getProductCode()).getImgUrl()),
												Util.getFullImageURLByVersion(prodMap.get(foo.getProductCode()).getImgUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()),
												foo.getStorage().getAvailable().intValue());
										break;
									}
								}
							}
						}
					}
				}
			} catch (SqlException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			JSONObject obj = new JSONObject();
			// 应付金额
			double payPrice = totalIkanPrice - totalVIPReduceFee
					- totalPromotionReduceFee;
//			// 计算运费
//			EbOrderService ebOrderService = SystemInitialization
//					.getApplicationContext().getBean(EbOrderService.class);
//			double shippingThreshold = ebOrderService.isFirstOrder(userId) ? 39
//					: 68;
//			double shipping = payPrice >= shippingThreshold ? 0 : 8;
			
			//邮费将从配置里读取计算
			double shipping = Util.computeShipping(payPrice, userId);

			if (giftItem != null) {
				Gson gson = new Gson();
				obj.put("giftItem", gson.toJson(giftItem));
			}
			// 商品总金额
			obj.put("totalIkanPrice", totalIkanPrice);
			// VIP会员优惠：totalVIPReduce
			obj.put("totalVIPReduce", totalVIPReduceFee);
			// 活动优惠:totalPromotionReduce
			obj.put("totalPromotionReduce", totalPromotionReduceFee);
			// 运费:shipping
			obj.put("shipping", shipping);
			// 应付金额：payPrice
			obj.put("payPrice", payPrice + shipping);
			// 计算可使用的积分,VIP双倍积分
			int credit = (int) (ebProduct.getCreditPercentage() * (isMember ? ebProduct
					.getSvprice() * 2 : ebProduct.getVprice()))
					* cartItem.getAmount();
			// 去除活动优惠分部分
			int canUseCredits = (int) (credit - totalPromotionReduceFee
					* (isMember ? 2 : 1) * ebProduct.getCreditPercentage());

			obj.put("useCredits", (canUseCredits > 0 ? canUseCredits : 0));
			obj.put("ebProduct", new JSONObject(s));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "", obj,
					this);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * @功能描述: 将EbProduct列表转换成Map,其key值为productCode,value值为EbProduct对象
	 * @return Map<Integer,EbSku>
	 * @author yusf
	 */
	private Map<Integer, EbProduct> getEbProductHash(List<EbSku> skus)
			throws Exception {
		if (skus == null || skus.size() <= 0) {
			return null;
		}
		Set<Integer> prodCodeSet = new HashSet<Integer>();
		for (EbSku ebSku : skus) {
			prodCodeSet.add(ebSku.getProductCode());
		}
		EbProductService ebProductService = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> productList = ebProductService
				.retrieveEbProductByCodes(prodCodeSet);
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

	/**
	 * 初始化订单信息
	 * 
	 * @param orderConfirm
	 * @param ebOrder
	 * @param userId
	 * @throws JSONException
	 */
	protected void bindBasicOrderInfo(OrderConfirm orderConfirm,
			EbOrder ebOrder, int userId) throws JSONException {
		String deviceCode = getContext().getHead().uniqueId;
		ebOrder.setPayType(orderConfirm.getPayType());
		ebOrder.setUserId(userId);
		ebOrder.setStatus(EbOrderStatusEnum.ORDERSUCCESS);
		ebOrder.setDeviceCode(deviceCode);
		ebOrder.setOrderTime(new Date());
		ebOrder.setIsNeedInvoice(orderConfirm.isNeedInvoice() ? 1 : 0);
		ebOrder.setInvoiceTitle(orderConfirm.getInvoiceTitle());
		ebOrder.setBook(orderConfirm.getBook());
		MobileTypeEnum plat = MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform());
		ebOrder.setOrderPlat(plat);
		ebOrder.setOrderType(EbOrderTypeEnum.NORMAL);
		ebOrder.setOrderTime(new Date());
		ebOrder.setAddressId(orderConfirm.getAddressId());
		ebOrder.setIsDelete(0);
		//添加版本号，用于微信支付合并。由于以前版本还用以前的微信支付APPID等信息。所以定时取消订单时根据版本号区分
		ebOrder.setTerminalVersion(getContext().getHead().getVersion());
	}

	/**
	 * 初始化收货地址
	 * 
	 * @param ebOrder
	 * @throws SqlException
	 */
	private void bindAddress(EbOrder ebOrder) throws SqlException {
		EbUserAddressService euas = SystemInitialization
				.getApplicationContext().getBean(EbUserAddressService.class);
		EbUserAddress ebUserAddress = euas.retrieveEbUserAddressById(ebOrder
				.getAddressId());
		ebOrder.setAddressId(ebOrder.getAddressId());
		ebOrder.setAddress(ebUserAddress.getAddress());
		ebOrder.setAreaId(ebUserAddress.getAreaId());
		ebOrder.setAreaName(ebUserAddress.getAreaName());
		ebOrder.setCellphone(ebUserAddress.getCellphone());
		ebOrder.setCityId(ebUserAddress.getCityId());
		ebOrder.setCityName(ebUserAddress.getCityName());
		ebOrder.setEmail(ebUserAddress.getPostalCode());// 暂时把email当做ZIP用
		ebOrder.setProvinceName(ebUserAddress.getProvinceName());
		ebOrder.setUserName(ebUserAddress.getUserName());

	}

	/**
	 * 优惠券使用
	 * 
	 * @param ebOrder
	 * @param couponId
	 * @return
	 * @throws SqlException
	 * @throws JSONException
	 */
	private EbCoupon couponProcess(EbOrder ebOrder, int couponId,
			EbCouponService ebCouponService) throws SqlException, JSONException {
		if (couponId > 0) {
			EbCoupon ebCoupon = ebCouponService.retrieveEbCoupon(ebOrder
					.getUserId().intValue(), couponId);
			Date now = new Date();
			if (ebCoupon == null) {
				throw new JSONException("所选优惠券不存在");
			}
			if (ebCoupon.getUsed()) {
				throw new JSONException("所选优惠券已使用过");
			}
			if (now.before(ebCoupon.getStartTime())
					|| now.after(ebCoupon.getEndTime())) {
				throw new JSONException("优惠券不在使用期内");
			}
			// 对所选商品筛选，去掉不可使用优惠券的商品
			Set<EbOrderDetail> products = orderProductCellectionProcess(ebOrder
					.getOrderDetails());
			if (products == null || products.size() == 0) {
				return null;
			}

			double orderTotalPrice = 0;
			for (EbOrderDetail d : products) {
				orderTotalPrice += d.getTotalPrice().doubleValue();
			}

			if (ebCoupon.getEbProductCollection() == null) {// 全部商品都适用该优惠券
				if (orderTotalPrice >= ebCoupon.getMinAmount()) {// 符合使用规则
					ebCoupon.setUsed(true);
					ebCoupon.setEbOrder(ebOrder);
					ebCoupon.setUseTime(new Date());

					double money = ebCoupon.getMoney().doubleValue();
					money = money > orderTotalPrice ? orderTotalPrice
							: ebCoupon.getMoney().doubleValue();
					
					//订单总价格为0时不均摊
					if(orderTotalPrice > 0){
						double  total = 0d;
						EbOrderDetail[] orderDetailList = products.toArray(new EbOrderDetail[0]);
						// 先均摊,按订单详情金额与优惠的金额的比例，计算优惠金额
						for (int i = 0; i < orderDetailList.length; i++) {
							EbOrderDetail ebOrderDetail = orderDetailList[i];
							double subCouponPrice = (ebOrderDetail.getTotalPrice()
									.doubleValue() / orderTotalPrice) * money;
							double price = ebOrderDetail.getTotalPrice() - subCouponPrice;
							if (price < 0) {
								price = 0;
							}
							//处理尾差
							if(i == orderDetailList.length - 1){
								ebOrderDetail.setTotalPrice(DoubleUtil.sub(DoubleUtil.sub(orderTotalPrice, money),total));
							}else{
								ebOrderDetail.setTotalPrice(DoubleUtil.round(price, 2));
							}
							total = DoubleUtil.add(ebOrderDetail.getTotalPrice(), total);
						}
//						for (EbOrderDetail ebOrderDetail : products) {
//							double subCouponPrice = (ebOrderDetail.getTotalPrice()
//									.doubleValue() / orderTotalPrice) * money;
//							subCouponPrice = Util.roundDouble(subCouponPrice, 2);
//							double price = ebOrderDetail.getTotalPrice()
//									.doubleValue() - subCouponPrice;
//							if (price < 0) {
//								price = 0;
//							}
//							ebOrderDetail.setTotalPrice(price);
//							total += subCouponPrice;
//						}
					}
					// 后扣减总金额.
					ebOrder.setTotalPrice(DoubleUtil.sub(ebOrder.getTotalPrice().doubleValue(), money));
					orderTotalPrice -= money;
					// 记录优惠券使用
					ebCoupon.setCouponMoney(money);
					ebCoupon.setEbOrderDetails(products);
					return ebCoupon;
				}
			} else {// 限定部分商品、品牌、分类
				EbProductCollection ebProductCollection = ebCoupon
						.getEbProductCollection();
				// productTotalPrice
				double totalPrice = 0d;
				Set<EbOrderDetail> recordList = new HashSet<EbOrderDetail>();
				for (EbOrderDetail ebOrderDetail : products) {
					if (isInCollection(ebProductCollection,
							ebOrderDetail.getProductCode())) {
						totalPrice += ebOrderDetail.getTotalPrice();
						recordList.add(ebOrderDetail);
					}
				}
				if (totalPrice >= ebCoupon.getMinAmount()
						&& ebCoupon.getEndTime().after(new Date())
						&& ebCoupon.getStartTime().before(new Date())) {// 适用
					ebCoupon.setUsed(true);
					ebCoupon.setEbOrder(ebOrder);
					ebCoupon.setUseTime(new Date());
					double money = ebCoupon.getMoney().doubleValue() > totalPrice ? totalPrice
							: ebCoupon.getMoney().doubleValue();
					// 先均摊
					for (EbOrderDetail ebOrderDetail : recordList) {
						double subCouponPrice = (ebOrderDetail.getTotalPrice()
								.doubleValue() / totalPrice) * money;
						double price = ebOrderDetail.getTotalPrice()
								.doubleValue() - subCouponPrice;
						if (price < 0) {
							price = 0;
						}
						ebOrderDetail.setTotalPrice(price);
					}
					// 后扣减总金额.
					ebOrder.setTotalPrice(ebOrder.getTotalPrice().doubleValue()
							- money);
					orderTotalPrice -= money;
					// 记录优惠券使用
					ebCoupon.setCouponMoney(money);
					ebCoupon.setEbOrderDetails(recordList);
					return ebCoupon;
				}
			}
		}
		return null;
	}

	private Set<EbOrderDetail> orderProductCellectionProcess(
			Set<EbOrderDetail> orderDetails) throws SqlException {
		EbProductCellectionService ebProductCollectionService = SystemInitialization
				.getApplicationContext().getBean(
						EbProductCellectionService.class);
		List<EbProductCollection> collections = ebProductCollectionService
				.retrieveNonCouponCollection();
		if (collections == null || collections.size() == 0) {
			return orderDetails;
		}
		Set<EbOrderDetail> products = new HashSet<EbOrderDetail>();
		for (EbProductCollection c : collections) {
			for (EbOrderDetail foo : orderDetails) {
				if (!isInCollection(c, foo.getProductCode())) {
					products.add(foo);
				}
			}
		}
		return products;
	}

	/**
	 * @param ebProductCollection
	 * @param productCode
	 * @return
	 */
	private boolean isInCollection(EbProductCollection ebProductCollection,
			int productCode) {
		EbProductService ebProductService = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		EbProduct ebProduct = null;
		try {
			ebProduct = ebProductService.retrieveEbProductById(productCode);
		} catch (SqlException e) {
			e.printStackTrace();
		}
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
	 * 积分抵用按实际的价格抵用，即刨除促销活动、VIP优惠后的价格
	 * 
	 * @param ebOrder
	 * @param useCredits
	 *            用户使用的积分
	 * @param isMemeber
	 *            用户会员状态
	 * @throws SqlException
	 */
	private void creditsProcess(EbOrder ebOrder, int useCredits,
			boolean isMember) throws SqlException {
		// 可抵用的总积分
		int totalCredits = 0;
		for (EbOrderDetail d : ebOrder.getOrderDetails()) {
			// VIP双倍积分抵扣，注意：再bindDetails()处理的时候d.getUseCredits()保存的是每件商品的积分抵扣百分比
			int credits = (int) Math.round(d.getUseCredits()
					* d.getTotalPrice() * (isMember ? 2 : 1));
			d.setUseCredits(credits);
			totalCredits += d.getUseCredits();
		}
		CreditService cs = SystemInitialization.getApplicationContext()
				.getBean(CreditService.class);
		// 用户的总积分
		int userCredits = cs.getCreditByUser(ebOrder.getUserId());
		if (useCredits != 0 && ebOrder.getTotalPrice() > 0 && totalCredits != 0
				&& totalCredits >= useCredits && userCredits >= useCredits) {
			double totalPrice = 0d;
			EbOrderDetail maxPrice = null;
			//前端输入的使用积分
			for (EbOrderDetail d : ebOrder.getOrderDetails()) {
				// 占用的积分
				double uc = Math.round(d.getUseCredits() * useCredits * 1.0 / totalCredits);
				// 积分抵用的金额
				double creditsReduce = uc / 100.0;
				if (creditsReduce > d.getTotalPrice()) {
					creditsReduce = d.getTotalPrice();
				}
				double detailTotalPrice = d.getTotalPrice() > creditsReduce ? d
						.getTotalPrice() - creditsReduce : 0;
				d.setTotalPrice(detailTotalPrice);
				if (maxPrice == null) {
					maxPrice = d;
				} else {
					if (maxPrice.getTotalPrice() < detailTotalPrice) {
						maxPrice = d;
					}
				}
				// 防止多扣积分和少扣积分
				int c = (int) Math.round(uc);
				if (c > useCredits) {
					c = useCredits;
					useCredits = 0;
				} else {
					useCredits -= c;
				}

				d.setUseCredits(c);
				totalPrice += detailTotalPrice;
			}
			if (useCredits > 0 && maxPrice != null) {
				double creditsReduce = useCredits * 1.0 / 100.0;
				if (totalPrice >= creditsReduce) {
					maxPrice.setUseCredits(maxPrice.getUseCredits()
							+ useCredits);
					maxPrice.setTotalPrice(maxPrice.getTotalPrice() - creditsReduce);
					totalPrice -= creditsReduce;
				}
			}
			ebOrder.setTotalPrice(totalPrice);
		} else {
			for (EbOrderDetail d : ebOrder.getOrderDetails()) {
				d.setUseCredits(0);
			}
		}
	}

	/**
	 * 订单VO
	 */
	class OrderVO {
		// 订单id
		private Long orderId;
		// 订单日期
		private String orderDate;
		// 实付金额
		private double payPrice;
		// 订单状态
		private Integer orderStatus;
		// 订单状态中文名称
		private String statusName;
		// 订单明细
		private List<OrderDetailVO> orderDetails;
		// vip减免金额
		private double vipReduceFee;
		// 优惠券减免金额
		private double couponReduceFee;
		// 活动减免金额
		private double promotionReduceFee;
		// 运费
		private double shipping;
		// 积分抵用金额
		private double creditsPrice;
		// 应付金额：总金额－活动优惠－vip优惠+运费
		private double duePrice;
		// 用户订单收货地址
		private UserAddressVO address;
		// 支付方式
		private Integer payType;
		// 商品总额，未减名的金额
		private double totalPrice;
		// 订单明细中商品总数量
		private Integer productAmount;
		// 0未评论 1已评论
		private Integer isComment = null;
		// 订单类型
		private Integer orderType;
		// 快递公司
		private String expressCompany = "";
		// 快递单号
		private String expLogistics = "";

		public String getExpressCompany() {
			return expressCompany;
		}

		public void setExpressCompany(String expressCompany) {
			this.expressCompany = expressCompany;
		}

		public String getExpLogistics() {
			return expLogistics;
		}

		public void setExpLogistics(String expLogistics) {
			this.expLogistics = expLogistics;
		}

		public Integer getOrderType() {
			return orderType;
		}

		public void setOrderType(Integer orderType) {
			this.orderType = orderType;
		}

		public Integer getIsComment() {
			return isComment;
		}

		public void setIsComment(Integer isComment) {
			this.isComment = isComment;
		}

		public Integer getPayType() {
			return payType;
		}

		public void setPayType(Integer payType) {
			this.payType = payType;
		}

		public Integer getProductAmount() {
			return productAmount;
		}

		public void setProductAmount(Integer productAmount) {
			this.productAmount = productAmount;
		}

		public double getTotalPrice() {
			return totalPrice;
		}

		public void setTotalPrice(double totalPrice) {
			this.totalPrice = totalPrice;
		}

		public double getVipReduceFee() {
			return vipReduceFee;
		}

		public void setVipReduceFee(double vipReduceFee) {
			this.vipReduceFee = vipReduceFee;
		}

		public double getCouponReduceFee() {
			return couponReduceFee;
		}

		public void setCouponReduceFee(double couponReduceFee) {
			this.couponReduceFee = couponReduceFee;
		}

		public double getPromotionReduceFee() {
			return promotionReduceFee;
		}

		public void setPromotionReduceFee(double promotionReduceFee) {
			this.promotionReduceFee = promotionReduceFee;
		}

		public double getShipping() {
			return shipping;
		}

		public void setShipping(double shipping) {
			this.shipping = shipping;
		}

		public double getCreditsPrice() {
			return creditsPrice;
		}

		public void setCreditsPrice(double creditsPrice) {
			this.creditsPrice = creditsPrice;
		}

		public double getDuePrice() {
			return duePrice;
		}

		public void setDuePrice(double duePrice) {
			this.duePrice = duePrice;
		}

		public UserAddressVO getAddress() {
			return address;
		}

		public void setAddress(UserAddressVO address) {
			this.address = address;
		}

		public Long getOrderId() {
			return orderId;
		}

		public void setOrderId(Long orderId) {
			this.orderId = orderId;
		}

		public String getOrderDate() {
			return orderDate;
		}

		public void setOrderDate(String orderDate) {
			this.orderDate = orderDate;
		}

		public double getPayPrice() {
			return payPrice;
		}

		public void setPayPrice(double payPrice) {
			this.payPrice = payPrice;
		}

		public Integer getOrderStatus() {
			return orderStatus;
		}

		public void setOrderStatus(Integer orderStatus) {
			this.orderStatus = orderStatus;
		}

		public String getStatusName() {
			return statusName;
		}

		public void setStatusName(String statusName) {
			this.statusName = statusName;
		}

		public List<OrderDetailVO> getOrderDetails() {
			return orderDetails;
		}

		public void setOrderDetails(List<OrderDetailVO> orderDetails) {
			this.orderDetails = orderDetails;
		}

	}

	/**
	 * 订单详情VO
	 */
	class OrderDetailVO {
		// 订单明细id
		private Integer orderDetailId;
		// 商品code
		private Integer productCode;
		// 产品名称
		private String productName;
		// 产品图片
		private String imageSrc;
		// 购买数量
		private Integer amount;
		// 价格
		private double price;
		// 款式
		private String size;
		// 颜色
		private String color;
		// 订单明细总金额
		private double totalPrice;
		// 是否评论：1已评论，0代表未评论
		private Integer isComment = 0;
		private boolean isGift;

		public Integer getProductCode() {
			return productCode;
		}

		public void setProductCode(Integer productCode) {
			this.productCode = productCode;
		}

		public Integer getIsComment() {
			return isComment;
		}

		public void setIsComment(Integer isComment) {
			this.isComment = isComment;
		}

		public String getImageSrc() {
			return imageSrc;
		}

		public void setImageSrc(String imageSrc) {
			this.imageSrc = imageSrc;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public Integer getOrderDetailId() {
			return orderDetailId;
		}

		public void setOrderDetailId(Integer orderDetailId) {
			this.orderDetailId = orderDetailId;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public Integer getAmount() {
			return amount;
		}

		public void setAmount(Integer amount) {
			this.amount = amount;
		}

		public double getTotalPrice() {
			return totalPrice;
		}

		public void setTotalPrice(double totalPrice) {
			this.totalPrice = totalPrice;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public boolean isGift() {
			return isGift;
		}

		public void setGift(boolean isGift) {
			this.isGift = isGift;
		}

	}

	/**
	 * 用户订单的收货地址
	 */
	class UserAddressVO {
		// 收货人名字
		private String userName;
		// 电话
		private String cellphone;
		// 省
		private String provinceName;
		// 市
		private String cityName;
		// 区
		private String areaName;
		// 地址
		private String address;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getCellphone() {
			return cellphone;
		}

		public void setCellphone(String cellphone) {
			this.cellphone = cellphone;
		}

		public String getCityName() {
			return cityName;
		}

		public void setCityName(String cityName) {
			this.cityName = cityName;
		}

		public String getProvinceName() {
			return provinceName;
		}

		public void setProvinceName(String provinceName) {
			this.provinceName = provinceName;
		}

		public String getAreaName() {
			return areaName;
		}

		public void setAreaName(String areaName) {
			this.areaName = areaName;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

	}

	/**
	 * 已评论VO
	 */
	class HaveCommentOrderVO {
		// 订单id
		private Long orderId;
		// 订单明细id
		private Integer orderDetailId;
		// 产品名称
		private String productName;
		// 产品图片
		private String imageSrc;
		// 款式
		private String size;
		// 颜色
		private String color;
		// 是否评论：1已评论，0代表未评论
		private Integer isComment = 0;
		// 评论时间
		private String commentTime = null;
		// 评论ID
		private int commentId;
		// 商品编码
		private int productCode;

		public int getProductCode() {
			return productCode;
		}

		public void setProductCode(int productCode) {
			this.productCode = productCode;
		}

		public Long getOrderId() {
			return orderId;
		}

		public void setOrderId(Long orderId) {
			this.orderId = orderId;
		}

		public Integer getOrderDetailId() {
			return orderDetailId;
		}

		public void setOrderDetailId(Integer orderDetailId) {
			this.orderDetailId = orderDetailId;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public String getImageSrc() {
			return imageSrc;
		}

		public void setImageSrc(String imageSrc) {
			this.imageSrc = imageSrc;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public Integer getIsComment() {
			return isComment;
		}

		public void setIsComment(Integer isComment) {
			this.isComment = isComment;
		}

		public String getCommentTime() {
			return commentTime;
		}

		public void setCommentTime(String commentTime) {
			this.commentTime = commentTime;
		}

		public int getCommentId() {
			return commentId;
		}

		public void setCommentId(int commentId) {
			this.commentId = commentId;
		}
	}

}
