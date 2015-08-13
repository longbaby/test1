package  com.icbc.emall.cas.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import com.icbc.emall.common.utils.CommomProperty;

public class CashierToEbankpController extends AbstractController {
	public final static String EBANK_URL = "cas.eBankUrl";
	
	@NotNull
    private ServicesManager servicesManager;

    /**
     * Boolean to determine if we will redirect to any url provided in the
     * service request parameter.
     */
    private boolean followServiceRedirects;
	
    /** Logout view name. */
    @NotNull
    private String logoutView;
    
    public CashierToEbankpController() {
        setCacheSeconds(0);
    }
    
    @Override
    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {

    	//获取跳转到网银的地址
    	String ebank_url = CommomProperty.getDBManager().getsmsProperty(EBANK_URL);
    	ebank_url = (ebank_url==null? "": ebank_url.trim());
    	//获取登录后要带回给电商的数据
    	String eMallField = request.getParameter("eMallField");
    	eMallField = (eMallField==null? "": eMallField.trim());
    	//设置forwardFlag,1-回电商 forwardFlag,3-回电商，只展示网银登录页
    	String forwardFlag = "3";
        if (this.followServiceRedirects && ebank_url != null) {
//            final RegisteredService rService = this.servicesManager.findServiceBy(new SimpleWebApplicationServiceImpl(ebank_url));
//
//            if (rService != null && rService.isEnabled()) {
//            	RedirectView redirectView = new RedirectView(ebank_url);
//            	redirectView.
//                return new ModelAndView();
//            }
        	//设置传递给网银的参数
        	request.setAttribute("eMallField", eMallField);
        	request.setAttribute("forwardFlag", forwardFlag);
        	request.setAttribute("redirectUrl", ebank_url);
        }
        
        return new ModelAndView(this.logoutView);
    }
    
	 public void setFollowServiceRedirects(final boolean followServiceRedirects) {
	        this.followServiceRedirects = followServiceRedirects;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

	public void setLogoutView(String logoutView) {
		this.logoutView = logoutView;
	}
    
}
