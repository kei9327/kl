var PollCtrl = {
	
	pollNo : null,	
	type : null,
	title : null,
	timer : null,
	drawingTool : null,
	isProgress : false,   // 폴 진행여부 체크
	progressPoll : null,
	isHost : false,
	isSubmitAnswer : false,   // 답변 제출여부 체크
	
	recvTarget : "",
	
	totalPaticipantCnt : 0,
	
	isPresenter : false,
	
	init : function(pollNo, type, title, allowCnt) {
		this.pollNo = pollNo;
		this.type = type;
		this.title = title;
	},
	
	
	/**
	 * PollCtrl.startDrawingPollAnswer
	 * - 판서 질문을 받고 내 보드로 진입했을 때 호출되는 함수
	 */
	startDrawingPollAnswer : function(pollData) {
		var pollNo;
		var timeLimit;
		var isCountdown;
		var imageInfo;
		
		if (pollData != "" || typeof pollData != "undefined") {
			var pollInfo = JSON.parse(pollData);
			
			pollNo = pollInfo.pollno;
			timeLimit = pollInfo.timelimit;
			isCountdown = pollInfo.iscountdown;
			imageInfo = pollInfo.image;
			
			var seqNo = imageInfo.seqno;
			PollCtrl.questionImgInit(seqNo);
			
			PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, false);
		} else {
			Utils.log("Error !! start drawing poll answer");
		}
		sessionStorage.removeItem("polldata");  // 세션스토리지에 임시로 저장된 폴 데이터 삭제
	},
	
	
	/**
	 * PollCtrl.questionImgInit
	 *  - BGImg.init으로 초기화할 수 없는 질문 이미지에 대해서 이미지 배치를 설정하는 함수
	 */
	questionImgBasePos : [80, 80],
	questionImgInit : function(seqNo) {
		
		var timer = setInterval(function() {
			if (Ctrl.BGImg.data != null) {
				console.log(Ctrl.BGImg.data);
				var imageData = Ctrl.BGImg.data.get(seqNo);
				var board = UI.getBoard();
				var imgCanvas = board.getCanvas("img");

				Ctrl.BGImg.redraw(seqNo, "");

				var initPosition = PollCtrl.questionImgBasePos[0] + (Ctrl.BGImg.newCnt * 20);   // 기본 이미지 배치 포지션은 100 * 100
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
	broadcastPoll : function(type, pollNo, target, timeLimit, isCountDown, userNo, pollType) {
		var mode = "all";	// 전체 수업 
		var roomId = PacketMgr.roomid;
		var packet = {"cmd":"poll","type":""+type+"","pollno":""+pollNo+"","target":""+target+""};
		
		if(typeof timeLimit != 'undefined')
			packet['timelimit'] = timeLimit;
		if(typeof isCountDown != 'undefined')
			packet['iscountdown'] = isCountDown;	
		if(typeof userNo != 'undefined')
			packet['userno'] = userNo;
		if(typeof pollType != 'undefined')
			packet['polltype'] = pollType;
		
		if (type == "report") {
			//packet['totalusercnt'] = Ctrl.Member.classList.length - 1;  // 총 대상자 수를 패킷으로 전송함 (클래스 유저의 인원수)
			packet['totalusercnt'] = PollCtrl.totalParticipantCnt;        // 총 대상자 수를 패킷으로 전송함 (클래스 유저의 인원수)
			packet['presentor'] = PacketMgr.userno;
		}
		Utils.log("[broadcastPoll] packet : " + JSON.stringify(packet));
		
		PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
	},

	
	/**
	 * PollCtrl.receive
	 *  - 폴 패킷을 수신받았을 때 type별로 처리해주는 함수
	 */
	receive : function(packet) {
		var type      = packet.type;
		var pollNo    = packet.pollno;
		var targetUser = typeof packet.target != 'undefined' ? packet.target : "";
		var timeLimit = typeof packet.timelimit != 'undefined' ? packet.timelimit : "";
		var isCountdown = typeof packet.iscountdown != 'undefined' ? packet.iscountdown : "";
		var pollType = typeof packet.polltype != 'undefined' ? packet.polltype : "";
		
		if(type == "start") {
			PollCtrl.isPresenter = false;
			if (pollType == "3") {  // 판서형일 경우..
				console.log("PacketMgr.isParentCreator : " + PacketMgr.isParentCreator);
				console.log("PacketMgr.isCreator : " + PacketMgr.isCreator);
				console.log("PacketMgr.userno : " + PacketMgr.userno);
				console.log("PacketMgr.userid : " + PacketMgr.userid);
				console.log("[PollCtrl.receive] target : " + targetUser);
				if(PacketMgr.userid == PacketMgr.userno)  // 게스트는 판서 폴 답변할 수 없도록 예외처리
					return;
				
				if (targetUser == "teacher") {
					if(!PacketMgr.isParentCreator) {
						PollCtrl.isProgress = true;
						return;
					} else {
						if (!PacketMgr.isCreator) {  // 학생이 다른 보드에서 판서형 질문을 받았을 때는 자신의 방으로 이동시킴
							console.log("학생 보드에 있는 선생님이 질문을 받았습니다.");
							PollCtrl.Action.Attender.moveSubroomForDrawing(pollNo, timeLimit, isCountdown);
							return;
							
						} else {  // 선생님이 선생님 보드에 있을 때 설문을 받은 경우
							console.log("선생님이 선생님 보드에서 질문을 받았습니다.");
							PollCtrl.Action.Attender.readyToAnswerDrawingQuestion(pollNo);
							
							// 하단 답변하기 버튼 노출..
							/**
							PollCtrl.UI.show("btn_call_poll_answer");
							$("#btn_call_poll_answer").click(function(e) {
								e.preventDefault();
								PollCtrl.UI.show("drawing_answer_sheet");
							});
							**/
							
							PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, false);
							return;
						}
					}
				} else {
					if(!PacketMgr.isParentCreator) {
						if (!PacketMgr.isCreator) {  // 선생님방에서 판서형 질문을 받았을 때는 자신의 방으로 이동시킴
							console.log("선생님 보드에 있는 학생이 질문을 받았습니다.");
							
							/*
							PollCtrl.UI.open("alert_poll");
							$("#alert_poll .btn_close").hide();
							$("#alert_poll #btn_confirm").click(function(e) {
								e.preventDefault();
								PollCtrl.Action.Attender.moveSubroomForDrawing(pollNo, timeLimit, isCountdown);
							});*/
							
							PollCtrl.Action.Attender.moveSubroomForDrawing(pollNo, timeLimit, isCountdown);
							
							return;
							
						} else {  // 학생이 학생방에 있을 때 설문을 받은 경우
							console.log("학생이 자신의 보드에서 질문을 받았습니다.");
							PollCtrl.Action.Attender.readyToAnswerDrawingQuestion(pollNo);
							
							/*
							PollCtrl.UI.show("btn_call_poll_answer");
							$("#btn_call_poll_answer").click(function(e) {
								e.preventDefault();
								$("#poll_gudie_pop").hide();
								PollCtrl.UI.close("noti_drawing_poll");
								
								//PollCtrl.UI.show("drawing_answer_sheet");
								PollCtrl.UI.toggleMinMax("drawing_answer_sheet");
							});
							*/
							
							PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, false);
							return;
						}
					}
				}
			} else {
				PollCtrl.UI.show("btn_call_poll_answer_wrap");
				$("#btn_call_poll_answer").click(function(e) {
					e.preventDefault();
					$("#poll_gudie_pop").hide();
					var idStr = pollType == "0" ? "multichoice_answer_sheet" : 
						pollType == "1" ? "alternative_answer_sheet" : 
							pollType == "2" ? "short_answer_sheet" : "";
					//PollCtrl.UI.show(idStr);
					PollCtrl.UI.toggleMinMax(idStr);
				});
				PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown, true);
			}
		} else if(type == "interrupt") {
			if (targetUser == "teacher" && !PacketMgr.isParentCreator) {
				PollCtrl.isProgress = false;
				return;
			}
			PollCtrl.Action.Attender.exitPoll(pollNo);
			sessionStorage.removeItem("polldata");
			
		} else if(type == "report") {
			var totalusercnt = typeof packet.totalusercnt != 'undefined' ? packet.totalusercnt : "";
			var presentor = typeof packet.presentor != 'undefined' ? packet.presentor : "";
			PollCtrl.Action.Common.makePollResult(pollNo, totalusercnt, presentor);
		} else if(type == "answer") {
			var userNo = packet.userno;
			PollCtrl.UI.updateAnswerUser(userNo);
		}
	},
	
	
	destroy : function() {
		$("#dup_choice_allow").unbind("click");
		
		$(".poll_tab > a").unbind("click");
		
		$("#selection_capture_container").unbind("touchstart");
		$("#selection_capture_container").unbind("movestart");
		$("#selection_capture_container").unbind("move");
		$("#selection_capture_container").unbind("moveend");
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
				// 질문 생성 (판서형과 서술형)
				
				/**
				 * PollCtrl.Action.Master.Question.createQuestion
				 *  - 판서형과 서술형 질문을 생성하는 다이얼로그를 띄움.
				 *  @deprecated
				 */
				createQuestion : function (sendFlag) {
					var shutdownTime = $("#question_shutdown_time").val();
					
					var curDate = new Date();
					var curMs   = curDate.getTime();
					var endMs   = curMs + (shutdownTime * 1000);
					var endDate = new Date(endMs);
					
					var pollKey      = RoomSvr.userno;
					var pollTitle    = $("#question_title").val();
					var pollType     = $(":radio[name='question_type']:checked").val();
					var pollCategory = "1";    // 판서형은 pollCategory값이 1
					
					var userKey   = RoomSvr.userno;
					var userId    = RoomSvr.userid;
					var userNm    = RoomSvr.usernm;
					var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
					
					var allowCnt = 1;
					var itemStr  = '';
					// pollType == 2일땐 itemStr을 공백으로 보냄
					
					
					// 예외처리 구간..
					if(!Ctrl._checkAuth(true, false)) return;
					
					if(Ctrl.Member.list.length <= 1) {
						Ctrl.Msg.show(_msg("poll.alert.no.participant"));
						return;
					}
					
					if(pollTitle == '') {     // 폴 내용 예외처리
						Ctrl.Msg.show(_msg("poll.alert.title"));
						return;
					}	
					
					if(isNaN(shutdownTime)) {  // 시간 설정 부분 예외처리
						Ctrl.Msg.show(_msg("poll.alert.timer"));
						return;
					}
					
					var param = {
						pollkey       : pollKey,
						polltype      : pollType,
						pollcategory  : pollCategory,
						title         : encodeURIComponent(pollTitle),
						allowcnt      : allowCnt,
						startdatetime : PollCtrl.Util.getDateStr(curDate),
						enddatetime   : PollCtrl.Util.getDateStr(endDate),
						items         : encodeURIComponent(itemStr),
						userkey       : userKey,
						usernm        : encodeURIComponent(userNm),
						guesttype     : guestType
						//email : userId,
						
					};
					
					Utils.log(param);
					Utils.log(RoomSvr.userid + " / " + RoomSvr.userno + " / " + RoomSvr.usernm);

					$.ajax({
						type : 'post',
						url : Utils.addContext('poll/tmp/add.json'),
						async : true,
						cache : false,
						dataType : 'json',
						data : param,
						beforeSend : function(){},
						success : function(data) {
							Utils.log("[createQuestion] success.." + JSON.stringify(data));

							if(data.result == '0'){
								Ctrl.Msg.show(_msg("poll.alert.add"));
								PollCtrl.UI.close('question_info_box');
								if(data.polltempno != null) {
									var pollTempNo = data.polltempno;
									//PollCtrl.Action.Master.makePollList(1);
									if(sendFlag) {
										console.log(shutdownTime);
										PollCtrl.Action.Master.sendPoll(pollTempNo, shutdownTime);	
									}
								}
							}
						},
						error : function(){ Utils.log("error..") },
						complete : function(){ Utils.log("complete..") } 
					});
				},
				
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
					
					$.ajax({
						type : 'post',
						url : Utils.addContext('poll/draw/share.json'),
						async : true,
						cache : false,
						dataType : 'json',
						data : param,
						beforeSend : function(){},
						success : function(data) {
							Utils.log("success.." + data);

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
			
			/**
			 * PollCtrl.Action.Master.createPoll
			 *  - 폴 템플릿을 신규로 생성할 때 호출하는 함수
			 *  - sendFlag : true - 생성과 동시에 질문 브로드캐스팅, false : 질문 생성만 함
			 */
			createPoll : function(sendFlag) {
				
				var shutdownTime = $("#shutdown_time").val() == "custom" ? 
						parseInt($("#custom_time").val()) : $("#shutdown_time").val();
				
				var curDate = new Date();
				var curMs = curDate.getTime();
				var endMs = curMs + (shutdownTime * 1000);
				var endDate = new Date(endMs);
				
				var pollKey =  RoomSvr.userno;
				var pollTitle =	$("#poll_title").val();
				var pollType = $(".poll_tab > a.on").attr("value");
				var pollCategory = pollType != "3" ? "0" : "1";
				var isDupChoice = $("#dup_choice_allow").hasClass("on");
				
				var userKey   = RoomSvr.userno;
				var userId    = RoomSvr.userid;
				var userNm    = RoomSvr.usernm;
				var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
				
				var allowCnt = isDupChoice == true ? PollCtrl.Util.getItemCnt() : 1;
				var targetUser = $(".question_to .question_to_all").hasClass("on") ? "all" : "teacher";
				PollCtrl.recvTarget = pollType != "3" ? "" : targetUser;
				
				// 선다형, 양자택일만 사용하는 변수. 단답형, 판서형에서는 itemStr을 공백으로 보냄.
				// 항목을 입력하지 않으면 자동으로 인덱스값을 세팅함..
				var itemStr = "";
				var qusBinary = "";
				
				/*
				itemStr = 
					pollType == '0' ? 
							$("#multichoice_box > :text").map(function(idx){
									if(this.value != "")
										return this.value;
									else
										return idx+1;
								}).get().join("|") :
								pollType == '1' ? 
										$("#alternative_box .alternative_direct :text").map(function(idx){
											if(this.value != "")
												return this.value;
											else
												return idx+1;
										}).get().join("|") : '';
				*/
				
				if (pollType == '0') {
					itemStr = $("#multichoice_box > :text").map(function(idx){
						if(this.value != "")
							return this.value;
						else
							return "";
					}).get().join("|");
					
				} else if (pollType == '1') {
					itemStr = $("#alternative_box .alternative_direct :text").map(function(idx){
						if(this.value != "")
							return this.value;
						else
							return "";
					}).get().join("|");
					
				} else if (pollType == '3') {
					if ($("#qusbinary").val() != "") 
						qusBinary = $("#qusbinary").val();
					
//					if ($("#full_capture").hasClass("on")) {
//						var binary = $("#full_capture > span.poll_thumb").css("background-image");
//						qusBinary = binary.split(",")[1]; 
//					} else if ($("#select_capture").hasClass("on")) {
//						var binary = $("#select_capture > span.poll_thumb").css("background-image");
//						qusBinary = binary.split(",")[1];
//					}
				}

				// 예외처리 구간..
				if(!Ctrl._checkAuth(true, false)) return;
				
				//var userNums = Ctrl.Member.list.length;   // 2016.12.20 - deprecated..
				var userNums = Ctrl.Member.classList.length;   // 클래스 유저 수 기준으로..
				
				var targetUserCnt = 0;
				for(var i=0; i<userNums; i++) {
					var userNo = Ctrl.Member.classList[i].userno;
					var userId = Ctrl.Member.classList[i].userid;
					
					if (userNo == PacketMgr.userno) continue;
					if (pollType == '3' && userNo == userId) continue;   // 판서형 질문에서 게스트는 제외해야 하므로 카운팅하지 않음..
					targetUserCnt++;
					
				}
				
				if(sendFlag && targetUserCnt == 0) {
					Ctrl.Msg.show(_msg("poll.alert.no.participant"));
					return;
				}
				
				if(pollTitle == '') {
					Ctrl.Msg.show(_msg("poll.alert.title"));
					$("#poll_title").focus();
					return;
				}
				
				if(pollTitle.bytes() > 256) {
					Ctrl.Msg.show(_msg("poll.draw.letter.lmt"));
					$("#poll_title").focus();
					return;
				}
				
				
				
				// 선다형과 양자택일 질문은 항목 입력완료 여부를 체크함..
				/*
				if(pollType == '0' && pollType == '1'){
					if(itemStr == '') {
						Ctrl.Msg.show(_msg("poll.alert.answer"));
						return;
					}
				}*/
				
				// 선다형
				if (pollType == '0') {
					var checkResult = true;
					$("#multichoice_box > :text").each(function(){
						if(this.value == "") {
							checkResult = false;
							$(this).focus();
							return false;
						}
					});
					
					if (!checkResult) {
						Ctrl.Msg.show(_msg("poll.answer.input"));
						return;
					}
					
				}
				
				// 양자택일
				if (pollType == '1') {
					var checkResult = true;
					$("#alternative_box .alternative_direct :text").each(function(){
						if(this.value == "") {
							checkResult = false;
							$(this).focus();
							return false;
						}
					});
					
					if (!checkResult) {
						Ctrl.Msg.show(_msg("poll.answer.input"));
						return;
					}
					
					// 양자택일 유형 선택 여부 예외처리..
					var selectAlterOpt = $("#alternative_box_sel option:selected").val();
					if(selectAlterOpt == "") {
						Ctrl.Msg.show(_msg("poll.alert.answer"));
						return;
					}
					
				}
				
				// 판서형
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
				
				// 종료시간을 직접 입력했을 경우.. 입력한 시간에 대한 예외처리..
				if($("#custom_time").val() != "") {
					if (!$("#custom_time").val().isNum()) {
						Ctrl.Msg.show(_msg("poll.alert.number.check"));
						return;
					}
					
					if($("#custom_time").val() < 30 || $("#custom_time").val() > 600) {
						Ctrl.Msg.show(_msg("poll.time.from.to"));
						return;
					}
				}
					
				
				var param = {
					pollkey       : pollKey,
					polltype      : pollType,
					pollcategory  : pollCategory,
					title         : encodeURIComponent(pollTitle),
					allowcnt      : allowCnt,
					startdatetime : PollCtrl.Util.getDateStr(curDate),
					enddatetime   : PollCtrl.Util.getDateStr(endDate),
					items         : encodeURIComponent(itemStr),
					userkey       : userKey,
					usernm        : encodeURIComponent(userNm),
					guesttype     : guestType,
					qusbinary     : qusBinary
					//email : userId,
				};
				
				Utils.log("[createPoll] param.polltype: " + param.polltype + " / param.pollkey: " + param.pollkey + " / param.pollcategory: " + param.pollcategory + " / param.items: " + param.items + " / param.guesttype: " + param.guesttype + " / param.userkey: " + param.userkey+ " / param.usernm: " + param.usernm);
				//Utils.log("[createPoll] param : " + JSON.stringify(param));
				//Utils.log(RoomSvr.userid + " / " + RoomSvr.userno + " / " + RoomSvr.usernm);

				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/tmp/add.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[createPoll] poll/tmp/add.json success ----- data : " + JSON.stringify(data));

						if (data.result == '0') {
							Ctrl.Msg.show(_msg("poll.alert.add"));
							PollCtrl.UI.close('poll_info_box');
							if(data.polltempno != null) {
								var pollTempNo = data.polltempno;
								if(sendFlag) {
									Utils.log("[createPoll] shutdownTime : " + shutdownTime);
									PollCtrl.Action.Master.sendPoll(pollTempNo, shutdownTime, targetUser);
								} else {
									PollCtrl.Action.Master.makePollList(1);
								}
							}
						} else {
							
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
				
			},
			
			/*
			 폴 템플릿 정보 업데이트
			 @param
			 	- pollTempNo : 폴 템플릿 넘버 (UUID)
			 	- sendFlag   : 업데이트 후 바로 전송 여부 (true / false)
			 */
			updatePoll : function(pollTempNo, sendFlag) {
				//var shutdownTime = $("#shutdown_time").val() * 1000;  // 밀리세컨드 단위
				var shutdownTime = $("#shutdown_time").val() == "custom" ? 
						parseInt($("#custom_time").val()) : parseInt($("#shutdown_time").val());
				
				var curDate = new Date();
				var curMs = curDate.getTime();
				var endMs = curMs + shutdownTime;
				var endDate = new Date(endMs);
				
				var pollKey = RoomSvr.userno;   // Freeboard는 poll key값이 userno..
				var pollTitle =	$("#poll_title").val();
				var pollType = $(".poll_tab > a.on").attr("value");
				var pollCategory = pollType != "3" ? "0" : "1";
				var isDupChoice = $("#dup_choice_allow").hasClass("on");
				
				var userKey   = RoomSvr.userno;
				var userId    = RoomSvr.userid;
				var userNm    = RoomSvr.usernm;
				var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
				
				var allowCnt = isDupChoice == true ? PollCtrl.Util.getItemCnt() : 1;
				
//				var itemStr = 
//					pollType == '0' ? $("#multichoice_box :text").map(function(){if(this.value != "") return this.value;}).get().join("|") : 
//						pollType == '1' ? $("#alternative_box :text").map(function(){if(this.value != "") return this.value;}).get().join("|") : 
//							pollType == '2' ? $("#short_box textarea").val() : '';
				

				var itemStr = 
					pollType == '0' ? $("#multichoice_box > :text").map(
								function(idx){
									if (this.value != "") return this.value;
									else return idx + 1;
								}).get().join("|") :
							pollType == '1' ? $("#alternative_box .alternative_direct :text").map(
									function(idx) {
										if(this.value != "") return this.value;
										else return idx+1;
									}).get().join("|") : '';

							
				// 예외처리 구간..	
				if(!Ctrl._checkAuth(true, false)) return;
				
				if(pollTempNo == '' || pollTempNo == undefined) {
					Ctrl.Msg.show(_msg("poll.alert.error"));
					return;
				}
				if(pollType == '1'){
					if(itemStr == '') {
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
					userkey       : userKey,
					usernm        : userNm,
					guesttype     : guestType
					//email : userId,
				};
				
				Utils.log(param);
				Utils.log(RoomSvr.userid + " / " + RoomSvr.userno + " / " + RoomSvr.usernm);

				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/tmp/update.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[updatePoll] poll/tmp/update.json success ----- data :  " + JSON.stringify(data));

						if(data.result == '0') {
							Ctrl.Msg.show(_msg("poll.alert.update"));
							if(sendFlag) {
								PollCtrl.Action.Master.sendPoll(pollTempNo, shutdownTime);
							} else {
								PollCtrl.UI.close("poll_info_box");
								PollCtrl.Action.Master.makePollList(1);
							}
							
							
						} else {
							Utils.log(data);
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
			},
			
			
			/**
			 * PollCtrl.Action.Master.deletePoll : 폴 템플릿 데이터를 삭제해주는 함수
			 */
			deletePoll : function(pollTempNo) {
				
				// 예외처리 구간..
				if(!Ctrl._checkAuth(true, false)) return;
				
				var param = { polltempno : pollTempNo };
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/tmp/remove.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[deletePoll] poll/tmp/remove.json success ----- data : " + JSON.stringify(data));
						if(data.result == '0') {
							Ctrl.Msg.show(_msg("poll.alert.tmp_del.success"));
							PollCtrl.UI.close("poll_info_box");
							PollCtrl.Action.Master.makePollList(1);
						} else {
							Ctrl.Msg.show(_msg("poll.alert.tmp_del.fail"));
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
				
			},
			
			
			/**
			 * PollCtrl.Action.Master.deleteCompletedPoll : 완료된 폴 내역을 삭제할 때 호출하는 함수
			 * 
			 */
			deleteCompletedPoll : function() {
				
				// 예외처리 구간..
				if(!Ctrl._checkAuth(true, false)) return;
				
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
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/remove.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[deleteCompletedPoll] poll/remove.json success ----- data : " + JSON.stringify(data));
						if (data.result == '0') {
							Ctrl.Msg.show(_msg("poll.alert.delete.success"));
							PollCtrl.UI.close("poll_complete_list_box");
							PollCtrl.Action.Master.makePollCompleteList(1);   // 삭제가 완료되면 리스트의 첫번째 페이지 화면으로 전환시켜 줌..
						} else {
							Ctrl.Msg.show(_msg("poll.alert.delete.fail"));
							Utils.log(data);
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
				
			},
			
			
			/**
			 * PollCtrl.Action.Master.sendPoll : 질문을 참여자들에게 보내기 (수업 내 모든 보드로 브로드캐스팅하는 방식)
			 *	- pollTempNo : 폴 템플릿 넘버 (UUID)
			 *	- shutdown : 제한 시간 (단위 : 초)
			 */
			sendPoll : function(pollTempNo, shutdown, targetUser) {
				
				if(!Ctrl._checkAuth(true, false)) return;
				
				//var userNums = Ctrl.Member.list.length;
				var userNums = Ctrl.Member.classList.length;   // 클래스 유저 수 기준으로..
				
				var pollType = $(".poll_tab > a.on").attr("value");
				var targetUserCnt = 0;
				for(var i=0; i<userNums; i++) {
					var userNo = Ctrl.Member.classList[i].userno;
					var userId = Ctrl.Member.classList[i].userid;
					
					if (userNo == PacketMgr.userno) continue;
					if (pollType == '3' && userNo == userId) continue;   // 판서형 질문에서 게스트는 제외해야 하므로 카운팅하지 않음..
					targetUserCnt++;
					
				}
				
				// 참여자 인원 수 체크
				if(targetUserCnt == 0) {
					Ctrl.Msg.show(_msg("poll.alert.no.participant"));
					return;
				}
				
				PollCtrl.totalParticipantCnt = targetUserCnt;   // 총 대상자 인원 수 값 저장..
				
				$(".poll_count").show();
				PollCtrl.UI.close("poll_list_box");
				PollCtrl.UI.close("question_info_box");
				
				var param = { polltempno : pollTempNo };
				var paramForCopy;
				var isCountdown = true;
				
				$.ajax({
					type       : 'post',
					url        : Utils.addContext('poll/tmp/get.json'),
					async      : true,
					cache      : false,
					dataType   : 'json',
					data       : param,
					beforeSend : function(){},
					success    : function(data) {
						Utils.log("[sendPoll] poll/tmp.get.json success ----- data : " + JSON.stringify(data));
						if(data.result == '0') {
							var resultMap = data.map;
							console.log(resultMap);
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
							var pollTitle = resultMap.title;
							
							$.ajax({
								type       : 'post',
								url        : Utils.addContext('poll/tmp/copy.json'),
								async      : true,
								cache      : false,
								dataType   : 'json',
								data       : paramForCopy,
								beforeSend : function(){},
								success    : function(data) {
									Utils.log("[sendPoll] poll/tmp/copy.json success ----- data : " + JSON.stringify(data));
									if(data.result == '0') {
										Ctrl.Msg.show(_msg("poll.alert.send.success"));
										var newPollNo = data.pollno;
										
										PollCtrl.isPresenter = true;
										PollCtrl.isProgress   = true;
										PollCtrl.progressPoll = newPollNo;
										
										if(shutdown == undefined) shutdown = 30; // 시간 지정 안된 경우는 디폴트 30초로
										if(shutdown == 0) isCountdown = false;   // 시간 제한 없을 경우..
										
										
										var shutdownMs = shutdown * 1000;
										var displayTime  = shutdown == 0 ? "" : shutdown;
										
										console.log("[sendPoll] shutdownMs(ms) = " + shutdownMs);
										console.log("[sendPoll] displayTime(sec) = " + displayTime);
										console.log("[sendPoll] isCountdown  = " + isCountdown);
										
										// 시간제한은 지정안할시에 기본 30초
//										if(shutdownTime == "" || isNaN(shutdownTime))
//											shutdownTime = 30;
										
										
										PollCtrl.UI.open("poll_timer_box");  // 폴 답변 대기 화면을 띄움..
										PollCtrl.UI.show("btn_wait_poll_answer");  // 하단 답변 대기중 버튼을 띄움..
										$("#btn_wait_poll_answer").click(function(e) {
											e.preventDefault();
											//PollCtrl.UI.show("poll_timer_box");
											PollCtrl.UI.toggleMinMax("poll_timer_box");
										});
										
										
										$("#answer_cnt").text("0");
										$("#answer_tot_cnt").text(targetUserCnt);

										PollCtrl.UI.renderAnswerUserList(paramForCopy.polltype);  // 폴 참여자리스트를 뿌려줌..
										
										$("#poll_timer_box .question_text").text(pollTitle);   // 폴 타이틀 값 출력
										
										$("#timer").text(displayTime);
										
										$("#poll_stop_btn").prop("href", "javascript:PollCtrl.Action.Master.stopPoll('" + newPollNo + "')");
										//$("#poll_timer_box .poll_header a").prop("href", "javascript:PollCtrl.UI.hide('poll_timer_box')");
										$("#poll_timer_box a#btn_exit_poll").prop("href", "javascript:PollCtrl.Action.Master.stopPoll('" + newPollNo + "')");
										
										// 스톱워치 Progress UI 설정
										if (isCountdown) {
											$("#poll_timer_box .poll_count").hasClass("time_infinite") ? $("#poll_timer_box .poll_count").removeClass("time_infinite") : $("#poll_timer_box .poll_count");
											
											if ($("#poll_timer_box .poll_count").text() == "") {
												$("#timer").after(_msg("poll.timer.second") + "초");
											}
											
											PollCtrl.timer = setInterval(function() {
												$("#timer").text(displayTime--);
												if (displayTime < 0) {
													//PollCtrl.UI.close("poll_timer_box");
													PollCtrl.broadcastPoll("interrupt", newPollNo, PollCtrl.recvTarget);   // 진행자 기준으로 종료시점 맞추기..
													PollCtrl.Action.Master.finishPoll(newPollNo);
										        }
											}, 1000);
											//PollCtrl.UI.animateStopWathSmooth(shutdownMs);
											PollCtrl.UI.animateProgress(shutdownMs);
											
										} else {
											$("#poll_timer_box .poll_count").addClass("time_infinite");
											$("#poll_timer_box .poll_count").contents().filter(function(){
												return this.nodeType === 3;
											}).remove();
											
											$("#poll_progress").css("width", "0%");
										}
										
										//PacketMgr.Master.poll('start', newPollNo, shutdownMs, isCountdown);
										
										// 2016.12.20 - 문제 출제는 수업 내 모든 보드로 브로드캐스트 하는 방식으로 변경됨
										PollCtrl.broadcastPoll("start", newPollNo, typeof targetUser == 'undefined' ? "" : targetUser, shutdownMs, isCountdown, "", paramForCopy.polltype);   
										
									} else {
										Ctrl.Msg.show(_msg("poll.alert.send.fail"));
									}
								},
								error      : function(){ Utils.log("error..") },
								complete   : function(){} 
							});
						}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){} 
				});
				
			},
			
			
			/**
			 * PollCtrl.Action.Master.sendPollFromList
			 *  - 폴 템플릿 리스트에서 폴을 전송할 때 호출하는 함수
			 */
			sendPollFromList : function() {
				var selectedPollNo = $(":radio[name='poll_item']:checked").val();
				if(selectedPollNo == '' || selectedPollNo == undefined){
					Ctrl.Msg.show(_msg("poll.alert.check"));
					return;
				}
				var defaultTime = 30;  // 리스트에서 바로 Send할때는 기본 제한시간을 적용함 (30초)
				
				PollCtrl.UI.close("poll_list_box");
				PollCtrl.Action.Master.sendPoll(selectedPollNo, defaultTime);
			},
			
			
			/**
			 * PollCtrl.Action.Master.sendPollReport
			 *  - 폴 결과화면 보로드캐스팅
			 */
			sendPollReport : function(pollNo) {
				//PacketMgr.Master.poll('report', pollNo, '', '');
				PollCtrl.broadcastPoll("report", pollNo, "");
			},
			
			
			/**
			 * PollCtrl.Action.Master.finishPoll
			 *  - 폴이 종료되었을 때, UI 처리
			 */
			finishPoll : function(pollNo) {
				PollCtrl.isProgress = false;
				PollCtrl.progressPoll = null;
				PollCtrl.recvTarget = "";
				
				PollCtrl.UI.animateProgress();  // 애니메이션 종료
				
				Ctrl.Msg.show(_msg("poll.alert.finish"));
				clearInterval(PollCtrl.timer);
				$("#poll_progress").css("width", "0");
				
				// 열려있는 창 닫기
				PollCtrl.UI.close("poll_timer_box");
				PollCtrl.UI.close("alert_poll");
				PollCtrl.UI.hide("btn_wait_poll_answer");
				
				// 폴 결과 창 열기
				PollCtrl.Action.Common.makePollResult(pollNo, Ctrl.Member.classList.length - 1, PacketMgr.userno);
				
			},
			
			
			/**
			 * PollCtrl.ACtion.Master.stopPoll
			 *  - 폴 진행자가 폴을 종료시킬 때 호출하는 함수
			 */
			stopPoll : function(pollNo) {   // 진행자 파트
				Ctrl.Modal.confirm(_msg("poll.confirm.stop"), function() {
					//PacketMgr.Master.poll('interrupt', pollNo, '', '');
					PollCtrl.broadcastPoll("interrupt", pollNo, PollCtrl.recvTarget);
					PollCtrl.Action.Master.finishPoll(pollNo);
				});
			},
			
			
			/**
			 * PollCtrl.Action.Master.showPollResult
			 *  - 질문 결과 상세보기 다이얼로그
			 */
			showPollResult : function(pollNo) {
				PollCtrl.UI.close('poll_complete_list_box');
				//PollCtrl.UI.open('poll_result_box_admin');
				PollCtrl.Action.Common.makePollResult(pollNo);
			},
			
			
			/**
			 * 폴 템플릿 상세보기 화면 열기..
			 */
			makePollUpdateFrm : function(pollTempNo) {
				var param = { polltempno : pollTempNo };
				
				$.ajax({
					type       : 'post',
					url        : Utils.addContext('poll/tmp/get.json'),
					async      : true,
					cache      : false,
					dataType   : 'json',
					data       : param,
					beforeSend : function(){},
					success    : function(data) {
						Utils.log("[makePollUpdateFrm] poll/tmp/get.json success ----- data : " + JSON.stringify(data));
						if(data.result == '0') {
							var resultMap = data.map;
							PollCtrl.UI.close("poll_list_box");
							PollCtrl.UI.open("poll_info_box");
							PollCtrl.UI.renderPollUpdateFrm(resultMap);
						}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){ Utils.log("complete..") } 
				});
			},
			
			// 폴 리스트 뷰
			makePollList : function(pageNum) {
				if(!Ctrl._checkAuth(true, false)) return;
				
				PollCtrl.UI.open("poll_list_box");
				
				var pollKey = RoomSvr.userno;
				var param = {
					pollkey   : pollKey,
					rows : PollCtrl.UI.ROWS_PER_PAGE,
					pageno : pageNum
				};
				
				$.ajax({
					type       : 'post',
					url        : Utils.addContext('poll/tmp/list.json'),
					async      : true,
					cache      : false,
					dataType   : 'json',
					data       : param,
					beforeSend : function(){},
					success    : function(data) {
						Utils.log("[makePollList] poll/tmp/list.json success ----- data : " + JSON.stringify(data));
						if (data.result == '0') {
							PollCtrl.UI.renderPollList(data, pageNum);
						}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){} 
				});
			},
			
			
			// 완료된 폴 리스트 뷰
			// PollCtrl.Action.Master.makePollCompleteList(pageNum);
			makePollCompleteList : function(pageNum) {
				if (!Ctrl._checkAuth(true, false))
					return;
				
				PollCtrl.UI.open("poll_complete_list_box");
				
				var pollKey = RoomSvr.roomid;
				var param = {
					pollkey   : pollKey,
					rows : PollCtrl.UI.ROWS_PER_PAGE,
					pageno : pageNum
				};
				$.ajax({
					type       : 'post',
					url        : Utils.addContext('poll/list.json'),
					async      : true,
					cache      : false,
					dataType   : 'json',
					data       : param,
					beforeSend : function(){},
					success    : function(data) {
						Utils.log("[makePollCompleteList] poll/list.json success / data : " + JSON.stringify(data));
						Utils.log(data);
						//if(data.result == '0') {
							PollCtrl.UI.renderCompletedPollList(data, pageNum);
						//}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){ Utils.log("complete..") } 
				});
			},
			
			// 방 종료했을 때 폴관련 정보를 삭제함
			// PollCtrl.Action.Master.removeRoomPoll(pollKey);
			removeRoomPoll : function(pollKey) {
				var param = { pollkey : pollKey };
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/all/remove.json'),
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
					complete   : function(){} 
				});
			}
			
		},
		Attender : {
			
			/**
			 * PollCtrl.Action.Attender.makePollSheet
			 *  - 질문에 대한 답변을 입력할 수 있는 다이얼로그를 생성함.
			 */
			makePollSheet : function(pollNo, timeLimit, isCountdown, isShow) {
				Utils.log("[makePollSheet] timeLimit = " + timeLimit);
				Utils.log("[makePollSheet] isCountdown = " + isCountdown + " " + typeof isCountdown);
				
				$("#quickLink .gotoRoom").bind("click", function(evt) {
					evt.preventDefault();
					if (PollCtrl.isProgress) {
						Ctrl.Msg.show(_msg("poll.draw.cant.mine.ansr"));
						return;
					}
				});
				
				var param = { pollno : pollNo };
				//$(".poll_user_count", "#hand_writing_sheet").show();
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/get.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[makePollSheet] poll/get.json success / data : " + JSON.stringify(data));
						
						var pollStart = data.map.startdatetime;
						var pollEnd = data.map.enddatetime;
						if(data.result == '0') {
							
							PollCtrl.isProgress = true;
							PollCtrl.progressPoll = pollNo;
							
							var resultMap = data.map;
							var pollType = resultMap.polltype;
							var pollCategory = resultMap.pollcategory;
							var userTimeLimit = timeLimit + 1;  // 1초의 딜레이를 추가함
							
							PollCtrl.UI.renderPollSheet(resultMap, isShow);
							
							var containerIdStr;
							if (pollType == '0') {
								containerIdStr = "multi_choice_sheet";
							} else if (pollType == '1') {
								containerIdStr = "alternative_sheet";
							} else if (pollType == '2') {
								if (pollCategory == '0') {
									containerIdStr = "short_answer_sheet";
								} else {
									containerIdStr = "text_answer_sheet";
								}
							} else if (pollType == '3') {
								containerIdStr = "drawing_answer_sheet";
							}
							
							PollCtrl.UI.show("btn_call_poll_answer_wrap");
							$("#btn_call_poll_answer").unbind("click");
							$("#btn_call_poll_answer").click(function(e) {
								e.preventDefault();
								$("#poll_gudie_pop").hide();
								PollCtrl.UI.close("noti_drawing_poll");
								
								//PollCtrl.UI.show(containerIdStr);
								PollCtrl.UI.toggleMinMax(containerIdStr);
							});
							
							if(isCountdown == true || isCountdown == "true") {
								Utils.log("[makePollResult] 시간 제한이 있는 질문입니다.");
								
								$($("#" + containerIdStr + " .poll_user_count")[0]).show();
								$($("#" + containerIdStr + " .poll_user_count")[1]).hide();
								
								timeLimit = timeLimit / 1000;
								$("#" + containerIdStr + " .poll_user_count > span").text(timeLimit);
								PollCtrl.timer = setInterval(function() {
									$("#" + containerIdStr + " .poll_user_count > span").text(timeLimit);
									timeLimit--;
									if (timeLimit < 0) {   // 질문 제한시간이 다 되었을 때,
										if (pollType == '0') {
											PollCtrl.UI.close("multi_choice_sheet"); // 선다형 답변 다이얼로그를 닫음.
										} else if (pollType == '1') {
											PollCtrl.UI.close("alternative_sheet");  // 양자택일형 답변 다이얼로그를 닫음.
										} else if (pollType == '2') {
											if (pollCategory == '1') {
												PollCtrl.UI.close("text_answer_sheet"); // 서술형 답변 다이얼로그를 닫음.
											} else {
												PollCtrl.UI.close("short_answer_sheet"); // 단답형 답변 다이얼로그를 닫음.
											}
										} else if (pollType == '3') {
											//PollCtrl.UI.close("hand_writing_sheet");   // 2015.12.20 - deprecated..
											PollCtrl.UI.close("drawing_answer_sheet"); // 판서형 답변 다이얼로그를 닫음.
										}
										
										Ctrl.Msg.show(_msg("poll.alert.finish"));
										PollCtrl.Action.Attender.exitPoll(pollNo);
										
										//PollCtrl.Action.Master.finishPoll(pollNo);
							        }
								}, 1000);
							} else {
								//$(".poll_user_count", "#hand_writing_sheet").hide();
								
								$($("#" + containerIdStr + " .poll_user_count")[0]).hide();
								$($("#" + containerIdStr + " .poll_user_count")[1]).show();
							}
							
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
			},
			
			// 설문 시트 결과 전송
			submitPollResult : function(pollType) {
				
				if (PollCtrl.isSubmitAnswer) {
					Ctrl.Msg.show(_msg("poll.draw.submit.twice"));
					return;
				}
				
				PollCtrl.isSubmitAnswer = true;
				var pollNo  = $("#sheetPollNo").val();
				var userKey = RoomSvr.userno;
				var userId  = RoomSvr.userid;
				var userNm  = RoomSvr.usernm;
				var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
				
				var pollItemNo = "";
				var answerTxt = "";
				var answerBin = "";

				if(pollType == '2') {           // 단답형, 서술형
					answerTxt = $("#short_answer_sheet .answer_short").val();
					if(answerTxt == "" || typeof answerTxt == "undefined") 
						answerTxt = $("#text_answer_sheet .answer_text").val();
				} else if (pollType == '3') {   // 판서형..
					
					/*
					// 이전 버전의 판서형 질문 방식 - deprecated..
					var canvasEl      = $("#answer_canvas").children().get(0);
					var shapeCanvasEl = $("#answer_canvas").children().get(1);
					
					var ctx = $(canvasEl)[0].getContext("2d");
					var shapeCtx = $(shapeCanvasEl)[0].getContext("2d");
					
					shapeCtx.fillStyle = "rgb(245, 245, 245)";
					shapeCtx.fillRect(0, 0, $("#answer_canvas").width(), $("#answer_canvas").height());
					shapeCtx.strokeStyle = "#EBEBEB";
					shapeCtx.lineWidth = 5;
					shapeCtx.strokeRect(0, 0, $("#answer_canvas").width(), $("#answer_canvas").height());
					
					shapeCtx.drawImage(canvasEl, 0, 0);
					
					try {
						var canvasBinary = shapeCanvasEl.toDataURL();
						answerBin = canvasBinary.split(',')[1];
					} catch(e) {
						Ctrl.Msg.show(_msg("poll.alert.upload.fail"));
						return;
					}
					*/
					
					if ($("#answer_full_capture").hasClass("on")) {
						var binary = $("#answer_full_capture > span.poll_thumb").css("background-image");
						answerBin = binary.split(",")[1]; 
					} else if ($("#answer_select_capture").hasClass("on")) {
						var binary = $("#answer_select_capture > span.poll_thumb").css("background-image");
						answerBin = binary.split(",")[1];
					}
					
					
				} else {
					pollItemNo = $("input[name='poll_choice_item']:checked").map(function() {
						if(this.value != "") {
							return this.value; 
						}
					}).get().join("|");
				}
				
				if(pollType != '3'){
					if(pollItemNo == "" && answerTxt == "") {
						Ctrl.Msg.show(_msg("poll.alert.empty.answer"));
						return;
					}
				}
				
				console.log("answerTxt : " + answerTxt + " (" + answerTxt.bytes() + ")");
				
				if(pollType == '2') {
					if(answerTxt == "" || typeof answerTxt == "undefined") {
						Ctrl.Msg.show(_msg("poll.alert.empty.answer"));
						return;
					}
					
					if(answerTxt.bytes() > 256) {
						Ctrl.Msg.show(_msg("poll.draw.letter.lmt"));
						return;
					}
				}
				
				$(".poll_user_count span").text("");
				
				var param = {
					pollno : pollNo,
					polltype : pollType,
					userkey : userKey,
					//svctype : svcType,
					usernm : encodeURIComponent(userNm),
					//email : email,
					//telno : telNo,
					guesttype : guestType,
					pollitemno : pollItemNo,
					anstxt : encodeURIComponent(answerTxt),
					ansbinary : answerBin
				};
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/answer/add.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[submitPollResult] poll/answer/add.json success ----- data : " + JSON.stringify(data));
						if(data.result == '0') {
							
							/*
							if(PollCtrl.drawingTool != null) {
								PollCtrl.drawingTool["destroy"]();
								PollCtrl.drawingTool = null;
								PollCtrl.Event.unbindClickEvent();
							}
							PollCtrl.UI.close("hand_writing_sheet");
							*/
							
							Ctrl.Msg.show(_msg("poll.alert.submit"));
							PollCtrl.UI.close("multi_choice_sheet");
							PollCtrl.UI.close("alternative_sheet");
							PollCtrl.UI.close("short_answer_sheet");
							
							PollCtrl.UI.close("drawing_answer_sheet");
							PollCtrl.UI.close("text_answer_sheet");
							
							PollCtrl.broadcastPoll("answer", pollNo, "", "", "", userKey);   // 답변 완료시에 답변했다고 패킷을 보냄..
							
							
							PollCtrl.UI.hide("btn_call_poll_answer_wrap");
							PollCtrl.isSubmitAnswer = false;
							
							if (pollType == "3" && PollCtrl.recvTarget == "") {
								PollCtrl.UI.open("alert_poll");
								
								var url = $("#quickLink.go_teacher a").attr("href");
								$("#alert_poll #btn_confirm").attr("href", url);
								$("#alert_poll .btn_cancel").click(function(e) {
									e.preventDefault();
									PollCtrl.UI.close("alert_poll");
								});
							}
							
							return;
						} else if(data.result == '-102') {
							PollCtrl.isSubmitAnswer = false;
							Ctrl.Msg.show(_msg("poll.alert.already.submit"));
							return;
						} else if(data.result == '-201') {
							PollCtrl.isSubmitAnswer = false;
							Ctrl.Msg.show(_msg("poll.alert.upload.fail"));
							return;
						} else {
							PollCtrl.isSubmitAnswer = false;
							Ctrl.Msg.show(_msg("poll.alert.answer.fail"));
							return;
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
			},
			
			/**
			 * PollCtrl.Action.Attender.exitPoll
			 *  - 질문이 종료되었을 때 참여자 쪽의 UI 처리 함수
			 */
			exitPoll : function(pollNo) {   // 참여자 파트
				
				$("#quickLink .gotoRoom").unbind("click");
				
				PollCtrl.isProgress = false;
				PollCtrl.progressPoll = null;
				
				if(PollCtrl.drawingTool != null) {
					PollCtrl.drawingTool["destroy"]();
					PollCtrl.drawingTool = null;
					PollCtrl.Event.unbindClickEvent();
				}
				PollCtrl.UI.close("multi_choice_sheet");
				PollCtrl.UI.close("alternative_sheet");
				PollCtrl.UI.close("short_answer_sheet");
				//PollCtrl.UI.close("hand_writing_sheet");
				PollCtrl.UI.close("drawing_answer_sheet");
				PollCtrl.UI.close("text_answer_sheet");
				
				PollCtrl.UI.hide("btn_call_poll_answer_wrap");
				
				//PollCtrl.UI.close("alert_poll");
				
				// 임시 폴 데이터 저장 목적의 필드를 삭제함
				var pollDataEl = document.getElementById("polldata");
				if (pollDataEl) {
					$(pollDataEl).val("");
					$(pollDataEl).remove();
				}
				
				Ctrl.Msg.show(_msg("poll.alert.stop"));
				clearInterval(PollCtrl.timer);
				
				PollCtrl.UI.close("noti_drawing_poll");
				$("#noti_drawing_poll #noti_poll_title").text("");
				$("#noti_drawing_poll .poll_alert_image").css("background-image", "");
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
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/subroom/move.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[moveSubroomForDrawing] success ----- data : " + JSON.stringify(data));
						if (data.result == '0') {
							
							if(PacketMgr.code == data.code) {
                                return;
                            }
							
							var imageMap = data.image;
							var params = {
								pollno : pollNo,
								timelimit : timeLimit,
								iscountdown : isCountdown,
								image : imageMap
							};
							
							sessionStorage.setItem("polldata", JSON.stringify(params));
							
							PollCtrl.UI.open("noti_drawing_poll");
							$("#noti_drawing_poll .poll_alert_text").text(data.title);
							$("#noti_drawing_poll .poll_alert_image").css("background-image", "url(" + data.fileurl + ")");
							$("#noti_drawing_poll #btn_move").text(_msg("move.myroom.btn"));
							$("#noti_drawing_poll #btn_move").click(function(e) {
								e.preventDefault();
								location.href = Utils.addContext("room/") + parseInt(data.code);
							});
							return;
						} else {
							Ctrl.Msg.show(_msg("poll.alert.answer.fail"));
							return;
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
			},
			
			readyToAnswerDrawingQuestion : function(pollNo) {
				var param = {
					pollno : pollNo,
					roomid : RoomSvr.roomid,
					deviceid : RoomSvr.deviceid
				};
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/subroom/file/add.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[readyToAnswerDrawingQuestion] poll/subroom/file/add.json success ----- data : " + JSON.stringify(data));
						if (data.result == '0') {
							var imageMap = data.map;
                            Ctrl.BGImg.init(imageMap);
                            
                            PollCtrl.UI.open("noti_drawing_poll");
                            $("#noti_drawing_poll .poll_alert_text").text(imageMap.filedata);
							$("#noti_drawing_poll .poll_alert_image").css("background-image", "url(" + imageMap.dnloadurl + ")");
							$("#noti_drawing_poll #btn_move").text(_msg("poll.draw.sheet.title"));
							$("#noti_drawing_poll #btn_move").unbind("click");
							$("#noti_drawing_poll #btn_move").click(function(e) {
								e.preventDefault();
								PollCtrl.UI.close("noti_drawing_poll");
							});
                            
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

			
			/**
			 * PollCtrl.Action.Common.makePollResult
			 */
			makePollResult : function(pollNo, totalUserCnt, presentor) {
				//var isMaster = PacketMgr.isParentCreator;
				
				//var pollNo = '334662b6596ke67a146a';
				
				var param = { pollno : pollNo };
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/get.json'),
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("[makePollResult] poll/get.json success ------ data : " + JSON.stringify(data));
						if(data.result == '0') {
							var resultMap = data.map;
							var pollType = resultMap.polltype;
							if(pollType == '3') {
								// TODO 판서형 폴 결과값 렌더링
								$.ajax({
									type : 'post',
									url  : Utils.addContext('poll/answer/list.json'),
									async : true,
									cache : false,
									dataType : 'json',
									data : { pollno : pollNo },
									success : function(data) {
										console.log("[makePollResult] poll/answer/list.json success / data : " + JSON.stringify(data));
										if(data.result == 0) {
											//var answerTotalCnt = parseInt(resultMap.answertotalcnt);
											var answerTotalCnt = totalUserCnt;
											var answerUserCnt  = parseInt(resultMap.answerusercnt);
											var answerList = data.list;
											
											//PollCtrl.UI.renderHandWritingResult(pollNo, answerTotalCnt, answerUserCnt, answerList, isMaster);
											PollCtrl.UI.renderDrawingPollResult(pollNo, resultMap.title, answerTotalCnt, answerUserCnt, answerList, PacketMgr.isParentCreator);
											
										} else if(data.result == -103) {
											var answerTotalCnt = totalUserCnt;
											var answerUserCnt  = 0;
											var answerList = [];
											
											//PollCtrl.UI.renderHandWritingResult(pollNo, answerTotalCnt, answerUserCnt, answerList, isMaster);
											PollCtrl.UI.renderDrawingPollResult(pollNo, resultMap.title, answerTotalCnt, answerUserCnt, answerList, PacketMgr.isParentCreator);
											//Ctrl.Msg.show(_msg("poll.alert.no.answer"));
										}
									}
								});
							} else {
								PollCtrl.UI.renderPollGraph(resultMap, PacketMgr.isMC, totalUserCnt);
							}
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){} 
				});
			},
			
			
			/**
			 *  - 유저별 판서 답안 이미지를 출력하는 함수
			 *  @deprecated
			 */
			getAnswerFile : function(pollNo, pollUserNo, el) {
				var param = {
					pollno : pollNo,
					polluserno : pollUserNo
				};
				
				$.ajax({
					type : 'post',
					url : Utils.addContext('poll/file/item/get.json'),
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
							PollCtrl.UI.renderUserPollFile(fileNo, filePath, pollUserNo, el);
							
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){ Utils.log("complete..") } 
				});
			}
		}
	},
	
	Event : {
		
		/**
		 * 구 버전 판서 폴 관련 이벤트 초기화 함수
		 * @deprecated
		 */
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
				PollCtrl.UI.Cursor.change("hpen");
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
				PollCtrl.UI.Cursor.change("del");
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
		
		// pollType에 맞는 항목 폼을 토글러
		toggleItemForm : function(type) {
			var elem = $("#poll_info_box");
			
			if (type == '0') {
				$(".multichoice_box", elem).show();
				$("#item_adddel").show();
				$(".alternative_box", elem).hide();
				$(".short_box", elem).hide();
				$(".drawpoll_box").hide();
				if($("#poll_update_btn").css("display") == "none")
					$("#poll_save_btn").show();
				$("#poll_send_btn").removeClass("btn_full_blue");
			} else if (type == '1') {
				$(".multichoice_box", elem).hide();
				$("#item_adddel").hide();
				$(".alternative_box", elem).show();
				$(".short_box", elem).hide();
				$(".drawpoll_box").hide();
				
				if($("#poll_update_btn").css("display") == "none")
					$("#poll_save_btn").show();
				$("#poll_send_btn").removeClass("btn_full_blue");
				
			} else if (type == '2'){
				$(".multichoice_box", elem).hide();
				$("#item_adddel").hide();
				$(".alternative_box", elem).hide();
				$(".short_box", elem).show();
				$(".drawpoll_box").hide();
				
				if($("#poll_update_btn").css("display") == "none")
					$("#poll_save_btn").show();
				$("#poll_send_btn").removeClass("btn_full_blue");
				
				
			} else if (type == '3') {
				$(".multichoice_box", elem).hide();
				$("#item_adddel").hide();
				$(".alternative_box", elem).hide();
				$(".short_box", elem).hide();
				$(".drawpoll_box").show();
				
				$("#poll_save_btn").hide();
				$("#shutdown_time").val("0");  // 제한 시간 없음으로 설정함
				$("#custom_time").hide();
				
				$("#poll_send_btn").addClass("btn_full_blue");
				
				
				PollCtrl.UI.captureCanvas();   // 전체화면 캡쳐 프로세스 실행
			}
		},
		
		// 항목 열 추가
		addItemRow : function() {
			var itemCnt = $("#multichoice_box :text").length;
			var num = itemCnt + 1;
			
			$("#multichoice_box").append($("<span>" + num + "</span><input type='text' placeholder='" + _msg("poll.answer.input") + "'/>"));
			$("#multichoice_box").scrollTop($("#multichoice_box").prop("scrollHeight"));
			
			if(num <= 4)
				$("#item_adddel .btn_delexam").hide();
			else
				$("#item_adddel .btn_delexam").show();
			if(num >= 10)
				$("#item_adddel .btn_addexam").hide();
			else
				$("#item_adddel .btn_addexam").show();
		},
		
		// 항목 열 삭제
		delItemRow : function() {
			$("#multichoice_box span").get(-1).remove();
			$("#multichoice_box :text").get(-1).remove();
			
			var rows = $("#multichoice_box > :text").length;
			if(rows <= 4)
				$("#item_adddel .btn_delexam").hide();	
			else
				$("#item_adddel .btn_delexam").show();
			if(rows >= 10)
				$("#item_adddel .btn_addexam").hide();
			else
				$("#item_adddel .btn_addexam").show();
				
		},
		
		toggleFolder : function(elem) {
			if($(elem).parent().hasClass("open")) {
				$(elem).parent().removeClass("open");
			} else {
				$(elem).parent().addClass("open");
			}
		},
		
		closePollResult : function(idStr) {
			$("#" + idStr).hide();
			$("#" + idStr).empty();
		},
		
		close : function(idStr) {
			var container = document.getElementById(idStr);
			if (container) {
				$(container).hide();
			}
			
			if (idStr == 'short_answer_sheet') {
				$("#short_answer_sheet .answer_short").val('');
				
			} else if (idStr == 'text_answer_sheet') {
				$("#text_answer_sheet .answer_text").val('');
				
			} else if (idStr == 'drawing_answer_result') {
				$(".poll_box_drawDetailWrap").each(function(){
					Utils.log("열려있는 상세 판서 답안 창을 닫는다...");
					$(this).remove();
				});
			} else if (idStr.indexOf("drawing_result") > -1) {
				$(container).remove();
			} else if (idStr == "drawing_answer_sheet") {   // 판서형 질문 답변 창 UI 초기화
				$("#" + idStr + " span.poll_thumb").css("background-image", "");
				$("#answer_select_capture > a.btn_del_img").hide();   // 영역 선택의 X 버튼은 보이지 않도록 처리함
				$("#" + idStr).removeClass("maxmode").addClass("minimode");  // 최소화 모드로 초기화..
			}
			
			$(document).unbind("keyup");  // ESC키 눌러서 질문 창 닫는 이벤트 destroy 처리..
			
		},
		
		show : function(idStr) {
			if (idStr == "btn_call_poll_answer_wrap") {
				var container = document.getElementById(idStr);
				if (container) {
					$(container).show();
					$("#poll_guide_pop").show();
				} else {
					var htmlStr = "<div id=\"btn_call_poll_answer_wrap\" class=\"btn_call_pollAnswer_wrap\" style=\"visibility:visible;\"><div id=\"poll_guide_pop\" class=\"useGuide_pop guide04\">"+_msg("canvas.bubble.response")+"</div>\
							<a id=\"btn_call_poll_answer\" href=\"\" class=\"btn_call_pollAnswer\">"+_msg("poll.btn.submit")+"<span class=\"loader\"></span></a></div>";
					$("#wrap .quicklink").append(htmlStr);
					$("#poll_guide_pop").click(function(e){
						e.preventDefault();
						$("#poll_guide_pop").hide();
					});
				}
				
			} else if (idStr == "btn_wait_poll_answer") {
				var container = document.getElementById(idStr);
				if (container) {
					$(container).show();
				} else {
					var htmlStr = "<a id=\"btn_wait_poll_answer\" href=\"\" class=\"btn_wait_pollAnswer\">"+_msg("poll.btn.submit.ing")+"<span class=\"loader\"></span></a>";
					$("#wrap .quicklink").append(htmlStr);
				}
			} else {
				var container = document.getElementById(idStr);
				if (container) {
					$(container).show();
				}
			}
		},
		
		hide : function(idStr) {
			var container = document.getElementById(idStr);
			if (container) {
				$(container).hide();
				if (idStr == "btn_call_poll_answer_wrap") {
					$("#poll_guide_pop").hide();
				}
			}
		},
		
		
		toggleMinMax : function(idStr) {
			var container = document.getElementById(idStr);
			if ($(container).css("display") == "none")
				$(container).show();
			if ($(container).hasClass("minimode")) {
				$(container).addClass("maxmode").removeClass("minimode");
			} else if ($(container).hasClass("maxmode")) {
				$(container).removeClass("maxmode").addClass("minimode");
			}
		},
		
		
		/**
		 * PollCtrl.UI.open : 폴과 관련된 UI 요소들을 띄워줌
		 */
		open : function(idStr) {
			$(".mail_box").hide();
			
			$(".poll_box").hide();
			$(".poll_box2").hide();
			
			// ESC키 눌러서 질문 창 닫는 이벤트 추가
			$(document).bind("keyup", function(e) {
				if (e.keyCode == 27) {
					PollCtrl.UI.close(idStr);
				}
			});
			
			if (idStr == "poll_info_box") {
				if(!Ctrl._checkAuth(true, false)) return;
				
				if(PollCtrl.isProgress) {
					Ctrl.Msg.show(_msg("poll.draw.in.prgs"));
					return;
				}
				
				$("#poll_info_box .poll_tab").removeClass("poll_tab_length3");
				$("#poll_info_box .tab_draw").show();
				
				PollCtrl.UI.drawingContainerMode = "question";
				
				var container = document.getElementById(idStr);
				
				if (container) {
					$(container).show();
					PollCtrl.UI.clearPollMakeForm();
				} else {
					
					var htmlStr = "<div id=\"poll_info_box\" class=\"poll_box\" style=\"display: block;\">\
									<span class=\"poll_header\">\
										<span class=\"pop_tit\">" + _msg("poll.info.header") + "</span>\
										<a href=\"javascript:PollCtrl.UI.close('poll_info_box');\"></a>\
									</span>\
									<div class=\"poll_body\">\
										<div class=\"question_box\">\
											<textarea id=\"poll_title\" placeholder=\"" + _msg("poll.title.input") + "\"></textarea>\
										</div>\
										<div class=\"poll_tab\">\
											<a href=\"javascript:PollCtrl.UI.toggleItemForm('0')\" class=\"tab_multi on\" value=\"0\">\
												<span>" + _msg("poll.type.multi") + "</span>\
											</a>\
											<a href=\"javascript:PollCtrl.UI.toggleItemForm('1')\" class=\"tab_ox\" value=\"1\">\
												<span>" + _msg("poll.type.alter") + "</span>\
											</a>\
											<a href=\"javascript:PollCtrl.UI.toggleItemForm('2')\" class=\"tab_short \" value=\"2\">\
												<span>" + _msg("poll.type.short") + "</span>\
											</a>\
											<a href=\"javascript:PollCtrl.UI.toggleItemForm('3')\" class=\"tab_draw \" value=\"3\">\
												<span>" + _msg("poll.type.draw") + "</span>\
											</a>\
										</div>\
										<div class=\"answer_box\">\
											<div id=\"multichoice_box\" class=\"multichoice_box\" style=\"display: block;\">\
												<span>1</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
												<span>2</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
												<span>3</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
												<span>4</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
											</div>\
											<div id=\"item_adddel\" class=\"example_adddel\">\
												<span class=\"duplicate_chk\">\
													<a href=\"#\" id=\"dup_choice_allow\" class=\"btn_mo_checkbox\"></a>\
													<label for=\"radio4\">" + _msg("poll.dup.choice") + "</label></span>\
												<a href=\"javascript:PollCtrl.UI.delItemRow();\" class=\"btn_delexam\" style=\"display:none;\">" + _msg("btn.del") + "</a>\
												<a href=\"javascript:PollCtrl.UI.addItemRow();\" class=\"btn_addexam\">" + _msg("btn.add") + "</a>\
											</div>\
											<div id=\"alternative_box\" class=\"alternative_box\" style=\"display: none;\">\
												<select id=\"alternative_box_sel\" onchange=\"PollCtrl.UI.toggleAlternativePollFrm(this.value)\">\
													<option value=\"\">" + _msg("poll.alter.select") + "</option>\
													<option value=\"0\">" + _msg("poll.alter.0") + "</option>\
													<option value=\"1\">" + _msg("poll.alter.1") + "</option>\
													<option value=\"2\">" + _msg("poll.alter.2") + "</option>\
													<option value=\"3\">" + _msg("poll.alter.other") + "</option>\
												</select>\
												<div class=\"alternative_ab\" style=\"display: none;\">\
													<span>A.</span><input type=\"text\" placeholder=\"A Enter the Examples\"/>\
													<span>B.</span><input type=\"text\" placeholder=\"B Enter the Examples\"/>\
												</div>\
												<div class=\"alternative_direct\" style=\"display: none;\">\
													<span>1</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
													<span>2</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
												</div>\
											</div>\
											<div class=\"drawpoll_box\" style=\"display: none;\">\
												<div id=\"full_capture\" class=\"allArea on\">\
													<span id=\"full_screen_thumb\" class=\"poll_thumb\" style=\"cursor:pointer;\">\
													</span>\
													<a href=\"\" class=\"btn_selectArea\">" + _msg("poll.draw.qstn.entire") + "</a>\
												</div>\
												<div id=\"select_capture\" class=\"selectArea off\">\
													<span id=\"selection_thumb\" class=\"poll_thumb addImg\" style=\"cursor:pointer;\">\
														<span class=\"description\">영역을 선택 해 주세요</span>\
														<a href=\"\" id=\"btn_del_img\" class=\"btn_del_img\" title=\"Delete\" style=\"display:none;\"></a>\
													</span>\
													<a href=\"#\" class=\"btn_selectArea\">" + _msg("poll.draw.qstn.select") + "</a>\
												</div>\
												<input type=\"hidden\" id=\"qusbinary\" />\
												<div class=\"question_to\">\
													<p>" + _msg("poll.draw.objt") + "</p>\
													<a href=\"\" class=\"question_to_all on\">" + _msg("poll.draw.objt.all") + "</a>\
													<a href=\"\" class=\"question_to_teacher\">" + _msg("poll.draw.objt.teacher") + "</a>\
												</div>\
											</div>\
										</div>\
										<div class=\"option_box\">\
											<select id=\"shutdown_time\" onchange=\"PollCtrl.UI.onSelectTime();\">\
												<option value=\"30\" selected>" + _msg("poll.time.sel.30") + "</option>\
												<option value=\"40\">" + _msg("poll.time.sel.40") + "</option>\
												<option value=\"50\">" + _msg("poll.time.sel.50") + "</option>\
												<option value=\"60\">" + _msg("poll.time.sel.60") + "</option>\
												<option value=\"80\">" + _msg("poll.time.sel.80") + "</option>\
												<option value=\"100\">" + _msg("poll.time.sel.100") + "</option>\
												<option value=\"0\">" + _msg("poll.time.sel.nolmt") + "</option>\
												<option value=\"custom\">" + _msg("poll.time.sel.custom") + "</option>\
											</select>\
											<input type=\"text\" id=\"custom_time\" class=\"input_time\" placeholder=\"Enter the end times directly\" style=\"display:none;\"/>\
										</div>\
										<div class=\"poll_btn\">\
											<a id=\"poll_update_btn\" style=\"display:none;\">" + _msg("poll.btn.update") + "</a>\
											<a id=\"poll_remove_btn\" style=\"display:none;\">" + _msg("poll.btn.remove") + "</a>\
											<a id=\"poll_send_btn\" href=\"javascript:PollCtrl.Action.Master.createPoll(true);\">" + _msg("poll.btn.send") + "</a>\
											<a id=\"poll_save_btn\" href=\"javascript:PollCtrl.Action.Master.createPoll(false);\">" + _msg("btn.save") + "</a>\
										</div>\
									</div>\
								</div>";
					
					$("#wrap").append(htmlStr);
					
					$("#dup_choice_allow").bind("click", function(){
						$(this).toggleClass("on");
					});
					
					$(".poll_tab > a").bind("click", function(){
						$(".poll_tab > a").each(function(){
							$(this).removeClass("on");
						});
						$(this).addClass("on");
					})
					
					$("#full_capture > a.btn_selectArea, #full_screen_thumb").bind("click", function(e){
						e.preventDefault();
						PollCtrl.UI.captureCanvas();
						$("#select_capture").removeClass("on");
						$("#select_capture").addClass("off");
						$("#full_capture").removeClass("off");
						$("#full_capture").addClass("on");
					});
					
					//$("#select_capture > a.btn_selectArea, #select_capture > span.poll_thumb").bind("click", function(){});
					$("#select_capture > a.btn_selectArea, #selection_thumb").bind("click", function(e){
						e.preventDefault();
						$("#full_capture").removeClass("on");
						$("#full_capture").addClass("off");
						$("#select_capture").removeClass("off");
						$("#select_capture").addClass("on");
						//console.log($("#selection_thumb").css("background-image"));
						if($("#selection_thumb").css("background-image") == "" || $("#selection_thumb").css("background-image") == "none")
							PollCtrl.UI.selectCaptureArea();
					});
					
					
					$(".question_to_all").click(function(e) {
						e.preventDefault();
						if (!PacketMgr.isParentCreator) {
							return;
						}
						if(!$(e.target).hasClass("on")) {
							$(e.target).addClass("on");
							$(".question_to_teacher").removeClass("on");
						} else {
							return;
						}
					});
					
					$(".question_to_teacher").click(function(e) {
						e.preventDefault();
						if (PacketMgr.isParentCreator) {
							return;
						}
						if (!$(e.target).hasClass("on")) {
							$(e.target).addClass("on");
							$(".question_to_all").removeClass("on");
						} else {
							return;
						}
					});
					
					$("#btn_del_img").click(function(e) {
						e.stopPropagation();
						e.preventDefault();
						$(e.target).hide();
						$("#selection_thumb").css("background-image", "none");
						$("#qusbinary").val("");
					});
					
					PollCtrl.UI.clearPollMakeForm();
				}
				
				
				
			} else if (idStr == "question_info_box") {
				if(!Ctrl._checkAuth(true, false)) return;
				
				var container = document.getElementById(idStr);
				console.log(container);
				if(container != null) {
					$(container).show();
					//PollCtrl.UI.clearPollMakeForm();
					
				} else {
					var htmlStr = "<div id=\"question_info_box\" class=\"poll_box\" style=\"display:block;\">\
						<span class=\"poll_header\">\
							<span class=\"pop_tit\">"+_msg("poll.title.question.start")+"</span>\
							<a href=\"javascript:PollCtrl.UI.close('question_info_box');\"></a>\
						</span>\
						<div class=\"poll_body\">\
							<div class=\"question_box\">\
								<span class=\"poll_tit\"><span>"+_msg("poll.title.question.text")+"</span><span class=\"sub_tit\">"+_msg("poll.title.question.sub")+"</span></span>\
								<textarea id=\"question_title\" placeholder=\""+_msg("poll.input.question.placeholder")+"\"/></textarea>\
							</div>\
							<div class=\"answer_box\">\
								<span class=\"poll_tit2\"><span>"+_msg("poll.title.answer.text")+"</span><span class=\"sub_tit\">"+_msg("poll.title.answer.sub")+"</span></span>\
								<div class=\"answer_type\">\
									<input type=\"radio\" name=\"question_type\" id=\"writing_type\" value=\"3\" checked /><label for=\"writing_type\">"+_msg("poll.question.type.draw")+"</label>\
									<input type=\"radio\" name=\"question_type\" id=\"text_type\" value=\"2\" /><label for=\"text_type\">"+_msg("poll.question.type.text")+"</label>\
								</div>\
							</div>\
							<div class=\"option_box\">\
								<span class=\"poll_tit\"><span>"+_msg("poll.question.option")+"</span><span class=\"sub_tit\">"+_msg("poll.time.sel.title")+"</span></span>\
								<select id=\"question_shutdown_time\">\
									<option value=\"30\" selected>"+_msg("poll.question.time.30")+"</option>\
									<option value=\"60\">"+_msg("poll.question.time.1")+"</option>\
									<option value=\"180\">"+_msg("poll.question.time.3")+"</option>\
									<option value=\"300\">"+_msg("poll.question.time.5")+"</option>\
									<option value=\"600\">"+_msg("poll.question.time.10")+"</option>\
									<option value=\"900\">"+_msg("poll.question.time.15")+"</option>\
									<option value=\"0\">"+_msg("poll.question.time.unlimit")+"</option>\
								</select>\
							</div>\
							<div class=\"poll_btn\">\
								<a id=\"question_send_btn\" href=\"javascript:PollCtrl.Action.Master.Question.createQuestion(true);\" class=\"btn_full\">"+_msg("poll.btn.send")+"</a>\
							</div>\
						</div>\
					</div>"; 
					
					$("#wrap").append(htmlStr);
				}
				//$("#question_send_btn").prop("href", "javascript:PollCtrl.Action.Master.Question.createQuestion(true);");
				
			} else if (idStr == "poll_list_box") {
				//if(!Ctrl._checkAuth(true, false)) return;
				var container = document.getElementById(idStr);
				
				if (container) {
					$(container).show();
				} else {
					var htmlStr = "<div id=\"poll_list_box\" class=\"poll_box\">\
							<span class=\"poll_header\">\
								<span class=\"pop_tit\">"+_msg("poll.tmp.list.header")+"</span>\
								<a href=\"javascript:PollCtrl.UI.close('poll_list_box');\"></a>\
							</span>\
							<div class=\"poll_body\">\
								<ul id=\"poll_list\" class=\"poll_list\"></ul>\
								<ul id=\"page_navi\" class=\"page_navi\"></ul>\
								<div class=\"poll_btn\">\
									<a href=\"javascript:PollCtrl.Action.Master.sendPollFromList();\" class=\"btn_full\">"+_msg("poll.btn.send")+"</a>\
								</div>\
							</div>\
						</div>";
					$("#wrap").append(htmlStr);
				}
				
			} else if (idStr == "poll_complete_list_box") {
				var container = document.getElementById(idStr);
				
				if (container) {
					$(container).show();
				} else {
					var htmlStr = "<div id=\"poll_complete_list_box\" class=\"poll_box\">\
							<span class=\"poll_header\">\
								<span class=\"pop_tit\">" + _msg("poll.list.header") + "</span>\
								<a href=\"javascript:PollCtrl.UI.close('poll_complete_list_box');\"></a>\
							</span>\
							<div class=\"poll_body\">\
								<ul id=\"poll_complete_list\" class=\"poll_list\"></ul>\
								<ul id=\"page_navi_complete\" class=\"page_navi\"></ul>\
								<div class=\"poll_btn\">\
									<a href=\"javascript:PollCtrl.Action.Master.deleteCompletedPoll();\" class=\"btn_full\">" + _msg("poll.btn.remove") + "</a>\
								</div>\
							</div>\
						</div>";
					$("#wrap").append(htmlStr);			
				}
			} else if (idStr == "poll_timer_box") {
				var container = document.getElementById(idStr);
				
				if (container) {
					$(container).show();
					$("#poll_timer_box > .question_box").text("");
					$("#stay_user_list").empty();
					
				} else {
					var htmlStr = "<div id=\"poll_timer_box\" class=\"poll_box maxmode\">\
								<span class=\"poll_header\">\
									<span class=\"pop_tit\">"+_msg("poll.timer.header")+"</span>\
									<a id=\"btn_exit_poll\" href=\"javascript:PollCtrl.UI.invisible('poll_timer_box');\"></a>\
									<a href=\"javascript:PollCtrl.UI.toggleMinMax('poll_timer_box');\" class=\"btn_poll_minimize\"></a>\
								</span>\
								<div class=\"poll_body\">\
									<div class=\"question_box\">\
										<span class=\"question_text\"></span>\
									</div>\
									<div class=\"poll_stay_user\">\
										<div class=\"stay_user_tit\">\
											" + _msg("poll.title.answered.text") + " ( <span id=\"answer_cnt\" class=\"user_count\">0</span>/<span id=\"answer_tot_cnt\">9</span> )\
										</div>\
										<div id=\"stay_user_list\" class=\"stay_user_list\">\
										</div>\
									</div>\
									<div class=\"poll_stay2\">\
										<div class=\"poll_count\">\
											<a id=\"timer\"></a>"+_msg("poll.timer.second") + "\
										</div>\
										<div class=\"bg_count\">\
											<span id=\"poll_progress\" class=\"poll_prograss\" style=\"width:100%\"></span>\
										</div>\
									</div>\
									<div class=\"poll_btn\">\
										<a id=\"poll_stop_btn\" class=\"btn_full\">"+_msg("btn.finish")+"</a>\
									</div>\
								</div>\
							</div>";
				
					$("#wrap").append(htmlStr);
				}
				
			} else if (idStr == "multi_choice_sheet") {   // 선다형 문제지 화면
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"multi_choice_sheet\" class=\"poll_box\">\
									<span class=\"poll_header\">\
										<span class=\"pop_tit\"></span>\
										<a href=\"javascript:PollCtrl.UI.hide('multi_choice_sheet');\"></a>\
									</span>\
									<div class=\"poll_body\">\
										<div class=\"question_box\">\
											<span class=\"question_text\"></span>\
										</div>\
										<div class=\"answer_input_box\">\
											<span class=\"poll_tit2\"><span>" + _msg("poll.sheet.answer") + "</span></span>\
											<ul id=\"multi_choice_item\" class=\"answermulti_box\" style=\"display: block;\"></ul>\
										</div>\
										<div class=\"poll_user_count\">" + _msg("poll.sheet.timer") + " : <span></span> " + _msg("poll.timer.second") + "</div>\
										<div class=\"poll_user_count\" style=\"display:none;\">" + _msg("poll.time.sel.nolmt") + "</div>\
										<div class=\"poll_btn\">\
											<a href=\"javascript:PollCtrl.Action.Attender.submitPollResult('0')\" class=\"btn_full\">" + _msg("poll.btn.submit") + "</a>\
										</div>\
									</div>\
								</div>";
					
					$("#wrap").append(htmlStr);
				}
				
			} else if (idStr == 'alternative_sheet') {   // 양자택일 문제지 화면
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"alternative_sheet\" class=\"poll_box\">\
									<span class=\"poll_header\">\
										<span class=\"pop_tit\"></span>\
										<a href=\"javascript:PollCtrl.UI.hide('alternative_sheet');\"></a>\
									</span>\
									<div class=\"poll_body\">\
										<div class=\"question_box\">\
											<span class=\"question_text\"></span>\
										</div>\
										<div class=\"answer_input_box\">\
											<span class=\"poll_tit2\"><span>" + _msg("poll.sheet.answer") + "</span></span>\
											<ul id=\"alternative_item\" class=\"answermulti_box\" style=\"display: block;\"></ul>\
										</div>\
										<div class=\"poll_user_count\">" + _msg("poll.sheet.timer") + " : <span></span> " + _msg("poll.timer.second") + "</div>\
										<div class=\"poll_user_count\" style=\"display:none;\">" + _msg("poll.time.sel.nolmt") + "</div>\
										<div class=\"poll_btn\">\
											<a href=\"javascript:PollCtrl.Action.Attender.submitPollResult('1')\" class=\"btn_full\">" + _msg("poll.btn.submit") + "</a>\
										</div>\
									</div>\
								</div>";
					$("#wrap").append(htmlStr);
				}
			} else if (idStr == "short_answer_sheet") {
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"short_answer_sheet\" class=\"poll_box\">\
									<span class=\"poll_header\">\
										<span class=\"pop_tit\"></span>\
										<a href=\"javascript:PollCtrl.UI.hide('short_answer_sheet');\"></a>\
									</span>\
									<div class=\"poll_body\">\
										<div class=\"question_box\">\
											<span class=\"question_text\"></span>\
										</div>\
										<div class=\"answer_input_box\">\
											<span class=\"poll_tit2\"><span>" + _msg("poll.sheet.answer") + "</span></span>\
											<textarea class=\"answer_short\"></textarea>\
										</div>\
										<div class=\"poll_user_count\">" + _msg("poll.sheet.timer") + " : <span></span> " + _msg("poll.timer.second") + "</div>\
										<div class=\"poll_user_count\" style=\"display:none;\">" + _msg("poll.time.sel.nolmt") + "</div>\
										<div class=\"poll_btn\">\
											<a href=\"javascript:PollCtrl.Action.Attender.submitPollResult('2')\" class=\"btn_full\">" + _msg("poll.btn.submit") + "</a>\
										</div>\
									</div>\
								</div>";
					$("#wrap").append(htmlStr);
				}
			} else if (idStr == "hand_writing_sheet") {    // 판서형 예전 버전 화면 - deprecated (2016.12.20)
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"hand_writing_sheet\" class=\"poll_box2\">\
							<span class=\"poll_header\">\
								<span class=\"pop_tit\">" + _msg("poll.draw.sheet.title") + "</span>\
								<a href=\"javascript:PollCtrl.UI.close('hand_writing_sheet');\"></a>\
							</span>\
							<div class=\"poll_body\">\
								<div class=\"question_box\">\
									<span class=\"poll_tit\"><span>" + _msg("poll.draw.sheet.question") + "</span></span>\
									<span class=\"question_text\"></span>\
								</div>\
								<div class=\"answer_input_box\">\
									<span class=\"poll_tit2\"><span>" + _msg("poll.draw.sheet.answer") + "</span></span>\
									<div class=\"answer_draw_menu\">\
										<a title=\"clear\" id=\"answer_clear\" class=\"btn_clear\"></a>\
										<a title=\"pen\" id=\"answer_eraser\" class=\"btn_eraser\"></a>\
										<span class=\"section_divide\">|</span>\
										<div id=\"answer_pen_size\">\
											<a title=\"color\" id=\"pen_size3\" class=\"color_select2\"><span class=\"value3\" style=\"background:rgba(0,0,0,1)\"></span></a>\
											<a title=\"color\" id=\"pen_size2\" class=\"color_select2\"><span class=\"value2\" style=\"background:rgba(0,0,0,1)\"></span></a>\
											<a title=\"color\" id=\"pen_size1\" class=\"color_select2 checked\"><span class=\"value1\" style=\"background:rgba(0,0,0,1)\"></span></a>\
										</div>\
										<span class=\"section_divide\">|</span>\
										<div id=\"answer_pen_color\">\
											<a title=\"color\" id=\"pen_color3\" class=\"color_select2\"><span class=\"pointer_color3\"></span></a>\
											<a title=\"color\" id=\"pen_color2\" class=\"color_select2\"><span class=\"pointer_color2\"></span></a>\
											<a title=\"color\" id=\"pen_color1\" class=\"color_select2 checked\"><span class=\"pointer_color1\"></span></a>\
										</div>\
										<span class=\"section_divide\">|</span>\
										<a title=\"pen\" id=\"answer_pen\" class=\"btn_pen checked\"></a>\
									</div>\
									<div id=\"cursor_wrap\">\
										<div id=\"answer_canvas\" class=\"answer_draw sketch_poll\">\
										</div>\
									</div>\
								</div>\
								<div class=\"poll_user_count\">" + _msg("poll.sheet.timer") + " : <span></span> " + _msg("poll.timer.second") + "</div>\
								<div class=\"poll_btn\">\
									<a href=\"javascript:PollCtrl.Action.Attender.submitPollResult('3')\" class=\"btn_full\">" + _msg("poll.btn.submit") + "</a>\
								</div>\
							</div>\
						</div>";
					$("#wrap").append(htmlStr);
				}
			} else if (idStr == "text_answer_sheet") {   // 서술형 문제지 화면
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"text_answer_sheet\" class=\"poll_box2\">\
								<span class=\"poll_header\">\
								<span class=\"pop_tit\">" + _msg("poll.text.sheet.title") + "</span>\
								<a href=\"javascript:PollCtrl.UI.close('text_answer_sheet');\"></a>\
							</span>\
							<div class=\"poll_body\">\
								<div class=\"question_box\">\
									<span class=\"poll_tit\"><span>" + _msg("poll.text.sheet.question") + "</span></span>\
									<span class=\"question_text\"></span>\
								</div>\
								<div class=\"answer_input_box\">\
									<span class=\"poll_tit2\"><span>" + _msg("poll.text.sheet.answer") + "</span></span>\
									<textarea class=\"answer_text\" placeholder=\"\"></textarea>\
								</div>\
								<div class=\"poll_user_count\">" + _msg("poll.sheet.timer") + " : <span></span> " + _msg("poll.timer.second") + "</div>\
								<div class=\"poll_user_count\" style=\"display:none;\">" + _msg("poll.time.sel.nolmt") + "</div>\
								<div class=\"poll_btn\">\
									<a href=\"javascript:PollCtrl.Action.Attender.submitPollResult('2')\" class=\"btn_full\">" + _msg("poll.btn.submit") + "</a>\
								</div>\
							</div>\
						</div>";
					$("#wrap").append(htmlStr);
				}
			} else if (idStr == "drawing_answer_sheet") {   // 판서형 문제지 화면 - 신규 추가됨 (2016.12.20)
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
					$("#answer_full_capture").removeClass("on").addClass("off");
					$("#answer_select_capture").removeClass("on").addClass("off");
					$("#answer_full_capture > span.poll_thumb").css("background-image", "none");
					$("#answer_select_capture > span.poll_thumb").css("background-image", "none");
					$("#answer_select_capture > a.btn_del_img").hide();
				} else {
					var htmlStr = "<div id=\"drawing_answer_sheet\" class=\"poll_box minimode\">\
										<span class=\"poll_header\">\
											<span class=\"pop_tit\">" + _msg("poll.draw.sheet.title") + "</span>\
											<a href=\"javascript:PollCtrl.UI.close('drawing_answer_sheet');\"></a>\
											<a href=\"javascript:PollCtrl.UI.toggleMinMax('drawing_answer_sheet');\" class=\"btn_poll_minimize\"></a>\
										</span>\
										<div class=\"poll_body\">\
											<div class=\"question_box\">\
												<span class=\"question_text\"></span>\
											</div>\
											<div class=\"answer_draw_box\">\
												<span class=\"poll_tit2\"><span>Answer</span></span>\
												<div class=\"drawpoll_box\">\
													<div id=\"answer_full_capture\" class=\"allArea off\">\
														<span class=\"poll_thumb\" style=\"cursor:pointer;\">\
														</span>\
														<a href=\"\" class=\"btn_selectArea\">" + _msg("poll.draw.qstn.entire") + "</a>\
													</div>\
													<div id=\"answer_select_capture\" class=\"selectArea off\">\
														<span class=\"poll_thumb addImg\" style=\"cursor:pointer;\">\
															<span class=\"description\">영역을 선택 해 주세요</span>\
															<a href=\"\" class=\"btn_del_img\" title=\"Delete\" style=\"display:none;\"></a>\
														</span>\
														<a href=\"\" class=\"btn_selectArea\">" + _msg("poll.draw.qstn.select") + "</a>\
													</div>\
												</div>\
											</div>\
											<div class=\"poll_user_count\">" + _msg("poll.sheet.timer") + " : <span></span> " + _msg("poll.timer.second") + "</div>\
											<div class=\"poll_user_count\" style=\"display:none;\">" + _msg("poll.time.sel.nolmt") + "</div>\
											<div class=\"poll_btn\">\
												<a href=\"javascript:PollCtrl.Action.Attender.submitPollResult('3')\" class=\"btn_full\">" + _msg("poll.btn.submit") + "</a>\
											</div>\
										</div>\
									</div>";
					$("#wrap").append(htmlStr);
					
					$("#answer_full_capture > a.btn_selectArea, #answer_full_capture > span.poll_thumb").bind("click", function(e){
						e.preventDefault();
						PollCtrl.UI.captureCanvas();
						$("#answer_select_capture").removeClass("on");
						$("#answer_select_capture").addClass("off");
						$("#answer_full_capture").removeClass("off");
						$("#answer_full_capture").addClass("on");
					});
					
					$("#answer_select_capture > a.btn_selectArea, #answer_select_capture > span.poll_thumb").bind("click", function(e){
						e.stopPropagation();
						e.preventDefault();
						$("#answer_full_capture").removeClass("on");
						$("#answer_full_capture").addClass("off");
						$("#answer_select_capture").removeClass("off");
						$("#answer_select_capture").addClass("on");
						if($("#answer_select_capture > span.poll_thumb").css("background-image") == "" || $("#answer_select_capture > span.poll_thumb").css("background-image") == "none")
							PollCtrl.UI.selectCaptureArea();
					});
					
					$("#answer_select_capture > a.btn_del_img").click(function(e) {
						e.stopPropagation();
						//e.preventDefault();
						$(e.target).hide();
						$("#answer_select_capture > span.poll_thumb").css("background-image", "none");
					});
				}
			
			
			} else if (idStr == 'selection_capture_container') {  // 영역 선택하는 UI - 신규 추가됨 (2016.12.20)
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"selection_capture_container\" class=\"poll_selectCaptureAreaWrap\">\
									<p class=\"poll_selectCaptureArea_tit\">" + _msg("poll.draw.sel.area.ansr") + "</p>\
									<div id=\"select_area\" class=\"selectArea\">\
										<a href=\"\" id=\"cancel_capture\" class=\"btn_cancel\" style=\"z-index:9999;\"></a>\
										<a href=\"\" id=\"confirm_capture\" class=\"btn_select\" style=\"z-index:9999;\"></a>\
									</div>\
								</div>";
					$("#wrap").append(htmlStr);
				}
				
				
			
			
			} else if (idStr == "poll_result_box") {
				var container = document.getElementById(idStr);
				
				if(container) {
					$(container).show();
				} else {
					var htmlStr = "<div id=\"poll_result_box\" class=\"poll_box\">\
										<span class=\"poll_header\">\
										<span class=\"pop_tit\">" + _msg("poll.result.header") + "</span>\
										<a onclick=\"javascript:PollCtrl.UI.close('poll_result_box');\"></a>\
									</span>\
									<div class=\"poll_body\">\
										<div class=\"question_box\">\
											<span class=\"question_text\"></span>\
										</div>\
										<ul id=\"poll_result_graph\" class=\"poll_result\">\
										</ul>\
										<div class=\"people_result\">( " + _msg("poll.people.total") + " : <a id=\"total_cnt\"></a> / " + _msg("poll.people.voted") + " : <a id=\"answer_cnt\"></a> )</div>\
										<div class=\"poll_btn\" style=\"display:block;\">\
											<a id=\"poll_report_send_btn\" class=\"btn_full\">" + _msg("poll.btn.send") + "</a>\
										</div>\
									</div>\
								</div>";
					$("#wrap").append(htmlStr);
				}
			
			} else if (idStr == "drawing_answer_result") {  // 판서형 질문의 결과 화면 - 신규 추가됨 (2016.12.20)
				var container = document.getElementById(idStr);
				
				if(container) { 
					$(container).show();
				} else {
					var htmlStr = "<div id=\"drawing_answer_result\" class=\"poll_box\">\
										<span class=\"poll_header\">\
											<span class=\"pop_tit\">" + _msg("poll.result.header") + "</span>\
											<a onclick=\"javascript:PollCtrl.UI.close('drawing_answer_result');\"></a>\
										</span>\
										<div class=\"poll_body\">\
											<div class=\"question_box\">\
												<span class=\"question_text\"></span>\
											</div>\
											<ul id=\"answer_user_list\" class=\"poll_result_draw\">\
											</ul>\
											<div class=\"people_result\">( " + _msg("poll.people.total") + " : <a id=\"draw_total_cnt\"></a> / " + _msg("poll.people.voted") + " : <a id=\"draw_answer_cnt\"></a> )</div>\
											<div class=\"poll_btn\" style=\"display:block;\">\
												<a id=\"poll_report_send_btn\" class=\"btn_full\">" + _msg("poll.btn.send") + "</a>\
											</div>\
										</div>\
									</div>";
					$("#wrap").append(htmlStr);
				}
				
			} else if (idStr == "alert_poll") {
				var container = document.getElementById(idStr);
				if (container) {
					$(container).show();
				} else {
					var htmlStr = "<div id=\"alert_poll\" class=\"pop_alert\">\
										<div class=\"pop_conts\">\
											<p class=\"subtitle\">"+_msg("poll.submit.back.t")+"\
										</div>\
										<div class=\"pop_btnArea\">\
											<a href=\"\" class=\"btn_cancel color_white\">" + _msg("klounge.cancel") + "</a>\
											<a href=\"\" id=\"btn_confirm\" class=\"color_green\">" + _msg("klounge.ok") + "</a>\
										</div>\
									</div>";
					$("#wrap").append(htmlStr);
				}
				
			} else if (idStr == "noti_drawing_poll") {
				var container = document.getElementById(idStr);
				if (container) {
					$(container).show();
				} else {
					var htmlStr = "<div id=\"noti_drawing_poll\" class=\"poll_box\">\
							<span class=\"poll_header\">\
								<span class=\"pop_tit\">"+_msg("poll.type.draw")+"</span>\
								<a onclick=\"javascript:PollCtrl.UI.close('noti_drawing_poll');\"></a>\
							</span>\
							<div class=\"poll_body\">\
								<div class=\"poll_alert\">"+_msg("poll.draw.get.qstn")+"</div>\
								<div class=\"poll_alert_question\">\
									<div class=\"poll_alert_textWrap\">\
										<div id=\"noti_poll_title\" class=\"poll_alert_text\">\
										</div>\
									</div>\
									<div class=\"poll_alert_image\">\
									</div>\
								</div>\
								<div class=\"poll_btn\">\
									<a href=\"\" id=\"btn_move\" class=\"btn_full\">내 방으로 이동</a>\
								</div>\
							</div>\
						</div>";
					$("#wrap").append(htmlStr);
				}
			
			} else {
				$("#" + idStr).css("display", "block");
			}
			
		},
		
		
		/**
		 * PollCtrl.UI.renderPollGraph
		 *  - 질문 결과 화면 (선다형, 양자택일, 단답형)
		 */
		renderPollGraph : function(dataMap, isMaster, totalUserCnt) {
			Utils.log("[renderPollGraph] paramMap : " + JSON.stringify(dataMap));
			var itemIdx        = dataMap.itemidx;
			var itemNm         = dataMap.itemnm;
			var pollItemNo     = dataMap.pollitemno;
			var pollNo         = dataMap.pollno;
			var answerTotalCnt = parseInt(dataMap.answertotalcnt);
			var answerUserCnt  = parseInt(dataMap.answerusercnt);
			var itemList       = dataMap.itemlist;
			var pollType       = dataMap.polltype;
			var pollTitle      = dataMap.title;
			
			
			
			PollCtrl.UI.open("poll_result_box");
			
			$("#poll_result_box .question_text").text(pollTitle);
			
			// 보내기 버튼 UI 처리..
			if (isMaster) {
				$("#poll_result_box .poll_btn").show();
				$("#poll_result_box .btn_full").unbind("click");
				$("#poll_result_box .btn_full").bind("click", function(e) {
					e.preventDefault();
					PollCtrl.Action.Master.sendPollReport(pollNo);
				});
			} else {
				$("#poll_result_box .poll_btn").hide();
			}
			
			$("#poll_result_graph").empty();
			
			var len = itemList.length;
			if (len > 0) {
				for (var i=0; i<len; i++) {
					var itemInfo = itemList[i];
					if (pollType == '0' || pollType == '1') {
						var itemIdx   = itemInfo.itemidx;
						var itemNm    = itemInfo.itemnm;
						var answerCnt = parseInt(itemInfo.answercnt);
						
						//Utils.log(answerCnt + "/" + answerTotalCnt);
						//Utils.log(answerCnt/answerTotalCnt);
						
						var rate = answerCnt / answerTotalCnt * 100;
						if(answerCnt == 0 && answerTotalCnt == 0 && isNaN(rate))
							rate = 0;
						
						var width = 185 * rate / 100;
						width += 85;   // 그래프 미니멈 픽셀값
						
						// 아이템 그래프 영역
						var listEl = document.createElement("li");
						
						var idxSpan = document.createElement("span");
						idxSpan.className = "examno" + itemIdx;
						idxSpan.innerHTML = itemIdx;
						
						var barSpan = document.createElement("span");
						barSpan.className = "exam_value" + itemIdx;
						barSpan.style.width = width + "px";
						//barSpan.innerHTML = parseInt(rate) + "%";
						
						var percentSpan = document.createElement("span");
						percentSpan.className = "value_per";
						percentSpan.innerHTML = parseInt(rate) + "%";
						
						var itemNmSpan = document.createElement("span");
						itemNmSpan.className = "short_value";
						itemNmSpan.innerHTML = itemNm;
						barSpan.appendChild(percentSpan);
						barSpan.appendChild(itemNmSpan);
						
						var cntSpan = document.createElement("span");
						cntSpan.className = "selectuser_no";
						cntSpan.innerHTML = answerCnt + _msg("poll.people");
						
						listEl.appendChild(idxSpan);
						listEl.appendChild(barSpan);
						listEl.appendChild(cntSpan);
						
						$("#poll_result_graph").append($(listEl));
						$("#poll_result_graph").removeClass().addClass("poll_result");
						
					} else if (pollType == '2') {
						var itemIdx    = itemInfo.itemidx;
						var itemNm     = itemInfo.itemnm;
						var answerUser = itemInfo.answeruser;
						console.log(itemInfo);
						var answerUserInfo = answerUser[0];
						
						var userNm = answerUserInfo.usernm;
						var userThumb = answerUserInfo.thumbnail;
						
						if (typeof(userThumb) == "undefined") {
							userThumb = Utils.addResPath("images", "thum_user.png");
						}
						
						var rowHtml = "<li>\
											<img class=\"user_photo\" src=\""+ userThumb +"\" />\
											<p class=\"user_name\">" + userNm + "</p>\
											<p class=\"answer\">" + itemNm + "</p>\
											<a onclick=\"javascript:PollCtrl.UI.toggleFolder(this);\" class=\"fold\"></a>\
										</li>";
						
						$("#poll_result_graph").append(rowHtml);
						$("#poll_result_graph").removeClass().addClass("poll_result_openissues");
					} else if (pollType == '3') {
						// TODO : 판서 질문 결과 보기도 같은 함수 내에 통합..
					}
					
				}
				
				var totalAnswerCnt = (typeof(totalUserCnt) == "undefined" || totalUserCnt == 0) ? answerTotalCnt : parseInt(totalUserCnt);
				$("#poll_result_box #total_cnt").text(totalAnswerCnt);
				$("#poll_result_box #answer_cnt").text(answerUserCnt);				
			
			} else {
				$("#poll_result_box #total_cnt").text(0);
				$("#poll_result_box #answer_cnt").text(0);
			}
			
			
			/**
			$("#poll_result_box div.people_result").show();
			if (typeof(totalUserCnt) == "undefined") {
				$("#poll_result_box div.people_result").hide();
			}
			**/
		},
		
		
		/**
		 * PollCtrl.UI.renderShortAnswerResult
		 *  - 질문 결과 화면 (단답형 / 서술형)
		 *  @deprecated
		 */
		renderShortAnswerResult : function(dataMap, isMaster) {
			console.log("[PollCtrl.UI.renderShortAnswerResult] dataMap : " + JSON.stringify(dataMap));
			var itemIdx        = dataMap.itemidx;
			var itemNm         = dataMap.itemnm;
			var pollItemNo     = dataMap.pollitemno;
			var pollNo         = dataMap.pollno;
			var answerTotalCnt = parseInt(dataMap.answertotalcnt);
			var answerUserCnt  = parseInt(dataMap.answerusercnt);
			var itemList       = dataMap.itemlist;
			var pollType       = dataMap.polltype;
			var pollCategory   = dataMap.pollcategory;
			var pollTitle      = dataMap.title;
			
			var containerIdStr  = null;
			var resultListIdStr = null;
			var container;
			
			if(isMaster) {
				containerIdStr  = "poll_result_short_answer_admin";
				resultListIdStr = "poll_list_short_answer_admin";
			} else {
				containerIdStr  = "poll_result_short_answer_user";
				resultListIdStr = "poll_list_short_answer_user";
			}
			
			PollCtrl.UI.open("poll_result_box");
			
			$("#poll_result_box .question_text").text(pollTitle);
			
			// 보내기 버튼 UI 처리..
			if (isMaster) {
				$("#poll_result_box .poll_btn").show();
				$("#poll_result_box .btn_full").unbind("click");
				$("#poll_result_box .btn_full").bind("click", function(e) {
					e.preventDefault();
					PollCtrl.Action.Master.sendPollReport(pollNo);
				});
			} else {
				$("#poll_result_box .poll_btn").hide();
			}
			
			$("#poll_result_graph").empty();
			
			var len = itemList.length;
			if (len > 0) {
				for (var i=0; i<len; i++) {
					
					var itemInfo   = itemList[i];
					var itemIdx    = itemInfo.itemidx;
					var itemNm     = itemInfo.itemnm;
					var answerUser = itemInfo.answeruser;
					console.log(itemInfo);
					var answerUserInfo = answerUser[0];
					
					var userNm = answerUserInfo.usernm;
					var userThumb = answerUserInfo.thumbnail;
					
					if (typeof(userThumb) == "undefined") {
						userThumb = Utils.addResPath("images", "thum_user.png");
					}
					
					var rowHtml = "<li>\
										<img class=\"user_photo\" src=\""+ userThumb +"\" />\
										<p class=\"user_name\">" + userNm + "</p>\
										<p class=\"answer\">" + itemNm + "</p>\
										<a onclick=\"javascript:PollCtrl.UI.toggleFolder(this);\" class=\"fold\"></a>\
									</li>";
					
					$("#poll_result_graph").append(rowHtml);
					
					// 아이템 그래프 영역
					
					$("#poll_result_box #total_cnt").text(answerTotalCnt);
					$("#poll_result_box #answer_cnt").text(answerUserCnt);
				}
				
			} else {
				$("#poll_result_box #total_cnt").text(0);
				$("#poll_result_box #answer_cnt").text(0);
				
			}
			
		},
		
		
		/**
		 * PollCtrl.UI.renderDrawingPollResult
		 *  - 질문 결과 화면 (판서)
		 */
		renderDrawingPollResult : function(pollNo, pollTitle, answerTotalCnt, answerCnt, answerList, isMaster) {
			
			PollCtrl.UI.open("drawing_answer_result");
			
			var container = $("#drawing_answer_result");
			
			var answerListEl = $("#drawing_answer_result #answer_user_list");
			answerListEl.empty();
			
			$("#draw_total_cnt").text(answerTotalCnt);
			$("#draw_answer_cnt").text(answerCnt);
			$("#drawing_answer_result .question_text").text(pollTitle);
			
			if (isMaster) {
				$("#drawing_answer_result .poll_btn").show();
				if(isMaster) {
					$("#drawing_answer_result .btn_full").unbind("click");
					$("#drawing_answer_result .btn_full").bind("click", function(e) {
						e.preventDefault();
						PollCtrl.Action.Master.sendPollReport(pollNo);
					});
				}
			} else {
				$("#drawing_answer_result .poll_btn").hide();
			}
			
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
					var htmlStr = "<li>\
										<img class=\"user_photo\" src=\"" + thumbnailSrc + "\">\
										<p class=\"user_name\">" + userNm + "</p>\
										<a class=\"btn_open\" onclick=\"javascript:PollCtrl.UI.showUserDrawingAnswer(" + i + ", '" + pollNo + "', '" + pollUserNo + "')\"></a>\
									</li>";
					answerListEl.append(htmlStr);
					
				}
			}
		},
		
		

		/**
		 * PollCtrl.UI.showUserDrawingAnswer
		 *  - 답변자별로 판서 답안을 볼 수 있는 함수
		 */
		showUserDrawingAnswer : function(idx, pollNo, pollUserNo) {
			var param = {
				pollno : pollNo,
				polluserno : pollUserNo
			};
			
			var el = $("#answer_user_list").children().get(idx);
			$(el).toggleClass("open");
			var userNm = $(el).find("p.user_name").text();
			var userThumbnail = $(el).find("img.user_photo").prop("src");
			
			$.ajax({
				type : 'post',
				url : Utils.addContext('poll/file/item/get.json'),
				async : true,
				cache : false,
				dataType : 'json',
				data : param,
				beforeSend : function(){},
				success : function(data) {
					Utils.log("[showUserDrawingAnswer] poll/file/item/get.json success ----- data : " + JSON.stringify(data));
					if (data.result == '0') {
						var resultMap = data.map;
						var fileNo = resultMap.POLLFILENO;
						var filePath = resultMap.filepath;
						
						var htmlStr = "<div id=\"drawing_result_" + fileNo + "\" class=\"poll_box_drawDetailWrap\">\
											<div class=\"drawDetail\">\
												<div class=\"userInfo\">\
													<img src=\"" + userThumbnail + "\" alt=\"\" class=\"user_photo\">\
													<span class=\"user_name\">" + userNm + "</span>\
												</div>\
												<a id=\"drawing_img_" + fileNo + "\" href=\"\" title=\"크게보기\"><img src=\"" + filePath + "\" alt=\"\"></a>\
												<div class=\"btnarea\">\
													<a href=\"javascript:PollCtrl.Action.Master.Question.shareDrawAnswer('" + fileNo + "', '" + filePath + "', '" + pollUserNo + "');\" class=\"btn_Detailadd\">삽입</a>\
													<a href=\"javascript:PollCtrl.UI.close('drawing_result_" + fileNo +"');\" class=\"btn_Detailclose\">닫기</a>\
												</div>\
											</div>\
										</div>";
						$("#wrap").append(htmlStr);
						
						if (!PollCtrl.isPresenter) {
							$("#drawing_result_" + fileNo + " a.btn_Detailadd").hide();
						} else {
							// 판서 답안 확대보기 이벤트..
							$("#drawing_result_" + fileNo +" a#drawing_img_" + fileNo).click(function(e){
								e.preventDefault();
								PollCtrl.UI.openDrawingDetail(fileNo, filePath);
							});
						}
					}
				},
				error : function(){ Utils.log("error..") },
				complete : function(){} 
			});
			
		},
		
		
		/**
		 * PollCtrl.UI.openDrawingDetail
		 *  - 판서 답안 확대보기 UI 처리..
		 */
		openDrawingDetail : function(fileNo, filePath) {
			
			var screenWidth = $(window).width();
			var screenHeight = $(window).height();
			
			console.log("[PollCtrl.UI.openDrawingDetail] screenWidth : " + screenWidth + ", screenHeight : " + screenHeight);
			
			var idStr = "drawing_detail_" + fileNo;
			var container = document.getElementById(idStr);
			
			if(container) {
				
			} else {
				
				var errorImg = Utils.addResPath("images", "user_answer.png");
				//var errorImg = Utils.addResPath("images", "insert_img2.jpg");
				
				var htmlStr = "<div id=\"" + idStr + "\" class=\"poll_drawDetail_contsdetail \">\
								<div class=\"drawDetailviewWrap\">\
									<a href=\"\" title=\"close\" class=\"drawDetailview\">\
										<img src=\"" + filePath + "\" alt=\"\" style=\"max-width:"+screenWidth+"px; max-height:"+screenHeight+"px;\" onerror=\"this.src='"+errorImg+"'\" \">\
									</a>\
								</div>\
							</div>";
				
				$("#wrap").append(htmlStr);
				
				$("#" + idStr + " a.drawDetailview").bind("click", function(e){
					e.preventDefault();
					$("#" + idStr).remove();
				});
			}
		},

		
		/**
		 * PollCtrl.UI.renderUserPollFile
		 *  - 유저들이 답변한 판서 답안을 이미지 형태로 공유할 때 호출하는 함수
		 *  - 2016.12.20 deprecated..
		 *  @deprecated
		 */
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
		
		
		/**
		 * PollCtrl.UI.renderHandWritingResult : 판서 질문 결과화면을 보여주는 함수
		 * @deprecated
		 */
		renderHandWritingResult : function(pollNo, totalCnt, answerCnt, answerList, isMaster) {
			
			var containerIdStr = "poll_result_hand_writing";
			var container = document.getElementById(containerIdStr);
			
			if(container) {
				PollCtrl.UI.open(containerIdStr);
			} else {
				var htmlStr = "<div id=\"poll_result_hand_writing\" class=\"poll_box2\">\
							<span class=\"poll_header\">\
								<span class=\"pop_tit\">" + _msg("poll.write.result.header") + "</span>\
								<a onclick=\"javascript:PollCtrl.UI.close('" + containerIdStr + "');\"></a>\
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
						thumbnail.src = thumbnailSrc == "" ? Utils.addResPath("images", "thum_user.png") : thumbnailSrc;
						
					var userNameSpan = document.createElement("span");
						userNameSpan.innerHTML = userNm;
					
					userAnchor.appendChild(thumbnail);	
					userAnchor.appendChild(userNameSpan);
					
					$(answerListEl).append(userAnchor);
				}
			}
		},
		
		
		
		/**
		 * PollCtrl.UI.renderPollSheet
		 *  - 질문 답안 선택 창을 띄우는 함수
		 */
		renderPollSheet : function(resultMap, isShow) {
			Utils.log("[renderPollSheet] paramData : " + JSON.stringify(resultMap));
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
			
			if (pollType == '0') {
				PollCtrl.UI.open("multi_choice_sheet");
				$("#multi_choice_sheet .pop_tit").text(_msg("poll.text.sheet.title"));
				$("#multi_choice_sheet .question_text").text(pollTitle);
				
				var container = $("#multi_choice_item");
				container.empty();
				var itemInput = allowCnt > 1 ? "checkbox" : "radio"; 
				var len = itemList.length;
				
				for (var i=0; i<len; i++) {
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
				
			} else if (pollType == '1') {
				PollCtrl.UI.open("alternative_sheet");
				$("#alternative_sheet .pop_tit").text(_msg("poll.text.sheet.title"));
				$("#alternative_sheet .question_text").text(pollTitle);
				
				var container = $("#alternative_item");
				container.empty();
				var len = itemList.length;
				for (var i=0; i<len; i++) {
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
				
			} else if (pollType == '2') {
				if (pollCategory == '1') {   // 서술형 답변 창
					PollCtrl.UI.open("text_answer_sheet");
					
					$("#text_answer_sheet .pop_tit").text(_msg("poll.text.sheet.title"));
					$("#text_answer_sheet .question_text").text(pollTitle);
					
					$("#text_answer_sheet").append($(pollNoHiddenEl));
					$(".answer_text", "#text_answer_sheet").val("");
				} else {
					PollCtrl.UI.open("short_answer_sheet");
					
					$("#short_answer_sheet .pop_tit").text(_msg("poll.text.sheet.title"));
					$("#short_answer_sheet .question_text").text(pollTitle);
					
					$("#short_answer_sheet").append($(pollNoHiddenEl));
					$(".answer_short", "#short_answer_sheet").val("");
				}
				
				
			} else if (pollType == '3') {
				
				/*
				// 2016.12.20 - deprecated..
				PollCtrl.UI.open("hand_writing_sheet");
				
				console.log("판서형 렌더링");
				$("#hand_writing_sheet .pop_tit").text(pollTitle);
				$("#hand_writing_sheet .question_text").text(pollTitle);
				// TODO - Canvas initialize..
				var canvasWrapper = "#answer_canvas";
				$("#answer_canvas").empty();
				
				var app = new CustomBoard();
				PollCtrl.drawingTool = app.init(canvasWrapper, 0, 0);
				
				
				
				PollCtrl.Event.bindClickEvent();
				PollCtrl.UI.Cursor.change("hpen");
				$("#hand_writing_sheet").append($(pollNoHiddenEl));
				*/
				
				PollCtrl.UI.open("drawing_answer_sheet");
				if (typeof(isShow) != "undefined" && isShow == false) {
					$("#drawing_answer_sheet").hide();
				}
				if (typeof(isShow) != "undefined" && isShow == true) {
					$("#drawing_answer_sheet").show();
				}
				
				PollCtrl.UI.drawingContainerMode = "answer";
				PollCtrl.UI.captureCanvas();
				
				$("#drawing_answer_sheet span.question_text").text(pollTitle);
				$("#drawing_answer_sheet").append($(pollNoHiddenEl));
			}
		},
		
		
		
		/**
		 * 폴 상세보기 화면을 그려주는 함수..
		 */
		renderPollUpdateFrm : function(pollInfo) {
			var pollNo    = pollInfo.polltempno;
			var pollType  = pollInfo.polltype;
			var pollTitle = pollInfo.title;
			var itemList  = pollInfo.itemlist;
			var allowCnt  = parseInt(pollInfo.allowcnt);
			
			$("#poll_info_box .poll_tab").addClass("poll_tab_length3");
			$("#poll_info_box .tab_draw").hide();
			
			
			$("#poll_title").val(pollTitle);
			
			$("#poll_update_btn").prop("href", "javascript:PollCtrl.Action.Master.updatePoll('" + pollNo + "', false);");
			$("#poll_remove_btn").prop("href", "javascript:PollCtrl.Action.Master.deletePoll('" + pollNo + "');");
			$("#poll_send_btn").prop("href", "javascript:PollCtrl.Action.Master.updatePoll('" + pollNo + "', true);");
			
			PollCtrl.UI.toggleItemForm(pollType);
			
			$(".poll_btn", "#poll_info_box").addClass("edit_mode");
			$("#poll_update_btn").show();
			$("#poll_remove_btn").show();
			
			$("#poll_save_btn").hide();
			
			$(".poll_tab > a").each(function(){
				$(this).removeClass("on");
			});
			
			if (pollType == '0') {
				$(".poll_tab > a.tab_multi").addClass("on");
			} else if (pollType == '1') {
				$(".poll_tab > a.tab_ox").addClass("on");
			} else if (pollType == '2') {
				$(".poll_tab > a.tab_short").addClass("on");
			} 
			
			if (allowCnt > 1) {
				//$("#dup_choice_allow").prop("checked", true)
				$("#dup_choice_allow").addClass("on");
			} else {
				//$("#dup_choice_allow").prop("checked", false)
				if($("#dup_choice_allow").hasClass("on"))
					$("#dup_choice_allow").removeClass("on");
			}
			
			
			
			var container = pollType == '0' ? $("#multichoice_box") :
				pollType == '1' ? $("#alternative_box .alternative_direct") : $("#short_box");
			
			container.empty();
			
			if (container.css("display") == "none") {
				container.css("display", "block");
			}
			
			var len = itemList.length;
			if (pollType == '0' || pollType == '1') {
	 			for (var i=0; i<len; i++) {
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
			
			if (pollType == '0' && len > 4) {
				$(".btn_delexam").show();
			}
		},
		

		
		/**
		 * PollCtrl.UI.renderPollList
		 *  - 저장된 폴 템플릿의 리스트 화면을 보여주는 함수
		 */
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
					var pollIdx = ((pageNum-1) * PollCtrl.UI.ROWS_PER_PAGE) + (i+1);
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
					
					/*var spanEl = document.createElement("span");
					spanEl.innerHTML = "Q" + pollIdx + ".";*/
					
					var labelEl = document.createElement("label");
					labelEl.htmlFor = "q" + pollIdx;
					labelEl.innerHTML = pollTitle;
					labelEl.setAttribute("onclick", "PollCtrl.Action.Master.makePollUpdateFrm('" + pollTempNo + "')");
					
					listEl.appendChild(inputEl);
					//listEl.appendChild(spanEl);
					listEl.appendChild(labelEl);
					
					container.append($(listEl));
				}
				
				PollCtrl.UI.renderPager("TMP_POLL", pageNum, pollTotalCnt);
				
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
					
					PollCtrl.UI.renderPager("POLL", pageNum, pollTotalCnt);
					
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
		
		
		/**
		 * PollCtrl.UI.animateProgress
		 *  - 선형 프로그레스 애니메이터
		 *  - 파라미터를 보내지 않으면 진행중인 animate를 멈춘다..
		 */
		animateProgress : function(totalMs) {
			$("#poll_progress").css("width", "100%").animate({width : 0},
				{
					duration : totalMs + 1200,
					step : function(progress) {
						$(this).css("width", progress + "%");
					}
				}
			);
			
			if (typeof totalMs == 'undefined') {
				$("#poll_progress").stop();
			}
		},
		

		/**
		 * PollCtrl.UI.animateStopWatchSmooth
		 *  - 원형 프로그레스 애니메이터
		 *  - 2016.12.20 deprecated..
		 * @deprecated Use animateProgress()
		 */
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
		
		
		/**
		 * 스톱워치 애니메이션..
		 * @deprecated Use animateProgress()
		 */
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
		
		
		/**
		 * PollCtrl.UI.toggleAlternativePollFrm
		 *  - 양자택일의 유형에 따라 적절한 UI를 보여주는 함수
		 */
		toggleAlternativePollFrm : function(tmpType) {
			var item_1 = $(".alternative_direct input").get(0);
			var item_2 = $(".alternative_direct input").get(1);
			item_1.readOnly = true;
			item_2.readOnly = true;
			$(item_1).css("background", "rgba(110, 110, 110, 0.6)");
			$(item_2).css("background", "rgba(110, 110, 110, 0.6)");
			
			if(tmpType == '') {
				$(item_1).parent().attr("style", "display:none;");
			} else if(tmpType == '0') {
				$(item_1).parent().attr("style", "display:block;");
				item_1.value = "A";
				item_2.value = "B";
			} else if(tmpType == '1') {
				$(item_1).parent().attr("style", "display:block;");
				item_1.value = "O";
				item_2.value = "X";
			} else if(tmpType == '2') {
				$(item_1).parent().attr("style", "display:block;");
				item_1.value = _msg("poll.answer.alter.agree");
				item_2.value = _msg("poll.answer.alter.disagree");;
			} else if(tmpType == '3') {
				$(item_1).parent().attr("style", "display:block;");
				item_1.value = "";
				item_2.value = "";
				item_1.readOnly = false;
				item_2.readOnly = false;
				$(item_1).css("background", "rgba(245,245,245,0.6)");
				$(item_2).css("background", "rgba(245,245,245,0.6)");
			}
		},
		
		// 폴 창 초기화
		clearPollMakeForm : function() {
			
			$(".poll_tab > a").each(function(){
					$(this).removeClass("on");
			});
			$(".poll_tab > a.tab_multi").addClass("on");
			
			$(".poll_btn", "#poll_info_box").removeClass("edit_mode");
			
			$("#poll_save_btn").show();
			$("#poll_update_btn").hide();
			$("#poll_remove_btn").hide();
			
			//$("#poll_send_btn").prop("href", "");
			//$("#poll_update_btn").prop("href", "");
			
			PollCtrl.UI.toggleItemForm('0');
			
			$("#dup_choice_allow").removeClass("on");
			
			$("#poll_title").val("");
			$("#question_title").val("");
			var cnt = 0;
			$("#item_rows :text").each(function(){
				$(this).val("");
			});
			
			$("#multichoice_box").empty();
			var htmlStr = "<span>1.</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
						<span>2.</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
						<span>3.</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>\
						<span>4.</span><input type=\"text\" placeholder=\"" + _msg("poll.answer.input") + "\"/>";
			$("#multichoice_box").append(htmlStr);
			
			$(".btn_delexam").hide();
			
			$("#alternative_box :text").each(function(){
				$(this).val("");
			})
			$("#shutdown_time option:eq(0)").prop("selected", "selected");
			$("#question_shutdown_time option:eq(0)").prop("selected", "selected");
			
			$("#alternative_box option:eq('')").prop("selected", true);
			
			$("#custom_time").css("display", "none");
			$("#custom_time").val("");
			
			$("#poll_info_box input[name='poll_type']:eq('0')").prop("checked", "checked");
			//PollCtrl.UI.open("multichoice_box");
			//PollCtrl.UI.close("alternative_box");
			$("#multichoice_box").show();
			$("#alternative_box").hide();
			
			$("#poll_send_btn").prop("href", "javascript:PollCtrl.Action.Master.createPoll(true);");
			
			// 판서형 질문 UI 초기화
			$("#qusbinary").val("");
			$("#full_capture").removeClass("off");
			$("#select_capture").removeClass("on");
			$("#select_capture").removeClass("off");
			$("#full_capture").addClass("on");
			
			$("#full_screen_thumb").css("background-image", "none");
			$("#selection_thumb").css("background-image", "none");
			$("#btn_del_img").hide();
			
			Utils.log("PacketMgr.isParentCreator : " + PacketMgr.isParentCreator);
			if (PacketMgr.isParentCreator) {
				$(".question_to_all").addClass("on")
				$(".question_to_teacher").removeClass("on")
			} else {
				$(".question_to_all").removeClass("on")
				$(".question_to_teacher").addClass("on")
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

		/**
		 * PollCtrl.UI.captureCanvas
		 *  - 판서형 질문에서 영역 선택으로 캔버스 화면을 캡쳐하고자 할 때 호출하는 함수
		 */
		captureCanvas : function (startX, startY, sWidth, sHeight, density) {
			var isFullScreen = (typeof startX == 'undefined' || typeof startY == 'undefined' || typeof sWidth == 'undefined' || typeof sHeight == 'undefined') ? true : false;
			console.log("startX : " + startX + ", startY : " + startY + ", sWidth : " + sWidth + ", sHeight : " + sHeight);
			console.log("hmenubar height : " + $("#hmenubar").height());
			var topMenuBarHeight = $("#hmenubar").height();
			 
			try {
				if (UI.boards == null)
					return;
				
				var board = UI.getBoard();
				if (!isFullScreen) {
					startY = startY - topMenuBarHeight;  // Y좌표값은 상단 메뉴바가 차지하는 height 만큼 빼서 계산함
					//if (startY < 0) startY = 0;
					board.saveCapture(startX/density, startY/density, sWidth/density, sHeight/density);
				} else {
					board.saveCapture(startX, startY, sWidth, sHeight);
				}
				
				
				var saveCanvas = $("#saveCanvas").get(0);
				if (saveCanvas){
					// make represent thumbnail
					//console.log("Captured canvas : " + saveCanvas.toDataURL());

					if(Utils.cordova()) {
						var binary = saveCanvas.toDataURL().split(',')[1];
						var param = {
							img : binary,
							isfullscreen : isFullScreen
						};
						
						cordova.exec(function(result) {
							console.log("cordova.exec() success.. confirmCapture");
							$(saveCanvas).remove();
						}, function(result) {
							alert('confirmCapture err : ' + JSON.stringify(result));
						}, "PollPlugin", "confirmCapture", [param]);
					} else {
						
						if(!Utils.browser("msie")) {
							var binary = saveCanvas.toDataURL();
							if (isFullScreen) {
								if (PollCtrl.UI.drawingContainerMode == "question") {
									$("#full_screen_thumb").css("background-image", "url(" + binary + ")");
								} else if (PollCtrl.UI.drawingContainerMode == "answer") {
									$("#answer_full_capture > span.poll_thumb").css("background-image", "url(" + binary + ")");
								}
							} else {
								if (PollCtrl.UI.drawingContainerMode == "question") {
									$("#selection_thumb").css("background-image", "url(" + binary + ")");
									$("#btn_del_img").show();
								} else if (PollCtrl.UI.drawingContainerMode == "answer") {
									$("#answer_select_capture > span.poll_thumb").css("background-image", "url(" + binary + ")");
									$("#answer_select_capture > span.poll_thumb > a.btn_del_img").show();
								}
							}
							
							$("#qusbinary").val(binary.split(',')[1]);
						}
						
					}
					$(saveCanvas).remove();
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
		 * PollCtrl.UI.selectCaptureArea
		 *  - 영역 캡쳐 모드로 전환..
		 */
		finalX : 0,
		finalY : 0,
		finalWidth : 0,
		finalHeight : 0,
		isMouse : false,
		selectCaptureArea : function() {
			Utils.log("[selectCaptureArea]");
			$("#poll_info_box").hide();
			
			PollCtrl.UI.open('selection_capture_container');
			$("#selection_capture_container").width($("#wrap").width());
			$("#selection_capture_container").height($("#wrap").height());
			
			$("#select_area").show();
			
			//disableScroll();
			
			var initX = 0;
			var initY = 0;
			
			var initPosition = [];
			var diffX;
			var diffY;
			
			$("#cancel_capture").bind("click", function(e) {
				e.preventDefault();
				
				enableScroll();
				PollCtrl.UI.finishCapture();
				$("#poll_info_box").show();
			});
			
			$("#confirm_capture").bind("click", function(e) {
				e.preventDefault();
				
				enableScroll();
				var sx = parseInt($("#select_area").css("left").replace("px", ""));
				var sy = parseInt($("#select_area").css("top").replace("px", ""));
				var swidth = parseInt($("#select_area").css("width").replace("px", ""));
				var sheight = parseInt($("#select_area").css("height").replace("px", ""));
				
				Utils.log("[confirm_capture] 선택 영역 width : " + $("#select_area").css("width"));
				Utils.log("[confirm_capture] 선택 영역 height : " + $("#select_area").css("height"));
				
				if (PollCtrl.UI.drawingContainerMode == "question") {
					$("#poll_info_box").show();
				} else {
					$("#drawing_answer_sheet").show();
				}
				
				sy = sy - $("#" + UI.HEADER).height();
				
				
				//PollCtrl.UI.captureCanvas(sx, sy, swidth, sheight, 1);
				PollCtrl.UI.captureCanvas(PollCtrl.UI.finalX, PollCtrl.UI.finalY, PollCtrl.UI.finalWidth, PollCtrl.UI.finalHeight, 1);
				PollCtrl.UI.finishCapture();
			});
			
			
			var scrollTop = 0;
			var captureContainer = document.getElementById("selection_capture_container");
			
			$("#selection_capture_container").bind("touchstart", function(ev) {
				if (ev.target.id == "confirm_capture") return;
				
				$(captureContainer).css("cursor", "crosshair");
				
				disableScroll();
				
				var position = getPoint(ev);
				initPosition = position;
				scrollTop = $(document).scrollTop();
				
				ev.preventDefault();
				console.log("[mousedown] x : " + position[0] + ", y : " + position[1]);
			}).bind("mouseenter", function(ev){
				$(captureContainer).css("cursor", "crosshair");		
				
			}).bind("mousedown", function(ev) {
				if (ev.target.id == "confirm_capture" ||  ev.target.id == "cancel_capture") return;
				
				$(captureContainer).css("cursor", "crosshair");
				PollCtrl.UI.isMouse = true;
				disableScroll();
				
				var position = getPoint(ev);
				initPosition = position;
				scrollTop = $(document).scrollTop();
				
                ev.stopPropagation();
				ev.preventDefault();
				console.log("[mousedown] x : " + position[0] + ", y : " + position[1]);
			}).bind("mousemove", function(ev) {
				if (ev.target.id == "confirm_capture" ||  ev.target.id == "cancel_capture") return;
				if (!PollCtrl.UI.isMouse) return;
				var position = getPoint(ev);
				//console.log("[move] x : " + position[0] + ", y : " + position[1]);
				scrollTop = $(document).scrollTop();
				
				diffX = initPosition[0] - position[0];
                diffY = initPosition[1] - position[1];
                
                var sx = initPosition[0] > position[0] ? position[0] : initPosition[0];
                var sy = initPosition[1] > position[1] ? position[1] : initPosition[1];
                var width = Math.abs(diffX);
                var height = Math.abs(diffY);
                
                /*
                if (width <= 110 && height <= 60) {
                	
                	$("#select_area").css("left", 0);
                    $("#select_area").css("top", 0);
                    $("#select_area").css("width", 0);
                    $("#select_area").css("height", 0);
                    
                } else {
	                $("#select_area").css("left", sx);
	                $("#select_area").css("top", scrollTop > 0 ? sy - scrollTop : sy);
	                $("#select_area").css("width", width);
	                $("#select_area").css("height", height);
                }
                */
                
                $("#select_area").css("left", sx);
                $("#select_area").css("top", scrollTop > 0 ? sy - scrollTop : sy);
                $("#select_area").css("width", width);
                $("#select_area").css("height", height);
                
                ev.stopPropagation();
                ev.preventDefault();
                console.log("[mousemove]");
			}).bind("mouseup", function(ev) {
				if (ev.target.id == "confirm_capture" ||  ev.target.id == "cancel_capture") return;
				if (!PollCtrl.UI.isMouse) return;
				
				$(captureContainer).css("cursor", "default");
				
				PollCtrl.UI.isMouse = false;
				var position = getPoint(ev);
				
				scrollTop = $(document).scrollTop();
				
				diffX = initPosition[0] - position[0];
                diffY = initPosition[1] - position[1];
                
                var sx = initPosition[0] > position[0] ? position[0] : initPosition[0];
                var sy = initPosition[1] > position[1] ? position[1] : initPosition[1];
                var width = Math.abs(diffX);
                var height = Math.abs(diffY);
                
                if (width <= 110 && height <= 60) {
                	Ctrl.Msg.show(_msg("poll.draw.sel.area.lag"));
                	$("#select_area").css("left", 0);
                    $("#select_area").css("top", 0);
                    $("#select_area").css("width", 0);
                    $("#select_area").css("height", 0);
                    return;
                }
                
                $("#select_area").css("left", sx);
                $("#select_area").css("top", scrollTop > 0 ? sy - scrollTop : sy);
                $("#select_area").css("width", width);
                $("#select_area").css("height", height);
                
                
                PollCtrl.UI.finalX = initPosition[0] > position[0] ? position[0] : initPosition[0];
                PollCtrl.UI.finalY = initPosition[1] > position[1] ? position[1] : initPosition[1];
                PollCtrl.UI.finalWidth = width;
                PollCtrl.UI.finalHeight = height;
                
                ev.preventDefault();
                console.log("[mouseup] sx : " + PollCtrl.UI.finalX + ", sy : " + PollCtrl.UI.finalY + ", swidth : " + PollCtrl.UI.finalWidth + ", sheight : " + PollCtrl.UI.finalHeight);
			});
		},
		
		
		/**
		 * PollCtrl.UI.finishCapture
		 *  - 영역 캡쳐가 완료되었을 때 호출하는 함수
		 */
		finishCapture : function() {
			Utils.log("[finishCapture]");
			//enableScroll();
			$("#confirm_capture").unbind("click");
			$("#selection_capture_container").unbind("touchstart");
			$("#selection_capture_container").unbind("mousemove");
			$("#selection_capture_container").unbind("mousedown");
			$("#selection_capture_container").unbind("mouseup");
			$("#selection_capture_container").unbind("mouseout");
			
			$("#select_area").css("left", "").css("top", "").css("width", "").css("height", "");
			$("#select_area").hide();
			PollCtrl.UI.close('selection_capture_container');
		},
		
		
		/**
		 * PollCtrl.UI.renderAnswerUserList
		 *  - 질문을 받은 사용자들의 썸네일을 프로그레스 화면에 출력해주는 함수
		 */
		renderAnswerUserList : function(pollType) {
			var len = Ctrl.Member.classList.length;
			
			for (var i=0; i<len; i++) {
				var user = Ctrl.Member.classList[i];
				console.log(user);
				var userNo = user.userno;
				var userId = user.userid;
				var userNm = user.usernm;
				var userThumb = user.thumbnail;
				var userType = user.usertype;
				
				if(userNo == PacketMgr.userno)
					continue;
				if(pollType == "3" && userId == userNo)  // 게스트는 판서 답변 대상에서 뺀다..
					continue;
				
				var htmlStr = "<div id=\"poll_" + userNo + "\" attr=\"" + userNo + "\" class=\"stay_user wait\" title=\"" + userNm + "\">\
								<span class=\"state\"></span>\
								<img src=\"" + userThumb + "\" alt=\"\">\
							</div>";
				$("#stay_user_list").append(htmlStr);
			}
		},
		
		
		
		/**
		 * PollCtrl.UI.updateAnswerUser
		 * - 답변 완료한 유저에 대해 UI 처리..
		 */
		updateAnswerUser : function(userNo) {
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
		}
	},
	
	
	
	Util : {
		getItemCnt : function() {
			var cnt = 0;
			var elem = $("#poll_info_box");
			var inputNodes = $("#multichoice_box :text");
			inputNodes.each(function(idx) {
				cnt++;
				//if($(this).val() != "") 
					//cnt++;
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


//left: 37, up: 38, right: 39, down: 40,
//spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
var keys = {37: 1, 38: 1, 39: 1, 40: 1};

function preventDefault(e) {
	e = e || window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;  
}

function preventDefaultForScrollKeys(e) {
	if (keys[e.keyCode]) {
		preventDefault(e);
		return false;
	}
}

function disableScroll() {
	if (window.addEventListener) // older FF
		window.addEventListener('DOMMouseScroll', preventDefault, false);
	window.onwheel = preventDefault; // modern standard
	window.onmousewheel = document.onmousewheel = preventDefault; // older browsers, IE
	window.ontouchmove  = preventDefault; // mobile
	document.onkeydown  = preventDefaultForScrollKeys;
}

function enableScroll() {
	if (window.removeEventListener)
		window.removeEventListener('DOMMouseScroll', preventDefault, false);
	window.onmousewheel = document.onmousewheel = null; 
	window.onwheel = null; 
	window.ontouchmove = null;  
	document.onkeydown = null;  
}


function getPoint(ev) {
	var container = document.getElementById("contsWrapper");
	var drawingEl = document.getElementById("sketch" + UI.current);
	
	var offsets = offset(drawingEl);
	
	var x = -1, y = -1;
	if(ev.type == "touchstart"){
		x = ev.originalEvent.touches[0].pageX;
		y = ev.originalEvent.touches[0].pageY;
			
	}else if(ev.type == "movestart" || ev.type == "move" || ev.type == "moveend" || ev.type == "mouseout" || ev.type == "mousemove" || ev.type == "mousedown" || ev.type == "mouseup"){
		x = ev.pageX;
		y = ev.pageY;
		
	}else if(ev.type == "touchend"){
		x = event.changedTouches[event.changedTouches.length-1].pageX; 
		y = event.changedTouches[event.changedTouches.length-1].pageY;
		
	}
	
	// 점을 찍은경우 x포인트만 한개 더 추가해준다.
//	if(ev.type == "moveend" || ev.type == "touchend"){
//		var isDot = (parseInt(compareX) ==  parseInt(x)) ? true : false;
//		if(isDot) x += 1;	
//	}
	
			
	x - offsets[ 0 ] + ( container.scrollLeft || container.scrollLeft);
	y - offsets[ 1 ] + ( container.scrollTop || container.scrollTop);
	
	return [x, y]
}

function offset(el, relativeToEl) {
    var offset = [ 0, 0 ];
    
    for (var node = el; (relativeToEl ? node != relativeToEl && node : node); node = node.offsetParent) {
		var left = node.offsetLeft;
		var top = node.offsetTop;
		
		/** 2014.10.31 슬라이드 이동후 화면을 그려버리면 left에 잡혀버려서 문제된다. wrap은 skip */
		if(node.id == "wrap") left = 0;
				
    	offset[ 0 ] += left;
        offset[ 1 ] += top;        
    }
        
    return offset;
}

$(document).ready(function() {
	// 판서형 질문을 받고 내 보드로 이동한 케이스일 경우.. 답변창을 띄워주는 함수를 호출한다 - 2017.01.19
	if (sessionStorage.getItem("polldata") != null) {
		PollCtrl.startDrawingPollAnswer(sessionStorage.getItem("polldata"));
	}
});
