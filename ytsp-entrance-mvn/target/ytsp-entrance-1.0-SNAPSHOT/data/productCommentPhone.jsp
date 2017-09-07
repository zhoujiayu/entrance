<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
<head>     
<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
<meta name="viewport" content="target-densitydpi=device-dpi,width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
<title>评价</title>
<style type="text/css">
<!--
body{ font-family:Microsoft YaHei;}
*{margin:0;padding:0;}
.pingjia{width:100%;margin:10px 0 0 0;}
.man_yi_du{width:100%; height:38px;border-top:1px solid #c9caca;border-bottom:1px solid #c9caca;}
.man_yi_du p{float:left;color:#888;line-height:38px; vertical-align:middle;margin-left:12px;font-size:13px;}
.man_yi_du ul{float:left; margin:10px;}
.man_yi_du li{width:17px;height:16px; float:left; list-style:none;margin-right:4px;}
.star_1{background:url(imgs/star_1.png); background-size:100%;}
.star_2{background:url(imgs/star_2.png); background-size:100%;}
.man_yi_du i{ color:#ff8c40;font-size:16px;font-style:normal;line-height:38px; vertical-align:middle;}
.man_yi_du span{float:right;line-height:38px; vertical-align:middle;color:#b7b8b8;margin-right:12px;font-size:13px;}
.pj_list{width:90%;margin:15px auto 0 auto;font-size:14px; overflow:auto; border-bottom:1px dashed #c9caca; padding-bottom:15px;}
.pj_list a{color:#888;float:left;font-size:16px;font-weight:bold;}
.pj_list span{color:#959595;float:right;}
.pj_list p{width:100%;color:#999;float:left;line-height:20px;}
-->
</style>
</head>
	
<body>
	<div class="pingjia">
		<s:iterator value="comments" id="ct">
			<div class="pj_list">
				<a>${ct.userName}</a>
				<span>${ct.commentTime }</span>
				<p>${ct.comment }</p>
			</div>
		</s:iterator>
	</div>
</body>
</html>

