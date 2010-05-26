class web {
	        def onApplicationStart(app){
	        }

	        def onApplicationEnd(app){

	        }
                       
	        def onSessionStart(session){
	                session.uniqueId =  Math.abs(new Random().nextInt() % 100000) + 1
	        }

	        def onSessionEnd(session){

	       }
}