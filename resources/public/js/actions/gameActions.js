var ApiUtils = require('../apiUtils/GameApi');
var AppDispatcher = require('../dispatcher/AppDispatcher');
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
    }
};

module.exports = GameActions;