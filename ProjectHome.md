EasyGSP allows you to create dynamic web pages using the Groovy programming language without the need for a typical java app server. Instead, EasyGSP communicates with HTTP servers like Lighttpd or Apache via the SCGI protocol. Developing a web page with EasyGSP is very similar to developing a web page in other scripting languages such as PHP, Python, ASP or Ruby.

> ## [Download and Get Started](InstallEasyGSP.md) ##

**What is the point of this project?**

Read these articles:
  * [http://java.dzone.com/news/hosting-java-web-applications-](http://java.dzone.com/news/hosting-java-web-applications-)
  * [http://www.dzone.com/links/r/why\_are\_there\_so\_few\_apps\_being\_built\_with\_jsp.html](http://www.dzone.com/links/r/why_are_there_so_few_apps_being_built_with_jsp.html)


It's notoriously difficult to get started with java web development.   It's also notoriously expensive to host a java web application since you very often require a dedicated servlet container.    This project aims to create a lightweight alternative to traditional Java servlet containers for doing quick and easy script based web development. Just like ruby, php and python can be used in your existing LAMP stack,  EasyGSP makes it just as easy to use Groovy.


**How is this different than running Tomcat behind Apache or LightTPD?**

It's different because EasyGSP is not an HTTP server or servlet container like Tomcat or Jetty.  It's a runtime environment for processing groovy scripts.  It's not a servlet container and doesn't have the overhead of a servlet container.   While EasyGSP does use the Servlet API, because it's familiar, it does not fully implement it and has no aspirations to be a servlet container in the traditional sense.

**Why not just use Grails?**

If you have a dedicated server, this would not be a bad choice.  Grails is a great framework, but it's a framework built on existing Servlet API implementations and that means your application and development effort will suffer from the same problems that most java web application do: excessive memory consumption, massive war files, general complexity and expensive hosting.  Because of the underlying products used in Grails, it's especially memory hungry and produces a +10MB war file regardless of the how much code you write for your application.

If you have a dedicated server, then Grails is a good choice.
If you've wanted the power of Java but in a simple scripting environment like that of other languages, EasyGSP might be what you've been looking for.

**What about the Google App Engine (GAE)?**

Google's App Engine for Java is pretty sweet, plus it's essentially free. It does solve the main problem of Java web hosting: price. If price was the only hurdle that kept you from building the next big webapp in Java, then the GAE is for you.  But if you believe there are problems beyond cost, like simplicity and productivity, give EasyGSP a try.

Similar projects:
  1. http://code.google.com/p/mod-groovy/
  1. http://code.google.com/p/groovy-lamp/
  1. http://gaelyk.appspot.com/  (not really similar but is in the Groovy without Grails spirit)


## Some key Features ##
  1. GZip Compression
  1. Internationalization
  1. Template Inheritance
  1. Clustering Support
  1. Simple Script Based Controllers