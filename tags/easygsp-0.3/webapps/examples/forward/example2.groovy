


def tomorrow = new Date().toString()
bind 'tomorrow', tomorrow + 1

// this controller(example.groovy) will auto forward to example.gspx, since no forward() was explicitly issued