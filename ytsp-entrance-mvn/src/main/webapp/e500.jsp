<%@ page language="java" isErrorPage="true" pageEncoding="UTF-8"%>

<% 
	String path = request.getContextPath(); 
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/"; 
%> 

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>哎哟，出错了!!</title>
		<base href="<%=basePath%>"> 
	</head>
	<%response.setStatus(HttpServletResponse.SC_OK);%>
	
	<body>
		<div style="width:550px; height:300px; background-color:#CCC;">
			<div style="margin:20px; float:left;">
				<b><%=exception.getMessage() %></b>
			</div>
		</div>
		
	</body>
</html>