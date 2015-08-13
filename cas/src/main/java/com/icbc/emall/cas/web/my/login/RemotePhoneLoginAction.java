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

import com.icbc.common.utils.ApplicationConfigUtils;
import com.icbc.emall.EmallServiceException;
import com.icbc.emall.SystemMessageUtil;
import com.icbc.emall.auth.model.Auth;
import com.icbc.emall.auth.service.AuthService;
import com.icbc.emall.bp.service.BpCommonService;
import com.icbc.emall.cache.CacheManager;
import com.icbc.emall.ciscode.model.Ciscode;
import com.icbc.emall.ciscode.service.CiscodeService;
import com.icbc.emall.common.dao.BlackListDAO;
import com.icbc.emall.common.model.Address;
import com.icbc.emall.common.service.AddressService;
import com.icbc.emall.common.utils.CommomProperty;
import com.icbc.emall.common.utils.Crypt;
import com.icbc.emall.common.utils.Globe.AuthType;
import com.icbc.emall.common.utils.Globe.LoginWay;
import com.icbc.emall.common.utils.Globe.MallUserType;
import com.icbc.emall.common.utils.Globe.SceneType;
import com.icbc.emall.common.utils.Globe.YesOrNo;
import com.icbc.emall.common.utils.PubUtils;
import com.icbc.emall.common.utils.SpringContextLoaderListener;
import com.icbc.emall.ebankuseraddr.model.EBankUserAddr;
import com.icbc.emall.ebankuseraddr.service.EbankUserAddrService;
import com.icbc.emall.lottery.model.LotteryUserinfo;
import com.icbc.emall.mall.model.MallLoginInfo;
import com.icbc.emall.mall.service.MallLoginInfoService;
import com.icbc.emall.mall.service.MallLoginLogInfoService;
import com.icbc.emall.member.model.MallUserInfo;
import com.icbc.emall.member.service.EBankSavedUserService;
import com.icbc.emall.member.service.MallUserInfoService;
import com.icbc.emall.merchant.dao.AreacodeMapDAO;
import com.icbc.emall.merchant.model.AreacodeMap;
import com.icbc.emall.merchant.service.AreacodeMapService;
import com.icbc.emall.util.BaseServiceData;
import com.icbc.emall.util.gtcg.Gtcg;
import com.icbc.emall.util.gtcg.GtcgIOException;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputPassQueryBankInfo;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputQueryUserInfo;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputSignUp;
import com.icbc.emall.util.gtcg.model.input.unifiedAuth.UAInputUpdateInfo;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.passQueryBankInfo.UAOutputPassQueryBankInfo;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.queryInfo.UAOutputQueryInfo;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.signUp.UAOutputSignUp;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.updateInfo.UAOutputUpdateInfo;
import com.icbc.emall.util.keygen.Constants;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;
import com.icbc.emall.utils.StringUtil;

/**
 * 身份认证服务器跳转回商城服务器
 * 所使用的场景，都登录成功后，通过CAS生成登录信息，且需要走到login-other-webflow.xml时，会经过此类，场景包含
 * 1、以网银身份登录商城成功后跳转回商城
 * 2、注册成功后点击去商城逛逛，以注册信息登录成功后跳转回商城(此时会带有userid，用以告诉CAS，以哪个用户身份登录)
 * 3、在我的商城点击实名认证成功后跳转回商城(此时会带有userid，用以告诉CAS,如果需要补录网银信息，需要往哪个用户信息中实录)
 * 4、未登录时，点击立即购买或去结点时，先跳转到网银登录成功后跳转回商城
 * @author kfzx-buzc
 *
 */
public class RemotePhoneLoginAction extends AbstractAction {
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
	private MallLoginLogInfoService mallLoginLogInfoService;
	private BpCommonService bpCommonService;
	private AddressService addressService;
	private EbankUserAddrService eBankUserAddrService;
	private AreacodeMapDAO areacodeMapDAO;
	private CiscodeService ciscodeService;
	private static HashMap hashMap;
	private BlackListDAO blackListDAO;
	private UAOutputPassQueryBankInfo uAOutputPassQueryBankInfo;
	private Gtcg gtcg;
	
	public Gtcg getGtcg() {
		return gtcg;
	}

	public void setGtcg(Gtcg gtcg) {
		this.gtcg = gtcg;
	}

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
		
		/*
		 * sceneType逻辑：
		 * （1）emallField不为空，从emallField里取
		 * （2）emallField为空，从trandata里取，trandata里没有，置为1
		 * 
		 */
		
		log.debug(" RemotePhoneLoginAction----------"+new Date()+"----------------start");
		String eMallFieldEncryped=null;
		log.debug("base service value:" + BaseServiceData.baseServiceEnable);
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		eMallFieldEncryped = request.getParameter("eMallField");
		eMallFieldEncryped = (eMallFieldEncryped==null?"":eMallFieldEncryped.trim());
		String eMallField = null;
		String servInEField = "";
		
		//对于eMallField中的数据进行3DES解密
		try {
			eMallField = Crypt.decrypt(eMallFieldEncryped, "UTF-8", 1, 0);
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error("eMallField error: " + eMallFieldEncryped);
			context.getFlowScope().put("error","eMallField error");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}
		
		String b2cReqFlag = request.getParameter("b2cReqFlag");
		b2cReqFlag = b2cReqFlag==null?"":b2cReqFlag.trim();
		if("1".equals(b2cReqFlag))
			context.getFlowScope().put("b2cReqFlag","1");
		String jesion = new SecurityCheckUtils().getRemoteTranData(request);
		JSONObject json = null;
		if (jesion != null && !"".equals(jesion)) {
			json = JSONObject.fromObject(jesion);
		} else {
			log.error(" json error: " + json);
			context.getFlowScope().put("error", "json is null");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return result("submit");
		}
		
		
		String channelIdentifier = (String) json.get("channelIdentifiler");

		UsernamePasswordCredentials user = new UsernamePasswordCredentials();
		context.getFlowScope().put(
				"warnCookieValue",
				Boolean.valueOf(this.warnCookieGenerator
						.retrieveCookieValue(request)));
		
		
		
		JSONObject json_eMallField = null;
		String sceneType = "";
		if(eMallField.equals("")){
			
			String scene = (String) json.get("sceneType");
			scene = scene==null?"":scene.trim();
			if(!"".equals(scene)){
				sceneType = scene;
			}else{
			//从手机网银登录商城成功后跳转回商城，此场景将不会携带eMallField，做特殊处理
				sceneType = SceneType.LOGIN_AFTER_EBANKP_PHONE_IM;
			}
			
			if(json.get("service")!=null){
				servInEField = (String)json.get("service");
				servInEField = servInEField==null?"":servInEField.trim();
			}
			
		}else{
			json_eMallField = JSONObject.fromObject(eMallField);
			//获取eMallField中的sceneType
			if(json_eMallField.get("sceneType")!=null)
				sceneType = (String)json_eMallField.get("sceneType");
			if(json_eMallField.get("service")!=null){
				servInEField = (String)json_eMallField.get("service");
				servInEField = servInEField==null?"":servInEField.trim();
			}

		}
		context.getFlowScope().put("sceneType", sceneType);
		log.debug("=============sceneType====="+sceneType);
		
		
		/*
		//String jesion = "{\"provinceAddr\":\"北京市\",\"logonNum\":\"9558880200005890503\",\"mobileNum\":\"13810900003\",\"custmerMac\":\"00-21-CC-BF-5B-FF\",\"channelIdentifier\":\"82.201.62.18.20160601085343.2401\",\"custmerIp\":\"82.201.93.144\",\"conarea\":\"010\",\"mainAreaCode\":\"0200\",\"mainCIS\":\"020006202156614\",\"timeStamp\":\"20160601085343\",\"postalcode\":\"100193\",\"conexin\":\"111\",\"aliasName\":\"lisimiao\",\"countyAddr\":\"海淀区\",\"homeTel\":\"\",\"teleNum\":\"82706096\",\"CustXingji\":\"5\",\"cityAddr\":\"北京市\",\"commAddr\":\"东北旺西路8号院中关村软件园16号楼\",\"companyTel\":\"\",\"CustName\":\"李四苗\"}";
		"{"sessionid":"","cisno":"020000300004173","timestamp":"1624607362801","deviceid":"61653639313932363864343835646433","funccode":"","service":"http://icbc.huacai.com/touch/","sceneType":"15","channelIdentifiler":"EP2020062200005502.202106251549220801"}"
		 
		*/
	  if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE_IM) ){
			//场景5：从手机网银登录商城成功后跳转回商城
			if(!scene5(context,json,user,sceneType,eMallFieldEncryped))
				return result("submit");
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
		context.getFlowScope().put("error","user is null");
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


		Service service = WebUtils.getService(this.argumentExtractors, context);
		if(service == null ){
			CommomProperty instance = CommomProperty.getDBManager();
			String url = "";
			if(!servInEField.equals("")) //如果emallField中有service
				url = servInEField;
			else
			{
				//手机IM中不存在service，跳转到手机error页面
//				url=instance.getsmsProperty("cas.mobile.loginErrorPage");
				context.getFlowScope().put("error", "service not exists  ---");
				return result("submit");
				 
			}
			service = this.createServiceFrom(url, null);
		}
		if (service == null) {
			service = WebUtils.getService(context);
		}

		if (service != null && log.isDebugEnabled()) {
			log.debug("Placing service in FlowScope: " + service.getId());
		}

		context.getFlowScope().put("service", service);

		CacheManager.getInstance().putCache(channelIdentifier,"channelIdentifier");
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
		 * @desc 新用户注册到统一认证
		 * @param areacodeMap
		 * @param ciscode
		 * @param aliasname
		 * @param mobile
		 * @return
		 */
	private UAOutputSignUp signUpUA(AreacodeMap areacodeMap,String ciscode,String aliasname,String mobile ) {
		
			UAInputSignUp uAInputSignUp = new UAInputSignUp();
			if(areacodeMap != null)
			{
				uAInputSignUp.setCITY(areacodeMap.getCity()!=null?areacodeMap.getCity():"");
				uAInputSignUp.setPROVINCE(areacodeMap.getProvince());
			}
			uAInputSignUp.setCIS_CODE(ciscode);
			uAInputSignUp.setALIASNAME(aliasname);
			uAInputSignUp.setMOBILE(mobile);
			uAInputSignUp.setREGISTERWAY("1");
//			uAInputSignUp.setREGISTERWAY("2");
			Gtcg gtcg = (Gtcg) SpringContextLoaderListener
					.getSpringWebApplicationContext().getBean(
							"gtcgService");
			UAOutputSignUp uAOutputSignUp = null;
			try {
				uAOutputSignUp = (UAOutputSignUp) gtcg.sendToEbank(uAInputSignUp, "PassRegister");
			} catch (GtcgIOException e) {
				// TODO Auto-generated catch block
				this.log.error(e.getMessage());
				return null;
			}
			return uAOutputSignUp;
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
	
	private UAOutputQueryInfo uAOutputQueryInfo;
	
	
	
	/**
	 * 场景1 以网银身份登录商城成功后跳转回商城
	 */
	
	private void syncLocalInfo(UAOutputQueryInfo output,String cisCode,String sessionid,String custmerIp,String custmerMAC,String loginChannels,String userbrowser,String useros,String loginWay,String custName,String mobile) throws Exception{
		// TODO Auto-generated method stub
		this.log.debug("local userinfo sync start----------------------------------------------------");
		Auth authInfo = null;
		MallUserInfo mallUserInfo = null;
		String userid = output.getPrivateInfo().getUserid();
		authInfo = authService.getAuthByUserId(null, null, userid);
		mallUserInfo  = this.mallUserInfoService.getMallUserById1(userid);
		
		output.getPrivateInfo().setMobile(mobile);
		
		if(authInfo == null)
		{
			//补录信息
			this.log.info("no userinfo in mall");
			this.log.info("add auth & malluserinfo &ciscode &lotteryuserinfo in mall");
			authInfo = this.createAuth(output);
			mallUserInfo = createMallUserInfo(output);
			if( !StringUtil.isBlank(custName) ){
				mallUserInfo.setRealName(custName);
			}
			
			MallLoginInfo mallLogInfo = createLoginLog(userid,sessionid,custmerIp,custmerMAC,loginChannels,userbrowser,useros,loginWay);
			LotteryUserinfo lotteryUserinfo = new LotteryUserinfo();
			lotteryUserinfo.setUserId(userid);
			String outuserid = serialGeneratorMgr.getSerialKey(Constants.LOTTERYUSERINFODSERIAL).trim();
			lotteryUserinfo.setOutuserId(outuserid);
			Ciscode ciscode = new Ciscode();
			ciscode.setCiscode(cisCode);
			ciscode.setUserid(userid);
			try
			{
				ciscodeService.addUser(mallUserInfo, authInfo, ciscode,mallLogInfo,lotteryUserinfo);

			}
			catch(Exception e)
			{
				this.log.error("error add userinfo in mall");
				throw e;
			}

		}
		else
		{
			//返回信息同本地auth不一致 更新本地auth
			//			0 auth一致 mallUserInfo一致
			//			1 auth一致 mallUserInfo不一致
			//			2 auth不一致 mallUserInfo一致
			//			3 auth不一致 mallUserInfo不一致
			int result = isUserInfoConsistent(authInfo,mallUserInfo,output);
			//补录个网custName
			if(!StringUtil.isBlank(custName) && (!custName.equals(mallUserInfo.getRealName()))){
				mallUserInfo.setRealName(custName);
				if(result==0){
					result=1;
				}else if(result==2){
					result=3;
				}
			}
			
			if(result==1)
			{
				mallUserInfoService.updateMallUserInfo(null, null, mallUserInfo);
			}
			else if(result == 2)
			{
				authService.updateAuthByUserId(authInfo);
			}
			else if(result == 3)
			{
				mallUserInfoService.updateMallUserInfoAndAuth(mallUserInfo, authInfo);
			}
		}
		this.log.debug("local userinfo sync end----------------------------------------------------");
	}
	 

	/*
	 * auth表、mallUserInfo表中信息是否与统一认证接口返回的信息一致
	 * 返回值：0 一致
	 * 		 1 auth一致 mallUserInfo不一致
	 * 		 2 auth不一致 mallUserInfo一致
	 * 		 3 auth不一致 mallUserInfo不一致
	 */
	private int isUserInfoConsistent(Auth authInfo, MallUserInfo mallUserInfo,UAOutputQueryInfo output) {
		// TODO Auto-generated method stub
		if(isAuthConsistent(authInfo,output))
		{
			if(isMallUserInfoConsistent(mallUserInfo, output))
				return 0;
			else
				return 1;
		}
		else
		{
			if(isMallUserInfoConsistent(mallUserInfo, output))
				return 2;
			else
				return 3;
		}
		
	}
	
	/*
	 * 判断商城auth表中信息是否与统一认证相同
	 */
	private boolean isAuthConsistent(Auth authInfo, UAOutputQueryInfo output)
	{
		String loginID = authInfo.getLoginId();
		loginID = loginID==null?"":loginID.trim();
		String userType = authInfo.getUserType();
		userType = userType==null?"":userType.trim();
		String isLock = authInfo.getIsLock();
		isLock = isLock==null?"":isLock.trim();
		String isEnable = authInfo.getIsEnable();
		isEnable = isEnable==null?"":isEnable.trim();
		
		String loginID1 = output.getPrivateInfo().getLoginID();
		loginID1 = loginID1==null?"":loginID1.trim();
		String userType1 = output.getPrivateInfo().getUserType();
		userType1 = userType1==null?"":userType1.trim();
		String isLock1 = output.getPrivateInfo().getIsLock();
		isLock1 = isLock1==null?"":isLock1.trim();
		String isEnable1 = output.getPrivateInfo().getIsEnable();
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
	
	private boolean isMallUserInfoConsistent(MallUserInfo mallUserInfo,UAOutputQueryInfo output)
	{
		String email = mallUserInfo.getEmail();
		email = email==null?"":email.trim();
		String mobile = mallUserInfo.getMobile();
		mobile = mobile==null?"":mobile.trim();
		String province = mallUserInfo.getProvince();
		province = province==null?"":province.trim();
		String city = mallUserInfo.getCity();
		city = city==null?"":city.trim();
		String cisCode = mallUserInfo.getCisCode();
		cisCode = cisCode==null?"":cisCode.trim();
		
		String email1 = output.getPrivateInfo().getEmail();
		email1 = email1==null?"":email1.trim();
		String mobile1 = output.getPrivateInfo().getMobile();
		mobile1 = mobile1==null?"":mobile1.trim();
		String province1 = output.getPrivateInfo().getProvince();
		province1 = province1==null?"":province1.trim();
		String city1 = output.getPrivateInfo().getCity();
		city1 = city1==null?"":city1.trim();
		String cisCode1 = output.getPrivateInfo().getCisCode();
		cisCode1 = cisCode1==null?"":cisCode1.trim();
		
		if(email.equals(email1)&&mobile.equals(mobile1)&&province.equals(province1)&&city.equals(city1)&&cisCode.equals(cisCode1))
			return true;
		else
		{
			mallUserInfo.setEmail(email1);
			mallUserInfo.setMobile(mobile1);
			mallUserInfo.setProvince(province1);
			mallUserInfo.setCity(city1);
			mallUserInfo.setCisCode(cisCode1);
			if(!"".equals(cisCode1))
			{
				mallUserInfo.setUserType("1");
			}
			else
			{
				mallUserInfo.setUserType("0");
			}
			return false;
		}

	}
	
	private MallLoginInfo createLoginLog(String userId,String sessionid,String custmerIp,String custmerMAC,String loginChannels,String userbrowser,String useros,String loginWay) throws ParseException {
		// TODO Auto-generated method stub
		MallLoginInfo mallLogInfo = new MallLoginInfo();
		mallLogInfo.setUserid(userId);
		mallLogInfo.setSessionId(sessionid);
		mallLogInfo.setClientIp(custmerIp);
		mallLogInfo.setClientMAC(custmerMAC);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mallLogInfo.setLastLoginTime((Date) dateFormat.parseObject(dateFormat.format(new Date())));
		mallLogInfo.setErrorTimes(new BigDecimal(0));
		mallLogInfo.setLastLoginChannels(loginChannels); // 0 互联网；1 移动互联网
		mallLogInfo.setLastLoginWay(loginWay);
		mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
		mallLogInfo.setLastLoginOs(useros);
		return mallLogInfo;
	}
	 
	//适用于网银用户首次登录 慎用
	private MallUserInfo createMallUserInfo(String userid,String aliasName,String cisCode,String custName,String mainAreaCode,String ebankUserLevel,String mobile) {
		// TODO Auto-generated method stub
		MallUserInfo mallUserInfo = new MallUserInfo();
		mallUserInfo.setUserid(userid);
		mallUserInfo.setCisCode(cisCode);
		mallUserInfo.setRealName(custName);
		//1实名认证
		mallUserInfo.setUserType(MallUserType.REALNAMEAUTH);
		//用户级别 
		mallUserInfo.setUserLevel("");
		Date date = new Date();
		mallUserInfo.setRegisterTime(date);
		mallUserInfo.setModifyTime(date);
		mallUserInfo.setMobile(mobile);
		//1是首次登录
		mallUserInfo.setIsFirstLogin(YesOrNo.YES);
		//注册渠道  0 互联网；1 移动互联网
		mallUserInfo.setRegisterChannels("0");
		//注册方式 0 商城；1 手机商城 ；2 网银；3 手机银行； 
		mallUserInfo.setRegisterWay("2");
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
		return mallUserInfo;
	}

	private Auth createAuth(UAOutputSignUp output) {
		// TODO Auto-generated method stub
		Auth authInfo = new Auth();
		authInfo.setUserid(output.getPrivateInfo().getUserid());
		authInfo.setLoginId(output.getPrivateInfo().getLoginid());
		authInfo.setUserType(AuthType.MALL);
		authInfo.setIsLock("0");
		authInfo.setIsEnable("1");
		return authInfo;
	}

	private UAOutputQueryInfo isFirstTimeLogin(String cisCode) throws GtcgIOException {
		// TODO Auto-generated method stub
		this.log.debug("isFirstTimeLogin starts----------------------------");
		UAOutputQueryInfo uAOutputQueryInfo = null;
		UAInputQueryUserInfo uAInputQueryUserInfo = new UAInputQueryUserInfo();
		uAInputQueryUserInfo.setFIELDTYPE("4");
		uAInputQueryUserInfo.setFIELDVALUE(cisCode);
		uAOutputQueryInfo = (UAOutputQueryInfo)gtcg.sendToEbank(uAInputQueryUserInfo, "PassVerifyInfoQry");

		return uAOutputQueryInfo;
		
	}
	
	 
	private Auth createAuth(UAOutputQueryInfo output)
	{
		Auth authInfo = new Auth();
		authInfo.setUserid(output.getPrivateInfo().getUserid());
		authInfo.setUserType(output.getPrivateInfo().getUserType());
		authInfo.setIsLock(output.getPrivateInfo().getIsLock());
		authInfo.setIsEnable(output.getPrivateInfo().getIsEnable());
		authInfo.setLoginId(output.getPrivateInfo().getLoginID());
		return authInfo;
	}
	
	
	private MallUserInfo createMallUserInfo(UAOutputQueryInfo output) {
		// TODO Auto-generated method stub
		MallUserInfo mui = new MallUserInfo();
		mui.setUserid(output.getPrivateInfo().getUserid());
		mui.setMobile(output.getPrivateInfo().getMobile());
		mui.setEmail(output.getPrivateInfo().getEmail());
		//非实名
		mui.setUserType("0");
		mui.setProvince(output.getPrivateInfo().getProvince());
		mui.setCity(output.getPrivateInfo().getCity());
		mui.setUserLevel("");
		//注册方式 0 商城；1 手机商城 ；2 网银；3 手机银行； 
		mui.setRegisterWay("0");
		//注册渠道  0 互联网；1 移动互联网
		mui.setRegisterChannels("0");
		mui.setPost("");
		//是否访问过我的商城 : 0未访问  1 访问
		mui.setIsVisitedMember("0");
		
		String cisCode = output.getPrivateInfo().getCisCode();
		cisCode = cisCode==null?"":cisCode.trim();
		
		mui.setCisCode(cisCode);
		if(!"".equals(cisCode))
		{
			mui.setUserType("1");
		}
		else
		{
			mui.setUserType("0");
		}
		
		//行政地区号转化成行内地区号
		AreacodeMapService areacodeMapService = (AreacodeMapService)SpringContextLoaderListener
				.getSpringWebApplicationContext().getBean(
						"areacodeMapService");
		try {
			areacodeMapService.setMallUserAreacode(mui);
		} catch (EmallServiceException e) {
			// TODO Auto-generated catch block
			this.log.error("行政地区号转化成行内地区号出错.");
			log.error("",e);
		}
		return mui;
	}
	

 
	/**
	 * 场景5，从手机网银登录商城成功后跳转回商城
	 */
	private boolean scene5(final RequestContext context, JSONObject json, UsernamePasswordCredentials user, String sceneType,String eMallFieldEncryped) {
		String uaswitch = ApplicationConfigUtils.getInstance()
				.getPropertiesValue(com.icbc.emall.Constants.UNIFIED_AUTH_BUS_SWITCH);
		if(uaswitch.equals("1"))
			return scene5_ua(context,json,user,sceneType,eMallFieldEncryped);
		else
//			return scene5_ua(context,json,user,sceneType,eMallFieldEncryped);
			return scene5_old(context,json,user,sceneType,eMallFieldEncryped);
	}
	
	private boolean scene5_ua(final RequestContext context, JSONObject json, UsernamePasswordCredentials user, String sceneType,String eMallFieldEncryped) {
		log.debug("===== scene5_UA ===== sceneType = "+ sceneType +" ===== "+ new Date()+ "----start---");
		//// 将参数sceneType放入FlowScope
		//context.getFlowScope().put("sceneType",SceneType.LOGIN_AFTER_EBANKP_PHONE);
			
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//网银用户别名
		String aliasName = (String) json.get("aliasName");
		aliasName = aliasName ==null ? "":aliasName.trim(); 
		//客户信息号
		String cisCode =(String) json.get("cisno");
		cisCode = cisCode ==null ? "":cisCode.trim(); 
		//客户IP
		String custmerIp = (String) json.get("custmerIp");
		custmerIp = custmerIp ==null ? "":custmerIp.trim(); 
		//客户MAC
		String custmerMAC = "";
		if(null != json.get("custmerMac")){
			if(!"null".equals(json.get("custmerMac").toString()) && !"".equals(json.get("custmerMac").toString())){
				custmerMAC = (String) json.get("custmerMac");
				custmerMAC = custmerMAC ==null ? "":custmerMAC.trim(); 
			}else{
				log.debug("===================custmerMAC=手机商城个人网银登录暂时无法记录客户端ip地址！=======================");
			}
			
		}
		
		//注册地区号
		String mainAreaCode = (String) json.get("mainAreaCode");
		mainAreaCode = mainAreaCode ==null ? "":mainAreaCode.trim();
		//用户的手机号
		String custMobile = (String) json.get("mobileNum");
		custMobile = custMobile==null ? "": custMobile.trim();
		//真实姓名
		String custName = (String) json.get("CustName");
		custName = custName ==null ? "":custName.trim();
		//网银用户星级
		String ebankUserLevel = (String) json.get("CustXingji");
		ebankUserLevel = ebankUserLevel ==null ? "":ebankUserLevel.trim();
		//网银登录方式卡号，别名或手机号
		String logonNum = (String) json.get("logonNum");
		logonNum = logonNum ==null ? "":logonNum.trim();
		//用户的浏览器名称
		String userbrowser=SystemMessageUtil.getBrowser(request);
		//用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		Auth auth = null;
		MallUserInfo mallUserInfo = null;
		String loginId = "";
		String userid = "";
		String password = "";
		//用来存储到日志里loginName=号+loginid-手机号-ciscode
		String loginName = "";
		//首次登录商城
		boolean firstTimeFlag = false;
		UAOutputQueryInfo uAOutputQueryInfo=null;
		try {
			uAOutputQueryInfo = isFirstTimeLogin(cisCode);
			//0：一条记录 1：没有记录
			if(uAOutputQueryInfo!=null&&"1".equals(uAOutputQueryInfo.getPublicInfo().getRetCode())){
				firstTimeFlag = true;
			}else if(uAOutputQueryInfo!=null&&"0".equals(uAOutputQueryInfo.getPublicInfo().getRetCode())){
				firstTimeFlag = false;
			}else{
				throw new GtcgIOException("统一登录认证查询用户信息时出错。");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			context.getFlowScope().put("error",
					"GtcgIOException");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return false;
		}
		//调用通行证注册接口完成注册。注册接口调用失败，登录失败；调用成功，补录电商本地数据，补录成功，执行后续流程；补录失败，登录失败。
		if(firstTimeFlag)
		{
			if("1".equals(BaseServiceData.baseServiceEnable))
			{
				log.error("basic service");
				context.getFlowScope().put("error",
						"basic service and never login before");
				context.getFlowScope().put("errorCode","3");
				return false;
			}
			this.log.info("ua sign up start");
			for(int i=mainAreaCode.length();i<5;i++)
				mainAreaCode = "0"+mainAreaCode;
			log.info("mainAreaCode:" + mainAreaCode);
			AreacodeMap areacodeMap= this.areacodeMapDAO.selectByPrimaryKey(mainAreaCode);
			
			UAOutputSignUp uAOutputSignUp = signUpUA(areacodeMap,cisCode,aliasName,custMobile); 
			if(uAOutputSignUp == null)
			{
				this.log.error("调用统一认证注册接口出错");
				context.getFlowScope().put("error",
						"调用统一认证注册接口出错");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}
			if(!"0".equals(uAOutputSignUp.getPublicInfo().getRetCode()))
			{
				this.log.error("调用统一认证注册接口出错");
				context.getFlowScope().put("error",
						"调用统一认证注册接口出错");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}	
			this.log.info("ua sign up succeed");
			this.log.info("save userinfo to mall start");
			userid = uAOutputSignUp.getPrivateInfo().getUserid();
			loginId = uAOutputSignUp.getPrivateInfo().getLoginid();
			loginName = logonNum+"-"+loginId +"-"+custMobile+ "-"+ cisCode;
			context.getFlowScope().put("userId", userid);
			context.getFlowScope().put("loginId",loginName);
			//context.getFlowScope().put("loginId","loveFish");
			try
			{
				auth = createAuth(uAOutputSignUp);
				mallUserInfo = createMallUserInfo(uAOutputSignUp.getPrivateInfo().getUserid(),aliasName,cisCode,custName,mainAreaCode,ebankUserLevel,custMobile);
				String lastLoginWayValue="";
				if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE) 
						|| sceneType.equals(SceneType.LOGIN_LOTTERY_MOBILE) 
						|| sceneType.equals(SceneType.LOGIN_JIPIAO_MOBILE)
						|| sceneType.equals(SceneType.LOGIN_AFTER_WAPBANK_PHONE)){
					lastLoginWayValue = LoginWay.MOBILEEBANK;
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PAD) ){
					lastLoginWayValue = LoginWay.PADEBANK;
				}
				MallLoginInfo mallLogInfo = createLoginLog(userid,request.getSession().getId(),custmerIp,custmerMAC,"1",userbrowser,useros,lastLoginWayValue);
				LotteryUserinfo lotteryUserinfo = new LotteryUserinfo();
				lotteryUserinfo.setUserId(userid);
				String outuserid = serialGeneratorMgr.getSerialKey(Constants.LOTTERYUSERINFODSERIAL).trim();
				lotteryUserinfo.setOutuserId(outuserid);
				Ciscode ciscode = new Ciscode();
				ciscode.setCiscode(cisCode);
				ciscode.setUserid(userid);
				try
				{
					//ciscodeService.addUser(mallUserInfo, auth, null,mallLogInfo,lotteryUserinfo);
					ciscodeService.addUser(mallUserInfo, auth, ciscode,mallLogInfo,lotteryUserinfo);

				}
				catch(Exception e)
				{
					log.error(e.getMessage());
					context.getFlowScope().put("error",
							"网银用户首次登录记录本地信息出错");
					context.getFlowScope().put("eMallField", eMallFieldEncryped);
					return false;
				}
				context.getFlowScope().put("eBankUserFirstLogin","YES");
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"网银用户首次登录记录本地信息出错");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}
		}
		else
		{
			/*
			 * 用userid到本地查询，
				（1）查询有记录，比较本地记录与登录接口返回用户信息是否一致。一致，执行后续流程。不一致，更新本地记录，更新成功，执行后续流程；更新失败，登录失败。
				（2）查询无记录，补录电商本地数据，补录成功，执行后续流程；补录失败，登录失败。
			 */
			try
			{
				/*
				 *  		调用统一认证查询接口，获取该用户的信息
				 * 					在进行判断手机号，如果如果手机号为null，进行上送更新统一认证服务器，
				 *				处理该方法syncLocalInfo，注册新添手机号，如果不是注册，判断手机号是否一致，不一致修改
				*/
				userid = uAOutputQueryInfo.getPrivateInfo().getUserid();
				String mobile = uAOutputQueryInfo.getPrivateInfo().getMobile();
				//如果手机号为空,更新统一认证服务器中的数据库，使用统一通行证上的手机号
				if(org.apache.commons.lang.StringUtils.isBlank(mobile)){
					UAOutputUpdateInfo uAInputUpdateInfo = updateMobileUA(userid,custMobile); 
					if(uAInputUpdateInfo == null)
					{
						this.log.error("调用统一认证注册接口出错");
						context.getFlowScope().put("error",
								"调用统一认证注册接口出错");
						context.getFlowScope().put("eMallField", eMallFieldEncryped);
						return false;
					}
					if(!"0".equals(uAInputUpdateInfo.getPublicInfo().getRetCode()))
					{
						this.log.error("调用统一认证注册接口出错");
						context.getFlowScope().put("error",
								"调用统一认证注册接口出错");
						context.getFlowScope().put("eMallField", eMallFieldEncryped);
						return false;
					}	
				}else{
					custMobile = mobile;
				}
				
				syncLocalInfo(uAOutputQueryInfo,cisCode,request.getSession().getId(), custmerIp, custmerMAC, "0", userbrowser, useros,LoginWay.MOBILEEBANK,custName,custMobile);
				
				if(uAOutputQueryInfo.getPrivateInfo().getIsLock().equals("1")){
					context.getFlowScope().put("error",
							"账户被锁定，禁止登录。该账户将在锁定24小时之后解锁！");
					context.getFlowScope().put("errorCode", "2");
					context.getFlowScope().put("eMallField", eMallFieldEncryped);
					return false;
				}
				
				auth = authService.getAuthByUserId(null, null, userid);
				
				loginId = auth.getLoginId();
				loginName = logonNum+"-"+loginId +"-"+custMobile+ "-"+ cisCode;
				context.getFlowScope().put("userId", userid);
				context.getFlowScope().put("loginId", loginName);
				password = auth.getPassword();
				
				String lastLoginWayValue="";
				if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE) 
						|| sceneType.equals(SceneType.LOGIN_LOTTERY_MOBILE) 
						|| sceneType.equals(SceneType.LOGIN_JIPIAO_MOBILE)
						|| sceneType.equals(SceneType.LOGIN_AFTER_WAPBANK_PHONE)){
					lastLoginWayValue = LoginWay.MOBILEEBANK;
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PAD) ){
					lastLoginWayValue = LoginWay.PADEBANK;
				}
				MallLoginInfo mallLogInfo = this.createLoginLog(userid, request.getSession().getId(), custmerIp, custmerMAC, "1", userbrowser, useros,lastLoginWayValue);
				mallUserInfo = new MallUserInfo();
				mallUserInfo.setUserid(userid);
				mallUserInfo.setUserType(YesOrNo.YES);
				mallUserInfo.setRegisterChannels("2");
				mallUserInfo.setRegisterAreaNumber(mainAreaCode);
				mallUserInfo.setIsFirstLogin(YesOrNo.NO);
				//网银客户星级
				mallUserInfo.setEbankUserLevel(ebankUserLevel);
				//是否访问过我的商城 : 0未访问  1 访问
				mallUserInfoService.updateUserAndLoginInfo(null, null, mallUserInfo, mallLogInfo);
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error.saveAuthOrMallUserInfo");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}
		}
		updateEbankUserAddr(json, cisCode, custName);
		//设置用户名密码以及是否检查密码标志，用于后继的flow流的校验,在CustomerAuthenticationHandler校验用户名+密码时使用
		user.setIsCheckPwd(YesOrNo.NO);
		user.setUsername(loginId);
		user.setPassword(password);
		user.setCis(cisCode);
		
		log.debug("===== scene5_UA ===== sceneType = "+ sceneType +" ===== "+ new Date()+ "----end---");
		return true;
	
	}

	/*
	 * 场景5 -- old
	 */
	private boolean scene5_old(final RequestContext context, JSONObject json,
			UsernamePasswordCredentials user, String sceneType,String eMallFieldEncryped) {
		log.debug("===== scene5_OLD ===== sceneType = "+ sceneType +" ===== "+ new Date() + "----start---");
		
		//// 将参数sceneType放入FlowScope
		//context.getFlowScope().put("sceneType",
		//		SceneType.LOGIN_AFTER_EBANKP_PHONE);

		MallUserInfo mallUserInfo = null;
		final HttpServletRequest request = WebUtils
				.getHttpServletRequest(context);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 网银用户别名
		String aliasName = (String) json.get("aliasName");
		aliasName = aliasName == null ? "" : aliasName.trim();
		// 客户信息号
		String cisCode = (String) json.get("mainCIS");
		cisCode = cisCode == null ? "" : cisCode.trim();
		// 客户IP
		String custmerIp = (String) json.get("custmerIp");
		custmerIp = custmerIp == null ? "" : custmerIp.trim();
		//网银登录方式卡号，别名或手机号
		String logonNum = (String) json.get("logonNum");
		logonNum = logonNum ==null ? "":logonNum.trim();
		// 客户MAC
		String custmerMAC = "";
		//用来存储到日志里loginName=号+loginid-手机号-ciscode
		String loginName = "";
		//用户的手机号
		String custMobile = (String) json.get("mobileNum");
		custMobile = custMobile==null ? "": custMobile.trim();
		if (null != json.get("custmerMac")) {
			if (!"null".equals(json.get("custmerMac").toString())
					&& !"".equals(json.get("custmerMac").toString())) {
				custmerMAC = (String) json.get("custmerMac");
				custmerMAC = custmerMAC == null ? "" : custmerMAC.trim();
			} else {
				log.debug("===================custmerMAC=手机商城个人网银登录暂时无法记录客户端ip地址！=======================");
			}
		}
		// 注册地区号
		String mainAreaCode = (String) json.get("mainAreaCode");
		mainAreaCode = mainAreaCode == null ? "" : mainAreaCode.trim();
		// 真实姓名
		String custName = (String) json.get("CustName");
		custName = custName == null ? "" : custName.trim();
		// 网银用户星级
		String ebankUserLevel = (String) json.get("CustXingji");
		ebankUserLevel = ebankUserLevel == null ? "" : ebankUserLevel.trim();
		// 用户的浏览器名称
		String userbrowser = SystemMessageUtil.getBrowser(request);
		// 用户的操作系统名
		String useros = SystemMessageUtil.getOs(request);
		Auth auth = null;
		String loginId = "";
		String password = "";

		Ciscode record = null;
		// 根据CIS号检查b2c_mall_user_info表中是否存用户记录
		try {
			record = ciscodeService.selectByCiscode(cisCode);
		} catch (Exception e2) {
			e2.printStackTrace();
			log.error(e2.getMessage());
			context.getFlowScope().put("error", "Exception");
			context.getFlowScope().put("eMallField", eMallFieldEncryped);
			return false;
		}
		if (record == null) {// 如果不存在用户记录
			if("1".equals(BaseServiceData.baseServiceEnable))
			{
				log.error("basic service");
				context.getFlowScope().put("error",
						"basic service and never login before");
				context.getFlowScope().put("errorCode","3");
				return false;
			}
			String userId = null;
			// 从序列值中生成userId信息
			try {
				userId = serialGeneratorMgr.getSerialKey(Constants.AUTH_USERID)
						.trim();
			} catch (Exception e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error", "error generating userid");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}
			context.getFlowScope().put("userId", userId);

			// 生成商城登录名
			// 判断网银是否返回别名json串中的aliasName
			if (aliasName != null && !"".equals(aliasName)) {
				// 有别名
				try {
					Auth tmpAuth = authService.getAuthByLoginId(null, null,
							aliasName);
					if (tmpAuth != null) {
						// 有 重名，根据 规则 重新 生成 logId
						String ID = "";
						do {// 对于重新生成的用户名，需要再次检查是否会重名
							// loginId =aliasName+i;
							ID = serialGeneratorMgr.getSerialKey(
									Constants.AUTH_LOGIN_ID).trim();
							while (ID != null && ID.startsWith("0")) {
								ID = ID.substring(1);
							}
							loginId = aliasName + ID;
							tmpAuth = authService.getAuthByLoginId(null, null,
									loginId);
						} while (tmpAuth != null);
					} else {
						loginId = aliasName;
					}
				} catch (EmallServiceException e) {
					log.error(e.getMessage());
					context.getFlowScope().put("error",
							"error.userOrPassword.error");
					context.getFlowScope()
							.put("eMallField", eMallFieldEncryped);
					return false;
				}
				loginName = logonNum+"-"+loginId +"-"+custMobile+ "-"+ cisCode;
				context.getFlowScope().put("loginId", loginName);
			} else {
				// 无别名 生成虚拟 loginId
				Auth reAuth = null;
				do {
					String ID = serialGeneratorMgr.getSerialKey(
							Constants.AUTH_LOGIN_ID).trim();
					while (ID != null && ID.startsWith("0")) {
						ID = ID.substring(1);
					}
					loginId = "WY" + ID;
					try {
						reAuth = authService.getAuthByLoginId(null, null,
								loginId);
					} catch (EmallServiceException e) {
						log.error(e.getMessage());
						context.getFlowScope().put("error",
								"EmallServiceException");
						context.getFlowScope().put("eMallField",
								eMallFieldEncryped);
						return false;
					}
				} while (reAuth != null);
				loginName = logonNum +"-"+loginId +"-"+custMobile+ "-"+ cisCode;
				context.getFlowScope().put("loginId", loginName);
			}

			log.debug("scene5=====LOGIN_AFTER_EBANKP_PHONE  " + new Date()
					+ "----synchronized---start");
			// 信息同步
			try {
				// 个人会员登录信息表
				MallLoginInfo mallLogInfo = new MallLoginInfo();
				mallLogInfo.setUserid(userId);
				mallLogInfo.setSessionId(request.getSession().getId());
				mallLogInfo.setClientIp(custmerIp);
				mallLogInfo.setClientMAC(custmerMAC);
				mallLogInfo.setLastLoginTime((Date) dateFormat
						.parseObject(dateFormat.format(new Date())));
				mallLogInfo.setErrorTimes(new BigDecimal(0));
				mallLogInfo.setLastLoginChannels("1"); // 1 移动互联网
//				mallLogInfo.setLastLoginWay("2"); // 2 网银 3 手机银行
				if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE) 
						|| sceneType.equals(SceneType.LOGIN_LOTTERY_MOBILE) 
						|| sceneType.equals(SceneType.LOGIN_JIPIAO_MOBILE)
						|| sceneType.equals(SceneType.LOGIN_AFTER_WAPBANK_PHONE)){
					mallLogInfo.setLastLoginWay(LoginWay.MOBILEEBANK);
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PAD) ){
					mallLogInfo.setLastLoginWay(LoginWay.PADEBANK);
				}
				mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
				mallLogInfo.setLastLoginOs(useros);

				// 认证信息表
				auth = new Auth();
				auth.setIsEnable("1");
				auth.setIsLock("0");
				auth.setLoginId(loginId);
				auth.setPassword("");
				auth.setUserType("0");
				auth.setUserid(userId);

				// 个人会员信息表
				mallUserInfo = new MallUserInfo();
				mallUserInfo.setUserid(userId);
				mallUserInfo.setMemberNickname(aliasName);
				mallUserInfo.setCisCode(cisCode);
				mallUserInfo.setRealName(custName);
				mallUserInfo.setIsFirstLogin(YesOrNo.YES);
				// 1实名认证
				mallUserInfo.setUserType(MallUserType.REALNAMEAUTH);
				// 用户级别
				mallUserInfo.setUserLevel("");
				Date date = new Date();
				mallUserInfo.setRegisterTime(date);
				mallUserInfo.setModifyTime(date);
				// 1是首次登录
				mallUserInfo.setIsFirstLogin(YesOrNo.YES);
				// 注册渠道 0 互联网；1 移动互联网
				mallUserInfo.setRegisterChannels("1");
				// 注册方式 0 商城；1 手机商城 ；2 网银；3 手机银行；
				mallUserInfo.setRegisterWay("2");
				mallUserInfo.setRegisterAreaNumber(mainAreaCode);
				// 是否访问过我的商城 : 0未访问 1 访问
				mallUserInfo.setIsVisitedMember("0");
				// 设置网银客户星级
				mallUserInfo.setEbankUserLevel(ebankUserLevel);
				// 将行内地区号映射成行政地区号
				for (int i = mainAreaCode.length(); i < 5; i++)
					mainAreaCode = "0" + mainAreaCode;
				log.info("mainAreaCode:" + mainAreaCode);
				AreacodeMap areacodeMap = this.areacodeMapDAO
						.selectByPrimaryKey(mainAreaCode);
				if (areacodeMap != null) {
					mallUserInfo.setCity(areacodeMap.getCity());
					mallUserInfo.setProvince(areacodeMap.getProvince());
				}

				Ciscode ciscode = new Ciscode();
				ciscode.setCiscode(cisCode);
				ciscode.setUserid(userId);
				
				LotteryUserinfo lotteryUserinfo = new LotteryUserinfo();
				lotteryUserinfo.setUserId(userId);
				String outuserid = serialGeneratorMgr.getSerialKey(Constants.LOTTERYUSERINFODSERIAL).trim();
				lotteryUserinfo.setOutuserId(outuserid);
				try {
					ciscodeService.addUser(mallUserInfo, auth, ciscode,
							mallLogInfo,lotteryUserinfo);
				} catch (Exception e) {
					log.error(e.getMessage());
					context.getFlowScope().put("error", "网银用户同时首次登录");
					context.getFlowScope()
							.put("eMallField", eMallFieldEncryped);
					return false;
				}

				log.debug("eBankLogin  " + new Date()
						+ "----synchronized---end");
				context.getFlowScope().put("eBankUserFirstLogin", "YES");
			} catch (Exception e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error.saveAuthOrMallUserInfo");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}
		} else {// 如果存在用户记录
			String userId = record.getUserid().trim();
			try {
				// 登录前检查黑名单
				/*BlackList blackListInfo = blackListDAO.selectBymemberId(userId);
				if (blackListInfo != null
						&& !("02".equals(blackListInfo.getStatus()))) {
					context.getFlowScope().put("error", "该用户已被列入黑名单，禁止登录！");
					context.getFlowScope()
							.put("eMallField", eMallFieldEncryped);
					// 错误码 1表示黑名单
					context.getFlowScope().put("errorCode", "1");
					return false;
				}*/
				// 根据userId查询表b2c_auth，获取用户认证信息loginId，password
				auth = authService.getAuthByUserId(null, null, userId);
				if (auth.getIsLock() != null
						&& YesOrNo.YES.equals(auth.getIsLock().trim())) {
					MallLoginInfo loginInfo = mallLoginInfoService
							.findMallLoginInfoByLoginId(auth.getUserid());
					Date unlockDate = null;
					Date currentDate = new Date();
					try {
						// 解锁时间，（锁定日期+24小时）
						unlockDate = PubUtils.getLockDate(loginInfo
								.getLockTime());
					} catch (ParseException e) {
						context.getFlowScope().put("error", "获取解锁时间失败");
						context.getFlowScope().put("eMallField",
								eMallFieldEncryped);
						return false;
					}
					if (currentDate.after(unlockDate)) {
						auth.setIsLock(YesOrNo.NO);
						try {
							authService.updateByPrimaryKeySelective(auth);
						} catch (EmallServiceException e) {
							context.getFlowScope().put("error", "解锁更新auth失败");
							context.getFlowScope().put("eMallField",
									eMallFieldEncryped);
							return false;
						}
					} else {
						context.getFlowScope().put("error", "账户被锁定");
						context.getFlowScope().put("errorCode", "2");
						context.getFlowScope().put("eMallField",
								eMallFieldEncryped);
						return false;
					}
				}
				loginId = auth.getLoginId();
				loginName = logonNum +"-"+loginId +"-"+custMobile+ "-"+ cisCode;
				context.getFlowScope().put("userId", userId);
				context.getFlowScope().put("loginId", loginName);
				password = auth.getPassword();

				// 信息同步
				// 更新个人会员登录信息表
				MallLoginInfo mallLogInfo = new MallLoginInfo();
				mallLogInfo.setUserid(userId);
				mallLogInfo.setSessionId(request.getSession().getId());
				mallLogInfo.setClientIp(custmerIp);
				mallLogInfo.setClientMAC(custmerMAC);

				mallLogInfo.setLastLoginTime((Date) dateFormat
						.parseObject(dateFormat.format(new Date())));
				mallLogInfo.setErrorTimes(new BigDecimal(0));
				mallLogInfo.setLastLoginChannels("1"); // 1 移动互联网
//				mallLogInfo.setLastLoginWay("2"); // 2 网银 3 手机银行
				if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PHONE) 
						|| sceneType.equals(SceneType.LOGIN_LOTTERY_MOBILE) 
						|| sceneType.equals(SceneType.LOGIN_JIPIAO_MOBILE)
						|| sceneType.equals(SceneType.LOGIN_AFTER_WAPBANK_PHONE)){
					mallLogInfo.setLastLoginWay(LoginWay.MOBILEEBANK);
				}else if(sceneType.equals(SceneType.LOGIN_AFTER_EBANKP_PAD) ){
					mallLogInfo.setLastLoginWay(LoginWay.PADEBANK);
				}
				mallLogInfo.setLastLoginDeviceBrowser(userbrowser);
				mallLogInfo.setLastLoginOs(useros);

				// 更新个人会员信息表
				mallUserInfo = new MallUserInfo();
				mallUserInfo.setUserid(userId);
				mallUserInfo.setUserType(YesOrNo.YES);
				mallUserInfo.setRegisterChannels("1");
				mallUserInfo.setRegisterAreaNumber(mainAreaCode);
				mallUserInfo.setIsFirstLogin(YesOrNo.NO);
				// 网银客户星级
				mallUserInfo.setEbankUserLevel(ebankUserLevel);
				// 是否访问过我的商城 : 0未访问 1 访问
				// mallUserInfo.setIsVisitedMember(YesOrNo.YES);
				mallUserInfoService.updateUserAndLoginInfo(null, null,
						mallUserInfo, mallLogInfo);
			} catch (Exception e) {
				log.error(e.getMessage());
				context.getFlowScope().put("error",
						"error.saveAuthOrMallUserInfo");
				context.getFlowScope().put("eMallField", eMallFieldEncryped);
				return false;
			}
		}
		updateEbankUserAddr(json, cisCode, custName);

		// 设置用户名密码以及是否检查密码标志，用于后继的flow流的校验,在CustomerAuthenticationHandler校验用户名+密码时使用
		user.setIsCheckPwd(YesOrNo.NO);
		user.setUsername(loginId);
		user.setPassword(password);
		user.setCis(cisCode);
		
		log.debug("===== scene5_OLD ===== sceneType = "+ sceneType +" ===== "+ new Date() + "----end---");
		return true;
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
		if(city == null||"".equals(city))
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
			
			// 分机号码
			String conexin = (String) json.get("conexin");
			conexin = conexin == null ? "" : conexin.trim();
			
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
			log.error("error updating ebankuseraddr",e);
		}
	}

	public MallLoginLogInfoService getMallLoginLogInfoService() {
		return mallLoginLogInfoService;
	}

	public void setMallLoginLogInfoService(
			MallLoginLogInfoService mallLoginLogInfoService) {
		this.mallLoginLogInfoService = mallLoginLogInfoService;
	}

	public BlackListDAO getBlackListDAO() {
		return blackListDAO;
	}

	public void setBlackListDAO(BlackListDAO blackListDAO) {
		this.blackListDAO = blackListDAO;
	}

	public CiscodeService getCiscodeService() {
		return ciscodeService;
	}

	public void setCiscodeService(CiscodeService ciscodeService) {
		this.ciscodeService = ciscodeService;
	}
	
	/**
	 * 通过CIS号查询电子银行渠道注册信息，同时开通个网&&手机银行&&状态正常，返回true
	 * @param cisCode
	 * @return
	 * @throws GtcgIOException
	 */
	private boolean isEbankAndMBank(String cisCode) throws GtcgIOException {
		// TODO Auto-generated method stub
		this.log.debug("PassQueryBankInfo  isEbankAndMBank starts----------------------------");
		uAOutputPassQueryBankInfo = null;
		UAInputPassQueryBankInfo uAInputPassQueryBankInfo = new UAInputPassQueryBankInfo();
		uAInputPassQueryBankInfo.setCIS_CODE(cisCode);
		uAOutputPassQueryBankInfo = (UAOutputPassQueryBankInfo)gtcg.sendToEbank(uAInputPassQueryBankInfo, "PassQueryBankInfo");

		if(uAOutputPassQueryBankInfo!=null&&"0".equals(uAOutputPassQueryBankInfo.getPublicInfo().getRetCode())){//调用成功
			String webFlag = uAOutputPassQueryBankInfo.getPrivateInfo().getWebFlag();//个人网银 0否 1是
			String wapFlag = uAOutputPassQueryBankInfo.getPrivateInfo().getWapFlag();//手机银行 0否 1是
			String webStatus = uAOutputPassQueryBankInfo.getPrivateInfo().getWebStatus();//个人网银状态 0正常 1冻结
			String wapStatus = uAOutputPassQueryBankInfo.getPrivateInfo().getWapStatus();//手机银行状态 0正常 1冻结
			log.debug("PassQueryBankInfo webFlag = "+webFlag+" && wapFlag = "+wapFlag+" && webStatus = "+webStatus+" && wapStatus = "+wapStatus);
			
			if("1".equals(webFlag) && "1".equals(wapFlag) && "0".equals(webStatus) && "0".equals(wapStatus)){
				return true;
			}else{
				return false;
			}
		}
		else{
			throw new GtcgIOException("统一登录认证查询电子银行渠道注册信息时出错。");
		}
	}


	public static void main(String[] args) {
//		"{\"provinceAddr\":\"北京市\",\"logonNum\":\"9558880200005890503\",
//		"{"sessionid":"","cisno":"020000300004173","timestamp"
//		"{\"sessionid\":\"\",\"cisno\":\"020000300004173\",\"timestamp\":\"1624602417809\",\"deviceid\":\"61653639313932363864343835646433\",\"funccode\":\"\",\"service\":\"http://icbc.huacai.com/touch/\",\"sceneType\":\"15\",\"channelIdentifiler\":\"EP2020062200005502.202106251426570809\"}\"

		String str = "sessionid=#cisno=020000300004173#timestamp=1624602417809#deviceid=61653639313932363864343835646433#funccode=#service=http://icbc.huacai.com/touch/#sceneType=15#channelIdentifiler=EP2020062200005502.202106251426570809";
		str = "{\"" + str.replace("#", "\",\"").replace("=","\":\"") + "\"}";
		System.out.println(str);
		JSONObject json = JSONObject.fromObject(str);
		System.err.println(json);
	}
	
}



