//$(function(){
//	//登录动作触发start
//	//商城登录提交开始
//   	$("#id_btn_mall").click(function(){
//   		var usernameLen = $("#id_username").val().length;
//   		var passwordLen = $("#id_pswd_mall_tmp").val().length;
//   		//alert(usernameLen);
//   		if(usernameLen < 1){
//   			$("#id_username_tip").css("display","block");
//   			return ;
//   		}else if(passwordLen < 1){
//   			$("#id_username_tip").css("display","none");
//   			$("#id_pswd_mall_tmp_tip").css("display","block");
//   			return ;
//   		}else{
//   			$("#id_username_tip").css("display","none");
//   			$("#id_pswd_mall_tmp_tip").css("display","none");
//   		}
//   		var keyBoardFlag = $("#id_safeKeyBoard").val()=="1";
//		if(keyBoardFlag){
//			//把密码键盘产生的密文填写到页面的隐藏域
//			submitData();	
//		}else{
//			submitHandler();
//		}
//    	$("#id_form_mall").trigger("submit");
//    	//将a标签解绑click事件防止重复提交
//    	$("#id_btn_mall").unbind("click");
//	});
//  	//商城登录提交结束
//	
//  	//网银登录提交开始
//	$("#id_btn_eBank").click(function(){
//		var logonCardNumLen = $("#id_logonCardNum").val().length;
//		var pswdeBankTmpLen = $("#id_pswd_eBank_tmp").val().length;
//		//alert(logonCardNumLen);
//		if(logonCardNumLen < 1){
//			$("#id_logonCardNum_tip").css("display","block");
//			return ;
//		}else if(pswdeBankTmpLen < 1){
//			$("#id_logonCardNum_tip").css("display","none");
//			$("#id_pswd_eBank_tmp_tip").css("display","block");
//			return ;
//		}else{
//			$("#id_logonCardNum_tip").css("display","none");
//			$("#id_pswd_eBank_tmp_tip").css("display","none");
//		}
//		//把密码键盘产生的密文填写到页面的隐藏域
//		submitData();
//		//alert(5555);
//		$("#id_form_eBank").trigger("submit");
//		//将a标签解绑click事件防止重复提交
//		$("#id_btn_eBank").unbind("click");
//	});
//	//网银登录提交结束
//	//登录动作触发end
//	
//	//初始化处理 start
//	//判断页面类型-是否是从注册页回来的
//	var formType = getParam("fromType");
//	//获取从网银登录失败后返回的错误码
//	var errorCode = getParam("errorCode");
//
//	//网银登录出错后弹出错误信息
//	if(null != errorCode && "" != errorCode){
//		checkErrCode(errorCode);
//	}
//	
//	//判断是否是从注册页回来的
//	if("register" == formType){
//		$('.box-jinrong').hide();
//	  	$('.box-rongyigou').show();
//		//alert(67890);
//	}else{
//		//alert(90000);
//	}
//	
//	//用户名或密码错误弹框提示
//	if ($(".errors span").length < 1) {
//		//$(".errors span:eq(1)").hide();
//	}else{
//		 $('.box-jinrong').hide();
//	  	 $('.box-rongyigou').show();
//		alertJqmInfo($(".errors span").text());
//	}
//	// 启用安全键盘初始化处理
//	$("#id_pswd_mall_tmp").attr('readonly','readonly').bind('click',{id:"keyMall"},callsoftKeyBoard).val("");
//	$("#id_pswd_eBank_tmp").attr('readonly','readonly').bind('click',{id:"keyeBank"},callsoftKeyBoard).val("");
//	$("#id_safeKeyBoard").val("1");
//	$(".safe-keyboard").addClass("safe-keyboard-green");
//	
//	//将选中的input框底线变黑色
//	$('.def_c').focus(function(){
//	  $(this).parent().parent().parent().addClass('msg-border');
//	});
//	$('.def_c').blur(function(){
//	  $(this).parent().parent().parent().removeClass('msg-border');
//	});
//	
//	//安全键盘切换
//	$(".safe-keyboard").toggle(
//	  function () {
//		jQuery("#id_pswd_mall_tmp").blur();
//		jQuery("#id_pswd_mall_tmp").removeAttr('readonly').unbind('click').val("");
//		jQuery("#id_safeKeyBoard").val("0");
//		if(isiPhone()){
//			iOSExcuteNativeMethod("native://closeSoftKeyBoard");
//		}
//		$(this).removeClass("safe-keyboard-green");
//	  },
//	  function () {
//		jQuery("#id_pswd_mall_tmp").blur();
//		jQuery("#id_pswd_mall_tmp").attr('readonly','readonly').bind('click',{id:"keyMall"},callsoftKeyBoard).val("");
//		jQuery("#id_safeKeyBoard").val("1");
//		$(this).addClass("safe-keyboard-green");
//	  }
//	);
//	//金融@家与融易购切换
//   	$('.login-rongyigou').click(function(){
//   		$('.box-jinrong').hide();
//   		$('.box-rongyigou').show();
//   		});
//   	$('.login-jinrong').click(function(){
//   		$('.box-jinrong').show();
//   		$('.box-rongyigou').hide();
//   	});
//	//初始化处理 end
//});
//
//
////密码键盘相关js start 
//	var callback_id = "";
//   function isiPhone(){
//	   return (navigator.userAgent.toLowerCase().match(/iphone os/i) == "iphone os");
//   }
//
//   function isAndroid(){
//       return (navigator.userAgent.toLowerCase().match(/android/i) == "android");
//   }
//
//   function iOSExcuteNativeMethod(param){
//       var iFrame;
//       iFrame = document.createElement("iframe");
//   iFrame.setAttribute("src", param);
//   iFrame.setAttribute("style", "display:none");
//   iFrame.setAttribute("height", "0px");
//   iFrame.setAttribute("width", "0px");
//   iFrame.setAttribute("frameborder", "0");
//       document.body.appendChild(iFrame);
//       iFrame.parentNode.removeChild(iFrame);
//       iFrame = null;
//   }
//
//   function callsoftKeyBoard(event){
//   	   $("#id_pswd_mall_tmp").val("");
//   $("#id_pswd_mall").val("");
//   
//   $("#id_pswd_eBank_tmp").val("");
//   $("#id_pswd_eBank").val("");
//
//   //alert("id:"+event.data.id);
//   if( isiPhone() ){
//	   if("keyMall" == event.data.id){
//		   iOSExcuteNativeMethod("native://callSoftKeyBoard;0;1,1,0");
//	   }else if("keyeBank" == event.data.id){
//		   iOSExcuteNativeMethod("native://callSoftKeyBoard;1;1,1,0");
//	   }
//	   
//   }else if( isAndroid() ){
//	   if("keyMall" == event.data.id){
//		   prompt("callsoftKeyBoard","0;1,1,0");
//	   }else if("keyeBank" == event.data.id){
//		   prompt("callsoftKeyBoard","1;1,1,0");
//    	   }
//    	   
//       }
//   }
//
//   function enableInput( id ){
//       if( id == 0 ){
//           document.getElementById("id_pswd_mall").value = "";
//       //document.getElementById("id_pswd_mall").disabled = false;
//   }else if(id == 1){
//	   document.getElementById("id_pswd_eBank").value = "";
//       }
//   }
//
//   function setText( id, param ){
//	   if( id == 0 ){
//          document.getElementById("id_pswd_mall_tmp").value = param;   
//   }else if(id == 1){
//	   document.getElementById("id_pswd_eBank_tmp").value = param;
//	   }
//   }
//    
//   function submitData(){
//       if( isiPhone() ){
//    	   callback_id = "0";
//	   iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&0");
//	   callback_id = "1";
//	   iOSExcuteNativeMethod("native://submitData&callBack=submitCallBack&1");
//   }else if( isAndroid() ){
//	   //如果是商城登录调用的密码键盘
//	   var paramMallOrg = prompt("getEncryptString","0");
//	   var paramMall = (new Function("return" + paramMallOrg))();
//	   var pswdMall = paramMall.loginPasswd;
//	   var changeruleMall = paramMall.changeRule;
//	   var ruleMall = paramMall.rule;
//	   //alert(loginPasswd1);
//	   //alert(changerule1);
//	   //alert(rule1);
//	   $("#id_pswd_mall").val(pswdMall);
//       $("#id_changerule_mall").val(changeruleMall);
//       $("#id_rule_mall").val(ruleMall);
//       
//       //如果是网银登录调用的密码键盘
//       var parameBankOrg = prompt("getEncryptString","1");
//	   var parameBank = (new Function("return" + parameBankOrg))();
//	   var pswdeBank = parameBank.loginPasswd;
//	   var changeruleeBank = parameBank.changeRule;
//	   var ruleeBank = parameBank.rule;
//	   //alert(loginPasswd1);
//	   //alert(changerule1);
//	   //alert(rule1);
//	   $("#id_pswd_eBank").val(pswdeBank);
//       $("#id_changerule_eBank").val(changeruleeBank);
//       $("#id_rule_eBank").val(ruleeBank);
//       }
//   }
//
//   function submitCallBack( params ){
//	   var loginPasswd = params.loginPasswd;
//       var changerule = params.changeRule;
//       var rule = params.rule;
//       if(callback_id == "0"){
//	   //alert("mall:"+loginPasswd);
//	   $("#id_pswd_mall").val(loginPasswd);
//       $("#id_changerule_mall").val(changerule);
//       $("#id_rule_mall").val(rule);
//   }else if(callback_id == "1"){
//	   //alert("eBank:"+loginPasswd);
//	   $("#id_pswd_eBank").val(loginPasswd);
//       $("#id_changerule_eBank").val(changerule);
//       $("#id_rule_eBank").val(rule);
//       }
//   }
////密码键盘相关js end   
//
//   
// //弹窗提示
//	 function alertJqmInfo(msg,func,params){
//		$("#modal_show_tip").text(msg);
//		$('#modalShowTip').on('hide',function(){
//			if(func){
//				if(params){
//					func(params);
//					}else{func();} 
//				}		
//			});
//		$('#modalShowTip').modal();	
//	 }
//	//根据id获取url参数
//		function getParam(param){
//			var url = location.href;
//			var paraString = url.substring(url.indexOf("?")+1,url.length).split("&");
//			var paraObj = {};
//			for(i=0;j=paraString[i];i++){
//				paraObj[j.substring(0,j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=")+1,j.length);
//			}
//			var returnVal = paraObj[param.toLowerCase()];
//			if(typeof(returnVal) == "undefined"){
//				return "";
//			}else {
//				return returnVal;
//			}
//			
//		}
//	
//
////	 	alert(errorCode);
////		2680:用户密码错误
////		3401:用户名不存在
////		96111945:验证码错误或超时
////		7956:错误次数已超过最大次数
////		1:必须修改网银登录密码
////		2:必须修改网银预留信息
//		function checkErrCode(code){
//			switch(code){
//			case "2680":
//				alertJqmInfo("用户名或登录密码错误");
//				break;
//			case "3401":
//				alertJqmInfo("用户名或登录密码错误");
//				break;
//			case "7956":
//				alertJqmInfo("错误次数已超过最大次数");
//				break;
//			case "5787":
//				alertJqmInfo("请按照您预先设定的登录方式登录");
//				break;
//			case "1":
//				alertJqmInfo("请登录个人网上银行修改网银登录密码");
//				break;
//			case "2":
//				alertJqmInfo("请登录个人网上银行修改网银预留信息");
//				break;
//			default:
//				alertJqmInfo("抱歉，网银系统错误");
//			}
//		}
//		