function scrollDown() {
  var focusBottom = document.getElementById("adobewordpress");
  focusBottom.scrollTop = focusBottom.scrollHeight;
}

$("input").keypress(function(event) {
  if (event.which == 13) {
    event.preventDefault();
    $('form.chat input[type="submit"]').click();
  }
});
$('form.chat input[type="submit"]').click(function(event) {
  event.preventDefault();
  var inputBox = $('form.chat input[type="text"]');
  var message = inputBox.val();
  if ($('form.chat input[type="text"]').val()) {
    var d = new Date();
    var hour = d.getHours();
    var minutes = d.getMinutes();
    var seconds = d.getSeconds();
    var clock = (('' + hour).length < 2 ? '0' : '') + hour + ":" + (('' + minutes).length < 2 ? '0' : '') + minutes +":"+(('' + seconds).length < 2 ? '0' : '')+seconds;
    var month = d.getMonth() + 1;
    var day = d.getDate();
    var currentDate =
      (('' + day).length < 2 ? '0' : '') + day + '.' +
      (('' + month).length < 2 ? '0' : '') + month + '.' +
      d.getFullYear() + '&nbsp;&nbsp;' + clock;
    $('form.chat div.messages').append('<div class="message"><div class="myMessage"><p>' + encodeHTML(message) + '</p><date>' + currentDate + '</date></div></div>');
    setTimeout(function() {
      $('form.chat > span').addClass('spinner');
    }, 100);
    setTimeout(function() {
      $('form.chat > span').removeClass('spinner');
    }, 2000);
  }
  inputBox.val('');
  inputBox.focus();
  scrollDown();
});

function encodeHTML(value) {
    return $("<div/>").text(value).html();
}

function addServerMessage(message) {
    var d = new Date();
    var hour = d.getHours();
    var minutes = d.getMinutes();
    var seconds = d.getSeconds();
    var clock = (('' + hour).length < 2 ? '0' : '') + hour + ":" + (('' + minutes).length < 2 ? '0' : '') + minutes +":"+(('' + seconds).length < 2 ? '0' : '')+seconds;
    var month = d.getMonth() + 1;
    var day = d.getDate();
    var currentDate =
      (('' + day).length < 2 ? '0' : '') + day + '.' +
      (('' + month).length < 2 ? '0' : '') + month + '.' +
      d.getFullYear() + '&nbsp;&nbsp;' + clock;
    $('form.chat div.messages').append('<div class="message"><div class="fromThem"><p>' + encodeHTML(message) + '</p><date>' + currentDate + '</date></div></div>');
    scrollDown();
}