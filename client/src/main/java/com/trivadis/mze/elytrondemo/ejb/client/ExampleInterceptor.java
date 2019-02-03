package com.trivadis.mze.elytrondemo.ejb.client;

import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;

/**
 * Example EJB client interceptor.
 *
 * Configured in: META-INF/services/org.jboss.ejb.client.EJBClientInterceptor
 */
public class ExampleInterceptor implements EJBClientInterceptor {

	@Override
	public void handleInvocation(EJBClientInvocationContext context) throws Exception {
		// TODO Auto-generated method stub
		try {
			context.sendRequest();
		} catch (Exception e) {
			System.err.println("handleInvocation: " + e);
			throw e;
		}

	}

	@Override
	public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
		// TODO Auto-generated method stub
		try {
			return context.getResult();
		} catch (Exception e) {
			System.err.println("handleInvocationResult: " + e);
			throw e;
		}
	}

}
