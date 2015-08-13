package com.icbc.emall.cas.actions;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.icbc.common.utils.VerifyCode.VerifyCodeInfo;
import com.icbc.emall.cas.validate.PhoneCredentials;

public class MerchantPhoneLoginCheck extends AbstractAction{

	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		//获取验证码
//		String validateCode =  context.getRequestParameters().get("validateCode");
		//获取发送验证码和生成验证码比对，比对成功返回
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		
		String pageCode = request.getParameter("phoneValidateCode");
		if (pageCode == null || "".equals(pageCode)) {
			return result("error");
		}
		VerifyCodeInfo phoneVerifyCode = (VerifyCodeInfo) request.getSession().getAttribute("phoneVerifyCode");
		
		if (pageCode.equals(phoneVerifyCode)) {
			return result("success");
		}
		return result("error");
	}
	
	public void getPhoneCode(Credentials credentials) {
		PhoneCredentials pc = (PhoneCredentials)credentials;
		pc.getPhoneValidateCode();
		pc.getLoginWay();
	}

}
