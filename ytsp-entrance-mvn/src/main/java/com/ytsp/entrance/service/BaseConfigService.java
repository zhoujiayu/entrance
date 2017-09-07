package com.ytsp.entrance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.BaseConfigDataDao;
import com.ytsp.db.domain.BaseConfigData;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.system.IConstants;


@Service("baseConfigService")
@Transactional
public class BaseConfigService {

	@Resource(name = "baseConfigDataDao")
	private BaseConfigDataDao baseConfigDataDao;
	
	/**
	* <p>功能描述:根据编码获取配置的热搜词</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<String></p>
	 */
	public List<String> getHotSearchKeys(String code) throws SqlException{
		List<String> configs = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE attrcode = ? and status = ?");
		List<BaseConfigData> hotKeyConfig = baseConfigDataDao.findAllByHql(sql.toString(), new Object[]{code,ValidStatusEnum.VALID});
		if(hotKeyConfig == null || hotKeyConfig.size() <= 0){
			return configs;
		}
		for (BaseConfigData baseConfigData : hotKeyConfig) {
			if (StringUtil.isNullOrEmpty(baseConfigData.getAttrvalue())) {
				continue;
			}
			String[] hotKeys = baseConfigData.getAttrvalue().split(",");
			if (hotKeys == null || hotKeys.length == 0) {
				return configs;
			}
			for (String key : hotKeys) {
				if (StringUtil.isNullOrEmpty(key)) {
					continue;
				}
				configs.add(key);
			}
		}
		return configs;
	}
	
	/**
	* <p>功能描述:根据code获取某个配置信息</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：BaseConfigData</p>
	 */
	public BaseConfigData getBaseConfigDataByCode(String code) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE attrcode = ? and status = ?");
		BaseConfigData hotKeyConfig = baseConfigDataDao.findOneByHql(sql.toString(), new Object[]{code,ValidStatusEnum.VALID});
		return hotKeyConfig;
	}
	
	/**
	* <p>功能描述:获取所有的热搜词，并将其分类</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<String></p>
	 */
	public Map<String,List<String>> getAllHotSearchKeys() throws SqlException{
		Map<String,List<String>> allHotKeys = new HashMap<String, List<String>>();
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE attrcode in(?,?,?,?) and status = ?");
		List<BaseConfigData> hotKeyConfig = baseConfigDataDao
				.findAllByHql(sql.toString(), new Object[] {
						IConstants.CONFIG_RECOMMEND_SK,
						IConstants.CONFIG_PRODUCT_SK,
						IConstants.CONFIG_ALBUM_SK,
						IConstants.CONFIG_KNOWLEDGE_SK, ValidStatusEnum.VALID });
		if(hotKeyConfig == null || hotKeyConfig.size() <= 0){
			return allHotKeys;
		}
		//将数据封装为一个key对应某一类热词
		for (BaseConfigData baseConfigData : hotKeyConfig) {
			if(StringUtil.isNullOrEmpty(baseConfigData.getAttrvalue())){
				continue;
			}
			String[] hotKeys = baseConfigData.getAttrvalue().split(",");
			if (hotKeys == null || hotKeys.length == 0) {
				continue;
			}
			for (String key : hotKeys) {
				if(allHotKeys.containsKey(baseConfigData.getAttrcode())){
					if (StringUtil.isNullOrEmpty(key)) {
						continue;
					}
					allHotKeys.get(baseConfigData.getAttrcode()).add(key);
				}else{
					List<String> configs = new ArrayList<String>();
					configs.add(key);
					allHotKeys.put(baseConfigData.getAttrcode(), configs);
				}
			}
		}
		return allHotKeys;
	}
}
