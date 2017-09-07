/*
 * $Id: AuthMBean.java 287 2011-08-07 09:35:56Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.api; 

/**
 * 安全认证MBean，提供用户对资源进行权限认证的服务通道。
 * 
 * @author Louis
 */
public interface AuthMBean {
	
	/**
	 * 获取会话访问者对资源的权限代码，可通过AuthLevel进行权限
	 * 判断。
	 * 
	 * @see com.ytsp.entrance.api.AuthLevel
	 * @param sid 会话id
	 * @param rid 资源id
	 * @return 权限代码
	 */
	int validResource(String sid, String rid);

}
