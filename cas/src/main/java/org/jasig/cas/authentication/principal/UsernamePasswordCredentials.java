package org.jasig.cas.authentication.principal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.type.YesNoType;
import org.jasig.cas.authentication.principal.Credentials;

import com.icbc.emall.common.utils.Globe.YesOrNo;

public class UsernamePasswordCredentials implements Credentials {

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -8343864967200862794L;

    /** The username. */
    @NotNull
    @Size(min=1,message = "required.username")
    private String username;

    private String password;

    private String cis;
    
    //是否检验密码 0-否 1-是
    private String isCheckPwd;
    
    /**
     * 登录方式
     * 其中1是代表手机端登录
     */
	private String loginWay;
	/**
	 * mobile
	 * 变化规则
	 */
	private String changerule;
	
	/**
	 * mobile
	 * 加密规则
	 */
	private String rule;
	/**
	 * mobile
	 * 是否启用密码键盘
	 */
	private String safeKeyBoard;
	
	/**
	 * mobile
	 * 手机端加密后密文
	 */
	private String passwordPre;
	
	/**
	 * mobile
	 * 是否记住用户名
	 */
	private String isHoldUser;
	
    public String getIsHoldUser() {
		return isHoldUser;
	}

	public void setIsHoldUser(String isHoldUser) {
		this.isHoldUser = isHoldUser;
	}

	public String getPasswordPre() {
		return passwordPre;
	}

	public void setPasswordPre(String passwordPre) {
		this.passwordPre = passwordPre;
	}

	public String getSafeKeyBoard() {
		return safeKeyBoard;
	}

	public void setSafeKeyBoard(String safeKeyBoard) {
		this.safeKeyBoard = safeKeyBoard;
	}

	public String getChangerule() {
		return changerule;
	}

	public void setChangerule(String changerule) {
		this.changerule = changerule;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getCis() {
		return cis;
	}

	public void setCis(String cis) {
		this.cis = cis;
	}

	public String getIsCheckPwd() {
		return isCheckPwd;
	}

	public void setIsCheckPwd(String isCheckPwd) {
		this.isCheckPwd = isCheckPwd;
	}

	/**
     * @return Returns the password.
     */
    public final String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return Returns the userName.
     */
    public final String getUsername() {
        return this.username;
    }

    /**
     * @param userName The userName to set.
     */
    public final void setUsername(final String userName) {
        this.username = userName;
    }

    public String toString() {
        return "[username: " + this.username + "]";
    }
    public UsernamePasswordCredentials(){}
    
    public UsernamePasswordCredentials(String userName,String password){
    	this.username = userName;
    	this.password = password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsernamePasswordCredentials that = (UsernamePasswordCredentials) o;
        if(that.isCheckPwd!=null && this.isCheckPwd.equals(YesOrNo.NO)){
        	return true;
        }else{
        	if (password != null ? !password.equals(that.password) : that.password != null) return false;
            if (username != null ? !username.equals(that.username) : that.username != null) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
	public String getLoginWay() {
		return loginWay;
	}

	public void setLoginWay(String loginWay) {
		this.loginWay = loginWay;
	}
}
