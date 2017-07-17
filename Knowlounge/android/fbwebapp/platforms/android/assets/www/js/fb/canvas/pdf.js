"use strict"
/**
 * @title : Pdf Viewer
 * @date : 2013.11.26
 * @author : kim dong hyuck
 * @description : PdfJS로 구현된 PDF viewer interface - IE9이상만 동작
 */
var PDFViewer = {
	WRAPPER : "pdfWrapper",
	SLIDER : "pdfSlider",
	VIEWER : "pdfWrap",
	SKETCH : "pdf",
	LOADER : "pdfLoading",
	PAGER : "pdfNavi",
	HANDLE : "pdfHandle",
	FIRST_HOLDER : "pdfHolder1",
	TITLE : "pdfTitle",
	INITPDF : 3,
	canvasWidth : 0,
	canvasHeight : 0,
	idx : 0,
	pdfs : null, // [{name:filename, url:downloadpath}, ...]
	sizeMap : [],
	pdf : null, // -- pdfjs object render list
	pwd : "",	
	current : 1, // 현재 바라보고 있는 pdf 파일
	initCnt : 3,
	renderScale : 0.8,	// 기본 캔버스에 최적화 시킬 배율 
	baseScale : 0, // 초기 고정된 해상도의 baseScale -> 기본배율 1
	zoomScale : 0, // zoom에 의해 변형된 scale -> zoomScale = newScale / baseScale
	scale : 1,
	lastZoomPacket : null, // zoom의 마지막 패킷
	move : 0, // animation 이동
	slideX : 0, // slide 너비
	pageCnt : 0, // pdf 페이지 수
	passwdCnt : 0,	// 패스워드 시도 횟수	
	initRenderList : [],
	basePos : [200, 0],
	orgSize : [],
	currentPacket : null,
	canvasSize : [],
	isMixMode : false,	// height가 0인 편집모드 중일때.. PDF랜더링시 넓이를 재조정해줘야 한다.
	isRedraw : false,
	blocked : false,	
	slideSmooth : true,
	
	initializing : false, // page init 중
	rendering : false, 	  // PDF 현재 드로잉 중인지 여부
	paging : false,		  // page 이동중
	zooming : false,	  // zoom 진행 중은 겹치지 않게 한다.	
	dragging : false,
	resizing : false,
	useCache : true,
	cacheMap : null,	// pageid, data, canvas

	init : function(data){ 
		// pdf : function(url, canvas, seqno, scaleW, scaleH, x, y, mode, typeFlag, docPageNo)
		var url = data.dnloadurl; 
		if(url.indexOf("test") > -1) url = url.replace("test", "dev");		

		// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var pdfCanvas = PDFViewer.getCanvas("pdf");
		
		var seqno = data.seqno;
		var mode = "0";
		var typeFlag = "p";
		var docPageNo = "1";
		var sketch = $("#sketch" + UI.current);
		var canvasWidth = sketch.width() * PDFViewer.renderScale;
		var canvasHeight = sketch.height() * PDFViewer.renderScale;
		
		this.canvasSize = [canvasWidth, canvasWidth];
	 
		if(PDFViewer.currentPacket != null){
			PDFViewer.destroy(false);
			PDFViewer.currentPacket = null;
		}

		// 문서를 읽어오고 editor를 만든뒤 패킷을 보낸다.		
		var pdfs = {url : url, pageno : 1};
		
		PDFViewer.setEditor(data.filename, 1);
//------------------------------------------------------------------------------
		var opts = {
			width : canvasWidth, // width
			height : canvasHeight, // height
			callback : function(pageNumber, width, height) {		
// ------------------ send to attender packet (for center align) ---------------
				// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
				var pdfCanvas = PDFViewer.getCanvas("pdf");
				// var defaultX = (pdfCanvas.width / 2) - (width / 2);
				// 2016.03.26 대표님 요청으로 좌측으로 반배율 더 땡겨서 뷰처리.
				var defaultX = ((pdfCanvas.width / 2) - (width / 2)) / 2;
				var defaultY = PDFViewer.basePos[1];				
				var container = document.getElementById(UI.CONTAINER);					
				var convertX = defaultX + (container.scrollLeft || container.scrollLeft);
		        var convertY = defaultY + (container.scrollTop || container.scrollTop);
		        var fixedX = PDFViewer.getFixedX(pdfCanvas.width, pdfCanvas.height, convertX);
				var fixedY = PDFViewer.getFixedY(pdfCanvas.width, pdfCanvas.height, convertY);				
				var packet = {
					"cmd":"pdf",
					"mode":"0",
					"seqno":""+ data.seqno +"",
					"posx":"" + fixedX + "",
					"posy":"" + fixedY + "",
					"scalew":"" + 1 + "",
					"scaleh":"" + 1 + "",
					"url": ""+ url + "",
					"typeflag": "p",
					"fname" : data.filename,
					"docpageno": "1"
				};				
				PDFViewer.currentPacket = packet;
			 	PDFViewer.sendPacket("0", defaultX, defaultY, 1, 1, false);
//--------------------------------------------------------------------------------------
				// 초기 설정한 PDF갯수만큼 랜더링한다.
				for (var i = 1; i <= pageNumber; i++) {
					PDFViewer.setCanvas(i, width);
				}
				
				//-- set drag drop
				PDFViewer.setEvent();
				
				//-- set editor position
				// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
				var pdfCanvas = PDFViewer.getCanvas("pdf");
				PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
				 
				$("#" + PDFViewer.WRAPPER).css("width", width + "px");
				$("#" + PDFViewer.WRAPPER).css("height", height + "px");			
				$("#" + PDFViewer.WRAPPER).show();
				
				/*				
					// height가 0일수 있으니 수정.
					$(".btn_left", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
					$(".btn_right", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
					$(".btn_left", "#" + PDFViewer.HANDLE).show();
					$(".btn_right", "#" + PDFViewer.HANDLE).show();
	
					// $(".btn_right", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
					$("#" + PDFViewer.PAGER).css("top", (height - 30) + "px"); 
					$("#" + PDFViewer.PAGER).show();
				*/				
				PDFViewer.alignCtrl(height);
				
				
				//--  to master canvas 
				PDFViewer.toCanvasPacket(packet, width, height);		
 
				PDFViewer.auth();

				$("#" + PDFViewer.SLIDER).hide();
								
				//-- end redering 
				PDFViewer.initRenderList = [];
				PDFViewer.rendering = false;
				PDFViewer.initializing = false;
				
Utils.logger("pdf", "렌더링 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
			}
		};
		
Utils.logger("pdf", "렌더링 시작", (new Date().toString() + ", millisecond : " + new Date().getTime()));

		PDFViewer.initializing = true;
		PDFViewer.initDoc(pdfs, opts);
		
		// setEditorPosition
		/**
		var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
		**/
		
	},
	
	draw : function(packet, drawCallback){
		// var pdfUrl = "http://betafile.wenote.com/data/file/hashs/20/20f72661e2f0398578bc81ba7428eda7";
		// console.log(pdfUrl);
		// var pdfs = {url : packet.url, pageno : 1};
		if(PDFViewer.blocked) return;
		
		if(packet.url.indexOf("test") > -1) packet.url = packet.url.replace("test", "dev");		

		var canvasWrapper = $("#" + UI.WRAPPER);
		var width = canvasWrapper.width();
		var canvasWidth = width / 2;
		var canvasHeight = width / 2
		
		var canvasScale = width * PDFViewer.renderScale;
		this.canvasSize = [canvasScale, canvasScale];
		
		if(PDFViewer.currentPacket != null && PDFViewer.currentPacket.seqno != packet.seqno){
			// 새로운 pdf가 들어온경우 삭제하고 초기화
			PDFViewer.destroy(false);			
			PDFViewer.currentPacket = null;
		}
		
		// update, remove, init 구분 필요 
		if(PDFViewer.currentPacket != null){
			// console.log("PDFViewer.currentPacket.docpageno : " + PDFViewer.currentPacket.docpageno + ", packet.docpageno : " + packet.docpageno);
			var mode = packet.mode;
			if(mode == "1"){
				PDFViewer.destroy(false);				
			}else{
Utils.logger("pdf", "렌더링 시작", (new Date().toString() + ", millisecond : " + new Date().getTime()));
				if(PDFViewer.currentPacket.scaleh != packet.scaleh){					
					PDFViewer.removeCache(UI.current);
					
					var width = PDFViewer.orgSize[0] * packet.scalew;
					var height = PDFViewer.orgSize[1] * packet.scaleh;
					
					PDFViewer.currentPacket = packet;

					// control 재정렬
					PDFViewer.alignCtrl(height);
					PDFViewer.resizeCanvas(width, height);					
					PDFViewer.resizeAction(false, width, height);
					
					// resize시 cache를 제거해 준다.
Utils.logger("pdf", "렌더링 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
					return;
				}				

				if(PDFViewer.currentPacket.docpageno != packet.docpageno){					
					PDFViewer.slideSmooth = false;
					
					PDFViewer.changePage(packet.docpageno, function(){
						PDFViewer.toCanvasPacket(packet);
						PDFViewer.slideSmooth = true;
					});
					
					PDFViewer.currentPacket = packet;
					
Utils.logger("pdf", "렌더링 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
					return;
				}
								
				if(PDFViewer.currentPacket.posx != packet.posx || PDFViewer.currentPacket.posy != packet.posy){
					PDFViewer.toCanvasPacket(packet);					
					// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
					var pdfCanvas = PDFViewer.getCanvas("pdf");					
					PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
					PDFViewer.currentPacket = packet;
Utils.logger("pdf", "렌더링 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
				}
			}			
			
		}else{			
			PDFViewer.currentPacket = packet;
			PDFViewer.setEditor(packet.fname, packet.docpageno);
			PDFViewer.scale = packet.scaleh;
			
			var sketch = $("#sketch" + UI.current);
			var canvasWidth = sketch.width() * PDFViewer.renderScale;
			var canvasHeight = sketch.height() * PDFViewer.renderScale;
			
			PDFViewer.Loader.setPosition();
			
			PDFViewer.rendering = true;
			
			var opts = {
				width : canvasWidth, // width
				height : canvasHeight, // height				
				callback : function(pageNumber, width, height){					
					for(var i=1; i<=pageNumber; i++) {
						PDFViewer.setCanvas(i, width);
					}
					
					// movePage
					PDFViewer.movePageForce(packet.docpageno);
					
					//--  to attender canvas 
					PDFViewer.toCanvasPacket(packet);	
					
					//-- set event
					PDFViewer.setEvent();
				
					$("#" + PDFViewer.WRAPPER).css("width", width + "px");
					$("#" + PDFViewer.WRAPPER).css("height", height + "px");

					// height가 0일수 있으니 수정.
					/*$(".btn_left", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
					$(".btn_right", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
					$(".btn_left", "#" + PDFViewer.HANDLE).show();
					$(".btn_right", "#" + PDFViewer.HANDLE).show();

					// $(".btn_right", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
					$("#" + PDFViewer.PAGER).css("top", (height - 30) + "px"); 
					$("#" + PDFViewer.PAGER).show();*/
					
					PDFViewer.alignCtrl(height);
					
					//-- end redering 
					PDFViewer.initRenderList = [];	
					PDFViewer.rendering = false;	
 					
					$("#" + PDFViewer.SLIDER).hide();
Utils.logger("pdf", "렌더링 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
					// 로딩중 큐에 쌓여있는 데이터가 있으면 clear 해					
					PDFViewer.auth();

					if(PacketMgr.queue != null && PacketMgr.queue.length > 0){
						PacketMgr.shiftQueueSkipOverlapCmd();
					}					
				}
			};

			//-- set editor position, 로딩때문에 이곳에 추가
			// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
			var pdfCanvas = PDFViewer.getCanvas("pdf");
			PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
			
			$("#" + PDFViewer.WRAPPER).show()
			 
Utils.logger("pdf", "렌더링 시작", (new Date().toString() + ", millisecond : " + new Date().getTime()));
			this.initDoc(packet, opts);			
		}				
	},
	
	initDoc : function(pdfs, options) {
		// UI viewer, wrapper -> 전역변수 사용
		// file번호는 0번부터 시작하므로 1개 빼준다.
		if(pdfs == null || pdfs == '' || pdfs.length < 1){
			// alert("PDF 파일 정보를 찾을 수 없습니다. 관리자에 문의해 주세요.");
			Ctrl.Msg.show(RoomSvr.roomtitle + _msg("pdf.not.found"));
			if(options.callback) options.callback(0);
			return; 
		} 
		
		this.slider = $("#" + this.VIEWER);
		
		this.pdfs = pdfs;
		var url = pdfs.url;
		
		this.canvasWidth = parseInt(options.width, 10) || 0;
		this.canvasHeight = parseInt(options.height, 10) || 0;
		this.sizeMap = [];
		  
		var pageNo = pdfs.docpageno || 1;
		this.initRenderList = (pageNo == 1) ? [1,2,3] : [pageNo-1, pageNo, pageNo+1];
 
		PDFViewer.getDoc("", options);		
	},
	
	initSlider : function(){
		var canvasWrapper = $("#" + UI.WRAPPER);
		var width = canvasWrapper.width();
		
		var canvasWidth = PDFViewer.canvasSize[0];
		var canvasHeight = PDFViewer.canvasSize[1];
		
		// var wrapperHeight = canvasHeight + ($("#pdfTitle").height() + 19 + 36);
		
		$("#" + PDFViewer.WRAPPER).height(canvasWidth + "px");
		$("#" + PDFViewer.VIEWER).height(canvasHeight + "px");
	},
	
	setEditor : function(fileName, pageNo){
		// setDrag : function(img, canvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail)
		/**
			var url = packet.url; 	
			var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");		
			var posX = this._getOrgX(pdfCanvas.width, pdfCanvas.height, packet.posx);
			var posY = this._getOrgY(pdfCanvas.width, pdfCanvas.height, packet.posy);
		**/
		 
		
		this.initSlider(); 
		
		$("#" + PDFViewer.WRAPPER).append("");
		
		var pdfHandle = document.getElementById(PDFViewer.HANDLE);
		var pdfSlider = document.getElementById(PDFViewer.SLIDER);
		var pdfLoader = document.getElementById(PDFViewer.LOADER);
		
		if(pdfHandle || pdfSlider || pdfLoader) this.destroy();
		
		if(pdfHandle) $(pdfHandle).remove();
		if(pdfSlider) $(pdfSlider).remove();
		if(pdfLoader) $(pdfLoader).remove();
		
		var html = "<div id=\"pdfLoading\" class=\"pdf_loading\">\
					   <div id=\"floatingCirclesG\">\
							<div class=\"f_circleG\" id=\"frotateG_01\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_02\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_03\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_04\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_05\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_06\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_07\"></div>\
							<div class=\"f_circleG\" id=\"frotateG_08\"></div>\
					   </div>\
					   <div class=\"loading_msg\">"+_msg("pdf.loading")+"</div>\
					</div>\
					<div id=\"pdfHandle\" class=\"pdf_box_edit\">\
						<div id=\"pdfTitle\" class=\"pdf_title\"><span>"+ fileName +"</span><div class=\"pdfZoom\"><a href=\"javascript:PDFViewer.toggleScaleBtn(event, this);\" class=\"btn_pdfZoom\" title=\"배율\"></a><div class=\"pdfZoomOption\"><a href=\"javascript:PDFViewer.setScaleForce('0.5');\">x0.5</a><a href=\"javascript:PDFViewer.setScaleForce('1');\">x1</a><a href=\"javascript:PDFViewer.setScaleForce('1.2');\">x1.2</a></div></div><a class=\"btn_x\" onclick=\"PDFViewer.remove(event);\"></a></div>\
						<a href=\"javascript:void(0)\" class=\"btn_left\" style=\"display:none;\" onclick=\"PDFViewer.prevNext(event, 'prev');\"></a>\
						<a href=\"javascript:void(0)\" class=\"btn_right\" style=\"display:none;\" onclick=\"PDFViewer.prevNext(event, 'next');\"></a>\
						<div id=\"pdfNavi\" style=\"position:absolute; right:20px; margin:4px 0 0 0; font-size:12px; text-align:center; overflow:hidden; display:none;\"><span id=\"page_current\">"+pageNo+"</span>/<span id=\"page_cnt\"></span></div>\
						<a href=\"javascript:PDFViewer.download();\" class=\"btn_pdf_share\"></a>\
						<span class=\"tl\"></span>\
						<span class=\"tc\"></span>\
						<span class=\"tr\"></span>\
						<span class=\"ml\"></span>\
						<span class=\"mr\"></span>\
						<span class=\"bl\"></span>\
						<span class=\"bc\"></span>\
						<span class=\"br\"></span>\
					</div>\
					<div id=\"pdfSlider\" class=\"wrapper_container\"><div id=\"pdfWrap\" class=\"pdf_wrapper\"></div></div>";
		
		// --> IE안됨
		$("#" + PDFViewer.WRAPPER).append(html);		
		// $("#" + PDFViewer.WRAPPER).hide();		
		
	},
	
	auth : function(){
		var isHand = Ctrl.isHand();
		var isText = Ctrl.isText();
		var isZoom = UI.scale == 1 ? false : true;
		// isZoom = false;
		if(PacketMgr.isMC && !isZoom && isHand){
			this.enable(PacketMgr.isMC, isHand, isZoom, isText);
		}else{
			this.disable(PacketMgr.isMC, isHand, isZoom);
		}
	},
	
	drag : function(div, enable){
		try{
			if(enable){
				// $(div).draggable("enable");
				$(div).resizable("enable");
			}else{
				// $(div).draggable("disable");
				$(div).resizable("disable");
			}			
		}catch(e){
			// console.log(e);
		}
	},
	
	enable : function(isMC, isHand, isZoom, isText){
		var div = document.getElementById(PDFViewer.WRAPPER);		
		// PDFViewer.pdfs != null
		if(div && PDFViewer.pdfs != null){
			if(PacketMgr.isMC){
				$(div).show();
				
				PDFViewer.Loader.hide();
				
				if(isHand && !isText){
					/*var height = $("#" + PDFViewer.FIRST_HOLDER).height();
					$("#" + PDFViewer.WRAPPER).height(height);	
					$("#" + PDFViewer.WRAPPER).css("border", "1px dotted #c8c8c8");*/					
					PDFViewer.setBodyHeight(true);
					PDFViewer.drag(div, true);		
					PDFViewer.isMixMode = false;
				}else{					
					/*$("#" + PDFViewer.WRAPPER).height(0);
					$("#" + PDFViewer.WRAPPER).css("border", "0px");*/
					PDFViewer.setBodyHeight(false);
					PDFViewer.drag(div, false);
					PDFViewer.isMixMode = true;
				}
			}
			/*var height = $("#" + PDFViewer.WRAPPER).height();
			PDFViewer.alignCtrl(height);*/
		}	
	},	
	disable : function(isMC, isHand, isZoom){
		var div = document.getElementById(PDFViewer.WRAPPER);
		if(div){
			/**
			$(div).draggable("disable");
			$(div).resizable("disable");
			// $(div).hide();
			**/			
			// 마스터일 경우 높이를 0으로 변환하여, 드로잉과 동시에 페이지 이동 기능이 가능하게 한다.
			if(PacketMgr.isMC && !isZoom){	// pen mode zoom때문에 show 적용
				$(div).show();
				//$(div).hide();
				
				PDFViewer.Loader.hide();  
				
				if(isHand){
					/*var height = $("#" + PDFViewer.FIRST_HOLDER).height();
					$("#" + PDFViewer.WRAPPER).height(height);
					$("#" + PDFViewer.WRAPPER).css("border", "1px dotted #c8c8c8");*/
					PDFViewer.setBodyHeight(true);
					PDFViewer.drag(div, true);
					PDFViewer.isMixMode = false;
				}else{
					/*$("#" + PDFViewer.WRAPPER).height(0);
					$("#" + PDFViewer.WRAPPER).css("border", "0px");*/
					PDFViewer.setBodyHeight(false);
					PDFViewer.drag(div, false);
					PDFViewer.isMixMode = true;
				}
			}else{
				$(div).hide();
				PDFViewer.isMixMode = false;
			}
		}
	},
		
	// pen mode시 body에 드로잉 가능하게 하기 위함
	setBodyHeight : function(isEnable){
		
		if(isEnable){
			var height = $("#" + PDFViewer.FIRST_HOLDER).height();
			$("#" + PDFViewer.WRAPPER).height(height);	
			$("#" + PDFViewer.WRAPPER).css("border", "1px dotted #c8c8c8");
		}else{
			$("#" + PDFViewer.WRAPPER).height(0);
			$("#" + PDFViewer.WRAPPER).css("border", "0px");
		}
	},
	
	setEvent : function(){			
		var orgW = 0;
		var orgH = 0;
		var div = document.getElementById(PDFViewer.WRAPPER);
		
		/**
		$(div).off().click(function(e){
console.log("click!!!!!!!!!!!!!!!!!!!!!!!");
			e.stopImmediatePropagation();
			e.stopPropagation();
			e.preventDefault();	         
			e.cancelBubble = true;
	        return false;
		});
			
		$(div).off().mousedown(function(e){
console.log("mousedown!!!!!!!!!!!!!!!!!!!!!!!");
			e.stopImmediatePropagation();
			e.stopPropagation();
			e.preventDefault();
	        return false; 
		});
		
		// 일정이상으로 컸을때 클릭으로 페이지이동.
		$(div).click(function(e){			
			if(PDFViewer.dragging || PDFViewer.resizing) return false;			
			// 한 화면에 보일때는 move를 시키지 않는다.
			if($(div).height() <= window.screen.availHeight){
				return false;
			}	
			var x = e.pageX - div.offsetLeft;
		    var y = e.pageY - div.offsetTop;			
			var isPrev = x < $(div).width() / 2 ? true : false;			
			if(isPrev){
				PDFViewer.prev();
			}else{
				PDFViewer.next();
			}			
		}); 
		**/
		
		$(div).draggable({
			handle : $("#pdfTitle span"),
			containment: $('#docWrapper'),
			start : function(e){
				// UI.skboards[UI.current-1].clearPdf();
				PDFViewer.clear();
				
				$("#" + PDFViewer.SLIDER).show();
				div.style.opacity = 0.5;
				
				PDFViewer.dragging = true;
				
				if(!Ctrl.isHand()) PDFViewer.setBodyHeight(true);
				
				// IE에서 draggable 스크롤 버그 있음
				if(Utils.browser("msie")) {
					var container = document.getElementById(UI.CONTAINER);

					var top = (document.documentElement && document.documentElement.scrollTop) || document.body.scrollTop;
					if(Utils.browser("msie")) $(this).data("startingScrollTop", top);
				}
			},
			drag : function(e, ui){ },
			stop : function(e, ui){
		        var posX = $("#" + PDFViewer.WRAPPER).position().left;
				var posY = $("#" + PDFViewer.WRAPPER).position().top;
				 
				var packet = PDFViewer.sendPacket("2", posX, posY, PDFViewer.current, PDFViewer.scale, false);	
				PDFViewer.toCanvasPacket(packet);
				
				PDFViewer.currentPacket = packet;	
				
				div.style.opacity = 1;
				$("#" + PDFViewer.SLIDER).hide();

				// 드래그 드랍이 보이기 위함
				if(!Ctrl.isHand()) PDFViewer.setBodyHeight(false);
				
				// IE 드래그드랍 스크롤 버그 수정 
				if(Utils.browser("msie")){
					var st = parseInt($(this).data("startingScrollTop"));
					var doc = (document.documentElement || document.body);
					doc.scrollTop = st;
				} 
				
				// click이벤트와 중첩시 약간의 delay가 필요하다.
				setTimeout(function(){
					PDFViewer.dragging = false;
				}, "500");
			}
		});		
		
		var beforeWidth = 0;		
		$(div).resizable({
			handles : "e,s,se,n,ne,w,sw,nw",
			zIndex : 1002,
			animation : true,
			containment: $('#docWrapper'),
			aspectRatio : true,	// 정사각 비율로 resize
			minWidth : PDFViewer.orgSize[0] * 0.5,
			minHeight : PDFViewer.orgSize[1] * 0.5,			
			start : function(e, ui){
				PDFViewer.Loader.show();
				
				// UI.skboards[UI.current-1].clearPdf();
				PDFViewer.clear();
				
				beforeWidth = ui.width;
				
				$("#" + PDFViewer.SLIDER).show();
				PDFViewer.resizing = true;
			},
			resize : function(e, ui){
				PDFViewer.Loader.show();
				
				var width = ui.size.width;
				var height = ui.size.height;

				PDFViewer.resizeCanvas(width, height);			
				
				// 컨트롤 버튼 정렬.
				PDFViewer.alignCtrl(height);				
			},
			stop : function(e, ui){
				var width = ui.size.width;
				var height = ui.size.height; 
				
				PDFViewer.resizeAction(true, width, height);
				
				$("#" + PDFViewer.SLIDER).hide();
				PDFViewer.Loader.hide();				
				
				PDFViewer.removeCache();
			}
		});
		
		this.auth();
	},
	
	resizeCanvas : function(width, height){		
		var viewerWidth = 0;
		var slideTerm = 0;
		var pageNo = PDFViewer.current;
						
		// canvas pdf clear!
		// UI.skboards[UI.current-1].clearPdf();
		PDFViewer.clear();
		
		$("div", "#" + PDFViewer.VIEWER).each(function(index){
			$(this).width(width);
			$("canvas", $(this)).width(width);
			
			
			$(this).height(height);
			$("canvas", $(this)).height(height);
			
			var canvas = $("canvas", $(this)).get(0);
			if(canvas){
				var context = canvas.getContext("2d");
				context.clearRect(0, 0, canvas.width, canvas.height);	
			}
			
			viewerWidth += $(this).width();
			if(index <= pageNo - 2) slideTerm += width;
		});
		// slider update
		PDFViewer.viewer.width(viewerWidth);

		$("#" + PDFViewer.WRAPPER).width(width);
		$("#" + PDFViewer.WRAPPER).height(height);
		
		$("#" + PDFViewer.VIEWER).css("left", -(slideTerm) + "px");
		PDFViewer.move = -(slideTerm);
	},
	
	resizeAction : function(isSend, width, height){
		
		// 2015.09.22 리사이징시 로딩 추가!
		PDFViewer.Loader.show();
		
		$("canvas", "#" + PDFViewer.VIEWER).each(function(){
			$(this).remove();
		}); 
		
		// 참여자는 에디터가 안보이기 때문에 current로 구해준
		this.scale = height / PDFViewer.orgSize[1];
		
		if(isSend){
			var x = $("#" + PDFViewer.WRAPPER).position().left;
			var y = $("#" + PDFViewer.WRAPPER).position().top;
			
			PDFViewer.sendPacket("2", x, y, PDFViewer.current, PDFViewer.scale, false);
		}
		
		this.resize(function(resizeWidth, resizeHeight){
			var viewerWidth = 0;
			$("div", "#" + PDFViewer.VIEWER).each(function(){
				$(this).width(resizeWidth);
				$(this).height(resizeHeight);
				// $("canvas", $(this)).width(resizeWidth);
				// $("canvas", $(this)).height(resizeHeight);				
				viewerWidth += resizeWidth;
			});
			
			PDFViewer.viewer.width(viewerWidth);
			
			$("#" + PDFViewer.WRAPPER).width(resizeWidth);
			$("#" + PDFViewer.WRAPPER).height(resizeHeight);
			
			// movePage
			PDFViewer.movePage(PDFViewer.current);

			/**
			if(isSend){
			// 2015.09.22 로딩 때문에 변경
				var x = $("#" + PDFViewer.WRAPPER).position().left;
				var y = $("#" + PDFViewer.WRAPPER).position().top;				
				var packet = PDFViewer.sendPacket("2", x, y, PDFViewer.current, PDFViewer.scale, false);
			}else{				
				var packet = PDFViewer.currentPacket;
				PDFViewer.toCanvasPacket(packet);	
			}
			**/
			
			var packet = PDFViewer.currentPacket;
			PDFViewer.toCanvasPacket(packet);
			
			// click이벤트와 중첩시 약간의 delay가 필요하다.
			setTimeout(function(){
				PDFViewer.Loader.hide();	
				PDFViewer.resizing = false;
				
				var isHand = Ctrl.isHand();
				var isText = Ctrl.isText();
				var isZoom = UI.scale == 1 ? false : true;

				// 펜모드시 높이를 0으로 만들기 위함.		
				if(PacketMgr.isMC) PDFViewer.auth();
				
				if(PDFViewer.useCache) PDFViewer.flushCache();

			}, "500");
			
		});
	},
	
	resize : function(callback){		
		/* var canvasWidth = $("#sketch1").width() * PDFViewer.renderScale;
		var canvasHeight = $("#sketch1").height() * PDFViewer.renderScale; */
		var sketch = $("#sketch" + UI.current);
		var canvasWidth = sketch.width() * PDFViewer.renderScale;
		var canvasHeight = sketch.height() * PDFViewer.renderScale;
		
		var pageNo = this.current;
		PDFViewer.pdfs.docpageno = pageNo;
		
		this.initRenderList = (pageNo == 1) ? [1,2,3] : [pageNo-1, pageNo, pageNo+1];
		var options = {
			width : canvasWidth, // width
			height : canvasHeight, // height				
			callback : function(pageNumber, width, height) {
				// 초기 설정한 PDF갯수만큼 랜더링한다.
				for (var i = 1; i <= pageNumber; i++) {
					PDFViewer.setCanvas(i, width);
				}
 				
				//-- end redering 
				PDFViewer.initRenderList = [];		
				PDFViewer.rendering = false;
				
				if(callback) callback(width, height);
			}
		};
		
		PDFViewer.getDoc("", options);
	},
	
	changePage : function(idx, callback) {
		if(idx < 1) return;

		if (this._isRender(idx)) {			
// 리사이징시 initCnt보다 큰 페이지가 이미 렌더링 된 상태로 콜백을 받을수 있기때문에 렌더링과 동일하게 callback을 호출해준다.
			this.movePage(idx);			
			if(callback) callback(idx);			
		} else {
			this.drawPdf(idx, idx, callback);
		}
		
		PDFViewer.setPage(PDFViewer.pageCnt, idx);
	},
	
	drawPdf : function(startIdx, pageIdx, callback) {
		var onDrawPdfs = function(drawPageIdx) {
			// 복수개의 PDF를 렌더링하는 경우 마지막 페이지에 도달했을때.
			if (drawPageIdx > pageIdx) {
				// 렌더링을 새로그린 가장 첫번째 페이지 이전의 마지막 Pensetting 패킷으로 pen을 세팅한다.
				PDFViewer.movePage(pageIdx);
				
				if (callback) callback(pageIdx);
				return;
			}

			// 이미 렌더링 되어 있으면 pass
			if(PDFViewer._isRender(drawPageIdx)) {
				// 이밈 랜더링 되어 있다면 
				onDrawPdfs(++drawPageIdx);
				return;
			}

			PDFViewer.rendering = true;
			var onPdfRederEnd = function(pageNumber, width, height) {
				// var realCanvasWidth = Math.floor(PDFViewer.sizeMap[0].width);
				PDFViewer.setCanvas(drawPageIdx, width);

				// UI.setViewer();
				PDFViewer.rendering = false;
 				
				onDrawPdfs(++drawPageIdx);
			};

			PDFViewer.render(drawPageIdx, onPdfRederEnd);
		};
        
		onDrawPdfs(startIdx);
	},
	
	
	setCanvas : function(pageNo, slideX) {
		var viewer = PDFViewer.VIEWER;
		var viewerWidth = 0;
		var slideTerm = 0;
		// $(".pdfpage", $("#wrap")).each(function(index){
		
		$("div.pdf_wrap", $("#"+ this.VIEWER)).each(function(index) {
			var w = $(this).width();			
			viewerWidth += w;
			
			if (index <= pageNo - 2) slideTerm += w;
		});
		
		// 2014.10.17일 setViewer 변경
		this.viewer = $("#" + viewer);
		this.viewer.width(viewerWidth);
	},
	
	getDoc : function(passwd, options){
		// pwd		
		var url = PDFViewer.pdfs.url;		
		var pageNo = PDFViewer.pdfs.docpageno || 1;
 		
		if(location.hostname == "test.wenote.com"){
			url = url.replaceAll("https://dev.wenote.com:443","http://test.wenote.com");				
		} 
		
		/** 파라미터 참고 : url, data, httpHeaders, withCredentials, password, initialData */
		var passwd = passwd != "" ? passwd : PDFViewer.pwd ? PDFViewer.pwd : "";
		var info = {};
		info.url = url;
		
		if(passwd) {
			info.password = passwd;
			PDFViewer.pwd = passwd;			
		}
		
		// 패스워드 물음 
		PDFJS.getDocument(info, null, function(newPasswd, response){
			// 1. needPassword
			// 2. incorrectPassword
			var passwd = (response == 1) ? prompt(_msg("m.password.msg.3"), "") : (response == 2) ? prompt(_msg("m.password.msg.4"), "") : "";
			if(PDFViewer.passwdCnt > 3){	// 4회이상 실패시 파기 처리 
				PDFViewer.blocked = true;
				PDFViewer.initializing = false;
				PDFViewer.destroy(false);	
				Ctrl.Msg.show(_msg("m.password.msg.5"));				
				return;
			}
			
			PDFViewer.getDoc(passwd, options);
			
			if(response == 1 || response == 2){
				PDFViewer.passwdCnt++;	
			}
			
		}).then(function getPdfForm(pdf) {
			PDFViewer.pdf = pdf;
			PDFViewer.isRedraw = false;
			
			// ui setting
			PDFViewer.setPage(pdf.numPages, pageNo);
			
			var initCnt = pageNo + 1 > pdf.numPages ? pdf.numPages : pageNo + 1;
			/**
			if(PDFViewer.pageCnt > PDFViewer.initCnt && pageNo == 1){
				initCnt = PDFViewer.initCnt;
			}
			**/
			
			// filter
			PDFViewer.setFilter();
			
			// 1page씩만 렌더링하는 구조로 변경
			var pageNumber = 1; 
			// 재귀함수
			var recursionRender = function(index, width, height) {
				if (index > initCnt) {
					// 로딩 삭제
					/**
						var loader = document.getElementById(PDFViewer.loader);
						if(loader) PDFViewer.Control.Loader.hide();
					**/
					PDFViewer.Loader.hide();
					
					// Ctrl.Loader.hide();
					// 마지막에 렌더링한 번호를 리턴
					var retIdx = index - 1;
					if(options.callback) options.callback(retIdx, width, height);
					
					return;
				}
				PDFViewer.render(index, recursionRender);
			};

			PDFViewer.render(pageNumber, recursionRender);
		}, function(e){
			console.log("PDFJS.getDocument fail : " + e);
			PDFViewer.initializing = false;
		});
		
	},
	
	addFile : function(packet){
		this.pdfs.push(packet);  
	},

	setPage : function(totalCnt, currentCnt){		
		PDFViewer.pageCnt = totalCnt;		
		// page 표시 
		$("#page_cnt").html(totalCnt);
		
		$("#page_current").html(currentCnt);
	},
	
	setFilter : function(){
		var filterList = [];		
		var len = this.initRenderList == null ? 0 : this.initRenderList.length;
		for(var i=0; i<len; i++){
			var idx = this.initRenderList[i];
			if(idx <= this.pageCnt){
				filterList.push(idx);				
			}
		}
		this.initRenderList = filterList;		
	},
	
	
	// canvas rendering
	render : function(pageNumber, callback) {
		var pdf = PDFViewer.pdf;
		var viewer = document.getElementById(PDFViewer.VIEWER);
		PDFViewer.renderPage(viewer, pdf, pageNumber, function pageRenderingComplete(width, height) {
			var nextPageNumber = pageNumber + 1;
			callback(nextPageNumber, width, height);
		});
	},
  
	slideWrapperFix : function(realCanvasWidth) {
		$("#" + PDFViewer.wrapper).width(realCanvasWidth);
	},

	renderPage : function(div, pdf, pageNumber, callback) {
		// pageNumber가 string인 경우 오류 발견 2014.06.20
		if (typeof (pageNumber) == "string") pageNumber = parseInt(pageNumber, 10);

		var startTime = new Date().getTime(); 
// console.log("pdf getPage 시작  : ", (new Date().toString() + ", millisecond : " + startTime));
		
		pdf.getPage(pageNumber).then(function(page) {			
			var endTime = new Date().getTime();
// console.log("pdf getPage 끝  : ", (new Date().toString() + ", millisecond : " + (endTime - startTime)));
			
			// 넓이기준 scale 조정
			// var scale = PDFViewer.canvasWidth / page.getViewport(1.0).width;
			// 세로기준 scale 조정
			var orgViewportWidth = page.getViewport(1.0).width;
			var orgViewportHeight = page.getViewport(1.0).height;
			// 가로가 길거나 세로가 긴경우 긴쪽에 맞는 배율로 작업 
			var scale = (orgViewportWidth > orgViewportHeight) ? 
					(PDFViewer.canvasWidth / page.getViewport(1.0).width) : (PDFViewer.canvasHeight / page.getViewport(1.0).height);
			
			var orgViewPort = page.getViewport(scale);			
			scale = scale * PDFViewer.scale;
// alert("PDFViewer.scale : " + PDFViewer.scale + ", scale : " + scale);
			
			var viewport = page.getViewport(scale);
			// viewport 저장
			PDFViewer.sizeMap.push(viewport);
			if (pageNumber == 1) {
				PDFViewer.slideWrapperFix(Math.floor(viewport.width));
				PDFViewer.baseScale = scale;	
				
				// 1배율 저장
				PDFViewer.orgSize = [orgViewPort.width, orgViewPort.height];
			}

			
			var pageDisplayWidth = Math.floor(viewport.width);
			var pageDisplayHeight = Math.floor(viewport.height);
			var pageDivHolder = document.getElementById('pdfHolder' + pageNumber);
			
			// loader set position
			PDFViewer.Loader.setPosition(pageDisplayHeight);
			
			if(pageDivHolder == null){
				// 702 x 486
				pageDivHolder = document.createElement('div');
				pageDivHolder.id = 'pdfHolder' + pageNumber;
				pageDivHolder.className = 'pdf_wrap';
				pageDivHolder.style.width = pageDisplayWidth + 'px';
				pageDivHolder.style.height = pageDisplayHeight + 'px';
				pageDivHolder.style.background = 'transparent';
				// pageDivHolder.style.backgroundColor = "grey";
				// 2014.11.18 zoom했을경우 드로잉이 삐져나올수있어서 추가함
				pageDivHolder.style.overflow = 'hidden';
				pageDivHolder.style.cssFloat = "left";
				
				div.appendChild(pageDivHolder);
			}

			if(PDFViewer.initRenderList.length > 0 && PDFViewer.initRenderList.indexOf(pageNumber) < 0){
				PDFViewer.initRenderList.without(pageNumber);
				callback(pageDisplayWidth, pageDisplayHeight);
				return;
			} 
			
			// Prepare canvas using PDF page dimensions
			var canvas = document.createElement('canvas');
			var context = canvas.getContext('2d');
			canvas.id = "pdf" + pageNumber;
			canvas.className = "pdf" + pageNumber;
			canvas.width = pageDisplayWidth;
			canvas.height = pageDisplayHeight;
			
			pageDivHolder.appendChild(canvas);

			$("#" + PDFViewer.WRAPPER).width(pageDisplayWidth);
			
			// 펜모드일때 PDF를 불러오는 경우 
			if(PDFViewer.isMixMode && !PDFViewer.resizing){
				$("#" + PDFViewer.WRAPPER).height(0);		
				$("#" + PDFViewer.WRAPPER).css("border", "0px");				
			}else{
				$("#" + PDFViewer.WRAPPER).height(pageDisplayHeight);
			}
			
			// 2017.01.05 pdf caching 처리 - Render PDF page into canvas context
			var data = PDFViewer.cacheMap != null ? PDFViewer.cacheMap.get(UI.current) : null;
			var cacheCanvas = data != null ? $(".pdf" + pageNumber, "#" + data.layerid).get(0) : null;
			
			var startTime3 = new Date().getTime(); 
			// console.log("pdf caching 시작  : ", (new Date().toString() + ", millisecond : " + startTime3));

			/*
			if(data){
				// alert("UI.current : " + UI.current + ", data.layerid : " + data.layerid)	
			}*/      
			
// console.log("PDFViewer.usecache : " + PDFViewer.usecache + ", data : " + data);
			
			if(PDFViewer.useCache && data && cacheCanvas && !PDFViewer.resizing){
				// var cacheCanvas = $(".pdf" + pageNumber, "#" + data.layerid).get(0);
				var cacheContext = cacheCanvas.getContext('2d');				
				context.drawImage(cacheCanvas, 0, 0);
				
				var endTime3 = new Date().getTime();
Utils.log("pdf caching : ", (new Date().toString() + ", millisecond : " + (endTime3 - startTime3)));
				callback(pageDisplayWidth, pageDisplayHeight);
				
			}else{				
				// new rendering
				var renderContext = {
					canvasContext : context,
					viewport : viewport
				};
				
				// 2014.01.20 pdf렌더링 chrome crash문제 때문에 버전업하였고, callback 방식 변경됨
				var startTime2 = new Date().getTime(); 
				// console.log("pdf rendering 시작  : ", (new Date().toString() + ", millisecond : " + startTime2));
				
				var pageRendering = page.render(renderContext);
				pageRendering.promise.then(function pdfPageRenderCallback() {
					var endTime2 = new Date().getTime();
Utils.log("pdf rendering end : ", (new Date().toString() + ", millisecond : " + (endTime2 - startTime2)));
					// 성공시 callback
					callback(pageDisplayWidth, pageDisplayHeight);
				}, function pdfPageRenderError(error) {
					console.log("pdfPageRenderError error !! : " + error);
				}); 
			}			
			
		}, function(){
			console.log("PDFJS renderPage excpetion : " + e);
			PDFViewer.initializing = false;			
		});
	},

	// zoom 처리 : settled가 0일때 html5 transform의 scale을 이용해서 확대하고, settled가 1인경우
	// 재렌더링한다.
	zoom : function(index, json, isReset, callback) {
		if (PDFViewer.zooming) return;

		var scale = json.scale ? json.scale : 1;
		var x = json.x ? json.x : 0;
		var y = json.y ? json.y : 0;
		var settled = json.settled ? json.settled : "0";
		
		// zoom skip check
		/**
		if (this.lastZoomPacket != null) {
			var beforeScale = this.lastZoomPacket.scale;
			var beforeX = this.lastZoomPacket.x;
			var beforeY = this.lastZoomPacket.y;
			var beforeSettled = this.lastZoomPacket.settled;

			if (!isReset && beforeScale == scale && beforeX == x && beforeY == y && beforeSettled == settled) return;
		}
		**/
		
		this.lastZoomPacket = json;
		 
		if (settled == "1") {		
			PDFViewer.zooming = true;

			if(typeof (index) == "string") index = parseInt(index, 10);

			var pdf = PDFViewer.pdf;
			pdf.getPage(index).then(function(page) {
				var canvas = document.getElementById("pdf" + index);
				var context = canvas.getContext('2d');

				var w = $(canvas).width();
				var h = $(canvas).height();

				var newScale = PDFViewer._getMergeScale(scale);
				// var newScale = scale;
				var renderX = PDFViewer._getRenderX(canvas, x, scale, newScale);
				var renderY = PDFViewer._getRenderY(canvas, y, scale, newScale);

				// 1. 0,0 배울의 canvas context를 미리 저장하고 있는다. (기존에 드로잉된
				// canvas clear를 위함)
				context.save();

				// 14.09.16 페이지 이동없이 한페이지에서 settled 1이 적용된경우, 기존에 그려진
				// Canvas에
				canvas.style.backgroundColor = "#fff";
				context.clearRect(0, 0, w, h);

				context.translate(renderX, renderY);

				var viewport = page.getViewport(newScale);
				var renderContext = {
					canvasContext : context,
					viewport : viewport
				};

				// Step 1: store a refer to the renderer
				var pageRendering = page.render(renderContext);
				pageRendering.internalRenderTask.callback = function() {
					// 2, 0,0 배율의 context를 context.save()로 미리 저장하고 있다가,
					// 드로잉이 끝나고 context.restore()로 복구한다.
					context.restore();

					// settled0-확대체크용
					canvas.settled = "1";

					// back에 있는 drawing canvas는 html5 zoom scale을 따라간다.
					/**
					var newScale = PDFViewer._getScale(scale);
					if (newScale < 1) newScale = 1;
					json["newScale"] = newScale;
					**/
					PDFViewer.zooming = false;

					callback(index, json);
				};
			});

		} else {
			
			var canvas = document.getElementById("pdf" + index);
			var canvasSettled = canvas.settled ? canvas.settled : "0";
			
			if (canvasSettled == "1") {
				// pdf가 실렌더링이 한번이라도 된경우 다시 원본사이즈로 만든뒤 확대한다.
				PDFViewer.zooming = true;

				// 2014.06.20 pagenumber string일 경우 오류
				if (typeof (index) == "string") index = parseInt(index, 10);

				var pdf = PDFViewer.pdf;
				pdf.getPage(index).then(function(page) {
					var context = canvas.getContext('2d');
					var viewport = page.getViewport(PDFViewer.baseScale);

					var renderContext = {
						canvasContext : context,
						viewport : viewport
					};

					// Step 1: store a refer to the renderer
					var pageRendering = page.render(renderContext);
					pageRendering.internalRenderTask.callback = function() {						
						canvas.settled = "0";
						// settled 값에 따라 추가로직이 들어갈수 있으므로 이곳을 거처서 호출하게 한다.
						/**
						var newScale = PDFViewer._getScale(scale);
						if (newScale < 1) newScale = 1;
						json["newScale"] = newScale;
						**/  
						
						callback(index, json);
						
						PDFViewer.zooming = false;
					};
				});
				
			} else {
				/**
					var newScale = this._getScale(scale);
					if (newScale < 1) newScale = 1;
					json["newScale"] = newScale;
				**/
				callback(index, json);
			}
			
		}

		return json;
	},
	
	
	toCanvas : function(){
		// drawPdf(pageCanvas, posX, posY, width, height)		
		// var canvas = document.getElementById("pdf" + this.current);
		// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var pdfCanvas = PDFViewer.getCanvas("pdf");
		var canvas = document.getElementById("pdf" + this.current);
		var posX = $("#" + PDFViewer.WRAPPER).position().left;
		var posY = $("#" + PDFViewer.WRAPPER).position().top;
		var width = canvas.width;
		var height = canvas.height;

		var container = document.getElementById(UI.CONTAINER);					
		posX = posX + (container.scrollLeft || container.scrollLeft);
		posY = posY + (container.scrollTop || container.scrollTop);
		 		
		// posY += $("#pdfTitle").height() + 19;
		// UI.skboards[UI.current-1].drawPdf(canvas, posX, posY, width, height);
		var board = UI.getBoard();
		board.drawPdf(canvas, posX, posY, width, height);
		// $("#" + PDFViewer.WRAPPER).hide();	
	},
	
	toCanvasPacket : function(packet, width, height){		
		// var canvas = document.getElementById("pdf" + this.current);
		// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var pdfCanvas = PDFViewer.getCanvas("pdf");
		var canvas = document.getElementById("pdf" + this.current);
		var posX = this._getOrgX(pdfCanvas.width, pdfCanvas.height, packet.posx);
		var posY = this._getOrgY(pdfCanvas.width, pdfCanvas.height, packet.posy);
		
		var width = canvas != null ? canvas.width : width != null ? width : 0;
		var height = canvas != null ? canvas.height : height != null ? height : 0;
		// posY += ($("#pdfTitle").height() + 55);
		// posY += $("#pdfTitle").height() + 19;
		// UI.skboards[UI.current-1].drawPdf(canvas, posX, posY, width, height);
		var board = UI.getBoard();
		board.drawPdf(canvas, posX, posY, width, height);
		// $("#" + PDFViewer.WRAPPER).hide();		
	},
	
	sendPacket : function(mode, x, y, pageNo, scale, isScrollSkip){
		// 0-insert or update, 1-delete
		var packet = PDFViewer.currentPacket;
		var url = packet.url;
		// var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var pdfCanvas = PDFViewer.getCanvas("pdf");
		var seqno = packet.seqno;
		var scaleW = scale || 1;
		var scaleH = scale || 1;
		var fileName = packet.fname || '';
		
		// var x = $("#" + PDFViewer.WRAPPER).position().left;
		// var y = $("#" + PDFViewer.WRAPPER).position().top;
		// var mode = "2";	// 0-insert or update, 1-delete
		var typeFlag = "p";
		var docPageNo = pageNo;
		
		// resize시에는 스크롤을 넣지 않는다.
		if(!isScrollSkip){
			var container = document.getElementById(UI.CONTAINER);					
			x = x + (container.scrollLeft || container.scrollLeft);
	        y = y + (container.scrollTop || container.scrollTop);	
		}
        // 헤더 높이를 더해준다.        
        // y += 36;
		var packet = PacketMgr.Master.pdf(url, pdfCanvas, seqno, scaleW, scaleH, x, y, mode, typeFlag, docPageNo, fileName);
		PacketMgr.currentPacket = packet;
		return packet;
	},
	
	// var posX = this.canvasWidth - (w / 2);
	// var posY = h / 10;
	setEditorPosition : function(canvas, x, y){
		var posX = this._getOrgX(canvas.width, canvas.height, x);
		var posY = this._getOrgY(canvas.width, canvas.height, y);
		
		// 헤더 높이 + 패딩높이 추가
		// posY -= $("#pdfTitle").height() + 19;
		
		$("#" + PDFViewer.WRAPPER).css("left", posX + "px");
		$("#" + PDFViewer.WRAPPER).css("top", posY + "px");
		
		return [posX, posY];
	},
	
	setScaleForce : function(val){
		if(PDFViewer.rendering) return;
		
		var scale = parseFloat(val);
		var orgWidth = PDFViewer.orgSize[0];
		var orgHeight = PDFViewer.orgSize[1];
		var width = parseInt(orgWidth * scale);
		var height = parseInt(orgHeight * scale);
 		var currentWidth 	= $("#" + PDFViewer.WRAPPER).width();
		var currentHeight	= $("#" + PDFViewer.WRAPPER).height();
		
		// 넓이 변화가 없다면 skip
		if(width == currentWidth && height == currentHeight) return;

		PDFViewer.resizing = true;

		PDFViewer.alignCtrl(height);
		
		PDFViewer.resizeCanvas(width, height);		
		PDFViewer.resizeAction(true, width, height);
		
		$(".btn_pdfZoom", "#" + PDFViewer.TITLE).removeClass("on");		
	},
	
	alignCtrl : function(height){
		// height가 0일수 있으니 수정.
		$(".btn_left", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
		$(".btn_right", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
		$(".btn_left", "#" + PDFViewer.HANDLE).show();
		$(".btn_right", "#" + PDFViewer.HANDLE).show();

		// $(".btn_right", "#" + PDFViewer.HANDLE).css("top", (height / 2) + "px");
		$("#" + PDFViewer.PAGER).css("top", (height - 30) + "px"); 
		$("#" + PDFViewer.PAGER).show();
	},
	
	toggleScaleBtn : function(e, thisNode){
		// 버튼으로 PDF 렌더링		
		var btn = $(".btn_pdfZoom", "#" + PDFViewer.TITLE);
		if(btn.hasClass("on")){
			btn.removeClass("on");
		}else{
			btn.addClass("on");
		}		
	},
	prevNext : function(e, flag){
		if(flag == 'prev'){
			this.prev();
		}else{
			this.next();
		}
		
		e.stopPropagation();
	},
	
	prev : function(){
		// if(this.current < 1) return;
		if(this.current < 2 || this.paging) return;
		
		var prevPageNo =  this.current - 1;		
		PDFViewer.currentPacket.docpageno = prevPageNo;
		 
		var x = $("#" + PDFViewer.WRAPPER).position().left;
		var y = $("#" + PDFViewer.WRAPPER).position().top;
		var scale = PDFViewer.scale;
				
		this.sendPacket("2", x, y, prevPageNo, scale, false);
		this.paging = true;
		
Utils.logger("pdf", "페이징 시작", (new Date().toString() + ", millisecond : " + new Date().getTime()));
		this.changePage(prevPageNo, function(){
			PDFViewer.toCanvas();
			PDFViewer.paging = false;
			PDFViewer.currentPacket.docpageno = prevPageNo;
Utils.logger("pdf", "페이징 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
		});	
	},
	
	next : function(){
		// if(this.current == 1) return;
		if(this.current == PDFViewer.pageCnt || this.paging) return;
		
		var nextPageNo = this.current + 1;
		PDFViewer.currentPacket.docpageno = nextPageNo;
		 
		var x = $("#" + PDFViewer.WRAPPER).position().left;
		var y = $("#" + PDFViewer.WRAPPER).position().top;
		var scale = PDFViewer.scale;
		
		this.sendPacket("2", x, y, nextPageNo, scale, false);
		this.paging = true;
		
Utils.logger("pdf", "페이징 시작", (new Date().toString() + ", millisecond : " + new Date().getTime()));		
		this.changePage(nextPageNo, function(){
			PDFViewer.toCanvas();
			PDFViewer.paging = false;
			PDFViewer.currentPacket.docpageno = nextPageNo;
Utils.logger("pdf", "페이징 완료", (new Date().toString() + ", millisecond : " + new Date().getTime()));
		});		
	},
	
	download : function(){
		var svrFlag = _prop('svr.flag');
		var svrHost = _prop('svr.host.' + svrFlag);
		var url = svrHost + _prop("pdf.download") + "?seqno=" + PDFViewer.currentPacket.seqno;

		var form = document.createElement("form");
		form.target = "targetFrm";
		form.method = "POST";
		form.action = url;
		form.submit();
		
		$(form).remove();		
	},
	
	movePageForce : function(idx){
		this.move = this._getSlideTerm(idx - 1);
		this.slider.css({left : this.move});
		this.current = parseInt(idx, 10);
	},
	
	// 기존 UI쪽에 있던 부분 추가 개발
	
	movePage : function(idx) {
		idx = parseInt(idx, 10);
		
		var term = idx - this.current;
		if (term == 0) return;

		this.move = this._getSlideTerm(idx - 1);
		this.slider.css({left : this.move});
		
		this.current = parseInt(idx, 10);
	},
	
	/** -- animation version	
	movePage : function(idx) {
		idx = parseInt(idx, 10);
		
		var term = idx - this.current;
		if (term == 0) return;
 				
		if (term < 0) {
			if (term == -1) {
				if (this.slideSmooth) {
					var sketch = this._getSketch(idx);
					var slideX = $(sketch).width();
					this.move += slideX;
					this.slider.animate({left : this.move}, "fast", function() {
						// callback
						// UI.slideFlag = false;
					});
				} else {
					this.move = this._getSlideTerm(idx - 1);
					this.slider.css({left : this.move});
				}
			} else {
				this.move = this._getSlideTerm(idx - 1);
				this.slider.css({left : this.move});
			}

		} else {
			if (term == 1) {
				if (this.slideSmooth) {
					var sketch = this._getSketch(idx - 1);
					var slideX = $(sketch).width();			
					
					this.move -= slideX;
					this.slider.animate({left : this.move}, "fast", function() {
						// callback
						// PDFViewerslideFlag = false;
					});

				} else {
					// 비동기로 SlideBar를 빠르게 움직인경우 애니메이션은 취소한다.
					this.move = this._getSlideTerm(idx - 1);
					this.slider.css({left : this.move});
				}
			} else {
				this.move = this._getSlideTerm(idx - 1);
				this.slider.css({left : this.move});
			}
		}
		
		this.current = parseInt(idx, 10);
	},
	***/
	
	
	// settled 1 목적의 PDF 실제 렌더링 배율
	_getScale : function(scale) {
		return scale;
	},

	_getMergeScale : function(scale) {
		return scale * this.baseScale;
	},

	// pdfjs로 렌더링할때의 이동해야하는 x좌표 -> pdfjs로 렌더링시 넓이 배율은 (늘어난배율 -1) * 캔버스넓이
	_getRenderX : function(canvas, x, scale, newScale) {
		var w = $(canvas).width();
		var h = $(canvas).height();

		// 신규 배율을 기본배율로 나눈걸로 값을 구한다.
		var maxX = w * (scale - 1);
		var orgX = this._getOrgX(w, h, x);
		var halfX = w / 2;		
		var scaleX = orgX - halfX;
//console.log("scaleX : " + scaleX + ", maxX : " + maxX);
		if (scaleX > maxX || scaleX < 0) {
// console.log("_getRenderX scaleX overflow : " + scaleX + ", maxX : " + maxX);
// 			scaleX = 0;
		}

		return -(scaleX);
	},

	_getRenderY : function(canvas, y, scale, newScale) {
		var w = $(canvas).width();
		var h = $(canvas).height();

		var maxY = h * (scale - 1);
		var orgY = this._getOrgY(w, h, y);
		var halfY = h / 2;
		var scaleY = orgY - halfY;		
//console.log("scaleY : " + scaleY + ", maxY : " + maxY);
		if (scaleY > maxY || scaleY < 0) {
// console.log("_getRenderY scaleY overflow : " + scaleY + ", maxY : " + maxY);
//			scaleY = 0;
		}
		
		return -(scaleY);
	},
	_getSketch : function(pageIdx) {
		return document.getElementById(this.SKETCH + pageIdx);
	},
	
	_getOrgX : function(w, h, dx) {
		var orgScaleX = (w > h) ? (w / 1024) : (w / 768);
		return dx * orgScaleX;
	},

	_getOrgY : function(w, h, dy) {
		var orgScaleY = (w > h) ? (h / 748) : (h / 1004);
		return dy * orgScaleY;
	},
	
	getFixedX : function(w, h, dx){
		var scaleX = (w > h) ? (1024 / w) : (768 / w);
		return dx * scaleX;
	},
	
	getFixedY : function(w, h, dy){
		var scaleY = (w > h) ? (748 / h) : (1004 / h);
		return dy * scaleY;
	},
	
	_isRender : function(pageIdx) {
		var layout = $(".pdf" + pageIdx, "#" + PDFViewer.SLIDER).get(0);
		return (layout) ? true : false;
	},	
	
	_getSlideTerm : function(idx) {
		var slideTerm = 0;
		$(".pdf_wrap", $("#" + this.VIEWER)).each(function(index) {
			// idx는 1부터 시작임을 확인
			if (index <= idx - 1) {
				slideTerm += $(this).width();
			}
		});
 
		return -(slideTerm);
	},
	
	remove : function(e){
		e.stopImmediatePropagation();
		e.stopPropagation();
		
		if (!PacketMgr.isMC) {
			Ctrl.Msg.auth(false);
			return;
		}
		
		Ctrl.Modal.confirm(_msg("confirm.remove.pdf"), true, function(){
			// remove packet 전송 
			var x = $("#" + PDFViewer.WRAPPER).position().left;
			var y = $("#" + PDFViewer.WRAPPER).position().top;
			var scale = PDFViewer.scale;
			
			PDFViewer.sendPacket("1", x, y, PDFViewer.current, scale, false);
			
			PDFViewer.destroy(false);			
		});
	},
	
	Loader : {
		
		setPosition : function(height){
			// cache가 존재하면 cache의 넓이 높이로 계산한다.
			
			var id = "pdfCache_" + UI.current;
			var cacheCanvas = $("canvas", "#" + id).get(0);
			// default
			var h = 400;
			if(typeof(height) != "undefined"){
				h = height;
			}else if(cacheCanvas){
				h = cacheCanvas.height;
			}			

			var top = h / 2;
			var beforeTop = $("#" + PDFViewer.LOADER).position().top;

			if(top == beforeTop) return;

			$("#" + PDFViewer.LOADER).css("top", h / 2);			
		},
		
		show : function(){
			if(PacketMgr.isMC){				
				// 손모드가 아닐경
				$("#" + PDFViewer.LOADER).show();				
			}else{
				$("#" + PDFViewer.WRAPPER).show();
				$("#" + PDFViewer.LOADER).show();
				$("#" + PDFViewer.SLIDER).hide();

				$("#" + PDFViewer.HANDLE).hide();				
			}
		},		
		hide : function(){
			if(PacketMgr.isMC){
				$("#" + PDFViewer.LOADER).hide();
			}else{
				$("#" + PDFViewer.WRAPPER).hide();
				$("#" + PDFViewer.LOADER).hide();
				$("#" + PDFViewer.HANDLE).show();				
			}			
		},
		
		destroy : function(){
			
		}
	},
	
	getCanvas : function(type){
		var board = UI.getBoard();
		return board.getCanvas(type);		
	},
	
	clear : function(){
		var board = UI.getBoard();
		board.clearPdf();
	},
	
	addCache : function(){
		if(!PDFViewer.useCache) return;
		
		var id = "pdfCache_" + UI.current;
		var layer = $("#" + id, "#pdfCacheContainer").get(0);
		
		if(layer != null){
			$(layer).remove();			
		}
		
		var div = document.createElement('div');
		div.id = id;	
		$("#pdfCacheContainer").append(div);
	
		// copy canvas
		var pdfCnt = 0;
		$(".pdf_wrap", "#" + PDFViewer.SLIDER).each(function(){
			var canvas = $("canvas", $(this)).get(0);
			if(canvas != null){
				pdfCnt++;
				
				$(div).append(canvas);

				$(canvas, $(div)).removeAttr("id");
				
				var targetCanvas = $("." + canvas.className, "#" + id).get(0);
				var targetContext = targetCanvas.getContext('2d');
				
				targetContext.drawImage(canvas, 0, 0);	
			}		
			
		});
		
		if(pdfCnt > 0){
			if(PDFViewer.cacheMap == null) PDFViewer.cacheMap = new Map();
			
			var data = {
				layerid : id,
				packet : PDFViewer.currentPacket,
				pdf : PDFViewer.pdf
			}
			PDFViewer.cacheMap.put(UI.current, data);	
		}
			 
	},
	
	flushCache : function(){
		var cacheParentId = "pdfCache_" + UI.current;
		// var cacheParentId =
			
		$(".pdf_wrap", "#" + PDFViewer.SLIDER).each(function(){
			var canvas = $("canvas", $(this)).get(0);
			if(canvas != null){
				//-- div.append(canvas);
				var targetCanvas = $("." + canvas.className, "#" + cacheParentId).get(0);				
// 4페이지를 넘어가는 신규 렌더링은 cache가 존재하지 않을 수 있다. 따라서 exist후 렌더링 한다.
				if(targetCanvas){
					var targetContext = targetCanvas.getContext('2d');
					targetContext.clearRect(0, 0, targetCanvas.width, targetCanvas.height);
					targetContext.drawImage(canvas, 0, 0);	
				}
			}
		});			
	},
	
	removeCache : function(){
		var cacheParentId = "pdfCache_" + UI.current;
		$("canvas", "#" + cacheParentId).remove();
	},
	
	destroy : function(makeCache) {		
		try{
			
			if(typeof(makeCache) == "undefined") makeCache = false;
			
			if(makeCache) {
				this.addCache();
			}else{
				this.removeCache();
			}
			
			// 리스너 삭제 필요
			this.canvasHeight = 0;
			this.sizeMap = [];
 			this.canvasSize = [];
			this.current = 1;
			this.slideX = 0;
			this.pageCnt = 0;
			this.rendering = false;			
			this.initRenderList = [];
			this.orgSize = [];
			this.currentPacket = null;
			this.canvasSize = [];
			this.scale = 1;
			this.move = 0;
			this.pwd = "";
			this.passwdCnt = 0;			

			this.clear();
			 
			var div = document.getElementById(PDFViewer.WRAPPER);
			if(div){
				try{
					if(PacketMgr.isMC && this.pdfs != null){
						$(div).draggable("destroy");
						$(div).resizable("destroy");
						this.pdfs = null;
					}
				}catch(e){
					console.log("draggable, resizable destroy fail.");
				}				
			}
			// editor clear
			div.innerHTML = "";
			$(div).hide();

		}catch(e){
			console.log(e);
		}
	}
};