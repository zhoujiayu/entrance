package com.ytsp.entrance.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.common.util.FileUtil;
import com.ytsp.entrance.system.SystemConfig;
import com.ytsp.entrance.system.SystemManager;

public class DmsUploadServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(UploadServlet.class);

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		String status = "ok";
		JSONObject json = new JSONObject();
		PrintWriter writer = response.getWriter();
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			status = "fail";
			String content = ">> It is not a file upload request!";
			logger.error(content);
			
			try {
				json.put("content", content);
			} catch (JSONException e) {
				logger.error("", e);
			}
		} else {
			String _savePath = request.getParameter("savepath");
			String _picName = request.getParameter("picName");
//			String savePath = request.getSession().getServletContext().getRealPath(_savePath);
			SystemConfig sysConfig = SystemManager.getInstance().getSystemConfig();
			String savePath = sysConfig.getImgSavePath() + File.separator + _savePath;
			
			// create factory and file cleanup tracker
			FileCleaningTracker tracker = FileCleanerCleanup.getFileCleaningTracker(getServletContext());
			File tmpDir = new File(savePath + File.separator + "tmp");
			if(!tmpDir.exists()){
				tmpDir.mkdirs();
			}
			
			DiskFileItemFactory factory = new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, tmpDir);
			factory.setFileCleaningTracker(tracker);

			// save upload file to disk
			ServletFileUpload upload = new ServletFileUpload(factory);
			JSONArray urls = new JSONArray();
			try {
				List<FileItem> items = upload.parseRequest(request);
				String _fileName = null;
				File savefile = null;
				for (FileItem item : items) {
					if (!item.isFormField()) {
						// 确定是文件而不是一个普通的表单字段
						_fileName = item.getName();
						String suffix = FileUtil.getSuffix(_fileName);
						String fileName = _picName + "." + suffix;
						savefile = new File(savePath + File.separator + fileName);
						item.write(savefile);
						writer.flush();
						
						urls.put(_savePath + "/" + fileName);
						logger.info(">> [save] " + savefile.getAbsolutePath());
					}
				}
				
				
				json.put("content", urls);
			} catch (Exception e) {
				status = "fail";
				String content = ">> Save upload file fail!";
				logger.error(content, e);
				
				try {
					json.put("content", content);
				} catch (JSONException ex) {
					logger.error("", ex);
				}
			}
		}

		try {
			json.put("status", status);
		} catch (JSONException e) {
			logger.error("", e);
		}
		
		String ret = json.toString();
		writer.write(ret);
	}
	
	
}
