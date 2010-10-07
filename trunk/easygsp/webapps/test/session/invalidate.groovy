
response.setContentType("text/xml")

request.getSession(true)

def sessionId1 = session.id

session.invalidate()

def sessionId2 = session?.id

request.getSession(true)

def sessionId3 = session?.id


bind 'sessionId1', sessionId1
bind 'sessionId2', sessionId2
bind 'sessionId3', sessionId3
