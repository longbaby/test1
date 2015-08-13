var _arc = _arc || [];
jQuery(function(){
    /*var pvURL='http://82.200.30.232:8080/js/tongji.js';//"${pvUrl}?version=$!{staticResourceVersion}";
    jQuery.getScriptForIcbc(pvURL,function(){
  		_arc.push(['cust_id','']);
		_arc.push(['seaval', '']);
		_arc.push(['mchid', '']);
		_arc.push(['plevl1', '']);
		_arc.push(['plevl2', '']);
		_arc.push(['plevl3', '']);
		_arc.push(['chlid', '']);
		_arc.push(['prdname', '']);
		_arc.push(['prdid', '']);
		_arc.push(['prdcatid1', '']);
		_arc.push(['prdcatid2', '']);
		_arc.push(['prdcatid3', '']);
		_arc.push(['pageId', '']);
		_arc.push(['columnId', '']);
		_arc.push(['columnCatId', '']);
		_arc.push(['sessionId','']);
		_arc.push(['moduleId', '']);
		_arc.push(['vendorid', '']);
		ar_main();
	},true);
	*/
	
	
	_arc.push(['cust_id','']);
		_arc.push(['seaval', '']);
		_arc.push(['mchid', '']);
		_arc.push(['plevl1', '']);
		_arc.push(['plevl2', '']);
		_arc.push(['plevl3', '']);
		_arc.push(['chlid', '']);
		_arc.push(['prdname', '']);
		_arc.push(['prdid', '']);
		_arc.push(['prdcatid1', '']);
		_arc.push(['prdcatid2', '']);
		_arc.push(['prdcatid3', '']);
		_arc.push(['pageId', '']);
		_arc.push(['columnId', '']);
		_arc.push(['columnCatId', '']);
		_arc.push(['sessionId','']);
		_arc.push(['moduleId', '']);
		_arc.push(['vendorid', '']);
		ar_main();
	
});
(function($){
	jQuery.getScriptForIcbc = function(url, callback, cache) {
   	 	jQuery.ajax({type: 'GET', url: url, success: callback, dataType: 'script', ifModified: true, cache: cache});
	};
})(jQuery);


/**获取被统计网页传递参数*/
function ar_get_clientInfo(key){
	var _arc = window._arc || [];
	for(i=0;i<_arc.length;i++) {
		if (_arc[i][0] == key) {
			return _arc[i][1];
		}
	}
	return "";
}

/**获取被统计网页传递参数*/
function ar_join_clientInfo(){
	var u = "";
	var _arc = window._arc || [];
	for(i=0;i<_arc.length;i++) {
		u += "&" + _arc[i][0] + "=" + _arc[i][1];
	}
	return u;
}
 
/**函数可对字符串进行编码，这样就可以在所有的计算机上读取该字符串。*/
function ar_encode(str)
{
	var e = "", i = 0;

	for(i=0;i<str.length;i++) {
		if(str.charCodeAt(i)>=0&&str.charCodeAt(i)<=255) {
			e = e + escape(str.charAt(i));
		}
		else {
			e = e + str.charAt(i);
		}
	}

	return e;
}


/**屏幕分辨率*/
function ar_get_screen()
{
	var c = "";

	if (self.screen) {
		c = screen.width+"x"+screen.height;
	}

	return c;
}

/**颜色质量*/
function ar_get_color()
{
	var c = ""; 

	if (self.screen) {
		c = screen.colorDepth+"-bit";
	}

	return c;
}

/**返回当前的浏览器语言*/
function ar_get_language()
{
	var l = "";
	var n = navigator;

	if (n.language) {
		l = n.language.toLowerCase();
	}
	else
	if (n.browserLanguage) {
		l = n.browserLanguage.toLowerCase();
	}

	return l;
}

/**返回浏览器类型IE,Firefox*/
function ar_get_agent()
{
	var a = "";
	var n = navigator;

	if (n.userAgent) {
		a = n.userAgent;
	}

	return a;
}

/**方法可返回一个布尔值，该值指示浏览器是否支持并启用了Java*/
function ar_get_jvm_enabled()
{
	var j = "";
	var n = navigator;

	j = n.javaEnabled() ? 1 : 0;

	return j;
}

/**返回浏览器是否支持(启用)cookie */
function ar_get_cookie_enabled()
{
	var c = "";
	var n = navigator;
	c = n.cookieEnabled ? 1 : 0;

	return c;
}

/**检测浏览器是否支持Flash或有Flash插件*/
function ar_get_flash_ver()
{
	var f="",n=navigator;

	if (n.plugins && n.plugins.length) {
		for (var ii=0;ii<n.plugins.length;ii++) {
			if (n.plugins[ii].name.indexOf('Shockwave Flash')!=-1) {
				f=n.plugins[ii].description.split('Shockwave Flash ')[1];
				break;
			}
		}
	}
	else
	if (window.ActiveXObject) {
		for (var ii=10;ii>=2;ii--) {
			try {
				var fl=eval("new ActiveXObject('ShockwaveFlash.ShockwaveFlash."+ii+"');");
				if (fl) {
					f=ii + '.0';
					break;
				}
			}
			 catch(e) {}
		}
	}
	return f;
} 

/**浏览器的名称信息*/
function ar_get_app()
{
	var a = "";
	var n = navigator;

	if (n.appName) {
		a = n.appName;
	}
	return a; 
}
 
/**匹配顶级域名*/
function ar_c_ctry_top_domain(str)
{
	var pattern = "/^aero$|^cat$|^coop$|^int$|^museum$|^pro$|^travel$|^xxx$|^com$|^net$|^gov$|^org$|^mil$|^edu$|^biz$|^info$|^name$|^ac$|^mil$|^co$|^ed$|^gv$|^nt$|^bj$|^hz$|^sh$|^tj$|^cq$|^he$|^nm$|^ln$|^jl$|^hl$|^js$|^zj$|^ah$|^hb$|^hn$|^gd$|^gx$|^hi$|^sc$|^gz$|^yn$|^xz$|^sn$|^gs$|^qh$|^nx$|^xj$|^tw$|^hk$|^mo$|^fj$|^ha$|^jx$|^sd$|^sx$/i";

	if(str.match(pattern)){ return 1; }

	return 0;
}

/**匹配域名*/
function ar_c_ctry_domain(str)
{
	var pattern = "/^ac$|^ad$|^ae$|^af$|^ag$|^ai$|^al$|^am$|^an$|^ao$|^aq$|^ar$|^as$|^at$|^au$|^aw$|^az$|^ba$|^bb$|^bd$|^be$|^bf$|^bg$|^bh$|^bi$|^bj$|^bm$|^bo$|^br$|^bs$|^bt$|^bv$|^bw$|^by$|^bz$|^ca$|^cc$|^cd$|^cf$|^cg$|^ch$|^ci$|^ck$|^cl$|^cm$|^cn$|^co$|^cr$|^cs$|^cu$|^cv$|^cx$|^cy$|^cz$|^de$|^dj$|^dk$|^dm$|^do$|^dz$|^ec$|^ee$|^eg$|^eh$|^er$|^es$|^et$|^eu$|^fi$|^fj$|^fk$|^fm$|^fo$|^fr$|^ly$|^hk$|^hm$|^hn$|^hr$|^ht$|^hu$|^id$|^ie$|^il$|^im$|^in$|^io$|^ir$|^is$|^it$|^je$|^jm$|^jo$|^jp$|^ke$|^kg$|^kh$|^ki$|^km$|^kn$|^kp$|^kr$|^kw$|^ky$|^kz$|^la$|^lb$|^lc$|^li$|^lk$|^lr$|^ls$|^lt$|^lu$|^lv$|^ly$|^ga$|^gb$|^gd$|^ge$|^gf$|^gg$|^gh$|^gi$|^gl$|^gm$|^gn$|^gp$|^gq$|^gr$|^gs$|^gt$|^gu$|^gw$|^gy$|^ma$|^mc$|^md$|^mg$|^mh$|^mk$|^ml$|^mm$|^mn$|^mo$|^mp$|^mq$|^mr$|^ms$|^mt$|^mu$|^mv$|^mw$|^mx$|^my$|^mz$|^na$|^nc$|^ne$|^nf$|^ng$|^ni$|^nl$|^no$|^np$|^nr$|^nu$|^nz$|^om$|^re$|^ro$|^ru$|^rw$|^pa$|^pe$|^pf$|^pg$|^ph$|^pk$|^pl$|^pm$|^pr$|^ps$|^pt$|^pw$|^py$|^qa$|^wf$|^ws$|^sa$|^sb$|^sc$|^sd$|^se$|^sg$|^sh$|^si$|^sj$|^sk$|^sl$|^sm$|^sn$|^so$|^sr$|^st$|^su$|^sv$|^sy$|^sz$|^tc$|^td$|^tf$|^th$|^tg$|^tj$|^tk$|^tm$|^tn$|^to$|^tp$|^tr$|^tt$|^tv$|^tw$|^tz$|^ua$|^ug$|^uk$|^um$|^us$|^uy$|^uz$|^va$|^vc$|^ve$|^vg$|^vi$|^vn$|^vu$|^ye$|^yt$|^yu$|^za$|^zm$|^zr$|^zw$/i";

	if(str.match(pattern)){ return 1; }

	return 0;
}

/**处理域名地址*/
function ar_get_domain(host)
{
	var d=host.replace(/^www\./, "");

	var ss=d.split(".");
	var l=ss.length;

	if(l == 3){
		if(ar_c_ctry_top_domain(ss[1]) && ar_c_ctry_domain(ss[2])){
		}
		else{
			d = ss[1]+"."+ss[2];
		}
	}
	else if(l >= 3){

		var ip_pat = "^[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*$";

		if(host.match(ip_pat)){
			return d;
		}

		if(ar_c_ctry_top_domain(ss[l-2]) && ar_c_ctry_domain(ss[l-1])) {
			d = ss[l-3]+"."+ss[l-2]+"."+ss[l-1];
		}
		else{
			d = ss[l-2]+"."+ss[l-1];
		}
	}
		
	return d;
}

/**返回cookie信息*/
function ar_get_cookie(name)
{
	var mn=name+"=";
	var b,e;
	var co=document.cookie;

	if (mn=="=") {
		return co;
	}

	b=co.indexOf(mn);

	if (b < 0) {
		return "";
	}

	e=co.indexOf(";", b+name.length);

	if (e < 0) {
		return co.substring(b+name.length + 1);
	}
	else {
		return co.substring(b+name.length + 1, e);
	}
}

/**设置cookie信息*/
function ar_set_cookie(name, val, cotp) 
{ 
	var date=new Date; 
	var year=date.getFullYear(); 
	var hour=date.getHours(); 

	var cookie="";

	if (cotp == 0) { 
		cookie=name+"="+val+";"; 
	} 
	else if (cotp == 1) { 
		year=year+10; 
		date.setYear(year); 
		cookie=name+"="+val+";expires="+date.toGMTString()+";"; 
	} 
	else if (cotp == 2) { 
		hour=hour+1; 
		date.setHours(hour); 
		cookie=name+"="+val+";expires="+date.toGMTString()+";"; 
	} 

	var d=ar_get_domain(document.domain);
	if(d != ""){
		cookie +="domain="+d+";";
	}
	cookie +="path="+"/;";

	document.cookie=cookie;
}

/**字符串逆转*/
function str_reverse(str) {
	var ln = str.length;
	var i=0;
	var temp="";
	for(i=ln-1; i>-1; i--) {
		if(str.charAt(i)==".")
			temp += "#";
		else
			temp += str.charAt(i);
	}

	return temp;
}

function ar_get_ss_id(str)
{
	len=str.indexOf("_");
	str=str.substring(len+1);
	len=str.indexOf("_");
	str=str.substring(len+1);
	return str;
}

function ar_get_ss_no(str) {
	len=str.indexOf("_");
	str=str.substring(0,len);
	return parseInt(str);
}

/**返回客户端时间*/
function ar_get_stm() 
{ 
	var date = new Date(); 
	var yy=date.getFullYear(); 
	var mm=date.getMonth(); 
	var dd=date.getDate(); 
	var hh=date.getHours(); 
	var ii=date.getMinutes(); 
	var ss=date.getSeconds(); 
	var i; 
	var tm=0; 
	for(i = 1970; i < yy; i++) { 
		if ((i % 4 == 0 && i % 100 != 0) || (i % 100 == 0 && i % 400 == 0)) { 
			tm=tm+31622400; 
		} 
		else { 
			tm=tm+31536000; 
		} 
	}
	mm=mm+1;
	
	for(i = 1; i < mm; i++) { 
		if (i == 1 || i == 3 || i == 5 || i == 7 || i == 8 || i == 10 || i == 12) { 
			tm=tm+2678400; 
		} 
		else { 
			if (i == 2) { 
				if ((yy % 4 == 0 && yy % 100 != 0) || (yy % 100 == 0 && yy % 400 == 0)) { 
					tm=tm+2505600; 
				} 
				else { 
					tm=tm+2419200; 
				} 
			} 
		 	else { 
				tm=tm+2592000; 
			} 
		} 
	}
	
	tm = tm +  (dd-1) * 86400; tm = tm +  hh * 3600; 
	tm = tm +  ii * 60; 
	tm = tm +  ss; 
	return tm; 
} 


function ar_get_ctm(str) {
	len=str.indexOf("_");
	str=str.substring(len+1);
	len=str.indexOf("_");
	str=str.substring(0,len);
	return parseInt(str, 10); 
}

/**返回指定个数的随机数字串*/
function ar_get_random(n) {
	var str = "";
	for (var i = 0; i < n; i ++) {
		str += String(parseInt(Math.random() * 10));
	}
	return str;
}

/* main function */
function ar_main() {
	
	var unit_id     = "9999";//网站id
	//var dest_path   = "http://82.200.30.232:8080/images/blank.gif?unit_id="+unit_id; 
	var dest_path   = ctx+"/scripts/casjs/blank.gif?unit_id="+unit_id; 
	var expire_time = 1800; 
	var i;

	var host=document.location.host;
	var domain = ar_get_domain(host.toLocaleLowerCase());
	var hashval = 0;
	for (i=0; i< domain.length; i++){
		hashval += domain.charCodeAt(i);
	}

	var uv_str = ar_get_cookie("ar_stat_uv");
	var uv_id = "";
	var uv_new = 0;
	if (uv_str == ""){
		uv_new = 1;

		uv_id = ar_get_random(20);

		var value = uv_id+"|"+unit_id;
		
		ar_set_cookie("ar_stat_uv", value, 1);
	}
	else{
		var arr = uv_str.split("|");
		uv_id  = arr[0];
		var uids_str = arr[1];
		var uids = uids_str.split("@");
		var uid_num = uids.length;

		var bingo = 0;
		for(var pos=0,max=uids.length;pos<max;pos++) {
			var uid = uids[pos];
			if (uid == unit_id){
				bingo = 1;
				break;
			}
		}

		if (bingo == 0){
			uv_new = 1;

			if (uid_num >= 100){
				var value = uv_id+"|"+unit_id;
			}
			else{
				var value = uv_str+"@"+unit_id;
			}	
			
			ar_set_cookie("ar_stat_uv", value, 1);
		}
	}

	var ss_str = ar_get_cookie("ar_stat_ss"); 
	var ss_id = "";  //随即数
	var ss_no = 0;   //session有效期内访问页面的次数
	if (ss_str == ""){
		ss_no = 0;
		ss_id = ar_get_random(10);
		value = ss_id+"_"+"0_"+ar_get_stm()+"_"+unit_id;
		ar_set_cookie("ar_stat_ss", value, 0); 
	} 
	else { 
		var arr = ss_str.split("|");
		var ss_num = arr.length;

		var bingo = 0;
		for(var pos=0,max=arr.length;pos<max;pos++) {
			var ss_info = arr[pos];
			var items = ss_info.split("_");

			var cookie_ss_id  = items[0];
			var cookie_ss_no  = parseInt(items[1]);
			var cookie_ss_stm = items[2];
			var cookie_ss_uid = items[3];

			if (cookie_ss_uid == unit_id){
				bingo = 1;

				if (ar_get_stm() - cookie_ss_stm > expire_time) { 
					ss_no = 0;
					ss_id = ar_get_random(10);
				} 
				else{
					ss_no = cookie_ss_no + 1;
					ss_id = cookie_ss_id;
				}

				value = ss_id+"_"+ss_no+"_"+ar_get_stm()+"_"+unit_id;

				arr[pos] = value;
				ss_str = arr.join("|");
				ar_set_cookie("ar_stat_ss", ss_str, 0); 

				break;
			}
		}

		if (bingo == 0)
		{
			ss_no = 0;
			ss_id = ar_get_random(10);
			value = ss_id+"_"+"0_"+ar_get_stm()+"_"+unit_id;

			if (ss_num >= 20){
				pos = parseInt(Math.random() * ss_num);
			}
			else{
				pos = ss_num;
			}

			arr[pos] = value;
			ss_str = arr.join("|");
			ar_set_cookie("ar_stat_ss", ss_str, 0); 
		}
	}
  
    //返回导航到当前网页的超链接所在网页的URL
	var ref = document.referrer; 
	ref = ar_encode(String(ref)); 

	var url = document.URL; 
	url = ar_encode(String(url)); 
	
	var urlname = document.URL.substring(document.URL.lastIndexOf("/")+1);
	urlname = ar_encode(String(urlname)); 

	var title = document.title;
	title = escape(String(title)); 

	var charset = document.charset;
	charset = ar_encode(String(charset)); 

	var screen = ar_get_screen(); 
	screen = ar_encode(String(screen)); 

	var color =ar_get_color(); 
	color =ar_encode(String(color)); 

	var language = ar_get_language(); 
	language = ar_encode(String(language));
 
	var agent =ar_get_agent(); 
	agent =ar_encode(String(agent));

	var jvm_enabled =ar_get_jvm_enabled(); 
	jvm_enabled =ar_encode(String(jvm_enabled)); 

	var cookie_enabled =ar_get_cookie_enabled(); 
	cookie_enabled =ar_encode(String(cookie_enabled)); 

	var flash_ver = ar_get_flash_ver();
	flash_ver = ar_encode(String(flash_ver)); 

	var stat_uv = ss_id+"_"+ss_no+"_"+ar_get_stm();

	dest=dest_path+"&url="+url+"&urlname="+urlname+"&title="+title+"&chset="+charset+"&scr="+screen+"&col="+color+"&lg="+language+"&je="+jvm_enabled+"&ec="+cookie_enabled+"&fv="+flash_ver+"&cnv="+String(Math.random())+"&ref="+ref+"&uagent="+agent+"&stat_ss="+uv_id+"&stat_uv="+stat_uv;
    dest+=ar_join_clientInfo();
  
	 var dom=$("<img src=\""+dest+"\" border=\"0\" width=\"1\" height=\"1\" />");
   document.body.appendChild(dom.get(0));
    
}

