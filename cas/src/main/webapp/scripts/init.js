$(function(){
//顶部栏目hover悬浮购物车
$('#ct').live('mouseover',function(){
	$(this).addClass('ctm_show');
});
$('#ct').live('mouseout',function(){
	$(this).removeClass('ctm_show');
});
$('#ct').live('click',function(){
	$(this).removeClass('ctm_show');
});
// 添加物品到购物车 效果
//$('.btn3').click(function () { $('#ct').addClass('ctm_show') })
//$('.btn3').click(function () { $('#ct').addClass('ctm_show').show('slow'); })




//返回顶部初始化
(function($){
	var doc_width=950;		//设置当前文档内容的宽度
	var top_offset=500;		//设置相对顶部距离的高度
	var backToTop_width=41; //设置回到顶部图标的宽度
	var speed=300; 			//回到顶部的速度
	var $backToTopTxt = "返回顶部", $backToTopEle = $('<div class="backToTop"></div>').appendTo($("body")).attr("title", $backToTopTxt).click(function() {
		$("html, body").animate({scrollTop:0 },speed);
}), $backToTopFun = function() {
	var st = $(document).scrollTop(), winh = $(window).height();
	(st > top_offset)? $backToTopEle.show(): $backToTopEle.hide();	
	var _offset=($(document.body).width()-doc_width)/2-backToTop_width;
	$backToTopEle.css('right',_offset);
	//if($.browser.msie&&$.browser.version<=6){b={right:"-21px"}}else{b={right:(document.documentElement.clientWidth-c)
	//IE6下的定位
	if (!window.XMLHttpRequest){$backToTopEle.css("top", st + winh - 96);}
};
	$(window).bind("scroll", $backToTopFun);
	 $backToTopFun(); 	
})(jQuery)


$(window).bind('scroll',function(){
	if($('#p_detaillist').length){
	var tbtop=$('#p_detaillist').offset().top;
	if($(document).scrollTop()>tbtop){
		$('#tabname').addClass('fixed');
			if ($.browser.msie && ($.browser.version == "6.0") && !$.support.style) { 
			$('#tabname').css('top',$(document).scrollTop());
			} 
	}else{
	$('#tabname').removeClass('fixed');
	};
	}
});
	
});

