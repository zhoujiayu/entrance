package com.ytsp.entrance.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ImagePropertyUtil {
	
	private static Properties config = null;
	
	private static String PROP_FILE_NAME = "imageHost.properties";
	
	private ImagePropertyUtil(){ }
	
	static{
		if(config == null){
			try {
				initConfig();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void initConfig(){
		config = loadPropertyFile(PROP_FILE_NAME);
	}
	
	public static String getPropertiesValue(String key){
		return config.getProperty(key);
	}
	
	public static void main(String[] args){
		System.out.println(getPropertiesValue( "imagehost" ));
	}
	
	/**
	* <p>功能描述:获取source文件下的property文件</p>
	* <p>参数：@param name
	* <p>参数：@param key
	* <p>参数：@param fileName
	* <p>参数：@return</p>
	* <p>返回类型：Integer</p>
	 */
	private static Properties loadPropertyFile(String fileName) {
		InputStream is = null;
		Properties props = new Properties();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			classLoader = Util.class.getClassLoader();
		}
		is = classLoader.getResourceAsStream(fileName);
		if (is != null) {
			try {
				props.load(is);

			} catch (IOException e) {
				System.out.println("加载配置文件出错");
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					System.out.println("加载配置文件出错");
				}
			}
		}
		return props;
	}
}

