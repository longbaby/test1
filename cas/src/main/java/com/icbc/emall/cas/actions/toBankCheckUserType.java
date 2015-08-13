package com.icbc.emall.cas.actions;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import cn.com.infosec.icbc.ReturnValue;

import com.icbc.emall.EmallServiceException;
import com.icbc.emall.appswitch.service.SwitchService;
import com.icbc.emall.common.utils.CommomProperty;
import com.icbc.emall.common.utils.Crypt;
import com.icbc.emall.common.utils.SpringContextLoaderListener;
import com.icbc.emall.mall.model.MallLoginInfo;
import com.icbc.emall.mall.service.MallLoginInfoService;
import com.icbc.emall.member.model.MallUserInfo;
import com.icbc.emall.member.service.MallUserInfoService;
import com.icbc.emall.util.crypt.FileBytes;
import com.icbc.emall.util.keygen.Constants;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;
import com.icbc.systemlinker.common.util.CommonUtil;
public class toBankCheckUserType extends AbstractAction {

	public static final Logger logger = LoggerFactory.getLogger(toBankCheckUserType.class);
	@Autowired
	private MallLoginInfoService mallLoginInfoService;
	@Autowired
	private SerialGeneratorMgr serialGeneratorMgr;
	
	@Autowired
	private SwitchService switchService;
	
	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

    	try
    	{
	    	logger.debug("Invoking method toBankCheckUserType : execute method checkUserType start");
	        String service = "";
	        String eMallField = request.getParameter("eMallField");
			eMallField = (eMallField==null?"":eMallField.trim());
			//对于eMallField中的数据进行3DES解密
			logger.debug("Invoking method toBankCheckUserType : eMallField encrypted:{} ",eMallField);
			eMallField = Crypt.decrypt(eMallField, "UTF-8", 1, 0);
			logger.debug("Invoking method toBankCheckUserType : eMallField decrypted: {}",eMallField);
			JSONObject json_eMallField = JSONObject.fromObject(eMallField);
	        String userid = (String)json_eMallField.get("userId");
	        logger.debug("Invoking method toBankCheckUserType : The userId is {}",userid);
	        //登录方式
	        //登录方式：MALL="0";MOBILEEMALL ="1";EBANK="2";MOBILEEBANK="3";
	        String loginWay = (String)json_eMallField.get("loginWay");
	       
	        MallLoginInfo mallLogInfo = new MallLoginInfo();
			mallLogInfo = mallLoginInfoService.findMallLoginInfoByLoginId(userid);
			boolean loginFree = false;
			
			//注入OP名称
	    	String injectTranName = request.getParameter("injectTranName");
	    	logger.debug("Invoking method toBankCheckUserType : injectTranName:"+injectTranName);
	    	String injectTranData = request.getParameter("injectTranData");
	    	logger.debug("Invoking method toBankCheckUserType : injectTranData:"+injectTranData);
	    	String injectSignStr = request.getParameter("injectSignStr");
	    	logger.debug("Invoking method toBankCheckUserType : injectSignStr:"+injectSignStr);
	    	request.setAttribute("injectTranName", injectTranName);
	    	request.setAttribute("injectTranData", injectTranData);
	    	request.setAttribute("injectSignStr", injectSignStr);
	    	
	        if(loginWay.equals("2"))
	        {
	        	Date date = mallLogInfo.getLastLoginTime();
	        	Date date1 = new Date();
	        	long diff = date1.getTime() - date.getTime();
	        	long validIntervalTime = 0;
				CommomProperty instance = CommomProperty.getDBManager();
				//valid loginfree time interval  default:15minutes
				validIntervalTime = Long.parseLong(instance.getsmsProperty("cas.loginfree.valid.interval")); 
				if(diff<=validIntervalTime)
				{
					loginFree = true;
				}
	        }
	      //loginFree 免登录,直接去网银支付，去登录
	        if(loginFree)
	        {
	        	logger.debug("loginfree");
				// 注入地址
				service = CommomProperty.getDBManager().getsmsProperty("MALL.EBANKP_FIN_INJECT_URL");
				request.setAttribute("ebankService", service);
				MallUserInfoService mallUserInfoService = (MallUserInfoService) SpringContextLoaderListener
						.getSpringWebApplicationContext().getBean(
								"mallUserInfoService");
				MallUserInfo mallUserInfo = null;
				try{
					mallUserInfo = mallUserInfoService.getMallUserById1(userid);
					StringBuffer json = new StringBuffer();
					json.append("{\"mainCIS\":\"");
					json.append(mallUserInfo.getCisCode() + "\",");// {"mainCIS":"mainCIS",
					json.append("\"custmerIp\":\"");
					json.append(mallLogInfo.getClientIp() + "\",");
					Date date = new Date();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS");
					json.append("\"timeStamp\":\"");
					json.append(df.format(date) + "\",");
					//json.append("9999-10-10-10.10.10.111112" + "\",");
					json.append("\"mainAreaCode\":\"");
					json.append(mallUserInfo.getRegisterAreaNumber() + "\",");
					String custmerMac = mallLogInfo.getClientMAC();
					json.append("\"custmerMac\":\"");
					json.append(custmerMac + "\",");
					String custmerRefer = "mall.icbc.com.cn";
					json.append("\"custmerRefer\":\"");
					json.append(custmerRefer + "\",");
					json.append("\"BATCHID\":\"");
					json.append("\",");
					String channelIdentifier = getSerialGeneratorMgr().getSerialKey(Constants.CHANNEL_IDENTIFIER);;
					json.append("\"channelIdentifier\":\"");
					json.append(channelIdentifier + "\"}");
					logger.debug("logdata before base64:"+json.toString());
					byte[] logDataByte = json.toString().getBytes("GBK");
					byte[] logDataBase64 = ReturnValue.base64enc(logDataByte);
					String logData = new String(logDataBase64, "GBK");
					logger.debug("logData after base64:" + logData);
					String keyPath = CommonUtil.SystemlinkerOperationProperty("certSigner.keyLocation");
					logger.info("keyPath:"+keyPath);
					FileBytes fb = FileBytes.instance();
					byte[] bkey=fb.getBytes(keyPath);
					String keyCode = CommonUtil.SystemlinkerOperationProperty("certSigner.password");
					String password = Crypt.decryptStringA(keyCode);
					byte[] signeddata = ReturnValue.sign(logDataByte, logDataByte.length, bkey, password.toCharArray());
					String logDataSignMsg = new String(ReturnValue.base64enc(signeddata),"GBK");
					logDataSignMsg = URLEncoder.encode(logDataSignMsg, "GBK");
					logger.info("Invoking method toBankCheckUserType :logData: {} " , logData);
					logger.info("Invoking method toBankCheckUserType :logDataSignMsg {}", logDataSignMsg);
					
					return result("toEbank");
					
				}catch(EmallServiceException e){
					logger.debug("Invoking method toBankCheckUserType : To ebank login page");
		        	return result("toEbankLogin");
				}
	        }
	        else{
	        	logger.debug("Invoking method toBankCheckUserType : To ebank login page");
	        	return result("toEbankLogin");
	        }
    	}
    	catch(Exception e)
    	{
    		logger.error(e.getMessage());
    		return result("toEbankLogin");
    	}
    
	}


	
	protected String toBankCheckUserType(final RequestContext context){
		return "default";
	}
	
	
	
	public SwitchService getSwitchService() {
		return switchService;
	}

	public void setSwitchService(SwitchService switchService) {
		this.switchService = switchService;
	}


	public MallLoginInfoService getMallLoginInfoService() {
		return mallLoginInfoService;
	}


	public void setMallLoginInfoService(MallLoginInfoService mallLoginInfoService) {
		this.mallLoginInfoService = mallLoginInfoService;
	}


	public SerialGeneratorMgr getSerialGeneratorMgr() {
		return serialGeneratorMgr;
	}


	public void setSerialGeneratorMgr(SerialGeneratorMgr serialGeneratorMgr) {
		this.serialGeneratorMgr = serialGeneratorMgr;
	}

}
