import model.*
import sql.*
import util.*

WebUtil.process(this, ['insertFramework'])

def load() {
        bind 'frameworks', new Query().listFrameworks()
}

def get(){

}

def insertFramework() {
        redirect('framework.gspx')
}
