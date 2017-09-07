package com.rest.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="resource") // 标注类名为XML根节点
@XmlAccessorType(XmlAccessType.FIELD) // 表示将所有域作为XML节点
public class Resource {

	/**
	 * 资源id
	 */
	private String id;
	/**
	 * 资源名称
	 */
	private String name;
	/**
	 * 资源个数，JavaBean最好使用包装类型，原因是原始类型会默认初始化为默认值
	 * 这对于程序来说是很危险的！
	 */
	private Integer number;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
	
	public Resource(){
		// do nothing
	}

	public Resource(String id, String name, Integer number) {
		super();
		this.id = id;
		this.name = name;
		this.number = number;
	}
	
	
	
}

