$(function(){
	
//include help.html和footer.html

$.get("topbar_min.html","html",function(data){
$("#topbar").html(data);
},'html');
$.get("header_min.html","html",function(data){
$("#header").html(data);
},'html');
	
});