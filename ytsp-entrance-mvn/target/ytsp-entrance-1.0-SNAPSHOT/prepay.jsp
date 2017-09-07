<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@page import="java.util.*" %>
<%@page import="com.ytsp.db.domain.*"  %>
<%@page import="com.ytsp.entrance.recharge.alipay.model.*" %>

<% 
	String path = request.getContextPath(); 
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/"; 
%> 
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<base href="<%=basePath%>"> 
	<title>充值</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="keywords" content="爱看"/>
	<meta name="description" content="爱看" />
	<link href="css/prepay.css" rel="stylesheet" type="text/css">
	<script src="js/prepay.js" type="text/javascript"></script>
	
	<% 
		//Customer customer = (Customer)request.getAttribute("cus");
		PayChannelResult pcr = (PayChannelResult)request.getAttribute("ch");
		String method = (String)request.getAttribute("method");
		method = method == null ? "" : method.trim();
		List<MemberShipFee> fees = (List<MemberShipFee>)request.getAttribute("fees");
	%>
	
    <script type="text/javascript"> 
    	var basePath = "<%=basePath%>"; 
    	var method = "<%=method%>";
    </script> 
</head>
<body onload="resetPanel();">
	<!--主体begin-->	
	<div class="main_2">
		<!-- div class="chongzhi-left">
			<div class="chongzhi-nav-wrap">
				<ul class="chongzhi-list">
					<li class="chongzhi-nav_1"><a href="javascript:methodChange('prepaid');"></a></li>
					<li class="chongzhi-nav_2"><a href="javascript:methodChange('debitcard');"></a></li>
					<li class="chongzhi-nav_3"><a href="javascript:methodChange('creditcard');"></a></li>
					<li class="chongzhi-nav_4"><a href="javascript:methodChange('ali');"></a></li>
				</ul>
			</div>
		</div -->
		
		<div id="prepaid_panel" class="chongzhi-right" style="display:none">
			<h4>点卡充值信息</h4>
			<div class="chongzhi-text-wrap">
				<form action="pay.wtf" method="post" id="prepaid_form">
					<input type="hidden" name="type" value="prepaid" />
					<input type="hidden" name="from" value="MOBILE" />
					
					<fieldset>
					 	<legend>您选择了点卡支付充值方式。</legend>
						<label>“爱看”账号：</label>
						<div>
							<input type="text" name="account" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label>“爱看”密码：</label>
						<div>
							<input type="password" name="psw" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label>点卡帐号：</label>
						<div>
							<input type="text" name="cardNum" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label>点卡密码：</label>
						<div>
							<input type="password" name="cardPsw" value="" />
						</div>
					</fieldset>
					<fieldset>
						<input name="" class="small-box" type="checkbox" value="">
						 <p>我已阅读并接受<a href="provision.jsp" target="_self">账户使用条款</a>。</p>
					</fieldset>
					<input type="button" value="确认支付" onclick="javascript:submitForm(this, 'prepaid_form');"/><a class="quxiao" href="#">取消</a>
				</form>
			</div>
		</div>
		
		<div id="debitcard_panel" class="chongzhi-right" style="display:none">
			<h4>储蓄卡充值信息</h4>
			<div class="chongzhi-text-wrap">
				<form action="pay.wtf" method="post" id="debitcard_form">
					<input type="hidden" name="type" value="alipay" />
					<input type="hidden" name="from" value="MOBILE" />
					<input type="hidden" name="price" value="" id="debitcard_price"/>
					<input type="hidden" name="subject" value="" id="debitcard_subject"/>
					
					<fieldset>
					 	<legend>您选择了储蓄卡支付充值方式。</legend>
						<label>“爱看”账号：</label>
						<div>
							<input type="text" name="account" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label>“爱看”密码：</label>
						<div>
							<input type="password" name="psw" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label for="add">开通时长：</label>
						<div class="w260">
					<% 
						for(MemberShipFee fee : fees){
							double _price = fee.getPrice().multiply(fee.getDiscount()).doubleValue();
					%>
						<input name="duration" type="radio" value="<%= fee.getMonths()%>" onclick="setHiddenValue('debitcard_price', 'debitcard_price_span', <%= _price%>, 'debitcard_subject', <%= fee.getMonths()%>);"><%= fee.getMonths()%>个月
					<%
						}
					%>
						</div>
					</fieldset>
					<fieldset>
						<label for="add">付款金额：</label>
						<div>
						<span id="debitcard_price_span"></span>
						</div>
					</fieldset>
					<fieldset>
						<label for="add">选择银行：</label>
						<div>
						<% 
							List<SupportTopPayChannel> quickPays = pcr.getSupportedPayChannelList();
					        if (quickPays != null) {
					            for (SupportTopPayChannel supportTopPayChannel : quickPays) {
					                if (supportTopPayChannel.getCashierCode().equals("DEBITCARD")) {//储蓄卡支付
					                	List<SupportSecPayChannel> secPays = supportTopPayChannel.getSupportSecPayChannelList();
					                	for(SupportSecPayChannel secPayChannel : secPays){
					                		
					    %>
					    	<input name="channel" type="radio" value="<%= secPayChannel.getCashierCode()%>"><%= secPayChannel.getName()%>
					    <%
					                	}
					                	
					                	break;
					                }
					            }    
					        }       
						%>
						</div>
					</fieldset>
					<fieldset>
						<input name="" class="small-box" type="checkbox" value="">
						 <p>我已阅读并接受<a href="provision.jsp" target="_self">账户使用条款</a>。</p>
					</fieldset>
					<input type="button" value="储蓄卡支付" onclick="javascript:submitForm(this, 'debitcard_form');" /><a class="quxiao" href="#">取消</a>
				</form>
			</div>
		</div>
		
		<div id="creditcard_panel" class="chongzhi-right" style="display:none">
			<h4>信用卡充值信息</h4>
			<div class="chongzhi-text-wrap">
				<form action="pay.wtf" method="post" id="creditcard_form">
					<input type="hidden" name="type" value="alipay" />
					<input type="hidden" name="from" value="MOBILE" />
					<input type="hidden" name="price" value="" id="creditcard_price"/>
					<input type="hidden" name="subject" value="" id="creditcard_subject"/>
				
					<fieldset>
					 	<legend>您选择了信用卡支付充值方式。</legend>
						<label>“爱看”账号：</label>
						<div>
							<input type="text" name="account" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label>“爱看”密码：</label>
						<div>
							<input type="password" name="psw" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label for="add">开通时长：</label>
						<div class="w260">
						
					<% 
						for(MemberShipFee fee : fees){
							double _price = fee.getPrice().multiply(fee.getDiscount()).doubleValue();
					%>
						<input name="duration" type="radio" value="<%= fee.getMonths()%>" onclick="setHiddenValue('creditcard_price', 'creditcard_price_span', <%= _price%>, 'creditcard_subject', <%= fee.getMonths()%>);"><%= fee.getMonths()%>个月
					<%
						}
					%>
						
						</div>
					</fieldset>
					<fieldset>
						<label for="add">付款金额：</label>
						<div>
						<span id="creditcard_price_span"></span>
						</div>
					</fieldset>
					<fieldset>
						<label for="add">选择银行：</label>
						<div>
						<% 
					        if (quickPays != null) {
					            for (SupportTopPayChannel supportTopPayChannel : quickPays) {
					                if (supportTopPayChannel.getCashierCode().equals("CREDITCARD")) {//储蓄卡支付
					                	List<SupportSecPayChannel> secPays = supportTopPayChannel.getSupportSecPayChannelList();
					                	for(SupportSecPayChannel secPayChannel : secPays){
					                		
					    %>
					    	<input name="channel" type="radio" value="<%= secPayChannel.getCashierCode()%>"><%= secPayChannel.getName()%>
					    <%
					                	}
					                	
					                	break;
					                }
					            }    
					        }       
						%>
						</div>
					</fieldset>
					<fieldset>
						<input name="" class="small-box" type="checkbox" value="">
						 <p>我已阅读并接受<a href="provision.jsp" target="_self">账户使用条款</a>。</p>
					</fieldset>
					<input type="button" value="银行信用卡支付" onclick="javascript:submitForm(this, 'creditcard_form');" /><a class="quxiao" href="#">取消</a>
				</form>
			</div>
		</div>
		
		<div id="ali_panel" class="chongzhi-right" style="display:none">
			<h4>支付宝充值信息</h4>
			<div class="chongzhi-text-wrap">
				<form action="pay.wtf" method="post" id="alipay_form">
					<input type="hidden" name="type" value="alipay" />
					<input type="hidden" name="from" value="MOBILE" />
					<input type="hidden" name="price" value="" id="ali_price"/>
					<input type="hidden" name="subject" value="" id="ali_subject"/>
				
					<fieldset>
					 	<legend>您选择了支付宝支付充值方式。</legend>
						<label>“爱看”账号：</label>
						<div>
							<input type="text" name="account" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label>“爱看”密码：</label>
						<div>
							<input type="password" name="psw" value="" />
						</div>
					</fieldset>
					<fieldset>
						<label for="add">开通时长：</label>
						
						<div class="w260">
					<% 
						for(MemberShipFee fee : fees){
							double _price = fee.getPrice().multiply(fee.getDiscount()).doubleValue();
					%>
						<input name="duration" type="radio" value="<%= fee.getMonths()%>" onclick="setHiddenValue('ali_price', 'ali_price_span', <%= _price%>, 'ali_subject', <%= fee.getMonths()%>);"><%= fee.getMonths()%>个月
					<%
						}
					%>
						</div>
					</fieldset>
					<fieldset>
						<label for="add">付款金额：</label>
						<div>
						<span id="ali_price_span"></span>
						</div>
					</fieldset>
					<fieldset>
						<input name="" class="small-box" type="checkbox" value="">
						 <p>我已阅读并接受<a href="provision.jsp" target="_self">账户使用条款</a>。</p>
					</fieldset>
					<input type="button" value="支付宝支付" onclick="javascript:submitForm(this, 'alipay_form');" /><a class="quxiao" href="#">取消</a>
				</form>
			</div>
		</div>
		
		
		
	</div>
</body>
</html>