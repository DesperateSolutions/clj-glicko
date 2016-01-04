var React = require('react');

var PlayerActions = require('./../actions/playerActions');

var CreatePlayer = React.createClass({

    handleChange: function(event) {
        this.setState({input: event.target.value});
    },

    handleSubmit: function() {
        PlayerActions.create(this.state.input);
    },

    render: function() {
        return (
            <div className="row">
                <div className="col s12 m6">
                    <div className="card green darken-1">
                        <div className="card-content white-text">
                            <span className="card-title">Add Player</span>
                        </div>
                        <div className="card-action">
                            <form>
                                <div className="row">
                                    <div className="col s8">
                                        <input className="white-text" type="text" placeholder="Player name" onChange={this.handleChange}/>
                                    </div>
                                    <div className="col s4">
                                        <button type="button" className="btn-large waves-effect waves-light right" onClick={this.handleSubmit} >Add</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});

module.exports = CreatePlayer;