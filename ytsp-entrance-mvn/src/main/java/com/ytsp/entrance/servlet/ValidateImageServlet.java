package com.ytsp.entrance.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.ImageVerify;
import com.ytsp.db.enums.ValidateTypeEnum;
import com.ytsp.entrance.command.base.BodyInfo;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.ImageVerifyService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.VerifyCodeUtils;

public class ValidateImageServlet  extends HttpServlet{
	
	private static final Logger logger = Logger.getLogger(ValidateImageServlet.class);
	
	private static final long serialVersionUID = -2708980032414465438L;

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		
		String his = request.getHeader(CommandList.HEAD_INFO);
		int w = 200, h = 80;
		// 生成随机字串
		String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
		if(checkHeadInfo(his)){
			try {
				HeadInfo head = new HeadInfo(his);
				int useRange = 0;
				//构建Body
				BodyInfo bodyInfo = buildBodyInfo(request);
				if(bodyInfo.getBodyObject().has("width")){
					w = bodyInfo.getBodyObject().optInt("width");
				}
				if(StringUtil.isNotNullNotEmpty(request.getParameter("height"))){
					h = bodyInfo.getBodyObject().optInt("height");
				}
				if(StringUtil.isNotNullNotEmpty(request.getParameter("useRange"))){
					useRange = bodyInfo.getBodyObject().optInt("useRange");
				}
				// 存入会话session
//				HttpSession session = request.getSession(true);
//				session.setAttribute("rand", verifyCode.toLowerCase());
				ImageVerify imageVerify = buildImageVerify(bodyInfo,head,verifyCode,useRange);
				saveImageVerify(imageVerify);
				// 生成图片
				VerifyCodeUtils.outputImage(w, h, response.getOutputStream(),
						verifyCode);
			} catch (Exception e) {
				logger.error("create image error : " ,e);
			}
		}else{
			logger.error("head param format error ");
			String device = "";
			int useRange = 0;
			if(StringUtil.isNotNullNotEmpty(request.getParameter("width"))){
				w = Integer.parseInt(request.getParameter("width"));
			}
			if(StringUtil.isNotNullNotEmpty(request.getParameter("height"))){
				h = Integer.parseInt(request.getParameter("height"));
			}
			if(StringUtil.isNotNullNotEmpty(request.getParameter("device"))){
				device = request.getParameter("device");
			}
			if(StringUtil.isNotNullNotEmpty(request.getParameter("useRange"))){
				useRange = Integer.parseInt(request.getParameter("useRange"));
			}
			ImageVerify imageVerify = buildImageVerify(device, 0, verifyCode,useRange);
			saveImageVerify(imageVerify);
			// 生成图片
			VerifyCodeUtils.outputImage(w, h, response.getOutputStream(),
					verifyCode);
		}
	}
	
	/**
	* <p>功能描述:保存图片验证</p>
	* <p>参数：@param body
	* <p>参数：@param head</p>
	* <p>返回类型：void</p>
	 */
	private void saveImageVerify(ImageVerify imageVerify){
		try {
			ImageVerifyService imageServ = SystemInitialization.getApplicationContext().getBean(ImageVerifyService.class);
			if(imageVerify != null){
				imageServ.saveImageVerify(imageVerify);
			}
		} catch (Exception e) {
			logger.error("save imageVerify error ",e);
		}
	}
	
	/**
	* <p>功能描述:创建图片验证实体类</p>
	* <p>参数：@return</p>
	* <p>返回类型：ImageVerify</p>
	 */
	private ImageVerify buildImageVerify(BodyInfo body,HeadInfo head,String code,int type){
		ImageVerify imgVerify = new ImageVerify();
		imgVerify.setDeviceNumber(head.getUniqueId());
		imgVerify.setCreateDate(new Date());
		imgVerify.setUserId(head.getUid());
		imgVerify.setUseType(0);
		imgVerify.setCode(code);
		imgVerify.setStatus(0);
		imgVerify.setUseRange(ValidateTypeEnum.valueOf(type));
		return imgVerify;
	}
	
	/**
	* <p>功能描述:创建图片验证实体类</p>
	* <p>参数：@return</p>
	* <p>返回类型：ImageVerify</p>
	 */
	private ImageVerify buildImageVerify(String device,int userId,String code,int type){
		ImageVerify imgVerify = new ImageVerify();
		imgVerify.setDeviceNumber(device);
		imgVerify.setCreateDate(new Date());
		imgVerify.setUserId(userId);
		imgVerify.setUseType(0);
		imgVerify.setCode(code);
		imgVerify.setStatus(0);
		imgVerify.setUseRange(ValidateTypeEnum.valueOf(type));
		return imgVerify;
	}
	
	
	/**
	* <p>功能描述:校验请求头格式是否正确</p>
	* <p>参数：@param his
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean checkHeadInfo(String his) {
		if (!StringUtil.isNotNullNotEmpty(his))
			return false;
		if (!his.matches("\\{.*\\}"))
			return false;
		return true;
	}
	
	/**
	* <p>功能描述:构建BodyInfo</p>
	* <p>参数：@param request
	* <p>参数：@return</p>
	* <p>返回类型：BodyInfo</p>
	 */
	private BodyInfo buildBodyInfo(HttpServletRequest request){
		StringBuilder bodystr = new StringBuilder();
		String line = null;
		BodyInfo body = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					request.getInputStream()));
			while ((line = reader.readLine()) != null) {
				bodystr.append(line);
			}
			body = new BodyInfo(bodystr.toString());
		} catch (Exception e) {
			logger.error("parse body json info error : " + bodystr.toString(),
					e);
		}
		return body;
	}
	
}
