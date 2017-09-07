package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.EbShoppingCart;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.ShippingRule;
import com.ytsp.db.enums.ShippingTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.ShoppingCartVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.EbShoppingCartService;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.ShippingRuleService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.Util;

/**
 * @description 购物车命令
 * 
 */
public class ShoppingCartCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_SHOPPINGCART_LIST
				|| code == CommandList.CMD_SHOPPINGCART_ADD
				|| code == CommandList.CMD_SHOPPINGCART_DELETE
				|| code == CommandList.CMD_SHOPPINGCART_INCREASE
				|| code == CommandList.CMD_SHOPPINGCART_DECREASE
				|| code == CommandList.CMD_SHOPPINGCART_SYN
				|| code == CommandList.CMD_SHOPPINGCART_CLEAR
				|| code == CommandList.CMD_SHOPPINGCART_CHECKED
				|| code == CommandList.CMD_SHOPPINGCART_CHECKED_ALL
				|| code == CommandList.CMD_SHOPPINGCART_COUNT;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		if (code == CommandList.CMD_SHOPPINGCART_LIST) {
			return shoppingCartList();
		} else if (code == CommandList.CMD_SHOPPINGCART_ADD) {
			return addShoppingCartItem();
		} else if (code == CommandList.CMD_SHOPPINGCART_DELETE) {
			return shoppingCartDelete();
		} else if (code == CommandList.CMD_SHOPPINGCART_INCREASE) {
			return increaseShoppingCartItem();
		} else if (code == CommandList.CMD_SHOPPINGCART_DECREASE) {
			return decreaseShoppingCartItem();
		} else if (code == CommandList.CMD_SHOPPINGCART_SYN) {
			return shoppingCartSyn();
		} else if (code == CommandList.CMD_SHOPPINGCART_CLEAR) {
			return clearShoppingCart();
		} else if (code == CommandList.CMD_SHOPPINGCART_CHECKED) {
			return checked();
		} else if (code == CommandList.CMD_SHOPPINGCART_CHECKED_ALL) {
			return checkedAll();
		} else if (code == CommandList.CMD_SHOPPINGCART_COUNT) {
			return count();
		}
		return null;
	}
	
	/**
	* <p>功能描述:选中购物车中指定的商品，入参ids</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult checkShoppingCartItemByIds() {
		try{
		logger.info("###> start AutoCompleteOrder orders");
		int success = 0;
		int failure = 0;
		int total = 0;
		long startTime = System.currentTimeMillis();
		EbOrderService ebOrderService = SystemInitialization.getApplicationContext().getBean(EbOrderService.class);
		List<Long> orders = ebOrderService.findAutoCompleteOrder();
		if (orders == null || orders.size() == 0) {
			logger.info("###> no order auto complete, over");
		}
		total = orders.size();
		logger.info("###> order count will be autocomplete:" + total);
		//要做到足够的原子性,只能一条一条的处理.
		for (Long orderid : orders) {
			try{
				ebOrderService.updateCompleteOrder(orderid);
				success++;
			} catch (Exception e) {
				logger.error("complete order exception, order NO:" + orderid + ",exception:" + e.getMessage());
				failure++;
			}
		}
		long end = System.currentTimeMillis() - startTime;
		logger.info("###> complete-order mission accomplished, takes time:" + end+ "milliseconds, orders should be complete:" + total +" completed:" + success + " failed:" + failure);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
				null, this);

	}
	
	/**
	 * 购物车列表,不需要参数
	 * 
	 * @return
	 */
	private ExecuteResult shoppingCartList() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		// Service
		EbShoppingCartService ebShoppingCartService = SystemInitialization
				.getApplicationContext().getBean(EbShoppingCartService.class);
		// 获取购物车列表
		try {
			JSONObject obj = new JSONObject();
			// 更新合并购物车
			// Map<Integer, EbShoppingCart> ebShoppingCarts =
			// ebShoppingCartService.updateAndCombineShoppingCart(
			// userId, cartId);
			List<EbShoppingCart> ebShoppingCarts = ebShoppingCartService
					.updateAndCombineShoppingCart(userId, cartId);
			ShoppingCartVO shoppingCartVO = ebShoppingCartService
					.getShoppingCartVO(userId, cartId, SystemManager
							.getInstance().getSystemConfig().getImgServerUrl(),
							ebShoppingCarts,getContext().getHead().getVersion(),getContext().getHead().getPlatform());
			Gson gson = new Gson();
			obj.put("shoppingCartVO", gson.toJson(shoppingCartVO));
			obj.put("shippingRule", getShippingRule());
			//添加统计数据
			Util.addStatistics(getContext(), shoppingCartVO);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:获取邮费计算规则提示信息</p>
	* 1.如果未登录：提示新用户首单满39免邮
	* 2.如果已登录：（1）首单提示新用户首单满39免邮（2）非首单，全场满68包邮
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 * @throws SqlException 
	 */
	private String getShippingRule() throws SqlException{
		ShippingRuleService shippingRuleServ = SystemInitialization.getApplicationContext().getBean(ShippingRuleService.class);
		Map<Integer,ShippingRule> ruleMap = shippingRuleServ.getShippingRule();
		//已登录
		if(isLogin()){
			EbOrderService orderServ = SystemInitialization.getApplicationContext().getBean(EbOrderService.class);
			//首单
			if(orderServ.isFirstOrder(getContext().getHead().getUid())){
				if(ruleMap.containsKey(ShippingTypeEnum.FIRSTORDER.getValue())){
					ShippingRule shipingRule = ruleMap.get(ShippingTypeEnum.FIRSTORDER.getValue());
					return shipingRule.getDescription();
				}else{
					return IConstants.SHIPPING_RULE_FIRST_ORDER_DESC;
				}
			}else{//非首单
				if(ruleMap.containsKey(ShippingTypeEnum.NOTFIRST.getValue())){
					ShippingRule shipingRule = ruleMap.get(ShippingTypeEnum.NOTFIRST.getValue());
					return shipingRule.getDescription();
				}else{
					return IConstants.SHIPPING_RULE_NOT_FIRST_DESC;
				}
			}
		}else{//未登录
			if(ruleMap.containsKey(ShippingTypeEnum.FIRSTORDER.getValue())){
				ShippingRule shipingRule = ruleMap.get(ShippingTypeEnum.FIRSTORDER.getValue());
				return shipingRule.getDescription();
			}else{
				return IConstants.SHIPPING_RULE_FIRST_ORDER_DESC;
			}
		}
	}
	
	/**
	 * 功能描述:是否登录
	 * 参数：@return
	 * 返回类型：boolean
	 */
	private boolean isLogin() {
		if (getSessionCustomer() == null) {
			return false;
		} else if (getSessionCustomer().getCustomer() == null) {
			return false;
		} else if (getContext().getHead().getUid() == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 添加一件商品到购物车 <br/>
	 * 入参：int skuCode;
	 * 
	 * @return
	 */
	private ExecuteResult addShoppingCartItem() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			if (userId == 0 && StringUtils.isEmpty(cartId)) {
				result.put("result", false);
				result.put("msg", "数据错误");
			} else {
				int skuCode = jsonObj.optInt("skuCode");
				int amount = jsonObj.optInt("amount");
				if (amount == 0) {
					amount = 1;
				}
				EbShoppingCartService ebShoppingCartService = SystemInitialization
						.getApplicationContext().getBean(
								EbShoppingCartService.class);
				EbShoppingCart cart = ebShoppingCartService
						.addShopiingCartItem(skuCode, cartId, userId, amount);
				if (cart == null) {
					result.put("result", false);
					result.put("msg", "商品不存在");
				} else {
					result.put("result", true);
				}
				//添加统计信息
				Util.addStatistics(getContext(), cart);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					result, this);

		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 删除购物车商品<br/>
	 * 入参：int[] ids;
	 * 
	 * @return
	 */
	private ExecuteResult shoppingCartDelete() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONArray jsonIDS = jsonObj.optJSONArray("ids");
		try {
			JSONObject obj = new JSONObject();
			if (jsonIDS != null && jsonIDS.length() > 0) {
				int size = jsonIDS.length();
				int[] ids = new int[size];
				for (int i = 0; i < size; i++) {
					ids[i] = jsonIDS.optInt(i);
				}
				EbShoppingCartService shoppingCartService = SystemInitialization
						.getApplicationContext().getBean(
								EbShoppingCartService.class);
				shoppingCartService.delete(userId, cartId, ids);
			}
			obj.put("result", true);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 增加件数<br/>
	 * 入参：int id;
	 * 
	 * @return
	 */
	private ExecuteResult increaseShoppingCartItem() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			JSONObject obj = new JSONObject();
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			EbSkuService ebSkuService = SystemInitialization
					.getApplicationContext().getBean(EbSkuService.class);
			int id = jsonObj.optInt("id");
			if (id == 0) {
				obj.put("result", false);
				obj.put("msg", "购物车不存在当前商品");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"操作成功", obj, this);
			}
			EbShoppingCart ebShoppingCart = shoppingCartService
					.retrieveShoppingCart(userId, cartId, id);
			if (ebShoppingCart != null) {
				if (ebShoppingCart.getAmount() < 10) {
					ebShoppingCart.setAmount(ebShoppingCart.getAmount() + 1);
					shoppingCartService.updateEbShoppingCart(ebShoppingCart);
					EbSku ebSku = ebSkuService
							.retrieveEbSkuBySkuCode(ebShoppingCart.getSkuCode());
					obj.put("result", true);
					obj.put("oos", ebSku.getStorage().getAvailable()
							- ebShoppingCart.getAmount());
				} else {
					obj.put("result", false);
					obj.put("msg", "每件商品限购10件");
				}
			} else {
				obj.put("result", false);
				obj.put("msg", "购物车不存在当前商品");
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 减少件数<br/>
	 * 入参：int id;
	 * 
	 * @return
	 */
	private ExecuteResult decreaseShoppingCartItem() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			JSONObject obj = new JSONObject();
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			int id = jsonObj.optInt("id");
			if (id == 0) {
				obj.put("result", false);
				obj.put("msg", "购物车不存在当前商品");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"操作成功", obj, this);
			}
			EbShoppingCart ebShoppingCart = shoppingCartService
					.retrieveShoppingCart(userId, cartId, id);
			if (ebShoppingCart != null) {
				if (ebShoppingCart.getAmount() > 1) {
					ebShoppingCart.setAmount(ebShoppingCart.getAmount() - 1);
					shoppingCartService.updateEbShoppingCart(ebShoppingCart);
					obj.put("result", true);
				} else {
					obj.put("result", false);
					obj.put("msg", "购物车商品至少要有一件");
				}
			} else {
				obj.put("result", false);
				obj.put("msg", "购物车不存在当前商品");
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 同步购物车<br/>
	 * 入参：List<ShoppingCartItem> cartItems;
	 * 
	 * @return
	 */
	private ExecuteResult shoppingCartSyn() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		try {
			if (!jsonObj.isNull("cartItems")) {
				EbShoppingCartService shoppingCartService = SystemInitialization
						.getApplicationContext().getBean(
								EbShoppingCartService.class);
				JSONArray cartItemArray = jsonObj.getJSONArray("cartItems");
				List<EbShoppingCart> ebShoppingCartsClient = new ArrayList<EbShoppingCart>();
				int size = cartItemArray.length();
				for (int i = 0; i < size; i++) {
					JSONObject json = cartItemArray.getJSONObject(i);
					EbShoppingCart cart = new EbShoppingCart();
					cart.setAddTime(new Date());
					cart.setAmount(json.getInt("amount"));
					cart.setProductCode(json.getInt("productCode"));
					cart.setProductColor(json.getString("color"));
					cart.setProductImage(json.getString("productImage"));
					cart.setProductName(json.getString("productName"));
					cart.setProductSize(json.getString("size"));
					cart.setSkuCode(json.getInt("skuCode"));
					cart.setStatus(0);
					cart.setUserId(userId);
					ebShoppingCartsClient.add(cart);
				}
				// List<EbShoppingCart> ebShoppingCartsServer =
				// shoppingCartService
				// .retrieveShoppingCarts(userId, cartId);
				// for (EbShoppingCart ebShoppingCartClient :
				// ebShoppingCartsClient) {
				// for (EbShoppingCart ebShoppingCartServer :
				// ebShoppingCartsServer) {
				// if (ebShoppingCartClient.getSkuCode().equals(
				// ebShoppingCartServer.getSkuCode())) {
				// // 将临时购物车合并到登陆后的购物车
				// int totalCount = ebShoppingCartServer.getAmount()
				// + ebShoppingCartClient.getAmount();
				// ebShoppingCartServer.setAmount(totalCount > 10 ? 10
				// : totalCount);
				// }
				// }
				// }
				// shoppingCartService.updateAll(ebShoppingCartsServer);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					null, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 清空购物车
	 * 
	 * @return
	 */
	private ExecuteResult clearShoppingCart() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		try {
			JSONObject obj = new JSONObject();
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			shoppingCartService.deleteAll(userId, cartId);
			obj.put("result", true);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * 选购物车选中指定的商品，其它不选中。入参：checked:布尔类型，ids:购物车id,以json数组传入<br/>
	 * 入参:int[] ids,boolean checked
	 * 
	 * @return
	 */
	private ExecuteResult checked() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		boolean checked = jsonObj.optBoolean("checked");
		JSONArray jsonIDS = jsonObj.optJSONArray("ids");
		
		try {
			JSONObject obj = new JSONObject();
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			if (jsonIDS != null && jsonIDS.length() > 0) {
				int size = jsonIDS.length();
				int[] ids = new int[size];
				for (int i = 0; i < size; i++) {
					ids[i] = jsonIDS.optInt(i);
				}
				shoppingCartService.updateShoppingCartChecked(userId, cartId,
						checked, ids);

			}
			obj.put("result", true);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * 全选<br/>
	 * 入参:boolean checked
	 * 
	 * @return
	 */
	private ExecuteResult checkedAll() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		boolean checked = jsonObj.optBoolean("checked");
		try {
			JSONObject obj = new JSONObject();
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			shoppingCartService.updateShoppingCartCheckedAll(userId, cartId, checked);
			obj.put("result", true);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}

	/**
	 * 购物车数量
	 * 
	 * @return
	 */
	private ExecuteResult count() {
		int userId = getContext().getHead().getUid();
		String cartId = getContext().getHead().getCartId();
		try {
			JSONObject obj = new JSONObject();
			EbShoppingCartService shoppingCartService = SystemInitialization
					.getApplicationContext().getBean(
							EbShoppingCartService.class);
			obj.put("count", shoppingCartService.getCount(userId, cartId));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取购物车内数量成功", obj, this);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}

	}
}
