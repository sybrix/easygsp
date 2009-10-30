
import model.*;
import util.*

WebUtil.process(this, ['framework'])

def load(){
        ['categories': FaceoffCategory.list([orderBy: 'category'])]
}

def get(){
        def categoryId = new Integer(params.categoryId?:'1')
        def frameworks = FaceoffFramework.findAll([active:true, categoryId: categoryId, orderBy:'framework'])
        
        ['frameworks':frameworks,'categoryId': categoryId.toString()]
}

