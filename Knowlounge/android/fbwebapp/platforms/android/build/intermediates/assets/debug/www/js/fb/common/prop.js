
var apiContext = "mapi";
var PropResource = {
    "svr.flag"   : "1",        // local : 0, dev : 1, com : 2
    "svr.host.0" : "http://192.168.0.195/",
    "svr.host.1" : "https://dev.knowlounges.com/",
    "svr.host.2" : "https://www.knowlounges.com/",

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

    "poll.tmp.add" : apiContext + "/poll/tmp/add.json",
    "poll.tmp.get" : apiContext + "/poll/tmp/get.json",
    "poll.tmp.update" : apiContext + "/poll/tmp/update.json",
    "poll.tmp.remove" : apiContext + "/poll/tmp/remove.json",
    "poll.tmp.copy" : apiContext + "/poll/tmp/copy.json",
    "poll.tmp.list" : apiContext + "/poll/tmp/list.json",


    "poll.get" : apiContext + "/poll/get.json",
    "poll.list" : apiContext + "/poll/list.json",
    "poll.remove" : apiContext + "/poll/remove.json",
    "poll.all.remove" : apiContext + "/poll/all/remove.json",

    "poll.answer.add" : apiContext + "/poll/answer/add.json",
    "poll.answer.list" : apiContext + "/poll/answer/list.json",

    "poll.file.item.get" : apiContext + "/poll/file/item.get.json",

    "poll.subroom.move" : apiContext + "/poll/subroom/move.json",

    "canvas.move.sub.room" : apiContext + "/room/createSubRoom.json",

    // s : 메모
	"memo.add" : apiContext + "/canvas/memo/add.json",
	"memo.update.pos" : apiContext + "/canvas/plugin/pos/update.json",
	"memo.update.color" : apiContext + "/canvas/plugin/color/update.json",
	"memo.update.fold" : apiContext + "/canvas/plugin/fold/update.json",
	"memo.update.ord" : apiContext + "/canvas/plugin/ord/update.json",
	"memo.save" : apiContext + "/canvas/memo/save.json",
	"memo.save.title" : apiContext + "/canvas/memo/title/save.json",
	"memo.remove" : apiContext + "/canvas/memo/remove.json",
    // e : 메모

    // s : 유튜브 영상 공유
    "vshare.add" : apiContext + "/canvas/vshare/add.json",
    "vshare.save" : apiContext + "/canvas/vshare/save.json",
    "vshare.save.title" : apiContext + "/canvas/vshare/title/save.json",
    "vshare.remove" : apiContext + "/canvas/vshare/remove.json",
    "magicbox.list" : apiContext + "/magicbox/list.json",
    "magicbox.save" : apiContext + "/magicbox/save.json",

	"plugin.update.pos" : apiContext + "/canvas/plugin/pos/update.json",

    "youtube.host" : "https://youtu.be/",
    // e : 유튜브 영상 공유


	// s : 멀티 페이지
	"page.add" : apiContext + "/page/add.json",
	"page.remove" : apiContext + "/page/remove.json",
	"page.change" : apiContext + "/page/change.json",
	"page.order" : apiContext + "/page/order.json",
	"page.get.info" : apiContext + "/page/getInfo.json",
	// e : 멀티 페이지
    "get.turn.server" : apiContext + "/getTurnSvr.json"
};


PropResource.getProp = function(str) {
    if(PropResource[str]) str = PropResource[str];
    return str;
}

function _prop(str) {
    return PropResource.getProp(str);
}
