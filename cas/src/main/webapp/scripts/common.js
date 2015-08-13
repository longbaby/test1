//设置购物车是否有滚动条
function resetFloatCart(){
if($('#cart_f .pro').length>4){
	$('#cart_f ul').addClass('scroll');
	$('#cart_f').width($('#cart_f').width()+7);
	}else{
		$('#cart_f ul').removeClass('scroll');
		if($(this).find('.scroll').length)
		$('#cart_f').width($('#cart_f').width()-7);
		}
}

//数字加减控件
$.fn.opNum=function(options){
	var ops={
		addBtn:'aBtn',		//minusBtn：递减按钮
		minusBtn:'mBtn',	//递增按钮
		nums:'nums',		//input值框
		downLimit:1,		//下限值
		numStep:50,			//步长值
		defVal:1,			//默认值
		upLimit:999			//上限值
	 }
	options&&$.extend(ops,options);
	$(this).find('.'+ops.nums).val(ops.defVal);
	$(this).find('.'+ops.minusBtn).click(function(){
		var numsV=parseInt($(this).siblings('.'+ops.nums).val());
		if(numsV>ops.downLimit){
			$(this).siblings('.'+ops.nums).val(numsV-ops.numStep);
		}	
	});
	$(this).find('.'+ops.addBtn).click(function(){
		var numsV=parseInt($(this).siblings('.'+ops.nums).val());
		if(numsV<ops.upLimit){
			$(this).siblings('.'+ops.nums).val(numsV+ops.numStep);
		}	
	});
}

//首页简易幻灯片控件
$.fn.easyBanr=function(options){
	var ops={
		idx:0,
		looper:3500,
		imgLi:'a',
		pages:'pgs',
		pageCur:'pgc'
	 }
	options&&$.extend(ops,options); 
	var obj=$(this);
	var len=$(ops.imgLi,obj).length;
	var pgn=1+ops.idx;
	$('.'+ops.pages,obj).html(""+$(ops.imgLi,obj).length);
	$('.'+ops.pageCur,obj).html(pgn);
	
	function showImg(){
		if(len >1){
			var imgli = $(ops.imgLi,obj).eq(ops.idx);
			$(ops.imgLi,obj).not(imgli).hide();
			imgli.stop(true,true).fadeIn('slow');
			pgn=ops.idx = (++ops.idx) % (len);	
			(pgn==0)?$('.'+ops.pageCur,obj).html(len):$('.'+ops.pageCur,obj).html(pgn);		
		}	
	}
		
	var myTimer;
	$(this).hover(function(){
		if(myTimer){clearInterval(myTimer);}
	},function(){
		myTimer = setInterval(function(){
					showImg();
					}, ops.looper);
	}).mouseleave();
	showImg();
}


//飞入到购物车
$.fn.flyTo = function(options){
	var ops={
target:null, //设置飞入的对象
trigger:null //设置点击触发的对象
}
	options&&$.extend(ops,options);
	var obj=$(this);
	var _top=obj.offset().top;
	var _left=obj.offset().left;
	var _tleft=$(ops.target).offset().left+($(ops.target).width()/2);
	var _ttop=$(ops.target).offset().top+($(ops.target).height()/2);
    $(ops.trigger).click(function(){
	var $cpic=obj.clone().css({'position':'absolute','z-index':999,'left':_left,'top':_top});
	$(document.body).append($cpic);
	$cpic.animate({top:_ttop,left:_tleft,height:0,width:0,display:'none'},'slow',function(){
		$cpic.remove();
		});
	});
}
