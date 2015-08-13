package com.icbc.emall.cas.util;

import java.util.List;

public class PasswordHelper {
	
	/*public static void main(String[] args) throws Exception {
		List<String> dict_list=PasswordDictionaryUtil.generateDict();
		System.out.println(dict_list);
		String key=String.valueOf(dict_list.hashCode());
		
		String result1=encrypt("+?/*", dict_list);
		System.out.println(decrypt(result1, dict_list));
		String reuslt2=encrypt(".,\"'~", dict_list);
		System.out.println(decrypt(reuslt2, dict_list));
		String result3=encrypt("!@#$%", dict_list);
		System.out.println(decrypt(result3, dict_list));
		String result4=encrypt("^&()_", dict_list);
		System.out.println(decrypt(result4, dict_list));
		String result5=encrypt("-=<>:", dict_list);
		System.out.println(decrypt(result5, dict_list));
		String result6=encrypt(";[]{}", dict_list);
		System.out.println(decrypt(result6, dict_list));
		String result7=encrypt("|\\ ", dict_list);
		System.out.println(decrypt(result7, dict_list));
	}
	*/
	/**
	 * 公开加密接口
	 * @param password
	 * @param dict_list
	 * @return
	 * @throws Exception 
	 */
	public static String encrypt(String password,List<String> dict_list) throws Exception{

		StringBuilder sb=new StringBuilder();
		for(int i=0;i<password.length();i++){
			String c=password.substring(i,i+1);
			for(int j=0;j<dict_list.size()/2;j++){
				if(c.equals(dict_list.get(j*2))){
					sb.append(dict_list.get(j*2+1));
					break;
				}
				if(j==dict_list.size()/2-1){
					throw new Exception("非法字符！");
				}
			}
		}
		
		return sb.toString();
	}

	/**
	 * 公开解密接口
	 * @param ciphertext 密文
	 * @param dict_list 字典
	 * @return
	 * @throws Exception 
	 */
	public static String decrypt(String ciphertext,List<String> dict_list) throws Exception{
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<ciphertext.length();i++){
			String c=ciphertext.substring(i,i+1);
			for(int j=0;j<dict_list.size()/2;j++){
				if(c.equals(dict_list.get(j*2+1))){
					sb.append(dict_list.get(j*2));
					break;
				}else if(j==dict_list.size()/2-1){
					throw new Exception("非法字符");
				}
			}
		}
		
		return sb.toString();
	}
	
}
