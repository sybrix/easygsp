<%

request.path=''
request.homeCssClass=''
request.documentationCssClass=''
request.communityCssClass=''
reqsuest.downloadCssClass='current_page_item'

def showEasyGSPQuestion = false
def title = ''
        
%>
<%@ include file="header.gsp" %>


<div class="narrowcolumn" style="padding-top:20px">

        <div class="post">
                <div class="post-top">
                        <div class="post-title-download" >
                                <h2><a href="downloads.gsp" rel="bookmark" title="">Downloads</a></h2>
                        </div>
                </div>
                <div class="entry">
                        <table border="1" cellpadding="3" cellspacing="3" width="500px" align="top">
                                <tr>
                                        <td nowrap="true"valign="top" style="padding:5px 20px" width="1%">Version 0.1</td>
                                         <td nowrap="true"valign="top" style="padding:5px 20px" width="1%">10/13/2009</td>
                                        <td align="left">
                                                <a href="downloads/easygsp-0.1.zip">easygsp-0.1.zip</a><br/>
                                                <a href="downloads/easygsp-0.1.tar.gz">easygsp-0.1.tar.gz</a>
                                        </td>
                                </tr>
                        </table>
                </div>

        </div>

</div>

<br/> <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
<br/> <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
<%@ include file="footer.gsp" %>
