package com.trivadis.mze.elytrondemo.ejb.client;

public class ClientUtil {

	public static <T> String getJndiName(Class<T> remoteInterface) {
		// Wildfly 9+
		return "ejb:" + ClientConfig.EJB_APP_NAME + "/" + ClientConfig.EJB_MODULE_NAME + "/" + ClientConfig.EJB_DISTINCT_NAME + "/"
				+ remoteInterface.getSimpleName().replaceFirst("Remote$", "") + "!" + remoteInterface.getName();
	}
	
}
