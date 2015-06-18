
// Simple session test
request.getSession(true)

response.setContentType("text/xml")

if (!session.counter)
        session.counter = 0


session.counter = session.counter + 1


