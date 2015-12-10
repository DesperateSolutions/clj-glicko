var React = require('react');

var GamesList = React.createClass({

    render: function() {
        var gameNodes = this.props.games.map(function(game) {
            return (
                <Game game={game} key={game._id}/>
            )
        });

        return (
            <div>
                <h1 className="header green-text">Previous games</h1>
                <table className="striped">
                    <thead>
                    <tr>
                        <th>White</th>
                        <th>Black</th>
                        <th>Result</th>
                    </tr>
                    </thead>
                    <tbody>
                    {gameNodes}
                    </tbody>
                </table>
            </div>
        );
    }
});

var Game = React.createClass({
    render: function() {
        return (
            <tr>
                <th>{this.props.game.white}</th>
                <th>{this.props.game.black}</th>
                <th>{this.props.game.result}</th>
            </tr>
        );
    }
});

module.exports = GamesList;