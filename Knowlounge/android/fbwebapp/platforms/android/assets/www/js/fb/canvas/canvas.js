
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
	// skboards : [], 	// canvas
	boards : null,		// canvas { pageNo : boards }
	scale : 1, 		// 현재 zoom scale
	current : 1,
	pageCnt : 1,
	file : 0,
	key : 48,
	rendering : false,
	isShift : false,  
	bfUnload : false,
	unloadIgnore : false,
	cutBottom : false,
	resolutionTryCnt : 0,
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
					var context = Utils.isKLounge() ? "klounge" : "fb";
					img.src = Utils.addResPath("images", "pointer_type0"+ j + token + i + ".png");
					
					this.images.push(img);
				};
			}
		}
	},
	
	Page : {
		limit : 3,
		beforeScale : 1,
		beforeZoomInfo : [0,0],		
		changing : false,
		auth : function(){
			// 권한에 따른 이벤트 처리 
			if(PacketMgr.isMC){
				this.setSortable();
			}else{
				if($("#sortable_box").hasClass("ui-sortable")){
					$("#sortable_box").sortable("destroy");
				}
			}			
		},
		
		fold : function(beforePage){
			$("#pageContainer").hide();
		},
		
		setSortable : function(){
			$("#sortable_box").sortable({
				opacity : 0.5,
				revert : true,
				start : function(e){					
				},
				drag : function(e, ui){
					ui.position.left = 0;
				},
				stop : function(e, ui){
					UI.Page.order();
				}
			});
		},
		
		sync : function(){
			if(UI.Page.changing){
				CanvasApp.hideHistoryLoading();
				if(PacketMgr.isMC){
					PacketMgr.Master.syncStatus("page");
				}				
				changing = false;				
			}	
		},
		set : function(pageId){			
			var board = UI.getBoard(pageId);
			if(board == null){				
				UI.setBoard(pageId);
			}

			UI.current = pageId;
			PacketMgr.lastPageId = pageId;
						
			// 2. history destroy & load
			PacketMgr.loadHistory(PacketMgr.roomid, pageId, function(packetCnt){
				// history draw				
				if(Utils.isKLounge() && packetCnt > PacketMgr.skipCnt){
					UI.Page.changing = true;					 
					CanvasApp.confirmDrawPacket("page");
					
				}else{ 
					var isAnimation = CanvasApp.isAnimation;				
					if(isAnimation){
						var maxWidth = parseInt($("#progTxt").width()) - 20;				
						Ctrl.ProgressLoader.show(maxWidth);
						// Ctrl.Loader.show();
					}else{
						Ctrl.Loader.show();
					}
					
					Utils.runCallback(function(){
						PacketMgr._drawHistoryPacket(pageId, CanvasApp.isAnimation, function(){
							CanvasApp.drawEnd(false);
							$("#loadHistory").hide();
							
							if(PacketMgr.isMC){
								PacketMgr.Master.syncStatus("page");
							}
							
							// 페이지 이동시 썸네일 뜨는 기능 안되게 수정 thumbnail 동기화
							// Ctrl.Uploader.checkSaveTimer("background", "3000");
						});
					}, 100);
				}
			});			
			
			// get plugin packet
			// 1. background set
			// 2. img, pdf set
			// 3. memo, vshare set
			this.load(pageId, function(data){				
				var background = data.background;
				var files = data.files;
				var memo = data.memo;
				var vShare = data.vshare;
				 
				if(background){
					var r = background.red;
					var g = background.green;
					var b = background.blue;
					var bgImg = background.plugindata; 
					
					// 1. background set
					var backgroundPacket = {"cmd":"background","color_r":""+r+"","color_g":""+g+"","color_b":""+b+"","bgimg":bgImg,"page":pageId};
					Ctrl.Background.receive(backgroundPacket);	
				}
				
				// 2. img & pdf set
				var len = files == null ? 0 : files.length;
				for(var i=0; i<len; i++){
					var cmd = files[i].typeflag == "p" ? "pdf" : "img";  
					var cmdAddFile = {cmd:cmd};
					cmdAddFile.seqno = files[i].seqno;
					cmdAddFile.posx = files[i].posx;
					cmdAddFile.posy = files[i].posy;
					cmdAddFile.scalew = files[i].scalew;
					cmdAddFile.scaleh = files[i].scaleh;
					cmdAddFile.url = files[i].url;
					cmdAddFile.ord = files[i].ord;
					cmdAddFile.typeflag = files[i].typeflag;
					cmdAddFile.thumbnail = files[i].thumbnail;
					cmdAddFile.usernm = files[i].usernm;
					cmdAddFile.fname = files[i].fname;
					cmdAddFile.docpageno = files[i].docpageno;
					cmdAddFile.degree = files[i].degree;

					if(files[i].typeflag == "p"){
						PDFViewer.draw(cmdAddFile);
					}else{
						Ctrl.BGImg.draw(cmdAddFile, false);	
					}
				}
				
				// 3. memo packet
				if(memo != null){					
					var len = memo == null ? 0 : memo.length;
					for(var i=0; i<len; i++){
						var memoInfo = memo[i];
						var type = "0";
						var memono = memoInfo.commentno || '';
						var seqno = memoInfo.seqno || '';
						var userid = PacketMgr.userid || '';
						var userno = PacketMgr.userno || '';
						var usernm = PacketMgr.usernm || '';
						var datetime = memoInfo.cdatetime || '';
						var title = memoInfo.title || '';
						var content = memoInfo.content || '';
						var left = memoInfo.posx || 0;
						var top = memoInfo.posy || 0;
						var fold = memoInfo.plugindata || 1;
						var ord = memoInfo.ord || 1;
						var r = memoInfo.red;
						var g = memoInfo.green;
						var b = memoInfo.blue;
						
						var packet = {"cmd":"memo","type":type,"memono":""+memono+"","seqno":""+seqno+"", "userid":""+userid+"","userno":""+userno+"","usernm":""+usernm+"","datetime":""+datetime+"","title":""+title+"","content":""+content+"","x":""+left+"","y":""+top+"","color_r":""+r+"","color_g":""+g+"","color_b":""+b+"","fold":""+fold+"","ord":""+ord+""};
						Ctrl.Memo.receive(packet);
					}						 
				}
				
				// 4. video share packet
				if(vShare != null){					
					var len = vShare == null ? 0 : vShare.length;
					for(var i=0; i<len; i++){
						var vshareInfo = vShare[i];
						var type = "0";
						var vsno = vshareInfo.commentno || '';
						var seqno = vshareInfo.seqno || '';
						var userid = PacketMgr.userid || '';
						var userno = PacketMgr.userno || '';
						var usernm = PacketMgr.usernm || '';
						var datetime = vshareInfo.cdatetime || '';
						var title = vshareInfo.title || '';
						var link = vshareInfo.content || '';
						var left = vshareInfo.posx || 0;
						var top = vshareInfo.posy || 0;
						var status = vshareInfo.plugindata || '';
						var ord = vshareInfo.ord || 1;
						var r = vshareInfo.red;
						var g = vshareInfo.green;
						var b = vshareInfo.blue;
						var time = 0; 
						 
						var vsharePacket = {"cmd":"vshare","type":type,"vsno":""+vsno+"","seqno":""+seqno+"","datetime":""+datetime+"","title":""+title+"","content":""+link+"","x":""+left+"","y":""+top+"","status":""+status+"","ord":""+ord+"","time":""+time+"","userno":""+userno+""};			
						Ctrl.VShare.receive(vsharePacket);
					}
				}
				
				// PAGE AUTH
				// 문서가 반드시 존재하는 회의만 판서관련 패킷을 sync 한다.
				/****
					Ctrl.callCurPensetting(true);
					// 현재 마스터가 뷰 모드 보고있으면, 뷰 모드로 전환해준
					if(Ctrl.isHand() ) PacketMgr.Master.changeMode("view");
					
					// ZOOM 유지
					if(UI.scale > 1){
						// 현재 마스터의 zoom 패킷을 보내 동기화를 맞춰준다.
						var percent = parseInt($("#zoomval").val(), 10);
						PacketMgr.Master.zoomCurrent(percent, "1");
					}
				***/
			});
			
		},
		
		load : function(pageId, callback){
			var url = Utils.addContext(_url("page.get.info"));
			var data = {
				roomid : PacketMgr.roomid,
				pageid : pageId
			};			
			Utils.request(url, "json", data, function(json){
				if(json.result == 0){
					callback(json);
				}
			});
		},
		
		add : function(){
			if(!Ctrl._checkAuth(true)) return;
			
			// UI.Page.limit;
			var currentCnt = $(".multi_page", "#sortable_box").length;
			var userLimitCnt = parseInt($("#userLimitCnt").html());
			
			if(PacketMgr.userLimitCnt <= 3 && currentCnt >= 3){
				AttendPopup.requestPageExtend('0', RoomSvr.roomid, 'canvas');				
				return;
			}

			Ctrl.Modal.confirm(_msg("confirm.page.add"), function(){
				var url = Utils.addContext(_url("page.add"));
				var data = {
					roomid : PacketMgr.roomid
				};			
				Utils.request(url, "json", data, function(json){		
					var pageId = json.pageid;					
					// set page menu layer
					UI.Page.addLayer(pageId);					
					
					PacketMgr.Master.page(pageId, '0', '');					
					
					UI.Page.change(pageId);
				}); 
			});			
		},
		
		change : function(pageId){
			if(UI.current == pageId) return;
			
			if(!Ctrl._checkAuth(true)) return;
			// change page이전 캔버스 저장
			// Ctrl.Uploader.checkSaveTimer("ended", null);
			var url = Utils.addContext(_url("page.change"));
			var data = {
				roomid : PacketMgr.roomid,
				pageid : pageId
			};
			
			Utils.request(url, "json", data, function(json){			
				// UI.Page.changeUI(pageId);
				PacketMgr.Master.page(pageId, '1', '');
				UI.Page.changeUI(pageId);
			});
		},
		
		changeUI : function(pageId){			
			if(UI.current == pageId) return;
			
			CanvasApp.hideHistoryLoading();
			
			var beforeBoard = UI.getBoard();
			this.beforeScale = beforeBoard.getZoomScale();
			this.beforeZoomInfo = beforeBoard.getZoom();
			
			UI.Page.destroy(UI.current);

			$("#" + UI.current).removeClass("multi_selected");
			$(".removePageBtn", "#" + UI.current).show();
			$("#sketch" + UI.current).hide();
 
			$("#" + pageId).addClass("multi_selected");
			$(".removePageBtn", "#" + pageId).hide();
			$("#sketch" + pageId).show();
			
			UI.Page.syncPageInfo(pageId);

			UI.Page.set(pageId);
			
			UI.current = pageId;
			// text annotaion 갱신
			// Ctrl.Text._setTextEvent();
			// text annotation canvas vlur event
			Ctrl.Text._setTextEvent();
		},
		
		remove : function(pageId){
			
			if(pageId == UI.current) return;
			
			if(!Ctrl._checkAuth(true)) return;
			
			Ctrl.Modal.confirm(_msg("confirm.page.remove"), function(){
				var url = Utils.addContext(_url("page.remove"));
				var data = {
					roomid : PacketMgr.roomid,
					pageid : pageId
				};				
				Utils.request(url, "json", data, function(json){	
					UI.Page.removeLayer(pageId);
					PacketMgr.Master.page(pageId, '2', '');					
				});				
			});
			
		},
		
		receive : function(packet){
			var type = packet.type; // 0=add, 1-change, 2-remove
			var pageId = packet.pageid;
			
			if(type == "0"){
				UI.Page.addLayer(pageId);
			}else if(type == "1"){				
				UI.Page.changeUI(pageId);				
			}else if(type == "2"){
				UI.Page.removeLayer(pageId);
			}else if(type == "3"){
				UI.Page.orderUI(packet.order);
			}
		},
		
		addLayer : function(pageId){
			
			// set skBoards
			var skboardDiv = "<div id=\"sketch"+pageId+"\" class=\"sketch\" style=\"width: 100%; height: 100%; overflow: hidden; position: relative;\"></div>";
			$(skboardDiv).insertBefore(".draw_container");
			
			// selected clear
			$(".multi_page", "#pageContainer").removeClass("multi_selected");
			
			var roomId = RoomSvr.roomid;
			var pageImgSrc = "/data/fb/room/" + roomId.substring(0, 3) + "/" + roomId + "_page" + "/" + pageId + ".jpg";
			// <img src="<%=pagePreview %>" alt="" onerror="this.style.display = 'none';" />
			
			var html = "<div id=\""+pageId+"\" class=\"multi_page multi_selected\">\
							<a class=\"btn_close2 removePageBtn\" alt=\"close\" title=\"close\"></a>\
							<img src=\""+pageImgSrc+"\" alt=\"\" onerror=\"this.style.display='none';\" />\
						</div>"
			
			$("#sortable_box").append(html);
			
			$("#" + pageId, "#pageContainer").click(function(){
				var pageId = $(this).attr("id");
				UI.Page.change(pageId);
				

			    
			});
			
			$(".removePageBtn", "#" + pageId).click(function(e){
				var pageId = $(this).parent().attr("id");
				e.stopImmediatePropagation();
				UI.Page.remove(pageId);
			});
			
			// 미리보기 상단 
			this.syncPageInfo(pageId);
		},
		
		removeLayer : function(pageId){
			$(".multi_page", "#pageContainer").each(function(){
				var id = $(this).attr("id");
				if(pageId == id){
					$(this).unbind("click");
					$(".removePageBtn", $(this)).unbind("click");
					
					$(this).remove();
				}					
			})
			
			this.syncPageInfo(pageId);
		},
		
		syncPageInfo : function(pageId){
			// 미리보기 상단 
			var cnt = 0;
			$(".multi_page", "#pageContainer").each(function(){
				if(pageId == $(this).attr("id")){
					var idx = $(this).index();
					$("#pageIdx").html( ++idx );		
				}
				cnt++;
			});
			
			$("#pageCnt").html(cnt);
		},
		
		order : function(){
			var ordList = "";
			var ordNo = 1;
			$("div.multi_page", "#sortable_box").each(function(){
				var pageId = $(this).attr("id");
				var ord = pageId + "|" + ordNo++;
				
				if(ordList != "") ordList += ",";
				ordList += ord; 
			});
			
			var url = Utils.addContext(_url("page.order"));
			var data = {
				roomid : PacketMgr.roomid,
				ordlist : ordList
			};				
			Utils.request(url, "json", data, function(json){
				PacketMgr.Master.page(UI.current, '3', ordList);
				
				UI.Page.syncPageInfo(UI.current);
			});
		},
		
		orderUI : function(ordList){
			// orderList => pageId|orderno
			var orders = ordList.split(",");
			var len = orders == null ? 0 : orders.length;
			 
			var orderMap = new Map();
			for(var i=0; i<len; i++){
				var pageId = orders[i].split("|")[0];
				var orderNo = orders[i].split("|")[1];
				orderMap.put(orderNo, pageId);				
			}			
			// set first layer
			/**
			var firstPageId = orderMap.get(1); 
			var currentLayer = $("#" + firstPageId);	
			$("#sortable_box").prepend(currentLayer)
			
			for(var i=1; i<len-1; i++){
				var nextOrder = orderMap.get((i+1));
				$("#" + nextOrder).insertAfter(currentLayer);
				currentlayer = $("#" + nextOrder);				
			}
			***/
			
			$("#ghost").append( $(".multi_page", "#sortable_box") );
				
			for(var i=0; i<len; i++){
				var pageId = orderMap.get((i+1));
				// $("#" + pageId).insertBefore( $("#addPageBtn") );
				$("#sortable_box").append($("#" + pageId));
			}
			 
			// page index 번호 다시 맞춤
			this.syncPageInfo(UI.current);			
		},
		
		destroy : function(pageId){
			
			// 1. background 초기화 
			Ctrl.Background._clear();
			
			// 2. image box 초기화
			Ctrl.BGImg.destroy();
			
			// 3. pdf wrapper 초기화
			PDFViewer.destroy(true);

			// 4. memo destroy
			Ctrl.Memo.destroy();
			
			// . video share destroy
			Ctrl.VShare.destroy();
			
			// 6. text annotation destroy
			// Ctrl.Text.destroy();
			
			Ctrl.Text.clear();
			
			/**
			 *  page destroy
			 */			
			// 1. board destroy
			UI.removeBoard(pageId);

			// layer destroy
			$("#sketch" + pageId).html("");
			$("#sketch" + pageId).css({left : 0});
			$("#sketch" + pageId).hide();
		}
	},
	
	init : function(pageId, pageLimit){
		UI.current = pageId;
		UI.Page.limit = pageLimit;
			
		this.Cursor.init();
		
		this.setDivHolder();
			
		this.setBoard(UI.current);		
		
		this.setEvent();

		this.setBottomShadow();
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
	_isRender : function(pageId) {
		var layout = document.getElementById("sketch" + pageId);
		return (layout) ? true : false;
	},
	
	_getSketch : function(pageId) {
		return document.getElementById(this.SKETCH + pageId);
	},
	
	setBoard : function(pageId){
		// fileMap = new Map();
		var id = "#sketch" + pageId;
		var containerId = "#" + UI.CONTAINER;
		
		var isWeb = !Utils.cordova();
        var app = new SketchBoard();
        drawing = app.init(containerId, id, isWeb, UI.Cursor.images);
		// UI.skboards.push(drawing);
        if(this.boards == null) this.boards = new Map();
        this.boards.put(pageId, drawing);
	},

	getBoard : function(pageId){
		if(typeof(pageId) == "undefined" || pageId == null) pageId = UI.current;
		return this.boards.get(pageId);
	},
	
	removeBoard : function(pageId){
		var board = this.getBoard(pageId);
		board.destroy();
		
		this.boards.remove(pageId);
	},
	
	setDivHolder : function(pageId){
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
		// IE는 무조건 가장 큰 모니터 기준으로 나온다
		
		$("#" + UI.VIEWER).width(canvasWidth);
		$("#" + UI.WRAPPER).width(canvasWidth);		
		$("#" + UI.VIEWER).height(canvasHeight);
		$("#" + UI.WRAPPER).height(canvasHeight);			
	},
	mailtoIgnore : function(){
		this.unloadIgnore = true;
	},
	
	setBottomShadow : function(){
		if(this.cutBottom){
			var id = "#sketch" + UI.current;		
			var height = $(id).height();
			var headerHeight = $("#" + UI.HEADER).height();
			var videoHeight = $("#" + VideoCtrl.CONTAINER).height();
			
			// alert( videoHeight );
			// var shadowHeight = height - window.screen.availHeight + headerHeight - videoHeight;
			
			var termY = Utils.browser("msie") ? $("#" + UI.HEADER).height() : $("#" + UI.HEADER).height();
			var maxWidth = Utils.mobile() ? $(window).width() : window.screen.availWidth;
			var maxHeight =  Utils.mobile() ? $(window).height() : (window.screen.availHeight - (window.outerHeight - window.innerHeight)) - termY;

			//  ("maxWidth : " + maxWidth + ", maxHeight : " + maxHeight);
			// 16 : 9
			var displayHeight = maxWidth * 12 / 16;
			var height = maxWidth - displayHeight;
			
			$("#shadow").height(height);
			
		}else{
			$("#shadow").hide();
		}		
	},
	
	setEvent : function(){	
		var isResolutionConfirmed = false;
		// UI.resolutionTryCnt = 0;
		$(window).bind('resolutionchange', function(){
			if(!isResolutionConfirmed){
				isResolutionConfirmed = true;
				
				Ctrl.Modal.confirm(_msg("noti.res.change"), function(){
					location.href = Utils.addContext("room/" + PacketMgr.code);
				}, function(){
					if(UI.resolutionTryCnt < 2){
						isResolutionConfirmed = false;
						UI.resolutionTryCnt++;
					}					
				});
			}
		});
		
		$(window).bind("beforeunload", function(e){
			// mailto 태그에서 이곳이 걸릴수 있으므로 주의.. mailto는 frame 처리			
			// IE는 A태그 이동시 before unload가 튀어버려서 예외처리 해줘야 한다.			
			// 최초 접속시 scroll 최상단으로
			$(window).scrollTop(0);
			
			// 보드 이동하면서 답변을 진행한 경우에는 polldata 인풋 제거 
			if (typeof $("input[name=polldata]") == "undefined") {
				//Utils.log("답변을 완료하여 polldata 히든필드가 삭제되었네요..")
				window.location.replace(window.location.href);
				return true;
			}
			
			if(UI.unloadIgnore) {
				UI.unloadIgnore = false;
				return;
			}
			
			/**
			if(!Utils.browser("msie")) {
				VideoCtrl.destroyAll();
				RoomSvr.destroy();	
				PacketMgr.destroy();
						
				UI.destroy();
				Ctrl.destroy();
				CanvasApp.destroy();
			}	
			**/
			
			CanvasApp.destroy();
			
			
		});
		
		$(window).bind("unload", function(){			
			CanvasApp.destroy();
		});		
		
		// shift누를 경우 도형 정각형 처리
		$(window).bind("keydown keyup", function(e){
			if(e.keyCode == 16) {
				UI.isShift = (e.type == "keydown") ? true : false;
			}
		});
		
		// window disableselection
		$(window).attr('unselectable','on').css({'-moz-user-select':'-moz-none',
	           '-moz-user-select':'none',
	           '-o-user-select':'none',
	           '-khtml-user-select':'none',
	           '-webkit-user-select':'none',
	           '-ms-user-select':'none',
	           'user-select':'none'
	    }).bind('selectstart', function(){ return false; });
		
		// 네트워크 처리
		$(window).bind("online", function(e){
			Ctrl.setMyNetworkStatus(true, false);
		});

		$(window).bind("offline", function(e){
			Ctrl.setMyNetworkStatus(false, false);			
		});		
		
		/*	
		$(window).scroll(function(e){
		 	Ctrl.Preview.scroll(e);
		});
		*/
		
		// set Page Event
		var pageContainer = $("#pageContainer");			
		$("#addPageBtn").click(function(){
			UI.Page.add();
		})
		
		$(".multi_page", "#pageContainer").click(function(e){
			var pageId = $(this).attr("id");
			UI.Page.change(pageId);
		});
		
		$(".removePageBtn", "#pageContainer").click(function(e){
			var pageId = $(this).parent().attr("id");
			e.stopImmediatePropagation();
			UI.Page.remove(pageId);
		});
		
		$(".btn_close", "#pageContainer").click(function(e){
			
			/**
			$("#pageContainer").hide();			
			$("#preview_navigator").hide();
			
			$(".btn_preview", "#contsWrapper").attr("before", "pageContainer");
			$(".btn_preview", "#contsWrapper").show();
			**/
			
			UI.Page.fold("pageContainer");
			
			e.stopImmediatePropagation();			
		});
		  
		if(PacketMgr.isMC){
			UI.Page.setSortable();		
		}
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
		/**
		var prevBoard = UI.skboards[idx - 2];
		var nextBoard = UI.skboards[idx];
		// 현재 페이지 Zoom
		UI.skboards[idx - 1]["async"](zoomPacket);
		
		// 이전 페이지 Zoom, 다음페이지의 Zoom은 clear한다.
		if(prevBoard) prevBoard.clearZoom();
		if(nextBoard) nextBoard.clearZoom();
		***/
		var currentBoard = UI.getBoard();
		currentBoard["async"](zoomPacket);	
				
		// preview update
		Ctrl.Preview.update(zoomPacket);
	},

	destroy : function(){
		try{
			// pdf를 먼저 삭제하고 날린다.
			PDFViewer.destroy(false);
/**
			var len = this.skboards.length;
			for (var i = 0; i < len; i++) {
				this.skboards[i].destroy();
			}
*/		 
			$("#addPageBtn").unbind("click");			
			$(".multi_page", "#pageContainer").unbind("click");			
			$(".removePageBtn", "#pageContainer").unbind("click");			
			$(".btn_close", "#pageContainer").unbind("click");
			$(".btn_minimize2", "#pageContainer").unbind("click");
			
			if(PacketMgr.isMC && $("#sortable_box").hasClass("ui-sortable")){
				$("#sortable_box").sortable("destroy");	
			}
			
			var keys = UI.boards.keys();			
			var len = keys == null ? 0 : keys.length;
			for(var i=0; i<len; i++){
				var idx = keys[i];
				var board = UI.boards.get(idx);
				board.destroy();
			}
			
			this.boards = null;
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
	usertype : "",
	email : "",
	thumbnail : "",
	roomid : "",	
	groupno : "",
	masterid : "",
	masterno : "",
	parentcreatorno : "",	// 부모방 마스터
	creatorid : "",
	creatornm : "",
	layout : "",		// VIEW 모드 1-영상, 2-혼합
	bg : "",
	code : "",
	auth : "2",
	fullscreen : "",
	waitFileSeq : 9999,	
	maxPacketCnt : 0,
	currentCnt : 0,	
	skipCnt : 0,
	userLimitCnt : 3,
	percent : 0,
	packet : null,	
	lastTime : 0,
	isSend : false, // 룸서버에 전송해야 하는지 여부
	isMC : false,	// 진행자 여부
	isCreator : false,	// 방 생성자 여부
	isParentCreator : false,	// 부모방 생성자 여부	
	isAllowMaster : true,	// 기본 허용
	isAllowChat : true,	// 기본 허용
	isAllowComment : true,	// 기본 허용
	isAllowExport : true,	// 기본 허용
	isOnlyTeacherVCam : false,	// 캠 선생님만 허용
	isOnlyTeacherVShare : false,	// 영상공유 선생님만 허용
	isGuest : false, 
	isVideo : false,
	isKicked : false,
	pdfs : [],
	queue : [], // 렌더링 밀릴경우 명령어 큐를 따로 쌓아두고 처리한다.
	progressQueue : [],
	animationQueue : [],
	penIdxList : [],
	pageMap : null,
	penMap : null,	//	load로 전달받은 기본 패킷  
	lastPensetting : null,	// 회의방 최초 입장시 받아온 패킷의 펜세팅값. 마지막 드로잉 패킷값. (초기 로딩시에만 사용)
	lastPageId : null,	
	lastPacketNo : 0,		// history 저장용 
	isInitFChange : true, // 초기 filechange 이벤트가 안올수도 있기때문에 flag처리
	isLoadPacket : false,
	isLoadResize : false,
	isCommentPos : false,
	isMemoPos : false,
	saveList : ['pensetting', 'redo', 'undo', 'erasermode', 'draw', 'began', 'moved', 'ended', 'eraserbegan', 'erasermoved', 'eraserended', 'zoom', 'view', 'textbox'],
	receiveForceList : ['masterdone', 'sync', 'kickuser', 'video_options', 'video_group', 'video_screen'],	// receive시 자기가 보낸 패킷을 그대로 receive기능으로 동작시킨다.
	skipList : ['pdf','img','memo','zoom','sync'],
	shapePassList : ['moved', 'ended', 'erasermoved', 'eraserended'],
	init : function(options) { 
		// 마스터와 권한을 저장한다.
		this.userid = options.userid || '';
		this.usernm = options.usernm || '';
		this.userno = options.userno || '';
		this.usertype = options.usertype || '';
		this.email = options.email || '';
		this.thumbnail = options.thumbnail || '';		
		this.roomid = options.roomid || '';
		this.groupno = options.groupno || '';		
		this.masterid = options.masterid || '';	// 마스터 아이디는 권환 회수시 마스터 비교용으로 사용한다.
		this.masterno = options.masterno || '';
		this.parentcreatorno = options.parentcreatorno || '';	// 부모방 마스터.
		this.creatorid = options.creatorid || '';
		this.creatornm = options.creatornm || '';		
		this.creatorno = options.creatorno || '';
		this.layout = options.layout || '';
		this.lastPageId = options.currentpageid || '';
		this.lastPacketNo = options.packetno || '';
		this.code =  options.code || '';
		this.fullscreen = options.fullscreen || '';
		this.userLimitCnt = options.userlimitcnt || 3;
		
		// var authJson = $.parseJSON(options.auth);
		var authJson = options.auth;
		this.isAllowMaster = authJson.authtype == "1" ? true : false;
		this.isAllowChat = authJson.chatopt == "1" ? true : false; 
		this.isAllowComment = authJson.cmtopt == "1" ? true : false; 
		this.isAllowExport = authJson.expopt == "1" ? true : false;
		
		this.isOnlyTeacherVCam = (authJson.vcamopt == "1") ? true : false;
		this.isOnlyTeacherVShare = (authJson.vshareopt == "1") ? true : false;
				
		this.isMC = this.masterid == this.userid ? true : false;
		this.isCreator = this.creatorid == this.userid ? true : false;					
		this.isGuest = this.userid == this.userno ? true : false;
		this.isParentCreator = this.parentcreatorno == this.userno ? true : false;
		 
		// var plugin = $.parseJSON(options.plugin);
		var plugin = options.plugin;
		this.isCommentPos = plugin.comment == "1" ? true : false;
		this.isMemoPos = plugin.memo == "1" ? true : false;		
		// this.bg = $.parseJSON(options.bg) || '';
		this.bg = options.bg || '';
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
		if(PacketMgr.queue == null || PacketMgr.queue.length < 1) return;		
// Utils.log("UI.rendering : " + UI.rendering + ", queue len : " + PacketMgr.queue.length);		
		if(PDFViewer.rendering || PacketMgr.isLoadPacket){
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
			var pageId = typeof(json.page) != "undefined" && json.page != null ? json.page : UI.current;			
			PacketMgr.Master.toCanvasPage(json, pageId, true);			
		}

		PacketMgr.shiftQueue();
		// setTimeout(PacketMgr.shiftQueue, "10");		
	},

	shiftQueueSkipOverlapCmd : function(){
		if(PacketMgr.queue != null && PacketMgr.queue.length > 0){			
			var list = [];
			var len = PacketMgr.queue.length;
			for(var i=0; i<len; i++){
				var tmpPacket = PacketMgr.queue[i];
				if(PacketMgr.skipList.indexOf(tmpPacket.cmd) < 0){
					list.push(tmpPacket);
				}else{
					var beforePacketIdx = -1;
					for(var j=0; j<list.length; j++){
						if(list[j].cmd ==  tmpPacket.cmd){
							beforePacketIdx = j;
							break;
						}
					}
					
					var beforePacket =  beforePacketIdx > -1 ? list[beforePacketIdx] : -1;
					list = list.without(beforePacket);
				}				
				list.push(tmpPacket);
			}
			

			PacketMgr.queue = [];
			PacketMgr.queue = list;
			
			PacketMgr.shiftQueue();
			// pagePacketList = pagePacketList.without(pagePacketList[i]);
			// PacketMgr.shiftQueue();
		}
	},
	
	_shapeUnfinishedDrawing : function(){
		var board = UI.getBoard(UI.current);		
		if(board){
			// 기존 드로잉 패킷이 정상 종료가 되지 않았다면, 강제로 ended시켜준 
			if(!board.isDrawEnd()){
				board.drawShapeForce();
				
				/** 패킷 히스토리에서 꺼내는 방식 
				var existedPacket = PacketMgr.pageMap.get(UI.current);
				if(existedPacket != null){				
					var len = existedPacket.get("list") == null ? 0 : existedPacket.get("list").length;
					var list = existedPacket.get("list");
					
					if(list != null && list.length > 0){
						var lastPacket = list[list.length - 1]; 
						
						if(lastPacket){
							// (lastPacket.cmd.indexOf("began") > -1 || lastPacket.cmd.indexOf("moved") > -1
							var isChanged = false;
							if(lastPacket.cmd.indexOf("began") > -1) {
								lastPacket.cmd = lastPacket.cmd.replace("began", "ended");		
								isChanged = true;
							}else if(lastPacket.cmd.indexOf("moved") > -1){
								lastPacket.cmd = lastPacket.cmd.replace("moved", "ended");
								isChanged = true;
							}						
							if(isChanged){ 
								
console.log("do last Packet save");

								PacketMgr.Master.toCanvasPage(lastPacket, UI.current, false);	
							}
						}					
					}				
				}
				***/
			}
		}
	},
	
	
	// room서버에서 전송받은 data 처리
	receive : function(userid, data) {		
		
		// 잠금화면 일때 간혹 online callback이 떨어지지 않을때가 있다. 
		// receive 패킷을 받았다면 online이므로 online 처리 해준다.
		Ctrl.setMyNetworkStatus(true, false);
		
		this.lastTime = new Date().getTime();
// Utils.log("receive userid : " + userid + ", data : " + data + ", this.lastTime : " + this.lastTime);
		
 		// if 큐가 데이터 존재하면 큐에 넣고 break;
		var json = (typeof(data) == "object") ? data : JSON.parse(data);
		var cmd = json.cmd ? json.cmd : json.from ? json.from : "";

		if (userid == this.userid && PacketMgr.receiveForceList.indexOf(cmd) < 0) {
// 내가 그린 패킷을 새로 그리지 않는다.
// Utils.log("<< packet same user >> " + JSON.stringify(data));

		} else {
// Utils.logger(json.cmd, "패킷수신", (new Date().toString() + ", millisecond : " + new Date().getTime()));
			// addfile은 순서에 상관없이 강제로 실행시킨다.
			if(cmd == "addfile"){
				PacketMgr.Command[cmd](json);
				return;
			}
			
/*Utils.log("receive PDFViewer.rendering : " + PDFViewer.rendering + ", PacketMgr.isLoadPacket : " + PacketMgr.isLoadPacket + ", PacketMgr.isInitFChange : " + PacketMgr.isInitFChange 
		+ ", this.Command[cmd] : " + typeof(this.Command[cmd]) + ", PacketMgr.waitFileSeq : " + PacketMgr.waitFileSeq + ", PacketMgr.queue : " + JSON.stringify(PacketMgr.queue) ); 
*/
			// UI가 렌더링 중이거나 addFile이 안들어온경우 큐에 저장하고 있는다.

			if(PDFViewer.rendering){
				// 2016.01.12 pdf rendering 중일때는 pdf 관련 패킷만 queue 처리 한다.
				if(cmd == "pdf"){
					PacketMgr.queue.push(json);
				}else{
					if(typeof (this.Command[cmd]) != "undefined"){
						// 큐가 비어있는 경우는 그냥 명령 실행
						PacketMgr.Command[cmd](json);				
					} else {
						// 드로잉 관련 패킷은 이곳으로 들어오고, 렌더링 중일때는 큐에 쌓았다가 던진다.				
						PacketMgr.Master.toCanvasPage(json, UI.current, false);
					}
				}
				
			} else if(PacketMgr.isLoadPacket || PacketMgr.isLoadResize) {
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

				// begin 패킷을 받았을때 드로잉 완료안된 부분이 있따면 강제로 shape 시켜준다.
				// moaved가 began에서 끝난경우 shape 시켜준다.
				/**
				if(json.cmd != "moved" && json.cmd != "ended") PacketMgr._shapeUnfinishedDrawing();
				
				**/
				
console.log("PacketMgr.shapePassList.indexOf(json.cmd) : " + PacketMgr.shapePassList.indexOf(json.cmd));
				if(PacketMgr.shapePassList.indexOf(json.cmd) < 0){
					 PacketMgr._shapeUnfinishedDrawing();
				}
				
				// 드로잉 관련 패킷은 이곳으로 들어오고, 렌더링 중일때는 큐에 쌓았다가 던진다.		
				PacketMgr.Master.toCanvasPage(json, UI.current, false);
			}

			// history가 아닌 packet으로 받은 pensetting만 업데이
			if(!PacketMgr.isMC && cmd == "pensetting") PacketMgr.Master.masterPensetting = json;
			
			// 히스토리에 어노테이션 저장
			if(PacketMgr.saveList.indexOf(cmd) > -1) PacketMgr._setPacketMap(json, UI.current);
		}
	},
	
	// 히스토리 패킷 처리  
	receiveForSync : function(data, pageId, isAnimation, nextPacket){
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
		var isChanged = false;
		if(cmd == "began" || cmd == "eraserbegan"){
			var lastPenSetting = PacketMgr._getLastPen(json.packetno);			
// Utils.log("UI.rendering : " + UI.rendering + ", PacketMgr.queue.length : " + PacketMgr.queue.length);			
			if(lastPenSetting != null){
				if(PDFViewer.rendering || PacketMgr.queue.length > 0){
					PacketMgr.queue.push(lastPenSetting);				
				}else{
					PacketMgr.Command["pensetting"](lastPenSetting);	
				}
			}
		}else if(cmd == "moved"){
			if(nextPacket == null || (nextPacket.cmd != "ended" && nextPacket.cmd != "moved")){
				cmd = "ended";
				json.cmd = "ended";
				isChanged = true;
				
			}
		}else if(cmd == "erasermoved"){
			if(nextPacket == null || (nextPacket.cmd != "eraserended" && nextPacket.cmd != "erasermoved")){
				cmd = "eraserended";
				json.cmd = "eraserended";
				isChanged = true;
			}			
		}
		
		if(isChanged){
console.log("changed json : " + JSON.stringify(json));

		}


		// UI가 렌더링 중이거나 addFile이 안들어온경우 큐에 저장하고 있는다.
		PacketMgr._runCommand(cmd, json, pageId);
		
// console.log("PacketMgr.currentCnt : " + PacketMgr.currentCnt + ", PacketMgr.maxPacketCnt : " + PacketMgr.maxPacketCnt);		
	},
	
	_runCommand : function(cmd, json, pageId){		
// Utils.log("_runCommand UI.rendering : " + UI.rendering + ", queue : " + JSON.stringify(PacketMgr.queue));
		if (PDFViewer.rendering) {
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
			PacketMgr.Master.toCanvasPage(json, pageId, true);
		}
	},
	
	setPageScale : function(pageId){
		// var board = UI.skboards[pageNo - 1];
		var board = UI.getBoard();
		if(board != null){
			var scale = board.getZoomScale();
			Ctrl.setZoomScale(scale);
		}
	},

	/***  fileMap(fileno->isajax, ->pageMap(pageno->isdraw, ->list[packetlist])) */ 
	loadHistory : function(roomId, pageId, callback){		
// Utils.log('enter load!!');		
// 이미 로딩된 데이터가 있는지 체크한다.
		/**
		if(PacketMgr.fileMap != null){
			PacketMgr._clearHistory();
			
			var fileMap = PacketMgr.fileMap.get(fileNo);
			// 이미 request 한적이 있으면 pass
			if(typeof(fileMap) != "undefined" && fileMap != null) return;			
		}
		 
		var roomFileCnt = parseInt(fileCnt);		
		var getPen = roomFileCnt > 1 ? "1" : "";		
		*/
		 
		if(PacketMgr.pageMap != null){  
			// 페이징시 이미 패킷이 있는 경우 데이터 가공
			var existedPacket = PacketMgr.pageMap.get(pageId);
			// console.log("existedPacket", JSON.stringify(existedPacket));
			if(existedPacket != null){				
				var len = existedPacket.get("list") == null ? 0 : existedPacket.get("list").length;
				var list = existedPacket.get("list");

				PacketMgr.penIdxList = null;				
				PacketMgr.penIdxList = [];
				PacketMgr.penMap = null;
				PacketMgr.penMap = new Map();
				
				for(var i=0; i<len; i++){
					var packet = list[i];
					var cmd = packet.cmd;
					var packetIdx = packet.packetno;
					
					if(cmd == "pensetting"){
						PacketMgr.penIdxList.push(parseInt(packetIdx));						
						if(PacketMgr.penMap == null){
							PacketMgr.penMap = new Map();
						}						
						PacketMgr.penMap.put(packetIdx, packet);
					}
				}				
				if(PacketMgr.penIdxList != null && PacketMgr.penIdxList.length > 1){
					PacketMgr.penIdxList.sort(function(a, b){ return a-b;});
				}

				if(callback) callback(len);
				return;
			}			
		}		 
		
		var module = $("#rsaModule").val();
		var exponent = $("#rsaExponent").val();
		var uuid = Utils.createUUID().substring(0, 5);
		
		var rsa = new RSAKey();
		rsa.setPublic(module, exponent);
		
        var tokenStr = rsa.encrypt(roomId + ',' + pageId + ',' + uuid);
		if(tokenStr == null) {
			alert("encrypt fail..");
			return;
		}
		
		var url = Utils.addContext(_url("canvas.packet"));
		var data = {
			//roomid : roomId,	
			//fileno : fileNo,
			token  : tokenStr
		};
		
		PacketMgr.isLoadPacket = true;
		
		// pen idx 초기화
		PacketMgr.penIdxList = null;
		PacketMgr.penIdxList = [];

var startTime = new Date().getTime(); 
Utils.log("History Request 시작  : ", (new Date().toString() + ", millisecond : " + startTime));

		Utils.request(url, "json", data, function(json){
			
var endTime = new Date().getTime();
Utils.log("History Request 끝  : ", (new Date().toString() + ", millisecond : " + (endTime - startTime)));
			
			var isFirstLoad = false;
			var pageMap = PacketMgr.pageMap;
			var skipCnt = json.skipcnt || 0;
			// 파일맵은 무조건 한번 init 해준다.
			if(typeof(pageMap) == "undefined" || pageMap == null){
				isFirstLoad = true;
				pageMap = new Map();
			} 
			
			if(json && json.result == 0){
				var list = json.list;
				var len = list == null ? 0 : list.length;

				PacketMgr.lastPensetting = json.lastpensetting != null ? json.lastpensetting : null;
				
				var packetMap = pageMap.get(pageId);
				if(typeof(packetMap) == "undefined" || packetMap == null) packetMap = new Map();
				
				var drawFlag = packetMap.get("isdraw");
				if(typeof(drawFlag) == "undefined" || drawFlag == null) packetMap.put("isdraw", false);
				
				var pagePacketList = packetMap.get("list");
				if(typeof(pagePacketList) == "undefined" || pagePacketList == null) pagePacketList = [];
				
				for(var i=0; i<len; i++){
					var item = list[i];
					var pageId = item.pageid;
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
					// if(cmd == "pensetting" || cmd == "view") continue;
					
					packet.packetno = packetIdx;
					pagePacketList.push(packet);
				}

				packetMap.put("list", pagePacketList);
				pageMap.put(pageId, packetMap);
				PacketMgr.pageMap = pageMap;

				if(!isFirstLoad && PacketMgr.penIdxList != null && PacketMgr.penIdxList.length > 1){
					PacketMgr.penIdxList.sort(function(a, b){ return a-b;});
				}
				
				
				if(PacketMgr.skipCnt == 0){
					PacketMgr.skipCnt = skipCnt;
				}				

				PacketMgr.isLoadPacket = false;
				if(callback) callback(len);
			} 
			
		}, function(e){
// Utils.log("load history error : " + JSON.stringify(e));
			PacketMgr.isLoadPacket = false;			
		}, function(e){
			PacketMgr.isLoadPacket = false;			
		}); 
	},
	
	_setPacketMap : function(packet, pageId){
		var cmd = packet.cmd;		
		if((cmd == "textbox" && packet.type == "2")) return;
		
		PacketMgr.lastPacketNo++;  

		// packet 을 저장한 데이터로 재사용하기 위해서는 pensetting 저장 필요
		var packetIdx = PacketMgr.lastPacketNo;
		if(cmd == "pensetting"){
			PacketMgr.penIdxList.push(parseInt(packetIdx));
			if(PacketMgr.penMap == null){
				PacketMgr.penMap = new Map();
			}
			PacketMgr.penMap.put(packetIdx, packet);
		}
		
		var pageMap = PacketMgr.pageMap;
		if(typeof(pageMap) == "undefined" || pageMap == null){
			isFirstLoad = true;
			pageMap = new Map();
		} 
		
		var packetMap = pageMap.get(pageId);
		if(typeof(packetMap) == "undefined" || packetMap == null) packetMap = new Map();
		
		var drawFlag = packetMap.get("isdraw"); 
		if(typeof(drawFlag) == "undefined" || drawFlag == null) packetMap.put("isdraw", false);
		
		var pagePacketList = packetMap.get("list");
		if(typeof(pagePacketList) == "undefined" || pagePacketList == null) pagePacketList = [];
				
		packet.packetno = packetIdx;
		pagePacketList.push(packet);
		packetMap.put("list", pagePacketList);

		pageMap.put(pageId, packetMap);
	
		PacketMgr.pageMap = pageMap;
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
	
	_getNextPacket : function(pageId, packetNo){
		var existedPacket = PacketMgr.pageMap.get(pageId);
		if(existedPacket != null){
			var len = existedPacket.get("list") == null ? 0 : existedPacket.get("list").length;
			var list = existedPacket.get("list");
			for(var i=0; i<len; i++){
				
				
				/**
				var dataPacketNo = PacketMgr.penIdxList[i];
				if(dataPacketNo > packetIdx) break;					
				***/
				
			}
			
		}
	},
	
	_drawPagePacket : function(pageId, isAnimation){
		// 히스토리가 존재하는지 체크할것		
		if(PacketMgr.pageMap != null){
			var packetMap = PacketMgr.pageMap.get(pageId);

			if(packetMap != null){
				var isDraw = packetMap.get("isdraw");
				var isRender = UI._isRender(pageId);
				// if(!isDraw && isRender){
				if(isRender){
					var pagePacketList = packetMap.get("list");
					var len = pagePacketList == null ? 0 : pagePacketList.length;
					
					PacketMgr.maxPacketCnt = len;
					for(var i=0; i<len; i++){
						var packet = pagePacketList[i];
						var nextPacket = (i+1) >= len ? null : pagePacketList[i+1];
						
						if(isAnimation){
							// 1. queue에 만 쌓아두는 방식은 좀 느리다. 평균 26 / 50 / 2분 30초
							// 1. 느리지만 정밀한 큐잉
							// PacketMgr.animationQueue.push(packet);								
							// 2. 빠르지만 부분적으로 뭉텅이로 그려지는 빠른 큐잉
							PacketMgr.animationQueue.push(packet);
							setTimeout(function(){
								if(PacketMgr.animationQueue.length < 1) {
									Ctrl.ProgressLoader.hide();
									return;
								}

								var json = PacketMgr.animationQueue.shift();
								if(json != null) PacketMgr.receiveForSync(json, pageId, isAnimation, nextPacket);	

								var percent = PacketMgr.currentCnt++ * 100 / PacketMgr.maxPacketCnt;
								Ctrl.ProgressLoader.update(Math.floor(parseInt(percent)));							
								
								if(PacketMgr.animationQueue.length == 0){
									// view모드로 강제 전환 
									PacketMgr.Master.changeMode("view");
								}								
							}, "1");
							
						}else{				
							PacketMgr.receiveForSync(packet, pageId, isAnimation, nextPacket);
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
		
		if(isAnimation) {
			PacketMgr._shiftAnimationQueue(pageId);
		}
	},

	_shiftAnimationQueue : function(pageId){
		if(PacketMgr.animationQueue.length < 1) {
			Ctrl.Loader.hide();
			Ctrl.ProgressLoader.hide();
			return;
		}
		
		var len = PacketMgr.animationQueue == null ? 0 : PacketMgr.animationQueue.length;
		// UI가 렌더링 중일경우 밀린 큐 하나씩 꺼내서 실행
	  
		setTimeout(function(){
			var json = PacketMgr.animationQueue.shift();
			PacketMgr.receiveForSync(json, pageId, true, null);
			PacketMgr._shiftAnimationQueue(pageId);
		}, "1");
		
	},
	
 	// 히스토리 초기화 및 id와 일치하는 패킷 삭제 (삭제된 패킷 리턴)
	_removePacket : function(pageId, id, isRedraw){ 	
 		var removedPacket = null; 		
 		if(PacketMgr.pageMap != null){
			var packetMap = PacketMgr.pageMap.get(pageId);
			if(packetMap != null){
				var isDraw = packetMap.get("isdraw");
				var isRender = UI._isRender(pageId);					
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
 		
 		return removedPacket;
 	},

	removePacketPage : function(pageId){
		if(PacketMgr.pageMap != null){
			// 마지막 펜과 zoom을 빼고, 모두 지운다.
			// PacketMgr.pageMap.remove(pageId);
			
			var packetMap = PacketMgr.pageMap.get(pageId);
			if(packetMap != null){
				var pagePacketList = packetMap.get("list");
				var configList = [];
				var len = pagePacketList == null ? 0 : pagePacketList.length;
	
				var lastPen = null;
				var lastZoom = null;
				
				PacketMgr.lastPacketNo = 0;
				
				for(var i=(len-1); i>=0; i--){
					var packet = pagePacketList[i];
// var cmd = lastPacket.cmd ? lastPacket.cmd : lastPacket.from ? lastPacket.from : "";
					if(packet.cmd == "pensetting" && lastPen == null){
						packet.packetno = ++PacketMgr.lastPacketNo;
						lastPen = packet;
						configList.push(packet);
						continue;						
					}else if(packet.cmd == "zoom" && lastZoom == null){
						packet.packetno = ++PacketMgr.lastPacketNo;
						lastZoom = packet;
						configList.push(packet); 
						continue;
					}
				} 

				packetMap.remove("list");
				packetMap.put("list", configList);
				
				// lastPacketNo
			}			
		}
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
	// 초기 입장시 이전에 저장된 패킷 드로잉
	_drawHistoryPacket : function(pageId, isAnimation, callback){		
// 일단 이부분은 진행자가 아니면 pass
// if(!PacketMgr.isMC) return;
		if(PacketMgr.isLoadPacket){
			// 로딩끝날떄까지 재귀호출
			Utils.runCallback(function(){
				PacketMgr._drawHistoryPacket(pageId, isAnimation, callback);
			}, 100);
			
		}
/*
		if(PacketMgr.fileMap == null){
			Ctrl.Loader.hide();
			Ctrl.ProgressLoader.hide();
			return;
		}
		*/
		// var drawPacket = function(){
// Utils.log("drawpacket enter!! PacketMgr.lastPensetting : " + PacketMgr.lastPensetting + ", fileMap : " + PacketMgr.fileMap);

		if(PacketMgr.lastPensetting != null){
			var packet = PacketMgr.lastPensetting.packet; 
			// PacketMgr.Master.toCanvas(packet);
			PacketMgr.Master.toCanvasPage(packet, UI.current, true);
		}
		
		/**
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
				Ctrl.ProgressLoader.hide();
			}		
		}
		***/
		if(PacketMgr.pageMap != null){			
			PacketMgr._drawPagePacket(pageId, isAnimation);
		}else{
			Ctrl.Loader.hide();
			Ctrl.ProgressLoader.hide();
		}
		
		if(callback) callback();
		// };
		
		// 함수가 call되는 순간 비동기 동작이 일어난다.
		// drawPacket();
	},
	
	_drawLastZoomPacket : function(pageId){
		// 1. 마지막 페이지의 zoom 을 찾는다.
		// 2. drawing
		
		var lastPacket = null;
		/**
		if(PacketMgr.fileMap != null){	
			// var pageMap = PacketMgr.fileMap.get(fileNo);
			var fileDataMap = PacketMgr.fileMap.get(fileNo);				
			var pageMap = fileDataMap != null ? fileDataMap.get("pagemap") : null;
			
			if(pageMap != null) {
				var packetMap = pageMap.get(pageId);
				if(packetMap != null){
					var isRender = UI._isRender(pageId);
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
		***/
		if(PacketMgr.pageMap != null){
			var pageMap = PacketMgr.pageMap;
			var packetMap = pageMap.get(pageId);
			if(packetMap != null){
				var isRender = UI._isRender(pageId);
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
// Utils.log("lastPacket : " + JSON.stringify(lastPacket) + ", UI.rendering : " + UI.rendering + ", PacketMgr.queue.length : " + PacketMgr.queue.length );
		if(lastPacket != null){
			var cmd = lastPacket.cmd ? lastPacket.cmd : lastPacket.from ? lastPacket.from : "";
			PacketMgr._runCommand(cmd, lastPacket, pageId);
		}		
	},
	
	Command : {
		masterchange : function(packet){			
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
			
			// 영상 공유 
			Ctrl.VShare.auth();

			// slider enable
			Ctrl._usePlugin(PacketMgr.isMC);
			
			// 초기화 
			Ctrl.Text.auth();
			
			// pdf viewer 
			PDFViewer.auth();
			
			// page auth
			UI.Page.auth();
			
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
			
			// masterChange 
			VideoCtrl.masterChange(userInfo != null ? userInfo.userno : "");
			
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
			
			// 영상 공유
			Ctrl.VShare.auth();
			
			// slider enable
			Ctrl._usePlugin(PacketMgr.isMC);
			
			// text annotation auth
			Ctrl.Text.auth();
			
			// pdf viewer auth
			PDFViewer.auth();
			
			// page auth
			UI.Page.auth();
			
			// 초기화 
			// Ctrl.Text.auth();
			// masterchange 시 마스터 위치 변경 			
			var userInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
			if(userInfo != null){
				var userNm = userInfo == null ? "" : userInfo.usernm;		
				
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
			
			// masterChange
			VideoCtrl.masterChange(userInfo != null ? userInfo.userno : "");
			
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
		
		page : function(packet){
			UI.Page.receive(packet);
		}, 
		
		chat : function(packet){
			Ctrl.Chat.receive(packet);
		},  
		
		pensetting : function(packet) {			
// Utils.log("pensetting enter : " + JSON.stringify(packet));
			PacketMgr.Master.curPensetting = packet;
			
			// PacketMgr.Master.toCanvas(packet);
			PacketMgr.Master.toCanvasPage(packet, UI.current, false);
			
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
			// PacketMgr.Master.toCanvas(packet);
			PacketMgr.Master.toCanvasPage(packet, UI.current, false);
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
		// 지우개
		erasermode : function(packet) {
			var mode = packet.eraserMode;
			var pageId = packet.page;
			
			if (mode == 1) {
				PacketMgr.Master.toCanvas(packet, false);
			} else {
				PacketMgr.Master.toCanvasPage(packet, pageId, false);				
				// map data remove All
				PacketMgr.removePacketPage(pageId);
			}
			
			Ctrl.Text.removeAll(true);
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
		
		vshare : function(packet){
			// type-0: add, 1-update, 2-remove
			Ctrl.VShare.receive(packet);			
		},		
		
		vcam : function(packet){			
			VideoCtrl.receive(packet);
		},
		
		textbox : function(packet){
			PacketMgr.Master.toCanvasPage(packet, UI.current, false);			
			/**
				if(Ctrl.Text.redrawSkipId != "") return;
			***/			
			Ctrl.Text.receive(packet);
		},
		

		poll : function(packet) {
			console.log(JSON.stringify(packet));
			PollCtrl.receive(packet);
		},
		
		getdata : function(packet){
// getdata 패킷을 받았지만 나는 마스터가 아니라면 pass 시킨다.
// Utils.log("getdata : PacketMgr.isMC : " + PacketMgr.isMC + ", PacketMgr.isInitFChange : " + PacketMgr.isInitFChange);
			if(PacketMgr.isMC){
				PacketMgr.Master.syncStatus("getdata");
			}
		},
		
		pdf : function(packet){
			PDFViewer.draw(packet);
		},
		
		kickuser : function(packet){			
			RoomSvr.kickuser(packet.roomid, packet.userno, packet.userid, packet.username, packet.type);		
		},
		
		call : function(packet){
			// 모든 유저 호출 
			Ctrl.BroadCast[packet.cmd](packet);			
		},

		sync : function(packet){
			// 화면 동기화 용으로 사용하며 fName과 함수명을 매핑해서 호출하는 방식으로 사용한다. fName을 넘겨서 코드 메시지의 함수명을 찾아서 호출한다.
			// var packet = {"cmd":"sync", "method":fName};
			var method = packet.method;
			if(method != null && method != ""){
				var fName = _code(method);
				if(Ctrl.Sync[fName] != null && typeof(Ctrl.Sync[fName]) == "function"){
					Ctrl.Sync[fName].call();
				}
			}
		},
		
		video_options : function(packet){
			VideoCtrl.Command[packet.cmd](packet);			
		},
		
		video_screen : function(packet){
			// {"cmd":"video_screen","action":action,"userno":""+userNo+""};
			VideoCtrl.Command[packet.cmd](packet);
		},
		
		video_group : function(packet){
			// {"cmd":"video_group","roomid":roomId};
			VideoCtrl.Command[packet.cmd](packet);
		},
		
		video_noti : function(packet){
			// var packet = {"cmd":"video_noti","action":""+action+"", "userno":""+userNo+""};
			VideoCtrl.Command[packet.cmd](packet);
		}
	},

	// 컨트롤에서 Call되는 함수들
	Master : {
		// history 때문에 펜세팅 깨질수 있기 때문에 임시 저장 
		masterPensetting : null,
		curPensetting : null,
		curPointer : null,
		curRoomInfo : null,
		syncStatus : function(from){			
			// 현재 마스터의 펜세팅 적용
			// 문서가 반드시 존재하는 회의만 판서관련 패킷을 sync 한다.
			Ctrl.callCurPensetting(true);
			
			// 현재 마스터가 뷰 모드 보고있으면, 뷰 모드로 전환해준
			if(Ctrl.isHand() ) PacketMgr.Master.changeMode("view");
			
			// 마지막 줌 패킷
			var percent = parseInt($("#zoomval").val(), 10);
			if(percent > 100){
				if(from == "page"){
					this.zoomSync(percent, "1");
				}else{
					this.zoomCurrent(percent, "1");	
				}					
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
		
		syncPensetting : function(pageId) {			
// Utils.log("PacketMgr.Master.curPensetting : " + JSON.stringify(PacketMgr.Master.curPensetting) );
			if(PacketMgr.Master.curPensetting == null) {
				// 마스터인 경우, 현재 팬세팅이 없으면 기본 팬세팅으로 해준다.
				return;
			}
			
			if (typeof (pageId) != "undefined" && pageId != null) {
				PacketMgr.Master.toCanvasPage(PacketMgr.Master.curPensetting, pageId, false);
				var menuSelect = PacketMgr.Master.curPensetting.menuselect;
				if(menuSelect == 6) PacketMgr.Master.toCanvasPage(PacketMgr.Master.curPointer, pageId, false);				
			} else {
				PacketMgr.Master.toCanvas(PacketMgr.Master.curPensetting, false);
				var menuSelect = PacketMgr.Master.curPensetting.menuselect;
				if(menuSelect == 6) PacketMgr.Master.toCanvas(PacketMgr.Master.curPointer, false);				
			}
			
			// 현재 뷰 모드라면 펜세팅후 뷰모드로 맞춰준다.
			
			if(!PacketMgr.isMC || Ctrl.isHand() ){
				PacketMgr.Master.changeMode("view");
			}			
		},
		
		syncPensettingForce : function(pensetting){
			PacketMgr.Master.curPensetting = pensetting;
			// PacketMgr.Master.toCanvas(PacketMgr.Master.curPensetting);			
			PacketMgr.Master.toCanvasPage(PacketMgr.Master.curPensetting, UI.current, false);
			
			var menuSelect = PacketMgr.Master.curPensetting.menuselect;
			if(menuSelect == 6 && PacketMgr.Master.curPointer) {
				// PacketMgr.Master.toCanvas(PacketMgr.Master.curPointer);
				PacketMgr.Master.toCanvasPage(PacketMgr.Master.curPointer, UI.current, false);
			}
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
						
			var packet = {"chatid":""+uuidTxt+"","cmd":"chat","from":""+ PacketMgr.userno +"","text":""+msg+"","time":""+dateTime+"","to":""+target+"","type":"room"};

			// 진행자가 아니어도 채팅 보낼수 있게 한다.
			this.toRoomForce(packet);
		},

		// 펜세팅, 투명펜, 지우게, 레이저포인터 기능 포함
		pensetting : function(isSend, menu, width, r, g, b, alpha, stamp_kind, line_cap, fill) {			
			var defaultOpts = {
				"menuselect" : 4,
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

			this.toCanvas(PacketMgr.Master.curPensetting, false);

			if(isSend) this.toRoom(PacketMgr.Master.curPensetting);
		},
		
		laserpointer : function(type, r, g, b, isSend){
			var defaultOpts = {"cmd":"laserpointer","page":UI.current,"type":0,"green":0,"red":255,"blue":0};
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
		
		redo : function(pageId) {
			var packet = {"cmd":"redo","page":UI.current};
			this.toCanvasPage(packet, pageId, false);
			this.toRoom(packet);
		},

		undo : function(pageId) {
			var packet = {"cmd":"undo","page":UI.current};
			this.toCanvasPage(packet, pageId, false);
			this.toRoom(packet);
		},

		eraserMode : function(mode, pageId) {
			// { "cmd" : "erasermode", "eraserMode" : 2, "currentPage" : 1 }
			var packet = {"cmd":"erasermode","eraserMode":mode,"page":pageId};
			if (mode == 1) {
				this.toCanvas(packet);
			} else {
				this.toCanvasPage(packet, pageId, false);
			}
			
			this.toRoom(packet);
			
			Ctrl.Text.removeAll(true);
			
			Ctrl.Uploader.checkSaveTimer("erasermode", null);
		},

		// --> draw.js에서 발생한 마우스 궤적 이동 이벤트 (**** 개인모드일때는 패킷을 보내면 안된다.)
		draw : function(cmd, menu, pageId, x, y, fill) {
			// draw cmd : menu4, began, moved, ended
			// eraser cmd : menu5, eraserbegan, erasermoved, eraserended
			// pointer cmd : menu6, cursor, 커서아웃시 -9999, -9999
			var packet = {"cmd":cmd,"menuselect":menu,"locationx":x,"locationy":y,"page":UI.current};
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
			// var board = UI.skboards[UI.current - 1];
			var board = UI.getBoard();
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
			var packet = {"cmd":"zoom","scale":scale,"settled":settled,"x":zoomX,"y":zoomY,"page":UI.current};
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
			var canvas = document.getElementById("sketch" + UI.current);
			var w = $(canvas).width();
			var h = $(canvas).height();
			
			var dx = $(canvas).width() / 2;
			var dy = $(canvas).height() / 2;
			
			var scale = 1;
			var zoomX = 0;
			var zoomY = 0;
			
			// var board = UI.skboards[UI.current - 1];
			var board = UI.getBoard();
			if(board != null){
				scale = board.getZoomScale() > 1 ? board.getZoomScale() : (parseInt(percent) * 0.01);
				var zoomInfo = board.getZoom();
				zoomX = zoomInfo != null && zoomInfo[0] > 0 ? zoomInfo[0] : (PDFViewer.getFixedX(w, h, dx) * scale);
				zoomY = zoomInfo != null && zoomInfo[1] > 0 ? zoomInfo[1] : (PDFViewer.getFixedY(w, h, dy) * scale);
			}else{
				zoomX = (PDFViewer.getFixedX(w, h, dx) * scale);
				zoomY = (PDFViewer.getFixedY(w, h, dy) * scale);				
			}
			
			var packet = {"cmd":"zoom","scale":scale,"settled":settled,"x":zoomX,"y":zoomY,"page":UI.current};
			// 룸서버에 호출
 			this.toRoom(packet);
		},
		
		zoomSync : function(percent, settled){
			var canvas = document.getElementById("sketch" + UI.current);
			var w = $(canvas).width();
			var h = $(canvas).height();
			
			var dx = $(canvas).width() / 2;
			var dy = $(canvas).height() / 2;
			
			var scale = 1;
			var zoomX = 0;
			var zoomY = 0;
			
			// var board = UI.skboards[UI.current - 1];
			var board = UI.getBoard();
			if(board != null){
				// scale = board.getZoomScale() > 1 ? board.getZoomScale() : (parseInt(percent) * 0.01);
				// var zoomInfo = board.getZoom();
				scale = UI.Page.beforeScale;
				var zoomInfo = (percent > 100) ? UI.Page.beforeZoomInfo : null;
				
				zoomX = zoomInfo != null && zoomInfo[0] > 0 ? zoomInfo[0] : (PDFViewer.getFixedX(w, h, dx) * scale);
				zoomY = zoomInfo != null && zoomInfo[1] > 0 ? zoomInfo[1] : (PDFViewer.getFixedY(w, h, dy) * scale);
			}else{
				zoomX = (PDFViewer.getFixedX(w, h, dx) * scale);
				zoomY = (PDFViewer.getFixedY(w, h, dy) * scale);				
			}
			
			var packet = {"cmd":"zoom","scale":scale,"settled":settled,"x":zoomX,"y":zoomY,"page":UI.current};
			// 룸서버에 호출
 			this.toRoom(packet);
 			
 			// 본인 zoom 처리
 			PacketMgr.Command.zoom(packet); 			
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
		zoomHandle : function(mode, zoomScale, pageId, hStartX, hStartY, pointX, pointY, pointFixedX, pointFixedY, startZoomX, startZoomY){
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
			
			this.zoomHandleCenter(zoomScale, settled, centerX, centerY, pageId);
		},
		
		zoomHandleCenter : function(zoomScale, settled, centerX, centerY, pageId){
			var packet = {"cmd":"zoom","scale":zoomScale,"settled":settled,"x":centerX,"y":centerY,"page":UI.current};
			UI.zoomEnd(pageId, packet);

			// 마스터만 패킷 보낸다.
			this.toRoom(packet);
		},		
		
		masterWithDraw : function(){			
			var packet = {"cmd":"masterwithdraw"};
			
			// 진행자가 다른사람일수도 있으므로 강제로 브로드케스팅한다.
			this.toRoomForce(packet);
			
			PacketMgr.Command.masterwithdraw(packet);
		},
		
		masterChange : function(userid) {
			if(PacketMgr.isGuest || (!PacketMgr.isAllowMaster && !PacketMgr.isCreator)) return;
			
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
		
		updateRoomInfo : function(authType, chatOpt, cmtOpt, expOpt, title, passwd, openFlag, content, vCamOpt, vShareOpt){
			var curRoomInfo = PacketMgr.Master.curRoomInfo;
			
			// 채팅
			authType = (authType != null) ? authType : (curRoomInfo != null ) ? curRoomInfo.authType : "1";
			chatOpt = (chatOpt != null) ? chatOpt : (curRoomInfo != null ) ? curRoomInfo.chatopt : "1";
			cmtOpt = (cmtOpt != null) ? cmtOpt : (curRoomInfo != null ) ? curRoomInfo.cmtopt : "1";
			expOpt = (expOpt != null) ? expOpt : (curRoomInfo != null ) ? curRoomInfo.expopt : "1";
			title = (title != null) ? title : (curRoomInfo != null ) ? curRoomInfo.title : "";
			passwd = (passwd) != null ? passwd : (curRoomInfo != null ) ? curRoomInfo.passwd : "";
			
			openFlag = (openFlag) != null ? openFlag : (curRoomInfo != null ) ? curRoomInfo.openFlag : "1";
			content = (content) != null ? content : (curRoomInfo != null ) ? curRoomInfo.content : "";
			
			vCamOpt = (vCamOpt) != null ? vCamOpt : (curRoomInfo != null ) ? curRoomInfo.vCamOpt : "0";
			vShareOpt = (vShareOpt) != null ? vShareOpt : (curRoomInfo != null ) ? curRoomInfo.vShareOpt : "0";
			
			// 패스워드는 재작업 필요			
			// creator가 보내는 패킷
			// var packet = {"attendeemic":""+attendeemic+"","chatopt":""+chatOpt+"","cmd":"updateroominfo","dcodeopt":""+dcodeOpt+"","passwd":""+passwd+"","secretfile":""+secretFile+"","title":""+title+"","voice":""+voice+""};
			var packet = {"cmd":"updateroominfo","authtype":""+authType+"","chatopt":""+chatOpt+"","cmtopt":""+cmtOpt+"","expopt":""+expOpt+"", "passwd":""+passwd+"","title":""+title+"","openflag":""+openFlag+"","content":""+content+"","vcamopt":""+vCamOpt+"","vshareopt":""+vShareOpt+""};
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
				
				// 마스터도 회의방 나가게 처리 - 밖에서 방 파괴하는게 있어서 receive받고 방을 지운다. 
				// PacketMgr.Command.masterdone(packet);
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
		
		img : function(url, canvas, seqno, scaleW, scaleH, x, y, mode, order, typeFlag, userNm, thumbnail, degree, fileData){			
			var fixedX = UI.getFixedX(canvas.width, canvas.height, x);
			var fixedY = UI.getFixedY(canvas.width, canvas.height, y);
			typeFlag = typeFlag || "0";
			userNm = userNm || "";
			thumbnail = thumbnail || "";
			degree = degree || "";
			fileData = fileData || "";
			
			scaleW = scaleW || 1;
			scaleH = scaleH || 1;
			
			var packet = {"cmd":"img","mode":mode,"seqno":""+seqno+"","posx":"" + fixedX + "","posy":"" + fixedY + "","scalew":"" + scaleW + "","scaleh":"" + scaleH + "","url": ""+url+ "","ord":"" + order + "","typeflag": typeFlag  + "","usernm":"" + userNm + "","thumbnail":"" + thumbnail + "", "degree":""+ degree +"", "filedata":""+fileData+""};
			this.toRoom(packet);
			
			// upload room represent image
			Ctrl.Uploader.checkSaveTimer("img", null);
		},
		
		background : function(bgImg, r, g, b){
			// bgImg -> 1,2,3,4
			var packet = {"cmd":"background","color_r":""+r+"","color_g":""+ g + "","color_b":"" + b + "","bgimg":bgImg,"page":UI.current};			
			this.toRoom(packet);
			
			Ctrl.Uploader.checkSaveTimer("background", "1000");			
		},

		textbox : function(id, type, txt, face, size, r, g, b, w, h, x, y, italic, bold){
			
			if(x == 0 && y == 0) return;
			
			var canvas = document.getElementById("sketch" + UI.current);
			var canvasWidth = $(canvas).width();
			var canvasHeight = $(canvas).height();
			var leftX = UI.getFixedX(canvasWidth, canvasHeight, x); 
			var topY = UI.getFixedY(canvasWidth, canvasHeight, y);		
			
			var packet = {"cmd":"textbox","type":""+type+"","id":""+id+"","text":""+txt+"","face":""+face+"","size":""+size+"","w":""+w+"","h":""+h+"","x":""+leftX+"","y":""+topY+"","r":""+r+"","g":""+g+"","b":""+b+"","bold":""+bold+"","italic":""+italic+"","page":UI.current};
			this.toRoom(packet);			
			/**
			if(type == "0"){
				// 추가시에는 canvas에도 적용해주고 화면 처리 
				PacketMgr.Command.textbox(packet, pageId);
			}else{
				// 수정과 삭제는 receive만 동작 
				Ctrl.Text.receive(packet);	
			}
			**/
			
			PacketMgr.Command.textbox(packet, UI.current);
			
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

			// upload room represent image
			Ctrl.Uploader.checkSaveTimer("pdf", null);
			
			return packet;
		},
		
		kickUser : function(roomId, userNo, userId, userName){			
			var packet = {"cmd":"kickuser","type":"1","roomid":""+roomId+"","userno":""+userNo+"","userid":""+userId+"","username":""+userName+""};			
			this.toRoomCreator(packet);
			
			//-- dead session을 지워야 할 경우도 있어서 receive 와 같은 동작을 보내는 사람도 시켜준다. 
			PacketMgr.Command.kickuser(packet);
		},
		// 메모와 기본 표준을 맞춘다.						   
		vShare : function(type, vsno, seqno, title, link, left, top, status, ord, time, userno){			
			vsno = vsno || '';
			seqno = seqno || '';
			status = status || '0';
			userid = PacketMgr.userid || '';
			userno = PacketMgr.userno || '';
			usernm = PacketMgr.usernm || '';
			datetime = Utils.getDate(new Date() ) || '';
			title = title || '';
			content = link || '';
			left = left || 0;
			top = top || 0;  
			ord = ord || 1;
			userno = userno || ''; 
			
			// var packet = {"cmd":"vshare","type":type,"seqno":""+seqno+"","userid":""+userid+"","userno":""+userno+"","usernm":""+usernm+"","datetime":""+datetime+"","title":""+title+"","content":""+link+"","x":""+left+"","y":""+top+"","color_r":""+r+"","color_g":""+g+"","color_b":""+b+"","fold":""+fold+"","ord":""+ord+""};
			var packet = {"cmd":"vshare","type":type,"vsno":""+vsno+"","seqno":""+seqno+"","datetime":""+datetime+"","title":""+title+"","content":""+link+"","x":""+left+"","y":""+top+"","status":""+status+"","ord":""+ord+"","time":""+time+"","userno":""+userno+""};			
			this.toRoom(packet);
		},
		
		vCam : function(action, userNo, userId){
			var packet = {"cmd":"vcam","action":action,"userno":""+userNo+"","userid":""+userId+""};
			this.toRoomForce(packet);
		},
		
		sync : function(fName){
			// 화면 동기화 용으로 사용하며 fName과 함수명을 매핑해서 호출하는 방식으로 사용한다. fName을 넘겨서 코드 메시지의 함수명을 찾아서 호출한다.
			// 룸간 전송시 사용한다.
			var packet = {"cmd":"sync","method": fName};
			this.toRoom(packet);
		},
		
		page : function(pageId, type, order){
			// type = 0(add), 1(change), 2(remove), 3(ordering)
			var packet = {"cmd":"page","pageid":pageId,"type":type,"order":order};
			this.toRoom(packet);
		},
		
		videoOptions : function(videoCtrl, soundOnly){
			var packet = {"cmd":"video_options","roomid":""+PacketMgr.roomid+"","videoctrl":videoCtrl,"soundonly":soundOnly};
			this.toRoomForce(packet);
		},
		
		videoScreen : function(action, userNo){
	 		var packet = {"cmd":"video_screen","roomid":""+PacketMgr.roomid+"","action":action,"userno":""+userNo+""};
			this.toRoom(packet);
		},
		
		videoNoti : function(action, from, to){
			// action = connect, disconnect, request
			var packet = {"cmd":"video_noti","action":""+action+"","from":""+from+"","to":""+to+""};
			this.toRoomForce(packet);
		},
		
		// canvas 동기화
		toCanvas : function(packet, isHistory){
			/**
			var len =  UI.skboards != null ? UI.skboards.length : 0;
			for(var i = 0; i < len; i++){
				UI.skboards[i]["async"](packet);
			};
			**/			
			var keys = UI.boards.keys();			
			var len = keys == null ? 0 : keys.length;
			for(var i=0; i<len; i++){
				var idx = keys[i];
				var board = UI.boards.get(idx);
				board["async"](packet, isHistory);
			}
		},

		toCanvasPage : function(packet, pageId, isHistory) {
			/**
			var idx = pageNo - 1 < 0 ? 0 : pageNo - 1;			
// Utils.log("UI.skboards[idx] : " + UI.skboards[idx] + ", packet : " + packet + ", idx : " + idx);			
			if(UI.skboards[idx] != null && packet != null){
				UI.skboards[idx]["async"](packet);	
			}			
			***/
			var board = UI.getBoard(pageId);
			if(board) board["async"](packet, isHistory);
		},
		
		toCanvasMode : function(mode) {
			/**
			if(UI.skboards != null && mode != null && mode != ""){
				var len = UI.skboards.length;
				for (var i = 0; i < len; i++) {
					UI.skboards[i]["setMode"](mode);
				}	
			}
			**/
			if(UI.boards != null && mode != null && mode != ""){
				var keys = UI.boards.keys();
				var len = keys == null ? 0 : keys.length;
				for(var i=0; i<len; i++){
					var idx = keys[i];
					var board = UI.boards.get(idx);
					board["setMode"](mode);
				}
			}
		},

		toCanvasModePage : function(mode, pageId) {
			/**
			var idx = pageNo - 1 < 0 ? 0 : pageNo - 1;			
// Utils.log("UI.skboards[idx] : " + UI.skboards[idx] + ", idx : " + idx + ", mode : " + mode);
			if(UI.skboards[idx] != null && mode != null && mode != ""){
				UI.skboards[idx]["setMode"](mode);	
			}
			**/			
			if(UI.boards != null && mode != null && mode != ""){
				var board = UI.boards.get(pageId);
				board["setMode"](mode);				
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
					PacketMgr._setPacketMap(packet, UI.current);
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
		},
		
		toBroadCastCreator : function(mode, roomId, packet){			// isParentCreator
			if(PacketMgr.isParentCreator){				
				RoomSvr.sendbroadcast(mode, roomId, JSON.stringify(packet));
			}
		},

		toBroadCastForce : function(mode, roomId, packet){			
			RoomSvr.sendbroadcast(mode, roomId, JSON.stringify(packet));			
		},
		
		// 전체 방에 전송하는 메시징처리
		BroadCast : {
			sendMsg : function(mode, roomId, msg, drCode, history){
				var packet = {"cmd":"broadcastmsg","type":"bcm01","msg":""+msg+"","drcode":""+drCode+"","history":""+history+""};
				PacketMgr.Master.toBroadCast(mode, roomId, packet);
			},			
			chat : function(mode, roomId, msg, target, targetNm){			
				var uuid = Utils.createUUID();
				var uuidTxt = uuid.substring(0, 8);
				var dateTime = Utils.getDate(new Date() ); 
				              
				if(target == "") targetNm = "";
							
				var packet = {"chatid":""+uuidTxt+"","cmd":"chat","from":""+ PacketMgr.userno +"","text":""+msg+"","time":""+dateTime+"","to":""+target+"","type":"class"};

				// 진행자가 아니어도 채팅 보낼수 있게 한다.
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			},
				
			call : function(mode, roomId, drCode){
				// 학생 부르기 기능 
				var packet = {"cmd":"call","drcode":""+drCode+"","roomid":""+ roomId +"","userno":""+RoomSvr.userno+"","usernm":""+RoomSvr.usernm+""};
				PacketMgr.Master.toBroadCastCreator(mode, roomId, packet);
			},
			
			sync : function(mode, roomId, fName){
				// 화면 동기화 용으로 사용하며 fName과 함수명을 매핑해서 호출하는 방식으로 사용한다. fName을 넘겨서 코드 메시지의 함수명을 찾아서 호출한다.
				var packet = {"cmd":"sync", "method": fName};
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			},
			
			videoOptions : function(mode, roomId, videoCtrl, soundOnly){
				var idx = roomId.indexOf("_");				
				var bRoomId =  idx > -1 ? roomId.substring(0, idx) : roomId;
				var packet = {"cmd":"video_options","roomid":""+bRoomId+"","videoctrl":videoCtrl,"soundonly":soundOnly};
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			},			 
			 
			videoScreen : function(mode, roomId, action, userNo){
				var idx = roomId.indexOf("_");				
				var bRoomId =  idx > -1 ? roomId.substring(0, idx) : roomId;				
		 		var packet = {"cmd":"video_screen","roomid":""+bRoomId+"","action":action,"userno":""+userNo+""};
		 		PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			},
			
			videoGroup : function(mode, roomId, separate){
				var packet = {"cmd":"video_group","roomid":""+roomId+"","separate":separate};
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			},
			
			videoNoti : function(mode, roomId, action, from, to){
				// action = connect, disconnect, request
				var packet = {"cmd":"video_noti","action":""+action+"", "from":""+from+"", "to":""+to+""};
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			}
		}
	},
	
	destroy : function(){ 
		try{
			this.pdfs = null;
			this.queue = null;
			this.penIdxList = null;
			this.penMap = null;
			this.pageMap = null;
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
	penIdx : 1,
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
	isOnline : true,
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
				var title = _msg("noti");
						
				var display = (type == "creator") ? "inline-block" : "none";
				var confirmMsg = (type == "creator") ? _msg("msg.exit.admin") : _msg("msg.exit.user");
				var modalHtml = "<div id=\"exitModal\" class=\"popup_dimd on\">\
									<div class=\"popup_box\" style=\"display: block; \">\
										<span class=\"popup_header\">\
											<span class=\"pop_tit\">"+title+"</span>\
											<a href=\"javascript:Ctrl.Modal.hideExit();\"></a>\
										</span>\
										<div class=\"popup_body\">\
											<span class=\"popup_msg2\">"+confirmMsg+"</span>\
											<div class=\"popbtn_box\">\
												<a href=\"javascript:Ctrl.exit(false);\" class=\"btn_submit\">"+_msg("btn.exit")+"</a>\
												<a id=\"removeMeetingBtn\" href=\"javascript:Ctrl.exit(true);\" class=\"btn_submit\" style=\"display:"+display+";\">"+_msg("btn.end")+"</a>\
												<a href=\"javascript:Ctrl.Modal.hideExit();\" class=\"btn_cancel\">"+_msg("klounge.cancel")+"</a>\
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
			// if(PacketMgr.userid == PacketMgr.creatorid){
			
			if(PacketMgr.isParentCreator){			
				var titleModal = document.getElementById("titleModal");
				if(titleModal){				 
					$(titleModal).show();					
				}else{
					var tcount = RoomSvr.roomtitle.length;					
					var html = "<div id=\"titleModal\" class=\"popup_dimd on\">\
									<div class=\"popup_box\">\
										<span class=\"popup_header\">\
											<span class=\"pop_tit\">"+_msg("title")+"</span>\
											<a href=\"javascript:Ctrl.Modal.hideTitle();\"></a>\
										</span>\
										<div class=\"popup_body\">\
											<span class=\"popinput_byte\"><font id=\"ck_byte\">"+tcount+"/60</font></span>\
											<textarea id=\"popup_title_txt\" class=\"popinput_title\">"+ RoomSvr.roomtitle +"</textarea>\
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
			
			var tcount = title.length;			
			if(tcount > 60){
				Ctrl.Msg.show(_msg("validation.title"));
				return;
			}			
			
			RoomSvr.roomtitle = title;
			
			Ctrl.Room.update('modal');
			
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

			// if(!Utils.mobile()) Avgrund.show("#inviteModal");
			// Ctrl.avgrund(true, "inviteModal");
		},
		
		hideInvite : function(){
			Ctrl.Modal.hide('inviteModal');
			// if(!Utils.mobile()) Avgrund.hide("#inviteModal");
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
			// if(!Utils.mobile()) Avgrund.show("#loginModal");
			// Ctrl.avgrund(true, "loginModal");
		},
		
		confirm : function(msg, callback, cancelCallback){
			if($("#cfmModal").get(0) != null){
				this.destroyConfirm();				
			}
			
			var modalHtml = "<div id=\"cfmModal\" class=\"popup_dimd on\">\
								<div class=\"popup_box\" style=\"display: block;\">\
									<span class=\"popup_header\">\
										<span class=\"pop_tit\">"+_msg("noti")+"</span>\
										<a href=\"javascript:Ctrl.Modal.destroyConfirm();\"></a>\
									</span>\
									<div class=\"popup_body\">\
										<span class=\"popup_msg2\">"+msg+"</span>\
										<div class=\"popbtn_box\">\
											<a href=\"javascript:void(0);\" class=\"btn_submit\">"+_msg("btn.confirm")+"</a>\
											<a href=\"javascript:void(0);\" class=\"btn_cancel\">"+_msg("klounge.cancel")+"</a>\
										</div>\
									</div>\
								</div>\
							</div>";
										
			$(document.body).append(modalHtml);
			
			$(".btn_submit", "#cfmModal").click(function(){
				if(callback) callback();
				
				Ctrl.Modal.destroyConfirm();				
			});
			
			$(".btn_cancel", "#cfmModal").click(function(){
				if(cancelCallback) cancelCallback();
				
				Ctrl.Modal.destroyConfirm();
			});
		},
		
		destroyConfirm : function(){
			$(".btn_submit", "#cfmModal").unbind("click");
			$(".btn_cancel", "#cfmModal").unbind("click");
			$("#cfmModal").remove();
			
//			if(!Utils.mobile()) Avgrund.hide("#cfmModal");
			// Ctrl.avgrund(false, "cfmModal");
		},
		
		confirmCall : function(msg, callback){
			if($("#cfmModal").get(0) != null){
				this.destroyConfirmCall();				
			}
			
			var modalHtml = "<div id=\"cfmModal\" class=\"popup_dimd on\">\
								<div class=\"popup_box\" style=\"display: block;\">\
									<span class=\"popup_header\">\
										<span class=\"pop_tit\">"+_msg("noti")+"</span>\
										<a href=\"javascript:Ctrl.Modal.destroyConfirm();\"></a>\
									</span>\
									<div class=\"popup_body\">\
										<p class=\"popup_callAert\">\
											<span class=\"big\">"+msg+"</span>\
											<a href=\"javascript:void(0);\" class=\"btn_confirm\">"+_msg("klounge.teacher.call.confirm")+"</a>\
											<a href=\"javascript:Ctrl.Modal.destroyConfirm();\">"+_msg("klounge.teacher.call.cancel")+"</a>\
										</p>\
									</div>\
								</div>\
							</div>";
										
			$(document.body).append(modalHtml);			
			
			// if(!Utils.mobile()) Avgrund.show("#cfmModal");
			// Ctrl.avgrund(true, "cfmModal");
			
			$(".btn_confirm", "#cfmModal").click(function(){
				if(callback) callback();
				Ctrl.Modal.destroyConfirm();
			});
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
				var modalHtml = "<div id=\"passwdModal\" class=\"popup_dimd dimd_password on\">\
									<div class=\"dimd_header\"></div>\
									<div class=\"popup_box\" style=\"display: block;\">\
										<span class=\"popup_header\">\
											<span class=\"pop_tit\">"+_msg('m.password.title')+"</span>\
										</span>\
										<div class=\"popup_body\">\
											<div id=\"password_info\">\
												<span class=\"popup_msg1\"><b>"+_msg('m.password.msg.1')+"</b><br />"+_msg('m.password.msg.2')+"</span>\
												<span id=\"ck_byte3\" class=\"popinput_byte\"><font>0</font> / 6</span>\
												<input id=\"room_password\" type=\"password\" class=\"popinput_name\" placeholder=\""+_msg('m.password.placeholder')+"\" maxlength=\"8\" onkeyup=\"Utils.textCutProcess(this, 8, '', 'ck_byte3', 'room_password');\" />\
												<div class=\"popbtn_box\">\
													<a href=\"javascript:void(0);\" class=\"btn_submit\">"+_msg('m.password.btn.enter')+"</a>\
													<a href=\"javascript:location.href=Utils.addContext('main');\" class=\"btn_cancel\">"+_msg('m.password.btn.home')+"</a>\
												</div>\
											</div>\
										</div>\
									</div>\
								</div>";
								
				$(document.body).append(modalHtml);
				
				$("#room_password").focus();
				
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
											<a href=\"javascript:Ctrl.exit(false);\" class=\"btn_cancel\">"+_msg("m.network.btn.2") +"</a>\
											<a href=\"javascript:location.reload();\" class=\"btn_submit\">"+_msg("m.network.btn.1") +"</a>\
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
		limitCnt : 8,
		readyCmdList : ["began", "eraserbegan"],	// canvas 저장 요청 command
		saveCmdList : ["ended", "eraserended", "pdf", "img", "textbox", "erasermode", "background"],	// canvas 저장 요청 command
		isWait : false,
		timer : null,
		auth : false,		
		loopTime : "3000",
		progressing : false,
		
		init : function(){
			// Ctrl.Uploader.init();
			Ctrl.Uploader.limitCnt = CanvasApp.info.imgmaxcnt;
			Ctrl.Uploader.setProgress();
		},
		
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
			// UI.skboards[0].save();
			var board = UI.getBoard();
			board.save();			
			
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
				pageid : UI.current,
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
					PacketMgr.Master.sync("RELOAD_ROOM_THUMB");	
					
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
				}
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
				pageid : UI.current,
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
				Ctrl.Uploader.clear(id);
				return;
			};
			if(!this.checkImgExt()){
				Ctrl.Msg.show(_msg("check.file.img"));
				Ctrl.Uploader.clear(id);
				return;
			}
			
			var count = Ctrl.BGImg.getCnt();
			if(count > this.limitCnt){
				Ctrl.Msg.show(_msg("check.file.count"));
				Ctrl.Uploader.clear(id);
				return;
			}
			
			this.upload(e, id, uploadFile, RoomSvr.roomid, '0', function(data){
				// upload 
				Ctrl.BGImg.init(data);
				
				// 이미지 업로드 후 손 모드
				Ctrl.toggleRC(0, -1, false);
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
		
		clear : function(id){
			if(Utils.browser("msie")){
			    $("#" + id).replaceWith( $("#" + id).clone(true) );	
			} else {
			    $("#" + $(this).attr("id")).val('');
			}
		},
		
		uploadPdf : function(e, id){ 
			Ctrl.Uploader.id_pdf = id;
			
			var files = e.target.files || e.dataTransfer.files;
			var uploadFile = (files && files.length > 0) ? files[0] : null; 
			if(uploadFile == null){
				Ctrl.Msg.show(_msg("file.not.found"));
				Ctrl.Uploader.clear(id);
				return;
			};
			
			if(!this.checkPdfExt()){
				Ctrl.Msg.show(_msg("check.file.pdf"));
				Ctrl.Uploader.clear(id);
				return;
			}
			
			if(this.progressing || PDFViewer.initializing){
				Ctrl.Msg.show(_msg("check.file.pdf.init"));
				Ctrl.Uploader.clear(id);
				return;
			}
			
			this.progressing = true;
			PDFViewer.blocked = false;
			this.upload(e, id, uploadFile, RoomSvr.roomid, 'p', function(data){
				Ctrl.Uploader.progressing = false;
				PDFViewer.init(data);
				
				// 손모드 적용
				Ctrl.toggleRC(0, -1, false);
			})
		},
		
		upload : function(e, id, uploadFile, roomId, typeFlag, callback){			
			var uploadURL = Utils.addContext(_url("upload"));			
			var formData = new FormData();
			formData.append(id, uploadFile);
			formData.append('roomid', roomId);
			formData.append('typeflag', typeFlag);
			formData.append('pageid', UI.current);
			
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
		        	/**
	        		if(Utils.browser("msie")){
	        			$("#" + id).replaceWith( $("#" + id).clone(true) );	        			
	        		}else{	        			
	        			$("#" + id).val("");
	        		}
	        		**/
	        		Ctrl.Uploader.clear(id);
	        		
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
			// $(html).insertBefore( $("#hmenubar") );
			// 2016.11.21 
			$(html).insertBefore( $("#confContainer"));			
			
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
			// Utils.log(isForce + " " + PacketMgr.isGuest + " " + PacketMgr.isAllowMaster);
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
	
	/**
	 *  다른방에서 전체 혹은 개인 호출시 사용한다.
	 */
	BroadCast : {
		isOpened : false,
		receive : function(packet){
			var cmd = packet.cmd;
			var type = packet.type;
			var msg = packet.msg;
			var drcode = packet.drcode;
			var history = packet.history;
			
			// cmd, type, msg, drcode, history
			if(type == "crm01"){
				
			}else if(type == "crm02"){
				
			}
			
			var title = "";
			
			Ctrl.Noti.show(title, msg);		
		},
		call : function(packet){
			var drCode = packet.drcode;		
			var userNo = packet.userno;
			var userNm = packet.usernm;
			if(parseInt(PacketMgr.code) != parseInt(drCode)){
				var msg = userNm + " " + _msg("klounge.teacher.call");
				Ctrl.Modal.confirmCall(msg, function(){
					location.href = Utils.addContext("room/" + drCode);
				});								
			}
		},
		
		chat : function(packet){
			// 
			 
		}
	},	
	
	// 참여자 및 권한 관련 하단 메시지 뷰  
	Noti : {		
		isProgress : false,		
		show : function(title, msg, sound){			
			if(msg == "") return;
			// 노티 중복체크 필요 			
			this.isProgress = true;
			
			$("#notiContent").html(msg);			
			$("#notiWrap").show("slow", function(){				
				setTimeout(Ctrl.Noti.hide, "2000");
			});
			 
			// 노티 사운드 추가
			if(sound){
				var soundPath = Utils.getSound("1"); 
			    var snd = new Audio(soundPath); // buffers automatically when created
	            snd.play();	
			}
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
	
	ProgressLoader : {
		max : 0,
		current : 0,
		topTxt : null,
		percentTxt : null,
		show : function(fullWidth){
			this.max = fullWidth;
			
			$("#progLoader").show();	
			
			this.topTxt = $("#progTopText");
			this.progPercentTxt = $("#progPercentTxt");			
		},
		hide : function(){
			$("#progLoader").hide();
			this.max = 0;
			this.current = 0;
		},
		update : function(val){
			if(val == this.current) {				
				if(val == 100) Ctrl.ProgressLoader.progPercentTxt.html( val );				
				return;
			}

			Ctrl.ProgressLoader.current = val;
			
			// 100프로는 max값 ex:) 440이라면 1%는 4.4 px
			var realPx = Ctrl.ProgressLoader.max * 0.01 * val; 

			Ctrl.ProgressLoader.topTxt.width( realPx );
			Ctrl.ProgressLoader.progPercentTxt.html( val );			
		}		
	},
	
	Loader : {		
		show : function(){
			$("#loading").show();			
		},
		showF : function(){
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
		id : "chat_wrapper",
		init : function(){			
			$("#" + this.id).draggable({
				handle: ".chat_header",
				containment: $('#docWrapper'),
				start : function(e){					
				},
				drag : function(e, ui){					
				},
				stop : function(e, ui){
				}
			});
		},
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
			
			$("#chatCtl").removeClass("checked");
			Ctrl.Chat.chatBadgeCnt = 0;
			$("#chat_badge").hide();
			
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
						</div>";		
							
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
				Ctrl.Noti.show(notiTitle, notiContent, true);
				Ctrl.Chat.chatBadgeCnt++;
				$(".btn_chat").addClass("checked");
				$("#chat_badge").show();
				$("#chat_badge").text(Ctrl.Chat.chatBadgeCnt);
			}	
		},
		setEvent : function(){
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
		destroy : function(){
			// $("#" + this.id).draggable("destroy");
			
			console.log("222222222222");
			
			$(".chat_close", "#chat_wrapper").unbind("click");
			
			$("#sendchat").unbind("click");
			
			$("#chatmsg").unbind("keydown");
			
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
					usernm : Utils.Local.get("guest") || "",
					usertype : "0",
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
		
		_add : function(userId, userName, userNo, isGuest, thumbnail, userType){				
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
					usertype : userType,
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
			
			var masterChange = function(type, userId){
				if(Ctrl._checkWaiting() ){ // 누가 드로잉중이라 기달려야 하는 상황 이라면?
					var masterUserInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
					var authContent = masterUserInfo.usernm + _msg("noti.leading.host");
					Ctrl.Msg.show(authContent);	
				}else{
					if(type != null && type == "creator"){
						PacketMgr.Master.masterWithDraw();
					}else{
						PacketMgr.Master.masterChange(userId);
					}
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
						// PacketMgr.Master.masterWithDraw(userId);						
						masterChange("creator", userId);
					});
					
				}else{
					// PacketMgr.Master.masterWithDraw(PacketMgr.userid);
					masterChange("creator", PacketMgr.userid);
				}
				
				
			}else if(iconNm == "get_authority"){		
				// if(!confirm(_msg("confirm.allow.auth"))) return;
				
				if(isConfirm){
					Ctrl.Modal.confirm(_msg("confirm.allow.auth"), function(){
						var userId = $(thisNode.parentNode).attr("userid");
						// PacketMgr.Master.masterChange(userId);
						masterChange("user", userId);
					});	
				}else{
					// PacketMgr.Master.masterChange(PacketMgr.userid);
					masterChange("user", PacketMgr.userid);
				}				
				
			}else if(iconNm == "send_authority"){
				// if(!confirm(_msg("confirm.allow.auth"))) return;
				
				if(isConfirm){
					Ctrl.Modal.confirm(_msg("confirm.allow.auth"), function(){
						var userId = $(thisNode.parentNode).attr("userid");
						// PacketMgr.Master.masterChange(userId);
						masterChange("user", userId);
					});
				}else{
					// PacketMgr.Master.masterChange(PacketMgr.userid);
					masterChange("user", PacketMgr.userid);
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
				masterLayer.className = "userinfo";
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
					// $(this).attr("class", "user_box_chairman userinfo");
					$(this).attr("class", "userinfo");
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
		
		newUser : function(userId, userName, userNo, isGuest, thumbnail, userType, isVideoAllow, fullScreenUserId){
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
				var status = PacketMgr.masterid == userId ? _msg("progressing"): isGuest == "1" ? _msg("attending") + "(Guest)" : _msg("attending");
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
				
				// var divClass = userFlag == "1" ? "user_box_opener" : userFlag == "2" ? "user_box_chairman" : "user_box";
				// var divClass = userFlag == "1" ? "" : userFlag == "2" ? "" : "";
				var subLayer = userFlag == "1" ? "opener" : userFlag == "2" ? "chairman" : "";
				var wrapperId = userFlag == "1" ? "user_creator" : userFlag == "2" ? "user_master" : "";
				
				var kickLayer = PacketMgr.isCreator ? "<span class='kick' onclick='Ctrl.Member.kick(this);'></span>" : "";
				
				if(userId == userNo) userName += _msg("guest"); 
					
				var html = "<div id=\""+wrapperId+"\" class=\"userinfo\" userno=\""+userNo+"\" userid=\""+ userId+"\">\
								<img class=\"user_photo\" src=\""+profileUrl+"\" />\
								<span class=\"user_name\">"+userName+"</span>\
								<span class=\""+authCls+"\" onclick=\"Ctrl.Member.auth(this);\"></span>\
								" + kickLayer + "\
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
			
			/** 2016.06.20 타 유저가 들어왔을시 진행자가 sync 패킷을 전달해 준다. */
			if(userNo != RoomSvr.userno){				
				// 타인이 들어왔을때 비디오 share sync를 맞춰준다.
				Ctrl.VShare.sendMCPlayerStatus(userNo);
			}
			
			// member 추가 
			Ctrl.Member._add(userId, userName, userNo, isGuest, thumbnail, userType);
						
			// 체팅 selectbox 갱신
			Ctrl.Chat.addMember(userNo);

			
			Ctrl.Member.removeNotAttendee(userNo);	
			
			// fullscreen target
			VideoCtrl.fullScreenUserId = fullScreenUserId;
			
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
		
		removeNotAttendee : function(userNo){
			var isRemove = false;
			$(".userinfo", "#not_attendee_wrapper").each(function(){
				var targetUserNo = $(this).attr("userno");
				if(userNo == targetUserNo){
					$(this).remove();
					isRemove = true;
				}	
			});
			
			if(isRemove){
				if($("#abasenceCnt").get(0) != null){
					var abasenceCnt = parseInt($("#abasenceCnt").html());
					abasenceCnt -= 1;
					
					if(abasenceCnt < 0) abasenceCnt = 0;				
					$("#abasenceCnt").html(abasenceCnt);	
				}
			}			
		},
		
		refreshNotAttendee : function(){			
			$("#not_attendee_wrapper").html("");
			
			// var roomId
			var url = Utils.addContext(_url("notify.invite.not.attend"));
			var params = {
				roomid : PacketMgr.roomid
			};
			
			Utils.request(url, "json", params,  function(json){				
				var list = json != null ? json.list : null;
				var len = list == null ? 0 : list.length;
				
				var buf = new StringBuffer();
				for(var i=0; i<len; i++){
					var data = list[i];
					var userNo = data.userno;
					var userId = data.userid;
					var userNm = data.usernm;
					var thumbnail = data.thumbnail;
					
					if(userNo == PacketMgr.userno) continue;
				
					html =  "<div class=\"\" userno=\""+userNo+"\" userid=\""+userId+"\">\
									<img class=\"user_photo\" src=\""+thumbnail+"\"/>\
									<span class=\"user_name\">"+ userNm +"</span>\
									<span class=\"\" ></span>\
								</div>";
				
					buf.append(html);					
				}
				
				$("#not_attendee_wrapper").html(buf.toString() );
				
				if($("#abasenceCnt").get(0) != null){
					$("#abasenceCnt").html(buf.length());
				}
				
			}, function(e){
				console.log(e);
			});
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

			/*
			if(PollCtrl.isProgress && PollCtrl.progressPoll != null && userNo == PacketMgr.parentcreatorno)
				PollCtrl.Action.Attender.exitPoll(PollCtrl.progressPoll);
			*/
		},
		
		kick : function(thisNode){			
			var userNo = $(thisNode.parentNode).attr("userno");
			if(PacketMgr.isCreator && userNo != null){
				var targetUserInfo = Ctrl.Member.getUserOnline(userNo, "userno");	
				var msg = _msg("confirm.kick.user") + " " + "("+ targetUserInfo.usernm +")";				
				Ctrl.Modal.confirm(msg, function(){
					PacketMgr.Master.kickUser(PacketMgr.roomid, userNo, targetUserInfo.userid, targetUserInfo.usernm);
				});
			}
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
		commentBadgeCnt : 0,
		init : function(){
			this.setEvent();
			
			var plugin = CanvasApp.info.plugin;
			PacketMgr.isCommentPos = plugin.comment == "1" ? true : false;
			PacketMgr.isMemoPos = plugin.memo == "1" ? true : false;
			
			if(!PacketMgr.isCommentPos){
				var sketch = UI._getSketch(UI.current);
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
					/* 
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
					 */		
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
				pageid : Ctrl.Comment.current
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
				pageid : ++Ctrl.Comment.current
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
				
				Ctrl.Noti.show(notiTitle, notiContent, true);
				
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
			// $("#" + this.id).slideToggle("destroy");
			try{
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
				
				// $("#" + this.id).draggable("destroy"); /** 이동은 되데 저장은 하지 않게 수정 **/
				
			}catch(e){
				console.log("comment destroy : ", e);
			}
		}
	},
	
	Memo : {
		template : "",		
		list : [],
		// 1,2,3,4,5,6
		init : function(){
			$(".memo", $("#memoWrapper")).each(function(){				
				var bgColor = $(this).css("background");
				bgColor = bgColor.indexOf("rgba") > -1 ? bgColor.replace("rgba(", "").replace(")", "").split(",") : bgColor.replace("rgb(", "").replace(")", "").split(",");

				var r = bgColor[0];
				var g = bgColor[1];
				var b = bgColor[2];				
				var ord = $(this).attr("ord");
				
				var memo = new Memo(UI.CONTAINER);
				memo.init($(this).get(0), ord, r, g, b);
					       		
				Ctrl.Memo.list.push(memo);				
			})
			
			Ctrl.Memo.auth();
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
				var memo = new Memo(UI.CONTAINER);
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
		
		removeOne : function(memoId){
			var len = Ctrl.Memo.list.length;
			var deleteMemo = null;			
			for(var i=0; i<len; i++){
				var memo = Ctrl.Memo.list[i];				
				if(memoId == memo.get("id")){		
					deleteMemo = memo;
					break;
				}				
			}
			if(deleteMemo){
				Ctrl.Memo.list = Ctrl.Memo.list.without(deleteMemo);	
			}
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
				if(typeof(packet.bgimg) != "undefined" && packet.bgimg != "") this._setImg(parseInt(packet.bgimg) -1);
				if(packet.color_r != "" && packet.color_g  != "" && packet.color_b  != ""){
					// color가 있을경우 check 처리
					$("span", "#bg_box").each(function(){
						if($(this).index() == 11){
							$(this).removeClass("checked");
						}			
					});					
					this._setRgbColor(packet.color_r, packet.color_g, packet.color_b);
				}
			}				
		},
		
		save : function(){
			var url = Utils.addContext(_url("bg.save"));
			var params = {
				roomid : RoomSvr.roomid,
				pageid : UI.current,
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
			var imgUrl = Utils.addResPath("images", "background_0" + (idx+1) + ".png");
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
					
					bgColor = bgColor.indexOf("rgba") > -1 ? bgColor.replace("rgba(", "").replace(")", "").split(",") : bgColor.replace("rgb(", "").replace(")", "").split(",");
					
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
				
				if(this.red == "" && this.green == "" && this.blue == ""){
					$("span", "#bg_box").each(function(){
						if( $(this).index() == 11){
							$(this).addClass("checked");
						} 		
					});
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
			
			var fileData = packet.filedata || "";

			//-- test2			
			// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
			var board = UI.getBoard();
			var imgCanvas = board.getCanvas("img");
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
					var editor = Ctrl.BGImg.setDrag(img, imgCanvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree, fileData);
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
				
				PacketMgr.Master.img(url, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, degree, fileData);
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
			// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");			
			var board = UI.getBoard();
			var imgCanvas = board.getCanvas("img");			
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
			
			var fileData = packet.filedata;
			
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
	    			var editor = Ctrl.BGImg.setDrag(img, imgCanvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree, fileData);
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
			// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
			var board = UI.getBoard();
			var imgCanvas = board.getCanvas("img");
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
				// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
				var board = UI.getBoard();
				var imgCanvas = board.getCanvas("img");
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
			Ctrl.Modal.confirm(_msg("confirm.remove.file.all"), function(){
				var remove = function(deleteSeqNo){
					// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
					var board = UI.getBoard();
					var imgCanvas = board.getCanvas("img");
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
		
		setDrag : function(img, canvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree, fileData){		
			var defaultThumb = Utils.addResPath("images", "thum_user.png"); 
			var headerHeight = $("#" + UI.HEADER).height();
			var context = canvas.getContext("2d");
			
			var board = UI.getBoard();
			var imgCanvas = board.getCanvas("img");
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
			
			var attachClass = (typeFlag == "1") ? "img_answer" : (typeFlag == "2") ? "poll_img_box" : ""
						
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
			var userInfo = (typeFlag == "1") ? "<a class=\"answered_user\"><img class=\"user_photo\" src=\""+thumbnail+"\" onerror=\"this.src='"+defaultThumb+"'\" ><span>"+userNm+"</span></a>" : 
								(typeFlag == "2") ? "<a class=\"poll_questionText\"><div class=\"poll_questionTextWrap\"><span>" + fileData + "</span></div></a>" : "";
			div.innerHTML = "<span class=\"img_del\" title=\"delete image\" style=\"z-index:"+zIndex+"\" onclick=\"Ctrl.BGImg.remove('"+seqNo+"');\"><a class=\"btn_x\"></a></span>\
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

				PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, lastDegree, fileData);

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

					PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, lastDegree, fileData);

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
		        
				PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, degree, '');
				
				// 정렬 맞춤
				Ctrl.BGImg.redraw("", seqNo);
				 
				$(div).css("zIndex", zIndex);
				$(".img_del", div).css("zIndex", zIndex);
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
			var sketch = UI._getSketch(UI.current);
			var layer = $(".img_box_edit", sketch).get(0);
			if(layer != null){
				// typeflag
				$(".img_box_edit", sketch).each(function(){
					var typeFlag = $(this).attr("typeflag") || "0";
					if(typeFlag == "1"){
						// remove Class
						$(this).removeClass("img_answer");
						$(this).css("cursor", "move");
					}else if(typeFlag == "2"){
						$(this).removeClass("poll_img_box_disable");
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
			var sketch = UI._getSketch(UI.current);			
			var layer = $(".img_box_edit", sketch).get(0);
			if(layer != null){
				// typeflag
				$(".img_box_edit", sketch).each(function(){
					var typeFlag = $(this).attr("typeflag") || "0";
					if(typeFlag == "1"){
						// add Class
						$(this).addClass("img_answer");
						$(this).css("cursor", "default");
					}else if(typeFlag == "2"){
						$(this).addClass("poll_img_box_disable");
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
			var sketch = UI._getSketch(UI.current);
			var layer = $(".img_box_edit", sketch).get(0);
			if(layer != null){				
				$(".img_box_edit", sketch).unbind("click");
				$(".img_box_edit", sketch).draggable("destroy");
				$(".img_box_edit", sketch).resizable("destroy");
				$(".img_box_edit", sketch).remove();
			}
			
			Ctrl.BGImg.data = null;
		}
	},
	
	Text : {
		// id : {object}
		data : null,
		current : "",	// 현재 편집중인 id
		redrawSkipId : "", // text annotaion시 skip 처리하는 아이디
		historySkip : false,
		dragging : false,
		editorMinWidth : 224,
		edit : ["fitalic","fbold","fleft","fcenter","fright"],
        ctrlDown : false,
		CTRLKEY : 17,
		CMDKEY : 91,
		AKEY : 65,		
		init : function(){
			this._setEvent();
			
			// 기본 사이즈 지정
			this.editorMinWidth = $("#txt_area").width();
		},
		
		isActive : function(){
			return $("#text_btn").hasClass("checked");
		},
		
		isOpened : function(){
			var display = $("#fontbox").css("display");
			return (display == "none") ? false : true;
		},		
		
		isBlank : function(){
			if($("#txt_area").get(0) == null) return true;
			
			var val = $("#txt_area").val().trim();
			return val == "" ? true : false;
		},
		
		_setEvent : function(){
			$(".ffamily", "#fontbox").change(function(e){
				var val = $(this).val();
				$("#txt_area").css("fontFamily", val);
				
				Ctrl.Text.resizeTextArea(e);
			});
			
			$(".fsize", "#fontbox").change(function(e){
				var val = $(this).val();
				$("#txt_area").css("fontSize", val + "px");
				
				Ctrl.Text.resizeTextArea(e);
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
					PacketMgr.Master.textbox(packet.id, "2", packet.text, packet.face, packet.size, packet.r, packet.g, packet.b, packet.w, packet.h, packet.x, packet.y, packet.italic, packet.bold);	
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
		
		resizeTextArea : function(e){
			var text = document.getElementById("txt_area");			
			if(text.scrollHeight < 100) text.scrollHeight = 100 + "px";
			
			text.style.height = 'auto';
	        text.style.height = text.scrollHeight+'px';	       

	        // text update width
	        if(typeof(e) != "undefined" && e != null && (e.type == "keyup" || e.type == "change")){
	        	var txt = text.value;
	  	        txt = txt.replace(/\n/g, "<br />");
	  	        
	  	        $("#fk_1").html(txt);
	  	        $("#fk_1").css("fontFamily", text.style.fontFamily);
	  	        $("#fk_1").css("fontSize", text.style.fontSize);
	  	        
	        	var w = $("#fk_1").width() + 60; 
	        	if(w < Ctrl.Text.editorMinWidth){
	  				w = Ctrl.Text.editorMinWidth;
	  			}
	  	        text.style.width = w + "px";
	        }
	      	
			if(e.type == "keydown"){
		        if(e.keyCode == Ctrl.Text.CTRLKEY || e.keyCode == Ctrl.Text.CMDKEY) Ctrl.Text.ctrlDown = true;
		        if(Ctrl.Text.ctrlDown){
					// ctrl + a => select all
					if(e.keyCode == Ctrl.Text.AKEY){
						$(text).select();
					}					
				}
				
			}else if(e.type == "keyup"){
				if (e.keyCode == Ctrl.Text.CTRLKEY || e.keyCode == Ctrl.Text.CMDKEY) Ctrl.Text.ctrlDown = false;
			}
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
			// UI.skboards[UI.current-1].removeText(id);
			
			var board = UI.getBoard();
			board.removeText(id);
			
			// data 삭제
			Ctrl.Text._removeData(id);

			// history 삭제 
			PacketMgr._removePacket(PacketMgr.lastPageId, id, false);
			
			Ctrl.Text.current = "";
			
			$("#txt_" + id).remove();
			$("#txt_area").val("");				
			$("#fontbox").hide();
		},
		
		removeAll : function(isRemovePacket){
			// 휴지통에서 지울경우 전부 삭제 
			var len = this.data == null || this.data.keys() == null ? 0 : this.data.keys().length;
			if(this.data != null){
				var dataKeys = this.data.keys();
				var len = dataKeys == null ? 0 : dataKeys.length;
				for(var i=0; i<len; i++){
					var dataId = dataKeys[i];
					// request check
					if(isRemovePacket) PacketMgr._removePacket(PacketMgr.lastPageId, dataId, false);
					Ctrl.Text._removeData(dataId)
					
					// UI.skboards[UI.current-1].removeText(dataId);
					var board = UI.getBoard();
					board.removeText(dataId);
			
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
			// var cvs = $("#sketch" + UI.current).get(0);
			$("#sketch" + UI.current).mousedown(function(ev){
				// hide menu;				
				Ctrl.hideToolMenu();
				
				// $("#textContainer").mousedown(function(ev){
				/* mousedown은 드로잉과 text 동시에 사용 가능하게 한다. event 전파 시켜야 함.
					ev.stopImmediatePropagation();
					ev.preventDefault();
				*/
// Utils.log("step1 Ctrl._checkAuth(false) : " + Ctrl._checkAuth(false) + ", UI.scale : " + UI.scale);
				if(!Ctrl._checkAuth(false)) {
					Ctrl.Text.current = "";
					$("#fontbox").hide();
					return;
				}			
				 
				if(UI.scale > 1){
					if(Ctrl.Text.isOpened()) { 
						Ctrl.Msg.show(_msg("textbox.cant.add"));
					}					
					return;
				}				  
// 이시점은 addMode이기 때문에 current가 공백이면 return 처리 해야한다.
// Utils.log("step2 Ctrl.Text.isOpened() : " + Ctrl.Text.isOpened() + ", Ctrl.Text.current : " + Ctrl.Text.current + ". Ctrl.Text.isBlank : " + Ctrl.Text.isBlank() + ", Ctrl.Text.isActive : " + Ctrl.Text.isActive());
				if(Ctrl.Text.isOpened() && (Ctrl.Text.current != "" || !Ctrl.Text.isBlank() )){
					Ctrl.Text.save();
					
					// 기존에 저장된 적이 있던 패킷이라면.....
					if(Ctrl.Text.current != ""){
						var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
						PacketMgr.Master.toCanvasPage(beforePacket, UI.current);	
					}
				}				
// Utils.log("step3 Ctrl.Text.isActive() : " + Ctrl.Text.isActive());
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
				// $("#text_btn2").removeClass("checked");
				
				Ctrl.Text.current = "";
				
				Ctrl.changeCursor("1");		
				
				Ctrl._selectToolMenu(1);
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
console.log("Ctrl.Text.isOpened() : " + Ctrl.Text.isOpened() + ", Ctrl.Text.current : " + Ctrl.Text.current + ", id : " + id);
console.log("$('#fontbox').val() : " + $("#fontbox").val());

					if(Ctrl.Text.isOpened() && Ctrl.Text.current != id && $("#txt_area").val() != ""){
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

					// UI.skboards[UI.current-1].removeText(id);
					var board = UI.getBoard();
					board.removeText(id);
					
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
							// UI.skboards[UI.current-1].removeText(id);
							var board = UI.getBoard();
							board.removeText(id);
						}	
						
						Ctrl.Text.current = id;
						
						var selectPacket = Ctrl.Text._getData(id);
						Ctrl.Text._syncEditor(selectPacket);
						
						// {"cmd":"textbox","type":"1","id":"1fbd85e0","text":"aaaaaaa","face":"Arial","size":"20","w":"224","h":"46","x":"442.8","y":"294.140625","r":"0","g":"0","b":"0","bold":"0","italic":"0"}
						var bold = selectPacket.bold == "1" ? "font-weight:bold;" : "";
						var italic = selectPacket.italic == "1" ? "font-style:italic;" : "";						
						
						div.style.fontFamily = selectPacket.face;
						div.style.fontSize = selectPacket.size + "px";
						div.style.whiteSpace = "nowrap";
						
						if(selectPacket.bold == "1") div.style.fontWeight = "bold";
						if(selectPacket.italic == "1") div.style.fontStyle = "italic";
					 	div.style.color = "rgb("+selectPacket.r+","+selectPacket.g+","+selectPacket.b+")";

						var txt = selectPacket.text.replace(/\n/g, "<br />");
						div.innerHTML = txt;
						
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

						// UI.skboards[UI.current-1].removeText(id);
						var board = UI.getBoard();
						board.removeText(id);
						
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
					PacketMgr.Master.textbox(packet.id, "2", packet.text, packet.face, packet.size, packet.r, packet.g, packet.b, packet.w, packet.h, packet.x, packet.y, packet.italic, packet.bold);
					return true;
				}
				if(text == packet.text && w == packet.w && h == packet.h && size == packet.size && face == packet.face
						&& r == packet.r && g == packet.g && b == packet.b && italic == packet.italic && bold == packet.bold && compareX == packet.x && compareY == packet.y){
// 					Ctrl.Text.receive(packet);					
					Utils.log("Not Changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!! : " + JSON.stringify(packet));
					return false;
				}
			}
			
			PacketMgr.Master.textbox(id, type, text, face, size, r, g, b, w, h, x, y, italic, bold);
			
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
				// Ctrl.changeCursor("2");
			}else{
				$("div", "#textWrapper").css("cursor", "default");
				// text모드에서 zoom한경우 zoom동작시키면 커서를 손으로 바꾼다.
				// Ctrl.penIdx == 0 && UI.scale == 1
				// text 모드 였을 경
				if(UI.scale > 1){
					Ctrl.changeCursor((Ctrl.penIdx == 0 ? "1" : ""));
				}							
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
			// $("#text_btn2").removeClass("checked");
			
// $("#handCtl").addClass("checked");

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
				if(beforePacket) PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
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
			if(UI.scale > 1) {				
				Ctrl.Msg.show(_msg("textbox.cant.add"));
				return false;
			}
			
			// zoom
			if(!Ctrl.isHand()){
				Ctrl.toggleRC(0, -1, false);
			} 
			
			if($("#text_btn").hasClass("checked")){
				Ctrl.Text.cancel(true);				
				Ctrl.changeCursor("1");				
			}else{				
				$("#text_btn").addClass("checked");
				// $("#text_btn2").addClass("checked");				
				Ctrl.changeCursor("2");
			}
			
			// Ctrl.BGImg.auth();
			Ctrl.BGImg.auth();
			
			PDFViewer.auth();
			
			return true;
		},
				
		toggleEdit : function(name){
			if($("#txt_area").hasClass(name)){
				$("#txt_area").removeClass(name);
			}else{
				$("#txt_area").addClass(name);
			}				
		},

		// 페이지 변경시 초기화
		clear : function(){
			this.removeAll(false);
			// $("#sketch" + UI.current).unbind("click");
			$("#sketch" + UI.current).unbind("mousedown");			
		},
		
		destroy : function(){

			// $("#sketch" + UI.current).unbind("click");
			$("#sketch" + UI.current).unbind("mousedown");
			
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
			$("#btn_room_update").click(function(){
				if(!$("#ck_vcam").is(":disabled") && $("#ck_vcam").is(":checked")){					
					Ctrl.Modal.confirm(_msg("klounge.setup.cam.alert"), function(){						
						$("#ck_vcam").attr("disabled", true);
						Ctrl.Room.update('setting');
					});						
				}else{
					Ctrl.Room.update('setting');	
				}
			});
			
			$("#ck_passwd").click(function(){
				if($(this).is(":checked")){
					$("#passwd_txt").parent().show();
				}else{
					$("#passwd_txt").parent().hide();
				}
			});
		},
		
		update : function(){
			if(PacketMgr.userid != PacketMgr.creatorid){
				// Ctrl.Msg.show(_msg("not.allow"));
				Ctrl.Msg.auth(true);
				return;
			}

			var ckAuthType = $("#ck_authtype").get(0) == null ? "" : ($("#ck_authtype").is(":checked") ? "1" : "0");
			var ckUsePasswd = $("#ck_passwd").get(0) == null ? false : $("#ck_passwd").is(":checked");
			var passwdTxt = ckUsePasswd ? $("#passwd_txt").val() : "";
			var chatOpt = $("#ck_chat").get(0) == null ? "" : $("#ck_chat").is(":checked") ? "1" : "0";
			var cmtOpt = $("#ck_cmt").get(0) == null ? "" : $("#ck_cmt").is(":checked") ? "1" : "0";
			var expOpt = $("#ck_exp").get(0) == null ? "" : $("#ck_exp").is(":checked") ? "1" : "0";
			
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
			
			var tcount = title.length;			
			if(tcount > 60){				
				Ctrl.Msg.show(_msg("validation.title"));
				return;
			}
			
			if(ckUsePasswd && passwdTxt.trim() == ""){
				Ctrl.Msg.show(_msg("insert.passwd"));
				return;
			}
			
			if(ckUsePasswd && (passwdTxt.length < 4 || passwdTxt.length > 6)){
				Ctrl.Msg.show(_msg("validation.passwd"));
				return;
			}			
			
			if(ckUsePasswd && !passwdTxt.isEngNum()) {
				Ctrl.Msg.show(_msg("validation.passwd.kor"));
				return;
			}
			
			if(!ckUsePasswd) passwdTxt = "";
			
			PacketMgr.Master.updateRoomInfo(ckAuthType, chatOpt, cmtOpt, expOpt, title, passwdTxt);			
			
			$("#room_title").html(title);
			
			$("#setup_box").hide();
			
			$("#titleModal").hide();
			
			Ctrl.Msg.show(_msg("msg.success.infomation"));
		},
		
		updateRoomInfo : function(packet){
			PacketMgr.isAllowMaster = (packet.authtype == "1") ? true : false;
			PacketMgr.isAllowChat = (packet.chatopt == "1") ? true : false; 
			PacketMgr.isAllowComment = (packet.cmtopt == "1") ? true : false; 
			PacketMgr.isAllowExport = (packet.expopt == "1") ? true : false;
			PacketMgr.isOnlyTeacherVCam = (packet.vcamopt == "1") ? true : false;
			PacketMgr.isOnlyTeacherVShare = (packet.vshareopt == "1") ? true : false;			
			
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
	
	// 화면 동기화 
	Sync : {		
		reloadRoomThumb : function(){
			Ctrl.Preview.refresh();
		},		
		removeLimit : function(){
			SyncUpdated.removeLimit();			
		}
	},
	
	// videoShare
	VShare : {		
		template : "",		
		list : [],
		limit : 1,	// 제한 수
		// 1,2,3,4,5,6
		done : false,
		status : 0,	// 0-정지, 1-시작, 2-일시정지
		init : function(){
			$(".vshare", $("#vShareWrapper")).each(function(){
				var ord = 1;
				var vShare = new VShare(UI.CONTAINER);
				vShare.init($(this).get(0), PacketMgr.isMC, ord);
					       		
				Ctrl.VShare.list.push(vShare);				
			});
		},		
		add : function(){			
			if(PacketMgr.isOnlyTeacherVShare && PacketMgr.usertype != "2" && !PacketMgr.isCreator){
				Ctrl.Msg.show(_msg("m.auth.msg.1"));				
				return;
			}

			var _add = function(){
				var ord = Ctrl.VShare.getMax() + 1;
				var vShare = new VShare(UI.CONTAINER);
				vShare.draw("vShareWrapper", ord, PacketMgr.isMC);
				
				Ctrl.VShare.list.push(vShare);
			}
			
			var len = this.list.length;
			if(len >= Ctrl.VShare.limit){
				Ctrl.Modal.confirm(_msg("noti.ytube.only"), function(){
					// Ctrl.VShare.destroy();
					// Ctrl.VShare.remove(true);
					var removeIdx = -1;
					var len = Ctrl.VShare.list == null ? 0 : Ctrl.VShare.list.length;
					if(len > 0){
						var vShare = Ctrl.VShare.list[0];
						vShare.remove(true, function(){
							_add();		
						});
					}
				});
				
				// title 변경
				$(".pop_tit", "#cfmModal").html(_msg("insert.ytube.title"));
				
			}else{
				_add();	
			}			
		},
		// 패킷을 받아서 드로잉
		receive : function(packet){ 
			var type = packet.type || '';
			var vsNo = packet.vsno || ''; 
			var seqNo = packet.seqno || '';
			var title = packet.title || '';
			var content = packet.content || '';
			var left = packet.x || 0;
			var top = packet.y || 0;
			var status = packet.status || '0';
			var time = packet.time || '';
			var ord = packet.ord || Ctrl.VShare.list.length;
			
			if(status != this.status){
				this.status = status;
			}
			
			if(type == '0'){				
				var len = Ctrl.VShare.list.length;				
				if(len > 0){					
					this.destroy();
				}
				var vShare = new VShare(UI.CONTAINER);
				vShare.receive(type, "vShareWrapper", PacketMgr.isMC, vsNo, seqNo, title, content, left, top, status, 1, time);
				
				this.list.push(vShare);
				
			}else if(type == '1' || type == '2'){
				// 삭제
				var removeIdx = -1;
				var len = this.list == null ? 0 : this.list.length;
				for(var i=0; i<len; i++){
					var vShare = this.list[i];
					// $(container).attr("memono", json.map.commentno);		
					if(vShare.get("vsno") == vsNo){						
						vShare.receive(type, "vShareWrapper", PacketMgr.isMC, vsNo, seqNo, title, content, left, top, status, 1, time);
						removeIdx = i;			
						break;
					}							
				}
								
				if(type == '2' && removeIdx > -1) this.list.splice(removeIdx, 1);	
			}
		},		
		auth : function(){
			// 마스터 바뀐경우 이벤트 다시 수정
			var len = this.list.length;
			for(var i=0; i<len; i++){
				var vShare = this.list[i];
				vShare.changeMC(PacketMgr.isMC);
			}				
		},		
		getMax : function(){
			var bfOrd = -1;
			var len = this.list.length;
			for(var i=0; i<len; i++){
				var vShare = this.list[i];
				var ord	= vShare.getOrd();
// console.log("ord : " + ord);
				if(bfOrd < ord){
					bfOrd = ord;
				}				
			}
			return bfOrd;			
		},		
		
		seek : function(){
			 
		},
		
		onPlayerReady : function(event){
			/**
		 	console.log( event.target.getCurrentTime() );
			event.target.playVideo();
			**/
			var player = Ctrl.VShare.list[0].getPlayer();
			if(player) Ctrl.VShare.list[0].setTitle(player.getVideoData().title);
		},
		
		onPlayerStateChange : function(event){
			if(PacketMgr.isMC){
				/**
				YT.PlayerState.ENDED
				YT.PlayerState.PLAYING
				YT.PlayerState.PAUSED
				YT.PlayerState.BUFFERING
				YT.PlayerState.CUED
				**/
				
				var status = event.target.getPlayerState();
				/**
				if(event.data == YT.PlayerState.ENDED && this.status != YT.PlayerState.ENDED) {
					
					Ctrl.VShare.list[0].changeStatus(YT.PlayerState.ENDED, event.target.getCurrentTime(), '');
					
				}else if(event.data == YT.PlayerState.PLAYING && (this.status != YT.PlayerState.PLAYING)){
					
					Ctrl.VShare.list[0].changeStatus(YT.PlayerState.PLAYING, event.target.getCurrentTime(), '');
				}else if(event.data == YT.PlayerState.PAUSED && this.status != YT.PlayerState.PAUSED){
					
					Ctrl.VShare.list[0].changeStatus(YT.PlayerState.PAUSED, event.target.getCurrentTime(), '');
					// event.target.getCurrentTime()
				}
				**/
				// 정지일때는 보내지 않는다.
				Ctrl.VShare.list[0].changeStatus(status, event.target.getCurrentTime(), '');

				this.status = status;
			}			
		},
		
		sendMCPlayerStatus : function(newUserNo){
			if(PacketMgr.isMC && this.list.length > 0){			
				var player = Ctrl.VShare.list[0].getPlayer();
				var status = player.getPlayerState();
				var currentTime = player.getCurrentTime();

				if(status == YT.PlayerState.ENDED){
					
				}else if(status == YT.PlayerState.PLAYING){					
					Ctrl.VShare.list[0].changeStatus(status, currentTime, newUserNo);					
				}else if(status == YT.PlayerState.PAUSED || status == YT.PlayerState.BUFFERING){
					

					if(currentTime > 0){
						Ctrl.VShare.list[0].changeStatus(status, currentTime, newUserNo);
					}
				}
			}			
		},
		
		changeOpt : function(){
			// show
			var len = Ctrl.VShare.list.length;
			for(var i=0; i<len; i++){
				var vShare = Ctrl.VShare.list[i];
				
				if(PacketMgr.isOnlyTeacherVShare && PacketMgr.usertype != "2" && !PacketMgr.isCreator){
					vShare.hide();
				}else{
					vShare.show();
				}
			}
		},
		
		remove : function(){
			Ctrl.VShare.list = null;
			Ctrl.VShare.list = [];						
		},
		
		destroy : function(){
			var len = Ctrl.VShare.list.length;
			for(var i=0; i<len; i++){
				var vShare = Ctrl.VShare.list[i];
				
				if(PacketMgr.isMC){
					vShare.destroy();	
				} else {
					vShare.destroyUser();
				}
			}
			
			Ctrl.VShare.list = [];			
		}
	},	
	
	// zoom Preview
	Preview : {
		orgWidth : 200,
		orgHeight : 200,
		orgScaleX : 0,
		orgScaleY : 0,
		init : function(){
			this._setEvent();
		},
		
		_setOrgSize : function(w, h){
			if(this.orgScaleX > 0 && this.orgScaleY > 0) return;
			if(w > h){
	            this.orgScaleX = w / 1024;
	            this.orgScaleY = h / 748;
	        }else{
	        	this.orgScaleX = w / 768;
	        	this.orgScaleY = h / 1004; 
	        }
		},
		_setEvent : function(){			
			var delta = 0;
			$(".naviView", "#preview_navigator").on('mousewheel DOMMouseScroll', function(e) {
				if(PacketMgr.isMC){
					var E = e.originalEvent;
	                delta = (E.detail) ? E.detail * -40 : E.wheelDelta; 
	                if(delta > 0){      
	                	Ctrl.zoomIn("0");          	
	                }else{
	                	Ctrl.zoomOut("0");
	                }	
				}
				e.preventDefault();
            }); 

			/*$(".btn_close2", "#preview_navigator").click(function(){
				// alert("toggle")
				$("#preview_navigator").toggleClass("fold");
			});*/
			/*$(".btn_minimize2", "#preview_navigator").click(function(){
				$("#pageContainer").hide();			
				$("#preview_navigator").show();
			});
			*/
			
			$(".btn_maximize2", "#preview_navigator").click(function(){
				$("#preview_navigator").hide();
				// $(".btn_preview", "#contsWrapper").hide();
				$("#pageContainer").show();
			});
			
			$(".btn_close", "#preview_navigator").click(function(e){
				/**
				$("#pageContainer").hide();
				$("#preview_navigator").hide();
				
				$(".btn_preview", "#contsWrapper").attr("before", "preview_navigator");
				$(".btn_preview", "#contsWrapper").show();
				***/				
				UI.Page.fold("preview_navigator");
				
				e.stopImmediatePropagation();
				e.preventDefault();
			});
			
			var guideWidth = this.orgWidth;
			var guideHeight = this.orgHeight;
			var posX = 0;
			var posY = 0;						
			var settled = "0";
			
			$(".navi_nowView", "#preview_navigator").draggable({
				containment: $(".naviView", "#preview_navigator"),
				start : function(e){
					guideWidth = parseInt($(".navi_nowView", "#preview_navigator").css("width").replace("px"));
					guideHeight = parseInt($(".navi_nowView", "#preview_navigator").css("height").replace("px"));
				},
				drag : function(e, ui){
					posX = ui.position.left;
					posY = ui.position.top; 
					settled = "0";
					
					zoomHandle();
				},
				stop : function(e, ui){
					posX = ui.position.left;
					posY = ui.position.top;
					settled = "1";
 					zoomHandle();
				}
			});
			
			var zoomHandle = function(){
				if(UI.scale == 1) return;
				
				// var drawCanvas = UI.skboards[UI.current-1].getCanvas("draw");
				var board = UI.getBoard();
				var drawCanvas = board.getCanvas("draw");
				var w = $(drawCanvas).width();
				var h = $(drawCanvas).height();
				
				var centerX = posX + (guideWidth / 2);
				var centerY = posY + (guideHeight / 2);
				var dx = centerX * w / Ctrl.Preview.orgWidth;
				var dy = centerY * h / Ctrl.Preview.orgHeight;
				
				var fixedX = UI.getFixedX(w, h, dx) * UI.scale;
				var fixedY = UI.getFixedY(w, h, dy) * UI.scale;

				PacketMgr.Master.zoomHandleCenter(UI.scale, settled, fixedX, fixedY, UI.current);	
			}
		},
		
		update : function(packet){
			// 1. scale text update
			var val = Math.floor(packet.scale * 100);
			$(".preview_scale", "#preview_navigator").html(val + "%");
			
			// 2. width, height update 
			var guideWidth = this.orgWidth * 100 / val;
			var guideHeight = this.orgHeight * 100 / val;
			 
			$(".navi_nowView", "#preview_navigator").css("width", guideWidth + "px");
			$(".navi_nowView", "#preview_navigator").css("height", guideHeight + "px");
			
			var moveX = 0;
			var moveY = 0;
			
			if(packet.scale > 1){
				// var drawCanvas = UI.skboards[UI.current-1].getCanvas("draw");
				var board = UI.getBoard();
				var drawCanvas = board.getCanvas("draw");
				var w = $(drawCanvas).width();
				var h = $(drawCanvas).height();

				this._setOrgSize(w, h);
				
				var newScale = packet.scale; 
				var orgX = packet.x * this.orgScaleX;
				var orgY = packet.y * this.orgScaleY;
				
				var scaleX = ((w * newScale / 2) - orgX) / newScale;
				var scaleY = ((h * newScale / 2) - orgY) / newScale;
				var maxX = (w / 2) * (newScale-1) / newScale; 
				var maxY = (h / 2) * (newScale-1) / newScale;				
				var centerX = (this.orgWidth / 2) - (guideWidth / 2);
				var centerY = (this.orgHeight / 2) - (guideHeight / 2);				
				var newScaleX = scaleX * this.orgWidth / w;
				var newScaleY = scaleY * this.orgHeight / h;
				
				var moveX = centerX - newScaleX;
				var moveY = centerY - newScaleY;

				if(moveX < 0) moveX = 0;
				if(moveY < 0) moveY = 0;
				 
			}			
			
			$(".navi_nowView", "#preview_navigator").css("left", moveX);
			$(".navi_nowView", "#preview_navigator").css("top", moveY);			
			
			/*
			 * var translateX = scaleX;
    		var translateY = scaleY;
    		$("img", "#preview_navigator").css('-webkit-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('-moz-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('ms-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('-o-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)');
			*/
    		
		},
				
		refresh : function(){
			if($("img", "#preview_navigator").get(0) != null){
				var src = $("img", "#preview_navigator").attr("src");
				if(src.indexOf("?") > -1){
					src = $("img", "#preview_navigator").attr("src").substring(0, src.indexOf("?"));
				}
				src += "?t=" + new Date().getTime();				
				$("img", "#preview_navigator").attr("src", src);	
			}
			if($("img", "#" + UI.current).get(0) != null){
				var src = $("img", "#" + UI.current).attr("src")
				if(src.indexOf("?") > -1){
					src = $("img", "#" + UI.current).attr("src").substring(0, src.indexOf("?"));
				}
				src += "?t=" + new Date().getTime();			
				
				$("img", "#" + UI.current).show();
				$("img", "#" + UI.current).attr("src", src);
			}
		},
		
		scroll : function(){
			
		},
		
		destroy : function(){
			$(".naviView", "#preview_navigator").unbind('mousewheel DOMMouseScroll');
			// $(".btn_close2", "#preview_navigator").unbind("click");
			$(".btn_maximize2", "#preview_navigator").unbind("click");
			$(".btn_close", "#preview_navigator").unbind("click");
			
			// $(".navi_nowView", "#preview_navigator").draggable("destroy");
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
		/**
		this._drawPrevCanvas(5);
		
		this._drawPrevCanvas(6);
		
		this._drawPrevCanvas(7);
		***/
		
		Ctrl.Background.init();
		
		Ctrl.Room.init();
		
		Ctrl.Comment.init();
		
		Ctrl.Chat.init();
		
		Ctrl.Memo.init();
		
		// Ctrl.Uploader.setProgress();
		Ctrl.Uploader.init();
		
		Ctrl.Text.init();
		
		Ctrl.Member.init();
		
		Ctrl.VShare.init();
		
		Ctrl.Preview.init();
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
			if(!Ctrl.isOnline){
				Ctrl.isOnline = true;
				displayIcon(true);
			}				
		}else{			
			if(Ctrl.isOnline){
				Ctrl.isOnline = false;
				displayIcon(false);
				if(PacketMgr.isMC && !skipModal){					
					Ctrl.Modal.network();
				}	
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
		// return ($("#hbar_0").css('display') == 'none') ? false : true;
		return $("#handCtl").hasClass("checked");
		
	},	
	isText : function(){
		return ($("#text_btn").hasClass("checked") || Ctrl.Text.isOpened()) ? true : false;
	},	
	isZoom : function(){
		return UI.scale == 1 ? false : true;
	},
	
	_checkAuth : function(showMsg, isAuto) {
		// 메시지 한곳에서 처리하기 위해 이곳에서 작업함
// Utils.log("flag : " + PacketMgr.isMC + ", showMsg : " + showMsg);
		
		if(!PacketMgr.isMC && showMsg){
			if((!PacketMgr.isCreator && !PacketMgr.isAllowMaster) || PacketMgr.isGuest){
				Ctrl.Msg.show(_msg("noti.not.allow"));
				return;
			}
			
			isAuto = (typeof(isAuto) == "undefined" || isAuto == null) ? true : isAuto;			
			if(!isAuto){
				// 자동 권한 이전에 제한을 둬야하는 기능이라면 권한 모달만 띄운
				Ctrl.Msg.auth(true);				
				return;
			}
			
			// Ctrl.Msg.auth(false);
			if(this._checkWaiting() ){ // 누가 드로잉중이라 기달려야 하는 상황 이라면?
				// var masterUserInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
				var masterUserInfo = Ctrl.Member.getClassUserOnline(PacketMgr.masterid, "userid");
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
		
		this._setMenuEvent();
		
		this._setDocEvent();
		
		this._setPenEvent();		
		
		this._setSlider();

		Ctrl.Chat.setEvent();
	},
	
	_selectToolMenu : function(index){
		$('#ctrlToolbar > .right_menubar > a').removeClass("checked");
		$('#ctrlToolbar > .right_menubar > a').eq(index).addClass("checked");
	},
	
	_setRemoteControl : function(){		
		
		
		$("#menuCtl", "#ctrlToolbar").click(function(){
			$("#ctrlToolbar").toggleClass("closed");						
		});
		
		/**
		$("#menuCtl", "#ctrlToolbar").click(function(){			
			if(menuOpened){
				menuOpened = false;				
				$('#ctrlToolbar > .right_menubar').children().not('#menuCtl').hide();				
				$('#ctrlToolbar > .right_menubar_option').hide();
				
			}else{
				menuOpened = true;
				$('#ctrlToolbar > .right_menubar').children().not('#menuCtl').show();				
				$('#ctrlToolbar > .right_menubar_option').show();
			}			
		});		
		**/
		
		$("#handCtl").click(function(){
			// 2017.02.16 핸드모드시 자동 권한 이전 추가.
			var isSuccess = Ctrl.toggleRC(0, -1, true);
			if(isSuccess) Ctrl._selectToolMenu(1);
		});
		
		$("#penCtl", "#ctrlToolbar").click(function(){
			var isSuccess = Ctrl.toggleRC(1, -1, true);
			if(isSuccess) Ctrl._selectToolMenu(2);			
		});
		
		$("#eraserCtl", "#ctrlToolbar").click(function(){
			var isSuccess = Ctrl.toggleRC(3, -1, true);
			if(isSuccess) Ctrl._selectToolMenu(3);
		});
		
		$("#figureCtl", "#ctrlToolbar").click(function(){
			$(".pointer_option", "#ctrlToolbar").hide();			
			$(".figure_option", "#ctrlToolbar").toggle();
		});

		$("#pointerCtl", "#ctrlToolbar").click(function(){
			$(".figure_option", "#ctrlToolbar").hide();			
			$(".pointer_option", "#ctrlToolbar").toggle();
		});
				
		$("#text_btn").click(function(e){						
			var result = Ctrl.Text.toggle($(this) );
			if(result) Ctrl._selectToolMenu(6);			
		});
		
		/*$("#bg_file2").click(function(){
			if(!Ctrl._checkAuth(true)) return;
			$("#file1").trigger("click");
		});*/
		
		$("#undoCtl").click(function() {		
			if(!Ctrl._checkAuth(true)) return;			
			PacketMgr.Master.undo(UI.current);
		});
		
		$("#redoCtl").click(function() {
			if(!Ctrl._checkAuth(true)) return;			
			PacketMgr.Master.redo(UI.current);
		});
		
		$("#clear_btn").click(function(){
			if(!Ctrl._checkAuth(true)) return;
			
			Ctrl.Modal.confirm(_msg("confirm.remove.history"), function(){
				PacketMgr.Master.eraserMode(2, UI.current);
				PacketMgr.removePacketPage(UI.current);
			});
		});
		
		$("#clear_btn2").click(function(){
			$("#clear_btn").trigger("click");
		});
	
		$("#ctrlToolbar > .figure_option > a").click(function(){
			var isSuccess = Ctrl.toggleRC(5, $(this).index(), true);
			if(isSuccess){
				Ctrl._selectToolMenu(4);				
				$("#ctrlToolbar > .figure_option").hide();
			}			
		});
		
		$("#ctrlToolbar > .pointer_option > a").click(function(){
			var isSuccess = Ctrl.toggleRC(4, $(this).index(), true);
			if(isSuccess){
				$("#pointerCtl").attr("class", "btn_lptype" + $(this).index() );
				Ctrl._selectToolMenu(5);		
				$("#ctrlToolbar > .pointer_option").hide();	
			}			
		});
	},

	_saveCanvas : function(){		
		try{
			// UI.skboards[0].save();
			var board = UI.getBoard();
			board.save();
			
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
				        
				        /***
		    		    $(saveCanvas).remove();
				        $(a).remove();
				        $(img).remove();
				        **/
					};
					
				}else{
					Ctrl.Uploader.download(saveCanvas.toDataURL() );				        
	    		    $(saveCanvas).remove();	   
				}
			}
		}catch(e){			
			var saveCanvas = $("#saveCanvas").get(0);
			if(saveCanvas){
				  $(saveCanvas).remove();	  
			}	
		} 
	},
	
	// 헤더및 메뉴 이벤트
	_setMenuEvent : function(){
		
		
		// IE에서는 이미지 파일이 많으면 다운로드 안되므로 주의하자 
		$("#save_btn").click(this._saveCanvas);
			
		$("#attend_btn").click(function(){			
			$("#bg_box").hide();
			$("#setup_box").hide();	
			$("#invite_layer").hide();

			UI.Page.fold("");
			
			// $("#user_wrapper").slideToggle({easing:"easeInCubic", duration:800});
			$("#user_wrapper").toggle();
		});
		
		$("#setup_btn").click(function(){
			$("#user_wrapper").hide();
			$("#bg_box").hide();
			$("#invite_layer").hide();
			
			UI.Page.fold("");
			// $("#setup_box").slideToggle({easing:"easeInCubic", duration:800});
			$("#setup_box").toggle();
		});
		
		$("#invite_noti_btn").click(function(){
			if(typeof(Notify) != "undefined") Notify.Invite.show("canvas");
			
			$("#bg_box").hide();
			$("#user_wrapper").hide();
			$("#setup_box").hide();
			
			UI.Page.fold("");
		});
		
		$("#bg_btn").click(function(){
			$("#user_wrapper").hide();
			$("#setup_box").hide();
			$("#invite_layer").hide();
			
			UI.Page.fold("");
			// $("#bg_box").slideToggle({easing:"easeInCubic", duration:800});			
			$("#bg_box").toggle();
		});
		
		//--- 우측 하단 메뉴
		$("#chatCtl").click(function(){
			Ctrl.Chat.toggle();			
		});
		
		$("#commentCtl").click(function(){
			Ctrl.Comment.commentBadgeCnt = 0;
			$("#comment_badge").hide();
			$(this).removeClass("checked");
			Ctrl.Comment.toggle();			
		});
		
		$("#memoCtl").click(function(e){
			if(!Ctrl._checkAuth(true)) return;
			
			Ctrl.Memo.add(e);			
		});
		
		$("#bg_file_del").click(function(e){
			if(!Ctrl._checkAuth(true)) return;			
			Ctrl.BGImg.removeAll(e);
		});
		
		$("#bg_file").click(function(){
			if(!Ctrl._checkAuth(true)) return;
			$("#file1").click();
		});
		
		$("#file1").change(function(e){
			if(!Ctrl._checkAuth(true)) return;
			
			Ctrl.Uploader.uploadImg(e, $(this).attr("id"));			
		});
		
		$("#bg_pdf").click(function(){
			if(!Ctrl._checkAuth(true)) return;
			$("#file_pdf").click();
		});

		$("#vsCtl").click(function(e){
			if(!Ctrl._checkAuth(true)) return;
			
			Ctrl.VShare.add(e);			
		});
		
		$("#file_pdf").change(function(e){
			if(!Ctrl._checkAuth(true)) return;
			Ctrl.Uploader.uploadPdf(e, "file_pdf");			
		});
		
		$("#exit").click(function(e){
			var type = (PacketMgr.isCreator) ? "creator" : "";
			Ctrl.Modal.exit(type);
		});
		
		$(".header_tit", "#hmenubar").click(function(e){
			var type = (PacketMgr.isCreator) ? "creator" : "";
			Ctrl.Modal.title(type);
		});
		
		$("#invite_btn").click(function(){
			Ctrl.Modal.invite();
		});
		 
		/*$(".btn_capture", "#quick_wrapper").click(function(e){
			if(!Ctrl._checkAuth(true)) return;			
			
			// Ctrl.Text.toggle($(this) );			
			Ctrl.Capture.open();
		});*/
		
		$("#pollCtrl").click(function(){
			$("#pollOptions").toggle();			
		});
		
		$("#pageCtrl").click(function(){
			// $(".btn_preview", "#contsWrapper").trigger("click");
			/**
			if($("#pageContainer").css("display") == "none" ){
				$(".btn_maximize2", "#preview_navigator").trigger("click");	
			}else{				
				$(".btn_close", "#pageContainer").trigger("click");
			}
			***/
			
			$("#pageContainer").toggle();
			
		});		
		
		$(".btn_poll", "#pollOptions").click(function(){
			PollCtrl.UI.open('poll_info_box');
			$("#pollOptions").hide();	
		});
		
		$(".btn_pollList", "#pollOptions").click(function(){
			PollCtrl.Action.Master.makePollList(1);
			$("#pollOptions").hide();
		});
		
		$(".btn_resultList", "#pollOptions").click(function(){
			PollCtrl.Action.Master.makePollCompleteList(1);
			$("#pollOptions").hide();
		});
		
	},
	
	toggleRC : function(penIdx, subIdx, isCheck){		
				
		// 1. annotation setting
		// 2. toggle header
		// 1, 3, 6, 7, 8, 9
		if(isCheck && !Ctrl._checkAuth(true)) return false;	
		
		if(penIdx > 4){
			penIdx = penIdx + subIdx - 1;			
		}
		this.penIdx = penIdx;

		// background image disable
		this.BGImg.disable();
		 
		// PDFViewer.disable();		
		PDFViewer.disable(PacketMgr.isMC, (this.penIdx == 0 ? true : false), Ctrl.isZoom());
		
		this.toggleToolMenu();

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
		
		return true;
	},
	
	// header menu
	toggleToolMenu : function(){
		var barIdx = this.penIdx;

		// $(".mopt_bar", "#hmenubar").hide();
		$(".mopt_bar", "#wrap").hide();
		
		if(barIdx == 0){
			// 손을 제외한 나머지 전부 clear
			$("a", "#ctrlToolbar").removeClass("checked");
			$("#handCtl").addClass("checked");
		}else{			
			var toolIdx = (barIdx == 6 || barIdx == 7) ? 5 : barIdx; 
			$("#hbar_" + toolIdx).show();			
			$("#handCtl").removeClass("checked");

			if(barIdx == 5 || barIdx == 6 || barIdx == 7){				
				var mode = $(".figure_btn", "#hbar_5").eq(barIdx - 5).attr("mode");
				Ctrl.__setFigureSelection(mode);
			}
			
		}
	},
	
	hideToolMenu : function(){
		$(".mopt_bar", "#wrap").hide();
		
		$("#ctrlToolbar > .figure_option").hide();
		$("#ctrlToolbar > .pointer_option").hide();
	},
	
	toggleBookmark : function(){
		var checked = $("#bmCtl").hasClass("btn_star_sel");
		
		// confirm.add.bookmark
		if(PacketMgr.isGuest){				
			Ctrl.Msg.show(_msg("comment.add.fail.auth"));	
			return;
		}
		
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
		
		$("#videoCtrl").click(function(){
			VideoCtrl.toggle();
		});
		
		$("#bmCtl").click(function(){
			Ctrl.toggleBookmark();
		});
		
		/**
		$("#handCtl").click(function(){
			// 2017.02.16 핸드모드시 자동 권한 이전 추가.
			Ctrl.toggleRC(0, -1, true);
		});
		
		$("#undoCtl").click(function() {		
			if(!Ctrl._checkAuth(true)) return;			
			PacketMgr.Master.undo(UI.current);
		});

		$("#redoCtl").click(function() {
			if(!Ctrl._checkAuth(true)) return;			
			PacketMgr.Master.redo(UI.current);
		});
		***/
		
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
			Ctrl.__setFillClear($(this), idx, 6);
		});
		
		// circle pen color
		$("a.color_select2", "#cpen_color_wrap").click(function(index){
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
			Ctrl.__setFillClear($(this), idx, 7);
		});
		
		// index번호가 안맞아서 방식 변경 
		$("a.color_select2", "#hbar_4").click(function(index){
			var idx = $("a.color_select2", "#hbar_4").index(this);
			Ctrl.__setPenColor($(this), idx, 4, "0");
		});
		
		$("a", "#pen_preview_4").click(function(index){
			Ctrl.pointerIdx = $(this).index();
			var idx = -1;
			var colorLayer = null;
			$("a.checked", "#hbar_4").each(function(){
				if($(this).hasClass("checked")){
					idx = $(this).index();
					colorLayer = $(this);
				}
			});
			Ctrl.__setPenColor(colorLayer, idx, 4, "0");			
		});
		
		$(".figure_btn", "#hbar_5").click(function(){
			var isSuccess = Ctrl.toggleRC(5, ($(this).index() + 1), true);
			if(isSuccess){
				
				Ctrl._selectToolMenu(4);				
				$("#ctrlToolbar > .figure_option").hide();
			}		
			
		});
			
		$("input[type=radio]", "#hbar_5").click(function(){
			var val = $(this).val();
			var isStroke = val.length > 1 ? false : true;
			var type = val.substring(0, 1);
			if(isStroke){
				$("#" + type + "pen_color_wrap").show();
				$("#" + type + "pen_color_wrap2").hide();
			}else{				
				$("#" + type + "pen_color_wrap2").show();
				$("#" + type + "pen_color_wrap").hide();
			}			
		});
				
		/**
		$("#pen_preview_5", "#hbar_5").click(function(index){
			$(".preview_box1", "#hbar_5").toggle();
		});
		
		$("#pen_preview_6", "#hbar_6").click(function(index){
			$(".preview_box2", "#hbar_6").toggle();
		});

		$("#pen_preview_7", "#hbar_7").click(function(index){
			$(".preview_box3", "#hbar_7").toggle();
		});
		****/
		
		// set custom color
		// mopt_bar
		
		// $(".miniColor_jqueryP", "#hmenubar").each( function() {
		$(".miniColor_jqueryP", "#drawToolbar").each( function() {
			var idx = $(".miniColor_jqueryP", "#drawToolbar").index(this) + 1;
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
		/**
		$("#clear_btn").click(function(){
			if(!Ctrl._checkAuth(true)) return;
			
			// if(!confirm(_msg("confirm.remove.history"))) return;
			Ctrl.Modal.confirm(_msg("confirm.remove.history"), function(){
				PacketMgr.Master.eraserMode(2, UI.current);
				PacketMgr.removePacketPage(UI.current);
			});
		});
		***/
		
	},
	
	__setFigureSelection : function(mode){
		// Tool
		$(".figure_wrap", "#hbar_5").hide();
		
		$("#"+mode+"Tool").show();
		$("#"+mode+"ColorBox").show();
		
		var figureIdx = mode == "line" ? 0 : mode == "square" ? 1 : 2;
		$(".figure_btn", "#hbar_5").each(function(){
			if($(this).index() == figureIdx){
				$(this).addClass("selected");		
			}else{
				$(this).removeClass("selected");
			}
		});
	},	 
	
	__setPenCustomColor : function(thisNode, idx, penIdx, figureType, code){
		if(!Ctrl._checkAuth(true)) return;
		
		var __checked = function(penIdx){
			var wrapperId = ((penIdx == 2) ? "h" : (penIdx == 4) ? "p" : (penIdx == 5) ? "l" : (penIdx == 6) ? "s" : (penIdx == 7) ? "c" : "") + "pen_color_wrap";			
			var doChecked = function(wrapperId){				// if(figureType == "1") wrapperId += "2";
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
						
						// $("a", "#" + wrapperId + "2").eq(idx-1).addClass("checked");
						$("a", "#" + wrapperId + "2").eq(idx).addClass("checked");
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
		
		// 2017.04.07 var colorIdx = idx - 1;
		
		var colorIdx = idx;
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
			/**
			Ctrl.Chat.send(msg, target, targetNm);
			PacketMgr.Master.chat(msg, target, targetNm);
			**/
			
			Ctrl.Chat.send(msg, target, targetNm);			
			PacketMgr.Master.BroadCast.chat('all', RoomSvr.roomid, msg, target, targetNm);			
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

		if(penIdx == 1){
			var idx = Ctrl.sizeList.indexOf(lineWidth);
			if(idx > -1){
				$("#" + id).attr("class", "preview_line_box");
				$("#" + id).addClass("pre_value" + (idx+1));
			}
			
			r = this.hexToRgb(strokeStyle)["r"];
			g = this.hexToRgb(strokeStyle)["g"];
			b = this.hexToRgb(strokeStyle)["b"];
			
			var alphaRatio = (alpha * 0.01);
			$("path", "#" + id).eq(1).css("stroke", "rgb("+r+", "+g+", "+b+")");
			$("path", "#" + id).eq(1).css("opacity", alphaRatio);
			
		}else if(penIdx == 3){
			var idx = Ctrl.eSizeList.indexOf(lineWidth);
// Utils.log("id : "+ id +", idx : " + idx + ", lineWidth : " + lineWidth);
			if(idx > -1){
				$("#" + id).attr("class", "preview_line_box");
				$("#" + id).addClass("pre_value" + (idx+1));
			}			
		}else if(penIdx == 4){
			// 	var pointerIdx = this.pointerIdx;	
			// var className = "lp_c" + (this.pColorIdx+1) + "t" + (this.pointerIdx+1);
			$("a", "#" + id).each(function(){
				var className = "lp_c" + (Ctrl.pColorIdx + 1) + "t" + ($(this).index() + 1);
				$(this).attr("class", className);
				
				if((Ctrl.pointerIdx + 1) == ($(this).index() + 1)){
					$(this).addClass("selected");
				}				
			});
			
		}else if(penIdx == 5){
			var idx = Ctrl.sizeList.indexOf(lineWidth);
			if(idx > -1){
				$("#" + id).attr("class", "preview_line_box");
				$("#" + id).addClass("pre_value" + (idx+1));
			}
			
			r = this.hexToRgb(strokeStyle)["r"];
			g = this.hexToRgb(strokeStyle)["g"];
			b = this.hexToRgb(strokeStyle)["b"];
			
			var alphaRatio = (alpha * 0.01);
			$("line", "#" + id).css("stroke", "rgb("+r+", "+g+", "+b+")");
			$("line", "#" + id).css("opacity", alphaRatio);
			
		}else if(penIdx == 6 || penIdx == 7){
			var idx = Ctrl.sizeList.indexOf(lineWidth);
			if(idx > -1){
				$("#" + id).attr("class", "preview_line_box");
				$("#" + id).addClass("pre_value" + (idx+1));
			}
			
			// 색깔만 변경
			r = this.hexToRgb(strokeStyle)["r"];
			g = this.hexToRgb(strokeStyle)["g"];
			b = this.hexToRgb(strokeStyle)["b"];
			
			var fillR = fillStyle != "" ? this.hexToRgb(fillStyle)["r"] : "";
			var fillG = fillStyle != "" ? this.hexToRgb(fillStyle)["g"] : "";
			var fillB = fillStyle != "" ? this.hexToRgb(fillStyle)["b"] : "";
			var alphaRatio = (alpha * 0.01);
			
			
			var target = penIdx == 6 ? "path" : "circle";
			
			$(target, "#" + id).css("stroke", "rgb("+r+", "+g+", "+b+")");
			if(fillStyle == ""){
				$(target, "#" + id).css("fill", "none");					
			}else{
				if(figureType == "0"){
					$(target, "#" + id).css("fill", "rgb("+fillR+", "+fillG+", "+fillB+")");	
				}				
			}
			$(target, "#" + id).css("opacity", alphaRatio);			
		} 
		
		if(penIdx > 4){
			// this._drawPrevCanvas(penIdx);
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

		if(this.penIdx == 0) this.penIdx = 1;
		
		var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
		var alpha = this[token + "alpha"];
		var colorIdx = this.penIdx == 1 ? this.colorIdx : this[token + "ColorIdx"];
		var lineWidth = this.penIdx == 1 ? this.lineWidth : this[token + "LineWidth"];
		var stampKind = this.pointerIdx;
		var lineCap = this.lineCap;
		var targetStyle = this.penIdx == 1 ? this.strokeStyle : this[token + "StrokeStyle"];
		
		if(this.penIdx == 3) colorIdx = -1;
		/**
			var r = 0;
			var g = 0;
			var b = 0;
			// 지우개는 rgb 코드 0으로 보내야 한다.		
			// alert(isSend + " " +  r + " " + g + " " + b);
		**/
		var r = this.colorMap[this.colorIdx]["r"];
		var g = this.colorMap[this.colorIdx]["g"];
		var b = this.colorMap[this.colorIdx]["b"];
		
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
	/*
	_setPointer : function(flag){
		var cursor = (flag == "1") ? "hand"  : (flag == "2") ? "text" : (this.penIdx == 1) ? "hpen" : (this.penIdx == 2) ? "hpen" : (this.penIdx == 3) ? "del" : (this.penIdx == 4) ? "pointer" : 
			(this.penIdx == 5 || this.penIdx == 6 || this.penIdx == 7) ? "diagram" : "";
		
		this.Cursor.change(cursor);
	},
*/	   
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
		if(typeof(hex) == "undefined" || hex == null) return;
		
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
				CanvasApp.destroy();				
				location.href = Utils.addContext("main");				
			}, "1000");			
		}else{			
			CanvasApp.destroy();						
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
		// try{
			
			// destroy ctrl service
			Ctrl.Comment.destroy();
			
			Ctrl.Chat.destroy();
			
			Ctrl.Memo.destroy();
			
			Ctrl.Background.destroy();
			
			Ctrl.BGImg.destroy();

			Ctrl.Text.destroy();
			
			Ctrl.Room.destroy();
			
			Ctrl.Modal.destroy();
			
			Ctrl.VShare.destroy();
			
			Ctrl.Preview.destroy();
			
			// destroy _setRemoteControl 
			// $('ul.circle_menu').circleMenu("destroy");
			
			$("#file2").unbind("change");
			$("#text_btn").unbind("click");

			// destroy _setMenuToggle
			$("#save_btn").unbind("click");
			$("#attend_btn").unbind("click");
			$("#setup_btn").unbind("click");
			$("#bg_btn").unbind("click");
			
			$("#chatCtl").unbind("click");
			$("#commentCtl").unbind("click");
			$("#memoCtl").unbind("click");
			
			$("#bg_file").unbind("click");
			$("#bg_file_del").unbind("click");
			
			$("#file1").unbind("change");
			$("#exit").unbind("click");
			$("#room_title").unbind("click");
			$("#invite_btn").unbind("click");
			$("#pageCtrl").unbind("click");
			
			$("#pollCtrl").unbind("click");	
			$(".btn_poll", "#pollOptions").unbind("click");		
			$(".btn_pollList", "#pollOptions").unbind("click");		
			$(".btn_resultList", "#pollOptions").unbind("click");
			
			// $(".btn_capture", "#quick_wrapper").unbind("click");			
			// $("ul.bottom_cmenu", "#quick_wrapper").circleMenu("destroy");
			
			// destroy  _setDocEvent
			$("#handCtl").unbind("click");
			$("#undoCtl").unbind("click");
			$("#redoCtl").unbind("click");

			$("#clear_btn").unbind("click");
			$("#clear_btn2").unbind("click");

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
			$("a", "#pen_preview_4").unbind("click");
			
			/***
				$("#pen_preview_5", "#hbar_5").unbind("click");
				$("#pen_preview_6", "#hbar_6").unbind("click");
				$("#pen_preview_7", "#hbar_7").unbind("click");
			***/			
			// $(".miniColor_jqueryP", "#hmenubar").minicolors("destroy");
			$(".miniColor_jqueryP", "#drawToolbar").minicolors("destroy");
			
			// destroy _setSlider
			/***
				$('#penSizeSlider').slider("destroy");						
				$('#opacitySlider').slider("destroy");
				$("#epenSizeSlider").slider("destroy");
				$('#lpenSizeSlider').slider("destroy");
				$('#lopacitySlider').slider("destroy");
				$('#spenSizeSlider').slider("destroy");
				$('#sopacitySlider').slider("destroy");
				$('#cpenSizeSlider').slider("destroy");
				$('#copacitySlider').slider("destroy");
			***/ 
			
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
			
		/*}catch(e){
			// error exception
			console.log("Ctrl.destroy exception : " + e);
		} */
		
	},
	
	Mail : {
		sendList : [],
		openModal : function() {
			// Ctrl.Modal.hide('inviteModal');
			Ctrl.Modal.hideInvite();
			
			$(".poll_box").hide();
			var container = document.getElementById("invite_mail_modal");
			
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
										<img src=\""+roomThumbnail+"\" alt=\"\" class=\"thumbnail\" onerror=\"this.src='"+Utils.addResPath("images", "invite_default_thumb.png")+"'\" >\
										<div class=\"roominfo\">\
											<p class=\"tit\">KNOWLOUNGE</p>\
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
	login : false,
	userid : "",
	usernm : "",
	userno : "",
	roomid : "",
	clientip : "",
	deviceid : "",
	usertype : "",
	vcamopt : "0",
	creatorid : "",
	creatorno : "",
	parentcreatorno : "",
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
		this.usertype = options.usertype || '';
		this.vcamopt = options.auth.vcamopt || '0';
		this.creatorid = options.creatorid || '';
		this.creatorno = options.creatorno || '';
		this.creatornm = options.creatornm || '';
		this.parentcreatorno = options.parentcreatorno || '';
		
		this.roomthumbnail = options.roomthumbnail || '';
		
		// 2016.12.28 게스트 thumnail 공백으로 보냄
		if(this.userid == this.userno){
			this.thumbnail = "";
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
		this.socket.on('sendbroadcast', this.sendbroadcast);
		this.socket.on('newuser', this.newuser);
		this.socket.on('leaveuser', this.leaveuser);
		this.socket.on('roomlist', this.roomlist);
		this.socket.on('roomlistEx', this.roomlistEx);
		this.socket.on('userlist', this.userlist);
		this.socket.on('roomuserlist', this.roomuserlist);		
		this.socket.on("disconnect", this.disconnect);		
		this.socket.on("newvc", this.newvc);
		this.socket.on("newvcf", this.newvcf);
		this.socket.on("streamvideo", this.streamvideo);
		this.socket.on("kickuser", this.kickuser);		
		this.socket.on("error", this.error);
		
		// 모바일은 isKLounge를 무조건 true로 놓고 쓸것.
		if(Utils.isKLounge() ){
			this.socket.on("newclassuser", this.newclassuser);
			this.socket.on("leaveclassuser", this.leaveclassuser);
		}		
	},
	connect : function() {		
		// cmd : adduser -> userid, roomid, roomname, username, userno, deviceid, svctype(0-우리,1-타기관)
		// svctype 0-우리회의, 1-타기관회의
		var videoStatus = VideoCtrl.checkSupport() ? "1" : "0";
		var vCamOpt = RoomSvr.vcamopt;
		var creatorId = RoomSvr.creatorid;
		var creatorNm = RoomSvr.creatornm;
		var separate = CanvasApp.info.separate; 
		
		this.emit("adduser", '' + RoomSvr.userid + '','' + RoomSvr.roomid + '', ''+RoomSvr.roomtitle+'', '' + RoomSvr.usernm + '', '' + RoomSvr.userno + '', ''+ RoomSvr.deviceid +'', ''+ RoomSvr.thumbnail +'', ''+ RoomSvr.clientip +'', ''+ RoomSvr.usertype +'', ''+videoStatus+'', ''+vCamOpt+'', ''+creatorId+'', ''+creatorNm+'', ''+separate+'');
		
		// this.emit("sendmsg", '{"cmd":"enterroom", "userid":"' + RoomSvr.userid+ '", "roomid":"' + RoomSvr.roomid + '","username":"'+ RoomSvr.usernm + '","userno":"' + RoomSvr.userno + '"}');		
	},	
	
	error : function(){
		alert("KNOWLOUNGE Room Server 연결에 실패하였습니다.");
		location.href = Utils.addContext("home");
	},
	
	disconnect : function(){		
// Utils.log("disconnect enter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		// Ctrl.Member.setOnline("disconnect", false, true);
		Ctrl.setMyNetworkStatus(false, true);
	},
	
	send : function(data) {
// Utils.log("send data : " + JSON.stringify(data));
		if(PacketMgr.isKicked) return;
		
		if(typeof(data) != "undefined" && data != null && data != ""){
			var json = JSON.parse(data);
			Utils.logger(json.cmd, "패킷전송", (new Date().toString() + ", millisecond : " + new Date().getTime()));
						
			// 룸서버로 보낼때는 data는 json string 형태로 보내야 한다.			
 			this.socket.emit("sendmsg", data);			
		}
	},
	
	sendbroadcast : function(mode, roomId, data){
Utils.log("sendbroadcast : " + mode + ", roomId : " + roomId + ", data : " + data);
		if(PacketMgr.isKicked) return;

		// 상대방 방에 브로드 케스팅을 보내는 테스트 
		if(typeof(data) != "undefined" && data != null && data != ""){
			// 룸서버로 보낼때는 data는 json string 형태로 보내야 한다.			
 			this.socket.emit("sendbroadcast", mode, roomId, data);			
		}		
	},	
	
	sendmsg : function(userid, data) {
		// 서버에서 내려오는 패킷 draw
 		PacketMgr.receive(userid, data);
	},
		
	kickuser : function(roomid, userno, userid, username, kicktype){
// console.log("kickuser roomid : " + roomid + ", userid : " + userid + ", username : " + username);
// console.log("roomid : " + roomid + ", PacketMgr.userid : " + PacketMgr.userid + ", userId : " + userid);
		/**
		 * kicktype이 0번이면 이전에 접속한 내 계정의 세션을 끊는다.
		 */		

		if(RoomSvr.roomid == roomid){
			if(PacketMgr.userid == userid){ 
				PacketMgr.isKicked = true;
				Ctrl.Loader.showF();

				CanvasApp.destroy();

				location.href = Utils.addContext("main");
				
				if(kicktype == null || kicktype == '0') alert(_msg("msg.connect.another.device"));

/***
				if(kicktype == "1"){
					if(PacketMgr.isGuest){
						Utils.Local.remove("guest");	
					}
					// 화면에서 지운다.
					RoomSvr.leaveuser(userid, username, userno);					
					// 영상을 끊어준다.
				}
***/
				
			}else{
				// socket이 붙어있으면 leave시키지만 안붙어 있는 경우를 대비해서 leave시킨다.
				RoomSvr.leaveuser(userid, username, userno);
			}
		}
	},
	// userid, username, userno, isGuest, thumbnail
	newuser : function(userid, username, userno, guest, thumbnail, usertype, isvideoallow, fullscreenuserid) {
// Utils.log("enter newuser! userid : " + userid + ", username : " + username + ", userno : " + userno + ", guest : " + guest);
		// 내정보만 getdata로 보내야 한다.
		/** 히스토리를 서버 베이스로 저장하기 때문에 이 로직은 제외한다.
		if(RoomSvr.userid == userid){
			this.emit("sendmsg", '{"cmd":"getdata","userid":"' + RoomSvr.userid+ '","usernm":"' + RoomSvr.usernm + '"}');
		}
		**/
		
		RoomSvr.login = true;
		
		// 마스터가 접속한 경우 마지막 파일하고 페이지 broadcast 한다.
		Ctrl.Member.newUser(userid, username, userno, guest, thumbnail, usertype, isvideoallow, fullscreenuserid);		
		// WebRTC 부분..
	},
	
	leaveuser : function(userId, userName, userNo) {		
		Ctrl.Member.leaveUser(userId, userName, userNo);		
		// fullsize check때문에
		if(!CanvasApp.useZico){
			VideoCtrl.destroy(userNo, userId);	
		}
		
		// layer 삭제 필요함.
		// if(!Utils.browser("msie")) VideoCtrl.destroy(userNo);
		
	},
	
	// 수업 참여 callback
	newclassuser : function(roomId, userId, userName, userNo, isGuest, thumbnail, userRoomId, userRoomSeqNo, userType, connectedRoomTitle, connectedRoomCreatorName, connectedRoomSeparate){
		if(Utils.isKLounge()){
			Ctrl.Member.addClassUser(roomId, userId, userName, userNo, isGuest, thumbnail, userRoomId, userRoomSeqNo, userType, connectedRoomTitle, connectedRoomCreatorName, connectedRoomSeparate);	
		}		
	},
	
	leaveclassuser : function(roomId, userId, userName, userNo){
		if(Utils.isKLounge() ){
			Ctrl.Member.leaveClassUser(roomId, userId, userName, userNo);
		}
	},
	
	// web rtc initialize connection pair
	newvc : function(roomId, user1, user2, data, user1_video_allow, user2_video_allow, user1_mic_status, user2_mic_status){
console.log("newvc roomid : " + roomId + ", user1 : " + user1 + ", user2 : " + user2 + ", data : " + JSON.stringify(data));
// Utils.log('user1_video_allow user1 : ' + user1 + ', user1_video_allow : ' + user1_video_allow );
// Utils.log('user2_video_allow user2 : ' + user2 + ', user2_video_allow : '+ user2_video_allow );
// console.log("newvc idx", idx);
// console.log("user1_mic_status : " + user1_mic_status);
// console.log("user2_mic_status : " + user2_mic_status);
		if(CanvasApp.useZico) return;

		// && PacketMgr.usertype != "2" && !PacketMgr.isCreator) ? false : true;
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
		var isDisplay = ((mode == "offer" && user1_video_allow == 0) || (mode == "answer" && user2_video_allow == 0))? false : true;
		// var isMicOn = ((mode == "offer" && user1_mic_status == 0) || (mode == "answer" && user2_mic_status == 0))? false : true;
		var micStatus = mode == "offer" ? user1_mic_status : user2_mic_status;
		
		if(!Utils.isKLounge()) isDiaplay = true;
		
		if(mode != ""){
			var targetUserNo = (mode == "answer") ? user2 : user1;			
			// 상대 유저가 마스터 인지 여부
			var isMC = PacketMgr.masterno == targetUserNo ? true : false;
			VideoCtrl.initPeerMedia(mode, RoomSvr.roomid, param, isMC, isDisplay, micStatus);
		}
		
	},
	newvcf : function(roomId, user1, user2, data, user1_video_allow, user2_video_allow, user1_mic_status, user2_mic_status){
		if(!Utils.isKLounge()) return;
		
Utils.log("newvcf roomid : " + roomId + ", user1 : " + user1 + ", user2 : " + user2 + ", data : " + JSON.stringify(data));
Utils.log('newvcf user1_video_allow user1 : ' + user1 + ', user1_video_allow : ' + user1_video_allow );
Utils.log('newvcf user2_video_allow user2 : ' + user2 + ', user2_video_allow : '+ user2_video_allow );

		if(CanvasApp.useZico) return;
		
		// 1. user가 없는 유저면 pass 시킨다.
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
		var isDisplay = ((mode == "offer" && user1_video_allow == 0) || (mode == "answer" && user2_video_allow == 0))? false : true;
		
		if(mode == "answer"){
			var data = VideoCtrl.map.get(RoomSvr.userno);
			var myDisplay = user1_video_allow == 1 ? true : false;
			if(data) data.show(myDisplay);
		}	

		var targetUserNo = (mode == "answer") ? user2 : user1;
		var isMC = PacketMgr.masterno == targetUserNo ? true : false;

		VideoCtrl.initPeerMedia(mode, RoomSvr.roomid, param, isMC, isDisplay);
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
	isAnimation : false,
	confirmAction : false,
	ckPassword : false,
	useZico : false,
	mcPenInterval : null,
	count : 0,	
	init : function(){
		var deviceId = Utils.getDevice();
		// layout은 video/doc 모드중 어떤 화면을 보고 있는지에 대한 값으로, changeMasterㅇ ㅘ changeWidhtDraw에서 무조건 날려줘야 한다.
		var codeStr = location.pathname.split("/").pop();
		CanvasApp.getRoomInfo(codeStr);
		
		this.setEvent();
		
		if($("#useZico").get(0) != null && $("#useZico").val() == "1"){
			CanvasApp.useZico = true;	
		}		
	},
	
	initSvc : function(isGuest, guestName){
		var sessionCnt = $("#sessionCnt").val();		
		if(CanvasApp.info.passwdflag == "1" && !this.ckPassword && !PacketMgr.isCreator && sessionCnt < 1){
			Ctrl.Modal.password(function(){
				CanvasApp.ckPassword = true;
				CanvasApp.initSvc(isGuest, guestName);				
			});
			return;
		}
		
		CanvasApp.loadHistory();
		
		if(isGuest){
			var guestName = guestName != "" ? guestName : $("#guest_nm").val();
			if(guestName.trim() == "") {
				Ctrl.Msg.show(_msg("insert.guest.name"));
				return;
			}
			
			var guestThumb = Utils.addResPath("images", "thum_user.png");
			var info = this.info;
			this.info.userid = this.info.deviceid;
			this.info.userno = this.info.deviceid;
			// this.info.usernm = guestName + "("+_msg("guest") +")";
			this.info.usernm = guestName;
			this.info.thumbnail = guestThumb;
			
			Utils.Local.set("guest", guestName);
			
			// roomserver connect
			CanvasApp.initInfo();
			
			Ctrl.Modal.hide("loginModal");			
			// if(!Utils.mobile()) Avgrund.hide("#loginModal");
			// Ctrl.avgrund(false, "loginModal");
			// oRomSvr.newclassuser(roomId, userId, userName, userNo, isGuest, thumbnail, userRoomId, userRoomSeqNo, userType){
			// guest 클래스 최초 접속				
			RoomSvr.newclassuser(info.roomid, info.userid, guestName, info.userno, '1', guestThumb, '', '', '');
			
			// guest 접속 
			VideoCtrl.guestFullScreen( (info.vcamfullscreen == "1") ? true : false );
		}else{

			// roomserver connect
			CanvasApp.initInfo();
			
		}
				
		// this.isCreator = this.creatorid == this.userid ? true : false;		
		var isCreator = (CanvasApp.info.creatorid == CanvasApp.info.userid) ? true : false;
		
		var len = CanvasApp.info.user != null ? CanvasApp.info.user.length : 0;
		if(!Utils.isKLounge() ){
			var currentUserInfo = Ctrl.Member.getUserOnline(info.userno, "userno");		
			if(isGuest) currentUserInfo.usernm = guestName;
			
			VideoCtrl.initMyMedia(CanvasApp.info.roomid, currentUserInfo, PacketMgr.isMC, true);	
		}
		
		var isGuide = Utils.Local.get("guide");
		if(typeof(isGuide) == "undefined" || isGuide == null){
			$("#guide").show();
			if($("#quickLink").get(0) != null){
				$("#quickLinkGuide").show();
			}
		}
		
		/***
			if(PacketMgr.isMC){ // 진행권한이 있을경우 펜세팅으로 시작. 룸 최소 생성시 펜세팅의 약간의 딜레이가 필요하다.
				setTimeout(function(){
					console.log('go toggle RC');
					Ctrl.toggleRC(1, -1, true);
				}, 500);
			}
		***/		
	},
	
	initInfo : function(){
		var info = CanvasApp.info;
		// room server control
		RoomSvr.init(info);		

		// packet control
		PacketMgr.init(info);
		
		this.initVideo();		
		// memo는 class base라 권한여부를 다시 체크해줘야ㅕ 한다.
		// Ctrl.Memo.auth();

		// 영상 공유
		// Ctrl.VShare.auth();		
	},
	
	initVideo : function(){
		if(CanvasApp.useZico){
			/*
				var statusInfo = {
					video : "ON",
					mic : "ON",
					speaker : "ON"
				}			
				VideoCtrl.init(statusInfo);
			*/			
			VideoCtrl.Popup.show();			
		}
	},
	
	initMCPensetting : function(){
		/**
		if(!CanvasApp.initFlag){
			console.log('go toggle RC');
			Ctrl.toggleRC(1, -1, true);
			CanvasApp.initFlag = true; 
		}
		**/
		if(CanvasApp.count > 4) return;
		
		// new user 뒤에 호출되어야 한다.
		if(PacketMgr.isMC){ // 진행권한이 있을경우 펜세팅으로 시작. 룸 최소 생성시 펜세팅의 약간의 딜레이가 필요하다.
			// last pen이 현재 펜과 다른 경우 펜세팅을 해준다.
			// 만약 로그인이 안된 상태라면 timer로 딜레이를 줘야 한다. 
			// 히스토리가 없어서 룸에 바로 접속했는데 newUser callback보다 히스토리 드로잉이 먼저 끝난 경우 아래와 같이 로그인 재귀호출 방식으로 처리 한다.
			if(!RoomSvr.login){
				// Ctrl.toggleRC(1, -1, true);
				// this.mcPenInterval =
				setTimeout(function(){
					CanvasApp.count++;
					CanvasApp.initMCPensetting();
				}, 500);				
			}else{
				// Ctrl.toggleRC(1, -1, true);
				$("#penCtl").trigger("click");
				
				Ctrl.hideToolMenu();
			}
		}
	},
	
	isLogin : function(){
		if(Utils.Cookie.get("FBMC") != null && Utils.Cookie.get("FBCS") != null){
			// 로그인 입ㅈ 
			return true;
		}else{
			// 게스트 입장 
			return false;
		}
	},
	
	setEvent : function(){
		$("#guide").click(function(){
			Utils.Local.set("guide", "1");
			$("#guide").hide();			
		});
	},
	
	checkSession : function(callback){
		var roomId = this.info.roomid;
		var code = this.info.code;

		var url = Utils.addContext( _url("room.check"));
		var params = {
			roomid : roomId,
			code : code
		};

		Utils.request(url, "json", params, function(data){
			var result = data.result;
			if(result == _code("SUCCESS")){
				CanvasApp.initSvc(true, '');
				return;
			}else if(result == _code("ROOM_USER_LIMITED")){
				Ctrl.Msg.show(_msg("msg.user.count.limited"));	
			}else if(result == _code("NOT_SUPPORT_BROWSER")){
				Ctrl.Msg.show(_msg("msg.support.browser"));
			}else if(result == _code("INVALID_ROOM")){
				Ctrl.Msg.show(_msg("msg.not.found.room"));
			}else if(result == _code("INCORRECT_PASSWORD")){
				Ctrl.Msg.show(_msg("msg.incorrect.passwd"));
			}else if(result == _code("ROOM_USER_MAX_LIMITED")){
				Ctrl.Msg.show(_msg("msg.user.count.max.limited"));
			}else{
				Ctrl.Msg.show(_msg("msg.exception"));
			}			
			
			setTimeout(function(){
				location.href = Utils.addContext("home");
			}, "2000");
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
		
		var tokenStr = rsa.encrypt(CanvasApp.info.roomid + "," + passwdStr);
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

		
		var startTime = new Date().getTime();
		Utils.log("캔버스 GET 시작  : ", (new Date().toString() + ", millisecond : " + startTime));

		var url = Utils.addContext(_url("canvas.get"));
		Utils.request(url, "json", param, function(data){
			if(data.result == '0') {
				var endTime = new Date().getTime();
				Utils.log("캔버스 GET 끝  : ", (new Date().toString() + ", millisecond : " + (endTime - startTime)));
					
				var info = data;
				info.deviceid = Utils.getDevice();
				 
				CanvasApp.info = info;
				
				UI.current = info.currentpageid;
				PacketMgr.lastPageId = info.currentpageid;
				
				// 2017.02.08 메모 때문에 master 먼저 세팅해줌.
				PacketMgr.isMC = info.masterid == info.userid ? true : false;
				
				// room initialize
				// if(CanvasApp.isLogin()) CanvasApp.initInfo();
				
				// canvas control
				UI.init(info.currentpageid, info.pageLimit);

				// draw tool control
				Ctrl.init();
				
				AttendPopup.init(info);
				
				VideoCtrl.limit = info.videoLimit;

				if(info.userid == "" && info.userno == ""){
					var guestName = Utils.Local.get("guest") || "";
					if(guestName != ""){
						/**
							if(info.vcamfullscreen == "1"){
								$("#videoContainer").addClass("video_fullsize");
							}
						**/
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
	
	loadHistory : function(){		
		var isAnimation = CanvasApp.isAnimation;				
		if(isAnimation){
			var maxWidth = parseInt($("#progTxt").width()) - 20;				
			Ctrl.ProgressLoader.show(maxWidth);
			// Ctrl.Loader.show();
		}else{
			Ctrl.Loader.show();
		}

// Utils.logger("시작  : ", (new Date().toString() + ", millisecond : " + new Date().getTime()));
		
		PacketMgr.loadHistory(CanvasApp.info.roomid, UI.current, function(packetCnt){
			if(Utils.isKLounge() && packetCnt > PacketMgr.skipCnt){
				CanvasApp.drawEnd(false);
				CanvasApp.confirmDrawPacket("room");
			}else{
				Utils.runCallback(function(){
					PacketMgr._drawHistoryPacket(PacketMgr.lastPageId, isAnimation, function(){
						CanvasApp.drawEnd(false);						
						$("#loadHistory").hide();
						
						CanvasApp.initMCPensetting();
					});
				}, 100);
			}
		});
	},
	
	drawEnd : function(isRedraw){
		var isAnimation = CanvasApp.isAnimation;
		// 마지막 보고있는 페이지의 zoom 확인 (마스터가 들어온 경우만 사용)
		PacketMgr._drawLastZoomPacket(PacketMgr.lastPageId);

		// 팬세팅 맞춤
		if(!PacketMgr.isMC){
			// 마스터가 아닌경우 펜세팅 싱크 맞춰줘야 한다.
			// PacketMgr.Master.syncPensettingForce( PacketMgr._getLastPen() );					
			PacketMgr.pushQueue(PacketMgr._getLastPen());
		}
		// 새로 그린게 손모드이면 view모드로 change해줘야 한다.		
		if((!isRedraw || Ctrl.isHand()) && !PacketMgr.isMC) {
			Ctrl._callPensetting(4, false);			
			PacketMgr.Master.changeMode("view");
		}
		
		// queue shift
		PacketMgr.shiftQueue();
		
		if(!isAnimation) {
			Ctrl.Loader.hide();
			Ctrl.ProgressLoader.hide();
		}	
		
	},
	
	redrawHistory : function(){
		CanvasApp.confirmAction = false;
		
		var isAnimation = CanvasApp.isAnimation;
		if(isAnimation){
			var maxWidth = parseInt($("#progTxt").width()) - 20;				
			Ctrl.ProgressLoader.show(maxWidth);
			// Ctrl.Loader.show();
		}else{
			Ctrl.Loader.show();
		}
		
		// packet modal 삭제
		CanvasApp.hideHistoryLoading();

		Utils.runCallback(function(){			
			var startTime = new Date().getTime();
console.log("드로잉 시작  : ", (new Date().toString() + ", millisecond : " + startTime));			
			// loading이 ui thread를 가져가므로 settimeout으로 비동기 처리 한다.
			PacketMgr._drawHistoryPacket(PacketMgr.lastPageId, isAnimation, function(){
				CanvasApp.drawEnd(true);

				var endTime = new Date().getTime();
console.log("드로잉 끝  : ", (new Date().toString() + ", millisecond : " + (endTime - startTime)));

				$("#loadHistory").hide();	
				UI.Page.sync();
				
				CanvasApp.initMCPensetting();
			});			
		}, 100);
	},
	
	confirmDrawPacket : function(from){		
		var html = "<div id=\"historyLoading\" class=\"windowLoadingSkip on\">\
						<div class=\"windowLoadingSkipWrap\">\
							<div class=\"header\">"+_msg('klounge.history.loading.title')+"</div>\
							<p>"+_msg('klounge.history.loading.txt.1')+"</p>\
							<p class=\"description\">"+_msg('klounge.history.loading.txt.2')+"</p>\
							<a href=\"javascript:CanvasApp.redrawHistory();\" class=\"btn_loading_continue\">"+_msg('klounge.history.loading.confirm')+"</a>\
							<a href=\"javascript:CanvasApp.hideDrawPacketModal();\" class=\"btn_loading_cancel\">"+_msg('klounge.history.loading.cancel')+"</a>\
						</div>\
					</div>";
		
		$(document.body).append(html);		
		
		if(from == "page"){
			setTimeout(function(){
				UI.Page.sync();										
			}, "7000");
		}
		
	},
	
	hideDrawPacketModal : function(){
		// hideDrawPacketModal
		CanvasApp.confirmAction = false;
		this.hideHistoryLoading();
		UI.Page.sync();		
	},
	
	hideHistoryLoading : function(sync){
		$("#historyLoading").remove();		
	},
	
    drawNt : function(action, x, y, density, webViewWidth, webViewHeight){
        var id = "sketch" + UI.current;        
        var canvasWidth = document.getElementById(id).clientWidth;
        var canvasHeight = document.getElementById(id).clientHeight;
    	var canvasX = x / density;
    	var canvasY = y / density;

    	// UI.skboards[UI.current - 1]["asyncNt"](action, canvasX, canvasY);
    	
    	var board = UI.getBoard();
    	board["asyncNt"](action, canvasX, canvasY);
    },
    
    pinchZoomNt : function(scaleFactor){
    	var percent = Math.floor(scaleFactor * 100);
    	if(percent < 100) percent = 100;    	
    	if(percent > 500) percent = 500;    	 
    	Ctrl.setZoomVal(percent);
        PacketMgr.Master.zoom(percent, "1");
    },   
    setConfig : function(){
    	// 플랫폼 별로 구분이 필요하면 이곳에 데이터를 설정한다.
    	var useCanvasEvent = true;
    	Utils.setCordova(useCanvasEvent);
    },
    resize : function(){
    	// UI.skboards[UI.current - 1]["resize"]();
    	var board = UI.getBoard();
    	board["resize"]();
    },
    
    destroy : function(){    	
    	Ctrl.Loader.show();
    	
    	// ie는 버그있어서 destroy를 예외처리 해준다.
		if(!Utils.browser("msie")) {
			VideoCtrl.destroy();		
		}
		
		// IE는 이곳에서 destroy 해야 한다.
		RoomSvr.destroy();
		
		PacketMgr.destroy();
		
		UI.destroy();
		
		Ctrl.destroy();
		
    	$("#guide").unbind("click");    	    	
    }
}; 

$(document).ready(function() {
	Utils.logger("Service Initialize", new Date().toString(), "", "");
	CanvasApp.init();	
}); 
