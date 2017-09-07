package com.ytsp.entrance.command.base;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class HeadInfo {

	public int commandCode = 0; // 指令
	public String timestamp = ""; // 时间戳
	public String rd = ""; // 随机数
	public String sig = ""; // md5对校字符串
	public double ver = 1f;// 版本代号
	public String version = "1.0"; // 版本号描述
	public String platform = "";// 平台
	public String vpn = ""; // 品牌
	public int screenWidth = 0; // 屏幕宽
	public int screenHeight = 0; // 屏幕高
	public String uniqueId = "";// 设备唯一码
	public String otherInfo = "";// 其他信息
	public int uid;// 用户 id
	public String ip = "";// ip信息
	public String appDiv = "IKAN";// 应用区分
	public String params = "";// 扩展字段
	public String sessionId = "";// 扩展字段
	public String cartId = "";// 扩展字段
	//校验码：生成规则：platform+appVersion+userId+commandCode+timestamp+"imagemedia"的MD5值
	public String secretKey = "";

	public HeadInfo() {
	}

	public HeadInfo(String json) throws Exception {
		try {
			JSONObject jsonObj = new JSONObject(json);
			this.setCommandCode(jsonObj.getInt("commandCode"));
			if (!jsonObj.isNull("screenWidth")) {
				this.setScreenWidth(jsonObj.getInt("screenWidth"));
			}
			if (!jsonObj.isNull("screenHeight")) {
				this.setScreenHeight(jsonObj.getInt("screenHeight"));
			}
			if (!jsonObj.isNull("uid")) {
				this.setUid(jsonObj.getInt("uid"));
			}
			if (!jsonObj.isNull("sessionId")) {
				this.setSessionId(jsonObj.getString("sessionId"));
			}
			if (!jsonObj.isNull("timestamp")) {
				this.setTimestamp(jsonObj.getString("timestamp"));
			}
			if (!jsonObj.isNull("rd")) {
				this.setRd(jsonObj.getString("rd"));
			}
			if (!jsonObj.isNull("sig")) {
				this.setSig(jsonObj.getString("sig"));
			}
			if (!jsonObj.isNull("version")) {
				this.setVersion(jsonObj.getString("version"));
			}
			if (!jsonObj.isNull("platform")) {
				this.setPlatform(jsonObj.getString("platform"));
			}
			if (!jsonObj.isNull("vpn")) {
				this.setVpn(jsonObj.getString("vpn"));
			}
			if (!jsonObj.isNull("uniqueId")) {
				this.setUniqueId(jsonObj.getString("uniqueId"));
			}
			if (!jsonObj.isNull("otherInfo")) {
				this.setOtherInfo(jsonObj.getString("otherInfo"));
			}
			if (!jsonObj.isNull("appDiv")) {
				this.setAppDiv(jsonObj.getString("appDiv"));
			}
			if (!jsonObj.isNull("params")) {
				this.setParams(jsonObj.optString("params"));
			}
			if (!jsonObj.isNull("cartId")) {
				this.setCartId(jsonObj.optString("cartId"));
			}
			if (!jsonObj.isNull("secretKey")) {
				this.setSecretKey(jsonObj.optString("secretKey"));
			}
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("New HeadInfo error : " + json,
					e);
			throw e;
		}
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getCartId() {
		return cartId;
	}

	public void setCartId(String cartId) {
		this.cartId = cartId;
	}

	public int getCommandCode() {
		return commandCode;
	}

	public void setCommandCode(int commandCode) {
		this.commandCode = commandCode;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getRd() {
		return rd;
	}

	public void setRd(String rd) {
		this.rd = rd;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public double getVer() {
		return ver;
	}

	public void setVer(double ver) {
		this.ver = ver;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getVpn() {
		return vpn;
	}

	public void setVpn(String vpn) {
		this.vpn = vpn;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getOtherInfo() {
		return otherInfo;
	}

	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAppDiv() {
		return appDiv;
	}

	public void setAppDiv(String appDiv) {
		this.appDiv = appDiv;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{commandCode:" + commandCode);
		sb.append(";timestamp:" + timestamp);
		sb.append(";rd:" + rd);
		sb.append(";sig:" + sig);
		sb.append(";ver:" + ver);
		sb.append(";version:" + version);
		sb.append(";platform:" + platform);
		sb.append(";vpn:" + vpn);
		sb.append(";screenWidth:" + screenWidth);
		sb.append(";screenHeight:" + screenHeight);
		sb.append(";uniqueId:" + uniqueId);
		sb.append(";otherInfo:" + otherInfo);
		sb.append(";uid:" + uid);
		sb.append(";ip:" + ip);
		sb.append(";params:" + params);
		sb.append(";appDiv:" + appDiv + "}");
		return sb.toString();
	}
}
