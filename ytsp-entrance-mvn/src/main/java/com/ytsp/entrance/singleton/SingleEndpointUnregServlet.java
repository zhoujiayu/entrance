package com.ytsp.entrance.singleton;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.system.SystemInitialization;

public class SingleEndpointUnregServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(SingleEndpointUnregServlet.class);
       
    public SingleEndpointUnregServlet() {
        super();
    }

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	    final String remote = req.getRemoteAddr();
        String cid = req.getParameter("cid");

        if (StringUtil.isNullOrEmpty(cid)) {
            log.error(String.format("INSUFFICIENT parametersp[remote=%s, cid=%s]", remote, cid));
            return;
        }

        ApplicationContext ctx = SystemInitialization.getApplicationContext();
        assert ctx != null;
        final SingleEndpointServiceFacade facade = ctx.getBean("sesFacade", SingleEndpointServiceFacade.class);
        assert facade != null;
        facade.unregister(cid);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    doGet(request, response);
	}

}
