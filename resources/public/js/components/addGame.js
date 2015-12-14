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
            <div className="row">
                <div className="col s12 m6">
                    <div className="card green darken-1 add-player-card">
                        <div className="card-content white-text">
                            <span className="card-title">Add Game</span>
                        </div>
                        <div className="card-action">
                            <form>
                                <div className="input-field">
                                    <select className="browser-default" defaultValue="" name="white-id" onChange={this.onWhitePlayerChange}>
                                        <option value="" disabled>Select white player</option>
                                        {playerNodes}
                                    </select>
                                    <select className="browser-default" defaultValue="" name="white-id" onChange={this.onBlackPlayerChange}>
                                        <option value="" disabled>Select black player</option>
                                        {playerNodes}
                                    </select>
                                    <p>
                                        <input name="resultGroup" type="radio" id="whiteRadio" value="white" onChange={this.onWinnerChange}/>
                                        <label className="white-text" htmlFor="whiteRadio">White</label>
                                        <input name="resultGroup" type="radio" id="drawRadio" value="draw" onChange={this.onWinnerChange}/>
                                        <label className="white-text" htmlFor="drawRadio">Draw</label>
                                        <input name="resultGroup" type="radio" id="blackRadio" value="black" onChange={this.onWinnerChange}/>
                                        <label className="white-text"htmlFor="blackRadio">Black</label>
                                        <button type="button" type="button" className="right btn-large waves-effect waves-light" onClick={this.handleSubmit} >Add game</button>
                                    </p>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>

        );
    }
});

module.exports = AddGame;