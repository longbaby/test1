package com.icbc.emall.cas.validate;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

public class PhoneCredentials extends UsernamePasswordCredentials{
	
	/**
	 * 商户手机验证码
	 */
	private String phoneValidateCode;

	public String getPhoneValidateCode() {
		return phoneValidateCode;
	}

	public void setPhoneValidateCode(String phoneValidateCode) {
		this.phoneValidateCode = phoneValidateCode;
	}
	
	
}
