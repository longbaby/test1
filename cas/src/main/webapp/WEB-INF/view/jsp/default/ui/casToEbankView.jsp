<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<%@ page import="com.icbc.emall.util.BaseServiceData" %>
<%@ page import="com.icbc.emall.common.utils.Crypt" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.icbc.emall.util.BaseServiceData" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>


<%
//打包加密emallField=｛service=上送的,sceneType=上送的｝、传forwardFlag=1
System.out.println("casToEbankView.jsp==>begin");
String eMallFieldValue="";
String service = request.getParameter("service");
String sceneType = request.getParameter("sceneType");
Map map = new HashMap<String,String>();
map.put("sceneType",sceneType);
map.put("service",service);
JSONObject obj = JSONObject.fromObject(map);
eMallFieldValue = obj.toString();
String eMallFieldKey = "";
try {
	eMallFieldKey = Crypt.encrypt(eMallFieldValue, "UTF-8", 1, 0);
} catch (Exception e) {
	System.out.println("casToEbankView.jsp==>3desenc:"+e);
}
CommomProperty instance = CommomProperty.getDBManager();
String ebankClientUrl=null;

//基本服务 个人网银登陆接口地址跳转至   https://b2c.icbc.com.cn/icbc/Emall/main/login_mall.jsp
if("1".equals(BaseServiceData.baseServiceEnable))
{
	ebankClientUrl =  instance.getsmsProperty("cas.eBankUrl.baseService");
}
else
{
	ebankClientUrl=instance.getsmsProperty("cas.eBankUrl");
}

 //response.sendRedirect(casClientUrl);
 	response.sendRedirect(ebankClientUrl+"?"+"eMallField="+eMallFieldKey+"&forwardFlag=1");
%>
<body>
</body>
</html>