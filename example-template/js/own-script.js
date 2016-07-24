"use strict";

// Execute when page is loaded succesful
$(function () {
    log("Ready");

    createListeners();
    $("#REST-status").html("Requesting values...");
    //restTest("variable/read/Test");
    //restTest("variable/readMany/Test,participantName");
    //restTest("variable/write/systemDayOfMonth", "Hallo Welt! 👍");
    //restTest("variable/write/TestString", "ABC123");

    //restTest("variable/readGroupArray/Test");
    //restTest("variable/readInterventionArray/Test");

    //restTest("variable/calculateGroupAverage/Test");
    //restTest("variable/calculateInterventionAverage/Test");

    //restTest("variable/readGroupArrayMany/Test,TestString");
    //restTest("variable/readInterventionArrayMany/Test,TestString");
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
            $("#REST-status").html($("#REST-status").html() + "<br/>" + command + " --> SUCCESS: " + JSON.stringify(data, null, 2));
            log(data);
        },
        error: function (xhr, exception) {
            $("#REST-status").html($("#REST-status").html() + "<br/>" + command + " --> " + xhr.status + " (" + xhr.statusText + "): " + xhr.responseText);
        }
    });
};

var createListeners = function () {
    $("#image-upload-button").click(function () {
        var formData = new FormData($("#image-upload")[0]);
        log(formData);
        $.ajax({
            type: "POST",
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            url: config.rest + "image/upload/Test",
            xhr: function () {
                var myXhr = $.ajaxSettings.xhr();
                if (myXhr.upload) { // Check if upload property exists
                    myXhr.upload.addEventListener("progress", progressHandler, false);
                }
                return myXhr;
            },
            beforeSend: function (request) {
                request.setRequestHeader("token", config.token);
            },
            success: function (data) {
                $("#REST-status").html($("#REST-status").html() + "<br/>UPLOAD --> SUCCESS: " + JSON.stringify(data, null, 2));
                log(data);
            },
            error: function (xhr, exception) {
                $("#REST-status").html($("#REST-status").html() + "<br/>UPLOAD --> " + xhr.status + " (" + xhr.statusText + "): " + xhr.responseText);
            }
        });
    });
};

var progressHandler = function (e) {
    if (e.lengthComputable) {
        log(e.loaded + " of " + e.total);
    }
};