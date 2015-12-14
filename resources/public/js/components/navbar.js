var React = require('react');

var Navbar = React.createClass({

    render: function() {
        return (
            <nav className="green">
                <div className="nav-wrapper container">
                    <a href="#" className="brand-logo">IFI Sjakk</a>
                </div>
            </nav>

        );
    }
});

module.exports = Navbar;