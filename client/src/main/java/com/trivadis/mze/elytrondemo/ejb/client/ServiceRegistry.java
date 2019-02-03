package com.trivadis.mze.elytrondemo.ejb.client;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.security.sasl.AuthenticationException;
import javax.security.sasl.SaslException;

import org.jboss.ejb.client.RequestSendFailedException;
import org.wildfly.common.context.ContextManager;
import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.sasl.SaslMechanismSelector;
import org.wildfly.security.sasl.util.SaslMechanismInformation;

import com.trivadis.mze.elytrondemo.ejb.remote.UserServiceRemote;

public class ServiceRegistry {

	private static ServiceRegistry instance;

	private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

	private ServiceRegistry() {
	}

	public static ServiceRegistry getInstance() {
		if (null == instance) {
			instance = new ServiceRegistry();
		}
		return instance;
	}

	public void login(String username, String password) throws Exception {
		try {
			AuthenticationContext authenticationContext = getProgrammaticAuthCtx(
					username + ":" + ClientConfig.CLIENT_NAME, password, ClientConfig.REALM);

			ContextManager<AuthenticationContext> contextManager = authenticationContext.getInstanceContextManager();
			contextManager.setGlobalDefault(authenticationContext);

			System.out.println("Logged in as: " + getService(UserServiceRemote.class).whoAmI());
		} catch (Exception e) {
			if (isLoginException(e)) {
				logout();
			}
			throw e;
		}
	}

	public void logout() {
		AuthenticationContext authenticationContext = AuthenticationContext.empty();
		ContextManager<AuthenticationContext> contextManager = authenticationContext.getInstanceContextManager();
		contextManager.setGlobalDefault(authenticationContext);
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> remoteInterface) {
		if (services.containsKey(remoteInterface)) {
			return (T) services.get(remoteInterface);
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(instance.getClass().getClassLoader());
		try {
			Context ctx = createJnpContext();
			String jndiLookupName = ClientUtil.getJndiName(remoteInterface);
			T service = (T) ctx.lookup(jndiLookupName);
			services.put(remoteInterface, service);
			return service;

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

	private AuthenticationContext getProgrammaticAuthCtx(String username, String password, String realm) {
		AuthenticationConfiguration config = AuthenticationConfiguration
				.empty().setSaslMechanismSelector(SaslMechanismSelector.NONE
						.addMechanism(SaslMechanismInformation.Names.PLAIN).forbidMechanism("JBOSS-LOCAL-USER"))
				.useName(username).usePassword(password)
		// .useRealm(realm) // seems to be optional, also works without
		;
		AuthenticationContext authenticationContext = AuthenticationContext.empty().with(MatchRule.ALL, config);

		return authenticationContext;
	}

	private Context createJnpContext() throws NamingException {
		final Hashtable<String, String> jndiProperties = new Hashtable<>();
		jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
		jndiProperties.put(Context.PROVIDER_URL, ClientConfig.SERVER_URL);
		return new InitialContext(jndiProperties);
	}

	private static boolean isLoginException(Throwable e) {
		// TODO WildFly 15 doesn't return a LoginException anymore as with AS6 :-(
		// The generic RequestSendFailedException and SaslException are a real pain to
		// deal with!
		// Deep down in the RequestSendFailedException chain there's just a
		// "javax.security.sasl.SaslException: PLAIN: Server rejected authentication"
		// not very convenient...

		// See: org.wildfly.security.sasl.plain.PlainSaslServer:127
		// if (evc.isVerified() == false) {
		// throw saslPlain.mechPasswordNotVerified().toSaslException();
		// }
		// Exception conversion:
		// public SaslException toSaslException() {
		// return copyContents(this, new SaslException(getMessage(), getCause()));
		// }
		//
		// --> Using Elytron security with a legacy LoginModule always returns generic
		// SaslException!
		// Looks like there's no way to get detailed login exceptions anymore :-(
		//
		// How do we handle the good javax.security.auth.login.AccountExpiredException,
		// AccountLockedException, CredentialExpiredException,
		// CredentialNotFoundException etc with Elytron?
		// TODO: test with an Elytron custom realm if exceptions are propagated to the
		// client...

		// AS6 logic:
		if (e instanceof LoginException) {
			return true;
		} else if (e.getCause() != null && e.getCause() instanceof LoginException) {
			return true;
		} else if (e.getCause() != null && e.getCause().getCause() != null
				&& e.getCause().getCause() instanceof LoginException) {
			return true;
		}

		// Ugly WildFly 15 attempt, but this is a dead end:
		// FIXME: recursively check all suppressed exceptions & causes
		if (e instanceof RequestSendFailedException) {
			if (e.getCause() != null && e.getCause() instanceof AuthenticationException) {
				return true;
			}
			for (Throwable suppressed : e.getSuppressed()) {
				if (suppressed instanceof AuthenticationException) {
					return true;
				}
				if (suppressed instanceof SaslException
						&& suppressed.getMessage().contains("Server rejected authentication")) {
					return true;
				}
				if (suppressed.getCause() != null) {
					if (suppressed.getCause() instanceof AuthenticationException) {
						return true;
					}
					if (suppressed.getCause() instanceof SaslException
							&& suppressed.getCause().getMessage().contains("Server rejected authentication")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
