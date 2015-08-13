<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.icbc.emall.cache.tair.TairCacheManager" %>
<%@ page import="com.icbc.finance.pmis.common.CommomProperty" %>
<%@ page import="org.owasp.esapi.reference.DefaultRandomizer" %>
<%@ page import="com.icbc.emall.common.utils.ApplicationConfigUtils" %>
<%@ page import="com.icbc.emall.ad.service.AdInfoService" %>
<%@ page import="com.icbc.emall.common.utils.SpringContextLoaderListener" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.icbc.emall.Constants" %>
<%@ page import="com.icbc.emall.ad.model.AdInfo" %>
<%@ page import="com.icbc.emall.cas.util.PasswordDictionaryUtil" %>
<%@ page import="com.icbc.emall.Constants" %>
<%@ page import="java.util.*" %>
<%@ page import="com.icbc.emall.agreement.service.AgreementService" %>
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

String mallRegUtl =instance.getsmsProperty("cas.pad.RegUrl");
String mallDefUrl =instance.getsmsProperty("cas.padDefUrl");
String casClientUrl = instance.getsmsProperty("cas.client.pad.url");
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

String padRealContextPath = ApplicationConfigUtils.getInstance().getPropertiesValue("realContextPathPad");

AgreementService agreementService = (AgreementService)SpringContextLoaderListener.getSpringWebApplicationContext().getBean("mallAgreementService");
String agreementContent = agreementService.getAgreementContentByName(mallAgreementText);

String sceneType = request.getParameter("sceneType");
if(sceneType==null || sceneType==""){
	sceneType="11";
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
<link rel="stylesheet" type="text/css" href="${ctx}/styles/pad_reset_app.css" />

<script type="text/javascript" src="${ctx}/scripts/jquery-1.8.3.min.js"></script>

<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.include.pack-1.1.js"></script>
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox.src.js"></script> 
<script type="text/javascript" src="${ctx}/scripts/jquery.jBox-zh-CN.js"></script>
<script type="text/javascript" src="${ctx}/scripts/writeObject.js"></script>
<script type="text/javascript" src="${ctx}/scripts/pebank_browsercompatible.js"></script>
<script type="text/javascript" src="${ctx}/scripts/login.js"></script>
<script type="text/javascript" src="${ctx}/scripts/mobile/checkacct.js"></script>
<script type="text/javascript">
var isSysBoard = {
		"ebankPsd" : false,
		"mallPsd" : false
};
$(function(){

	//呼出商城协议
/* 	var proptSO = document.querySelectorAll('.prompt-so');
	var wrapRe = document.querySelector('.wrap-re');
	proptSO[0].addEventListener('touchstart',function(event){
		$(".re-wrap-box").css({'height' : '100%'});
		wrapRe.style.marginLeft='-30.5%';
		$('.bg-black').show(); 
		if( $(document).height() > $(window).height() ){
			$('.bg-black').css({
				'height' : $(document).height()
			});
		}
	}, false);
	
	proptSO[1].addEventListener('touchstart',function(event){
		$(".re-wrap-box").css({'height' : '100%'});
		wrapRe.style.marginLeft='-30.5%';
		$('.bg-black').show(); 
		if( $(document).height() > $(window).height() ){
			$('.bg-black').css({
				'height' : $(document).height()
			});
		}
	}, false);
	
	$('.icon-colse,#icon_agree').live('touchstart',function(){
		$('.bg-black').hide();
		$(".re-wrap-box").css({'height' : ''});
		wrapRe.style.marginLeft='0';
	}); */
	
	$(".prompt-so").click(function(){
		iOSExcuteNativeMethod("native://zhuceViewShow");
	});
	
	var formType = getParam("fromType");
	var errorCode = getParam("errorCode");
	var errMall = $("#id_msg_mall").html();
	//alert("errMall:"+errMall);
	if((typeof(errMall) != "undefined") && errMall != ""){
// 		class="one active" id="id_one
		$("#id_one").attr("class","one");
		$("#id_two").attr("class","two active");
		$('#id_eBank').hide();
	  	$('#id_eMall').show();
	}
	
	if(null != errorCode && "" != errorCode){
		checkErrCode(errorCode);
	}
	
	if("eMallShow" == formType){
		$("#id_one").attr("class","one");
		$("#id_two").attr("class","two active");
		$('#id_eBank').hide();
	  	$('#id_eMall').show();
		//alert(67890);
	}else{
		//alert(90000);
	}
	
	//商城登录提交开始
   	$("#id_btn_mall").click(function(){
   		var username = $("#id_username").val();
   		var usernameLen = $("#id_username").val().length;
   		var password = $("#id_pswd_mall_tmp").val();
   		var passwordLen = $("#id_pswd_mall_tmp").val().length;
   		var keyBoardFlag = $("#id_safeKeyBoard").val()=="1";
   		
   		if(jQuery.trim(username) == ""
			|| username == $("#id_username").attr("default")){
   			$("#id_errInfo_before").html("请输入用户名！");
   			return;
   		}else{
	   		//alert(usernameLen);
	   		if(usernameLen < 4 || usernameLen > 20){
	   			$("#id_errInfo_before").html("用户名错误，请重新输入！");
	   			return ;
	   		}else{
	   			var myRegName = /^[A-Za-z0-9_]{4,20}$/;
				if(!myRegName.test(username)){
					$("#id_errInfo_before").html("用户名错误，请重新输入！");
					return;	
				}
	   		}
   		}
   		
		if(password == null || password == ""){
			$("#id_errInfo_before").html("密码不能为空！");
			return;
		}else{
	   		if(!keyBoardFlag){
	   			var myReg = /^[A-Za-z0-9~!@#$%^&*()_\-+={}\[\]\\|:;"'<>,.?\/]{8,30}$/;
	   			var diReg = /^[0-9]*$/;
	   			var chReg = /^[A-Za-z]*$/;
	   			if(passwordLen <8 || passwordLen > 30 ){
	   				$("#id_errInfo_before").html("密码长度为8到30个字符！");
	   				return;	
	   			}
	   			if(diReg.test(password) && passwordLen <10){
	   				$("#id_errInfo_before").html("密码不可全为数字！");
	   				return;	
	   			}
	   			if(chReg.test(password)  && passwordLen <10){
	   				$("#id_errInfo_before").html("密码不可全为字母！");
	   				return;	
	   			}
	   			if(!myReg.test(password)){
	   				$("#id_errInfo_before").html("密码不可包含非法字符！");
	   				return;	
	   			}
	   		}else{
	   			if(passwordLen < 8){
	   	   			$("#id_errInfo_before").html("密码长度至少应该为八位!");
	   	   			return ;
	   	   		}
	   		}
	   		$("#id_errInfo_before").html("&nbsp;");
		}

		if(keyBoardFlag){
			//把密码键盘产生的密文填写到页面的隐藏域
			//alert("使用了密码控件");
			//submitData();
			if(isSysBoard.mallPsd==false){//安全键盘
				//把密码键盘产生的密文填写到页面的隐藏域
				submitData();	
			}else{//安全键盘切换为系统键盘
				$("#id_changerule_mall").val("");
                $("#id_rule_mall").val("");
				var psdObj = document.getElementById("id_pswd_mall_tmp");
				submitHandler(psdObj);
			}
		}else{
			//alert("未使用密码控件！");
			//submitHandler();
			$("#id_changerule_mall").val("");
            $("#id_rule_mall").val("");
 			$("#id_pswd_mall_tmp_submit").val(password);
 			var psdObj = document.getElementById("id_pswd_mall_tmp");
			submitHandler(psdObj);
		}
		$("#id_form_mall_login").html("登录中...");
    	$("#id_form_mall").trigger("submit");
    	//将a标签解绑click事件防止重复提交
    	$("#id_btn_mall").unbind("click");
	});
  	//商城登录提交结束
	
  	//网银登录提交开始
	$("#id_btn_eBank").click(function(){
		var logonCardNum = $("#id_logonCardNum").val();
		var logonCardNumLen = $("#id_logonCardNum").val().length;
		var pswdeBankTmpLen = $("#id_pswd_eBank_tmp").val().length;
		//alert(logonCardNumLen);
		if(logonCardNumLen < 1){
			$("#id_errInfo_before_jr").html("卡（账）号或登录ID不能为空!");
			return ;
		}else if(logonCardNumLen > 17 && logonCardNumLen != 19){
			$("#id_errInfo_before_jr").html("请输入正确的卡(账)号或登录ID!");
			return ;
		}
		
		//不判断17位年金账号,允许输入14位数字，后台判断是卡号还是别名
		if(logonCardNumLen > 15 && !isValidCardAndAcctPublic(logonCardNum) && logonCardNumLen != 17){
			$("#id_errInfo_before_jr").html("请输入正确的卡(账)号!");
			return;
		}else{
			//非法别名
			var re =/\W/;
			if (re.test(logonCardNum)){
				$("#id_errInfo_before_jr").html("请输入正确的登录ID!");
				return;
			}
		}
		
		if(pswdeBankTmpLen < 4){
			$("#id_errInfo_before_jr").html("密码长度至少应该为四位!");
			return ;
		}
		
		$("#id_errInfo_before_jr").html("&nbsp;");
		
		//把密码键盘产生的密文填写到页面的隐藏域
		//submitData();
		if(isSysBoard.ebankPsd==false){//安全键盘
			//把密码键盘产生的密文填写到页面的隐藏域
			submitData();
		}else{ //安全键盘切换为系统键盘
			$("#id_changerule_eBank").val("");
            $("#id_rule_eBank").val("");
			var psdObj = document.getElementById("id_pswd_eBank_tmp");
			submitHandler(psdObj);
			$("#id_pswd_eBank").val(psdObj.value);
		}
		
		$("#id_form_eBank_login").html("登录中...");
		$("#id_form_eBank").trigger("submit");
		//将a标签解绑click事件防止重复提交
		$("#id_btn_eBank").unbind("click");
	});
	//网银登录提交结束
	
	// 启用安全键盘
	//$("#id_pswd_mall_tmp").bind('click',{id:"keyMall"},callsoftKeyBoard).val("");
	$("#id_pswd_eBank_tmp").bind('click',{id:"keyeBank"},callsoftKeyBoard).val("");
	//$("#id_safeKeyBoard").val("0");
	$(".safe-keyboard").addClass("safe-keyboard-green");
	
	//def_c col_gray col_gray 
	//将选中的input框底线变黑色
	$('.def_c').focus(function(){
	  $(this).parent().parent().parent().addClass('msg-border');
	});
	$('.def_c').blur(function(){
	  $(this).parent().parent().parent().removeClass('msg-border');
	});
	
	//安全键盘切换
	$(".safe-keyboard1").toggle(
	  function () {
		jQuery("#id_pswd_mall_tmp").remove();
		jQuery("#pa1").prepend("<input type='text' class='txt' name='passwordPre' id='id_pswd_mall_tmp' readonly='readonly' value='' maxLength='30'/>");
		jQuery("#id_pswd_mall_tmp").bind('click',{id:"keyMall"},callsoftKeyBoard).blur();
		jQuery("#id_safeKeyBoard").val("1");
		$("#pa1").css("display","block");
		$("#pa2").css("display","block");
		$(this).html("禁用密码控件");
	  },
	  function () {
		jQuery("#id_pswd_mall_tmp").remove();
		jQuery("#pa1").prepend("<input type='password' class='txt' name='passwordPre' id='id_pswd_mall_tmp' value='' maxLength='30'/>");
		jQuery("#id_pswd_mall_tmp").unbind('click').blur();
		jQuery("#id_safeKeyBoard").val("0");
		$("#pa1").css("display","");
		$("#pa2").css("display","block");
		$(this).html("启用密码控件");
	  }
	);
});
	function changeLayer(flag) {
		if(flag == "0"){
			var jrcss = $('#id_eBank').css("display");
			var rygcss = $('#id_eMall').css("display");
			
			if(jrcss == "block" && rygcss == "none"){
				//alert("id_btn_eBank1");
				$("#id_btn_eBank").click();
			}else {
				//alert("id_btn_mall1");
				$("#id_btn_mall").click();
			}
		}else {
			//alert(90000);
		}
	}
	//根据id获取url参数
		function getParam(param){
			var url = location.href;
			var paraString = url.substring(url.indexOf("?")+1,url.length).split("&");
			var paraObj = {};
			for(i=0;j=paraString[i];i++){
				paraObj[j.substring(0,j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=")+1,j.length);
			}
			var returnVal = paraObj[param.toLowerCase()];
			if(typeof(returnVal) == "undefined"){
				return "";
			}else {
				return returnVal;
			}
			
		}
	
<%-- 		function submitHandler(){
			var pwdInput=document.getElementById("id_pswd_mall_tmp");
			var pwdValue=pwdInput.value;
			var tempChar;
			var encryptStr="";
			var dict = <%=sb.toString()%>
			//alert(pwdValue);
			for(var i=0;i<pwdValue.length;i++){
				tempChar=pwdValue.charAt(i);
				for(var j=0;j<dict.length/2;j++){
					if(tempChar==dict[j*2]){
						encryptStr+=dict[j*2+1];
						break;
					}else if(j==dict.length/2-1){
						alert("非法字符");
						return false;
					}
				}
			}
			//alert("加密后的字符："+encryptStr);
			
			$("#id_pswd_mall_tmp").val(encryptStr);
			//pwdInput.value=encryptStr;
			
			return true;
		}
 --%>		
		
 	function submitHandler(pwdInput){
		//var pwdInput=document.getElementById("id_pswd_mall_tmp_submit");
		var pwdValue=pwdInput.value;
		var tempChar;
		var encryptStr="";
		var dict = <%=sb.toString()%>
		for(var i=0;i<pwdValue.length;i++){
			tempChar=pwdValue.charAt(i);
			for(var j=0;j<dict.length/2;j++){
				if(tempChar==dict[j*2]){
					encryptStr+=dict[j*2+1];
					break;
				}else if(j==dict.length/2-1){
					alert("非法字符");
					return false;
				}
			}
		}
		//$("#id_pswd_mall_tmp_submit").val(encryptStr);
		pwdInput.value=encryptStr;
		return true;
	}
 
//	 	alert(errorCode);
//		2680:用户密码错误
//		3401:用户名不存在
//		96111945:验证码错误或超时
//		7956:错误次数已超过最大次数
//		1:必须修改网银登录密码
//		2:必须修改网银预留信息
		function checkErrCode(code){
			switch(code){
			case "2680":
				$("#id_errInfo_before_jr").html("用户名或登录密码错误");
				break;
			case "3401":
				$("#id_errInfo_before_jr").html("用户名或登录密码错误");
				break;
			case "7956":
				$("#id_errInfo_before_jr").html("错误次数超过3次被冻结，请您次日登录，或到我行网点办理网银登录密码重置业务。");
				break;
			case "5787":
				$("#id_errInfo_before_jr").html("请按照您预先设定的登录方式登录");
				break;
			case "1":
				$("#id_errInfo_before_jr").html("请登录个人网上银行修改网银登录密码");
				break;
			case "2":
				$("#id_errInfo_before_jr").html("请登录个人网上银行修改网银预留信息");
				break;
			case "7845":
				$("#id_errInfo_before_jr").html("非银行户口介质，不能办理此业务");
				break;
			default:
				$("#id_errInfo_before_jr").html("抱歉，登录失败，请重新登录");
			}
		}
	function oneact(){
		$("#id_username").val("");
		$("#id_pswd_mall_tmp").val("");
		 
		$("#id_errInfo_before").html("&nbsp;");
		$("#id_two").removeClass("active");
		$("#id_one").addClass("active");
		
		$("#id_eMall").css("display","none");
		$("#id_eBank").css("display","block");
	}
	function twoact(){
		$("#id_logonCardNum").val("");
		$("#id_pswd_eBank_tmp").val("");
		$("#id_errInfo_before_jr").html("&nbsp;");
		$("#id_one").removeClass("active");
		$("#id_two").addClass("active");
	
		$("#id_eBank").css("display","none");
		$("#id_eMall").css('display','block');
		//$("#id_eMall").find('.tab_con').show();
	}
	
	//回车自动登录
	function enterKeySubmit(event){
		if(event.keyCode == 13){
			var actId = document.activeElement.id;
			if(actId == "id_username"){
				$("#id_username").blur();
			}else if(actId == "id_logonCardNum"){
				$("#id_logonCardNum").blur();
			}else if(actId == "id_pswd_mall_tmp"){
				$("#id_btn_mall").click();
			}
		}
	}
	
	

</script>
</head>
<body class="wrap-body">
<div class="bg-black" style="display:none;"></div>

<div class="wrap re-wrap-box">

	<div class="wrap-re">
	    <div class="re-tit"></div>
		<div styel="width:60%;">
			<!-- 包含头部 -->
			<div id="header" class="content">
				<div class="logo">
					<a  href="<%=mallDefUrl%>"><img src="images/logo.png"  title="融e购首页"/></a>
				</div>
				<h1>&nbsp;&nbsp;|&nbsp;&nbsp;登录</h1>
			</div>
			<div class="line"></div>
			<!-- /包含头部 --> 
			
			<!-- 主体 -->
			<div id="entry_n" class="rel content clm shopuser">
			  <div class="form" id="login_list">
			    <ul class="tab_user_t cfx">    
			      <a href="javascript:;" onclick="oneact();"><li class="one active" id="id_one">&nbsp;<i></i></li></a>
			      <a href="javascript:;" onclick="twoact();"><li class="two" id="id_two">&nbsp;<i></i></li></a>
			    </ul>
		
			    <!--start 网上银行用户登录-->
			    <div class="tab_con" id="id_eBank"  style="display:block;"> 
				    <form id="id_form_eBank" name="logonform" method="post" action="/eBankMobileLogin">
					 	<!-- http://82.200.45.177:9082/servlet/ICBCINBSEstablishSessionServlet -->
					 	<!-- hidden start -->
					 	<input type="hidden" id="id_pswd_eBank" name="logonCardPass" value=""/>
						<input type="hidden" id="id_changerule_eBank" name="changerule" value=""/>
						<input type="hidden" id="id_rule_eBank" name="rule" value=""/>
						<!-- 区分手机还是pad的标识 -->
						<input type="hidden" name="mobileType" value="pad"/>
						<!-- 约定好的pad商城网银登陆必须要传递的值-->
						<input type="hidden" id="id_is" name="isMobileLogin" value="2"/>
						<!-- 要求提供的参数 -->
						<input type="hidden" name="netType" value="111" />
						
						<input type="hidden" id="id_scene_type" name="sceneType" value="<%=sceneType%>">
						<input type="hidden"  id="id_target_service_ebank" name="service" value="<%=casClientUrl%>"/>
						<!-- hidden end -->
						
						<!--金融@家 start-->
					    <div class="login-box ryg box-jinrong">
					    	<div class="errors" id="id_errInfo_before_jr">
					  	  		<form:errors path="*" id="id_msg_eBank" element="span" />
					  	  		<c:if test="${not empty param.error}">
					  	  	 		<span class="errInfo" style="font-weight:normal;font-size:13px;color:red;"></span>
					  	  		</c:if>&nbsp;
							</div>
							<ul class="tab_con_g">
						        <li><label for="textfield">卡（账）号/手机号/别名：</label></li>
						        <li>
						          <input type="text" data-role="none" class="txt" name="logonCardNum" id="id_logonCardNum" value="" maxLength="19" onkeydown="enterKeySubmit(event);"/>
						        </li>
						        <li>
						          <label for="textfield2">登录密码：</label>
						        </li>
						        <li class="">
					       			<input type="text" data-role="none" class="txt" name="pswdeBankTmp" id="id_pswd_eBank_tmp"  readonly="readonly" value="" maxLength="30"/>
						        </li>
						        <li>您使用工行商城，须遵守《<a class="prompt-so" >中国工商银行电子商务平台会员服务协议</a>》</li>
						        <li>
						          <button type="button"  id="id_btn_eBank" class="n_btn" name="login"><span id="id_form_eBank_login" class="ico_lg">登录</span></button>
						        </li>
							</ul>
					    </div>
					</form>
				</div>
				<!--end 网上银行用户登录--> 
				
		      	<!--start 商城用户登录-->
			    <div class="" id="id_eMall" style="display:none;"> 
			    	<form:form method="post" commandName="${commandName}" htmlEscape="true" id="id_form_mall" name="form_mall">
					    <div class="tab_con"  style="display:block;"> 
					      	<!--start 个人登录-->
				      		<div class="errors" id="id_errInfo_before">
					  	  		<form:errors path="*" id="id_msg_mall" element="span" />
					  	  		<c:if test="${not empty param.error}">
					  	  	 		<span class="errInfo" style="font-weight:normal;font-size:13px;color:red;"></span>
					  	  		</c:if>&nbsp;
							</div>
							<ul class="tab_con_g">
								<li><label for="textfield">用户名/手机号：</label></li>
								<li>
								  	<input type="text" data-role="none" class="txt" name="username" id="id_username" value="" maxLength="20" onkeydown="enterKeySubmit(event);"/>
								</li>		     
								<li>
									<a href="javascript:window.location='<%=padRealContextPath%>'+'/pad/getPassword/getMemberPass.jhtml?username='+jQuery('#id_username').val()" target="_parent" class="fr">忘记密码？马上找回</a>
								  	<label for="textfield2">登录密码：</label>
								</li>
								<li class="login_p">
									<div id="pa1" style='display:block' class="txt1 txt_pad1"  >
										<input type="password" class="txt" name="passwordPre" id="id_pswd_mall_tmp" value="" maxLength="30" onkeydown="enterKeySubmit(event);"/>
									</div>
									<span class="txt_box">
										<i class="icon_key"></i>
										<a href="javascript:;" onclick=""><span id="YUse" class="safe-keyboard1"  name="YUse">启用密码安全控件</span></a>
									</span>
								</li>
								<li>您使用工行商城，须遵守《<a class="prompt-so" id="prompt_so_mall">中国工商银行电子商务平台会员服务协议</a>》</li>
								<li>
									<button type="button"  id="id_btn_mall" class="n_btn" name="login" tabindex="6" ><span id="id_form_mall_login" class="ico_lg">登录</span></button>
									<a href="<%=mallRegUtl %>"  target="_parent" class="ml20">免费注册成为工行商城用户</a></li>
								</li>
							</ul>
				      		<!--end 个人登录--> 
				      		<ul>
				             	<input type="hidden" name="lt" value="${loginTicket}" />
						        <input type="hidden" name="execution" value="${flowExecutionKey}" />
						        <input type="hidden" name="_eventId" value="submit" />
						        <input type="hidden"  id="targetService" name="service" value="<%=casClientUrl%>"/>
						        <input type="hidden"  id="isSafe" name="isSafe"  value="1"/>
						        <input type="hidden" name="randomId" value="<%=UID%>"/>
						        <input type="hidden" name="dictKey" id="id_dictKey" value="<%=dictKey %>"/>
						        
						        <input type="hidden" name="loginWay" value="3"/>
						        <!-- 密码键盘相关开始 -->
						        <input type="hidden" id="id_pswd_mall" name="password" value=""/>
						        <input type="hidden" id="id_changerule_mall" name="changerule" value=""/>
						        <input type="hidden" id="id_rule_mall" name="rule" value=""/>
						        <input type="hidden" id="id_safeKeyBoard" name="safeKeyBoard" value="0"/>
						        <!-- 密码键盘相关结束 -->
							</ul>
						</div>
					</form:form>
				</div>
				<!--end 商城用户登录--> 
			  </div>  
		  	  <!-- endid="login_list" -->
		  	  
			</div>
			<!-- end 主体 -->
			
			<div id="divss" hidefocus="true" ></div>
		</div>
		
		<div class="re-tit"></div>
<%-- 		<!-- 商城协议 -->
		<div class="re-right" id="agreementright" > <a href="javascript:;" class="icon-colse" id="agreementblock"></a>
			<div class="re-treaty">
		    	<h2><%=mallAgreementText %></h2>
				<p><%=agreementContent.replaceAll("<a[^>]*>", "") %></p>
				<div class="pad-t10"><a id="icon_agree" class="btn btn-org btn-14 blk btnlr0">已阅读并同意以上协议</a></div>
			</div>
		</div> --%>
		
	</div>
	<!-- end class="wrap-re" -->



</div>
</body>
</html>
<script type="text/javascript" language="javascript">   
	   var callback_id = "";
       function isiPhone(){
    	   //alert("iphoneos:"+navigator.userAgent.toLowerCase().match(/ipad/i));
    	   return (navigator.userAgent.toLowerCase().match(/ipad/i) == "ipad");
       }
    
       function isAndroid(){
           return (navigator.userAgent.toLowerCase().match(/android/i) == "android");
       }
    
       function iOSExcuteNativeMethod(param){
           var iFrame;
           iFrame = document.createElement("iframe");
           iFrame.setAttribute("src", param);
           iFrame.setAttribute("style", "display:none");
           iFrame.setAttribute("height", "0px");
           iFrame.setAttribute("width", "0px");
           iFrame.setAttribute("frameborder", "0");
           document.body.appendChild(iFrame);
           iFrame.parentNode.removeChild(iFrame);
           iFrame = null;
       }
    
       function callsoftKeyBoard(event){
    	   isSysBoard.mallPsd=false;
    	   isSysBoard.ebankPsd=false;
    	   
       	   $("#id_pswd_mall_tmp").val("");
       	   $("#id_pswd_mall").val("");
       	   
       	   $("#id_pswd_eBank_tmp").val("");
       	   $("#id_pswd_eBank").val("");
       	
       	   //alert("id:"+event.data.id);
           if( isiPhone() ){
        	   if("keyMall" == event.data.id){
        		   iOSExcuteNativeMethod("native://showKeyBoardButton;0;0;1");
        		   iOSExcuteNativeMethod("native://callSoftKeyBoard;0;1,1,0");
        	   }else if("keyeBank" == event.data.id){
        		   iOSExcuteNativeMethod("native://showKeyBoardButton;0;0;0");
        		   iOSExcuteNativeMethod("native://callSoftKeyBoard;1;1,1,0");
        	   }
        	   
           }
       }
    
       function enableInput( id ){
           if( id == 0 ){
               document.getElementById("id_pswd_mall").value = "";
               //document.getElementById("id_pswd_mall").disabled = false;
           }else if(id == 1){
        	   document.getElementById("id_pswd_eBank").value = "";
           }
       }
    
       function setText( id, param ){
    	   if( id == 0 ){
              document.getElementById("id_pswd_mall_tmp").value = param;   
    	   }else if(id == 1){
    		   document.getElementById("id_pswd_eBank_tmp").value = param;
    	   }
       }
        
       function submitData(){
           if( isiPhone() ){
        	   callback_id = "0";
			   iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&0");
			   callback_id = "1";
			   iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&1");
           }else if( isAndroid() ){
        	   //如果是商城登录调用的密码键盘
        	   var paramMallOrg = prompt("getEncryptString","0");
        	   var paramMall = (new Function("return" + paramMallOrg))();
        	   var pswdMall = paramMall.loginPasswd;
        	   var changeruleMall = paramMall.changeRule;
        	   var ruleMall = paramMall.rule;
        	   //alert(loginPasswd1);
        	   //alert(changerule1);
        	   //alert(rule1);
        	   $("#id_pswd_mall").val(pswdMall);
               $("#id_changerule_mall").val(changeruleMall);
               $("#id_rule_mall").val(ruleMall);
               
               //如果是网银登录调用的密码键盘
               var parameBankOrg = prompt("getEncryptString","1");
        	   var parameBank = (new Function("return" + parameBankOrg))();
        	   var pswdeBank = parameBank.loginPasswd;
        	   var changeruleeBank = parameBank.changeRule;
        	   var ruleeBank = parameBank.rule;
        	   //alert(loginPasswd1);
        	   //alert(changerule1);
        	   //alert(rule1);
        	   $("#id_pswd_eBank").val(pswdeBank);
               $("#id_changerule_eBank").val(changeruleeBank);
               $("#id_rule_eBank").val(ruleeBank);
           }
       }
    
       function submitCallBack( params ){
    	   var loginPasswd = params.loginPasswd;
           var changerule = params.changeRule;
           var rule = params.rule;
           if(callback_id == "0"){
        	   //alert("mall:"+loginPasswd);
        	   $("#id_pswd_mall").val(loginPasswd);
               $("#id_changerule_mall").val(changerule);
               $("#id_rule_mall").val(rule);
           }else if(callback_id == "1"){
        	   //alert("eBank:"+loginPasswd);
        	   $("#id_pswd_eBank").val(loginPasswd);
               $("#id_changerule_eBank").val(changerule);
               $("#id_rule_eBank").val(rule);
           }
       }
       
       function changeToSystemKeyBoard(id){
      	 if(id=="0"){  //商城用户   	
      		 isSysBoard.mallPsd=true;
      		 document.getElementById("id_pswd_mall_tmp").type='password';
      		 $("#id_pswd_mall_tmp").removeAttr('readonly').focus();
      	 }else if(id=="1"){ //网银用户
      		 isSysBoard.ebankPsd=true;
      		 document.getElementById("id_pswd_eBank_tmp").type='password';
         		 $("#id_pswd_eBank_tmp").removeAttr('readonly').focus();
      	 }
       }
</script>