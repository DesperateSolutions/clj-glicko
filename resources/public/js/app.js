var React = require('react');
var ReactDOM = require('react-dom');

var PlayerStore = require('./stores/PlayerStore');
var GameStore = require('./stores/gameStore');
var PlayerActions = require('./actions/PlayerActions');
var GameActions = require('./actions/gameActions');

var PlayerList = require('./components/playerList');
var CreatePlayer = require('./components/createPlayer');
var GameList = require('./components/gamesList');
var AddGame = require('./components/addGame');

function getLeagueState() {
    return {
        players : PlayerStore.getAll(),
        games : GameStore.getAll()
    };
}

var App = React.createClass({

    getInitialState: function () {
        return getLeagueState();
    },

    componentDidMount: function () {
        PlayerStore.addChangeListener(this._onChange);
        GameStore.addChangeListener(this._onChange);
    },

    componentWillMount: function() {
        PlayerActions.getAll();
        GameActions.getAll();
    },

    componentWillUnmount: function () {
        PlayerStore.removeChangeListener(this._onChange);
        GameStore.removeChangeListener(this._onChange);
    },

    render: function () {

        return (
            <div>
                <PlayerList players={this.state.players}/>
                <CreatePlayer/>
                <GameList games={this.state.games} />
                <AddGame players={this.state.players}/>
            </div>
        );
    },

    _onChange: function() {
        this.setState(getLeagueState());
    }

});


ReactDOM.render(
    <App/>,
    document.getElementById('content')
);
