package com.duanglink.mipush;

public class Content {
    public static boolean finishStart = false;
    public String amount = "";
    public String msg_id = "";
    public String msg_sub_type = "";

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String title = "";

    public String getMsg_sub_type() {
        return msg_sub_type;
    }

    public void setMsg_sub_type(String msg_sub_type) {
        this.msg_sub_type = msg_sub_type;
    }
}
