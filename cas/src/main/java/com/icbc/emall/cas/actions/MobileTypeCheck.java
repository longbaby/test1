package com.icbc.emall.cas.actions;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class MobileTypeCheck extends AbstractAction{

	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		// TODO Auto-generated method stub
		//ParameterMap parameters = context.getRequestParameters();
		String mobileType =  context.getRequestParameters().get("mobileType");
		if("1".equals(mobileType)){
			 return result("phone");			
		}else{
			 return result("pad");
		}

	}

}
