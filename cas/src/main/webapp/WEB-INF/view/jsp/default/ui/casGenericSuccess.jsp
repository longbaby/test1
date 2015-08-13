<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<%
CommomProperty instance = CommomProperty.getDBManager();
String casClientUrl=instance.getsmsProperty("cas.client.url");
 //response.sendRedirect("http://mall.icbc.com.cn:8180/j_spring_cas_security_check");
 
//isb2b是否来自b2b判断
String isb2b = (request.getParameter("isb2b") == null) ? "" : request.getParameter("isb2b");
String loginTo = (request.getParameter("to") == null) ? "" : request.getParameter("to");
if("1".equals(isb2b)){  //如果是来自b2b的登录 
	casClientUrl = instance.getsmsProperty("b2b.cas.client.url");
   	if(LoginTarget.MERCHANT_PERSION.equals(loginTo)) {  //如果是商户登录，返回b2b商户中心
		casClientUrl = instance.getsmsProperty("b2b.cas.client.vendor.url");
	}
}

response.sendRedirect(casClientUrl);
%>
<!-- function redirect(){
	window.location.href="http://mall.icbc.com.cn:8180/j_spring_cas_security_check";
}  -->
