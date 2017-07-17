var UrlResource = {
	"context": "http://www.wenote.com",
	"signup": "/signup/view.do",
	//"auth"	: "https://www.wenote.com/common/login.do?wbredir=",

	"auth": "/login.jsp?wbredir=",

	"profile.set": "klounge/profile/setProfile.json",
	"profile.update": "klounge/profile/updateProfile.json",
	"profile.get.star": "klounge/profile/getStar.json",

	"history.list": "klounge/history/list",
	"history.remove": "klounge/history/remove.json",

	"canvas.download": "canvas/download",
	"canvas.add": "canvas/add.json",
	"canvas.packet": "canvas/packet.json",
	"canvas.history.list": "klounge/history/canvas/list",

	"canvas.get": "canvas/get.json",
	"canvas.remove": "canvas/remove.json",

	"comment.add": "canvas/comment/add.json",
	"comment.remove": "canvas/comment/remove.json",
	"comment.update.pos": "canvas/comment/pos/update.json",
	"comment.list": "canvas/comment/list",

	"memo.add": "canvas/memo/add.json",
	"memo.update.pos": "canvas/plugin/pos/update.json",
	"memo.update.color": "canvas/plugin/color/update.json",
	"memo.update.fold": "canvas/plugin/fold/update.json",
	"memo.update.ord": "canvas/plugin/ord/update.json",
	"memo.save": "canvas/memo/save.json",
	"memo.save.title": "canvas/memo/title/save.json",
	"memo.remove": "canvas/memo/remove.json",

	"vshare.add": "canvas/vshare/add.json",
	"vshare.save": "canvas/vshare/save.json",
	"vshare.save.title": "canvas/vshare/title/save.json",
	"vshare.remove": "canvas/vshare/remove.json",

	"vcam.update.screen": "vcam/updateScreen.json",

	"magicbox.list": "magicbox/list.json",
	"magicbox.save": "magicbox/save.json",


	"plugin.update.ord": "canvas/plugin/ord/update.json",
	"plugin.update.pos": "canvas/plugin/pos/update.json",

	"bg.save": "canvas/bg/save.json",

	"invite": "canvas/invite.json",

	"mail.check": "mail/check.json",
	"mail.send": "mail/send.json",

	"bookmark.add": "bm/add.json",
	"bookmark.remove": "bm/remove.json",


	"createSubRoom": "createSubRoom.json",
	"ck.connection": "checkConnection.json",
	"update.title": "updateRoomTitle.json",
	"upload": "uploadFiles.json",
	"upload.image": "uploadImages.json",
	"upload.save": "saveCanvas.json",
	"check.passwd": "chkPassword.json",
	"get.turn.svr": "getTurnSvr.json",
	"popup.capture": "canvas/capture",
	"pdf.download": "file/pdf",

	"get.friend.rooms": "/getFriendsRoomList.json",
	"get.turn.server": "/getTurnSvr.json",

	"friend.facebook.list": "fb/list",
	"friend.google.list": "gl/list",

	"sound.path": "res/common/sounds/",

	"facebook.profile.url.1": "https://graph.facebook.com/",
	"facebook.profile.url.2": "/picture",

	"notify.invite.remove": "invite/remove.json",
	"notify.invite.send": "invite/send.json",
	"notify.invite.search": "invite/search/list.json",
	"notify.invite.receive": "invite/receive/list.json",
	"notify.invite.not.attend": "invite/notattend/list.json",  
	"notify.invite.join.req": "invite/join/req.json",
	"notify.invite.read": "invite/read.json",
	"room.check": "room/check.json",
	"room.extend.roomuser": "room/extendRoomUser.json",

	"page.add": "page/add.json",
	"page.remove": "page/remove.json",
	"page.change": "page/change.json",
	"page.order": "page/order.json",
	"page.get.info": "page/getInfo.json",
	"youtube.host": "https://youtu.be/",
	
	"zico.host": "https://zico.knowlounges.com/",
	"zico.get.rtc.server": "peer/getRtcServer.json",
	"zico.get.turn.server": "peer/getTurnServer.json",
	"zico.get.access.token": "peer/getAccessToken.json"
};
UrlResource.getUrl = function (str) {
	if (UrlResource[str]) str = UrlResource[str];
	return str;
};

function _url(str) {
	return UrlResource.getUrl(str);
}


var CodeResource = {
	// data code
	"DEFAULT_USER_TYPE": "0",
	"STUDENT_USER_TYPE": "1",
	"TEACHER_USER_TYPE": "2",
	"REMOVE_LIMIT": "removeLimit",
	"RELOAD_ROOM_THUMB": "reloadRoomThumb",

	// 	result code
	"SUCCESS": "0",
	"ALREADY_EXTENDED_ROOM": "1",
	"NOT_SUPPORT_BROWSER": "-91",
	"USERID_NOT_FOUND": "-101",
	"INCORRECT_PASSWORD": "-102",
	"NOT_AUTHORIZED": "-103",
	"EXPIRED_SESSION": "-104",
	"NOT_INSTALLED_DEVICE": "-105",
	"NOT_FOUND_SESSION": "-106",
	"INVALID_ROOM": "-201",
	"ALREADY_PARTICIPATED": "-202",
	"ROOM_CREATE_FAIL": "-203",
	"ALREADY_EXIST_ROOMID": "-204",
	"ROOM_CNT_LIMITED": "-205",
	"ROOM_DELETE_FAIL": "-206",
	"ROOM_USER_LIMITED": "-207",
	"ROOM_USER_MAX_LIMITED": "-208",
	"ROOM_PAGE_NOT_FOUND": "-209",

	"ALREADY_EXIST_USER": "-301",
	"CAN_NOT_REMOVE_ROOM": "-401",

	"EMAIL_INVALID": "-501",
	"EMAIL_SEND_FAIL": "-502",
	"BOOKMARK_ALREADY_ADDED": "-503",
	"BOOKMARK_LIMIT_CNT": "-504",

	"FILE_HASH_FAIL": "-900",
	"FILE_UPLOAD_FAIL": "-901",
	"FILE_UPLOAD_SIZE_OVERFLOW": "-904",
	"FILE_NOT_FOUND": "-902",
	"FILE_DELETE_FAIL": "-903",

	"USER_CREATE_FAIL_ID_EXIST": "-1001",
	"DOMAIN_CREATE_FAIL_ID_EXIST": "-1002",

	"NOT_EXIST_GROUP": "-2001",
	"UNKNOWN_GROUP_ERROR": "-2002",
	"GROUP_URL_DISABLED": "-2003",

	"STAR_SERVER_CONNECTION_FAIL": "-3000",
	"STAR_USER_CREATE_FAIL": "-3001",
	"STAR_USER_ISSUE_TOKEN_FAIL": "-3002",
	"STAR_INSUFFICIENT_FUNDS": "-3098",
	"STAR_TOKEN_EXPIRED": "-3099",

	"PARAMETER_VALIDATION_CHECK_FAIL": "-8001",
	"DB_OPERATION_FAIL": "-8002",
	"COOKIE_EXPIRED": "-8011",
	"COOKIE_NOT_FOUND": "-8012",
	"COOKIE_INVALID_MC": "-8013",
	"COOKIE_INVALID_CS": "-8014",

	"UNKNOWN_FAIL": "-8080",
	"OTHER_DEVICE_ALREADY_IN_ROOM": "-5001"
};
CodeResource.getCode = function (str) {
	if (CodeResource[str]) str = CodeResource[str];
	return str;
};

function _code(str) {
	return CodeResource.getCode(str);
}

if (typeof (MessageResource) != "undefined") {
	MessageResource.getMessage = function (str) {
		if (MessageResource[str]) str = MessageResource[str];
		return str;
	};

	function _msg(str) {
		var msgKey = Utils.isKLounge() ? (str + ".klounge") : str;
		if (MessageResource.getMessage(msgKey) != msgKey) {
			return MessageResource.getMessage(msgKey);
		} else {
			if (MessageResource.getMessage(str) == str) return "";
		}
		return MessageResource.getMessage(str);
	}
}

function _msg(str) {
	var msgKey = Utils.isKLounge() ? (str + ".klounge") : str;
	if (MessageResource.getMessage(msgKey) != msgKey) {
		return MessageResource.getMessage(msgKey);
	} else {
		if (MessageResource.getMessage(str) == str) return "";
	}
	return MessageResource.getMessage(str);
}


/**
 * 	 @title 	  : Extend Prototype
 * 	 @date 	      : 2010.04.22
 *   @author      : kim dong hyuck 
 * 	 @description : 스크립트에서 Object사이의 데이터를 유지하고 기능이나 정보부분을 추가적으로 확장시킬수 있게 하는 부분이다.
 */
if (!Object.extend) {
	Object.extend = function (destination, source) {
		for (var property in source)
			destination[property] = source[property];
		return destination;
	};
}

/**
 *   @title 	  : String Class Extend
 * 	 @date 	      : 2010.04.22
 *   @author      : kim dong hyuck
 * 	 @description : String class를 Extend하여 확장시킨 메소드들 정의 
 */
Object.extend(String.prototype, (function () {
	//Object.extend(String.prototype, {
	function trim() {
		return this.replace(/^\s+|\s+$/g, "");
	}

	function ltrim() {
		return this.replace(/^\s+/, "");
	}

	function rtrim() {
		return this.replace(/\s+$/, "");
	}

	function bytes() {
		var cnt = 0;
		for (var i = 0; i < this.length; i++) {
			if (this.charCodeAt(i) > 127) cnt += 2;
			else cnt++;
		}
		return cnt;
	}

	function cut(len, sep) {
		var str = this;
		if (!sep) sep = '';
		var l = 0;
		for (var i = 0; i < str.length; i++) {
			l += (str.charCodeAt(i) > 128) ? 2 : 1;
			if (l > len) return str.substring(0, i) + sep;
		}
		return str;
	}

	function isUserid() {
		return (/^[a-zA-z]{1}[0-9a-zA-Z]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function isDeptid() {
		return (/^[a-zA-z]{1}[0-9a-zA-Z_-]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function toInt() {
		if (!isNaN(this)) return parseInt(this);
		else return null;
	}

	function money() {
		var num = this.trim();
		while ((/(-?[0-9]+)([0-9]{3})/).test(num)) {
			num = num.replace((/(-?[0-9]+)([0-9]{3})/), "$1,$2");
		}
		return num;
	}

	function digits(len) {
		var digit = "";
		if (this.length < len) {
			for (var i = 0; i < len - this.length; i++) {
				digit += "0";
			}
		}
		return digit + this;
	}

	function ext() {
		return (this.indexOf(".") < 0) ? "" : this.substring(this.lastIndexOf(".") + 1, this.length);
	}

	function meta() {
		var str = this;
		var result = "";
		for (var i = 0; i < str.length; i++) {
			if ((/([\$\(\)\*\+\.\[\]\?\\\^\{\}\|]{1})/).test(str.charAt(i))) {
				result += str.charAt(i).replace((/([\$\(\)\*\+\.\[\]\?\\\^\{\}\|]{1})/), "\\$1");
			} else {
				result += str.charAt(i);
			}
		}
		return result;
	}

	function remove() {
		var arg = arguments[0] ? arguments[0] : "";
		return (arg == "") ? this : eval("this.replace(/[" + arg.meta() + "]/g, \"\")");
	}

	function isBlank() {
		var str = this.trim();
		for (var i = 0; i < str.length; i++) {
			if ((str.charAt(i) != "\t") && (str.charAt(i) != "\n") && (str.charAt(i) != "\r")) {
				return false;
			}
		}
		return true;
	}

	function isNum() {
		return (/^[0-9]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function isEng() {
		return (/^[a-zA-Z]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function isEngNum() {
		return (/^[a-zA-Z0-9]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function isKor() {
		return (/^[가-힣]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function isNonSpecial() {
		return (/^[a-zA-Z0-9 ㄱ-ㅎㅏ-ㅣ가-힣 ]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function hasSpecialChar() {
		var stringRegx = /[~!@\#$%<>^&*\()\-=+_\’]/gi;
		return stringRegx.test(this.remove(arguments[0]));
	}

	function isTagString() {
		return (/^[a-zA-Z0-9 ㄱ-ㅎㅏ-ㅣ가-힣,]+$/).test(this.remove(arguments[0])) ? true : false;
	}

	function isJumin() {
		var arg = arguments[0] ? arguments[0] : "";
		var jumin = eval("this.match(/[0-9]{2}[01]{1}[0-9]{1}[0123]{1}[0-9]{1}" + arg + "[1234]{1}[0-9]{6}$/)");
		if (jumin == null) return false;
		else jumin = jumin.toString();

		// add
		if (arg != '') {
			jumin = jumin.replaceAll(arg, '');
		}

		var birthYY = (parseInt(jumin.charAt(6)) == (1 || 2)) ? "19" : "20";
		birthYY += jumin.substr(0, 2);
		var birthMM = jumin.substr(2, 2) - 1;
		var birthDD = jumin.substr(4, 2);
		var birthDay = new Date(birthYY, birthMM, birthDD);
		if (birthDay.getYear() % 100 != jumin.substr(0, 2) || birthDay.getMonth() != birthMM || birthDay.getDate() != birthDD) {
			return false;
		}

		var sum = 0;
		var num = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
		var last = parseInt(jumin.charAt(12));
		for (var i = 0; i < 12; i++) {
			sum += parseInt(jumin.charAt(i)) * num[i];
		}
		return ((11 - sum % 11) % 10 == last) ? true : false;
	}

	function isForeign() {
		var arg = arguments[0] ? arguments[0] : "";
		var jumin = eval("this.match(/[0-9]{2}[01]{1}[0-9]{1}[0123]{1}[0-9]{1}" + arg + "[5678]{1}[0-9]{1}[02468]{1}[0-9]{2}[6789]{1}[0-9]{1}$/)");
		if (jumin == null) {
			return false;
		} else {
			jumin = jumin.toString().num().toString();
		}

		var birthYY = (parseInt(jumin.charAt(6)) == (5 || 6)) ? "19" : "20";
		birthYY += jumin.substr(0, 2);
		var birthMM = jumin.substr(2, 2) - 1;
		var birthDD = jumin.substr(4, 2);
		var birthDay = new Date(birthYY, birthMM, birthDD);
		if (birthDay.getYear() % 100 != jumin.substr(0, 2) || birthDay.getMonth() != birthMM || birthDay.getDate() != birthDD) {
			return false;
		}
		if ((parseInt(jumin.charAt(7)) * 10 + parseInt(jumin.charAt(8))) % 2 != 0) {
			return false;
		}
		var sum = 0;
		var num = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
		var last = parseInt(jumin.charAt(12));
		for (var i = 0; i < 12; i++) {
			sum += parseInt(jumin.charAt(i)) * num[i];
		}
		return (((11 - sum % 11) % 10) + 2 == last) ? true : false;
	}

	function isBiznum() {
		var arg = arguments[0] ? arguments[0] : "";
		var biznum = eval("this.match(/[0-9]{3}" + arg + "[0-9]{2}" + arg + "[0-9]{5}$/)");
		if (biznum == null) {
			return false;
		} else {
			biznum = biznum.toString().num().toString();
		}

		var sum = parseInt(biznum.charAt(0));
		var num = [0, 3, 7, 1, 3, 7, 1, 3];
		for (var i = 1; i < 8; i++) sum += (parseInt(biznum.charAt(i)) * num[i]) % 10;
		sum += Math.floor(parseInt(parseInt(biznum.charAt(8))) * 5 / 10);
		sum += (parseInt(biznum.charAt(8)) * 5) % 10 + parseInt(biznum.charAt(9));
		return (sum % 10 == 0) ? true : false;
	}

	function isCorpnum() {
		var arg = arguments[0] ? arguments[0] : "";
		var corpnum = eval("this.match(/[0-9]{6}" + arg + "[0-9]{7}$/)");
		if (corpnum == null) return false;
		else corpnum = corpnum.toString().num().toString();

		var sum = 0;
		var num = [1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2];
		var last = parseInt(corpnum.charAt(12));
		for (var i = 0; i < 12; i++) {
			sum += parseInt(corpnum.charAt(i)) * num[i];
		}
		return ((10 - sum % 10) % 10 == last) ? true : false;
	}

	function isEmail() {
		return (/\w+([-+.]\w+)*@\w+([-.]\w+)*\.[a-zA-Z]{2,4}$/).test(this.trim());
	}

	function isPhone() {
		var arg = arguments[0] ? arguments[0] : "";
		//return eval("(/(02|0[3-9]{1}[0-9]{1})" + arg + "[1-9]{1}[0-9]{2,3}" + arg + "[0-9]{4}$/).test(this)");
		//return eval("(/(02|070|0502|0503|0505|0506|0[3-9]{1}[0-9]{1})" + arg + "[1-9]{1}[0-9]{2,3}" + arg + "[0-9]{4}$/).test(this)");
		if (this.length > 8) return eval("(/(02|070|0502|0503|0505|0506|0[3-9]{1}[0-9]{1})" + arg + "[1-9]{1}[0-9]{2,3}" + arg + "[0-9]{4}$/).test(this)");
		else return eval("(/(02|070|0502|0503|0505|0506|0[3-9]{1}[0-9]{1}){0,1}" + arg + "[1-9]{1}[0-9]{2,3}" + arg + "[0-9]{4}$/).test(this)");
	}

	function isMobile() {
		var arg = arguments[0] ? arguments[0] : "";
		//return eval("(/01[016789]" + arg + "[1-9]{1}[0-9]{2,3}" + arg + "[0-9]{4}$/).test(this)");
		return eval("(/(0502|0503|0505|0506|01[016789])" + arg + "[1-9]{1}[0-9]{2,3}" + arg + "[0-9]{4}$/).test(this)");
	}

	function escape() {
		var arg = this;
		arg = arg.replace(/&/g, "&amp;");
		arg = arg.replace(/</g, "&lt;");
		arg = arg.replace(/>/g, "&gt;");
		arg = arg.replace(/'/g, "&apos;");
		arg = arg.replace(/\n/g, "<br/>");
		return arg;
	}

	function unescape() {
		var arg = this;
		arg = arg.replace(/&amp;/g, "&");
		arg = arg.replace(/&lt;/g, "<");
		arg = arg.replace(/&gt;/g, ">");
		arg = arg.replace(/&quot;/g, "\"");
		arg = arg.replace(/&apos;/g, "'");
		arg = arg.replace(/<br>/g, "\n");
		arg = arg.replace(/<Br>/g, "\n");
		arg = arg.replace(/<BR>/g, "\n");
		return arg;
	}

	function isPwd() {
		// return (this.length < 6 || /^[0-9!@#$%^&*()]{6,12}$/.test(this) == true || /^[a-zA-Z!@#$%^&*()]{6,12}$/.test(this) == true)? false : true;
		return (/^[0-9a-zA-Z!@#$%^&*()]{6,12}$/).test(this);
	}

	function replaceAll(str1, str2) {
		var temp_str = "";
		var temp_trim = this.replace(/(^\s*)|(\s*$)/g, "");

		if (temp_trim && str1 != str2) {
			temp_str = temp_trim;
			while (temp_str.indexOf(str1) > -1) temp_str = temp_str.replace(str1, str2);
		}

		return temp_str;
	}

	function isURL() {

		var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;

		return regexp.test(this);
	}
	return {
		trim: trim,
		ltrim: ltrim,
		rtrim: rtrim,
		bytes: bytes,
		cut: cut,
		isUserid: isUserid,
		isDeptid: isDeptid,
		toInt: toInt,
		money: money,
		digits: digits,
		ext: ext,
		meta: meta,
		remove: remove,
		isBlank: isBlank,
		isNum: isNum,
		isEng: isEng,
		isEngNum: isEngNum,
		isKor: isKor,
		isJumin: isJumin,
		isForeign: isForeign,
		isBiznum: isBiznum,
		isCorpnum: isCorpnum,
		isEmail: isEmail,
		isPhone: isPhone,
		isMobile: isMobile,
		escape: escape,
		unescape: unescape,
		isPwd: isPwd,
		replaceAll: replaceAll,
		isURL: isURL,
		isNonSpecial: isNonSpecial,
		hasSpecialChar: hasSpecialChar,
		isTagString: isTagString
	};
})());

/**
 *   @title 	  : String Buffer Class
 * 	 @date 	      : 2010.04.22
 *   @author      : kim dong hyuck 
 * 	 @description : String Buffer를 Array로 제공하여 사용가능하게 정의한 클래스 
 */
function StringBuffer() {
	this.buffer = new Array();
}
StringBuffer.prototype = {
	append: function (obj) {
		this.buffer.push(obj);
	},
	toString: function () {
		return this.buffer.join("");
	},
	length: function () {
		return this.buffer.length;
	},
	indexOf: function (obj) {
		return this.buffer.indexOf(obj);
	},
	reverse: function () {
		return this.buffer.reverse();
	}
};

/**
 *   @title 	  : Array Extend Class
 * 	 @date 	      : 2010.04.22
 *   @author      : kim dong hyuck 
 * 	 @description : String Buffer를 Array로 제공하여 사용가능하게 정의한 클래스 
 */
Array.prototype.unique = function () {
	var a = {};
	for (var i = 0; i < this.length; i++) {
		if (typeof a[this[i].strip()] == "undefined") {
			a[this[i]] = 0;
		}
	}

	this.length = 0;
	for (var i in a) {
		this[this.length] = i;
	}
	return this;
};
Array.prototype.indexOf = function (item, i) {
	i || (i = 0);
	var length = this.length;
	if (i < 0) i = length + i;
	for (; i < length; i++)
		if (this[i] === item) return i;
	return -1;
}
Array.prototype.without = function () {
	var compare = arguments[0];
	var retArr = [];
	var len = this.length;

	for (var i = 0; i < len; i++) {
		var item = this[i];
		if (item == compare) {
			continue;
		}
		retArr.push(item);
	}
	return retArr;
}

/**
 *   @title       : Array Extend Class
 *      @date           : 2013.12.23
 *   @author      : kim dong hyuck 
 *      @description : Map 자료구조 정의 
 */
var Map = function () {
	this.map = new Object();
};
Map.prototype = {
	put: function (key, value) {
		this.map[key] = value;
	},
	get: function (key) {
		return this.map[key];
	},
	containsKey: function (key) {
		return key in this.map;
	},
	containsValue: function (value) {
		for (var prop in this.map) {
			if (this.map[prop] == value) return true;
		}
		return false;
	},
	isEmpty: function (key) {
		return (this.size() == 0);
	},
	clear: function () {
		for (var prop in this.map) {
			delete this.map[prop];
		}
	},
	remove: function (key) {
		delete this.map[key];
	},
	keys: function () {
		var keys = new Array();
		for (var prop in this.map) {
			keys.push(prop);
		}
		return keys;
	},
	values: function () {
		var values = new Array();
		for (var prop in this.map) {
			values.push(this.map[prop]);
		}
		return values;
	},
	size: function () {
		var count = 0;
		for (var prop in this.map) {
			count++;
		}
		return count;
	}
};


/**
 *   @title 	  : Date Extend Class
 * 	 @date 	      : 2010.05.06
 *   @author      : park han 
 * 	 @description : ISO 8601 타입의 date string을 date type 으로 변환 
 */
Date.prototype.setISO8601 = function (dString) {
	var regexp = /(\d\d\d\d)(-)?(\d\d)(-)?(\d\d)(T)?(\d\d)(:)?(\d\d)(:)?(\d\d)(\.\d+)?(Z|([+-])(\d\d)(:)?(\d\d))/;
	if (dString.toString().match(new RegExp(regexp))) {
		var d = dString.match(new RegExp(regexp));
		var offset = 0;
		this.setUTCDate(1);
		this.setUTCFullYear(parseInt(d[1], 10));
		this.setUTCMonth(parseInt(d[3], 10) - 1);
		this.setUTCDate(parseInt(d[5], 10));
		this.setUTCHours(parseInt(d[7], 10));
		this.setUTCMinutes(parseInt(d[9], 10));
		this.setUTCSeconds(parseInt(d[11], 10));
		if (d[12])
			this.setUTCMilliseconds(parseFloat(d[12]) * 1000);
		else
			this.setUTCMilliseconds(0);
		if (d[13] != 'Z') {
			offset = (d[15] * 60) + parseInt(d[17], 10);
			offset *= ((d[14] == '-') ? -1 : 1);
			this.setTime(this.getTime() - offset * 60 * 1000);
		}
	} else {
		this.setTime(Date.parse(dString));
	}
	return this;
};
Date.prototype.getDefStr = function () {
	var result = "";
	var y = this.getFullYear();
	var m = this.getMonth() + 1;
	var d = this.getDate();
	var h = this.getHours();
	var mi = this.getMinutes();
	var s = this.getSeconds();
	result = y + "-" + (m < 10 ? "0" + m : m) + "-" + (d < 10 ? "0" + d : d) + (h > 12 ? " 오후 " : " 오전 ") + (h > 12 ? h - 12 : h) + ":" + (mi < 10 ? "0" + mi : mi) + ":" + (s < 10 ? "0" + s : s);

	return result;
};


/***
 * 
 * @param text
 * @param x
 * @param y
 * @param fontSize
 * @param w
 * @param h
 * @param leading
 * @param indent
 * @returns textbox to canvas
 */
CanvasRenderingContext2D.prototype.textArea = function (text, x, y, fontSize, w, h, leading, indent) {

	w = parseInt(w);
	h = parseInt(h);

	var words = text instanceof Array ? text : text.split(/\b(?=\S)|(?=\s)/g),
		baseline = this.textBaseline,
		rowLength = (indent || 0),
		totalHeight = 0,
		fontSize = parseInt(fontSize),
		leading = (leading || 1) * fontSize,
		m, i, diff, s, breakPoint, w_l, w_r;

	this.textBaseline = "top";
	for (i = 0; i < words.length; ++i) {

		// Newline: don't bother measuring, just increase the total height. 
		if ("\n" === words[i]) {
			rowLength = 0;
			totalHeight += leading;

			// If the newline's pushed the rest of the text outside the drawing area, abort. 
			if (totalHeight + leading >= h) return {
				x: rowLength + x,
				y: totalHeight + y,
				remainder: words.slice(i)
			}
			continue;
		}


		// Strip any leading tabs. 
		if (!rowLength && /^\t+/.test(words[i]))
			words[i] = words[i].replace(/^\t+/, "");


		m = this.measureText(words[i]).width;

		// This is one honkin' long word, so try and hyphenate it. 
		if ((diff = w - m) <= 0) {
			diff = Math.abs(diff);

			// Figure out which end of the word to start measuring from. Saves a few extra cycles in an already heavy-duty function. 
			if (diff - w <= 0)
				for (s = words[i].length; s; --s) {
					if (w > this.measureText(words[i].substr(0, s) + "-").width + fontSize) {
						// if(w > this.measureText(words[i].substr(0, s)).width + fontSize){
						breakPoint = s;
						break;
					}
				}

			else
				for (s = 0; s < words[i].length; ++s) {
					if (w < this.measureText(words[i].substr(0, s + 1) + "-").width + fontSize) {
						// if(w < this.measureText(words[i].substr(0, s+1)).width + fontSize){
						breakPoint = s;
						break;
					}
				}

			if (breakPoint) {
				var w_l = words[i].substr(0, s + 1) + "-",
					// var w_l	=	words[i].substr(0, s+1),
					w_r = words[i].substr(s + 1);

				words[i] = w_l;
				words.splice(i + 1, 0, w_r);
				m = this.measureText(w_l).width;
			}
		}


		// If there's no more room on the current line to fit the next word, start a new line. 
		if (rowLength > 0 && rowLength + m >= w) {

			// We've run out of room. Return an array of the remaining words we couldn't fit. 
			if (totalHeight + leading * 2 >= h) return {
				x: rowLength + x,
				y: totalHeight + y,
				remainder: words.slice(i)
			};

			rowLength = 0;
			totalHeight += leading;

			// If the current word is just a space, don't bother. Skip (saves a weird-looking gap in the text) 
			if (" " === words[i]) continue;
		}


		// Write another word and increase the total length of the current line. 
		this.fillText(words[i], rowLength + x, totalHeight + y);
		rowLength += m;
	}

	// Restore the context's text baseline property 
	this.textBaseline = baseline;
	return {
		x: rowLength + x,
		y: totalHeight + y,
		remainder: []
	};
};


CanvasRenderingContext2D.prototype.txt2canvas = function (text, x, y, size, maxWidth, maxHeight, face) {
	y += parseInt(size, 10);

	var fontScale = face == "Arial" ? 1.4 : face == "calibri" ? 1.4 : face == "Georgia" ? 1.25 : face == "Tahoma" ? 1.55 : face == "Verdana" ? 1.45 : face == "Comic Sans MS" ? 1.61 : 1.4;

	var charWidth = this.measureText("M").width;
	var lineHeight = charWidth * fontScale;

	var leftStartX = x;

	text = text.replace(/(\r\n|\n\r|\r|\n)/g, "\n");
	text = text.replace(/(\t)/g, "        ");

	var lines = text.split('\n');
	for (var i = 0; i < lines.length; i++) {
		var words = lines[i];

		var lineCheck = 0;
		for (var n = 0; n < words.length; n++) {
			var ch = words.charAt(n);
			charWidth = this.measureText(ch).width;
			if (x == leftStartX && ch == " ") {
				x += (charWidth);
				continue;
			}

			// console.log("fillText : " + ch)
			this.fillText(ch, x, y);

			x += (charWidth);

			lineCheck += charWidth;

			if (lineCheck > (maxWidth - charWidth)) {
				y += lineHeight;
				lineCheck = 0;
				x = leftStartX;
			}
		}

		y += lineHeight;
		x = leftStartX;
	}
}

/**
 *   @title 	  : Utils Object
 * 	 @date 	      : 2010.04.22
 *   @author      : kim dong hyuck 
 * 	 @description : 자주쓰고 유용하게 사용되는 Util 메소드들의 집합 
 */
var Utils = {
	_gstrFullPath: null,
	isContext: true,
	isRewrite: false,
	isHttps: false,
	useAPIDomain: false,
	useLogger: false,
	isCordova: false,
	support: [
		"ar", // Arabic  2016.07.08 언어 안정화 전까지 아직 지원 안함.
		"da", // Danish
		"de", // German
		"es", // Spanish (latin)
		"fi", // Finnish
		"fr", // French		
		"it", // Italian
		"ja", // Japanese
		"ko", // korean
		"pl", // Polish
		"pt", // Portuguese
		"pt-BR", // Portuguese (brazil)
		"ru", // ussian		
		"tr", // Turkish
		"uk", // Ukrainian
		"zh", // Simplified Chinese
		"zh-TW", // Traditional Chinese
	],
	log: function (msg) {
		if (location.hostname.indexOf("test") > -1 || location.hostname.indexOf("dev") > -1) {
			// console.log.apply(null, arguments);
			var args = Array.prototype.slice.call(arguments);
			var console = window.console;
			if (typeof console == 'object' && console.log && console.log.apply) {
				console.log.apply(console, args);
			}
		}
	},
	logger: function (cmd, action, time) {
		// 국책과제용 로거
		if (this.useLogger) {
			console.log(cmd, action, time);
		}
	},
	isSvr: function () {
		if (location.hostname.indexOf("test") > -1 || location.hostname.indexOf("dev") > -1) {
			return false;
		} else {
			return true;
		}
	},
	isKLounge: function () {
		if(this.checkPlatform() == "web"){
			var hostName = location.hostname.toLowerCase();
			if (hostName.indexOf("knowlounges.com") > -1) {
				return true;
			} else {
				return false;
			}
		}else{
			return true;
		}		
	},
	popup: function (url, title, width, height) {
		var screenWidth = screen.availWidth;
		var screenHeight = screen.availHeight;
		var xPos, yPos, opt;

		var opt = '';
		if (width == 0 || height == 0) {
			xPos = screenWidth - 20;
			yPos = screenHeight - 240;
			if ($.browser.safari) {
				screenHeight = screenHeight - 30;
			}
			opt = 'left=0,top=0,width=' + screenWidth + ',height=' + screenHeight + ',toolbar=no,menubar=no,status=no,scrollbars=no,resizable=yes';
		} else {
			xPos = (screenWidth - width) / 2;
			yPos = (screenHeight - height) / 2;
			opt = 'left=' + xPos + ',top=' + yPos + ',width=' + width + ',height=' + height + ',toolbar=no,menubar=no,status=no,scrollbars=no,resizable=yes';
		}

		win = window.open(url, title, opt);
	},
	getStatusName: function () {
		return "FBSTATUS"
	},
	getFullPath: function () {
		var ret = "";
		if (this._gstrFullPath) {
			ret = this._gstrFullPath;
		} else {
			ret = this.getBaseUrl(this.isContext);
			this._gstrFullPath = ret;
		}
		return ret;
	},
	addContext: function (url) {
		if (url.indexOf("http://") < 0 && url.indexOf("https://") < 0) {
			url = this.getFullPath() + (url.indexOf("/") == 0 ? url : "/" + url);
		}

		return url;
	},
	addBase: function (url) {
		var fullUrl = this.getBaseUrl(false);
		return fullUrl + "/" + url;
	},
	addResPath: function (resType, url) {
		var svcType = this.isKLounge() ? "klounge" : "fb";
		return this.addContext("res/" + svcType + "/" + resType + "/" + url);
	},

	addResHomePath: function (resType, url) {
		var svcType = this.isKLounge() ? "klounge" : "fb";
		return this.addContext("res/" + svcType + "_home/" + resType + "/" + url);
	},

	addFullContext: function (link) {
		var strHost = document.location.host;
		var url = document.location.href;

		var getBaseUrl = function (isContext) {
			if (isContext) {
				var tmp = url.indexOf(strHost);

				var context = "";
				if (url.length > tmp + strHost.length) {
					var temp = url.substring(tmp + strHost.length + 1);
					if (temp.indexOf("/") > 0) {
						context = temp.substring(0, temp.indexOf("/"));
					}
				}
				strHost = strHost + "/" + context;
			}

			return (url.indexOf('https://') >= 0 ? "https://" + strHost : "http://" + strHost);
		}
		var getFullPath = function () {
			return getBaseUrl(Utils.isContext);
		}

		return getFullPath() + "/" + link;
	},
	getBaseUrl: function (isContext) {
		/*
		var strHost = document.location.host;
		var url = document.location.href;
		
		if (isContext) {
			var tmp = url.indexOf(strHost);
			
			var context = "";
			if (url.length > tmp + strHost.length) {
				var temp = url.substring(tmp + strHost.length + 1);
				if (temp.indexOf("/") > 0) {
					context = temp.substring(0, temp.indexOf("/"));
				}
			}
			strHost = strHost + "/" + context;
		}		
		
		return (url.indexOf('https://') >= 0 ? "https://"+strHost : "http://"+strHost);
		*/

		if (isContext) {
			var strHost = document.location.host;
			var url = document.location.href;
			var tmp = url.indexOf(strHost);
			var context = "";
			if (url.length > tmp + strHost.length) {
				var temp = url.substring(tmp + strHost.length + 1);
				if (temp.indexOf("/") > 0) {
					context = temp.substring(0, temp.indexOf("/"));
				}
			}
			return "/" + context;
		} else {
			return "";
		}
	},
	getImgUploadUrl: function (type) {
		var basePath = "";
		if (_msg("domain.svr") == 0 || _msg("domain.svr") == "0") {
			basePath = _url("file.base.url.test");
		} else {
			basePath = _url("file.base.url.svr");
		}
		var ctxPath = "";
		if (type == "addContact") {
			ctxPath = _url("file.upload.temp.Contact");
		} else if (type == "updateContact") {
			ctxPath = _url("file.upload.update.Contact");
		} else if (type == "socialTemplate") {
			ctxPath = _url("file.upload.socialcard");
		} else {
			ctxPath = _url("file.upload.profile");
		}
		return basePath + ctxPath;
	},
	getFileBaseUrl: function (type) {
		var basePath = "";
		if (_msg("domain.svr") == 0 || _msg("domain.svr") == "0") {
			basePath = _url("file.base.url.test");
		} else {
			basePath = _url("file.base.url.svr");
		}
		return basePath;
	},
	getImgDownloadUrl: function (ctxKey, imgKey, thumbCode) {
		var basePath = "";
		if (_msg("domain.svr") == 0) {
			basePath = _url("file.base.url.test");
		} else {
			basePath = _url("file.base.url.svr");
		}
		var ctxPath = _url(ctxKey);

		var strUrl = basePath + ctxPath + "/" + imgKey + "/" + ((thumbCode == null || "" == thumbCode) ? "" : thumbCode);

		return strUrl;
	},

	getPhotoUrl: function (userNo, mode, refId, thumbCode) {
		var ctxKey = _url("file.base.url.svr");

		var strUrl = ctxKey + "/photo/" + userNo + "/" + mode + "/" + refId + ((thumbCode == null || thumbCode == "") ? "" : "_" + thumbCode);
		return strUrl;
	},
	getProfileImgUrl: function (userId, thumbCode) {
		var ctxKey = "file.download.profile.url";
		return this.getImgDownloadUrl(ctxKey, userId, thumbCode);
	},
	getContactImgUrl: function (addrId, thumbCode) {
		var ctxKey = "file.download.contact.url";

		return this.getImgDownloadUrl(ctxKey, addrId, thumbCode);
	},
	getSocialImgUrl: function (userId) {
		var ctxKey = "file.download.socialcard.url";

		return this.getImgDownloadUrl(ctxKey, userId, "");
	},
	getTempImgUrl: function (fileId, thumbCode) {
		var ctxKey = "file.download.temp.url";
		return this.getImgDownloadUrl(ctxKey, fileId, thumbCode);
	},
	getDefaultImgCnt: function () {
		return 4;
	},
	getDefaultThumbUrl: function (num) {
		var svcType = this.isKLounge() ? "klounge" : "fb";
		return this.addResHomePath("images", "default_thumb0" + num + ".png");
	},
	getSound: function (no) {
		return Utils.addContext(_url("sound.path") + "sound" + no + ".ogg");
	},

	$ce: function (tagName, idName, className, styleObj) {
		var el = document.createElement(tagName);
		if (idName && idName.length > 0) {
			el.id = idName;
		}
		if (className && className.length > 0) {
			el.className = className;
		}
		if (styleObj) { // json type check
			try {
				$(el).css(styleObj);
			} catch (e) {}
		}
		return el;
	},

	requestSetBody: function (url, type, data, onsuccess, onfailure, oncreate, oncomplete) {
		var contentType = "application/json; charset=utf-8";
		this.request(url, type, data, onsuccess, onfailure, oncreate, oncomplete, contentType)
	},

	request: function (url, type, data, onsuccess, onfailure, oncreate, oncomplete, contentType) {

		var jQueryReq = $.ajax({
			type: 'post',
			url: url,
			data: data,
			async: true,
			cache: false,
			dataType: type,
			//success : onsuccess,
			success: onsuccess,
			beforeSend: oncreate,
			error: onfailure,
			complete: oncomplete,
			contentType: contentType
		});

		if (typeof ("contentType") != "undefined" && contentType != null) {
			jQueryReq.contentType = contentType;
		}
		// jQueryReq.abort()를 호출하면 request를 강제로 끊을 수 있다.
		return jQueryReq;
	},
	getDateFormat: function (txt, flag, yearCnt) {
		var firstCnt = yearCnt || 4;
		var sep = Object.extend(["-", ", ", ":"], flag);
		var year, month, day, hour, min, sec, retStr;

		year = txt.substring(0, firstCnt);
		month = txt.substring(firstCnt, firstCnt + 2);
		if (txt.length > (firstCnt + 2)) day = txt.substring(firstCnt + 2, firstCnt + 4);
		if (txt.length > (firstCnt + 4)) hour = txt.substring(firstCnt + 4, firstCnt + 6);
		if (txt.length > (firstCnt + 6)) min = txt.substring(firstCnt + 6, firstCnt + 8);
		// if(txt.length>(firstCnt+8)) sec=txt.substring(firstCnt+8, firstCnt+10);

		retStr = year + sep[0] + month + ((!day) ? "" : sep[0] + day + ((!hour) ? "" : sep[1] + hour + ((!min) ? "" : sep[2] + min + ((!sec) ? "" : sep[2] + sec))));

		return retStr;
	},
	getCutString: function (str, len, sep) {
		if (!sep) sep = "";
		return str.cut(len, sep);
	},
	$clone: function (container, target, flag) {
		var cloneEl = null;
		var targetBase = $(container);
		if (targetBase) {
			var targetElement = $(target, targetBase).get(0);
			if (targetElement) {
				cloneEl = targetElement.cloneNode(flag);
			}
		}
		return cloneEl;
	},
	xmlToJson: function (xml) {
		var convert = function (xml, tab) {
			var X = {
				toObj: function (xml) {
					var o = {};
					if (xml.nodeType == 1) { // element node ..
						if (xml.attributes.length) // element with attributes  ..
							for (var i = 0; i < xml.attributes.length; i++)
								o["@" + xml.attributes[i].nodeName] = (xml.attributes[i].nodeValue || "").toString();
						if (xml.firstChild) { // element has child nodes ..
							var textChild = 0,
								cdataChild = 0,
								hasElementChild = false;
							for (var n = xml.firstChild; n; n = n.nextSibling) {
								if (n.nodeType == 1) hasElementChild = true;
								else if (n.nodeType == 3 && n.nodeValue.match(/[^ \f\n\r\t\v]/)) textChild++; // non-whitespace text
								else if (n.nodeType == 4) cdataChild++; // cdata section node
							}
							if (hasElementChild) {
								if (textChild < 2 && cdataChild < 2) { // structured element with evtl. a single text or/and cdata node ..
									X.removeWhite(xml);
									for (var n = xml.firstChild; n; n = n.nextSibling) {
										if (n.nodeType == 3) o["#text"] = X.escape(n.nodeValue); // text node
										else if (n.nodeType == 4) o["#cdata"] = X.escape(n.nodeValue); // cdata node
										else if (o[n.nodeName]) { // multiple occurence of element ..
											if (o[n.nodeName] instanceof Array) o[n.nodeName][o[n.nodeName].length] = X.toObj(n);
											else o[n.nodeName] = [o[n.nodeName], X.toObj(n)];
										} else o[n.nodeName] = X.toObj(n); // first occurence of element..
									}
								} else { // mixed content
									if (!xml.attributes.length) o = X.escape(X.innerXml(xml));
									else o["#text"] = X.escape(X.innerXml(xml));
								}
							} else if (textChild) { // pure text
								if (!xml.attributes.length) o = X.escape(X.innerXml(xml));
								else o["#text"] = X.escape(X.innerXml(xml));
							} else if (cdataChild) { // cdata
								if (cdataChild > 1) o = X.escape(X.innerXml(xml));
								else
									for (var n = xml.firstChild; n; n = n.nextSibling)
										o["#cdata"] = X.escape(n.nodeValue);
							}
						}
						if (!xml.attributes.length && !xml.firstChild) o = null;
					} else if (xml.nodeType == 9) { // document.node
						o = X.toObj(xml.documentElement);
					} else alert("unhandled node type: " + xml.nodeType);
					return o;
				},
				toJson: function (o, name, ind) {
					var json = name ? ("\"" + name + "\"") : "";
					if (o instanceof Array) {
						for (var i = 0, n = o.length; i < n; i++) o[i] = X.toJson(o[i], "", ind + "\t");
						json += (name ? ":[" : "[") + (o.length > 1 ? ("\n" + ind + "\t" + o.join(",\n" + ind + "\t") + "\n" + ind) : o.join("")) + "]";
					} else if (o == null)
						json += (name && ":") + "null";
					else if (typeof (o) == "object") {
						var arr = [];
						for (var m in o) arr[arr.length] = X.toJson(o[m], m, ind + "\t");
						json += (name ? ":{" : "{") + (arr.length > 1 ? ("\n" + ind + "\t" + arr.join(",\n" + ind + "\t") + "\n" + ind) : arr.join("")) + "}";
					} else if (typeof (o) == "string") json += (name && ":") + "\"" + o.toString() + "\"";
					else json += (name && ":") + o.toString();
					return json;
				},
				innerXml: function (node) {
					var s = "";
					if ("innerHTML" in node) s = node.innerHTML;
					else {
						var asXml = function (n) {
							var s = "";
							if (n.nodeType == 1) {
								s += "<" + n.nodeName;
								for (var i = 0; i < n.attributes.length; i++)
									s += " " + n.attributes[i].nodeName + "=\"" + (n.attributes[i].nodeValue || "").toString() + "\"";
								if (n.firstChild) {
									s += ">";
									for (var c = n.firstChild; c; c = c.nextSibling)
										s += asXml(c);
									s += "</" + n.nodeName + ">";
								} else s += "/>";
							} else if (n.nodeType == 3) s += n.nodeValue;
							else if (n.nodeType == 4) s += "<![CDATA[" + n.nodeValue + "]]>";
							return s;
						};
						for (var c = node.firstChild; c; c = c.nextSibling) s += asXml(c);
					}
					return s;
				},
				escape: function (txt) {
					return txt.replace(/[\\]/g, "\\\\")
						.replace(/[\"]/g, '\\"')
						.replace(/[\n]/g, '\\n')
						.replace(/[\r]/g, '\\r');
				},
				removeWhite: function (e) {
					e.normalize();
					for (var n = e.firstChild; n;) {
						if (n.nodeType == 3) { // text node
							if (!n.nodeValue.match(/[^ \f\n\r\t\v]/)) { // pure whitespace text node
								var nxt = n.nextSibling;
								e.removeChild(n);
								n = nxt;
							} else n = n.nextSibling;
						} else if (n.nodeType == 1) { // element node
							X.removeWhite(n);
							n = n.nextSibling;
						} else n = n.nextSibling; // any other node

					}
					return e;
				}
			};
			if (xml.nodeType == 9) // document node
				xml = xml.documentElement;
			var json = X.toJson(X.toObj(X.removeWhite(xml)), xml.nodeName, "\t");
			return "{\n" + tab + (tab ? json.replace(/\t/g, tab) : json.replace(/\t|\n/g, "")) + "\n}";
		}

		var jsonData = eval('(' + convert(xml, "") + ')');
		var key = Object.keys(jsonData)[0];
		return jsonData[key];
	},
	getPhoneFormat: function (txt, flag) {
		if (!flag) flag = "-";
		var len = txt.length;
		var strNo = "";

		if (!txt.isNum()) {
			return txt;
		}

		if (len >= 11) {
			strNo = txt.substr(0, 3) + flag + txt.substr(3, 4) + flag + txt.substr(7);

		} else if (len == 10) {
			if (txt.substr(0, 2) == "02") {
				strNo = txt.substr(0, 2) + flag + txt.substr(2, 4) + flag + txt.substr(6);
			} else {
				strNo = txt.substr(0, 3) + flag + txt.substr(3, 3) + flag + txt.substr(6);
			}
		} else if (len == 9) {
			strNo = txt.substr(0, 2) + flag + txt.substr(2, 3) + flag + txt.substr(5);
		} else {
			strNo = txt;
		}

		return strNo;
	},
	flashLoad: function (file, width, height) {
		//
	},
	escapeString: function (str) {
		return str.escape();
	},

	unescapeString: function (str) {
		return str.unescape();
	},
	escapeLinkString: function (str) {
		function replaceURLWithHTMLLinks(text) {

			var isWWW = text.indexOf("www.") > -1 ? true : false;
			if (isWWW) text = text.replace("www.", "http://www.")

			var re = /(\(.*?)?\b((?:https?|ftp|file):\/\/[-a-z0-9+&@#\/%?=~_()|!:,.;]*[-a-z0-9+&@#\/%=~_()|])/ig;
			return text.replace(re, function (match, lParens, url) {
				var rParens = '';
				lParens = lParens || '';

				// Try to strip the same number of right parens from url
				// as there are left parens.  Here, lParenCounter must be
				// a RegExp object.  You cannot use a literal
				//     while (/\(/g.exec(lParens)) { ... }
				// because an object is needed to store the lastIndex state.
				var lParenCounter = /\(/g;
				while (lParenCounter.exec(lParens)) {
					var m;
					// We want m[1] to be greedy, unless a period precedes the
					// right parenthesis.  These tests cannot be simplified as
					//     /(.*)(\.?\).*)/.exec(url)
					// because if (.*) is greedy then \.? never gets a chance.
					if (m = /(.*)(\.\).*)/.exec(url) ||
						/(.*)(\).*)/.exec(url)) {
						url = m[1];
						rParens = m[2] + rParens;
					}
				}
				var hrefLink = url;
				if (isWWW) url = url.replace("http://", "");

				return lParens + "<a class='link' target='_blank' href='" + hrefLink + "'>" + url + "</a>" + rParens;
			});
		}

		var bf = str.escape();
		var result = replaceURLWithHTMLLinks(bf);
		return result;
	},

	checkUrl: function (strUrl) {
		var string = strUrl.toLowerCase();
		if (string.indexOf('http://') < 0) {
			string = 'http://' + string;
			strUrl = 'http://' + strUrl;
		}
		if (string.search(/^[a-zA-Z0-9\-\.]+\.(com|org|net|mil|edu|kr)/) != -1) {
			return strUrl;
		}

		if (string.search(/^[http:\/\/]+[a-zA-Z0-9\-\.]+\.(com|org|net|mil|edu|kr)/) != -1) {
			return strUrl;
		}

		return false;
	},
	Cookie: {
		get: function (name) {
			var start = document.cookie.indexOf(name + "=");
			var len = start + name.length + 1;
			if ((!start) && (name != document.cookie.substring(0, name.length))) {
				return null;
			}
			if (start == -1) return null;
			var end = document.cookie.indexOf(";", len);
			if (end == -1) end = document.cookie.length;

			return unescape(document.cookie.substring(len, end));
		},
		set: function (name, value) {
			this.remove(name);

			var today = new Date();
			today.setTime(today.getTime());
			var expires_date = new Date(today.getTime() + 63072000000);
			var domainName = location.hostname.substring(location.hostname.indexOf(".") + 1, location.hostname.length);
			document.cookie = name + "=" + escape(value) + ";expires=" + expires_date.toGMTString() + ";domain=" + domainName + ";path=" + "/";
		},

		remove: function (name) {
			if (this.get(name)) {
				var expireDate = new Date();
				expireDate.setDate(expireDate.getDate() - 1);
				document.cookie = name + "=" + " ; expires=" + expireDate.toGMTString() + "; path=" + "/";
			}
		}
	},

	Local: {
		get: function (key) {
			return localStorage.getItem(key);
		},
		set: function (key, val) {
			localStorage.setItem(key, val);
		},
		remove: function (key) {
			localStorage.removeItem(key);
		},
		clear: function () {
			localStorage.clear();
		}
	},

	Session: {
		get: function (key) {
			return sessionStorage.getItem(key);
		},
		set: function (key, val) {
			sessionStorage.setItem(key, val);
		},
		remove: function (key) {
			sessionStorage.removeItem(key);
		},
		clear: function () {
			sessionStorage.clear();
		}
	},

	Notification: {
		PERMISSION_ALLOWED: 0,
		PERMISSION_NOT_ALLOWED: 1,
		PERMISSION_DENIED: 2,
		support: function () {
			// 0-NOT SUPPORT, 1-SUPPORT, 2- DENYED
			var status = 0;

			/***
			 	permission
				0: 승인 됨, PERMISSION_ALLOWED
				1: 승인 하지 않음, 승인 전, unknown, PERMISSION_NOT_ALLOWED
				2: 거부 됨, PERMISSION_DENIED
			***/

			if ("Notification" in window) {

			} else if (Notification.permission === "granted") {

			} else {

			}
		},

		send: function (msg) {
			var notifi
		}

	},

	getDevice: function () {
		var deviceId = Utils.Local.get("deviceid");
		if (typeof (deviceId) == "undefined" || deviceId == null || deviceId == "") {
			var uuid = Utils.createUUID();
			deviceId = uuid.substring(0, 10);

			Utils.Local.set("deviceid", deviceId);
		}

		return deviceId;
	},

	getElementsByClass: function (searchClass, node, tag) {
		var classElements = new Array();
		if (node == null)
			node = document;
		if (tag == null)
			tag = '*';
		var els = node.getElementsByTagName(tag);
		var elsLen = els.length;
		var pattern = new RegExp('(^|\\s)' + searchClass + '(\\s|$)');
		for (i = 0, j = 0; i < elsLen; i++) {
			if (pattern.test(els[i].className)) {
				classElements[j] = els[i];
				j++;
			}
		}
		return classElements;
	},
	getiddomain: function (str) {
		var idx_start = str.indexOf("@");
		idx_start = (idx_start == -1) ? str.indexOf(".") : idx_start;
		var idx_end = str.lastIndexOf(".");
		var id = str.substring(0, idx_start).toLowerCase();
		var domain = str.substring(idx_start + 1, idx_end).toLowerCase();
		return [id, domain];
	},
	autofitsize: function () {
		window.resizeTo(455, 400);
		var winW, winH, sizeToW, sizeToH;
		if (parseInt(navigator.appVersion) > 3) {
			if (navigator.appName == "Netscape") {
				winW = window.innerWidth;
				winH = window.innerHeight;
			}
			if (navigator.appName.indexOf("Microsoft") != -1) {
				winW = document.body.scrollWidth;
				winH = document.body.scrollHeight;
			}
		}
		sizeToW = 0;
		sizeToH = 0;
		if (winW > 1024) { //1024은 제한하고자 하는 가로크기
			sizeToW = 1024 - document.body.clientWidth;
		} else if (Math.abs(document.body.clientWidth - winW) > 3) {
			sizeToW = winW - document.body.clientWidth;
		}
		if (winH > 768) { //768은 제한하고자 하는 세로크기
			szeToH = 768 - document.body.clientHeight;
		} else if (Math.abs(document.body.clientHeight - winH) > 4) {
			sizeToH = winH - document.body.clientHeight;
		}
		if (sizeToW != 0 || sizeToH != 0) {
			window.resizeBy(sizeToW, sizeToH);
		}
	},
	getTextEditor: function (targetTextNode, maxLen, callback, txtBoxLen, cutLen, txtBoxReSize) {
		var obj = this;
		var editorOption = {
			targetObj: targetTextNode,
			dblClick: false,
			click: false,
			title: "",
			maxLen: maxLen,
			txtBoxLen: (txtBoxLen || 50),
			cutLen: (cutLen || 44),
			cutStr: true,
			onChange: function (txt, isChanged) {
				callback(txt, isChanged);
			},
			txtBoxReSize: true
		};

		var textObj = NewTextEditor(targetTextNode, editorOption);
		textObj.setup();

		return textObj;
	},
	checkAll: function (checkNode, container) {
		var item = $(container).get(0);
		var checkboxItem = item.getElementsByTagName("input");
		for (var i = 0; i < checkboxItem.length; i++) {
			// 2014.08.13 disabled false 추가
			if (checkboxItem[i].type == "checkbox" && !checkboxItem[i].disabled) {
				checkboxItem[i].checked = checkNode.checked;
				// checkboxItem[i].setAttribute("checked", checkNode.checked);
			}
		}
	},
	ajaxloading: function (layer, options) {
		var target = $(layer);
		var options = options || {};
		var cls = (options.cls) ? options.cls : "loading.normal.white";
		var width = (options.width) ? (options.width + "px") : "100%";
		var height = (options.height) ? (options.height + "px") : (target.height() - 4) + "px";

		if (options.timer) {
			var timer = null;
			var drawFunc = null;
			try {
				drawFunc = function () {
					target.innerHTML = "<div class=\"" + _msg(cls) + "\" style=\"width:" + width + ";height:" + height + "; \">&nbsp;</div>";
					clearTimeout(timer);
				};
				timer = window.setTimeout(drawFunc, "1000");
			} catch (e) {
				clearTimeout(timer);
			} finally {
				drawFunc = null;
			}
		} else {
			var txt = "<div class=\"" + _msg(cls) + "\" style=\"width:" + width + ";height:" + height + "; \">&nbsp;</div>";
			target.html(txt);
		}
	},
	cancelBubble: function (e) {
		if (!e) var e = window.event;
		e.cancelBubble = true;
		if (e.stopPropagation) e.stopPropagation();
	},
	getElementByEvent: function (e) {
		if (!e) return null;
		return e.srcElement ? e.srcElement : e.target;
	},
	copyText: function (txt) {
		var IE = (window.clipboardData) ? true : false;
		if (IE) {
			window.clipboardData.setData("Text", txt);
			alert(_msg("url.copy.success"));
		} else {
			alert(_msg("url.copy.prompt"));
			prompt("", txt);
		}
	},
	roundXL: function (n, digits) {
		if (digits >= 0) {
			return parseFloat(n.toFixed(digits)); //소수부 반올림
		}
		digits = Math.pow(10, digits); //정수부 반올림
		var t = Math.round(n * digits) / digits;
		return parseFloat(t.toFixed(0));
	},
	convertFileSize: function (fileSize, cnt) {
		var ret = "";
		try {
			var str_list = ["bytes", "KB", "MB", "GB", "TB"];
			var size = parseFloat(fileSize);
			var idx = 0;
			while (size > 1024 && idx < cnt && idx < str_list.length) {
				size = size / 1024.0;
				idx++;
			}
			size = this.roundXL(size, 2);
			ret = size + " " + str_list[idx];
		} catch (e) {
			ret = fileSize;
		}
		return ret;
	},
	getFileExt: function (fileName) {
		var fileLen = fileName.length;
		var lastDotIndex = fileName.lastIndexOf('.');
		var ext = fileName.substring(lastDotIndex + 1, fileLen);

		return ext.toLowerCase();
	},
	generateServiceUrl: function (host, type, paramList) {
		var isRewrite = Utils.isRewrite;

		var url = "";

		try {
			var extUrl = "/";
			var txt = "";

			if (type != null && type.trim() != '') {
				extUrl = (isRewrite) ? "/" + type : "/" + type + ".do";
			} else {
				extUrl = (isRewrite) ? "" : "/main.do";
			}

			if (paramList != null) {
				var size = paramList.length;
				for (var i = 0; i < size; i++) {
					var paramObj = paramList[i];

					for (var key in paramObj) {
						var param = paramObj[key];

						txt += (isRewrite) ? "/" : (txt.indexOf("?") != -1) ? ("&" + key + "=") : ("?" + key + "=");
						txt += param;
					}
				}
			}

			url = host + extUrl + txt;

			return url;
		} catch (e) {
			//alert(e);
		}
	},
	generateScUrl: function (id) {
		var isRewrite = Utils.isRewrite;
		var ctx = "";
		// var domain = _msg("domain.svr");
		// ctx = "social/view?id=" + id;

		var domain = _msg("social.svr");
		if (domain == "1") {
			var domain = _url("scard.domain.1");
			var fullUrl = domain + id;
			return fullUrl;
		} else { // local
			ctx = "social/view?id=" + id;
			return this.addContext(ctx);
		}

		return this.addContext(ctx);
	},

	navigation: function (url, type) {
		if (type) {
			window.open(url);
		} else {

			window.location.href = url;
		}
		return false;
	},
	viewTextLink: function (txt) {
		var rtn = "";

		txt = txt.replace(/&/g, "&amp;");
		txt = txt.replace(/</g, "&lt;");
		txt = txt.replace(/>/g, "&gt;");
		rtn = txt;

		var resultStr = "";
		var fromIndex = 0;
		if (rtn.indexOf("http://") < 0 && rtn.indexOf("www.") < 0) {
			return rtn;
		}

		while (fromIndex < rtn.length) {
			var isHttp = false;
			var linkIndexHttp = rtn.indexOf("http://", fromIndex);
			var linkIndexWww = rtn.indexOf("www.", fromIndex);
			if (linkIndexHttp < 0 && linkIndexWww < 0) {
				resultStr += rtn[fromIndex];
				fromIndex++;
				continue;
			}

			var cutIndex = 0;
			var linkStr = "";
			var startLinkIndex = 0;
			if (linkIndexHttp >= 0 && linkIndexWww >= 0) {
				startLinkIndex = linkIndexHttp < linkIndexWww ? linkIndexHttp : linkIndexWww;
				isHttp = linkIndexHttp < linkIndexWww ? true : false;
			} else {
				startLinkIndex = linkIndexHttp < 0 ? linkIndexWww : linkIndexHttp;
				isHttp = linkIndexHttp < 0 ? false : true;
			}

			for (var i = startLinkIndex; i < rtn.length; i++) {
				var ch = rtn[i];
				cutIndex = i;
				if (ch == ' ') {
					break;
				} else {
					linkStr += ch;
				}
			}

			var preLink = rtn.substring(fromIndex, startLinkIndex);
			if (isHttp) {
				resultStr += preLink + "<a href=\"" + linkStr + "\" target=\"_blank\">" + linkStr + "</a> ";
			} else {
				resultStr += preLink + "<a href=\"http://" + linkStr + "\" target=\"_blank\">" + linkStr + "</a> ";
			}

			fromIndex = cutIndex + 1;
		}

		return resultStr;
	},
	jsonToStr: function (obj) {
		var t = typeof (obj);
		if (t != "object" || obj === null) {
			// simple data type
			if (t == "string") obj = '"' + obj + '"';
			return String(obj);
		} else {
			// recurse array or object
			var n, v, json = [],
				arr = (obj && obj.constructor == Array);
			for (n in obj) {
				v = obj[n];
				t = typeof (v);
				if (t == "string") v = '"' + v + '"';
				else if (t == "object" && v !== null) v = JSON.stringify(v);
				json.push((arr ? "" : '"' + n + '":') + String(v));
			}
			return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
		}
	},

	setDefaultImg: function (element) {
		//-- 
		var imgUrl = Utils.addContext(_url('image.url'));
		var defaultImg = imgUrl + '/bg_07s.png';

		$(element).attr("src", defaultImg);
	},

	setDefaultMsgImg: function (element) {
		var imgUrl = Utils.addContext(_url('image.url'));
		var defaultImg = imgUrl + '/bg_10s.png';

		$(element).attr("src", defaultImg);
	},

	createUUID: function () {
		// http://www.ietf.org/rfc/rfc4122.txt
		var s = [];
		var hexDigits = "0123456789abcdef";
		for (var i = 0; i < 32; i++) {
			s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
		}
		s[12] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
		s[16] = hexDigits.substr((s[16] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01

		var uuid = s.join("");
		return uuid;
	},

	// 이미지로 대신 클릭	
	openFile: function (fileNodeId) {
		// -- file
		var fileNode = document.getElementById(fileNodeId);
		if (fileNode) {
			fileNode.click();
		}
	},

	skipDomainChk: function (query) {
		var skipDomain = _msg('skip.domain');
		var skipDomainArr = skipDomain.split(',');
		var skipDomainLen = skipDomainArr.length;
		var queryArr = query.split('.');
		var queryLen = queryArr.length;
		var result = false;
		for (var i = 0; i < skipDomainLen; i++) {
			for (var j = 0; j < queryLen; j++) {
				if (skipDomainArr[i].trim() == queryArr[j].trim().toLowerCase()) {
					result = true;
					break;
				} else {
					result = false;
				}
			}
			if (result) {
				break;
			}
		}
		return result;
	},
	allowSocialUrl: function (url) {
		var allowUrl = _msg('allow.url');
		var allowUrlArr = allowUrl.split(',');
		var allowUrlArrLen = allowUrlArr.length;

		var result = false;
		for (var i = 0; i < allowUrlArrLen; i++) {
			if (allowUrlArr[i].trim() == url.trim().toLowerCase()) {
				result = true;
				break;
			} else {
				result = false;
			}
		}
		return result;
	},
	sleep: function (num) {
		var now = new Date();
		var stop = now.getTime() + num;
		while (true) {
			now = new Date();
			if (now.getTime() > stop) return;
		}
	},

	checkAndSlide: function () {
		// 2012.01.05 - IE7 빠짐 
		if (jQuery.browser.msie && (jQuery.browser.version == "6.0")) {
			$("#noti").slideDown();
			var closeCallback = function () {
				clearTimeout(closeCallback);
				$("#noti").slideUp();
			}
			setTimeout(closeCallback, "5000");
		}
	},

	phoneFormat: function (str) {
		var phone = "";
		var len = str.length;
		var phone1, phone2, phone3;
		var flag = "-";

		if (len > 14) { // 국제전화번호 규격
			phone = str;
		} else if (len >= 11) {
			var idx = len == 11 ? 3 : 4;
			phone1 = str.substring(0, idx);
			phone2 = str.substring(idx, len - 4);
			phone3 = str.substring(len - 4);

			phone = phone1 + flag + phone2 + flag + phone3;

		} else if (len > 8) {
			var idx = str.indexOf("02") == 0 ? 2 : 3;
			phone1 = str.substring(0, idx);
			phone2 = str.substring(idx, len - 4);
			phone3 = str.substring(len - 4);

			phone = phone1 + flag + phone2 + flag + phone3;
		} else {
			phone = str;
		}

		return phone;
	},

	imageTimer: null,
	imageLoad: false,
	loadDelay: "200",
	imageLoadCk: function (callback) {
		// 이미지는 데이터 없는경우 반드시, onerror 처리 반드시 할것	
		if (Utils.imageTimer) {
			clearInterval(Utils.imageTimer);
			Utils.imageLoad = false;
		}

		Utils.imageTimer = setInterval(function () {
			if (Utils.imageLoad) {
				clearInterval(Utils.imageTimer);
			}

			var loadCk = Utils.checkImages();
			if (loadCk) {
				clearInterval(Utils.imageTimer);
				Utils.imageLoad = true;
				callback();
			}

		}, Utils.loadDelay);
	},
	isImageOk: function (img) {
		if (!img.complete) {
			return false;
		}
		if (typeof img.naturalWidth != "undefined" && img.naturalWidth == 0) {
			return false;
		}
		// No other way of checking: assume it’s ok.
		return true;
	},
	checkImages: function () {
		for (var i = 0; i < document.images.length; i++) {
			if (!this.isImageOk(document.images[i])) {
				return false;
			}
		}
		return true;
	},

	/** 14.08.25 byte방식에서 length 방식으로 변경 */
	textCutProcess: function (thisForm, maxText, txt, byteCkId, messageInputId) {
		var tmpStr;
		var onechar;
		var temp = 0;
		var tcount = 0;
		var aquery = thisForm.value;

		tmpStr = new String(aquery);
		temp = tmpStr.length;

		for (var k = 0; k < temp; k++) {
			onechar = tmpStr.charAt(k);
			if (escape(onechar) == '%0D') {} else if (escape(onechar).length > 4) {
				tcount += 2;
			} else {
				tcount++;
			}
		}

		if (tcount > maxText) {
			reserve = tcount - maxText;
			// alert(txt + " "+_msg('korean')+" "+parseInt(maxText/2)+_msg('word')+", "+_msg('english')+" "+maxText+_msg('word')+" "+_msg("insert limit message"));
			// alert(txt + " 최대 " + maxText + "자 까지만 가능합니다.");

			this._netsCheck(thisForm, maxText);
			return;

		}

		if (byteCkId && messageInputId) {
			this.updateTxtByte(this, byteCkId, messageInputId, maxText);
		}

	},
	_netsCheck: function (thisForm, maxText) {
		var tmpStr;
		var onechar;
		var temp = 0;
		var tcount = 0;
		var aquery = thisForm.value;

		tmpStr = new String(aquery);
		temp = tmpStr.length;

		// byte -> length
		for (var k = 0; k < temp; k++) {
			onechar = tmpStr.charAt(k);

			if (escape(onechar).length > 4) {
				tcount += 2;
			} else {
				if (escape(onechar) == '%0A') {} else {
					tcount++;
				}
			}

			if (tcount > maxText) {
				tmpStr = tmpStr.substring(0, k);
				break;
			}
		}

		/**
		for(var k=0; k<temp; k++) {
			tcount++;
			if (tcount > maxText) {
				tmpStr = tmpStr.substring(0, k);
				break;
			}
		}
		**/

		thisForm.value = tmpStr;
	},

	updateTxtByte: function (obj, byteCkId, textAreaId, maxTxt) {
		// alert('in');
		var byteCheckLayer = document.getElementById(byteCkId);
		var textArea = document.getElementById(textAreaId);
		var tmpStr;
		var onechar;
		var temp = 0;
		var tcount = 0;
		var aquery = textArea.value;
		var maxText = maxTxt ? parseInt(maxTxt, 10) : 250;

		tmpStr = new String(aquery);
		temp = tmpStr.length;

		// byte -> length
		for (var k = 0; k < temp; k++) {
			onechar = tmpStr.charAt(k);
			if (escape(onechar) == '%0D') {} else if (escape(onechar).length > 4) {
				tcount += 2;
			} else {
				tcount++;
			}
		}

		if (tcount > maxText) {
			this.overflowScrapByte = true;
			byteCheckLayer.style.color = "#FF7777";
			byteCheckLayer.style.fontColor = "#FF7777";
			byteCheckLayer.innerHTML = +tcount + "/" + maxText;
			//$(byteCheckLayer).html("<font style='font-size: 1em;color:#FF7777'>" + len + "</font>");
		} else {
			byteCheckLayer.style.color = "";
			byteCheckLayer.style.fontColor = "";
			this.overflowScrapByte = false;
			byteCheckLayer.innerHTML = tcount + "/" + maxText;
		}
	},

	// bootstrap select function 
	changeSel: function (key, value, targetId, thisNode) {

		$("li", $(thisNode).parent()).removeClass("active");
		$(thisNode).addClass("active");

		var seltxt = document.getElementById(targetId);
		seltxt.setAttribute("key", key);
		seltxt.innerHTML = value;
	},

	getDate: function (date) {
		function pad(num) {
			num = num + '';
			return num.length < 2 ? '0' + num : num;
		}
		return date.getFullYear() + pad(date.getMonth() + 1) + pad(date.getDate()) + pad(date.getHours()) + pad(date.getMinutes()) + pad(date.getSeconds());
	},

	browser: function (browserNm) {
		var browser = (function () {
			var s = navigator.userAgent.toLowerCase();
			var match = /(webkit)[ \/](\w.]+)/.exec(s) ||
				/(opera)(?:.*version)?[ \/](\w.]+)/.exec(s) ||
				/(msie) ([\w.]+)/.exec(s) ||
				/(mozilla)(?:.*? rv:([\w.]+))?/.exec(s) || [];
			return {
				name: match[1] || "",
				version: match[2] || "0"
			};
		}());

		// ie11 체크
		if (navigator.userAgent.toLowerCase().search("trident") != -1 || navigator.userAgent.toLowerCase().search("msie") != -1 ||
			navigator.userAgent.toLowerCase().search("edge") != -1) {
			browser.name = "msie";
		} else if (navigator.userAgent.toLowerCase().search("chrome") != -1) {
			browser.name = "chrome";
		} else if (navigator.userAgent.toLowerCase().search("safari") != -1) {
			browser.name = "safari";
		} else if (navigator.userAgent.toLowerCase().search("opera") != -1) {
			browser.name = "opera";
		}

		return (browser.name == browserNm);
	},

	mobile: function () {
		if (navigator.userAgent.match(/Android/i) ||
			navigator.userAgent.match(/webOS/i) ||
			navigator.userAgent.match(/iPhone/i) ||
			navigator.userAgent.match(/iPad/i) ||
			navigator.userAgent.match(/iPod/i) ||
			navigator.userAgent.match(/BlackBerry/i) ||
			navigator.userAgent.match(/Windows Phone/i)) {
			return true;
		} else {
			return false;
		}
	},

	setCordova: function (flag) {
		this.isCordova = flag;
	},

	cordova: function () {
		return this.isCordova;
	},

	resize: function (fn, readyFn, timeout) {
		if (!fn || typeof fn != 'function') return 0;

		if (typeof (timeout) == "undefined" || timeout == null) timeout = 1000;

		var args = Array.prototype.slice.call(arguments, 2);

		Utils.resize.fnArr = Utils.resize.fnArr || [];
		Utils.resize.fnArr.push([fn, args]);
		Utils.resize.loop = function () {
			$.each(Utils.resize.fnArr, function (index, fnWithArgs, data) {
				// fnWithArgs[1].apply(undefined);
				fnWithArgs[0].apply(undefined, fnWithArgs[1]);
			});
		};

		// 이곳 resize에서는 window가 이동중일때는 skip할수 있는 로직을 넣는다.	
		$(window).on('resize', function (e) {
			// readyfunction이 존재시 미리 실행.
			if (readyFn) readyFn();

			window.clearTimeout(Utils.resize.timeout);
			Utils.resize.timeout = window.setTimeout(Utils.resize.loop, timeout);
		});
	},

	resizeAfterMove: function (fn, readyFn, timeout) {
		if (!fn || typeof fn != 'function') return 0;

		if (typeof (timeout) == "undefined" || timeout == null) timeout = 1000;

		// fixCnt = not move count
		var screenX = 0,
			screenY = 0,
			fixCnt = 0;
		var timer = null;

		// window가 move중이면 skip 하기 위해 체크
		Utils.resizeAfterMove.checkPosition = function () {
			function getSrceenPosition() {
				this.x = window.screenLeft;
				this.y = window.screenTop;
				return this;
			}
			var pos = getSrceenPosition();
			if (screenX != pos.x || screenY != pos.y) {
				fixCnt = 0;
				// onResize();
				screenX = pos.x;
				screenY = pos.y;
			} else {
				fixCnt++;
			}

			if (fixCnt > 1) Utils.resizeAfterMove.onResize();
		};

		Utils.resizeAfterMove.onResize = function () {
			// if(readyFn) readyFn();
			if (fn) fn();

			clearInterval(timer);
			timer = null;
		};

		$(window).on('resize', function (e) {
			// readyfunction이 존재시 미리 실행.

			if (readyFn) readyFn();

			if (timer != null) return;
			timer = window.setInterval(Utils.resizeAfterMove.checkPosition, timeout);

		});
	},

	checkConnectionState: function () {
		var result;
		var url = Utils.addContext('checkConnection.json');

		$.ajax({
			type: 'post',
			url: url,
			async: true,
			cache: false,
			dataType: 'json',
			data: null,
			beforeSend: function () {},
			success: function (data) {
				if (data.code == "200")
					result = true;
				else
					result = false;
				console.log(result);
			},
			error: function () {
				Utils.log("error..")
			},
			complete: function () {
				Utils.log("complete..")
			}
		});

		return result;
	},

	getParams: function () {
		var vars = {};
		var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
			function (m, key, value) {
				vars[key] = value;
			});
		return vars;
	},

	runCallback: function (callback, time) {
		if (time == null) time = 100;

		// setImmediate IE만 지원하는 메소드로서 바로 
		if (typeof (setImmediate) == "function") {
			setImmediate(callback);
		} else {
			setTimeout(callback, time);
		}
	},

	checkPlatform: function () {
		if (typeof (cordova) == "undefined") return "web";
		else return cordova.platformId;
	},
	// 앱 전용 Global Function
	append: function (loc, tag, str) {
		var temp = document.createElement(tag);
		temp.innerHTML = str;
		loc.appendChild(temp);
	},

	nslog: function (obj) {
		if (cordova.platformId == "ios")
			cordova.exec(null, null, "CommonPlugin", "pluginLog", obj);
	},

	parseQueryString: function (queryString) {
		var qs = decodeURIComponent(queryString),
			obj = {},
			params = qs.split('&');
		params.forEach(function (param) {
			var splitter = param.split('=');
			obj[splitter[0]] = splitter[1];
		});
		return obj;
	},

	toQueryString: function (obj) {
		var parts = [];
		for (var i in obj) {
			if (obj.hasOwnProperty(i)) {
				parts.push(encodeURIComponent(i) + "=" + encodeURIComponent(obj[i]));
			}
		}
		return parts.join("&");
	},

	getDeviceID: function () {
		var deviceId = Utils.Local.get("deviceid");
		if (typeof (deviceId) == "undefined" || deviceId == null || deviceId == "") {
			deviceId = Utils.createUUID();
			Utils.Local.set("deviceid", deviceId);
		}
		return deviceId;
	},
	
	getResizeThumbnail : function(link, size){
		if(link.indexOf("google") > -1){
			var orgLink =  link.substring(0, link.indexOf("?")-1);
			link = orgLink + "?sz=" + size;
		}else if(link.indexOf("facebook") > -1){
			// width
			link += "?width=" + size;
		}
		
		return link;		
	}
};