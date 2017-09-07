package com.ytsp.entrance.util;

import org.apache.commons.lang.xwork.StringUtils;

public class VerifyClientCustomer {
	
	public static boolean accountValidateV5_0(String account) {
		if (!(RegexUtils.matchStandarChar(account))) {
			return false;
		}

		int count = 0;
		char[] ch = account.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				count += 2;
			} else {
				count++;
			}
		}
		
		if(count < 6 || count > 20){
			return false;
		}
		
		return true;
	}
	
	public static boolean accountValidate(String account) {
		if (!(RegexUtils.matchStandarChar(account))) {
			return false;
		}

		int count = 0;
		char[] ch = account.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				count += 2;
			} else {
				count++;
			}
		}
		
		if(count < 4 || count > 30){
			return false;
		}
		
		return true;
	}
	
	/**
	* <p>功能描述:校验手机格式是否正确</p>
	* <p>参数：@param phone 11位手机号
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	public static boolean validateCellphone(String phone){
		return RegexUtils.matchMobilephone(phone);
	}
	
	public static boolean passwordValidate(String password){
		return RegexUtils.matchPassword(password);
	}
	
	public static boolean emailValidate(String email){
		if(!StringUtils.isEmpty(email)){
			return RegexUtils.matchEmail(email);
		}else{
			return true;
		}
	}
	
	//  GENERAL_PUNCTUATION 判断中文的“号  
    //  CJK_SYMBOLS_AND_PUNCTUATION 判断中文的。号  
    //  HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，号  
	public static boolean isChinese(char c) {  
  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
        }  
  
        return false;  
    }
}
