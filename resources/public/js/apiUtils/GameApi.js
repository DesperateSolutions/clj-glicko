var $ = require('jquery');

var GameApi = {
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
    }
};

module.exports = GameApi;