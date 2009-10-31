The groovy-all.jar is groovy recompiled w/one small change:

groovy.lang.GroovyObject has been modified to so that it extends java.io.Serializable

This is so that GroovyObjects can be set as session and app attributes.  Session and App attributes are managed by a caching
library that requires all objects be Serializable.