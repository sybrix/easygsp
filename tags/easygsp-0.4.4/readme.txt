The groovy-all.jar has been groovy recompiled :

groovy.lang.GroovyObject has been modified to so that it extends java.io.Serializable
groovy.lang.GroovyClassLoader.ClassCollector modified to add a SerialVersionUID to an groovy class that doesn't have one.

This is so that GroovyObjects can be set as session and app attributes.  Session and App attributes are managed by a caching
library that requires all objects be Serializable.