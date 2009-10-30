import model.*
import util.WebUtil
import sql.Query

import java.sql.Timestamp

WebUtil.process(this, ['saveComment'])

def load(){

}

def get(){
        request.getSession(true)
        def title = new Date()
        def framework, comments
        
        try {
                if (params.frameworkId != null) {

                        framework = FaceoffFramework.find([frameworkId: params.frameworkId])
                        comments = FaceoffFrameworkComment.findAll([frameworkId: params.frameworkId, orderBy: 'created DESC'])
                } else
                        framework = new FaceoffFramework()

        }catch(Exception e){
                log 'framework.get failed', e
        }
        
        ['title':title, 'framework':framework, 'comments': comments]
}

def saveComment(){
        FaceoffFrameworkComment comment = new FaceoffFrameworkComment([comment: params.comment, frameworkId: toInt(params.frameworkId), profileId: session.profileId, created: new Timestamp(System.currentTimeMillis()), screenName: params.screenName])
        comment.save()
        
        redirect 'framework.gspx?frameworkId=' + params.frameworkId
}

