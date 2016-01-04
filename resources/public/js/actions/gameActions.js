var ApiUtils = require('../apiUtils/gameApi');
var AppDispatcher = require('../dispatcher/appDispatcher');
var GameConstants = require('../constants/gameConstants');

var GameActions = {

    getAll : function() {
        ApiUtils.getAll(function (err, games) {
            if(err) {
                console.log(err);
            } else {
                AppDispatcher.dispatch({
                    actionType: GameConstants.GAMES_UPDATED,
                    games: games
                });
            }
        });
    },

    create: function (whiteId, blackId, winner) {
        var result;
        if (winner == "white"){
            result = "1";
        } else if (winner == "black") {
            result = "-1";
        } else {
            result = "0";
        }

        ApiUtils.create({whiteId : whiteId, blackId : blackId, result : result}, function (err, player) {
        });
    },

    deleteGame: function(gameId) {
        ApiUtils.deleteGame(gameId, function (err) {
            if (err) {
                console.log(err);
            } else {
                AppDispatcher.dispatch({
                    actionType: GameConstants.GAME_DELETED,
                    gameId : gameId
                });
            }
        });
    }
};

module.exports = GameActions;