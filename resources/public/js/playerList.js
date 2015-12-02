var React = require('react');

var PlayerList = React.createClass({
    render: function() {
        return (
            <div className="commentList">
                <h1 className="header green-text">Liga</h1>
                Hello, world! I am a CommentList.
                <table className="striped">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Rating</th>
                    </tr>
                    </thead>
                    <tbody>
                        <Player/>
                        <Player/>
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
                <th>{this.props.name}</th>
                <th>{this.props.rating}</th>
            </tr>
        );
    }
});

module.exports = PlayerList