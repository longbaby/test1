package com.icbc.emall.cas.validate;


import java.security.interfaces.RSAPrivateKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.NamedAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.com.infosec.icbc.ReturnValue;

import com.icbc.common.utils.ApplicationConfigUtils;
import com.icbc.common.utils.VerifyCode;
import com.icbc.common.utils.VerifyCode.VerifyCodeInfo;
import com.icbc.crypto.utils.TripleDesCryptFileInputKey;
import com.icbc.emall.CoreAuthException;
import com.icbc.emall.EmallServiceException;
import com.icbc.emall.SystemMessageUtil;
import com.icbc.emall.audit.service.impl.AuthServiceEvent;
import com.icbc.emall.auth.model.Auth;
import com.icbc.emall.auth.service.AuthService;
import com.icbc.emall.cas.util.Base64;
import com.icbc.emall.cas.util.CollectionOfUserInformation;
import com.icbc.emall.cas.util.PasswordHelper;
import com.icbc.emall.cas.util.SHA1;
import com.icbc.emall.ciscode.model.Ciscode;
import com.icbc.emall.ciscode.service.CiscodeService;
import com.icbc.emall.common.sso.LoginTransObject;
import com.icbc.emall.common.utils.Crypt;
import com.icbc.emall.common.utils.Globe.AuthException;
import com.icbc.emall.common.utils.Globe.AuthType;
import com.icbc.emall.common.utils.Globe.LoginChannels;
import com.icbc.emall.common.utils.Globe.LoginWay;
import com.icbc.emall.common.utils.Globe.YesOrNo;
import com.icbc.emall.common.utils.SpringContextLoaderListener;
import com.icbc.emall.lottery.model.LotteryUserinfo;
import com.icbc.emall.mall.model.MallLoginInfo;
import com.icbc.emall.mall.model.MallLoginLogInfo;
import com.icbc.emall.mall.service.MallLoginInfoService;
import com.icbc.emall.mall.service.MallLoginLogInfoService;
import com.icbc.emall.member.model.MallUserInfo;
import com.icbc.emall.member.service.MallUserInfoService;
import com.icbc.emall.merchant.service.AreacodeMapService;
import com.icbc.emall.util.gtcg.Gtcg;
import com.icbc.emall.util.gtcg.GtcgIOException;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputLogin;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputQueryUserInfo;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputQueryUserType;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputUpdateInfo;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.login.UAOutputLogin;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.queryInfo.UAOutputQueryInfo;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.queryUserType.UAOutputQueryUserType;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.realNameQuery.UAOutputRealNameQuery;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.updateInfo.UAOutputUpdateInfo;
import com.icbc.emall.util.keygen.Constants;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;
import com.icbc.finance.pmis.common.CommomProperty;
import com.utils.RSA;
import com.utils.decodeSoftKeyBoardRule;
import com.utils.readIni;


public class CustomerAuthenticationHandler implements
		NamedAuthenticationHandler,ApplicationEventPublisherAware{

	@Autowired
	private AuthService authService;
	
	private MallLoginInfoService mallLoginInfoService;
	
	private MallLoginLogInfoService mallLoginLogInfoService;
	
	private SerialGeneratorMgr serialGeneratorMgr;
	
	private CollectionOfUserInformation collectionOfUserInformation;
	
	private String sessionTimeOut;
	
	private ApplicationEventPublisher applicationEventPublisher;
	
	private CiscodeService ciscodeService;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	//ua
	private Gtcg gtcg; 
	
	/** Default class to support if one is not supplied. */
	private static final Class<UsernamePasswordCredentials> DEFAULT_CLASS = UsernamePasswordCredentials.class;
	
	private static final String MERCHANT_LOGIN_WAY = "4";

	/** Class that this instance will support. */
	@NotNull
	private Class<?> classToSupport = DEFAULT_CLASS;

	/**
	 * Boolean to determine whether to support subclasses of the class to
	 * support.
	 */
	private boolean supportSubClasses = true;

	/** The name of the authentication handler. */
	@NotNull
	private String name = getClass().getName();
	
	public CustomerAuthenticationHandler() { }

	public boolean authenticate(Credentials credentials)
			throws AuthenticationException {

		final boolean authenticated = doAuthentication(credentials);
		return postAuthenticate(credentials, authenticated);
	}

	/**
	 * Method to execute after authentication occurs.
	 * 
	 * @param credentials
	 *            the supplied credentials
	 * @param authenticated
	 *            the result of the authentication attempt.
	 * @return true if the handler should return true, false otherwise.
	 */
	private boolean postAuthenticate(final Credentials credentials,
			final boolean authenticated) {
		return authenticated;
	}

	private final boolean doAuthentication(final Credentials credentials)
			throws AuthenticationException {
		// 如果是网银用户直接返回       user.getLoginWay()  手机登录独有的属性
		if(credentials == null){
			log.error("error ==========>CustomerAuthenticationHandler:doAuthentication  :  credentials cant not null");
			return false;
		}else{
			UsernamePasswordCredentials user = (UsernamePasswordCredentials) credentials;
			if ( YesOrNo.NO.equals(user.getIsCheckPwd())) {
				return true;
			} else if ("1".equals(user.getLoginWay()) || "3".equals(user.getLoginWay())){
				String uaswitch = ApplicationConfigUtils.getInstance().getPropertiesValue(com.icbc.emall.Constants.UNIFIED_AUTH_BUS_SWITCH);
				if(uaswitch.equals("1"))
					return authenticatePhoneUsernamePasswordInternal1(user);
				else
					return authenticatePhoneUsernamePasswordInternal(user);
			//商户场景登录认证 loginWay　2、4（商户） 1、3(手机) 5（商城新的合并页面）
			} else if ("2".equals(user.getLoginWay())){
				return authenticateMerchantUsernamePasswordInternal(user);
			} else if (MERCHANT_LOGIN_WAY.equals(user.getLoginWay())){
				return authenticateMerchantPhoneCodeInternal(user);
			//合并登录页面之后的处理
			} else if(LoginWay.NEW_MALL.equals(user.getLoginWay())){	
				 return mallAuthenticateUsernamePassword(user , user.getLoginWay());
			//合并登录页面之后的处理---手机
			} else if(LoginWay.MOBILE_NEW_MALL.equals(user.getLoginWay()) || LoginWay.PAD_NEW_MALL.equals(user.getLoginWay())){	
				 return mallAuthenticateUsernamePasswordMobile(user , user.getLoginWay());
			//实名认证登录
			}else if(LoginWay.CAS_EBANK.equals(user.getLoginWay())){
				return realNameAuthenticateInternal(user);
			//商城登录原流程
			}else{
				String uaswitch = ApplicationConfigUtils.getInstance().getPropertiesValue(com.icbc.emall.Constants.UNIFIED_AUTH_BUS_SWITCH);
				if("1".equals(uaswitch))
					return authenticateUsernamePasswordInternal1(user);
				else
					return authenticateUsernamePasswordInternal(user);
			}
		}
	}
	
	
	
	/** 根据是否密码控件解密密码
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private String getPassword(boolean isUseSafeCtrl,UsernamePasswordCredentials credentials,HttpServletRequest request,VerifyCodeInfo verifycode) throws ICBCCASAuthenticationException
	{
		String userpswd="";
		if(isUseSafeCtrl==true){
			//userpswd = (String) credentials.getPassword();
			userpswd = ((UserCredentials)credentials).getLogonCardPass();
			String safePluginType = request.getParameter("SafePluginType");
			int pluginType = 0;
			if(StringUtils.isNotBlank(safePluginType)){
				pluginType = Integer.parseInt(safePluginType);
			}
			//解密
			byte[] passwordBytes = null;
			passwordBytes = Crypt.decodeFromSafePlugin(request.getParameter("randomId"),verifycode.verifyCode,userpswd,pluginType).getBytes();
			if (passwordBytes == null)
			{
				throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
			userpswd = new String(passwordBytes);
		}else{
			userpswd = (String) credentials.getPassword();
			String dictKey=request.getParameter("dictKey");
			HttpSession session=request.getSession();
			List<String> dictList=(List<String>)session.getAttribute(dictKey);
			try {
				userpswd=PasswordHelper.decrypt(userpswd, dictList);
			} catch (Exception e) {
				throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
		}
		return userpswd;
	}
	
	private boolean verifyCode(boolean isSafe,VerifyCodeInfo verifycode,HttpServletRequest request,UsernamePasswordCredentials credentials)throws ICBCCASAuthenticationException{
		String jCaptcha = ((UserCredentials) credentials).getJ_captcha();
		if ( isSafe==true) {
			// 有控件
			UserCredentials tmp = null;
			tmp = (UserCredentials) credentials;
			jCaptcha = tmp.getVerifyCode();
			String safePluginType = request.getParameter("SafePluginType");
			int pluginType = 0;
			if(StringUtils.isNotBlank(safePluginType)){
				pluginType = Integer.parseInt(safePluginType);
			}
			jCaptcha = Crypt.decodeFromSafePlugin(
					request.getParameter("randomId"), verifycode.verifyCode,
					jCaptcha,pluginType);
		} 
		boolean human = VerifyCode.isVerifycodeValid(verifycode, jCaptcha);
		if (!human) {
				log.error(" error==========>error VerifyCode  verifycode=" + verifycode	+ " ==========> jCaptcha=" + jCaptcha);
				return false;
		}
		return true;
	}
	
	/**
	 * @desc查询用户类型
	 * @param userName
	 * @param password
	 * @return
	 * @throws ICBCCASAuthenticationException
	 * @throws GtcgIOException 
	 */
	private String checkUserType(String userName, String password) throws ICBCCASAuthenticationException {
		this.log.debug("Invoking method checkUserType : checkUserType start -------");
		UAOutputQueryUserType uAOutputQueryUserType = null;
		UAInputQueryUserType uAInputQueryUserType = new UAInputQueryUserType();
		uAInputQueryUserType.setUSERNAME(userName);
		
		Pattern pa = Pattern.compile("^[0-9]{11}$");
		Matcher match = pa.matcher(userName);
		if(match.find())
		{
			uAInputQueryUserType.setUSERNAME_TYPE("3");
		}else{
			uAInputQueryUserType.setUSERNAME_TYPE("1");
		}
		 
		try {
			//用户类型查询
			uAOutputQueryUserType = (UAOutputQueryUserType)gtcg.sendToEbank(uAInputQueryUserType, "PassCheckUserType");
		} catch (GtcgIOException e) {
			this.log.error("Invoking method checkUserType : query userType failed !");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}

		if(uAOutputQueryUserType!=null&&"0".equals(uAOutputQueryUserType.getPublicInfo().getRetCode()))
			return uAOutputQueryUserType.getPrivateInfo().getUserType(); 
		else
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
	}
	
	
	/**
	 * @desc 实名用户查询
	 * @param userName
	 * @param userpswd
	 * @return
	 * @throws ICBCCASAuthenticationException
	 */
	private UAOutputRealNameQuery realNameQuery(String userName,String userpswd)throws ICBCCASAuthenticationException{
		//调用实名通行证认证接口
		this.log.info("Invoking method realNameQuery : query the realName start!");
		UAInputLogin uAInputLogin = new UAInputLogin();
		uAInputLogin.setLOGIN_ID(userName);
		uAInputLogin.setPASSWORD(userpswd);
		
		UAOutputRealNameQuery realNameQuery = null;
		try {
			realNameQuery = (UAOutputRealNameQuery) gtcg.sendToEbank(uAInputLogin, "PassRealNameVerify");
		} catch (GtcgIOException e) {
			log.error("Invoking method realNameQuery failed!", e);
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		this.log.info("Invoking method realNameQuey : Invoking method success!");
		return realNameQuery;
	}
	
	/**
	 * @desc 非实名用户查询
	 * @param userName
	 * @param userpswd
	 * @return
	 * @throws ICBCCASAuthenticationException
	 */
	private UAOutputLogin unRealNameQuery(String userName,String userpswd)throws ICBCCASAuthenticationException{
		this.log.info("Invoking method unRealNameQuery : Invoking unRealName query start");
		UAInputLogin uAInputLogin = new UAInputLogin();
		uAInputLogin.setLOGIN_ID(userName);
		uAInputLogin.setPASSWORD(userpswd);
		
		UAOutputLogin uAOutputLogin = null;
		try {
			uAOutputLogin = (UAOutputLogin) gtcg.sendToEbank(uAInputLogin, "PassUnRealNameVerify");
		} catch (GtcgIOException e) {
			this.log.error("Invoking method unRealNameQuery : Invoking unRealName query error");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		
		if(uAOutputLogin == null){
			this.log.error("Invoking method unRealNameQuery : Invoking unRealName query error");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		this.log.info("Invoking method unRealNameQuery :Invoking unRealName succeed");
		return uAOutputLogin;
	}
	
	
	private Auth createAuth(UAOutputLogin uAOutputLogin){
		Auth authInfo = new Auth();
		authInfo.setUserid(uAOutputLogin.getPrivateInfo().getUserid());
		authInfo.setUserType(uAOutputLogin.getPrivateInfo().getUserType());
		authInfo.setIsLock(uAOutputLogin.getPrivateInfo().getIsLock());
		authInfo.setIsEnable(uAOutputLogin.getPrivateInfo().getIsEnable());
		authInfo.setLoginId(uAOutputLogin.getPrivateInfo().getLoginID());
		return authInfo;
	}
	
	
	 
	
	/**
	 * @desc 非实名用户同步本地数据与远程一致
	 * @param uAOutputLogin
	 * @param username
	 * @throws ICBCCASAuthenticationException
	 */
	private void unRealNameDealLocalData(UAOutputLogin uAOutputLogin,String username)throws ICBCCASAuthenticationException{
		this.log.debug("Invoking method unRealNameDealLocalData :  deal local data start");
		uAOutputLogin.getPrivateInfo().setLoginID(username);
		Auth authInfo = null;
		MallUserInfo mallUserInfo = null;
		String userid = uAOutputLogin.getPrivateInfo().getUserid();
		try {
			authInfo = authService.getAuthByUserId(null, null, userid);
			mallUserInfo  = this.mallUserInfoService.getMallUserById1(userid);
		} catch (Exception e) {
			this.log.error("error get auth by userid:" + userid);
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		//(1)用userid到本地查询，查询有记录，允许登录，用户名、手机号、邮箱与本地不一致，更新本地的用户信息；更新失败，不允许登录
		if(authInfo == null) {
			//补录信息
			this.log.info("no userinfo in mall");
			this.log.info("add userinfo in mall");
			authInfo = this.createAuth(uAOutputLogin);
			MallUserInfo mui = createMallUserInfo(uAOutputLogin);
			//添加个人会员信息至个人会员信息表、登录信息表
			try {
				mallUserInfoService.addMallUserAndAuth(ApplicationConfigUtils.getInstance().getApplicationId(),
						ApplicationConfigUtils.getInstance().getApplicationKey(), mui,authInfo);
			} catch (EmallServiceException e) {
				this.log.error("Invoking method unRealNameDealLocalData : Add userinfo in mall failed!");
				throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
			this.log.info("Invoking method unRealNameDealLocalData : Add userinfo in mall succeed");
		}
		else
		{
			this.log.debug("Invoking method unRealNameDealLocalData : Update userinfo in mall start");
			try{
				//返回信息同本地auth不一致 更新本地auth
	//			1 auth一致 mallUserInfo不一致
	//			2 auth不一致 mallUserInfo一致
	//			3 auth不一致 mallUserInfo不一致
				int result = isUserInfoConsistent(authInfo,mallUserInfo,uAOutputLogin); 
				if(result==1){
					mallUserInfoService.updateMallUserInfo(null, null, mallUserInfo);
				}else if(result == 2){
					authService.updateAuthByUserId(authInfo);
				}else if(result == 3){
					mallUserInfoService.updateMallUserInfoAndAuth(mallUserInfo, authInfo);
				}
			}
			catch(Exception e){
				this.log.error("Invoking method unRealNameDealLocalData : update userinfo in mall error");
				throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
			this.log.debug("Invoking method unRealNameDealLocalData : update userinfo in mall end");
		}
		this.log.debug("Invoking method unRealNameDealLocalData : deal local data end");
	}
	
	/**
	 * @desc 实名认证用户信息返回，进行非实名认证用户处理
	 * @param realNameQuery
	 */
	private void realNameQueryInfo(UAOutputRealNameQuery realNameQuery , HttpServletRequest request)throws ICBCCASAuthenticationException{
		
		String cisCode = realNameQuery.getPrivateInfo().getMainCIS();
		
		UAOutputQueryInfo uAOutputQueryInfo = null;
		UAInputQueryUserInfo uAInputQueryUserInfo = new UAInputQueryUserInfo();
		uAInputQueryUserInfo.setFIELDTYPE("4");
		uAInputQueryUserInfo.setFIELDVALUE(cisCode);
		try {
			//认证信息查询接口
			uAOutputQueryInfo = (UAOutputQueryInfo)gtcg.sendToEbank(uAInputQueryUserInfo, "PassVerifyInfoQry");
			//0：一条记录 1：没有记录
			if(uAOutputQueryInfo!=null&&"0".equals(uAOutputQueryInfo.getPublicInfo().getRetCode())){
					this.realNameDealLocalData(realNameQuery, uAOutputQueryInfo,request);	//存在记录进行数据同步,还需要判断本地是否被锁
			}else if(uAOutputQueryInfo!=null&&"1".equals(uAOutputQueryInfo.getPublicInfo().getRetCode())){
					this.signUpLocalData(realNameQuery , request ); 							//不存在数据进行电商数据补录
			}else{
					throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
		} catch (GtcgIOException e) {
			log.error("");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
	}
	
	/**
	 * @desc 实名认证用户本地数据同步
	 * @param realNameQuery
	 * @throws ICBCCASAuthenticationException 
	 */
	private void signUpLocalData(UAOutputRealNameQuery realNameQuery, HttpServletRequest request) throws ICBCCASAuthenticationException{

		String mainAreaCode = realNameQuery.getPrivateInfo().getMainAreaCode();
		String userId = realNameQuery.getPrivateInfo().getUserId();
		String cisCode = realNameQuery.getPrivateInfo().getMainCIS();
		//用户的浏览器名称
		String userbrowser=SystemMessageUtil.getBrowser(request);
		//用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		for(int i=mainAreaCode.length();i<5;i++)
			mainAreaCode = "0"+mainAreaCode;
		
		try
		{
			Auth auth = createAuth(realNameQuery);
			MallUserInfo mallUserInfo = collectionOfUserInformation.createMallUserInfo(userId,realNameQuery.getPrivateInfo().getAliasName(),realNameQuery.getPrivateInfo().getMainCIS(),
					realNameQuery.getPrivateInfo().getCustName(),mainAreaCode,realNameQuery.getPrivateInfo().getCustXingji(),realNameQuery.getPrivateInfo().getMobileNum());
			MallLoginInfo mallLogInfo = collectionOfUserInformation.createLoginLog(userId,request.getSession().getId(),null,null,"1",userbrowser,useros,LoginWay.MALL);
			LotteryUserinfo lotteryUserinfo = new LotteryUserinfo();
			lotteryUserinfo.setUserId(userId);
			String outuserid = serialGeneratorMgr.getSerialKey(Constants.LOTTERYUSERINFODSERIAL).trim();
			lotteryUserinfo.setOutuserId(outuserid);
			Ciscode ciscode = new Ciscode();
			ciscode.setCiscode(cisCode);
			ciscode.setUserid(userId);
			
				//ciscodeService.addUser(mallUserInfo, auth, null,mallLogInfo,lotteryUserinfo);
				getCiscodeService().addUser(mallUserInfo, auth, ciscode,mallLogInfo,lotteryUserinfo);

		}
		catch(Exception e){
			log.error("Invoking method CustomerAuthenticationHandler<The method collectionOfUserInformation.syncLocalInfo> : sync failed !",e);
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
	
	}
	
	
	
	private Auth createAuth(UAOutputRealNameQuery realNameQuery) {
		Auth authInfo = new Auth();
		authInfo.setUserid(realNameQuery.getPrivateInfo().getUserId());
		authInfo.setLoginId(realNameQuery.getPrivateInfo().getAliasName());
		authInfo.setUserType(AuthType.MALL);
		authInfo.setIsLock("0");
		authInfo.setIsEnable("1");
		return authInfo;
	}

	/**
	 * @desc 实名用户同步本地数据与远程一致
	 * @param realNameQuery
	 * @param uAOutputLogin
	 * @throws ICBCCASAuthenticationException
	 * @throws EmallServiceException 
	 * @throws ParseException 
	 */
	private void realNameDealLocalData(UAOutputRealNameQuery realNameQuery, UAOutputQueryInfo uAOutputQueryInfo, HttpServletRequest request)throws ICBCCASAuthenticationException{
		
		
		/*
		 *  		调用统一认证查询接口，获取该用户的信息
		 * 					在进行判断手机号，如果如果手机号为null，进行上送更新统一认证服务器，
		 *				处理该方法syncLocalInfo，注册新添手机号，如果不是注册，判断手机号是否一致，不一致修改
		*/
		String custMobile = realNameQuery.getPrivateInfo().getMobileNum();
		String mainAreaCode = realNameQuery.getPrivateInfo().getMainAreaCode();
		String ebankUserLevel = realNameQuery.getPrivateInfo().getCustXingji();
		String userid = uAOutputQueryInfo.getPrivateInfo().getUserid();
		String mobile = uAOutputQueryInfo.getPrivateInfo().getMobile();
		//如果手机号为空,更新统一认证服务器中的数据库,不为空，使用统一通行证上的手机号 , 如果网银的手机号和统一通行证上的手机号不一致，使统一通行证上的手机号和网银保持一致
		//如果网银传递过来的手机号为null，使用统一通行证上的手机号
		if(org.apache.commons.lang.StringUtils.isBlank(mobile) || (org.apache.commons.lang.StringUtils.isNotBlank(custMobile) && !mobile.equals(custMobile))){
			UAOutputUpdateInfo uAInputUpdateInfo = updateMobileUA(userid,custMobile); 
			if(uAInputUpdateInfo == null)
			{
				this.log.error("===Invoking method updateMobileUA : return object UAOUtputUpdateInfo is null!");
			}
			if(!"0".equals(uAInputUpdateInfo.getPublicInfo().getRetCode()))
			{
				this.log.error("=== Invoking method updateMobileUA : return object UAOUtputUpdateInfo , the retCode is {} , the retmsg is {} " , uAInputUpdateInfo.getPublicInfo().getRetCode(), uAInputUpdateInfo.getPublicInfo().getRetMsg());
			}	
		}else{
			custMobile = mobile;
		}
		
		if(uAOutputQueryInfo.getPrivateInfo().getIsLock().equals("1")){
			throw new ICBCCASAuthenticationException(AuthException.AUTH04,"账户被锁定，禁止登录。该账户将在锁定24小时之后解锁！");
		}
		
		String cisCode = uAOutputQueryInfo.getPrivateInfo().getCisCode();
		//用户的浏览器名称
		String userbrowser=SystemMessageUtil.getBrowser(request);
		//用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		
		String custName = realNameQuery.getPrivateInfo().getCustName();
		
		try {
			
			collectionOfUserInformation.syncLocalInfo(uAOutputQueryInfo,cisCode,request.getSession().getId(), null, null, "0", userbrowser, useros,LoginWay.MOBILEEBANK,custName,custMobile);
			
			MallLoginInfo mallLogInfo = collectionOfUserInformation.createLoginLog(userid, request.getSession().getId(), null, null, "1", userbrowser, useros,LoginWay.MALL);
			
			MallUserInfo mallUserInfo = new MallUserInfo();
			mallUserInfo.setUserid(userid);
			mallUserInfo.setUserType(YesOrNo.YES);
			mallUserInfo.setRegisterChannels("2");
			mallUserInfo.setRegisterAreaNumber(mainAreaCode);
			mallUserInfo.setIsFirstLogin(YesOrNo.NO);
			//网银客户星级
			mallUserInfo.setEbankUserLevel(ebankUserLevel);
			//是否访问过我的商城 : 0未访问  1 访问
			mallUserInfoService.updateUserAndLoginInfo(null, null, mallUserInfo, mallLogInfo);
			
			collectionOfUserInformation.updateEbankUserAddr(realNameQuery);
		} catch (Exception e) {
			log.error("Invoking method CustomerAuthenticationHandler<The method collectionOfUserInformation.syncLocalInfo> : sync failed !",e);
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		
		 
		
		
	}
	
	
	
	
	/**
	 * @desc 更改用户手机号到同一认证服务器
	 * @param ciscode
	 * @param mobile
	 * @return
	 */
	private UAOutputUpdateInfo updateMobileUA( String userId, String mobile ) {
		UAInputUpdateInfo  uAInputUpdateInfo = new UAInputUpdateInfo();
		
		uAInputUpdateInfo.setUSERID(userId);
		uAInputUpdateInfo.setMOBILE(mobile);
		uAInputUpdateInfo.setMOBILEFLAG("1");
		Gtcg gtcg = (Gtcg) SpringContextLoaderListener
				.getSpringWebApplicationContext().getBean(
						"gtcgService");
		UAOutputUpdateInfo uAOutputUpdateInfo = null;
		try {
			uAOutputUpdateInfo = (UAOutputUpdateInfo) gtcg.sendToEbank(uAInputUpdateInfo, "PassVerifyInfoModify");
		} catch (GtcgIOException e) {
			this.log.error(e.getMessage());
			return null;
		}
		return uAOutputUpdateInfo;
	}
	
	
	/*
	 * auth表、mallUserInfo表中信息是否与统一认证接口返回的信息一致
	 * 返回值：0 一致
	 * 		 1 auth一致 mallUserInfo不一致
	 * 		 2 auth不一致 mallUserInfo一致
	 * 		 3 auth不一致 mallUserInfo不一致
	 */
	private int isUserInfoConsistent(Auth authInfo, MallUserInfo mallUserInfo,UAOutputLogin uAOutputLogin) {
		if(isAuthConsistent(authInfo,uAOutputLogin)){
			if(isMallUserInfoConsistent(mallUserInfo, uAOutputLogin))
				return 0;
			else
				return 1;
		}else{
			if(isMallUserInfoConsistent(mallUserInfo, uAOutputLogin))
				return 2;
			else
				return 3;
		}
	}
	
	/*
	 * 判断商城auth表中信息是否与统一认证相同
	 */
	private boolean isAuthConsistent(Auth authInfo, UAOutputLogin uAOutputLogin)
	{
		String loginID = authInfo.getLoginId();
		loginID = loginID==null?"":loginID.trim();
		String userType = authInfo.getUserType();
		userType = userType==null?"":userType.trim();
		String isLock = authInfo.getIsLock();
		isLock = isLock==null?"":isLock.trim();
		String isEnable = authInfo.getIsEnable();
		isEnable = isEnable==null?"":isEnable.trim();
		
		String loginID1 = uAOutputLogin.getPrivateInfo().getLoginID();
		loginID1 = loginID1==null?"":loginID1.trim();
		String userType1 = uAOutputLogin.getPrivateInfo().getUserType();
		userType1 = userType1==null?"":userType1.trim();
		String isLock1 = uAOutputLogin.getPrivateInfo().getIsLock();
		isLock1 = isLock1==null?"":isLock1.trim();
		String isEnable1 = uAOutputLogin.getPrivateInfo().getIsEnable();
		isEnable1 = isEnable1==null?"":isEnable1.trim();
		
		if(loginID.equals(loginID1)&&userType.equals(userType1)&&isLock.equals(isLock1)&&isEnable.equals(isEnable1))
			return true;
		else
		{
			authInfo.setLoginId(loginID1);
			authInfo.setUserType(userType1);
			authInfo.setIsLock(isLock1);
			authInfo.setIsEnable(isEnable1);
			return false;
		}
	}
	
	private boolean isMallUserInfoConsistent(MallUserInfo mallUserInfo,UAOutputLogin uAOutputLogin)
	{
		String email = mallUserInfo.getEmail();
		email = email==null?"":email.trim();
		String mobile = mallUserInfo.getMobile();
		mobile = mobile==null?"":mobile.trim();
		String province = mallUserInfo.getProvince();
		province = province==null?"":province.trim();
		String city = mallUserInfo.getCity();
		city = city==null?"":city.trim();
		String ciscode = mallUserInfo.getCisCode();
		ciscode = ciscode==null?"":ciscode.trim();
		
		String email1 = uAOutputLogin.getPrivateInfo().getEmail();
		email1 = email1==null?"":email1.trim();
		String mobile1 = uAOutputLogin.getPrivateInfo().getMobile();
		mobile1 = mobile1==null?"":mobile1.trim();
		String province1 = uAOutputLogin.getPrivateInfo().getProvince();
		province1 = province1==null?"":province1.trim();
		String city1 = uAOutputLogin.getPrivateInfo().getCity();
		city1 = city1==null?"":city1.trim();
		String ciscode1 = uAOutputLogin.getPrivateInfo().getCisCode();
		ciscode1 = ciscode1==null?"":ciscode1.trim();
		
		
		if(email.equals(email1)&&mobile.equals(mobile1)&&province.equals(province1)&&city.equals(city1)&&ciscode.equals(ciscode1))
			return true;
		else
		{
			mallUserInfo.setEmail(email1);
			mallUserInfo.setMobile(mobile1);
			mallUserInfo.setProvince(province1);
			mallUserInfo.setCity(city1);
			mallUserInfo.setCisCode(ciscode1);
			if("".equals(ciscode1))
				mallUserInfo.setUserType("0");
			else
				mallUserInfo.setUserType("1");
			return false;
		}

	}
	
	private MallUserInfo createMallUserInfo(UAOutputLogin uAOutputLogin) {
		MallUserInfo mui = new MallUserInfo();
		mui.setUserid(uAOutputLogin.getPrivateInfo().getUserid());
		mui.setMobile(uAOutputLogin.getPrivateInfo().getMobile());
		mui.setEmail(uAOutputLogin.getPrivateInfo().getEmail());
		//非实名
		mui.setUserType("0");
		mui.setProvince(uAOutputLogin.getPrivateInfo().getProvince());
		mui.setCity(uAOutputLogin.getPrivateInfo().getCity());
		mui.setUserLevel("");
		mui.setRegisterWay("0");		//注册方式 0 商城；1 手机商城 ；2 网银；3 手机银行； 
		mui.setRegisterChannels("0");	//注册渠道  0 互联网；1 移动互联网
		mui.setPost("");
		mui.setIsVisitedMember("0");	//是否访问过我的商城 : 0未访问  1 访问
		String cisCode = uAOutputLogin.getPrivateInfo().getCisCode();
		cisCode = cisCode==null?"":cisCode.trim();
		mui.setCisCode(cisCode);
		if(!"".equals(cisCode))
			mui.setUserType("1");
		else
			mui.setUserType("0");
		//行政地区号转化成行内地区号
		AreacodeMapService areacodeMapService = (AreacodeMapService)SpringContextLoaderListener
				.getSpringWebApplicationContext().getBean("areacodeMapService");
		try {
			areacodeMapService.setMallUserAreacode(mui);
		} catch (EmallServiceException e) {
			log.error("Invoking method CustomerAuthenticationHandler.createMallUserInfo : The administrative area number into the mainland area code(ICBC area code) error!",e);
		}
		
		return mui;
	}
	
	private String encrypt(String custpwd) throws Exception {
		/*
		 *  1，先用utf-8将信息编码为字节数组
			2，再加密
			调用方法com.icbc.crypto.utils.TripleDesCryptFileInputKey.IcbcTripleDes(in,len,out,0,1,1,keyFile) 
			----keyFile使用的是ICBC_EMALL_1_00000_3DES_16的路径
			3，最后用base64encode编码：  
		 */
		byte[] b = custpwd.getBytes("UTF-8");
		byte[] encByte = new byte[(b.length / 8) * 8 + 8];
		byte[] encByteBase64 = null;
		String KeyFilePath = CommomProperty.getDBManager().getsmsProperty("KeyFilePath"); 
		String keyFile = KeyFilePath.endsWith("/") ? (KeyFilePath+"ICBC_EMALL_1_00000_3DES_16"):(KeyFilePath+"/ICBC_EMALL_1_00000_3DES_16");
		int result = TripleDesCryptFileInputKey.IcbcTripleDes(b, b.length, encByte, 0, 1, 1, keyFile);
		if(result>0)
		{
			encByteBase64 = ReturnValue.base64enc(encByte);
			custpwd = new String(encByteBase64,"UTF-8");
		}
		else
		{
			this.log.error("result:"+result);
			throw new Exception("加密失败");
		}
		
		return custpwd;
	}
	
	
	/**
	 * @desc 实名认证登录（购买理财产品等）
	 * @param user
	 * @return
	 */
	private boolean realNameAuthenticateInternal(
			UsernamePasswordCredentials credentials) throws AuthenticationException{
		 
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo verifycode = (VerifyCodeInfo) request.getSession().getAttribute("verifycode");
		//是否使用控件
		boolean isSafe = this.isUseSafeCtrl(credentials);
		//验证码处理
		boolean verifyFlag = verifyCode(isSafe,verifycode,request,credentials);
		if(verifyFlag != true){
			throw new ICBCCASAuthenticationException(AuthException.AUTH06,"验证码输入错误！");
		}
		//获取密码明文
		String password = this.getPassword(isSafe,credentials,request,verifycode);
		
		try{
			//实名用户使用sha1 + base64加密
			password = encIBankingPasswd(password);
		}catch(Exception e){
			log.error("Invoking method mallAuthenticationUsernamePassword : Encrypt the password failed !");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		
		String userName = credentials.getUsername();
		try{
			//实名用户登录，登录失败，去查非实名用户，如果存在非实名用户，进行记录失败次数
			return realNameVerify(credentials, request, password, LoginWay.MALL);
			
		}catch(ICBCCASAuthenticationException e){
			this.dealLoginLogInfoNew(null,userName, request, credentials,"0",LoginWay.NEW_MALL);
			throw e;
		}
		
		
	}
	
	
	/**
	 * @desc 网银和商城用户调用 统一通行证
	 * @param user
	 * @return
	 */
	private boolean mallAuthenticateUsernamePassword(
			UsernamePasswordCredentials credentials, String loginWay) throws AuthenticationException{
		/**
		 * 1.调用“用户身份接口”
		 * 2.如果是非实名用户，调用“非实名通行证认证接口”，
		 * 3.如果是实名用户，调用“实名通行证认证接口”
		 * 4.如果登录成功，补录数据，如果登录失败，判断是否存在同名，
		 * 5.如果存在同名，对非实名用户进行累计，同名冲突出现三次，提示登录遇到问题
		 */
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo verifycode = (VerifyCodeInfo) request.getSession().getAttribute("verifycode");
		//是否使用控件
		boolean isSafe = this.isUseSafeCtrl(credentials);
		//验证码处理
		boolean verifyFlag = verifyCode(isSafe,verifycode,request,credentials);
		if(verifyFlag != true){
			throw new ICBCCASAuthenticationException(AuthException.AUTH06,"验证码输入错误！");
		}
		//获取密码明文
		String password = this.getPassword(isSafe,credentials,request,verifycode);
		String SHA1Password = null;
		try{
			//实名用户使用sha1 + base64加密
			SHA1Password = encIBankingPasswd(password);
			//非实名用户使用堆成3DES加密
			password = encrypt(password);
		}catch(Exception e){
			log.error("Invoking method mallAuthenticationUsernamePassword : Encrypt the password failed !");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		
		String userName = credentials.getUsername();
		try{
			//获取用户类型（1-实名认证客户、 2-非实名认证客户 、3-未注册客户）
			String userType = this.checkUserType(userName, password);
			log.debug("Invoking method mallAuthenticationUsernamePassword : Login the userType is {}",userType);
			if("1".equals(userType)){
				//实名用户登录，登录失败，去查非实名用户，如果存在非实名用户，进行记录失败次数
				return realNameVerify(credentials, request, SHA1Password, loginWay);
			}else if("2".equals(userType)){
				//非实名用户登录
				return this.unRealNameVerify(credentials, request, password, loginWay);
			}else if("3".equals(userType)){
				throw new ICBCCASAuthenticationException(AuthException.AUTH01,"用户不存在！");
			}else{
				throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
			
		}catch(ICBCCASAuthenticationException e){
			this.dealLoginLogInfoNew(null,userName, request, credentials,"0",loginWay);
			throw e;
		}
		
		
	}
	
	public String encIBankingPasswd(String plainTextPwd) {
		byte SHAS2[] = new byte[21];
		byte input[] = new byte[1024];
		String encPwd=null;
		try{
			Base64 b64 = new Base64();
			input = plainTextPwd.getBytes();
			SHA1 sha1 = new SHA1();
			sha1.init();
			sha1.Update(input, input.length);
			SHAS2 = sha1.end();
			
			b64.startEncode();
			b64.encode(SHAS2, SHAS2.length);
			b64.endEncode();
			byte byEncoded[] = b64.getEncodedResult();
			encPwd = new String(byEncoded);
			
		}
		 catch (Exception e)
		 {
			return null;
		}
		return encPwd;
}

	
	
	/**
	 * @desc 网银和商城用户调用 统一通行证----MOBILE & PAD
	 * @param user
	 * @return
	 */
	private boolean mallAuthenticateUsernamePasswordMobile(
			UsernamePasswordCredentials credentials, String loginWay) throws AuthenticationException{
		/**
		 * 1.调用“用户身份接口”
		 * 2.如果是非实名用户，调用“非实名通行证认证接口”，
		 * 3.如果是实名用户，调用“实名通行证认证接口”
		 * 4.如果登录成功，补录数据，如果登录失败，判断是否存在同名，
		 * 5.如果存在同名，对非实名用户进行累计，同名冲突出现三次，提示登录遇到问题
		 */
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		
		String userpswd = credentials.getPassword();
		String passwordPre = credentials.getPasswordPre();
		String userName =(String) credentials.getUsername();
		Auth authInfo = null;
		MallLoginInfo loginInfo = null;
		final String changerule = credentials.getChangerule();
		final String rule = credentials.getRule();
		// 此处转换用于处理是否启用安全控件
		final String safeKeyBoard = credentials.getSafeKeyBoard();
		String password=null;
		
		// 密码处理  获取密码明文
		try{
			if (safeKeyBoard != null && YesOrNo.YES.equals(safeKeyBoard)
				&& !"".equals(changerule) && !"".equals(rule) ){//用安全键盘
				
				//密码键盘解密部分
				try{
					log.debug("使用安全键盘");
					readIni ri = new readIni();
					ri.initRead(CustomerAuthenticationHandler.class);
					if(!ri.readIniInfo()) 
						return false;
					Vector<String> keys = ri.getKeyCodes();
					String moduls = ri.getModuleStr();
					String priExp = ri.getPriStr();
					RSAPrivateKey key = RSA.getPrivateKey( moduls, priExp );
					String source = RSA.decryptByPrivateKey(userpswd, key );
					System.out.println(source);
					decodeSoftKeyBoardRule deSBR = new decodeSoftKeyBoardRule();
					
					deSBR.setKeys(keys);
					//初始化rule
					deSBR.initRules(rule);
			    	//初始化changeRule\
					deSBR.initChangeKeyCode(changerule);
					password = deSBR.decodeByRule(source); //明文密码

				} catch (Exception e) {
					log.error("error ==========> mobile 密码键盘解密  转换异常  : "+e.getMessage(),e);
				}

			} else {
				log.debug("使用系统键盘");
				String dictKey=request.getParameter("dictKey");
				HttpSession session=request.getSession();
				@SuppressWarnings("unchecked")
				List<String> dictList=(List<String>)session.getAttribute(dictKey);
				try {
					password=PasswordHelper.decrypt(passwordPre, dictList); //明文密码
				} catch (Exception e) {
					log.error(" error =====authService.getAuthByLoginId===username="
							+ userName + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
			}
			
		} catch (CoreAuthException e) {
			log.error(" error =========  AuthException   " + e.getCode()
					+ ":" + e.getMessage(),e);
			this.dealLoginLogInfo(authInfo, request, credentials,"0");
			
			if (AuthException.AUTH00.equals(e.getCode())) {
				throw new ICBCCASAuthenticationException(e.getCode(),
						"系统内部错误");
			} else {
				throw new ICBCCASAuthenticationException(e.getCode(),
						e.getMessage());
			}
		}	
		
		String SHA1Password = null;
		try{
			//实名用户使用sha1 + base64加密
			SHA1Password = encIBankingPasswd(password);
			//非实名用户使用堆成3DES加密
			password = encrypt(password);
		}catch(Exception e){
			log.error("Invoking method mallAuthenticationUsernamePassword : Encrypt the password failed !");
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		

		try{
			//获取用户类型（1-实名认证客户、 2-非实名认证客户 、3-未注册客户）
			String userType = this.checkUserType(userName, password);
			log.debug("Invoking method mallAuthenticationUsernamePassword : Login the userType is {}",userType);
			if("1".equals(userType)){
				//实名用户登录，登录失败，去查非实名用户，如果存在非实名用户，进行记录失败次数
				return realNameVerify(credentials, request, SHA1Password, loginWay);
			}else if("2".equals(userType)){
				//非实名用户登录
				return this.unRealNameVerify(credentials, request, password, loginWay);
			}else if("3".equals(userType)){
				throw new ICBCCASAuthenticationException(AuthException.AUTH01,"用户不存在！");
			}else{
				throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
			}
			
		}catch(ICBCCASAuthenticationException e){
			this.dealLoginLogInfoNew(null,userName, request, credentials,"0",loginWay);
			throw e;
		}
	}
	
	
	/**
	 * @desc 商城登录调用统一通行证
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticateUsernamePasswordInternal1(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		VerifyCodeInfo verifycode = (VerifyCodeInfo) request.getSession().getAttribute("verifycode");
		//是否使用控件
		boolean isSafe = this.isUseSafeCtrl(credentials);
		
		//验证码处理
		boolean verifyFlag = verifyCode(isSafe,verifycode,request,credentials);
		if(verifyFlag != true){
			throw new ICBCCASAuthenticationException(AuthException.AUTH06,"验证码输入错误！");
		}
		//获取密码明文
		String password = this.getPassword(isSafe,credentials,request,verifycode);
		try
		{
			password = encrypt(password);
		}
		catch(Exception e)
		{
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		
		return this.unRealNameVerify(credentials, request, password, LoginWay.MALL);
		 
	}
	

	private void logLoginInfo(String userId, HttpServletRequest request,String loginChannel,String loginWay) {
		MallLoginInfo  loginInfo = mallLoginInfoService.findMallLoginInfoByLoginId(userId);
		authService.loginSuccDeal(loginInfo ,request,userId,loginChannel,loginWay);
	}

	/**
	 * 登录流水日志
	 */
	public void dealLoginLogInfoNew(String userId,String userName, HttpServletRequest request,
			UsernamePasswordCredentials credentials,String flag,String loginWay) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MallLoginLogInfo loginLogInfo = new MallLoginLogInfo();

		String key = serialGeneratorMgr.getSerialKey(Constants.MALL_USER_LOGIN_LOG_INFO_LOGID).trim();
		//开始标记用于区分成功失败标记：E:error;S:success
		if("0".equals(flag)){
			key = "E"+key;
		}else{
			key = "S"+key;	
		}
		loginLogInfo.setLogId(key);
		loginLogInfo.setSessionId(request.getSession().getId());
		loginLogInfo.setClientIp(SystemMessageUtil.getIp(request));
		loginLogInfo.setLoginChannels(LoginChannels.INTERNET);
		loginLogInfo.setLoginWay(loginWay);
		loginLogInfo.setLoginDeviceBrowser(SystemMessageUtil
				.getBrowser(request));
		loginLogInfo.setLoginOs(SystemMessageUtil.getOs(request));
		String clientMac = Crypt.getPlainMac(request.getParameter("currentmac"));
		loginLogInfo.setClientMac(clientMac);
		try {
			loginLogInfo.setLastLoginTime((Date) dateFormat
					.parseObject(dateFormat.format(new Date())));
		} catch (ParseException e) {
		}
		if (userId != null) {
			loginLogInfo.setUserid(userId);
			loginLogInfo.setLoginName(userName);
		} else {
			loginLogInfo.setLoginName(credentials.getUsername());
		}
		try {
			applicationEventPublisher.publishEvent(new AuthServiceEvent(new Object(),loginLogInfo));
			//mallLoginLogInfoService.addMallLoginInfo(loginLogInfo);
		} catch (Exception e) {
			log.error(" error =========mallLoginLogInfoService.addMallLoginlogInfo   "
					+ e.getMessage() + " id=" + loginLogInfo.getLogId(),e);
		}
	}
	
	
	public String getSessionTimeOut() {
		// 单位是分钟，默认30分钟失效
		if (StringUtils.isBlank(sessionTimeOut)) {
			return String.valueOf(30 * 60);
		}
		return sessionTimeOut;
	}

	public void setSessionTimeOut(String sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}

	/**
	 * @desc 商城不走统一通行证电商的
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticateUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {

		UserCredentials tmp = null;
		String isSafe = null;
		String userpswd = credentials.getPassword();
		String jCaptcha = ((UserCredentials) credentials).getJ_captcha();
		try {
			// 此处转换用于处理是否启用安全控件
			tmp = (UserCredentials) credentials;
			if (tmp != null) {
				if (tmp.getIsSafe() != null
						&& YesOrNo.YES.equals(tmp.getIsSafe())) {
					userpswd = tmp.getLogonCardPass();
					jCaptcha = tmp.getVerifyCode();
					isSafe = YesOrNo.YES;
					log.info("info ==========>   authenticateUsernamePasswordInternal   userpswd : "+userpswd);
				} else {
					isSafe = YesOrNo.NO;
				}
			}
		} catch (Exception e) {
			log.error("error ==========> 转换异常  : "+e.getMessage(),e);
		}

		final String username = credentials.getUsername();
		
		String sessionId = RequestContextHolder.getRequestAttributes()
				.getSessionId();
		log.info("info==========>CAS LOGIN username:" + username);
		log.info("info==========>CAS LOGIN sessionId:" + sessionId);
		log.info("info==========>CAS LOGIN isSafe:" + isSafe);
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra)
				.getRequest();

		// 验证码明文
		VerifyCodeInfo verifycode = (VerifyCodeInfo) request.getSession()
				.getAttribute("verifycode");
		if (isSafe != null && YesOrNo.YES.equals(isSafe)) {
			// 有控件
			String safePluginType = request.getParameter("SafePluginType");
			int pluginType = 0;
			if(StringUtils.isNotBlank(safePluginType)){
				pluginType = Integer.parseInt(safePluginType);
			}
			 jCaptcha = Crypt.decodeFromSafePlugin(
					request.getParameter("randomId"), verifycode.verifyCode,
					jCaptcha,pluginType);
		} 
		boolean human = VerifyCode.isVerifycodeValid(verifycode, jCaptcha);
		if (!human) {
				log.error(" error==========>error VerifyCode  verifycode=" + verifycode
						+ " ==========> jCaptcha=" + jCaptcha);
				throw new ICBCCASAuthenticationException(AuthException.AUTH06,
						"验证码输入错误！");
		}
		// 根据登陆id获取认证信息
		// 如果账号无效直接返回false否则如果被锁定，判断锁定时间与当前系统时间相差的天天数，
		// 如果超过一天，自动解锁，继续后边的校验，否则返回false
		// 验证密码如果通过返回true，否则更新个人登陆信息表（密码出错次数，是否锁定）
		Auth authInfo = null;
		MallLoginInfo loginInfo = null;
		try {
			Pattern pa = Pattern.compile("^[0-9]{11}$");
			Matcher match = pa.matcher(username);
			if(match.find())
			{
				//用户输入内容为手机号
				try{
					List<MallUserInfo> mallUserInfoByMobile=mallUserInfoService.getMallUserInfosByMobile(username);
					if(mallUserInfoByMobile==null){
						log.error(" error =====mallUserInfoByMobile is  null====");
						throw new CoreAuthException(AuthException.AUTH01, "用户名或密码错误");
					}else if(mallUserInfoByMobile.size()!=1){
						log.error(" error =====mobile not fount,or too many records===username="
								+ username);
						throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
					}else{
						authInfo = authService.getAuthByUserId(null, null, mallUserInfoByMobile.get(0).getUserid());
					}
				}catch (EmallServiceException e) {
					log.error(" error =====login by mobile ="
							+ username + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
			}else{
				//用户输入内容为loginId
				try {
					authInfo = authService.getAuthByLoginId(null, null, username);
				} catch (EmallServiceException e) {
					log.error(" error =====authService.getAuthByLoginId===username="
							+ username + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
			}
			if (authInfo == null) {
				log.error(" error =====authInfo is  null====");
				throw new CoreAuthException(AuthException.AUTH01, "用户名或密码错误");
			}
			// 密码处理
			if (isSafe != null && YesOrNo.YES.equals(isSafe)) {
				String safePluginType = request.getParameter("SafePluginType");
				int pluginType = 0;
				if(StringUtils.isNotBlank(safePluginType)){
					pluginType = Integer.parseInt(safePluginType);
				}
				userpswd = Crypt.encryptStringEbank(authInfo.getUserid(),
						request.getParameter("randomId"),
						verifycode.verifyCode, userpswd,pluginType);
			} else {
				String dictKey=request.getParameter("dictKey");
				HttpSession session=request.getSession();
				List<String> dictList=(List<String>)session.getAttribute(dictKey);
				String orgin_pwd=null;
				try {
					orgin_pwd=PasswordHelper.decrypt(userpswd, dictList);
				} catch (Exception e) {
					log.error(" error =====authService.getAuthByLoginId===username="
							+ username + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
				
				userpswd = Crypt.encryptStringOther(orgin_pwd,
						authInfo.getUserid());
			}
			if (userpswd == null) {
				log.error(" error =========Crypt  AuthException"
						+ AuthException.AUTH00);
				throw new CoreAuthException(AuthException.AUTH00, "用户名或密码错误");
			}
			
			
			LoginTransObject loginTransObject = new LoginTransObject();
			loginTransObject.setAuthInfo(authInfo);
			loginTransObject.setLoginId(username);
			loginTransObject.setLoginInfo(loginInfo);
			loginTransObject.setRequest(request);
			loginTransObject.setUserpswd(userpswd);

			boolean retflag = authService.loginService(loginTransObject,LoginChannels.INTERNET,LoginWay.MALL);
			log.info("info ==========> authService.loginService retflag:" + retflag);
			if (retflag) {
				this.dealLoginLogInfo(authInfo, request, credentials,"1");
				HttpSession session = request.getSession(false);
				session.invalidate();
				return retflag;
			} else {
				throw new CoreAuthException(AuthException.AUTH05, "用户名或密码错误！");
			}
		} catch (CoreAuthException e) {
			log.error(" error =========  AuthException   " + e.getCode()
					+ ":" + e.getMessage(),e);
			this.dealLoginLogInfo(authInfo, request, credentials,"0");
			
			if (AuthException.AUTH00.equals(e.getCode())) {
				throw new ICBCCASAuthenticationException(e.getCode(),
						"系统内部错误");
			} else {
				throw new ICBCCASAuthenticationException(e.getCode(),
						e.getMessage());
			}
		}
	}
	/**
	 * @return true if the credentials are not null and the credentials class is
	 *         equal to the class defined in classToSupport.
	 */
	public final boolean supports(final Credentials credentials) {
		if (((UsernamePasswordCredentials) credentials).getCis() != null) {
			return true;
		}
		return credentials != null
				&& ((this.classToSupport.equals(credentials.getClass()) || (this.classToSupport
						.isAssignableFrom(credentials.getClass()))
						&& this.supportSubClasses));
	}

	/**
	 * 登录流水日志
	 */
	public void dealLoginLogInfo(Auth auth, HttpServletRequest request,
			UsernamePasswordCredentials credentials,String flag) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MallLoginLogInfo loginLogInfo = new MallLoginLogInfo();

		String key = serialGeneratorMgr.getSerialKey(Constants.MALL_USER_LOGIN_LOG_INFO_LOGID).trim();
		//开始标记用于区分成功失败标记：E:error;S:success
		if("0".equals(flag)){
			key = "E"+key;
		}else{
			key = "S"+key;	
		}
		loginLogInfo.setLogId(key);
		loginLogInfo.setSessionId(request.getSession().getId());
		loginLogInfo.setClientIp(SystemMessageUtil.getIp(request));
		loginLogInfo.setLoginChannels(LoginChannels.INTERNET);
		loginLogInfo.setLoginWay(LoginWay.MALL);
		loginLogInfo.setLoginDeviceBrowser(SystemMessageUtil
				.getBrowser(request));
		loginLogInfo.setLoginOs(SystemMessageUtil.getOs(request));
		String clientMac = Crypt.getPlainMac(request.getParameter("currentmac"));
		loginLogInfo.setClientMac(clientMac);
		try {
			loginLogInfo.setLastLoginTime((Date) dateFormat
					.parseObject(dateFormat.format(new Date())));
		} catch (ParseException e) {
		}
		if (auth != null) {
			loginLogInfo.setUserid(auth.getUserid());
			loginLogInfo.setLoginName(auth.getLoginId());
		} else {
			loginLogInfo.setLoginName(credentials.getUsername());
		}
		try {
			applicationEventPublisher.publishEvent(new AuthServiceEvent(new Object(),loginLogInfo));
			//mallLoginLogInfoService.addMallLoginInfo(loginLogInfo);
		} catch (Exception e) {
			log.error(" error =========mallLoginLogInfoService.addMallLoginlogInfo   "
					+ e.getMessage() + " id=" + loginLogInfo.getLogId(),e);
		}
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}   
	
	/**
	 * @desc 手机登录原流程
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticatePhoneUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		log.error("========================我是手机=======我已经登录成功===old=========================loginWay = "+credentials.getLoginWay());
		CommomProperty instance = CommomProperty.getDBManager();
		String userpswd = credentials.getPassword();
		String passwordPre = credentials.getPasswordPre();
		Auth authInfo = null;
		MallLoginInfo loginInfo = null;
		
		final String username = credentials.getUsername();
		final String changerule = credentials.getChangerule();
		final String rule = credentials.getRule();
		
		// 此处转换用于处理是否启用安全控件
		
		final String safeKeyBoard = credentials.getSafeKeyBoard();
		String sessionId = RequestContextHolder.getRequestAttributes()
				.getSessionId();
		log.info("info==========>CAS LOGIN username:" + username);
		log.info("info==========>CAS LOGIN sessionId:" + sessionId);
		log.info("info==========>CAS LOGIN isSafe:" + safeKeyBoard);
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra)
				.getRequest();

		try {
			Pattern pa = Pattern.compile("^[0-9]{11}$");
			Matcher match = pa.matcher(username);
			if(match.find())
			{
				//用户输入内容为手机号
				try{
					List<MallUserInfo> mallUserInfoByMobile=mallUserInfoService.getMallUserInfosByMobile(username);
					if(mallUserInfoByMobile==null){
						log.error(" error =====mallUserInfoByMobile is  null====");
						throw new CoreAuthException(AuthException.AUTH01, "用户名或密码错误");
					}else if(mallUserInfoByMobile.size()!=1){
						log.error(" error =====mobile not fount,or too many records===username="
								+ username);
						throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
					}else{
						authInfo = authService.getAuthByUserId(null, null, mallUserInfoByMobile.get(0).getUserid());
					}
				}catch (EmallServiceException e) {
					log.error(" error =====login by mobile ="
							+ username + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
			}else{
				//用户输入内容为loginId
				try {
					authInfo = authService.getAuthByLoginId(null, null, username);
				} catch (EmallServiceException e) {
					log.error(" error =====authService.getAuthByLoginId===username="
							+ username + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}	
			}
			
			if (authInfo == null) {
				log.error(" error =====authInfo is  null====");
				throw new CoreAuthException(AuthException.AUTH01, "用户名或密码错误");
			}
			// 密码处理
			if (safeKeyBoard != null && YesOrNo.YES.equals(safeKeyBoard) 
					&& !"".equals(changerule) && !"".equals(rule) ) {
				
				//密码键盘解密部分
				try{
					log.debug("使用安全键盘");
					readIni ri = new readIni();
					ri.initRead(CustomerAuthenticationHandler.class);
					if(!ri.readIniInfo()) 
						return false;
					Vector<String> keys = ri.getKeyCodes();
					String moduls = ri.getModuleStr();
					String priExp = ri.getPriStr();
					RSAPrivateKey key = RSA.getPrivateKey( moduls, priExp );
					String source = RSA.decryptByPrivateKey(userpswd, key );
					System.out.println(source);
					decodeSoftKeyBoardRule deSBR = new decodeSoftKeyBoardRule();
					
					deSBR.setKeys(keys);
					//初始化rule
					deSBR.initRules(rule);
			    	//初始化changeRule\
					deSBR.initChangeKeyCode(changerule);
					String data = deSBR.decodeByRule(source);
					deSBR.cleanMemory();
					
					//加密
					userpswd = Crypt.encryptStringOther(data,
							authInfo.getUserid());
					
				} catch (Exception e) {
					log.error("error ==========> 转换异常  : "+e.getMessage(),e);
				}
			
			} else {
				log.debug("使用系统键盘");
				String dictKey=request.getParameter("dictKey");
				HttpSession session=request.getSession();
				@SuppressWarnings("unchecked")
				List<String> dictList=(List<String>)session.getAttribute(dictKey);
				String orgin_pwd=null;
				try {
					orgin_pwd=PasswordHelper.decrypt(passwordPre, dictList);
				} catch (Exception e) {
					log.error(" error =====authService.getAuthByLoginId===username="
							+ username + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
				
				
				userpswd = Crypt.encryptStringOther(orgin_pwd,
						authInfo.getUserid());
			}
			if (userpswd == null) {
				log.error(" error =========Crypt  AuthException"
						+ AuthException.AUTH00);
				throw new CoreAuthException(AuthException.AUTH00, "用户名或密码错误");
			}
			LoginTransObject loginTransObject = new LoginTransObject();
			loginTransObject.setAuthInfo(authInfo);
			loginTransObject.setLoginId(username);
			loginTransObject.setLoginInfo(loginInfo);
			loginTransObject.setRequest(request);
			loginTransObject.setUserpswd(userpswd);

			String lastLoginWay = "";
			if("1".equals(credentials.getLoginWay())){
				lastLoginWay = LoginWay.MOBILEEMALL;
			}else{
				lastLoginWay = LoginWay.PAD;
			}
			boolean retflag = authService.loginService(loginTransObject,LoginChannels.MOVEINTERNET,lastLoginWay);
			log.info("info ==========> authService.loginService retflag:" + retflag);
			if (retflag) {
				this.dealLoginLogInfo(authInfo, request, credentials,"1");
				HttpSession session = request.getSession(false);
				session.invalidate();
				return retflag;
			} else {
				throw new CoreAuthException(AuthException.AUTH05, "用户名或密码错误！");
			}
		} catch (CoreAuthException e) {
			log.error(" error =========  AuthException   " + e.getCode()
					+ ":" + e.getMessage(),e);
			this.dealLoginLogInfo(authInfo, request, credentials,"0");
			
			if (AuthException.AUTH00.equals(e.getCode())) {
				throw new ICBCCASAuthenticationException(e.getCode(),
						"系统内部错误");
			} else {
				throw new ICBCCASAuthenticationException(e.getCode(),
						e.getMessage());
			}
		}
	}
	
	
	/**
	 * 手机----新
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticatePhoneUsernamePasswordInternal1(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		log.error("========================我是手机=======我已经登录成功===UA=========================loginWay = "+credentials.getLoginWay());
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();

		String userpswd = credentials.getPassword();
		String passwordPre = credentials.getPasswordPre();
		String userName =(String) credentials.getUsername();
		Auth authInfo = null;
		final String changerule = credentials.getChangerule();
		final String rule = credentials.getRule();
		// 此处转换用于处理是否启用安全控件
		final String safeKeyBoard = credentials.getSafeKeyBoard();
		String password=null;
		
		try{
			// 密码处理  获取密码明文
			if (safeKeyBoard != null && YesOrNo.YES.equals(safeKeyBoard)
				&& !"".equals(changerule) && !"".equals(rule) ){//用安全键盘
				//密码键盘解密部分
				try{
					log.debug("使用安全键盘");
					readIni ri = new readIni();
					ri.initRead(CustomerAuthenticationHandler.class);
					if(!ri.readIniInfo()) 
						return false;
					Vector<String> keys = ri.getKeyCodes();
					String moduls = ri.getModuleStr();
					String priExp = ri.getPriStr();
					RSAPrivateKey key = RSA.getPrivateKey( moduls, priExp );
					String source = RSA.decryptByPrivateKey(userpswd, key );
					System.out.println(source);
					decodeSoftKeyBoardRule deSBR = new decodeSoftKeyBoardRule();
					deSBR.setKeys(keys);					//初始化rule
					deSBR.initRules(rule);					//初始化changeRule
					deSBR.initChangeKeyCode(changerule);
					password = deSBR.decodeByRule(source); //明文密码
				} catch (Exception e) {
					log.error("error ==========> mobile 密码键盘解密  转换异常  : "+e.getMessage(),e);
				}
			} else {
				log.debug("使用系统键盘");
				String dictKey=request.getParameter("dictKey");
				HttpSession session=request.getSession();
				@SuppressWarnings("unchecked")
				List<String> dictList=(List<String>)session.getAttribute(dictKey);
				try {
					password=PasswordHelper.decrypt(passwordPre, dictList); //明文密码
				} catch (Exception e) {
					log.error(" error =====authService.getAuthByLoginId===username="
							+ userName + "" + e.getMessage(),e);
					throw new CoreAuthException(AuthException.AUTH00, "系统内部错误");
				}
			}
			
		} catch (CoreAuthException e) {
			log.error(" error =========  AuthException   " + e.getCode()
					+ ":" + e.getMessage(),e);
			this.dealLoginLogInfo(authInfo, request, credentials,"0");
			
			if (AuthException.AUTH00.equals(e.getCode())) {
				throw new ICBCCASAuthenticationException(e.getCode(),
						"系统内部错误");
			} else {
				throw new ICBCCASAuthenticationException(e.getCode(),
						e.getMessage());
			}
		}	
		
		try
		{
			password = encrypt(password);
		}
		catch(Exception e)
		{
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
		//登录渠道
		String lastLoginWay = "";
		if("1".equals(credentials.getLoginWay())){
			lastLoginWay = LoginWay.MOBILEEMALL;
		}else{
			lastLoginWay = LoginWay.PAD;
		}
		//非实名用户登录处理
		return unRealNameVerify(credentials, request, password, lastLoginWay);
	
			
	}

	
	/**
	 * @desc 实名用户登录
	 *			1，实名接口调用后，登录成功，（如果没有补注册非实名，接口内部完成B2C_AUTH补注册等），返回消息码0成功，以及返回实名用户的ciscode，补注册生成的userid等相关信息。
	 *			2，实名接口调用后，登录失败，且存在用户名/手机号重复的非实名用户，则返回错误码1 卡号/用户名或者密码不正确，返回isMalluser 1-非实名表中存在用户记录，返回loginType（用户名/手机号），返回空的ciscode 和 非实名用户的userid等相关信息。
	 * @param credentials
	 * @param request
	 * @param userName
	 * @param password
	 * @param lastLoginWay
	 * @return
	 * @throws ICBCCASAuthenticationException
	 */
	private boolean realNameVerify(final UsernamePasswordCredentials credentials,
			HttpServletRequest request, String password,
			String lastLoginWay) throws ICBCCASAuthenticationException {
		//获取用户名
		String userName = credentials.getUsername();
		//接口调用
		UAOutputRealNameQuery realNameQuery = null;
		try{
			realNameQuery = this.realNameQuery(userName, password);

		}catch(ICBCCASAuthenticationException e){
			log.error("Invoking method realNameVerify : Query realName failed",e);
			this.dealLoginLogInfoNew(null,userName, request, credentials,"0",lastLoginWay);
			throw e;
		}
		String userId = "";
		String retCode = realNameQuery.getPublicInfo().getRetCode();
		
		// 如果非实名认证接口查询失败，在商城这里记录失败一次
		
		boolean doubleFlag = checkUserNameIfDouble(realNameQuery,userName);
		
		if(doubleFlag){
			throw new ICBCCASAuthenticationException(AuthException.AUTH15, "卡号/用户名或者密码不正确！");
		}
		/*
		 * 	0登录成功
		 *  1卡号/用户名或者密码不正确
		 *	2暂不支持理财e卡
		 *	3您的卡片状态不正常
		 *	4您的密码需要重置
		 *	5国际卡需要激活
		 *	6您需要重新将该信用卡添加或注册为个人网银注册卡
		 *	7尊敬的客户，您的预留验证信息为空	
		 *	8暂不支持自助注册国际卡
		 *	9999其他错误，具体错误码在retmsg中返回
		 */
		if("0".equals(retCode)){
			
			// 判断用户登录类型，1卡号2别名 4-账号 5-年金帐户 8.手机号
			
			String loginType = realNameQuery.getPrivateInfo().getLoginType();
			if("1".equals(loginType) || "4".equals(loginType) || "5".equals(loginType)){
					credentials.setUsername(realNameQuery.getPrivateInfo().getAliasName());
			}
			
			try{
				// 数据查询回来之后，进行网银数据的补录处理，模仿remoteLoginAction
				this.realNameQueryInfo(realNameQuery,request);
			}catch (ICBCCASAuthenticationException e) {
				throw e;
			}
			return true;
		} else if("1".equals(retCode)){
			this.log.error("Invoking method realNameQuery : Invoking failed, username or password is error!");
			this.dealLoginLogInfoNew(userId, userName,request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH01, "卡号/用户名或者密码不正确！");
		}else if("2".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH08, "暂不支持理财e卡 ！");
		}else if("3".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH09, "您的卡片状态不正常！");
		}else if("4".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH10, "您的密码需要重置！");
		}else if("5".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH11, "国际卡需要激活！");
		}else if("6".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH12, "您需要重新将该信用卡添加或注册为个人网银注册卡！");
		}else if("7".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH13, "尊敬的客户，您的预留验证信息为空！");
		}else if("8".equals(retCode)) {
			userId =realNameQuery.getPrivateInfo().getUserId();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH14, "暂不支持自助注册国际卡！");
		}else{
			this.log.error("Invoking method realNameQuery : Invoking failed, return retCode is {} , return retMsg is {} ", realNameQuery.getPublicInfo().getRetCode(), realNameQuery.getPublicInfo().getRetMsg());
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误(9999)！");
		}
	}
	
	
	/**
	 * @desc 判断该用户是否登录成功，是否存在重名的用户
	 * @param realNameQuery
	 * @return
	 */
	private boolean checkUserNameIfDouble(UAOutputRealNameQuery realNameQuery,String userName) {

		//1，实名接口调用后，登录成功，（如果没有补注册非实名，接口内部完成B2C_AUTH补注册等），返回消息码0成功，以及返回实名用户的ciscode，补注册生成的userid等相关信息。
		//2，实名接口调用后，登录失败，且存在用户名/手机号重复的非实名用户，则返回错误码1 卡号/用户名或者密码不正确，返回isMalluser 1-非实名表中存在用户记录，返回loginType（用户名/手机号），返回空的ciscode 和 非实名用户的userid等相关信息。
		
		String retCode = realNameQuery.getPublicInfo().getRetCode();
		String cisCode = realNameQuery.getPrivateInfo().getMainCIS();
		String userId = realNameQuery.getPrivateInfo().getUserId();
		String isMallUser = realNameQuery.getPrivateInfo().getIsMallUser();
		//用户登录成功，不需要核对是否存在同名用户
		if("0".equals(retCode) || StringUtils.isNotBlank(cisCode) || !("1".equals(isMallUser))){// 需要判断isMallUser是否为1
			return false;
		}else{
			 
			/*
			 * try {
				Auth auth = new Auth();
				auth.setUserid(userId);
				auth = authService.selectAuthByUserId(auth);
				
				Integer doubleCount = auth.getDoubleCount();

				if((doubleCount + "").contains("3")){
					return true;
				}else{
					
					Pattern pa = Pattern.compile("^[0-9]{11}$");
					Matcher match = pa.matcher(userName);
					if(match.find())
					{
						doubleCount = doubleCount + 10;
					}else{
						doubleCount = doubleCount + 1;
					}
					
					auth.setDoubleCount(doubleCount);
					authService.updateAuth(null, null, auth);
				}
				
			} catch (EmallServiceException e) {
				log.error("Invoking method checkUserNameIfDouble : invoking failed!",e);
				return true;
			}
			*/
			
			return true;
			}
		
	}

	/**
	 * @desc 非实名用户登录
	 * @param credentials
	 * @param request
	 * @param userName
	 * @param password
	 * @param lastLoginWay
	 * @return
	 * @throws ICBCCASAuthenticationException
	 */
	private boolean unRealNameVerify(final UsernamePasswordCredentials credentials,
			HttpServletRequest request, String password,
			String lastLoginWay) throws ICBCCASAuthenticationException {
		//获取用户名
		String userName = credentials.getUsername();
		//接口调用
		UAOutputLogin uAOutputLogin = null;
		try{
			uAOutputLogin = this.unRealNameQuery(userName, password);
		}catch(ICBCCASAuthenticationException e){
			this.dealLoginLogInfoNew(null,userName, request, credentials,"0",lastLoginWay);
			throw e;
		}
		String userId = "";
		String retCode = uAOutputLogin.getPublicInfo().getRetCode();
		/*
		 *  0--成功
			1--登录名不存在
			2--登录密码错误
			3--帐户被锁定
			4--用户无效
			5-存储过程出错
			9999-其他错误
		 */
		if("0".equals(retCode)){
			userId =uAOutputLogin.getPrivateInfo().getUserid();
			//userName改取接口返回的LoginID值
			String userNameTmp = uAOutputLogin.getPrivateInfo().getLoginID();
			credentials.setUsername(uAOutputLogin.getPrivateInfo().getLoginID());
			//存量数据处理
			try{
				this.unRealNameDealLocalData( uAOutputLogin, userNameTmp);
			}catch(ICBCCASAuthenticationException e){
				this.log.error("==>dealLocalData ERROR:",e);
				this.dealLoginLogInfoNew(userId,userNameTmp, request, credentials,"0",lastLoginWay);
				throw e;
			}
			//记录登录信息 b2c_mall_login_info
			String loginChannel = null;
			if(LoginWay.MALL.equals(lastLoginWay)){
				loginChannel = LoginWay.MALL;
			}else{
				loginChannel = LoginChannels.MOVEINTERNET;
			}
			
			this.logLoginInfo(userId,request,loginChannel,lastLoginWay);
			//登录流水处理成功
			this.dealLoginLogInfoNew(userId, userNameTmp,request, credentials,"1",lastLoginWay);
			return true;
		}
		else if("1".equals(retCode)||"2".equals(retCode))
		{
			this.dealLoginLogInfoNew(userId, userName,request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH01, "用户名或密码错误！");
		}
		else if("3".equals(retCode)) //账户被锁定
		{
			userId =uAOutputLogin.getPrivateInfo().getUserid();
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH04, "账户被锁定，禁止登录。该账户将在锁定24小时之后解锁！");
		}
		else{
			this.dealLoginLogInfoNew(userId,userName, request, credentials,"0",lastLoginWay);
			throw new ICBCCASAuthenticationException(AuthException.AUTH00,"系统内部错误");
		}
	}
	
	
	
	/**
	 * 判断商户用户名密码信息
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticateMerchantUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {

		
		return true;
		
	}
	
	/**
	 * 验证商户短信验证码
	 * @param credentials
	 * @return
	 * @throws AuthenticationException
	 */
	private boolean authenticateMerchantPhoneCodeInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		return true;
	}
	
	
	/**
	 * @desc 是否启用密码控件
	 * @param credentials
	 * @return
	 */
	private boolean isUseSafeCtrl(UsernamePasswordCredentials credentials){
		UserCredentials tmp = null;
		boolean isSafe = false;
		try {
			// 此处转换用于处理是否启用安全控件
			tmp = (UserCredentials) credentials;
			if (tmp != null) {
				if (tmp.getIsSafe() != null
						&& YesOrNo.YES.equals(tmp.getIsSafe())) {
					isSafe = true;
				} else {
					isSafe = false;
				}
			}
		} catch (Exception e) {
			log.error("error ==========> 转换异常  : "+e.getMessage(),e);
		}
		return isSafe;
	}
	
	
	
	
	
	
	
	public Gtcg getGtcg() {
		return gtcg;
	}

	public void setGtcg(Gtcg gtcg) {
		this.gtcg = gtcg;
	}
	
	private MallUserInfoService mallUserInfoService;
	
	public MallUserInfoService getMallUserInfoService() {
		return mallUserInfoService;
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

	public AuthService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}

	public MallLoginInfoService getMallLoginInfoService() {
		return mallLoginInfoService;
	}

	public void setMallLoginInfoService(
			MallLoginInfoService mallLoginInfoService) {
		this.mallLoginInfoService = mallLoginInfoService;
	}

	public MallLoginLogInfoService getMallLoginLogInfoService() {
		return mallLoginLogInfoService;
	}

	public void setMallLoginLogInfoService(
			MallLoginLogInfoService mallLoginLogInfoService) {
		this.mallLoginLogInfoService = mallLoginLogInfoService;
	}
	
	public final String getName() {
		return this.name;
	}

	public CollectionOfUserInformation getCollectionOfUserInformation() {
		return collectionOfUserInformation;
	}

	public void setCollectionOfUserInformation(
			CollectionOfUserInformation collectionOfUserInformation) {
		this.collectionOfUserInformation = collectionOfUserInformation;
	}

	public CiscodeService getCiscodeService() {
		return ciscodeService;
	}

	public void setCiscodeService(CiscodeService ciscodeService) {
		this.ciscodeService = ciscodeService;
	}

}
