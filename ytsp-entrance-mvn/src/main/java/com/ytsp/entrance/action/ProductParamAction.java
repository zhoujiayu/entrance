package com.ytsp.entrance.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.ytsp.db.domain.EbProductParam;
import com.ytsp.entrance.service.v4_0.ProductParamService;
import com.ytsp.entrance.system.SystemInitialization;

public class ProductParamAction extends ActionSupport implements ServletRequestAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	private String productId ; 
	private List<EbProductParam> params ;
	
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String execute() throws Exception {
		ProductParamService ps = SystemInitialization.getApplicationContext().getBean(ProductParamService.class);
		params = ps.getProductParamByProductId(Integer.parseInt(productId));
		if(platform==null)
			return null;
		if(platform.equals("ipad")||platform.equals("gpad"))
			return "pad";
		if(platform.equals("iphone")||platform.equals("gphone"))
			return "phone";
		return null;
	}
	public List<EbProductParam> getParams() {
		return params;
	}
	private String platform;
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public void setParams(List<EbProductParam> params) {
		this.params = params;
	}
}
