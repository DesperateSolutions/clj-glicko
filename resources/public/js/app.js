$("form#addplayer").on("submit", function(e) {
    e.preventDefault();
    $.ajax({
        type: "POST",
        url: "/addplayer",
        data: $("form#addplayer").serialize(),
        success: function(data) {
            location.href = "/";
        },
        error: function() {
        },
        statusCode: {
            406: function(msg) {
                msg = JSON.parse(msg.responseJSON.error);
            }
        }
    });
});

$("form#addgame").on("submit", function(e) {
    e.preventDefault();
    $.ajax({
        type: "POST",
        url: "/addgame",
        data: $("form#addgame").serialize(),
        success: function(data) {
            location.href = "/";
        },
        error: function() {
        },
        statusCode: {
            406: function(msg) {
                msg = JSON.parse(msg.responseJSON.error);
            }
        }
    });
});