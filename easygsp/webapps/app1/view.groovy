
        import model.*

        doThis()

        def doThis(){
                Profile p = new Profile()
                request.getSession(true)
                session.myname="puddin tang"
                session.time="puddin tang" + p.class
                //redirect  'http://www.google.com'
        }
