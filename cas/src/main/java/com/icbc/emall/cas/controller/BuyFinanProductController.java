package com.icbc.emall.cas.controller;

/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jasig.cas.services.ServicesManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import cn.com.infosec.icbc.ReturnValue;

import com.icbc.emall.EmallServiceException;
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


/**
 * Controller to delete ticket granting ticket cookie in order to log out of
 * single sign on. This controller implements the idea of the ESUP Portail's
 * Logout patch to allow for redirecting to a url on logout. It also exposes a
 * log out link to the view via the WebConstants.LOGOUT constant.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class BuyFinanProductController extends AbstractController {

	@NotNull
    private String logoutView="";

    @NotNull
    private ServicesManager servicesManager;

    @NotNull
    private SerialGeneratorMgr serialGeneratorMgr;
    /**
     * Boolean to determine if we will redirect to any url provided in the
     * service request parameter.
     */
    private boolean followServiceRedirects;
    
    private final static Logger logger = Logger.getLogger(BuyFinanProductController.class);
    
    public BuyFinanProductController() {
        setCacheSeconds(0);
    }

    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
    	try
    	{
	    	logger.debug("into BuyFinanProductController");
	        String service = "";
	        String eMallField = request.getParameter("eMallField");
			eMallField = (eMallField==null?"":eMallField.trim());
			//对于eMallField中的数据进行3DES解密
			logger.debug("eMallField encrypted:"+eMallField);
			eMallField = Crypt.decrypt(eMallField, "UTF-8", 1, 0);
			logger.debug("eMallField decrypted:"+eMallField);
			JSONObject json_eMallField = JSONObject.fromObject(eMallField);
	        String userid = (String)json_eMallField.get("userId");
	        System.out.println("userId:" + userid);
	        //登录方式
	        //登录方式：MALL="0";MOBILEEMALL ="1";EBANK="2";MOBILEEBANK="3";
	        String loginWay = (String)json_eMallField.get("loginWay");
	        MallLoginInfoService mallLoginInfoService = (MallLoginInfoService) SpringContextLoaderListener
					.getSpringWebApplicationContext().getBean(
							"mallLoginInfoService");
	        MallLoginInfo mallLogInfo = new MallLoginInfo();
			mallLogInfo = mallLoginInfoService.findMallLoginInfoByLoginId(userid);
			boolean loginFree = false;
			
			//注入OP名称
	    	String injectTranName = request.getParameter("injectTranName");
	    	logger.debug("injectTranName:"+injectTranName);
	    	String injectTranData = request.getParameter("injectTranData");
	    	logger.debug("injectTranData:"+injectTranData);
	    	String injectSignStr = request.getParameter("injectSignStr");
	    	logger.debug("injectSignStr:"+injectSignStr);
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
	        if(loginFree)//免登录
	        {
	        	logger.debug("loginfree");
				// 注入地址
				service = CommomProperty.getDBManager().getsmsProperty("MALL.EBANKP_FIN_INJECT_URL");
				
				logger.debug("Invoking method BuyFinanProductContorller, requesct this service is "+service);
				
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
					String channelIdentifier = serialGeneratorMgr.getSerialKey(Constants.CHANNEL_IDENTIFIER);;
					json.append("\"channelIdentifier\":\"");
					json.append(channelIdentifier + "\"}");
					logger.debug("logdata before base64:"+json.toString());
					byte[] logDataByte = json.toString().getBytes("GBK");
					byte[] logDataBase64 = ReturnValue.base64enc(logDataByte);
					String logData = new String(logDataBase64, "GBK");
					logger.debug("logData after base64:" + logData);
					/*
					DataShip dataship = new DataShip();
					Map<String, String> paramMap = new HashMap<String, String>();
					paramMap.put(PayClientPreset.INPUT_PARAM_API, "test_api");
					paramMap.put(PayClientPreset.INPUT_PARAM_FORMAT, "json");
					paramMap.put(PayClientPreset.INPUT_PARAM_VERSION, "1.0");
					dataship.setCustomObj(paramMap);
					ACenter paySignClient = CenterFactory.paySignClientCenter();
					dataship.setPrivateObj(json.toString());
					try {
						logDataSignMsg = (String) paySignClient.callMe(dataship);
						logDataSignMsg = URLEncoder.encode(logDataSignMsg, "GBK");
						logger.debug(("logDataEnc:" + logDataSignMsg));
					} catch (SystemLinkerCheckedException e) {
						logger.error("error signing logdata");
						e.printStackTrace();
					}*/
					String keyPath = CommonUtil.SystemlinkerOperationProperty("certSigner.keyLocation");
					logger.info("keyPath:"+keyPath);
					FileBytes fb = FileBytes.instance();
					byte[] bkey=fb.getBytes(keyPath);
					String keyCode = CommonUtil.SystemlinkerOperationProperty("certSigner.password");
					String password = Crypt.decryptStringA(keyCode);
					byte[] signeddata = ReturnValue.sign(logDataByte, logDataByte.length, bkey, password.toCharArray());
					String logDataSignMsg = new String(ReturnValue.base64enc(signeddata),"GBK");
					logDataSignMsg = URLEncoder.encode(logDataSignMsg, "GBK");
					logger.info("logData:" + logData);
					logger.info("logDataSignMsg:" + logDataSignMsg);
					request.setAttribute("logData", logData);
					request.setAttribute("logDataSignMsg", logDataSignMsg);
				}catch(EmallServiceException e){
					//e.printStackTrace();
					logger.error(e.getMessage());
					logger.debug("redirect to ebank login page");
		        	//获取跳转到网银的地址
		        	service = CommomProperty.getDBManager().getsmsProperty("cas.eBankUrl");
		        	
		        	logger.debug("Invoking method BuyFinanProductContorller, requesct this service is "+service);
		        	
	//	        	service = "https://82.201.30.119:11491/icbc/Emall/main/login_mall.jsp";
		        	request.setAttribute("ebankService", service);
		        	String forwardFlag = "2";
		        	request.setAttribute("forwardFlag", forwardFlag);
		        	return new ModelAndView(logoutView);
				}
	        }
	        else
	        {
	        	logger.debug("redirect to ebank login page");
	        	//获取跳转到网银的地址
	        	service = CommomProperty.getDBManager().getsmsProperty("cas.eBankUrl");
	//        	service = "https://82.201.30.119:11491/icbc/Emall/main/login_mall.jsp";
	        	
	        	logger.debug("Invoking method BuyFinanProductContorller, requesct this service is "+service);
	        	
	        	request.setAttribute("ebankService", service);
	        	String forwardFlag = "2";
	        	request.setAttribute("forwardFlag", forwardFlag);
	        }
	        return new ModelAndView(logoutView);
    	}
    	catch(Exception e)
    	{
    		this.logger.error(e.getMessage());
    		return new ModelAndView(logoutView);
    	}
    }

 

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }
    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

	public String getLogoutView() {
		return logoutView;
	}

	public void setLogoutView(String logoutView) {
		this.logoutView = logoutView;
	}
	
	public SerialGeneratorMgr getSerialGeneratorMgr() {
		return serialGeneratorMgr;
	}

	public void setSerialGeneratorMgr(SerialGeneratorMgr serialGeneratorMgr) {
		this.serialGeneratorMgr = serialGeneratorMgr;
	}
	
    
}
