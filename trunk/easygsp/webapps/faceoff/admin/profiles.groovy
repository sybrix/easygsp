import model.*
import util.WebUtil


WebUtil.process(this, ['save'])

def get() {
        ['p': FaceoffProfile.list([orderBy: 'screenName'])]
}

def save(){
        
       redirect 'profiles.gspx'
}