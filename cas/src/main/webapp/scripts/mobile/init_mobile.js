/**
 * 此文件是未加载jQuerymobile之前对页面做的一些设置
 */
$(document).bind("mobileinit", function(){
	
	/*
	   设定页面不以ajax方式提交,如果页面没全局设定可以使用单独的a标签设置：<a href="#" data-ajax="false"></a>
	 */
	$.mobile.ajaxEnabled=false;

	/*  
		页面跳转时可使用的一些事件
	
		pagebeforeshow：转场之前，页面被显示时触发
		
		pagebeforehide：转场之前，页面被隐藏时触发
		
		pageshow：转场之后，页面被显示时触发
		
		pagehide：转场之后，页面被隐藏时触发
		
		//使用
		$([id|class]).live('pageshow',function(event, ui){
		　alert(ui.prevPage);
		});
	*/

});
