package com.ytsp.entrance.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.ytsp.db.dao.EbProductImageDao;
import com.ytsp.db.domain.EbProductImage;
import com.ytsp.db.enums.EbProductImageTypeEnum;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;

public class ProductDetailAction extends ActionSupport implements
		ServletRequestAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		// TODO Auto-generated method stub

	}

	private String productId;
	private String platform;
	private String widthClient;
	private int widthClientInt;

	public int getWidthClientInt() {
		return widthClientInt;
	}

	public void setWidthClientInt(int widthClientInt) {
		this.widthClientInt = widthClientInt;
	}

	public String getWidthClient() {
		return widthClient;
	}

	public void setWidthClient(String widthClient) {
		this.widthClient = widthClient;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	private List<EbProductImage> details;

	public String getProductId() {
		return productId;
	}

	public List<EbProductImage> getDetails() {
		return details;
	}

	public void setDetails(List<EbProductImage> details) {
		this.details = details;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String execute() throws Exception {
		try {
			String ret = "";
			if (platform == null)
				return SUCCESS;
			int width = 0;
			if (platform.equals("ipad") || platform.equals("gpad"))
				ret = "pad";
			width = 830;
			if (platform.equals("iphone") || platform.equals("gphone")) {
				ret = "phone";
				width = 298;
			}
			if (widthClient == null || widthClient.equals(""))
				widthClientInt = width;
			else
				widthClientInt = (int) Float.parseFloat(widthClient);
			EbProductImageDao ps = SystemInitialization.getApplicationContext()
					.getBean(EbProductImageDao.class);
			details = ps.findAllByHql(
					"where productCode=? and type=? order by sortNum",
					new Object[] { Integer.parseInt(productId),
							EbProductImageTypeEnum.DETAIL });
			String imageHost = SystemManager.getInstance().getSystemConfig()
					.getImgServerUrl();
			if ("pad".equals(ret)) {
				width = details.size() > 25 ? width / 2 : width;
			}
			for (EbProductImage ebProductImage : details) {
				int height = (int) (width * ebProductImage.getImgHeight() / (float) ebProductImage
						.getImgWidth());
				ebProductImage
						.setImageSrc(String.format("%s%s.%dx%d", imageHost,
								ebProductImage.getImageSrc(), width, height));
				ebProductImage
						.setImgHeight((int) (widthClientInt
								* ebProductImage.getImgHeight() / (float) ebProductImage
								.getImgWidth()));
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}
}
