<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.icbc.emall.cache.tair.TairCacheManager" %>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<%@ page import="org.owasp.esapi.reference.DefaultRandomizer" %>
<%@ page import="com.icbc.emall.common.utils.Crypt" %>
<%@ page import="com.icbc.emall.common.utils.ApplicationConfigUtils" %>
<%@ page import="com.icbc.emall.ad.service.AdInfoService" %>
<%@ page import="com.icbc.emall.common.utils.SpringContextLoaderListener" %>
<%@ page import="com.icbc.emall.common.utils.Globe.LoginTarget" %>
<%@ page import="java.util.Date" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.icbc.emall.Constants" %>
<%@ page import="com.icbc.emall.ad.model.AdInfo" %>
<%@ page import="com.icbc.emall.cas.util.PasswordDictionaryUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="com.icbc.emall.util.BaseServiceData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/taglib/icbc-taglib.tld" prefix="icbc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>商城用户登录</title>
<%
CommomProperty instance = CommomProperty.getDBManager();

StringBuffer eBankUrl= new StringBuffer();

//基本服务 个人网银登陆接口地址跳转至   https://b2c.icbc.com.cn/icbc/Emall/main/login_mall.jsp
if("1".equals(BaseServiceData.baseServiceEnable))
{
	eBankUrl.append(instance.getsmsProperty("cas.eBankUrl.baseService"));
}
else
{
	eBankUrl.append(instance.getsmsProperty("cas.eBankUrl"));
}
/* String emallfield = "{\"sceneType\":\"1\"}";
String emallfieldEnc = Crypt.encrypt(emallfield, "UTF-8", 1,0); */

/* String service = request.getParameter("service");
HashMap<String, String> map = new HashMap<String,String>();
map.put("sceneType","1");
map.put("service",service);
JSONObject obj = JSONObject.fromObject(map);
String emallfield = obj.toString();
//String emallfield = "{\"sceneType\":\"1\"}";
String emallfieldEnc = Crypt.encrypt(emallfield, "UTF-8", 1,0);
eBankUrl.append("?eMallField=").append(emallfieldEnc).append("&forwardFlag=1"); */

String eMallField = request.getParameter("eMallField");
String service = request.getParameter("service");
String casClientUrl = instance.getsmsProperty("cas.client.url");
if(eMallField != null && !"".equals(eMallField.trim())){
	eBankUrl.append("?eMallField=").append(eMallField).append("&forwardFlag=1");
}else if(service != null && !"".equals(service.trim())){
	String sceneType = request.getParameter("sceneType");
	HashMap<String, String> map = new HashMap<String,String>();
	if(sceneType != null && !"".equals(sceneType.trim())){
		map.put("sceneType",sceneType);
	}else{
		map.put("sceneType","1");
	}

	map.put("service",service);
	JSONObject obj = JSONObject.fromObject(map);
	String emallfield = obj.toString();
	String emallfieldEnc = Crypt.encrypt(emallfield, "UTF-8", 1,0);
	eBankUrl.append("?eMallField=").append(emallfieldEnc).append("&forwardFlag=1");
}else{
	HashMap<String, String> map = new HashMap<String,String>();
	map.put("sceneType","1");
	map.put("service",casClientUrl);
	JSONObject obj = JSONObject.fromObject(map);
	String emallfield = obj.toString();
	String emallfieldEnc = Crypt.encrypt(emallfield, "UTF-8", 1,0);
	eBankUrl.append("?eMallField=").append(emallfieldEnc).append("&forwardFlag=1");
}


String mallRegUtl =instance.getsmsProperty("cas.mallRegUrl");
String mallFindPasUrl =instance.getsmsProperty("cas.mallFindPasUrl");
String mallDefUrl =instance.getsmsProperty("cas.mallDefUrl");
String pre=instance.getsmsProperty("mall.product.preview.url");

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
String b2bMerchantLoginUrl = "";
if("1".equals(isb2b)){  //b2b跳转过来的登陆
	//找回密码跳转url修改为b2b专用的页面
	String serviceFlag = request.getParameter("service") == null ? "" : "&service=" + java.net.URLEncoder.encode(request.getParameter("service"), "UTF-8");
	b2bMerchantLoginUrl = request.getContextPath() + "/login?isb2b=1&to=" + LoginTarget.MERCHANT_PERSION + serviceFlag;
	
	eBankUrl = new StringBuffer();
	eBankUrl.append(request.getContextPath() + "/login?isb2b=1&to=" + LoginTarget.B2B_PERSON_EBANK + serviceFlag);
	
	//跳转注册时带上b2b的回跳url
	String b2bMallUrl = instance.getsmsProperty("b2b.cas.client.url");
	mallRegUtl += (mallRegUtl.indexOf("?") > 0 ? "&" : "?") + "redirectUrl=" + java.net.URLEncoder.encode(b2bMallUrl.substring(0, b2bMallUrl.lastIndexOf("/")), "UTF-8");
}
%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<script type="text/javascript">
var ctx = '${ctx}';
var dict=<%=sb.toString() %>
</script>
			
<link rel="stylesheet" type="text/css" href="${ctx}/styles/base.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/styles/global.css"/>
<link rel="stylesheet" type="text/css" href="${ctx}/styles/regist.css" />

<script type="text/javascript" src="${ctx}/scripts/jquery-1.8.3.min.js"></script>

<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.include.pack-1.1.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox.src.js"></script> 
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox-zh-CN.js"></script>
<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript" src="${ctx}/scripts/pebank_browsercompatible.js"></script>
<script type="text/javascript" src="${ctx}/scripts/login.js"></script>
</head>
<body onload="load()">
<!-- 包含头部 -->
<div id="header" class="content">
	<div class="logo">
	<a  href="<%=mallDefUrl%>"  ><img src="images/logo.png"  title="融e购首页"></a>
	</div><h1>&nbsp;&nbsp;|&nbsp;&nbsp;登录</h1>
	</div>
	<div class="line"></div>
</div>
<!-- /包含头部 --> 
<!-- 主体 -->
<ul id="selectEntry" class="tabLi" style="display: none;width:950px;margin:0 auto;margin-bottom:10px;">
	<a href="<%=b2bMerchantLoginUrl %>"><li class="">企业登录</li></a>
	<a href="javascript:;" ><li class="on">个人登录</li></a>
</ul>
<div id="entry_n" class="rel content clm shopuser">
 	<div class="form" id="login_list">
    <ul class="tab_user_t cfx">    
      <li style="backgournd:none;opacity:1;filter: alpha(opacity=100);">
       	<img src="/images/bg12.png" />
       </li>
    </ul>
  
    <!--start 网上银行用户登录-->
    <div class="tab_con"  style="display:none;"> 
    </div>
   <!--end 网上银行用户登录--> 
      <!--start 商城用户登录-->
    <form:form method="post" commandName="${commandName}" htmlEscape="true"   id="form1" name="form1" >
		    <div class="tab_con"  style="display:block;"> 
		      <!--start 个人登录-->
		      		<div class="errors" id="errors">
							  	  		<form:errors path="*" id="msg" element="span" />
							  	  		<c:if test="${not empty param.error}">
							  	  	 		<span class="errInfo" style="font-weight:normal;font-size:13px;color:red;"></span>
							  	  		</c:if>&nbsp;
					</div>
		      <ul class="tab_con_g">
		        <li><label for="textfield">用户名/手机号：</label></li>
		        <li>
		          <input type="text" class="txt" name="username" id="username" maxlength="20" tabindex="1" autocomplete="off"  onKeyUp="getfocus1(this,'',event);" />
		        </li>
		        <li><a href="javascript:window.location='<%=mallFindPasUrl %>'+'?username='+jQuery('#username').val()"  target="_parent"  class="fr">忘记密码？马上找回</a>
		          <label for="textfield2">登录密码：</label>
		        </li>
		        <li class="login_p">
			         <span class="txt_box">
			        	<i class="icon_key"></i>
			        	<a href="javascript:;" onclick="dis_icon_key()"><span id="YUse"  name="YUse">禁用密码安全控件</span><span id="NUse"  name="NUse" style="display:none">启用密码安全控件</span></a>
			        </span>
 					<div id="pa1" class="txt3 txt_pad" style="cursor:text">
			        	<icbc:SafePass id="safeEdit1"	name="logonCardPass"  tabindex="2"
			        	type="paypass"   
						 objectWidth="257"
						objectHeight="30" 
						minLength="8" maxLength="30" rule="10111"
						uniqueID="<%=UID %>" custom="nextElemId=\"verify\"  onKeyUp=\"getfocus1(this,'KeyPart',event);\" "
						isPassword="true" />
					</div>
					<div id="pa2" style='display:none' class="txt3 txt_pad">
		          		<input type="password" class="txt_pad2" name="password" id="password"  maxlength="30" tabindex="3" onKeyUp="getfocus1(this,'j_captcha', event)"/>
		          	</div>
		        </li>
		        <li><label for="textfield3">验证码：</label></li>
		        <li>
		         	<span id="cap1" class="txt_pad3" style="cursor:text">
		          	    <icbc:SafePass id="KeyPart" name="verifyCode" tabindex="4" 
								type="verifyCode"
								objectWidth="176"
								objectHeight="33"
								minLength="4" maxLength="4" rule="10111"
								uniqueID="<%=UID %>" custom="nextElemId=\"login\"  onKeyUp=\"getfocus1(this,'login',event);\" "
								isPassword="false" />
		          	   </span>
		        	   <span id="cap2"  class="txt_pad3" style="display:none" >
		          	           <input type="text"  name="j_captcha" class="txt txt-t"  id="j_captcha"  maxlength="4" tabindex="5" onKeyUp="getfocus1(this,'login',event);"/>
		          	    </span>
		          	    <img src="${pageContext.request.contextPath}/captcha.jpg?timestamp=<%=System.currentTimeMillis() %>"  id="validateCode"/>
		          	            <a href="javascript:void(0);" id="imgAsynchHttpReqArea" class="num_link">看不清换一张</a> 
		          </li>
		          <li>您使用工行商城，须遵守《<a href="<%=pre+"/getMallRegAgreement.jhtml" %>" target="_new" ><%=mallAgreementText %></a>》</li>
		        <li>
		        <icbc:SafePass id="safeSubmit1" name="safeSubmit1" type="submit" />
		          <button type="button"  id="button" class="n_btn" name="login" tabindex="6" ><span class="ico_lg">登录</span></button>
		          <a href="<%=mallRegUtl %>"  target="_parent" class="ml20">免费注册成为工行商城用户</a></li>
		      </ul>
		      <!--end 个人登录--> 
		      		<ul>
								               <input type="hidden" name="lt" value="${loginTicket}" />
								               <input type="hidden" name="execution" value="${flowExecutionKey}" />
								               <input type="hidden" name="_eventId" value="submit" />
								               <input type="hidden"  id="targetService" name="service" value="<%=casClientUrl%>"/>
								               <input type="hidden" name="dictKey" value="<%=dictKey %>"/>
								               <input type="hidden"  id="isSafe" name="isSafe"  value="1"/>
								               <input type="hidden" name="randomId" value="<%=UID%>"/>
								               <input type="hidden" id="isb2b" name="isb2b" value="<%=isb2b %>" />
					</ul>
		    </div>
    </form:form>
  
       <!--end 商城用户登录--> 
  </div>
</div>
<!-- /主体 -->
<div id="divss" hidefocus="true" ></div>
<!-- 尾部 -->
<div id="footer"><span class="fl"><a href="http://www.icbc.com.cn" target="_blank">工行首页</a>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;<a href="<%=mallDefUrl%>"  target="_blank">融e购首页</a>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;中国工商银行版权所有</span><span class="copyright">京ICP证030247号&nbsp;&nbsp;<a href="http://www.hd315.gov.cn/beian/view.asp?bianhao=0102000120100015"><img src="images/del/icp2.png" width="30" height="36"></a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://search.szfw.org/cert/l/CX20120917001631001676"><img src="images/del/chengxin.jpg" /></a></span></div>
</body>
<script type="text/javascript" src="${ctx}/scripts/caspv.js"></script>
</html>