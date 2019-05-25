package com.sj.ws;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware {
	private static ApplicationContext CONTEXT;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		CONTEXT = context;
	}

	// This static method returns already-created-by-springframework beans.
	// By accessing this method, we can get a bean any where in our application.
	public static Object getBean(String beanName) {
		return CONTEXT.getBean(beanName);
	}
}