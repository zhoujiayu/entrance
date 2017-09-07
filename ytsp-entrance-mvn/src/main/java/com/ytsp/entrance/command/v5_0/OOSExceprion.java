package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.vo.GiftItem;
import com.ytsp.db.vo.ShoppingCartItem;

/**
 * @description 库存异常
 */
public class OOSExceprion extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3633489214089439588L;

	/**
	 * 超出库存的商品
	 */
	private List<ShoppingCartItem> cartItems = new ArrayList<ShoppingCartItem>();
	/**
	 * 超出库存的赠品
	 */
	private List<GiftItem> giftItems = new ArrayList<GiftItem>();
	private String message;

	public List<ShoppingCartItem> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<ShoppingCartItem> cartItems) {
		this.cartItems = cartItems;
	}

	public List<GiftItem> getGiftItems() {
		return giftItems;
	}

	public void setGiftItems(List<GiftItem> giftItems) {
		this.giftItems = giftItems;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public OOSExceprion() {
		super();
	}

	public OOSExceprion(String message, List<GiftItem> giftItems) {
		super();
		this.giftItems = giftItems;
		this.message = message;
	}

	public OOSExceprion(List<ShoppingCartItem> cartItems, String message) {
		super();
		this.cartItems = cartItems;
		this.message = message;
	}

	public OOSExceprion(String message) {
		super();
		this.message = message;
	}

	public JSONObject getOOSGiftItems() throws JSONException {
		JSONObject ret = new JSONObject();
		Gson gson = new Gson();
		ret.put("oosGiftItems", gson.toJson(this.giftItems));
		return ret;
	}

	public JSONObject getOOSCartItems() throws JSONException {
		JSONObject ret = new JSONObject();
		Gson gson = new Gson();
		ret.put("oosCartItems", gson.toJson(this.cartItems));
		return ret;
	}

	public JSONObject getOOSResult() throws JSONException {
		JSONObject ret = new JSONObject();
		Gson gson = new Gson();
		if (this.cartItems != null) {
			ret.put("oosCartItems", gson.toJson(this.cartItems));
		}
		if (this.giftItems != null) {
			ret.put("oosGiftItems", gson.toJson(this.giftItems));
		}
		return ret;
	}

	public void addOOSCartItem(EbOrderDetail detail, int available) {
		if (this.cartItems == null) {
			this.cartItems = new ArrayList<ShoppingCartItem>();
		}
		ShoppingCartItem cartItem = new ShoppingCartItem();
		cartItem.setAmount(detail.getAmount());
		cartItem.setColor(detail.getColor());
		cartItem.setStorageNum(available);
		cartItem.setOosNum(available - detail.getAmount());
		cartItem.setSkuCode(detail.getSkuCode());
		cartItem.setProductName(detail.getProductName());
		cartItem.setProductCode(detail.getProductCode());
		cartItem.setSize(detail.getSize());
		cartItem.setProductImage(detail.getImageSrc());
		this.cartItems.add(cartItem);
	}

	public void addOOSGiftItem(EbOrderDetail detail, int available) {
		if (this.giftItems == null) {
			this.giftItems = new ArrayList<GiftItem>();
		}
		GiftItem g = new GiftItem();
		g.setAmount(detail.getAmount());
		g.setColor(detail.getColor());
		g.setStorageNum(available);
		g.setOosNum(available - detail.getAmount());
		g.setSkuCode(detail.getSkuCode());
		g.setProductName(detail.getProductName());
		g.setProductCode(detail.getProductCode());
		g.setSize(detail.getSize());
		g.setProductImage(detail.getImageSrc());
		this.giftItems.add(g);
	}
}
