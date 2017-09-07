package com.ytsp.entrance.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.ExceptionLog;
import com.ytsp.entrance.service.v5_0.ExceptionLogService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.ValidateUtil;

public class ExceptionLogUploadServlet extends HttpServlet {
	private static final Logger logger = Logger
			.getLogger(ExceptionLogUploadServlet.class);

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/json;charset=UTF-8");
		JSONObject json = new JSONObject();
		PrintWriter writer = response.getWriter();
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("utf-8"); // 支持中文文件名
		List list = new ArrayList<FileItem>();
		Map<String,Object> map = new HashMap<String, Object>();
		String fileName = "";
		String saveFilePath = "/export/logs/exceptionLog";
		Gson gson = new Gson();
		boolean flag = true;
		boolean status = false;
//		ExceptionLogService exceptionLogService = SystemInitialization.getApplicationContext().getBean(ExceptionLogService.class);
//		try {
//			list = upload.parseRequest(request);
//			for (int i = 0; i < list.size(); i++) {
//				FileItem item = (FileItem) list.get(i);
//				if (item.isFormField()) { // 普通表单值
//					map.put(item.getFieldName(), item.getString("UTF-8"));
//				} else {
//					String name = item.getName(); // 获得上传的文件名（IE上是文件全路径，火狐等浏览器仅文件名）
//					fileName = ValidateUtil.generateShortUuid()+"."+name.split("\\.")[1];
//					// 上传操作
//					flag = upload4Stream(fileName, saveFilePath,
//							item.getInputStream()); // 上传文件
//				}
//			}
//			if(flag){
//			    SimpleDateFormat format =  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
//			    String d = format.format(Long.parseLong(map.get("exceptionTime")+""));
//			    Date date=format.parse(d);
//				map.put("exceptionTime",date);
//				ExceptionLog el = gson.fromJson(gson.toJson(map), ExceptionLog.class);
//				el.setCreateTime(new Date());
//				el.setFileName(fileName);
//				exceptionLogService.saveExceptionLog(el);
//				status = true;
//				json.put("status", status);
//			}
//		} catch (Exception e) {
//			
//		}
		try {
			json.put("status", status);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ret = json.toString();
		writer.write(ret);
	}
	
	
	private boolean upload4Stream(String fileName, String filePath,
			InputStream inStream) {
		boolean result = false;
		if ((filePath == null) || (filePath.trim().length() == 0)) {
			return result;
		}
		OutputStream outStream = null;
		try {
			String wholeFilePath = filePath + "/" + fileName;
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File outputFile = new File(wholeFilePath);
			boolean isFileExist = outputFile.exists();
			boolean canUpload = true;
			if (isFileExist) {
				canUpload = outputFile.delete();
			}
			if (canUpload) {
				int available = 0;
				outStream = new BufferedOutputStream(new FileOutputStream(
						outputFile), 2048);
				byte[] buffer = new byte[2048];
				while ((available = inStream.read(buffer)) > 0) {
					if (available < 2048)
						outStream.write(buffer, 0, available);
					else {
						outStream.write(buffer, 0, 2048);
					}
				}
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (inStream != null) {
					inStream.close();
				}
				if (outStream != null)
					outStream.close();
			} catch (Exception ex) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
				if (outStream != null)
					outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
