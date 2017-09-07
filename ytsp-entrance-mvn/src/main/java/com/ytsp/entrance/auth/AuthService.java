/*
 * $Id: AuthService.java 287 2011-08-07 09:35:56Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.auth; 

import com.ytsp.common.jmx.Service;
import com.ytsp.entrance.api.AuthMBean;

/**
 * 安全认证的服务实现，提供用户对资源进行权限认证的服务通道的实现。
 * 
 * @author Louis
 */
public class AuthService extends Service implements AuthMBean {

	@Override
	public int validResource(String sid, String rid) {
		return 0;
	}
	
}
