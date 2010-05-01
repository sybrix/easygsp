

// a controller, can be a simple a script.
// no class, no method, no nothing required

bind 'message', ''

if (request.method == 'POST'){
        def lastName = params.lastName
        def firstName = params.firstName
        saveData(lastName, firstName)
        bind 'message', 'Data successfully saved'
}



def saveData(val, val2){
        // save data
}