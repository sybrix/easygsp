# Adding Jars to the ClassPath #


To add new jars, add them to the $EASYGSP\_HOME/lib folder

```
 +-- $EASYGSP_HOME
       |
       +-- lib
            |
            |--- jdom.jar
            |--- commons-fileupload-1.2.1.jar
            |--- ADD NEW JAR HERE

```

If you need to add jars to your application it has to be done at the server level.  While applications each have their own classloader, they don't have the ability to load their own jars.  So all applications running under a EasyGSP instance share all of the same jars, that are inherited from their parent classloader.

This may sound odd to Java developers, but it's really not unusual.  Let's say you were running your PHP shopping cart on a hosted server and  you decided you wanted to move to something like Magneto, but also noticed your hosted server did not have mcrypt installed.  Well, you wouldn't be able to install it yourself; either the hosting provider would have to install it or you'd have to switch providers.

The point is, it's not unusual to not have full access to install whatever you want in a shared environment.  It's assumed and hoped that EasyGSP will be used in shared environments, hence the ability to add jars to the server is limited to who controls the server.