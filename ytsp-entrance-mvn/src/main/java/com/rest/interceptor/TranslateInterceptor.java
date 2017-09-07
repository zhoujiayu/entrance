package com.rest.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 *<p>类说明：
 * (这里用一句话描述这是一个什么样的类，格式：本类是XXXXXX。)
 * (这里用1~5句话描述这个类的功能是什么。 - 可选)
 * (这里介绍这个类涉及到的相关信息。 譬如，专有名词、概念的简单解释。 - 可选)
 * (这里介绍这个类在系统中的角色， 以及如何和其他类交互。 - 可选)
 * (这里介绍这个类的WHY，即为什么会存在这样一个类。 - 可选)
 * (这里介绍这个类的使用方法。 - 可选)
 * (这里是使用这个类的sample。 - 可选)
 * (这里是使用这个类的注意事项。 - 可选) 
 *
 */
public class TranslateInterceptor implements HandlerInterceptor{

	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		return true;
	}

	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}

}

