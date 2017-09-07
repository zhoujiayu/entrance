package com.rest.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="message") // 标注类名为XML根节点
@XmlAccessorType(XmlAccessType.FIELD) // 表示将所有域作为XML节点
public class Message {
	
	// 信息的具体描述
	private String msg;
	// 信息成功与否
	private String result;

	public Message(){
	}

	public Message(String msg, String result) {
		super();
		this.msg = msg;
		this.result = result;
	}
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}

