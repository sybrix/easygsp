import model.*
import util.WebUtil


WebUtil.process(this, ['update','delete'])

def get(){
        ['c': FaceoffCategory.find([categoryId: params.categoryId])]
}

def update(){
        FaceoffCategory category = FaceoffCategory.find([categoryId: params.categoryId])
        category.category = params.category
        category.save() 
        
        redirect 'category.gspx?categoryId=' + params.categoryId
}

def delete(){
        FaceoffCategory category = FaceoffCategory.find([categoryId: params.categoryId])
        category.delete()

        redirect 'categories.gspx'
}