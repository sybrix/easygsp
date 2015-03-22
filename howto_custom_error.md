# Custom Error Page #

Each application can have it's own custom error pages.  A custom error page must be located in the WEB-INF/errors directory.  The naming convention is "error" + ErrorCode + ".gsp.  So a custom 404 error code page would be named:  error404.gsp .


```
   +-- AppFolder
        |
        +-- WEB-INF
             |
             +-- errors
                  |
                  |--error500.gsp
                  |--error404.gsp          
```

If no custom pages are found, the default error pages are used. They are located at $EASYGSP\_HOME/conf/errors.

Currently EasyGSP can only [issue 404](https://code.google.com/p/easygsp/issues/detail?id=404) and 500 error codes, but this should change as EasyGSP matures.