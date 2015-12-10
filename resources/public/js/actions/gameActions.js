var ApiUtils = require('../apiUtils/GameApi');

var GameActions = {

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