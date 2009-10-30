
import model.*
import util.WebUtil


WebUtil.process(this, ['delete', 'insert'])

def load(){
        ['categories':FaceoffCategory.list([orderBy: 'category'])]
}

def get(){
        [ 'items': FaceoffScoreItem.findAll([orderBy: 'categoryId', categoryId: toInt(params.categoryId?:'1')])]
}


def insert(){

        def item = new FaceoffScoreItem()

        item.weightName = params.weightName
        item.description = params.description
        item.categoryId = toInt(params.categoryId)
        item.weight = new Double(params.weight)
        item.save()

        redirect 'scoreItems.gsp'
}


