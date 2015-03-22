[Windows](install_windows.md) | Linux

You may have to "sudo" for each of these commands

## Install Java ##
  1. Download the latest(non-rpm) jdk from http://www.oracle.com/technetwork/java/javase/downloads/
  1. Opena terminal window, cd to downloaded jdk, move to install location, install
```
  $ mkdir /opt/java
  $ mv jdk-6u22-linux-i586.bin /opt/java/
  $ cd /opt/java
  $ chmod a+x jdk-6u22-linux-i586.bin
  $ ./jdk-6u22-linux-i586.bin
```
  1. Create JAVA\_HOME environment variable by adding the following lines to /etc/profile
```
  JAVA_HOME=/opt/java/jdk1.6.0_22

  export JAVA_HOME
  export PATH=$PATH:$JAVA_HOME/bin
```
  1. Logout and log back in or try
```
  $ source /etc/profile
```
  1. Test variable
```
  $ java -version
```
> you should see the proper path

## Installing EasyGSP ##
```
  $ cd /opt
  $ wget http://easygsp.googlecode.com/files/easygsp-0.4.5.tar.gz
  $ tar -xvf easygsp-0.4.5.tar.gz
  $ ln -s easygsp-0.4.5 easygsp
  $ cd easygsp/bin
  $ chmod +x start.sh
  $ chmod +x stop.sh
  $ rm easygsp-0.4.5.tar.gz
```


**Start EasyGSP**
```
$ cd /opt/easygsp/bin
$ ./start.sh
```

or

```
$ sudo -E ./start.sh 
```


## Install LightTPD ##
**Ubuntu/Debian**

```
  $ sudo apt-get install lighttpd
```
**CentOS/Fedora**
```
  $ sudo yum install lighttpd
```

or

follow this link for more Lighttpd install options http://redmine.lighttpd.net/projects/1/wiki/GetLighttpd

## Configuring LightTPD ##
If you have an existing lighttpd install, skip this section and go here [Configuring Existing LightTDP install](config_http.md)

**Download EasyGSP Lighty Config**
```
    $ cd /opt
    $ wget http://easygsp.googlecode.com/files/lighttpd.conf
```

**Replace original lighttpd config file with downloaded one**
```
    $ mv /etc/lighttpd/lighttpd.conf /etc/lighttpd/lighttpd.conf_old 
    $ mv lighttpd.conf /etc/lighttpd/lighttpd.conf
```

**Restart lighttpd**
```
    $ /etc/init.d/lighttpd restart 
```





**Open browser, goto**
> http://localhost:8085/examples