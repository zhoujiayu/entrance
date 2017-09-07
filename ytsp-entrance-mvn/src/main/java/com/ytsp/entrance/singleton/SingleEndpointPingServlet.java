package com.ytsp.entrance.singleton;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.system.SystemInitialization;

public class SingleEndpointPingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(SingleEndpointPingServlet.class);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final String remote = req.getRemoteAddr();
        String cid = req.getParameter("cid");
        String ckey = req.getParameter("ckey");
        String callbackType = req.getParameter("callbackType");
        String callbackUri = req.getParameter("callbackUri");

        if (StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(ckey)
                || StringUtil.isNullOrEmpty(callbackType)) {

            log.error(String.format("INSUFFICIENT parametersp[remote=%s, cid=%s, ckey=%s, callbackType=%s]", remote, cid, ckey, callbackType));
            return;
        }

        if ("http".equals(callbackType)) {
            if(StringUtil.isNullOrEmpty(callbackUri)) {
                log.error("INSUFFICIENT parametersp for callback uri is NULL from remote " + remote);
                return;
            }

            URI uri = null;
            try {
                uri = new URI(callbackUri);
            } catch (URISyntaxException e) {
                log.error("ILLEGAL uri syntax: " + callbackUri);
                return;
            }
            ApplicationContext ctx = SystemInitialization.getApplicationContext();
            assert ctx != null;
            final SingleEndpointServiceFacade facade = ctx.getBean("sesFacade", SingleEndpointServiceFacade.class);
            assert facade != null;
            facade.pingWithHttpCallback(cid, ckey, uri);

        } else {
            log.error("UNSUPPORTED callbackType: " + callbackType);
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}
