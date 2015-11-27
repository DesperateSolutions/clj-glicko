var React = require('react');
var ReactDOM = require('react-dom');

var PlayerList = require('./playerList');

var app = React.createClass({
    render: function() {
        return (
            <div className="app">
                Hello, world! I am the app.
            </div>
        );
    }
});

ReactDOM.render(
    <PlayerList/>,
    document.getElementById('content')
);
