"use strict";

var uploadVariable = "challengeImage01";

// Execute when page is loaded succesful
$(function () {
    log("Ready");

    createListeners();
    $("#REST-status").html("Requesting values...");
    restTest("variable/read/challengeComment01");
    restTest("variable/readMany/challengeComment01,challengeImage01");
    
    restTest("variable/write/challengeComment02", "Hallo Welt!");

    restTest("variable/readGroupArray/challengeComment01");
    restTest("variable/readInterventionArray/challengeComment01");

    restTest("variable/readGroupArrayMany/challengeComment01,challengeImage01");
    restTest("variable/readInterventionArrayMany/challengeComment02");
    
    restTest("variable/calculateGroupAverage/points");
    restTest("variable/calculateInterventionAverage/points");
    
    restTest("voting/votings/challengeVotes02");
    restTest("voting/votingsGroupArray/challengeVotes02");
    restTest("voting/votingsInterventionArray/challengeVotes02");
    
    restTest("voting/vote/challengeVoting02/579a42679afa061cf073416b");

    restTest("variable/read/points");
    restTest("credits/storeCredit/points/c02");
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
            url: config.rest + "image/upload/"+uploadVariable,
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