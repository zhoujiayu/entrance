package com.ytsp.entrance.service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.dao.QuestionsDao;
import com.ytsp.db.domain.Questions;
import com.ytsp.db.domain.QuestionsEntry;
import com.ytsp.entrance.system.SystemManager;

/**
 * @author GENE
 * @description 试题服务
 */
public class QuestionsService {
	private static final Logger logger = Logger.getLogger(QuestionsService.class);

	private QuestionsDao questionsDao;
	
	public JSONArray getRandomQuestions(int age, int needCount) throws Exception{
		String HQL = " WHERE questionsAge.startAge <= ? AND questionsAge.endAge >= ?";
		Object[] filter = new Object[]{age, age};
		int count = questionsDao.getRecordCount(HQL, filter);
		
		JSONArray array = new JSONArray();
		if(count > 0){
			needCount = count < needCount ? count : needCount;
			Random random = new Random();
			int roll = random.nextInt(count - needCount + 1);
			List<Questions> questionss = questionsDao.findAllByHql(HQL, roll, needCount, filter);
			Collections.shuffle(questionss,new Random());
			for(Questions questions : questionss){
				JSONObject obj = new JSONObject();
				obj.put("qid", questions.getId());
				obj.put("title", questions.getTitle() == null ? "" : questions.getTitle());
//				obj.put("answer", questions.getAnswer());
				obj.put("description", questions.getDescription() == null ? "" : questions.getDescription());
				obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + questions.getImg());
				
				Set<QuestionsEntry> qe = questions.getQuestionsEntry();
				JSONArray items = new JSONArray();
				for (QuestionsEntry entry : qe) {
					JSONObject item = new JSONObject();
					item.put("item", entry.getItem() == null ? "" : entry.getItem());
					item.put("correct", Boolean.TRUE.equals(entry.getCorrect()));
					items.put(item);
				}
				
				obj.put("itemList", items);
				
				array.put(obj);
			}
		}
		return array;
	}
	
	public void saveQuestions(Questions questions) throws Exception {
		questionsDao.save(questions);
	}
	
	public void saveOrUpdate(Questions questions) throws Exception {
		questionsDao.saveOrUpdate(questions);
	}

	public void updateQuestions(Questions questions) throws Exception {
		questionsDao.update(questions);
	}

	public void deleteQuestions(Questions questions) throws Exception {
		questionsDao.delete(questions);
	}

	public Questions findQuestionsById(int questionsid) throws Exception {
		return questionsDao.findById(questionsid);
	}
	
	public List<Questions> getAllQuestionss() throws Exception {
		return questionsDao.getAll();
	}

	public void deleteQuestionsById(int questionsid) throws Exception {
		questionsDao.deleteById(questionsid);
	}
	
	public QuestionsDao getQuestionsDao() {
		return questionsDao;
	}

	public void setQuestionsDao(QuestionsDao questionsDao) {
		this.questionsDao = questionsDao;
	}

}
