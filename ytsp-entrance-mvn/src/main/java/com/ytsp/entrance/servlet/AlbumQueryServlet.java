package com.ytsp.entrance.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;

public class AlbumQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 5448580728810403570L;
	
	private static final Logger logger = Logger.getLogger(AlbumQueryServlet.class);

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String status = "ok";
		JSONObject json = new JSONObject();
		String albumId = request.getParameter("albumId");
		PrintWriter writer = response.getWriter();
		ApplicationContext ctx = SystemInitialization
				.getApplicationContext();
		AlbumServiceV5_0 albumServ = ctx.getBean(AlbumServiceV5_0.class);
		try {
			if (StringUtil.isNullOrEmpty(albumId)) {
				status = "fail";
			}else{
				List<Integer> ids = getAlbumIds(albumId);
				json.put("albums", albumServ.getAlbumByIds(ids));
			}
			json.put("status", status);
		} catch (Exception e) {
			try {
				json.put("status", "fail");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			logger.error("ERROR query album by Id error ,albumid = "
					+ albumId);
		}
		String ret = json.toString();
		writer.write(ret);
	}
	
	/**
	* <p>功能描述:校验请求的id，将id封装为List<Integer></p>
	* <p>参数：@param albumId
	* <p>参数：@return</p>
	* <p>返回类型：List<Integer></p>
	 */
	private List<Integer> getAlbumIds(String albumId){
		if(albumId == null){
			return null;
		}
		if(albumId.indexOf(",") == -1){
			return null;
		}
		String[] ids = albumId.split(",");
		List<Integer> idsList = new ArrayList<Integer>();
		String regex = "^\\d+$";
		for (int i = 0; i < ids.length; i++) {
			if(!ids[i].matches(regex)){
				continue;
			}
			idsList.add(new Integer(ids[i]));
		}
		return idsList;
	}

}
