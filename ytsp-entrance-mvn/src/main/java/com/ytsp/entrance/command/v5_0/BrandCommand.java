package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.EbBrand;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.BrandVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.EbBrandService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

/**
 * 品牌接口
 */
public class BrandCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_BRAND_ALL);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_BRAND_ALL) {
				return queryBrands();
			}
		} catch (Exception e) {
			logger.error("BabyCommand," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	/**
	 * 入参：page,pageSize,若不分页显示无需传page和pageSize参数
	 * <p>
	 * 功能描述:
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
	private ExecuteResult queryBrands() throws JSONException, SqlException {
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = -1;
		int pageSize = -1;
		if (!reqBody.isNull("page")) {
			page = reqBody.getInt("page");
		}
		if (!reqBody.isNull("pageSize")) {
			pageSize = reqBody.getInt("pageSize");
		}
		BrandInfoVO brandInfo = new BrandInfoVO();
		List<EbBrand> brands = getBrandByPage(page, pageSize);
		brandInfo.setBrands(getBrandVO(brands));
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(brandInfo));

		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取品牌成功",
				result, this);
	}

	/**
	 * <p>
	 * 功能描述:分页获取所有品牌
	 * </p>
	 * <p>
	 * 参数：@param page
	 * <p>
	 * 参数：@param pageSize
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbBrand>
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private List<EbBrand> getBrandByPage(int page, int pageSize)
			throws SqlException {
		EbBrandService brandServ = SystemInitialization.getApplicationContext()
				.getBean(EbBrandService.class);
		List<EbBrand> brands = brandServ.getAllBrandsByPage(page, pageSize);
		return brands;
	}

	/**
	 * <p>
	 * 功能描述:构建品牌VO
	 * </p>
	 * <p>
	 * 参数：@param brands
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<BrandVO>
	 * </p>
	 */
	private List<BrandVO> getBrandVO(List<EbBrand> brands) {
		if (brands == null || brands.size() <= 0) {
			return null;
		}
		List<BrandVO> brandVos = new ArrayList<BrandVO>();
		for (EbBrand brnad : brands) {
			BrandVO vo = new BrandVO();
			vo.setBrandId(brnad.getBrandId());
//			vo.setBrandLogo(Util.getFullImageURL(brnad.getAppbrandLogo()));
			vo.setBrandLogo(Util.getFullImageURLByVersion(brnad
					.getAppbrandLogo(), getContext().getHead().getVersion(),
					getContext().getHead().getPlatform()));
			vo.setBrandName(brnad.getBrandName());
			vo.setBrandShort(brnad.getBrandShort());
			brandVos.add(vo);
		}
		return brandVos;

	}

	/**
	 * 品牌信息VO：主要用来将品牌列表转换成json对象
	 */
	class BrandInfoVO {
		private List<BrandVO> brands = null;

		public List<BrandVO> getBrands() {
			return brands;
		}

		public void setBrands(List<BrandVO> brands) {
			this.brands = brands;
		}

	}

}
