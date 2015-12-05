var PlayerApi = {
    getAll: function(callback) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function() {
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200){
                callback(null, JSON.parse(xmlHttp.responseText));
            }
        };
        var asynchronous = true;
        xmlHttp.open("GET", "/players", asynchronous);
        xmlHttp.send(null);
    }
};

module.exports = PlayerApi;