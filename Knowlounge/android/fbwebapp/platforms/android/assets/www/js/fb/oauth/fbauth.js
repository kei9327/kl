var fbauth = (function () {

    var fbAppId, scope, cordovaOAuthRedirectURL;

    function init(params) {
        fbAppId = params.client_id;
        fbauth.scope = params.scope;
        cordovaOAuthRedirectURL = params.redirect_uri;
    }

    function login(callback) {
        var loginWindow,
            startTime,
            scope = fbauth.scope,
            redirectURL = cordovaOAuthRedirectURL;

        if (!fbAppId) {
            return callback({status: 'unknown', error: 'Facebook App Id not set.'});
        }

        startTime = new Date().getTime();
        loginWindow = window.open('https://www.facebook.com/dialog/oauth?client_id=' + fbAppId + '&redirect_uri=' + redirectURL + '&response_type=token&scope=' + scope, '_blank', 'location=no,clearcache=yes');

        loginWindow.addEventListener('loadstart', loginWindow_loadStartHandler);

        function loginWindow_loadStartHandler(event) {
            var queryString, obj;

            if (event.url.indexOf("access_token=") > 0 || event.url.indexOf("error=") > 0) {
                // When we get the access token fast, the login window (inappbrowser) is still opening with animation
                // in the Cordova app, and trying to close it while it's animating generates an exception. Wait a little...
                var timeout = 600 - (new Date().getTime() - startTime);
                setTimeout(function () {
                    loginWindow.close();
                }, timeout > 0 ? timeout : 0);

                if (event.url.indexOf("access_token=") > 0) {
                    queryString = event.url.substr(event.url.indexOf('#') + 1); //myUtils에 있는 함수
                    obj = parseQueryString(queryString);
                    if (callback) callback({status: 'connected', accessToken: obj['access_token']});
                } else if (event.url.indexOf("error=") > 0) {
                    queryString = event.url.substring(event.url.indexOf('?') + 1, event.url.indexOf('#'));
                    obj = parseQueryString(queryString);
                    if (callback) callback({status: 'not_authorized', error: obj.error});
                } else {
                    if (callback) callback({status: 'not_authorized'});
                }
            }
        }
    }

    return {
        init: init,
        login: login
    }

}());
