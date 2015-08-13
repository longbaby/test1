<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>商城协议</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<script type="text/javascript">var ctx = '${ctx}';</script>
<%-- <script type="text/javascript" src="${ctx}/scripts/jquery.include.pack-1.1.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox.src.js"></script> 
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox-zh-CN.js"></script> --%>
<script type="text/javascript" src="${ctx}/scripts/jquery-1.8.3.min.js"></script>
<script type="text/javascript">
var ctx = '${ctx}';
window.focus();
function xy(){
	$.ajax({
		type:"POST",
		url:"/compact",
		dataType:"json",
		async: false,
		success:function(jsonData){
			if(jsonData.str !=''){
				$('#errInfo').html(jsonData.str);
			}
		},
		error:function(e){
			alert("系统错误");
		}
	});
}
</script>
<body onload='xy()'>
	<div class="cinoact" id="cinoact">
				<form:errors path="*" id="msg" element="span" />
				<span id="errInfo" class="errInfo" style="font-weight:normal;font-size:13px;"></span>
	</div>
</body>
</html>