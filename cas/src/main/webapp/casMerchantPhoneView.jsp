<%@ page language="java" pageEncoding="UTF-8"
	contentType="text/html;charset=UTF-8"%>
<%@ page import="com.icbc.emall.cache.tair.TairCacheManager"%>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty"%>
<%@ page import="org.owasp.esapi.reference.DefaultRandomizer"%>
<%@ page import="com.icbc.emall.common.utils.Crypt"%>
<%@ page import="com.icbc.emall.common.utils.ApplicationConfigUtils"%>
<%@ page import="com.icbc.emall.ad.service.AdInfoService"%>
<%@ page
	import="com.icbc.emall.common.utils.SpringContextLoaderListener"%>
<%@ page import="java.util.Date"%>
<%@ page import="com.icbc.emall.Constants"%>
<%@ page import="com.icbc.emall.ad.model.AdInfo"%>
<%@ page import="com.icbc.emall.cas.util.PasswordDictionaryUtil"%>
<%@ page import="java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/taglib/icbc-taglib.tld" prefix="icbc"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=11; IE=10; IE=9; IE=8; IE=7; IE=EDGE"/>

<title>商户用户登录</title>
<%
CommomProperty instance = CommomProperty.getDBManager();

StringBuffer eBankUrl= new StringBuffer();
eBankUrl.append(instance.getsmsProperty("cas.eBankUrl"));
String emallfield = "{\"sceneType\":\"1\"}";
String emallfieldEnc = Crypt.encrypt(emallfield, "UTF-8", 1,0);
eBankUrl.append("?eMallField=").append(emallfieldEnc).append("&forwardFlag=1");
String mallRegUtl =instance.getsmsProperty("cas.mallRegUrl");
String mallFindPasUrl =instance.getsmsProperty("cas.mallFindPasUrl");
String mallDefUrl =instance.getsmsProperty("cas.mallDefUrl");
String casClientUrl = instance.getsmsProperty("cas.client.url");
String pre=instance.getsmsProperty("mall.product.preview.url");
String merchantHome=instance.getsmsProperty("cas.merchant.home");

String seq="1234567890";
String UID = DefaultRandomizer.getInstance().getRandomString(18, seq.toCharArray());
String mallAgreementText = ApplicationConfigUtils.getInstance().getPropertiesValue("IcbcMallAgreement");

AdInfoService adInfoService = (AdInfoService)SpringContextLoaderListener.getSpringWebApplicationContext().getBean("adInfoService");
AdInfo adInfo = new AdInfo();
adInfo.setColumnId(Constants.ColumnEnum.LOGINAD);
adInfo.setAdEndTime(new Date());
List<AdInfo> adInfoList = adInfoService.getAdInfos(null, null, adInfo);
String adImgUrl = "../images/login_bg.jpg";
String adHrefUrl = "#";
if(adInfoList != null && adInfoList.size()>=1){
	adInfo = adInfoList.get(0);
	if(adInfo.getImgUrl() != null){
		adImgUrl = adInfo.getImgUrl();
	}
	if(adInfo.getAdHrefUrl() != null){
		adHrefUrl = adInfo.getAdHrefUrl();
	}
}



List<String> dictList=PasswordDictionaryUtil.generateDict();
String dictKey=String.valueOf(dictList.hashCode());
session.setAttribute(dictKey, dictList);
final String[] convertStrArr={"'","\\"};
StringBuilder sb=new StringBuilder("[");
for(int i=0;i<dictList.size();i++){
	if(i!=0){
		sb.append(",");
	}
	sb.append("'");
	String tempStr=dictList.get(i);
	for(String cs:convertStrArr){
		if(tempStr.equals(cs)){
			sb.append("\\");
			break;
		}	
	}
	sb.append(tempStr);
	sb.append("'");
}
sb.append("];");

//b2b添加代码
String isb2b = request.getParameter("isb2b");
String b2bSwitch = (String)request.getAttribute(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH);
if(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH_OFF.equals(b2bSwitch)) {
	isb2b="";
}
if("1".equals(isb2b)){  //b2b跳转过来的登陆
	String b2bVendorUrl = instance.getsmsProperty("b2b.cas.client.vendor.url");
	merchantHome = b2bVendorUrl.substring(0, b2bVendorUrl.lastIndexOf("/"));  //b2b首页
}
	
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script type="text/javascript">
var ctx = '${ctx}';
var dict=<%=sb.toString() %>
</script>

<link rel="stylesheet" type="text/css" href="${ctx}/styles/base.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/styles/global.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/styles/regist.css" />

<script type="text/javascript" src="${ctx}/scripts/jquery-1.8.3.min.js"></script>

<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript"
	src="${ctx}/scripts/jquery.include.pack-1.1.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox.src.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox-zh-CN.js"></script>
<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript"
	src="${ctx}/scripts/pebank_browsercompatible.js"></script>
<script type="text/javascript" src="${ctx}/scripts/merchant/phoneValidate.js"></script>
</head>
<body >
	<!-- 包含头部 -->
	<div id="header" class="content">
		<div class="logo">
			<a href="<%=mallDefUrl%>"><img src="images/logo.png"
				title="融e购首页"></a>
		</div>
		<h1>&nbsp;&nbsp;|&nbsp;&nbsp;登录</h1>
	</div>
	<div class="line"></div>
	
	<!-- /包含头部 -->
	<!-- 主体 -->
	<div id="entry_n" class="rel content clm shopuser">
		<div class="form" id="login_list" id="phoneCodePage" style="background: #fff;">
			<!--start 商城用户登录-->
			<form:form method="post" commandName="${commandName}" htmlEscape="true" id="form1" name="form1">
				<h4 id="b2bMerchantTitle" class="yzTit clear" style="display: none;">
		         	<img src="images/man.png" style="margin:10px 10px 0px 0px;float:left;" />
		         	<span style="float:left;font-size:14px">企业用户登录</span>
		      	</h4>
		      	<div class="tab_con"  style="display:block; padding-bottom:30px ">
				<div id="b2cMerchantTitle" style="text-align: center;color:#c7000b;font-size:18px">商户用户登录</div>
				<div class="errors" id="errors">
					<form:errors path="*" id="msg" element="span" />
					<c:if test="${not empty param.error}">
						<span class="errInfo" style="font-weight: normal; font-size: 13px; color: red;"></span>
					</c:if>
				</div>
				
				<ul class="tab_con_g ">
					<li><label for="textfield">手机号：</label></li>
					<li>
						<input type="text" disabled class="txt" name="phoneno" value="${phoneno}" id="textfield" maxLength="20" />
					</li>
					<li class='cfx'>
						<input type="button" id="sendCode" onclick="javascript:sendMobileCode_before()" value="免费获取登录校验码" class="fl mt10" />
						<span id="sendCodeMsg" value="111111"></span> 
					</li>
					<li>
						<label for="textfield">请输入登录校验码：</label>
					</li>
					<li class="cfx">
						<input type="text" class="txt" name="validate" id="validatePhoneCode" maxLength="6" value="" />
						<input type="button" id='reSendCodeA' onclick="javascript:reSendMobileCode()" value="60秒后重新发送" class="fl mt10" />
					</li>
					<li class="cfx">
						<button type="button" id="button" class="n_btn fl mt10" name="login"tabindex="6">
							<span class="ico_lg">登录</span>
						</button>
						<a id="cancle" name="cancle" href="javascript:window.location='<%=merchantHome %>'" class="fr">返回首页面</a>
					</li>
				</ul>
				<!--end 个人登录-->
				<ul>
					<input type="hidden" name="loginWay" value="4" />
					<input type="hidden" name="issubmit" value="2" />
<!-- 					<input type="hidden" name="granttgt" -->
<%-- 						value="${ticketGrantingTicketId}" /> --%>
					<input type="hidden" name="lt" value="${loginTicket}" />
					<input type="hidden" name="execution" value="${flowExecutionKey}" />
					<input type="hidden" name="_eventId" value="submit" />
<!-- 					<input type="hidden" id="targetService" name="service" -->
<%-- 						value="<%=casClientUrl%>" /> --%>
					<input type="hidden" name="dictKey" value="<%=dictKey %>" />
					<input type="hidden" id="isSafe" name="isSafe" value="1" />
					<input type="hidden" name="randomId" value="<%=UID%>" />
					<input type="hidden" id="isb2b" name="isb2b" value="<%=isb2b %>" />
				</ul>
			</div>
		</form:form>

		<!--end 商城用户登录-->
		</div>
		
				
		
		<!-- 短信轰炸验证码拦截 -->
		<div id="validateCodePage" class="form" style="display: block;position: absolute;right: 0px;background: #fff;">
	
		<form method="post" id="form2">
			<h4 id="b2bMerchantTitle2" class="yzTit clear" style="display: none;">
	         	<img src="images/man.png" style="margin:10px 10px 0px 0px;float:left;" />
	         	<span style="float:left;font-size:14px">企业用户登录</span>
	      	</h4>
			<div class="tab_con"  style="display:block; padding-bottom:30px ">
				<div id="b2cMerchantTitle2" style="text-align: center;color:#c7000b;font-size:18px">商户用户登录</div>
				<div class="errors"></div>
				<ul class="tab_con_g ">
		       		<li>
			          <label for="textfield3">短信验证码发送过于频繁，请输入验证码：</label>
			        </li>
			       
			        <li class="text_yanz">
			          <span class="txt_pad3" style="cursor:text;width: 172px;">
			          <input type="hidden" />
			          <input type="text" id="validateCodeValue" name="validateCode" style="width: 170px;"/>
			          </span><img src="${pageContext.request.contextPath}/captcha.jpg?timestamp=<%=System.currentTimeMillis() %>" width="80" height="24" align="absmiddle" class="refreshValidateCode ml10 " id="validateCode" />&nbsp;&nbsp;
			          <a href="javascript:;" id="refreshValidateCode" class="num_link ml10 ">看不清<br/> 换一张</a> 
			          <input type="text" value="" style="width:1px;height:1px;overflow: hidden;border:none;padding:0;margin:0;"/>
			         </li>
			         
			        <li class="mt10">
			        	<input id="validateButton" name="button" type="button" value="获取短信验证码" class="btn btn_red btn_32" onclick="getPhoneCode()"/>
			        	<!--<input type=hidden name="UniqueID" value="${randomobj}">-->
		         	</li>
		       	</ul>
		   </div>
	    </form>
	    </div>
	   
	</div>
	
	
	<!-- /主体 -->
	<div id="divss" hidefocus="true"></div>
	
	<!-- 尾部 -->
	<div id="footer">
		<span class="fl"><a href="http://www.icbc.com.cn"
			target="_blank">工行首页</a>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;<a
			href="<%=mallDefUrl%>" target="_blank">融e购首页</a>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;中国工商银行版权所有</span><span
			class="copyright">京ICP证030247号&nbsp;&nbsp;<a
			href="http://www.hd315.gov.cn/beian/view.asp?bianhao=0102000120100015"><img
				src="images/del/icp2.png" width="30" height="36"></a>&nbsp;&nbsp;&nbsp;&nbsp;<a
			href="https://search.szfw.org/cert/l/CX20120917001631001676"><img
				src="images/del/chengxin.jpg" /></a></span>
	</div>
</body>
</html>