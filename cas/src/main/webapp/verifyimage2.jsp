<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%//String randomId = utb.getSessionId(); //该变量需要事先在外部jsp声明，下面将直接使用 %>
<%//"link4Verifyimage2Name" 这个id供部分交易修改链接文字使用，形如“document.getElementById("link4Verifyimage2Name").innerHTML="刷新";” %>
<%
String imageAlt4Verifyimage2 = java.net.URLEncoder.encode("点击图片可刷新", "gbk");
String link4Verifyimage2 = "<nobr>刷新验证码</nobr>";

String ebankServletUrl = instance.getsmsProperty("cas.eBankServletUrl");
//String ebankServletUrl = "https://82.201.30.32:11491";
%>
<IFRAME frameBorder="0" name="VerifyimageFrame" id="VerifyimageFrame" scrolling="no" 
	src="<%=ebankServletUrl %>?randomKey=<%=randomId%>&imageAlt=<%=imageAlt4Verifyimage2%>" 
	marginHeight=0 marginWidth=0 
	height=30 width=80 
	<%-- onload="this.contentWindow.document.oncontextmenu =  function(){return false;};" --%>
	>
</IFRAME>
<iframe style="position:absolute;top:-17px;left:3px;width:80px;height:30px;background:transparent;opacity:0;filter:progid:DXImageTransform.Microsoft.Alpha(opacity = 0); " >
</iframe>
<div onclick="refreshVerifyimage();" style="position:absolute;top:-17px;left:3px;width:80px;height:30px;background:transparent;cursor:pointer;">
</div>
&nbsp;<a id="link4Verifyimage2Name" href="JavaScript:refreshVerifyimage();" style="COLOR: blue; text-decoration: underline; "><nobr><%=link4Verifyimage2%></nobr></a>
<script language="JavaScript">
if(navigator.userAgent.toLowerCase().indexOf("chrome/")!=-1){//这里注意chrome的useragent也包含safari
	VerifyimageFrame.location.href="<%=ebankServletUrl %>?randomKey=<%=randomId%>&imageAlt=<%=imageAlt4Verifyimage2%>"+"&appendRandom="+(new Date()).getTime();
}else if(navigator.userAgent.toLowerCase().indexOf("safari")!=-1|| navigator.userAgent.toLowerCase().indexOf("firefox")!=-1){
	VerifyimageFrame.location.href="<%=ebankServletUrl %>?randomKey=<%=randomId%>&imageAlt=<%=imageAlt4Verifyimage2%>"+"&appendRandom="+(new Date()).getTime();
}
function refreshVerifyimage(){
	var url = "<%=ebankServletUrl %>?randomKey=<%=randomId%>&imageAlt=<%=imageAlt4Verifyimage2%>"+"&appendRandom="+(new Date()).getTime();
	if(navigator.userAgent.toLowerCase().indexOf("chrome/")!=-1){//这里注意chrome的useragent也包含safari
		VerifyimageFrame.location.replace(url);
	}else if(navigator.userAgent.toLowerCase().indexOf("safari")!=-1){//safari回退时，验证码不刷新问题
		window.focus();
		//VerifyimageFrame.location.href="<%=ebankServletUrl %>?randomKey=<%=randomId%>&imageAlt=<%=imageAlt4Verifyimage2%>"+"&appendRandom="+(new Date()).getTime();
		VerifyimageFrame.location.replace(url);
	}else{
		//VerifyimageFrame.location.reload();
		VerifyimageFrame.location.replace(url);
		
	}

	 try{
		//刷新验证码后重新进行倒计时，只对页面定义了vctimeout()的页面有效。
	    clearTimeout(vctime);
	    vctime=setTimeout("vctimeout()",300000);
	}catch(exception){} 
		
}
/*
if(pebankBrowserCompatible.isSafari()||pebankBrowserCompatible.isChrome()){
	//屏蔽回退按钮
	challengeFrame.history.go=function(){
		pebankBrowserCompatible.alertHistoryTip();
	}
	challengeFrame.history.back=function(){
		pebankBrowserCompatible.alertHistoryTip();
	}
}
*/
</script>