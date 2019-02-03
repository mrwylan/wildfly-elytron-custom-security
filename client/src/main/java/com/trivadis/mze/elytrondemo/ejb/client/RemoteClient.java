package com.trivadis.mze.elytrondemo.ejb.client;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.security.sasl.SaslException;

import org.jboss.ejb.client.RequestSendFailedException;

import com.trivadis.mze.elytrondemo.ejb.remote.SecuredEJBRemote;

/**
 * The remote client responsible for making calls to the secured EJBs.
 * <p>
 * Demos and quickstart examples usually use wildfly-config.xml. This client
 * uses a programmatic client configuration to dynamically set a server and user
 * credentials. Furthermore we don't want to wrap the client code inside a
 * Callable (see quickstart 'ejb-security-programmatic-auth'), but simply call
 * login() / logout() with the new credentials.
 */
public class RemoteClient implements ClientConfig {

    private static final ServiceRegistry registry = ServiceRegistry.getInstance();

    public static void main(String[] args) throws Exception {
        testUser(GUEST_USERNAME, GUEST_PASSWORD);

        testUser(REGULAR_USERNAME, REGULAR_USER_PASSWORD);

        testUser(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    static void testUser(String username, String password) throws Exception {
        try {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Client login as: " + username);

            registry.login(username, password);

            performServiceCalls();

            registry.logout();

            System.out.println("--------------------------------------------------------------------------------");
        } catch (RequestSendFailedException e) {
            // get the real cause of the exception, RequestSendFailedException is too
            // generic...
            Exception re = findRealCause(e);
            if (re != null) {
                throw re;
            }
            throw e;
        }
    }

    static void performServiceCalls() throws Exception {
        SecuredEJBRemote securedEJB = registry.getService(SecuredEJBRemote.class);
        System.out.println("Called secured bean, caller principal " + securedEJB.getSecurityInformation());
        boolean hasAdminPermission = false;
        try {
            hasAdminPermission = securedEJB.administrativeMethod();
        } catch (EJBAccessException e) {
        }
        System.out.println("Principal has admin permission: " + hasAdminPermission);
    }

    /**
     * Find the real cause of failure in the given RequestSendFailedException. The
     * error message alone in RequestSendFailedException is too generic and doesn't
     * give any insight why the client call failed.
     * 
     * TODO could this be done in an EJB client interceptor?
     */
    static Exception findRealCause(RequestSendFailedException e) {
        Throwable cause = e.getCause();
        if (cause != null && (cause instanceof EJBException || cause instanceof SaslException)) {
            return (Exception) cause;
        }
        Throwable[] suppressed = e.getSuppressed();
        for (Throwable throwable : suppressed) {
            if (throwable instanceof RequestSendFailedException) {
                return findRealCause((RequestSendFailedException) throwable);
            }
            if (throwable instanceof EJBException || throwable instanceof SaslException) {
                return (Exception) throwable;
            }
        }

        return null;
    }
}
