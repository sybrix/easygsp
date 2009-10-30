package model

class GameReview {
        private def dynamicProperties = []
        static primaryKeys = ['gameReviewId']
        
        Long gameReviewId
        Long gameId
        String comment
        Date created
        String username
}


