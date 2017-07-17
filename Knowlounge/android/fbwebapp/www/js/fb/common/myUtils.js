function append(loc, tag, str){
    var temp = document.createElement(tag);
    temp.innerHTML = str;
    loc.appendChild(temp);
}

function parseQueryString(queryString) {
    var qs = decodeURIComponent(queryString),
    obj = {},
    params = qs.split('&');
    params.forEach(function (param) {
                   var splitter = param.split('=');
                   obj[splitter[0]] = splitter[1];
                   });
    return obj;
}

function toQueryString(obj) {
    var parts = [];
    for (var i in obj) {
        if (obj.hasOwnProperty(i)) {
            parts.push(encodeURIComponent(i) + "=" + encodeURIComponent(obj[i]));
        }
    }
    return parts.join("&");
}

function getDeviceID(){
    var deviceId = Utils.Local.get("deviceid");
    if(typeof(deviceId) == "undefined" || deviceId == null || deviceId == ""){
        deviceId = Utils.createUUID();
        Utils.Local.set("deviceid", deviceId);
    }
    return deviceId;
}

var videoMapForiOS;