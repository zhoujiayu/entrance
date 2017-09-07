/* 
 * $Id: EncodingFilter.java 1571 2011-10-12 09:17:07Z jeff $ * 
 * All rights reserved 
 */

package com.ytsp.entrance.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.ytsp.common.util.StringUtil;

public class EncodingFilter implements Filter {

    FilterConfig config = null;
    private String targetEncoding = "UTF-8";

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        final String e = config.getInitParameter("encoding");
        if (StringUtil.isNotNullNotEmpty(e)) {
            this.targetEncoding = e;
        }
    }

    public void destroy() {
        config = null;
        targetEncoding = null;
    }

    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) srequest;
        request.setCharacterEncoding(targetEncoding);
        chain.doFilter(srequest, sresponse);
    }

}
