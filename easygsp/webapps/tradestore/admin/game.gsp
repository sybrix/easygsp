<%
	import model.*
	import util.*                                                                   

       
%>
<html>
	<head>
                <title>New Game</title>
                <script type="text/javascript"  src="../js/jquery-1.3.2.min.js"></script>
                <style>
                        .checkBoxItemLabel {
                                padding-left:10px;
                        }
                        .dirty {
                                background-color:red;
                        }
                </style>
                <script>
                      
                        // When DOM loads, init the page.
                        \$( InitPage );

                        // Init the page.
                        function InitPage(){
                                var jInput = \$( "#gameSystemId" );

                                // Bind the onchange event of the inputs to flag
                                // the inputs as being "dirty".
                                jInput.change(
                                        function( objEvent ){

                                                var gameSystemId = document.getElementById("gameSystemId").value;
                                                var gameId = document.getElementById("gameId").value;
                                                window.location.href='game.gsp?gameSystemId=' + gameSystemId + "&gameId=" + gameId;
                                        }
                                );
                        }

                </script>
	</head>
	<body>
                <form name="game.gspx" method="POST">
                        <input type="hidden" name="gameId" id="gameId" value="$params.gameId"/>
                        <table align="center">
                                <tr>
                                        <td>Title:</td>
                                        <td><input type="text" name="title" value="$params.title"/></td>
                                </tr>
                                <tr>
                                        <td valign="top">Description:</td>
                                        <td><textarea name="description" rows="5" cols="40">$params.description</textarea></td>
                                </tr>
                                <tr>
                                        <td valign="top">Genre:</td>
                                        <td>

                                        <%
                                                comboBox('gameGenreId1', GameGenre.findAll([parentId:0,orderBy:'sortOrder']), "gameGenreId", "description",""," - Select Genre - ")
                                                //checkBoxList('gameSystemId', GameSystem.list([orderBy:'gameSystem']), "gameSystemId", "gameSystem",[1])
                                        %>
                                        </td>
                                </tr>
                                <tr>
                                        <td valign="top">Sub Genre:</td>
                                        <td>

                                        <%
                                                comboBox('gameGenreId2', GameGenre.findAll([parentId:1,orderBy:'sortOrder']), "gameGenreId", "description",""," - Select Genre - ")
                                                //checkBoxList('gameSystemId', GameSystem.list([orderBy:'gameSystem']), "gameSystemId", "gameSystem",[1])
                                        %>
                                        </td>
                                </tr>

                                <tr>
                                        <td colspan="2"> Select Platform: 
                                                <%
                                                        comboBox('gameSystemId', GameSystem.list([orderBy:'gameSystem']), "gameSystemId", "gameSystem","", " - Platforms -")
                                                %>
                                        </td>
                                </tr>
                                <tr>
                                        <td></td>
                                        <td ><input type="submit" name="save" value="Save"/></td>
                                </tr>
                        </table>


                </form>
	</body>
</html>

<%@include file="../util.gsp" %>




