<% 
	import model.*
	import util.*

         if (request.method == "POST"){
                 GameTitle gameTitle = new GameTitle(title:params.title, description:params.description, gameGenreId1:new Integer(params.gameGenreId1), created:new Date(), lastModified:new Date())
                 gameTitle.save()

                 gameTitle = GameTitle.find([title:params.title])
                 redirect("game.gsp?gameId=$gameTitle.gameTitleId")
        }
%>
<html>
	<head>
                <title>New Game Title</title>
                <style>
                        .checkBoxItemLabel {
                                padding-left:10px;       
                        }
                </style>
	</head>
	<body>                                                        
                <form name="newGame.gspx" method="POST">
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
                                                comboBox('gameGenreId1', GameGenre.findAll([parentId:0L,orderBy:'sortOrder']), "gameGenreId", "description",''," - Select Genre - ")
                                        %>
                                        </td>
                                </tr>

                                <tr>
                                        <td></td>
                                        <td ><input type="submit" name="save" value="Save"/></td>
                                </tr>
                        </table>
                        <table border="1">
                                <tr>
                                        <td>
                                                <% def game = new Game()%>
                                                Quantity: <input type="text" name="quantityInStock "  id="quantityInStock" size="2" maxlength="2" value="$game.quantityInStock">
                                                Weight: <input type="text" name="weight"  id="weight" size="4" maxlength="5"  value="$game.weight"> lbs oz

                                                Cost: <input type="text" name="cost"  id="cost" size="5" maxlength="5" value="$game.cost">
                                                Retail Value: <input type="text" name="retailValue"  id="retailValue" size="5" maxlength="5" value="$game.retailValue">
                                                Trade In Value: <input type="text" name="tradeInValue"  id="tradeInValue" size="5" maxlength="5" value="$game.tradeInValue">
                                                Buy Back Price: <input type="text" name="buybackPrice"  id="buyBackPrice" size="5" maxlength="5" value="$game.buybackPrice">
                                                Accepting Trades: <input type="checkbox" name="acceptingTrades"  id="acceptingTrades" value="1" ${game.acceptingTrades?'checked="checked"':''}>
                                        </td>
                                </tr>
                        </table>

                </form>
	</body>
</html>


<%@include file="../util.gsp" %>

       


