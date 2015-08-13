package com.icbc.emall.cas.web.my.login;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jasig.cas.authentication.principal.Service;
import javax.servlet.http.HttpServletRequest;

import com.icbc.emall.SystemMessageUtil;
import com.icbc.emall.common.utils.CommomProperty;
import com.icbc.emall.common.utils.Globe.LoginChannels;
import com.icbc.emall.common.utils.Globe.LoginWay;
import com.icbc.emall.common.utils.Globe.SceneType;
import com.icbc.emall.mall.model.MallLoginLogInfo;
import com.icbc.emall.mall.service.MallLoginLogInfoService;
import com.icbc.emall.util.BaseServiceData;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;

import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;
import com.icbc.emall.util.keygen.Constants;

public class GenerateResponse {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private MallLoginLogInfoService mallLoginLogInfoService;
	
	private SerialGeneratorMgr serialGeneratorMgr;
	
	public SerialGeneratorMgr getSerialGeneratorMgr() {
		return serialGeneratorMgr;
	}

	public void setSerialGeneratorMgr(SerialGeneratorMgr serialGeneratorMgr) {
		this.serialGeneratorMgr = serialGeneratorMgr;
	}

	public MallLoginLogInfoService getMallLoginLogInfoService() {
		return mallLoginLogInfoService;
	}

	public void setMallLoginLogInfoService(
			MallLoginLogInfoService mallLoginLogInfoService) {
		this.mallLoginLogInfoService = mallLoginLogInfoService;
	}

	public Response getResponse( final RequestContext context,boolean haveError) {
		 	String orgUrl = "";

		 	final Map<String, String> parameters = new HashMap<String, String>();
		 	
		 	CommomProperty instance = CommomProperty.getDBManager();
		 	
		 	this.log.info("Invoking method GenerateResponse.response : base service value:" + BaseServiceData.baseServiceEnable);
		 	this.log.info("Invoking method GenerateResponse.response : errorCode:" + (String)context.getFlowScope().get("errorCode"));		
		 	
		 	//if("1".equals(BaseServiceData.baseServiceEnable))
			{
		 		if("3".equals( (String)context.getFlowScope().get("errorCode")))
		 		{
		 			orgUrl = instance.getsmsProperty("MALL.VIEW.PATH") + CommomProperty.getDBManager().getAddedProperty("MALL.BASIC_SERVICE.REDIRECT_URL");
			 		this.log.info("orgUrl:" + orgUrl);
					context.getFlowScope().put("responseUrl", orgUrl);
			        Response ret =  Response.getRedirectResponse(orgUrl, parameters);
			        log.info("Invoking method GenerateResponse.response : The errorCode is 3");
			        return ret;
		 		}
			}
		 	
	        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
	        String sceneType = (String)context.getFlowScope().get("sceneType");
			sceneType = sceneType==null?"":sceneType.trim();
	        try
			{
				//记录登录流水
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				MallLoginLogInfo loginLogInfo = new MallLoginLogInfo();
		
				String key = serialGeneratorMgr.getSerialKey(Constants.MALL_USER_LOGIN_LOG_INFO_LOGID).trim();
				
				//如果登录出错
				if(haveError)
				{
					key = "E"+key;
				}
				else{
					String nameVerifyFlag = (String) context.getFlowScope().get("nameVerifyFlag");
		        	//如果实名认证失败
		        	if(nameVerifyFlag!=null&&nameVerifyFlag.equals("cisUsed"))
		        		key = "E"+key;
		        	else
		        		key = "S"+key;	
				}
				log.debug("Invoking method GenerateResponse.response : Insert into MallLoginLogInfo data end , The logId is {}",key);
				loginLogInfo.setLogId(key);
				loginLogInfo.setSessionId(request.getSession().getId());
				loginLogInfo.setClientIp(SystemMessageUtil.getIp(request));
				loginLogInfo.setLoginChannels(LoginChannels.INTERNET);
				
				
				if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP) || sceneType.equals(SceneType.LOGIN_BILL_HALL_EBANK) 
						|| sceneType.equals(SceneType.LOGIN_AFTER_RELANAME_VERIFY) || sceneType.equals(SceneType.LOGIN_AFTER_TO_CASHIER)
						|| sceneType.equals(SceneType.LOGIN_LOTTERY)){
						loginLogInfo.setLoginWay(LoginWay.EBANK);
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_REG)){
						loginLogInfo.setLoginWay(LoginWay.MALL);
				}else if(sceneType.equals(SceneType.LOGIN_BILL_HALL_MALL)){
						loginLogInfo.setLoginWay(LoginWay.BILLHALL);
				// 手机商城记录loginWay
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE) || sceneType.equals(SceneType.LOGIN_LOTTERY_MOBILE)
						|| sceneType.equals(SceneType.LOGIN_JIPIAO_MOBILE) || sceneType.equals(SceneType.LOGIN_AFTER_WAPBANK_PHONE)){
						loginLogInfo.setLoginWay(LoginWay.MOBILEEBANK);
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_REG_PHONE)){
						loginLogInfo.setLoginWay(LoginWay.MOBILEEMALL);
				// iPad 
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PAD)){
						loginLogInfo.setLoginWay(LoginWay.PADEBANK);
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_REG_PAD)){
						loginLogInfo.setLoginWay(LoginWay.PAD);
				}
				
				loginLogInfo.setLoginDeviceBrowser(SystemMessageUtil
						.getBrowser(request));
				loginLogInfo.setLoginOs(SystemMessageUtil.getOs(request));
				loginLogInfo.setLastLoginTime((Date) df.parseObject(df.format(new Date())));
				
				String userId = (String)context.getFlowScope().get("userId");
				userId = userId==null?"":userId.trim();
				String loginId = (String)context.getFlowScope().get("loginId");
				loginId = loginId==null?"":loginId.trim();
				loginLogInfo.setUserid(userId);
				loginLogInfo.setLoginName(loginId);
				String custmerMac = (String)context.getFlowScope().get("custmerMac");
				custmerMac = custmerMac==null?"":custmerMac.trim();
				loginLogInfo.setClientMac(custmerMac);
				log.debug("Invoking method GenerateResponse.response : Insert into MallLoginLogInfo data begin , The userId is {}",userId);
				this.mallLoginLogInfoService.addMallLoginInfo(loginLogInfo);
				log.debug("Invoking method GenerateResponse.response : Insert into MallLoginLogInfo data end , The userId is {}",userId);
			}
			catch(Exception e)
			{
				this.log.error("Invoking method GenerateResponse.response : Insert into MallLoginLogInfo data is error ");
			}
	        //登录时如果不为空都认为是手机方式登录
	        Object loginWay = context.getFlowScope().get("loginWay");
	        String casClientUrlFlag=  (loginWay==null?"0":(String)loginWay);//instance.getsmsProperty("cas.client.urlFlag");
	        if(haveError)
	        {
	        	orgUrl =request.getParameter("failpae");
	        	if(orgUrl == null){
	        		//orgUrl =instance.getsmsProperty("cas.mallDefUrl");
	        		//如果出错重定向至错误页
	        		if (SceneType.LOGIN_AFTER_EBANKP_PHONE.equals(sceneType) || 
	        				SceneType.LOGIN_AFTER_REG_PHONE.equals(sceneType) ||
	        				SceneType.LOGIN_LOTTERY_MOBILE.equals(sceneType) ||
	        				SceneType.LOGIN_JIPIAO_MOBILE.equals(sceneType) ||
	        				SceneType.LOGIN_AFTER_EBANKP_PHONE_IM.equals(sceneType)){
	        			
	        			//手机
	        			orgUrl =instance.getsmsProperty("cas.mobile.loginErrorPage");

	        		} else if(SceneType.LOGIN_AFTER_EBANKP_PAD.equals(sceneType) || 
							SceneType.LOGIN_AFTER_REG_PAD.equals(sceneType)){
	        			//iPad
	        			orgUrl =instance.getsmsProperty("cas.pad.loginErrorPage");
	        			
	        		} else if(SceneType.LOGIN_AFTER_WAPBANK_PHONE.equals(sceneType)){
	        			//大学生平台 
	        			orgUrl = instance.getsmsProperty("cas.mobile.wapLoginErrorPage");
	        			
	        		} else {
	        			String b2cReqFlag = (String)context.getFlowScope().get("b2cReqFlag");
	        			//来自b2c成功页
	        			if("1".equals(b2cReqFlag))
	        			{
	        				//首页地址
	        				orgUrl = instance.getsmsProperty("cas.mallDefUrl");
	        			}
	        			else
	        				orgUrl =instance.getsmsProperty("cas.loginErrorPage");
	        		}
	        	}
	        	String error = (String) context.getFlowScope().get("error");
	        	String eMallField = request.getParameter("eMallField"); 
	        	String eMallField1 = (String) context.getFlowScope().get("eMallField");
	        	this.log.info("eMallField:" + eMallField);
	        	this.log.info("eMallField1:" + eMallField1);
	        	// 手机商城查错日志，后续应删除
	        	this.log.info("=======mobile sceneType========:" + sceneType);
	        	this.log.info("=======mobile orgUrl========:" + orgUrl);
	        	String errorCode = (String)context.getFlowScope().get("errorCode");
	        	//如果是黑名单用户
	        	boolean firstArg = true;
	        	if(errorCode!=null)
	        	{
	        		//parameters.put("errorCode", "1");
	        		orgUrl = orgUrl + "?errorCode="+errorCode ;
	        		firstArg = false;
	        	}
	        	//parameters.put("error", error);
	        	//parameters.put("result", "false");
	        	if(firstArg)
	        	{
	        		//parameters.put("eMallField", eMallField);
	        		orgUrl = orgUrl + "?eMallField=" + eMallField;
	        	}
	        	else
	        		orgUrl = orgUrl + "&eMallField=" + eMallField;
	        }else{
	        	String ticket = (String) context.getRequestScope().get("serviceTicketId");
	        	String casClientUrl = "";
	        	
	        	Service service = (Service)context.getFlowScope().get("service");
	        	if(service!=null)
	        		casClientUrl = service.getId();
	        	else
	        		casClientUrl=instance.getsmsProperty("cas.client.url");
	        	/*
	        	if ( "1".equals(casClientUrlFlag)) {
	        		casClientUrl=instance.getsmsProperty("cas.client.mobile.url");
	        	}else{
	        		casClientUrl=instance.getsmsProperty("cas.client.url");
	        	}*/
       		    //orgUrl=ticket==null?"":casClientUrl+"?ticket="+ticket;
	        	//orgUrl=ticket==null?"":casClientUrl.contains("?")?"&ticket":"?ticket="+ticket;
	        	
	        	if(ticket == null)
	        		orgUrl = "";
	        	else
	        	{
	        		if(casClientUrl.contains("?"))
	        		{
	        			orgUrl = casClientUrl + "&ticket=" + ticket;
	        		}
	        		else
	        		{
	        			orgUrl = casClientUrl + "?ticket=" + ticket;
	        		}
	        	}
	        	
       		    if(sceneType.equals(SceneType.LOGIN_AFTER_RELANAME_VERIFY))
       		    {
		        	String nameVerifyFlag = (String) context.getFlowScope().get("nameVerifyFlag");
		        	//如果实名认证失败
		        	if(nameVerifyFlag!=null&&nameVerifyFlag.equals("cisUsed"))
		        		orgUrl = orgUrl + "&nameVerifyFlag=fail";
		        	else
		        		orgUrl = orgUrl + "&nameVerifyFlag=success";
       		    }
	        	//如果网银用户首次登录
	        	String eBankUserFirstLogin = (String) context.getFlowScope().get("eBankUserFirstLogin");
	        	if(eBankUserFirstLogin!=null&&eBankUserFirstLogin.equals("YES"))
	        		orgUrl = orgUrl + "&eBankUserFirstLogin=YES";
	        	parameters.put("result", "true");
	        	if(sceneType.equals(SceneType.LOGIN_BILL_HALL_EBANK))
	        	{
	        		/*
	        		 * 传 dst、sessionid1、sessionid2
	        		 */
	        		String dst = (String) context.getFlowScope().get("dst");
	        		String sessionid1 = (String) context.getFlowScope().get("sessionid1");
	        		String sessionid2 = (String) context.getFlowScope().get("sessionid2");
	        		if(!"".equals(dst))
	        		{
	        			orgUrl = orgUrl + "&dst="+dst;
	        		}
	        		if(!"".equals(sessionid1))
	        		{
	        			orgUrl = orgUrl + "&sid1="+sessionid1;
	        		}
	        		if(!"".equals(sessionid2))
	        		{
	        			orgUrl = orgUrl + "&sid2="+sessionid2;
	        		}
	        	}
	        	
	        	// 大学生平台，手机银行跳转商城自动登录
	        	if(sceneType.equals(SceneType.LOGIN_AFTER_WAPBANK_PHONE)){
	        		String commitData = (String) context.getFlowScope().get("commitData");
	        		String channel = (String) context.getFlowScope().get("channel");
	        		String isShopCar = (String) context.getFlowScope().get("isShopCar");
	        		if(!"".equals(commitData))
	        		{
	        			orgUrl = orgUrl + "&orderProdJson=" + commitData;
	        		}
	        		if(!"".equals(channel))
	        		{
	        			orgUrl = orgUrl + "&channel=" + channel;
	        		}
	        		if(!"".equals(commitData))
	        		{
	        			orgUrl = orgUrl + "&isShopCar=" + isShopCar;
	        		}
	        		this.log.info("=======LOGIN_AFTER_WAPBANK_PHONE orgUrl========: " + orgUrl);
	 	        }
	        	
	        	//网银跳转商城自动登陆，记录ref参数，标识本次访问是从网银而来
	        	if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP) || sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PAD)){
	        		String ref = "mybank.icbc.com.cn";
	        		orgUrl = orgUrl + "&ref=" + ref;
	        		this.log.info("=======LOGIN_AFTER_EBANKP orgUrl========: " + orgUrl);
	 	        }
	        }
	        context.getFlowScope().put("responseUrl", orgUrl);
	        Response ret =  Response.getRedirectResponse(orgUrl, parameters);
	        return ret;
	    }
}