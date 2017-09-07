<%@ page language="java" import="com.rest.bean.Resource" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>资源获取</title>
</head>
<body>
	<% Resource resource = (Resource)request.getAttribute("resource"); %>
	<label>resource</label><br>
	<label>id：</label><%=resource.getId() %><br>
	<label>name：</label><%=resource.getName() %><br>
	<label>number：</label><%=resource.getNumber() %><br>
</body>
</html>