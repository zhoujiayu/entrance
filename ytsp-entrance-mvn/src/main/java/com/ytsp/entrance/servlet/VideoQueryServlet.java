package com.ytsp.entrance.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.vo.VideoVO;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;

public class VideoQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 5448580728810403570L;
	
	private static final Logger logger = Logger.getLogger(VideoQueryServlet.class);

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String status = "ok";
		JSONObject json = new JSONObject();
		String episode = request.getParameter("episode");
		String albumId = request.getParameter("albumId");
		PrintWriter writer = response.getWriter();
		ApplicationContext ctx = SystemInitialization
				.getApplicationContext();
		AlbumServiceV5_0 albumServ = ctx.getBean(AlbumServiceV5_0.class);
		try {
			if (StringUtil.isNullOrEmpty(albumId) || StringUtil.isNullOrEmpty(episode)) {
				status = "fail";
			}else{
				VideoVO video = albumServ.getVideoByAlbumIdAndEpisode(Integer.parseInt(albumId),Integer.parseInt(episode),"","");
				if(video != null){
					json.put("videoURL", video.getV720());
				}
			}
			json.put("status", status);
		} catch (Exception e) {
			try {
				json.put("status", "fail");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			logger.error("ERROR query video by albumId error ,albumid = "
					+ albumId +";episode="+episode);
		}
		String ret = json.toString();
		writer.write(ret);
	}

}
