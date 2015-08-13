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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import com.icbc.finance.pmis.common.CommomProperty;

import cn.com.infosec.icbc.ReturnValue;



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
public final class RealNameVerifyController extends AbstractController {

	@NotNull
    private String logoutView="";

    @NotNull
    private ServicesManager servicesManager;

    /**
     * Boolean to determine if we will redirect to any url provided in the
     * service request parameter.
     */
    private boolean followServiceRedirects;
    
    public RealNameVerifyController() {
        setCacheSeconds(0);
    }

    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
  /*  	String url_ebank = CommomProperty.getDBManager().getsmsProperty("cas.eBankUrl");
    	String emallField = request.getParameter("emallField");
    	logoutView = url_ebank + "?eMallField="+emallField+"&forwardFlag=1";
        return new ModelAndView(new RedirectView(logoutView)); */
    	String url_ebank = CommomProperty.getDBManager().getsmsProperty("cas.eBankUrl");
    	String emallField = request.getParameter("emallField");
    	request.setAttribute("service", url_ebank);
    	request.setAttribute("emallField", emallField);
    	return new ModelAndView(logoutView);
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
    
}
