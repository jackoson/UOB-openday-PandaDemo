'use strict'
var net = require('net');
var events = require('events');
var util = require('util');


function PlayerServer() {
    var self = this;
    this.server = net.createServer(function(player) {
        player.on('data', function(data) {
            self.onInput(player, data);
        });
    });
}
util.inherits(PlayerServer, events.EventEmitter);


PlayerServer.prototype.onInput = function(player, data) {
    var parsedData = JSON.parse(data);
    var type = parsedData['type'];
    if (type == 'REGISTER') {
        var studentId = parsedData['student_id'];
        this.emit('register', player, studentId);
    } else if (type == 'MOVE') {
        this.emit('move', player, parsedData);
    }
}


PlayerServer.prototype.listen = function(port) {
    this.server.listen(port);
}


PlayerServer.prototype.close = function() {
    this.server.close();
}


module.exports = PlayerServer;