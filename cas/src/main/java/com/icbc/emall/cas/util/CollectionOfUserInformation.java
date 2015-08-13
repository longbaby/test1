package com.icbc.emall.cas.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icbc.emall.EmallServiceException;
import com.icbc.emall.auth.model.Auth;
import com.icbc.emall.auth.service.AuthService;
import com.icbc.emall.ciscode.model.Ciscode;
import com.icbc.emall.ciscode.service.CiscodeService;
import com.icbc.emall.common.model.Address;
import com.icbc.emall.common.service.AddressService;
import com.icbc.emall.common.utils.Globe.MallUserType;
import com.icbc.emall.common.utils.Globe.YesOrNo;
import com.icbc.emall.common.utils.SpringContextLoaderListener;
import com.icbc.emall.ebankuseraddr.model.EBankUserAddr;
import com.icbc.emall.ebankuseraddr.service.EbankUserAddrService;
import com.icbc.emall.lottery.model.LotteryUserinfo;
import com.icbc.emall.mall.model.MallLoginInfo;
import com.icbc.emall.mall.service.MallLoginInfoService;
import com.icbc.emall.member.model.MallUserInfo;
import com.icbc.emall.member.service.MallUserInfoService;
import com.icbc.emall.merchant.dao.AreacodeMapDAO;
import com.icbc.emall.merchant.model.AreacodeMap;
import com.icbc.emall.merchant.service.AreacodeMapService;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.queryInfo.UAOutputQueryInfo;
import com.icbc.emall.util.gtcg.model.output.unifiedAuth.realNameQuery.UAOutputRealNameQuery;
import com.icbc.emall.util.keygen.Constants;
import com.icbc.emall.util.keygen.SerialGeneratorMgr;
import com.icbc.emall.utils.StringUtil;
/**
 * @desc 商城用户信息补录
 * @author kfzx-fanyk
 *
 */
public class CollectionOfUserInformation {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private MallLoginInfoService mallLoginInfoService;
	
	private MallUserInfoService mallUserInfoService;
	
	private AuthService authService;
	
	private CiscodeService ciscodeService;
	
	private SerialGeneratorMgr serialGeneratorMgr;
	
	private EbankUserAddrService eBankUserAddrService;
	
	private AddressService addressService;
	
	private AreacodeMapDAO areacodeMapDAO;
	
	
	
	/**
	 * @desc 同步本地数据与统一通行证一致
	 * @param output	统一通信证数据
	 * @param cisCode	cisCode
	 * @param sessionid	
	 * @param custmerIp	客户IP
	 * @param custmerMAC 客户MAC
	 * @param loginChannels 登录渠道
	 * @param userbrowser	使用浏览器
	 * @param useros	
	 * @param loginWay
	 * @param custName
	 * @param mobile
	 * @throws Exception
	 */
	public void syncLocalInfo(UAOutputQueryInfo output,String cisCode,String sessionid,String custmerIp,String custmerMAC,String loginChannels,String userbrowser,String useros,String loginWay,String custName,String mobile) throws Exception{

		this.logger.debug("Invoking method CollectionOfUserInfomation.syncLocalInfo : local userinfo sync start -----------------------------------------");
		Auth authInfo = null;
		MallUserInfo mallUserInfo = null;
		String userid = output.getPrivateInfo().getUserid();
		authInfo = authService.getAuthByUserId(null, null, userid);
		mallUserInfo  = this.mallUserInfoService.getMallUserById1(userid);
		
		if(!StringUtil.isBlank(mobile)){
			output.getPrivateInfo().setMobile(mobile);
		}
		
		if(authInfo == null)
		{
			//补录信息
			this.logger.info("Invoking method CollectionOfUserInfomation.syncLocalInfo : no userinfo in mall");
			this.logger.info("Invoking method CollectionOfUserInfomation.syncLocalInfo : add auth & malluserinfo &ciscode &lotteryuserinfo in mall");
			authInfo = this.createAuth(output);
			mallUserInfo = createMallUserInfo(output);
			if( !StringUtil.isBlank(custName) ){
				mallUserInfo.setRealName(custName);
			}
			
			MallLoginInfo mallLogInfo = createLoginLog(userid,sessionid,custmerIp,custmerMAC,loginChannels,userbrowser,useros,loginWay);
			LotteryUserinfo lotteryUserinfo = new LotteryUserinfo();
			lotteryUserinfo.setUserId(userid);
			String outuserid = getSerialGeneratorMgr().getSerialKey(Constants.LOTTERYUSERINFODSERIAL).trim();
			lotteryUserinfo.setOutuserId(outuserid);
			Ciscode ciscode = new Ciscode();
			ciscode.setCiscode(cisCode);
			ciscode.setUserid(userid);
			try
			{
				//ciscodeService.addUser(mallUserInfo, auth, null,mallLogInfo,lotteryUserinfo);
				ciscodeService.addUser(mallUserInfo, authInfo, ciscode,mallLogInfo,lotteryUserinfo);

			}
			catch(Exception e)
			{
				this.logger.error("error add userinfo in mall");
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
		this.logger.debug("local userinfo sync end----------------------------------------------------");
	}
	
	
	/**
	 * @desc 用户信息是否一致判断处理
	 * @param authInfo
	 * @param mallUserInfo
	 * @param output
	 * @return
	 */
	private int isUserInfoConsistent(Auth authInfo, MallUserInfo mallUserInfo,UAOutputQueryInfo output) {
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
	
	/**
	 * @desc 判断商城malluserinfo表中信息是否与统一认证相同
	 */
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
	
	
	/**
	 * @desc 判断商城auth表中信息是否与统一认证相同
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
	
	
	/**
	 * @desc 本地存在该用户信息进行同步
	 * @param output
	 * @return
	 */
	private Auth createAuth(UAOutputQueryInfo output){
		Auth authInfo = new Auth();
		authInfo.setUserid(output.getPrivateInfo().getUserid());
		authInfo.setUserType(output.getPrivateInfo().getUserType());
		authInfo.setIsLock(output.getPrivateInfo().getIsLock());
		authInfo.setIsEnable(output.getPrivateInfo().getIsEnable());
		authInfo.setLoginId(output.getPrivateInfo().getLoginID());
		return authInfo;
	}
	
	
	
	/**
	 * @desc 本地存在该用户信息，同步远程到本地
	 * @param output
	 * @return
	 */
	private MallUserInfo createMallUserInfo(UAOutputQueryInfo output) {
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
			this.logger.error("行政地区号转化成行内地区号出错.");
			logger.error("",e);
		}
		return mui;
	}
	
	
	//适用于网银用户首次登录 慎用
		public MallUserInfo createMallUserInfo(String userid,String aliasName,String cisCode,String custName,String mainAreaCode,String ebankUserLevel,String mobile) {
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
			logger.info("mainAreaCode:" + mainAreaCode);
			AreacodeMap areacodeMap= this.getAreacodeMapDAO().selectByPrimaryKey(mainAreaCode);
			if(areacodeMap != null)
			{
				mallUserInfo.setCity(areacodeMap.getCity());
				mallUserInfo.setProvince(areacodeMap.getProvince());
			}
			return mallUserInfo;
		}
	
	/**
	 * @desc 操作日志
	 * @param userId
	 * @param sessionid
	 * @param custmerIp
	 * @param custmerMAC
	 * @param loginChannels
	 * @param userbrowser
	 * @param useros
	 * @param loginWay
	 * @return
	 * @throws ParseException
	 */
	public MallLoginInfo createLoginLog(String userId,String sessionid,String custmerIp,String custmerMAC,String loginChannels,String userbrowser,String useros,String loginWay) throws ParseException {
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
	
	
	
	
	
	
	public void updateEbankUserAddr(UAOutputRealNameQuery realNameQuery)
	{
		try
		{
			//网银用户 省
			String provinceAddr = realNameQuery.getPrivateInfo().getProvinceAddr();
			
			
			// 网银用户 市
			String cityAddr = realNameQuery.getPrivateInfo().getCityAddr();
			// 网银用户 县/区
			String countyAddr = realNameQuery.getPrivateInfo().getCountyAddr();
			// 网银用户 通讯地址
			String commAddr = realNameQuery.getPrivateInfo().getCommAddr();
			// 网银用户 邮编
			String postalcode = realNameQuery.getPrivateInfo().getPostalcode();
			// 网银用户 电话区号
			String conarea = realNameQuery.getPrivateInfo().getConarea();
			// 网银用户 联系电话
			String teleNum = realNameQuery.getPrivateInfo().getTeleNum();
			// 网银用户 公司电话
			String companyTel = realNameQuery.getPrivateInfo().getCompanyTel();
			// 网银用户 家庭电话
			String homeTel = realNameQuery.getPrivateInfo().getHomeTel();
			// 网银用户 手机号
			String mobileNum = realNameQuery.getPrivateInfo().getMobileNum();
			String custName = realNameQuery.getPrivateInfo().getCustName();
			String cisCode = realNameQuery.getPrivateInfo().getMainCIS();
			Address address  = null;
			
			if(StringUtils.isNotBlank(provinceAddr)){
				address = new Address();
				address.setName(provinceAddr.length()<=2?provinceAddr:provinceAddr.substring(0, 2));
				address.setPid(AddressService.TOP_NODE_ADDRESS_ID);
				address = this.getAddressService().getAddressByProvinceName(address);
			}
			
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
				
				address = getAddressService().getAddressByCityNameAndProvID(address);
		
				//如果查到市
				if(address != null)
				{
					eBankUserAddr.setCity(address.getId());
					
					address.setPid(address.getId());
					address.setName(countyAddr);
					
					address = this.getAddressService().getAddressByCountyNameAndCityID(address);
					if(address != null)
					{
						eBankUserAddr.setDistrict(address.getId());
					}
				}
				
			}
			if(this.geteBankUserAddrService().getAddrByCIS(cisCode) == null)
			{
				geteBankUserAddrService().addEBankUserAddr(eBankUserAddr);
			}
			else
			{
				geteBankUserAddrService().updateEBankUserAddr(eBankUserAddr);
			}
		}
		catch(Exception e)
		{
			logger.error("error updating ebankuseraddr",e);
		}
	}
	
	
	
	private String handleException(String cityAddr) {
		

		Map<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("澳门", "澳门辖区");
		hashMap.put("北京", "北京市");
		hashMap.put("台湾", "台湾辖区");
		hashMap.put("天津", "天津市");
		hashMap.put("铜仁", "铜仁市");
		hashMap.put("香港", "香港辖区");
		hashMap.put("襄樊", "襄阳市");
		hashMap.put("重庆", "重庆市");
	
		
		String city = (String)(hashMap.get(cityAddr.length()<=2?cityAddr:cityAddr.substring(0,2)));	
		if(city == null||"".equals(city))
			return cityAddr;
		else
			return city;
	}
	
	
	
	

	public MallLoginInfoService getMallLoginInfoService() {
		return mallLoginInfoService;
	}

	public void setMallLoginInfoService(MallLoginInfoService mallLoginInfoService) {
		this.mallLoginInfoService = mallLoginInfoService;
	}

	public MallUserInfoService getMallUserInfoService() {
		return mallUserInfoService;
	}

	public void setMallUserInfoService(MallUserInfoService mallUserInfoService) {
		this.mallUserInfoService = mallUserInfoService;
	}

	public AuthService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}

	public CiscodeService getCiscodeService() {
		return ciscodeService;
	}

	public void setCiscodeService(CiscodeService ciscodeService) {
		this.ciscodeService = ciscodeService;
	}



	public SerialGeneratorMgr getSerialGeneratorMgr() {
		return serialGeneratorMgr;
	}



	public void setSerialGeneratorMgr(SerialGeneratorMgr serialGeneratorMgr) {
		this.serialGeneratorMgr = serialGeneratorMgr;
	}


	public EbankUserAddrService geteBankUserAddrService() {
		return eBankUserAddrService;
	}


	public void seteBankUserAddrService(EbankUserAddrService eBankUserAddrService) {
		this.eBankUserAddrService = eBankUserAddrService;
	}


	public AddressService getAddressService() {
		return addressService;
	}


	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}


	public AreacodeMapDAO getAreacodeMapDAO() {
		return areacodeMapDAO;
	}


	public void setAreacodeMapDAO(AreacodeMapDAO areacodeMapDAO) {
		this.areacodeMapDAO = areacodeMapDAO;
	}
	
	
}
