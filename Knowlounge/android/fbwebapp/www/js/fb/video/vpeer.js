
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
        //var container = null;
        var stream = null;
        var videoWrapper = null;
        var videoLayer = null;
        var offerStreamChecker = null;
        var offerStreamCheckCnt = 0;
        var answerStreamChecker = null;
        var answerStreamCheckCnt = 0;
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
        var isHttps = false;
        var tempGetUserMedia;
        if(cordova.platformId == "ios")
            tempGetUserMedia = new getUserMedia(null, null, null);
        var tempVideoTag;
        
        function init(){
            isHttps = location.href.indexOf("https") > -1 ? true : false;
            var mode = options.mode;
            console.log("[Video.init()] mode : " + mode);
            
            if(mode == "self"){
                initMyVideo();
            }else{
                initPeerVideo();
            }
        }
        
        // 내 영상 처리
        function initMyVideo(){
            var defaultOpts = {
                constraints : { "audio" : true, "video" : { mandatory: { maxWidth: 640, maxHeight: 360 }} },
                mediaopts : { mandatory : { offerToReceiveVideo : true, offerToReceiveAudio : true } }
            };
            
            //container = $("#" + containerId);
            
            roomid = options.roomid;
            mode = options.mode;
            stream = options.stream;
            support = options.support;
            
            $.extend(defaultOpts, options);
            
            // mappingUser가 없는 경우는 자기 자신만 비디오로 나오는 경우이다.
            var displayUser = mappingUser != null ? mappingUser : currentUser;
            // connection 객체 생성
            if(cordova.platformId == "ios") {
                VideoUI.create(displayUser);  // 앱버전에서는 제외.. UI는 전부 Native로 처리함 - 2016.02.01
            }
        }
        
        // 타인의 영상 처리
        function initPeerVideo(){
            // session이 중복발생하는 경우도 생길수 있따.
            console.log("[vpeer / initPeerVideo]");
            console.log("mappingUser : " + JSON.stringify(mappingUser) + ", currentUser : " + JSON.stringify(currentUser));
            
            if(typeof(mappingUser) == "undefined" || mappingUser == null) return;
            
            var defaultOpts = {
                constraints : { "audio" : true, "video" : { mandatory: { maxWidth: 640, maxHeight: 360 }} },
                support : { mandatory : { offerToReceiveVideo : true, offerToReceiveAudio : true } }
            };
            //container = $("#" + containerId);
            roomid = options.roomid;
            mode = options.mode;
            turnInfo = options.turnsvr;
            support = options.support;
            
            $.extend(defaultOpts, options);
            
            // mappingUser가 없는 경우는 자기 자신만 비디오로 나오는 경우이다.
            var displayUser = mappingUser != null ? mappingUser : currentUser;
            connect(turnInfo);
            
            if(cordova.platformId == "ios") {
                VideoUI.create(displayUser);  // 앱버전에서는 제외.. UI는 전부 Native로 처리함 - 2016.02.01
            }
            
        }
        function async(data){
            var cmd = data.cmd;
            var sender = data.from;
            var receiver = data.to;
            
            SignalReceive[cmd](data);
        }
        
        function connect(turnInfo){
            
            if(!support) return;
            
            if(cordova.platformId == "ios") {
                peerConnection = new RTCPeerConnection({
                    "iceServers" : turnInfo
                });
                
                peerConnection.onaddstream = function(event) {
                    Utils.log("peerConnection.onaddstream !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", event);
                    VideoUI.setRemoteVideo(event.stream);
                    // TODO : navtie에 스트림 데이터 전달..
                    
                };
                
                peerConnection.onicecandidate = function(event) {
                    Utils.log("peerConnection.onicecandidate", event);
                    if(event.candidate) {
                        // candidateQueue.push(event.candidate);
                        var candidateQueue = (mode == "offer") ? Caller.queue : Callee.queue;
                        candidateQueue.push(event.candidate);
                    }
                    
                    if(mode == "answer"){
                        Callee.sendAnswerCandidate();
                        console.log("Callee.sendAnswerCandidate()");
                    }
                    
                    /*
                     if(event.currentTarget.iceGatheringState === 'complete') {
                     
                     
                     if(mode == "offer"){
                     // Caller.sendOfferCandidate();
                     }else{
                     Callee.sendAnswerCandidate();
                     }
                     
                     
                     if(mode == "answer"){
                     Callee.sendAnswerCandidate();
                     }
                     }*/
                };
                
                peerConnection.oniceconnectionstatechange = function(event) {
                    console.log(event);
                }
                
                peerConnection.onsignalingstatechange = function(event) {
                    Utils.log("[VideoCtrl] (" + mappingUser.userid + ") Signaling 상태 : " + event.currentTarget.signalingState);
                    /*
                     *  signalingStateChange
                     *   - stable : offer, answer가 교환되지 않은 초기의 상태
                     *   - have-local-offer : localDescription에 offer가 성공적으로 적용.
                     *   - have-remote-offer : remoteDescription에 offer가 성공적으로 적용.
                     *   - completed : 유효한 커넥션을 연결하여 더이상 다른 커넥션들을 테스트하지 않는다.
                     */
                };
                
                peerConnection.onnegotiationneeded = function(event) {
                    Utils.log("peerConnection.onnegotiationneeded", event);
                };
                
                peerConnection.onremovestream = function(event) {
                    Utils.log("peerConnection.onremovestream", event);
                };
                
                peerConnection.onsignalingstatechange = function(event) {
                    Utils.log("peerConnection.onsignalingstatechange", event);
                    
                    /*
                     *  signalingStateChange
                     *   - stable : offer, answer가 교환되지 않은 초기의 상태
                     *   - have-local-offer : localDescription에 offer가 성공적으로 적용.
                     *   - have-remote-offer : remoteDescription에 offer가 성공적으로 적용.
                     *   - completed : 유효한 커넥션을 연결하여 더이상 다른 커넥션들을 테스트하지 않는다.
                     */
                };
                
                peerConnection.onstatechange = function(event) {
                    Utils.log("peerConnection.onstatechange event.currentTarget.readyState : " + event.currentTarget.readyState);
                    Utils.log("peerConnection.onstatechange event.currentTarget.iceState : " + event.currentTarget.iceState);
                    
                };
            }
            
            if(ExCall.getDeny() || ExCall.getNoCamera()){
                console.log("[vpeer / connect] 영상 거부 혹은 카메라가 없습니다.");
                if(mode == "offer") {
                    var data = {
                        from : currentUser.userid,
                        to : mappingUser.userid
                    }
                    
                    Callee.sendReverseOfferSignal(data);
                }
                
            }else{
                console.log("[vpeer / connect] 영상을 정상적으로 이용할 수 있는 상태입니다.");
                // 이 구간에서는 이미 영상을 allow했는지만 체크한다. 아직 allow안한 대기상태라면.. 대기한다.
                // isOfferReady
                isOfferReady = true;
                
                
                if(ExCall.getMyStream() != null){
                    peerConnection.addStream(ExCall.getMyStream());
                    
                    // reverse는 내가 카메라 deny거나 카메라가 없는경우 offer <-> answer관계가 reverse 되므로 offer를 날리지 않는다.
                    if(mode == "offer" && !reverse) {
                        var data = {
                            from : currentUser.userid,
                            to : mappingUser.userid
                        }
                        
                        Caller.sendOffer(data);
                    }
                }
                
                // https에서는 allow가 바로 일어나므로 타이머 없이 바로 동작시킨다.
                /*
                 if(isHttps){
                 if(ExCall.getAllow() ){
                 // allow가 늦은경우 setMyMedia 구간에서 처리한다.
                 peerConnection.addStream(ExCall.getMyStream());
                 
                 // reverse는 내가 카메라 deny거나 카메라가 없는경우 offer <-> answer관계가 reverse 되므로 offer를 날리지 않는다.
                 if(mode == "offer" && !reverse) {
                 Caller.sendReverseOffer();
                 }
                 }
                 
                 }else{
                 
                 // add stream...
                 offerStreamChecker = setInterval(function(){
                 
                 var clearTimer = function(){
                 clearInterval(offerStreamChecker);
                 offerStreamChecker = null;
                 offerStreamCheckCnt = 0;
                 }
                 
                 // deny 상태라면 offer 안보낸다.
                 if(offerStreamCheckCnt > checkCnt){
                 clearTimer();
                 }
                 
                 if(ExCall.getMyStream() != null){
                 clearTimer();
                 
                 peerConnection.addStream(ExCall.getMyStream());
                 
                 // reverse는 내가 카메라 deny거나 카메라가 없는경우 offer <-> answer관계가 reverse 되므로 offer를 날리지 않는다.
                 if(mode == "offer" && !reverse) {
                 var data = {
                 from : currentUser.userid,
                 to : mappingUser.userid
                 }
                 
                 Caller.sendOffer(data);
                 }
                 }
                 
                 offerStreamCheckCnt++;
                 
                 }, 1000);
                 }*/
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
                    // Utils.log("sendOffer data : " + JSON.stringify(data));
                    
                    ExCall.send('sdp_offer', receiver, sender, roomid, localSdp);
                    
                    Caller.isOfferWait = false;
                    
                    // offer candidate 전송 동기화
                    Caller.sendOfferCandidate();
                    
                },
                                           function(error) {
                                               // Utils.log("peerConnection.createOffer failed", error);
                                           }, options.constraints);
                
            }else{
                // 이구간은 SSL처럼 바로 자동 ALLOW 동작시에만 처리한다.
                if(peerConnection != null && !ExCall.getDeny() && !ExCall.getNoCamera() && isOfferReady){
                    peerConnection.addStream(stream);
                    if(mode == "offer") {
                        Caller.sendReverseOffer();
                    }else if(mode == "answer" && !reverse){
                        Callee.sendAnswer(options);
                    }
                }
            }
        }
        
        
        function destroy(){
            // connection destroy
            try{
                if(peerConnection != null) peerConnection.close();
                // layer 삭제
                videoWrapper.remove();
                
            }catch(e){
                Utils.log(e);
            }
        }
        
        
        // 카메라를 거부하거나 없는경우 신규유저가 들어왔을때 offer <-> answer로 바꿔서 내려준다.
        function _changeReverseMode(newMode){
            // 카메라를 block 했거나 없는경우, 있는쪽에서 역으로 offer를 발생해줘야 하므로 바꿔준다.
            mode = newMode;
            reverse = true;
        }
        
        // SignalReceive
        var SignalReceive = {
            
            //  @description sdp_offer는 callee user가 offer signal을 받았을때 호출되는 메소드.
            sdp_offer : function(options){
                
                console.log("[vpeer / SignalReceive / sdp_offer] 원격으로부터 offer를 받았습니다. sender : " + options.from + ", receiver : " + options.to);
                console.log("[vpeer / SignalReceive / sdp_offer] options : getDeny : " + ExCall.getDeny() + ", getNoCamera : " + ExCall.getNoCamera());
                console.log("[vpeer / SignalReceive / sdp_offer] sdp type : " + typeof options.msg.sdp);
                
                if(cordova.platformId == "android") {
                    // Native에 offer SDP 세팅하기..
                    var params = {
                        sender : options.from,
                        receiver : options.to,
                        sdp : options.msg.sdp
                    };
                    
                    cordova.exec(function(result) {
                        console.log("cordova.exec() success.. setOfferSdp");
                        console.log(result);
                    }, function(result) {
                        console.log('sendAnswer : ' + JSON.stringify(result));
                    }, "VideoPlugin", "setOfferSdp", [params]);
                    
                } else if(cordova.platformId == "ios") {
                    
                    if(ExCall.getDeny() || ExCall.getNoCamera()){
                        // stream을 체크해서 allow하지 않으면 영상이 보이지 않는다고 노티를 보내주던가 한다.
                        Callee.setRemoteDescription(new RTCSessionDescription(remoteSdp));
                        Callee.sendAnswer(options);
                    } else {
                        /***
                         * 	Answer를 만들기전에 상대방의 remote를 먼저 셋해줘야. 무선환경에서 description이 정상적으로 세팅 된다.
                         *
                         **/
                        var remoteSdp = options.msg;
                        Callee.setRemoteDescription(new RTCSessionDescription(remoteSdp));
                        
                        /**
                         *  Answer시 타이머를 두는 이유는 stream을 가져오는 시점이 클라이언트가 Allow를 누른 시점이기 때문이다. 따라서 비동기적으로 일어날수 있으므로 계속 체크해준다.
                         */
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
                    }
                }
                
                
            },
            
            //  @description sdp_answer는 offer를 보낸사람이 answer signal 을 받았을때 호출되는 곳이다. 즉 caller user가 answer 응답을 받는 메소드.
            sdp_answer : function(options){
                Utils.log("[SignalReceive sdp_answer : " + JSON.stringify(options));
                var sender = options.from;
                var receiver = options.to;
                var remoteSdp = options.msg;
                
                // Utils.log("[Video SignalReceive sdp_answer] offer : (" + sender + ") 원격 Description을 set했습니다.");
                
                if(cordova.platformId == "android") {
                    // Native에 answer SDP 세팅하기..
                    var params = {
                        sender : options.from,
                        receiver : options.to,
                        sdp : options.msg.sdp
                    };
                    
                    cordova.exec(function(result) {
                        console.log("cordova.exec() success.. setAnswerSdp");
                        console.log(result);
                    }, function(result) {
                        console.log('sendAnswer : ' + JSON.stringify(result));
                    }, "VideoPlugin", "setAnswerSdp", [params]);
                } else if(cordova.platformId == "ios") {
                    Caller.setRemoteDescription(new RTCSessionDescription(remoteSdp));
                }
            },
            
            // @description ice candidate 메시지를 받았을때 호출되는 메소드
            sdp_candidate : function(options){	// candidate 처리
                var sender = options.from;
                var receiver = options.to;
                var sdpCandidate = options.msg;
                
                console.log("[VideoCtrl SignalReceive sdp_candidate] 원격(" + sender + ")으로 받은 ice를 추가합니다.");
                console.log(sdpCandidate);
                
                Callee.setSdpCandidate(sender, sdpCandidate);
                
                
            },
            
            stream_allow : function(options){
                // 무선에서 새로운 유저가 접속한 경우 offer 보내는 쪽의 stream이 answer보내는 쪽의 stream보다 나중에 addStream이 되어야 한다 따라서 별도 패킷으로 맞춘다.
                Utils.log("stream_allow enter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
                peerConnection.removeStream(ExCall.getMyStream());
                
                peerConnection.addStream(ExCall.getMyStream());
            },
            
            reverse_offer : function(data){
                console.log("reverse_offer : " + JSON.stringify(data));
                _changeReverseMode("offer");
                
                if(cordova.platformId == "ios")
                    Caller.sendReverseOffer();   // 기존 웹방식의 reverse offer 요청..
                else {
                    // Native에 reverse offer 요청하기..
                    var params = { opponent : data.from };
                    
                    cordova.exec(function(result) {
                        console.log("cordova.exec() success.. requestReverseOffer");
                    }, function(result) {
                        console.log('requestReverseOffer Error : ' + JSON.stringify(result));
                    }, "VideoPlugin", "requestReverseOffer", [params]);
                }
            }
        };
        
        // 영상을 보내는 유저.. offer를 보내는 사람
        var Caller = {
            queue : [],
            isOfferWait : true,
            setLocalDescription : function(localSdp){
                try {
                    Utils.log("Caller.setLocalDescription");
                    Utils.log(peerConnection.localDescription, peerConnection.remoteDescription);
                    peerConnection.setLocalDescription(localSdp);
                } catch(e) {
                    Utils.log(e);
                }
            },
            setRemoteDescription : function(remoteSdp){
                try {
                    Utils.log("Caller.setRemoteDescription");
                    Utils.log("remote description !!!!!!!!!!!!!!!!!!!!", remoteSdp);
                    Utils.log(peerConnection.localDescription, peerConnection.remoteDescription);
                    
                    peerConnection.setRemoteDescription(remoteSdp);
                    
                    //-----------
                } catch(e) {
                    Utils.log(e);
                }
            },
            sendOffer : function(data){
                var sender = data.from;
                var receiver = data.to;
                
                if(cordova.platformId == "ios") {   // 기존 웹에서 동작
                    peerConnection.createOffer(function(localSdp) {
                        Caller.setLocalDescription(localSdp);
                        // Utils.log("sendOffer data : " + JSON.stringify(data));
                        
                        ExCall.send('sdp_offer', sender, receiver, roomid, localSdp);
                        
                        // offer candidate 전송 동기화
                        Caller.sendOfferCandidate();
                        
                        
                        Caller.isOfferWait = false;
                    },
                                               function(error) {
                                                   Utils.log("peerConnection.createOffer failed", error);
                                               }, options.constraints);
                } else {  // 웹앱에서 동작
                    var params = {
                        sender : data.from,
                        receiver : data.to
                    };
                    cordova.exec(function(result) {
                        console.log("cordova.exec() success.. createOffer");
                        console.log(result);
                    }, function(result) {
                        alert('add Video user ' + JSON.stringify(result));
                    }, "VideoPlugin", "createOffer", [params]);
                    
                }
                
                
            },
            
            sendReverseOffer : function(data){
                var data = {
                    from : currentUser.userid,
                    to : mappingUser.userid
                }
                
                Caller.sendOffer(data);
            },
            
            sendOfferCandidate : function(){
                if(Caller.isOfferWait){
                    setTimeout(Caller.sendOfferCandidate, "1000");
                    return;
                }
                
                var sender = currentUser.userid;
                var receiver = mappingUser.userid;
                
                for(; Caller.queue.length > 0;) {
                    ExCall.send('sdp_candidate', sender, receiver, roomid, Caller.queue.shift() );
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
                    /**
                     Utils.log("Callee.setLocalDescription");
                     Utils.log(peerConnection.localDescription, peerConnection.remoteDescription);
                     **/
                    peerConnection.setLocalDescription(localSdp);
                } catch(e) {
                    Utils.log(e);
                }
            },
            setRemoteDescription : function(remoteSdp){
                try {
                    /**
                     Utils.log("Callee.setRemoteDescription");
                     Utils.log(peerConnection.localDescription, peerConnection.remoteDescription);
                     **/
                    peerConnection.setRemoteDescription(remoteSdp);
                } catch(e) {
                    Utils.log(e);
                }
            },
            
            sendAnswer : function(data){
                var sender = data.from;
                var receiver = data.to;
                
                /***
                 var remoteSdp = data.msg;
                 
                 Utils.log("sendAnswer : " + JSON.stringify(data) );
                 
                 // stream을 체크해서 allow하지 않으면 영상이 보이지 않는다고 노티를 보내주던가 한다.
                 Callee.setRemoteDescription(new RTCSessionDescription(remoteSdp));
                 ***/
                
                Callee.isAnswerWait = false;
                
                Callee.sendAnswerCandidate();
                
                if(cordova.platformId == "ios") {   // 기존 웹에서 동작
//                    console.log(options.mediaopts);
                    peerConnection.createAnswer(function(localSdp) {
                        Callee.setLocalDescription(localSdp);
                        ExCall.send('sdp_answer', receiver, sender, roomid, localSdp);
                        // answer wait 상태
                        Callee.isAnswerWait = true;
                    },function(error) {alert(JSON.stringify(error))}, options.mediaopts);
                    
                } else {  // 웹앱에서 동작
                    var params = {
                        sender : data.from,
                        receiver : data.to
                    };
                    cordova.exec(function(result) {
                        console.log("cordova.exec() success.. createAnswer");
                        console.log(result);
                    }, function(result) {
                        alert('add Video user ' + JSON.stringify(result));
                    }, "VideoPlugin", "createAnswer", [params]);
                }
                
                
            },
            
            sendAnswerCandidate : function(){
                var sender = currentUser.userid;
                var receiver = mappingUser.userid;
                
                for(; Callee.queue.length > 0;) {
                    ExCall.send('sdp_candidate', sender, receiver, roomid, Callee.queue.shift() );
                }
            },
            
            setSdpCandidate : function(sender, sdpCandidate){
                try {
                    // allow 체크
                    if(cordova.platformId == "android") {
                        console.log(sdpCandidate);
                        var cordovaParams = {
                            opponentid : sender,
                            sdpcandidate : sdpCandidate
                        };
                        
                        cordova.exec(function(result) {
                            console.log("cordova.exec() success.. addPeerVideo");
                        }, function(result) {
                            alert('add Video user ' + JSON.stringify(result));
                        }, "VideoPlugin", "setRemoteCandidate", [cordovaParams]);
                    } else if(cordova.platformId == "ios") {
                        try {
                            peerConnection.addIceCandidate(new RTCIceCandidate(sdpCandidate));
                        }
                        catch(e) {
                            console.log(e);
                        }
                    }
                    
                    
                    
                } catch(e) {
                    Utils.log(e);
                }
                
            },
            
            sendStreamAllow : function(data){
                
                var sender = data.from;
                var receiver = data.to;
                
                ExCall.send('stream_allow', sender, receiver, roomid);
            },
            
            sendReverseOfferSignal : function(data){
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
            
            create : function(displayUser, isDisplay){
                tempVideoTag = document.createElement('video');
                tempVideoTag.autoplay = true;
                tempVideoTag.id = displayUser.userno;
                tempVideoTag.hidden = true;
                document.body.appendChild(tempVideoTag);
                tempGetUserMedia.addVideoTag_hg(tempVideoTag);
                videoMapForiOS.put(displayUser.userno,tempVideoTag);
                cordova.exec(null, null, "WebRTCPlugin", "addVideoview", [displayUser.userno]);
                tempVideoTag.src = URL.createObjectURL(stream);
            },
            
            setEvent : function(userToken){
                
                // drag drop
                videoWrapper.draggable({
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
                    
                    /***
                     if($(this).hasClass("turnoff") ){
                     // off - remove Stream -> pause
                     VideoUI.play();
                     $(this).removeClass("turnoff");
                     }else{
                     // on - add stream -> play
                     VideoUI.pause();
                     $(this).addClass("turnoff");
                     }
                     ***/
                });
            },
            
            changeVideoSize : function(userKey, num) {
                videoWrapper.attr("class", "cam_wrapper cam_container" + num);
            },
            
            changeBorderColor : function(userKey, num) {
                $(".cam_box", videoWrapper).removeClass().addClass("cam_box border" + num);
            },
            
            setCircleMenu : function(userId, diameter, radius, startAngle, endAngle) {
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
                
            },
            setVolumeSlider : function(userKey){
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
                if(cordova.platformId == "ios")
                    tempVideoTag.src = URL.createObjectURL(stream);
                else{
                    videoLayer.attr("src", ((window.URL || window.webkitURL).createObjectURL(stream) || stream));
                    videoLayer.get(0).autoplay = true;
                }
            },
            
            // 써클메뉴 이벤트 해제
            destroyCircleMenu : function () {
                // Utils.log("circleMenu(destroy)")
                $('ul.cam_circle_menu', videoWrapper).circleMenu("destroy");
            },
            destroy : function(){
                videoWrapper.draggable("destroy");
            }
        }
        
        init();
        
        //
        return {
            init : init,
            async : async,
            setMyMedia : setMyMedia,
            // isStream : isStream,	// stream하고 있는 레이어인지 체크
            destroy : destroy
        };
    }
    
    window.Video = Video;
    
})();