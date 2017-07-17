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
        
        var svrFlag = _prop("svr.flag");
        this.svrHost = _prop('svr.host.' + svrFlag);
        alert(this.svrHost);
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
    

	/**
	  PollCtrl.createPoll
	  폴 템플릿 생성하기 (true : 생성과 동시에 send / false : 생성만)
	  params = {
		 title : title,
		 polltype : pollType,
		 isdupchoice : isDupChoice,
		 allowcnt : allowCnt,   // isDupChoice가 true면 아이템갯수를, 아니면 1을
		 sendflag : sendFlag,
		 shutdowntime : shutdownTime,
		 items : itemStr
		}	
	 */
	createPoll : function(params) {
		
		var pollTitle    = params.title;
		var pollType     = params.polltype;
		var allowCnt     = params.allowcnt;
		var sendFlag     = params.sendflag;
		var shutdownTime = params.shutdowntime;
		var itemStr      = params.items;

		var pollCategory = pollType != "3" ? "0" : "1";
		
		var pollKey   = RoomSvr.userno;
		var userKey   = RoomSvr.userno;
		var userId    = RoomSvr.userid;
		var userNm    = RoomSvr.usernm;
		var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트
		
		var curDate = new Date();
		var curMs = curDate.getTime();
		var endMs = curMs + (shutdownTime * 1000);
		var endDate = new Date(endMs);
		
		// pollType == 2일땐 itemStr을 공백으로 보냄..
		// 항목을 입력하지 않으면 자동으로 인덱스값을 세팅함..
		
		// 예외처리 구간..
		if(!Ctrl._checkAuth(true)) return;
		
		if(sendFlag && Ctrl.Member.list.length <= 1) {
			//Ctrl.Msg.show(_msg("poll.alert.no.participant"));
			cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("poll.alert.no.participant")]);
			return;
		}
		
		if(pollTitle == '') {
			Ctrl.Msg.show(_msg("poll.alert.title"));
			cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("not.allow")]);
			return;
		}
		if(pollType != '2' && pollType != '3'){
			if(itemStr == '') {
				//Ctrl.Msg.show(_msg("poll.alert.answer"));
				cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("poll.alert.answer")]);
				return;
			}
		}
		if(isNaN(shutdownTime)) {
			//Ctrl.Msg.show(_msg("poll.alert.timer"));
			cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("poll.alert.timer")]);
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
				console.log("success.." + JSON.stringify(data));
				if(data.result == '0') {
					cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("poll.alert.add")]);
					
					if(data.polltempno != null) {
						var pollTempNo = data.polltempno;
						if(sendFlag) {
							console.log(shutdownTime);
							PollCtrl.sendPoll(pollTempNo, shutdownTime);
						} else {
							PollCtrl.getPollTmpList(1);
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
	  params = {
		  polltempno : pollTempNo,
		  polltype : pollType,
		  allowcnt: allowCnt  // 증복체크여부가 true면 항목갯수, 아니면 1
	  }
	 
	 */
	updatePoll : function(param, sendFlag, shutdownTime) {
		
		var curDate = new Date();
		var curMs = curDate.getTime();
		var endMs = curMs + shutdownTime;
		var endDate = new Date(endMs);
		
		var userId    = RoomSvr.userid;
		var guestType = RoomSvr.userno != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트

		// 예외처리 구간..
		if(!Ctrl._checkAuth(true)) return;
		
		if(param.polltempno == '' || param.polltempno == undefined) {
			Ctrl.Msg.show(_msg("poll.alert.error"));
			return;
		}
		if(param.polltype != '2'){
			if(param.items == '') {
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
		
		param.pollkey       = RoomSvr.userno;
		param.startdatetime = PollCtrl.Util.getDateStr(curDate);
		param.enddatetime   = PollCtrl.Util.getDateStr(endDate);
		param.userkey       = RoomSvr.userno;
		param.usernm        = RoomSvr.usernm;
		param.guesttype     = guestType;
		
		Utils.log(param);
		Utils.log(RoomSvr.userid + " / " + RoomSvr.userno + " / " + RoomSvr.usernm);
		
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
				Utils.log("updatePoll success.." + data);
				if(data.result == '0') {
					Ctrl.Msg.show(_msg("poll.alert.update"));
					if(sendFlag) {
						PollCtrl.sendPoll(pollTempNo, shutdownTime);
					} else {
						PollCtrl.makePollList(1);
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
	deletePollTmp : function(pollTempNo) {
		
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
				Utils.log("success.." + data);
				if(data.result == '0') {
					Ctrl.Msg.show(_msg("poll.alert.tmp_del.success"));
					PollCtrl.getPollTmpList(1);
				} else {
					Ctrl.Msg.show(_msg("poll.alert.tmp_del.fail"));
					console.log(data);
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
					PollCtrl.getCompletePollList(1);
				} else {
					Ctrl.Msg.show(_msg("poll.alert.delete.fail"));
					console.log(data);
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
	 - 템플릿 리스트에서 호출할 경우 shutdown는 디폴트 30으로 설정하고 보내주세요.
	 */
	sendPoll : function(pollTempNo, shutdown) {
		
		if(!Ctrl._checkAuth(true)) {
			return;
			// TODO : 권한이 자동으로 넘어가는데 어떻게 처리할지 고민할 것.
		}
		
		if(Ctrl.Member.list.length <= 1) {
			cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("poll.alert.no.participant")]);
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
					
					var svrFlag = _prop("svr.flag");
					var svrHost = _prop("svr.host." + svrFlag);
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
							Utils.log("success.." + JSON.stringify(data));
							if(data.result == '0') {
								Ctrl.Msg.show(_msg("poll.alert.send.success"));

								var cordovaParams = { pollno : data.pollno };
								cordova.exec(function(result) {
									console.log("cordova.exec() success.. getPollTmpDetail");
								}, function(result) {
									alert('getPollTmpDetail err : ' + JSON.stringify(result));
								}, "PollPlugin", "onReadySendPoll", [cordovaParams]);

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
								
								// TODO : Native에 프로그래스 UI 구성에 필요한 데이터를 넘겨주도록...

								$("#poll_stop_btn").prop("href", "javascript:PollCtrl.Action.Master.stopPoll('" + newPollNo + "')");
								
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

	// 폴 결과 그래프 불러오기
	getCompletePollDetail : function(pollNo) {
		var isMaster = PacketMgr.isMC;
		
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
				console.log("success makePollResult : " + JSON.stringify(data));
				if(data.result == '0') {
					var resultMap = data.map;
					var pollType = resultMap.polltype;
					if(pollType == '3') {
						
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

	// (참여자) 설문 시트 생성
	makePollSheet : function(pollNo, timeLimit, isCountdown) {
		console.log("폴시트를 생성합니다.");
		
		var param = { pollno : pollNo };

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
				Utils.log("success..");
				Utils.log(data);
				
				if(data.result == '0') {
					var cordovaParams = {
						polldata : data,
						timelimit : timeLimit,
						iscount : isCountdown
					};
					
					cordova.exec(function(result) {
						console.log("cordova.exec() success.. makePollSheet");
					}, function(result) {
						alert('makePollSheet err : ' + JSON.stringify(result));
					}, "PollPlugin", "makePollSheet", [cordovaParams]);
					
				}
				
			},
			error : function(){ Utils.log("error..") },
			complete : function(){ Utils.log("complete..") }
		});
	},


	// (참여자) 설문 시트 결과 전송  PollCtrl.Action.Attender.submitPollResult
	submitPollResult : function(params) {

		var pollNo  = params.pollno;
		var pollType = params.polltype;
		var userKey = params.userno;
		var userId  = params.userid;
		var userNm  = params.usernm;
		var pollItemNo = params.pollitemno;
		var answerTxt = params.answertxt;
		var guestType = userKey != userId ? "0" : "1";   // 0 : 일반유저, 1 : 게스트

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
	
	/*
	  카운트다운 후에 폴 종료시 호출
	 @param
	 - pollNo : 폴 넘버 (UUID)
	 */
	finishPoll : function(pollNo) {
		PollCtrl.isProgress = true;
		PollCtrl.progressPoll = null;
		
		Ctrl.Msg.show(_msg("poll.alert.finish"));
		PollCtrl.Action.Common.makePollResult(pollNo);
	},
	
	// 폴 강제로 종료
	stopPoll : function(pollNo) {
		PacketMgr.Master.poll('interrupt', pollNo, '', '');
		PollCtrl.Action.Master.finishPoll(pollNo);
	},

	// 폴 결과그래프를 참여자들과 공유할 때 호출
	sharePollReport : function(pollNo) {
		PacketMgr.Master.poll('report', pollNo, '', '');
	},


	// 폴 템플릿 리스트 받아오기
	getPollTmpList : function(pollKey, pageNum) {
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

	// 폴 템플릿 상세 정보 불러오기
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
	
	
	// 완료된 폴 리스트 받아오기 - PollCtrl.getCompletePollList(pageNum);
	getCompletePollList : function(pollKey, pageNum) {
		//var pollKey = RoomSvr.roomid;
		var param = {
			pollkey : pollKey,
			//rows  : PollCtrl.UI.ROWS_PER_PAGE,
			pageno  : pageNum
		};
		
		var svrFlag = _prop('svr.flag');
		var svrHost = _prop('svr.host.' + svrFlag);
		var url = svrHost + _prop("poll.list");
		$.ajax({
			type       : 'post',
			url        : url
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
				 //PollCtrl.UI.renderCompletedPollList(data, pageNum);
				 }*/
			},
			error      : function(){ Utils.log("error..") },
			complete   : function(){ Utils.log("complete..") }
		});
	},

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