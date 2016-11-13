
var BuildHTML = function () {
    function BuildHTML() {
        _classCallCheck(this, BuildHTML);

        this.messageWrapper = 'message-wrapper';
        this.circleWrapper = 'circle-wrapper';
        this.textWrapper = 'text-wrapper';

        this.meClass = 'me';
        this.themClass = 'them';
    }

    BuildHTML.prototype._build = function _build(text, who) {
        return '<div class="' + this.messageWrapper + ' ' + this[who + 'Class'] + '">\n              <div class="' + this.circleWrapper + ' animated bounceIn"></div>\n              <div class="' + this.textWrapper + '">...</div>\n            </div>';
    };

    BuildHTML.prototype.me = function me(text) {
        return this._build(text, 'me');
    };

    BuildHTML.prototype.them = function them(text) {
        return this._build(text, 'them');
    };

    return BuildHTML;
}();

$(document).ready(function () {
    var messenger = new Messenger();
    var buildHTML = new BuildHTML();

    var $input = $('#input');
    var $send = $('#send');
    var $content = $('#content');
    var $inner = $('#inner');

    function safeText(text) {
        $content.find('.message-wrapper').last().find('.text-wrapper').text(text).addClass('animated fadeIn');
    }

    function scrollBottom() {
        $($inner).animate({
            scrollTop: $($content).offset().top + $($content).outerHeight(true)
        }, {
            queue: false,
            duration: 'ease'
        });
    }

    function buildSent(message) {
        console.log('sending: ', message.text);

        $content.append(buildHTML.me(JSON.parse(message.text).body));
        safeText(JSON.parse(message.text).body);

        scrollBottom();
    }

    function buildRecieved(message) {
        console.log('recieving: ', message.text);

        $content.append(buildHTML.them(message.text));
        safeText(JSON.parse(message.text).body);

        scrollBottom();
    }

    function sendMessage() {
        var body = $input.val();

        if(body.trim().length > 0) {

            var from = $input.attr("data-from");
            var to = $input.attr("data-to");

            messenger.send(JSON.stringify({'from': from, to: to, 'body': body}));

            $input.val('');
            $input.focus();
        }
    }

    messenger.onSend = buildSent;
    messenger.onRecieve = buildRecieved;

    $input.focus();

    $send.on('click', function (e) {
        sendMessage();
    });

    $input.on('keydown', function (e) {
        var key = e.which || e.keyCode;

        if (key === 13) {
            // enter key
            e.preventDefault();

            sendMessage();
        }
    });
});