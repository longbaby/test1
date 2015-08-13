var keyboardHeight;// 安全键盘高度
var winHeight;// 屏幕高度
var docHeight;// 页面高度
var eleDocTop;// 密码框底部距离页面顶端的高度
var eleWinTop;// 密码框底部距离屏幕顶端的高度
var moveHeight=0;// 页面需要上移的高度
winHeight = $(window).height();// 首先获得屏幕高度
var isSysBoard = {
//		"ebankPsd" : false,
		"mallPsd" : false
};

$(function(){
	// 页面高度
	docHeight = $(document).height();
	// 密码框底部距离页面顶端的高度
	eleDocTop = $("#id_pswd_box").offset().top;
	// 密码框底部距离屏幕顶端的高度
	eleWinTop = eleDocTop - document.body.scrollTop;

	// 商城登录提交开始
   	$("#id_btn_mall").click(function(){
   		
   		var username = $("#id_username").val();
   		var usernameLen = $("#id_username").val().length;
   		var password = $("#id_pswd_mall_tmp").val();
   		var passwordLen = $("#id_pswd_mall_tmp").val().length;
   		var keyBoardFlag = document.getElementById("id_a_che_saveKb").checked;
   		
   		if(jQuery.trim(username) == "" || username == $("#id_username").attr("default")){		
   			$("#id_login_tip").html("请输入用户名！").css("display","block");
   			return;
   		}else{
   			/*if(usernameLen < 4 || usernameLen > 20){
   				$("#id_login_tip").html("用户名错误，请重新输入！").css("display","block");
   				return ;
   			}else{
	   			var myRegName = /^[A-Za-z0-9_]{4,20}$/;
				if(!myRegName.test(username)){
					$("#id_login_tip").html("用户名错误，请重新输入！").css("display","block");
					return;	
				}
	   		}*/
	   		$("#id_login_tip").html("").css("display","none");
   		}
   		
		if(password == null || password == ""){
			$("#id_login_tip").html("密码不能为空！").css("display","block");
			return;
		}else{
	   		if(!keyBoardFlag){
//	   			var myReg = /^[A-Za-z0-9~!@#$%^&*()_\-+={}\[\]\\|:;"'<>,.?\/]{8,30}$/;
	   			var myReg = /^[A-Za-z0-9~!@#$%^&*()_\-+={}\[\]\\|:;"'<>,.?\/]*$/;
	   			var diReg = /^[0-9]*$/;
	   			var chReg = /^[A-Za-z]*$/;
	   			/*if(passwordLen <8 || passwordLen > 30 ){
	   				$("#id_login_tip").html("密码长度为8到30个字符！").css("display","block");
	   				return;	
	   			}*/
	   			if(passwordLen <4){
	   				$("#id_login_tip").html("密码长度至少应该为四位！").css("display","block");
	   				return;	
	   			}
	   			if(diReg.test(password) && passwordLen <10){
	   				$("#id_login_tip").html("密码不可全为数字！").css("display","block");
	   				return;	
	   			}
	   			if(chReg.test(password)  && passwordLen <10){
	   				$("#id_login_tip").html("密码不可全为字母！").css("display","block");
	   				return;	
	   			}
	   			if(!myReg.test(password)){
	   				$("#id_login_tip").html("密码不可包含非法字符！").css("display","block");
	   				return;	
	   			}
	   		}else{
	   			/*if(passwordLen < 8){
		   	   		$("#id_login_tip").html("密码长度至少应该为八位！").css("display","block");
	   	   			return ;
	   	   		}*/
	   			if(passwordLen < 4){
		   	   		$("#id_login_tip").html("密码长度至少应该为四位！").css("display","block");
	   	   			return ;
	   	   		}
	   			
	   		}
	   		$("#id_login_tip").html("").css("display","none");
		}
		
		
		if(keyBoardFlag){
			if(isSysBoard.mallPsd==false){// 安全键盘
				// 把密码键盘产生的密文填写到页面的隐藏域
				submitData();	
			}else{// 安全键盘切换为系统键盘
				$("#id_changerule_mall").val("");
                $("#id_rule_mall").val("");
				var psdObj = document.getElementById("id_pswd_mall_tmp");
				submitHandler(psdObj);
			}
		}else{// 系统键盘
			$("#id_changerule_mall").val("");
            $("#id_rule_mall").val("");
 			$("#id_pswd_mall_tmp_submit").val(password);
			var psdObj = document.getElementById("id_pswd_mall_tmp_submit");
			submitHandler(psdObj);
		}
		$("#id_a_login_btn").html("登录中...");
    	$("#id_form_mall").trigger("submit");
    	// 将a标签解绑click事件防止重复提交
    	$("#id_btn_mall").unbind("click");
	});
  	// 商城登录提交结束
	
  	
	
	// 用户名或密码错误弹框提示
	if ($(".errors span").length < 1) {
		$("#id_login_tip").html("").css("display","none");
		// $(".errors span:eq(1)").hide();
	}else{
		$("#id_login_tip").html($(".errors span").text()).css("display","block");
/*		$(".errors span").hide();
		$(".errors span").css({
			'height':'0px',
			'width':'0px',
			'color':'white',
			'overflow':'hidden'
		});
		alertJqmInfo($(".errors span").text());
*/		
	}
	
	// 安全键盘切换
	$("#id_a_che_saveKb").change(function(){
		var changeStatus = document.getElementById("id_a_che_saveKb").checked;
		if(changeStatus){// 安全键盘
			jQuery("#id_safeKeyBoard").val("1");
			jQuery("#id_pswd_mall_tmp").remove();
			jQuery("#id_pswd_mall_tmp_submit").remove();
			jQuery("#id_pswd_mall_div").prepend("<input type='text' data-role='none' class='def_c' name='passwordPre' id='id_pswd_mall_tmp' value='' maxLength='30' placeholder='登录密码' />");
			jQuery("#id_pswd_mall_tmp").blur();
			jQuery("#id_pswd_mall_tmp").attr('readonly','readonly').bind('click',{id:"keyMall"},callsoftKeyBoard).val("");
		}else{// 系统键盘
			jQuery("#id_safeKeyBoard").val("0");
			jQuery("#id_pswd_mall_tmp").remove();
			jQuery("#id_pswd_mall_div").prepend("<input type='password' data-role='none' class='def_c' id='id_pswd_mall_tmp' value='' maxLength='30' placeholder='登录密码'  onkeydown='enterKeySubmit(event);'/>");
			jQuery("#id_pswd_mall_div").prepend("<input type='hidden' name='passwordPre' id='id_pswd_mall_tmp_submit' value='' maxLength='30'/>");
			jQuery("#id_pswd_mall_tmp").blur();
			jQuery("#id_pswd_mall_tmp").removeAttr('readonly').unbind('click').val("");
		}
	});
	
});

function changeLayer(flag) {
	if(flag == "0"){
		$('.blk-js').css({
			height: function() {
				return 0;
			}
		});

	}else if(flag == "1") {
		 // 呼出安全键盘
		if( (winHeight - eleWinTop)< keyboardHeight ){ // 密码框被遮挡
			// 计算需要上移的高度
			moveHeight = keyboardHeight - (winHeight - eleWinTop) + 10;
			// 判断页面长度是否足够上移
			var docBottom = docHeight - eleDocTop;	// 密码框底部距离页面底部的高度
			var winBottom = winHeight - eleWinTop;	// 密码框底部距离屏幕底部的高度
			if( (docBottom - winBottom) < moveHeight){
				// 不够上移高度，需要增加页面长度
				var blkHeight = moveHeight - (docBottom - winBottom);// 需要补充的空白高度
				$('.blk-js').css({
					height: function() {
						return blkHeight;
					}
				});
				
			}
			// 上移页面
			$('body,html').animate({scrollTop:($(window).scrollTop() + moveHeight)},150);
		}
	}
}

// 弹窗提示
/*function alertJqmInfo(msg,func,params){
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
*/
// 根据id获取url参数
/*function getParam(param){
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
*/
function submitHandler(pwdInput){
	// var pwdInput=document.getElementById("id_pswd_mall_tmp_submit");
	var pwdValue=pwdInput.value;
	var tempChar;
	var encryptStr="";
//		var dict = <%=sb.toString()%>
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
	// $("#id_pswd_mall_tmp_submit").val(encryptStr);
	pwdInput.value=encryptStr;
	return true;
}
 		

// 回车自动登录
function enterKeySubmit(event){
	if(event.keyCode == 13){
		var actId = document.activeElement.id;
		if(actId == "id_pswd_mall_tmp"){
			$("#id_btn_mall").click();
		}
	}
}


///////////////////////////////////
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
		// docHeight = $(document).height();
		// 密码框底部距离页面顶端的高度
		// eleDocTop = $("#id_mall_red_login").offset().top +
		// $("#id_mall_red_login").outerHeight();
		eleDocTop = $("#id_get_pass").offset().top;
		// 密码框底部距离屏幕顶端的高度
		eleWinTop = eleDocTop - document.body.scrollTop;
	}
	/*
	 * else if("keyeBank" == event.data.id){ // docHeight =
	 * $(document).height(); // 密码框底部距离页面顶端的高度 //eleDocTop =
	 * $("#id_eBank_red_login").offset().top +
	 * $("#id_eBank_red_login").outerHeight(); eleDocTop =
	 * $("#id_ebank_box").offset().top; // 密码框底部距离屏幕顶端的高度 eleWinTop = eleDocTop -
	 * document.body.scrollTop; }
	 */    	   
   changeLayer(1);
   isSysBoard.mallPsd=false;
//   isSysBoard.ebankPsd=false;
   
   $("#id_pswd_mall_tmp").val("");
   $("#id_pswd_mall").val("");
   
   $("#id_pswd_eBank_tmp").val("");
   $("#id_pswd_eBank").val("");

   if( isiPhone() ){
	   if("keyMall" == event.data.id){
		   iOSExcuteNativeMethod("native://showKeyBoardButton;0;0;1");
		   iOSExcuteNativeMethod("native://callSoftKeyBoard;0;1,1,0");
	   }/*else if("keyeBank" == event.data.id){
		   iOSExcuteNativeMethod("native://showKeyBoardButton;0;0;0");
		   iOSExcuteNativeMethod("native://callSoftKeyBoard;1;1,1,0");
	   }*/
	   
   }else if( isAndroid() ){
	   if("keyMall" == event.data.id){
		   prompt("showKeyBoardButton","登录;关闭;1");
		   prompt("callsoftKeyBoard","0;1,1,1");
	   }/*else if("keyeBank" == event.data.id){
		   prompt("showKeyBoardButton","登录;关闭;0");
		   prompt("callsoftKeyBoard","1;1,1,0");
	   }*/   
	}
}

function enableInput( id ){
	if( id == 0 ){
		document.getElementById("id_pswd_mall").value = "";
		// document.getElementById("id_pswd_mall").disabled = false;
	}/*else if(id == 1){
		document.getElementById("id_pswd_eBank").value = "";
	}*/
}

function setText( id, param ){
	if( id == 0 ){
		document.getElementById("id_pswd_mall_tmp").value = param;   
	}/*else if(id == 1){
		document.getElementById("id_pswd_eBank_tmp").value = param;
	}*/
}
    
function submitData(){
	  
	if( isiPhone() ){
		// 商城
		callback_id = "0";
		iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&0");
		/*// 网银
		callback_id = "1";
		iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&1");*/

	}else if( isAndroid() ){
		// 如果是商城登录调用的密码键盘
		var paramMallOrg = prompt("getEncryptString","0");
		var paramMall = (new Function("return" + paramMallOrg))();
		var pswdMall = paramMall.loginPasswd;
		var changeruleMall = paramMall.changeRule;
		var ruleMall = paramMall.rule;
		$("#id_pswd_mall").val(pswdMall);
		$("#id_changerule_mall").val(changeruleMall);
		$("#id_rule_mall").val(ruleMall);

		/*// 如果是网银登录调用的密码键盘
		var parameBankOrg = prompt("getEncryptString","1");
		var parameBank = (new Function("return" + parameBankOrg))();
		var pswdeBank = parameBank.loginPasswd;
		var changeruleeBank = parameBank.changeRule;
		var ruleeBank = parameBank.rule;
		$("#id_pswd_eBank").val(pswdeBank);
		$("#id_changerule_eBank").val(changeruleeBank);
		$("#id_rule_eBank").val(ruleeBank);*/
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
	}/*else if(callback_id == "1"){ 
		$("#id_pswd_eBank").val(loginPasswd);
		$("#id_changerule_eBank").val(changerule);
		$("#id_rule_eBank").val(rule);
	}*/
}


// 接收高度 kbHeight：键盘高度，scrHeight：屏幕高度
function sendSoftKeyBoardHeight(kbHeight,scrHeight){
   	winHeight = $(window).height();
   	var ra = (scrHeight/winHeight);// 比例
   	keyboardHeight = kbHeight/ra;// 实际用于计算的键盘高度
}
 
function changeToSystemKeyBoard(id){
	if(id=="0"){  // 商城用户
		isSysBoard.mallPsd=true;
		document.getElementById("id_pswd_mall_tmp").type='password';
		$("#id_pswd_mall_tmp").removeAttr('readonly').focus();
	}/*else if(id=="1"){ // 网银用户
		isSysBoard.ebankPsd=true;
		document.getElementById("id_pswd_eBank_tmp").type='password';
		$("#id_pswd_eBank_tmp").removeAttr('readonly').focus();
	}*/
}
