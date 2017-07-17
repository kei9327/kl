var UI = {
    BODYLAYER : "confContainer",
CONTAINER: "contsWrapper",
    HEADER : "hmenubar",
    VIEWER : "wrap",
    WRAPPER : "wrapper",
    LOADER : "loader",
    SKETCH : "sketch",
    RIGHT : "right_wrap",
    INITPDF : 3,
    DELKEY : 46,
    skboards : [], 	// canvas
    scale : 1, 		// 현재 zoom scale
    current : 1,
    file : 0,
    key : 48,
    rendering : false,
    isShift : false,
    bfUnload : false,
    canvas_w : [1200,1280,1366,1920],
    canvas_h : [681,939,1140],
    Cursor : {
        images : [],
        init : function() {
            for(var i=1; i<11; i++) {
                for (var j=1; j<4; j++) {
                    var img = new Image();
                    // img.src = Utils.addContext("/res/images/pointer_type0"+ j + "_0" + i + ".png");
                    var token = i < 10 ? "_0" : "_";
                    img.src = Utils.addResPath("fb/images", "pointer_type0"+ j + token + i + ".png");
                    
                    this.images.push(img);
                };
            }
        }
    },
    init : function(){
        this.Cursor.init();
        this.setDivHolder(1);
        this.setCanvas(1);
        this.setEvent();
    },
    
    _getWMode : function() {
        var w = $(window).width();
        var h = $(window).height();
        
        var left = $("#" + UI.CONTAINER).position().left;
        
        // 영상이랑 믹스된 버전인 경우 left를 더해서 랜더링한다.
        w -= left;
        
        var mode = "0";
        var len = UI.canvas.length;
        for (var i = 0; i < len; i++) {
            var ckWidth = UI.canvas[i].width + 10;
            var ckHeight = UI.canvas[i].height + 70;
            
            if (w > ckWidth && h > ckHeight) {
                mode = i.toString();
            }
        }
        
        return mode;
    },
    _isRender : function(pageIdx) {
        var layout = document.getElementById("sketch" + pageIdx);
        return (layout) ? true : false;
    },
    
    _getSketch : function(pageIdx) {
        return document.getElementById(this.SKETCH + pageIdx);
    },
    
    setCanvas : function(pageNo){
        var id = "#sketch" + pageNo;
        var containerId = "#" + UI.CONTAINER;
        
        var isWeb = !Utils.cordova();
        var app = new SketchBoard();
        drawing = app.init(containerId, id, isWeb, UI.Cursor.images);
        
        UI.skboards.push(drawing);
    },
    setDivHolder : function(pageNo){
        var termY = Utils.browser("msie") ? $("#" + UI.HEADER).height() : $("#" + UI.HEADER).height();
        var maxWidth = Utils.mobile() ? $(window).width() : window.screen.availWidth;
        var maxHeight =  Utils.mobile() ? $(window).height() : (window.screen.availHeight - (window.outerHeight - window.innerHeight)) - termY;
        var canvasWidth = maxWidth > maxHeight ? maxWidth : maxHeight;
        var canvasHeight = maxWidth > maxHeight ? maxWidth : maxHeight;
        
        // 가로는 스크롤이 없기 때문에 스크롤 넓이를 빼준다.
        /**
         if(canvasWidth > document.getElementById(UI.CONTAINER).scrollWidth){
         canvasWidth = document.getElementById(UI.CONTAINER).scrollWidth;
         canvasHeight = canvasWidth;
         }
         **/
        // IE는 무조건 가장 큰 모니터 기준으로 나온다.
        
        $("#" + UI.VIEWER).width(canvasWidth);
        $("#" + UI.WRAPPER).width(canvasWidth);
        $("#" + UI.VIEWER).height(canvasHeight);
        $("#" + UI.WRAPPER).height(canvasHeight);
    },
    
    setEvent : function(){
        $(window).bind("beforeunload", function(e){
            // mailto 태그에서 이곳이 걸릴수 있으므로 주의.. mailto는 frame 처리
            
            // IE는 A태그 이동시 before unload가 튀어버려서 예외처리 해줘야 한다.
            if(!Utils.browser("msie")) {
                VideoCtrl.destroyAll();
                
                RoomSvr.destroy();
                
                PacketMgr.destroy()
                
                UI.destroy();
                
                Ctrl.destroy();
            }
        });
        
        $(window).bind("unload", function(){
            
            // ie는 버그있어서 destroy를 예외처리 해준다.
            if(!Utils.browser("msie")) {
                VideoCtrl.destroyAll();
            }
            
            // IE는 이곳에서 destroy 해야 한다.
            RoomSvr.destroy();
            
            PacketMgr.destroy()
            
            UI.destroy();
            
            Ctrl.destroy();
            
        });
        
        // shift누를 경우 도형 정각형 처리
        $(window).bind("keydown keyup", function(e){
            if(e.keyCode == 16) UI.isShift = true;
        });
        
        // window disableselection
        $(window).attr('unselectable','on').css({'-moz-user-select':'-moz-none',
	           '-moz-user-select':'none',
	           '-o-user-select':'none',
	           '-khtml-user-select':'none', /* you could also put this in a class */
	           '-webkit-user-select':'none',/* and add the CSS class here instead */
	           '-ms-user-select':'none',
	           'user-select':'none'
        }).bind('selectstart', function(){ return false; });
        
        
        // 네트워크 처리
        $(window).bind("online", function(e){
            console.log(e);
            Ctrl.setMyNetworkStatus(true, false);
        });
        
        $(window).bind("offline", function(e){
            console.log(e);
            Ctrl.setMyNetworkStatus(false, false);
        });
    },
    
    getFixedX : function(w, h, dx){
        var scaleX = (w > h) ? (1024 / w) : (768 / w);
        return dx * scaleX;
    },
    
    getFixedY : function(w, h, dy){
        var scaleY = (w > h) ? (748 / h) : (1004 / h);
        return dy * scaleY;
    },
    
    getOrgX : function(w, h, dx){
        var orgScaleX = (w > h) ? (w / 1024) : (w / 768);
        return dx * orgScaleX;
    },
    
    getOrgY : function(w, h, dy){
        var orgScaleY = (w > h) ? (h / 748) : (h / 1004);
        return dy * orgScaleY;
    },
    
    zoomEnd : function(idx, zoomPacket) {
        var prevBoard = UI.skboards[idx - 2];
        var nextBoard = UI.skboards[idx];
        // 현재 페이지 Zoom
        UI.skboards[idx - 1]["async"](zoomPacket);
        
        // 이전 페이지 Zoom, 다음페이지의 Zoom은 clear한다.
        if(prevBoard) prevBoard.clearZoom();
        if(nextBoard) nextBoard.clearZoom();
    },
    destroy : function(){
        try{
            var len = this.skboards.length;
            for (var i = 0; i < len; i++) {
                this.skboards[i].destroy();
            }
            
            this.skboards = [];
            if(this.viewer){
                this.viewer.html("");
                this.viewer.css({left : 0});
            }
            
        }catch(e){
            // console.log("UI.destroy exception  : " + e);
        }
    }
};

// 마스터일 경우 UI to Room Server or draw to RoomServer or
var PacketMgr = {
    mode : "", // 회의모드 or 개인모드 구분 필요
    userid : "",
    usernm : "",
    userno : "",
    email : "",
    roomid : "",
    groupno : "",
    masterid : "",
    creatorid : "",
    layout : "",		// VIEW 모드 1-영상, 2-혼합
    bg : "",
    code : "",
    auth : "2",
    waitFileSeq : 9999,
    packet : null,
    lastTime : 0,
    isSend : false, // 룸서버에 전송해야 하는지 여부
    isMC : false,	// 진행자 여부
    isCreator : false,	// 방 생성자 여부
    isAllowMaster : true,	// 기본 허용
    isAllowChat : true,	// 기본 허용
    isAllowComment : true,	// 기본 허용
    isAllowExport : true,	// 기본 허용
    isGuest : false,
    isVideo : false,
    pdfs : [],
    queue : [], // 렌더링 밀릴경우 명령어 큐를 따로 쌓아두고 처리한다.
    animationQueue : [],
    penIdxList : [],
    penMap : null,	//	load로 전달받은 기본 패킷
    fileMap : null,	// key=파일시퀀스, value=pageMap  fileMap => 파일번호, 페이지맵(페이지번호, 패킷리스트)
    lastPensetting : null,	// 회의방 최초 입장시 받아온 패킷의 펜세팅값. 마지막 드로잉 패킷값. (초기 로딩시에만 사용)
    lastPageNo : "1",
    lastFileNo : "0",
    lastPacketNo : "",		// history 저장용
    isInitFChange : true, // 초기 filechange 이벤트가 안올수도 있기때문에 flag처리
    isLoadPacket : false,
    isLoadResize : false,
    isCommentPos : false,
    isMemoPos : false,
    saveList : ['pensetting', 'redo', 'undo', 'erasermode', 'draw', 'began', 'moved', 'ended', 'eraserbegan', 'erasermoved', 'eraserended', 'zoom', 'view', 'textbox'],
    init : function(options) {
        // 마스터와 권한을 저장한다.
        this.userid = options.userid || '';
        this.usernm = options.usernm || '';
        this.userno = options.userno || '';
        this.email = options.email || '';
        this.roomid = options.roomid || '';
        this.groupno = options.groupno || '';
        this.masterid = options.masterid || '';	// 마스터 아이디는 권환 회수시 마스터 비교용으로 사용한다.
        this.creatorid = options.creatorid || '';
        this.layout = options.layout || '';
        this.lastFileNo = options.fileno || 0;
        this.lastPageNo = options.pageno || 1;
        this.lastPacketNo = options.packetno || '';
        this.code =  options.code || '';
        
        // var authJson = $.parseJSON(options.auth);
        var authJson = options.auth;
        this.isAllowMaster = authJson.authtype == "1" ? true : false;
        this.isAllowChat = authJson.chatopt == "1" ? true : false;
        this.isAllowComment = authJson.cmtopt == "1" ? true : false;
        this.isAllowExport = authJson.expopt == "1" ? true : false;
        
        this.isMC = this.masterid == this.userid ? true : false;
        this.isCreator = this.creatorid == this.userid ? true : false;
        this.isGuest = this.userid == this.userno ? true : false;
        
        // var plugin = $.parseJSON(options.plugin);
        var plugin = options.plugin;
        this.isCommentPos = plugin.comment == "1" ? true : false;
        this.isMemoPos = plugin.memo == "1" ? true : false;
        // this.bg = $.parseJSON(options.bg) || '';
        this.bg = options.bg || '';
    },
    
    initFile : function(fileIdx) {
        this.isInitFChange = true;
        // 초기 파일을 렌더링하고 filechange가 오지 않는다면 첫번째 파일을 타이머 채크해서 동작시켜야 한다.
        var onSuccess = function() {
            Ctrl.init(PacketMgr.isMC);
            
            // 입장시 초기 init할때 마스터가 진행했던 패킷을 그린다.
            PacketMgr._drawHistoryPacket(PacketMgr.lastFileNo, PacketMgr.lastPageNo, false, function(){
                // 마지막 보고있는 페이지의 zoom 확인 (마스터가 들어온 경우만 사용)
                PacketMgr._drawLastZoomPacket(PacketMgr.lastFileNo, PacketMgr.lastPageNo);
                
                // 팬세팅 맞춤
                if(!PacketMgr.isMC){
                    // 마스터가 아닌경우 펜세팅 싱크 맞춰줘야 한다.
                    PacketMgr.pushQueue(PacketMgr._getLastPen());
                }
                
                // 마스터 기본 세팅
                PacketMgr.Master.changeMode("view");
                
                // queue shift
                PacketMgr.shiftQueue();
            });
            
            // 상단 파일 select부분 체크
            Ctrl.selectFile(fileIdx);
            
            // 로더 숨김
            // Ctrl.Loader.hide();
            if(PacketMgr.lastPageNo < 4 && PacketMgr.queue.length < 1){
                // 3페이지보다 작거나, 히스토리가 없는 경우
                // Ctrl.Loader.hide();
            }
        };
        
        UI.init(PacketMgr.pdfs, fileIdx, onSuccess);
    },
    
    isEmptyQueue : function(){
        return PacketMgr.queue.length < 1 ? true : false;
    },
    
    pushQueue : function(packet){
        if(typeof(packet) == "undefined" || packet == null) return;
        
        PacketMgr.queue.push(packet);
    },
    
    // UI가 렌더링 중일경우 밀린 큐 하나씩 꺼내서 실행
    shiftQueue : function() {
        if(PacketMgr.queue.length < 1) return;
        // Utils.log("UI.rendering : " + UI.rendering + ", queue len : " + PacketMgr.queue.length);
        if(UI.rendering || PacketMgr.isLoadPacket){
            // 랜더링 중이면 1초후 재실행
            setTimeout(PacketMgr.shiftQueue, "500");
            return;
        }
        
        var json = PacketMgr.queue.shift();
        var cmd = json.cmd ? json.cmd : json.from ? json.from : "";
        if(typeof (PacketMgr.Command[cmd]) != "undefined"){
            PacketMgr.Command[cmd](json);
        }else{
            // 초기 패킷은 current가 아니라 패킷에 저장된 페이지 번호로 드로잉 처리한다.
            var pageNo = typeof(json.page) != "undefined" && json.page != null ? json.page : UI.current;
            PacketMgr.Master.toCanvasPage(json, pageNo);
        }
        
        PacketMgr.shiftQueue();
        // setTimeout(PacketMgr.shiftQueue, "10");
    },
    
    // room서버에서 전송받은 data 처리
    receive : function(userid, data) {
        
        this.lastTime = new Date().getTime();
        Utils.log("receive userid : " + userid + ", data : " + data + ", this.lastTime : " + this.lastTime);
        
        // if 큐가 데이터 존재하면 큐에 넣고 break;
        var json = (typeof(data) == "object") ? data : JSON.parse(data);
        var cmd = json.cmd ? json.cmd : json.from ? json.from : "";
        if (userid == this.userid) {
            // 내가 그린 패킷을 새로 그리지 않는다.
            // Utils.log("<< packet same user >> " + JSON.stringify(data));
        } else {
            // addfile은 순서에 상관없이 강제로 실행시킨다.
            if(cmd == "addfile"){
                PacketMgr.Command[cmd](json);
                return;
            }
            /**
             Utils.log("receive UI.rendering : " + UI.rendering + ", PacketMgr.isLoadPacket : " + PacketMgr.isLoadPacket + ", PacketMgr.isInitFChange : " + PacketMgr.isInitFChange
             + ", this.Command[cmd] : " + typeof(this.Command[cmd]) + ", PacketMgr.waitFileSeq : " + PacketMgr.waitFileSeq + ", PacketMgr.queue : " + JSON.stringify(PacketMgr.queue) );
             **/
            // UI가 렌더링 중이거나 addFile이 안들어온경우 큐에 저장하고 있는다.
            if (UI.rendering || PacketMgr.isLoadPacket || PacketMgr.isLoadResize) {
                // Utils.log("receive UI.zooming : " + PDFViewer.zooming);
                // 렌더링 중이라면 큐에 계속 쌓는다.
                PacketMgr.queue.push(json);
                
            } else if(!PacketMgr.isInitFChange){
                // filechange가 addfile보다 먼저 들어온경우 큐 처리 한다.
                PacketMgr.queue.push(json);
                
                /******* filechange가 addfile보다 먼저 온경우 큐 처리 ********/
                if(cmd == "filechange") {
                    PacketMgr.waitFileSeq = json.count;
                }
                
                if(PacketMgr.waitFileSeq > PacketMgr.pdfs.length) return;
                
                PacketMgr.shiftQueue();
                
            } else if(PacketMgr.queue.length > 0){
                // filechange가 addfile보다 먼저 들어온경우 큐 처리 한다.
                PacketMgr.queue.push(json);
                PacketMgr.shiftQueue();
                
            } else if(typeof (this.Command[cmd]) != "undefined"){
                // 큐가 비어있는 경우는 그냥 명령 실행
                PacketMgr.Command[cmd](json);
            } else {
                // 드로잉 관련 패킷은 이곳으로 들어오고, 렌더링 중일때는 큐에 쌓았다가 던진다.
                PacketMgr.Master.toCanvasPage(json, UI.current);
            }
            
            // history가 아닌 packet으로 받은 pensetting만 업데이
            if(!PacketMgr.isMC && cmd == "pensetting") PacketMgr.Master.masterPensetting = json;
            
            // 히스토리에 어노테이션 저장
            if(PacketMgr.saveList.indexOf(cmd) > -1) PacketMgr._setPacketMap(json, UI.file, UI.current);
        }
    },
    
    // 히스토리 패킷 처리
    receiveForSync : function(data, pageNo, isAnimation){
        // // console.log("receiveForSync : " + JSON.stringify(data));
        
        if(typeof(data) == "undefined") return;
        
        // if 큐가 데이터 존재하면 큐에 넣고 break;
        var json = (typeof(data) == "object") ? data : JSON.parse(data);
        var cmd = json.cmd ? json.cmd : json.from ? json.from : "";
        
        // if(cmd == "filechange" || cmd == "changecurrentpage" || cmd == "pensetting" || cmd == "zoom" || cmd == "view") return;
        
        if(cmd == "filechange" || cmd == "changecurrentpage" || cmd == "pensetting" || cmd == "zoom" || cmd == "view" /*  || (cmd == "textbox" && json.id == Ctrl.Text.redrawSkipId) **/ ) return;
        
        // Utils.log("receive UI.rendering : " + UI.rendering);
        /*
         *  펜세팅 싱크를 맞추기 위해 다음과 같은 로직을 돌린다.
         *  	pensetting은 데이터 로드시 penIdxList와 penMap으로 별도 저장하고, packetno로 indexing해서 드로잉 시작전에 마지막 pensetting을 처리한다.		 *
         * 		- filechange와 엮인 부분은 file이 복수개일경우 getPacketListByFile로 가져와서 동기화를 맞춰준다.
         *
         */
        
        
        if(cmd == "began" || cmd == "eraserbegan"){
            // cmd == "pensetting"
            var lastPenSetting = PacketMgr._getLastPen(json.packetno);
            // Utils.log("lastPensetting : " + JSON.stringify(lastPenSetting) + ", pageNo : " + pageNo);
            // Utils.log("UI.rendering : " + UI.rendering + ", PacketMgr.queue.length : " + PacketMgr.queue.length);
            if(lastPenSetting != null){
                if(UI.rendering || PacketMgr.queue.length > 0){
                    PacketMgr.queue.push(lastPenSetting);
                }else{
                    PacketMgr.Command["pensetting"](lastPenSetting);
                }
            }
        }
        
        // UI가 렌더링 중이거나 addFile이 안들어온경우 큐에 저장하고 있는다.
        PacketMgr._runCommand(cmd, json, pageNo);
    },
    
    _runCommand : function(cmd, json, pageNo){
        // Utils.log("_runCommand UI.rendering : " + UI.rendering + ", queue : " + JSON.stringify(PacketMgr.queue));
        if (UI.rendering) {
            // 렌더링 중이라면 큐에 계속 쌓는다.
            PacketMgr.queue.push(json);
            
        } else if(PacketMgr.queue.length > 0){
            
            PacketMgr.queue.push(json);
            PacketMgr.shiftQueue();
            
        } else if(typeof (this.Command[cmd]) != "undefined"){
            // 큐가 비어있는 경우는 그냥 명령 실행
            PacketMgr.Command[cmd](json);
        } else {
            // 드로잉 관련 패킷은 이곳으로 들어오고, 렌더링 중일때는 큐에 쌓았다가 던진다.
            PacketMgr.Master.toCanvasPage(json, pageNo);
        }
    },
    
    setPageScale : function(pageNo){
        var board = UI.skboards[pageNo - 1];
        if(board != null){
            var scale = board.getZoomScale();
            Ctrl.setZoomScale(scale);
        }
    },
    
    /***  fileMap(fileno->isajax, ->pageMap(pageno->isdraw, ->list[packetlist])) */
    loadHistory : function(roomId, fileNo, pageNo, fileCnt, callback){
        // Utils.log('enter load!!');
        // 이미 로딩된 데이터가 있는지 체크한다.
        if(PacketMgr.fileMap != null){
            PacketMgr._clearHistory();
            
            var fileMap = PacketMgr.fileMap.get(fileNo);
            // 이미 request 한적이 있으면 pass
            if(typeof(fileMap) != "undefined" && fileMap != null) return;
        }
        
        // canvas/packet.json
        // 서버에서 파일개수 미리 체크할 예정
        var roomFileCnt = parseInt(fileCnt);
        var getPen = roomFileCnt > 1 ? "1" : "";
        
        var module = $("#rsaModule").val();
        var exponent = $("#rsaExponent").val();
        var uuid = Utils.createUUID().substring(0, 5);
        
        var rsa = new RSAKey();
        rsa.setPublic(module, exponent);
        var tokenStr = rsa.encrypt(roomId + ',' + fileNo + ',' + uuid);
        
        if(tokenStr == null) {
            alert("encrypt fail..");
            return;
        }
        
        var url = Utils.addContext("canvas/packet.json");
        var data = {
            //roomid : roomId,
            //fileno : fileNo,
            token  : tokenStr,
            pageno : pageNo,
            getpen : getPen
        };
        
        PacketMgr.isLoadPacket = true;
        
        Utils.request(url, "json", data, function(json){
            var isFirstLoad = false;
            var fileMap = PacketMgr.fileMap;
            // 파일맵은 무조건 한번 init 해준다.
            if(typeof(fileMap) == "undefined" || fileMap == null){
                isFirstLoad = true;
                fileMap = new Map();
            }
            
            if(json && json.result == 0){
                var list = json.list;
                var len = list == null ? 0 : list.length;
                
                PacketMgr.lastPensetting = json.lastpensetting != null ? json.lastpensetting : null;
                for(var i=0; i<len; i++){
                    var item = list[i];
                    var fileIdx = item.fileno;
                    var pageIdx = item.pageno;
                    var packetIdx = item.packetno;
                    
                    var packet = $.parseJSON(item.packet);
                    
                    var cmd = packet.cmd;
                    if(cmd == "pensetting"){
                        // penIdxList : [],
                        // PacketMgr.penPacketList : null,	//	load로 전달받은 기본 패킷
                        PacketMgr.penIdxList.push(parseInt(packetIdx));
                        if(PacketMgr.penMap == null){
                            PacketMgr.penMap = new Map();
                        }
                        PacketMgr.penMap.put(packetIdx, packet);
                    }
                    
                    // 아래 세가지 패킷은 처리할 필요 없다.
                    if(cmd == "filechange" || cmd == "changecurrentpage" || cmd == "pensetting" || cmd == "view") continue;
                    
                    var fileDataMap = fileMap.get(fileIdx);
                    if(typeof(fileDataMap) == "undefined" || fileDataMap == null) fileDataMap = new Map();
                    
                    var isAjax = fileDataMap.get("isajax");
                    if(typeof(isAjax) == "undefined" || isAjax == null) fileDataMap.put("isajax", true);
                    
                    var pageMap = fileDataMap.get("pagemap");
                    if(typeof(pageMap) == "undefined" || pageMap == null) pageMap = new Map();
                    
                    var packetMap = pageMap.get(pageIdx);
                    if(typeof(packetMap) == "undefined" || packetMap == null) packetMap = new Map();
                    
                    var drawFlag = packetMap.get("isdraw");
                    if(typeof(drawFlag) == "undefined" || drawFlag == null) packetMap.put("isdraw", false);
                    
                    var pagePacketList = packetMap.get("list");
                    if(typeof(pagePacketList) == "undefined" || pagePacketList == null) pagePacketList = [];
                    
                    packet.packetno = packetIdx;
                    pagePacketList.push(packet);
                    
                    packetMap.put("list", pagePacketList);
                    
                    pageMap.put(pageIdx, packetMap);
                    fileDataMap.put("pagemap", pageMap);
                    
                    fileMap.put(fileIdx, fileDataMap);
                }
                
                PacketMgr.fileMap = fileMap;
                
                if(!isFirstLoad && PacketMgr.penIdxList != null && PacketMgr.penIdxList.length > 1){
                    PacketMgr.penIdxList.sort(function(a, b){ return a-b;});
                }
                
                PacketMgr.isLoadPacket = false;
                
                if(callback) callback();
            }
            
        }, function(e){
            // 오류 발생시
            // Utils.log("load history error : " + JSON.stringify(e));
            PacketMgr.isLoadPacket = false;
        }, function(e){
            PacketMgr.isLoadPacket = false;
        });
    },
    
    _setPacketMap : function(packet, fileIdx, pageIdx){
        var cmd = packet.cmd;
        if(cmd == "filechange" || cmd == "changecurrentpage" || cmd == "view" || (cmd == "textbox" && packet.type == "2")) return;
        
        PacketMgr.lastPacketNo++;
        
        var packetIdx = PacketMgr.lastPacketNo;
        if(cmd == "pensetting"){
            PacketMgr.penIdxList.push(parseInt(packetIdx));
            if(PacketMgr.penMap == null){
                PacketMgr.penMap = new Map();
            }
            PacketMgr.penMap.put(packetIdx, packet);
            
            return;
        }
        
        var fileMap = PacketMgr.fileMap;
        if(typeof(fileMap) == "undefined" || fileMap == null){
            isFirstLoad = true;
            fileMap = new Map();
        }
        
        var fileDataMap = fileMap.get(fileIdx);
        if(typeof(fileDataMap) == "undefined" || fileDataMap == null) fileDataMap = new Map();
        
        var isAjax = fileDataMap.get("isajax");
        if(typeof(isAjax) == "undefined" || isAjax == null) fileDataMap.put("isajax", true);
        
        var pageMap = fileDataMap.get("pagemap");
        if(typeof(pageMap) == "undefined" || pageMap == null) pageMap = new Map();
        
        var packetMap = pageMap.get(pageIdx);
        if(typeof(packetMap) == "undefined" || packetMap == null) packetMap = new Map();
        
        var drawFlag = packetMap.get("isdraw");
        if(typeof(drawFlag) == "undefined" || drawFlag == null) packetMap.put("isdraw", false);
        
        var pagePacketList = packetMap.get("list");
        if(typeof(pagePacketList) == "undefined" || pagePacketList == null) pagePacketList = [];
        
        packet.packetno = packetIdx;
        pagePacketList.push(packet);
        packetMap.put("list", pagePacketList);
        
        pageMap.put(pageIdx, packetMap);
        fileDataMap.put("pagemap", pageMap);
        fileMap.put(fileIdx, fileDataMap);
        
        PacketMgr.fileMap = fileMap;
    },
    
    _getLastPen : function(packetNo){
        var lastPacketNo = -1;
        if(PacketMgr.penIdxList != null && PacketMgr.penIdxList.length > 0){
            var len = PacketMgr.penIdxList.length;
            if(packetNo != null && packetNo > -1){
                var packetIdx = parseInt(packetNo);
                for(var i=0; i<len; i++){
                    var dataPacketNo = PacketMgr.penIdxList[i];
                    if(dataPacketNo > packetIdx) break;
                    
                    lastPacketNo = dataPacketNo;
                }
            }else{
                // 없으면 가장 뒤에꺼
                lastPacketNo = PacketMgr.penIdxList[len -1];
            }
        }
        
        return (lastPacketNo > -1) ? PacketMgr.penMap.get(lastPacketNo) : null;
    },
    
    _drawPagePacket : function(fileNo, pageNo, isAnimation){
        // 히스토리가 존재하는지 체크할것
        if(PacketMgr.fileMap != null){
            var fileDataMap = PacketMgr.fileMap.get(fileNo);
            var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
            if(pageMap != null){
                var packetMap = pageMap.get(pageNo);
                if(packetMap != null){
                    var isDraw = packetMap.get("isdraw");
                    var isRender = UI._isRender(pageNo);
                    if(!isDraw && isRender){
                        var pagePacketList = packetMap.get("list");
                        var len = pagePacketList == null ? 0 : pagePacketList.length;
                        for(var i=0; i<len; i++){
                            // 느린 animation
                            var packet = pagePacketList[i];
                            if(isAnimation){
                                PacketMgr.animationQueue.push(packet);
                            }else{
                                PacketMgr.receiveForSync(packet, pageNo, isAnimation);
                            }
                            
                            // 빠른 animation
                            /***
                             var packet = pagePacketList[i];
                             if(isAnimation){
                             // 2015.05.26 퍼포먼스 개선을 위해 이구간에 로직 작업.
                             // // console.log("packet : " + JSON.stringify(packet));
                             
                             PacketMgr.animationQueue.push(packet);
                             setTimeout(function(){
                             if(PacketMgr.animationQueue.length < 1) {
                             Ctrl.Loader.hide();
                             return;
                             }
                             var json = PacketMgr.animationQueue.shift();
                             if(json != null) PacketMgr.receiveForSync(json, pageNo, isAnimation);
                             }, "1");
                             
                             }else{
                             PacketMgr.receiveForSync(packet, pageNo, isAnimation);
                             }
                             ***/
                            
                        }
                        packetMap.put("isdraw", true);
                    }
                }
            }
        }
        
        if(isAnimation) {
            // Ctrl.Loader.show();
            PacketMgr._shiftAnimationQueue(pageNo);
        }
    },
    
    _shiftAnimationQueue : function(pageNo){
        if(PacketMgr.animationQueue.length < 1) {
            Ctrl.Loader.hide();
            return;
        }
        
        var len = PacketMgr.animationQueue == null ? 0 : PacketMgr.animationQueue.length;
        // UI가 렌더링 중일경우 밀린 큐 하나씩 꺼내서 실행
        
        setTimeout(function(){
            var json = PacketMgr.animationQueue.shift();
            PacketMgr.receiveForSync(json, pageNo, true);
            PacketMgr._shiftAnimationQueue(pageNo);
        }, "1");
    },
    
    // 히스토리 초기화 및 id와 일치하는 패킷 삭제 (삭제된 패킷 리턴)
    _removePacket : function(fileNo, pageNo, id, isRedraw){
        var removedPacket = null;
        if(PacketMgr.fileMap != null){
            var fileDataMap = PacketMgr.fileMap.get(fileNo);
            var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
            if(pageMap != null){
                var packetMap = pageMap.get(pageNo);
                if(packetMap != null){
                    var isDraw = packetMap.get("isdraw");
                    var isRender = UI._isRender(pageNo);
                    if(isRender){
                        var pagePacketList = packetMap.get("list");
                        var len = pagePacketList == null ? 0 : pagePacketList.length;
                        for(var i=0; i<len; i++){
                            var packet = pagePacketList[i];
                            // var cmd = lastPacket.cmd ? lastPacket.cmd : lastPacket.from ? lastPacket.from : "";
                            if(packet.cmd == "textbox" && typeof(packet.id) != "undefined" && packet.id == id){
                                removedPacket = pagePacketList[i];
                                pagePacketList = pagePacketList.without(pagePacketList[i]);
                                
                                packetMap.put("list", pagePacketList);
                                break;
                            }
                            // 강제 동작 패킷이므로 유저를 넣지 않는다.
                            // PacketMgr.receiveForSync(packet, pageNo);
                        }
                        if(isRedraw) packetMap.put("isdraw", false);
                    }
                }
            }
        }
        
        return removedPacket;
    },
    
    /****
     _removePacket2 : function(fileNo, pageNo, id){
     var removedPacket = null;
     if(PacketMgr.fileMap != null){
     var fileDataMap = PacketMgr.fileMap.get(fileNo);
     var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
     if(pageMap != null){
     var packetMap = pageMap.get(pageNo);
     if(packetMap != null){
					var isDraw = packetMap.get("isdraw");
					var isRender = UI._isRender(pageNo);
					if(isRender){
     var pagePacketList = packetMap.get("list");
     var len = pagePacketList == null ? 0 : pagePacketList.length;
     for(var i=0; i<len; i++){
     var packet = pagePacketList[i];
     // var cmd = lastPacket.cmd ? lastPacket.cmd : lastPacket.from ? lastPacket.from : "";
     if(packet.cmd == "textbox" && typeof(packet.id) != "undefined" && packet.id == id){
     removedPacket = pagePacketList[i];
     pagePacketList = pagePacketList.without(pagePacketList[i]);
     
     packetMap.put("list", pagePacketList);
     break;
     }
     // 강제 동작 패킷이므로 유저를 넣지 않는다.
     // PacketMgr.receiveForSync(packet, pageNo);
     }
     // packetMap.put("isdraw", false);
					}
     }
     }
     }
     
     return removedPacket;
     },
     ****/
    
    _clearHistory : function(){
        // Utils.log("clear history enter!!!!");
        if(PacketMgr.fileMap != null){
            var fileMap = PacketMgr.fileMap;
            var fileKeys = fileMap.keys();
            var fileLen = fileKeys == null ? 0 : fileKeys.length;
            for(var i=0; i<fileLen; i++){
                var fileIdx = fileKeys[i];
                // request check
                var fileDataMap = PacketMgr.fileMap.get(fileIdx);
                var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
                if(pageMap != null) {
                    var pageKeys = pageMap.keys();
                    var pageLen = pageKeys == null ? 0 : pageKeys.length;
                    
                    for(var j=0; j<pageLen; j++){
                        var pageIdx = pageKeys[j];
                        
                        var packetMap = pageMap.get(pageIdx);
                        if(packetMap != null){
                            packetMap.put("isdraw", false);
                        }
                    }
                }
            }
        }
    },
    
    // 초기 입장시 이전에 저장된 패킷 드로잉
    _drawHistoryPacket : function(fileNo, pageNo, isAnimation, callback){
        // 일단 이부분은 진행자가 아니면 pass
        // if(!PacketMgr.isMC) return;
        if(PacketMgr.isLoadPacket){
            // 로딩끝날떄까지 재귀호출
            setTimeout(function(){
                PacketMgr._drawHistoryPacket(fileNo, pageNo, isAnimation, callback);
            }, "500");
        }
        
        if(PacketMgr.fileMap == null){
            Ctrl.Loader.hide();
            return;
        }
        
        // var drawPacket = function(){
        // Utils.log("drawpacket enter!! PacketMgr.lastPensetting : " + PacketMgr.lastPensetting + ", fileMap : " + PacketMgr.fileMap);
        
        if(PacketMgr.lastPensetting != null){
            var packet = PacketMgr.lastPensetting.packet;
            PacketMgr.Master.toCanvas(packet);
        }
        
        if(PacketMgr.fileMap != null){
            // var pageMap = PacketMgr.fileMap.get(fileNo);
            var fileDataMap = PacketMgr.fileMap.get(fileNo);
            var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
            if(pageMap != null) {
                var pageKeys = pageMap.keys();
                var pageLen = pageKeys == null ? 0 : pageKeys.length;
                
                for(var j=0; j<pageLen; j++){
                    var pageIdx = pageKeys[j];
                    
                    PacketMgr._drawPagePacket(fileNo, pageIdx, isAnimation);
                }
            }else{
                Ctrl.Loader.hide();
            }
        }
        
        if(callback) callback();
        // };
        
        // 함수가 call되는 순간 비동기 동작이 일어난다.
        // drawPacket();
    },
    
    _drawLastZoomPacket : function(fileNo, pageNo){
        // 1. 마지막 페이지의 zoom 을 찾는다.
        // 2. drawing
        
        var lastPacket = null;
        if(PacketMgr.fileMap != null){
            // var pageMap = PacketMgr.fileMap.get(fileNo);
            var fileDataMap = PacketMgr.fileMap.get(fileNo);
            var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
            
            if(pageMap != null) {
                var packetMap = pageMap.get(pageNo);
                if(packetMap != null){
                    var isRender = UI._isRender(pageNo);
                    // if(isRender){
                    var pagePacketList = packetMap.get("list");
                    var len = pagePacketList == null ? 0 : pagePacketList.length;
                    for(var i=len-1; i >= 0; i--){
                        var json = pagePacketList[i];
                        var cmd = json.cmd ? json.cmd : json.from ? json.from : "";
                        if(cmd == "zoom"){
                            lastPacket = json;
                            break;
                        }
                    }
                    // }
                }
            }
        }
        
        // Utils.log("lastPacket : " + JSON.stringify(lastPacket) + ", UI.rendering : " + UI.rendering + ", PacketMgr.queue.length : " + PacketMgr.queue.length );
        
        if(lastPacket != null){
            var cmd = lastPacket.cmd ? lastPacket.cmd : lastPacket.from ? lastPacket.from : "";
            PacketMgr._runCommand(cmd, lastPacket, pageNo);
        }
    },
    
    Command : {
        masterchange : function(packet) {
            var userid = packet.userid;
            
            PacketMgr.masterid = userid;
            PacketMgr.isMC = (userid == PacketMgr.userid) ? true : false;
            
            var isShow = (!PacketMgr.isMC && !PacketMgr.isGuest && (PacketMgr.isCreator || PacketMgr.isAllowMaster)) ? true : false;
            
            Ctrl.Member.masterChange(userid);
            
            // 마스터와 내가 같으면 커서 모양을 맞춰주고 아니면 커서를 초기화 시킨다.
            Ctrl.Cursor.sync(PacketMgr.isMC);
            
            // bgimage auth
            Ctrl.BGImg.auth();
            
            // memo event reset
            Ctrl.Memo.auth();
            
            // slider enable
            Ctrl._usePlugin(PacketMgr.isMC);
            
            // 초기화
            Ctrl.Text.auth();
            
            // pdf viewr 기
            PDFViewer.auth();
            
            // noti go
            var userInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
            // noti
            if(userInfo != null){
                var userNm = userInfo.usernm;
                
                var notiTitle = _msg("noti");
                var notiContent = userNm + _msg("noti.change.host");
                Ctrl.Noti.show(notiTitle, notiContent);
                
                // 상단 진행중 표시
                /*var authContent = userNm + _msg("noti.leading.host");
                 Ctrl.AuthNoti.show(notiTitle, authContent);*/
            }
            
            if(!PacketMgr.isMC){
                // 마스터가 아니면 뷰 모드로 강제 전환
                PacketMgr.Master.changeMode("view");
                
                // hand
                Ctrl.toggleRC(0, -1, false);
                
            }
            
            // 마스터가 바뀐경우 getdata로 마스터가 된사람의 정보를 얻어와야 한
            // 마스터 변경시 마스터 대상자는 현재 상태를 브로드캐스팅 해줘야 한다.
            PacketMgr.Command.getdata();
        },
        
        // 방 개설자가 권한 도로 찾아오는 패킷
        masterwithdraw : function(packet){
            // 진행자가 권한 도로 찾아올때
            PacketMgr.masterid = PacketMgr.creatorid;
            PacketMgr.isMC = (PacketMgr.masterid == PacketMgr.userid) ? true : false;
            
            // Utils.log("PacketMgr.isMC : " + PacketMgr.isMC + ", masterid : " + PacketMgr.masterid + ", PacketMgr.creatorid : " + PacketMgr.creatorid);
            var isShow = (!PacketMgr.isMC && !PacketMgr.isGuest && (PacketMgr.isCreator || PacketMgr.isAllowMaster)) ? true : false;
            
            // Ctrl.toggleMasterChange(isShow, PacketMgr.isMC, PacketMgr.isInitFChange);
            
            Ctrl.Member.masterWithDraw();
            
            // 마스터와 내가 같으면 커서 모양을 맞춰주고 아니면 커서를 초기화 시킨다.
            Ctrl.Cursor.sync(PacketMgr.isMC);
            
            // bgimage auth
            Ctrl.BGImg.auth();
            
            // memo event reset
            Ctrl.Memo.auth();
            
            // slider enable
            Ctrl._usePlugin(PacketMgr.isMC);
            
            // text annotation auth
            Ctrl.Text.auth();
            
            // pdf viewer auth
            PDFViewer.auth();
            
            // 초기화
            // Ctrl.Text.auth();
            
            var userInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
            var userNm = userInfo.usernm;
            
            // noti
            if(userNm != ""){
                var notiTitle = _msg("noti");
                var notiContent = userNm + _msg("noti.change.host");
                Ctrl.Noti.show(notiTitle, notiContent);
                
                // 상단 진행중 표시
                /*var authContent = userNm + _msg("noti.leading.host");
                 Ctrl.AuthNoti.show(notiTitle, authContent);*/
            }
            
            if(!PacketMgr.isMC){
                // 마스터가 아니면 뷰 모드로 강제 전환
                PacketMgr.Master.changeMode("view");
                
                // hand
                Ctrl.toggleRC(0, -1, false);
            }
            
            // 마스터 변경시 마스터 대상자는 현재 상태를 브로드캐스팅 해줘야 한다.
            PacketMgr.Command.getdata();
        },
        
        masterdone : function(){
            // 회의방 종료 처리
            Ctrl.Msg.show(RoomSvr.roomtitle + _msg("noti.end.meeting"));
            
            setTimeout(function(){
                location.href = Utils.addContext("main");
            }, "3000");
        },
        
        changecurrentpage : function(packet, callback) {
            // Utils.log("callback : " + callback);
            var drawHistory = callback ? callback : function(pageNo){
                PacketMgr._drawPagePacket(UI.file, pageNo, false);
                
                // 펜세팅을 마스터의 펜세팅으로 맞춰준다.
                // Utils.log("pen packet 1 : " + JSON.stringify(PacketMgr.Master.curPensetting) );
                PacketMgr.Master.syncPensettingCurrent();
                // Utils.log("pen packet 2 : " + JSON.stringify(PacketMgr.Master.curPensetting) );
                
            };
            
            var currentPage = packet.currentpage;
            UI.changePage(currentPage, drawHistory);
            
            Ctrl.changePageTxt(currentPage);
            
            // 무조건 100프로로 돌린다.
            // Ctrl.setZoomVal(100);
            PacketMgr.setPageScale(currentPage);
        },
        
        chat : function(packet){
            Ctrl.Chat.receive(packet);
        },
        
        pensetting : function(packet) {
            // Utils.log("pensetting enter : " + JSON.stringify(packet));
            PacketMgr.Master.curPensetting = packet;
            PacketMgr.Master.toCanvas(packet);
            
            // 펜세팅은 하지만 마스터가 아니라면 mode는 view모드로 지정해줘야 한다.
            // 펜세팅은 마스터가 아닌경우 view모드로 직접 드로잉은 안되게 한다.
            if(!PacketMgr.isMC){
                PacketMgr.Master.changeMode("view");
                // PacketMgr.Master.toCanvasMode("view");
            }
        },
        
        laserpointer : function(packet) {
            // {"blue":0,"cmd":"laserpointer","green":181,"red":101,"type":0}
            PacketMgr.Master.curPointer = packet;
            PacketMgr.Master.toCanvas(packet);
        },
        
        addfile : function(packet) {
            var len = PacketMgr.pdfs.length;
            var seqno = packet.seqno ? packet.seqno : -1;
            if(seqno == -1){
                // 신규파일이 추가된 경우
                PacketMgr.pdfs.push(packet);
                PDFViewer.addFile(packet);
                Ctrl.addFile(packet);
            }else{
                // 패킷이 없는걸 확인하고 insert 한다.
                if (len == seqno){
                    PacketMgr.pdfs.push(packet);
                    Ctrl.addFile(packet);
                }
            }
        },
        
        filechange : function(packet) {
            // 최초 fileChange는 안올수 있으므로 타이머 체크가 필요하다.
            // UI.fileChange(packet);
            if (!PacketMgr.isInitFChange) {
                PacketMgr.isInitFChange = true;
                PacketMgr.initFile(packet.count);
            } else {
                
                if(UI.file != packet.count) Ctrl.Loader.show();
                var onChangeEnd = function() {
                    // 미리 로딩된 패킷 draw
                    PacketMgr._drawHistoryPacket(packet.count.toString(), 1);
                    // 파일 체인지 다 끝나고 큐에 쌓인거 그려준
                    PacketMgr.shiftQueue();
                    // 마스터인 경우 기본 캔버스 세팅
                    // 마스터 펜 세팅 맞춤
                    PacketMgr.Master.syncPensettingCurrent();
                    
                    Ctrl.changePageTxt(1);
                    
                    // setZoomScale
                    PacketMgr.setPageScale(1);
                    
                    // 상단 파일 select부분 체크
                    Ctrl.selectFile(packet.count);
                    // 무조건 100프로로 돌린다.
                    // Ctrl.setZoomVal(100);
                };
                
                UI.fileChange(packet, onChangeEnd);
                
                // 미리 로딩된 패킷 조회
                PacketMgr.loadHistory(PacketMgr.roomid, packet.count.toString(), 1);
            }
        },
        // 지우개
        erasermode : function(packet) {
            var mode = packet.eraserMode;
            var pageNo = packet.currentPage;
            if (mode == 1) {
                PacketMgr.Master.toCanvas(packet);
            } else {
                PacketMgr.Master.toCanvasPage(packet, pageNo);
            }
            
            Ctrl.Text.removeAll();
        },
        zoom : function(packet) {
            // Utils.log("zoom enter.");
            var scale = packet.scale ? packet.scale : 1;
            UI.scale = scale;
            
            UI.zoomEnd(UI.current, packet);
            
            PacketMgr.setPageScale(UI.current);
            
            Ctrl.BGImg.auth();
            
            Ctrl.Text.auth();
            
            PDFViewer.auth();
            
            // if(scale > 1) Ctrl.Text.cancel(false);
        },
        
        authtype : function(packet) {
            // 공동진행
            // authtype : 0-> 진행안함, 2->진행함
            PacketMgr.isAllowMaster = (packet.type == "1") ? true : false;
            
            // Ctrl.toggleMasterChange(!PacketMgr.isMC);
            
            // 공동진행모드가 false라면, 권한 체인지 안되게 한다.
            var isShow = (!PacketMgr.isMC && !PacketMgr.isGuest && PacketMgr.isAllowMaster) ? true : false;
            // Ctrl.Msg.show("authtype PacketMgr.isMC : " + PacketMgr.isMC + ", PacketMgr.isAllowMaster : " + PacketMgr.isAllowMaster);
            
            // Ctrl.toggleMasterChange(isShow, PacketMgr.isMC, PacketMgr.isInitFChange);
        },
        updateroominfo : function(packet) {
            
            PacketMgr.Master.curRoomInfo = packet;
            
            Ctrl.Room.updateRoomInfo(packet);
        },
        
        img : function(packet){
            Ctrl.BGImg.draw(packet, false);
        },
        
        background : function(packet){
            Ctrl.Background.receive(packet);
        },
        
        comment : function(packet){
            Ctrl.Comment.layer(packet);
        },
        
        memo : function(packet){
            // type-0: add, 1-update, 2-remove
            Ctrl.Memo.receive(packet);
        },
        textbox : function(packet){
            PacketMgr.Master.toCanvasPage(packet, UI.current);
            
            /**
             if(Ctrl.Text.redrawSkipId != "") return;
             ***/
            Ctrl.Text.receive(packet);
        },
        
        poll : function(packet) {
            // TODO 패킷 받아서 클라이언트에 처리하는 코드..
            var type      = packet.type;
            var pollNo    = packet.pollno;
            var timeLimit = packet.timelimit;
            var isCountdown = packet.iscountdown;
            if(type == 'start') {
                // console.log(isCountdown);
                PollCtrl.Action.Attender.makePollSheet(pollNo, timeLimit, isCountdown);
            } else if(type == 'interrupt') {
                PollCtrl.Action.Attender.exitPoll(pollNo);
            } else if(type == 'report') {
                PollCtrl.Action.Common.makePollResult(pollNo, PacketMgr.isMC);
                PollCtrl.UI.open("poll_result_box");
            }
        },
        
        getdata : function(packet){
            // getdata 패킷을 받았지만 나는 마스터가 아니라면 pass 시킨다.
            // Utils.log("getdata : PacketMgr.isMC : " + PacketMgr.isMC + ", PacketMgr.isInitFChange : " + PacketMgr.isInitFChange);
            if(PacketMgr.isMC){
                PacketMgr.Master.syncStatus();
            }
        },
        
        pdf : function(packet){
            PDFViewer.draw(packet);
        }
    },
    
    // 컨트롤에서 Call되는 함수들
    Master : {
        // history 때문에 펜세팅 깨질수 있기 때문에 임시 저장
        masterPensetting : null,
        curPensetting : null,
        curPointer : null,
        curRoomInfo : null,
        syncStatus : function(){
            // 현재 마스터의 펜세팅 적용
            // 문서가 반드시 존재하는 회의만 판서관련 패킷을 sync 한다.
            Ctrl.callCurPensetting(true);
            // 현재 마스터가 뷰 모드 보고있으면, 뷰 모드로 전환해준
            if(Ctrl.isHand() ) PacketMgr.Master.changeMode("view");
            
            // 마지막 줌 패킷
            var percent = parseInt($("#zoomval").val(), 10);
            if(percent > 100){
                this.zoomCurrent(percent, "1");
            }
            
            /**
             var authType = PacketMgr.isAllowMaster ? "2" : "0";
             // 기본 authType 전송
             this.authType(authType);
             **/
            
            // 룸정보 변경
            // Ctrl.callUpdateRoomInfo();
            
            // sync 맞췄을때 마스터가 아니라면 view모드로 강제 전환 시켜줘야한다.
        },
        
        syncPensetting : function(pageNo) {
            // Utils.log("PacketMgr.Master.curPensetting : " + JSON.stringify(PacketMgr.Master.curPensetting) );
            if(PacketMgr.Master.curPensetting == null) {
                // 마스터인 경우, 현재 팬세팅이 없으면 기본 팬세팅으로 해준다.
                return;
            }
            
            if (typeof (pageNo) != "undefined" && pageNo != null) {
                PacketMgr.Master.toCanvasPage(PacketMgr.Master.curPensetting, pageNo);
                var menuSelect = PacketMgr.Master.curPensetting.menuselect;
                if(menuSelect == 6) PacketMgr.Master.toCanvasPage(PacketMgr.Master.curPointer, pageNo);
            } else {
                PacketMgr.Master.toCanvas(PacketMgr.Master.curPensetting);
                var menuSelect = PacketMgr.Master.curPensetting.menuselect;
                if(menuSelect == 6) PacketMgr.Master.toCanvas(PacketMgr.Master.curPointer);
            }
            
            // 현재 뷰 모드라면 펜세팅후 뷰모드로 맞춰준다.
            
            if(!PacketMgr.isMC || Ctrl.isHand() ){
                PacketMgr.Master.changeMode("view");
            }
        },
        
        syncPensettingForce : function(pensetting){
            PacketMgr.Master.curPensetting = pensetting;
            PacketMgr.Master.toCanvas(PacketMgr.Master.curPensetting);
            
            var menuSelect = PacketMgr.Master.curPensetting.menuselect;
            if(menuSelect == 6 && PacketMgr.Master.curPointer) PacketMgr.Master.toCanvas(PacketMgr.Master.curPointer);
        },
        
        syncPensettingCurrent : function(){
            if(PacketMgr.isMC){
                Ctrl.callCurPensetting(false);
            }else{
                if(PacketMgr.Master.masterPensetting != null){
                    PacketMgr.Master.curPensetting = PacketMgr.Master.masterPensetting;
                    PacketMgr.Master.syncPensetting();
                }
            }
        },
        
        changeMode : function(mode) {
            // console.log("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Master Send Change Mode : " + mode);
            PacketMgr.Master.toCanvasMode(mode);
            // UI.Cursor.setMode("pointer");
        },
        
        chat : function(msg, target, targetNm){
            var uuid = Utils.createUUID();
            var uuidTxt = uuid.substring(0, 8);
            var dateTime = Utils.getDate(new Date() );
            
            if(target == "") targetNm = "";
            
            var packet = {"chatid":""+uuidTxt+"","cmd":"chat","from":""+ PacketMgr.userno +"","text":""+msg+"","time":""+dateTime+"","to":""+target+"","type":"user"};
            
            // 진행자가 아니어도 채팅 보낼수 있게 한다.
            this.toRoomForce(packet);
        },
        
        // 펜세팅, 투명펜, 지우게, 레이저포인터 기능 포함
        pensetting : function(isSend, menu, width, r, g, b, alpha, stamp_kind, line_cap, fill) {
            var defaultOpts = {
                "menuselect" : 4,
                "file" : UI.file,
                "page" : UI.current,
                "cmd" : "pensetting",
                "r_color" : 0,
                "g_color" : 0,
                "b_color" : 0,
                "alpha_width" : 0,
                "line_width" : 0,
                "line_cap" : "round",	// round, square
                "fill_r_color" : 255,
                "fill_g_color" : 255,
                "fill_b_color" : 255
            };
            
            
            var options = {};
            /**
             options["menuselect"] = parseInt(menu);
             options["mypen_r_color"] = options["pen_r_color"] = options["r_color"] = r;
             options["mypen_g_color"] = options["pen_g_color"] = options["g_color"] = g;
             options["mypen_b_color"] = options["pen_b_color"] = options["b_color"] = b;
             options["mypen_line_width"] = options["pen_line_width"] = options["line_width"] = width;
             options["mypen_alpha_width"] = options["pen_alpha_width"] = options["alpha_width"] = alpha;
             
             options["stamp_kind"] = stamp_kind;
             **/
            options["menuselect"] = parseInt(menu);
            options["r_color"] = r;
            options["g_color"] = g;
            options["b_color"] = b;
            options["line_width"] = width;
            options["alpha_width"] = alpha;
            options["line_cap"] = line_cap || "round";
            options["fill_r_color"] = fill != null && fill.length > 2 ? fill[0] : 255;
            options["fill_g_color"] = fill != null && fill.length > 2 ? fill[1] : 255;
            options["fill_b_color"] = fill != null && fill.length > 2 ? fill[2] : 255;
            
            $.extend(defaultOpts, options);
            
            PacketMgr.Master.curPensetting = defaultOpts;
            
            this.toCanvas(PacketMgr.Master.curPensetting);
            
            if(isSend) this.toRoom(PacketMgr.Master.curPensetting);
        },
        
        laserpointer : function(type, r, g, b, isSend){
            var defaultOpts = {"cmd":"laserpointer","file":UI.file,"page":UI.current,"type":0,"green":0,"red":255,"blue":0};
            
            var options = {};
            options["type"] = parseInt(type);
            options["red"] = r;
            options["green"] = g;
            options["blue"] = b;
            
            $.extend(defaultOpts, options);
            
            this.curPointer = defaultOpts;
            this.toCanvas(this.curPointer);
            
            if(isSend) this.toRoom(this.curPointer);
        },
        
        // changePage는 화면에 구성할
        changePage : function(pageNo) {
            pageNo = parseInt(pageNo);
            
            // 패킷 페이지 범위를 벗어나면 sendMsg 날리지 않음
            var maxPageNo = parseInt($("#maxPageCnt").html(), 10);
            if(pageNo < 1 || pageNo > maxPageNo) return;
            
            Ctrl.changePageTxt(pageNo);
            
            // 시작전 드로잉 패킷이 있는지 체크한다.
            var drawHistory = function(pageNo){
                // Utils.log("changePage drawHistory UI.file : " + UI.file + ", pageNo : " + pageNo);
                PacketMgr._drawPagePacket(UI.file, pageNo, false);
                // 새로운 페이지 렌더링시 handle모드면 draw 안되게 mode를 set 해줘야 한다.
                
                // 패킷 히스토리가 존재하는경우, 펜세팅 패킷 싱크를 맞춰줘야 한다.
                PacketMgr.Master.syncPensettingCurrent();
                
                var cursor = Ctrl.Cursor.get();
                if(cursor == "hand"){
                    PacketMgr.Master.toCanvasMode("view");
                }
            };
            
            var packet = {"cmd":"changecurrentpage","currentpage":"" + pageNo + "","file":UI.file,"page":pageNo};
            UI.changePage(pageNo, drawHistory);
            
            // 페이지 이동시 줌 초기화
            // Ctrl.setZoomVal(100);
            PacketMgr.setPageScale(pageNo);
            
            /** this.toCanvas(packet); */
            this.toRoom(packet);
        },
        
        fileChange : function(idx){
            var viewerPacket = PDFViewer.pdfs[idx];
            var packet = {"cmd":"filechange","fileno":viewerPacket.fileno,"from":"filechange","userid":""+PacketMgr.userid+"","count":viewerPacket.seqno,"filename":""+viewerPacket.name+"","file":idx,"page":1};
            
            if(UI.file != idx) Ctrl.Loader.show();
            
            var onChangeEnd = function() {
                // 미리 로딩된 패킷 드로우
                PacketMgr._drawHistoryPacket(idx.toString(), 1);
                
                // PacketMgr.Master.toCanvas(PacketMgr.Master.curPensetting);
                // PacketMgr.Master.syncPensetting();
                
                // 파일 체인지 다 끝나고 큐에 쌓인거 그려준
                PacketMgr.shiftQueue();
                
                // masterPensync
                PacketMgr.Master.syncPensettingCurrent();
                
                // 상단 파일 select부분 체크
                Ctrl.selectFile(idx);
                
                // page txt
                Ctrl.changePageTxt(1);
                
                // 페이지 이동시 줌 초기화
                // Ctrl.setZoomVal(100);
                PacketMgr.setPageScale(1);
            };
            
            UI.fileChange(packet, onChangeEnd);
            
            this.toRoom(packet);
            
            // 히스토리 패킷 조회
            PacketMgr.loadHistory(PacketMgr.roomid, idx.toString(), 1);
        },
        
        addFile : function(fileList){
            var lastSeq = PacketMgr.pdfs == null ? 0 : PacketMgr.pdfs.length;
            var fileListLen = fileList == null ? 0 : fileList.length;
            for(var i = 0 ; i < fileListLen ; i++){
                var file = fileList[i];
                var url = file.dnloadurl;
                var hash = url.substring(url.lastIndexOf("/") + 1, url.length);
                var file = fileList[i];
                
                var packet = {"cmd":"addfile","seqno":(lastSeq + i),"fileno":file.fileno,"size":0,"hash":hash,"url":url,"name":file.filename};
                PacketMgr.Master.toRoomCreator(packet);
                
                PacketMgr.Command.addfile(packet);
            }
        },
        
        redo : function(pageNo) {
            var packet = {"cmd":"redo","file":UI.file,"page":UI.current};
            this.toCanvasPage(packet, pageNo);
            this.toRoom(packet);
        },
        
        undo : function(pageNo) {
            var packet = {"cmd":"undo","file":UI.file,"page":UI.current};
            this.toCanvasPage(packet, pageNo);
            this.toRoom(packet);
        },
        
        eraserMode : function(mode, pageNo) {
            // { "cmd" : "erasermode", "eraserMode" : 2, "currentPage" : 1 }
            var packet = {"cmd":"erasermode","eraserMode":mode,"currentPage":pageNo,"file":UI.file,"page":pageNo};
            if (mode == 1) {
                this.toCanvas(packet);
            } else {
                this.toCanvasPage(packet, pageNo);
            }
            
            this.toRoom(packet);
            
            Ctrl.Text.removeAll();
            
            Ctrl.Uploader.checkSaveTimer("erasermode", null);
        },
        
        // --> draw.js에서 발생한 마우스 궤적 이동 이벤트 (**** 개인모드일때는 패킷을 보내면 안된다.)
        draw : function(cmd, menu, pageNo, x, y, fill) {
            // draw cmd : menu4, began, moved, ended
            // eraser cmd : menu5, eraserbegan, erasermoved, eraserended
            // pointer cmd : menu6, cursor, 커서아웃시 -9999, -9999
            var packet = {"cmd":cmd,"menuselect":menu,"locationx":x,"locationy":y,"file":UI.file,"page":pageNo};
            this.toRoom(packet);
            
            if(cmd == "began" || cmd == "ended" || cmd == "eraserbegan" || cmd == "eraserended"){
                Ctrl.Uploader.checkSaveTimer(cmd, null);
            }
        },
        
        zoom : function(percent, settled, callback){
            // 마스터가 아니면 pass
            if (!Ctrl._checkAuth(false)) return;
            
            // zoomin, zoomout 계산.
            var canvas = document.getElementById("sketch" + UI.current);
            var w = $(canvas).width();
            var h = $(canvas).height();
            
            var dx = $(canvas).width() / 2;
            var dy = $(canvas).height() / 2;
            
            var scale =  parseInt(percent) * 0.01;
            if(scale < 1) scale = 1;
            
            var zoomX = 0;
            var zoomY = 0;
            var board = UI.skboards[UI.current - 1];
            if(board != null && scale > 1){
                var beforeScale = board.getZoomScale() > 1 ? board.getZoomScale() : 1;
                var zoomInfo = board.getZoom();
                
                var leftX = UI.getFixedX(w, h, dx);
                var topY = UI.getFixedY(w, h, dy);
                var centerX = (UI.getFixedX(w, h, dx) * scale);
                var centerY = (UI.getFixedY(w, h, dy) * scale);
                var rightX = centerX + ((centerX - leftX));
                var bottomY = centerY + ((centerY - topY));
                
                zoomX = zoomInfo != null && zoomInfo[0] > 0 ? zoomInfo[0] / beforeScale * scale : (UI.getFixedX(w, h, dx) * scale);
                zoomY = zoomInfo != null && zoomInfo[1] > 0 ? zoomInfo[1] / beforeScale * scale : (UI.getFixedY(w, h, dy) * scale);
                
                zoomX = zoomX < leftX ? leftX : zoomX > rightX ? rightX : zoomX;
                zoomY = zoomY < topY ? topY : zoomY > bottomY ? bottomY : zoomY;
                // Utils.log("zoomX : " + zoomX + ", zoomY : " + zoomY + ", leftX : " + leftX + ", topY : " + topY + ", rightX : " + rightX + ", bottomY : " + bottomY + ", centerX : " + centerX + ", centerY : " + centerY);
            }else{
                zoomX = (UI.getFixedX(w, h, dx) * scale);
                zoomY = (UI.getFixedY(w, h, dy) * scale);
            }
            
            // 최소점 벗어나면 더해준다.
            var packet = {"cmd":"zoom","scale":scale,"settled":settled,"x":zoomX,"y":zoomY,"file":UI.file,"page":UI.current};
            var scale = packet.scale ? packet.scale : 1;
            UI.scale = scale;
            
            UI.zoomEnd(UI.current, packet);
            
            Ctrl.BGImg.auth();
            
            Ctrl.Text.auth();
            
            PDFViewer.auth();
            
            this.toRoom(packet);
            
            if(callback) callback();
        },
        
        // syncstatus 용
        zoomCurrent : function(percent, settled){
            var canvas = document.getElementById("pdf" + UI.current);
            var w = $(canvas).width();
            var h = $(canvas).height();
            
            var dx = $(canvas).width() / 2;
            var dy = $(canvas).height() / 2;
            
            var scale = 1;
            var zoomX = 0;
            var zoomY = 0;
            var board = UI.skboards[UI.current - 1];
            if(board != null){
                scale = board.getZoomScale() > 1 ? board.getZoomScale() : (parseInt(percent) * 0.01);
                var zoomInfo = board.getZoom();
                zoomX = zoomInfo != null && zoomInfo[0] > 0 ? zoomInfo[0] : (PDFViewer.getFixedX(w, h, dx) * scale);
                zoomY = zoomInfo != null && zoomInfo[1] > 0 ? zoomInfo[1] : (PDFViewer.getFixedY(w, h, dy) * scale);
            }else{
                zoomX = (PDFViewer.getFixedX(w, h, dx) * scale);
                zoomY = (PDFViewer.getFixedY(w, h, dy) * scale);
            }
            
            var packet = {"cmd":"zoom","scale":scale,"settled":settled,"x":zoomX,"y":zoomY,"file":UI.file,"page":UI.current};
            
            // 룸서버에 호출
            this.toRoom(packet);
        },
        
        
        /***
         * 	1. zoomHandle은 zoom을 시작시 settled 0으로 만들고 1배율에서 해당 scale에 맞게 확대한다.
         *  2. packet에 보내는 x, y좌표는 확대했을때 확대의 중심이되는 x, y 좌표이다.
         *  3. 파라미터 설명
         *  	mode => began, moved, ended -> 시작, 이동, 멈춤
         *  	zoomScale => 현재 적용된 zoom배율
         *  	pageNo => 현재 페이지번호
         *  	hStartX, hstartY => 마우스로 찍은 시작 좌표(마우스 이동거리를 구하기위함)
         *  	pointX, pointY => 현재 마우스 좌표(fixed 처리안됨)
         *  	pointFixedX, pointFixedY => 1024 * 748 or 768 * 1024 표준 배율로 재조정된 좌표(패킷용)
         *  	startZoomX, startZoomY => zoom handling을 한번이라도 한경우,
         * 	4. logic
         * 		시작할때에는 처음에는 canvas의 center를 중심으로 잡고, handling이 한번이라도 일어나면 마우스 다운한 시점의 center좌표(startZoomX, Y)를 중심으로 확대한다.
         * 		이동시에는 minX, maxX값을 구해서 limit를 벗어나지 않게 패킷을 좌표가 벗어나도 예외처리한다.
         * 		Master도 Receiver와 동일하게 드로잉이 진행되게 하기우해 패킷만 생성하고, receivce쪽에서 패킷 좌표에 맞게 드로잉한다.		 *
         */
        zoomHandle : function(mode, zoomScale, pageNo, hStartX, hStartY, pointX, pointY, pointFixedX, pointFixedY, startZoomX, startZoomY){
            // mode, pageNo, fixedX, fixedY
            // Utils.log("mode : " + mode + ", zoomScale : " + zoomScale + ", pageNo : " + pageNo + ", hStartX : " + hStartX + ", hStartY : " + hStartY + ", pointX : " + pointX + ", pointY : " + pointY);
            // fixed 되서 들어오는 좌표들이다.
            
            // 마스터가 아니면 pass
            if (!Ctrl._checkAuth(false)) return;
            
            var canvas = document.getElementById("sketch" + UI.current);
            var w = $(canvas).width();
            var h = $(canvas).height();
            var dx = $(canvas).width() / 2;
            var dy = $(canvas).height() / 2;
            
            var fixedX = UI.getFixedX(w, h, dx) * zoomScale;
            var fixedY = UI.getFixedY(w, h, dy) * zoomScale;
            var centerX = 0;
            var centerY = 0;
            
            if(mode == "began"){
                if(startZoomX > 0 || startZoomY > 0){
                    centerX = startZoomX;
                    centerY = startZoomY;
                }else{
                    centerX = fixedX;
                    centerY = fixedY;
                }
                
            }else if(mode == "moved" || mode == "ended"){
                // 확대모드를 동작시킨다.
                
                var movedX = (hStartX - pointFixedX);
                var movedY = (hStartY - pointFixedY);
                
                var minX = (fixedX) * (zoomScale-1) / zoomScale;
                var minY = (fixedY) * (zoomScale-1) / zoomScale;
                
                if(startZoomX > 0 || startZoomY > 0){
                    
                    // move 가능한 X좌표는 ???
                    var limitLX = fixedX - minX;
                    var limitRX = fixedX + minX;
                    var limitTY = fixedY - minY;
                    var limitDY = fixedY + minY;
                    var termX = startZoomX + movedX;
                    var termY = startZoomY + movedY;
                    
                    centerX = (termX < limitLX) ? limitLX : (termX > limitRX) ? limitRX : (startZoomX + movedX);
                    centerY = (termY < limitTY) ? limitTY : (termY > limitDY) ? limitDY : (startZoomY + movedY);
                    
                }else{
                    if(movedX < 0){
                        movedX = (movedX < -(minX)) ? -(minX) : movedX;
                    }else{
                        movedX = (movedX > minX) ? minX : movedX;
                    }
                    
                    if(movedY < 0){
                        movedY = (movedY < -(minY)) ? -(minY) : movedY;
                    }else{
                        movedY = (movedY > minY) ? minY : movedY;
                    }
                    centerX = fixedX + movedX;
                    centerY = fixedY + movedY;
                }
            }
            
            // 마지막 ended는 실제 pdfjs로 재렌더링 한다.
            var settled = mode == "ended" ? "1" : "0";
            var packet = {"cmd":"zoom","scale":zoomScale,"settled":settled,"x":centerX,"y":centerY,"file":UI.file,"page":pageNo};
            
            UI.zoomEnd(pageNo, packet);
            
            // 마스터만 패킷 보낸다.
            this.toRoom(packet);
        },
        
        masterWithDraw : function(userid){
            var packet = {"cmd":"masterwithdraw"};
            
            // 진행자가 다른사람일수도 있으므로 강제로 브로드케스팅한다.
            this.toRoomForce(packet);
            
            PacketMgr.Command.masterwithdraw(packet);
        },
        
        masterChange : function(userid) {
            var packet = {"cmd":"masterchange","userid":userid};
            // 진행자가 다른사람일수도 있으므로 강제로 브로드케스팅한다.
            this.toRoomForce(packet);
            
            PacketMgr.Command.masterchange(packet);
        },
        
        authType : function(authType){
            this.curAuthType = (authType) ? authType : "1";
            
            // creator가 보내는 패킷
            var packet = {"cmd":"authtype","type":this.curAuthType};
            this.toRoomCreator(packet);
        },
        
        updateRoomInfo : function(authType, chatOpt, cmtOpt, expOpt, title, passwd){
            var curRoomInfo = PacketMgr.Master.curRoomInfo;
            
            // 채팅
            authType = (authType != null) ? authType : (curRoomInfo != null ) ? curRoomInfo.authType : "1";
            chatOpt = (chatOpt != null) ? chatOpt : (curRoomInfo != null ) ? curRoomInfo.chatopt : "1";
            cmtOpt = (cmtOpt != null) ? cmtOpt : (curRoomInfo != null ) ? curRoomInfo.cmtopt : "1";
            expOpt = (expOpt != null) ? expOpt : (curRoomInfo != null ) ? curRoomInfo.expopt : "1";
            title = (title != null) ? title : (curRoomInfo != null ) ? curRoomInfo.title : "";
            passwd = (passwd) != null ? passwd : (curRoomInfo != null ) ? curRoomInfo.passwd : "";
            
            // 패스워드는 재작업 필요
            // creator가 보내는 패킷
            // var packet = {"attendeemic":""+attendeemic+"","chatopt":""+chatOpt+"","cmd":"updateroominfo","dcodeopt":""+dcodeOpt+"","passwd":""+passwd+"","secretfile":""+secretFile+"","title":""+title+"","voice":""+voice+""};
            var packet = {"cmd":"updateroominfo","authtype":""+authType+"","chatopt":""+chatOpt+"","cmtopt":""+cmtOpt+"","expopt":""+expOpt+"", "passwd":""+passwd+"","title":""+title+""};
            PacketMgr.Master.curRoomInfo = packet;
            
            this.toRoomCreator(packet);
            
            Ctrl.Room.updateRoomInfo(packet);
        },
        
        exit : function(flag){
            /**
             var leaveRoomPacket = {"cmd":"leaveroom", "userid":""+PacketMgr.userid+"", "roomid":""+PacketMgr.roomid+"","username":""+PacketMgr.usernm+"","userno":""+PacketMgr.userno+""};
             this.toRoomForce(leaveRoomPacket);
             **/
            
            var leaveUserPacket = {"cmd":"leaveuser", "userid":""+PacketMgr.userid+""};
            this.toRoomForce(leaveUserPacket);
            
            // 생성자고 flag값 존재시
            if(flag){
                var packet = {"cmd":"masterdone"};
                this.toRoomCreator(packet);
                
                // 마스터도 회의방 나가게 처리
                PacketMgr.Command.masterdone(packet);
            }
            
            // 영상회의인경우 나가기
            // Utils.log("PacketMgr.isVideo : " + PacketMgr.isVideo);
        },
        
        // type0:add, 1:remove
        comment : function(type, commentno, userid, userno, usernm, datetime, content, thumbnail){
            commentno = commentno || '';
            userid = userid || '';
            userno = userno || '';
            usernm = usernm || '';
            datetime = datetime || '';
            content = content || '';
            thumbnail = thumbnail || '';
            
            // 04ce24db
            var packet = {"cmd":"comment","commentno":""+commentno+"","userid":""+userid+"","userno":""+userno+"","usernm":""+usernm+"","datetime":""+datetime+"","content":""+content+"","thumbnail": ""+thumbnail+"","type":type};
            
            // 진행자가 아니어도 채팅 보낼수 있게 한다.
            this.toRoomForce(packet);
        },
        
        memo : function(type, memono, seqno, title, content, left, top, r, g, b, fold, ord){
            memono = memono || '';
            seqno = seqno || '';
            userid = PacketMgr.userid || '';
            userno = PacketMgr.userno || '';
            usernm = PacketMgr.usernm || '';
            datetime = Utils.getDate(new Date() ) || '';
            title = title || '';
            content = content || '';
            left = left || 0;
            top = top || 0;
            fold = fold || 1;
            ord = ord || 1;
            
            r = r > -1 ? r : 235;
            g = g > -1 ? g : 235;
            b = b > -1 ? b : 235;
            
            var packet = {"cmd":"memo","type":type,"memono":""+memono+"","seqno":""+seqno+"", "userid":""+userid+"","userno":""+userno+"","usernm":""+usernm+"","datetime":""+datetime+"","title":""+title+"","content":""+content+"","x":""+left+"","y":""+top+"","color_r":""+r+"","color_g":""+g+"","color_b":""+b+"","fold":""+fold+"","ord":""+ord+""};
            this.toRoom(packet);
            
        },
        
        img : function(url, canvas, seqno, scaleW, scaleH, x, y, mode, order, typeFlag, userNm, thumbnail, degree){
            var fixedX = UI.getFixedX(canvas.width, canvas.height, x);
            var fixedY = UI.getFixedY(canvas.width, canvas.height, y);
            typeFlag = typeFlag || "0";
            userNm = userNm || "";
            thumbnail = thumbnail || "";
            degree = degree || "";
            
            scaleW = scaleW || 1;
            scaleH = scaleH || 1;
            
            var packet = {"cmd":"img","mode":mode,"seqno":""+seqno+"","posx":"" + fixedX + "","posy":"" + fixedY + "","scalew":"" + scaleW + "","scaleh":"" + scaleH + "","url": ""+url+ "","ord":"" + order + "","typeflag": typeFlag  + "","usernm":"" + userNm + "","thumbnail":"" + thumbnail + "", "degree":""+ degree +""};
            this.toRoom(packet);
            
            // upload room represent image
            Ctrl.Uploader.checkSaveTimer("img", null);
        },
        
        background : function(bgImg, r, g, b){
            // bgImg -> 1,2,3,4
            var packet = {"cmd":"background","color_r":""+r+"","color_g":""+ g + "","color_b":"" + b + "","bgimg": bgImg};
            this.toRoom(packet);
            
            Ctrl.Uploader.checkSaveTimer("background", "1000");
        },
        
        textbox : function(pageNo, id, type, txt, face, size, r, g, b, w, h, x, y, italic, bold){
            
            if(x == 0 && y == 0) return;
            
            var canvas = document.getElementById("sketch" + UI.current);
            var canvasWidth = $(canvas).width();
            var canvasHeight = $(canvas).height();
            var leftX = UI.getFixedX(canvasWidth, canvasHeight, x);
            var topY = UI.getFixedY(canvasWidth, canvasHeight, y);
            
            var packet = {"cmd":"textbox","type":""+type+"","id":""+id+"","text":""+txt+"","face":""+face+"","size":""+size+"","w":""+w+"","h":""+h+"","x":""+leftX+"","y":""+topY+"","r":""+r+"","g":""+g+"","b":""+b+"","bold":""+bold+"","italic":""+italic+""};
            this.toRoom(packet);
            /**
             if(type == "0"){
             // 추가시에는 canvas에도 적용해주고 화면 처리
             PacketMgr.Command.textbox(packet, pageNo);
             }else{
             // 수정과 삭제는 receive만 동작
             Ctrl.Text.receive(packet);
             }
             **/
            
            PacketMgr.Command.textbox(packet, pageNo);
            
            // upload room represent image
            Ctrl.Uploader.checkSaveTimer("textbox", null);
        },
        
        // 보내는 쪽.. (type = start/interrupt/report)
        poll : function(type, pollNo, timeLimit, isCountdown) {
            var packet = {"cmd":"poll","type":""+type+"","pollno":""+pollNo+"","timelimit":""+timeLimit+"","iscountdown":""+isCountdown+""};
            this.toRoom(packet);
            // toRoom으로 패킷 보낼것..
        },
        
        pdf : function(url, canvas, seqno, scaleW, scaleH, x, y, mode, typeFlag, docPageNo, fileName){
            var fixedX = UI.getFixedX(canvas.width, canvas.height, x);
            var fixedY = UI.getFixedY(canvas.width, canvas.height, y);
            // console.log(" x : " + x + ", y : " + y + ", canvas.width : " + canvas.width + ", canvas.height : " + canvas.height + " , fixedX : " + fixedX + ", fixedY : " + fixedY);
            
            scaleW = scaleW || 1;
            scaleH = scaleH || 1;
            fileName = fileName || '';
            
            var packet = {"cmd":"pdf","mode":mode,"seqno":""+seqno+"","posx":"" + fixedX + "","posy":"" + fixedY + "","scalew":"" + scaleW + "","scaleh":"" + scaleH + "","url": ""+url+ "", "typeflag": ""+typeFlag+"", "docpageno": ""+docPageNo +"", "fname": ""+ fileName +""};
            this.toRoom(packet);
            
            return packet;
        },
        
        // canvas 동기화
        toCanvas : function(packet){
            var len =  UI.skboards != null ? UI.skboards.length : 0;
            for (var i = 0; i < len; i++) {
                UI.skboards[i]["async"](packet);
            }
        },
        
        toCanvasPage : function(packet, pageNo) {
            var idx = pageNo - 1 < 0 ? 0 : pageNo - 1;
            // Utils.log("UI.skboards[idx] : " + UI.skboards[idx] + ", packet : " + packet + ", idx : " + idx);
            if(UI.skboards[idx] != null && packet != null){
                UI.skboards[idx]["async"](packet);
            }
        },
        
        toCanvasMode : function(mode) {
            if(UI.skboards != null && mode != null && mode != ""){
                var len = UI.skboards.length;
                for (var i = 0; i < len; i++) {
                    UI.skboards[i]["setMode"](mode);
                }
            }
        },
        
        toCanvasModePage : function(mode, pageNo) {
            var idx = pageNo - 1 < 0 ? 0 : pageNo - 1;
            // Utils.log("UI.skboards[idx] : " + UI.skboards[idx] + ", idx : " + idx + ", mode : " + mode);
            if(UI.skboards[idx] != null && mode != null && mode != ""){
                UI.skboards[idx]["setMode"](mode);
            }
        },
        
        // 룸서버에 전송
        toRoom : function(packet) {
            // Utils.log("toRoom PacketMgr.isMC : " + PacketMgr.isMC + ", packet : " + JSON.stringify(packet));
            // return;
            // --> 룸서버에 보낼려고 할경우 현재 진행모드 인지 참여자 모드인지 개인모드인지 확인해서 이곳에서 체크할것
            // 마스터일때만 패킷 보낸다.
            // Utils.log("PacketMgr.isMC " + PacketMgr.isMC + ", packet : " + JSON.stringify(packet));
            if (PacketMgr.isMC && packet != null){
                var packetStr = JSON.stringify(packet);
                RoomSvr.send(packetStr);
                
                //-- 히스토리 패킷 저장( draw 패킷은 received할수 없기 때문에 마스터는 패킷을 이곳에서 저장한다.
                var cmd = packet.cmd ? packet.cmd : packet.from ? packet.from : "";
                if(PacketMgr.saveList.indexOf(cmd) > -1){
                    PacketMgr._setPacketMap(packet, UI.file, UI.current);
                }
            }
            
        },
        
        // 권한 변경과 같은 진행자에 상관없이 브로드케스팅 해야되는 패킷은 이 메소드를 사용한다.
        toRoomForce : function(packet){
            if(packet != null) {
                var packetStr = JSON.stringify(packet);
                RoomSvr.send(packetStr);
            }
        },
        
        toRoomCreator : function(packet){
            if(PacketMgr.isCreator && packet != null) {
                var packetStr = JSON.stringify(packet);
                RoomSvr.send(packetStr);
            }
        }
    },
    
    destroy : function(){
        try{
            this.pdfs = null;
            this.queue = null;
            this.penIdxList = null;
            this.penMap = null;
            this.fileMap = null;
            this.lastPensetting = null;
            
        }catch(e){
            
        }
        
    }
};



var Ctrl = {
    // 기본 펜 세팅
    pensetting : null,
    // colorMap : [{r:250,g:20,b:10},{r:250,g:150,b:10},{r:250,g:250,b:10},{r:150,g:250,b:10},{r:10,g:150,b:250},{r:10,g:20,b:150},{r:250,g:20,b:250},{r:0,g:0,b:0},{r:0,g:0,b:0},{r:-1,g:-1,b:-1}],
    // colorMap : [{r:195,g:0,b:0},{r:255,g:130,b:10},{r:255,g:235,b:35},{r:100,g:205,b:50},{r:35,g:95,b:30},{r:0,g:145,b:220},{r:165,g:0,b:185},{r:90,g:90,b:90},{r:90,g:90,b:90},{r:-1,g:-1,b:-1}],
    // pointColorMap : [{r:250,g:20,b:10},{r:250,g:150,b:10},{r:250,g:250,b:10},{r:150,g:250,b:10},{r:10,g:150,b:250},{r:10,g:20,b:150},{r:250,g:20,b:250},{r:0,g:0,b:0},{r:0,g:0,b:0},{r:-1,g:-1,b:-1}],
    // colorMap : [{r:0,g:100,b:250},{r:100,g:0,b:250},{r:250,g:50,b:50},{r:250,g:100,b:0},{r:50,g:50,b:50},{r:0,g:200,b:250},{r:200,g:0,b:250},{r:250,g:100,b:100},{r:250,g:200,b:0},{r:-1,g:-1,b:-1}],
    colorMap : [{r:0,g:100,b:250},{r:100,g:0,b:250},{r:250,g:50,b:50},{r:250,g:100,b:0},{r:50,g:50,b:50},{r:0,g:200,b:250},{r:200,g:0,b:250},{r:250,g:100,b:100},{r:250,g:200,b:0},{r:-1,g:-1,b:-1},{r:-1,g:-1,b:-1}],
    pointColorMap : [{r:0,g:100,b:250},{r:100,g:0,b:250},{r:250,g:50,b:50},{r:250,g:100,b:0},{r:0,g:250,b:100},{r:0,g:200,b:250},{r:200,g:0,b:250},{r:250,g:100,b:100},{r:250,g:200,b:0},{r:0,g:250,b:200}],
    sizeList : [1, 2, 3, 4, 5, 6, 7, 8],
    eSizeList : [4, 6, 8, 10, 12, 14, 16, 18],
    colorIdx : 0,
    hColorIdx : 0,
    pColorIdx : 0,
    lColorIdx : 0,
    sColorIdx : 0,
    cColorIdx : 0,
    pointerIdx : 0,
    lineWidth : 4,
    hLineWidth : 4,
    eLineWidth : 10,
    lLineWidth : 4,
    sLineWidth : 4,
    cLineWidth : 4,
    lineCap : "round",
    mode : "freehand",
    penIdx : 0,
    strokeStyle : null, 	// pen 색이 바뀐경우
    hStrokeStyle : null, 	// 형관펜 style
    lStrokeStyle : null, 	// line style
    sStrokeStyle : null, 	// square style
    cStrokeStyle : null, 	// circle style
    sFillStyle : "#0064FA", // 형관펜 style
    cFillStyle : "#0064FA",
    alpha : 100,
    lalpha : 100,
    salpha : 100,
    calpha : 100,
    mode : 0,
    Modal : {
        bkCallback : null,
        exit : function(type){
            var exitModal = document.getElementById("exitModal");
            if(exitModal){
                var removeMeetingBtn = document.getElementById("removeMeetingBtn");
                var display = (type == "creator") ? "inline-block" : "none";
                removeMeetingBtn.style.display = display;
                
                $(exitModal).show();
            }else{
                var display = (type == "creator") ? "inline-block" : "none";
                var confirmMsg = (type == "creator") ? _msg("msg.exit.admin") : _msg("msg.exit.user");
                var modalHtml = "<div id=\"exitModal\" class=\"popup_dimd on\">\
                <div class=\"popup_box\" style=\"display: block; \">\
                <span class=\"popup_header\">\
                <span class=\"pop_tit\">"+_msg("t.confirmation")+"</span>\
                <a href=\"javascript:Ctrl.Modal.hideExit();\"></a>\
                </span>\
                <div class=\"popup_body\">\
                <span class=\"popup_msg2\">"+confirmMsg+"</span>\
                <div class=\"popbtn_box\">\
                <a href=\"javascript:Ctrl.Modal.hideExit();\" class=\"btn_submit\">"+_msg("cancel")+"</a>\
                <a id=\"removeMeetingBtn\" href=\"javascript:Ctrl.exit(true);\" class=\"btn_submit\" style=\"display:"+display+";\">"+_msg("btn.end")+"</a>\
                <a href=\"javascript:Ctrl.exit(false);\" class=\"btn_submit\">"+_msg("btn.exit")+"</a>\
                </div>\
                </div>\
                </div>\
                </div>"
                
                $(document.body).append(modalHtml);
            }
            
            // if(!Utils.mobile()) Avgrund.show("#exitModal");
            // Ctrl.avgrund(true, "exitModal");
        },
        
        hideExit : function(){
            Ctrl.Modal.hide('exitModal');
            // if(!Utils.mobile()) Avgrund.hide("#exitModal");
            // Ctrl.avgrund(false, "exitModal");
        },
        
        _getBytes : function(title){
            var tcount = 0;
            for (var k=0; k<title.length; k++) {
                onechar = title.charAt(k);
                if (escape(onechar) =='%0D') { } else if (escape(onechar).length > 4) { tcount += 2; } else { tcount++; }
            }
            return tcount;
        },
        
        title : function(){
            if(PacketMgr.userid == PacketMgr.creatorid){
                var titleModal = document.getElementById("titleModal");
                if(titleModal){
                    $(titleModal).show();
                }else{
                    var tcount = Ctrl.Modal._getBytes(RoomSvr.roomtitle);
                    var html = "<div id=\"titleModal\" class=\"popup_dimd on\">\
                    <div class=\"popup_box\">\
                    <span class=\"popup_header\">\
                    <span class=\"pop_tit\">"+_msg("title")+"</span>\
                    <a href=\"javascript:Ctrl.Modal.hideTitle();\"></a>\
                    </span>\
                    <div class=\"popup_body\">\
                    <span class=\"popinput_byte\"><font id=\"ck_byte\">"+tcount+"/40</font></span>\
                    <textarea id=\"popup_title_txt\" class=\"popinput_title\" onkeyup=\"Utils.textCutProcess(this, 40, '', 'ck_byte', 'popup_title_txt');\">"+ RoomSvr.roomtitle +"</textarea>\
                    <div class=\"popbtn_box\">\
                    <a href=\"javascript:Ctrl.Modal.updateTitle();\" class=\"btn_submit\">"+_msg("btn.save")+"</a>\
                    </div>\
                    </div>\
                    </div>\
                    </div>";
                    $(document.body).append(html);
                }
                
                //				if(!Utils.mobile()) Avgrund.show("#titleModal");
                // Ctrl.avgrund(true, "titleModal");
            }
        },
        
        hideTitle : function(){
            Ctrl.Modal.hide('titleModal');
            
            //			if(!Utils.mobile()) Avgrund.hide("#titleModal");
            // Ctrl.avgrund(false, "titleModal");
        },
        
        
        updateTitle : function(){
            var title = $("#popup_title_txt").val();
            if(title.trim() == ""){
                Ctrl.Msg.show(_msg("insert.title"));
                return;
            }
            
            var __getBytes = function(txt){
                var onechar;
                var tmpStr = new String(txt);
                var temp   = tmpStr.length;
                var tcount = 0;
                // byte -> length
                for (var k=0; k<temp; k++) {
                    onechar = tmpStr.charAt(k);
                    if (escape(onechar) =='%0D') { } else if (escape(onechar).length > 4) { tcount += 2; } else { tcount++; }
                }
                
                return tcount;
            }
            
            var tcount = __getBytes(title);
            if(tcount > 40){
                Ctrl.Msg.show(_msg("validation.title"));
                return;
            }
            
            RoomSvr.roomtitle = title;
            Ctrl.Room.update();
            
            Ctrl.Modal.hideTitle();
        },
        
        invite : function(){
            var inviteModal = document.getElementById("inviteModal");
            if(inviteModal){
                $(inviteModal).show();
            }else{
                var title = $("#title").val();
                var mailLink = "mailto:"+PacketMgr.emai + "?subject=" + title;
                var modalHtml = "<div id=\"inviteModal\" class=\"popup_dimd on\">\
                <div class=\"popup_box2\">\
                <span class=\"popup_header\">\
                <span class=\"pop_tit\">"+_msg("t.invite")+"</span>\
                <a onclick=\"Ctrl.Modal.hideInvite();\"></a>\
                </span>\
                <div class=\"popup_body\">\
                <span class=\"popup_msg1\"><b>"+_msg("t.invite.link")+"</b></span>\
                <input id=\"link_input\" type=\"text\" class=\"popinput_direct\" value=\""+location.href+"\" readonly=\"true\" onclick=\"this.select(); \" />\
                <a href=\"javascript:void(0)\" class=\"btn_copy\" onclick=\"Ctrl.Modal.copy();\">Copy</a>\
                <div class=\"btn_sharebox\">\
                <a onclick=\"Ctrl.Mail.openModal()\" class=\"dcode_share1\" >"+_msg("btn.email")+"</a>\
                <a id=\"fbBtn\" class=\"dcode_share2\" href=\"http://www.facebook.com/sharer/sharer.php?u="+location.href+"\" onclick=\"javascript:window.open(this.href, '', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=300,width=600'); return false;\">Facebook</a>\
                <a id=\"googleBtn\" href=\"https://plus.google.com/share?url="+location.href+"\" class=\"dcode_share3\" title=\"google plus\" onclick=\"javascript:window.open(this.href, '', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=420,width=500'); return false;\" data-service=\"google\" data-action=\"+1\" target=\"_blank\">Google +</a>\
                </div>\
                </div>\
                </div>\
                </div>";
                
                $(document.body).append(modalHtml);
                
                this.select();
            }
            
            //			if(!Utils.mobile()) Avgrund.show("#inviteModal");
            // Ctrl.avgrund(true, "inviteModal");
        },
        
        hideInvite : function(){
            
            Ctrl.Modal.hide('inviteModal');
            
            //			if(!Utils.mobile()) Avgrund.hide("#inviteModal");
            // Ctrl.avgrund(false, "inviteModal");
        },
        
        copy : function(){
            if(Utils.browser("msie")){
                var link = $("#link_input").val();
                window.clipboardData.setData("Text", link);
                Ctrl.Msg.show(_msg("invite.copy.ie"));
            }else{
                Ctrl.Msg.show(_msg("invite.copy.other"));
            }
            // Ctrl.Msg.show ( "주소가 복사되었습니다. \'Ctrl+V\'를 눌러 붙여넣기 해주세요.");
            $("#link_input").select();
        },
        
        select : function(){
            $("#link_input").select();
        },
        
        login : function(){
            $("#loginModal").show();
            
            //			if(!Utils.mobile()) Avgrund.show("#loginModal");
            // Ctrl.avgrund(true, "loginModal");
        },
        
        confirm : function(msg, callback){
            var modalHtml = "<div id=\"cfmModal\" class=\"popup_dimd on\">\
            <div class=\"popup_box\" style=\"display: block;\">\
            <span class=\"popup_header\">\
            <span class=\"pop_tit\">"+_msg("t.confirmation")+"</span>\
            <a href=\"javascript:Ctrl.Modal.destroyConfirm();\"></a>\
            </span>\
            <div class=\"popup_body\">\
            <span class=\"popup_msg2\">"+msg+"</span>\
            <div class=\"popbtn_box\">\
            <a href=\"javascript:Ctrl.Modal.destroyConfirm();\" class=\"btn_submit\">"+_msg("cancel")+"</a>\
            <a href=\"javascript:void(0);\" class=\"btn_submit btn_confirm\">"+_msg("btn.confirm")+"</a>\
            </div>\
            </div>\
            </div>\
            </div>";
            
            $(document.body).append(modalHtml);
            
            //			if(!Utils.mobile()) Avgrund.show("#cfmModal");
            // Ctrl.avgrund(true, "cfmModal");
            
            $(".btn_confirm", "#cfmModal").click(function(){
                if(callback) callback();
                
                Ctrl.Modal.destroyConfirm();
            });
            
        },
        
        destroyConfirm : function(){
            $(".btn_confirm", "#cfmModal").unbind("click");
            $("#cfmModal").remove();
            
            //			if(!Utils.mobile()) Avgrund.hide("#cfmModal");
            // Ctrl.avgrund(false, "cfmModal");
        },
        
        toggleLogin : function(type){
            if(type == "guest"){
                $("#account_info").hide();
                $("#guest_info").show();
            }else{
                $("#account_info").show();
                $("#guest_info").hide();
            }
        },
        
        hide : function(id){
            $("#" + id).hide();
        },
        
        password : function(callback) {
            var passwdModal = document.getElementById("passwdModal");
            if(passwdModal){
                $(passwdModal).show();
                
                //			 	if(!Utils.mobile()) Avgrund.show("#passwdModal");
                // Ctrl.avgrund(true, "passwdModal");
                
            }else{
                var modalHtml = "<div id=\"passwdModal\" class=\"popup_dimd on\">\
                <div class=\"popup_box\" style=\"display: block;\">\
                <span class=\"popup_header\">\
                <span class=\"pop_tit\">"+_msg('m.password.title')+"</span>\
                </span>\
                <div class=\"popup_body\">\
                <div id=\"password_info\">\
                <span class=\"popup_msg1\"><b>"+_msg('m.password.msg.1')+"</b><br />"+_msg('m.password.msg.2')+"</span>\
                <span id=\"ck_byte3\" class=\"popinput_byte\"><font>0</font> / 6</span>\
                <input id=\"room_password\" type=\"password\" class=\"popinput_name\" placeholder=\""+_msg('m.password.placeholder')+"\" maxlength=\"6\" onkeyup=\"Utils.textCutProcess(this, 6, '', 'ck_byte3', 'room_password');\" />\
                <div class=\"popbtn_box\">\
                <a href=\"javascript:location.href=Utils.addContext('main');\" class=\"btn_cancel\">"+_msg('m.password.btn.home')+"</a>\
                <a href=\"javascript:void(0);\" class=\"btn_submit\">"+_msg('m.password.btn.enter')+"</a>\
                </div>\
                </div>\
                </div> \
                </div>\
                </div>";
                
                $(document.body).append(modalHtml);
                
                //				if(!Utils.mobile()) Avgrund.show("#passwdModal");
                // Ctrl.avgrund(true, "passwdModal");
                
                $(".btn_submit", "#passwdModal").click(function(){
                    CanvasApp.submitPassword(function(){
                        //						if(!Utils.mobile()) Avgrund.hide("#passwdModal");
                        // Ctrl.avgrund(false, "passwdModal");
                        
                        $("#passwdModal").val();
                        callback();
                    });
                });
                
                $("#room_password").keydown(function(e){
                    if(e.keyCode == '13'){
                        CanvasApp.submitPassword(function(){
                            //							if(!Utils.mobile()) Avgrund.hide("#passwdModal");
                            // Ctrl.avgrund(false, "passwdModal");
                            callback();
                        });
                    }
                })
            }
        },
        
        network : function(){
            var networkModal = document.getElementById("networkModal");
            if(networkModal){
                $(networkModal).show();
            }else{
                var layer = "<div id=\"networkModal\" class=\"popup_dimd on\">\
                <div class=\"popup_box\" style=\"display: block;\">\
                <span class=\"popup_header\" >\
                <span class=\"pop_tit\">"+_msg("m.network.msg.1") +"</span>\
                <a href=\"javascript:location.reload();\"></a>\
                </span>\
                <div class=\"popup_body\">\
                <span class=\"popup_msg2\">"+_msg("m.network.msg.2") +"</span>\
                <div class=\"popbtn_box\">\
                <a href=\"javascript:location.reload();\" class=\"btn_submit\">"+_msg("m.network.btn.1") +"</a>\
                <a href=\"javascript:Ctrl.exit(false);\" class=\"btn_cancel\">"+_msg("m.network.btn.2") +"</a>\
                </div>\
                </div>\
                </div>\
                </div>";
                
                $(document.body).append(layer);
            }
        },
        
        destroy : function(){
            var cfmModal = document.getElementById("cfmModal");
            if(cfmModal){
                $(".btn_confirm", "#cfmModal").unbind("click");
            }
            
            var passwdModal = document.getElementById("passwdModal");
            if(passwdModal){
                $(".btn_submit", "#passwdModal").unbind("click");
                
                $("#room_password").unbind("keydown");
            }
            
        }
    },
    
    Uploader : {
        // fileList : [],
        id : "file1",
        id_pdf : "file_pdf",
        allowList : ["jpg","jpeg","png","gif","bmp"],
        progWidth : 300,
        limitCnt : 4,
        readyCmdList : ["began", "eraserbegan"],	// canvas 저장 요청 command
        saveCmdList : ["ended", "eraserended", "img", "textbox", "erasermode", "background"],	// canvas 저장 요청 command
        isWait : false,
        timer : null,
        auth : false,
        
        loopTime : "10000",
        progressing : false,
        /*******/
        checkSaveTimer : function(cmd, loopTime){
            /***
             *  드로잉이 끝났을때, Text가 끝났을때 10초 딜레이 후에 저장한다. 이때, 딜레이 사이에 이벤트가 들어오면 10초 연장한다.
             */
            // ready는 무조건 대기상태로만 만든다. (드로잉 시작, 지우게 시작)
            if(Ctrl.Uploader.readyCmdList.indexOf(cmd) > -1){
                Ctrl.Uploader.isWait = true;
                return;
            }
            
            // save리스트는 실제 저장요청이고, 기존에 타이머가 돌고 있으면, 삭제 시킨다.
            if(Ctrl.Uploader.saveCmdList.indexOf(cmd) > -1){
                if(Ctrl.Uploader.timer != null) clearTimeout(Ctrl.Uploader.timer);
            }else{
                return;
            }
            
            var loopTime = (typeof(loopTime) == "undefined" || loopTime == null) ? Ctrl.Uploader.loopTime : loopTime;
            
            Ctrl.Uploader.isWait = false;
            // 10초후 재동작
            Ctrl.Uploader.timer = setTimeout(Ctrl.Uploader.uploadRoomRep, loopTime);
        },
        
        uploadRoomRep : function(){
            if(Ctrl.Uploader.isWait) {
                if(Ctrl.Uploader.timer != null) {
                    clearTimeout(Ctrl.Uploader.timer);
                    Ctrl.Uploader.timer = null;
                }
                return;
            }
            
            // 다운로드 없이 저장만 ...
            UI.skboards[0].save();
            var saveCanvas = $("#saveCanvas").get(0);
            try{
                if(saveCanvas){
                    Ctrl.Uploader.save(saveCanvas.toDataURL() );
                    $(saveCanvas).remove();
                }
            }catch(e){
                console.log(e);
                if(saveCanvas) $(saveCanvas).remove();
            }
        },
        /*** end save canvas  **/
        
        setProgress : function(){
            // progress setting
            var scrollTop = $("#" + UI.CONATINER).scrollTop();
            var left = ($("#" + UI.VIEWER).width() / 2) - (this.progWidth / 2);
            var top = 300;
            
            var nPercent = 0;
            var nPercentTxt = "0%";
            
            $("#circle").css({position:"absolute",zIndex:"9999",left:left + "px",top:top + "px"});
            $('#circle').progressCircle({
                nPercent        : nPercent,
                showPercentText : nPercentTxt,
                thickness       : 3,
                circleSize      : this.progWidth
            });
        },
        
        progress : function(percent){
            if(percent == 100){
                $("#circle").hide();
            }else{
                $("#circle").show();
            }
            
            var nPercent = percent;
            var nPercentTxt = nPercent + "%";
            $('#circle').progressCircle({
                nPercent        : nPercent,
                showPercentText : nPercentTxt,
                thickness       : 3,
                circleSize      : this.progWidth
            });
        },
        
        checkImgExt : function(){
            var ext = document.getElementById(this.id).value; //파일을 추가한 input 박스의 값
            ext = ext.slice(ext.lastIndexOf(".") + 1).toLowerCase(); //파일 확장자를 잘라내고, 비교를 위해 소문자로 만듭니다.
            
            return (Ctrl.Uploader.allowList.indexOf(ext) > -1);
        },
        
        checkPdfExt : function(){
            var ext = document.getElementById(this.id_pdf).value; //파일을 추가한 input 박스의 값
            ext = ext.slice(ext.lastIndexOf(".") + 1).toLowerCase(); //파일 확장자를 잘라내고, 비교를 위해 소문자로 만듭니다.
            
            return ext == "pdf";
        },
        
        download : function(imgData){
            var url = Utils.addContext(_url("canvas.download"));
            var params = {
                title : RoomSvr.roomtitle,
                data : imgData
            };
            
            $("#saveform").attr("action", url);
            
            $("#title").val(RoomSvr.roomtitle);
            $("#imageData").val(imgData);
            $("#saveform").submit();
        },
        
        save : function(imgData){
            var url = Utils.addContext(_url("upload.save"));
            var params = {
                roomid : RoomSvr.roomid,
                title : RoomSvr.roomtitle,
                data : imgData
            };
            
            $.ajax({
                type : 'post',
                url : url,
                data : params,
                async : true,
                cache : false,
                dataType : "json",
                //success : onsuccess,
                success : function(ret){
                    // // console.log("success : " + JSON.stringify(ret));
                },
                beforeSend : function(xhr){
                    // xhr.setRequestHeader("Cache-Control", "no-cache");
                    // xhr.setRequestHeader("X-File-Name", file.fileName);
                    // xhr.setRequestHeader("X-File-Size", file.fileSize);
                    // xhr.setRequestHeader('Content-Type', 'application/json');
                },
                error : function(e){
                    // // console.log("error : " + JSON.stringify(e));
                },
                complete : function(ret){
                    // // console.log("complete : " + JSON.stringify(ret));
                },
            });
        },
        
        send : function(imgData){
            var count = Ctrl.BGImg.getCnt();
            if(count > this.limitCnt){
                Ctrl.Msg.show(_msg("check.file.count"));
                return;
            }
            
            // var imgData = $("#temp").attr("src");
            var url = Utils.addContext(_url("upload.image"));
            var params = {
                roomid : RoomSvr.roomid,
                title : RoomSvr.roomtitle,
                data : imgData
            };
            
            Utils.request(url, "json", params, function(data){
                var json = data;
                var result = json.result;
                
                if(result == 0){
                    var list = json.list;
                    var len = list == null ? 0 : list.length;
                    if(len > 0){
                        Ctrl.BGImg.init(list[0]);
                    }
                } else {
                    Ctrl.Msg.show(_msg("upload.fail"));
                }
            });
        },
        
        
        
        /**
         upload : function(e, id){
         Ctrl.Uploader.id = id;
         
         var files = e.target.files || e.dataTransfer.files;
         var uploadURL = Utils.addContext(_url("upload"));
         var uploadFile = (files && files.length > 0) ? files[0] : null;
         if(uploadFile == null){
         Ctrl.Msg.show(_msg("file.not.found"));
         return;
         };
         
         if(!this.checkImgExt()){
         Ctrl.Msg.show(_msg("check.file.img"));
         return;
         }
         
         var count = Ctrl.BGImg.getCnt();
         if(count > this.limitCnt){
         Ctrl.Msg.show(_msg("check.file.count"));
         return;
         }
         
         var formData = new FormData();
         formData.append(Ctrl.Uploader.id, uploadFile);
         formData.append('roomid', PacketMgr.roomid);
         formData.append('typeflag', '0');
         
         $.ajax({
         xhr: function() {
         var xhrobj = $.ajaxSettings.xhr();
         if (xhrobj.upload) {
         xhrobj.upload.addEventListener('progress', function(event) {
         var percent = 0;
         var position = event.loaded || event.position;
         var total = event.total;
         if (event.lengthComputable) {
         percent = Math.ceil(position / total * 100);
         }
         // set progress
         // status.setProgress(percent);
         Ctrl.Uploader.progress(percent);
         
         }, false);
         }
         return xhrobj;
	        },
	        url: uploadURL,
         type: "POST",
         contentType:false,
         dataType:"json",
         processData: false,
         cache: false,
         data: formData,
         success: function(data){
         // 초기화
         if(Utils.browser("msie")){
         $("#" + id).replaceWith( $("#" + id).clone(true) );
         }else{
         $("#" + id).val("");
         }
         
         // var json = $.parseJSON(data);
         var json = data;
         var result = json.result;
         if(result == 0){
         var list = json.list;
         var len = list == null ? 0 : list.length;
         if(len > 0){
         Ctrl.BGImg.init(list[0]);
         }
         
         } else if(result == -904){
         Ctrl.Msg.show(_msg("upload.size.overflow.fail"));
         
         } else {
         Ctrl.Msg.show(_msg("upload.fail"));
         }
         }
         });
         },
         ***/
        
        
        uploadImg : function(e, id){
            
            Ctrl.Uploader.id = id;
            
            var files = e.target.files || e.dataTransfer.files;
            var uploadFile = (files && files.length > 0) ? files[0] : null;
            if(uploadFile == null){
                Ctrl.Msg.show(_msg("file.not.found"));
                return;
            };
            
            if(!this.checkImgExt()){
                Ctrl.Msg.show(_msg("check.file.img"));
                return;
            }
            
            var count = Ctrl.BGImg.getCnt();
            if(count > this.limitCnt){
                Ctrl.Msg.show(_msg("check.file.count"));
                return;
            }
            
            
            this.upload(e, id, uploadFile, RoomSvr.roomid, '0', function(data){
                // upload
                Ctrl.BGImg.init(data);
            })
            
            /**
             var formData = new FormData();
             formData.append(Ctrl.Uploader.id, uploadFile);
             formData.append('roomid', PacketMgr.roomid);
             formData.append('typeflag', '0');
             
             $.ajax({
	            xhr: function() {
	            var xhrobj = $.ajaxSettings.xhr();
	            if (xhrobj.upload) {
             xhrobj.upload.addEventListener('progress', function(event) {
             var percent = 0;
             var position = event.loaded || event.position;
             var total = event.total;
             if (event.lengthComputable) {
             percent = Math.ceil(position / total * 100);
             }
             // set progress
             // status.setProgress(percent);
             Ctrl.Uploader.progress(percent);
             
             }, false);
             }
	            return xhrobj;
             },
             url: uploadURL,
             type: "POST",
             contentType:false,
             dataType:"json",
             processData: false,
             cache: false,
             data: formData,
             success: function(data){
             // 초기화
             if(Utils.browser("msie")){
             $("#" + id).replaceWith( $("#" + id).clone(true) );
             }else{
             $("#" + id).val("");
             }
             
             // var json = $.parseJSON(data);
             var json = data;
             var result = json.result;
             if(result == 0){
             var list = json.list;
             var len = list == null ? 0 : list.length;
             if(len > 0){
		        			Ctrl.BGImg.init(list[0]);
             }
             
             } else if(result == -904){
             Ctrl.Msg.show(_msg("upload.size.overflow.fail"));
             
             } else {
             Ctrl.Msg.show(_msg("upload.fail"));
             }
             }
             });
             ***/
        },
        
        
        uploadPdf : function(e, id){
            Ctrl.Uploader.id_pdf = id;
            
            var files = e.target.files || e.dataTransfer.files;
            var uploadFile = (files && files.length > 0) ? files[0] : null;
            if(uploadFile == null){
                Ctrl.Msg.show(_msg("file.not.found"));
                return;
            };
            
            if(!this.checkPdfExt()){
                Ctrl.Msg.show(_msg("check.file.pdf"));
                return;
            }
            
            if(this.progressing || PDFViewer.initializing){
                Ctrl.Msg.show(_msg("check.file.pdf.init"));
                return;
            }
            
            this.progressing = true;
            PDFViewer.blocked = false;
            this.upload(e, id, uploadFile, RoomSvr.roomid, 'p', function(data){
                Ctrl.Uploader.progressing = false;
                PDFViewer.init(data);
            })
        },
        
        upload : function(e, id, uploadFile, roomId, typeFlag, callback){
            var uploadURL = Utils.addContext(_url("upload"));
            var formData = new FormData();
            formData.append(id, uploadFile);
            formData.append('roomid', roomId);
            formData.append('typeflag', typeFlag);
            
            $.ajax({
            xhr: function() {
                var xhrobj = $.ajaxSettings.xhr();
                if (xhrobj.upload) {
                    xhrobj.upload.addEventListener('progress', function(event) {
                        var percent = 0;
                        var position = event.loaded || event.position;
                        var total = event.total;
                        if (event.lengthComputable) {
                            percent = Math.ceil(position / total * 100);
                        }
                        // set progress
                        // status.setProgress(percent);
                        Ctrl.Uploader.progress(percent);
                        
                    }, false);
                }
                return xhrobj;
            },
            url: uploadURL,
            type: "POST",
            contentType:false,
            dataType:"json",
            processData: false,
            cache: false,
            data: formData,
            success: function(data){
                // 초기화
                if(Utils.browser("msie")){
                    $("#" + id).replaceWith( $("#" + id).clone(true) );
                }else{
                    $("#" + id).val("");
                }
                
                // var json = $.parseJSON(data);
                var json = data;
                var result = json.result;
                if(result == 0){
                    // Ctrl.BGImg.init(list[0]);
                    var list = json.list;
                    var len = list == null ? 0 : list.length;
                    if(len > 0){
                        callback(list[0]);
                    }
                    
                } else if(result == -904){
                    Ctrl.Msg.show(_msg("upload.size.overflow.fail"));
                    
                } else {
                    Ctrl.Msg.show(_msg("upload.fail"));
                }
            },
                fail : function(){
                    Ctrl.Uploader.progressing = false;
                }
            });
        }
    },
    
    Msg : {
        show : function(msg, callback){
            $("#top_noti").remove();
            
            var html = "<div id=\"top_noti\" class=\"top_noti\">" + msg + "</div>";
            $(html).insertBefore( $("#hmenubar") );
            
            if(callback) callback();
        },
        hide : function(){
            // $("#notiWrap").hide("slow");
            Ctrl.Noti.isProgress = false;
            
            $("#top_noti").remove();
        },
        close : function(){
            // $("#notiWrap").hide("slow");
            Ctrl.Noti.isProgress = false;
        },
        
        auth : function(isForce){
            var msg = _msg("not.allow");
            Utils.log(isForce + " " + PacketMgr.isGuest + " " + PacketMgr.isAllowMaster);
            if(!isForce && (PacketMgr.isGuest || !PacketMgr.isAllowMaster)){
                this.show(msg);
                return;
            }
            
            var authModal = document.getElementById("authModal");
            if(authModal){
                $(authModal).show();
                
            }else{
                var authHtml = "<div id=\"authModal\" class=\"popup_dimd on\">\
                <div class=\"popup_box\" style=\"display: block;\">\
                <span class=\"popup_header\">\
                <span class=\"pop_tit\">"+_msg("m.auth.title")+"</span>\
                <a href=\"javascript:Ctrl.Msg.authHide();\"></a>\
                </span>\
                <div class=\"popup_body\">\
                <span class=\"popup_msg4\">"+_msg("m.auth.msg.1")+"</span>\
                <div class=\"take_rights\">\
                <a href=\"javascript:Ctrl.Member.authByModal();\" class=\"btn_duplex\"></a>\
                <span>"+_msg("m.auth.msg.2")+"</span>\
                </div>\
                <div class=\"popbtn_box\">\
                <a href=\"javascript:Ctrl.Msg.authHide();\" class=\"btn_ok\">"+_msg("m.auth.ok")+"</a>\
                </div>\
                </div>\
                </div>\
                </div>";
                
                $(document.body).append(authHtml);
            }
            
            // modal animation
            //			if(!Utils.mobile()) Avgrund.show("#authModal");
            // Ctrl.avgrund(true, "authModal");
        },
        
        wait : function(){
            
            
        },
        
        authHide : function(){
            // 2015.11.16  modal animation
            //			if(!Utils.mobile()) Avgrund.hide("#authModal");
            // Ctrl.avgrund(false, "authModal");
            
            $("#authModal").hide();
        },
        
        destroy : function(){
            
        }
    },
    
    /**
     AuthNoti : {
     isProgress : false,
     show : function(title, msg){
     //			if(this.isProgress) return;
     
     if(msg == "") return;
     
     $("#authWrap").show("slow");
     
     $("#authContent").html(msg);
     },
     hide : function(){
     $("#authWrap").hide("slow");
     Ctrl.AuthNoti.isProgress = false;
     },
     close : function(){
     $("#authWrap").hide("slow");
     Ctrl.AuthNoti.isProgress = false;
     },
     destroy : function(){
     
     }
     },
     **/
    
    // 참여자 및 권한 관련 하단 메시지 뷰
    Noti : {
        isProgress : false,
        show : function(title, msg){
            //			if(this.isProgress) return;
            
            if(msg == "") return;
            
            // 노티 중복체크 필요
            this.isProgress = true;
            
            $("#notiContent").html(msg);
            
            $("#notiWrap").show("slow", function(){
                setTimeout(Ctrl.Noti.hide, "2000");
            });
            
        },
        hide : function(){
            $("#notiWrap").hide("slow");
            Ctrl.Noti.isProgress = false;
        },
        close : function(){
            $("#notiWrap").hide("slow");
            Ctrl.Noti.isProgress = false;
        },
        destroy : function(){
            
        }
    },
    
    Loader : {
        show : function(){
            $("#loading").show();
        },
        hide : function(){
            $("#loading").hide();
        },
        toggle : function(){
            $("#loader").toggle();
        },
        showVideo : function(){
            $("#videoLoader").show();
        },
        hideVideo : function(){
            $("#videoLoader").hide();
        },
        toggleVideo : function(){
            $("#videoLoader").toggle();
        }
    },
    
    Cursor : {
        // mode_hand, mode_pen, mode_hpen, mode_del
        list : ["hand", "hpen", "del", "diagram", "text"],
        change : function(type){
            //  mode_hpen, mode_del, mode_pen, mode_hand, mode_diagram
            var modeStr = "mode_" +  (Utils.browser("msie") ? "ie_" : "") + type;
            
            $("#docWrapper").attr("class", "");
            
            $("#docWrapper").addClass(modeStr);
        },
        
        sync : function(isMC){
            var cursor = "";
            if(isMC){
                cursor = Ctrl.Cursor.get();
            }
            Ctrl.Cursor.change(cursor);
        },
        
        get : function(){
            cursor = Ctrl.isHand() ? "hand" : (this.penIdx == 1) ? "hpen" : (this.penIdx == 2) ? "hpen" : (this.penIdx == 3) ? "del" : (this.penIdx == 5 || this.penIdx == 6 || this.penIdx == 7) ? "diagram" : "";
            return cursor;
        }
    },
    
    // 채팅 영역
    Chat : {
        chatBadgeCnt : 0,
        flag : 0,
        toggle : function(){
            $("#chat_wrapper").slideToggle({easing:"easeInCubic", duration:800});
            /**
             if(this.flag == 0){
             $("#chat_wrapper").show();
             $("#chat_wrapper").css('-moz-transform', 'rotateX(180deg)')
             .css('-webkit-transform', 'rotateX(180deg)')
             .css('-o-transform', 'rotateX(180deg)')
             .css('transform', 'rotateX(180deg)')
             .css('ms-transform', 'rotateX(180deg)')
             
             this.flag = 1;
             }else{
             // $("#chat_wrapper").hide();
             $("#chat_wrapper").css('-moz-transform', 'rotateX(0deg)')
             .css('-webkit-transform', 'rotateX(0deg)')
             .css('-o-transform', 'rotateX(0deg)')
             .css('transform', 'rotateX(0deg)')
             .css('ms-transform', 'rotateX(0deg)')
             
             this.flag = 0;
             }
             **/
        },
        addMember : function(userNo){
            // 나와 같은 유저면 패스
            if(userNo == PacketMgr.userno) return;
            
            var userInfo = Ctrl.Member.getUserOnline(userNo, "userno");
            var userNm = userInfo.usernm;
            var userId = userInfo.userid;
            var isExist = false;
            $("option", "#chattarget").each(function(){
                var attenderId = $(this).attr("userid");
                if(attenderId == userId) isExist = true;
            });
            // Utils.log("isExist : " + isExist + ", userNo : " + userNo + ", userNm : " + userNm);
            if(!isExist){
                var option = "<option value=\""+userNo+"\" userid=\""+userId+"\">"+userNm+"</option>";
                $("#chattarget").append(option);
            }
        },
        
        removeMember : function(userId){
            $("option", "#chattarget").each(function(){
                var attenderId = $(this).attr("userid");
                if(attenderId == userId) $(this).remove();
            });
            
        },
        
        pad : function(num){
            num = num + '';
            return num.length < 2 ? '0' + num : num;
        },
        
        // 보낼때 화면 드로잉
        send : function(msg, target, targetNm){
            var today = new Date();
            var hour = today.getHours();
            var minute = today.getMinutes();
            var amPmTxt = hour > 11 ? "PM" : "AM";
            hour = hour % 12;
            hour = hour ? hour : 12; // the hour '0' should be '12'
            
            // if(target != "") targetNm = targetNm;
            if(targetNm == "전체") targetNm = "";
            
            var toHtml = "";
            if(target != ""){
                toHtml = "<span class=\"chat_target\">"+targetNm+"</span><span class=\"chat_to\">To :</span>";
            }
            
            var html = "<div class=\"chat_sender\">\
            <span class=\"chat_time\">"+ amPmTxt + " " + hour + ":" + Ctrl.Chat.pad(minute) + "</span>" + toHtml + "\
            <span class=\"chat_tail\"></span>\
            <span class=\"chat_text\">"+Utils.escapeLinkString(msg)+"</span>\
            </div>"
            
            
            $("#chat_cont").append(html);
            
            $("#chatmsg").val("");
            
            var chatObj = document.getElementById("chat_cont");
            chatObj.scrollTop = chatObj.scrollHeight;
        },
        // 받을때 화면 드로잉
        receive : function(packet){
            var name = packet.name;
            var chatid = packet.chatid;
            var time = packet.time;
            var text = packet.text;
            var from = packet.from;
            var to = packet.to;
            
            var hour = parseInt(time.substring(8, 10));
            var minute = time.substring(10, 12);
            var amPmTxt = hour > 11 ? "PM" : "AM";
            hour = hour % 12;
            hour = hour ? hour : 12; 	// the hour '0' should be '12'
            
            var userInfo = Ctrl.Member.getUserOnline(from, "userno");
            var userNm = userInfo.usernm;
            var thumbnail = userInfo.thumbnail;
            
            // 귓속말 pass
            if(to != ""){
                if(to == RoomSvr.userno){
                    userNm = userNm + _msg("noti.private.msg");
                }else{
                    return;
                }
            }else{
                // from => packet.from
            }
            
            // 외부에서 채팅 메시지 보낼수 잇기 때문에 추가 한다. (guest)
            userNm = (userNm == "") ? from : userNm;
            
            var html = "<div class=\"chat_receiver\">\
            <span class=\"chat_user_name\">"+userNm+"</span>\
            <span class=\"chat_time\">"+ amPmTxt + " " + hour + ":" + minute + "</span>\
            <img class=\"chat_user_photo\" src=\""+thumbnail+"\" />\
            <span class=\"chat_tail\"></span>\
            <span class=\"chat_text\">"+Utils.escapeLinkString(text)+"</span>\
            </div>"
            
            $("#chat_cont").append(html);
            
            var chatObj = document.getElementById("chat_cont");
            chatObj.scrollTop = chatObj.scrollHeight;
            
            var notiTitle = _msg("noti");
            var notiContent = "<a href='javascript:Ctrl.Chat.toggle();'>" + userNm + _msg("noti.send.msg") + "</a>";
            
            // 채팅 메뉴가 열려있으면 노티창을 보이게 하지 않는다.
            // Utils.log("menuOff : " + $("#confContainer").hasClass("menuOff") + ", chat : " + $("#chat_wrap").hasClass("openMenu"));
            if($("#chat_wrapper").css("display") == "none"){
                Ctrl.Noti.show(notiTitle, notiContent);
                Ctrl.Chat.chatBadgeCnt++;
                $(".btn_chat").addClass("checked");
                $("#chat_badge").show();
                $("#chat_badge").text(Ctrl.Chat.chatBadgeCnt);
            }
        }
    },
    
    Member : {
        // 뷰에서 호출
        list : null,
        init : function(){
            
            var userInfo = CanvasApp.info.user;
            var list = [];
            var len = userInfo == null ? 0 : userInfo.length;
            for(var i=0; i<len; i++){
                var map = userInfo[i];
                map.token = Utils.createUUID().substring(0, 8);
                
                list.push(map);
            }
            
            if(CanvasApp.info.userid == CanvasApp.info.userno){
                /**
                 this.info.userid = this.info.deviceid;
                 this.info.userno = this.info.deviceid;
                 this.info.usernm = guestName + "("+_msg("guest") +")";
                 **/
                var currentUserInfo = {
                    userid : CanvasApp.info.deviceid,
                    userno : CanvasApp.info.deviceid,
                    usernm : "",
                    token : Utils.createUUID().substring(0, 8)
                };
                
                list.push(currentUserInfo);
            }
            
            
            this.list = list;
        },
        
        get : function(key, type){
            var len = this.list == null ? 0 : this.list.length;
            
            for(var i=0; i<len; i++){
                var userInfo = this.list[i];
                if(userInfo[type] == key){
                    return userInfo;
                    break;
                }
            }
            return null;
        },
        
        _add : function(userId, userName, userNo, isGuest, thumbnail){
            var isExist = false;
            var len = this.list == null ? 0 : this.list.length;
            for(var i=0; i<len; i++){
                var userInfo = this.list[i];
                if(userInfo.userid == userId){
                    isExist = true;
                }
            }
            
            if(!isExist){
                this.list.push({
                    thumbnail : thumbnail,
                    email : "",
                    userid : userId,
                    userno : userNo,
                    usernm : userName,
                    token : Utils.createUUID().substring(0, 8)
                })
            }
        },
        
        _remove : function(userId){
            if(this.list == null) return;
            
            var len = this.list == null ? 0 : this.list.length;
            for(var i=0; i<len; i++){
                var userInfo = this.list[i];
                if(userInfo.userid == userId){
                    this.list = this.list.without(userInfo);
                    break;
                }
            }
            
        },
        
        auth : function(thisNode){
            // authtype이 0이면 기능 안되게 처리한다.
            var iconNm = thisNode.className;
            this.authChange(iconNm, thisNode, true);
        },
        
        authByModal : function(){
            if(PacketMgr.isGuest){
                //				if(!Utils.mobile()) Avgrund.hide("#authModal");
                // Ctrl.avgrund(false, "authModal");
                $("#authModal").hide();
                return;
            }
            
            $(".userinfo", "#user_wrapper").each(function(){
                var userId = $(this).attr("userid");
                var layer = $("span", $(this)).eq(1);
                // layer.removeClass("get_authority").removeClass("send_authority");
                if(userId != null && userId == PacketMgr.userid){
                    var iconNm = $(layer).get(0).className;
                    Ctrl.Member.authChange(iconNm, null, false);
                }
            });
            
            //			if(!Utils.mobile()) Avgrund.hide("#authModal");
            // Ctrl.avgrund(false, "authModal");
            
            $("#authModal").hide();
        },
        
        authChange : function(iconNm, thisNode, isConfirm){
            if(iconNm != "opener"){
                if(!PacketMgr.isAllowMaster){
                    Ctrl.Msg.show(_msg("msg.master.change.limit"));
                    return;
                }
            }
            
            if(iconNm == "opener"){
                // 생성자만 권한회수 가능
                if(PacketMgr.creatorid != PacketMgr.userid) return;
                
                // if(!confirm(_msg("confirm.get.auth"))) return;
                
                if(isConfirm){
                    Ctrl.Modal.confirm(_msg("confirm.get.auth"), function(){
                        // 내가 마스터면 pass
                        var userId = $(thisNode.parentNode).attr("userid");
                        PacketMgr.Master.masterWithDraw(userId);
                    });
                    
                }else{
                    PacketMgr.Master.masterWithDraw(PacketMgr.userid);
                }
                
                
            }else if(iconNm == "get_authority"){
                // if(!confirm(_msg("confirm.allow.auth"))) return;
                
                if(isConfirm){
                    Ctrl.Modal.confirm(_msg("confirm.allow.auth"), function(){
                        var userId = $(thisNode.parentNode).attr("userid");
                        PacketMgr.Master.masterChange(userId);
                    });
                }else{
                    PacketMgr.Master.masterChange(PacketMgr.userid);
                }
                
            }else if(iconNm == "send_authority"){
                // if(!confirm(_msg("confirm.allow.auth"))) return;
                
                if(isConfirm){
                    Ctrl.Modal.confirm(_msg("confirm.allow.auth"), function(){
                        var userId = $(thisNode.parentNode).attr("userid");
                        PacketMgr.Master.masterChange(userId);
                    });
                }else{
                    PacketMgr.Master.masterChange(PacketMgr.userid);
                }
                
            }else if(iconNm == "chairman"){
                // pass
                
            }else{
                // pass
            }
            
        },
        
        getUserOnline : function(key, type){
            return Ctrl.Member.get(key, type);
        },
        
        moveMasterLayer : function(userId){
            var masterLayer = document.getElementById("user_master");
            if(masterLayer){
                masterLayer.id = "";
                masterLayer.className = "user_box userinfo";
                $("#attender_list").append(masterLayer);
            }
            
            $(".userinfo", "#attender_list").each(function(){
                var attenderId = $(this).attr("userid");
                if(attenderId != null && attenderId == userId){
                    var isExistCreator = document.getElementById("user_creator");
                    if(isExistCreator){
                        $(this).insertAfter("#user_creator");
                    }else{
                        $(this).insertAfter("#user_header");
                    }
                    $(this).attr("id", "user_master");
                    $(this).attr("class", "user_box_chairman userinfo");
                }
            });
        },
        
        // command에서 호출 마스터가 권한 찾아옴
        masterWithDraw : function(){
            
            // 마스터 모드 일경우
            if(PacketMgr.isCreator){
                // Utils.log("i'm master");
                $(".chairman", "#user_wrapper").each(function(){
                    $(this).removeClass("chairman");
                    
                    if(PacketMgr.isAllowMaster) {
                        var layer = $("span", $(this)).eq(1);
                        layer.addClass("send_authority");
                    }
                });
                
                $(".userinfo", "#user_wrapper").each(function(){
                    var userId = $(this).attr("userid");
                    var userNo = $(this).attr("userno");
                    
                    var layer = $("span", $(this)).eq(1);
                    layer.removeClass("get_authority").removeClass("send_authority");
                    
                    if(userId != null && userId != PacketMgr.userid){
                        if(PacketMgr.isAllowMaster && userId != userNo) {
                            layer.addClass("send_authority")
                        }
                    }
                });
                
            }else{
                // Utils.log("i'm not master");
                $(".chairman", "#user_wrapper").removeClass("chairman");
                
                $(".userinfo", "#user_wrapper").each(function(){
                    var userId = $(this).attr("userid");
                    if(userId != null && userId == PacketMgr.creatorid){
                        $("span", $(this)).eq(1).addClass("chairman");
                    }
                    
                    if(userId != null && userId == PacketMgr.userid){
                        var layer = $("span", $(this)).eq(1);
                        layer.removeClass("send_authority");
                        
                        if(PacketMgr.isAllowMaster && !PacketMgr.isGuest) {
                            layer.addClass("get_authority");
                        }else{
                            layer.removeClass("get_authority");
                        }
                    }
                });
            }
            
            this.moveMasterLayer(PacketMgr.creatorid);
        },
        
        // command에서 호출
        masterChange : function(userId){
            // masterChange
            // $(".chairman", "#attender_wrap").removeClass("chairman");
            // 마스터모드 일경우 권한줄수 있는 버튼으로 바껴야 한다.
            
            // chairman -> master,
            // 내가 생성자일때
            if(PacketMgr.isCreator){
                // 나에게 권한이 돌아온경우
                if(PacketMgr.creatorid == userId){
                    // 바뀐사람이 생성자라면 ?
                    $(".chairman", "#user_wrapper").each(function(){
                        $(this).removeClass("chairman");
                        // $(this).addClass("get_authority");
                        if(PacketMgr.isAllowMaster) {
                            var layer = $("span", $(this)).eq(1);
                            layer.addClass("send_authority");
                        }
                    });
                    
                }else{
                    $(".chairman", "#user_wrapper").each(function(){
                        $(this).removeClass("chairman");
                        
                        if(PacketMgr.isAllowMaster) {
                            var layer = $("span", $(this)).eq(1);
                            layer.addClass("send_authority");
                        }
                    });
                    
                    $(".userinfo", "#user_wrapper").each(function(){
                        var attenderId = $(this).attr("userid");
                        if(attenderId != null && attenderId == userId){
                            var ctrl = $("span", $(this)).eq(1);
                            ctrl.removeClass("get_authority").removeClass("send_authority");
                            ctrl.addClass("chairman");
                            
                        }else if(attenderId != null && attenderId != PacketMgr.creatorid){
                            var attenderId = $(this).attr("userid");
                            var attenderNo = $(this).attr("userno");
                            
                            // 방 생성자 화면에서 권한 주는거 초기화
                            if(PacketMgr.isAllowMaster && attenderId != attenderNo){
                                var layer = $("span", $(this)).eq(1);
                                layer.addClass("send_authority");
                            }else{
                                var layer = $("span", $(this)).eq(1);
                                layer.removeClass("send_authority");
                            }
                        }
                    });
                }
                
            }else{
                // 내가 일반 유저 일때
                $(".chairman", "#user_wrapper").removeClass("chairman");
                
                $(".userinfo", "#user_wrapper").each(function(){
                    var attenderId = $(this).attr("userid");
                    
                    if(attenderId != null && attenderId == userId){
                        var layer = $("span", $(this)).eq(1);
                        layer.removeClass("get_authority").removeClass("send_authority");
                        layer.addClass("chairman");
                    }
                });
                
                // 내가 일반 접속자일경우 마스터 권한을 가질수 잇게 한다.
                if(!PacketMgr.isGuest && PacketMgr.userid != userId){
                    $(".userinfo", "#user_wrapper").each(function(){
                        var attenderId = $(this).attr("userid");
                        if(attenderId != null && attenderId == PacketMgr.userid){
                            var layer = $("span", $(this)).eq(1);
                            layer.removeClass("send_authority");
                            if(PacketMgr.isAllowMaster) {
                                layer.addClass("get_authority");
                            }else{
                                layer.removeClass("get_authority");
                            }
                        }
                        
                    });
                }
            }
            
            this.moveMasterLayer(userId);
        },
        
        authTypeChange : function(type){
            if(PacketMgr.isCreator && PacketMgr.masterid == PacketMgr.userid){
                this.masterWithDraw(PacketMgr.creatorid);
            }else{
                this.masterChange(PacketMgr.masterid);
            }
        },
        
        newUser : function(userId, userName, userNo, isGuest, thumbnail){
            // 게스트가 아니라면 count - 1
            var isExist = false;
            $(".userinfo", "#user_wrapper").each(function(){
                var memberNo = $(this).attr("userno");
                if(memberNo != null && memberNo == userNo){
                    isExist = true;
                }
            });
            
            if(!isExist){
                var profileUrl = thumbnail;
                var status = PacketMgr.masterid == userId ? _msg("progressing"): isGuest == "1" ? _msg("attending") + "(GUEST)" : _msg("attending");
                // var funcImg = PacketMgr.creatorid == userId ? ""
                
                var userFlag = "";
                var html = "";
                var authCls = "";
                // 1. 입장한 사람이 마스터 일때
                if(PacketMgr.creatorid == userId){
                    // <%=(isCreator) ? "opener" : (masterUser == null) ? "chairman" : ""
                    
                    // 진행자 모드일씨 마스터 버튼, 참여자 모드일시 대상이 진행자면
                    authCls = (PacketMgr.isCreator) ? "opener" : (PacketMgr.masterid == userId) ? "chairman" : "";
                    
                    userFlag = "1";
                }else if(PacketMgr.masterid == userId){
                    // 생성자가 아니면서 진행자인경우
                    authCls = "chairman";
                    userFlag = "2";
                }else{
                    // 마스터 모드일경우 진행권한을 받을수 있게 처리, 참여모드일경우 공백처리
                    authCls = isGuest == "1" ? "" : (PacketMgr.isCreator && PacketMgr.isAllowMaster) ? "send_authority" : (PacketMgr.userid == userId && PacketMgr.isAllowMaster) ? "get_authority" : "";
                    userFlag = "0";
                }
                
                var divClass = userFlag == "1" ? "user_box_opener" : userFlag == "2" ? "user_box_chairman" : "user_box";
                var subLayer = userFlag == "1" ? "opener" : userFlag == "2" ? "chairman" : "";
                var wrapperId = userFlag == "1" ? "user_creator" : userFlag == "2" ? "user_master" : "";
                
                var html = "<div id=\""+wrapperId+"\" class=\""+divClass+" userinfo\" userno=\""+userNo+"\" userid=\""+ userId+"\">\
                <img class=\"user_photo\" src=\""+profileUrl+"\" />\
                <span class=\"user_name\">"+userName+"</span>\
                <span class=\""+authCls+"\" onclick=\"Ctrl.Member.auth(this);\"></span>\
                </div>";
                
                if(userFlag == "1"){
                    $(html).insertAfter("#user_header");
                    
                }else if(userFlag == "2"){
                    var isExistCreator = document.getElementById("user_creator");
                    if(isExistCreator){
                        $(html).insertAfter("#user_creator");
                    }else{
                        $(html).insertAfter("#user_header");
                    }
                }else{
                    $("#attender_list").append(html);
                }
                
                // var beforeCnt = parseInt($("#attendCnt").html(), 10);
                var beforeCnt = $(".userinfo", "#user_wrapper").length;
                $("#attendCnt").html(beforeCnt);
                
                
                var isFirstUser = false;
                
                // $("dd", "#attender_wrap").each(function(){
                $(".userinfo", "#user_wrapper").each(function(idx){
                    var memberNo = $(this).attr("userno");
                    if(idx == 0 && userNo == memberNo){
                        isFirstUser = true;
                    }
                });
                
            }
            
            // member 추가
            Ctrl.Member._add(userId, userName, userNo, isGuest, thumbnail);
            
            // 체팅 selectbox 갱신
            Ctrl.Chat.addMember(userNo);
            
            // 나와 같으면 pass
            if(PacketMgr.userid != userId){
                var notiTitle = _msg("noti");
                var notiContent = userName + _msg("noti.enter");
                
                Ctrl.Noti.show(notiTitle, notiContent);
            }else{
                /*if(PacketMgr.isMC){
                 var notiTitle = _msg("noti");
                 var authContent = userName + _msg("noti.leading.host");
                 Ctrl.AuthNoti.show(notiTitle, authContent);
                 }*/
            }
        },
        
        leaveUser : function(userId, userName, userNo){
            
            var isFirstUser = false;
            
            var beforeCnt = 0;
            // $("dd", "#attender_wrap").each(function(){
            $(".userinfo", "#user_wrapper").each(function(idx){
                var memberNo = $(this).attr("userno");
                
                if(idx == 0 && userNo == memberNo){
                    isFirstUser = true;
                }
                
                if(memberNo != null && memberNo == userNo){
                    $(this).remove();
                }else{
                    beforeCnt++;
                }
            });
            
            $("#attendCnt").html(beforeCnt);
            
            // 멤버 삭제
            Ctrl.Member._remove(userId);
            
            // 체팅 selectbox 갱신
            Ctrl.Chat.removeMember(userId);
            
            if(PacketMgr.userid != userId){
                var notiTitle = _msg("noti");
                var notiContent = userName + _msg("noti.exit.meeting");
                
                Ctrl.Noti.show(notiTitle, notiContent);
            }
            
            if(PollCtrl.isProgress && PollCtrl.progressPoll != null)
                PollCtrl.Action.Attender.exitPoll(PollCtrl.progressPoll);
        }
    },
    
    Comment : {
        id : "commentWrapper",
        id_box : "commentBox",
        id_add : "cmt_add_btn",
        // id_fold : "cmt_fold_btn",
        id_open : "cmt_open_btn",
        id_hide : "cmt_hide_btn",
        id_close : "cmt_close_btn",
        id_more : "cmt_more_btn",
        id_more_cnt : "cmt_more_cnt",
        id_content : "cmt_content",
        id_widget : "cmt_widget",
        id_body : "cmt_body",
        id_empty : "cmt_cmpty",
        id_input : "cmt_input",
        current : 1,
        ROWS : 10,
        left : 50,
        top : 900,
        isProc : false,
        init : function(){
            this.setEvent();
            
            var plugin = CanvasApp.info.plugin;
            PacketMgr.isCommentPos = plugin.comment == "1" ? true : false;
            PacketMgr.isMemoPos = plugin.memo == "1" ? true : false;
            
            if(!PacketMgr.isCommentPos){
                var sketch = UI._getSketch(1);
                this.top = document.body.scrollHeight - 300;
            }
        },
        setEvent : function(){
            // 이동은 되데 저장은 하지 않게 수정
            $("#" + this.id).draggable({
            handle: ".comment_header",
            containment: $('#docWrapper'),
                start : function(e){
                },
                drag : function(e, ui){
                },
                stop : function(e, ui){
                    
                    var left = $(this).position().left;
                    var top = $(this).position().top;
                    if(!PacketMgr.isGuest){
                        var w = $("#" + UI.WRAPPER).width();
                        var h = $("#" + UI.WRAPPER).height();
                        var w2 = $("#" + Ctrl.Comment.id_widget).width();
                        var h2 = $("#" + Ctrl.Comment.id_widget).height();
                        
                        var posX = (left > (w - w2)) ? (w - w2) : left;
                        var posY = (top > (h + 10)) ? (h + 10) : top;
                        
                        if(posX < 0) posX = 50;
                        if(posY < 0) posY = 300;
                        
                        var url = Utils.addContext(_url("comment.update.pos"));
                        var params = {
                            roomid : PacketMgr.roomid,
                            posx : posX,
                            posy : posY,
                            typeflag : "0"
                        };
                        Utils.request(url, "json", params,  function(json){
                            Utils.log("update position result : " + json.result);
                        });
                    }
                }
            });
            
            // add
            $("#" + this.id_add).click(Ctrl.Comment.add);
            
            // enter
            $("#" + this.id_content).keydown(function(e){
                if(e.keyCode == '13'){
                    Ctrl.Comment.add();
                }
            });
            
            // fold
            $("#" + this.id_open).click(Ctrl.Comment.fold);
            
            // fold
            $("#" + this.id_hide).click(Ctrl.Comment.fold);
            
            // close
            $("#" + this.id_close).click(function(){
                Ctrl.Comment.toggle();
            });
            
            // more
            $("#" + this.id_more).click(Ctrl.Comment.more);
            
            
            $("#" + this.id_content).focus(Ctrl.Comment.show);
        },
        
        add : function(){
            // 중복답변 처리 필요
            if(Ctrl.Comment.isProc) return;
            
            if(!PacketMgr.isAllowComment && !PacketMgr.isMC){
                // Ctrl.Msg.show(_msg("not.allow"));
                Ctrl.Msg.show(_msg("not.allow"));
                
                $("#" + Ctrl.Comment.id_content).val("");
                return;
            }
            
            var txt = $("#" + Ctrl.Comment.id_content).val();
            if(txt.trim() == ""){
                Ctrl.Msg.show(_msg("insert.comment"));
                $("#" + Ctrl.Comment.id_content).focus();
                return;
            }
            
            Ctrl.Comment.show();
            
            var deviceId = Utils.getDevice();
            var url = Utils.addContext(_url("comment.add"));
            var params = {
                roomid : PacketMgr.roomid,
                deviceid : deviceId,
                content : txt,
                left : Ctrl.Comment.left,
                top : Ctrl.Comment.top
            };
            
            
            Ctrl.Comment.isProc = true;
            Utils.request(url, "json", params,  function(json){
                if(json.result == 0){
                    // $("#" + Ctrl.Comment.id_empty).hide();
                    
                    $("#" + Ctrl.Comment.id_body).removeClass("empty");
                    
                    $("#" + Ctrl.Comment.id_content).val("");
                    
                    var map = json.map;
                    PacketMgr.Master.comment("0", map.commentno, map.userid, map.userno, map.usernm, map.cdatetime, map.content, map.thumbnail);
                    
                    Ctrl.Comment.list();
                    
                }else if(json.result == -102){
                    Ctrl.Msg.show(_msg("comment.add.fail.auth"));
                }else{
                    Ctrl.Msg.show(_msg("comment.add.fail"));
                }
                
                Ctrl.Comment.isProc = false;
                
            }, function(){
                Ctrl.Msg.show(_msg("comment.add.fail"));
                Ctrl.Comment.isProc = false;
            });
        },
        
        remove : function(thisNode, commentNo){
            // "comment.remove.confirm"
            // if(!confirm(_msg("comment.remove.confirm"))) return;
            
            if(Ctrl.Comment.isProc) return;
            
            Ctrl.Modal.confirm(_msg("comment.remove.confirm"), function(){
                
                Ctrl.Comment.isProc = true;
                var url = Utils.addContext(_url("comment.remove"));
                var params = {
                    roomid : PacketMgr.roomid,
                    commentno : commentNo
                };
                
                Utils.request(url, "json", params,  function(json){
                    if(json.result == 0){
                        PacketMgr.Master.comment("1", commentNo);
                        
                        Ctrl.Comment.list();
                    }else{
                        Ctrl.Msg.show(_msg("comment.remove.fail"));
                    }
                    Ctrl.Comment.isProc = false;
                }, function(){
                    Ctrl.Comment.isProc = false;
                });
            });
            
        },
        
        toggle : function(){
            // $("#" + this.id_add).show();
            var wrapper = "#" + Ctrl.Comment.id;
            /**
             if(isForce){
             }else{
             $(wrapper).slideToggle({easing:"easeInCubic", duration:800});
             }
             **/
            $(wrapper).slideToggle({easing:"easeInCubic", duration:800});
            // $("#chat_wrapper").slideToggle({easing:"easeInCubic", duration:800});
        },
        
        fold : function(){
            var id = "#" + Ctrl.Comment.id_widget;
            var idBody = "#" + Ctrl.Comment.id_body;
            var container = "#" + Ctrl.Comment.id;
            
            if($(container).hasClass("comment_box_mini")){
                $(container).removeClass("comment_box_mini");
            }else{
                $(container).addClass("comment_box_mini");
            }
        },
        
        show : function(){
            var container = "#" + Ctrl.Comment.id;
            if($(container).hasClass("comment_box_mini")){
                $(container).removeClass("comment_box_mini");
            }
            
        },
        
        list : function(){
            $("#"  + Ctrl.Comment.id_more_cnt).remove();
            
            // refresh
            $(".comm_box", "#" + Ctrl.Comment.id_body).remove();
            $("#cmt_more_btn", "#" + Ctrl.Comment.id_body).remove();
            
            Ctrl.Comment.current = 1;
            
            var url = Utils.addContext(_url("comment.list"));
            var params = {
                roomid : PacketMgr.roomid,
                pageno : Ctrl.Comment.current
            }
            
            Utils.request(url, "html", params,  function(html){
                var htmlTxt = html.trim();
                
                // $(htmlTxt).insertBefore($("#" + Ctrl.Comment.id_more));
                $("#" + Ctrl.Comment.id_body).append(htmlTxt);
                
                var moreCnt = $("#" + Ctrl.Comment.id_more_cnt).val();
                if(moreCnt > Ctrl.Comment.ROWS){
                    $("#" + Ctrl.Comment.id_more).show();
                }else{
                    $("#" + Ctrl.Comment.id_more).hide();
                }
            });
        },
        
        more : function(){
            $("#"  + Ctrl.Comment.id_more_cnt).remove();
            
            $("#"  + Ctrl.Comment.id_more).remove();
            
            var url = Utils.addContext(_url("comment.list"));
            var params = {
                roomid : PacketMgr.roomid,
                pageno : ++Ctrl.Comment.current
            }
            
            Utils.request(url, "html", params,  function(html){
                var htmlTxt = html.trim();
                // $(htmlTxt).insertBefore($("#" + Ctrl.Comment.id_more));
                $("#" + Ctrl.Comment.id_body).append(htmlTxt);
                
                var moreCnt = $("#" + Ctrl.Comment.id_more_cnt).val();
                if(moreCnt < (Ctrl.Comment.ROWS + 1)){
                    $("#" + Ctrl.Comment.id_more).hide();
                }
            });
        },
        commentBadgeCnt : 0,
        layer : function(packet){
            
            var __add = function(packet){
                // guide 삭제
                // $("#" + Ctrl.Comment.id_empty).hide();
                $("#" + Ctrl.Comment.id_body).removeClass("empty");
                
                // var profileUrl = _url("profile") + packet.userno;
                var profileUrl = packet.thumbnail;
                var removeTmpt = (PacketMgr.userid == packet.userid) ? "<a href=\"javascript:void(0)\" onclick=\"Ctrl.Comment.remove(this, '"+packet.commentno+"')\" >"+_msg("delete")+"</a>" : "";
                var datetime = packet.datetime;
                var date = Utils.getDateFormat(datetime, "-", 2);
                
                var template = "<div class=\"comm_box\" commentno=\""+packet.commentno+"\">\
                <img class=\"user_photo\" src=\""+profileUrl+"\"/>\
                <span class=\"user_name\">"+packet.usernm+"</span>\
                " + removeTmpt + "\
                <span class=\"chat_time\">"+_msg("moment")+"</span>\
                <span class=\"comm_msg\">"+Utils.escapeLinkString(packet.content) + "</span>\
                </div>"
                
                var len =  $(".comm_box", "#" + Ctrl.Comment.id_body).length;
                if(len > 0){
                    $(template).insertBefore($(".comm_box", "#" + Ctrl.Comment.id_body).eq(0));
                    
                }else{
                    $(template).insertBefore($("#cmt_more_btn", "#" + Ctrl.Comment.id_body));
                }
            }
            
            var __remove = function(packet){
                // len가 0이면
                $(".comm_box", "#" + this.id_body).each(function(){
                    var beforeCommentNo = $(this).attr("commentno");
                    if(packet.commentno == beforeCommentNo){
                        $(this).remove();
                    }
                })
                
                var len = $(".comm_box", "#" + this.id_body).length;
                if(len < 1){
                    // $("#" + Ctrl.Comment.id_empty).show();
                    $("#" + Ctrl.Comment.id_body).addClass("empty");
                }
            }
            
            var type = packet.type;
            
            if(type == "0"){
                __add.call(this, packet);
                // __add(packet);
                var notiTitle = _msg("noti");
                var notiContent = packet.usernm  + _msg("comment.add.msg");
                
                Ctrl.Noti.show(notiTitle, notiContent);
                
                if($("#commentWrapper").css("display") == "none") {
                    Ctrl.Comment.commentBadgeCnt++;
                    $(".btn_comment").addClass("checked");
                    $("#comment_badge").show();
                    $("#comment_badge").text(Ctrl.Comment.commentBadgeCnt);
                }
                
            }else if(type == "1"){
                __remove.call(this, packet);
            }
        },
        
        destroy : function(){
            $("#" + this.id).draggable("destroy"); /** 이동은 되데 저장은 하지 않게 수정 **/
            
            // $("#" + this.id).slideToggle("destroy");
            
            // add
            $("#" + this.id_add).unbind("click");
            
            // fold
            $("#" + this.id_open).unbind("click");
            
            // close
            $("#" + this.id_hide).unbind("click");
            
            // close
            $("#" + this.id_close).unbind("click");
            
            // more
            $("#" + this.id_more).unbind("click");
            
            // enter
            $("#" + this.id_content).unbind("keydown");
            
            // focus
            $("#" + this.id_content).unbind("focus");
        }
    },
    
    Memo : {
        template : "",
        list : [],
        // 1,2,3,4,5,6
        init : function(){
            $(".memo", $("#memoWrapper")).each(function(){
                
                var bgColor = $(this).css("background");
                bgColor = bgColor.replace("rgb(", "").replace(")", "").split(",");
                var r = bgColor[0];
                var g = bgColor[1];
                var b = bgColor[2];
                var ord = $(this).attr("ord");
                
                var memo = new Memo(UI.CONTAINER);
                memo.init($(this).get(0), ord, r, g, b);
                
                //--
                
                Ctrl.Memo.list.push(memo);
                
            })
            
        },
        
        add : function(){
            // var idx = this.list.length;
            var ord = Ctrl.Memo.getMax() + 1;
            
            var memo = new Memo(UI.CONTAINER);
            memo.draw("memoWrapper", ord, PacketMgr.isMC);
            
            this.list.push(memo);
        },
        
        // 패킷을 받아서 드로잉
        receive : function(packet){
            var type = packet.type || '';
            var memoNo = packet.memono || '';
            var seqNo = packet.seqno || '';
            var title = packet.title || '';
            var content = packet.content || '';
            var left = packet.x || 0;
            var top = packet.y || 0;
            
            var r = packet.color_r || 235;
            var g = packet.color_g || 235;
            var b = packet.color_b || 235;
            var fold = packet.fold || "1";
            var ord = packet.ord || Ctrl.Memo.list.length;
            
            if(type == '0'){
                var memo = new Memo();
                memo.receive(type, "memoWrapper", ord, PacketMgr.isMC, memoNo, seqNo, title, content, left, top, r, g, b, fold);
                
                this.list.push(memo);
                
            }else if(type == '1' || type == '2'){
                // 삭제
                var len = this.list == null ? 0 : this.list.length;
                for(var i=0; i<len; i++){
                    var memo = this.list[i];
                    // $(container).attr("memono", json.map.commentno);
                    if(memo.get("memono") == memoNo){
                        memo.receive(type, "memoWrapper", ord, PacketMgr.isMC, memoNo, seqNo, title, content, left, top, r, g, b, fold);
                        break;
                    }
                }
            }
        },
        
        auth : function(){
            // 마스터 바뀐경우 이벤트 다시 수정
            var len = this.list.length;
            for(var i=0; i<len; i++){
                var memo = this.list[i];
                memo.changeMC(PacketMgr.isMC);
            }
        },
        
        getMax : function(){
            var bfOrd = -1;
            var len = this.list.length;
            for(var i=0; i<len; i++){
                var memo = this.list[i];
                var ord	= memo.getOrd();
                // console.log("ord : " + ord);
                if(bfOrd < ord){
                    bfOrd = ord;
                }
            }
            return bfOrd;
        },
        
        destroy : function(){
            var len = Ctrl.Memo.list.length;
            for(var i=0; i<len; i++){
                var memo = Ctrl.Memo.list[i];
                
                if(PacketMgr.isMC){
                    memo.destroy();
                } else {
                    memo.destroyUser();
                }
            }
            
            Ctrl.Memo.list = [];
        }
    },
    
    Background : {
        rgb : [{r:10,g:60,b:40},{r:250,g:20,b:10},{r:250,g:150,b:10},{r:250,g:250,b:10},{r:150,g:250,b:10},{r:10,g:150,b:250}],
        red : "",
        green : "",
        blue : "",
        img : "",
        init : function(){
            this._setEvent();
            this._setInit();
        },
        receive : function(packet){
            // var packet = {"cmd":"background","color_r":""+r+"","color_g":""+ g + "","color_b":"" + b + "","bgimg": imgType};
            if(packet.bgimg == "" && packet.color_r == "" && packet.color_g  == "" && packet.color_b  == ""){
                this._clear();
            }else{
                this._clear();
                
                if(packet.bgimg != "") this._setImg(parseInt(packet.bgimg) -1);
                
                if(packet.color_r != "" && packet.color_g  != "" && packet.color_b  != "") this._setRgbColor(packet.color_r, packet.color_g, packet.color_b);
            }
        },
        
        save : function(){
            var url = Utils.addContext(_url("bg.save"));
            var params = {
                roomid : RoomSvr.roomid,
                bgimg : this.img,
                bgred : this.red,
                bggreen : this.green,
                bgblue : this.blue
            };
            
            Utils.request(url, "json", params, function(data){
                if(data.result == "0"){
                    PacketMgr.Master.background(Ctrl.Background.img, Ctrl.Background.red, Ctrl.Background.green, Ctrl.Background.blue);
                    
                    $("#bg_box").slideToggle({easing:"easeInCubic", duration:800});
                }
            });
        },
        
        _setImg : function(idx){
            
            // var imgUrl = Utils.addContext("res/images/background_0" + (idx+1) + ".png");
            var imgUrl = Utils.addResPath("fb/images", "background_0" + (idx+1) + ".png");
            $("#" + (UI.SKETCH + UI.current)).css("backgroundImage", "url("+imgUrl+")");
            
            this.img = (idx+1);
            
            $("span", "#bg_box").each(function(){
                if($(this).index() == idx){
                    if( !$(this).hasClass("checked") && !$(this).hasClass("minicolors-swatch-color")){
                        $(this).addClass("checked");
                    }
                }else if(($(this).index() < 4 && !$(this).hasClass("minicolors-swatch-color"))){
                    $(this).removeClass("checked");
                }
            });
            
            $(".bg_del", "#bg_box").removeClass("checked");
            
        },
        
        _setColor : function(idx){
            var colorIdx = idx - 4;
            var r = this.rgb[colorIdx].r;
            var g = this.rgb[colorIdx].g;
            var b = this.rgb[colorIdx].b;
            
            $("span.minicolors-swatch-color", "#bg_box").removeClass("checked");
            
            $("span", "#bg_box").each(function(){
                if($(this).index() == idx){
                    if( !$(this).hasClass("checked")){
                        $(this).addClass("checked");
                    }
                }else if($(this).index() > 3 && $(this).index() < 12){
                    $(this).removeClass("checked");
                }
            });
            
            this._setRgbColor(r, g, b);
        },
        
        _setCustomColor : function(code){
            var r = Ctrl.hexToRgb(code)["r"];
            var g = Ctrl.hexToRgb(code)["g"];
            var b = Ctrl.hexToRgb(code)["b"];
            
            $("span", "#bg_box").each(function(){
                if($(this).index() > 3 && $(this).index() < 12){
                    $(this).removeClass("checked");
                }
            });
            
            $("span.minicolors-swatch-color", "#bg_box").addClass("checked");
            
            this._setRgbColor(r, g, b);
        },
        
        _setRgbColor : function(r, g, b){
            this.red = r;
            this.green = g;
            this.blue = b;
            
            $("#" + (UI.SKETCH + UI.current)).css("backgroundColor", "rgba("+r+","+g+","+b+", 1 )");
            var isNormal = false;
            $("span", "#bg_box").each(function(){
                
                if($(this).index() > 3 && $(this).index() < 10){
                    var bgColor = $(this).css("backgroundColor");
                    bgColor = bgColor.replace("rgba(", "").replace(")", "").split(",");
                    
                    var btnR = bgColor[0].trim();
                    var btnG = bgColor[1].trim();
                    var btnB = bgColor[2].trim();
                    
                    if($(this).index() > 3 && $(this).index() < 12){
                        if(r == btnR && g == btnG && b == btnB){
                            isNormal = true;
                            
                            if(!$(this).hasClass("checked")) $(this).addClass("checked");
                            
                            $(".minicolors-swatch-color", "#bg_box").removeClass("checked");
                            
                        }else{
                            $(this).removeClass("checked");
                        }
                    }
                }
                
                if(!isNormal && $(this).index() == 0 && $(this).hasClass("minicolors-swatch-color")){
                    $(".minicolors-swatch-color", "#bg_box").css("backgroundColor", "rgb("+r+","+g+","+b+")");
                    $(".minicolors-input", "#bg_box").val( "#" + Ctrl.rgbToHex(r, g, b));
                    
                    if(!$(".minicolors-swatch-color", "#bg_box").hasClass("checked"))  $(".minicolors-swatch-color", "#bg_box").addClass("checked");
                }
            });
            
        },
        
        _clear : function(){
            $("#" + (UI.SKETCH + UI.current)).css("backgroundImage", "");
            $("#" + (UI.SKETCH + UI.current)).css("backgroundColor", "");
            
            $("span.minicolors-swatch-color", "#bg_box").removeClass("checked");
            
            $("span", "#bg_box").each(function(){
                if( $(this).index() == 11){
                    $(this).addClass("checked");
                }else{
                    $(this).removeClass("checked");
                }
            });
            
            this.red = "";
            this.green = "";
            this.blue = "";
            this.img = "";
        },
        
        _setEvent : function(){
            $("span", "#bg_box").click(function(){
                if(!Ctrl._checkAuth(true)) return;
                
                var idx = $(this).index();
                if(idx < 4){
                    Ctrl.Background._setImg(idx);
                }else if(idx < 10){
                    Ctrl.Background._setColor(idx);
                }else if(idx == 11){
                    Ctrl.Background._clear();
                }
            });
            
            $(".miniColor_jqueryP", "#bg_box").minicolors({
            control: $(this).attr('data-control') || 'hue',
            defaultValue: $(this).attr('data-defaultValue') || '',
                inline: $(this).attr('data-inline') === 'true',
            letterCase: $(this).attr('data-letterCase') || 'lowercase',
            opacity: $(this).attr('data-opacity'),
            position: $(this).attr('data-position') || 'bottom right',
            change: function(hex, opacity) {
                if(!Ctrl._checkAuth(false)) return;
                
                Ctrl.Background._setCustomColor(hex);
            },
            theme: 'default'
            });
            
            $(".btn_apply", "#bg_box").click(function(){
                Ctrl.Background.save();
            });
        },
        
        _setInit : function(){
            var bg = CanvasApp.info.bg;
            if(bg != null){
                this.img = bg.bgimg;
                this.red = bg.bgred;
                this.green = bg.bggreen;
                this.blue = bg.bgblue;
                
                if(bg.coloridx == 6){
                    $("span.minicolors-swatch-color", "#bg_box").addClass("checked");
                }
            }
        },
        
        destroy : function(){
            $("span", "#bg_box").unbind("click");
            
            $(".miniColor_jqueryP", "#bg_box").minicolors("destroy");
            
            $(".btn_apply", "#bg_box").unbind("click");
            
            // $("#bg_box").slideToggle("destroy");
        }
    },
    
    BGImg : {
        id : "bgDiv",
        id_del : "bg_file_del",
        drag : true,
        current : null,
        scale : 1,
        zIndex : 49,
        basePos : [80, 80],
        baseSize : [1280,1280],
        ord : 0,	// 가장 큰 order 값
        newCnt : 1,
        data : null,
        seqData : null,
        queue : [],
        loading : false,
        isDrag : false,
        isRotate : false,
        // 내가 bg를 불러온 경우
        init : function(packet){
            //-- test
            var url = packet.dnloadurl;
            var seqNo = packet.seqno;
            var typeFlag = packet.typeflag || "0";
            var userNm = packet.usernm || "";
            var thumbnail = packet.thumbnail || "";
            var degree = packet.degree != null && packet.degree != "" ? parseInt(packet.degree) : 0;
            
            //-- test2
            var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
            var context = imgCanvas.getContext("2d");
            
            // 높이 기준으로
            var maxWidth = imgCanvas.width / 2;
            var maxHeight = imgCanvas.width / 2;
            var scaleW = 1;
            var scaleH = 1;
            
            Ctrl.BGImg.loading = true;
            
            var img = new Image();
            img.src = url;
            img.crossOrigin = "Anonymous";
            img.onload = function(){
                var imgObj = Ctrl.BGImg.getSize(img, imgCanvas);
                var width = imgObj[0], height = imgObj[1];
                var initPosition = Ctrl.BGImg.basePos[0] + (Ctrl.BGImg.newCnt * 20);
                var posX = initPosition, posY = initPosition;
                
                packet.orgw = width;
                packet.orgh = height;
                
                width = width * scaleW * (imgCanvas.width / Ctrl.BGImg.baseSize[0]);
                height = height * scaleH * (imgCanvas.height / Ctrl.BGImg.baseSize[1]);
                
                img.width = width;
                img.height = height;
                
                // 포인터 이미지도 투명도 영향을 받는다.
                context.setTransform(1,0,0,1,0,0);
                context.globalCompositeOperation = 'source-over';
                context.globalAlpha = 1;
                // context.clearRect(0, 0, imgCanvas.width, imgCanvas.height);
                context.drawImage(img, posX, posY, width, height);
                
                if(Ctrl.BGImg.drag) {
                    var editor = Ctrl.BGImg.setDrag(img, imgCanvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree);
                    packet.editor = editor;
                    packet.cvs = {
                        layer : img,
                        width : width,
                        height : height,
                        posx : posX,
                        posy : posY,
                        degree : degree
                    };
                }
                
                PacketMgr.Master.img(url, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, degree);
                /**
                 var data = {
                 seqno :   seqNo,
                 url : url,
                 posx : posX,
                 posy : posY,
                 scale : Ctrl.BGImg.scale
                 };
                 **/
                // 현재 single upload만 지원하므로 초기화 시킴
                // Ctrl.BGImg.list = [];
                // Ctrl.BGImg.list.push(data);
                // this.data = new Map();
                
                if(Ctrl.BGImg.data == null) Ctrl.BGImg.data = new Map();
                Ctrl.BGImg.data.put(seqNo, packet);
                
                // basePosition 처리
                Ctrl.BGImg.newCnt++;
                
                Ctrl.BGImg.loading = false;
            }
            
            // Ctrl.BGImg.current = img;
            $("#" + Ctrl.BGImg.id_del).show();
        },
        
        // packet으로 들어온 경우
        draw : function(packet, fromQueue){
            var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
            var context = imgCanvas.getContext("2d");
            var url = packet.url;
            var posX = UI.getOrgX(imgCanvas.width, imgCanvas.height, packet.posx);
            var posY = UI.getOrgY(imgCanvas.width, imgCanvas.height, packet.posy);
            var width = packet.width;
            var height = packet.height;
            var seqNo = packet.seqno;
            var scaleW = packet.scalew;
            var scaleH = packet.scaleh;
            var mode = packet.mode;
            var typeFlag = packet.typeflag || "0";
            var userNm = packet.usernm || "";
            var thumbnail = packet.thumbnail || "";
            var degree = packet.degree != null && packet.degree != "" ? parseInt(packet.degree) : 0;
            
            if(typeof(mode) != "undefined" && mode == "1"){
                this.removeLayer(seqNo);
                
                $("#" + Ctrl.BGImg.id_del).hide();
                return;
            }else{
                $("#" + Ctrl.BGImg.id_del).show();
            }
            
            // 이미지 크기에 따라 로딩속도가 다를수 있으므로 동기화를 맞춰줘야 한다.
            if(Ctrl.BGImg.loading){
                if(!fromQueue) Ctrl.BGImg.pushQueue(packet);
                return;
            }
            
            if(location.hostname == "test.wenote.com"){
                // url = url.replaceAll("fb.wenote.com","test.wenote.com");
            }
            
            Ctrl.BGImg.loading = true;
            
            var img = new Image();
            img.src = url;
            img.crossOrigin = "Anonymous";
            img.onload = function(){
                //if(Ctrl.BGImg.orgW == 0 && Ctrl.BGImg.orgH == 0){
                var imgObj = Ctrl.BGImg.getSize(img, imgCanvas);
                packet.orgw = imgObj[0];
                packet.orgh = imgObj[1];
                
                var width = imgObj[0], height = imgObj[1];
                width = width * scaleW * (imgCanvas.width / Ctrl.BGImg.baseSize[0]);
                height = height * scaleH * (imgCanvas.height / Ctrl.BGImg.baseSize[1]);
                
                img.width = width;
                img.height = height;
                
                if(!PacketMgr.isMC){
                    var data = Ctrl.BGImg.data;
                    if(data != null && data.get(seqNo) != null){
                        Ctrl.BGImg.updatePos(seqNo, posX, posY, width, height, degree);
                        Ctrl.BGImg.redraw("", seqNo);
                    }else{
                        /***
                         context.setTransform(1,0,0,1,0,0);
                         context.globalCompositeOperation = 'source-over';
                         context.globalAlpha = 1;
                         context.drawImage(img, posX, posY, width, height);
                         ***/
                        Ctrl.BGImg.drawImage(context, img, posX, posY, width, height, degree);
                    }
                }else{
                    /***
                     context.setTransform(1,0,0,1,0,0);
                     context.globalCompositeOperation = 'source-over';
                     context.globalAlpha = 1;
                     context.drawImage(img, posX, posY, width, height);
                     ***/
                    Ctrl.BGImg.drawImage(context, img, posX, posY, width, height, degree);
                }
                
                if(Ctrl.BGImg.drag) {
                    var editor = Ctrl.BGImg.setDrag(img, imgCanvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree);
                    packet.editor = editor;
                    packet.cvs = {
                        layer : img,
                        width : width,
                        height : height,
                        posx : posX,
                        posy : posY,
                        degree : degree
                    };
                }
                
                Ctrl.BGImg.ord = packet.ord;
                
                // 현재 single upload만 지원하므로 초기화 시킴
                if(Ctrl.BGImg.data == null) Ctrl.BGImg.data = new Map();
                Ctrl.BGImg.data.put(seqNo, packet);
                
                Ctrl.BGImg.loading = false;
                
                // 큐에 데이터가 있으면 draw
                Ctrl.BGImg.shiftQueue();
            }
            // Ctrl.BGImg.current = img;
        },
        
        drawImage : function(context, img, posX, posY, width, height, degree){
            if(degree > 0){
                var halfWidth = width / 2;
                var halfHeight = height / 2;
                var radians = degree * Math.PI / 180;
                
                // Rotate를 필요한 부분만 적용하기 위해서는 현재 그려야 하는 놈 바로 앞에서 행렬을 초기화 해줘야 한다.
                context.setTransform(1,0,0,1,0,0);
                context.translate(posX + halfWidth, posY + halfHeight);
                context.rotate(radians);
                context.drawImage(img, -(halfWidth), -(halfHeight), width, height);
                
            }else{
                context.setTransform(1,0,0,1,0,0);
                context.globalCompositeOperation = 'source-over';
                context.globalAlpha = 1;
                context.drawImage(img, posX, posY, width, height);
            }
        },
        
        shiftQueue : function() {
            if(this.queue != null && this.queue.length < 1) return;
            // Utils.log("UI.rendering : " + UI.rendering + ", queue len : " + PacketMgr.queue.length);
            if(this.loading){
                // 랜더링 중이면 1초후 재실행
                setTimeout(Ctrl.BGImg.shiftQueue, "500");
                return;
            }
            
            var json = this.queue.shift();
            
            this.draw(json, true);
            
            this.shiftQueue();
        },
        
        pushQueue : function(packet){
            if(packet != null) this.queue.push(packet);
            
        },
        
        redraw : function(skipSeqNo, frontSeqNo){
            var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
            var context = imgCanvas.getContext("2d");
            
            // tranlate를 초기값으로 돌려야 하기 때문에 transform 초기화를 해준다.
            context.setTransform(1,0,0,1,0,0);
            context.globalCompositeOperation = 'source-over';
            context.globalAlpha = 1;
            context.clearRect(0, 0, imgCanvas.width, imgCanvas.height);
            
            
            // 2015.11.12 크릭시 정렬 불규칙해서 로직 수정
            var tmpMap = new Map();
            var tmpArr = new Array();
            var tmpIndex = 0;
            $(".img_box_edit", "#sketch" + UI.current).each(function(){
                var tmpZIndex = $(this).css("zIndex");
                var tmpSeq = $(this).attr("seq");
                
                tmpMap.put(tmpZIndex, tmpSeq);
                tmpArr.push(tmpZIndex);
            });
            
            tmpArr.sort();
            
            var len = tmpArr == null ? 0 : tmpArr.length;
            var lastData = null;
            for(var i=0; i<len; i++){
                var seqNo = tmpMap.get(tmpArr[i]);
                var data = Ctrl.BGImg.data.get(seqNo);
                
                // 가장 앞으로 나와야할 데이터를 나중에 그린다.
                if(data != null && data.seqno == frontSeqNo){
                    lastData = data.cvs;
                    continue;
                }
                
                if(data != null && data.seqno != skipSeqNo){
                    var canvasInfo = data.cvs;
                    // Rotate를 필요한 부분만 적용하기 위해서는 현재 그려야 하는 놈 바로 앞에서 행렬을 초기화 해줘야 한다.
                    Ctrl.BGImg.drawImage(context, canvasInfo.layer, canvasInfo.posx, canvasInfo.posy, canvasInfo.width, canvasInfo.height, canvasInfo.degree);
                }
            }
            
            if(lastData != null){
                // Rotate를 필요한 부분만 적용하기 위해서는 현재 그려야 하는 놈 바로 앞에서 행렬을 초기화 해줘야 한다.
                Ctrl.BGImg.drawImage(context, lastData.layer, lastData.posx, lastData.posy, lastData.width, lastData.height, lastData.degree);
            }
        },
        
        updatePos : function(seqNo, posX, posY, width, height, degree){
            if(Ctrl.BGImg.data != null){
                var data = Ctrl.BGImg.data.get(seqNo);
                if(data != null){
                    data.cvs.posx = posX;
                    data.cvs.posy = posY;
                    data.cvs.width = width;
                    data.cvs.height = height;
                    data.cvs.degree = degree;
                }
            }
        },
        
        getCnt : function(){
            var count = 0;
            if(this.data != null){
                var dataKeys = this.data.keys();
                count = dataKeys == null ? 0 : dataKeys.length;
            }
            
            return count;
        },
        
        remove : function(seqNo){
            // var packet = {"cmd":"img","seqno":""+seqno+"","posx":"" + fixedX + "","posy":"" + fixedY + "","scalew":"" + scaleW + "","scaleh":"" + scaleH + "","url": ""+url+"","mode":mode};
            Ctrl.Modal.confirm(_msg("confirm.remove.file"), function(){
                var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
                // var data = bgImg.list.length < 1 ? "" : bgImg.list[0];
                var data = Ctrl.BGImg.data.get(seqNo);
                if(data != null){
                    var url = data.url || "";
                    var ord = data.ord || "0";
                    
                    if(seqNo != "") PacketMgr.Master.img(url, imgCanvas, seqNo, 1, 1,  0, 0, "1", ord);
                    
                    Ctrl.BGImg.removeLayer(seqNo);
                }
            });
        },
        
        removeAll : function(){
            Ctrl.Modal.confirm(_msg("confirm.remove.file"), function(){
                var remove = function(deleteSeqNo){
                    var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
                    // var data = bgImg.list.length < 1 ? "" : bgImg.list[0];
                    var data = Ctrl.BGImg.data.get(deleteSeqNo);
                    if(data != null){
                        var url = data.url || "";
                        var ord = data.ord || "0";
                        
                        if(deleteSeqNo != "") PacketMgr.Master.img(url, imgCanvas, deleteSeqNo, 1, 1,  0, 0, "1", ord);
                        
                        Ctrl.BGImg.removeLayer(deleteSeqNo);
                    }
                }
                
                if(Ctrl.BGImg.data != null){
                    var dataKeys = Ctrl.BGImg.data.keys();
                    var len = dataKeys == null ? 0 : dataKeys.length;
                    for(var i=0; i<len; i++){
                        var seqNo = dataKeys[i];
                        // request check
                        remove(seqNo);
                    }
                }
            });
        },
        
        removeLayer : function(seqNo){
            this.redraw(seqNo);
            
            if(Ctrl.BGImg.data != null){
                var data = Ctrl.BGImg.data.get(seqNo);
                if(data != null){
                    $(data.editor).unbind("click");
                    $(data.editor).draggable("destroy");
                    $(data.editor).resizable("destroy");
                    $(data.editor).remove();
                    Ctrl.BGImg.data.remove(seqNo);
                }
            }
        },
        
        setDrag : function(img, canvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree){
            var defaultThumb = Utils.addResPath("fb/images", "thum_user.png");
            var headerHeight = $("#" + UI.HEADER).height();
            var context = canvas.getContext("2d");
            
            /** 중복시 제거
             var len = $("#bgDiv").length;
             if(len > 0) {
             $("#" + Ctrl.BGImg.id).draggable("destroy");
             $("#" + Ctrl.BGImg.id).remove();
             }
             **/
            
            if(Ctrl.BGImg.data != null){
                var data = Ctrl.BGImg.data.get(seqNo);
                if(data != null){
                    $(data.editor).unbind("click");
                    $(data.editor).draggable("destroy");
                    $(data.editor).resizable("destroy");
                    $(data.editor).remove();
                }
            }
            
            var div = document.createElement("div");
            
            var attachClass = (typeFlag == "1") ? "img_answer" : "";
            
            var zIndex = ++Ctrl.BGImg.zIndex;
            // div.id = "bgDiv";
            div.className =  "img_box_edit " + attachClass;
            div.style.position = "absolute";
            div.style.left = posX + "px";
            div.style.top = posY + "px";
            div.style.width = img.width + "px";
            div.style.height = img.height + "px";
            div.style.cursor = "move";
            div.style.zIndex = zIndex;
            div.setAttribute("typeflag", typeFlag || "0");
            div.setAttribute("seq", seqNo);
            
            var ieContext = Utils.browser("msie") ? "_ie" : "";
            var userInfo = (typeFlag == "1") ? "<a class=\"answered_user\"><img class=\"user_photo\" src=\""+thumbnail+"\" onerror=\"this.src='"+defaultThumb+"'\" ><span>"+userNm+"</span></a>" : "";
            div.innerHTML = "<span class=\"img_del\" title=\"delete image\" onclick=\"Ctrl.BGImg.remove('"+seqNo+"');\"><a class=\"btn_x\"></a></span>\
            <span class=\"tl\"></span>\
            <span class=\"tc\"><div class=\"rotation"+ ieContext +"_handle\"><div class=\"rotation"+ieContext+"_bar\"></div><div class=\"rotate"+ieContext+"_handle\"></div></div></span>\
            <span class=\"tr\"></span>\
            <span class=\"ml\"></span>\
            <div class=\"imgDiv\">" + userInfo + "</div>\
            <span class=\"mr\"></span>\
            <span class=\"bl\"></span>\
            <span class=\"bc\"></span>\
            <span id=\"rz_br\"class=\"br\"></span>";
            
            if(degree > 0){
                $(div).css('-moz-transform', 'rotate(' + degree + 'deg)')
                .css('-webkit-transform', 'rotate(' + degree + 'deg)')
                .css('-o-transform', 'rotate(' + degree + 'deg)')
                .css('transform', 'rotate(' + degree + 'deg)')
                .css('ms-transform', 'rotate(' + degree + 'deg)')
            }
            
            // --> IE안됨
            $("#sketch" + UI.current).append(div);
            
            var drawToCanvas = function(finalOffset, positionX, positionY){
                
                div.style.backgroundImage = 'none';
                
                var container = document.getElementById(UI.CONTAINER);
                var termY = headerHeight + 9;
                // var finalOffset = $(this).offset();
                var posX = finalOffset.left + (container.scrollLeft || container.scrollLeft);
                var posY = finalOffset.top - termY + (container.scrollTop || container.scrollTop);
                
                if(lastDegree > 0){
                    posX = positionX;
                    posY = positionY;
                }
                
                var data = Ctrl.BGImg.data.get(seqNo);
                var orgW = data.orgw, orgH = data.orgh;
                // var orgW = Ctrl.BGImg.orgW, orgH = Ctrl.BGImg.orgH;// var imgObj = Ctrl.BGImg.getSize(img, imgCanvas);
                var scaleW = ($(div).width() / orgW / (canvas.width / Ctrl.BGImg.baseSize[0]));
                var scaleH = ($(div).height() / orgH / (canvas.height / Ctrl.BGImg.baseSize[1]));
                
                /***
                 context.setTransform(1,0,0,1,0,0);
                 context.globalCompositeOperation = 'source-over';
                 context.globalAlpha = 1;
                 
                 //-- webc client canvas에 그릴시 left, top padding값 포함해서 더함 (패킷에는 패딩값 더하지 않음)
                 context.drawImage(img, posX, posY, $(div).width() , $(div).height() );
                 ***/
                
                // draw canvas시에는 rotate가 들어간 경우 회전반경 만큼의 X, y 좌표를 재조정 해줘야 한다.
                Ctrl.BGImg.drawImage(context, img, posX, posY, $(div).width(), $(div).height(), lastDegree);
                
                PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, lastDegree);
                
                // data  update
                Ctrl.BGImg.updatePos(seqNo, posX, posY, $(div).width(), $(div).height(), lastDegree);
                
                
                Ctrl.BGImg.isDrag = false;
                
                div.style.opacity = 1;
            }
            
            // var RAD2DEG = 180 / Math.PI;
            var RAD2DEG = 180 / Math.PI;
            var offset, dragging = false;
            var dial = $(div);
            var lastDegree = degree;
            var divDegree = 0;
            var r = 0;
            
            $("span.tc", div).mousedown(function(e) {
                // 1. rotate flag setting
                Ctrl.BGImg.isRotate = true;
                
                // 2. set Editor background
                div.style.backgroundImage = "url('"+ img.src +"')";
                div.style.opacity = 0.5;
                
                var data = Ctrl.BGImg.data.get(seqNo);
                
                // 3. offset setting
                var layerLeft = data.cvs.posx;
                var layerTop = data.cvs.posy;
                dial.centerX = layerLeft + dial.width()/2;
                dial.centerY =  layerTop + dial.height()/2;
                
                offset = Math.atan2(dial.centerY - e.pageY, e.pageX - dial.centerX);
                
                // 4. imgcanvas redraw
                Ctrl.BGImg.redraw(seqNo, "");
                
                // 5. draggable 정지
                $(div).draggable("disable");
                
                $(document).mousemove(function(e) {
                    if(Ctrl.BGImg.isRotate){
                        var newOffset = Math.atan2(dial.centerY - e.pageY, e.pageX - dial.centerX);
                        r = (offset - newOffset) * RAD2DEG;
                        
                        //	270이 넘어가면 -로 바뀌는 버그가 있어서 일시적으로 이렇게 처리. ** 수정 및 원인파악 필요
                        if(r < 0) r += 360;
                        divDegree = r + lastDegree;
                        
                        if(divDegree > 360) divDegree = divDegree - 360;
                        
                        $(div).css('-moz-transform', 'rotate(' + divDegree + 'deg)')
                        .css('-webkit-transform', 'rotate(' + divDegree + 'deg)')
                        .css('-o-transform', 'rotate(' + divDegree + 'deg)')
                        .css('transform', 'rotate(' + divDegree + 'deg)')
                        .css('ms-transform', 'rotate(' + divDegree + 'deg)')
                    }
                    
                })
                
                $(document).mouseup(function() {
                    Ctrl.BGImg.isRotate = false;
                    lastDegree = divDegree;
                    
                    // draggable 재동작
                    div.style.backgroundImage = 'none';
                    
                    /**
                     var container = document.getElementById(UI.CONTAINER);
                     var termY = headerHeight + 9;
                     var finalOffset = $(div).offset();
                     var posX = finalOffset.left + (container.scrollLeft || container.scrollLeft);
                     var posY = finalOffset.top - termY + (container.scrollTop || container.scrollTop);
                     **/
                    var data = Ctrl.BGImg.data.get(seqNo);
                    var orgW = data.orgw, orgH = data.orgh;
                    
                    // 최초 포지션...
                    var posX = data.cvs.posx;
                    var posY = data.cvs.posy;
                    
                    // var orgW = Ctrl.BGImg.orgW, orgH = Ctrl.BGImg.orgH;// var imgObj = Ctrl.BGImg.getSize(img, imgCanvas);
                    var scaleW = ($(div).width() / orgW / (canvas.width / Ctrl.BGImg.baseSize[0]));
                    var scaleH = ($(div).height() / orgH / (canvas.height / Ctrl.BGImg.baseSize[1]));
                    
                    context.globalCompositeOperation = 'source-over';
                    context.globalAlpha = 1;
                    
                    /***
                     if(lastDegree > 0){
                     context.restore();
                     context.translate(canvas.width / 2, canvas.height / 2);
                     context.rotate(Math.PI / 4);
                     }
                     
                     //-- webc client canvas에 그릴시 left, top padding값 포함해서 더함 (패킷에는 패딩값 더하지 않음)
                     context.drawImage(img, posX, posY, $(div).width() , $(div).height() );
                     **/
                    
                    if(lastDegree > 0){
                        
                        var halfWidth = $(div).width() / 2;
                        var halfHeight = $(div).height() / 2;
                        
                        // Rotate를 필요한 부분만 적용하기 위해서는 현재 그려야 하는 놈 바로 앞에서 행렬을 초기화 해줘야 한다.
                        context.setTransform(1,0,0,1,0,0);
                        context.translate(posX + halfWidth, posY + halfHeight);
                        
                        var radians = lastDegree * Math.PI / 180;
                        
                        context.rotate(radians);
                        context.drawImage(img, -(halfWidth), -(halfHeight), $(div).width(), $(div).height());
                        
                    }else{
                        context.setTransform(1,0,0,1,0,0);
                        context.drawImage(img, posX, posY, $(div).width() , $(div).height() );
                    }
                    
                    PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, lastDegree);
                    
                    // data  update
                    Ctrl.BGImg.updatePos(seqNo, posX, posY, $(div).width(), $(div).height(), lastDegree);
                    
                    setTimeout(function(){
                        Ctrl.BGImg.isRotate = false;
                        div.style.opacity = 1;
                        
                        $(div).draggable("enable");
                        
                    }, "500");
                    
                    $(document).unbind("mouseup");
                    $(document).unbind("mousemove");
                })
            })
            
            // 이미 가장 최상의 order인경우 cancel
            var getMaxOrd = function(){
                var maxIndex = 0;
                $(".img_box_edit", "#sketch" + UI.current).each(function(){
                    var zIndex = $(this).css("zIndex");
                    var currentIndex = parseInt(zIndex);
                    if(currentIndex > maxIndex){
                        maxIndex = currentIndex;
                    }
                });
                return maxIndex;
            }
            
            var tmpCount = 0;
            // 정렬
            $(div).click(function(){
                
                if(Ctrl.BGImg.isDrag || Ctrl.BGImg.isRotate) return;
                
                var zIndex = $(this).css("zIndex");
                if(getMaxOrd() <= zIndex){
                    return;
                }else{
                    zIndex = getMaxOrd() + 1;
                }
                
                var container = document.getElementById(UI.CONTAINER);
                var termY = headerHeight + 9;
                
                var data = Ctrl.BGImg.data.get(seqNo);
                // 최초 포지션...
                var posX = data.cvs.posx;
                var posY = data.cvs.posy;
                var degree = data.cvs.degree;
                var orgW = data.orgw, orgH = data.orgh;
                
                // var orgW = Ctrl.BGImg.orgW, orgH = Ctrl.BGImg.orgH;// var imgObj = Ctrl.BGImg.getSize(img, imgCanvas);
                var scaleW = ($(div).width() / orgW / (canvas.width / Ctrl.BGImg.baseSize[0]));
                var scaleH = ($(div).height() / orgH / (canvas.height / Ctrl.BGImg.baseSize[1]));
                
                PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, degree);
                
                // 정렬 맞춤
                Ctrl.BGImg.redraw("", seqNo);
                
                $(div).css("zIndex", zIndex);
                $(".ui-resizable-handle", div).css("zIndex", zIndex);
            });
            
            var startX = 0;
            var startY = 0;
            
            // 편집기능 별도 추가 필요.
            
            $(div).draggable({
                // handle : $("#bgMoveDiv"),
            containment: $('#docWrapper'),
                start : function(e, ui){
                    Ctrl.BGImg.isDrag = true;
                    Ctrl.BGImg.redraw(seqNo, "");
                    div.style.backgroundImage = "url('"+ img.src +"')";
                    div.style.opacity = 0.5;
                    
                    var data = Ctrl.BGImg.data.get(seqNo);
                    if(data.cvs.degree > 0){
                        startX = ui.position.left;
                        startY = ui.position.top;
                    }
                },
                
                drag : function(e, ui){
                    if(lastDegree > 0){
                        var data = Ctrl.BGImg.data.get(seqNo);
                        // 최초 포지션...
                        var posX = data.cvs.posx;
                        var posY = data.cvs.posy;
                        
                        ui.position.left = posX + (ui.position.left - startX);
                        ui.position.top = posY + (ui.position.top - startY);
                    }
                },
                stop : function(e, ui){
                    Ctrl.BGImg.isDrag = false;
                    drawToCanvas( $(this).offset(), ui.position.left, ui.position.top);
                }
            });
            
            
            $(div).resizable({
                handles : "e,s,se,n,ne,w,sw,nw",
                animation : true,
            containment: $('#docWrapper'),
                aspectRatio : true,	// 정사각 비율로 resize
                minWidth : 100,
                minHeight : 100,
                start : function(e){
                    Ctrl.BGImg.isDrag = false;
                    Ctrl.BGImg.redraw(seqNo, "");
                    div.style.backgroundImage = "url('"+ img.src +"')";
                },
                drag : function(e, ui){
                    
                },
                stop : function(e, ui){
                    drawToCanvas( $(this).offset(), ui.position.left, ui.position.top);
                }
            });
            
            this.auth();
            
            return div;
        },
        
        getSize : function(img, canvas){
            var width = img.width;
            var height = img.height;
            var maxWidth = (width > height) ? Ctrl.BGImg.baseSize[0] / 2 : Ctrl.BGImg.baseSize[1] / 2;
            var maxHeight = (width > height) ? Ctrl.BGImg.baseSize[0] / 2 : Ctrl.BGImg.baseSize[1] / 2;
            
            // Check if the current width is larger than the max
            if(width > maxWidth){
                ratio = maxWidth / width;   // get ratio for scaling image
                img.width = maxWidth;		 // Set new width
                img.height = height * ratio;  // Scale height based on ratio
                height = height * ratio;    // Reset height to match scaled image
            }
            
            var width = img.width;    // Current image width
            var height = img.height;  // Current image height
            
            // Check if current height is larger than max
            if(height > maxHeight){
                ratio = maxHeight / height; // get ratio for scaling image
                img.height = maxHeight;   // Set new height
                img.width = width * ratio;    // Scale width based on ratio
                width = width * ratio;    // Reset width to match scaled image
            }
            // Utils.log("############ " + img.width + " " + img.height);
            
            return [img.width, img.height];
        },
        
        /**
         // 비율에 맞는 값으로 리턴
         getSize : function(img, canvas){
         var width = img.width;
         var height = img.height;
         var maxWidth = (width > height) ? canvas.width / 2 : canvas.height / 2;
         var maxHeight = (width > height) ? canvas.width / 2 : canvas.height / 2;
         
         // Check if the current width is larger than the max
         if(width > maxWidth){
         ratio = maxWidth / width;   // get ratio for scaling image
         img.width = maxWidth;		 // Set new width
         img.height = height * ratio;  // Scale height based on ratio
         height = height * ratio;    // Reset height to match scaled image
         }
         
         var width = img.width;    // Current image width
         var height = img.height;  // Current image height
         
         // Check if current height is larger than max
         if(height > maxHeight){
         ratio = maxHeight / height; // get ratio for scaling image
         img.height = maxHeight;   // Set new height
         img.width = width * ratio;    // Scale width based on ratio
         width = width * ratio;    // Reset width to match scaled image
         }
         // Utils.log("############ " + img.width + " " + img.height);
         
         return [img.width, img.height];
         },
         **/
        
        enable : function(){
            /**
             var div = document.getElementById(Ctrl.BGImg.id);
             if(div){
             $(div).show();
             $(div).draggable("enable");
             }
             **/
            
            var sketch = UI._getSketch(1);
            var layer = $(".img_box_edit", sketch).get(0);
            if(layer != null){
                // typeflag
                $(".img_box_edit", sketch).each(function(){
                    var typeFlag = $(this).attr("typeflag") || "0";
                    if(typeFlag == "1"){
                        // remove Class
                        $(this).removeClass("img_answer");
                        $(this).css("cursor", "move");
                    }
                    $(this).show();
                });
                
                $(".img_box_edit", sketch).draggable("enable");
                $(".img_box_edit", sketch).resizable("enable");
            }
        },
        
        disable : function(){
            // var div = document.getElementById(Ctrl.BGImg.id);
            // multi base는 전부 적용한다.
            var sketch = UI._getSketch(1);
            var layer = $(".img_box_edit", sketch).get(0);
            if(layer != null){
                // typeflag
                $(".img_box_edit", sketch).each(function(){
                    var typeFlag = $(this).attr("typeflag") || "0";
                    if(typeFlag == "1"){
                        // add Class
                        $(this).addClass("img_answer");
                        $(this).css("cursor", "default");
                    }
                    $(this).hide();
                });
                
                $(".img_box_edit", sketch).draggable("disable");
                $(".img_box_edit", sketch).resizable("disable");
            }
        },
        
        auth : function(){
            var isHand = Ctrl.isHand();
            var isText = Ctrl.isText();
            if(PacketMgr.isMC && UI.scale == 1 && isHand && !isText){
                this.enable();
            }else{
                this.disable();
            }
        },
        
        destroy : function(){
            var sketch = UI._getSketch(1);
            var layer = $(".img_box_edit", sketch).get(0);
            if(layer != null){
                $(".img_box_edit", sketch).unbind("click");
                $(".img_box_edit", sketch).draggable("destroy");
                $(".img_box_edit", sketch).resizable("destroy");
            }
        }
    },
    
    Text : {
        // id : {object}
        data : null,
        current : "",	// 현재 편집중인 id
        redrawSkipId : "", // text annotaion시 skip 처리하는 아이디
        historySkip : false,
        dragging : false,
        edit : ["fitalic","fbold","fleft","fcenter","fright"],
        init : function(){
            this._setEvent();
        },
        isActive : function(){
            return $("#text_btn").hasClass("checked");
        },
        
        isOpened : function(){
            var display = $("#fontbox").css("display");
            return (display == "none") ? false : true;
        },
        
        isBlank : function(){
            var val = $("#txt_area").val().trim();
            return val == "" ? true : false;
        },
        
        _setEvent : function(){
            $(".ffamily", "#fontbox").change(function(){
                var val = $(this).val();
                $("#txt_area").css("fontFamily", val);
                
                Ctrl.Text.resizeTextArea();
            });
            
            $(".fsize", "#fontbox").change(function(){
                var val = $(this).val();
                $("#txt_area").css("fontSize", val + "px");
                
                Ctrl.Text.resizeTextArea();
            });
            
            $("a.edit_btn", "#fbox_wrap").click(function(){
                if(!Ctrl._checkAuth(true)) return;
                
                var idx = $(this).index();
                var func = Ctrl.Text.edit[idx - 2];
                Ctrl.Text.toggleEdit(func);
                
                if($(this).hasClass("checked")){
                    $(this).removeClass("checked");
                }else{
                    $(this).addClass("checked");
                }
            });
            
            // 삭제
            $("#txt_del_btn").click(function(){
                if(!Ctrl._checkAuth(true)) return;
                
                // send remove packet - 생성하고, 저장 안한경우 레이어만 지운다.
                var id = Ctrl.Text.current;
                if(id != ""){
                    var packet = Ctrl.Text._getData(id);
                    PacketMgr.Master.textbox(UI.current, packet.id, "2", packet.text, packet.face, packet.size, packet.r, packet.g, packet.b, packet.w, packet.h, packet.x, packet.y, packet.italic, packet.bold);
                }else{
                    Ctrl.Text._removeLayer();
                }
            });
            
            $(".miniColor_jqueryP", "#fbox_wrap").minicolors({
            control: $(this).attr('data-control') || 'hue',
            defaultValue: $(this).attr('data-defaultValue') || '',
                inline: $(this).attr('data-inline') === 'true',
            letterCase: $(this).attr('data-letterCase') || 'lowercase',
            opacity: $(this).attr('data-opacity'),
            position: $(this).attr('data-position') || 'bottom right',
            change: function(hex, opacity) {
                $("#txt_area").css("color", hex);
            },
            theme: 'default'
            });
            
            $("#txt_area").blur(function(){
                Ctrl.BGImg.auth();
                PDFViewer.auth();
            });
            
            $("#txt_area").change(Ctrl.Text.resizeTextArea);
            $("#txt_area").keydown(Ctrl.Text.resizeTextArea);
            $("#txt_area").keyup(Ctrl.Text.resizeTextArea);
            
            // text annotation 관련 이벤트
            this._setTextEvent();
        },
        
        resizeTextArea : function(){
            var text = document.getElementById("txt_area");
            if(text.scrollHeight < 100) text.scrollHeight = 100 + "px";
            
            text.style.height = 'auto';
            text.style.height = text.scrollHeight+'px';
        },
        
        resizeEditArea : function(packet){
            var id = packet.id;
            var w = packet.w;
            var h = packet.h;
            
            $("#txt_" + id).width(w);
            $("#txt_" + id).height(h);
        },
        
        remove : function(id){
            //canvas에서 삭제
            UI.skboards[UI.current-1].removeText(id);
            
            // data 삭제
            Ctrl.Text._removeData(id);
            
            // history 삭제
            PacketMgr._removePacket(PacketMgr.lastFileNo, PacketMgr.lastPageNo, id, false);
            
            Ctrl.Text.current = "";
            
            $("#txt_" + id).remove();
            $("#txt_area").val("");
            $("#fontbox").hide();
        },
        
        removeAll : function(){
            // 휴지통에서 지울경우 전부 삭제
            var len = this.data == null || this.data.keys() == null ? 0 : this.data.keys().length;
            if(this.data != null){
                var dataKeys = this.data.keys();
                var len = dataKeys == null ? 0 : dataKeys.length;
                for(var i=0; i<len; i++){
                    var dataId = dataKeys[i];
                    // request check
                    PacketMgr._removePacket(PacketMgr.lastFileNo, PacketMgr.lastPageNo, dataId, false);
                    Ctrl.Text._removeData(dataId)
                    
                    UI.skboards[UI.current-1].removeText(dataId);
                    $("#txt_" + dataId).remove();
                }
            }
            
            Ctrl.Text.current = "";
            $("#txt_area").val("");
            $("#fontbox").hide();
        },
        
        _removeLayer : function(){
            $("#txt_area").val("");
            $("#fontbox").hide();
        },
        
        _setTextEvent : function(){
            var cvs = $("#sketch" + UI.current).get(0);
            $("#sketch" + UI.current).click(function(ev){
                if(!Ctrl._checkAuth(false)  || UI.scale > 1) {
                    Ctrl.Text.current = "";
                    $("#fontbox").hide();
                    return;
                }
                
                // 이시점은 addMode이기 때문에 current가 공백이면 return 처리 해야한다.
                // Utils.log("Ctrl.Text.isOpened() : " + Ctrl.Text.isOpened() + ", Ctrl.Text.current : " + Ctrl.Text.current + ". Ctrl.Text.isBlank : " + Ctrl.Text.isBlank() );
                if(Ctrl.Text.isOpened() && (Ctrl.Text.current != "" || !Ctrl.Text.isBlank() )){
                    Ctrl.Text.save();
                    
                    // 기존에 저장된 적이 있던 패킷이라면.....
                    if(Ctrl.Text.current != ""){
                        var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
                        PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
                    }
                }
                
                // text 모드가 아닐시 return
                if(!Ctrl.Text.isActive()) {
                    $("#fontbox").hide();
                    return;
                }
                
                Ctrl.Text.current = "";
                
                var container = document.getElementById(UI.CONTAINER);
                var x = ev.pageX + ( container.scrollLeft || container.scrollLeft);
                var y = ev.pageY - 60 + ( container.scrollTop || container.scrollTop);
                
                // min/max limit
                if(x > ($("#" + UI.VIEWER).width() - $("#fontbox").width())){
                    x = ($("#" + UI.VIEWER).width() - $("#fontbox").width());
                }
                if(y < 36) y = 36;
                
                if((y + $("#fontbox").height()) > $("#" + UI.VIEWER).height()){
                    y = $("#" + UI.VIEWER).height() - $("#fontbox").height();
                }
                
                $("#fontbox").css("left", x);
                $("#fontbox").css("top", y);
                $("#fontbox").show();
                
                $("#txt_area").val("");
                $("#txt_area").width(224);
                $("#txt_area").height(100);
                $("#txt_area").focus();
                
                // 바깥 클릭시 cancel
                $("#text_btn").removeClass("checked");
                $("#text_btn2").removeClass("checked");
                
                Ctrl.Text.current = "";
                
                Ctrl._setPointer("1");
            });
            
        },
        _setTextLayer : function(id){
            var container = document.getElementById(UI.CONTAINER);
            var canvas = document.getElementById("sketch" + UI.current);
            var canvasW = $(canvas).width();
            var canvasH = $(canvas).height();
            
            // id, text, x, y, w, h
            var packet = Ctrl.Text._getData(id);
            
            var id = packet.id;
            var text = packet.text;
            var x = UI.getOrgX(canvasW, canvasH, packet.x);
            var y = UI.getOrgY(canvasW, canvasH, packet.y);
            var w = packet.w;
            var h = packet.h;
            
            var layer = document.getElementById("txt_" + this.current);
            if(layer){
                // 업데이트
                layer.style.left = x + "px";
                layer.style.top = y + "px";
                layer.style.width = w + "px";
                layer.style.height = h + "px";
                layer.cursor = (PacketMgr.isMC) ? "text" : "default";
                
                $("#txt_area").val("");
                
                Ctrl.Text._syncEditor(packet);
                
            }else{
                // 신규생성
                var div = document.createElement("div");
                div.id = "txt_" + id;
                // div.className =  "";
                div.style.position = "absolute";
                div.style.left = x + "px";
                div.style.top = y + "px";
                div.style.width = w + "px";
                div.style.height = h + "px";
                // div.style.border = "1px solid red";
                
                // ie10에서 backgroun를 안주면 이벤트가 안먹는다
                div.style.backgroundColor = "rgba(255, 255, 255, 0)";
                div.style.cursor = (PacketMgr.isMC) ? "text" : "default";
                div.style.zIndex = 50;
                
                // 마스터일때만 동작
                $(div).click(function(e){
                    // console.log("Ctrl.Text.dragging : " + Ctrl.Text.dragging);
                    /************************************** 편집모드 **********************************************/
                    if(Ctrl.Text.dragging) return;
                    
                    // 권한이 안맞거나 줌 상태인경우 초기화 시킨다.
                    if(!Ctrl._checkAuth(false) || UI.scale > 1) {
                        Ctrl.Text.current = "";
                        $("#fontbox").hide();
                        return;
                    }
                    
                    var saveRet = false;
                    // add box or 타 edit box가 띄워져 있는경우 저장
                    if(Ctrl.Text.isOpened() && Ctrl.Text.current != id){
                        saveRet = Ctrl.Text.save();
                        if(!saveRet){
                            var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
                            PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
                        }
                    }
                    
                    // id 갖고 있음..
                    Ctrl.Text.current = id;
                    
                    // packet 변경될수 있음.
                    var selectPacket = Ctrl.Text._getData(id);
                    Ctrl.Text._syncEditor(selectPacket);
                    
                    $("#fontbox").show();
                    $("#txt_area").focus();
                    
                    UI.skboards[UI.current-1].removeText(id);
                    
                    Ctrl.BGImg.auth();
                    
                    PDFViewer.auth();
                    
                    return;
                });
                
                $(div).draggable({
                    // handle : $(this),
                containment: $('#docWrapper'),
                    zIndex : 49,
                    start : function(e, ui){
                        Ctrl.Text.dragging = true;
                        div.style.cursor = "move";
                        
                        if(Ctrl.Text.isOpened()){
                            var saveRet = Ctrl.Text.save();
                            if(!saveRet) Ctrl.Text.cancel(false);
                        }else{
                            UI.skboards[UI.current-1].removeText(id);
                        }
                        
                        Ctrl.Text.current = id;
                        
                        var selectPacket = Ctrl.Text._getData(id);
                        Ctrl.Text._syncEditor(selectPacket);
                        
                        // {"cmd":"textbox","type":"1","id":"1fbd85e0","text":"aaaaaaa","face":"Arial","size":"20","w":"224","h":"46","x":"442.8","y":"294.140625","r":"0","g":"0","b":"0","bold":"0","italic":"0"}
                        var bold = selectPacket.bold == "1" ? "font-weight:bold;" : "";
                        var italic = selectPacket.italic == "1" ? "font-style:italic;" : "";
                        
                        div.style.fontFamily = selectPacket.face;
                        div.style.fontSize = selectPacket.size + "px";
                        if(selectPacket.bold == "1") div.style.fontWeight = "bold";
                        if(selectPacket.italic == "1") div.style.fontStyle = "italic";
                        div.style.color = "rgb("+selectPacket.r+","+selectPacket.g+","+selectPacket.b+")";
                        
                        div.innerHTML = selectPacket.text;
                        
                        $(div).addClass("m_down");
                        
                        // draggable에는 IE는 스크롤 시작 좌표 버그가 있다. 따라서 dragging 시 빼준다.
                        if(Utils.browser("msie")) $(this).data("startingScrollTop", $(container).scrollTop());
                    },
                    drag : function(e, ui){
                        if(Utils.browser("msie")){
                            var st = parseInt($(this).data("startingScrollTop"));
                            ui.position.top += st;
                        }
                    },
                    stop : function(e, ui){
                        
                        div.innerHTML = "";
                        
                        div.style.fontFamily = "";
                        div.style.fontSize = "";
                        div.style.fontWeight = "";
                        div.style.fontStyle = "";
                        div.style.color = "";
                        
                        var finalOffset = $(this).offset();
                        var posX = finalOffset.left + (container.scrollLeft || container.scrollLeft);
                        var posY = finalOffset.top + (container.scrollTop || container.scrollTop);
                        // var x = $("#fontbox").position().left + ( container.scrollLeft || container.scrollLeft);
                        // var y = $("#fontbox").position().top + ( container.scrollTop || container.scrollTop);
                        var top = parseInt($("#fbox_wrap").css("top").replace("px", ""));
                        // top + padding
                        posY += (top - 10);
                        
                        $("#fontbox").css("left", posX);
                        $("#fontbox").css("top", posY);
                        $("#fontbox").show();
                        
                        // 						var selectPacket = Ctrl.Text._getData(id);
                        // 패킷 없데이트 전에 먼저 저장한다.
                        Ctrl.Text.save();
                        
                        UI.skboards[UI.current-1].removeText(id);
                        
                        $(div).removeClass("m_down");
                        
                        setTimeout(function(){
                            Ctrl.Text.dragging = false;
                            div.style.cursor = "text";
                        }, "500");
                    }
                });
                
                $("#txt_area").val("");
                
                // --> IE안됨
                $("#textWrapper").append(div);
            }
        },
        
        // mode : 0(add), 1(edit)
        save : function(mode){
            var container = document.getElementById(UI.CONTAINER);
            var x = $("#fontbox").position().left + (container.scrollLeft || container.scrollLeft);
            var y = $("#fontbox").position().top + (container.scrollTop || container.scrollTop);
            
            var text = $("#txt_area").val();
            var w = $("#txt_area").width();
            var h = $("#txt_area").height();
            var size = $("#fsize").val();
            var face = $("#ffamily").val();
            
            var color = $(".miniColor_jqueryP", "#fontbox").val();
            var r = Ctrl.hexToRgb(color)["r"];
            var g = Ctrl.hexToRgb(color)["g"];
            var b = Ctrl.hexToRgb(color)["b"];
            var italic = $("#italic").hasClass("checked") ? "1" : "0";
            var bold = $("#bold").hasClass("checked") ? "1" : "0";
            var canvas = document.getElementById("sketch" + UI.current);
            var canvasWidth = $(canvas).width();
            var canvasHeight = $(canvas).height();
            var compareX = UI.getFixedX(canvasWidth, canvasHeight, x);
            var compareY = UI.getFixedY(canvasWidth, canvasHeight, y);
            
            // 동일할 경우 return시킨다.
            var id = "";
            var type = "";
            
            if(this.data == null) this.data = new Map();
            
            if(this.current == "" || this.data.get(this.current) == null){
                var uuid = Utils.createUUID();
                id = uuid.substring(0, 8);
                type = "0";
                
                // 마스터일때 이전 패킷들 Canvas에 드로우(지워진 상태 이므로)
                if(text.trim() == "") {
                    return false;
                }
                
            }else{
                id = this.current;
                type = "1";
                
                // text가 없으면 삭제 처리 한
                var packet = Ctrl.Text._getData(id);
                if(text != null && text.trim() == ""){
                    // 텍스트 없으면 삭제 처리
                    PacketMgr.Master.textbox(UI.current, packet.id, "2", packet.text, packet.face, packet.size, packet.r, packet.g, packet.b, packet.w, packet.h, packet.x, packet.y, packet.italic, packet.bold);
                    return true;
                }
                if(text == packet.text && w == packet.w && h == packet.h && size == packet.size && face == packet.face
                   && r == packet.r && g == packet.g && b == packet.b && italic == packet.italic && bold == packet.bold && compareX == packet.x && compareY == packet.y){
                    // 					Ctrl.Text.receive(packet);
                    Utils.log("바뀐게 없다.!!!!!!!!!!!!!!!!!!!!!!!!!!!!! : " + JSON.stringify(packet));
                    return false;
                }
            }
            
            PacketMgr.Master.textbox(UI.current, id, type, text, face, size, r, g, b, w, h, x, y, italic, bold);
            
            return true;
        },
        
        // 외부에서 받음.
        receive : function(packet){
            var id = packet.id;
            var type = packet.type;
            
            if(type == "0"){
                if(this._getData(id) != null) {
                    this._removeData(id);
                }
                
                this._setData(packet);
                this._setTextLayer(id);
                
                this.authDrag();
                
            }else if(type == "1"){
                // 수정
                if(this._getData(id) != null) {
                    this._removeData(id);
                    this._setData(packet);
                    
                    if(PacketMgr.isMC){
                        Ctrl.Text.resizeEditArea(packet);
                    }else{
                        /*
                         var packet = Ctrl.Text._getData(id);
                         UI.skboards[0].updateText(id, packet);
                         **/
                    }
                }else{
                    this._setData(packet);
                    this._setTextLayer(id);
                }
                
            }else if(type == "2"){
                // 삭제
                this.remove(id);
            }
        },
        
        auth : function(){
            
            // data가 초기화 되기 전에는 clear를 처리하지 않는다. 최초 masterChange 패킷은 skip 처리
            this.cancel(false);
            
            this.toggleEditMode((Ctrl.penIdx == 0 && UI.scale == 1) ? true : false);
            
            this.changeEditorCursor();
            
            this.authDrag();
        },
        
        authDrag : function(){
            // authDrag ->
            if(PacketMgr.isMC && UI.scale == 1){
                $("div", "#textWrapper").draggable("enable");
            }else{
                $("div", "#textWrapper").draggable("disable");
            }
        },
        
        changeEditorCursor : function(){
            if(PacketMgr.isMC && UI.scale == 1){
                $("div", "#textWrapper").css("cursor", "text");
            }else{
                $("div", "#textWrapper").css("cursor", "default");
            }
        },
        
        _setData : function(packet){
            if(this.data == null) this.data = new Map();
            
            this.data.put(packet.id, packet);
        },
        
        _updateData : function(packet){
            if(this.data == null) this.data = new Map();
            
            this._removeData(packet.id);
            this.data.put(packet.id, packet);
        },
        
        _getData : function(id){
            if(this.data != null){
                return this.data.get(id);
            }
            return null;
        },
        
        _removeData : function(id){
            if(this.data != null) this.data.remove(id);
        },
        
        _syncEditor : function(packet){
            var canvas = document.getElementById("sketch" + UI.current);
            var canvasW = $(canvas).width();
            var canvasH = $(canvas).height();
            
            var txt = packet.text;
            var x = UI.getOrgX(canvasW, canvasH, packet.x);
            var y = UI.getOrgY(canvasW, canvasH, packet.y);
            var w = packet.w;
            var h = packet.h;
            var face = packet.face;
            var size = packet.size;
            var italic = packet.italic;
            var bold = packet.bold;
            var r = packet.r;
            var g = packet.g;
            var b = packet.b;
            var color = "#" + Ctrl.rgbToHex(r, g, b);
            
            $("#fontbox").css("left", x + "px");
            $("#fontbox").css("top", y + "px");
            $("#ffamily").val(face);
            $("#fsize").val(size);
            
            var canvasW = $(canvas).width();
            var canvasH = $(canvas).height();
            
            if(italic == "1"){
                $("#italic").addClass("checked");
                if(!$("#txt_area").hasClass("fitalic")){
                    $("#txt_area").addClass("fitalic");
                }
            }else{
                $("#italic").removeClass("checked");
                $("#txt_area").removeClass("fitalic");
            }
            
            if(bold == "1"){
                $("#bold").addClass("checked");
                if(!$("#txt_area").hasClass("fbold")){
                    $("#txt_area").addClass("fbold");
                }
            }else{
                $("#bold").removeClass("checked");
                $("#txt_area").removeClass("fbold");
            }
            
            // sync text area
            $("#txt_area").width(w);
            $("#txt_area").height(h);
            $("#txt_area").css("fontFamily", face);
            $("#txt_area").css("fontSize", size + "px");
            $("#txt_area").css("color", color);
            $("#txt_area").val(txt);
            
            $(".minicolors-swatch-color", "#fontbox").css("backgroundColor", "rgb("+r+","+g+","+b+")");
            $(".minicolors-input", "#fontbox").val(color);
        },
        
        cancel : function(forceSave){
            // pen 모드로 변경시 모든 정보를 저장하고 Text 모드를 해제한다.
            $("#text_btn").removeClass("checked");
            $("#text_btn2").removeClass("checked");
            
            if(forceSave) {
                /**
                 var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
                 PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
                 **/
                if(Ctrl.Text.isOpened() && (Ctrl.Text.current != "" || !Ctrl.Text.isBlank() )){
                    var saveRet = Ctrl.Text.save();
                    if(!saveRet){
                        var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
                        PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
                    }
                }
                
            }else{
                var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
                PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
            }
            
            Ctrl.Text.current = "";
            $("#fontbox").hide();
        },
        
        toggleEditMode : function(isShow){
            
            if(PacketMgr.isMC && isShow){
                var canvas = document.getElementById("sketch" + UI.current);
                var canvasW = $(canvas).width();
                var canvasH = $(canvas).height();
                
                // $("div", "#textWrapper").show();
                $("div", "#textWrapper").each(function(){
                    var id = $(this).attr("id").replace("txt_", "");
                    var packet = Ctrl.Text._getData(id);
                    if(packet != null){
                        var x = UI.getOrgX(canvasW, canvasH, packet.x);
                        var y = UI.getOrgY(canvasW, canvasH, packet.y);
                        
                        $(this).css("left", x + "px");
                        $(this).css("top", y + "px");
                    }
                    
                    $(this).show();
                });
                
                // show();
            }else{
                $("div", "#textWrapper").hide();
            }
        },
        
        toggle : function(){
            if(!Ctrl.isHand()){
                Ctrl.toggleRC(0, -1, false);
            }
            
            if($("#text_btn").hasClass("checked")){
                Ctrl.Text.cancel(true);
                Ctrl._setPointer("1");
            }else{
                $("#text_btn").addClass("checked");
                $("#text_btn2").addClass("checked");
                Ctrl._setPointer("2");
            }
            
            // Ctrl.BGImg.auth();
            Ctrl.BGImg.auth();
            
            PDFViewer.auth();
        },
        
        active : function(){
            
        },
        
        toggleEdit : function(name){
            if($("#txt_area").hasClass(name)){
                $("#txt_area").removeClass(name);
            }else{
                $("#txt_area").addClass(name);
            }
        },
        
        destroy : function(){
            
            $("#sketch" + UI.current).unbind("click");
            
            $("#txt_area").unbind("blur");
            
            $("#txt_area").unbind("change");
            
            $("#txt_area").unbind("keydown");
            
            $("#txt_area").unbind("keyup");
            
            $(".ffamily", "#fontbox").unbind("change");
            
            $(".fsize", "#fontbox").unbind("change");
            
            $(".ffamily", "#fontbox").unbind("click");
            
            $(".fsize", "#fontbox").unbind("click");
            
            $("textarea", "#fontbox").unbind("blur");
            
            $("a.edit_btn", "#fbox_wrap").unbind("click");
            
            $("#txt_del_btn", "#fbox_wrap").unbind("click");
            
            $(".miniColor_jqueryP", "#fbox_wrap").minicolors("destroy");
            
            $("div", "#textWrapper").unbind("click");
            
            $("div", "#textWrapper").draggable("destroy");
        }
    },
    
    Capture : {
        open : function(){
            var url = Utils.addContext(_url("popup.capture"));
            Utils.popup(url, "popup_capture", 700, 550);
        },
        destroy : function(){
        }
    },
    
    Room : {
        
        init : function(){
            this._setEvent();
        },
        
        _setEvent : function(){
            $("#ck_passwd").click(function(){
                if(!$(this).is(":checked")){
                    $("#passwd_txt").val("");
                }
            });
            
            $("#btn_room_update").click(function(){
                Ctrl.Room.update();
            });
        },
        
        update : function(){
            if(PacketMgr.userid != PacketMgr.creatorid){
                // Ctrl.Msg.show(_msg("not.allow"));
                Ctrl.Msg.auth(true);
                return;
            }
            
            var ckAuthType = $("#ck_authtype").is(":checked") ? "1" : "0";
            var ckUsePasswd = $("#ck_passwd").is(":checked");
            var passwdTxt = ckUsePasswd ? $("#passwd_txt").val() : "";
            var chatOpt = $("#ck_chat").is(":checked") ? "1" : "0";
            var cmtOpt = $("#ck_cmt").is(":checked") ? "1" : "0";
            var expOpt = $("#ck_exp").is(":checked") ? "1" : "0";
            var title = RoomSvr.roomtitle;
            if(typeof(title) != "string"){
                title = $("#room_title").val();
            }
            
            /**
             var title = $("#title_txt_input").val();
             if(title.trim() == ""){
             Ctrl.Msg.show(_msg("insert.title"));
             return;
             }
             **/
            var __getBytes = function(txt){
                var onechar;
                var tmpStr = new String(txt);
                var temp   = tmpStr.length;
                var tcount = 0;
                // byte -> length
                for (var k=0; k<temp; k++) {
                    onechar = tmpStr.charAt(k);
                    if (escape(onechar) == '%0D') { } else if (escape(onechar).length > 4) { tcount += 2; } else { tcount++; }
                }
                
                return tcount;
            }
            
            var tcount = __getBytes(title);
            if(tcount > 40){
                Ctrl.Msg.show(_msg("validation.title"));
                return;
            }
            
            if(ckUsePasswd && passwdTxt.trim() == ""){
                Ctrl.Msg.show(_msg("insert.passwd"));
                return;
            }
            
            if(ckUsePasswd && (passwdTxt.length < 4 || passwdTxt.length > 8)){
                Ctrl.Msg.show(_msg("validation.passwd"));
                return;
            }
            
            if(ckUsePasswd && !passwdTxt.isEngNum()) {
                Ctrl.Msg.show(_msg("validation.passwd.kor"));
                return;
            }
            
            PacketMgr.Master.updateRoomInfo(ckAuthType, chatOpt, cmtOpt, expOpt, title, passwdTxt);
            
            $("#room_title").html(title);
            $("#setup_box").hide();
            
            // 모달에서 띄운경우
            $("#titleModal").hide();
            
            Ctrl.Msg.show(_msg("msg.success.infomation"));
        },
        
        updateRoomInfo : function(packet){
            PacketMgr.isAllowMaster = (packet.authtype == "1") ? true : false;
            PacketMgr.isAllowChat = (packet.chatopt == "1") ? true : false;
            PacketMgr.isAllowComment = (packet.cmtopt == "1") ? true : false;
            PacketMgr.isAllowExport = (packet.expopt == "1") ? true : false;
            
            Ctrl.Member.authTypeChange(packet.authtype);
            
            // 버튼 disable 처리 할것 !!
            RoomSvr.roomtitle = packet.title;
            $("#room_title").html(packet.title);
            
            Ctrl.Msg.show(_msg("msg.success.infomation"));
        },
        
        destroy : function(){
            $("#ck_passwd").unbind("click");
            
            $("#btn_room_update").unbind("click");
        }
        
    },
    
    init : function() {
        /***************** 판서도구 ******************** */
        this.colorIdx = 0;
        
        var r = this.colorMap[this.colorIdx]["r"];
        var g = this.colorMap[this.colorIdx]["g"];
        var b = this.colorMap[this.colorIdx]["b"];
        
        this.strokeStyle = "#" + this.rgbToHex(r, g, b);
        this.hStrokeStyle = "#" + this.rgbToHex(r, g, b);
        this.lStrokeStyle = "#" + this.rgbToHex(r, g, b);
        this.sStrokeStyle = "#" + this.rgbToHex(r, g, b);
        this.cStrokeStyle = "#" + this.rgbToHex(r, g, b);
        
        this._setEvent();
        
        // 초기 prev canvas 그리기
        this._drawPrevCanvas(5);
        
        this._drawPrevCanvas(6);
        
        this._drawPrevCanvas(7);
        
        Ctrl.Background.init();
        
        Ctrl.Room.init();
        
        Ctrl.Comment.init();
        
        Ctrl.Memo.init();
        
        Ctrl.Uploader.setProgress();
        
        Ctrl.Text.init();
        
        Ctrl.Member.init();
    },
    
    setMyNetworkStatus : function(isOnline, skipModal){
        var displayIcon = function(flag){
            if(flag){
                $("#ico_connect").removeClass("off");
                $("#ico_connect").attr("title", _msg("online"));
            }else{
                if(!$("#ico_connect").hasClass("off")){
                    $("#ico_connect").addClass("off");
                }
                $("#ico_connect").attr("title", _msg("offline"));
            }
        }
        
        if(isOnline){
            displayIcon(true);
        }else{
            displayIcon(false);
            
            if(PacketMgr.isMC && !skipModal){
                Ctrl.Modal.network();
            }
        }
    },
    
    avgrund : function(isShow, id){
        // 사용 안함
        var useAvgrund = false;
        if(!useAvgrund) return;
        
        // 사용 안함
        if(!Utils.mobile()){
            if(isShow){
                Avgrund.show("#" + id);
            }else{
                Avgrund.hide("#" + id);
            }
        }
        
    },
    
    callCurPensetting : function(isSend){
        isSend = (typeof(isSend) != "undefined") ? isSend : true;
        if (this.penIdx == 1 || this.penIdx == 2) {
            this._callPensetting(4, isSend);
        } else if (this.penIdx == 3) {
            this._callPensetting(5, isSend);
        } else if (this.penIdx == 4) {
            this._callPointer(isSend);
        } else if (this.penIdx == 5){
            this._callPensetting(7, isSend);
        } else if (this.penIdx == 6){
            this._callPensetting(8, isSend);
        } else if (this.penIdx == 7){
            this._callPensetting(9, isSend);
        }
    },
    
    isHand : function(){
        return ($("#hbar_0").css('display') == 'none') ? false : true;
    },
    
    isText : function(){
        return ($("#text_btn").hasClass("checked") || Ctrl.Text.isOpened()) ? true : false;
    },
    
    _checkAuth : function(showMsg) {
        // 메시지 한곳에서 처리하기 위해 이곳에서 작업함
        // Utils.log("flag : " + PacketMgr.isMC + ", showMsg : " + showMsg);
        if(!PacketMgr.isMC && showMsg){
            if((!PacketMgr.isCreator && !PacketMgr.isAllowMaster) || PacketMgr.isGuest){
                Ctrl.Msg.show(_msg("noti.not.allow"));
                return;
            }
            // Ctrl.Msg.auth(false);
            if(this._checkWaiting() ){ // 누가 드로잉중이라 기달려야 하는 상황 이라면?
                var masterUserInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
                var authContent = masterUserInfo.usernm + _msg("noti.leading.host");
                Ctrl.Msg.show(authContent);
            }else{
                // 나로 masterChange
                var userId = PacketMgr.userid;
                PacketMgr.Master.masterChange(userId);
            }
        }
        return PacketMgr.isMC;
    },
    
    _checkWaiting : function(){
        var currentTime = new Date().getTime();
        return (currentTime < (PacketMgr.lastTime + 3000)) ? true : false;
    },
    
    //------------------------------------------- new code ----------------------------------------------------
    _setEvent : function(){
        this._setRemoteControl();
        
        this._setMenuToggle();
        
        this._setDocEvent();
        
        this._setPenEvent();
        
        this._setSlider();
        
        this._setChatEvent();
    },
    
    _setRemoteControl : function(){
        
        var isMobile =  Utils.mobile();
        
        // Utils.mobile()
        
        $('ul.circle_menu').circleMenu({
            depth : 1,
        item_diameter: 40,
        circle_radius: 150, // 메뉴가 펼쳐지는 너비
        trigger: Utils.mobile() ? 'click' : "hover", // 이벤트 옵션 (hover, click)
        speed: 600,
        delay: 400,
        step_in: -20,
        step_out: 60,
        angle:{
        start: 0,
        end: 90
        },
            // angle + item count
        depth2:{
        item_diameter: 40,
            circle_radius : 80,
            angle3 : {
            start: 10,
            end: 80
            }
        },
            select : function(ev, li){
                // // console.log("ev : " + JSON.stringify(ev) + ", li : " + li);
                var node = li.get(0);
                if(node.nodeName.toLowerCase() == "li" && typeof(node.id) != "undefined"){
                    var id = node.id;
                    var idx = -1;
                    var subIdx = -1;
                    // // console.log("node.className : " + node.className + ", node.id : " + id);
                    if(id.indexOf("_") > -1){
                        var token = id.replace("cmenu", "");
                        idx = parseInt(token.split("_")[0], 10);
                        subIdx = parseInt((token.split("_")[1] == "root" ? 1 : token.split("_")[1]) , 10);
                        
                    }else{
                        idx = parseInt(id.replace("cmenu", ""), 10);
                    }
                    
                    // idx = idx == null ? "" : idx;
                    if(isNaN(idx)) return;
                    
                    Ctrl.toggleRC(idx, subIdx, true);
                }
            }
        });
        
        /**
         $("#file2").change(function(e){
         if(!Ctrl._checkAuth(true)) return;
         Ctrl.Uploader.uploadImg(e, "file2");
         });
         **/
        
        $("#text_btn2").click(function(e){
            if(!Ctrl._checkAuth(true)) return;
            
            Ctrl.Text.toggle($(this) );
        });
    },
    
    _saveCanvas : function(){
        
        try{
            UI.skboards[0].save();
            var saveCanvas = $("#saveCanvas").get(0);
            if(saveCanvas){
                // make represent thumbnail
                Ctrl.Uploader.save(saveCanvas.toDataURL() );
                if(!PacketMgr.isAllowExport && !PacketMgr.isMC){
                    Ctrl.Msg.show(_msg("not.allow"));
                    // $(saveCanvas).remove();
                    return;
                }
                if(!Utils.browser("msie")) {
                    var img = document.createElement("img");
                    img.setAttribute('crossOrigin', 'anonymous');
                    img.src = saveCanvas.toDataURL();
                    
                    img.style.display = "none";
                    document.body.appendChild(img);
                    
                    // var img = document.images[0];
                    img.onload = function() {						
                        // atob to base64_decode the data-URI
                        var image_data = atob(img.src.split(',')[1]);
                        // Use typed arrays to convert the binary data to a Blob
                        var arraybuffer = new ArrayBuffer(image_data.length);
                        var view = new Uint8Array(arraybuffer);
                        for (var i=0; i<image_data.length; i++) {
                            view[i] = image_data.charCodeAt(i) & 0xff;
                        }
                        try {
                            // This is the recommended method:
                            var blob = new Blob([arraybuffer], {type: 'application/octet-stream'});
                        } catch (e) {
                            // The BlobBuilder API has been deprecated in favour of Blob, but older
                            // browsers don't know about the Blob constructor
                            // IE10 also supports BlobBuilder, but since the `Blob` constructor
                            //  also works, there's no need to add `MSBlobBuilder`.
                            var bb = new (window.WebKitBlobBuilder || window.MozBlobBuilder);
                            bb.append(arraybuffer);
                            var blob = bb.getBlob('application/octet-stream'); // <-- Here's the Blob
                        }
                        
                        // Use the URL object to create a temporary URL
                        var url = (window.URL || window.webkitURL).createObjectURL(blob);
                        //location.href = url; // <-- Download!
                        
                        var a = document.createElement("a");
                        document.body.appendChild(a);
                        a.style = "display: none";
                        a.href = url;
                        a.download = RoomSvr.roomtitle + ".png" ;
                        a.click();					        
                        // window.URL.revokeObjectURL(url); 
                        
                        $(saveCanvas).remove();
                        $(a).remove();
                        $(img).remove();
                    };
                    
                }else{
                    Ctrl.Uploader.download(saveCanvas.toDataURL() );				        
                    $(saveCanvas).remove();	   
                }
            }
        }catch(e){			
            console.log(e);
            var saveCanvas = $("#saveCanvas").get(0);
            if(saveCanvas){
                $(saveCanvas).remove();	  
            }	
        } 
    },
    
    _setMenuToggle : function(){
        // IE에서는 이미지 파일이 많으면 다운로드 안되므로 주의하자 
        $("#save_btn").click(this._saveCanvas);
        
        $("#attend_btn").click(function(){			
            $("#bg_box").hide();
            $("#setup_box").hide();	
            $("#invite_layer").hide();
            
            $("#user_wrapper").slideToggle({easing:"easeInCubic", duration:800});
        });
        
        $("#setup_btn").click(function(){
            $("#user_wrapper").hide();
            $("#bg_box").hide();
            $("#invite_layer").hide();
            
            $("#setup_box").slideToggle({easing:"easeInCubic", duration:800});
        });
        
        $("#invite_noti_btn").click(function(){			
            Notify.Invite.show("canvas");
            
            $("#bg_box").hide();
            $("#user_wrapper").hide();
            $("#setup_box").hide();
            
        });
        
        $("#bg_btn").click(function(){
            $("#user_wrapper").hide();
            $("#setup_box").hide();
            $("#invite_layer").hide();
            
            $("#bg_box").slideToggle({easing:"easeInCubic", duration:800});
        });
        
        //--- 우측 하단 메뉴
        $(".btn_chat", "#quick_wrapper").click(function(){
            Ctrl.Chat.chatBadgeCnt = 0;
            $("#chat_badge").hide();
            $(this).removeClass("checked");
            // $("#chat_wrapper").slideToggle({easing:"easeInCubic", duration:800});
            
            Ctrl.Chat.toggle();			
        });
        
        $(".btn_comment", "#quick_wrapper").click(function(){
            Ctrl.Comment.commentBadgeCnt = 0;
            $("#comment_badge").hide();
            $(this).removeClass("checked");
            Ctrl.Comment.toggle();			
        });
        
        $(".btn_memo", "#quick_wrapper").click(function(e){
            if(!Ctrl._checkAuth(true)) return;
            
            Ctrl.Memo.add(e);			
        });
        
        $(".btn_text", "#quick_wrapper").click(function(e){
            if(!Ctrl._checkAuth(true)) return;
            
            Ctrl.Text.toggle($(this) );
        });
        
        $(".btn_background_del", "#quick_wrapper").click(function(e){
            if(!Ctrl._checkAuth(true)) return;			
            Ctrl.BGImg.removeAll(e);
        });
        
        $("#bg_file").click(function(){
            if(!Ctrl._checkAuth(true)) return;
            $("#file1").click();
        });
        
        $("#bg_file2").click(function(){
            if(!Ctrl._checkAuth(true)) return;
            $("#file1").click();
        });
        
        $("#file1").change(function(e){
            if(!Ctrl._checkAuth(true)) return;
            Ctrl.Uploader.uploadImg(e, "file1");			
        });
        
        $("#bg_pdf").click(function(){
            if(!Ctrl._checkAuth(true)) return;
            $("#file_pdf").click();
        });
        
        $("#file_pdf").change(function(e){
            if(!Ctrl._checkAuth(true)) return;
            Ctrl.Uploader.uploadPdf(e, "file_pdf");			
        });
        
        
        $("#exit").click(function(e){
            var type = (PacketMgr.isCreator) ? "creator" : "";
            Ctrl.Modal.exit(type);
        });
        
        $("#room_title").click(function(e){
            var type = (PacketMgr.isCreator) ? "creator" : "";
            Ctrl.Modal.title(type);
        });
        
        $("#invite_btn").click(function(){
            Ctrl.Modal.invite();
        });
        
        $(".btn_capture", "#quick_wrapper").click(function(e){
            if(!Ctrl._checkAuth(true)) return;			
            
            // Ctrl.Text.toggle($(this) );			
            Ctrl.Capture.open();
        });
        
        $("ul.bottom_cmenu", "#quick_wrapper").circleMenu({
        item_diameter: 40, 
        circle_radius: 65, // 메뉴가 펼쳐지는 너비
        trigger: 'hover', // 이벤트 옵션 (hover, click)
        angle:{
        start: -135,
        end: -45
        } // 메뉴 펼쳐지는 각도
        });
        
    },
    
    toggleRC : function(penIdx, subIdx, isCheck){		
        // 1. annotation setting
        // 2. toggle header
        // 1, 3, 6, 7, 8, 9
        if(isCheck && !Ctrl._checkAuth(true)) return;	
        
        if(penIdx > 4){
            penIdx = penIdx + subIdx - 1;			
        }
        this.penIdx = penIdx;
        
        // background image disable
        this.BGImg.disable();
        
        PDFViewer.disable();
        
        this.toggleHeader();
        
        if(this.penIdx == 0){
            
            this.toggleHand(isCheck);
            
        }else if(this.penIdx == 1 || this.penIdx == 2) {
            
            this._callPensetting(4);
            
        }else if(this.penIdx == 4){
            
            this.pointerIdx = subIdx - 1;
            
            this._drawPrev(4);
            
            this._callPointer();
        }else{
            this._callPensetting(this.penIdx + 2);
        }
        
        this.changeCursor(this.penIdx == 0 ? "1" : "0");
        
        // text가 존재시 save
        Ctrl.Text.cancel(true);
        
        Ctrl.Text.toggleEditMode(this.penIdx == 0 ? true : false);		
    },
    
    // header menu
    toggleHeader : function(){
        var barIdx = this.penIdx;
        
        $(".mopt_bar", "#hmenubar").hide();
        
        $("#hbar_" + barIdx).show();
        
        if(barIdx == 0){
            if(!$("#handCtl").hasClass("checked")) $("#handCtl").addClass("checked");
        }else{
            $("#handCtl").removeClass("checked");
        }
    },
    
    toggleBookmark : function(){
        var checked = $("#bmCtl").hasClass("btn_star_sel");
        
        // confirm.add.bookmark
        
        var msg = checked ? _msg("confirm.remove.bookmark") : _msg("confirm.add.bookmark"); 
        Ctrl.Modal.confirm(msg, function(){			
            var ctx = checked ? _url("bookmark.remove") : _url("bookmark.add");
            var url = Utils.addContext( ctx );
            var params = {			
                roomid : RoomSvr.roomid			
            };
            
            Utils.request(url, "json", params, function(data){
                if(data.result == "0"){
                    if(checked) {
                        $("#bmCtl").removeClass("btn_star_sel").addClass("btn_star");
                    }else {
                        $("#bmCtl").removeClass("btn_star").addClass("btn_star_sel");
                    }				
                }else if(data.result == -503){
                    Ctrl.Msg.show(_msg("bookmark.already.added"));					
                }else if(data.result == -504){
                    Ctrl.Msg.show(_msg("bookmark.limit.added"));					
                }
            });			
        });
        
    },
    
    _setDocEvent : function(){
        
        $("#bmCtl").click(function(){
            Ctrl.toggleBookmark();
        });
        
        $("#handCtl").click(function(){
            Ctrl.toggleRC(0, -1, false);			
        });
        
        $("#undoCtl").click(function() {		
            if(!Ctrl._checkAuth(true)) return;
            
            PacketMgr.Master.undo(UI.current);
        });
        
        $("#redoCtl").click(function() {
            if(!Ctrl._checkAuth(true)) return;
            
            PacketMgr.Master.redo(UI.current);
        });
        
        //-------- 권한 가져오기
        /**
         $("#authBtn").click(function(){			
         // 마스터 모드인지 먼저 체크
         if(PacketMgr.isCreator){
         if(PacketMgr.creatorid == PacketMgr.masterid) return;
         
         // if(!confirm(_msg("confirm.get.auth"))) return;
         Ctrl.Modal.confirm(_msg("confirm.get.auth"), function(){
         var userId = PacketMgr.userid;
         PacketMgr.Master.masterWithDraw(userId);	
         });
         
         }else{
         // 확인 모드
         // if(!confirm(_msg("confirm.get.auth"))) return;
         Ctrl.Modal.confirm(_msg("confirm.get.auth"), function(){
         var userId = PacketMgr.userid;
         PacketMgr.Master.masterChange(userId);					
         });
         
         }
         });
         **/
        
        
        
        //------------ zoom
        $("#zoomOptBtn").click(function(){
            if (!Ctrl._checkAuth(true)) {
                return;
            }
            
            Ctrl.toggleZoomOpt();
        });
        
        $("a.zoomPct", "#zoomOpt").click(function(){
            if (!Ctrl._checkAuth(true)) {
                Ctrl.toggleZoomOpt();
                return;
            }
            
            var val = $(this).attr("value");
            Ctrl.zoom(val);		
            Ctrl.toggleZoomOpt();
            
        });
        
        // 누르고 있을때 계속 증가 시키기 위해 interval 적용
        var interval_zin = null;
        var interval_zout = null;
        $("#zoomin").mousedown(function(){
            if (!Ctrl._checkAuth(true)) return;
            
            // 무한 재귀
            interval_zin = setInterval(function(){
                Ctrl.zoomIn("0");
            }, "200");
        });				
        
        $("#zoomin").mouseout(function(){
            clearInterval(interval_zin);
        });		
        $("#zoomin").mouseup(function(){			
            clearInterval(interval_zin);
            
            if (!Ctrl._checkAuth(false)) return;
            
            Ctrl.zoomIn("1");
        });
        
        $("#zoomout").mousedown(function(){
            if (!Ctrl._checkAuth(true)) return;			
            // 무한 재귀
            interval_zout = setInterval(function(){
                Ctrl.zoomOut("0");
            }, "200");
        });
        
        $("#zoomout").mouseout(function(){
            clearInterval(interval_zout);
        });
        
        $("#zoomout").mouseup(function(){
            clearInterval(interval_zout);
            if (!Ctrl._checkAuth(false)) return;
            Ctrl.zoomOut("1");
        });	
        
    },
    
    _setPenEvent : function(){
        // pen picker popup
        $("#pen_picker").click(function(){
            $("#pen_color_wrap").toggle();
        });
        
        $("#lpen_picker").click(function(){
            $("#lpen_color_wrap").toggle();
        });
        
        $("#spen_picker").click(function(){
            $("#spen_color_wrap").toggle();
            
            $("#spen_color_wrap2").hide();			
        });
        
        $("#spen_picker2").click(function(){
            $("#spen_color_wrap2").toggle();
            
            $("#spen_color_wrap").hide();
        });
        
        $("#cpen_picker").click(function(){
            $("#cpen_color_wrap").toggle();
            
            $("#cpen_color_wrap2").hide();
        });
        
        $("#cpen_picker2").click(function(){
            $("#cpen_color_wrap").hide();
            
            $("#cpen_color_wrap2").toggle();
        });
        
        // pen color 
        $("a.color_select2", "#pen_color_wrap").click(function(index){
            var idx = $(this).index();
            Ctrl.__setPenColor($(this), idx, 1, "0");
        });
        
        // line pen color
        $("a.color_select2", "#lpen_color_wrap").click(function(index){
            // Ctrl.Msg.show("click color");			
            var idx = $(this).index();
            Ctrl.__setPenColor($(this), idx, 5, "0");
        });
        
        // square pen color
        $("a.color_select2", "#spen_color_wrap").click(function(index){
            // Ctrl.Msg.show("click color");			
            var idx = $(this).index();
            Ctrl.__setPenColor($(this), idx, 6, "0");
        });
        
        $("a.color_select2", "#spen_color_wrap2").click(function(index){
            // Ctrl.Msg.show("click color");			
            var idx = $(this).index();
            Ctrl.__setPenColor($(this), idx, 6, "1");
        });
        
        $("a.color_delete", "#spen_color_wrap").click(function(index){
            // 배경 삭제 
            var idx = $(this).index();
            // Ctrl.__setPenColor($(this), idx, 6, "2");
            Ctrl.__setFillClear($(this), idx, 6);
        });
        
        // circle pen color
        $("a.color_select2", "#cpen_color_wrap").click(function(index){
            // Ctrl.Msg.show("click color");			
            var idx = $(this).index();
            Ctrl.__setPenColor($(this), idx, 7, "0");
        });
        
        $("a.color_select2", "#cpen_color_wrap2").click(function(index){
            // Ctrl.Msg.show("click color");			
            var idx = $(this).index();
            Ctrl.__setPenColor($(this), idx, 7, "1");
        });
        
        $("a.color_delete", "#cpen_color_wrap").click(function(index){
            // 배경 삭제
            var idx = $(this).index();
            // Ctrl.__setPenColor($(this), idx, 7, "2");
            Ctrl.__setFillClear($(this), idx, 7);
        });
        
        
        // index번호가 안맞아서 방식 변경 
        $("a.color_select2", "#hbar_4").click(function(index){
            var idx = $("a.color_select2", "#hbar_4").index(this) + 1;			
            Ctrl.__setPenColor($(this), idx, 4, "0");
        });
        
        $("#pen_preview_5", "#hbar_5").click(function(index){
            $(".preview_box1", "#hbar_5").toggle();
        });
        
        $("#pen_preview_6", "#hbar_6").click(function(index){
            $(".preview_box2", "#hbar_6").toggle();
        });
        
        $("#pen_preview_7", "#hbar_7").click(function(index){
            $(".preview_box3", "#hbar_7").toggle();
        });
        
        // set custom color
        $(".miniColor_jqueryP", "#hmenubar").each( function() {			
            var idx = $(".miniColor_jqueryP", "#hmenubar").index(this) + 1;
            $(this).minicolors({
            control: $(this).attr('data-control') || 'hue',
            defaultValue: $(this).attr('data-defaultValue') || '',
                inline: $(this).attr('data-inline') === 'true',
            letterCase: $(this).attr('data-letterCase') || 'lowercase',
            opacity: $(this).attr('data-opacity'),
            position: $(this).attr('data-position') || 'bottom right',
            change: function(hex, opacity) {
                var menu = idx == 1 ? 1 : idx == 2 ? 5 : idx == 3 ? 6 : idx == 4 ? 6 : idx == 5 ? 7 : idx == 6 ? 7 : 1; 
                var fillType = idx == 4 || idx == 6 ? "1" : "0";
                var code = hex.replace("#", "");
                var colorIdx = 10; 
                
                Ctrl.__setPenCustomColor($(this), colorIdx, menu, fillType, code);					
            },
            theme: 'default'
            });		
        });
        
        // eraser
        $("#clear_btn").click(function(){
            if(!Ctrl._checkAuth(true)) return;
            
            // if(!confirm(_msg("confirm.remove.history"))) return;
            Ctrl.Modal.confirm(_msg("confirm.remove.history"), function(){
                PacketMgr.Master.eraserMode(2, UI.current);	
            });
        });
    },
    
    __setPenCustomColor : function(thisNode, idx, penIdx, figureType, code){
        if (!Ctrl._checkAuth(true)) return;
        
        var __checked = function(penIdx){
            var wrapperId = ((penIdx == 2) ? "h" : (penIdx == 4) ? "p" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_color_wrap";			
            var doChecked = function(wrapperId){				
                // if(figureType == "1") wrapperId += "2";
                $("a", "#" + wrapperId).each(function() {
                    $(this).removeClass("checked");
                });
                
                $("span.minicolors-swatch-color", "#" + wrapperId).addClass("checked");				
            }
            
            if(penIdx == 6 || penIdx == 7){
                if(figureType == "1"){
                    wrapperId += "2";
                    doChecked(wrapperId, thisNode);
                }else{
                    if(idx == 8){
                        $("a", "#" + wrapperId + "2").each(function() {
                            $(this).removeClass("checked");
                        });						
                    }else if(idx < 10){
                        
                        // custom 일때 checked 제거 
                        $("a", "#" + wrapperId + "2").each(function() {
                            $(this).removeClass("checked");
                        });
                        
                        // customColor remove
                        $("span.minicolors-swatch-color", "#" + wrapperId + "2").removeClass("checked");
                        
                        $("a", "#" + wrapperId + "2").eq(idx-1).addClass("checked");
                        
                    }
                    
                    doChecked(wrapperId, thisNode);				
                }				
            }else{
                doChecked(wrapperId, thisNode);
            }
        }
        
        var __pick = function(penIdx, r, g, b){
            var picker = ((penIdx == 2) ? "h" : (penIdx == 4) ? "p" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_picker";
            
            if(penIdx == 6 || penIdx == 7){
                if(r == -1 && g == -1 && b == -1){
                    $("span", "#" + picker).css("background", "transparent");
                }else{
                    if(figureType == "0"){
                        $("span", "#" + picker).css("background", "rgb("+r+","+g+","+b+")");
                    }
                    
                    $("span", "#" + picker + "2").css("border", "5px solid rgb("+r+","+g+","+b+")");
                }				
            }else{
                // isFigureLine
                $("span", "#" + picker).css("background", "rgb("+r+","+g+","+b+")");
            }
        }
        
        var colorIdx = idx - 1;
        
        var r = this.hexToRgb(code)["r"];
        var g = this.hexToRgb(code)["g"];
        var b = this.hexToRgb(code)["b"];		
        
        var menuSelect = (penIdx == 1 || penIdx == 2) ? 4 : (penIdx + 2);
        
        // 포인터 
        if(penIdx == 4){			
            this.pColorIdx = colorIdx;
            
            this.changeCursor("0");
            
            this._drawPrev(penIdx);
            
            this._callPointer();
            
        }else{
            //---- checked
            __checked(penIdx);
            
            //----- prev2
            __pick(penIdx, r, g, b);
            
            // 1,2,5,6,7 들어옴
            var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
            this[this.penIdx == 1 ? "colorIdx" : token + "ColorIdx"] = colorIdx;
            
            if(penIdx == 6 || penIdx == 7){
                if(figureType == "2"){
                    this[token + "FillStyle"] = "#" + code;					
                }else{
                    if(figureType == "0"){
                        this[token + "FillStyle"] = "#" + code;					
                    }
                    this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = "#" + code;
                }
            }else{
                this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = "#" + code;
            }
            
            this.changeCursor("0");
            
            this._drawPrev(penIdx, figureType);
            
            this._callPensetting(menuSelect);
        }
    },
    
    __setFillClear : function(thisNode, idx, penIdx){
        if (!Ctrl._checkAuth(true)) return;
        
        // figureType -> 0(기본), 1(fill), 2(속
        var __checked = function(penIdx){
            //----- 1. selected 처리
            var wrapperId = ((penIdx == 2) ? "h" : (penIdx == 4) ? "p" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_color_wrap";			
            var doChecked = function(wrapperId){				
                // if(figureType == "1") wrapperId += "2";
                $("a", "#" + wrapperId).each(function() {
                    $(this).removeClass("checked");
                });
                
                // customColor remove
                $("span.minicolors-swatch-color", "#" + wrapperId).removeClass("checked");
                
                thisNode.addClass("checked");
                
                // hide
                // $("#" + wrapperId).hide();
            }
            
            
            if(idx == 8){
                $("a", "#" + wrapperId + "2").each(function() {
                    $(this).removeClass("checked");
                });						
            }else if(idx < 10){
                
                // custom 일때 checked 제거 
                $("a", "#" + wrapperId + "2").each(function() {
                    $(this).removeClass("checked");
                });
                
                // customColor remove
                $("span.minicolors-swatch-color", "#" + wrapperId + "2").removeClass("checked");
                
                $("a", "#" + wrapperId + "2").eq(idx-1).addClass("checked");
            }
            
            doChecked(wrapperId, thisNode);				
        }
        
        var __pick = function(penIdx, r, g, b){
            var picker = ((penIdx == 2) ? "h" : (penIdx == 4) ? "p" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_picker";
            
            // fill color
            // picker += "2";
            // line만 draw
            if(r == -1 && g == -1 && b == -1){
                $("span", "#" + picker).css("background", "transparent");
            }else{
                if(figureType == "0"){
                    $("span", "#" + picker).css("background", "rgb("+r+","+g+","+b+")");
                }
                
                $("span", "#" + picker + "2").css("border", "5px solid rgb("+r+","+g+","+b+")");
            }
        }
        
        var colorIdx = idx - 1;
        
        var colorMap = Ctrl.colorMap[colorIdx];
        var r = colorMap.r;
        var g = colorMap.g;
        var b = colorMap.b;
        
        var code = this.rgbToHex(r, g, b);
        var menuSelect = (penIdx == 1 || penIdx == 2) ? 4 : (penIdx + 2);
        
        //---- checked
        __checked(penIdx);
        
        //----- prev2
        __pick(penIdx, r, g, b);
        
        // 1,2,5,6,7 들어옴
        var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
        this[this.penIdx == 1 ? "colorIdx" : token + "ColorIdx"] = colorIdx;
        
        this[token + "FillStyle"] = "";					
        
        this.changeCursor("0");
        
        this._drawPrev(penIdx, "2");
        
        this._callPensetting(menuSelect);	 
        
    },
    
    __setPenColor : function(thisNode, idx, penIdx, figureType) {		
        if (!Ctrl._checkAuth(true)) return;
        
        // figureType -> 0(기본), 1(fill), 2(속
        var __checked = function(penIdx){
            //----- 1. selected 처리
            var wrapperId = (penIdx == 4) ? "hbar_4" : ((penIdx == 2) ? "h" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_color_wrap";			
            var doChecked = function(wrapperId){				
                // if(figureType == "1") wrapperId += "2";
                
                $("a", "#" + wrapperId).each(function() {
                    $(this).removeClass("checked");
                });
                
                // customColor remove
                $("span.minicolors-swatch-color", "#" + wrapperId).removeClass("checked");
                
                thisNode.addClass("checked");
            }
            
            
            if(penIdx == 6 || penIdx == 7){
                if(figureType == "1"){
                    wrapperId += "2";
                    doChecked(wrapperId, thisNode);
                }else{					
                    if(idx == 8){
                        $("a", "#" + wrapperId + "2").each(function() {
                            $(this).removeClass("checked");
                        });						
                    }else if(idx < 10){
                        
                        // custom 일때 checked 제거 
                        $("a", "#" + wrapperId + "2").each(function() {
                            $(this).removeClass("checked");
                        });
                        
                        // customColor remove
                        $("span.minicolors-swatch-color", "#" + wrapperId + "2").removeClass("checked");
                        
                        $("a", "#" + wrapperId + "2").eq(idx-1).addClass("checked");
                    }
                    
                    doChecked(wrapperId, thisNode);
                }								
            }else{
                doChecked(wrapperId, thisNode);
            }			
        }
        
        var __pick = function(penIdx, r, g, b){
            var picker = ((penIdx == 2) ? "h" : (penIdx == 4) ? "p" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_picker";
            
            // fill color
            // picker += "2";
            if(penIdx == 6 || penIdx == 7){
                // line만 draw
                if(r == -1 && g == -1 && b == -1){
                    $("span", "#" + picker).css("background", "transparent");
                }else{
                    if(figureType == "0"){
                        $("span", "#" + picker).css("background", "rgb("+r+","+g+","+b+")");
                    }
                    
                    $("span", "#" + picker + "2").css("border", "5px solid rgb("+r+","+g+","+b+")");
                }				
            }else{
                // isFigureLine
                $("span", "#" + picker).css("background", "rgb("+r+","+g+","+b+")");
            }
        }
        
        var colorIdx = idx - 1;
        var colorMap = penIdx == 4 ? Ctrl.pointColorMap[colorIdx] : Ctrl.colorMap[colorIdx];
        
        var r = colorMap.r;
        var g = colorMap.g;
        var b = colorMap.b;
        
        var code = this.rgbToHex(r, g, b);
        var menuSelect = (penIdx == 1 || penIdx == 2) ? 4 : (penIdx + 2);
        
        // 포인터 
        if(penIdx == 4){			
            // set pointer Color
            this.pColorIdx = colorIdx;
            
            __checked(penIdx);
            
            this.changeCursor("0");
            
            this._drawPrev(penIdx);
            
            this._callPointer();
            
        }else{
            //---- checked
            __checked(penIdx);
            
            //----- prev2
            __pick(penIdx, r, g, b);
            
            // 1,2,5,6,7 들어옴
            var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
            this[this.penIdx == 1 ? "colorIdx" : token + "ColorIdx"] = colorIdx;
            
            if(penIdx == 6 || penIdx == 7){
                if(figureType == "2"){
                    this[token + "FillStyle"] = "#" + code;					
                }else{
                    if(figureType == "0"){
                        this[token + "FillStyle"] = "#" + code;					
                    }
                    this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = "#" + code;
                }
            }else{
                this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = "#" + code;
            }
            
            this.changeCursor("0");
            
            this._drawPrev(penIdx, figureType);
            
            this._callPensetting(menuSelect);
        }
    },
    
    _setSlider : function(){		
        $('#penSizeSlider').slider({
            max : 8,
            min : 1,
            value : 4,
            slide : function(e, ui) {
                var size = parseInt(ui.value);
                Ctrl.lineWidth = size;
                
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(1);
                Ctrl._callPensetting(4);				
            }
        });
        
        $('#opacitySlider').slider({
            max : 100,
            min : 30,
            value : 100,
            slide : function(e, ui) {				
                Ctrl.alpha = parseInt(ui.value);
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(1);
                Ctrl._callPensetting(4);
                
            }
        });
        
        // 1 5 10 15 20
        $("#epenSizeSlider").slider({
            max : 18,
            min : 4,
            value : 10,
            slide : function(e, ui) {
                var size = parseInt(ui.value);
                Ctrl.eLineWidth = size != 1 && size % 2 == 1 ? size + 1 : size;
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(3);
                Ctrl._callPensetting(5);	
            }
        });
        
        $('#lpenSizeSlider').slider({
            max : 8,
            min : 1,
            value : 4,
            slide : function(e, ui) {
                var size = parseInt(ui.value);
                Ctrl.lLineWidth = size;
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(5);
                Ctrl._callPensetting(7);	
            }
        });
        
        $('#lopacitySlider').slider({
            max : 100,
            min : 30,
            value : 100,
            slide : function(e, ui) {				
                Ctrl.lalpha = parseInt(ui.value);
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(5);
                Ctrl._callPensetting(7);
            }
        });
        
        $('#spenSizeSlider').slider({
            max : 8,
            min : 1,
            value : 4,
            slide : function(e, ui) {
                var size = parseInt(ui.value);
                Ctrl.sLineWidth = size;
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(6);
                Ctrl._callPensetting(8);	
            }
        });
        
        $('#sopacitySlider').slider({
            max : 100,
            min : 30,
            value : 100,
            slide : function(e, ui) {				
                Ctrl.salpha = parseInt(ui.value);
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(6);
                Ctrl._callPensetting(8);
            }
        });
        
        $('#cpenSizeSlider').slider({
            max : 8,
            min : 1,
            value : 4,
            slide : function(e, ui) {
                var size = parseInt(ui.value);
                Ctrl.cLineWidth = size;
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(7);
                Ctrl._callPensetting(9);	
            }
        });
        
        $('#copacitySlider').slider({
            max : 100,
            min : 30,
            value : 100,
            slide : function(e, ui) {				
                Ctrl.calpha = parseInt(ui.value);
                Ctrl.changeCursor("0");
                Ctrl._drawPrev(7);
                Ctrl._callPensetting(9);
            }
        });		
    },
    
    _setChatEvent : function(){
        //-- 
        $(".chat_close", "#chat_wrapper").click(function(){			
            $("#chat_wrapper").slideToggle({easing:"easeInCubic", duration:800});
        });		
        
        var sendChat = function (){
            if(!PacketMgr.isAllowChat && !PacketMgr.isMC){
                // Ctrl.Msg.show(_msg("not.allow"));
                Ctrl.Msg.show(_msg("not.allow"));
                $("#chatmsg").val("");
                return;
            }
            
            // 기본 채팅 허용
            var msg = $("#chatmsg").val();
            var target = $("#chattarget").val();
            // var targetNm = $("#chattarget").html();
            var targetNm = $("#chattarget option:selected").html();
            if(msg.trim() == ""){
                Ctrl.Msg.show(_msg("insert.msg"));
                $("#chatmsg").focus();
                return;
            }
            
            Ctrl.Chat.send(msg, target, targetNm);
            PacketMgr.Master.chat(msg, target, targetNm);
        };
        
        $("#sendchat").click(sendChat);
        
        $("#chatmsg").keydown(function(e){
            if(e.keyCode == 13){
                sendChat();
            }
        });
        
    },
    
    _drawPrev : function(penIdx, figureType){
        figureType = typeof(isFigureLine) == "undefined" ? "0" : figureType;
        
        var id = "pen_preview_" + penIdx;
        var token = (penIdx == 2 ? "h" : penIdx == 5 ? "l" : penIdx == 6 ? "s" : penIdx == 7 ? "c" : "")
        var lineWidthKey = penIdx == 1 ? "lineWidth" : (penIdx == 2) ? "hLineWidth" : (penIdx == 3) ? "eLineWidth" : (penIdx == 5) ? "lLineWidth" : (penIdx == 6) ? "sLineWidth" : (penIdx == 7) ? "cLineWidth" : "";
        var strokeStyle = penIdx == 1 ? this.strokeStyle : this[token + "StrokeStyle"];
        var fillStyle = penIdx == 6 ? this.sFillStyle : penIdx == 7 ? this.cFillStyle : null;
        
        var lineWidth = this[lineWidthKey];
        // var alpha = penIdx == 1 ? 100 : this[token + "alpha"];
        var alpha = this[token + "alpha"];
        
        if(penIdx == 1 || penIdx == 5){
            var idx = Ctrl.sizeList.indexOf(lineWidth);
            if(idx > -1){
                $("#" + id).attr("class", "");
                $("#" + id).addClass("value" + (idx+1));
            }
            
            r = this.hexToRgb(strokeStyle)["r"];
            g = this.hexToRgb(strokeStyle)["g"];
            b = this.hexToRgb(strokeStyle)["b"];
            
            var alphaRatio = (alpha * 0.01);
            
            $("#" + id).css("background", "rgba("+r+", "+g+", "+b+", "+ alphaRatio+")");
            
        }else if(penIdx == 3){
            var idx = Ctrl.eSizeList.indexOf(lineWidth);
            // Utils.log("id : "+ id +", idx : " + idx + ", lineWidth : " + lineWidth);
            if(idx > -1){
                $("#" + id).attr("class", "");
                $("#" + id).addClass("value" + (idx+1));
            }			
        }else if(penIdx == 4){
            // 	var pointerIdx = this.pointerIdx;	
            var className = "lp_c" + (this.pColorIdx+1) + "t" + (this.pointerIdx+1);
            
            $("#" + id).attr("class", "");
            $("#" + id).addClass(className);
            
        }else if(penIdx == 6 || penIdx == 7){
            // 색깔만 변경
            r = this.hexToRgb(strokeStyle)["r"];
            g = this.hexToRgb(strokeStyle)["g"];
            b = this.hexToRgb(strokeStyle)["b"];
            
            var fillR = fillStyle != "" ? this.hexToRgb(fillStyle)["r"] : "";
            var fillG = fillStyle != "" ? this.hexToRgb(fillStyle)["g"] : "";
            var fillB = fillStyle != "" ? this.hexToRgb(fillStyle)["b"] : "";
            var alphaRatio = (alpha * 0.01);
            
            if(fillStyle == ""){
                $("#" + id).css("background", "transparent");				
            }else{				
                if(figureType == "0"){
                    $("#" + id).css("background", "rgba("+fillR+", "+fillG+", "+fillB+", "+ alphaRatio+")");		
                }
            }
            $("#" + id).css("border", "5px solid rgba("+r+", "+g+", "+b+", "+ alphaRatio+")");
        }
        
        if(penIdx > 4){
            this._drawPrevCanvas(penIdx);
        }
    },
    
    _drawPrevCanvas : function(penIdx){		
        var token = (penIdx == 2 ? "h" : penIdx == 5 ? "l" : penIdx == 6 ? "s" : penIdx == 7 ? "c" : "")
        var layerId = token + "figure"; 
        var strokeStyle = penIdx == 1 ? this.strokeStyle : this[token + "StrokeStyle"];
        var lineWidth = penIdx == 1 ? this.lineWidth : this[token + "LineWidth"];
        var alpha = this[token + "alpha"];
        
        var canvas = document.getElementById(layerId);
        var context = canvas.getContext('2d');
        var drawCanvas = document.getElementById("sketch" + UI.current);
        var prevScale = ($(drawCanvas).width() / canvas.width * 0.1) + 1; 
        
        if(strokeStyle) context.strokeStyle = strokeStyle;		
        if(lineWidth) context.lineWidth = lineWidth * prevScale * 1.5;		
        if(alpha) context.globalAlpha = (alpha * 0.01);
        
        context.clearRect(0, 0, 500, 500);
        
        if(penIdx == 5){			
            context.beginPath();
            context.lineCap = "round";
            context.lineJoin = "round";
            context.moveTo(20, 80);
            context.bezierCurveTo(200, 0, 100, 150, 280, 50);
            // context.strokeStyle = 'rgba(255,20,10,1)'; // border color
            context.stroke();
            context.closePath();
            
        }else if(penIdx == 6){
            context.lineCap = "round";
            context.lineJoin = "round";
            context.strokeRect(80, 15, 140, 120);
            if(this.sFillStyle && this.sFillStyle.toLowerCase() != ""){
                context.fillStyle = this.sFillStyle;
                context.fillRect(80, 15, 140, 120);
            }
            context.closePath();
            
        }else if(penIdx == 7){
            var x1 = 80, y1 = 10, x2 = 220, y2 = 140,            
            p = 4 * ((Math.sqrt(2) -1) / 3),
            rx = (x2 - x1) / 2, ry = (y2 - y1) / 2,            
            cx = x1 + rx, cy = y1 + ry;
            
            context.lineCap = "round";
            context.lineJoin = "round";
            context.moveTo( cx, cy - ry);
   	        context.bezierCurveTo( cx + (p * rx), cy - ry,  cx + rx, cy - (p * ry), cx + rx, cy );
   	        context.bezierCurveTo( cx + rx, cy + (p * ry), cx + (p * rx), cy + ry, cx, cy + ry );
   	        context.bezierCurveTo( cx - (p * rx), cy + ry, cx - rx, cy + (p * ry), cx - rx, cy );
   	        context.bezierCurveTo( cx - rx, cy - (p * ry), cx - (p * rx), cy - ry, cx, cy - ry );  
            
   	        if(this.cFillStyle != null && this.cFillStyle != ""){
                context.fillStyle = this.cFillStyle;
                context.fill();    	        
   	        }
   	        context.stroke();
   	        context.closePath();
        }		
    },	
    //------------------------------------- end new code ----------------------------------------------------	
    
    // menu-4 (pen), 5(eraser), 6(pointer), 7(square), 8(circle), 9(line)
    _callPensetting : function(menu, isSend) {		
        isSend = (typeof(isSend) != "undefined") ? isSend : true;		
        
        var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
        var alpha = this[token + "alpha"];
        var colorIdx = this.penIdx == 1 ? this.colorIdx : this[token + "ColorIdx"];
        var lineWidth = this.penIdx == 1 ? this.lineWidth : this[token + "LineWidth"];
        var stampKind = this.pointerIdx;
        var lineCap = this.lineCap;
        var targetStyle = this.penIdx == 1 ? this.strokeStyle : this[token + "StrokeStyle"];
        
        if(this.penIdx == 3) colorIdx = -1;
        
        var r = 0;
        var g = 0;
        var b = 0;
        // 지우개는 rgb 코드 0으로 보내야 한다.
        
        if(menu == 6){
            r = colorIdx > -1 && colorIdx < 11 ? this.pointColorMap[colorIdx]["r"] : this.hexToRgb(targetStyle)["r"];
            g = colorIdx > -1 && colorIdx < 11 ? this.pointColorMap[colorIdx]["g"] : this.hexToRgb(targetStyle)["g"];
            b = colorIdx > -1 && colorIdx < 11 ? this.pointColorMap[colorIdx]["b"] : this.hexToRgb(targetStyle)["b"];
            
        }else if(menu != 5){			
            /**
             r = colorIdx > -1 && colorIdx < 10 ? ((menu == 6) ? this.pointColorMap[colorIdx]["r"] : this.colorMap[colorIdx]["r"]) : this.hexToRgb(targetStyle)["r"];
             g = colorIdx > -1 && colorIdx < 10 ? ((menu == 6) ? this.pointColorMap[colorIdx]["g"] : this.colorMap[colorIdx]["g"]) : this.hexToRgb(targetStyle)["g"];
             b = colorIdx > -1 && colorIdx < 10 ? ((menu == 6) ? this.pointColorMap[colorIdx]["b"] : this.colorMap[colorIdx]["b"]) : this.hexToRgb(targetStyle)["b"];
             **/			
            r = colorIdx > -1 && colorIdx < 9 ? this.colorMap[colorIdx]["r"] : this.hexToRgb(targetStyle)["r"];
            g = colorIdx > -1 && colorIdx < 9 ? this.colorMap[colorIdx]["g"] : this.hexToRgb(targetStyle)["g"];
            b = colorIdx > -1 && colorIdx < 9 ? this.colorMap[colorIdx]["b"] : this.hexToRgb(targetStyle)["b"];
        } 
        
        var fillRGB = [];
        if(menu == 4 || menu == 7){
            //
        }else if(menu == 8 || menu == 9){
            var fillStyle = menu == 8 ? this.sFillStyle : this.cFillStyle;
            var fillR = fillStyle != "" ? this.hexToRgb(fillStyle)["r"] : "";
            var fillG = fillStyle != "" ? this.hexToRgb(fillStyle)["g"] : ""; 
            var fillB = fillStyle != "" ? this.hexToRgb(fillStyle)["b"] : "";
            fillRGB.push(fillR);
            fillRGB.push(fillG);
            fillRGB.push(fillB);
        }
        
        PacketMgr.Master.pensetting(isSend, menu, lineWidth, r, g, b, alpha, stampKind, lineCap, fillRGB);
    },
    
    _callPointer : function(isSend){
        isSend = (typeof(isSend) != "undefined") ? isSend : true;
        
        var colorIdx = (this.penIdx == 1) ? this.colorIdx : (this.penIdx == 2) ? this.hColorIdx : (this.penIdx == 4) ? this.pColorIdx : 0;
        
        var r = this.pointColorMap[colorIdx]["r"];
        var g = this.pointColorMap[colorIdx]["g"];
        var b = this.pointColorMap[colorIdx]["b"];
        
        var type = this.pointerIdx;
        
        // 펜세팅 먼저
        this._callPensetting("6");
        
        // 그다음 레이저 포인터 등록 
        PacketMgr.Master.laserpointer(type, r, g, b, isSend);
    },
    
    // 도형 
    _callFigure : function(mode){
        if(this.penIdx != 1 && this.penIdx != 2){
            Ctrl.Msg.show(_msg("select.pen"));
            return;
        }
        // this._callPensetting("7", true, mode);
        
        var menuSelect = mode == "line" ? 7 : mode == "square" ? 8 : mode == "circle" ? 9 : 4;
        this._callPensetting(menuSelect, true);		
    },
    
    _usePlugin : function(flag) {		
        if(flag){			
            $('#penSizeSlider').slider("enable");
            
            $('#opacitySlider').slider("enable");
            
            $("#epenSizeSlider").slider("enable");
            
            $('#lpenSizeSlider').slider("enable");
            
            $('#lopacitySlider').slider("enable");
            
            $('#spenSizeSlider').slider("enable");
            
            $('#sopacitySlider').slider("enable");
            
            $('#cpenSizeSlider').slider("enable");
            
            $('#copacitySlider').slider("enable");	
            
        }else{
            $('#penSizeSlider').slider("disable");
            
            $('#opacitySlider').slider("disable");
            
            $("#epenSizeSlider").slider("disable");
            
            $('#lpenSizeSlider').slider("disable");
            
            $('#lopacitySlider').slider("disable");
            
            $('#spenSizeSlider').slider("disable");
            
            $('#sopacitySlider').slider("disable");
            
            $('#cpenSizeSlider').slider("disable");
            
            $('#copacitySlider').slider("disable"); 
        }
    },
    
    _setPointer : function(flag){
        var cursor = (flag == "1") ? "hand"  : (flag == "2") ? "text" : (this.penIdx == 1) ? "hpen" : (this.penIdx == 2) ? "hpen" : (this.penIdx == 3) ? "del" : (this.penIdx == 4) ? "pointer" : 
        (this.penIdx == 5 || this.penIdx == 6 || this.penIdx == 7) ? "diagram" : "";
        
        this.Cursor.change(cursor);
    },
	   
    changeCursor : function(flag){		
        var cursor = (flag == "1") ? "hand" : (flag == "2") ? "text" : (this.penIdx == 1) ? "hpen" : (this.penIdx == 2) ? "hpen" : (this.penIdx == 3) ? "del" : (this.penIdx == 4) ? "pointer" : 
        (this.penIdx == 5 || this.penIdx == 6 || this.penIdx == 7) ? "diagram" : "";
        
        this.Cursor.change(cursor);
    },
    
    toggleHand : function(isCheck){
        if(isCheck && !Ctrl._checkAuth(true)) return;
        
        Ctrl.BGImg.auth();
        
        PDFViewer.auth();
        
        PacketMgr.Master.changeMode("view");		
    },
    
    toggleZoomOpt : function(flag){
        if(flag){
            $("#zoomOpt").hide();
        }else{
            $("#zoomOpt").toggle();	
        }
    },
    
    rgbToHex : function(R, G, B) {
        if(R < 0 && G < 0 && B < 0) return "";
        
        this.toHex(R) + this.toHex(G) + this.toHex(B)
        return this.toHex(R) + this.toHex(G) + this.toHex(B);
    },
    
    toHex : function(n) {
        n = parseInt(n, 10);
        
        if (isNaN(n)) return "00";
        n = Math.max(0, Math.min(n, 255));
        return "0123456789ABCDEF".charAt((n - n % 16) / 16)+ "0123456789ABCDEF".charAt(n % 16);
    },
    
    hexToRgb : function(hex) {
        // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
        
        var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
        hex = hex.replace(shorthandRegex, function(m, r, g, b) {
            return r + r + g + g + b + b;
        });
        
        var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
        } : null;
    },
    
    zoom : function(val){
        $("#zoomval").val(val);
        
        var orgVal = parseInt($("#zoomval").val(), 10);		
        PacketMgr.Master.zoom(orgVal, "1");
    },
    
    // zoom +버튼 눌렀을때 
    zoomIn : function(settled){
        // 배율 10프로씩 증가.	
        var orgVal = parseInt($("#zoomval").val(), 10);
        var randomRate = parseInt(10 + (Math.floor(Math.random() * 10)/2));
        orgVal += randomRate;
        
        if(orgVal > 500) orgVal = 500;
        
        Ctrl.setZoomVal(orgVal);
        
        PacketMgr.Master.zoom(orgVal, settled);
    },
    
    // zoom -버튼 눌렀을때
    zoomOut : function(settled){
        // 배율 10프로씩 감소.
        var randomRate = parseInt(10 + (Math.floor(Math.random() * 10)/2));
        var orgVal = parseInt($("#zoomval").val(), 10);
        orgVal -= randomRate;
        
        if(orgVal < 100) orgVal = 100;
        
        Ctrl.setZoomVal(orgVal);
        
        PacketMgr.Master.zoom(orgVal, settled);
    },
    
    setZoomScale : function(scale){
        var val = Math.floor(scale * 100);
        this.setZoomVal(val);
    },
    
    setZoomVal : function(val){
        $("#zoomval").val(val);		
    },
    
    exit : function(flag){
        PacketMgr.Master.exit(flag);		
        
        Utils.Local.remove("guest");
        
        if(flag){			
            // 방삭제되는 딜레이시간이 있기 때문에 1초후에 나가게 한다.
            setTimeout(function(){
                location.href = Utils.addContext("main");
            }, "1000");
            
        }else{
            location.href = Utils.addContext("main");	
        }		
    },	
    
    invite : function(){
        var email = $("#invite_email").val();
        if(email.trim() == ""){
            Ctrl.Msg.show(_msg("insert.email"));
            $("#invite_email").focus();			
            return;
        }
        
        if(!email.isEmail() ){
            Ctrl.Msg.show(_msg("invalid.email"));
            $("#invite_email").focus();			
            return;
        }
        
        
        var url = Utils.addContext(_url("invite"));
        var params = {
            roomid : RoomSvr.roomid,
            email : (email) ? email : ""
        };
        
        Utils.request(url, "json", params, function(data){
            if(data.result == "0"){
                // Ctrl.Modal.hide("inviteModal");
                Ctrl.Modal.hideInvite();
            }else{
                Ctrl.Msg.show(_msg("invite.fail") + " code : " + data.result);
            }
        });
    },
    destroy : function() {
        try{
            // destroy ctrl service
            Ctrl.Comment.destroy();
            
            Ctrl.Memo.destroy();
            
            Ctrl.Background.destroy();
            
            Ctrl.BGImg.destroy();
            
            Ctrl.Text.destroy();
            
            Ctrl.Room.destroy();
            
            Ctrl.Modal.destroy();
            
            // destroy _setRemoteControl 
            $('ul.circle_menu').circleMenu("destroy");
            $("#file2").unbind("change");
            $("#text_btn2").unbind("click");
            
            // destroy _setMenuToggle
            $("#save_btn").unbind("click");
            $("#attend_btn").unbind("click");
            $("#setup_btn").unbind("click");
            $("#bg_btn").unbind("click");
            $(".btn_chat", "#quick_wrapper").unbind("click");
            $(".btn_comment", "#quick_wrapper").unbind("click");
            $(".btn_memo", "#quick_wrapper").unbind("click");
            $(".btn_text", "#quick_wrapper").unbind("click");
            $(".btn_background_del", "#quick_wrapper").unbind("click");
            $("#file1").unbind("change");
            $("#exit").unbind("click");
            $("#room_title").unbind("click");
            $("#invite_btn").unbind("click");
            $(".btn_capture", "#quick_wrapper").unbind("click");
            
            $("ul.bottom_cmenu", "#quick_wrapper").circleMenu("destroy");
            
            // destroy  _setDocEvent
            $("#handCtl").unbind("click");
            $("#undoCtl").unbind("click");
            $("#redoCtl").unbind("click");
            
            $("#zoomOptBtn").unbind("click");
            $("a.zoomPct", "#zoomOpt").unbind("click");
            $("#zoomin").unbind("mousedown");
            $("#zoomin").unbind("mouseout");
            $("#zoomin").unbind("mouseup");
            $("#zoomout").unbind("mousedown");
            $("#zoomout").unbind("mouseout");
            $("#zoomout").unbind("mouseup");
            
            // destroy _setPenEvent
            $("#pen_picker").unbind("click");					
            $("#lpen_picker").unbind("click");
            $("#spen_picker").unbind("click");
            $("#spen_picker2").unbind("click");
            $("#cpen_picker").unbind("click");
            $("#cpen_picker2").unbind("click");
            
            $("a.color_select2", "#pen_color_wrap").unbind("click");
            $("a.color_select2", "#lpen_color_wrap").unbind("click");
            $("a.color_select2", "#spen_color_wrap").unbind("click");
            $("a.color_select2", "#spen_color_wrap2").unbind("click");
            $("a.color_delete", "#spen_color_wrap").unbind("click");
            $("a.color_select2", "#cpen_color_wrap").unbind("click");
            $("a.color_select2", "#cpen_color_wrap2").unbind("click");
            $("a.color_delete", "#cpen_color_wrap").unbind("click");
            $("a.color_select2", "#hbar_4").unbind("click");
            $("#pen_preview_5", "#hbar_5").unbind("click");
            $("#pen_preview_6", "#hbar_6").unbind("click");
            $("#pen_preview_7", "#hbar_7").unbind("click");
            $(".miniColor_jqueryP", "#hmenubar").minicolors("destroy");
            $("#clear_btn").unbind("click");
            
            // destroy _setSlider
            $('#penSizeSlider').slider("destroy");						
            $('#opacitySlider').slider("destroy");
            $("#epenSizeSlider").slider("destroy");
            $('#lpenSizeSlider').slider("destroy");
            $('#lopacitySlider').slider("destroy");
            $('#spenSizeSlider').slider("destroy");
            $('#sopacitySlider').slider("destroy");
            $('#cpenSizeSlider').slider("destroy");
            $('#copacitySlider').slider("destroy");
            
            // destroy _setChatEvent
            $(".chat_close", "#chat_wrapper").unbind("click");
            $("#sendchat").unbind("click");
            $("#chatmsg").unbind("keyup"); 
            
            /**
             $("#user_wrapper").slideToggle("destroy");
             $("#setup_box").slideToggle("destroy");
             $("#bg_box").slideToggle("destroy");
             $("#chat_wrapper").slideToggle("destroy");
             **/
        }catch(e){
            // error exception
            console.log("Ctrl.destroy exception : " + e);
        } 
        
    },
    
    Mail : {
        sendList : [],
        openModal : function() {
            // Ctrl.Modal.hide('inviteModal');
            Ctrl.Modal.hideInvite();
            
            $(".poll_box").hide();
            var container = document.getElementById("invite_mail_modal");
            
            // gstrFullRootUrl + String.format("%s/room/%s/%s", thumbBasePath, roomId.substring(0, 3), roomId);
            
            var roomId = RoomSvr.roomid;
            var roomTitle = RoomSvr.roomtitle;
            var roomUrl = location.href;
            var roomThumbnail = RoomSvr.roomthumbnail + "_01.jpg";
            
            if(container) {
                $(container).show();
            } else {
                
                var htmlStr  = "<div id=\"invite_mail_modal\" class=\"mail_box\">\
                <div class=\"mail_header\">\
                <span class=\"mail_tit\">"+roomTitle+"</span>\
                <a onclick=\"Ctrl.Mail.closeModal();\" title=\"close\"></a>\
                </div>\
                <div class=\"mail_body\">\
                <img src=\""+roomThumbnail+"\" alt=\"\" class=\"thumbnail\" onerror=\"this.src='"+Utils.addContext('res/fb/images/invite_default_thumb.png')+"'\" >\
                <div class=\"roominfo\">\
                <p class=\"tit\">WENOTE</p>\
                <p class=\"room_ad\">"+roomUrl+"</p>\
                <textarea id=\"inviteComment\" placeholder=\""+_msg("invite.comment.guide")+"\" class=\"comment\"></textarea>\
                </div>\
                </div>\
                <div class=\"UserWrap\">\
                <div class=\"input_email\">\
                <input type=\"text\" id=\"emailInput\" placeholder=\""+_msg("invite.mail.guide")+"\" />\
                <a href=\"\" class=\"btn_add\" id=\"inviteAddBtn\">"+_msg("invite.btn.add")+"</a>\
                </div>\
                <div id=\"inviteUserList\" class=\"UserList\"></div>\
                </div>\
                <div class=\"mail_btn\">\
                <a onclick=\"Ctrl.Mail.closeModal();\" class=\"btn_cancel\">"+_msg("invite.btn.cancel")+"</a>\
                <a onclick=\"Ctrl.Mail.sendInviteMail();\" class=\"btn_send\">"+_msg("invite.btn.send")+"</a>\
                </div>\
                </div>\
                </div>";
                $("body").append(htmlStr);
            }
            
            $("#emailInput").focus();			 
            
            
            var onAddCallback = function(e){
                e.preventDefault();
                var email = $("#emailInput").val();
                if(email == ""){
                    Ctrl.Msg.show(_msg("mail.address.not.input"));
                    return false;
                }
                if(!email.isEmail()){
                    Ctrl.Msg.show(_msg("mail.address.format.invalid"));
                    return false;
                } 
                
                var len = Ctrl.Mail.sendList.length;
                if(len > 0) {
                    for(var i=0; i<Ctrl.Mail.sendList.length; i++) {
                        if(Ctrl.Mail.sendList[i] == email){
                            //Ctrl.Msg.show(_msg("mail.address.duplicate"));
                            return false;
                        }
                    }
                } 
                
                var url = Utils.addContext(_url("mail.check"));
                var params = { 'email' : email };
                Utils.request(url, "json", params, function(data) {
                    var json = data;
                    var result = json.result;
                    
                    if(result == 0) {
                        Ctrl.Mail.sendList.push(email);
                        var htmlStr = "<p>"+email+"<a href=\"#\" class=\"delUser\" title=\"remove\"></a></p>";
                        $("#inviteUserList").append(htmlStr);
                        
                        $("#emailInput").val("");
                        
                        $(".delUser").bind("click", function(e){
                            e.preventDefault();
                            var selectEmail = $(this).parent().text();
                            $(this).unbind("click");
                            $(this).parent().remove();
                            for(var i=0; i<Ctrl.Mail.sendList.length; i++) {
                                if(Ctrl.Mail.sendList[i] == email){
                                    Ctrl.Mail.sendList.splice(i, 1);
                                }
                            }
                        });
                    } else {
                        Ctrl.Msg.show(_msg("mail.address.invalid"));
                    }
                });
            }
            
            
            // 엔터 처리
            $("#emailInput").bind("keydown", function(e){
                if(e.keyCode == 13){
                    onAddCallback(e);
                }
            });
            
            // ADD 버튼 클릭
            $("#inviteAddBtn").bind("click", onAddCallback);
        },	
        
        closeModal : function() {
            Ctrl.Mail.sendList = [];
            
            $("#invite_mail_modal").hide();
            $("#inviteAddBtn").unbind("click");
            $(".delUser").unbind("click");
            
            $("#inviteComment").val("");
            $("#emailInput").val("");
            $("#inviteUserList").empty();
        },
        
        sendInviteMail : function() {
            var emailList = $("p", "#inviteUserList").map(function(){return $(this).text();}).get().join(",");
            
            if(emailList == "") {
                Ctrl.Msg.show(_msg("invite.alert.add"));
                return false;
            }
            var url = Utils.addContext(_url("mail.send"));
            var params = {
                'sendernm' : RoomSvr.usernm,
                'receiver' : emailList,
                'title'    : RoomSvr.roomtitle,
                'url'      : location.href,
                'roomid'   : RoomSvr.roomid,
                'comment'  : $("#inviteComment").val()
            };
            Utils.request(url, "json", params, function(data) {
                var json = data;
                var result = json.result;
                
                if(result == 0) {
                    Ctrl.Msg.show(_msg("invite.success"));
                    Ctrl.Mail.closeModal();
                } else {
                    Ctrl.Msg.show(_msg("invite.fail"));
                }
            });
        }
    }
};

/**
 * @title : Realtime Conference Interface
 * @date : 2013.11.26
 * @author : kim dong hyuck
 * @description : socket.io를 이용한 실시간 Realtime Conference Interface
 */
var RoomSvr = {
    // host : "https://wroom.wenote.com:443",
    host : "",
    socket : null,
    userid : "",
    usernm : "",
    userno : "",
    roomid : "",
    clientip : "",
    deviceid : "",
    init : function(options) {		
        this.host = options.host || '';
        this.userid = options.userid || '';
        this.usernm = options.usernm || '';
        this.userno = options.userno || '';
        this.roomid = options.roomid || '';
        this.roomtitle = options.roomtitle || '';
        this.deviceid = options.deviceid || '';
        this.thumbnail = options.thumbnail || '';
        this.clientip = options.clientip || '';
        
        this.roomthumbnail = options.roomthumbnail || '';
        
        if(this.userid == this.userno){
            this.thumbnail = Utils.addContext(_url("profile.default"));
        }
        
        if(this.host == ''){
            // Ctrl.Msg.show("룸서버 정보를 찾을수 없습니다.");
            Ctrl.Msg.show(_msg("cannot.find.room"));
            return;
        }
        
        var isSecure = this.host.indexOf("https") > -1 ? true : false;
        this.socket = io.connect(this.host, {secure : isSecure});
        
        this.socket.on('connect', this.connect);
        this.socket.on('sendmsg', this.sendmsg);
        this.socket.on('newuser', this.newuser);
        this.socket.on('leaveuser', this.leaveuser);
        this.socket.on('roomlist', this.roomlist);
        this.socket.on('roomlistEx', this.roomlistEx);
        this.socket.on('userlist', this.userlist);
        this.socket.on('roomuserlist', this.roomuserlist);		
        this.socket.on("disconnect", this.disconnect);		
        this.socket.on("newvc", this.newvc);
        this.socket.on("streamvideo", this.streamvideo);		
        this.socket.on("kickuser", this.kickuser);				
    },
    connect : function() {
        Utils.log("connect add user : " + RoomSvr.userid);
        
        // cmd : adduser -> userid, roomid, roomname, username, userno, deviceid, svctype(0-우리,1-타기관)
        // svctype 0-우리회의, 1-타기관회의		
        this.emit("adduser", '' + RoomSvr.userid + '','' + RoomSvr.roomid + '', ''+RoomSvr.roomtitle+'', '' + RoomSvr.usernm + '', '' + RoomSvr.userno + '', ''+ RoomSvr.deviceid +'', ''+ RoomSvr.thumbnail +'', ''+ RoomSvr.clientip +'');
        
        // this.emit("sendmsg", '{"cmd":"enterroom", "userid":"' + RoomSvr.userid+ '", "roomid":"' + RoomSvr.roomid + '","username":"'+ RoomSvr.usernm + '","userno":"' + RoomSvr.userno + '"}');		
    },	
    
    disconnect : function(){
        // Utils.log("disconnect enter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // Ctrl.Member.setOnline("disconnect", false, true);		
        Ctrl.setMyNetworkStatus(false, true);
    },
    
    send : function(data) {
        // Utils.log("data : " + JSON.stringify(data));
        if(typeof(data) != "undefined" && data != null && data != ""){
            // 룸서버로 보낼때는 data는 json string 형태로 보내야 한다.			
            this.socket.emit("sendmsg", data);			
        }
    },
    sendmsg : function(userid, data) {		
        // 서버에서 내려오는 패킷 draw
        PacketMgr.receive(userid, data);
    },
    kickuser : function(roomid, userid, username){
        // console.log("kickuser roomid : " + roomid + ", userid : " + userid + ", username : " + username);
        // console.log("roomid : " + roomid + ", PacketMgr.userid : " + PacketMgr.userid + ", userId : " + userid);
        
        if(RoomSvr.roomid == roomid && PacketMgr.userid == userid){
            alert(_msg("msg.connect.another.device"));
            location.href = Utils.addContext("main");
        }
    },
    // userid, username, userno, isGuest, thumbnail
    newuser : function(userid, username, userno, guest, thumbnail) {
        // Utils.log("enter newuser! userid : " + userid + ", username : " + username + ", userno : " + userno + ", guest : " + guest);
        // 내정보만 getdata로 보내야 한다.
        if(RoomSvr.userid == userid){
            this.emit("sendmsg", '{"cmd":"getdata","userid":"' + RoomSvr.userid+ '","usernm":"' + RoomSvr.usernm + '"}');
        }
        
        // 마스터가 접속한 경우 마지막 파일하고 페이지 broadcast 한다.
        Ctrl.Member.newUser(userid, username, userno, guest, thumbnail);
        // WebRTC 부분..
    },
    leaveuser : function(userId, userName, userNo) {
        Ctrl.Member.leaveUser(userId, userName, userNo);
        
        // layer 삭제 필요함.
        // if(!Utils.browser("msie")) VideoCtrl.destroy(userNo);
        VideoCtrl.destroy(userNo);
    },
    
    // web rtc initialize connection pair
    newvc : function(roomId, user1, user2, data){
        Utils.log("newvc roomid : " + roomId + ", user1 : " + user1 + ", user2 : " + user2 + ", data : " + JSON.stringify(data));
        
        var param = {
            offer : user2,
            answer : user1,
            offerTurn : data.id2,
            answerTurn : data.id1
        };
        
        /***
         * @description 신규로 들어오는 사람은 무조건 Answer이고, 기존에 영상으로 접속한 사람이 offer를 보내준다. 
         */
        var mode = RoomSvr.userno == user1 ? "answer" : RoomSvr.userno == user2 ? "offer" : "";
        if(mode != ""){
            VideoCtrl.initPeerMedia(mode, RoomSvr.roomid, param);
        }
        
    },
    
    voice : function(on, username) {
        Utils.log("enter voice!");
    },
    
    leave : function(username, userid, userno) {
        Utils.log("enter leave!");
    },
    
    roomlist : function(rooms, current_room) {
        Utils.log("enter roomlist!");
    },
    roomlistEx : function(rooms, current_room) {
        Utils.log("enter roomlistEx!");
    },
    userlist : function(users, current_room) {
        Utils.log("enter userlist!");
    },
    roomuserlist : function(users, current_room) {
        Utils.log("enter roomuserlist!");
    },
    switchRoom : function(room) {
        Utils.log("enter switchRoom.");
        
        currentRoom = room;
        this.socket.emit('switchRoom', room);
        
        $('#conversation').append('<b>' + room + '<br>').focus();
    },
    // WebRTC 비디오 추가 - Written by Minsu (2015.04.14)
    streamvideo : function(data){
        
        if(Utils.browser("msie")) return;
        
        VideoCtrl.receiveSignal(data);
    },	
    // WebRTC 관련 패킷 전송을 위한 function
    sendWebRtcData : function(data) {  
        this.socket.emit('streamvideo', data);
    },
    destroy : function() {
        // destroy
        if(this.socket) this.socket.disconnect();
    }
};

var CanvasApp = {
    info : null,
    init : function(){
        var deviceId = Utils.getDevice();
        // layout은 video/doc 모드중 어떤 화면을 보고 있는지에 대한 값으로, changeMasterㅇ ㅘ changeWidhtDraw에서 무조건 날려줘야 한다.
        var codeStr = location.pathname.split("/").pop();
        CanvasApp.getRoomInfo(codeStr);		
    },
    initSvc : function(isGuest, guestName){
        if(isGuest){			
            var guestName = guestName != "" ? guestName : $("#guest_nm").val();
            if(guestName.trim() == "") {
                Ctrl.Msg.show(_msg("insert.guest.name"));
                return;
            }
            
            var info = this.info;
            this.info.userid = this.info.deviceid;
            this.info.userno = this.info.deviceid;
            this.info.usernm = guestName + "("+_msg("guest") +")";
            
            Utils.Local.set("guest", guestName);
            
            Ctrl.Modal.hide("loginModal");
            
            // if(!Utils.mobile()) Avgrund.hide("#loginModal");
            // Ctrl.avgrund(false, "loginModal");			
        }
        
        var initializeServer = function(){			
            var info = CanvasApp.info;
            
            // room server control
            RoomSvr.init(info);		
            
            // packet control
            PacketMgr.init(info);
            
            // memo는 class base라 권한여부를 다시 체크해줘야ㅕ 한다.
            Ctrl.Memo.auth();
            
            // WebRTC initialize..
            //if(!Utils.browser("msie"))
            // VideoCtrl.init(info.userid, info.usernm, info.userno);   // WebRTC 생성
            var len = CanvasApp.info.user != null ? CanvasApp.info.user.length : 0;
            
            var currentUserInfo = Ctrl.Member.getUserOnline(info.userno, "userno");			
            VideoCtrl.initMyMedia(CanvasApp.info.roomid, currentUserInfo);			
        }
        
        //var passwordFlag = $("#passwdflag").val();
        var passwordFlag = CanvasApp.info.passwdflag;
        if(passwordFlag == '1') { // 비밀방임..
            Ctrl.Modal.password(initializeServer);
        }else{			
            initializeServer();
        }
        
    },
    
    importScript : function(callback){
        
        var language = $("#lang").val();	
        var lang = (language != "ko" && language != "ko_kr" && language != "ko-kr") ? "en" : "ko";
        var msgLink = Utils.addResPath("fb/js", "msg/msg_"+lang+".js");
        $.getScript(msgLink, function(){			
            callback();
        });		
    },
    
    submitPassword : function(callback) {
        var passwdStr = $("#room_password").val();
        var currDate = new Date().getTime();  // 현재시간 밀리세컨드
        
        if(passwdStr.trim() == ""){
            Ctrl.Msg.show(_msg("m.password.insert"));
            return;
        }
        
        var rsaModule   = $("#rsaModule").val();
        var rsaExponent = $("#rsaExponent").val();
        
        var rsa = new RSAKey();
        rsa.setPublic(rsaModule, rsaExponent);
        var tokenStr = rsa.encrypt(CanvasApp.info.roomid + "," + passwdStr + ',' + currDate);
        
        var url = Utils.addContext(_url("check.passwd"));
        var param = {
            token : tokenStr
        }
        
        Utils.request(url, "json", param, function(data){
            Utils.log("success.." + data);
            if (data.result == '0') {
                Ctrl.Modal.hide("passwdModal");	
                callback();
            } else {
                Ctrl.Msg.show(_msg("m.password.incorrect"));					
            }
        });
    },
    
    // 룸서버 보안 관련 추가 - 2015.06.19 (author : Min su)
    getRoomInfo : function(codeStr) {
        
        var module = $("#rsaModule").val();
        var exponent = $("#rsaExponent").val();
        var uuid = Utils.createUUID().substring(0, 5);
        
        var rsa = new RSAKey();
        rsa.setPublic(module, exponent);
        var tokenStr = rsa.encrypt(codeStr + ',' + uuid);
        
        if(tokenStr == null) {
            alert("encrypt fail..");
            return;
        }
        
        var param = {
            token : tokenStr
        };
        
        var info = null;
        var url = Utils.addContext(_url("canvas.get"));
        Utils.request(url, "json", param, function(data){
            Utils.log("success..");
            Utils.log(data);
            if(data.result == '0') {
                var info = data;
                info.deviceid = Utils.getDevice();
                /**
                 var info = {
                 userid : data.userid,
                 creatorid : data.creatorid,
                 masterid : data.masterid,
                 userno : data.userno,
                 usernm : data.usernm,
                 roomid : data.roomid,
                 roomtitle : data.roomtitle,
                 //groupno : '',
                 host : data.host,
                 plugin : JSON.stringify(data.plugin),
                 deviceid : deviceId,
                 thumbnail : data.thumbnail,
                 bg : JSON.stringify(data.bg),
                 code : data.code,
                 email : data.email,
                 auth : JSON.stringify(data.auth),
                 user : JSON.stringify(data.user),
                 passwdflag : data.passwdflag					
                 };
                 **/
                
                CanvasApp.info = info;
                
                Ctrl.Loader.show();
                
                // history drawing
                PacketMgr.loadHistory(info.roomid, 0, 1, 1, function(){			
                    // 히스토리 드로잉
                    
                    var isAnimation = Utils.browser("chrome") ? true : false;
                    isAnimation = false;
                    
                    PacketMgr._drawHistoryPacket(PacketMgr.lastFileNo, PacketMgr.lastPageNo, isAnimation, function(){
                        // 마지막 보고있는 페이지의 zoom 확인 (마스터가 들어온 경우만 사용)
                        PacketMgr._drawLastZoomPacket(PacketMgr.lastFileNo, PacketMgr.lastPageNo);					
                        // 팬세팅 맞춤
                        if(!PacketMgr.isMC){
                            // 마스터가 아닌경우 펜세팅 싱크 맞춰줘야 한다.
                            // PacketMgr.Master.syncPensettingForce( PacketMgr._getLastPen() );					
                            PacketMgr.pushQueue(PacketMgr._getLastPen());
                        }
                        
                        // 마스터 기본 세팅 
                        PacketMgr.Master.changeMode("view");
                        
                        // queue shift
                        PacketMgr.shiftQueue();
                        
                        if(!isAnimation) {
                            Ctrl.Loader.hide();
                        }		
                    });			
                });
                
                // canvas control
                UI.init();
                
                // draw tool control
                Ctrl.init();
                
                if(info.userid == "" && info.userno == ""){
                    var guestName = Utils.Local.get("guest") || "";
                    if(guestName != ""){
                        CanvasApp.initSvc(true, guestName);			
                    }else{
                        Ctrl.Modal.login();				
                    }
                }else{					
                    CanvasApp.initSvc(false);	
                }
                
                if(typeof(Notify) != "undefined") {					
                    Notify.init("canvas", info.snstype, info.userid, info.userno, info.usernm);				
                }
            }
        });		
    },   
    
    drawNt : function(action, x, y, density, webViewWidth, webViewHeight){
        var id = "sketch" + UI.current;        
        var canvasWidth = document.getElementById(id).clientWidth;
        var canvasHeight = document.getElementById(id).clientHeight;
        var canvasX = x / density;
        var canvasY = y / density;
        
        UI.skboards[UI.current - 1]["asyncNt"](action, canvasX, canvasY);    	
    },
    
    pinchZoomNt : function(scaleFactor){
        /**
         var percent = Math.floor(scaleFactor * 100);
         if(percent < 100) percent = 100;    	
         if(percent > 500) percent = 500;    	 
         Ctrl.setZoomVal(percent);
         PacketMgr.Master.zoom(percent, "1");
         **/
    },   
    setConfig : function(){
        // 플랫폼 별로 구분이 필요하면 이곳에 데이터를 설정한다.
        var useCanvasEvent = true;
        Utils.setCordova(useCanvasEvent);
    },
    resize : function(){
        UI.skboards[UI.current - 1]["resize"]();
    }
};

$(document).ready(function() {	
    CanvasApp.importScript(CanvasApp.init);
});