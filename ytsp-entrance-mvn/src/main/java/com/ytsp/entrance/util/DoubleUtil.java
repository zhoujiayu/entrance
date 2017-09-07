package com.ytsp.entrance.util;

import java.math.BigDecimal;

public class DoubleUtil {
	
	/**
	* <p>功能描述:加法运算</p>
	* <p>参数：@param d1
	* <p>参数：@param d2
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double add(double d1, double d2) {
		//尽量使用参数类型为String的构造函数,因为参数类型为double的构造方法的结果有一定的不可预知性
		BigDecimal b1 = new BigDecimal(String.valueOf(d1));
		BigDecimal b2 = new BigDecimal(String.valueOf(d2));
		return b1.add(b2).doubleValue();
	}

	/**
	* <p>功能描述:进行减法运算</p>
	* <p>参数：@param d1
	* <p>参数：@param d2
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double sub(double d1, double d2) { 
		BigDecimal b1 = new BigDecimal(String.valueOf(d1));
		BigDecimal b2 = new BigDecimal(String.valueOf(d2));
		return b1.subtract(b2).doubleValue();
	}
	
	/**
	* <p>功能描述:进行乘法运算</p>
	* <p>参数：@param d1
	* <p>参数：@param d2
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double mul(double d1, double d2) { 
		BigDecimal b1 = new BigDecimal(String.valueOf(d1));
		BigDecimal b2 = new BigDecimal(String.valueOf(d2));
		return b1.multiply(b2).doubleValue();
	}
	
	/**
	* <p>功能描述:将2个double类型相除保留指定长度</p>
	* <p>参数：@param d1
	* <p>参数：@param d2
	* <p>参数：@param len
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double div(double d1, double d2, int len) {// 进行除法运算
		BigDecimal b1 = new BigDecimal(String.valueOf(d1));
		BigDecimal b2 = new BigDecimal(String.valueOf(d2));
		return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	* <p>功能描述:将2个double类型相除</p>
	* <p>参数：@param d1
	* <p>参数：@param d2
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double div(double d1, double d2) {// 进行除法运算
		BigDecimal b1 = new BigDecimal(String.valueOf(d1));
		BigDecimal b2 = new BigDecimal(String.valueOf(d2));
		return b1.divide(b2).doubleValue();
	}
	
	/**
	* <p>功能描述:将2个double类型相除，保留指小位小数</p>
	* <p>参数：@param d1
	* <p>参数：@param d2
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double divRound(double d1, double d2,int length) {// 进行除法运算
		return round(div(d1, d2),length);
	}
	
	/**
	* <p>功能描述:将double类型进行四舍五入操作</p>
	* <p>参数：@param d
	* <p>参数：@param len
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double round(double d, int len) { 
		BigDecimal b1 = new BigDecimal(String.valueOf(d));
		BigDecimal b2 = new BigDecimal("1");
		// 任何一个数字除以1都是原数字
		// ROUND_HALF_UP是BigDecimal的一个常量，表示进行四舍五入的操作
		return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	* <p>功能描述:将double类型进行四舍五入操作</p>
	* <p>参数：@param d
	* <p>参数：@param length
	* <p>参数：@return</p>
	* <p>返回类型：double</p>
	 */
	public static double roundDouble(double d, int length) {
		BigDecimal bd = new BigDecimal(String.valueOf(d));
		return bd.setScale(length, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
}
