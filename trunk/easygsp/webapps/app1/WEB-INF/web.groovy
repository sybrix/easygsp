import util.*;

class web {
	def onApplicationStart(app){    
		System.out.println("starting application xxx")
                log 'log... app started'
                DAOUtil.populate(model.Profile.class)                  
                 
        }
	
	def onApplicationEnd(Object app){
		System.out.println("ending application")	
	}

	def onSessionStart(Object session){
		System.out.println("starting session app1")	
	}
	
	def onSessionEnd(Object session){
		System.out.println("ending session")		
	}
}