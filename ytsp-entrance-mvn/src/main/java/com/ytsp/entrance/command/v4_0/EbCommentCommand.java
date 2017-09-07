package com.ytsp.entrance.command.v4_0;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.EbComment;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.CommentService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateTimeFormatter;

public class EbCommentCommand  extends AbstractCommand{
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return ( code == CommandList.CMD_EB_SUBMITCOMMENT||
				 code == CommandList.CMD_EB_GETCOMMENTBYPRODUCT);
	}
	
	@Override
	public ExecuteResult execute() {
		//验证权限.
		int code = getContext().getHead().getCommandCode();
		SessionCustomer sc = getSessionCustomer();
		try{
			if( code == CommandList.CMD_EB_SUBMITCOMMENT)
				return submitComment(sc);
			else 
				return getCommentsByProductId();
		}catch(Exception e){
			logger.info("评论:" + code + " 失败 " + e);
			return getExceptionExecuteResult(e);
		}
	}
	
	private ExecuteResult getCommentsByProductId() throws Exception {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int productId = 0;
		if (!jsonObj.isNull("productId")) {
			productId = jsonObj.getInt("productId");
		}
		CommentService cs = SystemInitialization.getApplicationContext().getBean(CommentService.class);
//		JSONArray ja = ;
		JSONArray arr = new JSONArray();
		float total = 0f;
		for (EbComment ebComment : cs.getCommentsByProduct(productId)) {
			JSONObject foo = new JSONObject();
			foo.put("comment", ebComment.getComment());
			foo.put("score", ebComment.getScore());
			foo.put("commentTime", DateTimeFormatter.dateTime2String(ebComment.getCommentTime()));
			foo.put("userId", ebComment.getUserId());
			foo.put("userName", ebComment.getUserName());
			arr.put(foo);
			total += ebComment.getScore();
		}
		JSONObject jo = new JSONObject();
		jo.put("comments", arr);
		jo.put("count", arr.length());
		jo.put("average", new DecimalFormat("##0.0").format(total/arr.length()));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取评论成功",jo, this);
	}

	private ExecuteResult submitComment(SessionCustomer sc)  throws Exception{
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String comment = "";
			int score = -1;
			int productId = -1;
			CommentService cs = SystemInitialization.getApplicationContext().getBean(CommentService.class);
			if (!jsonObj.isNull("comment")) {
				comment = jsonObj.getString("comment");
			}
			if (!jsonObj.isNull("score")) {
				score = jsonObj.getInt("score");
			}
			if (!jsonObj.isNull("productId")) {
				productId = jsonObj.getInt("productId");
			}
			cs.submitComment(sc.getCustomer().getId(),sc.getCustomer().getAccount(),
					comment,score,productId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "评论成功", null, this);
		} catch (Exception e) {
			logger.error("submitComment() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
}
