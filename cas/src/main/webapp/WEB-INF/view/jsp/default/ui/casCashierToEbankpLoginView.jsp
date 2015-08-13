<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<%
String eMallField = (String)request.getAttribute("eMallField");
eMallField = (eMallField==null? "": eMallField.trim());

String forwardFlag = (String)request.getAttribute("forwardFlag");
forwardFlag = (forwardFlag==null? "": forwardFlag.trim());

String redirectUrl = (String)request.getAttribute("redirectUrl");
//redirectUrl = (redirectUrl==null? CommomProperty.getDBManager().getsmsProperty("cas.eBankUrl"): redirectUrl.trim());
//redirectUrl = "http://82.200.50.154:8180/memberLogin.jsp";
%>

<script >
       window.onload=function(){
    	   var form = document.getElementById("toEbankp");
    	   form.submit();
       };
    </script>
</head>
<body>
<form method="post" id="toEbankp" action="<%=redirectUrl%>">
	<input name="eMallField" type="hidden" value="<%=eMallField%>" />
	<input name="forwardFlag" type="hidden" value="<%=forwardFlag%>" />
</form>
</body>
</html>