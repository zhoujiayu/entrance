package com.ytsp.entrance.command.base;

public interface CommandList {

	public static final String HEAD_INFO = "HEAD_INFO";
	public static final String RESPONSE_CODE = "RESPONSE_CODE";
	public static final String RESPONSE_CODE_INFO = "RESPONSE_CODE_INFO";
	public static final String RESPONSE_BODY = "RESPONSE_BODY";

	// LOGIN VERSION 1
	public static final int CMD_REGIST = 101;
	/** @deprecated */
	public static final int CMD_LOGIN = 102;
	/** @deprecated */
	public static final int CMD_LOGOUT = 103;
	public static final int CMD_MODIFY_PWD = 104;
	/** @deprecated */
	public static final int CMD_REGIST_DEVICE = 112;
	public static final int CMD_REGIST_DEVICE_TOKEN = 113;
	public static final int CMD_CUSTOMER_REGIST_DEVICE_TOKEN = 114;
	public static final int CMD_VIDEO_COUNT = 201;
	public static final int CMD_VIDEO_PLAY = 203;
	public static final int CMD_ALBUM_COUNT = 211;
	public static final int CMD_ALBUM_SAVE_SCORE = 214;
	public static final int CMD_ALBUM_TOPLIST_COUNT = 215;
	public static final int CMD_ALBUM_TOPLIST_LIST = 216;
	public static final int CMD_CHANNEL_LIST = 221;
	public static final int CMD_FAVORITES_SAVE = 231;
	public static final int CMD_FAVORITES_LIST = 232;
	public static final int CMD_FAVORITES_DELETE = 233;
	public static final int CMD_FAVORITES_DELETE_ALL = 234;
	public static final int CMD_PARENTCONTROL_LOCALTIME_SAVE = 241;
	public static final int CMD_PARENTCONTROL_ONLINETIME_SAVE = 242;
	public static final int CMD_PARENTCONTROL_LOCALTIME_READ = 243;
	public static final int CMD_PARENTCONTROL_ONLINETIME_READ = 244;
	public static final int CMD_PARENTTIMECONTROL_LIST = 245;
	public static final int CMD_PARENTTIMECONTROL_ADD = 246;
	public static final int CMD_PARENTTIMECONTROL_DELETE = 247;

	// RECOMMEND - ADVERTISEMENT - QUESTIONS - CUSTOMER - PUSHMESSAGE - RECHARGE
	// 3
	public static final int CMD_RECOMMEND_LIST = 301;
	public static final int CMD_ADVERTISEMENT_RANDOM = 321;
	public static final int CMD_ADVERTISEMENT_HIT = 322;
	public static final int CMD_ADVERTISEMENT_DISABLE = 323;
	public static final int CMD_VIP_ENABLE = 324;
	public static final int CMD_SYSTEM_CONFIG = 325;
	public static final int CMD_SYSTEM_CONFIG_RELOAD = 326;
	public static final int CMD_QUESTIONS_RANDOM = 331;
	public static final int CMD_CUSTOMER_READ_PARENT = 341;
	public static final int CMD_CUSTOMER_READ_BABY = 342;
	public static final int CMD_CUSTOMER_SAVE_PARENT = 343;
	public static final int CMD_CUSTOMER_SAVE_BABY = 344;
	public static final int CMD_CUSTOMER_REGIST_PARENT_PWD = 345;
	public static final int CMD_CUSTOMER_MODIFY_PARENT_PWD = 346;
	public static final int CMD_CUSTOMER_PARENT_VERIFY = 347;
	public static final int CMD_CUSTOMER_READ_PARENT_AND_BABY = 348;
	public static final int CMD_CUSTOMER_ACCOUNT_STATUS = 349;
	public static final int CMD_CUSTOMER_PARENT_PWD_STATUS = 352;
	public static final int CMD_RECHARGE_COUNT = 361;
	public static final int CMD_RECHARGE_LIST = 362;
	public static final int CMD_AD_INFO_READ = 370;
	// STAR ACADEMY 6
	public static final int CMD_STAR_ACADEMY_RECOMMENDS = 601;// 推荐广告
	public static final int CMD_STAR_ACADEMY_RECOMMEND_ACTIVITYS = 602;// 热门活动
	public static final int CMD_STAR_ACADEMY_STARS_POPULARITY = 603;// 人气宝宝
	public static final int CMD_STAR_ACADEMY_STARS_NEWEST = 604;// 最新宝宝
	public static final int CMD_STAR_ACADEMY_ACTIVITYS_PEOPLENUMBER = 605;// 人气活动
	public static final int CMD_STAR_ACADEMY_ACTIVITYS_NEWEST = 606;// 最新活动
	public static final int CMD_STAR_ACADEMY_ACTIVTIY = 607;// 活动详情
	public static final int CMD_STAR_ACADEMY_STAR = 608;// 明星档案详情
	public static final int CMD_STAR_ACADEMY_READ_STARINFO = 609;// 明星信息
	public static final int CMD_STAR_ACADEMY_SAVE_STARINFO = 610;// 保存明星信息
	public static final int CMD_STAR_ACADEMY_REGISTRATION_ACTIVTIY = 611;// 活动报名
	public static final int CMD_STAR_ACADEMY_ACTION = 612;// 明星互动

	public static final int CMD_STAR_ACADEMY_ACTIVITYS_SEARCH = 613;// 搜索活动
	public static final int CMD_STAR_ACADEMY_STARS_SEARCH = 614;// 搜索宝宝

	public static final int CMD_STAR_ACADEMY_ACTIVITYS_LIST = 615;// 明星参与的活动
	public static final int CMD_STAR_ACADEMY_ACTIONS_LIST = 616;// 明星参与的互动
	public static final int CMD_STAR_ACADEMY_ACTIVITY_STAR_LIST = 617;// 参与获得的明星
	public static final int CMD_STAR_ACTIVITY_SNAPSHOT_LIST = 618;// 获得活动的照片

	// 点数
	public static final int CMD_POINT_FEEDBACKS = 701;

	public static final int CMD_SAVE_FEEDBACK = 801; // 保存用户反馈
	public static final int CMD_POINT_SAVA_CONSUME_ACTION = 802; // 保存消费 宝宝秀互动
	public static final int CMD_POINT_SAVA_CONSUME_VIDEO = 803; // 保存消费 剧集观看
	public static final int CMD_POINT_SAVA_CONSUME_VIDEO_DOWNLOAD = 804; // 保存消费
																			// 剧集下载
	/** @deprecated */
	public static final int CMD_POINT_REGIST = 805; // 保存消费 注册
	/** @deprecated */
	public static final int CMD_POINT_LOGIN = 806; // 保存消费 登陆
	public static final int CMD_WEICO_CANVASSING = 807; // 微博拉票赠送点数
	public static final int CMD_POINT_CONSUME_RECORD = 808; // 消费纪录
	public static final int CMD_POINT_RECHARGE_RECORD = 809; // 充值纪录
	public static final int CMD_POINT_RECHARGE_CONSUME_RECORD = 810; // 充值消费纪录
	public static final int CMD_POINT_BALANCE = 811; // 点数余额
	public static final int CMD_POINT_COST_DEFINE = 812; // 点数价格定义
	public static final int CMD_POINT_QUERY_CONSUME_CUSTOMER_VIDEO = 813; // 查找用户购买的视频
	public static final int CMD_POINT_SAVA_VIDEO_DOWNLOAD = 814; // 保存消费 剧集下载
	public static final int CMD_POINT_LOGIN_LOG = 815; // 登录纪录

	// 特殊
	public static final int CMD_ALBUM_LIST_THIRDPART = 2121;
	public static final int CMD_ALBUM_THIRDPART = 2131;

	public static final int CMD_VERSION_LAST = 111;
	public static final int CMD_VIDEO_LIST = 202;
	public static final int CMD_VIDEO_PLAY_TIME = 204;
	public static final int CMD_ALBUM_LIST = 212;
	public static final int CMD_ALBUM = 213;
	public static final int CMD_PUSHMESSAGE_LAST = 351;
	public static final int CMD_POINT_VALIDATA_APP_RECGARGE = 820; // 验证苹果支付
	public static final int CMD_POINT_VALIDATA_CARD_RECGARGE = 821; // 验证充值卡

	// 会员
	public static final int CMD_MEMBER_RECHARGE = 901; // 会员充值
	public static final int CMD_MEMBER_VALIDATE_APP_RECHGARGE = 902; // 验证苹果支付
	public static final int CMD_MEMBER_VIDEO_LIST = 903; // 会员剧集列表
	public static final int CMD_MEMBER_VIDEO_PLAY = 904; // 会员播放视频
	public static final int CMD_MEMBER_VIDEO_DOWNLOAD = 905; // 会员下载视频
	public static final int CMD_MEMBER_COST_DEFINE = 906; // 会员卡价格定义
	public static final int CMD_MEMBER_CHECK = 907; // 检查是否是会员
	public static final int CMD_MEMBER_CHECK_V2_5 = 908; // 检查是否是会员

	public static final int RESPONSE_STATUS_OK = 200;
	public static final int RESPONSE_STATUS_FAIL = 500;
	public static final int RESPONSE_STATUS_HEAD_JSON_ERROR = 501;
	public static final int RESPONSE_STATUS_BODY_JSON_ERROR = 502;
	public static final int RESPONSE_STATUS_LOGIN_FAIL = 401;
	public static final int RESPONSE_STATUS_NOPERMISSION = 402;
	public static final int RESPONSE_STATUS_NOLOGIN = 403;
	public static final int RESPONSE_STATUS_UNRECOGNIZED = 404;
	public static final int RESPONSE_STATUS_LOGIN_IN_OTHER_SIDE = 405;
	public static final int RESPONSE_STATUS_NOT_VIP = 406;
	public static final int RESPONSE_STATUS_REGIST_ACCOUNT_EXIST = 503;
	// 密码错误
	public static final int RESPONSE_STATUS_PASSWORD_ERROR = 504;
	// 关联账号信息
	public static final int RESPONSE_STATUS_PWD_ACCOUNT_ERROR = 506;

	// V3新增
	public static final int CMD_FORUM_LIST = 3221;
	public static final int CMD_TAG_ALBUM_LIST = 3901;
	public static final int CMD_TAG_ALBUM_COUNT = 3902;
	public static final int CMD_QUESTIONS_RANDOM_V3 = 3331;
	public static final int CMD_ACTIVITY_LIST = 3903;
	public static final int CMD_ACTIVITY_COUNT = 3904;
	public static final int CMD_RECENTLY_IMAGE = 3905;
	public static final int CMD_ACTIVITY_NOTIFICATION_LIST = 3907;
	public static final int CMD_ALBUM_TOPLIST_LIST_V3 = 3216;
	public static final int CMD_CHECK_VOICE = 3906;
	public static final int CMD_MEMBER_VIDEO_PLAY_V3_1 = 3104;
	public static final int CMD_AD_VIDEO = 3000;
	public static final int CMD_PV_LOG_AD_VIDEO = 3001;
	// 冰球
	public static final int CMD_ICE_HOCKEY_GET_GAMES_BY_CLUB = 1601;
	public static final int CMD_ICE_HOCKEY_GET_GAMES_BY_TEAM = 1602;
	public static final int CMD_ICE_HOCKEY_SAVE_GAME_LOG = 1603;
	public static final int CMD_ICE_HOCKEY_SAVE_GAME_DURATION = 1604;
	public static final int CMD_ICE_HOCKEY_GET_GROUPS = 1605;
	public static final int CMD_ICE_HOCKEY_GET_CLUBS = 1606;
	public static final int CMD_ICE_HOCKEY_IS_PLAYER = 1607;
	public static final int CMD_ICE_HOCKEY_SAVE_PLAYER_REGIST = 1608;
	public static final int CMD_ICE_HOCKEY_SAVE_CUSTOMER_REGIST = 1609;
	public static final int CMD_ICE_HOCKEY_GET_PLAYER_INFO = 1610;
	public static final int CMD_ICE_HOCKEY_EDIT_PLAYER_INFO = 1611;
	public static final int CMD_ICE_HOCKEY_SAVE_PLAYER_DEVICE_TOKEN = 1612;
	public static final int CMD_ICE_HOCKEY_VERSION_LAST = 1613;
	public static final int CMD_ICE_HOCKEY_SYSTEM_CONFIG = 1614;
	public static final int CMD_ICE_HOCKEY_MEMBER_VALIDATE_APP_RECHGARGE = 1615;
	public static final int CMD_ICE_HOCKEY_MEMBER_COST_DEFINE = 1616;
	public static final int CMD_ICE_HOCKEY_GET_PLAYER_BY_CUSTOMER = 1617;
	public static final int CMD_ICE_HOCKEY_IS_MEMBER = 1618;
	public static final int CMD_ICE_HOCKEY_ADD_REDIRECT_URL = 1619;
	// 首页大图、推送消息 pv
	public static final int CMD_LOG_PV_PUSH_MSG = 1620;
	public static final int CMD_LOG_PV_RECOMMENT_IMG = 1621;

	public static final int SYSTEM_MONITOR = 100000;

	// 4.0新增
	public static final int CMD_RECOMMEND_LIST_V4 = 4001;
	public static final int CMD_TOP_ALBUM = 4002;
	public static final int CMD_COUNT_TOP_ALBUM = 4012;
	public static final int CMD_TOP_EB_PRODUCT = 4003;
	public static final int CMD_COUNT_TOP_EB_PRODUCT = 4013;
	public static final int CMD_TOP_ACTIVITY = 4004;
	public static final int CMD_COUNT_TOP_ACTIVITY = 4014;
	public static final int CMD_GETACTIVITYBYID = 4017;
	// 电商平台 -- start
	// //购物车相关
	public static final int CMD_EB_SHOPPINGCART_ADD = 6000;
	public static final int CMD_EB_SHOPPINGCART_DELETE = 6001;
	public static final int CMD_EB_SHOPPINGCART_GET = 6002;
	// 订单相关
	public static final int CMD_EB_ORDER_ADD = 4016;
	public static final int CMD_EB_ORDER_LIST = 4018;
	public static final int CMD_EB_ORDER_DETAIL = 4019;
	public static final int CMD_EB_ORDER_RETURN = 4029;// 退款
	public static final int CMD_EB_ORDER_SKILL = 4024;// 秒杀下单
	public static final int CMD_EB_ORDER_PAY = 4020;// 支付订单
	public static final int CMD_EB_ORDER_CLIENT_PAY_SUCCESS = 4025;// 支付订单
	// 专题相关
	public static final int CMD_EB_ACTIVITY_GET = 4005;
	// 商品相关
	@Deprecated
	public static final int CMD_EB_PRODUCT_GETBYPRODUCTCODE = 4009;
	public static final int CMD_EB_PRODUCT_GETBYACTIVITYID = 4006;
	public static final int CMD_EB_EBSECKILL_GETBYACTIVITYID = 4008;
	public static final int CMD_EB_PRODUCT_SKU_GETBYPRODUCODE = 4007;
	// 地址相关
	public static final int CMD_EB_USERADDRESS_LIST = 4015;
	public static final int CMD_EB_USERADDRESS_ADD = 4031;
	public static final int CMD_EB_USERADDRESS_DELETE = 4032;
	public static final int CMD_EB_USERADDRESS_UPDATE = 4033;

	public static final int CMD_CREDIT_POLICY = 4021;
	public static final int CMD_CREDIT_BYUID = 4038;
	public static final int CMD_EB_SUBMITCOMMENT = 4022;
	public static final int CMD_EB_GETCOMMENTBYPRODUCT = 4023;
	// 电商平台 -- over

	// 第三方平台用户登录相关
	public static final int CMD_THIRD_PLATFORM_LOGIN = 4034;
	public static final int CMD_THIRD_PLATFORM_LOGIN_KEY = 4040;
	// 忘记密码
	public static final int CMD_FORGET_PWD = 4035;
	// 4.0的登陆注销command
	public static final int CMD_LOGIN_4_0 = 4036;
	public static final int CMD_LOGOUT_4_0 = 4037;
	public static final int CMD_MODIFY_PASSWD_4_0 = 4044;
	public static final int CMD_CREDIT_ADD = 4039;
	public static final int CMD_VIP_PRODUCT_LIST = 4041;
	public static final int CMD_VIP_PAY = 4042;
	public static final int CMD_ERROR_ANALYSIS = 4043;
	public static final int CMD_REGIST_4_0 = 4045;

	public static final int CMD_AD_LAUNCH = 4046;
	// 启动大图点击统计
	public static final int CMD_LOG_PV_AD_LAUNCH = 4047;
	public static final int CMD_ALBUM_DETAIL = 4048;

	// 购物车
	// 不需要参数
	public static final int CMD_SHOPPINGCART_LIST = 5001;
	// 入参：int skuCode;
	public static final int CMD_SHOPPINGCART_ADD = 5002;
	// 入参：int[] ids;
	public static final int CMD_SHOPPINGCART_DELETE = 5003;
	// 入参：int id;
	public static final int CMD_SHOPPINGCART_INCREASE = 5004;
	// 入参：int id;
	public static final int CMD_SHOPPINGCART_DECREASE = 5005;
	// 入参：List<ShoppingCartItem> cartItems;
	public static final int CMD_SHOPPINGCART_SYN = 5006;
	// 不需要参数
	public static final int CMD_SHOPPINGCART_CLEAR = 5007;
	// TODO 暂时不用
	public static final int CMD_SHOPPINGCART_CHECKED = 5008;
	// TODO 暂时不用
	public static final int CMD_SHOPPINGCART_CHECKED_ALL = 5009;
	// 不需要参数
	public static final int CMD_SHOPPINGCART_COUNT = 5010;

	// 优惠券
	// 入参：double totalPrice,int[] ids
	public static final int CMD_COUPON_LIST = 5011;
	// 入参：String serialNumber
	public static final int CMD_COUPON_EXCHANGE = 5012;
	// 入参:String serialNumber,double totalPrice,double
	// shipping,int[] ids
	public static final int CMD_COUPON_EXCHANGE_AND_USE = 5013;
	// 入参:int couponId,double totalPrice,double shipping,int[] ids
	public static final int CMD_COUPON_USE = 5014;

	// 结算下单
	// 入参：int[] ids,List<GiftItem> gifts
	public static final int CMD_ORDER_CONFIRM = 5015;
	// 入参：OrderConfirm orderConfirm
	public static final int CMD_ORDER_CREATE = 5016;
	// 入参：productCode productCode
	public static final int CMD_PRODUCT_GETBYPRODUCODE = 5017;
	// 入参：ebSpecialId ebSpecialId
	public static final int CMD_EBSPECIAL_GETBYID = 5018;
	// 入参：ebSpecialId ebSpecialId
	public static final int CMD_EBSPECIALPRODUCTS_GETBYID = 5022;

	// 入参：String couponId
	public static final int CMD_COUPON_OBTAIN = 5019;

	// 评价保存入参：productCode,skuCode,score,comment
	public static final int CMD_COMMENT_SAVE = 5020;
	// 分页获取评价入参：productCode,commentNum,commentId(最后一个评论id),commentType(评价类型：1差评2中评3好评)
	public static final int CMD_COMMENT_GETBYPAGE = 5021;
	// 获取分类名称:categoryId(整型),page(页数从0开始),pageSize
	public static final int CMD_SEARCH_PRODUCTCATEGORY = 5023;
	// 获取推荐页，空参
	public static final int CMD_RECOMMEND = 5024;
	// 推荐视频，分页，带分页参数
	public static final int CMD_RECOMMEND_PRODUCTPAGE = 5025;
	// 我的订单:入参：page,orderTime,pageSize,page从0开始
	public static final int CMD_ORDER_MINE = 5026;
	// 订单详情：入参 orderId
	public static final int CMD_ORDER_DETAIL = 5027;
	// 微信支付查询：入参：orderId
	public static final int CMD_WXPAY_QUERY = 5028;
	// 订单明细微信支付：入参：orderId
	public static final int CMD_WXPAY_PAY = 5029;
	// 足迹分页查询：入参：pageSize,最后一个trackId
	public static final int CMD_TRACK_QUERY = 5030;
	// （未用到）
	public static final int CMD_TRACK_SAVE = 5031;
	// 根据品牌查分类
	public static final int CMD_CATAGORY_GETBYBRAND = 5032;
	// 剧集详情
	public static final int CMD_ALBUM_DETAIL_V5 = 5033;
	// 动漫首页
	public static final int CMD_ALBUM_HOMEPAGE = 5034;
	// 动漫首页热播动漫分页：入参：page页数从0开始,pageSize每页显示数量
	public static final int CMD_ALBUM_PAGE = 5035;
	// 玩具首页
	public static final int CMD_PRODUCT_HOMEPAGE = 5036;
	// 玩具首页:热门玩具分页查询：入参：page页数从0开始,pageSize每页显示数量
	public static final int CMD_PRODUCT_PAGE = 5037;
	// 取消订单：入参：orderId
	public static final int CMD_ORDER_CANCEL = 5038;
	// 通过分类查询玩具:入参：categoryId
	public static final int CMD_PRODUCT_BYCATEGORY = 5039;
	// 商品综合排序按最高价格排序：入参：page 页数从0开始，pageSize每页显示个数，暂时没有用
	public static final int CMD_PRODUCT_MAXPRICE = 5040;
	// 商品综合排序按最高价低排序:入参：page 页数从0开始，pageSize每页显示个数，暂时没有用
	public static final int CMD_PRODUCT_LOWPRICE = 5041;
	// 商品综合排序按最新上架排序:入参：page 页数从0开始，pageSize每页显示个数，暂时没有用
	public static final int CMD_PRODUCT_NEWONSHELF = 5042;
	// 商品综合排序评价最佳排序:入参：page 页数从0开始，pageSize每页显示个数，暂时没有用
	public static final int CMD_PRODUCT_GOODCOMMENT = 5043;
	// 商品综合排序按销售量最多排序:入参：page 页数从0开始，pageSize每页显示个数，暂时没有用
	public static final int CMD_PRODUCT_SELLBEST = 5044;
	// 修改密码:入参：oldpassword,password,password2
	public static final int CMD_CUSTOMER_MODIFY_PASSOWRD = 5045;
	// 获取一级分类:参数：无
	public static final int CMD_CATEGORY_FIRST = 5046;
	// 宝宝设置添加保存,参数：name,birthday(字符串,格式:yyyy-MM-dd),sex
	public static final int CMD_BABY_SAVE = 5047;
	// 宝宝设置查询，最多可以有2个宝宝
	public static final int CMD_BABY_QUERY = 5048;
	// 活动列表：入参：page从0开始,pageSize 每页显示个数
	public static final int CMD_ACTIVITY_LIST_V5 = 5049;
	// 专场列表：入参：page从0开始,pageSize 每页显示个数
	public static final int CMD_SPECIAL_LIST = 5050;
	// 专题列表：入参：page从0开始,pageSize 每页显示个数
	public static final int CMD_TOPIC_LIST = 5051;
	// 知识主页
	public static final int CMD_KNOWLEDGE_HOMEPAGE = 5052;
	// 订单完成：入参：orderId
	public static final int CMD_ORDER_COMPLETE = 5053;
	// 订单删除:入参：orderId
	public static final int CMD_ORDER_DELETE = 5054;
	// 足迹删除
	public static final int CMD_TRACK_DELETE = 5055;
	// 待评论列表：入参：pageSize，orderId：当前页最后一个orderId
	public static final int CMD_ORDER_WAITCOMMENT = 5056;
	// 已评论列表：入参：pageSize，orderId：当前页最后一个orderId
	public static final int CMD_ORDER_HAVECOMMENT = 5057;
	// 清空足迹：无参
	public static final int CMD_TRACK_DELETEALL = 5058;
	// 动漫搜索条件
	public static final int CMD_SEARCH_ANIMECATEGORY = 5059;
	// 获取文章内容：入参：topicId
	public static final int CMD_TOPIC_GETBYID = 5060;
	// VIP充值产品,充值
	public static final int CMD_VIP_CREATE_ORDER_V5 = 5062;
	// 我的优惠券:入参:page,pageSize
	public static final int CMD_COUPON_MINE = 5063;
	// 知识首页分类搜索
	public static final int CMD_SEARCH_KNOWLEDGEHOME = 5064;
	// 动漫、知识搜索页获取分类视频：入参：categoryId,page,pageSize
	public static final int CMD_SEARCH_ALBUMBYCATEGORY = 5065;
	// 用户注册
	public static final int CMD_CUSTOMER_REGIST = 5066;
	// 所有品牌:page,pageSize,若不分页无需传分页参数
	public static final int CMD_BRAND_ALL = 5067;
	// 根据品牌id获取相应商品
	public static final int CMD_SEARCH_BYBRANDID = 5068;
	// 搜索商品
	public static final int CMD_SEARCH_PRODUCT = 5069;
	// 删除宝宝:入参：babyId
	public static final int CMD_BABY_DELETE = 5070;
	// 修改宝宝:入参：babyId，name,sex,birthday
	public static final int CMD_BABY_UPDATE = 5071;
	// 根据知识分类获取知识：入参：categorId
	public static final int CMD_KNOWLEDGE_BYCATEGORY = 5072;
	// 搜索动漫
	public static final int CMD_SEARCH_ANIME = 5073;
	// 搜索知识
	public static final int CMD_SEARCH_KNOWLEDGE = 5074;
	// 搜索商品带综合排序筛选，分类，年龄条件数据
	public static final int CMD_SEARCH_PRODUCT_WITHCONDITION = 5075;
	// 搜索动漫带综合排序，筛选，分类，年龄条件数据
	public static final int CMD_SEARCH_ANIME_WITHCONDITION = 5076;
	// 搜索知识带综合排序，分类，年龄条件数据
	public static final int CMD_SEARCH_KNOWLEDGE_WITHCONDITION = 5077;
	// 立即购买
	public static final int CMD_ORDER_BUY_NOW = 5078;
	// 混合搜索全部
	public static final int CMD_SEARCH_MIX = 5079;
	// 我的可用现金券分页:入参:page,pageSize,couponId最后一个现金券id
	public static final int CMD_COUPON_CASH_BYPAGE = 5080;
	// 我的可用满减券分页:入参:page,pageSize,couponId最后一个满减券id
	public static final int CMD_COUPON_REDUCE_BYPAGE = 5081;
	// 我的不可用现金券分页:入参:page,pageSize,couponId最后一个现金券id
	public static final int CMD_COUPON_UNABLE_CASH_BYPAGE = 5082;
	// 我的不可用满减券分页:入参:page,pageSize,couponId最后一个满减券id
	public static final int CMD_COUPON_UNABLE_REDUCE_BYPAGE = 5083;
	// 用户中心：入参：无
	public static final int CMD_CUSTOMER_CENTER = 5084;
	// 用户反馈
	public static final int CMD_FEEDBACK_SAVE = 5085;
	// 再次购买:入参：orderId
	public static final int CMD_ORDER_BUY_AGAIN = 5086;
	// 用户获取手机验证码:入参:phone 返回参数：validateNum,validate(0未验证1已验证2已占用)
	public static final int CMD_CUSTOMER_PHONE_VALIDATE_NUM = 5087;
	// 用户获取邮箱验证码
	public static final int CMD_CUSTOMER_EMAIL_VALIDATE_NUM = 5088;
	// 用户手机验证:入参：phone,validateNum
	public static final int CMD_CUSTOMER_PHONE_VALIDATE = 5089;
	// 用户邮箱验证:入参：email，validateNumƒΩß
	public static final int CMD_CUSTOMER_EMAIL_VALIDATE = 5090;
	// 用户手机是否验证:入参:phone
	public static final int CMD_CUSTOMER_PHONE_IS_VAILDATE = 5091;
	// 用户邮箱是否验证:入参：email
	public static final int CMD_CUSTOMER_EMAIL_IS_VAILDATE = 5092;
	// 解绑手机号:入参：phone,validateNum
	public static final int CMD_CUSTOMER_PHONE_UNBINDING = 5093;
	// 用户更换手机号:入参：phone,validateNum(验证码)两个参数都是String
	public static final int CMD_CUSTOMER_CHANGE_PHONE_BOUND = 5094;
	// 发送通过邮箱修改手机号验证邮件:入参：无
	public static final int CMD_CUSTOMER_CHANGE_PHONE_SEND_EMAIL = 5095;
	// 通过邮箱更换手机号:入参：phone,validateNum(验证码)两个参数都是String
	public static final int CMD_CUSTOMER_CHANGE_PHONE_BYEMAIL = 5096;
	// 获取密码找回的用户：入参:account
	public static final int CMD_CUSTOMER_GET_RECOVER_USER = 5097;
	// 获取密码找回手机验证码：入参：phone,account
	public static final int CMD_CUSTOMER_RECOVER_VALIDATE_CODE = 5098;
	// 发送密码找回验证码邮件：入参：email,account
	public static final int CMD_CUSTOMER_SEND_RECOVER_EMAIL = 5099;
	// 密码找回提交：入参：account，password,password2
	public static final int CMD_CUSTOMER_PASSWORD_RECOVER_CONFIRM = 5100;
	// 密码找回校验验证码是否正确，入参：phone或者email,validateNum,account
	public static final int CMD_CUSTOMER_VALIDATE_RECOVER_CODE = 5101;
	// 确认更换验证邮箱：email，validateNum
	public static final int CMD_CUSTOMER_CHANGE_EMAIL = 5102;
	// 解绑邮箱:入参：email,validateNum
	public static final int CMD_CUSTOMER_EMAIL_UNBINDING = 5103;
	// VIP购买价格定义列表：入参：无
	public static final int CMD_VIP_COST_DEFINE_LIST = 5104;
	// 获取推送消息：入参：lastId
	public static final int CMD_PUSHMESSAGE_LAST_V5 = 5105;
	// 获取广告页
	public static final int CMD_AD_LAUNCH_V5 = 5106;
	// 我的可使用优惠券:入参无
	public static final int CMD_COUPON_MINE_AVAILABLE = 5107;
	// 我的不可使用优惠券：入参无
	public static final int CMD_COUPON_MINE_UNAVAILABLE = 5108;
	// 可使用优惠券列表:入参无
	public static final int CMD_COUPON_LIST_AVAILABLE = 5109;
	// 不可使用列表优惠券：入参无
	public static final int CMD_COUPON_LIST_UNAVAILABLE = 5110;
	// 支付前处理：入参：orderId,payType
	public static final int CMD_ORDER_PAY_PREPARE = 5111;
	// 支付订单:入参:orderId
	public static final int CMD_EB_ORDER_CLIENT_PAY_SUCCESS_V5 = 5112;
	// 获取H5最新数据包，入参：h5Version
	public static final int CMD_H5_VERSION_LAST = 5113;
	//分页获取获取商品评入参：page,productCode,commentNum,commentType(评价类型：1差评2中评3好评)
	public static final int CMD_PRODUCT_COMMENT_BY_PAGE = 5114;
	// IPAD混合搜索全部，将显示结果分类
	public static final int CMD_SEARCH_MIX_BY_CATEGORY = 5115;
	//我的优惠券分类查询：入参：type(0:未使用，1:已使用，2:已过期)
	public static final int CMD_COUPON_LIST_BY_TYPE = 5116;
	// 我的订单:入参：type(0:全部|1：待付款|2：待收货|3：待评价|4：已取消),page从0开始,pageSize,orderTime上一页最后的时间,orderId上一页最后的订单id,第1页orderId和orderTime分别传0和空字符串
	//isGetNum:是否要取所有分类的数量，1代表获取数量，不传或者为0代表不获取数量
	public static final int CMD_ORDER_MINE_QUERY_BY_CATEGORY = 5117;
	// 获取最近播放的视频:入参：type(0：动漫视频，1：知识,2:推荐最新观看)
	public static final int CMD_VIDEO_QUERY_RECENT_PLAY = 5118;
	// 获取未审核剧集列表:key
	public static final int CMD_ALBUM_UNREVIEW_LIST = 5119;
	// 获取未审核视频列表:albumId,key
	public static final int CMD_VIDEO_UNREVIEW_LIST = 5120;
	// 审核视频:reviewType(1:审核通过2：打回)，videoId,key
	public static final int CMD_VIDEO_REVIEW = 5121;
	// 积分策略
	public static final int CMD_CREDIT_STRATEGY_QUERY = 5122;
	//通过条形码获取商品：入参: EANCode
	public static final int CMD_PRODUCT_GET_BY_EANCODE = 5123;
	//扫一扫:入参：code
	public static final int CMD_SCANNING_REDIRECT = 5124;
	//统计保存:入参：statisticsId(有就传没有就不传)
	public static final int CMD_STATISTICS_ADD = 5125;
	//移动端网站第三方登录,入参：openId，token，type，nickname
	public static final int CMD_WEBMOBILE_THIRD_PLATFORM_LOGIN = 5126;
	//移动端网站我的页面订单数量
	public static final int CMD_WEBMOBILE_MINE_PAGE_ORDER_NUMBER = 5127;
	//获取支付宝支付请求表单
	public static final int CMD_ALIPAY_REQUEST_PAY_FORM = 5128;
	//校验手机验证码是否正确：入参:phone，validateNum
	public static final int CMD_CUSTOMER_CHECK_PHONE_CODE = 5129;
	//校验邮箱验证码是否正确:入参:email，validateNum
	public static final int CMD_CUSTOMER_CHECK_EMAIL_CODE = 5130;
	//微信移动端网站三方登陆
	public static final int CMD_WEBMOBILE_WX_THIRD_PLATFORM_LOGIN = 5131;
	//积分来源记录列表
	public static final int CMD_CREDIT_RECORD_LIST = 5132;
	//获取用户收藏产品或者动漫列表 根据传入的type 类型 1 为产品 2为动漫
	public static final int CMD_CUSTOMER_COLLECTION_LIST = 5133;
	//保存用户收藏 根据传入的类型type  1 为产品 2为动漫
	public static final int CMD_CUSTOMER_SAVE_COLLECTION = 5134;
	//用户取消收藏 传用户ID 动漫 或者产品ID
	public static final int CMD_CUSTOMER_DEL_COLLECTION = 5135;
	//用户清空收藏 内容
	public static final int CMD_CUSTOMER_DEL_ALL_COLLECTION = 5136;
	//用户5.0 新版登录 
	public static final int CMD_NEW_LOGIN_5_0 = 5137;
	//用户手机快速登录
	public static final int CMD_PHONE_QUICK_LOGIN = 5138;
	//验证手机
	public static final int CMD_VALIDATE_PHONE = 5139;
	//批量删除收藏
	public static final int CMD_DEL_COLLECTIONS = 5140;
	//用户手机注册前手机校验
	public static final int CMD_PHONE_VALIDATE_REGIST = 5141;
	
	//用户手机注册
	public static final int CMD_PHONE_QUICK_REGIST = 5142;
	
	//用户补充密码
	public static final int CMD_CUSTOMER_SUPPLY_PWD = 5143;
	
	//关联账号
	public static final int CMD_RELATE_CUSTOMER_ACCOUNT = 5144;
	
	//校验图型验证是否正确：code
	public static final int CMD_IMAGE_VERIFY_CHECK = 5145;
	//手机发送短信验证码：入参:phone
	public static final int CMD_PHONE_SEND_SMS_VALIDATE_CODE = 5146;
	
	//校验手机验证码是
	public static final int CMD_PHONE_CHECK_VALIDATE_CODE = 5147;
	//用户名检查
	public static final int CMD_CUSTOMER_ACCOUNT_CHECK = 5148;
	
	//用户名称修改
	public static final int CMD_CUSTOMER_ACCOUNT_UPDATE = 5149;
	//用户解绑校验：
	public static final int  CMD_CUSTOMER_UNBINDING_VALIDATE= 5150;
	
	//快速找回密码发送验证码
	public static final int CMD_QUICK_FIND_PWD_SEND_CODE = 5151;
	
	//验证快速找回密码邮箱验证码
	public static final int CMD_QUICK_VALIDATE_EMAIL_CODE = 5152;
	
	//用户快速找密码
	public static final int CMD_QUICK_FIND_PWD = 5153;

	//检查第三方用户是否存在：入参与第三方登录参数一致
	public static final int CMD_LOGIN_CHECK_THIRD_PLATFORM_USER = 5154;
	//第三方登录用户绑定手机
	public static final int CMD_THIRD_PLAT_LOGIN_BINDING_ACCOUNT = 5155;
	
	//新增用户
	public static final int CMD_QUICK_ADD_CUSTOMER = 5156;
	
	//第三方登录用户关联已有帐号
	public static final int CMD_THIRD_PLAT_LOGIN_RELATE_EXISTS_USER = 5157;
	
	//新版普通登录
	public static final int CMD_COMMON_LOGIN = 5159;
	
	
	//第三方登录手机号已占用关联或不关联
	public static final int CMD_THIRD_PLAT_LOGIN_OCCUPY_RELATE = 5158;
	
	//获取崩溃日志列表
	public static final int CMD_EXCEPTION_LIST  =5160;
	
	// 获取新浪微博accesstoken
	public static final int CMD_GET_WEIBO_ACCESSTOKEN = 5161;
	
	// 检查微信第三方用户校验
	public static final int CMD_WAPMOBILE_WX_CHECK_THIRD_PLAT_USER = 5162;
	
	// 新验证苹果支付
	public static final int CMD_MEMBER_VALIDATE_APP_RECHGARGE_V5 = 5164;
	
	// 月饼券的特有活动，临时增加；凡一年以上vip能领到一张月饼券
	public static final int CMD_YUEBING_CHECK = 1000000;

	public static final int CMD_YUEBING_OBTAIN = 1000002;

	public static final int CMD_YUEBING_INIT = 1000001;
	
	
	// 获取微信素材列表
	public static final int CMD__WX_FILE_LIST = 5163;
	
	//母亲节活动  ACCESSTOKEN
	public static final int GET_WX_HD_ACCESSTOKEN = 5165;
	
}
