var React = require('react');

var PlayerActions = require('./actions/PlayerActions');

var CreatePlayer = React.createClass({

    handleChange: function(event) {
        this.setState({input: event.target.value});
    },

    handleSubmit: function() {
        PlayerActions.create(this.state.input);
    },

    render: function() {
        return (
            <div>
                <h5 className="header light">Add players</h5>
                <form>
                    <input type="text" placeholder="Player name" onChange={this.handleChange}/>
                    <button type="button" className="btn-large waves-effect waves-light green" onClick={this.handleSubmit} >Add player</button>
                </form>

            </div>
        );
    }
});

module.exports = CreatePlayer;