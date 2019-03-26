$(document).ready(function(){
    var host = location.href.replace(/http:\/\//i,"");

    window.CHAT = {
        nickName : null,
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
                    //向服务端发送自己的登录身份信息
                    CHAT.socket.send("[LOGIN][" + new Date().getTime() + "][" + CHAT.nickName +"]");
                };

                //获得消息事件
                CHAT.socket.onmessage = function (msg) {
                    //服务端返回的消息,将消息显示在文本域
                   $("#msgArea").append(msg.data + "\r\n");
                };

                //关闭事件
                CHAT.socket.onclose = function () {
                    $("#msgArea").append("[SYSTEM][" + new Date().getTime() + "][0]-服务器关闭，暂时无法聊天" +"\r\n");
                };

                //发生了错误事件
                CHAT.socket.onerror = function () {
                    $("#msgArea").append("socket发生错误！" + "\r\n");
                }
            }
        },

        login : function () {
            //将登陆页面隐藏，显示聊天页面
            $("#loginDiv").hide();
            $("#chatDiv").show();

            //将用户名称显示
            CHAT.nickName = $("#username").val();
            $("#userNameSpan").text(CHAT.nickName)

            //开启websocket
            CHAT.init();

        },
        logout : function () {
            CHAT.socket.send("[LOGOUT][" + new Date().getTime() + "][" + CHAT.nickName +"]");
            //退出逻辑
            window.location.reload();
            //退出关闭websocket
            CHAT.socket.close();
        },
        sendMsg : function () {
            CHAT.socket.send("[CHAT][" + new Date().getTime() + "][" + CHAT.nickName +"]-" + $("#myMsg").val());
            //清空文本框
            $("#myMsg").val("");
        }
    }
});