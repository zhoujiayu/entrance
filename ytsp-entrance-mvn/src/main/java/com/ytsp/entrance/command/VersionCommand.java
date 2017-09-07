package com.ytsp.entrance.command;

import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.UpdateH5ListDao;
import com.ytsp.db.dao.UpdateListDao;
import com.ytsp.db.domain.UpdateH5List;
import com.ytsp.db.domain.UpdateList;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.HardwareRegisterService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateTimeFormatter;
import com.ytsp.entrance.util.Util;

/**
 * @author GENE
 * @description 版本检查
 * 
 */
public class VersionCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_VERSION_LAST == code
				|| CommandList.CMD_H5_VERSION_LAST == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_VERSION_LAST == code) {
				return lastVersion();
			} else if (CommandList.CMD_H5_VERSION_LAST == code) {
				return lastH5Version();
			}
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}

	/**
	 * 获取H5数据更新包，入参：客户端的h5版本号h5Version
	 * 
	 * @return
	 */
	private ExecuteResult lastH5Version() {
		try {
			HeadInfo head = getContext().getHead();
			String platform = head.getPlatform();
			if (StringUtil.isNullOrEmpty(platform)) {
				logger.error("获取不到当前设备平台信息！" + platform);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"获取不到当前设备平台信息！", null, this);
			}
			platform = platform.trim();
			MobileTypeEnum mte = null;
			try {
				mte = MobileTypeEnum.valueOf(platform);
			} catch (Exception ex) {
				logger.error("不可识别的设备平台信息！" + mte);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"不可识别的设备平台信息！", null, this);
			}
			String _version = head.getVersion();
			_version = _version == null ? "0.0.0" : _version;
			int version = convert2Num(_version);
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int h5Version = reqBody.optInt("h5Version", 0);
			// TODO 根据客户端版本号，h5版本号获取最新的数据更新包
			UpdateH5ListDao dao = SystemInitialization.getApplicationContext()
					.getBean(UpdateH5ListDao.class);
			UpdateH5List h5 = dao
					.findOneByHql(
							" WHERE mobileType=? and publish=1 and version = ? ORDER BY version,h5Version DESC",
							new Object[] { mte ,_version.trim()});
			JSONObject obj = new JSONObject();
			if (h5 != null) {
				int uVer = convert2Num(h5.getVersion());
				int uH5Ver = h5.getH5Version() == null ? 0 : h5.getH5Version()
						.intValue();
				if (uVer >= version && h5Version < uH5Ver) {
					obj.put("version",
							h5.getVersion() == null ? "0.0.0" : h5.getVersion());
					obj.put("h5Version", uH5Ver);
					obj.put("size", h5.getSize() == null ? 0 : h5.getSize());
					obj.put("downloadUrl", h5.getDownloadUrl() == null ? ""
							: h5.getDownloadUrl());
					obj.put("md5Code",
							h5.getMd5Code() == null ? "" : h5.getMd5Code());
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
							"获取更新信息成功！", obj, this);
				}
			}
			obj.put("h5Version", 0);
			//添加统计内容
			Util.addStatistics(getContext(), obj);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"没有该版本的更新信息！", obj, this);

		} catch (Exception e) {
			logger.error("升级错误，失败！" + e.getMessage());
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	public static void main(String[] args) {
		String s = "/4.1";
		System.err.println(s.replace("/", "\n"));
		System.err.println((int) '\\');
		// System.err.println(convert2Num(ss)>convert2Num(s));
	}

	public ExecuteResult lastVersion() {
		try {
			// JSONObject jsonObj = getContext().getBody().getBodyObject();
			HeadInfo head = getContext().getHead();
			String platform = head.getPlatform();
			if (StringUtil.isNullOrEmpty(platform)) {
				logger.error("获取不到当前设备平台信息！" + platform);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"获取不到当前设备平台信息！", null, this);
			}

			platform = platform.trim();
			MobileTypeEnum mte = null;
			try {
				mte = MobileTypeEnum.valueOf(platform);
			} catch (Exception ex) {
				logger.error("不可识别的设备平台信息！" + mte);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"不可识别的设备平台信息！", null, this);
			}
			String _version = head.getVersion();
			_version = _version == null ? "0.0.0" : _version;
			int version = convert2Num(_version);
			UpdateListDao uld = SystemInitialization.getApplicationContext()
					.getBean(UpdateListDao.class);
			UpdateList ul = uld.findOneByHql(
					" WHERE mobileType=? ORDER BY version DESC",
					new Object[] { mte });
			// 顺便注册一下硬件
			String hardwareId = getContext().getHead().getUniqueId();
			String otherInfo = getContext().getHead().getOtherInfo();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();
			HardwareRegisterService hrs = SystemInitialization
					.getApplicationContext().getBean(
							HardwareRegisterService.class);
			hrs.saveByNumber(hardwareId, otherInfo, platform, _version, appDiv,
					ip);
			if (ul != null) {
				int updateType = 0; // 0：不需更新；1：可以更新；2：强制更新
				int ulVer = convert2Num(ul.getVersion());
				if (ulVer > version) {
					logger.warn("need update :" + head.getVersion() + "::"
							+ head.getPlatform() + "::" + head.getUniqueId());
					if (Boolean.TRUE.equals(ul.getEnforce())) {
						updateType = 2;
					} else {
						updateType = 1;
						// List<UpdateList> uls =
						// uld.findAllByHql(" WHERE mobileType=? ORDER BY version DESC",
						// new Object[]{mte});
						// for(UpdateList _ul : uls){
						// if(convert2Num(_ul.getVersion()) > version && version
						// < ulVer){
						// if(Boolean.TRUE.equals(_ul.getEnforce())){
						// updateType = 2;
						// break;
						// }
						// }
						// }
					}

					JSONObject obj = new JSONObject();
					obj.put("version",
							ul.getVersion() == null ? "0.0.0" : ul.getVersion());
					obj.put("size", ul.getSize() == null ? 0 : ul.getSize());
					obj.put("updateTime", DateTimeFormatter.dateTime2String(ul
							.getUpdateTime()));
					obj.put("lang", ul.getLang() == null ? "" : ul.getLang());
					obj.put("storeUrl",
							ul.getStoreUrl() == null ? "" : ul.getStoreUrl());
					obj.put("downloadUrl", ul.getDownloadUrl() == null ? ""
							: ul.getDownloadUrl());
					obj.put("resolution", ul.getResolution().getText());
					obj.put("mobileType", ul.getMobileType().getText());
					obj.put("updateType", String.valueOf(updateType));
					obj.put("description", ul.getDescription() == null ? ""
							: ul.getDescription().replaceAll("/", "\n"));
					//添加统计内容
					Util.addStatistics(getContext(), obj.toString());
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
							"获取更新信息成功！", obj, this);
				}
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"没有该版本的更新信息！", null, this);

		} catch (Exception e) {
			logger.error("升级错误，失败！" + e.getMessage());
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	public static int convert2Num(String version)
			throws IllegalArgumentException {
		if (StringUtil.isNullOrEmpty(version)) {
			return 0;
		}
		String vs[] = version.split("\\.");
		if (vs.length != 3) {
			if (vs.length == 2 && version.length() == 3) {
				version += ".0";
				vs = version.split("\\.");
			}
			// throw new
			// IllegalArgumentException("版本号必须是以'.'号分隔的三位数，例如 1.2.11");
		}
		StringBuilder _v = new StringBuilder();
		for (int i = 0; i < vs.length; i++) {
			if (i >= 3) {
				break;
			}
			String v = vs[i].trim();
			if (v.length() == 1) {
				v += "0";
			}
			_v.append(v);
		}
		try {
			return Integer.valueOf(_v.toString());
		} catch (Exception ex) {
			throw new IllegalArgumentException("版本号必须是以'.'号分隔的三位数");
		}
	}

}
