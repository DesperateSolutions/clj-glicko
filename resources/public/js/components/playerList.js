var React = require('react');

var PlayerList = React.createClass({

    render: function() {
        var playerNodes = this.props.players.map(function(player) {
            return (
                <Player player={player} key={player.name}/>
            )
        });

        return (
            <div>
                <h1 className="header green-text">Liga</h1>
                <table className="striped">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Rating</th>
                    </tr>
                    </thead>
                    <tbody>
                        {playerNodes}
                    </tbody>
                </table>
            </div>
        );
    }
});

var Player = React.createClass({
    render: function() {
        return (
            <tr>
                <th>{this.props.player.name}</th>
                <th>{this.props.player.rating}</th>
            </tr>
        );
    }
});

module.exports = PlayerList;