package com.ytsp.entrance.command.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.statistics.entity.Statistics;

/**
 * @author GENE
 * @description Command上下文
 */
public class CommandContext {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HeadInfo head;
	private BodyInfo body;
	//统计对象
	private Statistics statistics;
	public CommandContext() {
		super();
	}
	
	public Statistics getStatistics() {
		return statistics;
	}


	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}


	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HeadInfo getHead() {
		return head;
	}

	public void setHead(HeadInfo head) {
		this.head = head;
	}

	public BodyInfo getBody() {
		if(body==null)
			try {
				return new BodyInfo("{}");
			} catch (Exception e) {
				e.printStackTrace();
			}
		return body;
	}

	public void setBody(BodyInfo body) {
		this.body = body;
	}
	
	

}
