var UICordova = {
	Cursor: {
		images: [],
		init: function () {
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);

			for (var i = 1; i < 11; i++) {
				for (var j = 1; j < 4; j++) {
					var img = new Image();
					// img.src = Utils.addContext("/res/images/pointer_type0"+ j + "_0" + i + ".png");
					// img.src = Utils.addResPath("fb/images", "pointer_type0"+ j + "_0" + i + ".png");
					//img.src = "img/pointer_type0"+ j + "_0" + i + ".png";  // 웹앱 포팅용..
					var idxStr = i < 10 ? "0" + i : "" + i;
					img.src = svrHost + "fb/res/klounge/images/pointer_type0" + j + "_" + idxStr + ".png"; // 웹앱 포팅용.. (서버에서 포인터 이미지 가져옴..)
					this.images.push(img);
				};
			}
		}
	},


	/**
	 * UI.Page : 재정의
	 * 멀티페이지 대응..
	 */
	Page: {


		/**
		 * UI.Page.sync (재정의)
		 * 멀티페이지 대응..
		 */
		sync: function () {
			if (UI.Page.changing) {
				//CanvasApp.hideHistoryLoading();

				// 네이티브의 히스토리 로딩 Confirm 창 닫기..
				CanvasApp.hideHistoryLoading();

				if (PacketMgr.isMC) {
					PacketMgr.Master.syncStatus("page");
				}
				changing = false;
			}
		},


		/**
		 * UI.Page.set (재정의)
		 * 멀티페이지 대응..
		 */
		set: function (pageId) {
			console.log("[UI.Page.set] pageId : " + pageId);
			// 1. canvas create
			var board = UI.getBoard(pageId);
			console.log(board);
			if (board == null) {
				UI.setBoard(pageId);
			} else {
				board.init(); // 앱 버전에서 추가한 구문 - 2016.10.20
			}


			UI.current = pageId;
			PacketMgr.lastPageId = pageId;

			// 2. history destroy & load
			PacketMgr.loadHistory(PacketMgr.roomid, pageId, function (packetCnt) {

				// history draw
				if (Utils.isKLounge() && packetCnt > PacketMgr.skipCnt) {
					UI.Page.changing = true;
					CanvasApp.drawEnd(false);
					CanvasApp.confirmDrawPacket("page"); // 불러올 드로잉 패킷이 많을 경우 confirm 다이얼로그를 띄워 유저에게 로딩 진행 여부를 묻는다..

				} else {
					PacketMgr._drawHistoryPacket(pageId, CanvasApp.isAnimation, function () {
						CanvasApp.drawEnd(false);
						//$("#loadHistory").hide();

						if (PacketMgr.isMC) {
							PacketMgr.Master.syncStatus("page");
						}

						// 페이지 이동시 썸네일 뜨는 기능 안되게 수정 - thumbnail 동기화
						// Ctrl.Uploader.checkSaveTimer("background", "3000");
					});
				}

			});

			// get plugin packet
			// 1. background set
			// 2. img, pdf set
			// 3. memo, vshare set
			this.load(pageId, function (data) {

				/**
					data = {
						"result" : 0,
						"msg" : "success",
						"background" : {
							"red" : "",
							"green" : "",
							"blue" : ""
						},
						"files" : []
				*/

				var background = data.background;
				var files = data.files; // 이미지, PDF
				var memo = data.memo;
				var vShare = data.vshare;

				console.log("[UI.Page.load] callback() background : " + JSON.stringify(data.background));


				var r = "";
				var g = "";
				var b = "";
				var bgImg = "";
				var colorIdx = -1;

				if (background) {
					console.log("[UI.Page.load] callback() - 백그라운드 정보 반영");
					r = background.RED ? background.RED : "";
					g = background.GREEN ? background.GREEN : "";
					b = background.BLUE ? background.BLUE : "";
					bgImg = background.PLUGINDATA ? background.PLUGINDATA : "";

					// 1. background set
					var backgroundPacket = {
						"cmd": "background",
						"color_r": "" + r + "",
						"color_g": "" + g + "",
						"color_b": "" + b + "",
						"bgimg": "" + bgImg + "",
						"page": "" + pageId + ""
					};
					Ctrl.Background.receive(backgroundPacket);


					var len = Ctrl.Background.rgb.length;
					for (var i = 0; i < len; i++) {
						var rgbObj = Ctrl.Background.rgb[i];
						if (Ctrl.Background.red == rgbObj.r && Ctrl.Background.green == rgbObj.g && Ctrl.Background.blue == rgbObj.b) {
							colorIdx = i;
							break;
						}
					}
				}

				var bgParams = {
					coloridx: colorIdx,
					bgred: r,
					bggreen: g,
					bgblue: b,
					bgimg: bgImg
				};

				console.log("[UI.Page.load] callback() - params : " + JSON.stringify(bgParams));

				// Native에 룸 배경설정값 동기화..
				cordova.exec(function (result) {
					console.log("cordova.exec() updateRoomBg success..");
				}, function (result) {
					console.log("updateRoomBg error : " + JSON.stringify(result));
				}, "RoomPlugin", "updateRoomBg", [bgParams]);



				// 2. img & pdf set
				var len = files == null ? 0 : files.length;
				for (var i = 0; i < len; i++) {
					console.log("[UI.Page.load] callback() - 이미지/PDF 리스트 로딩.. length : " + len);
					var cmd = files[i].typeflag == "p" ? "pdf" : "img";
					var cmdAddFile = {
						cmd: cmd
					};
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

					if (files[i].typeflag == "p") {
						PDFViewer.draw(cmdAddFile);
					} else {
						Ctrl.BGImg.draw(cmdAddFile, false);
					}
				}

				// 3. memo packet
				if (memo != null) {
					var len = memo == null ? 0 : memo.length;
					if (len > 0) {
						console.log("[UI.Page.load] callback() - 메모 리스트 로딩.. length : " + len);
						for (var i = 0; i < len; i++) {
							var memoInfo = memo[i];
							var type = "0";
							var memono = memoInfo.COMMENTNO || '';
							var seqno = memoInfo.SEQNO || '';
							var userid = PacketMgr.USERID || '';
							var userno = PacketMgr.USERNO || '';
							var usernm = PacketMgr.USERNM || '';
							var datetime = memoInfo.CDATETIME || '';
							var title = memoInfo.TITLE || '';
							var content = memoInfo.CONTENT || '';
							var left = memoInfo.POSX || 0;
							var top = memoInfo.POSY || 0;
							var fold = memoInfo.PLUGINDATA || 1;
							var ord = memoInfo.ORD || 1;
							var r = memoInfo.RED;
							var g = memoInfo.GREEN;
							var b = memoInfo.BLUE;

							var packet = {
								"cmd": "memo",
								"type": type,
								"memono": "" + memono + "",
								"seqno": "" + seqno + "",
								"userid": "" + userid + "",
								"userno": "" + userno + "",
								"usernm": "" + usernm + "",
								"datetime": "" + datetime + "",
								"title": "" + title + "",
								"content": "" + content + "",
								"x": "" + left + "",
								"y": "" + top + "",
								"color_r": "" + r + "",
								"color_g": "" + g + "",
								"color_b": "" + b + "",
								"fold": "" + fold + "",
								"ord": "" + ord + ""
							};
							Ctrl.Memo.receive(packet);
						}
					}
				}

				// 4. video share packet
				if (vShare != null) {
					var len = vShare == null ? 0 : vShare.length;
					if (len > 0) {
						console.log("[UI.Page.load] callback() - vShare 리스트 로딩..");
						for (var i = 0; i < len; i++) {
							var vshareInfo = vShare[i];
							var type = "0";
							var vsno = vshareInfo.COMMENTNO || '';
							var seqno = vshareInfo.SEQNO || '';
							var userid = PacketMgr.USERID || '';
							var userno = PacketMgr.USERNO || '';
							var usernm = PacketMgr.USERNM || '';
							var datetime = vshareInfo.CDATETIME || '';
							var title = vshareInfo.TITLE || '';
							var link = vshareInfo.CONTENT || '';
							var left = vshareInfo.POSX || 0;
							var top = vshareInfo.POSY || 0;
							var status = vshareInfo.PLUGINDATA || '';
							var ord = vshareInfo.ORD || 1;
							var r = vshareInfo.RED;
							var g = vshareInfo.GREEN;
							var b = vshareInfo.BLUE;
							var time = 0;

							var vsharePacket = {
								"cmd": "vshare",
								"type": type,
								"vsno": "" + vsno + "",
								"seqno": "" + seqno + "",
								"datetime": "" + datetime + "",
								"title": "" + title + "",
								"content": "" + link + "",
								"x": "" + left + "",
								"y": "" + top + "",
								"status": "" + status + "",
								"ord": "" + ord + "",
								"time": "" + time + "",
								"userno": "" + userno + ""
							};
							Ctrl.VShare.receive(vsharePacket);
						}
					}
				}
				console.log(Ctrl.VShare.list);

				$(window).scrollTop(0);

				// PAGE AUTH
				// 문서가 반드시 존재하는 회의만 판서관련 패킷을 sync 한다.
				/**
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
			})
		},


		/**
		 * UI.Page.load (재정의)
		 * 해당 페이지 정보 읽어오기.. (메모, 이미지/PDF, 영상공유.. 드로잉을 제외한 나머지 요소들을 로드함)
		 */
		load: function (pageId, callback) {
			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("page.get.info");

			var data = {
				roomid: PacketMgr.roomid,
				pageid: pageId
			};

			Utils.request(url, "json", data, function (json) {
				console.log("[UI.Page.load] page/getInfo.json result : " + JSON.stringify(json));
				if (json.result == 0) {
					callback(json);
				}
			});
		},




		/**
		 * UI.Page.add (재정의)
		 * 페이지 추가 API 호출
		 */
		add: function () {
			if (!Ctrl._checkAuth(true)) return;

			Ctrl.Modal.confirm(_msg("confirm.page.add"), true, function () {
				UI.Page.addProc()
			});
		},


		addProc: function () {

			if (!PacketMgr.isMC) {
				Ctrl.Msg.show(_msg("noti.not.allow"));
				return;
			}

			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("page.add");

			var data = {
				roomid: PacketMgr.roomid
			};

			$.ajax({
				type: "POST",
				data: data,
				url: url,
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
				},
				success: function (json) {
					console.log("[UI.Page.add] page/add.json result : " + JSON.stringify(json));
					if (json.result == 0) {
						var pageId = json.pageid;
						UI.Page.addLayer(pageId);
						PacketMgr.Master.page(pageId, '0', ''); // type값이 0이면 페이지 추가..
						UI.Page.change(pageId);
					}
				},
				error: function (error) {
					console.log("[UI.Page.add] page/add.json error : " + JSON.stringify(error));
					Ctrl.Msg.show(_msg("msg.exception"), "LONG");
				}

			});
		},


		/**
		 * UI.Page.change (재정의)
		 * 페이지 이동 API 호출
		 */
		change: function (pageId) {
			console.log("[UI.Page.change] pageId : " + pageId);

			if (UI.current == pageId) return;

			if (!Ctrl._checkAuth(false)) return;

			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("page.change");

			var data = {
				roomid: PacketMgr.roomid,
				pageid: pageId
			};

			/*
			Utils.request(url, "json", data, function(json){
				// UI.Page.changeUI(pageId);
				PacketMgr.Master.page(pageId, '1', '');  // type값이 0이면 페이지 이동(변경)..
				UI.Page.changeUI(pageId);
			});
			*/

			$.ajax({
				type: "POST",
				data: data,
				url: url,
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
				},
				success: function (json) {
					console.log("[UI.Page.load] page/change.json result : " + JSON.stringify(json));
					if (json.result == 0) {
						// UI.Page.changeUI(pageId);
						PacketMgr.Master.page(pageId, '1', ''); // type값이 0이면 페이지 이동(변경)..
						UI.Page.changeUI(pageId);
					}
				},
				error: function (error) {
					console.log("[UI.Page.load] page/change.json error : " + JSON.stringify(error));
					Ctrl.Msg.show(_msg("msg.exception"), "LONG");
				}

			});
		},



		/**
		 * UI.Page.changeUI (재정의)
		 * 페이지 이동 UI 처리
		 */
		changeUI: function (pageId) {
			console.log("[UI.Page.changeUI] pageId : " + pageId);

			if (UI.current == pageId) return;

			CanvasApp.hideHistoryLoading();

			// 네이티브에서 룸 로딩 UI 활성화..
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. startRoomLoading");
			}, function (result) {
				console.log("startRoomLoading err  : " + JSON.stringify(result));
			}, "RoomPlugin", "startRoomLoading", [{
				type: "page"
			}]);

			var beforeBoard = UI.getBoard();
			this.beforeScale = beforeBoard.getZoomScale();
			this.beforeZoomInfo = beforeBoard.getZoom();

			UI.Page.destroy(UI.current);


			// 웹방식의 UI처리는 주석..
			// page de select
			//$("#" + UI.current).css("border", "none");
			$("#sketch" + UI.current).hide();

			// page select
			//$("#" + pageId).css("border", "1px solid red");
			$("#sketch" + pageId).show();

			// UI.Page.syncPageInfo(pageId);
			// TODO : 네이티브의 멀티 페이지 UI에 대해서 선택된 모습, 선택 해제된 모습의 UI처리를 호출하는 플러그인 필요..
			var params = {
				oldPageId: UI.current,
				newPageId: pageId
			};

			// TODO : 네이티브에서 페이지 UI 삭제 처리
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. changePage");
			}, function (result) {
				console.log("changePage error : " + JSON.stringify(result));
			}, "MultiPagePlugin", "changePage", [params]);


			UI.Page.set(pageId);
			UI.current = pageId;
			// text annotaion 갱신
			// Ctrl.Text._setTextEvent();
			// text annotation canvas vlur event
			Ctrl.Text._setTextEvent();

			this.beforeScale = 1;
			this.beforeZoomInfo = [0, 0];
		},



		/**
		 * UI.Page.remove (재정의)
		 * 페이지 삭제 API 호출
		 **/
		remove: function (pageId) {
			if (pageId == UI.current) {
				return;
			}

			if (!Ctrl._checkAuth(true)) return;

			Ctrl.Modal.confirm(_msg("confirm.page.remove"), true, function () {

				var svrFlag = _prop("svr.flag");
				var svrHost = _prop("svr.host." + svrFlag);
				var url = svrHost + _prop("page.remove");

				var data = {
					roomid: PacketMgr.roomid,
					pageid: pageId
				};

				$.ajax({
					type: "POST",
					data: data,
					url: url,
					beforeSend: function (xhr) {
						xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
					},
					success: function (json) {
						console.log("[UI.Page.load] page/remove.json result : " + JSON.stringify(json));
						UI.Page.removeLayer(pageId);
						PacketMgr.Master.page(pageId, '2', ''); // type값이 2이면 페이지 삭제..
					},
					error: function (error) {
						console.log("[UI.Page.load] page/remove.json error : " + JSON.stringify(error));
						Ctrl.Msg.show(_msg("msg.exception"), "LONG");
					}

				});

				/*
				Utils.request(url, "json", data, function(json){
					UI.Page.removeLayer(pageId);
					PacketMgr.Master.page(pageId, '2', '');  // type값이 2이면 페이지 삭제..
				});
				*/
			});

		},


		/**
		 * UI.Page.receive (재정의)
		 * 멀티 페이지 관련 패킷을 수신받았을 때 호출되는 함수. PacketMgr.Command.page(packet)에서 호출되는 함수.
		 **/
		receive: function (packet) {
			console.log("[UI.Page.receive] packet : " + JSON.stringify(packet));
			var type = packet.type; // 0=add, 1-change, 2-remove
			var pageId = packet.pageid;

			if (type == "0") {
				UI.Page.addLayer(pageId);
			} else if (type == "1") {
				UI.Page.changeUI(pageId);
			} else if (type == "2") {
				UI.Page.removeLayer(pageId);
			} else if (type == "3") {
				UI.Page.orderUI(packet.order);
			}
		},


		/**
		 * UI.Page.addLayer (재정의)
		 * 페이지 추가 UI 처리
		 **/
		addLayer: function (pageId) {

			/*
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
			*/

			// set skBoards
			var skboardDiv = "<div id=\"sketch" + pageId + "\" class=\"sketch\" style=\"width: 100%; height: 100%; overflow: hidden; position: relative;\"></div>";
			$(skboardDiv).insertBefore("#file1");

			var params = {
				pageid: pageId
			};

			// TODO : 네이티브에서 페이지 UI 추가 처리
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removePage");
			}, function (result) {
				console.log("removePage error : " + JSON.stringify(result));
			}, "MultiPagePlugin", "addPage", [params]);

			// 미리보기 상단
			//this.syncPageInfo(pageId);
		},


		/**
		 * UI.Page.removeLayer (재정의)
		 * 페이지 삭제 UI 처리
		 **/
		removeLayer: function (pageId) {
			/*
			$(".pageWrap", "#pageContainer").each(function(){
				var id = $(this).attr("id");
				if(pageId == id){
					$(this).unbind("click");
					$(this).remove();
				}
			})
			*/

			var params = {
				pageid: pageId
			};

			// TODO : 네이티브에서 페이지 UI 삭제 처리
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removePage");
			}, function (result) {
				console.log("removePage error : " + JSON.stringify(result));
			}, "MultiPagePlugin", "removePage", [params]);

			// this.syncPageInfo(pageId);
		},


		/**
		 * UI.Page.order (재정의)
		 *   페이지 순서 정렬 API 호출하는 함수
		 **/
		order: function (ordList, pageId) {
			/*
			var ordList = "";
			var ordNo = 1;
			$("div.multi_page", "#sortable_box").each(function(){
				var pageId = $(this).attr("id");
				var ord = pageId + "|" + ordNo++;

				if(ordList != "") ordList += ",";
				ordList += ord;
			});
			*/

			if (!Ctrl._checkAuth(false)) return;

			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("page.order");
			var data = {
				roomid: PacketMgr.roomid,
				ordlist: ordList
			};

			var onResult = function (flag) {
				var params = {
					"result": flag,
					"pageid": pageId // IOS에서 change UI처리 여부 판단을 위해 사용하는 값
				};

				cordova.exec(function (result) {
					console.log("cordova.exec() success.. orderResult");
					if (UI.current != pageId) {
						UI.Page.change(pageId);
					}
				}, function (result) {
					console.log("orderResult error : " + JSON.stringify(result));
				}, "MultiPagePlugin", "orderResult", [params]);
			};

			$.ajax({
				type: "POST",
				data: data,
				url: url,
				beforeSend: function (xhr) {
					xhr.setRequestHeader('Authorization', CanvasApp.userCredential);
				},
				success: function (json) {
					if (json.result == 0) {
						console.log("[UI.Page.load] page/order.json result : " + JSON.stringify(json));
						PacketMgr.Master.page(UI.current, '3', ordList);
						//UI.Page.syncPageInfo(UI.current);
						onResult(true);
					} else {
						onResult(false);
					}

				},
				error: function (error) {
					console.log("[UI.Page.load] page/order.json error : " + JSON.stringify(error));
					Ctrl.Msg.show(_msg("msg.exception"), "LONG");
					onResult(false);
				}
			});

			/*
			Utils.request(url, "json", data, function(json){
				PacketMgr.Master.page(UI.current, '3', ordList);

				//UI.Page.syncPageInfo(UI.current);
			});
			*/
		},


		/**
		 * UI.Page.orderUI (재정의)
		 *
		 **/
		orderUI: function (ordList) {
			// orderList => pageId|orderno

			/*
			var orders = ordList.split(",");
			var len = orders == null ? 0 : orders.length;

			var orderMap = new Map();
			for(var i=0; i<len; i++){
				var pageId = orders[i].split("|")[0];
				var orderNo = orders[i].split("|")[1];
				orderMap.put(orderNo, pageId);
			}

			$("#ghost").append( $(".multi_page", "#sortable_box") );

			for(var i=0; i<len; i++){
				var pageId = orderMap.get((i+1));
				// $("#" + pageId).insertBefore( $("#addPageBtn") );
				$("#sortable_box").append($("#" + pageId));
			}
			*/

			var orders = ordList.split(",");
			var len = orders == null ? 0 : orders.length;

			var orderList = [];
			for (var i = 0; i < len; i++) {
				var pageId = orders[i].split("|")[0];
				orderList.push(pageId);
			}


			// TODO : 네이티브에서 페이지 UI 삭제 처리
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. orderPage");
			}, function (result) {
				console.log("orderPage error : " + JSON.stringify(result));
			}, "MultiPagePlugin", "orderPage", orderList);

			// page index 번호 다시 맞춤
			//this.syncPageInfo();
		},


		/**
		 * Ctrl.Page.destroy (재정의)
		 *
		 **/
		destroy: function (pageId) {
			console.log("[Ctrl.Page.destroy] pageId : " + pageId);
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
			$("#sketch" + pageId).css({
				left: 0
			});
			$("#sketch" + pageId).hide();
		},


		/**
		 * Ctrl.Page.renderPageList (신규 정의)
		 *   최초 한번만 호출됨. canvas/get.json에서 받아온 페이지 리스트를 받아서 html로 렌더링 하는 함수. 앱에서만 사용함..
		 **/
		renderPageList: function (pageList, callback) {
			var pageCnt = pageList.length;
			for (var i = 0; i < pageCnt; i++) {
				var pageMap = pageList[i];

				var pageId = pageMap.pageid;
				var isCurrent = CanvasApp.info.currentpageid == pageId ? true : false;
				var pageStyle = isCurrent ? "" : "display:none;";

				var htmlStr = "<div id=\"sketch" + pageId + "\" class=\"sketch\" style=\"" + pageStyle + " width: 100%; height: 100%; overflow: hidden; position: relative;\"></div>";

				$("#wrap").prepend(htmlStr);
			}

			if (typeof callback != 'undefined') {
				callback();
			}
		}
	},



	/**
	 * UI.setDivHolder : 캔버스의 너비, 높이값을 결정함 (재정의)
	 *  - 2016.10.11 : 멀티 페이지 업데이트에 대응하여 수정됨 (pageId 파라미터 추가)
	 */
	setDivHolder: function (pageId) {
		var termY = Utils.browser("msie") ? $("#" + UI.HEADER).height() : $("#" + UI.HEADER).height();
		var maxWidth = Utils.mobile() ? $(window).width() : window.screen.availWidth;
		var maxHeight = Utils.mobile() ? $(window).height() : (window.screen.availHeight - (window.outerHeight - window.innerHeight)) - termY;
		var canvasWidth = maxWidth > maxHeight ? maxWidth : maxHeight;
		var canvasHeight = maxWidth > maxHeight ? maxWidth : maxHeight;

		var physicalScreenWidth = window.screen.width * window.devicePixelRatio;
		var physicalScreenHeight = window.screen.height * window.devicePixelRatio;

		console.log("[UI.setDivHolder] maxWidth : " + maxWidth + ", maxHeight : " + maxHeight);
		console.log("[UI.setDivHolder] canvasWidth : " + canvasWidth + ", canvasHeight : " + canvasHeight);
		console.log("[UI.setDivHolder] screen.width : " + window.screen.width + ", screen.height : " + window.screen.height);
		console.log("[UI.setDivHolder] physicalScreenWidth : " + physicalScreenWidth + ", physicalScreenHeight : " + physicalScreenHeight);


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


	/**
	 * UI.init : 최초 1번만 호출되는 함수.. getRoomInfo에서 호출함 (재정의)
	 *  - 2016.10.11 : 멀티 페이지 업데이트에 대응하여 수정됨 (pageId 파라미터 추가, pageLimit 파라미터 추가)
	 */
	init: function (pageId, pageLimit) {
		console.log("{UI.init]");
		UI.current = pageId;
		UI.Page.limit = pageLimit;

		this.Cursor.init();
		this.setDivHolder(pageId);

		this.setBoard(UI.current); // 멀티 페이지에 대응하여 추가됨..
		this.setEvent();
	},


	/**
	 * UI.touchDetector : 신규 정의함
	 * - 안드로이드 전용, 터치시 엘리먼트 감지하는 함수
	 */
	touchDetector : function(action, x, y, density, webViewWidth, webViewHeight) {
		console.log(document.elementFromPoint(x, y));
		UI.currentTouchTarget = document.elementFromPoint(x, y);
		//console.log(UI.currentTouchTarget);
		while(true) {
			if (UI.currentTouchTarget != null) {
				console.log(UI.currentTouchTarget.getAttribute("id"));
				if (UI.currentTouchTarget.getAttribute("id") == "pdfWrapper") {
					UI.currentTouchTarget = null;
					UI.renderDrawing = false;
					break;	
				} else if (UI.currentTouchTarget.getAttribute("id") == "memoWrapper") {
					UI.currentTouchTarget = null;
					UI.renderDrawing = false;
					break;
				} else if (UI.currentTouchTarget.getAttribute("id") == "vShareWrapper") {
					UI.currentTouchTarget = null;
					UI.renderDrawing = false;
					break;
				} else if (UI.currentTouchTarget.getAttribute("id") == "contsWrapper") {
					UI.currentTouchTarget = null;
					UI.renderDrawing = true;
					CanvasApp.drawNt(action, x, y, density, webViewWidth, webViewHeight);
					break;
				} else {
					UI.currentTouchTarget = UI.currentTouchTarget.parentElement;
				}
			} else {
				break;
			}
		}
	},

	currentTouchTarget : null,
	renderDrawing : false,

	/**
	 * UI.setEvent : 재정의
	 */
	setEvent: function () {
		$(window).bind("beforeunload", function (e) {
			// mailto 태그에서 이곳이 걸릴수 있으므로 주의.. mailto는 frame 처리

			// 최초 접속시 scroll 최상단으로 - 2016.10.18 추가
			$(window).scrollTop(0);

			// IE는 A태그 이동시 before unload가 튀어버려서 예외처리 해줘야 한다.
			if (!Utils.browser("msie")) {
				VideoCtrl.destroyAll();

				RoomSvr.destroy();

				PacketMgr.destroy();

				UI.destroy();

				Ctrl.destroy();
			}
		});

		$(window).bind("unload", function () {

			console.log("window unload...");
			// ie는 버그있어서 destroy를 예외처리 해준다.
			if (!Utils.browser("msie")) {
				VideoCtrl.destroyAll();
			}

			// IE는 이곳에서 destroy 해야 한다.
			RoomSvr.destroy();

			PacketMgr.destroy()

			UI.destroy();

			Ctrl.destroy();

		});


		// window disableselection
		$(window).attr('unselectable', 'on').css({
			'-moz-user-select': '-moz-none',
			'-moz-user-select': 'none',
			'-o-user-select': 'none',
			'-khtml-user-select': 'none',
			/* you could also put this in a class */
			'-webkit-user-select': 'none',
			/* and add the CSS class here instead */
			'-ms-user-select': 'none',
			'user-select': 'none'
		}).bind('selectstart', function () {
			return false;
		});


		// 네트워크 처리
		$(window).bind("online", function (e) {
			console.log("online.................");
			console.log(e);
			Ctrl.setMyNetworkStatus(true, false);
		});

		$(window).bind("offline", function (e) {
			console.log("offline.................");
			console.log(e);
			Ctrl.setMyNetworkStatus(false, false);
		});
	},


	/**
	 *  UI.setCanvas (재정의)
	 *  2016.10.12 Deprecated
	 **/
	setCanvas: function (pageNo) {
		var id = "#sketch" + pageNo;
		var containerId = "#" + UI.CONTAINER;

		var isWeb = !Utils.cordova();
		var app = new SketchBoard();
		drawing = app.init(containerId, id, false, UI.Cursor.images);

		UI.skboards.push(drawing);
	},


	/**
	 *  UI.setBoard (재정의)
	 *  2016.10.12 - 멀티 페이지 업데이트에 대응하여 추가함.
	 **/
	setBoard: function (pageId) {
		console.log("[UI.setBoard] Create board.. pageId : " + pageId);

		// fileMap = new Map();
		var id = "#sketch" + pageId;
		var containerId = "#" + UI.CONTAINER;

		var isWeb = !Utils.cordova();
		var app = new SketchBoard();
		drawing = app.init(containerId, id, isWeb, UI.Cursor.images); // isWeb은 터치를 이용한 캔버스 스크롤 제어를 위해서 사용하는 flag 값임..
		// UI.skboards.push(drawing);
		if (this.boards == null) this.boards = new Map();

		this.boards.put(pageId, drawing);
	},
}

$.extend(true, UI, UICordova);



var PacketMgrCordova = {


	/**
	 * PacketMgr.loadHistory : 재정의함
	 *  - 2016.10.11 : 멀티페이지 업데이트에 대응하여 수정됨
	 *  - 2016.10.20 : idx 파라미터 추가 / 패킷은 천만개 단위로 페이징처리 되어 내려옴. 따라서 읽어들일 패킷이 천만개 이상이라면 다음 패킷 묶음을 내려받기 위해 canvas/packet.json에 idx 파라미터까지 넘겨야 내려받을 수 있음.
	 */
	loadHistory: function (roomId, pageId, callback, idx) {

		console.log("[PacketMgr.loadHistory] pageId : " + pageId);

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

		if (PacketMgr.pageMap != null) {
			console.log("[PacketMgr.loadHistory] Exist PacketMgr.pageMap : " + PacketMgr.pageMap.keys().toString());
			// 페이징시 이미 패킷이 있는 경우 데이터 가공
			var existedPacket = PacketMgr.pageMap.get(pageId);
			// console.log("existedPacket", JSON.stringify(existedPacket));
			if (existedPacket != null) {
				var len = existedPacket.get("list") == null ? 0 : existedPacket.get("list").length;
				var list = existedPacket.get("list");

				PacketMgr.penIdxList = null;
				PacketMgr.penIdxList = [];
				PacketMgr.penMap = null;
				PacketMgr.penMap = new Map();

				for (var i = 0; i < len; i++) {
					var packet = list[i];
					var cmd = packet.cmd;
					var packetIdx = packet.packetno;

					if (cmd == "pensetting") {
						PacketMgr.penIdxList.push(parseInt(packetIdx));
						if (PacketMgr.penMap == null) {
							PacketMgr.penMap = new Map();
						}
						PacketMgr.penMap.put(packetIdx, packet);
					}
				}
				if (PacketMgr.penIdxList != null && PacketMgr.penIdxList.length > 1) {
					PacketMgr.penIdxList.sort(function (a, b) {
						return a - b;
					});
				}

				if (callback) callback(len);
				return;
			}
		}

		var module = $("#rsaModule").val();
		var exponent = $("#rsaExponent").val();
		var uuid = Utils.createUUID().substring(0, 5);

		var rsa = new RSAKey();
		rsa.setPublic(module, exponent);
		var tokenStr = rsa.encrypt(roomId + ',' + pageId + ',' + uuid);

		if (tokenStr == null) {
			console.log("encrypt fail..");
			return;
		}

		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("canvas.packet");
		var data = {
			token: tokenStr
		};

		PacketMgr.isLoadPacket = true;

		// pen idx 초기화
		PacketMgr.penIdxList = null;
		PacketMgr.penIdxList = [];

		console.log("[PacketMgr.loadHistory] data : " + JSON.stringify(data));
		Utils.request(url, "json", data, function (json) {
			//console.log("[PacketMgr.loadHistory] canvas/packet.json result : " + JSON.stringify(json));
			console.log("[PacketMgr.loadHistory] pageId 파라미터 : " + pageId);
			var isFirstLoad = false;
			var pageMap = PacketMgr.pageMap;
			var skipCnt = json.skipcnt || 0;
			// 파일맵은 무조건 한번 init 해준다.
			if (typeof (pageMap) == "undefined" || pageMap == null) {
				isFirstLoad = true;
				pageMap = new Map();
			}

			if (json && json.result == 0) {
				var list = json.list;
				var len = list == null ? 0 : list.length;
				var moreFlag = json.more;

				PacketMgr.lastPensetting = json.lastpensetting != null ? json.lastpensetting : null;

				var packetMap = pageMap.get(pageId);
				if (typeof (packetMap) == "undefined" || packetMap == null) packetMap = new Map();

				var drawFlag = packetMap.get("isdraw");
				if (typeof (drawFlag) == "undefined" || drawFlag == null) packetMap.put("isdraw", false);

				var pagePacketList = packetMap.get("list");
				if (typeof (pagePacketList) == "undefined" || pagePacketList == null) pagePacketList = [];

				for (var i = 0; i < len; i++) {
					var item = list[i];
					//var pageId = item.pageid;
					var packetIdx = item.packetno;

					var packet = $.parseJSON(item.packet);
					var cmd = packet.cmd;
					if (cmd == "pensetting") {
						// penIdxList : [],
						// PacketMgr.penPacketList : null,	//	load로 전달받은 기본 패킷
						PacketMgr.penIdxList.push(parseInt(packetIdx));

						if (PacketMgr.penMap == null) {
							PacketMgr.penMap = new Map();
						}
						PacketMgr.penMap.put(packetIdx, packet);
					}

					// 아래 세가지 패킷은 처리할 필요 없다.
					//if(cmd == "filechange" || cmd == "changecurrentpage" || cmd == "pensetting" || cmd == "view") continue;


					packet.packetno = packetIdx;
					pagePacketList.push(packet);

				}

				packetMap.put("list", pagePacketList);
				//console.log(packetMap);

				console.log("[PacketMgr.loadHistory] PacketMgr.pageMap에 패킷맵을 추가합니다. pageId : " + pageId);
				pageMap.put(pageId, packetMap);
				PacketMgr.pageMap = pageMap;
				console.log("[PacketMgr.loadHistory] Current PacketMgr.pageMap : " + PacketMgr.pageMap.keys().toString());
				//console.log(PacketMgr.pageMap.get(pageId));

				if (!isFirstLoad && PacketMgr.penIdxList != null && PacketMgr.penIdxList.length > 1) {
					PacketMgr.penIdxList.sort(function (a, b) {
						return a - b;
					});
				}

				if (PacketMgr.skipCnt == 0) {
					PacketMgr.skipCnt = skipCnt;
				}

				// 패킷이 천만개가 넘어가면 more값이 1로 내려오는데 이 때 서버로부터 패킷을 더 읽어와야 한다.. - 2016.10.20
				if (moreFlag == 1) {
					var idx = pagePacketList[pagePacketList.length - 1].idx;
					PacketMgr.loadHistory(roomId, pageId, callback, idx);
				}


				PacketMgr.isLoadPacket = false;



				if (callback) callback(len);

			} else if (json && json.result == -8080) {

				Ctrl.Msg.show(_msg("msg.exception"), "LONG");
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. forceFinishActivity");
				}, function (result) {
					console.log("forceFinishActivity error : " + JSON.stringify(result));
				}, "RoomPlugin", "forceFinishActivity", []);
			}


		}, function (e) {
			// Utils.log("load history error : " + JSON.stringify(e));
			PacketMgr.isLoadPacket = false;
		}, function (e) {
			PacketMgr.isLoadPacket = false;
		});
	},

	loadHistroyMore: function () {


	},




	/**
	 * PacketMgr._drawPagePacket : 재정의
	 *  - 2016.10.11 멀티페이지 업데이트에 대응하여 수정됨
	 **/
	_drawPagePacket: function (pageId, isAnimation) {
		// 히스토리가 존재하는지 체크할것
		if (PacketMgr.pageMap != null) {

			//console.log("[PacketMgr._drawPagePacket] pageId : " + pageId);
			var packetMap = PacketMgr.pageMap.get(pageId);
			//console.log("[PacketMgr._drawPagePacket] packetMap : " + JSON.stringify(packetMap));

			if (packetMap != null) {
				var isDraw = packetMap.get("isdraw");
				var isRender = UI._isRender(pageId);
				// if(!isDraw && isRender){
				if (isRender) {
					var pagePacketList = packetMap.get("list");
					var len = pagePacketList == null ? 0 : pagePacketList.length;

					PacketMgr.maxPacketCnt = len;
					for (var i = 0; i < len; i++) {
						var packet = pagePacketList[i];
						var nextPacket = (i+1) >= len ? null : pagePacketList[i+1];
						
						if (isAnimation) {
							// 1. queue에 만 쌓아두는 방식은 좀 느리다. 평균 26 / 50 / 2분 30초
							// 1. 느리지만 정밀한 큐잉
							// PacketMgr.animationQueue.push(packet);
							// 2. 빠르지만 부분적으로 뭉텅이로 그려지는 빠른 큐잉
							PacketMgr.animationQueue.push(packet);
							setTimeout(function () {
								if (PacketMgr.animationQueue.length < 1) {
									Ctrl.ProgressLoader.hide();
									return;
								}

								var json = PacketMgr.animationQueue.shift();
								if (json != null) PacketMgr.receiveForSync(json, pageId, isAnimation, nextPacket);

								var percent = PacketMgr.currentCnt++ * 100 / PacketMgr.maxPacketCnt;
								Ctrl.ProgressLoader.update(Math.floor(parseInt(percent)));

								if (PacketMgr.animationQueue.length == 0) {
									// view모드로 강제 전환
									PacketMgr.Master.changeMode("view");
								}

							}, "1");
						} else {
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
			} else {
				console.log("[PacketMgr._drawPagePacket] 로딩할 packetMap이 없습니다..");
			}
		}

		if (isAnimation) {
			PacketMgr._shiftAnimationQueue(pageId);
		}
	},


	/**
	 * PacketMgr._drawHistroyPacket : 초기 입장시 이전에 저장된 패킷 드로잉 (재정의)
	 *  - 2016.10.11 멀티페이지 업데이트에 대응하여 수정함.
	 *
	 */
	_drawHistoryPacket: function (pageId, isAnimation, callback) {
		//console.log("[PacketMgr._drawHistoryPacket] pageId : " + pageId);
		if (PacketMgr.isLoadPacket) {
			// 로딩끝날떄까지 재귀호출
			Utils.runCallback(function () {
				PacketMgr._drawHistoryPacket(pageId, isAnimation, callback);
			}, 100);

		}

		if (PacketMgr.lastPensetting != null) {
			var packet = PacketMgr.lastPensetting.packet;
			PacketMgr.Master.toCanvas(packet);
		}

		if (PacketMgr.pageMap != null) {
			PacketMgr._drawPagePacket(pageId, isAnimation);
		} else {
			//Ctrl.Loader.hide();
			//Ctrl.ProgressLoader.hide();
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. finishRoomLoading");
			}, function (result) {
				console.log("finishRoomLoading error : " + JSON.stringify(result));
			}, "RoomPlugin", "finishRoomLoading", []);
		}

		if (callback) callback();

		// 함수가 call되는 순간 비동기 동작이 일어난다.
		// drawPacket();
	},


	/**
	 * PacketMgr._drawLastZoomPacket (재정의)
	 * Native에 마지막 zoom 값을 전달해주기 위한 코드 추가
	 *  - 2016.10.11 멀티페이지 업데이트에 대응하여 수정됨.
	 */
	_drawLastZoomPacket: function (pageId) {
		// 1. 마지막 페이지의 zoom 을 찾는다.
		// 2. drawing

		var lastPacket = null;

		if (PacketMgr.pageMap != null) {
			var pageMap = PacketMgr.pageMap;
			var packetMap = pageMap.get(pageId);
			if (packetMap != null) {
				var isRender = UI._isRender(pageId);
				var pagePacketList = packetMap.get("list");
				var len = pagePacketList == null ? 0 : pagePacketList.length;
				for (var i = len - 1; i >= 0; i--) {
					var json = pagePacketList[i];
					var cmd = json.cmd ? json.cmd : json.from ? json.from : "";
					if (cmd == "zoom") {
						lastPacket = json;

						var zoomVal = Math.floor(json.scale * 100);
						$("#zoomval").val(zoomVal);

						// 마지막 줌 값을 Native로 전달..
						cordova.exec(function (result) {
							console.log("cordova.exec() success.. setZoomVal");
						}, function (result) {
							console.log("setZoomVal error : " + JSON.stringify(result));
						}, "RoomPlugin", "setZoomVal", [{
							"zoom": zoomVal
						}]);

						break;
					}
				}
			}

		}

		if (lastPacket != null) {
			if (Utils.checkPlatform() == "ios") {
				cordova.exec(null, null, "RoomPlugin", "setScrollState", [lastPacket]);
			} else {
				var cmd = lastPacket.cmd ? lastPacket.cmd : lastPacket.from ? lastPacket.from : "";
				PacketMgr._runCommand(cmd, lastPacket, pageId);
			}
		}

	},


	Command: {

		/**
		 *  PacketMgr.Command.masterchange : 재정의
		 */
		masterchange: function (packet) {
			console.log("PacketMgr.Command.masterchange : " + JSON.stringify(packet));
			var userid = packet.userid;

			PacketMgr.masterid = userid;
			PacketMgr.isMC = (userid == PacketMgr.userid) ? true : false;

			var isShow = (!PacketMgr.isMC && !PacketMgr.isGuest && (PacketMgr.creatorid == PacketMgr.userid || PacketMgr.isAllowMaster)) ? true : false;

			Ctrl.Member.masterChange(userid);

			// 마스터와 내가 같으면 커서 모양을 맞춰주고 아니면 커서를 초기화 시킨다.
			//Ctrl.Cursor.sync(PacketMgr.isMC);

			// bgimage auth
			Ctrl.BGImg.auth();

			// 메모 관련 이벤트 reset - memo.js의 changeMC()를 호출함
			Ctrl.Memo.auth();

			// 영상 공유
			Ctrl.VShare.auth();

			//Ctrl._usePlugin(PacketMgr.isMC);  // slider enable - 앱에서는 사용하지 않음

			// 초기화
			Ctrl.Text.auth();

			// pdf viewr 초기화
			PDFViewer.auth();

			// noti go
			var userInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");

			// noti
			if (userInfo != null) {
				var userNm = userInfo.usernm;
				var messageTxt = userNm + _msg("noti.change.host");
				Ctrl.Msg.show(messageTxt);
			}

			if (!PacketMgr.isMC) {
				// 마스터가 아니면 뷰 모드로 강제 전환
				PacketMgr.Master.changeMode("view");

				// hand
				Ctrl.toggleRC(0, -1, false);

			}

			// 마스터가 바뀐경우 getdata로 마스터가 된사람의 정보를 얻어와야 한
			// 마스터 변경시 마스터 대상자는 현재 상태를 브로드캐스팅 해줘야 한다.
			PacketMgr.Command.getdata();

		},


		/**
		 *  PacketMgr.Command.masterwithdraw : 재정의
		 *  개설자가 권한을 다시 찾아올 때 호출..
		 */
		masterwithdraw: function (packet) {
			// 진행자가 권한을 되찾아올 때
			PacketMgr.masterid = PacketMgr.creatorid;
			PacketMgr.isMC = (PacketMgr.masterid == PacketMgr.userid) ? true : false;

			// Utils.log("PacketMgr.isMC : " + PacketMgr.isMC + ", masterid : " + PacketMgr.masterid + ", PacketMgr.creatorid : " + PacketMgr.creatorid);
			var isShow = (!PacketMgr.isMC && !PacketMgr.isGuest && (PacketMgr.isCreator || PacketMgr.isAllowMaster)) ? true : false;

			// Ctrl.toggleMasterChange(isShow, PacketMgr.isMC, PacketMgr.isInitFChange);

			Ctrl.Member.masterWithDraw();

			// 마스터와 내가 같으면 커서 모양을 맞춰주고 아니면 커서를 초기화 시킨다. - 웹앱에서는 쓰이지 않음..
			//Ctrl.Cursor.sync(PacketMgr.isMC);

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
			if (userNm != "") {
				var messageTxt = userNm + _msg("noti.change.host");
				Ctrl.Msg.show(messageTxt);
			}

			if (!PacketMgr.isMC) {
				// 마스터가 아니면 뷰 모드로 강제 전환
				PacketMgr.Master.changeMode("view");

				// hand
				Ctrl.toggleRC(0, -1, false);
			}

			// 마스터 변경시 마스터 대상자는 현재 상태를 브로드캐스팅 해줘야 한다.
			PacketMgr.Command.getdata();
		},


		/**
		 * PacketMgr.Command.masterdone : 재정의
		 * 룸 종료 처리
		 */
		masterdone: function () {
			// 회의방 종료 처리
			Ctrl.Msg.show(RoomSvr.roomtitle + _msg("noti.end.meeting"));

			setTimeout(function () {
				Ctrl.goMainPage();
			}, "3000");
		},

		/** 2017.03.13 */
		zoom: function (packet) {
			if (Utils.checkPlatform() == "ios") {
				cordova.exec(null, null, "RoomPlugin", "setScrollState", [packet]);
				// set web local zoom value
				var percent = Math.floor(packet.scale * 100);
				Ctrl.setZoomVal(percent);
			} else {
				// Utils.log("zoom enter.");
				var scale = packet.scale ? packet.scale : 1;
				UI.scale = scale;

				UI.zoomEnd(UI.current, packet);

				PacketMgr.setPageScale(UI.current);

				Ctrl.BGImg.auth();

				Ctrl.Text.auth();

				PDFViewer.auth();
				// if(scale > 1) Ctrl.Text.cancel(false);
			}
		},
		/** 2017.03.13 end */

		/**
		 * PacketMgr.Command.masterdone : 재정의
		 * 룸 종료 처리
		 */
		updateroominfo: function (packet) {
			PacketMgr.Master.curRoomInfo = packet;

			Ctrl.Room.updateRoomInfo(packet, true);
		},

		/**
		 * PacketMgr.Command.vshare (재정의)
		 * 유튜브 영상 공유 패킷을 받아서 UI 처리하는 부분
		 */
		vshare: function (packet) {
			// type-0: add, 1-update, 2-remove
			Ctrl.VShare.receive(packet);
		},


		/**
		 * PacketMgr.Command.vcam (재정의)
		 * 캠 제어 패킷을 수신 받아서 처리해주는 함수
		 */
		vcam: function (packet) {
			console.log("[PacketMgr.Command.vcam] packet : " + JSON.stringify(packet));
			if (Utils.cordova()) {
				return;
			}
			VideoCtrl.receive(packet);
		},

		/**
		 * PacketMgr.Command.poll (재정의)
		 * 폴 패킷 받아서 처리하는 부분
		 */
		poll: function (packet) {
			console.log(JSON.stringify(packet));
			PollCtrl.receive(packet);
		},

		kickuser: function (packet) {
			RoomSvr.kickuser(packet.roomid, packet.userno, packet.userid, packet.username, packet.type);
		},

		call: function (packet) {
			// 모든 유저 호출
			Ctrl.BroadCast[packet.cmd](packet);
		},


		/**
		 * PacketMgr.Command.sync (재정의)
		 *    - cmd가 sync인 패킷이 내려올 경우 이 함수가 호출된다..
		 */
		sync: function (packet) {
			console.log("[PacketMgr.Command.sync] packet : " + JSON.stringify(packet));
			// 화면 동기화 용으로 사용하며 fName과 함수명을 매핑해서 호출하는 방식으로 사용한다. fName을 넘겨서 코드 메시지의 함수명을 찾아서 호출한다.
			// var packet = {"cmd":"sync", "method":fName};
			var method = packet.method;
			if (method != null && method != "") {
				var fName = _code(method);
				console.log("[PacketMgr.Command.sync] fName : " + fName);

				if (Ctrl.Sync[fName] != null && typeof (Ctrl.Sync[fName]) == "function") {
					Ctrl.Sync[fName].call();
				}
			}
		},

		video_options: function (packet) {
			// plugin code 
			console.log("<video_options / ZICO> packet: " + JSON.stringify(packet));
			cordova.exec(null, null, "VideoPlugin", "video_options", [packet]);
		},

		video_screen: function (packet) {
			// plugin code 
			cordova.exec(null, null, "VideoPlugin", "video_screen", [packet]);
		},

		video_group: function (packet) {
			// plugin code 
			cordova.exec(null, null, "VideoPlugin", "video_group", [packet]);
		},

		video_noti: function (packet) {
			// plugin code 
			cordova.exec(null, null, "VideoPlugin", "video_noti", [packet]);
		}
	},

	Master: {


		/**
		 * PacketMgr.Master.syncStatus (재정의)
		 *
		 **/
		syncStatus: function (from) {
			// 현재 마스터의 펜세팅 적용
			// 문서가 반드시 존재하는 회의만 판서관련 패킷을 sync 한다.
			Ctrl.callCurPensetting(true);
			// 현재 마스터가 뷰 모드 보고있으면, 뷰 모드로 전환해준
			if (Ctrl.isHand()) PacketMgr.Master.changeMode("view");

			// 마지막 줌 패킷
			var percent = parseInt($("#zoomval").val(), 10);
			if (percent > 100) {
				if (from == "page") {
					this.zoomSync(percent, "1");
				} else {
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


		/**
		 * PacketMgr.Master.background : 재정의
		 * 배경 설정 관련 패킷을 보냄..
		 * 2016.10.14 : page 정보 추가됨
		 **/
		background: function (bgImg, r, g, b) {
			console.log("[PacketMgr.Master.background] bgImg : " + bgImg + ", r : " + r + ", g : " + g + ", b : " + b);
			// bgImg -> 1,2,3,4
			var packet = {
				"cmd": "background",
				"color_r": "" + r + "",
				"color_g": "" + g + "",
				"color_b": "" + b + "",
				"bgimg": bgImg,
				"page": UI.current
			};
			this.toRoomCreator(packet);

			Ctrl.Uploader.checkSaveTimer("background", "1000");
		},

		/*** 2017.03.13 kdh 추가 extend */
		zoomHandleCenter: function (zoomScale, settled, centerX, centerY, pageId) {
			var packet = {
				"cmd": "zoom",
				"scale": zoomScale,
				"settled": settled,
				"x": centerX,
				"y": centerY,
				"page": pageId
			};
			this.toRoom(packet);

			if (Utils.checkPlatform() == "ios") {
				var percent = Math.floor(zoomScale * 100);
				Ctrl.setZoomVal(percent);
			} else if (Utils.checkPlatform() == "android") {
				UI.zoomEnd(pageId, packet);
			}
		},

		zoomCurrent: function (percent, settled) {
			if (Utils.checkPlatform() == "ios") {
				cordova.exec(null, null, "RoomPlugin", "getScrollState", null);
			} else {
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
				if (board != null) {
					scale = board.getZoomScale() > 1 ? board.getZoomScale() : (parseInt(percent) * 0.01);
					var zoomInfo = board.getZoom();
					zoomX = zoomInfo != null && zoomInfo[0] > 0 ? zoomInfo[0] : (PDFViewer.getFixedX(w, h, dx) * scale);
					zoomY = zoomInfo != null && zoomInfo[1] > 0 ? zoomInfo[1] : (PDFViewer.getFixedY(w, h, dy) * scale);
				} else {
					zoomX = (PDFViewer.getFixedX(w, h, dx) * scale);
					zoomY = (PDFViewer.getFixedY(w, h, dy) * scale);
				}

				var packet = {
					"cmd": "zoom",
					"scale": scale,
					"settled": settled,
					"x": zoomX,
					"y": zoomY,
					"page": UI.current
				};
				// 룸서버에 호출
				this.toRoom(packet);
			}
		},
		/*** 2017.03.13 end ***/

		/**
		 * PacketMgr.Master.BroadCast : 재정의
		 * 전체 방에 전송하는 메시징처리
		 **/
		BroadCast: {
			sendMsg: function (mode, roomId, msg, drCode, history) {
				var packet = {
					"cmd": "broadcastmsg",
					"type": "bcm01",
					"msg": "" + msg + "",
					"drcode": "" + drCode + "",
					"history": "" + history + ""
				};
				PacketMgr.Master.toBroadCast(mode, roomId, packet);
			},


			/**
			 * PacketMgr.Master.BroadCast.chat : 재정의
			 *
			 **/
			chat: function (mode, roomId, msg, target, targetNm) {
				var uuid = Utils.createUUID();
				var uuidTxt = uuid.substring(0, 8);
				var dateTime = Utils.getDate(new Date());

				if (target == "") targetNm = "";

				var packet = {
					"chatid": "" + uuidTxt + "",
					"cmd": "chat",
					"from": "" + PacketMgr.userno + "",
					"text": "" + msg + "",
					"time": "" + dateTime + "",
					"to": "" + target + "",
					"type": "class"
				};

				// 진행자가 아니어도 채팅 보낼수 있게 한다.
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			},


			/**
			 * PacketMgr.Master.BroadCast.call : 재정의
			 * 모든 학생 부르기
			 **/
			call: function (mode, roomId, drCode) {
				// 학생 부르기 기능
				var packet = {
					"cmd": "call",
					"drcode": "" + drCode + "",
					"roomid": "" + roomId + "",
					"userno": "" + RoomSvr.userno + "",
					"usernm": "" + RoomSvr.usernm + ""
				};
				//console.log("[PacketMgr.Master.BroadCast.call]");
				//console.log(packet);
				PacketMgr.Master.toBroadCastCreator(mode, roomId, packet);
			},

			sync: function (mode, roomId, fName) {
				// 화면 동기화 용으로 사용하며 fName과 함수명을 매핑해서 호출하는 방식으로 사용한다. fName을 넘겨서 코드 메시지의 함수명을 찾아서 호출한다.
				var packet = {
					"cmd": "sync",
					"method": fName
				};
				PacketMgr.Master.toBroadCastForce(mode, roomId, packet);
			}
		},


		/**
		 * PacketMgr.Master.vShare : 재정의
		 * 유튜브 영상 공유 관련 패킷을 룸서버로 송신하는 함수
		 * type : 0 - 추가, 1 - 업데이트, 2 - 삭제
		 **/
		// 메모와 기본 표준을 맞춘다.
		vShare: function (type, vsno, seqno, title, link, left, top, status, ord, time, userno) {
			vsno = vsno || '';
			seqno = seqno || '';
			status = status || '0';
			userid = PacketMgr.userid || '';
			userno = PacketMgr.userno || '';
			usernm = PacketMgr.usernm || '';
			datetime = Utils.getDate(new Date()) || '';
			title = title || '';
			content = link || '';
			left = left || 0;
			top = top || 0;
			ord = ord || 1;
			userno = userno || '';

			// var packet = {"cmd":"vshare","type":type,"seqno":""+seqno+"","userid":""+userid+"","userno":""+userno+"","usernm":""+usernm+"","datetime":""+datetime+"","title":""+title+"","content":""+link+"","x":""+left+"","y":""+top+"","color_r":""+r+"","color_g":""+g+"","color_b":""+b+"","fold":""+fold+"","ord":""+ord+""};
			var packet = {
				"cmd": "vshare",
				"type": type,
				"vsno": "" + vsno + "",
				"seqno": "" + seqno + "",
				"datetime": "" + datetime + "",
				"title": "" + title + "",
				"content": "" + link + "",
				"x": "" + left + "",
				"y": "" + top + "",
				"status": "" + status + "",
				"ord": "" + ord + "",
				"time": "" + time + "",
				"userno": "" + userno + ""
			};
			this.toRoom(packet);
		},


		/**
		 * PacketMgr.Master.page (재정의)
		 * 멀티 페이지 관련 패킷을 송출하기..
		 **/
		page: function (pageId, type, order) {
			// type = 0(add), 1(change), 2(remove), 3(ordering)
			var packet = {
				"cmd": "page",
				"pageid": pageId,
				"type": type,
				"order": order
			};
			this.toRoom(packet);
		},


		/**
		 * PacketMgr.Master.updateRoomInfo (재정의)
		 * 룸 정보 업데이트
		 **/
		updateRoomInfo: function (authType, chatOpt, cmtOpt, expOpt, title, passwd, openFlag, content, vCamOpt, vShareOpt, isShowToast) {
			var curRoomInfo = PacketMgr.Master.curRoomInfo;

			// 채팅
			authType = (authType != null) ? authType : (curRoomInfo != null) ? curRoomInfo.authType : "1";
			chatOpt = (chatOpt != null) ? chatOpt : (curRoomInfo != null) ? curRoomInfo.chatopt : "1";
			cmtOpt = (cmtOpt != null) ? cmtOpt : (curRoomInfo != null) ? curRoomInfo.cmtopt : "1";
			expOpt = (expOpt != null) ? expOpt : (curRoomInfo != null) ? curRoomInfo.expopt : "1";
			title = (title != null) ? title : (curRoomInfo != null) ? curRoomInfo.title : "";
			passwd = (passwd) != null ? passwd : (curRoomInfo != null) ? curRoomInfo.passwd : "";

			openFlag = (openFlag) != null ? openFlag : (curRoomInfo != null) ? curRoomInfo.openFlag : "1";
			content = (content) != null ? content : (curRoomInfo != null) ? curRoomInfo.content : "";

			vCamOpt = (vCamOpt) != null ? vCamOpt : (curRoomInfo != null) ? curRoomInfo.vCamOpt : "0";
			vShareOpt = (vShareOpt) != null ? vShareOpt : (curRoomInfo != null) ? curRoomInfo.vShareOpt : "0";

			// 패스워드는 재작업 필요
			// creator가 보내는 패킷
			// var packet = {"attendeemic":""+attendeemic+"","chatopt":""+chatOpt+"","cmd":"updateroominfo","dcodeopt":""+dcodeOpt+"","passwd":""+passwd+"","secretfile":""+secretFile+"","title":""+title+"","voice":""+voice+""};
			var packet = {
				"cmd": "updateroominfo",
				"authtype": "" + authType + "",
				"chatopt": "" + chatOpt + "",
				"cmtopt": "" + cmtOpt + "",
				"expopt": "" + expOpt + "",
				"passwd": "" + passwd + "",
				"title": "" + title + "",
				"openflag": "" + openFlag + "",
				"content": "" + content + "",
				"vcamopt": "" + vCamOpt + "",
				"vshareopt": "" + vShareOpt + ""
			};
			PacketMgr.Master.curRoomInfo = packet;

			this.toRoomCreator(packet);

			if (typeof isShowToast != 'undefined')
				Ctrl.Room.updateRoomInfo(packet, isShowToast);
			else
				Ctrl.Room.updateRoomInfo(packet);
		},


		exit: function (flag) {
			console.log("[PacketMgr.Master.exit]");

			var leaveUserPacket = {
				"cmd": "leaveuser",
				"userid": "" + PacketMgr.userid + ""
			};

			console.log(JSON.stringify(leaveUserPacket));

			this.toRoomForce(leaveUserPacket);

			// 생성자고 flag값 존재시
			if (flag) {
				var packet = {
					"cmd": "masterdone"
				};
				this.toRoomCreator(packet);

				// 마스터도 회의방 나가게 처리 - 밖에서 방 파괴하는게 있어서 receive받고 방을 지운다.
				// PacketMgr.Command.masterdone(packet);
			}

			// 영상회의인경우 나가기
			// Utils.log("PacketMgr.isVideo : " + PacketMgr.isVideo);
		}
	}

}

$.extend(true, PacketMgr, PacketMgrCordova);


var CtrlCordova = {

	colorMap: [{
			r: 0,
			g: 100,
			b: 250
		}, // 1번 컬러
		{
			r: 100,
			g: 0,
			b: 250
		}, // 2번 컬러
		{
			r: 250,
			g: 50,
			b: 50
		}, // 3번 컬러
		{
			r: 250,
			g: 100,
			b: 0
		}, // 4번 컬러
		{
			r: 50,
			g: 50,
			b: 50
		}, // 5번 컬러
		{
			r: 0,
			g: 200,
			b: 250
		}, // 6번 컬러
		{
			r: 200,
			g: 0,
			b: 250
		}, // 7번 컬러
		{
			r: 250,
			g: 100,
			b: 100
		}, // 8번 컬러
		{
			r: 250,
			g: 200,
			b: 0
		}, // 9번 컬러
		{
			r: -1,
			g: -1,
			b: -1
		}, // 10번
		{
			r: -1,
			g: -1,
			b: -1
		} // 11번
	],

	pointColorMap: [{
			r: 0,
			g: 100,
			b: 250
		}, // 1번 컬러
		{
			r: 100,
			g: 0,
			b: 250
		}, // 2번 컬러
		{
			r: 250,
			g: 50,
			b: 50
		}, // 3번 컬러
		{
			r: 250,
			g: 100,
			b: 0
		}, // 4번 컬러
		{
			r: 0,
			g: 250,
			b: 100
		}, // 5번 컬러
		{
			r: 0,
			g: 200,
			b: 250
		}, // 6번 컬러
		{
			r: 200,
			g: 0,
			b: 250
		}, // 7번 컬러
		{
			r: 250,
			g: 100,
			b: 100
		}, // 8번 컬러
		{
			r: 250,
			g: 200,
			b: 0
		}, // 9번 컬러
		{
			r: 0,
			g: 250,
			b: 200
		} // 10번 컬러
	],

	isPollProcessing: false, // 폴 진행여부
	isHandMode: true, // 선택모드 여부 (앱 전용)


	/**
	 *  Ctrl.isHand (재정의)
	 *  선택모드의 활성화/비활성화 상태 값을 가져오는 함수..
	 *  웹 방식은 HTML UI로 선택모드 활성화 여부를 판단했지만 앱에서는 isHandMode 변수를 할당하여 그 값으로 선택모드 활성화 여부를 식별함.
	 */
	isHand: function () {
		return this.isHandMode;
	},


	/**
	 *  Ctrl._checkAuth (재정의)
	 *  범용적으로 진행 권한을 체크하는 함수
	 *  기진행자가 드로잉 중이라면 즉시 권한을 획득할 수 없도록 예외처리가 되어있음
	 *    - showMsg : 노티 메세지 show/hide 여부
	 */
	_checkAuth: function (showMsg, isAuto) {
		//console.log("[Ctrl._checkAuth] isPollProcessing : " + this.isPollProcessing);
		// 메시지 한곳에서 처리하기 위해 이곳에서 작업함
		if (!PacketMgr.isMC && showMsg) {

			if (this.isPollProcessing) {
				Ctrl.Msg.show("폴 진행중에는 권한을 가져올 수 없습니다.");
				return false;
			}

			if ((!PacketMgr.isCreator && !PacketMgr.isAllowMaster) || PacketMgr.isGuest) {
				Ctrl.Msg.show(_msg("noti.not.allow"));
				return;
			}

			isAuto = (typeof (isAuto) == "undefined" || isAuto == null) ? true : isAuto;

			if (!isAuto) {
				// 자동 권한 이전에 제한을 둬야하는 기능이라면 권한 모달만 띄운
				Ctrl.Msg.auth(true);
				return;
			}

			// Ctrl.Msg.auth(false);
			if (this._checkWaiting()) { // 누가 드로잉중이라 기달려야 하는 상황 이라면?
				var masterUserInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
				var authContent = masterUserInfo.usernm + _msg("noti.leading.host");
				Ctrl.Msg.show(authContent);
			} else {
				// 나로 masterChange
				var userId = PacketMgr.userid;
				PacketMgr.Master.masterChange(userId);
			}

		}
		return PacketMgr.isMC;
	},

	/**
	 * Ctrl.Background
	 *   - this.img : 1 ~ 4까지 인덱스 값 (int)
	 *   - this.red : RGB코드의 R 값 (int)
	 *   - this.green : RGB코드의 G 값 (int)
	 *   - this.blue : RGB코드의 B 값 (int)
	 */
	Background: {


		/**
		 * Ctrl.Background.init : 재정의
		 */
		init: function () {
			this._setInit();
		},


		/**
		 * Ctrl.Background.receive (재정의)
		 * 페이지 배경 설정에 대한 패킷을 수신받으면 이 함수가 호출됨..
		 */
		receive: function (packet) {
			console.log("Background.receive : " + JSON.stringify(packet));
			// var packet = {"cmd":"background","color_r":""+r+"","color_g":""+ g + "","color_b":"" + b + "","bgimg": imgType};
			if (packet.bgimg == "" && packet.color_r == "" && packet.color_g == "" && packet.color_b == "") {
				this._clear();
			} else {
				this._clear();

				if (packet.bgimg != "") this._setImg(parseInt(packet.bgimg) - 1);

				if (packet.color_r != "" && packet.color_g != "" && packet.color_b != "") this._setRgbColor(packet.color_r, packet.color_g, packet.color_b);
			}
		},


		/**
		 * Ctrl.Background._setEvent (재정의)
		 * 앱에서 사용하지 않는 함수이므로 빈 함수로 재정의 하였음.
		 */
		_setEvent: function () {

		},


		/**
		 * Ctrl.Background._setInit : 재정의
		 */
		_setInit: function () {
			var bg = CanvasApp.info.bg;
			if (bg != null) {
				this.img = bg.bgimg;
				this.red = bg.bgred;
				this.green = bg.bggreen;
				this.blue = bg.bgblue;

				// 캔버스 배경 설정 초기화 (룸 입장시 최초 1회 실행) - 웹앱에서 추가한 구문..
				if (bg.bgimg != "") {
					var svrFlag = _prop('svr.flag');
					var svrHost = _prop('svr.host.' + svrFlag);
					var imgUrl = svrHost + "fb/res/klounge/images/background_0" + (parseInt(bg.bgimg)) + ".png";
					$("#" + (UI.SKETCH + UI.current)).css("backgroundImage", "url(" + imgUrl + ")");
				}
				$("#" + (UI.SKETCH + UI.current)).css("backgroundColor", "rgb(" + bg.bgred + "," + bg.bggreen + "," + bg.bgblue + ")");
			}
		},


		/**
		 * Ctrl.Background.save : 재정의
		 *   - 2016.10.14 : pageid 파라미터 추가함.
		 */
		save: function () {

			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("canvas.bg.save");

			var params = {
				roomid: RoomSvr.roomid,
				pageid: UI.current,
				userno: PacketMgr.userno,
				bgimg: this.img,
				bgred: this.red,
				bggreen: this.green,
				bgblue: this.blue
			};

			Utils.request(url, "json", params, function (data) {
				if (data.result == "0") {
					PacketMgr.Master.background(Ctrl.Background.img, Ctrl.Background.red, Ctrl.Background.green, Ctrl.Background.blue);

					var colorIdx = -1;
					var len = Ctrl.Background.rgb.length;
					for (var i = 0; i < len; i++) {
						var rgbObj = Ctrl.Background.rgb[i];
						if (Ctrl.Background.red == rgbObj.r && Ctrl.Background.green == rgbObj.g && Ctrl.Background.blue == rgbObj.b) {
							colorIdx = i;
							break;
						}
					}

					var cordovaParams = {
						coloridx: colorIdx,
						bgred: Ctrl.Background.red,
						bggreen: Ctrl.Background.green,
						bgblue: Ctrl.Background.blue,
						bgimg: Ctrl.Background.img
					};

					// Native에 룸 배경설정값 동기화..
					cordova.exec(function (result) {
						console.log("cordova.exec() updateRoomBg success..");
					}, function (result) {
						console.log("updateRoomBg error : " + JSON.stringify(result));
					}, "RoomPlugin", "updateRoomBg", [cordovaParams]);
				}
			});
		},


		/**
		 *  Ctrl.Background._setImg : 재정의
		 *  룸 배경 이미지 설정 이벤트
		 */
		_setImg: function (idx) {
			console.log("Background._setImg : " + idx);

			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var imgUrl = svrHost + "fb/res/klounge/images/background_0" + (idx + 1) + ".png";
			$("#" + (UI.SKETCH + UI.current)).css("backgroundImage", "url(" + imgUrl + ")");

			this.img = (idx + 1);
		},


		/**
		 *  Ctrl.Background._setColor : 재정의
		 *  룸 배경 컬러 지정 이벤트
		 */
		_setColor: function (idx) {
			var colorIdx = idx - 1;
			var r = this.rgb[colorIdx].r;
			var g = this.rgb[colorIdx].g;
			var b = this.rgb[colorIdx].b;

			this._setRgbColor(r, g, b);
		},


		/**
		 *  Ctrl.Background._setRgbColor (재정의)
		 *  룸 배경 컬러 지정 이벤트
		 */
		_setRgbColor: function (r, g, b) {
			this.red = r;
			this.green = g;
			this.blue = b;

			$("#" + (UI.SKETCH + UI.current)).css("backgroundColor", "rgb(" + r + "," + g + "," + b + ")");
		},


		/**
		 *  Ctrl.Background._clear (재정의)
		 *  룸 배경 속성 초기화
		 */
		_clear: function () {
			$("#" + (UI.SKETCH + UI.current)).css("backgroundImage", "");
			$("#" + (UI.SKETCH + UI.current)).css("backgroundColor", "");

			this.red = "";
			this.green = "";
			this.blue = "";
			this.img = "";
		},

		/**
		 * Ctrl.Background.destroy (재정의)
		 * 정의되지 않은 이벤트에 대해 destroy하는 코드들을 삭제하기 위해 재정의 함.
		 */
		destroy: function () {

		}


	},

	/**
	 * Ctrl.BGImg : 캔버스에 삽입된 이미지 (재정의)
	 *
	 **/
	BGImg: {

		/**
		 * Ctrl.BGImg.setDrag (재정의)
		 **/
		setDrag: function (img, canvas, seqNo, scaleW, scaleH, posX, posY, typeFlag, userNm, thumbnail, degree, fileData) {
			var defaultThumb = Utils.addResPath("fb/images", "thum_user.png");
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

			if (Ctrl.BGImg.data != null) {
				var data = Ctrl.BGImg.data.get(seqNo);
				if (data != null) {
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

			div.className = "img_box_edit " + attachClass;
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
			var userInfo = (typeFlag == "1") ? "<a class=\"answered_user\"><img class=\"user_photo\" src=\"" + thumbnail + "\" onerror=\"this.src='" + defaultThumb + "'\" ><span>" + userNm + "</span></a>" :
				(typeFlag == "2") ? "<a class=\"poll_questionText\"><div class=\"poll_questionTextWrap\"><span>" + fileData + "</span></div></a>" : "";
			div.innerHTML = "<span class=\"img_del\" title=\"delete image\" onclick=\"Ctrl.BGImg.remove('" + seqNo + "');\"><a class=\"btn_x\"></a></span>\
							 <span class=\"tl\"></span>\
							 <span class=\"tc\"><div class=\"rotation" + ieContext + "_handle\"><div class=\"rotation" + ieContext + "_bar\"></div><div class=\"rotate" + ieContext + "_handle\"></div></div></span>\
							 <span class=\"tr\"></span>\
							 <span class=\"ml\"></span>\
							 <div class=\"imgDiv\">" + userInfo + "</div>\
							 <span class=\"mr\"></span>\
							 <span class=\"bl\"></span>\
							 <span class=\"bc\"></span>\
							 <span id=\"rz_br\"class=\"br\"></span>";

			if (degree > 0) {
				$(div).css('-moz-transform', 'rotate(' + degree + 'deg)')
					.css('-webkit-transform', 'rotate(' + degree + 'deg)')
					.css('-o-transform', 'rotate(' + degree + 'deg)')
					.css('transform', 'rotate(' + degree + 'deg)')
					.css('ms-transform', 'rotate(' + degree + 'deg)')
			}

			// --> IE안됨
			$("#sketch" + UI.current).append(div);

			var drawToCanvas = function (finalOffset, positionX, positionY) {

				div.style.backgroundImage = 'none';

				var container = document.getElementById(UI.CONTAINER);
				var termY = headerHeight + 9;
				// var finalOffset = $(this).offset();

				console.log("[Ctrl.BGImg] drawToCanvas / container.scrollLeft : " + container.scrollLeft);
				console.log("[Ctrl.BGImg] drawToCanvas / container.scrollTop : " + container.scrollTop);
				console.log("[Ctrl.BGImg] drawToCanvas / finalOffset.left : " + finalOffset.left);
				console.log("[Ctrl.BGImg] drawToCanvas / finalOffset.top : " + finalOffset.top);
				//var posX = finalOffset.left + (container.scrollLeft || container.scrollLeft);
				//var posY = finalOffset.top - termY + (container.scrollTop || container.scrollTop);

				var posX = positionX;
				var posY = positionY;

				if (lastDegree > 0) {
					posX = positionX;
					posY = positionY;
				}

				var data = Ctrl.BGImg.data.get(seqNo);
				var orgW = data.orgw,
					orgH = data.orgh;
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

				console.log("[Ctrl.BGImg] drawToCanvas / posX : " + posX);
				console.log("[Ctrl.BGImg] drawToCanvas / posY : " + posY);

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

			$("span.tc", div).mousedown(function (e) {
				console.log("rotation mousedown fired..");
				// 1. rotate flag setting
				Ctrl.BGImg.isRotate = true;

				// 2. set Editor background
				div.style.backgroundImage = "url('" + img.src + "')";
				div.style.opacity = 0.5;

				var data = Ctrl.BGImg.data.get(seqNo);

				// 3. offset setting
				var layerLeft = data.cvs.posx;
				var layerTop = data.cvs.posy;
				dial.centerX = layerLeft + dial.width() / 2;
				dial.centerY = layerTop + dial.height() / 2;

				offset = Math.atan2(dial.centerY - e.pageY, e.pageX - dial.centerX);

				// 4. imgcanvas redraw
				Ctrl.BGImg.redraw(seqNo, "");

				// 5. draggable 정지
				$(div).draggable("disable");

				$(document).mousemove(function (e) {
					if (Ctrl.BGImg.isRotate) {
						var newOffset = Math.atan2(dial.centerY - e.pageY, e.pageX - dial.centerX);
						r = (offset - newOffset) * RAD2DEG;

						//	270이 넘어가면 -로 바뀌는 버그가 있어서 일시적으로 이렇게 처리. ** 수정 및 원인파악 필요
						if (r < 0) r += 360;
						divDegree = r + lastDegree;

						if (divDegree > 360) divDegree = divDegree - 360;

						$(div).css('-moz-transform', 'rotate(' + divDegree + 'deg)')
							.css('-webkit-transform', 'rotate(' + divDegree + 'deg)')
							.css('-o-transform', 'rotate(' + divDegree + 'deg)')
							.css('transform', 'rotate(' + divDegree + 'deg)')
							.css('ms-transform', 'rotate(' + divDegree + 'deg)')
					}

				})

				$(document).mouseup(function () {
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
					var orgW = data.orgw,
						orgH = data.orgh;

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

					if (lastDegree > 0) {

						var halfWidth = $(div).width() / 2;
						var halfHeight = $(div).height() / 2;

						// Rotate를 필요한 부분만 적용하기 위해서는 현재 그려야 하는 놈 바로 앞에서 행렬을 초기화 해줘야 한다.
						context.setTransform(1, 0, 0, 1, 0, 0);
						context.translate(posX + halfWidth, posY + halfHeight);

						var radians = lastDegree * Math.PI / 180;

						context.rotate(radians);
						context.drawImage(img, -(halfWidth), -(halfHeight), $(div).width(), $(div).height());

					} else {
						context.setTransform(1, 0, 0, 1, 0, 0);
						context.drawImage(img, posX, posY, $(div).width(), $(div).height());
					}

					PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, lastDegree, fileData);

					// data  update
					Ctrl.BGImg.updatePos(seqNo, posX, posY, $(div).width(), $(div).height(), lastDegree);

					setTimeout(function () {
						Ctrl.BGImg.isRotate = false;
						div.style.opacity = 1;

						$(div).draggable("enable");

					}, "500");

					$(document).unbind("mouseup");
					$(document).unbind("mousemove");
				})
			})

			// 이미 가장 최상의 order인경우 cancel
			var getMaxOrd = function () {
				var maxIndex = 0;
				$(".img_box_edit", "#sketch" + UI.current).each(function () {
					var zIndex = $(this).css("zIndex");
					var currentIndex = parseInt(zIndex);
					if (currentIndex > maxIndex) {
						maxIndex = currentIndex;
					}
				});
				return maxIndex;
			}

			var tmpCount = 0;
			// 정렬
			$(div).click(function (e) {
				if (Ctrl.BGImg.isDrag || Ctrl.BGImg.isRotate) {
					console.log("Image DIV click event fired..");
					console.log(e);
					return;
				}

				var zIndex = $(this).css("zIndex");
				if (getMaxOrd() <= zIndex) {
					return;
				} else {
					zIndex = getMaxOrd() + 1;
				}

				var container = document.getElementById(UI.CONTAINER);
				var termY = headerHeight + 9;

				var data = Ctrl.BGImg.data.get(seqNo);
				// 최초 포지션...
				var posX = data.cvs.posx;
				var posY = data.cvs.posy;
				var degree = data.cvs.degree;
				var orgW = data.orgw,
					orgH = data.orgh;

				// var orgW = Ctrl.BGImg.orgW, orgH = Ctrl.BGImg.orgH;// var imgObj = Ctrl.BGImg.getSize(img, imgCanvas);
				var scaleW = ($(div).width() / orgW / (canvas.width / Ctrl.BGImg.baseSize[0]));
				var scaleH = ($(div).height() / orgH / (canvas.height / Ctrl.BGImg.baseSize[1]));

				PacketMgr.Master.img(img.src, imgCanvas, seqNo, scaleW, scaleH, posX, posY, "0", ++Ctrl.BGImg.ord, typeFlag, userNm, thumbnail, degree, fileData);

				// 정렬 맞춤
				Ctrl.BGImg.redraw("", seqNo);

				$(div).css("zIndex", zIndex);
				$(".ui-resizable-handle", div).css("zIndex", zIndex);
			});

			var startX = 0;
			var startY = 0;

			// 편집기능 별도 추가 필요.


			// 이미지 드래그 이벤트 정의
			$(div).draggable({
				// handle : $("#bgMoveDiv"),
				handle: ".imgDiv, span.tc",
				containment: $('#docWrapper'),
				start: function (e, ui) {
					Ctrl.BGImg.isDrag = true;
					Ctrl.BGImg.redraw(seqNo, "");
					div.style.backgroundImage = "url('" + img.src + "')";
					div.style.opacity = 0.5;

					var data = Ctrl.BGImg.data.get(seqNo);
					if (data.cvs.degree > 0) {
						startX = ui.position.left;
						startY = ui.position.top;
					}
				},

				drag: function (e, ui) {
					if (lastDegree > 0) {
						var data = Ctrl.BGImg.data.get(seqNo);
						// 최초 포지션...
						var posX = data.cvs.posx;
						var posY = data.cvs.posy;
						console.log("[Ctrl.BGImg] draggable.stop() / posX : " + posX + ", posY : " + posY);
						ui.position.left = posX + (ui.position.left - startX);
						ui.position.top = posY + (ui.position.top - startY);
					}
				},
				stop: function (e, ui) {
					Ctrl.BGImg.isDrag = false;
					console.log("[Ctrl.BGImg] draggable.stop() / ui.position.left : " + ui.position.left + ", ui.position.top : " + ui.position.top);
					drawToCanvas($(this).offset(), ui.position.left, ui.position.top);
				}
			});


			$(div).resizable({
				handles: "e,s,se,n,ne,w,sw,nw",
				animation: true,
				containment: $('#docWrapper'),
				aspectRatio: true, // 정사각 비율로 resize
				minWidth: 100,
				minHeight: 100,
				start: function (e) {
					Ctrl.BGImg.isDrag = false;
					Ctrl.BGImg.redraw(seqNo, "");
					div.style.backgroundImage = "url('" + img.src + "')";
				},
				drag: function (e, ui) {

				},
				stop: function (e, ui) {
					drawToCanvas($(this).offset(), ui.position.left, ui.position.top);
				}
			});



			this.auth();

			return div;
		},


		/**
		 * Ctrl.BGImg.remove (재정의)
		 **/
		remove: function (seqNo) {
			// var packet = {"cmd":"img","seqno":""+seqno+"","posx":"" + fixedX + "","posy":"" + fixedY + "","scalew":"" + scaleW + "","scaleh":"" + scaleH + "","url": ""+url+"","mode":mode};
			Ctrl.Modal.confirm(_msg("confirm.remove.file"), function () {
				// var imgCanvas = UI.skboards[UI.current-1].getCanvas("img");
				var board = UI.getBoard();
				var imgCanvas = board.getCanvas("img");
				// var data = bgImg.list.length < 1 ? "" : bgImg.list[0];
				var data = Ctrl.BGImg.data.get(seqNo);
				if (data != null) {
					var url = data.url || "";
					var ord = data.ord || "0";

					if (seqNo != "") PacketMgr.Master.img(url, imgCanvas, seqNo, 1, 1, 0, 0, "1", ord);

					Ctrl.BGImg.removeLayer(seqNo);
				}
			});
		},

	},

	Text: {
		/**
		 * Ctrl.Text._setEvent : 재정의
		 *
		 */
		_setEvent: function () {
			$(".ffamily", "#fontbox").change(function () {
				var val = $(this).val();
				$("#txt_area").css("fontFamily", val);

				Ctrl.Text.resizeTextArea();
			});

			$(".fsize", "#fontbox").change(function () {
				var val = $(this).val();
				$("#txt_area").css("fontSize", val + "px");

				Ctrl.Text.resizeTextArea();
			});

			$("a.edit_btn", "#fbox_wrap").click(function () {
				if (!Ctrl._checkAuth(true)) return;

				var idx = $(this).index();
				var func = Ctrl.Text.edit[idx - 2];
				Ctrl.Text.toggleEdit(func);

				if ($(this).hasClass("checked")) {
					$(this).removeClass("checked");
				} else {
					$(this).addClass("checked");
				}
			});

			// 삭제
			$("#txt_del_btn").click(function () {
				if (!Ctrl._checkAuth(true)) return;

				// send remove packet - 생성하고, 저장 안한경우 레이어만 지운다.
				var id = Ctrl.Text.current;
				if (id != "") {
					var packet = Ctrl.Text._getData(id);
					PacketMgr.Master.textbox(packet.id, "2", packet.text, packet.face, packet.size, packet.r, packet.g, packet.b, packet.w, packet.h, packet.x, packet.y, packet.italic, packet.bold);
				} else {
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
				change: function (hex, opacity) {
					$("#txt_area").css("color", hex);
				},
				theme: 'default'
			});

			$("#txt_area").blur(function () {
				Ctrl.BGImg.auth();
				PDFViewer.auth();
			});

			$("#txt_area").change(Ctrl.Text.resizeTextArea);
			$("#txt_area").keydown(Ctrl.Text.resizeTextArea);
			$("#txt_area").keyup(Ctrl.Text.resizeTextArea);

			$("#txt_area").resizable({
				handles: "se",
				animation: true,
				containment: $('#docWrapper'),
				aspectRatio: false, // 정사각 비율로 resize
				minWidth: 231,
				minHeight: 71,
				start: function (e) {

				},
				resize: function (e, ui) {

				},
				stop: function (e, ui) {

				}
			});

			// text annotation 관련 이벤트
			this._setTextEvent();
		},


		/**
		 * Ctrl.Text.resizeTextArea : 재정의
		 *
		 */
		resizeTextArea: function (e) {
			var text = document.getElementById("txt_area");
			if (text.scrollHeight < 100) text.scrollHeight = 100 + "px";

			text.style.height = 'auto';
			text.style.height = text.scrollHeight + 'px';

			// text update width
			if (typeof (e) != "undefined" && e != null && (e.type == "keyup" || e.type == "change")) {
				var txt = text.value;
				txt = txt.replace(/\n/g, "<br />");

				$("#fk_1").html(txt);
				$("#fk_1").css("fontFamily", text.style.fontFamily);
				$("#fk_1").css("fontSize", text.style.fontSize);

				var w = $("#fk_1").width() + 60;
				if (w < Ctrl.Text.editorMinWidth) {
					w = Ctrl.Text.editorMinWidth;
				}
				text.style.width = w + "px";
				$("#text_input_box div.ui-wrapper").width(w);
			}

			if (e.type == "keydown") {
				if (e.keyCode == Ctrl.Text.CTRLKEY || e.keyCode == Ctrl.Text.CMDKEY) Ctrl.Text.ctrlDown = true;
				if (Ctrl.Text.ctrlDown) {
					// ctrl + a => select all
					if (e.keyCode == Ctrl.Text.AKEY) {
						$(text).select();
					}
				}

			} else if (e.type == "keyup") {
				if (e.keyCode == Ctrl.Text.CTRLKEY || e.keyCode == Ctrl.Text.CMDKEY) Ctrl.Text.ctrlDown = false;
			}
		},


		/**
		 * Ctrl.Text._setTextLayer (재정의)
		 *
		 */
		_setTextLayer: function (id) {
			console.log("[Ctrl.Text._setTextLayer]");
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

			console.log("[_setTextLayer] textWrapper의 div / x : " + x + ", y : " + y);



			var layer = document.getElementById("txt_" + this.current);
			if (layer) {
				// 업데이트
				layer.style.left = x + "px";
				layer.style.top = y + "px";
				layer.style.width = w + "px";
				layer.style.height = h + "px";
				layer.cursor = (PacketMgr.isMC) ? "text" : "default";

				$("#txt_area").val("");

				Ctrl.Text._syncEditor(packet);

			} else {
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
				$(div).click(function (e) {
					// console.log("Ctrl.Text.dragging : " + Ctrl.Text.dragging);
					/************************************** 편집모드 **********************************************/
					if (Ctrl.Text.dragging) return;

					// 권한이 안맞거나 줌 상태인경우 초기화 시킨다.
					if (!Ctrl._checkAuth(false) || UI.scale > 1) {
						Ctrl.Text.current = "";
						$("#fontbox").hide();
						return;
					}

					var saveRet = false;
					// add box or 타 edit box가 띄워져 있는경우 저장
					if (Ctrl.Text.isOpened() && Ctrl.Text.current != id) {
						saveRet = Ctrl.Text.save();
						if (!saveRet) {
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
					zIndex: 49,
					start: function (e, ui) {
						Ctrl.Text.dragging = true;
						div.style.cursor = "move";

						if (Ctrl.Text.isOpened()) {
							var saveRet = Ctrl.Text.save();
							if (!saveRet) Ctrl.Text.cancel(false);
						} else {
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
						if (selectPacket.bold == "1") div.style.fontWeight = "bold";
						if (selectPacket.italic == "1") div.style.fontStyle = "italic";
						div.style.color = "rgb(" + selectPacket.r + "," + selectPacket.g + "," + selectPacket.b + ")";

						div.innerHTML = selectPacket.text;

						$(div).addClass("m_down");

						// draggable에는 IE는 스크롤 시작 좌표 버그가 있다. 따라서 dragging 시 빼준다.
						if (Utils.browser("msie")) $(this).data("startingScrollTop", $(container).scrollTop());
					},
					drag: function (e, ui) {
						if (Utils.browser("msie")) {
							var st = parseInt($(this).data("startingScrollTop"));
							ui.position.top += st;
						}
					},
					stop: function (e, ui) {

						div.innerHTML = "";

						div.style.fontFamily = "";
						div.style.fontSize = "";
						div.style.fontWeight = "";
						div.style.fontStyle = "";
						div.style.color = "";

						var finalOffset = $(this).offset();
						console.log("finalOffset");
						console.log(finalOffset);

						var posX = finalOffset.left + (container.scrollLeft || container.scrollLeft);
						var posY = finalOffset.top + (container.scrollTop || container.scrollTop);
						// var x = $("#fontbox").position().left + ( container.scrollLeft || container.scrollLeft);
						// var y = $("#fontbox").position().top + ( container.scrollTop || container.scrollTop);

						//						var top = parseInt($("#fbox_wrap").css("top").replace("px", ""));

						// top + padding

						//						posY += (top - 10);

						console.log("[_setTextLayer] fontbox 위치..  posX : " + posX + ", posY : " + posY);

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

						setTimeout(function () {
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

		cancel: function (forceSave) {
			// pen 모드로 변경시 모든 정보를 저장하고 Text 모드를 해제한다.
			cordova.exec(null, null, "CommonPlugin", "cancelText", []);

			$("#text_btn").removeClass("checked");
			$("#text_btn2").removeClass("checked");


			if (forceSave) {
				/**
				 var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
				 PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
				 **/
				if (Ctrl.Text.isOpened() && (Ctrl.Text.current != "" || !Ctrl.Text.isBlank())) {
					var saveRet = Ctrl.Text.save();
					if (!saveRet) {
						var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
						PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
					}
				}

			} else {
				var beforePacket = Ctrl.Text._getData(Ctrl.Text.current);
				console.log("beforePacket : " + JSON.stringify(beforePacket));
				if (typeof beforePacket != 'undefined' && beforePacket != null) { // textbox 관련 패킷을 그리는 중에 undefined가 튀는 현상이 확인되어 예외처리 추가함 - 2016.10.21
					PacketMgr.Master.toCanvasPage(beforePacket, UI.current);
				}
			}

			Ctrl.Text.current = "";
			$("#fontbox").hide();
		},

		destroy: function () {

			$("#sketch" + UI.current).unbind("click");

			$("#txt_area").unbind("blur");

			$("#txt_area").unbind("change");

			$("#txt_area").unbind("keydown");

			$("#txt_area").unbind("keyup");

			$("#txt_area").resizable("destroy");

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


	/**
	 * Ctrl.Member
	 */
	Member: {

		list: null,
		classList: null,

		type_default: "0",
		type_student: "1",
		type_teacher: "2",

		/**
		 * Ctrl.Member.init() : 재정의
		 *
		 **/
		init: function () {
			console.log("[Ctrl.Member.init]");

			var userInfo = CanvasApp.info.user;
			var classInfo = CanvasApp.info.onlineClassList;
			console.log(userInfo);
			console.log(classInfo);

			var list = [];
			var len = userInfo == null ? 0 : userInfo.length;
			for (var i = 0; i < len; i++) {
				var map = userInfo[i];
				map.token = Utils.createUUID().substring(0, 8);

				list.push(map);
			}

			// 클래스 유저 리스트 추가 - 2016.07.22
			var classList = [];
			var len = classInfo == null ? 0 : classInfo.length;
			for (var i = 0; i < len; i++) {
				var map = classInfo[i];
				map.token = Utils.createUUID().substring(0, 8);

				classList.push(map);
			}

			// Ctrl.Member게스트 정보 추가..
			if (CanvasApp.info.userid == CanvasApp.info.userno) {
				/**
				this.info.userid = this.info.deviceid;
				this.info.userno = this.info.deviceid;
				this.info.usernm = guestName + "("+_msg("guest") +")";
				**/
				var currentUserInfo = {
					userid: CanvasApp.info.deviceid,
					userno: CanvasApp.info.deviceid,
					usernm: Utils.Local.get("guest") || "",
					usertype: this.type_default,
					token: Utils.createUUID().substring(0, 8)
				};

				list.push(currentUserInfo);
				classList.push(currentUserInfo);
			}

			this.list = list;
			this.classList = classList;
		},


		/**
		 * Ctrl.Member._addClass (재정의 - canvas_klounge.js에 정의된 내용)
		 * 클래스 유저 추가
		 */
		_addClass: function (userId, userName, userNo, isGuest, thumbnail) {

			var isExist = false;
			var len = this.classList == null ? 0 : this.classList.length;
			for (var i = 0; i < len; i++) {
				var userInfo = this.classList[i];
				if (userInfo.userid == userId) {
					isExist = true;
				}
			}

			if (!isExist) {
				this.classList.push({
					thumbnail: thumbnail,
					email: "",
					userid: userId,
					userno: userNo,
					usernm: userName,
					token: Utils.createUUID().substring(0, 8)
				})
			}
		},


		/**
		 * Ctrl.Member._removeClass (재정의)
		 * 클래스 유저 삭제
		 */
		_removeClass: function (userId) {
			if (this.classList == null) return;

			var len = this.classList == null ? 0 : this.classList.length;
			for (var i = 0; i < len; i++) {
				var userInfo = this.classList[i];
				if (userInfo.userid == userId) {
					this.classList = this.classList.without(userInfo);
					break;
				}
			}
		},


		/**
		 * Ctrl.Member.getClass (재정의)
		 * 클래스 유저 get
		 */
		getClass: function (key, type) {
			var len = this.classList == null ? 0 : this.classList.length;
			for (var i = 0; i < len; i++) {
				var classInfo = this.classList[i];
				if (classInfo[type] == key) {
					return classInfo;
					break;
				}
			}
			return null;
		},

		/**
		 * Ctrl.Member.ckConnect (재정의)
		 * 패킷 보내는놈이 실패난경우 이걸로 한번더 체크
		 */
		ckConnect: function (callback) {
			//------------- keep alive를 보내는 유저가 네트웍이 죽으면 포탈에 request 보내서 각자 살아있는지 체크한다.
			//var url = Utils.addContext(_url("ck.connection"));
			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("common.check.connect");

			Utils.request(url, "json", null, function (json) {
				// Utils.log("update position result : " + json.result);
				return (json.status == 200) ? callback(true) : callback(false);
			});
		},


		/**
		 * Ctrl.Member.newUser (재정의 함)
		 * RoomSvr.newuser에서 호출함..
		 * 새로 들어오는 유저 정보를 ListView에 출력할 때 호출하는 함수..
		 */
		newUser: function (userId, userName, userNo, isGuest, thumbnail, userType, isVideoAllow, fullScreenUserId) {

			var masterUserInfo = Ctrl.Member.get(userId, "userid");

			console.log("masterUserInfo : " + JSON.stringify(masterUserInfo));

			var creatorFlag = "0";
			var masterFlag = "0";
			var guestFlag = isGuest;

			// 1. 입장한 사람이 마스터 일때
			if (PacketMgr.creatorid == userId) {
				creatorFlag = "1";
				if (PacketMgr.masterid == userId)
					masterFlag = "1";

			} else if (PacketMgr.masterid == userId) { // 생성자가 아니면서 진행자인경우
				masterFlag = "1";
			} else {

			}

			if (userId == userNo) {
				userName = userName + " (" + _msg("guest") + ")";
			}


			/** 2016.06.20 현재 유저 */
			// 안드로이드에서는 플러그인 처리..
			// IOS에서는 initMyMedia 처리..
			if (userNo == RoomSvr.userno) {

				// 선생님만 캠허용 모드일 때, 선생님이나 개설자가 아니면 리턴..
				if (PacketMgr.isOnlyTeacherVCam && (!PacketMgr.isCreator && PacketMgr.usertype != "2")) {
					return;
				}
				console.log("[WebRTC / initMyMedia를 호출합니다.]");
				if (cordova.platformId == "android") {
					var cordovaParam = {
						isvideoallow: isVideoAllow
					};

					cordova.exec(function (result) {
						console.log("cordova.exec() success.. initMyMedia");
					}, function (result) {
						console.log("initMyMedia error : " + JSON.stringify(result));
					}, "VideoPlugin", "initMyMedia", [cordovaParam]);


				} else if (cordova.platformId == "ios") {
					var currentUserInfo = Ctrl.Member.getUserOnline(userNo, "userno");
					console.log("currentUserInfo : " + currentUserInfo + ", isVideoAllow : " + isVideoAllow);
					var isDisplay = isVideoAllow == 1 ? true : false;

					// VideoCtrl.initMyMedia(CanvasApp.info.roomid, currentUserInfo, PacketMgr.isMC, isDisplay);
				}
			}


			/** 2016.06.20 타 유저가 들어왔을시 진행자가 sync 패킷을 전달해 준다. */
			if (userNo != RoomSvr.userno) {
				// 타인이 들어왔을때 비디오 share sync를 맞춰준다.
				Ctrl.VShare.sendMCPlayerStatus(userNo);
			}

			// member 추가
			Ctrl.Member._add(userId, userName, userNo, isGuest, thumbnail, userType);

			// 미 참여자 목록에서 제거
			Ctrl.Member.removeNotAttendee(userNo);

			VideoCtrl.fullScreenUserId = fullScreenUserId;

			var cordovaParams = [{
				userid: userId,
				usernm: userName,
				userno: userNo,
				usertype: userType,
				creator: creatorFlag,
				master: masterFlag,
				guest: guestFlag,
				thumbnail: thumbnail
			}];

			//            if(CanvasApp.info.userno != userNo)  // 새로 들어온 유저에 대해서만 리스트에 뿌리기 위한 조건문
			console.log("새로 리스트에 추가할 참여자 정보 : " + JSON.stringify(cordovaParams));

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. addRoomUser");
			}, function (result) {
				console.log("addRoomUser error : " + JSON.stringify(result));
			}, "UserPlugin", "addRoomUser", cordovaParams);

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. addChatUserList");
			}, function (result) {
				console.log("addChatUserList error : " + JSON.stringify(result));
			}, "CommunicationPlugin", "addChatUserList", cordovaParams);

			//}
		},


		/**
		 * Ctrl.Member.removeRoomUser (신규 정의)
		 * RoomSvr.leaveuser에서 호출함..
		 * 새로 들어오는 유저 정보를 ListView에 출력할 때 호출하는 함수..
		 */
		removeRoomUser: function (userid, username, userno) {
			var params = [{
				userid: userid,
				usernm: username,
				userno: userno
			}];

			// ListView에 참여자 퇴장 처리
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removeRoomUser");
			}, function (result) {
				console.log("removeRoomUser error : " + JSON.stringify(result));
			}, "UserPlugin", "removeRoomUser", params);

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removeChatUserList");
			}, function (result) {
				console.log("removeChatUserList error : " + JSON.stringify(result));
			}, "CommunicationPlugin", "removeChatUserList", params);

			// 멤버 삭제
			Ctrl.Member._remove(userid);

			if (PacketMgr.userid != userid) {
				var notiTitle = _msg("noti");
				var notiContent = username + _msg("noti.exit.meeting");
				Ctrl.Msg.show(notiContent);
			}

			/*
			 Utils.log(PollCtrl.isProgress);
			 if(PollCtrl.isProgress && PollCtrl.progressPoll != null)
			 PollCtrl.Action.Attender.exitPoll(PollCtrl.progressPoll);


			 if(isFirstUser) {
			 PacketMgr.changeKeepAlive();
			 }
			 */

		},


		/**
		 * Ctrl.Member.addClassUser : 재정의함
		 * RoomSvr.userclassuser에서 호출함..
		 * 새로 들어오는 클래스 유저 정보를 ListView에 출력할 때 호출하는 함수..
		 */
		addClassUser: function (roomId, userId, userName, userNo, isGuest, thumbnail, userRoomId, userRoomSeqNo, userType, connectedRoomTitle, connectedRoomCreatorName, connectedRoomSeparate) {

			var cordovaParams = [{
				//roomid    : roomId,
				userno: userNo,
				userid: userId,
				usernm: userName,
				usertype: userType,
				isguest: isGuest,
				thumbnail: thumbnail,
				roomid: userRoomId,
				seqno: userRoomSeqNo,
				connected_roomid: roomId,
				connected_roomtitle: connectedRoomTitle,
				connected_roomcreatorname: connectedRoomCreatorName,
				connected_roomseparate: connectedRoomSeparate
			}];

			// member 추가
			Ctrl.Member._addClass(userId, userName, userNo, isGuest, thumbnail);

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. addClassUserList");
			}, function (result) {
				console.log("addClassUserList error : " + JSON.stringify(result));
			}, "UserPlugin", "addClassUserList", cordovaParams);

			// 클래스 채팅 유저 리스트 추가
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. addClassChatUserList");
			}, function (result) {
				console.log("addClassChatUserList error : " + JSON.stringify(result));
			}, "CommunicationPlugin", "addClassChatUserList", cordovaParams);
		},


		/**
		 * Ctrl.Member.leaveClassUser : 재정의함
		 * RoomSvr.leaveclassuser에서 호출함..
		 * 새로 들어오는 유저 정보를 ListView에 출력할 때 호출하는 함수..
		 */
		leaveClassUser: function (roomId, userId, userName, userNo) {

			// 멤버 삭제
			Ctrl.Member._removeClass(userId);

			var cordovaParams = [{
				roomid: roomId,
				userno: userNo,
				userid: userId,
				usernm: userName
			}];

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removeClassUserList");
			}, function (result) {
				console.log("removeClassUserList error : " + JSON.stringify(result));
			}, "UserPlugin", "removeClassUserList", cordovaParams);

			// 클래스 채팅 유저 삭제
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removeChatUserList");
			}, function (result) {
				console.log("removeClassChatUserList error : " + JSON.stringify(result));
			}, "CommunicationPlugin", "removeClassChatUserList", cordovaParams);
		},


		/**
		 * Ctrl.Member.removeNotAttendee : 재정의함
		 * 미참여자 목록에서 삭제하기
		 * 새로 들어오는 유저 정보를 ListView에 출력할 때 호출하는 함수..
		 */
		removeNotAttendee: function (userNo) {
			console.log("Ctrl.Member.removeNotAttendee - userNo : " + userNo);
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. removeNotAttendee");
			}, function (result) {
				console.log("removeNotAttendee error : " + JSON.stringify(result));
			}, "UserPlugin", "removeNotAttendee", [userNo]);

		},

		refreshNotAttendee: function () {

			$("#not_attendee_wrapper").html("");

			// var roomId
			var url = Utils.addContext(_url("notify.invite.not.attend"));
			var params = {
				roomid: PacketMgr.roomid
			};

			Utils.request(url, "json", params, function (json) {
				var list = json != null ? json.list : null;
				var len = list == null ? 0 : list.length;

				var buf = new StringBuffer();
				for (var i = 0; i < len; i++) {
					var data = list[i];
					var userNo = data.userno;
					var userId = data.userid;
					var userNm = data.usernm;
					var thumbnail = data.thumbnail;

					if (userNo == PacketMgr.userno) continue;

					html = "<div class=\"user_box\" userno=\"" + userNo + "\" userid=\"" + userId + "\">\
                                    <img class=\"user_photo\" src=\"" + thumbnail + "\"/>\
                                    <span class=\"user_name\">" + userNm + "</span>\
                                    <span class=\"\" ></span>\
                                </div>";

					buf.append(html);
				}

				$("#not_attendee_wrapper").html(buf.toString());
				$("#abasenceCnt").html(buf.length());

			}, function (e) {
				console.log(e);
			});
		},



		/**
		 * Ctrl.Member.requestAuthChange
		 */
		requestAuthChange: function (actionType, userId) {
			console.log("requestAuthChange - actionType : " + actionType);
			var msgStr = actionType == "opener" ? _msg("confirm.get.auth") : _msg("confirm.allow.auth");
			var params = [{
				type: '5',
				title: '권한',
				msg: msgStr + '|' + actionType + '|' + userId,
			}];

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. openConfirmDialog");
				console.log(result);
			}, function (result) {
				console.log("openConfirmDialog error : " + JSON.stringify(result));
			}, "CommonPlugin", "openConfirmDialog", params);
		},


		/**
		 * Ctrl.Member.authChange (재정의)
		 * 진행 권한을 넘기거나 가져올 때 호출하는 함수..
		 *   - 2016.10.18 : 기진행자가 진행중일때는 권한을 넘기거나 가져올 수 없도록 예외처리 추가함..
		 */
		authChange: function (actionType, userId, isConfirm) {
			console.log("[Ctrl.Member.authChange] actionType : " + actionType);

			var userIdVal = isConfirm ? userId : PacketMgr.userid;

			if (actionType != "opener") {
				if (!PacketMgr.isAllowMaster) {
					Ctrl.Msg.show(_msg("msg.master.change.limit"));
					return;
				}
			}


			// 기진행자가 진행중인 상황에 대한 예외처리.. - 2016.10.18
			var masterChange = function (type, userId) {
				if (Ctrl._checkWaiting()) {
					var masterUserInfo = Ctrl.Member.getUserOnline(PacketMgr.masterid, "userid");
					var authContent = masterUserInfo.usernm + _msg("noti.leading.host");
					Ctrl.Msg.show(authContent);
				} else {
					if (type != null && type == "creator") {
						PacketMgr.Master.masterWithDraw();
					} else {
						PacketMgr.Master.masterChange(userId);
					}
				}
			}

			if (actionType == "opener") {
				if (PacketMgr.creatorid != PacketMgr.userid) {
					// 생성자만 권한회수 가능
					console.log("생성자가 아닙니다.");
					return;
				}

				masterChange("creator", userId);

			} else if (actionType == "get_authority") {
				masterChange("user", userId);

			} else if (actionType == "send_authority") {
				masterChange("user", userId);

			} else if (actionType == "chairman") {
				// pass

			} else {
				// pass
			}
		},


		/**
		 * Ctrl.Member.masterChange : 재정의함
		 * Command에서 호출함.. masterchange 시에 UI 처리하는 함수
		 *   - userId : 마스터 권한을 부여받을 유저의 아이디 (String)
		 */
		masterChange: function (userId) {
			var masterUserInfo = Ctrl.Member.get(userId, "userid");
			var masterUserNm = "";
			if (masterUserInfo != null) {
				masterUserNm = masterUserInfo.usernm;
			}

			console.log("[Ctrl.Member.masterChange] 파라미터 userId : " + userId);
			console.log("[Ctrl.Member.masterChange] PacketMgr.creatorid (현재 개설자): " + PacketMgr.creatorid);
			console.log("[Ctrl.Member.masterChange] PacketMgr.masterid (새로 마스터가 된 아이디) : " + PacketMgr.masterid);
			console.log("[Ctrl.Member.masterChange] PacketMgr.userid (내 아이디): " + PacketMgr.userid);
			console.log("[Ctrl.Member.masterChange] masterUserNm : " + masterUserNm);
			//console.log("[Ctrl.Member.masterChange] Ctrl.Member.list : " + Ctrl.Member.list.toString());


			cordova.exec(function (result) {
				console.log("cordova.exec() success.. changeMaster");
			}, function (result) {
				console.log("changeMaster error " + JSON.stringify(result));
			}, "UserPlugin", "changeMaster", [{
				userid: userId
			}]);


			if (cordova.platformId == "android") {
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. VideoPlugin.changeMaster");
				}, function (result) {
					console.log("VideoPlugin.changeMaster error : " + JSON.stringify(result));
				}, "VideoPlugin", "changeMaster", [{
					userid: userId
				}]);
			}
			// this.moveMasterLayer(userId);  // 웹에서만 쓰는 UI 처리용 함수..
		},


		/**
		 * Ctrl.Member.masterWithDraw : 재정의함
		 * Command에서 호출함.. 권한 가져오기 했을 때 UI 처리하는 함수
		 */
		masterWithDraw: function () {

			console.log(Ctrl.Member.list);
			console.log("권한 가져가는 유저의 id : " + PacketMgr.creatorid);

			if (PacketMgr.creatorid == PacketMgr.userid) { // 개설자가 권한을 가져왔을 경우..
				console.log("[Ctrl.Member.masterWithDraw] PacketMgr.creatorid : " + PacketMgr.creatorid + ", PacketMgr.userid : " + PacketMgr.userid);

				var params = [{
					userid: PacketMgr.creatorid
				}];

				console.log(params);
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. changeMaster");
				}, function (result) {
					console.log("changeMaster error : " + JSON.stringify(result));
				}, "UserPlugin", "changeMaster", params);


				/*
				 $(".chairman", "#user_wrapper").each(function(){
				 $(this).removeClass("chairman");

				 if(PacketMgr.isAllowMaster) {
				 var layer = $("span", $(this)).eq(1);
				 layer.addClass("send_authority");
				 }
				 });*/

				/*
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
				 });*/

			} else { // 내가 개설자에게 권한을 뺏긴 경우
				console.log("[Ctrl.Member.masterWithDraw] PacketMgr.creatorid : " + PacketMgr.creatorid + ", PacketMgr.userid : " + PacketMgr.userid);
				var params = [{
					userid: PacketMgr.creatorid
				}];

				cordova.exec(function (result) {
					console.log("cordova.exec() success.. changeMaster");
					console.log(result);
				}, function (result) {
					console.log("changeMaster error : " + JSON.stringify(result));
				}, "UserPlugin", "changeMaster", params);
				/*
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
				 */
			}

			//this.moveMasterLayer(PacketMgr.creatorid);  //UI 제어
		},


		/**
		 *  Ctrl.Member.getUserOnline : 재정의 함
		 *
		 **/
		getUserOnline: function (key, type) {
			return Ctrl.Member.get(key, type);
		},


		/**
		 *  Ctrl.Member.getUserOnline : 재정의 함
		 *  수업 참여자 리스트
		 **/
		getClassUserOnline: function (key, type) {
			return Ctrl.Member.getClass(key, type);
		},


		/**
		 * Ctrl.Member.kick (재정의)
		 * 참여자 강제퇴장
		 */
		kick: function (userNo, userId, userNm) {

			//            var userNo = $(thisNode.parentNode).attr("userno");
			if (PacketMgr.isCreator && userNo != null) {
				//                var targetUserInfo = Ctrl.Member.getUserOnline(userNo, "userno");
				//                var msg = _msg("confirm.kick.user") + " " + "("+ targetUserInfo.usernm +")";

				var msg = _msg("confirm.kick.user") + " " + "(" + userNm + ")";

				var cordovaParams = {
					userno: userNo,
					userid: userId,
					usernm: userNm
				};

				cordova.exec(function (result) {
					console.log("cordova.exec() success.. deportUser");
				}, function (result) {
					console.log("deportUser error : " + JSON.stringify(result));
				}, "UserPlugin", "deportUser", [cordovaParams]);


				//                Ctrl.Modal.confirm(msg, function() {
				//                    PacketMgr.Master.kickUser(PacketMgr.roomid, userNo, targetUserInfo.userid, targetUserInfo.usernm);
				//                });
			}
		},

		// 웹앱에서는 사용하지 않는 메서드
		moveMyRoom: function (cookieStr) {
			// URL 세팅
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("canvas.move.sub.room");

			var params = {
				roomid: RoomSvr.roomid,
				deviceid: RoomSvr.deviceid
			};

			$.ajax({
				type: "POST",
				url: url,
				beforeSend: function (request) {
					request.setRequestHeader('Cookie', cookieStr);
				},
				success: function (data) {
					console.log(data);
					if (data.result == '0') {
						var roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + data.code;
						setTimeout(function () {
							cordova.exec(function (result) {}, function (result) {}, "RoomPlugin", "moveRoom", [roomUrl]);
						}, "1500");
					} else {
						Ctrl.Msg.show(_msg("klounge.subroom.create.fail"));
					}
				}
			});

		},

		/**
		 * Ctrl.Member.callAllStudent (신규 정의함)
		 * 수업 선생님(클래스 마스터)이 학생(클래스 유저)들을 선생님 방으로 호출하는 메서드
		 * 선생님만 호출하도록 해야 함
		 **/
		callAllStudent: function () {
			var mode = "all"; // 전체 수업
			var roomId = PacketMgr.roomid;
			var drCode = PacketMgr.code;

			console.log("[callAllStudent] drCode : " + drCode);

			// roomid를 null로 보내야 전체 메시징
			PacketMgr.Master.BroadCast.call(mode, roomId, drCode);
		}

	},


	/**
	 * Ctrl.Modal : 재정의
	 */
	Modal: {



		/**
		 * Ctrl.Modal.password : 재정의
		 */
		password: function (callback) {
			var passwdModal = document.getElementById("passwdModal");
			if (passwdModal) {
				$(passwdModal).show();
			} else {
				var modalHtml = "<div id=\"passwdModal\" class=\"popup_dimd on\">\
                <div class=\"popup_box\" style=\"display: block;\">\
                <span class=\"popup_header\">\
                <span class=\"pop_tit\">" + _msg('m.password.title') + "</span>\
                </span>\
                <div class=\"popup_body\">\
                <div id=\"password_info\">\
                <span class=\"popup_msg1\"><b>" + _msg('m.password.msg.1') + "</b><br />" + _msg('m.password.msg.2') + "</span>\
                <span id=\"ck_byte3\" class=\"popinput_byte\"><font>0</font> / 6</span>\
                <input id=\"room_password\" type=\"password\" class=\"popinput_name\" placeholder=\"" + _msg('m.password.placeholder') + "\" maxlength=\"6\" onkeyup=\"Utils.textCutProcess(this, 6, '', 'ck_byte3', 'room_password');\" />\
                <div class=\"popbtn_box\">\
                <a href=\"javascript:Ctrl.goMainPage();\" class=\"btn_cancel\">" + _msg('m.password.btn.home') + "</a>\
                <a href=\"#\" class=\"btn_submit\">" + _msg('m.password.btn.enter') + "</a>\
                </div>\
                </div>\
                </div> \
                </div>\
                </div>";

				$(document.body).append(modalHtml);

				$(".btn_submit", "#passwdModal").click(function () {
					CanvasApp.submitPassword(function () {
						$("#passwdModal").val();
						callback();
					});
				});

				$("#room_password").keydown(function (e) {
					if (e.keyCode == '13') {
						CanvasApp.submitPassword(function () {
							callback();
						});
					}
				})
			}
		},

		title: function () {
			if (PacketMgr.userid == PacketMgr.creatorid) {
				var titleModal = document.getElementById("titleModal");
				if (titleModal) {
					$(titleModal).show();
				} else {
					var tcount = Ctrl.Modal._getBytes(RoomSvr.roomtitle);
					var html = "<div id=\"titleModal\" class=\"popup_dimd on\">\
                    <div class=\"popup_box\">\
                    <span class=\"popup_header\">\
                    <span class=\"pop_tit\">" + _msg("title") + "</span>\
                    <a href=\"javascript:Ctrl.Modal.hide('titleModal');\"></a>\
                    </span>\
                    <div class=\"popup_body\">\
                    <span class=\"popinput_byte\"><font id=\"ck_byte\">" + tcount + "/40</font></span>\
                    <textarea id=\"popup_title_txt\" class=\"popinput_title\" onkeyup=\"Utils.textCutProcess(this, 40, '', 'ck_byte', 'popup_title_txt');\">" + RoomSvr.roomtitle + "</textarea>\
                    <div class=\"popbtn_box\">\
                    <a href=\"javascript:Ctrl.Modal.updateTitle();\" class=\"btn_submit\">" + _msg("btn.save") + "</a>\
                    </div>\
                    </div>\
                    </div>\
                    </div>";
					$(document.body).append(html);
				}
			}
		},

		updateTitle: function (roomSettingInfo, title) {

			console.log("[updateTitle] authInfo : " + roomSettingInfo);
			console.log("[updateTitle] title : " + title);

			if (title.trim() == "") {
				Ctrl.Msg.show(_msg("insert.title"));
				return;
			}

			var authInfo = JSON.parse(roomSettingInfo);


			/*
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
			console.log(tcount);
			*/

			var tcount = title.length;

			if (tcount > 60) {
				Ctrl.Msg.show(_msg("validation.title"));
				return;
			}


			RoomSvr.roomtitle = title;

			var updateParams = {
				authtype: authInfo.authtype,
				ckUsePasswd: authInfo.passwdflag == "1" ? true : false,
				passwd: authInfo.passwd,
				chtopt: authInfo.chatopt,
				cmtopt: authInfo.cmtopt,
				expopt: authInfo.expopt,
				openflag: authInfo.openflag,
				content: authInfo.content,
				title: RoomSvr.roomtitle
			};

			Ctrl.Room.update(updateParams);

			var param = [{
				title: title
			}];
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. updateRoomTitle");
				console.log(result);
			}, function (result) {
				console.log("updateRoomTitle error : " + JSON.stringify(result));
			}, "RoomPlugin", "updateRoomTitle", param);

		},


		/**
		 * Ctrl.Modal.confirm : 재정의
		 * 앱에서 confirm창을 띄울 수 있도록 재정의 함..
		 **/
		confirm: function (msg, cancelable, callback) {

			var isCancel = true;
			if (typeof (cancelable) == 'function') {
				isCancel = true;
			} else if (typeof (cancelable) == 'undefined') {
				isCancel = true;
			} else if (typeof (cancelable) == 'boolean') {
				isCancel = cancelable;
			} else {
				isCancel = cancelable;
			}

			var cordovaParam = {
				body_message: msg,
				cancelable: isCancel
			};

			console.log("[Ctrl.Modal.confirm] params : " + JSON.stringify(cordovaParam));

			cordova.exec(function (data) {
				console.log("cordova.exec() success.. showConfirm");
				console.log(data);
				var confirmResult = data.result;
				if (confirmResult) {
					if (typeof (cancelable) == 'function') {
						cancelable();
					} else {
						callback();
					}
				} else {

				}

			}, function (data) {
				console.log("showConfirm error : " + JSON.stringify(data));
			}, "CommonPlugin", "showConfirm", [cordovaParam]);


			/*
			$(".btn_confirm", "#cfmModal").click(function(){
				if(callback) callback();

				Ctrl.Modal.destroyConfirm();
			});
			*/
		}
	},

	/**
	 * Ctrl.Comment
	 * 댓글 (코멘트)
	 *   - setEvent : 댓글 관련 이벤트 정의
	 *   - add : 댓글 등록
	 *   - remove : 댓글 삭제
	 */
	Comment: {
		/**
		 *  Ctrl.Comment.setEvent (재정의)
		 *  앱에서는 사용하지 않는 이벤트이므로 빈 함수로 재정의 하였음.
		 */
		setEvent: function () {

		},

		add: function (txt) {
			// 중복답변 처리 필요
			if (Ctrl.Comment.isProc) return;

			if (!PacketMgr.isAllowComment && !PacketMgr.isMC) {
				Ctrl.Msg.show(_msg("not.allow"));

				//$("#" + Ctrl.Comment.id_content).val("");
				return;
			}

			//var txt = $("#" + Ctrl.Comment.id_content).val();

			if (txt.trim() == "") {
				Ctrl.Msg.show(_msg("insert.comment"));
				//$("#" + Ctrl.Comment.id_content).focus();
				return;
			}

			//Ctrl.Comment.show();

			var deviceId = device.uuid;

			// URL 세팅
			var svrFlag = _prop("svr.flag");
			var svrHost = _prop("svr.host." + svrFlag);
			var url = svrHost + _prop("canvas.comment.add");

			var params = {
				roomid: PacketMgr.roomid,
				deviceid: deviceId,
				content: txt,
				left: Ctrl.Comment.left,
				top: Ctrl.Comment.top,
				userid: CanvasApp.info.userid,
				userno: CanvasApp.info.userno,
				usernm: CanvasApp.info.usernm
			};


			Ctrl.Comment.isProc = true;
			Utils.request(url, "json", params, function (json) {
				if (json.result == 0) {
					// $("#" + Ctrl.Comment.id_empty).hide();

					//$("#" + Ctrl.Comment.id_body).removeClass("empty");

					//$("#" + Ctrl.Comment.id_content).val("");

					var map = json.map;
					PacketMgr.Master.comment("0", map.commentno, map.userid, map.userno, map.usernm, map.cdatetime, map.content, map.thumbnail);

					var commentParam = [{
						commentno: map.commentno,
						userid: map.userid,
						userno: map.userno,
						usernm: map.usernm,
						thumbnail: map.thumbnail,
						cdatetime: map.cdatetime,
						content: map.content
					}];

					console.log(commentParam);

					// 댓글 로딩 (11개씩)
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. addCommentList");
					}, function (result) {
						console.log("addCommentList error : " + JSON.stringify(result));
					}, "CommunicationPlugin", "addCommentList", commentParam);


					//Ctrl.Comment.list(1);  // 코멘트 리스트를 첫 페이지부터 다시 읽어옴..

				} else if (json.result == -102) {
					Ctrl.Msg.show(_msg("comment.add.fail.auth"));
				} else {
					Ctrl.Msg.show(_msg("comment.add.fail"));
				}

				Ctrl.Comment.isProc = false;

			}, function () {
				Ctrl.Msg.show(_msg("comment.add.fail"));
				Ctrl.Comment.isProc = false;
			});
		},

		/**
		 * Ctrl.Comment.remove : 재정의
		 *   - commentNo : 댓글(코멘트) 고유번호
		 */
		remove: function (commentNo) {
			// "comment.remove.confirm"
			// if(!confirm(_msg("comment.remove.confirm"))) return;

			if (Ctrl.Comment.isProc) return;

			Ctrl.Comment.isProc = true;

			// URL 세팅
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("canvas.comment.remove");

			var params = {
				roomid: PacketMgr.roomid,
				commentno: commentNo,
				userno: CanvasApp.info.userno
			};

			Utils.request(url, "json", params, function (json) {
				if (json.result == 0) {
					PacketMgr.Master.comment("1", commentNo);

					var commentParam = [{
						commentno: commentNo
					}];

					// 댓글 삭제
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. removeCommentList");
					}, function (result) {
						console.log("removeCommentList error : " + JSON.stringify(result));
					}, "CommunicationPlugin", "removeCommentList", commentParam);

					//Ctrl.Comment.list();
				} else {
					Ctrl.Msg.show(_msg("comment.remove.fail"));
				}
				Ctrl.Comment.isProc = false;
			}, function () {
				Ctrl.Comment.isProc = false;
			});
		},

		show: function () {
			var container = "#" + Ctrl.Comment.id;
			if ($(container).hasClass("comment_box_mini")) {
				$(container).removeClass("comment_box_mini");
			}

		},

		/**
		 * Ctrl.Comment.list : 재정의
		 *
		 **/
		list: function (pageNo) {

			//var url = Utils.addContext(_url("comment.list"));

			Ctrl.Comment.current = pageNo;

			// URL 세팅
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("canvas.comment.list");

			var params = {
				roomid: PacketMgr.roomid,
				pageno: pageNo
			}

			Utils.request(url, "json", params, function (data) {
				var commentList = data.commentList;

				// 댓글 로딩 (11개씩)
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. addCommentList");
				}, function (result) {
					console.log("addCommentList error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "addCommentList", commentList);

			});
		},


		/**
		 * Ctrl.Comment.more : 재정의
		 * 웹앱용 버전으로 재정의함.
		 */
		more: function () {
			//$("#"  + Ctrl.Comment.id_more_cnt).remove();

			//$("#"  + Ctrl.Comment.id_more).remove();

			// URL 세팅
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("canvas.comment.list");

			var params = {
				roomid: PacketMgr.roomid,
				pageno: ++Ctrl.Comment.current
			}


			Utils.request(url, "json", params, function (data) {
				console.log("comment/list success..")
				console.log(data);
				if (data.commentList.length != 0) {
					// 댓글 가져와서 등록 (11개씩)
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. addCommentList");
						console.log(result);
					}, function (result) {
						console.log("addCommentList error : " + JSON.stringify(result));
					}, "CommunicationPlugin", "addCommentList", data.commentList);
				} else {
					//TODO : 댓글 리스트 없을 때 예외처리
					//Ctrl.Msg.show(_msg("m.password.incorrect"));
				}
			});
		},

		/**
		 *  Ctrl.Comment.layer : 재정의
		 *  웹앱에서는 쓰이지 않는 HTML UI 제어 코드들을 삭제하여 재정의 하였음.
		 *    packet : {"cmd":"comment","commentno":""+commentno+"","userid":""+userid+"","userno":""+userno+"","usernm":""+usernm+"","datetime":""+datetime+"","content":""+content+"","thumbnail": ""+thumbnail+"","type":type}
		 */
		layer: function (packet) {

			var type = packet.type;
			var commentNo = packet.commentno;
			var userId = packet.userid;
			var userNm = packet.usernm;
			var userNo = packet.userno;
			var thumbnail = packet.thumbnail;
			var cdatetime = packet.datetime;
			var content = packet.content;

			var commentParam = [{
				commentno: commentNo,
				userid: userId,
				usernm: userNm,
				userno: userNo,
				thumbnail: thumbnail,
				cdatetime: cdatetime,
				content: content
			}];

			if (type == "0") {

				// 댓글 불러와서 등록 (11개씩)
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. addCommentList");
				}, function (result) {
					console.log("addCommentList error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "addCommentList", commentParam);

				var notiTitle = _msg("noti");
				var notiContent = packet.usernm + _msg("comment.add.msg");

				//Ctrl.Noti.show(notiTitle, notiContent);
				Ctrl.Msg.show(notiContent); // 댓글 등록시 Toast 출력

				/*
				 if($("#commentWrapper").css("display") == "none") {
				 Ctrl.Comment.commentBadgeCnt++;
				 $(".btn_comment").addClass("checked");
				 $("#comment_badge").show();
				 $("#comment_badge").text(Ctrl.Comment.commentBadgeCnt);
				 }*/

			} else if (type == "1") {

				// 댓글 불러와서 삭제 (11개씩)
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. removeCommentList");
				}, function (result) {
					console.log("removeCommentList error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "removeCommentList", commentParam);
			}
		},
	},

	Chat: {
		/**
		 * Ctrl.Chat.sendChat : 신규 정의함
		 *   - msg : 채팅 메세지 내용
		 *   - targetUserNo : 귓속말인 경우 받는사람 유저번호 (귓속말 아니면 공백값으로..)
		 *   - targetUserNm : 귓속말인 경우 받는사람 유저명 (귓속말 아니면 공백값으로..)
		 *   - chatType : room / class
		 */
		sendChat: function (msg, targetUserNo, targetUserNm, chatType) {
			if (!PacketMgr.isAllowChat && !PacketMgr.isMC) {
				Ctrl.Msg.show(_msg("not.allow"));
				return;
			}

			// 기본 채팅 허용
			if (msg.trim() == "") {
				Ctrl.Msg.show(_msg("insert.msg"));
				return;
			}

			Ctrl.Chat.send(msg, targetUserNo, targetUserNm, chatType);

			if (chatType == "room") {
				PacketMgr.Master.chat(msg, targetUserNo, targetUserNm);
			} else if (chatType == "class") {
				PacketMgr.Master.BroadCast.chat('all', RoomSvr.roomid, msg, targetUserNo, targetUserNm);
			}
		},


		/**
		 * Ctrl.Chat.send : 재정의
		 *  채팅 메세지 보낼 때 보내는 사람 쪽 UI 렌더링
		 *   - chatType : room / class
		 */
		send: function (msgStr, target, targetNm, chatType) {
			console.log("[Ctrl.Chat.send] target : " + target + ", targetNm : " + targetNm + ", chatType : " + chatType);
			var today = new Date();

			if (targetNm == "전체") targetNm = "";
			var dateTime = Utils.getDate(new Date());

			var chatParams = [{
				type: targetNm == "" ? '0' : '1',
				mode: '0',
				sender: PacketMgr.userno,
				receiver: target,
				usernm: targetNm,
				cdatetime: parseInt(dateTime / 100),
				thumbnail: "",
				msg: msgStr
			}];

			console.log("[Ctrl.Chat.send] chatParams : " + JSON.stringify(chatParams));

			// 수신된 채팅 데이터를 Native로 전달함
			if (chatType == "class") {
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. addClassChatData");
				}, function (result) {
					console.log("addClassChatData error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "addClassChatData", chatParams);
			} else if (chatType == "room") {
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. addChatData");
				}, function (result) {
					console.log("addChatData error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "addChatData", chatParams);
			}
		},


		/**
		 * Ctrl.Chat.receive : 재정의함
		 * 채팅 메세지 받았을 때 받는 사람 쪽 UI 렌더링
		 */
		receive: function (packet) {
			console.log("[Ctrl.Chat.receive] packet : " + JSON.stringify(packet));
			console.log(Ctrl.Member.list);
			console.log(Ctrl.Member.classList);

			var name = packet.name;
			var chatid = packet.chatid;
			var time = packet.time;
			var text = packet.text;
			var from = packet.from;
			var to = packet.to;
			var chatType = packet.type;

			var hour = time.substring(8, 10);
			var minute = time.substring(10, 12);
			//var amPmTxt = hour > 11 ? "PM" : "AM";
			//hour = hour % 12;
			//hour = hour ? hour : 12; 	// the hour '0' should be '12'

			var userInfo = (chatType == "class") ? Ctrl.Member.getClassUserOnline(from, "userno") : Ctrl.Member.getUserOnline(from, "userno");
			console.log(userInfo);
			var userNm = userInfo.usernm;
			var thumbnail = userInfo.thumbnail;

			// 귓속말 pass
			if (to != "") {
				if (to == RoomSvr.userno) {
					//userNm = userNm + _msg("noti.private.msg");
				} else {
					return;
				}
			} else {
				// from => packet.from
			}

			// 외부에서 채팅 메시지 보낼수 잇기 때문에 추가 한다. (guest)
			userNm = (userNm == "") ? from : userNm;

			var chatParams = [{
				type: to != RoomSvr.userno ? "0" : "1",
				mode: "1",
				sender: packet.from,
				receiver: packet.to,
				usernm: userNm,
				cdatetime: parseInt(time / 100),
				thumbnail: thumbnail,
				msg: text
			}];


			// 수신된 채팅 데이터를 Native로 전달함
			if (chatType == "class") {
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. addClassChatData");
				}, function (result) {
					console.log("addClassChatData error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "addClassChatData", chatParams);
			} else {
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. addChatData");
				}, function (result) {
					console.log("addChatData error : " + JSON.stringify(result));
				}, "CommunicationPlugin", "addChatData", chatParams);
			}

			Ctrl.Msg.show(userNm + _msg("noti.send.msg"));

			//            var notiTitle = _msg("noti");
			//            var notiContent = "<a href='javascript:Ctrl.Chat.toggle();'>" + userNm + _msg("noti.send.msg") + "</a>";
			//
			//            // 채팅 메뉴가 열려있으면 노티창을 보이게 하지 않는다.
			//            if($("#chat_wrapper").css("display") == "none"){
			//                Ctrl.Noti.show(notiTitle, notiContent);
			//                Ctrl.Chat.chatBadgeCnt++;
			//                $(".btn_chat").addClass("checked");
			//                $("#chat_badge").show();
			//                $("#chat_badge").text(Ctrl.Chat.chatBadgeCnt);
			//            }
		},

		/**
		 * Ctrl.Chat.setEvent (재정의)
		 * 웹에서 참조하는 이벤트 사용하지 않도록 재정의함
		 */
		setEvent: function () {

		}

	},

	Memo: {

		// TODO : 프로퍼티에 정의할 필요..
		rgbList: [
			[255, 245, 145],
			[182, 216, 132],
			[246, 156, 155],
			[106, 203, 221],
			[254, 205, 128],
			[189, 128, 185],
			[153, 153, 153],
			[235, 235, 235]
		],

		/**
		 * Ctrl.Memo.getRgbIndex (신규 정의)
		 */
		getRgbIndex: function (rCode, gCode, bCode) {
			var rgbIndex = -1;
			var len = Ctrl.Memo.rgbList.length;
			for (var i = 0; i < len; i++) {
				var rgb = Ctrl.Memo.rgbList[i];
				if (rCode == rgb[0] && gCode == rgb[1] && bCode == rgb[2]) {
					rgbIndex = i;
				}
			}

			return rgbIndex;
		},


		/**
		 * Ctrl.Memo.renderMemoList (신규 정의)
		 * JSP의 기능을 스크립트로 포팅.. 메모리스트를 읽어와서 html 코드로 렌더링 하는 함수
		 */
		renderMemoList: function (memoList, callback) {
			var len = memoList.length;
			var memoIdx = 0;

			if (len > 0) {
				for (var i = 0; i < len; i++) {
					var memo = memoList[i];

					var typeFlag = memo.TYPEFLAG;
					var seqNo = memo.SEQNO;
					var memoPosX = memo.POSX != undefined ? parseFloat(memo.POSX) : 0;
					var memoPosY = memo.POSY != undefined ? parseFloat(memo.POSY) : 0;

					var red = memo.RED;
					var green = memo.GREEN;
					var blue = memo.BLUE;

					var commentNo = memo.COMMENTNO;
					var content = memo.CONTENT;
					var title = memo.TITLE;
					var fold = memo.PLUGINDATA;
					var foldClassStr = fold == "1" ? "memo_box_mini" : "memo_box"

					var ord = memo.ORD != undefined ? parseInt(memo.ORD) : memoIdx;

					var rgbIndex = Ctrl.Memo.getRgbIndex(red, green, blue);
					var zIndex = ord + 51;

					var readonlyStr = "";
					if (PacketMgr.masterid != CanvasApp.info.userid) {
						readonlyStr = "readonly=readonly";
					}



					var htmlStr =
						"<div id=\"memo" + memoIdx + "\" class=\"memo_container" + (rgbIndex + 1) + " memo\" seqno=\"" + seqNo + "\" memono=\"" + commentNo + "\" ord=\"" + ord + "\" style=\"visibility:hidden; background:rgb(" + red + "," + green + "," + blue + "); left:" + memoPosX + "px; top:" + memoPosY + "px; z-index:" + zIndex + "\">\
							<div class=\"" + foldClassStr + "\">\
			                    <div class=\"mbox_header\">\
							        <div class=\"memo_tit\">\
					                    <a title=\"title.minimize\" class=\"btn_minimize\"></a>\
										<span class=\"title_txt\">" + title + "</span>\
						                <a title=\"title.delete\" class=\"btn_x\"></a>\
					                    <a title=\"title.setup\" class=\"btn_set\"></a>\
									</div>\
			                    <div class=\"mset_box\">\
							        <span>title :</span><input class=\"title_input\" type=\"text\" value=\"" + title + "\" maxlength=\"20\"  />\
				                    <div class=\"memo_colorbox\">\
					                    <a title=\"title.color\" class=\"btn_color1 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color2 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color3 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color4 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color5 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color6 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color7 memo_color\"></a>\
					                    <a title=\"title.color\" class=\"btn_color8 memo_color\"></a>\
				                    </div>\
							    </div>\
		                    </div>\
				            <div class=\"mbox_body\">\
							    <textarea class=\"memo_text\" " + readonlyStr + ">" + content + "</textarea>\
		                    </div>\
			            </div>\
                    </div>";

					++memoIdx;

					$("#memoWrapper").append(htmlStr);
				}
			}

			if (callback != undefined) {
				callback();
			}
		}

	},


	/**
	 * Ctrl.Uploader
	 * 파일 업로드
	 *   - checkImgExt : 이미지 파일 확장자 체크 (재정의)
	 *   - checkPdfExt : PDF 파일 확장자 체크 (재정의)
	 *   - uploadImgInCordova : 이미지 업로드 (validation 체크와 Ctrl.BGImg.init()을 수행..) (신규)
	 *   - uploadPdfInCordova : PDF 업로드 (validation 체크와 PDFViewer.init()을 수행..) (신규)
	 *   - uploadInCordova : 서버에 multipart로 파일 업로드 (신규)
	 *   - download : 파일 다운로드 (재정의)
	 *   - save :  (재정의)
	 */
	Uploader: {


		/**
		 * Ctrl.Uploader.checkImgExt (재정의)
		 * 이미지 업로드 이벤트 세팅
		 **/
		checkImgExt: function (fileName) {
			var ext = fileName.slice(fileName.lastIndexOf(".") + 1).toLowerCase(); //파일 확장자를 잘라내고, 비교를 위해 소문자로 만듭니다.
			return (Ctrl.Uploader.allowList.indexOf(ext) > -1);
		},


		/**
		 * Ctrl.Uploader.checkPdfExt (재정의)
		 * 이미지 업로드 이벤트 세팅
		 **/
		checkPdfExt: function (fileName) {
			var ext = fileName.slice(fileName.lastIndexOf(".") + 1).toLowerCase(); //파일 확장자를 잘라내고, 비교를 위해 소문자로 만듭니다.
			return ext == "pdf";
		},


		cropImageEnable: true,
		/**
		 * Ctrl.Uploader.setImgUploadEvent (신규 정의)
		 * 이미지 업로드 이벤트 세팅
		 **/
		setImageUploadEvent: function () {
			if (!Ctrl._checkAuth(true)) {
				return;
			}

			var platform = CanvasApp.info.platform;
			var version = CanvasApp.info.version;

			console.log("platform : " + platform + ", version : " + version);

			// 안드로이드 킷캣에 대해서만 예외처리 - input file 대신 FileChooser & FileTransfer 사용..
			if (platform.toLowerCase() === 'android') {

				filechooser.open({
					"mime_type": "image/*"
				}, function (data) {
					console.log(data);
					console.log(typeof data)
					var filePath = data.filepath;

					if (Ctrl.Uploader.cropImageEnable) {
						cordova.exec(function (result) {
							console.log("cordova.exec() success.. executeImageCrop");
						}, function (result) {
							console.log("executeImageCrop error : " + JSON.stringify(result));
						}, "CommonPlugin", "executeImageCrop", [data]);
					} else {
						Ctrl.Uploader.uploadImgInCordova(filePath, "file1");
					}
				}, function (e) {
					console.log(e);
				});
			} else {
				$("#file1").click();
			}
		},



		/**
		 * Ctrl.Uploader.uploadImgInCordova (신규 정의)
		 *
		 */
		uploadImgInCordova: function (fileURI, id) {
			// 업로드 직전에 진행 권한 예외처리
			if (!PacketMgr.isMC) {
				Ctrl.Msg.show(_msg("m.auth.msg.1"));
				return;
			}

			Ctrl.Msg.show("Start uploading file");

			Ctrl.Uploader.id = id;

			if (fileURI == null) {
				Ctrl.Msg.show(_msg("file.not.found"));
				return;
			};

			if (!this.checkImgExt(fileURI.substr(fileURI.lastIndexOf("/") + 1))) {
				Ctrl.Msg.show(_msg("check.file.img"));
				return;
			}

			var count = Ctrl.BGImg.getCnt();
			if (count > this.limitCnt) {
				Ctrl.Msg.show(_msg("check.file.count"));
				return;
			}

			this.uploadInCordova(id, fileURI, RoomSvr.roomid, '0', function (data) {
				console.log('배경 이미지 init..');
				console.log(data);
				Ctrl.BGImg.init(data);
			});
		},


		/**
		 * Ctrl.Uploader.setPdfUploadEvent (신규 정의함)
		 * PDF 업로드 이벤트 세팅
		 **/
		setPdfUploadEvent: function () {
			if (!Ctrl._checkAuth(true)) {
				return;
			}

			var platform = CanvasApp.info.platform;
			var version = CanvasApp.info.version;

			if (platform.toLowerCase() === 'android') {
				// Cordova FileChooser 사용..
				filechooser.open({
					"mime_type": "application/pdf"
				}, function (data) {
					console.log(data);

					var filePath = data.filepath;


					var fileContent = atob(data.content.replace(/\s/g, "").replace(/=+$/, ""));
					var byteNumbers = new Array(fileContent.length);
					for (var i = 0; i < fileContent.length; i++) {
						byteNumbers[i] = fileContent.charCodeAt(i);
					}
					var byteContent = new Uint8Array(byteNumbers);

					var blobContent = new Blob([byteContent], {
						type: data.mimetype
					});

					Ctrl.Uploader.uploadPdfInCordova(filePath, blobContent, data.filename, "file_pdf");
				}, function (e) {
					console.log(e);
				});
			} else if (platform.toLowerCase() === 'ios') {
				// TODO: IOS에서 aleart 메세지 띄워주는 플러그인 호출
			} else {
				$("#file_pdf").click();
			}
		},


		/**
		 * Ctrl.Uploader.uploadImgInCordova (신규 정의)
		 *
		 */
		uploadPdfInCordova: function (fileURI, blobContent, fileName, id) {
			// 업로드 직전에 진행 권한 예외처리
			if (!PacketMgr.isMC) {
				Ctrl.Msg.show(_msg("m.auth.msg.1"));
				return;
			}

			Ctrl.Msg.show("Start uploading file");

			Ctrl.Uploader.id_pdf = id;

			if (fileURI == null) {
				Ctrl.Msg.show(_msg("file.not.found"));
				return;
			}

			if (!this.checkPdfExt(fileURI.substr(fileURI.lastIndexOf("/") + 1))) {
				Ctrl.Msg.show(_msg("check.file.pdf"));
				return;
			}

			if (this.progressing || PDFViewer.initializing) {
				Ctrl.Msg.show(_msg("check.file.pdf.init"));
				return;
			}

			this.uploadInCordova(id, fileURI, RoomSvr.roomid, 'p', function (data) {
				console.log('PDF뷰어 init..');
				console.log(data);
				PDFViewer.init(data);
			});
		},



		/**
		 *  Ctrl.Uploader.uploadInCordova : 앱에서 쓰는 파일업로드 함수 (신규 정의)
		 *   - pageid 파라미터 추가
		 */
		uploadInCordova: function (id, fileURI, roomId, typeFlag, callback) {
			var svrFlag = _prop("svr.flag");
			var uploadURL = _prop("svr.host." + svrFlag) + _prop("room.upload.files");

			var encodedURL = encodeURI(uploadURL);

			console.log("upload start.. " + uploadURL);
			console.log("fileURI = " + fileURI);
			console.log("typeFlag = " + typeFlag);

			// 파일 정보 가공 - HTML5 File API
			var reader = new FileReader();
			reader.onloaded = function (e) {
				var fileData = e.target.result;
				console.log(fileData);
				console.log(typeof fileData);
				return fileData;
			};
			reader.onerror = function (e) {
				console.log(e);
			};

			//var uploadFile = reader.readAsBinaryString(fileURI);

			var uploadOpt = new FileUploadOptions();
			uploadOpt.fileKey = id;
			uploadOpt.fileName = fileURI.substr(fileURI.lastIndexOf("/") + 1);
			uploadOpt.mimeType = typeFlag == "p" ? "application/pdf" : "image/jpeg";
			uploadOpt.params = {
				//id : uploadFile,
				'roomid': roomId,
				'typeflag': typeFlag,
				'pageid': UI.current,
				'userid': CanvasApp.info.userid,
				'userno': CanvasApp.info.userno,
				'usernm': CanvasApp.info.usernm,
				'thumbnail': CanvasApp.info.thumbnail
			};

			var ft = new FileTransfer();
			ft.onprogress = function (evt) {
				var percent = 0;
				var position = evt.loaded || evt.position;
				var total = evt.total;
				if (evt.lengthComputable) {
					percent = Math.ceil(position / total * 100);
				}
				// set progress
				// status.setProgress(percent);
				Ctrl.Uploader.progress(percent);

				console.log("upload percent : " + percent);

				/*
				cordova.exec(function(result){
					console.log("success..");
				}, function(result){
					console.log("fail..");
				}, "RoomPlugin", "showUploadProgress", [{'percent' : percent}]);
				*/
			}

			// 업로드 성공 callback
			var onSuccess = function (uploadResult) {
				Ctrl.Msg.show("Uploading file finished.");
				console.log("파일업로드 성공 콜백..");
				console.log(uploadResult);

				if (Utils.browser("msie")) {
					$("#" + id).replaceWith($("#" + id).clone(true));
				} else {
					$("#" + id).val("");
				}

				// var json = $.parseJSON(data);
				var json = JSON.parse(uploadResult.response);
				var result = json.result;

				if (result == 0) {
					console.log('파일 업로드 성공..');
					// Ctrl.BGImg.init(list[0]);
					var list = json.list;
					var len = list == null ? 0 : list.length;
					if (len > 0) {
						callback(list[0]);
					}

				} else if (result == -904) {
					Ctrl.Msg.show(_msg("upload.size.overflow.fail"));

				} else {
					Ctrl.Msg.show(_msg("upload.fail"));
				}
			};

			var onError = function (err) {
				console.log(err);
				Ctrl.Uploader.progressing = false;
			};

			// Cordova의 FileTransfer.upload() 호출..
			ft.upload(fileURI, encodedURL, onSuccess, onError, uploadOpt, false);
		},

		/**
		 * Ctrl.Uploader.upload() 재정의
		 */
		upload: function (e, id, uploadFile, roomId, typeFlag, callback) {
			//var uploadURL = Utils.addContext(_url("upload"));
			var svrFlag = _prop("svr.flag");
			var uploadURL = _prop("svr.host." + svrFlag) + _prop("room.upload.files");

			var formData = new FormData();
			formData.append(id, uploadFile);
			formData.append('roomid', roomId);
			formData.append('typeflag', typeFlag);
			formData.append('userid', CanvasApp.info.userid);
			formData.append('userno', CanvasApp.info.userno);
			formData.append('usernm', CanvasApp.info.usernm);
			formData.append('thumbnail', CanvasApp.info.thumbnail);

			$.ajax({
				xhr: function () {
					var xhrobj = $.ajaxSettings.xhr();
					if (xhrobj.upload) {
						xhrobj.upload.addEventListener('progress', function (event) {
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
				contentType: false,
				dataType: "json",
				processData: false,
				cache: false,
				data: formData,
				success: function (data) {
					// 초기화
					if (Utils.browser("msie")) {
						$("#" + id).replaceWith($("#" + id).clone(true));
					} else {
						$("#" + id).val("");
					}

					// var json = $.parseJSON(data);
					var json = data;
					var result = json.result;
					if (result == 0) {
						// Ctrl.BGImg.init(list[0]);
						var list = json.list;
						var len = list == null ? 0 : list.length;
						if (len > 0) {
							callback(list[0]);
						}

					} else if (result == -904) {
						Ctrl.Msg.show(_msg("upload.size.overflow.fail"));

					} else {
						Ctrl.Msg.show(_msg("upload.fail"));
					}
				},
				fail: function () {
					Ctrl.Uploader.progressing = false;
				}
			});
		},

		/**
		 * Ctrl.Uploader.uploadForiOS : 신규 정의함
		 * IOS 쪽 이미지, PDF 업로드
		 */
		uploadForiOS: function (fileName, type) {
			function fail(err) {
				console.log(err);
			}
			window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);

			function gotFS(fileSystem) {
				fileSystem.root.getDirectory("/tmpDownload", {
					create: false,
					exclusive: false
				}, gotDir, fail);
			}

			function gotDir(dir) {
				var dirReader = dir.createReader();
				dirReader.readEntries(suc, fail);
			}

			function suc(entries) {
				var str = hangul_to_jaso(fileName);
				for (var i = 0; i < entries.length; i++) {
					if (str == entries[i].name) {
						entries[i].file(gotFile);
						break;
					}
				}
			}

			function gotFile(file) {
				if (type)
					Ctrl.Uploader.uploadInCordova("file1", file.localURL, RoomSvr.roomid, '0', Ctrl.BGImg.init);
				else
					Ctrl.Uploader.uploadInCordova("file1", file.localURL, RoomSvr.roomid, 'p', PDFViewer.init);
			}
			// 자음 모음 따로 떼는 함수
			function hangul_to_jaso(text) {
				console.log('한글자소분리');
				//초성(19자) ㄱ ㄲ ㄴ ㄷ ㄸ ㄹ ㅁ ㅂ ㅃ ㅅ ㅆ ㅇ ㅈ ㅉ ㅊ ㅋ ㅌ ㅍ ㅎ
				var ChoSeong = new Array(4352, 4353, 4354, 4355, 4356,
					4357, 4358, 4359, 4360, 4361, 4362, 4363, 4364,
					4365, 4366, 4367, 4368, 4369, 4370);
				//중성(21자) ㅏ ㅐ ㅑ ㅒ ㅓ ㅔ ㅕ ㅖ ㅗ ㅘ(9) ㅙ(10) ㅚ(11) ㅛ ㅜ ㅝ(14) ㅞ(15) ㅟ(16) ㅠ ㅡ ㅢ(19) ㅣ
				var JungSeong = new Array(4449, 4450, 4451, 4452, 4453,
					4454, 4455, 4456, 4457, 4458, 4459, 4460, 4461,
					4462, 4463, 4464, 4465, 4466, 4467, 4468, 4469);
				//종성(28자) <없음> ㄱ ㄲ ㄳ(3) ㄴ ㄵ(5) ㄶ(6) ㄷ ㄹ ㄺ(9) ㄻ(10) ㄼ(11) ㄽ(12) ㄾ(13) ㄿ(14) ㅀ(15) ㅁ ㅂ ㅄ(18) ㅅ ㅆ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ
				var JongSeong = new Array(0x0000, 4520, 4521, 4522, 4523,
					4524, 4525, 4526, 4527, 4528, 4529, 4530, 4531,
					4532, 4533, 4534, 4535, 4536, 4537, 4538, 4539,
					4540, 4541, 4542, 4543, 4544, 4545, 4546);
				var chars = new Array()
				var v = new Array();
				for (var i = 0; i < text.length; i++) {
					chars[i] = text.charCodeAt(i);
					//// "AC00:가" ~ "D7A3:힣" 에 속한 글자면 분해
					if (chars[i] >= 0xAC00 && chars[i] <= 0xD7A3) {
						var i1, i2, i3;

						i3 = chars[i] - 0xAC00;
						i1 = i3 / (21 * 28);
						i3 = i3 % (21 * 28);
						i2 = i3 / 28;
						i3 = i3 % 28;

						v.push(String.fromCharCode(ChoSeong[parseInt(i1)]));
						v.push(String.fromCharCode(JungSeong[parseInt(i2)]));
						// c가 0이 아니면, 즉 받침이 있으면
						if (i3 != 0x0000)
							v.push(String.fromCharCode(JongSeong[parseInt(i3)]));
					} else //한글이 아님
						v.push(String.fromCharCode(chars[i]));
				}
				var return_str = v.join('');
				return return_str;
			}
		},

		download: function (imgData) {
			//console.log('download = ' + imgData);

			// URL 세팅
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("canvas.download");

			var params = {
				data: imgData,
				title: RoomSvr.roomtitle,
				userno: CanvasApp.info.userno
			};

			$("#saveform").attr("action", url);

			$("#title").val(RoomSvr.roomtitle);
			$("#imageData").val(imgData);
			$("#saveFrmUserNo").val(CanvasApp.info.userno);
			$("#saveform").submit();
		},

		/**
		 * Ctrl.Uploader.save (재정의)
		 * 서버에 룸 섬네일을 저장함
		 **/
		save: function (imgData) {
			// URL 세팅
			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("room.save.canvas");

			console.log("[Ctrl.Uploader.save] url : " + url);

			var params = {
				roomid: RoomSvr.roomid,
				pageid: UI.current,
				title: RoomSvr.roomtitle,
				data: imgData,
				userno: CanvasApp.info.userno
			};

			$.ajax({
				type: 'post',
				url: url,
				data: params,
				async: true,
				cache: false,
				dataType: "json",
				//success : onsuccess,
				success: function (ret) {
					console.log("[Ctrl.Uploader.save] canvas/save.json success : " + JSON.stringify(ret));
					PacketMgr.Master.sync("RELOAD_ROOM_THUMB");
				},
				beforeSend: function (xhr) {
					// xhr.setRequestHeader("Cache-Control", "no-cache");
					// xhr.setRequestHeader("X-File-Name", file.fileName);
					// xhr.setRequestHeader("X-File-Size", file.fileSize);
					// xhr.setRequestHeader('Content-Type', 'application/json');
				},
				error: function (e) {
					console.log("error : " + JSON.stringify(e));
				},
				complete: function (ret) {
					// // console.log("complete : " + JSON.stringify(ret));
				},
			});
		},


		/**
		 * Ctrl.Uploader.saveAndDownload (신규 정의)
		 *  - 앱에서 캔버스 화면을 이미지파일로 다운로드할 때 호출하는 함수
		 */
		saveAndDownload: function (imgData) {
			//var url = Utils.addContext(_url("upload.save"));
			// URL 세팅

			var svrFlag = _prop('svr.flag');
			var svrHost = _prop('svr.host.' + svrFlag);
			var url = svrHost + _prop("room.save.canvas");

			console.log("[Ctrl.Uploader.save] url : " + url);

			var params = {
				roomid: RoomSvr.roomid,
				pageid: UI.current,
				title: RoomSvr.roomtitle,
				data: imgData,
				userno: CanvasApp.info.userno
			};

			$.ajax({
				type: 'post',
				url: url,
				data: params,
				async: true,
				cache: false,
				dataType: "json",
				//success : onsuccess,
				success: function (ret) {
					console.log("success : " + JSON.stringify(ret));
					var dnloadUrl = ret.list[0].dnloadurl;

					var fileName = CanvasApp.info.roomtitle + ".png";
					if (cordova.platformId == "ios") {
						cordova.exec(null, null, "RoomPlugin", "saveImage", [dnloadUrl, fileName, "Screenshot has been saved."]);
						return;
					}


					var uri = encodeURI(dnloadUrl);

					var cordovaParams = {
						url: uri,
						roomtitle: RoomSvr.roomtitle
					};

					cordova.exec(function (result) {
						console.log("cordova.exec() success.. saveCanvas");
					}, function (result) {
						console.log("saveCanvas error : " + JSON.stringify(result));
					}, "RoomPlugin", "saveCanvas", [cordovaParams]);


					/*
                    console.log(cordova.file);
                    var localPath = cordova.file.externalDataDirectory;

                    var fileTransfer = new FileTransfer();
                    fileTransfer.download(uri, localPath + fileName, function(entry) {
                        console.log("download complete");
                        console.log(entry);
                        console.log(entry.toURL());
                        cordova.exec(function(result){}, function(result){}, "CommonPlugin", "showToast", ["Screenshot has been saved."]);
                        //showLink(theFile.toURI());
                    }, function(error) {
                        console.log(error);
                        console.log("download error source " + error.source);
                        console.log("download error target " + error.target);
                        console.log("upload error code: " + error.code);
                    });
					*/

				},
				beforeSend: function (xhr) {
					// xhr.setRequestHeader("Cache-Control", "no-cache");
					// xhr.setRequestHeader("X-File-Name", file.fileName);
					// xhr.setRequestHeader("X-File-Size", file.fileSize);
					// xhr.setRequestHeader('Content-Type', 'application/json');
				},
				error: function (e) {
					console.log("error : " + JSON.stringify(e));
				},
				complete: function (ret) {},
			});


		}


	},

	Loader: {
		show: function () {
			// $("#loading").show();
		},
		hide: function () {
			$("#loading").hide();
		},
		toggle: function () {
			$("#loader").toggle();
		},
		showVideo: function () {
			$("#videoLoader").show();
		},
		hideVideo: function () {
			$("#videoLoader").hide();
		},
		toggleVideo: function () {
			$("#videoLoader").toggle();
		}
	},


	/**
	 * Ctrl.Room
	 */
	Room: {

		/**
		 * Ctrl.Room._setEvent (재정의)
		 * 쓰지 않는 이벤트 정의 구문을 삭제하기 위하여 재정의 함.
		 */
		_setEvent: function () {

		},

		/**
		 * Ctrl.Room.update (재정의)
		 * 룸 권한, 비밀번호등의 정보 변경시에 이 함수를 호출..
		 * 룸 타이틀 변경 시에도 이 함수를 호출함..
		 */
		update: function (jsonStr, isShowToast) {
			console.log("[Ctrl.Room.update] isShowToast : " + isShowToast);
			if (PacketMgr.userid != PacketMgr.creatorid) {
				Ctrl.Msg.show(_msg("not.allow"));
				//Ctrl.Msg.auth(true);
				return;
			}

			var jsonParam;
			if (typeof jsonStr == "string") {
				jsonParam = JSON.parse(jsonStr);
			} else if (typeof jsonStr == "object") {
				jsonParam = jsonStr;
			}
			//var jsonParam = JSON.parse(jsonStr);
			console.log("[Ctrl.Room.update] jsonParam : " + JSON.stringify(jsonParam));

			var curRoomInfo = PacketMgr.Master.curRoomInfo;
			var ckAuthType = (jsonParam.authtype != null) ? jsonParam.authtype : (curRoomInfo != null) ? curRoomInfo.authtype : "1"; // 공동 진행 허용 여부
			var ckUsePasswd = (jsonParam.ckpasswd != null) ? jsonParam.ckpasswd : false; // 수업 비밀번호 존재 여부
			var passwdTxt = (jsonParam.passwd != null) ? jsonParam.passwd : (curRoomInfo != null) ? curRoomInfo.passwd : ""; // 수업 비밀번호
			var chatOpt = (jsonParam.chatopt != null) ? jsonParam.chatopt : (curRoomInfo != null) ? curRoomInfo.chatopt : "1"; // 채팅 작성하기 허용 여부
			var cmtOpt = (jsonParam.cmtopt != null) ? jsonParam.cmtopt : (curRoomInfo != null) ? curRoomInfo.cmtopt : "1"; // 코멘트 작성하기 허용 여부
			var expOpt = (jsonParam.expopt != null) ? jsonParam.expopt : (curRoomInfo != null) ? curRoomInfo.expopt : "1"; // 캔버스 화면을 이미지로 저장하기 허용 여부
			var openFlag = (jsonParam.openflag != null) ? jsonParam.openflag : "1"; // 수업 공개 여부
			var content = (jsonParam.content != null) ? jsonParam.content : ""; // 수업 설명
			var userLimitCnt = (jsonParam.userlimitcnt != null) ? jsonParam.userlimitcnt : "3";
			var title = (jsonParam.roomtitle != null) ? jsonParam.roomtitle : RoomSvr.roomtitle;
			var vcamOpt = (jsonParam.vcamopt != null) ? jsonParam.vcamopt : "0";
			var vshareOpt = (jsonParam.vshareopt != null) ? jsonParam.vshareopt : "0";
			var usermaxcnt = (jsonParam.usermaxcnt != null) ? jsonParam.usermaxcnt : "30";

			/*
            var ckAuthType   = jsonParam.authtype;    // 공동 진행 허용 여부
            var ckUsePasswd  = jsonParam.ckUsePasswd; // 수업 비밀번호 존재 여부
            var passwdTxt    = jsonParam.passwd;      // 수업 비밀번호
            var chatOpt      = jsonParam.chatopt;     // 채팅 작성하기 허용 여부
            var cmtOpt       = jsonParam.cmtopt;      // 코멘트 작성하기 허용 여부
            var expOpt       = jsonParam.expopt;      // 캔버스 화면을 이미지로 저장하기 허용 여부
            var openFlag     = jsonParam.openflag;    // 수업 공개 여부
			var content      = jsonParam.content;     // 수업 설명
			var userLimitCnt = jsonParam.userlimitcnt
			var title        = RoomSvr.roomtitle;
            */

			if (typeof (title) != "string") {
				//title = $("#room_title").val();
			}

			/*
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
			console.log(tcount);
			*/

			var tcount = title.length;
			if (tcount > 60) {
				Ctrl.Msg.show(_msg("validation.title"));
				return;
			}

			if (ckUsePasswd && passwdTxt.trim() == "") {
				Ctrl.Msg.show(_msg("insert.passwd"));
				return;
			}

			if (ckUsePasswd && (passwdTxt.length < 4 || passwdTxt.length > 8)) {
				Ctrl.Msg.show(_msg("validation.passwd"));
				return;
			}

			if (ckUsePasswd && !passwdTxt.isEngNum()) {
				Ctrl.Msg.show(_msg("validation.passwd.kor"));
				return;
			}

			if (typeof isShowToast != 'undefined')
				PacketMgr.Master.updateRoomInfo(ckAuthType, chatOpt, cmtOpt, expOpt, title, passwdTxt, openFlag, content, vcamOpt, vshareOpt, isShowToast);
			else
				PacketMgr.Master.updateRoomInfo(ckAuthType, chatOpt, cmtOpt, expOpt, title, passwdTxt, openFlag, content, vcamOpt, vshareOpt);

			var param = [{
				authtype: ckAuthType,
				chatopt: chatOpt,
				cmtopt: cmtOpt,
				expopt: expOpt,
				passwd: passwdTxt,
				openflag: openFlag,
				content: content,
				userlimitcnt: userLimitCnt,
				title: title,
				vcamopt: vcamOpt,
				vshareopt: vshareOpt,
				usermaxcnt: usermaxcnt
			}];

			console.log(JSON.stringify(param));

			if (Object.keys(param).length > 0) {
				// 환경설정 정보를 네이티브로..
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. setRoomAuth");
				}, function (result) {
					console.log("setRoomAuth error : " + JSON.stringify(result));
				}, "RoomPlugin", "setRoomAuth", param);
			}

			//$("#room_title").html(title);
			$("#setup_box").hide();

			// 모달에서 띄운경우
			$("#titleModal").hide();

			/*
			if(typeof isShowToast != 'undefined' && isShowToast)
	            Ctrl.Msg.show(_msg("msg.success.infomation.klounge"));
			*/
		},


		/**
		 * Ctrl.Room.updateRoomInfo (재정의)
		 * 방 정보가 변경되었을 경우 참여자에게 호출되는 함수. PacketMgr.Command.updateroominfo()에서 호출함..
		 */
		updateRoomInfo: function (packet, isShowToast) {
			console.log("[Ctrl.Room.updateRoomInfo] packet : " + JSON.stringify(packet));

			var isChangeVCam = true;
			var isChangeVShare = true;

			PacketMgr.isAllowMaster = (packet.authtype == "1") ? true : false;
			PacketMgr.isAllowChat = (packet.chatopt == "1") ? true : false;
			PacketMgr.isAllowComment = (packet.cmtopt == "1") ? true : false;
			PacketMgr.isAllowExport = (packet.expopt == "1") ? true : false;

			if ((PacketMgr.isOnlyTeacherVCam && packet.vcamopt == "0") || (!PacketMgr.isOnlyTeacherVCam && packet.vcamopt == "1")) {
				isChangeVCam = true;
			}

			if ((PacketMgr.isOnlyTeacherVShare && packet.vshareopt == "0") || (!PacketMgr.isOnlyTeacherVShare && packet.vshareopt == "1")) {
				isChangeVShare = true;
			}

			PacketMgr.isOnlyTeacherVCam = (packet.vcamopt == "1") ? true : false;
			PacketMgr.isOnlyTeacherVShare = (packet.vshareopt == "1") ? true : false;

			Ctrl.Member.authTypeChange(packet.authtype);


			if (isChangeVCam) {
				VideoCtrl.changeOpt();
			}

			if (isChangeVShare) {
				Ctrl.VShare.changeOpt();
			}

			var authParams = {
				cmtopt: packet.cmtopt,
				passwd: packet.passwd,
				passwdflag: (packet.passwd != "") ? true : false,
				authtype: packet.authtype,
				chatopt: packet.chatopt,
				expopt: packet.expopt,
				vcamopt: packet.vcamopt,
				vshareopt: packet.vshareopt
				//userlimitcnt : 3    // TODO : 추후 작업할 것..
			};


			cordova.exec(function (result) {
				console.log("cordova.exec() success.. updateRoomAuth");
			}, function (result) {
				console.log("updateRoomAuth error : " + JSON.stringify(result));
			}, "RoomPlugin", "updateRoomAuth", [authParams]);

			RoomSvr.roomtitle = packet.title;

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. updateRoomTitle");
			}, function (result) {
				console.log("updateRoomTitle error : " + JSON.stringify(result));
			}, "RoomPlugin", "updateRoomTitle", [packet]);

			if (typeof isShowToast != 'undefined' && isShowToast)
				Ctrl.Msg.show(_msg("msg.success.infomation.klounge"));

		},
	},


	/**
	 *  Ctrl.BroadCast : 재정의
	 */
	BroadCast: {
		receive: function (packet) {
			console.log("[Ctrl.BroadCast.receive]");
			var cmd = packet.cmd;
			var type = packet.type;
			var msg = packet.msg;
			var drcode = packet.drcode;
			var history = packet.history;

			// cmd, type, msg, drcode, history
			if (type == "crm01") {

			} else if (type == "crm02") {

			}

			var title = "";

			//Ctrl.Noti.show(title, msg);
			Ctrl.Msg.show(msg); // 방 정보 변경 패킷을 수신했을 때, Toast 안내 메세지 출력
		},


		/**
		 * Ctrl.BroadCast.call : 재정의
		 * 모든 학생 부르기 수신 시에 UI 처리하는 함수
		 **/
		call: function (packet) {
			var drCode = packet.drcode;
			var userNo = packet.userno;
			var userNm = packet.usernm;

			if (parseInt(PacketMgr.code) != parseInt(drCode)) {
				// TODO : 모든 학생 부르기 수신했을 때 UI 처리..
				console.log("[Ctrl.BroadCast.call]");
				console.log(packet);

				var cordovaParams = {
					roomcode: drCode,
					userno: userNo,
					usernm: userNm
				};

				cordova.exec(function (result) {
					console.log("cordova.exec() success.. callAllStudent");
				}, function (result) {
					console.log("callAllStudent error : " + JSON.stringify(result));
				}, "RoomPlugin", "callAllStudent", [cordovaParams]);

			}
		},

		chat: function (packet) {
			//

		}
	},


	/**
	 * Ctrl.Sync : UI 동기화 목적으로 사용 (재정의)
	 *
	 **/
	Sync: {

		/**
		 * Ctrl.Sync.reloadRoomThumb : 프리뷰와 페이지 썸네일을 reload.. (재정의)
		 *
		 **/
		reloadRoomThumb: function () {
			//			console.log("Ctrl.Sync.reloadRoomThumb");
			//Ctrl.Preview.refresh();
			cordova.exec(function (result) {
				//				console.log("cordova.exec() reloadRoomThumb success..");
			}, function (result) {
				console.log("reloadRoomThumb error : " + JSON.stringify(result));
			}, "RoomPlugin", "reloadRoomThumb", []);

		},

		removeLimit: function () {
			//SyncUpdated.removeLimit();   // 웹 방식 주석처리
			cordova.exec(function (result) {
				console.log("cordova.exec() removeRoomUserLimit success..");
			}, function (result) {
				console.log("removeRoomUserLimit error : " + JSON.stringify(result));
			}, "RoomPlugin", "removeRoomUserLimit", []);
		}
	},

	/**
	 * Ctrl.VShare : 재정의
	 **/
	// videoShare
	VShare: {
		template: "",
		list: [],
		limit: 1, // 제한 수
		// 1,2,3,4,5,6
		done: false,
		status: 0, // 0-정지, 1-시작, 2-일시정지
		init: function () {
			console.log("[Ctrl.Vshare.init] ");
			$(".vshare", $("#vShareWrapper")).each(function () {
				var ord = 1;
				var vShare = new VShare(UI.CONTAINER);
				vShare.init($(this).get(0), PacketMgr.isMC, ord);

				Ctrl.VShare.list.push(vShare);
			});

		},


		/**
		 * Ctrl.VShare.add() : 재정의
		 **/
		add: function () {
			var len = this.list.length;
			if (len >= Ctrl.VShare.limit) {
				return;
			}

			var ord = Ctrl.VShare.getMax() + 1;

			var vShare = new VShare(UI.CONTAINER);
			vShare.draw("vShareWrapper", ord, PacketMgr.isMC); // 유튜브 영상 선택하는 UI 렌더링..

			this.list.push(vShare);
		},


		/**
		 * Ctrl.VShare.attachVideo(videoId) : 신규 정의함 / 앱에서 호출하는 유튜브 영상 공유 함수
		 *   - title[String] : 유튜브 비디오 제목
		 *   - videoId[String] : 유튜브 비디오 아이디
		 *
		 **/
		attachVideo: function (titleStr, videoId) {
			//if(!Ctrl._checkAuth(true)) return;   // 권한 체크..

			// 선생님만 영상공유 모드에 대응한 예외처리 코드 추가 - 2016.10.26
			if (PacketMgr.isOnlyTeacherVShare && PacketMgr.usertype != "2" && !PacketMgr.isCreator) {
				Ctrl.Msg.show(_msg("m.auth.msg.1"));
				return;
			}

			console.log("[Ctrl.VShare.attachVideo] titleStr : " + titleStr);
			var title = decodeURIComponent(titleStr);
			console.log("[Ctrl.VShare.attachVideo] title : " + title);

			console.log("[Ctrl.VShare.attachVideo] videoId : " + videoId);
			var len = this.list.length;
			console.log("[Ctrl.VShare.attachVideo] current video count : " + len);

			if (len >= Ctrl.VShare.limit) {
				// 제한된 개수 이상의 영상을 추가하려고 하면 리턴 시킴..
				Ctrl.VShare.removeAndAttachVideo(titleStr, videoId);
				return;
			}

			var ord = Ctrl.VShare.getMax() + 1;

			var vShare = new VShare(UI.CONTAINER);
			console.log("[Ctrl.VShare.attachVideo] create VShare Object..");
			console.log(vShare);
			vShare.drawForApp("vShareWrapper", ord, PacketMgr.isMC, title, videoId);

			this.list.push(vShare);
		},


		/**
		 * Ctrl.VShare.removeAndAttachVideo : 앱에서 호출하는 함수. 영상 공유가 제한된 개수 이상일 때, 기존에 공유된 영상을 삭제하고 새로 공유하려는 영상을 띄워주는 함수 (신규 정의)
		 *   - title[String] : 유튜브 비디오 제목
		 *   - videoId[String] : 유튜브 비디오 아이디
		 **/
		removeAndAttachVideo: function (titleStr, videoId) {
			this.list[0].removeWithCallback(function () {
				Ctrl.VShare.attachVideo(titleStr, videoId);
			});
		},


		/**
		 * Ctrl.VShare.receive() : 재정의 / cmd가 vshare인 패킷을 수신받으면 호출되는 함수.. PacketMgr.Command.vshare()에서 호출함.. 명시적으로 호출할 일 없음
		 *   - packet[String] : JSON String 형태의 packet 데이터
		 **/
		receive: function (packet) {
			//console.log("[Ctrl.VShare.receive] packet : " + packet);
			var type = packet.type || ''; // 0 : add / 1 : update / 2 : remove
			var vsNo = packet.vsno || '';
			var seqNo = packet.seqno || '';
			var title = packet.title || '';
			var content = packet.content || '';
			var left = packet.x || 0;
			var top = packet.y || 0;
			var status = packet.status || '0';
			var time = packet.time || '';
			var ord = packet.ord || Ctrl.VShare.list.length;

			if (status != this.status) {
				this.status = status;
			}

			if (type == '0') {

				var len = Ctrl.VShare.list.length;
				if (len > 0) {
					this.destroy();
				}

				var vShare = new VShare(UI.CONTAINER);
				vShare.receive(type, "vShareWrapper", PacketMgr.isMC, vsNo, seqNo, title, content, left, top, status, 1, time);

				this.list.push(vShare);

			} else if (type == '1' || type == '2') {
				// 삭제
				var removeIdx = -1;
				var len = this.list == null ? 0 : this.list.length;
				for (var i = 0; i < len; i++) {
					var vShare = this.list[i];
					// $(container).attr("memono", json.map.commentno);
					if (vShare.get("vsno") == vsNo) {
						vShare.receive(type, "vShareWrapper", PacketMgr.isMC, vsNo, seqNo, title, content, left, top, status, 1, time);
						removeIdx = i;
						break;
					}
				}

				if (type == '2' && removeIdx > -1) this.list.splice(removeIdx, 1);
			}
		},


		/**
		 * Ctrl.VShare.auth() : 재정의
		 **/
		auth: function () {
			// 마스터 바뀐경우 이벤트 다시 수정
			var len = this.list.length;
			for (var i = 0; i < len; i++) {
				var vShare = this.list[i];
				vShare.changeMC(PacketMgr.isMC);
			}
		},


		/**
		 * Ctrl.VShare.getMax() : 재정의
		 **/
		getMax: function () {
			var bfOrd = -1;
			var len = this.list.length;
			for (var i = 0; i < len; i++) {
				var vShare = this.list[i];
				var ord = vShare.getOrd();
				// console.log("ord : " + ord);
				if (bfOrd < ord) {
					bfOrd = ord;
				}
			}
			return bfOrd;
		},


		seek: function () {

			//
		},


		/**
		 * Ctrl.VShare.onPlayerReady() : 재정의 / Youtube API의 유튜브 플레이어가 Ready 상태일 때 수행할 작업을 정의해 둔 함수
		 **/
		onPlayerReady: function (event) {
			/**
		 	console.log( event.target.getCurrentTime() );
			event.target.playVideo();
			**/
			var player = Ctrl.VShare.list[0].getPlayer();
			if (player) Ctrl.VShare.list[0].setTitle(player.getVideoData().title);
			// setTitle에서 ExCall.draw("0")을 호출함..
		},


		/**
		 * Ctrl.VShare.onPlayerStateChange() : 재정의 / 유튜브 플레이어의 상태(ENDED, PLAYING, PAUSED, BUFFERING, CUED) 가 변했을 때 수행할 작업을 정의해 둔 함수
		 **/
		onPlayerStateChange: function (event) {
			if (PacketMgr.isMC) {
				/**
				YT.PlayerState.ENDED
				YT.PlayerState.PLAYING
				YT.PlayerState.PAUSED
				YT.PlayerState.BUFFERING
				YT.PlayerState.CUED
				**/

				var status = event.target.getPlayerState();
				console.log(event);

				// 재생 전에 유튜브 워터마크를 터치했을 때에 대한 예외처리.. 비디오롤 재생시키도록 제어하였음 - 2016.10.04
				if (event.data == -1) {
					event.target.playVideo();
					return;
				}
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


		/**
		 * Ctrl.VShare.sendMCPlayerStatus() : 재정의
		 **/
		sendMCPlayerStatus: function (newUserNo) {
			if (PacketMgr.isMC && this.list.length > 0) {
				var player = Ctrl.VShare.list[0].getPlayer();
				var status = player.getPlayerState();
				var currentTime = player.getCurrentTime();

				if (status == YT.PlayerState.ENDED) {

				} else if (status == YT.PlayerState.PLAYING) {
					Ctrl.VShare.list[0].changeStatus(status, currentTime, newUserNo);
				} else if (status == YT.PlayerState.PAUSED || status == YT.PlayerState.BUFFERING) {
					if (currentTime > 0) {
						Ctrl.VShare.list[0].changeStatus(status, currentTime, newUserNo);
					}
				}

			}
		},


		/**
		 * Ctrl.VShare.changeOpt : (재정의)
		 **/
		changeOpt: function () {
			console.log("[Ctrl.VShare.changeOpt] PacketMgr.isOnlyTeacherVShare : " + PacketMgr.isOnlyTeacherVShare + ", PacketMgr.usertype : " + PacketMgr.usertype);
			// show
			var len = Ctrl.VShare.list.length;
			for (var i = 0; i < len; i++) {
				var vShare = Ctrl.VShare.list[i];
				if (PacketMgr.isOnlyTeacherVShare && PacketMgr.usertype != "2" && !PacketMgr.isCreator) {
					vShare.hide();
				} else {
					vShare.show();
				}
			}
		},


		/**
		 * Ctrl.VShare.remove() : 재정의
		 **/
		remove: function () {
			Ctrl.VShare.list = null;
			Ctrl.VShare.list = [];
			console.log("[Ctrl.VShare.remove] video count : " + Ctrl.VShare.list.length);
		},


		/**
		 * Ctrl.VShare.destroy() : 재정의
		 **/
		destroy: function () {
			var len = Ctrl.VShare.list.length;
			for (var i = 0; i < len; i++) {
				var vShare = Ctrl.VShare.list[i];

				if (PacketMgr.isMC) {
					vShare.destroy();
				} else {
					vShare.destroyUser();
				}
			}

			Ctrl.VShare.list = [];
			console.log("[Ctrl.VShare.destroy] video count : " + Ctrl.VShare.list.length);
		},


		/**
		 * Ctrl.VShare.renderVShareList() : 신규 정의함 (2016.09.19)
		 *   기존 JSP에서 하던 UI 작업을 스크립트에서 수행하도록 추가한 함수
		 *   이미 가지고 있던 VShareList를 불러와서 UI에 출력해주는 함수
		 *   메모에서 쓰고 있는 renderMemoList와 유사한 역할을 수행함.
		 **/
		renderVShareList: function (vShareList, callback) {
			var len = vShareList.length;
			console.log("[Ctrl.VShare.renderVShareList] len : " + len);

			if (len > 0) {
				for (var i = 0; i < len; i++) {
					var vShare = vShareList[i];

					//console.log("[Ctrl.VShare.renderVShareList] vShare : " + JSON.stringify(vShare));

					var typeFlag = vShare.TYPEFLAG;
					var seqNo = vShare.SEQNO;
					var posX = vShare.POSX != undefined ? parseFloat(vShare.POSX) : 0;
					var posY = vShare.POSY != undefined ? parseFloat(vShare.POSY) : 0;

					var red = vShare.RED;
					var green = vShare.GREEN;
					var blue = vShare.BLUE;

					var commentNo = vShare.COMMENTNO;
					var content = vShare.CONTENT;
					var title = vShare.TITLE;
					var fold = vShare.FOLD;
					var foldClassStr = fold == "1" ? "memo_box_mini" : "memo_box"

					var ord = vShare.ORD != undefined ? parseInt(vShare.ORD) : memoIdx;

					var rgbIndex = Ctrl.Memo.getRgbIndex(red, green, blue);
					var zIndex = ord + 51;

					var videoLink = "https://youtu.be/" + content;

					console.log("[Ctrl.VShare.renderVShareList] posX : " + posX + ", posY : " + posY);

					// 2016.11.01 - video 컨테이너의 visibility 속성을 display 속성으로 변경함 (IOS 웹뷰에서 visibility 속성이 제대로 동작하지 않는 이슈때문에..)
					var htmlStr = "<div class=\"videoImportWrap vshare addVideo\" seqno=\"" + seqNo + "\" vsno=\"" + commentNo + "\" ord=\"" + ord + "\" style=\"display:none; left:" + posX + "px; top:" + posY + "px; z-index:" + zIndex + "\">\
								<div class=\"videoImportTitle\">\
									<span class=\"videoTitleSpan\">" + title + "</span>\
									<a class=\"btn_videoClose\"></a>\
								</div>\
								<div class=\"videoImportURLFirstWrap\" style=\"display:none;\">\
									<div class=\"videoImportURLFirst\">\
										<label for=\"\"><spring:message code=\"videoshare.body\" /></label>\
										<input type=\"text\" class=\"init_link\" value=\"" + videoLink + "\" />\
									</div>\
									<div class=\"videoImportListWrap\">\
										<div class=\"videoImportListTab\">\
											<a class=\"history\"><spring:message code=\"videoshare.history\" /></a>\
											<a class=\"guide on\"><spring:message code=\"videoshare.guide\" /></a>\
										</div>\
										<ul class=\"videoImportList\" style=\"display:none;\"></ul>\
										<ul class=\"videoGuideList\"></ul>\
									</div>\
									<a class=\"btn_videoImport_Import import\"><spring:message code=\"videoshare.import\" /></a>\
								</div>\
								<div id=\"vs_" + content + "\" class=\"vs_player\"></div>\
							</div>";

					$("#vShareWrapper").append(htmlStr);
				}
			}

			if (callback != undefined) {
				callback();
			}

		}
	},


	/**
	 *  Ctrl.Preview : 줌 미리보기 (재정의)
	 *
	 */
	Preview: {
		orgWidth: 200,
		orgHeight: 200,
		orgScaleX: 0,
		orgScaleY: 0,
		init: function () {
			this._setEvent();
		},

		_setOrgSize: function (w, h) {
			if (this.orgScaleX > 0 && this.orgScaleY > 0) return;
			if (w > h) {
				this.orgScaleX = w / 1024;
				this.orgScaleY = h / 748;
			} else {
				this.orgScaleX = w / 768;
				this.orgScaleY = h / 1004;
			}
		},
		_setEvent: function () {
			var delta = 0;
			$(".naviView", "#prevew_navigator").on('mousewheel DOMMouseScroll', function (e) {
				if (PacketMgr.isMC) {
					var E = e.originalEvent;
					delta = (E.detail) ? E.detail * -40 : E.wheelDelta;
					if (delta > 0) {
						Ctrl.zoomIn("0");
					} else {
						Ctrl.zoomOut("0");
					}
				}
				e.preventDefault();
			});

			$(".btn_close", "#prevew_navigator").click(function () {
				// alert("toggle")
				$("#prevew_navigator").toggleClass("fold");
			});

			var guideWidth = this.orgWidth;
			var guideHeight = this.orgHeight;
			var posX = 0;
			var posY = 0;
			var settled = "0";

			$(".navi_nowView", "#prevew_navigator").draggable({
				containment: $(".naviView", "#prevew_navigator"),
				start: function (e) {
					guideWidth = parseInt($(".navi_nowView", "#prevew_navigator").css("width").replace("px"));
					guideHeight = parseInt($(".navi_nowView", "#prevew_navigator").css("height").replace("px"));
				},
				drag: function (e, ui) {
					posX = ui.position.left;
					posY = ui.position.top;
					settled = "0";

					zoomHandle();
				},
				stop: function (e, ui) {
					posX = ui.position.left;
					posY = ui.position.top;
					settled = "1";
					zoomHandle();
				}
			});

			var zoomHandle = function () {
				if (UI.scale == 1) return;

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

				PacketMgr.Master.zoomHandleCenter(UI.scale, settled, fixedX, fixedY, UI.file, UI.current);
			}
		},

		update: function (packet) {
			// 1. scale text update
			var val = Math.floor(packet.scale * 100);
			$(".preview_scale", "#prevew_navigator").html(val + "%");

			// 2. width, height update
			var guideWidth = this.orgWidth * 100 / val;
			var guideHeight = this.orgHeight * 100 / val;

			$(".navi_nowView", "#prevew_navigator").css("width", guideWidth + "px");
			$(".navi_nowView", "#prevew_navigator").css("height", guideHeight + "px");

			var moveX = 0;
			var moveY = 0;

			if (packet.scale > 1) {
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
				var maxX = (w / 2) * (newScale - 1) / newScale;
				var maxY = (h / 2) * (newScale - 1) / newScale;
				var centerX = (this.orgWidth / 2) - (guideWidth / 2);
				var centerY = (this.orgHeight / 2) - (guideHeight / 2);
				var newScaleX = scaleX * this.orgWidth / w;
				var newScaleY = scaleY * this.orgHeight / h;

				var moveX = centerX - newScaleX;
				var moveY = centerY - newScaleY;

				if (moveX < 0) moveX = 0;
				if (moveY < 0) moveY = 0;

			}

			$(".navi_nowView", "#prevew_navigator").css("left", moveX);
			$(".navi_nowView", "#prevew_navigator").css("top", moveY);

			/*
			 * var translateX = scaleX;
    		var translateY = scaleY;
    		$("img", "#prevew_navigator").css('-webkit-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('-moz-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('ms-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('-o-transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)')
					.css('transform', 'scale('+newScale+', '+newScale+') translate('+translateX+'px, '+translateY+'px)');
			*/

		},

		refresh: function () {
			if ($("img", "#prevew_navigator").get(0) != null) {
				var src = $("img", "#prevew_navigator").attr("src") + "?t=" + new Date().getTime();
				$("img", "#prevew_navigator").attr("src", src);
			}
		},

		scroll: function () {

		},

		destroy: function () {
			$(".naviView", "#prevew_navigator").unbind('mousewheel DOMMouseScroll');
			$(".btn_close", "#prevew_navigator").unbind("click");

			try {
				$(".navi_nowView", "#prevew_navigator").draggable("destroy");

			} catch (e) {
				console.log(e);
			}
		}
	},


	/**
	 * Ctrl.init (재정의)
	 *
	 */
	init: function () {
		console.log("Ctrl.init()");
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

		// 웹에서 쓰던 미리보기 캔버스는 init하지 않음..
		//this._drawPrevCanvas(5);
		//this._drawPrevCanvas(6);
		//this._drawPrevCanvas(7);

		Ctrl.Background.init();

		Ctrl.Room.init();

		Ctrl.Comment.init();

		Ctrl.Memo.init();

		Ctrl.Uploader.setProgress();

		Ctrl.Text.init();

		Ctrl.Member.init();

		Ctrl.VShare.init();

	},

	/**
	 * Ctrl.destroy (재정의)
	 *
	 */
	destroy: function () {
		try {
			// destroy ctrl service
			//Ctrl.Comment.destroy();

			//Ctrl.Memo.destroy();

			Ctrl.Background.destroy();

			Ctrl.BGImg.destroy();

			Ctrl.Text.destroy();

			Ctrl.Room.destroy();

			Ctrl.Modal.destroy();

			Ctrl.VShare.destroy();

			// destroy _setRemoteControl

			// destroy _setMenuToggle
			$("#file1").unbind("change");
			$("#invite_btn").unbind("click");

			// destroy _setPenEvent

			// destroy _setSlider

			/**
			 $("#user_wrapper").slideToggle("destroy");
			 $("#setup_box").slideToggle("destroy");
			 $("#bg_box").slideToggle("destroy");
			 $("#chat_wrapper").slideToggle("destroy");
			 **/
		} catch (e) {
			// error exception
			console.log("Ctrl.destroy exception : " + e);
		}

	},


	/**
	 * Ctrl._setEvent (재정의)
	 * CircleMenu 이벤트 정의 부분.. 웹앱에서는 쓰지 않으므로 빈 함수로 재정의하였음.
	 */
	_setEvent: function () {
		this._setRemoteControl();

		this._setMenuToggle();

		this._setDocEvent();

		//this._setPenEvent();

		this._setSlider();

		this._setChatEvent();
	},


	/**
	 * Ctrl._setRemoteControl (재정의)
	 * CircleMenu 이벤트 정의 부분.. 기존 CircleMenu 이벤트가 정의되었던 부분.. 웹앱에서는 쓰지 않으므로 빈 함수로 재정의하였음.
	 *
	 */
	_setRemoteControl: function () {

	},


	/**
	 * Ctrl._setMenuToggle (재정의)
	 * 메뉴 UI에 대한 이벤트 정의
	 * 쓰이지 않는 이벤트 정의를 제하기 위해 재정의 하였음.
	 */
	_setMenuToggle: function () {
		// IE에서는 이미지 파일이 많으면 다운로드 안되므로 주의하자
		/*

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

		 // 하단 배경이미지 버튼의 클릭 이벤트
		 $("#bg_file").click(function(){
		 if(!Ctrl._checkAuth(true)) return;

		 var platform = CanvasApp.info.platform;
		 var version  = CanvasApp.info.version;

		 // 안드로이드 킷캣에 대해서만 예외처리 - input file 대신 FileChooser & FileTransfer 사용..

		 if( platform.toLowerCase() === 'android' && version.indexOf( '4.4' ) === 0 ) {
		 filechooser.open({}, function(data){
		 var filePath = data.filepath;
		 Ctrl.Uploader.uploadImgInCordova(filePath, "file1");
		 }, function(e){
		 console.log(e);
		 });
		 } else {
		 $("#file1").click();
		 }
		 });

		 $("#bg_pdf").click(function(){
		 if(!Ctrl._checkAuth(true)) return;

		 var platform = CanvasApp.info.platform;
		 var version  = CanvasApp.info.version;

		 if( platform.toLowerCase() === 'android' && version.indexOf( '4.4' ) === 0 ) {
		 // Cordova FileChooser 사용..
		 filechooser.open({}, function(data){
		 var filePath = data.filepath;
		 Ctrl.Uploader.uploadPdfInCordova(filePath, "file_pdf");
		 }, function(e){
		 console.log(e);
		 });
		 } else {
		 $("#file_pdf").click();
		 }
		 });

		 */
		// input file 엘리먼트 이벤트에 이미지 업로드 로직 매핑
		$("#file1").change(function (e) {
			console.log("file1 change() fire..");
			if (!Ctrl._checkAuth(true)) {
				return;
			}
			Ctrl.Uploader.uploadImg(e, "file1");
		});

		// input file 엘리먼트 이벤트에 PDF 업로드 로직 매핑
		$("#file_pdf").change(function (e) {
			if (!Ctrl._checkAuth(true)) {
				return;
			}

			Ctrl.Uploader.uploadPdf(e, "file_pdf");
		});


	},


	/**
	 * Ctrl._saveCanvas (재정의)
	 * 캔버스 화면을 이미지파일로 다운로드
	 * 웹앱 방식으로 사용하기 위해 재정의 하였음.
	 */
	_saveCanvas: function () {

		try {
			console.log("[_saveCanvas] fired..");

			// UI.skboards[0].save();   // saveCanvas 생성
			var board = UI.getBoard();
			board.save();

			var saveCanvas = $("#saveCanvas").get(0);
			console.log(saveCanvas);

			if (saveCanvas) {
				// make represent thumbnail
				//Ctrl.Uploader.save(saveCanvas.toDataURL() );
				if (!PacketMgr.isAllowExport && !PacketMgr.isMC) {
					Ctrl.Msg.show(_msg("not.allow"));
					// $(saveCanvas).remove();
					return;
				}
				Ctrl.Uploader.saveAndDownload(saveCanvas.toDataURL());
				$(saveCanvas).remove();

				/*
				 if(!Utils.browser("msie")) {
				 var img = document.createElement("img");
				 img.setAttribute('crossOrigin', 'anonymous');
				 img.src = saveCanvas.toDataURL();

				 img.style.display = "none";
				 document.body.appendChild(img);

				 console.log(img);

				 // var img = document.images[0];
				 img.onload = function() {
				 console.log("img onload fired..");
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

				 console.log(a);

				 a.click();

				 // window.URL.revokeObjectURL(url);

				 $(saveCanvas).remove();
				 $(a).remove();
				 $(img).remove();
				 };

				 } else {
				 Ctrl.Uploader.download(saveCanvas.toDataURL() );
				 $(saveCanvas).remove();
				 }
				 */
			}
		} catch (e) {
			console.log(e);
			var saveCanvas = $("#saveCanvas").get(0);
			if (saveCanvas) {
				$(saveCanvas).remove();
			}
		}
	},

	/**
	 * Ctrl.toggleRC (재정의)
	 * 기존 UI 그리는 부분을 삭제하기 위해 재정의 하였음.
	 *  - suvIdx : 1 - 선, 2 - 사각형, 3 - 원
	 */
	toggleRC: function (penIdx, subIdx, isCheck) {


		// 1. annotation setting
		// 2. toggle header
		// 1, 3, 6, 7, 8, 9
		if (isCheck && !Ctrl._checkAuth(true)) {
			return;
		}

		console.log("toggleRC / penIdx = " + penIdx + ", subIdx = " + subIdx + ", isCheck = " + isCheck);

		if (penIdx > 4) {
			penIdx = penIdx + subIdx - 1;
		}
		this.penIdx = penIdx;

		// background image disable
		this.BGImg.disable();

		PDFViewer.disable();

		//this.toggleHeader();

		if (this.penIdx == 0) {
			Ctrl.isHandMode = true;
			this.toggleHand(isCheck);

		} else if (this.penIdx == 1 || this.penIdx == 2) {
			Ctrl.isHandMode = false;
			this._callPensetting(4);

		} else if (this.penIdx == 4) {
			Ctrl.isHandMode = false;
			this.pointerIdx = subIdx - 1;
			this._drawPrev(4);
			this._callPointer();
			console.log("[toggleRC / laser] this.pointerIdx : " + this.pointerIdx);

		} else {
			Ctrl.isHandMode = false;
			this._callPensetting(this.penIdx + 2);

		}

		this.changeCursor(this.penIdx == 0 ? "1" : "0");

		// text가 존재시 save
		Ctrl.Text.cancel(true);

		Ctrl.Text.toggleEditMode(this.penIdx == 0 ? true : false);
	},

	changeCursor: function (flag) {
		var cursor = (flag == "1") ? "hand" : (flag == "2") ? "text" : (this.penIdx == 1) ? "hpen" : (this.penIdx == 2) ? "hpen" : (this.penIdx == 3) ? "del" : (this.penIdx == 4) ? "pointer" :
			(this.penIdx == 5 || this.penIdx == 6 || this.penIdx == 7) ? "diagram" : "";
		this.Cursor.change(cursor);
		cordova.exec(null, null, "CommonPlugin", "resetMode", []);
	},

	/**
	 * Ctrl.toggleHand (재정의)
	 */
	toggleHand: function (isCheck) {
		if (isCheck && !Ctrl._checkAuth(true)) {
			//cordova.exec(function(result){}, function(result){}, "CommonPlugin", "showToast", [_msg("not.allow")]);
			return;
		}

		console.log("toggleHand / isCheck = " + isCheck);

		Ctrl.BGImg.auth();

		PDFViewer.auth();

		PacketMgr.Master.changeMode("view");
	},


	/**
	 * Ctrl.setRedoUndoEvent  (새로 정의한 함수)
	 * redo, undo
	 */
	setRedoUndoEvent: function (menuNm) {
		var pluginCmd = "";

		if (menuNm == "redo") {
			// 권한 체크..
			if (!Ctrl._checkAuth(true)) {
				return;
			}
			PacketMgr.Master.redo(UI.current);
		} else if (menuNm == "undo") {
			// 권한 체크..
			if (!Ctrl._checkAuth(true)) {
				return;
			}
			PacketMgr.Master.undo(UI.current);
		}
	},

	saveRoomCanvas: function () {
		console.log("[saveRoomCanvas] fired...");
		if (device.platform === "Android") {
			this._saveCanvas();
		} else if (cordova.platformId == "ios") {
			Ctrl.Uploader.download(saveCanvas.toDataURL());
			$(saveCanvas).remove();
			return;
		}
	},


	/**
	 * Ctrl.zoomControl : 신규 정의함
	 **/
	zoomControl: function (val) {
		if (!Ctrl._checkAuth(true)) {
			return;
		}
		Ctrl.zoom(val);
	},


	/**
	 * Ctrl.setZoomVal : 재 정의함
	 **/
	setZoomVal: function (val) {
		$("#zoomval").val(val);

		// 마지막 줌 값을 Native로 전달..

		if (Utils.checkPlatform == "android") {
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. setZoomVal");
			}, function (result) {
				console.log("setZoomVal error : " + JSON.stringify(result));
			}, "RoomPlugin", "setZoomVal", [{
				"zoom": val
			}]);
		}
	},

	/**
	 * Ctrl._drawPrev (재정의함)
	 *   _drawPrevCanvas() 호출부분을 삭제하기 위해 재정의 하였음.
	 */
	_drawPrev: function (penIdx, figureType) {
		figureType = typeof (isFigureLine) == "undefined" ? "0" : figureType;

		var id = "pen_preview_" + penIdx;
		var token = (penIdx == 2 ? "h" : penIdx == 5 ? "l" : penIdx == 6 ? "s" : penIdx == 7 ? "c" : "")
		var lineWidthKey = penIdx == 1 ? "lineWidth" : (penIdx == 2) ? "hLineWidth" : (penIdx == 3) ? "eLineWidth" : (penIdx == 5) ? "lLineWidth" : (penIdx == 6) ? "sLineWidth" : (penIdx == 7) ? "cLineWidth" : "";
		var strokeStyle = penIdx == 1 ? this.strokeStyle : this[token + "StrokeStyle"];
		var fillStyle = penIdx == 6 ? this.sFillStyle : penIdx == 7 ? this.cFillStyle : null;

		var lineWidth = this[lineWidthKey];
		// var alpha = penIdx == 1 ? 100 : this[token + "alpha"];
		var alpha = this[token + "alpha"];

		if (penIdx == 1 || penIdx == 5) {
			var idx = Ctrl.sizeList.indexOf(lineWidth);
			if (idx > -1) {
				$("#" + id).attr("class", "");
				$("#" + id).addClass("value" + (idx + 1));
			}
			console.log("Ctrl._drawPrev / penIdx : " + penIdx + " , strokeStyle : " + strokeStyle);
			r = this.hexToRgb(strokeStyle)["r"];
			console.log("Ctrl._drawPrev / r : " + r);
			g = this.hexToRgb(strokeStyle)["g"];
			console.log("Ctrl._drawPrev / g : " + g);
			b = this.hexToRgb(strokeStyle)["b"];
			console.log("Ctrl._drawPrev / b : " + b);

			var alphaRatio = (alpha * 0.01);

			$("#" + id).css("background", "rgba(" + r + ", " + g + ", " + b + ", " + alphaRatio + ")");

		} else if (penIdx == 3) {
			var idx = Ctrl.eSizeList.indexOf(lineWidth);
			if (idx > -1) {
				$("#" + id).attr("class", "");
				$("#" + id).addClass("value" + (idx + 1));
			}
		} else if (penIdx == 4) {
			// 	var pointerIdx = this.pointerIdx;
			var className = "lp_c" + (this.pColorIdx + 1) + "t" + (this.pointerIdx + 1);

			$("#" + id).attr("class", "");
			$("#" + id).addClass(className);

		} else if (penIdx == 6 || penIdx == 7) {
			// 색깔만 변경
			r = this.hexToRgb(strokeStyle)["r"];
			g = this.hexToRgb(strokeStyle)["g"];
			b = this.hexToRgb(strokeStyle)["b"];

			var fillR = fillStyle != "" ? this.hexToRgb(fillStyle)["r"] : "";
			var fillG = fillStyle != "" ? this.hexToRgb(fillStyle)["g"] : "";
			var fillB = fillStyle != "" ? this.hexToRgb(fillStyle)["b"] : "";
			var alphaRatio = (alpha * 0.01);

			if (fillStyle == "") {
				$("#" + id).css("background", "transparent");
			} else {
				if (figureType == "0") {
					$("#" + id).css("background", "rgba(" + fillR + ", " + fillG + ", " + fillB + ", " + alphaRatio + ")");
				}
			}
			$("#" + id).css("border", "5px solid rgba(" + r + ", " + g + ", " + b + ", " + alphaRatio + ")");
		}

		if (penIdx > 4) {
			//this._drawPrevCanvas(penIdx);
		}
	},


	/**
	 * Ctrl._setPenWidthSlide (재정의)
	 *   펜 너비값 세팅
	 */
	_setPenWidthSlide: function (width) {
		console.log("_setPenWidthSlide : " + width);
		var size = parseInt(width);
		Ctrl.lineWidth = size;
		//Ctrl.changeCursor("0");
		//Ctrl._drawPrev(1);
		Ctrl._callPensetting(4); // 4 : pen,
	},


	/**
	 * Ctrl._setPenAlphaSlide (재정의)
	 *   펜 알파값 세팅
	 */
	_setPenAlphaSlide: function (alpha) {
		Ctrl.alpha = parseInt(alpha);
		//Ctrl.changeCursor("0");
		//Ctrl._drawPrev(1);
		Ctrl._callPensetting(4);
	},


	/**
	 * Ctrl.__setPenColor (재정의)
	 *   드로잉 관련 색상 세팅은 전부 이 함수를 통해서 이루어짐.
	 */
	__setPenColor: function (idx, penIdx, figureType) {
		// 권한체크 필요..
		console.log("[__setPenColor] idx : " + idx + ", penIdx : " + penIdx + ", figureType : " + figureType);
		var colorIdx = idx - 1;
		var colorMap = colorIdx == 4 ? Ctrl.pointColorMap[colorIdx] : Ctrl.colorMap[colorIdx];

		console.log("[__setPenColor] colorMap : " + JSON.stringify(colorMap));

		var r = colorMap.r;
		var g = colorMap.g;
		var b = colorMap.b;

		var code = this.rgbToHex(r, g, b);
		var menuSelect = (penIdx == 1 || penIdx == 2) ? 4 : (penIdx + 2);

		code == "" ? "" : "#" + code;

		if (penIdx == 4) { // 포인터
			console.log("[__setPenColor / Laser] colorIdx : " + colorIdx);

			this.pColorIdx = colorIdx; // set pointer Color

			//__checked(penIdx);

			this._callPointer();

		} else {
			//---- checked
			//__checked(penIdx);

			//----- prev2
			//__pick(penIdx, r, g, b);

			// 1,2,5,6,7 들어옴
			var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
			this[this.penIdx == 1 ? "colorIdx" : token + "ColorIdx"] = colorIdx;

			console.log("[__setPenColor] token : ", token);

			if (penIdx == 6 || penIdx == 7) { // 사각형 혹은 원 일때..
				if (figureType == "2") {
					this[token + "FillStyle"] = code;
					console.log("[__setPenColor] " + token + "FillStyle : ", this[token + "FillStyle"]);

				} else {
					if (figureType == "0") {
						console.log("도형.. penIdx : " + this.penIdx + ", figureType : " + figureType);
						this[token + "FillStyle"] = code;
						console.log("[__setPenColor] " + token + "FillStyle : ", this[token + "FillStyle"]);
					}
					this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = code;
					console.log("[__setPenColor] strokeStyle : ", this["strokeStyle"]);
					console.log("[__setPenColor] " + token + "StrokeStyle : ", this[token + "StrokeStyle"]);
				}
			} else {
				this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = code;
				console.log("[__setPenColor] strokeStyle : ", this["strokeStyle"]);
				console.log("[__setPenColor] " + token + "StrokeStyle : ", this[token + "StrokeStyle"]);
			}

			console.log("[__setPenColor] menuSelect : " + menuSelect);

			this._callPensetting(menuSelect);
		}
	},


	/**
	 * Ctrl._callPointer (재정의)
	 *   레이저 포인터 불러오기
	 */
	_callPointer: function (isSend) {
		isSend = (typeof (isSend) != "undefined") ? isSend : true;

		var colorIdx = (this.penIdx == 1) ? this.colorIdx : (this.penIdx == 2) ? this.hColorIdx : (this.penIdx == 4) ? this.pColorIdx : 0;

		var r = this.pointColorMap[colorIdx]["r"];
		var g = this.pointColorMap[colorIdx]["g"];
		var b = this.pointColorMap[colorIdx]["b"];

		//var type = this.pointerIdx;
		var type = this.pointerIdx;
		console.log("[_callPointer] colorIdx : " + colorIdx);
		console.log("[_callPointer] r : " + r + ", g : " + g + ", b : " + b);
		console.log("[_callPointer] type : " + type);

		// 펜세팅 먼저
		this._callPensetting("6");

		// 그다음 레이저 포인터 등록
		PacketMgr.Master.laserpointer(type, r, g, b, isSend);
	},


	/**
	 *  Ctrl.__setFillClear (재정의)
	 *  도형 배경 삭제
	 *  투명 : idx를 12로..
	 *  idx = 10
	 *  code : #를 뺀 RGB 코드 스트링
	 *
	 */
	__setPenCustomColor: function (idx, penIdx, figureType, code) {
		if (!Ctrl._checkAuth(true)) return;

		var colorIdx = idx - 1;
		var r = this.hexToRgb(code)["r"];
		var g = this.hexToRgb(code)["g"];
		var b = this.hexToRgb(code)["b"];

		var menuSelect = (penIdx == 1 || penIdx == 2) ? 4 : (penIdx + 2);
		// 포인터
		if (penIdx == 4) {
			this.pColorIdx = colorIdx;

			//this.changeCursor("0");

			//this._drawPrev(penIdx);

			this._callPointer();

		} else {

			// 1,2,5,6,7 들어옴
			var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
			this[this.penIdx == 1 ? "colorIdx" : token + "ColorIdx"] = colorIdx;

			if (penIdx == 6 || penIdx == 7) {
				if (figureType == "2") {
					this[token + "FillStyle"] = "#" + code;
				} else {
					if (figureType == "0") {
						this[token + "FillStyle"] = "#" + code;
					}
					this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = "#" + code;
				}
			} else {
				this[this.penIdx == 1 ? "strokeStyle" : token + "StrokeStyle"] = "#" + code;
			}

			//this.changeCursor("0");

			//this._drawPrev(penIdx, figureType);

			this._callPensetting(menuSelect);
		}
	},


	/**
	 *  Ctrl.__setFillClear (재정의함)
	 *  도형 배경을 투명하게 삭제할 때 호출하는 함수..
	 *  투명 : idx를 12로..
	 *   - penIdx : 5 - 선, 6 - 사각형, 7 - 원
	 *
	 */
	__setFillClear: function (idx, penIdx) {
		console.log("[Ctrl.__setFillClear] colorIdx : " + idx + ", penIdx : " + penIdx);
		if (!Ctrl._checkAuth(true)) {
			return;
		}

		var colorIdx = idx - 1;
		var colorMap = Ctrl.colorMap[colorIdx];
		var r = colorMap.r;
		var g = colorMap.g;
		var b = colorMap.b;

		var code = this.rgbToHex(r, g, b);
		var menuSelect = (penIdx == 1 || penIdx == 2) ? 4 : (penIdx + 2);

		// 1,2,5,6,7 들어옴
		var token = (this.penIdx == 2 ? "h" : this.penIdx == 3 ? "e" : this.penIdx == 4 ? "p" : this.penIdx == 5 ? "l" : this.penIdx == 6 ? "s" : this.penIdx == 7 ? "c" : "")
		this[this.penIdx == 1 ? "colorIdx" : token + "ColorIdx"] = colorIdx;

		this[token + "FillStyle"] = "";

		//this.changeCursor("0");

		//this._drawPrev(penIdx, "2");

		this._callPensetting(menuSelect);

	},

	_setEraserWidthSlide: function (width) {
		var size = parseInt(width);
		Ctrl.eLineWidth = size != 1 && size % 2 == 1 ? size + 1 : size;
		Ctrl._drawPrev(3);
		Ctrl._callPensetting(5);
	},


	/**
	 * Ctrl._eraserAllClear (신규 정의)
	 * 웹에서 $("#clear_btn")의 Click 이벤트와 대응되는 함수
	 **/
	_eraserAllClear: function () {
		if (!Ctrl._checkAuth(true)) {
			return;
		}

		PacketMgr.Master.eraserMode(2, UI.current);
		PacketMgr.removePacketPage(UI.current);

	},


	_setShapeWidthSlide: function (shapeIdx, width) {
		console.log("[_setShapeWidthSlide] shapeIdx : " + shapeIdx + ", width : " + width);

		if (shapeIdx == 5) {
			this._setLineWidthSlide(width);
		} else if (shapeIdx == 6) {
			this._setSquareWidthSlide(width);
		} else if (shapeIdx == 7) {
			this._setCircleWidthSlide(width);
		}
	},

	_setShapeAlphaSlide: function (shapeIdx, alpha) {
		console.log("[_setShapeAlphaSlide] shapeIdx : " + shapeIdx + ", alpha : " + alpha);

		if (shapeIdx == 5) {
			this._setLineAlphaSlide(alpha);
		} else if (shapeIdx == 6) {
			this._setSquareAlphaSlide(alpha);
		} else if (shapeIdx == 7) {
			this._setCircleAlphaSlide(alpha);
		}
	},

	_setLineWidthSlide: function (width) {
		Ctrl.lLineWidth = parseInt(width);
		//Ctrl.changeCursor("0");
		Ctrl._drawPrev(5);
		Ctrl._callPensetting(7);
	},

	_setLineAlphaSlide: function (alpha) {
		Ctrl.lalpha = parseInt(alpha);
		//Ctrl.changeCursor("0");
		//Ctrl._drawPrev(5);
		Ctrl._callPensetting(7);
	},

	_setSquareWidthSlide: function (width) {
		Ctrl.sLineWidth = parseInt(width);
		//Ctrl.changeCursor("0");
		//Ctrl._drawPrev(6);
		Ctrl._callPensetting(8);
	},

	_setSquareAlphaSlide: function (alpha) {
		Ctrl.salpha = parseInt(alpha);
		//Ctrl.changeCursor("0");
		//Ctrl._drawPrev(6);
		Ctrl._callPensetting(8);
	},

	_setCircleWidthSlide: function (width) {
		Ctrl.cLineWidth = parseInt(width);
		//Ctrl.changeCursor("0");
		//Ctrl._drawPrev(7);
		Ctrl._callPensetting(9);
	},

	_setCircleAlphaSlide: function (alpha) {
		Ctrl.calpha = parseInt(alpha);
		//Ctrl.changeCursor("0");
		Ctrl._drawPrev(7);
		Ctrl._callPensetting(9);
	},

	/**
	 * Ctrl.setPickersColor (신규)
	 * 커스텀 컬러 지정..
	 *  - regHexCode : "#RRGGBB" 형태의 스트링 데이터
	 *  - menuIdx : 1 - 펜, 5 - 직선, 6 - 사각형, 7 - 원
	 *  - fillIdx : 0 - Fill, 1 - Border
	 */
	setPickersColor: function (rgbHexCode, menuIdx, fillIdx) {
		var colorIdx = 9;
		var rgbCode = rgbHexCode.sreplace("#", "");
		var fillType = "0"
		if (menuIdx == 6 || menuIdx == 7) {
			if (fillIdx == 1) {
				fillType = "1"
			}
		}
		Ctrl.__setPenCustomColor(colorIdx, menu, fillType, rgbCode);
	},

	_setTextAnnotation: function () {
		if (!Ctrl._checkAuth(true)) {
			return;
		}

		//Ctrl.toggleRC(0, -1, false);
		Ctrl.Text.toggle();
	},


	/**
	 * Ctrl.Msg (재정의함)
	 * 메세지 및 alert, confirm 창들을 제어하는 영역
	 **/
	Msg: {

		/**
		 * Ctrl.Msg.show (재정의함)
		 * 토스트 형태의 메세지를 띄움
		 *   - msg : 메세지 내용
		 *   - time : 메세지 노출 시간 (안드로이드에서 사용)
		 **/
		show: function (msg, time) {
			if (typeof time == "undefined" || time == null)
				time = "";

			var params;
			if (cordova.platformId == "android") {
				params = {
					msg: msg,
					time: time
				};
			} else if (cordova.platformId == "ios") {
				params = msg;
			}

			cordova.exec(function (result) {
				console.log("cordova.exec() success.. showToast");
				//if(callback) callback();
			}, function (result) {
				console.log("showToast error : " + JSON.stringify(result));
			}, "CommonPlugin", "showToast", [params]);
			//if(callback) callback();
		},


		auth: function (isForce) {

			var msg = _msg("not.allow");
			Utils.log(isForce + " " + PacketMgr.isGuest + " " + PacketMgr.isAllowMaster);

			if (!isForce && (PacketMgr.isGuest || !PacketMgr.isAllowMaster)) {
				this.show(msg);
				return;
			}

			/*
			 //권한 체크 modal
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
			 }*/

		}
	},


	addMemo: function () {
		if (!Ctrl._checkAuth(true)) {
			Ctrl.Msg.show(_msg("not.allow"));
			return;
		}
		Ctrl.Memo.add();
	},


	/**
	 * Ctrl.setBookmark (신규 정의함)
	 * checked : 기존 체크 여부.. true - 이미 북마크 된 상태, false - 북마크 되지 않은 상태
	 */
	setBookmark: function (checked) {

		if (PacketMgr.isGuest) {
			Ctrl.Msg.show(_msg("comment.add.fail.auth"));
			return;
		}

		var ctx = checked ? _prop("bookmark.remove") : _prop("bookmark.add");

		var svrFlag = _prop('svr.flag');
		var svrHost = _prop('svr.host.' + svrFlag);
		var url = svrHost + "" + ctx;

		var params = {
			roomid: RoomSvr.roomid,
			userno: CanvasApp.info.userno
			//groupno :
		};
		Utils.request(url, "json", params, function (data) {
			if (data.result == "0") {
				console.log(data);
				/*
				 if(checked) {
				 $("#bmCtl").removeClass("btn_star_sel").addClass("btn_star");
				 }else {
				 $("#bmCtl").removeClass("btn_star").addClass("btn_star_sel");
				 }*/
			} else if (data.result == -503) {
				Ctrl.Msg.show(_msg("bookmark.already.added"));
			} else if (data.result == -504) {
				Ctrl.Msg.show(_msg("bookmark.limit.added"));
			}
		});
	},


	/**
	 * Ctrl.exit : 재정의
	 *  - flag:boolean - true (종료), false (나가기)
	 */
	exit: function (flag) {
		PacketMgr.Master.exit(flag);

		Utils.Local.remove("guest");

		if (flag) {
			// 방삭제되는 딜레이시간이 있기 때문에 1초후에 나가게 한다.
			setTimeout(function () {
				CanvasApp.destroy();
				Ctrl.goMainPage();
				//location.href = "index.html";
			}, "1000");

		} else {
			CanvasApp.destroy();
			Ctrl.goMainPage();
			//location.href = "index.html";
		}
	},

	/**
	 *  @deprecated..
	 */
	moveRoom: function (roomCode) {
		PacketMgr.Master.exit(false);
		Utils.Local.remove("guest");
		var roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
		cordova.exec(function (result) {}, function (result) {}, "RoomPlugin", "moveRoom", [roomUrl]);
	},



	/**
	 * Ctrl.goMainPage() : 신규 정의
	 */
	goMainPage: function () {
		cordova.exec(function (result) {}, function (result) {}, "RoomPlugin", "exitRoom", [{}]);
	},

	openRoomTitleModal: function () {
		var type = (PacketMgr.creatorid == PacketMgr.userid) ? "creator" : "";
		Ctrl.Modal.title(type);
	},


	getCurrentPageMode: function () {
		try {
			//			if(Ctrl.Text.active) return "text";
			if ($("#docWrapper").hasClass("mode_text")) return "text";

			return UI.getBoard(UI.current).getMode();


		} catch (e) {
			console.log(e);
			return "view";
		}
	}
};
$.extend(true, Ctrl, CtrlCordova);


/**
 * CanvasApp
 *   - info : 룸과 관련된 정보들을 가지는 자료구조
 *
 *
 */
var CanvasAppCordova = {

	info: null,
	push: null,
	isAnimation: false,
	userCredential: null, // 웹앱에서만 사용하는 값, API 호출할 때 필요한 유저 credential 값.


	/**
	 * CanvasApp.init : 재정의함
	 * 룸 입장 시, 최초 진입점 함수
	 **/
	init: function () {
		console.log("CanvasApp.init()..");
		CanvasApp.setConfig(); // Cordova 플랫폼 여부 설정..
		CanvasApp.bindCordovaEvents();

	},


	/**
	 * CanvasApp.bindCordovaEvents : 신규 정의함
	 * 룸 진입점..
	 **/
	bindCordovaEvents: function () {

		// 안드로이드 전용, 터치 시에 엘리먼트 감지하는 목적의 이벤트 리스너 정의 - 2017.02.28
		if(cordova.platformId == "android") {
			var bodyEl = document.getElementsByTagName("body")[0];
			bodyEl.addEventListener("touchstart", function(e){
				console.log(e.target);
				if (e.target)
					UI.currentTouchTarget = e.target;
				e.preventDefault;
			}, false);
		}	

		document.addEventListener('pause', function () {
			console.log("cordova pause event..");
			return;
		}, false);
		document.addEventListener('deviceready', function () {

			// 팝업창이 열리는 현상을 방지함..
			window.open = function () {
				return;
			};

			console.log("[CanvasApp.bindCordovaEvents] device info : " + JSON.stringify(device));
			console.log("[CanvasApp.bindCordovaEvents] network info");
			console.log(navigator.connection);

			var screenWidth = window.screen.width;
			var screenHeight = window.screen.height;
			console.log("[CanvasApp.bindCordovaEvents] screenWidth : " + screenWidth + ", screenHeight : " + screenHeight);

			// layout은 video/doc 모드중 어떤 화면을 보고 있는지에 대한 값으로, changeMaster와 changeWidhtDraw에서 무조건 날려줘야 한다.

			var codeStr = Utils.parseQueryString(location.search.slice(1)).code;
			console.log("[CanvasApp.bindCordovaEvents] room code : " + codeStr);

			// Native 단에서 유저 정보를 가져오는 Cordova 플러그인 호출부..
			cordova.exec(function (userInfo) {
				console.log("cordova.exec() success.. getUserInfo");
				console.log("[CanvasApp.bindCordovaEvents] userInfo : " + JSON.stringify(userInfo));
				var language = userInfo.locale;
				//var lang = (language != "ko" && language != "ko_kr" && language != "ko-kr") ? "en" : "ko";
				var msgLink = "js/fb/msg/msg_" + language + ".js";
				console.log("[CanvasApp.bindCordovaEvents] msgLink : " + msgLink);

				CanvasApp.userCredential = userInfo.cookie; // 자바스크립트에 유저 쿠키스트링값 저장하기

				$.getScript(msgLink, function () {
					// getMessage 함수 정의..
					MessageResource.getMessage = function (str) {
						if (MessageResource[str]) str = MessageResource[str];
						return str;
					};

					CanvasApp.getRoomInfo(codeStr, device, userInfo);
				});

			}, function (result) {
				console.log("getUserInfo error : " + JSON.stringify(result));
				CanvasApp.getRoomInfo(codeStr, device, null);
			}, "UserPlugin", "getUserInfo", []);

		}, false);
	},


	// 네트워크 상태 변경시 호출되는 이벤트 관리
	setNetworkEvent: function () {
		document.addEventListener("online", function () {
			// 온라인시 처리..
		}, false);
		document.addEventListener("offline", function () {
			// 오프라인시 처리..
		}, false);
	},


	/**
	 * CanvasApp.initSvc : (재정의)
	 *
	 **/
	initSvc: function (isGuest, guestName) {
		console.log("[CanvasApp.initSvc] isGuest : " + isGuest);

		CanvasApp.loadHistory();

		if (isGuest) {
			console.log("[CanvasApp.initSvc] Guest 유저");

			var info = this.info;
			this.info.userid = this.info.deviceid;
			this.info.userno = this.info.deviceid;
			//this.info.usernm = guestName + "(" + _msg("guest") + ")";
			this.info.usernm = guestName;

			Utils.Local.set("guest", guestName); // localStorage에 게스트 정보 저장

			CanvasApp.initInfo(); // roomserver connect

			var guestParam = [{
				guestid: this.info.deviceid,
				guestno: this.info.deviceid,
				//guestnm : guestName + "(" + _msg("guest") + ")",
				guestnm: guestName,
				guestflag: true
			}];

			// 게스트 정보 추가
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. initGuestInfo");
			}, function (result) {
				console.log("initGuestInfo error : " + JSON.stringify(result));
			}, "UserPlugin", "initGuestInfo", guestParam);

			console.log(CanvasApp.info.user);

			// 나머지 참여자들을 리스트에 추가함..
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. addRoomUser");
			}, function (result) {
				console.log("addRoomUser error : " + JSON.stringify(result));
			}, "UserPlugin", "addRoomUser", CanvasApp.info.user);

			// 게스트를 채팅 유저 리스트에 추가함..
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. addChatUserList");
			}, function (result) {
				console.log("addChatUserList error : " + JSON.stringify(result));
			}, "CommunicationPlugin", "addChatUserList", CanvasApp.info.user);



			RoomSvr.newclassuser(info.roomid, info.deviceid, info.usernm, info.deviceid, '1', '', '', '', '0', '', '', '');
		}

		var isCreator = (CanvasApp.info.creatorid == CanvasApp.info.userid) ? true : false;

		var len = CanvasApp.info.user != null ? CanvasApp.info.user.length : 0;
		/*
		if(!Utils.isKLounge() ){
			var currentUserInfo = Ctrl.Member.getUserOnline(info.userno, "userno");
			if(isGuest) currentUserInfo.usernm = guestName;

			VideoCtrl.initMyMedia(CanvasApp.info.roomid, currentUserInfo, PacketMgr.isMC);
		}*/

		console.log("[CanvasApp.initSvc] PacketMgr.isMC : " + PacketMgr.isMC);

		if (PacketMgr.isMC) { // 진행권한이 있을경우 펜세팅으로 시작.
			// 룸 최소 생성시 펜세팅의 약간의 딜레이가 필요하다.
			setTimeout(function () {
				console.log("[CanvasApp.initSvc] 펜세팅 활성화..");
				Ctrl.toggleRC(1, -1, true);
			}, 500);
		}
	},


	/**
	 * CanvasApp.initInfo (재정의)
	 */
	initInfo: function () {
		console.log("[CanvasApp.initInfo]");
		var info = CanvasApp.info;
		// room server control
		RoomSvr.init(info);

		// packet control
		PacketMgr.init(info);

		// memo는 class base라 권한여부를 다시 체크해줘야ㅕ 한다.
		// Ctrl.Memo.auth();

		// 영상 공유
		// Ctrl.VShare.auth();
	},



	/**
	 * CanvasApp.setEvent (재정의)
	 * 앱에서 쓰지 않는 이벤트 구문을 삭제하기 위해 재정의 함.
	 */
	setEvent: function () {

	},

	importScript: function (callback) {
		callback();
	},

	submitPassword: function (callback) {
		var passwdStr = $("#room_password").val();
		var currDate = new Date().getTime(); // 현재시간 밀리세컨드

		if (passwdStr.trim() == "") {
			Ctrl.Msg.show(_msg("m.password.insert"));
			return;
		}

		var rsaModule = $("#rsaModule").val();
		var rsaExponent = $("#rsaExponent").val();

		var rsa = new RSAKey();
		rsa.setPublic(rsaModule, rsaExponent);
		var tokenStr = rsa.encrypt(CanvasApp.info.roomid + "," + passwdStr + ',' + currDate);

		var svrFlag = _prop("svr.flag");
		var svrHost = _prop("svr.host." + svrFlag);
		var url = svrHost + _prop("room.check.passwd");

		//var url = Utils.addContext(_url("check.passwd"));
		var param = {
			// roomid : $("#roomid").val(),
			// roomid : CanvasApp.info.roomid,
			token: tokenStr
		}

		Utils.request(url, "json", param, function (data) {
			Utils.log("success.." + data);
			if (data.result == '0') {
				Ctrl.Modal.hide("passwdModal");
				callback();
			} else {
				Ctrl.Msg.show(_msg("m.password.incorrect"));
			}
		});
	},



	// 룸서버 보안 관련 추가 - 2015.06.19 (author : Min Su)
	/**
	 * CanvasApp.getRoomInfo (재정의)
	 *  - canvas/get.json API에서 수업정보들을 받아온 후 initialize하는 함수.
	 *  - 2016.10.12 : 멀티 페이지에 대응하여 수정됨
	 */
	getRoomInfo: function (codeStr, device, userInfoParams) {
		console.log("[CanvasApp.getRoomInfo] codeStr : " + codeStr);
		console.log("[CanvasApp.getRoomInfo] device : " + JSON.stringify(device));
		console.log("[CanvasApp.getRoomInfo] userInfoParams : " + JSON.stringify(userInfoParams));

		videoMapForiOS = null;
		videoMapForiOS = new Map();

		if (cordova.platformId == "ios")
			device.uuid = device.uuid.replaceAll("-", "");

		var module = $("#rsaModule").val();
		var exponent = $("#rsaExponent").val();
		var uuid = Utils.createUUID().substring(0, 5);

		var rsa = new RSAKey();
		rsa.setPublic(module, exponent);
		var tokenStr = rsa.encrypt(codeStr + ',' + uuid);

		if (tokenStr == null) {
			Ctrl.Msg.show(_msg("msg.exception"), "LONG");
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. forceFinishActivity");
			}, function (result) {
				console.log("forceFinishActivity error : " + JSON.stringify(result));
			}, "RoomPlugin", "forceFinishActivity", []);

			return;
		}

		var info = null;


		// 쿠키에서 유저정보 불러오기
		console.log("[CanvasApp.getRoomInfo] userInfoParams : " + JSON.stringify(userInfoParams));

		/*
			 <쿠키에서 추출하는 유저 정보 샘플 데이터>
			 aip: "112.217.207.82"
			 atime: "20160121113144"
			 email: "thooy@naver.com"
			 groupno: "0"
			 snstype: "0"
			 status: "0"
			 thumbnail: "https://graph.facebook.com/1110029269026734/picture"
			 userid: "1110029269026734"
			 usernm: "김민수"
			 userno: "337fa516450i63e7"
         */

		var userInfoJson = null;
		if (userInfoParams == null || typeof userInfoParams == 'undefined') {
			console.log("[CanvasApp.getRoomInfo] 게스트로 입장한 케이스..");
			userInfoJson = {};
			userInfoJson.userno = "";
			userInfoJson.usernm = "";
			userInfoJson.userid = "";
			userInfoJson.email = "";
			userInfoJson.thumbnail = "";
			userInfoJson.groupno = "";
		} else {
			console.log("[CanvasApp.getRoomInfo] 회원으로 입장한 케이스..");
			userInfoJson = userInfoParams;
		}

		console.log("[CanvasApp.getRoomInfo] userInfoJson : " + JSON.stringify(userInfoJson));

		var svrFlag = _prop('svr.flag');
		var svrHost = _prop('svr.host.' + svrFlag);
		var url = svrHost + _prop("canvas.get");

		var param = {
			token: tokenStr,
			userno: userInfoJson.userno,
			//usernm    : userInfoJson.userid == userInfoJson.userno ? userInfoJson.guest + "(" + _msg("guest") + ")" : userInfoJson.usernm,
			usernm: userInfoJson.userid == userInfoJson.userno ? userInfoJson.guest : userInfoJson.usernm,
			userid: userInfoJson.userid,
			email: userInfoJson.email,
			groupno: userInfoJson.groupno != "" ? userInfoJson.groupno : "0",
			thumbnail: userInfoJson.thumbnail
		};

		console.log("[CanvasApp.getRoomInfo] get.json parameter = " + JSON.stringify(param));

		Utils.request(url, "json", param, function (data) {
			if (data.result == '0') {
				var info = data;

				// 유저 장비 정보를 추가
				info.platform = device.platform;
				info.version = device.version;
				info.deviceid = device.uuid; //ios경우 고려 필요

				console.log("[CanvasApp.getRoomInfo] canvas/get.json result : " + JSON.stringify(info));

				/**
					<샘플 데이터>
					var info = {
						"result": 0,
						"clientip": "112.217.207.82",
						"userid": "118110755280136",
						"lang": "en",
						"plugin": {
							"comment": "0",
							"memo": "0"
						},
						"roomid": "905Fd7b4a9024255a523230D709eb1DB",
						"roomtitle": "김민수투's WENOTE",
						"bookmarkCnt": 0,
						"bg": {
							"bgimg": "",
							"bgred": "",
							"bggreen": "",
							"bgblue": "",
							"coloridx": -1
						},
						"memoList": [],
						"onlineClassList": [],
						"host": "https://fbroomdev.knowlounges.com:9016",
						"bookmark": false,
						"passwdflag": "0",
						"absence": [],
						"usernm": "김민수투",
						"code": "1989",
						"msg": "success",
						"userno": "3424eba71f6k844a",
						"roomthumbnail": "https://dev.knowlounges.com:443/data/fb/room/905/905Fd7b4a9024255a523230D709eb1DB",
						"masterid": "118110755280136",
						"creatorid": "118110755280136",
						"thumbnail": "https://graph.facebook.com/118110755280136/picture",
						"email": "thouuy2@gmail.com",
						"masterseqno": 1989,
						"masterno": "3424eba71f6k844a",
						"masternm": "김민수투",
						"currentpageid" : "DeD379e4",   // 현재 활성화된 페이지의 pageId
						"user": [{
							"thumbnail": "https://graph.facebook.com/118110755280136/picture",
							"email": "thouuy2@gmail.com",
							"status": 0,
							"userid": "118110755280136",
							"guest": 0,
							"usertype": "0",
							"usernm": "김민수투",
							"userno": "3424eba71f6k844a",
							"master": 1,
							"creator": 1,
							"online": 0
						}],
						"auth": {
							"authtype": "1",
							"chatopt": "1",
							"cmtopt": "1",
							"expopt": "1",
							"passwd": "",
							"passwdflag": "0",
							"userlimitcnt": 3,
							"openflag": "0"
						},
						"commentList": [],
						"pageList" : [],        // 멀티 페이지 리스트
						"pageLimit" : 3,        // 멀티 페이지 개수 제한 값
						"platform": "Android",
						"version": "4.4.4",
						"deviceid": "a8d63adf22c5eda8"
					}
				 **/

				info.snstype = userInfoJson.snstype == undefined ? "" : userInfoJson.snstype;
				info.usertype = userInfoJson.usertype == undefined ? "0" : userInfoJson.usertype;
				info.codestr = codeStr;
				CanvasApp.info = info;


				UI.current = info.currentpageid;
				PacketMgr.lastPageId = info.currentpageid;

				CordovaPlugin["RoomPlugin"]["initializeRoom"](CanvasApp.info);
				CordovaPlugin["MultiPagePlugin"]["initPageList"](info.pageList);

				var isGuest = false;
				if (userInfoJson.userid == userInfoJson.userno && userInfoJson.email == "") {
					isGuest = true;

				} else {
					isGuest = false;
				}

				if (!isGuest) {
					CanvasApp.initInfo();
				}


				// 게스트인 유저들은 접미사 추가
				for (var i = 0; i < CanvasApp.info.user.length; i++) {
					var userInfo = CanvasApp.info.user[i];
					if (userInfo.guest == 1) {
						userInfo.usernm = userInfo.usernm + " (" + _msg("guest") + ")";
					}
				}

				// 게스트인 유저들은 접미사 추가
				for (var i = 0; i < CanvasApp.info.onlineClassList.length; i++) {
					var classUserInfo = CanvasApp.info.onlineClassList[i];
					if (classUserInfo.userid == classUserInfo.userno) {
						classUserInfo.usernm = classUserInfo.usernm + " (" + _msg("guest") + ")";
					}
				}


				/*
				var isAnimation = CanvasApp.isAnimation;
				if(isAnimation){
					var maxWidth = parseInt($("#progTxt").width()) - 20;
					Ctrl.ProgressLoader.show(maxWidth);
					// Ctrl.Loader.show();
				}else{
					Ctrl.Loader.show();
				}
				// 히스토리 드로잉 - 비동기로 동작함.. (UI.current : 현재 페이지의 pageId 값)
                PacketMgr.loadHistory(info.roomid, UI.current, function(packetCnt) {

					// 드로잉 히스토리 로딩 옵션 기능 작업 - 2016.06.28
					if(Utils.isKLounge() && packetCnt > PacketMgr.skipCnt) {
						CanvasApp.drawEnd(false);
						setTimeout(function() {
							CanvasApp.confirmDrawPacket();   // 불러올 드로잉 패킷이 많을 경우 confirm 다이얼로그를 띄워 유저에게 로딩 진행 여부를 묻는다..
						}, 1500);

					} else {
						console.log("[CanvasApp.getRoomInfo] drawing not redraw mode");

						// 멀티 페이지에 대응하여 _drawHistoryPacket()에 lastPageId 파라미터 추가 - 2016.10.12
						console.log("[CanvasApp.getRoomInfo] PacketMgr.lastPageId : " + PacketMgr.lastPageId);
						PacketMgr._drawHistoryPacket(PacketMgr.lastPageId, isAnimation, function(){
							CanvasApp.drawEnd(false);
						});
					}
                });
				*/



				// 멀티 페이지 리스트를 html 코드로 렌더링
				UI.Page.renderPageList(info.pageList, function () {
					UI.init(info.currentpageid, info.pageLimit); // 멀티페이지에 대응하여 info.currentpageid 파라미터 추가 - 2016.10.12
				});
				console.log("[CanvasApp.getRoomInfo] UI.current : " + UI.current);


				// canvas control
				Ctrl.Memo.renderMemoList(info.memoList, function () { // 페이지별로 메모리스트가 내려옴
					if (typeof info.vShareList == 'undefined' || info.vShareList == null) {
						Ctrl.init();
					} else {
						Ctrl.VShare.renderVShareList(info.vShareList, function () {
							Ctrl.init();
						});
					}
				});





				//console.log("[CanvasApp.getRoomInfo] Ctrl.Member.list : " + JSON.stringify(Ctrl.Member.list));

				// 내 정보가 리스트에 없을 때 예외처리 추가 - 2016.06.03
				if (Ctrl.Member.getUserOnline(info.userno, "userno") == null) {
					var myInfo = userInfoJson;
					myInfo.creator = info.userid == info.creatorid ? 1 : 0,
						myInfo.master = info.userid == info.masterid ? 1 : 0,
						myInfo.guest = info.userid == info.userno ? 1 : 0,

						myInfo.token = Utils.createUUID().substring(0, 8);

					console.log(myInfo);
					Ctrl.Member.list.push(myInfo);
				}

				// 프로필에서 이름 수정 후 룸 진입 시 이름이 이전 이름으로 표시되는 버그에 대응한 추가 코드 - 2016.06.21
				var len = Ctrl.Member.list == null ? 0 : Ctrl.Member.list.length;
				for (var i = 0; i < len; i++) {
					var userInfo = Ctrl.Member.list[i];
					if (userInfo.userid == info.userid) {
						if (userInfo.usernm != info.usernm) {
							userInfo.usernm = info.usernm;
						}
					}
				}

				// 클래스 유저 리스트를 네이티브로 전달.. - 나를 제외한 나머지 클래스 유저
				var classUserListCordova = CanvasApp.info.onlineClassList;
				cordova.exec(function (result) {
					console.log("cordova exec() success.. addClassUserList")
				}, function (result) {
					console.log("addClassUserList error : " + JSON.stringify(result));
				}, "UserPlugin", "addClassUserList", classUserListCordova);

				// 미 참여자 리스트를 네이티브로 전달..
				cordova.exec(function (result) {
					console.log("cordova exec() success.. addNotAttendee")
				}, function (result) {
					console.log("addNotAttendee error : " + JSON.stringify(result));
				}, "UserPlugin", "addNotAttendee", CanvasApp.info.absence);


				if (isGuest) { // 게스트 유저일 때..
					console.log("[CanvasApp.getRoomInfo] 게스트 입니다.. guestNm : " + userInfoJson.guest);

					var guestName = userInfoJson.guest;
					Utils.Local.set("guest", guestName + " (" + _msg("guest") + ")");
					CanvasApp.initSvc(true, guestName);

				} else {
					console.log("[CanvasApp.getRoomInfo] 게스트가 아닙니다..");
					console.log(userInfoJson);

					CanvasApp.initSvc(false);

					var userList = [];
					var masterName = "";
					var userLen = CanvasApp.info.user.length;
					for (var i = 0; i < userLen; i++) {
						var onlineFlag = CanvasApp.info.user[i].online;
						var masterFlag = CanvasApp.info.user[i].master;
						if (onlineFlag == 1)
							userList.push(CanvasApp.info.user[i])
						if (masterFlag == 1)
							masterName = CanvasApp.info.user[i].usernm;
					}


					// 기존 참여자 리스트를 네이티브로 전달..
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. addRoomUser");
					}, function (result) {
						console.log("addRoomUser error : " + JSON.stringify(result));
					}, "UserPlugin", "addRoomUser", userList);


					// 내 정보를 클래스 유저 리스트에 등록함..
					var myInfoClassUser = {
						userno: userInfoJson.userno,
						userid: userInfoJson.userid,
						usernm: userInfoJson.usernm,
						thumbnail: userInfoJson.thumbnail,
						usertype: userInfoJson.usertype
						//roomid : "",
						//seqno : parseInt(codeStr)
					};

					cordova.exec(function (result) {
						console.log("cordova exec() success.. addClassUserList")
					}, function (result) {
						console.log("addClassUserList error : " + JSON.stringify(result));
					}, "UserPlugin", "addClassUserList", [myInfoClassUser]);



					var commentParam = info.commentList;

					// 댓글 불러와서 등록 (11개씩)
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. initCommentList");
					}, function (result) {
						console.log("initCommentList error : " + JSON.stringify(result));
					}, "CommunicationPlugin", "initCommentList", commentParam);

					var chatUserList = [];
					chatUserList = CanvasApp.info.user;
					for (var i in chatUserList) {
						if (chatUserList[i].userno == userInfoJson.userno) {
							chatUserList.splice(i, 1);
							break;
						}
					}

					// 채팅 유저 리스트를 네이티브로..
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. addChatUserList");
					}, function (result) {
						console.log("addChatUserList error : " + JSON.stringify(result));
					}, "CommunicationPlugin", "addChatUserList", chatUserList);


					// 클래스 채팅 유저 리스트 추가
					cordova.exec(function (result) {
						console.log("cordova.exec() success.. addClassChatUserList");
					}, function (result) {
						console.log("addChatUserList error : " + JSON.stringify(result));
					}, "CommunicationPlugin", "addClassChatUserList", CanvasApp.info.onlineClassList);

				}

				

				console.log("PacketMgr.creatorid : " + PacketMgr.creatorid);
				console.log("PacketMgr.masterid : " + PacketMgr.masterid);
				console.log("PacketMgr.userid : " + PacketMgr.userid);
			} else if (data.result == '-97') {
				Ctrl.Msg.show(_msg("cannot.find.room.klounge"));
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. forceFinishActivity");
				}, function (result) {
					console.log("forceFinishActivity error : " + JSON.stringify(result));
				}, "RoomPlugin", "forceFinishActivity", []);
			}
		});


	},


	/**
	 * CanvasApp.setConfig : 재정의
	 * 플랫폼별 설정 처리
	 *
	 **/
	setConfig: function () {
		// 플랫폼 별로 구분이 필요하면 이곳에 데이터를 설정한다.
		var useCanvasEvent = (cordova.platformId == "android" || cordova.platformId == "ios") ? true : false;
		Utils.setCordova(useCanvasEvent);
	},


	/**
	 * CanvasApp.redrawHistroy : 재정의
	 * 드로잉 히스토리를 로딩하는 함수
	 *
	 **/
	redrawHistory: function () {
		CanvasApp.confirmAction = false;

		var isAnimation = CanvasApp.isAnimation;
		if (isAnimation) {
			var maxWidth = parseInt($("#progTxt").width()) - 20;
			Ctrl.ProgressLoader.show(maxWidth);
		} else {
			// 네이티브에서 룸 로딩 UI 활성화..
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. startRoomLoading");
			}, function (result) {
				console.log("startRoomLoading err  : " + JSON.stringify(result));
			}, "RoomPlugin", "startRoomLoading", [{
				type: "room"
			}]);
		}

		// 네이티브의 히스토리 로딩 Confirm 창 닫기..
		CanvasApp.hideHistoryLoading();

		setTimeout(function () {
			// loading이 ui thread를 가져가므로 settimeout으로 비동기 처리 한다.
			PacketMgr._drawHistoryPacket(PacketMgr.lastPageId, isAnimation, function () {
				CanvasApp.drawEnd(true);
				//$("#loadHistory").hide();
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. finishRoomLoading");
				}, function (result) {
					console.log("finishRoomLoading error : " + JSON.stringify(result));
				}, "RoomPlugin", "finishRoomLoading", []);

				UI.Page.sync();
			});
		}, 100);
	},


	/**
	 * CanvasApp.confirmDrawPacket (재정의)
	 * 히스토리 패킷 개수가 한계치 이상일 경우 모든 패킷을 로딩할 것인지 아닌지를 묻는 UI를 띄워주는 함수임.
	 **/
	confirmDrawPacket: function (from) {
		this.confirmAction = true;

		var cordovaParam = {
			redraw_enable: this.confirmAction
		};

		cordova.exec(function (result) {
			console.log("cordova.exec() success.. setRedrawHistoryMode");
		}, function (result) {
			console.log("setRedrawHistoryMode error : " + JSON.stringify(result));
		}, "RoomPlugin", "setRedrawHistoryMode", [cordovaParam]);


		if (from == "page") {
			setTimeout(function () {
				// 7초후 자동으로 꺼진경우
				UI.Page.sync();
				CanvasApp.confirmAction = false;
			}, "8000"); // 앱에서는 8초로 설정하였음..
		}

	},


	/**
	 * CanvasApp.hideDrawPacketModal (재정의)
	 * 유저가 패킷을 로딩하지 않겠다고 선택했을 때 처리
	 **/
	hideDrawPacketModal: function () {
		// hideDrawPacketModal
		CanvasApp.confirmAction = false;
		UI.Page.sync();
	},


	/**
	 * CanvasApp.hideHistoryLoading (재정의)
	 * 패킷 히스토리 로딩여부를 묻는 confirm창을 닫는 함수
	 **/
	hideHistoryLoading: function () {
		var cordovaParam = {
			redraw_enable: false
		};

		cordova.exec(function (result) {
			console.log("cordova.exec() success.. setRedrawHistoryMode");
		}, function (result) {
			console.log("setRedrawHistoryMode error : " + JSON.stringify(result));
		}, "RoomPlugin", "setRedrawHistoryMode", [cordovaParam]);
	},


	/**
	 * CanvasApp.loadHistory (재정의)
	 *
	 **/
	loadHistory: function () {
		var isAnimation = CanvasApp.isAnimation;
		if (isAnimation) {
			var maxWidth = parseInt($("#progTxt").width()) - 20;
			Ctrl.ProgressLoader.show(maxWidth);
			// Ctrl.Loader.show();
		} else {
			Ctrl.Loader.show();
		}

		console.log("[CanvasApp.loadHistory] info.roomid : " + CanvasApp.info.roomid + ", UI.current : " + UI.current);

		// 히스토리 드로잉 - 비동기로 동작함.. (UI.current : 현재 페이지의 pageId 값)
		PacketMgr.loadHistory(CanvasApp.info.roomid, UI.current, function (packetCnt) {

			// 드로잉 히스토리 로딩 옵션 기능 작업 - 2016.06.28
			if (Utils.isKLounge() && packetCnt > PacketMgr.skipCnt) {
				CanvasApp.drawEnd(false);
				CanvasApp.confirmDrawPacket("room"); // 불러올 드로잉 패킷이 많을 경우 confirm 다이얼로그를 띄워 유저에게 로딩 진행 여부를 묻는다..

			} else {
				console.log("[CanvasApp.getRoomInfo] drawing not redraw mode");

				// 멀티 페이지에 대응하여 _drawHistoryPacket()에 lastPageId 파라미터 추가 - 2016.10.12
				console.log("[CanvasApp.getRoomInfo] PacketMgr.lastPageId : " + PacketMgr.lastPageId);
				Utils.runCallback(function () {
					PacketMgr._drawHistoryPacket(PacketMgr.lastPageId, isAnimation, function () {
						CanvasApp.drawEnd(false);
					});
				}, 100);
			}
		});
	},

	/**
	 * CanvasApp.drawEnd : 재정의
	 * 룸 패킷의 로딩이 완료되었을 때 UI 처리를 위해 호출하는 함수.
	 *  - 2016.10.19 : 멀티페이지 업데이트에 대응하여 수정됨 (PacketMgr.lastPageId값을 사용함..)
	 **/
	drawEnd: function (isRedraw, loaderHide) {
		var isAnimation = CanvasApp.isAnimation;
		// 마지막 보고있는 페이지의 zoom 확인 (마스터가 들어온 경우만 사용)
		PacketMgr._drawLastZoomPacket(PacketMgr.lastPageId);

		// 팬세팅 맞춤
		if (!PacketMgr.isMC) {
			// 마스터가 아닌경우 펜세팅 싱크 맞춰줘야 한다.
			// PacketMgr.Master.syncPensettingForce( PacketMgr._getLastPen() );
			PacketMgr.pushQueue(PacketMgr._getLastPen());
		}

		// 새로 그린게 손모드이면 view모드로 change해줘야 한다.
		if (!isRedraw || Ctrl.isHand()) {
			PacketMgr.Master.changeMode("view");
		}

		// queue shift
		PacketMgr.shiftQueue();

		if (!isAnimation) {
			//Ctrl.Loader.hide();
			//Ctrl.ProgressLoader.hide();

			setTimeout(function () {
				// 앱 전용 룸 로딩 UI 닫기
				cordova.exec(function (result) {
					console.log("cordova.exec() success.. finishRoomLoading");
				}, function (result) {
					console.log("finishRoomLoading error : " + JSON.stringify(result));
				}, "RoomPlugin", "finishRoomLoading", []);
			}, 1000);
		}
	},

	destroy: function () {
		// ie는 버그있어서 destroy를 예외처리 해준다.
		if (!Utils.browser("msie")) {
			VideoCtrl.destroyAll();
		}

		// IE는 이곳에서 destroy 해야 한다.
		RoomSvr.destroy();

		PacketMgr.destroy()

		UI.destroy();

		Ctrl.destroy();

		$("#guide").unbind("click");
	}
};

$.extend(true, CanvasApp, CanvasAppCordova);


var RoomSvrCordova = {


	/**
	 * RoomSvr.init (재정의)
	 *  RoomSvr 초기화 함수
	 */
	init: function (options) {
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

		this.roomthumbnail = options.roomthumbnail || '';

		//        if(this.userid == this.userno){
		//            this.thumbnail = Utils.addContext(_url("profile.default"));
		//        }

		if (this.host == '') {
			Ctrl.Msg.show(_msg("cannot.find.room"));
			return;
		}

		var isSecure = this.host.indexOf("https") > -1 ? true : false;
		isSecure = true;
		this.socket = io.connect(this.host, {
			secure: isSecure
		});

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

		// 모바일은 isKLounge를 무조건 true로 놓고 쓸것.
		//        if(Utils.isKLounge) {
		if (true) {
			this.socket.on("newclassuser", this.newclassuser);
			this.socket.on("leaveclassuser", this.leaveclassuser);
		}
	},

	disconnect: function () {
		console.log("[RoomSvr.disconnect]");
		// Ctrl.Member.setOnline("disconnect", false, true);
		Ctrl.setMyNetworkStatus(false, true);

	},

	sendbroadcast: function (mode, roomId, data) {
		console.log("[RoomSvr.sendbroadcast] mode : " + mode + ", roomId : " + roomId + ", data : " + data);

		if (PacketMgr.isKicked) return;

		// 상대방 방에 브로드 케스팅을 보내는 테스트
		if (typeof (data) != "undefined" && data != null && data != "") {
			// 룸서버로 보낼때는 data는 json string 형태로 보내야 한다.
			this.socket.emit("sendbroadcast", mode, roomId, data);
		}
	},


	/**
	 * RoomSvr.newuser (재정의)
	 *  새로운 유저가 룸에 입장했을 때 호출되는 콜백함수. 나 자신에 대해서도 newuser 콜백이 호출됨.
	 *  참여자 목록에 대한 presence처리는 이 콜백함수에서 처리함.
	 *
	 *   - userid       : 유저 아이디
	 *   - username     : 유저 이름
	 *   - userno       : 유저 번호
	 *   - guest        : 게스트 유저 여부
	 *   - thumbnail    : 유저 썸네일 URL
	 *   - userType     : 유저 타입  (0 - 일반, 1 - 학생, 2 - 선생님)
	 *   - isvideoallow : 나의 비디오 허용여부 (0 - 비허용, 1 - 허용)
	 */
	newuser: function (userid, username, userno, guest, thumbnail, usertype, isvideoallow, fullscreenuserid) {
		console.log("[RoomSvr.newuser] Socket.IO - userid : " + userid + ", username : " + username + ", userno : " + userno + ", guest : " + guest + ", usertype : " + usertype);
		console.log("[RoomSvr.newuser] Socket.IO - isvideoallow : " + isvideoallow);
		console.log("[RoomSvr.newuser] Socket.IO - Ctrl.penIdx : " + Ctrl.penIdx);

		/**
		 * 내정보만 getdata로 보내야 한다.
		 * 기존 마스터의 펜세팅과 권한 정보를 브로드캐스팅으로 받아오기 위한 목적으로 getdata 패킷을 보냄..
		 * [Update 2016.06.13] : 앱 쪽에서 ended 패킷이 보내지지 않는 버그가 있어서 주석처리 함.
		 **/
		/*
        if(RoomSvr.userid == userid){
            this.emit("sendmsg", '{"cmd":"getdata","userid":"' + RoomSvr.userid+ '","usernm":"' + RoomSvr.usernm + '"}');
        }
		*/


		var newUserInfo = Ctrl.Member.get(userno, "userno");
		if (RoomSvr.userno != userno && newUserInfo != null) {
			return;
		}

		if (usertype == null) {
			usertype = "0"; // 게스트는 userType이 null로 오는데 이 경우에 대한 값을 세팅해 줌..
		}

		// 마스터가 접속한 경우 마지막 파일하고 페이지 broadcast 한다.
		//Ctrl.Member.newUser(userid, username, userno, guest, thumbnail);
		Ctrl.Member.newUser(userid, username, userno, guest, thumbnail, usertype, isvideoallow, fullscreenuserid);
	},


	/**
	 * RoomSvr.leaveuser (재정의)
	 *  유저가 룸에서 퇴장했을 때 호출되는 함수
	 *  참여자 목록에 대한 presence처리는 이 콜백함수에서 처리함.
	 */
	leaveuser: function (userId, userName, userNo) {
		console.log("[RoomSvr.leaveuser] userName : " + userName);
		var leaveUserInfo = Ctrl.Member.get(userNo, "userno");
		if (leaveUserInfo == null) {
			return;
		}

		//Ctrl.Member.leaveUser(userId, userName, userNo);
		Ctrl.Member.removeRoomUser(userId, userName, userNo);

		VideoCtrl.destroy(userNo, userId);
		if (cordova.platformId == "android") {
			var cordovaParams = {
				userid: userId,
				reconnect: false
			};
			cordova.exec(function (result) {
				console.log("cordova.exec() success.. disconnectPeer");
			}, function (result) {
				console.log("disconnectPeer error : " + JSON.stringify(result));
			}, "VideoPlugin", "disconnectPeer", [cordovaParams]);
		}

	},


	/**
	 * RoomSvr.newclassuser (재정의)
	 *  새로운 유저가 수업에 입장했을 때 호출되는 콜백함수. newuser와 달리 나 자신에 대해서는 호출하지 않음.
	 *   클래스 유저리스트에 대한 presence처리는 이 콜백함수에서 처리
	 *
	 *   - roomid        : 룸 아이디
	 *   - userid        : 유저 아이디
	 *   - username      : 유저 이름
	 *   - userno        : 유저 번호
	 *   - isGuest       : 게스트 여부
	 *   - thumbnail     : 유저 썸네일
	 *   - userRoomId    :
	 *   - userRoomSeqNo :
	 *   - userType      : 유저 타입  (0 - 일반, 1 - 학생, 2 - 선생님)
	 */
	newclassuser: function (roomId, userId, userName, userNo, isGuest, thumbnail, userRoomId, userRoomSeqNo, userType, roomName, roomCreatorName, roomSeparate) {
		console.log("[RoomSvr.newclassuser] Socket.IO - roomId : " + roomId + ", userId : " + userId + ", userName : " + userName + ", userRoomId : " + userRoomId + ", userRoomSeqNo : " + userRoomSeqNo);
		console.log("[RoomSvr.newclassuser] Socket.IO - isGuest : " + isGuest);
		console.log("[RoomSvr.newclassuser] Socket.IO - userType : " + userType);
		console.log("[RoomSvr.newclassuser] Socket.IO - thumbnail : " + thumbnail);
		thumbnail = isGuest == "1" ? "" : thumbnail;
		userName = isGuest == "1" ? userName + "(" + _msg("guest") + ")" : userName;

		Ctrl.Member.addClassUser(roomId, userId, userName, userNo, isGuest, thumbnail, userRoomId, userRoomSeqNo, userType, roomName, roomCreatorName, roomSeparate);
		
	},


	/**
	 * RoomSvr.leaveclassuser (재정의)
	 *   클래스 단위에서 유저가 퇴장시에 호출되는 콜백함수
	 *   클래스 유저리스트에 대한 presence처리는 이 콜백함수에서 처리
	 */
	leaveclassuser: function (roomId, userId, userName, userNo) {
		console.log("[RoomSvr.leaveclassuser] Socket.IO - " + roomId + ", " + userId + ", " + userName + ", " + userNo);
		//if(Utils.isKLounge) {
		if (true) {
			Ctrl.Member.leaveClassUser(roomId, userId, userName, userNo);
		}
	},


	/**
	 * RoomSvr.kickuser (재정의)
	 *   같은 룸에 중복 접속시 기존 유저를 강제퇴장 시킴..
	 *   룸서버에서 내려주는 패킷을 받게되면 호출됨..
	 *
	 *  - roomid   : 룸 아이디
	 *  - userno   : 퇴장당할 유저의 유저번호
	 *  - userid   : 퇴장당할 유저의 유저아이디
	 *  - username : 퇴장당할 유저의 유저명
	 *  - kicktype : 강제퇴장 유형 (0 - 룸에 동일한 유저 중복 접속, 1 - 개설자에 의한 강퇴처리)
	 */
	kickuser: function (roomid, userno, userid, username, kicktype) {
		console.log("[RoomSvr.kickuser] roomid : " + roomid + ", userid : " + userid + ", username : " + username + ", kicktype : " + kicktype);

		if (RoomSvr.roomid == roomid) {
			if (PacketMgr.userid == userid) {
				console.log("[RoomSvr.kickuser]");
				PacketMgr.isKicked = true;
				//Ctrl.Loader.showF();

				var cordovaParams;

				if (kicktype == null || kicktype == '0') {
					cordovaParams = {
						type: "3",
						title: "Alert",
						msg: _msg("msg.connect.another.device")
					};
				} else if (kicktype == '1') {
					if (PacketMgr.isGuest) {
						Utils.Local.remove("guest");
					}
					// 화면에서 지운다.
					RoomSvr.leaveuser(userid, username, userno);
					cordovaParams = {};
				}

				CanvasApp.destroy();

				cordova.exec(function (result) {
					console.log("cordova.exec() success.. exitRoom");
					console.log(result);
				}, function (result) {
					console.log("exitRoom error : " + JSON.stringify(result));
				}, "RoomPlugin", "exitRoom", [cordovaParams]);


			} else {
				console.log("[RoomSvr.kickuser]", userid, username, userno);
				RoomSvr.leaveuser(userid, username, userno); // socket이 붙어있으면 leave시키지만 안붙어 있는 경우를 대비해서 leave시킨다.
			}
		}
	},


	/**
     * web rtc initialize connection pair
     * RoomSvr.newvc : Remote 유저의 비디오 처리를 위한 콜백함수. (재정의 함)
	 * 상대방 영상에 대한 처리는 newvc로부터 시작된다.
 	 * 콜백함수에서 내려주는 정보는 PeerConnection 단위로 1 pair씩 내려준다.
	 *
     *	- roomId : 룸 아이디
        - user1  : 새로 들어온 유저의 userNo
	    - user2  : 이미 접속중인 유저의 userNo
	    - data   : turn 서버 정보
            data = {
                id1 : [{
                        urls : "stun:27.122.249.46:10076"
                    }, {
                        credential : "768ccafc-13b6-456c-8ab5-75702f6d9274",
                        urls : "turn:27.122.249.46:10076",
                        username : "3c279b4c-2a45-4512-a477-061936f70989:334ed6c86bcg939e"
                }],
                id2 : [{
                        urls : "stun:27.122.249.46:10077"
					}, {
                        credential : "768ccafc-13b6-456c-8ab5-75702f6d9274",
                        urls : "turn:27.122.249.46:10077",
                        username : "3c279b4c-2a45-4512-a477-061936f70989:337fa516450i63e7"
                }]
            };
	 *   - user1_video_allow : 나의 영상 허용여부
	 *   - user2_video_allow : 상대방의 영상 허용여부
     */
	newvc: function (roomId, user1, user2, data, user1_video_allow, user2_video_allow, user1_mic_status, user2_mic_status) {
		console.log("[RoomSvr.newvc / WebRTC] enter..!!");
		//console.log(data.id1);
		//console.log(data.id2);

		if (user1 == user2) return;

		var param = {
			offer: user2,
			answer: user1,
			offerTurn: data.id2,
			answerTurn: data.id1
		};

		/***
		 * @description 신규로 들어오는 사람은 무조건 Answer이고, 기존에 영상으로 접속한 사람이 offer를 보내준다.
		 */
		var isDisplay = true;
		var mode = RoomSvr.userno == user1 ? "answer" : RoomSvr.userno == user2 ? "offer" : "";
		var isDisplay = ((mode == "offer" && user1_video_allow == 0) || (mode == "answer" && user2_video_allow == 0)) ? false : true;
		var micStatus = mode == "offer" ? user1_mic_status : user2_mic_status;

		if (mode == "offer") {
			console.log("[RoomSvr.newvc / WebRTC] Answer 유저(user2) : " + user1 + ", Offer 유저(user1) : " + user2);
			console.log("[RoomSvr.newvc / WebRTC] Answer 유저(user2) Video allow : " + user2_video_allow + ", Offer 유저(user1) Video allow : " + user1_video_allow);
			//isDisplay = user1_video_allow == 0 ? false : true;
		} else if (mode == "answer") {
			console.log("[RoomSvr.newvc / WebRTC] Answer 유저(user1) : " + user1 + ", Offer 유저(user2) : " + user2);
			console.log("[RoomSvr.newvc / WebRTC] Answer 유저(user1) Video allow : " + user1_video_allow + ", Offer 유저(user2) Video allow : " + user2_video_allow);
			//isDisplay = user2_video_allow == 0 ? false : true;
		}

		console.log("[RoomSvr.newvc] mode : " + mode + ", isDisplay : " + isDisplay);

		if (mode != "") {
			var targetUserNo = (mode == "answer") ? user2 : user1;
			// 상대 유저가 마스터 인지 여부
			var isMC = PacketMgr.masterno == targetUserNo ? true : false;
			// VideoCtrl.initPeerMedia(mode, RoomSvr.roomid, param, isMC, isDisplay, micStatus);
		}
	},


	/**
     * RoomSvr.newvcf : 영상 제한 제어 처리를 위한 콜백 (재정의 함)
	 * 이미 접속중인 유저에 대해 영상 제한이 해제되는 이벤트가 발생하면 이 콜백함수가 호출된다.
	 * 콜백함수에서 내려주는 정보는 PeerConnection 단위로 1 pair씩 내려준다.
	 *
     *	- roomId : 룸 아이디
        - user1  : 새로 들어온 유저의 userNo
	    - user2  : 이미 접속중인 유저의 userNo
	    - data   : turn 서버 정보
            data = {
                id1 : [{
                        urls : "stun:27.122.249.46:10076"
                    }, {
                        credential : "768ccafc-13b6-456c-8ab5-75702f6d9274",
                        urls : "turn:27.122.249.46:10076",
                        username : "3c279b4c-2a45-4512-a477-061936f70989:334ed6c86bcg939e"
                }],
                id2 : [{
                        urls : "stun:27.122.249.46:10077"
					}, {
                        credential : "768ccafc-13b6-456c-8ab5-75702f6d9274",
                        urls : "turn:27.122.249.46:10077",
                        username : "3c279b4c-2a45-4512-a477-061936f70989:337fa516450i63e7"
                }]
            };
	 *   - user1_video_allow : 나의 영상 허용여부
	 *   - user2_video_allow : 상대방의 영상 허용여부
     */
	newvcf: function (roomId, user1, user2, data, user1_video_allow, user2_video_allow, user1_mic_status, user2_mic_status) {
		console.log("[RoomSvr.newvcf / WebRTC] user1 : " + user1 + ", user2 : " + user2);
		console.log('[RoomSvr.newvcf / WebRTC] user1 : ' + user1 + ', user1_video_allow : ' + user1_video_allow);
		console.log('[RoomSvr.newvcf / WebRTC] user2 : ' + user2 + ', user2_video_allow : ' + user2_video_allow);
		// 1. user가 없는 유저면 pass 시킨다.
		var param = {
			offer: user2,
			answer: user1,
			offerTurn: data.id2,
			answerTurn: data.id1
		};

		/***
		 * @description 신규로 들어오는 사람은 무조건 Answer이고, 기존에 영상으로 접속한 사람이 offer를 보내준다.
		 */
		var mode = RoomSvr.userno == user1 ? "answer" : RoomSvr.userno == user2 ? "offer" : "";
		//		var isDisplay = (mode == "answer") ? (user1_video_allow == 1 ? true : false)  :  (user2_video_allow == 1 ? true : false);
		var isDisplay = ((mode == "offer" && user1_video_allow == 0) || (mode == "answer" && user2_video_allow == 0)) ? false : true;

		if (mode == "answer") {
			isDisplay = user1_video_allow == 0 ? false : true;
			var data = VideoCtrl.map.get(RoomSvr.userno);
			//if(cordova.platformId == "android" && data) data.show(!data.isDisplay);
		}

		var targetUserNo = (mode == "answer") ? user2 : user1;
		var isMC = PacketMgr.masterno == targetUserNo ? true : false;

		// VideoCtrl.initPeerMedia(mode, RoomSvr.roomid, param, isMC, isDisplay);
	},


	/**
	 *  WebRTC 비디오 추가 - Written by Minsu (2015.04.14)
	 */
	streamvideo: function (data) {

		if (Utils.browser("msie")) return;
		//console.log("socket.io로부터 streamvideo 패킷을 받았습니다.");
		//console.log(data);
		//console.log(JSON.stringify(data.msg));

		VideoCtrl.receiveSignal(data);
	},


	/**
	 *  WebRTC 관련 패킷 전송을 위한 function
	 */
	sendWebRtcData: function (data) {
		this.socket.emit('streamvideo', data);
	},

}

$.extend(true, RoomSvr, RoomSvrCordova);


var CordovaPlugin = {
	RoomPlugin: {
		initializeRoom: function (data) {
			var params = {
				bookmark: data.bookmark,
				roomid: data.roomid,
				roomtitle: data.roomtitle,
				creatorflag: data.userid == data.creatorid ? true : false,
				masterflag: data.userid == data.masterid ? true : false,
				guestflag: data.userid == data.userno ? true : false,
				masterid: data.masterid,
				masterno: data.masterno,
				creatorno: data.creatorno,
				masterseqno: data.masterseqno,
				parentcreatorno: data.parentcreatorno,
				userno: data.userno,
				userid: data.userid,
				usernm: data.usernm,
				auth: data.auth,
				bg: data.bg,
				snstype: data.snstype,
				usertype: data.usertype,
				userlimitcnt: data.auth.userlimitcnt,
				videolimit: data.videoLimit,
				currentpageid: data.currentpageid,
				codestr: data.codestr // IOS 때문에 추가됨.. 2016.08.17
			};

			console.log("[CanvasApp.getRoomInfo] 네이티브로 전달할 룸 초기 정보(currentRoomInfoParams) : " + JSON.stringify(params));

			CordovaPlugin.exec("RoomPlugin", "initializeRoom", [$.extend(PacketMgr, data)]);
		},
	},


	MultiPagePlugin: {
		initPageList: function (pageList) {
			console.log("pageList : " + JSON.stringify(pageList));
			// 페이지 초기 정보는 pageId값만 추출하여 플러그인으로 전달함 - 2016.10.24
			var pageArr = [];
			var pageLen = pageList.length;
			for (var i = 0; i < pageLen; i++) {
				pageArr.push(pageList[i].pageid);
			}

			// 캔버스 페이지 리스트를 네이티브로 전달함.. - 2016.10.19
			if (pageList != null && pageList.length > 0) {
				CordovaPlugin.exec("MultiPagePlugin", "initPageList", pageArr);
			}
		}
	},

	UserPlugin: {
		addClassUserList: function (arr) {
			// 클래스 유저 리스트를 네이티브로 전달.. - 나를 제외한 나머지 클래스 유저
			CordovaPlugin.exec("UserPlugin", "addClassUserList", arr);
		},

		addNotAttendee: function (arr) {
			// 미 참여자 리스트를 네이티브로 전달..
			CordovaPlugin.exec("UserPlugin", "addNotAttendee", arr);
		}
	},


	exec: function (plugin, method, params, onSuccess, onFail) {

		if (typeof onSuccess == 'undefined') {
			onSuccess = function (result) {
				console.log("cordova.exec() success.. " + method);
			}
		}

		if (typeof onFail == 'undefined') {
			onFail = function (result) {
				console.log(method + "error : " + JSON.stringify(result));
			}
		}

		cordova.exec(onSuccess, onFail, plugin, method, params);
	}
}


$(document).ready(function () {
	window.onbeforeunload = function () {
		return;
	};
});


function preventDefaultTest(e) {
	e = e || window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;  
}

function disableScrollTest() {
	/*
	if (window.addEventListener) // older FF
		window.addEventListener('DOMMouseScroll', preventDefault, false);
	window.onwheel = preventDefault; // modern standard
	window.onmousewheel = document.onmousewheel = preventDefault; // older browsers, IE
	*/
	window.ontouchmove  = preventDefault; // mobile
	//document.onkeydown  = preventDefaultForScrollKeys;
	$("body").css("touch-action", "none");
	

}

function enableScrollTest() {
	/*
	if (window.removeEventListener)
		window.removeEventListener('DOMMouseScroll', preventDefault, false);
	window.onmousewheel = document.onmousewheel = null; 
	window.onwheel = null; 
	*/
	window.ontouchmove = null;  
	//document.onkeydown = null;  
	$("body").removeAttr("style");
}	