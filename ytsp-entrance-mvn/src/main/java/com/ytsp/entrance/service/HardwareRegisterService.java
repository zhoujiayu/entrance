package com.ytsp.entrance.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.AppDownloadDao;
import com.ytsp.db.dao.HardwareAppDao;
import com.ytsp.db.dao.HardwareRegisterDao;
import com.ytsp.db.domain.AppDownload;
import com.ytsp.db.domain.HardwareRegister;
import com.ytsp.entrance.util.IPSeeker;

/**
 * @author GENE
 * @description 移动硬件设备信息服务
 */
@Service("hardwareRegisterService")
@Transactional
public class HardwareRegisterService {
	static final Logger logger = Logger.getLogger(HardwareRegisterService.class);

	@Resource(name="hardwareRegisterDao")
	private HardwareRegisterDao hardwareRegisterDao;

	@Resource(name="hardwareAppDao")
	private HardwareAppDao hardwareAppDao;

	@Resource(name="appDownloadDao")
	private AppDownloadDao appDownloadDao;
	
	public void saveHardwareRegister(HardwareRegister hardwareRegister) throws Exception {
		hardwareRegisterDao.save(hardwareRegister);
	}
	
	public void saveRegisterDeviceToken(String number, String token) throws Exception {
		HardwareRegister hw = hardwareRegisterDao.findOneByHql(" WHERE number=?", new Object[]{number});
		if(hw != null) {
			hw.setDeviceToken(token);
			hardwareRegisterDao.update(hw);
		}
		else{
			
		}
	}
	
	/**
	 * 注册硬件信息
	 * @param number
	 * @throws Exception
	 */
	public void saveByNumber(String number, String otherInfo, String platform, String version,String appDiv,String ip) throws Exception {
//		SystemParamInDB params = SystemManager.getInstance().getSystemParamInDB();
//		String value = params.getValue(IConstants.PROBATION_KEY);
//		int probation = 3;
//		if(StringUtil.isNotNullNotEmpty(value)){
//			try{
//				probation = Integer.valueOf(value);
//			}catch(Exception ex){
//				logger.error("", ex);
//			}
//		}
		//首先判断有没有注册硬件
		HardwareRegister hw = hardwareRegisterDao.findOneByHql(" WHERE number=?", new Object[]{number});
		
		//如果没有注册则注册
		if(hw == null){
			Date cur = new Date();
			hw = new HardwareRegister();
			hw.setNumber(number);
			hw.setOtherInfo(otherInfo);
			hw.setCreateTime(cur);
			hw.setTerminalType(platform);
			hw.setTerminalVersion(version);
			hw.setAppDiv(appDiv);
		    long free = (cur.getTime()/1000) + 60*60*24*3;//免费看片天数，默认为3  
		    hw.setProbation(new Date(free*1000));
		    hw.setIp(ip);
			String[] a = IPSeeker.getAreaNameByIp(ip);
			hw.setProvince(a[0]);
			hw.setCity(a[1]);
			hardwareRegisterDao.save(hw);
			List<AppDownload> ls = appDownloadDao.findAllByHql(
					"where  ip=? and activate=? and createTime>?",
					new Object[]{ip,false,new Date(System.currentTimeMillis()-8*3600*1000)});
			if(ls!=null&&ls.size()>0){
				ls.get(0).setActivate(true);
				appDownloadDao.update(ls.get(0));
			}
		}
	}
}
