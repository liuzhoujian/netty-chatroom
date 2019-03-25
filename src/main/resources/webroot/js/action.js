$(document).ready(function(){
    var host = location.href.replace(/http:\/\//i,"");

    window.CHAT = {
        serverAddr : "ws://" + host + "im",
        socket : null,
        init : function () {
            if (typeof (WebSocket) == "undefined") {
                console.log("您的浏览器不支持WebSocket");
            } else {
                console.log("您的浏览器支持WebSocket");

                CHAT.socket = new WebSocket(CHAT.serverAddr);

                //打开事件
                CHAT.socket.onopen = function () {
                    console.log("Socket 已打开");
                    //TODO 向服务端发送自己的登录身份信息
                    CHAT.socket.send("[LOGIN][" + new Date().getTime() + "][" + $("#username").val() +"]");
                };
                //获得消息事件
                CHAT.socket.onmessage = function (msg) {
                    //服务端返回的消息
                    console.log("来自服务端的消息：" + msg.data);
                };
                //关闭事件
                CHAT.socket.onclose = function () {
                    console.log("Socket已关闭");
                };

                //发生了错误事件
                CHAT.socket.onerror = function () {
                    console.log("Socket发生了错误");
                }
            }
        },

        login : function () {
            //将登陆页面隐藏，显示聊天页面
            $("#loginDiv").hide();
            $("#chatDiv").show();

            //将用户名称显示
            var username = $("#username").val();
            $("#userNameSpan").text(username)

            //开启websocket
            CHAT.init();

        },
        logout : function () {
            //TODO 退出前向服务端发送消息

            //退出逻辑
            window.location.reload();
            //退出关闭websocket
            window.CHAT.socket.close();
        }
    }
});