var Notify = {
    Invite : {
        invite : function(roomId, roomTitle, userNo, userNm, receiveUserNo, receiveUserId) {
            var msg = userNm + " " + _msg("invite.room.msg.1") + " " + roomTitle + " " + _msg("invite.room.msg.2");
            var data = {
                roomid : roomId,
                title : roomTitle,
                msg : msg,
                userno : receiveUserNo,
                userid : receiveUserId,
                guserno : userNo,
                gusernm : userNm
            };
            
            console.log(data);
            
            var svrFlag = _prop('svr.flag');
            var svrHost = _prop('svr.host.' + svrFlag);
            var url = svrHost + _prop("notify.invite.send");
            
            Utils.request(url, "json", data, function(json) {
                
                if(json.result == 0){
                    //Notify.Invite.alert(_msg("invite.noti.success"));
                    
                    var data = json.map;
                    data.msg = msg;
                    data.link = Utils.addContext("room") + "/" + data.roomCode;
                    data.recv = userNo;
                    cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", ["초대가 완료되었습니다."]);
                    //NotiService.sendMsg("Notify.receive", JSON.stringify(data));
                    
                    //$("li", "#invite_target").remove();
                    
                }
                else if(json.result == -8001)
                cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", ["초대할 방, 유저가 선택되었는지 확인해주세요."]);
                else{
                    // Notify.Invite.alert(_msg("invite.noti.fail"));
                    alert(JSON.stringify(json));
                }
                
            }, function(){
                Notify.Invite.alert(_msg("invite.noti.fail"));
            });
        },
        
        search : function(userNo, searchKey, searchTxt, suc) {
            console.log('search');
            console.log(suc);
            console.log(userNo + searchKey + searchTxt);
            var data = {
                userno : userNo,
                key : searchKey,   // usernm / email
                val : searchTxt
            }
            
            var svrFlag = _prop('svr.flag');
            var svrHost = _prop('svr.host.' + svrFlag);
            var url = svrHost + _prop("notify.invite.search");
            
            Utils.request(url, "json", data, function(json) {
                
                if(json.result == "0") {
                    suc(json.list);
                    
                    //cordova.exec(function(result){
                    //
                    //    console.log("cordova.exec() success.. searchInviteUserResult");
                    //    console.log(result);
                    //}, function(result){
                    //    alert('searchInviteUserResult err '+JSON.stringify(result));
                    //}, "myPlugin", "searchInviteUserResult", list);
                }
            });
        },
        
        remove : function(userNo, seqNo, suc){
            var svrFlag = _prop('svr.flag');
            var svrHost = _prop('svr.host.' + svrFlag);
            var url = svrHost + _prop("notify.invite.remove");
            
            var data = {
                userno : userNo,
                seqno : seqNo
            };
            
            Utils.request(url, "json", data, function(json){
                if(json.result == "0"){
                    // TODO : UI에서 삭제할 것
                    suc(seqNo);
                    //cordova.exec(null,null,"myPlugin", "removeInviteList", [seqNo]);
                }
                else
                alert(JSON.stringify(json));
            });
            
            //TODO : confirm 창 띄우기
            //this.confirm(_msg("invite.noti.confirm.remove"), callback);
        },
        
        fbDialog : function(receiveUserId, roomCode, roomId, roomTitle, userNo, userNm){
    		
        	var svrFlag = _prop('svr.flag');
   			var appId   = _prop("fb.id." + svrFlag);
   			var hostUrl = _prop("svr.host." + svrFlag);
    		
    		var link = hostUrl + "fb/room/" + roomCode;
    		
    		var url = "http://www.facebook.com/dialog/send?app_id=" + appId + "&link=" + roomUrl + "+&to=" + userId;
    		console.log("[fbDialog] " + url);
    		$.ajax({
                url     : url,
                type    : 'GET',
                success : function(data) {
                	console.log(data);
                	Notify.Invite.inviteSns(roomId, roomTitle, userNo, userNm, receiveUserId);
                },
                error : function(error){alert(JSON.stringify(error));}
            });
    	},

    	glDialog : function(){
    		var roomCode = $("input[name=room_list]:checked", "#search_room").attr("code");
    		var link = Utils.addContext("room") + "/" + roomCode;
    		var share = "https://plus.google.com/share?url=" + link;
    				
    		window.open(share, '', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=420,width=500'); return false;
    	},
    	
    	inviteSns : function(roomId, roomTitle, userNo, userNm, receiveUserId) {		
    		var roomId = "";
    		var roomTitle = "";
    		
    		if(roomId == ""){
    			cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("invite.not.found.room")]);
    			return;			
    		}
    		
    		var msg = userNm + " " + _msg("invite.room.msg.1") + " " + roomTitle + " " + _msg("invite.room.msg.2");		
    		var data = {
    			roomid : roomId,
    			title : roomTitle,
    			msg : msg,
    			// userno : userNo,
    			userid : receiveUserId,
    			guserno : userNo,
    			gusernm : userNm				
    		};
    		
    		var svrFlag = _prop('svr.flag');
            var svrHost = _prop('svr.host.' + svrFlag);
            var url = svrHost + _prop("notify.invite.send");
            
    		Utils.request(url, "json", data, function(json){			
    			
    			if(json.result == 0) {
    				
    				cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("invite.noti.success")]);
    				
    				//var data = json.map;
    				//data.msg = msg;
    				//data.link = Utils.addContext("room") + "/" + data.roomCode;
    				//data.recv = id;
    				//NotiService.sendMsg("Notify.receiveSns", JSON.stringify(data));  // DWR 코드는 제거
    				
    			} else {
    				cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("invite.noti.fail")]);
    			}
    			
    		}, function() {
    			cordova.exec(function(result){}, function(result){}, "myPlugin", "alertToast", [_msg("invite.noti.fail")]);
    		});
    		
    	}
    },
    
    getInvitedInfo : function(userNo, suc) {
        var svrFlag = _prop('svr.flag');
        var svrHost = _prop('svr.host.' + svrFlag);
        var url = svrHost + _prop("received.invite.get");
        console.log("[getInvitedInfo] request url : " + url);
        
        console.log(suc);
        
        var param = {
            userno : userNo
        };
        
        Utils.request(url, "json", param, function(data) {
            if(data.result == '0') {
                suc(data.list);
                
                //var param = {
                //  list : data.list
                //};
                //// 채팅 유저 리스트를 네이티브로..
                //cordova.exec(function(result) {
                //    console.log("cordova.exec() success.. initInvitedList");
                //}, function(result) {
                //    alert('initInvitedList err ' + JSON.stringify(result));
                //}, "myPlugin", "initInvitedList", [param]);
            }
            
            else {
                console.log(data);
            }
        });
    }
}