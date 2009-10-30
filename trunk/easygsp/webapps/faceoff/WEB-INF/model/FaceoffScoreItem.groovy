package model

class FaceoffScoreItem {
	private def dynamicProperties = []
	static primaryKeys = ['itemId']

	Integer itemId
	Integer categoryId
	String description
	String weightName
	Float weight
}