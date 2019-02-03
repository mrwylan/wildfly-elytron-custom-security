Module security-module 
======================

Custom WildFly Elytron security

Based on:

 - [Creating custom security realm for WildFly Elytron](https://hkalina.github.io/2018/06/06/custom-realm/)
 

# Compile

        mvn package

# Add module into WildFly


        bin/jboss-cli.sh
        module add --name=com.trivadis.mze.elytrondemo.custom-elytron \
                   --resources=/tmp/security-module-1.0-SNAPSHOT.jar \
                   --dependencies=com.trivadis.mze.elytrondemo.security-common,org.wildfly.security.elytron,org.wildfly.extension.elytron


# Add our custom realm

        bin/jboss-cli.sh -c
        /subsystem=elytron/custom-realm=DemoCustomRealm:add( \
            class-name=com.trivadis.mze.elytrondemo.security.realm.CustomSecurityRealm, \
            module=com.trivadis.mze.elytrondemo.custom-elytron, \
            configuration={ \
              user.admin="demo", \
              groups.admin="admin,user,guest", \
              user.jane="doe", \
              groups.jane="user,guest", \
              user.guest="guest", \
              groups.guest="guest" \
               })

# Add our custom principal transformer

        bin/jboss-cli.sh -c
        /subsystem=elytron/custom-principal-transformer=CustomPrincipalTransformer:add( \
            class-name=com.trivadis.mze.elytrondemo.security.transformer.CustomPrincipalTransformer, \
            module=com.trivadis.mze.elytrondemo.custom-elytron)

