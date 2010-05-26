class web {
	        def onApplicationStart(app){
                        def propFile = loadPropertiesFile('db.properties')  
                        addProperties(app,propFile)
	        }

	        def onApplicationEnd(app){              
	        }

	        def onSessionStart(session){
	        }

	        def onSessionEnd(session){
	       }
}                  