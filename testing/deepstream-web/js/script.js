var debug = true;
var deepstreamURL = "ws://localhost:6020/deepstream";
var user;
var secret;
var role = "participant";
var interventionPattern = "DS01";
var interventionPassword = "123";
var timestamp = 0;

var userName = "";

var restURL = "http://localhost:8080/MC/api/v02/";
var restUser = "ds:" + user;
var restToken = null;

var client;

var log = function(message) {
    if (debug)
        console.log(message);
};

var register = function() {
    log("Registering...");
    restCall("deepstream/register", { "nickname": userName, "role": role, "intervention-pattern": interventionPattern, "intervention-password": interventionPassword }, init);
}

var init = function() {
    log("Connecting...");
    client = deepstream(deepstreamURL).login({
            "user": user,
            "secret": secret,
            "role": role,
            "intervention-password": interventionPassword
        },
        function(success, result) {
            if (success) {
                log("Connected.");

                showSimulator();
                
                client.event.subscribe("message-update/" + user, function(message) {
                    log("Message " + message.id + ":");
                    log(message);
                    if (message.status == "SENT_BY_SYSTEM") {
                        addServerMessage(message.message);
                    };
                });
            } else {
                log("Could not connect.")
            }
        });
};

var send = function(message, date) {
    var timestamp = (date).getTime();
    log("Sending message \"" + message + "\" with timestamp " +
        timestamp + "...");

    client.rpc.make("user-message", {
        "user": user,
        "message": message,
        "timestamp": timestamp
    }, function(err, result) {
        log("Sending result:");
        log(result);
    });
};

var restCall = function(command, postData = null, successCallback = null) {
    log(postData);
    log(restURL + command);
    $.ajax({
        type: postData == null ? "GET" : "POST",
        data: postData == null ? null: JSON.stringify(postData),
        dataType: "json",
        contentType: "application/json; charset=UTF-8",
        url: restURL + command,
        success: function(data) {
            log("SUCCESS: " + command);
            log("Returned: " + JSON.stringify(data, null, 2));
            user = data.user;
            secret = data.secret;
            if (successCallback) {
                successCallback();
            }
        },
        error: function(xhr, exception) {
            log(xhr.statusCode());
            log("ERROR: " + command);
        }
    });
};

function subscribe() {
    if ($('form.subscription input[type="text"]').val()) {
        userName = $('form.subscription input[type="text"]').val();
        $("span.title").text("Web Simulator for " + userName);
        $("div.user-selection").hide();
        register();
    } else {
        alert("Choose a nickname, please!")
    }
}

$(document).ready(function() {
    //init();
});
