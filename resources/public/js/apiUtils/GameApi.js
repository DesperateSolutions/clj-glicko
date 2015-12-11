var $ = require('jquery');

var GameApi = {

    getAll: function(callback) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function() {
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200){
                callback(null, JSON.parse(xmlHttp.responseText));
            }
        };
        var asynchronous = true;
        xmlHttp.open("GET", "/games", asynchronous);
        xmlHttp.send(null);
    },

    create: function (game, callback) {
        $.ajax({
            type: "POST",
            url: "/addgame",
            data: game,
            success: function(data) {
                location.href = "/";
            },
            error: function(err) {
                console.log(err);
            },
            statusCode: {
                406: function(msg) {
                    msg = JSON.parse(msg.responseJSON.error);
                }
            }
        });
    },

    deleteGame: function (gameId, callback) {
        $.ajax({
            type: "DELETE",
            url: "/delete-game",
            data: {_id:  gameId},
            success: function(data) {
                callback(null);
            },
            error: function(err) {
                console.log(err);
            },
            statusCode: {
                406: function(msg) {
                    msg = JSON.parse(msg.responseJSON.error);
                }
            }
        });
    }
};

module.exports = GameApi;