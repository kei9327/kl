var Auth = {

	userInfo : {},
	deviceToken : null,
    clientIp : null,
    snsType : null,
    accessToken : null,

    init : function(deviceToken){
        this.deviceToken = deviceToken;
        networkinterface.getIPAddress(function(ipAddr) { Auth.clientIp = ipAddr; });

        var svrFlag = _prop('svr.flag');
		fbauth.init({
			client_id    : _prop("fb.id." + svrFlag),
			scope        : _prop("fb.scope"),
			redirect_uri : _prop("fb.rdurl." + svrFlag)
		});
		ggauth.init({
			client_id     : _prop("gg.id." + svrFlag),
			scope         : _prop("gg.scope"),
			redirect_uri  : _prop("gg.rdurl." + svrFlag),
			client_secret : _prop("gg.sk." + svrFlag)
		});
	},
    
    login : function(snsType, fn) {
        Auth.snsType = snsType;
    	if(snsType == "0") {
    		fbauth.login(function(response) {
    			console.log("fbauth.login() result : " + JSON.stringify(response));
    			if(response.status == 'connected') {
    			    Auth.accessToken = response.accessToken;
    			    localStorage.setItem("snsType" , "0");
    			    Auth.encryptAccessToken(response.accessToken);
    			    cordova.exec(function(result){}, function(result){}, "myPlugin", "saveAccessToken", [response.accessToken]);
    			    
    			    Auth.getCookie(response.accessToken, "fb", fn);
                } else {
                    console.log(response.status);
                }
    		});
	    } else if(snsType == "1") {
	    	ggauth.login(function(accessToken) {
	    	    Auth.accessToken = accessToken;
	    		Auth.getCookie(accessToken, "gg", fn);
	    	});
	    } else if(snsType == "") {
            localStorage.clear();
            localStorage.setItem("guest", fn);
        } else{}
    },
    
    logout : function(callback) {

        console.log(JSON.stringify(Auth.userInfo));
    	var userNo = Auth.userInfo.userno;
    	var deviceId = device.uuid;

    	Auth.Device.updateDeviceStatus(userNo, deviceId, "0", function(){
    	    localStorage.clear();
            callback();
    	}, function() {
    	    alert("로그아웃 중에 문제가 발생하였습니다. 관리자에게 문의해주세요.");
    	    return;
    	});


    },
    
    encryptAccessToken : function(accessToken) {
    	function encrypt(result){
            if(accessToken != null || accessToken != '') {
	            var encryptObj = CryptoJS.AES.encrypt(accessToken, result.key);
	            console.log("encryptObj.toString() : " + encryptObj.toString());
	            localStorage.setItem("access_token_encrypt", encryptObj.toString());
	            Auth.decryptAccessToken();
            }
        }
        cordova.exec(encrypt, function(result){alert('err'+JSON.stringify(result));}, "myPlugin", "getKeyVector", []);
    	
    },
    
    decryptAccessToken : function(callback) {
    	function decrypt(result) {
            var encryptToken = localStorage.getItem("access_token_encrypt");
            var decryptObj = CryptoJS.AES.decrypt(encryptToken, result.key);
            console.log("decryptObj.toString(CryptoJS.enc.Utf8) : " + decryptObj.toString(CryptoJS.enc.Utf8));
            var decryptStr = decryptObj.toString(CryptoJS.enc.Utf8);
            callback(decryptStr);
        }
    	cordova.exec(decrypt, function(result){alert('err'+JSON.stringify(result));}, "myPlugin", "getKeyVector", []);
    },
	
	getUserInfo : function(fn) {
		var cookie = null;

		var key = "";
		var iv  = "";
		cookie  = localStorage.getItem("MC");
        
		function decodeCookie(result){
            var ret = null;
            var key = CryptoJS.enc.Utf8.parse(result.key);
            var iv  = CryptoJS.enc.Utf8.parse(result.vector);
            if(cookie != null){
	            var decrypt = CryptoJS.AES.decrypt(Auth.Utils.hexToBase64(cookie), key, {iv: iv});
	            var decryptRes = decodeURI(decrypt.toString(CryptoJS.enc.Utf8));
	            ret = $.parseJSON(decryptRes);
            } else {
            	ret = null;
            }
            if(Object.keys(Auth.userInfo).length === 0) {
                Auth.userInfo = ret;
            }
            fn(ret);
        }
		
        cordova.exec(decodeCookie, function(result){alert('err'+JSON.stringify(result));}, "myPlugin", "getKeyVector", []);
	},

	getCookie : function(accessToken, snsType, onSuccess) {
        var svrFlag = _prop('svr.flag');
        var url = _prop("svr.host." + svrFlag) + _prop(snsType + ".auth");

        var savedDeviceToken = localStorage.getItem("devicetoken");
        console.log("getCookie fired.. accesToken : " + accessToken);
        console.log("[getCookie] url : " + url);
        console.log("[getCookie] deviceToken : " + savedDeviceToken);
        var deviceInfoParam = {
            devicetoken : savedDeviceToken,
            deviceid    : device.uuid.toLowerCase(),  // 32자리로 맞추기..
            deviceinfo  : '',
            ostype      : device.platform.toLowerCase(),
            osversion   : device.version,
            screensize  : window.devicePixelRatio+'|'+screen.width+'x'+screen.height,
            devicemodel : device.model,
            apptype     : 'freeboard',
            appversion  : '1',
            ipaddr      : Auth.clientIp
        };

        deviceInfoParam.deviceinfo = deviceInfoParam.ostype+'|'+deviceInfoParam.osversion+'|'+deviceInfoParam.screensize+'|'+deviceInfoParam.devicemodel;

        var param = {
            token: accessToken,
            deviceinfo : JSON.stringify(deviceInfoParam),
            deviceid : device.uuid,
            devicetoken : savedDeviceToken
        };

        Utils.request(url, "json", param, function(data){
            console.log(data);
            var result = data.result;
            if (result == '0') {
                console.log("get auth success..")
                localStorage.setItem("MC", data.cookie.FBMMC);
                localStorage.setItem("CS", data.cookie.FBMCS);
                Auth.getUserInfo(function(userinfo) {
                    Auth.userInfo = userinfo;
                    //Auth.Device.checkUserDevice(Auth.userInfo.userno, device.uuid);
                });
                onSuccess();
            } else {
                console.log(data);
            }
        });
    },
    

    // 인증과 관련된 유틸리티 Function들..
    Utils : {
    	hexToBase64: function(str) {
          return btoa(String.fromCharCode.apply(null,
            str.replace(/\r|\n/g, "").replace(/([\da-fA-F]{2}) ?/g, "0x$1 ").replace(/ +$/, "").split(" "))
          );
        }
    },

    Device : {

        /**
         * @Deprecated
         * 유저 디바이스 등록여부 체크
         */
        checkUserDevice : function(userNo, osType, deviceId) {
            var svrFlag = _prop("svr.flag");
            var params = {
                userno : userNo,
				ostype : osType,
                deviceid : deviceId
            };

            var url = _prop("svr.host." + svrFlag) + _prop("get.device");

            //console.log("Auth.Device.checkUserDevice / params : " + JSON.stringify(params));
            //console.log(url);

            $.ajax({
                url : url,
                type : "POST",
                data : params,
                cache : false,
                dataType : "json",
                success : function(data) {
                    if(data.result == '0') {
                       console.log("already exist device..");
                       var result = data.device;
                       //console.log(data);
                       data.device.devicetoken = MainApp.deviceToken;
                       data.device.apptype = "freeboard";
					   data.device.status = "1";
                       Auth.Device.updateUserDevice(result);
                    } else {
                        console.log("new user device..");
                        Auth.Device.addUserDevice();
                    }
                }, error : function(e) {
                    console.log("checkUserDevice Error : " + JSON.stringify(e));
                }
            });
        },

        /**
        * 유저 디바이스 등록
        */
        addUserDevice : function() {
            var svrFlag = _prop("svr.flag");
//            if(device.platform == 'iOS')
//                device.uuid = device.uuid.replaceAll("-", "");

            var param = {
                userno      : '',  // 쿠키 스트링에서 꺼낸 값 넣기..
                deviceid    : device.uuid.toLowerCase(),  // 32자리로 맞추기..
                devicetoken : Auth.deviceToken,  //
                deviceinfo  : '',
                ostype      : device.platform.toLowerCase(),
                osversion   : device.version,
                screensize  : window.devicePixelRatio+'|'+screen.width+'x'+screen.height,
                devicemodel : device.model,
                apptype     : 'freeboard',
                appversion  : '1',
                ipaddr      : Auth.clientIp,
				status      : '1'
            };
            param.deviceinfo = param.ostype+'|'+param.osversion+'|'+param.screensize+'|'+param.devicemodel;

            Auth.getUserInfo(function(user){
                param.userno = user.userno;
                console.log(param);
                $.ajax({
                    url     : _prop("svr.host." + svrFlag) + _prop("add.device"),
                    type    : 'POST',
                    data    : param,
                    success : function(data){
                        if(data.result == "0")
                            console.log('success : ' + JSON.stringify(data));
                        else
                            console.log('fail : ' + JSON.stringify(data))
                    },
                    error : function(error){alert(JSON.stringify(error));}
                });
            });
        },

        /**
        * 유저 디바이스 갱신(업데이트)
        */
        updateUserDevice : function(params) {
            console.log("updateUserDevice / params : " + JSON.stringify(params));
            params.devicetoken = MainApp.deviceToken;
            params.apptype = "freeboard";
            var svrFlag = _prop("svr.flag");
            $.ajax({
                url     : _prop("svr.host." + svrFlag) + _prop("update.device"),
                type    : 'POST',
                data    : params,
                success : function(data) {
                    if(data.result == "0") {
                        console.log("user device update success..");
                        // TODO
                    } else {
                        console.log("user device update fail..");
                        // TODO
                    }
                },
                error   : function(error){alert(JSON.stringify(error));}
            });
        },

        updateDeviceStatus : function(userNo, deviceId, status, onSuceess, onFail) {
            var svrFlag = _prop("svr.flag");
            var params = {
                userno : userNo,
                deviceid : deviceId,
                ostype : device.platform.toLowerCase(),
                status : status
            };

            $.ajax({
                url     : _prop("svr.host." + svrFlag) + _prop("update.device.status"),
                type    : 'POST',
                data    : params,
                success : function(data) {
                    if(data.result == "0") {
                        console.log("update device status success..");
                        onSuceess();
                    } else {
                        console.log("update device status fail..");
                        onFail();
                    }
                },
                error   : function(error){alert(JSON.stringify(error));}
            });

        }
    },
    
    test : function(){
        Auth.sendInfo();
    },
    
    sendInfo : function(){
//        if(device.platform == 'iOS')
//            device.uuid = device.uuid.replaceAll("-", "");
        
        var param = {
            userno : '',  // 쿠키 스트링에서 꺼낸 값 넣기..
            deviceid : device.uuid.toLowerCase(),  // 32자리로 맞추기..
            devicetoken : Auth.deviceToken,  //
            deviceinfo : '',
            ostype : device.platform.toLowerCase(),
            osversion : device.version,
            screensize : window.devicePixelRatio+'|'+screen.width+'x'+screen.height,
            devicemodel : device.model,
            apptype : device.platform.toLowerCase(),
            appversion : '1',
            ipaddr : Auth.clientIp
        };
        param.deviceinfo = param.ostype+'|'+param.osversion+'|'+param.screensize+'|'+param.devicemodel;

        /*
        Auth.getUserInfo(function(user){
            param.userno = user.userno;
            cordova.exec(function(res){
                param.devicetoken = res.token;
                console.log(param);
                $.ajax({
                    url: _prop("svr.host." + _prop('svr.flag')) + _prop("device.info"),
                    type: 'POST',
                    data: param,
                    success: function(data){
                        if(data.result == "0")
                            console.log('0 '+data);
                        else if(data.result == "1")
                            console.log('1, '+data);
                        else
                            console.log('else, '+data);
                    },
                    error: function(error){alert(JSON.stringify(error));}
                });
            },function(result){alert('err'+JSON.stringify(result));}, "myPlugin", "getPushToken", []);
        });
        */

        Auth.getUserInfo(function(user){
            param.userno = user.userno;
            console.log(param);
            $.ajax({
                url: _prop("svr.host." + _prop('svr.flag')) + _prop("device.info"),
                type: 'POST',
                data: param,
                success: function(data){
                    if(data.result == "0")
                        console.log('0 '+data);
                    else if(data.result == "1")
                        console.log('1, '+data);
                    else
                        console.log('else, '+data);
                },
                error: function(error){alert(JSON.stringify(error));}
            });
        });
    },
    
    validateFacebookAccessToken : function(userId, token, onSuccess, onFail) {
    	var checkTokenUrl = "https://graph.facebook.com/v2.5/" + userId + "?access_token=" + token;
    	
        $.ajax({
            url : checkTokenUrl,
            dataType : 'json',
            success : function(data, status) {
                console.log(data);
                onSuccess();
            },
            error: function(data, e1, e2) {
                var result = $.parseJSON(data.responseText).error;
                console.log($.parseJSON(data.responseText).error);
                var type = result.type;
                if(type == "OAuthException") {
                    onFail();
                }
            }
        });
        
    },
    
    getFacebookFriendList : function(userId) {
    	Auth.decryptAccessToken(function(accessToken){
    		console.log("복호화된 토큰값 : " + accessToken);
            Auth.validateFacebookAccessToken(userId, accessToken, function(){
            	//var url = "https://graph.facebook.com/v2.5/" + MainApp.userInfo.userid + "/friendlists?access_token=" + accessToken;
                var url = "https://graph.facebook.com/v2.5/me/friends?format=json&access_token=" + accessToken;
                console.log(url);
                $.ajax({
                	url : url,
                    dataType : 'json',
                    success : function(resultData, status) {
                    	console.log(resultData);
                    	cordova.exec(function(result){
                            console.log("cordova.exec() success.. addSNSFriendList");
                        }, function(result){
                            console.log('addSNSFriendList err ' + JSON.stringify(result));
                        }, "myPlugin", "addSNSFriendList", resultData.data);
                    },
                    error: function(data, e1, e2) {
                    	console.log($.parseJSON(data.responseText).error);
                    }
                });
            }, function() {
            	alert("페이스북 인증값이 올바르지 않습니다. 다시 로그인해주세요.");
            	return;
            });
    	});
    }
}

