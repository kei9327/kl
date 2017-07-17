var MainApp = {
    pushObj : null,
    cookie : null,
    isLogin : false,
    userInfo : null,
    myRoomLen : 3,
    deviceToken : "",
    tabIdx : 0,
    
    // Application Constructor
    initialize : function(token) {
        console.log("initialize");
        //console.log(navigator.userAgent + ", " + cordova.platformId);
        //console.log(device);
        
        MainApp.deviceToken = token;
        
        device.uuid = cordova.platformId == "ios" ? device.uuid.replaceAll("-", "") : device.uuid;
        
        MainApp.cookie = localStorage.MC
        MainApp.isLogin = typeof MainApp.cookie != 'undefined' ? true : false;  // 로그인 유무 체크
        
        MainApp._setEvent();         // 메인페이지 이벤트 세팅
        MainApp._setNetworkEvent();  // 네트워크 상태 체크 이벤트 세팅
        MainApp.UI.toggleMainContents(MainApp.isLogin);  // 초기 UI 제어
        
        Auth.init(token);
    },
    
    onResumeMain : function() {
        console.log("onResumeMain fired..");
        
        var isLogin = localStorage.MC != undefined ? true : false;
        MainApp.isLogin = isLogin;
        MainApp.UI.toggleMainContents(isLogin);
        
        /*
         if(MainApp.isLogin) {
         MainApp.getRoomList();
         MainApp.getDefaultRoomList(MainApp.userInfo.userno);   // 가장 많이 본 노트, 에디터 추천 노트 가져오기..
         } else {
         MainApp.getDefaultRoomList("");   // 가장 많이 본 노트, 에디터 추천 노트 가져오기..
         }*/
    },
    
    UI : {
        toggleMainContents : function(loginFlag) {
            if(loginFlag) {
                $("#beforeLogin").hide();
                $("#afterLogin").show();
                
                Auth.getUserInfo(function(result) {
                    console.log("로그인 상태입니다.. 유저 정보..");
                    
                    /* {
                     aip: "112.217.207.82"
                     atime: "20160116185928"
                     email: "thooy@naver.com"
                     groupno: "0"
                     snstype: "0"
                     status: "0"
                     thumbnail: "https://graph.facebook.com/1110029269026734/picture"
                     userid: "1110029269026734"
                     usernm: "김민수"
                     userno: "337fa516450i63e7"
                     } */
                    
                    console.log(result);
                    MainApp.userInfo = result;
                    MainApp.UI._renderUserInfo(result);
                    MainApp.getRoomList();
                    MainApp.getInviteRoomList();
                    MainApp.getDefaultRoomList(MainApp.userInfo.userno);   // 가장 많이 본 노트, 에디터 추천 노트 가져오기..
                    MainApp.UI._renderFriendList();
                });
                $(".roomList").hide();
                switch(MainApp.tabIdx) {
                    case 0 :
                        $("#myRoom").show();
                        break;
                    case 1 :
                        $("#historyRoom").show();
                        break;
                    case 2 :
                        $("#bookmarkRoom").show();
                        break;
                    case 3 :
                        $("#mostViewRoom").show();
                        break;
                    case 4 :
                        $("#recommandRoom").show();
                        break;
                }
                //$("#myRoom").show();
            } else {
                $("#beforeLogin").show();
                $("#afterLogin").hide();
                $(".roomList").hide();
                $("#mostViewRoom").show();
                MainApp.getDefaultRoomList('');   // 가장 많이 본 노트, 에디터 추천 노트 가져오기..
            }
        },
        deleteInviteFromUI : function(seqNo) {
            $("#"+seqNo).remove();
        },
        _renderFriendList : function(){
            console.log('_renderFriendList');
            console.log(localStorage.snsType);
            switch(parseInt(localStorage.snsType)) {
                case 0 :
                    console.log('fb');
                    Auth.getFacebookFriendList(MainApp.userInfo.userid, function(data){console.log('sucCB')});
                    break;
                case 1 :
                    console.log('gg');
                    break;
                default :
                    console.log('guest?');
                    break;
            }
        },
        _renderUserInfo : function(userInfo) {
            var userId = userInfo.userid;
            var userNm = userInfo.usernm;
            //var userNo = userInfo.userno;
            var thumbnail = userInfo.thumbnail;
            
            $("#userNm").text(userNm);
            $("#userThumb").prop("src", thumbnail);
            $("#userNoti").text("0");
        },
        _renderInviteMyRoomList : function(roomList) {
            $(".roomlist", ".invite").remove();
            var svrFlag = _prop('svr.flag');
            var svrHost = _prop('svr.host.' + svrFlag);
            
            for(var i=0; i<roomList.length; i++) {
                var roomThumbnail =  svrHost + "data/fb/room/" + roomList[i].roomid.substring(0, 3) + "/" + roomList[i].roomid;
                var defaultRoomThumb = svrHost + "mv/res/home/images/preview_" + (Math.floor(Math.random() * 7) + 1) + ".png";
                
                var roomStr = '<div id="' + roomList[i].roomid + '" class="roomlist" onclick="MainApp.social_invite_roomselect(this)"><span class="thumb">\
                <img src="'+ roomThumbnail +'" onerror="this.src=\'' + defaultRoomThumb + '\'" alt=""/>\
                </span>' + roomList[i].title + '</div>';
                
                $(".invite").prepend(roomStr);
            }
        },
        _renderRoomList : function(roomList, sectionId) {
            $(".roomWrap", "#" + sectionId).remove();
            var len = roomList.length;
            if(sectionId == "myRoom") {
                MainApp.myRoomLen = len;
                if(len < 3) {
                    var newRoomHtmlStr = "<div class=\"roomWrap makeroom\">\
                    <p class=\"description\">새 노트를 만들고<br /> 다른 사람들과 공유 해 보세요</p>\
                    <a href=\"#\" class=\"createRoom\" onclick=\"MainApp.makeRoom();\">노트 만들기</a>\
                    </div>";
                    $("#" + sectionId).append(newRoomHtmlStr);
                }
            }
            
            for(var i=0; i<len; i++) {
                var svrFlag = _prop('svr.flag');
                var svrHost = _prop('svr.host.' + svrFlag);
                
                var room = roomList[i];
                console.log(room);
                if(room != null) {
                    var userNo = room.userno;
                    var roomId = room.roomid;
                    var roomTitle = room.title;
                    var roomThumbnail =  svrHost + "data/fb/room/" + roomId.substring(0, 3) + "/" + roomId;
                    //var roomCode = userNo.substring(10, 12) + roomId.substring(0, 2) + roomId.substring(23, 27);
                    var seqNo = room.seqno + "";
                    if(seqNo.length < 5) {
                        while(true) {
                            seqNo = "0" + seqNo;
                            if(seqNo.length == 5) break;
                        }
                    }
                    //console.log("새로운 룸코드 : " + seqNo);
                    var roomCode = seqNo;
                    
                    var defaultRoomThumb = svrHost + "mv/res/home/images/preview_" + (Math.floor(Math.random() * 7) + 1) + ".png";
                    var defaultUserThumb = svrHost + "mv/res/fb/img/default_userImg.png"
                    
                    var roomUrl = "view.html?code=" + roomCode;
                    
                    var bookmarkFlag = room.bookmark;
                    var bookmarkFlagStr = bookmarkFlag == '1' ? "on" : "";
                    
                    var htmlStr = "<div class=\"roomWrap\">\
                    <div class=\"room\" onclick=\"MainApp.enterRoom('" + roomUrl + "');\">\
                    <div class=\"roomThumb\">\
                    <img src=\"" + roomThumbnail + "\" onerror=\"this.src='" + defaultRoomThumb + "'\" alt=\"\">\
                    </div>\
                    <img src=\"" + room.thumbnail + "\" onerror=\"this.src='" + defaultUserThumb + "'\" alt=\"\" class=\"userPic\" />\
                    <p class=\"userID\">" + room.usernm + "</p>\
                    <p class=\"roomTitle\">" + roomTitle + "</p>\
                    <p class=\"view\">" + room.readcnt +  " view</p>\
                    </div>\
                    <a href=\"#\" class=\"favorites " + bookmarkFlagStr + "\" onclick=\"MainApp.setBookmark(this);\" roomid=\"" + roomId + "\"></a>\
                    </div>";
                    $("#" + sectionId).append(htmlStr);
                }
            }
        },
    },
    
    _setEvent : function() {
        console.log("_setEvent");
        
        $("#fbLogin").click(function(e){
            e.preventDefault();
            var snsType = "0";
            Auth.login(snsType, function(){
                MainApp.isLogin = true;
                MainApp.UI.toggleMainContents(MainApp.isLogin);
                //location.reload();
            });
            //MainApp.login("0");
        });
        
        $("#ggLogin").click(function(){
            //e.preventDefault();
            var snsType = "1";
            Auth.login(snsType, function(){
                MainApp.isLogin = true;
                MainApp.UI.toggleMainContents(MainApp.isLogin);
            });
            //MainApp.login("1");
        });
        
        $("#guestLogin").click(function(){
            if(document.getElementById('name').value == "")
                alert('이름을 입력해 주세요');
            else{
                Auth.login("", document.getElementById('name').value);
                MainApp.isLogin = true;
                MainApp.UI.toggleMainContents(MainApp.isLogin);
            }
        });
        
        $("#signout").click(function(e){
            e.preventDefault();
            MainApp.logout();
        });
        
        $(".btn_floating_make, .createRoom").click(function(e) {
            e.preventDefault();
            if(!MainApp.isLogin) {
                alert("로그인 후 이용해주세요.");
                return;
            }
            if(MainApp.myRoomLen > 3) {
                alert("룸은 최대 3개까지 개설할 수 있습니다.");
                return;
            }
            MainApp.makeRoom();
        });
        
        
        $(".navWrap > a").click(function(e) {
            e.preventDefault();
            MainApp.tabIdx = parseInt($(this).attr("menuidx"));
            $(".navWrap > a").removeClass("on");
            $(this).addClass("on");
            var id = e.target.id;
            $(".roomList").hide();
            $("#" + id.split("_")[1]).show();
            
        });
        
        
        $('a','div.title').keypress(function(e){
            e.preventDefault();
            if(e.keyCode === 13) {
                var val = $(this).prev().val();
                val = val.split('/').pop();
                val = val.trim();
                
                MainApp.checkRoomCode(val, function(){
                    MainApp.enterRoom('view.html?code='+val);
                });
            }
        });
        
        $('a','div.title').click(function(e) {
            e.preventDefault();
            var val = $(this).prev().val();
            val = val.split('/').pop();
            val = val.trim();
            
            MainApp.checkRoomCode(val, function(){
                MainApp.enterRoom('view.html?code='+val);
            });
            
            //var svrFlag = _prop('svr.flag');
            //var svrHost = _prop('svr.host.' + svrFlag);
            //var url = svrHost + "mv/m/code/check.json";
            //
            //var param = { roomcode : val };
            //
            //Utils.request(url, "json", param, function(data) {
            //    console.log(data);
            //    if(data.result == "0") {
            //        MainApp.enterRoom('view.html?code='+val);
            //    } else {
            //        alert("유효하지 않은 룸코드입니다. 확인 후 다시 입력해주세요.");
            //        $("#direct_link").focus();
            //        return;
            //    }
            //});
        });
        
        $("header .user").click(function(e) {
            e.preventDefault();
            $(".mySocial").addClass("on");
            $(".mySocial").show();
            $("body").css("position", "fixed");
            return false;
        });
        $(".mySocial .close").click(function(e) {
            e.preventDefault();
            $(".mySocial").removeClass("on");
            $("body").css("position", "relative");
            return false;
        });
        //$(".mySocial .searchArea .searchResult li").click(function(e) {
        //    e.preventDefault();
        //    $(this).toggleClass("on");
        //    return false;
        //});
        //$(".mySocial .searchResult .selectAllFriends").click(function(e) {
        //    e.preventDefault();
        //    $(this).toggleClass("on");
        //    return false;
        //});
        //$("#IM0_SAF").click(function(e) {
        //    e.preventDefault();
        //    $(this).toggleClass("on");
        //
        //    var searchResult = $(".searchResult li");
        //    for(var i=0; i<searchResult.length; i++)
        //    MainApp.addInviteUser(searchResult[i]);
        //
        //    return false;
        //});
        $("#IM3_SAF").click(function(e) {
            e.preventDefault();
            $(this).toggleClass("on");
            return false;
        });
        //$(".mySocial .roomlist .cancel").click(function(e) {
        //    e.preventDefault();
        //    $(this).parents(".roomlist").removeClass("selected");
        //    return false;
        //});
        $(".mySocial .friendList li").click(function(e) {
            e.preventDefault();
            $(this).toggleClass("on");
            return false;
        });
        
        $("#inviteM1").click(function(e){
            e.preventDefault();
            $("#inviteM1").addClass("on");
            $("#inviteM2").removeClass("on");
            $("#inviteM3").removeClass("on");
            $(".invite").show();
            $(".invite2").hide();
            $(".invite3").hide();
        });
        $("#inviteM2").click(function(e){
            e.preventDefault();
            $("#inviteM1").removeClass("on");
            $("#inviteM2").addClass("on");
            $("#inviteM3").removeClass("on");
            $(".invite").hide();
            $(".invite2").show();
            $(".invite3").hide();
        });
        $("#inviteM3").click(function(e){
            e.preventDefault();
            $("#inviteM1").removeClass("on");
            $("#inviteM2").removeClass("on");
            $("#inviteM3").addClass("on");
            $(".invite").hide();
            $(".invite2").hide();
            $(".invite3").show();
        });
        
        $(".btn_search").click(function(e){
            e.preventDefault();
            var typeIndex = $(".searchType option").index($(".searchType option:selected"));
            var searchType = typeIndex ? 'email' : 'usernm';
            Notify.Invite.search(MainApp.userInfo.userno, searchType, $("#searchInput").val(), sucCB);
            function sucCB(data) {
                console.log(data);
                var appendTo = $(".searchResult ul");
                appendTo.removeClass("li");
                $(".searchResult ul li").remove();
                var svrFlag = _prop('svr.flag');
                var svrHost = _prop('svr.host.' + svrFlag);
                var defaultUserThumb = svrHost + "mv/res/fb/img/default_userImg.png"
                
                $(".searchResult").removeClass("on");
                if(data.length)
                    $(".searchResult").addClass("on");
                
                var inviteUserList = $(".searchResultUser li");
                for(var i=0; i<data.length; i++){
                    var isHave = false;
                    for(var j=0; j<inviteUserList.length; j++){
                        if(inviteUserList[j].id == data[i].userno){
                            isHave = true;
                            break;
                        }
                    }
                    console.log(isHave);
                    var htmlStr = '<li id="' + data[i].userno + '" onclick="MainApp.addInviteUser(this)">\
                    <img id="' + data[i].userid + '"src="'+ data[i].thumbnail +'" onerror="this.src=\'' + defaultUserThumb + '\'" alt=""/>' + data[i].usernm + '</li>';
                    appendTo.append(htmlStr);
                    if(isHave) $(".searchResult ul li:last-child").addClass("on");
                }
            }
        });
        
        $(".btn_invite").click(function(e){
            e.preventDefault();
            var roomList = $(".invite .roomlist");
            var roomId;
            var roomTitle;
            for(var i=0; i<roomList.length; i++)
                if($(roomList[i]).hasClass('selected')) {
                    roomId = roomList[i].id;
                    roomTitle = $(roomList[i]).text();
                    break;
                }
            console.log(roomId);
            console.log(e.target.id);
            if(e.target.id == "inviteBtn0") {
                var inviteUserList = $(".searchResultUser li");
                var receiveUserNo = "";
                var receiveUserId = "";
                for(var i=0; i<inviteUserList.length; i++) {
                    receiveUserNo += inviteUserList[i].id + ',';
                    receiveUserId += inviteUserList[i].getElementsByTagName('img')[0].id + ',';
                }
                Notify.Invite.invite(roomId, roomTitle, MainApp.userInfo.userno, MainApp.userInfo.usernm, receiveUserNo, receiveUserId);
                inviteUserList.remove();
                var searchUserList = $(".searchResult li");
                for(var i=0; i<searchUserList.length; i++)
                    $(searchUserList[i]).removeClass("on");
            }
        });
    },
    
    _setNetworkEvent : function() {
        console.log("_setNetworkEvent");
        document.addEventListener("online", MainApp.onOnline, false);
        document.addEventListener("offline", MainApp.onOffline, false);
    },
    
    onOnline : function(){
        console.log("online..");
    },
    
    onOffline : function(){
        console.log("offline..");
    },
    
    
    login : function(snsType) {  // snsType : 0 - facebook, 1 - google
        Auth.login(snsType, function(){
            MainApp.isLogin = true;
            MainApp.UI.toggleMainContents(MainApp.isLogin);
            //location.reload();
        });
        
    },
    
    logout : function() {
        Auth.logout(function(){
            MainApp.isLogin = false;
            MainApp.UI.toggleMainContents(MainApp.isLogin);
            //location.reload();
        });
        
    },
    
    getDefaultRoomList : function(userNo) {
        var svrFlag = _prop("svr.flag");
        var svrHost = _prop("svr.host." + svrFlag);
        var url = svrHost + _prop("default.room.list");
        
        console.log(userNo);
        var param = { userno : userNo };
        
        Utils.request(url, "json", param, function(data) {
            //console.log(data);
            if(data.result == "0") {
                //console.log(data);
                MainApp.UI._renderRoomList(data.list.mostview, "mostViewRoom");
                MainApp.UI._renderRoomList(data.list.recommand, "recommandRoom");
            } else {
                
            }
        });
    },
    
    getInviteRoomList : function() {
        Notify.getInvitedInfo(MainApp.userInfo.userno, sucCB);
        function sucCB(data){
            console.log(data);
            $(".invite2").text = '';
            var svrFlag = _prop('svr.flag');
            var svrHost = _prop('svr.host.' + svrFlag);
            var defaultUserThumb = svrHost + "mv/res/fb/img/default_userImg.png";
            for(var i=0; i<data.length; i++) {
                var defaultRoomThumb = svrHost + "mv/res/home/images/preview_" + (Math.floor(Math.random() * 7) + 1) + ".png";
                var roomThumbnail =  svrHost + "data/fb/room/" + data[i].roomid.substring(0, 3) + "/" + data[i].roomid;
                
                //<div class="roomlist selected">
                //<span class="thumb">
                //<img src="./res/images/imsi_thumb01.png" alt=""/>
                //</span>
                //포카리스웨트의 성분이 정말로 수분 공급에 적...
                //<img src="./res/images/imsi_user01.png" alt="" class="userPic"/>
                //<span class="userID">akreid.m</span>
                //<span class="time">2016.01.19</span>
                //<div class="accept">
                //<a href="" class="entrance"></a>
                //<a href="" class="delete"></a>
                //<a href="" class="cancel">x</a>
                //</div>
                //</div>
                var delStr = 'Notify.Invite.remove(\'' + MainApp.userInfo.userno + '\', \'' + data[i].seqno + '\', MainApp.UI.deleteInviteFromUI);';
                var cdatetime = data[i].cdatetime.slice(0,4) + '.' + data[i].cdatetime.slice(4,6) + '.' + data[i].cdatetime.slice(6,8);
                var htmlStr = '<div id="' + data[i].seqno + '" onclick="MainApp.selectInvitedRoom(this)" class="roomlist"> <span class="thumb">\
                <img src="' + roomThumbnail + '" onerror="this.src=\'' + defaultRoomThumb + '\'" alt=""/> </span>' + data[i].roomtitle
                + '<img src="' + data[i].fromthumb +'" onerror="this.src=\'' + defaultUserThumb + '\'" alt="" class="userPic"/>\
                <span class="userID">' + data[i].fromusernm + '</span>\
                <span class="time">' + cdatetime + '</span> <div class="accept">\
                <a href="#" onclick="MainApp.enterRoom(\'view.html?code='+data[i].roomcode+'\')" class="entrance"> </a>\
                <a href="#" onclick="' + delStr + '" class="delete"></a>\
                <a href="#" class="cancel">x</a> </div> </div>';
                $(".invite2").append(htmlStr);
            }
            $(".mySocial .roomlist .cancel").click(function(e) {
                e.preventDefault();
                $(this).parents(".roomlist").removeClass("selected");
                return false;
            });
        }
    },
    
    getRoomList : function() {
        if(!localStorage.MC || !localStorage.CS) {
            alert("로그인 정보가 없습니다. 로그인을 해주세요.");
            return;
        }
        
        var master   = localStorage.MC;
        var checksum = localStorage.CS;
        
        var queryString = "FBMMC=" + master + "&FBMCS=" + checksum;
        var url = _prop('svr.host.' + _prop('svr.flag')) + _prop('room.list');
        
        //alert(queryString + " " + url);
        
        $.ajax({
            type  : "POST",
            url   : url,
            beforeSend : function (request){
                request.setRequestHeader('Authorization', queryString);
            },
            success : function(data) {
                console.log(data);
                
                MainApp.UI._renderInviteMyRoomList(data.list.myroom);
                MainApp.UI._renderRoomList(data.list.myroom, "myRoom");
                MainApp.UI._renderRoomList(data.list.history, "historyRoom");
                MainApp.UI._renderRoomList(data.list.bookmark, "bookmarkRoom");
                
            },// resetRoom(i, data.list);},
            error : function(error){
                alert("err: "+JSON.stringify(error));
            }
        });
    },
    
    makeRoom : function(){
        if(!MainApp.isLogin) {
            alert("로그인 후에 이용해주세요.");
            return;
        }
        var params = {
            userid : MainApp.userInfo.userid,
            usernm : MainApp.userInfo.usernm,
            userno : MainApp.userInfo.userno,
            ipaddr : MainApp.userInfo.aip,
            deviceid : device.uuid
        };
        
        var url = _prop('svr.host.' + _prop('svr.flag')) + _prop('canvas.add');
        Utils.request(url, "json", params, function(data){
            if(data.result == "0") {
                console.log(data);
                // TODO : 새로 개설한 룸의 룸코드값을 얻었으니 룸 입장 처리가 필요하겠죠?
                MainApp.enterRoom('view.html?code='+data.code);
            }
            else{
                // TODO: 에러 케이스이니 예외처리 추가하면 됩니당
            }
        });
    },
    
    checkRoomCode : function(roomCodeStr, callback) {
        var svrFlag = _prop('svr.flag');
        var svrHost = _prop('svr.host.' + svrFlag);
        var url = svrHost + "mv/m/code/check.json";
        
        if(roomCodeStr == "") {
            alert("룸코드를 입력해주세요.");
            return;
        }
        
        var param = { roomcode : roomCodeStr };
        
        Utils.request(url, "json", param, function(data) {
            console.log(data);
            if(data.result == "0") {
                if(typeof callback == 'function') {
                    callback();
                }
                //MainApp.enterRoom('view.html?code='+val);
            } else {
                alert("올바르지 않은 룸코드 입니다. 확인 후 다시 입력해주세요.");
                $("#direct_link").focus();
                return;
            }
        });
    },
    
    
    // paramStr : view.html?code=0i838dD7 형태의 스트링
    enterRoom : function(paramStr) {
        cordova.exec(function(result) {
            console.log("enterRoom success : " + JSON.stringify(result));
        }, function(err) {
            console.log("enterRoom error : " + err);
        }, "myPlugin", "enterRoom", [paramStr]);
    },
    
    social_invite_roomselect : function(item) {
        $(".mySocial .invite .roomlist").removeClass("selected");
        $(item).addClass("selected");
        return false;
    },
    
    addInviteUser : function(item) {
        if($(item).hasClass('on')) {
            $(item).removeClass('on');
            inviteUserList = $(".searchResultUser li");
            for(var i=0; i<inviteUserList.length; i++)
                if(item.id == inviteUserList[i].id){
                    $(inviteUserList[i]).remove();
                    return;
                }
        }
        $(".searchResultUser").append(item.outerHTML);
        inviteUserList = $(".searchResultUser li");
        $(inviteUserList[inviteUserList.length-1]).append('<a onclick="MainApp.delInviteUser(this)" href="#" class="delete">x</a>');
        inviteUserList[inviteUserList.length-1].onclick = null;
        $(item).addClass('on');
        //<li>
        //<img src="./res/images/imsi_user05.png" alt=""/><span>thouy</span> <a href="" class="delete">x</a>
        //</li>
    },
    
    delInviteUser : function(item) {
        console.log($(item).parent());
        var searchResult = $(".searchResult li");
        for(var i=0; i<searchResult.length; i++)
            if($(item).parent()[0].id == searchResult[i].id){
                $(searchResult[i]).removeClass("on");
                break;
            }
        $(item).parent().remove();
    },
    selectInvitedRoom : function(item) {
        $(".mySocial .roomlist").removeClass("selected");
        $(item).toggleClass("selected");
    },
    setBookmark : function(elem) {
        console.log(MainApp.isLogin);
        if(!MainApp.isLogin) {
            alert("북마크 기능은 로그인 후에 이용하실 수 있습니다.");
            return;
        }
        
        var roomId = $(elem).attr("roomid");
        var params = {
            roomid : roomId,
            userno : MainApp.userInfo.userno,
            groupno : MainApp.userInfo.groupno
        };
        
        console.log(params);
        
        var svrFlag = _prop('svr.flag');
        var svrHost = _prop('svr.host.' + svrFlag);
        var postfixUrl = $(elem).hasClass("on") ? "mv/bm/remove.json" : "mv/bm/add.json";
        var url = svrHost + postfixUrl;
        
        if($(elem).hasClass("on")) {
            if(!confirm( _msg("confirm.remove.bookmark"))) return;
        }
        
        Utils.request(url, "json", params, function(data) {
            console.log(data);
            if(data.result == "0") {
                console.log(data);
                //location.reload();
                
                if($(elem).hasClass("on")) {
                    $(elem).removeClass("on");
                } else {
                    $(elem).addClass("on");
                }
                
                MainApp.UI.toggleMainContents(MainApp.isLogin);
            } else if (data.result == "-504") {
                alert("북마크는 최대 3개까지 등록할 수 있습니다.");
            } else {
                alert("오류가 발생하였습니다. 관리자에게 문의해주세요.");
            }
        });
        
    },
}

function setDeviceToken(token) {
    console.log("setDeviceToken"+token);
    localStorage.setItem("devicetoken", token);
    MainApp.initialize(localStorage.devicetoken);
}

document.addEventListener('deviceready', function(){
    console.log("deviceready event fired..");
    localStorage.removeItem('devicetoken');
    cordova.exec(function(result) {
        console.log("cordova.exec() success.. notifyDeviceReady");
    }, function(result) {
        alert('notifyDeviceReady err : ' + JSON.stringify(result));
    }, "myPlugin", "notifyDeviceReady", [{}]);
    
    //var loader = setInterval(function(){
    //    console.log("token loading...  " + MainApp.deviceToken);
    //    if(localStorage.devicetoken != undefined) {
    //        MainApp.initialize(localStorage.devicetoken);
    //        clearInterval(loader);
    //        return;
    //    }
    //}, 500);
});