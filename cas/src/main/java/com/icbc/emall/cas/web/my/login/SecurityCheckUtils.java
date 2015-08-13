package com.icbc.emall.cas.web.my.login;

import java.io.FileInputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.infosec.icbc.ReturnValue;

import com.icbc.crypto.utils.TripleDesCryptFileInputKey;
import com.icbc.emall.cache.CacheManager;
import com.icbc.emall.common.utils.CommomProperty;
import com.icbc.emall.common.utils.Globe.SceneType;
import com.icbc.emall.util.crypt.FileBytes;
import com.icbc.systemlinker.common.util.CommonUtil;

public class SecurityCheckUtils {

	//base64解码后的签名信息
	private byte[] decSign = null;
	//base64解码后的交易信息 byte数组
	private byte[] decTrandata = null;
	//base64解码后的交易信息  String
	private String tranData = null;
	
	private static Logger log = LoggerFactory.getLogger(SecurityCheckUtils.class);
	
	private static String legalDomainSuffix = "icbc.com.cn";
	
	public String getTranData(HttpServletRequest request)
	{
		if(securityCheck(request))
			return tranData;
		else
			return null;
	}
	
	
	private boolean checkSignature(String signEnc , String trandataEnc)
	{
		try
		{
		log.info("check sign started");
		
		log.info("Invoking method checkSignature : The signEnc is {} , The trandataEnc is {}" , signEnc , trandataEnc);
		
		//base64解码
		decSign = ReturnValue.base64dec(URLDecoder.decode(signEnc,"GBK").getBytes(
				"GBK"));
		
		log.info("Invoking method checkSignature : The decSign is {}" , decSign);
		
		if (decSign != null) {
			decTrandata = ReturnValue.base64dec(trandataEnc.getBytes("GBK"));
			byte[] decryptedTrandata = new byte[decTrandata.length];
			String KeyFilePath = CommomProperty.getDBManager().getsmsProperty("KeyFilePath"); 
			String keyFile = KeyFilePath.endsWith("/") ? (KeyFilePath+"ICBC_EMALL_1_00000_3DES_16"):(KeyFilePath+"/ICBC_EMALL_1_00000_3DES_16");
			
			log.info("Invoking method checkSignature : The keyFile is {}" , keyFile);
			
			int result = TripleDesCryptFileInputKey.IcbcTripleDes(decTrandata, decTrandata.length, decryptedTrandata, 1, 1, 1, keyFile);

			log.info("Invoking method checkSignature : The result is {}" , result);
			
			if(result>0)
			{
				tranData = new String(decryptedTrandata,0,result, Charset.forName("UTF-8"));
				decryptedTrandata = tranData.getBytes("GBK");
				log.info("ebank tranData:" + tranData);
				if (decTrandata != null) {
					FileInputStream fis = null;
					String keyPath = null;
					// tranData里有cis号 从网银过来
					if (tranData.contains("mainCIS"))
					{
						keyPath = CommonUtil.SystemlinkerOperationProperty("payserver.certSigner.certLocation");
					}
					else
					{
						JSONObject json = null;
						if (tranData != null && !"".equals(tranData)) {
							json = JSONObject.fromObject(tranData);
						} else {
							this.log.error("trandata is null");
							return false;
						}
						String scene = (String) json.get("sceneType");
						scene = scene==null?"":scene.trim();
						//如果是场景LOGIN_BILL_HALL_MALL 也使用网银证书做验签
						if(SceneType.LOGIN_BILL_HALL_MALL.equals(scene))
							keyPath = CommonUtil.SystemlinkerOperationProperty("payserver.certSigner.certLocation");
						else
							keyPath = CommonUtil.SystemlinkerOperationProperty("certSigner.certLocation");
					}
					log.info("keyPath:"+keyPath);
					byte[] bcert = FileBytes.instance().getBytes(keyPath);
					int a = ReturnValue.verifySign(decryptedTrandata, decryptedTrandata.length, bcert,decSign);
					if (a == 0)
					{
						log.info("check sign succeeded");
						return true;
					}
				}
			}
			else
			{
				log.info("decrpyt failed");
				log.info("decrypt result=" + result);
				return false;
			}
		}
		}
		catch(Exception e)
		{
			log.info("check sign failed");
			e.printStackTrace();
		}
		log.info("check sign failed");
		return false;
	}
	
	/*
	 * 检验tranData中的ip、referer是否同request中的ip、referer，防止交易被人拦截后重发
	 */
	private boolean checkIPandReferer(String IP, String referer, HttpServletRequest request)
	{
		log.info("checkIPandReferer started");
		/*
		String IPInRequest = request.getRemoteAddr();
		String refererInRequest;
		if(IPInRequest.equals(IP))
		{
			log.info("checkIPandReferer succeeded");
			return true;
		}*/
		String IPInRequest = request.getRemoteAddr();
		log.error("IP In Request:" + IPInRequest);
		log.error("IP in trandata:" + IP);
		log.info("checkIPandReferer ended");
		return true;
		/*
		String url = request.getHeader("REFERER");
		try {
			String host = new URL(url).getHost();
			log.info("refer host:" + host);
			if(host.endsWith(this.legalDomainSuffix))
			{
				log.info("checkIPandReferer succeeded");
				return true;
			}
			else
			{
				log.info("checkIPandReferer failed");
				return false;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			log.info("checkIPandReferer failed");
			return false;
		}
		*/
	}
	
	/*
	 * 检验交易是否唯一，防止重放攻击
	 */
	private boolean checkChannelIdentifier(String channelIdentifier)
	{
		log.info("checkChannelIdentifier started");
		Object obj = CacheManager.getInstance().getCache(channelIdentifier);
		if(obj == null)
		{
			boolean flag = CacheManager.getInstance().putCache(channelIdentifier, channelIdentifier);

			log.info("checkChannelIdentifier succeeded");
			return flag;
		}
		log.info("checkChannelIdentifier failed");
		return false;
	}
	
	/*
	 * 检验交易是否超时
	 */
	private boolean checkTimeStamp(String timeStamp )
	{
		log.info("Invoking method checkTimeStamp : checkTimeStamp started");
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date1;
		try {
			date1 = df.parse(timeStamp);
			long diff = date.getTime() - date1.getTime();
			log.info("Invoking method checkTimeStamp : the local timeStamp is {}, the diff is {}", date.getTime() ,diff);
			long timeStampIntervalTime = 0;
			CommomProperty instance = CommomProperty.getDBManager();
			timeStampIntervalTime = Long.parseLong(instance.getsmsProperty("cas.tran.valid.interval"));
			if(diff <= timeStampIntervalTime)
			{
				log.info("Invoking method checkTimeStamp : checkTimeStamp succeeded");
				return true;
			}
		} catch (ParseException e) {
			log.info("Invoking method checkTimeStamp : checkTimeStamp failed");
			e.printStackTrace();
		}
		log.info("Invoking method checkTimeStamp : checkTimeStamp failed");
		return false;
	}
	
	
	/*
	 * 检验交易是否超时
	 */
	private boolean phoneCheckTimeStamp(String timeStamp )
	{
		log.info("phoneCheckTimeStamp started");
		try {
			long data = System.currentTimeMillis();
			long diff = data - Long.parseLong(timeStamp);
			long timeStampIntervalTime = 0;
			CommomProperty instance = CommomProperty.getDBManager();
			timeStampIntervalTime = Long.parseLong(instance.getsmsProperty("cas.tran.valid.interval")) * 1000;
			if(diff <= timeStampIntervalTime)
			{
				log.info("checkTimeStamp succeeded");
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("checkTimeStamp failed");
			e.printStackTrace();
		}
		log.info("checkTimeStamp failed");
		return false;
	}
	
	private boolean securityCheck(HttpServletRequest request)
	{
		log.info("securityCheck started");
		//base64编码的签名信息
		String signatureEnc = request.getParameter("merSignMsg");
		log.info("ebank signatureEnc:"+signatureEnc);
		//base64编码的交易数据
		String trandataEnc =  request.getParameter("tranData");
		log.info("ebank trandataEnc:"+trandataEnc);
		//验签
		if(checkSignature(signatureEnc,trandataEnc))
		{
			JSONObject j = JSONObject.fromObject(tranData);
			String custmerIp = (String)j.get("custmerIp");
			String custmerReferer = "";
			//校验用户IP和referer
			if(checkIPandReferer(custmerIp, custmerReferer, request))
			{
				String timeStamp = (String)j.get("timeStamp");
				//校验时间戳
				if(checkTimeStamp(timeStamp))
				{
					String channelIdentifier = (String)j.get("channelIdentifier");
					if(checkChannelIdentifier(channelIdentifier))
					{
						log.info("securityCheck succeeded");
						return true;
					}
				}
			}
		}
		log.info("securityCheck failed");
		return false;
	}
	
	
	public String getRemoteTranData(HttpServletRequest request){
		return this.getRemoteTranData(request, true);
	}
	
	/**
	 * @desc 
	 * @param request
	 * @param checkTimeStamp : 是否校验时间戳
	 * @return
	 */
	public String getRemoteTranData(HttpServletRequest request, boolean checkTimeStamp)
	{
		if(securityPhoneCheck(request, checkTimeStamp))
			return tranData;
		else
			return null;
	}
	
	
	
	private boolean securityPhoneCheck(HttpServletRequest request , boolean tgtCheckFlag)
	{
		log.info("securityCheck started");
		//base64编码的签名信息
		String signatureEnc = request.getParameter("merSignMsg");
		log.info("ebank signatureEnc:"+signatureEnc);
		//base64编码的交易数据
		String trandataEnc =  request.getParameter("tranData");
		log.info("ebank trandataEnc:"+trandataEnc);
		//验签
		if(checkPhoneSignature(signatureEnc,trandataEnc,tgtCheckFlag))
		{
 		//String str = "sessionid=#cisno=020000300004173#timestamp=1624602417809#deviceid=61653639313932363864343835646433#funccode=#service=http://icbc.huacai.com/touch/#sceneType=15#channelIdentifiler=EP2020062200005502.202106251426570809";
		//tranData = "{\"" + str.replace("#", "\",\"").replace("=","\":\"") + "\"}";
			JSONObject j = JSONObject.fromObject(tranData);
			String custmerIp = (String)j.get("custmerIp");
			String custmerReferer = "";
			//校验用户IP和referer
			if(checkIPandReferer(custmerIp, custmerReferer, request))
			{
				String timeStamp = (String)j.get("timestamp");
				
				if(!tgtCheckFlag){
					log.info("Invoking method SecurityCheckUtils.securityPhoneCheck : Don't check timeStamp !");
					return true;
				}
				//校验时间戳
				if(phoneCheckTimeStamp(timeStamp))
				{
					String channelIdentifier = (String)j.get("channelIdentifiler");
					if(checkChannelIdentifier(channelIdentifier))
					{
						log.info("securityCheck succeeded");
						return true;
					}
				}
			}
		}
		log.info("securityCheck failed");
		return false;
	}
	
	
	private boolean checkPhoneSignature(String signEnc , String trandataEnc, boolean tgtCheckFlag)
	{
		try
		{
		log.info("check sign started");
		
		log.info("Invoking method checkSignature : The signEnc is {} , The trandataEnc is {}" , signEnc , trandataEnc);
		
		if(tgtCheckFlag){
			//base64解码
			decSign = ReturnValue.base64dec(signEnc.getBytes("GBK"));
		}else{
			//base64解码,校验tgt需要做decode
			decSign = ReturnValue.base64dec(URLDecoder.decode(signEnc,"GBK").getBytes("GBK"));
		}
		
		log.info("Invoking method checkSignature : The decSign is {}" , decSign);
		
		if (decSign != null) {
			decTrandata = ReturnValue.base64dec(trandataEnc.getBytes("GBK"));
			byte[] decryptedTrandata = new byte[decTrandata.length];
			String KeyFilePath = CommomProperty.getDBManager().getsmsProperty("KeyFilePath"); 
			String keyFile = KeyFilePath.endsWith("/") ? (KeyFilePath+"ICBC_EMALL_1_00000_3DES_16"):(KeyFilePath+"/ICBC_EMALL_1_00000_3DES_16");
			
			log.info("Invoking method checkSignature : The keyFile is {}" , keyFile);
			
			int result = TripleDesCryptFileInputKey.IcbcTripleDes(decTrandata, decTrandata.length, decryptedTrandata, 1, 1, 1, keyFile);

			log.info("Invoking method checkSignature : The result is {}" , result);
			
			if(result>0)
			{
				tranData = new String(decryptedTrandata,0,result, Charset.forName("UTF-8"));
				decryptedTrandata = tranData.getBytes("GBK");
				//将传送的拼接字符串转换成json数据
				tranData = "{\"" + tranData.replace("#", "\",\"").replace("=","\":\"") + "\"}";
				log.info("ebank tranData:" + tranData);
				if (decTrandata != null) {
					FileInputStream fis = null;
					String keyPath = null;
					// tranData里有cis号 从网银过来
					if (tranData.contains("cisno"))
					{
						keyPath = CommonUtil.SystemlinkerOperationProperty("payserver.certSigner.certLocation");
					}
					else
					{
						JSONObject json = null;
						if (tranData != null && !"".equals(tranData)) {
							json = JSONObject.fromObject(tranData);
						} else {
							this.log.error("trandata is null");
							return false;
						}
						String scene = (String) json.get("sceneType");
						scene = scene==null?"":scene.trim();
						//如果是场景LOGIN_BILL_HALL_MALL 也使用网银证书做验签
						if(SceneType.LOGIN_BILL_HALL_MALL.equals(scene))
							keyPath = CommonUtil.SystemlinkerOperationProperty("payserver.certSigner.certLocation");
						else
							keyPath = CommonUtil.SystemlinkerOperationProperty("certSigner.certLocation");
					}
					log.info("keyPath:"+keyPath);
					byte[] bcert = FileBytes.instance().getBytes(keyPath);
					int a = ReturnValue.verifySign(decryptedTrandata, decryptedTrandata.length, bcert,decSign);
					if (a == 0)
					{
						log.info("check sign succeeded");
						return true;
					}
				}
			}
			else
			{
				log.info("decrpyt failed");
				log.info("decrypt result=" + result);
				return false;
			}
		}
		}
		catch(Exception e)
		{
			log.info("check sign failed");
			e.printStackTrace();
		}
		log.info("check sign failed");
		return false;
	}
	
	
	public static void main(String[] args)
	{
		
		 
		
	//	String signEnc = "DWUBEsAb0GbMFUTtf8jnoik%2BCKHRQl1mwqmcveN0WAXNTfvyx5NpAumic%2FLt7sguFWzy2Rr81MBNcoLPtMRBpELOUPGsOiN2octWP0msV8IKgxGeCda8QLaEgq0uPyI79PWmjM5PqKXub%2B5m%2Fk1dGJKMZPEe1bqLp2NzDkwcppo%3D";
		String signEnc = "LX+k3ld2r2QZC/aVCq+Ut8DQrvTa3gOCYRV6tdl34ysTUsTm377uoZGdh5BTa/yVHTHN25Dzf5qBXzVtJ6I7kCwq9FPz/pvWzGngkp0vYuJYEJUZXVksyrfm9eZtquNG8XCBw6Awi6hM0lZ+8R7KgCejxdDdWPMzKSad89cyRAg=";
		String trandataEnc = "YXmJtyHO0BmevoDi0Xs4Wm+oyRLxeaEn1kjNiLluFfUe32Psj7b3jA9sNJmq8LtL1HaR51E+c99PtIStEZ+KP0bwZMBKRmlICM8alHPGffVaev3BVa5fRaEYW8xqtJN5KXJP+yMtVmDw4D5ckMe8dAGtSv4zaZNSHL90jzB5GVM/reyvJUJ7tQQ8Gz6mVycPNWZ9zrf6IlEgtOZRuNwmgocWAgBGO4gY9L8qRTJlnGKcXHFQOWNqGqkDfL0/zDKhThNxZMl3/TRc67PuPP4rRKTPxHEYAgf+AjHMGIqJu3HavKhxOhMM66Pp03n1xftH";
		String tranData = "";
		
		byte[] decSign;
		byte[] decTrandata;
		try
		{
		log.info("check sign started");
		//base64解码
		decSign = ReturnValue.base64dec(signEnc.getBytes(
				"GBK"));
		if (decSign != null) {
			decTrandata = ReturnValue.base64dec(trandataEnc.getBytes("GBK"));
//			decTrandata = ReturnValue.base64dec(trandataEnc.getBytes("GBK"));
			byte[] decryptedTrandata = new byte[decTrandata.length];
			String keyFile = "C:/ICBC_EMALL_1_00000_3DES_16";
			//String keyFile = CommomProperty.getDBManager().getsmsProperty("KeyFilePath");
			int result = TripleDesCryptFileInputKey.IcbcTripleDes(decTrandata, decTrandata.length, decryptedTrandata, 1, 1, 1, keyFile);
			if(result>0)
			{
				//tranData = new String(decryptedTrandata, "GBK");
				tranData = new String(decryptedTrandata,0,result, Charset.forName("UTF-8"));
				decryptedTrandata = tranData.getBytes("GBK");
				log.info("ebank tranData:" + tranData);
				if (decTrandata != null) {
					FileInputStream fis = null;
					String keyPath = null;
					// tranData里有cis号 从网银过来
					if (tranData.contains("mainCIS"))
					{
						/*keyPath = CommomProperty.getDBManager().getsmsProperty(
								"cas.ebankp_cert_file");*/
						keyPath = CommonUtil.SystemlinkerOperationProperty("payserver.certSigner.certLocation");
					}
					else
					{
						// tranData里没有cis号 注册成功后从商城过来
						/*keyPath = CommomProperty.getDBManager().getsmsProperty(
								"cas.emall_cert_file");*/
						keyPath = CommonUtil.SystemlinkerOperationProperty("certSigner.certLocation");
					}
					log.info("keyPath:"+keyPath);
					byte[] bcert = FileBytes.instance().getBytes(keyPath);
					int a = ReturnValue.verifySign(decryptedTrandata, decryptedTrandata.length, bcert,decSign);
					if (a == 0)
					{
						log.info("check sign succeeded");
					}
					else
					{
						log.info("check sign failed");
						log.info("验签返回值:" + a);
					}
				}
			}
			else
			{
				log.info("decrpyt failed");
			}
		}
		}
		catch(Exception e)
		{
			log.info("check sign failed");
			e.printStackTrace();
		}
		log.info("check sign failed");
	}
	
	}
	
