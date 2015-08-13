<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>


<%
String injectTranName = (String)request.getAttribute("injectTranName");
String injectTranData = (String)request.getAttribute("injectTranData");
String injectSignStr = (String)request.getAttribute("injectSignStr");
String logData = (String)request.getAttribute("logData");
String service = (String)request.getAttribute("ebankService");
//service= "http://82.200.47.48:9080/icbc/Emall/main/login_perbank.jsp";
String logDataSignMsg = "";
String forwardFlag = "";

boolean injectFlag = false;

if(logData != null)
	injectFlag = true;

if(injectFlag)
{
	logDataSignMsg = (String)request.getAttribute("logDataSignMsg");
}
else
{
	forwardFlag = (String)request.getAttribute("forwardFlag");
}
%>

<script >
       window.onload=function(){
    	   var form;
    	   if(<%=injectFlag%>==true)
    	   	   form = document.getElementById("injectForm");
    	   else
    		   form = document.getElementById("toEBankLoginForm");
    	   form.submit();
       };
</script>


<body>

<form method="post" id="injectForm" action="<%=service%>">
	<input name="injectTranName" type="hidden" value="<%=injectTranName%>" />
	<input name="injectTranData" type="hidden" value="<%=injectTranData%>" />
	<input name="injectSignStr" type="hidden" value="<%=injectSignStr%>" />
	<input name="logData" type="hidden" value="<%=logData%>" />
	<input name="logDataSignMsg" type="hidden" value="<%=logDataSignMsg%>" />
</form>

<form method="post" id="toEBankLoginForm" action="<%=service%>">
	<input name="injectTranName" type="hidden" value="<%=injectTranName%>" />
	<input name="injectTranData" type="hidden" value="<%=injectTranData%>" />
	<input name="injectSignStr" type="hidden" value="<%=injectSignStr%>" />
	<input name="forwardFlag" type="hidden" value="<%=forwardFlag%>" />
</form>

</body>
</html>