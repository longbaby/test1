$(function(){
	
//include help.html和footer.html

$.get("help.html","html",function(data){
$("#help").html(data);
},'html');
$.get("footer.html","html",function(data){
$("#footer").html(data);
},'html');
	
});