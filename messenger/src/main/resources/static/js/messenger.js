'use strict';

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var stompClient = null;

var Messenger = function () {
    function Messenger() {
        _classCallCheck(this, Messenger);

        var self = this;

        this.messageList = [];
        this.deletedList = [];

        this.socket = new SockJS('/messenger');

        stompClient = Stomp.over(this.socket);

        stompClient.connect({}, function (frame) {
            console.log('messenger connected: ' + frame);

            stompClient.subscribe('/sms/outgoing', function (message) {
                self.recieve(message.body);
            });

        });

        this.me = 1; // completely arbitrary id
        this.them = 5; // and another one

        this.onRecieve = function (message) {
            return console.log('Recieved: ' + message.text);
        };
        this.onSend = function (message) {
            return console.log('Sent: ' + message.text);
        };
        this.onDelete = function (message) {
            return console.log('Deleted: ' + message.text);
        };
    }

    Messenger.prototype.send = function send() {
        var text = arguments.length <= 0 || arguments[0] === undefined ? '' : arguments[0];

        text = this.filter(text);

        if (this.validate(text)) {
            var message = {
                user: this.me,
                text: text,
                time: new Date().getTime()
            };

            this.messageList.push(message);

            this.onSend(message);
            stompClient.send("/io/sms/send", {}, text);
        }
    };

    Messenger.prototype.recieve = function recieve() {
        var text = arguments.length <= 0 || arguments[0] === undefined ? '' : arguments[0];

        console.log("receiving message: "+ text);
        if (this.validate(text)) {
            var message = {
                user: this.them,
                text: text,
                time: new Date().getTime()
            };

            this.messageList.push(message);
            console.log("receiving message: "+ text);

            this.onRecieve(message);
        }
    };

    Messenger.prototype.delete = function _delete(index) {
        index = index || this.messageLength - 1;

        var deleted = this.messageLength.pop();

        this.deletedList.push(deleted);
        this.onDelete(deleted);
    };

    Messenger.prototype.filter = function filter(input) {
        var output = input.replace('bad input', 'good output'); // such amazing filter there right?
        return output;
    };

    Messenger.prototype.validate = function validate(input) {
        return !!input.length; // an amazing example of validation I swear.
    };

    return Messenger;
}();