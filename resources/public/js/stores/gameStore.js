var AppDispatcher = require('../dispatcher/appDispatcher');
var EventEmitter = require('events').EventEmitter;
var GameConstants = require('../constants/gameConstants');
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _games = [];


var GameStore = assign({}, EventEmitter.prototype, {

    getAll: function() {
        return _games;
    },

    emitChange: function() {
        this.emit(CHANGE_EVENT);
    },

    addChangeListener: function(callback) {
        this.on(CHANGE_EVENT, callback);
    },

    removeChangeListener: function(callback) {
        this.removeListener(CHANGE_EVENT, callback);
    },

    dispatcherIndex: AppDispatcher.register(function(action) {
        switch(action.actionType) {
            case GameConstants.GAMES_UPDATED:
                _games = action.games;
                GameStore.emitChange();
                break;
        }

        return true;
    })

});

module.exports = GameStore;