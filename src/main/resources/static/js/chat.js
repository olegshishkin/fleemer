'use strict';

var timeOptions = {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
};
var currentUserNickname = $('#current-user-nickname').text();
var currentUserId = $('#current-user-id').text();
var notificationSound = new Audio($('#message-receive-notification').attr('href'));
var locale = $('#locale').text();
var onConnectedCallbackMethod = null;
var onErrorCallbackMethod = null;
var socket = null;
var stompClient = null;
var timerId = null;

// Connection method with auto reconnection facility
function connect(onConnectedCallback, onErrorCallback) {
    onConnectedCallbackMethod = onConnectedCallback;
    onErrorCallbackMethod = onErrorCallback;
    socket = new SockJS('/chat/handshake');
    stompClient = Stomp.over(socket);
    var headers = {
        'activemq.prefetchSize': 1
    };
    stompClient.connect(headers, onConnectedCallback, onErrorCallback);
    clearInterval(timerId);
    socket.onclose = function() {
        socket = null;
        timerId = setInterval(function() {
            connect(onConnectedCallbackMethod, onErrorCallbackMethod);
        }, 2000);
    };
}

function usersStatesUpdate() {
    var users = $('#chat-destination').find('[data-target]').map(function () {
        var id = $(this).attr('id');
        return id.substring(id.lastIndexOf('-') + 1);
    }).get();
    $.ajax({
        method:'POST',
        url: '/chat/statesUpdate',
        data: JSON.stringify(users),
        contentType: 'application/json',
        success: function (result) {
            $.each(result, function (id, isOnline) {
                var chatAvatar = $('#chat-' + id).find('.medium-image');
                var chatBodyAvatars = $('#chat-body-' + id).find('.medium-image');
                paintAvatars(chatAvatar, isOnline);
                paintAvatars(chatBodyAvatars, isOnline);
            });
        }
    });
}

function paintAvatars(elements, isOnline) {
    $(elements).each(function () {
        if (isOnline) {
            $(this).addClass('active');
        } else {
            $(this).removeClass('active');
        }
    });
}

function setSearchUsersAutocompleteWidget() {
    $('input[name="search"]').autocomplete({
        source: function (request, response) {
            $.post(
                '/chat/search',
                { nickname: request.term },
                function (result) {
                    var array = [];
                    $.each(result, function (nickname, id) {
                        array.push({label: nickname, value: id})
                    });
                    response(array);
                }
            );
        },
        select: function (event, ui) {
            var item = ui.item;
            addChat(item.label, item.value);
            this.blur();
            this.val('');
        }
    });
}

//********************************************************************************************************************//
//                                                                                                                    //
//                                      Message notification for all pages                                            //
//                                                                                                                    //
//********************************************************************************************************************//

function onNotificationConnected() {
    stompClient.subscribe('/queue/chat/notification/' + currentUserId, onQueueNotificationReceived);
    stompClient.subscribe('/topic/chat/notification/' + currentUserId, onTopicNotificationReceived);
}

function onNotificationConnectionError() {
    console.log('New messages notification don\'t possible: connection to message server hasn\'t established.');
}

function onQueueNotificationReceived(payload) {
    if (!verifyPayload(payload)) {
        console.log('Payload isn\'t correct: ' + payload);
        return;
    }
    stompClient.send('/app/notify/all', {}, payload.body);
}

function onTopicNotificationReceived(payload) {
    if (!verifyPayload(payload)) {
        console.log('Payload isn\'t correct: ' + payload);
        return;
    }
    iziToast.settings({
        resetOnHover: true,
        close: false,
        closeOnClick: true,
        displayMode: 2,
        drag: false,
        timeout: 60000,
        icon: 'material-icons'
    });
    iziToast.success({
        iconUrl: $('#chat-notification-icon').text(),
        title: JSON.parse(payload.body),
        message: $('#chat-notification-info').text()
    });
}

//********************************************************************************************************************//
//                                                                                                                    //
//                                                      Chat                                                          //
//                                                                                                                    //
//********************************************************************************************************************//

function onChatConnected() {
    $('#server-connection').attr('hidden', true);
    $('#loader-container').attr('hidden', true);
    $('.panel').removeAttr('hidden');
    stompClient.subscribe('/queue/chat/' + currentUserId, onQueueChatMessageReceived);
    stompClient.subscribe('/topic/chat/' + currentUserId, onTopicChatMessageReceived);
}

function onChatConnectionError(frame) {
    $('#loader-container').attr('hidden', true);
    var errorMsg = $('#connection-error').text();
    $('#server-connection p').addClass('text-danger').html(errorMsg + '<br>' + frame);
}

function onQueueChatMessageReceived(payload) {
    if (!verifyPayload(payload)) {
        console.log('Payload isn\'t correct: ' + payload);
        return;
    }
    stompClient.send('/app/send/all', {}, payload.body);
}

function onTopicChatMessageReceived(payload) {
    if (!verifyPayload(payload)) {
        console.log('Payload isn\'t correct: ' + payload);
        return;
    }
    var message = JSON.parse(payload.body);
    var senderId = message.senderId;
    var senderNickname = message.senderNickname;
    var chatElem = $('#chat-' + senderId);
    if (chatElem.length === 0) {
        addChat('Nickname' + senderId, senderId);
    }
    if (!chatElem.hasClass('active')) {
        chatElem.addClass('noreaded');
    }
    var elem = $('#foreign-message').clone(true).removeAttr('id');
    var time = new Date(message.time);
    time.setHours(time.getHours() - time.getTimezoneOffset() / 60); // cast to local time
    var avatarClass = $('#chat-' + senderId).find('.medium-image').hasClass('active') ? 'active' : null;
    addChatMessage(elem, senderId, senderId, time, message.content, avatarClass);
    $('#chat-' + senderId).find('h3.name').text(senderNickname);
    $('#chat-body-' + senderId).find('.chat-body h4').text(senderNickname);
    notificationSound.play();
}

function verifyPayload(payload) {
    var body = payload.body;
    return body !== '' && body !== null;
}

function addChat(nickname, id) {
    var chatsList = $('#chat-destination');
    var chatElem = $(chatsList).find('#chat-' + id);
    if (chatElem.length !== 0) {
        alert($('#contact-existence-error').text());
        return;
    }
    addChatTabToList(chatElem, nickname, id, chatsList);
    usersStatesUpdate();
    addChatBody(id, $('#chat-body-destination'));
}

function addChatTabToList(chatElem, nickname, id, chatsList) {
    chatElem = $('#chat').clone(true).attr('id', 'chat-' + id).attr('data-target', '#chat-body-' + id);
    $(chatElem).find('h3.name').text(nickname);
    $(chatElem).find('.fa-trash-o').click(function () {
        $('#chat-body-' + id).remove();
        $('#chat-' + id).remove();
    });
    chatElem.click(function () {
        $(this).removeClass('noreaded');
        setTimeout(
            function () {
                var chatBody = $('#chat-body-' + id);
                $(chatBody).find('.chat-body').scrollTo('max', 50);
                $(chatBody).find('.chat-footer textarea').focus();
            }, 1
        )
    });
    chatsList.append(chatElem);
}

function addChatBody(id, chatBodyDestination) {
    var chatBodyElem = $('#chat-body').clone(true).attr('id', 'chat-body-' + id);
    $(chatBodyElem).find('button').click(function () {
        sendMessage(id);
    });
    chatBodyDestination.append(chatBodyElem);
    // Show chat-body if only one chat is existed
    if ($('#chat-destination').find('.info-combo').length === 1) {
        $('#chat-' + id).addClass('active');
        $('#chat-body-' + id).addClass('active');
    }
}

function sendMessage(receiverId) {
    var textInput = $('#chat-body-' + receiverId).find('textarea');
    if (textInput.val() === '') {
        return;
    }
    var payload = {
        senderId: currentUserId,
        senderNickname: null,
        receiverId: receiverId,
        time: new Date(),
        content: textInput.val()
    };
    var header = {
        'persistent': 'true'
    };
    var jsonPayload = JSON.stringify(payload);
    stompClient.send('/app/send/one', header, jsonPayload);
    stompClient.send('/app/notify/one', header, jsonPayload);
    textInput.val('');
    var elem = $('#my-message').clone(true).removeAttr('id');
    addChatMessage(elem, receiverId, currentUserNickname, payload.time, payload.content, null);
    textInput.focus();
}

function addChatMessage(elem, id, subject, time, content, avatarClass) {
    $(elem).find('h4').text(subject);
    $(elem).find('h5').text(time.toLocaleString(locale, timeOptions));
    $(elem).find('.message-text').html(content.replace(/\n/g, '<br>'));
    if (avatarClass !== 'undefined' && avatarClass !== null) {
        $(elem).find('.fa-user').addClass(avatarClass);
    }
    var chatBody = $('#chat-body-' + id).find('.chat-body');
    chatBody.append(elem);
    $(chatBody).scrollTo('max', 50);
}
