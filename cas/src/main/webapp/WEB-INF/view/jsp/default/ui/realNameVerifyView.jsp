<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>


<%
System.out.println("111");
String emallField = (String)request.getAttribute("emallField");
String service = (String)request.getAttribute("service");
%>

<script >
       window.onload=function(){
    	   var form;
    	   	form = document.getElementById("nameVerify");
    	    form.submit();
       };
</script>


<body>

<form method="post" id="nameVerify" action="<%=service%>">
	<input name="eMallField" type="hidden" value="<%=emallField%>" />
	<input name="forwardFlag" type="hidden" value="1" />
</form>
</body>
</html>