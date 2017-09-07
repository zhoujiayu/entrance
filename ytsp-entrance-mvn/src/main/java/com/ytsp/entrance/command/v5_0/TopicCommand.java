package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.Topic;
import com.ytsp.db.enums.EbPosterLinkUrlEnum;
import com.ytsp.db.enums.TopicTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.TopicVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.TopicService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.Util;

public class TopicCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_TOPIC_LIST || code == CommandList.CMD_TOPIC_GETBYID);
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (code == CommandList.CMD_TOPIC_LIST) {
				return getTopicList();
			} else if (code == CommandList.CMD_TOPIC_GETBYID) {
				return getTopicById();
			}
		} catch (Exception e) {
			logger.error("TopicCommand error: headInfo: "
					+ getContext().getHead().toString() + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 接口传入参数：topicId
	 * <p>
	 * 功能描述:获取文章详情
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws JSONException
	 * @throws SqlException
	 */
	private ExecuteResult getTopicById() throws JSONException, SqlException {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int topicId = 0;
		if (reqBody.isNull("topicId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"topicId不能为空", result, this);
		}
		topicId = reqBody.getInt("topicId");
		TopicService topicServ = SystemInitialization.getApplicationContext()
				.getBean(TopicService.class);
		Topic topic = topicServ.getTopicById(topicId);
		TopicVO vo = buildTopicVO(topic);
		TopicInfoVO info = new TopicInfoVO();
		info.setTopic(vo);
		//添加统计功能
		try {
			Util.addStatistics(getContext(), info);
		} catch (Exception e) {
			logger.error("文章详情统计出错；"+e.getMessage());
		}
		Gson gson = new Gson();
		result = new JSONObject(gson.toJson(info));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取文章详情成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:获取专题列表
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 * 
	 * @throws JSONException
	 * @throws SqlException
	 */
	private ExecuteResult getTopicList() throws JSONException, SqlException {
		JSONObject result = new JSONObject();
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		TopicTypeEnum topicType = null;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		if (!reqBody.isNull("topicType")) {
			int type = reqBody.getInt("topicType");
			topicType = TopicTypeEnum.valueOf(reqBody.getInt("topicType"));

		}
		int start = -1;
		if (pageSize != -1 && page != -1) {
			start = page * pageSize;
		}

		TopicService topicServ = SystemInitialization.getApplicationContext()
				.getBean(TopicService.class);
		List<Topic> topicList = topicServ.getTopicList(start, pageSize,
				topicType);
		Gson gson = new Gson();
		TopicList list = new TopicList();
		list.setTopiclist(getTopicVO(topicList));
		result = new JSONObject(gson.toJson(list));
		result.put("result", true);
		if(topicType != null){
			result.put("topicType", topicType.getValue());
		}
		Util.addStatistics(getContext(), list);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专题列表成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:获取专题VO
	 * </p>
	 * <p>
	 * 参数：@param topicList
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<TopicVO>
	 * </p>
	 */
	private List<TopicVO> getTopicVO(List<Topic> topicList) {
		if (topicList == null || topicList.size() <= 0) {
			return new ArrayList<TopicVO>();
		}
		List<TopicVO> voList = new ArrayList<TopicVO>();
		for (Topic topic : topicList) {
			TopicVO vo = new TopicVO();
			vo.setAuthor(topic.getAuthor());
			// vo.setContent(topic.getContent());
			vo.setCreatetime(DateFormatter.date2String(topic.getCreatetime()));
			vo.setSubTitle(topic.getSubTitle());
			vo.setTitle(topic.getTitle());
			vo.setTopicId(topic.getId());
			vo.setTopicType(topic.getTopicType() == null ? 0 : topic
					.getTopicType().getValue());
			vo.setTopicImage(getImage(topic.getTopicImage()));
//			vo.setUrl(Util.getFullImageURL(topic.getUrl()));
			vo.setUrl(Util.getFullImageURLByVersion(topic.getUrl(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
			vo.setTopicName(topic.getTopicName());
			vo.setStartTime(DateFormatter.date2String(topic.getStartDate()));
			vo.setEndTime(DateFormatter.date2String(topic.getEndDate()));
			vo.setOver(isOver(topic.getStartDate(), topic.getEndDate()));
			vo.setShareurl(IConstants.SHAREURL);
			voList.add(vo);
		}
		return voList;
	}

	/**
	 * <p>
	 * 功能描述:构建文章VO
	 * </p>
	 * <p>
	 * 参数：@param topic
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：TopicVO
	 * </p>
	 */
	private TopicVO buildTopicVO(Topic topic) {
		if (topic == null) {
			return null;
		}
		TopicVO vo = new TopicVO();
		vo.setAuthor(topic.getAuthor() == null ? "" : topic.getAuthor());
		//根据版本和平台，将文章内容里http替换为https
		vo.setContent(Util.replaceHttp2Https(topic.getContent(), getContext()
				.getHead().getVersion(), getContext().getHead().getPlatform()));
		vo.setCreatetime(DateFormatter.date2String(topic.getCreatetime()));
		vo.setSubTitle(topic.getSubTitle());
		vo.setTitle(topic.getTitle());
		vo.setTopicId(topic.getId());
		vo.setTopicType(topic.getTopicType() == null ? 0 : topic
				.getTopicType().getValue());
		vo.setTopicImage(getImage(topic.getTopicImage()));
//		vo.setUrl(Util.getFullImageURL(topic.getUrl()));
		vo.setUrl(Util.getFullImageURLByVersion(topic.getUrl(), getContext()
				.getHead().getVersion(), getContext().getHead().getPlatform()));
		vo.setTopicName(topic.getTopicName());
		vo.setStartTime(DateFormatter.date2String(topic.getStartDate()));
		vo.setEndTime(DateFormatter.date2String(topic.getEndDate()));
		vo.setOver(isOver(topic.getStartDate(), topic.getEndDate()));
		// 是否使用新的分享地址
		if (Util.isUseNewShareURL(getContext().getHead().getPlatform(),
				getContext().getHead().getVersion())) {
			vo.setShareurl(Util.getShareURL(EbPosterLinkUrlEnum.TOPICDETAIL, 0,""+ vo.getTopicId()));
			vo.setShareurl(vo.getShareurl().replaceAll("\\{", "@lt"));
			vo.setShareurl(vo.getShareurl().replaceAll("\\}", "@gt"));
		} else {
			vo.setShareurl(IConstants.SHAREURL);
		}
	
		return vo;
	}

	/**
	 * <p>
	 * 功能描述:专场是否结束
	 * </p>
	 * <p>
	 * 参数：@param startTime
	 * <p>
	 * 参数：@param endTime
	 * <p>
	 * 参数：@return 返回 true为结束，否则为false
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isOver(Date startTime, Date endTime) {
		Date now = new Date();
		if (startTime == null) {
			return false;
		}
		if (endTime == null) {
			return false;
		}
		return !(startTime.before(now) && endTime.after(now));
	}

	private List<String> getImage(String image) {
		if (image == null || image.length() <= 0) {
			return null;
		}
		List<String> ret = new ArrayList<String>();
		String[] imgArr = image.split(",");
		for (String img : imgArr) {
			ret.add(Util.getFullImageURLByVersion(img, getContext().getHead()
					.getVersion(), getContext().getHead().getPlatform()));
		}
		return ret;
	}

	class TopicList {
		private List<TopicVO> topiclist;

		public List<TopicVO> getTopiclist() {
			return topiclist;
		}

		public void setTopiclist(List<TopicVO> topiclist) {
			this.topiclist = topiclist;
		}

	}

	class TopicInfoVO {
		private TopicVO topic;

		public TopicVO getTopic() {
			return topic;
		}

		public void setTopic(TopicVO topic) {
			this.topic = topic;
		}
	}

}
