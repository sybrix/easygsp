package model

import java.sql.Timestamp

class FaceoffFrameworkComment {
	private def dynamicProperties = []
	static primaryKeys = ['commentId']

	Integer commentId
	String comment
	Integer frameworkId
	Integer profileId
	Timestamp created
        String email
        String screenName

}