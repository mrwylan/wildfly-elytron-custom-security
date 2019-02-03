package com.trivadis.mze.elytrondemo.ejb.client;

public interface ClientConfig {
	static final String SERVER_URL = "remote+http://localhost:8080";

	static final String CLIENT_NAME = "EJB-client";
	
	static final String REALM = "Custom Realm";
	static final String REGULAR_USERNAME = "jane";
	static final String REGULAR_USER_PASSWORD = "doe";
	static final String ADMIN_USERNAME = "admin";
	static final String ADMIN_PASSWORD = "demo";
	static final String GUEST_USERNAME = "guest";
	static final String GUEST_PASSWORD = "guest";
	
	static final String EJB_APP_NAME = "";
	static final String EJB_MODULE_NAME = "CustomRealm"; // see web.xml: module-name
	static final String EJB_DISTINCT_NAME = "";
	
}