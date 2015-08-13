<%@page import="java.net.URLDecoder"%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ page import="com.icbc.emall.cache.tair.TairCacheManager"%>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty"%>
<%@ page import="org.owasp.esapi.reference.DefaultRandomizer"%>
<%@ page import="com.icbc.emall.common.utils.Crypt"%>
<%@ page import="com.icbc.emall.common.utils.ApplicationConfigUtils"%>
<%@ page import="com.icbc.emall.ad.service.AdInfoService"%>
<%@ page import="com.icbc.emall.common.utils.SpringContextLoaderListener"%>
<%@ page import="com.icbc.emall.common.utils.Globe.LoginTarget"%>
<%@ page import="java.util.Date"%>
<%@ page import="com.icbc.emall.Constants"%>
<%@ page import="com.icbc.emall.ad.model.AdInfo"%>
<%@ page import="com.icbc.emall.cas.util.PasswordDictionaryUtil"%>
<%@ page import="java.util.*"%>
<%@ page import="com.icbc.emall.util.BaseServiceData"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/taglib/icbc-taglib.tld" prefix="icbc"%>

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
String emallfield = "{\"sceneType\":\"1\"}";
String emallfieldEnc = Crypt.encrypt(emallfield, "UTF-8", 1,0);
eBankUrl.append("?eMallField=").append(emallfieldEnc).append("&forwardFlag=1");
String mallRegUtl =instance.getsmsProperty("cas.mallRegUrl");
String mallFindPasUrl =instance.getsmsProperty("cas.mallFindPasUrl");
String mallDefUrl =instance.getsmsProperty("cas.mallDefUrl");
//String casClientUrl = instance.getsmsProperty("cas.client.url");
String pre=instance.getsmsProperty("mall.product.preview.url");

String seq="1234567890";
String randomId = DefaultRandomizer.getInstance().getRandomString(18, seq.toCharArray());
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

String page_netType="111";
String isEmallLogin="2";

//b2b添加代码
String isb2b = request.getParameter("isb2b");
String b2bMerchantLoginUrl = "";
String b2bPersonLoginUrl = "";
String serviceFlag = "";
if("1".equals(isb2b)){  //b2b跳转过来的登陆
		
	serviceFlag = request.getParameter("service") == null ? "" : request.getParameter("service");
	String serviceUrl = request.getParameter("service") == null ? "" : "&service=" + java.net.URLEncoder.encode(request.getParameter("service"), "UTF-8");
	b2bMerchantLoginUrl = request.getContextPath() + "/login?isb2b=1&to=" + LoginTarget.MERCHANT_PERSION + serviceUrl;
	b2bPersonLoginUrl = request.getContextPath() + "/login?isb2b=1&" + serviceUrl;
}
%>


<c:set var="ctx" value="${pageContext.request.contextPath}" />
<script type="text/javascript">
	var ctx = '${ctx}';
	var dict =<%=sb.toString()%>
	
</script>

<link rel="stylesheet" type="text/css" href="${ctx}/styles/base.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/styles/global.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/styles/regist.css" />

<script type="text/javascript" src="${ctx}/scripts/jquery-1.8.3.min.js"></script>

<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.include.pack-1.1.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox.src.js"></script> 
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox-zh-CN.js"></script>
<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript" src="${ctx}/scripts/pebank_browsercompatible.js"></script>
<script type="text/javascript" src="${ctx}/scripts/login.js"></script>
<script type="text/javascript">
//处理safari版控件 回车自动登录
//1） 增加JS函数，函数名称必须是“XReturnDown”，safeSubmit函数为页面中原来响应提交功能的函数；
var isEntered=false;//防止连续点击enter键造成浏览器死掉
function XReturnDown(item)
{
  if(item == "safeSubmit1"&&!isEntered)
  {
  	<%if(((String)request.getHeader("User-Agent")).toLowerCase().indexOf("chrome/")!=-1){%>
  		try{
  			document.all.KeyPart.removeFocus();//移除控件的焦点，避免后台钩子锁死键盘
  		}catch(e){}
  	<%}else{%>
    document.getElementById('submitaid').focus();
    <%}%>
    mySubmit();

  }else if(item == "verify")
  {
  	document.all.KeyPart.focus();
  }
}
var desktop=false;
function winopenrisk(){
	window.open("../main/riskTipMore.jsp","","top=0, left=0,toolbar=yes, menubar=yes, scrollbars=yes, resizable=yes, status=yes,location=yes");
}
function winopenspecial(){
	window.open("../main/specialTipMore.jsp","","top=0, left=0,toolbar=yes, menubar=yes, scrollbars=yes, resizable=yes, status=yes,location=yes");
}
function disagreement(){
	jQuery("#errors").text("您尚未接受我行电子银行章程与服务协议，无法登录");
	return;
}
function getfocus1(name, event){
	if(pebankBrowserCompatible.getKeycode(event)==13){
		<%if(((String)request.getHeader("User-Agent")).toLowerCase().indexOf("firefox")!=-1 || ((String)request.getHeader("User-Agent")).toLowerCase().indexOf("chrome/")!=-1){%>
			document.getElementById("divss").focus();
		<%}%>
		if(name=="login"){
			mySubmit();
		}else{
			eval("document.all."+name).focus();
		}
	}
}

function mySubmit(){
	try{

		if(pebankBrowserCompatible.isSafari()){
			if(typeof(navigator.mimeTypes['application/x-npsubmit-plugin'])=="undefined"){
				jQuery("#errors").text("您尚未正确安装工行网银控件，请您先下载安装工行网银控件");
				return;
			}else{
				var vs = "";
				try{
					vs=document.all.safeEdit1.getVersion();
				}catch(e){vs="";}
				<%--if(vs==""||'<%=pebank_macos_plugin_version%>'!=document.all.safeEdit1.getVersion()){
					jQuery("#errors").text("您安装的工行网银控件版本过低，请您先下载安装工行网银控件");
					return;
				}--%>
			}
		}
		var cardNum=document.logonform.logonCardNum.value;
		if(cardNum.length>17&&cardNum.length!=19){
				jQuery("#errors").text("请输入正确的卡(账)号或登录ID!");
				logonform.logonCardNum.select();
				logonform.logonCardNum.focus();
				return;
		}
		if(cardNum.length==0){
				jQuery("#errors").text("卡(账)号或登录ID不能为空!");
				logonform.logonCardNum.select();
				logonform.logonCardNum.focus();
				return;
		}

		if (document.all.safeEdit1.getLength()<4){
			jQuery("#errors").text("密码长度至少应该为四位!");
			document.all.safeEdit1.focus();
			return;
		}
		if (!document.all.safeEdit1.isValid()){
	   		jQuery("#errors").text("输入的密码不合法，请重新输入！");
	   		document.all.safeEdit1.focus();
			return ;
		}
		if (!document.all.KeyPart.isValid()){
			jQuery("#errors").text("输入的验证码不合法，请重新输入！");
		  	document.all.KeyPart.focus();
			return ;
		}

		//var cardbinstr=cardBinList.SupportCardBin(9);
		//var cardbin="|"+cardBinList.getCardBinNoValid(cardNum)+"|";

		//不判断17位年金账号
		/*
		if(cardNum.length>15&&!isValidCardAndAcctPublic(cardNum)&&cardNum.length!=17){
			jQuery("#errors").text("请输入正确的卡(账)号!");
			logonform.logonCardNum.select();
			logonform.logonCardNum.focus();
			return;

		}else{*/
			//非法别名
			var re =/\W/;
			if (re.test(cardNum)){
				jQuery("#errors").text("请输入正确的登录ID!");
				logonform.logonCardNum.select();
				logonform.logonCardNum.focus();
				return;
			}
		//}


		try{
			document.getElementById("submitkey").innerHTML = "";
			document.getElementById("nosubmitkey").style.display = "";
		}catch(exception){}
		try{
			document.all.safeSubmit1.reset();
			document.all.KeyPart.commitKeyPart(document.all.safeEdit1);
			document.all.safeEdit1.commit(document.all.safeSubmit1);
			document.all.KeyPart.commitKeyPart(document.all.KeyPart);
			document.all.KeyPart.commit(document.all.safeSubmit1);
			document.all.safeSubmit1.submit(logonform);
		}catch(exception){
			jQuery("#errors").text("您尚未正确安装工行网银控件，请您先下载安装工行网银控件");
		}
		isEntered=true;

		return false;
	}catch(Exception){
		jQuery("#errors").text("登录提交错误");
		return ;
	}
}
</script>
</head>
<body onload="loadB2BTab()">
	<!-- 包含头部 -->
	<div id="header" class="content">
		<div class="logo">
			<a href="<%=mallDefUrl%>"><img src="images/logo.png"
				title="融e购首页"></a>
		</div>
		<h1>&nbsp;&nbsp;|&nbsp;&nbsp;登录</h1>
	</div>
	<div class="line"></div>
	</div>
	<!-- /包含头部 -->
	<!-- 主体 -->
	<form id="id_form_eBank" name="logonform" method="post" action="/eBankLogin">
	<input type="hidden" id="isb2b" name="isb2b" value="<%=isb2b %>" />
	<input type="hidden" name="randomId" value="<%=randomId%>"/>
	<input type="hidden" name="netType"	value="<%=page_netType %>"> 
	<input type="hidden" name="isEmallLogin" value="<%=isEmallLogin%>">
	<input type="hidden" name="service" value="<%=serviceFlag%>">
	
	<ul id="selectEntry" class="tabLi" style="display: none;width:950px;margin:0 auto;margin-bottom:10px;">
		<a href="<%=b2bMerchantLoginUrl %>"><li class="">企业登录</li></a>
		<a href="javascript:;" ><li class="on">个人登录</li></a>
	</ul>
	<div id="entry_n" class="rel content clm shopuser">
		<div class="form" id="login_list">
			<ul class="tab_user_t cfx">
				<a href="javascript:;" target="_parent"><li class="one active">&nbsp;<i></i></li></a>
				<a href="<%=b2bPersonLoginUrl%>"><li class="two">&nbsp;<i></i></li></a>
			</ul>

			<!--start 网上银行用户登录-->
			<div class="tab_con">
			
				<div style="color: red" id="id_errInfo_before_jr">&nbsp;</div>
				<ul class="tab_con_g">
					<li><label for="textfield">卡（账）号/用户名：</label></li>
					<li>
						<input type="text" data-role="none" class="txt" name="logonCardNum" id="logonCardNum" value="" maxLength="20" 
							onkeydown="if((pebankBrowserCompatible.isSafari()||pebankBrowserCompatible.isFirefox()||pebankBrowserCompatible.isChrome())&&pebankBrowserCompatible.getKeycode(event)==9)event.preventDefault();"
							onKeyUp="getfocus1('safeEdit1',event)" />
					</li>
					<li><label for="textfield2">网银登录密码：</label></li>
					<li class="login_p">
						<%
							String custom1 = "";
							if(((String)request.getHeader("User-Agent")).toLowerCase().indexOf("chrome/")!=-1){
								custom1="nextElemId=\"verify\"   onKeyUp=\"getfocus1('KeyPart', event);\"";
							}else if(((String)request.getHeader("User-Agent")).toLowerCase().indexOf("safari")!=-1){
								custom1="nextElemtId=\"verify\"";
							}else if(((String)request.getHeader("User-Agent")).toLowerCase().indexOf("firefox")!=-1){
								custom1="nextElemId=\"verify\"  onKeyUp=\"getfocus1('KeyPart', event);\"";
							}else{
								custom1=" onKeyUp=\"getfocus1('KeyPart', event);\"";
							} 
						%> 
						<div id="pa1" class="txt3 txt_pad" style="cursor:text">
							<icbc:SafePass id="safeEdit1" 
								name="logonCardPass"
								type="paypass" 
								objectWidth="375" 
								objectHeight="28" 
								minLength="4" 
								maxLength="30"
								rule="10111" 
								uniqueID="<%=randomId %>" 
								custom="nextElemId=\"verify\"  onKeyUp=\"getfocus1('KeyPart',event);\""
								isPassword="true" />
						</div>
					</li>
					<li><label for="textfield3">验证码：</label></li>
					<li>
						<span id="cap1" class="txt_pad3" style="cursor:text">
		          	    <icbc:SafePass id="KeyPart" name="verifyCode" 
								type="verifyCode"
								objectWidth="176"
								objectHeight="33"
								minLength="4" maxLength="4" rule="10111"
								uniqueID="<%=randomId %>" custom="nextElemId=\"login\"  onKeyUp=\"getfocus1('login',event);\" "
								isPassword="false" />
		          	   </span>
		        	   <span id="cap2"  class="txt_pad3" style="display:none" >
		          	           <input type="text"  name="j_captcha" class="txt txt-t"  id="j_captcha"  maxlength="4" onKeyUp="getfocus1('login',event);"/>
		          	    </span>
		          	    <%--<img src="${pageContext.request.contextPath}/captcha.jpg?timestamp=<%=System.currentTimeMillis() %>"  id="validateCode"/>
		          	            <a href="javascript:void(0);" id="imgAsynchHttpReqArea" class="num_link">看不清换一张</a>
		          	             --%>
          	             <span style="position:relative;top:10px;">
							<%@ include file="verifyimage2.jsp"%> <!-- 验证码jsp -->
							</span>
					</li>
					<li>您使用工行商城，须遵守《<a href="<%=pre+"/getMallRegAgreement.jhtml" %>" target="_new" ><%=mallAgreementText %></a>》
					</li>
					<li>
						<icbc:SafePass id="safeSubmit1" name="safeSubmit1" type="submit" />
						<button type="button" class="n_btn" onClick="javascript:mySubmit();"><span class="ico_lg">登录</span></button>
					</li>
				</ul>
			</div>
			<!--end 网上银行用户登录-->
			<!--start 商城用户登录-->


			<!--end 商城用户登录-->
		</div>
	</div>
	</form>
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