package com.ytsp.entrance.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.dongman.util.UtilDate;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.DmsCardDao;
import com.ytsp.db.dao.LogVideoDao;
import com.ytsp.db.dao.MemberCostDefineDao;
import com.ytsp.db.dao.RechargeRecordAppleDao;
import com.ytsp.db.dao.RechargeRecordCardDao;
import com.ytsp.db.dao.VideoDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.DmsCard;
import com.ytsp.db.domain.LogVideo;
import com.ytsp.db.domain.MemberCostDefine;
import com.ytsp.db.domain.RechargeRecordApple;
import com.ytsp.db.domain.RechargeRecordCard;
import com.ytsp.db.domain.User;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.AlbumTypeEnum;
import com.ytsp.db.enums.CardTypeEnum;
import com.ytsp.db.enums.CardValidateEnum;
import com.ytsp.db.enums.MemberTypeEnum;
import com.ytsp.db.enums.MemberVideoStatusEnum;
import com.ytsp.db.enums.PlayVideoEnum;
import com.ytsp.db.enums.RechargeStatusEnum;
import com.ytsp.entrance.command.VersionCommand;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.URLParse;

public class MemberService {
	private RechargeRecordCardDao rechargeRecordCardDao;
	private RechargeRecordAppleDao rechargeRecordAppleDao;
	private MemberCostDefineDao memberCostDefineDao;
	private CustomerDao customerDao;
	private CustomerMemberDao customerMemberDao;
	private DmsCardDao dmsCardDao;
	private AlbumDao albumDao;
	private VideoDao videoDao;
	private LogVideoDao logVideoDao;
	private static final Logger logger = Logger.getLogger(MemberService.class);

	/**
	 * 充值卡充值
	 * 
	 * @param card_code
	 * @param card_password
	 * @param user_id
	 * @return
	 * @throws Exception
	 */
	public JSONObject saveCardRecharge(String cardCode, int userId,
			String terminalType, String terminalVersion, String ip)
			throws Exception {
		JSONObject ret = new JSONObject();
		List<DmsCard> cards = getByCodeAndPwd(cardCode);
		if (cards == null || cards.size() > 1 || cards.size() == 0) {
			ret.put("status", CardValidateEnum.CODE_ERROR.getValue());
			return ret;
		}

		DmsCard currCard = cards.get(0);
		boolean is_used = currCard.getIsUsed();
		Date live_time = currCard.getLiveTime();// 2011-09-07 00:00:00
		// 这是一个兼容性的判断，以前的卡都是有密码的，但是现在的都没有，所以只要有密码肯定不可用
		if (currCard.getCardPassword() != null
				&& currCard.getCardPassword().length() > 0) {
			ret.put("status", CardValidateEnum.CODE_ERROR.getValue());
			return ret;
		}
		if (is_used) {
			ret.put("status", CardValidateEnum.ALREADY_USED.getValue());
			return ret;
		}

		if (UtilDate.isOverLiveTime(live_time) > 0) {
			ret.put("status", CardValidateEnum.OVERDUE.getValue());
			return ret;
		}
		
		//校验同一批次的兑换码只能换一次
		if(isCardChargedByBatch(userId, currCard.getBatch())){
			ret.put("status", CardValidateEnum.BATCH_ERROR.getValue());
			return ret;
		}
		
		Customer customer = customerDao.getObject(Customer.class, userId);

		CardTypeEnum cardTypeEnum = currCard.getCardType();
		Date dateNow = new Date();
		CustomerMember member = customerMemberDao.findOneByHql(
				"where valid=? and customer=? order by endTime desc",
				new Object[] { true, customer });
		if (member == null || member.getEndTime().before(dateNow)) {
			member = new CustomerMember();
			member.setCustomer(customer);
			member.setCreateTime(dateNow);
			member.setStartTime(dateNow);
			Calendar endTime = Calendar.getInstance();
			endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
			member.setEndTime(endTime.getTime());
			member.setValid(true);
			customerMemberDao.save(member);
		} else {
			Calendar endTime = Calendar.getInstance();
			endTime.setTime(member.getEndTime());
			member = new CustomerMember();
			member.setCustomer(customer);
			member.setCreateTime(dateNow);
			member.setStartTime(dateNow);
			endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
			member.setEndTime(endTime.getTime());
			member.setValid(true);
			customerMemberDao.save(member);
		}
		ret.put("memberType", MemberTypeEnum.MEMBER.getValue());
		String endTime = DateFormatter.date2String(member.getEndTime());
		ret.put("endTime", endTime);
		ret.put("cardType", cardTypeEnum.getValue());
		currCard.setIsUsed(true);
		currCard.setUpdateTime(new Date());
		// 这里用的是个临时办法，暂时无法修改domain所以用user代替customer
		User usr = new User();
		usr.setId(userId);
		currCard.setUpdateBy(usr);
		dmsCardDao.update(currCard);
		RechargeRecordCard rechargeRecordCard = new RechargeRecordCard();
		rechargeRecordCard.setCard(currCard);
		rechargeRecordCard.setCustomer(customer);
		rechargeRecordCard.setCreateTime(dateNow);
		rechargeRecordCard.setTerminalType(terminalType);
		rechargeRecordCard.setTerminalVersion(terminalVersion);
		rechargeRecordCard.setIp(ip);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		rechargeRecordCard.setProvince(a[0]);
		rechargeRecordCard.setCity(a[1]);
		rechargeRecordCardDao.save(rechargeRecordCard);
		ret.put("status", CardValidateEnum.SUCCESS.getValue());
		return ret;
	}

	/*
	 * {"status":0,"receipt":
	 * {"product_id":"point101","original_purchase_date_ms":"1341891813000",
	 * "original_purchase_date"
	 * :"2012-07-10 03:43:33 Etc/GMT","purchase_date_pst"
	 * :"2012-07-09 20:43:33 America/Los_Angeles",
	 * "bvrs":"2.2.2","transaction_id"
	 * :"1000000052511524","original_purchase_date_pst"
	 * :"2012-07-09 20:43:33 America/Los_Angeles",
	 * "original_transaction_id":"1000000052511524","item_id":"540324174",
	 * "purchase_date_ms":"1341891813000","quantity":"1",
	 * "purchase_date":"2012-07-10 03:43:33 Etc/GMT", "bid":"cn.ikan.ikanpad"}}
	 */
	/**
	 * 苹果充值返回结果处理
	 * 
	 * @param rechargeCode
	 * @param appleCode
	 */
	public void saveAppleRechargeReturn(int userId, JSONObject receiptObj,
			String receipt_data, String terminalType, String terminalVersion,
			String ip, boolean sandBox) throws Exception {
		// 交易ID
		String originalTransactionId = receiptObj
				.getString("original_transaction_id");
		RechargeRecordApple rechargeRecordApple = rechargeRecordAppleDao
				.findOneByHql(
						"WHERE original_transaction_id=? ORDER BY create_time ",
						new Object[] { originalTransactionId });
		if (rechargeRecordApple != null) {
			// 不是充值
			if (rechargeRecordApple.getRechargeStatus() != RechargeStatusEnum.RECHARGE_SUCCESS) {
				// 验证支付，之前验证失败，这次成功
				rechargeRecordApple
						.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);
			} else {
				return;
			}
		} else {
			// 充值
			// 保存消费记录
			String product_id = receiptObj.getString("product_id");
			// 根据product_id取得产品的详细信息
			MemberCostDefine cost = getMemberCostDefineByProductId(product_id);
			CardTypeEnum cardTypeEnum = cost.getCardType();
			Date dateNow = new Date();
			Customer customer = customerDao.getObject(Customer.class, userId);
			CustomerMember member = customerMemberDao.findOneByHql(
					"where valid=? and customer=? order by endTime desc",
					new Object[] { true, customer });

			Calendar endTime = Calendar.getInstance();
			// 新的会员,或者之前的会员已过期
			if (member == null) {// 新会员
				member = new CustomerMember();
				member.setCustomer(customer);
				member.setCreateTime(dateNow);
				member.setStartTime(dateNow);
				endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
				member.setEndTime(endTime.getTime());
				member.setValid(true);
				customerMemberDao.save(member);
			} else if (member.getEndTime().before(dateNow)) {// 之前是会员，但会员已过期
				member.setStartTime(dateNow);
				endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
				member.setEndTime(endTime.getTime());
				member.setValid(true);
				customerMemberDao.update(member);
			} else {// 之前是会员，但会员未过期
				endTime.setTime(member.getEndTime());
				endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
				member.setEndTime(endTime.getTime());
				member.setValid(true);
				customerMemberDao.update(member);
			}
			if (sandBox)
				return;
			// 交易记录
			rechargeRecordApple = new RechargeRecordApple();
			rechargeRecordApple.setCustomer(customer);
			rechargeRecordApple.setCostId(cost);
			rechargeRecordApple.setRechargeMoney(cost.getPrice());
			rechargeRecordApple.setProductId(cost.getProductId());
			rechargeRecordApple.setReferenceName(cost.getReferenceName());
			rechargeRecordApple.setUnitPrice(cost.getUnitPrice());
			rechargeRecordApple.setProductDesc(cost.getProductDesc());
		}

		rechargeRecordApple.setCreateTime(new Date());
		rechargeRecordApple.setTerminalType(terminalType);
		rechargeRecordApple.setTerminalVersion(terminalVersion);
		rechargeRecordApple.setIp(ip);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		rechargeRecordApple.setProvince(a[0]);
		rechargeRecordApple.setCity(a[1]);
		// 充值交易时间 毫秒
		if (receiptObj.has("original_purchase_date_ms")) {
			rechargeRecordApple.setOriginalPurchaseDateMs(receiptObj
					.getString("original_purchase_date_ms"));
		}
		// 充值交易时间
		if (receiptObj.has("original_purchase_date")) {
			rechargeRecordApple.setOriginalPurchaseDate(receiptObj
					.getString("original_purchase_date"));
		}
		// 充值交易时间美国时间
		if (receiptObj.has("original_purchase_date_pst")) {
			rechargeRecordApple.setOriginalPurchaseDatePst(receiptObj
					.getString("original_purchase_date_pst"));
		}
		// 充值交易ID
		if (receiptObj.has("original_transaction_id")) {
			rechargeRecordApple.setOriginalTransactionId(receiptObj
					.getString("original_transaction_id"));
		}
		rechargeRecordApple
				.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);// 状态为成功
		if (receiptObj.has("purchase_date_pst")) {
			rechargeRecordApple.setPurchaseDatePst(receiptObj
					.getString("purchase_date_pst"));
		}
		if (receiptObj.has("purchase_date_ms")) {
			rechargeRecordApple.setPurchaseDateMs(receiptObj
					.getString("purchase_date_ms"));
		}
		if (receiptObj.has("purchase_date")) {
			rechargeRecordApple.setPurchaseDate(receiptObj
					.getString("purchase_date"));
		}
		//
		if (receiptObj.has("bvrs")) {
			rechargeRecordApple.setBvrs(receiptObj.getString("bvrs"));
		}
		// 查询交易ID
		if (receiptObj.has("transaction_id")) {
			rechargeRecordApple.setTransactionId(receiptObj
					.getString("transaction_id"));
		}
		//
		if (receiptObj.has("item_id")) {
			rechargeRecordApple.setItemId(receiptObj.getString("item_id"));
		}
		//
		if (receiptObj.has("quantity")) {
			rechargeRecordApple.setQuantity(receiptObj.getInt("quantity"));
		}
		//
		if (receiptObj.has("bid")) {
			rechargeRecordApple.setBid(receiptObj.getString("bid"));
		}
		rechargeRecordAppleDao.save(rechargeRecordApple);

	}
	
	/**
	* 功能描述:苹果充值返回结果处理,处理苹果新验证，结构与旧的验证不一样
	* 参数：@param userId
	* 参数：@param receiptObj
	* 参数：@param receipt_data
	* 参数：@param terminalType
	* 参数：@param terminalVersion
	* 参数：@param ip
	* 参数：@param sandBox
	* 参数：@throws Exception
	* 返回类型:void
	 */
	public void saveNewAppleRechargeReturn(int userId, JSONObject receiptObj,
			String receipt_data, String terminalType, String terminalVersion,
			String ip, boolean sandBox,String transactionId) throws Exception {
		//获取当前交易记录
		JSONObject currentTransaction = getCurrentTransaction(receiptObj, transactionId);
		if(currentTransaction == null){
			return;
		}
		// 交易ID
//		String originalTransactionId = receiptObj
//				.getString("original_transaction_id");
		String originalTransactionId = currentTransaction
				.getString("original_transaction_id");
		RechargeRecordApple rechargeRecordApple = rechargeRecordAppleDao
				.findOneByHql(
						"WHERE original_transaction_id=? ORDER BY create_time ",
						new Object[] { originalTransactionId });
		if (rechargeRecordApple != null) {
			// 不是充值
			if (rechargeRecordApple.getRechargeStatus() != RechargeStatusEnum.RECHARGE_SUCCESS) {
				// 验证支付，之前验证失败，这次成功
				rechargeRecordApple
						.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);
			} else {
				return;
			}
		} else {
			// 充值
			// 保存消费记录
			String product_id = currentTransaction.getString("product_id");
			// 根据product_id取得产品的详细信息
			MemberCostDefine cost = getMemberCostDefineByProductId(product_id);
			CardTypeEnum cardTypeEnum = cost.getCardType();
			Date dateNow = new Date();
			Customer customer = customerDao.getObject(Customer.class, userId);
			CustomerMember member = customerMemberDao.findOneByHql(
					"where valid=? and customer=? order by endTime desc",
					new Object[] { true, customer });

			Calendar endTime = Calendar.getInstance();
			// 新的会员,或者之前的会员已过期
			if (member == null) {// 新会员
				member = new CustomerMember();
				member.setCustomer(customer);
				member.setCreateTime(dateNow);
				member.setStartTime(dateNow);
				endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
				member.setEndTime(endTime.getTime());
				member.setValid(true);
				customerMemberDao.save(member);
			} else if (member.getEndTime().before(dateNow)) {// 之前是会员，但会员已过期
				member.setStartTime(dateNow);
				endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
				member.setEndTime(endTime.getTime());
				member.setValid(true);
				customerMemberDao.update(member);
			} else {// 之前是会员，但会员未过期
				endTime.setTime(member.getEndTime());
				endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
				member.setEndTime(endTime.getTime());
				member.setValid(true);
				customerMemberDao.update(member);
			}
			if (sandBox)
				return;
			// 交易记录
			rechargeRecordApple = new RechargeRecordApple();
			rechargeRecordApple.setCustomer(customer);
			rechargeRecordApple.setCostId(cost);
			rechargeRecordApple.setRechargeMoney(cost.getPrice());
			rechargeRecordApple.setProductId(cost.getProductId());
			rechargeRecordApple.setReferenceName(cost.getReferenceName());
			rechargeRecordApple.setUnitPrice(cost.getUnitPrice());
			rechargeRecordApple.setProductDesc(cost.getProductDesc());
		}

		rechargeRecordApple.setCreateTime(new Date());
		rechargeRecordApple.setTerminalType(terminalType);
		rechargeRecordApple.setTerminalVersion(terminalVersion);
		rechargeRecordApple.setIp(ip);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		rechargeRecordApple.setProvince(a[0]);
		rechargeRecordApple.setCity(a[1]);
		// 充值交易时间 毫秒
		if (currentTransaction.has("original_purchase_date_ms")) {
			rechargeRecordApple.setOriginalPurchaseDateMs(currentTransaction
					.getString("original_purchase_date_ms"));
		}
		// 充值交易时间
		if (currentTransaction.has("original_purchase_date")) {
			rechargeRecordApple.setOriginalPurchaseDate(currentTransaction
					.getString("original_purchase_date"));
		}
		// 充值交易时间美国时间
		if (currentTransaction.has("original_purchase_date_pst")) {
			rechargeRecordApple.setOriginalPurchaseDatePst(currentTransaction
					.getString("original_purchase_date_pst"));
		}
		// 充值交易ID
		if (currentTransaction.has("original_transaction_id")) {
			rechargeRecordApple.setOriginalTransactionId(currentTransaction
					.getString("original_transaction_id"));
		}
		rechargeRecordApple
				.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);// 状态为成功
		if (currentTransaction.has("purchase_date_pst")) {
			rechargeRecordApple.setPurchaseDatePst(currentTransaction
					.getString("purchase_date_pst"));
		}
		if (currentTransaction.has("purchase_date_ms")) {
			rechargeRecordApple.setPurchaseDateMs(currentTransaction
					.getString("purchase_date_ms"));
		}
		if (currentTransaction.has("purchase_date")) {
			rechargeRecordApple.setPurchaseDate(currentTransaction
					.getString("purchase_date"));
		}
		//
		if (receiptObj.has("bvrs")) {
			rechargeRecordApple.setBvrs(receiptObj.getString("bvrs"));
		}
		// 查询交易ID
		if (currentTransaction.has("transaction_id")) {
			rechargeRecordApple.setTransactionId(currentTransaction
					.getString("transaction_id"));
		}
		//
		if (currentTransaction.has("web_order_line_item_id")) {
			rechargeRecordApple.setItemId(currentTransaction.getString("web_order_line_item_id"));
		}
		//
		if (currentTransaction.has("quantity")) {
			rechargeRecordApple.setQuantity(currentTransaction.getInt("quantity"));
		}
		//
		if (receiptObj.has("bid")) {
			rechargeRecordApple.setBid(receiptObj.getString("bid"));
		}
		rechargeRecordAppleDao.save(rechargeRecordApple);

	}
	
	/**
	* 功能描述:获取当前交易
	* 参数：@param receiptObj
	* 参数：@return
	* 返回类型:JSONObject
	 * @throws JSONException 
	 */
	private JSONObject getCurrentTransaction(JSONObject receiptObj,String transactionId) throws JSONException{
		JSONObject ret = null;
		if(!receiptObj.has("in_app")){
			return null;
		}
		JSONArray appArray = receiptObj.getJSONArray("in_app");
		if(appArray == null || appArray.length() == 0){
			return null;
		}
		
		if(StringUtil.isNullOrEmpty(transactionId)){
			return appArray.getJSONObject(appArray.length() - 1);
		}
		
		for (int i = 0; i <appArray.length(); i++) {
			JSONObject transaction = appArray.getJSONObject(i);
			String transId = transaction.optString("transaction_id");
			if(transId.equals(transactionId)){
				return transaction;
			}
		}
		return appArray.getJSONObject(appArray.length() - 1);
	}
	
	/**
	* <p>功能描述:获取vip结果时间</p>
	* <p>参数：@param userId
	* <p>参数：@param product_id
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 * @throws Exception 
	 */
	public String getVipEndTime(Integer userId,String product_id) throws Exception{
		// 保存消费记录
		if(StringUtil.isNullOrEmpty(product_id)){
			return "";
		}
		// 根据product_id取得产品的详细信息
		//未登录情况，计算当前日期加上充值的月数
		if(userId == null || userId == 0){
			// 根据product_id取得产品的详细信息
			MemberCostDefine cost = getMemberCostDefineByProductId(product_id);
			CardTypeEnum cardTypeEnum = cost.getCardType();
			Calendar endTime = Calendar.getInstance();
			endTime.add(Calendar.MONTH, cardTypeEnum.getValue());
			return DateFormatter.date2String(endTime.getTime(), "yyyy-MM-dd HH:mm:ss");
		}else{//登录后
			Customer customer = customerDao.getObject(Customer.class, userId);
			CustomerMember member = customerMemberDao.findOneByHql(
					"where valid=? and customer=? order by endTime desc",
					new Object[] { true, customer });
			if(member != null){
				return DateFormatter.date2String(member.getEndTime(), "yyyy-MM-dd HH:mm:ss");
			}
		}
		return "";
	}
	
	/**
	 * 获取会员卡价格定义
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONArray getMemberCostDefines(String platform) throws Exception {
		JSONArray array = new JSONArray();
		List<MemberCostDefine> memberCostDefines = memberCostDefineDao
				.findAllByHql(
						"where sale_status = 1 and terminal_type = ? order by price",
						new Object[] { platform });
		for (MemberCostDefine memberCostDefine : memberCostDefines) {
			JSONObject obj = new JSONObject();
			obj.put("costId", memberCostDefine.getCostId());
			obj.put("price", memberCostDefine.getPrice());
			obj.put("cardType", memberCostDefine.getCardType().getValue());
			obj.put("cardTypeDes", memberCostDefine.getCardType()
					.getDescription());
			obj.put("unitPrice", memberCostDefine.getUnitPrice());
			obj.put("productId", memberCostDefine.getProductId());
			obj.put("referenceName", memberCostDefine.getReferenceName());
			obj.put("saleStatus", memberCostDefine.getSaleStatus());
			obj.put("productDesc", memberCostDefine.getProductDesc());
			array.put(obj);
		}
		return array;
	}

	/**
	 * 会员剧集列表
	 * 
	 * @param userId
	 * @param albumid
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public JSONArray getMemberVideos(int userId, int albumid,
			String terminalType, String terminalVersion, int start, int limit)
			throws Exception {
		JSONArray array = new JSONArray();
		List<Video> videos = this.getVideoList(albumid, start, limit);
		// if(terminalType.equals("gpadtv")){
		// return array;
		// }
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		// String inReviewPlatform =
		// spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		boolean _isInReview = false;
		String url_m3u8 = "http://videoa.ikan.cn/";
		String my_host = "http://114.112.50.220/";
		if (terminalType.equals("iphone")) {
			isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			// inReviewPlatform =
			// spi.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
			inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
			if (StringUtil.isNotNullNotEmpty(isInReview)) {
				_isInReview = "true".equalsIgnoreCase(isInReview.trim())
						&& inReviewVersion.equals(terminalVersion) ? true
						: false;
			}
		}
		if (terminalType.equals("ipad")) {
			if (StringUtil.isNotNullNotEmpty(isInReview)) {
				_isInReview = "true".equalsIgnoreCase(isInReview.trim())
						&& inReviewVersion.equals(terminalVersion) ? true
						: false;
			}
		}

		for (int i = 0; i < videos.size(); i++) {
			Video video = videos.get(i);
			JSONObject obj = new JSONObject();
			obj.put("vid", video.getId());
			obj.put("name", video.getName() == null ? "" : video.getName());
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig()
					.getImgServerUrl()
					+ video.getCover());
			// obj.put("startLevel", video.getScore() == null ? 0 :
			// video.getScore());
			// obj.put("summary", video.getDescription() == null ? "" :
			// video.getDescription());
			obj.put("time", video.getTime());
			// obj.put("starring", video.getStarring() == null ? "" :
			// video.getStarring());
			// obj.put("director", video.getDirector() == null ? "" :
			// video.getDirector());
			obj.put("episode",
					video.getEpisode() == null ? 0 : video.getEpisode());
			// obj.put("years",
			// DateFormatter.date2YearString(video.getYears()));
			// obj.put("submitTime",
			// DateTimeFormatter.dateTime2String(video.getCreteTime()));
			obj.put("isFree", Boolean.TRUE.equals(video.getFree()));
			String saveDir = video.getSaveDir() == null ? "" : video
					.getSaveDir();
			saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
			// obj.put("v720", url_mp4 + saveDir + video.getNumber() +
			// "-720p.mp4");
			obj.put("v720",
					URLParse.makeURL(saveDir + video.getNumber() + "-720p.mp4"));
			obj.put("v720-ios", url_m3u8 + saveDir + video.getNumber()
					+ "-720p/index.m3u8");
			obj.put("audio-ios", url_m3u8 + saveDir + video.getNumber()
					+ "-audio/index.m3u8");
			// obj.put("index-ios", url_m3u8 + saveDir + video.getNumber() +
			// ".m3u8");
			if (_isInReview) {// 如果在审核中
				obj.put("main-ios", my_host + saveDir + video.getNumber()
						+ ".m3u8");
			} else
				obj.put("main-ios", obj.get("v720-ios"));
			array.put(obj);
		}
		return array;
	}

	/**
	 * 获得剧集列表
	 * 
	 * @param albumid
	 * @return
	 */
	public List<Video> getVideoList(int albumid, int start, int limit)
			throws Exception {
		String SQL = "select v.* from ytsp_video v,ytsp_video_album va,ytsp_album a where a.id=va.aid and "
				+ " va.vid=v.id and a.id=";
		Album album = albumDao.findById(albumid);
		// List<Object> params = new ArrayList<Object>(2);
		if (albumid > 0) {
			// SQL =
			// " ( SELECT COUNT(*) FROM v.albums va WHERE va.id = ? AND va.review = ? )>0";
			// params.add(albumid);
			// params.add(ReviewStatusEnum.PASS);
			SQL += albumid;
		}else{//3.7版本调用些方法传albumid为空。所以这里特殊处理
			SQL += 0;
		}
		// HQL +=
		// "v.review=? AND v.status=? AND (   SELECT COUNT(*) FROM v.videoEncodeEntry vee WHERE vee.status = ?"
		// + " )>=1";
		SQL += " and v.review=1 AND v.status=1 ";
		// params.add(ReviewStatusEnum.PASS);
		// params.add(ValidStatusEnum.VALID);
		// params.add(VideoStatusEnum.SYNCHRONIZE_COMPLETE);

		// 如果是栏目的话，则按照集数倒序
		if (album != null
				&& (album.getType() == AlbumTypeEnum.PROGRAM || album.getType() == AlbumTypeEnum.PERIODICALS)) {
			SQL += " ORDER BY v.free desc , v.episode DESC";
		} else {
			SQL += " ORDER BY v.episode ASC";
		}
		List<Video> videos = videoDao.sqlFetch(SQL, Video.class, start, limit);
		return videos;
	}

	/**
	 * 会员播放视频
	 * 
	 * @param userId
	 * @param videoId
	 * @param albumId
	 * @param terminalType
	 * @param terminalVersion
	 * @param terminalNumber
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public JSONObject savePlayVideo(final int userId, final int videoId,
			int albumId, final String terminalType,
			final String terminalVersion, final String terminalNumber,
			final String ip) throws Exception {

		final JSONObject json = new JSONObject();
		// 判断视频是否存在
		final Video video = videoDao.findById(videoId);
		if (video == null) {
			json.put("result", MemberVideoStatusEnum.NO_VIDEO.getValue()); // 视频不存在
			return json;
		}

		int _version = VersionCommand.convert2Num(terminalVersion);
		if (_version < 400000) { // 视频不存在，屏蔽4.0版本以下的
			json.put("result", MemberVideoStatusEnum.NO_VIDEO.getValue());
			return json;
		}
		final Album album = albumDao.findById(albumId);

		if (Boolean.TRUE.equals(album.getVip())) {
			boolean isFree = video.getFree() == null ? true : video.getFree();
			if (!isFree) {
				// 先判断用户是否登录
				if (userId == 0) {
					// 未登录的时候，提示登录。退出
					json.put("result",
							MemberVideoStatusEnum.NEED_LOGIN.getValue()); // 需要先登录
					return json;
				}
				if (memberType(userId) != MemberTypeEnum.MEMBER) {
					json.put("result",
							MemberVideoStatusEnum.NEED_MEMBER.getValue()); // 需要会员
					return json;
				}
			}
		}
		final String url_m3u8 = "http://videoa.ikan.cn/";
		String my_host = "http://114.112.50.220/";
		String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
		saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
		// json.put("v720", url_mp4 + saveDir + video.getNumber() +
		// "-720p.mp4");
//		json.put("v720",
//				URLParse.makeURL(saveDir + video.getNumber() + "-720p.mp4"));
		json.put(
				"v720",
				URLParse.makeHttpsURLByVersion(saveDir + video.getNumber()
						+ "-720p.mp4", terminalVersion, terminalType));
		json.put("v720-ios", url_m3u8 + saveDir + video.getNumber()
				+ "-720p/index.m3u8");
		json.put("audio-ios", url_m3u8 + saveDir + video.getNumber()
				+ "-audio/index.m3u8");
		json.put("index-ios", url_m3u8 + saveDir + video.getNumber() + ".m3u8");

		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		if (terminalType.equals("iphone")) {
			isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			inReviewPlatform = spi
					.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
			inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
		}
		boolean _isInReview = false;
		if (StringUtil.isNotNullNotEmpty(isInReview)) {
			_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true
					: false;
		}
		if (_isInReview) {
			// 如果在审核中
			if (inReviewPlatform.equals(terminalType)
					&& inReviewVersion.equals(terminalVersion))
				json.put("main-ios", my_host + saveDir + video.getNumber()
						+ ".m3u8");
			else
				json.put("main-ios", json.get("v720-ios"));
		} else
			json.put("main-ios", json.get("v720-ios"));
		// 记录用户看过的视频
		final LogVideo log = new LogVideo();
		logVideoDao.save(log);
		LowPriorityExecutor.execLog(new Runnable() {
			@Override
			public void run() {
				try {
					log.setAlbumId(album.getId());
					log.setVideoId(videoId);
					log.setEpisode(video.getEpisode());
					log.setTerminalNumber(terminalNumber);
					log.setTerminalType(terminalType);
					log.setTerminalVersion(terminalVersion);
					log.setType(PlayVideoEnum.PLAY_ONLINE);
					// log.setVideoServer(url_m3u8);
					if ("ipad".equals(terminalType)
							|| "iphone".equals(terminalType))
						log.setVideoUrl(json.getString("main-ios"));
					else
						log.setVideoUrl(json.getString("v720"));
					log.setIp(ip);
					String[] a = IPSeeker.getAreaNameByIp(ip);
					log.setProvince(a[0]);
					log.setCity(a[1]);
					log.setTime(new Date());
					log.setCustomerId(userId);
					logVideoDao.saveOrUpdate(log);
					int count = album.getPlayCount() == null ? 0 : album
							.getPlayCount();
					album.setPlayCount(count + 1); // 专辑播放次数加1
					albumDao.update(album);
				} catch (Exception e) {
					logger.error("Save video play log error", e);
				}
			}
		});
		json.put("logid", log.getId()); // 返回播放时长用
		json.put("result", MemberVideoStatusEnum.SUCCESS.getValue());
		return json;
	}

	/**
	 * 会员下载视频
	 * 
	 * @param userId
	 * @param videoId
	 * @param albumId
	 * @return
	 * @throws Exception
	 */
	public JSONObject saveDownloadVideo(String version, int userId,
			int videoId, int albumId) throws Exception {
		JSONObject json = new JSONObject();
		Video video = videoDao.findById(videoId);

		if (video == null) {
			json.put("result", MemberVideoStatusEnum.NO_VIDEO.getValue()); // 视频不存在
			return json;
		}
		int _version = VersionCommand.convert2Num(version);
		if (_version < 400000) {
			json.put("result", MemberVideoStatusEnum.NO_VIDEO.getValue()); // 视频不存在
			return json;
		}
		Album album = albumDao.findById(albumId);
		if (Boolean.TRUE.equals(album.getVip())) {
			boolean isFree = Boolean.TRUE.equals(video.getFree());
			if (!isFree) {
				// 先判断用户是否登录
				if (userId == 0) {
					// 未登录的时候，提示登录。退出
					json.put("result",
							MemberVideoStatusEnum.NEED_LOGIN.getValue()); // 需要先登录
					return json;
				}
				if (memberType(userId) != MemberTypeEnum.MEMBER) {
					json.put("result",
							MemberVideoStatusEnum.NEED_MEMBER.getValue()); // 需要会员
					return json;
				}
			}
		}
		String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
		saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
		json.put("v720",
				URLParse.makeURL(saveDir + video.getNumber() + "-720p.mp4"));
		json.put("result", MemberVideoStatusEnum.SUCCESS.getValue()); // 可以下载
		return json;
	}

	public MemberCostDefine getMemberCostDefineByProductId(String product_id)
			throws Exception {
		MemberCostDefine cost = (MemberCostDefine) memberCostDefineDao
				.findOneByHql(" WHERE productId = ? ",
						new Object[] { product_id });
		return cost;
	}

	// 2.4以前的同名方法，已经从4.4版本修改不再兼容；
	public JSONObject memberCheck(int userId) throws Exception {
		JSONObject json = new JSONObject();
		json.put("memberType", MemberTypeEnum.NOMEMBER.getValue());
		json.put("endTime", "");
		CustomerMember customerMember = customerMemberDao.findOneByHql(
				" WHERE customer.id = ? and endTime>?  order by endTime desc",
				new Object[] { userId, new Date() });
		if (customerMember != null) {
			json.put("memberType", MemberTypeEnum.MEMBER.getValue());
			String endTime = DateFormatter.date2String(customerMember
					.getEndTime());
			json.put("endTime", endTime);
		}
		return json;
	}

	public JSONObject memberCheck_v2_5(int userId) throws Exception {
		JSONObject json = new JSONObject();
		Customer customer = customerDao.findById(userId);
		json.put("memberType", MemberTypeEnum.NOMEMBER.getValue());
		if (customer == null)
			return json;
		CustomerMember customerMember = customerMemberDao.findOneByHql(
				" WHERE customer.id = ? and endTime>?  order by endTime desc",
				new Object[] { userId, new Date() });
		if (customerMember != null) {
			json.put("memberType", MemberTypeEnum.MEMBER.getValue());
			String endTime = DateFormatter.date2String(customerMember
					.getEndTime());
			String vipEndTime = DateFormatter.date2String(customerMember
					.getEndTime(),"yyyy-MM-dd kk:mm:ss");
			json.put("endTime", endTime);
			json.put("vipEndTime", vipEndTime);
		}
		json.put("credits",
				customer.getCredits() == null || customer.getCredits() <= 0 ? 0 : customer.getCredits());
		return json;
	}

	private MemberTypeEnum memberType(int userId) throws Exception {
		CustomerMember customerMember = customerMemberDao.findOneByHql(
				" WHERE customer.id = ? order by endTime desc",
				new Object[] { userId });
		if (customerMember != null) {
			if (customerMember.getValid()) {
				Date endTime = customerMember.getEndTime();
				if (System.currentTimeMillis() < endTime.getTime()) {
					return MemberTypeEnum.MEMBER;
				}
			}
		}
		return MemberTypeEnum.NOMEMBER;
	}

	public List<DmsCard> getByCodeAndPwd(String card_code) throws Exception {
		List<DmsCard> cost = (List<DmsCard>) dmsCardDao.findAllByHql(
				" WHERE cardCode = ? ", new Object[] { card_code });
		return cost;
	}
	
	/**
	* <p>功能描述:校验同一批次充值卡只能兑换一次</p>
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean isCardChargedByBatch(int userId,String batch){
		int count = 0;
		StringBuffer sql = new StringBuffer();
		sql.append("select ci.* from dms_card_info ci,recharge_record_card rc where ci.card_id = rc.card_id and rc.customer = "
				+ userId + " and ci.batch = '" + batch + "'");
		List<DmsCard> list = dmsCardDao.sqlFetch(sql.toString(), DmsCard.class, 0, 2);
		if(list != null && list.size() > 0){
			count = list.size();
		}
		return count > 0 ? true : false;
	}
	
	public RechargeRecordCardDao getRechargeRecordCardDao() {
		return rechargeRecordCardDao;
	}

	public void setRechargeRecordCardDao(
			RechargeRecordCardDao rechargeRecordCardDao) {
		this.rechargeRecordCardDao = rechargeRecordCardDao;
	}

	public RechargeRecordAppleDao getRechargeRecordAppleDao() {
		return rechargeRecordAppleDao;
	}

	public void setRechargeRecordAppleDao(
			RechargeRecordAppleDao rechargeRecordAppleDao) {
		this.rechargeRecordAppleDao = rechargeRecordAppleDao;
	}

	public MemberCostDefineDao getMemberCostDefineDao() {
		return memberCostDefineDao;
	}

	public void setMemberCostDefineDao(MemberCostDefineDao memberCostDefineDao) {
		this.memberCostDefineDao = memberCostDefineDao;
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public CustomerMemberDao getCustomerMemberDao() {
		return customerMemberDao;
	}

	public void setCustomerMemberDao(CustomerMemberDao customerMemberDao) {
		this.customerMemberDao = customerMemberDao;
	}

	public DmsCardDao getDmsCardDao() {
		return dmsCardDao;
	}

	public void setDmsCardDao(DmsCardDao dmsCardDao) {
		this.dmsCardDao = dmsCardDao;
	}

	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	public VideoDao getVideoDao() {
		return videoDao;
	}

	public void setVideoDao(VideoDao videoDao) {
		this.videoDao = videoDao;
	}

	public LogVideoDao getLogVideoDao() {
		return logVideoDao;
	}

	public void setLogVideoDao(LogVideoDao logVideoDao) {
		this.logVideoDao = logVideoDao;
	}

}
