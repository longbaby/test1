package com.icbc.emall.ajaxcontroller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.jasig.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.icbc.by.util.string.StringUtils;
import com.icbc.emall.EmallServiceException;
import com.icbc.emall.cache.CacheManager;
import com.icbc.emall.cas.web.my.login.SecurityCheckUtils;

@Controller
public class TGTValidateController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * @desc 验证tgt的有效性
	 * @param request
	 * @param response
	 * @throws EmallServiceException
	 */
	@RequestMapping("/ticketValidate.ajax")
	@ResponseBody
	public void validateTGT(HttpServletRequest request, HttpServletResponse response) throws EmallServiceException {
		
		logger.info("Invoking method TGTValidateController.validateTGT :  this method start =======================");
		
		//RETCODE 为1失败， 为0成功 ， 默认失败
		String outPut= "{\"PRIVATE\":{\"Service\":\"\"},\"PUBLIC\":{\"RETMSG\":\"default data!\",\"RETCODE\":\"1\"}}";
		String ticketId="";
		String service="";
		try{
			String jesion = new SecurityCheckUtils().getRemoteTranData(request, false);
			
			JSONObject json = null;
			
			if (jesion != null && !"".equals(jesion)) {
				json = JSONObject.fromObject(jesion);
			} else {
				logger.error("Invoking method TGTValidateController.validateTGT :  The json is {}", json);
			}
			
			ticketId = (String) json.get("sessionid");
			service = (String) json.get("service");
			
		}catch(Exception e){
			logger.error("Invoking method TGTValidateController.validateTGT :  Decrypt error");
		}
		
		if(StringUtils.isBlank(ticketId)){
			logger.error("Invoking method TGTValidateController.validateTGT :  The ticketId is null !");
			outPut = "{\"PRIVATE\":{\"Service\":\""+service+"\"},\"PUBLIC\":{\"RETMSG\":\"ticketId is null !\",\"RETCODE\":\"1\"}}";
		}else{
			try{
				CacheManager client=CacheManager.getInstance();
				Ticket t =(Ticket)client.getCache(ticketId);
				logger.debug("Invoking method TGTValidateController.validateTGT :  The ticketId is {} ! , The service is {} !", ticketId, service);
				if(t==null){
					outPut = "{\"PRIVATE\":{\"Service\":\""+service+"\"},\"PUBLIC\":{\"RETMSG\":\"\",\"RETCODE\":\"1\"}}";
				}else{
					outPut = "{\"PRIVATE\":{\"Service\":\""+service+"\"},\"PUBLIC\":{\"RETMSG\":\"\",\"RETCODE\":\"0\"}}";
				}
			}catch(Exception e){
				outPut = "{\"PRIVATE\":{\"Service\":\""+service+"\"},\"PUBLIC\":{\"RETMSG\":\"system error!\",\"RETCODE\":\"1\"}}";
				logger.error("Invoking method TGTValidateController.validateTGT :  system error!" , e);
			}
		}
		
		 try {
			outPutAjax(outPut, response);
		} catch (Exception e) {
			 logger.error("Invoking method TGTValidateController : OutPut data failed!");
		}
	    
	}
	
	
	private void outPutAjax(String jsonStr, HttpServletResponse response) throws Exception{
		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
        PrintWriter  out = null;
        try {
        	out= response.getWriter();
            out.write(jsonStr);
        } catch(Exception e){
           logger.error("output jsonStr exception:",e);	
           throw e;
        } finally {
        	if(out!=null){
            out.flush();
            out.close();
        	}
        }
		
	}
	
}
