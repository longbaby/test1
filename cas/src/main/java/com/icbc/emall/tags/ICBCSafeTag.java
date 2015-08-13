package com.icbc.emall.tags;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.Enumeration;

public class ICBCSafeTag extends TagSupport{
	//注：本代码移植自个人网银
	private static final Logger logger = LoggerFactory.getLogger(ICBCSafeTag.class);
	//object控件ID 必输
		protected String id = "";
		//判断是支付密码还是验证码 paypass or verifycode or submit必输
		protected String type = "";
		//提交时输入域名称，如Paypass或者verifycode
		protected String name = "";
		//控件classid
		protected String classid = "";
		//控件codebase
		protected String codebasePath = ""; 
		//控件version(支付密码或验证码和提交的不一样)   
		protected String version = "";
		//用户自定义js
		protected String custom = "";
		//控件的宽度
		protected String objectWidth = "";
		//控件高度
		protected String objectHeight = "";
		//输入场最小长度
		protected String minLength = "";
		//输入场最大长度
		protected String maxLength = "";
		//判断规则
		protected String rule = "";
		//输入框类型
		protected String EditType = "";
		//是否是密码
		protected String isPassword = "";
		//唯一编号
		protected String UniqueIDTMP = "";
		//Fova控件codebase
		protected String codebasePathB2C = ""; 
		
		protected String tabindex="";
		
		public void setCodebasePathB2C(String codebasePathB2C) {
			this.codebasePathB2C = codebasePathB2C;
		}
		
		protected StringBuffer buffer;	 

		
		protected String inputclassid = "";
		protected String inputcodebase = "";
		protected String submitclassid = "";
		protected String submitcodebase = "";   
		
		
		
		protected final static String OBJECT_OPEN  = "<object id=\"";
		protected final static String OBJECT_CLOSE  = "</object>";	
		protected final static String EMBED_OPEN  = "<embed name=\"";
		protected final static String EMBED_CLOSE  = "</embed>";	
		protected final static String PARAM_OPEN  = "<param name=\"";
		protected final static String KEYNAME  = "keyName=\"";
		protected final static String QUOT  = "\"";
		protected final static String LO_RE = ">\n";
		protected static final String SP = " ";
		protected final static String VALUE  = "value=\"";
		protected static final String OBJECTWIDTH = "width=";
		protected static final String OBJECTHEIGHT = "height=";
		protected static final String CODEBASE = "codebase="; 
		protected static final String CLASSID = "classid="; 
		protected static final String TABINDEX = "tabindex=";
		

		public int doStartTag() throws JspException{	
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			String useragent = request.getHeader("User-agent").toLowerCase();
			String language = request.getHeader("Accept-Language");
			JspWriter output = pageContext.getOut();
			String IEVersionFlag = "";	
			String vLenMax="";
			String vLenMin="";
			String verifyHeight = "";

			String defualtRule = "";   
			String defualtMaxlength = "";   
			String defualtMinlength = "";   
			String defualtHeight = "";   
			String defualtWidth = "";  
			String defualtUniqueID = ""; 
			String defualtIsPassword = "";  
			//1202安卓需求变更 增加驱动型控件试用 start
			String EbankpControlFlag = "";	
			String defualtEditType = "";
			String KeyName = "";
			String firstlogin = "0";//firefox在登录首页新增div屏蔽火狐的控件提示
			EbankpControlFlag = "0";			

			try{
				//从相关配置文件中获取密码最大最小长度，验证码的高度
				vLenMax = "8";
				vLenMin = "8";
				verifyHeight = "2";
			}catch(Exception e){}
			String vLen= "4";

			if(type.equalsIgnoreCase("paypass")){
				defualtRule = "10111";   
				defualtMaxlength = "30";   
				defualtMinlength = "4";
				defualtHeight = "21";   
				defualtWidth = "145";    
				defualtIsPassword = "true";  
				defualtEditType = "2";
			}else if(type.equalsIgnoreCase("verifycode")){//验证码控件 
				defualtRule = "10111";   
				defualtMaxlength = vLen;
				defualtMinlength = vLen;
				defualtHeight = verifyHeight;   
				defualtWidth = vLen+"0";   
				defualtIsPassword = "false";  
				defualtEditType = "3";
			}else if(type.equalsIgnoreCase("dynpass")){//动态密码控件
				defualtRule = "10111";   
				defualtMaxlength = vLenMax;   
				defualtMinlength = vLenMin;
				defualtHeight = "21";   
				defualtWidth = String.valueOf(Integer.parseInt(vLenMax)*10);   
				defualtIsPassword = "true";  
				defualtEditType = "2";
			}else if(type.equalsIgnoreCase("acctNum")){//账号加密
				defualtRule = "00002";   
				defualtMaxlength = "19";   
				defualtMinlength = "17";
				defualtHeight = "20";   
				defualtWidth = "160";   
				defualtIsPassword = "false";  
				defualtEditType = "1";			
			}
			//defualtUniqueID = utb.getSessionId(); 
			defualtUniqueID = request.getRequestedSessionId();
			if(!objectWidth.trim().equals(""))defualtWidth=objectWidth;
			if(!objectHeight.trim().equals(""))defualtHeight=objectHeight;
			if(!minLength.trim().equals(""))defualtMinlength=minLength;
			if(!maxLength.trim().equals(""))defualtMaxlength=maxLength;
			if(!UniqueIDTMP.trim().equals(""))defualtUniqueID=UniqueIDTMP;
			if(!isPassword.trim().equals(""))defualtIsPassword=isPassword;
			if(!rule.trim().equals(""))defualtRule=rule;
			if(!EditType.trim().equals(""))defualtRule=EditType;
			try{
				//trident和rv标识用于兼容IE11
			    if (useragent.contains("msie") || (useragent.contains("trident") && useragent.contains("rv"))){
			    	if (useragent.contains("x64")){
			    		inputclassid = "CLSID:2C0ABAB2-9C90-407c-AB4D-7130C547AA7B";
					    inputcodebase = "/AxSafeControls_64.cab#version=1,0,0,22";
					    submitclassid = "CLSID:76E720F1-87EA-4813-B227-284229EE04EF";
					    submitcodebase = "/AxSafeControls_64.cab#version=1,0,0,10";
			    	}else{
				    	inputclassid = "CLSID:73E4740C-08EB-4133-896B-8D0A7C9EE3CD";
					    inputcodebase = "/AxSafeControls.cab#version=1,0,0,22";
					    submitclassid = "CLSID:8D9E0B29-563C-4226-86C1-5FF2AE77E1D2";
					    submitcodebase = "/AxSafeControls.cab#version=1,0,0,10";
			    	}
			    }
			    
		    }catch(Exception e){
				logger.error("ICBCSafePass tag 取参数错误",e);
		    }
		    
			try{
				if(useragent.contains("applewebkit") && useragent.contains("chrome/")){
					String Language="zh_cn";
					String mac_safecontrols_unInstrollTip= "您尚未安装安全控件，请点击下载安装！";
					String mac_safecontrols_lowerVersionTip="您的安全控件版本过低，请下载更新控件！";

					buffer = new StringBuffer("");  
					String pebank_chrome_plugin_url = "/ICBCChromeExtension.msi";
					if(codebasePathB2C!=null && !"".equals(codebasePathB2C)){
						pebank_chrome_plugin_url = codebasePathB2C.concat(pebank_chrome_plugin_url);
					}
					String pebank_chrome_plugin_input_version = "1.0.0.1";
					String pebank_chrome_plugin_submit_version = "1.0.0.1";
					String pebank_chrome_plugin_fullScreen_version = "1.0.0.1";
					String pebank_chrome_plugin_Cache_version = "1.0.0.2";
					String pebank_chrome_plugin_clientBinding_version = "1.0.0.0";
				
					//chrome域名
					//Brower brower =  ICBCINBSConstants.getBrowerConfigValue("IE-CHROME");
					//boolean isActivexDownload=brower.isActivexDownload();
					boolean isActivexDownload=true;
					String divid = "";
						try {
							if(type.equalsIgnoreCase("verifycode")){
								defualtWidth = String.valueOf(Integer.parseInt(defualtWidth)+4);
								divid = "KeyPartdiv";
							}else{
							defualtWidth = String.valueOf(Integer.parseInt(defualtWidth));//登陆页面密码框与卡号框对齐
								divid = "safeEdit1div";
							}
						} catch (Exception e) {
						}

						if(type.equalsIgnoreCase("submit")){//提交控件
							if("1".equals(firstlogin)){
							buffer.append("<div id='safeSubmit1div'>");
							}
							//阻止未安装控件时，弹出chrome的提示框
							buffer.append("<script>\n");
							buffer.append("if(!pulginHasInstalled()){\n");
							buffer.append("document.write(\"<aaa name='\");");						
							buffer.append("}else{\n");
							buffer.append("document.write(\"<object name='\");");						
							buffer.append("}\n");		
							buffer.append("</script>\n");
							//buffer.append(EMBED_OPEN);
							buffer.append(name).append("'").append(SP);
							buffer.append("style=\"margin-left:2px;\"");
							buffer.append("type=\"");
							buffer.append("application/x-icbc-plugin-chrome-npsubmit").append(QUOT).append(SP);
							buffer.append("id=\"");
							buffer.append(id).append(QUOT).append(SP);
							buffer.append(OBJECTWIDTH).append("0").append(SP); 
							buffer.append(OBJECTHEIGHT).append("0").append(SP); 
							buffer.append(custom).append(LO_RE); 
							buffer.append(OBJECT_CLOSE);
							if("1".equals(firstlogin)){
							buffer.append("</div>");
							}
						}else{//密码控件 
							if("1".equals(firstlogin)){
							buffer.append("<div id='" + divid + "' style='display:inline;float:left;margin-right:6px;'>");
							}
							buffer.append(OBJECT_OPEN);
							buffer.append(id).append(QUOT).append(SP);
							buffer.append("type=\"");
							buffer.append("application/x-icbc-plugin-chrome-npxxin-input").append(QUOT).append(SP);
							buffer.append(OBJECTWIDTH).append(defualtWidth).append(SP); 
							buffer.append(OBJECTHEIGHT).append(defualtHeight).append(SP);
							if(!tabindex.equals("")){
								buffer.append(TABINDEX).append(QUOT).append(tabindex).append(QUOT).append(SP);//tabindex
							}
							buffer.append("onmousedown=\"this.focus();\"").append(SP);
							buffer.append(custom).append(LO_RE);
							buffer.append(PARAM_OPEN).append("keyname").append(QUOT).append(SP).append(VALUE).append(name).append(QUOT).append(LO_RE);
							buffer.append(PARAM_OPEN).append("minLength").append(QUOT).append(SP).append(VALUE).append(defualtMinlength).append(QUOT).append(LO_RE);
							buffer.append(PARAM_OPEN).append("maxLength").append(QUOT).append(SP).append(VALUE).append(defualtMaxlength).append(QUOT).append(LO_RE);
							buffer.append(PARAM_OPEN).append("rule").append(QUOT).append(SP).append(VALUE).append(defualtRule).append(QUOT).append(LO_RE);
							buffer.append(PARAM_OPEN).append("UniqueID").append(QUOT).append(SP).append(VALUE).append(defualtUniqueID).append(QUOT).append(LO_RE);
							buffer.append(PARAM_OPEN).append("isPassword").append(QUOT).append(SP).append(VALUE).append("true".equals(defualtIsPassword)?"1":"0").append(QUOT).append(LO_RE);						
							buffer.append(custom).append(LO_RE);
							//阻止未安装控件时，弹出chrome的提示框
							buffer.append("<script>\n");
							buffer.append("if(!pulginHasInstalled()){\n");
							buffer.append("document.write(\"&nbsp;\");");						
							buffer.append("}\n");					
							buffer.append("</script>\n");
							buffer.append(OBJECT_CLOSE);
							if("1".equals(firstlogin)){
							buffer.append("</div>");
							}
							if(isActivexDownload){
								buffer.append("<script>\n");
								buffer.append("var judgeVersion").append(id).append("=function(n){\nvar mac_safecontrol_version=\"\";\n");					
								buffer.append("pluginInputVersion ='").append(pebank_chrome_plugin_input_version).append("'; \n");
								buffer.append("pluginSubmitVersion ='").append(pebank_chrome_plugin_submit_version).append("'; \n");
								buffer.append("pluginFullScreenVersion='").append(pebank_chrome_plugin_fullScreen_version).append("'; \n");
								buffer.append("pluginClientBindingVersion='").append(pebank_chrome_plugin_clientBinding_version).append("'; \n");
								buffer.append("pluginCLCacheVersion='").append(pebank_chrome_plugin_Cache_version).append("'; \n");
								buffer.append("if(!checkVersionIsOk()){\n");
								buffer.append("if(document.getElementById('plagin_download_tip')==null){\n");
								buffer.append("if(n==0){\n");
								buffer.append("document.write(\"<a id='plagin_download_tip' style=\'color:blue;\' href='").append(pebank_chrome_plugin_url).append("'><div style=\'width:100%;white-space:normal;\'>").append(mac_safecontrols_lowerVersionTip).append("</div></a>\");\n");
								buffer.append("}else{\n");
								buffer.append("document.getElementById('plagin_download_tip_span').insertAdjacentHTML(\"afterBegin\",\"<a id='plagin_download_tip'  style=\'color:blue;\' href='").append(pebank_chrome_plugin_url).append("'><div style=\'width:100%;white-space:normal;\'>").append(mac_safecontrols_lowerVersionTip).append("</div></a>\");\n");
								buffer.append("}\n");
								buffer.append("}\n");
								buffer.append("var embeds=document.body.getElementsByTagName(\"object\");\nfor(var i=0;i<embeds.length;i++){embeds[i].style.display=\"none\";}\n");
								buffer.append("}\n");
								buffer.append("}\n"); 
								buffer.append("if(!pulginHasInstalled()){\n");
								buffer.append("if(document.getElementById('plagin_download_tip')==null){\n");
								buffer.append("document.write(\"<a id='plagin_download_tip'  style=\'color:blue;\' href='").append(pebank_chrome_plugin_url).append("'><div style=\'width:100%;white-space:normal;\'>").append(mac_safecontrols_unInstrollTip).append("</div></a>\");\n");
								buffer.append("}\n");
								buffer.append("document.all.").append(id).append(".style.display=\"none\";\n");
								buffer.append("}else{\n");
								
								buffer.append("if(judgeVersion").append(id).append("(0)==false){\n");
								buffer.append("if(document.getElementById('plagin_download_tip_span')==null){\n");
								buffer.append("document.write(\"<span id='plagin_download_tip_span'></span>\");\n");
								buffer.append("}\n");
								buffer.append("window.onfocus=function(){setTimeout(\"judgeVersion").append(id).append("(1)\",200);}\n");
								buffer.append("\n");
								buffer.append("}\n");
								buffer.append("}\n");
								buffer.append("</script>"); 
							}
						}
					//}
				}else if(useragent.contains("msie") || (useragent.contains("trident") && useragent.contains("rv"))){
					buffer = new StringBuffer(OBJECT_OPEN);
					buffer.append(id).append(QUOT).append(SP);
					if(codebasePathB2C!=null && !"".equals(codebasePathB2C)){
						buffer.append(CODEBASE).append(codebasePathB2C);
					}else{
						buffer.append(CODEBASE);
					}
					
					if("1".equals(EbankpControlFlag)){
						KeyName = "KeyName";					
					}else{
						KeyName = "name";
					}				
					
					if(type.equalsIgnoreCase("submit")){//提交控件
						buffer.append(QUOT).append(submitcodebase).append(QUOT).append(SP);
						buffer.append(CLASSID).append(QUOT).append(submitclassid).append(QUOT).append(SP);
						buffer.append("height=").append("0").append(SP).append("width=").append("0");  
						buffer.append(custom).append(LO_RE); 
						buffer.append(OBJECT_CLOSE);		
					}else{//密码控件 
						buffer.append(QUOT).append(inputcodebase).append(QUOT).append(SP);//version
						buffer.append(CLASSID).append(QUOT).append(inputclassid).append(QUOT).append(SP);//classid
						buffer.append(OBJECTWIDTH).append(defualtWidth).append(SP); 
						buffer.append(OBJECTHEIGHT).append(defualtHeight).append(SP);
						if(!tabindex.equals("")){
							buffer.append(TABINDEX).append(QUOT).append(tabindex).append(QUOT).append(SP);//tabindex
						}
						buffer.append(custom).append(LO_RE); 
						buffer.append(PARAM_OPEN).append(KeyName).append(QUOT).append(SP).append(VALUE).append(name).append(QUOT).append(LO_RE);
						buffer.append(PARAM_OPEN).append("minLength").append(QUOT).append(SP).append(VALUE).append(defualtMinlength).append(QUOT).append(LO_RE);
						buffer.append(PARAM_OPEN).append("maxLength").append(QUOT).append(SP).append(VALUE).append(defualtMaxlength).append(QUOT).append(LO_RE);
					if("1".equals(EbankpControlFlag)){
						buffer.append(PARAM_OPEN).append("EditType").append(QUOT).append(SP).append(VALUE).append(defualtEditType).append(QUOT).append(LO_RE);
					}					
						buffer.append(PARAM_OPEN).append("rule").append(QUOT).append(SP).append(VALUE).append(defualtRule).append(QUOT).append(LO_RE);
						buffer.append(PARAM_OPEN).append("UniqueID").append(QUOT).append(SP).append(VALUE).append(defualtUniqueID).append(QUOT).append(LO_RE);
						buffer.append(PARAM_OPEN).append("IsPassword").append(QUOT).append(SP).append(VALUE).append(defualtIsPassword).append(QUOT).append(LO_RE);
						buffer.append(OBJECT_CLOSE);
					}
				}else if(useragent.contains("safari/")){
					String mac_safecontrols_unInstrollTip= "您尚未安装安全控件，请点击下载安装完成后重启浏览器！";
					String mac_safecontrols_lowerVersionTip="您的安全控件版本过低，请下载更新后重启浏览器！";

					buffer = new StringBuffer("");  
					String pebank_macos_plugin_url = "/SafeControls.dmg";
					String pebank_macos_plugin_version = "1.0.0.8";
					String divid = "";
					try {
						if(type.equalsIgnoreCase("verifycode")){
							defualtWidth = String.valueOf(Integer.parseInt(defualtWidth)+4);
							divid = "KeyPartdiv";
						}else{
						defualtWidth = String.valueOf(Integer.parseInt(defualtWidth));//登陆页面密码框与卡号框对齐
							divid = "safeEdit1div";
						}
					} catch (Exception e) {
					}
					buffer.append(EMBED_OPEN);
					buffer.append(id).append(QUOT).append(SP);
					buffer.append("style=\"vertical-align: middle;\"");
					buffer.append("type=\"");
					if(type.equalsIgnoreCase("submit")){//提交控件
						buffer.append("application/x-npsubmit-plugin").append(QUOT).append(SP);
						buffer.append(OBJECTWIDTH).append("0").append(SP); 
						buffer.append(OBJECTHEIGHT).append("0").append(SP); 
						buffer.append(custom).append(LO_RE); 
						buffer.append(EMBED_CLOSE);
						buffer.append("<input type=\"hidden\" name=\"SafePluginType\" value=\"1\" />");
					}else{//密码控件 
						buffer.append("application/x-npinput-plugin").append(QUOT).append(SP);
						buffer.append(OBJECTWIDTH).append(defualtWidth).append(SP); 
						buffer.append(OBJECTHEIGHT).append(defualtHeight).append(SP);
						buffer.append("keyName=\"").append(name).append(QUOT).append(SP);
						buffer.append("minLength=\"").append("1").append(QUOT).append(SP);
						buffer.append("maxLength=\"").append(defualtMaxlength).append(QUOT).append(SP);
						buffer.append("rule=\"").append(defualtRule).append(QUOT).append(SP);
						buffer.append("UniqueID=\"").append(defualtUniqueID).append(QUOT).append(SP);
						buffer.append("isPassword=\"").append(defualtIsPassword).append(QUOT).append(SP);
						if(!tabindex.equals("")){
							buffer.append(TABINDEX).append(QUOT).append(tabindex).append(QUOT).append(SP);//tabindex
						}
						buffer.append("onmousedown=\"this.focus();\"").append(SP);
						buffer.append(custom).append(LO_RE);
						buffer.append(EMBED_CLOSE);
	
						buffer.append("<script>\n");
						buffer.append("var judgeVersion").append(id).append("=function(n){\nvar mac_safecontrol_version=\"\";\n");
						
						buffer.append("try{\nmac_safecontrol_version=document.all.").append(id).append(".getVersion()\n}catch(e){return false;}\n");
						buffer.append("if(compareVersionPlugin(mac_safecontrol_version,'").append(pebank_macos_plugin_version).append("')){\n");
						buffer.append("if(document.getElementById('plagin_download_tip')==null){\n");
						buffer.append("if(n==0){\n");
						buffer.append("document.write(\"<a id='plagin_download_tip' style=\'color:blue;\' href='").append(pebank_macos_plugin_url).append("'>").append(mac_safecontrols_lowerVersionTip).append("</a>\");\n");
						buffer.append("}else{\n");
						buffer.append("document.getElementById('plagin_download_tip_span').insertAdjacentHTML(\"afterBegin\",\"<a id='plagin_download_tip' style=\'color:blue;\' href='").append(pebank_macos_plugin_url).append("'>").append(mac_safecontrols_lowerVersionTip).append("</a>\");\n");
						buffer.append("}\n");
						buffer.append("}\n");
						buffer.append("var embeds=document.body.getElementsByTagName(\"embed\");\nfor(var i=0;i<embeds.length;i++){embeds[i].style.display=\"none\";}\n");
						buffer.append("}\n");
						buffer.append("}\n");
						buffer.append("var macos_nsplagin=navigator.mimeTypes['application/x-npsubmit-plugin'];\n");
						buffer.append("if(typeof(macos_nsplagin)==\"undefined\"){\n");
						buffer.append("if(document.getElementById('plagin_download_tip')==null){\n");
						buffer.append("document.write(\"<a id='plagin_download_tip' style=\'color:blue;\' href='").append(pebank_macos_plugin_url).append("'>").append(mac_safecontrols_unInstrollTip).append("</a>\");\n");
						buffer.append("}\n");
						buffer.append("document.all.").append(id).append(".style.display=\"none\";\n");
						buffer.append("}else{\n");
						
						buffer.append("if(judgeVersion").append(id).append("(0)==false){\n");
						buffer.append("if(document.getElementById('plagin_download_tip_span')==null){\n");
						buffer.append("document.write(\"<span id='plagin_download_tip_span'></span>\");\n");
						buffer.append("}\n");
						buffer.append("window.onfocus=function(){setTimeout(\"judgeVersion").append(id).append("(1)\",200);}\n");
						buffer.append("\n");
						buffer.append("}\n");
						buffer.append("}\n");
						buffer.append("</script>"); 
					}
					
				}else{
					buffer = new StringBuffer("");
					buffer.append("该浏览器暂不支持安全控件登录");
				}
				output.println(buffer.toString());
				
			}catch(Exception e){
				logger.error("ICBCSafePass tag 内部错误",e);
			}
			return SKIP_BODY;
		}
		
		public int doEndTag() throws JspException{
			return EVAL_PAGE;
		}
		/**
		 * @return 获得 id。
		 */
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return 获得 type。
		 */
		public void setType(String type) {
			this.type = type;
		}
		/**
		 * @return 获得 name。
		 */
		public void setName(String name) {
			this.name = name;
		}
//		/**
//		 * @return 获得 classid。
//		 */
		public void setClassid(String classid) {
			this.classid = classid;
		}
//		/**
//		 * @return 获得 codebase。
//		 */
		public void setCodebasePath(String codebasePath) {
			this.codebasePath = codebasePath; 
		}
		/**
		 * @return 获得 version。
		 */
		public void setVersion(String version) {
			this.version = version;
		}
		/**
		 * @return 获得 custom。
		 */
		public void setCustom(String custom) {
			this.custom = custom;
		}
		/**
		 * @return 获得 objectHeight。
		 */
		public void setObjectHeight(String objectHeight) {
			this.objectHeight = objectHeight;
		}
		/**
		 * @return 获得 objectWidth。
		 */
		public void setObjectWidth(String objectWidth) {
			this.objectWidth = objectWidth;
		}
		/**
		 * @return 获得 minLength。
		 */
		public void setMinLength(String minLength) {
			this.minLength = minLength;
		}
		/**
		 * @return 获得 maxLength。
		 */
		public void setMaxLength(String maxLength) { 
			this.maxLength = maxLength;
		}
		/**
		 * @return 获得 rule。
		 */
		public void setRule(String rule) {
			this.rule = rule;
		}
		/**
		 * @return 获得EditType。
		 */
		public void setEditType(String EditType) {
			this.EditType = EditType;
		}
		/**
		 * @return 获得 IsPassword。
		 */
		public void setIsPassword(String isPassword) {
			this.isPassword = isPassword;
		}
		/**
		 * @return 获得 UniqueID。
		 */
		public void setUniqueID(String uniqueID) {
			this.UniqueIDTMP = uniqueID;
		}

		public String getTabindex() {
			return tabindex;
		}

		public void setTabindex(String tabindex) {
			this.tabindex = tabindex;
		}	
		
}

