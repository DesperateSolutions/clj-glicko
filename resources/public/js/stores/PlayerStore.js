var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var PlayerConstants = require('../constants/PlayerConstants');
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _players = {list :[{name: "John", rating: 1337}]};


var PlayerStore = assign({}, EventEmitter.prototype, {

    getAll: function() {
        return _players;
    },

    emitChange: function() {
        this.emit(CHANGE_EVENT);
    },

    /**
     * @param {function} callback
     */
    addChangeListener: function(callback) {
        this.on(CHANGE_EVENT, callback);
    },

    /**
     * @param {function} callback
     */
    removeChangeListener: function(callback) {
        this.removeListener(CHANGE_EVENT, callback);
    },

    dispatcherIndex: AppDispatcher.register(function(payload) {
        var action = payload.action;
        var text;

        switch(action.actionType) {
            case PlayerConstants.PLAYER_CREATE:
                text = action.text.trim();
                if (text !== '') {
                    //create(text);
                    PlayerStore.emitChange();
                }
                break;

            case PlayerConstants.TODO_DESTROY:
                //destroy(action.id);
                PlayerStore.emitChange();
                break;
        }

        return true; // No errors. Needed by promise in Dispatcher.
    })

});

module.exports = PlayerStore;