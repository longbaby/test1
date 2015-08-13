<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<%@ page import="org.owasp.esapi.reference.DefaultRandomizer" %>
<%@ page import="com.icbc.common.utils.ApplicationConfigUtils" %>
<%-- <%@ page import="com.icbc.emall.common.utils.Crypt" %> --%>
<%@ page import="com.icbc.emall.cas.util.PasswordDictionaryUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="com.icbc.emall.Constants"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/taglib/icbc-taglib.tld" prefix="icbc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="description" content="">
	<meta name="apple-touch-fullscreen" content="yes" />
	<meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport" />
	<meta content="no" name="apple-mobile-web-app-capable" />
	<meta content="black" name="apple-mobile-web-app-status-bar-style" />
	<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">
	<link rel="apple-touch-icon-precomposed" sizes="114x114" href="images/touch_logo_precomposed.png" />
	<title>登录</title>
	<%
		CommomProperty instance = CommomProperty.getDBManager();
	
		StringBuffer eBankUrl = new StringBuffer();
		String mallRegUtl = instance.getsmsProperty("cas.mobileRegUrl");
		String casClientUrl = instance.getsmsProperty("cas.client.mobile.url");
		String service = request.getParameter("service");
		if(service!=null && service!=""){
			casClientUrl = service;
		}
		
		String seq = "1234567890";
		String UID = DefaultRandomizer.getInstance().getRandomString(18,seq.toCharArray());
	
		List<String> dictList = PasswordDictionaryUtil.generateDict();
		String dictKey = String.valueOf(dictList.hashCode());
		session.setAttribute(dictKey, dictList);
		final String[] convertStrArr = { "'", "\\" };
		StringBuilder sb = new StringBuilder("[");
	
		for (int i = 0; i < dictList.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("'");
			String tempStr = dictList.get(i);
			for (String cs : convertStrArr) {
				if (tempStr.equals(cs)) {
					sb.append("\\");
					break;
				}
			}
			sb.append(tempStr);
			sb.append("'");
		}
		sb.append("];");
	
		String sceneType = request.getParameter("sceneType");
		if(sceneType==null || sceneType==""){
			sceneType="5";
		}
		String mobileRealContextPath = ApplicationConfigUtils.getInstance().getPropertiesValue("realContextPathMobile");
		String staticResourceVersion = ApplicationConfigUtils.getInstance().getPropertiesValue(Constants.STATIC_RESOURCE_VERSION);
	%>
	<c:set var="ctx" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var ctx = '${ctx}';
		var dict=<%=sb.toString() %>
	</script>

	<link href="${ctx}/styles/mobile/onePageLogin/reset_app.css?version=<%=staticResourceVersion%>" rel="stylesheet" type="text/css" />
	<link href="${ctx}/styles/mobile/onePageLogin/login.css?version=<%=staticResourceVersion%>" rel="stylesheet" type="text/css" />
	
	<script src="${ctx}/scripts/mobile/jquery-1.8.3.min.js?version=<%=staticResourceVersion%>"></script>
	<script src="${ctx}/scripts/mobile/common.js?version=<%=staticResourceVersion%>"></script>
	<script src="${ctx}/scripts/mobile/bootstrap-modal.js?version=<%=staticResourceVersion%>"></script>
	<script type="text/javascript" src="${ctx}/scripts/mobile/casPhoneOnePage.js"></script>
	
	<style>
		.a-iph-inner .a-iph-on{ background-image: url(${ctx}/images/mobile/onePageLogin/icon-U.png);background-position:40px 6px;}
		.a-iph-inner .a-iph-off{background-image: url(${ctx}/images/mobile/onePageLogin/icon-U2.png);background-position:6px 6px;}
	</style>	
</head>

<body class="new-bg">
<div class="login-wrap"> 
	<!--start header-->
<%-- 	<header class="header-p">
    	<div class="s-header">
	      	<div class="home-back"></div>
	      	<h1 class="title">登录</h1>
			<div class="g-nav"><a href="#" class="top-refresh-squ-bl top-w" style="display:none;"></a></div> 修改手机银行登录href
		</div>
	</header> --%>
	<!--end header-->
  
	<div class="s-bd-wrap">
	<!--融E购 start-->
	<form:form method="post" commandName="${commandName}" htmlEscape="true" id="id_form_mall" name="form_mall">
   		<div class="login-box">
			<div class="logo"><img src="${ctx}/images/mobile/onePageLogin/ryg.png"></div>

			<div class="myIcbc-i-name">
				<!-- 用户名 -->
				<div class="myIcbc-i-box">
					<div class="tit "><i class="reg-icon reg-i-people"></i></div>
					<div class="msg mag-r10"><div class="msg-info">
						<input type="text" data-role="none" class="def_c" name="username" id="id_username" value="" maxLength="20" placeholder="请输入用户名" />
					</div></div>
				</div>
				
				<!-- 密码 -->
				<div class="myIcbc-i-box" id="id_pswd_box">
					<div class="tit "><i class="reg-icon reg-i-lock"></i></div>
					<div class="msg mag-r10">
						<div class="msg-info group-txt" id="id_pswd_mall_div">
							<input type="password" data-role="none" class="def_c" id="id_pswd_mall_tmp" value="" maxLength="30"	placeholder="请输入密码" onkeydown="enterKeySubmit(event);" /> 
							<input type="hidden" name="passwordPre" id="id_pswd_mall_tmp_submit" value="" maxLength="30" />
	
							<div class="of-on"><div class="app-iphone">
								<label class="a-iph-lab" for="id_a_che_saveKb"> 
									<input type="checkbox" class="a-che" id="id_a_che_saveKb" />
									<div class="a-iph-inner">
										<div class="a-iph-on">ON</div>
										<div class="a-iph-off">OFF</div>
										<span class="a-iph-sw"></span>
									</div>
								</label>
							</div></div>
	
						</div>
					</div>
				</div>
			</div>
			
			<!-- 提示信息 -->
			<div class="l-t-tip col-red" id="id_login_tip" style="display:none;">提示：错误信息</div>
			
			<!-- 链接区域 -->
     		<div class="login-select"> 
	     		<a class=" tit-h6-f14" href="<%=mallRegUtl%>">注册</a> 
	     		<span class="fr tit-h6-f14">
	     		<a id="id_get_pass" 
	     			href="javascript:window.location='<%=mobileRealContextPath%>'+'/mobile/getPassword/getMemberPass.jhtml?username='+jQuery('#id_username').val()">
	     			找回密码
	     		</a> &nbsp; &nbsp; 
	     		<a href="#">遇到问题？</a>
	     		</span>
     		</div>
     		
     		<!-- 登录 -->
    		<div class="l-b-box" id="id_btn_mall" ><a class="login-btn" id="id_a_login_btn">登录</a> </div>
      
      		<!-- 温馨提示 -->
      		<div class="l-t-tip f-8"> <!--  col-grey2 -->
      			<h6 class="f-18 tit-h6-f14">温馨提示：</h6>
        		<p>用户名可以输入网银账号/网银别名/网银手机号/商城用户名/商城手机号</p>
      			<!-- 温馨提示：用户名可以输入网银账号/网银别名/网银手机号/商城用户名/商城手机号 -->
      		</div>		
		</div>
		
	    <!-- hidden -->
	    <div class="errors" id="errors" style="display:none;">
			<form:errors path="*" id="msg" element="span" />
			<c:if test="${not empty param.error}">
				<span style="color:white; height:0px; width:0px; overflow:hidden;"></span>
			</c:if>&nbsp;
	 	</div>
	    <div>
	   		<input type="hidden" name="loginWay" value="9"/>
	        <input type="hidden" name="lt" value="${loginTicket}" />
	        <input type="hidden" name="execution" value="${flowExecutionKey}" />
	        <input type="hidden" name="_eventId" value="submit" />
	        <input type="hidden"  id="targetService" name="service" value="<%=casClientUrl%>"/>
	        <input type="hidden"  id="isSafe" name="isSafe"  value="1"/>
	        <input type="hidden" name="randomId" value="<%=UID%>"/>
	        <input type="hidden" name="dictKey" id="id_dictKey" value="<%=dictKey%>"/>
	        
	        
	        <!-- 密码键盘相关开始 -->
	        <input type="hidden" id="id_pswd_mall" name="password" value=""/>
	        <input type="hidden" id="id_changerule_mall" name="changerule" value=""/>
	        <input type="hidden" id="id_rule_mall" name="rule" value=""/>
	        <input type="hidden" id="id_safeKeyBoard" name="safeKeyBoard" value=""/>
	        <!-- 密码键盘相关结束 -->
		</div>
    <!--融E购 end--> 
	</form:form>
	</div>
</div>
<div class="blk-js"></div>
<div class="blk-f125">&nbsp;</div>
<!--● btn-s-box end -->
<!---------------弹框提示 --------------------->
<!-- <div id="modalShowTip" class="modal hide fade" style="display:none;">
	<div class="modal-header"><a class="close" data-dismiss="modal" ></a>
		<h3>提示信息</h3>
	</div>
	<div class="modal-body">
		<p id="modal_show_tip"></p>
	</div>
	<div class="modal-footer btn-s-box"><a href="#" class="btn btn-white"  data-dismiss="modal">我知道了</a></div>
</div> -->

</body>
</html>
