

class web {
	def onApplicationStart(app){
		System.out.println("starting application app2")
                new myclass().sayHello()
        }
	
	def onApplicationEnd(Object app){
		System.out.println("ending application")	
	}

	def onSessionStart(Object session){
		System.out.println("starting session")	
	}
	
	def onSessionEnd(Object session){
		System.out.println("ending session")		
	}
}