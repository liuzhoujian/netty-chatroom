package chatroom.netty.server.handlers;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;

public class MyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    //根目录
    private static final String WEB_ROOT = "webroot";
    //classPath路径
    private URL baseURL = MyHttpHandler.class.getProtectionDomain().getCodeSource().getLocation();

    private String wsUri;

    public MyHttpHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    /**
     * 根据文件路径在webroot下获取文件
     * @param fileName
     * @return
     */
    private File getFileFromRoot(String fileName) throws Exception {
        //处理路径中含有中文乱码问题
        String basePath = URLDecoder.decode(baseURL.getPath(), "UTF-8");
        String path = basePath + WEB_ROOT + "/" + fileName;
        path = !path.startsWith("file:") ? path : path.substring(5);
        path = path.replaceAll("//", "/");
        return new File(path);
    }

    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();

        // 如果请求了Websocket，协议升级,此HTTP处理器不处理，并将他传递给下一个 ChannelHandler
        if(uri.equalsIgnoreCase("ws")) {
            ctx.fireChannelRead(request.retain());
            return;
        } else {
            String page = uri.equals("/") ? "hello.html" : uri;

            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(getFileFromRoot(page), "r");
            } catch (Exception e) {
                //e.printStackTrace(); 找不到对应文件
                ctx.fireChannelRead(request.retain());
                return;
            }

            //处理不同的响应格式
            String contentType = "text/html";
            if(uri.endsWith(".css")) {
                contentType = "text/css";
            } else if(uri.endsWith(".js")) {
                contentType = "text/javascript";
            } else if(uri.toLowerCase().matches("(jpg|png|gif)$")) {
                String ext = uri.substring(uri.lastIndexOf("."));
                contentType = "images/" + ext + ";";
            }

            //创建一个默认的HTTP响应
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            //设置Content Length
            HttpHeaderUtil.setContentLength(response, file.length());
            //设置Content Type
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType + ";charset=utf-8;");
            //长连接处理
            if(HttpHeaderUtil.isKeepAlive(request)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            //返回response
            ctx.write(response);
            //传输文件
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            //清空缓存
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            //如果不支持keep-Alive，服务器端主动关闭请求
            if (!HttpHeaderUtil.isKeepAlive(request)) {
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
