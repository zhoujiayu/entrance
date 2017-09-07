package com.ytsp.entrance.quartz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javapns.Push;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;
import javapns.notification.transmission.NotificationThread;
import javapns.notification.transmission.NotificationThreads;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.EbActivityDao;
import com.ytsp.db.dao.PushMessageDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.EbActivity;
import com.ytsp.db.domain.PushMessage;
import com.ytsp.db.enums.MessageNewTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.system.SystemInitialization;

public class IOSPushMsgService {

	public static boolean skip = true;
	protected static final Logger logger = Logger
			.getLogger(IOSPushMsgService.class);

	private PushMessageDao pushMessageDao;

	public void process() {
		pushProduct();
	}

	public PushMessageDao getPushMessageDao() {
		return pushMessageDao;
	}

	public void setPushMessageDao(PushMessageDao pushMessageDao) {
		this.pushMessageDao = pushMessageDao;
	}

	private AlbumDao albumDao;

	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	@SuppressWarnings("deprecation")
	private void pushProduct() {
		if (skip)
			return;
		PushMessage message = null;
		Date now = new Date();
		try {
			message = pushMessageDao
					.findOneByHql(
							" WHERE sendTime < ? and exceedTime > ? and iosSent=? ORDER BY id DESC",
							new Object[] { now, now, Boolean.FALSE });
			if (message != null) {
				message = (PushMessage) pushMessageDao
						.getSessionFactory()
						.getCurrentSession()
						.load(PushMessage.class, message.getId(),
								LockMode.UPGRADE);
				if (message.getIosSent() != null && message.getIosSent())
					return;
				message.setIosSent(true);
				pushMessageDao.update(message);
			}
		} catch (SqlException e1) {
			message = null;
			e1.printStackTrace();
		}
		if (message == null)
			return;
		Statement st = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			String token = "";
			String plat = "ipad";
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://172.16.1.10/ytsp?"
					+ "useUnicode=true&characterEncoding=utf-8", "imagemedia",
					"Kandongman");
			final String strIphone = IOSPushMsgService.class.getResource(
					"/Push_iphone_dis.p12").getFile();
			final String strIpad = IOSPushMsgService.class.getResource(
					"/Push_ipad_dis.p12").getFile();
			st = con.createStatement();
			rs = st.executeQuery("SELECT r.terminal_type ,r.device_token "
					+ "FROM ytsp_hardware_reg r where LENGTH(r.device_token)>0 ");
			/* 建立队列 */
			String content = message.getTitle() + message.getContent();
			NotificationThreads queuephone = (NotificationThreads) Push.queue(
					strIphone, "Kandongman", true, 100);
			NotificationThreads queuepad = (NotificationThreads) Push.queue(
					strIpad, "Kandongman", true, 100);
			PushNotificationPayload payload = new PushNotificationPayload();
			payload.addCustomAlertActionLocKey("打开看看");
			payload.addCustomAlertLocKey(content);
			payload.addCustomDictionary("id", message.getId());
			// if (message.getType() == MessageTypeEnum.album) {
			// Album album = albumDao.findById(Integer.parseInt(message
			// .getParams()));
			// payload.addCustomDictionary("msgType", "album");
			// payload.addCustomDictionary("albumId", message.getParams());
			// payload.addCustomDictionary("albumType", album.getType()
			// .getValue());
			// }
			// if (message.getType() == MessageTypeEnum.ebproduct) {
			// payload.addCustomDictionary("msgType", "ebproduct");
			// payload.addCustomDictionary("productId",
			// Integer.parseInt(message.getParams()));
			// }
			// if (message.getType() == MessageTypeEnum.ebactivity) {
			// EbActivityDao ed = SystemInitialization.getApplicationContext()
			// .getBean(EbActivityDao.class);
			// EbActivity act = ed.findById(Integer.parseInt(message
			// .getParams()));
			// payload.addCustomDictionary("msgType", "ebActivity");
			// payload.addCustomDictionary("ebActivityId",
			// Integer.parseInt(message.getParams()));
			// payload.addCustomDictionary("ebActivityName",
			// act.getActivityName());
			// }


			final Map<String, String> map = new HashMap<String, String>();
			while (rs.next()) {
				plat = rs.getString("terminal_type");
				token = rs.getString("device_token");
				if (token != null && !token.trim().equals(""))
					token = token.replaceAll(" ", "");
				else
					continue;
				map.put(token, plat);
			}
			PayloadPerDevice device = null;
			for (Entry<String, String> entry : map.entrySet()) {
				try {
					device = new PayloadPerDevice(payload, entry.getKey());
				} catch (Exception e) {
					System.err.println(token);
					continue;
				}
				if (entry.getValue().equals("iphone")) {
					queuephone.add(device);
				} else {
					queuepad.add(device);
				}
			}
			queuepad.setListener(new NotificationProgressListener("queuepad"));
			queuephone.setListener(new NotificationProgressListener(
					"queuephone"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	static class NotificationProgressListener implements
			javapns.notification.transmission.NotificationProgressListener {
		String name;

		public NotificationProgressListener(String string) {
			this.name = string;
		}

		public void eventThreadStarted(NotificationThread notificationThread) {
		}

		public void eventThreadFinished(NotificationThread notificationThread) {
		}

		public void eventCriticalException(
				NotificationThread notificationThread, Exception exception) {
		}

		public void eventConnectionRestarted(
				NotificationThread notificationThread) {
		}

		public void eventAllThreadsStarted(
				NotificationThreads notificationThreads) {
		}

		public void eventAllThreadsFinished(
				NotificationThreads notificationThreads) {
			System.out.println(name + " finished");
			PushedNotifications failed = notificationThreads
					.getFailedNotifications();
			PushedNotifications success = notificationThreads
					.getSuccessfulNotifications();
			System.out.println(name + " failed:" + failed.capacity());
			System.out.println(name + " success:" + success.capacity());
		}
	}
}
