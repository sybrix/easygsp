import model.*
import util.WebUtil


WebUtil.process(this, ['update', 'delete'])

def get() {
        ['p': FaceoffProfile.find([profileId: params.profileId])]
}


def update() {
        FaceoffProfile profile = FaceoffProfile.find([profileId: params.profileId])
        log 'here'
        profile.email = params.email
        log 'here 2'
        profile.screenName = params.screenName
        log 'here 3'
        if (params.password.size() > 0) {
                profile.password = params.password
        }

        profile.save()

        redirect 'profile.gspx?profileId=' + params.profileId
}


def delete() {
        FaceoffProfile profile = FaceoffProfile.find([profileId: params.profileId])
        profile.delete()

        redirect 'profiles.gspx'
}