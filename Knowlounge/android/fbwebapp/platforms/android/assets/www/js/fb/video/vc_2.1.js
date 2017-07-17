// sender는 무조건 나고, receive connection만 관리한	다.
var VideoCtrl = {
	CONTAINER : "videoContainer",
	WRAPPER : "videoWrapper",
	limit : 6, // 타인 카메라 갯수
	idx : 2,
	map : null,
	myStream : null,
	deny : false,
	noCamera : false,
	allow : false,
	constraints : {"audio":true, "video":{mandatory:{minWidth:320,minHeight:180}}},
	fullScreenUserId : "",
	peerTryCnt : 0,
	initMyMedia : function(roomid, currentUser, isMaster, isDisplay, initEndF){		// 미지원시 비디오 숨김
		if(!VideoCtrl.checkSupport()){
			$("#" + VideoCtrl.CONTAINER).hide();
			$("#videoCtrl").hide();
			return;
		}

		if(PacketMgr.isOnlyTeacherVCam && (!PacketMgr.isCreator && PacketMgr.usertype != "2")) {
			// 선생님만 캠 허용일 경우 내가 생성자가 아니면 사운드 off로 시작
			$("#" + VideoCtrl.CONTAINER).hide();
			$("#videoCtrl").hide();
			return;
		}

		if(cordova.platformId == "ios")
            tempGetUserMedia = new getUserMedia(null, null, null);

		var callback = function(isSuccess, stream){
			if(initEndF) initEndF(isSuccess, stream);

			VideoCtrl.allow = isSuccess;
			var options = {
				mode : "self",
				roomid : roomid,
				stream : stream,
				support : VideoCtrl.checkSupport(),
				isMaster : isMaster,
				isDisplay : isDisplay,
				mappinguserno : ''
			}

			if(VideoCtrl.map == null) VideoCtrl.map = new Map();

			var data = VideoCtrl.map.get(currentUser.userno);
			if(data == null){
				var video = new Video(VideoCtrl.WRAPPER, 1, currentUser, null, options);
				VideoCtrl.map.put(currentUser.userno, video);
			}

			var keys = VideoCtrl.map == null ? 0 : VideoCtrl.map.keys();
			for(var i=0; i<keys.length; i++){
				var data = VideoCtrl.map.get(keys[i]);
				data.setMyMedia(isSuccess, stream);
			}
			// 비디오 갯수마다 wrapper class를 조절해줘야한다.
			VideoCtrl.updateInviteLayer();

			if(PacketMgr.vCamFullScreen == "1"){
				// 내가 생성자일 경우 fullScreen
				// 내가 풀 스크린인 경우
				if(PacketMgr.userid == VideoCtrl.fullScreenUserId){
					VideoCtrl.changeScreenUser(VideoCtrl.fullScreenUserId);
				}else{
					VideoCtrl.allocateFullscreen();
				}
			}

		}

		if(!VideoCtrl.checkSupport()){
			VideoCtrl.noCamera = true;
			callback(false, null);
		}else{
			var renderingMedia = function(){
				try{
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
	                } else {
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
		}

	},

	initMyMediaAndroid : function(roomid, currentUser, isMaster, isVideoAllow){		// 미지원시 비디오 숨김

		if(PacketMgr.isOnlyTeacherVCam && (!PacketMgr.isCreator && PacketMgr.usertype != "2")) {
			// 선생님만 캠 허용일 경우 내가 생성자가 아니면 사운드 off로 시작
			return;
		}

		//VideoCtrl.allow = isSuccess;
		var options = {
			mode : "self",
			roomid : roomid,
			stream : null,
			support : VideoCtrl.checkSupport(),
			isMaster : isMaster,
			isDisplay : isVideoAllow == 1 ? true : false,
			mappinguserno : ''
		}

		if(VideoCtrl.map == null) VideoCtrl.map = new Map();

		var data = VideoCtrl.map.get(currentUser.userno);
		if(data == null){
			var video = new Video(VideoCtrl.WRAPPER, 1, currentUser, null, options);
			VideoCtrl.map.put(currentUser.userno, video);
		}


		if(PacketMgr.vCamFullScreen == "1"){
			// 내가 생성자일 경우 fullScreen
			// 내가 풀 스크린인 경우
			if(PacketMgr.userid == VideoCtrl.fullScreenUserId){
				VideoCtrl.changeScreenUser(VideoCtrl.fullScreenUserId);
			}else{
				VideoCtrl.allocateFullscreen();
			}
		}

		var cordovaParam = {
			isvideoallow : isVideoAllow
		};

		cordova.exec(function(result) {
			console.log("cordova.exec() success.. initMyMedia");
		}, function(result) {
			console.log("initMyMedia error : " + JSON.stringify(result));
		}, "VideoPlugin", "initMyMedia", [cordovaParam]);

	},

	reconnectAndroid : function(mode, roomid, params, isMaster, isDisplay) {
		console.log("[WebRTC / reconnectAndroid]")
		var userNo = (mode == "offer") ? params.offer : params.answer;
		var mappingUserNo = (mode == "offer") ? params.answer : params.offer;
		var turnInfo = RoomSvr.userno == params.offer ? params.offerTurn : params.answerTurn;
		var currentUserInfo = Ctrl.Member.getUserOnline(userNo, "userid");
		var mappingUserInfo = Ctrl.Member.getUserOnline(mappingUserNo, "userid");

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
	},

	initPeerMedia : function(mode, roomid, params, isMaster, isDisplay, micStatus){
		var userNo = (mode == "offer") ? params.offer : params.answer;
		var mappingUserNo = (mode == "offer") ? params.answer : params.offer;
		var turnInfo = RoomSvr.userno == params.offer ? params.offerTurn : params.answerTurn;
		var currentUserInfo = Ctrl.Member.getUserOnline(userNo, "userno");
		var mappingUserInfo = Ctrl.Member.getUserOnline(mappingUserNo, "userno");

		/**
		console.log("mappingUserNo", mappingUserNo);
		if(mappingUserNo == "34050488792i2b40"){
			if(VideoCtrl.peerTryCnt < 1){
				// mappingUserInfo = null;
				mappingUserInfo = null;
			}
		}
		// alert( mappingUserInfo );
		console.log("mappingUserInfo", mappingUserInfo);
		***/

		/**
		if(mappingUserInfo == null){
			setTimeout(function(){
				VideoCtrl.peerTryCnt++;
				VideoCtrl.initPeerMedia(mode, roomid, params, isMaster, isDisplay, micStatus);
			}, "100");
		}
		***/

		if(PacketMgr.isOnlyTeacherVCam){
			// PacketMgr.isCreator
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

		// 내가 접속한 경우 answer, 타인이 접속한 경우 offer
		var options = {
			mode : mode,
			roomid : roomid,
			turnsvr : turnInfo,
			stream : null,
			support : VideoCtrl.checkSupport(),
			isMaster : isMaster,
			isDisplay : isDisplay,
			micStatus : micStatus,
			mappinguserno : mappingUserNo
		}

		var map = VideoCtrl.map.get(mappingUserNo);
		console.log(map);
		if(map == null){
			var video = new Video(VideoCtrl.WRAPPER, VideoCtrl.idx++, currentUserInfo, mappingUserInfo, options);
			VideoCtrl.map.put(mappingUserNo, video);
			console.log(VideoCtrl.map);

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
				if(!isAlreadyDisplayed && isDisplay){
					// 레이어만 새로 그린다.
					// map.create(mappingUserInfo, isDisplay);
					map.show(false);
				}
			}
		}

		// 비디오 갯수마다 wrapper class를 조절해줘야한다.
		this.updateInviteLayer();

		if(PacketMgr.vCamFullScreen == "1"){
			// 내가 생성자일 경우 fullScreen
			// 내가 풀 스크린인 경우
			if(mappingUserInfo.userid == VideoCtrl.fullScreenUserId){
				VideoCtrl.changeScreenUser(VideoCtrl.fullScreenUserId);
			}
		}

	},

	setEvent : function(){
		$(".btn_close", "#" + VideoCtrl.CONTAINER).click(VideoCtrl.toggle);
		$(".btn_chargeVideo", "#" + VideoCtrl.CONTAINER).click(VideoCtrl.showInvite);
	},

	checkSupport : function(){
		if(Utils.browser("msie") || Utils.browser("safari") || Utils.browser("opera")){
			return false;
		}else{
			return true;
		}
	},

	allocateFullscreen : function(){
		var targetUserInfo = null;
		var checkExist = function(userId){
			return Ctrl.Member.getUserOnline(userId, "userid");
		}

		// 미접속자가 접속시
		var userInfo = checkExist(VideoCtrl.fullScreenUserId);
		if(userInfo == null){
			userInfo = checkExist(PacketMgr.masterid);
		}
		if(userInfo == null){
			userInfo = checkExist(PacketMgr.creatorid);
		}

		// 3초후 재 체크
		setTimeout(function(){
			VideoCtrl.changeScreenUser(userInfo == null ? "" : userInfo.userid);
		}, 2000);
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
		console.log("[WebRTC / VideoCtrl.receiveSignal] 원격으로부터 시그널을 받았습니다. cmd : " + data.cmd + ", sender : " + data.from + ", receiver : " + data.to)
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


		/**
		if(type === "sdp_offer") {
			// VideoCtrl.Signaling.sendAnswer(receiver, sender, data.msg);
		} else if (type === "sdp_answer") {
			// VideoCtrl.setRemoteDescription(sender, data.msg);

		} else if (type === "sdp_candidate") {
			// VideoCtrl.addIceCandidate(sender, data.msg);
		}
		**/

	},

	noti : function(){
		var container = document.getElementById("turn_cam_noti");
		var msg = !VideoCtrl.checkSupport() ? _msg("video.allow.ie") : _msg("video.allow.msg");
		if(!container) {
			var htmlStr = "<div id=\"turn_cam_noti\" class=\"turn_cam_noti none\" style=\"display:none;\">"+msg+"\
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
			if(typeof(userId) != "undefined" && userId == masterId){
				$(this).addClass("user_master");
				$("#" + VideoCtrl.WRAPPER).prepend( $(this) );

				$("video", $(this)).get(0).play();
			}else{
				$(this).removeClass("user_master");
			}
		});

		// masterChange

	},

	updateInviteLayer : function(){
		if($("#videoContainer").hasClass("video_fullsize")) return;

		// display count를 세서 limit를 초과하면 초대하기를 가린다.
		// 초대창 count 1개 제거
		var videoCnt = $(".videoWrap", "#" + VideoCtrl.WRAPPER).length - 1;
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
		 * 		mic_off
		 * 		mic_on
		 */
		var userId = packet.userid;
		var userNo = packet.userno;
		var action = packet.action;

		if(action.indexOf("mic") > -1 && VideoCtrl.map.get(userNo) != null){
			var targetVideo = VideoCtrl.map.get(userNo);
			targetVideo.receive(action, userNo, userId);
		}else if(action.indexOf("screen") > -1){
			VideoCtrl.changeScreen((action == "screen_full" ? true : false), userNo, userId);
		}
	},

	controlScreen : function(isMax, isForceAuth, userNo, userId){
		// 2016.11.21  펜권한과 동일하게 변경
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

				$("#" + VideoCtrl.CONTAINER).hide();
				$("#videoCtrl").hide();
			}else{
				var removeCnt = 0;
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

						removeCnt++;
					}
				}

				var len = Ctrl.Member.list == null ? 0 : Ctrl.Member.list.length;
				for(var i=0; i<len; i++){
					var userInfo = Ctrl.Member.list[i];
					var userId = userInfo.userid;
					var userNo = userInfo.userno;

					if (cordova.platformId == "ios") {
						var data = VideoCtrl.map.get(userNo);

						if (data && !data.getVideoDisplay()) {
							userInfo.isDisplay = true;
							data.create_ios(userInfo, 'peerMedia');
							removeCnt--;
							if (removeCnt<0) break;
						}
						continue;
				   }
					// 107334653864916823691
					// $("#videoWrapper").attr("userid")
// console.log("video cnt : " + $(".videoWrap", "#videoWrapper").length );

					$(".videoWrap", "#videoWrapper").each(function(){
// console.log("$(this).attr(userid) : " + $(this).attr("userid") + ", userId : " + userId + ", display : " + $(this).css("display"));

						// attr("userid")
						if(typeof($(this).attr("userid")) != "undefined" && $(this).attr("userid") == userId && $(this).css("display") == "none"){
							if(removeCnt > 0){
								removeCnt--;
								// $(this).css("display", "block");
								var data = VideoCtrl.map.get(userNo);
								// var myDisplay = user1_video_allow == 1 ? true : false;
								if(data) data.show(true);
							}
						}
					});
				}
			}
		}
	},

	guestFullScreen : function(isFullScreen){
		if(isFullScreen){
			$("#videoContainer").addClass("video_fullsize");
		}
	},

	controlSound : function(isSoundOn, userNo, userId){
		if(VideoCtrl.map != null && VideoCtrl.map.get(userNo) != null){
			var targetVideo = VideoCtrl.map.get(userNo);
			// VideoCtrl.map.get(userNo).destroy();
			targetVideo.receive(isSoundOn, userNo, userId);
		}
	},

	// 시도한 사람이 offer
	reconnect : function(userNo, userId){
		if(!this.checkSupport()) return;

		//var url = Utils.addContext( _url("get.turn.server"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("get.turn.server");
		var params = {
			roomid : RoomSvr.roomid,
            userno1 : RoomSvr.userno,
            userno2 : userNo
		};

		Utils.request(url, "json", params, function(data){

			if(data && data.success){
				var turnInfo = data.id1;
				// 1. data find (없으면 생성)
				// 2. view find (없으면 생성)
				// 3. peerconnection 생성

				if(VideoCtrl.map != null && VideoCtrl.map.get(userNo) != null){
					RoomSvr.sendWebRtcData({ cmd : "disconnect", roomid : RoomSvr.roomid, from : RoomSvr.userid, to : userId });

					if (cordova.platformId == "android") {
						var turnSvr = turnInfo;
						var sender = RoomSvr.userid;
						var receiver = userId;
						var param = {
							offer : sender,
							answer : receiver,
							offerTurn : turnInfo,
							answerTurn : turnInfo
						};
						var isDisplay = true;

						VideoCtrl.reconnectAndroid("offer", RoomSvr.roomid, param, RoomSvr.isMC, isDisplay);
					} else {
						var videoObj = VideoCtrl.map.get(userNo);
						videoObj.initReconnect(turnInfo, userId);
					}

					RoomSvr.sendWebRtcData({ cmd : "reconnect", roomid : RoomSvr.roomid, from : RoomSvr.userid, to : userId, turnsvr:data.id2 });
				}

			}
		});
	},

	destroy : function(userNo, userId){
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

		this.updateInviteLayer();

		// 현재 풀스크린 모드일 경우
		if($("#videoContainer").hasClass("video_fullsize")){
			// 나간 사람이 full screen 유저라면..
			if(this.fullScreenUserId == userId){
				var isMax = false;
				var targetUserId = "";
				var targetUserNo = "";
				if($(".videoWrap", "#" + VideoCtrl.WRAPPER).length > 1){
					var firstUserId = $(".videoWrap", "#" + VideoCtrl.WRAPPER).eq(0).attr("userid");
					var firstUserInfo = Ctrl.Member.getUserOnline(firstUserId, "userid");
					targetUserNo = firstUserInfo.userno;
					targetUserId = firstUserInfo.userid;
					isMax = true;
				}else{
					isMax = false;
				}

				if(PacketMgr.userid == Ctrl.Member.list[0].userid){
					VideoCtrl.controlScreen(isMax, false, targetUserNo, targetUserId);
				}
			}
		}

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

		$(".btn_close", "#" + VideoCtrl.CONTAINER).unbind("click");
		$(".btn_chargeVideo", "#" + VideoCtrl.CONTAINER).unbind("click");
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
