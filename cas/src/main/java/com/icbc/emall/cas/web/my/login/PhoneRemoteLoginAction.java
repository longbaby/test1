package com.icbc.emall.cas.web.my.login;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import net.sf.json.JSONObject;

import org.hibernate.validator.constraints.NotEmpty;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.util.HttpClient;
import org.jasig.cas.web.flow.InitialFlowSetupAction;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.icbc.emall.EmallServiceException;
import com.icbc.emall.SystemMessageUtil;
import com.icbc.emall.auth.model.Auth;
import com.icbc.emall.auth.service.AuthService;
import com.icbc.emall.bp.service.BpCommonService;
import com.icbc.emall.cache.CacheManager;
import com.icbc.emall.common.model.Address;
import com.icbc.emall.common.service.AddressService;
import com.icbc.emall.common.utils.Crypt;
import com.icbc.emall.common.utils.Globe.LoginChannels;
import com.icbc.emall.common.utils.Globe.LoginWay;
import com.icbc.emall.common.utils.Globe.MallUserType;
import com.icbc.emall.common.utils.Globe.RegWay;
import com.icbc.emall.common.utils.Globe.SceneType;
import com.icbc.emall.common.utils.Globe.YesOrNo;
import com.icbc.emall.ebankuseraddr.model.EBankUserAddr;
import com.icbc.emall.ebankuseraddr.service.EbankUserAddrService;
import com.icbc.emall.mall.model.MallLoginInfo;
import com.icbc.emall.mall.service.MallLoginInfoService;
import com.icbc.emall.member.model.MallUserInfo;
import com.icbc.emall.member.service.EBankSavedUserService;
import com.icbc.emall.member.service.MallUserInfoService;
import com.icbc.emall.merchant.dao.AreacodeMapDAO;
import com.icbc.emall.merchant.model.AreacodeMap;
import com.icbc.emall.util.keygen.Constants;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;
import com.icbc.finance.pmis.common.CommomProperty;

/**
 * 身份认证服务器跳转回商城服务器
 * 所使用的场景，都登录成功后，通过CAS生成登录信息，且需要走到login-phone-other-webflow.xml时，会经过此类，场景包含
 * 1、以网银身份登录商城成功后跳转回商城
 * 2、注册成功后点击去商城逛逛，以注册信息登录成功后跳转回商城(此时会带有userid，用以告诉CAS，以哪个用户身份登录)
 * 3、在我的商城点击实名认证成功后跳转回商城(此时会带有userid，用以告诉CAS,如果需要补录网银信息，需要往哪个用户信息中实录)
 * 4、未登录时，点击立即购买或去结点时，先跳转到网银登录成功后跳转回商城
 * @author kfzx-buzc
 *
 */
public class PhoneRemoteLoginAction extends AbstractAction {
	/** CookieGenerator for the Warnings. */
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@NotNull
	private CookieRetrievingCookieGenerator warnCookieGenerator;

	/** Extractors for finding the service. */
	@NotEmpty
	private List<ArgumentExtractor> argumentExtractors;

	/** Core we delegate to for handling all ticket related tasks. */
	@NotNull
	private CentralAuthenticationService centralAuthenticationService;

	@NotNull
	private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

	private InitialFlowSetupAction initialFlowSetupAction;

	private AuthService authService;
	private MallUserInfoService mallUserInfoService;
	private EBankSavedUserService ebankSavedUserService;
	private SerialGeneratorMgr serialGeneratorMgr;
	private MallLoginInfoService mallLoginInfoService;
	private BpCommonService bpCommonService;
	private AddressService addressService;
	private EbankUserAddrService eBankUserAddrService;
	private AreacodeMapDAO areacodeMapDAO;
	private static HashMap hashMap;
	
	static
	{
		hashMap = new HashMap();
		hashMap.put("澳门", "澳门辖区");
		hashMap.put("北京", "北京市");
		hashMap.put("台湾", "台湾辖区");
		hashMap.put("天津", "天津市");
		hashMap.put("铜仁", "铜仁市");
		hashMap.put("香港", "香港辖区");
		hashMap.put("襄樊", "襄阳市");
		hashMap.put("重庆", "重庆市");
	}
	
	protected Event doExecute(final RequestContext context) {
		//手机登录
		context.getFlowScope().put("loginWay","1");
		
		log.debug(" PhoneRemoteLoginAction----------"+new Date()+"----------------start");
		String eMallFieldEncryped=null;
		final HttpServletRequest request = WebUtils
				.getHttpServletRequest(context);
		eMallFieldEncryped = request.getParameter("eMallField");
		eMallFieldEncryped = (eMallFieldEncryped==null?"":eMallFieldEncryped.trim());
		String eMallField = null;
		//对于eMallField中的数据进行3DES解密
		try {
			eMallField = Crypt.decrypt(eMallFieldEncryped, "UTF-8", 1, 0);
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error("eMallField error: " + eMallFieldEncryped);
			context.getFlowScope().put("error",
					"eMallField error");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}
		SecurityCheckUtils securityUtil = new SecurityCheckUtils();
		String jesion = securityUtil.getTranData(request);
		JSONObject json = null;
		if (jesion != null && !"".equals(jesion)) {
			json = JSONObject.fromObject(jesion);
		} else {
			log.error(" json error: " + json);
			context.getFlowScope().put("error",
					"json is null");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}
		
		
		String channelIdentifier = (String) json.get("channelIdentifier");

		UsernamePasswordCredentials user = new UsernamePasswordCredentials();
		context.getFlowScope().put(
				"warnCookieValue",
				Boolean.valueOf(this.warnCookieGenerator
						.retrieveCookieValue(request)));
		
		
		
		JSONObject json_eMallField = null;
		String sceneType = "";
		if(eMallField.equals("")){
			//通过手机网银登录页面直接进入购物中心或在手机网银里点击去商城逛逛，此场景将不会携带eMallField，做特殊处理
			sceneType = SceneType.LOGIN_AFTER_EBANKP_PHONE;
		}else{
			json_eMallField = JSONObject.fromObject(eMallField);
			//获取eMallField中的sceneType
			sceneType = (String)json_eMallField.get("sceneType");
		}
		log.debug("=============sceneType====="+sceneType);
		if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE)){
			//1、以手机网银身份登录商城成功后跳转回商城,且还包含通过网银登录页面直接进入购物中心或在网银里点击去商城逛逛
			scene1(context,json,user,eMallFieldEncryped);
		}else if(sceneType.equals(SceneType.LOGIN_AFTER_REG_PHONE)){
			//2、在手机客户端注册成功后点击去商城逛逛，以注册信息登录成功后跳转回商城(此时会带有userid，用以告诉CAS，以哪个用户身份登录)
			scene2(context,json,user,eMallFieldEncryped);
		}else if(sceneType.equals(SceneType.LOGIN_AFTER_RELANAME_VERIFY)){
			//3、在我的商城点击实名认证成功后跳转回商城(此时会带有userid，用以告诉CAS,如果需要补录网银信息，需要往哪个用户信息中实录)
			scene3(context,json,json_eMallField,user,eMallFieldEncryped);
		}else if(sceneType.equals(SceneType.LOGIN_AFTER_TO_CASHIER)){
			//4、未登录时，点击立即购买或去结点时，先跳转到网银登录成功后跳转回商城
			scene4(context,json,user,eMallFieldEncryped);
		}else{
			//非法，做非法处理////////////////////////////////////////////////////////////
			log.error("sceneType error: " + sceneType);
			context.getFlowScope().put("error",
					"sceneType error");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}
		if(user.getUsername() ==null || "".equals(user.getUsername().trim())){
			log.error("user error: " + user.getUsername());
			context.getFlowScope().put("error",
					"user is null");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}
		Credentials credentials = user;
		if (!this.initialFlowSetupAction.pathPopulated) {
			final String contextPath = request.getContextPath();
			final String cookiePath = StringUtils.hasText(contextPath) ? contextPath
					+ "/"
					: "/";
			log.info("Setting path for cookies to: " + cookiePath);
			this.warnCookieGenerator.setCookiePath(cookiePath);
			this.ticketGrantingTicketCookieGenerator.setCookiePath(cookiePath);
		}
		this.initialFlowSetupAction.pathPopulated = true;

		context.getFlowScope().put("credentials", credentials);

		String createTicketGrantingTicket;
		try {

			createTicketGrantingTicket = this.centralAuthenticationService
					.createTicketGrantingTicket(credentials);
			WebUtils.putTicketGrantingTicketInRequestScope(context,
					createTicketGrantingTicket);
		} catch (TicketException e) {
			log.error("createTicketGrantingTicket error: " +e.getMessage());
			context.getFlowScope().put("error", "createTicketGrantingTicket error");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}

		// putWarnCookieIfRequestParameterPresent(context);

		Service service = WebUtils.getService(this.argumentExtractors, context);
		if(service == null ){
			CommomProperty instance = CommomProperty.getDBManager();
			String url=instance.getsmsProperty("cas.client.mobile.url");	
			service = this.createServiceFrom(url, null);
		}
		if (service == null) {
			service = WebUtils.getService(context);
		}

		if (service != null && log.isDebugEnabled()) {
			log.debug("Placing service in FlowScope: " + service.getId());
		}

		context.getFlowScope().put("service", service);

		//CacheManager.getInstance().putCache(channelIdentifier,"channelIdentifier");
		if(channelIdentifier != null){
			boolean  flag = CacheManager.getInstance().putCache(channelIdentifier, channelIdentifier);
			if(flag == false){
				log.info(" RemoteLoginAction----------"+new Date()+"----------------flag="+flag);
				context.getFlowScope().put("error", "error.channelIdentifier.error ---"+flag);
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return result("submit");
			}
		}
		
		log.info(" RemoteLoginAction----------"+new Date()+"----------------end");
		return result("submit");
	}
	
	/**
	 * 场景1 以网银身份登录商城成功后跳转回商城
	 */
	private void scene1(final RequestContext context,JSONObject json,UsernamePasswordCredentials user,String eMallFieldEncryped){
		log.debug("scene1=====LOGIN_AFTER_EBANKP  "+new Date()+"----start---");
		MallUserInfo mallUserInfo = null;
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//网银用户别名
		String aliasName = (String) json.get("aliasName");
		aliasName = aliasName ==null ? "":aliasName.trim(); 
		//客户信息号
		String cisCode =(String) json.get("mainCIS");
		cisCode = cisCode ==null ? "":cisCode.trim(); 
		//客户IP
		String custmerIp = (String) json.get("custmerIp");
		custmerIp = custmerIp ==null ? "":custmerIp.trim(); 
		//客户MAC
		String custmerMAC = (String) json.get("custmerMac");
		custmerMAC = custmerMAC ==null ? "":custmerMAC.trim(); 
		//注册地区号
		String mainAreaCode = (String) json.get("mainAreaCode");
		mainAreaCode = mainAreaCode ==null ? "":mainAreaCode.trim();
		//真实姓名
		String custName = (String) json.get("CustName");
		custName = custName ==null ? "":custName.trim();
		//网银用户星级
		String ebankUserLevel = (String) json.get("CustXingji");
		ebankUserLevel = ebankUserLevel ==null ? "":ebankUserLevel.trim();
		//用户的浏览器名称
		String userbrowser=SystemMessageUtil.getBrowser(request);
		//用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		Auth auth = null;
		String loginId = "";
		String password = "";
		
		//根据CIS号检查b2c_mall_user_info表中是否存用户记录
		try {
			mallUserInfo = mallUserInfoService.getMallUserInfoByCIS(cisCode);
		} catch (EmallServiceException e2) {
			e2.printStackTrace();
			log.error(e2.getMessage());
			context.getFlowScope().put("error",
					"EmallServiceException");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
		}
		if (mallUserInfo == null) {//如果不存在用户记录
			String userId = null;
			//从序列值中生成userId信息
			try {
				userId = serialGeneratorMgr.getSerialKey(
						Constants.AUTH_USERID).trim();
			} catch (Exception e) {
				//e.printStackTrace();
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error generating userid");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
			}
			//生成商城登录名
			//判断网银是否返回别名json串中的aliasName
			if (aliasName != null && !"".equals(aliasName)) {
				// 有别名
				try {
					Auth tmpAuth = authService.getAuthByLoginId(null,
							null, aliasName);
					if (tmpAuth != null) {
						// 有 重名，根据 规则 重新 生成 logId
						//int i=1;
						String ID = "";
						do{//对于重新生成的用户名，需要再次检查是否会重名
							//loginId =aliasName+i;
							ID=serialGeneratorMgr.getSerialKey(Constants.AUTH_LOGIN_ID).trim();
							while(ID != null && ID.startsWith("0")){
								ID = ID.substring(1);
							}
							loginId =aliasName+ID;
							tmpAuth = authService.getAuthByLoginId(null,
									null, loginId);
						}while(tmpAuth !=null);
					} else {
						loginId = aliasName;
					}
				} catch (EmallServiceException e) {
					log.error(e.getMessage());
					context.getFlowScope().put("error",
							"error.userOrPassword.error");
					context.getFlowScope().put("eMallField", eMallFieldEncryped);
				}
			} else {
				// 无别名 生成虚拟 loginId
				Auth reAuth= null;
				do{
					String ID=serialGeneratorMgr.getSerialKey(Constants.AUTH_LOGIN_ID).trim();
					while(ID != null && ID.startsWith("0")){
						ID = ID.substring(1);
					}
					loginId = "WY" + ID;
					try {
						reAuth = authService.getAuthByLoginId(null,null, loginId);
					} catch (EmallServiceException e) {
						log.error(e.getMessage());
						context.getFlowScope().put("error",
								"EmallServiceException");
						context.getFlowScope().put("eMallField", eMallFieldEncryped);
					}
				}while( reAuth != null);
			}
			try {
				log.debug("scene1=====eBankLogin  "+new Date()+"----synchronized---start");
				// 信息同步
				// 个人会员登录信息表
				MallLoginInfo mallLogInfo = new MallLoginInfo();
				mallLogInfo.setUserid(userId);
				mallLogInfo.setSessionId(request.getSession().getId());
				mallLogInfo.setClientIp(custmerIp);
				mallLogInfo.setClientMAC(custmerMAC);
				try {
					mallLogInfo.setLastLoginTime((Date) dateFormat.parseObject(dateFormat.format(new Date())));
				} catch (ParseException e1) {
					log.error(e1.getMessage());
					context.getFlowScope().put("error",
							"ParseException");
					context.getFlowScope().put("eMallField", eMallFieldEncryped);
				}
				mallLogInfo.setErrorTimes(new BigDecimal(0));
				mallLogInfo.setLastLoginChannels(LoginChannels.MOVEINTERNET);
				mallLogInfo.setLastLoginWay(LoginWay.MOBILEEBANK);
				mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
				mallLogInfo.setLastLoginOs(useros);
				mallLoginInfoService.addMallloginInfo(mallLogInfo);
				// 认证信息表
				auth = new Auth();
				auth.setIsEnable("1");
				auth.setIsLock("0");
				auth.setLoginId(loginId);
				auth.setPassword("");
				auth.setUserType("0");
				auth.setUserid(userId);
				authService.addAuth(null, null, auth);
				// 个人会员信息表
				mallUserInfo = new MallUserInfo();
				mallUserInfo.setUserid(userId);
				mallUserInfo.setMemberNickname(aliasName);
				mallUserInfo.setCisCode(cisCode);
				mallUserInfo.setRealName(custName);
				mallUserInfo.setIsFirstLogin(YesOrNo.YES);
				//1实名认证
				mallUserInfo.setUserType(MallUserType.REALNAMEAUTH);
				//用户级别 
				mallUserInfo.setUserLevel("");
				mallUserInfo.setRegisterTime(new Date());
				//1是首次登录
				mallUserInfo.setIsFirstLogin(YesOrNo.YES);
				//注册渠道  0 互联网；1 移动互联网
				mallUserInfo.setRegisterChannels("1");
				//注册方式 0 商城；1 手机商城 ；2 网银；3 手机银行； 
				mallUserInfo.setRegisterWay(RegWay.MOBILEEBANKREG);
				mallUserInfo.setRegisterAreaNumber(mainAreaCode);
				//是否访问过我的商城 : 0未访问  1 访问
				mallUserInfo.setIsVisitedMember("0");
				//设置网银客户星级
				mallUserInfo.setEbankUserLevel(ebankUserLevel);
				//将行内地区号映射成行政地区号
				for(int i=mainAreaCode.length();i<5;i++)
					mainAreaCode = "0"+mainAreaCode;
				log.info("mainAreaCode:" + mainAreaCode);
				AreacodeMap areacodeMap= this.areacodeMapDAO.selectByPrimaryKey(mainAreaCode);
				if(areacodeMap != null)
				{
					mallUserInfo.setCity(areacodeMap.getCity());
					mallUserInfo.setProvince(areacodeMap.getProvince());
				}
				mallUserInfoService.addMallUserInfo(null, null,mallUserInfo);
			
				
				log.debug("eBankLogin  "+new Date()+"----synchronized---end");
				context.getFlowScope().put("eBankUserFirstLogin","YES");
			} catch (EmallServiceException e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error.userOrPassword.error");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
			}
		} else {//如果存在用户记录
			String userId = mallUserInfo.getUserid().trim();
			try {
				//根据userId查询表b2c_auth，获取用户认证信息loginId，password
				auth = authService.getAuthByUserId(null, null, userId);
				loginId = auth.getLoginId();
				password = auth.getPassword();
				
				//信息同步
				//更新个人会员登录信息表
				MallLoginInfo mallLogInfo = new MallLoginInfo();
				mallLogInfo.setUserid(userId);
				mallLogInfo.setSessionId(request.getSession().getId());
				mallLogInfo.setClientIp(custmerIp);
				mallLogInfo.setClientMAC(custmerMAC);
				try {
					mallLogInfo.setLastLoginTime((Date) dateFormat.parseObject(dateFormat.format(new Date())));
				} catch (ParseException e1) {
					log.error(e1.getMessage());
					context.getFlowScope().put("error",
							"ParseException");
					context.getFlowScope().put("eMallField", eMallFieldEncryped);
				}
				mallLogInfo.setErrorTimes(new BigDecimal(0));
				mallLogInfo.setLastLoginChannels(LoginChannels.MOVEINTERNET);
				mallLogInfo.setLastLoginWay(LoginWay.MOBILEEBANK);
				mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
				mallLogInfo.setLastLoginOs(useros);
				mallLoginInfoService.updateByPrimaryKeySelective(mallLogInfo);
				//更新个人会员信息表
				
				mallUserInfo = new MallUserInfo();
				mallUserInfo.setUserid(userId);
				mallUserInfo.setUserType(YesOrNo.YES);
				//注册渠道  0 互联网；1 移动互联网
				mallUserInfo.setRegisterChannels("1");
				mallUserInfo.setRegisterAreaNumber(mainAreaCode);
				mallUserInfo.setIsFirstLogin(YesOrNo.NO);
				//网银客户星级
				mallUserInfo.setEbankUserLevel(ebankUserLevel);
				//是否访问过我的商城 : 0未访问  1 访问
				//mallUserInfo.setIsVisitedMember(YesOrNo.YES);
				try{
					mallUserInfoService.updateByPrimaryKeySelective(null, null,mallUserInfo);
				} catch (Exception e) {
					log.error(e.getMessage());
					context.getFlowScope().put("error",
							"error.userOrPassword.error");
					context.getFlowScope().put("eMallField", eMallFieldEncryped);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error.userOrPassword.error");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
			}
		}
		updateEbankUserAddr(json, cisCode, custName);
		//设置用户名密码以及是否检查密码标志，用于后继的flow流的校验,在CustomerAuthenticationHandler校验用户名+密码时使用
		user.setIsCheckPwd(YesOrNo.NO);
		user.setUsername(loginId);
		user.setPassword(password);
		user.setCis(cisCode);
	}
	
	/**
	 * 场景2 注册成功后点击去商城逛逛，以注册信息登录成功后跳转回商城
	 */
	private void scene2(final RequestContext context,JSONObject json,UsernamePasswordCredentials user,String eMallFieldEncryped){
		log.debug("scene2=====LOGIN_AFTER_REG  "+new Date()+"----start---");
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//用户注册名称
		String loginId= (String) json.get("loginId");
		loginId = loginId == null ? "" : loginId.trim();
		//客户IP
		String custmerIp = (String) json.get("custmerIp");
		custmerIp = custmerIp ==null ? "":custmerIp.trim();
		//用户的浏览器名称
		String userbrowser=SystemMessageUtil.getBrowser(request);
		//用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		Auth auth = null;
		try {
			auth = authService.getAuthByLoginId(null, null, loginId);
		} catch (EmallServiceException e) {
			log.error(e.getMessage());
			context.getFlowScope().put("error",
					"EmallServiceException");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return;
		}
		//新增个人会员登录信息表
		MallLoginInfo mallLogInfo = new MallLoginInfo();
		mallLogInfo.setUserid(auth.getUserid());
		mallLogInfo.setSessionId(request.getSession().getId());
		try {
			mallLogInfo.setLastLoginTime((Date) dateFormat.parseObject(dateFormat.format(new Date())));
		} catch (ParseException e1) {
			log.error(e1.getMessage());
			context.getFlowScope().put("error",
					"ParseException");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
		}
		mallLogInfo.setClientIp(custmerIp);
		mallLogInfo.setErrorTimes(new BigDecimal(0));
		mallLogInfo.setLastLoginChannels(LoginChannels.MOVEINTERNET);
		mallLogInfo.setLastLoginWay(LoginWay.MOBILEEMALL);
		mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
		mallLogInfo.setLastLoginOs(useros);
		mallLoginInfoService.addMallloginInfo(mallLogInfo);
		
		//设置用户名密码以及是否检查密码标志，用于后继的flow流的校验,在CustomerAuthenticationHandler校验用户名+密码时使用
		user.setIsCheckPwd(YesOrNo.NO);
		user.setUsername(loginId);
		user.setPassword(auth.getPassword());
		user.setCis("");//注册用户无cis号
		
	}
	/**
	 * 场景3 我的商城点击实名认证成功后跳转回商城
	 */
	private void scene3(final RequestContext context,JSONObject json,JSONObject json_eMallField, UsernamePasswordCredentials user,String eMallFieldEncryped){
		log.debug("scene3=====LOGIN_AFTER_RELANAME_VERIFY  "+new Date()+"----start---");
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//客户信息号
		String cisCode =(String) json.get("mainCIS");
		cisCode = cisCode ==null ? "":cisCode.trim(); 
		//客户IP
		String custmerIp = (String) json.get("custmerIp");
		custmerIp = custmerIp ==null ? "":custmerIp.trim();
		//客户MAC
		String custmerMAC = (String) json.get("custmerMac");
		custmerMAC = custmerMAC ==null ? "":custmerMAC.trim(); 
		//注册地区号
		String mainAreaCode = (String) json.get("mainAreaCode");
		mainAreaCode = mainAreaCode ==null ? "":mainAreaCode.trim();
		//真实姓名
		String custName = (String) json.get("custName");
		custName = custName ==null ? "":custName.trim();
		//网银用户星级
		String ebankUserLevel = (String) json.get("CustXingji");
		ebankUserLevel = ebankUserLevel ==null ? "":ebankUserLevel.trim();
		//用户的浏览器名称
		String userbrowser=SystemMessageUtil.getBrowser(request);
		//用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		Auth auth = null;
		String loginId = "";
		String password = "";
		
		MallUserInfo mallUserInfo = null;
		//根据CIS号检查b2c_mall_user_info表中是否存用户记录
		try {
			mallUserInfo = mallUserInfoService.getMallUserInfoByCIS(cisCode);
		} catch (EmallServiceException e2) {
			log.error(e2.getMessage());
			context.getFlowScope().put("error",
					"EmallServiceException");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return;
		}
			//通过eMallFiled中解析出userId
			String userId = (String)json_eMallField.get("userId");
			//根据userId查询b2c_auth表，获取loginId，password
			try {
				auth = authService.getAuthByUserId(null, null, userId);
			} catch (EmallServiceException e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"EmallServiceException");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
			}
			loginId = auth.getLoginId();
			password = auth.getPassword();
		if (mallUserInfo == null) {//如果不存在用户记录
			//更新个人会员登录信息表
			MallLoginInfo mallLogInfo = new MallLoginInfo();
			mallLogInfo.setUserid(userId);
			mallLogInfo.setSessionId(request.getSession().getId());
			try {
				mallLogInfo.setLastLoginTime((Date) dateFormat.parseObject(dateFormat.format(new Date())));
			} catch (ParseException e1) {
				log.error(e1.getMessage());
				context.getFlowScope().put("error",
						"ParseException");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
			}
			mallLogInfo.setClientIp(custmerIp);
			mallLogInfo.setClientMAC(custmerMAC);
			mallLogInfo.setErrorTimes(new BigDecimal(0));
			mallLogInfo.setLastLoginChannels("0");
			mallLogInfo.setLastLoginWay("2");
			mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
			mallLogInfo.setLastLoginOs(useros);
			mallLoginInfoService.updateByPrimaryKeySelective(mallLogInfo);
			
			//更新个人会员信息表
			
			MallUserInfo mallUserInfot = new MallUserInfo();
			
			mallUserInfot = new MallUserInfo();
			mallUserInfot.setUserid(userId);
			mallUserInfot.setCisCode(cisCode);
			mallUserInfot.setUserType(YesOrNo.YES);
			mallUserInfot.setRegisterChannels("2");
			mallUserInfot.setRealName(custName);
			mallUserInfot.setRegisterAreaNumber(mainAreaCode);
			mallUserInfot.setIsFirstLogin(YesOrNo.NO);
			//用户级别  登记客户星级
			mallUserInfot.setEbankUserLevel(ebankUserLevel);
			//是否访问过我的商城 : 0未访问  1 访问
			//mallUserInfot.setIsVisitedMember(YesOrNo.YES);
			
			try{
				mallUserInfoService.updateByPrimaryKeySelective(null, null,mallUserInfot);
			} catch (Exception e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error.userOrPassword.error");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
			}
		}else{//存在用户记录
			//提示该用户已经被实名认证过了
			context.getFlowScope().put("nameVerifyFlag",
					"cisUsed");
		}
		updateEbankUserAddr(json, cisCode, custName);
		//设置用户名密码以及是否检查密码标志，用于后继的flow流的校验,在CustomerAuthenticationHandler校验用户名+密码时使用
		user.setIsCheckPwd(YesOrNo.NO);
		user.setUsername(loginId);
		user.setPassword(password);
		user.setCis(cisCode);
		
	}
	/**
	 * 场景4 未登录时，点击立即购买或去结点时，先跳转到网银登录成功后跳转回商城
	 */
	private void scene4(final RequestContext context,JSONObject json,UsernamePasswordCredentials user,String eMallFieldEncryped){
		log.debug("scene4=====LOGIN_AFTER_TO_CASHIER  "+new Date()+"----start---");
		//同场景1
		scene1(context,json,user,eMallFieldEncryped);
	}
	
	public void setWarnCookieGenerator(
			final CookieRetrievingCookieGenerator warnCookieGenerator) {
		this.warnCookieGenerator = warnCookieGenerator;
	}

	public void setArgumentExtractors(
			final List<ArgumentExtractor> argumentExtractors) {
		this.argumentExtractors = argumentExtractors;
	}

	public final void setCentralAuthenticationService(
			final CentralAuthenticationService centralAuthenticationService) {
		this.centralAuthenticationService = centralAuthenticationService;
	}

	public void setInitialFlowSetupAction(
			InitialFlowSetupAction initialFlowSetupAction) {
		this.initialFlowSetupAction = initialFlowSetupAction;
	}

	public void setTicketGrantingTicketCookieGenerator(
			final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
		this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
	}

	public AuthService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}

	public MallUserInfoService getMallUserInfoService() {
		return mallUserInfoService;
	}

	public EBankSavedUserService getEbankSavedUserService() {
		return ebankSavedUserService;
	}

	public void setEbankSavedUserService(
			EBankSavedUserService ebankSavedUserService) {
		this.ebankSavedUserService = ebankSavedUserService;
	}

	public void setMallUserInfoService(MallUserInfoService mallUserInfoService) {
		this.mallUserInfoService = mallUserInfoService;
	}

	public SerialGeneratorMgr getSerialGeneratorMgr() {
		return serialGeneratorMgr;
	}

	public void setSerialGeneratorMgr(SerialGeneratorMgr serialGeneratorMgr) {
		this.serialGeneratorMgr = serialGeneratorMgr;
	}

	public MallLoginInfoService getMallLoginInfoService() {
		return mallLoginInfoService;
	}

	public void setMallLoginInfoService(
			MallLoginInfoService mallLoginInfoService) {
		this.mallLoginInfoService = mallLoginInfoService;
	}
    public AreacodeMapDAO getAreacodeMapDAO() {
		return areacodeMapDAO;
	}

	public void setAreacodeMapDAO(AreacodeMapDAO areacodeMapDAO) {
		this.areacodeMapDAO = areacodeMapDAO;
	}
    public static SimpleWebApplicationServiceImpl createServiceFrom(
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
	public BpCommonService getBpCommonService() {
		return bpCommonService;
	}
	public void setBpCommonService(BpCommonService bpCommonService) {
		this.bpCommonService = bpCommonService;
	}
	public AddressService getAddressService() {
		return addressService;
	}

	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}

	public EbankUserAddrService geteBankUserAddrService() {
		return eBankUserAddrService;
	}

	public void seteBankUserAddrService(EbankUserAddrService eBankUserAddrService) {
		this.eBankUserAddrService = eBankUserAddrService;
	}
	
	private String handleException(String cityAddr) {
		// TODO Auto-generated method stub
		String city = (String)(this.hashMap.get(cityAddr.length()<=2?cityAddr:cityAddr.substring(0,2)));	
		if(city == null)
			return cityAddr;
		else
			return city;
	}
	
	private void updateEbankUserAddr(JSONObject json,String cisCode,String custName)
	{
		try
		{
		//网银用户 省
		String provinceAddr = (String) json.get("provinceAddr");
		provinceAddr = provinceAddr ==null ? "":provinceAddr.trim();
		
		// 网银用户 市
		String cityAddr = (String) json.get("cityAddr");
		cityAddr = cityAddr == null ? "" : cityAddr.trim();

		// 网银用户 县/区
		String countyAddr = (String) json.get("countyAddr");
		countyAddr = countyAddr == null ? "" : countyAddr.trim();

		// 网银用户 通讯地址
		String commAddr = (String) json.get("commAddr");
		commAddr = commAddr == null ? "" : commAddr.trim();

		// 网银用户 邮编
		String postalcode = (String) json.get("postalcode");
		postalcode = postalcode == null ? "" : postalcode.trim();

		// 网银用户 电话区号
		String conarea = (String) json.get("conarea");
		conarea = conarea == null ? "" : conarea.trim();

		// 网银用户 联系电话
		String teleNum = (String) json.get("teleNum");
		teleNum = teleNum == null ? "" : teleNum.trim();

		// 网银用户 公司电话
		String companyTel = (String) json.get("companyTel");
		companyTel = companyTel == null ? "" : companyTel.trim();

		// 网银用户 家庭电话
		String homeTel = (String) json.get("homeTel");
		homeTel = homeTel == null ? "" : homeTel.trim();

		// 网银用户 手机号
		String mobileNum = (String) json.get("mobileNum");
		mobileNum = mobileNum == null ? "" : mobileNum.trim();
		
		Address address  =  new Address();
		address.setName(provinceAddr.length()<=2?provinceAddr:provinceAddr.substring(0, 2));
		address.setPid(AddressService.TOP_NODE_ADDRESS_ID);
		address = this.addressService.getAddressByProvinceName(address);
		EBankUserAddr eBankUserAddr = new EBankUserAddr();
		eBankUserAddr.setCis(cisCode);
		eBankUserAddr.setName(custName);
		eBankUserAddr.setAddress(commAddr);
		eBankUserAddr.setPostcode(postalcode);
		eBankUserAddr.setMobile(mobileNum);
		eBankUserAddr.setConttel(teleNum);
		eBankUserAddr.setComptel(companyTel);
		eBankUserAddr.setHometel(homeTel);
		eBankUserAddr.setContarea(conarea);
		
		//查到了省
		if(address !=null)
		{
			eBankUserAddr.setPovince(address.getId());
			
			address.setPid(address.getId());
			
			cityAddr = handleException(cityAddr);
			
			address.setName(cityAddr);
			
			address = this.addressService.getAddressByCityNameAndProvID(address);
	
			//如果查到市
			if(address != null)
			{
				eBankUserAddr.setCity(address.getId());
				
				address.setPid(address.getId());
				address.setName(countyAddr);
				
				address = this.addressService.getAddressByCountyNameAndCityID(address);
				if(address != null)
				{
					eBankUserAddr.setDistrict(address.getId());
				}
				
			}
			
		}
		if(this.eBankUserAddrService.getAddrByCIS(cisCode) == null)
		{
			eBankUserAddrService.addEBankUserAddr(eBankUserAddr);
		}
		else
		{
			eBankUserAddrService.updateEBankUserAddr(eBankUserAddr);
		}
		}
		catch(Exception e)
		{
			log.error("error updating ebankuseraddr");
		}
	}
}
