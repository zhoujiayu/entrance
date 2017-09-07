package com.ytsp.entrance.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.audit.Audit;
import com.ytsp.db.audit.AuditAction;
import com.ytsp.db.audit.Auditable;
import com.ytsp.db.audit.Executor;
import com.ytsp.db.domain.CreditsRecord;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbStorage;
import com.ytsp.db.domain.SendSmsConfig;
import com.ytsp.db.domain.ShippingRule;
import com.ytsp.db.enums.EbPosterLinkUrlEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.SendSmsTypeEnum;
import com.ytsp.db.enums.ShippingTypeEnum;
import com.ytsp.db.enums.ValidateTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.ShippingRuleService;
import com.ytsp.entrance.service.v5_0.CreditServiceV5_0;
import com.ytsp.entrance.service.v5_0.SendSmsConfigService;
import com.ytsp.entrance.sms.SmsConfig;
import com.ytsp.entrance.sms.SmsHandler;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.mail.MailFacade;
import com.ytsp.entrance.util.mail.MailServiceFactory;

/**
 * 工具类，提供一些方便的方法
 */
public class Util {

    /**
     * 从ip的字符串形式得到字节数组形式
     * @param ip 字符串形式的ip
     * @return 字节数组形式的ip
     */
    public static byte[] getIpByteArrayFromString(String ip) {
        byte[] ret = new byte[4];
        StringTokenizer st = new StringTokenizer(ip, ".");
        try {
            ret[0] = (byte)(Integer.parseInt(st.nextToken()) & 0xFF);
            ret[1] = (byte)(Integer.parseInt(st.nextToken()) & 0xFF);
            ret[2] = (byte)(Integer.parseInt(st.nextToken()) & 0xFF);
            ret[3] = (byte)(Integer.parseInt(st.nextToken()) & 0xFF);
        } catch (Exception e) {
          System.out.println("从ip的字符串形式得到字节数组形式报错" + e.getMessage());
        }
        return ret;
    }
    /**
     * @param ip ip的字节数组形式
     * @return 字符串形式的ip
     */
    public static String getIpStringFromBytes(byte[] ip) {
    	StringBuilder sb = new StringBuilder();
        sb.append(ip[0] & 0xFF);
        sb.append('.');
        sb.append(ip[1] & 0xFF);
        sb.append('.');
        sb.append(ip[2] & 0xFF);
        sb.append('.');
        sb.append(ip[3] & 0xFF);
        return sb.toString();
    }

    /**
	* <p>功能描述:校验手机是否验证</p>
	* <p>参数：@param cust
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean getCustomerValidateStatus(Customer cust){
		if(cust == null){
			return false;
		}else if(StringUtil.isNullOrEmpty(cust.getMobilephone())){
			return false;
		}else if(cust.getPhoneValidate() == null || cust.getPhoneValidate() == 0){
			return false;
		}else if(cust.getPhoneValidate() == 1){
			return true;
		}
		
		return false;
	}
    
    /**
     * 根据某种编码方式将字节数组转换成字符串
     * @param b 字节数组
     * @param offset 要转换的起始位置
     * @param len 要转换的长度
     * @param encoding 编码方式
     * @return 如果encoding不支持，返回一个缺省编码的字符串
     */
    public static String getString(byte[] b, int offset, int len, String encoding) {
        try {
            return new String(b, offset, len, encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(b, offset, len);
        }
    }
    
    /**
	* <p>功能描述:获取图片的全路径</p>
	* <p>参数：@param image
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getFullImageURL(String image){
//		String imageServerHost = SystemManager.getInstance()
//				.getSystemConfig().getImgServerUrl();
		String imageServerHost = ImagePropertyUtil.getPropertiesValue("custImageHost").trim();
		if(StringUtil.isNullOrEmpty(image)){
			return "";
		}
		if(image.startsWith(imageServerHost)){
			return image;
		}
		if(image.startsWith("http://webimg.ikan.cn/test")){
			return image;
		}
		if(image.startsWith("http://")){
			return image;
		}
		if(image.startsWith("https://")){
			return image;
		}
		if(imageServerHost.endsWith("/") && image.startsWith("/")){
			return imageServerHost+image.substring(1);
		}else if(!imageServerHost.endsWith("/") && !image.startsWith("/")){
			return imageServerHost+"/"+image;
		}else{
			return imageServerHost+image;
		}
	}
	
	/**
	* <p>功能描述:获取图片的全路径</p>
	* <p>参数：@param image
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getHttpsFullImageURL(String image){
		String imageServerHost = ImagePropertyUtil.getPropertiesValue("httpsCustImageHost").trim();
		if(StringUtil.isNullOrEmpty(image)){
			return "";
		}
		if(image.startsWith(imageServerHost)){
			return image;
		}
		if(image.startsWith("http://images.kandongman.com.cn")){
			return image.replaceAll("http://images.kandongman.com.cn", "https://images.ikan.cn");
		}
		
		if(image.startsWith("http://172.16.218.11")){
			return image.replaceAll("http://172.16.218.11", "https://apptest.ikan.cn");
		}
		if(image.startsWith("http://") || image.startsWith("https://")){
			return replaceHttp2Https(image);
		}
		if(imageServerHost.endsWith("/") && image.startsWith("/")){
			return imageServerHost+image.substring(1);
		}else if(!imageServerHost.endsWith("/") && !image.startsWith("/")){
			return imageServerHost+"/"+image;
		}else{
			return imageServerHost+image;
		}
	}
	
	/**
	* 功能描述:将http的Url地址替换成https
	* 参数：@param url
	* 参数：@return
	* 返回类型:String
	 */
	public static String replaceUrlHttp2Https(String url){
		if(StringUtil.isNullOrEmpty(url)){
			return "";
		}
		
		if(url.startsWith("http://images.kandongman.com.cn")){
			return url.replaceAll("http://images.kandongman.com.cn", "https://images.ikan.cn");
		}
		
		if(url.startsWith("http://172.16.218.11")){
			return url.replaceAll("http://172.16.218.11", "https://apptest.ikan.cn");
		}
		if(url.startsWith("http://") || url.startsWith("https://")){
			return replaceHttp2Https(url);
		}
		
		return url;
	}
	
	/**
	* 功能描述:根据版本获取所有图片,iphone5.1.0以后都用https图片地址
	* 参数：@param image
	* 参数：@param version
	* 参数：@return
	* 返回类型:String
	 */
	public static String getFullImageURLByVersion(String image,String version,String platform){
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			int startVer = convert2Num(IConstants.IPHONE_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return getHttpsFullImageURL(image);
			}else{
				return getFullImageURL(image);
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad)) {
			int startVer = convert2Num(IConstants.IPAD_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return getHttpsFullImageURL(image);
			}else{
				return getFullImageURL(image);
			}
		}else{
			return getFullImageURL(image);
		}
	}
	
	/**
	* 功能描述:根据版本和平台将地址里的http替换成https
	* 参数：@param image
	* 参数：@param version
	* 参数：@param platform
	* 参数：@return
	* 返回类型:String
	 */
	public static String replaceURLByVersion(String url,String version,String platform){
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			int startVer = convert2Num(IConstants.IPHONE_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return replaceUrlHttp2Https(url);
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad)) {
			int startVer = convert2Num(IConstants.IPAD_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return replaceUrlHttp2Https(url);
			}
		}
		
		return url;
	}
	
	/**
	* 功能描述:获取广告地址
	* 参数：@param adUrl
	* 参数：@param version
	* 参数：@param platform
	* 参数：@return
	* 返回类型:String
	 */
	public static String getAdHttpsUrlByVersion(String adUrl,String version,String platform){
		if(StringUtil.isNullOrEmpty(adUrl)){
			return "";
		}
		if(!adUrl.trim().startsWith("http")){
			return adUrl;
		}
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			int startVer = convert2Num(IConstants.IPHONE_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return adUrl.replaceFirst("http", "https");
			}else{
				return adUrl;
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad)) {
			int startVer = convert2Num(IConstants.IPAD_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return adUrl.replaceFirst("http", "https");
			}else{
				return adUrl;
			}
		}
		return adUrl;
	}
	
	/**
	* <p>功能描述:获取评论 图片地址</p>
	* <p>参数：@param image
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getCommentImageURL(String image){
		String hostUrl = ImagePropertyUtil.getPropertiesValue("imagehost").trim();
		if(StringUtil.isNullOrEmpty(hostUrl)){
			return SystemManager.getInstance()
					.getSystemConfig().getImgServerUrl() + image;
		}
		if(image.startsWith(hostUrl)){
			return image;
		}
		if(hostUrl.endsWith("/") && image.startsWith("/")){
			return hostUrl+image.substring(1);
		}else if(!hostUrl.endsWith("/") && !image.startsWith("/")){
			return hostUrl+"/"+image;
		}else{
			return hostUrl+image;
		}
	}
	
	/**
	* 功能描述:根据版本获取所有图片,iphone5.1.0以后都用https图片地址
	* 参数：@param image
	* 参数：@param version
	* 参数：@return
	* 返回类型:String
	 */
	public static String getCommentImageURLByVersion(String image,String version,String platform){
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			int startVer = convert2Num(IConstants.IPHONE_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return getHttspCommentImageURL(image);
			}else{
				return getCommentImageURL(image);
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad)) {
			int startVer = convert2Num(IConstants.IPAD_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return getHttspCommentImageURL(image);
			}else{
				return getCommentImageURL(image);
			}
		}else{
			return getCommentImageURL(image);
		}
	}
	
	
	/**
	* <p>功能描述:获取评论 图片地址</p>
	* <p>参数：@param image
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getHttspCommentImageURL(String image){
		String hostUrl = ImagePropertyUtil.getPropertiesValue("httpsImagehost").trim();
		if(StringUtil.isNullOrEmpty(hostUrl)){
			return SystemManager.getInstance()
					.getSystemConfig().getImgServerUrl() + image;
		}
		if(image.startsWith(hostUrl)){
			return image;
		}
		if(hostUrl.endsWith("/") && image.startsWith("/")){
			return hostUrl+image.substring(1);
		}else if(!hostUrl.endsWith("/") && !image.startsWith("/")){
			return hostUrl+"/"+image;
		}else{
			return hostUrl+image;
		}
	}
	
	/**
	* <p>功能描述:获取source文件下的property文件</p>
	* <p>参数：@param name
	* <p>参数：@param key
	* <p>参数：@param fileName
	* <p>参数：@return</p>
	* <p>返回类型：Integer</p>
	 */
	public static Properties loadPropertyFile(String fileName) {
		InputStream is = null;
		Properties props = new Properties();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			classLoader = Util.class.getClassLoader();
		}
		is = classLoader.getResourceAsStream(fileName);
		if (is != null) {
			try {
				props.load(is);

			} catch (IOException e) {
				System.out.println("加载配置文件出错");
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					System.out.println("加载配置文件出错");
				}
			}
		}
		
		return props;
	}
	
	/**
	* <p>功能描述:获取评论的用户名,由于有些评论数据没有用户名，重新取用户名</p>
	* <p>参数：@param cust
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String obtainUserName(Customer cust){
		if(cust == null){
			return "匿名"; 
		}
		//有用户名取用户名
		if(!StringUtil.isNullOrEmpty(cust.getAccount())){
			return cust.getAccount();
		//若没有用户名有nick，取昵称
		}else if(StringUtil.isNullOrEmpty(cust.getAccount()) 
				&& !StringUtil.isNullOrEmpty(cust.getNick())){
			return cust.getNick();
		//若都没有显示匿名
		}else{
			return "匿名";
		}
	}
	
	/**
	* <p>功能描述:校验版本格式是否正确</p>
	* <p>参数：@param version
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean validateVersion(String version){
		if(StringUtil.isNullOrEmpty(version)){
			return false;
		}
		Pattern var = Pattern.compile("^\\w+[.]\\w+[.]\\w+$");
		return var.matcher(version).matches();
	}
	
	public static int convert2Num(String version)
			throws IllegalArgumentException {
		if (StringUtil.isNullOrEmpty(version)) {
			return 0;
		}
		String vs[] = version.split("\\.");
		if (vs.length != 3) {
			if (vs.length == 2 && version.length() == 3) {
				version += ".0";
				vs = version.split("\\.");
			}
			// throw new
			// IllegalArgumentException("版本号必须是以'.'号分隔的三位数，例如 1.2.11");
		}
		StringBuilder _v = new StringBuilder();
		for (int i = 0; i < vs.length; i++) {
			if (i >= 3) {
				break;
			}
			String v = vs[i].trim();
			if (v.length() == 1) {
				v += "0";
			}
			_v.append(v);
		}
		try {
			return Integer.valueOf(_v.toString());
		} catch (Exception ex) {
			throw new IllegalArgumentException("版本号必须是以'.'号分隔的三位数");
		}
	}
	
	/**
	* <p>功能描述:校验苹果是否在审核中</p>
	* <p>参数：@param platfrom
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean isIOSInReview(String platfrom,String currVersion){
		boolean isInReview = false;
		if(StringUtil.isNullOrEmpty(platfrom)){
			return isInReview;
		}
		//校验版本的合法性
		if(!validateVersion(currVersion)){
			return isInReview;
		}
		if(StringUtil.isNullOrEmpty(currVersion)){
			return isInReview;
		}
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReviewCfg = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		//判断iphone是否在审核中：读取配置中的审核信息和版本，若与当前版本相同并且在审核中，表示在审核中
		if (platfrom.equals("iphone")) {
			isInReviewCfg = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
			if (StringUtil.isNotNullNotEmpty(isInReviewCfg)) {
				isInReview = "true".equalsIgnoreCase(isInReviewCfg.trim())
						&& inReviewVersion.equals(currVersion.trim()) ? true
						: false;
			}
		}
		if (platfrom.equals("ipad")) {
			if (StringUtil.isNotNullNotEmpty(isInReviewCfg)) {
				isInReview = "true".equalsIgnoreCase(isInReviewCfg.trim())
						&& inReviewVersion.equals(currVersion.trim()) ? true
						: false;
			}
		}
		return isInReview;
	}
	
	/**
	* <p>功能描述:获取商品下所有sku的总库存</p>
	* <p>参数：@param prod
	* <p>参数：@return</p>
	* <p>返回类型：Integer</p>
	 */
	public static Integer countProductStorage(EbProduct prod){
		int storageNum = 0;
		Set<EbSku> skuSet = prod.getSkus();
		for (EbSku ebSku : skuSet) {
			if(ebSku.getStatus() == EbProductValidStatusEnum.INVALID){
				continue;
			}
			EbStorage storage = ebSku.getStorage();
			if(storage != null && storage.getAvailable() > 0){
				storageNum += storage.getAvailable();
			}
		}
		return storageNum;
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@param type
	* <p>参数：@param isRemote
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getShareURL(EbPosterLinkUrlEnum type,int isRemote,String replaceCode){
		String sharePage = ImagePropertyUtil.getPropertiesValue("shareUrl").trim();
		if(StringUtil.isNullOrEmpty(sharePage)){
			sharePage = "http://entrance.ikan.cn/mobile/mobileAppAddress.html";
		}
		String ret = sharePage+"?isRemote="+isRemote+"&";
		String redirectURl = type.getDescription();
		redirectURl = redirectURl.replaceAll("replaceCode", replaceCode);
		return ret+"redirectUrl="+redirectURl;
	}
	
	/**
	* <p>功能描述:计算邮费，根据邮费规则配置计算邮费</p>
	*  目前配置的规则为：首单满39包邮，非首单满68包邮
	* <p>参数：@param payPrice 支付金额
	* <p>参数：@param userId 用户id
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 * @throws SqlException 
	 */
	public static double computeShipping(double payPrice,int userId) throws SqlException{
		ShippingRuleService shippingServ = SystemInitialization.getApplicationContext().getBean(ShippingRuleService.class);
		Map<Integer,ShippingRule> rule = shippingServ.getShippingRule();
		//首单邮费规则
		ShippingRule first = rule.get(ShippingTypeEnum.FIRSTORDER.getValue());
		//非首单邮费规则
		ShippingRule notFirst = rule.get(ShippingTypeEnum.NOTFIRST.getValue());
		EbOrderService orderServ = SystemInitialization.getApplicationContext().getBean(EbOrderService.class);
		boolean isFirstOrder = orderServ.isFirstOrder(userId);
		//配置了首单邮费规则
		if(first != null && isFirstOrder){
			if (payPrice >= first.getMinPrice()) {
				return first.getShipping();
			} else {
				return first.getStandardShipping() == null ? IConstants.SHIPPING
						: first.getStandardShipping();
			}
		}else{//若没有配置首单邮费规则,按非首单计算
			//配置了邮费规则，按配置计算
			if(notFirst != null){
				if(payPrice >= notFirst.getMinPrice()){
					return notFirst.getShipping();
				}else{
					return notFirst.getStandardShipping() == null ? IConstants.SHIPPING
							: notFirst.getStandardShipping();
				}
			}else{//未配置规则按默认首单39包邮非首单68包邮
				if(isFirstOrder && payPrice >= IConstants.FIRST_ORDER_SHIPPING_PRICE){
					return 0d;
				}else{
					if(payPrice >= IConstants.NOT_FIRST_ORDER_SHIPPING_PRICE){
						return 0d;
					}else{
						return IConstants.SHIPPING;
					}
				}
			}
		}
		
		
	}
	
	/**
	* <p>功能描述:为了兼容上线的版本，对是否使用新的分享地址进行判断</p>
	* iphone:小于等于5.0.3版本，还用以前的URL,其余的版本用新的
	* android:小于等于5.0.1版本，还用以前的URL,其余的版本用新的
	* <p>参数：@param platform
	* <p>参数：@param version
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean isUseNewShareURL(String platform,String version){
		boolean isUseNew = true;
		if(StringUtil.isNullOrEmpty(platform) || StringUtil.isNullOrEmpty(version)){
			return isUseNew;
		}
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			if ("5.0.0".equals(version) || "5.0.1".equals(version)
					|| "5.0.2".equals(version) || "5.0.3".equals(version)) {
				isUseNew = false;
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.gphone)) {
			if ("5.0.0".equals(version) || "5.0.1".equals(version)) {
				isUseNew = false;
			}
		} 
		return isUseNew;
	}
	
	/**
	* <p>功能描述:创建审记内容</p>
	* <p>参数：@param type
	* <p>参数：@param entity
	* <p>参数：@param description
	* <p>参数：@return</p>
	* <p>返回类型：Audit</p>
	 */
	public static Audit getAudit(AuditAction type,Auditable entity,String description,String serialNumber){
		Executor executor = new Executor(Executor.TYPE_ENTRANCE);
		Audit audit = new Audit(type,entity,new Date(),executor,description,serialNumber);
		return audit;
	}
	
	/**
	* <p>功能描述:将统计的实体类加入到conext里</p>
	* <p>参数：@param context
	* <p>参数：@param entity</p>
	* <p>返回类型：void</p>
	 */
	public static void addStatistics(CommandContext context,Object entity){
		if(context == null){
			return;
		}
		if(context.getStatistics() == null){
			return; 
		}
		if(entity == null){
			return;
		}
		
		try {
//			ExclusionStrategy myExclusionStrategy = new ExclusionStrategy() {
//				@Override
//				public boolean shouldSkipField(FieldAttributes fa) {
//					return fa.getName().equals("parent");
//				}
//				
//				@Override
//				public boolean shouldSkipClass(Class<?> clazz) {
//					return false;
//				}
//			};
//			Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy).create();
//			
//			// 如果统计的对象是多个，将多个存到Map里
//			if (context.getStatistics().isMult()) {
//				String className = getClassName(entity.getClass().getName());
//				if (context.getStatistics().getEntity() == null) {
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put(className, entity);
//					context.getStatistics().setEntity(map);
//				} else {
//					if (context.getStatistics().getEntity() instanceof HashMap) {
//						((HashMap) context.getStatistics().getEntity()).put(
//								className, entity);
//					}
//				}
//			} else {
//				if(entity instanceof JSONObject){
//					HashMap map = gson.fromJson(entity.toString(), HashMap.class);
//					context.getStatistics().setEntity(map);
//				}else if( entity instanceof JSONArray){
//					JSONObject json = new JSONObject();
//					json.put("Array", entity);
//					HashMap map = gson.fromJson(json.toString(), HashMap.class);
//					context.getStatistics().setEntity(map);
//				}else if(entity instanceof String){
//					HashMap map = gson.fromJson(entity.toString(), HashMap.class);
//					context.getStatistics().setEntity(map);
//				}else {
//					HashMap map = gson.fromJson(gson.toJson(entity), HashMap.class);
//					context.getStatistics().setEntity(map);
//				}
//			}
			context.getStatistics().setEntity(entity);
		} catch (Exception e) {
			System.out.println("添加统计对象出错；"+e.getMessage());
		}
		
	}
	
	/**
	* <p>功能描述:根据路径获取</p>
	* <p>参数：@param className
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private static String getClassName(String className){
		if(StringUtil.isNullOrEmpty(className)){
			return "";
		}
		if(className.lastIndexOf("$") != -1){
			return className.substring(className.lastIndexOf("$")+1);
		}else{
			return className.substring(className.lastIndexOf(".")+1);
		}
	}
	
	/**
	* <p>功能描述:用于微信支付合并，是否使用新的APPId微信支付</p>
	* <p>参数：@param version
	* <p>参数：@param platform
	* <p>参数：@return</p>
	* <p>返回类型：int  0:代表使用旧版本APPID，1代表使用合并的APPID,2代表移动端网站的微信支付（移动端网站使用公众号支付）</p>
	 */
	public static int isUseNewWXpay(String version,String platform) {
		//使用旧版本微信支付
		int useOldWXPay = 0;
		//使用合并后的微信支付
		int useCombineWXPay = 1;
		//移动端网站公众号微信支付
		int wapMobileWXPay = 2;
		if(StringUtil.isNullOrEmpty(platform) || StringUtil.isNullOrEmpty(version)){
			return useOldWXPay;
		}
		if (MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone) {
			//由于iphone5.0.0发版时版本为4.4.3，所这里处理一下。
			if ("4.4.3".equals(version) || "5.0.0".equals(version) || "5.0.1".equals(version)
					|| "5.0.2".equals(version) || "5.0.3".equals(version)
					|| "5.0.4".equals(version) || "5.0.5".equals(version)) {
				return useOldWXPay;
			}
		} else if (MobileTypeEnum.valueOf(platform) == MobileTypeEnum.gphone) {
			if ("5.0.0".equals(version) || "5.0.1".equals(version) || "5.0.2".equals(version)
					|| "5.0.3".equals(version)) {
				return useOldWXPay;
			}
		} else if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad){
			if ("5.0.0".equals(version)) {
				return useOldWXPay;
			}
		}else if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.wapmobile){
			return wapMobileWXPay;
		}
			
		return useCombineWXPay;
	}
	
	public static JSONObject getHttpClientResult(String url,String params) throws JSONException{
		JSONObject result = null;
		StringBuffer response = new StringBuffer();
		HttpClient client = new HttpClient();
		try {
			URL website = new URL(url+params);
			InputStream in = website.openStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			if(!"".equals(response.toString())){
				result = new JSONObject(response.toString());
			}
			
		} catch (IOException e) {
			
		} finally {
			
		}
		return result;
	}
	
	/**
	* <p>功能描述:保存积分记录</p>
	* <p>参数：@param type 积分类型：0：历史积分 1：非历史积分
	* <p>参数：@param uid
	* <p>参数：@param desc 积分使用说明
	* <p>参数：@param credits
	* <p>参数：@param userOldCredits
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public static void saveCreditRecord(int type,int uid,String desc,int credits,int userOldCredits) throws SqlException{
		CreditServiceV5_0 cs = SystemInitialization.getApplicationContext()
				.getBean(CreditServiceV5_0.class);
		CreditsRecord creditRec = cs.findCreditRecordByType(0, uid);
		//将已有的用户积分保存为历史积分
		if(creditRec == null){
			cs.saveCreditRecord(0, uid, "历史积分", userOldCredits);
		}
		//若积分大于0，保存积分来源记录
		if(credits != 0){
			cs.saveCreditRecord(type, uid, desc, credits);
		}
	}
	
	/**
	* <p>功能描述:创建用户名:ikan+手机号+6位随机数字，手机号为空</p>
	* <p>参数：@param phone
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getCustomerAccount(String phone){
		if(StringUtil.isNullOrEmpty(phone)){
			return "ikan"+createRandom(16);
		}
		return "ikan"+phone+createRandom(4);
	}
	
	/**
	* <p>功能描述:生成指定长度的随机数</p>
	* <p>参数：@param length
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
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
	
	/**
	* <p>功能描述:发送短信</p>
	* <p>参数：@param phone
	* <p>参数：@param code
	* <p>参数：@param type
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 * @throws SqlException 
	 */
	public static String sendSms(String phone,String code,int type) throws Exception{
		String template = getSmsContentTemplate(type);
		SendSmsConfigService sendServ = SystemInitialization.getApplicationContext().getBean(SendSmsConfigService.class);
		List<SendSmsConfig> configs = sendServ.getSendSmsConfig();
		SendSmsTypeEnum platType = null;
		if(configs == null || configs.size() == 0){
			int rd = Math.random() > 0.5 ? 1 : 0;
			platType = SendSmsTypeEnum.valueOf(rd);
		}else{
			SendSmsConfig smsConfig = configs.get(0);
			platType = smsConfig.getPlatType();
		}
		String isSuccess = sendSmsByPlat(platType, phone, template.replaceAll("CODE",code));
		System.err.println("send sms code:"+code+",successflag:"+isSuccess);
		return isSuccess;
	}
	
	/**
	* <p>功能描述:根据配置的发送短信平台发送短信</p>
	* <p>参数：@param platType
	* <p>参数：@param phone
	* <p>参数：@param content
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String sendSmsByPlat(SendSmsTypeEnum platType,String phone,String content){
		if(platType == null){
			return "fail";
		}
		if(platType == SendSmsTypeEnum.DFWR){
			String isSuccess = SmsHandler.DFWRSendSms(phone,content);
			isSuccess = getSuccessFlag(isSuccess);
			if("-6".equals(isSuccess)){
				//余额不足，发送余额不足邮件
				try {
					sendMail(IConstants.EMAIL_PERSON, "", "东方网润短信平台余额不足，请尽快续费！", "insufficient_balance");
					isSuccess = SmsHandler.HL95SendMsm(phone,content);
					return isSuccess;
				} catch (Exception e) {
				}
			}
			return isSuccess;
		}else if(platType == SendSmsTypeEnum.HLJW){
			String isSuccess = SmsHandler.HL95SendMsm(phone,content);
			if(isSuccess.equals("00")){
				return "0";
			}else if(isSuccess.equals("5")){
				//余额不足，发送余额不足邮件
				try {
					sendMail(IConstants.EMAIL_PERSON, "", "鸿联九五短信平台余额不足，请尽快续费！", "insufficient_balance");
					isSuccess = SmsHandler.DFWRSendSms(phone,content);
					return getSuccessFlag(isSuccess);
				} catch (Exception e) {
				}
			}
			return isSuccess;
		}
		return "";
	}
	
	/**
	* <p>功能描述:发送邮件</p>
	* <p>参数：@param email
	* <p>参数：@param account
	* <p>参数：@param validateNum
	* <p>参数：@param template
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean sendMail(String email,String account,String validateNum,String template) throws Exception{
		// 邮件内容
		String mailContent = "";
		mailContent = MailServiceFactory.getMailContentService()
					.readBindEmailHtmlContent(template,
							account == null? "" : account, validateNum);
		boolean sendOk = MailFacade.sendMail(mailContent, "“爱看”帐户邮箱验证",
					email);
		return sendOk;
	}
	
	
	/**
	* <p>功能描述:获取短信发送成功状态，东方网润接口返回格式为：0,28918291逗号前面是状态码</p>
	* <p>参数：@param isSuccess
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private static String getSuccessFlag(String isSuccess){
		if(StringUtil.isNullOrEmpty(isSuccess)){
			return "fail";
		}
		String[] ret = isSuccess.split(",");
		if(ret == null || ret.length == 0){
			return isSuccess;
		}
		return ret[0];
	}
	
	/**
	* <p>功能描述:获取短信验证的模板</p>
	* <p>参数：@param type
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getSmsContentTemplate(int type){
		if(checkTypeRight(type)){
			return ValidateTypeEnum.valueOf(type).getDescription();
		}
		return SmsConfig.SMS_MSG_CONTENT;
	}
	
	/**
	* <p>功能描述:判断验证类型是否正确</p>
	* <p>参数：@param type
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean checkTypeRight(int type){
		switch (type) {
		case 1:
			return true;
		case 3:
			return true;
		case 5:
			return true;
		case 7:
			return true;
		case 10:
			return true;
		case 11:
			return true;
		case 13:
			return true;
		case 14:
			return true;
		default:
			return false;
		}
	}
	
	/**
	* <p>功能描述:获取QQ用户信息</p>
	* <p>参数：@param appId
	* <p>参数：@param openid
	* <p>参数：@param token
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static JSONObject getQQUserInfo(String appId,String openid,String token){
		String url = IConstants.QQ_GET_USER_INFO;
		if (StringUtil.isNullOrEmpty(appId) || StringUtil.isNullOrEmpty(openid)
				|| StringUtil.isNullOrEmpty(token)) {
			return null;
		}
		JSONObject userInfo = new JSONObject();
		url = url.replaceAll("ACCESS_TOKEN", token);
		url = url.replaceAll("OAUTH_CONSUMER_KEY", appId);
		url = url.replaceAll("OPENID", openid);
		try {
			userInfo = getHttpClientResult(url, "");
		} catch (JSONException e) {
			
		}
		return userInfo;
	}
	
	/**
	* <p>功能描述:判时间与当前时间比，失效还是未开始</p>
	* <p>参数：@param startDate
	* <p>参数：@param endDate
	* <p>参数：@return</p>
	* <p>返回类型：int -1:传入为空，0表示未开始，1表示有效期之内 2:不在有效期之内</p>
	 */
	public static int checkDateValidate(Date startDate,Date endDate){
		if(startDate == null || endDate == null){
			return -1;
		}
		Date now = new Date();
		if(now.before(startDate)){
			return 0;
		}
		if(startDate.before(now) && now.before(endDate)){
			return 1;
		}
		return 2;
	}
	
	/**
	* 功能描述:根据版本和平台将内容里http替换成https
	* 参数：@param content
	* 参数：@param version
	* 参数：@param platform
	* 参数：@return
	* 返回类型:String
	 */
	public static String replaceHttp2Https(String content,String version,String platform){
		if(StringUtil.isNullOrEmpty(content)){
			return "";
		}
		String httpFlag = "http://";
		String httpsFlag = "https://";
		if(content.indexOf(httpFlag) == -1){
			return content;
		}
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			int startVer = convert2Num(IConstants.IPHONE_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return content.replaceAll("http://images.kandongman.com.cn", "http://images.ikan.cn").replaceAll(httpFlag, httpsFlag);
			}else{
				return content;
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad)) {
			int startVer = convert2Num(IConstants.IPAD_HTTPS_VERSION);
			int verNumber = convert2Num(version);
			if(verNumber > startVer){
				return content.replaceAll("http://images.kandongman.com.cn", "http://images.ikan.cn").replaceAll(httpFlag, httpsFlag);
			}else{
				return content;
			}
		}else{
			return content;
		}
	}
	
	
	/**
	* 功能描述:将内容里http替换成https
	* 参数：@param content
	* 参数：@param version
	* 参数：@param platform
	* 参数：@return
	* 返回类型:String
	 */
	public static String replaceHttp2Https(String content){
		if(StringUtil.isNullOrEmpty(content)){
			return "";
		}
		String httpFlag = "http://";
		String httpsFlag = "https://";
		if(content.indexOf(httpFlag) == -1){
			return content;
		}
		return content.replaceAll(httpFlag, httpsFlag);
	}
}
