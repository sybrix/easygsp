
java -Djava.util.logging.config.file=%EASYGSP_HOME%\conf\logging.properties -Deasygsp.home=%EASYGSP_HOME% -Djava.security.manager -Djava.security.policy=file://%EASYGSP_HOME%\conf\easygsp.policy -cp %EASYGSP_HOME%\.;%EASYGSP_HOME%\lib\*;%EASYGSP_HOME%\bin\easygsp.jar com.sybrix.easygsp.server.EasyGServer
