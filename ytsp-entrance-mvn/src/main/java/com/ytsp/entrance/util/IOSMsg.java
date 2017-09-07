package com.ytsp.entrance.util;

import java.util.Properties;

import javapns.Push;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;


public class IOSMsg {

	private static Properties emojis ;
	
	private static void pushMessage(String content,String str,String token,String plat,boolean product) throws Exception
    {
		token = token.replaceAll(" ", "");
    	try {
    		PushNotificationPayload payload = new PushNotificationPayload();
    		payload.addCustomAlertActionLocKey("进入爱看");
//    		payload.addCustomAlertLocKey("msg");
    		payload.addCustomAlertLocKey(content);
//    		payload.addAlert(content);
//    		payload.addBadge(1);
//    		payload.addSound("default");
	    	payload.addCustomDictionary("id",10);
    		payload.addCustomDictionary("redirect", "ikan://product/1001022");
    		PushedNotifications rets = Push.payload( payload,str, "Kandongman", product, token);
    		System.err.println(rets);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	static void pushSandbox() throws Exception{
		String token = null;//"5b9d1671 52332e86 ab442dcf 86f7c7b8 6ca4cbd8 cafbe4d6 d7641c21 a0725cc4";
		//c1ab9cf1 14052c73 e72d2a02 efac3f3c 527ea8e8 45a4289c 0993b906 ef8c7fe9
    	token = "9ca99edb 66b939cd d54c75c3 7fdea0cf 865b3249 97e99e0a a54a40a9 5d0596a1";
    	
     	final String strIphone  = IOSMsg.class.getResource("/Push_iphone_dev.p12").getFile();
		final String strIpad  = IOSMsg.class.getResource("/aps_dev_credentials.p12").getFile();
//		byte[] bs = string2bytes(emojis.getProperty("U+1F308"));
		String msg = new String("【暑期特惠】陀螺、巴啦啦、铠甲勇士玩具全网最低价，买就包邮哦~"); 
//		msg +="\u26BD";
//		msg +="\u26C4";
		pushMessage(msg,strIpad,token,"iphone",false);
	}

	
	static void pushSingleone() throws Exception{
		String token = null;//"5b9d1671 52332e86 ab442dcf 86f7c7b8 6ca4cbd8 cafbe4d6 d7641c21 a0725cc4";
    	token = "9ca99edb 66b939cd d54c75c3 7fdea0cf 865b3249 97e99e0a a54a40a9 5d0596a1";
//    	token = "5b9d1671 52332e86 ab442dcf 86f7c7b8 6ca4cbd8 cafbe4d6 d7641c21 a0725cc4";
    	final String strIphone  = IOSMsg.class.getResource("/Push_iphone_dis.p12").getFile();
		final String strIpad  = IOSMsg.class.getResource("/aps_dis_credentials.p12").getFile();
//		byte[] bs = string2bytes(emojis.getProperty("U+1F308"));
		String msg = new String("【乐高城市系列】乐高动画已在爱看播出，快来帮助市民们一起来解决城市中的各种问题吧~~"); 
//		msg +="\u26BD";
//		msg +="\u26C4";
		pushMessage(msg,strIpad,token,"iphone",true);
	}
	
	static byte[] string2bytes(String str){
		String[] bytestr = str.split(" ");
		byte[] ret = new byte[bytestr.length];
		for (int i = 0; i < bytestr.length; i++) {
			ret[i] = (byte)Integer.valueOf(bytestr[i].substring(2), 16).intValue();
		}
		return ret ;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		String msg = new String( "\u1f301".getBytes("iso8859-1"),"utf-8");
//		msg = "\u1f301";
//		byte[] bs = msg.getBytes();
//		for (int i = 0; i < bs.length; i++) {
//			System.err.println(Integer.toHexString(bs[i]));
//		}
//		Thread.sleep(1000*60*60*5);
		emojis = new Properties();
		emojis.load(IOSMsg.class.getResourceAsStream("emoji.properties"));
//		pushProduct();
//		pushSandbox();
		pushSingleone();
	}
	
}
