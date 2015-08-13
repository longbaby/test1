/**
* 功能：为了网银兼容多浏览器，在此文件中封装判断浏览器类型，部分js hack的公共js方法
* 
*/

function BrowserCompatible(){
}


/**
*获取浏览器类型
*@return 浏览器类型：MSIE;Safari;Firefox
*/
BrowserCompatible.prototype.getBrowserType = function(){
	var ua = navigator.userAgent;
	if (ua.indexOf("MSIE")>-1) {
		return 'MSIE';
	}else if (ua.indexOf("Chrome")>-1) {
		return 'Chrome';
	}else if (ua.indexOf("Safari")>-1) {
		return 'Safari';
	}else if (ua.indexOf("Firefox")>-1) {
		return 'Firefox';
	}
}

/**
*判断是否是IE
*@return true/false
*/
BrowserCompatible.prototype.isIE = function(){
	if(this.getBrowserType()==='MSIE') return true;
	else return false;
}
/**
*判断是否是Chrome
*@return true/false
*/
BrowserCompatible.prototype.isChrome = function(){
	if(this.getBrowserType()==='Chrome') return true;
	else return false;
}
/**
*判断是否是Safari
*@return true/false
*/
BrowserCompatible.prototype.isSafari = function(){
	if(this.getBrowserType()==='Safari') return true;
	else return false;
}
/**
*判断是否是Firefox
*@return true/false
*/
BrowserCompatible.prototype.isFirefox = function(){
	if(this.getBrowserType()==='Firefox') return true;
	else return false;
}
/**
*获取keycode
*@return true/false
*/
BrowserCompatible.prototype.getKeycode = function(e){
	if(this.isIE()) {
		return window.event.keyCode;
	}else if(this.isFirefox() || this.isChrome()) {
		var keyCode = window.event?e.keyCode:e.which;
		//ctrl按键过滤,tab键过滤，返回按back键处理
		if(e.ctrlKey == true || keyCode==0){
		keyCode = 8;}
		return keyCode;
	}else {return e.which;}
}
/**
*阻止默认事件
*@return true/false
*/
BrowserCompatible.prototype.eventPreventDefault = function(e){
	if(this.isIE()) {
		 window.event.returnValue = false;
	}else {
		 e.preventDefault();
	}
}
/**
*阻止事件冒泡
*@return true/false
*/
BrowserCompatible.prototype.eventStopPropagation = function(e){
	if(this.isIE()) {
		 window.event.cancelBubble = true;
	}else {
		 e.stopPropagation();
	}
}
/**
*阻止事件冒泡
*@return true/false
*/
BrowserCompatible.prototype.eventFromElement = function(e){
	if(this.isIE()) {
		 window.event.cancelBubble = true;
	}else {
		 e.preventDefault();
	}
}
/**
*event获取事件源
*@return 事件源对象
*/
BrowserCompatible.prototype.getEventFromElement = function(e){
	if(this.isIE()) {
		 return window.event.fromElement;
	}else {
		 return e.relatedTarget;
	}
}
/**
*event获取事件源
*@return 事件源对象
*/
BrowserCompatible.prototype.getEventSrcElement = function(e){
	if(this.isIE()) {
		 return window.event.srcElement;
	}else{
		 return e.target;
	}
}
/**
*获取元素的innerText
*@return 元素的innerText的值
*/
BrowserCompatible.prototype.getDomInnerText = function(dom){
	if(this.isIE()) {
		 return dom.innerText;
	}else {
		 return dom.textContent;
	}
}
/**
*获取浏览器版本号
*@return version
*/
BrowserCompatible.prototype.getBrowserVersion = function(dom){
	var ua = navigator.userAgent;
	var version;
	if(this.isSafari()) {
		var index = ua.indexOf("Version");
		version = ua.substr(index+8,5);
	}
	return version;
}

/**
*存sessionId到localStorage中，登陆页面中做签退操作
*/
BrowserCompatible.prototype.setContentToLocalStorage = function(sid){
		try{window.localStorage.setItem("sid",sid);}catch(exception){}
}
/**
*清除localStorage中保存的sessionid
*/
BrowserCompatible.prototype.clearLocalStorage = function(){
		try{window.localStorage.removeItem("sid");}catch(exception){}
}
/**
*获取localStorage中保存的sessionid
*/
BrowserCompatible.prototype.getLocalStorage = function(){
		try{return window.localStorage.getItem("sid");}catch(exception){}
}
var pebankBrowserCompatible = new BrowserCompatible();
//禁止所有浏览器input和textarea的拖入功能
try{
	if(pebankBrowserCompatible.isIE()){
		window.attachEvent("onload",function(){
			try{
				var _inputs = document.getElementsByTagName("input");
				var textareas = document.body.getElementsByTagName("TEXTAREA");		
				for(var _inputi=0;_inputi<_inputs.length;_inputi++){
				    if(_inputs[_inputi].type.toLowerCase() == 'text' && _inputs[_inputi].name.indexOf("-Suggest")==-1){
				    	_inputs[_inputi].ondrop = function(){return false;}
				    	_inputs[_inputi].ondragenter = function(){return false;}
				    	_inputs[_inputi].ondragover = function(){return false;}
				    }
				}
				for(var i=0;i<textareas.length;i++){
					textareas[i].ondrop = function(){return false;}
					textareas[i].ondragenter = function(){return false;}
					textareas[i].ondragover = function(){return false;}
				}
			}catch(e){}
		},false); 
	}else{
		window.addEventListener("load",function(){
			try{
				var _inputs = document.getElementsByTagName("input");
				var textareas = document.body.getElementsByTagName("TEXTAREA");		
				for(var _inputi=0;_inputi<_inputs.length;_inputi++){
				    if(_inputs[_inputi].type.toLowerCase() == 'text' && _inputs[_inputi].name.indexOf("-Suggest")==-1){
				    	_inputs[_inputi].ondrop = function(){return false;}
				    	_inputs[_inputi].ondragenter = function(){return false;}
				    	_inputs[_inputi].ondragover = function(){return false;}
				    }
				}
				for(var i=0;i<textareas.length;i++){
					textareas[i].ondrop = function(){return false;}
					textareas[i].ondragenter = function(){return false;}
					textareas[i].ondragover = function(){return false;}
				}
			}catch(e){}
		},false); 
	}
}catch(e_drag){}




/**
 * iframe中弹出新窗口时，新窗口中调用window.opener.focus()无法使opener获得焦点
 * 必须使用window.opener.top.focus();
 */
if(!pebankBrowserCompatible.isIE()){
	document.addEventListener("DOMContentLoaded",function(){
		 var beforeWindowFocus=window.focus;
		 window.focus=function(){
			 	if(window.top!=window){
			 		window.top.focus();
			 	}else{
			 		beforeWindowFocus();	
			 	}
		 	}
	},false);
}

/*
 * firefox控件相关方法
 * */
//比较控件版本
var pluginInputVersion;
var pluginSubmitVersion;
var pluginFullScreenVersion;
var pluginClientBindingVersion;
var pluginCLCacheVersion;
var pluginInputVersion_Store;
function cmpVersion(s, d, deli)
{
	var s1 = s.split(deli);
	var d1 = d.split(deli);
		for(var i = 0; i < s1.length; i++ )
		{
			if(s1[i] > d1[i])return 1;
			else if (s1[i] < d1[i])return -1;
		}
		return 0;
}
//首次下载版本,如果新控件还没安装则轮询
	function checkPlugin() {
	navigator.plugins.refresh(false);
    // 这里的npapidemo Plugin即plugin的name属性
    if (pulginHasInstalled()) {
      window.location.reload();
    } else {
      setTimeout("checkPlugin()", 1000);
    }
  }
//更新版本,如果新控件还没安装则轮询
function checkPluginUpdate() {
	  navigator.plugins.refresh(false);
	  if(checkVersionIsOk()){
		  window.location.reload();
	  }else{
	      setTimeout("checkPluginUpdate()", 1000);		  
	  }
  }

//查看插件是否安装全
function pulginHasInstalled() {
	navigator.plugins.refresh(false);
    // 这里的npapidemo Plugin即plugin的name属性
	var pluginsFlag;
	if(pebankBrowserCompatible.isFirefox()){
		pluginsFlag =  
		typeof(navigator.mimeTypes['application/x-icbcnpxxin-plugin-input'])!="undefined" &&
    	typeof(navigator.mimeTypes['application/x-icbc-plugin-submit'])!="undefined";
	}else if(pebankBrowserCompatible.isChrome()){
		pluginsFlag = 
		typeof(navigator.mimeTypes['application/x-icbc-plugin-chrome-npxxin-input'])!="undefined" &&
    	typeof(navigator.mimeTypes['application/x-icbc-plugin-chrome-npsubmit'])!="undefined";
	}
    if (pluginsFlag) {return true;}
    return false;
  }
//对比版本号
function checkVersionIsOk() {
	navigator.plugins.refresh(false);
	var des;
	  try{
		  if(pebankBrowserCompatible.isFirefox()){
			  des = navigator.mimeTypes['application/x-icbcnpxxin-plugin-input'].enabledPlugin['description'].split("_");
		  }else if(pebankBrowserCompatible.isChrome()){
			  des = navigator.mimeTypes['application/x-icbc-plugin-chrome-npxxin-input'].enabledPlugin['description'].split("_");
		  }
	      if (typeof(des[3]) == "undefined" || cmpVersion(des[3], pluginInputVersion, ".") != 0){	//一致后刷新页面，即能用最新的插件
	    	  return false;
	      }
	  }catch(exception){return false;}
	  try{
		  if(pebankBrowserCompatible.isFirefox()){
			  des = navigator.mimeTypes['application/x-icbc-plugin-submit'].enabledPlugin['description'].split("_");
		  }else if(pebankBrowserCompatible.isChrome()){
			  des = navigator.mimeTypes['application/x-icbc-plugin-chrome-npsubmit'].enabledPlugin['description'].split("_");
		  }
		  if (typeof(des[3]) == "undefined" || cmpVersion(des[3], pluginSubmitVersion, ".") != 0){	//一致后刷新页面，即能用最新的插件
			  return false;
		  }
	  }catch(exception){return false;}
	  
	return true;
  }

function storePulginHasInstalled() {
	navigator.plugins.refresh(false);
    // �����npapidemo Plugin��plugin��name����
	var pluginsFlag;
	pluginsFlag = typeof(navigator.mimeTypes['application/x-icbc-plugin-chrome-npxxin-store'])!="undefined";
    if (pluginsFlag) {return true;}
    return false;
  }
//�ԱȰ汾��
function storeCheckVersionIsOk() {
	navigator.plugins.refresh(false);
	var des;
	  try{
		  des = navigator.mimeTypes['application/x-icbc-plugin-chrome-npxxin-store'].enabledPlugin['description'].split("_");
	      if (typeof(des[4]) == "undefined" || cmpVersion(des[4], pluginInputVersion_Store, ".") != 0){	//һ�º�ˢ��ҳ�棬���������µĲ��
	    	  return false;
	      }
	  }catch(exception){return false;}
	return true;
  }
//控件使用
function getHTMLTextValue(text){
	var regx = /<INPUT TYPE\=hidden NAME\='\w+' VALUE\='(.*?)'>/;
	var result = text.match(regx);
	if(result != null){
		return result[1];
	}
	return null;
}
function getSafeEditValue(text){
	var ua = navigator.userAgent;
	if (ua.indexOf("Chrome")>-1) {
		return text;
	}else{
		return getHTMLTextValue(text);
	}
	
}
function compareVersionPlugin(inputnames,bttinputnames){
	var inputArray1=inputnames.split('.');
	var inputArray2=bttinputnames.split('.');
	var inputvalue1,inputvalue2;
	for(i=0;i<inputArray1.length;i++){
		inputvalue1=inputArray1[i];
		inputvalue2=inputArray2[i];
		if(parseInt(inputvalue1,10) < parseInt(inputvalue2,10)){
			return true;
		}else if(parseInt(inputvalue1,10) > parseInt(inputvalue2,10)){
			return false;
		}
	}
	return false;
}