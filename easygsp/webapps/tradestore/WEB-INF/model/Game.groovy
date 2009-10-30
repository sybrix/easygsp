package model

class Game {
        private def dynamicProperties = []
        static primaryKeys = ['gameId']
      
        Long gameId
        Long gameSystemId
        Long gameTitleId

        String smallImageUrl
        String largeImageUrl
        String mediumImageUrl

        Integer quantityInStock

        Double weight
        Double tradeinValue
        Double cost
        Double retailPrice
        Double buybackPrice
        Boolean active
        String upcCode
        Boolean acceptingTrades

        Date created
        Date lastModified
        Integer sortOrder
}
