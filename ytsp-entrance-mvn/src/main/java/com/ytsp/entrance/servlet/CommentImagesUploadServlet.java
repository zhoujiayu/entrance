package com.ytsp.entrance.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

public class CommentImagesUploadServlet extends HttpServlet {
	private static final Logger logger = Logger
			.getLogger(CommentImagesUploadServlet.class);

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/json;charset=UTF-8");
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
			Map<String, List<JSONObject>> map = new HashMap<String, List<JSONObject>>();
			JSONArray urls = new JSONArray();
			try {
				SystemConfig sysConfig = SystemManager.getInstance()
						.getSystemConfig();
				String appImgRootpPath = "ximages/appupload";
				// String savePath = sysConfig + File.separator +
				// appImgRootpPath ;
				// String savePath = this.getServletConfig().getServletContext()
				// .getRealPath("/")
				// + appImgRootpPath;
				String savePath = sysConfig.getImgSavePath() + File.separator
						+ appImgRootpPath;
				// create factory and file cleanup tracker
				FileCleaningTracker tracker = FileCleanerCleanup
						.getFileCleaningTracker(getServletContext());
				File tmpDir = new File(savePath + File.separator + "tmp");
				if (!tmpDir.exists()) {
					tmpDir.mkdirs();
				}

				DiskFileItemFactory factory = new DiskFileItemFactory(
						DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, tmpDir);
				factory.setFileCleaningTracker(tracker);

				// save upload file to disk
				ServletFileUpload upload = new ServletFileUpload(factory);

				List<FileItem> items = upload.parseRequest(request);
				String _fileName = null;
				String _fieldName = null;
				File savefile = null;
				for (FileItem item : items) {
					if (!item.isFormField()) {
						// 确定是文件而不是一个普通的表单字段
						_fileName = item.getName();
						_fieldName = item.getFieldName();
						String[] params = _fieldName.split("_");
						String param1 = params.length >= 1 ? params[0] : "";
						String param2 = params.length >= 2 ? params[1] : "";
						String param3 = params.length >= 3 ? params[2] : "";
						String sortNum = params.length >= 4 ? params[3] : "";

						String suffix = FileUtil.getSuffix(_fileName);
						String number = UUID.randomUUID().toString();
						String fileName = number + "." + suffix;

						StringBuffer sb = new StringBuffer();
						if (param1 != "") {
							sb.append(File.separator).append(param1);
						}
						if (param2 != "") {
							sb.append(File.separator).append(param2);
						}
						if (param3 != "") {
							sb.append(File.separator).append(param3);
						}
						String sPath = sb.toString();

						File fPath = new File(savePath + sPath);
						if (!fPath.exists()) {
							fPath.mkdirs();
						}

						savefile = new File(savePath + sPath + File.separator
								+ fileName);
						item.write(savefile);
						writer.flush();

						JSONObject jsonObject = new JSONObject();
						if (param1.compareToIgnoreCase("comment") == 0) {
							jsonObject.put("productCode", param3);
							jsonObject.put("sortNum", sortNum);
						}
						jsonObject.put("imageUrl", appImgRootpPath + sPath
								+ File.separator + fileName);

						urls.put(jsonObject);

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