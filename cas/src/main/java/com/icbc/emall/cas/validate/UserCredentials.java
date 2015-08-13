package com.icbc.emall.cas.validate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

public class UserCredentials extends UsernamePasswordCredentials {
	
	/** The j_captcha. */
    @Size(min=1,message = "required.j_captcha")
    private String j_captcha;

	public String getJ_captcha() {
		return j_captcha;
	}

	public void setJ_captcha(String j_captcha) {
		this.j_captcha = j_captcha;
	}
	//是否启动安全控件
	private String isSafe;
	private String issubmit;
	private String logonCardPass;
	
	private String  verifyCode;

	public String getIsSafe() {
		return isSafe;
	}

	public void setIsSafe(String isSafe) {
		this.isSafe = isSafe;
	}

	public String getLogonCardPass() {
		return logonCardPass;
	}

	public void setLogonCardPass(String logonCardPass) {
		this.logonCardPass = logonCardPass;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}
	private String phoneValidateCode;

	public String getPhoneValidateCode() {
		return phoneValidateCode;
	}

	public void setPhoneValidateCode(String phoneValidateCode) {
		this.phoneValidateCode = phoneValidateCode;
	}

	public String getIssubmit() {
		return issubmit;
	}

	public void setIssubmit(String issubmit) {
		this.issubmit = issubmit;
	}

	
	
	
}
