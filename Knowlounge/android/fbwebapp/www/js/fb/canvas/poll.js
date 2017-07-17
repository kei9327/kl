var PollCtrl = {
	
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
		this.svrHost = _prop('svr.host.' + svrFlag);
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
					if(!Ctrl._checkAuth(true)) return;
					
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
					
					console.log(param);
					console.log(RoomSvr.userid + " / " + RoomSvr.userno + " / " + RoomSvr.usernm);
					var url = PollCtrl.svrHost + _prop("poll.tmp.add");

					$.ajax({
						type  : 'post',
						url   : url,
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
									console.log(data);
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

				// 판서답변 공유하기
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
						polluserno : pollUserNo
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
			
			// 폴 템플릿 생성하기 (true : 생성과 동시에 send / false : 생성만)
			createPoll : function(params) {

				var pollTitle = params.polltitle;
				var pollType = params.polltype;
				var isDupChoice = params.isdupchoice;
				var sendFlag = params.sendflag;
				var shutdownTime = params.shutdown;
				var itemStr = params.itemstr;
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
				
				var allowCnt = isDupChoice == true ? PollCtrl.Util.getItemCnt() : 1;
				
				// pollType == 2일땐 itemStr을 공백으로 보냄..
				// 항목을 입력하지 않으면 자동으로 인덱스값을 세팅함..

				// 예외처리 구간..
				if(!Ctrl._checkAuth(true)) return;
				
				if(sendFlag && Ctrl.Member.list.length <= 1) {
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
					guesttype     : guestType
					//email : userId,
					
				};
				
				Utils.log(param);
				Utils.log(RoomSvr.userid + " / " + RoomSvr.userno + " / " + RoomSvr.usernm);

				var url = PollCtrl.svrHost + _prop("poll.tmp.add");
				$.ajax({
					type  : 'post',
					url   : url,
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						console.log("success.." + JSON.stringify(data));
						if(data.result == '0') {
							Ctrl.Msg.show(_msg("poll.alert.add"));
							PollCtrl.UI.close('poll_info_box');
							if(data.polltempno != null) {
								var pollTempNo = data.polltempno;
								PollCtrl.Action.Master.makePollList(1);
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
			
			/*
			 폴 템플릿 정보 업데이트
			 @param
			 	- pollTempNo : 폴 템플릿 넘버 (UUID)
			 	- sendFlag   : 업데이트 후 바로 전송 여부 (true / false)
			 */
			updatePoll : function(pollTempNo, sendFlag) {
				//var shutdownTime = $("#shutdown_time").val() * 1000;  // 밀리세컨드 단위
				var shutdownTime = $("#shutdown_time").val() == "custom" ? 
						parseInt($("#custom_time").val()) : $("#shutdown_time").val();
				
				var curDate = new Date();
				var curMs = curDate.getTime();
				var endMs = curMs + shutdownTime;
				var endDate = new Date(endMs);
				
				var pollKey = RoomSvr.userno;   // Freeboard는 poll key값이 userno..
				var pollTitle =	$("#poll_title").val();
				var pollType = $(":radio[name='poll_type']:checked").val();
				var isDupChoice = $("#dup_choice_allow").is(":checked");
				
				var userKey   = RoomSvr.userno;
				var userId    = RoomSvr.userid;
				var userNm    = RoomSvr.usernm;
				var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
				
				var allowCnt = isDupChoice == true ? PollCtrl.Util.getItemCnt() : 1;
				var itemStr = 
					pollType == '0' ? $("#multichoice_box :text").map(function(){if(this.value != "") return this.value;}).get().join("|") : 
						pollType == '1' ? $("#alternative_box :text").map(function(){if(this.value != "") return this.value;}).get().join("|") : 
							pollType == '2' ? $("#short_box textarea").val() : '';
				
				// 예외처리 구간..	
				if(!Ctrl._checkAuth(true)) return;
				
				if(pollTempNo == '' || pollTempNo == undefined) {
					Ctrl.Msg.show(_msg("poll.alert.error"));
					return;
				}
				if(pollType != '2'){
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

				var url = PollCtrl.svrHost + _prop("poll.tmp.update");
				$.ajax({
					type : 'post',
					url : url,
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("success.." + data);

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
					complete : function(){ Utils.log("complete..") } 
				});
			},
			
			// 폴 템플릿 삭제
			deletePoll : function(pollTempNo) {
				
				// 예외처리 구간..
				if(!Ctrl._checkAuth(true)) return;
				
				var param = { polltempno : pollTempNo };
				var url = PollCtrl.svrHost + _prop("poll.tmp.remove");
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
							Ctrl.Msg.show(_msg("poll.alert.tmp_del.success"));
							PollCtrl.UI.close("poll_info_box");
							PollCtrl.Action.Master.makePollList(1);
						} else {
							Ctrl.Msg.show(_msg("poll.alert.tmp_del.fail"));
							Utils.log(data);
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){ Utils.log("complete..") } 
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

				var url = PollCtrl.svrHost + _prop("poll.remove");
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
							PollCtrl.UI.close("poll_complete_list_box");
							PollCtrl.Action.Master.makePollCompleteList(1);
						} else {
							Ctrl.Msg.show(_msg("poll.alert.delete.fail"));
							Utils.log(data);
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){ Utils.log("complete..") } 
				});
				
			},
			
			
			/*
			 @param
			 	- pollTempNo : 폴 템플릿 넘버 (UUID)
			 	- shutdown : 제한 시간 (단위 : 초)
			 */
			sendPoll : function(pollTempNo, shutdown) {
				PollCtrl.UI.close('poll_list_box');
				PollCtrl.UI.close('poll_info_box');
				PollCtrl.UI.close('question_info_box');
				
				$(".poll_count").show();
				
				if(!Ctrl._checkAuth(true)) return;
				
				if(Ctrl.Member.list.length <= 1) {
					Ctrl.Msg.show(_msg("poll.alert.no.participant"));
					return;
				}
				
				var param = { polltempno : pollTempNo };
				var paramForCopy;
				var isCountdown = true;

				var url = PollCtrl.svrHost + _prop("poll.tmp.get");
				$.ajax({
					type       : 'post',
					url        : url,
					async      : true,
					cache      : false,
					dataType   : 'json',
					data       : param,
					beforeSend : function(){},
					success    : function(data) {
						Utils.log("[sendPoll] success.." + JSON.stringify(data));
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

							var url = PollCtrl.svrHost + _prop("poll.tmp.copy");
							$.ajax({
								type       : 'post',
								url        : url,
								async      : true,
								cache      : false,
								dataType   : 'json',
								data       : paramForCopy,
								beforeSend : function(){},
								success    : function(data) {
									Utils.log("success.." + JSON.stringify(data));
									if(data.result == '0') {
										Ctrl.Msg.show(_msg("poll.alert.send.success"));
										var newPollNo = data.pollno;
										
										PollCtrl.isProgress   = true;
										PollCtrl.progressPoll = newPollNo;
										
										if(shutdown == undefined) shutdown = 30; // 시간 지정 안된 경우는 디폴트 30초로
										if(shutdown == 0) isCountdown = false;   // 시간 제한 없을 경우..
										
										Utils.log("[sendPoll] isCountdown = ", isCountdown);
										
										var shutdownMs = shutdown * 1000;
										var displayTime  = shutdown == 0 ? "" : shutdown;
										
										console.log("[sendPoll] shutdownMs(ms) = " + shutdownMs);
										console.log("[sendPoll] displayTime(sec) = " + displayTime);
										console.log("[sendPoll] isCountdown  = " + isCountdown);
										
										// 시간제한은 지정안할시에 기본 30초
//										if(shutdownTime == "" || isNaN(shutdownTime))
//											shutdownTime = 30;
										
										PollCtrl.UI.open("poll_timer_box");
										$("#timer").text(displayTime);
										$("#poll_stop_btn").prop("href", "javascript:PollCtrl.Action.Master.stopPoll('" + newPollNo + "')");
										
										if(isCountdown) {
											PollCtrl.timer = setInterval(function() {
												$("#timer").text(displayTime--);
//												PollCtrl.UI.animateStopWatch(++time, shutdownTime)
												if (displayTime < 0) {
													PollCtrl.UI.close("poll_timer_box");
													PollCtrl.Action.Master.finishPoll(newPollNo);
										        }
											}, 1000);
											PollCtrl.UI.animateStopWathSmooth(shutdownMs);
										} else {
											$(".poll_count").hide();
										}
										
										PacketMgr.Master.poll('start', newPollNo, shutdownMs, isCountdown);
										
										
									} else {
										Ctrl.Msg.show(_msg("poll.alert.send.fail"));
									}
								},
								error      : function(){ Utils.log("error..") },
								complete   : function(){ Utils.log("complete..") } 
							});
						}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){ Utils.log("complete..") } 
				});
				
			},
			
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
			
			sendPollReport : function(pollNo) {
				PacketMgr.Master.poll('report', pollNo, '', '');
			},
			
			/*
			 진행자가 폴 종료 시키기
			 @param
			  - pollNo : 폴 넘버 (UUID)
			 */
			finishPoll : function(pollNo) {
				PollCtrl.isProgress = true;
				PollCtrl.progressPoll = null;
				
				Ctrl.Msg.show(_msg("poll.alert.finish"));
				clearInterval(PollCtrl.timer);
				
				PollCtrl.UI.close("poll_timer_box");
				$("#rotateLeft").hide();
				$("#rotateRight").hide();
				$("#rotateLeft").css({ WebkitTransform: 'rotate(0deg)'});
				$("#rotateLeft").css({ '-moz-transform': 'rotate(0deg)'});
				
				PollCtrl.UI.open('poll_result_box');
				PollCtrl.Action.Common.makePollResult(pollNo);
			},
			
			stopPoll : function(pollNo) {   // 진행자 파트
				Ctrl.Modal.confirm(_msg("poll.confirm.stop"), function() {
					PacketMgr.Master.poll('interrupt', pollNo, '', '');
					PollCtrl.Action.Master.finishPoll(pollNo);
				});
//				PacketMgr.Master.poll('interrupt', pollNo, '');
//				PollCtrl.Action.Master.finishPoll(pollNo);
			},
			
			showPollResult : function(pollNo) {
				PollCtrl.UI.close('poll_complete_list_box');
				PollCtrl.UI.open('poll_result_box');
				PollCtrl.Action.Common.makePollResult(pollNo);
			},
			
			// 폴 수정 창 열기
			makePollUpdateFrm : function(pollTempNo) {
				var param = { polltempno : pollTempNo };

				var url = PollCtrl.svrHost + _prop("poll.tmp.get");

				$.ajax({
					type       : 'post',
					url        : url,
					async      : true,
					cache      : false,
					dataType   : 'json',
					data       : param,
					beforeSend : function(){},
					success    : function(data) {
						Utils.log("success.." + data);
						if(data.result == '0') {
							var resultMap = data.map;
							console.log("makePollUpdateFrm : " + JSON.stringify(resultMap));
							PollCtrl.UI.close("poll_list_box");
							PollCtrl.UI.open("poll_info_box");
							PollCtrl.UI.renderPollUpdateFrm(resultMap);
						}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){ Utils.log("complete..") } 
				});
			},
			
			// 폴 리스트 받아오기
			makePollList : function(pollKey, pageNum) {
				//PollCtrl.UI.open("poll_list_box");
				//var pollKey = RoomSvr.userno;
				var param = {
					pollkey   : pollKey,
					rows : PollCtrl.UI.ROWS_PER_PAGE,
					pageno : pageNum
				};

				var url = PollCtrl.svrHost + _prop("poll.tmp.list");

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
							//PollCtrl.UI.renderPollList(data, pageNum);

							cordova.exec(function(result) {
								console.log("cordova.exec() success.. getPollTmpList");
							}, function(result) {
								alert('getPollTmpList err : ' + JSON.stringify(result));
							}, "PollPlugin", "getPollTmpList", data);
						}
					},
					error      : function(){ Utils.log("error..") },
					complete   : function(){ Utils.log("complete..") } 
				});
			},
			
			// 완료된 폴 리스트 받아오기
			// PollCtrl.Action.Master.makePollCompleteList(pageNum);
			makePollCompleteList : function(pollKey, pageNum) {
				//PollCtrl.UI.open("poll_complete_list_box");
				//var pollKey = RoomSvr.roomid;
				var param = {
					pollkey : pollKey,
					rows    : PollCtrl.UI.ROWS_PER_PAGE,
					pageno  : pageNum
				};

				var url = PollCtrl.svrHost + _prop("poll.list");
				$.ajax({
					type       : 'post',
					url        : Utils.addContext('poll/list.json'),
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
						}, "PollPlugin", "getCompletePollList", data);

						/*
						if(data.result == '0') {
							PollCtrl.UI.renderCompletedPollList(data, pageNum);
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

				var url = PollCtrl.svrHost + _prop("poll.all.remove");
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
			makePollSheet : function(pollNo, timeLimit, isCountdown) {
				console.log("폴시트를 생성합니다.");
				
				var param = { pollno : pollNo };
				$(".poll_user_count", "#hand_writing_sheet").show();

				var url = PollCtrl.svrHost + _prop("poll.get");
				$.ajax({
					type : 'post',
					url : url,
					async : true,
					cache : false,
					dataType : 'json',
					data : param,
					beforeSend : function(){},
					success : function(data) {
						Utils.log("success..");
						Utils.log(data);
						
						var pollStart = data.map.startdatetime;
						var pollEnd = data.map.enddatetime;
						if(data.result == '0') {
							
							PollCtrl.isProgress = true;
							PollCtrl.progressPoll = pollNo;
							
							var resultMap = data.map;
							var pollType = resultMap.polltype;
							var pollCategory = resultMap.pollcategory;
							var userTimeLimit = timeLimit + 1;  // 1초 딜레이
							
							PollCtrl.UI.renderPollSheet(resultMap);
							console.log("[makePollSheet] isCountdown = " + isCountdown + " " + typeof isCountdown);
							
							if(isCountdown == "true") {
								timeLimit = timeLimit / 1000;
								PollCtrl.timer = setInterval(function() {
									$(".poll_user_count span").text(timeLimit);
									timeLimit--;
									if (timeLimit < 0) {
										if(pollType == '0') {
											PollCtrl.UI.close("multi_choice_sheet");
										} else if(pollType == '1') {
											PollCtrl.UI.close("alternative_sheet");
										} else if(pollType == '2') {
											if(pollCategory == '1') {
												PollCtrl.UI.close("text_answer_sheet");
											} else {
												PollCtrl.UI.close("short_answer_sheet");
											}
										} else if(pollType == '3') {
											PollCtrl.UI.close("hand_writing_sheet");
										} 
										Ctrl.Msg.show(_msg("poll.alert.finish"));
										PollCtrl.Action.Attender.exitPoll(pollNo);
										//PollCtrl.Action.Master.finishPoll(pollNo);
							        }
								}, 1000);
							} else {
								$(".poll_user_count", "#hand_writing_sheet").hide();
							}
							
						}
					},
					error : function(){ Utils.log("error..") },
					complete : function(){ Utils.log("complete..") } 
				});
			},
			
			// 설문 시트 결과 전송
			submitPollResult : function(pollType) {
				var pollNo  = $("#sheetPollNo").val();
				var userKey = RoomSvr.userno;
				var userId  = RoomSvr.userid;
				var userNm  = RoomSvr.usernm;
				var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
				
				var pollItemNo = "";
				var answerTxt = "";
				var answerBin = "";

				if(pollType == '2') {           // 단답형..
					answerTxt = $("#short_answer_sheet .answer_short").val();
					if(answerTxt == "") 
						answerTxt = $("#text_answer_sheet .answer_text").val();
				} else if (pollType == '3') {   // 판서형..
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
					
					//console.log("판서 바이너리 = " + answerBin);
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

				var url = PollCtrl.svrHost + _prop("poll.answer.add");

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
							if(PollCtrl.drawingTool != null) {
								PollCtrl.drawingTool["destroy"]();
								PollCtrl.drawingTool = null;
								PollCtrl.Event.unbindClickEvent();
							}
							Ctrl.Msg.show(_msg("poll.alert.submit"));
							PollCtrl.UI.close("multi_choice_sheet");
							PollCtrl.UI.close("alternative_sheet");
							PollCtrl.UI.close("short_answer_sheet");
							PollCtrl.UI.close("hand_writing_sheet");
							PollCtrl.UI.close("text_answer_sheet");
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
			
			exitPoll : function(pollNo) {   // 참여자 파트
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
				PollCtrl.UI.close("hand_writing_sheet");
				PollCtrl.UI.close("text_answer_sheet");
				
				Ctrl.Msg.show(_msg("poll.alert.stop"));
				clearInterval(PollCtrl.timer);
			},
		},
		
		Common : {
			// 설문 결과 그래프 생성
			makePollResult : function(pollNo) {
				var isMaster = PacketMgr.isMC;
				
				//var pollNo = '334662b6596ke67a146a';
				
				var param = { pollno : pollNo };

				var url = PollCtrl.svrHost + _prop("poll.get");

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
						console.log("makePollResult : " + JSON.stringify(data));
						if(data.result == '0') {
							var resultMap = data.map;
							var pollType = resultMap.polltype;
							if(pollType == '3') {
								// TODO 판서형 폴 결과값 렌더링

								/*
								var url = PollCtrl.svrHost + _prop("poll.answer.list");
								$.ajax({
									type  : 'post',
									url   : url,
									async : true,
									cache : false,
									dataType : 'json',
									data : { pollno : pollNo },
									success : function(data) {
										console.log(data);
										if(data.result == 0) {
											var answerTotalCnt = parseInt(resultMap.answertotalcnt);
											var answerUserCnt  = parseInt(resultMap.answerusercnt);
											var answerList = data.list;
											//PollCtrl.UI.renderHandWritingResult(pollNo, answerTotalCnt, answerUserCnt, answerList, isMaster);
										} else if(data.result == -103) {
											Ctrl.Msg.show(_msg("poll.alert.no.answer"));
										}
									}
								}); */
								
							} else {
								cordova.exec(function(result) {
									console.log("cordova.exec() success.. getCompletePollDetail");
								}, function(result) {
									alert('getCompletePollDetail err : ' + JSON.stringify(result));
								}, "PollPlugin", "getCompletePollDetail", [resultMap]);
							}
							
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

				var url = PollCtrl.svrHost + _prop("poll.file.item.get");

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
			if(type == '0') {
				$(".multichoice_box", elem).css("display", "block");
				$("#item_adddel").css("display", "block");
				$(".alternative_box", elem).css("display", "none");
				$(".short_box", elem).css("display", "none");
			} else if (type == '1') {
				$(".multichoice_box", elem).css("display", "none");
				$("#item_adddel").css("display", "none");
				$(".alternative_box", elem).css("display", "block");
				$(".short_box", elem).css("display", "none");
			} else if (type == '2'){
				$(".multichoice_box", elem).css("display", "none");
				$("#item_adddel").css("display", "none");
				$(".alternative_box", elem).css("display", "none");
				$(".short_box", elem).css("display", "block");
			}
		},
		
		// 항목 열 추가
		addItemRow : function() {
			var itemCnt = $("#item_rows > :text").length;
			var num = itemCnt + 1;
			
			$("#item_rows").append($("<span>" + num + ".</span><input type='text' placeholder='#" + num + " " + _msg("poll.answer.input") + "'/>"));
			$("#multichoice_box").scrollTop($("#multichoice_box").prop("scrollHeight"));
			
			if(num <= 4)
				$("#item_adddel .btn_delexam").css("display", "none");
			else
				$("#item_adddel .btn_delexam").css("display", "block");
			if(num >= 10)
				$("#item_adddel .btn_addexam").css("display", "none");
			else
				$("#item_adddel .btn_addexam").css("display", "block");
			
			
		},
		
		// 항목 열 삭제
		delItemRow : function() {
			$("#item_rows > span").get(-1).remove();
			$("#item_rows > :text").get(-1).remove();
			
			var rows = $("#item_rows > :text").length;
			if(rows <= 4)
				$("#item_adddel .btn_delexam").css("display", "none");
			else
				$("#item_adddel .btn_delexam").css("display", "block");
			if(rows >= 10)
				$("#item_adddel .btn_addexam").css("display", "none");
			else
				$("#item_adddel .btn_addexam").css("display", "block");
				
		},
		
		close : function(idStr) {
			$("#" + idStr).css("display", "none");
		},
		
		open : function(idStr) {
			$(".mail_box").hide();
			
			$(".poll_box").css("display", "none");
			$(".poll_box2").css("display", "none");
			
			if(idStr == 'poll_info_box') {
				if(!Ctrl._checkAuth(true)) return;
				PollCtrl.UI.clearPollMakeForm();
				$("#poll_send_btn").prop("href", "javascript:PollCtrl.Action.Master.createPoll(true);");
				
			} else if(idStr == 'question_info_box') {
				if(!Ctrl._checkAuth(true)) return;
				
				
				var container = document.getElementById(idStr);
				
				if(container) {
					$(container).show();
					PollCtrl.UI.clearPollMakeForm();
					
				} else {
					var htmlStr = "<div id=\"question_info_box\" class=\"poll_box\">\
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
								<a id=\"question_send_btn\" class=\"btn_full\">"+_msg("poll.btn.send")+"</a>\
							</div>\
						</div>\
					</div>"; 
					
					$("#wrap").append(htmlStr);
				}
				$("#question_send_btn").prop("href", "javascript:PollCtrl.Action.Master.Question.createQuestion(true);");
				
			} else if(idStr == 'poll_list_box' || idStr == 'poll_complete_list_box') {
				if(!Ctrl._checkAuth(true)) return;
			}
			$("#" + idStr).css("display", "block");
		},
		
		
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
						<a onclick=\"javascript:PollCtrl.UI.close('" + containerIdStr + "');\"></a>\
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
					PollCtrl.UI.open("poll_result_box_user");
					
					var percentSpan = document.createElement("span");
					percentSpan.className = "value_per";
					percentSpan.innerHTML = parseInt(rate) + "%";
					
					var itemNmSpan = document.createElement("span");
					itemNmSpan.className = "short_value";
					itemNmSpan.innerHTML = itemNm;
					barSpan.appendChild(percentSpan);
					barSpan.appendChild(itemNmSpan);
				} else {
					PollCtrl.UI.open("poll_result_box_admin");
					
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
				PollCtrl.UI.open("multi_choice_sheet");
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
				PollCtrl.UI.open("alternative_sheet");
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
					PollCtrl.UI.open("text_answer_sheet");
					$("#text_answer_sheet .question_text").text(pollTitle);
					$("#text_answer_sheet").append($(pollNoHiddenEl));
					$(".answer_text", "#text_answer_sheet").val("");
				} else {
					PollCtrl.UI.open("short_answer_sheet");
					$("#short_answer_sheet .question_text").text(pollTitle);
					$("#short_answer_sheet").append($(pollNoHiddenEl));
					$(".answer_short", "#short_answer_sheet").val("");
				}
				
				
			} else if(pollType == '3') {
				
				PollCtrl.UI.open("hand_writing_sheet");
				console.log("판서형 렌더링");
				$("#hand_writing_sheet .question_text").text(pollTitle);
				// TODO - Canvas initialize..
				var canvasWrapper = "#answer_canvas";
				$("#answer_canvas").empty();
				
				var app = new CustomBoard();
				PollCtrl.drawingTool = app.init(canvasWrapper, 0, 0);
				
				
				
				PollCtrl.Event.bindClickEvent();
				PollCtrl.UI.Cursor.change("hpen");
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
			
			PollCtrl.UI.toggleItemForm(pollType);
			
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
		
		setAlterPollTemplate : function(tmpType) {
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
			
			$(".poll_btn", "#poll_info_box").removeClass("edit_mode");
			
			$("#poll_save_btn").css("display", "block");
			$("#poll_update_btn").css("display", "none");
			$("#poll_remove_btn").css("display", "none");
			
			$("#poll_send_btn").prop("href", "");
			$("#poll_update_btn").prop("href", "");
			
			$("#poll_title").val("");
			$("#question_title").val("");
			$("#item_rows :text").each(function(){
				$(this).val("");
			});
			
			$("#alternative_box :text").each(function(){
				$(this).val("");
			})
			$("#shutdown_time option:eq(0)").prop("selected", "selected");
			$("#question_shutdown_time option:eq(0)").prop("selected", "selected");
			
			$("#alternative_box option:eq('')").prop("selected", true);
			
			$("#custom_time").css("display", "none");
			$("#custom_time").val("");
			
			$("#poll_info_box input[name='poll_type']:eq('0')").prop("checked", "checked");
			PollCtrl.UI.open("multichoice_box");
			PollCtrl.UI.close("alternative_box");
			
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