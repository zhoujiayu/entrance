package com.ytsp.entrance.service.v4_0;

import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.FeedbackRecordDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.FeedbackRecord;

public class FeedbackService {
	private FeedbackRecordDao feedbackRecordDao;
	private CustomerDao customerDao;

	public FeedbackRecordDao getFeedbackRecordDao() {
		return feedbackRecordDao;
	}

	public void setFeedbackRecordDao(FeedbackRecordDao feedbackRecordDao) {
		this.feedbackRecordDao = feedbackRecordDao;
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	/**
	 * 保存反馈
	 * @param userId
	 * @param content
	 * @param mobile
	 * @param email
	 * @throws Exception
	 */
	public void saveFeedBack(int userId,FeedbackRecord feedbackRecord ) throws Exception{
		if(userId > 0)
		{
			Customer customer = customerDao.getObject(Customer.class, userId);
			feedbackRecord.setCustomer(customer);
		}
		feedbackRecordDao.save(feedbackRecord);
	}
	
}
