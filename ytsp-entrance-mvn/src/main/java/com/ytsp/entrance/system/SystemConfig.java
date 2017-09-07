/*
 * $Id: SystemConfig.java 287 2011-08-07 09:35:56Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.system;

public class SystemConfig {

	private String jmxHost;
	private int jmxPort;
	private String jmxProtocol;
	private String imgServerUrl;
	private String imgSavePath;

	public String getJmxHost() {
		return jmxHost;
	}

	public void setJmxHost(String jmxHost) {
		this.jmxHost = jmxHost;
	}

	public int getJmxPort() {
		return jmxPort;
	}

	public void setJmxPort(int jmxPort) {
		this.jmxPort = jmxPort;
	}

	public String getJmxProtocol() {
		return jmxProtocol;
	}

	public void setJmxProtocol(String jmxProtocol) {
		this.jmxProtocol = jmxProtocol;
	}

	public String getImgServerUrl() {
		return imgServerUrl;
	}

	public void setImgServerUrl(String imgServerUrl) {
		this.imgServerUrl = imgServerUrl;
	}

	public String getImgSavePath() {
		return imgSavePath;
	}

	public void setImgSavePath(String imgSavePath) {
		this.imgSavePath = imgSavePath;
	}

}
