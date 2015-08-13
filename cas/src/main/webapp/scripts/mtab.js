/**
 *jQuery选项卡插件（Tab）
 *@param tabNameClass：选项卡频道class名
 *@param tabConClass:选项卡频道内容class名
 *@param tabNameCur:当前选项卡频道class名
 *@param eventMod:绑定事件类型
 *@param execFn:切换执行的自定义函数
 */
$.fn.MTab=function(options){
	var settings={
		tabNameClass:'tabname',
		tabConClass:'tabcon',
		tabNameCur:'current',
		eventMod:'click',
		tabItemElmt:'li',
		execFn:null
	 }
	options&&$.extend(settings,options);
	var _this=$(this).selector;
	var _tabName=_this+'> .'+settings.tabNameClass;
	var _tabCon=_this+' .'+settings.tabConClass;
	var idx=$(_tabName+' .'+settings.tabNameCur).index(_tabName+' '+settings.tabItemElmt);
	$(_tabCon).eq(idx).show();
	$(_tabName+' '+settings.tabItemElmt).bind(settings.eventMod,function(){
	var idx=$(this).index(_tabName+' '+settings.tabItemElmt);
	$(_tabName+' '+settings.tabItemElmt).removeClass(settings.tabNameCur);	
	$(this).addClass(settings.tabNameCur);	
	$(_tabCon).stop().hide();
	$(_tabCon).eq(idx).stop().show();	
	if(settings.execFn)settings.execFn(idx);
	});
}