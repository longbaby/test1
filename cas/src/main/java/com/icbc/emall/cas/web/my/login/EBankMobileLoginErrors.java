package com.icbc.emall.cas.web.my.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icbc.emall.common.utils.CommomProperty;


public class EBankMobileLoginErrors extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory
			.getLogger(EBankMobileLoginErrors.class);
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String errorCode = request.getParameter("TranErrorCode");
		String isMobileLogin = request.getParameter("isMobileLogin");
		if(null != errorCode && !"".equals(errorCode)){
			errorCode = errorCode.trim();
		}else {
			errorCode = "0";
		}
		logger.debug("网银登录失败后的错误码errorCode:"+errorCode);
		CommomProperty instance = CommomProperty.getDBManager();
		String casClientMobileUrl = "";
		String to = "";
		String casServerLogin = instance.getAddedProperty("cas.server.login");
		//isMobileLogin 1为手机，2为pad
		if("1".equals(isMobileLogin)){
			casClientMobileUrl = instance.getsmsProperty("cas.client.mobile.url");
			to="";
		}else{
			casServerLogin = casServerLogin.replaceFirst("phonelogin", "login");
			casClientMobileUrl = instance.getsmsProperty("cas.client.pad.url");
			to="5";
		}
		StringBuffer redirectUrl = new StringBuffer();
		
//		2680:用户密码错误
//		3401:用户名不存在
//		96111945:验证码错误或超时
//		7956:错误次数已超过最大次数
//		1:必须修改网银登录密码
//		2:必须修改网银预留信息
		redirectUrl.append(casServerLogin).
					append("?service=").
					append(casClientMobileUrl).
					append("&errorCode=").
					append(errorCode).
					append("&to=").
					append(to);
		logger.debug("网银登录失败后跳转的url是errorRedirectUrl:"+redirectUrl.toString());
		response.sendRedirect(redirectUrl.toString());
		
//		response.sendRedirect("http://82.201.61.54:8010/phonelogin?service=http://82.201.61.54:8180/j_spring_cas_security_check&errorCode=er");
	}

}
