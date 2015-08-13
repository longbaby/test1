package com.icbc.emall.cas.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.icbc.emall.ad.service.AdInfoService;

public class LoginAdInfoHandlerInterceptor implements HandlerInterceptor {

	private static final String LOGIN_AD_IMAGE_PATH = "CAS_LOGIN_AD_IMAGE_PATH";

	private AdInfoService adInfoService;

	public AdInfoService getAdInfoService() {
		return adInfoService;
	}

	public void setAdInfoService(AdInfoService adInfoService) {
		this.adInfoService = adInfoService;
	}

	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		return true;
	}

	/**
	 * 登录页面广告位标识-页面代码
	 */
	private static final String LOGIN_PAGE_ID = "00003";

	/**
	 * 登录页面广告位标识-栏位代码
	 */
	private static final String LOGIN_COLUMN_ID = "00028";

	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
	}

	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
