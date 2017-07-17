
// Sender는 무조건 나고, receive connection만 관리한다.
var VideoCtrl = {
    limit : 16, // 타인 카메라 개수
    idx : 2,
    map : null,
    myStream : null,
    deny : false,
    noCamera : false,
    allow : false,
    tempGetUserMedia : null,
    constraints : {"audio":true, "video":{mandatory:{minWidth:320,minHeight:180}}},
	fullScreenUserId : "",
	Command : {
		screen_full : function(userNo, userId){
			VideoCtrl.changeScreen(true, userNo, userId);			
		},
		screen_default : function(userNo, userId){
			VideoCtrl.changeScreen(false, userNo, userId);
		},		
		sound_on : function(userNo, userId){
			
		},		
		sound_off : function(userNo, userId){
			
		},		
		video_on : function(userNo, userId){
			
		},
		video_off : function(userNo, userId){
			
		}
	},

    initMyMedia : function(roomid, currentUser, isMaster, isDisplay, initEndF){
		if(PacketMgr.isOnlyTeacherVCam && (!PacketMgr.isCreator && PacketMgr.usertype != "2")) {
			$("#" + VideoCtrl.CONTAINER).hide();
			$("#videoCtrl").hide();
			return;
		}

        if(cordova.platformId == "ios")
            tempGetUserMedia = new getUserMedia(null, null, null);
        // if(this.isLimit(mode)) return;
        var callback = function(isSuccess, stream) {
			if(initEndF) initEndF(isSuccess, stream);

            VideoCtrl.allow = isSuccess;
            var options = {
                mode : "self",
                roomid : roomid,
                stream : stream,
                support : VideoCtrl.checkSupport(),
                limit : VideoCtrl.isLimit("self"),
				isMaster : isMaster,
				isDisplay : isDisplay
            }
            
            if(VideoCtrl.map == null) VideoCtrl.map = new Map();
            
            var data = VideoCtrl.map.get(currentUser.userno);
            if(data == null){
                //console.log("Video Create..")
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
                                 {"audio":true, "video":{mandatory:{minWidth: 320, minHeight: 180 }}, "isDisplay" : isDisplay},
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
    
    initPeerMedia : function(mode, roomid, params, isMaster, isDisplay){
		
		var userNo = (mode == "offer") ? params.offer : params.answer;
        var mappingUserNo = (mode == "offer") ? params.answer : params.offer;
        var turnInfo = RoomSvr.userno == params.offer ? params.offerTurn : params.answerTurn;
		var currentUserInfo = Ctrl.Member.getUserOnline(userNo, "userno");         // 클라이언트 본인
        var mappingUserInfo = Ctrl.Member.getUserOnline(mappingUserNo, "userno");  // 상대방 유저
        
		if(PacketMgr.isOnlyTeacherVCam){
			// PacketMgr.isCreator
			
			PacketMgr.creatorid != currentUserInfo.userid && currentUserInfo.usertype != "2"  
			PacketMgr.creatorid != mappingUserInfo.userid && mappingUserInfo.usertype != "2"
				
			/**
			if(currentUserInfo.usertype != "2" || mappingUserInfo.usertype != "2"){
				return;	
			}			
			**/
			
			// 선생님만 캠 허용시 내가 개설자도 아니고 선생님도 아니라면 return
			if(PacketMgr.creatorid != currentUserInfo.userid && currentUserInfo.usertype != "2") return;
			
			// 선생님만 캠 허용시 상대편이 개설자도 아니고 선생님도 아니라면 return; 
			if(PacketMgr.creatorid != mappingUserInfo.userid && mappingUserInfo.usertype != "2") return;			
		}

		var platformStr = cordova.platformId;

        if(platformStr == "ios")
            tempGetUserMedia = new getUserMedia(null, null, null);

        
        if(VideoCtrl.map == null) VideoCtrl.map = new Map();
        
		console.log("[VideoCtrl.initPeerMedia / WebRTC] mappingUserNo : " + mappingUserNo);

		console.log("[VideoCtrl.initPeerMedia / WebRTC] currentUserInfo(나) : " + currentUserInfo.usernm + ", " + currentUserInfo.userid);
		if(mappingUserInfo != null) {
            console.log("[VideoCtrl.initPeerMedia / WebRTC] mappingUserInfo(상대방) : " + mappingUserInfo.usernm + ", " + mappingUserInfo.userid);
        }


		// 내가 접속한 경우 answer, 타인이 접속한 경우 offer
		var options = {
			mode : mode,
			roomid : roomid,
			turnsvr : turnInfo,
			stream : null,
			support : VideoCtrl.checkSupport(),
			limit : VideoCtrl.isLimit(mode),
			isMaster : isMaster,
			isDisplay : isDisplay
		}

        var map = VideoCtrl.map.get(mappingUserNo);

		console.log(map);

        if(map == null) {   // 내가 접속한 경우 answer, 타인이 접속한 경우 offer
			var video = new Video("videoWrapper", VideoCtrl.idx++, currentUserInfo, mappingUserInfo, options);  // vpeer 생성..
            VideoCtrl.map.put(mappingUserNo, video);
			
			if(platformStr == "android") {
				// Native에 신규 영상정보 전달..
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
					mode       : mode,
					isdisplay  : isDisplay
				};

				console.log("addPeerVideo params : " + JSON.stringify(cordovaParams));
				cordova.exec(function(result) {
					console.log("cordova.exec() success.. addPeerVideo");
				}, function(result) {
					console.log('addPeerVideo ' + JSON.stringify(result));
                    Ctrl.Msg.show(_msg("msg.exception"));
				}, "VideoPlugin", "addPeerVideo", [cordovaParams]);
			}

		}else{
			console.log("updatePeerVideo");
			//* layer만 display
			
			if(platformStr == "android") {
				
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
					mode       : mode,
					isdisplay  : isDisplay
				};

				cordova.exec(function(result) {
					console.log("cordova.exec() success.. updatePeerVideo");
				}, function(result) {
					console.log('updatePeerVideo ' + JSON.stringify(result));
                    Ctrl.Msg.show(_msg("msg.exception"));
				}, "VideoPlugin", "updatePeerVideo", [cordovaParams]);
				
			} else if(platformStr == "ios") {
                
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
                    mode       : mode,
                    isdisplay  : isDisplay
                };
                
                
                cordova.exec(null, null, "WebRTCPlugin", "updatePeerVideo", [cordovaParams]);
            } else {
				var isAlreadyDisplayed = map.getVideoDisplay();
				if(!isAlreadyDisplayed && isDisplay) {
					// 레이어만 새로 그린다.
					// map.create(mappingUserInfo, isDisplay); 
                // if(platformStr == "android") map.show(false);  // 사용하지 않는 코드
				}
			}
		}
		
		// 비디오 갯수마다 wrapper class를 조절해줘야한다.
		this.updateWrapper();
            
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
        if(VideoCtrl.map != null){
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

	toggle : function(){
		$("#" + VideoCtrl.CONTAINER).toggle();
	},
	
	showInvite : function(){
		this.toggle();		
		$("#invite_noti_btn").trigger("click");
	},
	
	masterChange : function(masterId){
		// 마스터 변경시 표시		
		$(".videoWrap", "#" + VideoCtrl.CONTAINER).each(function(){
			var userId = $(this).attr("userid");
			if(userId == masterId){
				$(this).addClass("user_master");
			}else{
				$(this).removeClass("user_master");
			}			
		});
	},
	
	updateWrapper : function(){
		// display count를 세서 limit를 초과하면 초대하기를 가린다.
		// 초대창 count 1개 제거
		var videoCnt = $(".videoWrap", "#videoWrapper").length - 1;
		if(videoCnt >= VideoCtrl.limit){
			$("#videoInvite").hide();
		}else{
			$("#videoInvite").show();
		}
	},
	

	receive : function(packet){
		/**
		 * action
		 * 		size_max
		 * 		size_normal
		 * 		sound_off
		 * 		sound_on
		 */		
		var userId = packet.userid;
		var userNo = packet.userno;
		var action = packet.action;
		
		if(typeof(VideoCtrl.Command[action]) != "undefined"){
			VideoCtrl.Command[action](userNo, userId);
		}		
	},
	
	controlScreen : function(isMax, isForceAuth, userNo, userId){
		if(isForceAuth && !Ctrl._checkAuth(true)) return;
		
		var url = Utils.addContext( _url("vcam.update.screen") );
		var params = {			
			roomid : RoomSvr.roomid,
			isfullscreen : isMax ? "1" : "0"
		};
		
		Utils.request(url, "json", params, function(data){
			if(data.result == "0"){
				if(userNo == null || userNo == ""){
					if($(".videoWrap", "#" + VideoCtrl.WRAPPER).length > 1){
						var firstUserId = $(".videoWrap", "#" + VideoCtrl.WRAPPER).eq(0).attr("userid");
						var firstUserInfo = Ctrl.Member.getUserOnline(firstUserId, "userid");	
						userNo = firstUserInfo.userno;
						userId = firstUserInfo.userid;				
					}
				}

				VideoCtrl.changeScreen(isMax, userNo, userId);
				
				var action = isMax ? "screen_full" : "screen_default";
				PacketMgr.Master.vCam(action, userNo, userId);
			} 
		});
		
	},
	
	changeScreenUser : function(userId){
		if(userId == null || userId == ""){
			if($(".videoWrap", "#" + VideoCtrl.WRAPPER).length == 1){
				// 없으면 minimize
				this.changeScreen(false);
				return;
			}
			
			// 유저가 다르면 첫번째 영상 유저
			var firstUserId = $(".videoWrap", "#" + VideoCtrl.WRAPPER).eq(0).attr("userid");
			var firstUserInfo = Ctrl.Member.getUserOnline(firstUserId, "userid");	
			userId = firstUserInfo.userid;			
		}
		
		
		$(".videoWrap", "#" + VideoCtrl.WRAPPER).each(function(){
			if($(this).attr("userid") == userId){
				$(this).addClass("fullsize");
			}else{
				$(this).removeClass("fullsize");
			}
		});
	},

	changeScreen : function(isMax, userNo, userId){		
		if(isMax){
			if($("#videoContainer").hasClass("video_fullsize")){
				// 이미 full size인 경우 
				// 다른유저 요청시 
				if(userId != this.fullScreenUserId){					
					// full 사이즈 상태일때, full 유저만 바뀜
					this.changeScreenUser(userId);
					this.fullScreenUserId = userId;
				}				
				return;				
			}
			
			$("#videoContainer").addClass("video_fullsize");
			
			this.changeScreenUser(userId);
			
			this.fullScreenUserId = userId;
			
		}else{
			$("#videoContainer").removeClass("video_fullsize");			
			$(".videoWrap", "#" + VideoCtrl.WRAPPER).removeClass("fullsize");						
		}		
	},
	
	changeOpt : function(){

		// 선생님을 제외한 모든 학생 연결 제거
		// 1. 학생일 경우, 모든 연결 제거 
		// 2. 선생님이나 방 개설자일 경우 상대가 선생님일 경우를 제외하고, 모든 연결을 끊는다. 
		if(PacketMgr.isOnlyTeacherVCam){
			if(!PacketMgr.isCreator && PacketMgr.usertype != "2"){
				// 본인 포함안 영상 전부 제거
				this.destroyAll();
				
				// hide 처리
				$("#" + VideoCtrl.CONTAINER).hide();
				$("#videoCtrl").hide();

				// Video 컨테이너 숨기는 플러그인 필요..
				
			}else{
				// 선생님이나 방 개설자인 경우, 대상이 학생인 사람과의 연결 제거  
				var dataKeys = VideoCtrl.map.keys();
				var len = dataKeys == null ? 0 : dataKeys.length;
				for(var i=0; i<len; i++){
					var key = dataKeys[i];
					var map = VideoCtrl.map.get(key);
					var mappingUser = map.getMappingUser();
					
					// 내 영상
					if(mappingUser == null) continue;
					
					if(PacketMgr.creatorid != mappingUser.userid && mappingUser.usertype != "2"){
						VideoCtrl.idx--;						
						map.destroy();
						VideoCtrl.map.remove(mappingUser.userno);						
					}
				}
				
			}
			
			// 선생님이
			
			
		} 
			
		
		/**	
				&& PacketMgr.usertype != "2" && !PacketMgr.isCreator){
			vShare.hide();
		}else{
			vShare.show();
		}
		***/
		
	},
    
    destroy : function(userNo, userId){
        console.log(VideoCtrl.map);
        
        if(VideoCtrl.map != null && VideoCtrl.map.get(userNo) != null){
            VideoCtrl.map.get(userNo).destroy();
            VideoCtrl.map.remove(userNo);
            this.idx--;
            if(this.idx < 2) this.idx = 2;
				
			if(cordova.platformId == "ios") {
                tempGetUserMedia.removeVideoTag_hg(videoMapForiOS.get(userNo));
                cordova.exec(null, null, "WebRTCPlugin", "removeVideoview", [userId]);
                videoMapForiOS.remove(userNo);
            }
        }

		this.updateWrapper();
    },
    
    destroyAll : function(){
        if(cordova.platformId == "ios") {
			/*
            if(videoMapForiOS != null) {
                for (var item in videoMapForiOS.map){
                    tempGetUserMedia.removeVideoTag_hg(videoMapForiOS.map[item]);
                    videoMapForiOS.remove(item);
                }
            }*/
			cordova.exec(null, null, "WebRTCPlugin", "onResetPlugin", null);
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
        console.log("[VideoCtrl.sendIceCandidate] socket.io로 Native의 candidate를 전송합니다..  sender : " + sender + ", receiver : " + receiver);
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
        
        //console.log("[VideoCtrl.sendNativeOffer] sdp : " + sdpDesc);
        
        //sdpDesc = sdpDesc.replaceAll("<br/>", "\r\n");
        sdpDesc = decodeURIComponent(sdpDesc);  // 웹에서 읽을 수 있는 형태로 변환
        //console.log("[VideoCtrl.sendNativeOffer] sdp : " + sdpDesc);
        
        var nativeLocalDescription = new Object();
        nativeLocalDescription.type = "offer";
        nativeLocalDescription.sdp = sdpDesc;
        
        RoomSvr.sendWebRtcData({ cmd : cmd, from : sender, to : receiver, roomid : roomId, msg : nativeLocalDescription });
    },
    
    // 신규 추가함.. - Native에서 생성한 Answer SessionDescription 전송하기..
    sendNativeAnswer : function(cmd, sender, receiver, roomId, sdpDesc) {
        console.log("[VideoCtrl.sendNativeAnswer] socket.io로 Native의 ANSWER SDP를 전송합니다..  sender : " + sender + ", receiver : " + receiver);
        
        //console.log("[VideoCtrl.sendNativeAnswer] sdp : " + sdpDesc);
        //sdpDesc = sdpDesc.replaceAll("<br/>", "\r\n");
        sdpDesc = decodeURIComponent(sdpDesc);  // 웹에서 읽을 수 있는 형태로 변환
        //console.log("[VideoCtrl.sendNativeAnswer] sdp : " + sdpDesc);
        
        var nativeLocalDescription = new Object();
        nativeLocalDescription.type = "answer";
        nativeLocalDescription.sdp = sdpDesc;
        
        RoomSvr.sendWebRtcData({ cmd : cmd, from : sender, to : receiver, roomid : roomId, msg : nativeLocalDescription });
    }
}