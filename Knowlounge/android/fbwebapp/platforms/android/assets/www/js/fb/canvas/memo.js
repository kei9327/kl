(function(){
"use strict"
/**
 * 	 @title 	  : Wenote Draw Application
 * 	 @date 	      : 2013.11.26
 *   @author      : kim dong hyuck 
 * 	 @description : 메모
 **/
var Memo = function(canvasContainer) {	
	var scrollTarget = canvasContainer;
	var container = null; 
	var idx = null;
	var isMC = false;
	var rgbIdx = null;
	// var rgb = [{r:255,g:255,b:255},{r:255,g:0,b:0},{r:228,g:93,b:3},{r:255,g:220,b:0},{r:101,g:181,b:0},{r:14,g:111,b:211},{r:103,g:10,b:207}];
	var minSize = [180, 140];
	var rgb = [{r:255,g:245,b:145},{r:182,g:216,b:132},{r:246,g:156,b:155},{r:106,g:203,b:221},{r:254,g:205,b:128},{r:189,g:128,b:185},{r:153,g:153,b:123},{r:235,g:235,b:235}];
	var red = 235;
	var green = 235;
	var blue = 235;
	var zIndex = 51;
	
	// var baseSize = [1280, 1280];
	var baseSize = [1280, 1280];
	var isProcSave = false;
	var saveTimer = null;
	var isDrag = false;
	var isAddLoading = false;
	
	function init(layer, ord, r, g, b){		
		container = layer;

		idx = parseInt(ord);
		
		// isMC = flag;
		red = r;
		green = g;
		blue = b; 
		
		// reposition
		var beforeX = $(container).position().left;
		var beforeY = $(container).position().top;
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];	    
	    var x = _getOrgX(w, h, beforeX);
	    var y = _getOrgY(w, h, beforeY);

		$(container).css("left", x);
		$(container).css("top", y);		
	    $(container).css("visibility", "visible");
	    
	    resize(container);
	    
	    setEvent();
	}	

	// 패킷 받아서 드로잉하는 경우
	function receive(type, parent, ord, flag, memono, seqno, title, content, left, top, r, g, b, fold){
		console.log("[memo.js/receive] content : " + content);
	    // 상대 좌표로 변경
	    var size = ExCall.getSize();
	    var w = size[0], h = size[1];

	    left = _getOrgX(w, h, left);
	    top = _getOrgY(w, h, top);
	    isMC = flag;
	    
		var __draw = function(parent, idx, isMC, memono, seqno, title, content, left, top, r, g, b, fold){
			var colorIdx = 0;
			var len = rgb == null ? 0 : rgb.length;
			for(var i=0; i<len; i++){
				var data = rgb[i];
				if(data.r == r && data.g == g && data.b == b){
					colorIdx = (i + 1);
				}				
			}
			 
			container = document.createElement("div");
			container.id = "memo" + idx;;
			container.className = "memo_container" + colorIdx + " memo";
			container.style.zIndex = zIndex = (parseInt(idx) + 51);
			
			$(container).css({left:left+ "px", top:top+ "px"});
			$(container).css("background", "rgb("+r+","+g+","+b+")");
			$(container).attr("seqno", seqno);
			$(container).attr("memono", memono); 		
			
			/**
			var template = "<div class=\"widget_memoWrap\" style=\"background-color:rgb("+r+","+g+","+b+")\">\
								<div class=\"widget_title\">\
									"+ _msg('memo') + "\
									<a href=\"javascript:void(0)\" class=\"widget_fold\"></a>\
									<a href=\"javascript:void(0)\" class=\"widget_close\"></a>\
								</div>\
								<div class=\"widget_body\" style=\"padding:10px 10px 10px 10px;\">\
									<div class=\"memo_text_wrap\" style=\"display:none;\">\
										<textarea class=\"memo_text\" style=\"width:200px;height:150px; border: 1px solid red;\" value=\""+content+"\"></textarea><br />\
										<input class=\"memo_save\" type=\"button\" value=\""+_msg("save")+"\" /> <input class=\"memo_cancel\" type=\"button\" value=\""+_msg("cancel")+"\" />\
									</div>\
									<div class=\"memo_text\" style=\"border: 1px solid yellow; width:180px;height:180px;\">"+content.escape()+"</div>\
								</div>\
							</div>";
			**/
			
			var isReadOnly = !isMC ? "readonly='readonly'" : "";

			var foldClassStr = fold == "1" ? "memo_box_mini" : "memo_box"
			
			var template = "<div class=\"" + foldClassStr + "\">\
								<div class=\"mbox_header\">\
									<div class=\"memo_tit\">\
										<a title=\"minimize\" class=\"btn_minimize\"></a>\
										<span class=\"title_txt\">"+title+"</span>\
										<a title=\"delete\" class=\"btn_x\"></a>\
										<a title=\"setup\" class=\"btn_set\"></a>\
									</div>\
									<div class=\"mset_box\">\
										<span>"+_msg("title")+" :</span><input type=\"text\" class=\"title_input\" value=\""+title+"\" />\
										<div class=\"memo_colorbox\">\
											<a title=\"color\" class=\"btn_color1 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color2 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color3 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color4 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color5 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color6 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color7 memo_color\"></a>\
											<a title=\"color\" class=\"btn_color8 memo_color\"></a>\
										</div>\
									</div>\
								</div>\
								<div class=\"mbox_body\">\
									<textarea class=\"memo_text\" "+isReadOnly+">"+content+"</textarea>\
								</div>\
							</div>";
			
			$(container).html(template);
			
			$("#" + parent).append(container); 
			    
			resize(container);
			
			setEvent();			
		}
		
		var __update = function(title, txt, left, top, r, g, b, fold, ord){
			$(".title_txt", $(container)).html(title.escape() );
			$(".title_input", $(container)).val(title);
			$(".memo_text", $(container)).val(txt );	
			
			if(left > -1 && top > -1) $(container).css({left:left+ "px", top:top+ "px"}); 
			
			if(r > -1 && g > -1 && b > -1) $(container).css("background", "rgb("+r+","+g+","+b+")");
		
			foldLayer(fold);
			
			setOrderLayer(ord);
		}
		
		if(type == '0'){			
			__draw(parent, ord, flag, memono, seqno, title, content, left, top, r, g, b, fold);			
		}else if(type == '1'){			
			__update(title, content, left, top, r, g, b, fold, ord);			
		}else if(type == '2'){
			destroyUser();
		}
		
	}
	
	function draw(parent, ord, flag){
		isMC = flag;
		idx = parseInt(ord);
		 
		var rgbIdx = Math.floor(Math.random() * 8);
		var r = rgb[rgbIdx].r;
		var g = rgb[rgbIdx].g;
		var b = rgb[rgbIdx].b;
		
		// idx = ord + 1;
		
		container = document.createElement("div");
		container.id = "memo" + (idx);
		container.className = "memo_container" + (rgbIdx+1) + " memo";
		container.style.left = "10px";
		container.style.top = "65px";
		// container.style.zIndex = (idx+1);
		container.style.zIndex = zIndex = (idx + 51);
		container.style.background = "rgb("+r+","+g+","+b+")";
		

		var template = "<div class=\"memo_box\">\
							<div class=\"mbox_header\">\
								<div class=\"memo_tit\">\
									<a title=\"minimize\" class=\"btn_minimize\"></a>\
									<span class=\"title_txt\">Memo</span>\
									<a title=\"delete\" class=\"btn_x\"></a>\
									<a title=\"setup\" class=\"btn_set\"></a>\
								</div>\
								<div class=\"mset_box\">\
									<span>"+_msg("title")+" :</span><input type=\"text\" class=\"title_input\" value=\"Memo\" />\
									<div class=\"memo_colorbox\">\
										<a title=\"color\" class=\"btn_color1 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color2 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color3 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color4 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color5 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color6 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color7 memo_color\"></a>\
										<a title=\"color\" class=\"btn_color8 memo_color\"></a>\
									</div>\
								</div>\
							</div>\
							<div class=\"mbox_body\">\
								<textarea class=\"memo_text\"></textarea>\
							</div>\
						</div>";

		$(container).html(template);
		
		$("#" + parent).append(container); 

		resize(container);
		    
//		alert(width + " " + height);
		
		setEvent();		
	}

	/**
	function toggleMode(flag){
		if(flag == "1"){	// hide
			$(".memo_text_wrap", $(container)).hide();
			$(".memo_text", $(container)).show();
		}else{
			$(".memo_input_wrap", $(container)).show();
			$(".memo_text", $(container)).hide();
		}		
	}
	**/
	
	function resize(container){
		var beforeW = $(container).width();
		var beforeH = $(container).height();

		var size = ExCall.getSize();
		var w = size[0], h = size[1];	     
		
		var customScale = (w / baseSize[0] >= 1) ? 1 : 1.5;  
		var width = minSize[0] * (w / baseSize[0]) * customScale;
		var height = minSize[1] * (h / baseSize[1]) * customScale;

		$(".memo_text", container).width(width);
		$(".memo_text", container).height(height);
	}
	
	function foldLayer(flag){
		console.log("[memo.js / foldLayer] flag : " + flag);
		if(flag == "0"){
			$(".memo_box_mini", $(container)).removeClass("memo_box_mini").addClass("memo_box");
		}else{			
			$(".memo_box", $(container)).removeClass("memo_box").addClass("memo_box_mini");
		}		
	}
	
	function isFold(){		
		var layer =  $(".memo_box", $(container)).get(0);
		return layer == null ? "1" : "0";
	}
	
	function fold(){		
		if(isDrag) return;
		
		if(typeof($(container).attr("seqno")) == "undefined"){			
			// 메모가 최초 추가시 1초간 상태 저장 시간을 갖는다.
			setTimeout(fold, "100");
			return;
		}
		
		var flag = "";
		if($(".memo_box", $(container)).get(0) != null ){ 
			$(".memo_box", $(container)).removeClass("memo_box").addClass("memo_box_mini");
			flag = "1";
		}else if($(".memo_box_mini", $(container)).get(0) != null ){
			$(".memo_box_mini", $(container)).removeClass("memo_box_mini").addClass("memo_box");
			flag = "0";
		}		
		
		var seqNo = $(container).attr("seqno");
		var memoNo = $(container).attr("memono");
		var seqNo = $(container).attr("seqno");
		var left = $(container).position().left;
		var top = $(container).position().top;			
		var txt = $(".memo_text", $(container)).val();
		var title =  $(".title_input", $(container)).val();
		var bgColor = $(container).css("backgroundColor");
		bgColor = bgColor.replace("rgb(", "").replace(")", "").split(",");
		
		var r = bgColor[0];
		var g = bgColor[1];
		var b = bgColor[2];
		
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		//var url = Utils.addContext(_url("memo.update.fold"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("memo.update.fold");

		var data = {
			seqno : seqNo,
			userno : CanvasApp.info.userno,
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			posx : left,
			posy : top,
			fold : flag
		}


		Utils.request(url, "json", data, function(json) {
			console.log("[memo.js/add] memo/update/fold.json result : " + JSON.stringify(json));
			if(json && json.result == '0'){				
				ExCall.draw("1", memoNo, seqNo, title, txt, posX, posY, r, g, b, flag, idx);
			}	
		}, function(error) {
			console.log("[memo.js/add] memo/update/fold.json error : " + JSON.stringify(error));
			Ctrl.Msg.show(_msg("msg.exception"), "LONG");
		}, function(xhr) {
			xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
		});
		
	}	

	function setOrder(){
		
		if(isDrag) return;
		
		var ord = ExCall.maxOrd();
		if(idx == ord) return;	
		
		idx = ord + 1;
		
		setOrderLayer(idx);
		
		var seqNo = $(container).attr("seqno");
		var memoNo = $(container).attr("memono");
		var seqNo = $(container).attr("seqno");
		var left = $(container).position().left;
		var top = $(container).position().top;			
		var txt = $(".memo_text", $(container)).val();
		var title =  $(".title_input", $(container)).val();
		var bgColor = $(container).css("backgroundColor");
		bgColor = bgColor.replace("rgb(", "").replace(")", "").split(",");
		var flag = isFold();
		
		var r = bgColor[0];
		var g = bgColor[1];
		var b = bgColor[2];
		
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		// var url = Utils.addContext(_url("memo.update.ord"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("memo.update.ord");

		var data = {
			seqno : seqNo,
			userno : CanvasApp.info.userno,
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			posx : left,
			posy : top,
			ord : idx
		}

		
		Utils.request(url, "json", data, function(json) {
			console.log("[memo.js/add] memo/update/ord.json result : " + JSON.stringify(json));
			if(json && json.result == '0'){				
				ExCall.draw("1", memoNo, seqNo, title, txt, posX, posY, r, g, b, flag, idx);
			}	
		}, function(error) {
			console.log("[memo.js/add] memo/update/ord.json error : " + JSON.stringify(error));
			Ctrl.Msg.show(_msg("msg.exception"), "LONG");
		}, function(xhr) {
			xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
		});
		
	}	
	
	function setOrderLayer(ord){
		$(container).css("zIndex", (zIndex = (parseInt(ord) + 50 + 1)));
	}
	
	function setEvent(){
		if(isMC) {
			setCreatorEvent(false);		
		}else{
			setUserEvent();
		}
	}
	
	function changeMC(flag){
		// isMC = flag;		7
		// 이전에 마스터 였다면
		if(isMC && !flag){
			console.log("메모 / 권한 변경됨");
			$(container).draggable("destroy");	    	
			/**
			$(".memo_save", $(container)).unbind("click", save);
			$(".memo_cancel", $(container)).unbind("click");
			$(".memo_text", $(container)).unbind("dblclick");
			**/ 
			
			$(".memo_tit", $(container)).css("cursor", "default");

			$(".memo_text", $(container)).attr("readonly", true);
			
			$(".btn_minimize", $(container)).unbind("click");
			
			$(".btn_set", $(container)).unbind("click");
			
			$(".memo_text", $(container)).unbind("blur");
			
			$(".title_input", $(container)).unbind("blur");
			
			$(".memo_color", $(container)).unbind("click")
			
			hideSetup();
			
		}else if(!isMC && flag){ 
			
			$(".memo_tit", $(container)).css("cursor", "move");

			$(".memo_text", $(container)).attr("readonly", false);
			
			setCreatorEvent(true);
		}
		
		isMC = flag;		
	}
	
	function setUserEvent(){
		// $(".btn_minimize", $(container)).click(fold);		
		
		$(".btn_x", $(container)).click(remove);
		/**
		$(container).mousedown(setOrder);
		**/
				
	}
	function setCreatorEvent(isSkip){
		if(!isSkip){
			$(".btn_x", $(container)).click(remove);
		}

		$(container).click(function(){
			setOrder();			
		});
		
		$(".btn_minimize", $(container)).click(fold);		
		
		$(".btn_set", $(container)).click(setup);
		
		$(".memo_text", $(container)).blur(save);
		
		$(".memo_text", $(container)).keyup(autoSave);
		
		$(".title_input", $(container)).blur(save);
		
		$(".memo_color", $(container)).click(function(idx){
			// var idx = $(this).index();
			 var idx = $(".memo_color", $(container)).index(this);
			changeColor(idx);
		});
		
		/**
		$(".memo_save", $(container)).click(save);

		$(".memo_cancel", $(container)).click(function(){			
			toggleMode("1");
		});

		$(".memo_text", $(container)).dblclick(function(){
			toggleMode("0");
		});
		
		$(".memo_color", $(container)).click(function(idx){
			// var idx = $(this).index();
			 var idx = $(".memo_color", $(container)).index(this);
			changeColor(idx);
		});
		**/
		$(container).draggable({
			//handle: ".memo_tit",
			handle: ".title_txt",
			containment: $('#docWrapper'),
			start : function(e){
				console.log("memo draggable start..");
				isDrag = true;
				UI.isTouchMemo = true;
			    var ord = ExCall.maxOrd() + 1;
				setOrderLayer(ord);
				
					
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
				var txt = $(".memo_text", $(this)).val();
				var title =  $(".title_input", $(this)).val();
				var bgColor = $(this).css("backgroundColor");
				bgColor = bgColor.replace("rgb(", "").replace(")", "").split(",");
				 
				var r = bgColor[0];
				var g = bgColor[1];
				var b = bgColor[2];
				var flag = isFold();

				var left = $(this).position().left;
				var top = $(this).position().top;			
				var seqNo = $(this).attr("seqno");

				var memoNo = $(this).attr("memono");
				var seqNo = $(this).attr("seqno"); 

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
				    
				    idx = ExCall.maxOrd() + 1;

					// var url = Utils.addContext(_url("memo.update.pos"));
					var svrFlag = _prop("svr.flag");
					var svrHost = _prop("svr.host." + svrFlag);
					var url = svrHost + _prop("memo.update.pos");

					var params = {
						seqno : seqNo,
						userno : CanvasApp.info.userno,
						roomid : PacketMgr.roomid,
						posx : posX,
						posy : posY,
						ord : idx
					};
					
					console.log(params);

					Utils.request(url, "json", params, function(json) {
						console.log("[memo.js/stop] memo/update/pos.json result : " + JSON.stringify(json));
						ExCall.draw("1", memoNo, seqNo, title, txt, posX, posY, r, g, b, flag, idx);
						
						isDrag = false;
						UI.isTouchMemo = false;
					}, function(error) {
						console.log("[memo.js/stop] memo/update/pos.json error : " + JSON.stringify(error));
						Ctrl.Msg.show(_msg("msg.exception"), "LONG");
					}, function(xhr) {
						xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
					});

				}				
			}
		});		
	}
	
	function add(callback){		
		var title =  $(".title_input", $(container)).val();
		var txt = $(".memo_text", $(container)).val();
		var left = $(container).position().left;
		var top = $(container).position().top;
		
		// memo_container1 
		var className = $(container).attr("class");
		var idx = parseInt(className.replace("memo_container", "").replace(" memo"), 10) - 1;
		idx = (idx < 0) ? 0 : idx;
		
		var rgbArr = rgb[idx];
		var r = rgbArr.r;
		var g = rgbArr.g;
		var b = rgbArr.b;

		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		/**
		if(txt.trim() == ""){
			alert(_msg("insert.memo"));			
			return;
		}
		**/		
console.log("isFold() : " + isFold());
		
		// var url = Utils.addContext(_url("memo.add"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("memo.add");

		var data = {
			seqno : "",	// 없음
			userno : CanvasApp.info.userno,
			userid : CanvasApp.info.userid,
			usernm : CanvasApp.info.usernm,
			ip : CanvasApp.info.clientip,
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			title : title,
			content : txt,
			deviceid : Utils.getDevice(),
			left : posX,
			top : posY,
			red : r, 
			green : g,
			blue : b,
			ord : idx,
			fold : isFold(),
		};
		
		console.log("[memo.js/add] data : " + JSON.stringify(data));
		

		Utils.request(url, "json", data, function(json) {
			console.log("[memo.js/add] memo/add.json result : " + JSON.stringify(json));
			if(json && json.result == '0'){
				console.log("memo/add.json result : " + JSON.stringify(json));
				// alert('seqno : ' + json.seqno);
				// $(".memo_text", $(container)).show();
				 $(container).attr("seqno", json.seqno);
				 $(container).attr("memono", json.map.commentno);
				 
				 $(".memo_text", $(container)).val(txt);
				 
				 // toggleMode("1");
											
				 ExCall.draw("0", json.map.commentno, json.seqno, title, txt, posX, posY, r, g, b, "0", idx);
				 
				 if(typeof(callback) != "undefined" && typeof(callback) == "function"){
					 callback();
				 }
			}
		}, function(error) {
			console.log("[memo.js/add] memo/add.json error : " + JSON.stringify(error));
			Ctrl.Msg.show(_msg("msg.exception"), "LONG");
		}, function(xhr) {
			xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
		});

	}
	
	function update(callback){		
		var memoNo = $(container).attr("memono");
		var seqNo = $(container).attr("seqno");
		var title =  $(".title_input", $(container)).val();
		var txt =  $(".memo_text", $(container)).val();
		var left = $(container).position().left;
		var top = $(container).position().top;			
		
		console.log("[memo.js/update] txt : " + txt);

		var bgColor = $(container).css("backgroundColor");
		bgColor = bgColor.replace("rgb(", "").replace(")", "").split(",");
		
		var r = bgColor[0];
		var g = bgColor[1];
		var b = bgColor[2];
		var foldFlag = isFold();
		
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		/**
		if(title.trim() == ""){
			alert(_msg("insert.m emo.title"));	
			return;
		}
		
		if(txt.trim() == ""){
			alert(_msg("insert.memo"));	
			return;
		}
		**/
		
		// var url = Utils.addContext(_url("memo.save"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("memo.save");

		var data = {
			userno : CanvasApp.info.userno,
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			memono : memoNo,
			title : title,
			content : txt				
		};

		
		
		Utils.request(url, "json", data, function(json) {
			console.log("[memo.js/update] memo/update.json result : " + JSON.stringify(json));
			if(json && json.result == '0'){				
				$(".title_txt", $(container)).html( title.escape() );
				  
				ExCall.draw("1", memoNo, seqNo, title, txt, posX, posY, r, g, b, foldFlag, idx);
				
				if(typeof(callback) != "undefined" && typeof(callback) == "function"){
					 callback();
				}
				
				return;
			}
		}, function(error) {
			console.log("[memo.js/update] memo/update.json error : " + JSON.stringify(error));
			Ctrl.Msg.show(_msg("msg.exception"), "LONG");
		}, function(xhr) {
			xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
		});
		
	}
	
	function remove(){
		
		if(isMC){
			// if(!confirm(_msg("confirm.remove.memo"))) return;
			
			ExCall.confirm(_msg("confirm.remove.memo"), function(){
				// confirm.remove.memo

				if(!PacketMgr.isMC){
					Ctrl.Msg.show(_msg("not.allow"));
					
					return;
				}

				var memoNo = $(container).attr("memono");
				var seqNo = $(container).attr("seqno");
				var title = "";
				var txt = "";
				var left = -1;
				var top = -1;	
				
				console.log("[memo.js / remove] seqNo : " + seqNo);
				// 데이터 저장없는 상태에서 바로 지운경우
				if(typeof(seqNo) == "undefined" || seqNo == null || seqNo == ""){
					destroy();
				}else{
					
					// var url = Utils.addContext(_url("memo.remove"));
					var svrFlag = _prop("svr.flag");
					var svrHost = _prop("svr.host." + svrFlag);
					var url = svrHost + _prop("memo.remove");
					
					var data = {
						userno : PacketMgr.userno,
						roomid : PacketMgr.roomid,
						pageid : UI.current,
						seqno : seqNo
					}
					
					Utils.request(url, "json", data, function(json) {
						console.log("[memo.js/remove] memo/remove.json result : " + JSON.stringify(json));
						if(json && json.result == '0'){
							ExCall.draw("2", memoNo, seqNo, title, txt, left, top, -1, -1, -1, "0", idx);
							destroy();				
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
			// alert(_msg("not.allow"));
			ExCall.alert(_msg("not.allow"));
		}
	}
	
	function save(){		
		console.log("[memo.js/save]");
		var seqNo = $(container).attr("seqno");
		if(seqNo != null && seqNo > 0){
			// update
			update();			
		}else{
			// insert
			add();
		}
	}
	
	function createSaveTimer(){
		saveTimer = setInterval(function(){
			if(isProcSave){
				isProcSave = false;
				return;
			}
			save();
			clearInterval(saveTimer);
			saveTimer = null;			
		}, "2000");
	}
	
	function autoSave(){
		if(isProcSave) return;
		isProcSave = true;
		
		if(saveTimer == null){
			createSaveTimer();
		}
	}
	
	function autoSaveSkip(){
		isProcSave = true;	
	}
	
	function setup(){
		// toggle setup
		// $(".mset_box", $(container)).toggle();
		if($(".mset_box", $(container)).hasClass("on")){
			 $(".mset_box", $(container)).removeClass("on")
		}else{
			 $(".mset_box", $(container)).addClass("on")
		}
	}
	
	function hideSetup(){
		 $(".mset_box", $(container)).removeClass("on");
	}
	
	function get(type){
		var memoNo = $(container).attr("memono");
		var seqNo = $(container).attr("seqno");
		
		return (type == "seqno") ? seqNo : (type == "memono") ? memoNo : idx;  
	}
	
	function getOrd(){
		return idx;
	}
	
	function changeColor(idx){		
		var seqNo = $(container).attr("seqno");
		var memoNo = $(container).attr("memono");
		var seqNo = $(container).attr("seqno");
		var title =  $(".title_input", $(container)).val();
		var txt =  $(".memo_text", $(container)).val();
		var left = $(container).position().left;
		var top = $(container).position().top;			
		
		var rgbArr = rgb[idx];
		var r = rgbArr.r;
		var g = rgbArr.g;
		var b = rgbArr.b;
		var flag = isFold();
		
		var size = ExCall.getSize();
	    var w = size[0], h = size[1];
	    var posX = _getFixedX(w, h, left);
	    var posY = _getFixedY(w, h, top);
	    
		if(typeof(seqNo) == "undefined" || seqNo == null){
			
			$(container).attr("class", "memo_container" + (idx+1) + " memo");
			$(container).css("background", "rgb("+r+","+g+","+b+")");

			red = r;
			green = g;
			blue = b;
			
			add();
			return;
		}
		
		// var url = Utils.addContext(_url("memo.update.color"));
		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("memo.update.color");

		var data = {
			seqno : seqNo,
			userno : CanvasApp.info.userno,
			roomid : PacketMgr.roomid,
			pageid : UI.current,
			posx : posX,
			posy : posY,
			red : r,
			green : g,
			blue : b
		}
		
		Utils.request(url, "json", data, function(json) {
			console.log("[memo.js/changeColor] memo/update/color.json result : " + JSON.stringify(json));
			if(json && json.result == '0'){
				$(container).attr("class", "memo_container" + (idx+1) + " memo");
				$(container).css("background", "rgb("+r+","+g+","+b+")");
				
				red = r;
				green = g;
				blue = b;
				
				ExCall.draw("1", memoNo, seqNo, title, txt, posX, posY, r, g, b, flag, idx);
			}	
		}, function(error) {
			console.log("[memo.js/changeColor] memo/update/color.json error : " + JSON.stringify(error));
			Ctrl.Msg.show(_msg("msg.exception"), "LONG");
		}, function(xhr) {
			xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
		});

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
	
    function destroy(){
    	try{
    		$(".btn_minimize", $(container)).unbind("click");		
    		
    		$(".btn_x", $(container)).unbind("click");
    		
    		$(".btn_set", $(container)).unbind("click");
    		
    		$(".memo_text", $(container)).unbind("blur");

    		$(".memo_text", $(container)).unbind("keyup");
    		
    		$(".title_input", $(container)).unbind("blur");
    		
    		$(".memo_color", $(container)).unbind("click");

    		if(saveTimer != null){
    			clearInterval(saveTimer);
    			saveTimer = null;
    		}

     		$(container).draggable("destroy");
    		$(container).unbind("click");
    		$(container).remove();
    		
    	}catch(e){
    		console.log(e);
    	}
    	
    	delete this;
    }
    
    function destroyUser(){
    	
		$(".btn_x", $(container)).unbind("click");
		
		// $(container).unbind("click");
		
		$(container).remove();
		
    	delete this;
    }
    
    var ExCall = {
       	draw : function(type, memoNo, seqNo, title, content, left, top, r, g, b, fold, ord){
       		PacketMgr.Master.memo(type, memoNo, seqNo, title, content, left, top, r, g, b, fold, ord);
       	},
       	
       	maxOrd : function(){
       		var idx = Ctrl.Memo.getMax();
       		return idx;
       	},
       	
       	alert : function(msg){
       		Ctrl.Msg.show(msg);
       	},
       	
       	confirm : function(msg, callback){
       		Ctrl.Modal.confirm(msg, callback);
       	},
       	
       	getSize : function(){
       		// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
       		var board = UI.getBoard();
       		var imgCanvas = board.getCanvas("img");
       		
       		return [imgCanvas.width, imgCanvas.height];
       	},
    };
    
    return {
        init: init,      
        draw : draw,
        get : get,
        getOrd : getOrd,
        changeMC : changeMC,
        receive : receive,
        destroy : destroy,
        destroyUser : destroyUser
    };
}; 

window.Memo = Memo;

})();