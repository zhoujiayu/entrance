package com.ytsp.entrance.action;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.springframework.web.util.HtmlUtils;

import com.opensymphony.xwork2.ActionSupport;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbSku;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.system.SystemInitialization;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class YimaGoodsAction extends ActionSupport implements ServletRequestAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3154899910875387503L;

	private static String uuid = "ec0ce465-9986-428d-a2a3-8d417531003b";
	private Integer pid;
	private String token;
	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		
	}
	
	public String execute() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
//		response.sendRedirect("http://weixin.qq.com/r/KUMIEH-E0yKsrTtz9xY7");
//		if(1>0)
//		return null;
		if(token==null||!token.equals(uuid))
			return null;
		EbProductService eps = SystemInitialization.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> ls = new ArrayList<EbProduct>();
		if(pid!=null&&pid>0){
			ls.add(eps.retrieveEbProductById(pid));
		}
		else
			ls = eps.getAllValid();
		DecimalFormat df = new DecimalFormat("#.##");
		List<Goods> ret = new ArrayList<Goods>();
		for (EbProduct ebProduct : ls) {
			Goods gd = new Goods();
			String brand = "";
			String brand_url = "";
			if(ebProduct.getEbBrand()!=null){
				brand = ebProduct.getEbBrand().getBrandName();
				brand_url = "http://ikan.cn/brand/"+ebProduct.getEbBrand().getBrandShort()+".html";
			}
			gd.setBrand_name(htmlEscape(brand));
			gd.setBrand_url(htmlEscape(brand_url));
			gd.setCurrent_price(df.format(ebProduct.getVprice()));
			String first_category_name = "";
			String first_category_url = "";
			if(ebProduct.getEbCatagory()!=null){
				first_category_name = ebProduct.getEbCatagory().getCname();
				first_category_url = "http://ikan.cn/category/"+
						ebProduct.getEbCatagory().getId()+".html";
			}
			gd.setFirst_category_name(htmlEscape(first_category_name));
			gd.setFirst_category_url(htmlEscape(first_category_url));
			gd.setGoods_id(ebProduct.getProductCode().toString());
			gd.setGoods_name(htmlEscape(ebProduct.getProductName()));
			gd.setGoods_status("Y");
			gd.setGoods_url(htmlEscape("http://ikan.cn/item/"+ebProduct.getProductCode()+".html"));
			boolean inventory_status = false;
			for (EbSku ebSku :ebProduct.getSkus() ) {
				if(ebSku.getStorage().getAvailable()>0)
					inventory_status = true;
			}
			gd.setInventory_status(inventory_status?"Y":"N");
			gd.setMaterial_url(htmlEscape("http://webimg.ikan.cn/"+ebProduct.getImgUrl()));
			gd.setOld_price(df.format(ebProduct.getPrice()));
			ret.add(gd);
		}
		Map dataMap = new HashMap();
		dataMap.put("goodsList", ret);
		Configuration configuration = new Configuration();
		configuration.setDefaultEncoding("utf-8");
		configuration.setClassForTemplateLoading(this.getClass(), "/com/ytsp/entrance/action");
		Template t=null;
		t = configuration.getTemplate("yimagoods.ftl");
		response.setContentType("text/xml;charset=UTF-8");
		t.process(dataMap, response.getWriter());
		return null;
	}
	private String htmlEscape(String src){
		if(src==null)
			return "";
		if(src.indexOf('&')>0||src.indexOf('>')>0||
				src.indexOf('<')>0||src.indexOf('"')>0||
				src.indexOf('\'')>0||src.indexOf('/')>0||
				src.indexOf('\\')>0)
			return HtmlUtils.htmlEscape(src);
		return src;
	}
}
