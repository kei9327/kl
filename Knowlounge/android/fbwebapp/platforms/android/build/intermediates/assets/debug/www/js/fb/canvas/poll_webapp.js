var PollCtrlCordova = {

    pollNo : null,
    type : null,
    title : null,
    timer : null,
    drawingTool : null,
    isProgress : false,
    progressPoll : null,
    svrHost : null,

    init : function(pollNo, type, title, allowCnt) {
        var svrFlag = _prop('svr.flag');

        this.pollNo = pollNo;
        this.type = type;
        this.title = title;

        var svrFlag = _prop("svr.flag");
        this.svrHost = _prop('svr.host.' + svrFlag);
        alert(this.svrHost);
    },


	/**
	 * PollCtrl.startDrawingPollAnswer
	 *  - 내 보드로 이동했을 때 이 함수가 진입점이다.
	 */
	startDrawingPollAnswer : function(pollNo, timeLimit, isCountdown, image) {
		console.log("[PollCtrl.startDrawingPollAnswer]");
		
		console.log("[PollCtrl.startDrawingPollAnswer] image : " + image);
		
		var imageMap;

		if (typeof image == "object") {
			imageMap = image;
		} else if (typeof image == "string") {
			imageMap = JSON.parse(image);
		}

		var seqNo = imageMap.seqno;
		PollCtrl.questionImgInit(seqNo);

		PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, false);
    },

	
	/**
	 * PollCtrl.questionImgInit
	 *  - BGImg.init으로 초기화할 수 없는 질문 이미지에 대해서 이미지 배치를 설정하는 함수
	 */
	
	questionImgBasePos : [150, 100],
	questionImgInit : function(seqNo) {
		
		var timer = setInterval(function() {
			if (Ctrl.BGImg.data != null) {
				console.log(Ctrl.BGImg.data);
				var imageData = Ctrl.BGImg.data.get(seqNo);
				var board = UI.getBoard();
				var imgCanvas = board.getCanvas("img");

				Ctrl.BGImg.redraw(seqNo, "");

				var initPosition = PollCtrl.questionImgBasePos[0] + (Ctrl.BGImg.newCnt * 20);   // 기본 이미지 배치 포지션은 70 * 120
				imageData.posx = initPosition;
				imageData.posy = initPosition;
				
				var url = imageData.url;
				//var seqNo = imageData.seqno;
				var scaleW = imageData.scalew;
				var scaleH = imageData.scaleh;
				var posX = initPosition;
				var posY = initPosition;
				var typeFlag = imageData.typeflag;
				var userNm = imageData.usernm;
				var thumbnail = imageData.thumbnail;
				var lastDegree = imageData.cvs.degree;
				var fileData = imageData.filedata;

				Ctrl.BGImg.draw(imageData, false);

				PacketMgr.Master.img(url, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, lastDegree, fileData);

				clearInterval(timer);
			}
		}, 500);
	},

	
	/**
	 * PollCtrl.broadcastPoll
	 *  - 수업 내 모든 보드에 폴 패킷을 브로드캐스팅 함.
	 *  - target : 폴 패킷을 브로드캐스팅하는 대상 (all / teacher)
	 */
    broadcastPoll : function(type, pollNo, target, timeLimit, isCountdown, userNo, pollType) {
        var mode = "all";
        var roomId = PacketMgr.roomid;
        var packet = {"cmd":"poll","type":""+type+"","pollno":""+pollNo+"","target":""+target+""};

        if(typeof timeLimit != 'undefined')
            packet['timelimit'] = timeLimit;
        if (typeof isCountdown != 'undefined')
            packet['iscountdown'] = isCountdown;
        if (typeof userNo != 'undefined')
            packet['userno'] = userNo;
        if (typeof pollType != 'undefined')
            packet['polltype'] = pollType;
		
		if (type == "report") {
			packet['totalusercnt'] = Ctrl.Member.classList.length - 1;
			packet['presentor'] = PacketMgr.userno;
		}

        console.log("[broadcastPoll] packet : " + JSON.stringify(packet));

		PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
    },

    receive : function(packet) {
        var type = packet.type;
        var pollNo = packet.pollno;
        var targetUser = typeof packet.target != 'undefined' ? packet.target : "";
        var timeLimit = typeof packet.timelimit != 'undefined' ? packet.timelimit : "";
        var isCountdown = typeof packet.iscountdown != 'undefined' ? packet.iscountdown : "";
        var pollType = typeof packet.polltype != 'undefined' ? packet.polltype : "";

        if (type == "start") {   // 질문 시작
            Ctrl.isPollProcessing = true;  // 폴 진행중일 때 예외처리 플래그값 활성화
            if (pollType == "3") {
				if(PacketMgr.userid == PacketMgr.userno)  // 게스트는 판서 폴 답변할 수 없도록 예외처리
					return;

                if (targetUser == "teacher") {
                    if (!PacketMgr.isParentCreator) {    // 학생이 선생님에게 질문했을 때, 다른 학생에게는 폴 패킷이 도달하지 않도록 함..
						
						// 다른 학생이 선생님에게 질문할 수 없도록 폴 state 값을 true로 설정함..
						var param = {
							poll_state : true
						};
						cordova.exec(function(result) {
							console.log("cordova updatePollProgressState success..");
						}, function(result) {
							console.log("cordova updatePollProgressState fail.." + JSON.stringify(result));
						}, "PollPlugin", "updatePollProgressState", [param]);
                        return;

                    } else {
                        //PollCtrl.Action.Attender.readyToAnswerDrawingQuestion(pollNo);
						if(!PacketMgr.isCreator) {
							console.log("학생 보드에 있는 선생님이 질문을 받았습니다.");
							PollCtrl.Action.Attender.moveSubroomForDrawing(pollNo, timeLimit, isCountdown);
						} else {
							console.log("선생님이 선생님 보드에서 질문을 받았습니다.");
							PollCtrl.Action.Attender.readyToAnswerDrawingQuestion(pollNo);
							PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, false);
						}
						return;
                    }
                } else {
					if (!PacketMgr.isParentCreator && !PacketMgr.isCreator) {  // 학생이 선생님 보드나 다른 학생의 보드에서 판서형 질문을 받았을 때..
						PollCtrl.Action.Attender.moveSubroomForDrawing(pollNo, timeLimit, isCountdown);
						return;
					} else if (!PacketMgr.isParentCreator && PacketMgr.isCreator) {  // 학생이 학생보드에서 판서형 질문을 받았을 때..
						PollCtrl.Action.Attender.readyToAnswerDrawingQuestion(pollNo);
						PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, false);  // 답변을 입력할 수 있는 화면을 보여줌
						return;
					}
				}

            } else {  // 이 외의 케이스에는 답변을 입력할 수 있는 화면을 출력함..
                PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, true);
            }
        } else if (type == "interrupt") {   // 질문 종료
            Ctrl.isPollProcessing = false;
            if (targetUser == "teacher" && !PacketMgr.isParentCreator) {
				var param = {
					poll_state : false
				};
				cordova.exec(function(result) {
					console.log("cordova updatePollProgressState success..");
				}, function(result) {
					console.log("cordova updatePollProgressState fail.." + JSON.stringify(result));
				}, "PollPlugin", "updatePollProgressState", [param]);
				return;
			}
			PollCtrl.Action.Attender.exitPoll(pollNo);
        } else if(type == "report") {   // 질의응답 결과 화면 공유
			var totalusercnt = typeof packet.totalusercnt != 'undefined' ? packet.totalusercnt : "";
			var presentor = typeof packet.presentor != 'undefined' ? packet.presentor : "";
			PollCtrl.Action.Common.makePollResult(pollNo, totalusercnt, presentor);
		} else if(type == "answer") {   // 답변자가 답변을 완료했음을 알려주는 패킷
			PollCtrl.UI.updateAnswerUser(packet.userno);

		}
    },


    /*
     Poll 샘플 데이터
     data = {
     map: {
     userkey: "3196961736dp8f7b"
     svctype: "0"
     title: "오늘 점심은?"
     pollkey: "878375c7-5b38-4d02-be7b-1789208af692"
     updatetime: null
     pollpgroupno: "3192c012b21pc3a764daeb"
     answerusercnt: 16
     polluserno: "3327b8434e8gc49a8765"
     startdatetime: "20150212105403"
     itemlist: [
     {
     pollitemno: "3327b8434e8gbbfeec0d"
     itemdesc: null
     pollno: "3327b8434e8g0577111a"
     itemidx: 1
     regdatetime: "20150212105403"
     answercnt: 6
     updatetime: null
     itemnm: "1"
     },
     {
     pollitemno: "3327b8434e9h5281686a"
     itemdesc: null
     pollno: "3327b8434e8g0577111a"
     itemidx: 2
     regdatetime: "20150212105403"
     answercnt: 3
     updatetime: null
     itemnm: "2"
     },
     {
     pollitemno: "3327b8434e9h105f7cf3"
     itemdesc: null
     pollno: "3327b8434e8g0577111a"
     itemidx: 3
     regdatetime: "20150212105403"
     answercnt: 5
     updatetime: null
     itemnm: "3"
     },
     {
     pollitemno: "3327b8434e9heb0414f3"
     itemdesc: null
     pollno: "3327b8434e8g0577111a"
     itemidx: 4
     regdatetime: "20150212105403"
     answercnt: 2
     updatetime: null
     itemnm: "4"
     }],
     telno: ""
     usernm: "위노트26"
     category: null
     answertotalcnt: 16
     polltype: "0"
     pollgroupno: "3192c012b21pc3a764daeb"
     guesttype: "0"
     ipaddr: "211.36.159.42"
     pollno: "3327b8434e8g0577111a"
     allowcnt: 1
     description: ""
     email: "wenote026@wenote.com"
     enddatetime: "20150212105432"
     regdatetime: "20150212105403"
     delflag: "0"
	    }
	    result: 0
	    msg: "success"
     };
     */

    Action : {
        Master : {
            
			Question : {
				shareDrawAnswer : function(pollFileNo, url, pollUserNo) {
					//var packet = {"cmd":"img","seqno":"" + seqNo + "","posx":"","posy":"","scalew":"","scaleh":"","url":"" + url + ""};
					//var packet = {"cmd":"img","x":"","y":"","width":"","height":"","url":"" + url + ""};
					
					var count = Ctrl.BGImg.getCnt();
					if(count > Ctrl.Uploader.limitCnt){
						Ctrl.Msg.show(_msg("check.file.count"));
						return;
					}
					
					var param = {
						pollfileno : pollFileNo,
						roomid : RoomSvr.roomid,
						polluserno : pollUserNo,
						pageid : UI.current
					};
					
					var svrFlag = _prop("svr.flag");
					var svrHost = _prop("svr.host." + svrFlag);
					var url = svrHost + 'mapi/poll/draw/share.json';

					$.ajax({
						type : 'post',
						url : url,
						async : true,
						cache : false,
						dataType : 'json',
						data : param,
						beforeSend : function(){},
						success : function(data) {
							Utils.log("[PollCtrl.Action.Master.shareDrawAnswer] mapi/poll/draw/share.json success ------ result : " + JSON.stringify(data));

							if(data.result == '0') {
								/**
								var newSeqNo = data.map.seqno;
								var fileName = data.map.filename;
								
								var packet = {"seqno":""+newSeqNo+"","dnloadurl":""+url+"","filename":""+fileName+""};
								console.log("답안 이미지 전송 패킷 : " + JSON.stringify(packet));								
								**/
								
								//PacketMgr.Command["img"](packet);
								Ctrl.BGImg.init(data.map);
							}
						},
						error : function(){ Utils.log("error..") },
						complete : function(){ Utils.log("complete..") } 
					});
				}
			
			},

            // 폴 템플릿 생성하기 (true : 생성과 동시에 send / false : 생성만)
            createPoll : function(params) {

                var pollTitle = params.title;
                var pollType = params.polltype;
                var allowCnt = params.allowcnt;
                var sendFlag = params.sendflag;
                var shutdownTime = params.shutdowntime;
                var itemStr = params.items;
                var pollKey =  RoomSvr.userno;
                var pollCategory = pollType != "3" ? "0" : "1";

                var curDate = new Date();
                var curMs = curDate.getTime();
                var endMs = curMs + (shutdownTime * 1000);
                var endDate = new Date(endMs);

                var userKey   = RoomSvr.userno;
                var userId    = RoomSvr.userid;
                var userNm    = RoomSvr.usernm;
                var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트

                var qusBinary = params.qusbinary;
                var targetUser = params.target;

                // pollType == 2일땐 itemStr을 공백으로 보냄..
                // 항목을 입력하지 않으면 자동으로 인덱스값을 세팅함..

                // 예외처리 구간..
                if(!Ctrl._checkAuth(true)) return;
                console.log(Ctrl.Member.classList);
				
				var userNums = Ctrl.Member.classList.length;   // 클래스 유저 수 기준으로..

				var targetUserCnt = 0;
				for(var i=0; i<userNums; i++) {
					var userNo = Ctrl.Member.classList[i].userno;
					var userId = Ctrl.Member.classList[i].userid;
					
					if (userNo == PacketMgr.userno) continue;
					if (pollType == "3" && userNo == userId) continue;   // 판서형 질문에서 게스트는 제외해야 하므로 카운팅하지 않음..
					targetUserCnt++;
					
				}

				if(sendFlag && targetUserCnt == 0) {   // 게스트 유저와 나를 뺀 나머지 유저의 인원수로 판단..
					Ctrl.Msg.show(_msg("poll.alert.no.participant"));
					return;
				}

				if(pollTitle == '') {
					Ctrl.Msg.show(_msg("poll.alert.title"));
					return;
				}

				if(pollType != '2' && pollType != '3'){
					if(itemStr == '') {
						Ctrl.Msg.show(_msg("poll.alert.answer"));
						return;
					}
				}

                if(pollType == '3') {
					if(!PacketMgr.isCreator) {
						Ctrl.Msg.show(_msg("poll.draw.nomine.lmt"));
						return;
					}
				}

				if(isNaN(shutdownTime)) {
					Ctrl.Msg.show(_msg("poll.alert.timer"));
					return;
				}

                var param = {
                    pollkey       : pollKey,
                    polltype      : pollType,
                    pollcategory  : pollCategory,
                    title         : pollTitle,
                    allowcnt      : allowCnt,
                    startdatetime : PollCtrl.Util.getDateStr(curDate),
                    enddatetime   : PollCtrl.Util.getDateStr(endDate),
                    items         : itemStr,
                    userkey       : userKey,
                    usernm        : userNm,
                    guesttype     : guestType,
                    qusbinary     : qusBinary
                };

                console.log("[createPoll] param : " + JSON.stringify(param));

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.tmp.add");
                $.ajax({
                    type  : 'post',
                    url   : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        console.log("[createPoll] poll/tmp/add.json success ----- data : " + JSON.stringify(data));
                        if(data.result == '0') {
                            Ctrl.Msg.show(_msg("poll.alert.add"));
                            if(data.polltempno != null) {
                                var pollTempNo = data.polltempno;

                                if(sendFlag) {
                                    console.log(shutdownTime);
                                    PollCtrl.Action.Master.sendPoll(pollTempNo, shutdownTime, targetUser);
                                } else {
									PollCtrl.Action.Master.makePollList(1);
								}

                            }
                        } else {
                            Ctrl.Msg.show(_msg("poll.alert.error"));
                        }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){}
                });
            },


            /**
             * PollCtrl.Action.Master.updatePoll : 폴 템플릿 정보 업데이트
             * @param
             * - pollTempNo : 폴 템플릿 넘버 (UUID)
             * - sendFlag   : 업데이트 후 바로 전송 여부 (true / false)
             **/
            updatePoll : function(params) {

                var pollTitle    = params.title;
                var pollTempNo   = params.polltempno;
                var pollType     = params.polltype;
                var allowCnt     = params.allowcnt;
                var sendFlag     = params.sendflag;
                var shutdownTime = params.shutdowntime;
                var itemStr      = params.items;

                var pollKey =  RoomSvr.userno;
                var pollCategory = pollType != "3" ? "0" : "1";

                var curDate = new Date();
                var curMs = curDate.getTime();
                var endMs = curMs + shutdownTime;
                var endDate = new Date(endMs);

                var userId    = RoomSvr.userid;
                var guestType = RoomSvr.userno != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트

                // 예외처리 구간..
                if(!Ctrl._checkAuth(true)) return;

                if(pollTempNo == '' || pollTempNo == undefined) {
                    Ctrl.Msg.show(_msg("poll.alert.error"));
                    return;
                }
                if(pollType != '2'){
                    if(params.items == '') {
                        Ctrl.Msg.show(_msg("poll.alert.answer"));
                        return;
                    }
                }
                if(sendFlag) {
                    if(isNaN(shutdownTime)) {
                        Ctrl.Msg.show(_msg("poll.alert.timer"));
                        return;
                    }
                }

                var param = {
                    polltempno    : pollTempNo,
                    pollkey       : pollKey,
                    polltype      : pollType,
                    title         : pollTitle,
                    allowcnt      : allowCnt,
                    startdatetime : PollCtrl.Util.getDateStr(curDate),
                    enddatetime   : PollCtrl.Util.getDateStr(endDate),
                    items         : itemStr,
                    userkey       : RoomSvr.userno,
                    usernm        : RoomSvr.usernm,
                    guesttype     : guestType
                    //email : userId,
                };

                console.log(JSON.stringify(param));

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.tmp.update");
                $.ajax({
                    type : 'post',
                    url : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {

                        console.log("[updatePoll] poll/tmp/update.json success ----- data : " + JSON.stringify(data));

                        if(data.result == '0') {
                            Ctrl.Msg.show(_msg("poll.alert.update"));
                            if(sendFlag) {
                                PollCtrl.Action.Master.sendPoll(pollTempNo, shutdownTime);
                            } else {
                                PollCtrl.Action.Master.makePollList(1);
                            }
                        } else {
                            Ctrl.Msg.show(_msg("poll.alert.error"));
                            return;
                        }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){}
                });
            },

            // 폴 템플릿 삭제
            deletePoll : function(pollTempNo) {

                // 예외처리 구간..
                if(!Ctrl._checkAuth(true)) return;

                var param = { polltempno : pollTempNo };
                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.tmp.remove");
                $.ajax({
                    type  : 'post',
                    url   : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        Utils.log("[deletePoll] poll/tmp/remove.json success ----- data : " + JSON.stringify(data));
                        if(data.result == '0') {
                            //Ctrl.Msg.show(_msg("poll.alert.tmp_del.success"));
                            PollCtrl.Action.Master.makePollList(1);
                        } else {
                            Ctrl.Msg.show(_msg("poll.alert.tmp_del.fail"));
                        }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){}
                });

            },

            // 완료된 폴 삭제
            deleteCompletedPoll : function() {

                // 예외처리 구간..
                if(!Ctrl._checkAuth(true)) return;

                if($("input[name='poll_item_ended']:checked").length == 0) {
                    Ctrl.Msg.show("Please select to remove poll item");
                    return;
                }
                var pollNoList = $("input[name='poll_item_ended']:checked").map(function(){
                    if(this.value != "") {
                        return this.value;
                    }
                }).get().join("|");

                var param = { pollno : pollNoList };

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.remove");
                $.ajax({
                    type  : 'post',
                    url   : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        Utils.log("success.." + data);
                        if(data.result == '0') {
                            Ctrl.Msg.show(_msg("poll.alert.delete.success"));
                            //PollCtrl.UI.close("poll_complete_list_box");
                            PollCtrl.Action.Master.makePollCompleteList(1);
                        } else {
                            Ctrl.Msg.show(_msg("poll.alert.delete.fail"));
                        }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){ Utils.log("complete..") }
                });

            },


            /**
			 * PollCtrl.Action.Master.sendPoll : 질문을 참여자들에게 보내기 (수업 내 모든 보드로 브로드캐스팅하는 방식)
			 *	- pollTempNo : 폴 템플릿 넘버 (UUID)
			 *	- shutdown : 제한 시간 (단위 : 초)
			 */
            sendPoll : function(pollTempNo, shutdown, targetUser) {

                if(!Ctrl._checkAuth(true, false)) return;

                //if(sendFlag && Ctrl.Member.list.length <= 1) {
				if(Ctrl.Member.classList.length < 1) {
					Ctrl.Msg.show(_msg("poll.alert.no.participant"));
					return;
				}

                var param = { polltempno : pollTempNo };
                var paramForCopy;
                var isCountdown = true;

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.tmp.get");

                $.ajax({
                    type       : 'post',
                    url        : url,
                    async      : true,
                    cache      : false,
                    dataType   : 'json',
                    data       : param,
                    beforeSend : function(){},
                    success    : function(data) {
                        console.log("[sendPoll] poll/tmp/get.json success ----- data : " + JSON.stringify(data));
                        if(data.result == '0') {
                            var resultMap = data.map;
                            paramForCopy = resultMap;
                            paramForCopy.pollkey = RoomSvr.roomid;

                            /*
                             var pollNo = resultMap.pollno;
                             var userKey = resultMap.userkey;
                             var userNm = resultMap.usernm;
                             var email = resultMap.email;
                             var telNo = resultMap.telno;
                             var guestType = resultMap.guesttype;
                             */

                            var url = svrHost + _prop("poll.tmp.copy");
                            $.ajax({
                                type       : 'post',
                                url        : url,
                                async      : true,
                                cache      : false,
                                dataType   : 'json',
                                data       : paramForCopy,
                                beforeSend : function(){},
                                success    : function(data) {
                                    console.log("[sendPoll] poll/tmp/copy.json success ----- data : " + JSON.stringify(data));
                                    if (data.result == '0') {
                                        Ctrl.Msg.show(_msg("poll.alert.send.success"));

                                        PollCtrl.recvTarget = typeof targetUser == 'undefined' ? "" : targetUser;

                                        var cordovaParams = {
                                            pollno : data.pollno,
                                            target : typeof targetUser == 'undefined' ? "" : targetUser
                                        };

										cordova.exec(function(result) {
                                            console.log("cordova.exec() success.. onReadySendPoll");
                                        }, function(result) {
                                            alert('onReadySendPoll err : ' + JSON.stringify(result));
                                        }, "PollPlugin", "onReadySendPoll", [cordovaParams]);

                                        var newPollNo = data.pollno;

                                        PollCtrl.isProgress   = true;
                                        PollCtrl.progressPoll = newPollNo;

                                        if(shutdown == undefined) shutdown = 30; // 시간 지정 안된 경우는 디폴트 30초로
                                        if(shutdown == 0) isCountdown = false;   // 시간 제한 없을 경우..
                                        var shutdownMs = shutdown * 1000;
										PollCtrl.broadcastPoll("start", newPollNo, typeof targetUser == 'undefined' ? "" : targetUser, shutdownMs, isCountdown, "", paramForCopy.polltype);

                                    } else {
                                        Ctrl.Msg.show(_msg("poll.alert.send.fail"));
                                    }
                                },
                                error      : function(){ Utils.log("error..") },
                                complete   : function(){ Utils.log() }
                            });
                        }
                    },
                    error      : function(){ Utils.log("error..") },
                    complete   : function(){ Utils.log() }
                });
            },


            /**
			 * PollCtrl.Action.Master.sendPollFromList : 리스트에서 선택하여 질문을 보내는 함수
			 *
			 */
            sendPollFromList : function() {
                var selectedPollNo = $(":radio[name='poll_item']:checked").val();
                if(selectedPollNo == '' || selectedPollNo == undefined){
                    Ctrl.Msg.show(_msg("poll.alert.check"));
                    return;
                }
                var defaultTime = 30;  // 리스트에서 바로 Send할때는 기본 제한시간을 적용함 (30초)

                PollCtrl.Action.Master.sendPoll(selectedPollNo, defaultTime);
            },


            /**
			 * PollCtrl.Action.Master.sendPollReport : 폴 결과화면 브로드캐스팅
			 *
			 */
            sendPollReport : function(pollNo) {
                PollCtrl.broadcastPoll("report", pollNo, "");
            },


            /**
			 * PollCtrl.Action.Master.stopPoll : 질문 종료하기
			 */
            stopPoll : function(pollNo) {
				PollCtrl.broadcastPoll("interrupt", pollNo, PollCtrl.recvTarget);
				
				PollCtrl.isProgress = false;
                PollCtrl.progressPoll = null;
                PollCtrl.recvTarget = "";

                Ctrl.Msg.show(_msg("poll.alert.finish"));
                clearInterval(PollCtrl.timer);

                PollCtrl.Action.Common.makePollResult(pollNo, Ctrl.Member.classList.length - 1, PacketMgr.userno);
            },


            // 폴 템플릿 상세정보 가져오기
            getPollTmpDetail : function(pollTempNo) {
                var param = { polltempno : pollTempNo };

                var svrFlag = _prop('svr.flag');
                var svrHost = _prop('svr.host.' + svrFlag);
                var url = svrHost + _prop("poll.tmp.get");

                $.ajax({
                    type       : 'post',
                    url        : url,
                    async      : true,
                    cache      : false,
                    dataType   : 'json',
                    data       : param,
                    beforeSend : function(){},
                    success    : function(data) {
                        console.log("success.." + data);
                        if(data.result == '0') {
                            var resultMap = data.map;
                            console.log("getPollTmpDetail : " + JSON.stringify(resultMap));

                            cordova.exec(function(result) {
                                console.log("cordova.exec() success.. getPollTmpDetail");
                            }, function(result) {
                                alert('getPollTmpDetail err : ' + JSON.stringify(result));
                            }, "PollPlugin", "getPollTmpDetail", [data]);

                            ////PollCtrl.UI.close("poll_list_box");
                            ////PollCtrl.UI.open("poll_info_box");
                            ////PollCtrl.UI.renderPollUpdateFrm(resultMap);
                        }
                    },
                    error      : function(){ Utils.log("error..") },
                    complete   : function(){ Utils.log("complete..") }
                });
            },

            // 폴 리스트 받아오기
            makePollList : function(pollKey, pageNum) {
                ////PollCtrl.UI.open("poll_list_box");
                //var pollKey = RoomSvr.userno;
                var param = {
                    pollkey   : pollKey,
                    rows : PollCtrl.UI.ROWS_PER_PAGE,
                    pageno : pageNum
                };

                var svrFlag = _prop('svr.flag');
                var svrHost = _prop('svr.host.' + svrFlag);
                var url = svrHost + _prop("poll.tmp.list");

                $.ajax({
                    type       : 'post',
                    url        : url,
                    async      : true,
                    cache      : false,
                    dataType   : 'json',
                    data       : param,
                    beforeSend : function(){},
                    success    : function(data) {
                        console.log("success..");
                        console.log(JSON.stringify(data));
                        if(data.result == '0') {
                            ////PollCtrl.UI.renderPollList(data, pageNum);

                            cordova.exec(function(result) {
                                console.log("cordova.exec() success.. getPollTmpList");
                            }, function(result) {
                                alert('getPollTmpList err : ' + JSON.stringify(result));
                            }, "PollPlugin", "getPollTmpList", [data]);
                        }
                    },
                    error      : function(){ Utils.log("error..") },
                    complete   : function(){ Utils.log("complete..") }
                });
            },

            // 완료된 폴 리스트 받아오기
            // PollCtrl.Action.Master.makePollCompleteList(pageNum);
            makePollCompleteList : function(pollKey, pageNum) {
                ////PollCtrl.UI.open("poll_complete_list_box");
                //var pollKey = RoomSvr.roomid;
                var param = {
                    pollkey : pollKey,
                    // rows    : PollCtrl.UI.ROWS_PER_PAGE,
                    pageno  : pageNum
                };

                var svrFlag = _prop('svr.flag');
                var svrHost = _prop('svr.host.' + svrFlag);
                var url = svrHost + _prop("poll.list");
                $.ajax({
                    type       : 'post',
                    url        : url,
                    async      : true,
                    cache      : false,
                    dataType   : 'json',
                    data       : param,
                    beforeSend : function(){},
                    success    : function(data) {
                        console.log("success..");
                        console.log(JSON.stringify(data));
                        cordova.exec(function(result) {
                            console.log("cordova.exec() success.. getCompletePollList");
                        }, function(result) {
                            alert('getCompletePollList err : ' + JSON.stringify(result));
                        }, "PollPlugin", "getCompletePollList", [data]);

                        /*
                         if(data.result == '0') {
                         //PollCtrl.UI.renderCompletedPollList(data, pageNum);
                         }*/
                    },
                    error      : function(){ Utils.log("error..") },
                    complete   : function(){ Utils.log("complete..") }
                });
            },

            // 방 종료했을 때 폴관련 정보를 삭제함
            // PollCtrl.Action.Master.removeRoomPoll(pollKey);
            removeRoomPoll : function(pollKey) {
                var param = { pollkey : pollKey };

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.all.remove");
                $.ajax({
                    type : 'post',
                    url : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        if(data.result == '0') {

                        } else {
                            alert("remove fail...");
                        }
                    },
                    error      : function(){ Utils.log("error..") },
                    complete   : function(){ Utils.log("complete..") }
                });
            }

        },
        Attender : {
            // 설문 시트 생성
            makePollSheet : function(pollNo, timeLimit, isCountdown, isShow) {
                console.log("폴시트를 생성합니다.");
				
				isShow == typeof isShow == "undefined" ? true : isShow;


				var param = { pollno : pollNo };
                $(".poll_user_count", "#hand_writing_sheet").show();

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.get");
                $.ajax({
                    type : 'post',
                    url : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        console.log("success..");
                        console.log(data);

                        if(data.result == '0') {
							// TODO : 판서형 폴은 예외처리 - 2016.06.27
							// if(data.map.polltype == "3") {
							// 	return;
							// }
                            var cordovaParams = {
                                polldata : data,
                                timelimit : timeLimit,
                                iscount : isCountdown,
								isshow : isShow   // 안드로이드에서만 사용함
                            };

                            cordova.exec(function(result) {
                                console.log("cordova.exec() success.. makePollSheet");
                            }, function(result) {
                                alert('makePollSheet err : ' + JSON.stringify(result));
                            }, "PollPlugin", "makePollSheet", [cordovaParams]);

                        }

                        /*
                        var pollStart = data.map.startdatetime;
                        var pollEnd = data.map.enddatetime;
                        if(data.result == '0') {

                            PollCtrl.isProgress = true;
                            PollCtrl.progressPoll = pollNo;

                            var resultMap = data.map;
                            var pollType = resultMap.polltype;
                            var pollCategory = resultMap.pollcategory;
                            var userTimeLimit = timeLimit + 1;  // 1초 딜레이

                            //PollCtrl.UI.renderPollSheet(resultMap);
                            console.log("[makePollSheet] isCountdown = " + isCountdown + " " + typeof isCountdown);

                            if(isCountdown == "true") {
                                timeLimit = timeLimit / 1000;
                                PollCtrl.timer = setInterval(function() {
                                    $(".poll_user_count span").text(timeLimit);
                                    timeLimit--;
                                    if (timeLimit < 0) {
//                                        Ctrl.Msg.show(_msg("poll.alert.finish"));
                                        PollCtrl.Action.Attender.exitPoll(pollNo);
                                        //PollCtrl.Action.Master.finishPoll(pollNo);
                                    }
                                }, 1000);
                            } else {
                                $(".poll_user_count", "#hand_writing_sheet").hide();
                            }
                        }*/
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){ Utils.log("complete..") }
                });
            },


			// 설문 시트 결과 전송  PollCtrl.Action.Attender.submitPollResult
            submitPollResult : function(params) {

                var pollNo  = params.pollno;
                var pollType = params.polltype;
                var userKey = params.userno;
                var userId  = params.userid;
                var userNm  = params.usernm;
                var pollItemNo = params.pollitemno;
                var answerTxt = params.answertxt;
                var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
                var answerBin = pollType == '3' ? params.answerbin : "";
                var timeLimit = params.timelimit;

                if(pollType != '3'){
                    if(pollItemNo == "" && answerTxt == "") {
                        Ctrl.Msg.show(_msg("poll.alert.empty.answer"));
                        return;
                    }
                }

                var param = {
                    pollno : pollNo,
                    polltype : pollType,
                    userkey : userKey,
                    //svctype : svcType,
                    usernm : userNm,
                    //email : email,
                    //telno : telNo,
                    guesttype : guestType,
                    pollitemno : pollItemNo,
                    anstxt : answerTxt,
                    ansbinary : answerBin
                };

                console.log("submitPollResult : " + JSON.stringify(param));

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.answer.add");

                $.ajax({
                    type  : 'post',
                    url   : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        Utils.log("success.." + JSON.stringify(data));
                        if(data.result == '0') {
                            Ctrl.Msg.show(_msg("poll.alert.submit"));
                            PollCtrl.broadcastPoll("answer", pollNo, "", "", "", userKey);   // 답변 완료시에 답변했다고 패킷을 보냄..
							
							// 답변 제출이 성공하였으므로 그에 맞는 네이티브 쪽 UI 처리를 실행 시킴..
							cordova.exec(function(result) {
                                console.log("cordova.exec() success.. successAnswer");
                            }, function(result) {
                                alert('makePollSheet err : ' + JSON.stringify(result));
                            }, "PollPlugin", "successAnswer", []);

							if (pollType == "3" && PollCtrl.recvTarget == "") {
								var msg = _msg("poll.submit.back.t");
								Ctrl.Modal.confirm(msg, true, function(){
									// TODO : 내 보드로 이동하여 답변하도록..
									var masterSeqNo = CanvasApp.info.masterseqno;
									cordova.exec(function(result){
									}, function(result){}, "RoomPlugin", "moveRoom", [{code : masterSeqNo}]);

								});
							}

							/*
                            if (timeLimit == "0") {
                                PollCtrl.Action.Attender.exitPoll(pollNo);
                            }*/

                            return;
                        } else if(data.result == '-102') {
                            Ctrl.Msg.show(_msg("poll.alert.already.submit"));
                            return;
                        } else if(data.result == '-201') {
                            Ctrl.Msg.show(_msg("poll.alert.upload.fail"));
                            return;
                        } else {
                            Ctrl.Msg.show(_msg("poll.alert.answer.fail"));
                            return;
                        }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){ Utils.log("complete..") }
                });
            },


            /**
			 * PollCtrl.Action.Attender.exitPoll
			 *  - 질문이 종료되었을 때 참여자 쪽의 UI 처리 함수
			 */
            exitPoll : function(pollNo) {
                console.log('exitPoll '+ pollNo);

                var cordovaParams = {
                    pollno : pollNo
                };
                cordova.exec(function(result) {
                    console.log("cordova exitPoll success..");
                    Ctrl.Msg.show(_msg("poll.alert.finish"));
                }, function(result) {
                    console.log("cordova exitPoll fail.." + JSON.stringify(result));
                }, "PollPlugin", "exitPoll", [cordovaParams]);

            },


            /**
			 * PollCtrl.Action.Attender.moveSubroomForDrawing
			 *  - 선생님방에서 폴 질문을 받았을 때, 학생방으로 이동시켜주는 함수
			 */
			moveSubroomForDrawing : function(pollNo, timeLimit, isCountdown) {

				var param = {
					pollno : pollNo,
					roomid : RoomSvr.roomid,
					deviceid : RoomSvr.deviceid
				};

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.subroom.move");

				$.ajax({
					type : 'post',
					url : url,
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
                    beforeSend : function(xhr) {
    					xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
    				},
					success : function(data) {
						console.log("[moveSubroomForDrawing] success ----- data : " + JSON.stringify(data));
						if (data.result == '0') {
                            if(PacketMgr.code == data.code) {
                                return;
                            } else {
                                var param = {
                                    code : data.code,
                                    pollno : pollNo,
                                    timelimit : timeLimit,
                                    iscountdown : isCountdown,
									title : data.title,
									url : data.fileurl,
									image: JSON.stringify(data.image),
									ismove : true

                                };
								
								/*
                                cordova.exec(function(result) {
                                    console.log("cordova exitPoll success..");
                                }, function(result) {
                                    console.log("cordova exitPoll fail.." + JSON.stringify(result));
                                }, "PollPlugin", "moveSubroom", [param]);
								*/

								cordova.exec(function(result) {
									console.log("cordova showQuestionArrivedDialog success..");
								}, function(result) {
									console.log("cordova showQuestionArrivedDialog fail.." + JSON.stringify(result));
								}, "PollPlugin", "showQuestionArrivedDialog", [param]);

    							return;
                            }
						} else {
							Ctrl.Msg.show(_msg("poll.alert.answer.fail"));
							return;
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){}
				});
			},


            /**
			 * PollCtrl.Action.Attender.readyToAnswerDrawingQuestion
			 *  - 학생방에서 학생이 폴 질문을 받았을 때, 학생방에 질문 이미지를 세팅해주는 함수
			 */
			readyToAnswerDrawingQuestion : function(pollNo) {
                console.log("PollCtrl.Action.Attender.readyToAnswerDrawingQuestion");
                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + 'mapi/poll/subroom/file/add.json'

				var param = {
					pollno : pollNo,
					roomid : RoomSvr.roomid,
					deviceid : RoomSvr.deviceid
				};

				$.ajax({
					type : 'post',
					url : url,
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
                    beforeSend : function(xhr) {
    					xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
    				},
					success : function(data) {
						console.log("[readyToAnswerDrawingQuestion] poll/subroom/file/add.json success ----- data : " + JSON.stringify(data));
						if (data.result == '0') {
                            var imageMap = data.map;
                            Ctrl.BGImg.init(imageMap);
				            //Ctrl.toggleRC(0, -1, false);   // 이미지 업로드 후 손 모드

							var param = {
								code : "",
								pollno : pollNo,
								timelimit : "",
								iscountdown : "",
								title : imageMap.filedata,
								url : imageMap.dnloadurl,
								ismove : false
							};
							
							/*
							cordova.exec(function(result) {
								console.log("cordova exitPoll success..");
							}, function(result) {
								console.log("cordova exitPoll fail.." + JSON.stringify(result));
							}, "PollPlugin", "moveSubroom", [param]);
							*/

							cordova.exec(function(result) {
								console.log("cordova showQuestionArrivedDialog success..");
							}, function(result) {
								console.log("cordova showQuestionArrivedDialog fail.." + JSON.stringify(result));
							}, "PollPlugin", "showQuestionArrivedDialog", [param]);

							return;
						} else {
							Ctrl.Msg.show(_msg("poll.alert.answer.fail"));
							return;
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){}
				});
			}
        },

        Common : {
            // 설문 결과 그래프 생성
            makePollResult : function(pollNo, totalUserCnt, presentor) {
                var isMaster = PacketMgr.isMC;

                //var pollNo = '334662b6596ke67a146a';

                var param = { pollno : pollNo };

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.get");
                console.log(url);
                $.ajax({
                    type  : 'post',
                    url   : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
						console.log("[makePollResult] poll/get.json success / data : " + JSON.stringify(data));
                        if(data.result == '0') {
                            var resultMap = data.map;
                            var pollType = resultMap.polltype;
                            
							/*
							if(pollType == '3') {
								// TODO 판서형 폴 결과값 렌더링

								$.ajax({
									type : 'post',
									url  : svrHost + 'mapi/poll/answer/list.json',
									async : true,
									cache : false,
									dataType : 'json',
									data : { pollno : pollNo },
									success : function(data) {
										console.log("[makePollResult] poll/answer/list.json success / data : " + JSON.stringify(data));
										if(data.result == 0) {
											//var answerTotalCnt = parseInt(resultMap.answertotalcnt);
											resultMap.answertotalcnt = totalUserCnt;
											resultMap.answerusercnt  = parseInt(resultMap.answerusercnt);
											resultMap.itemlist = data.list;

										} else if(data.result == -103) {
											resultMap.answertotalcnt = totalUserCnt;
											resultMap.answerusercnt = 0;
											resultMap.itemlist = [];
										}

										cordova.exec(function(result) {
											console.log("cordova.exec() success.. getCompletePollDetail");
										}, function(result) {
											alert('getCompletePollDetail err : ' + JSON.stringify(result));
										}, "PollPlugin", "getCompletePollDetail", [resultMap]);
									}
								});
							} else {
								cordova.exec(function(result) {
									console.log("cordova.exec() success.. getCompletePollDetail");
								}, function(result) {
									alert('getCompletePollDetail err : ' + JSON.stringify(result));
								}, "PollPlugin", "getCompletePollDetail", [resultMap]);
							}*/
							
							resultMap['presentor'] = presentor;
							
							cordova.exec(function(result) {
								console.log("cordova.exec() success.. getCompletePollDetail");
							}, function(result) {
								alert('getCompletePollDetail err : ' + JSON.stringify(result));
							}, "PollPlugin", "getCompletePollDetail", [resultMap]);
							
	                    }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){ Utils.log("complete..") }
                });
            },
            getAnswerFile : function(pollNo, pollUserNo, el) {
                var param = {
                    pollno : pollNo,
                    polluserno : pollUserNo
                };

                var svrFlag = _prop("svr.flag");
                var svrHost = _prop("svr.host." + svrFlag);
                var url = svrHost + _prop("poll.file.item.get");

                $.ajax({
                    type  : 'post',
                    url   : url,
                    async : true,
                    cache : false,
                    dataType : 'json',
                    data : param,
                    beforeSend : function(){},
                    success : function(data) {
                        Utils.log("success..");
                        Utils.log(data);
                        if(data.result == '0') {
                            var resultMap = data.map;
                            var fileNo = resultMap.POLLFILENO;
                            var filePath = resultMap.filepath;
                            //PollCtrl.UI.renderUserPollFile(fileNo, filePath, pollUserNo, el);

                        }
                    },
                    error : function(){ Utils.log("error..") },
                    complete : function(){ Utils.log("complete..") }
                });
            }
        }
    },

    Event : {
        bindClickEvent : function() {
            var tmpPenSize = 0;
            var penOpts = {
                "cmd" : "pensetting",
                "from" : "pensetting",
                "menuselect" : 4,
                "line_width" : 1,
                "r_color" : 0,
                "g_color" : 0,
                "b_color" : 0
            };

            // 일괄 삭제 모드
            $("#answer_clear").click(function() {
                PollCtrl.drawingTool["clean"]();
            });

            // 펜 모드
            $("#answer_pen").click(function() {
                $("div.answer_draw_menu > a[title='pen']").removeClass("checked");
                $(this).addClass("checked");
                $($("div#answer_pen_size > a[title='color']").get(2)).addClass("checked");
                $($("div#answer_pen_color > a[title='color']").get(2)).addClass("checked");

                penOpts["menuselect"] = 4;
                //penOpts["line_width"] = tmpPenSize > 0 ? tmpPenSize : 3;
                penOpts["line_width"] = 1;
                penOpts["r_color"] = 0;
                penOpts["g_color"] = 0;
                penOpts["b_color"] = 0;

                PollCtrl.drawingTool["async"](penOpts);
                //PollCtrl.UI.Cursor.change("hpen");
            });

            // 지우개 모드
            $("#answer_eraser").click(function() {
                $("div.answer_draw_menu > a[title='pen']").removeClass("checked");
                $("div#answer_pen_size > a[title='color']").removeClass("checked");
                $("div#answer_pen_color > a[title='color']").removeClass("checked");
                $(this).addClass("checked");

                penOpts["menuselect"] = 5;
                penOpts["line_width"] = 7;

                PollCtrl.drawingTool["async"](penOpts);
                //PollCtrl.UI.Cursor.change("del");
            });

            // 펜 크기 1
            $("#pen_size1").click(function(){
                if($("#answer_eraser").attr("class").indexOf("checked")!= -1) return;

                $("#answer_pen_size > a").removeClass("checked");
                $(this).addClass("checked");

                penOpts["line_width"] = 1;
                tmpPenSize = 1;
                PollCtrl.drawingTool["async"](penOpts);
            });

            // 펜 크기 2
            $("#pen_size2").click(function(){
                if($("#answer_eraser").attr("class").indexOf("checked")!= -1) return;

                $("#answer_pen_size > a").removeClass("checked");
                $(this).addClass("checked");

                penOpts["line_width"] = 3;
                tmpPenSize = 3;
                PollCtrl.drawingTool["async"](penOpts);
            });

            // 펜 크기 3
            $("#pen_size3").click(function(){
                if($("#answer_eraser").attr("class").indexOf("checked")!= -1) return;

                $("#answer_pen_size > a").removeClass("checked");
                $(this).addClass("checked");

                penOpts["line_width"] = 7;
                tmpPenSize = 7;
                PollCtrl.drawingTool["async"](penOpts);
            });

            // 펜 색상 1
            $("#pen_color1").click(function(){
                if($("#answer_eraser").attr("class").indexOf("checked")!= -1) return;

                $("#answer_pen_color > a").removeClass("checked");
                $(this).addClass("checked");

                penOpts["r_color"] = 0;
                penOpts["g_color"] = 0;
                penOpts["b_color"] = 0;

                PollCtrl.drawingTool["async"](penOpts);
            });

            // 펜 색상 2
            $("#pen_color2").click(function(){
                if($("#answer_eraser").attr("class").indexOf("checked")!= -1) return;

                $("#answer_pen_color > a").removeClass("checked");
                $(this).addClass("checked");

                penOpts["r_color"] = 250;
                penOpts["g_color"] = 150;
                penOpts["b_color"] = 10;

                PollCtrl.drawingTool["async"](penOpts);
            });

            // 펜 색상 3
            $("#pen_color3").click(function(){
                if($("#answer_eraser").attr("class").indexOf("checked")!= -1) return;

                $("#answer_pen_color > a").removeClass("checked");
                $(this).addClass("checked");

                penOpts["r_color"] = 150;
                penOpts["g_color"] = 250;
                penOpts["b_color"] = 10;

                PollCtrl.drawingTool["async"](penOpts);
            });
        },

        unbindClickEvent : function() {
            $("#answer_clear").unbind("click");
            $("#answer_pen").unbind("click");
            $("#answer_eraser").unbind("click");

            $("#pen_size1").unbind("click");
            $("#pen_size2").unbind("click");
            $("#pen_size3").unbind("click");

            $("#pen_color1").unbind("click");
            $("#pen_color2").unbind("click");
            $("#pen_color3").unbind("click");
        }

    },


    UI : {
        ROWS_PER_PAGE   : 10,   // 한 페이지당 노출되는 질문 개수
        PAGES_PER_BLOCK : 5,    // 한 블럭당 노출되는 페이지 개수


        // 설문조사 결과 그래프 렌더링..
        renderPollGraph : function(dataMap, isMaster) {
            var itemIdx        = dataMap.itemidx;
            var itemNm         = dataMap.itemnm;
            var pollItemNo     = dataMap.pollitemno;
            var pollNo         = dataMap.pollno;
            var answerTotalCnt = parseInt(dataMap.answertotalcnt);
            var answerUserCnt  = parseInt(dataMap.answerusercnt);
            var itemList       = dataMap.itemlist;

            var containerIdStr  = null;
            var resultListIdStr = null;
            var container;

            if(isMaster) {
                containerIdStr  = "poll_result_box_admin";
                resultListIdStr = "poll_result_graph_admin";
            } else {
                containerIdStr  = "poll_result_box_user";
                resultListIdStr = "poll_result_graph_user";
            }

            container = document.getElementById(containerIdStr);

            if(container) {
                $(container).show();
                $("#" + resultListIdStr).empty();
            } else {
                var htmlStr = "<div id=\"" + containerIdStr + "\" class=\"poll_box\" style=\"display: none;\">\
                <span class=\"poll_header\">\
                <span class=\"pop_tit\">" + _msg("poll.result.header") + "</span>\
                <a onclick=\"javascript://PollCtrl.UI.close('" + containerIdStr + "');\"></a>\
                </span>\
                <div class=\"poll_body\">\
                <ul id=\"" + resultListIdStr + "\" class=\"poll_result\"></ul>\
                <div class=\"people_result\">( " + _msg("poll.people.total") + " : <a id=\"total_cnt\"></a> / " + _msg("poll.people.voted") + " : <a id=\"answer_cnt\"></a> )</div>\
                </div>\
                </div>";
                $("#wrap").append(htmlStr);
            }


            var len = itemList.length;
            for(var i=0; i<len; i++) {
                var itemInfo = itemList[i];
                var itemIdx   = itemInfo.itemidx;
                var itemNm    = itemInfo.itemnm;
                var answerCnt = parseInt(itemInfo.answercnt);

                //Utils.log(answerCnt + "/" + answerTotalCnt);
                //Utils.log(answerCnt/answerTotalCnt);

                var rate = answerCnt / answerTotalCnt * 100;
                if(answerCnt == 0 && answerTotalCnt == 0 && isNaN(rate))
                    rate = 0;

                var width = 100 * rate / 100;
                width += 140;   // 그래프 미니멈 픽셀값

                // 아이템 그래프 영역
                var listEl = document.createElement("li");

                var idxSpan = document.createElement("span");
                idxSpan.className = "examno" + itemIdx;
                idxSpan.innerHTML = itemIdx + ".";

                var barSpan = document.createElement("span");
                barSpan.className = "exam_value" + itemIdx;
                barSpan.style.width = width + "px";

                if(!isMaster) {
                    //PollCtrl.UI.open("poll_result_box_user");

                    var percentSpan = document.createElement("span");
                    percentSpan.className = "value_per";
                    percentSpan.innerHTML = parseInt(rate) + "%";

                    var itemNmSpan = document.createElement("span");
                    itemNmSpan.className = "short_value";
                    itemNmSpan.innerHTML = itemNm;
                    barSpan.appendChild(percentSpan);
                    barSpan.appendChild(itemNmSpan);
                } else {
                    //PollCtrl.UI.open("poll_result_box_admin");

                    barSpan.innerHTML = parseInt(rate) + "%";

                    //$("#poll_report_send_btn").prop("href", "javascript:PollCtrl.Action.Master.sendPollReport('" + pollNo + "');")

                    if(document.getElementById("poll_report_send_btn")) {

                    } else {
                        var reportSendBtn =
                        "<div class=\"poll_btn\">\
                        <a id=\"poll_report_send_btn\" class=\"btn_full\">" + _msg("poll.btn.send") + "</a>\
                        </div>";
                        $(".poll_body", "#"+containerIdStr).append(reportSendBtn);
                    }
                    $("#poll_report_send_btn").unbind("click");
                    $("#poll_report_send_btn").bind("click", function(e) {
                        e.preventDefault();
                        PollCtrl.Action.Master.sendPollReport(pollNo);
                    });

                }

                var cntSpan = document.createElement("span");
                cntSpan.className = "selectuser_no";
                cntSpan.innerHTML = answerCnt + "People";

                listEl.appendChild(idxSpan);
                listEl.appendChild(barSpan);
                listEl.appendChild(cntSpan);

                $("#" + resultListIdStr).append($(listEl));
                $("#total_cnt", "#"+containerIdStr).text(answerTotalCnt);
                $("#answer_cnt", "#"+containerIdStr).text(answerUserCnt);

                /*
                 if(!isMaster) {
                 $("#poll_result_graph_user").append($(listEl));
                 $("#total_cnt_user").text(answerTotalCnt);
                 $("#answer_cnt_user").text(answerUserCnt);
                 } else {
                 $("#poll_result_graph_admin").append($(listEl));
                 $("#total_cnt_admin").text(answerTotalCnt);
                 $("#answer_cnt_admin").text(answerUserCnt);
                 }*/
            }

        },

        renderUserPollFile : function(fileNo, filePath, pollUserNo, el) {
            $(".answered_user", "#poll_result_hand_writing").removeClass("selected");
            console.log($(el));
            $(el).addClass("selected");
            var answerImgContainer = $("#answer_img");
            answerImgContainer.empty();

            var imgEl = document.createElement("img");
            imgEl.src = filePath;

            answerImgContainer.append($(imgEl));

            $("#answer_draw_send").attr("onclick", "javascript:PollCtrl.Action.Master.Question.shareDrawAnswer('" + fileNo + "','" + filePath + "','" + pollUserNo + "');");
        },

        // 판서 질의 결과 렌더링..
        renderHandWritingResult : function(pollNo, totalCnt, answerCnt, answerList, isMaster) {

            var containerIdStr = "poll_result_hand_writing";
            var container = document.getElementById(containerIdStr);

            if(container) {
                //PollCtrl.UI.open(containerIdStr);
            } else {
                var htmlStr = "<div id=\"poll_result_hand_writing\" class=\"poll_box2\">\
                <span class=\"poll_header\">\
                <span class=\"pop_tit\">" + _msg("poll.write.result.header") + "</span>\
                <a onclick=\"javascript://PollCtrl.UI.close('" + containerIdStr + "');\"></a>\
                </span>\
                <div class=\"poll_body\">\
                <div class=\"answer_input_box\">\
                <span class=\"poll_tit2\"><span>" + _msg("poll.write.sub.title") + "</span><span class=\"sub_scripts\">( " + _msg("poll.title.total") + " <a id=\"draw_total_cnt\"></a> " + _msg("poll.title.in") + " <a id=\"draw_answer_cnt\"></a> " + _msg("poll.title.answered.text") + " )</span></span>\
                <div id=\"answer_user_list\" class=\"ansewred_list\"></div>\
                <div id=\"answer_img\" class=\"answer_img\"></div>\
                </div>\
                <div class=\"poll_btn\">\
                <a href=\"#\" id=\"answer_draw_send\" class=\"btn_full\">" + _msg("poll.btn.share") + "</a>\
                </div>\
                </div>\
                </div>";
                $("#wrap").append(htmlStr);
            }


            //console.log(answerList);
            /*
             email: ""
             guesttype: "1"
             ipaddr: "192.168.0.195"
             polluserno: "334d320c31fn6e4040fa"
             regdatetime: "20150608212404"
             svctype: "0"df
             telno: ""
             userkey: "773524d462"
             usernm: "22(Guest)"
             */
            //$("#poll_result_graph_user").empty();

            var answerListEl = $("#answer_user_list");
            answerListEl.empty();
            $("#draw_total_cnt").text(totalCnt);
            $("#draw_answer_cnt").text(answerCnt);

            var len = answerList.length;
            /*
             {"cmd":"img","seqno":"334be98cddei6307f9dbccc","posx":"154.79999999999998","posy":"120.52412109375001","scalew":"1.184375","scaleh":"1.1830985915492958","url":"http://fb.wenote.com:80/data/fb/hashs/486/4867e62ca8d0be63dba5baa041de8adf"}
             */
            if(len > 0) {
                for(var i=0; i<len; i++) {
                    var answerUser = answerList[i];
                    var pollUserNo = answerUser.polluserno;
                    var userNm = answerUser.usernm;
                    var email = answerUser.email;
                    var thumbnailSrc = answerUser.thumbnail;
                    //Utils.log(answerCnt + "/" + answerTotalCnt);
                    //Utils.log(answerCnt/answerTotalCnt);

                    // 아이템 그래프 영역
                    var userAnchor = document.createElement("a");
                    if(i == 0) {
                        userAnchor.className = "answered_user selected";
                        PollCtrl.Action.Common.getAnswerFile(pollNo, pollUserNo, userAnchor);
                    }
                    else
                        userAnchor.className = "answered_user";
                    userAnchor.setAttribute("onclick", "javascript:PollCtrl.Action.Common.getAnswerFile('" + pollNo + "','" + pollUserNo + "', $('#answer_user_list').children().get(" + i + "))");

                    var thumbnail = document.createElement("img");
                    thumbnail.className = "user_photo";
                    thumbnail.src = thumbnailSrc == "" ? Utils.addContext(_url("profile.default")) : thumbnailSrc;

                    var userNameSpan = document.createElement("span");
                    userNameSpan.innerHTML = userNm;

                    userAnchor.appendChild(thumbnail);
                    userAnchor.appendChild(userNameSpan);

                    $(answerListEl).append(userAnchor);
                }
            }
        },



        // 설문조사 창 렌더링..
        renderPollSheet : function(resultMap) {
            Utils.log("renderPollSheet - " + resultMap);
            var pollNo       = resultMap.pollno;
            var pollType     = resultMap.polltype;
            var pollCategory = resultMap.pollcategory;
            var pollTitle    = resultMap.title;
            var itemList     = resultMap.itemlist;
            var allowCnt     = resultMap.allowcnt;

            var pollNoHiddenEl = document.createElement("input");
            pollNoHiddenEl.type = "hidden";
            pollNoHiddenEl.id = "sheetPollNo";
            pollNoHiddenEl.value = pollNo;

            $("#sheetPollNo").remove();  // 폴넘버를 임시로 저장하고 있는 히든 인풋을 삭제..

            if(pollType == '0') {
                //PollCtrl.UI.open("multi_choice_sheet");
                $("#multi_choice_sheet .question_text").text(pollTitle);

                var container = $("#multi_choice_item");
                container.empty();
                var itemInput = allowCnt > 1 ? "checkbox" : "radio";
                var len = itemList.length;

                for(var i=0; i<len; i++) {
                    var item = itemList[i];
                    var itemNo  = item.pollitemno;
                    var itemIdx = item.itemidx;
                    var itemNm  = item.itemnm;

                    var listEl  = document.createElement("li");

                    var inputEl = document.createElement("input");
                    inputEl.type = itemInput;
                    inputEl.name = "poll_choice_item";
                    inputEl.className = "answer_choice";
                    inputEl.id = "ac" + itemIdx;
                    inputEl.value = itemNo;

                    var spanEl = document.createElement("span");
                    spanEl.className = "answer_no";
                    spanEl.innerHTML = itemIdx;

                    var labelEl = document.createElement("label");
                    labelEl.htmlFor = "ac" + itemIdx;
                    labelEl.className = "answer_value";
                    labelEl.innerHTML = itemNm;

                    listEl.appendChild(inputEl);
                    listEl.appendChild(spanEl);
                    listEl.appendChild(labelEl);

                    container.append($(listEl));
                }
                container.append($(pollNoHiddenEl));

            } else if(pollType == '1') {
                //PollCtrl.UI.open("alternative_sheet");
                $("#alternative_sheet .question_text").text(pollTitle);

                var container = $("#alternative_item");
                container.empty();
                var len = itemList.length;
                for(var i=0; i<len; i++) {
                    var item = itemList[i];
                    var itemNo  = item.pollitemno;
                    var itemIdx = item.itemidx;
                    var itemNm  = item.itemnm;

                    var listEl  = document.createElement("li");

                    var inputEl = document.createElement("input");
                    inputEl.type = "radio";
                    inputEl.name = "poll_choice_item";
                    inputEl.className = "answer_choice";
                    inputEl.id = "ac" + itemIdx;
                    inputEl.value = itemNo;

                    var spanEl = document.createElement("span");
                    spanEl.className = "answer_no";
                    spanEl.innerHTML = itemIdx;

                    var labelEl = document.createElement("label");
                    labelEl.htmlFor = "ac" + itemIdx;
                    labelEl.className = "answer_value";
                    labelEl.innerHTML = itemNm;

                    listEl.appendChild(inputEl);
                    listEl.appendChild(spanEl);
                    listEl.appendChild(labelEl);

                    container.append($(listEl));
                }
                container.append($(pollNoHiddenEl));

            } else if(pollType == '2') {
                if(pollCategory == '1') {
                    //PollCtrl.UI.open("text_answer_sheet");
                    $("#text_answer_sheet .question_text").text(pollTitle);
                    $("#text_answer_sheet").append($(pollNoHiddenEl));
                    $(".answer_text", "#text_answer_sheet").val("");
                } else {
                    //PollCtrl.UI.open("short_answer_sheet");
                    $("#short_answer_sheet .question_text").text(pollTitle);
                    $("#short_answer_sheet").append($(pollNoHiddenEl));
                    $(".answer_short", "#short_answer_sheet").val("");
                }


            } else if(pollType == '3') {

                //PollCtrl.UI.open("hand_writing_sheet");
                console.log("판서형 렌더링");
                $("#hand_writing_sheet .question_text").text(pollTitle);
                // TODO - Canvas initialize..
                var canvasWrapper = "#answer_canvas";
                $("#answer_canvas").empty();

                var app = new CustomBoard();
                PollCtrl.drawingTool = app.init(canvasWrapper, 0, 0);



                PollCtrl.Event.bindClickEvent();
                //PollCtrl.UI.Cursor.change("hpen");
                $("#hand_writing_sheet").append($(pollNoHiddenEl));
            }
        },


        // 폴 업데이트 창 열기
        renderPollUpdateFrm : function(pollInfo) {
            var pollNo    = pollInfo.polltempno;
            var pollType  = pollInfo.polltype;
            var pollTitle = pollInfo.title;
            var itemList  = pollInfo.itemlist;
            var allowCnt  = parseInt(pollInfo.allowcnt);

            $("#poll_title").val(pollTitle);

            $(".poll_btn", "#poll_info_box").addClass("edit_mode");
            $("#poll_update_btn").css("display", "block");
            $("#poll_remove_btn").css("display", "block");
            $("#poll_save_btn").css("display", "none");

            $("#poll_update_btn").prop("href", "javascript:PollCtrl.Action.Master.updatePoll('" + pollNo + "', false);");
            $("#poll_remove_btn").prop("href", "javascript:PollCtrl.Action.Master.deletePoll('" + pollNo + "');");
            $("#poll_send_btn").prop("href", "javascript:PollCtrl.Action.Master.updatePoll('" + pollNo + "', true);");

            if(pollType == '0') {
                $("#multi_type").prop("checked", true);
            } else if(pollType == '1') {
                $("#alter_type").prop("checked", true);
            } else if(pollType == '2') {
                $("#short_type").prop("checked", true);
            }

            if(allowCnt > 1)
                $("#dup_choice_allow").prop("checked", true)
                else
                    $("#dup_choice_allow").prop("checked", false)

                    //PollCtrl.UI.toggleItemForm(pollType);

                    var container = pollType == '0' ? $("#item_rows") :
                    pollType == '1' ? $("#alternative_box .alternative_direct") : $("#short_box");

            container.empty();

            if(container.css("display") == "none") {
                container.css("display", "block");
            }

            var len = itemList.length;
            if(pollType == '0' || pollType == '1') {
                for(var i=0; i<len; i++) {
                    console.log(itemList);
                    var itemInfo = itemList[i];
                    var pollItemNo = itemInfo.pollitemno;
                    var pollNo     = itemInfo.polltempno;
                    var itemNm     = itemInfo.itemnm;
                    var itemIdx    = itemInfo.itemidx;
                    console.log(itemNm);

                    var spanEl = document.createElement("span");
                    spanEl.innerHTML = itemIdx + ".";

                    var inputEl = document.createElement("input");
                    inputEl.type = "text";
                    inputEl.placeholder = "#" + itemIdx + " " + _msg("poll.answer.input");
                    inputEl.value = itemNm;

                    container.append($(spanEl));
                    container.append($(inputEl));
                }
            } else {
                // pollType == '2' 일때는 poll item 영역을 렌더링하지 않음..
            }
        },


        // 질의 템플릿 리스트 렌더링
        renderPollList : function(resultMap, pageNum) {
            var pollList = resultMap.list;
            var pollTotalCnt = resultMap.totalcount;
            var container = $("#poll_list");
            var pageContainer = $("#page_navi");
            container.empty();
            pageContainer.empty();


            var len = pollList.length;
            if(len > 0) {
                for(var i=0; i<len; i++) {
                    //                    var pollIdx = ((pageNum-1) * PollCtrl.UI.ROWS_PER_PAGE) + (i+1);
                    var pollInfo = pollList[i];
                    var pollTempNo = pollInfo.polltempno;
                    var pollKey = pollInfo.pollkey;
                    var pollUserNo = pollInfo.polluserno;
                    var pollTitle = pollInfo.title;
                    var userKey = pollInfo.userkey;
                    var userNm = pollInfo.usernm;

                    var listEl  = document.createElement("li");

                    var inputEl = document.createElement("input");
                    inputEl.type = "radio";
                    inputEl.id = "q" + pollIdx;
                    inputEl.name = "poll_item";
                    inputEl.value = pollTempNo;

                    var spanEl = document.createElement("span");
                    spanEl.innerHTML = "Q" + pollIdx + ".";

                    var labelEl = document.createElement("label");
                    labelEl.htmlFor = "q" + pollIdx;
                    labelEl.innerHTML = pollTitle;
                    labelEl.setAttribute("onclick", "PollCtrl.Action.Master.makePollUpdateFrm('" + pollTempNo + "')");

                    listEl.appendChild(inputEl);
                    listEl.appendChild(spanEl);
                    listEl.appendChild(labelEl);

                    container.append($(listEl));
                }

                //PollCtrl.UI.renderPager("TMP_POLL", pageNum, pollTotalCnt);

            } else {  // no List
                var listEl  = document.createElement("li");
                listEl.className = "nolist";
                container.append($(listEl));
            }
        },


        // 문답 완료된 리스트 렌더링
        renderCompletedPollList : function(resultMap, pageNum) {
            var pollList = resultMap.list;
            var pollTotalCnt = resultMap.totalcount;
            var container = $("#poll_complete_list");
            container.empty();

            if(resultMap.result == -103) {
                var listEl  = document.createElement("li");
                listEl.className = "nolist";
                container.append($(listEl));
                return;
            } else if (resultMap.result == 0) {
                var len = pollList.length;
                if(len > 0) {
                    for(var i=0; i<len; i++) {
                        var pollIdx    = i+1;
                        var pollInfo   = pollList[i];
                        var pollNo     = pollInfo.pollno;
                        var pollKey    = pollInfo.pollkey;
                        var pollUserNo = pollInfo.polluserno;
                        var pollTitle  = pollInfo.title;
                        var userKey    = pollInfo.userkey;
                        var userNm     = pollInfo.usernm;

                        var listEl  = document.createElement("li");

                        var inputEl = document.createElement("input");
                        inputEl.type = "checkbox";
                        inputEl.id = "q" + pollIdx;
                        inputEl.name = "poll_item_ended";
                        inputEl.value = pollNo;

                        var spanEl = document.createElement("span");
                        spanEl.innerHTML = "Q" + pollIdx + ".";

                        var labelEl = document.createElement("label");
                        labelEl.htmlFor = "q" + pollIdx;
                        labelEl.innerHTML = pollTitle;
                        labelEl.setAttribute("onclick", "PollCtrl.Action.Master.showPollResult('" + pollNo + "')");

                        listEl.appendChild(inputEl);
                        listEl.appendChild(spanEl);
                        listEl.appendChild(labelEl);

                        container.append($(listEl));
                    }

                    //PollCtrl.UI.renderPager("POLL", pageNum, pollTotalCnt);

                }
            }

        },

        // type = "TMP_POLL" / "POLL"
        renderPager : function(type, pageNum, totalCnt) {
            var totalBlocks  = totalCnt/this.PAGES_PER_BLOCK;
            var currentBlock = Math.ceil(pageNum/this.PAGES_PER_BLOCK);
            var endPageNum   = currentBlock * this.PAGES_PER_BLOCK;
            var startPageNum = endPageNum - this.PAGES_PER_BLOCK;

            var pageContainer = type == "TMP_POLL" ? $("#page_navi") : $("#page_navi_complete");
            pageContainer.empty();

            // first, previous 영역
            if(currentBlock > 1) {
                var prevPageNum = (currentBlock * this.PAGES_PER_BLOCK) - this.PAGES_PER_BLOCK;
                var list1 = document.createElement("li");
                var firstAnchor = document.createElement("a");
                firstAnchor.className = "btn_first";
                firstAnchor.title = "first";
                if(type == "TMP_POLL")
                    firstAnchor.href = "javascript:PollCtrl.Action.Master.makePollList(1);";
                else if(type == "POLL")
                    firstAnchor.href = "javascript:PollCtrl.Action.Master.makePollCompleteList(1);";
                list1.appendChild(firstAnchor);

                var list2 = document.createElement("li");
                var prevAnchor = document.createElement("a");
                prevAnchor.className = "btn_pre";
                prevAnchor.title = "previous";
                if(type == "TMP_POLL")
                    prevAnchor.href = "javascript:PollCtrl.Action.Master.makePollList(" + prevPageNum + ");";
                else if(type == "POLL")
                    prevAnchor.href = "javascript:PollCtrl.Action.Master.makePollCompleteList(" + prevPageNum + ");";
                list2.appendChild(prevAnchor);

                pageContainer.append($(list1));
                pageContainer.append($(list2));
            }

            // pageNums 영역
            for(var i=startPageNum; i<=endPageNum; i++) {
                var currentRows = i * this.ROWS_PER_PAGE;
                var maxRowsPerBlock = currentBlock * this.PAGES_PER_BLOCK * this.ROWS_PER_PAGE;  // 현재 블럭 기준의 최대 질문 개수

                if(currentRows >= totalCnt) break;
                if(currentRows >= maxRowsPerBlock) break;

                var pageNo = i+1;
                var pageList = document.createElement("li");
                var pageAnchor = document.createElement("a");
                pageAnchor.title = pageNo;
                pageAnchor.innerHTML = pageNo;
                if(type == "TMP_POLL")
                    pageAnchor.href = "javascript:PollCtrl.Action.Master.makePollList(" + pageNo + ");";
                else if(type == "POLL")
                    pageAnchor.href = "javascript:PollCtrl.Action.Master.makePollCompleteList(" + pageNo + ");";
                if(pageNum == pageNo) {
                    pageAnchor.className = "now";
                }
                pageList.appendChild(pageAnchor);
                pageContainer.append($(pageList));
            }

            // last, next 영역
            if(currentBlock * this.PAGES_PER_BLOCK * this.ROWS_PER_PAGE < totalCnt ) {

                var lastPageNum = Math.ceil(totalCnt/this.ROWS_PER_PAGE);
                var nextPageNum = (currentBlock * this.PAGES_PER_BLOCK) + 1;

                var list1 = document.createElement("li");
                var nextAnchor = document.createElement("a");
                nextAnchor.className = "btn_next";
                nextAnchor.title = "next";
                if(type == "TMP_POLL")
                    nextAnchor.href = "javascript:PollCtrl.Action.Master.makePollList(" + nextPageNum + ");";
                else if(type == "POLL")
                    nextAnchor.href = "javascript:PollCtrl.Action.Master.makePollCompleteList(" + nextPageNum + ");";
                list1.appendChild(nextAnchor);

                var list2 = document.createElement("li");
                var lastAnchor = document.createElement("a");
                lastAnchor.className = "btn_last";
                lastAnchor.title = "last";
                if(type == "TMP_POLL")
                    lastAnchor.href = "javascript:PollCtrl.Action.Master.makePollList(" + lastPageNum + ");";
                else if(type == "POLL")
                    lastAnchor.href = "javascript:PollCtrl.Action.Master.makePollCompleteList(" + lastPageNum + ");";
                list2.appendChild(lastAnchor);

                pageContainer.append($(list1));
                pageContainer.append($(list2));
            }

        },

        // 스톱워치 애니메이션 스무뜨 버전..
        animateStopWathSmooth : function(totalMs) {
            //totalMs = (total) * 1000;

            $({deg: 0}).animate({deg: 360},
                                {
                                    duration : totalMs + 1200,
                                    step : function(angle) {
                                        if(angle <= 180) {
                                            $("#rotateLeft").show();
                                            $("#rotateRight").hide();
                                            $("#rotateLeft").css({ WebkitTransform: 'rotate(' + angle + 'deg)'});
                                            $("#rotateLeft").css({ '-moz-transform': 'rotate(' + angle + 'deg)'});
                                        } else {
                                            $("#rotateLeft").show();
                                            $("#rotateRight").show();
                                            angle = angle-180;

                                            $("#rotateLeft").css({ WebkitTransform: 'rotate(180deg)'});
                                            $("#rotateLeft").css({ '-moz-transform': 'rotate(180deg)'});
                                            $("#rotateRight").css({ WebkitTransform: 'rotate(' + angle + 'deg)'});
                                            $("#rotateRight").css({ '-moz-transform': 'rotate(' + angle + 'deg)'});
                                        }
                                    }
                                }
                                );
        },

        // 스톱워치 애니메이션..
        animateStopWatch : function(current, total) {
            var angle = (360 * (current-1)) / total;

            if(current == 0) {
                $("#rotateLeft").show();
                $("#rotateRight").hide();

                $("#rotateLeft").css({ WebkitTransform: 'rotate(0deg)'});
                $("#rotateLeft").css({ '-moz-transform': 'rotate(0deg)'});
            }

            if(angle <= 180) {
                $("#rotateLeft").css({ WebkitTransform: 'rotate(' + angle + 'deg)'});
                $("#rotateLeft").css({ '-moz-transform': 'rotate(' + angle + 'deg)'});
            } else {
                $("#rotateRight").show();
                $("#rotateRight").show();
                angle = angle-180;

                $("#rotateLeft").css({ WebkitTransform: 'rotate(180deg)'});
                $("#rotateLeft").css({ '-moz-transform': 'rotate(180deg)'});
                $("#rotateRight").css({ WebkitTransform: 'rotate(' + angle + 'deg)'});
                $("#rotateRight").css({ '-moz-transform': 'rotate(' + angle + 'deg)'});
            }

        },

        onSelectTime : function() {
            var val = $("#shutdown_time").val();
            console.log(val);
            if(val == "custom") {
                $("#custom_time").show();
            } else {
                $("#custom_time").hide();
            }
        },

        Cursor : {
            // mode_hand, mode_pen, mode_hpen, mode_del
            list : ["hand", "hpen", "del", "diagram", "text"],
            change : function(type){
                //  mode_hpen, mode_del, mode_pen, mode_hand, mode_diagram
                var modeStr = "mode_" +  (Utils.browser("msie") ? "ie_" : "") + type;

                $("#cursor_wrap").attr("class", "");

                $("#cursor_wrap").addClass(modeStr);
            },

            get : function(){
                cursor = Ctrl.isHand() ? "hand" : (this.penIdx == 1) ? "hpen" : (this.penIdx == 2) ? "hpen" : (this.penIdx == 3) ? "del" : (this.penIdx == 5 || this.penIdx == 6 || this.penIdx == 7) ? "diagram" : "";
                return cursor;
            }
        },

		drawingContainerMode : "question",

		captureCanvas : function (mode, startX, startY, sWidth, sHeight, density) {
			var isFullScreen = (typeof startX == 'undefined' || typeof startY == 'undefined' || typeof sWidth == 'undefined' || typeof sHeight == 'undefined') ? true : false;
			console.log("[PollCtrl.UI.captureCanvs] mode : " + mode + ", startX : " + startX + ", startY : " + startY + ", sWidth : " + sWidth + ", sHeight : " + sHeight);
			try {
				var board = UI.getBoard();
				if (!isFullScreen) {
                    var scrollLeft = $(document).scrollLeft();  // 폰에서 가로스크롤 시 scrollLeft값을 구함..
                    var scrollTop = $(document).scrollTop();    // 테블릿에서 세로스크롤 시 scrollTop값을 구함..
					board.saveCapture(startX/density + scrollLeft, startY/density + scrollTop, sWidth/density, sHeight/density);
				} else {
					board.saveCapture(startX, startY, sWidth, sHeight);
				}


				var saveCanvas = $("#saveCanvas").get(0);
				if (saveCanvas){
					// make represent thumbnail
					//console.log("Captured canvas : " + saveCanvas.toDataURL());

					if(Utils.cordova()) {
						var binary = saveCanvas.toDataURL().split(',')[1];
						$(saveCanvas).remove();

						var param = {
							img : binary,
							isfullscreen : isFullScreen,
                            mode : mode
						};

						cordova.exec(function(result) {
							console.log("cordova.exec() success.. confirmCapture");
						}, function(result) {
							console.log('confirmCapture err : ' + JSON.stringify(result));
						}, "PollPlugin", "confirmCapture", [param]);
					}
				}
			} catch (e) {
				console.log(e);
				var saveCanvas = $("#saveCanvas").get(0);
				if(saveCanvas){
					  $(saveCanvas).remove();
				}
			}
		},


        /**
		 * PollCtrl.UI.updateAnswerUser
		 * - 답변 완료한 유저에 대해 UI 처리..
		 */
		updateAnswerUser : function(userNo) {
            var param = {
                userno : userNo
            };

            cordova.exec(function(result) {
                console.log("cordova.exec() success / updateAnswerUser")
            }, function(result) {
                console.log("updateAnswerUser err : " + JSON.stringify(result));
            }, "PollPlugin", "updateAnswerUser", [param]);


            /*
            var currentAnswerCnt = parseInt($("#answer_cnt").text());
			currentAnswerCnt++;
			$("#answer_cnt").text(currentAnswerCnt);
			$("#stay_user_list div").each(function(){
				var thisUserNo = $(this).attr("attr");
				if (userNo == thisUserNo) {
					$(this).removeClass("wait");
					$(this).addClass("complete");
				}
			});
            */
		}
    },

    Util : {
        getItemCnt : function() {
            var cnt = 0;
            var elem = $("#poll_info_box");
            var inputNodes = $("#item_rows :text");
            inputNodes.each(function(idx) {
                if($(this).val() != "") cnt++;
            });

            return cnt;
        },

        getItemListStr : function() {
            var str;
            var elem = $("#poll_info_box");
            var inputNodes = $("div.multichoice_box", elem).children("input");
            inputNodes.map(function() {
                if(this.value != "") {
                    return this.value;
                }
            }).get().join("|");
        },

        getDateStr : function(dateObj) {
            var curMonth = (dateObj.getMonth() + 1).toString();
            curMonth = curMonth < 10 ? '0' + curMonth : curMonth;

            var curDay = dateObj.getDate().toString();
            curDay = curDay < 10 ? '0' + curDay : curDay;

            var curHour = dateObj.getHours().toString();
            curHour = curHour < 10 ? '0' + curHour : curHour;

            var curMinute = dateObj.getMinutes().toString();
            curMinute = curMinute < 10 ? '0' + curMinute : curMinute;

            var curSecond = dateObj.getSeconds().toString();

            var curDateTxt = dateObj.getFullYear().toString() + curMonth + curDay + curHour + curMinute + curSecond;
            return curDateTxt;
        }
    }

};

$.extend(true, PollCtrl, PollCtrlCordova);
