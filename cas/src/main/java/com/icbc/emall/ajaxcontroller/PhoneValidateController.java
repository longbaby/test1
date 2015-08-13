package com.icbc.emall.ajaxcontroller;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.owasp.esapi.reference.DefaultRandomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.icbc.common.utils.ApplicationConfigUtils;
import com.icbc.common.utils.VerifyCode;
import com.icbc.common.utils.VerifyCode.VerifyCodeInfo;
import com.icbc.emall.Constants;
import com.icbc.emall.EmallServiceException;
import com.icbc.emall.common.utils.SpringContextLoaderListener;
import com.icbc.emall.merchant.service.MerchantUserInfoService;
import com.icbc.emall.sendmessage.service.SendMessage;

@Controller
public class PhoneValidateController {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private MerchantUserInfoService merchantUserInfoService;
	//短信轰炸返回结果
	private static final String MESSAGE_RESULTS_TOO_FAST = "-1";
	
	@RequestMapping("/checkPhone.ajax")
	@ResponseBody
	public String checkPhone(){
		return "success";
	}
	
	@RequestMapping("/checkPhoneP.ajax")
	@ResponseBody
	public String checkPhone(@RequestParam(value="userId",required=true) String userId){
		
		System.out.println(userId);
		
		return userId;
	}
	
	@RequestMapping("/sendPhoneCode.ajax")
	@ResponseBody
	public void sendPhoneCode (HttpServletRequest request, HttpServletResponse response) throws EmallServiceException {
		String loginName = (String)request.getSession().getAttribute(Constants.MERCHANT_CAS_CONSTANTS.MERCHANT_SESSION_LOGINNAME_FLAG);
		if(StringUtils.isBlank(loginName)) {
			throw new EmallServiceException("get session info by username is null !");
		}
		String mobile = (String)request.getSession().getAttribute(Constants.MERCHANT_CAS_CONSTANTS.MERCHANT_SESSION_MOBILE_FLAG);
		//生产短信验证码
		VerifyCodeInfo phoneVerifyCode = VerifyCode.generateVerifyCode(0,3 * 60 * 1000);
		log.info("The phoneValidateCode is : " + phoneVerifyCode.verifyCode);
		request.getSession().setAttribute("phoneVerifyCode", phoneVerifyCode);
		
		//发送短信验证码
		String number = DefaultRandomizer.getInstance().getRandomString(6, com.icbc.emall.util.Constants.NUMBER_SEQ.toCharArray());
		SimpleDateFormat sdfStr = new SimpleDateFormat("yyyyMMddHHmmsskkkk");
        String clientTrace = sdfStr.format(new Date());
        clientTrace = clientTrace  + "MerchantLogin"+number.substring(3);
        try{
        	SendMessage sendmessage = (SendMessage) SpringContextLoaderListener.getSpringWebApplicationContext().getBean("SMSSendMessage");
        	Document document = sendmessage.send(mobile,"编号" +number+"，动态密码"+phoneVerifyCode.verifyCode+"。您正在登录工行商城，请勿泄露动态密码。","BUSSTYPE", "PIPGW001",clientTrace, "00200","0260");
        	Element root = document.getRootElement();
        	//返回验证码是否成功操作
        	String result = root.element("result").getText();
	        //防止短信轰炸
        	if( MESSAGE_RESULTS_TOO_FAST.equals(result) ) {
        		result = "error";
        		this.outPutAjax("{\"noId\":\""+number+"\",\"result\":\""+result+"\"}", response);
        	} else {
        		String isDebug = ApplicationConfigUtils.getInstance().getPropertiesValue("IsDebug");
    			if("0".equals(isDebug)){
    				outPutAjax("{\"noId\":\""+number+"\",\"newTempPwd\":\""+phoneVerifyCode.verifyCode+"\",\"result\":\"0\"}", response);
    			}else{
    				outPutAjax("{\"noId\":\""+number+"\",\"result\":\"0\"}", response);
    			}
        	}
        }catch(Exception e){
        	phoneVerifyCode = null;
        	//发送短信失败，将验证码清空
        	request.getSession().setAttribute("phoneVerifyCode", phoneVerifyCode);
        	
        	log.error("短信发送失败。",e);
        	//解决出现异常时response被浏览器缓存的情况
        	response.setContentType("text/xml; charset=UTF-8");
        	response.setHeader("Cache-Control", "no-cache");
        }
	}
	
	/**
	 * 短信轰炸后通过图片验证码验证后生成手机验证码
	 * 
	 */
	@RequestMapping("/getPhoneCodeByVerifyImage.ajax")
	@ResponseBody
	public void getPhoneCodeByVerifyImage(HttpServletRequest request, HttpServletResponse response) throws Exception{
		//校验图片验证码
		String validateCode = request.getParameter("validateCode");
		if(!validatorValidateCode(validateCode)){
			//校验不通过返回验证码页面重新输入
			validateCode = "error";
			outPutAjax("{\"validateCode\":\""+validateCode+"\"}", response);
			return;
		}
		//校验通过 发送短信
		SendMessage sendmessage = (SendMessage) SpringContextLoaderListener.getSpringWebApplicationContext().getBean("SMSSendMessage");
		//生成6位随机短信验证码
		VerifyCodeInfo phoneVerifyCode = VerifyCode.generateVerifyCode(0,3*60*1000);
		log.info("=================>The newTempPwd is:" + phoneVerifyCode.verifyCode);
		
		request.getSession().setAttribute("phoneVerifyCode", phoneVerifyCode);
		String mobile = (String)request.getSession().getAttribute(Constants.MERCHANT_CAS_CONSTANTS.MERCHANT_SESSION_MOBILE_FLAG);
		String number = DefaultRandomizer.getInstance().getRandomString(6, com.icbc.emall.util.Constants.NUMBER_SEQ.toCharArray());
		SimpleDateFormat sdfStr = new SimpleDateFormat("yyyyMMddHHmmsskkkk");
        String clientTrace = sdfStr.format(new Date());
        clientTrace = clientTrace  + "MerchantLogin"+number.substring(3);
        try{
        	Document document = sendmessage.sendNoCheck(mobile,"编号" +number+"，动态密码"+phoneVerifyCode.verifyCode+"。您正在登录工行商城，请勿泄露动态密码。","BUSSTYPE", "PIPGW001",clientTrace, "00200","0260");
        }catch(Exception e){
//        	发送短信失败，将验证码清空
        	phoneVerifyCode = null;
        	request.getSession().setAttribute("phoneVerifyCode", phoneVerifyCode);
        	log.error("短信发送失败。",e);
        }
        String isDebug = ApplicationConfigUtils.getInstance().getPropertiesValue("IsDebug");
		if("0".equals(isDebug)){
			outPutAjax("{\"noId\":\""+number+"\",\"newTempPwd\":\""+phoneVerifyCode.verifyCode+"\",\"result\":\"0\"}", response);
		}else{
			outPutAjax("{\"noId\":\""+number+"\",\"result\":\"0\"}", response);
		}
	}
	
	private boolean validatorValidateCode(String verifyCode)throws AuthenticationException {
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo sessionVerifyCode = (VerifyCodeInfo) request.getSession().getAttribute("verifycode");
		
		if ( null == sessionVerifyCode ) {
			log.warn("sessionVerifyCode is null !");
			return false;
		}
		if ( StringUtils.equals(sessionVerifyCode.verifyCode, verifyCode) ) {
			log.debug("verifyCode is " + verifyCode);
			return true;
		}
		return false;
	}
	
	private void outPutAjax(String jsonStr, HttpServletResponse response) throws Exception{
		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
        PrintWriter  out = null;
        try {
        	out= response.getWriter();
            out.write(jsonStr);
        } catch(Exception e){
           log.error("output jsonStr exception:",e);	
           throw e;
        } finally {
        	if(out!=null){
            out.flush();
            out.close();
        	}
        }
		
	}
	
//	private HttpServletRequest getHttpServletRequest(){
//		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
//		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
//		return request;
//	}
	
	public MerchantUserInfoService getMerchantUserInfoService() {
		return merchantUserInfoService;
	}

	public void setMerchantUserInfoService(
			MerchantUserInfoService merchantUserInfoService) {
		this.merchantUserInfoService = merchantUserInfoService;
	}
	
}
