<%


        def today = new Date().toString()
        bind 'today', today

        // you can forward to a template or a controller/script
        // to forward to a controller, the extension must be .groovy, when extension is .gsp or .gspx, the processing will be forwarded to a template
        forward 'example2.groovy'

%>