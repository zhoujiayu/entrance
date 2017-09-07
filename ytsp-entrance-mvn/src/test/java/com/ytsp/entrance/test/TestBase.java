package com.ytsp.entrance.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//"classpath:META-INF/spring-db.xml",
//@ContextConfiguration(locations = {"classpath:META-INF/spring-db.xml","classpath:spring-datasource.xml","classpath:spring-service.xml","classpath:spring-session.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
public class TestBase extends AbstractJUnit4SpringContextTests{
	//--子类可以直接调用log属性
	protected final Log logger = LogFactory.getLog(getClass());
}
