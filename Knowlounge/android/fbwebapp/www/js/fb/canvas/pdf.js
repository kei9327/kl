
/**
 * @title : Pdf Viewer
 * @date : 2013.11.26
 * @author : kim dong hyuck
 * @description : PdfJS로 구현된 PDF viewer interface - IE9이상만 동작 - PDF Canvas는
 *              Drawing Canvas와 별도로 관리 -> zoom 문제
 */
var PDFViewer = {
	WRAPPER : "pdfWrapper",
	SLIDER : "pdfSlider",
	VIEWER : "pdfWrap",
	SKETCH : "pdf",
	LOADER : "pdfLoading",
	PAGER : "pdfNavi",
	HANDLE : "pdfHandle",
	INITPDF : 3,
	canvasWidth : 0,
	canvasHeight : 0,
	idx : 0,
	pdfs : null, // [{name:filename, url:downloadpath}, ...]
	sizeMap : [],
	pdf : null, // -- pdfjs object render list
	pwd : "",
	isRedraw : false,
	current : 1, // 현재 바라보고 있는 pdf 파일
	initCnt : 3,
	renderScale : 0.35,	// 기본 캔버스에 최적화 시킬 배율 
	baseScale : 0, // 초기 고정된 해상도의 baseScale -> 기본배율 1
	zoomScale : 0, // zoom에 의해 변형된 scale -> zoomScale = newScale / baseScale
	scale : 1,
	zooming : false, // zoom 진행 중은 겹치지 않게 한다.
	lastZoomPacket : null, // zoom의 마지막 패킷
	move : 0, // animation 이동
	slideX : 0, // slide 너비
	pageCnt : 0, // pdf 페이지 수
	passwdCnt : 0,	// 패스워드 시도 횟수
	rendering : false, // PDF 현재 드로잉 중인지 여부
	paging : false,		// page 이동중
	initializing : false, // page init 중 
	blocked : false,	
	slideSmooth : true,
	initRenderList : [],
	basePos : [200, 70],
	orgSize : [],
	currentPacket : null,
	canvasSize : [], 
	 
	init : function(data){ 
		// pdf : function(url, canvas, seqno, scaleW, scaleH, x, y, mode, typeFlag, docPageNo)

		console.log("PDFViewer.init()");
		console.log(data);

		var url = data.dnloadurl; 
		var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var seqno = data.seqno;
		var mode = "0";
		var typeFlag = "p";
		var docPageNo = "1";
		var canvasWidth = $("#sketch1").width() * PDFViewer.renderScale;
		var canvasHeight = $("#sketch1").height() * PDFViewer.renderScale;
		this.canvasSize = [canvasWidth, canvasWidth];

		console.log("canvasWidth : " + canvasWidth + ", canvasHeight : " + canvasHeight);

		if(PDFViewer.currentPacket != null){
			PDFViewer.destroy();
			PDFViewer.currentPacket = null;
			//---- 
			
		}

		// 문서를 읽어오고 editor를 만든뒤 패킷을 보낸다.		
		var pdfs = {url : url, pageno : 1};

		PDFViewer.setEditor(data.filename, 1);
//---------------------------------------------------		
		var opts = { 
			width : canvasWidth, // width
			height : canvasHeight, // height
			callback : function(pageNumber, width, height) {		
// ------------------ send to attender packet (for center align) ---------------
				var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
				var defaultX = (pdfCanvas.width / 2) - (width / 2);
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

				console.log("width : " + width + ", height : " + height);
				console.log("defaultX : " + defaultX + ", defaultY : " + defaultY);
				console.log("convertX : " + convertX + ", convertY : " + convertY);
				console.log("fixedX : " + fixedX + ", fixedY : " + fixedY);

				PDFViewer.currentPacket = packet;
			 	PDFViewer.sendPacket("0", defaultX, defaultY, 1, 1, false);
//--------------------------------------
				// 초기 설정한 PDF갯수만큼 랜더링한다.
				for (var i = 1; i <= pageNumber; i++) {
					PDFViewer.setCanvas(i, width);
				}
				
				//-- set drag drop
				PDFViewer.setEvent();
				
				//-- set editor position
				var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
				PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
				
				$("#" + PDFViewer.WRAPPER).css("width", width + "px");
				$("#" + PDFViewer.WRAPPER).css("height", height + "px");			
				$("#" + PDFViewer.WRAPPER).show();
				
				//--  to master canvas 
				PDFViewer.toCanvasPacket(packet, width, height);		
				
				PDFViewer.auth();

				$("#" + PDFViewer.SLIDER).hide();	
				
				//-- end redering 
				PDFViewer.initRenderList = [];
				PDFViewer.rendering = false;
				PDFViewer.initializing = false;
			}
		};
		
		PDFViewer.initializing = true;
		PDFViewer.initDoc(pdfs, opts);
		
		// setEditorPosition
		/**
		var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
		**/
		
	},
	
	draw : function(packet, callback){
		// var pdfUrl = "http://betafile.wenote.com/data/file/hashs/20/20f72661e2f0398578bc81ba7428eda7";
		// console.log(pdfUrl);
		// var pdfs = {url : packet.url, pageno : 1};
		if(PDFViewer.blocked) return;
		
		var canvasWrapper = $("#" + UI.WRAPPER);
		var width = canvasWrapper.width();
		var canvasWidth = width / 2;
		var canvasHeight = width / 2
		
		var canvasScale = width * PDFViewer.renderScale;
		this.canvasSize = [canvasScale, canvasScale];
		
		if(PDFViewer.currentPacket != null && PDFViewer.currentPacket.seqno != packet.seqno){
			// 새로운 pdf가 들어온경우 삭제하고 초기화
			PDFViewer.destroy();			
			PDFViewer.currentPacket = null;
		}
		
		if(PDFViewer.currentPacket != null){
			console.log("PDFViewer.currentPacket.docpageno : " + PDFViewer.currentPacket.docpageno + ", packet.docpageno : " + packet.docpageno);			
		}
		
		// update, remove, init 구분 필요 
		if(PDFViewer.currentPacket != null){
			var mode = packet.mode;
			if(mode == "1"){
				PDFViewer.destroy();				
			}else{
				if(PDFViewer.currentPacket.scaleh != packet.scaleh){					
					var width = PDFViewer.orgSize[0] * packet.scalew;
					var height = PDFViewer.orgSize[1] * packet.scaleh;
					
					PDFViewer.currentPacket = packet;
					
					PDFViewer.resizeCanvas(width, height);					
					PDFViewer.resizeAction(false, width, height);
					
					return;
				}				

				if(PDFViewer.currentPacket.docpageno != packet.docpageno){					
					PDFViewer.slideSmooth = false;
					
					PDFViewer.changePage(packet.docpageno, function(){
						PDFViewer.toCanvasPacket(packet);
						PDFViewer.slideSmooth = true;
					});
					
					PDFViewer.currentPacket = packet;
					return;
				}
								
				if(PDFViewer.currentPacket.posx != packet.posx || PDFViewer.currentPacket.posy != packet.posy){
					PDFViewer.toCanvasPacket(packet);					
					var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
					PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
					PDFViewer.currentPacket = packet;
				}				
			}			
			
		}else{			
			// $("#" + PDFViewer.WRAPPER).hide();
			PDFViewer.currentPacket = packet;
			PDFViewer.setEditor(packet.fname, packet.docpageno);
			PDFViewer.scale = packet.scaleh;
			
			var canvasWidth = $("#sketch1").width() * PDFViewer.renderScale;
			var canvasHeight = $("#sketch1").height() * PDFViewer.renderScale;
			
			var opts = {
				width : canvasWidth, // width
				height : canvasHeight, // height				
				callback : function(pageNumber, width, height) {
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
					
					//-- end redering 
					PDFViewer.initRenderList = [];	
					PDFViewer.rendering = false;	
 					
					$("#" + PDFViewer.SLIDER).hide();
				}
			};
			 
			//-- set editor position, 로딩때문에 이곳에 추가
			var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
			PDFViewer.setEditorPosition(pdfCanvas, packet.posx, packet.posy);
			
			$("#" + PDFViewer.WRAPPER).show()
			 
			this.initDoc(packet, opts);			
		}				
	},
	
	initDoc : function(pdfs, options) {
		// UI viewer, wrapper -> 전역변수 사용
		// file번호는 0번부터 시작하므로 1개 빼준다.
		if(pdfs == null || pdfs == '' || pdfs.length < 1){
			alert("PDF 파일 정보를 찾을 수 없습니다. 관리자에 문의해 주세요.");
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
		if(pdfHandle){
			this.destroy();
			$(pdfHandle).remove();
		}
			
		var pdfSlider = document.getElementById("pdfSlider");
		if(pdfSlider){
			this.destroy();
			$(pdfSlider).remove();
		}
		
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
						<div id=\"pdfTitle\" class=\"pdf_title\"><span>"+ fileName +"</span><a class=\"btn_x\" onclick=\"PDFViewer.remove();\"></a></div>\
						<a href=\"javascript:void(0)\" class=\"btn_left\" onclick=\"PDFViewer.prev();\"></a>\
						<a href=\"javascript:void(0)\" class=\"btn_right\" onclick=\"PDFViewer.next();\"></a>\
						<div id=\"pdfNavi\" class=\"page_navi\" style=\"display:none;\"><span id=\"page_current\">"+pageNo+"</span>/<span id=\"page_cnt\"></span></div>\
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
		// $("#" + PDFViewer.WRAPPER).show();		
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
	
	enable : function(){
		var div = document.getElementById(PDFViewer.WRAPPER);
		
		// PDFViewer.pdfs != null
		if(div && PDFViewer.pdfs != null){
			// $(div).draggable("enable");
			// $(div).resizable("enable");
			$(div).show();
		}		
	},
	
	disable : function(){
		var div = document.getElementById(PDFViewer.WRAPPER);
		if(div){
			// $(div).draggable("disable");
			// $(div).resizable("disable");
			$(div).hide();
		}
	},
	
	setEvent : function(){			
		var orgW = 0;
		var orgH = 0;
		
		var div = document.getElementById(PDFViewer.WRAPPER);		
		$(div).draggable({
			// handle : $("#pdfTitle"), 
			containment: $('#docWrapper'),
			start : function(e){
				UI.skboards[UI.current-1].clearPdf();
				$("#" + PDFViewer.SLIDER).show();
				div.style.opacity = 0.5;
			},
			drag : function(e, ui){
			},
			stop : function(e, ui){	        
		        var posX = $("#" + PDFViewer.WRAPPER).position().left;
				var posY = $("#" + PDFViewer.WRAPPER).position().top;
				 
				var packet = PDFViewer.sendPacket("2", posX, posY, PDFViewer.current, PDFViewer.scale, false);	
				PDFViewer.toCanvasPacket(packet);
				
				PDFViewer.currentPacket = packet;	
				
				div.style.opacity = 1;
				$("#" + PDFViewer.SLIDER).hide();
			}
		});		
		
		var beforeWidth = 0;
		
		$(div).resizable({
			handles : "e,s,se,n,ne,w,sw,nw",
			zIndex : 1002,
			animation : true,
			containment: $('#docWrapper'),
			aspectRatio : true,	// 정사각 비율로 resize
			minWidth : PDFViewer.orgSize[0],
			minHeight : PDFViewer.orgSize[1],			 
			start : function(e, ui){
				PDFViewer.Loader.show();
				
				UI.skboards[UI.current-1].clearPdf();
				
				beforeWidth = ui.width;
				
				$("#" + PDFViewer.SLIDER).show();
			},
			resize : function(e, ui){
				PDFViewer.Loader.show();
				
				var width = ui.size.width;
				var height = ui.size.height;

				PDFViewer.resizeCanvas(width, height);				
			},
			stop : function(e, ui){
				var width = ui.size.width;
				var height = ui.size.height; 
				
				PDFViewer.resizeAction(true, width, height);
				
				$("#" + PDFViewer.SLIDER).hide();
				PDFViewer.Loader.hide();
			}
		});
		
		this.auth();
	},
	
	resizeCanvas : function(width, height){		
		var viewerWidth = 0;
		var slideTerm = 0;
		var pageNo = PDFViewer.current;
						
		// canvas pdf clear!
		UI.skboards[UI.current-1].clearPdf();		
		
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
				// $("canvas", $(this)).width(resizeWidth);
				
				$(this).height(resizeHeight);
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
			
			PDFViewer.Loader.hide();
		});
	},
	
	resize : function(callback){		
		var canvasWidth = $("#sketch1").width() * PDFViewer.renderScale;
		var canvasHeight = $("#sketch1").height() * PDFViewer.renderScale;
		
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
			url = url.replaceAll("fb.wenote.com","test.wenote.com");				
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
				PDFViewer.destroy();	
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
		console.log("addFile");
		console.log(packet);
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
		
		pdf.getPage(pageNumber).then(function(page) {
			// 넓이기준 scale 조정
			// var scale = PDFViewer.canvasWidth / page.getViewport(1.0).width;
			// 세로기준 scale 조정
			var scale = PDFViewer.canvasHeight / page.getViewport(1.0).height;
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
			canvas.width = pageDisplayWidth;
			canvas.height = pageDisplayHeight;

			
			pageDivHolder.appendChild(canvas);

			$("#" + PDFViewer.WRAPPER).width(pageDisplayWidth);
			$("#" + PDFViewer.WRAPPER).height(pageDisplayHeight);
			
			// Render PDF page into canvas context
			var renderContext = {
				canvasContext : context,
				viewport : viewport
			};
			/** 2014.01.20 pdf렌더링 chrome crash문제 때문에 버전업하였고, callback 방식 변경됨 * */
			/** 2014.03.20 */

			var pageRendering = page.render(renderContext);
			pageRendering.promise.then(function pdfPageRenderCallback() {
				// 성공시 callback
				callback(pageDisplayWidth, pageDisplayHeight);
			}, function pdfPageRenderError(error) {
				console.log("pdfPageRenderError error !! : " + error);
			});
			
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
		var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var canvas = document.getElementById("pdf" + this.current);
		var posX = $("#" + PDFViewer.WRAPPER).position().left;
		var posY = $("#" + PDFViewer.WRAPPER).position().top;
		var width = canvas.width;
		var height = canvas.height;

		var container = document.getElementById(UI.CONTAINER);					
		posX = posX + (container.scrollLeft || container.scrollLeft);
		posY = posY + (container.scrollTop || container.scrollTop);
		 		
		// posY += $("#pdfTitle").height() + 19;

		UI.skboards[UI.current-1].drawPdf(canvas, posX, posY, width, height);		
		// $("#" + PDFViewer.WRAPPER).hide();	
	},
	
	toCanvasPacket : function(packet, width, height){		
		// var canvas = document.getElementById("pdf" + this.current);
		var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
		var canvas = document.getElementById("pdf" + this.current);
		var posX = this._getOrgX(pdfCanvas.width, pdfCanvas.height, packet.posx);
		var posY = this._getOrgY(pdfCanvas.width, pdfCanvas.height, packet.posy);
		
		var width = canvas != null ? canvas.width : width != null ? width : 0;
		var height = canvas != null ? canvas.height : height != null ? height : 0;

		// posY += ($("#pdfTitle").height() + 55);
		// posY += $("#pdfTitle").height() + 19;
		UI.skboards[UI.current-1].drawPdf(canvas, posX, posY, width, height);		
		// $("#" + PDFViewer.WRAPPER).hide();		
	},
	
	sendPacket : function(mode, x, y, pageNo, scale, isScrollSkip){
		// 0-insert or update, 1-delete
		var packet = PDFViewer.currentPacket;
		var url = packet.url;
		var pdfCanvas = UI.skboards[UI.current-1].getCanvas("pdf");
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
		this.changePage(prevPageNo, function(){
			PDFViewer.toCanvas();
			PDFViewer.paging = false;
			PDFViewer.currentPacket.docpageno = prevPageNo;
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
		this.changePage(nextPageNo, function(){
			PDFViewer.toCanvas();
			PDFViewer.paging = false;
			PDFViewer.currentPacket.docpageno = nextPageNo;
		});		
	},
	
	download : function(){
		//var url = Utils.addContext(_url("pdf.download")) + "?seqno=" + PDFViewer.currentPacket.seqno;

		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("room.pdf.download") + "?seqno=" + PDFViewer.currentPacket.seqno;;

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
		var layout = document.getElementById("pdf" + pageIdx);
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
	
	remove : function(){
		if (!PacketMgr.isMC) {
			Ctrl.Msg.auth(false);
			return;
		}
		
		Ctrl.Modal.confirm(_msg("confirm.remove.pdf"), function(){
			// remove packet 전송 
			var x = $("#" + PDFViewer.WRAPPER).position().left;
			var y = $("#" + PDFViewer.WRAPPER).position().top;
			var scale = PDFViewer.scale;
			
			PDFViewer.sendPacket("1", x, y, PDFViewer.current, scale, false);
			
			PDFViewer.destroy();			
		});
	},
	
	Loader : {
		show : function(){
			
			if(PacketMgr.isMC){
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
			
		}
	},
	
	destroy : function() {
		try{
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
			// canvas clear
			UI.skboards[UI.current-1].clearPdf();
			
			// draggable
			var div = document.getElementById(PDFViewer.WRAPPER);
			if(div){
				try{
					if(PacketMgr.isMC){
						$(div).draggable("destroy");
						$(div).resizable("destroy");
					}	
				}catch(e){
					console.log("draggable, resizable destroy fail.");
				}				
			}
			// editor clear
			div.innerHTML = "";

 			// editor hide
			$(div).hide();
			
		}catch(e){
			console.log(e);
		}
	}
};