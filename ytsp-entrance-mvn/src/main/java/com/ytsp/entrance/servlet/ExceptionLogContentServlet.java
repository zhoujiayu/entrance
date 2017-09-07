package com.ytsp.entrance.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ExceptionLogContentServlet extends HttpServlet {
	private static final Logger logger = Logger
			.getLogger(ExceptionLogContentServlet.class);

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		String filePath = "/export/logs/exceptionLog";
		String fileName = request.getParameter("fileName");
		 File f = new File(filePath+"/"+fileName);
	        if (!f.exists()) {
	            return;
	        }
	        BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
	        byte[] buf = new byte[1024];
	        int len = 0;
        	response.reset(); 
            URL u = new URL("file:///" + filePath);
            response.setContentType(u.openConnection().getContentType());
            response.setHeader("Content-Disposition", "inline; filename=" + f.getName());
            OutputStream out = response.getOutputStream();
            while ((len = br.read(buf)) > 0)
            out.write(buf, 0, len);
            br.close();
            out.close();
	}
}
