<%

        def today = new Date().toString()
        bind 'today', today

        forward 'view.gsp'
%>