package com.icbc.emall.cas.web.my.login;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.RequestContext;

import com.icbc.common.utils.VerifyCode;
import com.icbc.common.utils.VerifyCode.VerifyCodeInfo;
import com.icbc.emall.Constants;
import com.icbc.emall.Constants.ValidateAuthMessage;
import com.icbc.emall.EmallServiceException;
import com.icbc.emall.SystemMessageUtil;
import com.icbc.emall.auth.model.Auth;
import com.icbc.emall.auth.service.AuthService;
import com.icbc.emall.cas.validate.ICBCCASAuthenticationException;
import com.icbc.emall.cas.validate.UserCredentials;
import com.icbc.emall.common.utils.AppConstant;
import com.icbc.emall.common.utils.Crypt;
import com.icbc.emall.common.utils.Globe;
import com.icbc.emall.common.utils.Globe.LoginChannels;
import com.icbc.emall.merchant.model.MerchantLoginInfo;
import com.icbc.emall.merchant.model.MerchantLoginLogInfo;
import com.icbc.emall.merchant.model.MerchantUserInfo;
import com.icbc.emall.merchant.service.MerchantLoginInfoService;
import com.icbc.emall.merchant.service.MerchantLoginLogInfoService;
import com.icbc.emall.merchant.service.MerchantResetPasswordService;
import com.icbc.emall.merchant.service.MerchantUserInfoService;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;
import com.icbc.merchant.common.ApplicationInfo;

public class MerchantLoginAction implements AuthenticationManager {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Binder that allows additional binding of form object beyond Spring
	 * defaults.
	 */
	private CredentialsBinder credentialsBinder;

	/** Core we delegate to for handling all ticket related tasks. */
	@NotNull
	private CentralAuthenticationService centralAuthenticationService;

	@NotNull
	private CookieGenerator warnCookieGenerator;
	
	@NotNull
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
	
	private AuthService authService;
	private MerchantLoginLogInfoService merchantLoginLogInfoService;
	private SerialGeneratorMgr serialGeneratorMgr;
	private MerchantUserInfoService merchantUserInfoService;
	private MerchantLoginInfoService merchantLoginInfoService;
	private MerchantResetPasswordService merchantResetPasswordService;

	public final void doBind(final RequestContext context,
			final Credentials credentials) throws Exception {
		final HttpServletRequest request = WebUtils
				.getHttpServletRequest(context);

		if (this.credentialsBinder != null
				&& this.credentialsBinder.supports(credentials.getClass())) {
			this.credentialsBinder.bind(request, credentials);
		}

	}

	public final String submit(final RequestContext context,
			final Credentials credentials, final MessageContext messageContext)
			throws Exception {
		try {
			if (authenticateMerchantUsernamePasswordInternal((UsernamePasswordCredentials) credentials)) {
				return "success";
			} else {
				return "error";
			}
		} catch (AuthenticationException e) {
			messageContext.addMessage(new MessageBuilder().error()
					.code(e.getCode()).defaultText(e.getCode()).build());
			RequestAttributes ra = RequestContextHolder
					.currentRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) ra)
					.getRequest();
			request.setAttribute("sub", "2");
			return "error";
		}
	}

	public final String phoneSubmit(final RequestContext context,
			final Credentials credentials, final MessageContext messageContext)
			throws Exception {
		try {
			if (authenticateMerchantPhoneCodeInternal((UsernamePasswordCredentials) credentials)) {

				String createTicketGrantingTicket;
				try {

					createTicketGrantingTicket = this.centralAuthenticationService
							.createTicketGrantingTicket(credentials);
					WebUtils.putTicketGrantingTicketInRequestScope(context,
							createTicketGrantingTicket);
					
					log.info("invoking method phoneSubmit :  createTicketGrantingTicket is :" + createTicketGrantingTicket);
					
				} catch (TicketException e) {
					log.error("createTicketGrantingTicket error: "
							+ e.getMessage());
					return "error";
				}
				//认证通过销毁session信息
				RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
				HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
				request.getSession().invalidate();
				
				log.info("invoking method phoneSubmit :  authentication  successful , createTicketGrantingTicket is :" + createTicketGrantingTicket);
				
				return "success";
			} else {
				
				final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
				
				if(ticketGrantingTicketCookieGenerator.retrieveCookieValue(request)!=null){
					return "success";
				}
				
				return "error";
			
				
			}
		} catch (AuthenticationException e) {
			messageContext.addMessage(new MessageBuilder().error()
					.code(e.getCode()).defaultText(e.getCode()).build());
			RequestAttributes ra = RequestContextHolder
					.currentRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) ra)
					.getRequest();
			
			log.error("invoking method phoneSubmit : authentiaction failed , the error is "  + e.getCode());
			
			request.setAttribute("phonesub", "1");
			return "error";
		}
	}

	/**
	 * 判断商户用户名密码信息
	 * 
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	@SuppressWarnings("unused")
	private boolean authenticateMerchantUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		AuthenticationException unAuthSupportedHandlerException = BadCredentialsAuthenticationException.ERROR;
		UserCredentials userCredentials = (UserCredentials)credentials;
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		
		//用户名密码校验
		String userName = userCredentials.getUsername();
		String password = userCredentials.getPassword();
		HttpSession session = request.getSession(false);
		String verifyCode = userCredentials.getJ_captcha();
		
		// 验证码明文
		VerifyCodeInfo verifycode = (VerifyCodeInfo) request.getSession().getAttribute("verifycode");
		UserCredentials tmp = (UserCredentials) credentials;
		if ( null == tmp ) {
			throw new ICBCCASAuthenticationException("用户数据信息获取失败！请重新登录或联系客服人员。", "");
		}
		password = tmp.getLogonCardPass();
		verifyCode = tmp.getVerifyCode();
		String safePluginType = request.getParameter("SafePluginType");
		int pluginType = 0;
		if(StringUtils.isNotBlank(safePluginType)){
			pluginType = Integer.parseInt(safePluginType);
		}
		verifyCode = Crypt.decodeFromSafePlugin(request.getParameter("randomId"), verifycode.verifyCode,verifyCode,pluginType);
		if(null == verifyCode){
			log.warn("verifyCode is null !");
			throw new ICBCCASAuthenticationException("您输入的验证码有误！", "");
		}
		password = Crypt.decodeFromSafePlugin(request.getParameter("randomId"), verifyCode, password,pluginType);
		if(null == password){
			log.warn("password is null !");
			throw new ICBCCASAuthenticationException("您输入的用户或密码信息有误！", "");
		}
		// 图片验证码校验
		if ( !authenticateMerchantVerifyCodeInternal(verifyCode) ) {
			throw new ICBCCASAuthenticationException("", "验证码错误");
		}
		try {
			//获取认证信息
			Auth auth = authService.getAuthByLoginId(null, null, userName);
			if ( null == auth ) {
				throw new EmallServiceException("您输入的商户用户不存在，请核对后重新输入！", "");
			}
			
			if (StringUtils.equals(AppConstant.IS_ENABLE_NO, auth.getIsEnable())) {
				throw new EmallServiceException("您输入的用户已注销或不可用，请使用其他用户登录。", "");
			}
			
			MerchantUserInfo merchantUserInfo = merchantUserInfoService.getMerchantUserInfoByUserId(ApplicationInfo.APPID, ApplicationInfo.APPKEY, auth.getUserid());
			if ( null == merchantUserInfo ) {
				throw new EmallServiceException("您输入的商户用户信息有误，请核对后重新输入！", "");
			}
			
			if (StringUtils.equals(AppConstant.IS_ENABLE_NO, merchantUserInfo.getStatus())) {
				throw new EmallServiceException("您输入的用户已注销或不可用，请使用其他用户登录。", "");
			}
			
			MerchantLoginInfo merchantLoginInfo = getMerchantLoginInfo(auth.getUserid(), merchantUserInfo.getBelongvendor());

			String validateMessage = authService.validateMerchantAuthLoginInfo(auth, merchantLoginInfo, password);
			if ( !StringUtils.equals(validateMessage, ValidateAuthMessage.VALIDATE_AUTH_MESSAGE_SUCCESS) ) {
				throw new EmallServiceException(validateMessage, "");
			}
			
			session.setAttribute(Constants.MERCHANT_CAS_CONSTANTS.MERCHANT_SESSION_LOGINNAME_FLAG, userName);
			
			//记录登录流水日志
			updateMerchantLoginInfoLog(auth, merchantUserInfo.getBelongvendor());
			
			String mobile = merchantUserInfo.getMobile();
			if(StringUtils.isBlank(mobile)) {
				throw new EmallServiceException("您未填写手机号信息，无法登录验证！", "");
			}
			session.setAttribute(Constants.MERCHANT_CAS_CONSTANTS.MERCHANT_SESSION_MOBILE_FLAG, mobile);
			//手机号吗格式化后展示到页面
			mobile = mobile.substring(0, 3) + "****" + mobile.substring(7, 11);
			//短信验证码输入错误侯数据丢失问题修改
			//request.setAttribute("phoneno", mobile);
			request.getSession().putValue("phoneno",mobile);
			//流程标记
			request.setAttribute("sub", "1");
		} catch (EmallServiceException e) {
			log.error("登录认证失败！", e);
			throw new ICBCCASAuthenticationException(e.getCode(), e.getMessage());
		} catch (Exception e) {
			log.error("用户数据信息有误！", e);
			throw new ICBCCASAuthenticationException("用户数据信息有误！", e.getMessage());
		}
		return true;
	}
	
	private MerchantLoginInfo getMerchantLoginInfo (String userId, String vendorId) throws EmallServiceException {
		MerchantLoginInfo merchantLoginInfo = merchantLoginInfoService.getMerchantLoginInfo(ApplicationInfo.APPID, ApplicationInfo.APPKEY, userId);
		
		if( null == merchantLoginInfo ) {
			merchantLoginInfo = new MerchantLoginInfo();
			merchantLoginInfo.setUserid(userId);
			merchantLoginInfo.setVendorId(vendorId);
			merchantLoginInfoService.saveMerchangLoginInfo(merchantLoginInfo);
		}
		
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		String ip = SystemMessageUtil.getIp(request);
		String deviceBrowser = SystemMessageUtil.getBrowser(request);
		String loginOs = SystemMessageUtil.getOs(request);
		loginOs = loginOs.substring(1, 2);
		String sessionId = request.getSession().getId();
		String clientMac = Crypt.getPlainMac(request.getParameter("currentmac"));
		//更新操作员信息表内容
		merchantLoginInfo.setLastLoginChannels(LoginChannels.MERCHANTINTERNET);
		merchantLoginInfo.setLastLoginTime(new Date());
		merchantLoginInfo.setClientIp(ip);
		merchantLoginInfo.setClientMac(clientMac);
		merchantLoginInfo.setLastLoginDeviceBrowser(deviceBrowser);
		merchantLoginInfo.setLastLoginOs(loginOs);
		merchantLoginInfo.setSessionId(sessionId);
		
		return merchantLoginInfo;
	}
	
	/**
	 * 验证商户短信验证码
	 * 
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticateMerchantPhoneCodeInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		// 获取用户名、短信验证码，并进行比对
		final String username = credentials.getUsername();
		
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo phoneVerifyCode = (VerifyCodeInfo) request.getSession().getAttribute("phoneVerifyCode");
		String phoneValidateCode = request.getParameter("validate");
		
		String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
		log.info("info==========>CAS LOGIN username:" + username);
		log.info("info==========>CAS LOGIN sessionId:" + sessionId);
		log.info("info==========>CAS LOGIN phoneValidateCode:" + phoneValidateCode);
		
		if ( phoneVerifyCode == null || StringUtils.isBlank(phoneVerifyCode.verifyCode)) {
			log.error("Invoking method authenticateMerchantPhoneCodeInternal : phoneVerifyCode.verifyCode is null ");
			throw new ICBCCASAuthenticationException("手机短信验证码有误", "");
		}
		
		if ( !VerifyCode.isVerifycodeValid(phoneVerifyCode, phoneValidateCode) ) {
			log.error("Invoking method authenticateMerchantPhoneCodeInternal : phoneValidateCode check failed ");
			throw new ICBCCASAuthenticationException("手机短信验证码有误，请核对后重新输入", "");
		}
		return true;
	}
	
	private boolean authenticateMerchantVerifyCodeInternal(String verifyCode)throws AuthenticationException {
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo sessionVerifyCode = (VerifyCodeInfo) request.getSession().getAttribute("verifycode");
		
		if ( null == sessionVerifyCode ) {
			log.warn("sessionVerifyCode is null !");
			return false;
		}
		if ( StringUtils.equals(sessionVerifyCode.verifyCode, verifyCode) ) {
			log.debug("verifyCode is " + verifyCode);
			return true;
		}
		return false;
	}
	
	private void updateMerchantLoginInfoLog(Auth auth, String vendorId) {
		try {
			RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
			String ip = SystemMessageUtil.getIp(request);
			String deviceBrowser = SystemMessageUtil.getBrowser(request);
			String loginOs = SystemMessageUtil.getOs(request);
			String sessionId = request.getSession().getId();
			String clientMac = Crypt.getPlainMac(request.getParameter("currentmac"));
			
	    	MerchantLoginLogInfo merchantLoginLogInfo = new MerchantLoginLogInfo();
	    	String logId = serialGeneratorMgr.getSerialKey(com.icbc.emall.util.keygen.Constants.MERCHANT_LOGIN_LOG);
	    	merchantLoginLogInfo.setUserid(auth.getUserid());
	    	merchantLoginLogInfo.setLogId(logId);
	    	merchantLoginLogInfo.setLoginName(auth.getLoginId());
	    	merchantLoginLogInfo.setClientIp(ip);
	    	merchantLoginLogInfo.setClientMac(clientMac);
	    	merchantLoginLogInfo.setLoginDeviceBrowser(deviceBrowser);
	    	merchantLoginLogInfo.setLoginOs(loginOs);
	    	merchantLoginLogInfo.setSessionId(sessionId);
	    	merchantLoginLogInfo.setLastLoginTime(new Date());
	    	merchantLoginLogInfo.setLoginChannels(LoginChannels.MERCHANTINTERNET);
	    	merchantLoginLogInfo.setLoginWay(Globe.LoginWay.MERCHANT);
	        merchantLoginLogInfo.setVendorId(vendorId);
	    	merchantLoginLogInfoService.insert(merchantLoginLogInfo);
		} catch (Exception e) {
			log.error("Got log is error", e);
		}
	}
	
	public CredentialsBinder getCredentialsBinder() {
		return credentialsBinder;
	}

	public void setCredentialsBinder(CredentialsBinder credentialsBinder) {
		this.credentialsBinder = credentialsBinder;
	}

	public CentralAuthenticationService getCentralAuthenticationService() {
		return centralAuthenticationService;
	}

	public void setCentralAuthenticationService(
			CentralAuthenticationService centralAuthenticationService) {
		this.centralAuthenticationService = centralAuthenticationService;
	}

	public CookieGenerator getWarnCookieGenerator() {
		return warnCookieGenerator;
	}

	public void setWarnCookieGenerator(CookieGenerator warnCookieGenerator) {
		this.warnCookieGenerator = warnCookieGenerator;
	}

	@Override
	public Authentication authenticate(Credentials credentials)
			throws AuthenticationException {
		return null;
	}

	public AuthService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}

	public MerchantLoginLogInfoService getMerchantLoginLogInfoService() {
		return merchantLoginLogInfoService;
	}

	public void setMerchantLoginLogInfoService(
			MerchantLoginLogInfoService merchantLoginLogInfoService) {
		this.merchantLoginLogInfoService = merchantLoginLogInfoService;
	}

	public SerialGeneratorMgr getSerialGeneratorMgr() {
		return serialGeneratorMgr;
	}

	public void setSerialGeneratorMgr(SerialGeneratorMgr serialGeneratorMgr) {
		this.serialGeneratorMgr = serialGeneratorMgr;
	}

	public MerchantUserInfoService getMerchantUserInfoService() {
		return merchantUserInfoService;
	}

	public void setMerchantUserInfoService(
			MerchantUserInfoService merchantUserInfoService) {
		this.merchantUserInfoService = merchantUserInfoService;
	}

	public MerchantLoginInfoService getMerchantLoginInfoService() {
		return merchantLoginInfoService;
	}

	public void setMerchantLoginInfoService(
			MerchantLoginInfoService merchantLoginInfoService) {
		this.merchantLoginInfoService = merchantLoginInfoService;
	}

	public MerchantResetPasswordService getMerchantResetPasswordService() {
		return merchantResetPasswordService;
	}

	public void setMerchantResetPasswordService(
			MerchantResetPasswordService merchantResetPasswordService) {
		this.merchantResetPasswordService = merchantResetPasswordService;
	}

	public CookieRetrievingCookieGenerator getTicketGrantingTicketCookieGenerator() {
		return ticketGrantingTicketCookieGenerator;
	}

	public void setTicketGrantingTicketCookieGenerator(
			CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
		this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
	}

}
