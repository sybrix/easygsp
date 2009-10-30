package model

class GameCategory {
        private def dynamicProperties = []
        static primaryKeys = ['gameCategoryId']
      
        Long gameCategoryId
        String description
}
