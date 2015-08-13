
var formSubmit = false; // 表单是否提交
//发送验证码,点击发送手机验证码之前，还需要再验证一下手机号是否合法
function sendMobileCode_before(){
	sendMobileCode("sendMobileCode();");
}

//重新发送验证码
function reSendMobileCode(){
	sendMobileCode("sendMobileCode();");
}

$(function(){
	$("#reSendCodeA").attr('disabled',true);
	
	//b2b分支处理
	var isb2bFlag = jQuery("#isb2b");
	if(isb2bFlag.val() == "1"){
		jQuery("#b2cMerchantTitle").css("display", "none");
		jQuery("#b2bMerchantTitle").css("display", "");
		jQuery("#b2cMerchantTitle2").css("display", "none");
		jQuery("#b2bMerchantTitle2").css("display", "");
	}
});

//发送短信验证码
function sendMobileCode(){
	jQuery.ajax({
			type: "GET",
			dataType: "text",
			url:"/sendPhoneCode.ajax",
			success: function(data){
				if (!data || data==""){
					$('#sendCodeMsg').html("短信发送异常");
					return true;
				}
				var secs = 59; //手机发送验证码的倒计时间
				var dataObj = eval('('+ data +')');
				var noid = dataObj.noId; //$("#sendCodeMsg").val();
				var newTempPwd = dataObj.newTempPwd;
				if(newTempPwd){
					$('#validatePhoneCode').val(newTempPwd);
				}
				$('#sendCodeMsg').val(noid);
				//发送短信成功
				$("#sendCode").attr('disabled',true);
				$('#sendCodeMsg').html("短信已发送到您的手机，请输入短信中的登录校验码，此服务免费，编号"+noid);
				$("#reSendCode").show();
				$("#sendCodeMsg").show();
				$("#reSendCodeA").attr('disabled',true);
				if(dataObj.secs){
					secs = dataObj.secs;
					$('#sendCodeMsg').html("验证码请求频率过快请" + secs + "秒后重新获取。");
				}
				
				if(dataObj.result == "error") {
					changeShowContext("error");
				} else {
					timeInterval(secs);
				}
				return true;
			}
	});
}
//短信验证码校验
function checkMobileCode(){
	var  code =$("input[name='validate']").val();
	if(code==""){
		alert("请输入登录校验码!");
		return;
	}
	
}
$(function () {
	changeShowContext("true");
	//刷新验证码
	$("#refreshValidateCode").click(function(){
		var timestamp = (new Date()).getTime();
		$("#validateCode").attr("src", ctx + "/captcha.jpg?timestamp=" + timestamp);
	});
});

function changeShowContext(resultType){
	var $validateCodePage = $("#validateCodePage");
	var $tab_con = $("#phoneCodePage");
	if(resultType=="error"){
		$tab_con.hide();
		reloadValidateCodePage();
		$validateCodePage.show();
	}else {
		$validateCodePage.hide();
		$tab_con.show();
	}
	
}

function getPhoneCode(){
	var $validateCode = $("#validateCodeValue").val();
	if($validateCode.length!=4){
//		$("#validateCodePage").find("label").css(color,"#c7000b").html("请输入正确验证码！");
		alert("请输入正确验证码！");
		return;
	}
	jQuery.ajax({
		type: "POST",
		dataType: "json",
		data:{validateCode:$validateCode},
		url:"/getPhoneCodeByVerifyImage.ajax",
		success: function(data){
			if (!data || data==""){
				$('#sendCodeMsg').html("短信发送异常");
				return true;
			}
			
			if(data.validateCode == "error"){
				alert("请输入正确验证码！");
				return;
			}
			timeInterval(59);
			var noid = data.noId; //$("#sendCodeMsg").val();
			var newTempPwd = data.newTempPwd;
			if(newTempPwd){
				$('#validatePhoneCode').val(newTempPwd);
			}
			$('#sendCodeMsg').val(noid);
			//发送短信成功
			$("#sendCode").attr('disabled',true);
			$('#sendCodeMsg').html("短信已发送到您的手机，请输入短信中的登录校验码，此服务免费，序列号为"+noid);
			$("#reSendCode").show();
			$("#sendCodeMsg").show();
			$("#reSendCodeA").attr('disabled',true);
			changeShowContext("true");
			return true;
		}
	});
}
var secs = "59";
var tm = null; //定时触发倒计时的句柄
function timeInterval(secsArgs){
	//清除计时器
	clearInterval(tm);
	 //手机发送验证码的倒计时间
	secs = secsArgs;
	tm = setInterval(
			function(){
				$('#reSendCodeA').val(secs+'秒后重新发送');
				if(secs>0){
					secs--;
				}else{
					$("#reSendCodeA").attr('disabled',false);
					$("#sendCode").attr('disabled',false);
					$('#reSendCodeA').removeClass("icon_btn_r_d");
					$('#reSendCodeA').val('60秒再次发送');
					clearInterval(tm);
					return null;
				}
			},
			1000
			);
}

function reloadValidateCodePage(){
	$("#validateCodeValue").val("");
	$("#refreshValidateCode").click();
}
$(function() {
	$("#button").click(function() {
//		alert("111111111111");
//		$("#errors").html("请输入用户名！");
		formSubmit = false;
//		alert("22222");
		$("#form1").trigger("submit");
		
	});
});