
var apiContext = "mapi";
var PropResource = {
    "svr.flag"   : "1",        // local : 0, dev : 1, com : 2
    "svr.host.0" : "http://192.168.0.195/",
    "svr.host.1" : "https://dev.wenote.com/",

    "canvas.js.path" : "fbres/res/fb/js/canvas/",
    "common.js.path" : "fbres/res/fb/js/common/",
    "oauth.js.path" : "fbres/res/fb/js/oauth/",
    "pdf.js.path" : "fbres/res/fb/js/pdf/",
    "main.js.path" : "fbres/res/fb/js/",


    "fb.id.0" : "1030834590275215",
    "fb.id.1" : "1030834590275215",
    "fb.rdurl.0" : "http://fb.wenote.com",
    "fb.rdurl.1" : "https://dev.wenote.com",
    "fb.scope" : "email,public_profile,user_friends",

    "gg.id.0" : "328338766882-22umrh27lveie56a2bga7a18km50337m.apps.googleusercontent.com",
    "gg.id.1" : "637630723263-ldk0mmipo5iq8bqt15h5s7gi154lh185.apps.googleusercontent.com",
    "gg.sk.0" : "Z4AUfpJ-NBpsy9e7Xjmx5mcz",
    "gg.sk.1" : "2HyMoY1vvwK9ytDBivreZKiA",
    "gg.rdurl.0" : "http://localhost:8080",
    "gg.rdurl.1" : "https://dev.wenote.com/oauth2callback",
    "gg.scope" : "email profile",

    "default.room.list" : apiContext + "/default/room/list.json",
    "room.list"         : apiContext + "/room/list.json",

    "fb.auth" : apiContext + "/fb/auth.json",
    "gg.auth" : apiContext + "/gl/auth.json",



    "add.device"           : apiContext + "/device/add.json",
    "update.device"        : apiContext + "/device/update.json",
    "get.device"           : apiContext + "/device/get.json",
    "update.device.status" : apiContext + "/device/status/update.json",

    "common.check.connect" : apiContext + "/common/checkConnection.json",

    "room.check.passwd" : apiContext + "/f/chkPassword.json",
    "room.save.canvas"  : apiContext + "/f/saveCanvas.json",
    "room.upload.files" : apiContext + "/f/uploadFiles.json",
    "room.pdf.download" : apiContext + "/f/file/pdf",

    "bookmark.add"     : apiContext + "/bm/add.json",
    "bookmark.remove"  : apiContext + "/bm/remove.json",

    "canvas.add" : apiContext + "/canvas/add.json",
    "canvas.get" : apiContext + "/canvas/get.json",
    "canvas.packet" : apiContext + "/canvas/packet.json",

    "canvas.bg.save" : apiContext + "/canvas/bg/save.json",
    "canvas.packet" : apiContext + "/canvas/packet.json",

    "canvas.download" : apiContext + "/canvas/download",

    "canvas.comment.add" : apiContext + "/canvas/comment/add.json",
    "canvas.comment.remove" : apiContext + "/canvas/comment/remove.json",
    "canvas.comment.list" : apiContext + "/canvas/comment/list",
    "canvas.comment.pos.update" : apiContext + "/canvas/comment/pos/update.json",

    "canvas.memo.add" : apiContext + "/canvas/memo/add.json",
    "canvas.memo.save" : apiContext + "/canvas/memo/save.json",
    "canvas.memo.remove" : apiContext + "/canvas/memo/remove.json",

    "canvas.plugin.fold.update" : apiContext + "/canvas/plugin/fold/update.json",
    "canvas.plugin.ord.update" : apiContext + "/canvas/plugin/ord/update.json",
    "canvas.plugin.pos.update" : apiContext + "/canvas/plugin/pos/update.json",
    "canvas.plugin.color.update" : apiContext + "/canvas/plugin/color/update.json",

    "received.invite.get" : apiContext + "/invite/receive/list.json",
    "notify.invite.send" : apiContext + "/invite/send.json",
    "notify.invite.search" : apiContext + "/invite/search/list.json",
    "notify.invite.remove" : apiContext + "/invite/remove.json",

};



PropResource.getProp = function(str) {
    if(PropResource[str]) str = PropResource[str];
    return str;
}
function _prop(str) {
    return PropResource.getProp(str);
}