package com.ytsp.entrance.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.ChannelDao;
import com.ytsp.db.domain.Channel;

/**
 * @author GENE
 * @description 栏目服务
 */
public class ChannelService {
	private static final Logger logger = Logger.getLogger(ChannelService.class);

	private ChannelDao channelDao;

	public void saveChannel(Channel channel) throws Exception {
		channelDao.save(channel);
	}
	
	public void saveOrUpdate(Channel channel) throws Exception {
		channelDao.saveOrUpdate(channel);
	}

	public void updateChannel(Channel channel) throws Exception {
		channelDao.update(channel);
	}

	public void deleteChannel(Channel channel) throws Exception {
		channelDao.delete(channel);
	}

	public Channel findChannelById(int channelid) throws Exception {
		return channelDao.findById(channelid);
	}
	
	public List<Channel> getAllChannelsOrderByWeight() throws Exception {
		return channelDao.findAllByHql("where is_forum=0 ORDER BY weight ASC");
	}

	public void deleteChannelById(int channelid) throws Exception {
		channelDao.deleteById(channelid);
	}
	
	public ChannelDao getChannelDao() {
		return channelDao;
	}

	public void setChannelDao(ChannelDao channelDao) {
		this.channelDao = channelDao;
	}

}
