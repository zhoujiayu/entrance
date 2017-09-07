package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbComment;
import com.ytsp.db.domain.EbCommentImg;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.CommentImgVO;
import com.ytsp.db.vo.CommentVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.v5_0.CustomerServiceV5_0;
import com.ytsp.entrance.service.v5_0.EbProductCommentService;
import com.ytsp.entrance.service.v5_0.OrderServiceV5_0;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.Util;

public class CommentCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_COMMENT_SAVE || code == CommandList.CMD_COMMENT_GETBYPAGE
				|| code == CommandList.CMD_PRODUCT_COMMENT_BY_PAGE);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		SessionCustomer sc = getSessionCustomer();
		try {
			if (code == CommandList.CMD_COMMENT_SAVE) {
				return submitComment(sc);
			} else if (code == CommandList.CMD_COMMENT_GETBYPAGE){
				return getCommentsByPage();
			}else if (code == CommandList.CMD_PRODUCT_COMMENT_BY_PAGE){
				return queryProductCommentsByPage();
			}
			return null;
		} catch (Exception e) {
			logger.info("CommentCommand:" + code + " 失败 " + ",headInfo:"
					+ getContext().getHead().toString() + "bodyParam:"
					+ getContext().getBody().getBodyObject().toString()
					+ e.getMessage());
			return getExceptionExecuteResult(e);
		}
	}
	
	
	/**
	* <p>功能描述:分页获取商品评论，按时间排序</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult queryProductCommentsByPage() throws Exception {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		if (jsonObj.isNull("commentType")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"评价类型不能为空！", result, this);
		}
		if (jsonObj.isNull("productCode")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"产品编号不能为空！", result, this);
		}
		if (jsonObj.isNull("commentNum")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"评价数量不能为空！", result, this);
		}
		int productCode = jsonObj.getInt("productCode");
		int commentType = jsonObj.getInt("commentType");
		int commentNum = jsonObj.getInt("commentNum");
		int page = jsonObj.getInt("page");
		JSONObject jo = new JSONObject();
		// 构建评价VO
		List<CommentVO> commentVo = getCommentVO(productCode, page,
				commentNum, commentType);
		Gson gson = new Gson();
		jo.put("comments", gson.toJson(commentVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取评论成功", jo,
				this);
	}
	
	/**
	 * @功能描述: 分页获取更多的评论
	 * @return
	 * @throws Exception
	 *             ExecuteResult
	 * @author yusf
	 */
	private ExecuteResult getCommentsByPage() throws Exception {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		if (jsonObj.isNull("commentType")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"评价类型不能为空！", result, this);
		}
		if (jsonObj.isNull("productCode")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"产品编号不能为空！", result, this);
		}
		if (jsonObj.isNull("commentNum")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"评价数量不能为空！", result, this);
		}
		if (jsonObj.isNull("commentId")) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"评价id不能为空！", result, this);
		}
		int productCode = jsonObj.getInt("productCode");
		int commentType = jsonObj.getInt("commentType");
		int commentNum = jsonObj.getInt("commentNum");
		int commentId = jsonObj.getInt("commentId");
		int startNum = 0;
		JSONObject jo = new JSONObject();
		// 构建评价VO
		List<CommentVO> commentVo = getCommentVO(productCode, startNum,
				commentNum, commentType, commentId);
		Gson gson = new Gson();
		jo.put("comments", gson.toJson(commentVo));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取评论成功", jo,
				this);
	}

	/**
	 * @功能描述:提交评价
	 * @param sc
	 * @return
	 * @throws Exception
	 *             ExecuteResult
	 * @author yusf
	 */
	private ExecuteResult submitComment(SessionCustomer sc) throws Exception {
		try {
			JSONObject result = new JSONObject();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (jsonObj.isNull("commentCommitVOs")) {
				return new ExecuteResult(
						CommandList.RESPONSE_STATUS_BODY_JSON_ERROR,
						"评价参数不能为空！", result, this);
			}
			if (jsonObj.isNull("orderId")) {
				return new ExecuteResult(
						CommandList.RESPONSE_STATUS_BODY_JSON_ERROR,
						"orderId不能为空！", result, this);
			}
			long orderId = jsonObj.getLong("orderId");
			List<EbComment> comments = new ArrayList<EbComment>();
			EbOrder order = getOrderById(orderId);

			if (order.getStatus() != EbOrderStatusEnum.COMPLETE) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"订单状态异常", result, this);
			}

			JSONArray commentArr = jsonObj.optJSONArray("commentCommitVOs");
			if (commentArr == null || commentArr.length() == 0) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"评论参数异常", result, this);
			}
			int size = commentArr.length();
			for (int i = 0; i < size; i++) {
				JSONObject json = commentArr.getJSONObject(i);
				JSONArray imgArr = null;
				if (!json.isNull("commentImgs")) {
					imgArr = json.getJSONArray("commentImgs");
				}
				if (json.isNull("orderDetailId")) {
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
							"评论参数异常", result, this);
				}
				int orderDetailId = json.optInt("orderDetailId");
				int score = json.optInt("score",5);
				String comment = json.optString("comment");
				//中差评，若不写评论，默认：没有评论内容
				if(StringUtil.isNullOrEmpty(comment) && score <= 3){
					comment = "没有评论内容";
				}else if(StringUtil.isNullOrEmpty(comment) && score > 3){
					comment = "好评";
				}
				EbOrderDetail detail =  getDetailSkuCode(order.getOrderDetails(),
						orderDetailId);
				int skuCode = detail.getSkuCode();
//				int productCode = detail.getProductCode();
				comments.add(createComment(comment, score, skuCode, imgArr,detail));
			}

			EbProductCommentService commentService = SystemInitialization
					.getApplicationContext().getBean(
							EbProductCommentService.class);
			commentService.saveAllComment(comments, order);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "评论成功",
					result, this);
		} catch (Exception e) {
			logger.error("submitComment() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:获取订单明细中的skuCode
	 * </p>
	 * <p>
	 * 参数：@param detail
	 * <p>
	 * 参数：@param orderDetailId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：int
	 * </p>
	 */
	private EbOrderDetail getDetailSkuCode(Set<EbOrderDetail> detail, int orderDetailId) {
		for (EbOrderDetail ebOrderDetail : detail) {
			if (orderDetailId == ebOrderDetail.getOrderDetailId()) {
				return ebOrderDetail;
			}
		}
		return null;
	}

	/**
	 * <p>
	 * 功能描述:根据订单id获取订单
	 * </p>
	 * <p>
	 * 参数：@param orderId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：EbOrder
	 * </p>
	 */
	private EbOrder getOrderById(long orderId) throws SqlException {
		OrderServiceV5_0 orderServ = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		return orderServ.getOrderByOrderId(orderId);
	}

	/**
	 * @功能描述: 获取评论返回VO
	 * @param productCode
	 * @param startNum
	 * @param commentNum
	 * @param commentType
	 * @return
	 * @throws SqlException
	 *             List<CommentVO>
	 * @author yusf
	 */
	private List<CommentVO> getCommentVO(int productCode, int startNum,
			int commentNum, int commentType, int commentId) throws SqlException {
		EbProductCommentService commentService = SystemInitialization
				.getApplicationContext().getBean(EbProductCommentService.class);
		List<EbComment> ebComments = commentService.getCommentByPage(
				productCode, startNum, commentNum, commentType, commentId);
		return buildCommentVO(ebComments);
	}
	
	/**
	* <p>功能描述:获取评论VO，评论VO是按评论时间排序</p>
	* <p>参数：@param productCode
	* <p>参数：@param startNum
	* <p>参数：@param commentNum
	* <p>参数：@param commentType
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<CommentVO></p>
	 */
	private List<CommentVO> getCommentVO(int productCode, int startNum,
			int commentNum, int commentType) throws SqlException {
		EbProductCommentService commentService = SystemInitialization
				.getApplicationContext().getBean(EbProductCommentService.class);
		List<EbComment> ebComments = commentService
				.queryPageProductCommentByTime(productCode, commentType,startNum,
						commentNum);
		return buildCommentVO(ebComments);
	}
	
	/**
	 * @功能描述:构建评价VO
	 * @param Comments
	 * @return CommentVO
	 * @author yusf
	 * @throws SqlException 
	 */
	private List<CommentVO> buildCommentVO(List<EbComment> Comments) throws SqlException {
		List<CommentVO> commentVos = new ArrayList<CommentVO>();
		CustomerServiceV5_0 custServ = SystemInitialization.getApplicationContext().getBean(CustomerServiceV5_0.class);
		for (EbComment comment : Comments) {
			if (comment.getValid().getValue().intValue() == 0) {
				continue;
			}
			CommentVO commentVo = new CommentVO();
			commentVo.setColor(comment.getColor());
			commentVo.setComment(comment.getComment());
			commentVo.setCommentId(comment.getId());
			commentVo
					.setCommentImgs(buildCommentImgVo(comment.getCommentImgs()));
			commentVo.setHaveImg(comment.getHaveImg());
			commentVo.setProductCode(comment.getProductId());
			// 如果没有评分，默认5分好评
			commentVo.setScore(comment.getScore() == null ? 5 : comment.getScore());
			commentVo.setSize(StringUtil.isNullOrEmpty(comment.getSize())?"默认" : comment.getSize());
			commentVo.setSkuCode(comment.getSkuCode());
			commentVo.setUserId(comment.getUserId());
			//由于有的数据没有userName或者第三方登录没有用户名，所以这里特殊处理名称，
			if(StringUtil.isNullOrEmpty(comment.getUserName())){
				int userId = comment.getUserId() == null? 0 : comment.getUserId();
				Customer cust = custServ.getCustomerById(userId);
				commentVo.setUserName(Util.obtainUserName(cust));
			}else{
				commentVo.setUserName(comment.getUserName());
			}
			commentVo.setCommentTime(DateFormatter.date2String(comment
					.getCommentTime(),"yyyy-MM-dd kk:mm:ss"));
			commentVos.add(commentVo);
		}
		return commentVos;
	}
	
	/**
	 * @功能描述:构建评价图片VO
	 * @return List<CommentImgVO>
	 * @author yusf
	 */
	private List<CommentImgVO> buildCommentImgVo(Set<EbCommentImg> commentImgs) {
		List<CommentImgVO> imgVos = new ArrayList<CommentImgVO>();
		for (EbCommentImg commentImg : commentImgs) {
			if (commentImg.getStatus().getValue().intValue() == 0) {
				continue;
			}
			CommentImgVO imgVo = new CommentImgVO();
			imgVo.setCommentId(commentImg.getCommentId());
			imgVo.setId(commentImg.getId());
			imgVo.setImgHeight(commentImg.getImgHeight());
			// TODO DEBUG 地址现在指向测试服务器
//			imgVo.setImgSrc("http://172.16.218.11/entrance/"
//					+ commentImg.getImgSrc());
			imgVo.setImgSrc(Util.getCommentImageURLByVersion(commentImg
					.getImgSrc(), getContext().getHead().getVersion(),
					getContext().getHead().getPlatform()));
			// imgVo.setImgSrc( commentImg.getImgSrc());
			imgVo.setImgWidth(commentImg.getImgWidth());
			imgVo.setSortNum(commentImg.getSortNum());
			imgVos.add(imgVo);
		}
		return imgVos;
	}

	/**
	 * <p>
	 * 功能描述:构建评价实体类
	 * </p>
	 * <p>
	 * 参数：@param comment
	 * <p>
	 * 参数：@param score
	 * <p>
	 * 参数：@param skuCode
	 * <p>
	 * 参数：@param imgs
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：EbComment
	 * </p>
	 */
	private EbComment createComment(String comment, int score, int skuCode,
			JSONArray imgs,EbOrderDetail detail) throws SqlException, JSONException {
		EbComment ebComment = new EbComment();
		Customer customer = getSessionCustomer().getCustomer();
		ebComment.setComment(comment);
		ebComment.setScore(score);
		ebComment.setUserId(customer.getId());
		//第三方登录用户可能没有帐号，没有帐号显示昵称
		ebComment.setUserName(StringUtil.isNullOrEmpty(customer.getAccount())?customer.getNick():customer.getAccount());
		ebComment.setDele(0);
		ebComment.setValid(ValidStatusEnum.INVALID);
		//添加评论图片
		ebComment.setCommentImgs(getCommentImages(imgs));
		ebComment.setHaveImg((imgs == null || imgs.length() <= 0) ? 0 : 1);
		ebComment.setCommentTime(getCurrentDate());
//		setSkuInfo(skuCode, ebComment,productCode);
		if(detail != null){
			ebComment.setSize(StringUtil.isNullOrEmpty(detail.getSize()) ? "默认"
					: detail.getSize());
			ebComment.setSkuCode(detail.getSkuCode());
			ebComment.setProductId(detail.getProductCode());
		}
		return ebComment;
	}

	/**
	 * <p>
	 * 功能描述:构建评论图片内容
	 * </p>
	 * <p>
	 * 参数：@param imgs
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws JSONException
	 * </p>
	 * <p>
	 * 返回类型：Set<EbCommentImg>
	 * </p>
	 */
	private Set<EbCommentImg> getCommentImages(JSONArray imgs)
			throws JSONException {
		if (imgs == null || imgs.length() <= 0) {
			return null;
		}
		Set<EbCommentImg> ret = new HashSet<EbCommentImg>();
		int size = imgs.length() > 5 ? 5 : imgs.length();
		for (int i = 0; i < size; i++) {
			EbCommentImg img = new EbCommentImg();
			img.setStatus(ValidStatusEnum.VALID);
			img.setImgSrc(imgs.getString(i));
			img.setSortNum(i);
			ret.add(img);
		}
		return ret;
	}

	/**
	 * @功能描述: 获取当前时间
	 */
	private Date getCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return calendar.getTime();
	}

	/**
	 * @功能描述:设置sku信息
	 * @param skuCode
	 * @param ebComment
	 * @throws SqlException
	 *             void
	 * @author yusf
	 */
	private void setSkuInfo(int skuCode, EbComment ebComment,int productCode)
			throws SqlException {
		EbSkuService skuService = SystemInitialization.getApplicationContext()
				.getBean(EbSkuService.class);
		EbSku sku = skuService.retrieveEbSkuBySkuCode(skuCode);
		ebComment.setSkuCode(skuCode);
		ebComment.setProductId(productCode);
		if (sku != null) {
			ebComment.setColor(sku.getColor());
			ebComment.setSize(sku.getSize());
		}else{
			ebComment.setSize("默认");
		}
	}
}
