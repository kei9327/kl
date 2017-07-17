(function(){
"use strict"
/**
 * 	 @title 	  : Wenote Draw Application
 * 	 @date 	      : 2016.07.11
 *   @author      : kim dong hyuck 
 * 	 @description : 영상 공유
 **/
var VShare = function(canvasContainer) {	
	var scrollTarget = canvasContainer;
	var container = null; 
	var idx = null;
	var isMC = false;
	var rgbIdx = null;
	var baseSize = [1024, 1024];
	// var baseSize = [1920, 1920];
	var minSize = [180, 140];
	// var baseVideoSize = [640, 485];
	var baseVideoSize = [430, 320];
 	var red = 0;
	var green = 0; 
	var blue = 0;
	var blankHeight = 100;
	var zIndex = 101;
	var player = null;
	var status = -1;	// yt player 상태값 	
	var loopCnt = 0;
	/**
		YT.PlayerState.ENDED
		YT.PlayerState.PLAYING
		YT.PlayerState.PAUSED
		YT.PlayerState.BUFFERING
		YT.PlayerState.CUED
	**/
	var isProcSave = false;
	var saveTimer = null;
	var isDrag = false;	
	var saveReady = false;
	var magicBoxList = null;
	
	function init(layer, ord){		

		console.log("v_share.js / init() - ord : " + ord);
		container = layer; 
		idx = parseInt(ord);
		isMC = PacketMgr.isMC;
		
		// isMC = flag;
		red = 0;
		green = 0;
		blue = 0; 
		
		
		getBaseVideo(true, function(historyHtml, guideHtml){

			console.log("v_share.js / getBaseVideo callback");
		 	$("ul.videoImportList", $(container)).html(historyHtml);
			$("ul.videoGuideList", $(container)).html(guideHtml);			
					
			var beforeX = $(container).position().left;
			var beforeY = $(container).position().top;
			beforeX = parseFloat($(container).css('left'));
			beforeY = parseFloat($(container).css('top'));

			setPosition(beforeX, beforeY);
			
			var size = ExCall.getSize();
			var w = size[0], h = size[1];
			if(w <= 1280) {
				$(container).removeClass("videoImportWrap");	
				$(container).addClass("videoImportWrap_1280");				
			}
			
			
			if(ExCall.getAuth()) show();

		    setVideo(true);
		    
		    setSize();
		     
		    setEvent();	    
		});
		 

	}	

	// 패킷 받아서 드로잉하는 경우
	function receive(type, parent, flag, vsno, seqno, title, content, left, top, changedStatus, ord, currentTime, userNo){
		
		console.log("[v_share.js / receive()] type : " + type + ", flag : " + flag + " vsno : " + vsno + ", content : " + content);

		if(!ExCall.getAuth()) return;   // 선생님만 영상공유 허용 기능 붙일 때 주석 풀것
		
		isMC = flag;

		var __draw = function(parent, idx, isMC, vsno, seqno, title, content, left, top, currentTime){
			var size = ExCall.getSize();
			var w = size[0], h = size[1];
			var wrapClass = "videoImportWrap";
			if(w <= 1280) wrapClass += "_1280";
			wrapClass += " addVideo";
			
			container = document.createElement("div");
			container.id = "vshare" + idx;
			container.className = wrapClass;
			container.style.zIndex = zIndex = 9999;
			/*container.style.left = left + "px";
			container.style.top = top + "px";*/ 			
			$(container).attr("vsno", vsno);
			$(container).attr("seqno", seqno); 		
			
			var isReadOnly = !isMC ? "readonly='readonly'" : "";
			// var disableClass = historyHtml == "" ? "dis" : "";
			
			//var link = _url("youtube.host") + content;	// 웹 버전..
			var link;
			if (!Utils.cordova()) {
				link = _url("youtube.host") + content;	
			} else {
				link = _prop("youtube.host") + content;	
			}
			
			var template = "<div class=\"videoImportTitle\">\
								<span class=\"videoTitleSpan\">"+_msg('videoshare.title')+"</span>\
								<a class=\"btn_videoClose\"></a>\
							</div>\
							<div class=\"videoImportURLFirstWrap\" style=\"display:none;\">\
								<div class=\"videoImportURLFirst\">\
									<label for=\"\">"+_msg('videoshare.body')+"</label>\
									<input type=\"text\" class=\"init_link\" value=\""+link+"\" placeholder=\""+_msg('videoshare.placeholder')+"\"/>\
								</div>\
								<div class=\"videoImportListWrap\">\
									<div class=\"videoImportListTab\">\
										<a class=\"history\">"+_msg('videoshare.history')+"</a>\
										<a class=\"guide on\">"+_msg('videoshare.guide')+"</a>\
									</div>\
									<ul class=\"videoImportList\" style=\"display:none;\"></ul>\
									<ul class=\"videoGuideList\"></ul>\
								</div>\
								<a class=\"btn_videoImport_Import import\">"+_msg('videoshare.import')+"</a>\
							</div>\
							<div class=\"vs_player\"></div>"; 
			
			$(container).html(template);
			
			$("#" + parent).append(container); 
			
			setPosition(left, top);
			
			setVideo(true);
			
			setEvent();			
			
			setSize();
		}
		
		var __update = function(title, link, left, top, changedStatus, ord, currentTime, userNo){
			// $(".memo_text", $(container)).val(txt );
			// $(".title", $(container)).val(title);
			$(".videoImportTitle > span", $(container)).html(title);
			$(".init_link", $(container)).val(link);
			
			if(left > -1 && top > -1){
				setPosition(left, top);
			}
			
			if(status != changedStatus){
				status = changedStatus;
				iframeControl(changedStatus, currentTime);
			}
		}
		
		if(type == '0'){	// 최초 패킷으로 insert
			__draw(parent, ord, flag, vsno, seqno, title, content, left, top, currentTime, userNo);			
		}else if(type == '1'){			
			__update(title, content, left, top, changedStatus, ord, currentTime, userNo);			
		}else if(type == '2'){
			destroyUser();
		}
		
	}
	
	function getBaseVideo(initFlag, callback){
		console.log("[v_share.js / getBaseVideo] initFlag : " + initFlag);
		var makeHtml = function(){
			console.log("[v_share.js / getBaseVideo > makeHtml]");
			var historyList = magicBoxList;			 
			var historyHtml = "";		
			var len = historyList == null ? 0 : historyList.length;
			for(var i=0; i<len; i++){
				var mediaKey = historyList[i].mediakey;
				var youtubeThumb = "https://img.youtube.com/vi/" + mediaKey + "/2.jpg";
					
				historyHtml += "<li key=\""+mediaKey+"\">\
									<a><img src=\""+youtubeThumb+"\" alt=\"\"></a>\
								</li>";
			} 
			
			var guideHtml = "";
			var videos = ["FSqgPcX7xks", "93BxFYXmVXg", "shoXoMA9Q4s", "pQODrVFGKRg", "WVFxAfP-gnk"];
			var guideLen = videos.length;
			var guideHtml = "";
			
			for(var i=0; i<guideLen; i++){
				var mediaKey = videos[i];
				var youtubeThumb = "https://img.youtube.com/vi/" + mediaKey + "/2.jpg";
					
				guideHtml += "<li key=\""+mediaKey+"\">\
									<a><img src=\""+youtubeThumb+"\" alt=\"\"></a>\
								</li>";
			}
			
			callback(historyHtml, guideHtml);
		} 
		
		if(initFlag) {
			
			//var url = Utils.addContext(_url("magicbox.list"));
			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("magicbox.list");
			
			
			var data = {
				userno : PacketMgr.userno
			};

			// 웹앱용 ajax
			if(Utils.cordova()) {
				$.ajax({
	                type  : "GET",
	                url   : url,
	                data : data,
					beforeSend : function(xhr) {
						xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
					},
	                success : function(data) {
	                    console.log(data);
	                    if(data.result == 0) {
							magicBoxList = data.map.history_list;
						} else {
							magicBoxList = [];
						}
	                },
	                fail : function() {
						console.log("magicbox/list.json fail..");
						magicBoxList = [];
					},
					error : function() {
						console.log("magicbox/list.json error..");
						magicBoxList = [];
					},
					complete : function() {
						makeHtml();
					}
	            });
			} else {
				Utils.request(url, "json", null, function(json){
					console.log(JSON.stringify(json));
					magicBoxList = json.list;
					makeHtml();
				});		
			}
			
			/*
			Utils.request(url, "json", null, function(json){
				console.log(JSON.stringify(json));
				magicBoxList = json.list;
				makeHtml();
			});	
			*/
			
		} else {
			makeHtml();
		}		
	}
	
	function draw(parent, ord, flag){

		console.log("[v_share.js / draw]");
		isMC = flag;
		idx = parseInt(ord);
		
		/**
		var rgbIdx = Math.floor(Math.random() * 8);
		var r = rgb[rgbIdx].r;
		var g = rgb[rgbIdx].g;
		var b = rgb[rgbIdx].b;
		**/
		var size = ExCall.getSize();
		var w = size[0], h = size[1];
		var wrapClass = "videoImportWrap";
		if(w <= 1280) wrapClass += "_1280";
		
		container = document.createElement("div");
		container.id = "vshare" + (idx);
		container.className = wrapClass;		
		/*
		container.style.left = ((w / 2) - (640 / 2)) + "px"; 
		container.style.top = "320px"; 
		*/
		// container.style.zIndex = (idx+1);
		container.style.width = "auto";
		container.style.zIndex = zIndex = 9999;
		// container.style.background = "rgb("+r+","+g+","+b+")";
		
		var draw = function(historyHtml, guideHtml){
			console.log("v_share.js / draw / draw");
			var disableClass = historyHtml == "" ? "dis" : "";
			var template = "<div class=\"videoImportTitle\">\
								<span class=\"videoTitleSpan\">"+_msg('videoshare.title')+"</span>\
								<a class=\"btn_videoClose\"></a>\
							</div>\
							<div class=\"videoImportURLFirstWrap\">\
								<div class=\"videoImportURLFirst\">\
									<label for=\"\">"+_msg('videoshare.body')+"</label>\
									<input type=\"text\" class=\"init_link\" value=\"\" placeholder=\""+_msg('videoshare.placeholder')+"\" />\
								</div>\
								<div class=\"videoImportListWrap\">\
									<div class=\"videoImportListTab\">\
										<a class=\"history "+disableClass+"\">"+_msg('videoshare.history')+"</a>\
										<a class=\"guide on\">"+_msg('videoshare.guide')+"</a>\
									</div>\
									<ul class=\"videoImportList\" style=\"display:none;\">"+historyHtml+"</ul>\
									<ul class=\"videoGuideList\">"+guideHtml+"</ul>\
								</div>\
								<a class=\"btn_videoImport_Import import\">"+_msg('videoshare.import')+"</a>\
							</div>\
							<div class=\"vs_player\"></div>"; 
			
			$(container).html(template);
			
			$("#" + parent).append(container); 

			console.log("w : " + w + ", vshare container width : " + $(container).width());
			console.log("h : " + h + ", vshare container height : " + $(container).height());

			var physicalScreenWidth = window.screen.width;
			var physicalScreenHeight = window.screen.height;
			// var physicalScreenWidth = window.screen.width * window.devicePixelRatio;
			// var physicalScreenHeight = window.screen.height * window.devicePixelRatio;
			console.log("physicalScreenWidth : " + physicalScreenWidth);
			console.log("physicalScreenHeight : " + physicalScreenHeight);

			var left = (w / 2) - ($(container).width() / 2); 
			var top = (h / 4) - ($(container).height() / 2) + (document.body.scrollTop || document.body.scrollTop);
			var posX = _getFixedX(w, h, left);
		    var posY = _getFixedY(w, h, top);
	
			setPosition(posX, posY)
			
			setEvent();		
			
			setSize();

			if(historyHtml != ""){
				// 히스토리 있으면 히스토리 동작.
				$("a.history", $(container)).trigger("click");
			}
		}
		
		getBaseVideo((magicBoxList == null ? true : false), function(historyHtml, guideHtml){
			draw(historyHtml, guideHtml);
		});
	} 
	
	// 앱에서 호출하는 draw 함수
	function drawForApp(parent, ord, flag, title, videoId) {
		console.log("[v_share.js /drawForApp] videoId : " + videoId);
		isMC = flag;
		idx = parseInt(ord);
		
		/**
		var rgbIdx = Math.floor(Math.random() * 8);
		var r = rgb[rgbIdx].r;
		var g = rgb[rgbIdx].g;
		var b = rgb[rgbIdx].b;
		**/
		var size = ExCall.getSize();
		var w = size[0], h = size[1];
		var wrapClass = "videoImportWrap";
		if(w <= 1280) wrapClass += "_1280";
		
		container = document.createElement("div");
		container.id = "vshare" + (idx);
		container.className = wrapClass;		
		/*
		container.style.left = ((w / 2) - (640 / 2)) + "px"; 
		container.style.top = "320px"; 
		*/
		// container.style.zIndex = (idx+1);
		container.style.width = "auto";
		container.style.zIndex = zIndex = 9999;
		container.style.display = "none";
		// container.style.background = "rgb("+r+","+g+","+b+")";
		
		var draw = function(historyHtml, guideHtml){
			console.log("[v_share.js] drawForApp > draw()");
			var disableClass = historyHtml == "" ? "dis" : "";
			var template = "<div class=\"videoImportTitle\">\
								<span class=\"videoTitleSpan\">"+_msg('videoshare.title')+"</span>\
								<a class=\"btn_videoClose\"></a>\
							</div>\
							<div class=\"videoImportURLFirstWrap\">\
								<div class=\"videoImportURLFirst\">\
									<label for=\"\">"+_msg('videoshare.body')+"</label>\
									<input type=\"text\" class=\"init_link\" value=\"\" placeholder=\""+_msg('videoshare.placeholder')+"\" />\
								</div>\
								<div class=\"videoImportListWrap\">\
									<div class=\"videoImportListTab\">\
										<a class=\"history "+disableClass+"\">"+_msg('videoshare.history')+"</a>\
										<a class=\"guide on\">"+_msg('videoshare.guide')+"</a>\
									</div>\
									<ul class=\"videoImportList\" style=\"display:none;\">"+historyHtml+"</ul>\
									<ul class=\"videoGuideList\">"+guideHtml+"</ul>\
								</div>\
								<a class=\"btn_videoImport_Import import\">"+_msg('videoshare.import')+"</a>\
							</div>\
							<div class=\"vs_player\"></div>"; 
			
			$(container).html(template);
			
			$("#" + parent).append(container); 

			console.log("[v_share.js] w : " + w + ", vshare container width : " + $(container).width());
			console.log("[v_share.js] h : " + h + ", vshare container height : " + $(container).height());

			var left = (w / 2) - ($(container).width() / 2); 
			var top = (h / 4) - ($(container).height() / 2) + (document.body.scrollTop || document.body.scrollTop);
			var posX = _getFixedX(w, h, left);
		    var posY = _getFixedY(w, h, top);
	
			setPosition(posX, posY)
			
			setEvent();		
			
			setSize();

			if(historyHtml != ""){
				// 히스토리 있으면 히스토리 동작.
				$("a.history", $(container)).trigger("click");
			}
		}
		
		getBaseVideo((magicBoxList == null ? true : false), function(historyHtml, guideHtml){
			draw(historyHtml, guideHtml);
		});


		
		setTimeout(function(){
			attachVideo(title, videoId);
		}, 300);
	}

	function setEvent(){
		if(isMC) {
			setCreatorEvent(false);		
		}else{
			setUserEvent();
		}
	}
	
	function changeMC(flag){		
		// 이전에 마스터 였다면		
		if(isMC && !flag){			
			$(container).draggable("destroy");
			
			var code = _getCode();
			if(code != ""){
				$(".btn_videoClose", $(container)).hide();
				$(".btn_videoClose", $(container)).unbind("click");
			}
			
			$(".import", $(container)).unbind("click");
			
			$("li", $(container)).unbind("click");
			
		}else if(!isMC && flag){
			$(".btn_videoClose", $(container)).show();
			setCreatorEvent(false);
		}
		
		isMC = flag;		
	}
	
	function changeStatus(changed, currentTime, userNo){
		console.log("[v_share.js] changeStatus - changed : " + changed);
		if(changed == -1) return;
		// -1 : 시작되지 않음, 0-정지, 1-시작, 2-일시정지
		status = changed;
		userNo = userNo || "";
				
		var seqNo = $(container).attr("seqno");
		var vsNo = $(container).attr("vsno");
		// var title =  $(".title", $(container)).val();
		var title = $(".videoImportTitle > span", $(container)).html();
		// var link = $(".init_link", $(container)).val().trim();
		
		var code = _getCode();
	    var idx = 1;
	    
		ExCall.draw("1", vsNo, seqNo, title, code, -1, -1, status, idx, currentTime, userNo);
	}
	
	function iframeControl(status, currentTime){	
		currentTime = parseInt(currentTime);
		
		if(typeof(player.getCurrentTime) == "undefined"){
			setTimeout(function(){
				++loopCnt;
				if(loopCnt >= 5) return;
				
				iframeControl(status, currentTime + 0.5);				
			}, 500);
			return;
		}
		
		loopCnt = 0;
		
		var myTime = parseInt(player.getCurrentTime());
		var termTime = Math.abs(parseInt(currentTime - myTime));
		if(status != '0' && termTime > 5){   // 정지 상태일때는 seekTo()가 실행되지 않도록 함
			player.seekTo(currentTime, true);	
		}
		
		if(status == '0'){	// stop
			// player.stopVideo();
		}else if(status == '1'){	// play
			player.playVideo();
			// iframeControl
		}else if(status == '2'){	// pause
			player.pauseVideo();
		}
			 
				
	}
	
	function setUserEvent(){
		// $(".btn_minimize", $(container)).click(fold);	
		// $(".btn_videoClose", $(container)).click(remove);
		/**
		$(container).mousedown(setOrder);
		**/
		// $(".btn_videoClose", $(container)).hide();
		var code = _getCode();
		if(code != ""){
			$(".btn_videoClose", $(container)).hide();
			$(".btn_videoClose", $(container)).unbind("click");	
		}
		
		// $(".videoImportWindowMenu", $(container)).hide();
		// $(".videoImportTitle", $(container)).hide();
	}
	function setCreatorEvent(isSkip){		 
		// $(".videoImportWindowMenu", $(container)).show();
		// $(".videoImportTitle", $(container)).show();

		// $(".videoImportWindowMenu", $(container)).css("visibility", "visible");		 
		if(!isSkip){
			$(".btn_videoClose", $(container)).show();
			$(".btn_videoClose", $(container)).unbind("click");
			$(".btn_videoClose", $(container)).click(remove);			
		}
		// $(".btn_videoImportEdit", $(container)).click(setup);
		// $(".edit", $(container)).click(save);
		
		$("li", $(container)).click(function(){
			var key = $(this).attr("key");
			var link = "https://youtu.be/" + key;
			
			$(".init_link", $(container)).val(link);
			
			$(".import", $(container)).trigger("click");
		});			
			
		
		$(".videoImportListTab > a", $(container)).click(function(){
			var disabled = $(this).hasClass("dis");
			if(!disabled){
				$(".videoImportListTab > a", $(container)).removeClass("on");
				$(this).addClass("on");
				
				if($(this).index() == 0){
					$("ul.videoImportList", $(container)).show();
					$("ul.videoGuideList", $(container)).hide();
				}else{
					$("ul.videoImportList", $(container)).hide();
					$("ul.videoGuideList", $(container)).show();
				}						
			}
			
		});
		
		$(".import", $(container)).click(function(){
			var link = $(".init_link", $(container)).val().trim();
			if(link == ""){
				Ctrl.Msg.show(_msg("videoshare.empty"));
				return;
			}
			 
			if(link.indexOf("youtube.com") < 0 && link.indexOf("youtu.be") < 0){
				Ctrl.Msg.show(_msg("videoshare.invalid"));
				$(".init_link", $(container)).val("");
				$(".init_link", $(container)).focus();
				return;
			}
			
			// set iframe replace
			if(link.indexOf("iframe") > -1 && link.indexOf("src=") > -1){
				var fIdx = link.indexOf("src=");
				var fToken = link.substring(fIdx + 5, link.length);
				console.log("[v_share.js] fToken : " + fToken);
				
				link = fToken.substring(0, fToken.indexOf(" ") -1);
				console.log("[v_share.js] link : " + link);
				$(".init_link", $(container)).val(link);
			}
			
			$(container).addClass("addVideo");
			
			setVideo(false);

			/**
			var parserIdx = (link.indexOf("?v=") > -1) ? link.indexOf("?v=") + 2 : (link.lastIndexOf("/") > -1) ? link.lastIndexOf("/") : -1;  
			if(parserIdx < 0 || parserIdx >= link.length) return;
			
			var code = link.substring(parserIdx + 1, link.length);
			**/
			
			
			var code = _getCode();

			//var url = Utils.addContext(_url("magicbox.save"));

			var url;
			if(!Utils.cordova()) {
				url = Utils.addContext(_url("magicbox.save"));
			} else {
				var svrFlag = _prop("svr.flag");
				var svrHost = _prop("svr.host." + svrFlag);
				url = svrHost + _prop("magicbox.save");
			}

			var params = {					
				roomid : PacketMgr.roomid,
				mediakey : code
			};
			
			Utils.request(url, "json", params, function(json){
				Utils.log("history save result : " + json.result);
			});
		});
		 
		
		console.log("[v_share.js] draggable event fired..");
		console.log(container);
		$(container).draggable({
			handle: ".videoTitleSpan",
			containment: $('#docWrapper'),
			start : function(e){
				isDrag = true;

				if(Utils.browser("msie")) {
					var container = document.getElementById(UI.CONTAINER);
					$(this).data("startingScrollTop", $(container).scrollTop());
				}
			},
			drag : function(e, ui){
				if(Utils.browser("msie")){
					var st = parseInt($(this).data("startingScrollTop"));
					ui.position.top += st;
				} 
			},
			
			stop : function(e, ui){	 
				var link = $(".init_link", $(container)).val().trim();
				// var title =  $(".title", $(this)).val();
				var title = $(".videoImportTitle > span", $(container)).html();
				var left = $(this).position().left;
				var top = $(this).position().top;
				var seqNo = $(this).attr("seqno");
				var vsNo = $(this).attr("vsno"); 

				if(isMC){
					var w = $("#" + UI.WRAPPER).width();	
					var h = $("#" + UI.WRAPPER).height();
					var w2 = $("#" + Ctrl.Comment.id_widget).width();
					var h2 = $("#" + Ctrl.Comment.id_widget).height();

					var posX = (left > (w - w2)) ? (w - w2) : left;					
					var posY = (top > (h + 10)) ? (h + 10) : top;
					
				    var scrollNode = document.getElementById(scrollTarget);
				    posX += (scrollNode.scrollLeft || scrollNode.scrollLeft)
				    posY += (scrollNode.scrollTop || scrollNode.scrollTop)

				    // 상대 좌표로 변경
				    var size = ExCall.getSize();
				    var w = size[0], h = size[1];
					posX = _getFixedX(w, h, posX);
				    posY = _getFixedY(w, h, posY);

					//var url = Utils.addContext(_url("plugin.update.pos"));
					var svrFlag = _prop("svr.flag");
					var svrHost = _prop("svr.host." + svrFlag);
					var url = svrHost + _prop("plugin.update.pos");
					
					//console.log("[v_share.js] url : " + url);
					var params = {
						roomid : PacketMgr.roomid,
						pageid : UI.current,
						seqno : seqNo,						
						posx : posX,
						posy : posY,
						ord : 1,
						userno : PacketMgr.userno
					};
					//console.log("[v_share.js] params : " + JSON.stringify(params));
					
					Utils.request(url, "json", params,  function(json) {
						//console.log("[v_share.js] update.json result : " + JSON.stringify(json));
						var code = _getCode();
						ExCall.draw("1", vsNo, seqNo, title, code, posX, posY, status, idx, -1);
						isDrag = false;
					});					
				}				 
			}
		}); 
	}
	
	function add(callback){		
		// var title =  $(".title", $(container)).val();
		var title = $(".videoImportTitle > span", $(container)).html();
		// var txt = $(".memo_text", $(container)).val();
		var link = $(".init_link", $(container)).val().trim();

		console.log("[v_share.js / add] init_link value : " + $(".init_link", $(container)).val());

		var left = $(container).position().left;
		var top = $(container).position().top;
		
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		//var url = Utils.addContext(_url("vshare.add"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("vshare.add");

		//console.log("[add()] url : " + url);
		var data = {
			seqno : "",	// 없음
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			title : title,
			content : _getCode(),
			deviceid : Utils.getDevice(),
			left : posX,
			top : posY,
			ord : 1,
			userno : PacketMgr.userno
		};

		//console.log("[add()] param : " + JSON.stringify(data));
		
		Utils.request(url, "json", data, function(json){
			if(json && json.result == '0'){
				//console.log("[add()] json : " + JSON.stringify(json));
				 $(container).attr("seqno", json.seqno);
				 $(container).attr("vsno", json.map.commentno);

//				 $(".memo_text", $(container)).val(txt);
				 var code = _getCode();
				 ExCall.draw("0", json.map.commentno, json.seqno, title, code, posX, posY, status, idx, 0);
			 
				 if(typeof(callback) != "undefined" && typeof(callback) == "function"){
					 callback();
				 }	
			}			
		});
	}


	/**
 	 * attachVideo
	 *   - videoId : 유튜브 비디오 아이디 값
	 **/
	function attachVideo(title, videoId) {
		console.log("[v_share.js / attachVideo] videoId : " + videoId);
		
		if(!ExCall.getAuth()){
			Ctrl.Msg.show(_msg("m.auth.msg.1"));	
			return;
		}

		if(videoId == "" || videoId == null || typeof videoId == "undefined"){
			Ctrl.Msg.show(_msg("videoshare.empty"));
			return;
		}
		
		var link = _prop("youtube.host") + videoId;
		$(".init_link", $(container)).val(link);
		console.log("[v_share.js / attachVideo] videoId : " + link);
		
		// set iframe replace
		if(link.indexOf("iframe") > -1 && link.indexOf("src=") > -1){
			var fIdx = link.indexOf("src=");
			var fToken = link.substring(fIdx + 5, link.length);
			
			link = fToken.substring(0, fToken.indexOf(" ") -1);
			$(".init_link", $(container)).val(link);
		}

		console.log("[v_share.js / attachVideo] init_link value : " + $(".init_link", $(container)).val(link));
		
		$(container).addClass("addVideo");
		
		setVideo(false);

		/**
		var parserIdx = (link.indexOf("?v=") > -1) ? link.indexOf("?v=") + 2 : (link.lastIndexOf("/") > -1) ? link.lastIndexOf("/") : -1;  
		if(parserIdx < 0 || parserIdx >= link.length) return;
		
		var code = link.substring(parserIdx + 1, link.length);
		**/
		
		var code = _getCode();
		var url;
		if(!Utils.cordova()) {
			url = Utils.addContext(_url("magicbox.save"));
			var params = {					
				roomid : PacketMgr.roomid,
				mediakey : code
			}
			
			Utils.request(url, "json", params, function(json){
				Utils.log("history save result : " + json.result);
			});
		} else {
			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			url = svrHost + _prop("magicbox.save");
			var params = {					
				roomid : PacketMgr.roomid,
				mediakey : code,
				title : title
			}
			
			console.log("[v_share.js] magicbox/save.json params : " + JSON.stringify(params));

			Utils.request(url, "json", params, function(json){
				console.log("history save result : " + json.result);
			});
		}

		$(container).show();
	}

	
	function update(callback){

		var vsNo = $(container).attr("vsno");
		var seqNo = $(container).attr("seqno");
		// var title =  $(".title", $(container)).val();
		var title = $(".videoImportTitle > span", $(container)).html();
		var link = $(".init_link", $(container)).val().trim();
		var left = $(container).position().left;
		var top = $(container).position().top;

		console.log("[v_share.js / update] init_link value : " + $(".init_link", $(container)).val());
		 
		/**
		var r = bgColor[0];
		var g = bgColor[1];
		var b = bgColor[2];
		var foldFlag = isFold();
		**/
		
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		var url;
		if(!Utils.cordova()) {
			url = Utils.addContext(_url("vshare.save"));
		} else {
			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			url = svrHost + _prop("vshare.save");
		}

		var data = {
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			vsno : vsNo,
			title : title,
			content : _getCode(),
			userno : PacketMgr.userno		
		};
		
		Utils.request(url, "json", data, function(json){
			if(json && json.result == '0'){				
 				// $(".title_txt", $(container)).html( title.escape() ); 				  
				// ExCall.draw("1", memoNo, seqNo, title, txt, posX, posY, r, g, b, foldFlag, idx);
				
				if(typeof(callback) != "undefined" && typeof(callback) == "function"){
					 callback();
				 }
				
				return;
			}			
		});
	}
	
	function remove(){
		if(isMC){
			// if(!confirm(_msg("confirm.remove.memo"))) return;			
			ExCall.confirm(_msg("videoshare.confirm.remove"), function(){
				if(!ExCall.getMC()){
					Ctrl.Msg.show(_msg("not.allow"));
					return;
				}
				// confirm.remove.memo
				var vsNo = $(container).attr("vsno");
				var seqNo = $(container).attr("seqno");
				var title = "";
				var link = $(".init_link", $(container)).val().trim();
				var left = -1;
				var top = -1;	
				
				// 데이터 저장없는 상태에서 바로 지운경우
				if(typeof(seqNo) == "undefined" || seqNo == null || (seqNo == "" && vsNo == "")){
					destroy();					
					ExCall.remove();
				}else{
					
					var url;
					if(!Utils.cordova()) {
						url = Utils.addContext(_url("vshare.remove"));
					} else {
						var svrFlag = _prop("svr.flag");
						var svrHost = _prop("svr.host." + svrFlag);
						url = svrHost + _prop("vshare.remove");
					}

					var data = {
						roomid : PacketMgr.roomid,
						seqno : seqNo,
						userno : PacketMgr.userno
					}
					
					Utils.request(url, "json", data, function(json){
						if(json && json.result == '0'){
							var code = _getCode();							
							ExCall.draw("2", vsNo, seqNo, title, code, left, top, status, idx);
							destroy();
							ExCall.remove();
						}
					});
				}
			});
			
		}else{
			var code = _getCode();			
			if(code != ""){
				// alert(_msg("not.allow"));
				ExCall.alert(_msg("not.allow"));								
			}else{
				if(isMC) {
					destroy();	
				}else{
					destroyUser();
				}
			}
		}
	}


	function removeWithCallback(onSuccess) {
		console.log("[v_share.js/removeWithCallback]");
		if(!ExCall.getAuth()){
			Ctrl.Msg.show(_msg("m.auth.msg.1"));	
			return;
		}
		
		console.log("[v_share.js/removeWithCallback] isMC : " + isMC);
		if(isMC){
			ExCall.confirm(_msg("videoshare.confirm.remove"), function(){
				console.log("[v_share.js/removeWithCallback] PacketMgr.isMC : " + PacketMgr.isMC);
				if(!ExCall.getMC()){
					Ctrl.Msg.show(_msg("not.allow"));
					return;
				}
				// confirm.remove.memo
				var vsNo = $(container).attr("vsno");
				var seqNo = $(container).attr("seqno");
				var title = "";
				var link = $(".init_link", $(container)).val().trim();
				var left = -1;
				var top = -1;	
				
				// 데이터 저장없는 상태에서 바로 지운경우
				if(typeof(seqNo) == "undefined" || seqNo == null || (seqNo == "" && vsNo == "")){
					destroy();					
					ExCall.remove();
				}else{
					
					var url;
					if(!Utils.cordova()) {
						url = Utils.addContext(_url("vshare.remove"));
					} else {
						var svrFlag = _prop("svr.flag");
						var svrHost = _prop("svr.host." + svrFlag);
						url = svrHost + _prop("vshare.remove");
					}

					var data = {
						roomid : PacketMgr.roomid,
						seqno : seqNo,
						userno : PacketMgr.userno
					}
					
					Utils.request(url, "json", data, function(json){
						if(json && json.result == '0'){
							console.log("[v_share/removeWithCallback] vshare/remove.json result : " + JSON.stringify(json));

							var code = _getCode();							
							ExCall.draw("2", vsNo, seqNo, title, code, left, top, status, idx);
							destroy();
							ExCall.remove();
							if(onSuccess) {
								// 완벽한 삭제를 보장해주기 위해 timeout을 줌..
								setTimeout(function(){
									onSuccess();
								}, 300);

							}
						}
					}, function(error) {
						console.log("[memo.js/remove] memo/remove.json error : " + JSON.stringify(error));
						Ctrl.Msg.show(_msg("msg.exception"), "LONG");
					}, function(xhr) {
						xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
					});
				}
			});
			
		}else{
			if(!ExCall.getMC()){
				Ctrl.Msg.show(_msg("not.allow"));
				return;
			}
			var code = _getCode();			
			if(code != ""){
				// alert(_msg("not.allow"));
				ExCall.alert(_msg("not.allow"));								
			}else{
				if(isMC) {
					destroy();	
				}else{
					destroyUser();
				}
			}
		}
	}

	
	function save(callback){		
		var seqNo = $(container).attr("seqno");
		if(seqNo != null && seqNo > 0){
			// update
			update(callback);			
		}else{
			// insert
			add(callback);
		}
	}
	
	function setup(){
		// toggle setup
		// $(".mset_box", $(container)).toggle();
		if($(".videoImportInputWrap", $(container)).hasClass("on")){
			 $(".videoImportInputWrap", $(container)).removeClass("on")
		}else{
			 $(".videoImportInputWrap", $(container)).addClass("on")
		}
	}
	
	function hideSetup(){
		 $(".videoImportInputWrap", $(container)).removeClass("on");
	}
	
	function setVideo(isSaved){		
		
		if(player != null) return;
		
		var link = "";
		if(isSaved){
			link = $(".init_link", $(container)).val().trim();	
		}else{
			$(".videoImportURLFirstWrap", $(container)).hide();

			console.log("[v_share.js / setVideo] init_link value : " + $(".init_link", $(container)).val());
			
			link = (typeof $(".init_link", $(container)) != 'undefined') ? $(".init_link", $(container)).val().trim() : '';			
			// save();			
			saveReady = true;
		}
				
		
		var code = _getCode();
		var playerId = "vs_" + code;
		console.log("[v_share.js / setVideo()] code : " + code);
		// var params = (videoIdx == 0 ? "" : "?enablejspi=0&loop=1");
		// params += params == "" ? "?modestbranding=1" : "&modestbranding=1";
		$(".vs_player", $(container)).attr("id", playerId);
		
		var size = ExCall.getSize();
		console.log("[size] size[0] : " + size[0] + ", size[1] : " + size[1]);
		
		
		var width = baseVideoSize[0] * (size[0] / baseSize[0]);
	    var height = baseVideoSize[1] * (size[1] / baseSize[1]);

		// 영상 모드 일때 ifame 위아래 blank를 없애기 위함
	    // height -= $(".videoImportWindowMenu", $(container)).height() ;    
	    //height -= $(".videoImportTitle", $(container)).height() ;
	    // width -= 100;
	    
		console.log("[v_share.js] setVideo / height : " + height);
		console.log("[v_share.js] setVideo / screen height : " + window.screen.height);
		console.log("[v_share.js] setVideo / screen width : " + window.screen.width);

		if(window.screen.height < 800 && window.screen.width < 800) {
			height -= 70;
		} else {
			height -= blankHeight;
		}

		player = new YT.Player(playerId, {
			width: width,
			height: height,				
			videoId: code,
			playerVars: {
			    controls: 1,
			    showinfo: 0,
			    modestbranding: 1,
			    autoplay : 0,
			    enablejsapi : 0,
			    loop : 1,
			    rel : 0
			},
			events: {
				'onReady': Ctrl.VShare.onPlayerReady,
				'onStateChange': Ctrl.VShare.onPlayerStateChange
			}
		});
		
		// 모바일에서 타이틀 너비 강제로 맞추기.. (CSS에 웹기준으로 작성된 내용이 있기 때문..)
		$(".videoTitleSpan").css("width", (width-50) + "px");

		setSize();		
	}


	function setTitle(title){
		// $(".videoImportURLFirst", $(container)).val(link);
		// $(".link", $(container)).val(link);		
		$(".videoImportTitle > span", $(container)).html(title);
		
		// 저장 해야되는 callback이라면
		if(saveReady){
			save(function(){				
				saveReady = false; 
			});			
		}
		
	}
	
	function setSize(){
		var size = ExCall.getSize();
		var width = baseVideoSize[0] * (size[0] / baseSize[0]);
	    var height = baseVideoSize[1] * (size[1] / baseSize[1]);
		// $(container).css({width:width, height:height});		
		
		console.log("[v_share.js] setSize / width : " + width + ", height : " + height);

		// 영상 모드 일때 ifame 위아래 blank를 없애기 위함
		var code = _getCode();
		if(code != ""){
			$(container).css("width", "auto");

			console.log("[v_share.js] setSize / width : auto");
			// $(container).height( $(container).height() - blankHeight);	
		}		
	}
	
	function setPosition(beforeX, beforeY){
		console.log("[v_share.js] setPosition / beforeX : " + beforeX + ", beforeY : " + beforeY);
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];	    
	    var x = _getOrgX(w, h, beforeX);
	    var y = _getOrgY(w, h, beforeY);

		console.log("[v_share.js] setPosition / left x : " + x + ", top y : " + y);
		
		/*
		if(Utils.cordova()) {
			var physicalScreenHeight = window.screen.height;
			y = (physicalScreenHeight /2) - ($(container).height() / 2);
		}*/

		// 모바일 디바이스의 스크린 가로/세로 값 읽어오기..
		/*
		if(Utils.cordova()) {
			var physicalScreenWidth = window.screen.width;
			var physicalScreenHeight = window.screen.height;

			if(physicalScreenWidth < physicalScreenHeight && physicalScreenWidth <= w) {
				x = 0;
			}
		}
		*/

	    $(container).css("left", x);
		$(container).css("top", y);
	}
		
	function get(type){
		var vsNo = $(container).attr("vsno");
		var seqNo = $(container).attr("seqno");
		
		return (type == "seqno") ? seqNo : (type == "vsno") ? vsNo : idx;  
	}
	
	function getPlayer(){
		return player;
	}
	
	function getOrd(){
		return idx;
	}
		
	function _getFixedX(w, h, dx){
		var scaleX = (w > h) ? (1024 / w) : (768 / w);
		return dx * scaleX;
	}
	
	function _getFixedY(w, h, dy){
		var scaleY = (w > h) ? (748 / h) : (1004 / h);
		return dy * scaleY;
	}
	
	function _getOrgX(w, h, dx){
		var orgScaleX = (w > h) ? (w / 1024) : (w / 768);
		return dx * orgScaleX;
	}
	
	function _getOrgY(w, h, dy){
		var orgScaleY = (w > h) ? (h / 748) : (h / 1004);
		return dy * orgScaleY;
	}	
	
	function _getCode(){
		
		var link = $(".init_link", $(container)).val().trim();
		console.log("[v_share.js / _getCode] link : " + link);
		if(link == "") return "";
		
		var parserIdx = (link.indexOf("?v=") > -1) ? link.indexOf("?v=") + 2 : (link.lastIndexOf("/") > -1) ? link.lastIndexOf("/") : -1;  
		if(parserIdx < 0 || parserIdx >= link.length) return;
		
		return link.substring(parserIdx + 1, link.length);
	}
	
	function show(){
		// 2016.11.01 - video 컨테이너의 visibility 속성을 display 속성으로 변경함 (IOS 웹뷰에서 visibility 속성이 제대로 동작하지 않는 이슈때문에..)
		$(container).css("display", "block");
		//$(container).css("visibility", "visible");
	}
	
	function hide(){
		// 2016.11.01 - video 컨테이너의 visibility 속성을 display 속성으로 변경함 (IOS 웹뷰에서 visibility 속성이 제대로 동작하지 않는 이슈때문에..)
		$(container).css("display", "none");
		//$(container).css("visibility", "hidden");
	}

    function destroy(){
		console.log("[v_share.js / destroy]");
		console.log(container);
    	try{
    		$(".btn_videoClose", $(container)).unbind("click");

    		$(".import", $(container)).unbind("click");
     		 
    		$("li", $(container)).unbind("click");	
    					
    		$(".videoImportListTab > a", $(container)).unbind("click");
    		 
    		$(container).draggable("destroy");
    		
    		$(container).remove();
    		
    	}catch(e){
    		console.log(e);
    	}
		
    	delete this; 
    }
    
    function destroyUser(){ 
		 
		$(container).remove();
		
    	delete this;
    }
    
    var ExCall = { 
       	draw : function(type, vsNo, seqNo, title, link, left, top, status, ord, currentTime, newUserNo){
       		// type, seqno, title, link, left, top, r, g, b, fold, ord
       		PacketMgr.Master.vShare(type, vsNo, seqNo, title, link, left, top, status, ord, currentTime, newUserNo);
       	},
       	
       	maxOrd : function(){
       		var idx = Ctrl.VShare.getMax();
       		return idx;
       	},
       	
       	alert : function(msg){
       		Ctrl.Msg.show(msg);
       	},
       	
       	confirm : function(msg, callback){
       		Ctrl.Modal.confirm(msg, callback);
       	},
       	
       	getSize : function(){
       		//var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
       		var board = UI.getBoard();
       		var imgCanvas = board.getCanvas("img");
       		return [imgCanvas.width, imgCanvas.height];
       	},
       	remove : function(){
       		Ctrl.VShare.remove();
       	},
		
		getMC : function(){
       		return PacketMgr.isMC;
       	},
       	
       	getRoomId : function(){
       		return PacketMgr.roomid;
       	},
       	
       	getCurrentPageId : function(){
       		return UI.current;
       	},
       	
       	getAuth : function(){
       		// 사용자타입(0:개인, 1:학생, 2:선생)       		
			// return (PacketMgr.isOnlyTeacherVShare && PacketMgr.usertype != "2") ? false : true;
       		return (PacketMgr.isOnlyTeacherVShare && (!PacketMgr.isCreator && PacketMgr.usertype != "2")) ? false : true;

       	}
    };   
    
    return {
    	 init: init,      
         draw : draw,
		 drawForApp : drawForApp,
         get : get,
         getOrd : getOrd,
         changeMC : changeMC,
         changeStatus : changeStatus,
         getPlayer : getPlayer,
         setTitle : setTitle,
         receive : receive,
		 show : show,
         hide : hide,
         destroy : destroy,
         destroyUser : destroyUser,
 		 removeWithCallback : removeWithCallback  // 2016.10.12 추가 - 앱에서 사용하는 함수
    };
}; 

window.VShare = VShare;

})();