function indexStart(){

    //alert("start");

    document.getElementById('fbLogin').onclick = Auth.loginF;
    document.getElementById('ggLogin').onclick = Auth.loginG;
    document.getElementById('signout').onclick = signOut;
    document.getElementById('init').onclick = storageInit;
    document.getElementById('getUserInfo').onclick = Auth.getUserInfo(function(result){
    	alert(JSON.stringify(result));
    });
    
    document.getElementById('test').onclick = test;
    document.getElementById('google').onclick = goGoogle;

    Auth.init();

}

function login(){
    if(event.target.id == 'fbLogin'){
        Auth.loginF(showGreetView);
    }
    else if(event.target.id == 'ggLogin'){
        Auth.loginG(showGreetView);
    }

}

function storageInit(){
    localStorage.clear();
    Auth.masterCookie = '';
    Auth.checksumCookie = '';
    showLoginView();
    alert("init");
}

function signOut(){
    storageInit();
}

function showLoginView(){
    $('#greet').hide();
    $('#login').show();
}

function showGreetView(){
    $('#login').hide();
    $('#greet').show();
}

function test(){
    Auth.test();
}
function goGoogle(){
    window.open('http://www.google.com', '_blank', 'location=no,toolbar=no');
}


function cookie2roomlist(){
    var svrFlag = _prop('svr.flag');
    var host = _prop('svr.host.' + svrFlag);
    var urlList = [_prop('my.list'), _prop('history.list'), _prop('bookmark.list')];

    var text = "FBMMC="+Auth.masterCookie+"&FBMCS="+Auth.checksumCookie;

    for(var i=0; i<3; i++){
        $.ajax({
            type:"POST",
            async: false,
            beforeSend: function (request){request.setRequestHeader('Authorization', text);},
            url: host + urlList[i],
            success: function(data) {alert(JSON.stringify(data));},// resetRoom(i, data.list);},
            error: function(error){alert("err: "+JSON.stringify(error));}
        });
    }
}
