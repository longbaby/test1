var isEntered=false;
//登陆提交
var formSubmit = false; // 表单是否提交
function XReturnDown(item){
	 if(item == "login"&&!isEntered){
  		try{
  			document.all.KeyPart.removeFocus();
  			enterLogin();
  		}catch(e){}
  }else if(item == "verify"){
  	document.all.KeyPart.focus();
  }
}
function getfocus1(obj,name, event){
	if(pebankBrowserCompatible.getKeycode(event)==13){
		obj.blur();		
		var flag = document.getElementById("isSafe").value;
		if(flag==1){
			document.getElementById("divss").focus();
			if(''==name){
				eval("document.all.safeEdit1").focus();
			}else if('login'==name){
				enterLogin();
			}else{
				eval("document.all."+name).focus();				
			}
		}else{
			if('login' ==name){
				eval("document.all.password").focus();
				enterLogin();
			}else if(''==name){
				eval("document.all.password").focus();	
			}else{
				eval("document.all."+name).focus();
			}
		}
	}
}

/*function xy(){
	var iWidth=225;
	var iHeight=235;
	var iTop =(window.screen.availHeight-30-iHeight)/2;
	var iLeft=(window.screen.availWidth-10-iWidth)/2;;
	//window.open('compact.jsp','', 'z-lick=yes,toolbar=no, menubar=no,location=no,statys=no,height=225,width=235,location=no,alwaysRaised=yes,resizable=no,top='+iTop+',left='+iLeft+'');
	var feature="dialogWidth:450px,dialogHeight:300px,status:no,help:no,resizable:no,center:yes,localtion:no,scrollbars:yes";
	window.showModalDialog('compact.jsp','', feature);
}*/

$(function() {
	if ($(".errors span").length > 1) {
		$(".errors span:eq(1)").hide();
	}else{
		$(".errors span").css({
			'font-weight':'normal',
			'font-size': '13px',
			'color':'red'
			//'background-color':'green'
			//'gackground':'url(../images/)'
		});
	}
	$("#validateCode").click(function() {
		var timestamp = (new Date()).getTime();
		$("#validateCode").attr("src", ctx + "/captcha.jpg?timestamp=" + timestamp);
	});
	
	$("#imgAsynchHttpReqArea").click(function() {
		var timestamp = (new Date()).getTime();
		$("#validateCode").attr("src", ctx + "/captcha.jpg?timestamp=" + timestamp);
	});
	
	$("#username").focus(function() {
		if ($(this).val() == $(this).attr("default")) {
			$(this).val("");
		}
	}).blur(function() {
		if (jQuery.trim($(this).val()) == "") {
			$(this).val($(this).attr("default"));
		}
	});
	


	$("#button").click(function() {
		$("#errors").html="";
		if (formSubmit) {
			return;
		}
		formSubmit = true;
	
		// 验证用户名格式
		var username = $("#username").val();
		if (jQuery.trim(username) == ""
				|| username == $("#username").attr("default")) {
			$("#errors").html("请输入用户名！");
			formSubmit = false;
			return;
		}else{
			if(username.length <4 ||username.length >20){
					$("#errors").html("用户名错误，请重新输入！");
					formSubmit = false;
					return;	
			}else{
					var myRegName = /^[A-Za-z0-9_]{4,20}$/;
					if(!myRegName.test(username)){
						$("#errors").html("用户名错误，请重新输入！");
						formSubmit = false;
						return;	
					}
			}
		}
		var flag = document.getElementById("isSafe").value;
		if(flag==1){
			if (document.all.safeEdit1.getLength()<8){
				//alert("密码长度至少应该为八位!");
				$("#errors").html("密码长度至少应该为八位!");
				document.all.safeEdit1.focus();
				formSubmit = false;
				return;
			}
			if (!document.all.safeEdit1.isValid()){
		   		//alert("输入的密码不合法，请重新输入！");
				$("#errors").html("输入的密码不合法，请重新输入！");
		   		document.all.safeEdit1.focus();
		   		formSubmit = false;
				return ;
				}
			if (!document.all.KeyPart.isValid()){
				//alert("输入的验证码不合法，请重新输入！");
				$("#errors").html("输入的验证码不合法，请重新输入！");
			  	document.all.KeyPart.focus();
			  	formSubmit = false;
				return ;
			}
			  // 判断输入控件中内容的有效性
			  if (!document.all.safeEdit1.isValid()) {
			    //alert("safeEdit1 has invalid content!");
				  $("#errors").html("safeEdit1 has invalid content!");
				  formSubmit = false;
			    return false;
			  }
			  
			//b2b记住用户名
			var isb2bFlag = jQuery("#isb2b");
			if(isb2bFlag.val() == "1"){  //b2b方式登录
				var isRememberUserName = jQuery("input[name='rememberPass']");
				  if("checked" == isRememberUserName.attr("checked")){
					  //alert(1);
					  setCookies("EUserName",jQuery("input[name='username']").val(),48, "/");
				  }else{
					  deleteCookie("EUserName");
				  }

			}
				
			  document.getElementById("password").value='jardejarde';
			 document.getElementById("j_captcha").value='jarde';
			  // 提交控件清空
			  document.all.safeSubmit1.reset();
			  // 提交验证码向safeEdit1
			  document.all.KeyPart.commitKeyPart(document.all.safeEdit1);
			  // 将safeEdit1的名字/值对提交至safeSubmit1
			  document.all.safeEdit1.commit(document.all.safeSubmit1);

			  // 提交验证码向KeyPart
			  document.all.KeyPart.commitKeyPart(document.all.KeyPart);
			  // 将KeyPart的名字/值对提交至safeSubmit1
			  document.all.KeyPart.commit(document.all.safeSubmit1);

			  //提交表单
			  document.all.safeSubmit1.submit(document.all.form1);
			  formSubmit = false;
			  return false;
		}

		//var password = getIBSInput("textfield2", ts, "", "");
		var password = $("#password").val();
		if (password == null) {
			$("#errors").html("密码不能为空！");
			formSubmit = false;
			return;
		} else {
			var myReg = /^[A-Za-z0-9~!@#$%^&*()_\-+={}\[\]\\|:;"'<>,.?\/]{8,30}$/;
			var diReg = /^[0-9]*$/;
			var chReg = /^[A-Za-z]*$/;
			if(password.length <8 ||password.length >30 ){
				$("#errors").html("密码长度为8到30个字符！");
				formSubmit = false;
				return;	
			}
			if(diReg.test(password) && password.length <10){
				$("#errors").html("密码不可全为数字！");
				formSubmit = false;
				return;	
			}else if(password.length >=10){
				//alert("为了您的密码安全，建议您密码使用字母加数字组合");
				//$("#errors").html("为了您的密码安全，建议您密码使用字母加数字组合！");
			}
			if(chReg.test(password)  && password.length <10){
				$("#errors").html("密码不可全为字母！");
				formSubmit = false;
				return;	
			}else if(password.length >=10){
				//alert("为了您的密码安全，建议您密码使用字母加数字组合");
				//$("#errors").html("为了您的密码安全，建议您密码使用字母加数字组合！");
			}
			if(!myReg.test(password)){
				$("#errors").html("密码不可包含非法字符！");
				formSubmit = false;
				return;	
			}
			$("[name=password]").val(password);
		}
		var j_captcha = $("#j_captcha").val();
		if (jQuery.trim(j_captcha) == "" || j_captcha.length != 4) {
			$("#errors").html("请输入四位验证码！");
			formSubmit = false;
			return;
		}
/*		if(document.all.rmbUserName.checked){
			setCookies("username",username,24,'/');
		}*/
		if(submitHandler()){
			$("form").trigger("submit");
		}else{
			$("#errors").html("密码不可包含非法字符！");
			formSubmit = false;
			return;
		}
			
	});
	
	/*
	$(document).keydown(function(e) {
		if (e.keyCode == 13 && $("#textfield3").val() !="") {
			$("[name=login]").trigger("click");
		}
	});
	*/
	
	//b2b跳转过来的登陆分支处理
	loadB2BTab();
});

function showTsxx() {
	$("#errors").html("登录密码和卡号查询密码相同！");
}
function isActiveXSupported() {
	return true;
}

function isActiveXRegistered(ctlname) {
	return true;
} 
function getCookiesValue(keyname){
	var name = escape(keyname);
	var allcookies = document.cookie + ";";  //分号为chrome兼容处理
	name+="=";
	var pos = allcookies.indexOf(name);
	if(pos != -1){
		var start = pos +name.length;
		var end = allcookies.indexOf(";",start);
		if(end != -1){
			//end = allcookies.length;
			var value = allcookies.substring(start, end);
			return unescape(value);
			//return value;
		}
	}else{
		return "";
	}
}
function setCookies(keyname,keyvalue,hours,path){
	var name = escape(keyname);
	var value = escape(keyvalue);
	var expires = new Date();
	expires.setTime(expires.getTime()+hours*3600000);
	_expires = (typeof hours) == "string" ? "":";expires="+expires.toUTCString();
	document.cookie = name + "="+value +_expires +path;
}
function deleteCookie(name){
	var date=new Date();
	date.setTime(date.getTime()-10000);
	document.cookie=name+"=v;expires="+date.toGMTString();
}
function getRememerInfo(){
	/*try{
		var userName ="";
		userName = getCookiesValue("username");
		if(userName !=undefined){
			document.getElementById("username").value=userName;
		}else{
			document.getElementById("username").value="";
		}

	}catch(err){
	}*/
}
function checkSetupPowerEnter() {
	getRememerInfo();
}

function doSubmit1()
{
  // 判断输入控件中内容的有效性
  if (!document.all.safeEdit1.isValid()) {
    alert("safeEdit1 has invalid content!");
    return false;
  }

  // 提交控件清空
  document.all.safeSubmit1.reset();
  // 提交验证码向safeEdit1
  document.all.KeyPart.commitKeyPart(document.all.safeEdit1);
  // 将safeEdit1的名字/值对提交至safeSubmit1
  document.all.safeEdit1.commit(document.all.safeSubmit1);

  // 提交验证码向KeyPart
  document.all.KeyPart.commitKeyPart(document.all.KeyPart);
  // 将KeyPart的名字/值对提交至safeSubmit1
  document.all.KeyPart.commit(document.all.safeSubmit1);

  //提交表单
  document.all.safeSubmit1.submit(document.all.form1);

  return false;
}

function dis_icon_key(){
	if(	document.getElementById("pa1").style.display =='none'){
		document.getElementById("pa1").style.display="";
		document.getElementById("pa2").style.display="none";
		
		document.getElementById("cap1").style.display="";
		document.getElementById("cap2").style.display="none";
		document.getElementById("isSafe").value=1;
		document.getElementById("YUse").style.display="";
		document.getElementById("NUse").style.display="none";

	}else{
		document.getElementById("pa2").style.display="";
		document.getElementById("pa1").style.display="none";
		document.getElementById("cap2").style.display="";
		document.getElementById("cap1").style.display="none";
		document.getElementById("isSafe").value=0;
		document.getElementById("YUse").style.display="none";
		document.getElementById("NUse").style.display="";
	}
}

function load(){
	document.getElementById("username").focus();
	
}

function loadB2BTab(){
	var isb2bFlag = jQuery("#isb2b");
	if(isb2bFlag.val() == "1"){
		//记住用户名处理
		var userName = getCookiesValue("EUserName");
		if(userName!=""){
			jQuery("input[name='username']").val(userName);
			jQuery("#rememberPass").attr("checked", "checked");
		}
		
		jQuery("#selectEntry").css("display", "");
		jQuery("#regMerchantLink").css("display", "");
		jQuery("#rememberPassLi").css("display", "");
		
		jQuery("#b2cMerchantTitle").css("display", "none");
		jQuery("#b2bMerchantTitle").css("display", "");
	}
}
function enterLogin(){
	$("#errors").html="";
	if (formSubmit) {
		return;
	}
	formSubmit = true;

	// 验证用户名格式
	var username = $("#username").val();
	if (jQuery.trim(username) == ""
			|| username == $("#username").attr("default")) {
		$("#errors").html("请输入用户名！");
		formSubmit = false;
		return;
	}else{
		if(username.length <4 ||username.length >20){
				$("#errors").html("用户名错误，请重新输入！");
				formSubmit = false;
				return;	
		}else{
				var myRegName = /^[A-Za-z0-9_]{4,20}$/;
				if(!myRegName.test(username)){
					$("#errors").html("用户名错误，请重新输入！");
					formSubmit = false;
					return;	
				}
		}
	}
	var flag = document.getElementById("isSafe").value;
	if(flag==1){
		if (document.all.safeEdit1.getLength()<8){
			//alert("密码长度至少应该为八位!");
			$("#errors").html("密码长度至少应该为八位!");
			document.all.safeEdit1.focus();
			formSubmit = false;
			return;
		}
		if (!document.all.safeEdit1.isValid()){
	   		//alert("输入的密码不合法，请重新输入！");
			$("#errors").html("输入的密码不合法，请重新输入！");
	   		document.all.safeEdit1.focus();
	   		formSubmit = false;
			return ;
			}
		if (!document.all.KeyPart.isValid()){
			//alert("输入的验证码不合法，请重新输入！");
			$("#errors").html("输入的验证码不合法，请重新输入！");
		  	document.all.KeyPart.focus();
		  	formSubmit = false;
			return ;
		}
		  // 判断输入控件中内容的有效性
		  if (!document.all.safeEdit1.isValid()) {
			  $("#errors").html("safeEdit1 has invalid content!");
			  formSubmit = false;
		    return false;
		  }
		  document.getElementById("password").value='jardejarde';
		 document.getElementById("j_captcha").value='jarde';
		  // 提交控件清空
		  document.all.safeSubmit1.reset();
		  // 提交验证码向safeEdit1
		  document.all.KeyPart.commitKeyPart(document.all.safeEdit1);
		  // 将safeEdit1的名字/值对提交至safeSubmit1
		  document.all.safeEdit1.commit(document.all.safeSubmit1);

		  // 提交验证码向KeyPart
		  document.all.KeyPart.commitKeyPart(document.all.KeyPart);
		  // 将KeyPart的名字/值对提交至safeSubmit1
		  document.all.KeyPart.commit(document.all.safeSubmit1);

		  //提交表单
		  document.all.safeSubmit1.submit(document.all.form1);
		  formSubmit = false;
		  return false;
	}
	var password = $("#password").val();
	if (password == null) {
		$("#errors").html("密码不能为空！");
		formSubmit = false;
		return;
	} else {
		var myReg = /^[A-Za-z0-9~!@#$%^&*()_\-+={}\[\]\\|:;"'<>,.?\/]{8,30}$/;
		var diReg = /^[0-9]*$/;
		var chReg = /^[A-Za-z]*$/;
		if(password.length <8 ||password.length >30 ){
			$("#errors").html("密码长度为8到30个字符！");
			formSubmit = false;
			return;	
		}
		if(diReg.test(password) && password.length <10){
			$("#errors").html("密码不可全为数字！");
			formSubmit = false;
			return;	
		}
		if(chReg.test(password)  && password.length <10){
			$("#errors").html("密码不可全为字母！");
			formSubmit = false;
			return;	
		}
		if(!myReg.test(password)){
			$("#errors").html("密码不可包含非法字符！");
			formSubmit = false;
			return;	
		}
		$("[name=password]").val(password);
	}
	var j_captcha = $("#j_captcha").val();
	if (jQuery.trim(j_captcha) == "" || j_captcha.length != 4) {
		$("#errors").html("请输入四位验证码！");
		formSubmit = false;
		return;
	}
	if(submitHandler()){
		$("form").trigger("submit");
	}else{
		$("#errors").html("密码不可包含非法字符！");
		formSubmit = false;
		return;
	}

}
function submitHandler(){
	var pwdInput=document.getElementById("password");
	var pwdValue=pwdInput.value;
	var tempChar;
	var encryptStr="";
	
	for(var i=0;i<pwdValue.length;i++){
		tempChar=pwdValue.charAt(i);
		for(var j=0;j<dict.length/2;j++){
			if(tempChar==dict[j*2]){
				encryptStr+=dict[j*2+1];
				break;
			}else if(j==dict.length/2-1){
				//alert("非法字符");
				return false;
			}
		}
	}
	//alert("加密后的字符："+encryptStr);
	
	pwdInput.value=encryptStr;
	
	return true;
}