package com.ytsp.entrance.util.alipay.params;

/**
 * 支付宝交易请求参数
 * */
public class AlipayRequestParams {
	/** 接口名称。 */
	public String service = "create_direct_pay_by_user";
	/**
	 * 合作者身 份ID ：签约的支付宝账号对应的支付宝唯一用户号，以2088开头的16位纯数字组成。
	 */
	public String partner;
	/**
	 * 参数编码字符集：商户网站使用的编码格式，如 utf-8 、 gbk 、 gb2312 等。
	 */
	public String _input_charset = "utf-8";
	/**
	 * 签名方式：DSA 、 RSA 、 MD5 三个值可选， 必须大写。
	 */
	public String sign_type;
	
	/**
	 * 默认网银
	 */
	public String defaultbank;
	/**
	 * 签名。
	 */
	public String sign;
	/**
	 * 服务器异步通知页面路径（长度190\可空）:支付宝服务器主动通知商户网站 里指定的页面 http路径。
	 */
	public String notify_url;
	/**
	 * 页面跳转同步通知页面路径（长度200\可空）:支付宝处理完请求后，当前页面 自动跳转到商户网站里指定页面 的 http 路径。
	 */
	public String return_url;
	/**
	 * 请求出错时的通知页面路径（长度200\可空）:当商户通过该接口发起请求时， 如果出现提示报错，支付宝会根 据“ 10.10
	 * item_orders_info 出 错时的通知错误码 ”和“ 10.11 请求出错时的通知错误码 ”通过 异步的方式发送通知给商户。
	 * 该功能需要联系支付宝开通。
	 */
	public String error_notify_url;
	/**
	 * 商户网站唯 一订单号（长度64）:支付宝合作商户网站唯一订单号 （确保在商户系统中唯一）。
	 */
	public String out_trade_no;
	/**
	 * 商品名称（长度256）:商品的标题 /交易标题 /订单标题 /订单关键字等。 该参数最长为 128 个汉字。
	 */
	public String subject;
	/**
	 * 支付类型（长度4）:默认值为： 1 （商品购买）。
	 */
	public String payment_type = "1";

	/**
	 * 交易金额（可空）:该笔订单的资金总额，单位为 RMB-Yuan。取值范围为[0.01，100000000.00]精确到小数点 后两位 。
	 */
	public double total_fee;

	/**
	 * 卖家支付宝账户号（长度16）:卖家支付宝账号对应的支付宝唯 一用户号。 以 2088 开头的纯 16 位数字。
	 */
	public String seller_id;

	/**
	 * 买家支付宝账户号（长度16\可空）:买家支付宝账号对应的支付宝唯 一用户号。 以 2088 开头的纯 16 位数字。
	 */
	public String buyer_id;
	/**
	 * 卖家支付宝账号（长度100\可空）:卖家支付宝账号，格式为邮箱或手 机号。
	 */
	public String seller_email;
	/**
	 * 买家支付宝账号（长度100\可空）:买家支付宝账号，格式为邮箱或手 机号。
	 */
	public String buyer_email;
	/**
	 * 卖家别名支付宝账 号（长度100\可空）:卖家别名支付宝账号。 <br/>
	 * 卖家信息优先级： seller_id>seller_account_name >seller_email 。
	 */
	public String seller_account_name;
	/**
	 * 买家别名支付宝账 号（长度100\可空）:卖家别名支付宝账号。 <br/>
	 * 买家信息优先级：buyer_id>buyer_account_name >buyer_email 。
	 */
	public String buyer_account_name;

	/**
	 * 商品单价（可空）:单位为： RMB Yuan 。取值范围 为 [0.01 ， 100000000.00] ，精确 到小数点后两位。 <br/>
	 * 规则： price 、 quantity 能代替 total_fee 。即存在 total_fee ，就 不能存在 price 和
	 * quantity ； 存在 price 、 quantity ，就不能存在 total_fee 。
	 */
	public double price;

	/**
	 * 购买数量（可空）:price 、 quantity 能代替 total_fee 。 即存在 total_fee ，就不能存在 price 和
	 * quantity ；存在 price 、 quantity ，就不能存在 total_fee 。
	 */
	public int quantity;
	/**
	 * 商品描述(长度1000\可空)：对一笔交易的具体描述信息。如 果是多种商品，请将商品描述字 符串累加传给 body 。
	 */
	public String body;
	/**
	 * 商品展示 网址 (长度400\可空)：收银台页面上，商品展示的超链 接 。
	 */
	public String show_url;
	/**
	 * 默认支付 方式 (可空)： 取值范围： creditPay （信用支付） ;directPay （余额支付）。 如果不设置，默认识别为余额支 付
	 */
	public String paymethod = "directPay";
	/**
	 * 支付渠道(可空)：用于控制收银台支付渠道显示,可支持多种支付渠道显示，以“ ^ ”
	 */
	public String enable_paymethod;
	/**
	 * 网银支付 时是否做 CTU 校验 (可空)：商户在配置了支持 CTU （支付宝 风险稽查系统）校验权限的前提 下，可以选择本次交易是否需要 经过
	 * CTU 校验。 <br/>
	 * Y ：做 CTU 校验； <br/>
	 * N ：不做 CTU 校验 （余额支付） 。
	 */
	public String need_ctu_check;
	/**
	 * 提成类型 (长度2\可空)：目前只支持一种类型： 10 （卖家 给第三方提成）。 <br/>
	 * 注意：当传递了参数 royalty_parameters 时，提成类 型参数不能为空 。
	 */
	public String royalty_type;
	/**
	 * 分润账号 集 (长度1000\可空) 。
	 */
	public String royalty_parameters;
	/**
	 * 防钓鱼时间戳(可空):通过时间戳查询接口获取的加密 支付宝系统时间戳。<br/>
	 * 如果已申请开通防钓鱼时间戳验 证，则此字段必填 。
	 */
	public String anti_phishing_key;
	/**
	 * 客户端 IP (长度15\可空):用户在创建交易时，该用户当前 所使用机器的 IP 。<br/>
	 * 如果商户申请后台开通防钓鱼选 项，此字段必填，校验用。
	 */
	public String exter_invoke_ip;
	/**
	 * 公用回传 参数(长度100\可空):如果用户请求时传递了该参数， 则返回给商户时会回传该参数。
	 */
	public String extra_common_param;
	/**
	 * 公用业务 扩展参数(可空):用于商户的特定业务信息的传 递，只有商户与支付宝约定了传 递此参数且约定了参数含义，此 参数才有效。<br/>
	 * 参数格式：参数名 1^ 参数值 1| 参数名 2^ 参数值 2| ...... 多条数据用“ | ”间隔
	 */
	public String extend_param;
	/**
	 * 超时时间(可空)：设置未付款交易的超时时间，一 旦超时，该笔交易就会自动被关 闭。<br/>
	 * 取值范围： 1m ～ 15d 。 m-分钟， h-小时， d-天， 1c-当天 （无论交易何时创建，都在 0 点 关闭）。<br/>
	 * 该参数数值不接受小数点，如 1.5h，可转换为 90m 该功能需要联系支付宝配置关闭时间
	 */
	public String it_b_pay;
	/**
	 * 超时时间(可空)：设置未付款交易的超时时间，一 旦超时，该笔交易就会自动被关 闭。<br/>
	 * 取值范围： 1m ～ 15d 。 m-分钟， h-小时， d-天， 1c-当天 （无论交易何时创建，都在 0 点 关闭）。<br/>
	 * 该参数数值不接受小数点，如 1.5h，可转换为 90m 该功能需要联系支付宝配置关闭时间
	 */
	public String default_login;

	/**
	 * 商户申请 的产品类 型(长度50\可空)：用于针对不同的产品，采取不同 的计费策略。 <br/>
	 * 如果开通了航旅垂直搜索平台产 品，请填写 CHANNEL_FAST_PAY ；如果没 有，则为空.
	 */
	public String product_type;
	/**
	 * 快捷登录 授权令牌(长度40\可空):如果开通了快捷登录产品，则需要 填写；如果没有开通，则为空。
	 */
	public String token;
	/**
	 * 快捷登录 授权令牌(长度4000\可空):买家通过 etao 购买的商品的详细 清单。如果是 etao 商户则填写； 如果不是，则为空。
	 */
	public String item_orders_info;
	/**
	 * 快捷登录 授权令牌(长度50\可空): 用于唯一标识商户买家。 如果本参数不为空，则 sign_name_ext 不能为空
	 */
	public String sign_id_ext;
	/**
	 * 快捷登录 授权令牌(长度128\可空):商户买家唯一标识对应的名字。
	 */
	public String sign_name_ext;
	/**
	 * 扫码支付 方式(长度1\可空):扫码支付的方式，支持前置模式和 跳转模式。 前置模式是将二维码前置到商户 的订单确认页的模式。需要商户在
	 * 自己的页面中以 iframe 方式请求 支付宝页面。<br/>
	 * 具体分为以下 3 种：<br/>
	 * 0 ：订 单码 - 简约前置模式，对 应 iframe 宽度不能小于 600px ，高度不能小于 300px ； <br/>
	 * 1 ：订单码 - 前置模式，对应 iframe 宽度不能小于 300px ， 高度不能小于 600px ； <br/>
	 * 3 ：订单码 - 迷你前置模式，对 应 iframe 宽度不能小于 75px ， 高度不能小于 75px 。<br/>
	 * 跳转模式下，用户的扫码界面是由 支付宝生成的，不在商户的域名 下。 <br/>
	 * 2 ：订单码 - 跳转模式。
	 */
	public String qr_pay_mode;
}
