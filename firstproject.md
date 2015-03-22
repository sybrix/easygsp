# Creating an Application #


A folder in the webapps directory represents in application in EasyGSP.  To create an application in EasyGSP

  * Create a folder in the %EASYGSP\_HOME%/webapps directory
  * Add a gsp file
  * Access that file via your web browser http://localhost:8085/MyApp/index.gsp


**Application Directory Layout**
```
 +-- $EASYGSP_HOME
       |
       +-- webapps
            |
            +- MyApp
                |
                |--index.gsp
```


Your application will start and EasyGSP will create a WEB-INF folder containing a web.groovy file.  Every application must have this folder and file, if they don't exist they wil be created on the first request for an application.


**web.groovy**

The web.groovy file allows you to execute custom code for certain application events like application and session start and end events. Each application can have a web.groovy file. The web.groovy file must be located in @ /APP\_FOLDER/WEB-INF/web.groovy

Like servlet based applications, EasyGSP applications can have a WEB-INF directory.  But unlike servlet based applications, there is no lib or classes folder nor is there a web.xml file.

Whenever the web.groovy file is modified, the application will restart.

```
 +-- $EASYGSP_HOME
       |
       +-- webapps
            |
            +-- MyApp
                 | 
                 |-- index.gsp
                 +-- WEB-INF
                      |
                      |-- web.groovy
```

This file can have the following methods

|Method|Description|
|:-----|:----------|
|void onApplicationStart(Application app)|Invoked when the application is initially started and on the first request after the file has been altered.  |
|void onApplicationEnd(Application app) | Invoked when the server is shutdown. |
|void onSessionStart(Session session) | Invoked immediately after a session is created|
|void onSessionEnd(Session session)| Invoked when a session expires, is invalidated or when an application is restarted.|

  * **onApplicationStart** and **onApplicationEnd** accept an app parameter which is a reference to the application (or ServletContext).
  * **onSessionStart** and **onSessionEnd** accept a reference to the session object in use.


Example - web.groovy:
```
class web {
        def onApplicationStart(app){
        }

        def onApplicationEnd(app){
        }

        def onSessionStart(session){
        }

        def onSessionEnd(session){
        }
}
```