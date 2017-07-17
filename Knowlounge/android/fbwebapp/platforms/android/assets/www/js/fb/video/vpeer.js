
(function(){

/**
 * 	 @title 	  : Web RTC PeerConnection Pool
 * 	 @date 	      : 2015.10.01
 *   @author      : kim dong hyuck
 * 	 @description : P2P Base의 영상채팅 서비스로, 기본적으로 신규 유저가 접속하면, 이미 영상을 접속한 타 유저가 Offer를 보내고, 신규로 접속한 유저가 Answer를 보내는 방식이다.
 * 					이때, 카메라를 Deny하거나 카메라가 없는경우, 신규유저는 Answer만 해주면 되므로, 영상서비스를 이용할수 있으며, Deny or Non Camera유저는
 * 					내가 접속한후 타유저가 계속 들어오는 경우 Reverse Offer를 보내서 내가 신규 접속할때처럼 Offer가 아닌 Answer만 받는 구조로 진행한다.
 *
 * 					이용케이스
 * 					1. 내영상만 보는경우 Peerconnection 객체는 만들지 앟는다.
 * 					2. Video객체 생성시 currentUser는 무조건 나이고, 같이 연결할 유저는 mappingUser에 들어간다.
 *
 */

var Video = function(containerId, idx, currentUser, mappingUser, options) {
	var container = null;
	var stream = null;
	var videoWrapper = null;
	var videoLayer = null;
	var offerStreamChecker = null;
	var offerStreamCheckCnt = 0;
	var answerStreamChecker = null;
	var answerStreamCheckCnt = 0;
	var tryCnt = 3;
	var peerConnection = null;
	var turnInfo = null;
	var mode = null;	// offer, answer
	var roomid = null;
	var offer = null;
	var answer = null;
	var options = options;
	var roomid = "";
	var checkCnt = 20;		// 30초 까지만 허용...
	var support = false;
	var reverse = false;		// deny했거나 카메라 없는경우  reverse offer 했는지 여부
	var isOfferReady = false;	// offer보낼 타이밍에 allow가 아직 안된경우 sync용
	var isHttps = true;
	var isMaster = false;
	var isDisplay = true;
	var micStatus = "";
	var iceConnectionState = ""; // checking,. connected, completed, disconnected, failed, closed
	var signalingState = "";
	var mappingUserNo = null;
	var mappingUserFindCnt = 0;

	var defaultOpts = {
	      constraints : { "audio" : true, "video" : { mandatory: { maxWidth: 640, maxHeight: 360 }} },
	      mediaopts : { mandatory : { OfferToReceiveVideo : true, OfferToReceiveAudio : true } }
	};
	var pcConstraints = {
		googImprovedWifiBwe : true,  	//	Bandwidth Estimation Algorithm을 향상시켜주는 FLAG
		googCpuOveruseDetection : true, // CPU 사용률에 따라 비디오와 오디오 품질을 낮춰주는 FLAG..
		googCpuUnderuseThreshold : 50,  // CPU 사용률 Minimum 지정 (단위: %)
	    googCpuOverUseThreshold : 80,  	// CPU 사용률 Maximum 지정 (단위: %)
	    DtlsSrtpKeyAgreement : true,
    	RtpDataChannels : false
	};

	// IOS에서 쓰는 변수
	var iosGetUserMedia;
	var iosVideoTag;

	function init(){
		if(checkPlatform() == "ios") iosGetUserMedia = new getUserMedia(null, null, null);;

		var mode = options.mode;
		if(mode == "self"){
			initMyVideo();
		}else{
			initPeerVideo();
		}
	}


	function initReconnect(turnInfo, userId){
		// 기존 peerconnection destroy
		// mapping user check 없으면 생성
		// layout check 없으면 생성
		// layout이 생성되지 않은 경우

		options.turnsvr = turnInfo;
		if(videoLayer == null){
			// mappingUser = mappingUserInfo;z
			// var mappingUserInfo = Ctrl.Member.getUserOnline(mappingUserNo, "userno");
			mappingUser = ExCall.getUser(userId, "userid");
			initPeerVideo();
		}else{
			// 레이아웃은 그려졌으나 네트웍 문제로 연결이 실패난 경우
			connect(turnInfo);
		}
	}

	// 내 영상 처리
	function initMyVideo(){
console.log("initMyVideo options.isMaster : " + options.isMaster);
		container = $("#" + containerId);

		roomid = options.roomid;
		mode = options.mode;
		stream = options.stream;
		support = options.support;
		// isMaster = options.isMaster;
		isMaster = ExCall.isMaster();
		isDisplay = options.isDisplay;

		// mappingUser가 없는 경우는 자기 자신만 비디오로 나오는 경우이다.
		var displayUser = mappingUser != null ? mappingUser : currentUser;
		displayUser.isDisplay = isDisplay;

		// connection 객체 생성
		/*if(checkPlatform() == "ios") {
			// displayUser.isDisplay = isDisplay
			iosGetUserMedia = new getUserMedia(null, null, null);;
			VideoUI.create_ios(displayUser, 'myMedia');
		} else if(checkPlatform() == ""){
			VideoUI.create(displayUser, 'myMedia');
		}
		*/

		var platformId = checkPlatform();
		var platformMethod = "create" + (platformId != "" ? "_" + platformId  : "")

		VideoUI[platformMethod](displayUser, 'myMedia');
	}

	// 타인의 영상 처리
	function initPeerVideo(){
console.log("initPeerVideo options.isMaster : " + options.isMaster);
		mappingUserNo = options.mappinguserno;
		// session이 중복발생하는 경우도 생길수 있따.
		if(typeof(mappingUser) == "undefined" || mappingUser == null) {
			mappingUser = ExCall.getUser(mappingUserNo, 'userno');
			if(mappingUser == null){
				if(mappingUserFindCnt > 1){
					mappingUserFindCnt = 0;
					return;
				}
				setTimeout(function(){
					mappingUserFindCnt++;
					initPeerVideo();
				}, "500");
				return;
			}
		}else{
			mappingUserFindCnt = 0;
		}

		container = $("#" + containerId);
		roomid = options.roomid;
		mode = options.mode;
		turnInfo = options.turnsvr;
		support = options.support;
		// isMaster = options.isMaster;
		isMaster = ExCall.isMaster(mappingUser.userid);

		isDisplay = options.isDisplay;
		micStatus = options.micStatus;

		// mappingUser가 없는 경우는 자기 자신만 비디오로 나오는 경우이다.
		Utils.logger("camera video", "영상 연결 시작 : ("+ mappingUser.usernm+")", (new Date().toString() + ", millisecond : " + new Date().getTime()));
		var displayUser = mappingUser != null ? mappingUser : currentUser;
		displayUser.isDisplay = isDisplay;

		connect(turnInfo);
		/*
		if(checkPlatform() == "ios") {
			// displayUser.isDisplay = isDisplay;
			VideoUI.create_ios(displayUser, 'peerMedia');
		} else if(checkPlatform() == ""){
			VideoUI.create(displayUser, 'peerMedia');
		}
		*/

		var platformId = checkPlatform();
		var platformMethod = "create" + (platformId != "" ? "_" + platformId  : "")
		VideoUI[platformMethod](displayUser, 'peerMedia');
	}

	function async(data){
		var cmd = data.cmd;
		var sender = data.from;
		var receiver = data.to;

		SignalReceive[cmd](data);
	}

	function connect(turnInfo){
		if(checkPlatform() == "android") return;

		if(!support) {
Utils.logger("camera video", "영상 연결 미지원: ("+ mappingUser.usernm+")", (new Date().toString() + ", millisecond : " + new Date().getTime()));
			return;
		}

		var pcOpts = {
			"iceServers" : turnInfo
		}
		$.extend(pcOpts, pcConstraints);

		peerConnection = (checkPlatform() == "ios") ? new RTCPeerConnection(pcOpts) : RTCPeerConnection(pcOpts);
		peerConnection.onaddstream = function(event) {
Utils.logger("camera video", "영상 연결 완료 : ("+ mappingUser.usernm+")", new Date().toString() + ", millisecond : " + new Date().getTime() );
// console.log("peerConnection.onaddstream !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", event);
			// video 태그에 set
			VideoUI.setRemoteVideo(event.stream);
		};

		peerConnection.onicecandidate = function(event) {
// console.log("peerConnection.onicecandidate", event);

			if(mode == "answer") {
                Callee.queue.push(event.candidate);
                Callee.sendAnswerCandidate();
            }else {
                if(event.candidate){
                    Caller.queue.push(event.candidate);
                }else {
                    Caller.isOfferWait = false;
                    Caller.sendOfferCandidate();
                }
            }
		};

		peerConnection.oniceconnectionstatechange = function(event) {

// console.log("peerConnection", peerConnection);
// console.log("peerConnection.oniceconnectionstatechange @@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ", event);
if(checkPlatform() == "" && peerConnection != null){
	// console.log("event.currentTarget.iceConnectionState @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ", event.currentTarget.iceConnectionState);
	// console.log("peerConnection.iceConnectionState @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ", peerConnection.iceConnectionState);
		var displayUser = mappingUser != null ? mappingUser : currentUser;
	// console.log("peerConnection.iceConnectionState @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ", displayUser.usernm);
}

			if(event.currentTarget && event.currentTarget.iceConnectionState == 'failed') {
				console.log('oniceconnectionstatechange connecting : ' + displayUser.usernm);
				resendIce();
			}
        };

		peerConnection.onnegotiationneeded = function(event) {
			// Utils.log("peerConnection.onnegotiationneeded", event);
		};

		peerConnection.onremovestream = function(event) {
			// Utils.log("peerConnection.onremovestream", event);
		};

		peerConnection.onsignalingstatechange = function(event) {
			// Utils.log("peerConnection.onsignalingstatechange", event);
			if (event.currentTarget) {
				iceConnectionState = event.currentTarget.iceConnectionState;
				signalingState = event.currentTarget.signalingState;
			}
			/*
             *  signalingStateChange
             *   - stable : offer, answer가 교환되지 않은 초기의 상태
             *   - have-local-offer : localDescription에 offer가 성공적으로 적용.
             *   - have-remote-offer : remoteDescription에 offer가 성공적으로 적용.
             *   - completed : 유효한 커넥션을 연결하여 더이상 다른 커넥션들을 테스트하지 않는다.
             */
		};

		if(ExCall.getDeny() || ExCall.getNoCamera()){
			if(mode == "offer") {
				var data = {
					from : currentUser.userid,
					to : mappingUser.userid
				}

				Callee.sendReverseOffer(data);
			}
		}else{
			// 이 구간에서는 이미 영상을 allow했는지만 체크한다. 아직 allow안한 대기상태라면.. 대기한다.
			// isOfferReady
			isOfferReady = true;
			// https에서는 allow가 바로 일어나므로 타이머 없이 바로 동작시킨다.
		 	if(ExCall.getAllow() ){
				// allow가 늦은경우 setMyMedia 구간에서 처리한다.
				peerConnection.addStream(ExCall.getMyStream());
				// reverse는 내가 카메라 deny거나 카메라가 없는경우 offer <-> answer관계가 reverse 되므로 offer를 날리지 않는다.
				if(mode == "offer" && !reverse) {
					Caller.sendOffer();
					isOfferReady = false;
				}
			}
		}
	}

	// allow 버튼을 누른 경우 ..
	function setMyMedia(setMyMedia, stream){

		// block이거나 카메라 없는경우 역방향으로 offer를 보낸다.
		if(reverse){
			// reverse ready 상태라면 offer를 보내준다.
			var sender = mappingUser.userid;
			var receiver = currentUser.userid;

			peerConnection.addStream(ExCall.getMyStream());
			peerConnection.createOffer(function(localSdp) {
				Caller.setLocalDescription(localSdp);
			// console.log("sendOffer data : " + JSON.stringify(data));

				ExCall.send('sdp_offer', receiver, sender, roomid, localSdp);

			}, function(e){
				// console.log("reverse send offer reject : ", e);
			}, defaultOpts.mediaopts);

		}else{
			// 이구간은 SSL처럼 바로 자동 ALLOW 동작시에만 처리한다.
			if(peerConnection != null && !ExCall.getDeny() && !ExCall.getNoCamera() && isOfferReady){
				peerConnection.addStream(stream);
				if(mode == "offer") {
					Caller.sendOffer();
				}else if(mode == "answer" && !reverse){
					// answer는 sdp_offer를 받은후에 한다.
					// Callee.sendAnswer(options);
				}
			}
		}
	}

	function getVideoDisplay(){
		return isDisplay;
	}
	/**
	 *  iceConnectionState가 failed 된 경우 재시도 하는 로직을 만든다.
	 */
	function resendIce(){
		if(mode == "offer"){
			Caller.sendOffer(true);
		}
	}

	function getMode(){
		return mode;
	}

	// 카메라를 거부하거나 없는경우 신규유저가 들어왔을때 offer <-> answer로 바꿔서 내려준다.
	function _changeReverseMode(newMode){
		// 카메라를 block 했거나 없는경우, 있는쪽에서 역으로 offer를 발생해줘야 하므로 바꿔준다.
		mode = newMode;
		reverse = true;
	}

	function checkPlatform(){
		if(typeof(cordova) == "undefined") return "";
		else return cordova.platformId;
	}

	function getMappingUser(){
		return mappingUser;
	}


	function controlSound(isMicOn, isSendPacket, userNo, userId){
		var displayMic = function(userId, isMicOn){
			$(".videoWrap", "#videoWrapper").each(function(){
				if($(this).attr("userid") == userId && $(".btn_videoMuteToggle", $(this)).get(0) != null){
					// $(".btn_videoMuteToggle", $(this)).toggleClass("mute");
					// $(".btn_videoMuteToggle", $(this)).toggleClass("mute");
					if(isMicOn){
						$(".btn_videoMuteToggle", $(this)).removeClass("mute");
					}else{
						$(".btn_videoMuteToggle", $(this)).addClass("mute");
					}
				}
			});
		}

		if(ExCall.isMyUserId(userId)){
			stream.getAudioTracks()[0].enabled = isMicOn;
			/**
			$(".videoWrap", "#videoWrapper").each(function(){
				if($(this).attr("userid") == userId){
					$(".btn_videoMuteToggle", $(this)).toggleClass("mute");
				}
			});
			**/
			displayMic(userId, isMicOn);
		}else{
		// 타인의 음성 제어라면 패킷을 보내 음소거 처리를 한다.
			displayMic(userId, isMicOn);
			// 자신의 음성은 각자 끌수 있기때문에 권한체크를 하지 않는다.
		}

		if(isSendPacket){
			ExCall.sendPacket(isMicOn, "mic", userNo, userId);
		}
	}

	function receive(action, userNo, userId){
		if(typeof(Async[action]) != "undefined" && Async[action] != null){
			Async[action](userNo, userId);
		}

		/**
		// 내영상에 대한 제어라면
		if(ExCall.isMyUserId(userId)){
			stream.getAudioTracks()[0].enabled = isSoundOn;
			$(".videoWrap", "#videoWrapper").each(function(){
				if($(this).attr("userid") == userId){
					$(".btn_videoMuteToggle", $(this)).toggleClass("mute");
				}
			});
		}else{
		// 타인의 음성 제어라면 패킷을 보내 음소거 처리를 한다.

			if(ExCall.isCreator()){
				ExCall.sendPacket(isSoundOn, "mic", userNo, userId)
			}
		}
		***/


		// 비디오 태그 음소거 방식
		// 기존 음소거 기능..
		/**
		$(".videoWrap", "#videoWrapper").each(function(){
			if($(this).attr("userid") == userId){
				var videoLayer = $("video", $(this));
				if(typeof(videoLayer.attr("src")) != "undefined"){
					videoLayer.get(0).muted = isMute;
				}
				$(".btn_videoMuteToggle", $(this)).toggleClass("mute");
			}
		});
		***/
	}

	function disConnect() {
		if(peerConnection != null) {
			if(iceConnectionState != "closed"){
				peerConnection.close();
				peerConnection = null;
			}
		}
	}

	function destroy(){
		// connection destroy
		try{
			/**
			if(peerConnection != null) {
				if(iceConnectionState != "closed"){
					peerConnection.close();
					peerConnection = null;
				}
			}
			**/
			disConnect();
			// layer 삭제
		}catch(e){
			// console.log(e);
		}

		if(videoWrapper != null) videoWrapper.remove();
	}

	// SignalReceive
	var SignalReceive = {

		//  @description sdp_offer는 callee user가 offer signal을 받았을때 호출되는 메소드.
		sdp_offer : function(options){
// console.log("[SignalReceive sdp_offer : " + JSON.stringify(options));
			if(checkPlatform() == "android") {   // Android에 대응..
				this.sdp_offer_android(options);

			} else {

				if(ExCall.getDeny() || ExCall.getNoCamera()){
					var remoteSdp = options.msg;
					// stream을 체크해서 allow하지 않으면 영상이 보이지 않는다고 노티를 보내주던가 한다.
					Callee.setRemoteDescription(new RTCSessionDescription(remoteSdp));

					Callee.sendAnswer(options);

				}else{
					/***
					 * 	Answer를 만들기전에 상대방의 remote를 먼저 셋해줘야. 무선환경에서 description이 정상적으로 세팅 된다.
					 *
					 **/
					var remoteSdp = options.msg;
					Callee.setRemoteDescription(new RTCSessionDescription(remoteSdp));
					// Callee.sendAnswer(options);
					/**
					 *  Answer시 타이머를 두는 이유는 stream을 가져오는 시점이 클라이언트가 Allow를 누른 시점이기 때문이다. 따라서 비동기적으로 일어날수 있으므로 계속 체크해준다.
					 */
					// 상용서버에서 저사양 장비가 영상로딩이 느려서 answer가 먼저 보내져서 offer유저가 영상 안나오는 버그 수정.
					answerStreamChecker = setInterval(function(){
						var clearTimer = function(){
							clearInterval(answerStreamChecker);
							answerStreamChecker = null;
							answerStreamCheckCnt = 0;
						}

						if(answerStreamCheckCnt > checkCnt){
							clearTimer();
						}

						if(ExCall.getMyStream() != null){
							clearTimer();
							Callee.sendAnswer(options);
						}
						answerStreamCheckCnt++;
					}, 500);

					/***
					if(isHttps){
						if(mode == "answer" && !reverse){
							Callee.sendAnswer(options);
						}
					}else{
						// http에서는 allow가 비동기 동작하므로 타이머로 체크한다.
						answerStreamChecker = setInterval(function(){
							var clearTimer = function(){
								clearInterval(answerStreamChecker);
								answerStreamChecker = null;
								answerStreamCheckCnt = 0;
							}

							if(answerStreamCheckCnt > checkCnt){
								clearTimer();
							}

							if(ExCall.getMyStream() != null){
								clearTimer();
								Callee.sendAnswer(options);
							}

							answerStreamCheckCnt++;

						}, 1000);
					}
					****/
				}
			}

    	},
    	sdp_offer_android : function(options){
    		// Native에 offer SDP 세팅하기..
			var params = {
				sender : options.from,
				receiver : options.to,
				sdp : options.msg.sdp
			};

			cordova.exec(function(result) {
				// console.log("cordova.exec() success.. setOfferSdp");
				// console.log(result);
			}, function(result) {
				// console.log('setOfferSdp : ' + JSON.stringify(result));
			}, "VideoPlugin", "setOfferSdp", [params]);

    	},
    	//  @description sdp_answer는 offer를 보낸사람이 answer signal 을 받았을때 호출되는 곳이다. 즉 caller user가 answer 응답을 받는 메소드.
    	sdp_answer : function(options){
// console.log("[Video SignalReceive sdp_answer] offer : (" + sender + ") 원격 Description을 set했습니다.");
			if(checkPlatform() == "android") {   // Android에 대응..
				this.sdp_answer_android(options);
			} else {

				var sender = options.from;
				var receiver = options.to;
				var remoteSdp = options.msg;

	   			Caller.setRemoteDescription(new RTCSessionDescription(remoteSdp));
			}
    	},

    	sdp_answer_android : function(options){
    		var sender = options.from;
			var receiver = options.to;
			var remoteSdp = options.msg;

			// Native에 answer SDP 세팅하기..
			var params = {
				sender : options.from,
				receiver : options.to,
				sdp : options.msg.sdp
			};

			cordova.exec(function(result) {
				// console.log("cordova.exec() success.. setAnswerSdp");
				// console.log(result);
			}, function(result) {
				// console.log('setAnswerSdp : ' + JSON.stringify(result));
			}, "VideoPlugin", "setAnswerSdp", [params]);
    	},

    	// @description ice candidate 메시지를 받았을때 호출되는 메소드
    	sdp_candidate : function(options){	// candidat
// console.log("[SignalReceive (받음) mode : " + mode + ",  원격(" + sender + ") sdp_candidate : " + JSON.stringify(options));

			if(checkPlatform() == "android") {   // Android에 대응..
				this.sdp_candidate_android(options);
			} else {
				var sender = options.from;
				var receiver = options.to;
				var sdpCandidate = options.msg;
Utils.logger("[VideoCtrl SignalReceive sdp_candidate] 원격(" + sender + ")으로 받은 ice를 추가합니다. sdp_candidate : " + JSON.stringify(options));
				Callee.setSdpCandidate(sender, sdpCandidate);
			}
    	},

    	sdp_candidate_android : function(options){
    		var sender = options.from;
			var receiver = options.to;
			var sdpCandidate = options.msg;

    		var cordovaParams = {
				opponentid : sender,
				sdpcandidate : sdpCandidate
			};

			cordova.exec(function(result) {
				// Utils.logger("cordova.exec() success.. setRemoteCandidate");
			}, function(result) {
				//alert('setRemoteCandidate ' + JSON.stringify(result));
			}, "VideoPlugin", "setRemoteCandidate", [cordovaParams]);

    	},

    	stream_allow : function(options){
    		// 무선에서 새로운 유저가 접속한 경우 offer 보내는 쪽의 stream이 answer보내는 쪽의 stream보다 나중에 addStream이 되어야 한다 따라서 별도 패킷으로 맞춘다.
    		// Utils.logger("stream_allow enter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			peerConnection.removeStream(ExCall.getMyStream());
			peerConnection.addStream(ExCall.getMyStream());
    	},

    	reverse_offer : function(data){
    		if(checkPlatform() == "android") {   // Android에 대응..
    			this.reverse_offer_android(data);
    		}else{
    			_changeReverseMode("offer");
    			Caller.sendOffer();
    		}
    	},

    	reverse_offer_android : function(data){

    		_changeReverseMode("offer");

			var params = { opponent : data.from };
			cordova.exec(function(result) {
				// Utils.logger("cordova.exec() success.. requestReverseOffer");
			}, function(result) {
				// Utils.logger('requestReverseOffer Error : ' + JSON.stringify(result));
			}, "VideoPlugin", "requestReverseOffer", [params]);

    	},

    	disconnect : function(data){
			if(checkPlatform() == "android") {
				this.disconnect_android(data);
			} else {
    			disConnect();
			}
    	},

		disconnect_android : function(data) {
			var userId = data.from;
			var cordovaParams = {
				userid    : userId,
				reconnect : true
			};
			cordova.exec(function(result) {
				console.log("cordova.exec() success.. disconnectPeer");
			}, function(result) {
				console.log("disconnectPeer error : " + JSON.stringify(result));
			}, "VideoPlugin", "disconnectPeer", [cordovaParams]);
		},

    	reconnect : function(data){
			if(checkPlatform() == "android") {
				this.reconnect_android(data);
			} else {
    			initReconnect(data.turnsvr, data.from);
			}
    	},

		reconnect_android : function(data) {
			var turnInfo = data.turnsvr;
			var sender = data.from;
			var receiver = data.to;
			var param = {
				offer : sender,
				answer : receiver,
				offerTurn : turnInfo,
				answerTurn : turnInfo
			};
			var isDisplay = true;

			VideoCtrl.reconnectAndroid("answer", RoomSvr.roomid, param, RoomSvr.isMC, isDisplay);

			// setTimeout(function(){
			// 	VideoCtrl.reconnectAndroid("answer", RoomSvr.roomid, param, RoomSvr.isMC, isDisplay);
			// }, "2000");
		}
	};

	// 영상을 보내는 유저.. offer를 보내는 사람
	var Caller = {
		queue : [],
		isOfferWait : true,
		setLocalDescription : function(localSdp){
			try {
				// Utils.logger("Caller.setLocalDescription");
				//Utils.logger(peerConnection.localDescription, peerConnection.remoteDescription);
				peerConnection.setLocalDescription(localSdp);
			} catch(e) {
				// Utils.logger(e);
				console.log(e);
			}
		},
		setRemoteDescription : function(remoteSdp){
			try {
				// Utils.logger("Caller.setRemoteDescription");
				peerConnection.setRemoteDescription(remoteSdp);

//-----------
			} catch(e) {
				console.log(e);
			}
		},

		sendOffer : function(iceRestart){
			/**
			var sender = data.from;
			var receiver = data.to;
			**/
			var sender = currentUser.userid;
			var receiver = mappingUser.userid;

			if(iceRestart){
				/*offerOptions.iceRestart = true;
				peerConnection.localDescription = null;
				peerConnection.remoteDescription = null;		*/
				return;
			}

			peerConnection.createOffer(function(localSdp) {
				Caller.setLocalDescription(localSdp);
// Utils.logger("sendOffer data : " + JSON.stringify(data));

				ExCall.send('sdp_offer', sender, receiver, roomid, localSdp);

			}, function(e){
				// Utils.logger("sendOffer reject", e)
			}, defaultOpts.mediaopts);
		},


		sendOfferCandidate : function(){
			if(Caller.isOfferWait) return;

			var sender = currentUser.userid;
			var receiver = mappingUser.userid;

			for(; Caller.queue.length > 0;) {
// Utils.logger("[sendOfferCandidate (전송) mode : " + mode);
// Utils.logger(candidate);
				var candidate = Caller.queue.shift();
				ExCall.send('sdp_candidate', sender, receiver, roomid, candidate);
			}

			Caller.isOfferWait = true;
		}

	}

	// 영상을 받는 쪽 유저.. answer보내는 사람
	var Callee = {
		// answer의 ice candidate가 setRemoteDescrition보다 먼저 실행되면 오류 난다. 따라서 받는쪽에서 큐 처리 한다.
		queue : [],
		isAnswerWait : true,
		setLocalDescription : function(localSdp){
			try {
				peerConnection.setLocalDescription(localSdp);

			} catch(e) {
				console.log(e);
			}
		},
		setRemoteDescription : function(remoteSdp){
			try {
				peerConnection.setRemoteDescription(remoteSdp);
			} catch(e) {
				// Utils.logger(e);
			}
		},

		sendAnswer : function(data){
			var sender = data.from;
			var receiver = data.to;

			/***
				var remoteSdp = data.msg;
				// Utils.logger("sendAnswer : " + JSON.stringify(data) );
				// stream을 체크해서 allow하지 않으면 영상이 보이지 않는다고 노티를 보내주던가 한다.
				Callee.setRemoteDescription(new RTCSessionDescription(remoteSdp));
			***/
			if(peerConnection == null){
				console.log("peerconnection not established");
				return;
			}

			Callee.isAnswerWait = false;
			peerConnection.createAnswer(function(localSdp) {
				Callee.setLocalDescription(localSdp);

				ExCall.send('sdp_answer', receiver, sender, roomid, localSdp);

				// answer wait 상태
				Callee.isAnswerWait = true;
			},
			function(error) {
			}, defaultOpts.mediaopts);

		},

		sendAnswerCandidate : function(){
			var sender = currentUser.userid;
			var receiver = mappingUser.userid;

			for(; Callee.queue.length > 0;) {
				var candidate = Callee.queue.shift();
// Utils.logger("[sendAnswerCandidate (전송) mode : " + mode);
// Utils.logger(candidate);

				ExCall.send('sdp_candidate', sender, receiver, roomid, candidate);
			}
		},

		setSdpCandidate : function(sender, sdpCandidate){
			try {
				peerConnection.addIceCandidate(new RTCIceCandidate(sdpCandidate));
			} catch(e) {
				// Utils.logger(e);
			}
		},

		sendStreamAllow : function(data){
			var sender = data.from;
			var receiver = data.to;

			ExCall.send('stream_allow', sender, receiver, roomid);
		},

		sendReverseOffer : function(data){
			// reverse offer------
			var sender = data.from;
			var receiver = data.to;

			ExCall.send('reverse_offer', sender, receiver, roomid);

			_changeReverseMode("answer");
		}
	}

	// 외부로 시그널 보낼때나 사용자 정보 조회시 사용
	var ExCall = {
		send : function(cmd, sender, receiver, roomid, sdp){
			// send signal
			RoomSvr.sendWebRtcData({ cmd : cmd, from : sender, to : receiver, roomid : roomid, msg : sdp });
		},
		getDeny : function(){
			return VideoCtrl.deny;
		},
		getNoCamera : function(){
			return VideoCtrl.noCamera;
		},
		getAllow : function(){	// deny, nocamera 외에 별도의 오류가 있거나 대기상태일수도 있으므로 allow를 별도 체크
			return VideoCtrl.allow;
		},
		getMyStream : function(){
			return VideoCtrl.myStream;
		},
		noti : function(){
			VideoCtrl.noti();
		},
		isMyUserId : function(userId){
			return PacketMgr.userid == userId;
		},
		isOnlyTeacherVCam : function(){
			return PacketMgr.isOnlyTeacherVCam;
		},
		isCreator : function(userId){
			if(typeof(userId) == "undefined") return PacketMgr.isCreator;

			return (PacketMgr.creatorid == userId);
		},
		isMaster : function(userId){
			if(typeof(userId) == "undefined") return PacketMgr.isMC;

			return (PacketMgr.masterid == userId);
		},
		sendPacket : function(isSoundOn, action, userNo, userId){
			action += (isSoundOn ? "_on" : "_off");
			PacketMgr.Master.vCam(action, userNo, userId);
		},
		getIdx : function(userId){
			// if(!Ctrl._checkAuth(true)) return;
			// online_list
			if(mode == "offer"){
				return $("li", "#online_list").length;
			}

			if(PacketMgr.masterid == userId){
				return 0;
			}else if(PacketMgr.creatorid == userId){
				// 마스터와 같지 않으면서 creator인 경우
				return 1;
			}else{
				var idx = -1;
				$("li", "#online_list").each(function(){
					if($(this).attr("userid") == userId){
						idx = $(this).index();
					}
				});
				return idx;
			}
		},

		sendReconnect : function(targetUserNo, targetUserId){
			VideoCtrl.reconnect(targetUserNo, targetUserId);
		},

		getUser : function(key, type){
			return Ctrl.Member.getUserOnline(key, type);
		}
	}

	var VideoUI = {
		basePos : [20, 0],
		getPositionIdx : function(){
			var posIdx = -1;
			var wrapper =  $(".cam_wrapper", container).get(0);
			if(wrapper != null){

				var existList = [];
				$(".cam_wrapper", container).each(function(layerIndex){
					var idx = parseInt($(this).attr("idx"));
					existList.push(idx);
				})

				for(var i=2; i<20; i++){
					if(existList.indexOf(i) > -1){
						continue;
					}else{
						posIdx = i;
						break;
					}
				}
			}

			if(posIdx < 0) posIdx = idx;

			return posIdx;
		},

		create : function(displayUser, from){
			var userId = displayUser.userid;
			var userNo = displayUser.userno;
			var userNm = displayUser.usernm;
			var userToken = displayUser.token;
			if(userNo == userId){
				userNm += "&nbsp;(" + _msg("guest") + ")";
			}

			var isMic = (mode == "self") ? "mic" : "speaker";
// 방 개설자만 sound 제어 버튼이 노출되고 나머지는 자기 자기 영상에 대해서만 노출된다.
			var soundLayer = (ExCall.isCreator() || from == "myMedia") ? "<a class=\"btn_videoMuteToggle "+ isMic +"\" style=\"\"></a>" : "";
			var reconnectLayer = (from != "myMedia") ? "<a class=\"btn_videoReconnect\" title=\"\"></a>" : "";

console.log("create userid : " + userId + ", isMaster : " + isMaster);

			var masterClass = isMaster ? "user_master" : "";
			var idx = ExCall.getIdx(userId);

			// 2016.11.21 내 비디오에 대해서
			var muted = (mode == "self") ? "muted" : "";

			// 2016.06.23 음성 muted 제거
			var html = "<div class=\"videoWrap "+masterClass+"\" id=\"video_"+userToken+"\" userid=\""+ userId +"\" idx=\""+idx+"\">\
							<video type=\"video/mp4\" autoplay "+muted+"></video>\
							<div class=\"videoUserWrap\">\
								"+soundLayer+"\
								"+reconnectLayer+"\
								<p class=\"username\">"+userNm+"</p>\
							</div>\
						</div>";

			if(isMaster){
				$("#videoWrapper").prepend(html);
			}else{
// layer ordering ...........
				if(mode == "offer"){	// 다른 유저가 들어온 경우로 무조건 맨뒤에 insert 한다.
					$(html).insertBefore("#videoInvite");
				}else{
					var beforeLayer = null;
					$(".videoWrap", "#videoWrapper").each(function(){
						if($(this).attr("id") != "videoInvite"){
							var layerIdx = parseInt($(this).attr("idx"));
							if(idx > layerIdx){
								beforeLayer = $(this);
							}
						}
					});

					if(beforeLayer != null){
						$(html).insertAfter(beforeLayer);
					}else{
						// 레이어가 있는 상태에서 그거보다 큰 녀석이 있으면 before로 넣어야 한다.
						if($(".videoWrap", "#videoWrapper").length > 1){
							$(html).insertBefore( $(".videoWrap", "#videoWrapper").eq(0));
						}else{
							$(html).insertBefore("#videoInvite");
						}
					}
				}
			}

			videoWrapper = $("#video_" + userToken);
			videoLayer = $("video", videoWrapper);

			// 선생님만 캠 옵션이 켜있는 경우
			if(ExCall.isOnlyTeacherVCam()){
				// 내 영상이 생성자인 경우를 제외하고 모두  mic off
				if(displayUser.usertype == '2' && !ExCall.isCreator(displayUser.userid)){
					// $(".btn_videoMuteToggle", videoWrapper).addClass("mute");
					VideoUI.drawMuteIcon( (micStatus == '1' ? true : false));
					if(mode == "self" && stream != null && stream.getAudioTracks() != null && micStatus != '1'){
						stream.getAudioTracks()[0].enabled = false;
						$(".btn_videoMuteToggle", videoWrapper).addClass("mute");
					}
				}
			}else{
				// 음소거 상태인 유저는 유저만 음소거 아이콘 표시
			 	if(micStatus == '0'){
					VideoUI.drawMuteIcon(false);
				}
			}

			if(!support){
				/*	$(".cam_volume_box", videoWrapper).hide();
					$(".cam_circle_menu", videoWrapper).hide();
					$(videoWrapper).css("cursor", "default");
				*/
				videoWrapper.click(function(){
					ExCall.noti();
				});

				// 자기꺼 일때
				$(".btn_videoMuteToggle", videoWrapper).hide();

				if(!isDisplay) VideoUI.hide();

				return;
			}

			try{

				if(from == "myMedia" && stream == null){
					// stream이 null이면 음소거 감춤
					$(".btn_videoMuteToggle", videoWrapper).hide();
				}

				videoWrapper.dblclick(function(){
					var src = $("video", videoWrapper).attr("src");
					if(typeof(src) == "undefined" || src == null || src == "") return;

					VideoCtrl.controlScreen(true, true, userNo, userId);
				});

				$(".btn_videoMuteToggle", videoWrapper).click(function(){
					/**
						if(typeof(videoLayer.attr("src")) != "undefined"){
							videoLayer.get(0).muted = !$(this).hasClass("mute");
						}
						$(this).toggleClass("mute");
					***/
					controlSound($(this).hasClass("mute"), true, userNo, userId);
				});


				if(mode != "self"){
					$(".btn_videoReconnect", videoWrapper).click(function(){ // VideoCtrl.reconnect(userNo, userId);

						ExCall.sendReconnect(userNo, userId);
					});
				}


				// init peer로 들어온 메소드는 비디오가 없으므로 레이아웃만 생성한다.
				// mymedia
				if(stream != null){
					// 자기꺼 일때
					videoLayer.attr("src", (window.URL.createObjectURL(stream) || stream));
					if(!isDisplay){
						VideoUI.controlTracks(false);
						VideoUI.hide();
					}
				}else{
					if(!isDisplay) VideoUI.hide();
				}

			}catch(e){
				// Utils.logger(e);
			}
			// isDiaplay = true;
		},

		create_ios : function(displayUser, from){
			var userId = displayUser.userid;
			var userNm = displayUser.usernm;
			var userToken = displayUser.token;

			iosVideoTag = document.createElement('video');
			iosVideoTag.autoplay = displayUser.isDisplay;
			iosVideoTag.id = displayUser.userid;
			iosVideoTag.hidden = true;

			document.body.appendChild(iosVideoTag);
			iosGetUserMedia.addVideoTag_hg(iosVideoTag);
			videoMapForiOS.put(displayUser.userno,iosVideoTag);
			cordova.exec(null, null, "WebRTCPlugin", "addVideoview", [displayUser.userid, displayUser.isDisplay]);
			iosVideoTag.src = URL.createObjectURL(stream);

			if(!support){
				/*	$(".cam_volume_box", videoWrapper).hide();
					$(".cam_circle_menu", videoWrapper).hide();
					$(videoWrapper).css("cursor", "default");
				*/
				videoWrapper.click(function(){
					ExCall.noti();
				});
				if(!isDisplay) VideoUI.hide();

				return;
			}

			try {
				/*  2016.04.26 변경
					this.setCircleMenu(userToken, 40, 70, 0, 90);
					this.setVolumeSlider(userToken);
					this.setEvent(userToken);
				*/

				// init peer로 들어온 메소드는 비디오가 없으므로 레이아웃만 생성한다.
				// mymedia
				if(stream != null) {
					videoLayer.attr("src", (window.URL.createObjectURL(stream) || stream));

					if(!isDisplay){
						// stream은 track의 enabled로 제어할수 있다.
						stream.getVideoTracks()[0].enabled = false;
						VideoUI.hide();
					}
				} else {
					// peerMedia - limited
					if(!isDisplay) VideoUI.hide();
				}

			}catch(e){
				// Utils.logger(e);
			}
		},

		create_android : function(displayUser, from){
			// android no action
		},

		hide : function(){
			videoLayer.parent().hide();
			isDisplay = false;
			// videoLayer.parent().css("visibility", "hidden");
		},

		show : function(streamEnable){
			if(streamEnable && stream != null){
				// stream.getVideoTracks()[0].enabled = true;
				VideoUI.controlTracks(true);
			}

			videoLayer.parent().show();
			isDisplay = true;
			// videoLayer.parent().css("visibility", "visible");
		},

		setEvent : function(userToken){

			// drag drop
			/*videoWrapper.draggable({
				containment : $('#contsWrapper'),
				scroll : false
			});

			// change border color
			$(".btn_camcolor", videoWrapper).click(function(){
				var idx = $(".btn_camcolor", videoWrapper).index(this);
				VideoUI.changeBorderColor(userToken, (idx + 1));
			})

			$(".btn_volume", videoWrapper).click(function(){
				VideoUI.toggleVolumeBar(userToken);
			})

			$(".btn_camsz", videoWrapper).click(function(){
				var idx = $(".btn_camsz", videoWrapper).index(this);
				var num = 3 - idx;

				$(videoWrapper).attr("class", "cam_wrapper cam_container" + num);
			})

			$(".btn_vtoggle", videoWrapper).click(function(){
				if( $(this).hasClass("btn_campause")){
					VideoUI.pause();
					$(this).removeClass("btn_campause").addClass("btn_camplay");
					$(this).attr("title", _msg("video.btn.play"));

				}else{
					VideoUI.play();
					$(this).removeClass("btn_camplay").addClass("btn_campause");
					$(this).attr("title", _msg("video.btn.pause"));
				}
			});
			*/
		},

		changeVideoSize : function(userKey, num) {
			/*videoWrapper.attr("class", "cam_wrapper cam_container" + num);*/
		},

		changeBorderColor : function(userKey, num) {
			/*$(".cam_box", videoWrapper).removeClass().addClass("cam_box border" + num);*/
		},

		setCircleMenu : function(userId, diameter, radius, startAngle, endAngle) {
			/*
			$("ul.cam_circle_menu", videoWrapper).circleMenu({
				open : function(evt) {
					$(evt.currentTarget).find(".sub_circle").each(function(){
						if($(this).attr("id") == $(evt.target).attr("id")) {
							return true;
						} else {
							$(this).circleMenu("close");
						}
					});
					if($(evt.target).hasClass("sub_circle")){
						$(".cam_circle_menu").addClass("on");
					}
				},
				close : function(evt) {
					$(".cam_circle_menu").removeClass("on");
				},
				depth:1,
				item_diameter : diameter,
				circle_radius : radius, // 메뉴가 펼쳐지는 너비
				trigger : 'click', // 이벤트 옵션 (hover, click)
				speed: 200,
				delay: 500,
				angle : {
					start: startAngle,
					end: endAngle
				},
				depth2 : {  // 2번째 depth에 대한 angle, diameter 설정..
					item_diameter: 25,
					circle_radius : 70,
					angle5 : {    // 자식노드가 5개인 노드의 angle 설정
						start : -70,
						end : 50
					},
					angle4 : {    // 자식노드가 4개인 노드의 angle 설정
						start : -10,
						end : 100
					}
				}
			});
			*/
		},
		setVolumeSlider : function(userKey){
			/*
			$(".opt_slider3", videoWrapper).slider({
			      orientation: "vertical",
			      min : 0,
			      max : 100,
			      value : 0,
			      slide : function(event, ui) {
			    	  // 2015.09.24 영상 시작시 소리 계속 들려서 muted 처리
			    	  videoLayer.get(0).muted = false;

					  var val = ui.value / 100;
					  videoLayer.prop("volume", val);

			    	  if(ui.value == 100) {
			    		  $("#video_" + userKey + " > .cam_volume_box > a").removeClass();
				    	  $("#video_" + userKey + " > .cam_volume_box > a").addClass("btn_volume volume04");
			    	  }else if(ui.value >= 66) {
			    		  $("#video_" + userKey + " > .cam_volume_box > a").removeClass();
				    	  $("#video_" + userKey + " > .cam_volume_box > a").addClass("btn_volume volume03");
			    	  } else if(ui.value > 0) {
			    		  $("#video_" + userKey + " > .cam_volume_box > a").removeClass();
				    	  $("#video_" + userKey + " > .cam_volume_box > a").addClass("btn_volume volume02");
			    	  } else if(ui.value == 0) {
			    		  $("#video_" + userKey + " > .cam_volume_box > a").removeClass();
				    	  $("#video_" + userKey + " > .cam_volume_box > a").addClass("btn_volume volume01");
			    	  }
			      }
			});
			*/
		},

		toggleVolumeBar : function(userKey) {
			var displayStr = $(".opt_slider3Wrap", videoWrapper).css("display")
			if(displayStr == "none"){
				//$(".opt_slider3Wrap", "#video_" + userKey).css("display", "block")
				$(".opt_slider3Wrap", videoWrapper).fadeIn("fast");
			} else {
				//$(".opt_slider3Wrap", "#video_" + userKey).css("display", "none");
				$(".opt_slider3Wrap", videoWrapper).fadeOut("fast");
			}
		},

		play : function(){
			// addsteam
			// 내가 아니면 -- offer 보냄 x
			if(videoLayer.get(0) != null) videoLayer.get(0).play();
		},

		pause : function(){
			// removestream
			if(videoLayer.get(0) != null) videoLayer.get(0).pause();
		},

		setRemoteVideo : function(stream){
            if(checkPlatform() == "ios")
                iosVideoTag.src = URL.createObjectURL(stream);
			else {
				videoLayer.attr("src", ((window.URL || window.webkitURL).createObjectURL(stream) || stream));
				videoLayer.get(0).autoplay = true;
			}
		},

		controlTracks : function(enabled){
			var tracks = stream.getTracks();
			var len = tracks == null ? 0 : tracks.length;
			for(var i=0; i<len; i++){
				var track = tracks[i];
				track.enabled = enabled;
			}
		},

		drawMuteIcon : function(isMicOn){
			if(isMicOn){
				$(".btn_videoMuteToggle", videoWrapper).removeClass("mute");
			}else{
				$(".btn_videoMuteToggle", videoWrapper).addClass("mute");
			}

		},

		// 써클메뉴 이벤트 해제
		destroyCircleMenu : function () {
			// // Utils.logger("circleMenu(destroy)")
			$('ul.cam_circle_menu', videoWrapper).circleMenu("destroy");
		},

		destroy : function(){
			// videoWrapper.draggable("destroy");
			if(!support) $("#video_" + userToken).unbind("click");

			$(".btn_videoMuteToggle", videoWrapper).unbind("click");
		}
	}

	var Async = {
		mic_on : function(userNo, userId){
			controlSound(true, false, userNo, userId);
		},
		mic_off : function(userNo, userId){
			controlSound(false, false, userNo, userId);
		}
	}

	init();

	//
	return {
	    init : init,
	    initReconnect : initReconnect,
        async : async,
        setMyMedia : setMyMedia,
        // 외부에서 UI만 생성 할때 사용(limit sync 용)
        create : VideoUI.create,
		create_ios : VideoUI.create_ios,
        hide : VideoUI.hide,
        show : VideoUI.show,
        getVideoDisplay : getVideoDisplay,
        getMode : getMode,
        getMappingUser : getMappingUser,
        resendIce : resendIce,
        // isStream : isStream,	// stream하고 있는 레이어인지 체크

        receive : receive,	// sound on/off
        disConnect : disConnect,
        destroy : destroy
    };
}

window.Video = Video;

})();
