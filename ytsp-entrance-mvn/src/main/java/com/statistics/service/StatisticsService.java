package com.statistics.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.statistics.dao.StatisticsDao;
import com.statistics.entity.Statistics;
@Service("statisticsService")
public class StatisticsService {
	
	@Resource(name = "statisticsDao")
	private StatisticsDao statisticsDao;
	
	public void insertStatistics(Statistics statistics){
		statisticsDao.insert(statistics);
	}
}
