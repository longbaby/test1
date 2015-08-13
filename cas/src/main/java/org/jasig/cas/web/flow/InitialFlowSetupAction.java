package org.jasig.cas.web.flow;                                                                                  
                                                                                                                 
import java.net.URL;                                                                                             
import java.util.List;                                                                                           
                                                                                                                 
import javax.servlet.http.HttpServletRequest;                                                                    
import javax.validation.constraints.NotNull;                                                                     
import javax.validation.constraints.Size;                                                                        
                                                                                                                 
import net.sf.json.JSONObject;                                                                                   
import org.jasig.cas.authentication.principal.Service;                                                           
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;                                   
import org.jasig.cas.util.HttpClient;                                                                            
import org.jasig.cas.web.support.ArgumentExtractor;                                                              
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;                                                
import org.jasig.cas.web.support.WebUtils;                                                                       
import org.slf4j.Logger;                                                                                         
import org.slf4j.LoggerFactory;                                                                                  
import org.springframework.beans.factory.annotation.Autowired;                                                   
import org.springframework.util.StringUtils;                                                                     
import org.springframework.webflow.action.AbstractAction;                                                        
import org.springframework.webflow.execution.Event;                                                              
import org.springframework.webflow.execution.RequestContext;                                                     
import com.icbc.emall.common.utils.Crypt;                                                                        
import com.icbc.emall.Constants;                                                                                 
import com.icbc.emall.cas.web.my.login.SecurityCheckUtils;                                                       
import com.icbc.emall.common.utils.Globe.LoginTarget;                                                            
import com.icbc.emall.system.service.SysParamsService;                                                           
import com.icbc.finance.pmis.common.CommomProperty;                                                              
                                                                                                                 
public final class InitialFlowSetupAction extends AbstractAction {                                               
                                                                                                                 
	private Logger log = LoggerFactory.getLogger(this.getClass());                                                 
    /** CookieGenerator for the Warnings. */                                                                     
    @NotNull                                                                                                     
    private CookieRetrievingCookieGenerator warnCookieGenerator;                                                 
                                                                                                                 
    /** CookieGenerator for the TicketGrantingTickets. */                                                        
    @NotNull                                                                                                     
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;                                 
                                                                                                                 
    /** Extractors for finding the service. */                                                                   
    @NotNull                                                                                                     
    @Size(min=1)                                                                                                 
    private List<ArgumentExtractor> argumentExtractors;                                                          
                                                                                                                 
    /** Boolean to note whether we've set the values on the generators or not. */                                
    public boolean pathPopulated = false;                                                                        
    private String eMallFieldEncryped="";                                                                        
    @Autowired                                                                                                   
	private SysParamsService sysParamsService;                                                                     
    public static final String B2B_MERCHANT_LOGIN_VISIT_SWITCH_ON = "on";                                        
                                                                                                                 
    protected Event doExecute(final RequestContext context) throws Exception {                                   
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);                              
        if (!this.pathPopulated) {                                                                               
            final String contextPath = context.getExternalContext().getContextPath();                            
            final String cookiePath = StringUtils.hasText(contextPath) ? contextPath + "/" : "/";                
            logger.info("Setting path for cookies to: "                                                          
                + cookiePath);                                                                                   
            this.warnCookieGenerator.setCookiePath(cookiePath);                                                  
            this.ticketGrantingTicketCookieGenerator.setCookiePath(cookiePath);                                  
            this.pathPopulated = true;                                                                           
        }                                                                                                        
                                                                                                                 
        //isb2b是否来自b2b判断                                                                                   
        String isb2b = (request.getParameter("isb2b") == null) ? "" : request.getParameter("isb2b");             
        String natureIsb2b = isb2b;                                                                              
        String loginTo = (request.getParameter("to") == null) ? "" : request.getParameter("to");                 
        //b2b商户中心开关                                                                                        
        String value = sysParamsService.getValueByInnerName(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH);          
        if(!org.apache.commons.lang3.StringUtils.endsWith(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH_ON, value)) {
        	isb2b = "";                                                                                            
        }                                                                                                        
        context.getFlowScope().put(                                                                              
            "ticketGrantingTicketId", this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request));    
        context.getFlowScope().put(                                                                              
            "warnCookieValue",                                                                                   
            Boolean.valueOf(this.warnCookieGenerator.retrieveCookieValue(request)));                             
                                                                                                                 
        Service service = WebUtils.getService(this.argumentExtractors,                                           
            context);                                                                                            
                                                                                                                 
        if (service != null && logger.isDebugEnabled()) {                                                        
            logger.debug("Placing service in FlowScope: " + service.getId());                                    
        }                                                                                                        
        CommomProperty instance = CommomProperty.getDBManager();                                                 
        if(service == null ){ //没有service参数                                                                  
    		eMallFieldEncryped = request.getParameter("eMallField");                                                 
    		eMallFieldEncryped = (eMallFieldEncryped==null?"":eMallFieldEncryped.trim());                            
    		log.info("INFO-->InitialFlowSetupAction==>doExecute:eMallFieldEncryped:"+eMallFieldEncryped);            
    		String eMallField = null;                                                                                
    		//对于eMallField中的数据进行3DES解密                                                                     
    		try {                                                                                                    
    			eMallField = Crypt.decrypt(eMallFieldEncryped, "UTF-8", 1, 0);                                         
    		} catch (Exception e1) {                                                                                 
    			log.error("ERROR:InitialFlowSetupAction==>doExecute:",e1);                                             
    			context.getFlowScope().put("error",                                                                    
    					"eMallField error");                                                                               
    			context.getFlowScope().put("eMallField", eMallFieldEncryped);                                          
    			return result("submit");                                                                               
    		}                                                                                                        
    		                                                                                                         
    		JSONObject json_eMallField = null;                                                                       
    		log.info("INFO-->InitialFlowSetupAction==>doExecute:eMallField:"+eMallField);                            
    		String resiveService = null;                                                                             
    		if (eMallField != null && !"".equals(eMallField)) {//有emallField                                        
    			json_eMallField = JSONObject.fromObject(eMallField);                                                   
    			if(json_eMallField != null ){                                                                          
    				if(json_eMallField.get("service")!=null)                                                             
    				{                                                                                                    
    					resiveService =  (String)json_eMallField.get("service");                                           
    					if(resiveService != null && !"".equals(resiveService.trim() )){                                    
	    	    			Service rService  = this.createServiceFrom(resiveService, null);                               
	    	    			context.getFlowScope().put("service", rService);                                               
    					}                                                                                                  
    				}                                                                                                    
    				else //emallfield里没有service                                                                       
    				{                                                                                                    
    					String casClientUrl=instance.getsmsProperty("cas.client.url");                                     
    					if("1".equals(isb2b)){  //如果是来自b2b的登录                                                      
    						casClientUrl = getB2BRedirectUrl(service, loginTo, instance);                                    
    					}else if("1".equals(natureIsb2b) ){ //来自b2b的登录，但是启用b2b开关为off                          
    						casClientUrl = getB2CRedirectUrl(service, loginTo, instance);                                    
    					}                                                                                                  
    					                                                                                                   
    	    			Service service1 = this.createServiceFrom(casClientUrl, null);                                   
    	    			context.getFlowScope().put("service", service1);                                                 
    				}                                                                                                    
    			}                                                                                                      
    		}else{//没有emallField                                                                                   
    			//CommomProperty instance = CommomProperty.getDBManager();                                             
            	String casClientUrl=instance.getsmsProperty("cas.client.url");                                     
            	if("1".equals(isb2b)){  //如果是来自b2b的登录                                                      
					casClientUrl = getB2BRedirectUrl(service, loginTo, instance);                                          
				}else if("1".equals(natureIsb2b) ){ //来自b2b的登录，但是启用b2b开关为off                                
					casClientUrl = getB2CRedirectUrl(service, loginTo, instance);                                          
				}                                                                                                        
            	                                                                                                   
    			Service service1 = this.createServiceFrom(casClientUrl, null);                                         
    			context.getFlowScope().put("service", service1);                                                       
    		}                                                                                                        
        }else{                                                                                                   
        	/*                                                                                                     
        	context.getFlowScope().put("service", service);                                                        
        	*/                                                                                                     
        	//防止钓鱼                                                                                             
        	try{                                                                                                   
            	String host = new URL(service.getId()).getHost().toLowerCase();                                    
            	String allowableHost = instance.getsmsProperty("allowableHost");                                   
            	String casClientUrl=null;                                                                          
            	//覆盖原来的service                                                                                
            	 if("1".equals(natureIsb2b) && !"1".equals(isb2b) ){ //来自b2b的登录，但是启用b2b开关为off         
						casClientUrl = getB2CRedirectUrl(service, loginTo, instance);                                        
						service = this.createServiceFrom(casClientUrl, null);                                                
					}                                                                                                      
            	                                                                                                   
            	if(allowableHost != null && host != null){                                                         
            		String[] doamins = allowableHost.split(";");                                                     
            		boolean flag = false;                                                                            
            		for(int i=0;i<doamins.length;i++){                                                               
        	        	if(host.endsWith(doamins[i])){                                                               
        	        		context.getFlowScope().put("service", service);                                            
        	        		flag = true;                                                                               
        	        		break;                                                                                     
        	        	}                                                                                            
            		}                                                                                                
            		if(!flag){                                                                                       
        	        		log.error("ERROR:InitialFlowSetupAction-->doExecute==>service= "+service);                 
        	        		casClientUrl=instance.getsmsProperty("cas.client.url");                                    
        	            	if("1".equals(isb2b)){  //如果是来自b2b的登录                                            
        						casClientUrl = getB2BRedirectUrl(service, loginTo, instance);                                
        					}else if("1".equals(natureIsb2b) ){ //来自b2b的登录，但是启用b2b开关为off                      
        						casClientUrl = getB2CRedirectUrl(service, loginTo, instance);                                
        					}                                                                                              
        	            	                                                                                         
        	    			Service service1 = this.createServiceFrom(casClientUrl, null);                               
        	    			context.getFlowScope().put("service", service1);                                             
            		}                                                                                                
            	}else{                                                                                             
            		log.error("ERROR:InitialFlowSetupAction-->doExecute==>allowableHost or host is null");           
	            	casClientUrl=instance.getsmsProperty("cas.client.url");                                          
	            	if("1".equals(isb2b)){  //如果是来自b2b的登录                                                    
						casClientUrl = getB2BRedirectUrl(service, loginTo, instance);                                        
					}else if("1".equals(natureIsb2b) ){ //来自b2b的登录，但是启用b2b开关为off                              
						casClientUrl = getB2CRedirectUrl(service, loginTo, instance);                                        
					}                                                                                                      
	            	                                                                                                 
	            	                                                                                                 
	            	                                                                                                 
	            	if(host != null && host.endsWith("icbc.com.cn")){                                                
	            		context.getFlowScope().put("service", service);                                                
	            	}else{                                                                                           
		    			Service service1 = this.createServiceFrom(casClientUrl, null);                                     
		    			context.getFlowScope().put("service", service1);                                                   
	            	}                                                                                                
            	}                                                                                                  
                                                                                                                 
        	}catch(Exception e){                                                                                   
        		log.error("ERROR:InitialFlowSetupAction-->doExecute==>",e);                                          
            	String casClientUrl=instance.getsmsProperty("cas.client.url");                                     
            	if("1".equals(isb2b)){  //如果是来自b2b的登录                                                      
					casClientUrl = getB2BRedirectUrl(service, loginTo, instance);                                          
				}else if("1".equals(natureIsb2b) ){ //来自b2b的登录，但是启用b2b开关为off                                
					casClientUrl = getB2CRedirectUrl(service, loginTo, instance);                                          
				}                                                                                                        
            	                                                                                                   
    			Service service1 = this.createServiceFrom(casClientUrl, null);                                         
    			context.getFlowScope().put("service", service1);                                                       
        	}                                                                                                      
        }                                                                                                        
        return result("success");                                                                                
    }                                                                                                            
                                                                                                                 
    public void setTicketGrantingTicketCookieGenerator(                                                          
        final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {                             
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;                          
    }                                                                                                            
                                                                                                                 
    public void setWarnCookieGenerator(final CookieRetrievingCookieGenerator warnCookieGenerator) {              
        this.warnCookieGenerator = warnCookieGenerator;                                                          
    }                                                                                                            
                                                                                                                 
    public void setArgumentExtractors(                                                                           
        final List<ArgumentExtractor> argumentExtractors) {                                                      
        this.argumentExtractors = argumentExtractors;                                                            
    }                                                                                                            
    public  SimpleWebApplicationServiceImpl createServiceFrom(                                                   
            String service, final HttpClient httpClient) {                                                       
            final String targetService =service;                                                                 
            final String serviceToUse = StringUtils.hasText(targetService)                                       
                ? targetService :service;                                                                        
                                                                                                                 
            if (!StringUtils.hasText(serviceToUse)) {                                                            
                return null;                                                                                     
            }                                                                                                    
            final String id = cleanupUrl(serviceToUse);                                                          
            return new SimpleWebApplicationServiceImpl(id, httpClient);                                          
        }                                                                                                        
    protected static String cleanupUrl(final String url) {                                                       
        if (url == null) {                                                                                       
            return null;                                                                                         
        }                                                                                                        
                                                                                                                 
        final int jsessionPosition = url.indexOf(";jsession");                                                   
                                                                                                                 
        if (jsessionPosition == -1) {                                                                            
            return url;                                                                                          
        }                                                                                                        
                                                                                                                 
        final int questionMarkPosition = url.indexOf("?");                                                       
                                                                                                                 
        if (questionMarkPosition < jsessionPosition) {                                                           
            return url.substring(0, url.indexOf(";jsession"));                                                   
        }                                                                                                        
                                                                                                                 
        return url.substring(0, jsessionPosition)                                                                
            + url.substring(questionMarkPosition);                                                               
    }                                                                                                            
                                                                                                                 
    /**                                                                                                          
     * 判断b2b登录后跳转页面                                                                                     
     * @param casClientUrl                                                                                       
     * @param loginTo                                                                                            
     * @param instance                                                                                           
     * @return                                                                                                   
     */                                                                                                          
    private String getB2BRedirectUrl(Service service, String loginTo, CommomProperty instance) {                 
    	if(service == null){                                                                                       
	    	String casClientUrl = instance.getsmsProperty("b2b.cas.client.url");                                     
	    	if(LoginTarget.MERCHANT_PERSION.equals(loginTo)) {  //如果是商户登录，返回b2b商户中心                    
				casClientUrl = instance.getsmsProperty("b2b.cas.client.vendor.url");                                     
			}                                                                                                          
	    	return casClientUrl;                                                                                     
    	} else {                                                                                                   
    		return service.getId();                                                                                  
    		                                                                                                         
    	}                                                                                                          
    }                                                                                                            
                                                                                                                 
    /**                                                                                                          
     * 判断b2c登录后跳转页面                                                                                     
     * @param casClientUrl                                                                                       
     * @param loginTo                                                                                            
     * @param instance                                                                                           
     * @return                                                                                                   
     */                                                                                                          
    private String getB2CRedirectUrl(Service service, String loginTo, CommomProperty instance) {                 
    	                                                                                                           
	    	String casClientUrl = instance.getsmsProperty("cas.client.url");                                         
	    	if(LoginTarget.MERCHANT_PERSION.equals(loginTo)) {  //如果是商户登录，返回b2b商户中心                    
				casClientUrl = instance.getsmsProperty("cas.merchant.client.url");                                       
			}                                                                                                          
	    	return casClientUrl;                                                                                     
    	                                                                                                           
    }                                                                                                            
}                                                                                                                