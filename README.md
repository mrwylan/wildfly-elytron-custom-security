WildFly Elytron custom security demo project
============================================

---

Work in progress - more features will be added...

---

Proof of concept project to test different features with the new Elytron security system.

The following features were chosen for a JBoss AS6 project migration to WildFly 15:

 - Custom realm

   The old LoginModule is still supported but deprecated (see [ejb-security-jaas quickstart example](https://github.com/wildfly/quickstart/tree/master/ejb-security-jaas)). Time for a custom realm...

 - Setting a custom principal in the security system

   Requirement: a custom principal is used throughout the application from the SessionContext.

 - Programmatic EJB client configuration
 
   Requirement: the same client jar is used for all environments. Server configuration is set in environment variable.
 
 - Dynamic EJB client (re-)authentication without using runnable
 
   Requirement: always on rich client in kiosk mode with user login / logout functionality. Client cannot be restarted. (Runnable implementation as in the [ejb-security-programmatic-auth quickstart example](https://github.com/wildfly/quickstart/tree/master/ejb-security-programmatic-auth) cannot be used).

 - Secured web application
 
 - TODO Secured RESTeasy resources

 - TODO Secured SOAP web services

## Environment

 - WildFly 15
 - JDK 8
 - Maven 3.3

## Project modules

 - security-common (WildFly module)
     - Shared security resources
     - Custom principal implementation
 - security-module (WildFly module)
     - Elytron security components
     - Custom realm with configurable users and groups
     - Custom principal transformer  
 - webapp
     - EJB backend with simple test servlet
 - ejb-client
     - EJB client resources
 - client
     - Standalone client with remote EJB calls
     - Programmatic JNDI context configuration and dynamic authentication

# Setup

## Build

        mvn clean install

## Add modules into WildFly

        $WILDFLY_HOME/bin/jboss-cli.sh --file=install-modules.cli

## Configure Elytron security

        $WILDFLY_HOME/bin/jboss-cli.sh -c --file=configure.cli

## Deploy webapp

        cp /webapp/target/webapp-1.0-SNAPSHOT.war $WILDFLY_HOME/standalone/deployments/

# Test programs

 - Servlet: localhost:8080/CustomRealm/
 - EJB client: ./client/src/main/java/com/trivadis/mze/elytrondemo/ejb/client/RemoteClient.java