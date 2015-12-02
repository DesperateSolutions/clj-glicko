var PlayerApi = {
    getAll: function(callback) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function() {
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200){
                callback(null, xmlHttp.responseText);
            }
        };
        var asynchronous = true;
        xmlHttp.open("GET", "/", asynchronous);
        xmlHttp.send(null);
    }
};

module.exports = PlayerApi;