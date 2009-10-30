import model.*
import util.WebUtil

WebUtil.process(this, ['save'])

def get() {
        request.categories = FaceoffCategory.list([orderBy: 'category'])
}


def save(){
       FaceoffCategory f  = new FaceoffCategory(category:params.category)
       f.save()
       redirect 'categories.gspx'
}