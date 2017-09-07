package com.ytsp.entrance.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import com.google.gson.Gson;
import com.statistics.entity.Header;
import com.statistics.entity.Location;
import com.statistics.entity.Statistics;
import com.statistics.enums.PageTypeEnum;
import com.statistics.service.StatisticsService;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.LogPVDao;
import com.ytsp.db.domain.LogPV;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.BodyInfo;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.command.base.CommandHandler;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.HashKeyGeneration;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.MD5;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.WebUtils;

/**
 * @author GENE
 * @description 客户端http请求入口
 */
public class Entrance extends HttpServlet {
	private static final Logger logger = Logger.getLogger(Entrance.class);

	private static final long serialVersionUID = 1L;

	private boolean checkHeadInfo(String his) {
		if (!StringUtil.isNotNullNotEmpty(his))
			return false;
		if (!his.matches("\\{.*\\}"))
			return false;
		return true;
	}

	//TODO 临时使用
	private static  Map<String,Integer> redirectMap = new HashMap<String,Integer>();
	
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String his = request.getHeader(CommandList.HEAD_INFO);
		
		// 监控需求
		ExecuteResult er = null;
		Statistics s = null;
//		long start = System.currentTimeMillis();
		StringBuffer logBuffer = new StringBuffer();
		logBuffer.append("HEAD_INFO:"+his+"\r\n");
		//用于统计时密钥正不正确
		boolean isKeyValidate = false;
		if (request.getParameter("MONITOR") != null
				&& request.getParameter("MONITOR").equals("ALI")) {
			CommandHandler handler = CommandHandler.getInstance();
			CommandContext context = new CommandContext();
			context.setRequest(request);
			context.setResponse(response);
			context.setHead(new HeadInfo() {
				{
					commandCode = 100000;
				}
			});
			context.setBody(new BodyInfo());
			List<ExecuteResult> ers = handler.execute(context);
			// 只返回一个命令结果
			if (ers.size() > 0) {
				er = ers.get(0);
			}
			logger.debug(er.getStatus() + ":" + er.getStatusMsg());
		} else {
			if (checkHeadInfo(his)) {
				HeadInfo head = null;
				boolean flag = true;
				try {
					head = new HeadInfo(his);
					// 从head中取ip地址
//					System.err.println(head.commandCode);
					String real_ip = request.getHeader("X-Real-IP");
					if (real_ip == null || real_ip.isEmpty()) {
						head.ip = WebUtils.getRemoteAddress(request);
					} else {
						head.ip = real_ip;
					}
				} catch (Exception e) {
					flag = false;
					logger.error("parse head json info error! head info : "
							+ his, e);
					// er = new
					// ExecuteResult(CommandList.RESPONSE_STATUS_HEAD_JSON_ERROR,
					// "请求报头错误！", null, null);
				}
				if (flag) {
					// 记录pv
					if (checkPV(head.commandCode)) {
						try {
//							if(head.commandCode == CommandList.CMD_VERSION_LAST
//									&&(head.platform.equals("gpad")||head.platform.equals("gphone"))){
//								int today = (int) (System.currentTimeMillis()/86400000);
//								if(redirectMap.get(head.getUniqueId())==null||
//										redirectMap.get(head.getUniqueId())!=today){
//									response.sendRedirect("http://api.power-flying.com/s/c/Meb?idfa="+head.getUniqueId());
//									redirectMap.put(head.getUniqueId(), today);
//									head.params="click";
//								}else if(Math.random()<0.03){
//									response.sendRedirect("http://api.power-flying.com/s/c/Meb?idfa="+head.getUniqueId());
//									redirectMap.put(head.getUniqueId(), today);
//									head.params="click";
//								}
//							}
							final LogPVDao pvDao = (LogPVDao) SystemInitialization
									.getApplicationContext().getBean("logPVDaoYTSPLOG");
							final LogPV pv = new LogPV();
							pv.setTime(new Date());
							if (head != null) {
								pv.setIp(head.ip);
								pv.setCommand(String.valueOf(head.commandCode));
								pv.setNumber(head.getUniqueId());
								pv.setTerminalFrom(head.getOtherInfo());
								pv.setTerminalType(head.getPlatform());
								pv.setTerminalVersion(head.getVersion());
								pv.setParams(head.getParams());
							}
							LowPriorityExecutor.execLog(new Runnable() {
								@Override
								public void run() {
									try {
										pvDao.save(pv);
									} catch (SqlException e) {
										logger.error("PV command log error", e);
									}
								}
							});
						} catch (Exception ex) {
							logger.error("Log pv error : ", ex);
						}
					}
					if(head.platform.equals("gpadtv"))
						return;
					if (is4PVLog(head.commandCode))
						return;
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(request.getInputStream()));
					StringBuilder bodystr = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						bodystr.append(line);
					}
					BodyInfo body = null;
					try {
						logBuffer.append("BODY:"+bodystr.toString());
//						logger.info("HEAD_INFO:"+his+"\r\n"+"BODY:"+new Gson().toJson(body));
						body = new BodyInfo(bodystr.toString());
					} catch (Exception e) {
						logger.error(
								"parse body json info error : "
										+ bodystr.toString(), e);
						// er = new
						// ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR,
						// "请求体错误！", null, null);
					}
					//校验码是否一致
					isKeyValidate = isKeyValidate(head);
//					isKeyValidate = false;
					if(isKeyValidate){
						s = buildStatistics(head, body, his);
					}
					
					CommandHandler handler = CommandHandler.getInstance();
					CommandContext context = new CommandContext();
					context.setStatistics(s);
					context.setRequest(request);
					context.setResponse(response);
					context.setHead(head);
					context.setBody(body);
					List<ExecuteResult> ers = handler.execute(context);
//					sb.append("CommandCode:"+head.getCommandCode());
					// 只返回一个命令结果
					if (ers.size() > 0) {
						er = ers.get(0);
					}
				}
			}
			if (er == null) {
				er = new ExecuteResult(
						CommandList.RESPONSE_STATUS_UNRECOGNIZED, "不可识别命令！",
						null, null);
				logger.error("不可识别命令："+his);
			}
		}

		// response.setHeader(RESPONSECODE, er.getStatus());
		// response.getWriter().write(er.getResult());
		JSONObject jo = new JSONObject();
		try {
			jo.put(CommandList.RESPONSE_CODE, er.getStatus());
			jo.put(CommandList.RESPONSE_CODE_INFO, er.getStatusMsg());
			jo.put(CommandList.RESPONSE_BODY, er.getResult());
		} catch (JSONException e) {
			logger.error("generate return json object error!", e);
		}
		
		response.setContentType("text/json;charset=UTF-8");
		response.getWriter().write(jo.toString());
		logger.info(logBuffer.toString());
		long second = System.currentTimeMillis();
		try {
			//只有前端的校验码与后端的校验码一致时才会统计
			if(isKeyValidate){
//				System.out.println("commandCode:"+s.getCommandCode()+";用时："+(second - start));
			
				/**
				 * 将数据参数写入mongo数据库中
				 */
//				s.setResponseData(jo.toString());
//				saveStatistics(s);
				
				
//				System.out.println("commandCode:"+s.getCommandCode()+";用时："+(System.currentTimeMillis() - second));
			}
		} catch (Exception e) {
			logger.error("保存统计内容出错："+e.getMessage());
		}
//		System.out.println(sb.toString()+";"+jo.toString());
		// HttpSession session = request.getSession();
		// Object user = session.getAttribute("SESSION_USER");
		// if(user == null){
		// user = "I'm Gene! And my jsessionid is " + session.getId();
		// session.setAttribute("SESSION_USER", user);
		// }
		//
		// response.addHeader("coolmind", "excellent!");
		// response.getWriter().write(user.toString());
	}
	
	/**
	* <p>功能描述:构建统计实体类</p>
	* <p>参数：@param headInfo
	* <p>参数：@return</p>
	* <p>返回类型：Statistics</p>
	 * @throws JSONException 
	 */
	private Statistics buildStatistics(HeadInfo headInfo,BodyInfo bodyInfo,String reqParam){
		Statistics statistics = new Statistics();
		try {
			JSONObject reqObj = new JSONObject(reqParam);
			Header head = buildStatisticsHeader(headInfo, reqObj);
			statistics.setHead(head);
			statistics.setSecretKey(headInfo.getSecretKey());
			statistics.setCommandCode(String.valueOf(headInfo.getCommandCode()));
			//设置创建时间
			statistics.setTime(new Date());
			//设置页面类型
			if (!reqObj.isNull("pageType")) {
				String pageType = reqObj.optString("pageType");
				PageTypeEnum pageTypeEnum = PageTypeEnum.valueOf(Integer.parseInt(pageType));
				statistics.setPageType(pageType);
				statistics.setPageDesc(pageTypeEnum.getDescription());
			}
			//设置位置
			if (!reqObj.isNull("location")) {
				statistics.setLocation(reqObj.optString("location"));
				//获取位置名称
				statistics.setLocationName(Location.getLocation(reqObj.optString("location")));
			}
//			System.out.println("commandCode:" + headInfo.getCommandCode()
//					+ ";pageType:" + statistics.getPageType() + ";pageDesc:"
//					+ statistics.getPageDesc() + ";location:"
//					+ statistics.getLocation() + ";LocationName:"
//					+ statistics.getLocationName());
			//设置页面类型
			if (bodyInfo != null && bodyInfo.getBodyObject() != null
					&& bodyInfo.getBodyObject().length() > 0) {
				Gson gson = new Gson();
				HashMap paramMap = gson.fromJson(bodyInfo.getBodyObject()
						.toString(), HashMap.class);
				statistics.setBizParam(paramMap);
			}
			//设置mongodb分片所需要的 hashKey:取值为int%256
			int hashKey = HashKeyGeneration.getInstance().getHashKey();
//			System.out.println("hashKey:"+hashKey);
			statistics.set_hashkey(hashKey);
		} catch (Exception e) {
			logger.error(" Build Statistics error:"+e.getMessage());
		}
		return statistics;
	}
	
	/**
	* <p>功能描述:校验前端加密与后端是否一致platform+appVersion+userId+commandCode+timestamp+"imagemedia"的MD5值</p>
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isKeyValidate(HeadInfo head){
		boolean isValidate = false;
		if(!isCurrVersionStatistics(head)){
			return isValidate;
		}
		if(StringUtil.isNullOrEmpty(head.getSecretKey())){
			return isValidate;
		}
		String secretKey = head.getPlatform()+head.getVersion()+head.getUid()+head.getCommandCode()+head.getTimestamp()+"imagemedia";
		if(MD5.code(secretKey).equals(head.getSecretKey())){
			return true;
		}
		return isValidate;
	}
	
	/**
	* <p>功能描述:当前版本是否需要统计</p>
	* <p>ipad:不添加统计，android:版本小于等于5.0.2不添加统计；iphone:版本小于等于5.0.4不添加统计</p>
	* <p>参数：@param version
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isCurrVersionStatistics(HeadInfo headInfo){
		String version = headInfo.getVersion();
		//校验版本格式是否正确
		if(!Util.validateVersion(version)){
			return false;
		}
		String platform = headInfo.getPlatform();
		if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad){
			if (version.trim().equals("5.0.0")
					|| version.trim().equals("5.0.1")) {
				return false;
			}
		}else if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.gphone){
			if (version.trim().equals("5.0.0")
					|| version.trim().equals("5.0.1")
					|| version.trim().equals("5.0.2")) {
				return false;
			}
		}else if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone){
			if (version.trim().equals("4.4.3") || version.trim().equals("5.0.0")
					|| version.trim().equals("5.0.1")
					|| version.trim().equals("5.0.2")
					|| version.trim().equals("5.0.3")
					|| version.trim().equals("5.0.4")) {
				return false;
			}
		}else if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.wapmobile){
			return false;
		}
		return true;
	}
	
	/**
	* <p>功能描述:构建统计head</p>
	* <p>参数：@param headInfo
	* <p>参数：@param json
	* <p>参数：@return</p>
	* <p>返回类型：Header</p>
	 * @throws JSONException 
	 */
	private Header buildStatisticsHeader(HeadInfo headInfo,JSONObject reqObj){
		Header header = new Header();
		try {
			//平台
			header.setPlatform(headInfo.getPlatform());
			header.setAppVersion(headInfo.getVersion());
			//系统版本
			if (!reqObj.isNull("systemVersion")) {
				header.setSystemVersion(reqObj.optString("systemVersion"));
			}
			//H5版本
			if (!reqObj.isNull("H5Version")) {
				header.setH5Version(reqObj.optString("H5Version"));
			}
			//用户创建时间
			if (!reqObj.isNull("userCreateTime")) {
				header.setUserCreateTime(reqObj.optString("userCreateTime"));
			}
			header.setUserId(String.valueOf(headInfo.getUid()));
			//用户名
			if (!reqObj.isNull("userAccount")) {
				String account = URLDecoder.decode(reqObj.optString("userAccount"), "utf-8");
				header.setUserAccount(account);
			}
			//设置是否为vip用户标识
			if (!reqObj.isNull("isVip")) {
				header.setIsVip(reqObj.optString("isVip"));
			}
			//ip地址
			header.setIp(headInfo.getIp());
			//分辨率
			if(headInfo.getScreenHeight() > 0 && headInfo.getScreenWidth() > 0){
				header.setScreenResolution(headInfo.getScreenWidth()+"x"+headInfo.getScreenHeight());
			}
			//设备厂商
			if (!reqObj.isNull("deviceManufacturer")) {
				header.setDeviceManufacturer(reqObj.optString("deviceManufacturer"));
			}
			//设备型号
			if (!reqObj.isNull("deviceModel")) {
				header.setDeviceModel(reqObj.optString("deviceModel"));
			}
			//渠道标识
			header.setChannelSign(headInfo.getOtherInfo().trim());
			//手机imsi卡
			if (!reqObj.isNull("imsi")) {
				header.setImsi(reqObj.optString("imsi"));
			}
		
		} catch (Exception e) {
			logger.error(" Build Statistics Header error:"+e.getMessage());
		}
		return header;
	}
	
	/**
	* <p>功能描述:将实体类保存到</p>
	* <p>参数：@param statistics</p>
	* <p>返回类型：void</p>
	 */
	private void saveStatistics(Statistics statistics){
		ApplicationContext ctx = SystemInitialization
				.getApplicationContext();
		StatisticsService StatisticsServ = ctx.getBean(StatisticsService.class);
		StatisticsServ.insertStatistics(statistics);
	}
	
	private boolean is4PVLog(int commandCode) {
		return commandCode == CommandList.CMD_LOG_PV_PUSH_MSG
				|| commandCode == CommandList.CMD_LOG_PV_RECOMMENT_IMG
				|| commandCode == CommandList.CMD_LOG_PV_AD_LAUNCH;
	}

	private boolean checkPV(int cmd) {
		return (cmd == CommandList.CMD_ACTIVITY_LIST
				||cmd == CommandList.CMD_VERSION_LAST
				|| cmd == CommandList.CMD_REGIST
				|| cmd == CommandList.CMD_LOGIN
				|| cmd == CommandList.CMD_LOGOUT
				|| cmd == CommandList.CMD_MODIFY_PWD
				|| cmd == CommandList.CMD_VIDEO_PLAY
				|| cmd == CommandList.CMD_CUSTOMER_SAVE_PARENT
				|| cmd == CommandList.CMD_CUSTOMER_SAVE_BABY
				|| cmd == CommandList.CMD_POINT_VALIDATA_APP_RECGARGE
				|| cmd == CommandList.CMD_POINT_VALIDATA_CARD_RECGARGE
				|| cmd == CommandList.CMD_MEMBER_RECHARGE
				|| cmd == CommandList.CMD_MEMBER_VALIDATE_APP_RECHGARGE
				|| cmd == CommandList.CMD_MEMBER_VIDEO_PLAY_V3_1
				|| cmd == CommandList.CMD_RECOMMEND_LIST
				|| cmd == CommandList.CMD_MEMBER_VIDEO_DOWNLOAD
				|| cmd == CommandList.CMD_MEMBER_VIDEO_PLAY
				|| cmd == CommandList.CMD_LOG_PV_PUSH_MSG
				|| cmd == CommandList.CMD_LOG_PV_RECOMMENT_IMG
				|| cmd == CommandList.CMD_RECOMMEND_LIST_V4
				|| cmd == CommandList.CMD_AD_INFO_READ
						|| cmd == CommandList.CMD_EB_SUBMITCOMMENT
						|| cmd == CommandList.CMD_LOGIN_4_0
								|| cmd == CommandList.CMD_EB_ACTIVITY_GET
								|| cmd == CommandList.CMD_THIRD_PLATFORM_LOGIN
										|| cmd == CommandList.CMD_EB_EBSECKILL_GETBYACTIVITYID
												|| cmd == CommandList.CMD_EB_PRODUCT_SKU_GETBYPRODUCODE
														||cmd > 5000)&&cmd!=CommandList.CMD_PUSHMESSAGE_LAST_V5;//把目前5.0版本以后的命令全纪录下来
				
	}
	
}
