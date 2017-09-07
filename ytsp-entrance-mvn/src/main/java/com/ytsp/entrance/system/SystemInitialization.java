/*
 * $Id: SystemInitialization.java 501 2011-08-18 11:05:56Z gene $
 * All rights reserved
 */
package com.ytsp.entrance.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.rmi.registry.Registry;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jmx.mbeanserver.JmxMBeanServer;
import com.sun.jmx.mbeanserver.MBeanInstantiator;
import com.ytsp.common.jmx.JMX;
import com.ytsp.common.jmx.ServiceManager;
import com.ytsp.common.util.NetUtil;
import com.ytsp.common.util.W3CDomUtil;
import com.ytsp.entrance.command.AlbumCommand;
import com.ytsp.entrance.command.AlbumTopListCommand;
import com.ytsp.entrance.command.ChannelCommand;
import com.ytsp.entrance.command.CustomerCommand;
import com.ytsp.entrance.command.FavoritesCommand;
import com.ytsp.entrance.command.LoginCommand;
import com.ytsp.entrance.command.MemberCommand;
import com.ytsp.entrance.command.ParentControlCommand;
import com.ytsp.entrance.command.PointCommand;
import com.ytsp.entrance.command.PushMessageCommand;
import com.ytsp.entrance.command.QuestionsCommand;
import com.ytsp.entrance.command.RechargeCommand;
import com.ytsp.entrance.command.RecommendCommand;
import com.ytsp.entrance.command.RegistCommand;
import com.ytsp.entrance.command.SystemConfigCommand;
import com.ytsp.entrance.command.SystemMonitorCommand;
import com.ytsp.entrance.command.VersionCommand;
import com.ytsp.entrance.command.VideoCommand;
import com.ytsp.entrance.command.base.CommandHandler;
import com.ytsp.entrance.command.v3_0.ActivityCommand;
import com.ytsp.entrance.command.v3_0.AlbumCommandV3;
import com.ytsp.entrance.command.v3_0.ChannelCommandV3;
import com.ytsp.entrance.command.v3_0.CheckVoiceCommand;
import com.ytsp.entrance.command.v3_0.QuestionsCommandV3;
import com.ytsp.entrance.command.v3_1.AdVideoCommand;
import com.ytsp.entrance.command.v3_1.MemberCommandV31;
import com.ytsp.entrance.command.v4_0.ADCommand;
import com.ytsp.entrance.command.v4_0.AlbumCommandV4;
import com.ytsp.entrance.command.v4_0.CreditCommand;
import com.ytsp.entrance.command.v4_0.EbActivityCommand;
import com.ytsp.entrance.command.v4_0.EbOrderCommand;
import com.ytsp.entrance.command.v4_0.EbProductCommand;
import com.ytsp.entrance.command.v4_0.EbSkillOrderCommand;
import com.ytsp.entrance.command.v4_0.EbUserAddressCommand;
import com.ytsp.entrance.command.v4_0.FeedbackCommand;
import com.ytsp.entrance.command.v4_0.ForgetPwdCommand;
import com.ytsp.entrance.command.v4_0.LoginCommandv4_0;
import com.ytsp.entrance.command.v4_0.RecommendCommandV4;
import com.ytsp.entrance.command.v5_0.ADCommandV5_0;
import com.ytsp.entrance.command.v5_0.ActivityCommandV5_0;
import com.ytsp.entrance.command.v5_0.AlbumCommandV5_0;
import com.ytsp.entrance.command.v5_0.AlipayCommand;
import com.ytsp.entrance.command.v5_0.BabyCommand;
import com.ytsp.entrance.command.v5_0.BrandCommand;
import com.ytsp.entrance.command.v5_0.CommentCommand;
import com.ytsp.entrance.command.v5_0.CouponCommand;
import com.ytsp.entrance.command.v5_0.CreditCommandV5_0;
import com.ytsp.entrance.command.v5_0.CustomerCommandV5_0;
import com.ytsp.entrance.command.v5_0.EbSpecialCommand;
import com.ytsp.entrance.command.v5_0.ExceptionLogCommand;
import com.ytsp.entrance.command.v5_0.FeedbackCommandV5_0;
import com.ytsp.entrance.command.v5_0.KnowledgeCommand;
import com.ytsp.entrance.command.v5_0.LoginCommandv5_0;
import com.ytsp.entrance.command.v5_0.OrderCommand;
import com.ytsp.entrance.command.v5_0.ProductCommandV5_0;
import com.ytsp.entrance.command.v5_0.RecommendCommandV5_0;
import com.ytsp.entrance.command.v5_0.RegistCommandV5_0;
import com.ytsp.entrance.command.v5_0.ScanningCommand;
import com.ytsp.entrance.command.v5_0.SearchCommand;
import com.ytsp.entrance.command.v5_0.ShoppingCartCommand;
import com.ytsp.entrance.command.v5_0.StatisticsCommandV5_0;
import com.ytsp.entrance.command.v5_0.TopicCommand;
import com.ytsp.entrance.command.v5_0.TrackCommand;
import com.ytsp.entrance.command.v5_0.VIPCommandV5_0;
import com.ytsp.entrance.command.v5_0.VideoReviewCommand;
import com.ytsp.entrance.command.v5_0.WXPayCmd;
import com.ytsp.entrance.command.wapmobile.WapMobileLogin;
import com.ytsp.entrance.quartz.IOSPushMsgService;
import com.ytsp.entrance.quartz.InvalidOrderCleaner;
import com.ytsp.entrance.service.SystemParamService;
import com.ytsp.entrance.util.OrderIdGenerationUtil;

/**
 * 系统初始化，包括JMX服务启动等。
 * 
 * @author Louis
 */
public class SystemInitialization implements ServletContextListener {

	protected final static transient Logger logger = Logger
			.getLogger(SystemInitialization.class.getName());

	private static final String SYSTEM_FILE = "WEB-INF/system.xml";
	private static final String JMXSERVICE_FILE = "WEB-INF/jmx-service.xml";
	private static final String BALANCE_FILE = "WEB-INF/balance.xml";
	private static final String MASTER_FILE = "master.properties";

	private MBeanServer mbs;
	private JMXConnectorServer norcs;
	private Registry rmiRegistry;
	private static WebApplicationContext ctx;

	public void contextInitialized(ServletContextEvent sce) {
		if (logger.isEnabledFor(Level.INFO)) {
			logger.info("开始加载系统配置...");
		}
		ServletContext sctx = sce.getServletContext();

		// 初始话spring的WebApplicationContext
		ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(sctx);

		connectBalanceNet(sctx);
		initSystemConfig(sctx);
		// 暂时不使用JMX服务
		// initJMX(sctx);
		initCommand(sctx);
	}

	private void initSystemConfig(ServletContext sctx) {
		SystemConfig sysConfig = new SystemConfig();
		SystemStatus sysStatus = new SystemStatus();

		// system config
		if (logger.isEnabledFor(Level.INFO)) {
			logger.info("加载系统配置文件：" + SYSTEM_FILE);
		}

		try {
			parseSystemConfig(sctx, sysConfig);
			System.setProperty("java.rmi.server.hostname",
					sysConfig.getJmxHost());
		} catch (Exception e) {
			throw new SystemException("加载system.xml配置出错", e);
		}

		if (logger.isEnabledFor(Level.INFO)) {
			logger.info(String.format("JMX服务主机地址 %s:%s",
					sysConfig.getJmxHost(), sysConfig.getJmxPort()));
			logger.info(String.format("JMX服务连接协议:%s",
					sysConfig.getJmxProtocol()));
		}

		sctx.setAttribute(IConstants.SYSTEM_CONFIG_KEY, sysConfig);
		sctx.setAttribute(IConstants.SYSTEM_STATUS_KEY, sysStatus);
		SystemManager.getInstance().setSystemConfig(sysConfig);
		SystemManager.getInstance().setSystemStatus(sysStatus);

		logger.info("加载存储于数据库中的系统参数");
		SystemParamService sps = ctx.getBean(SystemParamService.class);
		try {
			sps.syncVar();
		} catch (Exception e) {
			logger.error("加载存储于数据库中的系统参数异常！", e);
		}
	}

	protected void initJMX(ServletContext sctx) {
		// jmx service
		String jmxServicePath = sctx.getRealPath(JMXSERVICE_FILE);
		if (logger.isEnabledFor(Level.INFO)) {
			logger.info("加载JMX配置：" + JMXSERVICE_FILE);
		}

		SystemConfig sysConfig = SystemManager.getInstance().getSystemConfig();
		File jmxServiceFile = new File(jmxServicePath);
		try {
			startService(jmxServiceFile, sysConfig.getJmxHost(),
					sysConfig.getJmxPort(), sysConfig.getJmxProtocol());
		} catch (Exception e) {
			throw new SystemException("启动JMX服务失败...", e);
		}
	}

	/**
	 * 初始化命令执行器
	 * 
	 * @param sctx
	 * @throws SystemException
	 */
	private void initCommand(ServletContext sctx) throws SystemException {
		CommandHandler handler = CommandHandler.getInstance();

		handler.registCommand(VideoCommand.class);
		handler.registCommand(RecommendCommand.class);
		handler.registCommand(AlbumCommand.class);
		handler.registCommand(AlbumTopListCommand.class);
		handler.registCommand(FavoritesCommand.class);
		handler.registCommand(RegistCommand.class);
		handler.registCommand(LoginCommand.class);
		handler.registCommand(QuestionsCommand.class);
		handler.registCommand(CustomerCommand.class);
		handler.registCommand(PushMessageCommand.class);
		handler.registCommand(RechargeCommand.class);
		handler.registCommand(VersionCommand.class);
		handler.registCommand(SystemConfigCommand.class);
		handler.registCommand(ChannelCommand.class);
		handler.registCommand(ParentControlCommand.class);
		handler.registCommand(PointCommand.class);
		handler.registCommand(MemberCommand.class);
		handler.registCommand(AlbumCommandV3.class);
		handler.registCommand(ChannelCommandV3.class);
		handler.registCommand(QuestionsCommandV3.class);
		handler.registCommand(ActivityCommand.class);
		handler.registCommand(CheckVoiceCommand.class);
		handler.registCommand(MemberCommandV31.class);
		handler.registCommand(AdVideoCommand.class);
		handler.registCommand(SystemMonitorCommand.class);
		handler.registCommand(CreditCommand.class);
		handler.registCommand(EbActivityCommand.class);
		handler.registCommand(EbOrderCommand.class);
		handler.registCommand(EbProductCommand.class);
		handler.registCommand(EbSkillOrderCommand.class);
		handler.registCommand(RecommendCommandV4.class);
		handler.registCommand(EbUserAddressCommand.class);
		handler.registCommand(LoginCommandv4_0.class);
		handler.registCommand(FeedbackCommand.class);
		handler.registCommand(ForgetPwdCommand.class);
		handler.registCommand(ADCommand.class);
		handler.registCommand(AlbumCommandV4.class);
		handler.registCommand(CouponCommand.class);
		handler.registCommand(OrderCommand.class);
		handler.registCommand(ShoppingCartCommand.class);
		handler.registCommand(ProductCommandV5_0.class);
		handler.registCommand(EbSpecialCommand.class);
		handler.registCommand(RecommendCommandV5_0.class);
		handler.registCommand(CommentCommand.class);
		handler.registCommand(SearchCommand.class);
		handler.registCommand(WXPayCmd.class);
		handler.registCommand(AlbumCommandV5_0.class);
		handler.registCommand(TrackCommand.class);
		handler.registCommand(BabyCommand.class);
		handler.registCommand(ActivityCommandV5_0.class);
		handler.registCommand(TopicCommand.class);
		handler.registCommand(KnowledgeCommand.class);
		handler.registCommand(VIPCommandV5_0.class);
		handler.registCommand(BrandCommand.class);
		handler.registCommand(RegistCommandV5_0.class);
		handler.registCommand(FeedbackCommandV5_0.class);
		handler.registCommand(CustomerCommandV5_0.class);	
		handler.registCommand(ADCommandV5_0.class);
		handler.registCommand(VideoReviewCommand.class);
		handler.registCommand(CreditCommandV5_0.class);
		handler.registCommand(ScanningCommand.class);
		handler.registCommand(StatisticsCommandV5_0.class);
		handler.registCommand(WapMobileLogin.class);
		handler.registCommand(AlipayCommand.class);
		handler.registCommand(LoginCommandv5_0.class);
		handler.registCommand(ExceptionLogCommand.class);
	}

	private void parseSystemConfig(ServletContext sctx, SystemConfig sysConfig)
			throws Exception {
		String systemPath = sctx.getRealPath(SYSTEM_FILE);

		Document doc = W3CDomUtil.loadDocument(systemPath, true);

		// host
		Node hostlNode = W3CDomUtil.selectSingleNode(doc,
				"/system/jmx-service/host");
		if (hostlNode == null) {
			String hostname = NetUtil.getLocalIP();
			sysConfig.setJmxHost(hostname);
			logger.info("系统配置没有配置JMX服务的主机地址，自动识别为：" + hostname);
		} else {
			String hostStr = W3CDomUtil.getTextTrim(hostlNode);
			if (hostStr == null) {
				throw new IllegalArgumentException("JMX服务的主机地址配置不能为空");
			}
			sysConfig.setJmxHost(hostStr);
		}
		// 每台服务器有不同的orderseed
//		Node master = W3CDomUtil.selectSingleNode(doc, "/system/master");
//		if (master != null) {
//			int seed = 101;
//			if (W3CDomUtil.getTextTrim(master).equals("1")) {
//				InvalidOrderCleaner.skip = false;// 主机
//				IOSPushMsgService.skip = false;
//				seed = 100;
//			}
//			OrderIdGenerationUtil.getInstance().setSeed(seed);
//		}
		Integer masterNum = getMasterHost(MASTER_FILE, "master");
		if(masterNum != null){
			if(masterNum == 3){
				InvalidOrderCleaner.skip = false;// 主机
				IOSPushMsgService.skip = false;
			}
			OrderIdGenerationUtil.getInstance().setMaster(masterNum);
		}
		// port
		org.w3c.dom.Node portNode = W3CDomUtil.selectSingleNode(doc,
				"/system/jmx-service/port");
		if (portNode == null) {
			throw new SystemException("系统配置缺少JMX服务的端口配置，请检查system.xml配置文件");
		}
		String portStr = W3CDomUtil.getTextTrim(portNode);
		if (portStr == null) {
			throw new IllegalArgumentException("JMX服务的端口配置不能为空");
		}
		int port = Integer.parseInt(portStr);
		sysConfig.setJmxPort(port);

		// protocol
		org.w3c.dom.Node protocolNode = W3CDomUtil.selectSingleNode(doc,
				"/system/jmx-service/protocol");
		if (protocolNode == null) {
			throw new SystemException("系统配置缺少JMX服务的连接协议配置，请检查system.xml配置文件");
		}
		String protocolStr = W3CDomUtil.getTextTrim(protocolNode);
		if (protocolStr == null) {
			throw new IllegalArgumentException("JMX服务的连接协议配置不能为空");
		}
		sysConfig.setJmxProtocol(protocolStr);

		NodeList urlNodes = W3CDomUtil.selectNodes(doc,
				"/system/img-service/url");
		if (urlNodes.getLength() == 0) {
			throw new SystemException("系统配置缺少图片服务器访问路径配置，请检查system.xml配置文件");
		}

		org.w3c.dom.Node urlNode = urlNodes.item(0);
		String urlStr = W3CDomUtil.getTextTrim(urlNode);
		if (urlStr == null) {
			throw new IllegalArgumentException("图片服务器地址配置不能为空");
		}
		sysConfig.setImgServerUrl(urlStr);

		NodeList savepathNodes = W3CDomUtil.selectNodes(doc,
				"/system/img-service/savepath");
		if (savepathNodes.getLength() == 0) {
			throw new SystemException("系统配置缺少图片服务器文件保存路径配置，请检查system.xml配置文件");
		}

		org.w3c.dom.Node savepathNode = savepathNodes.item(0);
		String savepathStr = W3CDomUtil.getTextTrim(savepathNode);
		if (savepathStr == null) {
			throw new IllegalArgumentException("图片服务器文件保存路径配置不能为空");
		}
		sysConfig.setImgSavePath(savepathStr);
	}

	private void connectBalanceNet(ServletContext sctx) throws SystemException {
		// logger.info("[初始化负载均衡器]");
		// try {
		// URL url = new File(sctx.getRealPath(BALANCE_FILE)).toURI().toURL();
		// BalanceManager.create(url);
		// } catch (MalformedURLException e) {
		// throw new SystemException("初始化负载均衡器失败...", e);
		// }
	}

	private void startService(File jmxServiceFile, String hostname, int port,
			String protocol) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {// 注册JVM退出时处理释放JMX资源的的钩子
					@Override
					public void run() {
						super.run();
						shutdownJMXResource();
					}
				});

		MBeanServer mbs = MBeanServerFactory.createMBeanServer();
		// FIXME 以下代码存在风险，由于没有找到有效注册ClassLoader的机制，因此使用反射
		// 侵入com.sun.jmx.mbeanserver.JmxMBeanServer获取instantiator以调用
		// getClassLoaderRepository()得到类加载器发布器来添加JEE容器的ClassLoader，这
		// 样才能使JEE里的MBean被MBeanServer识别。
		JmxMBeanServer sunMBS = (JmxMBeanServer) mbs;
		getInstantiator(sunMBS).getClassLoaderRepository().addClassLoader(
				getClass().getClassLoader());

		ServiceManager manager = new ServiceManager(jmxServiceFile, mbs,
				getClass().getClassLoader());
		manager.loadServices();
		manager.startServices();

		// JMXServiceURL url = new JMXServiceURL(protocol, hostname, port);
		// rmiRegistry = LocateRegistry.createRegistry(port);
		// JMXServiceURL url =
		// new JMXServiceURL("service:jmx:rmi://"+hostname+
		// ":"+port+"/jndi/rmi://"+hostname+":"+port+"/jmxrmi");
		// HashMap<String,Object> env = new HashMap<String,Object>();
		// norcs = JMXConnectorServerFactory.newJMXConnectorServer(url, env,
		// mbs);
		norcs = JMX.newJmxConnectorServer(protocol, hostname, port, mbs);
		norcs.start();
	}

	private MBeanInstantiator getInstantiator(JmxMBeanServer server) {
		try {
			Field f = JmxMBeanServer.class.getDeclaredField("instantiator");
			f.setAccessible(true);
			Object obj = f.get(server);
			return (MBeanInstantiator) obj;
		} catch (Throwable t) {
			throw new IllegalArgumentException(
					"反射JmxMBeanServer变量域['instantiator']失败", t);
		}
	}

	public static WebApplicationContext getApplicationContext() {
		return ctx;
	}

	protected void shutdownJMXResource() {
		if (norcs != null && norcs.isActive()) {
			try {
				norcs.stop();
			} catch (IOException e) {
				if (logger.isEnabledFor(Level.ERROR)) {
					logger.error("关闭JMX连接服务器失败...", e);
				}
			}
		}
		// if(rmiRegistry != null) {
		// try {
		// UnicastRemoteObject.unexportObject(rmiRegistry, true);
		// } catch (NoSuchObjectException e) {
		// if(logger.isEnabledFor(Level.ERROR)) {
		// logger.error("关闭JMX RMI注册端口失败...", e);
		// }
		// }
		// try {
		// PortableRemoteObject.unexportObject(rmiRegistry);
		// } catch (NoSuchObjectException e) {
		// if(logger.isEnabledFor(Level.ERROR)) {
		// logger.error("关闭JMX RMI注册端口失败...", e);
		// }
		// }
		// }
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// 在JVM退出前在此destory处先执行一次JMX资源释放，尽可能保证释放
		shutdownJMXResource();
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@param name
	* <p>参数：@param key
	* <p>参数：@return</p>
	* <p>返回类型：int</p>
	 */
	private Integer getMasterHost(String name,String key) {
		Integer master = null;
		InputStream is = null;
		Properties props = new Properties();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			classLoader = SystemInitialization.class.getClassLoader();
		}
		is = classLoader.getResourceAsStream(name);
		if (is != null) {
			try {
				props.load(is);

			} catch (IOException e) {
				logger.error("getMasterHost()", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("getMasterHost()", e);
				}
			}
		}
		if(props != null || !props.isEmpty()){
			master = Integer.parseInt(((String)props.get(key)).trim());
		}
		return master;
	}
	
}
