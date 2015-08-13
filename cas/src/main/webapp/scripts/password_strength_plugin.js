(function($){ 
$.fn.shortPass = '太短'; 
$.fn.badPass = '弱'; 
$.fn.goodPass = '中'; 
$.fn.strongPass = '强'; 
$.fn.samePassword = '用户名和密码相同'; 
$.fn.resultStyle = ""; 
$.fn.passStrength = function(options) { 
var defaults = {
isSelf:true,		//false:自定义密码强壮状态栏，
msgToClassName:null,//自定义情况下密码强壮信息写入到的元素， 必须isSelf为false才可使用。
execFun:null,		//按下调用的函数传入参数值。
shortPass: "shortPass", //optional 
badPass: "badPass", //optional 
goodPass: "goodPass", //optional 
strongPass: "strongPass", //optional 
baseStyle: "testresult", //optional 
userid: "", //required override 
messageloc: 1//before == 0 or after == 1 
}; 
var opts = $.extend(defaults, options); 
return this.each(function(){ 
var obj = $(this); 
$(obj).unbind().keyup(function() 
{
var results = $.fn.teststrength($(this).val(),$(opts.userid).val(),opts);
if(opts.isSelf){
	if(opts.messageloc === 1)
	{
	$(this).next("." + opts.baseStyle).remove(); 
	$(this).after("<span class=\""+opts.baseStyle+"\"><span></span></span>"); 
	$(this).next("." + opts.baseStyle).addClass($(this).resultStyle).find("span").text(results); 
	} 
	else 
	{
	$(this).prev("." + opts.baseStyle).remove(); 
	$(this).before("<span class=\""+opts.baseStyle+"\"><span></span></span>"); 
	$(this).prev("." + opts.baseStyle).addClass($(this).resultStyle).find("span").text(results); 
	}
}else{
	if(opts.msgToClassName)$(this).siblings('.'+opts.msgToClassName).find('span span').text(results); 
	($(this).samePassword===results||$(this).shortPass===results||$(this).badPass===results)?$('.'+opts.badPass).addClass('ok'):$('.'+opts.badPass).removeClass('ok');
	($(this).goodPass===results)?$('.'+opts.goodPass).addClass('ok'):$('.'+opts.goodPass).removeClass('ok');
	($(this).strongPass===results)?$('.'+opts.strongPass).addClass('ok'):$('.'+opts.strongPass).removeClass('ok');
	if(opts.execFun)opts.execFun($(this).val());
}
}); 
//FUNCTIONS 
$.fn.teststrength = function(password,username,option){ 
var score = 0; 
//password < 4 
if (password.length < 4 ) { this.resultStyle = option.shortPass;return $(this).shortPass;} 
//password == user name 
if($.trim(username).length>0)
if (password.toLowerCase()==username.toLowerCase()){this.resultStyle = option.badPass;return $(this).samePassword;} 
//password length 
score += password.length * 4; 
score += ( $.fn.checkRepetition(1,password).length - password.length ) * 1; 
score += ( $.fn.checkRepetition(2,password).length - password.length ) * 1; 
score += ( $.fn.checkRepetition(3,password).length - password.length ) * 1; 
score += ( $.fn.checkRepetition(4,password).length - password.length ) * 1; 
//password has 3 numbers 
if (password.match(/(.*[0-9].*[0-9].*[0-9])/)){ score += 5;} 
//password has 2 symbols 
if (password.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)){ score += 5 ;} 
//password has Upper and Lower chars 
if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)){ score += 10;} 
//password has number and chars 
if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/)){ score += 15;} 
// 
//password has number and symbol 
if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([0-9])/)){ score += 15;} 
//password has char and symbol 
if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([a-zA-Z])/)){score += 15;} 
//password is just a numbers or chars 
if (password.match(/^\w+$/) || password.match(/^\d+$/) ){ score -= 10;} 
//verifying 0 < score < 100 
if ( score < 0 ){score = 0;} 
if ( score > 100 ){ score = 100;} 
if (score < 34 ){ this.resultStyle = option.badPass; return $(this).badPass;} 
if (score < 68 ){ this.resultStyle = option.goodPass;return $(this).goodPass;} 
this.resultStyle= option.strongPass; 
return $(this).strongPass; 
//Download by http://www.codefans.net
}; 
}); 
}; 
})(jQuery); 
$.fn.checkRepetition = function(pLen,str) { 
var res = ""; 
for (var i=0; i<str.length ; i++ ) 
{ 
var repeated=true; 
for (var j=0;j < pLen && (j+i+pLen) < str.length;j++){ 
repeated=repeated && (str.charAt(j+i)==str.charAt(j+i+pLen)); 
} 
if (j<pLen){repeated=false;} 
if (repeated) { 
i+=pLen-1; 
repeated=false; 
} 
else { 
res+=str.charAt(i); 
} 
} 
return res; 
}; 
