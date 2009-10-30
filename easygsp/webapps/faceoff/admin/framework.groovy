
import model.*
import util.*

WebUtil.process(this, ['save', 'delete', 'insert'])

def load(){

        bind('categories',FaceoffCategory.list([orderBy: 'category']))
}

def get(){
        def framework
        if (params.frameworkId != '')
                framework = FaceoffFramework.find([frameworkId: params.frameworkId])
        else
                framework =  new FaceoffFramework()

        ['f':framework]
}

def save(){
        log 'save invoked'
        def framework = FaceoffFramework.find([frameworkId: params.frameworkId])

        framework = WebUtil.populateBean(framework, params)

//        framework.framework = params.framework
//        framework.version = params.version
//        framework.url = params.url
//        framework.categoryId =  Integer.parseInt(params.categoryId)
//        framework.description = params.description
        framework.active = new Boolean(params.active ?: 'false')

        framework.save()

        bind 'f', framework

}

def insert(){

        def framework = new FaceoffFramework()

        framework.framework = params.framework
        framework.version = params.version
        framework.url = params.url
        framework.categoryId =  Integer.parseInt(params.categoryId)
        framework.description = params.description
        framework.active = new Boolean(params.active ?: 'false')
        framework.save()

        bind 'f', framework

        redirect 'frameworks.gspx'
}


def delete(){
        def framework = FaceoffFramework.find([frameworkId: params.frameworkId])
        framework.delete()

        redirect 'frameworks.gspx'
}