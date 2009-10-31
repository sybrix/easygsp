#!/bin/sh

JAVA_HOME=/usr/java/jdk1.6.0_14
EASYGSP_HOME=/usr/local/share/easygsp

export JAVA_HOME 
export EASYGSP_HOME


$JAVA_HOME/bin/java -Djava.util.logging.config.file=$EASYGSP_HOME/conf/logging.properties -Deasygsp.home=$EASYGSP_HOME -Djava.security.manager -Djava.security.policy=file:$EASYGSP_HOME/conf/easygsp.policy -cp .:$EASYGSP_HOME/.:$EASYGSP_HOME/lib/*:$EASYGSP_HOME/bin/easygsp.jar com.sybrix.easygsp.server.EasyGServer &



