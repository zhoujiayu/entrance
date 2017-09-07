package com.ytsp.entrance.util.alipay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.alipay.httpclient.HttpProtocolHandler;
import com.ytsp.entrance.util.alipay.httpclient.HttpRequest;
import com.ytsp.entrance.util.alipay.httpclient.HttpResponse;
import com.ytsp.entrance.util.alipay.httpclient.HttpResultType;

/* *
 *类名：AlipaySubmit
 *功能：支付宝各接口请求提交类
 *详细：构造支付宝各接口表单HTML文本，获取远程HTTP数据
 *版本：3.3
 *日期：2012-08-13
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipaySubmit {

	/**
	 * 支付宝提供给商户的服务接入网关URL(新)
	 */
	private static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

	/**
	 * 生成签名结果
	 * 
	 * @param sPara
	 *            要签名的数组
	 * @return 签名结果字符串
	 */
	public static String buildRequestMysign(Map<String, String> sPara) {
		String prestr = AlipayCore.createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
		String mysign = "";
		// if (AlipayConfig.sign_type.equals("MD5")) {
		// mysign = MD5.sign(prestr, AlipayConfig.ali_public_key,
		// AlipayConfig.input_charset);
		// } else if (AlipayConfig.sign_type.equals("RSA")) {
		mysign = RSA.sign(prestr, AlipayConfig.private_key,
				AlipayConfig.input_charset);
		// }
		// mysign = MD5.sign(prestr, AlipayConfig.ali_public_key,
		// AlipayConfig.input_charset);
		return mysign;
	}
	
	/**
	* <p>功能描述:根据签名类型获取对应的签名</p>
	* <p>参数：@param sPara
	* <p>参数：@param type
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String buildRequestMysign(Map<String, String> sPara,String type) {
		String prestr = AlipayCore.createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
		String mysign = "";
		if(type.equals("MD5")){
			mysign = MD5.sign(prestr, AlipayConfig.ali_public_key,
					 AlipayConfig.input_charset);
		}else if(type.equals("RSA")){
			mysign = RSA.sign(prestr, AlipayConfig.private_key,
					AlipayConfig.input_charset);
		}
		return mysign;
	}
	
	/**
	 * 生成要请求给支付宝的参数数组
	 * 
	 * @param sParaTemp
	 *            请求前的参数数组
	 * @return 要请求的参数数组
	 */
	private static Map<String, String> buildRequestPara(
			Map<String, String> sParaTemp) {
		// 除去数组中的空值和签名参数
		Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
		// 生成签名结果
		String mysign = buildRequestMysign(sPara);

		// 签名结果与签名方式加入请求提交参数组中
		sPara.put("sign", mysign);
		sPara.put("sign_type", AlipayConfig.sign_type);

		return sPara;
	}
	
	/**
	* <p>功能描述:构建请求参数</p>
	* <p>参数：@param sParaTemp
	* <p>参数：@param type
	* <p>参数：@return</p>
	* <p>返回类型：Map<String,String></p>
	 */
	private static Map<String, String> buildRequestParaBySignType(
			Map<String, String> sParaTemp,String type) {
		// 除去数组中的空值和签名参数
		Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
		// 生成签名结果
		String mysign = buildRequestMysign(sPara, type);

		// 签名结果与签名方式加入请求提交参数组中
		sPara.put("sign", mysign);
		sPara.put("sign_type",type);

		return sPara;
	}
	
	 /**
     * 建立请求，以表单HTML形式构造（默认）
     * @param sParaTemp 请求参数数组
     * @param strMethod 提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     */
    public static String buildRequest(Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + ALIPAY_GATEWAY_NEW
                      + "_input_charset=" + AlipayConfig.input_charset + "\" method=\"" + strMethod
                      + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"/></form>");
        sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

        return sbHtml.toString();
    }

	/**
	 * 建立请求，以表单HTML形式构造，带文件上传功能
	 * 
	 * @param sParaTemp
	 *            请求参数数组
	 * @param strMethod
	 *            提交方式。两个值可选：post、get
	 * @param strButtonName
	 *            确认按钮显示文字
	 * @param strParaFileName
	 *            文件上传的参数名
	 * @return 提交表单HTML文本
	 */
	public static String buildRequest(Map<String, String> sParaTemp,
			String strMethod, String strButtonName, String strParaFileName) {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\"  enctype=\"multipart/form-data\" action=\""
				+ ALIPAY_GATEWAY_NEW
				+ "_input_charset="
				+ AlipayConfig.input_charset
				+ "\" method=\""
				+ strMethod
				+ "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"hidden\" name=\"" + name
					+ "\" value=\"" + value + "\"/>");
		}

		sbHtml.append("<input type=\"file\" name=\"" + strParaFileName
				+ "\" />");

		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName
				+ "\" style=\"display:none;\"></form>");

		return sbHtml.toString();
	}
	
	/**
	 * 建立请求，以模拟远程HTTP的POST请求方式构造并获取支付宝的处理结果
	 * 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值 如：buildRequest("",
	 * "",sParaTemp)
	 * 
	 * @param strParaFileName
	 *            文件类型的参数名
	 * @param strFilePath
	 *            文件路径
	 * @param sParaTemp
	 *            请求参数数组
	 * @return 支付宝处理结果
	 * @throws Exception
	 */
	public static String buildRequest(String strParaFileName,
			String strFilePath, Map<String, String> sParaTemp,String type) throws Exception {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestParaBySignType(sParaTemp, type);

		HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler
				.getInstance();

		HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		// 设置编码集
		request.setCharset(AlipayConfig.input_charset);

		request.setParameters(generatNameValuePair(sPara));
		String strUrl = ALIPAY_GATEWAY_NEW + "_input_charset="
				+ AlipayConfig.input_charset;
		request.setUrl(strUrl);

		HttpResponse response = httpProtocolHandler.execute(request,
				strParaFileName, strFilePath);
		if (response == null) {
			return null;
		}
		String strResult = response.getStringResult();

		return strResult;
	}
	
	/**
	 * 建立请求，以模拟远程HTTP的POST请求方式构造并获取支付宝的处理结果
	 * 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值 如：buildRequest("",
	 * "",sParaTemp)
	 * 
	 * @param strParaFileName
	 *            文件类型的参数名
	 * @param strFilePath
	 *            文件路径
	 * @param sParaTemp
	 *            请求参数数组
	 * @return 支付宝处理结果
	 * @throws Exception
	 */
	public static String buildRequest(String strParaFileName,
			String strFilePath, Map<String, String> sParaTemp) throws Exception {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp);

		HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler
				.getInstance();

		HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		// 设置编码集
		request.setCharset(AlipayConfig.input_charset);

		request.setParameters(generatNameValuePair(sPara));
		String strUrl = ALIPAY_GATEWAY_NEW + "_input_charset="
				+ AlipayConfig.input_charset;
		request.setUrl(strUrl);
		HttpResponse response = httpProtocolHandler.execute(request,
				strParaFileName, strFilePath);
		if (response == null) {
			return null;
		}
		String strResult = response.getStringResult();

		return strResult;
	}

	/**
	 * MAP类型数组转换成NameValuePair类型
	 * 
	 * @param properties
	 *            MAP类型数组
	 * @return NameValuePair类型数组
	 */
	private static NameValuePair[] generatNameValuePair(
			Map<String, String> properties) {
		NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			nameValuePair[i++] = new NameValuePair(entry.getKey(),
					entry.getValue());
		}

		return nameValuePair;
	}

	/**
	 * 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
	 * 
	 * @return 时间戳字符串
	 * @throws IOException
	 * @throws DocumentException
	 * @throws MalformedURLException
	 */
	public static String query_timestamp() throws MalformedURLException,
			DocumentException, IOException {

		// 构造访问query_timestamp接口的URL串
		String strUrl = ALIPAY_GATEWAY_NEW + "service=query_timestamp&partner="
				+ AlipayConfig.partner + "&_input_charset"
				+ AlipayConfig.input_charset;
		StringBuffer result = new StringBuffer();

		SAXReader reader = new SAXReader();
		Document doc = reader.read(new URL(strUrl).openStream());

		List<Node> nodeList = doc.selectNodes("//alipay/*");

		for (Node node : nodeList) {
			// 截取部分不需要解析的信息
			if (node.getName().equals("is_success")
					&& node.getText().equals("T")) {
				// 判断是否有成功标示
				List<Node> nodeList1 = doc
						.selectNodes("//response/timestamp/*");
				for (Node node1 : nodeList1) {
					result.append(node1.getText());
				}
			}
		}

		return result.toString();
	}
	
	
	/**
	* <p>功能描述:即时到账有密退款</p>
	* <p>参数：@param trade_no 
	* <p>参数：@param out_trade_no ikan订单号
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：int</p>
	 */
	public static int refundFastpay(String out_trade_no, String refundReson)
			throws Exception {
		String trade_no = null;
		String refundMenoy = null;
		int ret = 0;
		Map<String,Object> payDetail = queryAccountDetailQuery("", out_trade_no);
//		Map<String,Object> payDetail = new HashMap<String, Object>();
		//判断获取交易明细
		if (payDetail.containsKey("is_success")) {
			String is_success = payDetail.get("is_success").toString();
			//T代表 成功 F代表失败
			if (is_success.equals("F")) {
				ret = -1;
			} 
		}
	
		if(!payDetail.containsKey("buyer_account")){
			return 2;
		}
		if(!payDetail.containsKey("total_fee")){
			return 2;
		}
//		获取买家支付宝帐号
		trade_no = payDetail.get("trade_no").toString();
//		获取交易总金额
		refundMenoy = payDetail.get("total_fee").toString();
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		// 退款批次号:每进行一次即时到账批量退款，都需要提供一个批次号，通过该批次号可以查询这一批次的退款交易记录，对于每一个合作伙伴，传递的每一个批次号都必须保证唯一性。
		sParaTemp.put("batch_no",DateFormatter.date2String(new Date(), "yyyyMMdd")+out_trade_no);
		//总笔数
		sParaTemp.put("batch_num", "1");
		// 交易退款数据集的格式为：原付款支付宝交易号^退款总金额^退款理由；
		sParaTemp.put("detail_data", trade_no + "^" + refundMenoy + "^"
						+ refundReson);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("notify_url", "http://entrance.ikan.cn/entrance/entrance");
		// 	签约的支付宝账号对应的支付宝唯一用户号。以2088开头的16位纯数字组成
		sParaTemp.put("partner", AlipayConfig.partner);
		//退款请求的当前时间。格式为：yyyy-MM-dd hh:mm:ss。
		sParaTemp.put("refund_date",
				DateFormatter.date2String(new Date(), "yyyy-MM-dd HH:mm:ss"));
		//卖家支付宝账号
		sParaTemp.put("seller_email", AlipayConfig.seller_email);
		sParaTemp.put("seller_user_id", AlipayConfig.partner);
		//接口名称
		sParaTemp.put("service", AlipayConfig.refund_fastpay_by_platform_pwd);

		String sHtmlText = AlipaySubmit.buildRequest("","",sParaTemp);
		Map<String, Object> map = parseRspXMLParams(sHtmlText);

		return ret;
	}
	
	/**
	* <p>功能描述:查询账务明细交易信息：用于查询支付宝交易信息</p>
	* <p>参数：@param trade_no 
	* <p>参数：@param out_trade_no ikan订单号
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	 */
	public static Map<String, Object> queryAccountDetailQuery(String trade_no, String out_trade_no) throws Exception {
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		//接口
		sParaTemp.put("service", "account.page.query");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("trade_no", trade_no);
		sParaTemp.put("merchant_out_order_no", out_trade_no);
		sParaTemp.put("page_no", "1");
		
		String sHtmlText = AlipaySubmit.buildRequest("", "", sParaTemp);
		Map<String, Object> map = parseRspXMLParams(sHtmlText);
		return map;
	}
	
	
	/**
	* <p>功能描述:支付宝关闭交易</p>
	* <p>参数：@param trade_no  支付宝交易号
	* <p>参数：@param out_trade_no  ikan订单号
	* <p>参数：@return 1、关闭成功 2、交易不存在 3、交易状态不符合 4、调用支付宝参数问题
	* <p>参数：@throws Exception</p>
	* <p>返回类型：int</p>
	 */
	public static int closeTrade(String trade_no, String out_trade_no) throws Exception {
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "close_trade");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("out_order_no", out_trade_no);
		sParaTemp.put("trade_role", "S");

		int ret = 0;
		// 建立请求
		String sHtmlText = AlipaySubmit.buildRequest("","",sParaTemp);
		Map<String, Object> map = parseRspXMLParams(sHtmlText);
		if (map.containsKey("is_success")) {
			String is_success = map.get("is_success").toString();
			//T代表 成功 F代表失败
			if (is_success.equals("T")) {
				//关闭成功
				return 1;
			} else {
				// 查询交易信息失败的错误代码
				String error = map.get("error").toString();
				if("TRADE_NOT_EXIST".equals(error)){
					//交易不存在
					return  2;
				}else if("TRADE_STATUS_NOT_AVAILD".equals(error)){
					//交易状态不正确
					return 3;
				}else{
					//调用参数问题
					return 4;
				}
			}
		}
		return ret;
	}
	
	/**
	 * 支付宝交易号与商户网站订单号不能同时为空
	 * 
	 * @param trade_no
	 *            支付宝交易号
	 * @param out_trade_no
	 *            商户订单号
	 * @return 0未支付；1支付成功；-1支付成功但金额不对
	 * @throws Exception
	 */
	public static int queryPaySuccess(String trade_no, String out_trade_no,
			double totalPrice) throws Exception {
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "single_trade_query");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("trade_no", trade_no);
		sParaTemp.put("out_trade_no", out_trade_no);

		int ret = 0;
		// 建立请求
		String sHtmlText = AlipaySubmit.buildRequest("", "", sParaTemp);
		Map<String, Object> map = parseRspXMLParams(sHtmlText);

		if (map.containsKey("is_success")) {
			String is_success = map.get("is_success").toString();
			if (is_success.equals("T")) {// 请求成功不代表业务处理成功
				if (map.containsKey("trade_status")) {
					// 交易状态
					// WAIT_SELLER_SEND_GOODS 买家已付款，等待卖家发货
					// WAIT_BUYER_CONFIRM_GOODS 卖家已发货，等待买家确认
					// TRADE_FINISHED 交易成功结束
					// WAIT_SYS_PAY_SELLER 买家确认收货，等待支付宝打款给卖家
					// TRADE_SUCCESS 支付成功
					// BUYER_PRE_AUTH 买家已付款（语音支付）
					// COD_WAIT_SYS_PAY_SELLER 签收成功等待系统打款给卖家（货到付款）
					// WAIT_BUYER_PAY 等待买家付款
					// TRADE_CLOSED 交易中途关闭（已结束，未成功完成）
					// WAIT_SYS_CONFIRM_PAY 支付宝确认买家银行汇款中，暂勿发货
					// TRADE_REFUSE 立即支付交易拒绝
					// TRADE_REFUSE_DEALING 立即支付交易拒绝中
					// TRADE_CANCEL 立即支付交易取消
					// TRADE_PENDING 等待卖家收款
					// COD_WAIT_SELLER_SEND_GOODS 等待卖家发货（货到付款）
					// COD_WAIT_BUYER_PAY 等待买家签收付款（货到付款）
					String trade_status = map.get("trade_status").toString();
					if (trade_status.equals("WAIT_SELLER_SEND_GOODS")
							|| trade_status.equals("WAIT_BUYER_CONFIRM_GOODS")
							|| trade_status.equals("TRADE_FINISHED")
							|| trade_status.equals("WAIT_SYS_PAY_SELLER")
							|| trade_status.equals("TRADE_SUCCESS")
							|| trade_status.equals("BUYER_PRE_AUTH")
							|| trade_status.equals("COD_WAIT_SYS_PAY_SELLER")) {
						// 交易金额
						double total_fee = Double.valueOf(
								map.get("total_fee").toString()).doubleValue();
						// 付款时间
						String gmt_payment = map.get("gmt_payment").toString();
						if (total_fee == totalPrice) {
							ret = 1;
						} else {
							ret = -1;
						}
					} else {
						//支付宝业务处理不成功
						ret = -2;
					}
				}
			} else {
				// 查询交易信息失败的错误代码
				String error = map.get("error").toString();
				//ios切换程序时会调用，此时状态为TRADE_NOT_EXIST
				if(StringUtil.isNotNullNotEmpty(error) && "TRADE_NOT_EXIST".equals(error)){
					ret = -2;
				}else{
					ret = 0;
				}
			}
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		// // 支付宝交易号
//		String trade_no = "";
		// // 支付宝交易号与商户网站订单号不能同时为空
		// // 商户订单号
//		String out_trade_no = new String("1509301436460100");
		// //
		// ////////////////////////////////////////////////////////////////////////////////
		// // 把请求参数打包成数组
//		Map<String, String> sParaTemp = new HashMap<String, String>();
//		sParaTemp.put("service", "single_trade_query");
//		sParaTemp.put("partner", AlipayConfig.partner);
//		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
//		sParaTemp.put("trade_no", trade_no);
//		sParaTemp.put("out_trade_no", out_trade_no);
//		// 建立请求
//		String sHtmlText = AlipaySubmit.buildRequest("", "", sParaTemp);
//		parseRspXMLParams(sHtmlText);
//		queryAccountDetailQuery("", "1601071232263100");
//		closeTrade("", "1601110944113100");
		
//		queryPaySuccess("", "1601110944113100", 81d);
//		queryAccountDetailQuery("", "1601110944113100");
		refundFastpay("1601122027060100", "买家退货");

	}

	public static Map<String, Object> parseRspXMLParams(String paramsStr)
			throws UnsupportedEncodingException, DocumentException {
		SAXReader reader = new SAXReader();
		Map<String, Object> map = new HashMap<String, Object>();
		if(StringUtil.isNullOrEmpty(paramsStr)){
			return map;
		}
		Document doc = reader.read(new ByteArrayInputStream(paramsStr
				.getBytes("UTF-8")));
		parseElement(doc.getRootElement(), map);
		return map;
	}

	public static void parseElement(Element element, Map<String, Object> map) {
		System.out.println(element.getName() + ":" + element.getData());
		String key = element.getName();
		Object value = element.getData();
		map.put(key, value);
		Iterator tickets = element.elementIterator();
		while (tickets.hasNext()) {
			parseElement((Element) tickets.next(), map);
		}
	}
}
