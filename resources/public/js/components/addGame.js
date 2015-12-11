var React = require('react');
var GameActions = require('./../actions/gameActions');

var AddGame = React.createClass({

    onWhitePlayerChange: function(event) {
        this.setState({selectedWhitePlayer : event.target.value});
    },

    onBlackPlayerChange: function(event) {
        this.setState({selectedBlackPlayer : event.target.value});
    },

    onWinnerChange: function(event) {
        this.setState({result : event.target.value});
    },

    handleSubmit: function() {
        GameActions.create(this.state.selectedWhitePlayer, this.state.selectedBlackPlayer, this.state.result);
    },

    render: function() {

        var playerNodes = this.props.players.map(function(player) {
            return (
                <option key={player._id} value={player._id}>{player.name}</option>
            )
        });

        return (
            <div>
                <h5 className="header light">Add Game</h5>
                <form>
                    <div className="input-field">
                        <select className="browser-default" defaultValue="" name="white-id" onChange={this.onWhitePlayerChange}>
                            <option value="" disabled>Select white player</option>
                            {playerNodes}
                        </select>
                        <p>
                            <input name="resultGroup" type="radio" id="whiteRadio" value="white" onChange={this.onWinnerChange}/>
                            <label htmlFor="whiteRadio">White</label>
                        </p>
                        <p>
                            <input name="resultGroup" type="radio" id="drawRadio" value="draw" onChange={this.onWinnerChange}/>
                            <label htmlFor="drawRadio">Draw</label>
                        </p>
                        <p>
                            <input name="resultGroup" type="radio" id="blackRadio" value="black" onChange={this.onWinnerChange}/>
                            <label htmlFor="blackRadio">Black</label>
                        </p>

                        <select className="browser-default" defaultValue="" name="white-id" onChange={this.onBlackPlayerChange}>
                            <option value="" disabled>Select black player</option>
                            {playerNodes}
                        </select>
                        <button type="button" type="button" className="btn-large waves-effect waves-light green" onClick={this.handleSubmit} >Add game</button>
                    </div>
                </form>

            </div>
        );
    }
});

module.exports = AddGame;