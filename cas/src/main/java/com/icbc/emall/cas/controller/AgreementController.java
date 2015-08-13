package com.icbc.emall.cas.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.icbc.emall.agreement.service.AgreementService;

@Controller
public class AgreementController  extends AbstractController {

	private AgreementService mallAgreementService;
	
	public AgreementService getMallAgreementService() {
		return mallAgreementService;
	}

	public void setMallAgreementService(AgreementService mallAgreementService) {
		this.mallAgreementService = mallAgreementService;
	}

	public void finadAgreement(@RequestParam("id")String id,HttpServletResponse res){
		try {
			res.getWriter().print("{\"del\":\true\"}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		// TODO Auto-generated method stub
		ModelAndView test = new ModelAndView("jsonView");
		Map <String,String> map = new HashMap<String,String>();
		String str = null;
		try{
			str = mallAgreementService.getMallRegAgreement();
			if(str== null || "".equals(str.trim() )){
				str="";
			}
		}catch(Exception e){
			str="query Agreement content failed";
		}
		map.put("str",str);
		test.addAllObjects(map);
		return test;
	}

}
