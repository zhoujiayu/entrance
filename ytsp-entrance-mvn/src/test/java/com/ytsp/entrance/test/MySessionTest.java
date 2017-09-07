package com.ytsp.entrance.test;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ytsp.session.service.SessionService;

//"classpath:META-INF/spring-db.xml",
@ContextConfiguration(locations = {"classpath:spring-session.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MySessionTest extends TestBase{
	
	@Resource(name = "sessionService")
	private SessionService sessionService;
	
	@Test
	public void test() {
		for (int i = 0 ; i < 1000; i++) {
			final int j = i;
			Thread t = new Thread(){
				@Override
				public void run() {
					super.run();
					String session = sessionService.signIn(j);
					sessionService.isSignIn(session,123);
				}
			};
			t.start();
		}
	
	}
}
