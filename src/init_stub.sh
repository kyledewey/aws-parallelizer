#!/bin/sh

SHOULD_SHUTDOWN_SENTINEL="/tmp/should_shutdown"
export CLASSPATH=.:aws-java-sdk-1.1.9/lib/aws-java-sdk-1.1.9.jar:aws-java-sdk-1.1.9/third-party/commons-codec-1.3/commons-codec-1.3.jar:aws-java-sdk-1.1.9/third-party/commons-httpclient-3.0.1/commons-httpclient-3.0.1.jar:aws-java-sdk-1.1.9/third-party/commons-logging-1.1.1/commons-logging-1.1.1.jar:aws-java-sdk-1.1.9/third-party/jackson-1.4/jackson-core-asl-1.4.3.jar:aws-java-sdk-1.1.9/third-party/java-mail-1.4.3/mail-1.4.3.jar:aws-java-sdk-1.1.9/third-party/stax-api-1.0.1/stax-api-1.0.1.jar:aws-java-sdk-1.1.9/third-party/stax-ri-1.2.0/stax-1.2.0.jar 

cd /home/ec2-user
java Client

if [ -f $SHOULD_SHUTDOWN_SENTINEL ]
then
    shutdown -h now
fi

