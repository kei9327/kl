

// sender는 무조건 나고, receive connection만 관리한다.
var VideoCtrl = {
    limit : 16, // 타인 카메라 갯수
    idx : 2,
    map : null,
    myStream : null,
    deny : false,
    noCamera : false,
    allow : false,
    tempGetUserMedia : null,
    constraints : {"audio":true, "video":{mandatory:{minWidth:320,minHeight:180}}},
    initMyMedia : function(roomid, currentUser){
        if(cordova.platformId == "ios")
            tempGetUserMedia = new getUserMedia(null, null, null);
        // if(this.isLimit(mode)) return;
        var callback = function(isSuccess, stream){
            VideoCtrl.allow = isSuccess;
            var options = {
                mode : "self",
                roomid : roomid,
                stream : stream,
                support : VideoCtrl.checkSupport(),
                limit : VideoCtrl.isLimit("self")
            }
            
            if(VideoCtrl.map == null) VideoCtrl.map = new Map();
            
            var data = VideoCtrl.map.get(currentUser.userno);
            if(data == null){
                console.log("Video 생성..")
                var video = new Video("videoWrapper", 1, currentUser, null, options);  // initMyVideo() 호출
                VideoCtrl.map.put(currentUser.userno, video);
            }            
            var keys = VideoCtrl.map == null ? 0 : VideoCtrl.map.keys();
            for(var i=0; i<keys.length; i++){
                var data = VideoCtrl.map.get(keys[i]);
                if(cordova.platformId == "ios") {
                    data.setMyMedia(isSuccess, stream);
                }
            }
        }
        
        var renderingMedia = function(){
            try {
                if(cordova.platformId == "ios") {
                    getUserMedia(
                                 {"audio":true, "video":{mandatory:{minWidth: 320, minHeight: 180 }}},
                                 function(stream) {
                                     VideoCtrl.noCamera = false;
                                     VideoCtrl.deny = false;
                                     VideoCtrl.myStream = stream;
                                     callback(true, stream);
                                 },
                                 
                                 function(error) {
                                     if(error.name == "DevicesNotFoundError"){
                                         VideoCtrl.noCamera = true;
                                     }else if(error.name == "PermissionDeniedError"){
                                         VideoCtrl.deny = true;
                                     }
                                     
                                     // VideoCtrl.deny = true;
                                     callback(false, null);
                                 }
                                 );
                }
            }catch(e){
                console.log(e);
            }
        }
        
        renderingMedia();
        
    },
    
    initPeerMedia : function(mode, roomid, params){
        if(cordova.platformId == "ios")
            tempGetUserMedia = new getUserMedia(null, null, null);
        //console.log("[VideoCtrl] initPeerMedia / mode : " + mode + ", params : " + JSON.stringify(params));
        
        // if(this.isLimit(mode)) return;
        
        // answer일 경우 user1이 currentUser, offer는 user2가 currentUser
        var userNo = (mode == "offer") ? params.offer : params.answer;
        var mappingUserNo = (mode == "offer") ? params.answer : params.offer;
        var turnInfo = RoomSvr.userno == params.offer ? params.offerTurn : params.answerTurn;
        
        var currentUserInfo = Ctrl.Member.getUserOnline(userNo, "userno");         // 클라이언트 본인
        var mappingUserInfo = Ctrl.Member.getUserOnline(mappingUserNo, "userno");  // 상대방 유저
        
        if(VideoCtrl.map == null) VideoCtrl.map = new Map();
        
        var map = VideoCtrl.map.get(mappingUserNo);
        if(map == null) {   // 내가 접속한 경우 answer, 타인이 접속한 경우 offer
            var options = {
                mode    : mode,
                roomid  : roomid,
                turnsvr : turnInfo,
                stream  : null,
                support : VideoCtrl.checkSupport(),
                limit   : VideoCtrl.isLimit(mode)
            }
            
            var video = new Video("videoWrapper", VideoCtrl.idx++, currentUserInfo, mappingUserInfo, options);
            VideoCtrl.map.put(mappingUserNo, video);
            
            
            if(cordova.platformId == "android") {
                // Native에 신규 영상유저 정보 전달
                var callerUserId = (mode == "offer") ? currentUserInfo.userid : mappingUserInfo.userid;
                var calleeUserId = (mode == "offer") ? mappingUserInfo.userid : currentUserInfo.userid;
                var callerUserNm = (mode == "offer") ? currentUserInfo.usernm : mappingUserInfo.usernm;
                var calleeUserNm = (mode == "offer") ? mappingUserInfo.usernm : currentUserInfo.usernm;
                
                var cordovaParams = {
                    partner    : mappingUserInfo.userid,
                    caller     : callerUserId + "|" + callerUserNm,
                    callee     : calleeUserId + "|" + calleeUserNm,
                    offerTurn  : params.offerTurn,
                    answerTurn : params.answerTurn,
                    mode       : mode
                };
                cordova.exec(function(result) {
                    console.log("cordova.exec() success.. addPeerVideo");
                }, function(result) {
                    alert('add Video user ' + JSON.stringify(result));
                }, "VideoPlugin", "addPeerVideo", [cordovaParams]);
            }
        }
        
        console.log(VideoCtrl.map);
        
    },
    
    isLimit : function(mode){
        if(mode == "self"){
            var cameraUserCnt = this.getCameraUserCnt();
            if(cameraUserCnt > (this.limit + 1)){
                return true;
            }
        }else{
            // if(PacketMgr.isGuest) return true;
            
            // 자신 카메라 + 1
            var cameraUserCnt = VideoCtrl.map == null ? 0 : VideoCtrl.map.keys().length;
            // alert(mode + " " + cameraUserCnt + " " + this.limit);
            
            if(mode == "offer" && cameraUserCnt > VideoCtrl.limit) return true;
        }
        
        return false;
    },
    
    checkSupport : function(){
        if(Utils.browser("msie") || Utils.browser("safari") || Utils.browser("opera")) {
            return false;
        } else if(!navigator.webkitGetUserMedia && !navigator.mozGetUserMedia) {
            return false;
        } else {
            return true;
        }
    },
    
    //--
    get : function(key){
        if(VideoCtrl.map == null){
            return VideoCtrl.map.get(key);
        }
        
        return null;
    },
    
    getCameraUserCnt : function(){
        var list = Ctrl.Member.list;
        var len = list == null ? 0 : list.length;
        var cnt = 0;
        for(var i=0; i<len; i++){
            var data = list[i];
            if(data.userno != data.userid){
                cnt++;
            }
        }
        
        return cnt;
    },
    
    receiveSignal : function(data){
        console.log("[VideoCtrl.receiveSignal] 원격으로부터 시그널을 받았습니다. cmd : " + data.cmd + ", sender : " + data.from + ", receiver : " + data.to)
        var type = data.cmd;
        var sender = data.from;
        var receiver = data.to;
        var ord = data.ord;
        
        // Utils.log("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ streamvideo type : " + type + ", sender : " + sender + ", receive : " + receiver + ", ord : " + ord);
        var	mappingUserInfo = null;
        
        if(RoomSvr.userid == sender){
            mappingUserInfo = Ctrl.Member.getUserOnline(receiver, "userid");
        }else if(RoomSvr.userid == receiver){
            mappingUserInfo = Ctrl.Member.getUserOnline(sender, "userid");
        }
        
        var mappingUserNo = mappingUserInfo.userno;
        
        var video = VideoCtrl.map.get(mappingUserNo);
        video["async"](data);
        
    },
    
    noti : function(){
        var container = document.getElementById("turn_cam_noti");
        var msg = !VideoCtrl.checkSupport() ? _msg("video.allow.ie") : _msg("video.allow.msg");
        if(!container) {
            var htmlStr = 	"<div id=\"turn_cam_noti\" class=\"turn_cam_noti none\" style=\"display:none;\">"+msg+"\
            <a onclick=\"$('#turn_cam_noti').fadeOut('slow');\" class=\"btn_close\"></a>\
            </div>";
            
            $("body").append(htmlStr);
        }
        
        $("#turn_cam_noti").show();
        
        setTimeout(function(){
            
            $("#turn_cam_noti").hide("2000");
            
        }, "5000");
        
    },
    
    destroy : function(userNo, userId){
        console.log(VideoCtrl.map);
        
        if(VideoCtrl.map != null && VideoCtrl.map.get(userNo) != null){
            VideoCtrl.map.get(userNo).destroy();
            VideoCtrl.map.remove(userNo);
            
            if(cordova.platformId == "ios") {
                tempGetUserMedia.removeVideoTag_hg(videoMapForiOS.get(userNo));
                cordova.exec(null, null, "WebRTCPlugin", "removeVideoview", [userNo]);
                videoMapForiOS.remove(userNo);
            }
            
            this.idx--;
            if(this.idx < 2) this.idx = 2;
        }
    },
    
    destroyAll : function(){
        if(cordova.platformId == "ios") {
            if(videoMapForiOS != null) {
                for (var item in videoMapForiOS.map){
                    tempGetUserMedia.removeVideoTag_hg(videoMapForiOS.map[item]);
                    cordova.exec(null, null, "WebRTCPlugin", "removeVideoview", [item]);
                    videoMapForiOS.remove(item);
                }
            }
        }
        if(VideoCtrl.map != null){
            var keys = VideoCtrl.map.keys();
            for(var i=0; i<keys.length; i++){
                VideoCtrl.map.get(keys[i]).destroy();				
            }
            VideoCtrl.map = null;
            this.idx = 2;
        }
    },
    
    // 신규 정의함 - Native에서 만들어진 Ice candidate 전송하기..
    sendIceCandidate : function(cmd, sender, receiver, roomId, sdpMid, sdpIdx, candidateStr) {
        console.log("[VideoCtrl.sendIceCandidate] socket.io로 Native의 candidate를 전송합니다..");
        //console.log("cmd : " + cmd + ", sender : " + sender + ", receiver : " + receiver);
        
        var candidateObj = new Object();
        candidateObj.candidate = candidateStr;
        candidateObj.sdpMLineIndex = sdpIdx;
        candidateObj.sdpMid = sdpMid;
        
        /*
         var candidateObj = {
         'sdpMLineIndex' : sdpIdx,
         'sdpMid' : sdpMid,
         'candidate' : candidateStr
         };*/
        
        var rtcIceCandidate = new window.RTCIceCandidate(candidateObj);
        
        //console.log(candidateObj);
        
        RoomSvr.sendWebRtcData({ cmd : cmd, from : sender, to : receiver, roomid : roomId, msg : rtcIceCandidate });
    },
    
    
    // 신규 추가함.. - Native에서 생성한 Offer SessionDescription 전송하기..
    sendNativeOffer : function(cmd, sender, receiver, roomId, sdpDesc) {
        console.log("[VideoCtrl.sendNativeOffer] socket.io로 Native의 OFFER SDP를 전송합니다..  sender : " + sender + ", receiver : " + receiver);
        
        console.log("[VideoCtrl.sendNativeOffer] sdp : " + sdpDesc);
        
        //sdpDesc = sdpDesc.replaceAll("<br/>", "\r\n");
        sdpDesc = decodeURIComponent(sdpDesc);
        console.log("[VideoCtrl.sendNativeOffer] sdp : " + sdpDesc);
        
        var nativeLocalDescription = new Object();
        nativeLocalDescription.type = "offer";
        nativeLocalDescription.sdp = sdpDesc;
        
        RoomSvr.sendWebRtcData({ cmd : cmd, from : sender, to : receiver, roomid : roomId, msg : nativeLocalDescription });
    },
    
    // 신규 추가함.. - Native에서 생성한 Answer SessionDescription 전송하기..
    sendNativeAnswer : function(cmd, sender, receiver, roomId, sdpDesc) {
        console.log("[VideoCtrl.sendNativeAnswer] socket.io로 Native의 ANSWER SDP를 전송합니다..  sender : " + sender + ", receiver : " + receiver);
        
        console.log("[VideoCtrl.sendNativeAnswer] sdp : " + sdpDesc);
        //sdpDesc = sdpDesc.replaceAll("<br/>", "\r\n");
        sdpDesc = decodeURIComponent(sdpDesc);
        console.log("[VideoCtrl.sendNativeAnswer] sdp : " + sdpDesc);
        
        var nativeLocalDescription = new Object();
        nativeLocalDescription.type = "answer";
        nativeLocalDescription.sdp = sdpDesc;
        
        RoomSvr.sendWebRtcData({ cmd : cmd, from : sender, to : receiver, roomid : roomId, msg : nativeLocalDescription });
    }
}