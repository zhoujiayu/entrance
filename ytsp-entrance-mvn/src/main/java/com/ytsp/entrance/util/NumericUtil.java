package com.ytsp.entrance.util;


public class NumericUtil {
	public static int parseInt(Integer i){
		if(i==null)
			return 0;
		return i.intValue();
	}
	public static int parseInt(Integer i,int dft){
		if(i==null)
			return dft;
		return i.intValue();
	}
	public static double parseDouble(Double i){
		if(i==null)
			return 0;
		return i.intValue();
	}
	public static float parseFloat(Float i){
		if(i==null)
			return 0;
		return i.intValue();
	}
	public static long parseLong(Long i){
		if(i==null)
			return 0;
		return i.intValue();
	}
}
