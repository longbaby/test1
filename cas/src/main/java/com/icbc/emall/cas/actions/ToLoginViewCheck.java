package com.icbc.emall.cas.actions;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.icbc.emall.Constants;
import com.icbc.emall.Constants.SWITCH;
import com.icbc.emall.EmallServiceException;
import com.icbc.emall.appswitch.service.SwitchService;
import com.icbc.emall.cache.CacheManager;
import com.icbc.emall.cas.validate.ICBCCASAuthenticationException;
import com.icbc.emall.common.utils.CommomProperty;
import com.icbc.emall.common.utils.Globe.LoginTarget;
import com.icbc.emall.system.service.SysParamsService;

public class ToLoginViewCheck extends AbstractAction {

	public static final Logger logger = LoggerFactory.getLogger(ToLoginViewCheck.class);
	@Autowired
	private SysParamsService sysParamsService;
	@Autowired
	private SwitchService switchService;
	
//	 public String IN_OUT_TAIR_KEY="switch0007%1$sLoginInOneSwitch";
	 public String IN_OUT_TAIR_KEY="switch%2$s%1$sLoginInOneSwitch";
	
	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		logger.debug("Excuting method doExecute .");
		//to 从request中获取，决定去哪里
		String to = context.getRequestParameters().get("to");
		logger.debug("to value is " + to);
		if (LoginTarget.PERSION_EBANK.equals(to)) {
			return result("ebank");
		} else if (LoginTarget.PERSION_PC_IPAD.equals(to)) {
//			return result("ipad");
			//进行处理是否启用新页面
			String isPageValue = switchFlag(Constants.APP_TYPE.MALL_PAD.getValue());
			if("1".equals(isPageValue)){
				return result("ipad_onepage");
			}else{
				return result("ipad");
			}
		} else if (LoginTarget.MERCHANT_PERSION.equals(to)) {
			
			//submit  1 商户正常登录提交  2 表示手机验证码提交
			String submit = context.getRequestParameters().get("issubmit");
			RequestAttributes ra = RequestContextHolder
					.currentRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) ra)
					.getRequest();
			
			//屏蔽b2b商户
			String value = sysParamsService.getValueByInnerName(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH);
			if(StringUtils.endsWith(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH_OFF, value)) {
				request.setAttribute(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH, Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH_OFF);
			}
			
			//sub  1 商户正常登录提交  2 商户正常登录提交出错
			//phonesub 1 商户手机验证码提交出错
			//submit　　＝１　　用户名密码提交界面隐藏域　　用于下一步跳转判断
			//submit　　＝2 　手机验证码提交界面隐藏域表示手机验证码提交
			if ("1".equals(request.getAttribute("sub")) && "1".equals(submit) || "1".equals(request.getAttribute("phonesub"))) {
				logger.debug("Running merchant_phone...");
				return result("merchant_phone");
			} else if ("2".equals(submit)) {
				logger.debug("Running merchant_phone_sub...");
				return result("merchant_phone_sub");
			} else {
				logger.debug("Running merchant...");
				return result("merchant");
			}
		} else if (LoginTarget.B2B_PERSON_EBANK.equals(to)) {
			return result("b2b_ebank");
		}else if(LoginTarget.B2C_MALL_HELP.equals(to)) {
			return result("default_help");
			
		}else {
			return result("default");
			//进行处理是否启用新页面
			
//			String isPageValue = switchFlag(Constants.APP_TYPE.ONE_PAGE.getValue());
//			
//			if("1".equals(isPageValue)){
//				return result("default_onepage");
//			}else{
//				return result("default");
//			}
		}

	}

	private String switchFlag(String page) throws ICBCCASAuthenticationException,
			EmallServiceException {
		//通过查询env_added.propertie查询参数 env_added.IN_OUT_FLAG,是否启用新页面
		String inOutFlag = CommomProperty.getDBManager().getAddedProperty("IN_OUT_FLAG");
		//处理判断出tairKey
		String tairKey = String.format(this.IN_OUT_TAIR_KEY, inOutFlag, page);
		//获取tair服务器的实例
		CacheManager cacheManager =  CacheManager.getInstance();
		if (cacheManager == null) {
			logger.error("加载tair实例失败！");
			throw new ICBCCASAuthenticationException("tair服务器启动失败","系统内部错误");
			
		 }
		/**
		 * 
		 * 以key=switch00071LoginInOneSwitch或key=switch00072LoginInOneSwitch缓存（缓存10分钟）。结果为1表示该服务器支持一个登录页签，否则不支持。默认值为不支持，当程序异常或取不到值时默认为不支持。
		**/
		String isPageValue = (String) cacheManager.getCache(tairKey);
		
		if(StringUtils.isBlank(isPageValue)){
			isPageValue = switchService.getValueByInnerName(page , inOutFlag,SWITCH.LOGININ_ONE_SWITCH.getValue());
			if(StringUtils.isNotBlank(isPageValue)){
				cacheManager.putCache(tairKey, isPageValue ,600 );
			}
		} 
		
		if(StringUtils.isBlank(isPageValue)){
			isPageValue = "0";
		}
		return isPageValue;
	}

	protected String toLoginViewCheck(final RequestContext context)
			throws Exception {
				String to = context.getRequestParameters().get("to");
		if (LoginTarget.PERSION_EBANK.equals(to)) {
			return "ebank";
		} else if (LoginTarget.PERSION_PC_IPAD.equals(to)) {
			return "ipad";
		} else if (LoginTarget.MERCHANT_PERSION.equals(to)) {
			return "merchant";
		} else {
			return "default";
		}

	}
	
	/**
	 * @desc 手机
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public final String phoneLoginViewCheck(final RequestContext context)
			throws Exception {
		
		String isPageValue = switchFlag(Constants.APP_TYPE.MALL_MOBILE.getValue());
		
		if("1".equals(isPageValue)){
			return "phone_one";
		}else{
			return "phone";
		}
	}

	public final String merchentLoginViewCheck(final RequestContext context)
			throws Exception {
		logger.debug("Executing method merchantLoginViewCheck.");
		String submit = context.getRequestParameters().get("issubmit");
		logger.debug("submit value is " + submit);
		RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra)
				.getRequest();
		
		//屏蔽b2b商户
		String value = sysParamsService.getValueByInnerName(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH);
		if(StringUtils.endsWith(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH_OFF, value)) {
			request.setAttribute(Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH, Constants.B2B_MERCHANT_LOGIN_VISIT_SWITCH_OFF);
		}
		
		//sub　＝1　表示正常提交用户名和密码
		//submit　　＝１　　用户名密码提交界面隐藏域　　用于下一步跳转判断
		//submit　　＝2 　手机验证码提交界面隐藏域表示手机验证码提交
		if ("1".equals(request.getAttribute("sub")) && "1".equals(submit)) {
			logger.debug("Running merchant_phone...");
			return "merchant_phone";
		} else if ("2".equals(submit)) {
			logger.debug("Running merchant_phone_sub...");
			return "merchant_phone_sub";
		} else {
			logger.debug("Running merchant...");
			return "merchant";
		}

	}

	public SwitchService getSwitchService() {
		return switchService;
	}

	public void setSwitchService(SwitchService switchService) {
		this.switchService = switchService;
	}

}
