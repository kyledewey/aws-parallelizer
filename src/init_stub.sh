#!/bin/sh

SHOULD_SHUTDOWN_SENTINEL="/tmp/should_shutdown"
export CLASSPATH=.:aws-java-sdk-1.3.25/lib/aws-java-sdk-1.3.25.jar:aws-java-sdk-1.3.25/lib/aws-java-sdk-flow-build-tools-1.3.25.jar:aws-java-sdk-1.3.25/lib/aws-java-sdk-1.3.25-sources.jar:aws-java-sdk-1.3.25/lib/aws-java-sdk-1.3.25-javadoc.jar:aws-java-sdk-1.3.25/third-party/jackson-core-1.8/jackson-core-asl-1.8.7.jar:aws-java-sdk-1.3.25/third-party/java-mail-1.4.3/mail-1.4.3.jar:aws-java-sdk-1.3.25/third-party/commons-codec-1.3/commons-codec-1.3.jar:aws-java-sdk-1.3.25/third-party/httpcomponents-client-4.1.1/httpcore-4.1.jar:aws-java-sdk-1.3.25/third-party/httpcomponents-client-4.1.1/httpclient-4.1.1.jar:aws-java-sdk-1.3.25/third-party/jackson-mapper-1.8/jackson-mapper-asl-1.8.7.jar:aws-java-sdk-1.3.25/third-party/freemarker-2.3.18/freemarker-2.3.18.jar:aws-java-sdk-1.3.25/third-party/commons-logging-1.1.1/commons-logging-1.1.1.jar:aws-java-sdk-1.3.25/third-party/spring-3.0/spring-core-3.0.7.jar:aws-java-sdk-1.3.25/third-party/spring-3.0/spring-context-3.0.7.jar:aws-java-sdk-1.3.25/third-party/spring-3.0/spring-beans-3.0.7.jar:aws-java-sdk-1.3.25/third-party/aspectj-1.6/aspectjrt.jar:aws-java-sdk-1.3.25/third-party/aspectj-1.6/aspectjweaver.jar:aws-java-sdk-1.3.25/third-party/stax-ri-1.2.0/stax-1.2.0.jar:aws-java-sdk-1.3.25/third-party/stax-api-1.0.1/stax-api-1.0.1.jar

cd /home/ec2-user
java Client

if [ -f $SHOULD_SHUTDOWN_SENTINEL ]
then
    shutdown -h now
fi

