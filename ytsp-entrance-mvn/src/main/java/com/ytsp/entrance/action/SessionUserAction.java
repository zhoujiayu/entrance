package com.ytsp.entrance.action;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.ytsp.db.domain.Customer;
import com.ytsp.entrance.listener.SessionListener;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.util.WebClient;

public class SessionUserAction extends ActionSupport implements ServletRequestAware {// 该类继承了ActionSupport类。这样就可以直接使用SUCCESS,
	// LOGIN等变量和重写execute等方法

	private static final long serialVersionUID = 1L;

	private List<Customer> customers;
	private List<Customer> sessionlist;
	
	private String username;
	private String password;
	private int sessionTotal;
	private int total;
	private int noRepeatTotal;
	private Map<Integer, String> onlyMap;
	
	private float maxMem;

	private float totalMem;
	
	private float freeMemory;
	
	private String rspString;
	
	private String head="{'commandCode':301,'screenHeight':0,'uid':0,'platform':'web','ver':2.1,'rd':'','otherInfo':'test','uniqueId':'9774d56d682e549c','version':'2.1','ip':'10.0.2.15','timestamp':'','screenWidth':0,'sessionId':'','vpn':'','sig':''}";
	
	private String body;
	private String action;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	// 定义一个HttpServletRequest对象
		private HttpServletRequest request;

		// 实现ServletRequestAware接口必须重写的方法
		public void setServletRequest(HttpServletRequest request) {
			this.request = request;
		}

	
	public String executeAction() throws Exception {
		String ip = request.getLocalAddr();
		int port = request.getLocalPort();
		System.out.println(ip);
		System.out.println(port);
		
		String SERVER_INTERFACE = "http://"+ip+":"+port+"/entrance/entrance";
		
		System.out.println(SERVER_INTERFACE);
		
		byte[] rspByte = WebClient.getWebContentByPost(SERVER_INTERFACE, body, head);
		rspString = toStringAndReplaceChars(rspByte);
		System.out.println(rspString);
		
		return SUCCESS;
	}

	@Override
	public String execute() throws Exception {
		try
		{
			if(action != null)
			{
				return executeAction();
			}
			
			if ("admin".equals(username) && "Kandongman".equals(password)) {
				customers = new ArrayList<Customer>();
				onlyMap = new HashMap<Integer, String>();
				Map<String, HttpSession> sessions = SessionListener.getSessions();
				if (sessions != null) {
					sessionlist = new ArrayList<Customer>();
					for (String key : sessions.keySet()) {
						HttpSession session = sessions.get(key);
						SessionCustomer customer = (SessionCustomer) session
								.getAttribute(IConstants.SESSION_CUSTOMER);
						if (customer != null) {
							Customer c = customer.getCustomer();
							c.setPassword(session.getId());
							c.setRegisterCity(getTime(session.getCreationTime()));
							customers.add(c);
							total++;
							if (!onlyMap.containsKey(c.getId())) {
								onlyMap.put(c.getId(), c.getAccount());
								noRepeatTotal++;
							}
						}
						else
						{
							Customer c = new Customer();
							c.setId((Integer)session.getAttribute(IConstants.SESSION_Uid));
							c.setPassword(session.getId());
							c.setTerminalNumber((String)session.getAttribute(IConstants.SESSION_UniqueId));
							c.setNick(String.valueOf((Integer)session.getAttribute(IConstants.SESSION_OP)));
							c.setTerminalType((String)session.getAttribute(IConstants.SESSION_Platform));
							c.setTerminalVersion((String)session.getAttribute(IConstants.SESSION_Version));
							c.setRegisterCity(getTime(session.getCreationTime()));
							
							sessionlist.add(c);
						}
						sessionTotal++;
					}
				}
				Comparator<Customer> comp = new Comparator<Customer>() {
					public int compare(Customer o1, Customer o2) {
						Customer p1 = (Customer) o1;
						Customer p2 = (Customer) o2;
						if (p1.getCreateTime().before(p2.getCreateTime()))
							return 1;
						else
							return 0;
					}
				};
				Collections.sort(customers, comp);
				
				//内存，最大内存
				maxMem = Runtime.getRuntime().maxMemory()/1024/1024;
				totalMem = Runtime.getRuntime().totalMemory()/1024/1024;
				freeMemory = Runtime.getRuntime().freeMemory()/1024/1024;
				return SUCCESS;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return LOGIN;
	}
	public String toStringAndReplaceChars(byte[] rspByte) {
		String string = null;
		try {
			string = new String(rspByte,"UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return string;
	}
	public List<Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}

	private String getTime(long timeTemp) {
		long time = timeTemp;
		Date date = new Date(time);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sf.format(date));
		return sf.format(date);
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getNoRepeatTotal() {
		return noRepeatTotal;
	}

	public void setNoRepeatTotal(int noRepeatTotal) {
		this.noRepeatTotal = noRepeatTotal;
	}

	public Map<Integer, String> getOnlyMap() {
		return onlyMap;
	}

	public void setOnlyMap(Map<Integer, String> onlyMap) {
		this.onlyMap = onlyMap;
	}

	public int getSessionTotal() {
		return sessionTotal;
	}

	public void setSessionTotal(int sessionTotal) {
		this.sessionTotal = sessionTotal;
	}

	public List<Customer> getSessionlist() {
		return sessionlist;
	}

	public void setSessionlist(List<Customer> sessionlist) {
		this.sessionlist = sessionlist;
	}

	public float getMaxMem() {
		return maxMem;
	}

	public void setMaxMem(float maxMem) {
		this.maxMem = maxMem;
	}

	public float getTotalMem() {
		return totalMem;
	}

	public void setTotalMem(float totalMem) {
		this.totalMem = totalMem;
	}

	public float getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(float freeMemory) {
		this.freeMemory = freeMemory;
	}

	public String getRspString() {
		return rspString;
	}

	public void setRspString(String rspString) {
		this.rspString = rspString;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	

}