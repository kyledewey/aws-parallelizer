#!/bin/sh

SHOULD_SHUTDOWN_SENTINEL="/tmp/should_shutdown"
export CLASSPATH=.:aws-java-sdk-1.10.69/lib/aws-java-sdk-1.10.69-javadoc.jar:aws-java-sdk-1.10.69/lib/aws-java-sdk-1.10.69.jar:aws-java-sdk-1.10.69/lib/aws-java-sdk-flow-build-tools-1.10.69.jar:aws-java-sdk-1.10.69/third-party/lib/aspectjrt-1.8.2.jar:aws-java-sdk-1.10.69/third-party/lib/aspectjweaver.jar:aws-java-sdk-1.10.69/third-party/lib/commons-codec-1.6.jar:aws-java-sdk-1.10.69/third-party/lib/commons-logging-1.1.3.jar:aws-java-sdk-1.10.69/third-party/lib/freemarker-2.3.9.jar:aws-java-sdk-1.10.69/third-party/lib/httpclient-4.3.6.jar:aws-java-sdk-1.10.69/third-party/lib/httpcore-4.3.3.jar:aws-java-sdk-1.10.69/third-party/lib/jackson-annotations-2.5.0.jar:aws-java-sdk-1.10.69/third-party/lib/jackson-core-2.5.3.jar:aws-java-sdk-1.10.69/third-party/lib/jackson-databind-2.5.3.jar:aws-java-sdk-1.10.69/third-party/lib/jackson-dataformat-cbor-2.5.3.jar:aws-java-sdk-1.10.69/third-party/lib/javax.mail-api-1.4.6.jar:aws-java-sdk-1.10.69/third-party/lib/joda-time-2.8.1.jar:aws-java-sdk-1.10.69/third-party/lib/spring-beans-3.0.7.RELEASE.jar:aws-java-sdk-1.10.69/third-party/lib/spring-context-3.0.7.RELEASE.jar:aws-java-sdk-1.10.69/third-party/lib/spring-core-3.0.7.RELEASE.jar:aws-java-sdk-1.10.69/third-party/lib/spring-test-3.0.7.RELEASE.jar

cd /home/ec2-user
java Client

if [ -f $SHOULD_SHUTDOWN_SENTINEL ]
then
    java CancelPersistent
    shutdown -h now
fi

