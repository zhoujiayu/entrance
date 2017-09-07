package com.ytsp.entrance.service.v3_0;

import java.util.List;

import com.ytsp.db.dao.VoiceDao;
import com.ytsp.db.domain.Voice;

public class VoiceService {
	private VoiceDao voiceDao;

	public List<Voice> getAllVoice() throws Exception{
		return voiceDao.getAll();
	}
	
	public VoiceDao getVoiceDao() {
		return voiceDao;
	}

	public void setVoiceDao(VoiceDao voiceDao) {
		this.voiceDao = voiceDao;
	}
	
	
}
