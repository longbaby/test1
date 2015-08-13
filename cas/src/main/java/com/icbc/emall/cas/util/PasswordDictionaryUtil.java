package com.icbc.emall.cas.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PasswordDictionaryUtil {

	private static final String[] dict_char = new String[]{" ", "!", "\"", "#", "$", "%", "&", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^", "_", "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~", "'"};
	
	/**
	 * 生成字典,偶数索引集与奇数索引集分别是一个无重复完整的乱序字典
	 * @return
	 */
	public static List<String> generateDict(){
		int length=dict_char.length*2;
		
		List<String> resultDict=new ArrayList<String>(length);
		
		List<String> dictOdd=generateDict4Single();
		List<String> dictEven=generateDict4Single();
		while(dictOdd.hashCode()==dictEven.hashCode() && dictOdd.equals(dictEven)){
			dictEven=generateDict4Single();
		}
		
		for(int i=0;i<length;i++){
			if(i%2==0){
				resultDict.add(dictEven.get(i/2));
			}else{
				resultDict.add(dictOdd.get(i/2));
			}
		}

		return resultDict;
	}
	
	
	private static List<String> generateDict4Single(){
		
		int size=dict_char.length;
		List<Integer> index_list=new LinkedList<Integer>();
		for(int i=0;i<size;i++){
			index_list.add(i);
		}
		
		List<String> resultDict=new ArrayList<String>(size);
		
		Random r=new Random();
		
		for(int i=0;i<size;i++){
			int rindex=r.nextInt(index_list.size());
			int dictIndex=index_list.get(rindex);
			index_list.remove(rindex);
			resultDict.add(dict_char[dictIndex]);
		}
		
		return resultDict;
	}
	
/*	public static void main(String[] args) {
		List<String> dictList=PasswordDictionaryUtil.generateDict();
		int hashcode=dictList.hashCode();
		System.out.println(dictList);
		System.out.println(hashcode);
	}
	*/
}
