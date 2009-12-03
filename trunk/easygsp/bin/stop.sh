#!/bin/sh

JAVA_HOME=/opt/jdk1.6.0_17
EASYGSP_HOME=/usr/local/easygsp

export EASYGSP_HOME
export JAVA_HOME

$JAVA_HOME/bin/java -Djava.util.logging.config.file=%EASYGSP_HOME%\conf\logging.properties -Djava.util.logging.config.file=$EASYGSP_HOME/conf/logging.properties -Deasygsp.home=$EASYGSP_HOME -Djava.security.manager -Djava.security.policy=file:$EASYGSP_HOME/conf/easygsp.policy -cp .:$EASYGSP_HOME/.:$EASYGSP_HOME/lib/*:$EASYGSP_HOME/bin/easygsp.jar com.sybrix.easygsp.server.Shutdown $EASYGSP_HOME