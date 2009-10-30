
import java.text.SimpleDateFormat

request.path="../"

request.documentationCssClass='current_page_item'
request.homeCssClass=''
request.downloadCssClass=''
request.communityCssClass=''

bind 'title', 'Documentation'

if (params.id!=null)
        forward params.id + '.gspx'
else
        forward 'index.gspx'