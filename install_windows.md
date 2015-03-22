Windows | [Linux](install_linux.md)

## Install Java ##
  1. Download the latest jdk from http://www.oracle.com/technetwork/java/javase/downloads/index.html
  1. Install
  1. Create JAVA\_HOME environment variable
> #### To view or change environment variables: ####
    1. Right-click My Computer, and then click Properties.
    1. Click the Advanced tab
    1. Click Environment variables.
    1. Click one the following options, for either a user or a system variable:Click New to add a new variable name and value.
      1. Click an existing variable, and then click Edit to change its name or value.
      1. Click an existing variable, and then click Delete to remove it

## Installing EasyGSP ##
  1. Download EasyGSP @ http://code.google.com/p/easygsp/downloads/list
  1. Unzip to c:\
  1. Rename dir c:\easygsp-[VERSION](VERSION.md) to c:\easygsp
  1. Create EASYGSP\_HOME environment variable that points to c:\easygsp

## Start EasyGSP ##
  * c:\easygsp\EasyGSP.bat

## Install LightTPD ##
  1. Goto the WMLP Project Site http://en.wlmp-project.net/downloads.php
  1. Download the latest and greatest w/OpenSSL support
  1. Install to c:\LightTPD

## Configure LightTPD ##
  1. Download config file from: http://easygsp.googlecode.com/files/lighttpd-inc.conf
  1. Replace the current config file @ c:\LightTPD\conf\lighttpd-inc.conf (rename it) with the downloaded one
  1. Restart LightTPD

## Test Install ##
  * Open  browser to http://localhost:8085/examples
