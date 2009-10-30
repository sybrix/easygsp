package model

import java.sql.Timestamp

class FaceoffProfile {
	private def dynamicProperties = []
	static primaryKeys = ['profileId']

	Integer profileId
	String screenName
	String email
	Timestamp created
	String password

}