package com.ytsp.entrance.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ytsp.entrance.service.SystemParamService;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author gene
 * @description 同步系统存于DB的参数
 *
 */
public class SystemParamServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(SystemParamServlet.class);

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");

		String action = request.getParameter("action");
		if("sync".equals(action)){
			SystemParamService sps = SystemInitialization.getApplicationContext().getBean(SystemParamService.class);
			try {
				sps.syncVar();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

}
