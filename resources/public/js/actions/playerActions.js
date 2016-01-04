var AppDispatcher = require('../dispatcher/appDispatcher');
var PlayerConstants = require('../constants/playerConstants');
var ApiUtils = require('../apiUtils/playerApi');

var PlayerActions = {
    getAll : function() {
        ApiUtils.getAll(function (err, players) {
            if(err) {
                console.log(err);
            } else {
                AppDispatcher.dispatch({
                    actionType: PlayerConstants.PLAYERS_UPDATED,
                    players: players
                });
            }
        });
    },

    create: function (name) {
        ApiUtils.create({name : name}, function (err, player) {
        });
    }
};

module.exports = PlayerActions;