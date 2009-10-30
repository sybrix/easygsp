package model

class GameTitle {
        private def dynamicProperties = []
        static primaryKeys = ['gameTitleId']


        Long gameTitleId
        String title
        String description
        Integer gameGenreId1
        Integer gameGenreId2
        Date created
        Date lastModified = new Date()

}
