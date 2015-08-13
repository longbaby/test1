// JavaScript Document
/*IE6下拉框遮蔽*/
$.fn.TransF=function(options){
	var settings={
		target:'self'//'self':为当前对象
		}
options&&$.extend(settings,options);
var tFrm="<iframe id='tFrm' style='position:absolute;top:0;left:0;border:0 none;z-index:-1;display:block;' src='scripts/transF.html' scrolling='no'></iframe>";
if($(this).css('position')=='static'){$(this).css('position','relative');}
if(!!!$(this).find('.tFrm').length){
if(settings.target=='self'){
$(this).append(tFrm);
$(this).find('#tFrm').width($(this).innerWidth());
$(this).find('#tFrm').height($(this).innerHeight());
}else{
$(this).find(settings.target).append(tFrm);
$(this).find(settings.target+' #tFrm').width($(this).find(settings.target).innerWidth());
$(this).find(settings.target+' #tFrm').height($(this).find(settings.target).innerHeight());
}
}
}