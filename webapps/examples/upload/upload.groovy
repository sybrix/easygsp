import org.apache.commons.fileupload.FileItem
import java.text.SimpleDateFormat

def uploadLocation = ''
def uploadSuccessful = false


// if there are no uploads, the map is empty
request.uploads.each { // uploads is a map
	FileItem item = fileItem.value
	File uploadedFile = new File(application.appPath + File.separator + fileItem.name)
	item.write(uploadedFile); // save file to disk
	
	uploadSuccessful = true
	bind 'fileName', fileItem.name
	
	def sdf = new SimpleDateFormat('MMMM dd, yyyy HH:mm')
	bind 'timeStamp', sdf.format(new Date())
	
	// when the script is done, control is automtically forwarded to the view
}

bind 'uploadSuccessful', uploadSuccessful