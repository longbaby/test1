$(function(){
	
//include help.html和footer.html

$.get("topbar.html","html",function(data){
$("#topbar").html(data);
},'html');
$.get("header_huodong.html","html",function(data){
$("#header").html(data);
},'html');
	
});