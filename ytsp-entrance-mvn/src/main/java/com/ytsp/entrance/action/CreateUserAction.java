package com.ytsp.entrance.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbSales;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.v4_0.EbSalesService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.MD5;

public class CreateUserAction extends ActionSupport implements ServletRequestAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String key = "S&Y1gD01qFn*tx86lkk7Y1xn0UAZ0b4M";
	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		
	}
	public static void main(String[] args) {
		System.err.println(MD5.code("account=18924199323&award=1005002S&Y1gD01qFn*tx86lkk7Y1xn0UAZ0b4M"));
	}
	private String account;
	private String award;
	private String sign ;
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getAward() {
		return award;
	}
	public void setAward(String award) {
		this.award = award;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String execute() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=UTF-8");
		
		try {
			String tmp = "account="+account+"&award="+award+key;
			if(!MD5.code(tmp).equals(sign)){
				response.getWriter().write("denied:"+account);
				return null;
			}
			/**
			 * 更新后的对应关系
			 * 1005002：1005009
			 * 1005003：1005008
			 * 1004022：1005007
			*/
			if("1005002".equals(award))
				award = "1005009";
			else if("1005003".equals(award))
				award = "1005008";
			else if("1004022".equals(award))
				award = "1005007";
			CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
			Customer customer = cs.findCustomerByAccount(account);
			if(customer==null){
				customer = new Customer();
				customer.setCreateTime(new Date());
				customer.setAccount(account);
				customer.setPassword(MD5.code(account));
				customer.setCreateTime(new Date());
				customer.setTerminalType("website");
				customer.setRegisterFrom("YOUYOU");
				cs.saveOrUpdate(customer);
				System.out.println(account);
			}
			EbSalesService es = SystemInitialization.getApplicationContext().getBean(EbSalesService.class);
			EbProductService eps = SystemInitialization.getApplicationContext().getBean(EbProductService.class);
			EbSales sales = es.findOne(customer.getId(),Integer.parseInt(award));
			
			if(sales==null){
				sales = new EbSales();
				sales.setAward("YOUYOU");
				sales.setUserid(customer.getId());
				sales.setCount(1);
				sales.setProduct(eps.retrieveEbProductById(Integer.parseInt(award)));
				es.saveEbSales(sales);
			}else{
				sales.setCount(1+sales.getCount());
				es.saveEbSales(sales);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.getWriter().write("exception:"+account);
			return null;
		}
		return "SUCCESS";
	}
	
}
