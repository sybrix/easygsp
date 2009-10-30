
import model.*
import util.WebUtil


WebUtil.process(this, ['save', 'delete'])


def load(){
        [ 'categories':FaceoffCategory.list([orderBy: 'category'])]
}

def get(){
        if (params.itemId != null)
                bind 'item', FaceoffScoreItem.find([itemId: params.itemId])

}

def save(){
        log 'save invoked'
        def item = FaceoffScoreItem.find([itemId: params.itemId]);


        item.weightName = params.weightName
        item.description = params.description
        item.categoryId = toInt(params.categoryId)
        item.weight = new Double(params.weight)
        item.save()

        ['item': item]
}


def delete(){
        def item = FaceoffScoreItem.find([itemId: params.itemId])
        item.delete()

        redirect 'scoreItems.gspx'
}