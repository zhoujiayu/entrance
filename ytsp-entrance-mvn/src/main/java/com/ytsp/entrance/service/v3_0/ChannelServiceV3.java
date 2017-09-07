package com.ytsp.entrance.service.v3_0;

import java.util.List;

import com.ytsp.db.dao.ChannelDao;
import com.ytsp.db.domain.Channel;

public class ChannelServiceV3 {

	private ChannelDao channelDao;


	public Channel findChannelById(int channelid) throws Exception {
		return channelDao.findById(channelid);
	}
	
	//v3的channel在设计阶段叫forum，不过为了方便实现版本更迭，所以代码中继续用了channel这个概念，通过is_forum区分
	public List<Channel> getAllChannelsOrderByWeight() throws Exception {
		return channelDao.findAllByHql("where is_forum=1 ORDER BY weight ASC");
	}
	
	public ChannelDao getChannelDao() {
		return channelDao;
	}

	public void setChannelDao(ChannelDao channelDao) {
		this.channelDao = channelDao;
	}
}
