(function () {
	"use strict"
	/**
	 * 	 @title 	  : Wenote Draw Application
	 * 	 @date 	      : 2013.11.26
	 *   @author      : kim dong hyuck 
	 * 	 @description : 화이트보드 데이터를 관리 initialize하고, destroy 한다.
	 */
	var SketchBoard = function () {
		// slideX, slideY는 슬라이드 구조의 캔버스 구성시 원본에 위치해야되는 offset좌표를 파라미터로 받는다.
		function init(container, selector, isWeb, pointers) {
			var el = select(selector);
			if (!el) return false;

			var containerEl = select(container);
			if (!containerEl) return false;

			var drawing = new Drawing(this, containerEl, el, isWeb, pointers);
			return drawing.init();
		}
		return {
			init: init
		};
	};


	/**
	 * 	 @title 	  : WhiteBoard Class
	 * 	 @date 	      : 2013.11.26
	 *   @author      : kim dong hyuck 
	 * 	 @description : 화이트보드를 ClassBase로 만들고, 공용적으로 쓸수있게 작업
	 *   
	 **/
	var Drawing = function (app, container, el, isWeb, pointers) {
		var drawingEl = el;
		var container = container;
		var width;
		var height;
		var offsetXy;
		var pageNo = 0; // canvas가 몇번째 drawing인지 저장하는 board index 번호
		var pdfCanvas;
		var imgCanvas;
		var textCanvas;
		var drawCanvas;
		var shapeCanvas;
		var pdfContext;
		var imgContext;
		var drawContext;
		var shapeContext;
		var textContext;
		var mode = 'freehand'; // freehand, eraser, pointer, view(패킷은 미존재)
		var drag = []; // 마우스 이동된 궤적
		var shapes = []; // mousedown-mousemove-mouseup 한구간에 저장된 drag 정보
		var deletedShapes = []; // redo-undo 처리를 위한 삭제된 shape 목록
		var textboxEl = null;
		var formId = null;
		var penColor = ['000', 'fff']; // 0-선색(hexa), 1-fill(hexa) 
		var pntColor = ['000']; // 포인터 색깔
		//var pntRGB = ["FF140A", "FA960A", "FAFA0A", "96FA0A", "0A96FA", "0A1496", "FA14FA"];
		var pntRGB = ["0064FA", "6400FA", "FA3232", "FA6400", "00FA64", "00C8FA", "C800FA", "FA6464", "FAC800", "00FAC8"]; // 웹앱용 색상 배열
		var fill = false; // 원, 사각형 채울지 여부
		var size = 1; // 펜 굵기 - 해상도에 맞춰서 배율 조절 필요
		var orgSize = 0; // 웹 해상도에 맞춰서 변경된 penSize    
		var alpha = 1; // 투명도 0.1~1까지 있으며 packet 서버에서 내려온값 * 0.01 해서 계산
		var line_cap = "round"; // 기본 라운드  
		var scaleX = 0; // 모바일 해상도로 sending 용도의 X좌표 배율
		var scaleY = 0; // 모바일 해상도로 sending 용도의 Y좌표 배율
		var orgScaleX = 0; // 웹 해상도에 맞춰서 변경된 x좌표 배율
		var orgScaleY = 0; // 웹 해상도에 맞춰서 변경된 y좌표 배율    
		var scalePen = 0; // 모바일 해상도에 맞춰서 변경된 펜 사이즈 배율
		var orgScalePen = 0; // 웹 해상도에 맞춰서 변경된 펜 사이즈 배율
		var stamp_kind = 0; // 1:빨강 원, 2:녹색 원, 3:파랑 원, 4:빨강 화살표, 5:녹색 화살표, 6:파랑 화살표, 7:빨강 손가락, 8:녹색 손가락, 9:파랑 손가락     
		var zoomScale = 1; // 레이저포인트 크기 싱크용 html5 canvas zoomScale
		var sZoomX = 0; // zoom 시작시 마지막에 저장된 canvas의 중심좌표
		var sZoomY = 0; // zoom 시작시 마지막에 저장된 canvas의 중심좌표
		var zoomX = 0; // 마지막으로 기록된 zoom센터 좌표 
		var zoomY = 0; // 마지막으로 기록된 zoom센터 좌표
		var hStartX = 0; // 문서 핸들링시 최초 좌표(이동거리 구하기 위함)	
		var hStartY = 0; // 문서 핸들링시 최초 좌표(이동거리 구하기 위함)
		var pSize = 40; // 포인터 높이 넓이
		var brushImg = null;
		var eventHandler = null;
		var compareX = 0;
		var slideX = 0;
		var slideY = 0;
		var isTouch = false;
		var isMouse = false;

		// 모바일일때 좌측 상단 표시
		var mobilePointerTermX = -50;
		var mobilePointerTermY = -50;

		var lastDrawingPacket = null;
		var audio = null;

		function init() {

			drawingEl.style.position = 'relative';

			_setPageNo();

			_setSize();

			// _setSound();

			/** slideX, slideY는 슬라이드 구조의 그림판인경우 offset의 좌표를 재설정해준다. */
			_setOffset(slideX, slideY);

			pdfCanvas = drawingEl.appendChild(makeCanvas({
				'cls': 'pdfCanvas',
				width: width,
				height: height,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			pdfContext = pdfCanvas.getContext('2d');
			/*
			textCanvas = drawingEl.appendChild( makeCanvas( {
				id:'txtCanvas',
				width: width, height: height,
			    style: {
			        position: 'absolute', left: 0, top: 0
			    }
			}));*/

			imgCanvas = drawingEl.appendChild(makeCanvas({
				'cls': 'imgCanvas',
				width: width,
				height: height,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			imgContext = imgCanvas.getContext('2d');

			shapeCanvas = drawingEl.appendChild(makeCanvas({
				'cls': 'shapeCanvas',
				width: width,
				height: height,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			shapeContext = shapeCanvas.getContext('2d');

			textCanvas = drawingEl.appendChild(makeCanvas({
				'cls': 'txtCanvas',
				width: width,
				height: height,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			textContext = textCanvas.getContext('2d');

			// 문서 보기모드일때 커서모양 제어
			drawCanvas = drawingEl.appendChild(makeCanvas({
				'cls': 'drawCanvas',
				width: width,
				height: height,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			drawContext = drawCanvas.getContext('2d');

			// linecap -> butt, round, square
			drawContext.lineCap = shapeContext.lineCap = line_cap;
			// linejoin -> milter, bevel, round -> 곡선을 그릴때 가장자리 모서리 처리 
			drawContext.lineJoin = shapeContext.lineJoin = 'round';

			setEvent();

			// 모바일 해상도 용 좌표 및 펜사이즈 적용 
			_calcScale(width, height);

			// 웹 scale용 좌표 및 펜사이즈 적용
			_calcOrgScale(width, height);

			return this;
		}

		// mousedown시에는 클릭시 drawing이 되지 않는 버그 발생(jquery.event.move 플러그인 문제점) 
		function _drawBegin(point) {

			// 드로잉 시작전 offset을 재설정 해준다. 해상도가 중간에 변경될수 있기 때문 
			_setOffset(slideX, slideY);

			//	레이져 포인터인경우 draw가 아닌 포인터 처리    	
			// console.log("mousedown mode : " + mode);

			// var point = getPoint(ev, offsetXy, container, 0);         
			// if(point == null) return;
			compareX = point[0];
			// console.log("[draw / _drawBegin] mode : " + mode);

			if (mode == "view") {
				// zoom 상태일때 핸들링 처리
				_makeHandleZoom(point, "began");
				/**
							listen(drawCanvas, 'mousemove', _drawMove);
							listen(drawCanvas, 'mouseup', _drawEnd);
							listen(drawCanvas, 'mouseout', _onmouseout);
				**/
				return false;

			} else if (mode == "pointer") { //-- 마우스 다운시 포인터 이미지 변경
				// 모바일은 포인터를 50씩 빼준다
				var platform = checkPlatform();
				if (platform == "ios") {
					point[0] += mobilePointerTermX;
					point[1] += mobilePointerTermY;
				}

				var newPoint = _getZoomPoint(point[0], point[1]);
				_drawPointer(drawContext, newPoint, true);

			} else {
				drawContext.strokeStyle = _getStrokeStyle();
				drawContext.lineWidth = _getLineWidth();
				drawContext.globalAlpha = _getStrokeAlpha();

				drag = [];
				// var newPoint = _getZoomPoint(point[0], point[1]);

				// point를 2개 입력해야 처음 클릭시 드로잉한
				drag.push(_getZoomPoint(point[0], point[1]));

				// if(mode == "freehand" || mode == "line" || mode == "square" || mode == "circle"){

				// 1개의 포인트 클릭시 점을 찍기위해 +1 포인트 강제 추가.
				if (mode == "freehand") {
					drag.push(_getZoomPoint(point[0] + 1, point[1]));
				}

				// 선 시리즈는 같은 지점의 2point로 시작한다.
				if (mode == "line" || mode == "square" || mode == "circle") drag.push(_getZoomPoint(point[0], point[1]));

				// drag = _getZoomPoint(point[0], point[1]);
				// console.log("drag : " + JSON.stringify(drag));
				// console.log("point[0] : " + point[0] + ", point[1] : " + point[1]);

				_drawCanvas(drawContext, false);

				//  drag = [point];;
			}

			/**
			listen(drawCanvas, 'mousemove', _drawMove);
			listen(drawCanvas, 'mouseup', _drawEnd);
			listen(drawCanvas, 'mouseout', _onmouseout);
			**/

			// 패킷은 무조건 PacketManager에 전송하고, 마스터모드 체크(룸서버에 보낼지 말지는) 는 그곳에서 체크한다.
			_makePacket(point, "began");

			if (mode == "freehand") {
				// 점 포인트 때문에 1포인트 진행된 패킷을 동시에 전송해준다. ** zoom일때 포인트 안맞음
				_makePacket([point[0] + 0.1, point[1]], "moved");
			}

			return false;
		}

		function _drawMove(point) {
			// var point = getPoint(ev, offsetXy, container, 0);
			// if(point == null) return;
			/**	
			 * 일반 drawing시 front 에만 그리고 up에서 shape 저장
			 */
			if (mode == "view") {
				// _makeHandleZoom(ev, "moved");
				_makeHandleZoom(point, "moved");

				return false;
			} else if (mode == 'freehand') {
				drag.push(_getZoomPoint(point[0], point[1]));
				// drag = [_getZoomX(point[0]), _getZoomY(point[1])];
				// drag.push(point);
				// _clear(drawContext);
				_drawCanvas(drawContext, false);

			} else if (mode == 'eraser') {
				// 지우게는 front 에 그리자마자 back에 동기화(mousedown시 바로 지워야 함)
				drag.push(_getZoomPoint(point[0], point[1]));
				// drag.push(point);

				_drawCanvas(drawContext);

				_clear(drawContext);

				_drawCanvas(shapeContext);

			} else if (mode == 'pointer') {
				// 포인터 이동시 이미지로 커서를 표시한다.
				// 모바일은 포인터를 50씩 빼준다
				var platform = checkPlatform();
				if (platform == "ios") {
					point[0] += mobilePointerTermX;
					point[1] += mobilePointerTermY;
				}

				var newPoint = _getZoomPoint(point[0], point[1]);
				_drawPointer(drawContext, newPoint);

			} else if (mode == 'square' || mode == 'circle' || mode == 'line') {
				var len = drag.length || 0;
				var beforeDrag = drag.splice(len - 1, 1);

				var x1 = point[0];
				var y1 = point[1];
				// var x2 = beforeDrag[0][0];
				// var y2 = beforeDrag[0][1];			
				var x3 = drag[drag.length - 1][0];
				var y3 = drag[drag.length - 1][1];

				if (mode != "line" && ExCall.isShift()) {
					// shift 누를시 배율 맞추기
					var termX = (x1 - x3);
					var termY = (y1 - y3);
					var isLeft = termX < 0 ? true : false;
					var isTop = termY < 0 ? true : false;

					termX = Math.abs(termX);
					termY = Math.abs(termY);

					if (termX > termY) {
						var term = termX - termY;
						y1 = (isTop) ? y1 - term : y1 + term;
					} else if (termY > termX) {
						var term = termY - termX;
						x1 = (isLeft) ? x1 - term : x1 + term;
					}
				}

				drag.push(_getZoomPoint(x1, y1));
				//    		drag.push(_getZoomPoint(point[0], point[1]));

				_clear(drawContext);

				_drawCanvas(drawContext);

				// square와 circle은 처음과 끝패킷만 보낸다. 
				return false;

			} else {
				drag[1] = point;
			}
			_makePacket(point, "moved");

			return false;
		}

		function _drawEnd(point) {
			// 포인터
			if (mode == "view") {
				// _makeHandleZoom(ev, "ended");
				_makeHandleZoom(point, "ended");

				return false;

			} else if (mode == "pointer") {
				// pointer 제거  
				_clear(drawContext);

			} else {
				// draw & eraser        	
				//_clear(drawContext);

				// 지우게는 shapeCanvas에 그리지  않는다. shape만 저장 
				if (mode != "eraser") _drawCanvas(shapeContext, true);

				_saveShape();

				_clear(drawContext);

				// alert('clear drawContext!');
			}

			// 패킷은 무조건 PacketManager에 전송하고, 마스터모드 체크(룸서버에 보낼지 말지는) 는 그곳에서 체크한다.
			_makePacket(point, "ended");

			return false;
		}

		// canvas 벗어날경우 이벤트 stop
		function _onmouseout(ev) {
			// focus 버그 발생		
			// console.log("mouseout enter!!!!!!!!!!!!!!!!!!!!!!!! ");
			_drawEnd(ev);
		}

		function _makeHandleZoom(point, mode) {
			// zoom된 상태에서만 가능
			if (zoomScale <= 1) return;

			// var point = getPoint(ev, offsetXy, container, compareX);
			// if(point == null) return;    	
			var termX = zoomScale > 1 ? (((width * zoomScale) - width) / 2) / zoomScale : 0;
			var termY = zoomScale > 1 ? (((height * zoomScale) - height) / 2) / zoomScale : 0;
			var pointX = (point[0] / zoomScale) + termX;
			var pointY = (point[1] / zoomScale) + termY;

			if (zoomScale > 1) {
				// 이동좌표 구하기 위함
				if (mode == "began") {
					hStartX = pointX;
					hStartY = pointY;
					// 시작전 중심좌표 
					sZoomX = zoomX;
					sZoomY = zoomY;
				} else {
					/*
					pointX = pointX * zoomScale;
					pointY = pointY * zoomScale;		
					**/
				}

				// 줌 handling
				ExCall.handle(mode, pageNo, pointX, pointY);
			}
		}

		function _makePacket(point, mouseMode) {
			var cmd = "";
			if (mouseMode == "began") cmd = mode == "eraser" ? "eraserbegan" : mode == "pointer" ? "cursor" : "began";
			else if (mouseMode == "moved") cmd = mode == "eraser" ? "erasermoved" : mode == "pointer" ? "cursor" : "moved";
			else if (mouseMode == "ended") cmd = mode == "eraser" ? "eraserended" : mode == "pointer" ? "cursor" : "ended";

			/*
		var point = getPoint( ev, offsetXy, container, compareX);
		if(point == null) return;
    	*/

			if (point == null) return;

			var menu = mode == "eraser" ? "5" : mode == "pointer" ? "6" : mode == "line" ? "7" : mode == "square" ? "8" : mode == "circle" ? "9" : "4";

			var pointX = 0;
			var pointY = 0;
			// 포인터는 이미지의 넓이 높이의 반만큼 빼준다.
			if (mode == "pointer") {
				if (mouseMode == "ended") {
					pointX = -9999;
					pointY = -9999;
				} else {
					// pointX = point[0];
					// pointY = point[1];
					var termX = zoomScale > 1 ? (((width * zoomScale) - width) / 2) / zoomScale : 0;
					var termY = zoomScale > 1 ? (((height * zoomScale) - height) / 2) / zoomScale : 0;

					var translateX = 0;
					var translateY = 0;
					if (zoomScale > 1 && (zoomX != 0 || zoomY != 0)) {
						// matrix는 무한대수가 나올수 있어서 floating 해줘야 한다.
						//var transform = $.browser.webkit ? 'webkitTransform' : $.browser.mozilla ? 'mozTransform' : 'transform';
						var transform = Utils.browser("msie") ? "transform" : "webkitTransform";
						var matrix = $(drawCanvas).css(transform).replace(/[^0-9\-.,]/g, '').split(',')
						translateX = parseFloat(matrix[12] || matrix[4]) / zoomScale;
						translateY = parseFloat(matrix[13] || matrix[5]) / zoomScale;
					}

					pointX = (point[0] / zoomScale) + termX - translateX;
					pointY = (point[1] / zoomScale) + termY - translateY;
				}
			} else {
				// 2015.05.27 shift로 도형을 그릴 수 있기 때문에, 포인트가 아닌 마지막 drag 포인트로 좌표를 맞춰준다. 
				if (mouseMode == "ended" && (mode == "circle" || mode == "square")) {
					point[0] = drag[drag.length - 1][0],
						point[1] = drag[drag.length - 1][1];
				}

				var termX = zoomScale > 1 ? (((width * zoomScale) - width) / 2) / zoomScale : 0;
				var termY = zoomScale > 1 ? (((height * zoomScale) - height) / 2) / zoomScale : 0;
				var translateX = 0;
				var translateY = 0;
				if (zoomScale > 1 && (zoomX != 0 || zoomY != 0)) {
					// matrix는 무한대수가 나올수 있어서 floating 해줘야 한다.
					//var transform = $.browser.webkit ? 'webkitTransform' : $.browser.mozilla ? 'mozTransform' : 'transform';
					var transform = Utils.browser("msie") ? "transform" : "webkitTransform";
					var matrix = $(drawCanvas).css(transform).replace(/[^0-9\-.,]/g, '').split(',')
					translateX = parseFloat(matrix[12] || matrix[4]) / zoomScale;
					translateY = parseFloat(matrix[13] || matrix[5]) / zoomScale;
				}

				pointX = (point[0] / zoomScale) + termX - translateX;
				pointY = (point[1] / zoomScale) + termY - translateY;
			}

			ExCall.draw(cmd, menu, pageNo, pointX, pointY);
		}

		function _clear(context) {
			context.clearRect(0, 0, width, height);
		}

		function _drawPointer(context, point) {
			var stampNo = parseInt(stamp_kind);
			/**
			var colorIdx = pntColor[0] == "FF0000" ? 1 : pntColor[0] == "E45D03" ? 2 : pntColor[0] == "FFDC00" ? 3 : pntColor[0] == "65B500" ? 4 : 
				pntColor[0] == "0E6FD3" ? 5 : (pntColor[0] == "670ACF" ||  pntColor[0] == "670A67") ? 6 : 1;
			**/
			var colorIdx = pntRGB.indexOf(pntColor[0]) > -1 ? (pntRGB.indexOf(pntColor[0]) + 1) : 1;
			var pointOrd = stampNo;
			if (colorIdx > 1) {
				pointOrd = (colorIdx - 1) * 3 + stampNo;
			}

			var pointerImage = pointers[pointOrd];
			if (pointerImage) {
				var pointerW = zoomScale < 1 ? pSize : pSize / zoomScale;
				var pointerH = zoomScale < 1 ? pSize : pSize / zoomScale;

				var halfPointerW = (pSize / 2) / zoomScale;
				var halfPointerH = (pSize / 2) / zoomScale;

				/**
				var termX = (((width * zoomScale) - width) / 2) / zoomScale;
				var termY = (((height * zoomScale) - height) / 2) / zoomScale;

				// pdfJS는 필요좌표만큼만 짤라서 그리기때문에 1배율기준값으로 구해서 드로잉한다.
				var x = (point[0] / zoomScale) < 0 ? 0 : (point[0] / zoomScale) + termX;
				var y = (point[1] / zoomScale) < 0 ? 0 : (point[1] / zoomScale) + termY;
				**/

				var drawX = (point[0] > 0) ? point[0] - halfPointerW : 0;
				var drawY = (point[1] > 0) ? point[1] - halfPointerH : 0;

				// 포인터 이미지도 투명도 영향을 받는다.
				context.globalCompositeOperation = 'source-over';
				context.globalAlpha = 1;
				context.clearRect(0, 0, width, height);
				context.drawImage(pointerImage, drawX, drawY, pointerW, pointerH);
			}
		}

		// canvas에 draw
		function _drawCanvas(context, end) {
			context.strokeStyle = _getStrokeStyle();
			context.lineWidth = _getLineWidth();
			context.lineCap = _getLineCap();
			context.globalAlpha = _getStrokeAlpha();

			var menuStr = (mode == "eraser") ? "5" : (mode == "pointer") ? "6" : (mode == "line") ? "7" : (mode == "square") ? "8" : (mode == "circle") ? "9" : "4";
			//    	console.log("[draw] - mode:" + mode + ", menu:" + menuStr + ", x:" + drag[drag.length- 1][0] + ", y:" + drag[drag.length- 1][1] + ", lineWidth:" + _getLineWidth());

			draw[mode]({
				context: context,
				end: end,
				x: drag[drag.length - 1][0],
				y: drag[drag.length - 1][1],
				lineWidth: _getLineWidth(),
				strokeStyle: _getStrokeStyle(),
				globalAlpha: _getStrokeAlpha(),
				fillStyle: _getFillStyle(),
				menu: (mode == "eraser") ? "5" : (mode == "pointer") ? "6" : (mode == "line") ? "7" : (mode == "square") ? "8" : (mode == "circle") ? "9" : "4",
				pageNo: pageNo
			});
		}

		function _saveShape() {
			if (mode != 'textbox') {
				// 현재 저장된 shape가 0개인경우, deletedShapes를 초기화 시킨다.( redo 기능 때문 )
				// 빈화면에 처음 그리는 경우 삭제되었던 shapes들 초기화 시킴
				if (shapes == null || shapes.length < 1) deletedShapes = [];

				shapes.push({
					mode: mode,
					lineWidth: _getLineWidth(),
					strokeStyle: _getStrokeStyle(),
					globalAlpha: _getStrokeAlpha(),
					fillStyle: _getFillStyle(),
					drag: drag
				});
			}
		}

		function _clearPage() {
			drawContext.strokeStyle = _getStrokeStyle();
			drawContext.lineWidth = _getLineWidth();
			drawContext.globalAlpha = _getStrokeAlpha();

			shapeContext.strokeStyle = _getStrokeStyle();
			shapeContext.lineWidth = _getLineWidth();
			shapeContext.globalAlpha = _getStrokeAlpha();

			drag = [];
			shapes = [];
			deletedShapes = [];

			_clear(drawContext);
			_clear(shapeContext);
			_clear(textContext);
		}

		function _getMidPoint(p1, p2) {
			return [
				p1[0] + (p2[0] - p1[0]) / 2,
				p1[1] + (p2[1] - p1[1]) / 2
			];
		}

		// draw controll
		var draw = {
			// 선그리기
			/** 일반 path drawing
        freehand: function(o) {
        	// zoom scale 계산
        	// globalCompositeOperation은 기본 source-over 이다.        	
			o.context.globalCompositeOperation = 'source-over';
        	o.context.beginPath();
        	for (var i = 0; i < drag.length; i++){
            	o.context[i == 0 ? 'moveTo' : 'lineTo'](drag[i][0], drag[i][1]);
            }
            o.context.stroke();
            o.context.closePath();
        },
        ***/

			// Bezier Curve
			/**
        freehand: function(o) {			
        	// zoom scale 계산
        	// globalCompositeOperation은 기본 source-over 이다.        	
			o.context.globalCompositeOperation = 'source-over';
        	o.context.beginPath();
        	for (var i = 0; i < drag.length; i++){
        		if(i == 0){
        			o.context.moveTo(drag[i][0], drag[i][1]);
        		}else{
        			if((i+1) >= drag.length){
        				o.context.lineTo(drag[i][0], drag[i][1]);
        			}else{
        				// bezier curve
        				var p1 = [drag[i][0], drag[i][1]];
            			var p2 = [drag[i+1][0], drag[i+1][1]];
            			var midPoint = _getMidPoint(p1, p2);
            			
            			o.context.quadraticCurveTo(p1[0], p1[1], midPoint[0], midPoint[1]);            			
        			}
        		}
        	}
        	
        	// Draw last line as a straight line while
			// we wait for the next point to be able to calculate
			// the bezier control point
			// o.context.lineTo(drag[drag.length-1][0], drag[drag.length-1][1]);
            o.context.stroke();
            o.context.closePath();
			//console.log("[draw / freehand mode] " + new Date().getTime());
        },
        ***/

			// 불필요하게 drawing 된 point를 제거하고 line으로 연결하는 방식으로 수정
			freehand: function (o) {
				// zoom scale 계산
				// globalCompositeOperation은 기본 source-over 이다.
				var xLast = 0;
				var yLast = 0;
				var brushDiameter = 7;
				var end = o.end;
				var isHistory = typeof (o.isHistory) == "undefined" ? false : o.isHistory;

				var drawPen = function () {
					o.context.globalCompositeOperation = 'source-over';
					o.context.beginPath();

					for (var i = 0; i < drag.length; i++) {
						if (i == 0) {
							o.context.moveTo(drag[i][0], drag[i][1]);
						} else {
							if ((i + 1) >= drag.length) {
								o.context.lineTo(drag[i][0], drag[i][1]);
							} else {
								// bezier curve
								var p1 = [drag[i][0], drag[i][1]];
								var p2 = [drag[i + 1][0], drag[i + 1][1]];
								var midPoint = _getMidPoint(p1, p2);

								o.context.quadraticCurveTo(p1[0], p1[1], midPoint[0], midPoint[1]);
							}
						}
					}

					// Draw last line as a straight line while
					// we wait for the next point to be able to calculate
					// the bezier control point
					// o.context.lineTo(drag[drag.length-1][0], drag[drag.length-1][1]);
					o.context.stroke();
					o.context.closePath();
				}

				if (end == true) {

					drawPen();

				} else {
					// 2016.12.26 히스토리 드로잉 동작 부분 스킵
					// 히스토리로 드로잉 하는 경우 move 과정을 생략한다.
					if (isHistory) return;

					_clear(o.context);

					drawPen();

					/*
					o.context.globalCompositeOperation = 'source-over';
					o.context.beginPath();
					// bezier curve
					if (drag.length > 1) {
						var p1 = [drag[drag.length - 2][0], drag[drag.length - 2][1]];
						var p2 = [drag[drag.length - 1][0], drag[drag.length - 1][1]];
						var midPoint = _getMidPoint(p1, p2);

						o.context.moveTo(p1[0], p1[1]);
						o.context.lineTo(p2[0], p2[1]);
						// o.context.quadraticCurveTo(p1[0], p1[1], midPoint[0], midPoint[1]);
					}
					o.context.stroke();
					o.context.closePath();
					*/

				}
				// 분필 효과 
				/**
        	for (var i = 0; i < drag.length; i++){
        		if(i == 0){
        			xLast = drag[i][0];
        			yLast = drag[i][1];
        		}else{
        			var x = drag[i][0];
         			var y = drag[i][1];
         			
console.log("x : " + x + ", y : " + y);
         			
         			var length = Math.round(Math.sqrt(Math.pow(x-xLast,2)+Math.pow(y-yLast,2))/(5/brushDiameter));
          			var xUnit = (x-xLast)/length;
          			var yUnit = (y-yLast)/length;
          			 
          			for(var j=0; j<length; j++ ){
          				var xCurrent = xLast+(j*xUnit);	
          				var yCurrent = yLast+(j*yUnit);
          				var xRandom = xCurrent+(Math.random()-0.5)*brushDiameter*1.2;			
          				var yRandom = yCurrent+(Math.random()-0.5)*brushDiameter*1.2;
          				
          				o.context.clearRect( xRandom, yRandom, Math.random()*2+2, Math.random()+1);
          			}
          			
          			xLast = x;
          			yLast = y;  
        		}            		
        	}
        	***/
			},

			line: function (o) {
				o.context.globalCompositeOperation = 'source-over';
				o.context.beginPath();

				o.context.moveTo(drag[0][0], drag[0][1]);
				o.context.lineTo(o.x, o.y);
				o.context.stroke();
				o.context.closePath();
				//            console.log("[draw / line mode] " + new Date().getTime());
			},

			chalk: function (o) {
				var xLast = 0;
				var yLast = 0;
				var brushDiameter = 7;
				var end = o.end;
				var isHistory = typeof (o.isHistory) == "undefined" ? false : o.isHistory;

				if (end == true) {
					shapeContext.drawImage(drawCanvas, 0, 0);
				} else {
					// 2016.12.26 히스토리 드로잉 동작 부분 스킵
					// 히스토리로 드로잉 하는 경우 move 과정을 생략한다.
					if (isHistory) return;

					var x = 0;
					var y = 0;

					if (drag.length == 1) {
						xLast = drag[0][0];
						yLast = drag[0][1];
						x = xLast + 1;
						y = yLast + 1;
					} else {
						xLast = drag[drag.length - 2][0];
						yLast = drag[drag.length - 2][1];
						x = drag[drag.length - 1][0];
						y = drag[drag.length - 1][1];
					}

					o.context.beginPath();
					o.context.moveTo(xLast, yLast);
					o.context.lineTo(x, y);
					o.context.stroke();

					var length = Math.round(Math.sqrt(Math.pow(x - xLast, 2) + Math.pow(y - yLast, 2)) / (5 / brushDiameter));
					var xUnit = (x - xLast) / length;
					var yUnit = (y - yLast) / length;
					for (var j = 0; j < length; j++) {
						var xCurrent = xLast + (j * xUnit);
						var yCurrent = yLast + (j * yUnit);
						var xRandom = xCurrent + (Math.random() - 0.5) * brushDiameter * 1.2;
						var yRandom = yCurrent + (Math.random() - 0.5) * brushDiameter * 1.2;

						o.context.clearRect(xRandom, yRandom, Math.random() * 2 + 2, Math.random() + 1);
					}
				}

			},

			brush: function (o) {
				// 이미지로 drawing하기 때문에 크기나 색상조절이 안된다.
				function distanceBetween(point1, point2) {
					return Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
				}

				function angleBetween(point1, point2) {
					return Math.atan2(point2.x - point1.x, point2.y - point1.y);
				}

				if (brushImg == null) {
					brushImg = new Image();
					brushImg.src = "../res/images/brush.png";
					// 'http://www.tricedesigns.com/wp-content/uploads/2012/01/brush2.png';
				}

				o.context.globalCompositeOperation = 'source-over';
				o.context.beginPath();

				var lastPoint = drag.length > 0 ? {
					x: drag[0][0],
					y: drag[0][1]
				} : null;
				for (var i = 0; i < drag.length; i++) {
					// o.context[i == 0 ? 'moveTo' : 'lineTo'](drag[i][0], drag[i][1]);        		
					var currentPoint = {
						x: drag[i][0],
						y: drag[i][1]
					};
					var dist = distanceBetween(lastPoint, currentPoint);
					var angle = angleBetween(lastPoint, currentPoint);

					for (var j = 0; j < dist; j++) {
						x = lastPoint.x + (Math.sin(angle) * j) - 25;
						y = lastPoint.y + (Math.cos(angle) * j) - 25;
						o.context.drawImage(brushImg, x, y);
					}

					lastPoint = currentPoint;
				}
				o.context.stroke();
				o.context.closePath();
				//        	console.log("[draw / brush mode] " + new Date().getTime());
			},

			// 사각형
			square: function (o) {
				o.context.globalCompositeOperation = 'source-over';
				o.context.beginPath();
				for (var i = 0; i < drag.length; i++) {
					//if(o.fillStyle && o.fillStyle.toLowerCase() != "#fff" && o.fillStyle.toLowerCase() != "#ffffff"){
					if (o.fillStyle != null && o.fillStyle != "" && o.fillStyle != "#") {
						o.context.fillStyle = o.fillStyle;
						o.context.fillRect(drag[i][0], drag[i][1], o.x - drag[i][0], o.y - drag[i][1]);
					}

					o.context.strokeRect(drag[i][0], drag[i][1], o.x - drag[i][0], o.y - drag[i][1]);
				}
				o.context.closePath();
			},
			// 원
			circle: function (o) {
				// 1포인트는 skip한다.
				if (drag != null && drag.length == 1) return;

				o.context.globalCompositeOperation = 'source-over';
				o.context.beginPath();

				// console.log("ExCall.isShift : " + ExCall.isShift() );
				for (var i = 0; i < drag.length; i++) {
					var x1 = drag[i][0],
						y1 = drag[i][1],
						x2 = o.x,
						y2 = o.y;

					var p = 4 * ((Math.sqrt(2) - 1) / 3),
						rx = (x2 - x1) / 2,
						ry = (y2 - y1) / 2,
						cx = x1 + rx,
						cy = y1 + ry;
					// console.log("x1 : " + x1 + ", x2 : " + x2 + ", y1 : " + y1 + ", y2 : " + y2 + ", p : " + p);

					o.context.moveTo(cx, cy - ry);
					o.context.bezierCurveTo(cx + (p * rx), cy - ry, cx + rx, cy - (p * ry), cx + rx, cy);
					o.context.bezierCurveTo(cx + rx, cy + (p * ry), cx + (p * rx), cy + ry, cx, cy + ry);
					o.context.bezierCurveTo(cx - (p * rx), cy + ry, cx - rx, cy + (p * ry), cx - rx, cy);
					o.context.bezierCurveTo(cx - rx, cy - (p * ry), cx - (p * rx), cy - ry, cx, cy - ry);

					if (o.fillStyle != null && o.fillStyle != "" && o.fillStyle != "#") {
						o.context.fillStyle = o.fillStyle;
						o.context.fill();
					}
				}

				// closePath를 먼저 해주지 않으면 우측 하단에 점 포인트가 생긴다.
				o.context.closePath();
				o.context.stroke();
			},
			// 글씨 박스
			textbox: function (o) {
				var _getLineHeight = function (size, face) {
					var line = document.getElementById("fk_2");
					if (line == null) {
						var body = document.body;
						line = document.createElement('div');
						line.id = "fk_2";
						// line.style.position = 'absolute';
						// line.style.whiteSpace = 'nowrap';
						line.style.font = size + 'px ' + face;
						line.innerHTML = 'm';
						body.appendChild(line);
					} else {
						line.style.font = size + 'px ' + face;
						line.innerHTML = 'm';
					}
					var height = line.offsetHeight;
					$(line).remove();

					return height;
				}

				var _drawTextToCanvas = function (ctx, text, x, y, size, maxWidth, maxHeight, face) {
					y += parseInt(size, 10);

					var fontScale = face == "Arial" ? 1.4 : face == "calibri" ? 1.4 : face == "Georgia" ? 1.25 : face == "Tahoma" ? 1.55 : face == "Verdana" ? 1.45 : face == "Comic Sans MS" ? 1.61 : 1.4;
					var charWidth = ctx.measureText("M").width;
					// var lineHeight = charWidth * fontScale;
					var lineHeight = _getLineHeight(size, face);
					/**
									text = text.replace(/(\r\n|\n\r|\r|\n)/g, "\n");
									text = text.replace(/(\t)/g, "        ");
					**/
					var lines = text.split('\n');
					for (var i = 0; i < lines.length; i++) {
						var words = lines[i];
						ctx.fillText(words, x, y);

						y += lineHeight;
					}
				}

				o.context.globalCompositeOperation = 'source-over';
				o.context.fillStyle = o.fillStyle;
				o.context.globalAlpha = 1;
				o.context.font = (o.bold == "1" ? "bold " : "") + (o.italic == "1" ? "italic " : "") + (parseInt(o.size, 10)) + "px " + o.face;
				// o.context.txt2canvas(o.text, o.x, o.y, o.size, o.w, o.h, o.face);

				_drawTextToCanvas(o.context, o.text, o.x, o.y, o.size, o.w, o.h, o.face);

			},
			// 지우게 
			eraser: function (o) {
				var oldStrokeStyle = o.context.strokeStyle,
					oldLineWidth = o.context.lineWidth,
					oldGlobalAlpha = o.context.globalAlpha,
					penColor = drawingEl.style.backgroundColor;

				// text와 shape 영역 같이 지워지게 처리 
				var mainCtx = o.context;
				var subCtx = typeof (o.context2) != "undefined" ? o.context2 : textContext;

				mainCtx.globalCompositeOperation = subCtx.globalCompositeOperation = 'destination-out';
				mainCtx.strokeStyle = subCtx.strokeStyle = 'rgba(0, 0, 0, 1)';
				mainCtx.lineWidth = subCtx.lineWidth = oldLineWidth;
				mainCtx.globalAlpha = subCtx.globalAlpha = 1;
				mainCtx.beginPath();
				subCtx.beginPath();

				for (var i = 0; i < drag.length; i++) {
					mainCtx[i == 0 ? 'moveTo' : 'lineTo'](drag[i][0], drag[i][1]);
					subCtx[i == 0 ? 'moveTo' : 'lineTo'](drag[i][0], drag[i][1]);
				}

				mainCtx.stroke();
				mainCtx.closePath();

				subCtx.stroke();
				subCtx.closePath();

				mainCtx.strokeStyle = mainCtx.strokeStyle = oldStrokeStyle;
				mainCtx.lineWidth = mainCtx.lineWidth = oldLineWidth;
				mainCtx.globalAlpha = mainCtx.globalAlpha = oldGlobalAlpha;

				//-- text 부분  지우게 ------------------
				/***
				subCtx.globalCompositeOperation = 'destination-out';
				subCtx.strokeStyle = 'rgba(0, 0, 0, 1)';
				subCtx.lineWidth = oldLineWidth;
				subCtx.globalAlpha = 1;
				subCtx.beginPath();
				for (var i = 0; i < drag.length; i++ ) {
					subCtx[i == 0 ? 'moveTo' : 'lineTo'](drag[i][0], drag[i][1]);            	
				}            
				subCtx.stroke();
				subCtx.closePath();
				subCtx.strokeStyle = oldStrokeStyle;
				subCtx.lineWidth = oldLineWidth;
				subCtx.globalAlpha = oldGlobalAlpha;
				***/
				//------- end  text

				if (o.call) {
					// *** 이 방식은 첫번째 패킷과 마지막 패킷은 중복된다. ** 차후 수정할것
					for (var i = 0; i < drag.length; i++) {
						var cmd = o.menu == "5" ? "erasermoved" : o.menu == "6" ? "cursor" : "moved";
						o.call(cmd, o.menu, o.pageNo, drag[i][0], drag[i][1]);
					}
				}
			}
		};

		// 선 색깔
		function _getStrokeStyle() {
			return '#' + penColor[0];
		}

		function _getStrokeAlpha() {
			// return '#' + penColor[ 0 ];
			return alpha;
		}

		// 채우기 색깔
		function _getFillStyle() {
			return '#' + penColor[1];
		}

		// 선 사이즈 
		function _getLineWidth() {
			return size;
		}

		function _getLineCap() {
			return line_cap;
		}

		function _setSize(w, h) {
			width = drawingEl.clientWidth;
			height = drawingEl.clientHeight;

			if (drawCanvas) {
				drawCanvas.width = width;
				drawCanvas.height = height;
			}

			// resize 시
			if (shapeCanvas) {
				shapeCanvas.width = width;
				shapeCanvas.height = height;
			}
		}

		function _setSound() {
			var path = Utils.addBase("fb/" + _url("sound.path") + "chalk.wav");
			audio = new Audio(path);
			audio.loop = true;
		}

		// 0-pl
		function _actionSound(status) {

			if (audio == null) return;

			if (status == "1") {
				audio.play();
			} else {
				audio.pause();
				audio.currentTime = 0;
			}
		}

		function _setPageNo() {
			pageNo = parseInt(el.id.replaceAll("sketch", ""), 10);
		}

		// slide resize
		function _setOffset(resetX, resetY) {
			slideX = resetX;
			slideY = resetY;

			offsetXy = offset(drawingEl);

			if (slideX > 0) offsetXy[0] = offsetXy[0] - slideX;
			if (slideY > 0) offsetXy[1] = offsetXy[1] - slideY;
		}

		function _clearRectAll() {
			/**
    	if(drawContext) drawContext.clearRect(0, 0, width, height);
    	if(shapeContext) shapeContext.clearRect(0, 0, width, height);
	**/

			drag = [];
			shapes = [];
			deletedShapes = [];

			_clear(drawContext);
			_clear(shapeContext);
		}

		// Canvas 리사이즈
		function resize(w, h, offsetX, offsetY) {

			// 리사이징시 다시 드로잉하기 때문에 기존 shape들을 모두 날려줘야 한다.
			_clearPage();

			// size 재설정
			_setSize();

			/*// point 재설정
    	_setOffset(offsetX, offsetY);     
    	
    	// 좌표 재설정 - 모바일 해상도 용 좌표 및 펜사이즈 적용 
        _calcScale(w, h);
        
        // 좌표 재설정 - 웹 scale용 좌표 및 펜사이즈 적용
        _calcOrgScale(w, h);
*/
			/*    	drawContext.lineCap = shapeContext.lineCap = 'round';
			        drawContext.lineJoin = shapeContext.lineJoin = 'round';*/

		}

		// Canvas 리사이즈
		function resizeTest() {
			// 리사이징시 다시 드로잉하기 때문에 기존 shape들을 모두 날려줘야 한다.
			_clearPage();

			// size 재설정
			_setSize();
		}

		// zoom 초기화
		function clearZoom() {
			$(drawCanvas).css('-moz-transform', '')
				.css('-webkit-transform', '')
				.css('-o-transform', '')
				.css('transform', '')
				.css('ms-transform', '');

			$(shapeCanvas).css('-moz-transform', '')
				.css('-webkit-transform', '')
				.css('-o-transform', '')
				.css('transform', '')
				.css('ms-transform', '');
		}

		// history back
		function undo() {
			// 더이상 shape가 없는경우 deleteShape에 저장하지 않는다.
			if (shapes.length < 1) return;

			_clear(shapeContext);
			_clear(textContext);

			for (var i = 0; i < shapes.length - 1; i++) {
				var shape = shapes[i];
				// 가비지 shape가 저장된 경우나 shape에 태그가 저장된경우 return
				if (typeof (shape) == "undefined" || (typeof (shape.tagName) != "undefined" && shape.tagName)) continue;

				if (shape.mode == 'textbox') {

					draw[shape.mode]({
						mode: shape.mode,
						context: textContext,
						text: shape.text,
						x: shape.x,
						y: shape.y,
						w: shape.w,
						h: shape.h,
						face: shape.face,
						italic: shape.italic,
						bold: shape.bold,
						size: shape.size,
						globalAlpha: 1,
						fillStyle: shape.fillStyle
					});

				} else {
					shapeContext.lineWidth = shape.lineWidth;
					shapeContext.strokeStyle = shape.strokeStyle;
					shapeContext.globalAlpha = shape.globalAlpha;

					drag = shape.drag;

					draw[shape.mode]({
						context: shapeContext,
						end: true,
						x: drag[drag.length - 1][0],
						y: drag[drag.length - 1][1],
						lineWidth: shape.lineWidth,
						strokeStyle: shape.strokeStyle,
						globalAlpha: shape.globalAlpha,
						fillStyle: shape.fillStyle
					});
				}
			}

			var lastShape = shapes[shapes.length - 1];
			if (lastShape && lastShape.tagName) { // textbox
				lastShape.parentNode.removeChild(lastShape);
			}

			// 삭제된 shape에 저장한다.
			var shape = shapes.pop();
			deletedShapes.push(shape);
		}

		// history forword
		function redo() {
			if (deletedShapes != null && deletedShapes.length < 1) return;

			shapes.push(deletedShapes.pop());

			_clear(shapeContext);
			_clear(textContext);

			for (var i = 0; i < shapes.length; i++) {
				var shape = shapes[i];
				if (typeof (shape) == "undefined" || (typeof (shape.tagName) != "undefined" && shape.tagName)) continue;

				if (shape.mode == 'textbox') {
					draw[shape.mode]({
						mode: shape.mode,
						context: textContext,
						text: shape.text,
						x: shape.x,
						y: shape.y,
						w: shape.w,
						h: shape.h,
						face: shape.face,
						italic: shape.italic,
						bold: shape.bold,
						size: shape.size,
						globalAlpha: 1,
						fillStyle: shape.fillStyle
					});

				} else {
					shapeContext.lineWidth = shape.lineWidth;
					shapeContext.strokeStyle = shape.strokeStyle;
					shapeContext.globalAlpha = shape.globalAlpha;

					drag = shape.drag;

					draw[shape.mode]({
						context: shapeContext,
						end: true,
						x: drag[drag.length - 1][0],
						y: drag[drag.length - 1][1],
						lineWidth: shape.lineWidth,
						strokeStyle: shape.strokeStyle,
						globalAlpha: shape.globalAlpha,
						fillStyle: shape.fillStyle
					});
				}
			}

			var lastShape = shapes[shapes.length - 1];
			if (lastShape && lastShape.tagName) { // textbox
				lastShape.parentNode.removeChild(lastShape);
			}
		}

		// 특정 패킷만 
		function updateText(id, options) {

			if (shapes.length < 1) return;

			textContext.clearRect(0, 0, width, height);

			var updateIndex = -1;
			for (var i = 0; i < shapes.length; i++) {
				var shape = shapes[i];

				// 가비지 shape가 저장된 경우나 shape에 태그가 저장된경우 return
				if (typeof (shape) == "undefined" || (typeof (shape.tagName) != "undefined" && shape.tagName)) continue;

				if (shape.mode == 'textbox') {

					if (id == shape.id) {
						var fColor = "#" + Async.rgbToHex(options.r, options.g, options.b);
						var leftX = _getOrgX(options.x);
						var topY = _getOrgY(options.y);
						var updateShape = {
							mode: "textbox",
							context: textContext,
							id: options.id,
							text: options.text,
							x: leftX,
							y: topY,
							w: options.w,
							h: options.h,
							face: options.face,
							italic: options.italic,
							bold: options.bold,
							size: options.size,
							globalAlpha: 1,
							fillStyle: fColor
						}

						draw[shape.mode](updateShape);

						shapes[i] = updateShape;

					} else {
						draw[shape.mode]({
							mode: shape.mode,
							context: textContext,
							text: shape.text,
							x: shape.x,
							y: shape.y,
							w: shape.w,
							h: shape.h,
							face: shape.face,
							italic: shape.italic,
							bold: shape.bold,
							size: shape.size,
							globalAlpha: 1,
							fillStyle: shape.fillStyle
						});
					}
				}

			}
			// shape에서 삭제한다.                 
			// 삭제된 shape에 저장한다.
			// var shape = shapes.pop();     
		}

		function removeText(id) {
			if (shapes.length < 1) return;

			textContext.clearRect(0, 0, width, height);

			var removeIndex = -1;
			for (var i = 0; i < shapes.length; i++) {
				var shape = shapes[i];

				// 가비지 shape가 저장된 경우나 shape에 태그가 저장된경우 return
				if (typeof (shape) == "undefined" || (typeof (shape.tagName) != "undefined" && shape.tagName)) continue;

				if (shape.mode == 'textbox') {
					if (id != shape.id) {
						draw[shape.mode]({
							mode: shape.mode,
							context: textContext,
							text: shape.text,
							x: shape.x,
							y: shape.y,
							w: shape.w,
							h: shape.h,
							face: shape.face,
							italic: shape.italic,
							bold: shape.bold,
							size: shape.size,
							globalAlpha: 1,
							fillStyle: shape.fillStyle
						});
					} else {
						removeIndex = i;
					}
				}
			}

			// shape에서 삭제한다.
			var shape = null;
			if (removeIndex > -1) {
				shape = shapes.splice(removeIndex, 1);
			}
		}

		function removeTextShape(id) {
			if (shapes.length < 1) return;

			var removeIndex = -1;
			for (var i = 0; i < shapes.length; i++) {
				var shape = shapes[i];

				// 가비지 shape가 저장된 경우나 shape에 태그가 저장된경우 return
				if (typeof (shape) == "undefined" || (typeof (shape.tagName) != "undefined" && shape.tagName)) continue;

				if (shape.mode == 'textbox') {
					if (id == shape.id) {
						removeIndex = i;
					}
				}
			}

			// shape에서 삭제한다.
			var shape = null;
			if (removeIndex > -1) {
				shape = shapes.splice(removeIndex, 1);
			}
		}

		function save() {
			var saveCanvas = drawingEl.appendChild(makeCanvas({
				id: 'saveCanvas',
				width: width,
				height: height,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			var saveContext = saveCanvas.getContext('2d');
			saveContext.clearRect(0, 0, width, height);

			// 기본 background color와 image를 적용한 
			var sketch = saveCanvas.parentNode;

			// var bgColor = sketch.style.backgroundColor != null ? sketch.style.backgroundColor.replace("rgb(", "").replace(")", "").split(",") : null;
			var bgColor = null;
			if (sketch.style.backgroundColor != null) {
				bgColor = sketch.style.backgroundColor.indexOf("rgba") > -1 ? sketch.style.backgroundColor.replace("rgba(", "").replace(")", "").split(",") :
					sketch.style.backgroundColor.replace("rgb(", "").replace(")", "").split(",");
			}

			var imageLink = sketch.style.backgroundImage != null ? sketch.style.backgroundImage.slice(4, -1).replaceAll("\"", "") : null;
			if (bgColor != null && bgColor != "" && bgColor.length == 3) {
				saveContext.fillStyle = "#" + Async.rgbToHex(bgColor[0], bgColor[1], bgColor[2]);
				saveContext.fillRect(0, 0, width, height);
			} else {
				// 색이 없는 경우 기본 흰색처리
				saveContext.fillStyle = "#fff";
				saveContext.fillRect(0, 0, width, height);
			}

			if (imageLink != null && imageLink != "") {
				var bgImg = new Image();
				bgImg.src = imageLink;
				var ptrn = saveContext.createPattern(bgImg, 'repeat'); // Create a pattern with this image, and set it to "repeat".
				saveContext.fillStyle = ptrn;
				saveContext.fillRect(0, 0, width, height);
			}

			saveContext.drawImage(pdfCanvas, 0, 0);
			saveContext.drawImage(imgCanvas, 0, 0);
			saveContext.drawImage(shapeCanvas, 0, 0);
			saveContext.drawImage(textCanvas, 0, 0);
		}

		function saveCapture(paramX, paramY, paramWidth, paramHeight) {

			var sx = typeof paramX == 'undefined' ? 0 : paramX;
			var sy = typeof paramY == 'undefined' ? 0 : paramY;
			var swidth = typeof paramWidth == 'undefined' ? width : paramWidth;
			var sheight = typeof paramHeight == 'undefined' ? height : paramHeight;

			var saveCanvas = drawingEl.appendChild(makeCanvas({
				id: 'saveCanvas',
				width: swidth,
				height: sheight,
				style: {
					position: 'absolute',
					left: 0,
					top: 0
				}
			}));

			var saveContext = saveCanvas.getContext('2d');
			saveContext.clearRect(0, 0, swidth, sheight);

			// 기본 background color와 image를 적용한 
			var sketch = saveCanvas.parentNode;

			// var bgColor = sketch.style.backgroundColor != null ? sketch.style.backgroundColor.replace("rgb(", "").replace(")", "").split(",") : null;
			var bgColor = null;
			if (sketch.style.backgroundColor != null) {
				bgColor = sketch.style.backgroundColor.indexOf("rgba") > -1 ? sketch.style.backgroundColor.replace("rgba(", "").replace(")", "").split(",") :
					sketch.style.backgroundColor.replace("rgb(", "").replace(")", "").split(",");
			}

			var imageLink = sketch.style.backgroundImage != null ? sketch.style.backgroundImage.slice(4, -1).replaceAll("\"", "") : null;
			if (bgColor != null && bgColor != "" && bgColor.length == 3) {
				saveContext.fillStyle = "#" + Async.rgbToHex(bgColor[0], bgColor[1], bgColor[2]);
				saveContext.fillRect(0, 0, swidth, sheight);
			} else {
				// 색이 없는 경우 기본 흰색처리
				saveContext.fillStyle = "#fff";
				saveContext.fillRect(0, 0, swidth, sheight);
			}

			if (imageLink != null && imageLink != "") {
				var bgImg = new Image();
				bgImg.src = imageLink;
				var ptrn = saveContext.createPattern(bgImg, 'repeat'); // Create a pattern with this image, and set it to "repeat".
				saveContext.fillStyle = ptrn;
				saveContext.fillRect(0, 0, swidth, sheight);
			}

			saveContext.drawImage(pdfCanvas, sx, sy, swidth, sheight, 0, 0, swidth, sheight);
			saveContext.drawImage(imgCanvas, sx, sy, swidth, sheight, 0, 0, swidth, sheight);
			saveContext.drawImage(shapeCanvas, sx, sy, swidth, sheight, 0, 0, swidth, sheight);
			saveContext.drawImage(textCanvas, sx, sy, swidth, sheight, 0, 0, swidth, sheight);
		}

		// draw.js -> packetManager를 통해 room 서버로 콜하는 메소드 
		var ExCall = {
			draw: function (cmd, menu, pageNo, pointX, pointY) {
				// console.log("cmd : " + cmd + ", menu : " + menu + ", pageNo : " + pageNo + ", pointX : " + pointX + ", pointY : " + pointY);

				var fixedX = (pointX > -9999) ? _getFixedX(pointX) : pointX;
				var fixedY = (pointY > -9999) ? _getFixedY(pointY) : pointY;

				// console.log("pointX : " + pointX + ", pointY : " + pointY + ", fixedX : " + fixedX + ", fixedY : " + fixedY);

				PacketMgr.Master.draw(cmd, menu, pageNo, fixedX, fixedY, fill);
			},

			// zoom handling
			handle: function (mode, pageNo, pointX, pointY) {
				var hStartFixedX = (hStartX > -9999) ? _getFixedX(hStartX) : hStartX;
				var hStartFixedY = (hStartY > -9999) ? _getFixedY(hStartY) : hStartY;

				var fixedX = (pointX > -9999) ? _getFixedX(pointX) : pointX;
				var fixedY = (pointY > -9999) ? _getFixedY(pointY) : pointY;

				// mode, zoomScale, pageNo, hStartX, hStartY, pointX, pointY, pointFixedX, pointFixedY, orgX, orgY
				PacketMgr.Master.zoomHandle(mode, zoomScale, pageNo, hStartFixedX, hStartFixedY, pointX, pointY, fixedX, fixedY, sZoomX, sZoomY);
			},

			isShift: function () {
				return UI.isShift;
			},

			auth: function () {
				return PacketMgr.isMC;
			},

			hideToolMenu: function () {
				var platform = checkPlatform();
				if (platform == "web") {
					// 드로잉 툴바 touch 시 제거
					Ctrl.hideToolMenu();
				}
			}
		};

		// native code로 들어온 좌표
		var AsyncNt = {
			began: function (action, x, y) {
				// 2016.06.28  모바일일때 좌측 상단으로 표시한다.
				if (mode == "pointer") {
					x += mobilePointerTermX;
					y += mobilePointerTermY;
				}
				var point = getNtPoint(offsetXy, container, compareX, action, x, y);
				_drawBegin(point);
			},

			moved: function (action, x, y) {
				// 2016.06.28  모바일일때 좌측 상단으로 표시한다.
				if (mode == "pointer") {
					x += mobilePointerTermX;
					y += mobilePointerTermY;
				}
				var point = getNtPoint(offsetXy, container, compareX, action, x, y);
				_drawMove(point);
			},

			ended: function (action, x, y) {
				// 2016.06.28  모바일일때 좌측 상단으로 표시한다.
				if (mode == "pointer") {
					x += mobilePointerTermX;
					y += mobilePointerTermY;
				}
				var point = getNtPoint(offsetXy, container, compareX, action, x, y);
				_drawEnd(point);
			}
		}

		// 외부에서 그린경우 DataSync, 패킷의 cmd 명으로 함수 호출
		var Async = {
			draw: function (options, isHistory) {
				var cmd = options.cmd != null ? options.cmd.toLowerCase() : options.from != null ? options.from.toLowerCase() : "";
				var posX = _getOrgX(options.locationx);
				var posY = _getOrgY(options.locationy);
				var menuSelect = options.menuselect || 4;

				// console.log("orgScaleX : " + orgScaleX + ", orgScaleY : " + orgScaleY);
				// console.log("options.locationx : " + options.locationx + ", posX : " + posX + ", options.locationy : " + options.locationy + ", posY : " + posY);
				if (cmd == 'began') drag = [];
				drag.push([posX, posY]);
				// setMode - draw

				// 2014.12.15 내가 마스터가 될수도 있기때문에 setMode는 하지않고 드로잉하게 한다.
				// mode = "freehand";
				var drawMode = menuSelect == 7 ? "line" : menuSelect == 8 ? "square" : menuSelect == 9 ? "circle" : "freehand";
				this.front(drawMode, (cmd == "ended") ? true : false, isHistory);

				if (cmd == 'ended') {
					this.shape(drawMode, (cmd == "ended") ? true : false);
					drag = [];
					lastDrawingPacket = null;
				} else {
					lastDrawingPacket = options;
				}
			},

			eraser: function (options) {
				var cmd = options.cmd != null ? options.cmd.toLowerCase() : options.from != null ? options.from.toLowerCase() : "";
				var posX = _getOrgX(options.locationx);
				var posY = _getOrgY(options.locationy);

				if (cmd == "eraserbegan" || cmd == "began") drag = [];
				drag.push([posX, posY]);

				this.frontEraser("eraser");

				if (cmd == 'eraserended' || cmd == "ended") {
					this.shapeEraser("eraser");
					drag = [];
					lastDrawingPacket = null;
				} else {
					lastDrawingPacket = options;
				}
			},

			cursor: function (options) {
				//-- pointer는 canvas와 무관련이므로, UI쪽 으로 호출    		
				// -9999는 화면에서 삭제   		
				if (options.locationx < 0 && options.locationy < 0) {
					_clear(drawContext);
					return;
				}

				var posX = _getOrgX(options.locationx);
				var posY = _getOrgY(options.locationy);

				var point = [posX, posY];
				_drawPointer(drawContext, point, null);
			},

			laserpointer: function (options) {
				// 레이저 포인터를 설정하지 않아도 나오기 때문에 갖고 있어야 한다.
				// {"blue":0,"cmd":"laserpointer","green":181,"red":101,"type":2}
				var r = options.red ? options.red : 0;
				var g = options.green ? options.green : 0;
				var b = options.blue ? options.blue : 0;

				stamp_kind = options.type;

				var color = this.rgbToHex(r, g, b);
				pntColor[0] = color;
			},

			pensetting: function (options) {
				/** 2013.11.26 pensize 배율에 맞춰서 적용 */
				// 3: 형광 4 : 펜 5 : 지우개 6 : 레이저
				var menuSelect = options.menuselect ? options.menuselect : 0;
				stamp_kind = options.stamp_kind ? options.stamp_kind : stamp_kind;
				line_cap = options.line_cap ? options.line_cap : line_cap;

				var r = 0;
				var g = 0;
				var b = 0;

				// 2015.03.03 penSize & alpha 추가
				if (menuSelect == 4 || menuSelect == 0 || menuSelect == 5 || menuSelect == 7 || menuSelect == 8 || menuSelect == 9) {
					size = options.line_width ? _getOrgPenSize(options.line_width) : _getOrgPenSize(size);
					alpha = options.alpha_width ? (parseInt(options.alpha_width, 10) * 0.01) : alpha;

					r = options.r_color ? parseInt(options.r_color, 10) : 0;
					g = options.g_color ? parseInt(options.g_color, 10) : 0;
					b = options.b_color ? parseInt(options.b_color, 10) : 0;

					if (menuSelect == 8 || menuSelect == 9) {
						var fillR = options.fill_r_color === "" ? -1 : parseInt(options.fill_r_color, 10);
						var fillG = options.fill_g_color === "" ? -1 : parseInt(options.fill_g_color, 10);
						var fillB = options.fill_b_color === "" ? -1 : parseInt(options.fill_b_color, 10);
						// penColor[1] = this.rgbToHex(fillR, fillG, fillB);
						penColor[1] = (fillR == -1 && fillG == -1 && fillB == -1) ? "" : this.rgbToHex(fillR, fillG, fillB);
					}
				}

				// options.menuselect => 3 / 4 -> 3일경우 투명하게 
				/**
				var lazerPointer = document.getElementById("lazer");
				lazerPointer.style.display = "none";
				**/

				// 펜 색상 변경
				if (r > -1 && g > -1 && b > -1) {
					penColor[0] = this.rgbToHex(r, g, b);
				}

				// 4번일때는 지우게로 모드를 설정해준다.
				setMode((menuSelect == 5) ? "eraser" : (menuSelect == 6) ? "pointer" : (menuSelect == 7) ? "line" : (menuSelect == 8) ? "square" : (menuSelect == 9) ? "circle" : "freehand");
			},

			undo: function () {
				// Async.undo -> undo 호출
				undo();
			},

			redo: function () {
				redo();
			},

			erasermode: function (options) {
				// eraserMode = 2 : 현재 PDF 페이지 지움
				// eraserMode = 1 : 모든 PDF 지움 
				// currentPage
				// shape와 drag clear 
				_clearPage();
			},

			zoom: function (options) {
				var newScale = options.scale ? options.scale : 1;
				var scale = options.scale ? options.scale : 1;
				var x = options.x ? options.x : 0;
				var y = options.y ? options.y : 0;

				// 레이져 포인트 크기 싱크용
				zoomScale = newScale;

				// zoomX, zoomY는 매번 저장하고 있다가, zoomhandle이 일어날경우 start시 데이터만 사용한다.
				zoomX = x;
				zoomY = y;

				var translateX = _getTranslateX(drawCanvas, x, scale, newScale);
				var translateY = _getTranslateY(drawCanvas, y, scale, newScale);
				var settled = options.settled ? options.settled : "0";

				if (settled == "1") {
					this.settled(newScale, translateX, translateY);
				} else {
					// settled0이면 html5 zoom으로 확대한다.    			
					this.setExpand(newScale, translateX, translateY);
				}
			},

			// pdf 렌더링하고, 백그라운드 canvas만 확대
			settled: function (newScale, translateX, translateY) {
				// 2016.01.05 사파리에서 무한급수로 translate값이 들어오면 scale이 안먹는 버그 발생. 수정 (소수점 네자리 반올림)
				translateX = Math.floor(translateX * 10000) / 10000;
				translateY = Math.floor(translateY * 10000) / 10000;

				$(drawCanvas).css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(shapeCanvas).css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(textCanvas).css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(imgCanvas).css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(pdfCanvas).css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')

			},

			// setExpand는 PDF를 1배율로 만든뒤 HTML5의 scale과 translate로 확대하는 기능이다.
			setExpand: function (newScale, translateX, translateY) {

				translateX = Math.floor(translateX * 10000) / 10000;
				translateY = Math.floor(translateY * 10000) / 10000;

				$(drawCanvas).css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(shapeCanvas).css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(textCanvas).css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(imgCanvas).css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

				$(pdfCanvas).css('-moz-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-webkit-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('-o-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)')
					.css('ms-transform', 'scale(' + newScale + ', ' + newScale + ') translate(' + translateX + 'px, ' + translateY + 'px)');

			},

			rgbToHex: function (R, G, B) {
				this.toHex(R) + this.toHex(G) + this.toHex(B)
				return this.toHex(R) + this.toHex(G) + this.toHex(B);
			},
			toHex: function (n) {
				n = parseInt(n, 10);

				if (isNaN(n)) return "00";
				n = Math.max(0, Math.min(n, 255));
				return "0123456789ABCDEF".charAt((n - n % 16) / 16) + "0123456789ABCDEF".charAt(n % 16);
			},

			textbox: function (options) {
				var fColor = "#" + this.rgbToHex(options.r, options.g, options.b);
				var leftX = _getOrgX(options.x);
				var topY = _getOrgY(options.y);

				// 0- 신규, 1-업데이트, 2-삭제 
				if (options.type != "2") {
					// 현재 저장된 shape가 0개인경우, deletedShapes를 초기화 시킨다.( redo 기능 때문 )
					// 빈화면에 처음 그리는 경우 삭제되었던 shapes들 초기화 시킴
					if (options.type == "1") {
						// 기존패킷이 존재하면 지운다.
						removeText(options.id);
					}

					var drawMode = "textbox";
					draw[drawMode]({
						mode: drawMode,
						context: textContext,
						id: options.id,
						text: options.text,
						x: leftX,
						y: topY,
						w: options.w,
						h: options.h,
						face: options.face,
						italic: options.italic,
						bold: options.bold,
						size: options.size,
						globalAlpha: 1,
						fillStyle: fColor
					});

					if (shapes == null || shapes.length < 1) deletedShapes = [];
					shapes.push({
						mode: drawMode,
						context: textContext,
						id: options.id,
						text: options.text,
						x: leftX,
						y: topY,
						w: options.w,
						h: options.h,
						face: options.face,
						italic: options.italic,
						bold: options.bold,
						size: options.size,
						globalAlpha: 1,
						fillStyle: fColor
					});

				} else {
					removeText(options.id);
				}
			},

			pdf: function (options) {
				// --options
				var pageCanvas = document.getElementById("pdf" + options.pageno);
				var posX = options.posx;
				var posY = options.posy;
				var width = options.scalew;
				var height = options.scaleh;

				// shape없이 draw한			
				drawPdf(pageCanvas, posX, posY, width, height)
			},

			// top canvas
			front: function (modeParam, end, isHistory) {
				var drawMode = (typeof (modeParam) != "undefined" && modeParam != null) ? modeParam : mode;

				drawContext.strokeStyle = _getStrokeStyle();
				drawContext.lineWidth = _getLineWidth();
				drawContext.globalAlpha = _getStrokeAlpha();

				// 1. front canvas에 draw 
				// drawContext.clearRect( 0, 0, width, height );
				draw[drawMode]({
					isHistory: isHistory,
					context: drawContext,
					end: end,
					x: drag[drag.length - 1][0],
					y: drag[drag.length - 1][1],
					lineWidth: _getLineWidth(),
					strokeStyle: _getStrokeStyle(),
					fillStyle: _getFillStyle()
				});
			},

			frontEraser: function (modeParam) {
				var drawMode = (typeof (modeParam) != "undefined" && modeParam != null) ? modeParam : mode;

				drawContext.strokeStyle = _getStrokeStyle();
				drawContext.lineWidth = _getLineWidth();
				drawContext.globalAlpha = _getStrokeAlpha();

				// 1. front canvas에 draw 
				drawContext.clearRect(0, 0, width, height);
				draw[drawMode]({
					context: shapeContext,
					context2: textContext,
					x: drag[drag.length - 1][0],
					y: drag[drag.length - 1][1],
					lineWidth: _getLineWidth(),
					strokeStyle: _getStrokeStyle(),
					fillStyle: _getFillStyle()
				});

				// mode가 지우개 모드인경우 바로 shapeCanvas에도 바로 적용해야 한다.            
				shapeContext.strokeStyle = _getStrokeStyle();
				shapeContext.lineWidth = _getLineWidth();
				shapeContext.globalAlpha = _getStrokeAlpha();

				// draw[drawMode]({context: shapeContext});
			},

			// bottom canvas - ended 이벤트 떨어졌을경우 백단에 옮김
			shape: function (modeParam, end) {
				var drawMode = (typeof (modeParam) != "undefined" && modeParam != null) ? modeParam : mode;

				drawContext.clearRect(0, 0, width, height);

				shapeContext.strokeStyle = _getStrokeStyle();
				shapeContext.lineWidth = _getLineWidth();
				shapeContext.globalAlpha = _getStrokeAlpha();

				draw[drawMode]({
					context: shapeContext,
					context2: textContext,
					end: end,
					x: drag[drag.length - 1][0],
					y: drag[drag.length - 1][1],
					lineWidth: _getLineWidth(),
					strokeStyle: _getStrokeStyle(),
					globalAlpha: _getStrokeAlpha(),
					fillStyle: _getFillStyle()
				});

				if (drawMode != 'textbox') {
					// 현재 저장된 shape가 0개인경우, deletedShapes를 초기화 시킨다.( redo 기능 때문 )
					// 빈화면에 처음 그리는 경우 삭제되었던 shapes들 초기화 시킴
					if (shapes == null || shapes.length < 1) deletedShapes = [];

					shapes.push({
						mode: drawMode,
						x: drag[drag.length - 1][0],
						y: drag[drag.length - 1][1],
						lineWidth: _getLineWidth(),
						strokeStyle: _getStrokeStyle(),
						globalAlpha: _getStrokeAlpha(),
						fillStyle: _getFillStyle(),
						drag: drag
					});
				} else {

				}
			},

			shapeEraser: function (modeParam) {
				var drawMode = (typeof (modeParam) != "undefined" && modeParam != null) ? modeParam : mode;
				drawContext.clearRect(0, 0, width, height);

				// 현재 저장된 shape가 0개인경우, deletedShapes를 초기화 시킨다.( redo 기능 때문 )
				// 빈화면에 처음 그리는 경우 삭제되었던 shapes들 초기화 시킴
				if (shapes == null || shapes.length < 1) deletedShapes = [];

				shapes.push({
					mode: drawMode,
					context: shapeContext,
					context2: textContext,
					x: drag[drag.length - 1][0],
					y: drag[drag.length - 1][1],
					lineWidth: _getLineWidth(),
					strokeStyle: _getStrokeStyle(),
					globalAlpha: _getStrokeAlpha(),
					fillStyle: _getFillStyle(),
					drag: drag
				});
			}
		};

		function _calcScale(width, height) {
			if (width > height) {
				scaleX = 1024 / width;
				scaleY = 748 / height;
				scalePen = 1024 / width;
			} else {
				scaleX = 768 / width;
				scaleY = 1004 / height;
				scalePen = 768 / height;
			}
		}

		function _calcOrgScale(width, height) {
			if (width > height) {
				orgScaleX = width / 1024;
				orgScaleY = height / 748;
				orgScalePen = width / 1024;
			} else {
				orgScaleX = width / 768;
				orgScaleY = height / 1004;
				orgScalePen = height / 768;
			}
		}

		function _getFixedX(dx) {
			// console.log("_getFixedX(dx) dx : " + dx + ", scaleX : " + scaleX + ", return : " + (dx * scaleX));    	

			return dx * scaleX;
		}

		function _getFixedY(dy) {
			// console.log("_getFixedY(dy) dy : " + dy + ", scaleY : " + scaleY + ", return : " + (dy * scaleY));

			return dy * scaleY;
		}

		function _getOrgX(dx) {
			// console.log("_getOrgX(dx) dx : " + dx + ", orgScaleX : " + orgScaleX + ", return : " + (dx * orgScaleX));
			return dx * orgScaleX;
		}

		function _getOrgY(dy) {
			// console.log("_getOrgY(dy) dy : " + dy + ", orgScaleY : " + orgScaleY + ", return : " + (dy * orgScaleY));
			return dy * orgScaleY;
		}

		function _getFixedPenSize(d) {
			return d * scalePen;
		}

		function _getOrgPenSize(d) {
			return d * orgScalePen;
		}


		/***
		 *  zoomPoint = 좌표 + 확대된 영역값 - 줌이동시 translate된 좌표
		 *  	translate값을 빼는 이유는 실제 우측으로 이동시 translate는 반대방향으로 -값으로 이동되기 때문이다. 
		 */
		function _getZoomPoint(dx, dy) {
			var termX = zoomScale > 1 ? (((width * zoomScale) - width) / 2) / zoomScale : 0;
			var termY = zoomScale > 1 ? (((height * zoomScale) - height) / 2) / zoomScale : 0;

			var translateX = 0;
			var translateY = 0;

			if (zoomScale > 1 && (zoomX != 0 || zoomY != 0)) {
				// matrix는 무한대수가 나올수 있어서 floating 해줘야 한다.
				//var transform = $.browser.webkit ? 'webkitTransform' : $.browser.mozilla ? 'mozTransform' : 'transform';
				var transform = Utils.browser("msie") ? "transform" : 'webkitTransform';
				var matrix = $(drawCanvas).css(transform).replace(/[^0-9\-.,]/g, '').split(',')

				translateX = parseFloat(matrix[12] || matrix[4]) / zoomScale;
				translateY = parseFloat(matrix[13] || matrix[5]) / zoomScale;
				// x = (dx / zoomScale) + termX - translateX;
				// y = (dy / zoomScale) + termY - translateY;
			}

			return [((dx / zoomScale) + termX - translateX), ((dy / zoomScale) + termY - translateY)];
		}

		// (0,0) 에 맞추는 좌표 구하는 공식은 w * (배율-1) / 배율 -> 0, 0 최소좌표)
		function _getTranslateX(canvas, x, scale, newScale) {
			var w = $(canvas).width();
			var h = $(canvas).height();

			var halfW = w / 2;
			var minX = halfW * (newScale - 1) / newScale;

			// translateX는 minX와 maxX를 넘어선 안된다.		
			var orgX = _getOrgX(x);
			// var scaleX = ((w * newScale / 2) - orgX) / newScale;
			/** 2016.07.07 zoom scaleX 배율 공식 변경*/

			var scaleX = ((w * newScale / 2) - orgX) / newScale;
			// console.log("orgX : "+ orgX +", scaleX : " + scaleX + ", minX : " + minX);

			//  지우면 안됨.. min, max 예외처리용
			/**
					scaleX = (scaleX > minX) ? minX : scaleX;
					scaleX = (scaleX < -(minX)) ? -(minX) : scaleX;
			**/

			return scaleX;
		}

		function _getTranslateY(canvas, y, scale, newScale) {
			var w = $(canvas).width();
			var h = $(canvas).height();

			var halfH = h / 2;
			var minY = halfH * (newScale - 1) / newScale;

			var orgY = _getOrgY(y);
			// var scaleY = ((h * scale / 2) - orgY) / scale;
			/** 2016.07.07 zoom scaleX 배율 공식 변경*/
			var scaleY = ((h * scale / 2) - orgY) / zoomScale;
			// console.log("orgY : "+ orgY +", scaleY : " + scaleY + ", minY : " + minY);
			// 지우면 안됨.. min, max 예외처리용
			/**
			 		scaleY = (scaleY > minY) ? minY : scaleY;
					scaleY = (scaleY < -(minY)) ? -(minY) : scaleY;
			**/
			return scaleY;
		}

		function async(options, isHistory) {
			if (typeof (isHistory) == "undefined" || isHistory == null) {
				isHistory = true;
			}

			var cmd = options.cmd != null ? options.cmd.toLowerCase() : options.from != null ? options.from.toLowerCase() : "";
			var menuSelect = options.menuselect != null ? options.menuselect : 4;

			var mode = cmd;
			if (menuSelect == 5 && (cmd == 'began' || cmd == 'moved' || cmd == 'ended' || cmd == 'eraserbegan' || cmd == 'erasermoved' || cmd == 'eraserended')) mode = "eraser";
			else if (cmd == 'began' || cmd == 'moved' || cmd == 'ended') mode = "draw";

			if (typeof (Async[mode]) == "undefined") {

			} else {
				// console.log("############## Async[mode]: " + mode);
				// packet이 end되지 않았을때 기존 shape를 강제로 저장시켜 준다.
				// if(!isDrawEnd()) drawShapeForce();
				if (!isHistory && cmd != 'moved' && cmd != 'ended' && cmd != 'erasermoved' && cmd != 'eraserended') {
					// eraserbegan
					if (!isDrawEnd()) {
						drawShapeForce();
					}
				}

				Async[mode](options, isHistory);
			}
		}

		function asyncNt(action, x, y) {
			AsyncNt[action](action, x, y);
		}

		// whiteboard drawing 초기화 --> sound stop 시 사용  
		function clean() {
			_clearPage();
		}

		function setMode(m) {
			// console.log("setMode : " + m);
			mode = m;

			if (mode == "view") {
				if (ExCall.auth() == true) {
					setEvent();
				} else {
					destroyEvent();
				}

			} else {
				if (ExCall.auth() == true) {
					setEvent();
				}

			}

		}

		function getMode() {
			return mode;
		}

		function getZoomScale() {
			return zoomScale;
		}

		function getZoom() {
			return [zoomX || 0, zoomY || 0];
		}

		function getCanvas(type) {
			return (type == "img") ? imgCanvas : (type == "draw") ? drawCanvas : (type == "shape") ? shapeCanvas : (type == "pdf") ? pdfCanvas : null;
		}

		function drawImage() {
			/**
			imgContext.globalCompositeOperation = 'source-over';
			imgContext.globalAlpha = 1;			
			imgContext.clearRect(0, 0, imgCanvas.width, imgCanvas.height);
			imgContext.drawImage(img, posX, posY, width, height);
			***/
		}

		function drawPdf(pageCanvas, posX, posY, width, height) {
			if (pageCanvas != null) {
				pdfContext.globalCompositeOperation = 'source-over';
				pdfContext.globalAlpha = 1;
				pdfContext.clearRect(0, 0, pdfCanvas.width, pdfCanvas.height);
				pdfContext.drawImage(pageCanvas, posX, posY, width, height);
			}
		}

		function clearPdf() {
			pdfContext.globalCompositeOperation = 'source-over';
			pdfContext.globalAlpha = 1;
			pdfContext.clearRect(0, 0, pdfCanvas.width, pdfCanvas.height);
		}

		function isDrawEnd() {	
// console.log("lastDrawingPacket : " + JSON.stringify(lastDrawingPacket));    	
			return lastDrawingPacket == null ? true : false;   
		}

		function drawShapeForce() {
// console.log("drawShapeForce call @@@@@@@@@@@@@@@@@@@@@ : " + JSON.stringify(lastDrawingPacket));
			if(lastDrawingPacket != null && lastDrawingPacket.cmd.indexOf("moved") < 0){
				lastDrawingPacket = null;
				return;
			}
			lastDrawingPacket.cmd = lastDrawingPacket.cmd.replace("moved", "ended");
// console.log("drawShapeForce call @@@@@@@@@@@@@@@@@@@@@ : " + JSON.stringify(lastDrawingPacket));		
			async(lastDrawingPacket, false);
		}

		function _lockScroll(e) {
			e.preventDefault();
		}

		function setEvent() {
			var __setControlEvent = function () {
				// mouse event initialize    			 
				var platform = checkPlatform();
				if(platform == "web"){
	    			$(drawCanvas).bind("movestart", function(e){    				
						// touch return
						if(e.targetTouches && e.targetTouches.length > 0) return;
						
	// console.log("move start isMouse : " + isMouse);
	
						isMouse = true;
	
						_setOffset(slideX, slideY);
						
						var point = getPoint(e, offsetXy, container, compareX);
						_drawBegin(point);
						
						_actionSound("1");	
						
					}).bind('move', function(e){					
						// touch return
						if(e.targetTouches && e.targetTouches.length > 0) return; 
	
	// console.log("move isMouse : " + isMouse);
						
						var point = getPoint(e, offsetXy, container, compareX);
						if(point == null) return;
						
						_drawMove(point);
						
					}).bind("moveend", function(e){					
						// touch return
						if(e.targetTouches && e.targetTouches.length > 0) return;
	
						isMouse = false;		
	
						var point = getPoint(e, offsetXy, container, compareX);
						if(point == null) return;
						
						_drawEnd(point);					
						_actionSound("0");
						
					});	
				}

				// console.log('setEvent');
				$(drawCanvas).bind("touchstart", _touchStart);
				$(drawCanvas).bind("touchmove", _touchMove);
				$(drawCanvas).bind("touchend", _touchEnd);
				$(drawCanvas).bind("touchcancel", _touchEnd);
			}

			var __setEmptyEvent = function () {
				drawCanvas.addEventListener("touchstart", _lockScroll, false);
				drawCanvas.addEventListener("touchmove", _lockScroll, false);
				drawCanvas.addEventListener("touchend", _lockScroll, false);
			}

			var platform = checkPlatform();
			if (platform == "web") {
				if (eventHandler == null || eventHandler == false) {
					eventHandler = true;
					__setControlEvent();
				}
			} else if (platform == "ios") {
				var isEnable = mode == "view" ? true : false;
				if (isEnable) {
					// 스크롤 가능
					if (eventHandler) destroyEvent();
				} else {
					if (eventHandler == null || !eventHandler) {
						eventHandler = true;
						__setControlEvent();
					}
				}
			} else if (platform == "android") {
				// 스크롤 안됨    		
				var isEnable = mode == "view" ? true : false;
				if (isEnable) {
					// 스크롤 가능
					if (eventHandler) {
						destroyEvent();
					}
				} else {
					if (eventHandler == null || !eventHandler) {
						eventHandler = true;
						__setEmptyEvent();
					}
				}
			}
		}

		function _touchStart(e) {
// console.log("_touchStart : " + isTouch  + " " + new Date().toString());
			// scroll lock
			e.preventDefault();

			// for web : touch시 hide tool menu
			ExCall.hideToolMenu();

			if (event.touches.length > 1) { 
				_touchCancel(e);
				return false;
			}

			isTouch = true;

			_setOffset(slideX, slideY);

			var point = getPoint(e, offsetXy, container, compareX);
			if (point == null) return;
			
// console.log("_touchStart call touchBegin");
			_drawBegin(point);

			// audio.play();
			// _actionSound("1"); 
		}

		function _touchMove(e) {
// console.log("_touchMove : " + isTouch  + " " +  new Date().toString()); 
			e.preventDefault();

			if(isTouch){
				if(event.touches.length > 1){
					_touchCancel(e);
					return false;
				}

				var point = getPoint(e, offsetXy, container, compareX);
				if (point == null) return;

				_drawMove(point);
			} 

		}

		function _touchEnd(e) {
// console.log("_touchEnd : " + isTouch + " " + new Date().toString());
 
			// if(e.originalEvent.touches && e.originalEvent.touches.length > 1) return;
			e.preventDefault();

			if(isTouch){				
				if (event.touches.length > 1) {
					isTouch = false;
					return false;
				}

				var point = getPoint(e, offsetXy, container, compareX);
				if (point == null) return;
				
				_drawEnd(point);
				
				isTouch = false;
			} 
			
		}

		function _touchCancel(e) {
// console.log("_touchCancel : " + isTouch + new Date().toString());

			if (!isTouch) return;

			var point = getPoint(e, offsetXy, container, compareX);
			if (point == null) return;

// console.log("call touchCancel _drawEnd !!");
			_drawEnd(point);

			isTouch = false;

		}

		function destroyEvent() {
			/*
				drawCanvas.removeEventListener("mousedown", null, false);
				drawCanvas.removeEventListener("mousemove", null, false);
				drawCanvas.removeEventListener("mouseup", null, false);
				drawCanvas.removeEventListener("touchstart", _touchStart, false);
				drawCanvas.removeEventListener("touchmove", _touchMove, false);
				drawCanvas.removeEventListener("touchend", _touchEnd, false);	
				drawCanvas.removeEventListener("touchcancel", _touchEnd, false);
			*/

			$(drawCanvas).unbind("movestart");
			$(drawCanvas).unbind("move");
			$(drawCanvas).unbind("moveend");
			$(drawCanvas).unbind("touchstart");
			$(drawCanvas).unbind("touchmove");
			$(drawCanvas).unbind("touchend");
			$(drawCanvas).unbind("touchcancel");

			if (checkPlatform() == "android") {
				drawCanvas.removeEventListener("touchstart", _lockScroll);
				drawCanvas.removeEventListener("touchmove", _lockScroll);
				drawCanvas.removeEventListener("touchend", _lockScroll);
			}

			eventHandler = false;
		}

		// whiteboard 완전히 제거시 사용
		function destroy() {
			$(drawCanvas).unbind("movestart");
			$(drawCanvas).unbind("move");
			$(drawCanvas).unbind("moveend");
			$(drawCanvas).unbind("touchstart");
			$(drawCanvas).unbind("touchmove");
			$(drawCanvas).unbind("touchend");
			$(drawCanvas).unbind("touchcancel");

			eventHandler = false;

			_clearPage();

			delete this;
		}

		return {
			init: init,
			setMode: setMode,
			// 2016.04.07 문현균이가 요청해서 만들어줌. mode=freehand, eraser, line, square, circle, textbox ..
			getMode: getMode,
			setFill: function (f) {
				fill = f;
			},
			setSize: function (s) {
				size = s;
			},
			resize: resize,
			clearZoom: clearZoom,
			// 2015.05.19
			save: save,
			saveCapture: saveCapture,
			// 2016.06.01 - text annotation
			updateText: updateText,
			removeText: removeText,
			// 2013.10.14
			async: async,
			// 2016.02.29
			asyncNt: asyncNt,
			// 2013.11.25
			clean: clean,
			// 2014.11.17
			getZoomScale: getZoomScale,
			// 2015.03.16
			getCanvas: getCanvas,
			getZoom: getZoom,
			// 2016.06.10
			drawImage: drawImage,
			// 2015.07.05
			drawPdf: drawPdf,
			clearPdf: clearPdf,
			isDrawEnd: isDrawEnd,
			drawShapeForce: drawShapeForce,
			destroy: destroy
		};
	};

	/** 공용 함수 구간 */
	function select(selector, p) {
		if (selector.substring(0, 1) == '#')
			return document.getElementById(selector.substring(1));
		var s = selector.split('.'),
			list = (p ? p : document).getElementsByTagName(s[0] ? s[0] : '*'),
			a = [];
		for (var i = 0; i < list.length; i++) {
			if (!s[1] || classified(list.item(i), s[1]))
				a.push(list.item(i));
		}
		return a;
	}

	function each(o, f, s) {
		for (var i in o)
			if (o.hasOwnProperty(i))
				if (f.call(s, o[i], i, o) === false)
					break;
		return o;
	}

	function make(o) {
		var el = document.createElement(o.tag);
		each(o, function (v, k) {
			if (k != 'tag' && k != 'style' && k != 'children') {
				k = k == 'cls' ? 'className' : k;
				el[k] = v;
			}
		});
		each(o.style, function (v, k) {
			el.style[k] = v;
		});
		each(o.children, function (v, k) {
			el.appendChild(make(v));
		});
		return el;
	}

	function makeCanvas(o) {
		o.tag = 'canvas';
		var canvas = make(o);
		if (!canvas.getContext)
			G_vmlCanvasManager.initElement(canvas);

		return canvas;
	}

	function listen(el, e, f, s) {
		var sf = null;
		if (s)
			sf = function () {
				return f.apply(s, arguments);
			};
		if (el.addEventListener)
			el.addEventListener(e, s ? sf : f, false);
		else
			el.attachEvent('on' + e, s ? sf : f);
		return el;
	}

	// 이벤트 삭제
	function ignore(el, e, f) {
		if (el.removeEventListener)
			el.removeEventListener(e, f, false);
		else
			el.detachEvent('on' + e, f);
		return el;
	}

	// 레이아웃의 offset 좌표 리턴
	function offset(el, relativeToEl) {
		var offset = [0, 0];

		for (var node = el;
			(relativeToEl ? node != relativeToEl && node : node); node = node.offsetParent) {
			var left = node.offsetLeft;
			var top = node.offsetTop;

			/** 2014.10.31 슬라이드 이동후 화면을 그려버리면 left에 잡혀버려서 문제된다. wrap은 skip */
			if (node.id == "wrap") left = 0;

			offset[0] += left;
			offset[1] += top;
		}

		return offset;
	}

	// text box blur
	function nl2br(text) {
		var ch;
		text = escape(text);
		if (text.indexOf('%0D%0A') > -1)
			ch = /%0D%0A/g;
		else if (text.indexOf('%0A') > -1)
			ch = /%0A/g;
		else if (text.indexOf('%0D') > -1)
			ch = /%0D/g;
		return unescape(text.replace(ch, '<br />'));
	}

	function getNtPoint(offsets, container, compareX, action, x, y) {

		try {
			// 점을 찍은경우 x포인트만 한개 더 추가해준다.
			if (action == "end") {
				var isDot = (parseInt(compareX) == parseInt(x)) ? true : false;
				if (isDot) x += 1;
			}

			if (x < 0 || y < 0) return null;

			// native의 scroll이벤트는 document body에서 크스롤 포인트를 체크할 수 있다.
			return [
				x - offsets[0] + (document.body.scrollLeft || document.body.scrollLeft),
				y - offsets[1] + (document.body.scrollTop || document.body.scrollTop)
			];

		} catch (e) {
			return null;
		}
	}

	function getPoint(ev, offsets, container, compareX) {
		// touch event도 동작시켜야 하므로 뒤에꺼는 touch start 좌표 이다.
		try {
			var x = -1,
				y = -1;
			if (ev.type.indexOf("touch") > -1) {
				if (ev.type == "touchstart" || ev.type == "touchmove") {
					x = ev.originalEvent.touches[0].pageX;
					y = ev.originalEvent.touches[0].pageY;

				} else if (ev.type == "touchend") {
					x = event.changedTouches[event.changedTouches.length - 1].pageX;
					y = event.changedTouches[event.changedTouches.length - 1].pageY;
				}
			} else {
				x = ev.pageX;
				y = ev.pageY;
			}

			return [
				x - offsets[0] + (container.scrollLeft || container.scrollLeft),
				y - offsets[1] + (container.scrollTop || container.scrollTop)
			];

		} catch (e) {
			console.log(e);
			return null;
		}
	}

	function checkPlatform() {
		if (typeof (cordova) == "undefined") return "web";
		else return cordova.platformId;
	}

	window.SketchBoard = SketchBoard;

})();