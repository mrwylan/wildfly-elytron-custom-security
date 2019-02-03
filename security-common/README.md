Module security-common
======================

Shared security resources

 - custom principal implementation
 - installed as WildFly module


# Compile

        mvn package

# Add module into WildFly

        bin/jboss-cli.sh
        module add --name=com.trivadis.mze.elytrondemo.security-common --resources=/tmp/security-common-1.0-SNAPSHOT.jar

