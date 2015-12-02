var AppDispatcher = require('../dispatcher/AppDispatcher');
var PlayerConstants = require('../constants/PlayerConstants');
var ApiUtils = require('../apiUtils/PlayerApi');

var PlayerActions = {
    getAll : function() {
        ApiUtils.getAll(function (err, players) {
            if(err) {
                console.log(err);
            } else {
                console.log(players);
                AppDispatcher.dispatch({
                    actionType: PlayerActions.PLAYERS_UPDATED,
                    players: players
                });
            }
        });
    }
};

module.exports = PlayerActions;