var React = require('react');
var ReactDOM = require('react-dom');
var PlayerStore = require('./stores/PlayerStore');

var PlayerList = require('./playerList');

function getLeagueState() {
    return {leagueTable : PlayerStore.getAll()};
}

var App = React.createClass({

    getInitialState: function() {
        console.log(getLeagueState());
        return getLeagueState();
    },

    render: function() {
        return (
            <div className="app">
                Hello, world! I am the app.
                <PlayerList/>
            </div>
        );
    }

});


ReactDOM.render(
    <App/>,
    document.getElementById('content')
);
