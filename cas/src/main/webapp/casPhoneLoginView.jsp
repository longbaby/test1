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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
//	String mallFindPasUrl =instance.getsmsProperty("cas.mallFindPasUrlfn");
//	String mallDefUrl =instance.getsmsProperty("cas.mallDefUrlfn");
//	String pre=instance.getsmsProperty("mall.product.preview.urlfn");
//	String eBankLogin=instance.getsmsProperty("cas.mobile.eBankLogin");

	String sceneType = request.getParameter("sceneType");
	if(sceneType==null || sceneType==""){
		sceneType="5";
	}
	String mobileRealContextPath = ApplicationConfigUtils.getInstance().getPropertiesValue("realContextPathMobile");
	String staticResourceVersion = ApplicationConfigUtils.getInstance().getPropertiesValue(Constants.STATIC_RESOURCE_VERSION);
%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<script type="text/javascript">var ctx = '${ctx}';</script>

<!-- jquerymobile结构样式 -->
<!-- 元素基础样式 -->
<link href="${ctx}/styles/mobile/reset_app.css?version=<%=staticResourceVersion%>" rel="stylesheet" type="text/css" />
<link href="${ctx}/styles/mobile/login.css?version=<%=staticResourceVersion%>" rel="stylesheet" type="text/css" />


<script src="${ctx}/scripts/mobile/jquery-1.8.3.min.js?version=<%=staticResourceVersion%>"></script>
<script src="${ctx}/scripts/mobile/common.js?version=<%=staticResourceVersion%>"></script>
<script src="${ctx}/scripts/mobile/tab.js?version=<%=staticResourceVersion%>"></script>
<script src="${ctx}/scripts/mobile/login.js?version=<%=staticResourceVersion%>"></script>
<script src="${ctx}/scripts/mobile/checkacct.js?version=<%=staticResourceVersion%>"></script>
<script src="${ctx}/scripts/mobile/bootstrap-modal.js?version=<%=staticResourceVersion%>"></script>
<script type="text/javascript">
var keyboardHeight;//安全键盘高度
var winHeight;//屏幕高度
var docHeight;//页面高度
var eleDocTop;//密码框底部距离页面顶端的高度
var eleWinTop;//密码框底部距离屏幕顶端的高度
var moveHeight=0;//页面需要上移的高度
winHeight = $(window).height();//首先获得屏幕高度
var isSysBoard = {
		"ebankPsd" : false,
		"mallPsd" : false
};
$(function(){
	// 页面高度
	docHeight = $(document).height();
	// 密码框底部距离页面顶端的高度
	eleDocTop = $("#id_ebank_box").offset().top;
	// 密码框底部距离屏幕顶端的高度
	eleWinTop = eleDocTop - document.body.scrollTop;
	
	var formType = getParam("fromType");
	var errorCode = getParam("errorCode");
	
	if(null != errorCode && "" != errorCode){
		checkErrCode(errorCode);
	}
	if("register" == formType){
		$('.box-jinrong').hide();
	  	$('.box-rongyigou').show();
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
   			$("#id_username_tip").find("span").html("请输入用户名！");
   			$("#id_username_tip").css("display","block");
   			return;
   		}else{
	   		if(usernameLen < 4 || usernameLen > 20){
	   			$("#id_username_tip").find("span").html("用户名错误，请重新输入！");
	   			$("#id_username_tip").css("display","block");
	   			return ;
	   		}else{
	   			var myRegName = /^[A-Za-z0-9_]{4,20}$/;
				if(!myRegName.test(username)){
					$("#id_username_tip").find("span").html("用户名错误，请重新输入！");
		   			$("#id_username_tip").css("display","block");
					return;	
				}
	   		}
	   		$("#id_username_tip").find("span").html("");
   			$("#id_username_tip").css("display","none");
   		}
   		
		if(password == null || password == ""){
			$("#id_pswd_mall_tmp_tip").find("span").html("密码不能为空！");
			$("#id_pswd_mall_tmp_tip").css("display","block");
			return;
		}else{
	   		if(!keyBoardFlag){
	   			var myReg = /^[A-Za-z0-9~!@#$%^&*()_\-+={}\[\]\\|:;"'<>,.?\/]{8,30}$/;
	   			var diReg = /^[0-9]*$/;
	   			var chReg = /^[A-Za-z]*$/;
	   			if(passwordLen <8 || passwordLen > 30 ){
	   				$("#id_pswd_mall_tmp_tip").find("span").html("密码长度为8到30个字符！");
	   				$("#id_pswd_mall_tmp_tip").css("display","block");
	   				return;	
	   			}
	   			if(diReg.test(password) && passwordLen <10){
	   				$("#id_pswd_mall_tmp_tip").find("span").html("密码不可全为数字！");
	   				$("#id_pswd_mall_tmp_tip").css("display","block");
	   				return;	
	   			}
	   			if(chReg.test(password)  && passwordLen <10){
	   				$("#id_pswd_mall_tmp_tip").find("span").html("密码不可全为字母！");
	   				$("#id_pswd_mall_tmp_tip").css("display","block");
	   				return;	
	   			}
	   			if(!myReg.test(password)){
	   				$("#id_pswd_mall_tmp_tip").find("span").html("密码不可包含非法字符！");
	   				$("#id_pswd_mall_tmp_tip").css("display","block");
	   				return;	
	   			}
	   		}else{
	   			if(passwordLen < 8){
		   	   		$("#id_pswd_mall_tmp_tip").find("span").html("密码长度至少应该为八位！");
					$("#id_pswd_mall_tmp_tip").css("display","block");
	   	   			return ;
	   	   		}
	   		}
	   		$("#id_pswd_mall_tmp_tip").find("span").html("");
			$("#id_pswd_mall_tmp_tip").css("display","none");
		}
		
		
		if(keyBoardFlag){
			if(isSysBoard.mallPsd==false){//安全键盘
				//把密码键盘产生的密文填写到页面的隐藏域
				submitData();	
			}else{//安全键盘切换为系统键盘
				$("#id_changerule_mall").val("");
                $("#id_rule_mall").val("");
				var psdObj = document.getElementById("id_pswd_mall_tmp");
				submitHandler(psdObj);
			}
		}else{//系统键盘
			$("#id_changerule_mall").val("");
            $("#id_rule_mall").val("");
 			$("#id_pswd_mall_tmp_submit").val(password);
			var psdObj = document.getElementById("id_pswd_mall_tmp_submit");
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
		if(logonCardNumLen < 1){
			$("#id_logonCardNum_tip_txt").html("卡(账)号或登录ID不能为空");
			$("#id_logonCardNum_tip").css("display","block");
			return ;
		}else if(logonCardNumLen > 17 && logonCardNumLen != 19){
			$("#id_logonCardNum_tip_txt").html("请输入正确的卡(账)号或登录ID");
			$("#id_logonCardNum_tip").css("display","block");
			return;
		}
		
		//不判断17位年金账号,允许输入14位数字，后台判断是卡号还是别名
		if(logonCardNumLen > 15 && !isValidCardAndAcctPublic(logonCardNum) && logonCardNumLen != 17){
			$("#id_logonCardNum_tip_txt").html("请输入正确的卡(账)号");
			$("#id_logonCardNum_tip").css("display","block");
			return;
		}else{
			//非法别名
			var re =/\W/;
			if (re.test(logonCardNum)){
				$("#id_logonCardNum_tip_txt").html("请输入正确的登录ID");
				$("#id_logonCardNum_tip").css("display","block");
				return;
			}
		}
		
		
		if(pswdeBankTmpLen < 4){
			$("#id_logonCardNum_tip").css("display","none");
			$("#id_pswd_eBank_tmp_tip").css("display","block");
			return ;
		}else{
			$("#id_logonCardNum_tip").css("display","none");
			$("#id_pswd_eBank_tmp_tip").css("display","none");
		}
		
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
	
	//用户名或密码错误弹框提示
	if ($(".errors span").length < 1) {
		//$(".errors span:eq(1)").hide();
	}else{
		 $('.box-jinrong').hide();
	  	 $('.box-rongyigou').show();
	  	 
		$(".errors span").hide();
		$(".errors span").css({
			'height':'0px',
			'width':'0px',
			'color':'white',
			'overflow':'hidden'
		});
		alertJqmInfo($(".errors span").text());
	}
	// 启用安全键盘
	//$("#id_pswd_mall_tmp").attr('readonly','readonly').bind('click',{id:"keyMall"},callsoftKeyBoard).val("");
	$("#id_pswd_eBank_tmp").attr('readonly','readonly').bind('click',{id:"keyeBank"},callsoftKeyBoard).val("");
	$("#id_safeKeyBoard").val("0");
	//$(".safe-keyboard").addClass("safe-keyboard-green");
	
	//def_c col_gray col_gray 
	//将选中的input框底线变黑色
	$('.def_c').focus(function(){
	  $(this).parent().parent().parent().addClass('msg-border');
	});
	$('.def_c').blur(function(){
	  $(this).parent().parent().parent().removeClass('msg-border');
	});
	
	//安全键盘切换
	$(".safe-keyboard").toggle(
	  function () {//安全键盘
		jQuery("#id_pswd_mall_tmp").remove();
		jQuery("#id_pswd_mall_tmp_submit").remove();
		jQuery("#id_pswd_mall_div").prepend("<input type='text' data-role='none' class='def_c' name='passwordPre' id='id_pswd_mall_tmp' value='' maxLength='30' placeholder='登录密码' />");
		jQuery("#id_pswd_mall_tmp").blur();
		jQuery("#id_pswd_mall_tmp").attr('readonly','readonly').bind('click',{id:"keyMall"},callsoftKeyBoard).val("");
		jQuery("#id_safeKeyBoard").val("1");
		$(this).addClass("safe-keyboard-green");
		 
	  },
	  function () {//系统键盘
		jQuery("#id_pswd_mall_tmp").remove();
		jQuery("#id_pswd_mall_div").prepend("<input type='password' data-role='none' class='def_c' id='id_pswd_mall_tmp' value='' maxLength='30' placeholder='登录密码'  onkeydown='enterKeySubmit(event);'/>");
		jQuery("#id_pswd_mall_div").prepend("<input type='hidden' name='passwordPre' id='id_pswd_mall_tmp_submit' value='' maxLength='30'/>");
		jQuery("#id_pswd_mall_tmp").blur();
		jQuery("#id_pswd_mall_tmp").removeAttr('readonly').unbind('click').val("");
		jQuery("#id_safeKeyBoard").val("0");
		$(this).removeClass("safe-keyboard-green");
	  }
	);
	//金融@家与融易购切换
   	$('.login-rongyigou').click(function(){
   		$("#id_username").val("");
		$("#id_pswd_mall_tmp").val("");
		$("#id_username_tip").find("span").html("");
		$("#id_username_tip").css("display","none");
		$("#id_pswd_mall_tmp_tip").find("span").html("");
		$("#id_pswd_mall_tmp_tip").css("display","none");
/* 		$("#id_pswd_mall_tmp_submit").val("");
		$("#id_pswd_mall").val("");
		$("#id_changerule_mall").val("");
		$("#id_rule_mall").val(""); */
   		$('.box-jinrong').hide();
   		$('.box-rongyigou').show();
   		});
   	$('.login-jinrong').click(function(){
   		$("#id_logonCardNum").val("");
		$("#id_pswd_eBank_tmp").val("");
		
/* 		$("#id_pswd_eBank").val("");
		$("#id_changerule_eBank").val("");
		$("#id_rule_eBank").val(""); */
   		$('.box-jinrong').show();
   		$('.box-rongyigou').hide();
   	});
});

function changeLayer(flag) {
	if(flag == "0"){
		$('.blk-js').css({
			height: function() {
				return 0;
			}
		});
		
		var jrcss = $('.box-jinrong').css("display");
		var rygcss = $('.box-rongyigou').css("display");
	
		if(jrcss == "block" && rygcss == "none"){
			$("#id_btn_eBank").click();
		}else {
			$("#id_btn_mall").click();
		}
	}else if(flag == "1") {
		 //呼出安全键盘
		if( (winHeight - eleWinTop)< keyboardHeight ){ //密码框被遮挡
			//计算需要上移的高度
			moveHeight = keyboardHeight - (winHeight - eleWinTop) + 10;
			//判断页面长度是否足够上移
			var docBottom = docHeight - eleDocTop;	//密码框底部距离页面底部的高度
			var winBottom = winHeight - eleWinTop;	//密码框底部距离屏幕底部的高度
			if( (docBottom - winBottom) < moveHeight){
				//不够上移高度，需要增加页面长度
				var blkHeight = moveHeight - (docBottom - winBottom);//需要补充的空白高度
				$('.blk-js').css({
					height: function() {
						return blkHeight;
					}
				});
				
			}
			//上移页面
//			document.body.scrollTop = $(window).scrollTop() + moveHeight;
			$('body,html').animate({scrollTop:($(window).scrollTop() + moveHeight)},150);
		}
	}
}

	//弹窗提示
	 function alertJqmInfo(msg,func,params){
		$("#modal_show_tip").text(msg);
		$('#modalShowTip').on('hide',function(){
			if(func){
				if(params){
					func(params);
					}else{func();} 
				}		
			});
		$('#modalShowTip').modal();	
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
			var pwdInput=document.getElementById("id_pswd_mall_tmp_submit");
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
			
			$("#id_pswd_mall_tmp_submit").val(encryptStr);
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
 

//		2680:用户密码错误
//		3401:用户名不存在
//		96111945:验证码错误或超时
//		7956:错误次数已超过最大次数
//		1:必须修改网银登录密码
//		2:必须修改网银预留信息
		function checkErrCode(code){
			switch(code){
			case "2680":
				alertJqmInfo("用户名或登录密码错误");
				break;
			case "3401":
				alertJqmInfo("用户名或登录密码错误");
				break;
			case "7956":
				alertJqmInfo("错误次数超过3次被冻结，请您次日登录，或到我行网点办理网银登录密码重置业务。");
				break;
			case "5787":
				alertJqmInfo("请按照您预先设定的登录方式登录");
				break;
			case "1":
				alertJqmInfo("请登录个人网上银行修改网银登录密码");
				break;
			case "2":
				alertJqmInfo("请登录个人网上银行修改网银预留信息");
				break;
			case "7845":
				alertJqmInfo("非银行户口介质，不能办理此业务");
				break;
			default:
				alertJqmInfo("抱歉，登录失败，请重新登录");
				break;
			}
		}		
		
		//回车自动登录
		function enterKeySubmit(event){
			if(event.keyCode == 13){
				var actId = document.activeElement.id;
				if(actId == "id_pswd_mall_tmp"){
					$("#id_btn_mall").click();
				}
			}
		}
</script>
</head>
<body class="moblie-wrap-bg">
<div class="login-wrap"> 
  <!--start header-->
  <header class="header-p header-pr">
    <div class="s-header">
      <div class="home-back"></div>
      <h1 class="title">登录</h1>
      <div class="g-nav"><a class="top-refresh-squ-bl top-w" style="display:none;"></a></div> <%-- 修改手机银行登录href --%>
    </div>
  </header>
  <!--end header-->
  
  <div class="s-bd-wrap s-bd-wrap-header">
  <form id="id_form_eBank" name="logonform" method="post" action="/eBankMobileLogin">
 	<!-- http://82.200.45.177:9082/servlet/ICBCINBSEstablishSessionServlet -->
 	<!-- hidden start -->
 	<input type="hidden" id="id_pswd_eBank" name="logonCardPass" value="">
	<input type="hidden" id="id_changerule_eBank" name="changerule" value="">
	<input type="hidden" id="id_rule_eBank" name="rule" value="">
	<input type="hidden" id="id_scene_type" name="sceneType" value="<%=sceneType%>">
	<input type="hidden"  id="id_target_service_ebank" name="service" value="<%=casClientUrl%>"/>
	
	<!-- 约定好的手机商城网银登陆必须要传递的值-->
	<input type="hidden" id="id_is" name="isMobileLogin" value="1">
	
	<!-- 要求提供的参数 -->
	<input type="hidden" name="netType" value="111" />
	
	<!-- hidden end -->
	
   <!--金融@家 start-->
    <div class="login-box ryg box-jinrong">
      <div class="logo"><img src="${ctx}/images/mobile/drawable-mdpi/logo_m.png" /></div>
      <div class="myIcbc-i-name">
        <div class="myIcbc-i-box">
          <div class="tit "><i class="reg-icon reg-i-people"></i></div>
          <div class="msg mag-r10">
            <div class="msg-info">
              <input type="text" data-role="none" class="def_c" name="logonCardNum" id="id_logonCardNum" value="" maxLength="19" placeholder="卡（账）号/手机号/别名" />
            </div>
          </div>
        </div>
        <!-- cardNum check start -->
        <div id="id_logonCardNum_tip" style="display:none;">
        <div class="myIcbc-i-box prompt-box" >
			<div class="tit"></div>
			<div class="msg mag-r10">
				<div class="msg-info">
					<div class="myIcbc-i-tip bg-org" style="display:block;"><span id="id_logonCardNum_tip_txt" class="pad-l10">卡(账)号或登录ID不能为空</span></div>
				</div>
			</div>
		</div>
		</div>
        <!-- cardNum check end -->
        <div class="myIcbc-i-box">
          <div class="tit "><i class="reg-icon reg-i-lock"></i></div>
          <div class="msg mag-r10">
            <div class="msg-info group-txt">
              <input type="text" data-role="none" class="def_c" name="pswdeBankTmp" id="id_pswd_eBank_tmp" value="" maxLength="30" placeholder="登录密码" />
<!--               <div class="safe-keyboard"><i class=" icon-safe-keyboard"></i><i class=" icon-safe-key"></i></div> -->
            </div>
          </div>
        </div>
        <!-- password check start -->
        <div id="id_pswd_eBank_tmp_tip" style="display:none;">
        <div class="myIcbc-i-box prompt-box" >
			<div class="tit"></div>
			<div class="msg mag-r10">
				<div class="msg-info">
					<div class="myIcbc-i-tip bg-org" style="display:block;"><span id="id_pswd_eBank_tmp_tip_txt" class="pad-l10">密码长度至少应该为四位</span></div>
				</div>
			</div>
		</div>
		</div>
        <!-- password check end -->
      </div>
		<div class="blk-f125"></div>
      <!--● btn-s-box end -->
      
      <div  class="btn-s-box" id="id_ebank_box"><a id="id_btn_eBank" ontouchstart="this.className='btn btn-org-active'" ontouchend="this.className='btn btn-org'" class="btn btn-org"><span class="btn-f"><i class="icon-login"></i><span id="id_form_eBank_login">登录</span></span></a> </div>
      <div id="id_eBank_red_login" style="height:8px;"></div>
      <!--● btn-s-box start -->
      
      <div class="logo-bottom login-rongyigou"><img src="${ctx}/images/mobile/drawable-mdpi/top_refresh_square_bl.png" class="logo-refresh" /><img src="${ctx}/images/mobile/drawable-mdpi/logo_ryg.png" class="logo-right" /></div>
    </div>
    </form>
    <!--金融@家 end--> 
    <form:form method="post" commandName="${commandName}" htmlEscape="true" id="id_form_mall" name="form_mall">
    <!--融已购 start-->
    <div class="login-box ryg  box-rongyigou"  style="display:none;">
      <div class="logo"><img src="${ctx}/images/mobile/drawable-mdpi/logo_ryg.png"></div>
      <div class="myIcbc-i-name">
        <div class="myIcbc-i-box">
          <div class="tit "><i class="reg-icon reg-i-people"></i></div>
          <div class="msg mag-r10">
            <div class="msg-info">
              <input type="text" data-role="none" class="def_c" name="username" id="id_username" value="" maxLength="20" placeholder="用户名/手机号" />
            </div>
          </div>
        </div>
        <div id="id_username_tip" style="display:none;">
        <div class="myIcbc-i-box prompt-box" >
			<div class="tit"></div>
			<div class="msg mag-r10">
				<div class="msg-info">
					<div class="myIcbc-i-tip bg-org" style="display:block;"><span class="pad-l10">请输入用户名</span></div>
				</div>
			</div>
		</div>
		</div>
		
        <div class="myIcbc-i-box">
          <div class="tit "><i class="reg-icon reg-i-lock"></i></div>
          <div class="msg mag-r10">
            <div class="msg-info group-txt" id="id_pswd_mall_div">
              <input type="password" data-role="none" class="def_c" id="id_pswd_mall_tmp" value="" maxLength="30" placeholder="登录密码" onkeydown="enterKeySubmit(event);"/>
              <input type="hidden" name="passwordPre" id="id_pswd_mall_tmp_submit" value="" maxLength="30"/>
              <div class="safe-keyboard"><i class="icon-safe-keyboard"></i><i class=" icon-safe-key"></i></div>
            </div>
          </div>
          <div class="errors" id="errors" style="display:none;">
			<form:errors path="*" id="msg" element="span" />
			<c:if test="${not empty param.error}">
				<span style="color:white; height:0px; width:0px; overflow:hidden;"></span>
			</c:if>&nbsp;
	  </div>
     </div>
     	<div id="id_pswd_mall_tmp_tip" style="display:none;">
       	<div class="myIcbc-i-box prompt-box">
			<div class="tit"></div>
			<div class="msg mag-r10">
				<div class="msg-info">
					<div class="myIcbc-i-tip bg-org" style="display:block;"><span class="pad-l10">请输入登录密码</span></div>
				</div>
			</div>
		</div>
		</div>
      </div>
      
      <div class="login-select" id="id_get_pass">
       	 <a href="javascript:window.location='<%=mobileRealContextPath%>'+'/mobile/getPassword/getMemberPass.jhtml?username='+jQuery('#id_username').val()" class="col-blue">忘记密码？马上找回</a>   
      </div>
      
      
		<div class="blk-f125"></div>
      <div class="btn-s-box btn-s-box-no">
	      <a href="<%=mallRegUtl%>" ontouchstart="this.className='btn btn-org-active'" ontouchend="this.className='btn btn-org'" class="btn btn-org"><span class="btn-f"><i class="icon-register"></i>注 册</span></a>
	      <a ontouchstart="this.className='btn btn-org-active'" ontouchend="this.className='btn btn-org'" id="id_btn_mall" class="btn btn-org"><span class="btn-f"><i class="icon-login"></i><span id="id_form_mall_login">登 录</span></span></a> 
      </div>
      <div id="id_mall_red_login" style="height:8px;"></div>
      <div class="logo-bottom login-jinrong"><img src="${ctx}/images/mobile/drawable-mdpi/top_refresh_square_bl.png" class="logo-refresh" /><img src="${ctx}/images/mobile/drawable-mdpi/logo_m.png" class="logo-right" /></div>
    </div>
    <!-- hidden -->
    <div>
        <input type="hidden" name="lt" value="${loginTicket}" />
        <input type="hidden" name="execution" value="${flowExecutionKey}" />
        <input type="hidden" name="_eventId" value="submit" />
        <input type="hidden"  id="targetService" name="service" value="<%=casClientUrl%>"/>
        <input type="hidden"  id="isSafe" name="isSafe"  value="1"/>
        <input type="hidden" name="randomId" value="<%=UID%>"/>
        <input type="hidden" name="dictKey" id="id_dictKey" value="<%=dictKey%>"/>
        
        <input type="hidden" name="loginWay" value="1"/>
        <!-- 密码键盘相关开始 -->
        <input type="hidden" id="id_pswd_mall" name="password" value=""/>
        <input type="hidden" id="id_changerule_mall" name="changerule" value=""/>
        <input type="hidden" id="id_rule_mall" name="rule" value=""/>
        <input type="hidden" id="id_safeKeyBoard" name="safeKeyBoard" value=""/>
        <!-- 密码键盘相关结束 -->
	</div>
    <!--融已购 end--> 
</form:form>
  </div>
 </div>
<div class="blk-js"></div>
<div class="blk-f125">&nbsp;</div>
<!--● btn-s-box end -->
<!---------------弹框提示 --------------------->
<div id="modalShowTip" class="modal hide fade">
	<div class="modal-header"><a class="close" data-dismiss="modal" ></a>
		<h3>提示信息</h3>
	</div>
	<div class="modal-body">
		<p id="modal_show_tip"></p>
	</div>
	<div class="modal-footer btn-s-box"><a class="btn btn-white"  data-dismiss="modal">我知道了</a></div>
</div>

</body>
</html>
<script type="text/javascript" language="javascript">   
	   var callback_id = "";
       function isiPhone(){
    	   return (navigator.userAgent.toLowerCase().match(/iphone os/i) == "iphone os");
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
    	   if("keyMall" == event.data.id){
//    		   docHeight = $(document).height();
    			// 密码框底部距离页面顶端的高度
    			//eleDocTop = $("#id_mall_red_login").offset().top + $("#id_mall_red_login").outerHeight();
    			eleDocTop = $("#id_get_pass").offset().top;
    			// 密码框底部距离屏幕顶端的高度
    			eleWinTop = eleDocTop - document.body.scrollTop;
    	   }
/*     	   else if("keyeBank" == event.data.id){
//	    		docHeight = $(document).height();
	   			// 密码框底部距离页面顶端的高度
	   			//eleDocTop = $("#id_eBank_red_login").offset().top + $("#id_eBank_red_login").outerHeight();
	   			eleDocTop = $("#id_ebank_box").offset().top;
	   			// 密码框底部距离屏幕顶端的高度
	   			eleWinTop = eleDocTop - document.body.scrollTop;
    	   }
*/    	   
    	   changeLayer(1);
    	   isSysBoard.mallPsd=false;
    	   isSysBoard.ebankPsd=false;
    	   
       	   $("#id_pswd_mall_tmp").val("");
       	   $("#id_pswd_mall").val("");
       	   
       	   $("#id_pswd_eBank_tmp").val("");
       	   $("#id_pswd_eBank").val("");
       	
           if( isiPhone() ){
        	   if("keyMall" == event.data.id){
        		   iOSExcuteNativeMethod("native://showKeyBoardButton;0;0;1");
        		   iOSExcuteNativeMethod("native://callSoftKeyBoard;0;1,1,0");
        	   }else if("keyeBank" == event.data.id){
        		   iOSExcuteNativeMethod("native://showKeyBoardButton;0;0;0");
        		   iOSExcuteNativeMethod("native://callSoftKeyBoard;1;1,1,0");
        	   }
        	   
           }else if( isAndroid() ){
        	   if("keyMall" == event.data.id){
        		   prompt("showKeyBoardButton","登录;关闭;1");
        		   prompt("callsoftKeyBoard","0;1,1,1");
        	   }else if("keyeBank" == event.data.id){
        		   prompt("showKeyBoardButton","登录;关闭;0");
        		   prompt("callsoftKeyBoard","1;1,1,1");
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
        	   //商城
        	   callback_id = "0";
			   iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&0");
         	   //网银
			   callback_id = "1";
			   iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&1");

           }else if( isAndroid() ){
        	   //如果是商城登录调用的密码键盘
        	   var paramMallOrg = prompt("getEncryptString","0");
        	   var paramMall = (new Function("return" + paramMallOrg))();
        	   var pswdMall = paramMall.loginPasswd;
        	   var changeruleMall = paramMall.changeRule;
        	   var ruleMall = paramMall.rule;
        	   $("#id_pswd_mall").val(pswdMall);
               $("#id_changerule_mall").val(changeruleMall);
               $("#id_rule_mall").val(ruleMall);

               //如果是网银登录调用的密码键盘
               var parameBankOrg = prompt("getEncryptString","1");
        	   var parameBank = (new Function("return" + parameBankOrg))();
        	   var pswdeBank = parameBank.loginPasswd;
        	   var changeruleeBank = parameBank.changeRule;
        	   var ruleeBank = parameBank.rule;
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
        	   $("#id_pswd_mall").val(loginPasswd);
               $("#id_changerule_mall").val(changerule);
               $("#id_rule_mall").val(rule);
           }else if(callback_id == "1"){ 
        	   $("#id_pswd_eBank").val(loginPasswd);
               $("#id_changerule_eBank").val(changerule);
               $("#id_rule_eBank").val(rule);
           }
       }
     //接收高度    kbHeight：键盘高度，scrHeight：屏幕高度
       function sendSoftKeyBoardHeight(kbHeight,scrHeight){
       	winHeight = $(window).height();
       	var ra = (scrHeight/winHeight);//比例
       	keyboardHeight = kbHeight/ra;//实际用于计算的键盘高度
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