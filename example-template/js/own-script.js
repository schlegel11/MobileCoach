"use strict";

// Execute when page is loaded succesful
$(function () {
    log("Ready");

    $("#REST-status").html("Requesting values...");
    //restTest("variable/read/Test");
    //restTest("variable/readArray/Test,participantName");
    //restTest("variable/write/systemDayOfMonth", "Hallo Welt! 👍");
    //restTest("variable/write/Tedddst", "Hallo Welt! 👍");
    //restTest("variable/readGroupArray/Test");
    //restTest("variable/readInterventionArray/Test");
    restTest("variable/calculateGroupAverage/Test");
    restTest("variable/calculateInterventionAverage/Test");
});

// Helpers
var log = function (value) {
    if (config.debug) {
        console.log(value);
    }
};

// Own functions
var restTest = function (command, postData) {

    $.ajax({
        type: postData == null ? "GET" : "POST",
        data: postData,
        dataType: postData == null ? "json" : "text/plain",
        contentType: postData == null ? "application/json; charset=UTF-8" : "text/plain; charset=UTF-8",
        beforeSend: function (request) {
            request.setRequestHeader("token", config.token);
        },
        url: config.rest + command,
        success: function (data) {
            $("#REST-status").html($("#REST-status").html() + "<br/>" + command + " --> SUCCESS: " + JSON.stringify(data));
            log(data);
        },
        error: function (xhr, exception) {
            $("#REST-status").html($("#REST-status").html() + "<br/>" + command + " --> " + xhr.status + " (" + xhr.statusText + "): " + xhr.responseText);
        }
    });
};