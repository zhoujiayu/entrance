package com.ytsp.entrance.test;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.icu.util.Calendar;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.util.DateFormatter;

public class TestAllCommand {
	// 知识主页
	public static final int CMD_KNOWLEDGE_HOMEPAGE = CommandList.CMD_KNOWLEDGE_HOMEPAGE;
	// 商品一级分类
	public static final int CMD_CATEGORY_FIRST = CommandList.CMD_CATEGORY_FIRST;
	// 待评论列表：入参：page,pageSize
	public static final int CMD_ORDER_WAITCOMMENT = CommandList.CMD_ORDER_WAITCOMMENT;
	// 已评论列表：入参：page,pageSize
	public static final int CMD_ORDER_HAVECOMMENT = CommandList.CMD_ORDER_HAVECOMMENT;
	// 商品分类
	public static final int CMD_CATAGORY_QUERY = CommandList.CMD_SEARCH_PRODUCTCATEGORY;
	// 文章详情
	public static final int CMD_TOPIC_GETBYID = CommandList.CMD_TOPIC_GETBYID;
	// 动漫搜索
	public static final int CMD_SEARCH_ANIMECONDITION = CommandList.CMD_SEARCH_ANIMECATEGORY;

	public static final int CMD_SEARCH_KNOWLEDGEHOME = CommandList.CMD_SEARCH_KNOWLEDGEHOME;
	// 根据分类id搜索知识或者动漫
	public static final int CMD_SEARCH_ALBUMBYCATEGORY = CommandList.CMD_SEARCH_ALBUMBYCATEGORY;

//	public static final int CMD_VIP_PRODUCT_LIST_V5 = CommandList.CMD_VIP_PRODUCT_LIST_V5;

	public static final int CMD_VIP_CREATE_ORDER_V5 = CommandList.CMD_VIP_CREATE_ORDER_V5;

	public static final int CMD_CUSTOMER_REGIST = CommandList.CMD_CUSTOMER_REGIST;
	// 所有品牌
	public static final int CMD_BRAND_ALL = CommandList.CMD_BRAND_ALL;
	// 优惠券列表
	public static final int CMD_COUPON_MINE = CommandList.CMD_COUPON_MINE;
	// 搜索品牌
	public static final int CMD_SEARCH_BYBRANDID = CommandList.CMD_SEARCH_BYBRANDID;
	// 搜索
	public static final int CMD_SEARCH = CommandList.CMD_SEARCH_PRODUCT;

	public static final int CMD_SEARCH_ANIME = CommandList.CMD_SEARCH_ANIME;
	// 分类获取知识
	public static final int CMD_KNOWLEDGE_BYCATEGORY = CommandList.CMD_KNOWLEDGE_BYCATEGORY;
	// 搜索商品带综合排序筛选，分类，年龄条件数据
	public static final int CMD_SEARCH_PRODUCT_WITHCONDITION = CommandList.CMD_SEARCH_PRODUCT_WITHCONDITION;
	// 搜索动漫带综合排序，筛选，分类，年龄条件数据
	public static final int CMD_SEARCH_ANIME_WITHCONDITION = CommandList.CMD_SEARCH_ANIME_WITHCONDITION;
	// 搜索知识带综合排序，分类，年龄条件数据
	public static final int CMD_SEARCH_KNOWLEDGE_WITHCONDITION = CommandList.CMD_SEARCH_KNOWLEDGE_WITHCONDITION;
	//
	public static final int CMD_SEARCH_ALL = CommandList.CMD_SEARCH_MIX;

	// 我的可用现金券分页:入参:page,pageSize,couponId最后一个现金券id
	public static final int CMD_COUPON_CASH_BYPAGE = CommandList.CMD_COUPON_CASH_BYPAGE;
	// 我的可用满减券分页:入参:page,pageSize,couponId最后一个满减券id
	public static final int CMD_COUPON_REDUCE_BYPAGE = CommandList.CMD_COUPON_REDUCE_BYPAGE;
	// 我的不可用现金券分页:入参:page,pageSize,couponId最后一个现金券id
	public static final int CMD_COUPON_UNABLE_CASH_BYPAGE = CommandList.CMD_COUPON_UNABLE_CASH_BYPAGE;
	// 我的不可用满减券分页:入参:page,pageSize,couponId最后一个满减券id
	public static final int CMD_COUPON_UNABLE_REDUCE_BYPAGE = CommandList.CMD_COUPON_UNABLE_REDUCE_BYPAGE;

	public static final int CMD_CUSTOMER_CENTER = CommandList.CMD_CUSTOMER_CENTER;
	
	public static final int CMD_CUSTOMER_PHONE_IS_VAILDATE =CommandList.CMD_CUSTOMER_PHONE_IS_VAILDATE;

	public static final int CMD_CUSTOMER_PHONE_VALIDATE_NUM =CommandList.CMD_CUSTOMER_PHONE_VALIDATE_NUM;
	
	public static final int CMD_CUSTOMER_PHONE_VALIDATE = CommandList.CMD_CUSTOMER_PHONE_VALIDATE;
	public static final int CMD_CUSTOMER_EMAIL_VALIDATE = CommandList.CMD_CUSTOMER_EMAIL_VALIDATE;
	public static final int CMD_CUSTOMER_CHANGE_PHONE_BYEMAIL = CommandList.CMD_CUSTOMER_CHANGE_PHONE_BYEMAIL;
	public static final int CMD_CUSTOMER_CHANGE_PHONE_SEND_EMAIL = CommandList.CMD_CUSTOMER_CHANGE_PHONE_SEND_EMAIL;
	public static final int CMD_FORGET_PWD = CommandList.CMD_FORGET_PWD;
	public static final int CMD_SHOPPINGCART_LIST = CommandList.CMD_SHOPPINGCART_LIST;
	public static final int CMD_VIP_COST_DEFINE_LIST = CommandList.CMD_VIP_COST_DEFINE_LIST;
	public static final int CMD_TOPIC_LIST = CommandList.CMD_TOPIC_LIST;
	public static final int CMD_SPECIAL_LIST = CommandList.CMD_SPECIAL_LIST;
	public static final int CMD_PUSHMESSAGE_LAST_V5 = CommandList.CMD_PUSHMESSAGE_LAST_V5;
	public static final int CMD_AD_LAUNCH_V5 = CommandList.CMD_AD_LAUNCH_V5;
	public static final int CMD_COUPON_MINE_AVAILABLE = CommandList.CMD_COUPON_MINE_AVAILABLE;
	public static final int CMD_COUPON_MINE_UNAVAILABLE = CommandList.CMD_COUPON_MINE_UNAVAILABLE;
	public static final int CMD_COUPON_LIST_AVAILABLE = CommandList.CMD_COUPON_LIST_AVAILABLE;
	public static final int CMD_COUPON_LIST_UNAVAILABLE = CommandList.CMD_COUPON_LIST_UNAVAILABLE;
	public static final int CMD_PRODUCT_GETBYPRODUCODE = CommandList.CMD_PRODUCT_GETBYPRODUCODE;
	public static final int CMD_SEARCH_MIX_FOR_IPAD = CommandList.CMD_SEARCH_MIX_BY_CATEGORY;
	public static final int CMD_RECOMMEND_PRODUCTPAGE = CommandList.CMD_RECOMMEND_PRODUCTPAGE;
	
	public static final int CMD_ALBUM_HOMEPAGE = CommandList.CMD_ALBUM_HOMEPAGE;
	public static final int CMD_COUPON_LIST_BY_TYPE = CommandList.CMD_COUPON_LIST_BY_TYPE;
	public static final int CMD_RECOMMEND = CommandList.CMD_RECOMMEND;
	public static final int CMD_ORDER_MINE_QUERY_BY_CATEGORY = CommandList.CMD_ORDER_MINE_QUERY_BY_CATEGORY;
	public static final int CMD_WXPAY_QUERY = CommandList.CMD_WXPAY_QUERY;
	
	public static final int CMD_VIDEO_QUERY_RECENT_PLAY = CommandList.CMD_VIDEO_QUERY_RECENT_PLAY;
	public static final int CMD_MEMBER_VIDEO_PLAY_V3_1 = CommandList.CMD_MEMBER_VIDEO_PLAY_V3_1;
	public static final int CMD_ALBUM_UNREVIEW_LIST = CommandList.CMD_ALBUM_UNREVIEW_LIST;
	public static final int CMD_VIDEO_UNREVIEW_LIST = CommandList.CMD_VIDEO_UNREVIEW_LIST;
	public static final int CMD_CREDIT_STRATEGY_QUERY = CommandList.CMD_CREDIT_STRATEGY_QUERY;
	public static final int CMD_EB_ORDER_CLIENT_PAY_SUCCESS_V5 = CommandList.CMD_EB_ORDER_CLIENT_PAY_SUCCESS_V5;
	public static final int CMD_PRODUCT_GET_BY_EANCODE = CommandList.CMD_PRODUCT_GET_BY_EANCODE;
	public static final int CMD_ALBUM_DETAIL_V5 = CommandList.CMD_ALBUM_DETAIL_V5;
	public static final int CMD_SCANNING_REDIRECT = CommandList.CMD_SCANNING_REDIRECT;
	public static final int CMD_ORDER_PAY_PREPARE = CommandList.CMD_ORDER_PAY_PREPARE;
	public static final int CMD_WEBMOBILE_MINE_PAGE_ORDER_NUMBER = CommandList.CMD_WEBMOBILE_MINE_PAGE_ORDER_NUMBER;
	public static void main(String[] args) {
		HeadInfo payPre = getHeadInfo(CMD_ORDER_PAY_PREPARE);
		HeadInfo albumDetail = getHeadInfo(CMD_ALBUM_DETAIL_V5);
		HeadInfo eancode = getHeadInfo(CMD_PRODUCT_GET_BY_EANCODE);
		HeadInfo creditStrategy = getHeadInfo(CMD_CREDIT_STRATEGY_QUERY);
		HeadInfo pushMsg = getHeadInfo(CMD_PUSHMESSAGE_LAST_V5);
		HeadInfo ad = getHeadInfo(CMD_AD_LAUNCH_V5);
		HeadInfo topList = getHeadInfo(CMD_SPECIAL_LIST);
		HeadInfo mycoupon = getHeadInfo(CMD_COUPON_MINE);
		HeadInfo brand = getHeadInfo(CMD_BRAND_ALL);
		HeadInfo knowHead = getHeadInfo(CMD_KNOWLEDGE_HOMEPAGE);
		HeadInfo firstCate = getHeadInfo(CMD_CATEGORY_FIRST);
		HeadInfo waitHead = getHeadInfo(CMD_ORDER_WAITCOMMENT);
		HeadInfo commentHead = getHeadInfo(CMD_ORDER_HAVECOMMENT);
		HeadInfo categroyHead = getHeadInfo(CMD_CATAGORY_QUERY);
		HeadInfo topicHead = getHeadInfo(CMD_TOPIC_GETBYID);
		HeadInfo searchHead = getHeadInfo(CMD_SEARCH_ANIMECONDITION);
		HeadInfo knowsearchHead = getHeadInfo(CMD_SEARCH_KNOWLEDGEHOME);
		HeadInfo searchCategory = getHeadInfo(CMD_SEARCH_ALBUMBYCATEGORY);
		HeadInfo regist = getHeadInfo(CMD_CUSTOMER_REGIST);
		HeadInfo brandHead = getHeadInfo(CMD_SEARCH_BYBRANDID);
		HeadInfo searchAllHead = getHeadInfo(CMD_SEARCH);
		HeadInfo searchAnimeHead = getHeadInfo(CMD_SEARCH_ANIME);
		HeadInfo knowCategory = getHeadInfo(CMD_KNOWLEDGE_BYCATEGORY);

		HeadInfo prodSearchWitCond = getHeadInfo(CMD_SEARCH_PRODUCT_WITHCONDITION);
		HeadInfo animeSearchWitCond = getHeadInfo(CMD_SEARCH_ANIME_WITHCONDITION);
		HeadInfo knowSearchWitCond = getHeadInfo(CMD_SEARCH_KNOWLEDGE_WITHCONDITION);
		HeadInfo searchMix = getHeadInfo(CMD_SEARCH_ALL);
		HeadInfo cashCoupon = getHeadInfo(CMD_COUPON_CASH_BYPAGE);
		HeadInfo reduceCoupon = getHeadInfo(CMD_COUPON_REDUCE_BYPAGE);
		HeadInfo unalbeCashCoupon = getHeadInfo(CMD_COUPON_UNABLE_CASH_BYPAGE);
		HeadInfo unableRedCoupon = getHeadInfo(CMD_COUPON_UNABLE_REDUCE_BYPAGE);
		HeadInfo custCenter = getHeadInfo(CMD_CUSTOMER_CENTER);
		
//		HeadInfo isphoneVal = getHeadInfo(CMD_CUSTOMER_PHONE_IS_VAILDATE);
		HeadInfo phoneValNum = getHeadInfo(CMD_CUSTOMER_PHONE_VALIDATE_NUM);
		HeadInfo phoneVal = getHeadInfo(CMD_CUSTOMER_PHONE_VALIDATE);
		HeadInfo emailVal =getHeadInfo( CMD_CUSTOMER_EMAIL_VALIDATE);
		HeadInfo changePhonebyEmail =getHeadInfo( CMD_CUSTOMER_CHANGE_PHONE_BYEMAIL);
		HeadInfo sendEmail =getHeadInfo(CMD_CUSTOMER_CHANGE_PHONE_SEND_EMAIL);
		HeadInfo forget =getHeadInfo(CMD_FORGET_PWD);
		HeadInfo shoppingCart = getHeadInfo(CMD_SHOPPINGCART_LIST);
		HeadInfo vipCostDef = getHeadInfo(CMD_VIP_COST_DEFINE_LIST);
		HeadInfo available = getHeadInfo(CMD_COUPON_LIST_AVAILABLE);
		HeadInfo unavailable = getHeadInfo(CMD_COUPON_LIST_UNAVAILABLE);
		HeadInfo prodDetail = getHeadInfo(CMD_PRODUCT_GETBYPRODUCODE);
		HeadInfo recommendPage = getHeadInfo(CMD_RECOMMEND_PRODUCTPAGE);
		HeadInfo mixSearch4pad = getHeadInfo(CMD_SEARCH_MIX_FOR_IPAD);
		HeadInfo albumHome = getHeadInfo(CMD_ALBUM_HOMEPAGE);
		HeadInfo couponList = getHeadInfo(CMD_COUPON_LIST_BY_TYPE);
		// HeadInfo saveHead = getSaveHeadInfo();
		HeadInfo recommend = getHeadInfo(CMD_RECOMMEND);
		HeadInfo myOrderByType = getHeadInfo(CMD_ORDER_MINE_QUERY_BY_CATEGORY);
		HeadInfo WXQuery = getHeadInfo(CMD_WXPAY_QUERY);
		HeadInfo recentVideo = getHeadInfo(CMD_VIDEO_QUERY_RECENT_PLAY);
		HeadInfo memberVide = getHeadInfo(CMD_MEMBER_VIDEO_PLAY_V3_1);
		HeadInfo unreviewList = getHeadInfo(CMD_ALBUM_UNREVIEW_LIST);
		HeadInfo unreviewVideos = getHeadInfo(CMD_VIDEO_UNREVIEW_LIST);
		HeadInfo paySuccess = getHeadInfo(CMD_EB_ORDER_CLIENT_PAY_SUCCESS_V5);
		HeadInfo scanning = getHeadInfo(CMD_SCANNING_REDIRECT);
		HeadInfo wapMobile = getHeadInfo(CMD_WEBMOBILE_MINE_PAGE_ORDER_NUMBER);
		try {
			
//			SendPostRequst.sendPostRequest(wapMobile, getBodyInfo());
//			SendPostRequst.sendPostRequest(scanning, getBodyInfo());
//			SendPostRequst.sendPostRequest(eancode, getBodyInfo());
//			SendPostRequst.sendPostRequest(albumDetail, getBodyInfo());
//			SendPostRequst.sendPostRequest(paySuccess, getBodyInfo());
//			SendPostRequst.sendPostRequest(creditStrategy, getBodyInfo());
//			SendPostRequst.sendPostRequest(unreviewVideos, getBodyInfo());
//			SendPostRequst.sendPostRequest(unreviewList, getBodyInfo());
//			SendPostRequst.sendPostRequest(memberVide, getBodyInfo());
//			SendPostRequst.sendPostRequest(recentVideo, getBodyInfo());
//			SendPostRequst.sendPostRequest(WXQuery, getBodyInfo());
//			SendPostRequst.sendPostRequest(shoppingCheck, getBodyInfo());
//			SendPostRequst.sendPostRequest(myOrderByType, getBodyInfo());
//			SendPostRequst.sendPostRequest(recommend, getBodyInfo());
//			SendPostRequst.sendPostRequest(couponList, getBodyInfo());
//			SendPostRequst.sendPostRequest(albumHome, getBodyInfo());
//			SendPostRequst.sendPostRequest(mixSearch4pad, getBodyInfo());
			SendPostRequst.sendPostRequest(recommendPage, getBodyInfo());
//			SendPostRequst.sendPostRequest(prodDetail, getBodyInfo());
//			SendPostRequst.sendPostRequest(available, getBodyInfo());
//			SendPostRequst.sendPostRequest(unavailable, getBodyInfo());
//			SendPostRequst.sendPostRequest(pushMsg, getBodyInfo());
//			SendPostRequst.sendPostRequest(ad, getBodyInfo());
//			SendPostRequst.sendPostRequest(topList, getBodyInfo());
//			SendPostRequst.sendPostRequest(vipCostDef, getBodyInfo());
//			SendPostRequst.sendPostRequest(shoppingCheck, getBodyInfo());
//			SendPostRequst.sendPostRequest(forget, getBodyInfo());
//			SendPostRequst.sendPostRequest(sendEmail, getBodyInfo());
//			SendPostRequst.sendPostRequest(changePhonebyEmail, getBodyInfo());
//			SendPostRequst.sendPostRequest(emailVal, getBodyInfo());
//			SendPostRequst.sendPostRequest(phoneValNum, getBodyInfo());
//			SendPostRequst.sendPostRequest(phoneVal, getBodyInfo());
//			SendPostRequst.sendPostRequest(isphoneVal, getBodyInfo());
//			SendPostRequst.sendPostRequest(custCenter, getBodyInfo());
			// SendPostRequst.sendPostRequest(cashCoupon, getBodyInfo());
			// SendPostRequst.sendPostRequest(unalbeCashCoupon, getBodyInfo());
			// SendPostRequst.sendPostRequest(reduceCoupon, getBodyInfo());
			// SendPostRequst.sendPostRequest(unableRedCoupon, getBodyInfo());
			// SendPostRequst.sendPostRequest(prodSearchWitCond,
			// getSearchBodyInfo());
			// SendPostRequst.sendPostRequest(animeSearchWitCond,
			// getSearchBodyInfo());
			// SendPostRequst.sendPostRequest(knowSearchWitCond,
			// getSearchBodyInfo());
			// SendPostRequst.sendPostRequest(searchMix, getSearchBodyInfo());
			// SendPostRequst.sendPostRequest(knowCategory, getBodyInfo());
			// SendPostRequst.sendPostRequest(searchAnimeHead,
			// getSearchBodyInfo());
//			SendPostRequst.sendPostRequest(searchAllHead, getSearchBodyInfo());
//			 SendPostRequst.sendPostRequest(brandHead, getBodyInfo());
			// SendPostRequst.sendPostRequest(mycoupon, getBodyInfo());
//			 SendPostRequst.sendPostRequest(brand, getBodyInfo());
			// SendPostRequst.sendPostRequest(regist, getBodyInfo());
			// SendPostRequst.sendPostRequest(VIPorder, getBodyInfo());
			// SendPostRequst.sendPostRequest(searchCategory, getBodyInfo());
//			 SendPostRequst.sendPostRequest(knowsearchHead, getBodyInfo());
//			 SendPostRequst.sendPostRequest(knowHead, getBodyInfo());
			// SendPostRequst.sendPostRequest(firstCate, getBodyInfo());
			// SendPostRequst.sendPostRequest(waitHead, getBodyInfo());
			// SendPostRequst.sendPostRequest(commentHead, getBodyInfo());
			// SendPostRequst.sendPostRequest(categroyHead, getBodyInfo());
//			SendPostRequst.sendPostRequest(topicHead, getBodyInfo());
			// SendPostRequst.sendPostRequest(searchHead, getBodyInfo());

			// SendPostRequst.sendPostRequest(saveHead, getSaveBodyInfo());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HeadInfo getHeadInfo(int cmd) {
		HeadInfo head = new HeadInfo();
		head.setCommandCode(cmd);
		// 10043040;10081948
		head.setUid(10083811);
		head.setPlatform("iphone");
		head.setIp("111.161.31.94");
		head.setVersion("5.0.0");
//		head.setCartId("2D7760E6-5036-4AAB-A5D1-CB6D3A50AB04");
		return head;
	}

	/**
	* <p>功能描述:</p>
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	*/
	private static JSONObject getBodyInfo() throws JSONException {
		JSONObject bodyJson = new JSONObject();
//		bodyJson.put("pageSize", 25);
//		bodyJson.put("page", 0);
//		bodyJson.put("trackId", 0);
//		bodyJson.put("categoryId", 10);
//		bodyJson.put("page", 0);
//		bodyJson.put("topicId", 2);
//		bodyJson.put("categoryId", 104);
//		bodyJson.put("nick", "灬o小明o灬");
//		bodyJson.put("account", "zhangming");
//		bodyJson.put("phone", "18618227655");
//		bodyJson.put("email", "396461777@qq.com");
//		bodyJson.put("password", "4158078");
//		bodyJson.put("password2", "4158078");
//		bodyJson.put("brandId", "4");
//		bodyJson.put("couponId", 0);
//		bodyJson.put("lastId", 0);
//		bodyJson.put("validateNum", "2292");
//		JSONArray arr = new JSONArray();
//		JSONObject skuObj1 = new JSONObject();
//		JSONObject skuObj2 = new JSONObject();
//		JSONObject skuObj3 = new JSONObject();
//		skuObj1.put("skuCode", 100101801);
//		skuObj2.put("skuCode", 100102201);
//		skuObj3.put("skuCode", 100200101);
//		arr.put(skuObj1);
//		arr.put(skuObj2);
//		arr.put(skuObj3);
//		bodyJson.put("cartItems", arr);
//		bodyJson.put("type", 4);
//		bodyJson.put("page", 0);
//		bodyJson.put("pageSize", 100);
//		bodyJson.put("orderTime", "2015-09-14 15:49:50");
//		bodyJson.put("orderId", "1509141549501100");
		bodyJson.put("page", 9);
		bodyJson.put("pageSize", 12);
//		bodyJson.put("pageSize", 12);
//		bodyJson.put("searchKey", "Balala");
//		JSONArray js = new JSONArray();
//		js.put(73580);
//		js.put(73578);
//		js.put(73525);
//		bodyJson.put("ids", js);
//		bodyJson.put("checked", true);
//		bodyJson.put("userId", 10083811);
//		bodyJson.put("type", 0);
//		bodyJson.put("vid", "1");
//		bodyJson.put("type", 4);
//		bodyJson.put("albumId", 3);
//		bodyJson.put("key", "0514a1bcbb78228d1baf49c421df34cf");
//		bodyJson.put("searchKey", "如比");
//		bodyJson.put("orderId", "1512071146213100");
//		bodyJson.put("EANCode", "12345622qqq7890127");
		bodyJson.put("code", "ed3de50d038b9268561a866f0aae5301");
		return bodyJson;
	}

	private static JSONObject getSearchBodyInfo() throws JSONException {
		// StringBuffer sb = new
		// StringBuffer("{\"catagorys\": \"8\",\"ageRange\": \"groupId\": \"4\",\"tagId\": \"37\"},\"sortVos\": \"1\",\"filterInfo\": {\"brandId\": [\"14\",\"12\"],\"albumId\": [\"2\",\"8\"],\"groupIds\": [{\"groupId\": \"1\",\"tagId\": [\"14\",\"15\",\"16\"]},{\"groupId\": \"2\",\"tagId\": [\"22\",\"23\",\"24\"]},{\"groupId\": \"3\",\"tagId\": [\"27\",\"28\"]}]}}");
		// StringBuffer sb = new
		// StringBuffer("{\"searchKey\":\"乐高\",\"sortVos\":\"1\",\"ageRange\":{\"groupId\":\"4\",\"tagId\":\"60\"},\"catagorys\":\"69\",\"filterInfo\":{\"brandId\":\"12\",\"albumId\":\"8\",\"groupIds\":[{\"groupId\":\"1\",\"tagId\":\"38\"},{\"groupId\":\"2\",\"tagId\":\"46\"},{\"groupId\":\"24\",\"tagId\":\"52\"}]}}");
		// StringBuffer sb = new
		// StringBuffer("{\"catagorys\":\"10\",\"page\":\"0\",\"pageSize\":\"12\"}");
		// StringBuffer animeSb = new
		// StringBuffer("{\"sortId\":\"1\",\"searchKey\":\"电击小子\",\"page\":\"0\",\"pageSize\":\"12\"}");
		// StringBuffer animeSb = new
		// StringBuffer("{\"catagoryId\":\"105\",\"page\":\"0\",\"pageSize\":\"12\",\"ageRange\":{\"groupId\":\"13\",\"tagId\":\"99\"}}");
		StringBuffer animeSb = new StringBuffer(
				"{\"searchKey\":\"如比\",\"page\":\"0\",\"pageSize\":\"100\"}");
		// JSONObject categroy = new JSONObject();
		// categroy.put("catagorys", 8);
		// JSONObject ageRange = new JSONObject();
		// ageRange.put("groupId", 4);
		// ageRange.put("tagId", 37);
		// JSONObject sortVos = new JSONObject();
		// sortVos.put("sortVos", 1);

		// JSONObject searchKey = new JSONObject();
		// searchKey.put("searchKey", 8);

		JSONObject bodyJson = new JSONObject(animeSb.toString());
		return bodyJson;
	}

	/**
	 * 创建指定数量的随机字符串
	 * 
	 * @param numberFlag
	 *            是否是数字
	 * @param length
	 * @return
	 */
	public static String createRandom(int length) {
		String retNum = "";
		String validateNum = "1234567890";
		int len = validateNum.length();
		for (int i = 0; i < length; i++) {
			double randomNum = Math.random() * len;
			int num = (int) Math.floor(randomNum);
			retNum += validateNum.charAt(num);
		}
		
		return retNum;
	}
	
	
	private static boolean isValidateNumValid(Date validatTime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(validatTime);
		cal.add(Calendar.MINUTE, 10);
		System.out.println(cal.getTime());
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.add(Calendar.MINUTE, 20);
		return cal.after(now);
	}
	
	private static boolean isHaveFont(String words,int time,char key){
		int length = words.length();
		int count = 0;
		for (int i = 0; i < length; i++) {
			char c = words.charAt(i);
			if(key == c){
				count++;
			}
		}
		if(time == count){
			return true;
		}
		return false;
	}
	
}
