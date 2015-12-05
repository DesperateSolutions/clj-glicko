var React = require('react');
var ReactDOM = require('react-dom');
var PlayerStore = require('./stores/PlayerStore');
var PlayerActions = require('./actions/PlayerActions');
var PlayerList = require('./playerList');

function getLeagueState() {
    return {players : PlayerStore.getAll()};
}

var App = React.createClass({

    getInitialState: function () {
        return getLeagueState();
    },

    componentDidMount: function () {
        PlayerStore.addChangeListener(this._onChange);
    },

    componentWillMount: function() {
        PlayerActions.getAll();
    },

    componentWillUnmount: function () {
        PlayerStore.removeChangeListener(this._onChange);
    },

    render: function () {

        return (
            <div>
                <PlayerList players={this.state.players}/>
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
