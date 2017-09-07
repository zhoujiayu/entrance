package com.ytsp.entrance.service.v5_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.EbTrackDao;
import com.ytsp.db.domain.EbTrack;
import com.ytsp.db.enums.EbTrackTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;

@Service("trackService")
@Transactional
public class TrackService {
	
	@Resource(name="ebTrackDao")
	private EbTrackDao ebTrackDao;

	public EbTrackDao getEbTrackDao() {
		return ebTrackDao;
	}

	public void setEbTrackDao(EbTrackDao ebTrackDao) {
		this.ebTrackDao = ebTrackDao;
	}
	
	public void saveTrack(EbTrack ebTrack) throws SqlException{
		ebTrackDao.save(ebTrack);
	}
	
	public List<EbTrack> getTrackByPage(int userId,int trackId,int pageSize,String platform) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append(" WHERE 1=1 and status = 1 and userid = "+userId);
		if(trackId != 0){
			sb.append(" AND id < ? ");
		}
		//移动端网站不返回验证码
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.wapmobile)) {
			sb.append(" AND trackType = 1 ");
		}
		sb.append(" ORDER BY id desc");
		
		List<EbTrack> list = new ArrayList<EbTrack>();
		
		if(trackId == 0){
			list = ebTrackDao.findAllByHql(sb.toString(), 0, pageSize);
		}else{
			list = ebTrackDao.findAllByHql(sb.toString(), 0, pageSize, new Object[]{trackId});
		}
		
		return list;
	}
	
	/**
	* 功能描述:删除用户下所有重复的足迹
	* 参数：@param userId
	* 返回类型:void
	 */
	public void deleteRepeatTracks(int userId){
		if(userId <= 0){
			return;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" update ytsp_ebiz_track set status = 0 where id in (");
		sql.append(" select t.minId from (");
		sql.append("select userid,productcode,albumid,tracktype,createtime,count(id),min(id) minId from ytsp_ebiz_track WHERE status = 1 and userid = "
				+ userId
				+ " group by userid,productcode,albumid,tracktype,createtime having count(id) >1) t)");
		ebTrackDao.executeSqlUpdate(sql.toString());
	}

	/**
	* 功能描述:
	* 参数：@param list
	* 参数：@return
	* 返回类型:Map<String,List<EbTrack>>
	 * @throws SqlException 
	 */
	private List<EbTrack> deleteRepeatTrack(List<EbTrack> list) throws SqlException{
		if(list == null || list.isEmpty()){
			return null;
		}
		Map<String,List<EbTrack>> trackMaps = new HashMap<String, List<EbTrack>>();
		List<EbTrack> retTrack = new ArrayList<EbTrack>();
		List<EbTrack> removeTrack = new ArrayList<EbTrack>();
		for (EbTrack ebTrack : list) {
			if (trackMaps.containsKey(ebTrack.getCreateTime())) {
				if (isTrackExists(trackMaps.get(ebTrack.getCreateTime())
						, ebTrack)) {
					ebTrack.setStatus(0);
					removeTrack.add(ebTrack);
					continue;
				}
				trackMaps.get(ebTrack.getCreateTime()).add(ebTrack);
			} else {
				List<EbTrack> l = new ArrayList<EbTrack>();
				l.add(ebTrack);
				trackMaps.put(ebTrack.getCreateTime(),l);
			}
			retTrack.add(ebTrack);
		}
		ebTrackDao.updateAll(removeTrack);
		return retTrack;
	}
	
	/**
	* 功能描述:
	* 参数：@param list
	* 参数：@param trackVO
	* 参数：@return
	* 返回类型:boolean
	 */
	private boolean isTrackExists(List<EbTrack> list,EbTrack trackVO){
		if(list == null || list.isEmpty()){
			return false;
		}
		int type = trackVO.getTrackType().getValue().intValue();
		for (EbTrack track : list) {
			if(type != track.getTrackType().getValue().intValue()){
				continue;
			}
			if (type == EbTrackTypeEnum.PRODUCT.getValue().intValue()) {
				if (trackVO.getProductCode().intValue() == track.getProductCode()) {
					return true;
				}
			} else {
				if (trackVO.getAlbumId().intValue() == track.getAlbumId()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	* <p>功能描述:根据商品id或者专辑id和类型获取用户是否浏览过</p>
	* <p>参数：@return</p>
	* <p>返回类型：EbTrack</p>
	 * @throws SqlException 
	 */
	public EbTrack getTrackIdAndType(int userId,int trackType,String trackDate,int id) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append(" WHERE userId = ? and trackType= ? and createTime= ? and status = 1 ");
		if(trackType == 1){
			sb.append(" and productCode = ?");
			
		}else{
			sb.append(" and albumId = ?");
		}
		return ebTrackDao.findOneByHql(sb.toString(),new Object[]{userId,EbTrackTypeEnum.valueOf(trackType),trackDate,id});
	}
	
	public void updateTrack(EbTrack track) throws SqlException{
		ebTrackDao.update(track);
	}
	
	/**
	* <p>功能描述:获取一个最新浏览的足迹</p>
	* <p>参数：@param userId
	* <p>参数：@param trackType
	* <p>参数：@return</p>
	* <p>返回类型：EbTrack</p>
	 * @throws SqlException 
	 */
	public EbTrack getLastedOneTrack(int userId,EbTrackTypeEnum trackType) throws SqlException{
		String hql = " WHERE userId =? and trackType = ? and status = 1 ORDER BY id desc";
		return ebTrackDao.findOneByHql(hql,new Object[]{userId,trackType});
	}
	
	/**
	* <p>功能描述:删除足迹</p>
	* <p>参数：@param ids 足迹id列表</p>
	* <p>返回类型：void</p>
	 * @throws SqlException 
	 */
	public void deleteTrackByIds(List<Integer> ids) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append(" SET status = 0 WHERE id in(");
		for (int i = 0; i < ids.size(); i++) {
			if(i == ids.size() -1){
				sb.append(ids.get(i));
			}else{
				sb.append(ids.get(i)+",");
			}
		}
		sb.append(" )");
		ebTrackDao.updateByHql(sb.toString());
	}
	
	/**
	* <p>功能描述:删除用户所有足迹</p>
	* <p>参数：@param userId</p>
	* <p>返回类型：void</p>
	 * @throws SqlException 
	 */
	public void deleteAllTracks(int userId) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append(" SET status = 0 WHERE userId = ").append(userId);
		ebTrackDao.updateByHql(sb.toString());
	}
	
	/**
	* <p>功能描述:删除足迹</p>
	* <p>参数：@param track
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void deleteTrack(EbTrack track) throws SqlException{
		ebTrackDao.delete(track);
	}
	
}
