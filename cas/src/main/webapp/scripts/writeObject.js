var _pk="BgIAAACkAABSU0ExAAQAAAEAAQAdutdRBfxSOgF8WdAHVrWuB29etHrxLVFUW+A6L9EvUeFH9ot2TktETwaGmyZ1FUFOvycc1htuRX7zToFBixdjBBZYFkYzApQEQuG1uZ32lPTGC7ntabvP3MpuGV87ry/Yfq++usK+uSGh8wsuZG8XLSdAsgt3sXwXb697q3G57Q==";

function powerConfig(args) {
	var defaults = { "width":150, "height":22, "maxLength":12, "minLength":6, "maskChar":"#", "backColor":"#FFFFFF", "textColor":"#0000FF", "borderColor":"#7F9DB9", "accepts":"\\w{0,}", "caption":"中国民生银行", "captionColor":"#FFFFFF", "captionFont":"", "captionSize":0, "captionBold":"true", "lang":"zh_CN"};
	for (var p in args)
		if (args[p] != null) defaults[p] = args[p];
	return defaults;
}

function writeObject(oid, clsid, cfg) {
	var tempHtml = '';
	tempHtml = '<object id="' + oid + '"  classid="' + clsid + '" width="' + cfg.width + '" height="' + cfg.height
		+ '" style="width:' + cfg.width + 'px;height:' + cfg.height + 'px">';
	
	for (var name in cfg)
		tempHtml = tempHtml + '<param name="' + name + '" value="' + cfg[name] + '">';
	
	tempHtml = tempHtml + '</object>';
	return tempHtml;
}

function getPassObject(oid, cfg) {
	var passObject = '';
	
	if (!oid || typeof(oid) != "string") {
		alert("getPassObject Failed: oid are required!");
	} else {
		passObject = writeObject(oid, "clsid:7FF86CB1-3AB6-4647-90D6-8D0A2C4D3821", powerConfig(cfg));
	}
	
	return passObject;
}

function getEditObject(oid, cfg) {
	var editObject = '';
	
	if (!oid || typeof(oid) != "string") {
		alert("getEditObject Failed: oid are required!");
	} else {
		editObject = writeObject(oid, "clsid:757316A5-CB37-4360-B3C6-F6EEBC54FD82", powerConfig(cfg));
	}
	
	return editObject;
}

function getUtilObject(oid, cfg) {
	var utilObject = '';
	
	if (!oid || typeof(oid) != "string") {
		alert("getUtilObject Failed: oid are required!");
	} else {
		utilObject = writeObject(oid, "clsid:C406B749-2574-4348-A5BF-1C92003516EA", powerConfig(cfg));
	}
	
	return utilObject;
}

function getIBSInput(id, ts, spanId,massage) 
{
    try 
    {
		var powerobj = document.getElementById(id);	
		powerobj.setTimestamp(ts);
		powerobj.publicKeyBlob(_pk);
		var nresult = powerobj.verify();
		if(nresult < 0)
		{			
			return null;
		}	
				
		value = powerobj.getValue();
		if(value=="")
		{
			PEGetElement(spanId).innerHTML= massage+powerobj.lastError(); 
			return null;
		}
		else
		{
			return value;
		}
	}
	catch(e)
	{
		PEGetElement(spanId).innerHTML= massage +e; 
	}
	return null;
}

function getMFMInput(id, ts, spanId,massage) 
{
    try 
    {
		var powerobj = document.getElementById(id);	
		powerobj.setTimestamp(ts);
		powerobj.publicKeyBlob(_pk);
		value = powerobj.getMFM();
		if(value=="")
		{
			PEGetElement(spanId).innerHTML= massage+powerobj.lastError(); 
			return null;
		}
		else
		{
			return value;
		}
	}
	catch(e)
	{
		PEGetElement(spanId).innerHTML= massage +e; 
	}
	return null;
}

function PEGetElement(id)
{
	return  window.document.getElementById(id);
}

