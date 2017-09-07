package com.ytsp.entrance.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.ytsp.db.dao.EbCommentDao;
import com.ytsp.db.domain.EbComment;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.entrance.system.SystemInitialization;

public class ProductCommentsAction extends ActionSupport implements ServletRequestAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	private String productId ; 
	private String platform;
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
public static void main(String[] args) throws UnsupportedEncodingException {
	String ss ="http%3A%2F%2F115.29.103.107%2Fbmw_srp%2Fapp.php%3Fboxid=901e84260bf211e483f300163e022466&response_type=code&scope=snsapi_base&state=123#wechat_redirect ";
	System.out.println(URLDecoder.decode(ss, "UTF8"));
}
	private List<EbComment> comments ;
	public String getProductId() {
		return productId;
	}
	public List<EbComment> getComments() {
		return comments;
	}
	public void setComments(List<EbComment> comments) {
		this.comments = comments;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public String execute() throws Exception {
		EbCommentDao ps = SystemInitialization.getApplicationContext().
				getBean(EbCommentDao.class);
		comments = ps.findAllByHql("where productId=? and valid=? order by commentTime desc", new Object[]{
				Integer.parseInt(productId),ValidStatusEnum.VALID});
		for (EbComment ebComment : comments) {
			if(ebComment.getUserName()==null||ebComment.getUserName().equals(""))
				ebComment.setUserName("匿名");
		}
		if(platform==null)
			return SUCCESS;
		if(platform.equals("ipad")||platform.equals("gpad"))
			return "pad";
		if(platform.equals("iphone")||platform.equals("gphone"))
			return "phone";
		return SUCCESS;
	}
}
