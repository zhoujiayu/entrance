package com.ytsp.entrance.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.dongman.util.UtilDate;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerLoginRecordDao;
import com.ytsp.db.dao.DmsCardDao;
import com.ytsp.db.dao.FeedbackRecordDao;
import com.ytsp.db.dao.LogVideoDao;
import com.ytsp.db.dao.ParentDao;
import com.ytsp.db.dao.PointConsumeRecordDao;
import com.ytsp.db.dao.PointCostDefineDao;
import com.ytsp.db.dao.PointCustomerVideoDao;
import com.ytsp.db.dao.PointPresentDefineDao;
import com.ytsp.db.dao.PointRechargeRecordDao;
import com.ytsp.db.dao.VideoDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerLoginRecord;
import com.ytsp.db.domain.DmsCard;
import com.ytsp.db.domain.FeedbackRecord;
import com.ytsp.db.domain.LogVideo;
import com.ytsp.db.domain.Parent;
import com.ytsp.db.domain.PointConsumeRecord;
import com.ytsp.db.domain.PointCostDefine;
import com.ytsp.db.domain.PointCustomerVideo;
import com.ytsp.db.domain.PointRechargeRecord;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.AlbumTypeEnum;
import com.ytsp.db.enums.CardValidateEnum;
import com.ytsp.db.enums.ConsumeStatusEnum;
import com.ytsp.db.enums.PlayVideoEnum;
import com.ytsp.db.enums.PointOperationTypeEnum;
import com.ytsp.db.enums.RechargeStatusEnum;
import com.ytsp.db.enums.RechargeTypeEnum;
import com.ytsp.db.enums.ReviewStatusEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.enums.VideoConsumeStatusEnum;
import com.ytsp.db.enums.VideoStatusEnum;
import com.ytsp.db.vo.PointRechargeConsumeRecord;
import com.ytsp.entrance.command.base.Command;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.DateTimeFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.MD5;

public class PointService {

	private FeedbackRecordDao feedbackRecordDao;
	private PointConsumeRecordDao pointConsumeRecordDao;
	private PointCostDefineDao pointCostDefineDao;
	private PointCustomerVideoDao pointCustomerVideoDao;
	private PointPresentDefineDao pointPresentDefineDao;
	private PointRechargeRecordDao pointRechargeRecordDao;
	private CustomerDao customerDao;
	private VideoDao videoDao;
	private AlbumDao albumDao;
	private ParentDao parentDao;
	private DmsCardDao dmsCardDao;
	private LogVideoDao logVideoDao;
	
	/**
	 * 保存用户反馈信息
	 * @param customer
	 * @param content
	 * @param mobile
	 * @param email
	 * @throws Exception
	 */
	public void insertFeedback(Customer customer,String content,String mobile,String email ) throws Exception
	{
		
		if(content != null && !content.isEmpty())
		{
			FeedbackRecord obj = new FeedbackRecord();
			obj.setCustomer(customer);
			obj.setContent(content);
			obj.setMobile(mobile);
			obj.setEmail(email);
			obj.setCreateTime(new Date());
			feedbackRecordDao.save(obj);
		}
	}
	
	public JSONArray getAlbumVideoArray(int userId,int albumid, int start, int limit) throws Exception {
		String HQL = "";
		
		Album album = albumDao.findById(albumid);

		List<Object> params = new ArrayList<Object>(2);
		if (albumid > 0) {
			HQL = " ( SELECT COUNT(*) FROM v.albums va WHERE va.id = ? AND va.review = ? )>0";
			params.add(albumid);
			params.add(ReviewStatusEnum.PASS);
		}

		if (StringUtil.isNullOrEmpty(HQL)) {
			HQL = " v WHERE ";
			
		} else {
			HQL = " v WHERE " + HQL + " AND ";
		}
		
		HQL += "v.review=? AND v.status=? AND ( " + " SELECT COUNT(*) FROM v.videoEncodeEntry vee WHERE vee.status = ?" + " )>=1";
		params.add(ReviewStatusEnum.PASS);
		params.add(ValidStatusEnum.VALID);
		params.add(VideoStatusEnum.SYNCHRONIZE_COMPLETE);

		//如果是栏目的话，则按照集数倒序
		if(album != null && album.getType() == AlbumTypeEnum.PROGRAM)
		{
			HQL += " ORDER BY v.episode DESC";
		}
		else
		{
			HQL += " ORDER BY v.episode ASC";
		}

		List<Video> videos = videoDao.findAllByHql(HQL, start, limit, params.toArray());
		JSONArray array = new JSONArray();
		for (Video video : videos) {
			JSONObject obj = new JSONObject();
			obj.put("vid", video.getId());
			obj.put("name", video.getName() == null ? "" : video.getName());
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + video.getCover());
			obj.put("summary", video.getDescription() == null ? "" : video.getDescription());
			obj.put("time", video.getTime());
			obj.put("starring", "");
			obj.put("director", "");
			obj.put("episode", video.getEpisode() == null ? 0 : video.getEpisode());
			obj.put("years", "");
			obj.put("submitTime", DateTimeFormatter.dateTime2String(video.getCreteTime()));
			Integer videoPoint = 0;

			if(videoPoint > 0)
			{
				//判断用户是否已经购买
				if(userId == 0)
				{
					obj.put("status", VideoConsumeStatusEnum.NEED_POINT.getValue());
				}
				else
				{
					PointCustomerVideo pointCustomerVideo = pointCustomerVideoDao.findOneByHql("where customer.id = ? and (videoId = ?  or (albumId = ? and episode = ? ) )",new Object[]{userId,video.getId(),albumid,video.getEpisode()});
					if(pointCustomerVideo == null)
					{
						obj.put("status", VideoConsumeStatusEnum.NEED_POINT.getValue());
					}
					else
					{
						obj.put("status", VideoConsumeStatusEnum.BUYED.getValue());
					}
				}
			}
			else if(videoPoint == 0)
			{
				obj.put("status", VideoConsumeStatusEnum.VIDEO_FREE.getValue());
			}
			obj.put("statusDesc", VideoConsumeStatusEnum.valueOf(obj.getInt("status")).getDescription());
			array.put(obj);
		}
		return array;
	}

	/**
	 * 充值卡充值
	 * @param card_code
	 * @param card_password
	 * @param user_id
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	public CardValidateEnum saveCardRecharge(String rechargeCode,String cardCode, String cardPassword,
			int userId,String terminalType,String terminalVersion,String ip) throws Exception {
			CardValidateEnum trade_status = CardValidateEnum.SUCCESS;
			//验证为客户端发来的请求信息
			List<DmsCard> cards = getByCodeAndPwd(cardCode, cardPassword);
			if(cards == null || cards.size() > 1 || cards.size() == 0)
			{
				trade_status =  CardValidateEnum.CODE_ERROR;
			}
			else
			{
				DmsCard currCard = cards.get(0);
				boolean is_used = currCard.getIsUsed();
				Date live_time = currCard.getLiveTime();//2011-09-07 00:00:00

				if(is_used)
				{
					//此卡已使用
					trade_status = CardValidateEnum.ALREADY_USED;
				}
				else
				{
					if(UtilDate.isOverLiveTime(live_time) <= 0)
					{
						//根据product_id取得产品的详细信息
						PointCostDefine cost = getPointCostDefineByPrice(currCard.getCardPrice().floatValue());
						
						PointRechargeRecord record = new PointRechargeRecord();
						Customer customer = customerDao.getObject(Customer.class, userId);
						record.setCustomer(customer);
						record.setCostId(cost);
						record.setRechargeMoney(cost.getPrice());
						record.setRechargePoint(cost.getPoint());
						record.setProductId(cost.getProductId());
						record.setReferenceName(cost.getReferenceName());
						record.setUnitPrice(cost.getUnitPrice());
						record.setProductDesc(cost.getProductDesc());
						record.setRechargeType(PointOperationTypeEnum.CARD_RECHARGE);
						record.setRechargeMoney(currCard.getCardPrice().floatValue());
						record.setRechargePoint(cost.getPoint());
						record.setCreateTime(new Date());
						record.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);//状态为等待充值
						record.setTerminalType(terminalType);
						record.setTerminalVersion(terminalVersion);
						record.setCardCode(cardCode);
						record.setQuantity(1);
						record.setRechargeCode(rechargeCode);
						record.setIp(ip);
						String[] a = IPSeeker.getAreaNameByIp(ip);
						record.setProvince(a[0]);
						record.setCity(a[1]);
						
						int pointBalance = customer.getConsumerPoints() + cost.getPoint();
						record.setPointBalance(pointBalance);
						
						pointRechargeRecordDao.save(record);
						if(customer.getConsumerPoints() == null)
						{
							customer.setConsumerPoints(0);
						}
						customer.setConsumerPoints(customer.getConsumerPoints()+cost.getPoint());
						customerDao.update(customer);
						
						currCard.setIsUsed(true);
						currCard.setUpdateTime(new Date());
						dmsCardDao.update(currCard);
					}
					else
					{
						//过期
						trade_status =  CardValidateEnum.OVERDUE;
					}
				}
			}
		return trade_status;
	}
	
	/*
	 * {"status":0,"receipt":
	 * {"product_id":"point101","original_purchase_date_ms":"1341891813000",
	 * "original_purchase_date":"2012-07-10 03:43:33 Etc/GMT","purchase_date_pst":"2012-07-09 20:43:33 America/Los_Angeles",
	 * "bvrs":"2.2.2","transaction_id":"1000000052511524","original_purchase_date_pst":"2012-07-09 20:43:33 America/Los_Angeles",
	 * "original_transaction_id":"1000000052511524","item_id":"540324174",
	 * "purchase_date_ms":"1341891813000","quantity":"1",
	 * "purchase_date":"2012-07-10 03:43:33 Etc/GMT",
	 * "bid":"cn.ikan.ikanpad"}}
	 */
	/**
	 * 苹果充值返回结果处理
	 * @param rechargeCode
	 * @param appleCode
	 */
	public void saveAppleRechargeReturn(int userId,JSONObject receiptObj,String receipt_data,String terminalType,String terminalVersion,String ip) throws Exception
	{
		//保存消费记录
		String product_id = receiptObj.getString("product_id");
		//根据product_id取得产品的详细信息
		PointCostDefine cost = getPointCostDefineByProductId(product_id);
		
		
		PointRechargeRecord record = new PointRechargeRecord();
		Customer customer = customerDao.getObject(Customer.class, userId);
		record.setCustomer(customer);
		record.setRechargeType(PointOperationTypeEnum.APP_RECHARGE);
		record.setCostId(cost);
		record.setRechargeCode(receipt_data);
		record.setRechargeMoney(cost.getPrice());
		record.setRechargePoint(cost.getPoint());
		record.setProductId(cost.getProductId());
		record.setReferenceName(cost.getReferenceName());
		record.setUnitPrice(cost.getUnitPrice());
		record.setProductDesc(cost.getProductDesc());
		record.setCreateTime(new Date());
		record.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);//状态为成功
		record.setTerminalType(terminalType);
		record.setTerminalVersion(terminalVersion);
		record.setIp(ip);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		record.setProvince(a[0]);
		record.setCity(a[1]);
		
		if(receiptObj.has("original_purchase_date_ms"))
		{
			record.setOriginalPurchaseDateMs(receiptObj.getString("original_purchase_date_ms"));
		}
		if(receiptObj.has("original_purchase_date"))
		{
			record.setOriginalPurchaseDate(receiptObj.getString("original_purchase_date"));
		}
		if(receiptObj.has("purchase_date_pst"))
		{
			record.setPurchaseDatePst(receiptObj.getString("purchase_date_pst"));
		}
		if(receiptObj.has("bvrs"))
		{
			record.setBvrs(receiptObj.getString("bvrs"));
		}
		if(receiptObj.has("transaction_id"))
		{
			record.setTransactionId(receiptObj.getString("transaction_id"));
		}
		if(receiptObj.has("original_purchase_date_pst"))
		{
			record.setOriginalPurchaseDatePst(receiptObj.getString("original_purchase_date_pst"));
		}
		if(receiptObj.has("original_transaction_id"))
		{
			record.setOriginalTransactionId(receiptObj.getString("original_transaction_id"));
		}
		if(receiptObj.has("item_id"))
		{
			record.setItemId(receiptObj.getString("item_id"));
		}
		if(receiptObj.has("purchase_date_ms"))
		{
			record.setPurchaseDateMs(receiptObj.getString("purchase_date_ms"));
		}
		if(receiptObj.has("quantity"))
		{
			record.setQuantity(receiptObj.getInt("quantity"));
		}
		if(receiptObj.has("purchase_date"))
		{
			record.setPurchaseDate(receiptObj.getString("purchase_date"));
		}
		if(receiptObj.has("bid"))
		{
			record.setBid(receiptObj.getString("bid"));
		}
		
		int pointBalance = customer.getConsumerPoints() + cost.getPoint();
		record.setPointBalance(pointBalance);
		pointRechargeRecordDao.save(record);
		if(customer.getConsumerPoints() == null)
		{
			customer.setConsumerPoints(0);
		}
		customer.setConsumerPoints(customer.getConsumerPoints()+cost.getPoint());
		customerDao.update(customer);
	}

	public List<DmsCard> getByCodeAndPwd(String card_code,String card_password) throws Exception
	{
		List<DmsCard> cost = (List<DmsCard>)dmsCardDao.findAllByHql(" WHERE cardCode = ? and cardPasswordMd5 = ? ",  new Object[]{card_code,card_password});
		return cost;
	}

	public PointCostDefine getPointCostDefineByProductId(String product_id) throws Exception
	{
		PointCostDefine cost = (PointCostDefine)pointCostDefineDao.findOneByHql(" WHERE productId = ? ",  new Object[]{product_id});
		return cost;
	}
	
	
	public PointCostDefine getPointCostDefineByPrice(float price) throws Exception
	{
		PointCostDefine cost = (PointCostDefine)pointCostDefineDao.findOneByHql(" WHERE price = ? ",  new Object[]{price});
		return cost;
	}
	
	
	public boolean isRepeatRechargeByOrderCode(String rechargeCode) throws Exception
	{
		List<PointRechargeRecord> list = (List<PointRechargeRecord>)pointRechargeRecordDao.findAllByHql(" WHERE rechargeCode = ? ",  new Object[]{rechargeCode});
		if(list == null || list.size() == 0)
		{
			return false;
		}
		return true;
	}
	
	public JSONArray queryFeedbacks() throws Exception{
		List<FeedbackRecord> ts = feedbackRecordDao.loadAllObject(FeedbackRecord.class);
		
		JSONArray array = new JSONArray();
		for(FeedbackRecord e : ts)
		{
			JSONObject obj = new JSONObject();
			obj.put("content", e.getContent());
			obj.put("customer", e.getCustomer().getAccount());
			obj.put("mobile", e.getMobile());
			obj.put("email", e.getEmail());
			array.put(obj);
		}
		return array;
	}
	
	
	/**
	 * 保存剧集消费
	 * @param userId
	 * @param consumeCode
	 * @param videoId
	 * @param albumId
	 * @throws Exception
	 */
	public JSONObject saveVideoConsume(int userId, int videoId, int albumId, String consumeCode,String terminalType,String terminalVersion,String terminalNumber,String ip) throws Exception{
		
		JSONObject json = new JSONObject();
		//判断参数的正确性
		if(albumId == 0 || videoId == 0 )
		{
			json.put("result", VideoConsumeStatusEnum.PARAM_ERROR.getValue());
			return json;
		}
		//判断视频是否存在
		Video video = videoDao.findById(videoId);
		if (video == null) {
			json.put("result", VideoConsumeStatusEnum.NO_VIDEO.getValue()); //视频不存在
			return json;
		}
		Album album = albumDao.findById(albumId);
		Customer consumeCustomer = null; //用户
		Integer consumerPoints = 0; //用户积分
		PointCustomerVideo pointCustomerVideo; //点卡充值纪录
		
		//如果视频是免费的，则返回播放地址
		Integer videoPoint = 0; //剧集点数
		if(videoPoint != null && videoPoint > 0)
		{
			//收费视频，则判断该视频是否已经购买
			
			//先判断用户是否登录
			if(userId == 0)
			{
				//未登录的时候，提示登录。退出
				json.put("result", VideoConsumeStatusEnum.NEED_POINT.getValue()); //需要先登录
				return json;
			}
			consumeCustomer = customerDao.findById(userId);
			consumerPoints = consumeCustomer.getConsumerPoints();
			pointCustomerVideo = pointCustomerVideoDao.findOneByHql("where customer.id = ? and (videoId = ?  or (albumId = ? and episode = ? ) )",new Object[]{userId,videoId,albumId,video.getEpisode()});
			if(pointCustomerVideo == null)
			{
				//没有购买该视频,则购买
				if(consumerPoints < videoPoint)
				{
					//钱数不足
					json.put("result", VideoConsumeStatusEnum.NO_BALANCE.getValue()); //需要先登录
					return json;
				}
				consumerPoints = consumerPoints - videoPoint;
				consumeCustomer.setConsumerPoints(consumerPoints);
				customerDao.update(consumeCustomer);
				
				Calendar calNow = Calendar.getInstance();
				Date interactTime = calNow.getTime();
				
				pointCustomerVideo = new PointCustomerVideo();
				pointCustomerVideo.setVideoId(videoId);
				pointCustomerVideo.setEpisode(video.getEpisode());
				pointCustomerVideo.setVideoName(video.getName());
				pointCustomerVideo.setAlbumId(album.getId());
				pointCustomerVideo.setAlbumName(album.getName());
				pointCustomerVideo.setCustomer(consumeCustomer);
				pointCustomerVideo.setBuyType(PointOperationTypeEnum.EPISODE);
				pointCustomerVideo.setBuyPoint(videoPoint);
				pointCustomerVideo.setCreateTime(interactTime);
				pointCustomerVideoDao.save(pointCustomerVideo);
				
				PointConsumeRecord pointConsumeRecord = new PointConsumeRecord();
				pointConsumeRecord.setCustomer(consumeCustomer);
				pointConsumeRecord.setConsumeCode(consumeCode);
				pointConsumeRecord.setConsumeType(PointOperationTypeEnum.EPISODE);
				pointConsumeRecord.setBuyId(pointCustomerVideo.getBuyId());
				pointConsumeRecord.setConsumePoint(videoPoint);
				pointConsumeRecord.setConsumeStatus(ConsumeStatusEnum.SUCCESS);
				pointConsumeRecord.setCreateTime(interactTime);
				pointConsumeRecord.setTerminalType(terminalType);
				pointConsumeRecord.setTerminalVersion(terminalVersion);
				pointConsumeRecord.setTerminalNumber(terminalNumber);
				pointConsumeRecord.setPointBalance(consumerPoints);
				pointConsumeRecordDao.save(pointConsumeRecord);
			}
		}
		else
		{
			//免费的
			if(userId > 0)
			{
				consumeCustomer = customerDao.findById(userId);
			}
		}
		
		if(album != null){
			int count = 0;
			count = album.getPlayCount() == null ? 0 : album.getPlayCount();
			album.setPlayCount(count + 1); //专辑播放次数加1
			albumDao.update(album);
		}
		String url_m3u8 = "http://videoa.ikan.cn/";
		String url_mp4 = "http://videob.ikan.cn/";
		
		String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
		saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
		json.put("v720", url_mp4 + saveDir + video.getNumber() + "-720p.mp4");
		json.put("v360",json.get("v720"));
		json.put("v480",json.get("v720"));
		
		json.put("v720-ios", url_m3u8 + saveDir + video.getNumber() + "-720p/index.m3u8");
		json.put("v360-ios",json.get("v720-ios"));
		json.put("v480-ios",json.get("v720-ios"));
		json.put("audio-ios", url_m3u8 + saveDir + video.getNumber() + "-audio/index.m3u8");
		json.put("index-ios", url_m3u8 + saveDir + video.getNumber() + ".m3u8");
		
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		if(terminalType.equals("iphone"))
		{
			 isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			 inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
			 inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
		}
		boolean _isInReview = false;
		if(StringUtil.isNotNullNotEmpty(isInReview)){
			_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true : false;
		}
		if(_isInReview)
		{
			//如果在审核中
			if(inReviewPlatform.equals(terminalType) && inReviewVersion.equals(terminalVersion))
			{
				json.put("main-ios", json.get("index-ios"));
			}
			else
			{
				json.put("main-ios", json.get("v720-ios"));
			}
		}
		else
		{
			json.put("main-ios", json.get("v720-ios"));
		}

		//记录用户看过的视频
		LogVideo log = new LogVideo();
		log.setAlbumId(album.getId());
		log.setVideoId(videoId);
		log.setEpisode(video.getEpisode());
		log.setTerminalNumber(terminalNumber);
		log.setTerminalType(terminalType);
		log.setTerminalVersion(terminalVersion);
		log.setType(PlayVideoEnum.PLAY_ONLINE);
		log.setVideoPoint(videoPoint);
		log.setVideoServer(url_m3u8);
		if("ipad".equals(terminalType) || "iphone".equals(terminalType) )
		{
			log.setVideoUrl(json.getString("main-ios"));
		}
		else
		{
			log.setVideoUrl(json.getString("v720"));
		}
		log.setIp(ip);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		log.setProvince(a[0]);
		log.setCity(a[1]);
		log.setTime(new Date());
		log.setCustomerId(userId);
		logVideoDao.save(log);
		json.put("logid", log.getId()); //返回播放时长用
		json.put("result", VideoConsumeStatusEnum.SUCCESS.getValue());
		return json;
	}
	/**
	 * 保存剧集下载消费
	 * @param userId
	 * @param videoId
	 * @param consumeCode
	 * @param terminalType
	 * @param terminalVersion
	 * @return
	 * @throws Exception
	 */
	public JSONObject saveVideoDownloadConsume(int userId, int videoId,int albumId) throws Exception{
		boolean free = false; //是否免费
		Customer consumeCustomer = null; //用户
		Integer consumerPoints = 0; //用户积分
		PointCustomerVideo pointCustomerVideo; //点卡充值纪录
		
		JSONObject json = new JSONObject();
		Video video = videoDao.findById(videoId);

		if (video == null) {
			json.put("result", VideoConsumeStatusEnum.NO_VIDEO.getValue()); //视频不存在
			return json;
		}
		
		Integer videoPoint = 0; //剧集点数
		
		if (userId == 0) {
			if(videoPoint != null && videoPoint != 0) {
				json.put("result", VideoConsumeStatusEnum.NEED_POINT.getValue()); //播放此视频需要点数 请先登陆
				return json;
			} else {
				free = true; //是免费
			}
		} else {
			consumeCustomer = customerDao.findById(userId);
			consumerPoints = consumeCustomer.getConsumerPoints();
			pointCustomerVideo = pointCustomerVideoDao.findOneByHql("where customer.id = ? and (videoId = ?  or (albumId = ? and episode = ? ) )",new Object[]{userId,videoId,albumId,video.getEpisode()});
			if (videoPoint == null || videoPoint == 0) { 
				free = true; //是免费
			} else {
				if(pointCustomerVideo != null) {
					free = true; //是免费
				} else {
					if(consumerPoints < videoPoint) {
						json.put("result", VideoConsumeStatusEnum.NO_BALANCE.getValue()); //消费余额不足
						return json; 
					}
				}
			}
		}
		
		json.put("free", free);
		json.put("result", VideoConsumeStatusEnum.SUCCESS.getValue()); //可以下载
		return json;
	}
	
	public ExecuteResult saveVideoDownload(Command command, int albumid, int videoid, int userId, String consumeCode, boolean free, String ip, String terminalType, String terminalVersion, String terminalNumber) throws Exception {
		Album album = albumDao.findById(albumid);
		if (album == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "专辑不存在！", null, command);
		}
		
		Video video = videoDao.findById(videoid);
		if (video == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "视频不存在！", null, command);
		}
		
		JSONObject json = new JSONObject();
		String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
		saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
		
		String url_mp4 = "http://videob.ikan.cn/";
		
		json.put("v720", url_mp4 + saveDir + video.getNumber() + "-720p.mp4");
		int count = album.getPlayCount() == null ? 0 : album.getPlayCount();
		album.setPlayCount(count + 1);
		albumDao.update(album);
		String snapshot = SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover();
		// 记录用户下载过的视频
		LogVideo log = new LogVideo();
		log.setAlbumId(album.getId());
		log.setVideoId(videoid);
		log.setEpisode(video.getEpisode());
		log.setTerminalNumber(terminalNumber);
		log.setTerminalType(terminalType);
		log.setTerminalVersion(terminalVersion);
		log.setType(PlayVideoEnum.DOWNLOAD);
		log.setVideoServer(url_mp4);
		log.setVideoUrl(json.getString("v720"));
		log.setIp(ip);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		log.setProvince(a[0]);
		log.setCity(a[1]);
		log.setTime(new Date());
		logVideoDao.save(log);

		json.put("snapshot", snapshot);// 封面

		if (!free) {
			Customer consumeCustomer = customerDao.findById(userId);
//			Integer videoPoint = 0; // 剧集点数
			Integer consumerPoints = consumeCustomer.getConsumerPoints();
			consumeCustomer.setConsumerPoints(consumerPoints);
			customerDao.update(consumeCustomer);

			Calendar calNow = Calendar.getInstance();
			Date interactTime = calNow.getTime();
			PointCustomerVideo pointCustomerVideo = new PointCustomerVideo();
			pointCustomerVideo.setVideoId(videoid);
			pointCustomerVideo.setEpisode(video.getEpisode());
			pointCustomerVideo.setVideoName(video.getName());
			pointCustomerVideo.setAlbumId(album.getId());
			pointCustomerVideo.setAlbumName(album.getName());
			pointCustomerVideo.setCustomer(consumeCustomer);
			pointCustomerVideo.setBuyType(PointOperationTypeEnum.EPISODE);
//			pointCustomerVideo.setBuyPoint(videoPoint);
			pointCustomerVideo.setCreateTime(interactTime);
			pointCustomerVideoDao.save(pointCustomerVideo);

			PointConsumeRecord pointConsumeRecord = new PointConsumeRecord();
			pointConsumeRecord.setCustomer(consumeCustomer);
			pointConsumeRecord.setConsumeCode(consumeCode);
			pointConsumeRecord.setConsumeType(PointOperationTypeEnum.EPISODE);
			pointConsumeRecord.setBuyId(pointCustomerVideo.getBuyId());
//			pointConsumeRecord.setConsumePoint(videoPoint);
			pointConsumeRecord.setConsumeStatus(ConsumeStatusEnum.SUCCESS);
			pointConsumeRecord.setCreateTime(interactTime);
			pointConsumeRecord.setTerminalType(terminalType);
			pointConsumeRecord.setTerminalVersion(terminalVersion);
			pointConsumeRecord.setTerminalNumber(terminalNumber);
			pointConsumeRecord.setPointBalance(consumerPoints);
			pointConsumeRecord.setIp(ip);
			pointConsumeRecord.setProvince(a[0]);
			pointConsumeRecord.setCity(a[1]);
			pointConsumeRecordDao.save(pointConsumeRecord);
		}
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "下载影片成功！", json, command);
	}
	
	/**
	 * 保存注册 并赠送点数
	 * @param password
	 * @param account
	 * @param nick
	 * @param ip
	 * @param platform
	 * @param version
	 * @param hardwareId
	 * @param email
	 * @param otherInfo
	 * @param appDiv
	 * @param httpSession
	 * @return
	 * @throws Exception
	 */
	public JSONObject saveRegist(String password,String account,String nick,String ip,String platform,String version,String terminalNumber,String email,String otherInfo,String appDiv,HttpSession httpSession) throws Exception{
		JSONObject json = new JSONObject();
		String md5Pwd = MD5.code(password.trim());

		Customer customer = new Customer();
		customer.setAccount(account);
		customer.setPassword(md5Pwd);
		customer.setNick(nick);
		customer.setCreateTime(new Date());
		customer.setRegisterIp(ip);
		customer.setTerminalType(platform);
		customer.setTerminalVersion(version);
		customer.setTerminalNumber(terminalNumber);
		String[] a = IPSeeker.getAreaNameByIp(ip);
		customer.setRegisterProvince(a[0]);
		customer.setRegisterCity(a[1]);
		customer.setConsumerPoints(customer.getConsumerPoints() + IConstants.REGISTER_POINT);
		customerDao.save(customer);
		
		savePointGiftRecord(customer, PointOperationTypeEnum.REGISTER, IConstants.REGISTER_POINT, platform, version, terminalNumber);
		json.put("isPresent", true);
		json.put("registerPoint", IConstants.REGISTER_POINT);
		
		
		Parent parent = new Parent();
		parent.setCustomer(customer);
		parent.setEmail(email);
		parentDao.saveOrUpdate(parent);
		
		HardwareRegisterService hrs = SystemInitialization.getApplicationContext().getBean(HardwareRegisterService.class);
		hrs.saveByNumber(terminalNumber, otherInfo,platform,version,appDiv,ip);
		
		SessionCustomer sc = new SessionCustomer(customer);
		httpSession.setAttribute(IConstants.SESSION_CUSTOMER, sc);
		LoginService.singleRegister(httpSession, customer);//单例登录

		
		json.put("uid", customer.getId());
		return json;
	}
	
	
	public JSONObject getLoginLog(int userId)  throws Exception{
		Calendar calNow = Calendar.getInstance();
		int year = calNow.get(Calendar.YEAR);
		int month = calNow.get(Calendar.MONTH) + 1;
		int day = calNow.get(Calendar.DAY_OF_MONTH);
		CustomerLoginRecordDao recordDao = SystemInitialization.getApplicationContext().getBean(CustomerLoginRecordDao.class);
		
		List<CustomerLoginRecord> customerLoginRecords = recordDao.findAllByHql("WHERE customer.id = ? AND YEAR(time) = ? AND MONTH(time) = ? AND DAY(time) = ?", new Object[]{userId,year,month,day});
		JSONObject json = new JSONObject();
		if(customerLoginRecords.size() > 0) {
			json.put("isLogin", true);
		} else {
			json.put("isLogin", false);
		}
		return json;
	}
	/**
	 * 微博拉票 赠送点数
	 * @param userId
	 * @param rechargeType
	 * @param rechargePoint
	 * @param terminalType
	 * @param terminalVersion
	 * @throws Exception
	 */
	public JSONObject saveWeicoCanvassing(int userId,RechargeTypeEnum rechargeType,String terminalType,String terminalVersion,String terminalNumber) throws Exception{
		JSONObject json = new JSONObject();
		Calendar calNow = Calendar.getInstance();
		int year = calNow.get(Calendar.YEAR);
		int month = calNow.get(Calendar.MONTH) + 1;
		int day = calNow.get(Calendar.DAY_OF_MONTH);
		List<PointRechargeRecord> pointRechargeRecords = pointRechargeRecordDao.findAllByHql("WHERE customer.id = ? AND rechargeType = ? AND YEAR(createTime) = ? AND MONTH(createTime) = ? AND DAY(createTime) = ?", new Object[]{userId,RechargeTypeEnum.SHARE_WEIBO,year,month,day});
		int limit = 0;
		if (pointRechargeRecords.size() < IConstants.WEIBO_POINT_COUNT) {
			Customer customer = customerDao.getObject(Customer.class, userId);
			customer.setConsumerPoints(customer.getConsumerPoints() + IConstants.WEIBO_POINT);
			customerDao.update(customer);
			savePointGiftRecord(customer, PointOperationTypeEnum.SHARE_WEIBO, IConstants.WEIBO_POINT, terminalType, terminalVersion,terminalNumber);
			json.put("isPresent", true);
		} else {
			limit = 1;
		}
		json.put("limit", limit);
		return json;
	}
	/**
	 * 保存赠送点数纪录
	 * @param customer
	 * @param rechargeType
	 * @param rechargePoint
	 * @param terminalType
	 * @param terminalVersion
	 * @throws Exception
	 */
	public void savePointGiftRecord(Customer customer,PointOperationTypeEnum rechargeType,int rechargePoint,String terminalType,String terminalVersion,String terminalNumber) throws Exception{
		PointRechargeRecord record = new PointRechargeRecord();
		record.setCustomer(customer);
		record.setRechargeType(rechargeType);
		record.setRechargePoint(rechargePoint);
		record.setCreateTime(new Date());
		record.setRechargeStatus(RechargeStatusEnum.RECHARGE_SUCCESS);//状态为成功
		record.setTerminalType(terminalType);
		record.setTerminalVersion(terminalVersion);
		record.setTerminalNumber(terminalNumber);
		record.setPointBalance(customer.getConsumerPoints());
		
		pointRechargeRecordDao.save(record);
	}
	
	/**
	 * 获取消费纪录
	 * @param userId
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public JSONArray getPointConsumeRecord(int userId, int start, int limit) throws Exception {
		JSONArray array = new JSONArray();
		List<PointConsumeRecord> pointConsumeRecords = pointConsumeRecordDao.findAllByHql("where customer.id = ? ORDER BY createTime DESC", start, limit , new Object[]{userId});
		for(PointConsumeRecord pointConsumeRecord : pointConsumeRecords) {
			JSONObject obj = new JSONObject();
			obj.put("consumeType", pointConsumeRecord.getConsumeType().getDescription());
			obj.put("consumePoint", pointConsumeRecord.getConsumePoint());
			obj.put("consumeTime", pointConsumeRecord.getCreateTime());
			array.put(obj);
		}
		return array;
	}
	
	/**
	 * 获取充值纪录
	 * @param userId
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public JSONArray getPointRechargeRecord(int userId, int start, int limit) throws Exception {
		JSONArray array = new JSONArray();
		List<PointRechargeRecord> pointRechargeRecords = pointRechargeRecordDao.findAllByHql("where customer.id = ? ORDER BY createTime DESC", start, limit , new Object[]{userId});
		for(PointRechargeRecord pointRechargeRecord : pointRechargeRecords) {
			JSONObject obj = new JSONObject();
			obj.put("rechargeType", pointRechargeRecord.getRechargeType().getDescription());
			obj.put("rechargeMoney", pointRechargeRecord.getRechargeMoney());
			obj.put("rechargePoint", pointRechargeRecord.getRechargePoint());
			obj.put("rechargeStatus", pointRechargeRecord.getRechargeStatus().getDescription());
			obj.put("rechargeTime", pointRechargeRecord.getCreateTime());
			array.put(obj);
		}
		return array;
	}

	/**
	 * 获取点数价格定义
	 * @return
	 * @throws Exception
	 */
	public JSONArray getPointCostDefines(String platform) throws Exception {
		JSONArray array = new JSONArray();
		List<PointCostDefine> pointCostDefines = pointCostDefineDao.findAllByHql("where sale_status = 1 and terminal_type = ? ORDER BY point",0,6, new Object[]{platform});
		for(PointCostDefine pointCostDefine : pointCostDefines) {
			JSONObject obj = new JSONObject();
			obj.put("costId", pointCostDefine.getCostId());
			obj.put("price", pointCostDefine.getPrice());
			obj.put("point", pointCostDefine.getPoint());
			obj.put("unitPrice", pointCostDefine.getUnitPrice());
			obj.put("productId", pointCostDefine.getProductId());
			obj.put("referenceName", pointCostDefine.getReferenceName());
			obj.put("saleStatus", pointCostDefine.getSaleStatus());
			obj.put("productDesc", pointCostDefine.getProductDesc());
			array.put(obj);
		}
		return array;
	}
	
	/**
	 * 获取充值消费纪录
	 * @param userId
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public JSONArray getPointRechargeConsumeRecord(int userId, int start, int limit) throws Exception {
		JSONArray array = new JSONArray();
		List<PointRechargeConsumeRecord> pointRechargeConsumeRecords = pointRechargeRecordDao.getPointRechargeConsumeRecord(userId,start,limit);
		for(PointRechargeConsumeRecord pointRechargeConsumeRecord : pointRechargeConsumeRecords) {
			JSONObject obj = new JSONObject();
			obj.put("createTime", pointRechargeConsumeRecord.getCreateTime());
			obj.put("recordType", pointRechargeConsumeRecord.getRecordType());
			obj.put("type", pointRechargeConsumeRecord.getType());
			obj.put("money", pointRechargeConsumeRecord.getMoney());
			obj.put("point", pointRechargeConsumeRecord.getPoint());
			obj.put("balance", pointRechargeConsumeRecord.getBalance());
			obj.put("albumName", pointRechargeConsumeRecord.getAlbumName());
			obj.put("videoName", pointRechargeConsumeRecord.getVideoName());
			array.put(obj);
		}
		return array;
	}
	
	/**
	 * 获取用户剩余点数
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public int getPointBalance(int userId) throws Exception {
		Customer customer = customerDao.findById(userId);
		int balance = customer.getConsumerPoints();
		return balance;
	}
	
	public FeedbackRecordDao getFeedbackRecordDao() {
		return feedbackRecordDao;
	}
	public void setFeedbackRecordDao(FeedbackRecordDao feedbackRecordDao) {
		this.feedbackRecordDao = feedbackRecordDao;
	}
	public PointConsumeRecordDao getPointConsumeRecordDao() {
		return pointConsumeRecordDao;
	}
	public void setPointConsumeRecordDao(PointConsumeRecordDao pointConsumeRecordDao) {
		this.pointConsumeRecordDao = pointConsumeRecordDao;
	}
	public PointCostDefineDao getPointCostDefineDao() {
		return pointCostDefineDao;
	}
	public void setPointCostDefineDao(PointCostDefineDao pointCostDefineDao) {
		this.pointCostDefineDao = pointCostDefineDao;
	}
	public PointCustomerVideoDao getPointCustomerVideoDao() {
		return pointCustomerVideoDao;
	}
	public void setPointCustomerVideoDao(PointCustomerVideoDao pointCustomerVideoDao) {
		this.pointCustomerVideoDao = pointCustomerVideoDao;
	}
	public PointPresentDefineDao getPointPresentDefineDao() {
		return pointPresentDefineDao;
	}
	public void setPointPresentDefineDao(PointPresentDefineDao pointPresentDefineDao) {
		this.pointPresentDefineDao = pointPresentDefineDao;
	}
	public PointRechargeRecordDao getPointRechargeRecordDao() {
		return pointRechargeRecordDao;
	}
	public void setPointRechargeRecordDao(
			PointRechargeRecordDao pointRechargeRecordDao) {
		this.pointRechargeRecordDao = pointRechargeRecordDao;
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public VideoDao getVideoDao() {
		return videoDao;
	}

	public void setVideoDao(VideoDao videoDao) {
		this.videoDao = videoDao;
	}

	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	public ParentDao getParentDao() {
		return parentDao;
	}

	public void setParentDao(ParentDao parentDao) {
		this.parentDao = parentDao;
	}

	public DmsCardDao getDmsCardDao() {
		return dmsCardDao;
	}

	public void setDmsCardDao(DmsCardDao dmsCardDao) {
		this.dmsCardDao = dmsCardDao;
	}

	public LogVideoDao getLogVideoDao() {
		return logVideoDao;
	}

	public void setLogVideoDao(LogVideoDao logVideoDao) {
		this.logVideoDao = logVideoDao;
	}
	
}
