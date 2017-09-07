package com.ytsp.entrance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javapns.Push;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;
import javapns.notification.transmission.NotificationThread;
import javapns.notification.transmission.NotificationThreads;
import javapns.notification.transmission.PushQueue;

import com.ytsp.db.enums.MessageTypeEnum;


public class NewIOSMsg {

	private static Properties emojis ;
	
	private static void pushMessage(String content,String str,String token,String plat,boolean product) throws Exception
    {
		token = token.replaceAll(" ", "");
    	try {
    		PushNotificationPayload payload = new PushNotificationPayload();
    		payload.addCustomAlertActionLocKey("打开看看");
//    		payload.addCustomAlertLocKey("msg");
    		payload.addCustomAlertLocKey(content);
//    		payload.addAlert(content);
//    		payload.addBadge(1);
//    		payload.addSound("default");
//    		payload.addCustomDictionary("msgType", "album");
//    		payload.addCustomDictionary("msgType", "webview");
//    		payload.addCustomDictionary("albumId", "296");
//    		payload.addCustomDictionary("addr", "http://ikan.cn/redirect.jsp");
//    		payload.addCustomDictionary("albumType", 0);
    		PushedNotifications rets = Push.payload( payload,str, "Kandongman", product, token);
    		System.err.println(rets);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	static void pushSingleone(String token) throws Exception{
    	token = token.replaceAll(" ", "");
    	final String strIphone  = NewIOSMsg.class.getResource("/Push_iphone_dis.p12").getFile();
		final String strIpad  = NewIOSMsg.class.getResource("/Push_ipad_dis.p12").getFile();
		String content = "上爱看，免费得限量版变形金刚！";
	       PushQueue queuephone = Push.queue(strIphone, "Kandongman", true, 2); 
	        /* Start the queue (所有的线程和连接将被初始化) */  
//	       PushQueue queuepad = Push.queue(strIpad, "Kandongman", true, 1); 
	        /* Start the queue (所有的线程和连接将被初始化) */  
	    PushNotificationPayload payload = new PushNotificationPayload();
 		payload.addCustomAlertActionLocKey("打开看看");
 		payload.addCustomAlertLocKey(content);
 		queuephone.start();
 		queuephone.add(payload,token);
	}
	static void pushSandbox() throws Exception{
		String token = null;//"5b9d1671 52332e86 ab442dcf 86f7c7b8 6ca4cbd8 cafbe4d6 d7641c21 a0725cc4";
    	token = "0267794b d15fabc3 d2c0b7da 984ba486 8a86226d 8b25a3cf 7f545ab0 3e46341b";
//    	token = "5b9d1671 52332e86 ab442dcf 86f7c7b8 6ca4cbd8 cafbe4d6 d7641c21 a0725cc4";
    	final String strIphone  = NewIOSMsg.class.getResource("/Push_iphone_dev.p12").getFile();
		final String strIpad  = NewIOSMsg.class.getResource("/Push_ipad_dev.p12").getFile();
//		byte[] bs = string2bytes(emojis.getProperty("U+1F308"));
		String msg = new String("上爱看，乐高、托马斯等名品玩具全场一折起！");
//		msg +="\u26BD";
//		msg +="\u26C4";
		pushMessage(msg,strIphone,token,"iphone",false);
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
//		Thread.sleep((long) (1000*60*20));
//		emojis = new Properties();
//		emojis.load(IOSMsg.class.getResourceAsStream("emoji.properties"));
		ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					pushProduct();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000*60*60*4, 1000*60*60*24, TimeUnit.MILLISECONDS);
//		pushSandbox();
//		pushSingleone("4b38ff6e 85af6995 348decce 81a0ba51 e55cfa53 b3b8cdf2 ae1effd8 173d9549");
	}
	static void pushProduct() throws Exception{
		//yy的手机token
    	String token = "";
    	String plat = "ipad";
    	Statement  st=null; //Statement对象（SQL语句）
    	Connection con = null;
	    ResultSet   rs=null; //结果集对象
		Class.forName("com.mysql.jdbc.Driver");
		 con=DriverManager.getConnection ("jdbc:mysql://114.112.50.209/ytsp?" +
			 		"useUnicode=true&characterEncoding=utf-8","imagemedia","Kandongman");
		final String strIphone  = NewIOSMsg.class.getResource("/Push_iphone_dis.p12").getFile();
		final String strIpad  = NewIOSMsg.class.getResource("/Push_ipad_dis.p12").getFile();
		
    	try{
			   st=con.createStatement();
			   rs = st.executeQuery("SELECT r.terminal_type ,r.device_token " +
			   		"FROM ytsp_hardware_reg r where LENGTH(r.device_token)>0 ");
			   /* 建立队列 */  
			   String content = "上爱看，免费得限量版变形金刚！";
			   Calendar cal = Calendar.getInstance();
			   int day = cal.get(Calendar.DAY_OF_WEEK);
			   if(day==Calendar.SUNDAY||day==Calendar.SATURDAY)
				   content = "明晚十点，上爱看，一元得乐高、托马斯等名品玩具！";
			   NotificationThreads queuephone = (NotificationThreads) Push.queue(strIphone, "Kandongman", true, 100); 
		       NotificationThreads queuepad = (NotificationThreads) Push.queue(strIpad, "Kandongman", true, 100); 
		       PushNotificationPayload payload = new PushNotificationPayload();
	    		payload.addCustomAlertActionLocKey("打开看看");
	    		payload.addCustomAlertLocKey(content);
	    		MessageTypeEnum.ebproduct.notify();
			   final Map<String, String> map = new HashMap<String, String>();
			   while(rs.next()){
				   plat = rs.getString("terminal_type");
				   token =  rs.getString("device_token");
				   if(token!=null&&!token.trim().equals(""))
					   token = token.replaceAll(" ", "");
				   else
					   continue;
				   map.put(token, plat);
			   }
			   PayloadPerDevice device = null;
			   for (Entry<String, String> entry:map.entrySet()) {
				   try {
						   device = new PayloadPerDevice(payload, entry.getKey());
					} catch (Exception e) {
						System.err.println(token);
						continue;
					}
				   if(entry.getValue().equals("iphone")){
				       queuephone.add(device);
				   }else{
					   queuepad.add(device);
				   }
			   }
			   queuepad.setListener(new NotificationProgressListener("queuepad"));
			   queuephone.setListener(new NotificationProgressListener("queuephone"));
		 }
		 catch(Exception  e)  {
			 e.printStackTrace();
		 }
		finally{
			rs.close();
			st.close();
		}
	}
	
	static class NotificationProgressListener implements javapns.notification.transmission.NotificationProgressListener{
		String name ;
		public NotificationProgressListener(String string) {
			this.name = string;
		}
		public void eventThreadStarted(NotificationThread notificationThread) {
		}
		public void eventThreadFinished(NotificationThread notificationThread) {
		}
		public void eventCriticalException(NotificationThread notificationThread,
				Exception exception) {
		}
		public void eventConnectionRestarted(NotificationThread notificationThread) {
		}
		public void eventAllThreadsStarted(NotificationThreads notificationThreads) {
		}
		public void eventAllThreadsFinished(NotificationThreads notificationThreads) {
			System.out.println(name+" finished");
			PushedNotifications failed = notificationThreads.getFailedNotifications();
			PushedNotifications success = notificationThreads.getSuccessfulNotifications();
			System.out.println(name+" failed:"+failed.capacity());
			System.out.println(name+" success:"+success.capacity());
		}
	}
}
