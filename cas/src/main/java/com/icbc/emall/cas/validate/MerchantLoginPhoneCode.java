package com.icbc.emall.cas.validate;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.icbc.common.utils.VerifyCode;
import com.icbc.common.utils.VerifyCode.VerifyCodeInfo;

public class MerchantLoginPhoneCode {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public boolean validatePhoneCode(final UsernamePasswordCredentials credentials) {
		// 获取用户名、短信验证码，并进行比对
		final String username = credentials.getUsername();
		String phoneValidateCode = ((PhoneCredentials) credentials).getPhoneValidateCode();

		String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
		log.info("info==========>CAS LOGIN username:" + username);
		log.info("info==========>CAS LOGIN sessionId:" + sessionId);
		log.info("info==========>CAS LOGIN phoneValidateCode:" + phoneValidateCode);

		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo phoneVerifyCode = (VerifyCodeInfo) request.getSession().getAttribute("phoneVerifyCode");

		if (VerifyCode.isVerifycodeValid(phoneVerifyCode, phoneValidateCode)) {
			return true;
		} else {
			return false;
		}
	}
}
