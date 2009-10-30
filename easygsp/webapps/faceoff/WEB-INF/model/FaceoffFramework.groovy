package model

class  FaceoffFramework {
        private def dynamicProperties = []
        static primaryKeys = ['frameworkId']

        Integer frameworkId
        String framework
        String version
        String url
        String description
        String score
        Integer categoryId
        Boolean active = false

}