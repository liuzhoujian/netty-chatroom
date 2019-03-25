package chatroom.netty.protocol;

/**
 * 自定义IM协议
 */
public enum IMP {
    /*系统消息*/
    SYSTEM("SYSTEM"),
    /*登录*/
    LOGIN("LOGIN"),
    /*登出*/
    LOGOUT("LOGOUT"),
    /*聊天*/
    CHAT("CHAT"),
    /*送鲜花*/
    FLOWER("FLOWER");

    private String name;
    IMP(String name) {
        this.name = name;
    }

    public static boolean isIMP(String content) {
        return content.matches("^\\[(SYSTEM|LOGIN|LOGOUT|CHAT|FLOWER)\\]");
    }

    public String getName() {
        return name;
    }
}
