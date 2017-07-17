var ggauth = {

    client_id:'',
    client_secret:'',
    scope: '',
    redirect_uri: '',

    init: function(options) {
        this.client_id = options.client_id;
        this.client_secret = options.client_secret;
        this.scope = options.scope;
        this.redirect_uri = options.redirect_uri;
    },

    login: function(myCallback) {

        var authUrl = 'https://accounts.google.com/o/oauth2/auth?' + $.param({
            client_id: this.client_id,
            redirect_uri: this.redirect_uri,
            response_type: 'code',
            scope: this.scope
        });

        var authWindow = window.open(authUrl, '_blank', 'location=no,toolbar=no, clearcache=yes');

        authWindow.addEventListener('loadstart', googleCallback);

        function googleCallback(e){
            //e.url = authUrl;
            var url = (typeof e.url !== 'undefined' ? e.url : e.originalEvent.url);
            var code = /\?code=(.+)$/.exec(url);
            var error = /\?error=(.+)$/.exec(url);

            if (code || error) {
                authWindow.close();
            }

            if (code) {
                $.post('https://accounts.google.com/o/oauth2/token', {
                    code: code[1],
                    client_id: ggauth.client_id,
                    client_secret: ggauth.client_secret,
                    redirect_uri: ggauth.redirect_uri,
                    grant_type: 'authorization_code'
                }).done(function(data) {
                    myCallback(data.access_token);
                }).fail(function(response) {
                    deferred.reject(response.responseJSON);
                });
            } else if (error) {
                //The user denied access to the app
                alert(error);
            }
        }

    }
};