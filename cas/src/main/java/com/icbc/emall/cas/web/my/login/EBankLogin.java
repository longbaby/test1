package com.icbc.emall.cas.web.my.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.icbc.emall.common.utils.Crypt;
import com.icbc.emall.common.utils.Globe.LoginChannels;
import com.icbc.emall.common.utils.Globe.SceneType;
import com.icbc.finance.pmis.common.CommomProperty;

public class EBankLogin extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory
			.getLogger(EBankLogin.class);
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String isb2b = request.getParameter("isb2b");
				
		String eMallFieldValue="";
		
		Map<String,String> map1 = new HashMap<String,String>();
		CommomProperty instance = CommomProperty.getDBManager();
		
		//service url
		String casClientUrl = instance.getsmsProperty("b2b.cas.client.url");  //个人网银登录后返回url
		String serviceFlag = request.getParameter("service");
		if(serviceFlag != null && !"".equals(serviceFlag)){
			casClientUrl = serviceFlag;
		}
		
		//String mobileType = request.getParameter("mobileType");
		
		map1.put("sceneType", SceneType.LOGIN_AFTER_EBANKP);  //以网银身份登录商城成功后跳转回商城,且还包含通过网银登录页面直接进入购物中心或在网银里点击去商城逛逛
		//map1.put("loginWay", "1");
		map1.put("loginChannels", LoginChannels.INTERNET);
		map1.put("service", casClientUrl);
		map1.put("isb2b", isb2b);
		
		JSONObject obj = JSONObject.fromObject(map1);
		eMallFieldValue = obj.toString();
		
		System.out.println("eMallField=" + eMallFieldValue);
		String eMallFieldKey = "";
		try {
			eMallFieldKey = Crypt.encrypt(eMallFieldValue, "UTF-8", 1, 0);
		} catch (Exception e) {
			logger.debug("3desenc",e);
		}
		String logonCardNum = request.getParameter("logonCardNum");  //卡号
		String logonCardPass = request.getParameter("logonCardPass");  //密码
		//String changerule = request.getParameter("changerule");
		//String rule = request.getParameter("rule");
		//String isMobileLogin = request.getParameter("isMobileLogin");
		String netType = request.getParameter("netType");
		String isEmallLogin = request.getParameter("isEmallLogin");
		String randomId = request.getParameter("randomId");
		String verifyCode = request.getParameter("verifyCode");
		
		
		//request.setAttribute("service", "http://www.icbc.com.cn");
		String casEBankLoginUrl = instance.getsmsProperty("cas.mobile.eBanklogin");
		logger.debug("cas.mobile.eBanklogin:" + casEBankLoginUrl);
		SecurityContextHolder.getContext().setAuthentication(null);
//		requestCache.saveRequest(request, response);
//		response.sendRedirect(casEBankLoginUrl+"?"+"eMallField="+eMallFieldKey);
		StringBuffer redirectUrl = new StringBuffer();
		
//		//网银账号写死数据qianfang  111111a
//		redirectUrl.append(casEBankLoginUrl)
//		.append("?eMallField=")
//		.append("DE2EF1EF7A55815F312E2A3652B3950FA3A71BEFCF26B454F913D8392501CD050329D05B15C746174A659D99E24C7311546CBC5430591F9FC413EDD2E02F337827FE54D9A454018DD476A408F71D69E3CF5FCF132576DF0BB07939BFB9AE531221761A3D0F068D2D26962E7E4742D467F64B2E58C57E19CC")
//		.append("&logonCardNum=")
//		.append("qianfang")
//		.append("&logonCardPass=")
//		.append("1B45D000E069C906F5187D0E3F9BC79E2215AC3DC4D077ECD6ECBC1408918B3AA9AF5FB076ADF09CA5EAACC0D5C49D43999B9C64EB4B9400EC79E77EAE25C99A2AFA04CE2D4894BD3D02C7BC9CB9FD71DE8E9A995E3EF4808460410A18238BD9D30CEBD9B212772F5021577FB30373601780D8D32DED7B6860C6B2BBEA6FA02A")
//		.append("&changerule=")
//		.append("1247")
//		.append("&rule=")
//		.append("113121222101311442341542711632862282215223121432182121")
//		.append("&isMobileLogin=")
//		.append("1")
//		.append("&netType=")
//		.append("111");
		
//		redirectUrl.append(casEBankLoginUrl).append("?eMallField=").append("DE2EF1EF7A55815F312E2A3652B3950FA3A71BEFCF26B454F913D8392501CD050329D05B15C746174A659D99E24C7311546CBC5430591F9FC413EDD2E02F337827FE54D9A454018D8804541E190C48BC1E2CBB127B22ACFA5BE46D8D5FE5374B1BFFA1CC9412BD04A068272583D68AE4454434A9813E30907DF3C9CEEEC27694").append("&logonCardNum=").append("qianfang").append("&logonCardPass=")
//		.append("0E822EB4397E6F04050C1554C22AD20A0C3F9EE4FB6D0917E7A93619445B0C5AFD013F1895F5E1165AE928C7A3D5ADD81B52B08E44A703B553D873AF9C8DDA22CFDD6EA9F71BD87781702CC5BD26027F5168864E56386E0BBFCE055630BA78E9903E7EF66CE5ED11ED26138C178C75C96E265BD97ABCED639647AADCF18B02AA").append("&changerule=").append("1283").append("&rule=").append("1122531211342731452201552831652941832332155270214525622132792103241217315").append("&isMobileLogin=")
//		.append("1").append("&netType=").append("111");
		
		
		//动态数据
		redirectUrl.append(casEBankLoginUrl)
		.append("?eMallField=").append(eMallFieldKey)
		.append("&logonCardNum=").append(logonCardNum)
		.append("&logonCardPass=").append(logonCardPass)
		//.append("&changerule=").append(changerule)
		//.append("&rule=").append(rule)
		//.append("&isMobileLogin=").append(isMobileLogin)
		.append("&netType=").append(netType)
		.append("&isEmallLogin=").append(isEmallLogin)
		.append("&randomId=").append(randomId)
		.append("&verifyCode=").append(verifyCode);
		
		System.out.println(redirectUrl.toString());
		logger.debug("cas.mobile.eBanklogin&redirectUrl:" + redirectUrl.toString());
		response.sendRedirect(redirectUrl.toString());
		
//		request.getRequestDispatcher(casEBankLoginUrl+"?"+"eMallField="+eMallFieldKey).forward(request, response);
//		request.getRequestDispatcher("/test?"+"eMallField="+eMallFieldKey).forward(request, response);
		
	}
}





























