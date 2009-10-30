package model

class GameGenre {
        private def dynamicProperties = []
        static primaryKeys = ['gameGenreId']
      
        Long gameGenreId
        String description
        Integer parentId
        Integer sortOrder
}
