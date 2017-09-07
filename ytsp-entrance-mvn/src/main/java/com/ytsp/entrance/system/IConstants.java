package com.ytsp.entrance.system;

/**
 * @author GENE
 * @description 系统常量
 */
public interface IConstants {

	public static final String SESSION_CUSTOMER = "_SessionUser";
	public static final String MOBILE_SESSIONID = "mobile-sessionid";
	public static final String SINGLEENDPOINT_KICKOUT = "_SingleEndPointKickOut";
	public static final String SESSION_Uid = "_SessionUid";
	public static final String SESSION_OP = "_SessionOP";
	public static final String SESSION_Platform = "_SessionPlatform";
	public static final String SESSION_Version = "_SessionVersion";
	public static final String SESSION_UniqueId = "_SessionUniqueId";

	// 系统配置（记录于数据库）
	public static final String PROBATION_KEY = "probation";
	public static final String DISABLE_AD_KEY = "disableAd";
	public static final String ENABLE_VIP = "enableVip";
	public static final String VIDEO_NO_LOGIN = "videoNoLogin";
	public static final String VIP_TIPS = "vipTips";

	/** 系统配置键，对应于ServletContext的attribute */
	public static final String SYSTEM_CONFIG_KEY = "system.config.key";

	/** 系统状态键，对应于ServletContext的attribute */
	public static final String SYSTEM_STATUS_KEY = "system.status.key";

	/** 系统存于数据库中的参数键，对应于ServletContext的attribute */
	public static final String SYSTEM_PARAM_IN_DB_KEY = "system.param_in_db.key";

	/* 宝宝互动扣除的点数 */
	public static final int ACTION_POINT = 1; // 互动消费点数
	public static final int REGISTER_POINT = 100; // 注册赠送
	public static final int LOGIN_POINT = 5; // 登陆赠送
	public static final int LOGIN_POINT_INCREMENTAL = 1; // 连续登陆递增送点数
	public static final int LOGIN_POINT_COUNT = 1; // 登陆，每天1次
	public static final int WEIBO_POINT = 5;// 微薄拉票赠送
	public static final int WEIBO_POINT_COUNT = 2; // 微薄拉票，每天2次

	/* 哪些需要审核时候隐藏 */
	public static final String WEIBO_DESC = "weiboDesc";
	public static final String WEIBO_LOGIN_DESC = "weiboLoginDesc";

	public static final String IS_IN_REVIEW_KEY = "isInReview";
	public static final String IN_REVIEW_PLATFORM = "inReviewPlatform";
	public static final String IN_REVIEW_VERSION = "inReviewVersion";

	public static final String IS_IN_REVIEW_KEY_IPHONE = "isInReviewIPhone";
	public static final String IN_REVIEW_PLATFORM_IPHONE = "inReviewPlatformIPhone";
	public static final String IN_REVIEW_VERSION_IPHONE = "inReviewVersionIPhone";

	public static final String[] GRADE = { "grade_0", "grade_1st", "grade_2ed",
			"grade_3rd", "grade_4th", "grade_5th", "grade_6th" };

	public static final String[] SUBJECT = { "subject_zh", "subject_math",
			"subject_en", "subject_music", "subject_other" };

	// 分享地址，TODO 是否做精准统计（from）？iPad、iPhone、Android、AndroidHD
	public static final String SHAREURL = "http://m.ikan.cn/mobileAppAddress.action?from=14";
	// 商品标签组id
	public static final Integer AGEGROUPID = 4;
	// 动漫年龄taggroupId
	public static final Integer ANIMEAGEGROUPID = 13;
	// 视频地区groupId
	public static final Integer ANIMEAREAGROUPID = 24;
	// 有货groupId
	public static final Integer HAVEGOODSGROUPID = 33;
	
	public static final String VIDEOSAVEPATH480P = "Disk01_6000g/20151124alone/";
	//推荐热搜词配置
	public static final String CONFIG_RECOMMEND_SK = "CONFIG_RECOMMEND_SK";
	//玩具热搜词配置
	public static final String CONFIG_PRODUCT_SK = "CONFIG_PRODUCT_SK";
	//视频热搜词配置
	public static final String CONFIG_ALBUM_SK = "CONFIG_ALBUM_SK";
	//知识热搜词配置
	public static final String CONFIG_KNOWLEDGE_SK = "CONFIG_KNOWLEDGE_SK";
	//首单邮费规则说明
	public static final String SHIPPING_RULE_FIRST_ORDER_DESC = "新用户首单39免邮";
	//非首单邮费规则说明
	public static final String SHIPPING_RULE_NOT_FIRST_DESC = "全场满68免邮";
	//标准邮费
	public static final double SHIPPING = 8d;
	// 首单邮费金额
	public static final double FIRST_ORDER_SHIPPING_PRICE = 39d;
	// 非首单邮费规则说明
	public static final double NOT_FIRST_ORDER_SHIPPING_PRICE = 68d;
	//分享地址页面
	public static final String SHAREPAGE = "http://m.ikan.cn/mobileAppAddress.html";
	//视频审核密钥
	public static final String VIDEOREVIEWKEY = "imagemedia";
	
	//视频审核密钥
	public static final String VIPURL = "ikan://vip/";
	//移动端网站，商品详情下载提示信息
	public static final String DOWNLOADINFO = "CONFIG_DOWNLOAD_REMIND"; 
	
	public static final String QQ_APP_ID = "101171747";
	
	public static final String QQ_GET_USER_INFO = "https://graph.qq.com/user/get_user_info?access_token=ACCESS_TOKEN&oauth_consumer_key=OAUTH_CONSUMER_KEY&openid=OPENID";
	//优惠券不可使用原因
	public static final String COUPONUSEDATEREASON = "不在使用日期范围内";
	//优惠券不在使用范围原因
	public static final String COUPONRANGEREASON = "不可用于购买所选商品";
	//优惠券金额不足原因
	public static final String COUPONMONEYREASON = "差MONEY元可用该券";
	//发送短信时长
	public static final Integer SENDSMSLIMITTIME = 90;
	
	public static final String EMAIL_PERSON = "yushifang@ikan.cn";
	
	//iphone https版本号
//	public static final String IPHONE_HTTPS_VERSION = "5.1.1";
	//ipad https版本号
//	public static final String IPAD_HTTPS_VERSION = "5.0.5";
	
	//iphone https版本号，暂时使用
	public static final String IPHONE_HTTPS_VERSION = "6.1.1";
	//ipad https版本号，暂时使用
	public static final String IPAD_HTTPS_VERSION = "6.0.5";
}
