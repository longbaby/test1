<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<%
CommomProperty instance = CommomProperty.getDBManager();
String casClientUrl=instance.getsmsProperty("cas.mobileDefUrl");
 response.sendRedirect(casClientUrl);
%>