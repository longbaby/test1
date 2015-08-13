package com.icbc.emall.cas.validate;

import org.jasig.cas.authentication.handler.AuthenticationException;

public class ICBCCASAuthenticationException extends AuthenticationException {

	public ICBCCASAuthenticationException(String code, Throwable throwable) {
		super(code, throwable);
		// TODO Auto-generated constructor stub
	}

	public ICBCCASAuthenticationException(String code,String message) {
		super(code,message);
		// TODO Auto-generated constructor stub
	}
}
